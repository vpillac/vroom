/**
 * 
 */
package vroom.common.modeling.dataModel;

import java.util.List;

import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.utilities.dataModel.IObjectWithID;

/**
 * <code>INodeVisit</code> is an interface for classes represeting nodes of a
 * routes.
 * <p>
 * Creation date: Apr 29, 2010 - 1:59:33 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface INodeVisit extends Comparable<INodeVisit>, Cloneable,
		IObjectWithID {

	/**
	 * Getter for the predecessors list used in precedence constraints
	 * 
	 * @return a list of predecessors
	 */
	public List<INodeVisit> getPredecesors();

	/**
	 * Getter for the successor list used in precedence constraints
	 * 
	 * @return a list of successors
	 */
	public List<INodeVisit> getSuccessors();

	/**
	 * Getter for pickup
	 * 
	 * @return a flag defining whether this visit is a pickup or delivery for PD
	 *         problems
	 */
	public boolean isPickup();

	/**
	 * Fixed status of this node.
	 * <p>
	 * This flag may be used in local search procedures to define whether this
	 * node visit can be moved in the containing route
	 * </p>
	 * <p>
	 * An expected but not guaranteed behavior will be that if this node visit
	 * is the starting or ending depot of the route this method should return
	 * <code>true</code>, while interior nodes would return <code>false</code>
	 * </p>
	 * 
	 * @return <code>true</code> if this node is fixed and should not be moved,
	 *         <code>false</code> otherwise
	 * @see #fix()
	 * @see #free()
	 */
	public boolean isFixed();

	/**
	 * Set the {@link #isFixed() fixed} flag to <code>true</code>
	 * 
	 * @see #isFixed()
	 * @see #free()
	 */
	public void fix();

	/**
	 * Set the {@link #isFixed() fixed} flag to <code>false</code>
	 * 
	 * @see #fix()
	 * @see #isFixed()
	 */
	public void free();

	/**
	 * Gets the node.
	 * 
	 * @return the node
	 */
	public Node getNode();

	/**
	 * Gets the parent request.
	 * 
	 * @return the parentRequest
	 */
	public IVRPRequest getParentRequest();

	/**
	 * Checks if is depot.
	 * 
	 * @return if the visited node is a depot
	 */
	public boolean isDepot();

	/**
	 * Demand associated with this node for a specific product
	 * 
	 * @param product
	 * @return the demand of the underlying node visit for the specified product
	 */
	public double getDemand(int product);

	/**
	 * Demand associated with this node for a unique/default product
	 * 
	 * @return the demand of the underlying node visit
	 */
	public double getDemand();

	/**
	 * Returns the duration of this visit
	 * 
	 * @return the duration of this visit
	 */
	public double getServiceTime();

	/**
	 * Returns the time window for this visit
	 * 
	 * @return the time window for this visit
	 */
	public ITimeWindow getTimeWindow();

	public INodeVisit clone();
}