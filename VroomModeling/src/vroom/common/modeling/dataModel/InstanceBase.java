package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.dataModel.ObjectWithNameAndId;

/**
 * <code>InstanceBase</code> is a base type to describe a VRP instance.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public abstract class InstanceBase extends ObjectWithNameAndId implements IVRPInstance {

    /** The cost helper. */
    private CostCalculationDelegate mCostHelper;

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#setCostHelper(vroom.common
     * .modelling.dataModel.CostCalculationDelegate)
     */
    @Override
    public void setCostHelper(CostCalculationDelegate costHelper) {
        mCostHelper = costHelper;
    }

    /**
     * Getter for <code>costHelper</code>
     * 
     * @return the costHelper
     */
    @Override
    public CostCalculationDelegate getCostDelegate() {
        return mCostHelper;
    }

    /** The depots. */
    private List<Depot> mDepots;

    @Override
    public List<Depot> getDepots() {
        return new ArrayList<Depot>(mDepots);
    }

    /** The nodes defined in this instance */
    private final Map<Integer, NodeVisit> mNodesVisits;

    /**
     * Getter for the set of node visits
     * 
     * @return the reference to the set of nodes defined in this instance
     */
    protected Map<Integer, NodeVisit> getNodeVisitsMap() {
        return mNodesVisits;
    }

    /** A mapping between requests and the associated node visits */
    private final Map<IVRPRequest, NodeVisit[]> mNodesMap;

    /** The depots defined in this instance as node visits */
    private final Map<Integer, NodeVisit>       mDepotsVisits;

    /**
     * Getter for <code>depotsVisits</code>
     * 
     * @return the depotsVisits
     */
    @Override
    public Set<INodeVisit> getDepotsVisits() {
        return new HashSet<INodeVisit>(mDepotsVisits.values());
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getNodeVisits()
     */
    @Override
    public Set<INodeVisit> getNodeVisits() {
        return new HashSet<INodeVisit>(mNodesVisits.values());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#getNodeVisits(vroom.common
     * .modelling.dataModel.IRequest)
     */
    @Override
    public INodeVisit[] getNodeVisits(IVRPRequest request) {
        return mNodesMap.get(request);
    }

    @Override
    public INodeVisit getNodeVisit(int nodeID) {
        if (mDepotsVisits.containsKey(nodeID))
            return mDepotsVisits.get(nodeID);
        else if (mNodesVisits.containsKey(nodeID))
            return mNodesVisits.get(nodeID);
        else
            throw new IllegalArgumentException("No node or depot is associated with id " + nodeID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getArcs()
     */
    @Override
    public Set<IArc> getArcs() {
        return mArcs;
    }

    /** The arcs defined in this instance */
    private final Set<IArc> mArcs;

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#setDepots(java.util.List)
     */
    @Override
    public void setDepots(List<Depot> depots) {
        if (mDepots != null) {
            throw new IllegalStateException("The depot list is already set for this instance:"
                    + mDepots);
        }
        mDepots = depots;
        if (depots != null) {
            for (Depot depot : depots) {
                mDepotsVisits.put(depot.getID(), newDepotVisit(depot));
            }
        }
    }

    protected NodeVisit newDepotVisit(Depot depot) {
        return new NodeVisit(depot);
    }

    /** The fleet. */
    private Fleet<?> mFleet;

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#setFleet(vroom.common.modeling
     * .dataModel.Fleet)
     */
    @Override
    public void setFleet(Fleet<?> fleet) {
        // if(mFleet!=null)
        // throw new
        // IllegalStateException("The fleet is already set for this instance:"+mFleet);
        mFleet = fleet;
    }

    /** The routing problem. */
    private final VehicleRoutingProblemDefinition mRoutingProblem;

    /** The planning period. */
    private IPlanningPeriod                       mPlanningPeriod;

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#setPlanningPeriod(vroom
     * .common.modelling.dataModel.IPlanningPeriod)
     */
    @Override
    public void setPlanningPeriod(IPlanningPeriod planningPeriod) {
        if (mPlanningPeriod != null) {
            throw new IllegalStateException("The planning period is already set for this instance:"
                    + mPlanningPeriod);
        }
        mPlanningPeriod = planningPeriod;
    }

    /** A flag defining whether this instance graph is symmetric or not **/
    private boolean mSymmetric;

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#isSymmetric()
     */
    @Override
    public boolean isSymmetric() {
        return mSymmetric;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#setSymmetric(boolean)
     */
    @Override
    public void setSymmetric(boolean symmetric) {
        mSymmetric = symmetric;
    }

    /**
     * Instantiates a new instance base.
     * 
     * @param id
     *            an id for this instance
     * @param name
     *            a name for this instance
     * @param routingProblem
     *            a definition of the associated routing problem
     * @param fleet
     *            the fleet that will be associated with this instance
     * @param depots
     *            a list of the depots present in this instance
     * @param planningPeriod
     *            the planning period associated with this instance
     * @param costDelegate
     *            a delegate for the calculation of costs
     */
    public InstanceBase(int id, String name, VehicleRoutingProblemDefinition routingProblem,
            Fleet<?> fleet, List<Depot> depots, IPlanningPeriod planningPeriod,
            CostCalculationDelegate costDelegate) {
        super(name, id);
        mRoutingProblem = routingProblem;
        mSymmetric = true;

        mArcs = new HashSet<IArc>();
        mNodesVisits = new HashMap<Integer, NodeVisit>();
        mDepotsVisits = new HashMap<Integer, NodeVisit>();

        mNodesMap = new HashMap<IVRPRequest, NodeVisit[]>();

        setFleet(fleet);
        setDepots(depots);
        setCostHelper(costDelegate);
        setPlanningPeriod(planningPeriod);
    }

    /**
     * Creates a new empty <code>InstanceBase</code>
     * 
     * @param id
     *            an id for this instance
     * @param routingProblem
     *            a definition of the associated routing problem
     */
    public InstanceBase(int id, String name, VehicleRoutingProblemDefinition routingProblem) {
        this(id, name, routingProblem, null, null, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#addRequest(vroom.common
     * .modelling.dataModel.IRequest)
     */
    @Override
    public final boolean addRequest(IVRPRequest request) {
        boolean b = addRequestInternal(request);

        if (b) {
            addNodes(request);
        }

        return b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#addRequests(java.util.
     * Collection)
     */
    @Override
    public boolean addRequests(Collection<IVRPRequest> requests) {
        boolean b = true;
        for (IVRPRequest request : requests) {
            b &= addRequest(request);
        }
        return b;
    }

    /**
     * Add the {@link NodeVisit} corresponding to the given request to the set of node visits {@link #mNodesVisits}
     * 
     * @param request
     *            the request for which the nodes will be generated
     */
    private void addNodes(IVRPRequest request) {
        NodeVisit[] nodes = NodeVisit.createNodeVisits(request);

        // Adding arcs with the depots
        for (NodeVisit depot : mDepotsVisits.values()) {
            for (NodeVisit n : nodes) {
                addArc(depot, n);
            }
        }

        // Adding arcs with other nodes
        for (NodeVisit node : mNodesVisits.values()) {
            for (NodeVisit n : nodes) {
                addArc(node, n);
            }
        }
        for (NodeVisit n : nodes) {
            mNodesVisits.put(n.getID(), n);
        }

        mNodesMap.put(request, nodes);
    }

    /**
     * Add the arc(s) between node1 and node2. If the instance is {@link #isSymmetric() symmetric} then only one arc
     * (node1,node2) is added, otherwise both are added.
     * 
     * @param node1
     * @param node2
     */
    private void addArc(NodeVisit node1, NodeVisit node2) {
        if (node1.compareTo(node2) > 0) {
            NodeVisit tmp = node1;
            node1 = node2;
            node2 = tmp;
        }

        if (!Utilities.equal(node1, node2)) {
            mArcs.add(new Arc(node1, node2, mCostHelper.getDistance(node1, node2), !isSymmetric()));
            if (!isSymmetric()) {
                mArcs.add(new Arc(node2, node1, mCostHelper.getDistance(node2, node1),
                        !isSymmetric()));
            }
        }
    }

    /**
     * Add a new request to this instance.
     * 
     * @param request
     * @return <code>true</code> if the request was successfully added to the instance
     */
    protected abstract boolean addRequestInternal(IVRPRequest request);

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#removeRequest(vroom.common
     * .modelling.dataModel.IRequest)
     */
    @Override
    public boolean removeRequest(IVRPRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#getCost(vroom.common.modeling
     * .dataModel.Node, vroom.common.modeling.dataModel.Node)
     */
    @Override
    public double getCost(INodeVisit origin, INodeVisit destination) {
        return mCostHelper.getCost(origin, destination);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#getCost(vroom.common.modeling
     * .dataModel.Node, vroom.common.modeling.dataModel.Node,
     * vroom.common.modeling.dataModel.Vehicle)
     */
    @Override
    public double getCost(INodeVisit origin, INodeVisit destination, Vehicle vehicle) {
        return mCostHelper.getCost(origin, destination, vehicle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.IVRPInstance#getInsertionCost(vroom.
     * common.modelling.dataModel.Node, vroom.common.modeling.dataModel.Node,
     * vroom.common.modeling.dataModel.Node,
     * vroom.common.modeling.dataModel.Vehicle)
     */
    @Override
    public double getInsertionCost(INodeVisit node, INodeVisit pred, INodeVisit succ,
            Vehicle vehicle) {
        return mCostHelper.getInsertionCost(node, pred, succ, vehicle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getDepot(int)
     */
    @Override
    public Depot getDepot(int depotId) {
        return mDepots.get(depotId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getFleet()
     */
    @Override
    public Fleet<?> getFleet() {
        return mFleet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getDepotCount()
     */
    @Override
    public int getDepotCount() {
        return mDepots.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getRequestCount()
     */
    @Override
    public abstract int getRequestCount();

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getRequests()
     */
    @Override
    public abstract List<IVRPRequest> getRequests();

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getRoutingProblem()
     */
    @Override
    public VehicleRoutingProblemDefinition getRoutingProblem() {
        return mRoutingProblem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.IVRPInstance#getPlanningPeriod()
     */
    @Override
    public IPlanningPeriod getPlanningPeriod() {
        return mPlanningPeriod;
    }

    @Override
    public String toString() {
        return String.format("%s (size:%s fleet size:%s)", getName(), getRequestCount(), getFleet()
                .isUnlimited() ? "na" : getFleet().size());
    }

}// end VRPInstance