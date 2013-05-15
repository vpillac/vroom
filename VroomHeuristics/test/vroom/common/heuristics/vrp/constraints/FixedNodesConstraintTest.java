/*
 * 
 */
package vroom.common.heuristics.vrp.constraints;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.heuristics.Move;
import vroom.common.heuristics.vrp.OrOptMove;
import vroom.common.heuristics.vrp.StringExchangeMove;
import vroom.common.heuristics.vrp.SwapMove;
import vroom.common.heuristics.vrp.TwoOptMove;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.CircularInstanceGenerator;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;

/**
 * <code>FixedNodesConstraintTest</code>
 * <p>
 * Creation date: Aug 25, 2010 - 6:08:20 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class FixedNodesConstraintTest {

	static int sNumNodes = 20;

	FixedNodesConstraint<Solution<ArrayListRoute>> ctr;
	Solution<ArrayListRoute> solution;
	IVRPInstance instance;

	private ArrayListRoute route2;

	private ArrayListRoute route1;

	@Before
	public void setUp() throws Exception {
		ctr = new FixedNodesConstraint<Solution<ArrayListRoute>>();
		System.out.println("--------------------------------");
		System.out.println(" SETUP");
		List<Node> nodes = new LinkedList<Node>();
		instance = CircularInstanceGenerator.newCircularInstance(sNumNodes,
				nodes, 10);
		instance.setFleet(Fleet.newHomogenousFleet(2, new Vehicle(0, "V",
				sNumNodes / 2 + 1)));

		solution = new Solution<ArrayListRoute>(instance);
		route1 = new ArrayListRoute(solution, instance.getFleet().getVehicle());
		route2 = new ArrayListRoute(solution, instance.getFleet().getVehicle());

		solution.addRoute(route1);
		solution.addRoute(route2);

		route1.appendNode(instance.getDepotsVisits().iterator().next());
		route2.appendNode(instance.getDepotsVisits().iterator().next());

		ArrayList<INodeVisit> visits = new ArrayList<INodeVisit>(
				instance.getNodeVisits());
		Collections.sort(visits, new ObjectWithIdComparator());
		for (int i = 0; i < visits.size(); i++) {
			if (i < visits.size() * 0.8) {
				route1.appendNode(visits.get(i));
			} else {
				route2.appendNode(visits.get(i));
			}
		}
		route1.getNodeAt(1).fix();

		route1.appendNode(instance.getDepotsVisits().iterator().next());
		route2.appendNode(instance.getDepotsVisits().iterator().next());

		System.out.println("Dummy solution initialized:");
		System.out.println(solution);
	}

	/**
	 * Test method for
	 * {@link vroom.common.heuristics.vrp.constraints.FixedNodesConstraint#isFeasible(vroom.common.modeling.dataModel.IVRPSolution, vroom.common.heuristics.Move)}
	 * .
	 */
	@Test
	public void testCheckMove() {
		assertTrue("First node should be fixed", route1.getFirstNode()
				.isFixed());
		assertTrue("First node should be fixed", route2.getFirstNode()
				.isFixed());
		assertTrue("Last node should be fixed", route1.getLastNode().isFixed());
		assertTrue("Last node should be fixed", route2.getLastNode().isFixed());

		// Two opt
		Move mve;
		for (int i = -5; i <= route1.length() + 5; i++) {
			for (int j = -5; j <= route2.length() + 5; j++) {
				for (int k = 0; k < 2; k++) {
					mve = new TwoOptMove(0, solution, 0, 1, i, j, k == 0);
					if (i < 1 || j < 0 || i > route1.length() - 2
							|| j > route2.length() - 2) {
						assertFalse("2opt Move should be illegal " + mve,
								ctr.isFeasible(solution, mve));
					} else {
						assertTrue("2opt Move should be legal " + mve,
								ctr.isFeasible(solution, mve));
					}

					if (i < j) {
						mve = new TwoOptMove(0, solution, 0, 0, i, j, k == 0);
						if (i < 1 || j < 0 || i > route1.length() - 2
								|| j > route1.length() - 2) {
							assertFalse("2opt Move should be illegal " + mve,
									ctr.isFeasible(solution, mve));
						} else {
							assertTrue("2opt Move should be legal " + mve,
									ctr.isFeasible(solution, mve));
						}
					}
				}
			}
		}

		// Swap
		for (int i = -5; i <= route1.length() + 5; i++) {
			for (int r = 0; r < solution.getRouteCount(); r++) {
				for (int j = -5; j <= solution.getRoute(r).length() + 5; j++) {
					mve = new SwapMove(0, solution, 0, r, i, j);
					if (i < 2 || j < 1 || i > route1.length() - 2
							|| j > solution.getRoute(r).length() - 2
							|| (r == 0 && j == 1)) {
						assertFalse("Swap Move should be illegal " + mve,
								ctr.isFeasible(solution, mve));
					} else {
						assertTrue("Swap Move should be legal " + mve,
								ctr.isFeasible(solution, mve));
					}
				}
			}
		}

		// Or-opt
		for (int i = 0; i <= route1.length(); i++) {
			for (int j = i; j <= route1.length(); j++) {
				for (int r = 0; r < solution.getRouteCount(); r++) {
					for (int k = 0; k < solution.getRoute(r).length(); k++) {
						mve = new OrOptMove<Solution<ArrayListRoute>>(0,
								solution, 0, i, j, k, r);
						if (i <= 1 || j < i || i >= route1.length() - 1
								|| j >= route1.length() - 1
								|| (r == 0 && i <= k && k <= j)
								|| k <= (r == 0 ? 1 : 0)
								|| k >= solution.getRoute(r).length() - 1) {
							assertFalse("Or-opt Move should be illegal " + mve,
									ctr.isFeasible(solution, mve));
						} else {
							assertTrue("Or-opt Move should be legal " + mve,
									ctr.isFeasible(solution, mve));
						}
					}
				}
			}
		}

		// String
		for (int i = 0; i <= route1.length(); i++) {
			for (int j = i; j <= route1.length() + 5; j++) {
				for (int r = 0; r < solution.getRouteCount(); r++) {
					for (int k = 0; k < solution.getRoute(r).length(); k++) {
						for (int l = k; l < solution.getRoute(r).length(); l++) {
							mve = new StringExchangeMove<Solution<ArrayListRoute>>(
									solution, 0, r, i, j, k, l);

							int m = r == 0 ? 1 : 0;
							if (i <= 1 || j < i || i >= route1.length() - 1
									|| j >= route1.length() - 1 || k <= m
									|| l < k
									|| k >= solution.getRoute(r).length() - 1
									|| l >= solution.getRoute(r).length() - 1) {
								assertFalse("Or-opt Move should be illegal "
										+ mve, ctr.isFeasible(solution, mve));
							} else {
								assertTrue(
										"Or-opt Move should be legal " + mve,
										ctr.isFeasible(solution, mve));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Test method for
	 * {@link vroom.common.heuristics.vrp.constraints.FixedNodesConstraint#isFeasible(vroom.common.modeling.dataModel.IVRPSolution)}
	 * .
	 */
	@Test
	public void testCheckSolution() {
		// fail("Not yet implemented");
	}

}
