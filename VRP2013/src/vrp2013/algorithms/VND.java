package vrp2013.algorithms;

import java.util.ArrayList;
import java.util.List;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.vrp.OrOptNeighborhood;
import vroom.common.heuristics.vrp.SwapNeighborhood;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.SimpleParameters;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;

/**
 * The class <code>VND</code> is a simple implementation of a Variable Neighborhood Descent algorithm with a fixed set
 * of neighborhoods (Swap, 2-Opt, Or-Opt, String-Exchange).
 * <p>
 * Creation date: May 4, 2013 - 10:18:18 AM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class VND implements IVRPOptimizationAlgorithm {
    private final IVRPInstance                        mInstance;

    private final List<INeighborhood<VRPSolution, ?>> mNeighborhoods;

    private final IRoutePool<INodeVisit>              mRoutePool;

    private final Stopwatch                           mNeighStopwatch;

    /** the start solution **/
    private VRPSolution                               mStartSolution;

    /** the best solution **/
    private VRPSolution                               mBestSolution;

    /**
     * Getter for the current solution
     * 
     * @return the current solution
     */
    public VRPSolution getCurrentSolution() {
        return this.mStartSolution;
    }

    /**
     * Setter for the current solution
     * 
     * @param solution
     *            the current solution
     */
    public void setInitialSolution(VRPSolution solution) {
        this.mStartSolution = solution;
    }

    /**
     * Creates a new <code>VND</code>
     * 
     * @param instance
     *            the instance on which optimization will be performed
     * @param constraintHandler
     *            a constraint handler that contains the constraints from the VRP
     * @param routePool
     *            a pool that will store the routes found in the local search
     */
    public VND(IVRPInstance instance, ConstraintHandler<VRPSolution> constraintHandler,
            IRoutePool<INodeVisit> routePool) {
        mInstance = instance;

        mNeighborhoods = new ArrayList<>();
        mNeighborhoods.add(new OrOptNeighborhood<>(constraintHandler));
        mNeighborhoods.add(new SwapNeighborhood<>(constraintHandler));
        mNeighborhoods.add(new TwoOptNeighborhood<>(constraintHandler));

        mNeighStopwatch = new Stopwatch();
        mRoutePool = routePool;
    }

    @Override
    public void dispose() {
        mNeighborhoods.clear();
    }

    /**
     * Execute the VND algorithm on the given {@code  solution}
     * 
     * @param solution
     *            the solution on which the local search will be applied
     * @return {@code true} if the solution was changed, {@code false} otherwise
     */
    public boolean localSearch(final VRPSolution solution) {

        boolean changed = false;

        for (INeighborhood<VRPSolution, ?> neighborhood : mNeighborhoods) {
            mNeighStopwatch.restart();

            // Explore the neighborhood
            changed = neighborhood.localSearch(solution, SimpleParameters.BEST_IMPROVEMENT);

            mNeighStopwatch.pause();

            // Log the solution
            VRPLogging.logOptResults(neighborhood.getShortName(), changed, mNeighStopwatch,
                    solution);
        }

        setBestSolution(solution);
        return changed;
    }

    @Override
    public VRPSolution call() {
        VRPSolution sol = getCurrentSolution().clone();
        localSearch(sol);
        setBestSolution(sol);
        return sol;
    }

    /**
     * Return the list of neighborhoods defined in this VND
     * 
     * @return the list of neighborhoods defined in this VND
     * @author vpillac
     */
    List<INeighborhood<VRPSolution, ?>> getNeighborhoods() {
        return mNeighborhoods;
    }

    @Override
    public IRoutePool<INodeVisit> getRoutePool() {
        return mRoutePool;
    }

    @Override
    public VRPSolution getBestSolution() {
        return mBestSolution;
    }

    /**
     * Set the best solution found by this algorithm in the last call to {@link #call()} or
     * {@link #localSearch(VRPSolution)} JAVADOC
     * 
     * @param bestSolution
     * @author vpillac
     */
    protected void setBestSolution(VRPSolution bestSolution) {
        mBestSolution = bestSolution;
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
        return null;
    }

    /**
     * Returns the stopwatch used in this instance
     * 
     * @return the stopwatch used in this instance
     * @author vpillac
     */
    protected Stopwatch getNeighStopwatch() {
        return mNeighStopwatch;
    }

}
