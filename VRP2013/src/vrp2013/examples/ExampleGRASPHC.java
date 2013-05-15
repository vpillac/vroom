/**
 * 
 */
package vrp2013.examples;

import ilog.concert.IloException;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vrp2013.algorithms.HeuristicConcentration;
import vrp2013.util.RoutePoolFactory;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * The class <code>ExampleGRASPHC</code> is an extension of {@link ExampleGRASP} that adds a heuristic concentration
 * post-optimization
 * <p>
 * Creation date: 07/05/2013 - 5:56:17 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class ExampleGRASPHC extends ExampleGRASP {

    private final HeuristicConcentration mHC;

    /**
     * Returns the heuristic concentration used in this example
     * 
     * @return the heuristic concentration used in this example
     */
    public HeuristicConcentration getHeurCons() {
        return mHC;
    }

    /**
     * Creates a new <code>ExampleGRASPHC</code>
     * 
     * @param instance
     *            the instance to solve
     * @param bks
     *            the best known solutions
     * @param routePool
     *            the route pool to collect routes (optional)
     */
    public ExampleGRASPHC(StaticInstance instance, BestKnownSolutions bks,
            IRoutePool<INodeVisit> routePool) {
        super(instance, bks, routePool);
        mHC = new HeuristicConcentration(getInstance(), getGrasp().getSolutionFactory());
    }

    @Override
    public VRPSolution call() {
        // Run the GRASP
        super.call();
        VRPLogging.getOptLogger().info("Executing the Heuristic Concentration post-optimization");

        try {
            mHC.initialize(getGrasp().getRoutePool(), getGrasp().getBestSolution());
            getStopwatch().restart();
            // Run the heuristic concentration
            getHeurCons().call();
            getStopwatch().pause();

            getHeurCons().exportModel("./HCSetCovering.lp");

            logResult("HC", getHeurCons().getBestSolution());

            return getHeurCons().getBestSolution();
        } catch (IloException e) {
            Logging.getBaseLogger().exception("Test2.main", e);
            return null;
        }
    }

    /**
     * Main method for the GRASP+HC example
     * 
     * @param args
     */
    public static void main(String[] args) {
        // Setup the loggin system
        // The first argument is the default logger level
        // The second is the filtering level of the appender (i.e. console output)
        // The last can be set to true to do the logging in a separate thread, or false to do it in the main thread
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_DEBUG, true);
        VRPLogging.getBenchLogger().setLevel(LoggerHelper.LEVEL_INFO);
        VRPLogging.getOptLogger().setLevel(LoggerHelper.LEVEL_DEBUG);

        StaticInstance instance = VRPUtilities.pickInstance();

        // Select the pool to use
        IRoutePool<INodeVisit> pool = RoutePoolFactory.newListPool();
        // IRoutePool<INodeVisit> pool = RoutePoolFactory.newHashPoolGroer(instance);
        // IRoutePool<INodeVisit> pool = RoutePoolFactory.newHashPoolSet(instance);

        ExampleGRASPHC example = new ExampleGRASPHC(instance, VRPUtilities.getBKS(), pool);

        example.call();

        shutdown();
    }

}
