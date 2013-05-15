/**
 * 
 */
package vroom.common.utilities;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <code>IntegerSet</code> is an implementation of {@link Set} to store integers. It is backed up by an array of
 * booleans and requires the maximum value to be known.
 * <p>
 * {@link #add(Integer)}, {@link #remove(Object)}, and {@link #contains(Object)} are executed in constant time, while
 * iterating over the set is done in O({@code maxValue})
 * </p>
 * <p>
 * Creation date: Oct 14, 2011 - 1:55:53 PM
 * </p>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class IntegerSet extends AbstractSet<Integer> {

    private final Boolean[] mMask;
    private int             mSize;
    private int             mMinIdx;

    /**
     * Creates a new <code>IntegerSet</code> with an initial set of values.
     * <p>
     * Note that the set will not be able to handle values that are higher than the maximum value stored in
     * {@code  values}
     * </p>
     * 
     * @param values
     *            an initial set of values
     */
    public IntegerSet(Collection<Integer> values) {
        int maxValue = -1;
        for (Integer i : values)
            if (i > maxValue)
                maxValue = i;

        mMask = new Boolean[maxValue + 1];
        mMinIdx = mMask.length - 1;
        Arrays.fill(mMask, Boolean.FALSE);
        addAll(values);
    }

    /**
     * Creates a new <code>IntegerSet</code> with an initial set of values
     * 
     * @param values
     *            an initial set of values
     * @param maxValue
     *            the maximum value that will ever be contained in this set
     */
    public IntegerSet(Collection<Integer> values, int maxValue) {
        this(maxValue);
        addAll(values);
    }

    /**
     * Creates a new empty <code>IntegerSet</code>
     * 
     * @param maxValue
     *            the maximum value that will ever be contained in this set
     */
    public IntegerSet(int maxValue) {
        mMask = new Boolean[maxValue + 1];
        mMinIdx = mMask.length - 1;
        Arrays.fill(mMask, Boolean.FALSE);
        mSize = 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            int e = (Integer) o;
            return contains(e);
        } else {
            return false;
        }
    }

    /**
     * Special implementation of {@link #contains(Object)} to prevent additional wrapping of integers
     * 
     * @param e
     *            the element to remove
     * @return <code>true</code> if this set contains the specified element
     * @see #contains(Integer)
     */
    public boolean contains(int e) {
        if (e < 0 || e >= mMask.length)
            return false;
        return mMask[e];
    }

    @Override
    public boolean add(Integer e) {
        return add(e.intValue());
    }

    /**
     * Special implementation of {@link #add(Integer)} to prevent additional wrapping of integers
     * 
     * @param e
     *            the element to add
     * @return <code>true</code> if this set did not already contain the specified element
     * @see #add(Integer)
     */
    public boolean add(int e) {
        boolean prev = mMask[e];
        if (!prev) {
            mMask[e] = Boolean.TRUE;
            if (e < mMinIdx)
                mMinIdx = e;
            mSize++;
        }
        return !prev;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Integer) {
            int e = (Integer) o;
            return remove(e);
        } else {
            return false;
        }
    }

    /**
     * Special implementation of {@link #remove(Object)} to prevent additional wrapping of integers
     * 
     * @param e
     *            the element to remove
     * @return <code>true</code> if this set contained the specified element
     * @see #remove(Integer)
     */
    public boolean remove(int e) {
        if (e < 0 || e >= mMask.length)
            return false;
        boolean prev = mMask[e];
        if (prev) {
            mMask[e] = Boolean.FALSE;
            if (e == mMinIdx)
                mMinIdx++;
            mSize--;
        }
        return prev;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c)
            changed |= remove(o);
        return changed;
    }

    /*
     * (non-Javadoc)
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public IntegerSetIterator iterator() {
        return new IntegerSetIterator();
    }

    /*
     * (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return mSize;
    }

    /**
     * Return the maximum value that can be stored in this set
     * 
     * @return the maximum value that can be stored in this set
     */
    public int maxValue() {
        return mMask.length - 1;
    }

    @Override
    public void clear() {
        Arrays.fill(mMask, Boolean.FALSE);
        mSize = 0;
    }

    /**
     * <code>IntegerSetIterator</code> is an implementation of {@link Iterator} that iterates over an instance of
     * {@link IntegerSet}. This implementation iterates over the whole set in {@link IntegerSet#maxValue() maxId} steps.
     * <p>
     * Creation date: Oct 14, 2011 - 2:15:50 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public class IntegerSetIterator implements Iterator<Integer> {

        private int mCurrent;
        private int mNext;

        /**
         * Creates a new <code>IntegerSetIterator</code>
         */
        public IntegerSetIterator() {
            super();
            mNext = mMinIdx - 1;
            moveToNextElement();
            mMinIdx = mNext;
        }

        private void moveToNextElement() {
            mNext++;
            while (mNext < IntegerSet.this.mMask.length && !IntegerSet.this.mMask[mNext].booleanValue())
                mNext++;
        }

        @Override
        public boolean hasNext() {
            return mNext < IntegerSet.this.mMask.length;
        }

        @Override
        public Integer next() {
            if (!hasNext())
                throw new NoSuchElementException();
            mCurrent = mNext;
            moveToNextElement();
            return mCurrent;
        }

        @Override
        public void remove() {
            if (mCurrent < 0)
                throw new IllegalStateException();
            IntegerSet.this.remove(mCurrent);
            mCurrent = -1;
        }

    }

}
