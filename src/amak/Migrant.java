package amak;

import application.Controller;
import application.ExceptionHandler;
import business.Blob;
import business.Critere;

/**
 * Classe fille de {@link amak.BlobAgent BlobAgent}. <br>
 * Agent li� � un blob "r�el" : soit dans To, soit dans Tr.
 * <p>
 * Ses actions d�pendent s'il se situe dans To ou Tr :<br>
 * <ul>
 * <li>Dans To : Il peut m�rir ou se d�placer.</li>
 * <li>Dans Tr (li� � un spectateur): Il peut cr�er ou changer de forme. Il ne
 * peut ni se suicider, ni se d�placer.
 * </ul>
 * 
 * 
 * @author Claire MEVOLHON
 *
 */
public class Migrant extends BlobAgent {

	/** true si l'agent est dans To. False si dans Treel */
	private boolean isHome;
	/** true si l'agent est m�r, ie pr�t � partir de To */
	private boolean isRiped;
	/** compteur : nombre de cycles depuis lequel l'agent est m�r. */
	private int cptRiped;
	/** taux de m�rissement dans To (en pourcentage) */
	private double tauxMurissement = 5;
	/** nouvelles coordonn�es du blob qui seront � affecter en fin de cycle */
	private double[] cooFutur;
	/** true si de nouvelles coordonn�es sont � affecter en fin de cycle */
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

	/** boolean renvoyant true avec une probabilit� de 'tauxMurissement' */
	private boolean mustRipe() {
		return (Math.random() * 100 < tauxMurissement);
	}

	/** action de murir */
	private void action_murir() {
		isRiped = true;
		cptRiped = 20;
	}

