/**
 * 
 */
package vroom.common.utilities.graphs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The Class <code>Cut</code> represents a cut as a set of node indices.
 * <p>
 * Creation date: 14 août 2010 - 18:28:35.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class Cut implements Iterable<Integer> {

    private final CompleteGraph mGraph;

    /** the set of nodes in this cut **/
    private final Set<Integer>  mCut;

    private double              mCapacity;

    private boolean             mChanged = true;

    /**
     * Getter for the set of nodes in this cut
     * 
     * @return the set of nodes in this cut
     */
    public Set<Integer> getCut() {
        return new HashSet<Integer>(mCut);
    }

    /** The complement of this cut */
    private final Set<Integer> mComplement;

    /**
     * Returns the complement of this cut
     * 
     * @param nodeCount
     *            the number of nodes in the
     * @return the complement of this cut
     */
    public Set<Integer> getComplement() {
        return new HashSet<Integer>(mComplement);
    }

    /**
     * Creates a new empty <code>Cut</code>
     * 
     * @param graph
     *            the parent graph
     */
    public Cut(CompleteGraph graph) {
        super();
        mGraph = graph;
        mCut = new HashSet<Integer>(graph.getNodeCount());
        mComplement = new HashSet<Integer>(graph.getNodeCount());
        for (int n = 0; n < graph.getNodeCount(); n++) {
            mComplement.add(n);
        }
        mChanged = true;
    }

    /**
     * Creates a new static <code>Cut</code>
     * <p/>
     * Note that nodes should not be added removed from cuts created this way
     * 
     * @param cut
     *            the nodes in the cut
     * @param complement
     *            the complement of the cut
     */
    public Cut(Set<Integer> cut, Set<Integer> complement) {
        mGraph = null;
        mCut = cut;
        mComplement = complement;
        mChanged = false;
    }

    /**
     * Add a node to this cut
     * 
     * @param node
     *            the node to be added
     * @return <code>true</code> if this cut did not already contained the node
     */
    public boolean add(int node) {
        boolean b = mCut.add(node);
        mComplement.remove(node);
        mChanged |= b;
        return b;
    }

    /**
     * Removes a node from this cut.
     * 
     * @param node
     *            the node to be removed
     * @return <code>true</code> if this cut contained the given node
     */
    public boolean remove(int node) {
        boolean b = mCut.remove(node);
        mComplement.add(node);
        mChanged |= b;
        return b;
    }

    /**
     * Test whether a node is in the cut or not
     * 
     * @param node
     * @return <code>true</code> if this cut contains <code>node</code>
     */
    public boolean contains(int node) {
        return mCut.contains(node);
    }

    /**
     * Returns the capacity of this cut.
     * 
     * @return the capacity of this cut
     */
    public double getCapacity() {
        if (mChanged) {
            mCapacity = 0;
            for (Integer i : mCut) {
                for (Integer j : mComplement) {
                    mCapacity += mGraph.getArcCapacity(i, j);
                }
            }
            mChanged = false;
        }

        return mCapacity;
    }

    /**
     * Number of nodes in this cut
     * 
     * @return the number of nodes in this cut
     */
    public int size() {
        return mCut.size();
    }

    @Override
    public Iterator<Integer> iterator() {
        return mCut.iterator();
    }

    @Override
    public String toString() {
        return String.format("Count:%s Capacity:%s Nodes:%s", size(), getCapacity(), mCut);
    }

}
