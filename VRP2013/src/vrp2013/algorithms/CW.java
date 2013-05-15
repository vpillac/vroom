package vrp2013.algorithms;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.algorithms.RandomizedSavingsHeuristic;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.modeling.util.ISolutionFactory;
import vrp2013.util.VRPSolution;

/**
 * The class <code>CW</code> is a convenience class that uses an instance of {@link ClarkeAndWrightHeuristic} to build
 * new routes
 * <p>
 * Creation date: 09/05/2013 - 10:45:23 AM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class CW implements IVRPOptimizationAlgorithm {

    private VRPSolution                                 mLastSolution;
    private final IVRPInstance                          mInstance;

    private final ISolutionFactory                      mSolutionFactory;

    private final ClarkeAndWrightHeuristic<VRPSolution> mCWHeuristic;

    /**
     * Creates a new <code>CW</code>
     * 
     * @param instance
     * @param solFactory
     * @param constraintHandler
     */
    public CW(IVRPInstance instance, ISolutionFactory solFactory,
            ConstraintHandler<VRPSolution> constraintHandler) {
        mInstance = instance;
        mSolutionFactory = solFactory;

        CWParameters cwParams = new CWParameters();
        cwParams.setDefaultValues();
        cwParams.set(CWParameters.RANDOM_SEED, 50l);

        mCWHeuristic = new ClarkeAndWrightHeuristic<>(cwParams, RandomizedSavingsHeuristic.class,
                constraintHandler);

        mCWHeuristic.setSolutionFactory(getSolutionFactory());
    }

    @Override
    public VRPSolution call() {
        mLastSolution = mCWHeuristic.newSolution(null, getInstance(), null);
        return mLastSolution;
    }

    @Override
    public void dispose() {
    }

    @Override
    public IRoutePool<INodeVisit> getRoutePool() {
        return null;
    }

    @Override
    public VRPSolution getBestSolution() {
        return mLastSolution;
    }

    @Override
    public IVRPInstance getInstance() {
        return mInstance;
    }

    @Override
    public int getIterations() {
        return 0;
    }

    @Override
    public ISolutionFactory getSolutionFactory() {
        return mSolutionFactory;
    }

    /**
     * Update the random seed of the CW heuristic
     * 
     * @param seed
     */
    public void updateSeed(long seed) {
        mCWHeuristic.updateSeed(seed);
    }

    /**
     * Sets the solution factory used in the CW heuristic
     * 
     * @param solFactory
     */
    public void setSolutionFactory(ISolutionFactory solFactory) {
        mCWHeuristic.setSolutionFactory(solFactory);
    }

}
