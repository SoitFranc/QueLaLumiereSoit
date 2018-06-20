package application;


import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import amak.AmasThread;
import amak.BlobAgent;
import amak.Immaginaire;
import amak.Migrant;
import business.Blob;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.fxml.Initializable;

public class Controller implements Initializable{
	
	private ArrayList<Migrant> blobHibernants;
	private ArrayList<Migrant> blobActifs;
	private Migrant blobToMove;
	private double[] valeurCurseurs = new double[4];
	
	
    @FXML
    private TableView<?> testtableview;

    @FXML
    private Slider Sdiso;

    @FXML
    private Slider sHeterogeneite;

    @FXML
    private Label AffichDiso;

    @FXML
    private Slider sStabilitePosition;


    @FXML
    private AnchorPane panelTideal;

    @FXML
    private AnchorPane panelTreel;
    
    @FXML
    private AnchorPane panelToriginel;
    
    @FXML
    private AnchorPane panelBlobSelectione;
    
    @FXML
    private Label labelAide;
    
    
    @FXML
    private Slider sRadiusVoisins;
    
    @FXML
    private Button buttonSortirBlob;
    
    @FXML
    private Button buttonChangerBlob;
    
    @FXML
    private Button buttonOKNbBlobs;
    
    @FXML
    private TextField textFieldNbBlobs;
    
    @FXML
    private Pane paneAppercuBlob;
    
    

    private TerrainForm tideal;  
    private TerrainForm treel;    
    private ToForm toriginel;
    private AppercuBlob appercuBlob;
    
    private AmasThread tAmas;
    
    DoubleProperty diso = new SimpleDoubleProperty(0);
    DoubleProperty hetero = new SimpleDoubleProperty(0);
    DoubleProperty stabPos = new SimpleDoubleProperty(0);
    DoubleProperty radiusVoisins = new SimpleDoubleProperty(0);


	@FXML
    void clicIso(MouseEvent event) {
    	
    	System.out.println(" Valeur Degr�s d'isolement : " + diso.get() + "\n");
    	tAmas.setIsolement(diso.getValue().intValue());
    	valeurCurseurs[0] = diso.getValue();
    }
	
	@FXML
    void clicHeter(MouseEvent event) {
    	System.out.println(" Valeur Degr�s d'heterog�n�it� : " + hetero.get() + "\n");
    	tAmas.setHeterogeneite(hetero.getValue().intValue());
    	valeurCurseurs[3] = hetero.getValue();
    }
	
	@FXML
    void clicStabPos(MouseEvent event) {
    	System.out.println(" Valeur de la stabilit� de la position du voisinage : " + stabPos.get() + "\n");
    	tAmas.setStabilitePosition(stabPos.getValue().intValue());
    	valeurCurseurs[2] = stabPos.getValue();
    }
	

    @FXML
    void clicRadiusVoisins(MouseEvent event) {
    	System.out.println(" Valeur du radius des voisins : " + radiusVoisins.get() + "\n");
    	tAmas.setRadiusVoisinage(radiusVoisins.getValue());
    	valeurCurseurs[1] = radiusVoisins.getValue();

    }
	
    @FXML
    void onClicButtonSortirBlob(MouseEvent event) {
    	// va sortir un Blob mur, pris au hasard dans To
    	if (blobHibernants != null)
    	{
    		// trouvons un blob mur :
    		boolean found = false;
    		Migrant migrant = blobHibernants.get(0);
    		int i = 0;
    		while(! found && i < blobHibernants.size()){
    			if (blobHibernants.get(i).isRiped()){
    				found = true;
    				migrant = blobHibernants.get(i);
    			}
    			i++;
    		}
    		
    		if(found)
    			sortirBlob(migrant);
    	}
    }
	
