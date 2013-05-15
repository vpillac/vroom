package vrp2013.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.GenericNeighborhood;
import vroom.common.heuristics.vrp.StringExchangeNeighborhood;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.SimpleParameters;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;

/**
 * The class <code>ParallelVND</code> is a sequential implementation of the Variable Neighborhood Search.
 * <p>
 * Creation date: 13/05/2013 - 9:36:40 AM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class VNS extends VND {

    private final List<NeighborhoodExplorer> mExplorers;

    /**
     * Creates a new <code>VNS</code>
     * 
     * @param instance
     * @param constraintHandler
     * @param routePool
     * @author vpillac
     */
    public VNS(IVRPInstance instance, ConstraintHandler<VRPSolution> constraintHandler,
            IRoutePool<INodeVisit> routePool) {
        super(instance, constraintHandler, routePool);

        // Redefine the neighborhoods used in this VNS
        getNeighborhoods().clear();
        getNeighborhoods().add(new StringExchangeNeighborhood<>(constraintHandler, 2));
        getNeighborhoods().add(new StringExchangeNeighborhood<>(constraintHandler, 3));
        getNeighborhoods().add(new StringExchangeNeighborhood<>(constraintHandler, 4));
        getNeighborhoods().add(new StringExchangeNeighborhood<>(constraintHandler, 5));

        mExplorers = new ArrayList<>(getNeighborhoods().size());
        for (INeighborhood<VRPSolution, ?> neighborhood : getNeighborhoods()) {
            mExplorers.add(new NeighborhoodExplorer(neighborhood, mExplorers.size()));
        }
    }

    /**
     * Returns the list of neighborhoods explorers
     * 
     * @return the list of neighborhoods explorers
     * @author vpillac
     */
    protected List<NeighborhoodExplorer> getExplorers() {
        return mExplorers;
    }

    @Override
    public boolean localSearch(final VRPSolution solution) {
        VRPLogging.logOptResults("ls-init", false, getNeighStopwatch(), solution);

        // The current solution is the best neighbor from the previous iteration
        VRPSolution currentSolution = solution;
        // A flag set to true if the solution was changed in the last iteration
        boolean changedLastIt = true;
        // A flag set to true if the solution was changed at least once
        boolean changedOverall = false;
        while (changedLastIt) {
            changedLastIt = false;
            getNeighStopwatch().restart();

            for (NeighborhoodExplorer explorer : mExplorers) {
                // Set the starting solution for this explorer
                explorer.setStartSolution(currentSolution);

                // Execute the exploration
                VRPSolution neighbor = explorer.call();
                if (OptimizationSense.MINIMIZATION.isBetter(currentSolution, neighbor)) {
                    // We found a neighbor that is better that the current best neighbor
                    changedLastIt = true;
                    changedOverall = true;
                    currentSolution = neighbor;

                    // Restart from the first neighborhood
                    break;
                }
            }

            getNeighStopwatch().stop();
        }

        // Copy the best solution to the solution passed as argument
        if (changedOverall) {
            solution.clear();
            for (RouteBase r : currentSolution)
                if (r.length() > 2)
                    solution.addRoute(r);
        }

        return changedOverall;
    }

    /**
     * The class <code>NeighborhoodExploration</code> is an implementation of {@link Callable} that performs the
     * exploration of a neighborhood
     * <p>
     * Creation date: 13/05/2013 - 9:43:02 AM
     * 
     * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
     * @version 1.0
     */
    public class NeighborhoodExplorer implements Callable<VRPSolution> {
        private final SimpleParameters              mShakeParameters;
        private final VND                           mLS;
        private final INeighborhood<VRPSolution, ?> mShakeNeighborhood;
        private VRPSolution                         mStartSolution, mLastSolution;
        private final Stopwatch                     mStopwatch;

        /**
         * Creates a new <code>NeighborhoodExplorer</code>
         * 
         * @param neighborhood
         *            the neighborhood that will be used to <em>shake</em> the solution
         * @param seed
         *            the seed for the random stream
         * @author vpillac
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public NeighborhoodExplorer(INeighborhood<VRPSolution, ?> neighborhood, long seed) {
            super();
            mShakeNeighborhood = neighborhood;
            mLS = new VND(getInstance(),
                    ((GenericNeighborhood) getShakeNeighborhood()).getConstraintHandler(),
                    getRoutePool());

            mShakeParameters = new SimpleParameters(LSStrategy.RND_NON_IMPROVING,
                    Integer.MAX_VALUE, Integer.MAX_VALUE, seed);

            // Make sure the local search does not use the neighborhoods defined in the shake
            ListIterator<INeighborhood<VRPSolution, ?>> it = mLS.getNeighborhoods().listIterator();
            while (it.hasNext()) {
                Class<? extends INeighborhood> nClass = it.next().getClass();
                for (INeighborhood<VRPSolution, ?> shake : getNeighborhoods()) {
                    if (nClass == shake.getClass())
                        it.remove();
                    break;
                }
            }
            mStopwatch = new Stopwatch();
        }

        /**
         * Sets the start solution for the next call to {@link #call()} <br/>
         * Note that this will {@link VRPSolution#clone() clone} the {@code  startSolution}
         * 
         * @param startSolution
         * @author vpillac
         */
        public void setStartSolution(VRPSolution startSolution) {
            mStartSolution = startSolution.clone();
        }

        /**
         * Return the last solution found while exploring the attached neighborhood
         * 
         * @return the last solution found while exploring the attached neighborhood
         * @author vpillac
         */
        public VRPSolution getLastSolution() {
            return mLastSolution;
        }

        @Override
        public VRPSolution call() {
            mStopwatch.restart();

            // Clone the start solution
            mLastSolution = mStartSolution;

            // Shake
            getShakeNeighborhood().pertub(getInstance(), mLastSolution, mShakeParameters);
            VRPLogging.logOptResults("#" + getShakeNeighborhood().getShortName(), true, mStopwatch,
                    mLastSolution);

            // Execute the best improvement local search (VND)
            mLS.localSearch(mLastSolution);

            mStopwatch.stop();

            // Return the solution
            return mLastSolution;
        }

        public INeighborhood<VRPSolution, ?> getShakeNeighborhood() {
            return mShakeNeighborhood;
        }

    }
}
