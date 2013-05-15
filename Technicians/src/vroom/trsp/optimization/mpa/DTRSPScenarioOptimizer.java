/**
 * 
 */
package vroom.trsp.optimization.mpa;

import static vroom.trsp.util.TRSPGlobalParameters.ALNS_MAX_IT;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_MAX_TIME;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_SA_ALPHA;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_SA_P;
import static vroom.trsp.util.TRSPGlobalParameters.ALNS_SA_W;
import vroom.common.heuristics.alns.ALNSGlobalParameters;
import vroom.common.heuristics.alns.AdaptiveLargeNeighborhoodSearch;
import vroom.common.heuristics.alns.IDestroy;
import vroom.common.heuristics.alns.IRepair;
import vroom.common.heuristics.utils.HeuristicsLogging;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IComponentHandler;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.RndComponentHanlder;
import vroom.common.utilities.optimization.SAAcceptanceCriterion;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.common.utilities.optimization.SimpleStoppingCriterion;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerBase;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerParam;
import vroom.trsp.ALNSSCSolver;
import vroom.trsp.MPASolver;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.optimization.alns.RepairRndRegret;
import vroom.trsp.optimization.constructive.TRSPConstructiveHeuristic;
import vroom.trsp.optimization.constructive.TRSPRepairConsHeur;
import vroom.trsp.optimization.localSearch.TRSPCompositeNeighborhood;
import vroom.trsp.optimization.localSearch.TRSPRelocate;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>DTRSPScenarioOptimizer</code> is a scenario optimizer for the D-TRSP
 * <p>
 * Creation date: Feb 7, 2012 - 11:01:36 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPScenarioOptimizer extends ScenarioOptimizerBase<DTRSPSolution> {

    /** A flag to toggle mSolution checking after each move */
    private static boolean sCheckSolutionAfterMove = false;

    /**
     * Setter for <code>checkSolutionAfterMove</code>
     * 
     * @param checkSolutionAfterMove
     *            the checkSolutionAfterMove to set
     */
    public static void setCheckSolutionAfterMove(boolean checkSolutionAfterMove) {
        sCheckSolutionAfterMove = checkSolutionAfterMove;
        if (sCheckSolutionAfterMove) {
            HeuristicsLogging
                    .getNeighborhoodsLogger()
                    .warn("DTRSPScenarioOptimizer.CheckSolutionAfterMove is set to true, set to false to increase performance (set in %s)",
                            Thread.currentThread().getStackTrace()[2]);
        }
    }

    private final MPASolver mSolver;

    /**
     * Return a reference to the parent {@link MPASolver}
     * 
     * @return a reference to the parent {@link MPASolver}
     */
    protected MPASolver getSolver() {
        return mSolver;
    }

    private final TRSPConstructiveHeuristic               mConsHeur;

    private AdaptiveLargeNeighborhoodSearch<TRSPSolution> mALNS;
    private SimpleParameters                              mALNSParams;

    private VariableNeighborhoodSearch<TRSPSolution>      mVNS;
    private SimpleParameters                              mVNSParams;

    private TRSPInstance getInstance() {
        return (TRSPInstance) getMSAProxy().getInstance();
    }

    public DTRSPScenarioOptimizer(ComponentManager<DTRSPSolution, ?> componentManager) {
        super(componentManager);

        mSolver = getMSAProxy().getParameters().get(MPASolver.TRSP_MPA_SOLVER);

        // Setup the constructive heuristic
        // ----------------------------------------
        mConsHeur = new TRSPRepairConsHeur(getInstance(), getSolver().getParams(), getSolver()
                .getTourCtrHandler(), null, new RepairRndRegret(getSolver().getParams(),
                getSolver().getTourCtrHandler(), 2, getMSAProxy().getOptimizationRandomStream()));
        // ----------------------------------------

        setupALNS();
        setupVNS();
    }

    private void setupALNS() {
        // Setup the LNS
        // ----------------------------------------
        IComponentHandler<IDestroy<TRSPSolution>> destroyComponents = new RndComponentHanlder<IDestroy<TRSPSolution>>(
                getSolver().getParams().getALNSRndStream(), ALNSSCSolver.newDestroySet(getSolver()
                        .getParams()), false);
        IComponentHandler<IRepair<TRSPSolution>> repairComponents = new RndComponentHanlder<IRepair<TRSPSolution>>(
                getSolver().getParams().getALNSRndStream(), ALNSSCSolver.newRepairSet(getSolver()
                        .getTourCtrHandler(), getSolver().getParams()), false);
        // IComponentHandler<IDestroy<TRSPSolution>> destroyComponents = new
        // RndComponentHanlder<IDestroy<TRSPSolution>>(
        // getSolver().getParams().getALNSRndStream(),
        // Collections.<IDestroy<TRSPSolution>> singleton(new DestroyRandom()), false);
        // IComponentHandler<IRepair<TRSPSolution>> repairComponents = new RndComponentHanlder<IRepair<TRSPSolution>>(
        // getSolver().getParams().getALNSRndStream(),
        // Collections.<IRepair<TRSPSolution>> singleton(new RepairRegret(getSolver().getParams(), getSolver()
        // .getSolCtrHandler().getTourConstraintHandler(), 1, false)), false);

        ALNSGlobalParameters alnsParams = new ALNSGlobalParameters();
        alnsParams.set(ALNSGlobalParameters.DESTROY_SIZE_RANGE, new double[] {
                getSolver().getParams().get(TRSPGlobalParameters.ALNS_XI_MIN),
                getSolver().getParams().get(TRSPGlobalParameters.ALNS_XI_MAX) });

        mALNS = new AdaptiveLargeNeighborhoodSearch<TRSPSolution>(OptimizationSense.MINIMIZATION,
                getSolver().getParams().getALNSRndStream(), alnsParams, destroyComponents,
                repairComponents);

        mALNSParams = new SimpleParameters(LSStrategy.DET_BEST_IMPROVEMENT, getSolver().getParams()
                .get(ALNS_MAX_TIME), getSolver().getParams().get(ALNS_MAX_IT), getSolver()
                .getParams().getALNSRndStream());
        // ----------------------------------------
    }

    @SuppressWarnings("unchecked")
    private void setupVNS() {
        mVNS = VariableNeighborhoodSearch.newVNS(VNSVariant.VND, OptimizationSense.MINIMIZATION,
                null, getMSAProxy().getOptimizationRandomStream(), //
                new TRSPRelocate(getSolver().getSolCtrHandler(), getSolver().getTourCtrHandler()),// ,
                new TRSPCompositeNeighborhood<TRSPTwoOptMove, TRSPTwoOpt>(getSolver()
                        .getSolCtrHandler(), new TRSPTwoOpt(getSolver().getTourCtrHandler())) //
                );

        mVNSParams = new SimpleParameters(LSStrategy.DET_BEST_IMPROVEMENT, getSolver().getParams()
                .get(ALNS_MAX_TIME), getSolver().getParams().get(ALNS_MAX_IT), getSolver()
                .getParams().getALNSRndStream());
    }

    private void optimize(DTRSPSolution scenario, int maxIt, int maxTime) {
        // if (scenario.getNonImprovingCount() > 5)
        optimizeALNS(scenario, maxIt, maxTime);
        // else
        // optimizeVNS(scenario, maxIt, maxTime);
    }

    private void optimizeALNS(DTRSPSolution scenario, int maxIt, int maxTime) {
        mALNSParams.setAcceptanceCriterion(new SAAcceptanceCriterion(
                OptimizationSense.MINIMIZATION, getSolver().getParams().getALNSRndStream(),
                scenario.getObjectiveValue(), getSolver().getParams().get(ALNS_SA_W), getSolver()
                        .getParams().get(ALNS_SA_P), mALNSParams.getMaxIterations(), getSolver()
                        .getParams().get(ALNS_SA_ALPHA), true));
        mALNSParams.setStoppingCriterion(getMSAProxy().newStoppingCriterion(
                new SimpleStoppingCriterion(maxTime, maxIt), true));

        TRSPSolution sol = mALNS.localSearch(getInstance(), scenario, mALNSParams);
        checkScenario(sol, "optimize-?");
        scenario.importSolution(sol);
    }

    private void optimizeVNS(DTRSPSolution scenario, int maxIt, int maxTime) {
        mVNSParams.setStoppingCriterion(getMSAProxy().newStoppingCriterion(
                new SimpleStoppingCriterion(maxTime, maxIt), true));

        TRSPSolution sol = mVNS.localSearch(getInstance(), scenario, mVNSParams);
        scenario.importSolution(sol);
    }

    @Override
    public boolean initialize(DTRSPSolution scenario, ScenarioOptimizerParam params) {
        mConsHeur.initializeSolution(scenario);
        boolean initOk = checkScenario(scenario, "initialize-init");

        scenario.setCostDelegate(getSolver().getParams().newALNSCostDelegate(
                getSolver().getInstance().getSimulator().getCurrentSolution()));
        scenario.getCostDelegate().setPenalize(true);
        scenario.getCostDelegate().setUnservedPenalty(
                getSolver().getInstance().getSimulator().getCurrentSolution(),
                getSolver().getParams().get(TRSPGlobalParameters.ALNS_OBJ_GAMMA) * 100
                        / scenario.getInstance().getReleasedRequests().size());

        if (initOk) {
            optimize(scenario, 1000, params.getMaxTime());

            return checkScenario(scenario, "initialize");
        } else
            return false;

    }

    @Override
    public boolean optimize(DTRSPSolution scenario, ScenarioOptimizerParam params) {
        double cost = scenario.getObjectiveValue();
        scenario.getCostDelegate().setUnservedPenalty(
                scenario,
                getSolver().getParams().get(TRSPGlobalParameters.ALNS_OBJ_GAMMA) * 100
                        / scenario.getInstance().getReleasedRequests().size());
        checkScenario(scenario, "optimize-init");
        optimize(scenario, 5000, params.getMaxTimePerScen());
        if (scenario.getObjectiveValue() < cost)
            scenario.resetNonImprovingCount();
        else
            scenario.incrementNonImprovingCount();

        return checkScenario(scenario, "optimize") && scenario.getUnservedCount() == 0;
    }

    private final static TRSPSolutionChecker SOL_CHECK = new TRSPSolutionChecker(false);

    /**
     * Check a scenario coherence (only is {@link #setCheckSolutionAfterMove(boolean) CheckSolutionAfterMove} is set to
     * {@code true}
     * 
     * @param solution
     * @param context
     * @return {@code true} if the scenario is coherent, {@code false} if it has problems
     */
    private boolean checkScenario(TRSPSolution solution, String context) {
        if (sCheckSolutionAfterMove) {
            if (solution.getUnservedCount() > 0 && !context.endsWith("-init"))
                TRSPLogging
                        .getOptimizationLogger()
                        .warn("DTRSPScenarioOptimizer.%s: incoherencies detected in scenario %s unserved requests %s",
                                context, solution.hashCode(),
                                Utilities.toShortString(solution.getUnservedRequests()));
            String err = SOL_CHECK.checkSolution(solution);
            if (err.length() > 0) {
                TRSPLogging.getOptimizationLogger().warn(
                        "DTRSPScenarioOptimizer.%s: incoherencies detected in scenario %s (%s)",
                        context, solution.hashCode(), err);
                return false;
            }
        }
        return true;
    }
}
