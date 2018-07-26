package application;

import amak.BlobAgent;
import javafx.application.Platform;
import javafx.scene.Parent;

/**
 * Forme comportant le zoom sur un blob en particulier. <br>
 * (Celle situ�e actuellement en bas � droite de l'�cran)
 * <p>
 * Elle affiche le blob qui est actuellement s�lectionn� (encadr�) dans Tr.
 * </p>
 * <p>
 * Elle est mise � jour directement par le controller
 * </p>
 * 
 * @author Claire
 *
 */
public class AppercuBlob extends Parent {

	/** contient un {@link application.BlobForm BlobForm} li� au blob � afficher */
	BlobForm blobForm;
	/** contient l'agent � afficheer */
	BlobAgent agent;
	/** coordonn�e o� sera affich� le blob. Initialis� dans le constructeur */
	double[] coordonnee;

	public AppercuBlob() {
		coordonnee = new double[2];
		coordonnee[0] = 50;
		coordonnee[1] = 50;
	}

	/**
	 * Ajoute l'affichage de l'agent donn� en param�tre
	 * 
	 * @param agent
	 *            l'agent � afficher
	 */
	public void add_blob(BlobAgent agent) {
		this.agent = agent;

		Platform.runLater(new Runnable() {
			public void run() {
				blobForm = new BlobForm(agent.getBlob(), coordonnee, 200);
				getChildren().add(blobForm);

			}
		});

	}

	/**
	 * Supprime l'affichage de l'agent donn� en param�tre
	 * 
	 * @param agent
	 *            l'agent � supprimer
	 */
	public void remove_blob(BlobAgent agent) {
		Platform.runLater(new Runnable() {
			public void run() {
				getChildren().remove(blobForm);
			}
		});

	}

	/**
	 * Modifie l'affichage de l'agent donn� en param�tre
	 * 
	 * @param agent
	 *            l'agent � modifier
	 */
	public void move_blob(BlobAgent agent) {
		Platform.runLater(new Runnable() {
			public void run() {
				blobForm.changeBlob(agent.getBlob(), coordonnee, 200);
			}
		});
	}

}
