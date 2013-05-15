/**
 * 
 */
package vroom.trsp.datamodel;

import java.util.ListIterator;

/**
 * <code>ITourIterator</code> is an interface for iterators over {@linkplain ITRSPTour tours}.
 * <p>
 * Creation date: Sep 26, 2011 - 4:24:31 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ITourIterator extends ListIterator<Integer>, Cloneable {

    /**
     * Returns an iterator starting at the current position
     * <p>
     * The next call to {@link #next()} of both the original and returned iterator will return the same value
     * </p>
     * 
     * @return an iterator starting at the current position
     */
    public ITourIterator subIterator();

}
