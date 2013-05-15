/**
 *
 */
package vroom.trsp.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.ISolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;

/**
 * <code>TRSPTour</code> is a doubly linked list representation of a tour using only nodes id. It is largely based on
 * {@link DoublyLinkedTour}
 * <p>
 * This class implements {@link ISolution} as it can be used to represent a solution of the single technician TRSP
 * </p>
 * <p>
 * Creation date: Feb 23, 2011 - 3:55:00 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */

public class TRSPTour extends TRSPTourBase {

    // TODO TRSPTour redesign: migrate all arrays to TRSPSolution and use getters/setters
    /** The Constant NA to represent unknown data, for instance for cumulative cost of a node that is not in the tour */
    public final static double NA           = Double.NaN;

    /** The technician which is executing this tour */
    private final Technician   mTechnician;

    /** The cumulative cost at each node. */
    private final double[]     mCumulatedCost;

    /** The earliest time of visit at each node. */
    private final double[]     mEarliestArrivalTime;

    /** The latest feasible arrival time at each node. */
    private final double[]     mLatestFeasibleTime;

    /** The total waiting time at between any two nodes */
    private final double[][]   mWaitingTime;

    /** The maximum lateness in this tour */
    private double             mMaxLateness = 0;

    /** The available tools at each node */
    private final boolean[][]  mTools;

    /** The available spare parts at each node */
    private final int[][]      mSpareParts;

    /** The number of spare parts required at each node and for the rest of the tour */
    private final int[][]      mRequiredSpareParts;

    /** <code>true</code> if the depot is visited prior to the corresponding node */
    private final boolean[]    mDepotVisited;

    /** The First Node ID. */
    private int                mFirst;

    /** The Last Node ID. */
    private int                mLast;

    /** The Length. */
    private int                mLength      = 0;

    /** The Predecessor of each node. */
    private final int[]        mPred;

    /** The Successor of each node. */
    private final int[]        mSucc;

    /**
     * the autoupdated flag: <code>true</code> if stored information such as cost should be automatically updated,
     * <code>false</code> otherwise
     **/
    private boolean            mAutoUpdated;

    /**
     * Getter for the auto-updated flag:.
     * 
     * @return <code>true</code> if stored information such as cost should be automatically updated, <code>false</code>
     *         otherwise
     */
    public boolean isAutoUpdated() {
        return this.mAutoUpdated;
    }

    /**
     * Setter for the auto-updated flag.
     * 
     * @param <code>true</code> if stored information such as cost should be automatically updated, <code>false</code>
     *        otherwise
     */
    public void setAutoUpdated(boolean autoUpdated) {
        boolean prev = this.mAutoUpdated;
        this.mAutoUpdated = autoUpdated;
        if (autoUpdated && !prev) {
            // Update the tour
            propagateUpdate(ITRSPTour.UNDEFINED, ITRSPTour.UNDEFINED, true);
            getCostDelegate().evaluateTour(this, true);
        }

    }

    /**
     * Creates a new <code>TRSPTour</code> based on the given <code>instance</code> and <code>technician</code>.
     * 
     * @param solution
     * @param technician
     */
    public TRSPTour(TRSPSolution solution, Technician technician) {
        super(solution);
        mTechnician = technician;

        this.mPred = new int[getInstance().getMaxId()];
        this.mSucc = new int[getInstance().getMaxId()];

        this.mCumulatedCost = new double[getInstance().getMaxId()];
        this.mEarliestArrivalTime = new double[getInstance().getMaxId()];
        this.mLatestFeasibleTime = new double[getInstance().getMaxId()];
        // this.mLateness = new double[getInstance().getMaxId()];
        this.mWaitingTime = new double[getInstance().getMaxId()][];
        for (int i = 0; i < mWaitingTime.length; i++) {
            mWaitingTime[i] = new double[mWaitingTime.length - i];
        }
        this.mTools = new boolean[getInstance().getMaxId()][getInstance().getToolCount()];
        this.mSpareParts = new int[getInstance().getMaxId()][getInstance().getSpareCount()];
        this.mRequiredSpareParts = new int[getInstance().getMaxId()][getInstance().getSpareCount()];
        this.mDepotVisited = new boolean[getInstance().getMaxId()];

        // Set to default all data
        // TODO TRSPTour redesign: remove this initialization
        for (int id = 0; id < getInstance().getMaxId(); id++) {
            mPred[id] = UNDEFINED;
            mSucc[id] = UNDEFINED;
            // We ignore this part as it is redundant with the fact that no node is present in the tour
            // mCumulatedCost[id]=NA;
            // mArrivalTime[id] = NA;
            // for (int i = 0; i < mSlackTime.length; i++) {
            // setSlackTime(i, id, NA);
            // }
            // mLatestFeasibleTime[id]=NA;
        }
        mDepotVisited[TRSPInstance.MAIN_DEPOT] = true;

        mFirst = ITRSPTour.UNDEFINED;
        mLast = ITRSPTour.UNDEFINED;
        mLength = 0;
        setTotalCost(0);

    }

