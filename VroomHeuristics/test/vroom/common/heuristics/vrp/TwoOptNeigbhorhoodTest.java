package vroom.common.heuristics.vrp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.attributes.DeterministicDemand;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.modeling.util.EuclidianDistance;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;

public class TwoOptNeigbhorhoodTest {

	static int numNodes = 10;
	static int radius = 10;

	/** Swapped nodes indexes */
	static int numSwaps = 10;

	ArrayListRoute route1, route2;
	Solution<IRoute<INodeVisit>> solution;
	CostCalculationDelegate costHelper;
	List<Node> nodes1, nodes2;
	Depot depot;

	int id = 1;

	@Before
	public void setUp() throws Exception {
		depot = new Depot(0, new PointLocation(radius, 0));

		Vehicle v = new Vehicle(0, "TestTruc", numNodes);
		costHelper = new EuclidianDistance();

		solution = new Solution<IRoute<INodeVisit>>(new StaticInstance(
				"TestInstance", 0, Fleet.newHomogenousFleet(1, v),
				Collections.singletonList(depot), null, costHelper));

		route1 = new ArrayListRoute(solution, v);
		nodes1 = new ArrayList<Node>(numNodes + 2);
		generateRoute(route1, nodes1, radius, v);

		route2 = new ArrayListRoute(solution, v);
		nodes2 = new ArrayList<Node>(numNodes + 2);
		generateRoute(route2, nodes2, 2 * radius, v);

		solution.addRoute(route1);
		solution.addRoute(route2);

		System.out.println("Node sequences:");
		System.out.println(nodes1);
		System.out.println(nodes2);

		System.out.println("Routes:");
		System.out.println(route1);
		System.out.println(route2);

		System.out.println("Solution:");
		System.out.println(solution);
	}

	/**
	 * Generate a route
	 * 
	 * @param r
	 * @param nodes
	 * @param rte
	 * @return
	 */
	protected void generateRoute(ArrayListRoute rte, List<Node> nodes, int r,
			Vehicle v) {
		nodes.add(depot);
		rte.appendNode(new NodeVisit(depot));
		for (int n = 1; n <= numNodes; n++) {
			double x = r * Math.cos(2 * Math.PI * n / (numNodes + 1));
			double y = r * Math.sin(2 * Math.PI * n / (numNodes + 1));

			Node node = new Node(id, new PointLocation(x, y));
			nodes.add(node);
			for (NodeVisit nv : NodeVisit
					.createNodeVisits(new Request(id, node))) {
				nv.getParentRequest().setAttribute(RequestAttributeKey.DEMAND,
						new DeterministicDemand(1));
				rte.appendNode(nv);
			}

			id++;
		}
		nodes.add(depot);
		rte.appendNode(new NodeVisit(depot));
	}

	protected Solution<IRoute<INodeVisit>> scrambleSolution(
			Solution<IRoute<INodeVisit>> solution) {
		Solution<IRoute<INodeVisit>> scrambledSolution = solution.clone();

		Random r = new Random(0);
		for (int i = 0; i < numSwaps; i++) {
			int r1 = r.nextInt(solution.getRouteCount());
			int r2 = r.nextInt(solution.getRouteCount());

			int k = r.nextInt(solution.getRoute(r1).length() - 2) + 1;
			int l = r.nextInt(solution.getRoute(r2).length() - 2) + 1;

			INodeVisit n1 = scrambledSolution.getRoute(r1).extractNode(k);
			INodeVisit n2 = scrambledSolution.getRoute(r2).extractNode(l);
			scrambledSolution.getRoute(r2).insertNode(l, n1);
			scrambledSolution.getRoute(r1).insertNode(k, n2);
		}

		return scrambledSolution;
	}

	protected ArrayListRoute scrambleRoute(ArrayListRoute route) {
		ArrayListRoute scrambledRoute = route.clone();

		Random r = new Random(0);
		for (int i = 0; i < numSwaps; i++) {
			int k = r.nextInt(route.length() - 2) + 1;
			int l = r.nextInt(route.length() - 2) + 1;
			scrambledRoute.swapNodes(k, l);
		}

		return scrambledRoute;
	}

