/**
 * 
 */
package vrp2013.util;

import java.util.ListIterator;

import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.dataModel.Solution;

/**
 * <code>VRPSolution</code> is an extension of {@link Solution} using {@link RouteBase} as {@link IRoute} implementation
 * used to simplify code
 * <p>
 * Creation date: 30/04/2013 - 5:08:45 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class VRPSolution extends Solution<RouteBase> {

    public VRPSolution(IVRPInstance parentInstance) {
        super(parentInstance);
    }

    /**
     * Remove empty routes from this solution
     */
    public void removeEmptyRoutes() {

        // Remove empty routes
        ListIterator<RouteBase> solIt = iterator();
        while (solIt.hasNext())
            if (solIt.next().length() < 3)
                solIt.remove();

    }

    @Override
    public VRPSolution clone() {
        VRPSolution clone = new VRPSolution(getParentInstance());

        for (RouteBase route : this) {
            clone.addRoute(route.clone());
        }

        return clone;
    }
}
