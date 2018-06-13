package amak;

import java.util.ArrayList;

import application.Controller;
import fr.irit.smac.amak.Environment;
import fr.irit.smac.amak.Scheduling;

public class MyEnvironment extends Environment {
	private ArrayList<BlobAgent> agents;
	private ArrayList<Migrant> hibernants;
	private int radius = 4; // radius utilis� pour les diff�rents sliders
	/* possede les valeurs des differents curseurs*/
	private int isolement;
	private double stabilite_etat;
	private double stabilite_position;
	private double heterogeneite;
	private double distanceRealite;
	private double tauxMurissemnt;
	public double rayonTerrain = 12.5; // exprim� en metres
	
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
		stabilite_etat = controller.getStabiliteHeterogeneite();
		stabilite_position = controller.getStabilitePosition();
		heterogeneite = controller.getHeterogenite();
		distanceRealite = controller.getDistanceRealite();
		tauxMurissemnt = controller.getTauxMurissement();
		
	}

	

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
			if(subject.getBlob().isVoisin(hibernants.get(j).getBlob(), radius))
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

	//Cette fonction est appel�e par l'agent apr�s avoir fait le changement.
	// il ne reste donc ici qu'� mettre � jour les listes dans l'environnement
	public void addAgent(BlobAgent agent) {
		agents.add(agent);
	}

	//Cette fonction est appel�e par l'agent apr�s avoir fait le changement.
	// il ne reste donc ici qu'� mettre � jour les listes dans l'environnement
	public void removeAgent(BlobAgent agent) {
		agents.remove(agent);
	}
	
	//Cette fonction est appel�e par l'agent apr�s avoir fait le changement.
	// il ne reste donc ici qu'� mettre � jour les listes dans l'environnement
	public void addMigrant(Migrant migrant) {
		hibernants.add(migrant);
	}	

	//Cette fonction est appel�e par l'agent apr�s avoir fait le changement.
	// il ne reste donc ici qu'� mettre � jour les listes dans l'environnement
	public void t0_to_tr(Migrant migrant){
		hibernants.remove(migrant);
		agents.add(migrant);
	}
	
	//Cette fonction est appel�e par l'agent apr�s avoir fait le changement.
	// il ne reste donc ici qu'� mettre � jour les listes dans l'environnement
	public void tr_to_t0(Migrant migrant){
		agents.remove(migrant);
		hibernants.add(migrant);
	}
	
	
	// indique si la coordonn�e entr�e en param�tre est valide, ie si elle n'est pas hors terrain.
	// returne true if ok. 
	//Ici il s'agit de To : valide si compris dans un carr� de 100*100
	private boolean isValideInTo(double[] coo){
		if (0 < coo[0] && coo[0] < 100 && 0 < coo[1] && coo[1] < 100)
			return true;
		return false;
	}
	
	// indique si la coordonn�e entr�e en param�tre est valide, ie si elle n'est pas hors terrain.
	// returne true if ok. 
	//Ici il s'agit de Tr ou Ti : valide si compris dans un cercle de rayon RayonTerrain et de centre (RayonTerrain;RayonTerrain)
	private boolean isValideInTi(double[] coo){
		if ((coo[0] - rayonTerrain)*(coo[0] - rayonTerrain) + (coo[1] - rayonTerrain) * (coo[1] - rayonTerrain) <= rayonTerrain * rayonTerrain)
			return true;
		return false;
	}
	
	
	
	
	
	// fonction qui � partir de coordonn�es initiales, propose de nouvelles coordonn�es � un certain rayon (le pas).
		public double[] nouvellesCoordonnees(BlobAgent agent, double pas){
			double[] res = new double[2];
			double[] coordonnee = agent.getBlob().getCoordonnee();
			
			// Je dois prendre en compte les bordures. Je d�cide de ne pas compliquer les calculs : 
			// Je mets le tout dans une boucle, et je relance l'al�atoire si je suis en dehors du terrain.
			boolean isOK = false;
			while(!isOK){
			
				//normalement : coo[0] - pas < res[0] < coo[0] + pas
				res[0] = (Math.random() * 2 * pas) - pas + coordonnee[0];
			
				// j'utilise l'equation d'un cercle de rayon pas.
				// (res[0] - coo[0])� + (res[1] - coo[1])� = pas�
				// � partir de res[0], j'ai 2 solutions possible pour res[1]. 1 positive, une n�gative. choisissons al�atoirement.
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
			// Je dois prendre en compte les bordures. Je d�cide de ne pas compliquer les calculs : 
			// Je mets le tout dans une boucle, et je relance l'al�atoire si je suis en dehors du terrain.
			
			
			
			if ( pastDirection != null && Math.random()*100 < 75)
			{
				// je maintiens ma direction pr�c�dente, dont j'ai stock� le vecteur dans pastDirection
				res[0] = coordonnee[0] + pastDirection[0];
				res[1] = coordonnee[1] + pastDirection[1];
				
				if( (agent instanceof Migrant) && ((Migrant)agent).isHome())
					isOK = isValideInTo(res);
				else
					isOK = isValideInTi(res);
			}
			
			
			if(!isOK) // l'ancienne direction ne me dirige pas comme il se doit.
				res = nouvellesCoordonnees(agent, pas);
			
			// cette fonction est appel�e pour bouger et on pour cr�er.
			// Je remets donc � jour la variable pastDirection du blob en question.
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
	
	public int getIsolement() {
		return isolement;
	}

	public void setIsolement(int isolement) {
		this.isolement = isolement;
		System.out.println("la nouvelle valeur d'isolement a �t� prise en compte");
	}

	public double getStabilite_etat() {
		return stabilite_etat;
	}

	public void setStabilite_etat(int stabilite_etat) {
		this.stabilite_etat = stabilite_etat;
		System.out.println("la nouvelle valeur de la stabilit� d'�tat a �t� prise en compte");
	}

	public double getStabilite_position() {
		return stabilite_position;
	}

	public void setStabilite_position(int stabilite_position) {
		this.stabilite_position = stabilite_position;
		System.out.println("la nouvelle valeur de stabilit� de la position a �t� prise en compte");
	}

	public double getHeterogeneite() {
		return heterogeneite;
	}

	public void setHeterogeneite(int heterogeneite) {
		this.heterogeneite = heterogeneite;
		System.out.println("la nouvelle valeur " + heterogeneite + " d'h�t�rog�n�it� a �t� prise en compte");
	}


	public Controller getController() {
		return controller;
	}


	public void setController(Controller controller) {
		this.controller = controller;
	}

	public double getDistanceRealite() {
		return distanceRealite;
	}

	public void setDistanceRealite(int distanceRealite) {
		this.distanceRealite = distanceRealite;
	}


	public double getTauxMurissemnt() {
		return tauxMurissemnt;
	}


	public void setTauxMurissemnt(double tauxMurissemnt) {
		this.tauxMurissemnt = tauxMurissemnt;
	}


	
}
