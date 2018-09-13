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
 * Socket client non utilisée. Elle a été créée pour fonctionner avec
 * l'application mobile utilisant Indoor-Atlas. L'application avec Indoor-Atlas
 * ayant été abandonnée avant sa finition, il est possible qu'il faille quelques
 * changements si reprise sous cette voie.
 * <p>
 * Cette classe est appelée par une instance ServerThread.
 * </p>
 * 
 * @author claire Mevolhon
 *
 */
public class ConnectedClient implements Runnable {

	private BufferedReader in;
	private PrintWriter out;
	private ServerThread server;
	private Migrant agent;

	public ConnectedClient(final Socket clientSocket, final ServerThread _server) {
		System.out.println("j'initialise le socket");
		try {
			server = _server;
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			new Thread(this).start();
			out = new PrintWriter(clientSocket.getOutputStream());
			// out.println("Vous êtes connecté zéro !");
			// out.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		String line;
		try {
			while ((line = in.readLine()) != null) {
				System.out.println("Hey ! je viens de recevoir quelque chose !");
				final String[] res = line.split(";");

				if (res[0].equals("move")) {
					double[] coo = new double[2];
					coo[0] = Double.parseDouble(res[1]);
					coo[1] = Double.parseDouble(res[2]);
					System.out.println("Je demande de mettre le blob à " + coo[0] + ";" + coo[1]);

					if (agent == null) {
						System.out.println("Je n'ai pas encore de blobs. Je vais donc en prendre un");
						System.out.println("Que je place en " + server.cooGeolocToMetre(coo)[0] + ";"
								+ server.cooGeolocToMetre(coo)[1]);
						agent = server.adopterBlob(server.cooGeolocToMetre(coo));
					} else
						server.moveBlob(agent, server.cooGeolocToMetre(coo));

				}

				ArrayList<double[]> listePos = agent.getBlob().getGlobules_position();
				ArrayList<Couleur> listeCouleur = agent.getBlob().getGlobules_couleurs();
				String str = "" + listePos.get(0)[0] + ";" + listePos.get(0)[1] + ";" + listeCouleur.get(0).toString();

				for (int i = 1; i < listePos.size(); i++) {
					str += ":" + listePos.get(i)[0] + ";" + listePos.get(i)[1] + ";" + listeCouleur.get(i).toString();
				}
				out.println(str);
				out.flush();

				/*
				 * if (res[0].equals("adopter")) {
				 * 
				 * server.sortirBlob(agent); //.getChart(res[1], ChartType.valueOf(res[2]),
				 * true, Integer.valueOf(res[3])); } else if (res[0].equals("move")) {
				 * 
				 * server.moveBlob(agent, coo);
				 * 
				 * if (res[2].isEmpty()) { server.getChart(res[1], ChartType.LINE, true,
				 * -1).add( Double.parseDouble(res[3]), Double.parseDouble(res[4])); } else {
				 * server.getChart(res[1], ChartType.LINE, true, -1).add(res[2],
				 * Double.parseDouble(res[3]), Double.parseDouble(res[4])); } } else if
				 * (res[0].equals("close")) { server.getChart(res[1], ChartType.LINE, true,
				 * -1).close(); }
				 */
			}
		} catch (final IOException e) {

		}
	}

}
