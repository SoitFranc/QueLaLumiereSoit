package amak;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import application.Controller;
import application.ExceptionHandler;
import business.Blob;
import business.Couleur;
import business.Critere;
//import business.CriticalityFunction;
import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.tools.Log;

enum Action {
	CREER, SE_DEPLACER, SE_SUICIDER, RESTER, CHANGER_COULEUR, CHANGER_FORME, MURIR
};

/**
 * Agent de AMAK. Lequel correspond à un blob
 * <ul>
 * <li>L'agent possède un attribut blob.</li>
 * <li>Calcule des criticités concernant les valeurs données par les curseurs de
 * l'IHM</li>
 * <li>Il suit les cycles de AMAK</li>
 * </ul>
 * <p>
 * Cette classe comporte 2 classes filles :
 * <ul>
 * <li>{@link amak.Immaginaire Immaginaire} : les blobs non réels, n'appartenant
 * que à Ti</li>
 * <li>{@link amak.Migrant Migrant} : les blobs "réels", de To ou Tr</li>
 * </ul>
 * 
 * @author Claire MEVOLHON
 *
 */
public class BlobAgent extends Agent<MyAMAS, MyEnvironment> {

	protected Blob blob;
	/**
	 * liste des voisins relatifs au cycle AMAK en cours. Initialisé par
	 * l'environnement à chaque perception, en fonction du radius de l'IHM
	 */
	protected ArrayList<BlobAgent> voisins;
	protected Action currentAction;

	/**
	 * Attribut utilisé lors de la décision de procréer. Null sinon
	 */
	protected Immaginaire newFils;

	/** Couleur la plus présente dans le voisinage. Utilisé pour l'hétérogénéité */
	protected Couleur couleurEnvironnante;

	/**
	 * Action passive : il s'agit d'un changement de couleur ou de forme dû à
	 * l'environnement et non à la coopération
	 */
	protected Action actionPassive;

	/**
	 * vecteur [x;y] concernant la direction empruntée par le blob lors de son
	 * dernier déplacement. Cet attribut est utilisé pour avoir une direction plus
	 * générale lors du déplacement.
	 */
	protected double[] pastDirection;

	/**
	 * Tableau des différentes criticites. Par convention : negative si en manque,
	 * positive si trop nombreux. Utilise la classe {@link business.Critere Critere}
	 * pour les indices.
	 */
	protected double[] criticite;

	/**
	 * Somme des différentes criticités. Utilisé pour déterminer quel agent
	 * secourir.
	 */
	protected double criticite_globale;
	/**
	 * vecteur [x;y] comportant la direction globale des dernières directions
	 * prises. Ce vecteur est utilisé pour la criticité stabilite_position.
	 */
	protected double[] directGeneral;
	/**
	 * valeur en dessous de laquelle on considerera des valeurs comme nulles. utlisé
	 * par exemple pour le seuil de déplacement en dessous duquel on considère
	 * l'agent comme immobile.
	 */
	protected double epsilon = 0.05;

	/** IHM */
	protected Controller controller;

	// lie aux decisions 'passives' : en fonction de l'etat du voisinage

	/**
	 * Compteur d'expériences coopératives (ici seuelement changement de couleur).
	 * Agit sur le changement de forme passive. Réinitialisé à 0 après chaque
	 * changement de forme.
	 */
	private int nbExperience;

	/** Nombre d'expériences requises avant un changement de forme passive */
	private int nbExperiencesRequises = 7;

	/**
	 * repertorie le temps (en nombre de cycles) passé avec un agent voisin. Agit
	 * sur le changement de couleur passive
	 */
	private HashMap<BlobAgent, Integer> connaissance; // repertorie le temps passe avec un agent

	/**
	 * Durée de connaissance requise entre 2 agents avant un échange de couleur
	 * passif entre eux.
	 */
	private int tpsConnaissanceRequise = 2;

	/**
	 * temps système, utilisé pour la fréquence de mise à jour de l'affichage IHM
	 */
	protected long tps;

