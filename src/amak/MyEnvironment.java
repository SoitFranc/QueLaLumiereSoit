package amak;

import java.util.ArrayList;

import application.Controller;
//import business.CriticalityFunction;
import fr.irit.smac.amak.Environment;
import fr.irit.smac.amak.Scheduling;

public class MyEnvironment extends Environment {
	private ArrayList<BlobAgent> agents;
	private ArrayList<Migrant> hibernants;
	private int radius = 4; // radius utilisé pour les différents sliders
	/* possede les valeurs des differents curseurs*/
	private double isolement;
	//private double stabilite_etat;
	private double stabilite_position;
	private double heterogeneite;
	//private double tauxMurissemnt;
	public double rayonTerrain = 12.5; // exprimé en metres
	//private CriticalityFunction fctCriticalityStabiliteEtat;
	//private int nbInitialBlob;
	
	/* communication avec l'interface graphique */
	private Controller controller;
	
	
	public MyEnvironment(Controller controller) {
		super(Scheduling.DEFAULT, controller);
	}
	
	
	@Override
	public void onInitialization(){
		agents = new ArrayList<BlobAgent>();
		hibernants = new ArrayList<Migrant>();
		//this.setRealBlobs(realBlobs);
		this.controller = (Controller) params[0];
		isolement = controller.getIsolement();
		stabilite_position = controller.getStabilitePosition();
		heterogeneite = controller.getHeterogenite();
		//tauxMurissemnt = controller.getTauxMurissement();
		//fctCriticalityStabiliteEtat = new CriticalityFunction(-1.2, 1.2, -0.05, 0.05);
		//fctCriticalityStabiliteEtat = new CriticalityFunction(-(1 - stabilite_etat/100) * 0.05 - 1, (1 - stabilite_etat/100) * 0.05 + 1, -(1 - stabilite_etat/100) * 1.2 - 0.5, (1 - stabilite_etat/100) * 1.2 + 0.5);
	}

	
	// j'ai considéré que cette criticité prend en compte l'isolement et le curseur stabilite états.
	// j'appelle donc cette méthode en cas de changement de chacun des 2 curseurs.
	/*private void majFctCriticalityStabiliteEtat(){
		fctCriticalityStabiliteEtat.setParameters(-(1 - stabilite_etat/100) * 0.05 - 1, (1 - stabilite_etat/100) * 0.05 + 1, -(1 - stabilite_etat/100) * 1.2 - 0.5, (1 - stabilite_etat/100) * 1.2 + 0.5);
	}
	*/
	
	public ArrayList<BlobAgent> getAgents() {
		return agents;
	}

	public void setAgents(ArrayList<BlobAgent> agent) {
		this.agents = agent;
	}
	
	
	
	private void generateNeighboursTideal(BlobAgent subject){
		for (int j = 0; j < agents.size(); j++ )
		{
			if(subject.getBlob().isVoisin(agents.get(j).getBlob(), radius))
				subject.addVoisin(agents.get(j));
		}
	}
	private void generateNeighboursToriginel(BlobAgent subject){
		for (int j = 0; j < hibernants.size(); j++ )
		{
			if(subject.getBlob().isVoisin(hibernants.get(j).getBlob(), 2*radius))
				subject.addVoisin(hibernants.get(j));
		}
	}
	
	
	public void generateNeighbours(BlobAgent subject){
		subject.clearVoisin();
		if( (subject instanceof Migrant) && ((Migrant)subject).isHome())
			generateNeighboursToriginel(subject);
		else
			generateNeighboursTideal(subject);
	}


	public ArrayList<Migrant> getHibernants() {
		return hibernants;
	}


	public void setHibernants(ArrayList<Migrant> hibernants) {
		this.hibernants = hibernants;
	}

	//Cette fonction est appelée par l'agent après avoir fait le changement.
	// il ne reste donc ici qu'à mettre à jour les listes dans l'environnement
	public void addAgent(BlobAgent agent) {
		agents.add(agent);
	}

	//Cette fonction est appelée par l'agent après avoir fait le changement.
	// il ne reste donc ici qu'à mettre à jour les listes dans l'environnement
	public void removeAgent(BlobAgent agent) {
		agents.remove(agent);
	}
	
	//Cette fonction est appelée par l'agent après avoir fait le changement.
	// il ne reste donc ici qu'à mettre à jour les listes dans l'environnement
	public void addMigrant(Migrant migrant) {
		hibernants.add(migrant);
	}	

	//Cette fonction est appelée par l'agent après avoir fait le changement.
	// il ne reste donc ici qu'à mettre à jour les listes dans l'environnement
	public void t0_to_tr(Migrant migrant){
		hibernants.remove(migrant);
		agents.add(migrant);
	}
	
	//Cette fonction est appelée par l'agent après avoir fait le changement.
	// il ne reste donc ici qu'à mettre à jour les listes dans l'environnement
	public void tr_to_t0(Migrant migrant){
		agents.remove(migrant);
		hibernants.add(migrant);
	}
	
	
	// indique si la coordonnée entrée en paramètre est valide, ie si elle n'est pas hors terrain.
	// returne true if ok. 
	//Ici il s'agit de To : valide si compris dans un carré de 100*100
	private boolean isValideInTo(double[] coo){
		
		// si dans un carré de 100*100
		/*if (0 < coo[0] && coo[0] < 100 && 0 < coo[1] && coo[1] < 100)
			return true;*/
		
		// si dans un cercle de diametre 100 ie de rayon 50
		if ((coo[0] - 50)*(coo[0] - 50) + (coo[1] - 50) * (coo[1] - 50) <= 50 * 50)
			return true;
		return false;
	}
	
