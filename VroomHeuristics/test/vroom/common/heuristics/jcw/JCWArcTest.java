/**
 * 
 */
package vroom.common.heuristics.jcw;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.heuristics.cw.JCWArc;
import vroom.common.modeling.dataModel.Arc;
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
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.util.EuclidianDistance;

/**
 * <code>JCWArcTest</code> is a test case for {@link JCWArc}
 * <p>
 * Creation date: Apr 30, 2010 - 9:25:22 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class JCWArcTest {

	private int numNodes = 10;
	private double radius = 10;

	private List<Node> nodes;
	private IRoute<INodeVisit> route;
	private Collection<Arc> arcs;
	private Collection<JCWArc> jCWArcs;
	private EuclidianDistance costHelper;

	@Before
	public void setUp() throws Exception {

		Depot depot = new Depot(0, new PointLocation(radius, 0));

		Vehicle v = new Vehicle(0, "TestTruc", 100);
		costHelper = new EuclidianDistance();
		route = new ArrayListRoute(new Solution<IRoute<?>>(new StaticInstance(
				"TestInstance", 0, Fleet.newHomogenousFleet(1, v),
				Collections.singletonList(depot), null, costHelper)), v);

		nodes = new ArrayList<Node>(numNodes + 2);
		arcs = new LinkedList<Arc>();
		jCWArcs = new LinkedList<JCWArc>();

		route.appendNode(new NodeVisit(depot));
		nodes.add(depot);
		for (int n = 1; n <= numNodes; n++) {
			double x = radius * Math.cos(2 * Math.PI * n / (numNodes + 1));
			double y = radius * Math.sin(2 * Math.PI * n / (numNodes + 1));

			Node node = new Node(n, new PointLocation(x, y));
			nodes.add(node);
			for (NodeVisit nv : NodeVisit
					.createNodeVisits(new Request(n, node))) {
				route.appendNode(nv);
			}
		}
		route.appendNode(new NodeVisit(depot));
		nodes.add(depot);

		for (int i = 0; i < route.length(); i++) {
			for (int j = 0; j < route.length(); j++) {
				INodeVisit d = route.getNodeAt(j);
				INodeVisit o = route.getNodeAt(i);
				Arc a = new Arc(o, d, costHelper.getDistance(o, d), true);
				arcs.add(a);
				jCWArcs.add(new JCWArc(a, i * route.length() + j));
			}
		}
	}

	/**
	 * Test method for
	 * {@link vroom.common.heuristics.cw.JCWArc#compareTo(vroom.common.heuristics.cw.IJCWArc)}
	 * .
	 */
	@Test
	public void testCompareTo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link vroom.common.heuristics.cw.JCWArc#isHeadNodeInterior()}.
	 */
	@Test
	public void testIsHeadNodeInterior() {
	}

	/**
	 * Test method for
	 * {@link vroom.common.heuristics.cw.JCWArc#isTailNodeInterior()}.
	 */
	@Test
	public void testIsTailNodeInterior() {
		fail("Not yet implemented");
	}

}