    /**
     * Creates a new <code>TRSPTour</code> by cloning the given instance.
     * 
     * @param original
     *            the original tour
     */
    private TRSPTour(TRSPTour original) {
        super(original.getSolution());
        this.mPred = Arrays.copyOf(original.mPred, original.mPred.length);
        this.mSucc = Arrays.copyOf(original.mSucc, original.mSucc.length);

        this.mCumulatedCost = Arrays
                .copyOf(original.mCumulatedCost, original.mCumulatedCost.length);
        this.mEarliestArrivalTime = Arrays.copyOf(original.mEarliestArrivalTime,
                original.mEarliestArrivalTime.length);
        this.mLatestFeasibleTime = Arrays.copyOf(original.mLatestFeasibleTime,
                original.mLatestFeasibleTime.length);
        // this.mLateness = Arrays.copyOf(original.mLateness, original.mLateness.length);
        this.mWaitingTime = new double[original.mWaitingTime.length][];
        for (int i = 0; i < mWaitingTime.length; i++) {
            this.mWaitingTime[i] = Arrays.copyOf(original.mWaitingTime[i],
                    original.mWaitingTime[i].length);
        }

        this.mMaxLateness = original.mMaxLateness;
        setTotalCost(original.getTotalCost());

        this.mTools = new boolean[original.mTools.length][original.getInstance().getToolCount()];
        this.mSpareParts = new int[original.mSpareParts.length][original.getInstance()
                .getSpareCount()];
        this.mRequiredSpareParts = new int[original.mRequiredSpareParts.length][original
                .getInstance().getSpareCount()];
        for (int i = 0; i < mTools.length; i++) {
            this.mTools[i] = Arrays.copyOf(original.mTools[i], original.mTools[i].length);
            this.mSpareParts[i] = Arrays.copyOf(original.mSpareParts[i],
                    original.mSpareParts[i].length);
            this.mRequiredSpareParts[i] = Arrays.copyOf(original.mRequiredSpareParts[i],
                    original.mRequiredSpareParts[i].length);
        }
        this.mDepotVisited = Arrays.copyOf(original.mDepotVisited, original.mDepotVisited.length);

        this.mFirst = original.mFirst;
        this.mLast = original.mLast;
        this.mLength = original.mLength;

        this.mTechnician = original.mTechnician;

        // setAutoUpdated(original.isAutoUpdated());
        this.mAutoUpdated = original.mAutoUpdated;
    }

    /**
     * Creates a new <code>TRSPTour</code> by copying an existing tour and possibly changing the associated technician
     * 
     * @param solution
     *            the parent solution
     * @param tour
     *            the tour to be copied
     * @param technicianId
     *            the id of the new technician to be associated with this tour
     */
    public TRSPTour(TRSPSolution solution, ITRSPTour tour, int technicianId) {
        this(solution, solution.getInstance().getFleet().getVehicle(technicianId));
        for (Integer n : tour) {
            appendNode(n);
        }
    }

    /**
     * Getter for the parent instance
     * 
     * @return the parent instance of this tour
     */
    @Override
    public TRSPInstance getInstance() {
        return getSolution().getInstance();
    }

    @Override
    public Technician getTechnician() {
        return mTechnician;
    }

    @Override
    public int getTechnicianId() {
        return getTechnician().getID();
    }

    /**
     * Getter for the cost calculation delegate used to evaluate this tour.
     * 
     * @return the cost calculation delegate used to evaluate this tour.
     */
    public TRSPCostDelegate getCostDelegate() {
        return getSolution().getCostDelegate();
    }

    /**
     * Gets the cumulated cost at a given node
     * 
     * @param nodeId
     *            the id of the considered node
     * @return the cumulated cost at the specified node
     */
    public double getCumulatedCost(int nodeId) {
        return mCumulatedCost[nodeId];
    }

    /**
     * Sets the cumulated cost at a given node
     * 
     * @param nodeId
     *            the id of the considered node
     * @param value
     *            the cumulated cost at the specified node
     */
    public void setCumulatedCost(int nodeId, double value) {
        mCumulatedCost[nodeId] = value;
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
     * Gets the cumulated waiting time between two nodes, i.e. the total time the vehicle will spend waiting between
     * nodes <code>i</code> and <code>j</code>
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
     * Sets the cumulative waiting time between two nodes, i.e. the total time the vehicle will spend waiting between
     * nodes <code>i</code> and <code>j</code>
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

        mWaitingTime[i][j - i] = time;
    }

