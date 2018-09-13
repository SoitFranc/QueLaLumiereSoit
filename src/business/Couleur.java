package business;

import javafx.scene.paint.Color;

/**
 * Couleurs utilisées pour décrire les Blobs. Chaque couleur est relié à une
 * couleur de la classe Color de Java(fx)
 * 
 * @author Claire MEVOLHON
 *
 */
public enum Couleur {
	RED, BLUE, YELLOW, GREEN, AQUA, DARKORANGE, BISQUE, BLUEVIOLET, DARKSALMON, CYAN, BURLYWOOD, CORAL;

	/**
	 * retourne la couleur correspondante de la classe Color de Java(fx)
	 * 
	 * @param clr
	 *            Couleur (de l'énumaration)
	 * @return la Color de java(fx) correspondante
	 */
	public Color getColor(Couleur clr) {
		if (clr == Couleur.YELLOW)
			return (Color.YELLOW);
		if (clr == RED)
			return (Color.RED);
		if (clr == BLUE)
			return (Color.BLUE);
		if (clr == GREEN)
			return (Color.GREEN);
		if (clr == AQUA)
			return (Color.AQUA);
		if (clr == BISQUE)
			return (Color.BISQUE);
		if (clr == BLUEVIOLET)
			return (Color.BLUEVIOLET);
		if (clr == BURLYWOOD)
			return (Color.BURLYWOOD);
		if (clr == CORAL)
			return (Color.CORAL);
		if (clr == CYAN)
			return (Color.CYAN);
		if (clr == DARKSALMON)
			return (Color.DARKSALMON);
		if (clr == DARKORANGE)
			return (Color.DARKORANGE);

		return (Color.GRAY);
	}

}