	@Override
	protected void onInitialization() {
		this.blob = (Blob) params[0];
		criticite = new double[Critere.FIN.getValue()];
		for (int i = 0; i < Critere.FIN.getValue(); i++)
			criticite[i] = 0;
		controller = (Controller) params[1];
		voisins = new ArrayList<>();
		nbExperience = 0;
		connaissance = new HashMap<>();
		directGeneral = new double[2];
		directGeneral[0] = 0;
		directGeneral[1] = 0;
		actionPassive = Action.SE_DEPLACER;
		currentAction = Action.SE_DEPLACER;
		super.onInitialization();
		tps = System.currentTimeMillis();
	}

	public BlobAgent(MyAMAS amas, Blob b, Controller controller) {
		super(amas, b, controller);
	}

	/**
	 * renvoie la moyenne des positions. Utilisé pour estimer le centre du blob
	 * considérant les positions de ses globules
	 * 
	 * @param maListe
	 *            liste des différentes positions (des globules du blob)
	 * @return la moyenne
	 */
	private double[] calcule_moyenne(ArrayList<double[]> maListe) {
		double[] res = new double[2];
		res[0] = 0;
		res[1] = 0;
		try {
			int i;
			for (i = 0; i < maListe.size(); i++) {
				res[0] += maListe.get(i)[0];
				res[1] += maListe.get(i)[1];
			}
			res[0] = res[0] / i;
			res[1] = res[1] / i;
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
		return res;
	}

	/**
	 * Changement passive de Couleur quand 2 blobs sont voisins pour une durée
	 * suffisemment longue. <br>
	 * 
	 * C'est alors le globule de plus proche du voisin qui va prendre la couleur
	 * prédominante de ce voisin.
	 * 
	 * @param voisin
	 *            voisin avec lequel cet agent va échanger de couleur.
	 */
	protected void changer_de_couleur_passif(BlobAgent voisin) {
		try {
			ArrayList<Couleur> listeMesCouleurs = blob.getGlobules_couleurs();
			double[] centreVoisin = voisin.getBlob().getCoordonnee().clone();
			// probleme la coordonnee du voisin pointe en haut a droite. Il faut la centrer.
			double[] tmp = calcule_moyenne(voisin.getBlob().getGlobules_position());
			centreVoisin[0] += tmp[0];
			centreVoisin[1] += tmp[1];

			// trouvons quel est le globule le plus proche du voisin.
			ArrayList<double[]> listePosGlob = blob.getGlobules_position();
			// les position des globules sont relative a la position du blob.
			// on va donc enlever la position du blob a celle du voisin, pour ne pas
			// calculer la position exacte des globules � chaque fois.
			centreVoisin[0] -= blob.getCoordonnee()[0];
			centreVoisin[1] -= blob.getCoordonnee()[1];
			double distance;
			int indiceMin = 0;
			double distanceMin = Math.sqrt((centreVoisin[0] - listePosGlob.get(0)[0])
					* (centreVoisin[0] - listePosGlob.get(0)[0])
					+ ((centreVoisin[1] - listePosGlob.get(0)[1]) * (centreVoisin[1] - listePosGlob.get(0)[1])));
			for (int i = 1; i < listePosGlob.size(); i++) {
				distance = Math.sqrt((centreVoisin[0] - listePosGlob.get(i)[0])
						* (centreVoisin[0] - listePosGlob.get(i)[0])
						+ ((centreVoisin[1] - listePosGlob.get(i)[1]) * (centreVoisin[1] - listePosGlob.get(i)[1])));
				if (distance < distanceMin) {
					distanceMin = distance;
					indiceMin = i;
				}
			}

			// on modifie la couleur de ce globule en la couleur la plus presente de notre
			// voisin
			listeMesCouleurs.set(indiceMin, voisin.blob.getCouleurLaPLusPresente());
			blob.setGlobules_couleurs(listeMesCouleurs);
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * renvoie le nombre de blobs dans les 4 zones autour de l'agent respectivement
	 * Nord(N) - Est(E) - Sud(S) - Ouest(W)
	 * 
	 * @return [N ; E ; S ; W]
	 */
	private Integer[] determinerPositionVoisins() {
		Integer[] res = new Integer[4];
		// Nous pouvons définir 2 droites (considérons notre blob (x;y) et le voisin
		// (X;Y) ):
		// (d1) separant W-N de S-W : Y = X + (y-x)
		// (d2) separant N-E de W-S : Y = -X + (y+x)
		try {
			for (int i = 0; i < 4; i++)
				res[i] = Integer.valueOf(0);

			double ordonnee1 = blob.getCoordonnee()[1] - blob.getCoordonnee()[0]; // ordonnee1 = (y-x)
			double ordonnee2 = blob.getCoordonnee()[1] + blob.getCoordonnee()[0]; // ordonnee2 = (y+x)

			double[] coo;
			for (BlobAgent voisin : voisins) {
				coo = voisin.getBlob().getCoordonnee();
				if (coo[1] > coo[0] + ordonnee1)
					if (coo[1] > -coo[0] + ordonnee2)
						res[0]++; // appartient à la zone Nord
					else
						res[3]++; // appartient à la zone West
				else if (coo[1] < -coo[0] + ordonnee2)
					res[2]++; // appartient à la zone Sud
				else
					res[1]++; // appartient à la zone Est
			}
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
		return res;
	}

	/**
	 * Changement de forme. Utilise la répartition des voisins autour. Fait appel à
	 * la méthode {@link business.Blob#changeForme(Integer[]) Blob.changeForm}
	 */
	protected void changer_de_forme() {
		try {
			blob.changeForme(determinerPositionVoisins());
			nbExperience = 0;
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * mise à jour de l'aspect du Blob : Méthode qui s'occupe des changements
	 * PASSIFS
	 */
	protected void majAspectAgent() {
		try {
			// La forme s'acquiert a partir d'un nombre d'expérience atteint.
			if (nbExperience >= nbExperiencesRequises) {
				synchronized (getBlob().lock) {
					changer_de_forme();
				}
			}

			// la couleur s'acquiert si un voisin est present depuis un temps defini.
			Iterator<BlobAgent> it = connaissance.keySet().iterator();
			while (it.hasNext()) {
				BlobAgent blobConnu = (BlobAgent) it.next();
				if (connaissance.get(blobConnu) > tpsConnaissanceRequise) {
					synchronized (blobConnu.getBlob().lock) {
						changer_de_couleur_passif(blobConnu);
						actionPassive = Action.CHANGER_COULEUR;
					}
					connaissance.put(blobConnu, 0);
				}
				if (!voisins.contains(blobConnu))
					it.remove();
			}
			// ITERATION
			if (actionPassive.equals(Action.CHANGER_COULEUR) || currentAction.equals(Action.CHANGER_COULEUR)) {
				nbExperience++;
				actionPassive = Action.RESTER;
			}

			// maj des connaissances:

			for (int i = 0; i < voisins.size(); i++) {
				if (connaissance.containsKey(voisins.get(i))) {
					connaissance.put(voisins.get(i), connaissance.get(voisins.get(i)) + 1);
				} else
					connaissance.put(voisins.get(i), 0);
			}
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * Perception de l'agent : on demande à l'environnement de mettre à jour la
	 * liste de voisins.
	 */
	@Override
	protected void onPerceive() {
		try {
			getAmas().getEnvironment().generateNeighbours(this);
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/*
	 * ********************************************************************
	 * **************** ACTION **********************************************
	 * *********************************************************************
	 */

	/**
	 * Action de se suicider, par la méthode destroy de amak.<br>
	 * L'action de détruire l'agent n'est effectif qu'à la fin du cycle (Il n'ya a
	 * donc pas de souci lors d'acces à ce blob par des voisins)<br>
	 * Seuls les {@link amak.Immaginaire Immaginaire} peuvent se suicider, si
	 * voisinage trop dense.
	 */
	protected void action_se_suicider() {
		Log.debug("quela", "imag decide suicide");
		try {
			currentAction = Action.SE_SUICIDER;
			getAmas().getEnvironment().removeAgent(this);
			destroy();
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * action de créer (décidé si trop isolé) Notre blob va créer un blob totalement
	 * semblable à lui-même (même forme, même couleur) et va le positionner
	 * aléatoirement autour de lui à une certaine distance. L'environnement (qui
	 * possède la liste des agents) ne sera mis à jour qu'à la fin du cycle.
	 */
	protected void action_creer() {
		Log.debug("quela", "imag decide creer");
		try {
			currentAction = Action.CREER;
			Log.debug("quela", "imag decide copy");
			Blob newBlob = blob.copy_blob();
			Log.debug("quela", "imag decide setcoord");
			Log.debug("coord", "coord " + newBlob.getCoordonnee());

			newBlob.setCoordonnee(
					(getAmas().getEnvironment().nouvellesCoordonneesTT(this, 2, newBlob.getCoordonnee())));
			Log.debug("quela", "imag decide newfils");
			newFils = new Immaginaire(getAmas(), newBlob, controller);

			// getAmas().getEnvironment().addAgent(newFils);
			// Log.debug("quela", "imag decide addagent");

		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}

	}

	/**
	 * Action de se déplacer (décidé si criticité de la position est trop faible).
	 * L'agent prend une nouvelle position à une certaine distance autour de lui en
	 * favorisant la dernière direction prise. On remet à jour la variable
	 * {@link #directGeneral directGeneral} utilisée pour la criticité de
	 * Déplacement.
	 */
	protected void action_se_deplacer() {
		try {
			double[] tmp = getAmas().getEnvironment().nouvellesCoordonnees(this, Math.random() * 1.2, pastDirection);
			blob.setCoordonnee(tmp);
			currentAction = Action.SE_DEPLACER;

			directGeneral[0] = 0.6 * pastDirection[0] + 0.4 * directGeneral[0];
			directGeneral[1] = 0.6 * pastDirection[1] + 0.4 * directGeneral[1];
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * Changement de couleur ACTIF. (décidé en fonction de la criticité
	 * heterogeneite).
	 * <p>
	 * Afin de ne pas perdre les couleurs acquises par expérience, on choisira pour
	 * notre blob de ne changer que la couleur la plus fréquente parmi ses globules.
	 * </p>
	 * Ici la couleur de remplacement est prise aléatoirement.
	 */
	protected void action_changerCouleur() {
		try {
			// choix d'une nouvelle couleur
			Couleur[] couleurListe = Couleur.values();
			int indiceCouleur = (int) (Math.random() * (couleurListe.length));
			Couleur nvlleCouleur = couleurListe[indiceCouleur];

			Couleur MostPresentCouleur = blob.getCouleurLaPLusPresente();
			ArrayList<Couleur> listeGlobulesCouleur = blob.getGlobules_couleurs();
			for (int i = 0; i < listeGlobulesCouleur.size(); i++) {
				Couleur couleur = listeGlobulesCouleur.get(i);
				if (couleur == null)
					couleur = Couleur.BLUE;
				if (couleur.equals(MostPresentCouleur))
					listeGlobulesCouleur.set(i, nvlleCouleur);
			}
			blob.setGlobules_couleurs(listeGlobulesCouleur);
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/**
	 * Action de changer de couleur en prenant celle la plus presente dans
	 * l'environnement, laquelle est donnee en argument.
	 * <p>
	 * Afin de ne pas perdre les couleurs acquises par expérience, on choisira pour
	 * notre blob de ne changer que la couleur la plus fréquente parmi ses globules.
	 * </p>
	 * 
	 * @param couleur
	 *            couleur de remplacement
	 */
	protected void action_changerCouleur(Couleur couleur) {
		try {

			Couleur MostPresentCouleur = blob.getCouleurLaPLusPresente();
			ArrayList<Couleur> listeGlobulesCouleur = blob.getGlobules_couleurs();
			for (int i = 0; i < listeGlobulesCouleur.size(); i++) {
				System.out.println(listeGlobulesCouleur);
				Couleur couleur2 = listeGlobulesCouleur.get(i);
				if (couleur2 == null)
					couleur2 = Couleur.BLUE;

				if (couleur2.equals(MostPresentCouleur))
					listeGlobulesCouleur.set(i, couleur);
			}
			blob.setGlobules_couleurs(listeGlobulesCouleur);
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
	}

	/*
	 * ************************************************************************ *
	 * ************** CRITICALITY *************************** *
	 * ************************************************************************
	 */

	/**
	 * Calcul de la criticité d'Isolement : par différence entre le nombre de
	 * voisins voulu (curseur sur IHM) et le nombre de voisins effectif.
	 * 
	 * @return criticité concernant l'isolement.
	 */
	protected double computeCriticalityIsolement() {
		double res = 0;
		try {
			res = getAmas().getEnvironment().getIsolement() - voisins.size();
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}

		return (res);
	}

	/**
	 * Calcul de la criticité d'Heterogeneite. Utilise la couleur prédominante de
	 * CHACUN des voisins (notre agent inclus) ainsi que la couleur (C) la plus
	 * présente du voisinage.
	 * <p>
	 * Retourne la différence entre le nombre de voisins effectifs de couleur (C) et
	 * le nombre de voisins de couleur (C) voulus. <br>
	 * Le nombre de voisins de couleur (C) voulu utilise le pourcentage demandé par
	 * le curseur (IHM) par rapport au nombre de voisins.
	 * </p>
	 * 
	 * @return criticité concernant l'hétérogénéité.
	 */
	protected double computeCriticalityHeterogeneite() {

		// recuperation des couleurs environnantes
		HashMap<Couleur, Integer> couleurs = new HashMap<>();
		Couleur couleur;
		for (int i = 0; i < voisins.size(); i++) {
			synchronized (voisins.get(i).getBlob().lock) {
				couleur = voisins.get(i).getBlob().getCouleurLaPLusPresente();
				if (couleurs.containsKey(couleur))
					couleurs.put(couleur, 1 + couleurs.get(couleur));
				else
					couleurs.put(couleur, 1);
			}
		}

		// recuperation de la couleur la plus presente.
		Set<Couleur> couleurSet = couleurs.keySet();
		int maxNbCouleur = 0;
		int tmp;
		for (Couleur clr : couleurSet) {
			if ((tmp = couleurs.get(clr)) > maxNbCouleur) {
				maxNbCouleur = tmp;

				couleurEnvironnante = clr;

				if (couleurEnvironnante == null) {
					System.err.println("couleur environnante null");
					System.exit(-1);
				}
			}
		}

		// calcul de la criticite autour de cette couleur.
		double nbVoisinsOptimal = ((100 - getAmas().getEnvironment().getHeterogeneite()) / 100) * voisins.size();
		return (maxNbCouleur - nbVoisinsOptimal);
	}

	/**
	 * Calcul de la criticité de stabilité de position : <br>
	 * Retourne la différence entre le nombre de voisins mouvants voulu et le nombre
	 * de voisins mouvants effectif.
	 * 
	 * <p>
	 * Utilisation de la variable {@link #directGeneral directGeneral} des
	 * différents voisins et de la variable {@link #epsilon epsilon} pour déterminer
	 * le nombre de voisins mouvants.
	 * </p>
	 * <p>
	 * Le nombre de voisins de mouvants voulu utilise le pourcentage demandé par le
	 * curseur (IHM) par rapport au nombre de voisins.
	 * </p>
	 * 
	 * @return criticité concernant la stabilité de position.
	 */
	protected double computeCriticalityStabilitePosition() {
		// calcul du nombre de voisins qui "bougent".
		// chaque voisin bouge ssi DirectGeneral > (eps,eps)
		double nbBougent = 0;
		for (int i = 0; i < voisins.size(); i++) {
			if (voisins.get(i).getDirectGeneral()[0] + voisins.get(i).getDirectGeneral()[1] > epsilon)
				nbBougent++;
		}

		// nb de voisins optimal qui devraient bouger, selon le curseur.
		double nbOptimal = (getAmas().getEnvironment().getStabilite_position() / 100) * voisins.size();

		if (nbBougent < nbOptimal)
			return (nbOptimal - nbBougent);

		// le problème, si trop de blobs bougent autour, je ne veux pas lever la
		// criticité, afin d'espérer agir pour une autre criticité.
		return (0);
	}

	/**
	 * Calcule les différentes criticités dans Tideal.
	 * 
	 * @return la criticité globale.
	 */
	protected double computeCriticalityInTideal() {

		try {
			criticite[Critere.Isolement.getValue()] = computeCriticalityIsolement();
			criticite[Critere.Heterogeneite.getValue()] = computeCriticalityHeterogeneite();
			criticite[Critere.Stabilite_etat.getValue()] = 0;
			criticite[Critere.Stabilite_position.getValue()] = computeCriticalityStabilitePosition();

			criticite_globale = criticite[Critere.Heterogeneite.getValue()] + criticite[Critere.Isolement.getValue()]
					+ criticite[Critere.Stabilite_etat.getValue()] + criticite[Critere.Stabilite_position.getValue()];
		} catch (Exception e) {
			ExceptionHandler eh = new ExceptionHandler();
			eh.showError(e);
		}
		return criticite_globale;
	}

	/**
	 * retourne le critere qui a une plus grande criticite
	 * 
	 * @param subjectCriticity
	 *            tableau de criticités
	 * @return le critère le plus critique
	 */
	public Critere Most_critical_critere(double[] subjectCriticity) {
		// return (Collections.max(criticite.entrySet(),
		// Map.Entry.comparingByValue()).getKey());
		double max_valeur = subjectCriticity[0];
		int max_critere = 0;
		for (int i = 0; i < subjectCriticity.length; i++)
			if (Math.abs(max_valeur) < Math.abs(subjectCriticity[i])) {
				max_valeur = subjectCriticity[i];
				max_critere = i;
			}
		return Critere.valueOf(max_critere);
	}

	/**
	 * renvoie l'agent le plus critique parmi ses voisins, incluant lui-meme
	 * 
	 * @return l'agent le plus critique
	 */
	protected BlobAgent getMoreCriticalAgent() {
		Iterator<BlobAgent> itr = voisins.iterator();
		double criticiteMax = criticite_globale;
		BlobAgent res = this;
		while (itr.hasNext()) {
			BlobAgent blobagent = itr.next();
			if (blobagent.criticite_globale > criticiteMax) {
				criticiteMax = blobagent.criticite_globale;
				res = blobagent;
			}
		}
		return (res);
	}

	/*
	 * ****************************************** **************************
	 * *********** GETTER / SETTER **************************
	 * ***********************************************************************
	 */

	public Blob getBlob() {
		return blob;
	}

	public void setBlob(Blob blob) {
		this.blob = blob;
	}

	public double[] getCriticite() {
		return criticite;
	}

	public ArrayList<BlobAgent> getVoisins() {
		return voisins;
	}

	public void setVoisins(ArrayList<BlobAgent> voisins) {
		this.voisins = voisins;

	}

	public double getCriticite_globale() {
		return criticite_globale;
	}

	public void setCriticite_globale(int criticite_globale) {
		this.criticite_globale = criticite_globale;
	}

	public double[] getPastDirection() {
		return pastDirection;
	}

	public void setPastDirection(double[] pastDirection) {
		this.pastDirection = pastDirection;
	}

	public double[] getDirectGeneral() {
		return directGeneral;
	}

	public void setDirectGeneral(double[] directGeneral) {
		this.directGeneral = directGeneral;
	}

	public void addVoisin(BlobAgent blobToAdd) {
		this.voisins.add(blobToAdd);
	}

	public void clearVoisin() {
		this.voisins.clear();
	}

}
