/**
 * 
 */
package vroom.trsp.optimization.constructive;

import vroom.common.heuristics.alns.IRepair;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.alns.RepairRegret;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;

/**
 * <code>TRSPRepairConsHeur</code> is an implementation of {@link TRSPConstructiveHeuristic} based on a {@link IRepair}
 * instance
 * <p>
 * Creation date: Jun 7, 2011 - 11:13:27 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPRepairConsHeur extends TRSPConstructiveHeuristic {

    private final IRepair<TRSPSolution> mRepair;

    /**
     * Creates a new <code>TRSPRepairConsHeur</code>
     * 
     * @param instance
     * @param parameters
     * @param constraintHandler
     * @param costDelegate
     * @param repair
     */
    public TRSPRepairConsHeur(TRSPInstance instance, TRSPGlobalParameters parameters,
            TourConstraintHandler constraintHandler, TRSPCostDelegate costDelegate, IRepair<TRSPSolution> repair) {
        super(instance, parameters, constraintHandler, costDelegate);
        mRepair = repair;
        mRepair.initialize(instance);
    }

    /**
     * Creates a new <code>TRSPRepairConsHeur</code> with a default regret-2 repair heuristic
     * 
     * @param instance
     * @param parameters
     * @param constraintHandler
     * @param costDelegate
     */
    public TRSPRepairConsHeur(TRSPInstance instance, TRSPGlobalParameters parameters,
            TourConstraintHandler constraintHandler, TRSPCostDelegate costDelegate) {
        this(instance, parameters, constraintHandler, costDelegate, new RepairRegret(parameters, constraintHandler, 3,
                true));
    }

    @Override
    protected void initializeSolutionInternal(TRSPSolution sol) {
        SimpleParameters params = new SimpleParameters(null, Long.MAX_VALUE, Integer.MAX_VALUE, getParameters()
                .getInitRndStream());
        mRepair.repair(sol, null, params);
    }
}
