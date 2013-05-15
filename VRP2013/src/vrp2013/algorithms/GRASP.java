/**
 * 
 */
package vrp2013.algorithms;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.Stopwatch;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;

/**
 * <code>GRASP</code> is a simple implementation of the Greedy Randomized Adaptive Search Procedure based on a
 * {@link ClarkeAndWrightHeuristic} for initialization and a {@link VND} for local search.
 * <p>
 * Creation date: 30/04/2013 - 5:06:35 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class GRASP implements IVRPOptimizationAlgorithm {

    private final IVRPInstance                   mInstance;

    private final CW                             mInitialization;
    private final VND                            mLocalSearch;
    private final ConstraintHandler<VRPSolution> mConstraintHandler;
    private final Stopwatch                      mStopwatch;

    private final int                            mIterations;

    private final long                           mInitSeed;

    private final IRoutePool<INodeVisit>         mRoutePool;

    private VRPSolution                          mBestSolution;

    private final ISolutionFactory               mSolutionFactory;

    /* (non-Javadoc)
     * @see vrp2013.algorithms.VRPOptimizationAlgorithm#getRoutePool()
     */
    @Override
    public IRoutePool<INodeVisit> getRoutePool() {
        return mRoutePool;
    }

    /* (non-Javadoc)
     * @see vrp2013.algorithms.VRPOptimizationAlgorithm#getBestSolution()
     */
    @Override
    public VRPSolution getBestSolution() {
        return mBestSolution;
    }

    @Override
    public IVRPInstance getInstance() {
        return mInstance;
    }

    @Override
    public int getIterations() {
        return mIterations;
    }

    @Override
    public ISolutionFactory getSolutionFactory() {
        return mSolutionFactory;
    }

    /**
     * Creates a new <code>GRASP</code>
     * 
     * @param instance
     *            the instance that will be solved
     * @param solutionFactory
     *            the solution factory that will be used to create new solutions and routes
     * @param initSeed
     *            the initial random seed
     * @param iterations
     *            the number of iterations
     * @param routePool
     *            an optional pool to collect routes
     */
    public GRASP(IVRPInstance instance, ISolutionFactory solutionFactory, long initSeed,
            int iterations, IRoutePool<INodeVisit> routePool) {
        mInstance = instance;
        mConstraintHandler = new ConstraintHandler<>(new CapacityConstraint<VRPSolution>());

        mIterations = iterations;

        mSolutionFactory = solutionFactory;

        CWParameters cwParams = new CWParameters();
        cwParams.setDefaultValues();

        mStopwatch = new Stopwatch();

        mInitialization = new CW(getInstance(), getSolutionFactory(), mConstraintHandler);

        mRoutePool = routePool;
        // mRoutePool = new ListRoutePool<>();

        mLocalSearch = new VND(getInstance(), mConstraintHandler, mRoutePool);

        mInitSeed = initSeed;

    }

    @Override
    public VRPSolution call() {
        mBestSolution = null;
        mStopwatch.restart();
        for (int it = 0; it < mIterations; it++) {
            long seed = mInitSeed + it;
            VRPLogging.getOptLogger().debug("%s GRASP iteration %s/%s (%s)",
                    mStopwatch.readTimeString(3, true, false), it + 1, mIterations, seed);

            Stopwatch sw = new Stopwatch();
            sw.start();
            // Execute initialization
            mInitialization.updateSeed(seed);
            VRPSolution solution = mInitialization.call();
            sw.stop();

            VRPLogging.logOptResults("init", true, sw, solution);

            // Execute the local search
            mLocalSearch.setInitialSolution(solution);
            solution = mLocalSearch.call();

            // Remove empty roads for clarity
            solution.removeEmptyRoutes();

            if (mBestSolution == null
                    || solution.getObjectiveValue() < mBestSolution.getObjectiveValue()) {
                // We found a new best solution
                mBestSolution = solution;
            }

            VRPLogging.getOptLogger().debug(
                    "%s GRASP iteration %s/%s (%s) finished - Current:%.2f Best:%.2f",
                    mStopwatch.readTimeString(3, true, false), it + 1, mIterations, seed,
                    solution.getCost(), mBestSolution.getCost());
        }

        VRPLogging.getOptLogger().info("%s GRASP terminated - Best solution: %s",
                mStopwatch.readTimeString(3, true, false), mBestSolution);
        mStopwatch.stop();
        return mBestSolution;
    }

    @Override
    public void dispose() {
        mLocalSearch.dispose();
        mRoutePool.dispose();
    }
}
