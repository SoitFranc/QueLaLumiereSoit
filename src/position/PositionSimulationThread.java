package position;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import amak.AmasThread;
import amak.Migrant;
import javafx.application.Platform;

/**
 * Normalement, cette application fonctionne en tant que serveur pour recevoir
 * la position des portables ayant adopté des blobs. La position des blobs dans
 * Tréel dépendent donc du serveur. <br>
 * En mode test : pas besoin de connexions aux portables. <br>
 * Cette classe permet de simuler un serveur recevant les positions des
 * différents portables.
 * <p>
 * Pour simuler un déplacement, cette classe fait appel à la méthode
 * nouvellesCoordonnees() de la classe MyEnvironment, permettant de simuler un
 * déplacement semblable à celui d'un agent immaginaire. <br>
 * On suppose ici un déplacement de 1m , effectué à une fréquence de chaque
 * seconde
 * 
 * @author claire MEVOLHON
 *
 */
public class PositionSimulationThread extends Thread {
	/**
	 * Liste contenant l'ensemble des blobs de Tr dont la position doit �être
	 * simulée.
	 */
	private ArrayList<Migrant> blobActifs;
	/**
	 * L'appelant des différentes méthodes est l'IHM. Mais les positions ne sont aps
	 * directement renvoyées à l'IHM, car c'est l'AMAS qui gère les positions. <br>
	 * Toute demande de nouvelle position passe donc par l'appel de méthodes du
	 * thread tAmas donné en attribut.
	 */
	private AmasThread tAmas;
	private Timer timer;
	/**
	 * L'IHM permet à tout moment de (re)démarrer ou stopper la simulation.<br>
	 * Ce booleen indique donc l'état du thread
	 */
	public boolean is_interrupt;

	@SuppressWarnings("unchecked")
	public PositionSimulationThread(AmasThread tAmas, ArrayList<Migrant> migrants) {
		super();

		this.tAmas = tAmas;
		blobActifs = new ArrayList<>();
		// l'IHM peut comporter un blob selectionner qu'on ne doit pas simuler.
		// Nous n'avons donc pas constament la m�me liste. D'o� la copie :
		blobActifs = (ArrayList<Migrant>) migrants.clone();
	}

	/**
	 * demande le déplacement d'un blob à l'AMAS. l'IHM sera maj par l'AMAS.
	 * 
	 * @param b
	 *            le blob
	 * @param coo
	 *            ses nouvelles coordonnees
	 */
	public void moveBlob(Migrant b, double[] coo) {
		tAmas.move_blob(b, coo);
	}

	/**
	 * méthode interne pour simuler de nouvelles coordonnées probables pour chaque
	 * blobs de la simulation.
	 */
	private void bouger_blobs() {
		double[] coo;

		for (Migrant blob : blobActifs) {
			coo = blob.getAmas().getEnvironment().nouvellesCoordonnees(blob, 1, blob.getPastDirection());
			moveBlob(blob, coo);
		}
	}

	/**
	 * En lien avec l'IHM.<br>
	 * L'utilisateur peut décider de simuler ou non le déplacement des blobs dans
	 * Tr.<br>
	 * Ainsi un bouton est mis à disposition dans l'IHM pour interrompre ce thread.
	 */
	public void interruption() {
		timer.cancel();
		is_interrupt = true;

	}

	/**
	 * Démarre (ou redémarre) la simulation par un scheduller d'une fréquence de 1
	 * seconde faisant appel à la méthode {@link #bouger_blobs() bouger_blobs}.
	 */
	// @Override
	public void demarrer() {

		timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Your database code here
				bouger_blobs();
			}
		}, 1 * 1000, 1 * 1000);

		System.out.println("hey !");
		is_interrupt = false;
	}

	@Override
	public void run() {
		is_interrupt = true;

	}

	public boolean isIs_interrupt() {
		return is_interrupt;
	}

	public void setIs_interrupt(boolean is_interrupt) {
		this.is_interrupt = is_interrupt;
	}

	/**
	 * Méthode appelée par l'IHM lorsque l'utilisateur décide d'adopter un blob.<br>
	 * Un blob passe donc de To � Tr et son d�placement doit maintenant être
	 * simulé.<br>
	 * Autre appel : lors de la désélection d'un blob de Tr par l'IHM.
	 * 
	 * @param blob
	 *            la référence du blob à simuler.
	 */
	public void add_blob(Migrant blob) {
		Platform.runLater(new Runnable() {
			public void run() {
				blobActifs.add(blob);
			}
		});
	}

	/**
	 * Méthode appelée par l'IHM lorsque l'utilisateur de remettre un blob de Tr
	 * vers To.<br>
	 * Le déplacement du blob n'est alors plus simulé car répond aux règles de
	 * l'AMAS.<br>
	 * Autre appel : lors de la Sélection d'un blob de Tr par l'IHM : le déplacement
	 * de ce blob se fait alors manuellement par l'IHM.
	 * 
	 * @param blob
	 *            la référence du blob à simuler.
	 */
	public void remove_blob(Migrant blob) {
		Platform.runLater(new Runnable() {
			public void run() {
				blobActifs.remove(blob);
			}
		});
	}

}
