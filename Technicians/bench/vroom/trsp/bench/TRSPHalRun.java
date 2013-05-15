/**
 * 
 */
package vroom.trsp.bench;

import java.io.File;
import java.util.List;
import java.util.Map;

import vroom.common.modeling.io.DynamicPersistenceHelper;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.params.ParametersFilePersistenceDelegate;
import vroom.trsp.ALNSSCSolver;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.io.DynamicTRSPPersistenceHelper;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPHalRun</code> is the main type for HAL runs.
 * <p>
 * Creation date: Dec 27, 2011 - 5:49:18 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPHalRun {

    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_FATAL, LoggerHelper.LEVEL_FATAL, true);

        // printAllArguments();
        // System.exit(0);

        if (args.length < 1) {
            System.err.println("Required arguments: instanceFile");
            System.exit(1);
        }

        // Arguments:
        // $0 instance file
        String instanceFile = args[0].replace("../..", "");

        // $1 config file
        String configFile = "./config/hal/hal_defaults.cfg";

        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File(configFile));
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("TRSPRunBase.main", e);
        }

        // Override parameters
        for (int i = 1; i < args.length; i++) {
            if (!args[i].contains("="))
                continue;// Ignore
            try {
                String[] p = args[i].split("=");
                ParameterKey<?> key = params.getRegisteredKey(p[0]);
                params.setNoCheck(key, ParametersFilePersistenceDelegate.castProperty(key, p[1]));
                TRSPLogging.getRunLogger().info("TRSPBench.main : Set parameter %s=%s", key, p[1]);
            } catch (Exception e1) {
                e1.printStackTrace();
                System.exit(1);
            }
        }
        // ------------------------------------------------------------

        String bksFile = params.get(TRSPGlobalParameters.RUN_BKS_FILE);
        BestKnownSolutions bks = new BestKnownSolutions(bksFile);

        // Read instance
        TRSPInstance instance = null;
        try {
            instance = readInstance(instanceFile, params);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(2);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

        // Run the instance
        TRSPRunBase run = run(instance, params);

        // Print run stats
        double bksVal = bks.getBKS(instance.getName());
        double objVal = run.getSolver().getFinalSolution().getObjectiveValue();
        double gapBKS = bks.getGapToBKS(instance.getName(), objVal, OptimizationSense.MINIMIZATION);
        if (ALNSSCSolver.class.isAssignableFrom(run.getSolver().getClass())) {
            ALNSSCSolver solver = (ALNSSCSolver) run.getSolver();
            System.out.printf("%s;%s\n", solver.getALNS().getTimer().readTimeS(), gapBKS);
        } else {
            System.err.println("Unsupported solver type: "
                    + run.getSolver().getClass().getSimpleName());
        }
    }

    public static TRSPInstance readInstance(String instanceFile, TRSPGlobalParameters params)
            throws IllegalArgumentException, Exception {
        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(instanceFile);
        TRSPInstance instance = reader.readInstance(new File(instanceFile), params.isCVRPTW());
        if (params.isDynamic()) {
            Map<String, List<File>> rdFileMapping = DynamicPersistenceHelper.getRelDateFiles(
                    params.get(TRSPGlobalParameters.RUN_REL_DATE_FOLDER), new int[] { 10 });
            List<File> rdFiles = rdFileMapping.get(instance.getName());
            if (rdFiles != null && !rdFiles.isEmpty()) {
                DynamicTRSPPersistenceHelper.readRelDates(instance, rdFiles.get(0),
                        params.isCVRPTW());
            }
        }
        return instance;
    }

    public static TRSPRunBase run(TRSPInstance instance, TRSPGlobalParameters params) {
        TRSPRunBase run = new TRSPRunBase(null, instance, params, null, 0, "hal");
        try {
            run.call();
        } catch (Exception e) {
            TRSPLogging.getBaseLogger().exception("TRSPHalRun.run", e);
        }
        return run;
    }

    public static void printAllArguments() {
        TRSPGlobalParameters.printAllKeys(TRSPGlobalParameters.class, true);
    }
}
