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
 * La classe BlobForm correspond � la forme du Blob.<br>
 * Il contient donc les diff�rents globules des diff�rentes couleurs.<br>
 * C'est �galement lui qui g�re le flou appliqu�, et le cadre autour du blob
 * s�lectionn�.
 * 
 * 
 * @author Claire
 *
 */
public class BlobForm extends Parent {
	/** contient le Blob qu'il se doit de repr�senter */
	private Blob blob;

	Circle fond_blob;
	/**
	 * Liste des Circles repr�senttant les diff�rents globules du Blob li�
	 */
	ArrayList<Circle> globules;
	/** Rectangle autour du blob, affich� ssi le blob est s�lectionn�. */
	Rectangle selection = null;
	/**
	 * taille en pixel du cot� du carr� dans lequel se situe le blob.<br>
	 * Un blob comportant 4 globules au plus d'align�s, un globule est de diam�tre
	 * tailleBlob/4
	 */
	private int tailleBlob;
	/** Correspond au flou appliqu� � chaque globule */
	BoxBlur boxBlur;// = new BoxBlur(5, 5, 5);

	/**
	 * initialise le flou boxBlur en focntion de la taille d�finie pour le blob
	 */
	private void generateBoxBlur() {
		if (tailleBlob > 100)
			boxBlur = new BoxBlur(0.15 * tailleBlob, tailleBlob * 0.15, 2);
		else
			boxBlur = new BoxBlur(tailleBlob * 0.2, tailleBlob * 0.2, 2);
	}

	/**
	 * Constructeur. <br>
	 * On lui donne en param�tre le blob � afficher et sa taille.
	 * <p>
	 * On cr�e donc les globules (des Cercles de couleurs) qu'on ajoute en enfant �
	 * la forme, et dont on garde la r�f�rence dans une liste.
	 * </p>
	 * <p>
	 * Le rectangle de s�lection est automatiquement cr�� mais est transparent tant
	 * que le blob n'est pas s�lectionn�.
	 * </p>
	 * 
	 * @param b
	 *            le blob � afficher
	 * @param coo
	 *            l'endroit o� l'afficher dans le terrain
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
	 * Le blob � repr�senter comporte un attribut pour les positions de ses
	 * globules, mais relatif � un carr� de 100*100 pixels.<br>
	 * Ici on veut que le blob soit dans un carr� de cot� {@link #tailleBlob
	 * tailleBlob} pixels.
	 * <p>
	 * Cette m�thode convertit les coordonn�es des diff�rents globules pour �tre
	 * dans un carr� de tailleBlob * tailleBlob pixels.
	 * </p>
	 * 
	 * @param globules_position
	 *            position des globules dans un carr� 100*100
	 * @return position des globules dans un carr� de tailleBlob * tailleBlob
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
	 * On lui donne en param�tre le blob � afficher et sa taille.
	 * <p>
	 * M�me fonctionnement que {@link #BlobForm(Blob, double[], int)
	 * BlobForm(blob,coo,tailleBlob)} mais cette fois les couleurs des globules ne
	 * refl�tent pas la couleur indiqu�e dans l'attribut du blob. LEs couleurs
	 * r�pondranont � celle donn�e en param�tre.
	 * </p>
	 * <p>
	 * Ce Constructeur est notamment appel� par ToForm lorsque les blobs ne sont pas
	 * m�rs : les blobs ont d�j� une couleur pr�d�finie, mais leur affichage doit
	 * n�anmoins �tre blanc.
	 * </p>
	 * 
	 * @param b
	 *            le blob � afficher
	 * @param coo
	 *            l'endroit o� l'afficher dans le terrain
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
	 *            blob � modifier
	 * @param tailleBlob
	 *            la taille du blob en pixel
	 */
	public void changeBlob(Blob b, int tailleBlob) {
		synchronized (b.lock) {
			this.tailleBlob = tailleBlob;
			this.blob = b; // th�oriquement, cette instruction n'a pas lieu d'�tre
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
	 *            blob � modifier
	 * @param coo
	 *            les coordonn�es du blob
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

	// cette fonction est appel�e si le globule n'est pas m�r et doit �tre
	// rep�sent� blanc.
	// la couleur blanche est donc donn�e en param�tre.
	/**
	 * modifie la forme du blob pass� en param�tre. <br>
	 * M�me principe que la m�thode {@link #changeBlob(Blob, double[], int)
	 * changeBlob(blob,coo,tailleBlob)} mais tous les globules seront de la couleur
	 * sp�cifi�e en param�tre.
	 * <p>
	 * Cete m�thode est appel�e par ToForm (pour des changement de positions) pour
	 * un blob non m�r : la couleur blanche est sp�cifi�e.
	 * </p>
	 * 
	 * @param b
	 *            le blob � modifier
	 * @param coo
	 *            les coordonn�es du blob
	 * @param couleur
	 *            la couleur � donner � chaque globule
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
	 * Permet de rendre visible la s�lection de ce blob : les bordures du rectangle
	 * qui l'encadre deviennent alors visibles.
	 */
	public void showSelection() {

		assert (selection != null);
		selection.setStroke(Color.ANTIQUEWHITE);
	}

	/**
	 * Permet de d�s�lectionner le blob en rendant transparentes les bordures du
	 * rectangle qui l'encadre.
	 */
	public void deleteSelection() {
		selection.setStroke(Color.TRANSPARENT);
	}

}
