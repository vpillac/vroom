/**
 * 
 */
package vroom.common.heuristics.alns;

import vroom.common.heuristics.alns.IDestroy.IDestroyResult;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>IRepair</code> is an interface for the <em>repair</em> procedure used in the
 * {@link AdaptiveLargeNeighborhoodSearch ALNS}.
 * <p>
 * Creation date: May 12, 2011 - 1:16:17 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IRepair<S extends ISolution> extends IALNSComponent<S> {

    /**
     * Repair a solution
     * 
     * @param solution
     *            the solution to be repaired
     * @param destroyResult
     *            the destroy result
     * @param params
     *            the parameters
     * @return <code>true</code> if the solution was completely repaired, <code>false</code> otherwise
     */
    public boolean repair(S solution, IDestroyResult<S> destroyResult, IParameters params);

    /**
     * Clone this repair operator
     * 
     * @return a copy of this repair operator
     */
    public IRepair<S> clone();

}
