/**
 * 
 */
package vroom.common.utilities.dataModel;

import vroom.common.utilities.optimization.ISolution;

/**
 * <code>DefaultSolutionHasher</code> is an implementation of {@link ISolutionHasher} that returns the values generated
 * by the default {@link Object#hashCode()} method
 * <p>
 * Creation date: May 25, 2011 - 1:15:31 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DefaultSolutionHasher<S extends ISolution> implements ISolutionHasher<S> {

    /* (non-Javadoc)
     * @see vroom.common.utilities.dataModel.ISolutionHasher#hash(vroom.common.utilities.optimization.ISolution)
     */
    @Override
    public int hash(S solution) {
        if (solution == null)
            throw new NullPointerException("Hashed solution cannot be null");
        return solution.hashCode();
    }

}
