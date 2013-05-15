/**
 * 
 */
package vroom.common.modeling.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.utilities.ILockable;
import vroom.common.utilities.Utilities;

/**
 * <code>SolutionChecker</code> is a class which function is to check if a solution routes internal costs and loads are
 * consistent with the visited nodes
 * <p>
 * Creation date: Jun 29, 2010 - 4:16:32 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SolutionChecker {

    /** A tolerance for the zero value */
    public static double ZERO_TOLERANCE = 10E-6;

    /**
     * Checking of a route
     * 
     * @param route
     *            the route to be checked
     * @param autoRepair
     *            <code>true</code> if the route should be automatically repaired
     * @param startAtDeopt
     *            <code>true</code> if the routes should start at the depot
     * @param endAtDepot
     *            <code>true</code> if the routes should end at the depot
     * @return a string describing the incoherences in the route internal cost and load, or <code>null</code> if the
     *         internal data is coherent.
     * @see IRoute#calculateCost(boolean)
     * @see IRoute#calculateLoad(boolean)
     */
    public static <N extends INodeVisit, R extends IRoute<N>> String checkRoute(R route,
            boolean autoRepair, boolean startAtDepot, boolean endAtDepot) {
        StringBuilder sb = new StringBuilder();

        boolean error = false;
        if (route.length() <= 0) {
            sb.append("Route is empty");
            error = true;
        } else {
            if (startAtDepot && !route.getNodeAt(0).isDepot()) {
                sb.append("Route does not start at the depot");
                error = true;
            }
            if (endAtDepot && !route.getNodeAt(route.length() - 1).isDepot()) {
                sb.append("Route does not end at the depot");
                error = true;
            }
        }
        if (error) {
            sb.append(" (");
            sb.append(route.toString());
            sb.append(")");
        }

        double cost = calculateCost(route);
        double[] loads = calculateLoads(route);

        if (Math.abs(cost - route.getCost()) > ZERO_TOLERANCE) {
            sb.append(String.format("Route %s: Inconsistent cost (expected:%.3f, stored:%.3f))",
                    route.hashCode(), cost, route.getCost()));
            if (autoRepair) {
                route.updateCost(-route.getCost() + cost);
            }
        }

        if (route instanceof ILockable) {
            ((ILockable) route).acquireLock();
        }
        for (int p = 0; p < loads.length; p++) {
            if (Math.abs(loads[p] - route.getLoad(p)) > ZERO_TOLERANCE) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(String.format(
                        "Route %s: Inconsistent load (prod:%s, expected:%.3f, stored:%.3f))",
                        route.hashCode(), p, loads[p], route.getLoad(p)));
                if (autoRepair) {
                    route.updateLoad(p, -route.getLoad(p) + loads[p]);
                }
            }
        }

        if (route instanceof ILockable) {
            ((ILockable) route).releaseLock();
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Calculates the load of the route
     * 
     * @param route
     * @return an array containing the aggregated load for each product
     */
    public static double[] calculateLoads(IRoute<?> route) {
        double[] loads = new double[route.getVehicle().getCompartmentCount()];

        if (route instanceof ILockable) {
            ((ILockable) route).acquireLock();
        }

        Iterator<INodeVisit> it = Utilities.castIterator(route.iterator());
        if (it.hasNext()) {
            INodeVisit pred = it.next();

            for (int p = 0; p < loads.length; p++) {
                loads[p] += pred.getDemand(p);
            }
            INodeVisit succ = null;
            while (it.hasNext()) {
                succ = it.next();

                for (int p = 0; p < loads.length; p++) {
                    loads[p] += succ.getDemand(p);
                }

                pred = succ;
            }
        }

        if (route instanceof ILockable) {
            ((ILockable) route).releaseLock();
        }

        return loads;
    }

    /**
     * Calculate the cost of a route
     * 
     * @param route
     *            the route which cost has to be calculated
     * @return the total cost of the given route
     */
    public static double calculateCost(IRoute<?> route) {
        double cost = 0;

        if (route instanceof ILockable) {
            ((ILockable) route).acquireLock();
        }

        Iterator<INodeVisit> it = Utilities.castIterator(route.iterator());
        if (it.hasNext()) {
            INodeVisit pred = it.next();

            INodeVisit succ = null;
            while (it.hasNext()) {
                succ = it.next();

                cost += route.getParentSolution().getParentInstance().getCostDelegate()
                        .getCost(pred, succ, route.getVehicle());

                pred = succ;
            }
        }

        if (route instanceof ILockable) {
            ((ILockable) route).releaseLock();
        }

        return cost;
    }

    /**
     * Calculate the cost of a solution
     * 
     * @param solution
     *            the solution which cost has to be calculated
     * @return the total cost of the given solution
     */
    public static double calculateCost(IVRPSolution<?> solution) {
        double cost = 0;

        for (IRoute<?> route : solution) {
            cost += calculateCost(route);
        }

        return cost;
    }

    /**
     * Check a solution for missing customers
     * 
     * @param solution
     *            the solution to be checked
     * @return a collection containing the {@link INodeVisit} present in the instance but not served by any route
     * @see IVRPInstance#getNodeVisits()
     */
    public static Collection<INodeVisit> checkUnservedCustomers(IVRPSolution<?> solution) {
        HashMap<Integer, INodeVisit> missingCustomers = new HashMap<Integer, INodeVisit>();
        for (INodeVisit visit : solution.getParentInstance().getNodeVisits()) {
            missingCustomers.put(visit.getID(), visit);
        }

        for (IRoute<?> route : solution) {
            for (INodeVisit n : route) {
                missingCustomers.remove(n.getID());
            }
        }

        return missingCustomers.values();
    }

    /**
     * Checking of a route
     * 
     * @param solution
     *            the solution to be checked
     * @param autorepair
     *            <code>true</code> if the solution should be automatically repaired
     * @param startAtDeopt
     *            <code>true</code> if the routes should start at the depot
     * @param endAtDepot
     *            <code>true</code> if the routes should end at the depot
     * @return a string describing the incoherences in the solution's routes internal cost and load, or
     *         <code>null</code> if the internal data is coherent.
     * @see #checkRoute(IRoute, boolean, boolean, boolean)
     */
    public static String checkSolution(IVRPSolution<?> solution, boolean autorepair,
            boolean startAtDeopt, boolean endAtDepot) {
        StringBuilder sb = new StringBuilder();

        for (IRoute<?> r : solution) {
            String checkR = checkRoute(r, autorepair, startAtDeopt, endAtDepot);
            if (checkR != null) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append('[');
                sb.append(checkR);
                sb.append(']');
            }
        }

        Collection<INodeVisit> unservedVisits = checkUnservedCustomers(solution);
        if (!unservedVisits.isEmpty()) {
            sb.append("Unserved customers:{");
            for (INodeVisit n : unservedVisits) {
                sb.append(n);
                sb.append(',');
            }
            sb.setCharAt(sb.length() - 1, '}');
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    public static void removeEmptyRoutes(IVRPSolution<?> solution) {
        Iterator<?> routes = solution.iterator();
        while (routes.hasNext()) {
            IRoute<?> r = (IRoute<?>) routes.next();
            if (r.length() == 0
                    || (r.length() <= 2 && r.getFirstNode().isDepot() && r.getLastNode().isDepot())) {
                routes.remove();
            }
        }
    }
}
