package application;

import java.util.HashMap;
import java.util.Map;

import business.Blob;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;


// l'ensemble des coordonn�es des blobs seront donn�es en poucentage pour les absisses et ordonn�es.
public class ToForm extends Parent{
	private Map<Blob, BlobForm> blobList;
	private double dimRepresentation = 350;	// rayon/cot� de la repr�sentation en pxl (il s'agit d'une sph�re)
	private double tailleBlob = 12;

	public ToForm() {
		blobList = new HashMap<Blob, BlobForm>();

	//	Circle fond_Terrain = new Circle (dimRepresentation, dimRepresentation, dimRepresentation);

		Rectangle fond_Terrain = new Rectangle ();
		fond_Terrain.setWidth(dimRepresentation);
		fond_Terrain.setHeight(dimRepresentation);
		fond_Terrain.setArcWidth(10);
		fond_Terrain.setArcHeight(10);
		fond_Terrain.setFill(Color.BLACK);

		this.setTranslateX(0);// on positionne le groupe plut�t que le rectangle
		this.setTranslateY(0);

		this.getChildren().add(fond_Terrain);// on ajoute le rectangle au groupe

	}
	
	// param : the coordinates on percent. Returne the coordinates(pxl) in the shape.
	private double[] percentToRepresentation(double[] coo){
		double[] res = new double[2];
		res[0] = coo[0]/100 * (dimRepresentation - tailleBlob);
		res[1] = coo[1]/100 * (dimRepresentation - tailleBlob);
		return res;
	}
	
	

	public void add_blob(Blob b) {

		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf = new BlobForm(b, percentToRepresentation(b.getCoordonnee()));
				blobList.put(b, bf);
				getChildren().add(bf);
				
			}
		});

	}

	public void remove_blob(Blob b) {
		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf = blobList.get(b);
				getChildren().remove(bf);
				blobList.remove(b);
			}
		});

	}

	public void move_blob(Blob b) {
		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf = blobList.get(b);
				bf.changeBlob(b, percentToRepresentation(b.getCoordonnee()));
			}
		});
	}
	
	
	
	
}