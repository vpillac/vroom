/**
 * 
 */
package vroom.trsp.sim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import vroom.common.utilities.IntegerSet;
import vroom.common.utilities.Utilities;
import vroom.trsp.datamodel.ITRSPNode;
import vroom.trsp.datamodel.ITRSPNode.NodeType;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPSimulator</code>
 * <p>
 * Creation date: Nov 8, 2011 - 4:53:20 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPSimulator extends Observable {

    /**
     * The cutoff times depending on the instance group
     */
    public static final Map<String, Double> CUTOFF_TIMES = new HashMap<>();
    static {
        CUTOFF_TIMES.put("C1", 0.380);
        CUTOFF_TIMES.put("C2", 0.509);
        CUTOFF_TIMES.put("R1", 0.357);
        CUTOFF_TIMES.put("R2", 0.419);
        CUTOFF_TIMES.put("RC1", 0.321);
        CUTOFF_TIMES.put("RC2", 0.400);
    }

    public static enum NotificationType {
        NODE_ASSIGNED, NODE_SERVED, REQ_REJECTED, REQ_RELEASED, NODE_SET_AS_CURRENT, TIME_CHANGED
    };

    private final TRSPInstance            mInstance;

    /** A list of the requests that have already been released */
    private final IntegerSet              mReleasedRequests;
    private final Set<Integer>            mReleasedRequestsView;

    /** A list of the unserved requests that have already been released */
    private final IntegerSet              mUnservedReleasedRequests;
    private final Set<Integer>            mUnservedReleasedRequestsView;

    private final IntegerSet              mServedRequests;
    private final Set<Integer>            mServedRequestsView;

    private final IntegerSet              mRejectedRequests;
    private final Set<Integer>            mRejectedRequestsView;

    private final LinkedList<TRSPRequest> mReleaseQueue;
    private final List<TRSPRequest>       mReleaseQueueView;

    private double                        mClock;

    private final DTRSPSolution           mCurrentSolution;

    private final ITRSPNode[]             mAssignedNodes;
    private final ITRSPNode[]             mCurrentNode;

    /** a flag set to {@code true} when the simulator is in a static setting **/
    private boolean                       mStaticSetting;

    private final TRSPGlobalParameters    mParameters;

    /**
     * Getter for a flag set to {@code true} when the simulator is in a static setting
     * 
     * @return {@code true} if the simulator is in a static setting
     */
    public boolean isStaticSetting() {
        return this.mStaticSetting;
    }

    /**
     * Creates a new <code>TRSPSimulator</code>
     * 
     * @param instance
     * @param costDelegate
     * @param params
     */
    public TRSPSimulator(TRSPInstance instance, TRSPCostDelegate costDelegate,
            TRSPGlobalParameters params) {
        super();
        mParameters = params;
        mStaticSetting = false;
        mInstance = instance;
        mServedRequests = new IntegerSet(mInstance.getMaxId());
        mServedRequestsView = Collections.unmodifiableSet(mServedRequests);
        mRejectedRequests = new IntegerSet(mInstance.getMaxId());
        mRejectedRequestsView = Collections.unmodifiableSet(mRejectedRequests);
        mReleaseQueue = new LinkedList<TRSPRequest>();
        mReleaseQueueView = Collections.unmodifiableList(mReleaseQueue);
        mReleasedRequests = new IntegerSet(mInstance.getMaxId());
        mReleasedRequestsView = Collections.unmodifiableSet(mReleasedRequests);
        mUnservedReleasedRequests = new IntegerSet(mInstance.getMaxId());
        mUnservedReleasedRequestsView = Collections.unmodifiableSet(mUnservedReleasedRequests);

        for (TRSPRequest r : mInstance.getRequests()) {
            if (r.getReleaseDate() >= 0) {
                mReleaseQueue.add(r);
            } else {
                mReleasedRequests.add(r.getID());
                mUnservedReleasedRequests.add(r.getID());
            }
        }
        Collections.sort(mReleaseQueue, new Comparator<TRSPRequest>() {
            @Override
            public int compare(TRSPRequest o1, TRSPRequest o2) {
                return Double.compare(o1.getReleaseDate(), o2.getReleaseDate());
            }
        });

        setSimulationTime(-1);

        mAssignedNodes = new ITRSPNode[instance.getFleet().size()];
        mCurrentNode = new ITRSPNode[instance.getFleet().size()];
        mCurrentSolution = new DTRSPSolution(getInstance(), costDelegate);
        for (TRSPRequest r : mReleaseQueue)
            mCurrentSolution.markAsServed(r.getID());
        for (TRSPTour t : mCurrentSolution)
            t.setAutoUpdated(true);
        mCurrentSolution.freeze();

        // TODO Find more robust way of setting the cutoff
        if (!instance.isCVRPTW() && CUTOFF_TIMES.containsKey(instance.getGroup())) {
            params.set(TRSPGlobalParameters.RUN_CUTOFF_TIME, CUTOFF_TIMES.get(instance.getGroup()));
            TRSPLogging.getSimulationLogger().info(
                    "Setting the cutoff time to %4.3f (%.1f)",
                    params.get(TRSPGlobalParameters.RUN_CUTOFF_TIME),
                    instance.getMainDepot().getTimeWindow().endAsDouble()
                            * params.get(TRSPGlobalParameters.RUN_CUTOFF_TIME));
        } else {
            params.set(TRSPGlobalParameters.RUN_CUTOFF_TIME, Double.POSITIVE_INFINITY);
        }
    }

    /**
     * Returns the parent instance
     * 
     * @return the parent instance
     */
    public TRSPInstance getInstance() {
        return mInstance;
    }

    /**
     * Returns the currently executed solution
     * 
     * @return the currently executed solution
     */
    public DTRSPSolution getCurrentSolution() {
        return mCurrentSolution;
    }

    /**
     * Setter for <code>clock</code>
     * 
     * @param clock
     *            the clock to set
     */
    private void setSimulationTime(double clock) {
        mClock = clock;
    }

    /**
     * Returns the current simulation time
     * 
     * @return the current simulation time
     */
    public double simulationTime() {
        return mClock;
    }

    /**
     * Advance the simulation time
     * 
     * @param time
     * @throws IllegalArgumentException
     *             if {@code  time} is before than current simulation time
     */
    public void advanceTime(double time) {
        if (time < simulationTime())
            throw new IllegalArgumentException("Time must be greater than current simulation time");
        setSimulationTime(time);
        // Remove requests from the release queue
        while (!mReleaseQueue.isEmpty()
                && mReleaseQueue.peek().getReleaseDate() <= simulationTime())
            mReleaseQueue.pop();
        notifyUpdate(NotificationType.TIME_CHANGED, -1, -1);
    }

    /**
     * Returns a view of the list of unreleased requests
     * 
     * @return a view of the list of unreleased requests
     */
    public List<TRSPRequest> getUnreleasedRequests() {
        return mReleaseQueueView;
    }

    /**
     * Returns the number of unreleased requests
     * 
     * @return the number of unreleased requests
     */
    public int getUnreleasedCount() {
        return mReleaseQueue.size();
    }

    public boolean hasUnreleasedRequests() {
        return !mReleaseQueue.isEmpty();
    }

    /**
     * Returns the next released requests and advance the internal clock to the correct time
     * 
     * @return the next released requests
     */
    public Collection<TRSPRequest> nextRelease() {
        TRSPRequest r = mReleaseQueue.pop();
        setSimulationTime(r.getReleaseDate());

        LinkedList<TRSPRequest> release = new LinkedList<TRSPRequest>();
        release.add(r);
        requestReleased(r);

        while (!mReleaseQueue.isEmpty()
                && mReleaseQueue.peek().getReleaseDate() == simulationTime()) {
            r = mReleaseQueue.pop();
            release.add(r);
            requestReleased(r);
        }

        return release;
    }

    public void requestReleased(TRSPRequest request) {
        mReleaseQueue.remove(request);
        mReleasedRequests.add(request.getID());
        mUnservedReleasedRequests.add(request.getID());
        mCurrentSolution.markAsUnserved(request.getID());
        notifyUpdate(NotificationType.REQ_RELEASED, -1, request.getID());
    }

    /**
     * Returns all the released requests in this instance (i.e. that have been made known to the system).
     * 
     * @return a list containing all the released requests in this instance
     */
    public Set<Integer> getReleasedRequests() {
        return mReleasedRequestsView;
    }

    /**
     * Returns all the unserved released requests in this instance (i.e. that have been made known to the system, and
     * are neither served or rejected).
     * 
     * @return a list containing all the unserved released requests in this instance
     */
    public Set<Integer> getUnservedReleasedRequests() {
        return mUnservedReleasedRequestsView;
    }

    /**
     * Returns {@code true} iif the request has already been served or has been rejected
     * 
     * @param reqId
     * @return {@code true} iif the request has already been served or has been rejected
     */
    public boolean isServedOrRejected(int reqId) {
        return isServed(reqId) || isRejected(reqId);
    }

    /**
     * Returns {@code true} iif the node with id {@code  nodeId} has been served or is currently assigned to a
     * technician.
     * <p>
     * This method is based on the definition of the {@link #getCurrentSolution() current solution}</p<
     * 
     * @param nodeId
     * @return {@code true} iif the node with id {@code  nodeId} has been served or is currently assigned to a
     *         technician.
     */
    public boolean isServedOrAssigned(int nodeId) {
        return getCurrentSolution() != null && getCurrentSolution().getVisitingTour(nodeId) != null;
    }

    /**
     * Returns {@code true} iif the request has been served
     * 
     * @param id
     * @return {@code true} iif the request has been served
     */
    public boolean isServed(int id) {
        return mServedRequests.contains(id);
    }

    /**
     * Returns {@code true} iif the request has been rejected
     * 
     * @param id
     * @return {@code true} iif the request has been rejected
     */
    public boolean isRejected(int id) {
        return mRejectedRequests.contains(id);
    }

    /**
     * Returns the set of served requests
     * 
     * @return the set of served requests
     */
    public Set<Integer> getServedRequests() {
        return mServedRequestsView;
    }

    /**
     * Mark a request as rejected
     * 
     * @param reqId
     */
    public void markAsRejected(int reqId) {
        if (getInstance().isRequest(reqId)) {
            mRejectedRequests.add(reqId);
            mReleasedRequests.remove(reqId);
            mUnservedReleasedRequests.remove(reqId);
            getCurrentSolution().markAsServed(reqId);
            notifyUpdate(NotificationType.REQ_REJECTED, -1, reqId);
        }
    }

    /**
     * Returns the set of rejected requests
     * 
     * @return the set of rejected requests
     */
    public Set<Integer> getRejectedRequests() {
        return mRejectedRequestsView;
    }

    /**
     * Returns {@code true} iif the request has already been released, i.e. made known to the system.
     * 
     * @param reqId
     * @return {@code true} iif the request has already been released
     */
    public boolean isReleased(int reqId) {
        return getReleasedRequests().contains(reqId);
    }

    /**
     * Assign a request to a technician, and append it to the current solution
     * 
     * @param technician
     *            the technician to which {@code  node} will be assigned
     * @param node
     *            the request to be assigned
     * @param arrivalTime
     *            the arrival time at {@code  node}
     * @return the request previously assigned to {@code  technician}
     */
    public ITRSPNode assignNodeToTechnician(int technician, ITRSPNode node, double arrivalTime) {
        ITRSPNode prev = mAssignedNodes[technician];
        mAssignedNodes[technician] = node;
        if (node != null) {
            appendToCurrentSolution(node.getID(), technician, arrivalTime);
            notifyUpdate(NotificationType.NODE_ASSIGNED, technician, node.getID());
        }
        return prev;
    }

    /**
     * Append {@code  node} to the tour of {@code  technician} in the current solution
     * 
     * @param node
     *            the node to be appended
     * @param technician
     *            the id of the considered technician
     * @param arrivalTime
     *            the time at which the appended node will be visited
     */
    private void appendToCurrentSolution(int node, int technician, double arrivalTime) {
        mCurrentSolution.unfreeze();
        // Ensure that earliestArrival is properly set
        TRSPTour tour = mCurrentSolution.getTour(technician);
        tour.appendNode(node, arrivalTime);
        mCurrentSolution.freeze(node, arrivalTime, tour.getEarliestDepartureTime(node));
        mCurrentSolution.markAsServed(node);
        mUnservedReleasedRequests.remove(node);
        mCurrentSolution.freeze();
    }

    /**
     * Returns the node assigned to a technician
     * 
     * @param technician
     * @return the node assigned to {@code  technician}
     */
    public ITRSPNode getAssignedNode(int technician) {
        return mAssignedNodes[technician];
    }

    /**
     * Returns a list of the currently assigned nodes
     * 
     * @return a list of the currently assigned nodes
     */
    public List<ITRSPNode> getAssignedNodes() {
        ArrayList<ITRSPNode> nodes = new ArrayList<>(mAssignedNodes.length);
        for (ITRSPNode n : mAssignedNodes)
            if (n != null)
                nodes.add(n);
        return nodes;
    }

    /**
     * Returns the current (or last known) location of a technician
     * 
     * @param technician
     * @return the current (or last known) location of a technician
     */
    public ITRSPNode getCurrentNode(int technician) {
        return mCurrentNode[technician];
    }

    /**
     * Sets the current location of the technician as the previously assigned location
     * 
     * @param technician
     *            the id of the technician
     * @throws IllegalStateException
     *             if no assigned node is defined for {@code  technician}
     * @return the assigned/current node
     */
    public ITRSPNode setAssignedNodeAsCurrent(int technician) {
        ITRSPNode node = getAssignedNode(technician);
        if (node == null)
            throw new IllegalStateException(String.format("Technician %s has no assigned node",
                    technician));
        mCurrentNode[technician] = node;
        notifyUpdate(NotificationType.NODE_SET_AS_CURRENT, technician, node.getID());
        return node;
    }

    /**
     * Mark the current request as served, and set the assigned node to <code>null</code>
     * 
     * @param technician
     *            the id of the technician
     * @throws IllegalStateException
     *             if no current node is defined for {@code  technician}
     * @return the current/served node
     */
    public ITRSPNode markCurrentRequestAsServed(int technician) {
        ITRSPNode node = getCurrentNode(technician);
        if (node == null)
            throw new IllegalStateException(String.format(
                    "Technician %s has no current node defined", technician));
        if (node.getType() == NodeType.REQUEST) {
            mServedRequests.add(node.getID());
            mUnservedReleasedRequests.remove(node.getID());
        }
        notifyUpdate(NotificationType.NODE_SERVED, technician, node.getID());
        assignNodeToTechnician(technician, null, 0);
        return node;
    }

    /**
     * Update the current solution
     * 
     * @param solution
     *            the solution that is currently executed
     */
    public void updateState(TRSPSolution solution) {
        TRSPLogging.getSimulationLogger().info("Updating the current state - sim time: %.1f",
                simulationTime());

        DTRSPSolution dsol = DTRSPSolution.class.isAssignableFrom(solution.getClass()) ? (DTRSPSolution) solution
                : null;

        for (TRSPTour currentTour : mCurrentSolution) {
            TRSPTour updateTour = solution.getTour(currentTour.getTechnicianId());

            TRSPTourIterator curIt = currentTour.iterator();
            TRSPTourIterator upIt = updateTour.iterator();

            // Check that the current executed portions are the same
            int pred = ITRSPTour.UNDEFINED;
            while (curIt.hasNext()) {
                if (!upIt.hasNext())
                    throw new IllegalStateException(
                            "The updated tour is shorter than the current tour :" + updateTour);
                int cur = curIt.next();
                pred = upIt.next();
                if (cur != pred)
                    throw new IllegalStateException(
                            "The updated tour differs from the current tour :" + updateTour);
                if (currentTour.getEarliestArrivalTime(cur) != updateTour
                        .getEarliestArrivalTime(cur))
                    throw new IllegalStateException(
                            "The updated tour has a different earliest departure for node " + cur);
            }

            // Append the portion that was executed
            // boolean assignNext = false;
            int node = ITRSPTour.UNDEFINED;
            while (upIt.hasNext()) {
                node = upIt.next();
                // if (updateTour.getLatestFeasibleDepartureTime(n) <= simulationTime()) {
                // The latest departure time so that there is no waiting at the next node
                double latestDeparture = pred != ITRSPTour.UNDEFINED ? updateTour
                        .getWaitDepartureTime(pred) : currentTour.getEarliestStartTime();
                boolean departed = latestDeparture <= simulationTime();
                if ((// assignNext ||
                        departed || currentTour.length() == 0) // node should be appended
                        // Do not send a technician home before cutoff
                        && !(pred != ITRSPTour.UNDEFINED
                                && getInstance().getTRSPNode(node).getType().isHome() && !cutoff(simulationTime()))) {

                    // The arrival time at node depending on the latest departure time at the predecessor
                    double arrivalTime = latestDeparture
                            + (pred != ITRSPTour.UNDEFINED ? currentTour.getTravelTime(pred, node)
                                    : 0);

                    assignNodeToTechnician(updateTour.getTechnicianId(),
                            getInstance().getTRSPNode(node), arrivalTime);
                    if (dsol != null) {
                        if (!dsol.isFrozen(node))
                            updateTour.setEarliestArrivalTime(node, arrivalTime);

                        if (pred != ITRSPTour.UNDEFINED)
                            // Update the previous node
                            dsol.freeze(pred, updateTour.getEarliestArrivalTime(pred),
                                    latestDeparture);
                        // Freeze the current node
                        dsol.freeze(node, arrivalTime, updateTour.getEarliestDepartureTime(node));
                    }
                    // if (!departed)
                    // // The vehicle is assigned to node but has not finished servicing it yet
                    // assignNext = false;
                    // else
                    // // We will assign the next node, regardless of its departure date as vehicle may be en route
                    // assignNext = true;
                } else {
                    freezeTechnician(dsol, currentTour.getTechnicianId(), pred);
                    break;
                }
                pred = node;
            }
            int current = currentTour.getLastNode();
            TRSPLogging.getSimulationLogger().info(
                    " Technician %2s is at %3s - next %3s - ed:%7.1f - executed tour: %s (%s)",
                    currentTour.getTechnicianId(), current, updateTour.getSucc(current),
                    currentTour.getEarliestDepartureTime(current), currentTour,
                    TRSPSolutionChecker.INSTANCE.checkTour(currentTour));

        }
    }

    /**
     * Freezes a technician at its current node by setting its earliest departure time
     * 
     * @param updateSolution
     * @param techId
     * @param node
     */
    private void freezeTechnician(DTRSPSolution updateSolution, int techId, int node) {
        if (updateSolution == null)
            return;
        TRSPTour updateTour = updateSolution.getTour(techId);
        int succ = updateTour.getSucc(node);

        double earliestDeparture = Math.max(simulationTime(),
                updateTour.getEarliestDepartureTime(node));

        mCurrentSolution.unfreeze();

        if (!updateSolution.isFrozen(node))
            updateSolution.freeze(node, updateTour.getEarliestArrivalTime(node), earliestDeparture);
        mCurrentSolution.freeze(node, updateTour.getEarliestArrivalTime(node), earliestDeparture);

        String err = TRSPSolutionChecker.INSTANCE.checkTour(updateTour);
        if (!err.isEmpty())
            TRSPLogging.getBaseLogger().warn(err);

        if (succ != ITRSPTour.UNDEFINED) {
            if (!updateSolution.isFrozen(node))
                updateTour.setEarliestArrivalTime(succ,
                        earliestDeparture + updateTour.getTravelTime(node, succ));
            mCurrentSolution.getTour(techId).setEarliestArrivalTime(succ,
                    earliestDeparture + updateTour.getTravelTime(node, succ));
        }

        mCurrentSolution.freeze();
    }

    private void notifyUpdate(NotificationType type, int resource, int request) {
        setChanged();
        notifyObservers(new UpdateNotification(type, resource, request));
    }

    /**
     * Setup this simulator for static a-posteri setting
     */
    public void staticSetup() {
        setSimulationTime(Double.POSITIVE_INFINITY);
        mReleaseQueue.clear();
        mServedRequests.clear();
        mUnservedReleasedRequests.addAll(mReleasedRequests);
        mStaticSetting = true;
        mCurrentSolution.unfreeze();
        mCurrentSolution.clear();
        mCurrentSolution.freeze();
    }

    /**
     * Terminate the simulation and update to the final state
     * 
     * @param solution
     */
    public void terminate(TRSPSolution solution) {
        if (hasUnreleasedRequests())
            throw new IllegalStateException();

        setSimulationTime(Double.POSITIVE_INFINITY);
        updateState(solution);
    }

    /**
     * Returns the simulation time after which technicians will be sent home when idle
     * 
     * @return the simulation time after which technicians will be sent home when idle
     */
    public double getCutoffTime() {
        return getInstance().getMainDepot().getTimeWindow().endAsDouble()
                * mParameters.get(TRSPGlobalParameters.RUN_CUTOFF_TIME);
    }

    /**
     * Returns {@code true} if {@code  simulationTime} is after the cutoff
     * 
     * @param simulationTime
     * @return {@code true} if {@code  simulationTime} is after the cutoff
     */
    public boolean cutoff(double simulationTime) {
        return simulationTime > getCutoffTime();
    }

    @Override
    public String toString() {
        return String.format("Time:%s Served:%s Rejected:%s Unserved:%s Unreleased:%s",
                simulationTime(), Utilities.toShortString(mServedRequestsView),
                Utilities.toShortString(mRejectedRequestsView),
                Utilities.toShortString(mUnservedReleasedRequestsView),
                Utilities.toShortString(mReleaseQueueView));
    }

    /**
     * The class <code>UpdateNotification</code> is used to send information to attached {@link Observer observers} when
     * this instance has been updated
     * <p>
     * Creation date: Mar 27, 2012 - 10:12:41 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class UpdateNotification {
        public final NotificationType type;
        public final int              resource;
        public final int              request;

        /**
         * Creates a new <code>UpdateNotification</code>
         * 
         * @param type
         * @param resource
         * @param request
         */
        public UpdateNotification(NotificationType type, int resource, int request) {
            this.type = type;
            this.resource = resource;
            this.request = request;
        }
    }

}
