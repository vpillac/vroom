/**
 * 
 */
package vrp2013.algorithms;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.optimization.OptimizationSense;
import vrp2013.util.BatchExecutor;
import vrp2013.util.BatchExecutor.Result;
import vrp2013.util.VRPSolution;

/**
 * <code>pGRASP</code> is a parallel implementation of {@link GRASP}
 * <p>
 * Creation date: May 4, 2013 - 2:56:28 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class ParallelGRASP implements IVRPOptimizationAlgorithm {

    private final IVRPInstance           mInstance;
    private final ISolutionFactory       mSolutionFactory;
    private final long                   mInitSeed;
    private final int                    mIterations;

    private final List<GRASP>            mSubprocesses;

    private final IRoutePool<INodeVisit> mRoutePool;

    private VRPSolution                  mBestSolution;

    private final BatchExecutor          mExecutor;

    /**
     * Returns the pool of routes generated during the iterations of the GRASP
     * 
     * @return the pool containing the generated routes
     */
    @Override
    public IRoutePool<INodeVisit> getRoutePool() {
        return mRoutePool;
    }

    /**
     * Returns the best solution found by this GRASP instance on the last run
     * 
     * @return the best solution found by this GRASP instance on the last run
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
     * Creates a new <code>ParallelGRASP</code>
     * 
     * @param instance
     *            the instance that will be solved
     * @param solutionFactory
     *            a factory to generate new solutions and routes
     * @param initSeed
     *            the seed for the first iteration of the GRASP
     * @param iterations
     *            the total number of iterations
     * @param routePool
     *            the pool in which routes will be stored
     * @author vpillac
     */
    public ParallelGRASP(IVRPInstance instance, ISolutionFactory solutionFactory, long initSeed,
            int iterations, IRoutePool<INodeVisit> routePool) {
        mInstance = instance;
        mInitSeed = initSeed;
        mSolutionFactory = solutionFactory;
        mIterations = iterations;

        mSubprocesses = new LinkedList<>();

        // Initialize the GRASP subprocesses
        // - Get the number of available processors
        int threadCount = Runtime.getRuntime().availableProcessors();

        // - Create one GRASP instance for each iteration
        for (int t = 0; t < mIterations; t++) {
            // Create a new subprocess
            mSubprocesses
                    .add(new GRASP(mInstance, mSolutionFactory, initSeed, 1, routePool.clone()));
            // Make sure the seeds are the same as they would be in the sequential execution
            initSeed++;
        }

        mRoutePool = routePool;

        // Create an executor that will be used to run the GRASP subprocess
        mExecutor = new BatchExecutor(threadCount, "pGRASP");
    }

    @Override
    public VRPSolution call() {
        // Submit the subprocesses to the executor
        // This will create the thread and run the GRASP subprocesses
        // Once all subprocesses have finished, the method returns a mapping between subprocesses and the best solution
        // found (Result is a wrapper that allows for exceptions to be captured)
        Map<GRASP, Result<VRPSolution>> results = mExecutor.submitBatchAndWait(mSubprocesses);

        // Loop through the <GRASP subprocess, Best solution> pairs
        for (Entry<GRASP, Result<VRPSolution>> r : results.entrySet()) {
            if (mBestSolution == null
                    || OptimizationSense.MINIMIZATION.isBetter(getBestSolution(), r.getValue()
                            .get()))
                // Found a better solution
                mBestSolution = r.getValue().get();
            if (getRoutePool() != null)
                // Add the routes collected in the subprocess to the main route pool
                getRoutePool().add(r.getKey().getRoutePool());
        }

        // Shutdown the executor and terminate its threads
        mExecutor.shutdown();

        return getBestSolution();
    }

    @Override
    public void dispose() {
        if (mRoutePool != null)
            mRoutePool.dispose();
        for (GRASP g : mSubprocesses)
            g.dispose();
    }
}
