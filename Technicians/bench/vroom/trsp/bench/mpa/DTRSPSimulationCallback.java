/**
 * 
 */
package vroom.trsp.bench.mpa;

import java.util.LinkedList;

import vroom.common.utilities.Utilities;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.events.DecisionEvent;
import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.events.MSAEvent;
import vroom.optimization.online.jmsa.events.NewRequestEvent;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.events.ResourceEvent.EventTypes;
import vroom.optimization.online.jmsa.utils.MSASimulationCallback;
import vroom.optimization.online.jmsa.utils.MSASimulator.DuplicateEventCleaner;
import vroom.optimization.online.jmsa.utils.MSASimulator.IEventQueueCleaner;
import vroom.optimization.online.jmsa.utils.MSASimulator.ResourceState;
import vroom.optimization.online.jmsa.utils.MSASimulator.ResourceStates;
import vroom.optimization.online.jmsa.utils.MSASimulator.ScheduledEvent;
import vroom.trsp.datamodel.ITRSPNode;
import vroom.trsp.datamodel.ITRSPNode.NodeType;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPInstance.DepotNode;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.optimization.mpa.DTRSPScenarioUpdater;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.optimization.mpa.DTRSPSolution.DTRSPTour;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPSimulationCallback</code> is an extension of {@link MSACallbackBase} that is used to control the simulation
 * process. It listens to MSA and schedules resource events depending on the decisions that are taken.
 * <p>
 * Creation date: Mar 15, 2012 - 4:29:05 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPSimulationCallback extends MSASimulationCallback {

    private final double      mDelta;
    private final double      mTimeMargin;

    private final DTRSPRunMPA mRun;
    private boolean           mAllResourcesStopped = false;

    /**
     * Returns the parent TRSP run
     * 
     * @return the parent TRSP run
     */
    private DTRSPRunMPA getRun() {
        return mRun;
    }

    /**
     * Returns the instance being solved
     * 
     * @return the instance being solved
     */
    private TRSPInstance getInstance() {
        return getRun().getInstance();
    }

    public DTRSPSimulationCallback(DTRSPRunMPA run) {
        super(run.getMSASimulator());
        mRun = run;

        // We use a 100ms margin to schedule events a bit earlier
        mTimeMargin = getSimulator().wallToSimTime(0.100);

        // Estimate the value for Delta:
        mDelta = run.getInstance().getFleet().size()
                * run.getInstance().getMainDepot().getTimeWindow().width()
                / (10 * run.getInstance().getRequestCount()) + mTimeMargin;
    }

    @Override
    protected void msaStarted(MSACallbackEvent event) {
        super.msaStarted(event);
        for (Technician t : getInstance().getFleet()) {
            // Set the current location as the home of the technician
            ITRSPNode home = getInstance().getTRSPNode(t.getHome().getID());
            getInstance().getSimulator().assignNodeToTechnician(t.getID(), home,
                    getSimulator().simulationTime());
            getInstance().getSimulator().setAssignedNodeAsCurrent(t.getID());
        }
    }

    @Override
    protected void msaEnded(MSACallbackEvent event) {
        super.msaEnded(event);
        // Do nothing
    }

    @Override
    protected void newDistinguishedSolution(MSACallbackEvent event, IDistinguishedSolution oldSol,
            IDistinguishedSolution newSol) {
        super.newDistinguishedSolution(event, oldSol, newSol);
        getSimulator().pause();
        if (newSol == null) {
            if (mAllResourcesStopped) {
                getSimulator().resume();
                return;
            }
            // return;
            getSimulator().releaseLock();
            throw new IllegalStateException("Distinguished solution should not be null");
        }

        DTRSPSolution newScen = (DTRSPSolution) newSol;
        TRSPLogging
                .getSimulationLogger()
                .info("TRSPSimulationCallback.newDistinguishedSolution: new distinguished solution, assigning requests to idle technicians (current time:%.2f, next slice:%.2f) - %s",
                        getSimulator().simulationTime(), getSimulator().simulationTime() + mDelta,
                        newScen.toShortString());

        if (!newScen.getUnservedRequests().isEmpty()) {
            TRSPLogging
                    .getSimulationLogger()
                    .warn("TRSPSimulationCallback.newDistinguishedSolution: Distinguished solution leaves unserved requests: %s",
                            Utilities.toShortString(newScen.getUnservedRequests()));
            // throw new IllegalStateException(
            // "Distinguished solution leaves some requests unserved: "
            // + Utilities.toShortString(newScen.getUnservedRequests()));
        }

        LinkedList<Integer> unservableRequests = new LinkedList<>();
        for (Integer r : newScen.getUnservedRequests()) {
            if (!getInstance().getRequest(r).getTimeWindow()
                    .isFeasible(getSimulator().simulationTime())) {
                TRSPLogging
                        .getSimulationLogger()
                        .info("TRSPSimulationCallback.newDistinguishedSolution: unserved request %3s cannot be serviced in time (%.2f@%s)",
                                r, getSimulator().simulationTime(),
                                getInstance().getRequest(r).getTimeWindow());
                unservableRequests.add(r);
            }
        }
        if (!unservableRequests.isEmpty()) {
            getSimulator().releaseLock();
            TRSPLogging
                    .getSimulationLogger()
                    .error("TRSPSimulationCallback.newDistinguishedSolution: infeasible requests: %s - pool: %s",
                            Utilities.toShortString(unservableRequests),
                            getRun().getMPA().getProxy().getScenarioPool());
            for (int r : unservableRequests) {
                TRSPLogging
                        .getSimulationLogger()
                        .error("TRSPSimulationCallback.newDistinguishedSolution: sim time: %.2f - infeasible request: %s",
                                getSimulator().simulationTime(), getInstance().getRequest(r));
            }
            throw new IllegalStateException(String.format(
                    "Distinguished solution leaves requests that cannot be served: %s",
                    Utilities.toShortString(unservableRequests)));
        }

        boolean cutoff = getInstance().getSimulator().cutoff(getSimulator().simulationTime());

        boolean oneAssign = false;
        boolean allIdle = true;
        boolean oneIdle = false;
        boolean rescheduleDecision = false;
        for (Technician t : getInstance().getFleet()) {
            DTRSPTour tour = newScen.getTour(t.getID());
            ResourceState state = getSimulator().getState(t.getID());
            final int techId = t.getID();
            // The current request
            final int current = getRun().getSimulator().getCurrentNode(techId).getID();
            if (state.getState() == ResourceStates.IDLE) {
                ITRSPNode nextNode = (ITRSPNode) newSol.getNextRequest(t.getID());
                oneIdle = true;

                // The latest feasible departure time at the current node
                final double latestDepTime = tour.getLatestFeasibleDepartureTime(current);

                if (nextNode != null) {
                    // A flag set to true if the technician should be sent back home
                    boolean sendHome = false;
                    if (nextNode.getType() == NodeType.HOME) {
                        if (cutoff) {
                            // Check that the technician is sent home in all scenarios
                            sendHome = true;
                            for (DTRSPSolution s : getRun().getMPA().getProxy().getScenarioPool()) {
                                if (s.getNextRequest(t.getID()).getID() != nextNode.getID()) {
                                    sendHome = false;
                                    break;
                                }
                            }
                        } else if (latestDepTime < getSimulator().simulationTime() + mDelta) {
                            sendHome = true;
                        }
                    }

                    if (nextNode.getType() != NodeType.HOME || sendHome
                    // || getInstance().getUnservedReleasedRequests().isEmpty()
                    ) {
                        // The assigned request
                        int next = nextNode.getID();
                        // The time of the last event
                        final double lastEventTime = getSimulator().getPenultimateEvent() != null ? getSimulator()
                                .getPenultimateEvent().time() : 0;
                        // Check that the current state is feasible
                        if (latestDepTime < getSimulator().simulationTime()) {
                            if (latestDepTime > lastEventTime - getSimulator().simTimeTolerance()) { // Allow a cheating
                                // Adjust the simulation time
                                TRSPLogging
                                        .getSimulationLogger()
                                        .warn("TRSPSimulationCallback.newDistinguishedSolution: adjusting simulation time for node %s (was:%7.2f latest feasible dep time:%7.2f)",
                                                current, getSimulator().simulationTime(),
                                                latestDepTime);
                                if (latestDepTime < getSimulator().simulationTime()
                                        - getSimulator().wallToSimTime(2))
                                    TRSPLogging
                                            .getSimulationLogger()
                                            .warn("TRSPSimulationCallback.newDistinguishedSolution: the simulation time adjustment is greater than 2s cpu time");
                                getSimulator().adjustSimulationTime(latestDepTime);
                            } else {
                                // The departure time is in the past
                                // TODO check what happens with end of service events: the decision should be taken
                                // IMMEDIATLY, is it the case?
                                getSimulator().releaseLock();
                                throw new IllegalStateException(
                                        String.format(
                                                "Simulation time (%.2f) and last event time (%.2f) are after latest feasible departure time for node %s (lfd:%.2f ed:%.2f) - delta_cpu:%.0fms, last_event:%s, technician %s, tour:%s",
                                                getSimulator().simulationTime(),
                                                lastEventTime,
                                                current,
                                                latestDepTime,
                                                tour.getEarliestDepartureTime(current),
                                                getSimulator().simToWallTime(
                                                        latestDepTime
                                                                - getSimulator().simulationTime()) * 1000,
                                                getSimulator().getPenultimateEvent(), techId, tour));
                            }
                        }
                        // The temptative departure time : latest between
                        final double depTime = Math.max( //
                                // The earliest departure + the waiting time at the next node
                                tour.getWaitDepartureTime(current),
                                // The current simulation time
                                getSimulator().simulationTime());
                        // Make sure that there are no other assigned event for this resource
                        getSimulator().cleanQueue(new IEventQueueCleaner() {
                            @Override
                            public boolean remove(ScheduledEvent event) {
                                MSAEvent e = event.getMSAEvent();
                                if (!ResourceEvent.class.isAssignableFrom(e.getClass()))
                                    return false;
                                ResourceEvent r = (ResourceEvent) e;
                                if (r.getType() == EventTypes.REQUEST_ASSIGNED
                                        && r.getResourceId() == techId) {
                                    // Reset the arrival time
                                    ((ITRSPNode) r.getRequest()).setArrivalTime(Double.NaN);
                                    return true;
                                } else
                                    return false;
                            }
                        });
                        // Set the arrival time at the destination (will be used when the request will be assigned)
                        nextNode.setArrivalTime(depTime
                                + getInstance().getCostDelegate().getTravelTime(current, next, t));
                        // Schedule the assigned event
                        if (depTime < getSimulator().simulationTime() + mDelta) {
                            // The departure time is before t+Delta, we assume no change can be done and assign the
                            // request directly
                            getRun().getMPA().getEventFactory()
                                    .raiseRequestAssignedEvent(techId, nextNode);

                            TRSPLogging
                                    .getSimulationLogger()
                                    .info("TRSPSimulationCallback.newDistinguishedSolution: technician %3s assigned to %s at time %7.2f (%s)",
                                            techId, nextNode.getDescription(), depTime, state);
                        } else {
                            // The departure time is after t+Delta
                            // Freeze the technician to prevent too aggressive optimization
                            freezeTechnician(techId, tour);
                            rescheduleDecision = true;
                        }
                        oneAssign = true;
                    } else {
                        // The next node is the technician home
                        // We wait until all request have been served before sending him back home
                        // Freeze the technician to prevent too aggressive optimization
                        freezeTechnician(techId, tour);
                        rescheduleDecision = true;
                    }
                } else {
                    // The technician is idle but is not assigned to a request yet
                    // Freeze the technician to prevent too aggressive optimization
                    freezeTechnician(techId, tour);
                    rescheduleDecision = true;
                }
            } else {
                allIdle = false;

                TRSPLogging
                        .getSimulationLogger()
                        .info("TRSPSimulationCallback.newDistinguishedSolution: technician %3s is %s (current:%3s next: %3s)",
                                techId, state, current, tour.getSucc(current));
            }
        }

        if (rescheduleDecision) {
            double time = Math.max(0, getSimulator().simulationTime() + mDelta - mTimeMargin);
            TRSPLogging
                    .getSimulationLogger()
                    .info("TRSPSimulationCallback.newDistinguishedSolution: rescheduling a decision event at %.2f",
                            time);
            // We schedule a decision event at t+Delta
            getSimulator().scheduleEvent(
                    new DecisionEvent(time, getRun().getMPA().getEventFactory()));
            getSimulator().cleanQueue(mDecisionCleaner);
        }

        getSimulator().resume();
        if (allIdle && !oneAssign && !getInstance().getUnservedReleasedRequests().isEmpty()) {
            getSimulator().releaseLock();
            throw new IllegalStateException(
                    "All technicicans are idle but no assignment was made, unserved requests: "
                            + Utilities.toShortString(getInstance().getUnservedReleasedRequests()));
        }
    }

    /**
     * Freeze a technician at its current location
     * 
     * @param technician
     * @param tour
     */
    protected void freezeTechnician(int technician, DTRSPTour tour) {
        int current = getRun().getSimulator().getCurrentNode(technician).getID();

        TRSPLogging
                .getSimulationLogger()
                .info("TRSPSimulationCallback.newDistinguishedSolution: technician %3s frozen until %7.2f (current:%3s next: %3s)",
                        technician, getSimulator().simulationTime() + mDelta, current,
                        tour.getSucc(current));

        // We fix the earliest departure date to t+Delta
        // Check all scenarios and current solution
        LinkedList<IScenario> infeasibleScenarios = new LinkedList<>();
        for (IScenario s : getRun().getMPA().getProxy().getScenarioPool()) {
            // [12/06/14] this may lead to infeasible scenarios, check their feasibility and possibly fix them
            DTRSPSolution scen = (DTRSPSolution) s;
            scen.freeze(current, tour.getEarliestArrivalTime(current), getSimulator()
                    .simulationTime() + mDelta);
            if (!((DTRSPScenarioUpdater) getRun().getMPA().getComponentManager()
                    .getScenarioUpdater()).fixTour(scen, scen.getTour(tour.getTechnicianId()))) {
                infeasibleScenarios.add(s);
            }
        }
        // [12/06/20] Also update the distinguished solution
        ((DTRSPSolution) getRun().getMPA().getDistinguishedSolution()).freeze(current,
                tour.getEarliestArrivalTime(current), getSimulator().simulationTime() + mDelta);

        if (!infeasibleScenarios.isEmpty()) {
            TRSPLogging
                    .getSimulationLogger()
                    .info("TRSPSimulationCallback.newDistinguishedSolution: %s scenario(s) became infeasible and will be discarded",
                            infeasibleScenarios.size());
            getRun().getMPA().getProxy().getScenarioPool().removeScenarios(infeasibleScenarios);
        }

        // Update the current solution
        DTRSPSolution currentSol = ((DTRSPSolution) getRun().getMPA().getCurrentSolution());
        currentSol.unfreeze();
        currentSol.freeze(current, tour.getEarliestArrivalTime(current), getSimulator()
                .simulationTime() + mDelta);
        if (!((DTRSPScenarioUpdater) getRun().getMPA().getComponentManager().getScenarioUpdater())
                .fixTour(currentSol, currentSol.getTour(tour.getTechnicianId()))) {
            getSimulator().releaseLock();
            throw new IllegalStateException(
                    "TRSPSimulationCallback.newDistinguishedSolution: distinguished solution became infeasible");
        }
        currentSol.freeze();
    }

    @Override
    protected void resourceStarted(MSACallbackEvent cbEvent, ResourceEvent e) {
        getSimulator().pause();
        super.resourceStarted(cbEvent, e);
        // Schedule a decision event
        TRSPLogging
                .getSimulationLogger()
                .info("TRSPSimulationCallback.resourceStarted: technicians %s available, raising decision event",
                        e.getResourceId());
        getInstance().markRequestAsServed(
                getInstance().getTRSPNode(
                        getInstance().getFleet().getVehicle(e.getResourceId()).getHome().getID()),
                e.getResourceId());
        getRun().getMPA().getEventFactory().raiseDecisionEvent();
    }

    @Override
    protected void resourceStopped(MSACallbackEvent cbEvent, ResourceEvent e) {
        getSimulator().pause();
        super.resourceStopped(cbEvent, e);

        boolean finished = true;
        // Check if all technicians are back to their home
        for (Technician t : getRun().getInstance().getFleet())
            if (!getRun().getMPA().getInstance().isResourceStopped(t.getID())) {
                finished = false;
                break;
            }
        mAllResourcesStopped = finished;
        if (finished) {
            getSimulator().stop();
            TRSPLogging
                    .getSimulationLogger()
                    .info("TRSPSimulationCallback.resourceStopped: all technicians are back to their home, stopping");
            getRun().getMPA().stop();
        } else {
            getSimulator().resume();
        }
    }

    @Override
    protected void requestAssigned(MSACallbackEvent cbEvent, ResourceEvent e) {
        // FIXME check if the request is frozen and the times are set properly
        getSimulator().pause();
        super.requestAssigned(cbEvent, e);
        ITRSPNode n = (ITRSPNode) e.getRequest();
        int next = n.getID();
        int current = getRun().getSimulator().getCurrentNode(e.getResourceId()).getID();
        double travelTime = getRun()
                .getInstance()
                .getCostDelegate()
                .getTravelTime(current, next,
                        getRun().getInstance().getTechnician(e.getResourceId()));
        // Schedule the start of service
        double startTime = Math.max(getSimulator().simulationTime() + travelTime, n.getTimeWindow()
                .startAsDouble());

        TRSPLogging
                .getSimulationLogger()
                .info("TRSPSimulationCallback.requestAssigned: %s assigned to %s, simulating travel time and scheduling start of service at %.2f",
                        n.getDescription(), e.getResourceId(), startTime);
        getSimulator().scheduleEvent(
                ResourceEvent.newStartOfServiceEvent(startTime,
                        getRun().getMPA().getEventFactory(), e.getResourceId(), getRun()
                                .getInstance().getTRSPNode(next), null));
        getSimulator().resume();
    }

    @Override
    protected void startOfService(MSACallbackEvent cbEvent, ResourceEvent e) {
        getSimulator().pause();
        super.startOfService(cbEvent, e);
        ITRSPNode n = (ITRSPNode) e.getRequest();
        getRun().getSimulator().setAssignedNodeAsCurrent(e.getResourceId());
        TRSPLogging
                .getSimulationLogger()
                .info("TRSPSimulationCallback.startOfService: %s being served by %s, simulating service time and scheduling end of service",
                        n.getDescription(), e.getResourceId());
        double time = e.getSimulationTimeStamp() + n.getServiceTime();
        if (time < getSimulator().simulationTime())
            time = getSimulator().simulationTime();
        // TODO Make sure that the decision will be taken IMMEDIATLY after the end of service (idea: schedule a decision
        // event here?)
        getSimulator().scheduleEvent(
                ResourceEvent.newEndOfServiceEvent(time, getRun().getMPA().getEventFactory(),
                        e.getResourceId(), n, null));
        getSimulator().resume();
    }

    @Override
    protected void endOfService(MSACallbackEvent cbEvent, ResourceEvent e) {
        getSimulator().pause();
        super.endOfService(cbEvent, e);
        ITRSPNode n = (ITRSPNode) e.getRequest();
        if (n.getType() == NodeType.HOME && ((DepotNode) n).isDuplicate()) {
            // The served request is the technician home, schedule resource stop
            TRSPLogging
                    .getSimulationLogger()
                    .info("TRSPSimulationCallback.endOfService: %s served by %s, raising resource stop event (technician returned to home)",
                            n.getDescription(), e.getResourceId());
            getSimulator().scheduleEvent(
                    ResourceEvent.newStopServiceEvent(getSimulator().simulationTime(), getRun()
                            .getMPA().getEventFactory(), e.getResourceId(), null));
            getSimulator().resume();

        } else {
            TRSPLogging.getSimulationLogger().info(
                    "TRSPSimulationCallback.endOfService: %s served by %s, raising decision event",
                    n.getDescription(), e.getResourceId());
            getRun().getMPA().getEventFactory().raiseDecisionEvent();
        }
    }

    @Override
    protected void afterExecture(MSACallbackEvent event) {
        super.afterExecture(event);
        if (getSimulator().simulationTime() > 1.5 * getInstance().getMainDepot().getTimeWindow()
                .endAsDouble()) {
            TRSPLogging
                    .getSimulationLogger()
                    .error("TRSPSimulationCallback.afterExecute: the current simulation time suggests an error, aborting MSA\n"
                            + " MSASimulator : %s\n" + " TRSPSimulator: %s\n" + " MSA          :%s",
                            getSimulator(), getInstance().getSimulator(),
                            getRun().getSolver().getMPA());
            getRun().getSolver().getMPA().stop();
        }
    }

    @Override
    protected void newRequest(MSACallbackEvent event, NewRequestEvent e) {
        getSimulator().pause();
        super.newRequest(event, e);
        // If the request is accepted we want to raise a decision event in case one of
        // the technicians has to be dispatched earlier
        // At this stage we do not know if this would be the case, but we raise a decision event nonetheless
        getRun().getMPA().getEventFactory().raiseDecisionEvent();
    }

    private final DuplicateEventCleaner<DecisionEvent> mDecisionCleaner = new DuplicateEventCleaner<>(
                                                                                DecisionEvent.class,
                                                                                0.01);

}
