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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import position.PositionSimulationThread;
import position.ServerThreadAcceleration;
import javafx.fxml.Initializable;

public class Controller implements Initializable{
	
	private boolean experience;
	private ArrayList<Migrant> blobHibernants;
	private ArrayList<Migrant> blobActifs;
	private Migrant blobToMove;
	private double[] valeurCurseurs = new double[4];
	private PositionSimulationThread tSimuPosition;
	
	
	
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
    
    @FXML
    private Button buttonMouvementAleatoire;
    
    

    private TerrainForm tideal;
    private TerrainForm tideal_exp;
    private TerrainForm treel;
    private ToForm toriginel;
    private ToForm toriginel_exp;
    private AppercuBlob appercuBlob;
    
    private AmasThread tAmas;
    
    DoubleProperty diso = new SimpleDoubleProperty(0);
    DoubleProperty hetero = new SimpleDoubleProperty(0);
    DoubleProperty stabPos = new SimpleDoubleProperty(0);
    DoubleProperty radiusVoisins = new SimpleDoubleProperty(0);

	//décalage fenêtre pour déplacer To et Ti
    private double xOffset = 0;
    private double yOffset = 0;
    
	@FXML
    void clicIso(MouseEvent event) {
    	
    	System.out.println(" Valeur Degrés d'isolement : " + diso.get() + "\n");
    	tAmas.setIsolement(diso.getValue().intValue());
    	valeurCurseurs[0] = diso.getValue();
    }
	
	@FXML
    void clicHeter(MouseEvent event) {
    	System.out.println(" Valeur Degrés d'heterogénéité : " + hetero.get() + "\n");
    	tAmas.setHeterogeneite(hetero.getValue().intValue());
    	valeurCurseurs[3] = hetero.getValue();
    }
	
	@FXML
    void clicStabPos(MouseEvent event) {
    	System.out.println(" Valeur de la stabilité de la position du voisinage : " + stabPos.get() + "\n");
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
    	if (blobHibernants != null && !blobHibernants.isEmpty())
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
    void onClicButtonMouvementAleatoire(MouseEvent event) {

    	if(!tSimuPosition.is_interrupt)
    		tSimuPosition.interruption();
    		//tSimuPosition = null;
    	else
    		tSimuPosition.demarrer();
    	   	
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
    		
    		
    	
    	
    	
    	if(blobToMove == null || experience)
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
    		
    		if(!isValideInTi(coo))
    			return;
    		moveBlob(blobToMove, coo);
    	}
    	else if (kcode.isLetterKey())
    	{
    		Migrant tmp = blobToMove;
    		deleteSelection();
    		rentrerBlob(tmp);
    	}
    	else if (kcode.equals(KeyCode.ESCAPE))
    		deleteSelection();
    	
    	// remise des curseurs à leur etat actuel
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
    	System.out.println("on a cliqué sur les coordonnées : " + xcor + " ; " + ycor);
    	
    	// la scene prend en compte le 1er xpanel. j'enlève donc sa largeur fixe de 500pxl
    	xcor -= 500;
    	
    	// les coordonnees des Blobs sont exprimés en metres ... je transforme donc les pxls en metres.
    	double[] tmp = new double[2];
    	tmp[0] = xcor;
    	tmp[1] = ycor;
    	tmp = treel.PxlTometre(tmp);
    	System.out.println("equivalent en metre à  : " + tmp[0] + " ; " + tmp[1]);

    	
    	
    	if(blobActifs.size() == 0)
    	{
    		System.out.println("Il n'y a rien a selectionner");
    		return;
    	}
    	
    	
    	//deleteSelection();
    	
    	// Trouvons le blob le plus proche de l'endroit cliqué.
    	
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
		
		if(!experience)
		{
			tSimuPosition = new PositionSimulationThread(tAmas, blobActifs);
			tSimuPosition.start();
		}
		else
		{
			buttonMouvementAleatoire.setDisable(true);
			buttonSortirBlob.setDisable(true);
			//buttonChangerBlob.setDisable(true);
			System.out.println("Je cree le serveur");
			ServerThreadAcceleration server = new ServerThreadAcceleration(tAmas, blobHibernants);
			System.out.println("Je le run");
			server.start();
			System.out.println("j'ai fini de traiter ce bouton");

		}
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
		blobHibernants = new ArrayList<>();
		
		valeurCurseurs[0] = Sdiso.getValue();
		valeurCurseurs[1] = sRadiusVoisins.getValue();
		valeurCurseurs[2] = sStabilitePosition.getValue();
		valeurCurseurs[3] = sHeterogeneite.getValue();
		
	}
    
