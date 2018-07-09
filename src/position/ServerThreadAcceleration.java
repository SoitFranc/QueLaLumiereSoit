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


// permet de lancer un thread pour �tablir la connexion
// et r�colter les donn�es en temps r�el
// pour les transmettres aux environnements correspondants




public class ServerThreadAcceleration extends Thread{
	//private ArrayList<Migrant> blobHibernants;
	//private ArrayList<Migrant> blobActifs;
	private AmasThread tAmas;
			
	private ServerSocket socket;
	private boolean running = false;
	
	private static int serverPort = 8100;
	
	
	

	public ServerThreadAcceleration(AmasThread tAmas, ArrayList<Migrant> migrants) {
		//this(" - localhost:" + serverPort);
		this.tAmas = tAmas;
			//
		
		//blobHibernants = migrants;
	//	blobActifs = new ArrayList<>();
		
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
//		blobHibernants.add(b);
//		blobActifs.remove(b);
	}
	
	public void moveBlob(Migrant b, double[] coo){
		tAmas.move_blob(b, coo);	
	}    
}

