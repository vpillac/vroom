package vroom.optimization.online.jmsa.vrp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import vroom.common.modeling.dataModel.Arc;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IPlanningPeriod;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.ObserverManager;
import vroom.common.utilities.Update;
import vroom.common.utilities.Utilities;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * Creation date: Apr 29, 2010 - 10:59:22 AM<br/>
 * <code>VRPInstance</code>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 2.0
 */
public class MSAVRPInstance implements IInstance, IVRPInstance, IObservable {

    /** The started state of vehicles */
    private final boolean[]                      mVehicleStartedStates;
    /** The stopped state of vehicles */
    private final boolean[]                      mVehicleStoppedStates;
    /** The current cost of each vehicle */
    private final double[]                       mVehicleCosts;

    /** A mapping between requests and their id */
    private final Map<Integer, VRPActualRequest> mAllRequests;
    /** A per vehicle list of the assigned visits */
    private final LinkedList<?>[]                mAssignedVisits;
    /** An overall list of assigned requests */
    private final LinkedList<VRPActualRequest>   mAllAssignedRequests;
    /** A per vehicle list of the assigned requests */
    private final LinkedList<?>[]                mAssignedRequests;
    /** An array containing the shrunk node associated with each vehicle */
    private final VRPShrunkRequest[]             mShrunkRequests;
    /** A list of the pending requests */
    private final Set<VRPActualRequest>          mPendingRequests;

    private final Set<IArc>                      mArcs;

    /** A mapping of the assigned status of requests */
    private final Map<VRPActualRequest, Boolean> mAssignedStatus;
    /** A mapping of the served status of requests */
    private final Map<VRPActualRequest, Boolean> mServedStatus;

    /** The underlying instance **/
    private final DynamicInstance                mInstance;

    /** The global parameters for the msa procedure */
    private final MSAGlobalParameters            mParameters;

    /** A cost helper wrapper used to handle shrunk nodes */
    @SuppressWarnings("unused")
    private final MSAVRPCostHelperWrapper        mCostHelper;

    /**
     * Getter for the underlying instance.
     * <p>
     * Should not be modified directly
     * </p>
     * 
     * @return the underlying instance.
     */
    public DynamicInstance getInstance() {
        return mInstance;
    }

    /**
     * @param instance
     *            the wrapped instance
     */
    public MSAVRPInstance(DynamicInstance instance, MSAGlobalParameters parameters) {
        super();
        mObsHandler = new ObserverManager(this);
        mLock = new ExtendedReentrantLock();

        mCostHelper = new MSAVRPCostHelperWrapper();

        acquireLock();

        mParameters = parameters;

        mInstance = instance;
        mVehicleStartedStates = new boolean[instance.getFleet().size()];
        mVehicleStoppedStates = new boolean[instance.getFleet().size()];
        mVehicleCosts = new double[getFleet().size()];

        mAllRequests = new HashMap<Integer, VRPActualRequest>();

        mPendingRequests = new HashSet<VRPActualRequest>();

        mAllAssignedRequests = new LinkedList<VRPActualRequest>();
        mAssignedRequests = new LinkedList<?>[instance.getFleet().size()];
        mShrunkRequests = new VRPShrunkRequest[instance.getFleet().size()];

        for (int v = 0; v < mShrunkRequests.length; v++) {
            mShrunkRequests[v] = new VRPShrunkRequest(instance.getFleet().getVehicle(v), this);
        }

        mServedStatus = new HashMap<VRPActualRequest, Boolean>();
        mAssignedStatus = new HashMap<VRPActualRequest, Boolean>();

        mAssignedVisits = new LinkedList<?>[instance.getFleet().size()];
        for (int v = 0; v < mAssignedVisits.length; v++) {
            mAssignedVisits[v] = new LinkedList<NodeVisit>();
            mAssignedRequests[v] = new LinkedList<VRPActualRequest>();
        }

        mArcs = new HashSet<IArc>();

        updateDepotVisits();

        // Add the instance node visits as requests
        for (INodeVisit visit : getInstance().getNodeVisits()) {
            addNodeVisitInternal(visit);
        }

        // Register the depots as request (used when building a distinguished
        // plan)
        for (INodeVisit depot : getDepotsVisits()) {
            VRPActualRequest depotReq = (VRPActualRequest) depot;

            VRPActualRequest oldValue = mAllRequests.put(depotReq.getID(), depotReq);
            if (oldValue != null && !oldValue.equals(depotReq)) {
                MSALogging.getSetupLogger().warn("MSAVRPInstance.init: request id collision %s - %s", depotReq,
                        oldValue);
            }

        }

        releaseLock();
    }

