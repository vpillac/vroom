/**
 * 
 */
package vrp2013.examples;

import ilog.concert.IloException;
import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.ProgressMonitor;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.SimpleParameters;
import vrp2013.algorithms.CW;
import vrp2013.algorithms.HeuristicConcentration;
import vrp2013.util.RoutePoolFactory;
import vrp2013.util.SolutionFactories;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * The class <code>ExampleMSH</code> execute a simple version of a the Multi Space Sample Heuristic
 * <p>
 * J.E. Mendoza, J.G. Villegas. A multi-space sampling heuristic for the vehicle routing problem with stochastic
 * demands. Optimization Letters, Forthcoming (DOI:10.1007/s11590-012-0555-8.).
 * </p>
 * <p>
 * Creation date: 09/05/2013 - 4:58:56 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class ExampleMSH extends ExampleBase {

    public static int                             sIterations = 1000;

    private final CW                              mCW;
    private final TwoOptNeighborhood<VRPSolution> m2Opt;
    private final HeuristicConcentration          mHC;

    private final IRoutePool<INodeVisit>          mRoutePool;

    /**
     * Creates a new <code>ExampleMSH</code>
     * 
     * @param instance
     * @param bks
     */
    public ExampleMSH(StaticInstance instance, BestKnownSolutions bks, IRoutePool<INodeVisit> pool) {
        super(instance, bks);

        ConstraintHandler<VRPSolution> constraintHandler = new ConstraintHandler<>(
                new CapacityConstraint<VRPSolution>());
        mCW = new CW(getInstance(), SolutionFactories.ARRAY_LIST_SOL_FACTORY, constraintHandler);

        m2Opt = new TwoOptNeighborhood<>(constraintHandler);

        mHC = new HeuristicConcentration(getInstance(), SolutionFactories.ARRAY_LIST_SOL_FACTORY);

        mRoutePool = pool;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public VRPSolution call() {
        getStopwatch().start();

        VRPLogging.getOptLogger().info("Generating solutions");
        VRPSolution best = null;
        ProgressMonitor p = new ProgressMonitor(sIterations, true);
        p.start();
        for (int i = 0; i < sIterations; i++) {
            if (i % (sIterations / 10) == 0)
                VRPLogging.getOptLogger().info("%s", p);
            VRPSolution solution = mCW.call();

            // for (RouteBase r : solution) {
            // if (r.length() > 2) {
            // VRPSolution sol = (VRPSolution) mCW.getSolutionFactory().newSolution(
            // getInstance());
            // sol.addRoute(r);
            // m2Opt.localSearch(sol, new SimpleParameters(LSStrategy.DET_FIRST_IMPROVEMENT,
            // Integer.MAX_VALUE, 10));
            // }
            // }

            m2Opt.localSearch(solution, new SimpleParameters(LSStrategy.DET_FIRST_IMPROVEMENT,
                    Integer.MAX_VALUE, 3));

            mRoutePool.add(solution);
            if (best == null || solution.getCost() < best.getCost())
                best = solution;
            p.iterationFinished();
        }
        p.stop();

        getStopwatch().pause();
        logResult("init", best);
        logPoolStatistics("INIT", mRoutePool);

        getStopwatch().restart();
        VRPLogging.getOptLogger().info("Executing the Heuristic Concentration");
        try {
            mHC.initialize(mRoutePool, best);
            mHC.call();
            logResult("hc", mHC.getBestSolution());
        } catch (IloException e) {
            VRPLogging.getOptLogger().exception("ExampleMSH.run", e);
        }

        getStopwatch().stop();
        return mHC.getBestSolution();
    }

    /**
     * JAVADOC
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
        // IRoutePool<INodeVisit> pool = RoutePoolFactory.newListPool();
        // IRoutePool<INodeVisit> pool = RoutePoolFactory.newHashPoolGroer(instance);
        IRoutePool<INodeVisit> pool = RoutePoolFactory.newHashPoolSet(instance);

        ExampleMSH example = new ExampleMSH(instance, VRPUtilities.getBKS(), pool);

        example.call();

        shutdown();

    }

}
