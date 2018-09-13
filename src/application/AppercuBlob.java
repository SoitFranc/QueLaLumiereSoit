package application;

import amak.BlobAgent;
import javafx.application.Platform;
import javafx.scene.Parent;

/**
 * Forme comportant le zoom sur un blob en particulier. <br>
 * (Celle située actuellement en bas à droite de l'écran)
 * <p>
 * Elle affiche le blob qui est actuellement sélectionné (encadré) dans Tr.
 * </p>
 * <p>
 * Elle est mise à jour directement par le controller
 * </p>
 * 
 * @author Claire
 *
 */
public class AppercuBlob extends Parent {

	/** contient un {@link application.BlobForm BlobForm} lié au blob à afficher */
	BlobForm blobForm;
	/** contient l'agent à afficheer */
	BlobAgent agent;
	/** coordonnée où sera affiché le blob. Initialisé dans le constructeur */
	double[] coordonnee;

	public AppercuBlob() {
		coordonnee = new double[2];
		coordonnee[0] = 50;
		coordonnee[1] = 50;
	}

	/**
	 * Ajoute l'affichage de l'agent donné en paramètre
	 * 
	 * @param agent
	 *            l'agent à afficher
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
	 * Supprime l'affichage de l'agent donné en paramètre
	 * 
	 * @param agent
	 *            l'agent à supprimer
	 */
	public void remove_blob(BlobAgent agent) {
		Platform.runLater(new Runnable() {
			public void run() {
				getChildren().remove(blobForm);
			}
		});

	}

	/**
	 * Modifie l'affichage de l'agent donné en paramètre
	 * 
	 * @param agent
	 *            l'agent à modifier
	 */
	public void move_blob(BlobAgent agent) {
		Platform.runLater(new Runnable() {
			public void run() {
				blobForm.changeBlob(agent.getBlob(), coordonnee, 200);
			}
		});
	}

}
