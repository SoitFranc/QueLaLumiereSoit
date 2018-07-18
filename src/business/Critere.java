package business;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> Enum�ration des crit�res utilis�s pour les criticit�s des agents. </p>
 * <p> Ce sons aussi ceux gouvern�s par les curseurs. </p>
 * <p> Cette �num�ration n'est utilis� que pour les criticit� dans le package amak </p>
 * 
 * @author Claire M�volhon
 *
 */
public enum Critere {
	Isolement(0), Stabilite_etat(1), Stabilite_position(2), Heterogeneite(3), Murissement(4), FIN(5);
	private int value;
	private static Map<Integer, Critere> map = new HashMap<>();
	
	/**
	 * permet de creer un tableau de Criteres, en utilisant les indices
	 * � partir d'un indice, renvoie le crit�re �quivalant
	 * @param id	l'indice
	 */
	private Critere(int id){
		value = id;
	}
	
	/**
	 * regroupe chaque Critere � un entier
	 * permet de creer un tableau de Criteres, en utilisant les indices
	 */
	static {
        for (Critere critere : Critere.values()) {
            map.put(critere.value, critere);
        }
    }

	
	/**
	 * retourne le nombre li� au critere donn� en param�tre
	 * pouvant correspondre � son indice dans un tableau
	 * @param critere le critere
	 * @return le nombre (ou l'indice) correspondant
	 */
    public static Critere valueOf(int critere) {
        return (Critere) map.get(critere);
    }

    /**
     * retourne le nombre li�
	 * pouvant correspondre � son indice dans un tableau
     * @return le nombre (ou l'indice) correspondant
     */
    public int getValue() {
        return value;
    }
	
	
}
