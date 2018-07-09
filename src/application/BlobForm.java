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


public class BlobForm extends Parent{
	private Blob blob;
	Circle fond_blob;
	ArrayList<Circle> globules;
	Rectangle selection = null;
	private int tailleBlob;
	BoxBlur boxBlur;// = new BoxBlur(5, 5, 5); 
	
	
	private void generateBoxBlur(){
		if (tailleBlob > 100)
			boxBlur = new BoxBlur(0.15 * tailleBlob, tailleBlob * 0.15, tailleBlob/200);
		else
			boxBlur = new BoxBlur(tailleBlob * 0.2, tailleBlob *0.2, tailleBlob/2);
	}
	
	
	public BlobForm(Blob b, double[] coo, int tailleBlob){
		//blobList = new HashMap<Blob, BlobForm>();
		synchronized(b.lock) {
		
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
			for(int i = 0 ; i < positionGlobule.size(); i++)
			{
				Couleur couleur = couleurGlobule.get(i);
				if (couleur == null)
					couleur = Couleur.BLUE;
				fond_blob = new Circle(positionGlobule.get(i)[0] ,positionGlobule.get(i)[1] ,tailleBlob/6, couleur.getColor(couleur) ); 
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
		        this.getChildren().add(fond_blob);//ajout du rectangle de fond			
			}
		}
    }
	
	private ArrayList<double[]> proportionToVal(ArrayList<double[]> globules_position) {
		ArrayList<double[]> res = new ArrayList<>();
		double[] coo;
		for (int i = 0; i < globules_position.size(); i++)
		{
			coo = new double[2];
			coo[0] = globules_position.get(i)[0]/100 * tailleBlob;
			coo[1] = globules_position.get(i)[1] / 100 * tailleBlob;
			res.add(coo);
		}
		return res;
	}

	public BlobForm(Blob b, double[] coo, Color couleur, int tailleBlob){
		this.tailleBlob = tailleBlob;
		globules = new ArrayList<Circle>();
		generateBoxBlur();
		this.setTranslateX(coo[0]);// on positionne le groupe 
		this.setTranslateY(coo[1]);
		
		synchronized(b.lock)
		{
		
			ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			globules.clear();
			for(int i = 0 ; i < positionGlobule.size(); i++)
			{
				fond_blob = new Circle(positionGlobule.get(i)[0] ,positionGlobule.get(i)[1] ,tailleBlob/6, couleur ); 
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
		        this.getChildren().add(fond_blob);//ajout du rectangle de fond			
			}
		}
		selection = new Rectangle(tailleBlob, tailleBlob);
		selection.setFill(Color.TRANSPARENT);
		selection.setStrokeType(StrokeType.CENTERED);
		selection.setStroke(Color.TRANSPARENT);
		this.getChildren().add(selection);
    }
	
	
	public void changeBlob(Blob b, int tailleBlob){
		synchronized(b.lock)
		{
			this.tailleBlob = tailleBlob;
			this.blob = b;
			this.setTranslateX(blob.getCoordonnee()[0]);//positionnement du blob
	        this.setTranslateY(blob.getCoordonnee()[1]);
			for(int i = 0 ; i < globules.size(); i++){
				this.getChildren().remove(globules.get(i));
			}
	        
	        ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			ArrayList<Couleur> couleurGlobule = b.getGlobules_couleurs();
			globules.clear();
			for(int i = 0 ; i < positionGlobule.size(); i++)
			{
				Couleur couleur = couleurGlobule.get(i);
				if (couleur == null)
					couleur = Couleur.BLUE;
				fond_blob = new Circle(positionGlobule.get(i)[0] ,positionGlobule.get(i)[1] ,tailleBlob/6, couleur.getColor(couleur) ); 
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
		        this.getChildren().add(fond_blob);//ajout du globule	
			}
		}
	}
	
	
	public void changeBlob(Blob b, double[] coo, int tailleBlob){
		
		synchronized(b.lock)
		{
			this.tailleBlob = tailleBlob;
			this.blob = b;
			this.setTranslateX(coo[0]);//positionnement du blob
	        this.setTranslateY(coo[1]);
			for(int i = 0 ; i < globules.size(); i++){
				this.getChildren().remove(globules.get(i));
			}
	        
	        ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			ArrayList<Couleur> couleurGlobule = b.getGlobules_couleurs();
			globules.clear();
					
			for(int i = 0 ; i < positionGlobule.size(); i++)
			{
				Couleur couleur = couleurGlobule.get(i);
				if (couleur == null)
					couleur = Couleur.BLUE;
				fond_blob = new Circle(positionGlobule.get(i)[0] ,positionGlobule.get(i)[1] ,tailleBlob/6, couleur.getColor(couleur) ); 
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
		        this.getChildren().add(fond_blob);//ajout du globule	
			}
		}
	}
	
	// cette fonction est appel�e si le globule n'est pas m�r et doit �tre rep�sent� blanc.
	// la couleur blanche est donc donn�e en param�tre.
	public void changeBlob(Blob b, double[] coo, Color couleur, int tailleBlob){
		
		synchronized(b.lock)
		{
			this.tailleBlob = tailleBlob;
			this.blob = b;
			this.setTranslateX(coo[0]);//positionnement du blob
	        this.setTranslateY(coo[1]);
			for(int i = 0 ; i < globules.size(); i++){
				this.getChildren().remove(globules.get(i));
			}
	        
	        ArrayList<double[]> positionGlobule = proportionToVal(b.getGlobules_position());
			globules.clear();
			for(int i = 0 ; i < positionGlobule.size(); i++)
			{
				fond_blob = new Circle(positionGlobule.get(i)[0] ,positionGlobule.get(i)[1] ,tailleBlob/6, couleur ); 
				fond_blob.setEffect(boxBlur);
				globules.add(fond_blob);
		        this.getChildren().add(fond_blob);//ajout du globule	
			}
		}
	}
	
	
	public void showSelection(){
		
		assert(selection != null);
		//selection.setStrokeType(StrokeType.CENTERED);
		selection.setStroke(Color.ANTIQUEWHITE);
		
		//selection.setFill(Color.WHITE);
	}
	
	public void deleteSelection(){
		selection.setStroke(Color.TRANSPARENT);
		//selection.setFill(Color.TRANSPARENT);
	}
	
	
	
	
}
