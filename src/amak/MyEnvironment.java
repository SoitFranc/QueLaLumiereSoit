package amak;

import java.util.ArrayList;
import application.Controller;
//import business.CriticalityFunction;
import fr.irit.smac.amak.Environment;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.Log;

public class MyEnvironment extends Environment {
	/**
	 * Liste des agents actuellement dans Tid�al : les immaginaires ET les Migrants
	 * qui sont sortis
	 */
	private ArrayList<BlobAgent> agents;

	/** Liste des Migrants qui sont actuellement dans To */
	private ArrayList<Migrant> hibernants;

	/** Radius pris en compte pour le voisinage. M�J par un curseur de l'IHM. */
	private int radius;
	/**
	 * Degr� d'isolement : il d�finit le nombre de voisins optimal � avoir. <br>
	 * MaJ par un curseur de l'IHM.
	 */
	private double isolement;
	/**
	 * Pourcentage de d�placements : il d�finit le pourcentage optimal de voisins
	 * devant se d�placer.<br>
	 * MaJ par un curseur de l'IHM.
	 */
	private double stabilite_position;
	/**
	 * Pourcentage d'h�t�rog�n�it� : utilis� comme correspondant au pourcentage de
	 * pr�sence de la couleur pr�dominante du voisinage. <br>
	 * MaJ par un curseur de l'IHM.
	 */
	private double heterogeneite;
	/**
	 * Rayon du Terrain Treel (en m�tres).<br>
	 * Utilis� pour conna�tre les limites de la map lors de d�placement ou cr�ation
	 * d'agents.
	 */
	public double rayonTerrain = 12.5;

	/** Controller : pour la communication avec l'interface graphique */
	private Controller controller;

	/**
	 * Verrou concernant les Arraylistes qui peuvent etre lues par le thread de maj
	 * de l'IHM
	 */
	public final Object lockListes = new Object();

	public Object getLockListes() {
		return lockListes;
	}

	public MyEnvironment(Controller controller) {
		super(Scheduling.DEFAULT, controller);
	}

	/**
	 * Initialisation de l'environnement avec initialisation des diff�rentes
	 * ArrayLists et mise � jour des diff�rentes valeurs des curseurs pour les
	 * criticit�s.
	 */
	@Override
	public void onInitialization() {
		agents = new ArrayList<BlobAgent>();
		hibernants = new ArrayList<Migrant>();
		this.controller = (Controller) params[0];
		isolement = controller.getIsolement();
		stabilite_position = controller.getStabilitePosition();
		heterogeneite = controller.getHeterogenite();
		radius = controller.getRadius();
	}

	public ArrayList<BlobAgent> getAgents() {
		return agents;
	}

	public void setAgents(ArrayList<BlobAgent> agent) {
		this.agents = agent;
	}

	/**
	 * G�n�ration des voisins dans Tideal de l'agents donn� en param�tre, en
	 * utilisant le radius actuel.<br>
	 * M�thode interne de {@link #generateNeighbours(BlobAgent) generateNeighbours}
	 * 
	 * @param subject
	 *            agent qui demande de g�n�rer ses voisins
	 */
	private void generateNeighboursTideal(BlobAgent subject) {
		for (int j = 0; j < agents.size(); j++) {
			if (subject.getBlob().isVoisin(agents.get(j).getBlob(), radius))
				subject.addVoisin(agents.get(j));
		}
	}

	/**
	 * G�n�ration des voisins dans To de l'agents donn� en param�tre, en utilisant
	 * le radius actuel.<br>
	 * M�thode interne de {@link #generateNeighbours(BlobAgent) generateNeighbours}
	 * 
	 * @param subject
	 *            agent qui demande de g�n�rer ses voisins
	 */
	private void generateNeighboursToriginel(BlobAgent subject) {
		for (int j = 0; j < hibernants.size(); j++) {
			if (subject.getBlob().isVoisin(hibernants.get(j).getBlob(), 2 * radius))
				subject.addVoisin(hibernants.get(j));
		}
	}

