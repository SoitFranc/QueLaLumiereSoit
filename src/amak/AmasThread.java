package amak;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import application.Controller;
import javafx.application.Platform;
import position.ServerThread;

public class AmasThread extends Thread{
	Controller controller;
	MyAMAS myAmas;
	ServerThread tposition;
	int nbBlobs;
	private final Lock lock = new ReentrantLock(true);
	
	public ServerThread getTposition() {
		return tposition;
	}

	public void setTposition(ServerThread tposition) {
		this.tposition = tposition;
	}

	public AmasThread(Controller controller, int nbBlobs){
			super();
			this.controller = controller;
			this.nbBlobs = nbBlobs;
	}
	
	// le thread PositionBluetooth transmet le mouvement d'un blob reel, lequel est associé ici à un blob agent
	// il nous faut trouver le bon blobAgent et prévenir l'environnemnt et MyAmas.
	/*
	public void add_blob(Blob b){
		Blob blob = b.copy_blob();
		BlobAgent agent = new BlobAgent(myAmas,blob);
		myAmas.getEnvironment().addAgent(agent);
		//map.put(b, agent);
	}
	*/
		 
	
	
	
	public void move_blob(Migrant b, double[] coo){
		if(!myAmas.getEnvironment().isValideInTi(coo))
		{
			System.out.println("hors map");
		      return; 
		}

		synchronized(b.getBlob().lock) {
			b.getBlob().setCoordonnee(coo);
		}
		controller.move_blobMigrant(b);  // TODO : à voir si je peux supprimer. grace a onUpdateRender
	}
	
	// de la part du thread ConnectedClient
	public Migrant adopter(double[] coo) {
		//P
		//lock.lock(); //Prendre
		Migrant migrant = myAmas.getEnvironment().adopter();
		if(migrant==null)
		{
			//lock.unlock();
			return null;
		}
		t0_to_tr(migrant, coo);
		return migrant;
	}
	
	
	public void t0_to_tr(Migrant blob, double[] coo){
		if(!myAmas.getEnvironment().isValideInTi(coo)) 
	    {// Les coordonnées fournies ne sont pas valides. Je lui affecte une valeur aléatoire dans la salle de diametre
	 
	          blob.t0_to_tr(blob.getBlob().genererCoordonneeAleaDansCercle(25)); 
	          //lock.unlock();//V Laisser 

	    } 
	    else 
	    { 

	          blob.t0_to_tr(coo); 
	          //lock.unlock();//V Laisser 

	    } 
	}
	public void t0_to_tr(Migrant blob){

				blob.t0_to_tr();

	}
	
	public void tr_to_t0(Migrant blob){

				blob.tr_to_t0();

	}
	
	
	
	public void run(){

		MyEnvironment env = new MyEnvironment(controller);
        myAmas = new MyAMAS(env, controller, nbBlobs);
        controller.setBlobHibernants(env.getHibernants());
        
        
        /*
        PositionThread tPosition = new PositionThread(this, env.getHibernants());
		tPosition.start();
        */
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	
	/*public void setCaracteristiques(int isolement, int heterogeneite, int stabilite_etat, int stabilite_position){
		Platform.runLater(new Runnable() {
			public void run() {
				myAmas.getEnvironment().setIsolement(isolement);
				myAmas.getEnvironment().setHeterogeneite(heterogeneite);
				myAmas.getEnvironment().setStabilite_etat(stabilite_etat);
				myAmas.getEnvironment().setStabilite_position(stabilite_position);
			}
		});
	}*/
	
	public void setIsolement(int isolement){
	
				myAmas.getEnvironment().setIsolement(isolement);
				System.out.println("tAmas : changement Taux d'isolement à " + isolement);

	}

	public void setHeterogeneite(int heterogeneite) {
	
				myAmas.getEnvironment().setHeterogeneite(heterogeneite);
				System.out.println("tAmas : changement Taux d'hétérogénéité " + heterogeneite);
			
	}


	public void setStabilitePosition(int stabilite_position) {

				myAmas.getEnvironment().setStabilite_position(stabilite_position);
				System.out.println("tAmas : changement de la Stabilité des positions à " + stabilite_position);
	
		
	}

	public void setRadiusVoisinage(double radiusVoisins) {

				myAmas.getEnvironment().setRadiusVoisins(radiusVoisins);
				System.out.println("tAmas : changement du radius à " + radiusVoisins);

		
	}
	
	public MyEnvironment getEnvironnement() {
		return(myAmas.getEnvironment());
	}
	
	
}

