package position;

import java.io.IOException;
import java.util.ArrayList;
//import java.util.Map;
//import java.util.TreeMap;
import java.net.ServerSocket;
import java.net.Socket;

import amak.AmasThread;
//import amak.BlobAgent;
import amak.Migrant;

//https://openclassrooms.com/courses/java-et-la-programmation-reseau/les-sockets-cote-serveur
// https://gfx.developpez.com/tutoriel/java/network/

// https://gfx.developpez.com/tutoriel/java/network/#L4

/**
 * Cette classe permet de lancer un thread pour etablir la connexion et recolter
 * les donnees en temps reel pour les transmettres aux environnements
 * correspondants.
 * <p>
 * Il s'agit de la 2eme version du serveur : de par les soucis � la g�ode, on
 * utilise pas ici IndoorAtlas. Elle a ete utilisee reellement sur place : elle
 * est donc peut-etre plus fonctionnelle que {@link position.ConnectedClient
 * ConnectedClient}.<br>
 * On utilise ici l'orientation (ou on aurait pr�f�r� l'acc�l�ration) du
 * portable, on a donc pas besoin des parametres de la salle. Nous avons convenu
 * de consid�rer qu'une personne adoptant un blob doit se trouver face au mur
 * sur lequel est projet� To, � une distance d'environ 2 m�tres. On obtient
 * ainsi une estimation sur la position.
 * 
 * </p>
 * 
 * 
 * @author Claire Mevolhon
 *
 */

public class ServerThreadAcceleration extends Thread {
	/**
	 * {@link amak.AmasThread le thread amak} pour pouvoir communiquer les nouvelles
	 * positions ou demande d'adoption au SMA.
	 */
	private AmasThread tAmas;
	/** la socket du Serveur */
	private ServerSocket socket;
	private boolean running = false;
	/** port en commun avec celui �crit dans le code de l'application mobile */
	private static int serverPort = 8100;

	/**
	 * Initialisation du thread serveur.
	 * 
	 * @param tAmas
	 *            thread pour pouvoir communiquer avec l'AMAS.
	 * @param migrants
	 *            liste des blobs de To (non utlis�s).
	 */
	public ServerThreadAcceleration(AmasThread tAmas, ArrayList<Migrant> migrants) {
		// this(" - localhost:" + serverPort);
		this.tAmas = tAmas;

	}

	@Override
	public void run() {

		try {
			socket = new ServerSocket(serverPort);
			running = true;
			while (running) {
				try {
					System.out.println("j'�coute");
					final Socket clientSocket = socket.accept();
					System.out.println("Je viens d'entendre qqn");
					new ConnectedClientAcceleration(clientSocket, this);
				} catch (final IOException e) {
					e.printStackTrace();
				}

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * action d'adopter un blob, demand� par un client via l'application mobile.<br>
	 * Le blob passe alors du territoir To � Tr/Ti.<br>
	 * C'est un blob m�r al�atoire de To qui est choisi.<br>
	 * Il doit prendre la poistion coo (donn� en param�tre) dans Tr/Ti.
	 * 
	 * @param coo
	 *            coordonn�e (converti en m�tres) du blob
	 * @return le migrant (pointeur) qui a �t� adopt�.
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
	 * Action de rendre le blob li� au portable (donc � cette instance). <br>
	 * On demande donc � amasThread de faire passer le blob de Tr � To.
	 * 
	 * @param b
	 *            Le blob � rendre.
	 */
	public void rentrerBlob(Migrant b) {
		tAmas.tr_to_t0(b);
	}

	/**
	 * Action de demander � {@link amak.AmasThread tAmas} de positionner le
	 * blob.<br>
	 * (Rappel : seul le package amak est autoris� � faire les changements
	 * concernant les blobs.)
	 * 
	 * @param b
	 *            le blob dont on veut changer la position
	 * @param coo
	 *            les nouvelles coordonn�es � affecter au blob.
	 */
	public void moveBlob(Migrant b, double[] coo) {
		tAmas.move_blob(b, coo);
	}
}