	@Test
	public void testExploreNeighborhood() {
		System.out.println("---------------------------------");
		System.out.println("ExploreNeighborhood test");
		System.out.println("---------------------------------");

		TwoOptNeighborhood<Solution<IRoute<INodeVisit>>> neighborhood = new TwoOptNeighborhood<Solution<IRoute<INodeVisit>>>();

		System.out.println("Thorough exploration");
		System.out.println("- - - - - - - - - - - - - - - - -");
		Solution<IRoute<INodeVisit>> scrambledSol = scrambleSolution(solution);
		System.out.println("Scrambled mSolution:");
		System.out.println(scrambledSol);
		neighborhood.localSearch(scrambledSol, new VRPParameters(
				Long.MAX_VALUE, Integer.MAX_VALUE, false, false, null));
		System.out.println("Resulting mSolution:");
		System.out.println(scrambledSol);
		TwoOptMove move = neighborhood.exploreNeighborhood(scrambledSol,
				new VRPParameters(Long.MAX_VALUE, Integer.MAX_VALUE, false,
						false, null));
		System.out.printf("Improving move: %s\n", move);

		System.out.println();
		System.out.println("####################################");
		System.out.println(" Capacity constraints");
		System.out.println("####################################");

		System.out.println("Thorough exploration");
		System.out.println("- - - - - - - - - - - - - - - - -");
		scrambledSol = scrambleSolution(solution);
		System.out.println("Scrambled mSolution:");
		System.out.println(scrambledSol);
		neighborhood.getConstraintHandler().addConstraint(
				new CapacityConstraint<Solution<IRoute<INodeVisit>>>());
		neighborhood.localSearch(scrambledSol, new VRPParameters(
				Long.MAX_VALUE, Integer.MAX_VALUE, false, false, null));
		System.out.println("Resulting mSolution:");
		System.out.println(scrambledSol);
		move = neighborhood.exploreNeighborhood(scrambledSol,
				new VRPParameters(Long.MAX_VALUE, Integer.MAX_VALUE, false,
						false, null));
		System.out.printf("Improving move: %s\n", move);

		// boolean
		// reversed=!scrambledRoute.getNodeAt(1).equals(route1.getNodeAt(1));
		// for(int nodeI=0; nodeI< route1.length(); nodeI++){
		// assertEquals(
		// reversed?route1.getNodeAt(route1.length()-1-nodeI):route1.getNodeAt(nodeI),
		// scrambledRoute.getNodeAt(nodeI));
		// }
		// assertEquals("Cost", optimalCost,scrambledRoute.getCost(),0.001);

		// System.out.println();
		// System.out.println("First improvement");
		// System.out.println("- - - - - - - - - - - - - - - - -");
		// scrambledRoute= route1.cloneObject();
		// scrambledRoute.swapNodes(1, 2);
		// scrambledRoute.swapNodes(5, 8);
		// System.out.println("Scrambled route:");
		// System.out.println(scrambledRoute);
		// neighborhood.localSearch(scrambledRoute,
		// new VRPParameters(Long.MAX_VALUE, Integer.MAX_VALUE, true, false,
		// costHelper));
		// System.out.println("Resulting route:");
		// System.out.println(scrambledRoute);

		// for(int nodeI=0; nodeI< route1.length(); nodeI++){
		// if(nodeI!=5 && nodeI!=8)
		// assertEquals(
		// route1.getNodeAt(nodeI),
		// scrambledRoute.getNodeAt(nodeI));
		// }
		// assertEquals(
		// route1.getNodeAt(5),
		// scrambledRoute.getNodeAt(8));
		// assertEquals(
		// route1.getNodeAt(8),
		// scrambledRoute.getNodeAt(5));

		// System.out.println();
		// System.out.println("Thorough exploration with started route");
		// ArrayListRoute routeCopy = route1.cloneObject();
		// routeCopy.extractSubroute(0, 5);
		// routeCopy.calculateCost(true);
		// double cost= routeCopy.getCost();
		// System.out.println("Started Route:");
		// System.out.println(routeCopy);
		// System.out.println("- - - - - - - - - - - - - - - - -");
		//
		// scrambledRoute=scrambleRoute(routeCopy);
		// System.out.println("Scrambled route:");
		// System.out.println(scrambledRoute);
		// neighborhood.localSearch(scrambledRoute,
		// new VRPParameters(Long.MAX_VALUE, Integer.MAX_VALUE, false, false,
		// costHelper));
		// System.out.println("Resulting route:");
		// System.out.println(scrambledRoute);
		//
		// reversed=!scrambledRoute.getNodeAt(1).equals(routeCopy.getNodeAt(1));
		// for(int nodeI=0; nodeI< routeCopy.length(); nodeI++){
		// assertEquals(
		// reversed?routeCopy.getNodeAt(route1.length()-1-nodeI):routeCopy.getNodeAt(nodeI),
		// scrambledRoute.getNodeAt(nodeI));
		// }
		// assertEquals("Cost", cost,scrambledRoute.getCost(),0.001);
	}

	public static void main(String[] args) {
		Logging.setupRootLogger(LoggerHelper.LEVEL_DEBUG,
				LoggerHelper.LEVEL_DEBUG, true);

		TwoOptNeigbhorhoodTest test = new TwoOptNeigbhorhoodTest();

		try {
			test.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}

		numSwaps = 20;

		test.testExploreNeighborhood();
	}
}
