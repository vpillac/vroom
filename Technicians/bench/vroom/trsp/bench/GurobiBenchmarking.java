/**
 *
 */
package vroom.trsp.bench;

import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntParam;
import gurobi.GRB.StringAttr;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.gurobi.GRBEnvProvider;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.trsp.optimization.matheuristic.TRSPGRBStatCollector;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>GurobiBenchmarking</code> is a benchmarking class to run a set of models and collect stats
 * <p>
 * Creation date: Aug 25, 2011 - 4:48:45 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GurobiBenchmarking {

    public static final String   DETAILED_STATS = "results/gurobi/GRBBench_%s-Detail.csv";
    public static final String   TARGET_STATS   = "results/gurobi/GRBBench_%s-Target.csv";

    public static final String[] MODEL_EXT      = new String[] { ".mps.bz2", ".lp" };

    /**
     * main method
     * 
     * @param args
     *            [instanceDirectory,gurobiConfigFile]
     */
    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_INFO, LoggerHelper.LEVEL_INFO, false);

        if (args.length != 2) {
            TRSPLogging.getRunLogger().info("Arguments: instanceDirectory gurobiConfigFile");
            TRSPLogging.getRunLogger().info("Wrong number of arguments: " + args.length);
            for (String a : args) {
                TRSPLogging.getRunLogger().info(a);
            }
            Logging.awaitLogging(60000);
            System.exit(1);
        }

        try {
            GRBEnvProvider.getEnvironment().readParams(args[1]);
            GRBEnvProvider.getEnvironment().set(IntParam.OutputFlag, 0);
        } catch (GRBException e1) {
            e1.printStackTrace();
            Logging.awaitLogging(60000);
            System.exit(1);
        }

        String detStatFile = String.format(DETAILED_STATS, Utilities.Time.getDateString());
        String ttStatFile = String.format(TARGET_STATS, Utilities.Time.getDateString());
        String comment = String.format("Gurobi Benchmarks\nInstances: %s\nConfig: %s", args[0], args[1]);

        File dir = new File(args[0]);
        File[] instances = dir.listFiles();
        Arrays.sort(instances);

        LinkedList<File[]> runs = new LinkedList<File[]>();
        for (File file : instances) {
            for (String ext : MODEL_EXT) {
                if (file.getName().endsWith(ext)) {
                    File solFile = new File(dir, file.getName().replace(ext, ".mst"));
                    runs.add(new File[] { file, solFile });
                    break;
                }
            }
        }

        for (File[] run : runs) {
            double startObj = Double.POSITIVE_INFINITY;
            if (run[1].exists()) {
                // Read the initial solution value
                try {
                    GRBModel model = GurobiModelSolver.readModel(run[0].getAbsolutePath(), run[1].getAbsolutePath(),
                            false);
                    GRBVar vars[] = model.getVars();
                    double starts[] = model.get(DoubleAttr.Start, vars);
                    double costs[] = model.get(DoubleAttr.Obj, vars);
                    startObj = 0;
                    for (int i = 0; i < starts.length; i++) {
                        if (starts[i] > 0)
                            startObj += costs[i] * starts[i];
                    }

                } catch (GRBException e) {
                    TRSPLogging.getRunLogger().exception("GurobiBenchmarking.main", e);
                }
            }

            runModel(run[0], run[1], detStatFile, comment, ttStatFile, startObj);
            if (run[1].exists())
                runModel(run[0], null, detStatFile, comment, ttStatFile, startObj);
        }

        Logging.awaitLogging(60000);
        System.exit(0);
    }

    /**
     * Run a model stored in a file
     * 
     * @param modelFile
     *            the model file
     * @param solFile
     *            the initial solution file
     * @param detStatFile
     *            the path of the detailed stats path
     * @param statComment
     *            a comment for the detailed stats file
     * @param ttStatFile
     *            the collector for time to target values
     */
    public static void runModel(File modelFile, File solFile, String detStatFile, String statComment,
            String ttStatFile, double startObj) {
        GRBModel model = null;
        try {
            String modelName = getModelName(modelFile.getName());
            TRSPLogging.getRunLogger().info("Running model %s (initial solution: %s)", modelFile, solFile);

            model = GurobiModelSolver.readModel(modelFile.getAbsolutePath(),
                    solFile != null && solFile.exists() ? solFile.getAbsolutePath() : null, false);
            model.set(StringAttr.ModelName, modelName);

            TRSPGRBStatCollector bench = new TRSPGRBStatCollector(model, statComment, detStatFile, ttStatFile);

            TRSPLogging.getRunLogger().info(" Starting the optimizer");
            bench.call();

            bench.collectStats();

        } catch (Exception e) {
            TRSPLogging.getRunLogger().exception("GurobiBenchmarking.runModel", e);
        } finally {
            if (model != null)
                model.dispose();
        }

    }

    /**
     * Gets a model's name from its file name
     * 
     * @param fileName
     *            the name of the file containing the model
     * @return the name of the model
     */
    public static String getModelName(String fileName) {
        for (String ext : MODEL_EXT) {
            if (fileName.endsWith(ext)) {
                return fileName.replace(ext, "");
            }
        }
        return fileName;
    }
}
