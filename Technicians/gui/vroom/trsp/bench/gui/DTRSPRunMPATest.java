package vroom.trsp.bench.gui;

import java.io.File;
import java.util.List;
import java.util.Map;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.modeling.io.DynamicPersistenceHelper;
import vroom.common.utilities.IntegerSet;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.trsp.bench.TRSPBench;
import vroom.trsp.bench.mpa.DTRSPRunMPA;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPDistance;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.io.DynamicTRSPPersistenceHelper;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>DTRSPRunMPATest</code>
 * <p>
 * Creation date: Mar 28, 2012 - 3:52:28 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPRunMPATest {
    public static void main(String[] args) {
        // String configFile = "./config/bench/bench_dvrptw_lackner_pBiALNS.cfg";
        // String instanceFile = "C201.txt";

        int dp = 50;
        // String configFile = "./config/bench/bench_dtrsp_mpa_25crew.cfg";
        // String instanceFile = "R104.100_25-5-5-5.txt";
        String configFile = "./config/bench/bench_dvrptw_lackner_MPA.cfg";
        String instanceFile = "RC102.txt";
        // int dp = 10;
        // String instanceFile = "C107.100_25-5-5-5.txt";
        // String instanceFile = "RC101.100_25-5-5-5.txt";
        // String instanceFile = "C201.100_25-5-5-5.txt";

        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_LOW_DEBUG, true);
        TRSPLogging.getRunLogger().setLevel(LoggerHelper.LEVEL_DEBUG);
        Logging.getSetupLogger().setLevel(LoggerHelper.LEVEL_INFO);
        MSALogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_WARN);
        MSALogging.getProcedureLogger().setLevel(LoggerHelper.LEVEL_DEBUG);
        MSALogging.getSimulationLogger().setLevel(LoggerHelper.LEVEL_INFO);
        TRSPLogging.getSimulationLogger().setLevel(LoggerHelper.LEVEL_INFO);
        TRSPLogging.getOptimizationLogger().setLevel(LoggerHelper.LEVEL_WARN);

        // DTRSPScenarioOptimizer.setCheckSolutionAfterMove(true);
        // NeighborhoodBase.setCheckSolutionAfterMove(true);

        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File(configFile));
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.main", e);
        }

        // Set the simulation speed
        // params.set(TRSPGlobalParameters.RUN_SIM_SPEED, 10d);
        params.set(TRSPGlobalParameters.SC_ENABLED, false);

        // Fix the number of threads
        params.set(
                TRSPGlobalParameters.THREAD_COUNT,
                Math.min(params.getThreadCount(),
                        Math.max(Runtime.getRuntime().availableProcessors() - 1, 1)));

        // Setup the TRSPBench
        TRSPBench.setup(params, true, "TRSPRunBase_test");

        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(params
                .get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER));
        TRSPInstance instance = null;
        String com = "";
        try {
            instance = reader.readInstance(
                    new File(String.format("%s/%s",
                            params.get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER), instanceFile)),
                    false);
            if (params.isDynamic()) {
                Map<String, List<File>> rdFileMapping = DynamicPersistenceHelper.getRelDateFiles(
                        params.get(TRSPGlobalParameters.RUN_REL_DATE_FOLDER), new int[] { dp });
                List<File> rdFiles = rdFileMapping.get(instance.getName());
                if (rdFiles != null && !rdFiles.isEmpty()) {
                    DynamicTRSPPersistenceHelper.readRelDates(instance, rdFiles.get(0),
                            params.isCVRPTW());
                    com = rdFiles.get(0).getName();
                }
            }

        } catch (IllegalArgumentException e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.main", e);
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("DTRSPRunMPA.main", e);
        }

        if (instance != null) {
            DTRSPRunMPA run = new DTRSPRunMPA(0, instance, params, null, 0, com);

            TRSPSimFrame frame = new TRSPSimFrame();
            frame.initialize(run);
            frame.pack();
            frame.setVisible(true);
            frame.startUpdateDaemon();

            try {
                run.call();
            } catch (Exception e1) {
                TRSPLogging.getBaseLogger().exception("DTRSPRunMPATest.main", e1);
            }

            Logging.awaitLogging(60000);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                TRSPLogging.getBaseLogger().exception("DTRSPRunMPA.main", e);
            }

            TRSPLogging.getRunLogger().info("=========================================");
            TRSPLogging.getRunLogger().info("Final status: %s", run.getSolver().getStatus());
            if (run.getSolver().getStatus() == ProcedureStatus.TERMINATED) {
                TRSPWorkingTime wt = new TRSPWorkingTime();
                TRSPDistance dist = new TRSPDistance();
                TRSPLogging.getRunLogger().info("Final solution : wt:%7.1f dist:%7.1f cd:%s",
                        wt.evaluateSolution(run.getFinalSolution(), true, true),
                        dist.evaluateSolution(run.getFinalSolution(), true, false),
                        run.getFinalSolution().getCostDelegate());
                TRSPLogging.getRunLogger().info("Static solution: wt:%7.1f dist:%7.1f cd:%s",
                        wt.evaluateSolution(run.getStaticSolution(), true, true),
                        dist.evaluateSolution(run.getStaticSolution(), true, false),
                        run.getFinalSolution().getCostDelegate());

                TRSPLogging.getRunLogger().info("Check sol      : %s",
                        TRSPSolutionChecker.INSTANCE.checkSolution(run.getFinalSolution()));

                TRSPLogging.getRunLogger().info("-----------------------------------------");
                TRSPLogging.getRunLogger().info("Solution detail:");
                IntegerSet served = new IntegerSet(instance.getMaxId());
                IntegerSet unserved = new IntegerSet(instance.getRequestsId());
                for (TRSPTour t : run.getSolver().getFinalSolution()) {
                    TRSPLogging.getRunLogger().info(" %s", t);
                    for (Integer n : t)
                        if (instance.isRequest(n)) {
                            served.add(n);
                            unserved.remove(n);
                        }
                }
                TRSPLogging.getRunLogger().info("-----------------------------------------");
                TRSPLogging.getRunLogger().info("Released requests: %-3s %s",
                        instance.getReleasedRequests().size(),
                        Utilities.toShortString(instance.getReleasedRequests()));
                TRSPLogging.getRunLogger().info("Served   requests: %-3s %s", served.size(),
                        Utilities.toShortString(served));
                TRSPLogging.getRunLogger().info("Unserved requests: %-3s %s", unserved.size(),
                        Utilities.toShortString(unserved));
                TRSPLogging.getRunLogger().info("Rejected requests: %-3s %s",
                        instance.getSimulator().getRejectedRequests().size(),
                        Utilities.toShortString(instance.getSimulator().getRejectedRequests()));
                TRSPLogging.getRunLogger().info("=========================================");
            }
            run.dispose();
            System.gc();
        } else
            System.exit(0);
    }
}
