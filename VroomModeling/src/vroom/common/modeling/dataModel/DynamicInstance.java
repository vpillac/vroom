package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import vroom.common.modeling.dataModel.attributes.IReleaseDate;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.Utilities;

/**
 * <code>DynamicInstance</code> is an extension of {@link InstanceBase} that represents a dynamic instance of a VRP
 * problem. By dynamic we mean that data from such instance can be changed after its initialization, causing the
 * notification of the registered {@link Observer}s.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public class DynamicInstance extends StaticInstance {

    /**
     * Creates a new <code>DynamicInstance</code> from a static instance. All references will be kept.
     * 
     * @param instance
     */
    public DynamicInstance(IVRPInstance instance) {
        super(instance.getName(), instance.getID(), instance.getFleet(), instance.getDepots(),
                instance.getRoutingProblem(), instance.getCostDelegate());
        addRequests(instance.getRequests());
        setSymmetric(instance.isSymmetric());
    }

    /**
     * Returns a list containing the requests already released at the specified {@code  time}
     * 
     * @param time
     * @return a list containing the requests already released at the specified {@code  time}
     */
    public List<IVRPRequest> getReleasedRequests(double time) {
        ArrayList<IVRPRequest> requests = new ArrayList<IVRPRequest>(getRequestCount());
        for (IVRPRequest r : getRequestsMap().values()) {
            IReleaseDate rd = r.getAttribute(RequestAttributeKey.RELEASE_DATE);
            if (rd == null || rd.doubleValue() <= time) {
                requests.add(r);
            }
        }
        return requests;
    }

    /**
     * Instantiates a new dynamic instance.
     * 
     * @param name
     *            the name
     * @param id
     *            the id
     * @param fleet
     *            the fleet
     * @param depots
     *            the depots
     * @param routingProblem
     *            the routing problem
     * @param costHelper
     *            the cost helper
     * @see InstanceBase#InstanceBase(int, String, VehicleRoutingProblemDefinition, Fleet, List, IPlanningPeriod,
     *      CostCalculationDelegate)
     */
    public DynamicInstance(String name, int id, Fleet<?> fleet, List<Depot> depots,
            VehicleRoutingProblemDefinition routingProblem, CostCalculationDelegate costHelper) {
        super(name, id, fleet, depots, routingProblem, costHelper);
    }

    /**
     * Creates a new <code>DynamicInstance</code>
     * 
     * @param name
     * @param id
     * @param vrpDef
     */
    public DynamicInstance(String name, int id, VehicleRoutingProblemDefinition vrpDef) {
        super(name, id, vrpDef);
    }

    public boolean nodeVisited(INodeVisit node) {
        boolean b = node != null && Utilities.equal(getNodeVisitsMap().get(node.getID()), node);

        if (b)
            getNodeVisitsMap().remove(node.getID());

        return b;
    }
}// end DynamicInstance