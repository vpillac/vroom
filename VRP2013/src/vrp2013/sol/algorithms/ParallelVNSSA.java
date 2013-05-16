package vrp2013.sol.algorithms;

import java.util.Map;
import java.util.Map.Entry;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.RouteBase;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.optimization.IAcceptanceCriterion;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.SAAcceptanceCriterion;
import vrp2013.util.BatchExecutor.Result;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;
import vrp2013.util.VRPUtilities;

/**
 * The class <code>ParallelVND</code> is an implementation of VND that explores neighborhoods in parallel.
 * <p>
 * Creation date: 13/05/2013 - 9:36:40 AM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class ParallelVNSSA extends ParallelVNS {

    private IAcceptanceCriterion mAccept;

    public ParallelVNSSA(IVRPInstance instance, ConstraintHandler<VRPSolution> constraintHandler,
            IRoutePool<INodeVisit> routePool) {
        super(instance, constraintHandler, routePool);
    }

    @Override
    public boolean localSearch(final VRPSolution solution) {
        VRPLogging.logOptResults("ls-init", false, getNeighStopwatch(), solution);
        Stopwatch mainSW = new Stopwatch();
        mainSW.start();
        int it = 0;

        mAccept = new SAAcceptanceCriterion(OptimizationSense.MINIMIZATION, VRPUtilities
                .getInstance().getRandomStream(), solution.getCost(), 0.5, 0.5, 100, 0.001, false);

        setBestSolution(solution);

        // Start with the current solution
        VRPSolution bestNeighbor = solution;
        // The current solution is the best neighbor from the previous iteration
        VRPSolution currentSolution = solution;
        // A flag set to true if the solution was changed in the last iteration
        boolean changedLastIt = true;
        // A flag set to true if the solution was changed at least once
        boolean changedOverall = false;
        while (changedLastIt) {
            it++;
            // The explorer that found the best solution (for logging purposes)
            NeighborhoodExplorer bestExplorer = null;
            changedLastIt = false;
            getNeighStopwatch().restart();

            for (NeighborhoodExplorer explorer : getExplorers()) {
                // Set the starting solution for this explorer
                explorer.setStartSolution(currentSolution);
            }

            Map<NeighborhoodExplorer, Result<VRPSolution>> results = null;
            // Explore the neighborhoods in parallel threads
            results = getExecutor().submitBatchAndWait(getExplorers());
            // Find the best neighbor in the results
            bestNeighbor = null;
            for (Entry<NeighborhoodExplorer, Result<VRPSolution>> entry : results.entrySet()) {
                if (bestNeighbor == null
                        || OptimizationSense.MINIMIZATION.isBetter(bestNeighbor, entry.getValue()
                                .get())) {
                    bestNeighbor = entry.getValue().get();
                    bestExplorer = entry.getKey();
                }
            }

            if (mAccept.accept(currentSolution, bestNeighbor)) {
                changedLastIt = true;
                changedOverall = true;
                currentSolution = bestNeighbor;
                VRPLogging.logOptResults(bestExplorer.getShakeNeighborhood().getShortName() + "*",
                        changedLastIt, getNeighStopwatch(), bestNeighbor);

                VRPLogging.getOptLogger().debug("[%9s] It: %5s Time:%s - obj=%.2f", "current", it,
                        mainSW.readTimeString(3, true, false), currentSolution.getCost());
            }

            getNeighStopwatch().stop();
            if (currentSolution != null
                    && OptimizationSense.MINIMIZATION.isBetter(getBestSolution(), currentSolution)) {
                setBestSolution(currentSolution);

                VRPLogging.getOptLogger().debug("[%9s] It: %5s Time:%s - obj=%.2f", "new best", it,
                        mainSW.readTimeString(3, true, false), getBestSolution().getCost());
            }
        }

        // Copy the best solution to the solution passed as argument
        if (changedOverall) {
            solution.clear();
            for (RouteBase r : bestNeighbor)
                if (r.length() > 2)
                    solution.addRoute(r);
        }

        mainSW.stop();

        return changedOverall;
    }

}
