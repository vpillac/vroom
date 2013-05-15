/**
 * 
 */
package vroom.optimization.online.jmsa.vrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.NodeInsertion;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.utilities.Constants;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.IDerefenceable;
import vroom.common.utilities.ILockable;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.ObserverProxy;
import vroom.common.utilities.Update;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.ValueUpdate;
import vroom.common.utilities.dataModel.IDHelper;
import vroom.common.utilities.dataModel.IObjectWithID;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.ISampledRequest;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * Creation date: Apr 6, 2010 - 3:03:52 PM<br/>
 * <code>VRPScenarioRoute</code> is an implementation of {@link IRoute} that wrap another instance of {@link IRoute}
 * that represent the <em>variable</em> part of the route, while adding information about the <em>fixed</em> part of the
 * route that correspond to the nodes that have already been visited.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPScenarioRoute implements IRoute<VRPRequest>, ILockable, IObjectWithID, IObserver, Cloneable,
        IDerefenceable {

    private final ObserverProxy mObserverProxy;

    public static enum State {
        NOT_STARTED, STARTED, TERMINATED, DELETED
    };

    /**
     * The wrapped instance of {@link IRoute}
     */
    private final RouteBase  mRoute;

    /** The last request that was fixed */
    private VRPActualRequest mLastFixedRequest;

    /** The served status of the last fixed visit **/
    private boolean          mLastRequestServed;

    /** An unique ID for this scenario */
    private final int        mID;

    /** A flag set to true if this route contains a shrunk node */
    private boolean          mShrunkNode;

    /** The current state of this route **/
    private State            mCurrentState;

    /**
     * Getter for currentState : The current state of this route
     * 
     * @return the value of currentState
     */
    public State getCurrentState() {
        return mCurrentState;
    }

    /**
     * Setter for currentState : The current state of this route
     * 
     * @param currentState
     *            the value to be set for currentState
     */
    public void setCurrentState(State currentState) {
        mCurrentState = currentState;
    }

    /**
     * <code>true</code> if this route should be added as observer each time a request is added
     */
    private boolean mAddAsObserver;

    /**
     * Getter for the <code>addAsObserver</code> flag.
     * 
     * @return <code>true</code> if this route should be added as observer each time a request is added
     */
    public boolean isAddAsObseverEnabled() {
        return mAddAsObserver;
    }

    /**
     * Sets the <code>addAsObserver</code> flag.
     * <p>
     * This flag should be disabled for performance when initializing or optimizing a route
     * </p>
     * <p>
     * Note that setting the <code>addAsObserver</code> flag to <code>false</code> will unregister this instance from
     * all requests, while setting it to <code>true</code> will register it.
     * 
     * @param addAsObserver
     *            <code>true</code> if this route should be added as observer each time a request is added
     */
    public void setAddAsObserver(boolean addAsObserver) {
        checkLock();

        mAddAsObserver = addAsObserver;

        for (VRPRequest i : this) {
            if (mAddAsObserver) {
                i.addObserver(mObserverProxy);
            } else {
                i.removeObserver(mObserverProxy);
            }
        }
    }

    /**
     * Creates a new <code>VRPScenarioRoute</code> based on an {@link ArrayListRoute}
     * <p/>
     * This instance will use the same lock contained in the parent VRPScenario
     * 
     * @param parentScenario
     *            the parent scenario
     * @param vehicle
     *            the vehicle to which this route will be associated
     */
    public VRPScenarioRoute(VRPScenario parentScenario, Vehicle vehicle) {
        this(ArrayListRoute.class, parentScenario, vehicle);

        setAddAsObserver(false);
    }

    /**
     * Creates a new <code>VRPScenarioRoute</code>
     * <p/>
     * This instance will use the same lock contained in the parent VRPScenario
     * 
     * @param routeClass
     *            the type of route to be used as backup for this instance
     * @param parentScenario
     *            the parent scenario
     * @param vehicle
     *            the vehicle to which this route will be associated
     */
    public VRPScenarioRoute(Class<? extends RouteBase> routeClass, VRPScenario parentScenario, Vehicle vehicle) {

        // Optimization for knwon route type
        if (routeClass == ArrayListRoute.class) {
            mRoute = new ArrayListRoute(parentScenario, vehicle);
        } else {
            mRoute = Utilities.newInstance(routeClass, parentScenario, vehicle);
        }
        mLastFixedRequest = null;
        setLastRequestServed(true);
        mID = IDHelper.getNextId(this.getClass());
        mCurrentState = State.NOT_STARTED;

        setAddAsObserver(false);

        mObserverProxy = new ObserverProxy(this);
    }

    /**
     * Creates a new <code>VRPScenarioRoute</code> for cloning operations
     * 
     * @param route
     */
    private VRPScenarioRoute(RouteBase route) {
        mRoute = route;
        mLastFixedRequest = null;
        setLastRequestServed(true);
        mID = IDHelper.getNextId(this.getClass());
        mCurrentState = State.NOT_STARTED;

        setAddAsObserver(false);

        mObserverProxy = new ObserverProxy(this);
    }

    /**
     * Creates a new <code>VRPScenarioRoute</code> associated with the given scenario and copying the node sequence
     * contained in the given route
     * 
     * @param parentScenario
     *            the parent scenario for this instance
     * @param route
     *            the route which node sequence will be copied
     */
    public VRPScenarioRoute(VRPScenario parentScenario, VRPScenarioRoute route) {
        this(parentScenario, route.getVehicle());
        mRoute.appendRoute(route);
        mShrunkNode = route.mShrunkNode;
        mCurrentState = route.mCurrentState;

        setAddAsObserver(false);
    }

    /**
     * Getter for the first actual visit (possibly a depot)
     * 
     * @return the visit corresponding to the first {@link IActualRequest} of this route
     */
    public VRPActualRequest getFirstActualRequest() {
        // checkLock();
        int ind = 0;
        for (VRPRequest node : this) {
            if (node instanceof VRPActualRequest && (// getCurrentState()==State.NOT_STARTED
                    !node.isDepot() || ind > 0)) {
                internalReleaseLock();
                return (VRPActualRequest) node;
            }
            ind++;
        }
        // internalReleaseLock();
        return null;
    }

    /**
     * Getter for the lastRequestServed flag
     * 
     * @return the served status of the last fixed visit: <code>true</code> if the last request has been served
     */
    public boolean isLastFixedRequestServed() {
        return mLastRequestServed;
    }

    /**
     * Set the lastRequestServed flag
     * 
     * @param lastRequestServed
     */
    private void setLastRequestServed(boolean lastRequestServed) {
        mLastRequestServed = lastRequestServed;
    }

    /**
     * Fix the first actual request of this route
     * 
     * @return the {@link VRPRequest} corresponding to the first actual request or depot
     */
    public VRPActualRequest fixFirstActualRequest() {
        VRPActualRequest firstActualVisit = null;

        if (getCurrentState() == State.TERMINATED) {
            throw new IllegalStateException("Route is already terminated");
        }

        if (!containsShrunkNode()) {
            throw new IllegalStateException(
                    "Cannot fix the first actual request on a route that does not contain a shrunk node");
        }

        checkLock();

        if (length() > 1) {
            if (!isLastFixedRequestServed()) {
                MSALogging
                        .getComponentsLogger()
                        .warn("VRPScenarioRoute.fixFirstActualRequest: Should not fix the first actual visit until the last fixed visit has been served (last visit: %s)",
                                getLastFixedRequest());
            }

            Iterator<VRPRequest> it = iterator();

            int ind = 0;
            int start = 0;
            while (it.hasNext() && firstActualVisit == null) {
                VRPRequest v = it.next();

                if (v.isDepot() && ind == 0 || v instanceof VRPShrunkRequest) {
                    start++; // Ignore the first depot and possible shrunk node
                }

                if (v instanceof VRPActualRequest && (!v.isDepot() || ind > 0)) {
                    firstActualVisit = (VRPActualRequest) v;
                } else {
                    ind++;
                }
            }

            if (start > 0 && start <= ind) {
                mLastFixedRequest = firstActualVisit;
                setLastRequestServed(false);

                if (start == length() - 1) {
                    mCurrentState = State.TERMINATED;
                }

                extractNodes(start, ind);

                // Update the cost: shrunk node [---AB] has the same location as
                // B
                // Route was 0[---A]BC--- is now 0[---AB]C---
                // extractNodes update the cost by delta=-cBB-cBC+cBC
                // need to update cost by delta=-cAB-c0A+c0B
                VRPShrunkRequest shrunk = getParentSolution().getParentInstance()
                        .getShrunkRequest(getVehicle().getID());
                List<VRPActualRequest> nodes = shrunk.getShrunkNodes();
                if (nodes.size() >= 2) {
                    VRPActualRequest prelast = nodes.get(nodes.size() - 2);
                    VRPActualRequest last = nodes.get(nodes.size() - 1);
                    double remArcCost = -mRoute.getArcCost(prelast, last) - mRoute.getArcCost(getFirstNode(), prelast)
                            + mRoute.getArcCost(getFirstNode(), last);
                    updateCost(remArcCost);
                }

                internalReleaseLock();
                return firstActualVisit;
            } else {
                internalReleaseLock();
                return null;
            }
        } else {
            internalReleaseLock();
            return null;
        }
    }

    /**
     * Getter for the last fixed visit
     * 
     * @return the last visit that was fixed
     */
    public VRPActualRequest getLastFixedRequest() {
        // checkLock();

        VRPActualRequest r = mLastFixedRequest;
        // internalReleaseLock();
        return r;
    }

    /**
     * Sets the lastRequestServed flag to <code>true</code>
     * 
     * @link {@link #isLastFixedRequestServed()}
     * @return <code>true</code> if the last visit was not already marked as served
     */
    public boolean markLastRequestAsServed() {
        checkLock();

        boolean prevVal = isLastFixedRequestServed();

        setLastRequestServed(true);

        internalReleaseLock();
        return !prevVal;
    }

    /**
     * Ordered list of actual visits
     * 
     * @return a list containing the visits corresponding to {@link IActualRequest} in the order in which they appear in
     *         the route
     */
    public List<VRPActualRequest> getOrderedActualRequests() {
        checkLock();

        List<VRPActualRequest> requests = new ArrayList<VRPActualRequest>();
        int ind = 0;
        for (VRPRequest v : this) {
            if (v instanceof VRPActualRequest && (getCurrentState() == State.NOT_STARTED || !v.isDepot() || ind > 0)) {
                requests.add((VRPActualRequest) v);
            }
            ind++;
        }
        internalReleaseLock();
        return requests;
    }

    /**
     * Ordered list of sampled visits
     * 
     * @return a list containing the visits corresponding to {@link ISampledRequest} in the order in which they appear
     *         in the route
     */
    public List<VRPSampledRequest> getOrderedSampledRequests() {
        checkLock();

        List<VRPSampledRequest> requests = new ArrayList<VRPSampledRequest>();
        for (VRPRequest v : this) {
            if (v instanceof VRPSampledRequest) {
                requests.add((VRPSampledRequest) v);
            }
        }

        internalReleaseLock();
        return requests;
    }

    /**
     * @param node
     *            the node to be checked
     * @param added
     *            <code>true</code> if the node was added, <code>false</code> if it was removed
     */
    protected void checkNode(VRPRequest node, boolean added) {
        if (node instanceof VRPShrunkRequest) {
            if (added) {
                if (mShrunkNode) {
                    throw new IllegalStateException("This route already contains a shrunk node");
                }
                mShrunkNode = true;
            } else {
                mShrunkNode = false;
            }
        }

        if (added && isAddAsObseverEnabled()) {
            node.addObserver(mObserverProxy);
        } else {
            node.removeObserver(mObserverProxy);
        }
    }

    /**
     * Getter for the shrunk node presence flag
     * 
     * @return <code>true</code> if this route contains a shrunk node at any position
     */
    public boolean containsShrunkNode() {
        return mShrunkNode;
    }

    /**
     * Ensure that the shrunk node is the first visited node.
     * 
     * @return <code>true</code> if the route has a coherent sequence
     */
    public boolean ensureRouteSequence() {
        boolean coherent = false;
        if (containsShrunkNode()) {
            if (getNodeAt(1) instanceof VRPShrunkRequest) {
                // The first node is the shrunk node
                coherent = true;
            } else if (getNodeAt(length() - 2) instanceof VRPShrunkRequest) {
                // The last node is the shrunk node
                reverseRoute();
                coherent = true;
            } else {
                // The shrunk node is neither in first nor in last position
                coherent = false;
            }
        } else {
            coherent = true;
        }

        return coherent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#appendNode(vroom.common.modeling
     * .dataModel.VRPRequest)
     */
    @Override
    public boolean appendNode(VRPRequest node) {
        checkLock();
        checkNode(node, true);
        boolean r = mRoute.appendNode(node);
        getParentSolution().nodeAdded(this, node);
        internalReleaseLock();
        return r;
    }

    @Override
    public boolean appendRoute(IRoute<? extends VRPRequest> appendedRoute) {
        checkLock();

        boolean r = mRoute.appendRoute(appendedRoute);
        if (r) {
            for (VRPRequest node : appendedRoute) {
                checkNode(node, true);
                getParentSolution().nodeAdded(this, node);
            }
        }
        internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#append(java.util.List)
     */
    @Override
    public boolean appendNodes(List<? extends VRPRequest> nodes) {
        checkLock();
        boolean r = mRoute.appendNodes(nodes);
        if (r) {
            for (VRPRequest node : nodes) {
                checkNode(node, true);
                getParentSolution().nodeAdded(this, node);
            }
        }
        internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#bestInsertion(edu.uniandes.copa
     * .VroomModelling.dataModel.VRPRequest)
     */
    @Override
    public boolean bestInsertion(VRPRequest node) {
        checkLock();

        boolean r = mRoute.bestInsertion(node);

        if (r) {
            checkNode(node, true);
            getParentSolution().nodeAdded(this, node);
        }

        internalReleaseLock();
        return r;

    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#calculateCost(boolean)
     */
    @Override
    public void calculateCost(boolean force) {
        // checkLock();

        mRoute.calculateCost(force);
        // internalReleaseLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#calculateRemaingCapacities(boolean
     * )
     */
    @Override
    public void calculateLoad(boolean force) {
        // checkLock();

        mRoute.calculateLoad(force);
        // internalReleaseLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#canAccommodateRequest(edu.uniandes
     * .copa.VroomModelling.dataModel.Request)
     */
    @Override
    public boolean canAccommodateRequest(IVRPRequest request) {
        // checkLock();
        boolean r = mRoute.canAccommodateRequest(request);
        // internalReleaseLock();
        return r;
    }

    /**
     * Insertion viability test
     * 
     * @param request
     * @return <code>true</code> if the given request can be inserted in this route
     */
    public boolean canAccommodateRequest(VRPRequest request) {
        // checkLock();

        calculateLoad(true);

        for (int p = 0; p < getVehicle().getCompartmentCount(); p++) {
            if (getLoad(p) + request.getDemand(p) > getVehicle().getCapacity(p)) {
                internalReleaseLock();
                return false;
            }
        }

        // internalReleaseLock();
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#extractNode(int)
     */
    @Override
    public VRPRequest extractNode(int index) {
        checkLock();
        VRPRequest node = (VRPRequest) mRoute.extractNode(index);
        checkNode(node, false);
        getParentSolution().nodeRemoved(this, node);
        internalReleaseLock();
        return node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#extractSubRoute(int, int)
     */
    @Override
    public VRPScenarioRoute extractSubroute(int start, int end) {
        checkLock();

        RouteBase route = mRoute.extractSubroute(start, end);

        for (INodeVisit node : route) {
            checkNode((VRPRequest) node, false);
            getParentSolution().nodeRemoved(this, (VRPRequest) node);
        }

        internalReleaseLock();
        return new VRPScenarioRoute(route);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#extractSubroute(int, int)
     */
    @Override
    public List<VRPRequest> extractNodes(int start, int end) {
        checkLock();

        List<VRPRequest> list = Utilities.convertToList(mRoute.extractNodes(start, end));

        for (VRPRequest node : list) {
            checkNode(node, false);
            getParentSolution().nodeRemoved(this, node);
        }

        internalReleaseLock();
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#getBestNodeInsertion(edu.uniandes
     * .copa.VroomModelling.dataModel.Node)
     */
    @Override
    public NodeInsertion getBestNodeInsertion(INodeVisit node) {
        checkLock();

        NodeInsertion rInt = mRoute.getBestNodeInsertion(node);

        internalReleaseLock();
        return new NodeInsertion(node, rInt.getCost(), rInt.getPosition(), this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IRoute#getBestNodeInsertion(edu.uniandes
     * .copa.VroomModelling.dataModel.Node, int, int)
     */
    @Override
    public NodeInsertion getBestNodeInsertion(INodeVisit node, int min, int max) {
        checkLock();

        NodeInsertion rInt = mRoute.getBestNodeInsertion(node, min, max);

        internalReleaseLock();
        return new NodeInsertion(node, rInt.getCost(), rInt.getPosition(), this);
    }

    /**
     * Best insertion of a request
     * <p>
     * This method assumes that the route is
     * </p>
     * 
     * @param request
     *            the request to be inserted
     * @return the best insertion for the given request, or <code>null</code> if this route cannot accommodate the given
     *         request
     * @see VRPScenarioRoute#getBestNodeInsertion(INodeVisit, int, int)
     * @see #canAccommodateRequest(VRPRequest)
     */
    public NodeInsertion getBestRequestInsertion(VRPRequest request) {
        if (!canAccommodateRequest(request)) {
            return null;
        }
        if (ensureRouteSequence()) {
            int min = 0, max = length();
            if (getLastNode().isDepot()) {
                max -= 1;
            }
            if (getFirstNode().isDepot()) {
                min += 1;
            }
            if (containsShrunkNode()) {
                min += 1;
            }

            return getBestNodeInsertion(request, min, max);
        } else {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getCost()
     */
    @Override
    public double getCost() {
        return mRoute.getCost();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getFirstNode()
     */
    @Override
    public VRPRequest getFirstNode() {
        // checkLock();

        VRPRequest r = (VRPRequest) mRoute.getFirstNode();
        // internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getLastNode()
     */
    @Override
    public VRPRequest getLastNode() {
        // checkLock();

        VRPRequest r = (VRPRequest) mRoute.getLastNode();
        // internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getNodeAt(int)
     */
    @Override
    public VRPRequest getNodeAt(int index) {
        // checkLock();

        VRPRequest r = (VRPRequest) mRoute.getNodeAt(index);
        // internalReleaseLock();
        return r;
    }

    @Override
    public int getNodePosition(INodeVisit node) {
        int i = 0;
        for (INodeVisit n : this) {
            if (n.getID() == node.getID()) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getNodeSequence()
     */
    @Override
    public List<VRPRequest> getNodeSequence() {
        // checkLock();

        return Utilities.convertToList(mRoute.getNodeSequence());
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getParentSolution()
     */
    @Override
    public VRPScenario getParentSolution() {
        return (VRPScenario) mRoute.getParentSolution();
    }

    /**
     * Changes the parent solution of this route.
     * <p>
     * Useful to copy a route to a new solution without copying the actual information
     * </p>
     * <p>
     * Warning: This method will remove this route from the current parent solution, but will not add it to the new one
     * </p>
     * 
     * @param parentSolution
     *            the new parent solution
     */
    protected void changeParentSolution(IVRPSolution<?> parentSolution) {
        mRoute.changeParentSolution(parentSolution);
    }

    @Override
    public double[] getLoads() {
        // double[] loads = mRoute.getLoads();
        // for (int p = 0; p < loads.length; p++)
        // loads[p] += getParentSolution().getParentInstance().getCurrentLoad(
        // getVehicle().getID(), p);
        // return loads;
        return mRoute.getLoads();
    }

    @Override
    public double getLoad() {
        return mRoute.getLoad(0);
    }

    @Override
    public double getLoad(int product) {
        return mRoute.getLoad(product);
        // + getParentSolution().getParentInstance().getCurrentLoad(
        // getVehicle().getID(), product);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#getVehicle()
     */
    @Override
    public Vehicle getVehicle() {
        return mRoute.getVehicle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#insertNode(int,
     * vroom.common.modeling.dataModel.VRPRequest)
     */
    @Override
    public boolean insertNode(int index, VRPRequest node) {
        checkLock();

        checkNode(node, true);

        boolean r = mRoute.insertNode(index, node);
        getParentSolution().nodeAdded(this, node);
        internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.Route#insertNode(vroom.common.modeling
     * .dataModel.RouteBase.NodeInsertion,
     * vroom.common.modeling.dataModel.VRPRequest)
     */
    @Override
    public boolean insertNode(NodeInsertion ins, VRPRequest node) {
        checkLock();

        checkNode(node, true);

        boolean r = mRoute.insertNode(ins, node);
        getParentSolution().nodeAdded(this, node);

        internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#insertSubroute(int,
     * java.util.List)
     */
    @Override
    public boolean insertNodes(int index, List<? extends VRPRequest> subroute) {
        checkLock();

        for (VRPRequest node : subroute) {
            checkNode(node, true);
        }

        boolean r = mRoute.insertNodes(index, subroute);

        for (VRPRequest node : subroute) {
            getParentSolution().nodeAdded(this, node);
        }
        internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#insertSubroute(int,
     * vroom.common.modeling.dataModel.IRoute)
     */
    @Override
    public boolean insertSubroute(int index, IRoute<? extends VRPRequest> subroute) {
        checkLock();

        for (VRPRequest node : subroute) {
            checkNode(node, true);
        }

        boolean r = mRoute.insertSubroute(index, subroute);
        for (VRPRequest node : subroute) {
            getParentSolution().nodeAdded(this, node);
        }
        internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#length()
     */
    @Override
    public int length() {
        return mRoute.length();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#revertSubRoute(int, int)
     */
    @Override
    public void reverseSubRoute(int start, int end) {
        checkLock();

        mRoute.reverseSubRoute(start, end);
        internalReleaseLock();
    }

    @Override
    public void reverseRoute() {
        checkLock();

        mRoute.reverseRoute();
        internalReleaseLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#setNodeAt(int,
     * vroom.common.modeling.dataModel.VRPRequest)
     */
    @Override
    public VRPRequest setNodeAt(int index, VRPRequest node) {
        checkLock();

        VRPRequest prev = (VRPRequest) mRoute.setNodeAt(index, node);

        checkNode(prev, false);
        checkNode(node, true);

        getParentSolution().nodeAdded(this, node);
        getParentSolution().nodeRemoved(this, node);
        internalReleaseLock();
        return prev;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#subroute(int, int)
     */
    @Override
    public List<VRPRequest> subroute(int start, int end) {
        checkLock();

        List<VRPRequest> r = Utilities.convertToList(mRoute.subroute(start, end));
        internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#swapNodes(int, int)
     */
    @Override
    public boolean swapNodes(int node1, int node2) {
        checkLock();

        boolean r = mRoute.swapNodes(node1, node2);
        internalReleaseLock();
        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.Route#updateCost(double)
     */
    @Override
    public void updateCost(double delta) {
        checkLock();
        mRoute.updateCost(delta);
        internalReleaseLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#updateLoad(int, double)
     */
    @Override
    public void updateLoad(int product, double delta) {
        checkLock();
        mRoute.updateLoad(product, delta);
        internalReleaseLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public ListIterator<VRPRequest> iterator() {
        return Utilities.castIterator(mRoute.iterator());
    }

    @Override
    public String getNodeSeqString() {
        StringBuilder b = new StringBuilder(length() * 4);

        b.append("<");

        Iterator<VRPRequest> it2 = iterator();
        while (it2.hasNext()) {
            b.append(it2.next().toShortString());
            if (it2.hasNext()) {
                b.append(',');
            }
        }
        b.append('>');
        return b.toString();
    }

    @Override
    public synchronized String toString() {
        boolean l = tryLock(TRY_LOCK_TIMOUT);

        if (l) {
            StringBuilder b = new StringBuilder(length() * 5);

            b.append(String.format("cost:%.2f", getCost()));
            b.append(" length:");
            b.append(length());
            b.append(" load:");
            b.append(Arrays.toString(getLoads()));
            b.append(" ");
            b.append(getNodeSeqString());

            releaseLock();

            return b.toString();
        } else {
            return Constants.TOSTRING_LOCKED;
        }
    }

    @Override
    public VRPScenarioRoute clone() {
        VRPScenarioRoute clone = new VRPScenarioRoute(mRoute.clone());

        clone.setLastRequestServed(mLastRequestServed);
        clone.mShrunkNode = mShrunkNode;
        clone.mLastFixedRequest = mLastFixedRequest;
        clone.mCurrentState = mCurrentState;

        return clone;
    }

    /**
     * Clone this route with a reference to the cloned parent scenario.
     * <p>
     * This method should only be used when cloning a scenario
     * </p>
     * 
     * @param clonedScenario
     *            the clone of this route parent scenario
     * @return a clone of this route associated with the cloned scenario
     */
    protected VRPScenarioRoute cloneInternal(VRPScenario clonedScenario) {
        VRPScenarioRoute clone = new VRPScenarioRoute(mRoute.getClass(), clonedScenario, getVehicle());

        clone.mRoute.appendRoute(mRoute);
        clone.setLastRequestServed(mLastRequestServed);
        clone.mShrunkNode = mShrunkNode;
        clone.mLastFixedRequest = mLastFixedRequest;
        clone.mCurrentState = mCurrentState;

        return clone;
    }

    @Override
    public int getID() {
        return mID;
    }

    // ------------------------------------
    // ILockable interface implementation
    // ------------------------------------
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
            if (!getParentSolution().getLockInstance().tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
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
        getParentSolution().getLockInstance().unlock();
    }

    protected void internalReleaseLock() {
        getParentSolution().internalReleaseLock();
    }

    @Override
    public boolean isLockOwnedByCurrentThread() {
        return getParentSolution().getLockInstance().isHeldByCurrentThread();
    }

    @Override
    public ExtendedReentrantLock getLockInstance() {
        return getParentSolution().getLockInstance();
    }

    private void checkLock() throws ConcurrentModificationException {
        getParentSolution().checkLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.utilities.IObserver#update(vroom.common.utilities.IObservable
     * , java.lang.Object)
     */
    @Override
    public void update(IObservable source, Update update) {
        if (update instanceof ValueUpdate && VRPRequest.PROP_DEMANDS.equals(((ValueUpdate) update).getDescription())) {
            // acquireLock();
            for (int p = 0; p < getVehicle().getCompartmentCount(); p++) {
                mRoute.updateLoad(p, -((double[]) ((ValueUpdate) update).getOldValue())[p]
                        + ((double[]) ((ValueUpdate) update).getNewValue())[p]);
            }
            // releaseLock();
        }

    }

    @Override
    public boolean contains(INodeVisit node) {
        return mRoute.contains(node);
    }

    @Override
    public boolean remove(INodeVisit node) {
        boolean b = mRoute.remove(node);
        if (b && node instanceof VRPRequest) {
            getParentSolution().nodeRemoved(this, (VRPRequest) node);
            checkNode((VRPRequest) node, false);
        }
        return b;
    }

    @Override
    public void dereference() {
        for (VRPRequest i : this) {
            i.removeObserver(mObserverProxy);
            if (i instanceof IDerefenceable) {
                ((IDerefenceable) i).dereference();
            }
        }
        mObserverProxy.detach();
        mCurrentState = State.DELETED;
    }
}
