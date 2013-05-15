/**
 * 
 */
package vroom.common.modeling.util;

import java.util.Collections;
import java.util.List;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.dataModel.attributes.PointLocation;

/**
 * <code>CircularInstanceGenerator</code> is a utility class that can be use to generate simple circular instances
 * <p>
 * Creation date: Apr 30, 2010 - 11:39:43 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CircularInstanceGenerator {

    public static IVRPInstance newCircularInstance(int numNodes, List<Node> nodes) {
        return newCircularInstance(numNodes, nodes, 10);
    }

    public static IVRPInstance newCircularInstance(int numNodes, List<Node> nodes, int radius) {
        Depot depot = new Depot(0, new PointLocation(0, 0));

        Vehicle v = new Vehicle(0, "TestTruc", numNodes);
        CostCalculationDelegate costHelper = new EuclidianDistance();
        ArrayListRoute route = new ArrayListRoute(new Solution<IRoute<?>>(new StaticInstance(
                "TestInstance", 0, Fleet.newHomogenousFleet(1, v),
                Collections.singletonList(depot), null, costHelper)), v);

        IVRPInstance instance = new DynamicInstance("CircualInstance", 0,
                Fleet.newUnlimitedFleet(v), Collections.singletonList(depot),
                VehicleRoutingProblemDefinition.VRP, costHelper);

        nodes.add(depot);
        route.appendNode(new NodeVisit(depot));
        for (int n = 1; n <= numNodes; n++) {
            double x = radius * Math.cos(2 * Math.PI * n / (numNodes + 1));
            double y = radius * Math.sin(2 * Math.PI * n / (numNodes + 1));

            Node node = new Node(n, new PointLocation(x, y));
            nodes.add(node);

            IVRPRequest req = new Request(n, node);
            req.setDemands(1);
            instance.addRequest(req);

            for (NodeVisit nv : NodeVisit.createNodeVisits(new Request(n, node))) {
                route.appendNode(nv);
            }
        }
        nodes.add(depot);
        route.appendNode(new NodeVisit(depot));

        return instance;
    }

}
