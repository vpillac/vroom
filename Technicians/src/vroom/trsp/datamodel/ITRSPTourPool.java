/**
 *
 */
package vroom.trsp.datamodel;

import java.util.Collection;

import vroom.common.utilities.IDisposable;

/**
 * <code>ITRSPTourPool</code> is an interface for classes that will store instances of {@link TRSPTour}
 * <p>
 * Creation date: Aug 16, 2011 - 2:52:03 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ITRSPTourPool extends IDisposable, Iterable<ITRSPTour> {

    /**
     * Add all the tours to this pool
     * 
     * @param tours
     *            the tours to be added
     * @return the number of tours that were actually added to the pool
     */
    public int add(Iterable<? extends ITRSPTour> tours);

    /**
     * Gets all the tours in this pool
     * 
     * @return all the tours in this pool
     */
    public Collection<ITRSPTour> getAllTours();

    /**
     * Gets the number of tours in this pool
     * 
     * @return the number of tours in this pool
     */
    public int size();

    public void clear();
}