    @Override
    public String getName() {
        return getInstance().getName();
    }

    @Override
    public boolean isResourceStarted(int resourceId) {
        return mVehicleStartedStates[resourceId];
    }

    @Override
    public boolean isResourceStopped(int resourceId) {
        return mVehicleStoppedStates[resourceId];
    }

    @Override
    public void setResourceStarted(int resourceId, Object param) {
        checkLock();

        mVehicleStartedStates[resourceId] = true;

        if (param instanceof INodeVisit && ((INodeVisit) param).isDepot()) {
            // Add the depot to the vehicle's served requests list
            VRPActualRequest req = new VRPActualRequest((INodeVisit) param);
            getAssignedRequestsInternal(resourceId).add(req);
            // getShrunkRequest(resourceId).shrunkRequest(req);
            getShrunkRequest(resourceId).markAsServed();
            mServedStatus.put(req, true);
        }
    }

    @Override
    public void setResourceStopped(int resourceId, Object param) {
        checkLock();

        mVehicleStoppedStates[resourceId] = true;

        if (param instanceof INodeVisit && ((INodeVisit) param).isDepot()) {
            // Add the depot to the vehicle's served requests list
            VRPActualRequest prev = getLastAssignedRequest(resourceId);
            VRPActualRequest req = new VRPActualRequest((INodeVisit) param);
            getAssignedRequestsInternal(resourceId).add(req);
            getShrunkRequest(resourceId).shrunkRequest(req);
            mServedStatus.put(req, true);
            // Update the cost
            // Update the vehicle costs
            if (prev != null) {
                mVehicleCosts[resourceId] += getCost(prev, ((INodeVisit) param), getFleet().getVehicle(resourceId));
            }

            mObsHandler.notifyObservers(new RequestUpdate(resourceId, req, prev, RequestUpdate.Type.ASSIGNED));
        }
    }

    @Override
    public boolean assignRequestToResource(IActualRequest request, int resourceId) {
        checkLock();

        VRPActualRequest vrpReq = (VRPActualRequest) request;

        // Check if the request has already been assigned
        if (isRequestAssigned(vrpReq)) {
            MSALogging
                    .getProcedureLogger()
                    .warn("MSAVRPInstance.assignRequestToResource: Error - the request has already been assigned (req:%s v:%s)",
                            vrpReq, resourceId);
            return false;
        }

        // Check if the last request to which the vehicle was assigned has
        // actually been served
        if (!getShrunkRequest(resourceId).isServed()) {
            MSALogging
                    .getProcedureLogger()
                    .warn("MSAVRPInstance.assignRequestToResource: Error - the vehicle has not served its last request (req:%s v:%s)",
                            vrpReq, resourceId);

            return false;
        }

        if (!vrpReq.isDepot() && !mPendingRequests.contains(vrpReq)) {
            MSALogging
                    .getProcedureLogger()
                    .warn("MSAVRPInstance.assignRequestToResource: Error - the request doesnt appear in the pending list (req:%s v:%s)",
                            vrpReq, resourceId);

            return false;
        }

        // Set the assigned flag
        mAssignedStatus.put(vrpReq, Boolean.TRUE);

        // Add the request to the vehicle's served requests list
        VRPActualRequest prev = getLastAssignedRequest(resourceId);
        getAssignedRequestsInternal(resourceId).add(vrpReq);
        getShrunkRequest(resourceId).shrunkRequest(vrpReq);

        // Add the request to the global served requests list
        mAllAssignedRequests.add(vrpReq);

        // Remove the request from the pending requests list
        mPendingRequests.remove(vrpReq);

        // Remove the corresponding node visit from the underlying instance
        getInstance().nodeVisited(vrpReq.getNodeVisit());

        mObsHandler.notifyObservers(new RequestUpdate(resourceId, vrpReq, prev, RequestUpdate.Type.ASSIGNED));

        return true;
    }

