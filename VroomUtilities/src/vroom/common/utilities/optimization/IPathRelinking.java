/**
 * 
 */
package vroom.common.utilities.optimization;

import java.util.List;

/**
 * <code>IPathRelinking</code> is an interface for all classes that will do a path relinking between two solutions
 * <p>
 * Creation date: Nov 29, 2011 - 4:46:56 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IPathRelinking<S extends ISolution> {

    /**
     * Perform a path relinking between {@code  start} and {@code  target}
     * 
     * @param start
     *            the starting solution of the PR
     * @param target
     *            the target solution for the PR
     * @param params
     *            parameters for the PR
     * @return a list of solutions found while performing the path relinking between {@code  start} and {@code  target}
     */
    public List<S> pathRelinking(S start, S target, IParameters params);

}
