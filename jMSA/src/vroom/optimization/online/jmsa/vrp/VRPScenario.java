package vroom.optimization.online.jmsa.vrp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.NodeInsertion;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.utilities.Constants;
import vroom.common.utilities.ILockable;
import vroom.common.utilities.Utilities;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.ISampledRequest;
import vroom.optimization.online.jmsa.IScenario;

/**
 * Creation date: Feb 25, 2010 - 3:16:51 PM<br/>
 * <code>VRPScenario</code> is an implementation of {@link IScenario} that represents a scenario in the context of
 * vehicle routing problems. It implements the {@link ILockable} interface to ensure that no concurrent modifications
 * are done on the same instance. Therefore before manipulating an instance, the current thread should acquire the lock
 * by calling {@link #acquireLock()} and release it afterward with {@link #releaseLock()}.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @see VRPActualRequest
 * @see VRPSampledRequest
 * @see RouteBase
 */
public class VRPScenario extends Solution<VRPScenarioRoute> implements IScenario {

    private final List<VRPSampledRequest> mSampledRequests;
    // private final Map<Integer, VRPActualRequest> mPendingActualRequests;

    private int                           mNonImproving = 0;

    /**
     * Creates a new <code>RoutingScenario</code> based on the given <code>vrpInstance</code>
     * 
     * @param vrpInstance
     *            the instance of VRP from which the actual requests will be taken.
     * @param actualRequests
     *            a list of pending actual requests (can be <code>null</code>)
     * @param sampledRequests
     *            a list of sampled requests (can be <code>null</code>)
     */
    public VRPScenario(MSAVRPInstance vrpInstance, List<VRPActualRequest> actualRequests,
            List<VRPSampledRequest> sampledRequests) {
        super(vrpInstance);
        // mInstance = vrpInstance;

        acquireLock();

        if (vrpInstance == null) {
            throw new IllegalArgumentException("Argument vrpInstance cannot be null");
        }

        mSampledRequests = new LinkedList<VRPSampledRequest>();
        if (sampledRequests != null) {
            mSampledRequests.addAll(mSampledRequests);
        }

        // mPendingActualRequests = new HashMap<Integer, VRPActualRequest>();

        // if (actualRequests != null) {
        // for (VRPActualRequest r : actualRequests) {
        // mPendingActualRequests.put(r.getID(), r);
        // }
        // }

        releaseLock();
    }

    @Override
    public List<VRPActualRequest> getActualRequests() {
        // checkLock();
        // ArrayList<VRPActualRequest> list = new ArrayList<VRPActualRequest>(mPendingActualRequests.values());

        // internalReleaseLock();
        // return list;
        return getParentInstance().getPendingRequests();
    }

    @Override
    public List<VRPSampledRequest> getSampledRequests() {
        // checkLock();
        ArrayList<VRPSampledRequest> list = new ArrayList<VRPSampledRequest>(mSampledRequests);

        // internalReleaseLock();
        return list;
    }

    public VRPActualRequest getLastFixedRequest(int resource) {
        // checkLock();

        VRPActualRequest visit = getRoute(resource).getLastFixedRequest();

        // internalReleaseLock();

        return visit != null ? (VRPActualRequest) visit : null;
    }

    /**
     * Retrieves an {@linkplain VRPActualRequest actual request} contained in this scenario by its id.
     * 
     * @param id
     *            the request id
     * @return the {@link VRPActualRequest} contained in this scenario with the given id, or <code>null</code> if there
     *         is no such request.
     */
    public VRPActualRequest getActualRequest(int id) {
        // return mPendingActualRequests.get(id);
        return getParentInstance().getNodeVisit(id);
    }

    @Override
    public IActualRequest getFirstActualRequest(int resource) {
        // checkLock();

        IActualRequest req = getRoute(resource).getFirstActualRequest();

        // internalReleaseLock();

        return req;
    }

    @Override
    public List<IActualRequest> getOrderedActualRequests(int resource) {
        // checkLock();

        List<IActualRequest> list = Utilities.convertToList(getRoute(resource)
                .getOrderedActualRequests());

        // internalReleaseLock();

        return list;
    }

    @Override
    public List<ISampledRequest> getOrderedSampledRequests(int resource) {
        // checkLock();

        List<ISampledRequest> list = Utilities.convertToList(getRoute(resource)
                .getOrderedSampledRequests());

        // internalReleaseLock();

        return list;
    }

