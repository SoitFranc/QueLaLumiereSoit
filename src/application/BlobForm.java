package application;

import java.util.ArrayList;

import business.Blob;
import business.Couleur;
import javafx.scene.Parent;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

// https://openclassrooms.com/courses/les-applications-web-avec-javafx/les-noeuds-graphiques

/**
 * La classe BlobForm correspond à la forme du Blob.<br>
 * Il contient donc les différents globules des différentes couleurs.<br>
 * C'est également lui qui gère le flou appliqué, et le cadre autour du blob
 * sélectionné.
 * 
 * 
 * @author Claire
 *
 */
public class BlobForm extends Parent {
	/** contient le Blob qu'il se doit de représenter */
	private Blob blob;

	Circle fond_blob;
	/**
	 * Liste des Circles représenttant les différents globules du Blob lié
	 */
	ArrayList<Circle> globules;
	/** Rectangle autour du blob, affiché ssi le blob est sélectionné. */
	Rectangle selection = null;
	/**
	 * taille en pixel du coté du carré dans lequel se situe le blob.<br>
	 * Un blob comportant 4 globules au plus d'alignés, un globule est de diamètre
	 * tailleBlob/4
	 */
	private int tailleBlob;
	/** Correspond au flou appliqué à chaque globule */
	BoxBlur boxBlur;// = new BoxBlur(5, 5, 5);

	/**
	 * initialise le flou boxBlur en focntion de la taille définie pour le blob
	 */
	private void generateBoxBlur() {
		if (tailleBlob > 100)
			boxBlur = new BoxBlur(0.15 * tailleBlob, tailleBlob * 0.15, 2);
		else
			boxBlur = new BoxBlur(tailleBlob * 0.2, tailleBlob * 0.2, 2);
	}

	/**
	 * Constructeur. <br>
	 * On lui donne en paramètre le blob à afficher et sa taille.
	 * <p>
	 * On crée donc les globules (des Cercles de couleurs) qu'on ajoute en enfant à
	 * la forme, et dont on garde la référence dans une liste.
	 * </p>
	 * <p>
	 * Le rectangle de sélection est automatiquement créé mais est transparent tant
	 * que le blob n'est pas sélectionné.
	 * </p>
	 * 
	 * @param b
	 *            le blob à afficher
	 * @param coo
	 *            l'endroit où l'afficher dans le terrain
	 * @param tailleBlob
	 *            taille du Blob en pixel
	 */
	public BlobForm(Blob b, double[] coo, int tailleBlob) {
		// blobList = new HashMap<Blob, BlobForm>();
		synchronized (b.lock) {

			this.tailleBlob = tailleBlob;
			globules = new ArrayList<Circle>();
			generateBoxBlur();
			selection = new Rectangle(tailleBlob, tailleBlob);
			selection.setFill(Color.TRANSPARENT);
			selection.setStrokeType(StrokeType.CENTERED);
			selection.setStroke(Color.TRANSPARENT);
			this.getChildren().add(selection);

			this.setTranslateX(coo[0]);// on positionne le groupe
			this.setTranslateY(coo[1]);

			ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			ArrayList<Couleur> couleurGlobule = b.getGlobules_couleurs();
			globules.clear();
			for (int i = 0; i < positionGlobule.size(); i++) {
				Couleur couleur = couleurGlobule.get(i);
				if (couleur == null)
					couleur = Couleur.BLUE;
				fond_blob = new Circle(positionGlobule.get(i)[0], positionGlobule.get(i)[1], tailleBlob / 6,
						couleur.getColor(couleur));
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
				this.getChildren().add(fond_blob);// ajout du rectangle de fond
			}
		}
	}

	/**
	 * Le blob à représenter comporte un attribut pour les positions de ses
	 * globules, mais relatif à un carré de 100*100 pixels.<br>
	 * Ici on veut que le blob soit dans un carré de coté {@link #tailleBlob
	 * tailleBlob} pixels.
	 * <p>
	 * Cette méthode convertit les coordonnées des différents globules pour être
	 * dans un carré de tailleBlob * tailleBlob pixels.
	 * </p>
	 * 
	 * @param globules_position
	 *            position des globules dans un carré 100*100
	 * @return position des globules dans un carré de tailleBlob * tailleBlob
	 */
	private ArrayList<double[]> proportionToVal(ArrayList<double[]> globules_position) {
		ArrayList<double[]> res = new ArrayList<>();
		double[] coo;
		for (int i = 0; i < globules_position.size(); i++) {
			coo = new double[2];
			coo[0] = globules_position.get(i)[0] / 100 * tailleBlob;
			coo[1] = globules_position.get(i)[1] / 100 * tailleBlob;
			res.add(coo);
		}
		return res;
	}

