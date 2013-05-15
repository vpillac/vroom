/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */
package vrp2013.sol.examples;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWLogging;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.utilities.logging.LoggerHelper;
import vrp2013.algorithms.CW;
import vrp2013.util.SolutionFactories;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

public class TestPertub {

    public TestPertub() {
        // TODO Auto-generated constructor stub
    }

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
        CWLogging.getBaseLogger().setLevel(LoggerHelper.LEVEL_INFO);
        // Sets the logging level for the VRP examples logger
        VRPLogging.getOptLogger().setLevel(LoggerHelper.LEVEL_LOW_DEBUG);

        // Use larger instances
        // VRPUtilities.setup("./instances/cvrp/christofides-mingozzi-toth-sandi");
        StaticInstance instance = VRPUtilities.pickInstance(0);

        ConstraintHandler<VRPSolution> ctr = new ConstraintHandler<>(
                new CapacityConstraint<VRPSolution>());
        CW cw = new CW(instance, SolutionFactories.ARRAY_LIST_SOL_FACTORY, ctr);
        for (int i = 0; i < 10; i++) {
            VRPSolution sol = cw.call();
            // try {
            // Thread.sleep(500);
            // } catch (InterruptedException e) {
            // }
            System.out.println(sol);
        }

        // StringExchangeNeighborhood<VRPSolution> neigh = new StringExchangeNeighborhood<>(ctr);
        //
        // SimpleParameters params = new SimpleParameters(LSStrategy.RND_NON_IMPROVING,
        // Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        // for (int i = 0; i < 20; i++) {
        // VRPSolution nSol = sol.clone();
        // neigh.pertub(instance, nSol, params);
        // System.out.println(nSol);
        // }
    }

}
