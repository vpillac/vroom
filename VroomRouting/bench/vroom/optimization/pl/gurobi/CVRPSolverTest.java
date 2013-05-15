/**
 * 
 */
package vroom.optimization.pl.gurobi;

import gurobi.GRB.DoubleAttr;
import gurobi.GRBException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.io.ChristofidesPersistenceHelper;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.gurobi.GRBConstraintManager;
import vroom.common.utilities.gurobi.GRBUtilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.lp.SolverStatus;
import vroom.optimization.pl.IVRPSolver;
import vroom.optimization.pl.symphony.vrp.CVRPSymphonySolver;

/**
 * <code>CVRPSolverTest</code> is a test class for {@link CVRPSolverCapOld}
 * <p>
 * Creation date: Jul 6, 2010 - 3:43:31 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CVRPSolverTest {

	private final static File INSTANCES_DIRECTORY = new File(
			"../Instances/cvrp/christodifes-mingozzi-toth");

	public static final LoggerHelper LOGGER = LoggerHelper
			.getLogger(CVRPSolverTest.class.getSimpleName());

	protected static final boolean COMPARE_CAP = false;

	/** Time limit (in sec) */
	protected static int sTimeLimit = 60;

	protected List<IVRPInstance> mInstances;

	protected StatCollector mStats;

	public static Label<?>[] LABELS = new Label<?>[] {
			new Label<String>("instance", String.class),
			new Label<Integer>("size", Integer.class),
			new Label<Double>("capacity", Double.class),
			new Label<String>("solver", String.class),
			new Label<Double>("obj", Double.class),
			new Label<Integer>("time", Integer.class),
			new Label<String>("status", String.class) };

	private final CVRPCuttingPlaneSolver mCutSolver, mCutInjSolver;
	private final CVRPSymphonySolver mSymSolver;
	private final CVRPCapBasedSolver mCapSolver;

	public CVRPSolverTest() throws GRBException {
		preinit();
		mInstances = readInstances();
		mStats = newStatCollector();
		mCutSolver = new CVRPCuttingPlaneSolver(false, false);
		mCutInjSolver = new CVRPCuttingPlaneSolver(false, true);
		mSymSolver = new CVRPSymphonySolver();
		mCapSolver = new CVRPCapBasedSolver(false);
	}

	protected StatCollector newStatCollector() {
		return new StatCollector("CVRPSolverTest", LABELS);
	}

	protected void preinit() {

	}

	protected List<IVRPInstance> readInstances() {
		// NovoaPersistenceHelper reader = new NovoaPersistenceHelper();
		// try {
		// return reader.readInstances(new int[]{20}, new int[]{0,1}, new
		// int[]{1}, null);
		// // return Collections.singletonList(reader.readInstance(8, 1, 2, 1));
		// } catch (IOException e) {
		// e.printStackTrace();
		// return new LinkedList<DynamicInstance>();
		// }

		ChristofidesPersistenceHelper reader = new ChristofidesPersistenceHelper();
		String[] children = INSTANCES_DIRECTORY.list();
		List<IVRPInstance> instances = new LinkedList<IVRPInstance>();
		if (children == null) {
			// Either dir does not exist or is not a directory
			throw new IllegalStateException("Cannot find instances");
		} else {
			for (String file : children) {
				if (!file.contains("info") && !file.equals(".svn")) {
					try {
						instances.add(reader.readInstance(new File(
								INSTANCES_DIRECTORY.getAbsolutePath()
										+ File.separator + file)));
					} catch (Exception e) {
						System.err.printf("Error when reading %s : %s", file,
								e.getMessage());
					}
				}
			}
		}

		return instances;
	}

	public void run() {
		for (IVRPInstance instance : mInstances) {
			LOGGER.info("=========================================================");
			LOGGER.info(" Solving instance " + instance.getName());
			LOGGER.info("=========================================================");

			try {
				double objBC = solveInstanceCutPlanes(instance, true);
				if (objBC != Double.POSITIVE_INFINITY) {
					optimalFound(instance, objBC);
				} else {
					timeLimitReached(instance);
				}
			} catch (GRBException e) {
				LOGGER.exception("CVRPSolverTest.run (instance %s)", e,
						instance.getName());
			}

			LOGGER.info("=========================================================");
		}

		LOGGER.info("FINISHED");
	}

	protected void timeLimitReached(IVRPInstance instance) {
	}

	protected void optimalFound(IVRPInstance instance, double objBC) {
	}

	public static void main(String[] args) {
		LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_LOW_DEBUG,
				LoggerHelper.LEVEL_LOW_DEBUG, true);
		CVRPSolverBase.LOGGER.setLevel(LoggerHelper.LEVEL_DEBUG);
		GRBConstraintManager.LOGGER.setLevel(LoggerHelper.LEVEL_DEBUG);

		LOGGER.info("java.library.path");
		LOGGER.info(System.getProperty("java.library.path"));

		CVRPSolverTest test;
		try {
			test = new CVRPSolverTest();
			test.run();
		} catch (GRBException e) {
			e.printStackTrace();
		}
	}

	protected void storeStats(String name, SolverStatus status,
			IVRPInstance instance, IVRPSolver solver) {
		double obj = Double.NaN;
		if (status == SolverStatus.OPTIMAL) {
			obj = solver.getObjectiveValue();
		}
		mStats.collect(instance.getName(), instance.getRequestCount(), instance
				.getFleet().getVehicle().getCapacity(), name, obj,
				(int) solver.getSolveTime(), status);
	}

	/**
	 * Solve a {@link IVRPInstance} with the {@link CVRPBranchCutSolverBad}
	 * solver
	 * 
	 * @param instance
	 * @return the value of the objective function
	 * @throws GRBException
	 */
	public double solveInstanceCutPlanes(IVRPInstance instance,
			boolean solInjection) throws GRBException {
		LOGGER.info(" #### Cutting Planes Solver %s ####",
				solInjection ? "with solution injection" : "");
		CVRPCuttingPlaneSolver solver = solInjection ? mCutInjSolver
				: mCutSolver;

		solver.readInstance(instance);
		solver.setTimeLimit(sTimeLimit);

		// solver.printModel();
		// System.exit(1);
		SolverStatus status = solver.solve();
		String name = solInjection ? "cvrp_cut_inj" : "cvrp_cut";
		storeStats(name, status, instance, solver);
		switch (status) {
		case OPTIMAL:
			if (!solver.isSolutionFeasible()) {
				LOGGER.error("Solution is not feasible : %s",
						solver.getSolution());
				System.exit(1);
			} else {
				solver.printSolution(true);
			}
			break;
		case TIME_LIMIT:
			LOGGER.info("Solution: %s", solver.getSolution());
			return Double.POSITIVE_INFINITY;
		default:
			LOGGER.error("Solver :"
					+ GRBUtilities.solverStatusString(solver.getModel()));
			return Double.POSITIVE_INFINITY;
		}
		double obj = solver.getModel().get(DoubleAttr.ObjVal);

		solver = null;
		Runtime.getRuntime().gc();
		return obj;
	}

	public double solveInstanceSymphony(IVRPInstance instance)
			throws InterruptedException, IOException {
		LOGGER.info(" #### Symphony Solver ####");

		mSymSolver.readInstance(instance);
		mSymSolver.setTimeLimit(sTimeLimit);

		// solver.printModel();
		// System.exit(1);
		SolverStatus status = mSymSolver.solve();
		String name = "cvrp_symph";
		storeStats(name, status, instance, mSymSolver);
		switch (status) {
		case OPTIMAL:
			if (!mSymSolver.isSolutionFeasible()) {
				LOGGER.error("Solution is not feasible : %s",
						mSymSolver.getSolution());
				System.exit(1);
			} else {
				mSymSolver.printSolution(true);
			}
			break;
		case TIME_LIMIT:
			LOGGER.info("Solution: %s", mSymSolver.getSolution());
			return Double.POSITIVE_INFINITY;
		default:
			LOGGER.error("Status : %s", status);
			return Double.POSITIVE_INFINITY;
		}
		double obj = mSymSolver.getObjectiveValue();

		Runtime.getRuntime().gc();
		return obj;
	}

	/**
	 * Solve a {@link IVRPInstance} with the {@link CVRPSolverCapOld} solver
	 * 
	 * @param instance
	 * @return the value of the objective function
	 * @throws GRBException
	 */
	public double solveInstanceCap(IVRPInstance instance) throws GRBException {
		LOGGER.info(" #### Capacity Based Solver ####");

		mCapSolver.setTimeLimit(sTimeLimit);

		mCapSolver.readInstance(instance);

		// solver.printModel();

		SolverStatus status = mCapSolver.solve();
		storeStats("cvrp_capBased", status, instance, mCapSolver);
		switch (status) {
		case OPTIMAL:
			mCapSolver.printSolution(true);
			break;
		default:
			LOGGER.error("Solver :"
					+ GRBUtilities.solverStatusString(mCapSolver.getModel()));
			return Double.POSITIVE_INFINITY;
		}
		return mCapSolver.getModel().get(DoubleAttr.ObjVal);
	}
}
