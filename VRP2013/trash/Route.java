/**
 * 
 */
package vrp2013.datamodel;

import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.Cloneable;

/**
 * <code>Route</code> is a general interface for a CVRP route
 * <p>
 * Creation date: 09/04/2013 - 8:06:56 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public abstract class Route implements Iterable<Integer>, Cloneable<Route> {

    /**
     * The vehicle id (optional)
     */
    private final int mVehicle;

    /**
     * Returns the id of the vehicle associated with this route (optional)
     * 
     * @return the vehicle id
     */
    public int getVehicle() {
        return mVehicle;
    }

    /**
     * Creates a new <code>Route</code>
     * 
     * @param vehicle
     *            the id of the vehicle associated with this route (optional)
     */
    public Route(int vehicle) {
        super();
        mVehicle = vehicle;
    }

    /**
     * Check if this route contains a node
     * 
     * @param node
     * @return {@code true} if this route contains {@code  node}
     */
    public abstract boolean contains(int node);

    /**
     * Returns this route as a list of nodes
     * 
     * @return this route as a list of nodes
     */
    public abstract List<Integer> getNodeSequence();

    /**
     * Insert a sequence of nodes at a specific position
     * 
     * @param subroute
     * @param index
     */
    public abstract void insertNodesAt(List<Integer> subroute, int index);

    /**
     * Insert a single node at a specific position
     * 
     * @param node
     * @param index
     */
    public abstract void insertNodeAt(int node, int index);

    /**
     * Remove a node
     * 
     * @param node
     *            the node to be removed
     */
    public abstract void remove(int node);

    /**
     * Returns a subroute
     * 
     * @param start
     *            the index of the first node
     * @param end
     *            the index of the last node
     * @return the nodes between indices {@code  start} and {@code  end}
     */
    public abstract List<Integer> getSubroute(int start, int end);

    /**
     * Remove and return a sequence of nodes
     * 
     * @param start
     *            the index of the first node to be removed
     * @param end
     *            the index of the last node to be removed
     * @return the nodes previously between indices {@code  start} and {@code  end}
     */
    public abstract List<Integer> extractSubroute(int start, int end);

    /**
     * Returns the node at a specific position
     * 
     * @param index
     * @return the node at position {@code  index}
     */
    public abstract int getNodeAt(int index);

    /**
     * Sets the node at a specific position
     * 
     * @param index
     * @param node
     * @return the node previously at position {@code  index}
     */
    public abstract int setNodeAt(int index, int node);

    /**
     * Remove and return a node at a specific position
     * 
     * @param index
     * @return the node previously at position {@code  index}
     */
    public abstract int extractNodeAt(int index);

    /**
     * Append a list of nodes to this route
     * 
     * @param nodes
     */
    public abstract void append(List<Integer> nodes);

    /**
     * Append a single node to this route
     * 
     * @param node
     */
    public abstract void append(int node);

    @Override
    public abstract ListIterator<Integer> iterator();

    /**
     * Returns the length of this route in number of nodes
     * 
     * @return the number of nodes in this route
     */
    public abstract int length();

    /**
     * Returns the last node in this route
     * 
     * @return the last node in this route
     */
    public abstract int getLastNode();

    /**
     * Returns the first node in this route
     * 
     * @return the first node in this route
     */
    public abstract int getFirstNode();

    /**
     * Look up the position of a node in this route
     * 
     * @param node
     * @return the index of {@code  node} in this route, or {@code  -1} if this route does not contain {@code  node}
     */
    public abstract int getNodePosition(int node);

    @Override
    public abstract Route clone();

}
