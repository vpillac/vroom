/*
 * 
 */
package vroom.common.utilities;

/**
 * <code>IDerefenceable</code> is an interface for objects that are susceptible of creating references to themselves in
 * other objects preventing proper collection by the GC.
 * <p>
 * This interface defines the {@link #dereference()} method which implementation should ensure that all created
 * references are removed
 * </p>
 * <p>
 * Creation date: Sep 6, 2010 - 6:37:30 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IDerefenceable {

    /**
     * Remove all the self created references to this object
     */
    public void dereference();

}
