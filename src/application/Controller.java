package application;


import java.net.URL;
import java.util.ResourceBundle;

import amak.AmasThread;
import amak.BlobAgent;
import amak.Immaginaire;
import amak.Migrant;
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
    
    @FXML
    private AnchorPane panelToriginel;
    
    @FXML
    private Slider STauxMurissement;
    
    @FXML
    private Slider SDistanceR�alit�;
    
    
    private TerrainForm tideal;  
    private TerrainForm treel;    
    private TerrainForm toriginel;
    
    private AmasThread tAmas;
    
    DoubleProperty diso = new SimpleDoubleProperty(0);
    DoubleProperty hetero = new SimpleDoubleProperty(0);
    DoubleProperty stabPos = new SimpleDoubleProperty(0);
    DoubleProperty stabEtat = new SimpleDoubleProperty(0);
    DoubleProperty tauxMur = new SimpleDoubleProperty(0);
    DoubleProperty distanceR�alit� = new SimpleDoubleProperty(0);


	@FXML
    void clicIso(MouseEvent event) {
    	
    	System.out.println(" Valeur Degr�s d'isolement : " + diso.get() + "\n");
    	
    }
	
	@FXML
    void clicHeter(MouseEvent event) {
    	
    	System.out.println(" Valeur Degr�s d'heterog�n�it� : " + hetero.get() + "\n");
    	
    }
	
	@FXML
    void clicStabPos(MouseEvent event) {
    	
    	System.out.println(" Valeur de la stabilit� de la position du voisinage : " + stabPos.get() + "\n");
    	
    }
	
	@FXML
    void clicEtatVois(MouseEvent event) {
    	
    	System.out.println(" Valeur de la stabilit� de l'etat du voisinage : " + stabEtat.get() + "\n");
    	if(tAmas != null){
    		tAmas.setCaracteristiques(diso.getValue().intValue(), hetero.getValue().intValue(), stabEtat.getValue().intValue(), stabPos.getValue().intValue());
    		
    	}
    }
	
	@FXML
    void clicTauxMur(MouseEvent event) {
    	
    	System.out.println(" Valeur de la stabilit� du taux de murriseement : " + tauxMur.get() + "\n");
    	
    }
	
	@FXML
    void clicDistR�a(MouseEvent event) {
    	
    	System.out.println(" Valeur de la stabilit� de la distance � la r�alit� : " + distanceR�alit�.get() + "\n");
    	
    }
	
	


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		diso.bind(Sdiso.valueProperty());
		hetero.bind(sHeterogeneite.valueProperty());
		stabPos.bind(sStabilit�Position.valueProperty());
		stabEtat.bind(sStabilit�Etat.valueProperty());
		tauxMur.bind(STauxMurissement.valueProperty());
	    distanceR�alit�.bind(SDistanceR�alit�.valueProperty());
	    
		
		tideal = new TerrainForm();
		panelTideal.getChildren().add(tideal);
		
		treel = new TerrainForm();
		panelTreel.getChildren().add(treel);
		
		toriginel = new TerrainForm();
		panelToriginel.getChildren().add(toriginel);
		
		// J'initialise � 2 chaque sliders.
		Sdiso.setValue(2);
		sHeterogeneite.setValue(2);
		sStabilit�Etat.setValue(2);
		sStabilit�Position.setValue(2);
		
		
		
	}
	
	public void add_blobImmaginaire(Immaginaire b){
		tideal.add_blob(b.getBlob());
	}
	
	public void add_blobMigrant(Migrant b){
		tideal.add_blob(b.getBlob());
		treel.add_blob(b.getBlob());
	}
	
	public void add_blobHibernant(BlobAgent b){
		toriginel.add_blob(b.getBlob());
	}
	
	public void remove_blobImmaginaire(Immaginaire b){
		tideal.remove_blob(b.getBlob());
	}
	
	public void remove_blobMigrant(Migrant b){
		tideal.remove_blob(b.getBlob());
		treel.remove_blob(b.getBlob());
	}
	
	
	public void remove_blobHibernant(BlobAgent b){
		toriginel.remove_blob(b.getBlob());

	}
	
	public void move_blobImmaginaire(Immaginaire b){
		tideal.move_blob(b.getBlob());
	}
	
	public void move_blobMigrant(Migrant b){
		tideal.move_blob(b.getBlob());
		treel.move_blob(b.getBlob());
	}
	
	public void move_blobHibernant(BlobAgent b){
		toriginel.move_blob(b.getBlob());

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


	public AmasThread gettAmas() {
		return tAmas;
	}


	public void settAmas(AmasThread tAmas) {
		this.tAmas = tAmas;
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