/**
 *
 */
package vroom.common.modeling.dataModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.dataModel.IObjectWithID;
import vroom.common.utilities.dataModel.IObjectWithName;
import vroom.common.utilities.optimization.IInstance;

/**
 * <code>IVRPInstance</code> is a
 * <p>
 * Creation date: Apr 29, 2010 - 10:38:17 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IVRPInstance extends IInstance, IObjectWithID, IObjectWithName {

    /**
     * Set the {@link CostCalculationDelegate} for this instance.
     * 
     * @param costHelper
     *            the cost helper to be set
     * @throws IllegalStateException
     *             if a value was already set
     */
    public void setCostHelper(CostCalculationDelegate costHelper);

    /**
     * Set of nodes defined in this instance
     * 
     * @return a set containing the nodes defined in this instance
     */
    public Set<INodeVisit> getNodeVisits();

    /**
     * Getter for the {@linkplain NodeVisit node visits} associated with a given request
     * 
     * @param request
     *            the considered request
     * @return a collection containing the node visits associated with <code>request</code>
     */
    public INodeVisit[] getNodeVisits(IVRPRequest request);

    /**
     * Set of arcs defined in this instance
     * 
     * @return the set containing the arcs that have been defined in this instance
     */
    public Set<IArc> getArcs();

    /**
     * Set the list of {@linkplain Depot Depots} for this instance
     * 
     * @param depots
     *            the list of depots to be set
     * @throws IllegalStateException
     *             if a value was already set
     */
    public void setDepots(List<Depot> depots);

    /**
     * Set the fleet associated with this instance
     * 
     * @param fleet
     *            the fleet to be sets
     * @throws IllegalStateException
     *             if a value was already set
     */
    public void setFleet(Fleet<?> fleet);

    /**
     * Set the planning period for this instance
     * 
     * @param planningPeriod
     *            the planning period to be set
     * @throws IllegalStateException
     *             if a value was already set
     */
    public void setPlanningPeriod(IPlanningPeriod planningPeriod);

    /**
     * Getter for the symmetric flag
     * 
     * @return A flag defining whether this instance graph is symmetric or not
     */
    public boolean isSymmetric();

    /**
     * Setter for the symmetric flag : A flag defining whether this instance graph is symmetric or not
     * 
     * @param symmetric
     *            the value to be set for the symmetric flag
     */
    public void setSymmetric(boolean symmetric);

    /**
     * Add a new request to this instance.
     * 
     * @param request
     *            the request to be added
     * @return <code>true</code> if the request was successfully added to the instance, otherwise.
     */
    public boolean addRequest(IVRPRequest request);

    /**
     * Add the given requests to this instance
     * 
     * @param requests
     *            the requests to be added
     * @return <code>true</code> if the requests were successfully added
     */
    public boolean addRequests(Collection<IVRPRequest> requests);

    /**
     * Remove a request from this instance.
     * 
     * @param request
     *            the request to be added
     * @return if the request was successfully removed from the instance, otherwise.
     */
    public boolean removeRequest(IVRPRequest request);

    /**
     * Cost calculation as the distance between two points <br/>
     * It is recommended to use {@link #getCost(INodeVisit, INodeVisit, Vehicle)} instead.
     * 
     * @param origin
     *            the origin node
     * @param destination
     *            the destination node
     * @return the cost between
     * @see CostCalculationDelegate#getCost(Node, Node)
     */
    public double getCost(INodeVisit origin, INodeVisit destination);

    /**
     * Cost calculation based on two nodes and a specific vehicle.
     * 
     * @param origin
     *            the origin node
     * @param destination
     *            the destination node
     * @param vehicle
     *            the considered vehicle
     * @return the cost associated with the () arc when the gievn is used
     * @see CostCalculationDelegate#getCost(Node, Node,Vehicle)
     */
    public double getCost(INodeVisit origin, INodeVisit destination, Vehicle vehicle);

    /**
     * Calculation of the insertion cost.
     * 
     * @param node
     *            the node to be inserted
     * @param pred
     *            the candidate predecessor of <code>node</code>
     * @param succ
     *            the candidate successor of <code>node</code>
     * @param vehicle
     *            the considered vehicle
     * @return the cost of inserting as given by
     * @see CostCalculationDelegate#getInsertionCost(Node, Node, Node, Vehicle)
     */
    public double getInsertionCost(INodeVisit node, INodeVisit pred, INodeVisit succ,
            Vehicle vehicle);

    /**
     * Access to a specific depot.
     * 
     * @param depotId
     *            the id of the considered depot
     * @return the depot with id
     */
    public Depot getDepot(int depotId);

    /**
     * List of the depots currently defined in this instance.<br/>
     * Implementations should not return the data structure used to store the depots but instead a new list containing
     * its elements
     * 
     * @return a list of the depots of this instance
     */
    public List<Depot> getDepots();

    /**
     * Getter for the vehicle fleet.
     * 
     * @return the fleet defined for this instance
     */
    public Fleet<?> getFleet();

    /**
     * Depot count.
     * 
     * @return the number of depots in this instance
     */
    public int getDepotCount();

    /**
     * Request count.
     * 
     * @return the number of requests in this instance.
     */
    public int getRequestCount();

    /**
     * List of the requests currently defined in this instance.<br/>
     * Implementations should not return the data structure used to store the requests but instead a new list containing
     * its elements
     * 
     * @return a list of the requests of this instance
     */
    public List<IVRPRequest> getRequests();

    /**
     * Returns the request with the specified id
     * 
     * @param id
     *            the id of the request
     * @return the request with the specified id
     */
    public IVRPRequest getRequest(int id);

    /**
     * Getter for the associated routing problem definition.
     * 
     * @return the routing problem definition associated with this instance
     */
    public VehicleRoutingProblemDefinition getRoutingProblem();

    /**
     * Getter for the planning period.
     * 
     * @return the planning period associated with this instance
     */
    public IPlanningPeriod getPlanningPeriod();

    /**
     * Getter for the set of depot visits
     * 
     * @return the set of depot visits
     */
    public abstract Set<INodeVisit> getDepotsVisits();

    /**
     * Getter for the cost helper
     * 
     * @return the cost calculation delegate used to calculate travelling costs
     */
    public abstract CostCalculationDelegate getCostDelegate();

    /**
     * Retrieves a node visit from its id
     * 
     * @param nodeID
     *            the node id
     * @return the corresponding node visit
     */
    public INodeVisit getNodeVisit(int nodeID);

}