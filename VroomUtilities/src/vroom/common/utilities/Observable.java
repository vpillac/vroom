package vroom.common.utilities;

import java.util.Observer;

/**
 * The Interface Observable.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public interface Observable {

    /**
     * Adds the observer.
     * 
     * @param observer
     *            the observer
     */
    public void addObserver(Observer observer);

    /**
     * Notify observers.
     * 
     * @param param
     *            the param
     */
    public void notifyObservers(Object param);
}
