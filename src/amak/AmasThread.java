package amak;

import application.Controller;
import position.ServerThread;

/**
 * Thread servant d'interface entre AMAK et les autres packages (application ou
 * positions)
 * <ul>
 * <li>Il est initialis� par le controller de l'IHM (application.Controller)
 * </li>
 * <li>Il permet l'initialisation de AMAK</li>
 * <li>Il sert d'interface pour toutes les m�thodes en destination de AMAK</li>
 * </ul>
 * 
 * @author Claire MEVOLHON
 *
 */
public class AmasThread extends Thread {
	/**
	 * Controller (=ihm) permettant de dialoguer avec lui. Concerne � la fois
	 * l'affichage des blobs et la mise � jour des curseurs
	 */
	Controller controller;
	/**
	 * MyAMAS de AMAK. Permet d'acceder principalement � l'environnement pour
	 * atteindre les blobs
	 */
	MyAMAS myAmas;
	/**
	 * ServerThread permettant de dialoguer avec le thread Serveur. R�cup�re surtout
	 * les nouvelles positions et renvoie les infos des Blobs
	 */
	ServerThread tposition;
	/** nombre de Blobs r�els � mettre : donn�e par l'IHM application.Controller */
	int nbBlobs;

	public ServerThread getTposition() {
		return tposition;
	}

	public void setTposition(ServerThread tposition) {
		this.tposition = tposition;
	}

	/**
	 * constructeur. Initialis� par application.Controller .
	 * 
	 * @param controller
	 *            lien vers le controller de l'application
	 * @param nbBlobs
	 *            nombre de Blobs r�els � creer dans TO. Fourni par l'utilisateur
	 *            dans l'IHM.
	 */
	public AmasThread(Controller controller, int nbBlobs) {
		super();
		this.controller = controller;
		this.nbBlobs = nbBlobs;
	}

	/**
	 * Demande de changement de position d'un blob.
	 * <ul>
	 * <li>Le changement n'est effectu� que par Amak.</li>
	 * <li>on fournit donc une nouvelle coordonn�e en param�tre.</li>
	 * <li>Si une mauvaise coordonn�e est demand�e (en dehors de la map) : la
	 * demande est ignor�e.</li>
	 * <li>Cette m�thode est appel�e soit par application.Controller si en mode
	 * Test, soit par position.ServerThread ou position.ServerThreadAcceleration si
	 * en mode experience.</li>
	 * </ul>
	 * 
	 * @param b
	 *            l'agent � modifier (seul un Migrant est autoris�)
	 * @param coo
	 *            la nouvelle coordonn�e � affecter
	 */
	public void move_blob(Migrant b, double[] coo) {
		if (!myAmas.getEnvironment().isValideInTi(coo)) {
			System.out.println("hors map");
			return;
		}

		synchronized (b.getBlob().lock) {
			b.getBlob().setCoordonnee(coo);
		}
		controller.move_blobMigrant(b);
	}

	/**
	 * Demande d'adoption d'un Blob : on permet donc le passage d'un agent m�r de TO
	 * vers Tr et on retourne le migrant en question (pour que le thread appelant
	 * ait les infos sur ce blob).
	 * 
	 * <p>
	 * M�thode appel�e par le thread ConnectedClient.
	 * </p>
	 * <p>
	 * Proc�d� :
	 * <ul>
	 * <li>On demande � l'environnement pour r�cup�rer un blob m�r</li>
	 * <li>Renvoie null si aucun blob n'est m�r.</li>
	 * <li>On demande � cet agent d'effectuer son passage de To � Tr par la m�thode
	 * t0_to_tr. Le changement de terrain sera en r�alit� effectif qu'� la fin du
	 * cycle de AMAK.</li>
	 * </ul>
	 * 
	 * @see t0_to_tr
	 * @param coo
	 *            coordonn�e o� doit se placer le blob � sa sortie sur Tr
	 * @return l'agent (de type Migrant) qui s'est fait adopter.
	 */
	public Migrant adopter(double[] coo) {

		Migrant migrant = myAmas.getEnvironment().adopter();
		if (migrant == null) {
			return null;
		}
		t0_to_tr(migrant, coo);
		return migrant;
	}

	/**
	 * demande de passage de l'agent blob de To vers Tr avec une coordonn�e pr�cise
	 * dans Tr
	 * <p>
	 * Cette m�thode fait une v�rification sur la coordonn�e demand�e. Si une
	 * mauvaise coordonn�e est fournie, on effectue tout de m�me le passage dans Tr
	 * mais avec une coordonn�ee al�atoire valide.
	 * </p>
	 * <p>
	 * appel � la fonction {@link amak.Migrant#t0_to_tr() Migrant.t0_to_tr} avec la
	 * coordonn�e valide.
	 * </p>
	 * 
	 * @param blob
	 *            Migrant � d�placer de To � Tr
	 * @param coo
	 *            Coordonn�e voulue dans Tr
	 */
	public void t0_to_tr(Migrant blob, double[] coo) {
		if (!myAmas.getEnvironment().isValideInTi(coo))
			// Les coordonn�es fournies ne sont pas valides. Je lui affecte une valeur
			// aléatoire dans la salle de diametre
			blob.t0_to_tr();// (blob.getBlob().genererCoordonneeAleaDansCercle(25));
		else
			blob.t0_to_tr(coo);
	}

