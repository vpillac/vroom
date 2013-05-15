/*
 * 
 */
package vroom.common.modeling.util;

import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.Vehicle;

/**
 * <code>DefaultSolutionFactory</code> is an implementation of {@link ISolutionFactory} that will return instances of
 * {@link Solution} and {@link ArrayListRoute}
 * <p>
 * Creation date: Sep 7, 2010 - 10:42:42 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DefaultSolutionFactory implements ISolutionFactory {

    /* (non-Javadoc)
     * @see vroom.common.modeling.util.ISolutionFactory#newSolution(vroom.common.modeling.dataModel.IVRPInstance, java.lang.Object[])
     */
    @Override
    public IVRPSolution<?> newSolution(IVRPInstance instance, Object... params) {
        return new Solution<ArrayListRoute>(instance);
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.util.ISolutionFactory#newRoute(vroom.common.modeling.dataModel.IVRPSolution, vroom.common.modeling.dataModel.Vehicle, java.lang.Object[])
     */
    @Override
    public IRoute<?> newRoute(IVRPSolution<?> solution, Vehicle vehicle, Object... params) {
        return new ArrayListRoute(solution, vehicle);
    }

}