	/**
	 * G�n�ration des voisins de l'agents donn� en param�tre, en utilisant le radius
	 * actuel.<br>
	 * Cette m�thode est appel� par chaque agent � chaque perception.
	 * 
	 * @param subject
	 *            agent qui demande de g�n�rer ses voisins
	 */
	public void generateNeighbours(BlobAgent subject) {
		subject.clearVoisin();
		if ((subject instanceof Migrant) && ((Migrant) subject).isHome())
			generateNeighboursToriginel(subject);
		else
			generateNeighboursTideal(subject);
	}

	public ArrayList<Migrant> getHibernants() {
		return hibernants;
	}

	public void setHibernants(ArrayList<Migrant> hibernants) {
		this.hibernants = hibernants;
	}

	/**
	 * Cette fonction est appelee par l'agent apres avoir fait le changement. il ne
	 * reste donc ici qu'� mettre � jour les listes dans l'environnement
	 * 
	 * @param agent
	 *            agent actif � ajouter dans Ti
	 */
	public void addAgent(BlobAgent agent) {
		agents.add(agent);
	}

	/**
	 * Enl�ve l'agent en param�tre de la liste des agents actifs (de Ti). Cette
	 * fonction est appelee par l'agent apres avoir fait le changement. il ne reste
	 * donc ici qu'� mettre � jour les listes dans l'environnement
	 * 
	 * @param agent
	 *            agent actif � supprimer de Ti
	 */
	public void removeAgent(BlobAgent agent) {
		agents.remove(agent);
	}

	/**
	 * Ajoute un Migrant � To. Cette fonction est appelee par l'agent apres avoir
	 * fait le changement. il ne reste donc ici qu'� mettre � jour les listes dans
	 * l'environnement
	 * 
	 * @param migrant
	 *            migrant � ajouter dans To
	 */
	public void addMigrant(Migrant migrant) {
		hibernants.add(migrant);
	}

	/**
	 * Cette m�thode met � jour les listes apr�s avoir fait passer un migrant de
	 * Toriginel � Treel. <br>
	 * Cette fonction est appelee par l'agent apres avoir fait le changement. Il ne
	 * reste donc ici qu'� mettre � jour les listes dans l'environnement
	 * 
	 * @param migrant
	 *            migrant qui vient de passer de To � Tr.
	 */
	public void t0_to_tr(Migrant migrant) {
		hibernants.remove(migrant);
		agents.add(migrant);
	}

	/**
	 * Cette m�thode met � jour les listes apr�s avoir fait passer un migrant de
	 * Treel � Toriginel. <br>
	 * Cette fonction est appelee par l'agent apres avoir fait le changement. Il ne
	 * reste donc ici qu'� mettre � jour les listes dans l'environnement
	 * 
	 * @param migrant
	 *            migrant qui vient de passer de Tr � To.
	 */
	public void tr_to_t0(Migrant migrant) {
		agents.remove(migrant);
		hibernants.add(migrant);
	}

	/**
	 * Indique si la coordonnee entree en parametre est valide, ie si elle n'est pas
	 * hors terrain. Retourne true si ok.<br>
	 * Ici il s'agit de To : valide si compris dans un cercle de diametre 100.
	 * (commenter / decommenter pour avoir un carre 100*100)
	 * 
	 * @param coo
	 *            coordonn�e � tester
	 * @return vrai si dans la map.
	 */
	private boolean isValideInTo(double[] coo) {

		// si dans un carre de 100*100
		/*
		 * if (0 < coo[0] && coo[0] < 100 && 0 < coo[1] && coo[1] < 100) return true;
		 */

		// si dans un cercle de diametre 100 ie de rayon 50
		if ((coo[0] - 50) * (coo[0] - 50) + (coo[1] - 50) * (coo[1] - 50) <= 50 * 50)
			return true;
		return false;
	}

