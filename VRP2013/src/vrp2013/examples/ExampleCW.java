package vrp2013.examples;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWLogging;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.logging.LoggerHelper;
import vrp2013.algorithms.CW;
import vrp2013.util.SolutionFactories;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * The class <code>ExampleCW</code> illustrates how the {@link CW Clarke and Wright} constructive heuristic works
 * <p>
 * Creation date: 09/05/2013 - 11:17:00 AM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class ExampleCW extends ExampleBase {

    private final CW mCW;

    public ExampleCW(StaticInstance instance, BestKnownSolutions bks) {
        super(instance, bks);
        mCW = new CW(getInstance(), SolutionFactories.ARRAY_LIST_SOL_FACTORY,
                new ConstraintHandler<>(new CapacityConstraint<VRPSolution>()));
    }

    @Override
    public VRPSolution call() {
        getStopwatch().restart();
        mCW.call();
        getStopwatch().stop();
        logResult("CW", mCW.getBestSolution());
        return mCW.getBestSolution();
    }

    public static void main(String[] args) {
        // Setup the logging system
        // The first argument is the default logger level
        // The second is the filtering level of the appender (i.e. console output)
        // The last can be set to true to do the logging in a separate thread, or false to do it in the main thread
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_LOW_DEBUG, true);

        // Sets the logging level for the CW loggers
        CWLogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_WARN);

        // Sets the logging level for the VRP examples logger
        VRPLogging.getOptLogger().setLevel(LoggerHelper.LEVEL_DEBUG);
        VRPLogging.getBenchLogger().setLevel(LoggerHelper.LEVEL_INFO);

        ExampleCW example = new ExampleCW(VRPUtilities.pickInstance(), VRPUtilities.getBKS());
        example.call();

        shutdown();
    }
}
