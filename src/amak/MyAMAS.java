package amak;

import java.util.Scanner;

import application.Controller;

//import java.util.ArrayList;

import business.Blob;
import business.Couleur;
import business.Forme;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Scheduling;
import javafx.application.Platform;

public class MyAMAS extends Amas<MyEnvironment>{
	
	private Controller controller;
	
	@Override
	protected void onInitialConfiguration() {
		
		System.out.println("combien de Blobs sont � cr�er dans To ?");
		@SuppressWarnings("resource")
		int nbBlobs = new Scanner(System.in).nextInt();
		Migrant migrant;
		double xcor;
		double ycor;
		Couleur[] couleurListe = Couleur.values();
		int indiceCouleur; 
		Blob blob;
		controller = (Controller) params[0];		
		for(int i = nbBlobs ; i > 0 ; i--){
			xcor = Math.random() * ( 100 );
			ycor = Math.random() * ( 100 );
			indiceCouleur = (int) (Math.random() * ( couleurListe.length ));
			blob = new Blob(xcor,ycor, couleurListe[indiceCouleur], 1, Forme.carre, true);
			migrant = new Migrant(this, blob, controller);
			getEnvironment().addMigrant(migrant);
		}
		super.onInitialConfiguration();
		System.out.println("fin de l'initilisation de MyAmas");
	}
	
	public MyAMAS(MyEnvironment env, Controller controller) {
		super(env, Scheduling.DEFAULT, controller);
	}

	
	@Override
    protected void onInitialAgentsCreation() {
	}
	
	
	
	protected void addAgent(Blob b){
		BlobAgent agent = new BlobAgent(this, b, controller);
		getEnvironment().addAgent(agent);
	}
	
	protected void moveAgent(Blob b, BlobAgent agent){
		Platform.runLater(new Runnable() {
			public void run() {
				agent.setBlob(b);
				
				// normalement, pr�vient donc l'environnement
			}
		});
	}
	
	protected void removeAgent(Blob b, BlobAgent agent){
		Platform.runLater(new Runnable() {
			public void run() {
				getEnvironment().getAgents().remove(agent);
				//agents.remove(agent);
				// normalement, pr�vient donc l'environnement
			}
		});
		
	}
	
	
	
}