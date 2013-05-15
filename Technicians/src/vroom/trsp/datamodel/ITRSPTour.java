package vroom.trsp.datamodel;

import java.util.List;

import vroom.common.utilities.optimization.ISolution;

/**
 * <code>ITRSPTour</code> is a common interface for all classes that will represent a tour in the TRSP
 * <p>
 * Creation date: Aug 16, 2011 - 3:25:19 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ITRSPTour extends Iterable<Integer>, ISolution {

    /** The Constant UNDEFINED is used when a node has no or predecessor/successor */
    public final static int UNDEFINED = -2;

    /**
     * Returns the parent instance
     * 
     * @return the parent instance
     */
    public TRSPInstance getInstance();

    /**
     * Gets the id of the technician performing this tour
     * 
     * @return the id of the technician performing this tour
     */
    public int getTechnicianId();

    /**
     * Gets the total cost associated with this tour.
     * 
     * @return the total cost
     */
    public double getTotalCost();

    /**
     * Sets the total cost associated with this tour
     * 
     * @param totalCost
     *            the total cost
     */
    public void setTotalCost(double totalCost);

    /**
     * Returns the node sequence of this tour as an array
     * 
     * @return the node sequence of this tour as an array
     */
    public abstract int[] asArray();

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object obj);

    /**
     * Returns the id of the first node of this tour
     * 
     * @return the id of the first node of this tour
     */
    public int getFirstNode();

    /**
     * Returns the id of the last node of this tour
     * 
     * @return the id of the last node of this tour
     */
    public int getLastNode();

    /**
     * Returns the id of the node at the given position
     * 
     * @param index
     *            the position of the node in the tour
     * @return the id of the node at the given position
     */
    public int getNodeAt(int index);

    /**
     * Gets the length of this tour.
     * 
     * @return the length of this tour
     */
    public int length();

    /**
     * Returns the node sequence in a list.
     * 
     * @return the node sequence
     */
    public abstract List<Integer> asList();

    /**
     * Returns the node sequence in form of a String <code>&lt;node1,...,nodeK&gt;</code>
     * 
     * @returns the node sequence as a String
     */
    public String getNodeSeqString();

    /**
     * Checks if a node is visited in this tour, in other words returns <code>true</code> if <code>node</code> has a
     * predecessor or a successor
     * 
     * @param node
     *            the node to be checked
     * @return <code>true</code> is the node is visited
     */
    public boolean isVisited(int node);

    @Override
    public ITourIterator iterator();

    @Override
    public ISolution clone();

    /**
     * Returns the parent solution for this tour, or <code>null</code> if this tour is not associated to any solution
     * 
     * @return the parent solution for this tour, or <code>null</code> if this tour is not associated to any solution
     */
    public TRSPSolution getSolution();

    /**
     * Returns a detailed string representing this solution
     * 
     * @return
     */
    public String toDetailedString();

}