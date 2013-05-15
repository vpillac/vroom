package vrp2013.util;

import vroom.common.modeling.dataModel.GroerSolutionHasher;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.ListRoutePool;
import vroom.common.modeling.dataModel.NodeSetSolutionHasher;
import vroom.common.modeling.util.HashRoutePool;
import vrp2013.examples.ExampleGRASP;

/**
 * The class <code>RoutePoolFactory</code> provides utility methods to instantiate route pools.
 * <p>
 * Creation date: 09/05/2013 - 3:58:43 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class RoutePoolFactory {

    /**
     * Instantiate and return a new {@link HashRoutePool} using an instance of {@link GroerSolutionHasher} as hasher
     * 
     * @param instance
     *            the instance being solved
     * @return a new {@link HashRoutePool}
     */
    public static HashRoutePool<INodeVisit> newHashPoolGroer(IVRPInstance instance) {
        return new HashRoutePool<>(1,// Number of vehicles
                ExampleGRASP.sGRASPIterations * 4 * 20,// Expected number of routes
                new GroerSolutionHasher(instance)); // Hash function
    }

    /**
     * Instantiate and return a new {@link HashRoutePool} using an instance of {@link NodeSetSolutionHasher} as hasher
     * 
     * @param instance
     *            the instance being solved
     * @return a new {@link HashRoutePool}
     */
    public static HashRoutePool<INodeVisit> newHashPoolSet(IVRPInstance instance) {
        return new HashRoutePool<>(1,// Number of vehicles
                ExampleGRASP.sGRASPIterations * 4 * 20,// Expected number of routes
                new NodeSetSolutionHasher(instance)); // Hash function
    }

    /**
     * Instantiate and return a new {@link ListRoutePool}
     * 
     * @return a new {@link ListRoutePool}
     */
    public static ListRoutePool<INodeVisit> newListPool() {
        return new ListRoutePool<>();
    }
}
