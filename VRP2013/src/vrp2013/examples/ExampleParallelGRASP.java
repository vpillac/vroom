/**
 * 
 */
package vrp2013.examples;

import ilog.concert.IloException;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.logging.LoggerHelper;
import vrp2013.algorithms.HeuristicConcentration;
import vrp2013.algorithms.ParallelGRASP;
import vrp2013.util.RoutePoolFactory;
import vrp2013.util.SolutionFactories;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * The class <code>ExampleParallelGRASP</code> is an example to run a parallel GRASP algorithm on a single instance
 * <p>
 * Creation date: 10/05/2013 - 2:07:02 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class ExampleParallelGRASP extends ExampleBase {

    private final ParallelGRASP          mPGRASP;

    private final HeuristicConcentration mHC;

    /**
     * Creates a new <code>ExampleParallelGRASP</code>
     * 
     * @param instance
     * @param bks
     */
    public ExampleParallelGRASP(StaticInstance instance, BestKnownSolutions bks) {
        super(instance, bks);

        mPGRASP = new ParallelGRASP(getInstance(), SolutionFactories.ARRAY_LIST_SOL_FACTORY, 0,
                ExampleGRASP.sGRASPIterations, RoutePoolFactory.newHashPoolSet(getInstance()));
        mHC = new HeuristicConcentration(getInstance(), SolutionFactories.ARRAY_LIST_SOL_FACTORY);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public VRPSolution call() {
        getStopwatch().start();

        try {
            mPGRASP.call();

            logResult("Parallel GRASP", mPGRASP.getBestSolution());
            logPoolStatistics("Parallel GRASP", mPGRASP.getRoutePool());

            mHC.initialize(mPGRASP.getRoutePool(), mPGRASP.getBestSolution());
            getStopwatch().restart();
            // Run the heuristic concentration
            mHC.call();
            getStopwatch().pause();

            logResult("HC", mHC.getBestSolution());
        } catch (IloException e) {
            VRPLogging.getOptLogger().exception("ExampleParallelGRASP.run", e);
        }

        getStopwatch().stop();

        mPGRASP.dispose();
        return mHC.getBestSolution();
    }

    /**
     * Main method for the Parallel GRASP example
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

        ExampleParallelGRASP example = new ExampleParallelGRASP(VRPUtilities.pickInstance(),
                VRPUtilities.getBKS());
        example.call();

        shutdown();
    }

}
