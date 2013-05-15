/*
 * 
 */
package vroom.common.modeling.util;

import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>ISolutionFactory</code> is an interface for factory classes responsible for the instantiation of new
 * {@link IVRPSolution}
 * <p>
 * Creation date: Sep 7, 2010 - 10:22:02 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ISolutionFactory {

    /**
     * A factory method to create a new solution
     * 
     * @param instance
     *            the parent instance
     * @param params
     *            optional parameters
     * @return a new instance of {@link ISolution} associated with the given <code>instance</code>
     */
    public IVRPSolution<?> newSolution(IVRPInstance instance, Object... params);

    /**
     * A factory method to create a new route
     * 
     * @param solution
     *            the parent solution
     * @param vehicle
     *            the route vehicle
     * @param params
     *            optional parameters
     * @return a new instance of {@link IRoute} associated with the given <code>solution</code> and <code>vehicle</code>
     */
    public IRoute<?> newRoute(IVRPSolution<?> solution, Vehicle vehicle, Object... params);
}
