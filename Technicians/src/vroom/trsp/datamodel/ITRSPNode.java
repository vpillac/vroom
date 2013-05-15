/**
 * 
 */
package vroom.trsp.datamodel;

import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.utilities.IToShortString;
import vroom.common.utilities.dataModel.IObjectWithID;
import vroom.optimization.online.jmsa.IActualRequest;

/**
 * <code>ITRSPNode</code>
 * <p>
 * Creation date: Mar 19, 2012 - 4:30:04 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ITRSPNode extends IActualRequest, IObjectWithID, IToShortString {

    public static enum NodeType {
        REQUEST(false), HOME(true), MAIN_DEPOT(true);

        public String toShortString() {
            switch (this) {
            case REQUEST:
                return "R";
            case HOME:
                return "H";
            case MAIN_DEPOT:
                return "D";
            default:
                return "?";
            }
        }

        private final boolean mDepot;

        private NodeType(boolean depot) {
            mDepot = depot;
        }

        /**
         * Returns <code>true</code> if this type corresponds to a depot
         * 
         * @return <code>true</code> if this type corresponds to a depot
         */
        public boolean isDepot() {
            return mDepot;
        }

        /**
         * Returns <code>true</code> if this type corresponds to a home
         * 
         * @return <code>true</code> if this type corresponds to a home
         */
        public boolean isHome() {
            return this == HOME;
        }
    }

    /**
     * Returns the time required to service this node
     * 
     * @return the time required to service this node
     */
    public double getServiceTime();

    /**
     * Returns the time window within which this node may be visited
     * 
     * @return the time window within which this node may be visited
     */
    public ITimeWindow getTimeWindow();

    /**
     * Returns the type of node
     * 
     * @return the type of node
     */
    public NodeType getType();

    /**
     * Return the {@link Node} representing this instance in the graph
     * 
     * @return the {@link Node} representing this instance in the graph
     */
    public Node getNode();

    /**
     * Returns a string giving the type of node and its id
     * 
     * @return a string giving the type of node and its id
     */
    public String getDescription();

    /**
     * Return the release date of this node, by convention static requests, homes, and depots should have a negative
     * release date.
     * 
     * @return the release date of this node
     */
    public double getReleaseDate();

    /**
     * Returns the arrival time at the request. The value will only be defined in a dynamic context
     * 
     * @return the arrival
     */
    public double getArrivalTime();

    /**
     * Sets the arrival time at the request
     * 
     * @param arrival
     *            the arrival time to set
     */
    public void setArrivalTime(double arrival);

}
