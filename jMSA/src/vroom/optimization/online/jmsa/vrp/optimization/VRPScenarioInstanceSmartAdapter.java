package vroom.optimization.online.jmsa.vrp.optimization;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import vroom.common.heuristics.cw.JCWArc;
import vroom.common.modeling.dataModel.Arc;
import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IPlanningPeriod;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPShrunkRequest;

/**
 * <code>VRPScenarioInstanceSmartAdapter</code> is an implementation of {@link IVRPInstance} based on an existing
 * scenario.
 * <p/>
 * It is used to only show the information related to one scenario and to pass the correct references to a generic
 * optimization procedure.
 * <p>
 * Creation date: May 4, 2010 - 9:51:33 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPScenarioInstanceSmartAdapter implements IVRPInstance {

    /** The underlying scenario **/
    private final VRPScenario mScenario;

    /**
     * Getter for scenario : The underlying scenario
     * 
     * @return the value of scenario
     */
    public VRPScenario getScenario() {
        return mScenario;
    }

    private final Set<INodeVisit>    mDepotsVisits;
    private final Set<INodeVisit>    mNodes;

    private final Set<IArc>          mArcs;

    private final VRPShrunkRequest[] mShrunkNodes;

    /**
     * @param resource
     * @return <code>true</code> if the given resource has already started its service and therefore the first node of
     *         the route should be the shrunk node
     */
    public boolean isFirstNodeShrunk(int resource) {
        return getShrunkNode(resource) != null;
    }

    /**
     * @param resource
     * @return <code>true</code> if the given resource has already served the last assigned node
     */
    public boolean isShrunkNodeServed(int resource) {
        return getShrunkNode(resource).isServed();
    }

    /**
     * @param resource
     * @return the shrunk node associated with the specified resource
     */
    public VRPShrunkRequest getShrunkNode(int resource) {
        return mShrunkNodes[resource];
    }

    public VRPScenarioInstanceSmartAdapter(VRPScenario scenario) {
        super();

        // TODO this should be replaced by directly accessing the instance
        // where the set of arcs should be maintained

        mScenario = scenario;
        mDepotsVisits = new HashSet<INodeVisit>();

        mNodes = new HashSet<INodeVisit>();
        mArcs = new HashSet<IArc>();

        mNodes.addAll(mScenario.getActualRequests());
        mNodes.addAll(mScenario.getSampledRequests());

        MSAVRPInstance instance = mScenario.getParentInstance();

        mDepotsVisits.add(new VRPActualRequest(instance.getDepotsVisits().iterator().next()));

        mShrunkNodes = new VRPShrunkRequest[instance.getFleet().size()];

        // Create the aggregated node representing the current route
        Collection<VRPShrunkRequest> aggRequests = new LinkedList<VRPShrunkRequest>();
        for (int r = 0; r < instance.getFleet().size(); r++) {
            VRPShrunkRequest shReq = instance.getShrunkRequest(r);
            if (shReq.isEmpty()) {
                shReq.shrunkRequest(new VRPActualRequest(getDepotsVisits().iterator().next()));
            }

            aggRequests.add(shReq);
            mShrunkNodes[r] = shReq;
        }

        // Add all the aggregated visits to the global set
        mNodes.addAll(aggRequests);

        // Add bi-directional arcs between pending all nodes
        Set<INodeVisit> tmp = new HashSet<INodeVisit>(mNodes);
        for (INodeVisit n : getNodeVisits()) {
            tmp.remove(n);

            addArc(n, mDepotsVisits.iterator().next());

            for (INodeVisit m : tmp) {
                addArc(m, n);
            }
        }
    }

    @Override
    public boolean addRequest(IVRPRequest request) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see vroom.common.modeling.dataModel.IVRPInstance#addRequests(java.util.
     * Collection)
     */
    @Override
    public boolean addRequests(Collection<IVRPRequest> requests) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IArc> getArcs() {
        return mArcs;
    }

    @Override
    public double getCost(INodeVisit origin, INodeVisit destination) {
        return mScenario.getParentInstance().getCost(origin, destination);
    }

    @Override
    public double getCost(INodeVisit origin, INodeVisit destination, Vehicle vehicle) {
        return mScenario.getParentInstance().getCost(origin, destination, vehicle);
    }

    @Override
    public CostCalculationDelegate getCostDelegate() {
        return mScenario.getParentInstance().getCostDelegate();
    }

    @Override
    public Depot getDepot(int depotId) {
        return mScenario.getParentInstance().getDepot(depotId);
    }

    @Override
    public int getDepotCount() {
        return mScenario.getParentInstance().getDepotCount();
    }

    @Override
    public Set<INodeVisit> getDepotsVisits() {
        return mDepotsVisits;
    }

    @Override
    public Fleet<?> getFleet() {
        return mScenario.getParentInstance().getFleet();
    }

    @Override
    public double getInsertionCost(INodeVisit node, INodeVisit pred, INodeVisit succ,
            Vehicle vehicle) {
        return mScenario.getParentInstance().getInsertionCost(node, pred, succ, vehicle);
    }

    @Override
    public Set<INodeVisit> getNodeVisits() {
        return mNodes;
    }

    @Override
    public INodeVisit[] getNodeVisits(IVRPRequest request) {
        return mScenario.getParentInstance().getNodeVisits(request);
    }

    @Override
    public INodeVisit getNodeVisit(int requestId) {
        return mScenario.getParentInstance().getNodeVisit(requestId);
    }

    @Override
    public IPlanningPeriod getPlanningPeriod() {
        return mScenario.getParentInstance().getPlanningPeriod();
    }

    @Override
    public int getRequestCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Depot> getDepots() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IVRPRequest> getRequests() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IVRPRequest getRequest(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public VehicleRoutingProblemDefinition getRoutingProblem() {
        return mScenario.getParentInstance().getRoutingProblem();
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

    @Override
    public boolean removeRequest(IVRPRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCostHelper(CostCalculationDelegate costHelper) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setDepots(List<Depot> depots) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setFleet(Fleet<?> fleet) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setPlanningPeriod(IPlanningPeriod planningPeriod) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void setSymmetric(boolean symmetric) {
        throw new UnsupportedOperationException();

    }

    /**
     * Add the arc(s) between node1 and node2. If the instance is {@link #isSymmetric() symmetric} then only one arc
     * (node1,node2) is added, otherwise both are added.
     * 
     * @param node1
     * @param node2
     */
    private void addArc(INodeVisit node1, INodeVisit node2) {
        // if(node1.compareTo(node2)>0){
        // INodeVisit tmp = node1;
        // node1 = node2;
        // node2 = tmp;
        // }

        double cost = getCostDelegate().getDistance(node1, node2);
        double saving = -cost
                + getCostDelegate().getDistance(mDepotsVisits.iterator().next(), node1)
                + getCostDelegate().getDistance(mDepotsVisits.iterator().next(), node2);

        // Only add arcs with a negative saving?
        // if(saving>0){
        mArcs.add(new JCWArc(new Arc(node1, node2, cost, false), saving));
        // }
        // }
    }

    /*
     * (non-Javadoc)
     *
     * @see vroom.common.utilities.dataModel.IObjectWithID#getID()
     */
    @Override
    public int getID() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see vroom.common.utilities.dataModel.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return toString();
    }

}
