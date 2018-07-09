package amak;

import application.Controller;

//import java.util.ArrayList;

import business.Blob;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.Log;
import javafx.application.Platform;

public class MyAMAS extends Amas<MyEnvironment>{
	
	private Controller controller;
	
	// genere des coordonn�es cart�siennes aleatoires dans un cercle de diametre 100 et de centre 50;50
	private double[] genererCoordonneeCercle(){
		boolean isOk = false;
		double[] res = new double[2];
		double xcor = 0;
		double ycor = 0;
		
		while(!isOk)
		{
			xcor = Math.random() * ( 100 );
			ycor = Math.random() * ( 100 );
			
			if ((xcor - 50)*(xcor - 50) + (ycor - 50)*(ycor - 50) <= 50*50)
				isOk = true;
		}
		res[0] = xcor;
		res[1] = ycor;
		return res;
	}
	
	
	@Override
	protected void onInitialConfiguration() {
		int nbBlobs = (int) params[1];
		Migrant migrant;
		//double xcor;
		//double ycor;
		Blob blob;
		controller = (Controller) params[0];		
		for(int i = nbBlobs ; i > 0 ; i--){
			
			// si dans un cercle
			double[] coo = genererCoordonneeCercle();
			blob = new Blob(coo[0],coo[1], true);

			
			// si dans un carr� :
			//xcor = Math.random() * ( 100 );
			//ycor = Math.random() * ( 100 );
			//blob = new Blob(xcor,ycor, true);
			
			migrant = new Migrant(this, blob, controller);
			getEnvironment().addMigrant(migrant);
			controller.add_blobHibernant(migrant);
		}
		super.onInitialConfiguration();
		System.out.println("fin de l'initilisation de MyAmas");
	}
	
	public MyAMAS(MyEnvironment env, Controller controller, int nbBlobs) {
		super(env, Scheduling.DEFAULT, controller, nbBlobs);
	}

	
	
	
	protected void addAgent(Blob b){
		BlobAgent agent = new BlobAgent(this, b, controller);
		getEnvironment().addAgent(agent);
	}
	
	protected void moveAgent(Blob b, BlobAgent agent){
//		Platform.runLater(new Runnable() {
//			public void run() {
				agent.setBlob(b);
				
				// normalement, pr�vient donc l'environnement
//			}
//		});
	}
	
	protected void removeAgent(Blob b, BlobAgent agent){
//		Platform.runLater(new Runnable() {
//			public void run() {
				getEnvironment().getAgents().remove(agent);
				//agents.remove(agent);
				// normalement, pr�vient donc l'environnement
//			}
//		});
		
	}
	
	@Override
	protected void onSystemCycleEnd() {
		Log.debug("quela", "cycle end");
	}
	
}