    @FXML
    void onClicButtonModifierBlob(MouseEvent event) {
    	if(labelAide.isVisible())
    	{
    		labelAide.setVisible(false);
    		panelTreel.getChildren().add(treel);
    	}
    	else
    	{
    		labelAide.setVisible(true);
    		panelTreel.getChildren().remove(treel);
    	}
    	   	
    }
    
    
    @FXML
    void onKeyPressed(KeyEvent event) {
    	
    	
    	KeyCode kcode = event.getCode();
    	//System.out.println("je viens d'appuyer sur une touche !");
    	
    	if (textFieldNbBlobs.getText().equals(""))
    	{
    		if (kcode.isDigitKey())
    			textFieldNbBlobs.setText(kcode.getName());
    		return;
    	}
    		
    		
    	
    	
    	
    	if(blobToMove == null)
    		return;
    	if(!blobActifs.contains(blobToMove))
    		return;
    	
    	if(kcode.isArrowKey())
    	{
    		double[] coo = blobToMove.getBlob().getCoordonnee().clone();
    	
    		if (kcode.equals(KeyCode.UP))
    			coo[1] -= 1;
    		else if (kcode.equals(KeyCode.DOWN))
    			coo[1] += 1;
    		else if (kcode.equals(KeyCode.RIGHT))
    			coo[0] += 1;
    		else
    			coo[0] -= 1;
    		
    		moveBlob(blobToMove, coo);
    	}
    	else if (kcode.isLetterKey())
    	{
    		deleteSelection();
    		rentrerBlob(blobToMove);
    	}
    	else if (kcode.equals(KeyCode.ESCAPE))
    		deleteSelection();
    	
    	// remise des curseurs � leur etat actuel
    	Sdiso.setValue(valeurCurseurs[0]);
    	sStabilitePosition.setValue(valeurCurseurs[2]);
    	sHeterogeneite.setValue(valeurCurseurs[3]);
    	sRadiusVoisins.setValue(valeurCurseurs[1]);
    }
    
    
    /* calcule la distance euclidienne entre 2 points cooA et cooB */
	private double calculeDistance(double[] cooA, double[] cooB){
		double sum = 0;
		for(int i = 0; i < cooA.length ; i++)
			sum += ((cooB[i] - cooA[i])*(cooB[i] - cooA[i]));
		return Math.sqrt(sum);		
		
	}
    
    
    @FXML
    void onClicTr(MouseEvent event) {
    	
    	if (blobToMove != null)
    		deleteSelection();
    	
    	
    	// Trouvons les coordonnes du clic au niveau de Tr
    	double xcor = event.getSceneX();
    	double ycor = event.getSceneY();
    	System.out.println("on a cliqu� sur les coordonn�es : " + xcor + " ; " + ycor);
    	
    	// la scene prend en compte le 1er xpanel. j'enl�ve donc sa largeur fixe de 500pxl
    	xcor -= 500;
    	
    	// les coordonnees des Blobs sont exprim�s en metres ... je transforme donc les pxls en metres.
    	double[] tmp = new double[2];
    	tmp[0] = xcor;
    	tmp[1] = ycor;
    	tmp = treel.PxlTometre(tmp);
    	System.out.println("equivalent en metre �  : " + tmp[0] + " ; " + tmp[1]);

    	
    	
    	if(blobActifs.size() == 0)
    	{
    		System.out.println("Il n'y a rien a selectionner");
    		return;
    	}
    	
    	
    	//deleteSelection();
    	
    	// Trouvons le blob le plus proche de l'endroit cliqu�.
    	
    	blobToMove = blobActifs.get(0);
    	double distanceMin = calculeDistance(tmp, blobToMove.getBlob().getCoordonnee());
    	double distance;
    	
    	for (int i = 0; i < blobActifs.size(); i++){
    		distance = calculeDistance(tmp, blobActifs.get(i).getBlob().getCoordonnee());
    		if(distance < distanceMin)
    		{
    			distanceMin = distance;
    			blobToMove = blobActifs.get(i);
    		}
    	}
    	
    	showSelection();
    	
    }
    

    @FXML
    void onClicButtonOKnbBlobs(MouseEvent event) {
		System.out.println(textFieldNbBlobs.textProperty().getValue());
		int nbBlobs = Integer.parseInt(textFieldNbBlobs.textProperty().getValue());
		
		
    	tAmas = new AmasThread(this, nbBlobs);
		tAmas.start();
		 
		buttonOKNbBlobs.setDisable(true);
    }
    
    
    

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		diso.bind(Sdiso.valueProperty());
		hetero.bind(sHeterogeneite.valueProperty());
		stabPos.bind(sStabilitePosition.valueProperty());
		radiusVoisins.bind(sRadiusVoisins.valueProperty());
	    
		
		tideal = new TerrainForm();
		panelTideal.getChildren().add(tideal);
		
		treel = new TerrainForm();
		panelTreel.getChildren().add(treel);
		
		toriginel = new ToForm();
		panelToriginel.getChildren().add(toriginel);
		