    @Override
    public VRPActualRequest getNodeVisit(int requestId) {
        VRPActualRequest req = mAllRequests.get(requestId);

        return req != null && req.isDepot() ? req.clone() : req;
    }

    @Override
    public List<VRPActualRequest> getPendingRequests() {
        return new ArrayList<VRPActualRequest>(mPendingRequests);
    }

    public Set<VRPActualRequest> getPendingRequestsSet() {
        return mPendingRequests;
    }

    @Override
    public List<VRPActualRequest> getServedRequests() {
        return new ArrayList<VRPActualRequest>(mAllAssignedRequests);
    }

    @Override
    public List<VRPActualRequest> getServedRequests(int resourceId) {
        return new ArrayList<VRPActualRequest>(getAssignedRequestsInternal(resourceId));
    }

    @Override
    public boolean requestReleased(IActualRequest request) {
        boolean b = getInstance().addRequest(((VRPActualRequest) request).getParentRequest());

        if (b) {
            addNodeVisitInternal((VRPActualRequest) request);
            mObsHandler.notifyObservers(new RequestUpdate(-1, (VRPActualRequest) request, null,
                    RequestUpdate.Type.ADDED));
        }

        return b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#addRequests(java.util.
     * Collection )
     */
    @Override
    public boolean addRequests(Collection<IVRPRequest> requests) {
        boolean b = true;
        for (IVRPRequest request : requests) {
            b &= addRequest(request);
        }
        return b;
    }

    /**
     * Add a request to the data structures of this instance
     * 
     * @param request
     */
    protected void addNodeVisitInternal(INodeVisit visit) {
        checkLock();
        VRPActualRequest request;
        if (visit instanceof VRPActualRequest) {
            request = (VRPActualRequest) visit;
        } else {
            request = mParameters.newInstance(VRPParameterKeys.ACTUAL_REQUEST_CLASS, visit);
        }
        // Adding arcs with the depots
        for (INodeVisit depot : getDepotsVisits()) {
            addArc(depot, request);
        }
        // Adding arcs with other nodes
        for (INodeVisit node : mAllRequests.values()) {
            addArc(node, request);
        }

        VRPActualRequest oldValue = mAllRequests.put(request.getID(), request);

        if (oldValue != null && !oldValue.equals(request)) {
            MSALogging.getSetupLogger().warn("MSAVRPInstance.addNodeVisitInternal: request id collision %s - %s",
                    request, oldValue);
        }

        mPendingRequests.add(request);

        mAssignedStatus.put(request, Boolean.FALSE);
        mServedStatus.put(request, Boolean.FALSE);
    }

