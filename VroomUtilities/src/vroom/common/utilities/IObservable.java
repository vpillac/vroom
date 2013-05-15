package vroom.common.utilities;

import java.util.Observer;

/**
 * <code>IObservable</code> is an interface for classes that will observe implementations of {@link IObserver}.
 * <p>
 * Creation date: Apr 23, 2010 - 11:06:35 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @see Observer
 */
public interface IObservable {

    /**
     * Adds an observer to the set of observers for this object, provided that it is not the same as some observer
     * already in the set. The order in which notifications will be delivered to multiple observers is not specified.
     * 
     * @param o
     *            an observer to be added.
     * @throws IllegalArgumentException
     *             if the parameter o is null.
     */
    public void addObserver(IObserver o);

    /**
     * Deletes an observer from the set of observers of this object. Passing <CODE>null</CODE> to this method will have
     * no effect.
     * 
     * @param o
     *            the observer to be deleted.
     */
    public void removeObserver(IObserver o);

    /**
     * Remove all observers from the set of observers of this object.
     */
    public void removeAllObservers();

}