	/**
	 * indique si la coordonnee entree en parametre est valide, ie si elle n'est pas
	 * hors terrain. Retourne true si ok. <br>
	 * Ici il s'agit de Tr ou Ti : valide si compris dans un cercle de rayon
	 * {@link #rayonTerrain rayonTerrain} et de centre (rayonTerrain;rayonTerrain)
	 * 
	 * @param coo
	 *            coordonn�e � tester
	 * @return vrai si dans la map
	 */
	public boolean isValideInTi(double[] coo) {
		boolean b = (coo[0] - rayonTerrain) * (coo[0] - rayonTerrain)
				+ (coo[1] - rayonTerrain) * (coo[1] - rayonTerrain) <= rayonTerrain * rayonTerrain;
		Log.debug("quela", "%f %f %f " + b, coo[0], coo[1], rayonTerrain);
		if (b)
			return true;
		return false;
	}

	/**
	 * fonction qui � partir de coordonnees initiales, propose de nouvelles
	 * coordonnees � un certain rayon (le pas).<br>
	 * Utilis� soit pour le d�placement d'un agent, soit lorsqu'un agent procr�e.
	 * 
	 * @param agent
	 *            l'agent qui demande
	 * @param pas
	 *            � quel distance de la coordonnee donn�e en parametre doit se
	 *            trouver la nouvelle coordonn�e
	 * @param coordonnee
	 *            coordonn�e de d�part (en g�n�ral celle de l'agent)
	 * @return nouvelle coordonn�e
	 */
	public double[] nouvellesCoordonneesTT(BlobAgent agent, double pas, double[] coordonnee) {
		double[] res = new double[2];

		// Je dois prendre en compte les bordures. Je decide de ne pas compliquer les
		// calculs :
		// Je mets le tout dans une boucle, et je relance l'aleatoire si je suis en
		// dehors du terrain.
		boolean isOK = false;
		int count = 0;

		while (!isOK) {
			if (count++ > 1000) {

				Log.error("quela", "more than 1000 loop");
				double angle = Math.random() * Math.PI * 2;
				return new double[] { 12.5 + Math.cos(angle) * getRandom().nextDouble() * 12.5,
						12.5 + Math.sin(angle) * getRandom().nextDouble() * 12.5 };
			}

			// normalement : coo[0] - pas < res[0] < coo[0] + pas
			res[0] = (Math.random() * 2 * pas) - pas + coordonnee[0];

			// j'utilise l'equation d'un cercle de rayon pas.
			// (res[0] - coo[0])^2 + (res[1] - coo[1])^2= pas^2
			// a partir de res[0], j'ai 2 solutions possible pour res[1]. 1 positive, une
			// negative. choisissons aleatoirement.
			double sign = 1;
			if (Math.random() < 0.5)
				sign = -1;
			res[1] = coordonnee[1]
					+ (sign * Math.sqrt(pas * pas + (res[0] - coordonnee[0]) * (res[0] - coordonnee[0])));
			if ((agent instanceof Migrant) && ((Migrant) agent).isHome())
				isOK = isValideInTo(res);
			else
				isOK = isValideInTi(res);
		}
		return res;
	}