    @Override
    public boolean markRequestAsServed(IActualRequest request, int resourceId) {
        checkLock();

        VRPActualRequest vrpReq = (VRPActualRequest) request;

        // Check that the request has previously been assigned
        if (!isRequestAssigned(vrpReq)) {
            MSALogging
                    .getProcedureLogger()
                    .warn("MSAVRPInstance.markRequestAsServed: Error - the request has not been previously assigned (req:%s v:%s)",
                            vrpReq, resourceId);
            return false;
        }

        // Check that the request has not already been marked as served
        if (isRequestServed(vrpReq)) {
            MSALogging
                    .getProcedureLogger()
                    .warn("MSAVRPInstance.markRequestAsServed: Error - the request has already been marked as served (req:%s v:%s)",
                            vrpReq, resourceId);
            return false;
        }

        // Check that the request is the last that was assigned to the resource
        if (getAssignedRequestsInternal(resourceId).isEmpty() || !getLastAssignedRequest(resourceId).equals(vrpReq)) {
            MSALogging
                    .getProcedureLogger()
                    .warn("MSAVRPInstance.markRequestAsServed: Error - the request is not the last request assign to the vehicle (req:%s lastReq:%s)",
                            vrpReq,
                            getAssignedRequestsInternal(resourceId) == null ? null : getAssignedRequestsInternal(
                                    resourceId).getLast());
            return false;

        }

        // Update the served status
        mServedStatus.put(vrpReq, Boolean.TRUE);
        getShrunkRequest(resourceId).markAsServed();

        // Update the vehicle costs
        int L = getServedRequests(resourceId).size();
        if (L > 1) {
            mVehicleCosts[resourceId] += getCost(getAssignedRequestsInternal(resourceId).get(L - 2),
                    getAssignedRequestsInternal(resourceId).get(L - 1), getFleet().getVehicle(resourceId));
        }

        mObsHandler.notifyObservers(new RequestUpdate(resourceId, vrpReq, null, RequestUpdate.Type.SERVED));

        return true;
    }

    /**
     * Getter for the shrunk request associated with a vehicle
     * 
     * @param resouceId
     *            the id of the vehicle
     * @return the shrunk request representing the current state of the vehicle
     */
    public VRPShrunkRequest getShrunkRequest(int resouceId) {
        return mShrunkRequests[resouceId];
    }

    /**
     * @param resourceId
     * @return the list containing the requests assigned to the specified resource
     */
    @SuppressWarnings("unchecked")
    private LinkedList<VRPActualRequest> getAssignedRequestsInternal(int resourceId) {
        return (LinkedList<VRPActualRequest>) mAssignedRequests[resourceId];
    }

    /**
     * @param resourceId
     * @return the last request assigned to the specified resource
     */
    public VRPActualRequest getLastAssignedRequest(int resourceId) {
        if (getAssignedRequestsInternal(resourceId).isEmpty()) {
            return null;
        } else {
            return getAssignedRequestsInternal(resourceId).getLast();
        }
    }

    /**
     * @param request
     *            the considered request
     * @return <code>true</code> if a resource has been assigned to <code>request</code>
     */
    public boolean isRequestAssigned(VRPActualRequest request) {
        if (request == null) {
            return true;
        } else if (!mAssignedStatus.containsKey(request)) {
            mAssignedStatus.put(request, Boolean.FALSE);
            return false;
        } else {
            return mAssignedStatus.get(request);
        }
    }

    /**
     * Served status of a request
     * 
     * @param request
     *            the considered request
     * @return <code>true</code> if <code>request</code> has been served by a vehicle
     */
    public boolean isRequestServed(VRPActualRequest request) {
        if (request == null) {
            return true;
        } else if (!mServedStatus.containsKey(request)) {
            mServedStatus.put(request, Boolean.FALSE);
            return false;
        } else {
            return mServedStatus.get(request);
        }
    }

    /**
     * Getter for the vehicle load
     * <p/>
     * A vehicle compartment load is updated whenever a request is marked as served by this vehicle
     * 
     * @param vehicleId
     *            the considered vehicle id
     * @param compartment
     *            the considered compartment
     * @return the current load of the specified compartment of the given vehicle
     * @see #markRequestAsServed(IActualRequest, int)
     * @see #getCurrentLoads(int)
     */
    public double getCurrentLoad(int vehicleId, int compartment) {
        return getShrunkRequest(vehicleId).getDemand(compartment);
    }

