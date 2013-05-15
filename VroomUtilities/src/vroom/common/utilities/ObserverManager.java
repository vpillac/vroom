package vroom.common.utilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * <code>ObserverManager</code> is a utility class to register and notify observers.
 * <p>
 * Creation date: Apr 23, 2010 - 11:11:46 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ObserverManager {

    private final Set<IObserver> mObservers;

    private final IObservable    mSource;

    /**
     * Creates a new <code>ObserverManager</code>
     * 
     * @param source
     *            the object that {@link IObserver observers} will monitor
     */
    public ObserverManager(IObservable source) {
        mSource = source;
        mObservers = Collections.synchronizedSet(new HashSet<IObserver>());
    }

    /**
     * Notify all the registered observers with the given <code>update</code>
     * <p>
     * Note that this method will automatically remove detached {@link ObserverProxy}.
     * </p>
     * 
     * @param update
     *            an object describing the change made on the source
     */
    public void notifyObservers(Update update) {
        synchronized (mObservers) {
            LinkedList<IObserver> rem = new LinkedList<IObserver>();
            for (IObserver o : mObservers) {
                if (o instanceof ObserverProxy && ((ObserverProxy) o).isDetached()) {
                    rem.add(o);
                } else {
                    o.update(mSource, update);
                }
            }
            mObservers.removeAll(rem);
        }
    }

    /**
     * Adds an observer to the set of observers for this object, provided that it is not the same as some observer
     * already in the set. The order in which notifications will be delivered to multiple observers is not specified.
     * 
     * @param o
     *            an observer to be added.
     * @throws IllegalArgumentException
     *             if the parameter o is null.
     */
    public void addObserver(IObserver o) {
        if (o == null) {
            throw new IllegalArgumentException("The observer cannot be null");
        }
        synchronized (mObservers) {
            if (!mObservers.contains(o)) {
                mObservers.add(o);
            }
        }
    }

    /**
     * Removes an observer from the set of observers of this object. Passing <CODE>null</CODE> to this method will have
     * no effect.
     * 
     * @param o
     *            the observer to be removed.
     */
    public void removeObserver(IObserver o) {
        synchronized (mObservers) {
            mObservers.remove(o);
        }
    }

    /**
     * Remove all observers from the set of observers of this object.
     */
    public void removeAllObservers() {
        synchronized (mObservers) {
            mObservers.clear();
        }
    }
}
