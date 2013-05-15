package vrp2013.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.SimpleParameters;
import vrp2013.algorithms.CW;
import vrp2013.algorithms.VND;
import vrp2013.util.SolutionFactories;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * <code>ExampleRoute</code>
 * <p>
 * Creation date: 08/05/2013 - 2:25:43 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class ExampleRoutesOptim extends ExampleBase {

    public static int      sIterations = 10;

    private final CW       mCW;
    private final VND      mVND;

    /** an array containing running times [ArrayList:CW,ArrayList:VND,LinkedList:CW,LinkedList:VND] */
    private final double[] mTimes;

    public ExampleRoutesOptim(StaticInstance instance, BestKnownSolutions bks) {
        super(instance, bks);

        ConstraintHandler<VRPSolution> mConstraintHandler = new ConstraintHandler<>(
                new CapacityConstraint<VRPSolution>());

        CWParameters cwParams = new CWParameters();
        cwParams.setDefaultValues();

        mCW = new CW(getInstance(), null, mConstraintHandler);

        mVND = new VND(getInstance(), mConstraintHandler, null);

        mTimes = new double[4];
    }

    @Override
    public VRPSolution call() {
        Stopwatch cwSW = new Stopwatch();
        Stopwatch vndSW = new Stopwatch();

        IParameters params = new SimpleParameters(LSStrategy.DET_BEST_IMPROVEMENT,
                Integer.MAX_VALUE, Integer.MAX_VALUE);

        LinkedList<VRPSolution> solutions = new LinkedList<>();
        mCW.updateSeed(0);
        mCW.setSolutionFactory(SolutionFactories.ARRAY_LIST_SOL_FACTORY);
        cwSW.start();
        for (int i = 0; i < sIterations; i++) {
            solutions.add(mCW.call());
        }
        cwSW.stop();

        vndSW.start();
        for (VRPSolution sol : solutions) {
            mVND.localSearch(sol);
        }
        vndSW.stop();
        mTimes[0] = cwSW.readTimeMS();
        mTimes[1] = vndSW.readTimeMS();

        cwSW.reset();
        vndSW.reset();

        solutions = new LinkedList<>();
        mCW.updateSeed(0);
        mCW.setSolutionFactory(SolutionFactories.LINKED_LIST_SOL_FACTORY);
        cwSW.start();
        for (int i = 0; i < sIterations; i++) {
            solutions.add(mCW.call());
        }
        cwSW.stop();

        vndSW.start();
        for (VRPSolution sol : solutions) {
            mVND.localSearch(sol);
        }
        vndSW.stop();
        mTimes[2] = cwSW.readTimeMS();
        mTimes[3] = vndSW.readTimeMS();

        return null;

    }

    public static void main(String[] args) {
        // Setup the loggin system
        // The first argument is the default logger level
        // The second is the filtering level of the appender (i.e. console output)
        // The last can be set to true to do the logging in a separate thread, or false to do it in the main thread
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, false);
        VRPLogging.getBenchLogger().setLevel(LoggerHelper.LEVEL_DEBUG);

        String bksFile = "./instances/cvrp/augerat.sol";
        BestKnownSolutions bks = new BestKnownSolutions(bksFile);

        double[] times = new double[4];
        List<File> instances = null;
        try {
            instances = Utilities.listFiles("./instances/cvrp/christofides-mingozzi-toth-sandi",
                    ".+xml.zip");
        } catch (FileNotFoundException e) {
            VRPLogging.getBenchLogger().exception("ExampleRoute.main", e);
        }
        for (File f : instances) {
            StaticInstance instance = VRPUtilities.loadInstance(f);
            ExampleRoutesOptim example = new ExampleRoutesOptim(instance, bks);
            example.call();
            for (int i = 0; i < times.length; i++) {
                times[i] += example.mTimes[i];
            }
        }

        VRPLogging.getBenchLogger().info("ExampleRoute.run:ArrayList  CW %.1fms VND:%.1fms",
                times[0], times[1]);
        VRPLogging.getBenchLogger().info("ExampleRoute.run:LinkedList CW %.1fms VND:%.1fms",
                times[2], times[3]);

        shutdown();
    }
}
