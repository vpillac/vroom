/**
 * 
 */
package vroom.common.utilities.dataModel;

import vroom.common.utilities.optimization.ISolution;

/**
 * <code>ISolutionHasher</code> is an interface for classes that associate a hash to a {@link ISolution}
 * <p>
 * Creation date: May 23, 2011 - 10:45:13 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ISolutionHasher<S extends ISolution> {

    /**
     * Evaluates the hash of a solution, the result is a signed 32bit integer, and implementations should ensure that
     * different hashes are associated with different solutions.
     * 
     * @param solution
     *            the solution to be hashed
     * @return a 32bit integer hash for <code>solution</code>
     * @see Object#hashCode()
     */
    public int hash(S solution);

}
