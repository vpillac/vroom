/*
 * 
 */
package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.vns.VNSParameters;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.heuristics.vrp.SwapNeighborhood;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ISolutionBuilderParam;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;
import vroom.optimization.online.jmsa.vrp.VRPShrunkRequest;
import vroom.optimization.online.jmsa.vrp.optimization.MSAFixedNodeConstraint;

/**
 * <code>VRPSDSampledRegret</code> is an implementation of the Regret algorithm that only considers a subset of
 * candidate requests and estimate their regret value by performing a fast local search on each scenario.
 * <p>
 * Creation date: Aug 25, 2010 - 1:44:46 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDSampledRegret extends VRPSDSimpleRegret {

    public static int                                     sSampleSize = 5;

    private final VariableNeighborhoodSearch<VRPScenario> mVNS;

    private final VRPSDConsensus                          mConsensus;

    public VRPSDSampledRegret(ComponentManager<VRPScenario, ?> componentManager) {
        super(componentManager);
        mConsensus = new VRPSDConsensus(componentManager);

        List<INeighborhood<VRPScenario, ?>> neighborhoods = new LinkedList<INeighborhood<VRPScenario, ?>>();
        ConstraintHandler<VRPScenario> ctr = new ConstraintHandler<VRPScenario>();
        ctr.addConstraint(new CapacityConstraint<VRPScenario>());
        ctr.addConstraint(new MSAFixedNodeConstraint<VRPScenario>());
        neighborhoods.add(new SwapNeighborhood<VRPScenario>(ctr));
        neighborhoods.add(new TwoOptNeighborhood<VRPScenario>(ctr));
        mVNS = VariableNeighborhoodSearch.newVNS(VNSVariant.VND, OptimizationSense.MINIMIZATION, null, getMSAProxy()
                .getDecisionRandomStream(), neighborhoods);
    }

    @Override
    public IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam param) {
        // Special case when vehicle is at the depot, use consensus
        if (getInstance().getShrunkRequest(0).isDepot()) {
            return mConsensus.buildDistinguishedPlan(param);
        } else {
            return super.buildDistinguishedPlan(param);
        }
    }

    @Override
    protected Collection<? extends IActualRequest> selectCandidateRequests() {
        CandidateList candidateList = selectCandidateRequestsKNearest();
        ArrayList<VRPActualRequest> candidates = new ArrayList<VRPActualRequest>(sSampleSize);
        // Retreive the best candidates
        for (CandidateList.Eval e : candidateList.bestEvals) {
            candidates.add(getInstance().getNodeVisit(e.id));
        }

        // Add the depot
        // if (candidates.isEmpty()) {
        candidates.add(new VRPActualRequest(getInstance().getDepotsVisits().iterator().next()));
        // }
        return candidates;
    }

    protected CandidateList selectCandidateRequestsKNearest() {

        CandidateList candidateList = new CandidateList();

        INodeVisit depot = getInstance().getDepotsVisits().iterator().next();

        for (VRPActualRequest n : getInstance().getPendingRequests()) {
            for (VRPScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
                double insCost = 0;

                VRPScenarioRoute r = s.getRoute(0);

                if (r.length() > 2) {
                    INodeVisit next = r.getNodeAt(2);
                    INodeVisit cur = getInstance().getShrunkRequest(0);

                    if (next.getID() == n.getID()) {
                        insCost = 0;
                    } else {
                        insCost = getInstance().getCost(cur, n) + getInstance().getCost(n, next)
                                - getInstance().getCost(cur, next);

                    }
                }

                if (!r.canAccommodateRequest(n)) {
                    insCost += getInstance().getCost(depot, n);
                }

                candidateList.updateEval(n.getID(), -insCost);
            }
        }

        return candidateList;
    }

    /**
     * Selects a subset of {@link #sSampleSize} candidate requests that are the most frequently present in the first
     * {@link #sSampleSize} nodes of any scenario.
     */
    protected CandidateList selectCandidateRequestsKFirsts() {

        CandidateList candidateList = new CandidateList();

        // Check whether the vehicle is currently at the depot
        boolean atDepot = getInstance().getShrunkRequest(0).getNodeVisit().isDepot();
        double resCap = getInstance().getFleet().getVehicle().getCapacity()
                - getInstance().getShrunkRequest(0).getDemand();

        // Iterate over all scenarios
        for (VRPScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
            // Number of candidates examined
            int count = 0;
            // Current route
            int route = 0;
            // Iterate over all routes until sSampleSize nodes were examined
            while (route < s.getRouteCount()) {
                count = 0;

                s.acquireLock();
                Iterator<VRPActualRequest> it = Utilities.castIterator(s.getOrderedActualRequests(route).iterator());
                s.releaseLock();

                // Safe-remove the first node (depot)
                if (!it.next().isDepot()) {
                    s.acquireLock();
                    it = Utilities.castIterator(s.getOrderedActualRequests(route).iterator());
                    s.releaseLock();
                }

                // Iterate over the route nodes
                while (it.hasNext() && count < sSampleSize) {
                    VRPActualRequest req = it.next();

                    // Do not consider depot if already at the depot
                    // or requests violating the remaining capacity
                    if (req.isDepot() && atDepot || req.getDemand() > resCap) {
                        break;
                    }

                    candidateList.updateEval(req.getID(), 1);

                    count++;
                }
                route++;
            }
        }

        return candidateList;
    }

    @Override
    protected IDistinguishedSolution defaultDecision(ISolutionBuilderParam param) {
        return mConsensus.buildDistinguishedPlan(param);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.RegretSolutionBuilderBase# evaluateRegret(vroom.optimization.online.jmsa.IActualRequest,
     * vroom.optimization.online.jmsa.IScenario, double)
     */
    @Override
    protected double evaluateRegret(IActualRequest request, VRPScenario scenario, double currentValue) {
        // Ignore empty scenarios or invalid requests
        if (scenario.getRouteCount() <= 0 || request == null) {
            return currentValue;
        }
        // If the request is already the first, keep the value unchanged
        INodeVisit first = scenario.getRoute(0).getNodeAt(2);
        VRPActualRequest req = (VRPActualRequest) request;
        if (first == null || first.getID() == request.getID() || first.isDepot() && req.isDepot()
                || !(scenario.getRoute(0).getNodeAt(1) instanceof VRPShrunkRequest)) {
            return currentValue;
        }

        // Depot
        VRPActualRequest depot = new VRPActualRequest(getInstance().getDepotsVisits().iterator().next());

        // The actual request with sampled demands of the scenario
        req = req.isDepot() ? req : scenario.getActualRequest(request.getID());

        // Clone the scenario
        VRPScenario tmp = scenario.clone();
        VRPScenarioRoute firstRoute = tmp.getRoute(0);

        double insCost;
        double cap = firstRoute.getVehicle().getCapacity() - tmp.getRoute(0).getLoad();
        int len = firstRoute.length();
        int last = len - 1;
        double delta = 0;
        boolean prevFixState = req.isFixed();
        if (!req.isDepot()) {
            // Insertion cost
            insCost = -scenario.getCost();
            // Remove the request from its current route
            for (VRPScenarioRoute r : tmp) {
                if (r.remove(req)) {
                    break;
                }
            }
            // Insert the request at after the shrunk node
            firstRoute.insertNode(2, req);
            req.fix(); // Fix the node for the VNS
            insCost += tmp.getCost();

            // Repair solution
            // Try to remove nodes
            while (cap < 0 && last > 4) {
                last--;
                cap += firstRoute.getNodeAt(last).getDemand();
            }
        } else {
            // Special case for replenishment trip
            // remove the shrunk node from the route
            VRPShrunkRequest shrunk = (VRPShrunkRequest) firstRoute.extractNode(1);

            delta = 2 * getInstance().getCost(depot, shrunk);
            insCost = 0;
        }

        if (cap >= 0) {
            // Feasibility can be repaired
            if (last < len - 1) {
                // Remove nodes to ensure feasibility
                VRPScenarioRoute newRoute = firstRoute.extractSubroute(last, len - 2);
                newRoute.insertNode(0, depot);
                newRoute.appendNode(depot);
                // Add a new route
                tmp.addRoute(newRoute);
            }

            // Reoptimize the resulting scenario
            tmp = mVNS.localSearch(getInstance(), tmp, new VNSParameters(200, 100, true, 50, 10, null, null));

            delta += tmp.getCost() - scenario.getCost();
        } else {
            // Route will necessarily fail
            // Estimate the failure cost as the insertion cost of req in first
            // position
            // plus twice the distance from the depot to req
            delta = insCost + 2 * getInstance().getCost(depot, req);
        }

        if (prevFixState) {
            req.fix();
        } else {
            req.free();
        }

        return currentValue + delta;
    }

    @Override
    protected MSAVRPInstance getInstance() {
        return (MSAVRPInstance) getComponentManager().getParentMSAProxy().getInstance();
    }

    public static class CandidateList {

        final Map<Integer, Eval> evals     = new HashMap<Integer, Eval>();

        final LinkedList<Eval>   bestEvals = new LinkedList<Eval>();

        public void updateEval(int reqID, double deltaEval) {
            // Update the request evaluation
            Eval e;
            if (!evals.containsKey(reqID)) {
                e = new Eval(reqID, deltaEval);
                evals.put(reqID, e);
            } else {
                e = evals.get(reqID);
                e.eval += deltaEval;
            }

            if (!bestEvals.contains(e)) {
                bestEvals.add(e);
                Collections.sort(bestEvals);
            }

            // Remove exceeding candidates
            while (bestEvals.size() > sSampleSize) {
                bestEvals.removeLast();
            }
        }

        private static class Eval implements Comparable<Eval> {
            int    id;
            double eval;

            public Eval(int id, double eval) {
                super();
                this.id = id;
                this.eval = eval;
            }

            @Override
            public int hashCode() {
                return id;
            }

            @Override
            public int compareTo(Eval o) {
                return Double.compare(this.eval, o.eval);
            }

            @Override
            public String toString() {
                return String.format("%s:%s", id, eval);
            }
        }

        @Override
        public String toString() {
            return bestEvals.toString();
        }
    }

}
