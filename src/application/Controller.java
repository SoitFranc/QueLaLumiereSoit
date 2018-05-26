package application;


import java.net.URL;
import java.util.ResourceBundle;

import business.Blob;
import business.Couleur;
import business.Forme;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.Initializable;

public class Controller implements Initializable{
	
    @FXML
    private TableView<?> testtableview;

    @FXML
    private Slider Sdiso;

    @FXML
    private Slider sHeterogeneite;

    @FXML
    private Label AffichDiso;

    @FXML
    private Slider sStabilit�Position;

    @FXML
    private Slider sStabilit�Etat;

    @FXML
    private AnchorPane panelTideal;

    @FXML
    private AnchorPane panelTreel;
    
    
    DoubleProperty Diso = new SimpleDoubleProperty(0);
    
    private TerrainForm tideal;
    
    private TerrainForm treel;

	@FXML
    void clic(MouseEvent event) {
    	
    	System.out.println(" Valeur Degr�s d'isolement : " + Diso.get() + "\n");
    	
    	
    }

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Diso.bind(Sdiso.valueProperty());
		
		tideal = new TerrainForm();
		panelTideal.getChildren().add(tideal);
		
		treel = new TerrainForm();
		panelTreel.getChildren().add(treel);
		
		
	}
	
	public void add_blobAgent(Blob b){
		tideal.add_blob(b);
	}
	
	public void add_blobReel(Blob b){
		treel.add_blob(b);
	}
	
	
	public void remove_blobAgent(Blob b){
		tideal.remove_blob(b);
	}
	
	public void remove_blobReel(Blob b){
		treel.remove_blob(b);
	}
	
	
	public void move_blobAgent(Blob b){
		tideal.move_blob(b);
	}
	
	public void move_blobReel(Blob b){
		treel.move_blob(b);
	}
	
	public int getIsolement(){
		return(Sdiso.valueProperty().intValue());
	}
	
	public int getHeterogenite(){
		return(sHeterogeneite.valueProperty().intValue());
	}
	
	public int getStabiliteHeterogeneite(){
		return(sStabilit�Etat.valueProperty().intValue());
	}
	
	public int getStabilitePosition(){
		return(sStabilit�Position.valueProperty().intValue());
	}
	
	public int getDistanceRepresentation(){
		//TODO
		return(0);
	}
	

}

/*
il suffit de construire une BufferedImage (format d'image standard de Java) et de la passer � un ImagePlus ou ImageProcessor (format ImageJ).

BufferedImage monimage = new BufferedImage(width, height, BufferedImage.LeTypeVoulu) ;

//Puis en fonction du type de l'image
return new BinaryProcessor(new ByteProcessor((java.awt.Image)source)) ;
return new ByteProcessor(source) ;
return new ShortProcessor(source) ;
*/