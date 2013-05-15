/**
 *
 */
package vroom.common.utilities;

/**
 * <code>IDisposable</code> is an interface for classes that will implement a {@link #dispose()} method to help the
 * garbage collector freeing up memory and stopping threads that are no longer needed
 * <p>
 * Creation date: Aug 22, 2011 - 9:39:05 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IDisposable {

    /**
     * Dispose this object to help the garbage collector freeing up memory
     */
    public void dispose();

}