    @Override
    public int getResourceCount() {
        return getRouteCount();
    }

    @Override
    public IActualRequest fixFirstActualRequest(int resource) {
        checkLock();

        IActualRequest req = getRoute(resource).fixFirstActualRequest();
        // mPendingActualRequests.remove(req.getID());

        internalReleaseLock();

        return req;
    }

    @Override
    public boolean markLastVisitAsServed(int resource) {
        checkLock();
        boolean r = getRoute(resource).markLastRequestAsServed();
        internalReleaseLock();

        return r;
    }

    @Override
    public VRPScenarioRoute getRoute(int index) {
        // checkLock();
        VRPScenarioRoute r = super.getRoute(index);
        // internalReleaseLock();

        return r;
    }

    @Override
    public ListIterator<VRPScenarioRoute> iterator() {
        // checkLock();
        ListIterator<VRPScenarioRoute> it = super.iterator();
        // internalReleaseLock();

        return it;
    }

    @Override
    public void addRoute(VRPScenarioRoute route) {
        checkLock();
        if (route.getParentSolution() != this) {
            throw new IllegalStateException(
                    "The added route parent solution is different from this instance");
        }

        super.addRoute(route);
        internalReleaseLock();
    }

    @Override
    public MSAVRPInstance getParentInstance() {
        return (MSAVRPInstance) super.getParentInstance();
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.Solution#removeRoute(vroom.common.modeling.dataModel.IRoute)
     */
    @Override
    public void removeRoute(IRoute<?> route) {
        checkLock();
        super.removeRoute(route);
        internalReleaseLock();
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.Solution#clear()
     */
    @Override
    public void clear() {
        checkLock();
        super.clear();
        internalReleaseLock();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public VRPScenario clone() {
        checkLock();
        VRPScenario clone = new VRPScenario(getParentInstance(), new ArrayList<VRPActualRequest>(
                getActualRequests()), new ArrayList<VRPSampledRequest>(getSampledRequests()));
        clone.acquireLock();
        clone.clear();
        for (VRPScenarioRoute route : this) {
            clone.addRoute(route.cloneInternal(clone));
        }
        clone.releaseLock();
        internalReleaseLock();
        return clone;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.Solution#addRoute(vroom.common.modeling.dataModel.IRoute, int)
     */
    @Override
    public void addRoute(VRPScenarioRoute route, int index) {
        checkLock();
        super.addRoute(route, index);
        internalReleaseLock();
    }

    /**
     * Update the scenario when a request has been added to any route.
     * <p>
     * This method should only be called by the {@link VRPScenarioRoute}
     * </p>
     * 
     * @param route
     *            the route in which the request was added
     * @param request
     *            the added request
     */
    public void actualRequestAdded(VRPScenarioRoute route, VRPActualRequest request) {
        // mPendingActualRequests.put(request.getID(), request);
    }

    /**
     * Update the scenario when a request has been added to any route.
     * <p>
     * This method should only be called by the {@link VRPScenarioRoute}
     * </p>
     * 
     * @param route
     *            the route in which the request was added
     * @param request
     *            the added request
     */
    protected void nodeAdded(VRPScenarioRoute route, VRPRequest request) {
        // if(request instanceof VRPActualRequest){
        // actualRequestAdded(route,request);
        // }
    }

    /**
     * Update the scenario when a request has been removed from any route.
     * <p>
     * This method should only be called by the {@link VRPScenarioRoute}
     * </p>
     * 
     * @param route
     *            the route in which the request was added
     * @param request
     *            the added request
     */
    protected void nodeRemoved(VRPScenarioRoute route, VRPRequest request) {
        // if(request instanceof VRPActualRequest){
        // VRPActualRequest r = (VRPActualRequest) request;
        // if(mPendingActualRequests.containsKey(r.getID())){
        // mPendingActualRequests.remove(r.getID());
        // }
        // }
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.Solution#getCost()
     */
    @Override
    public double getCost() {
        // acquireLock();
        // checkLock();
        return super.getCost();
    }

    /**
     * Calculation of the best node insertion over all the routes contained in this scenario.
     * <p>
     * Note that this implementation will assume for each route that the request cannot be inserted before or after the
     * first and last node if they are depots. It will also assume that the request can only be inserted after the
     * shrunk node is any, assuming that it is in first position.
     * 
     * @param request
     *            the request to be inserted
     * @param exlusionFilter
     *            an array containing the index of the routes to be excluded in the search
     * @return a {@link NodeInsertion} representing the best insertion, or <code>null</code> if no feasible insertion
     *         has been found.
     * @see IRoute#getBestNodeInsertion(INodeVisit, int, int)
     * @see IRoute#canAccommodateRequest(vroom.common.modeling.dataModel.IVRPRequest)
     * @see VRPScenarioRoute#ensureRouteSequence()
     */
    public NodeInsertion getBestInsertion(VRPRequest request, int... exlusionFilter) {
        Arrays.sort(exlusionFilter);

        NodeInsertion ins = null, bestIns = null;
        for (int rte = 0; rte < getRouteCount(); rte++) {
            VRPScenarioRoute route = getRoute(rte);

            // The route can accommodate the request
            if (Arrays.binarySearch(exlusionFilter, rte) < 0
                    && route.canAccommodateRequest(request)) {

                int max = (route.getLastNode() != null && route.getLastNode().isDepot()) ? route
                        .length() - 1 : route.length();
                int min = (route.getFirstNode() != null && route.getFirstNode().isDepot()) ? 1 : 0;

                min += route.containsShrunkNode() ? 1 : 0;

                // Find the best insertion in this route
                ins = route.getBestNodeInsertion(request, min, max);

                if (bestIns == null || ins.getCost() < bestIns.getCost()) {
                    // This insertion is the best found so far
                    bestIns = ins;
                }
            }
        }

        return bestIns;

    }

    @Override
    public String toString() {
        boolean b = tryLock(Constants.TOSTRING_LOCK_TIMOUT);

        if (b) {
            StringBuilder string = new StringBuilder(getRouteCount() * 50);

            string.append(String.format("Total Cost:%.2f", getCost()));
            string.append(" Routes: {");
            Iterator<VRPScenarioRoute> it = iterator();
            while (it.hasNext()) {
                string.append(it.next().toString());
                if (it.hasNext()) {
                    string.append(',');
                }
            }

            string.append('}');

            releaseLock();

            return string.toString();
        } else {
            return Constants.TOSTRING_LOCKED;
        }
    }

    /**
     * Import the data of the given scenario into this scenario.
     * <p/>
     * To present duplicate references, all the copied information will be removed from the given scenario.
     * 
     * @param scenario
     *            the scenario which routes have to be imported
     */
    public void importScenario(VRPScenario scenario) {
        // Ignore if the given scenario is this scenario
        if (scenario != this) {
            checkLock();
            scenario.acquireLock();

            // mPendingActualRequests.clear();
            // mPendingActualRequests.putAll(scenario.mPendingActualRequests);

            mSampledRequests.clear();
            mSampledRequests.addAll(scenario.mSampledRequests);

            super.clear();

            // Copy routes in a temporary list to prevent concurrent modification exc.
            LinkedList<VRPScenarioRoute> routes = new LinkedList<VRPScenarioRoute>();
            for (VRPScenarioRoute r : scenario) {
                routes.add(r);
            }
            // Change the routes parent solution and add them to this scenario
            for (VRPScenarioRoute r : routes) {
                r.changeParentSolution(this);
                addRoute(r);
            }

            // Clear the imported scenario
            scenario.clear();
            scenario.mSampledRequests.clear();
            // scenario.mPendingActualRequests.clear();

            scenario.releaseLock();

            internalReleaseLock();
        }
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.modeling.dataModel.Solution#acquireLock()
     */
    @Override
    public void acquireLock() {
        try {
            if (!getLockInstance().tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
                throw new IllegalStateException(
                        String.format(
                                "Unable to acquire lock on this instance of %s (%s) after %s %s, owner: %s",
                                this.getClass().getSimpleName(), hashCode(), TRY_LOCK_TIMOUT,
                                TRY_LOCK_TIMOUT_UNIT, getLockInstance().getOwnerName()));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format(
                    "Unable to acquire lock on this instance of %s (%s)", this.getClass()
                            .getSimpleName(), hashCode()), e);
        }

    }

    @Override
    public int getNonImprovingCount() {
        return mNonImproving;
    }

    @Override
    public void incrementNonImprovingCount() {
        mNonImproving++;
    }

    @Override
    public void resetNonImprovingCount() {
        mNonImproving = 0;
    }

    @Override
    public void dereference() {
        for (VRPScenarioRoute r : this) {
            r.dereference();
        }
        clear();
    }

}