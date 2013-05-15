/**
 * 
 */
package vroom.common.utilities;

import java.util.Comparator;

/**
 * <code>ToStringComparator</code> is an implementation of {@link Comparator} that compares two objects by comparing the
 * strings returned by the {@link Object#toString()} method
 * <p>
 * Creation date: May 31, 2011 - 9:43:27 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ToStringComparator implements Comparator<Object> {

    public static final ToStringComparator INSTANCE = new ToStringComparator();

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Object o1, Object o2) {
        String s1 = o1 != null ? o1.toString() : "null";
        String s2 = o2 != null ? o2.toString() : "null";
        return s1.compareTo(s2.toString());
    }

}
