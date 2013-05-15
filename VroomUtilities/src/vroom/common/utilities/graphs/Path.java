/**
 * 
 */
package vroom.common.utilities.graphs;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * The Class <code>Path</code> represents a path in a graph as a list of node indices.
 * <p>
 * Creation date: 14 août 2010 - 18:36:40
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Path implements Iterable<Integer>, Cloneable {

    private final CompleteGraph       mGraph;

    /** the list of indices representing this path **/
    private final LinkedList<Integer> mNodeSequence;

    /**
     * Getter for the list of indices representing this path
     * 
     * @return the node sequence in this path
     */
    public LinkedList<Integer> getNodeSequence() {
        return new LinkedList<Integer>(mNodeSequence);
    }

    /** an optional flow defined on this path **/
    private double mFlow;

    /**
     * Getter for an optional flow defined on this path
     * 
     * @return the value of the flow defined on this path
     */
    public double getFlow() {
        return mFlow;
    }

    /**
     * Setter for an optional flow defined on this path
     * 
     * @param the
     *            flow defined on this path the value to be set for the flow defined on this path
     */
    public void setFlow(double flow) {
        mFlow = flow;
    }

    /**
     * Creates a new empty <code>Path</code>
     */
    public Path(CompleteGraph graph) {
        mGraph = graph;
        mNodeSequence = new LinkedList<Integer>();
    }

    /**
     * Append a node to this path
     * 
     * @param node
     *            the index of the node to be appended
     */
    public void append(int node) {
        mNodeSequence.add(node);
    }

    /**
     * Remove and return the last node of this path
     * 
     * @return the last node of this path
     */
    public int pop() {
        return mNodeSequence.pollLast();
    }

    /**
     * Returns the first node of this path
     * 
     * @return the first node of this path
     */
    public int getFirst() {
        return mNodeSequence.getFirst();
    }

    /**
     * Returns the last node of this path
     * 
     * @return the last node of this path
     */
    public int getLast() {
        return mNodeSequence.getLast();
    }

    @Override
    public Iterator<Integer> iterator() {
        return mNodeSequence.iterator();
    }

    @Override
    public Path clone() {
        Path clone = new Path(mGraph);
        clone.mNodeSequence.addAll(mNodeSequence);
        return clone;
    }

    /**
     * Returns the number of nodes in this path
     * 
     * @return the number of nodes in this path
     */
    public int lenght() {
        return mNodeSequence.size();
    }

    @Override
    public String toString() {
        return String.format("length:%s flow:%s path:%s", lenght(), getFlow(), mNodeSequence);
    }
}
