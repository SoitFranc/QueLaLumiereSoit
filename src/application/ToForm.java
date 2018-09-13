package application;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import business.Blob;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
//import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


// l'ensemble des coordonnees des blobs seront donnees en poucentage pour les absisses et ordonnees.
public class ToForm extends Parent{
	private Map<Blob, BlobForm> blobList;
	private double dimRepresentation;	// rayon/cot� de la repr�sentation en pxl (il s'agit d'une sph�re)
	private int tailleBlob = 60;
	Circle fond_Terrain;

	
	public ToForm(int tailleRepresentation) {
		dimRepresentation = tailleRepresentation;
		tailleBlob = tailleBlob * tailleRepresentation / 350 ;
		blobList = new ConcurrentHashMap<Blob, BlobForm>();

		fond_Terrain = new Circle (dimRepresentation/2, dimRepresentation/2, dimRepresentation/2);
		fond_Terrain.setFill(Color.BLACK);

		this.setTranslateX(0);// on positionne le groupe plut�t que le rectangle
		this.setTranslateY(0);

		this.getChildren().add(fond_Terrain);// on ajoute le rectangle au groupe
	}
	
	
	
	public ToForm() {
		blobList = new HashMap<Blob, BlobForm>();
		dimRepresentation = 350;
		fond_Terrain = new Circle (dimRepresentation/2, dimRepresentation/2, dimRepresentation/2);
		/*
		Rectangle fond_Terrain = new Rectangle ();
		fond_Terrain.setWidth(dimRepresentation);
		fond_Terrain.setHeight(dimRepresentation);
		fond_Terrain.setArcWidth(10);
		fond_Terrain.setArcHeight(10);
		*/
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
	
	

	public void add_blob(Blob b, boolean isRiped) {

		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf;
				if (isRiped)
					bf = new BlobForm(b, percentToRepresentation(b.getCoordonnee()), (int)tailleBlob);
				else
					bf = new BlobForm(b, percentToRepresentation(b.getCoordonnee()), Color.WHITE, (int)tailleBlob);

				blobList.put(b, bf);
				getChildren().add(bf);
				
			}
		});

	}

	
	
	public void artifice(BlobForm bf) {
		FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.1),bf);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		fadeTransition.setCycleCount(10);
		fadeTransition.play();
		
		fadeTransition.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
            	getChildren().remove(bf);
            }
        });
	}
	
	
	public void remove_blob(Blob b) {
		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf = blobList.get(b);
				blobList.remove(b);
				// petit artifice.
				artifice(bf);
				//bf.artifice();
				//getChildren().remove(bf);
			}
		});

	}

	public void move_blob(Blob b, boolean isRiped) {
		Platform.runLater(new Runnable() {
			public void run() {
				BlobForm bf = blobList.get(b);
				if (isRiped)
					bf.changeBlob(b, percentToRepresentation(b.getCoordonnee()), tailleBlob);
				else
					bf.changeBlob(b, percentToRepresentation(b.getCoordonnee()), Color.WHITE, tailleBlob);

			}
		});
	}
	
	public void putStroke() {	
		fond_Terrain.setStroke(Color.WHITE);	
	}
	
	
	
	
}
