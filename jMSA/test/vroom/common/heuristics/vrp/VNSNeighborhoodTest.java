/*
 * 
 */
package vroom.common.heuristics.vrp;

import java.util.LinkedList;
import java.util.List;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.algorithms.RandomizedSavingsHeuristic;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.vns.VNSParameters;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.heuristics.vrp.constraints.FixedNodesConstraint;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.CircularInstanceGenerator;
import vroom.common.modeling.util.DefaultSolutionFactory;
import vroom.common.modeling.visualization.VRPVisualizationUtilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.OptimizationSense;

/**
 * <code>VNSNeighborhoodTest</code>
 * <p>
 * Creation date: Aug 26, 2010 - 3:51:55 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VNSNeighborhoodTest {

	public static void main(String[] args) {
		LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN,
				LoggerHelper.LEVEL_DEBUG, true);

		CWParameters cwParams = new CWParameters();
		ConstraintHandler<Solution<ArrayListRoute>> constraintHandler = new ConstraintHandler<Solution<ArrayListRoute>>();
		constraintHandler
				.addConstraint(new CapacityConstraint<Solution<ArrayListRoute>>());
		constraintHandler
				.addConstraint(new FixedNodesConstraint<Solution<ArrayListRoute>>());

		cwParams.set(CWParameters.SOLUTION_FACTORY_CLASS,
				DefaultSolutionFactory.class);

		ClarkeAndWrightHeuristic<Solution<ArrayListRoute>> cw = new ClarkeAndWrightHeuristic<Solution<ArrayListRoute>>(
				cwParams, RandomizedSavingsHeuristic.class,
				constraintHandler);

		List<INeighborhood<Solution<ArrayListRoute>, ?>> neighborhoods = new LinkedList<INeighborhood<Solution<ArrayListRoute>, ?>>();
		neighborhoods.add(new SwapNeighborhood<Solution<ArrayListRoute>>(
				constraintHandler));
		neighborhoods.add(new TwoOptNeighborhood<Solution<ArrayListRoute>>(
				constraintHandler));
		neighborhoods.add(new OrOptNeighborhood<Solution<ArrayListRoute>>(
				constraintHandler));
		neighborhoods
				.add(new StringExchangeNeighborhood<Solution<ArrayListRoute>>(
						constraintHandler));

		VNSParameters vnsParams = new VNSParameters(10000, 50000, false, 1000,
				100, null, null);

		VariableNeighborhoodSearch<Solution<ArrayListRoute>> vns = VariableNeighborhoodSearch
				.newVNS(VNSVariant.VND, OptimizationSense.MINIMIZATION, null,
						new MRG32k3a(), neighborhoods);

		for (int i = 1; i < 4; i += 2) {
			int size = 30 * i;
			int radius = i;
			List<Node> nodes = new LinkedList<Node>();
			IVRPInstance instance = CircularInstanceGenerator
					.newCircularInstance(size, nodes, radius);
			instance.setFleet(Fleet.newUnlimitedFleet(new Vehicle(0, "v", 10)));

			cw.initialize(instance);
			cw.run();

			Solution<ArrayListRoute> sol = cw.getSolution();
			VRPVisualizationUtilities.showVisualizationFrame(sol).setTitle(
					String.format("Size: %s CW", size));

			sol = vns.localSearch(instance, sol, vnsParams);
			VRPVisualizationUtilities.showVisualizationFrame(sol).setTitle(
					String.format("Size: %s CW + VNS", size));

			System.out.printf(
					"Circular instance of size %s, radius:%s, opt:%s \n", size,
					radius, (2 * Math.PI + 2 * (size / 10)) * radius);
			System.out.println(sol);
			for (ArrayListRoute r : sol) {
				if (!r.getFirstNode().isDepot()) {
					System.err.println("Route does not start with a depot");
				}
				if (!r.getLastNode().isDepot()) {
					System.err.println("Route does not end with a depot");
				}
			}
			System.out.println();
		}

	}

}
