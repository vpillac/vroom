/**
 * 
 */
package vroom.common.utilities;

/**
 * <code>Update</code> is a generic interface for parameters that will be passed to the
 * {@link IObserver#update(IObservable, Update)} method of the {@linkplain IObserver observers} registered to an
 * instance of {@link IObservable}.
 * <p>
 * Creation date: Jun 30, 2010 - 2:16:28 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface Update {

    /**
     * A description for this update
     * 
     * @return
     */
    public String getDescription();

}
