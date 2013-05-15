package vrp2013.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
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
public class ExampleRoutesAtomic extends ExampleBase {

    public static int      sIterations = 1000;

    /**
     * an array containing running times
     * [ArrayList:append,ArrayList:getAt,ArrayList:removeFirst,LinkedList:append,LinkedList
     * :getAt,LinkedList:removeFirst]
     */
    private final double[] mTimes;

    public ExampleRoutesAtomic(StaticInstance instance, BestKnownSolutions bks) {
        super(instance, bks);

        mTimes = new double[6];
    }

    @Override
    public VRPSolution call() {
        Stopwatch swAppend = new Stopwatch();
        Stopwatch swGetAt = new Stopwatch();
        Stopwatch swRmv = new Stopwatch();

        // Create a solution with empty routes
        VRPSolution sol = (VRPSolution) SolutionFactories.ARRAY_LIST_SOL_FACTORY
                .newSolution(getInstance());
        for (int i = 0; i < sIterations; i++) {
            RouteBase route = (RouteBase) SolutionFactories.ARRAY_LIST_SOL_FACTORY.newRoute(sol,
                    getInstance().getFleet().getVehicle(0));
            sol.addRoute(route);
        }
        // Generate an array containing randomly generated indices
        Random rnd = new Random(0);
        int[] rndIdx = new int[getInstance().getNodeVisits().size()];
        for (int i = 0; i < rndIdx.length; i++) {
            rndIdx[i] = rnd.nextInt(getInstance().getNodeVisits().size());
        }

        swAppend.start();
        for (int i = 0; i < sIterations; i++) {
            for (INodeVisit n : getInstance().getNodeVisits())
                sol.getRoute(i).appendNode(n);
        }
        swAppend.stop();

        swGetAt.start();
        for (int i = 0; i < sIterations; i++) {
            for (int k : rndIdx)
                sol.getRoute(i).getNodeAt(k);
        }
        swGetAt.stop();

        swRmv.start();
        for (int i = 0; i < sIterations; i++) {
            while (sol.getRoute(i).length() > 0)
                sol.getRoute(i).extractNode(0);
        }
        swRmv.stop();
        mTimes[0] = swAppend.readTimeMS();
        mTimes[1] = swGetAt.readTimeMS();
        mTimes[2] = swRmv.readTimeMS();

        swAppend.reset();
        swGetAt.reset();
        swRmv.reset();

        sol = (VRPSolution) SolutionFactories.LINKED_LIST_SOL_FACTORY.newSolution(getInstance());
        for (int i = 0; i < sIterations; i++) {
            RouteBase route = (RouteBase) SolutionFactories.LINKED_LIST_SOL_FACTORY.newRoute(sol,
                    getInstance().getFleet().getVehicle(0));
            sol.addRoute(route);
        }
        swAppend.start();
        for (int i = 0; i < sIterations; i++) {
            for (INodeVisit n : getInstance().getNodeVisits())
                sol.getRoute(i).appendNode(n);
        }
        swAppend.stop();

        swGetAt.start();
        for (int i = 0; i < sIterations; i++) {
            for (int k = 0; k < sol.getRoute(i).length(); k++)
                sol.getRoute(i).getNodeAt(k);
        }
        swGetAt.stop();

        swRmv.start();
        for (int i = 0; i < sIterations; i++) {
            while (sol.getRoute(i).length() > 0)
                sol.getRoute(i).extractNode(0);
        }
        swRmv.stop();
        mTimes[3] = swAppend.readTimeMS();
        mTimes[4] = swGetAt.readTimeMS();
        mTimes[5] = swRmv.readTimeMS();

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

        double[] times = new double[6];

        // Read all the instances from the christofides-mingozzi-toth-sandi benchmark
        List<File> instanceFiles = null;
        try {
            instanceFiles = Utilities.listFiles(
                    "./instances/cvrp/christofides-mingozzi-toth-sandi", ".+xml.zip");
        } catch (FileNotFoundException e) {
            VRPLogging.getBenchLogger().exception("ExampleRoute.main", e);
        }
        List<StaticInstance> instances = new LinkedList<>();
        for (File f : instanceFiles) {
            instances.add(VRPUtilities.loadInstance(f));
        }

        // Warm-up: make sure all classes are already loaded and let time for runtime optimization
        for (StaticInstance instance : instances) {
            ExampleRoutesAtomic example = new ExampleRoutesAtomic(instance, bks);
            example.call();
        }

        // Actual test
        for (StaticInstance instance : instances) {
            ExampleRoutesAtomic example = new ExampleRoutesAtomic(instance, bks);
            example.call();
            for (int i = 0; i < times.length; i++) {
                times[i] += example.mTimes[i];
            }
        }

        VRPLogging.getBenchLogger().info(
                "ExampleRoute.run:ArrayList  Append:%.1fms GetNodeAt:%.1fms RemoveFirst:%.1fms",
                times[0], times[1], times[2]);
        VRPLogging.getBenchLogger().info(
                "ExampleRoute.run:LinkedList Append:%.1fms GetNodeAt:%.1fms RemoveFirst:%.1fms",
                times[3], times[4], times[5]);

        shutdown();
    }
}
