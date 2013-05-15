/**
 * 
 */
package vroom.common.heuristics.alns;

import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>IDestroy</code> is an interface for the <em>destroy</em> procedure used in the
 * {@link AdaptiveLargeNeighborhoodSearch ALNS}.
 * <p>
 * Creation date: May 12, 2011 - 1:16:08 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IDestroy<S extends ISolution> extends IALNSComponent<S> {

    /**
     * Destroy a solution
     * 
     * @param solution
     *            the solution
     * @param parameters
     *            the parameters
     * @param size
     *            the size of the <em>destroy</em> neighborhood, by convention in [0,1]
     * @return the destroy result
     */
    public IDestroyResult<S> destroy(S solution, IParameters parameters, double size);

    /**
     * Clone this destroy operator
     * 
     * @return a copy of this destroy operator
     */
    public IDestroy<S> clone();

    /**
     * The Interface <code>IDestroyResult</code> describes the result of the execution of the
     * {@link IDestroy#destroy(ISolution, IParameters, int)} procedure on a solution
     * <p>
     * Creation date: May 13, 2011 - 11:03:52 AM.
     * 
     * @param <SS>
     *            the generic type
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public interface IDestroyResult<SS extends ISolution> {

        /**
         * Returns <code>true</code> if the result is empty, in other words if the solution was left unchanged by the
         * {@link IDestroy#destroy(ISolution, IParameters, int)} procedure)
         * 
         * @return <code>true</code> if the result is empty
         */
        public boolean isNull();

    }
}
