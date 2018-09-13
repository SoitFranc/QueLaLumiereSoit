package position;

import java.io.IOException;
import java.util.ArrayList;
//import java.util.Map;
//import java.util.TreeMap;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Policy.Parameters;

import amak.AmasThread;
//import amak.BlobAgent;
import amak.Migrant;

// https://openclassrooms.com/courses/java-et-la-programmation-reseau/les-sockets-cote-serveur
// https://gfx.developpez.com/tutoriel/java/network/
// https://gfx.developpez.com/tutoriel/java/network/#L4

/**
 * 
 * Thread concernant le serveur, lequel n'est pas actuellement utilisé.<br>
 * Cette classe permet de lancer un thread pour établir la connexion et récolter
 * les données en temps réel pour les transmettres aux environnements
 * correspondants
 * <p>
 * Cette classe devait fonctionner avec l'application mobile utilisant
 * IndoorAtlas, lequel fournissait à ce serveur la lattitude et la longitude du
 * téléphone. Au préalable, cette application mobile via Indoor Atlas permet
 * d'enregistrer des points repères du lieu. Le but serait donc de prendre
 * différents points proches de la bordure de la salle (rectangulaire ou
 * cerculaire) qu'on notera en dur dans ce code pour se faire une repésentation
 * du lieu (centre et rayon si c'est une salle ronde). <br>
 * Ainsi lorsqu'on reçoit une coordonnée d'un poprtable, on sait où le placer
 * dans la salle.
 * </p>
 * <p>
 * Autre souci : Nous avons décidé de considérer les positions des blobs en
 * mètres. LA latitude et longitude ne sont pas isométriques... Ce qui ajoute de
 * la complexité dans les calculs. Nous avons abandonné l'application Indoor
 * Atlas pour le projet du CEMES avant d'avoir pu tester ces calculs.
 * </p>
 * 
 * @author claire MEVOLHON
 *
 */

/*
 * Given you're looking for a simple formula, this is probably the simplest way
 * to do it, assuming that the Earth is a sphere of perimeter 40075 km. Length
 * in meters of 1° of latitude = always 111.32 km Length in meters of 1° of
 * longitude = 40075 km * cos( latitude ) / 360
 */

public class ServerThread extends Thread {
	/** liste des blobs hibernants, ie de To */
	private ArrayList<Migrant> blobHibernants;
	/** Liste des blobs actifs ie ceux qui sont adoptés et dans Tr/Ti */
	private ArrayList<Migrant> blobActifs;
	/**
	 * {@link amak.AmasThread le thread amak} pour pouvoir communiquer les nouvelles
	 * positions ou demande d'adoption au SMA.
	 */
	private AmasThread tAmas;
	/**
	 * Estimation du rayon de la salle (laquelle est ronde) calculé dans le
	 * constructeur.
	 */
	private double cooCercleRayon;
	/**
	 * Estimation des coordonnées du centre de la salle, calculées dans le
	 * constructeur.
	 */
	private double[] cooCercleCentre;
	/**
	 * Contient les coordonnées du point en haut à gauche du carré si on suppose la
	 * salle dans un carré. <br>
	 * Il s'agit des coordonnées 0;0 dans l'IHM, ici exprimées en
	 * (latitude,longitude).
	 */
	private double[] cooCercleOrigine; // contient le point en haut à gauche du carré circonscrit

	/** rayon de la salle théorique, connu */
	private double rayonSalle = 17.5; // en metres

	/** la socket du Serveur */
	private ServerSocket socket;
	private boolean running = false;

	/** port en commun avec celui écrit dans le code de l'application mobile */
	private static int serverPort = 8100;

