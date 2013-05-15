/**
 * 
 */
package vroom.optimization.online.jmsa.benchmarking;

import java.io.IOException;
import java.util.LinkedList;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.benchmarking.NovoaBenchmarking.NovoaBenchmarkSettings;
import vroom.optimization.online.jmsa.vrp.vrpsd.VRPSDSmartConsensusDepot;

/**
 * <code>SubsetBenchmarking</code>
 * <p>
 * Creation date: Oct 12, 2010 - 2:56:35 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NovoaSubsetBenchmarking {

    /**
     * Getter for this class logger
     * 
     * @return the logger associated with this class
     */
    public static LoggerHelper getLogger() {
        return LoggerHelper.getLogger(NovoaSubsetBenchmarking.class);
    }

    public static void main(String[] args) {
        LinkedList<NovoaBenchmarkSettings> settings = new LinkedList<NovoaBenchmarking.NovoaBenchmarkSettings>();

        String comment = "";

        MSAGlobalParameters p = NovoaBenchmarking.getDefaultParameters();
        p.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS, VRPSDSmartConsensusDepot.class);
        settings.add(new NovoaBenchmarkSettings(p, "SmartCDepot"));

        // p = NovoaBenchmarking.getDefaultParameters();
        // p.set(MSAGlobalParameters.SOLUTION_BUILDER_CLASS, VRPSDSmartConsensus.class);
        // settings.add(new NovoaBenchmarkSettings(p, "SmartC"));

        NovoaBenchmarking benchmark = null;

        int maxThreads = Runtime.getRuntime().availableProcessors();

        if (args.length >= 2) {
            String file = args[0];
            comment = args[1];

            if (args.length >= 3) {
                maxThreads = Math.min(maxThreads, Integer.valueOf(args[2]));
            }

            try {
                benchmark = new NovoaBenchmarking(file, comment, settings);
                benchmark.setThreadCount(maxThreads);
            } catch (IOException e) {
                getLogger().exception("NovoaSubsetBenchmarking.main", e);
            }
        } else {
            System.err
                    .println("Wrong number of arguments, usage: main benchFile comment [maxThreads]");
            System.exit(0);
        }

        benchmark.run();
    }
}
