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

import vroom.common.utilities.dataModel.IObjectWithID;

/**
 * The class <code>ObjectWithIDSet</code> is an implementation of {@link Set} used to store instances of
 * {@link IObjectWithID}.
 * <p>
 * Creation date: Mar 29, 2012 - 3:58:55 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ObjectWithIdSet<E extends IObjectWithID> extends AbstractSet<E> {

    private final Object[] mMask;
    private int            mSize;
    private int            mMinIdx;

    /**
     * Creates a new <code>ObjectWithIDSet</code> with an initial set of values.
     * <p>
     * Note that the set will not be able to handle values that are higher than the maximum id stored in {@code  values}
     * </p>
     * 
     * @param values
     *            an initial set of values
     */
    public ObjectWithIdSet(Collection<E> values) {
        int maxValue = -1;
        for (IObjectWithID i : values)
            if (i.getID() > maxValue)
                maxValue = i.getID();

        mMask = new Object[maxValue + 1];
        mMinIdx = mMask.length - 1;
        addAll(values);
    }

    /**
     * Creates a new <code>ObjectWithIDSet</code> with an initial set of values
     * 
     * @param values
     *            an initial set of values
     * @param maxID
     *            the maximum id that will ever be contained in this set
     */
    public ObjectWithIdSet(Collection<E> values, int maxID) {
        this(maxID);
        addAll(values);
    }

    /**
     * Creates a new empty <code>ObjectWithIDSet</code>
     * 
     * @param maxID
     *            the maximum id that will ever be contained in this set
     */
    public ObjectWithIdSet(int maxID) {
        mMask = new Object[maxID + 1];
        mMinIdx = mMask.length - 1;
        mSize = 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof IObjectWithID) {
            int e = ((IObjectWithID) o).getID();
            if (e < 0 || e >= mMask.length)
                return false;
            return mMask[e] != null;
        } else {
            return false;
        }
    }

    @Override
    public boolean add(IObjectWithID o) {
        Object prev = mMask[o.getID()];
        if (prev == null) {
            mMask[o.getID()] = o;
            if (o.getID() < mMinIdx)
                mMinIdx = o.getID();
            mSize++;
            return true;
        } else
            return false;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof IObjectWithID) {
            IObjectWithID e = (IObjectWithID) o;
            if (e.getID() < 0 || e.getID() >= mMask.length)
                return false;
            Object prev = mMask[e.getID()];
            if (prev != null) {
                mMask[e.getID()] = null;
                if (e.getID() == mMinIdx)
                    mMinIdx++;
                mSize--;
                return true;
            } else
                return false;
        } else {
            return false;
        }
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
    public ObjectWithIDSetIterator iterator() {
        return new ObjectWithIDSetIterator();
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
        Arrays.fill(mMask, null);
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
    public class ObjectWithIDSetIterator implements Iterator<E> {

        private int mCurrent;
        private int mNext;

        /**
         * Creates a new <code>IntegerSetIterator</code>
         */
        public ObjectWithIDSetIterator() {
            super();
            mNext = mMinIdx - 1;
            moveToNextElement();
            mMinIdx = mNext;
        }

        private void moveToNextElement() {
            mNext++;
            while (mNext < ObjectWithIdSet.this.mMask.length && ObjectWithIdSet.this.mMask[mNext] == null)
                mNext++;
        }

        @Override
        public boolean hasNext() {
            return mNext < ObjectWithIdSet.this.mMask.length;
        }

        @SuppressWarnings("unchecked")
        @Override
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            mCurrent = mNext;
            moveToNextElement();
            return (E) ObjectWithIdSet.this.mMask[mCurrent];
        }

        @Override
        public void remove() {
            if (mCurrent < 0)
                throw new IllegalStateException();
            ObjectWithIdSet.this.remove(mCurrent);
            mCurrent = -1;
        }

    }

}
