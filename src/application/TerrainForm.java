package application;

import java.util.HashMap;
import java.util.Map;

import business.Blob;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
//import javafx.scene.shape.Rectangle;

public class TerrainForm extends Parent {

	// ArrayList<Blob> blobList;
	// HashMap<Blob, BlobForm> blobList;
	private Map<Blob, BlobForm> blobList;
	private double rayonSalle = 12.5; // rayon de la salle d'exposition en m
	private double rayonRepresentation = 175;	// rayon de la représentation en pxl
	private int tailleBlob = 16; 
	
	
	
	
	public TerrainForm() {
		// blobList = new ArrayList<Blob>();
		blobList = new HashMap<Blob, BlobForm>();

		Circle fond_Terrain = new Circle(rayonRepresentation, rayonRepresentation, rayonRepresentation);
		
		// si rectangulaire :
		/*fond_Terrain.setWidth(400);
		fond_Terrain.setHeight(300);
		fond_Terrain.setArcWidth(30);
		fond_Terrain.setArcHeight(30);*/
		fond_Terrain.setFill(Color.BLACK);

		this.setTranslateX(0);// on positionne le groupe plutôt que le rectangle
		this.setTranslateY(0);

		this.getChildren().add(fond_Terrain);// on ajoute le rectangle au groupe

	}
	
	public TerrainForm(int rayonRepresentation) {
		this.rayonRepresentation = (double) rayonRepresentation;
		tailleBlob = tailleBlob * rayonRepresentation / 175 ;
		//TerrainForm();
		blobList = new HashMap<Blob, BlobForm>();

		Circle fond_Terrain = new Circle(rayonRepresentation, rayonRepresentation, rayonRepresentation);
		fond_Terrain.setFill(Color.BLACK);

		this.setTranslateX(0);// on positionne le groupe plutôt que le rectangle
		this.setTranslateY(0);

		this.getChildren().add(fond_Terrain);// on ajoute le rectangle au groupe

	}
	
	
	// param : the coordinates on meter. Returne the coordinates(pxl) in the shape.
	public double[] metreToPxl(double[] coo){
		double[] res = new double[2];
		res[0] = coo[0]/rayonSalle * (rayonRepresentation - tailleBlob/2);
		res[1] = coo[1]/rayonSalle * (rayonRepresentation - tailleBlob/2);
		return res;
	}
	
	// param : the coordinates on meter. Returne the coordinates(pxl) in the shape.
		public double[] PxlTometre(double[] coo){
			double[] res = new double[2];
			res[0] = (coo[0])/(rayonRepresentation - tailleBlob/2) * rayonSalle;
			res[1] = (coo[1])/(rayonRepresentation - tailleBlob/2) * rayonSalle;
			return res;
		}
	

	public void add_blob(Blob b) {
		/*
		BlobForm bf = new BlobForm(b, metreToPxl(b.getCoordonnee()), tailleBlob);
		blobList.put(b, bf);
		getChildren().add(bf);
		*/
		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf = new BlobForm(b, metreToPxl(b.getCoordonnee()), tailleBlob);
				blobList.put(b, bf);
				getChildren().add(bf);
				
			}
		});

	}

	public void remove_blob(Blob b) {
		/*BlobForm bf = blobList.get(b);
		getChildren().remove(bf);
		blobList.remove(b);
		*/
		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf = blobList.get(b);
				getChildren().remove(bf);
				blobList.remove(b);
			}
		});

	}

	public void move_blob(Blob b) {
		/*
		 BlobForm bf = blobList.get(b);
		 assert bf!=null;
		bf.changeBlob(b, metreToPxl(b.getCoordonnee()), tailleBlob);
		
		*/
		 Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf = blobList.get(b);
				assert bf!=null;
				bf.changeBlob(b, metreToPxl(b.getCoordonnee()), tailleBlob);
			}

		});
		
	}
	
	public void showSelection(Blob blob){
		BlobForm bfToChange = blobList.get(blob);
		bfToChange.showSelection();
		
		/* Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bfToChange = blobList.get(blob);
				assert(bfToChange != null);
				bfToChange.showSelection();
			}
		});
		*/
	}
	
	public void deleteSelection(Blob blob){
		BlobForm bfToChange = blobList.get(blob);
		bfToChange.deleteSelection();
		/*
		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bfToChange = blobList.get(blob);
				bfToChange.deleteSelection();
			}
		});*/
	}
	
	public double getRayonSalle() {
		return rayonSalle;
	}


	public void setRayonSalle(double rayonSalle) {
		this.rayonSalle = rayonSalle;
	}


	public double getRayonRepresentation() {
		return rayonRepresentation;
	}


	public void setRayonRepresentation(double rayonRepresentation) {
		this.rayonRepresentation = rayonRepresentation;
	}


	public int getTailleBlob() {
		return tailleBlob;
	}


	public void setTailleBlob(int tailleBlob) {
		this.tailleBlob = tailleBlob;
	}
	
	
	
}