		appercuBlob = new AppercuBlob();
		paneAppercuBlob.getChildren().add(appercuBlob);
		
		// J'initialise chaque sliders.
		Sdiso.setValue(10);
		sHeterogeneite.setValue(50);
		sStabilitePosition.setValue(75);
		sRadiusVoisins.setValue(7);
		blobActifs = new ArrayList<>();
		
		valeurCurseurs[0] = Sdiso.getValue();
		valeurCurseurs[1] = sRadiusVoisins.getValue();
		valeurCurseurs[2] = sStabilitePosition.getValue();
		valeurCurseurs[3] = sHeterogeneite.getValue();
		
	}
	
	public void add_blobImmaginaire(Immaginaire b){
		tideal.add_blob(b.getBlob());
	}
	
	public void add_blobMigrant(Migrant b){
		tideal.add_blob(b.getBlob());
		treel.add_blob(b.getBlob());
	}
	
	public void add_blobHibernant(Migrant b){
		toriginel.add_blob(b.getBlob(), false);
	}
	
	public void remove_blobImmaginaire(Immaginaire b){
		tideal.remove_blob(b.getBlob());
	}
	
	public void remove_blobMigrant(Migrant b){
		tideal.remove_blob(b.getBlob());
		treel.remove_blob(b.getBlob());
		if (b == blobToMove)
			deleteSelection();
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
		if (b == blobToMove)
			appercuBlob.move_blob(b);
	}
	
	public void move_blobHibernant(Migrant b){
		toriginel.move_blob(b.getBlob(), b.isRiped());
	}
	
	public int getIsolement(){
		return(Sdiso.valueProperty().intValue());
	}
	
	public int getHeterogenite(){
		return(sHeterogeneite.valueProperty().intValue());
	}
	
	
	public int getStabilitePosition(){
		return(sStabilitePosition.valueProperty().intValue());
	}
	


	public AmasThread gettAmas() {
		return tAmas;
	}


	public void settAmas(AmasThread tAmas) {
		this.tAmas = tAmas;
	}
	
	
	private void showSelection(){
		treel.showSelection(blobToMove.getBlob());
		appercuBlob.add_blob(blobToMove);
		
	}
	
	private void deleteSelection(){
		if(blobActifs.contains(blobToMove))
		{
			treel.deleteSelection(blobToMove.getBlob());
			appercuBlob.remove_blob(blobToMove);
		}
		
		blobToMove = null;
	}
	
	
	/* ***************************************************************************** *
	 *  ******** 		METHODES DE POSITION_THREAD			************************ *
	 *	**************************************************************************** */
	
	public void sortirBlob(Migrant b){
		Blob tmp = b.getBlob();
		double[] coo = new double[2];
		coo[0] = Math.random() * 25;
		boolean isOk = false;
		while(!isOk){
			coo[1] = Math.random() * 25;
			if ((coo[0] - 12.5)*(coo[0] - 12.5) + (coo[1] - 12.5) * (coo[1] - 12.5) <= 12.5 * 12.5)
				isOk = true;
		}
		
		tmp.setCoordonnee(coo);
		b.setBlob(tmp);
		tAmas.t0_to_tr(b);
		blobHibernants.remove(b);
		blobActifs.add(b);	
	}
	
	
	public void rentrerBlob(Migrant b){
		if (b == blobToMove)
			deleteSelection();
		tAmas.tr_to_t0(b);
		blobHibernants.add(b);
		blobActifs.remove(b);
		
	}
	
	public void moveBlob(Migrant b, double[] coo){
		tAmas.move_blob(b, coo);
		if (b == blobToMove)
			appercuBlob.move_blob(b);
	}

	public ArrayList<Migrant> getBlobHibernants() {
		return blobHibernants;
	}

	public void setBlobHibernants(ArrayList<Migrant> blobHibernants) {
		this.blobHibernants = blobHibernants;
	}
	
	

}

/*
il suffit de construire une BufferedImage (format d'image standard de Java) et de la passer ÃÂ  un ImagePlus ou ImageProcessor (format ImageJ).

BufferedImage monimage = new BufferedImage(width, height, BufferedImage.LeTypeVoulu) ;

//Puis en fonction du type de l'image
return new BinaryProcessor(new ByteProcessor((java.awt.Image)source)) ;
return new ByteProcessor(source) ;
return new ShortProcessor(source) ;
*/