    /**
     * Getter for the vehicle load for all compartments
     * <p/>
     * A vehicle compartment load is updated whenever a request is marked as served by this vehicle
     * 
     * @param vehicleId
     *            the considered vehicle id
     * @return the current load of the specified vehicle
     * @see #markRequestAsServed(IActualRequest, int)
     * @see #getCurrentLoad(int, int)
     */
    public double[] getCurrentLoads(int vehicleId) {
        return getShrunkRequest(vehicleId).getDemands();
    }

    /**
     * @param vehicleId
     * @return
     */
    public double getCurrentCost(int vehicleId) {
        return mVehicleCosts[vehicleId];
    }

    /**
     * Getter for the current mSolution.
     * <p/>
     * This method will return a {@link Solution} containing the current (or final is the MSA procedure is terminated)
     * routes associated with each vehicle.
     * 
     * @return the current mSolution
     */
    @Override
    public Solution<ArrayListRoute> getCurrentSolution() {
        Solution<ArrayListRoute> solution = new Solution<ArrayListRoute>(getInstance());

        for (int r = 0; r < mAssignedRequests.length; r++) {
            ArrayListRoute route = new ArrayListRoute(solution, getFleet().getVehicle(r));
            for (VRPActualRequest node : getAssignedRequestsInternal(r)) {
                route.appendNode(node);
            }
            solution.addRoute(route);
        }

        return solution;
    }

    // ------------------------------------
    // ILockable interface implementation
    // ------------------------------------
    /** A lock to be used by this instance */
    private final ExtendedReentrantLock mLock;

