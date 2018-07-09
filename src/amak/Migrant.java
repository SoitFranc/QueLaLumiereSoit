package amak;

import application.Controller;
import application.ExceptionHandler;
import business.Blob;
import business.Critere;

public class Migrant extends BlobAgent{
	
	
	private boolean isHome;
	private boolean isRiped;
	private int cptRiped;
	//private int cpt_hibernation;
	//private int nbRipedIdeal = 1;
	private double tauxMurissement = 5;
	private double[] cooFutur;
	private boolean isGoingToMove;
	
	
	
	public Migrant(MyAMAS amas, Blob b, Controller controller) {
		super(amas, b, controller);
		isHome = true;
		isRiped = false;
		isGoingToMove = false;
		cptRiped = 1;
	}

	public boolean isHome() {
		return isHome;
	}

	public void setHome(boolean isHome) {
		this.isHome = isHome;
	}
	
	
	// boolean renvoyant true avec une probabilité de 'tauxMurissement' géré dans l'IHM.
	private boolean mustRipe(){
		return( Math.random() * 100 < tauxMurissement);
	}
	
	
	/*
	private double computeCriticalityMurissement(){
		// je compte le nombre de voisins murs autour de moi.
		double cpt = 0;
		for (int i = 0; i< voisins.size(); i++){
			if( ( (Migrant)(voisins.get(i))).isRiped)
				cpt++;
		}
		return(nbRipedIdeal - cpt);
	}
	
	private double computeCriticalityPositionTo(){
		// je compte le nombre de voisins qui bougent autour de moi.
		double cpt = 0;
		for (int i = 0; i< voisins.size(); i++){
			if( ( (Migrant)(voisins.get(i))).isRiped)
				cpt++;
		}
		return(nbRipedIdeal - cpt);
	}
	
	private double computeCriticalityIsolementTo(){
		//if (nbBlobs / 2 > 1 )
		
		return(getAmas().getEnvironment().getIsolement() - voisins.size());
	}
	*/
	
	
	private void action_murir(){
		isRiped = true;
		cptRiped = 20;
	}
	
	@Override
	protected void onDecideAndAct() {
		try {
			synchronized(blob.lock)
			{
				nbChangements = 0;
				currentAction = Action.RESTER; // to initialise
				if (isHome){
					if(isRiped){
						if(cptRiped <= 0)
							isRiped = false;
						else
							cptRiped--;
					}
					else
						if(mustRipe())
							action_murir(); //isRiped = true;
					
					action_se_deplacer();
				}			
				else
					majAspectAgent();
				BlobAgent agentNeedingHelp = super.getMoreCriticalAgent();
				Critere most_critic = Most_critical_critere(agentNeedingHelp.getCriticite());
				
				// Si je suis sans TR/TI ne peux pas me mouvoir. Je ne peux donc pas gérer la criticité de position
				// Je vais aider le plus critique sur une autre de ses criticités.
				if (!isHome && most_critic == Critere.Stabilite_position){
					double[] tmp = agentNeedingHelp.getCriticite();
					tmp[Critere.Heterogeneite.getValue()] = 0;
					most_critic = Most_critical_critere(tmp);
				}
				
				 switch (most_critic){
				 case Isolement:
					 // too few neighboors -> criticite.ISOLEMENT > 0 -> I have procreate
					 if(criticite[Critere.Isolement.getValue()] > 0)
						 action_creer();
					 break;
						 
				 case Stabilite_position:					 
					 break;
					 
				 case Heterogeneite:
					 // if >0 then it's too homogeneous. --> I change the color in a random one.
					 // else it's too heterogeneous.  -> I change my color to the most present color
					 if(criticite[Critere.Heterogeneite.getValue()] > 0)
						 action_changerCouleur();
					 else
						 action_changerCouleur(couleurEnvironnante);
		
					 break;
				 
				 default:
					break;		 
				 }
			}
			super.onDecideAndAct();
		} catch(Exception e)
		{
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}	
	}
	
	
	@Override
    protected void onUpdateRender() {
		
		if (tps + 1000 > System.currentTimeMillis() && !(currentAction.equals(Action.CREER) || currentAction.equals(Action.MURIR))) {
			return;
		}
		tps = System.currentTimeMillis();
		try {
	    	switch(currentAction){
	    	case CREER :
	    		synchronized (this)
	            {
	    			super.controller.add_blobImmaginaire(newFils);
	            }
				break;
	    		
			default:
				if(isHome)
					synchronized (this)
			        {
						super.controller.move_blobHibernant(this);
			        }
	    		else
	    			synchronized (this)
	    	        {
	    				super.controller.move_blobMigrant(this);
	    	        }
	    		break;
	    	}
		} catch(Exception e)
		{
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
    }
	
	public void t0_to_tr(){
		try {
			isHome = false;
			blob.setCoordonnee(blob.genererCoordonneeAleaDansCercle(getAmas().getEnvironment().rayonTerrain * 2));
			getAmas().getEnvironment().t0_to_tr(this);
			controller.add_blobMigrant(this);
			controller.remove_blobHibernant(this);
		} catch(Exception e)
		{
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}
	
	public void t0_to_tr(double[] coo){
		try {
			isHome = false;
			blob.setCoordonnee(coo);
			getAmas().getEnvironment().t0_to_tr(this);
			controller.add_blobMigrant(this);
			controller.remove_blobHibernant(this);
		} catch(Exception e)
		{
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	@Override
	protected void onAgentCycleEnd() {
		
		if(isGoingToMove)
		{
			isHome = true;
			isGoingToMove = false;
			getAmas().getEnvironment().tr_to_t0(this);
			blob.setCoordonnee(cooFutur);
			controller.add_blobHibernant(this);
			controller.remove_blobMigrant(this);
		}
		if(currentAction.equals(Action.CREER))
			getAmas().getEnvironment().addAgent(newFils);
		
		
		super.onAgentCycleEnd();
	}
	
	public void tr_to_t0(){
		try {
			
			cooFutur = blob.genererCoordonneeAleaDansCercle(100);
			isGoingToMove = true;
			// j'attends la fin du cycle pour me mettre à jour
			//blob.setCoordonnee(blob.genererCoordonneeAleaDansCercle(100));
			/*
			getAmas().getEnvironment().tr_to_t0(this);
			controller.add_blobHibernant(this);
			controller.remove_blobMigrant(this);*/
		}catch(Exception e)
		{
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}
	
//	public void tr_to_t0(double[] coo){
//		try {
//			isHome = true;
//			blob.setCoordonnee(coo);
//			getAmas().getEnvironment().tr_to_t0(this);
//			controller.add_blobHibernant(this);
//			controller.remove_blobMigrant(this);
//		} catch(Exception e)
//		{
//			ExceptionHandler eh = new ExceptionHandler();
//			eh.showError(e);
//		}
//	}

	
	
	/*private double computeCriticalityInTo(){
		criticite[Critere.Murissement.getValue()] =	computeCriticalityMurissement();
		criticite[Critere.Isolement.getValue()] = computeCriticalityIsolementTo();
		criticite[Critere.Stabilite_position.getValue()] = computeCriticalityPositionTo();

		return criticite[Critere.Murissement.getValue()]; // TODO
	}*/
	
	@Override
    protected double computeCriticality() {
		double res = 0;
		try {
		if(!isHome)
			res = computeCriticalityInTideal();
		} catch(Exception e)
		{
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
		return(res);
	}

	public boolean isRiped() {
		return isRiped;
	}

	public void setRiped(boolean isRiped) {
		this.isRiped = isRiped;
	}
	
	
	
	
}
