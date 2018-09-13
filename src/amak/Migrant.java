package amak;

import application.Controller;
import application.ExceptionHandler;
import business.Blob;
import business.Critere;

/**
 * Classe fille de {@link amak.BlobAgent BlobAgent}. <br>
 * Agent lié à un blob "réel" : soit dans To, soit dans Tr.
 * <p>
 * Ses actions dépendent s'il se situe dans To ou Tr :<br>
 * <ul>
 * <li>Dans To : Il peut mûrir ou se déplacer.</li>
 * <li>Dans Tr (lié à un spectateur): Il peut créer ou changer de forme. Il ne
 * peut ni se suicider, ni se déplacer.
 * </ul>
 * 
 * 
 * @author Claire MEVOLHON
 *
 */
public class Migrant extends BlobAgent {

	/** true si l'agent est dans To. False si dans Treel */
	private boolean isHome;
	/** true si l'agent est mûr, ie prêt à partir de To */
	private boolean isRiped;
	/** compteur : nombre de cycles depuis lequel l'agent est mûr. */
	private int cptRiped;
	/** taux de mûrissement dans To (en pourcentage) */
	private double tauxMurissement = 5;
	/** nouvelles coordonnées du blob qui seront à affecter en fin de cycle */
	private double[] cooFutur;
	/** true si de nouvelles coordonnées sont à affecter en fin de cycle */
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

	/**
	 * boolean renvoyant true avec une probabilité de 'tauxMurissement'
	 * 
	 * @return vrai s'il doit mûrir
	 */
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

				// Si je suis sans TR/TI ne peux pas me mouvoir. Je ne peux donc pas gérer la
				// criticité de position
				// Je vais aider le plus critique sur une autre de ses criticités.
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
	 * Passage de l'agent de To à Tr. Ici l'agent prendra une coordonnée aléatoire
	 * possible dans Tr.
	 * <p>
	 * ATTENTION : À changer plus tard si procréation possible dans To. <br>
	 * (Ici, comme il n'y a pas de création de blob dans To, il n'y a pas de risque
	 * lié au changement de coordonnées. Tout est fait immédiatement.)
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
	 * Passage de l'agent de To à Tr. Ici l'agent se positionnera à la coordonnée
	 * donnée en paramètre.
	 * <p>
	 * ATTENTION : À changer plus tard si procréation possible dans To. <br>
	 * (Ici, comme il n'y a pas de création de blob dans To, il n'y a pas de risque
	 * lié au changement de coordonnées. Tout est fait immédiatement.)
	 * </p>
	 * 
	 * @param coo
	 *            coordonnées à prendre dans Tr.
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
	 * Actions exécutés en fin de cycle de Amak :
	 * <ul>
	 * <li>On met à jour la liste des agents de l'environnement en cas de création
	 * d'un blob.<br>
	 * La raison est que la création ou la suppression d'un agent n'est effectif
	 * qu'au cycle suivant. On ne veut donc pas que l'environnement utilise ce
	 * nouvel agent avant le cycle suivant.</li>
	 *
	 * <li>"isGoingToMove" quand un agent passe de Tr à To : on met à jour ses
	 * nouvelles coordonnées et l'environnement. (Ainsi, si notre agent était au
	 * milieu de son cycle, ses actions s'effectueront avec les attributs qu'il
	 * avait lors de sa décision.)</li>
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
	 * Passage de l'agent de Tr à To. L'agent prendra une coordonnée aléatoire
	 * possible dans To.
	 * <p>
	 * Cette méthode est appelée depuis un thread exterieur à AMAS. Elle peut donc
	 * s'exécuter en plein cycle de l'agent. Pour éviter certains problèmes (comme
	 * créer un fils avec des coordonnées hors-map) les changements de coordonées
	 * s'effectueront la fin du cycle dans la methode {@link #onAgentCycleEnd()
	 * onAgentCycleEnd}
	 * </p>
	 */
	public void tr_to_t0() {
		try {

			cooFutur = blob.genererCoordonneeAleaDansCercle(100);
			isGoingToMove = true;
			// j'attends la fin du cycle pour me mettre à jour
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
	 * Calcule de la criticité. Laquelle dépend si l'agent se situe actuellement en
	 * To ou Tr. <br>
	 * La criticité dans Tr se calcule de la même façon qu'un agent immaginaire de
	 * Ti (seules les décisions diffèrent). <br>
	 * Pour le moment, il n'y a pas d'action coopératives dans To. Donc pas de
	 * calcul de criticité.
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