    @Override
    public boolean tryLock(long timeout) {
        try {
            return getLockInstance().tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void acquireLock() {
        try {
            if (!getLockInstance().tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
                throw new IllegalStateException(String.format(
                        "Unable to acquire lock on this instance of %s (%s) after %s %s, owner: %s", this.getClass()
                                .getSimpleName(), hashCode(), TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT, getLockInstance()
                                .getOwnerName()));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format("Unable to acquire lock on this instance of %s (%s)", this
                    .getClass().getSimpleName(), hashCode()), e);
        }
        ;
    }

    @Override
    public void releaseLock() {
        mLock.unlock();
    }

    @Override
    public boolean isLockOwnedByCurrentThread() {
        return mLock.isHeldByCurrentThread();
    }

    @Override
    public ExtendedReentrantLock getLockInstance() {
        return mLock;
    }

    private void checkLock() throws ConcurrentModificationException {
        if (!isLockOwnedByCurrentThread()) {
            throw new ConcurrentModificationException(String.format(
                    "The current thread (%s) does not have the lock on this object", Thread.currentThread()));
        }
    }

    // ------------------------------------

    // ------------------------------------
    // IVRPInstance interface implementation
    // ------------------------------------
    private Set<INodeVisit> mDepotVisits;

    protected void updateDepotVisits() {
        mDepotVisits = new HashSet<INodeVisit>();
        for (INodeVisit n : getInstance().getDepotsVisits()) {
            mDepotVisits.add(new VRPActualRequest(n));
        }
    }

    @Override
    public boolean addRequest(IVRPRequest request) {
        boolean b = getInstance().addRequest(request);

        if (b) {
            if (request instanceof INodeVisit) {
                addNodeVisitInternal((INodeVisit) request);
            } else {
                for (INodeVisit visit : getInstance().getNodeVisits(request)) {
                    addNodeVisitInternal(visit);
                }
            }
        }
        return b;
    }

    @Override
    public double getCost(INodeVisit origin, INodeVisit destination) {
        return getInstance().getCost(origin, destination);
    }

    @Override
    public double getCost(INodeVisit origin, INodeVisit destination, Vehicle vehicle) {
        return getInstance().getCost(origin, destination, vehicle);
    }

    @Override
    public Depot getDepot(int depotId) {
        return getInstance().getDepot(depotId);
    }

    @Override
    public int getDepotCount() {
        return getInstance().getDepotCount();
    }

    @Override
    public Fleet<?> getFleet() {
        return getInstance().getFleet();
    }

    @Override
    public double getInsertionCost(INodeVisit node, INodeVisit pred, INodeVisit succ, Vehicle vehicle) {
        return getInstance().getInsertionCost(node, pred, succ, vehicle);
    }

    @Override
    public Set<INodeVisit> getNodeVisits() {
        return Utilities.convertToSet(getPendingRequests());
    }

    @Override
    public Set<IArc> getArcs() {
        return mArcs;
    }

    @Override
    public IPlanningPeriod getPlanningPeriod() {
        return getInstance().getPlanningPeriod();
    }

    @Override
    public int getRequestCount() {
        return getInstance().getRequestCount();
    }

    @Override
    public List<IVRPRequest> getRequests() {
        ArrayList<IVRPRequest> req = new ArrayList<IVRPRequest>(mPendingRequests.size());
        for (VRPRequest r : mPendingRequests) {
            req.add(r.getParentRequest());
        }
        return req;
    }

    @Override
    public IVRPRequest getRequest(int id) {
        return mAllRequests.get(id).getParentRequest();
    }

    @Override
    public VehicleRoutingProblemDefinition getRoutingProblem() {
        return getInstance().getRoutingProblem();
    }

    @Override
    public boolean isSymmetric() {
        return getInstance().isSymmetric();
    }

    @Override
    public boolean removeRequest(IVRPRequest request) {
        boolean b = getInstance().removeRequest(request);
        if (b) {
            mObsHandler.notifyObservers(new RequestUpdate(-1, (VRPActualRequest) request, null,
                    RequestUpdate.Type.REMOVED));
        }
        return b;
    }

    @Override
    public void setCostHelper(CostCalculationDelegate costHelper) {
        getInstance().setCostHelper(costHelper);
    }

    @Override
    public void setDepots(List<Depot> depots) {
        getInstance().setDepots(depots);
        updateDepotVisits();
    }

    @Override
    public void setFleet(Fleet<?> fleet) {
        getInstance().setFleet(fleet);
    }

    @Override
    public void setPlanningPeriod(IPlanningPeriod planningPeriod) {
        getInstance().setPlanningPeriod(planningPeriod);
    }

    @Override
    public void setSymmetric(boolean symmetric) {
        getInstance().setSymmetric(symmetric);
    }

    @Override
    public INodeVisit[] getNodeVisits(IVRPRequest request) {
        return getInstance().getNodeVisits(request);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getDepotsVisits()
     */
    @Override
    public Set<INodeVisit> getDepotsVisits() {
        return mDepotVisits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getCostHelper()
     */
    @Override
    public CostCalculationDelegate getCostDelegate() {
        // return mCostHelper;
        return getInstance().getCostDelegate();
    }

    /**
     * Add the arc(s) between node1 and node2. If the instance is {@link #isSymmetric() symmetric} then only one arc
     * (node1,node2) is added, otherwise both are added.
     * 
     * @param node1
     * @param node2
     */
    private void addArc(INodeVisit node1, INodeVisit node2) {
        if (node1.compareTo(node2) > 0) {
            INodeVisit tmp = node1;
            node1 = node2;
            node2 = tmp;
        }

        if (!Utilities.equal(node1, node2)) {
            mArcs.add(new Arc(node1, node2, getCostDelegate().getDistance(node1, node2), !isSymmetric()));
            if (!isSymmetric()) {
                mArcs.add(new Arc(node2, node1, getCostDelegate().getDistance(node2, node1), !isSymmetric()));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.utilities.dataModel.IObjectWithID#getID()
     */
    @Override
    public int getID() {
        return getInstance().getID();
    }

    public class MSAVRPCostHelperWrapper extends CostCalculationDelegate {

        @Override
        public double getDistance(Node origin, Node destination) {
            return getDistanceInternal(origin, destination);
        }

        @Override
        public double getDistance(INodeVisit origin, INodeVisit destination) {
            double cost = 0;
            if (origin instanceof VRPShrunkRequest) {
                cost = ((VRPShrunkRequest) origin).getAggregatedCost();
                cost /= ((VRPShrunkRequest) origin).getVehicle().getVariableCost();
                origin = ((VRPShrunkRequest) origin).getNodeVisit();
            } else if (destination instanceof VRPShrunkRequest) {
                destination = ((VRPShrunkRequest) destination).getFirstNode();
            }
            return cost + super.getDistance(origin, destination);
        }

        @Override
        protected double getDistanceInternal(Node origin, Node destination) {
            return getInstance().getCostDelegate().getDistance(origin, destination);
        }

        @Override
        public double getCost(INodeVisit origin, INodeVisit destination, Vehicle vehicle) {
            double cost = 0;
            if (origin instanceof VRPShrunkRequest) {
                cost = ((VRPShrunkRequest) origin).getAggregatedCost();
                origin = ((VRPShrunkRequest) origin).getNodeVisit();
            } else if (destination instanceof VRPShrunkRequest) {
                destination = ((VRPShrunkRequest) destination).getFirstNode();
            }
            return cost + super.getCost(origin, destination, vehicle);
        }

        @Override
        public double getCost(INodeVisit origin, INodeVisit destination) {
            double cost = 0;
            if (origin instanceof VRPShrunkRequest) {
                cost = ((VRPShrunkRequest) origin).getAggregatedCost();
                origin = ((VRPShrunkRequest) origin).getNodeVisit();
            } else if (destination instanceof VRPShrunkRequest) {
                destination = ((VRPShrunkRequest) destination).getFirstNode();
            }
            return cost + super.getCost(origin, destination);
        }

        @Override
        public String getDistanceType() {
            return getCostDelegate().getDistanceType();
        }

        @Override
        protected void precisionChanged() {
            throw new UnsupportedOperationException();
        }
    }

    private final ObserverManager mObsHandler;

    @Override
    public void addObserver(IObserver o) {
        mObsHandler.addObserver(o);
    }

    @Override
    public void removeObserver(IObserver o) {
        mObsHandler.removeObserver(o);
    }

    @Override
    public void removeAllObservers() {
        mObsHandler.removeAllObservers();
    }

    public static class RequestUpdate implements Update {

        public enum Type {
            ASSIGNED, SERVED, ADDED, REMOVED
        };

        /** The resource **/
        private final int mResource;

        /**
         * Getter for The resource
         * 
         * @return the value of resource
         */
        public int getResource() {
            return mResource;
        }

        /** The request **/
        private final VRPActualRequest mRequest;

        /**
         * Getter for The request
         * 
         * @return the value of request
         */
        public VRPActualRequest getRequest() {
            return mRequest;
        }

        /** The previous request **/
        private final VRPActualRequest mPrevRequest;

        /**
         * Getter for The previous request
         * 
         * @return the value of prevRequest
         */
        public VRPActualRequest getPrevRequest() {
            return mPrevRequest;
        }

        /** The type of update **/
        private final Type mUpdateType;

        /**
         * Getter for The type of update
         * 
         * @return the value of updateType
         */
        public Type getUpdateType() {
            return mUpdateType;
        }

        @Override
        public String getDescription() {
            return String.format("%s assigned to %s", mResource, mRequest);
        }

        public RequestUpdate(int resource, VRPActualRequest request, VRPActualRequest prevRequest, Type updateType) {
            super();
            mResource = resource;
            mRequest = request;
            mPrevRequest = prevRequest;
            mUpdateType = updateType;
        }

    }

    @Override
    public List<Depot> getDepots() {
        return mInstance.getDepots();
    }

}
