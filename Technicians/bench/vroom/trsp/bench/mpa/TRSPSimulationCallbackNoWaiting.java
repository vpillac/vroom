/**
 * 
 */
package vroom.trsp.bench.mpa;

import vroom.common.utilities.Utilities;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.events.NewRequestEvent;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.utils.MSASimulationCallback;
import vroom.optimization.online.jmsa.utils.MSASimulator.ResourceState;
import vroom.optimization.online.jmsa.utils.MSASimulator.ResourceStates;
import vroom.trsp.datamodel.ITRSPNode;
import vroom.trsp.datamodel.ITRSPNode.NodeType;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.Technician;
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
public class TRSPSimulationCallbackNoWaiting extends MSASimulationCallback {

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

    public TRSPSimulationCallbackNoWaiting(DTRSPRunMPA run) {
        super(run.getMSASimulator());
        mRun = run;
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
        TRSPLogging
                .getSimulationLogger()
                .info("TRSPSimulationCallback.newDistinguishedSolution: new distinguished solution, assigning requests to idle technicians (%s)",
                        newSol);
        if (newSol == null) {
            if (mAllResourcesStopped)
                return;
            // return;
            throw new IllegalStateException("Distinguished solution should not be null");
        }
        boolean oneAssign = false;
        boolean allIdle = true;
        boolean oneIdle = false;
        for (Technician t : getInstance().getFleet()) {
            ResourceState state = getSimulator().getState(t.getID());
            if (state.getState() == ResourceStates.IDLE) {
                ITRSPNode n = (ITRSPNode) newSol.getNextRequest(t.getID());
                oneIdle = true;
                if (n != null) {
                    if (n.getType() != NodeType.HOME
                            || getInstance().getUnservedReleasedRequests().isEmpty()) {
                        // The next node is the technician home: wait until all requests are served
                        // TODO add max tour duration?

                        TRSPLogging
                                .getSimulationLogger()
                                .info("TRSPSimulationCallback.newDistinguishedSolution: assigning %s to technician %s (%s)",
                                        n.getDescription(), t.getID(), state);
                        // getInstance().assignRequestToResource((TRSPRequest) r, t.getID());
                        getRun().getMPA().getEventFactory().raiseRequestAssignedEvent(t.getID(), n);
                        oneAssign = true;
                    }
                }
            } else {
                allIdle = false;
            }
        }
        if (allIdle && !oneAssign && !getInstance().getUnservedReleasedRequests().isEmpty())
            throw new IllegalStateException(
                    "All technicicans are idle but no assignment was made, unserved requests: "
                            + Utilities.toShortString(getInstance().getUnservedReleasedRequests()));
    }

    @Override
    protected void resourceStarted(MSACallbackEvent cbEvent, ResourceEvent e) {
        super.resourceStarted(cbEvent, e);
        // Schedule a decision event
        TRSPLogging
                .getSimulationLogger()
                .info("TRSPSimulationCallback.resourceStarted: technicians %s available, raising decision event",
                        e.getResourceId());
        // getInstance().getSimulator().appendToCurrentSolution(
        // getInstance().getTechnician(e.getResourceId()).getHome().getID(), e.getResourceId());
        getInstance().markRequestAsServed(
                getInstance().getTRSPNode(
                        getInstance().getFleet().getVehicle(e.getResourceId()).getHome().getID()),
                e.getResourceId());
        getRun().getMPA().getEventFactory().raiseDecisionEvent();
    }

    @Override
    protected void resourceStopped(MSACallbackEvent cbEvent, ResourceEvent e) {
        super.resourceStopped(cbEvent, e);

        // getInstance().getSimulator().assignNodeToTechnician(
        // e.getResourceId(),
        // getInstance().getTRSPNode(
        // getInstance()
        // .getHomeDuplicate(getInstance().getTechnician(e.getResourceId()).getHome().getID())));

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
        }
    }

    @Override
    protected void requestAssigned(MSACallbackEvent cbEvent, ResourceEvent e) {
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
    }

    @Override
    protected void startOfService(MSACallbackEvent cbEvent, ResourceEvent e) {
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
        getSimulator().scheduleEvent(
                ResourceEvent.newEndOfServiceEvent(time, getRun().getMPA().getEventFactory(),
                        e.getResourceId(), n, null));
    }

    @Override
    protected void endOfService(MSACallbackEvent cbEvent, ResourceEvent e) {
        super.endOfService(cbEvent, e);
        ITRSPNode n = (ITRSPNode) e.getRequest();
        if (n.getID() == getInstance().getHomeDuplicate(
                getInstance().getTechnician(e.getResourceId()).getHome().getID())) {
            // The served request is the technician home, schedule resource stop
            TRSPLogging
                    .getSimulationLogger()
                    .info("TRSPSimulationCallback.endOfService: %s served by %s, raising resource stop event (technician returned to home)",
                            n.getDescription(), e.getResourceId());
            getSimulator().scheduleEvent(
                    ResourceEvent.newStopServiceEvent(getSimulator().simulationTime(), getRun()
                            .getMPA().getEventFactory(), e.getResourceId(), null));
        } else {
            TRSPLogging.getSimulationLogger().info(
                    "TRSPSimulationCallback.endOfService: %s served by %s, raising decision event",
                    n.getDescription(), e.getResourceId());
            getRun().getMPA().getEventFactory().raiseDecisionEvent();
        }
    }

    @Override
    protected void newRequest(MSACallbackEvent event, NewRequestEvent e) {
        super.newRequest(event, e);
    }

    @Override
    protected void beforeExecute(MSACallbackEvent event) {
        super.beforeExecute(event);
        getSimulator().pause();
    }

    @Override
    protected void afterExecture(MSACallbackEvent event) {
        super.afterExecture(event);
        getSimulator().resume();
    }
}
