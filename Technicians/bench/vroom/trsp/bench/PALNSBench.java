/**
 * 
 */
package vroom.trsp.bench;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Level;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.params.ParameterExperimentDesign;
import vroom.common.utilities.params.ParameterExperimentDesign.ExperimentParameterSetting;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>PALNSBench</code>
 * <p>
 * Creation date: Jun 21, 2012 - 2:42:37 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class PALNSBench {

    public static String  sConfigFile = "config/bench/bench_cvrptw_palnssc.cfg";
    public static boolean sSCEnabled  = false;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Accepts exactly two arguments: trspParamsFile expeConfigFile");
            System.exit(1);
        }

        Level benchmarkLevel = LoggerHelper.LEVEL_INFO;
        Level algoLevel = LoggerHelper.LEVEL_ERROR;
        String fileCom = "pALNS";
        boolean noStat = false;
        TRSPBench.setupLoggers(benchmarkLevel, algoLevel, null);
        TRSPGlobalParameters params = new TRSPGlobalParameters();
        ParameterExperimentDesign<TRSPGlobalParameters> expe = new ParameterExperimentDesign<TRSPGlobalParameters>(
                params);
        try {
            // Read configuration file
            // ------------------------------------------------------------
            params.loadParameters(new File(args[0]));
            params.set(TRSPGlobalParameters.SC_ENABLED, sSCEnabled);

            // Experiment design
            // ------------------------------------------------------------
            File expeFile = new File(args[1]);
            expe.load(expeFile);
            fileCom = expeFile.getName();
            // ------------------------------------------------------------

        } catch (Exception e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        // ------------------------------------------------------------

        TRSPBench.setup(params, noStat, fileCom);
        TRSPBench.getInstance().createRuns();

        ArrayList<TRSPRunBase> runs = new ArrayList<>(TRSPBench.getInstance().getRuns());
        TRSPBench.getInstance().getRuns().clear();

        int runid = 0;
        for (ExperimentParameterSetting<TRSPGlobalParameters> setting : expe.getExperiments()) {
            for (TRSPRunBase r : runs) {
                TRSPBench.getInstance().getRuns().add(r.clone(runid++, setting));
            }
        }

        TRSPLogging.getRunLogger().info("Experiment Parameters: " + expe.getParameterKeys());
        TRSPBench.getInstance().run();
        TRSPLogging.getRunLogger().info("Experiment Parameters: " + expe.getParameterKeys());
    }
}
