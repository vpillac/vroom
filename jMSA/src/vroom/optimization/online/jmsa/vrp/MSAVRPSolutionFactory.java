/*
 * 
 */
package vroom.optimization.online.jmsa.vrp;

import java.util.List;

import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.ISolutionFactory;

/**
 * <code>MSAVRPSolutionFactory</code>
 * <p>
 * Creation date: Sep 7, 2010 - 11:00:21 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MSAVRPSolutionFactory implements ISolutionFactory {

    private final Class<? extends RouteBase> mRouteImplem;

    /**
     * Creates a new <code>MSAVRPSolutionFactory</code>
     * 
     * @param routeImplem
     *            the implementation of {@link IRoute} to be used
     */
    public MSAVRPSolutionFactory(Class<? extends RouteBase> routeImplem) {
        mRouteImplem = routeImplem;
    }

    /**
     * Creates a new <code>MSAVRPSolutionFactory</code>
     */
    public MSAVRPSolutionFactory() {
        this(ArrayListRoute.class);
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.util.ISolutionFactory#newSolution(vroom.common.modeling.dataModel.IVRPInstance, java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public VRPScenario newSolution(IVRPInstance instance, Object... params) {
        List<VRPActualRequest> ar = null;
        List<VRPSampledRequest> sr = null;

        if (params.length >= 2) {
            if (params[0] instanceof List<?> && !((List<?>) params[0]).isEmpty()
                    && ((List<?>) params[0]).get(0) instanceof VRPActualRequest) {
                ar = (List<VRPActualRequest>) params[0];
            }
            if (params[1] instanceof List<?> && !((List<?>) params[1]).isEmpty()
                    && ((List<?>) params[1]).get(0) instanceof VRPSampledRequest) {
                sr = (List<VRPSampledRequest>) params[1];
            }
        }

        return new VRPScenario((MSAVRPInstance) instance, ar, sr);
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.util.ISolutionFactory#newRoute(vroom.common.modeling.dataModel.IVRPSolution, vroom.common.modeling.dataModel.Vehicle,
     * java.lang.Object[])
     */
    @Override
    public VRPScenarioRoute newRoute(IVRPSolution<?> solution, Vehicle vehicle, Object... params) {
        return new VRPScenarioRoute(mRouteImplem, (VRPScenario) solution, vehicle);
    }

}
