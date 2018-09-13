package amak;

import application.Controller;
import position.ServerThread;

/**
 * Thread servant d'interface entre AMAK et les autres packages (application ou
 * positions)
 * <ul>
 * <li>Il est initialisé par le controller de l'IHM (application.Controller)
 * </li>
 * <li>Il permet l'initialisation de AMAK</li>
 * <li>Il sert d'interface pour toutes les méthodes en destination de AMAK</li>
 * </ul>
 * 
 * @author Claire MEVOLHON
 *
 */
public class AmasThread extends Thread {
	/**
	 * Controller (=ihm) permettant de dialoguer avec lui. Concerne à la fois
	 * l'affichage des blobs et la mise à jour des curseurs
	 */
	Controller controller;
	/**
	 * MyAMAS de AMAK. Permet d'acceder principalement à l'environnement pour
	 * atteindre les blobs
	 */
	MyAMAS myAmas;
	/**
	 * ServerThread permettant de dialoguer avec le thread Serveur. Récupère surtout
	 * les nouvelles positions et renvoie les infos des Blobs
	 */
	ServerThread tposition;
	/** nombre de Blobs réels à mettre : donnée par l'IHM application.Controller */
	int nbBlobs;

	public ServerThread getTposition() {
		return tposition;
	}

	public void setTposition(ServerThread tposition) {
		this.tposition = tposition;
	}

	/**
	 * constructeur. Initialisé par application.Controller .
	 * 
	 * @param controller
	 *            lien vers le controller de l'application
	 * @param nbBlobs
	 *            nombre de Blobs réels à creer dans TO. Fourni par l'utilisateur
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
	 * <li>Le changement n'est effectué que par Amak.</li>
	 * <li>on fournit donc une nouvelle coordonnée en paramètre.</li>
	 * <li>Si une mauvaise coordonnée est demandée (en dehors de la map) : la
	 * demande est ignorée.</li>
	 * <li>Cette méthode est appelée soit par application.Controller si en mode
	 * Test, soit par position.ServerThread ou position.ServerThreadAcceleration si
	 * en mode experience.</li>
	 * </ul>
	 * 
	 * @param b
	 *            l'agent à modifier (seul un Migrant est autorisé)
	 * @param coo
	 *            la nouvelle coordonnée à affecter
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
	 * Demande d'adoption d'un Blob : on permet donc le passage d'un agent mûr de TO
	 * vers Tr et on retourne le migrant en question (pour que le thread appelant
	 * ait les infos sur ce blob).
	 * 
	 * <p>
	 * Méthode appelée par le thread ConnectedClient.
	 * </p>
	 * <p>
	 * Procédé :
	 * <ul>
	 * <li>On demande à l'environnement pour récupérer un blob mûr</li>
	 * <li>Renvoie null si aucun blob n'est mûr.</li>
	 * <li>On demande à cet agent d'effectuer son passage de To à Tr par la méthode
	 * t0_to_tr. Le changement de terrain sera en réalité effectif qu'à la fin du
	 * cycle de AMAK.</li>
	 * </ul>
	 * 
	 * @see t0_to_tr
	 * @param coo
	 *            coordonnée où doit se placer le blob à sa sortie sur Tr
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
	 * demande de passage de l'agent blob de To vers Tr avec une coordonnée précise
	 * dans Tr
	 * <p>
	 * Cette méthode fait une vérification sur la coordonnée demandée. Si une
	 * mauvaise coordonnée est fournie, on effectue tout de même le passage dans Tr
	 * mais avec une coordonnéee aléatoire valide.
	 * </p>
	 * <p>
	 * appel à la fonction {@link amak.Migrant#t0_to_tr() Migrant.t0_to_tr} avec la
	 * coordonnée valide.
	 * </p>
	 * 
	 * @param blob
	 *            Migrant à déplacer de To à Tr
	 * @param coo
	 *            Coordonnée voulue dans Tr
	 */
	public void t0_to_tr(Migrant blob, double[] coo) {
		if (!myAmas.getEnvironment().isValideInTi(coo))
			// Les coordonnées fournies ne sont pas valides. Je lui affecte une valeur
			// alÃ©atoire dans la salle de diametre
			blob.t0_to_tr();// (blob.getBlob().genererCoordonneeAleaDansCercle(25));
		else
			blob.t0_to_tr(coo);
	}