	/**
	 * demande de passage d'un agent (Migrant) de To � Tr, sans coordonn�e
	 * pr�d�finie. l'agent sera donc plac� � une coordonn�e al�atoire dans Tr.
	 * <p>
	 * Cette m�thode fait appel � la m�thode {@link amak.Migrant#t0_to_tr()
	 * Migrant.t0_to_tr}. Ce changement sera effectif qu'� la fin du cycle de AMAS.
	 * </p>
	 * 
	 * @param blob
	 *            l'agent (Migrant) � d�placer de To � Tr
	 */
	public void t0_to_tr(Migrant blob) {
		blob.t0_to_tr();
	}

	/**
	 * demande de passage d'un agent(Migrant) de Tr vers To
	 * 
	 * Cette m�thode fait appel � la m�thode {@link amak.Migrant#t0_to_tr()} <br>
	 * Le changement ne sera effectif qu'� la fin du cycle de AMAK <br>
	 * L'agent sera plac� � une coordonn�e al�atoire dans To <br>
	 * 
	 * @param blob
	 *            l'agent (Migrant) � d�placer de Tr � To.
	 */
	public void tr_to_t0(Migrant blob) {
		blob.tr_to_t0();
	}

	/**
	 * d�marre le thread, et initialise MyAMAS de AMAK, avec un nombre de blob qui a
	 * �t� d�fini dans l'IHM.
	 * <p>
	 * Cette m�thode met � jour la liste des blobs hibernants dans To de la classe
	 * application.Controller (l'IHM)
	 * </p>
	 */
	public void run() {
		MyEnvironment env = new MyEnvironment(controller);
		myAmas = new MyAMAS(env, controller, nbBlobs);
		controller.setBlobHibernants(env.getHibernants());
	}

	/**
	 * getter du Controller de l'IHM
	 * 
	 * @return le controller
	 */
	public Controller getController() {
		return controller;
	}

	/**
	 * setter de l'IHM
	 * 
	 * @param controller
	 *            le controller actif
	 */
	public void setController(Controller controller) {
		this.controller = controller;
	}

	/**
	 * Met � jour la valeur optimale de l'isolement.
	 * <ul>
	 * <li>Demand� par l'IHM lors d'une modification du curseur.</li>
	 * <li>Met � jour cette donn�e dans l'environnement de l'AMAS</li>
	 * </ul>
	 * 
	 * @see amak.MyEnvironment#setIsolement(int)
	 * @param isolement
	 *            nouvelle valeur du curseur "degr� d'isolement"
	 */
	public void setIsolement(int isolement) {

		myAmas.getEnvironment().setIsolement(isolement);
		System.out.println("tAmas : changement Taux d'isolement à " + isolement);

	}

	/**
	 * Met � jour la valeur optimale de l'h�t�rog�n�it�.
	 * <ul>
	 * <li>Demand� par l'IHM lors d'une modification du curseur.</li>
	 * <li>Met � jour cette donn�e dans l'environnement de l'AMAS</li>
	 * </ul>
	 * 
	 * @see amak.MyEnvironment#setHeterogeneite(int)
	 * @param heterogeneite
	 *            nouvelle valeur du curseur "pourcentage d'h�t�rog�n�it�"
	 */
	public void setHeterogeneite(int heterogeneite) {

		myAmas.getEnvironment().setHeterogeneite(heterogeneite);
		System.out.println("tAmas : changement Taux d'h�t�rog�n�it� " + heterogeneite);

	}

	/**
	 * Met � jour la valeur optimale de la stabilit� de position.
	 * <ul>
	 * <li>Demand� par l'IHM lors d'une modification du curseur.</li>
	 * <li>Met � jour cette donn�e dans l'environnement de l'AMAS</li>
	 * </ul>
	 * 
	 * @see amak.MyEnvironment#setStabilite_position(int)
	 * @param stabilite_position
	 *            nouvelle valeur du curseur "stabilit� de position"
	 */
	public void setStabilitePosition(int stabilite_position) {

		myAmas.getEnvironment().setStabilite_position(stabilite_position);
		System.out.println("tAmas : changement de la Stabilit� des positions � " + stabilite_position);

	}

	/**
	 * Met � jour la valeur du radius utilis� pour le voisinage.
	 * <ul>
	 * <li>Demand� par l'IHM lors d'une modification du curseur.</li>
	 * <li>Met � jour cette donn�e dans l'environnement de l'AMAS</li>
	 * </ul>
	 * 
	 * @see amak.MyEnvironment#setRadiusVoisins(double)
	 * @param radiusVoisins
	 *            nouvelle valeur du curseur "Radius Voisins"
	 */
	public void setRadiusVoisinage(double radiusVoisins) {

		myAmas.getEnvironment().setRadiusVoisins(radiusVoisins);
		System.out.println("tAmas : changement du radius à " + radiusVoisins);

	}

	/**
	 * getter de l'environnement de l'AMAS
	 * <ul>
	 * <li>utilis� notamment pour r�cup�rer des infos sur les blobs par les autres
	 * threads</li>
	 * </ul>
	 * 
	 * @return l'environnement de l'AMAS
	 */
	public MyEnvironment getEnvironnement() {
		return (myAmas.getEnvironment());
	}
}
