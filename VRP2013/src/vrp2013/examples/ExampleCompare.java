/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */
/**
 * 
 */
package vrp2013.examples;

import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.OptimizationSense;
import vrp2013.util.RoutePoolFactory;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * The class <code>ExampleCompare</code> runs all the examples for a single instance and logs the results.
 * <p>
 * Creation date: 13/05/2013 - 1:05:49 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class ExampleCompare {

    /**
     * JAVADOC
     * 
     * @param args
     * @author vpillac
     */
    public static void main(String[] args) {
        // Setup the logging system
        // The first argument is the default logger level
        // The second is the filtering level of the appender (i.e. console output)
        // The last can be set to true to do the logging in a separate thread, or false to do it in the main thread
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_LOW_DEBUG, true);
        VRPLogging.getBenchLogger().setLevel(LoggerHelper.LEVEL_INFO);
        // Sets the logging level for the VRP examples logger
        VRPLogging.getOptLogger().setLevel(LoggerHelper.LEVEL_WARN);

        StaticInstance instance = VRPUtilities.pickInstance();

        ExampleBase[] examples = new ExampleBase[] { //
                new ExampleCW(instance, VRPUtilities.getBKS()), //
                new ExampleVND(instance, VRPUtilities.getBKS()), //
                new ExampleGRASP(instance, VRPUtilities.getBKS(),
                        RoutePoolFactory.newHashPoolSet(instance)), //
                new ExampleGRASPHC(instance, VRPUtilities.getBKS(),
                        RoutePoolFactory.newHashPoolSet(instance)), //
                new ExampleParallelGRASP(instance, VRPUtilities.getBKS()), //
                new ExampleVNS(instance, VRPUtilities.getBKS()), //
                new ExampleParallelVNS(instance, VRPUtilities.getBKS()) //
        };

        Stopwatch sw = new Stopwatch();
        for (ExampleBase example : examples) {
            sw.restart();
            VRPSolution sol = example.call();
            sw.stop();
            logResult(example.getClass().getSimpleName().replace("Example", ""), sol,
                    VRPUtilities.getBKS(), sw);
        }

        Logging.awaitLogging(60000);

    }

    /**
     * Log the results of an optimization algorithm
     * 
     * @param methodName
     *            the name of the optimization algorithm
     * @param solution
     *            the best solution found
     */
    public static void logResult(String methodName, VRPSolution solution, BestKnownSolutions bks,
            Stopwatch sw) {
        VRPLogging.getBenchLogger().info("================================================");
        VRPLogging.getBenchLogger().info("%s terminated after %s", methodName,
                sw.readTimeString(3, true, false));
        VRPLogging.getBenchLogger().info("%s solution: %s", methodName, solution);
        VRPLogging.getBenchLogger().info(
                "%s gap to best known solution: %.2f%%",
                methodName,
                bks.getGapToBKS(solution.getParentInstance().getName(), solution.getCost(),
                        OptimizationSense.MINIMIZATION) * 100);
    }

}
