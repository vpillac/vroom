/**
 * 
 */
package vroom.optimization.pl.gurobi;

import gurobi.GRBException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;

import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.io.NovoaPersistenceHelper;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.StatCollector;
import vroom.common.utilities.gurobi.GRBConstraintManager;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.lp.SolverStatus;
import vroom.optimization.pl.IVRPSolver;

/**
 * <code>CVRPSolverNovoa</code> is a test class for {@link CVRPSolverCapOld}
 * <p>
 * Creation date: Jul 6, 2010 - 3:43:31 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NovoaCVRPBench extends CVRPSolverTest {

	private BestKnownSolutions mBKS;

	static boolean IGNORE_OPTIMAL = true;

	public static void main(String[] args) {
		Level level = LoggerHelper.LEVEL_INFO;
		LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_INFO,
				LoggerHelper.LEVEL_INFO, true);
		CVRPSolverBase.LOGGER.setLevel(level);
		GRBConstraintManager.LOGGER.setLevel(level);

		LOGGER.info("java.library.path");
		LOGGER.info(System.getProperty("java.library.path"));

		sTimeLimit = 120;

		NovoaCVRPBench test;
		try {
			test = new NovoaCVRPBench();
			test.run();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public NovoaCVRPBench() throws GRBException {
		super();
		mStats.setFile(new File("./benchmarks/novoa_cutting_planes2t.csv"),
				true, false);
		StatCollector.sCVSSeparator = ";";
		StatCollector.sDecimalSeparator = ',';
	}

	@Override
	protected void preinit() {
		mBKS = new BestKnownSolutions("../Instances/vrpsd/novoa.sol");
	}

	@Override
	protected List<IVRPInstance> readInstances() {
		NovoaPersistenceHelper reader = new NovoaPersistenceHelper();
		try {
			List<DynamicInstance> instances = reader.readInstances(
					new int[] { 5 }, new int[] { 0, 1 }, new int[] { 1 }, null);
			// new int[]{30}, new int[]{1}, new int[]{1}, null);

			LinkedList<IVRPInstance> filtered = new LinkedList<IVRPInstance>();
			for (DynamicInstance i : instances) {
				if (IGNORE_OPTIMAL
						|| mBKS.getBKS(i.getName()) == null
						|| !mBKS.isOptimal(i.getName())) {
					filtered.add(i);
				} else {
					LOGGER.info(
							"Optimal solution already available for instance %s (%s)",
							i.getName(), mBKS.getBKS(i.getName()));
				}
			}
			return filtered;
		} catch (IOException e) {
			LOGGER.exception("NovoaCVRPBench.readInstances", e);
			return new LinkedList<IVRPInstance>();
		}
	}

	@Override
	public void run() {
		for (IVRPInstance instance : mInstances) {
			LOGGER.info("=========================================================");
			LOGGER.info(" Solving instance " + instance.getName());
			LOGGER.info("=========================================================");

			try {
				for (IVRPRequest r : instance.getRequests()) {
					LOGGER.info("R: %s (%s)", r, r.getDemand());
				}
				double objSy = solveInstanceSymphony(instance);
				if (objSy != Double.POSITIVE_INFINITY) {
					optimalFound(instance, objSy);
				} else {
					timeLimitReached(instance);
				}

				double objCP = solveInstanceCutPlanes(instance, false);
				if (objCP != Double.POSITIVE_INFINITY) {
					optimalFound(instance, objCP);
				} else {
					timeLimitReached(instance);
				}
				double objCPINS = solveInstanceCutPlanes(instance, true);
				if (objCPINS != Double.POSITIVE_INFINITY) {
					optimalFound(instance, objCPINS);
				} else {
					timeLimitReached(instance);
				}
				double objCap = solveInstanceCap(instance);
				if (objCap != Double.POSITIVE_INFINITY) {
					optimalFound(instance, objCap);
				} else {
					timeLimitReached(instance);
				}

				if (Math.abs(objCap - objCP) > 1e-4
						|| Math.abs(objCap - objCPINS) > 1e-4
						|| Math.abs(objCP - objCPINS) > 1e-4
						|| Math.abs(objCP - objSy) > 1e-4) {
					throw new IllegalStateException(
							String.format(
									"Solvers returned different values: (cap:%s cut:%s cutInj:%s symph:%s)",
									objCap, objCP, objCPINS, objSy));
				}
			} catch (GRBException e) {
				LOGGER.exception("NovoaCVRPBench.run (instance %s)", e,
						instance.getName());
			} catch (InterruptedException e) {
				LOGGER.exception("NovoaCVRPBench.run (instance %s)", e,
						instance.getName());
			} catch (IOException e) {
				LOGGER.exception("NovoaCVRPBench.run (instance %s)", e,
						instance.getName());
			}

			Runtime.getRuntime().gc();

			LOGGER.info("=========================================================");
		}

		LOGGER.info("FINISHED");
	}

	@Override
	protected void optimalFound(IVRPInstance instance, double objBC) {
		if (IGNORE_OPTIMAL) {
			return;
		}
		super.optimalFound(instance, objBC);
		mBKS.setBestKnownSolution(instance.getName(), objBC, true, null, null, null);
		try {
			mBKS.save("Solutions found with B&C");
		} catch (FileNotFoundException e) {
			LOGGER.exception("NovoaCVRPBench.optimalFound (saving bks)", e);
		} catch (IOException e) {
			LOGGER.exception("NovoaCVRPBench.optimalFound (saving bks)", e);
		}
	}

	@Override
	protected void storeStats(String name, SolverStatus status,
			IVRPInstance instance, IVRPSolver solver) {
		if (IGNORE_OPTIMAL) {
			return;
		}
		super.storeStats(name, status, instance, solver);
	}
}
