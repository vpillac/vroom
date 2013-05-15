/**
 *
 */
package vroom.common.modeling.dataModel;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * The class <code>TRSPTour</code> JAVADOC
 * <p>
 * Creation date: Apr 30, 2013 - 3:22:44 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class GiantPermRoute extends RouteBase {

    public static final int UNDEFINED = -2;

    private int             mFirst    = UNDEFINED;
    private int             mLast     = UNDEFINED;

    private int             mLength   = 0;

    public GiantPermRoute(TRSPSolution parentSolution, Vehicle vehicle) {
        super(parentSolution, vehicle);
    }

    @Override
    public TRSPSolution getParentSolution() {
        return (TRSPSolution) super.getParentSolution();
    }

    public IVRPInstance getInstance() {
        return getParentSolution().getParentInstance();
    }

    @Override
    public int getNodePosition(INodeVisit node) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public INodeVisit getFirstNode() {
        return mFirst != UNDEFINED ? getInstance().getNodeVisit(mFirst) : null;
    }

    @Override
    public INodeVisit getLastNode() {
        return mLast != UNDEFINED ? getInstance().getNodeVisit(mLast) : null;
    }

    @Override
    public ListIterator<INodeVisit> iterator() {
        return new GiantPermRouteIterator();
    }

    @Override
    protected INodeVisit getNodeAtImplem(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected List<INodeVisit> subrouteImplem(int start, int end) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean appendNodeImplem(INodeVisit node) {
        if (mFirst == UNDEFINED)
            mFirst = node.getID();
        if (mLast != UNDEFINED) {
            getParentSolution().getGiantPermutation().setSucc(mLast, node.getID());
            getParentSolution().getGiantPermutation().setPred(node.getID(), mLast);
        }
        mLast = node.getID();
        mLength++;

        return true;
    }

    @Override
    protected boolean appendNodesImplem(List<? extends INodeVisit> nodes) {
        for (INodeVisit n : nodes)
            appendNodeImplem(n);
        return true;
    }

    @Override
    protected boolean appendRouteImplem(IRoute<? extends INodeVisit> appendedRoute) {
        for (INodeVisit n : appendedRoute)
            appendNodeImplem(n);
        return true;
    }

    @Override
    protected INodeVisit[] setNodeAtImplem(int index, INodeVisit node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    protected INodeVisit[] extractNodeImplem(int index) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object[] extractSubrouteImplem(int start, int end) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object[] extractNodesImplem(int start, int end) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    protected INodeVisit[] removeImplem(INodeVisit node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected INodeVisit[] insertNodeImplem(int index, INodeVisit node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected INodeVisit[] insertNodesImplem(int index, List<? extends INodeVisit> subroute) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected INodeVisit[] reverseSubrouteImplem(int start, int end) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int length() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<INodeVisit> getNodeSequence() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean contains(INodeVisit node) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RouteBase clone() {
        // TODO Auto-generated method stub
        return null;
    }

    public GiantPermRoute clone(TRSPSolution trspSolution) {
        // TODO Auto-generated method stub
        return null;
    }

    public void clear() {
        // TODO Auto-generated method stub

    }

    public int getPred(int node) {
        return getParentSolution().getGiantPermutation().getPred(node);
    }

    public int getSucc(int node) {
        return getParentSolution().getGiantPermutation().getSucc(node);
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
    public class GiantPermRouteIterator implements ListIterator<INodeVisit> {

        /** <code>true</code> if the value of <code>cursor</code> cannot be trusted */
        private final boolean noCursor;

        /** The current node, which will be returned at the next call to {@link #next()}. */
        private int           current;

        /** The cursor. */
        private int           cursor;

        /**
         * Instantiates a new tRSP tour iterator.
         */
        protected GiantPermRouteIterator() {
            current = getFirstNode().getID();
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
        public GiantPermRouteIterator(int nodeId) {
            this(nodeId != UNDEFINED ? nodeId : getFirstNode().getID(), 0, nodeId != UNDEFINED);
        }

        /**
         * Creates a new <code>TRSPTourIterator</code>
         * 
         * @param noCursor
         * @param current
         * @param cursor
         */
        private GiantPermRouteIterator(int current, int cursor, boolean noCursor) {
            super();
            this.noCursor = noCursor;
            this.current = current;
            this.cursor = cursor;
        }

        @Override
        public void add(INodeVisit e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return current != UNDEFINED;
        }

        @Override
        public boolean hasPrevious() {
            return current != UNDEFINED;
        }

        @Override
        public INodeVisit next() {
            if (hasNext()) {
                cursor++;
                int c = current;
                current = GiantPermRoute.this.getSucc(current);
                return getParentSolution().getParentInstance().getNodeVisit(c);
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
        public INodeVisit previous() {
            if (hasPrevious()) {
                cursor--;
                int c = current;
                current = GiantPermRoute.this.getPred(current);
                return getParentSolution().getParentInstance().getNodeVisit(c);
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
            throw new UnsupportedOperationException(
                    "remove operation is not supported by this iterator");
        }

        @Override
        public void set(INodeVisit e) {
            throw new UnsupportedOperationException(
                    "set operation is not supported by this iterator");
        }

        @Override
        protected GiantPermRouteIterator clone() {
            return new GiantPermRouteIterator(current, cursor, noCursor);
        }

    }
}
