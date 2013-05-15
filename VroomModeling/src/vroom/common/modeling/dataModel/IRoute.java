package vroom.common.modeling.dataModel;

import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.ICloneable;

/**
 * The Interface IRoute defines the methods that a route has to implement
 * 
 * @param <V>
 *            the value type
 */
public interface IRoute<V extends INodeVisit> extends Iterable<V>, ICloneable<IRoute<V>>, java.lang.Cloneable {

    /**
     * Gets the parent solution.
     * 
     * @return the parent solution
     * @see Solution
     */
    public IVRPSolution<?> getParentSolution();

    /**
     * Gets the vehicle.
     * 
     * @return the associated vehicle
     */
    public Vehicle getVehicle();

    /**
     * Calculates the total cost of this route. Note that this implementation supposes that the all nodes are explicitly
     * contained in the route, including the depot.
     * 
     * @return the cost of this route
     */
    public double getCost();

    /**
     * Calculation of the route cost.
     * 
     * @param force
     *            <code>true</code> if the cost has to be re-calculated
     */
    public void calculateCost(boolean force);

    /**
     * Changes the stored cost
     * 
     * @param delta
     *            the value to be added to the currently stored cost
     */
    public void updateCost(double delta);

    /**
     * Changes the stored load for the specified product
     * 
     * @param product
     *            the product for which the load has to be updated
     * @param delta
     *            the value to be added to the currently stored load
     */
    public void updateLoad(int product, double delta);

    /**
     * Calculation of the current load of the vehicle.
     * 
     * @return the current load of the vehicle, assuming that there is only one product
     */
    public double getLoad();

    /**
     * Calculation of the current load of the vehicle for a specific product.
     * 
     * @param product
     *            the index of the product for which the remaining capacity is needed
     * @return the current load for the specified <code>product</code>
     */
    public double getLoad(int product);

    /**
     * Calculation of the current loads of the vehicle for all products.
     * 
     * @return an array containing the current loads of each compartment of the vehicle
     */
    public double[] getLoads();

    /**
     * Calculation of the current load of the vehicle for each product.
     * 
     * @param force
     *            if <code>true</code> then the loads will be recalculated
     */
    public void calculateLoad(boolean force);

    /**
     * Checks if the route can accommodate the request
     * <p>
     * The default implementation only considers the capacity constraint of the vehicle for single node request
     * </p>
     * .
     * 
     * @param request
     *            the request to be checked
     * @return if this route can accommodate the given
     */
    public boolean canAccommodateRequest(IVRPRequest request);

    /**
     * Access to a node at a specific position.
     * 
     * @param index
     *            the index of the desired vertex
     * @return the that is at position
     */
    public V getNodeAt(int index);

    /**
     * Search for a node
     * 
     * @param node
     *            the node which position is required
     * @return the position of the given node in the route, or <code>-1</code> is the node was not found
     */
    public int getNodePosition(INodeVisit node);

    /**
     * Access to the first node.
     * 
     * @return the first {@link NodeVisit} of this route
     */
    public V getFirstNode();

    /**
     * Access to the last node.
     * 
     * @return the last {@link NodeVisit} of this route
     */
    public V getLastNode();

    /**
     * Returns the subroute defined by the range <code>start-end</code>.
     * 
     * @param start
     *            the position of the first node of the subroute
     * @param end
     *            the position of the last node of the subroute
     * @return a list containing the nodes in the positions between
     */
    public List<V> subroute(int start, int end);

    /*
     * Route manipulation methods
     */
    /**
     * Appending of a node at the end of the route.
     * <p>
     * This implementation automatically updates the route cost and remaining capacity
     * </p>
     * 
     * @param node
     *            the node to be added at the end of the route
     * @return if the node was successfully added, otherwise
     */
    public boolean appendNode(V node);

    /**
     * Appending of a route at the end of this route.
     * <p>
     * This implementation automatically updates the route cost and remaining capacity
     * </p>
     * 
     * @param appendedRoute
     *            the route to be appended at the end of this route
     * @return <code>true</code> if the route was successfully appended
     */
    public boolean appendRoute(IRoute<? extends V> appendedRoute);

    /**
     * Appending of a route at the end of this route.
     * <p>
     * This implementation automatically updates the route cost and remaining capacity
     * </p>
     * 
     * @param appendedRoute
     *            the route to be appended at the end of this route
     * @return <code>true</code> if the route was successfully appended
     */
    public boolean appendNodes(List<? extends V> node);

    /**
     * Sets the node at a specific index of the route.
     * 
     * @param index
     *            the index of the node to be set
     * @param node
     *            the node that will be visited at position <code>index</code>
     * @return the previous node that was at position
     */
    public V setNodeAt(int index, V node);

    /**
     * Extracts the specified node from this route.<br/>
     * The node at position <code>index</code>will be removed from this route and returned<br/>
     * <b>Please note that the first node (with index 0) and the last node (with index length-1) will generally be
     * depots and should not be removed from the route.</b>
     * 
     * @param index
     *            the index of the node to be extracted
     * @return the node previously at position
     */
    public V extractNode(int index);

    /**
     * Extracts the specified subroute from this route.<br/>
     * The nodes between indexes <code>start</code> and <code>end</code> (inclusive) will be removed from this route and
     * returned as a route.<br/>
     * <b>Please note that the first node (with index 0) and the last node (with index length-1) will generally be
     * depots and should not be removed from the route.</b>
     * 
     * @param start
     *            the index of first node of the subroute
     * @param end
     *            the index of the last node of the subroute
     * @return a route containing the nodes between i and j (inclusive)
     */
    public IRoute<V> extractSubroute(int start, int end);