	/**
	 * demande de passage d'un agent (Migrant) de To à Tr, sans coordonnée
	 * prédéfinie. l'agent sera donc placé à une coordonnée aléatoire dans Tr.
	 * <p>
	 * Cette méthode fait appel à la méthode {@link amak.Migrant#t0_to_tr()
	 * Migrant.t0_to_tr}. Ce changement sera effectif qu'à la fin du cycle de AMAS.
	 * </p>
	 * 
	 * @param blob
	 *            l'agent (Migrant) à déplacer de To à Tr
	 */
	public void t0_to_tr(Migrant blob) {
		blob.t0_to_tr();
	}

	/**
	 * demande de passage d'un agent(Migrant) de Tr vers To
	 * 
	 * Cette méthode fait appel à la méthode {@link amak.Migrant#t0_to_tr()} <br>
	 * Le changement ne sera effectif qu'à la fin du cycle de AMAK <br>
	 * L'agent sera placé à une coordonnée aléatoire dans To <br>
	 * 
	 * @param blob
	 *            l'agent (Migrant) à déplacer de Tr à To.
	 */
	public void tr_to_t0(Migrant blob) {
		blob.tr_to_t0();
	}

	/**
	 * démarre le thread, et initialise MyAMAS de AMAK, avec un nombre de blob qui a
	 * été défini dans l'IHM.
	 * <p>
	 * Cette méthode met à jour la liste des blobs hibernants dans To de la classe
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
	 * Met à jour la valeur optimale de l'isolement.
	 * <ul>
	 * <li>Demandé par l'IHM lors d'une modification du curseur.</li>
	 * <li>Met à jour cette donnée dans l'environnement de l'AMAS</li>
	 * </ul>
	 * 
	 * @see amak.MyEnvironment#setIsolement(int)
	 * @param isolement
	 *            nouvelle valeur du curseur "degré d'isolement"
	 */
	public void setIsolement(int isolement) {

		myAmas.getEnvironment().setIsolement(isolement);
		System.out.println("tAmas : changement Taux d'isolement Ã  " + isolement);

	}

	/**
	 * Met à jour la valeur optimale de l'hétérogénéité.
	 * <ul>
	 * <li>Demandé par l'IHM lors d'une modification du curseur.</li>
	 * <li>Met à jour cette donnée dans l'environnement de l'AMAS</li>
	 * </ul>
	 * 
	 * @see amak.MyEnvironment#setHeterogeneite(int)
	 * @param heterogeneite
	 *            nouvelle valeur du curseur "pourcentage d'hétérogénéité"
	 */
	public void setHeterogeneite(int heterogeneite) {

		myAmas.getEnvironment().setHeterogeneite(heterogeneite);
		System.out.println("tAmas : changement Taux d'hétérogénéité " + heterogeneite);

	}

	/**
	 * Met à jour la valeur optimale de la stabilité de position.
	 * <ul>
	 * <li>Demandé par l'IHM lors d'une modification du curseur.</li>
	 * <li>Met à jour cette donnée dans l'environnement de l'AMAS</li>
	 * </ul>
	 * 
	 * @see amak.MyEnvironment#setStabilite_position(int)
	 * @param stabilite_position
	 *            nouvelle valeur du curseur "stabilité de position"
	 */
	public void setStabilitePosition(int stabilite_position) {

		myAmas.getEnvironment().setStabilite_position(stabilite_position);
		System.out.println("tAmas : changement de la Stabilité des positions à  " + stabilite_position);

	}

	/**
	 * Met à jour la valeur du radius utilisé pour le voisinage.
	 * <ul>
	 * <li>Demandé par l'IHM lors d'une modification du curseur.</li>
	 * <li>Met à jour cette donnée dans l'environnement de l'AMAS</li>
	 * </ul>
	 * 
	 * @see amak.MyEnvironment#setRadiusVoisins(double)
	 * @param radiusVoisins
	 *            nouvelle valeur du curseur "Radius Voisins"
	 */
	public void setRadiusVoisinage(double radiusVoisins) {

		myAmas.getEnvironment().setRadiusVoisins(radiusVoisins);
		System.out.println("tAmas : changement du radius Ã  " + radiusVoisins);

	}

	/**
	 * getter de l'environnement de l'AMAS
	 * <ul>
	 * <li>utilisé notamment pour récupérer des infos sur les blobs par les autres
	 * threads</li>
	 * </ul>
	 * 
	 * @return l'environnement de l'AMAS
	 */
	public MyEnvironment getEnvironnement() {
		return (myAmas.getEnvironment());
	}
}
