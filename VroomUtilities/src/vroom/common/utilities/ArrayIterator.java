package vroom.common.utilities;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * <code>ArrayIterator</code> is used to create a simple iterator over an array of type E.
 * <p>
 * Creation date: Apr 19, 2010 - 4:36:22 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <E>
 */
public class ArrayIterator<E> implements ListIterator<E> {

    private final E[] mArray;

    private int       mPosition = 0;

    /**
     * Creates a new <code>ArrayIterator</code>
     * 
     * @param array
     */
    public ArrayIterator(E[] array) {
        mArray = array;
    }

    @Override
    public boolean hasNext() {
        return (mPosition < mArray.length);
    }

    @Override
    public E next() {
        if (hasNext()) {
            return mArray[mPosition++];
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPrevious() {
        return (mPosition > 0);
    }

    @Override
    public E previous() {
        if (hasPrevious()) {
            return mArray[--mPosition];
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public int nextIndex() {
        return mPosition;
    }

    @Override
    public int previousIndex() {
        return mPosition - 1;
    }

    @Override
    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(E e) {
        throw new UnsupportedOperationException();
    }

}
