package vrp2013.examples;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.logging.LoggerHelper;
import vrp2013.algorithms.GRASP;
import vrp2013.util.SolutionFactories;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * The class <code>ExampleGRASP</code> execute the {@link GRASP} algorithm on a single instance
 * <p>
 * Creation date: 09/05/2013 - 4:10:54 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class ExampleGRASP extends ExampleBase {

    /** The number of iterations for the GRASP algorithm */
    public static int   sGRASPIterations = 10;
    public static int   sSeed            = 0;

    private final GRASP mGrasp;

    /**
     * Returns the GRASP algorithm used in this example
     * 
     * @return the GRASP algorithm used in this example
     */
    public GRASP getGrasp() {
        return mGrasp;
    }

    /**
     * Creates a new <code>ExampleGRASP</code>
     * 
     * @param instance
     *            the instance to solve
     * @param bks
     *            the best known solutions
     * @param routePool
     *            the route pool to collect routes (optional)
     */
    public ExampleGRASP(StaticInstance instance, BestKnownSolutions bks,
            IRoutePool<INodeVisit> routePool) {
        super(instance, bks);

        mGrasp = new GRASP(instance, SolutionFactories.ARRAY_LIST_SOL_FACTORY, sSeed,
                sGRASPIterations, routePool);
    }

    @Override
    public VRPSolution call() {
        VRPLogging.getOptLogger().info("Executing the GRASP algorithm");
        getStopwatch().start();
        getGrasp().call();
        getStopwatch().pause();

        logResult("GRASP", getGrasp().getBestSolution());

        if (getGrasp().getRoutePool() != null)
            logPoolStatistics("GRASP", getGrasp().getRoutePool());

        return getGrasp().getBestSolution();
    }

    /**
     * Main method for the GRASP example
     * 
     * @param args
     */
    public static void main(String[] args) {
        // Setup the logging system
        // The first argument is the default logger level
        // The second is the filtering level of the appender (i.e. console output)
        // The last can be set to true to do the logging in a separate thread, or false to do it in the main thread
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, true);
        VRPLogging.getBenchLogger().setLevel(LoggerHelper.LEVEL_INFO);
        VRPLogging.getOptLogger().setLevel(LoggerHelper.LEVEL_DEBUG);

        ExampleGRASP example = new ExampleGRASP(VRPUtilities.pickInstance(), VRPUtilities.getBKS(),
                null);
        example.call();

        shutdown();
    }
}
