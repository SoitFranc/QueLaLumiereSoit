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


// permet de lancer un thread pour établir la connexion
// et récolter les données en temps réel
// pour les transmettres aux environnements correspondants


/* Given you're looking for a simple formula, this is probably the simplest way to do it, assuming that the Earth is a sphere of perimeter 40075 km.
 * Length in meters of 1° of latitude = always 111.32 km
 * Length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
*/


public class ServerThread extends Thread{
	private ArrayList<Migrant> blobHibernants;
	private ArrayList<Migrant> blobActifs;
	private AmasThread tAmas;
	private double cooCercleRayon;
	private double[] cooCercleCentre;
	private double[] cooCercleOrigine; // contient le point en haut à gauche du carré circonscrit
	
	private double rayonSalle = 17.5 ; // en metres
	
	//private BufferedReader in;
	
	private ServerSocket socket;
	private boolean running = false;
	//private final Map<String, BlobAgent> agent = new TreeMap<String, BlobAgent>();
	//private boolean uniqueWindow;
	
	//private ServerSocket server;
	private static int serverPort = 8100;
	
	
	

	public ServerThread(AmasThread tAmas, ArrayList<Migrant> migrants) {
		//this(" - localhost:" + serverPort);
		try {
			socket = new ServerSocket(serverPort);
			running = true;
			new Thread(this).start();
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
		tmp[0] = 43.56226037;
		tmp[1] = 1.46760197;
		checkpoints.add(tmp.clone());
		tmp[0] = 43.56226819;
		tmp[1] = 1.46761181;
		checkpoints.add(tmp.clone());
		tmp[0] = 43.56204986;
		tmp[1] = 1.46782698;
		checkpoints.add(tmp.clone());
		tmp[0] = 43.5620397;
		tmp[1] = 1.46780492;
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
	
	
	
	
	/* **********************************************************************************************
	 * ***************			Calculs coo du cercle selon	les checkpoints	************************
	 * ********************************************************************************************* */
	
	// calcule l'intersection de 2 droites d1( a1 * X + b1) et d2( a2 * X + b2)
	private double[] intersection(double a1, double b1, double a2, double b2)
	{
		double[] res = new double[2];
		res[0] = (b2 - b1)/(a1 - a2);
		res[1] = a1 * res[0] + b1;
		return res;
	}
	
	
	// renvoie a et b de l'equation ax+b=y représentant la médiatrice du segment [AB] dont les coordonnees
	// de A et B sont donnes en parametres
	private double[] calculMediatrice(double[] cooA, double[] cooB) {
		// trouvons l'equation Ax + B = y de la mediatrice 
		double A = -(cooA[0] - cooB[0])/(cooA[1] - cooB[1]); // perpandiculaire à (AB)
		double B = (cooA[1] + cooB[1])/2 - A * (cooA[0] + cooB[0])/2; // par coo du point au milieu de [AB]
					
		double[] res = new double[2];
		res[0] = A;
		res[1] = B;
		return res;	
	}
	
	
	private void calculCooCercle(ArrayList<double[]> checkpoints) {
		double[] res = new double[2];
		int cpt = 0;
		double[] med1; //mediatrice 1
		double[] med2;	// mediatrice 2
		double[] tmp;
		for (int i = 0; i < checkpoints.size() - 2; i++)
		{
			med1 = calculMediatrice(checkpoints.get(i), checkpoints.get(i+1));
			med2 = calculMediatrice(checkpoints.get(i), checkpoints.get(i+2));
			tmp = intersection(med1[0], med1[1], med2[0], med2[1]);
			res[0] += tmp[0];
			res[1] += tmp[1];
			cpt++;
		}
		res[0] /= cpt;
		res[1] /= cpt;
		cooCercleCentre = res;	// ici : tout a ete fait en Longitude-Latitude.
		cooCercleRayon = calculeDistance(cooCercleCentre, checkpoints.get(0));
		cooCercleOrigine = new double[2];
		cooCercleOrigine[0] = cooCercleCentre[0] - cooCercleRayon;
		cooCercleOrigine[1] = cooCercleCentre[1] - cooCercleRayon;
				
	}
	
	/* calcule la distance euclidienne entre 2 points cooA et cooB */
	private double calculeDistance(double[] cooA, double[] cooB){
		double sum = 0;
		for(int i = 0; i < cooA.length ; i++)
			sum += ((cooB[i] - cooA[i])*(cooB[i] - cooA[i]));
		return Math.sqrt(sum);		
		
	}
	
	double[] cooGeolocToMetre(double[] coo) {
		double[] res = new double[2];
		res[0] = (coo[0] - cooCercleOrigine[0]) / cooCercleRayon * rayonSalle;
		res[1] = (coo[1] - cooCercleOrigine[1]) / cooCercleRayon * rayonSalle;

		//res[0] = coo[0]/cooCercleRayon * rayonSalle;
		//res[1] = coo[1]/cooCercleRayon * rayonSalle;
		return res;
	}
	
	
	
	
	
	
	
	/*
	public void run(){
		
		//testByConsole();
		
		 startServer();
		
		
	}   
	
	private void startServer() {
		try
		{
		  server = new ServerSocket(serverPort);
		  server.setSoTimeout(1000);
		} catch (IOException ioe) {
		  System.err.println("[Cannot initialize Server]\n" + ioe);
		  System.exit(1);
		}
		
	}
	
	
	private void getClient() {
		
		
		Socket client = server.accept();
		
		//running = true;
		new Thread(this).start();
		
		//new Authorizer(client, password);
	}
	*/
	
	
	
	public Migrant adopterBlob(double[] coo) {
		return(tAmas.adopter(coo));		
	}
	
	
	
	/*
	public void sortirBlob(Migrant b, double[] coordonnees){
		Blob tmp = b.getBlob();
		double[] coo = new double[2];
		coo[0] = Math.random() * 25;
		boolean isOk = false;
		while(!isOk){
			coo[1] = Math.random() * 25;
			if ((coo[0] - 12.5)*(coo[0] - 12.5) + (coo[1] - 12.5) * (coo[1] - 12.5) <= 12.5 * 12.5)
				isOk = true;
		}
		
		tmp.setCoordonnee(coo);
		b.setBlob(tmp);
		tAmas.t0_to_tr(b);
		blobHibernants.remove(b);
		blobActifs.add(b);	
	}*/
	
	
	public void rentrerBlob(Migrant b){
		tAmas.tr_to_t0(b);
		blobHibernants.add(b);
		blobActifs.remove(b);
	}
	
	public void moveBlob(Migrant b, double[] coo){
		tAmas.move_blob(b, coo);	
	}    
}

