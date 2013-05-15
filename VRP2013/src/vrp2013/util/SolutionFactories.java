/**
 * 
 */
package vrp2013.util;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.ListRoute;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.ISolutionFactory;

/**
 * <code>SolutionFactories</code> contains different solution factories to illustrate the differences between data
 * structures
 * <p>
 * Creation date: 30/04/2013 - 5:02:02 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class SolutionFactories {
    public static final ISolutionFactory ARRAY_LIST_SOL_FACTORY  = new ISolutionFactory() {
                                                                     @Override
                                                                     public VRPSolution newSolution(
                                                                             IVRPInstance instance,
                                                                             Object... params) {
                                                                         return new VRPSolution(
                                                                                 instance);
                                                                     }

                                                                     @Override
                                                                     public RouteBase newRoute(
                                                                             IVRPSolution<?> solution,
                                                                             Vehicle vehicle,
                                                                             Object... params) {
                                                                         return new ListRoute.ArrayListRoute(
                                                                                 solution, vehicle);
                                                                     }
                                                                 };

    public static final ISolutionFactory LINKED_LIST_SOL_FACTORY = new ISolutionFactory() {
                                                                     @Override
                                                                     public VRPSolution newSolution(
                                                                             IVRPInstance instance,
                                                                             Object... params) {
                                                                         return new VRPSolution(
                                                                                 instance);
                                                                     }

                                                                     @Override
                                                                     public RouteBase newRoute(
                                                                             IVRPSolution<?> solution,
                                                                             Vehicle vehicle,
                                                                             Object... params) {
                                                                         return new ListRoute.LinkedListRoute(
                                                                                 solution, vehicle);
                                                                     }
                                                                 };

    // This implmentation is not compatible with repeated depots
    // public static final ISolutionFactory DOUBLY_LINKED_ROUTE_SOL_FACTORY = new ISolutionFactory() {
    // @Override
    // public VRPSolution newSolution(
    // IVRPInstance instance,
    // Object... params) {
    // return new VRPSolution(
    // instance);
    // }
    //
    // @Override
    // public RouteBase newRoute(
    // IVRPSolution<?> solution,
    // Vehicle vehicle,
    // Object... params) {
    // return new DoublyLinkedRoute(
    // solution,
    // vehicle);
    // }
    // };
}
