/**
 * 
 */
package vroom.common.utilities.optimization;

/**
 * <code>IInstance</code> is an interface for all instances that can be used within an optimization procedure
 * <p>
 * Creation date: Apr 28, 2010 - 4:04:36 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IInstance {

    /**
     * Returns the name of this instance
     * 
     * @return the name of this instance
     */
    public String getName();

}
