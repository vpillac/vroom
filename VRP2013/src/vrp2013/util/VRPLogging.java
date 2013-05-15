package vrp2013.util;

import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;

/**
 * The class <code>Logging</code> provides a default logger for the VRP 2013 examples
 * <p>
 * Creation date: 30/04/2013 - 4:48:05 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class VRPLogging {
    /** The optimization logger */
    public static final String OPT_LOGGER   = "vrp2013.opt";
    /** The benchmarking logger */
    public static final String BENCH_LOGGER = "vrp2013.bench";

    /**
     * Optimization logger
     * 
     * @return the optimization logger
     */
    public final static LoggerHelper getOptLogger() {
        return LoggerHelper.getLogger(OPT_LOGGER);
    }

    /**
     * Benchmarking logger
     * 
     * @return the benchmarking logger
     */
    public final static LoggerHelper getBenchLogger() {
        return LoggerHelper.getLogger(BENCH_LOGGER);
    }

    /**
     * Log the results from an optimization step
     * 
     * @param optName
     * @param changed
     * @param sw
     * @param solution
     */
    public static void logOptResults(String optName, boolean changed, Stopwatch sw,
            VRPSolution solution) {

        VRPLogging.getOptLogger().lowDebug("[%9s] Time: %s (%s) - obj=%.2f", optName,
                sw.readTimeString(3, true, false), changed ? "  changed" : "unchanged",
                solution.getCost());

    }
}
