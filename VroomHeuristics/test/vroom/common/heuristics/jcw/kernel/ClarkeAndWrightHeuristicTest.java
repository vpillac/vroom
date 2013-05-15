/**
 * 
 */
package vroom.common.heuristics.jcw.kernel;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWLogging;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.algorithms.RandomizedSavingsHeuristic;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.util.CircularInstanceGenerator;
import vroom.common.modeling.util.DefaultSolutionFactory;
import vroom.common.utilities.logging.LoggerHelper;

/**
 * <code>ClarkeAndWrightHeuristicTest</code> is a test case for
 * {@link ClarkeAndWrightHeuristic}
 * <p>
 * Creation date: Apr 30, 2010 - 8:54:55 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ClarkeAndWrightHeuristicTest {

	IVRPInstance mInstance;

	ClarkeAndWrightHeuristic<IVRPSolution<?>> mCW;

	private CWParameters mParameters;

	private ConstraintHandler<IVRPSolution<?>> mConstraintHandler;

	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setUp() throws Exception {
		// mInstance = VRPInstanceBuilder.newSimpleDynamicInstance(10, 5, 10,
		// 10, 0, false);
		List<Node> nodes = new LinkedList<Node>();
		mInstance = CircularInstanceGenerator.newCircularInstance(300, nodes,
				10);
		mInstance.setFleet(Fleet.newUnlimitedFleet(new Vehicle(0, "v", 10)));

		mParameters = new CWParameters();
		mConstraintHandler = new ConstraintHandler<IVRPSolution<?>>();
		mConstraintHandler
				.addConstraint(new CapacityConstraint<IVRPSolution<?>>());

		mParameters.set(CWParameters.SOLUTION_FACTORY_CLASS,
				DefaultSolutionFactory.class);

		mCW = new ClarkeAndWrightHeuristic(mParameters,
				RandomizedSavingsHeuristic.class, mConstraintHandler);
	}

	@Test
	public void testRun() {
		mCW.initialize(mInstance);

		mCW.run();

		IVRPSolution<?> sol = mCW.getSolution();

		CWLogging.getBaseLogger().info("Solution:");
		CWLogging.getBaseLogger().info(sol);
	}

	public static void main(String[] args) {
		LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_DEBUG,
				LoggerHelper.LEVEL_DEBUG, true);
		ClarkeAndWrightHeuristicTest test = new ClarkeAndWrightHeuristicTest();
		try {
			test.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		test.testRun();
	}
}
