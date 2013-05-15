package vrp2013.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * JAVADOC <code>DoublyLinkedIdRoute</code>
 * <p>
 * Creation date: 09/04/2013 - 8:14:03 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class DoublyLinkedRoute extends Route {
    private final static int UNDEFINED = -2;

    private final int[]      mPred;
    private final int[]      mSucc;

    private final double[]   mCumulatedCost;

    private int              mFirst;
    private int              mLast;
    private int              mLength   = 0;

    /**
     * Instantiates a new doubly linked tsp route.
     * 
     * @param parentSolution
     *            the parent solution
     * @param vehicle
     *            the vehicle
     * @param depot
     *            the depot
     */
    public DoublyLinkedRoute(int vehicle, int maxId) {
        super(vehicle);

        mPred = new int[maxId];
        mSucc = new int[maxId];
        mCumulatedCost = new double[maxId];

        Arrays.fill(mPred, UNDEFINED);
        Arrays.fill(mSucc, UNDEFINED);
        Arrays.fill(mCumulatedCost, 0);

        mFirst = UNDEFINED;
        mLast = UNDEFINED;
        mLength = 0;
    }

    @Override
    public int getNodePosition(int node) {
        // Complexity : O(n)
        int cursor = 0;
        int pred = node;

        while (this.mPred[pred] != UNDEFINED) {
            pred = this.mPred[pred];
            cursor++;
        }
        return cursor;
    }

    @Override
    public int getFirstNode() {
        return mFirst;
    }

    @Override
    public int getLastNode() {
        return mLast;
    }

    @Override
    public int length() {
        return mLength;
    }

    @Override
    public ListIterator<Integer> iterator() {
        return new DoublyLinkedIdRouteIterator();
    }

    /**
     * Gets the Id of the node at the given position
     * 
     * @param index
     * @return the id of the node at index
     * @see #getNodeAt(int)
     */
    @Override
    public int getNodeAt(int index) {
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

    @Override
    public List<Integer> getSubroute(int start, int end) {
        List<Integer> subroute = new ArrayList<Integer>(end - start + 1);

        int node = getNodeAt(start);
        int cursor = start;

        while (cursor < end) {
            subroute.add(node);
            node = this.mSucc[node];
            cursor++;
        }

        return subroute;
    }

    @Override
    public void append(int node) {
        this.mSucc[mLast] = node;
        this.mPred[node] = mLast;
        this.mLast = node;

        mLength++;
    }

    @Override
    public void append(List<Integer> nodes) {
        for (int n : nodes) {
            append(n);
        }
    }

    @Override
    public int setNodeAt(int index, int node) {
        int current = getNodeAt(index);

        if (this.mPred[current] != UNDEFINED)
            this.mSucc[this.mPred[current]] = node;
        if (this.mSucc[current] != UNDEFINED)
            this.mPred[this.mSucc[current]] = node;
        this.mPred[node] = this.mPred[current];
        this.mSucc[node] = this.mSucc[current];
        this.mPred[current] = UNDEFINED;
        this.mSucc[current] = UNDEFINED;

        return current;
    }

    @Override
    public int extractNodeAt(int index) {
        int node = getNodeAt(index);

        this.mSucc[this.mPred[node]] = this.mSucc[node];
        this.mPred[this.mSucc[node]] = this.mPred[node];

        this.mPred[node] = UNDEFINED;
        this.mSucc[node] = UNDEFINED;

        return node;
    }

    @Override
    public List<Integer> extractSubroute(int start, int end) {
        List<Integer> nodes = new ArrayList<Integer>(end - start + 1);

        int current = getNodeAt(start);
        int pred = this.mPred[current];
        int succ = this.mSucc[current];

        int cursor = start;

        while (cursor <= end && current != UNDEFINED) {
            succ = this.mSucc[current];
            this.mPred[current] = UNDEFINED;
            this.mSucc[current] = UNDEFINED;
            current = succ;
            cursor++;
        }

        this.mSucc[pred] = succ;
        this.mPred[succ] = pred;

        return nodes;
    }

    @Override
    public void remove(int node) {
        if (this.mPred[node] != UNDEFINED)
            this.mSucc[this.mPred[node]] = this.mSucc[node];
        if (this.mSucc[node] != UNDEFINED)
            this.mPred[this.mSucc[node]] = this.mPred[node];

        this.mPred[node] = UNDEFINED;
        this.mSucc[node] = UNDEFINED;
    }

    @Override
    public void insertNodeAt(int node, int index) {
        int current = getNodeAt(index);

        this.mSucc[this.mPred[current]] = node;
        this.mPred[node] = this.mPred[current];

        this.mPred[current] = node;
        this.mSucc[node] = current;

    }

    @Override
    public void insertNodesAt(List<Integer> subroute, int index) {
        int current = getNodeAt(index);

        Iterator<Integer> it = subroute.iterator();
        int first = UNDEFINED, last = UNDEFINED;
        int pred = it.next();
        int succ = pred;
        first = pred;
        while (it.hasNext()) {
            succ = it.next();

            this.mPred[succ] = pred;
            this.mSucc[pred] = succ;

            pred = succ;
        }
        last = succ;

        this.mSucc[this.mPred[current]] = first;
        this.mPred[first] = this.mPred[current];

        this.mPred[current] = last;
        this.mSucc[last] = current;
    }

    @Override
    public List<Integer> getNodeSequence() {
        List<Integer> seq = new ArrayList<Integer>(length());
        int node = mFirst;
        while (node != UNDEFINED) {
            seq.add(node);
            node = this.mSucc[node];
        }

        return seq;
    }

    @Override
    public boolean contains(int node) {
        return mPred[node] != UNDEFINED || mSucc[node] != UNDEFINED;
    }

    @Override
    public Route clone() {
        DoublyLinkedRoute clone = new DoublyLinkedRoute(getVehicle(), mPred.length);

        clone.mFirst = this.mFirst;
        clone.mLast = this.mLast;
        clone.mLength = this.mLength;

        for (int i = 0; i < this.mPred.length; i++) {
            clone.mPred[i] = this.mPred[i];
            clone.mSucc[i] = this.mSucc[i];
        }

        return clone;
    }

    protected class DoublyLinkedIdRouteIterator implements ListIterator<Integer> {

        private int current;
        private int cursor;

        protected DoublyLinkedIdRouteIterator() {
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
        public Integer next() {
            if (hasNext()) {
                cursor++;
                int c = current;
                current = DoublyLinkedRoute.this.mSucc[current];
                return c;
            } else
                throw new IllegalStateException("Iterator has no next element");
        }

        @Override
        public Integer previous() {
            if (hasPrevious()) {
                cursor--;
                int c = current;
                current = DoublyLinkedRoute.this.mPred[current];
                return c;
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
            // DoublyLinkedRoute.this.remove(getLastReturnedNextNode());
            throw new UnsupportedOperationException(
                    "set operation is not supported by this iterator");
        }

        @Override
        public void set(Integer e) {
            // int l = getLastReturnedNextNode();
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException(
                    "set operation is not supported by this iterator");
        }

        @Override
        public void add(Integer e) {
            // DoublyLinkedRoute.this.in
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException(
                    "add operation is not supported by this iterator");
        }

        private int getLastReturnedNextNode() {
            return DoublyLinkedRoute.this.mPred[current];
        }

    }

}
