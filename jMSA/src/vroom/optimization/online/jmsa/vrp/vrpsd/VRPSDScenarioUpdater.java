/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.NodeInsertion;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.MSAVRPSolutionFactory;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPParameterKeys;
import vroom.optimization.online.jmsa.vrp.VRPRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute.State;
import vroom.optimization.online.jmsa.vrp.VRPScenarioUpdaterBase;
import vroom.optimization.online.jmsa.vrp.VRPShrunkRequest;
import vroom.optimization.online.jmsa.vrp.optimization.MSAFixedNodeConstraint;

/**
 * <code>VRPSDScenarioUpdater</code> is a specialization of {@link VRPScenarioUpdaterBase} for the VRPSD problem.
 * <p>
 * Creation date: May 7, 2010 - 11:46:35 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDScenarioUpdater extends VRPScenarioUpdaterBase {

    private final MSAVRPSolutionFactory           mSolutionFactory;

    private final TwoOptNeighborhood<VRPScenario> mTwoOptNeigh;

    /**
     * Creates a new <code>VRPSDScenarioUpdater</code>
     * 
     * @param componentManager
     */
    public VRPSDScenarioUpdater(ComponentManager<?, ?> componentManager) {
        super(componentManager);

        mSolutionFactory = getComponentManager().getParentMSAProxy().getParameters()
                .newInstance(VRPParameterKeys.SCENARIO_FACTORY_CLASS);

        ConstraintHandler<VRPScenario> constraintHandler = new ConstraintHandler<VRPScenario>();
        constraintHandler.addConstraint(new MSAFixedNodeConstraint<VRPScenario>());
        constraintHandler.addConstraint(new CapacityConstraint<VRPScenario>());
        mTwoOptNeigh = new TwoOptNeighborhood<VRPScenario>(constraintHandler);
    }

    @Override
    public boolean startOfServiceUpdate(IScenario scenario, int resourceId, IActualRequest request) {
        VRPScenario scen = (VRPScenario) scenario;

        boolean b = repairCapacity(scen);

        SolutionChecker.checkSolution(scen, true, true, true);

        return b;
    }

    @Override
    public boolean endOfServiceUpdate(IScenario scenario, int resourceId, IActualRequest servedRequest) {

        boolean r = false;

        VRPScenario s = (VRPScenario) scenario;

        s.acquireLock();

        // Check that the last fixed request is the good one
        if (s.getLastFixedRequest(resourceId) == null
        // Newly generated scenarios are assumed to be coherent with the system
        // state
                || s.getLastFixedRequest(resourceId).getID() == servedRequest.getID()) {
            // Mark the last request as served
            s.markLastVisitAsServed(resourceId);
            r = true;
        }

        s.releaseLock();

        return r;
    }

    @Override
    public boolean startServicingUpdate(IScenario scenario, int resourceId) {
        VRPScenario scen = (VRPScenario) scenario;

        scen.acquireLock();

        if (!(scen.getRoute(resourceId).getNodeAt(1) instanceof VRPShrunkRequest)) {
            repairSolution(scen);
        }

        boolean b = false;
        if (scen.getRoute(resourceId).getFirstNode().isDepot()) {
            // Fix the first request (depot)
            // scen.fixFirstActualRequest(resourceId);
            // Mark the first request as served as the vehicle left the
            // depot
            scen.markLastVisitAsServed(resourceId);
            scen.getRoute(resourceId).setCurrentState(State.STARTED);

            b = true;
        }

        scenario.releaseLock();
        return b;
    }

    @Override
    public boolean stopServicingUpdate(IScenario scenario, int resourceId) {
        if (scenario instanceof VRPScenario) {

            scenario.acquireLock();
            boolean b = ((VRPScenario) scenario).getRoute(resourceId).length() == 0
                    || ((VRPScenario) scenario).getRoute(resourceId).length() == 1
                    && ((VRPScenario) scenario).getRoute(resourceId).getFirstNode().isDepot();
            scenario.releaseLock();
            return b;
        } else {
            return false;
        }
    }

    @Override
    public boolean enforceDecision(IScenario scenario, IActualRequest request, int resourceId) {
        boolean b = false;

        scenario.acquireLock();
        VRPScenario scen = (VRPScenario) scenario;
        VRPScenarioRoute firstRoute = scen.getRoute(0);
        VRPActualRequest req = (VRPActualRequest) request;
        VRPShrunkRequest shrunk = getInstance().getShrunkRequest(0);

        if (!(firstRoute.getNodeAt(1) instanceof VRPShrunkRequest)) {
            if (!repairSolution(scen)) {
                MSALogging
                        .getComponentsLogger()
                        .warn("VRPSDScenarioUpdater.enforceDecision: Incoherent scenario detected while enforcing request %s: %s",
                                request, scen);
                return false;
            } else {
                firstRoute = scen.getRoute(0);
            }
        }

        if (req.isDepot()) {
            // Remove first route when request is a depot
            // reinsert remaining requests in other routes or remove scenario
            List<VRPActualRequest> actualRequests = firstRoute.getOrderedActualRequests();
            for (VRPActualRequest r : actualRequests) {
                if (!r.isDepot()) {
                    int bestRoute = -1;
                    NodeInsertion ins = null, bestIns = null;

                    // Find the best insertion
                    for (int rte = 1; rte < scen.getRouteCount(); rte++) {
                        ins = scen.getRoute(rte).getBestRequestInsertion(r);
                        if (ins != null && (bestIns == null || ins.getCost() < bestIns.getCost())) {
                            bestIns = ins;
                            bestRoute = rte;
                        }
                    }

                    if (bestIns != null) {
                        scen.getRoute(bestRoute).insertNode(bestIns, r);
                    } else {
                        // The request cannot be reinserted, discard this
                        // scenario
                        return false;
                    }
                }
            }
            scen.removeRoute(firstRoute);
            if (scen.getRouteCount() > 0 && !scen.getRoute(0).containsShrunkNode()) {
                b = scen.getRoute(0).insertNode(1, scen.getParentInstance().getShrunkRequest(resourceId));
            }

        } else if (firstRoute.getFirstActualRequest() == null) {
            // There is no first actual request (empty route)
            if (request == null) {
                return true;
            } else {
                return false;
            }
        }
        // The first request is already the good one
        else if (firstRoute.getFirstActualRequest().getID() == request.getID() && firstRoute.containsShrunkNode()) {
            if (!firstRoute.isLastFixedRequestServed()) {
                firstRoute.markLastRequestAsServed();
            }
            b = req.getID() == scen.fixFirstActualRequest(resourceId).getID();
        }
        // The last served request is a depot
        // Find if the request is the first or last of any route
        else if (shrunk.size() > 1
                && shrunk.getShrunkNodes().get(getInstance().getShrunkRequest(0).size() - 2).isDepot()) {
            Iterator<VRPScenarioRoute> it = scen.iterator();
            boolean found = false;
            VRPScenarioRoute reqRoute = null;
            VRPScenarioRoute shrunkRoute = null;
            int i = 0;
            int routeIdx = -1;
            while (it.hasNext() && (!found || shrunkRoute == null)) {
                VRPScenarioRoute route = it.next();

                if (route.containsShrunkNode()) {
                    shrunkRoute = route;
                }

                if (!found && route.length() > 2 && (!route.containsShrunkNode() || route.length() > 3)) {
                    int first = route.containsShrunkNode() ? 2 : 1;
                    VRPRequest firstReq = route.getNodeAt(first);
                    VRPRequest lastReq = route.getNodeAt(route.length() - 2);

                    found = firstReq.getID() == request.getID();

                    if (found) {
                        reqRoute = route;
                    }

                    if (!found && lastReq.getID() == request.getID()) {
                        reqRoute = route;
                        reqRoute.reverseRoute();
                        found = true;
                        routeIdx = i;
                    }

                    // Temporarily remove the route from the scenario
                    if (found && routeIdx != 0) {
                        it.remove();
                    }
                }
                i++;
            }

            if (found) {
                if (routeIdx != 0) {
                    scen.addRoute(reqRoute, 0);
                    firstRoute = reqRoute;
                }
                if (shrunkRoute != reqRoute || !(shrunkRoute.getNodeAt(1) instanceof VRPShrunkRequest)) {
                    shrunkRoute.remove(shrunk);
                    if (shrunkRoute.length() < 3) {
                        scen.removeRoute(shrunkRoute);
                    }
                    firstRoute.insertNode(1, shrunk);

                    // Repair the scenario if route capacity is exceeded
                    b = repairCapacity(scen);
                }

                if (!firstRoute.isLastFixedRequestServed()) {
                    firstRoute.markLastRequestAsServed();
                }
                b &= req.getID() == scen.fixFirstActualRequest(resourceId).getID();
            } else {
                b = false;
            }
        }
        // Attempt to repair the scenario
        else {
            VRPScenarioRoute reqRoute = null;
            int reqIndex = -1;

            // Find the route in which the request appears
            for (VRPScenarioRoute route : scen) {
                int idx = route.getNodePosition(req);
                if (idx >= 0) {
                    reqIndex = idx;
                    reqRoute = route;
                    break;
                }
            }

            if (reqRoute != null) {
                reqRoute.extractNode(reqIndex);
                firstRoute.insertNode(2, req);

                if (!firstRoute.isLastFixedRequestServed()) {
                    firstRoute.markLastRequestAsServed();
                }
                b = req.getID() == scen.fixFirstActualRequest(resourceId).getID();
                if (b) {
                    // Fast reopt
                    VRPScenario newScen = mTwoOptNeigh.localSearch(getInstance(), scen, new SimpleParameters(
                            LSStrategy.DET_BEST_IMPROVEMENT, 200, 200, getMSAProxy().getOptimizationRandomStream()));
                    b = repairSolution(newScen);
                    if (b) {
                        scen.clear();
                        scen.importScenario(newScen);
                        firstRoute = scen.getRoute(0);
                    }
                }
            } else {
                b = false;
            }
        }

        SolutionChecker.checkSolution(scen, true, true, true);

        scenario.releaseLock();

        return b;
        // Breakpoint condition
        // !b
        // || scen.getRoute(0) != firstRoute
        // || firstRoute.getFirstActualRequest().getID() == req.getID()
        // || firstRoute.isLastFixedRequestServed()
        // || !firstRoute.containsShrunkNode()
        // || firstRoute.getLastFixedRequest() == null
        // || firstRoute.getLastFixedRequest().getID() != req.getID()
    }

    /**
     * Repair a scenario by finding all routes exceeding vehicle capacity and reinserting nodes in other existing or new
     * routes
     * 
     * @param scenario
     *            the scenario to be repaired
     * @return <code>true</code> if the scenario was successfully repaired
     */
    public boolean repairCapacity(VRPScenario scenario) {
        // Find routes that violate capacity and remove exceeding nodes
        LinkedList<VRPRequest> pendingRequests = new LinkedList<VRPRequest>();

        for (VRPScenarioRoute r : scenario) {
            r.calculateLoad(true);
            for (int p = 0; p < r.getVehicle().getCompartmentCount(); p++) {
                int end = r.length() - 2;
                int start = r.length();
                double load = r.getLoad(p);
                ListIterator<VRPRequest> it = r.iterator();
                while (load > r.getVehicle().getCapacity(p) && it.hasPrevious()) {
                    load -= it.previous().getDemand(p);
                    start--;
                }
                if (start <= end) {
                    pendingRequests.addAll(r.extractNodes(start, end));
                }
            }
        }

        if (pendingRequests.isEmpty()) {
            return true;
        }

        Iterator<VRPRequest> it = pendingRequests.iterator();
        // Attempt to reinsert requests in existing routes of the scenario
        while (it.hasNext()) {
            if (insertRequest(scenario, it.next())) {
                it.remove();
            }
        }

        boolean error = false;
        while (!pendingRequests.isEmpty() && !error) {
            it = pendingRequests.iterator();
            // Create a new route
            VRPScenarioRoute r = mSolutionFactory.newRoute(scenario, getInstance().getFleet().getVehicle());
            // Add remaining requests to a new route
            error = true;
            while (it.hasNext()) {
                VRPRequest req = it.next();
                if (r.canAccommodateRequest(req)) {
                    r.bestInsertion(req);
                    it.remove();
                    error = false; // Prevent infinite loop
                }
            }
            // Add route to scenario
            scenario.addRoute(r);
        }

        return pendingRequests.isEmpty();
    }

    /**
     * Reorganize the scenario so that the route containing the shrunk node is in first position, and the shrunk node
     * appears in first position in the route itself.
     * <p>
     * Additionally this method tries to improve consensus by reordering routes
     * </p>
     * 
     * @param scenario
     *            the scenario to be repaired
     * @return <code>true</code> if the scenario is coherent
     */
    public static boolean repairSolution(VRPScenario scenario) {
        if (scenario == null) {
            return false;
        }

        scenario.acquireLock();

        VRPShrunkRequest shReq = scenario.getParentInstance().getShrunkRequest(0);
        if (scenario.getRouteCount() == 0 || shReq.getParentRequest() == null) {
            return true;
        }

        boolean b = true;
        boolean shrunkNodeFound = false;
        List<VRPScenarioRoute> removedRoutes = new LinkedList<VRPScenarioRoute>();
        for (int r = 0; r < scenario.getRouteCount(); r++) {
            VRPScenarioRoute route = scenario.getRoute(r);
            if (route.length() <= 2) {
                removedRoutes.add(route);
            } else if (route.containsShrunkNode()) {
                if (shrunkNodeFound) {
                    // The mSolution already contains a shrunk node
                    return false;
                } else {
                    shrunkNodeFound = true;
                }

                if (r != 0) {
                    // Put the route in first position
                    scenario.removeRoute(route);
                    scenario.addRoute(route, 0);
                }

                b &= route.ensureRouteSequence();
                if (!b) {
                    return false;
                }
            }
        }

        for (VRPScenarioRoute route : removedRoutes) {
            scenario.removeRoute(route);
        }

        // Try to improve consensus by re-ordering route when the vehicle is at
        // a depot
        if (b && (shReq.isDepot() || shReq.getParentRequest() == null)) {
            List<VRPScenarioRoute> routes = new LinkedList<VRPScenarioRoute>();

            for (VRPScenarioRoute route : scenario) {
                // Empty route
                if (route.containsShrunkNode() && route.length() > 3 || !route.containsShrunkNode()
                        && route.length() > 2) {
                    if (route.containsShrunkNode()) {
                        route.extractNode(1);
                    }

                    reorderRoute(route);
                    routes.add(route);
                }
            }

            Collections.sort(routes, new Comparator<VRPScenarioRoute>() {
                @Override
                public int compare(VRPScenarioRoute o1, VRPScenarioRoute o2) {
                    MSAVRPInstance ins = o1.getParentSolution().getParentInstance();
                    ins.getDepotsVisits().iterator().next();
                    // return o1.getNodeAt(1).getID() - o2.getNodeAt(1).getID();
                    // return (int) Math.round((ins.getCost(depot,
                    // o2.getNodeAt(1)) - ins.getCost(
                    // depot, o1.getNodeAt(1))) * 10000);
                    return (int) Math.round((o2.getLoad() - o1.getLoad()) * 1000);
                }
            });

            if (!routes.isEmpty()) {
                routes.get(0).insertNode(1, shReq);
            }

            scenario.clear();

            for (VRPScenarioRoute route : routes) {
                scenario.addRoute(route);
            }
        } else {

        }

        scenario.releaseLock();

        return b && shrunkNodeFound;
    }

    /**
     * Reorder the route: will ensure that the first node has a lower id than the last.
     * 
     * @param route
     */
    public static void reorderRoute(VRPScenarioRoute route) {
        int first = route.containsShrunkNode() ? 2 : 1;

        if (route.length() > 2 + first) {
            // if (route.getNodeAt(first).getID() >
            // route.getNodeAt(route.length() - 2).getID()) {
            MSAVRPInstance instance = route.getParentSolution().getParentInstance();
            INodeVisit dep = instance.getDepotsVisits().iterator().next();
            if (instance.getCost(dep, route.getNodeAt(route.length() - 2)) > instance.getCost(dep,
                    route.getNodeAt(first))) {
                if (route.containsShrunkNode()) {
                    route.reverseSubRoute(2, route.length() - 2);
                } else {
                    route.reverseRoute();
                }
            }
        }
    }
}
