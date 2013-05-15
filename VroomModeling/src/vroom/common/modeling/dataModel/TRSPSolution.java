/**
 *
 */
package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import vroom.common.utilities.ArrayIterator;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.ObjectWithIdSet;
import vroom.common.utilities.Utilities;

/**
 * <code>TRSPSolution</code> is a class representing a solution for the TRSP. It contains a set of
 * {@link GiantPermRoute}, each associated with a {@link Vehicle}
 * <p>
 * Creation date: Mar 24, 2011 - 2:14:41 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPSolution extends Solution<GiantPermRoute> {

    /** The main data structure for all tours */
    private final GiantPermutation mGiantPermutation;

    /**
     * Returns the giant permutation in which all tour data is stored
     * 
     * @return the giant permutation in which all tour data is stored
     */
    protected GiantPermutation getGiantPermutation() {
        return mGiantPermutation;
    }

    // Arrays shared across all tours

    /** the set of unserved requests **/
    private final Set<IVRPRequest> mUnservedRequests;

    /**
     * Returns the set of unserved requests <br/>
     * This set is initialized with all requests but should be maintained externally
     * 
     * @return the set of unserved requests
     */
    public Set<IVRPRequest> getUnservedRequests() {
        return this.mUnservedRequests;
    }

    /** The tours contained in this solution */
    private final GiantPermRoute[] mTours;

    /**
     * Returns the tour associated with a given vehicle
     * 
     * @param techId
     *            the id of the vehicle
     * @return the tour associated with the vehicle with id <code>techID</code>
     */
    public GiantPermRoute getTour(int techId) {
        return mTours[techId];
    }

    /**
     * Returns the tour that visits a given request
     * 
     * @param reqId
     *            the considered request
     * @return the tour that visits the request with id <code>reqId</code>
     */
    public GiantPermRoute getVisitingTour(int reqId) {
        int tech = getGiantPermutation().getVisitingVehicle(reqId);
        return tech >= 0 ? getTour(getGiantPermutation().getVisitingVehicle(reqId)) : null;
    }

    // /**
    // * Add the tour to this solution, replacing possible previous values
    // *
    // * @param tour
    // * the tour to be added
    // * @return the tour previously associated with the same vehicle
    // */
    // public TRSPTour importTour(TRSPTour tour) {
    // TRSPTour t = getTour(tour.getVehicleId());
    // TRSPSimpleTour prev = new TRSPSimpleTour(t);
    // t.importTour(tour);
    // for (int i : prev)
    // markAsUnserved(i);
    // for (int i : tour)
    // markAsServed(i);
    //
    // return prev;
    // }
    //
    // /**
    // * Import a solution into this solution. Will replace all the tours from this solution with the one present in
    // * {@code solution}
    // *
    // * @param solution
    // * the solution to import
    // */
    // public void importSolution(TRSPSolution solution) {
    // if (solution != this) {
    // getGiantPermutation().clear();
    // mUnservedRequests.clear();
    // mUnservedRequests.addAll(solution.getUnservedRequests());
    // for (TRSPTour tour : solution) {
    // TRSPTour thisTour = getTour(tour.getVehicleId());
    // thisTour.importTour(tour);
    // }
    //
    // getGiantPermutation().importPermutationInternal(solution.getGiantPermutation());
    // }
    // }

    /**
     * Creates a new <code>TRSPSolution</code>
     * 
     * @param instance
     *            the parent instance
     * @param costDelegate
     *            the cost delegate used to evaluate tours
     * @see #freeze()
     * @see #unfreeze()
     */
    public TRSPSolution(IVRPInstance instance) {
        super(instance);
        mLock = new ExtendedReentrantLock();
        mTours = new GiantPermRoute[instance.getFleet().size()];

        mGiantPermutation = newGiantPermutation();
        mUnservedRequests = new ObjectWithIdSet<>(Utilities.getMaxId(getParentInstance()
                .getNodeVisits()));

        for (IVRPRequest r : instance.getRequests())
            markAsUnserved(r);

        for (int i = 0; i < mTours.length; i++) {
            mTours[i] = newTour(getParentInstance().getFleet().getVehicle(i));
        }
    }

    /**
     * Instanciates a new giant permutation, used in constructor
     * 
     * @return
     */
    protected GiantPermutation newGiantPermutation() {
        return new GiantPermutation(this);
    }

    /**
     * Factory method used in constructor {@link #TRSPSolution(IVRPInstance, TRSPCostDelegate, boolean)} to instanciate
     * the tours
     * 
     * @param t
     *            the vehicle to associate with the returned tour
     * @return a new tour associated with the given vehicle
     */
    protected GiantPermRoute newTour(Vehicle t) {
        return new GiantPermRoute(this, t);
    }

    /**
     * Creates a new <code>TRSPSolution</code> by cloning
     * 
     * @param parent
     *            the solution to be cloned
     */
    protected TRSPSolution(TRSPSolution parent) {
        super(parent.getParentInstance());
        mLock = new ExtendedReentrantLock();
        mTours = new GiantPermRoute[getParentInstance().getFleet().size()];

        mGiantPermutation = clonePermutation(parent.getGiantPermutation());

        mUnservedRequests = new ObjectWithIdSet<IVRPRequest>(Utilities.getMaxId(getParentInstance()
                .getNodeVisits()));
        mUnservedRequests.addAll(parent.getUnservedRequests());

        for (int i = 0; i < mTours.length; i++) {
            mTours[i] = cloneTour(parent.getTour(i));
        }
    }

    /**
     * clone a permutation, used internally in {@link #TRSPSolution(TRSPSolution)}
     * 
     * @param perm
     * @return
     */
    protected GiantPermutation clonePermutation(GiantPermutation perm) {
        return perm.clone(this);
    }

    /**
     * Clone a tour, used internally in {@link #TRSPSolution(TRSPSolution)}
     * 
     * @param tour
     *            the tour to be cloned
     * @return a clone of {@code  tour}
     */
    protected GiantPermRoute cloneTour(GiantPermRoute tour) {
        return tour.clone(this);
    }

    /**
     * Returns the number of tours in this solution
     * 
     * @return the number of tours in this solution
     */
    public int getTourCount() {
        return mTours.length;
    }

    @Override
    public Comparable<?> getObjective() {
        return getObjectiveValue();
    }

    @Override
    public double getObjectiveValue() {
        double cost = 0;

        for (int r = 0; r < getRouteCount(); r++) {
            cost += getRoute(r).getCost();
        }

        return cost;
    }

    /**
     * Returns the number of requests that are not served by this solution
     * 
     * @return the number of requests that are not served by this solution
     */
    public int getUnservedCount() {
        return getUnservedRequests().size();
    }

    /**
     * Mark a request as served, or does nothing if {@code  id} is not a valid request id
     * 
     * @param id
     *            an id
     */
    public void markAsServed(IVRPRequest r) {
        getUnservedRequests().remove(r);
    }

    /**
     * Mark all requests as served, in other words clear the {@link #getUnservedRequests() unserved request}
     */
    public void markAllAsServed() {
        mUnservedRequests.clear();
    }

    /**
     * Mark a request as unserved, or does nothing if {@code  id} is not a valid request id
     * 
     * @param r
     *            an id
     */
    public void markAsUnserved(IVRPRequest r) {
        getUnservedRequests().add(r);
    }

    @Override
    public TRSPSolution clone() {
        return new TRSPSolution(this);
    }

    @Override
    public ListIterator<GiantPermRoute> iterator() {
        return new ArrayIterator<GiantPermRoute>(mTours);
    }

    // ------------------------------------
    // ILockable interface implementation
    // ------------------------------------
    /** A lock to be used by this instance */
    private final ExtendedReentrantLock mLock;
    private boolean                     mSelfLock = false;

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
        ;
    }

    @Override
    public void releaseLock() {
        if (mLock.isLocked()) {
            this.mLock.unlock();
        }
    }

    @Override
    public void internalReleaseLock() {
        if (mSelfLock) {
            releaseLock();
            mSelfLock = false;
        }
    }

    @Override
    public boolean isLockOwnedByCurrentThread() {
        return this.mLock.isHeldByCurrentThread();
    }

    @Override
    public ExtendedReentrantLock getLockInstance() {
        return this.mLock;
    }

    /**
     * Check the lock state of this object
     * 
     * @return <code>true</code> if there was no previous lock and lock was acquired, <code>false</code> if the lock was
     *         already owned by the current thread
     * @throws ConcurrentModificationException
     */
    @Override
    public boolean checkLock() throws ConcurrentModificationException {
        if (!isLockOwnedByCurrentThread() && mLock.isLocked()) {
            throw new ConcurrentModificationException(
                    String.format(
                            "The current thread (%s) does not have the lock on this instance of %s, owner: %s",
                            Thread.currentThread(), this.getClass().getSimpleName(),
                            getLockInstance().getOwnerName()));
        } else if (!mLock.isLocked()) {
            acquireLock();
            mSelfLock = true;
            return true;
        } else {
            mSelfLock = false;
            return false;
        }
    }

    // ------------------------------------

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append(String.format("Cost:%.2f, Unserved:%s Tours [", getObjectiveValue(),
                Utilities.toShortString(getUnservedRequests())));
        Iterator<GiantPermRoute> it = iterator();
        while (it.hasNext()) {
            b.append(it.next().toString());
            if (it.hasNext())
                b.append(',');
        }

        b.append(']');

        return b.toString();
    }

    // @Override
    // public int hashCode() {
    // return getParentInstance().getSolutionHasher().hash(this);
    // }
    //
    // @Override
    // public int hashSolution() {
    // return getParentInstance().getSolutionHasher().hash(this);
    // }

    /**
     * Returns the hash code of this instance as defined in {@link Object#hashCode()}
     * 
     * @return the hash code of this instance as defined in {@link Object#hashCode()}
     */
    public int defaultHashCode() {
        return super.hashCode();
    }

    /**
     * Convert a solution to a giant tour
     * 
     * @return a list containing all the tours concatenated into a giant tour
     */
    public List<INodeVisit> toGiantTour() {
        ArrayList<INodeVisit> tour = new ArrayList<INodeVisit>(getParentInstance().getNodeVisits()
                .size());
        for (GiantPermRoute t : this) {
            for (INodeVisit node : t) {
                tour.add(node);
            }
        }
        return tour;
    }

    /**
     * Convert a solution to a short string of the form: <br/>
     * <code>"<1,11,12,0,13,21|2,14,15,16,22>"</code>
     * 
     * @return a short string describing this solution
     */
    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        for (GiantPermRoute tour : this) {
            if (sb.length() > 1)
                sb.append("|");
            int idx = 0;
            for (INodeVisit node : tour) {
                if (idx > 0)
                    sb.append(",");
                sb.append(node.toString());
                idx++;
            }
        }
        sb.append(">");

        return sb.toString();
    }

    /**
     * <code>GiantPermutation</code>
     * <p>
     * Creation date: Apr 26, 2012 - 4:56:09 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class GiantPermutation {
        /** The cumulative cost at each node. */
        private final double[]     mCumulativeCost;

        /** The earliest time of visit at each node. */
        private final double[]     mEarliestArrivalTime;

        /** The latest feasible arrival time at each node. */
        private final double[]     mLatestFeasibleTime;

        /** The total waiting time at between any two nodes */
        private final double[][]   mWaitingTime;

        /** The forward time slack between any two nodes */
        private final double[][]   mFwdTimeSlack;

        /** The available spare parts at each node */
        private final double[][]   mCapacity;

        /** The number of spare parts required at each node and for the rest of the tour */
        private final int[][]      mRequiredSpareParts;

        /** <code>true</code> if the depot is visited prior to the corresponding node */
        private final boolean[]    mDepotVisited;

        /** The Predecessor of each node. */
        private final int[]        mPred;

        /** The Successor of each node. */
        private final int[]        mSucc;

        /** The vehicle visiting each node */
        private final int[]        mTechId;

        private final TRSPSolution mSolution;

        /**
         * The Constant NA to represent unknown data, for instance for cumulative cost of a node that is not in the tour
         */
        public final static double NA = Double.NaN;

        private IVRPInstance getParentInstance() {
            return mSolution.getParentInstance();
        }

        protected GiantPermutation(TRSPSolution solution) {
            mSolution = solution;
            int maxID = Utilities.getMaxId(getParentInstance().getNodeVisits());
            mCumulativeCost = new double[maxID];
            mEarliestArrivalTime = new double[maxID];
            mLatestFeasibleTime = new double[maxID];

            mWaitingTime = new double[maxID][];
            boolean defFwdSlck = false; // FIXME set depending on the probleme at hand
            mFwdTimeSlack = defFwdSlck ? new double[maxID][] : null;
            for (int i = 0; i < mWaitingTime.length; i++) {
                mWaitingTime[i] = new double[defFwdSlck ? mWaitingTime.length - i : 1];
                if (defFwdSlck)
                    mFwdTimeSlack[i] = new double[mFwdTimeSlack.length - i];
            }
            mCapacity = new double[maxID][getParentInstance().getFleet().getVehicle()
                    .getCompartmentCount()];
            mRequiredSpareParts = new int[maxID][getParentInstance().getFleet().getVehicle()
                    .getCompartmentCount()];
            mDepotVisited = new boolean[maxID];
            mPred = new int[maxID];
            mSucc = new int[maxID];
            mTechId = new int[maxID];

            for (int id = 0; id < mTechId.length; id++) {
                mTechId[id] = GiantPermRoute.UNDEFINED;
                mPred[id] = GiantPermRoute.UNDEFINED;
                mSucc[id] = GiantPermRoute.UNDEFINED;
            }
        }

        /**
         * Import {@code giantPermutation} in this permutation
         * 
         * @param giantPermutation
         */
        protected void importPermutationInternal(GiantPermutation giantPermutation) {
            // Nothing to do here - the work is delegated to TRSPTour.importTour
        }

        /**
         * Used internally in {@link #clone()} to instantiate a new permutation, should be overriden by subclasses
         * 
         * @param solution
         * @return
         */
        protected GiantPermutation cloneInternal(TRSPSolution solution) {
            return new GiantPermutation(solution);
        }

        /**
         * Clone this permutation and associate it to a possibly different solution
         * 
         * @param solution
         * @return a clone of this permutation
         */
        private GiantPermutation clone(TRSPSolution solution) {
            GiantPermutation clone = cloneInternal(solution);
            clone.importPermutation(this);
            return clone;
        }

        /**
         * Clone this permutation and associate it to a possibly different solution
         * 
         * @param solution
         * @param clone
         *            an empty instance of {@link GiantPermutation}
         * @return a clone of this permutation
         */
        public void importPermutation(GiantPermutation clone) {
            for (int i = 0; i < mCumulativeCost.length; i++) {
                this.mCumulativeCost[i] = clone.mCumulativeCost[i];
                this.mEarliestArrivalTime[i] = clone.mEarliestArrivalTime[i];
                this.mLatestFeasibleTime[i] = clone.mLatestFeasibleTime[i];
                for (int j = 0; j < this.mWaitingTime[i].length; j++)
                    this.mWaitingTime[i][j] = clone.mWaitingTime[i][j];
                if (this.mFwdTimeSlack != null)
                    for (int j = 0; j < this.mFwdTimeSlack[i].length; j++)
                        this.mFwdTimeSlack[i][j] = clone.mFwdTimeSlack[i][j];
                for (int j = 0; j < this.mCapacity[i].length; j++)
                    this.mCapacity[i][j] = clone.mCapacity[i][j];
                for (int j = 0; j < this.mRequiredSpareParts[i].length; j++)
                    this.mRequiredSpareParts[i][j] = clone.mRequiredSpareParts[i][j];
                this.mDepotVisited[i] = clone.mDepotVisited[i];
                this.mPred[i] = clone.mPred[i];
                this.mSucc[i] = clone.mSucc[i];
                this.mTechId[i] = clone.mTechId[i];
            }
        }

        /**
         * Returns {@code true} if the forward slack time is defined in this instance
         * 
         * @return {@code true} if the forward slack time is defined in this instance
         */
        public boolean isFwdSlackTimeDefined() {
            return mFwdTimeSlack != null;
        }

        /**
         * Returns the id of the vehicle visiting the given node
         * 
         * @param node
         * @return the id of the vehicle visiting the given node
         */
        public int getVisitingVehicle(int node) {
            return mTechId[node];
        }

        /**
         * Sets the id of the vehicle visiting the given node
         * 
         * @param node
         * @param techId
         */
        public void setVisitingVehicle(int node, int techId) {
            if (mTechId[node] != GiantPermRoute.UNDEFINED && techId != GiantPermRoute.UNDEFINED
                    && techId != mTechId[node])
                throw new IllegalStateException(String.format(
                        "Cannot affect %s to vehicle %s (already visited by %s)", node, techId,
                        mTechId[node]));
            mTechId[node] = techId;
        }

        /**
         * Gets the cumulative cost at a given node
         * 
         * @param nodeId
         *            the id of the considered node
         * @return the cumulative cost at the specified node
         */
        public double getCumulativeCost(int nodeId) {
            return mCumulativeCost[nodeId];
        }

        /**
         * Sets the cumulative cost at a given node
         * 
         * @param nodeId
         *            the id of the considered node
         * @param value
         *            the cumulative cost at the specified node
         */
        public void setCumulativeCost(int nodeId, double value) {
            mCumulativeCost[nodeId] = value;
        }

        /**
         * Gets the earliest arrival time at a given node, independently of the node time window.
         * 
         * @param nodeId
         *            the id of the considered node
         * @return the earliest arrival time at node with id <code>nodeId</code>
         */
        public double getEarliestArrivalTime(int nodeId) {
            return mEarliestArrivalTime[nodeId];
        }

        /**
         * Sets the earliest arrival time at a given node
         * 
         * @param nodeId
         *            the id of the considered node
         * @param time
         *            the earliest arrival time at node with id <code>nodeId</code>
         */
        public void setEarliestArrivalTime(int nodeId, double time) {
            mEarliestArrivalTime[nodeId] = time;
        }

        /**
         * Gets the waiting time at a given node, i.e. the time that the vehicle will be waiting before the start of the
         * time window.
         * 
         * @param nodeId
         *            the id of the considered node
         * @return the waiting time at node with id <code>nodeId</code>
         */
        public double getWaitingTime(int nodeId) {
            return mWaitingTime[nodeId][0];
        }

        /**
         * Gets the cumulative waiting time between two nodes, i.e. the total time the vehicle will spend waiting
         * between nodes <code>i</code> and <code>j</code>
         * 
         * @param i
         *            the first node
         * @param j
         *            the second node
         * @return the waiting time at node with id <code>nodeId</code>
         */
        public double getWaitingTime(int i, int j) {
            if (j < i) {
                int a = j;
                j = i;
                i = a;
            }

            return mWaitingTime[i][j - i];
        }

        /**
         * Sets the waiting time at a given node, i.e. the time that the vehicle will be waiting before the start of the
         * time window.
         * 
         * @param nodeId
         *            the id of the considered node
         * @param time
         *            the waiting time at node with id <code>nodeId</code>
         */
        public void setWaitingTime(int nodeId, double time) {
            mWaitingTime[nodeId][0] = time;
        }

        /**
         * Sets the cumulative waiting time between two nodes, i.e. the total time the vehicle will spend waiting
         * between nodes <code>i</code> and <code>j</code>
         * 
         * @param i
         *            the first node
         * @param j
         *            the second node
         * @param time
         *            the cumulative waiting time between <code>i</code> and <code>j</code>
         * @return the waiting time at node with id <code>nodeId</code>
         */
        public void setWaitingTime(int i, int j, double time) {
            if (j < i) {
                int a = j;
                j = i;
                i = a;
            }
            try {
                mWaitingTime[i][j - i] = time;
            } catch (ArrayIndexOutOfBoundsException e) {
                // FIXME remove
                e.printStackTrace();
                throw e;
            }
        }

        /**
         * Returns the forward slack time at node {@code  i} relative to the path {@code  (i,...,j)}
         * 
         * @param i
         * @param j
         * @return the forward slack time at node {@code  i} relative to the path {@code  (i,...,j)}
         */
        public double getFwdSlackTime(int i, int j) {
            if (j < i) {
                int a = j;
                j = i;
                i = a;
            }
            return mFwdTimeSlack[i][j - i];
        }

        /**
         * Sets the forward slack time at node {@code  i} relative to the path {@code  (i,...,j)}
         * 
         * @param i
         * @param j
         * @param slack
         *            the forward slack time at node {@code  i} relative to the path {@code  (i,...,j)}
         */
        public void setFwdSlackTime(int i, int j, double slack) {
            if (j < i) {
                int a = j;
                j = i;
                i = a;
            }

            mFwdTimeSlack[i][j - i] = slack;
        }

        /**
         * Gets the latest arrival time at a node so that the remaining of the route can be executed.
         * 
         * @param nodeId
         *            the id of the considered node
         * @return the latest feasible arrival time at node with id <code>nodeId</code>
         */
        public double getLatestFeasibleArrivalTime(int nodeId) {
            return mLatestFeasibleTime[nodeId];
        }

        /**
         * Sets the latest arrival time at a node so that the remaining of the route can be executed.
         * 
         * @param nodeId
         *            the id of the considered node
         * @param time
         *            the latest feasible arrival time at node with id <code>nodeId</code>
         */
        public void setLatestFeasibleTime(int nodeId, double time) {
            mLatestFeasibleTime[nodeId] = time;
        }

        /**
         * Gets the predecessor of a node.
         * 
         * @param node
         *            the node
         * @return the predecessor of <code>node</code>
         */
        public int getPred(int node) {
            return this.mPred[node];
        }

        /**
         * Sets the predecessor of a node.
         * 
         * @param node
         *            the node
         */
        public void setPred(int node, int pred) {
            this.mPred[node] = pred;
        }

        /**
         * Gets the successor of a node.
         * 
         * @param node
         *            the node
         * @return the successor of <code>node</code>
         */
        public int getSucc(int node) {
            return this.mSucc[node];
        }

        /**
         * Sets the predecessor of a node.
         * 
         * @param node
         *            the node
         * @param succ
         *            the successor of <code>node</code>
         */
        public void setSucc(int node, int succ) {
            this.mSucc[node] = succ;
        }

        protected void resetNodeData(int node) {
            setPred(node, GiantPermRoute.UNDEFINED);
            setSucc(node, GiantPermRoute.UNDEFINED);
            setVisitingVehicle(node, GiantPermRoute.UNDEFINED);
            // FIXME find why inconsistencies appear if this is not reset
            setCumulativeCost(node, NA);
            setEarliestArrivalTime(node, GiantPermutation.NA);
            setLatestFeasibleTime(node, GiantPermutation.NA);
            Arrays.fill(this.mCapacity[node], 0);
            Arrays.fill(this.mRequiredSpareParts[node], 0);
            setWaitingTime(node, NA);
            // if (!getParentInstance().isCVRPTW()) { FIXME implement this case
            // for (INodeVisit n : getParentInstance().getNodeVisits()) {
            // setWaitingTime(n.getID(), node, NA);
            // }
            // }
        }

        /**
         * Reset all the information stored in this instance
         */
        public void clear() {
            for (int n = 0; n < mPred.length; n++)
                resetNodeData(n);
        }

        /**
         * Check this giant permutation for cycles
         * 
         * @return the first cycle found in the permutation, or <code>null</code> if none was found
         */
        public List<Integer> checkForCycles() {
            boolean[] touched = new boolean[mPred.length];
            int start = 0;
            while (start < mPred.length) {
                if (!touched[start]) {
                    LinkedList<Integer> cycle = new LinkedList<>();
                    // start was not touched before
                    int n = start;
                    cycle.add(n);
                    touched[n] = true;
                    while (mSucc[n] != GiantPermRoute.UNDEFINED) {
                        cycle.add(n);
                        if (touched[n]) // We found a cycle
                            return cycle;
                        touched[n] = true;
                        n = mSucc[n];
                    }
                } else {
                    start++; // start was already touched: move to the next node
                }
            }
            return null;
        }

        public boolean isFrozen(int node) {
            return false;
        }
    }

    /** Reset this solution to its initial state */
    @Override
    public void clear() {
        getGiantPermutation().clear();
        for (GiantPermRoute t : this) {
            t.clear();
        }
        for (IVRPRequest r : getParentInstance().getRequests())
            getUnservedRequests().add(r);
    }

}