package vrp2013.examples;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.logging.LoggerHelper;
import vrp2013.algorithms.CW;
import vrp2013.algorithms.VND;
import vrp2013.util.SolutionFactories;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * The class <code>ExampleVND</code> runs the {@link VND Variable Neighborhood Descent} algorithm on a single instance
 * <p>
 * Creation date: 16/05/2013 - 1:31:41 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class ExampleVND extends ExampleBase {

    private final CW  mCW;
    private final VND mVND;

    public ExampleVND(StaticInstance instance, BestKnownSolutions bks) {
        super(instance, bks);

        ConstraintHandler<VRPSolution> constraintHandler = new ConstraintHandler<>(
                new CapacityConstraint<VRPSolution>());
        mCW = new CW(getInstance(), SolutionFactories.ARRAY_LIST_SOL_FACTORY, constraintHandler);
        mVND = new VND(getInstance(), constraintHandler, null);
    }

    @Override
    public VRPSolution call() {
        // Find an initial solution
        VRPSolution sol = mCW.call();

        getStopwatch().start();
        // Execute the VND algorithm
        mVND.setInitialSolution(sol);
        sol = mVND.call();
        sol.removeEmptyRoutes();
        getStopwatch().stop();

        logResult("VND", sol);

        return sol;
    }

    public static void main(String[] args) {
        // Setup the logging system
        // The first argument is the default logger level
        // The second is the filtering level of the appender (i.e. console output)
        // The last can be set to true to do the logging in a separate thread, or false to do it in the main thread
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_LOW_DEBUG, true);
        VRPLogging.getBenchLogger().setLevel(LoggerHelper.LEVEL_INFO);
        // Sets the logging level for the VRP examples logger
        VRPLogging.getOptLogger().setLevel(LoggerHelper.LEVEL_LOW_DEBUG);

        ExampleVND example = new ExampleVND(VRPUtilities.pickInstance(), VRPUtilities.getBKS());
        example.call();

        shutdown();

    }

}
