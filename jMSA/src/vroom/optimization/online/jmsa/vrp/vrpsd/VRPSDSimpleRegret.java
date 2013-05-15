/*
 * 
 */
package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import umontreal.iro.lecuyer.probdist.Distribution;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.attributes.IStochasticDemand;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.optimization.online.jmsa.DistinguishedSolutionBase;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ISolutionBuilderParam;
import vroom.optimization.online.jmsa.components.RegretSolutionBuilderBase;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPRequest;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;

/**
 * The Class <code>VRPSimpleRegret</code> is an implementation of the regret algorithm as proposed in Van Hentenryck and Bent (Online Stochastic
 * Combinatorial Optimization, 2006).
 * <p>
 * For each pending requests <code>r</code> it calculates the sum of the <code>f(r,s)</code> over all the scenarios <code>s</code>, where
 * <code>f(r,s)</code> is equal to <code>0</code> if there exists a feasible insertion/swap allowing the request <code>r</code> to be served first in
 * scenario <code>r</code>.
 * </p>
 * <p>
 * This implementation is for the single vehicle dynamic VRPSD
 * </p>
 * <p>
 * Creation date: Aug 24, 2010 - 11:43:29 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDSimpleRegret extends RegretSolutionBuilderBase<VRPScenario> {

    private double mInsertionCostUB;

    public VRPSDSimpleRegret(ComponentManager<VRPScenario, ?> componentManager) {
        super(componentManager);

        double maxCost1 = 0, maxCost2 = 0, minCost = Double.POSITIVE_INFINITY;
        Set<INodeVisit> reqs = getInstance().getNodeVisits();
        reqs.addAll(getInstance().getDepotsVisits());

        boolean s = getInstance().isSymmetric();

        if (s) {
            Iterator<INodeVisit> it = reqs.iterator();
            while (!reqs.isEmpty()) {
                INodeVisit i = it.next();
                it.remove();

                for (INodeVisit j : reqs) {
                    double c = getInstance().getCost(i, j, getInstance().getFleet().getVehicle());
                    if (c > maxCost1) {
                        maxCost2 = maxCost1;
                        maxCost1 = c;
                    } else if (c > maxCost2) {
                        maxCost2 = c;
                    }
                    if (c < minCost) {
                        minCost = c;
                    }
                }
            }
            mInsertionCostUB = maxCost1 + maxCost2 - minCost;
        } else {
            throw new UnsupportedOperationException("Asymmetric instances are not supported");
        }
    }

    @Override
    protected Collection<? extends IActualRequest> selectCandidateRequests() {
        Iterable<? extends IActualRequest> reqs = getComponentManager().getParentMSAProxy()
            .getInstance()
            .getPendingRequests();
        LinkedList<VRPActualRequest> candidates = new LinkedList<VRPActualRequest>();

        Set<INodeVisit> depotsSet = getInstance().getDepotsVisits();
        LinkedList<VRPActualRequest> depots = new LinkedList<VRPActualRequest>();
        for (INodeVisit d : depotsSet) {
            depots.add(new VRPActualRequest(d));
        }
        double cap = getInstance().getFleet().getVehicle().getCapacity() - getInstance().getCurrentLoad(0, 0);
        if (cap <= 0) {
            return depots;
        }

        for (IActualRequest r : reqs) {
            VRPActualRequest ar = (VRPActualRequest) r;
            Distribution dist = ((IStochasticDemand) ar.getParentRequest().getAttribute(
                RequestAttributeKey.DEMAND)).getDistribution(0);
            if (dist.cdf(cap) > 0.9) {
                candidates.add(ar);
            }
        }

        return candidates.isEmpty() ? depots : candidates;
    }

    protected MSAVRPInstance getInstance() {
        return (MSAVRPInstance) getComponentManager().getParentMSAProxy().getInstance();
    }

    @Override
    protected IDistinguishedSolution defaultDecision(ISolutionBuilderParam param) {
        return new DistinguishedSolutionBase(new VRPActualRequest(getInstance().getDepotsVisits()
            .iterator()
            .next()));
    }

    @Override
    protected double evaluateRegret(IActualRequest request, VRPScenario s, double currentValue) {
        // Ignore empty scenarios
        if (s.getRouteCount() <= 0) {
            return currentValue;
        }

        VRPScenarioRoute currentRoute = s.getRoute(0);
        VRPActualRequest req = (VRPActualRequest) request;
        VRPActualRequest firstReq = currentRoute.getFirstActualRequest();

        if (firstReq == null || req == null) {
            return currentValue;
        }

        double regret = 0;

        double cap = getInstance().getFleet().getVehicle().getCapacity() - getInstance().getCurrentLoad(0, 0);

        // r is already the first request
        if (firstReq.getID() == request.getID()) {
            regret = 0;
        }
        // r can be visited in the route without violating the
        // vehicle capacity, regret is the normalized insertion cost
        else if (req.getDemand() <= cap) {
            regret = getInstance().getCostDelegate().getInsertionCost(req, getInstance().getShrunkRequest(0),
                firstReq, getInstance().getFleet().getVehicle())
                    / mInsertionCostUB;
        } else {
            cap -= req.getDemand();
            for (VRPRequest r : currentRoute.getNodeSequence()) {
                if (cap + r.getDemand() >= 0) {
                    // r can be visited in the route if one request is removed
                    // regret is the normalized insertion cost + 1
                    regret = getInstance().getCostDelegate().getInsertionCost(req,
                        getInstance().getShrunkRequest(0), firstReq, getInstance().getFleet().getVehicle())
                            / mInsertionCostUB + 1;
                    break;
                }
            }

            // r cannot be visited in the route if only one request can be removed
            // there is no guarantee that r can be served in the route
            if (regret == 0) {
                regret = 2;
            }
        }

        return currentValue + regret;
    }

}
