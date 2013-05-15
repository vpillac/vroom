/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import umontreal.iro.lecuyer.probdist.Distribution;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.attributes.IDemand;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.StochasticDemand;
import vroom.optimization.online.jmsa.DistinguishedSolutionBase;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ISolutionBuilderParam;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;

/**
 * <code>VRPSDSmartConsensus</code> is an improvement of {@link VRPSDConsensus} that considers alternative requests
 * before before returning the customer selected by consensus.
 * <p>
 * Creation date: Oct 4, 2010 - 5:48:08 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDSmartConsensus extends VRPSDConsensus {

    private final double  mAlpha           = 1.15;

    private final double  mLambda          = 0.05;

    private final double  mSecondThreshold = 0.2;

    public static boolean sImproveDecision = true;

    boolean               sDepotOnly       = false;

    public VRPSDSmartConsensus(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    @Override
    public IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam param) {
        VRPSDConsensus.sRepTripHack = false;

        INodeVisit n = getInstance().getShrunkRequest(0);

        // If at a depot use the default improved consensus for VRPSD
        if (n.isDepot()) {
            DistinguishedSolutionBase s = (DistinguishedSolutionBase) super
                    .buildDistinguishedPlan(param);
            return s;
        } // Else find the default consensus request and check if a better
          // request exists
        else {
            int nextRequestId = -1;
            double eval = 0, bestEval = -1;
            Map<Integer, Double> evaluations = new HashMap<Integer, Double>();
            for (IScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
                s.acquireLock();

                IActualRequest req = s.getFirstActualRequest(0);
                if (req != null && isRequestFeasible(req)) {
                    int reqId = req.getID();
                    eval = updateEvaluation(evaluations, reqId);
                    if (eval >= bestEval) {
                        bestEval = eval;
                        nextRequestId = reqId;
                    }
                }
                s.releaseLock();
            }

            // Next request
            VRPActualRequest m = (VRPActualRequest) getComponentManager().getParentMSAProxy()
                    .getInstance().getNodeVisit(nextRequestId);

            // No suitable request was found, force return to the first depot
            if (m == null) {
                m = new VRPActualRequest(getInstance().getDepotsVisits().iterator().next());
            } // Look for a better request
            else if (sImproveDecision && (!sDepotOnly || m.isDepot())) {
                /*
                 * Notations: 
                 * n - current node 
                 * m - next node (consensus) 
                 * j - candidate node 
                 * i,k - predecesor/succesor of j in a given scenario
                 */
                INodeVisit depot = getInstance().getDepotsVisits().iterator().next();

                // Cost of arc (n,m)
                double c_nm = getInstance().getCost(getInstance().getShrunkRequest(0), m,
                        getInstance().getFleet().getVehicle());

                // Best candidate evaluation
                double bestEval2 = Double.POSITIVE_INFINITY;
                // Best candidate
                VRPActualRequest bestRequest = null;
                // Vehicle remaining capacity
                double cap = getInstance().getFleet().getVehicle().getCapacity()
                        - getInstance().getCurrentLoad(0, 0);

                // Candidate requests
                Set<VRPActualRequest> candidates = new HashSet<VRPActualRequest>(getInstance()
                        .getPendingRequests());
                Iterator<VRPActualRequest> it = candidates.iterator();
                double maxScndOccurences = getMSAProxy().getScenarioPool().size()
                        * mSecondThreshold;
                while (it.hasNext()) {
                    VRPActualRequest r = it.next();
                    // Check that the request is not the consensus request
                    boolean valid = r.getID() == nextRequestId;

                    IDemand dem = r.getParentRequest().getAttribute(RequestAttributeKey.DEMAND);
                    if (valid && dem instanceof StochasticDemand) {
                        // Failure evaluation threshold
                        Distribution dist = ((StochasticDemand) dem).getDistribution(0);
                        valid = 1 - dist.cdf(cap) < mLambda;
                    } else if (valid) {
                        // Feasible demand
                        valid = dem.getDemand(0) <= cap;
                    }

                    if (!valid) {
                        it.remove();
                    } else {
                        // Check if request appear too often is second place
                        int secondCount = 0;
                        for (IScenario s : getComponentManager().getParentMSAProxy()
                                .getScenarioPool()) {
                            if (((VRPScenario) s).getRoute(0).length() > 3
                                    && ((VRPScenario) s).getRoute(0).getNodeAt(3).getID() == r
                                            .getID()) {
                                secondCount++;
                            }

                            if (secondCount > maxScndOccurences) {
                                it.remove();
                                break;
                            }
                        }
                    }
                }

                // Consider all unserved requests
                for (VRPActualRequest j : candidates) {
                    Distribution dist = null;
                    double failProba = 1;

                    IDemand dem = j.getParentRequest().getAttribute(RequestAttributeKey.DEMAND);
                    if (dem instanceof StochasticDemand) {
                        // Failure evaluation threshold
                        dist = ((StochasticDemand) dem).getDistribution(0);
                        failProba = 1 - dist.cdf(cap);
                    } else {
                        // Feasible demand
                        failProba = dem.getDemand(0) <= cap ? 0 : 1;
                    }

                    // Scenario dependent detour cost
                    double scenDetourCost = 0;
                    int count = 0;

                    // Evaluate all scenarios
                    for (IScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
                        VRPScenario scen = (VRPScenario) s;

                        // Find the candidate route and position
                        int indexOfJ = -1;
                        VRPScenarioRoute routeOfJ = null;
                        for (VRPScenarioRoute route : scen) {
                            indexOfJ = route.getNodePosition(j);
                            if (indexOfJ >= 0) {
                                routeOfJ = route;
                                break;
                            }
                        }

                        // The candidate was found in this scenario
                        if (indexOfJ >= 0) {
                            INodeVisit i = indexOfJ > 0 ? routeOfJ.getNodeAt(indexOfJ - 1) : null;
                            INodeVisit k = indexOfJ < routeOfJ.length() - 1 ? routeOfJ
                                    .getNodeAt(indexOfJ + 1) : null;
                            // Expected cost of detour
                            // -c_ij -c_jk +c_ik
                            scenDetourCost += -getCost(i, j) - getCost(j, k) + getCost(i, k);

                            count++;
                        }

                    }
                    // Average scenario detour cost
                    scenDetourCost /= count;

                    // Expected detour cost:
                    // c_nj +c_jm+p_f (c_j0 + c_0j) + scenDetourCost
                    double detourCost = +getCost(n, j) + getCost(j, m) + 2 * failProba
                            * getCost(j, depot) + scenDetourCost;

                    if (detourCost < bestEval2) {
                        bestEval2 = detourCost;
                        bestRequest = j;
                    }

                }

                if (bestRequest != null && bestEval2 < c_nm * mAlpha) {
                    MSALogging
                            .getComponentsLogger()
                            .info("%s.buildDistinguishedPlan: consensus decision bypassed - cons: %s (%.3f) detour: %s (%.3f)",
                                    getClass().getSimpleName(), m.getID(), c_nm,
                                    bestRequest.getID(), bestEval2);
                    m = bestRequest;
                    bestEval = bestEval2;
                } else if (bestRequest != null) {
                    MSALogging
                            .getComponentsLogger()
                            .info("%s.buildDistinguishedPlan: alternative request discarded - cons: %s (%.3f) detour: %s (%.3f) - factor: %.3f",
                                    getClass().getSimpleName(), m.getID(), c_nm,
                                    bestRequest.getID(), bestEval2, bestEval2 / c_nm);
                }
            }

            MSALogging.getComponentsLogger().info(
                    "%s.buildDistinguishedPlan: best request found : %s - score:%s",
                    getClass().getSimpleName(), m, bestEval);
            return new DistinguishedSolutionBase(m);
        }

    }

    public double getCost(INodeVisit i, INodeVisit j) {
        if (i == null || j == null) {
            return 0;
        } else {
            return getInstance().getCost(i, j, getInstance().getFleet().getVehicle());
        }
    }
}
