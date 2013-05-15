package vroom.optimization.online.jmsa.benchmarking;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;

public class SolutionChecker {

    public boolean checkSolution(IVRPSolution<? extends IRoute<?>> solution) {
        boolean b = true;

        // Route feasibility
        for (IRoute<?> route : solution) {
            b &= checkRoute(route);
            if (!b) {
                return false;
            }
        }

        return b;
    }

    public boolean checkRoute(IRoute<?> route) {

        double[] load = new double[route.getVehicle().getCompartmentCount()];
        for (INodeVisit node : route) {
            for (int p = 0; p < load.length; p++) {
                if (node.isDepot()) {
                    load[p] = 0;
                } else {
                    load[p] += node.getDemand(p);
                }

                if (load[p] > route.getVehicle().getCapacity(p)) {
                    return false;
                }
            }
        }

        return true;

    }

}
