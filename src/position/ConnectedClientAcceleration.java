package position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import amak.Migrant;
import business.Couleur;

/**
 * Socket pour un Client, utilisée lors de l'exposition au CMES. <br>
 * Elle est ici optimisée pour l'application crée à cette occasion : utilisant
 * l'accéléromètre du portable, et non Indoor Alas.
 * <p>
 * Rappel : à cette occasion, nous avons été déconcerté par l'impossibilité de
 * nous localiser avec précision dans cette salle à effet cage de Farraday.<br>
 * Nous avons convenu de considérer qu'une personne adoptant un blob doit se
 * trouver face au mur sur lequel est projeté To, à une distance d'environ 2
 * mètres. On obtient ainsi une estimation sur la position. Nous avons alors
 * tenté d'utilisé l'accéléromètre du téléphone pour l'application portable.
 * Nous r�coltons donc les diff�rents mouvements calcul�s et renvoy�s par le
 * t�l�phone pour estimer la position du portable et donc du blob dans Tr.
 * </p>
 * <p>
 * Cette classe est instanciée par une instance de ServerTreadAcceleration pour
 * chaque connexion détectée.
 * </p>
 * <p>
 * Attention... : Lors de l'exposition, nous ne sommes pas bien parvenu à
 * récupérer les valeurs de l'accéléromètre. l'application portable devait
 * envoyer un couple ((triplet accélération) ; (triplet orientation)) mais nous
 * n'avons finalement envoyé que l'orientation du portable. <br>
 * Peut-être certaines choses seraient donc à remodifier en cas d'envoi des
 * bonnes valeurs de l'accéléromètre.
 *  
 * @author claire Mevolhon
 *
 */
public class ConnectedClientAcceleration implements Runnable {

	private BufferedReader in;
	private PrintWriter out;
	private ServerThreadAcceleration server;
	private Migrant agent;
	private double[] cooInitiale;
	private Socket socket;

	public ConnectedClientAcceleration(final Socket clientSocket, final ServerThreadAcceleration _server) {
		System.out.println("j'initialise le socket");
		socket = clientSocket;
		try {
			server = _server;
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			new Thread(this).start();
			out = new PrintWriter(clientSocket.getOutputStream());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		// comme expliqu� plus haut : un adoptant est suppos� adopt� un blob en �tant
		// face � la projection de To � une distance approximative de 2m.
		cooInitiale = new double[2];
		cooInitiale[0] = 2;
		cooInitiale[1] = 12.5;

	}

	@Override
	public void run() {

		double[] coo = new double[2];
		String line;
		try {
			while ((line = in.readLine()) != null) {

				System.out.println("Hey ! je viens de recevoir quelque chose !");
				final String[] res = line.split(";");

				if (res[0].equals("move")) {
					double[] resDouble = new double[6];
					for (int i = 1; i < res.length; i++)
						resDouble[i - 1] = Double.parseDouble(res[i]);
					System.out.println("Je recoie " + resDouble[0] + ";" + resDouble[1] + ";" + resDouble[2] + ";"
							+ resDouble[3] + ";" + resDouble[4] + ";" + resDouble[5]);

					if (agent == null) {
						System.out.println("Je n'ai pas encore de blobs. Je vais donc en prendre un");
						System.out.println("Que je place en " + cooInitiale[0] + ";" + cooInitiale[1]);
						agent = server.adopterBlob(cooInitiale);
					} else {
						// Les trois 1eres valeurs me donne l'info de l'orientation du t�l�phone

						// les 3 suivantes sur l'acc�l�ration dans ce rep�re.

						synchronized (agent.getBlob().lock) {
							coo = agent.getBlob().getCoordonnee().clone();
							coo[0] += resDouble[3];
							coo[1] += resDouble[4];
						}

						server.moveBlob(agent, coo);
					}

				}

				ArrayList<double[]> listePos = agent.getBlob().getGlobules_position();
				ArrayList<Couleur> listeCouleur = agent.getBlob().getGlobules_couleurs();
				String str = "" + listePos.get(0)[0] + ";" + listePos.get(0)[1] + ";" + listeCouleur.get(0).toString();
				Couleur couleur;
				for (int i = 1; i < listePos.size(); i++) {
					if ((couleur = listeCouleur.get(i)) == null)
						couleur = Couleur.BLUE;
					str += ":" + listePos.get(i)[0] + ";" + listePos.get(i)[1] + ";" + couleur.toString();
				}
				out.println(str);
				out.flush();

			}

			System.out.println("sortie de la boucle while");
			server.rentrerBlob(agent);

		} catch (final IOException e) {
			System.out.println("erreur Connexion");
			server.rentrerBlob(agent);
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
