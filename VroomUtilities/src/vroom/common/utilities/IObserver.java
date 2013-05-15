package vroom.common.utilities;

/**
 * <code>IObserver</code> is an interface for classes that can observe changes made to instances of classes implementing
 * {@link IObservable}
 * <p>
 * Creation date: Apr 23, 2010 - 11:07:12 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @see IObservable
 */
public interface IObserver {

    /**
     * Method called by the observed objects to notify a change
     * 
     * @param source
     * @param update
     */
    public void update(IObservable source, Update update);
}
