/**
 * 
 */
package vroom.optimization.alns;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vroom.common.heuristics.alns.DiversifiedPool;
import vroom.common.modeling.io.DynamicPersistenceHelper;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.trsp.ALNSSCSolver;
import vroom.trsp.bench.TRSPBench;
import vroom.trsp.bench.TRSPRunBase;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.io.DynamicTRSPPersistenceHelper;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.optimization.constructive.TRSPConstructiveHeuristic;
import vroom.trsp.util.BrokenPairsDistance;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * JAVADOC <code>DiversifiedPoolTest</code>
 * <p>
 * Creation date: Feb 29, 2012 - 1:28:40 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DiversifiedPoolTest {

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {

        String configFile = "./config/bench/bench_cvrptw_palnssc.cfg";
        String instanceFile = "C104.txt";
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, true);
        TRSPLogging.getRunLogger().setLevel(LoggerHelper.LEVEL_DEBUG);
        Logging.getSetupLogger().setLevel(LoggerHelper.LEVEL_INFO);

        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File(configFile));
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.main", e);
            System.exit(1);
        }
        TRSPBench.setup(params, true, "TRSPRunBase_test");

        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(params
                .get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER));
        TRSPInstance instance = null;
        String com = "";
        try {
            instance = reader.readInstance(
                    new File(String.format("%s/%s",
                            params.get(TRSPGlobalParameters.RUN_INSTANCE_FOLDER), instanceFile)),
                    params.isCVRPTW());
            if (params.isDynamic()) {
                Map<String, List<File>> rdFileMapping = DynamicPersistenceHelper.getRelDateFiles(
                        params.get(TRSPGlobalParameters.RUN_REL_DATE_FOLDER), new int[] { 10 });
                List<File> rdFiles = rdFileMapping.get(instance.getName());
                if (rdFiles != null && !rdFiles.isEmpty()) {
                    DynamicTRSPPersistenceHelper.readRelDates(instance, rdFiles.get(0),
                            params.isCVRPTW());
                    com = rdFiles.get(0).getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        TRSPRunBase run = new TRSPRunBase(null, instance, params, null, 0, com);
        DiversifiedPool<TRSPSolution> pool = new DiversifiedPool<>(new BrokenPairsDistance(),
                OptimizationSense.MINIMIZATION, 4);
        ((ALNSSCSolver) run.getSolver()).initialization();
        TRSPConstructiveHeuristic init = ((ALNSSCSolver) run.getSolver()).getInit();

        System.out.println("Generating solutions");
        ArrayList<TRSPSolution> solutions = new ArrayList<>(1000);
        for (int i = 0; i < 100; i++) {
            init.call();
            solutions.add(init.getSolution());
        }
        System.out.println("---------------");

        Stopwatch timer = new Stopwatch();
        timer.start();
        for (TRSPSolution s : solutions) {
            System.out.println(" New  Solution: " + s);
            boolean added = pool.add(s, false);
            System.out.println(pool);
            System.out.println(" Added : " + added);
            System.out.println(" Best Solution: " + pool.getBest().getObjectiveValue());
            // System.out.println(" Check pool   : " + pool.checkPool());
            System.out.println();
        }
        timer.stop();
        System.out.println("Total time : " + timer.readTimeString());
        System.out.println("Time per it: " + timer.readTimeS());

    }

}
