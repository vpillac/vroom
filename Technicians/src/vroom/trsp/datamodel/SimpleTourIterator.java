package vroom.trsp.datamodel;

import java.util.NoSuchElementException;

/**
 * <code>SimpleTourIterator</code> is a generic implementation of {@link ITourIterator} for implementations
 * {@link ITRSPTour}. It uses {@link ITRSPTour#getNodeAt(int)} to iterate over the tour.
 * <p>
 * Creation date: Sep 26, 2011 - 4:47:01 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SimpleTourIterator implements ITourIterator {

    private final ITRSPTour mTour;
    private int             mPosition;

    /**
     * Creates a new <code>SimpleTourIterator</code>
     * 
     * @param it
     */
    public SimpleTourIterator(SimpleTourIterator it) {
        mTour = it.mTour;
        mPosition = it.mPosition;
    }

    /**
     * Creates a new <code>SimpleTourIterator</code>
     */
    public SimpleTourIterator(ITRSPTour tour) {
        mTour = tour;
        mPosition = 0;
    }

    @Override
    public synchronized void add(Integer e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean hasNext() {
        return mPosition < mTour.length();
    }

    @Override
    public synchronized boolean hasPrevious() {
        return mPosition > 0;
    }

    @Override
    public synchronized Integer next() {
        if (hasNext()) {
            return mTour.getNodeAt(mPosition++);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public synchronized Integer previous() {
        if (hasPrevious()) {
            return mTour.getNodeAt(--mPosition);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public synchronized int previousIndex() {
        return mPosition - 1;
    }

    @Override
    public synchronized int nextIndex() {
        return mPosition;
    }

    @Override
    public synchronized void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void set(Integer e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized ITourIterator subIterator() {
        return new SimpleTourIterator(this);
    }

}