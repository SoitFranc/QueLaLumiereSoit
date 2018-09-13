package amak;

import application.Controller;

//import java.util.ArrayList;

import business.Blob;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.Log;

/**
 * MyAMAS de AMAK
 * 
 * @author claire
 *
 */
public class MyAMAS extends Amas<MyEnvironment> {

	private Controller controller;

	/**
	 * genere des coordonnees cartesiennes aleatoires dans un cercle de diametre 100
	 * et de centre 50;50
	 * 
	 * @return la coordonnée
	 */
	private double[] genererCoordonneeCercle() {
		boolean isOk = false;
		double[] res = new double[2];
		double xcor = 0;
		double ycor = 0;

		while (!isOk) {
			xcor = Math.random() * (100);
			ycor = Math.random() * (100);

			if ((xcor - 50) * (xcor - 50) + (ycor - 50) * (ycor - 50) <= 50 * 50)
				isOk = true;
		}
		res[0] = xcor;
		res[1] = ycor;
		return res;
	}

	/**
	 * Cette méthode de Amak fonctionne avec le constructeur. Elle possède donc en
	 * parmètre le controller et le nombre de Blobs nbBlobs.
	 * <p>
	 * Le nombre de Blobs a été défini dans l'IHM.
	 * </p>
	 * <p>
	 * On va ici créer les nbBlobs agents(Migrants) dans To à des coordonnées
	 * aléatoires.<br>
	 * On met alors ensuite à jour les listes de blobs du controller et de
	 * l'environement.
	 * </p>
	 */
	@Override
	protected void onInitialConfiguration() {
		int nbBlobs = (int) params[1];
		Migrant migrant;
		// double xcor;
		// double ycor;
		Blob blob;
		controller = (Controller) params[0];
		for (int i = nbBlobs; i > 0; i--) {

			// si dans un cercle
			double[] coo = genererCoordonneeCercle();
			blob = new Blob(coo[0], coo[1], true);

			// si dans un carre :
			// xcor = Math.random() * ( 100 );
			// ycor = Math.random() * ( 100 );
			// blob = new Blob(xcor,ycor, true);

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

	/*
	 * protected void addAgent(Blob b) { BlobAgent agent = new BlobAgent(this, b,
	 * controller); getEnvironment().addAgent(agent); }
	 * 
	 * 
	 * protected void moveAgent(Blob b, BlobAgent agent) { agent.setBlob(b); }
	 * 
	 * protected void removeAgent(Blob b, BlobAgent agent) {
	 * getEnvironment().getAgents().remove(agent);
	 * 
	 * }
	 */

	@Override
	protected void onSystemCycleEnd() {
		Log.debug("quela", "cycle end");
	}

}