	/**
	 * Fonction qui � partir des coordonnees de l'agent donn� en param�tre, propose
	 * de nouvelles coordonnees � un certain rayon (le pas), tout en favorisant
	 * l'ancienne direction qui avait �t� prise pr�c�dement laquelle est founie en
	 * param�tre. Cette direction sera enfin remise � jour.<br>
	 * Utilis� pour le d�placement d'un agent.
	 * <p>
	 * Proc�d� :
	 * <ul>
	 * <li>L'agent � 90% de chance de reprendre son ancienne direction</li>
	 * <li>Soit car la coordonn�e est hors map, soit car on est dans les 10% de
	 * chance restant : on fait alors appel � la m�thode
	 * {@link #nouvellesCoordonneesTT(BlobAgent, double, double[])
	 * nouvellesCoordonneesTT}</li>
	 * <li>Enfin on remet � jour la variable pastDirection de l'agent qui a �t�
	 * donn�e en param�tre.</li>
	 * </ul>
	 * 
	 * @param agent
	 *            l'agent qui demande
	 * @param pas
	 *            � quelle distance doit se d�placer l'agent
	 * @param pastDirection
	 *            direction prise pr�c�demment.
	 * @return nouvelle coordonn�e
	 */
	public double[] nouvellesCoordonnees(BlobAgent agent, double pas, double[] pastDirection) {
		double[] res = new double[2];
		double[] coordonnee = agent.getBlob().getCoordonnee();
		boolean isOK = false;
		// Je dois prendre en compte les bordures. Je d�cide de ne pas compliquer les
		// calculs :
		// Je mets le tout dans une boucle, et je relance l'al�atoire si je suis en
		// dehors du terrain.

		if (pastDirection != null && Math.random() * 100 < 90) {
			// je maintiens ma direction pr�c�dente, dont j'ai stock� le vecteur dans
			// pastDirection
			res[0] = coordonnee[0] + pastDirection[0];
			res[1] = coordonnee[1] + pastDirection[1];

			if ((agent instanceof Migrant) && ((Migrant) agent).isHome())
				isOK = isValideInTo(res);
			else
				isOK = isValideInTi(res);
		}

		if (!isOK) // l'ancienne direction ne me dirige pas comme il se doit.
			res = nouvellesCoordonneesTT(agent, pas, agent.getBlob().getCoordonnee());

		// cette fonction est appel�e pour bouger et on pour cr�er.
		// Je remets donc � jour la variable pastDirection du blob en question.
		if (pastDirection == null)
			pastDirection = new double[2];
		pastDirection[0] = res[0] - coordonnee[0];
		pastDirection[1] = res[1] - coordonnee[1];

		// double[] nvlleDirection = new double[2];

		agent.setPastDirection(pastDirection);

		return res;
	}

	/**
	 * Adoption d'un blob. Cette m�thode retourne un migrannt m�r disponible dans
	 * To.<br>
	 * Elle est appel�e par AmasThread de la part d'un thread position. C'est la
	 * 1ere �tape de l'adoption : l'agent n'est pas encore d�plac� : la demande de
	 * passage de To � Tr s'effectue apr�s par AmasThread.
	 * <p>
	 * Appel�e par {@link amak.AmasThread#adopter(double[]) AmasThread.adopter}
	 * </p>
	 * 
	 * @return un migrant m�r de To disponible
	 */
	public Migrant adopter() {
		for (int i = 0; i < hibernants.size(); i++) {
			if (hibernants.get(i).isRiped())
				return hibernants.get(i);
		}
		return null;

	}

	/*
	 * *****************************************************************************
	 * ************ ********************* getter / setter
	 * ****************************************
	 * *****************************************************************************
	 * ******** *
	 */

	public double getIsolement() {
		return isolement;
	}

	public void setIsolement(int isolement) {
		this.isolement = isolement;
		System.out.println("la nouvelle valeur d'isolement a �t� prise en compte");
		// majFctCriticalityStabiliteEtat();
	}

	public double getStabilite_position() {
		return stabilite_position;
	}

	public void setStabilite_position(int stabilite_position) {
		this.stabilite_position = stabilite_position;
		System.out.println("la nouvelle valeur de stabilit� de la position a �t� prise en compte");
	}

	public double getHeterogeneite() {
		return heterogeneite;
	}

	public void setHeterogeneite(int heterogeneite) {
		this.heterogeneite = heterogeneite;
		System.out.println(
				"la nouvelle valeur " + heterogeneite + " d'h�t�rog�n�it� a �t� prise en compte");
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public void setRadiusVoisins(double radiusVoisins) {
		this.radius = (int) radiusVoisins;
	}

}
