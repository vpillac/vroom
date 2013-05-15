package vroom.trsp;

import vroom.common.heuristics.ProcedureStatus;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.optimization.rch.TRSPRndConstructiveHeuristic;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

public class RCHSCSolverFeas extends RCHSCSolver {

    public RCHSCSolverFeas(TRSPInstance instance, TRSPGlobalParameters params) {
        super(instance, params);
    }

    @Override
    void generateToursSequential() {
        final double maxIt = getParams().get(TRSPGlobalParameters.RCH_MAX_IT);
        getMonitor().start();
        int samplesPerHeur = Math.max(getHeuristics().size(), (int) Math.ceil(maxIt / getHeuristics().size()));
        for (TRSPRndConstructiveHeuristic h : getHeuristics()) {
            for (int it = 0; it < samplesPerHeur; it++) {
                ProcedureStatus status = ProcedureStatus.INITIALIZED;
                // try {
                // Generate the tours
                status = h.call();
                // } catch (Exception e) {
                // TRSPLogging.getOptimizationLogger().exception("RCHSCSolver.generateTours", e);
                // }

                if (status == ProcedureStatus.TERMINATED) {
                    // Add the generated tours to the pool
                    int addedTours = getTourPool().add(h.getTourPool());
                    TRSPLogging.getOptimizationLogger().debug(
                            "RCHSC %s: %s added %s/%s tours to the pool, new size:%s", getMonitor(), h, addedTours,
                            h.getTourPool().size(), getTourPool().size());
                } else {
                    throw new IllegalStateException("Constructive heuristic returned an unsupported state: " + status);
                }

                getMonitor().iterationFinished();
            }
        }
    }

}
