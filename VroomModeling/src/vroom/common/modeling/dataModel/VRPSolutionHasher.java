/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */
/**
 * 
 */
package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import vroom.common.utilities.dataModel.ISolutionHasher;

/**
 * The class <code>VRPSolutionHasher</code> is a base implementation of {@link ISolutionHasher} defining hashing
 * functions for both {@link IVRPSolution} and {@link IRoute}
 * <p>
 * Creation date: May 2, 2013 - 5:42:47 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public abstract class VRPSolutionHasher implements ISolutionHasher<IVRPSolution<?>> {

    final static Comparator<IRoute<?>> ROUTE_COMPARATOR = new Comparator<IRoute<?>>() {

                                                            @Override
                                                            public int compare(IRoute<?> o1,
                                                                    IRoute<?> o2) {
                                                                if (o1.getVehicle().getID() != o2
                                                                        .getVehicle().getID())
                                                                    return o1.getVehicle().getID()
                                                                            - o2.getVehicle()
                                                                                    .getID();

                                                                int id1 = o1.length() > 2 ? o1
                                                                        .getNodeAt(1).getID() : o1
                                                                        .length() > 1 ? o1
                                                                        .getNodeAt(0).getID() : 0;
                                                                int id2 = o2.length() > 2 ? o2
                                                                        .getNodeAt(1).getID() : o2
                                                                        .length() > 1 ? o2
                                                                        .getNodeAt(0).getID() : 0;

                                                                return id1 - id2;
                                                            }
                                                        };

    @Override
    public int hash(IVRPSolution<?> solution) {
        int hash = 0;

        // Sort the routes to break symmetries
        ArrayList<IRoute<?>> routes = new ArrayList<>();
        for (int t = 0; t < solution.getRouteCount(); t++) {
            routes.add(solution.getRoute(t));
        }
        Collections.sort(routes, ROUTE_COMPARATOR);

        for (IRoute<?> t : routes) {
            hash = hash(t, hash);
        }

        return hash;
    }

    /**
     * Hash a route starting with an initial hash value
     * 
     * @param route
     * @param hash
     * @return the hash code for {@code  route} using {@code  hash} as a base value
     * @author vpillac
     */
    protected abstract int hash(IRoute<?> route, int hash);

    /**
     * Hash a route
     * 
     * @param route
     * @return the hash for {@code  route}
     * @author vpillac
     */
    public int hash(IRoute<?> route) {
        return hash(route, 0);
    }

}
