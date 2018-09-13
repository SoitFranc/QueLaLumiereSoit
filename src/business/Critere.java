package business;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Enumération des critères utilisés pour les criticités des agents.
 * </p>
 * <p>
 * Ce sons aussi ceux gouvernés par les curseurs.
 * </p>
 * <p>
 * Cette énumération n'est utilisé que pour les criticité dans le package amak
 * </p>
 * 
 * @author Claire Mévolhon
 *
 */
public enum Critere {
	Isolement(0), Stabilite_etat(1), Stabilite_position(2), Heterogeneite(3), Murissement(4), FIN(5);
	private int value;
	private static Map<Integer, Critere> map = new HashMap<>();

	/**
	 * permet de creer un tableau de Criteres, en utilisant les indices à partir
	 * d'un indice, renvoie le critère équivalant
	 * 
	 * @param id
	 *            l'indice
	 */
	private Critere(int id) {
		value = id;
	}

	/**
	 * regroupe chaque Critere à un entier permet de creer un tableau de Criteres,
	 * en utilisant les indices
	 */
	static {
		for (Critere critere : Critere.values()) {
			map.put(critere.value, critere);
		}
	}

	/**
	 * retourne le nombre lié au critere donné en paramètre pouvant correspondre à
	 * son indice dans un tableau
	 * 
	 * @param critere
	 *            le critere
	 * @return le nombre (ou l'indice) correspondant
	 */
	public static Critere valueOf(int critere) {
		return (Critere) map.get(critere);
	}

	/**
	 * retourne le nombre lié pouvant correspondre à son indice dans un tableau
	 * 
	 * @return le nombre (ou l'indice) correspondant
	 */
	public int getValue() {
		return value;
	}

}
