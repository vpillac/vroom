/**
 * 
 */
package vroom.common.utilities;

import java.util.AbstractSequentialList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Fixed size implementation of the {@link List} interface, this class holds a list of at most <code>k</code> elements
 * sorted according to their natural ordering.
 * <p>
 * <code>LimitedSortedList</code> is backed up by a doubly linked list, and {@link #add(Comparable) add} is executed in
 * {@code  O(k)}
 * </p>
 * <p>
 * Creation date: Oct 18, 2011 - 2:04:51 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class LimitedSortedList<E> extends AbstractSequentialList<E> {

    /** The maximum size of this list */
    private final int                   mMaxSize;

    private int                         mSize;

    private Element                     mFirst;
    private Element                     mLast;

    private final Comparator<? super E> mComparator;

    /**
     * Creates a new <code>LimitedSortedList</code>
     * 
     * @param size
     *            the desired size for this list
     */
    public LimitedSortedList(int size) {
        this(size, new Comparator<E>() {
            @SuppressWarnings("unchecked")
            @Override
            public int compare(E o1, E o2) {
                return ((Comparable<? super E>) o1).compareTo(o2);
            }
        });
    }

    /**
     * Creates a new <code>LimitedSortedList</code>
     * 
     * @param size
     *            the desired size for this list
     */
    public LimitedSortedList(int size, Comparator<E> comparator) {
        if (size < 1)
            throw new IllegalArgumentException("Illegal size: " + size);
        mMaxSize = size;
        mSize = 0;
        mComparator = comparator;
    }

    @Override
    public boolean add(E e) {
        if (mFirst == null) {
            // The list is currently empty
            mFirst = new Element(e);
            mLast = mFirst;
            mSize++;
            return true;
        } else if (isBefore(e, mFirst.mElement)) {
            // The element better than the first element
            Element el = new Element(e);
            el.mNext = mFirst;
            mFirst.mPrev = el;
            mFirst = el;
            mSize++;
            checkSize();
            return true;
        } else {
            boolean roomLeft = size() < mMaxSize;
            boolean betterThanLast = isBefore(e, mLast.mElement);
            if (roomLeft && !betterThanLast) {
                // Append the element to the end of the list
                Element el = new Element(e);
                el.mPrev = mLast;
                mLast.mNext = el;
                mLast = el;
                mSize++;
                // We dont check the size as this will only happen if we have space left
                return true;
            } else if (betterThanLast) {
                // The element better than the last element or the list not full
                ListIterator<E> it = listIterator();
                it.next(); // Skip the first element
                while (it.hasNext()) {
                    E next = it.next();
                    if (isBefore(e, next)) {
                        // The element is better than the next element of the list
                        break;
                    }
                }

                it.previous(); // Move the iterator to its previous position
                it.add(e); // Insert the element
                checkSize();

                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns {@code true} if {@code  o1} should appear before {@code  o2} in the list
     * 
     * @param o1
     * @param o2
     * @return {@code true} if {@code  o1} should appear before {@code  o2} in the list
     */
    private boolean isBefore(E o1, E o2) {
        return mComparator.compare(o1, o2) < 0;
    }

    private void checkSize() {
        while (size() > mMaxSize) {
            mLast = mLast.mPrev;
            mLast.mNext = null;
            mSize--;
        }
    }

    public int size() {
        return mSize;
    }

    /**
     * Returns the first element of this list
     * 
     * @return the first element of this list
     */
    public E first() {
        if (mFirst == null)
            throw new NoSuchElementException();
        return mFirst.mElement;
    }

    /**
     * Returns the last element of this list
     * 
     * @return the last element of this list
     */
    public E last() {
        if (mLast == null)
            throw new NoSuchElementException();
        return mLast.mElement;
    }

    @Override
    public void clear() {
        mFirst = null;
        mLast = null;
        mSize = 0;
    }

    @Override
    public LimitedSortedListIterator listIterator(int index) {
        if (index > size())
            throw new IndexOutOfBoundsException();

        int idx = 0;
        Element first = mFirst;

        while (idx < index) {
            first = first.mNext;
            idx++;
        }

        return new LimitedSortedListIterator(first);
    }

    private class Element {
        private final E mElement;
        private Element mPrev;
        private Element mNext;

        /**
         * Creates a new <code>Element</code>
         * 
         * @param element
         */
        private Element(E element) {
            mElement = element;
        }

        @Override
        public String toString() {
            return mElement != null ? mElement.toString() : "null";
        }
    }

    private class LimitedSortedListIterator implements ListIterator<E> {

        private Element mCurrent;
        private int     mIdx;

        private LimitedSortedListIterator(Element first) {
            mCurrent = new Element(null);
            mCurrent.mNext = first;
            mIdx = 0;
        }

        @Override
        public boolean hasNext() {
            return mCurrent != null && mCurrent.mNext != null;
        }

        @Override
        public E next() {
            if (!hasNext())
                throw new NoSuchElementException();
            mCurrent = mCurrent.mNext;
            mIdx++;
            return mCurrent.mElement;
        }

        @Override
        public void remove() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            if (mCurrent.mPrev == null) {
                // Current was the first node
                mFirst = mCurrent.mNext;
            } else {
                mCurrent.mPrev.mNext = mCurrent.mNext;
            }

            if (mCurrent.mNext == null) {
                // Current was the last node
                mLast = mCurrent.mPrev;
            } else {
                mCurrent.mNext.mPrev = mCurrent.mPrev;
            }
            mSize--;

        }

        @Override
        public boolean hasPrevious() {
            return mCurrent != null;
        }

        @Override
        public E previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            Element prev = mCurrent;
            mCurrent = mCurrent.mPrev;
            mIdx--;
            return prev.mElement;
        }

        @Override
        public int nextIndex() {
            return mIdx;
        }

        @Override
        public int previousIndex() {
            return mIdx - 1;
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
            Element el = new Element(e);
            el.mPrev = mCurrent;
            if (mCurrent.mNext != null)
                mCurrent.mNext.mPrev = el;

            el.mNext = mCurrent.mNext;
            mCurrent.mNext = el;

            if (mCurrent.mNext == mFirst)
                // The next element is the first element of the list
                mFirst = el;
            mCurrent = el;
            mSize++;
        }

    }
}