	/**
	 * Constructeur. <br>
	 * On lui donne en paramètre le blob à afficher et sa taille.
	 * <p>
	 * Même fonctionnement que {@link #BlobForm(Blob, double[], int)
	 * BlobForm(blob,coo,tailleBlob)} mais cette fois les couleurs des globules ne
	 * reflètent pas la couleur indiquée dans l'attribut du blob. LEs couleurs
	 * répondranont à celle donnée en paramètre.
	 * </p>
	 * <p>
	 * Ce Constructeur est notamment appelé par ToForm lorsque les blobs ne sont pas
	 * mûrs : les blobs ont déjà une couleur prédéfinie, mais leur affichage doit
	 * néanmoins être blanc.
	 * </p>
	 * 
	 * @param b
	 *            le blob à afficher
	 * @param coo
	 *            l'endroit où l'afficher dans le terrain
	 * @param couleur
	 *            couleur que doit prendre les globules
	 * @param tailleBlob
	 *            taille du Blob en pixel
	 */
	public BlobForm(Blob b, double[] coo, Color couleur, int tailleBlob) {
		this.tailleBlob = tailleBlob;
		globules = new ArrayList<Circle>();
		generateBoxBlur();
		this.setTranslateX(coo[0]);// on positionne le groupe
		this.setTranslateY(coo[1]);

		synchronized (b.lock) {

			ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			globules.clear();
			for (int i = 0; i < positionGlobule.size(); i++) {
				fond_blob = new Circle(positionGlobule.get(i)[0], positionGlobule.get(i)[1], tailleBlob / 6, couleur);
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
				this.getChildren().add(fond_blob);// ajout du rectangle de fond
			}
		}
		selection = new Rectangle(tailleBlob, tailleBlob);
		selection.setFill(Color.TRANSPARENT);
		selection.setStrokeType(StrokeType.CENTERED);
		selection.setStroke(Color.TRANSPARENT);
		this.getChildren().add(selection);
	}

	/**
	 * modifie l'affichage du blob.
	 * 
	 * @param b
	 *            blob à modifier
	 * @param tailleBlob
	 *            la taille du blob en pixel
	 */
	public void changeBlob(Blob b, int tailleBlob) {
		synchronized (b.lock) {
			this.tailleBlob = tailleBlob;
			this.blob = b; // théoriquement, cette instruction n'a pas lieu d'être
			this.setTranslateX(blob.getCoordonnee()[0]);// positionnement du blob
			this.setTranslateY(blob.getCoordonnee()[1]);
			for (int i = 0; i < globules.size(); i++) {
				this.getChildren().remove(globules.get(i));
			}

			ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			ArrayList<Couleur> couleurGlobule = b.getGlobules_couleurs();
			globules.clear();
			for (int i = 0; i < positionGlobule.size(); i++) {
				Couleur couleur = couleurGlobule.get(i);
				if (couleur == null)
					couleur = Couleur.BLUE;
				fond_blob = new Circle(positionGlobule.get(i)[0], positionGlobule.get(i)[1], tailleBlob / 6,
						couleur.getColor(couleur));
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
				this.getChildren().add(fond_blob);// ajout du globule
			}
		}
	}

	/**
	 * modifie l'affichage du blob.
	 * 
	 * @param b
	 *            blob à modifier
	 * @param coo
	 *            les coordonnées du blob
	 * @param tailleBlob
	 *            la taille du blob en pixel
	 */
	public void changeBlob(Blob b, double[] coo, int tailleBlob) {

		synchronized (b.lock) {
			this.tailleBlob = tailleBlob;
			this.blob = b;
			this.setTranslateX(coo[0]);// positionnement du blob
			this.setTranslateY(coo[1]);
			for (int i = 0; i < globules.size(); i++) {
				this.getChildren().remove(globules.get(i));
			}

			ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			ArrayList<Couleur> couleurGlobule = b.getGlobules_couleurs();
			globules.clear();

			for (int i = 0; i < positionGlobule.size(); i++) {
				Couleur couleur = couleurGlobule.get(i);
				if (couleur == null)
					couleur = Couleur.BLUE;
				fond_blob = new Circle(positionGlobule.get(i)[0], positionGlobule.get(i)[1], tailleBlob / 6,
						couleur.getColor(couleur));
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
				this.getChildren().add(fond_blob);// ajout du globule
			}
		}
	}

	// cette fonction est appelï¿½e si le globule n'est pas mï¿½r et doit ï¿½tre
	// repï¿½sentï¿½ blanc.
	// la couleur blanche est donc donnï¿½e en paramï¿½tre.
	/**
	 * modifie la forme du blob passé en paramètre. <br>
	 * Même principe que la méthode {@link #changeBlob(Blob, double[], int)
	 * changeBlob(blob,coo,tailleBlob)} mais tous les globules seront de la couleur
	 * spécifiée en paramètre.
	 * <p>
	 * Cete méthode est appelée par ToForm (pour des changement de positions) pour
	 * un blob non mûr : la couleur blanche est spécifiée.
	 * </p>
	 * 
	 * @param b
	 *            le blob à modifier
	 * @param coo
	 *            les coordonnées du blob
	 * @param couleur
	 *            la couleur à donner à chaque globule
	 * @param tailleBlob
	 *            taille du blob en pixels.
	 */
	public void changeBlob(Blob b, double[] coo, Color couleur, int tailleBlob) {

		synchronized (b.lock) {
			this.tailleBlob = tailleBlob;
			this.blob = b;
			this.setTranslateX(coo[0]);// positionnement du blob
			this.setTranslateY(coo[1]);
			for (int i = 0; i < globules.size(); i++) {
				this.getChildren().remove(globules.get(i));
			}

			ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			globules.clear();
			for (int i = 0; i < positionGlobule.size(); i++) {
				fond_blob = new Circle(positionGlobule.get(i)[0], positionGlobule.get(i)[1], tailleBlob / 6, couleur);
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
				this.getChildren().add(fond_blob);// ajout du globule
			}
		}
	}

	/**
	 * Permet de rendre visible la sélection de ce blob : les bordures du rectangle
	 * qui l'encadre deviennent alors visibles.
	 */
	public void showSelection() {

		assert (selection != null);
		selection.setStroke(Color.ANTIQUEWHITE);
	}

	/**
	 * Permet de désélectionner le blob en rendant transparentes les bordures du
	 * rectangle qui l'encadre.
	 */
	public void deleteSelection() {
		selection.setStroke(Color.TRANSPARENT);
	}

}
