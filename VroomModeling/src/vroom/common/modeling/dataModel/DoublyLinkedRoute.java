package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.Utilities;

/**
 * The Class <code>DoublyLinkedRoute</code> is an extension of {@link RouteBase} that stores a route in the form of a
 * permutation.
 * <ul>
 * <li>{@link #getNodePosition(INodeVisit)} : O(n)</li>
 * </ul>
 * <p>
 * <p>
 * <b>WARNING: This implementation needs debugging and refactoring, in particular it does not support repeated depot id,
 * and the array of predecessors and successors should be stored at the {@link Solution} level</b>
 * </p>
 * Creation date: Feb 15, 2011 - 6:43:25 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DoublyLinkedRoute extends RouteBase {

    // FIXME debug and move the pred/succ arrays to the parent solution

    /** The Constant UNDEFINED. */
    private final static int   UNDEFINED = -2;

    /** The Predecessor of each node */
    private final int[]        mPred;

    /** The Successor of each node */
    private final int[]        mSucc;

    /** The Nodes. */
    private final INodeVisit[] mNodes;

    /** The Cumulated cost at each node */
    private final double[]     mCumulatedCost;

    /** The First Node ID. */
    private int                mFirst;

    /** The Last Node ID. */
    private int                mLast;

    /** The Length. */
    private int                mLength   = 0;

    /** The max id of the parent instance nodevisits */
    private final int          mOriginalMaxId;

    /**
     * Instantiates a new doubly linked tsp route.
     * 
     * @param parentSolution
     *            the parent solution
     * @param vehicle
     *            the vehicle
     */
    public DoublyLinkedRoute(IVRPSolution<?> parentSolution, Vehicle vehicle) {
        super(parentSolution, vehicle);

        int maxId = Utilities.getMaxId(parentSolution.getParentInstance().getNodeVisits()) + 1;

        mOriginalMaxId = maxId;

        // Allow for a copy of each depot
        maxId += Utilities.getMaxId(parentSolution.getParentInstance().getDepotsVisits()) + 1;

        mPred = new int[maxId];
        mSucc = new int[maxId];
        mCumulatedCost = new double[maxId];
        mNodes = new INodeVisit[maxId];

        Arrays.fill(mPred, UNDEFINED);
        Arrays.fill(mSucc, UNDEFINED);
        Arrays.fill(mCumulatedCost, 0);

        // Store the nodes for faster access
        for (INodeVisit d : getParentSolution().getParentInstance().getDepotsVisits()) {
            cacheNodeVisit(d, d.getID());
            cacheNodeVisit(d, getDepotDuplicateID(d));
        }
        for (INodeVisit n : getParentSolution().getParentInstance().getNodeVisits()) {
            cacheNodeVisit(n, n.getID());
        }

        mFirst = UNDEFINED;
        mLast = UNDEFINED;
        mLength = 0;
    }

    private void cacheNodeVisit(INodeVisit n, int id) {
        if (mNodes[id] != null)
            throw new IllegalStateException(String.format("Two nodes have the same id: %s and %s",
                    mNodes[n.getID()], n));
        mNodes[id] = n;
    }

    /**
     * Returns the id corresponding to the duplicate of depot
     * 
     * @param depot
     * @return the id corresponding to the duplicate of depot
     * @author vpillac
     */
    private int getDepotDuplicateID(INodeVisit depot) {
        return depot.getID() + mOriginalMaxId;
    }

    /**
     * Returns the id corresponding to the original depot
     * 
     * @param depotDuplicateId
     *            the id of the depot duplicate
     * @return the id corresponding to the original depot
     * @author vpillac
     */
    private int getDepotOriginalID(int depotDuplicateId) {
        return depotDuplicateId - mOriginalMaxId;
    }

    /**
     * Creates a new <code>DoublyLinkedRoute</code> by cloning the given instance
     * 
     * @param doublyLinkedRoute
     */
    private DoublyLinkedRoute(DoublyLinkedRoute original) {
        super(original.getParentSolution(), original.getVehicle());
        this.mPred = Arrays.copyOf(original.mPred, original.mPred.length);
        this.mSucc = Arrays.copyOf(original.mSucc, original.mSucc.length);
        this.mCumulatedCost = Arrays
                .copyOf(original.mCumulatedCost, original.mCumulatedCost.length);
        this.mNodes = Arrays.copyOf(original.mNodes, original.mNodes.length);

        this.mFirst = original.mFirst;
        this.mLast = original.mLast;
        this.mLength = original.mLength;

        mOriginalMaxId = original.mOriginalMaxId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#getNodePosition(vroom.common.
     * modelling.dataModel.INodeVisit)
     */
    @Override
    public int getNodePosition(INodeVisit node) {
        // Complexity : O(n)
        int cursor = 0;
        int pred = node.getID();

        while (this.mPred[pred] != UNDEFINED) {
            pred = this.mPred[pred];
            cursor++;
        }
        return cursor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#getFirstNode()
     */
    @Override
    public INodeVisit getFirstNode() {
        return getNode(mFirst);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#getLastNode()
     */
    @Override
    public INodeVisit getLastNode() {
        return getNode(mLast);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IRoute#iterator()
     */
    @Override
    public ListIterator<INodeVisit> iterator() {
        return new DoublyLinkedRouteIterator();
    }

    /**
     * Gets the node id at.
     * 
     * @param index
     *            the index
     * @return the node id at
     */
    private int getNodeIdAt(int index) {
        if (index < length() / 2) {
            int cursor = 0;
            int node = mFirst;
            while (cursor < index) {
                cursor++;
                node = this.mSucc[node];
            }
            return node;
        } else {
            int cursor = length() - 1;
            int node = mLast;
            while (cursor > index) {
                cursor--;
                node = this.mPred[node];
            }
            return node;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#getNodeAtImplem(int)
     */
    @Override
    protected INodeVisit getNodeAtImplem(int index) {
        return getNode(getNodeIdAt(index));
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#subrouteImplem(int, int)
     */
    @Override
    protected List<INodeVisit> subrouteImplem(int start, int end) {
        List<INodeVisit> subroute = new ArrayList<INodeVisit>(end - start + 1);

        int node = getNodeIdAt(start);
        int cursor = start;

        while (cursor < end) {
            subroute.add(getNode(node));
            node = this.mSucc[node];
            cursor++;
        }

        return subroute;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.RouteBase#appendNodeImplem(vroom.common
     * .modelling.dataModel.INodeVisit)
     */
    @Override
    protected boolean appendNodeImplem(INodeVisit node) {
        int nodeId = checkAddNode(node);

        // Ignore if route is empty
        if (mLast != UNDEFINED) {
            this.mSucc[mLast] = nodeId;
            this.mPred[nodeId] = mLast;
        } else {
            // Initialize the route
            this.mFirst = nodeId;
        }

        this.mLast = nodeId;

        mLength++;

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.RouteBase#appendNodesImplem(java.util
     * .List)
     */
    @Override
    protected boolean appendNodesImplem(List<? extends INodeVisit> nodes) {
        for (INodeVisit n : nodes) {
            appendNodeImplem(n);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.RouteBase#appendRouteImplem(vroom.common
     * .modelling.dataModel.IRoute)
     */
    @Override
    protected boolean appendRouteImplem(IRoute<? extends INodeVisit> appendedRoute) {
        return appendNodesImplem(appendedRoute.getNodeSequence());
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#setNodeAtImplem(int,
     * vroom.common.modeling.dataModel.INodeVisit)
     */
    @Override
    protected INodeVisit[] setNodeAtImplem(int index, INodeVisit node) {
        int current = getNodeIdAt(index);

        if (current != node.getID()) {
            int nodeId = checkAddNode(node);

            if (this.mPred[current] != UNDEFINED)
                this.mSucc[this.mPred[current]] = nodeId;
            if (this.mSucc[current] != UNDEFINED)
                this.mPred[this.mSucc[current]] = nodeId;
            this.mPred[nodeId] = this.mPred[current];
            this.mSucc[nodeId] = this.mSucc[current];
            this.mPred[current] = UNDEFINED;
            this.mSucc[current] = UNDEFINED;
        }
        return new INodeVisit[] { getNode(this.mPred[node.getID()]), getNode(current),
                getNode(this.mSucc[node.getID()]) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#extractNodeImplem(int)
     */
    @Override
    protected INodeVisit[] extractNodeImplem(int index) {
        int node = getNodeIdAt(index);

        if (this.mPred[node] != UNDEFINED)
            this.mSucc[this.mPred[node]] = this.mSucc[node];
        else
            mFirst = this.mSucc[node];
        if (this.mSucc[node] != UNDEFINED)
            this.mPred[this.mSucc[node]] = this.mPred[node];
        else
            mLast = this.mPred[node];

        this.mPred[node] = UNDEFINED;
        this.mSucc[node] = UNDEFINED;

        return new INodeVisit[] { getNode(this.mPred[node]), getNode(node),
                getNode(this.mSucc[node]) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#extractSubrouteImplem(int,
     * int)
     */
    @Override
    protected Object[] extractSubrouteImplem(int start, int end) {
        List<INodeVisit> subroute = new ArrayList<INodeVisit>(end - start + 1);

        int node = getNodeIdAt(start);
        int pred = getPred(node);
        int succ = getSucc(node);

        int index = start;

        while (index <= end) {
            subroute.add(getNode(node));
            succ = getSucc(node);
            this.mPred[node] = UNDEFINED;
            this.mSucc[node] = UNDEFINED;
            node = succ;
            index++;
        }
        this.mSucc[pred] = succ;
        this.mPred[succ] = pred;

        return new Object[] { getNode(pred), subroute, getNode(succ) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#extractNodesImplem(int,
     * int)
     */
    @Override
    protected Object[] extractNodesImplem(int start, int end) {
        List<INodeVisit> nodes = new ArrayList<INodeVisit>(end - start + 1);

        int current = getNodeIdAt(start);
        int predecessor = this.mPred[current];
        int next = this.mSucc[current];

        int cursor = start;

        while (cursor <= end && current != UNDEFINED) {
            next = this.mSucc[current];
            this.mPred[current] = UNDEFINED;
            this.mSucc[current] = UNDEFINED;
            current = next;
            cursor++;
        }

        this.mSucc[predecessor] = next;
        this.mPred[next] = predecessor;

        return new Object[] { getNode(predecessor), nodes, getNode(next) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#removeImplem(vroom.common.
     * modelling.dataModel.INodeVisit)
     */
    @Override
    protected INodeVisit[] removeImplem(INodeVisit node) {
        int n = node.getID();
        int pred = this.mPred[n];
        int succ = this.mSucc[n];

        if (pred != UNDEFINED)
            this.mSucc[pred] = succ;
        if (succ != UNDEFINED)
            this.mPred[succ] = pred;

        this.mPred[n] = UNDEFINED;
        this.mSucc[n] = UNDEFINED;

        return new INodeVisit[] { getNode(pred), getNode(succ) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#insertNodeImplem(int,
     * vroom.common.modeling.dataModel.INodeVisit)
     */
    @Override
    protected INodeVisit[] insertNodeImplem(int index, INodeVisit node) {
        int nodeId = checkAddNode(node);
        int current = getNodeIdAt(index);

        this.mSucc[this.mPred[current]] = nodeId;
        this.mPred[nodeId] = this.mPred[current];

        this.mPred[current] = nodeId;
        this.mSucc[nodeId] = current;

        return new INodeVisit[] { getPredecessor(node), getSuccessor(node) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#insertNodesImplem(int,
     * java.util.List)
     */
    @Override
    protected INodeVisit[] insertNodesImplem(int index, List<? extends INodeVisit> subroute) {
        if (subroute.isEmpty())
            return new INodeVisit[2];

        int current = getNodeIdAt(index);

        INodeVisit[] r = new INodeVisit[] { getPredecessor(current), getNode(current) };

        Iterator<? extends INodeVisit> it = subroute.iterator();
        int first = UNDEFINED, last = UNDEFINED;
        int pred = checkAddNode(it.next());
        int succ = pred;
        first = pred;
        while (it.hasNext()) {
            succ = checkAddNode(it.next());

            this.mPred[succ] = pred;
            this.mSucc[pred] = succ;

            pred = succ;
        }
        last = succ;

        this.mSucc[this.mPred[current]] = first;
        this.mPred[first] = this.mPred[current];

        this.mPred[current] = last;
        this.mSucc[last] = current;

        return r;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#reverseSubrouteImplem(int,
     * int)
     */
    @Override
    protected INodeVisit[] reverseSubrouteImplem(int start, int end) {
        int first = getNodeIdAt(start), last = first;
        int pred = this.mPred[first];

        int node = first, succ = first;
        int index = start;
        // Reverse the subroute
        while (index <= end) {
            succ = this.mSucc[node];
            this.mSucc[node] = this.mPred[node];
            this.mPred[node] = succ;
            last = node;
            node = succ;
            index++;
        }

        // Relink the subroute extremities
        this.mSucc[first] = succ;
        this.mPred[succ] = first;
        this.mSucc[last] = pred;
        this.mPred[pred] = last;

        return new INodeVisit[] { getNode(pred), getNode(first), getNode(last), getNode(succ) };
    }

    /**
     * Check if a node is already present in this route and return the internal id. This method allows for depots to be
     * added twice.
     * 
     * @param node
     *            the node that is to be added to this route
     * @return the internal id for {@code  node}
     * @throws IllegalArgumentException
     *             if {@code  node} is already present in this route and cannot be added
     * @author vpillac
     */
    private int checkAddNode(INodeVisit node) {
        int id = node.getID();

        if (contains(id)) {
            if (node.isDepot()) {
                id = getDepotDuplicateID(node);
                if (contains(id)) {
                    throw new IllegalArgumentException(String.format(
                            "Depot %s is already present twice in this route", node));
                }
            } else {
                throw new IllegalArgumentException(String.format(
                        "Node %s is already present in this route", node));
            }
        }

        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#length()
     */
    @Override
    public int length() {
        return mLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#getNodeSequence()
     */
    @Override
    public List<INodeVisit> getNodeSequence() {
        List<INodeVisit> seq = new ArrayList<INodeVisit>(length());
        int node = mFirst;
        while (node != UNDEFINED) {
            seq.add(getNode(node));
            node = this.mSucc[node];
        }

        return seq;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.RouteBase#contains(vroom.common.modeling
     * .dataModel.INodeVisit)
     */
    @Override
    public boolean contains(INodeVisit node) {
        return contains(node.getID());
    }

    /**
     * Returns {@code true} if this routes contains the node of id {@code  nodeId}
     * 
     * @param nodeId
     * @return {@code true} if this routes contains the node of id {@code  nodeId}
     * @author vpillac
     */
    public boolean contains(int nodeId) {
        return mPred[nodeId] != UNDEFINED || mSucc[nodeId] != UNDEFINED || mFirst == nodeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.RouteBase#clone()
     */
    @Override
    public DoublyLinkedRoute clone() {
        return new DoublyLinkedRoute(this);
    }

    /**
     * Gets the {@link INodeVisit} associated with a given id.
     * 
     * @param id
     *            the id
     * @return the node
     */
    public INodeVisit getNode(int id) {
        return id >= 0 && id < mNodes.length ? mNodes[id] : null;
    }

    /**
     * Gets the predecessor id.
     * 
     * @param id
     *            the id
     * @return the pred
     */
    public int getPred(int id) {
        return this.mPred[id];
    }

    /**
     * Gets the predecessor.
     * 
     * @param id
     *            the id of the considered node
     * @return the predecessor
     */
    public INodeVisit getPredecessor(int id) {
        return getNode(getPred(id));
    }

    /**
     * Gets the predecessor.
     * 
     * @param node
     *            the node
     * @return the predecessor
     */
    public INodeVisit getPredecessor(INodeVisit node) {
        return getNode(getPred(node.getID()));
    }

    /**
     * Gets the successor id
     * 
     * @param id
     *            the id
     * @return the succ
     */
    public int getSucc(int id) {
        return this.mSucc[id];
    }

    /**
     * Gets the predecessor.
     * 
     * @param id
     *            the id of the considered node
     * @return the predecessor
     */
    public INodeVisit getSuccessor(int id) {
        return getNode(getSucc(id));
    }

    /**
     * Gets the predecessor.
     * 
     * @param node
     *            the node
     * @return the predecessor
     */
    public INodeVisit getSuccessor(INodeVisit node) {
        return getNode(getSucc(node.getID()));
    }

    /**
     * The Class <code>DoublyLinkedRouteIterator</code> is an implementation of {@link ListIterator} to iterate over a
     * {@link DoublyLinkedRoute}
     * <p>
     * Creation date: Feb 16, 2011 - 5:15:56 PM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    protected class DoublyLinkedRouteIterator implements ListIterator<INodeVisit> {

        private int current;
        private int cursor;

        protected DoublyLinkedRouteIterator() {
            current = mFirst;
            cursor = 0;
        }

        @Override
        public boolean hasNext() {
            return cursor < DoublyLinkedRoute.this.length();
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public INodeVisit next() {
            if (hasNext()) {
                cursor++;
                int c = current;
                current = DoublyLinkedRoute.this.mSucc[current];
                return getNode(c);
            } else
                throw new IllegalStateException("Iterator has no next element");
        }

        @Override
        public INodeVisit previous() {
            if (hasPrevious()) {
                cursor--;
                int c = current;
                current = DoublyLinkedRoute.this.mPred[current];
                return getNode(c);
            } else
                throw new IllegalStateException("Iterator has no previous element");
        }

        @Override
        public int nextIndex() {
            return cursor + 1;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                    "remove operation is not supported by this iterator");
        }

        @Override
        public void set(INodeVisit e) {
            throw new UnsupportedOperationException(
                    "set operation is not supported by this iterator");
        }

        @Override
        public void add(INodeVisit e) {
            throw new UnsupportedOperationException(
                    "add operation is not supported by this iterator");
        }

    }
}
