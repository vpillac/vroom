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
import java.util.ListIterator;
import java.util.Map;

import vroom.common.heuristics.ConstraintHandler;
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
import vroom.optimization.online.jmsa.vrp.VRPRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;
import vroom.optimization.online.jmsa.vrp.VRPShrunkRequest;
import vroom.optimization.online.jmsa.vrp.optimization.MSAFixedNodeConstraint;

/**
 * The Class <code>VRPSDDetourRegretNew</code> is an implementation of the Regret algorithm that considers a subset of
 * candidate requests and estimate their regret value by evaluating the detour cost and load improvement on each
 * scenario.
 * <p>
 * <p>
 * Creation date: Dec 3, 2010 - 10:19:55 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDDetourRegretNew extends VRPSDSimpleRegret {

    public static int                                     sSampleSize  = Integer.MAX_VALUE;

    private final double                                  mCostEpsilon = 10 / 100;

    private double                                        mAlpha       = 1;

    private final VariableNeighborhoodSearch<VRPScenario> mVNS;

    private final VRPSDConsensus                          mConsensus;

    /**
     * Getter for <code>alpha</code>
     * 
     * @return the alpha
     */
    public double getAlpha() {
        return mAlpha;
    }

    /**
     * Setter for <code>alpha</code>
     * 
     * @param alpha
     *            the alpha to set
     */
    public void setAlpha(double alpha) {
        mAlpha = alpha;
    }

    /**
     * Getter for <code>vNS</code>
     * 
     * @return the vNS
     */
    public VariableNeighborhoodSearch<VRPScenario> getVNS() {
        return mVNS;
    }

    public VRPSDDetourRegretNew(ComponentManager<VRPScenario, ?> componentManager) {
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
        // CandidateList candidateList = selectCandidateRequestsKNearest();
        CandidateList candidateList = selectKLowerInsCost();
        ArrayList<VRPActualRequest> candidates = new ArrayList<VRPActualRequest>(candidateList.bestEvals.size());
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

        VRPActualRequest req = (VRPActualRequest) request;

        INodeVisit first = scenario.getRoute(0).getNodeAt(2);
        // If the request is already the first, keep the value unchanged
        if (first == null || first.getID() == request.getID() || first.isDepot() && req.isDepot()
                || !(scenario.getRoute(0).getNodeAt(1) instanceof VRPShrunkRequest)) {
            return currentValue;
        } else {
            VRPScenarioRoute route = scenario.getRoute(0);

            // The actual request with sampled demands of the scenario
            req = req.isDepot() ? req : scenario.getActualRequest(request.getID());

            // Depot
            VRPActualRequest depot = new VRPActualRequest(getInstance().getDepotsVisits().iterator().next());

            double insRelCost = 1 + evaluateInsertionCost(depot, route, req) / scenario.getCost();

            double loadDelta = 0;
            if (req.isDepot()) {
                loadDelta = getInstance().getShrunkRequest(0).getDemand() - route.getVehicle().getCapacity();
            } else if (!route.contains(req)) {
                loadDelta = req.getDemand();
            }
            double loadRatio = 1 + loadDelta / route.getVehicle().getCapacity();

            return (currentValue != 0 ? currentValue : 1) * (insRelCost / (Math.pow(loadRatio, getAlpha())));

        }
    }

    /**
     * Selects a subset of {@link #sSampleSize} requests that have the lower insertion cost
     * 
     * @return the corresponding candidate list
     */
    protected CandidateList selectKLowerInsCost() {
        CandidateList candidateList = new CandidateList();

        INodeVisit depot = getInstance().getDepotsVisits().iterator().next();

        new HashMap<Integer, Integer>(getInstance().getPendingRequests().size());

        double avgScenCost = 0;
        int count = 0;

        for (VRPScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
            avgScenCost += s.getCost();
            count++;
            for (VRPActualRequest n : getInstance().getPendingRequests()) {
                VRPScenarioRoute r = s.getRoute(0);

                candidateList.updateEval(n.getID(), -evaluateInsertionCost(depot, r, n));
            }
        }
        if (count > 0) {
            avgScenCost /= count;
        }

        // Filter out costly requests
        ListIterator<CandidateList.Eval> it = (ListIterator<vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDDetourRegretNew.CandidateList.Eval>) candidateList.bestEvals
                .iterator();
        while (it.hasNext()) {
            if (it.next().eval > avgScenCost * mCostEpsilon) {
                it.remove();
            }
        }

        return candidateList;
    }

    /**
     * Selects a subset of {@link #sSampleSize} candidate requests that appear first with the highest frequency
     * 
     * @return the corresponding candidate list
     */
    protected CandidateList selectKFirst() {

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

    protected double evaluateInsertionCost(INodeVisit depot, VRPScenarioRoute route, VRPRequest req) {
        double insCost = 0;
        if (route.length() > 2) {
            INodeVisit next = route.getNodeAt(2);
            INodeVisit cur = getInstance().getShrunkRequest(0);

            // Already the first request
            if (next.getID() == req.getID()) {
                insCost = 0;
            }
            // Insertion cost
            else {
                insCost = getInstance().getCost(cur, req) + getInstance().getCost(req, next)
                        - getInstance().getCost(cur, next);

            }
        }

        // A route failure will occur in this scenario
        if (!route.canAccommodateRequest(req)) {
            // Add the cost of a replenishment trip
            insCost += 2 * getInstance().getCost(depot, req);
        }
        return insCost;
    }

    @Override
    protected IDistinguishedSolution defaultDecision(ISolutionBuilderParam param) {
        return mConsensus.buildDistinguishedPlan(param);
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

        public void remove(int reqID) {
            if (evals.containsKey(reqID)) {
                bestEvals.remove(evals.get(reqID));
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
