/**
 * 
 */
package vroom.trsp.optimization.rch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.utilities.IDisposable;
import vroom.common.utilities.IObservable;
import vroom.common.utilities.IObserver;
import vroom.common.utilities.Update;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.TRSPHeuristic;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.optimization.split.SplitTourArcBuilder;
import vroom.trsp.optimization.split.TRSPSplit;
import vroom.trsp.util.TRSPGlobalParameters;

/**
 * <code>TRSPRndConstructiveHeuristic</code>
 * <p>
 * Creation date: Sep 22, 2011 - 1:48:19 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class TRSPRndConstructiveHeuristic extends TRSPHeuristic {

    /**
     * {@code true} if the heuristic will only be used to generate giant tours, {@code false} for feasible tours only
     */
    private final boolean mGiantTours;

    /**
     * Returns {@code true} if the heuristic will only be used to generate giant tours, {@code false} for feasible tours
     * only
     * 
     * @return {@code true} if the heuristic will only be used to generate giant tours, {@code false} for feasible tours
     *         only
     */
    public boolean isGiantTours() {
        return mGiantTours;
    }

    /** {@code true} if TW feasibility should be checked when building a giant tour */
    private final boolean mCheckTWFeas;

    /**
     * Returns {@code true} if TW feasibility should be checked when building a giant tour
     * 
     * @return {@code true} if TW feasibility should be checked when building a giant tour
     */
    public boolean isCheckTWFeas() {
        return mCheckTWFeas;
    }

    private final int mKmax;

    /**
     * Returns the K<sub>max</sub> value
     * 
     * @return the K<sub>max</sub> value
     */
    public int getKmax() {
        return mKmax;
    }

    /** The collection containing the tours that were generated during last iteration */
    private final Collection<ITRSPTour> mTourPool;

    /**
     * Returns the collection containing the tours that were generated during last iteration
     * 
     * @return the collection containing the tours that were generated during last iteration
     */
    public Collection<ITRSPTour> getTourPool() {
        return mTourPool;
    }

    /** The randomized factor, read at construction time for performance */
    private final double             mRndFact;

    /** The spliting procedure */
    private final TRSPSplit          mSplit;

    /** the observable node associated with each request */
    private final ObservableNode[]   mObsNodes;

    /** List of the compatible requests for each technician */
    private final ObservableNode[][] mCompRequests;

    /**
     * Creates a new <code>TRSPRndConstructiveHeuristic</code>
     * 
     * @param parameters
     *            the global parameters used in this heuristic
     * @param constraintHandler
     *            the constraints used in this heuristic
     * @param costDelegate
     *            the cost delegate used to evaluate the tours
     * @param kMax
     *            the K<sub>max</sub> value
     * @param giantTours
     *            {@code true} if the heuristic will only be used to generate giant tours, {@code false} for feasible
     *            tours only
     * @param revertSortOrder
     *            <code>true</code> if the order of decisions has to be reversed (for eg for furthest insertion)
     */
    public TRSPRndConstructiveHeuristic(TRSPInstance instance, TRSPGlobalParameters parameters,
            TourConstraintHandler constraintHandler, TRSPCostDelegate costDelegate, int kMax) {
        super(instance, parameters, constraintHandler, costDelegate);
        mKmax = kMax - 1;
        mGiantTours = getParameters().get(TRSPGlobalParameters.RCH_GIANT_SPLIT);
        mCheckTWFeas = getParameters().get(TRSPGlobalParameters.RCH_GIANT_TW_CHECK);

        mObsNodes = new ObservableNode[getInstance().getMaxId()];
        mObsNodes[getInstance().getMainDepot().getID()] = new ObservableNode(getInstance().getMainDepot().getID());
        for (Technician t : getInstance().getFleet())
            mObsNodes[t.getHome().getID()] = new ObservableNode(t.getHome().getID());

        mCompRequests = new ObservableNode[getInstance().getFleet().size()][];

        mTourPool = new LinkedList<ITRSPTour>();

        // The randomized factor, read at construction time for performance
        mRndFact = parameters.get(TRSPGlobalParameters.RCH_RND_FACTOR);

        mSplit = new TRSPSplit(new SplitTourArcBuilder(constraintHandler, costDelegate, parameters));
        Collection<Integer> requests = getInstance().getReleasedRequests();
        List<ObservableNode> requestsId = new ArrayList<ObservableNode>(requests.size());
        for (int r : requests) {
            mObsNodes[r] = new ObservableNode(r);
            requestsId.add(mObsNodes[r]);
        }
        initialize(requestsId);
    }

    /**
     * Initialization of the precalculated data structures
     * 
     * @param requestsId
     *            a list of the requests id defined in the {@linkplain #getInstance() instance}
     */
    protected void initialize(List<ObservableNode> requestsId) {
        initRequestLists(requestsId);
    }

    /**
     * Initialize the lists of compatible requests associated with each technician
     * 
     * @param requestsId
     */
    private void initRequestLists(List<ObservableNode> requestsId) {
        for (Technician t : getInstance().getFleet()) {
            LinkedList<ObservableNode> compReq = new LinkedList<ObservableNode>();
            for (ObservableNode r : requestsId) {
                if (getInstance().isCompatible(t.getID(), r.getId()))
                    compReq.add(r);
            }
            mCompRequests[t.getID()] = compReq.toArray(new ObservableNode[compReq.size()]);
        }
    }

    @Override
    public ProcedureStatus call() {
        setStatus(ProcedureStatus.RUNNING);

        reset();

        if (isGiantTours()) {
            // Generate giant tours
            Collection<ITRSPTour> giantTours = generateGiantTours();

            // TRSPLogging.getOptimizationLogger().lowDebug("%s.call: generated %s giant tours",
            // getClass().getSimpleName(), giantTours.size());

            // Split giant tours
            for (ITRSPTour giantTour : giantTours) {
                getTourPool().addAll(mSplit.splitTour(giantTour));
            }
        } else {
            // Generate one feasible tour per technician and add them to the pool
            getTourPool().addAll(generateFeasibleTours());
        }
        setStatus(ProcedureStatus.TERMINATED);

        return getStatus();
    }

    /**
     * Returns the {@link ObservableNode} instance corresponding to the home of a technician
     * 
     * @param tech
     *            the id of the considered technician
     * @return the {@link ObservableNode} instance corresponding to the home of a technician
     */
    protected ObservableNode getHome(int tech) {
        return mObsNodes[tech + 1];
    }

    /**
     * Returns the {@link ObservableNode} instance corresponding to the given request
     * 
     * @param reqId
     *            the id of the considered request
     * @return the {@link ObservableNode} instance corresponding to the given request
     */
    protected ObservableNode getObsNode(int reqId) {
        return mObsNodes[reqId];
    }

    /**
     * Returns an array containing the ids of the requests compatible with the specified technician
     * 
     * @param tech
     * @return an array containing the ids of the requests compatible with the specified technician
     */
    protected ObservableNode[] getCompatibleRequests(int tech) {
        return mCompRequests[tech];
    }

    /**
     * Generate a set of giant tours.
     * <p>
     * This method will be called by {@link #call()} and is to be used by subclasses only.
     * </p>
     * <p>
     * Tours will automatically added to the {@link #getTourPool()tour pool}
     * </p>
     * 
     * @return a set of tours
     */
    protected Collection<ITRSPTour> generateGiantTours() {
        if (getParameters().isCVRPTW()) {
            // Generate a giant tour for the base vehicle
            return generateGiantTour(getInstance().getFleet().getVehicle());
        } else {
            ArrayList<ITRSPTour> pool = new ArrayList<ITRSPTour>(getInstance().getFleet().size());
            // For each technician
            for (Technician tech : getInstance().getFleet()) {
                // Generate a giant tour
                pool.addAll(generateGiantTour(tech));
            }
            return pool;
        }
    }

    /**
     * Generate a giant tour for a specific technician.
     * <p>
     * By convention generated tours should start at the technician home, and end at the last served request
     * </p>
     * 
     * @param technician
     *            the technician
     * @return a giant tour for the given technician
     */
    protected abstract Collection<ITRSPTour> generateGiantTour(Technician tech);

    /**
     * Generate a set of feasible tours (1 per technician).
     * <p>
     * This method will be called by {@link #call()} and is to be used by subclasses only.
     * </p>
     * <p>
     * Tours will automatically added to the {@link #getTourPool()tour pool}
     * </p>
     * 
     * @return a set of feasible tours
     */
    protected Collection<ITRSPTour> generateFeasibleTours() {
        ArrayList<ITRSPTour> pool = new ArrayList<ITRSPTour>(getInstance().getFleet().size());
        // For each technician
        for (Technician tech : getInstance().getFleet()) {
            // Generate a giant tour
            pool.add(generateFeasibleTour(tech));
        }
        return pool;
    }

    /**
     * Generate a feasible tour for a specific technician
     * <p>
     * By convention generated tours should start and end at the technician home
     * </p>
     * 
     * @param technician
     *            the technician
     * @return a feasible tour for the given technician
     */
    protected abstract ITRSPTour generateFeasibleTour(Technician tech);

    /**
     * Reset the state of all nodes
     */
    protected void resetNodes() {
        for (ObservableNode n : mObsNodes)
            if (n != null)
                n.reset();
    }

    /**
     * Reset the state of all nodes that compatible with the technician <code>tech</code>
     * 
     * @param tech
     *            the id of the considered technician
     */
    protected void resetNodes(int tech) {
        for (ObservableNode n : getCompatibleRequests(tech))
            n.reset();
    }

    /**
     * Generate a pseudo-random index between <code>0</code> and
     * <code>min(max,{@link #getKmax() K<sub>max</sub>})</code>
     * 
     * @param max
     *            the maximum value to be generated
     * @return a pseudo-random index between <code>0</code> and <code>min(max,{@link #getKmax() K<sub>max</sub>})</code>
     */
    protected int nextIdx(int max) {
        if (max == 0)
            return 0;
        return getParameters().getRCHRndStream().nextInt(0, Math.min(max - 1, mKmax));
        // double rnd = getRandomStream().nextDouble();
        // return (int) (Math.pow(rnd, mRndFact) * max);
    }

    /**
     * Evaluates the cost of inserting {@code  node} between {@code  pred} and {@code  succ} with {@code  technician}
     * 
     * @param node
     *            the node to be inserted
     * @param pred
     *            the predecessor
     * @param succ
     *            the successor
     * @param technician
     * @param twPenalty
     *            {@code true} if a penalty should be added if the insertion violates tw
     * @return the cost of inserting {@code  node} between {@code  pred} and {@code  succ} with {@code  technician}
     */
    protected double evaluateInsertionCost(Integer node, Integer pred, Integer succ, Technician technician,
            boolean twPenalty) {
        double insCost = getInstance().getCostDelegate().getInsertionCost(node, pred, succ, technician);
        if (twPenalty && isCheckTWFeas()) {
            // Check the predecessor arc
            if (!getInstance().isArcTWFeasible(pred, node))
                // We will have to split the tour at this point
                insCost += getInstance().getCostDelegate().getCost(pred, technician.getHome().getID(), technician)
                        + getInstance().getCostDelegate().getCost(technician.getHome().getID(), node, technician);
            // Check the successor arc
            if (!getInstance().isArcTWFeasible(node, succ))
                // We will have to split the tour at this point
                insCost += getInstance().getCostDelegate().getCost(node, technician.getHome().getID(), technician)
                        + getInstance().getCostDelegate().getCost(technician.getHome().getID(), succ, technician);
        }
        return insCost;
    }

    private void reset() {
        mTourPool.clear();
    }

    @Override
    public void dispose() {
        mTourPool.clear();
    }

    /**
     * The class <code>ObservableNode</code> wraps a node to allow more efficient updates of the list of unserved
     * neighbors of a node
     * <p>
     * Creation date: Oct 5, 2011 - 4:24:09 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected static class ObservableNode implements IObservable, IDisposable {

        // private final ObserverManager mObservers;

        private final Integer mId;

        private boolean       mServed;

        /**
         * Returns the id of the wrapped node
         * 
         * @return the id of the wrapped node
         */
        public Integer getId() {
            return mId;
        }

        /**
         * Returns <code>true</code> if this node is currently served, <code>false</code> otherwise
         * 
         * @return <code>true</code> if this node is currently served, <code>false</code> otherwise
         */
        public boolean isServed() {
            return mServed;
        }

        /**
         * Creates a new <code>ObservableNode</code>
         * 
         * @param nodeId
         */
        protected ObservableNode(Integer nodeId) {
            super();
            mId = nodeId;
            // mObservers = new ObserverManager(this);
            mServed = false;
        }

        /**
         * Mark this node as served
         */
        public void markAsServed() {
            mServed = true;
            // mObservers.notifyObservers(null);
        }

        /**
         * Reset this node, remove all observers and mark as unserved
         */
        public void reset() {
            mServed = false;
            // mObservers.notifyObservers(null);
        }

        @Override
        public void addObserver(IObserver o) {
            // mObservers.addObserver(o);
            throw new UnsupportedOperationException("Removed for performance");
        }

        @Override
        public void removeObserver(IObserver o) {
            // mObservers.removeObserver(o);
        }

        @Override
        public void removeAllObservers() {
            // mObservers.removeAllObservers();
        }

        @Override
        public void dispose() {
            removeAllObservers();
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", getId(), isServed() ? "s" : "u");
        }

    }

    /**
     * <code>Neighbor</code> is a wrapper class that contains a reference to the node and the distance to the reference
     * node
     * <p>
     * Creation date: Oct 4, 2011 - 3:31:11 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected static class Neighbor implements Comparable<Neighbor> {
        private final ObservableNode mNode;
        private ObservableNode       mNeigh;
        private double               mDistance;

        /**
         * Returns the node
         * 
         * @return the node
         */
        public ObservableNode getNode() {
            return mNode;
        }

        /**
         * Returns the neighbor node
         * 
         * @return the neighbor node
         */
        public ObservableNode getNeighbor() {
            return mNeigh;
        }

        /**
         * The distance to the reference node
         * 
         * @return the distance to the reference node
         */
        public double getDistance() {
            return mDistance;
        }

        /**
         * Creates a new <code>Neighbor</code>
         * 
         * @param node
         *            the considered node
         * @param neigh
         *            the neighbor of the node
         * @param distance
         *            the distance between the considered node and the neighbor
         */
        public Neighbor(ObservableNode node, ObservableNode neigh, double distance) {
            mNode = node;
            mNeigh = neigh;
            mDistance = distance;
        }

        /**
         * Update the stored neighbor and associated distance
         * 
         * @param newNeighbor
         * @param newDistance
         */
        public void update(ObservableNode newNeighbor, double newDistance) {
            mNeigh = newNeighbor;
            mDistance = newDistance;
        }

        @Override
        public int compareTo(Neighbor o) {
            return Double.compare(getDistance(), o.getDistance());
        }

        @Override
        public String toString() {
            return String.format("%s-%s (%.3f)", getNode(), getNeighbor(), getDistance());
        }
    }

    /**
     * The class <code>NeighborListElement</code> is a simple implementation of a list of {@link ObservableNode} that
     * automatically removes nodes that were marked as served, it is intended to be used with {@link NeighborList}
     * <p>
     * Creation date: Oct 5, 2011 - 4:35:34 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected static class NeighborListElement implements IObserver, IDisposable {

        private final Neighbor            mNeighbor;

        private final NeighborListElement mOriginalPrev;
        private NeighborListElement       mOriginalNext;

        private NeighborListElement       mPrev;
        private NeighborListElement       mNext;

        /**
         * Creates a new <code>NeighborListElement</code>
         * 
         * @param neighbor
         * @param prev
         * @param next
         * @param autoUpdate
         */
        private NeighborListElement(Neighbor neighbor, NeighborListElement prev, NeighborListElement next,
                boolean autoUpdate) {
            super();
            mNeighbor = neighbor;
            mOriginalPrev = prev;
            mOriginalNext = next;
            mPrev = prev;
            mNext = next;

            if (autoUpdate && mNeighbor != null)
                mNeighbor.getNeighbor().addObserver(this);
        }

        @Override
        public void update(IObservable source, Update update) {
            if (mNeighbor.getNeighbor().isServed()) {
                if (mPrev != null)
                    this.mPrev.mNext = this.mNext;
                if (mNext != null)
                    this.mNext.mPrev = this.mPrev;
            } else {
                this.mPrev = this.mOriginalPrev;
                this.mNext = this.mOriginalNext;
                if (this.mPrev != null) {
                    this.mPrev.mNext = this;
                }
                if (this.mNext != null) {
                    this.mNext.mPrev = this;
                }
            }
        }

        @Override
        public void dispose() {
            mNeighbor.getNeighbor().removeObserver(this);
        }

        @Override
        public String toString() {
            return mNeighbor != null ? mNeighbor.toString() : "null";
        }
    }

    /**
     * The class <code>NeighborList</code> is a simple list implementation to store ordered lists of neighbors that
     * automatically update when nodes are marked as served
     * <p>
     * Creation date: Oct 5, 2011 - 5:15:05 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected static class NeighborList implements Iterable<Neighbor> {

        private final NeighborListElement mHead;
        private final TRSPInstance        mInstance;

        protected NeighborList(TRSPInstance instance, List<Neighbor> nodes, boolean autoUpdate) {
            mInstance = instance;
            mHead = new NeighborListElement(null, null, null, autoUpdate);
            NeighborListElement prev = mHead;
            for (Neighbor node : nodes) {
                NeighborListElement e = new NeighborListElement(node, prev, null, autoUpdate);
                prev.mNext = e;
                prev.mOriginalNext = e;
                prev = e;
            }
        }

        /**
         * Returns the first neighbor of this list, or <code>null</code> if the list is empty
         * 
         * @return the first neighbor of this list, or <code>null</code> if the list is empty
         */
        protected Neighbor getFirst() {
            return mHead.mNext != null ? mHead.mNext.mNeighbor : null;
        }

        /**
         * Returns the neighbor at a specified position, the last neighbor if the index is out of range, or
         * <code>null</code> if the list is empty
         * 
         * @param idx
         *            the index of the neighbor
         * @param tech
         *            the id of the technician
         * @return the neighbor at a specified position, the last neighbor if the index is out of range, or
         *         <code>null</code> if the list is empty
         */
        protected Neighbor get(int idx, int tech) {
            int i = -1;
            NeighborListIterator it = iterator();
            Neighbor n = null;
            Neighbor ncomp = null;
            while (it.hasNext() && i < idx) {
                n = it.next();
                if (mInstance.isCompatible(tech, n.getNeighbor().getId())) {
                    i++;
                    ncomp = n;
                }
            }
            return ncomp;
        }

        /**
         * Returns the unserved neighbor at a specified position, the last neighbor if the index is out of range, or
         * <code>null</code> if the list is empty
         * 
         * @param idx
         *            the index of the neighbor
         * @param tech
         *            the id of the technician
         * @return the neighbor at a specified position, the last neighbor if the index is out of range, or
         *         <code>null</code> if the list is empty
         */
        protected Neighbor getUnserved(int idx, int tech) {
            int i = -1;
            NeighborListIterator it = iterator();
            Neighbor n = null;
            Neighbor kuns = null;
            while (it.hasNext() && i < idx) {
                n = it.next();
                if (!n.getNeighbor().isServed() && mInstance.isCompatible(tech, n.getNeighbor().getId())) {
                    i++;
                    kuns = n;
                }
            }
            return kuns;
        }

        /**
         * Returns the number of neighbors in this list, complexity is in <code>O(size)</code>
         * 
         * @return the number of neighbors in this list, complexity is in <code>O(size)</code>
         */
        public int size() {
            int size = 0;
            NeighborListIterator it = iterator();
            while (it.hasNext()) {
                it.next();
                size++;
            }
            return size;
        }

        /**
         * Returns the number of neighbors compatible with <code>tech</code> in this list, complexity is in
         * <code>O(size)</code>
         * 
         * @param tech
         *            the id of the considered technician
         * @return the number of neighbors compatible with <code>tech</code> in this list, complexity is in
         *         <code>O(size)</code>
         */
        public int sizeComp(int tech) {
            int size = 0;
            NeighborListIterator it = iterator();
            while (it.hasNext()) {
                Neighbor n = it.next();
                if (mInstance.isCompatible(tech, n.getNeighbor().getId()))
                    size++;
            }
            return size;
        }

        /**
         * Returns the number of unserved neighbors compatible with <code>tech</code> in this list, complexity is in
         * <code>O(size)</code>
         * 
         * @param tech
         *            the id of the considered technician
         * @return the number of unserved neighbors compatible with <code>tech</code> in this list, complexity is in
         *         <code>O(size)</code>
         */
        public int sizeUnserved(int tech) {
            int size = 0;
            NeighborListIterator it = iterator();
            while (it.hasNext()) {
                Neighbor n = it.next();
                if (!n.getNeighbor().isServed() && mInstance.isCompatible(tech, n.getNeighbor().getId()))
                    size++;
            }
            return size;
        }

        @Override
        public NeighborListIterator iterator() {
            return new NeighborListIterator(mHead);
        }

        @Override
        public String toString() {
            return vroom.common.utilities.Utilities.toShortString(this);
        }

    }

    protected static class NeighborListIterator implements Iterator<Neighbor> {

        private NeighborListElement mCurrent;

        /**
         * Creates a new <code>NeighborListIterator</code>
         * 
         * @param current
         */
        private NeighborListIterator(NeighborListElement current) {
            super();
            mCurrent = current;
        }

        @Override
        public boolean hasNext() {
            return mCurrent.mNext != null;
        }

        @Override
        public Neighbor next() {
            if (!hasNext())
                throw new NoSuchElementException();
            mCurrent = mCurrent.mNext;
            return peek();
        }

        /**
         * Returns the {@link Neighbor} that was returned by the last call to {@link #next()}
         * 
         * @return the {@link Neighbor} that was returned by the last call to {@link #next()}
         */
        public Neighbor peek() {
            return mCurrent.mNeighbor;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