    /**
     * Extracts the specified subroute from this route.<br/>
     * The nodes between indexes <code>start</code> and <code>end</code> (inclusive) will be removed from this route and
     * returned as a list.<br/>
     * <b>Please note that the first node (with index 0) and the last node (with index length-1) will generally be
     * depots and should not be removed from the route.</b>
     * 
     * @param start
     *            the index of first node of the subroute
     * @param end
     *            the index of the last node of the subroute
     * @return a list containing the nodes between i and j (inclusive)
     */
    public List<V> extractNodes(int start, int end);

    /**
     * Calculation of the best insertion position of a node in the route.
     * 
     * @param node
     *            the node to be inserted
     * @return a description of the best insertion
     */
    public NodeInsertion getBestNodeInsertion(INodeVisit node);

    /**
     * Calculation of the best insertion position of a node in the route between specific bounds.
     * <p>
     * Will find the best insertion between positions <code>min</code> and <code>max</code> (inclusive), as defined in
     * {@link #insertNode(int, INodeVisit)}
     * </p>
     * 
     * @param node
     *            the node to be inserted
     * @param min
     *            the minimum insertion position (between <code>0</code> and <code>length</code>)
     * @param max
     *            the maximum insertion position (between <code>0</code> and <code>length</code>)
     * @return a description of the best insertion
     */
    public abstract NodeInsertion getBestNodeInsertion(INodeVisit node, int min, int max);

    /**
     * Best insertion of a node in this route
     * <p>
     * This implementation automatically updates the route cost and remaining capacity
     * </p>
     * .
     * 
     * @param node
     *            the node visit to be inserted
     * @return otherwise
     * @see #insertNode(int, NodeVisit)
     */
    public boolean bestInsertion(V node);

    /**
     * Insertion of a node at a specific index of the route.<br/>
     * <p>
     * If operation is successful, the <code>node</code> will be at position <code>index</code> while the node
     * previously at index <code>index</code> (if any) and after will be shifted to the right (their indices will be
     * incremented by 1)
     * </p>
     * <p>
     * This implementation automatically updates the route cost and remaining capacity
     * </p>
     * 
     * @param index
     *            the index at which the given node should be inserted
     * @param node
     *            the node that will be visited at position <code>index</code>
     * @return <code>true</code> if the node was correctly inserter.
     */
    public boolean insertNode(int index, V node);

    /**
     * Insertion of a node at a specific position in the route.<br/>
     * If operation is successful, the <code>node</code> will be at position
     * 
     * @param ins
     *            the the insertion position and cost
     * @param node
     *            the node that will be visited at position <code>index</code>
     * @return otherwise {@link NodeInsertion#mPosition ins.position} while the node previously at this position (if
     *         any) and after will be shifted to the right (their indices will be incremented by 1)
     *         <p>
     *         This implementation automatically updates the route cost and remaining capacity
     *         </p>
     */
    public boolean insertNode(NodeInsertion ins, V node);

    /**
     * Inserts the specified subroute a the specified position.<br/>
     * Shifts the node currently at that position (if any) and any subsequent elements to the right (adds
     * subroute.lenght() to their indices).
     * 
     * @param index
     *            the position at which the <code>subroute</code> will be inserted.
     * @param subroute
     *            the sequence of nodes to be inserter.
     * @return if the insertion was successful
     */
    public boolean insertSubroute(int index, IRoute<? extends V> subroute);

    /**
     * Inserts the specified node sequence a the specified position.<br/>
     * Shifts the node currently at that position (if any) and any subsequent elements to the right (adds nodes.size()
     * to their indices).
     * 
     * @param index
     *            the position at which the <code>nodes</code> will be inserted.
     * @param nodes
     *            the sequence of nodes to be inserter.
     * @return if the insertion was successful
     */
    public boolean insertNodes(int index, List<? extends V> nodes);

    /**
     * Swaps the specified nodes in this route.<br/>
     * The node at position <code>node1</code> will be at <code>node2</code> and vice-versa.
     * 
     * @param node1
     *            the index of the first node
     * @param node2
     *            the index of the second node
     * @return if the swap was successful
     */
    public boolean swapNodes(int node1, int node2);

    /**
     * Reverses the subroute specified by the <code>start</code> and <code>end</code> indices inside this route
     * (inclusive).
     * 
     * @param start
     *            the position of the first node of the subroute to be reverted
     * @param end
     *            the position of the last node of the subroute to be reverted
     */
    public void reverseSubRoute(int start, int end);

    /**
     * Reverses this route.
     * <p/>
     * All node positions will be swapped.
     */
    public void reverseRoute();

    /**
     * To string.
     * 
     * @return the string
     */
    @Override
    public String toString();

    /**
     * Length of this route.
     * 
     * @return the number of nodes in this route (including the depot)
     */
    public int length();

    /**
     * Sequence of {@link NodeVisit}.
     * 
     * @return a list containing the sequence of {@link NodeVisit} of this route
     */
    public List<V> getNodeSequence();

    /**
     * Checks if this route visits a given {@link NodeVisit}
     * 
     * @param node
     *            the node visit
     * @return <code>true</code> if <code>node</code> is visited by this route
     */
    public boolean contains(INodeVisit node);

    /**
     * Removes a node from this route
     * 
     * @param node
     *            the node to be removed
     * @return <code>true</code> if the node was contained in this route and removed, <code>false</code> otherwise
     */
    public boolean remove(INodeVisit node);

    @Override
    public ListIterator<V> iterator();

    /**
     * Returns the node sequence as a string
     * 
     * @return the node sequence as a strings
     */
    public String getNodeSeqString();
}