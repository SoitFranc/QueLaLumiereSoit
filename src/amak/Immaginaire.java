package amak;

import application.Controller;
import application.ExceptionHandler;
import business.Blob;
import business.Critere;

public class Immaginaire extends BlobAgent{

	public Immaginaire(MyAMAS amas, Blob b, Controller controller) {
		super(amas, b, controller);
		
	}
	
	@Override
    protected double computeCriticality() {
		return(computeCriticalityInTideal());
	}
	
	@Override
    protected void onUpdateRender() {
    	try {
			switch(currentAction){
	    	case SE_DEPLACER :
	    		synchronized (this)
	            {
	        		controller.move_blobImmaginaire(this);
	            }
	    		break;
	    	case CREER :
	    		synchronized (this)
	            {
	    			controller.add_blobImmaginaire(newFils);
	            }
	    		break;
	    	case SE_SUICIDER :
	    		synchronized (this)
	            {
	    			controller.remove_blobImmaginaire(this);
	            }
				break;
			default:
				break;
	    	}
    	} catch(Exception e)
		{
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}

    }
	
	@Override
	protected void action_se_deplacer(){
		double[] tmp = getAmas().getEnvironment().nouvellesCoordonnees(this, Math.random() * 0.7, pastDirection);
		blob.setCoordonnee(tmp);
		currentAction = Action.SE_DEPLACER;
		
		directGeneral[0] = 0.6 * pastDirection[0] + 0.4 * directGeneral[0];
		directGeneral[1] = 0.6 * pastDirection[1] + 0.4 * directGeneral[1];
	}
	
	
	@Override
	protected void onDecideAndAct() {
		try {
			synchronized(blob.lock){ 
				nbChangements = 0;
				 majAspectAgent();		 
				 currentAction = Action.RESTER; // to initialise
			     BlobAgent agentNeedingHelp = super.getMoreCriticalAgent();
				 Critere most_critic = Most_critical_critere(agentNeedingHelp.getCriticite());
				 //System.out.println("criticite de stabilité de position = " + criticite[Critere.Stabilite_position.getValue()]);
				 //System.out.println("le plus critique est : " + most_critic.toString());
				 
				 switch (most_critic){
				 case Isolement:
					 // too many neighboors -> criticite.ISOLEMENT<0 -> I have to kill myself
					 if(criticite[Critere.Isolement.getValue()] < 0)
						 action_se_suicider();
					 else
						 action_creer();
					 break;
						 
				 case Stabilite_etat:
					 
					 
					 
					 break;
					 
				 case Stabilite_position:
					 action_se_deplacer();
					 break;
					 
				 case Heterogeneite:
					 //System.out.println(" \t avec : " + criticite[Critere.Heterogeneite.getValue()]);
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
	
	
	

}
