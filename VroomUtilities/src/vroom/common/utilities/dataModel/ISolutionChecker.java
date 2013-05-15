/**
 *
 */
package vroom.common.utilities.dataModel;

import vroom.common.utilities.optimization.ISolution;

/**
 * <code>ISolutionChecker</code> is an interface for classes used to check the feasibility of a solution.
 * <p>
 * Creation date: Aug 29, 2011 - 3:56:43 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ISolutionChecker<S extends ISolution> {

    /**
     * Check the feasibility of a solution
     * 
     * @param solution
     *            the solution to be checked
     * @return a string describing the infeasibility of <code>solution</code>, or an empty string if the solution is
     *         feasible
     */
    public String checkSolution(S solution);

}
