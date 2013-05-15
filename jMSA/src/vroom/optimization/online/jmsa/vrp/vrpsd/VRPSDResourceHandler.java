/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import vroom.common.modeling.dataModel.NodeInsertion;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.dataModel.Request;
import vroom.common.utilities.Constants;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.events.EventHandlingException;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.ScenarioPool;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerParam;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.events.ResourceHandler;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;
import vroom.optimization.online.jmsa.vrp.VRPShrunkRequest;

/**
 * <code>VRPSDResourceHandler</code> is a specialization of {@link ResourceHandler} for the VRPSD.
 * <p>
 * Creation date: May 10, 2010 - 8:58:15 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDResourceHandler extends ResourceHandler<VRPScenario, MSAVRPInstance> {

    /**
     * Creates a new <code>VRPSDResourceHandler</code>
     * 
     * @param parentMSA
     */
    public VRPSDResourceHandler(MSAProxy<VRPScenario, MSAVRPInstance> parentMSA) {
        super(parentMSA);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.events.ResourceHandler#handleEvent(edu .uniandes .copa.jMSA.events.ResourceEvent)
     */
    @Override
    public boolean handleEvent(ResourceEvent event) throws EventHandlingException {
        MSAVRPInstance instance = getParentMSAProxy().getInstance();

        boolean result;

        switch (event.getType()) {
        case START:
            result = super.handleEvent(event);
            break;
        case STOP:
            result = super.handleEvent(event);
            break;
        case REQUEST_ASSIGNED:
            result = super.handleEvent(event);
            break;
        case END_OF_SERVICE:
            result = super.handleEvent(event);

            // 0. Check if the request is a depot and the route has been started
            if (event.getRequest() != null
                    && ((VRPRequest) event.getRequest()).isDepot()
                    && getParentMSAProxy().getInstance().isResourceStarted(event.getResourceId())
                    && getParentMSAProxy().getInstance().getShrunkRequest(event.getResourceId())
                            .size() > 1) {

                result = handleReplenishmentTrip(event);
            }
            break;
        case START_OF_SERVICE:
            // Update the request demand
            if (event.getRequest() instanceof VRPSDActualRequest) {
                ((VRPSDActualRequest) event.getRequest()).setActualDemands((double[]) event
                        .getAdditionalInformation());
            }

            int veh = event.getResourceId();

            double excLoad = instance.getCurrentLoad(veh, 0)
                    - instance.getFleet().getVehicle(veh).getCapacity();
            // 0. Check if the route capacity is violated
            if (excLoad > 0) {
                if (!(event.getRequest() instanceof VRPActualRequest)) {
                    throw new IllegalArgumentException(
                            "Route failure occured at a node that is not a VRP actual");
                }

                // Handle the failure
                result = handleFailure(event, excLoad, instance.getFleet().getVehicle(veh)
                        .getCapacity());

                getParentMSAProxy().callbacks(EventTypes.EVENTS_RESOURCE,
                        new Object[] { event, result, excLoad });
            } else {
                result = super.handleEvent(event);

                // Reset all scenarios
                // for (VRPScenario s : getParentMSAProxy().getScenarioPool()) {
                // s.clear();
                // getParentMSAProxy().getComponentManager().getScenarioOptimizer()
                // .initialize(s, Integer.MAX_VALUE);
                // }

                // Force optimization of the pool
                Stopwatch t = new Stopwatch();
                t.start();
                int reinitCount = 0;
                MSALogging.getEventsLogger().debug(
                        "VRPSDResourceHandler.handleEvent: reoptimizing the pool");
                for (VRPScenario s : getParentMSAProxy().getScenarioPool()) {

                    boolean reinit = false;
                    // Check if scenario is feasible
                    for (VRPScenarioRoute r : s) {
                        if (r.getLoad() > r.getVehicle().getCapacity()) {
                            reinit = true;
                            break;
                        }
                    }
                    if (reinit) {
                        s.clear();
                        getParentMSAProxy()
                                .getComponentManager()
                                .getScenarioOptimizer()
                                .initialize(s,
                                        new ScenarioOptimizerParam(Integer.MAX_VALUE, 100, false));
                        reinitCount++;
                    } else {
                        // getParentMSAProxy().getComponentManager().optimize(s,
                        // new ScenarioOptimizerParam(60, 2, false));

                    }
                }
                t.stop();
                MSALogging
                        .getEventsLogger()
                        .debug("VRPSDResourceHandler.handleEvent: pool reoptimized in %sms, %s scenarios were reinitialized out of %s",
                                t.readTimeMS(), reinitCount,
                                getParentMSAProxy().getScenarioPool().size());
            }

            break;
        default:
            result = super.handleEvent(event);
            break;
        }

        return result;
    }

    /**
     * Handle a replenishment trip
     * 
     * @param event
     * @return <code>true</code>
     */
    protected boolean handleReplenishmentTrip(ResourceEvent event) {
        ScenarioPool<VRPScenario> pool = getParentMSAProxy().getScenarioPool();
        LinkedList<VRPScenario> removedScenarios = new LinkedList<VRPScenario>();

        // 1 Update the scenario pool
        for (VRPScenario scenario : pool) {
            scenario.acquireLock();

            VRPScenarioRoute firstRoute = scenario.getRoute(0);

            // 2 Remove the first route of all scenarios if it is of length
            // inferior to 2
            // or equal to 3 ending with a depot
            // nodeI.e. <depot,shrunkNode[,depot]>
            if (firstRoute.length() <= 2 || firstRoute.length() == 3
                    && firstRoute.getLastNode().isDepot()) {
                scenario.removeRoute(scenario.getRoute(0));
                // 3 Try to repair
            } else {
                if (reinsertRequests(scenario, 0, 2, firstRoute.length() - 1)) {
                    // 3.a Repair successful, remove the first route
                    scenario.removeRoute(firstRoute);
                } else {
                    // 3.b Repair failed, remove the scenario
                    removedScenarios.add(scenario);
                }
            }
            scenario.releaseLock();
        }

        pool.removeScenarios(removedScenarios);

        return true;
    }

    /**
     * Handle a failure
     * 
     * @param event
     * @param excLoad
     * @param vehicleCap
     * @return <code>true</code>
     * @throws EventHandlingException
     */
    protected boolean handleFailure(ResourceEvent event, double excLoad, double vehicleCap)
            throws EventHandlingException {
        boolean result;

        VRPActualRequest req = (VRPActualRequest) event.getRequest();

        // 1. Change the given request so that it only has the feasible demand
        double feasibleDem = req.getDemand() - excLoad;
        if (Constants.isPositive(feasibleDem)) {
            if (req instanceof VRPSDActualRequest) {
                ((VRPSDActualRequest) req).setActualDemands(feasibleDem);
            } else {
                req.getParentRequest().setDemands(feasibleDem);
            }
        } else {
            throw new IllegalStateException(String.format(
                    "The feasible demand is negative (%s): failure occured before", feasibleDem));
        }

        MSALogging
                .getEventsLogger()
                .info("VRPSDResourceHandler.handleEvent: a route failure has occured, updating the pool (request:%s exceeding capacity:%s)",
                        req, excLoad);

        result = super.handleEvent(event);

        // 2. Update all the scenarios by forcing the closure of the current
        // route, reinsert remaining nodes
        LinkedList<VRPScenario> removedScenarios = new LinkedList<VRPScenario>();
        for (VRPScenario scenario : getParentMSAProxy().getScenarioPool()) {
            scenario.acquireLock();

            // Assume that the first route is the current route
            VRPScenarioRoute firstRoute = scenario.getRoute(0);

            // The route should have the sequence: <depot,shrunkNode,...,depot>
            // where the shrunkNode includes the failure node
            if (firstRoute.length() > 3) {
                // There are subsequent node visits
                // Reinsert them in the other routes
                if (!reinsertRequests(scenario, 0, 2, firstRoute.length() - 2)) {
                    removedScenarios.add(scenario);
                } else {
                    firstRoute.calculateLoad(true);
                }
            }

            scenario.releaseLock();
        }
        // 2.b Remove scenarios that could not be repaired
        getParentMSAProxy().getScenarioPool().removeScenarios(removedScenarios);

        // 3. Create and insert in the instance and scenarios new requests with
        // the remaining demand
        int numAdVisit = (int) Math.ceil(excLoad / vehicleCap);
        VRPActualRequest[] newReqs = new VRPActualRequest[numAdVisit];
        for (int v = 0; v < numAdVisit; v++) {
            double dem = Math.min(excLoad, vehicleCap);
            excLoad -= dem;

            Request request = new Request((v + 1) * 10000 + req.getID(), req.getNode());
            request.setDemands(dem);

            newReqs[v] = new VRPActualRequest(NodeVisit.createNodeVisits(request)[0]);

            getParentMSAProxy().getInstance().acquireLock();
            getParentMSAProxy().getInstance().requestReleased(newReqs[v]);
            getParentMSAProxy().getInstance().releaseLock();
        }

        getParentMSAProxy().getComponentManager().insertRequest(newReqs);

        // TODO 4. Raise callback event?

        return result;
    }

    /**
     * Check and intent to restore capacity feasibility of the first route of each scenario
     * 
     * @param event
     * @return <code>true</code>
     */
    protected boolean restoreFeasibility(ResourceEvent event) {

        LinkedList<VRPScenario> removedScenarios = new LinkedList<VRPScenario>();

        for (VRPScenario scenario : getParentMSAProxy().getScenarioPool()) {
            VRPScenarioRoute firstRoute = scenario.getRoute(0);

            // 0. Force load recalculation
            firstRoute.calculateLoad(true);

            // 1. Check route feasibility
            double cap = getParentMSAProxy().getInstance().getFleet()
                    .getVehicle(event.getResourceId()).getCapacity();
            if (firstRoute.getLoad() > cap) {

                // 2. Find the failure
                double load = 0;
                int failure = 0;
                Iterator<VRPRequest> it = firstRoute.iterator();

                while (it.hasNext() && load <= cap) {
                    failure++;
                    load += it.next().getDemand();
                }

                // 3. Try to reinsert subsequent nodes
                if (!reinsertRequests(scenario, 0, failure, firstRoute.length() - 2)) {
                    // 3.a Failed: remove the scenario
                    removedScenarios.add(scenario);
                }
            }

        }

        getParentMSAProxy().getScenarioPool().removeScenarios(removedScenarios);
        return true;
    }

    /**
     * Request extraction and re-insertion.
     * <p>
     * This method will remove the nodes in the range <code>min...max</code> (inclusive) from the given
     * <code>route</code> and attempt to reinsert them in the other routes of the scenario.
     * </p>
     * <p>
     * Note that this method will only reinsert instances of {@link VRPActualRequest} and ignore depots
     * </p>
     * 
     * @param scenario
     *            the scenario to modify
     * @param route
     *            the index of the route from which nodes will be extracted
     * @param start
     *            the index of the first removed node
     * @param end
     *            the index of the last removed node
     * @return <code>true</code> if all nodes were extracted and reinserted, <code>false</code> otherwise
     */
    protected boolean reinsertRequests(VRPScenario scenario, int route, int start, int end) {

        // Extract the remaining nodes
        Collection<VRPRequest> requests = scenario.getRoute(route).extractNodes(start, end);

        boolean b = true;

        for (VRPRequest movedReq : requests) {
            // Cannot reinsert a shrunk request
            if (movedReq instanceof VRPShrunkRequest) {
                return false;
            }

            // Only reinsert actual requests
            if (movedReq instanceof VRPActualRequest && !movedReq.isDepot()) {
                // Get the best insertion in all subsequent routes
                NodeInsertion ins = scenario.getBestInsertion(movedReq, route);

                if (ins != null) {
                    // insert the node in the best position
                    b &= ((VRPScenarioRoute) ins.getRoute()).insertNode(ins, movedReq);
                } else {
                    // No feasible re-insertion, remove the scenario
                    return false;
                }
            }
        }

        return b;
    }
}