    /**
     * Gets the earliest departure time at a given node, depending on its time window and service time: <br/>
     * <code>max(ea[nodeId],a[nodeId])+s[nodeId]</code>
     * 
     * @param nodeId
     *            the id of the considered node
     * @return the earliest departure time at node with id <code>nodeId</code>
     * @see #getEarliestArrivalTime(int)
     */
    public double getEarliestDepartureTime(int nodeId) {
        return getTimeWindow(nodeId).getEarliestStartOfService(getEarliestArrivalTime(nodeId))
                + getServiceTime(nodeId);
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
     * Returns the total duration of this tour.
     * 
     * @return the total duration of this tour.
     */
    public double getTotalDuration() {
        if (getLastNode() == ITRSPTour.UNDEFINED)
            return 0;
        else
            return getEarliestDepartureTime(getLastNode());
    }

    /**
     * Gets the lateness at a given node
     * 
     * @param node
     *            the id of the considered node
     * @return the lateness at node with id <code>nodeId</code>
     */
    public double getLateness(int node) {
        // We assume that tws[node] + service[node] < twe[node], therefore getting to a node earlier ensures that there
        // will be no lateness
        return getTimeWindow(node).getViolation(getEarliestArrivalTime(node));
        // Lateness was disabled for performance
        // return mLateness[nodeId];
    }

    /**
     * Gets the maximum lateness in this tour
     * 
     * @return the maximum lateness in this tour
     */
    public double getMaxLateness() {
        // return Utilities.max(mLateness);
        throw new UnsupportedOperationException("Maximum lateness was disabled for performance");
        // return mMaxLateness;
    }

    /**
     * Returns <code>true</code> if the main depot is visited in this tour.
     * 
     * @return <code>true</code> if the main depot is visited in this tour.
     */
    public boolean isMainDepotVisited() {
        return isVisited(TRSPInstance.MAIN_DEPOT);
    }

    /**
     * Returns <code>true</code> if the main depot is visited in this tour before visiting <code>node</code>.
     * 
     * @param node
     *            the id of the considered node
     * @return <code>true</code> if the main depot is visited in this tour before visiting <code>node</code>.
     */
    public boolean isMainDepotVisited(int node) {
        return mDepotVisited[node];
    }

    /**
     * Returns <code>true</code> if a visit to the main depot is required to ensure the feasibility of this tour.
     * 
     * @return <code>true</code> if a visit to the main depot is required to ensure the feasibility of this tour.
     */
    public boolean isVisitToMainDepotRequired() {
        if (length() == 0)
            return false;
        if (Utilities.compare(mRequiredSpareParts[getFirstNode()], getTechnicianSpareParts()) > 0) {
            return true;
        }
        for (int node : this) {
            if (!getInstance().hasRequiredTools(getTechnician().getID(), node))
                return true;
        }

        return false;
    }

    /**
     * Append a single node to this tour.
     * 
     * @param node
     *            the node
     * @return true, if successful
     */
    public boolean appendNode(int node) {
        checkNode(node);

        if (mLast != ITRSPTour.UNDEFINED) {// Tour is not empty
            setSucc(mLast, node);
            setPred(node, mLast);
        } else { // Tour is empty
            // Initialize the tour
            this.mFirst = node;
        }
        mLength++;
        this.mLast = node;

        propagateUpdate(getLastNode(), getLastNode(), false);

        getCostDelegate().nodeInserted(this, getPred(node), node, ITRSPTour.UNDEFINED);

        return true;
    }

    /**
     * Append nodes.
     * 
     * @param nodes
     *            the nodes
     * @return true, if successful
     */
    public boolean appendNodes(List<Integer> nodes) {
        for (int n : nodes) {
            appendNode(n);
        }
        return true;
    }

    @Override
    public TRSPTour clone() {
        // TODO TRSPTour redesign: throw an exception here - cloning should only supported at solution level
        return new TRSPTour(this);
    }

    /**
     * Extract node at a specific index.
     * <p>
     * Complexity is in <code>O(n/2)</code> for the actual removal, and <code>O(n)</code> for the update
     * </p>
     * 
     * @param index
     *            the index in the tour of the node to be removed
     * @return the id of the node that was at <code>index</code>
     */
    public int extractNode(int index) {
        int node = getNodeAt(index);

        removeNode(node);

        return node;
    }

    /**
     * Remove a given node from this tour
     * <p>
     * Runs in constant time for the actual removal, and <code>O(n)</code> for the update
     * </p>
     * 
     * @param node
     *            the node to be removed
     * @return <code>true</code> if the node was present in the tour
     */
    public boolean removeNode(int node) {
        if (!isVisited(node))
            return false;

        int pred = getPred(node);
        int succ = getSucc(node);

        if (pred != ITRSPTour.UNDEFINED)
            setSucc(pred, succ);
        else
            this.mFirst = succ;
        if (succ != ITRSPTour.UNDEFINED)
            setPred(succ, pred);
        else
            this.mLast = pred;

        // Erase information stored for the removed node
        resetNodeData(node);

        // Update length
        this.mLength--;

        // Propagate from the predecessor
        propagateUpdate(succ, pred, false);

        getCostDelegate().nodeRemoved(this, pred, node, succ);

        return pred != ITRSPTour.UNDEFINED || succ != ITRSPTour.UNDEFINED;
    }

    /**
     * Remove all nodes from this tour and reset all stored information
     */
    public void clear() {
        for (int i = 0; i < mPred.length; i++) {
            resetNodeData(i);
        }
    }

    /**
     * Reset to default values the data stored for a node
     * 
     * @param node
     *            the id of the node to be erased
     */
    private void resetNodeData(int node) {
        setPred(node, ITRSPTour.UNDEFINED);
        setSucc(node, ITRSPTour.UNDEFINED);
        Arrays.fill(this.mTools[node], false);
        Arrays.fill(this.mSpareParts[node], 0);
        Arrays.fill(this.mRequiredSpareParts[node], 0);
        setCumulatedCost(node, NA);
        setEarliestArrivalTime(node, NA);
        for (int i = 0; i < mWaitingTime.length; i++) {
            setWaitingTime(i, node, NA);
        }
        setLatestFeasibleTime(node, NA);
        mDepotVisited[node] = node == TRSPInstance.MAIN_DEPOT;
    }

    /**
     * Extract subtour.
     * 
     * @param start
     *            the start
     * @param end
     *            the end
     * @return the list
     */
    public List<Integer> extractSubtour(int start, int end) {
        List<Integer> subtour = new ArrayList<Integer>(end - start + 1);

        int node = getNodeAt(start);
        int pred = getPred(node);
        int succ = getSucc(node);

        int index = start;

        while (index <= end) {
            subtour.add(node);
            succ = getSucc(node);
            // Erase the node info
            resetNodeData(node);
            // Update length
            this.mLength--;
            node = succ;
            index++;
        }
        if (pred != ITRSPTour.UNDEFINED)
            setSucc(pred, succ);
        if (succ != ITRSPTour.UNDEFINED)
            setPred(succ, pred);

        propagateUpdate(succ, pred, false);

        getCostDelegate().subtourRemoved(this, pred, subtour, succ);

        return subtour;
    }

    /**
     * Insert a node before a request of this tour.
     * 
     * @param pred
     *            the request after which the <code>node</code> will be inserted, if equal to
     *            {@link ITRSPTour#UNDEFINED} the <code>node</code> will be inserted at the head
     * @param node
     *            the node to be inserted
     * @return true, if successful
     */
    public boolean insertAfter(int pred, int node) {
        checkNode(node);

        if (!isVisited(pred))
            throw new IllegalArgumentException("The insertion successor " + pred
                    + " is not visited by this tour");

        int succ;

        if (pred == ITRSPTour.UNDEFINED) {
            if (length() == 0) {
                appendNode(node);
                return true;
            } else {
                succ = getFirstNode();
                setFirst(node);
            }
        } else {
            succ = getSucc(pred);
            setSucc(pred, node);
        }

        setPred(node, pred);

        setPred(succ, node);
        setSucc(node, succ);
        // Update length
        this.mLength++;

        propagateUpdate(node, node, false);

        getCostDelegate().nodeInserted(this, getPred(node), node, getSucc(node));

        return true;
    }

    /**
     * Insert a node before a request of this tour.
     * 
     * @param succ
     *            the request before which the <code>node</code> will be inserted, if equal to
     *            {@link ITRSPTour#UNDEFINED} the <code>node</code> will be appended
     * @param node
     *            the node to be inserted
     * @return true, if successful
     */
    public boolean insertBefore(int succ, int node) {
        checkNode(node);

        if (!isVisited(succ))
            throw new IllegalArgumentException("The insertion successor " + succ
                    + " is not visited by this tour");

        if (succ == ITRSPTour.UNDEFINED) {
            appendNode(node);
            return true;
        }

        int pred = getPred(succ);

        if (pred != ITRSPTour.UNDEFINED)
            setSucc(pred, node);
        else
            setFirst(node);

        setPred(node, pred);

        setPred(succ, node);
        setSucc(node, succ);
        // Update length
        this.mLength++;

        propagateUpdate(node, node, false);

        getCostDelegate().nodeInserted(this, getPred(node), node, getSucc(node));

        return true;
    }

    /**
     * Insert nodes.
     * 
     * @param index
     *            the index
     * @param subtour
     *            the subtour
     * @return true, if successful
     */
    public boolean insertNodes(int index, List<Integer> subtour) {

        if (subtour.isEmpty())
            return true;

        final int current = getNodeAt(index);
        final int predecessor = getPred(current);

        Iterator<? extends Integer> it = subtour.iterator();
        int first = ITRSPTour.UNDEFINED, last = ITRSPTour.UNDEFINED;
        int pred = it.next();
        int succ = pred;
        first = pred;
        while (it.hasNext()) {
            checkNode(pred);

            succ = it.next();

            setPred(succ, pred);
            setSucc(pred, succ);

            pred = succ;
        }
        last = succ;

        setSucc(getPred(current), first);
        setPred(first, getPred(current));

        setPred(current, last);
        setSucc(last, current);

        // Update length
        this.mLength += subtour.size();

        propagateUpdate(predecessor, current, false);
        getCostDelegate().tourInserted(this, predecessor, subtour, current);

        return true;
    }

    /**
     * Reverse subtour.
     * 
     * @param start
     *            the start
     * @param end
     *            the end
     * @return true, if successful
     */
    public boolean reverseSubtour(int start, int end) {
        if (start == end)
            return true;

        int first = getNodeAt(start), last = first;
        int pred = getPred(first);

        int node = first, succ = first;
        int index = start;
        // Reverse the subtour
        while (index <= end) {
            succ = getSucc(node);
            setSucc(node, getPred(node));
            setPred(node, succ);
            last = node;
            node = succ;
            index++;
        }

        // Relink the subtour extremities
        setSucc(first, succ);
        setPred(succ, first);
        setSucc(last, pred);
        setPred(pred, last);

        getCostDelegate().subtourReversed(this, pred, first, last, succ);

        return true;
    }

    /**
     * Sets the node at a specific index.
     * 
     * @param index
     *            the index
     * @param node
     *            the node
     * @return the node that was previously at {@code  index}
     */
    public int setNodeAt(int index, int node) {
        int old = getNodeAt(index);

        setNode(old, node);

        return old;
    }

    /**
     * Replace a node by another
     * 
     * @param old
     *            the node to be replaced
     * @param node
     *            the new node
     * @throws IllegalStateException
     *             if {@code  node} is node visited by this tour
     */
    public void setNode(int old, int node) {
        if (!isVisited(old))
            throw new IllegalStateException("The specified node is not visited by this tour");
        checkNode(node);

        int pred = getPred(old);
        int succ = getSucc(old);
        if (pred != ITRSPTour.UNDEFINED)
            setSucc(pred, node);
        if (succ != ITRSPTour.UNDEFINED)
            setPred(succ, node);
        setPred(node, pred);
        setSucc(node, succ);
        resetNodeData(old);

        getCostDelegate().nodeReplaced(this, getPred(node), old, node, getSucc(node));
    }

    /**
     * Gets the first node.
     * 
     * @return the first node
     */
    @Override
    public int getFirstNode() {
        return mFirst;
    }

    /**
     * Sets the first node.
     * 
     * @param first
     *            the first node of the tour
     */
    public void setFirst(int first) {
        mFirst = first;
    }

    /**
     * Gets the last node.
     * 
     * @return the last node
     */
    @Override
    public int getLastNode() {
        return mLast;
    }

    /**
     * Sets the last node.
     * 
     * @param last
     *            the last node of the tour
     */
    public void setLast(int last) {
        mLast = last;
    }

    /**
     * Gets the node id at.
     * 
     * @param index
     *            the index
     * @return the node id at
     */
    @Override
    public int getNodeAt(int index) {
        if (index < 0 || index >= length())
            throw new IllegalArgumentException(String.format("Index if out of range: %s [%s]",
                    index, length()));
        if (index < length() / 2) {
            int cursor = 0;
            int node = mFirst;
            while (cursor < index) {
                cursor++;
                node = getSucc(node);
            }
            return node;
        } else {
            int cursor = length() - 1;
            int node = mLast;
            while (cursor > index) {
                cursor--;
                node = getPred(node);
            }
            return node;
        }
    }

    /**
     * Gets the node position.
     * 
     * @param node
     *            the node
     * @return the node position
     */
    public int getNodePosition(int node) {
        // Complexity : O(n)
        int cursor = 0;
        int pred = node;

        while (getPred(pred) != ITRSPTour.UNDEFINED) {
            pred = getPred(pred);
            cursor++;
        }
        return cursor;
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

    /*
     * (non-Javadoc)
     * @see vroom.trsp.datamodel.ITRSPTour#iterator()
     */
    @Override
    public TRSPTourIterator iterator() {
        return new TRSPTourIterator();
    }

    /**
     * Iterator starting from a given node
     * 
     * @param nodeId
     *            the id of the first considered node (if {@link ITRSPTour#UNDEFINED} then the iterator will start at
     *            the beginning of the tour)
     * @return a list iterator starting at the node <code>nodeId</code>
     */
    public TRSPTourIterator iterator(int nodeId) {
        return new TRSPTourIterator(nodeId);
    }

    /**
     * Return the tour length, in number of visited nodes, including depots.
     * 
     * @return the tour length, in number of visited nodes, including depots
     */
    @Override
    public int length() {
        return mLength;
    }

    /**
     * Propagates the available tools, spare parts, earliest arrival time and lateness from the predecessor of
     * <code>fwdNode</code> recursively to all successors, and the latest feasible arrival time and required spare parts
     * from the successor of <code>bwdNode</code> recursively to all its predecessors
     * 
     * @param fwdNode
     *            the node from which update will be propagated forward, if equal to {@link ITRSPTour#UNDEFINED} the
     *            first node will be used
     * @param bwdNode
     *            the node from which update will be propagated backward, if equal to {@link ITRSPTour#UNDEFINED} the
     *            last node will be used
     * @param force
     *            set to <code>true</code> to force the propagation to the all the tour (for example when tour was
     *            changed in more than one point), set to <code>false</code> to stop propagation as soon as stored
     *            values equal calculated values (for example when tour was changed only at one point).
     */
    public void propagateUpdate(int fwdNode, int bwdNode, boolean force) {
        // Ignore if auto update is disabled
        if (!isAutoUpdated())
            return;
        // Ignore tour is empty
        if (length() == 0)
            return;

        if (fwdNode == ITRSPTour.UNDEFINED)
            fwdNode = getFirstNode();

        propagateEarliestTime(fwdNode, force);
        propagateWaitingTime(fwdNode);
        propagateTools(fwdNode, force);
        propagateSpareParts(fwdNode, force);
        propagateDepotVisitedUpdateFwd(fwdNode, force);

        if (bwdNode == ITRSPTour.UNDEFINED)
            bwdNode = getLastNode();
        propagateLatestFeasibleArrivalTime(bwdNode, force);
        propagateRequiredSpareParts(bwdNode, force);
    }

    /**
     * Sets the depot visited flag for <code>node</code> by propagating the flag from its predecessor, and recursively
     * call the method on its successor.
     * 
     * @param node
     *            the considered node
     * @param force
     *            set to <code>true</code> to force the propagation to the all the tour, set to <code>false</code> to
     *            stop propagation as soon as stored values equal calculated values
     */
    protected void propagateDepotVisitedUpdateFwd(int node, boolean force) {
        if (node == ITRSPTour.UNDEFINED)
            return; // Nothing to do
        int pred = getPred(node);
        boolean old = mDepotVisited[node];
        if (node == TRSPInstance.MAIN_DEPOT)
            // Node is a depot
            mDepotVisited[node] = true;
        else if (pred == ITRSPTour.UNDEFINED)
            // Start of the tour (and node is not the main depot)
            mDepotVisited[node] = false;
        else
            // Propagate change
            mDepotVisited[node] = mDepotVisited[pred];

        // Recursive call
        if (force || old != isMainDepotVisited(node) || node == TRSPInstance.MAIN_DEPOT)
            propagateDepotVisitedUpdateFwd(getSucc(node), force);
    }

    /**
     * Sets the depot visited flag for <code>node</code> by propagating the flag from its predecessor, and recursively
     * call the method on its successor.
     * 
     * @param node
     *            the considered node
     * @param force
     *            set to <code>true</code> to force the propagation to the all the tour, set to <code>false</code> to
     *            stop propagation as soon as stored values equal calculated values
     */
    protected void propagateDepotVisitedUpdateBwd(int node, boolean force) {
        if (node == ITRSPTour.UNDEFINED)
            return; // Nothing to do
        int succ = getSucc(node);
        boolean old = mDepotVisited[node];
        if (node == TRSPInstance.MAIN_DEPOT)
            // Node is a depot
            mDepotVisited[node] = true;
        else if (succ == ITRSPTour.UNDEFINED)
            // End of the tour
            mDepotVisited[node] = node == TRSPInstance.MAIN_DEPOT;
        else
            // Propagate change
            mDepotVisited[node] = succ != TRSPInstance.MAIN_DEPOT && mDepotVisited[succ];

        // Recursive call
        if (force || old != isMainDepotVisited(node) || node == TRSPInstance.MAIN_DEPOT)
            propagateDepotVisitedUpdateBwd(getPred(node), force);
    }

    /**
     * Sets the waiting time time for <code>node</code> by propagating the time from its predecessor, and recursively
     * call the method on its successor. This method also updates lateness
     * <p>
     * Please note that this method assumes that the {@linkplain #getEarliestArrivalTime(int) earliest arrival} is
     * properly defined
     * </p>
     * 
     * @param node
     *            the considered node
     */
    protected void propagateWaitingTime(int node) {
        if (node == UNDEFINED || length() == 0)
            // Nothing to do
            return;
        int i, j;
        TRSPTourIterator it = iterator(node);

        // Update the waiting time of node and its successors
        while (it.hasNext()) {
            i = it.next();
            /*
             * The waiting time at node is equal to max(0,tws[node] - early[node])
             */
            setWaitingTime(i,
                    Math.max(0, getTimeWindow(i).startAsDouble() - getEarliestArrivalTime(i)));
        }

        // Update the cumulated waiting time of all nodes
        it = iterator();
        double cumWait = 0;
        // If true then the sub-loop will start at the changed node
        boolean jumpToNode = true;
        i = getFirstNode();
        while (it.hasNext() && getSucc(i) != UNDEFINED) {
            if (i == node)
                jumpToNode = false;
            // The start node for the sub-loop
            int startNode = jumpToNode ? node : getSucc(i);
            int predStartNode = getPred(startNode);
            if (predStartNode == UNDEFINED || predStartNode == i)
                cumWait = 0;
            else
                cumWait = getWaitingTime(i, predStartNode) + getWaitingTime(predStartNode);

            TRSPTourIterator succ = iterator(startNode);
            while (succ.hasNext()) {
                j = succ.next();
                setWaitingTime(i, j, cumWait);
                cumWait += getWaitingTime(j);
            }

            i = it.next();
        }

        // Recursive call
        propagateWaitingTime(getSucc(node));
    }

    /**
     * Sets the earliest visit time for <code>node</code> by propagating the time from its predecessor, and recursively
     * call the method on its successor. This method also updates lateness
     * 
     * @param node
     *            the considered node
     * @param force
     *            set to <code>true</code> to force the propagation to the all the tour, set to <code>false</code> to
     *            stop propagation as soon as stored values equal calculated values
     */
    protected void propagateEarliestTime(int node, boolean force) {
        if (node == ITRSPTour.UNDEFINED)
            return; // Nothing to do

        double old = getEarliestArrivalTime(node);
        int pred = getPred(node);
        if (pred == ITRSPTour.UNDEFINED)
            // Start of the tour
            setEarliestArrivalTime(node, getEarliestStartTime());
        else
            /*
             * The earliest time at node is equal to max(early[pred],tws[pred]) + service[pred] + travel[pred,node] This
             * means that we consider that the service of the predecessor cannot be started before the start of its time
             * window
             */
            setEarliestArrivalTime(node, getEarliestDepartureTime(pred) + getTravelTime(pred, node));

        // We assume that tws[node] + service[node] < twe[node], therefore getting to a node earlier ensures that there
        // will be no lateness
        // double lateness = getTimeWindow(node).getViolation(getArrivalTime(node));
        // if (lateness > getMaxLateness())
        // mMaxLateness = lateness;
        // mLateness[node] = lateness;

        // Recursive call
        if (force || old != getEarliestArrivalTime(node))
            propagateEarliestTime(getSucc(node), force);
    }

    /**
     * Sets the latest feasible arrival time for <code>node</code> by propagating the time at <code>succ</code>
     * 
     * @param succ
     *            the <code>node</code> successor
     * @param node
     *            the considered node
     * @param force
     *            set to <code>true</code> to force the propagation to the all the tour, set to <code>false</code> to
     *            stop propagation as soon as stored values equal calculated values
     */
    protected void propagateLatestFeasibleArrivalTime(int node, boolean force) {
        if (node == ITRSPTour.UNDEFINED)
            return; // Nothing to do

        double old = getLatestFeasibleArrivalTime(node);
        int succ = getSucc(node);
        if (succ == ITRSPTour.UNDEFINED)
            // End of the tour: twe[node]
            setLatestFeasibleTime(node, getTimeWindow(node).endAsDouble());
        else {
            /*
             * The latest feasible arrival time is equal to min(twe[node],late[succ]-service[node]-travel[node,succ])
             */
            setLatestFeasibleTime(
                    node,
                    Math.min(getTimeWindow(node).endAsDouble(), getLatestFeasibleArrivalTime(succ)
                            - getServiceTime(node) - getTravelTime(node, succ)));
        }

        // Recursive call
        if (force || old != getLatestFeasibleArrivalTime(node))
            propagateLatestFeasibleArrivalTime(getPred(node), force);
    }

    /**
     * Sets the available tools for <code>node</code> by propagating the available tools from its predecessor, and
     * recursively call the method on its successor.
     * 
     * @param node
     *            the considered node
     * @param force
     *            set to <code>true</code> to force the propagation to the all the tour, set to <code>false</code> to
     *            stop propagation as soon as stored values equal calculated values
     */
    protected void propagateTools(int node, boolean force) {
        if (node == ITRSPTour.UNDEFINED)
            return; // Nothing to do

        boolean changed = false;
        if (isMainDepot(node))
            // Main depot: assumes all tools are available
            for (int t = 0; t < this.mTools[node].length; t++) {
                if (this.mTools[node][t] != true) {
                    this.mTools[node][t] = true;
                    changed = true;
                }
            }
        else if (getPred(node) == ITRSPTour.UNDEFINED)
            // No predecessor: assumes start of the tour
            for (int t = 0; t < this.mTools[node].length; t++) {
                if (this.mTools[node][t] != getTechnician().getToolSet().hasAttribute(t)) {
                    this.mTools[node][t] = getTechnician().getToolSet().hasAttribute(t);
                    changed = true;
                }
            }
        else
            // Propagate the tools available at the predecessor
            for (int t = 0; t < this.mTools[node].length; t++) {
                if (this.mTools[node][t] != this.mTools[getPred(node)][t]) {
                    this.mTools[node][t] = this.mTools[getPred(node)][t];
                    changed = true;
                }
            }

        // Recursive call
        if (force || changed)
            propagateTools(getSucc(node), force);
    }

    /**
     * Sets the available spare parts for <code>node</code> by propagating the available spare parts from its
     * predecessor, and recursively call the method on its successor.
     * 
     * @param node
     *            the considered node
     * @param force
     *            set to <code>true</code> to force the propagation to the all the tour, set to <code>false</code> to
     *            stop propagation as soon as stored values equal calculated values
     */
    protected void propagateSpareParts(int node, boolean force) {
        if (node == ITRSPTour.UNDEFINED)
            return; // Nothing to do

        boolean changed = false;
        if (isMainDepot(node))
            // Main depot: assumes all spare parts are available
            for (int p = 0; p < getInstance().getSpareCount(); p++) {
                if (this.mSpareParts[node][p] != Integer.MAX_VALUE) {
                    this.mSpareParts[node][p] = Integer.MAX_VALUE;
                    changed = true;
                }
            }
        else if (getPred(node) == ITRSPTour.UNDEFINED)
            // No predecessor: assumes start of the tour
            for (int p = 0; p < getInstance().getSpareCount(); p++) {
                // Available spare parts, ignoring requirements if a visit to the depot was made before
                int av = getTechnician().getAvailableSpareParts(p) == Integer.MAX_VALUE ? Integer.MAX_VALUE
                        : getTechnician().getAvailableSpareParts(p)
                                - getInstance().getSparePartReq(node, p);
                if (this.mSpareParts[node][p] != av) {
                    this.mSpareParts[node][p] = av;
                    changed = true;
                }
            }

        else
            // Propagate the spare parts available at the predecessor
            for (int p = 0; p < getInstance().getSpareCount(); p++) {
                // Available spare parts, ignoring requirements is a visit to the depot was made before
                int av = getAvailableSpareParts(getPred(node), p) == Integer.MAX_VALUE ? Integer.MAX_VALUE
                        : getAvailableSpareParts(getPred(node), p)
                                - getInstance().getSparePartReq(node, p);
                if (this.mSpareParts[node][p] != av) {
                    this.mSpareParts[node][p] = av;
                    changed = true;
                }
            }

        // Recursive call
        if (force || changed)
            propagateSpareParts(getSucc(node), force);

    }

    /**
     * Sets the required spare parts for <code>node</code> by propagating the required spare parts from its successor,
     * and recursively call the method on its predecessor.
     * 
     * @param node
     *            the considered node
     * @param force
     *            set to <code>true</code> to force the propagation to the all the tour, set to <code>false</code> to
     *            stop propagation as soon as stored values equal calculated values
     */
    protected void propagateRequiredSpareParts(int node, boolean force) {
        if (node == ITRSPTour.UNDEFINED)
            return; // Nothing to do

        boolean changed = false;
        // if (getInstance().isMainDepot(node)) {
        // // Reset the requirements
        // Arrays.fill(mRequiredSpareParts[node], 0);
        // } else

        // The requirements of the successor
        int[] succReq = getSucc(node) != ITRSPTour.UNDEFINED ? mRequiredSpareParts[getSucc(node)]
                : new int[getInstance().getSpareCount()];
        // The requirements for this node
        int[] nodeReq = getInstance().isRequest(node) ? getInstance().getRequest(node)
                .getSparePartRequirements() : new int[getInstance().getSpareCount()];

        // Update the stored requirements
        for (int p = 0; p < nodeReq.length; p++) {
            int old = mRequiredSpareParts[node][p];
            mRequiredSpareParts[node][p] = succReq[p] + nodeReq[p];
            changed |= old != mRequiredSpareParts[node][p];
        }

        // Recursive call
        if (force || changed)
            propagateRequiredSpareParts(getPred(node), force);
    }

    @Override
    public boolean isVisited(int node) {
        return node != ITRSPTour.UNDEFINED
                && (getPred(node) != ITRSPTour.UNDEFINED || getSucc(node) != ITRSPTour.UNDEFINED || (node == mFirst && node == mLast));
    }

    @Override
    public Comparable<?> getObjective() {
        return getTotalCost();
    }

    @Override
    public double getObjectiveValue() {
        return getTotalCost();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(length() * 3);

        sb.append(String.format("t:%s c:%.2f d:%.2f l:%s <", getTechnician().getID(),
                getTotalCost(), getTotalDuration(), length()));

        TRSPTourIterator it = iterator();
        int[] spreq = new int[getTechnician().getCompartmentCount()];
        while (it.hasNext()) {
            int n = it.next();
            for (int s = 0; s < spreq.length; s++)
                if (getInstance().isRequest(n))
                    spreq[s] += getInstance().getSparePartReq(n, s);
                else
                    spreq[s] = 0;
            // sb.append(String.format("%s[et:%.2f,lft:%.2f,l:%.2f]", n, getEarliestArrivalTime(n),
            // getLatestFeasibleArrivalTime(n), getLateness(n)));
            // if (getLateness(n) > 0)
            // sb.append(String.format("%s[l:%.1f]", n, getLateness(n)));
            // else
            sb.append(n);
            // sb.append(String.format("%s%s", n, Utilities.toShortString(spreq)));
            // sb.append(String.format("%s%s", n, isMainDepotVisited(n) ? "+" : ""));
            if (it.hasNext())
                sb.append(",");
        }
        sb.append(">");

        return sb.toString();

    }

    @Override
    public String getNodeSeqString() {
        StringBuilder sb = new StringBuilder(length() * 3);

        sb.append("<");

        Iterator<Integer> it = iterator();
        while (it.hasNext()) {
            int n = it.next();
            sb.append(n);
            if (it.hasNext())
                sb.append(",");
        }
        sb.append(">");

        return sb.toString();
    }

    /**
     * Checks if a tool is available at a given node
     * 
     * @param node
     *            the considered node
     * @param tool
     *            the considered tool
     * @return <code>true</code> if the <code>tool</code> is available at the given <code>node</code>
     */
    public boolean isToolAvailable(int node, int tool) {
        if (node == ITRSPTour.UNDEFINED)
            return getTechnician().getToolSet().hasAttribute(tool);
        return mTools[node][tool];
    }

    /**
     * Gets the number of spare parts available after servicing a given node
     * 
     * @param node
     *            the considered node
     * @param type
     *            the considered spare part type
     * @return the number of spare parts of <code>type</code> available after servicing the given <code>node</code>, or
     *         at the start of the tour if <code>node={@link ITRSPTour#UNDEFINED}</code>
     */
    public int getAvailableSpareParts(int node, int type) {
        if (node == ITRSPTour.UNDEFINED)
            return getTechnician().getAvailableSpareParts(type);
        return mSpareParts[node][type];
    }

    /**
     * Gets the number of spare parts required to serve the tour starting at a given node.
     * <p>
     * The returned value ignores possible trips to the depot in the following nodes.
     * </p>
     * 
     * @param node
     *            the considered node
     * @param type
     *            the considered spare part type
     * @return the number of spare parts of <code>type</code> required to serve the given <code>node</code> and all
     *         subsequent nodes, or 0 if <code>node={@link ITRSPTour#UNDEFINED}</code>
     */
    public int getRequiredSpareParts(int node, int type) {
        if (node == ITRSPTour.UNDEFINED)
            return 0;
        return mRequiredSpareParts[node][type];
    }

    /**
     * Check if a node can be inserted/added/appended to this tour
     * 
     * @param node
     *            the node to check
     * @throws IllegalArgumentException
     *             if the node is already present in the tour.
     */
    protected void checkNode(int node) throws IllegalArgumentException {
        if (isVisited(node))
            throw new IllegalArgumentException(String.format(
                    "Node %s is already visited in this tour", node));
    }

    /**
     * The Class <code>TRSPTourIterator</code> is an implementation of {@link ListIterator} to iterate over a.
     * {@link TRSPTour}
     * <p>
     * Creation date: Feb 16, 2011 - 5:15:56 PM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    public class TRSPTourIterator implements ITourIterator {

        /** <code>true</code> if the value of <code>cursor</code> cannot be trusted */
        private final boolean noCursor;

        /** The current node, which will be returned at the next call to {@link #next()}. */
        private int           current;

        /** The cursor. */
        private int           cursor;

        /**
         * Instantiates a new tRSP tour iterator.
         */
        protected TRSPTourIterator() {
            current = getFirstNode();
            cursor = 0;
            noCursor = false;
        }

        /**
         * Creates a new <code>TRSPTourIterator</code> starting at the given node.
         * <p>
         * Note that the first call to {@link #next()} of {@link #previous()} will return <code>nodeId</code>
         * </p>
         * 
         * @param nodeId
         *            the id of the current node. If equal to {@link ITRSPTour#UNDEFINED} then the iterator will be
         *            initialized with the first node
         */
        public TRSPTourIterator(int nodeId) {
            this(nodeId != ITRSPTour.UNDEFINED ? nodeId : getFirstNode(), 0,
                    nodeId != ITRSPTour.UNDEFINED);
        }

        /**
         * Creates a new <code>TRSPTourIterator</code>
         * 
         * @param noCursor
         * @param current
         * @param cursor
         */
        private TRSPTourIterator(int current, int cursor, boolean noCursor) {
            super();
            this.noCursor = noCursor;
            this.current = current;
            this.cursor = cursor;
        }

        @Override
        public void add(Integer e) {
            TRSPTour.this.insertBefore(current, e);
        }

        @Override
        public boolean hasNext() {
            return current != ITRSPTour.UNDEFINED;
        }

        @Override
        public boolean hasPrevious() {
            return current != ITRSPTour.UNDEFINED;
        }

        @Override
        public Integer next() {
            if (hasNext()) {
                cursor++;
                int c = current;
                current = TRSPTour.this.getSucc(current);
                return c;
            } else
                throw new NoSuchElementException();
        }

        @Override
        public int nextIndex() {
            if (noCursor)
                throw new IllegalStateException(
                        "Cannot trust the index position when the iterator was started from an initial node different from the tour start");
            return cursor + 1;
        }

        @Override
        public Integer previous() {
            if (hasPrevious()) {
                cursor--;
                int c = current;
                current = TRSPTour.this.getPred(current);
                return c;
            } else
                throw new NoSuchElementException();
        }

        @Override
        public int previousIndex() {
            if (noCursor)
                throw new IllegalStateException(
                        "Cannot trust the index position when the iterator was started from an initial node different from the tour start");
            return cursor - 1;
        }

        @Override
        public void remove() {
            TRSPTour.this.removeNode(getPred(current));
        }

        @Override
        public void set(Integer e) {
            throw new UnsupportedOperationException(
                    "set operation is not supported by this iterator");
        }

        @Override
        protected ITourIterator clone() {
            return new TRSPTourIterator(current, cursor, noCursor);
        }

        @Override
        public ITourIterator subIterator() {
            return clone();
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TRSPTour))
            return false;

        TRSPTour tour = (TRSPTour) obj;

        if (getTechnician().getID() != tour.getTechnician().getID())
            return false;

        if (length() != tour.length())
            return false;

        TRSPTourIterator thisIt = iterator();
        TRSPTourIterator tourIt = tour.iterator();

        while (thisIt.hasNext()) {
            if (thisIt.next() != tourIt.next())
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getSolution() != null ? getSolution().getInstance().getSolutionHasher().hash(this)
                : super.hashCode();
    }

}
