/*
 * 
 */
package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import umontreal.iro.lecuyer.probdist.DiscreteDistributionInt;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.StochasticDemand;
import vroom.optimization.online.jmsa.DistinguishedSolutionBase;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ConsensusSolutionBuilder;
import vroom.optimization.online.jmsa.components.ISolutionBuilderParam;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPConsensusSolutionBuilder;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;

/**
 * <code>VRPConsensusSolutionBuilder</code> is a specialization of {@link ConsensusSolutionBuilder} for the VRPSD with a single vehicle.
 * <p>
 * Creation date: Sep 8, 2010 - 5:27:57 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDConsensus extends VRPConsensusSolutionBuilder {

    /**
     * <em>Hack</em> the consensus algorithm to force the visiting of any feasible request before going back to depot
     */
    public static boolean sRepTripHack = false;

    /**
     * Creates a new <code>VRPConsensusSolutionBuilder</code>
     * 
     * @param componentManager
     */
    public VRPSDConsensus(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    @Override
    public IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam param) {

        // Special case when vehicle is at the depot, evaluate all
        // starting/ending nodes of all routes
        if (getInstance().getShrunkRequest(0).isDepot()) {
            int nextRequestId = -1;
            double eval = 0, bestEval = -1, bestCost = 0;
            Map<Integer, Double> evaluations = new HashMap<Integer, Double>();

            INodeVisit depot = getInstance().getShrunkRequest(0);
            for (IScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
                s.acquireLock();

                VRPScenario scen = (VRPScenario) s;

                for (VRPScenarioRoute r : scen) {
                    if (r.length() > 2 && (!r.containsShrunkNode() || r.length() > 3)) {
                        // Evaluate the first and last requests
                        int first = r.containsShrunkNode() ? 2 : 1;
                        VRPActualRequest firstReq = (VRPActualRequest) r.getNodeAt(first);
                        VRPActualRequest lastReq = (VRPActualRequest) r.getNodeAt(r.length() - 2);

                        if (isRequestFeasible(firstReq)) {
                            eval = updateEvaluation(evaluations, firstReq.getID());
                            double cost = scen.getParentInstance().getCost(depot, firstReq);
                            if (eval > bestEval || (eval == bestEval && cost > bestCost)) {
                                bestEval = eval;
                                nextRequestId = firstReq.getID();
                                bestCost = cost;
                            }
                        }

                        if (isRequestFeasible(lastReq)) {
                            eval = updateEvaluation(evaluations, lastReq.getID());
                            double cost = scen.getParentInstance().getCost(depot, lastReq);
                            if (eval > bestEval || (eval == bestEval && cost > bestCost)) {
                                bestEval = eval;
                                nextRequestId = lastReq.getID();
                            }
                        }
                    }
                }

                s.releaseLock();
            }

            IActualRequest nextRequest = getInstance().getNodeVisit(nextRequestId);

            MSALogging.getComponentsLogger().info(
                "ConsensusSolutionBuilder.buildDistinguishedPlan: best request found : %s - score:%s",
                nextRequest, bestEval);

            return new DistinguishedSolutionBase(nextRequest);
        } else {
            int nextRequestId = -1;
            double eval = 0, bestEval = -1;
            Map<Integer, Double> evaluations = new HashMap<Integer, Double>();
            for (IScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
                s.acquireLock();

                VRPSDScenarioUpdater.repairSolution((VRPScenario) s);

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

            VRPActualRequest nextRequest = (VRPActualRequest) getComponentManager().getParentMSAProxy()
                .getInstance()
                .getNodeVisit(nextRequestId);

            // No suitable request was found, force return to the first depot
            if (nextRequest == null) {
                nextRequest = new VRPActualRequest(getInstance().getDepotsVisits().iterator().next());
            } else if (sRepTripHack && nextRequest.isDepot()) {
                // Check is there is a "failsafe" request
                bestEval = 0;
                VRPActualRequest bestRequest = null;
                double cap = getInstance().getFleet().getVehicle().getCapacity()
                        - getInstance().getCurrentLoad(0, 0);
                for (Entry<Integer, Double> cand : evaluations.entrySet()) {
                    if (getInstance().getNodeVisit(cand.getKey()) instanceof VRPSDActualRequest) {
                        VRPSDActualRequest r = (VRPSDActualRequest) getInstance().getNodeVisit(cand.getKey());

                        StochasticDemand dists = (StochasticDemand) r.getParentRequest().getAttribute(
                            RequestAttributeKey.DEMAND);
                        if (((DiscreteDistributionInt) dists.getDistribution(0)).getXsup() < cap
                                && (bestRequest == null || cand.getValue() > bestEval)) {
                            bestEval = cand.getValue();
                            bestRequest = r;
                        }
                    }
                }
                if (bestRequest != null) {
                    nextRequest = bestRequest;
                }
            }

            MSALogging.getComponentsLogger().info(
                "ConsensusSolutionBuilder.buildDistinguishedPlan: best request found : %s - score:%s",
                nextRequest, bestEval);

            return new DistinguishedSolutionBase(nextRequest);
        }
    }
}
