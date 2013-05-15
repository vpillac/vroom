/* 
 * This file is part of the VRP 2013 Computational Aspects of Vehicle Routing course (http://www.ima.uco.fr/vrp2013/).
 * 
 * You can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or any later version.
 * 
 * Author: Victor Pillac - http://www.victorpillac.com
 */
package vrp2013.examples;

import java.util.concurrent.Callable;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.ImmutableRoute;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.util.HashRoutePool;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.utilities.BestKnownSolutions;
import vroom.common.utilities.ObjectWithIdSet;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.OptimizationSense;
import vrp2013.util.VRPLogging;
import vrp2013.util.VRPSolution;

/**
 * The class <code>ExampleBas</code> is the base implementation for all examples.
 * <p>
 * Creation date: 07/05/2013 - 5:46:12 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public abstract class ExampleBase implements Callable<VRPSolution> {

    static {
        HashRoutePool.sCountCollisions = true;
    }

    private final StaticInstance     mInstance;
    private final BestKnownSolutions mBKS;
    private final Stopwatch          mStopwatch;

    /**
     * Returns the VRP instance being solved
     * 
     * @return the VRP instance being solved
     */
    public StaticInstance getInstance() {
        return mInstance;
    }

    /**
     * Returns the best known solution dictionary.
     * 
     * @return the the best known solution dictionary
     */
    public BestKnownSolutions getBKS() {
        return mBKS;
    }

    /**
     * Return the main stopwatch for this example
     * 
     * @return the main stopwatch for this example
     */
    public Stopwatch getStopwatch() {
        return mStopwatch;
    }

    /**
     * Creates a new <code>ExampleBase</code>
     * 
     * @param instance
     * @param bks
     */
    public ExampleBase(StaticInstance instance, BestKnownSolutions bks) {
        mInstance = instance;
        mBKS = bks;
        mStopwatch = new Stopwatch();
    }

    /**
     * Log the results of an optimization algorithm
     * 
     * @param methodName
     *            the name of the optimization algorithm
     * @param solution
     *            the best solution found
     */
    public void logResult(String methodName, VRPSolution solution) {
        VRPLogging.getOptLogger().info("================================================");
        VRPLogging.getOptLogger().info("%s terminated after %s", methodName,
                getStopwatch().readTimeString(3, true, false));
        VRPLogging.getOptLogger().info("%s solution: %s", methodName, solution);
        VRPLogging.getOptLogger().info(
                "%s gap to best known solution: %.2f%%",
                methodName,
                getBKS().getGapToBKS(getInstance().getName(), solution.getCost(),
                        OptimizationSense.MINIMIZATION) * 100);
        VRPLogging.getOptLogger().info("================================================");
    }

    /**
     * Log the number of routes that have duplicates
     */
    public static void logPoolStatistics(String context, IRoutePool<INodeVisit> routePool) {
        ObjectWithIdSet<?>[] routeNodes = new ObjectWithIdSet<?>[routePool.size()];
        int i = 0;
        for (ImmutableRoute<INodeVisit> r : routePool.getAllRoutes()) {
            routeNodes[i++] = new ObjectWithIdSet<>(r.getNodeSequence());
        }

        int uniqueDuplicates = 0;
        int totalDuplicates = 0;
        for (int r = 0; r < routeNodes.length; r++) {
            if (routeNodes[r] != null) {
                boolean rHasDuplicate = false;
                for (int p = r + 1; p < routeNodes.length; p++) {
                    if (routeNodes[p] != null && routeNodes[r].equals(routeNodes[p])) {
                        if (!rHasDuplicate)
                            uniqueDuplicates++;
                        totalDuplicates++;
                        routeNodes[p] = null;
                        rHasDuplicate = true;
                    }

                }
            }
        }

        VRPLogging.getOptLogger().info("%s generated %s routes (%s hash collisions)", context,
                routePool.size(), routePool.getCollisionsCount());
        VRPLogging
                .getOptLogger()
                .info("%s unique routes, %s routes have at least one duplicate, %s duplicated routes in total",
                        routePool.size() - totalDuplicates, uniqueDuplicates, totalDuplicates);
    }

    public static void shutdown() {
        // Wait for logging messages
        Logging.awaitLogging(60000);
        // Force all threads to exit
        System.exit(0);
    }

    @Override
    public abstract VRPSolution call();

}
