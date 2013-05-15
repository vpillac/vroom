package vrp2013.sol.algorithms;

import java.util.ArrayList;
import java.util.List;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.vrp.OrOptNeighborhood;
import vroom.common.heuristics.vrp.StringExchangeNeighborhood;
import vroom.common.heuristics.vrp.SwapNeighborhood;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.SimpleParameters;
import vrp2013.algorithms.IVRPOptimizationAlgorithm;
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

    /** the current solution **/
    private VRPSolution                               mCurrentSolution;

    /**
     * Getter for the current solution
     * 
     * @return the current solution
     */
    public VRPSolution getCurrentSolution() {
        return this.mCurrentSolution;
    }

    /**
     * Setter for the current solution
     * 
     * @param solution
     *            the current solution
     */
    public void setInitialSolution(VRPSolution solution) {
        this.mCurrentSolution = solution;
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

        mNeighborhoods = new ArrayList<>(4);
        mNeighborhoods.add(new SwapNeighborhood<>(constraintHandler));
        mNeighborhoods.add(new TwoOptNeighborhood<>(constraintHandler));
        mNeighborhoods.add(new OrOptNeighborhood<>(constraintHandler));
        mNeighborhoods.add(new StringExchangeNeighborhood<>(constraintHandler));

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
    public boolean localSearch(VRPSolution solution) {

        boolean changed = true;

        while (changed) {
            for (INeighborhood<VRPSolution, ?> neighborhood : mNeighborhoods) {
                mNeighStopwatch.restart();

                // Explore the neighborhood
                changed = neighborhood.localSearch(solution, SimpleParameters.BEST_IMPROVEMENT);

                mNeighStopwatch.pause();

                // Log the solution
                VRPLogging.logOptResults(neighborhood.getShortName(), changed, mNeighStopwatch,
                        solution);

                // Add the routes from the solution to the pool
                if (mRoutePool != null)
                    mRoutePool.add(solution);

                // An improvement was found, restart from the first neighborhood
                if (changed)
                    break;
            }
        }

        return changed;
    }

    @Override
    public VRPSolution call() {
        localSearch(getCurrentSolution());
        return getCurrentSolution();
    }

    /**
     * Return the list of neighborhoods defined in this VND
     * 
     * @return the list of neighborhoods defined in this VND
     * @author vpillac
     */
    protected List<INeighborhood<VRPSolution, ?>> getNeighborhoods() {
        return mNeighborhoods;
    }

    @Override
    public IRoutePool<INodeVisit> getRoutePool() {
        return mRoutePool;
    }

    @Override
    public VRPSolution getBestSolution() {
        return getCurrentSolution();
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

}
