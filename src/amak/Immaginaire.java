package amak;

import application.Controller;
import application.ExceptionHandler;
import business.Blob;
import business.Critere;
import fr.irit.smac.amak.tools.Log;

/**
 * Classe fille de {@link amak.BlobAgent BlobAgent}. Agent li� � un blob
 * virtuel, pr�sent uniquement dans Tidel.
 * <p>
 * Il peut d�cider de Cr�er ; se suicider ; se d�placer ; changer de formes.
 * </p>
 * 
 * @author Claire MEVOLHON
 *
 */
public class Immaginaire extends BlobAgent {

	public Immaginaire(MyAMAS amas, Blob b, Controller controller) {
		super(amas, b, controller);

	}

	@Override
	protected double computeCriticality() {
		return (computeCriticalityInTideal());
	}

	/**
	 * Fonction de mise � jour de l'affichage appel�e � chaque cycle de AMAK : on ne
	 * d�cide d'afficher qu'� une certaine fr�quence. (sauf pour les actions
	 * importantes : cr�ation et suicide)
	 */
	@Override
	protected void onUpdateRender() {
		if (tps + 300 > System.currentTimeMillis()
				&& !(currentAction.equals(Action.CREER) || currentAction.equals(Action.SE_SUICIDER))) {
			return;
		}
		tps = System.currentTimeMillis();
		try {
			switch (currentAction) {
			case SE_DEPLACER:
				synchronized (this) {
					controller.move_blobImmaginaire(this);
				}
				break;
			case CREER:
				synchronized (this) {
					controller.add_blobImmaginaire(newFils);
				}
				break;
			case SE_SUICIDER:
				synchronized (this) {
					controller.remove_blobImmaginaire(this);
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}

	}

	@Override
	protected void action_se_deplacer() {
		double[] tmp = getAmas().getEnvironment().nouvellesCoordonnees(this, Math.random() * 0.7, pastDirection);
		blob.setCoordonnee(tmp);
		currentAction = Action.SE_DEPLACER;

		directGeneral[0] = 0.6 * pastDirection[0] + 0.4 * directGeneral[0];
		directGeneral[1] = 0.6 * pastDirection[1] + 0.4 * directGeneral[1];
	}

	/**
	 * Decide et agit en fonction du blob le plus critique parmi son voisinage,
	 * incluant lui-même. <br>
	 * 
	 * <ul>
	 * <li>En fonction du critère le plus critique :
	 * <li>ISOLEMENT : se suicide si voisinage trop dense, se reproduit sinon.</li>
	 * <li>STABILITE ETAT : non pris en compte ni calculé.</li>
	 * <li>STABILITE POSITION : Si le voisinage est trop statique alors l'agent va
	 * se déplacer.</li>
	 * <li>HETEROGENEITE : l'agent va changer de couleur. Si trop hétérogène, alors
	 * il adoptera la couleur la plus pr�sente parmi le voisinage. Sinon un de ses
	 * globules prendra une couleur autre.</li>
	 * </ul>
	 * 
	 * 
	 * 
	 */
	@Override
	protected void onDecideAndAct() {
		try {
			synchronized (blob.lock) {
				majAspectAgent();
				currentAction = Action.RESTER; // to initialise
				BlobAgent agentNeedingHelp = super.getMoreCriticalAgent();
				Critere most_critic = Most_critical_critere(agentNeedingHelp.getCriticite());

				switch (most_critic) {
				case Isolement:
					// too many neighboors -> criticite.ISOLEMENT<0 -> I have to kill myself
					if (criticite[Critere.Isolement.getValue()] < 0)
						action_se_suicider();
					else
						action_creer();
					break;

				case Stabilite_etat:

					Log.debug("quela", "imag decide stabeta");

					break;

				case Stabilite_position:
					Log.debug("quela", "imag decide stab pos");
					action_se_deplacer();
					break;

				case Heterogeneite:
					Log.debug("quela", "imag decide eheteroge");
					// System.out.println(" \t avec : " +
					// criticite[Critere.Heterogeneite.getValue()]);
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

			Log.debug("quela", "imag decide before super decide and act");
			super.onDecideAndAct();

			Log.debug("quela", "imag decide before catch");
		} catch (Exception e) {
			Log.debug("quela", "imag decide catch");
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
		Log.debug("quela", "end da immag");
	}

	/**
	 * Actions ex�cut�s en fin de cycle de Amak :
	 * <ul>
	 * <li>On met � jour la liste des agents de l'environnement en cas de cr�ation
	 * d'un blob.<br>
	 * La raison est que la cr�ation ou la suppression d'un agent n'est effectif
	 * qu'au cycle suivant. On ne veut donc pas que l'environnement utilise ce
	 * nouvel agent avant le cycle suivant.</li>
	 * </ul>
	 */
	protected void onAgentCycleEnd() {

		if (currentAction.equals(Action.CREER))
			getAmas().getEnvironment().addAgent(newFils);
		super.onAgentCycleEnd();
	}

}