	public void configTerrain(Stage stage, Parent root)
	{
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
		
		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	stage.setX(event.getScreenX() - xOffset);
            	stage.setY(event.getScreenY() - yOffset);
            }
        });
	}
	
	public void initTO()
	{	
		Stage towindow = new Stage();

		towindow.initStyle(StageStyle.UNDECORATED);
		toriginel_exp = new ToForm(1075);

		towindow.setTitle("Territoire Originel");
		towindow.getIcons().add(new Image(Main.class.getResourceAsStream("icon_blob.png")));
		
		
		configTerrain(towindow, toriginel_exp);
		
		Scene scene = new Scene(toriginel_exp, Color.rgb(42, 42, 42));
		
		towindow.setScene(scene);
		towindow.show();
	}
	
	public void initTI()
	{
		Stage tiwindow = new Stage();

		tiwindow.initStyle(StageStyle.UNDECORATED);
		tideal_exp = new TerrainForm(1000/2);

		tiwindow.setTitle("Territoire Ideal");
		tiwindow.getIcons().add(new Image(Main.class.getResourceAsStream("icon_blob.png")));
		configTerrain(tiwindow, tideal_exp);

		tiwindow.setScene(new Scene(tideal_exp, Color.rgb(42, 42, 42)));
		tiwindow.show();
	}
	
	public void add_blobImmaginaire(Immaginaire b){
		tideal.add_blob(b.getBlob());
		if (experience)
			tideal_exp.add_blob(b.getBlob());

	}
	
	public void add_blobMigrant(Migrant b){
		System.out.println("J'ajoute un migrant Tr");
		tideal.add_blob(b.getBlob());
		if (experience)
			tideal_exp.add_blob(b.getBlob());
		treel.add_blob(b.getBlob());
		blobActifs.add(b);
	}
	
	public void add_blobHibernant(Migrant b){
		System.out.println("j'ajoute un hibernant tO");
		toriginel.add_blob(b.getBlob(), false);
		if (experience)
			toriginel_exp.add_blob(b.getBlob(), false);
		blobHibernants.add(b);
	}
	
	public void remove_blobImmaginaire(Immaginaire b){
		tideal.remove_blob(b.getBlob());
		if (experience)
			tideal_exp.remove_blob(b.getBlob());
	}
	
	public void remove_blobMigrant(Migrant b){
		if (b == blobToMove)
			deleteSelection();
		tideal.remove_blob(b.getBlob());
		if (experience)
			tideal_exp.remove_blob(b.getBlob());
		treel.remove_blob(b.getBlob());
		blobActifs.remove(b);

	}
	
	// ce remove est appelé par Amak seulement.
	public void remove_blobHibernant(BlobAgent b){
		toriginel.remove_blob(b.getBlob());
		if (experience)
			toriginel_exp.remove_blob(b.getBlob());
		blobHibernants.remove(b);
	}
	
	public void move_blobImmaginaire(Immaginaire b){
		tideal.move_blob(b.getBlob());
		if (experience)
			tideal_exp.move_blob(b.getBlob());
	}
	
	public void move_blobMigrant(Migrant b){
		tideal.move_blob(b.getBlob());
		if (experience)
			tideal_exp.move_blob(b.getBlob());
		treel.move_blob(b.getBlob());
		if (b == blobToMove)
			appercuBlob.move_blob(b);
	}
	
	public void move_blobHibernant(Migrant b){
		toriginel.move_blob(b.getBlob(), b.isRiped());
		if (experience)
			toriginel_exp.move_blob(b.getBlob(), b.isRiped());
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

	public void setexperience(boolean experience) {
		this.experience = experience;
	}
	
	public void settAmas(AmasThread tAmas) {
		this.tAmas = tAmas;
	}
	
	
	private void showSelection(){
		treel.showSelection(blobToMove.getBlob());
		appercuBlob.add_blob(blobToMove);
		if(!experience)
			tSimuPosition.remove_blob(blobToMove);
	}
	
	private void deleteSelection(){
		if(blobActifs.contains(blobToMove))
		{
			treel.deleteSelection(blobToMove.getBlob());
			appercuBlob.remove_blob(blobToMove);
			if(!experience)
				tSimuPosition.add_blob(blobToMove);
		}
		
		blobToMove = null;
	}
	
	
	/* ***************************************************************************** *
	 *  ******** 		METHODES DE POSITION_THREAD			************************ *
	 *	**************************************************************************** */
	
	// indique si la coordonnée entrée en paramètre est valide, ie si elle n'est pas hors terrain.
	// returne true if ok. 
	//Ici il s'agit de Tr ou Ti : valide si compris dans un cercle de rayon RayonTerrain et de centre (RayonTerrain;RayonTerrain)
	private boolean isValideInTi(double[] coo){
		if ((coo[0] - treel.getRayonSalle())*(coo[0] - treel.getRayonSalle()) + (coo[1] - treel.getRayonSalle()) * (coo[1] - treel.getRayonSalle()) <= treel.getRayonSalle() * treel.getRayonSalle())
			return true;
		return false;
	}
		

	// cette fonction n'est appelée que si nous sommes en mode test
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
		tSimuPosition.add_blob(b);
	}
	
	// cette fonction n'est appelée que si nous sommes en mode test
	public void rentrerBlob(Migrant b){
		System.out.println("je suis le 1 :" + b.getBlob().getCouleurLaPLusPresente().toString());
		if (b == blobToMove)
			deleteSelection();
		System.out.println("je suis le blob :" + b.getBlob().getCouleurLaPLusPresente().toString());
		tAmas.tr_to_t0(b);
		tSimuPosition.remove_blob(b);

		
	}
	
	// cette fonction n'est appelée que si nous sommes en mode test
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
il suffit de construire une BufferedImage (format d'image standard de Java) et de la passer ÃÂÃÂ  un ImagePlus ou ImageProcessor (format ImageJ).

BufferedImage monimage = new BufferedImage(width, height, BufferedImage.LeTypeVoulu) ;

//Puis en fonction du type de l'image
return new BinaryProcessor(new ByteProcessor((java.awt.Image)source)) ;
return new ByteProcessor(source) ;
return new ShortProcessor(source) ;
*/