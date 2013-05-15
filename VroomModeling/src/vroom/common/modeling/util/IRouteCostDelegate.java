/**
 * 
 */
package vroom.common.modeling.util;

import java.util.List;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;

/**
 * <code>IRouteCostDelegate</code> is an interface for classes that will be
 * responsible of updating the cost of a route.
 * 
 * <p>
 * Creation date: Feb 16, 2011 - 11:09:20 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * 
 * @version 1.0
 * 
 */
public interface IRouteCostDelegate {

	/**
	 * Evaluate the cost of a route, ignoring any previously stored values.
	 * 
	 * @param route
	 *            the route that will be reevaluated
	 * @return
	 */
	public double evaluateRoute(IRoute<?> route);

	/**
	 * Updates the <code>route</code> cost after a node was inserted.
	 * 
	 * @param route
	 *            the modified route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if the node was inserted at
	 *            the beginning
	 * @param node
	 *            the inserted node
	 * @param successor
	 *            the successor, <code>null</code> if the node was appended
	 */
	public void nodeInserted(IRoute<?> route, INodeVisit predecessor,
			INodeVisit node, INodeVisit successor);

	/**
	 * Updates the <code>route</code> cost after a route was inserted.
	 * 
	 * @param route
	 *            the modified route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if the modified route was
	 *            inserted at the beginning
	 * @param insertedRoute
	 *            the inserted route, implementations should assume that its
	 *            cost is coherent
	 * @param successor
	 *            the successor, <code>null</code> if the modified route was
	 *            appended
	 */
	public void routeInserted(IRoute<?> route, INodeVisit predecessor,
			IRoute<?> insertedRoute, INodeVisit successor);

	/**
	 * Updates the <code>route</code> cost after a route was inserted.
	 * 
	 * @param route
	 *            the modified route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if the modified route was
	 *            inserted at the beginning
	 * @param insertedRoute
	 *            the inserted route
	 * @param successor
	 *            the successor, <code>null</code> if the modified route was
	 *            appended
	 */
	public void routeInserted(IRoute<?> route, INodeVisit predecessor,
			List<? extends INodeVisit> insertedRoute, INodeVisit successor);

	/**
	 * Updates the <code>route</code> cost after a node was removed.
	 * 
	 * @param route
	 *            the modified route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if the first node was
	 *            removed
	 * @param node
	 *            the removed node
	 * @param successor
	 *            the successor, <code>null</code> if the last node was removed
	 */
	public void nodeRemoved(IRoute<?> route, INodeVisit predecessor,
			INodeVisit node, INodeVisit successor);

	/**
	 * Updates the <code>route</code> cost after a node was replaced.
	 * 
	 * @param route
	 *            the modified route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if the first node was
	 *            replaced
	 * @param previousNode
	 *            the node that was replaced
	 * @param node
	 *            the new node
	 * @param successor
	 *            the successor, <code>null</code> if the last node was replaced
	 */
	public void nodeReplaced(IRoute<?> route, INodeVisit predecessor,
			INodeVisit previousNode, INodeVisit node, INodeVisit successor);

	/**
	 * Updates the <code>route</code> cost after two nodes were swapped.
	 * 
	 * <p>
	 * Note that <code>node1</code> have to appear before <code>node2</code> in
	 * the route
	 * </p>
	 * 
	 * @param route
	 *            the modified route
	 * @param pred1
	 *            the predecessor of the first node
	 * @param node1
	 *            the first node
	 * @param succ1
	 *            the successor of the first node
	 * @param pred2
	 *            the predecessor of the second node
	 * @param node2
	 *            the second node
	 * @param succ2
	 *            the successor of the second node
	 */
	public void nodesSwapped(IRoute<?> route, INodeVisit pred1,
			INodeVisit node1, INodeVisit succ1, INodeVisit pred2,
			INodeVisit node2, INodeVisit succ2);

	/**
	 * Updates the <code>route</code> cost after a subroute was removed.
	 * 
	 * @param route
	 *            the modified route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if the first node was
	 *            removed
	 * @param removedRoute
	 *            the removed route, implementations should assume that its cost
	 *            is coherent
	 * @param successor
	 *            the successor, <code>null</code> if the last node was removed
	 */
	public void subrouteRemoved(IRoute<?> route, INodeVisit predecessor,
			IRoute<?> removedRoute, INodeVisit successor);

	/**
	 * Updates the <code>route</code> cost after a subroute was removed.
	 * 
	 * @param route
	 *            the modified route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if the first node was
	 *            removed
	 * @param removedRoute
	 *            the removed route
	 * @param successor
	 *            the successor, <code>null</code> if the last node was removed
	 */
	public void subrouteRemoved(IRoute<?> route, INodeVisit predecessor,
			List<? extends INodeVisit> removedRoute, INodeVisit successor);

	/**
	 * Updates the <code>route</code> cost after a subroute was reversed.
	 * 
	 * @param route
	 *            the modified route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if the first node was
	 *            included
	 * @param first
	 *            the first node of the reversed subroute
	 * @param last
	 *            the last node of the reversed subroute
	 * @param successor
	 *            the successor, <code>null</code> if the last node was included
	 */
	public void subrouteReversed(IRoute<?> route, INodeVisit predecessor,
			INodeVisit first, INodeVisit last, INodeVisit successor);

	/**
	 * Gets the insertion cost of a subroute.
	 * 
	 * @param route
	 *            the considered route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if head insertion
	 * @param insertedRoute
	 *            the inserted route
	 * @param successor
	 *            the successor, <code>null</code> if tail insertion
	 * @return the insertion cost
	 */
	public abstract double getInsertionCost(IRoute<?> route,
			INodeVisit predecessor, List<? extends INodeVisit> insertedRoute,
			INodeVisit successor);

	/**
	 * Gets the insertion cost of a subroute.
	 * 
	 * @param route
	 *            the considered route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if head insertion
	 * @param insertedRoute
	 *            the inserted route
	 * @param successor
	 *            the successor, <code>null</code> if tail insertion
	 * @return the insertion cost
	 */
	public abstract double getInsertionCost(IRoute<?> route,
			INodeVisit predecessor, IRoute<?> insertedRoute,
			INodeVisit successor);

	/**
	 * Gets the insertion cost if a node.
	 * 
	 * @param route
	 *            the considered route
	 * @param predecessor
	 *            the predecessor, <code>null</code> if head insertion
	 * @param node
	 *            the inserted node
	 * @param successor
	 *            the successor, <code>null</code> if tail insertion
	 * @return the insertion cost
	 */
	public abstract double getInsertionCost(IRoute<?> route,
			INodeVisit predecessor, INodeVisit node, INodeVisit successor);

}
