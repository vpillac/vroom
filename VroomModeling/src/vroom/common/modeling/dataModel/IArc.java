/**
 * 
 */
package vroom.common.modeling.dataModel;

/**
 * <code>IArc</code> is a generic definition of an arc in a graph.
 * <p>
 * Creation date: Apr 29, 2010 - 7:36:39 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IArc {

    /**
     * Getter for the tail node visit
     * 
     * @return The tail node visit
     */
    public INodeVisit getTailNode();

    /**
     * Getter for the head node visit
     * 
     * @return The head node visit
     */
    public INodeVisit getHeadNode();

    /**
     * Getter for the distance
     * 
     * @return the distance separating the origin and head
     */
    public double getDistance();

    /**
     * Getter for the directed flag
     * 
     * @return A flag defining whether this arc is directed or not
     */
    public boolean isDirected();

}