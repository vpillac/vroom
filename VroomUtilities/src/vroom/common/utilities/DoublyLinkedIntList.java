/**
 * 
 */
package vroom.common.utilities;

import java.util.List;

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
public class DoublyLinkedIntList extends DoublyLinkedIntSet implements List<Integer> {

    /**
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <code>DoublyLinkedIntegerList</code>.
     * 
     * @param maxValue
     *            the maximum value that will be stored in this list
     */
    public DoublyLinkedIntList(Integer maxValue) {
        super(maxValue);
    }

    /**
     * Gets the first element.
     * 
     * @return the first element
     */
    public Integer getFirst() {
        return super.getFirst();
    }

    /**
     * Gets the last element.
     * 
     * @return the last element
     */
    public Integer getLast() {
        return super.getLast();
    }

    /**
     * Gets the element predecessor.
     * 
     * @param element
     *            the element
     * @return the element predecessor
     */
    public Integer getPred(Integer element) {
        return super.getPred(element);
    }

    /**
     * Gets the element successor.
     * 
     * @param element
     *            the element
     * @return the element successor
     */
    public Integer getSucc(Integer element) {
        return super.getSucc(element);
    }

    /**
     * Insert an element.
     * 
     * @param element
     *            the element to be inserted
     * @param succ
     *            the successor of the inserted element
     */
    public void insert(Integer element, Integer succ) {
        super.insert(element, succ);
    }
}