	@Override
	protected void onDecideAndAct() {
		try {
			synchronized (blob.lock) {
				currentAction = Action.RESTER; // to initialise
				if (isHome) {
					if (isRiped) {
						if (cptRiped <= 0)
							isRiped = false;
						else
							cptRiped--;
					} else if (mustRipe())
						action_murir(); // isRiped = true;

					action_se_deplacer();
				} else
					majAspectAgent();
				BlobAgent agentNeedingHelp = super.getMoreCriticalAgent();
				Critere most_critic = Most_critical_critere(agentNeedingHelp.getCriticite());

				// Si je suis sans TR/TI ne peux pas me mouvoir. Je ne peux donc pas g�rer la
				// criticit� de position
				// Je vais aider le plus critique sur une autre de ses criticit�s.
				if (!isHome && most_critic == Critere.Stabilite_position) {
					double[] tmp = agentNeedingHelp.getCriticite();
					tmp[Critere.Heterogeneite.getValue()] = 0;
					most_critic = Most_critical_critere(tmp);
				}

				switch (most_critic) {
				case Isolement:
					// too few neighboors -> criticite.ISOLEMENT > 0 -> I have procreate
					if (criticite[Critere.Isolement.getValue()] > 0)
						action_creer();
					break;

				case Stabilite_position:
					break;

				case Heterogeneite:
					// if >0 then it's too homogeneous. --> I change the color in a random one.
					// else it's too heterogeneous. -> I change my color to the most present color
					if (criticite[Critere.Heterogeneite.getValue()] > 0)
						action_changerCouleur();
					else
						action_changerCouleur(couleurEnvironnante);

					break;

				default:
					break;
				}
			}
			super.onDecideAndAct();
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	@Override
	protected void onUpdateRender() {

		if (tps + 300 > System.currentTimeMillis()
				&& !(currentAction.equals(Action.CREER) || currentAction.equals(Action.MURIR))) {
			return;
		}
		tps = System.currentTimeMillis();
		try {
			switch (currentAction) {
			case CREER:
				synchronized (this) {
					super.controller.add_blobImmaginaire(newFils);
				}
				break;

			default:
				if (isHome)
					synchronized (this) {
						super.controller.move_blobHibernant(this);
					}
				else
					synchronized (this) {
						super.controller.move_blobMigrant(this);
					}
				break;
			}
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * Passage de l'agent de To � Tr. Ici l'agent prendra une coordonn�e al�atoire
	 * possible dans Tr.
	 * <p>
	 * ATTENTION : � changer plus tard si procr�ation possible dans To. <br>
	 * (Ici, comme il n'y a pas de cr�ation de blob dans To, il n'y a pas de risque
	 * li� au changement de coordonn�es. Tout est fait imm�diatement.)
	 * </p>
	 */
	public void t0_to_tr() {
		try {
			isHome = false;
			blob.setCoordonnee(blob.genererCoordonneeAleaDansCercle(getAmas().getEnvironment().rayonTerrain * 2));
			getAmas().getEnvironment().t0_to_tr(this);
			controller.add_blobMigrant(this);
			controller.remove_blobHibernant(this);
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * Passage de l'agent de To � Tr. Ici l'agent se positionnera � la coordonn�e
	 * donn�e en param�tre.
	 * <p>
	 * ATTENTION : � changer plus tard si procr�ation possible dans To. <br>
	 * (Ici, comme il n'y a pas de cr�ation de blob dans To, il n'y a pas de risque
	 * li� au changement de coordonn�es. Tout est fait imm�diatement.)
	 * </p>
	 * 
	 * @param coo
	 *            coordonn�es � prendre dans Tr.
	 */
	public void t0_to_tr(double[] coo) {
		try {
			isHome = false;
			blob.setCoordonnee(coo);
			getAmas().getEnvironment().t0_to_tr(this);
			controller.add_blobMigrant(this);
			controller.remove_blobHibernant(this);
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * Actions ex�cut�s en fin de cycle de Amak :
	 * <ul>
	 * <li>On met � jour la liste des agents de l'environnement en cas de cr�ation
	 * d'un blob.<br>
	 * La raison est que la cr�ation ou la suppression d'un agent n'est effectif
	 * qu'au cycle suivant. On ne veut donc pas que l'environnement utilise ce
	 * nouvel agent avant le cycle suivant.</li>
	 *
	 * <li>"isGoingToMove" quand un agent passe de Tr � To : on met � jour ses
	 * nouvelles coordonn�es et l'environnement. (Ainsi, si notre agent �tait au
	 * milieu de son cycle, ses actions s'effectueront avec les attributs qu'il
	 * avait lors de sa d�cision.)</li>
	 * </ul>
	 */
	@Override
	protected void onAgentCycleEnd() {

		if (isGoingToMove) {
			isHome = true;
			isGoingToMove = false;
			getAmas().getEnvironment().tr_to_t0(this);
			blob.setCoordonnee(cooFutur);
			controller.add_blobHibernant(this);
			controller.remove_blobMigrant(this);
		}
		if (currentAction.equals(Action.CREER))
			getAmas().getEnvironment().addAgent(newFils);

		super.onAgentCycleEnd();
	}

	/**
	 * Passage de l'agent de Tr � To. L'agent prendra une coordonn�e al�atoire
	 * possible dans To.
	 * <p>
	 * Cette m�thode est appel�e depuis un thread exterieur � AMAS. Elle peut donc
	 * s'ex�cuter en plein cycle de l'agent. Pour �viter certains probl�mes (comme
	 * cr�er un fils avec des coordonn�es hors-map) les changements de coordon�es
	 * s'effectueront la fin du cycle dans la methode {@link #onAgentCycleEnd()
	 * onAgentCycleEnd}
	 * </p>
	 */
	public void tr_to_t0() {
		try {

			cooFutur = blob.genererCoordonneeAleaDansCercle(100);
			isGoingToMove = true;
			// j'attends la fin du cycle pour me mettre � jour
			// blob.setCoordonnee(blob.genererCoordonneeAleaDansCercle(100));
			/*
			 * getAmas().getEnvironment().tr_to_t0(this);
			 * controller.add_blobHibernant(this); controller.remove_blobMigrant(this);
			 */
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * Calcule de la criticit�. Laquelle d�pend si l'agent se situe actuellement en
	 * To ou Tr. <br>
	 * La criticit� dans Tr se calcule de la m�me fa�on qu'un agent immaginaire de
	 * Ti (seules les d�cisions diff�rent). <br>
	 * Pour le moment, il n'y a pas d'action coop�ratives dans To. Donc pas de
	 * calcul de criticit�.
	 */
	@Override
	protected double computeCriticality() {
		double res = 0;
		try {
			if (!isHome)
				res = computeCriticalityInTideal();
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
		return (res);
	}

	public boolean isRiped() {
		return isRiped;
	}

	public void setRiped(boolean isRiped) {
		this.isRiped = isRiped;
	}

}
