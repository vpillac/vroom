/**
 * 
 */
package vroom.common.utilities;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <code>DoublyLinkedList</code>
 * <p>
 * Creation date: Oct 4, 2011 - 3:54:37 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DoublyLinkedIntSet extends AbstractSequentialList<Integer> implements java.io.Serializable, Set<Integer> {

    /** The Constant UNDEFINED is used when an element has no or predecessor/successor. */
    public final static Integer UNDEFINED        = -2;

    /** The Constant serialVersionUID. */
    private static final long   serialVersionUID = 1L;

    /** The element id. */
    private Integer             mFirst;

    /** The last element id. */
    private Integer             mLast;

    /** The Length. */
    private Integer             mSize            = 0;

    /** The Predecessor of each node. */
    private final Integer[]     mPred;

    /** The Successor of each node. */
    private final Integer[]     mSucc;

    /**
     * Creates a new <code>DoublyLinkedIntegerList</code>.
     * 
     * @param maxValue
     *            the maximum value that will be stored in this list
     */
    public DoublyLinkedIntSet(Integer maxValue) {
        if (maxValue < 0)
            throw new IllegalArgumentException("Max value cannot be negative");
        mFirst = UNDEFINED;
        mLast = UNDEFINED;
        mSize = 0;
        mPred = new Integer[maxValue + 1];
        mSucc = new Integer[maxValue + 1];

        Arrays.fill(mPred, UNDEFINED);
        Arrays.fill(mSucc, UNDEFINED);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#listIterator()
     */
    @Override
    public DoublyLinkedIterator listIterator() {
        return new DoublyLinkedIterator();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractSequentialList#listIterator(int)
     */
    @Override
    public DoublyLinkedIterator listIterator(int index) {
        if (index > size() || index < 0)
            throw new IllegalArgumentException("The specified index (" + index + ") is out of range");

        DoublyLinkedIterator it = listIterator();
        for (int i = 0; i < index; i++)
            it.next();

        return it;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return mSize;
    }

    /**
     * Gets the first element.
     * 
     * @return the first element
     */
    Integer getFirst() {
        return mFirst;
    }

    /**
     * Gets the last element.
     * 
     * @return the last element
     */
    Integer getLast() {
        return mLast;
    }

    /**
     * Gets the element predecessor.
     * 
     * @param element
     *            the element
     * @return the element predecessor
     */
    Integer getPred(Integer element) {
        return mPred[element];
    }

    /**
     * Gets the element successor.
     * 
     * @param element
     *            the element
     * @return the element successor
     */
    Integer getSucc(Integer element) {
        return mSucc[element];
    }

    /**
     * Sets the element predecessor.
     * 
     * @param element
     *            the element
     * @param pred
     *            the element predecessor
     * @return the integer
     */
    private Integer setPred(Integer element, Integer pred) {
        return mPred[element] = pred;
    }

    /**
     * Sets the element successor.
     * 
     * @param element
     *            the element
     * @param succ
     *            the element successor
     * @return the integer
     */
    private Integer setSucc(Integer element, Integer succ) {
        return mSucc[element] = succ;
    }

    /**
     * Sets the first element.
     * 
     * @param first
     *            the new first element
     */
    private void setFirst(Integer first) {
        mFirst = first;
    }

    /**
     * Sets the last element.
     * 
     * @param last
     *            the new last element
     */
    private void setLast(Integer last) {
        mLast = last;
    }

    /**
     * Insert an element.
     * 
     * @param element
     *            the element to be inserted
     * @param succ
     *            the successor of the inserted element
     */
    void insert(Integer element, Integer succ) {
        if (contains(element))
            throw new IllegalArgumentException("Element " + element + " is already present in this permutation "
                    + Utilities.toShortString(this));

        int pred = getPred(succ);

        if (pred != UNDEFINED)
            setSucc(pred, element);
        else
            setFirst(element);

        setPred(element, pred);

        setPred(succ, element);
        setSucc(element, succ);
        // Update length
        this.mSize++;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#add(java.lang.Object)
     */
    @Override
    public boolean add(Integer element) {
        if (mLast != UNDEFINED) {// Tour is not empty
            setSucc(getLast(), element);
            setPred(element, getLast());
        } else { // Tour is empty
            // Initialize the tour
            setFirst(element);
        }
        setLast(element);
        this.mSize++;

        return true;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o) {
        if (!contains(o))
            return false;

        Integer element = (Integer) o;
        Integer pred = getPred(element);
        Integer succ = getSucc(element);

        if (pred != UNDEFINED)
            setSucc(pred, succ);
        else
            setFirst(succ);
        if (succ != UNDEFINED)
            setPred(succ, pred);
        else
            setLast(pred);

        // Update length
        this.mSize--;

        return true;
    }

    @Override
    public void clear() {
        mFirst = UNDEFINED;
        mLast = UNDEFINED;
        mSize = 0;
        Arrays.fill(mPred, UNDEFINED);
        Arrays.fill(mSucc, UNDEFINED);
    }

    /**
     * Replace an element of this list
     * 
     * @param oldElement
     * @param newElement
     * @return <code>true</code> if oldElement was replaced by newElement
     */
    boolean setElement(Integer oldElement, Integer newElement) {
        if (!contains(oldElement))
            return false;
        Integer pred = getPred(oldElement);
        Integer succ = getSucc(oldElement);

        if (pred != UNDEFINED)
            setSucc(pred, newElement);
        else
            setFirst(newElement);
        if (succ != UNDEFINED)
            setPred(succ, newElement);
        else
            setLast(newElement);

        setPred(newElement, pred);
        setSucc(newElement, succ);

        setPred(oldElement, UNDEFINED);
        setSucc(oldElement, UNDEFINED);
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            Integer element = (Integer) o;
            return getPred(element) != UNDEFINED || getSucc(element) != UNDEFINED || getFirst() == element;
        } else
            return false;
    }

    @Override
    public Integer[] toArray() {
        Integer[] array = new Integer[size()];
        int i = 0;
        for (Integer e : this)
            array[i++] = e;

        return array;
    }

    /**
     * The Class <code>DoublyLinkedListIterator</code> is an implementation of {@link ListIterator} used to iterate over
     * a {@link DoublyLinkedIntSet}
     * <p>
     * Creation date: Oct 4, 2011 - 4:25:51 PM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public class DoublyLinkedIterator implements ListIterator<Integer> {

        /** <code>true</code> if the value of <code>cursor</code> cannot be trusted. */
        private final boolean noCursor;

        /** The current node, which will be returned at the next call to {@link #next()}. */
        private int           current;

        /** The cursor. */
        private int           cursor;

        /**
         * Instantiates a new tRSP tour iterator.
         */
        protected DoublyLinkedIterator() {
            current = DoublyLinkedIntSet.this.getFirst();
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
        protected DoublyLinkedIterator(int nodeId) {
            this(nodeId != UNDEFINED ? nodeId : DoublyLinkedIntSet.this.getFirst(), 0, nodeId != UNDEFINED);
        }

        /**
         * Creates a new <code>TRSPTourIterator</code>.
         * 
         * @param current
         *            the current
         * @param cursor
         *            the cursor
         * @param noCursor
         *            the no cursor
         */
        private DoublyLinkedIterator(int current, int cursor, boolean noCursor) {
            super();
            this.noCursor = noCursor;
            this.current = current;
            this.cursor = cursor;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        @Override
        public void add(Integer e) {
            DoublyLinkedIntSet.this.insert(e, current);
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return current != UNDEFINED;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasPrevious()
         */
        @Override
        public boolean hasPrevious() {
            return current != UNDEFINED;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#next()
         */
        @Override
        public Integer next() {
            if (hasNext()) {
                cursor++;
                int c = current;
                current = DoublyLinkedIntSet.this.getSucc(current);
                return c;
            } else
                throw new NoSuchElementException();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#nextIndex()
         */
        @Override
        public int nextIndex() {
            if (noCursor)
                throw new IllegalStateException(
                        "Cannot trust the index position when the iterator was started from an initial node different from the tour start");
            return cursor;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previous()
         */
        @Override
        public Integer previous() {
            if (hasPrevious()) {
                cursor--;
                int c = current;
                current = DoublyLinkedIntSet.this.getPred(current);
                return c;
            } else
                throw new NoSuchElementException();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previousIndex()
         */
        @Override
        public int previousIndex() {
            if (noCursor)
                throw new IllegalStateException(
                        "Cannot trust the index position when the iterator was started from an initial node different from the tour start");
            return cursor - 1;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#remove()
         */
        @Override
        public void remove() {
            DoublyLinkedIntSet.this.remove(getPred(current));
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        @Override
        public void set(Integer e) {
            DoublyLinkedIntSet.this.set(getPred(current), e);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @Override
        protected DoublyLinkedIterator clone() {
            return new DoublyLinkedIterator(current, cursor, noCursor);
        }

        /**
         * Sub iterator.
         * 
         * @return the doubly linked list iterator
         */
        public DoublyLinkedIterator subIterator() {
            return clone();
        }

    }

}