	/**
	 * Initialisation du thread server. C'est également ici que sont écrit les
	 * différents checkpoints relevés sous IndoorAtlas, pour estimer les paramètres
	 * de la salle.
	 * 
	 * @param tAmas
	 *            le thread pour communiquer avec l'AMAS.
	 * @param migrants
	 *            la liste des blobs présents dans TO.
	 */
	public ServerThread(AmasThread tAmas, ArrayList<Migrant> migrants) {
		// this(" - localhost:" + serverPort);
		try {
			socket = new ServerSocket(serverPort);
			running = true;
			// new Thread(this).start();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		this.tAmas = tAmas;
		blobHibernants = migrants;
		blobActifs = new ArrayList<>();

		ArrayList<double[]> checkpoints = new ArrayList<>();
		double tmp[] = new double[2];
		// coo suivants à remplacer par les vrais checkpoints
		// à l'IRIT :
		/*
		 * tmp[0] = 43.56226037; tmp[1] = 1.46760197; checkpoints.add(tmp.clone());
		 * tmp[0] = 43.56226819; tmp[1] = 1.46761181; checkpoints.add(tmp.clone());
		 * tmp[0] = 43.56204986; tmp[1] = 1.46782698; checkpoints.add(tmp.clone());
		 * tmp[0] = 43.5620397; tmp[1] = 1.46780492; checkpoints.add(tmp.clone());
		 */

		// boule du CMES :
		tmp[0] = 43.57992918;
		tmp[1] = 1.46328402;
		checkpoints.add(tmp.clone());
		tmp[0] = 43.58000378;
		tmp[1] = 1.46324747;
		checkpoints.add(tmp.clone());
		tmp[0] = 43.5799379;
		tmp[1] = 1.46348318;
		checkpoints.add(tmp.clone());
		tmp[0] = 43.58000756;
		tmp[1] = 1.46349998;
		checkpoints.add(tmp.clone());
		tmp[0] = 43.58008816;
		tmp[1] = 1.46339072;
		checkpoints.add(tmp.clone());

		System.out.println("calculons les coo du cercle");
		calculCooCercle(checkpoints);
		System.out.println("j'ai trouvé pour centre : " + cooCercleCentre[0] + " ; " + cooCercleCentre[1]);
		System.out.println("pour rayon : " + cooCercleRayon);
		System.out.println("j'ai pour coin en haut à gauche : " + cooCercleOrigine[0] + " ; " + cooCercleOrigine[1]);

	}

	@Override
	public void run() {
		while (running) {
			try {
				System.out.println("j'écoute");
				final Socket clientSocket = socket.accept();
				System.out.println("Je viens d'entendre qqn");
				new ConnectedClient(clientSocket, this);
			} catch (final IOException e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * ***************************************************************** ********
	 * Calculs coo du cercle selon les checkpoints*************
	 * *****************************************************************
	 */

	/**
	 * Calcule l'intersection de 2 droites d1( a1 * X + b1) et d2( a2 * X + b2)
	 * 
	 * @param a1
	 *            coefficient directeur de la droite 1
	 * @param b1
	 *            ordonnée à l'origine de la droite 1
	 * @param a2
	 *            coefficient directeur de la droite 2
	 * @param b2
	 *            ordonnée à l'origine de la droite 2
	 * @return les coordonnées [x;y] de l'intersection des deux droites.
	 */
	private double[] intersection(double a1, double b1, double a2, double b2) {
		double[] res = new double[2];
		res[0] = (b2 - b1) / (a1 - a2);
		res[1] = a1 * res[0] + b1;
		return res;
	}

	// renvoie a et b de l'equation ax+b=y représentant la médiatrice du segment
	// [AB] dont les coordonnees
	// de A et B sont donnes en parametres
	/**
	 * renvoie a et b de l'equation ax+b=y représentant la médiatrice du segment
	 * [AB] dont les coordonnees de A et B sont donnes en parametres
	 * 
	 * @param cooA
	 *            les coordonnées du point A
	 * @param cooB
	 *            les coordonnées du point B
	 * @return les paramètres de la droite [coeff directeur; ordonnée à l'origine]
	 *         de la médiatrice [AB]
	 */
	private double[] calculMediatrice(double[] cooA, double[] cooB) {
		// trouvons l'equation Ax + B = y de la mediatrice
		double A = -(cooA[0] - cooB[0]) / (cooA[1] - cooB[1]); // perpandiculaire à (AB)
		double B = (cooA[1] + cooB[1]) / 2 - A * (cooA[0] + cooB[0]) / 2; // par coo du point au milieu de [AB]

		double[] res = new double[2];
		res[0] = A;
		res[1] = B;
		return res;
	}

	/**
	 * Utilise les différents checkpoints supposés sur la périphérie de la salle
	 * circulaire, pour déterminer le centre et le rayon de la salle (par
	 * approximation des différentes intersections des médiatrices des cordes
	 * obtenues par les checkpoints.)
	 * 
	 * @param checkpoints
	 *            une liste des différentes coordonnées : checkpoints
	 */
	private void calculCooCercle(ArrayList<double[]> checkpoints) {
		double[] res = new double[2];
		int cpt = 0;
		double[] med1; // mediatrice 1
		double[] med2; // mediatrice 2
		double[] tmp;
		for (int i = 0; i < checkpoints.size() - 2; i++) {
			med1 = calculMediatrice(checkpoints.get(i), checkpoints.get(i + 1));
			med2 = calculMediatrice(checkpoints.get(i), checkpoints.get(i + 2));
			tmp = intersection(med1[0], med1[1], med2[0], med2[1]);
			res[0] += tmp[0];
			res[1] += tmp[1];
			cpt++;
		}
		res[0] /= cpt;
		res[1] /= cpt;
		cooCercleCentre = res; // ici : tout a ete fait en Longitude-Latitude.
		cooCercleRayon = calculeDistance(cooCercleCentre, checkpoints.get(0));
		cooCercleOrigine = new double[2];
		cooCercleOrigine[0] = cooCercleCentre[0] - cooCercleRayon;
		cooCercleOrigine[1] = cooCercleCentre[1] - cooCercleRayon;

	}

	/**
	 * calcule la distance euclidienne entre 2 points cooA et cooB
	 * 
	 * @param cooA
	 *            coordonnée du point A
	 * @param cooB
	 *            coordonnée du point B
	 * @return la distance euclidienne AB
	 */
	private double calculeDistance(double[] cooA, double[] cooB) {
		double sum = 0;
		for (int i = 0; i < cooA.length; i++)
			sum += ((cooB[i] - cooA[i]) * (cooB[i] - cooA[i]));
		return Math.sqrt(sum);

	}

	/**
	 * à partir de coordonnées [latitude ; longitude] communiqué par le téléphone
	 * renvoie la position du téléphone (donc du blob) dans le reprère utilisé en
	 * mètre.
	 * 
	 * @param coo
	 *            coordonnée du portable [latitude ; longitude]
	 * @return coordonnée du blob [x;y] en mètres.
	 */
	double[] cooGeolocToMetre(double[] coo) {
		double[] res = new double[2];
		res[0] = (coo[0] - cooCercleOrigine[0]) / cooCercleRayon * rayonSalle;
		res[1] = (coo[1] - cooCercleOrigine[1]) / cooCercleRayon * rayonSalle;

		// res[0] = coo[0]/cooCercleRayon * rayonSalle;
		// res[1] = coo[1]/cooCercleRayon * rayonSalle;
		return res;
	}

	/*
	 * public void run(){
	 * 
	 * //testByConsole();
	 * 
	 * startServer();
	 * 
	 * 
	 * }
	 * 
	 * private void startServer() { try { server = new ServerSocket(serverPort);
	 * server.setSoTimeout(1000); } catch (IOException ioe) {
	 * System.err.println("[Cannot initialize Server]\n" + ioe); System.exit(1); }
	 * 
	 * }
	 * 
	 * 
	 * private void getClient() {
	 * 
	 * 
	 * Socket client = server.accept();
	 * 
	 * //running = true; new Thread(this).start();
	 * 
	 * //new Authorizer(client, password); }
	 */

	/**
	 * action d'adopter un blob, demandé par un client via l'application mobile.<br>
	 * Le blob passe alors du territoir To à Tr/Ti.<br>
	 * C'est un blob mûr aléatoire de To qui est choisi.<br>
	 * Il doit prendre la poistion coo (donné en paramètre) dans Tr/Ti.
	 * 
	 * @param coo
	 *            coordonnée (converti en mètres) du blob
	 * @return le migrant (pointeur) qui a été adopté.
	 */
	public Migrant adopterBlob(double[] coo) {
		return (tAmas.adopter(coo));
	}

	/*
	 * public void sortirBlob(Migrant b, double[] coordonnees){ Blob tmp =
	 * b.getBlob(); double[] coo = new double[2]; coo[0] = Math.random() * 25;
	 * boolean isOk = false; while(!isOk){ coo[1] = Math.random() * 25; if ((coo[0]
	 * - 12.5)*(coo[0] - 12.5) + (coo[1] - 12.5) * (coo[1] - 12.5) <= 12.5 * 12.5)
	 * isOk = true; }
	 * 
	 * tmp.setCoordonnee(coo); b.setBlob(tmp); tAmas.t0_to_tr(b);
	 * blobHibernants.remove(b); blobActifs.add(b); }
	 */

	/**
	 * Action de rendre le blob lié au portable (donc à cette instance). <br>
	 * On demande donc à amasThread de faire passer le blob de Tr à To.
	 * 
	 * @param b
	 *            Le blob à rendre.
	 */
	public void rentrerBlob(Migrant b) {
		tAmas.tr_to_t0(b);
		blobHibernants.add(b);
		blobActifs.remove(b);
	}

	/**
	 * Action de demander à {@link amak.AmasThread tAmas} de positionner le
	 * blob.<br>
	 * (Rappel : seul le package amak est autorisé à faire les changements
	 * concernant les blobs.)
	 * 
	 * @param b
	 *            le blob dont on veut changer la position
	 * @param coo
	 *            les nouvelles coordonnées à affecter au blob.
	 */
	public void moveBlob(Migrant b, double[] coo) {
		tAmas.move_blob(b, coo);
	}
}