	// indique si la coordonnée entrée en paramètre est valide, ie si elle n'est pas hors terrain.
	// returne true if ok. 
	//Ici il s'agit de Tr ou Ti : valide si compris dans un cercle de rayon RayonTerrain et de centre (RayonTerrain;RayonTerrain)
	private boolean isValideInTi(double[] coo){
		if ((coo[0] - rayonTerrain)*(coo[0] - rayonTerrain) + (coo[1] - rayonTerrain) * (coo[1] - rayonTerrain) <= rayonTerrain * rayonTerrain)
			return true;
		return false;
	}
	
	
	
	
	
	// fonction qui à partir de coordonnées initiales, propose de nouvelles coordonnées à un certain rayon (le pas).
		public double[] nouvellesCoordonnees(BlobAgent agent, double pas){
			double[] res = new double[2];
			double[] coordonnee = agent.getBlob().getCoordonnee();
			
			// Je dois prendre en compte les bordures. Je décide de ne pas compliquer les calculs : 
			// Je mets le tout dans une boucle, et je relance l'aléatoire si je suis en dehors du terrain.
			boolean isOK = false;
			while(!isOK){
			
				//normalement : coo[0] - pas < res[0] < coo[0] + pas
				res[0] = (Math.random() * 2 * pas) - pas + coordonnee[0];
			
				// j'utilise l'equation d'un cercle de rayon pas.
				// (res[0] - coo[0])² + (res[1] - coo[1])² = pas²
				// à partir de res[0], j'ai 2 solutions possible pour res[1]. 1 positive, une négative. choisissons aléatoirement.
				double sign = 1;
				if (Math.random() < 0.5)
					sign = -1;
				res[1] =  coordonnee[1] + (sign * Math.sqrt(pas * pas + (res[0] - coordonnee[0]) * (res[0] - coordonnee[0]) ));
				if( (agent instanceof Migrant) && ((Migrant)agent).isHome())
					isOK = isValideInTo(res);
				else
					isOK = isValideInTi(res);
			}	
			return res;
		}
		
		public double[] nouvellesCoordonnees(BlobAgent agent, double pas, double[] pastDirection){
			double[] res = new double[2];
			double[] coordonnee = agent.getBlob().getCoordonnee();
			boolean isOK = false;
			// Je dois prendre en compte les bordures. Je décide de ne pas compliquer les calculs : 
			// Je mets le tout dans une boucle, et je relance l'aléatoire si je suis en dehors du terrain.
			
			
			
			if ( pastDirection != null && Math.random()*100 < 90)
			{
				// je maintiens ma direction précédente, dont j'ai stocké le vecteur dans pastDirection
				res[0] = coordonnee[0] + pastDirection[0];
				res[1] = coordonnee[1] + pastDirection[1];
				
				if( (agent instanceof Migrant) && ((Migrant)agent).isHome())
					isOK = isValideInTo(res);
				else
					isOK = isValideInTi(res);
			}
			
			
			if(!isOK) // l'ancienne direction ne me dirige pas comme il se doit.
				res = nouvellesCoordonnees(agent, pas);
			
			// cette fonction est appelée pour bouger et on pour créer.
			// Je remets donc à jour la variable pastDirection du blob en question.
			if(pastDirection == null)
				pastDirection = new double[2];
			pastDirection[0] = res[0] - coordonnee[0];
			pastDirection[1] = res[1] - coordonnee[1];
			
			//double[] nvlleDirection = new double[2];
			
			agent.setPastDirection(pastDirection);
			
			
			return res;
		}
	

	
	/* *****************************************************************************************
	 * *********************   getter / setter			****************************************
	 * ************************************************************************************* * */
	
	public double getIsolement() {
		return isolement;
	}

	public void setIsolement(int isolement) {
		this.isolement = isolement;
		System.out.println("la nouvelle valeur d'isolement a été prise en compte");
		//majFctCriticalityStabiliteEtat();
	}


	public double getStabilite_position() {
		return stabilite_position;
	}

	public void setStabilite_position(int stabilite_position) {
		this.stabilite_position = stabilite_position;
		System.out.println("la nouvelle valeur de stabilité de la position a été prise en compte");
	}

	public double getHeterogeneite() {
		return heterogeneite;
	}

	public void setHeterogeneite(int heterogeneite) {
		this.heterogeneite = heterogeneite;
		System.out.println("la nouvelle valeur " + heterogeneite + " d'hétérogénéité a été prise en compte");
	}


	public Controller getController() {
		return controller;
	}


	public void setController(Controller controller) {
		this.controller = controller;
	}


	public void setRadiusVoisins(double radiusVoisins) {
		this.radius = (int)radiusVoisins;
	}

	public Migrant adopter() {
		for (int i = 0; i < hibernants.size(); i++) {
			if(hibernants.get(i).isRiped())
				return hibernants.get(i);
		}
		return null;
		
	}
	
	
	
}
