package vroom.optimization.online.jmsa.vrp.vrpsd;

import static vroom.common.heuristics.vls.VLSGlobalParameters.OPTIMIZATION_DIRECTION;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PARAM_INIT;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PARAM_LOCALSEARCH;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PARAM_PERTUBATION;

import java.util.ArrayList;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.GenericNeighborhoodHandler;
import vroom.common.heuristics.Identity;
import vroom.common.heuristics.GenericNeighborhoodHandler.Strategy;
import vroom.common.heuristics.vls.SimpleAcceptanceCriterion;
import vroom.common.heuristics.vls.VLSGlobalParameters;
import vroom.common.heuristics.vls.VLSParameters;
import vroom.common.heuristics.vls.VLSStateBase;
import vroom.common.heuristics.vls.VersatileLocalSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch;
import vroom.common.heuristics.vns.VariableNeighborhoodSearch.VNSVariant;
import vroom.common.heuristics.vrp.OrOptNeighborhood;
import vroom.common.heuristics.vrp.RelocateNeighborhood;
import vroom.common.heuristics.vrp.StringExchangeNeighborhood;
import vroom.common.heuristics.vrp.SwapNeighborhood;
import vroom.common.heuristics.vrp.TwoOptNeighborhood;
import vroom.common.heuristics.vrp.VRPParameters;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.util.SolutionChecker;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerBase;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerParam;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.MSAVRPSolutionFactory;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioGeneratorBase;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;
import vroom.optimization.online.jmsa.vrp.optimization.MSACWInitialization;
import vroom.optimization.online.jmsa.vrp.optimization.MSAFixedNodeConstraint;
import vroom.optimization.online.jmsa.vrp.optimization.VRPScenarioInstanceSmartAdapter;
import vroom.optimization.online.jmsa.vrp.optimization.VRPSmartInitialization;

/**
 * <code>VRPSDSVScenarioOptimizer</code> is a optimizer for the VRPSD with a single vehicle based on a VLS procedure.
 * <p>
 * It uses a Clark and Wright heuristic for initialization and a VNS for the local search.
 * <p/>
 * <p>
 * Creation date: 5-May-2010 9:48:13 a.m.
 * <p/>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <S>
 * @param <I>
 */

public class VRPSDSVScenarioOptimizer<S extends VRPScenario, I extends IInstance> extends ScenarioOptimizerBase<S> {

    private final VersatileLocalSearch<S>       mInitVLS;
    private final VLSGlobalParameters           mInitGlobParams;
    private final VLSParameters                 mInitVLSParams;

    private final VariableNeighborhoodSearch<S> mInitVNS;
    private final VRPParameters                 mInitVNSParams;

    private final VersatileLocalSearch<S>       mOptVLS;
    private final VLSGlobalParameters           mOptGlobParams;
    private final VLSParameters                 mOptVLSParams;

    private final VariableNeighborhoodSearch<S> mOptVNS;
    private final VRPParameters                 mOptVNSParams;

    private final ConstraintHandler<S>          mConstraintHandler;
    private final VRPSDAcceptanceCriterion      mAcceptanceCriterion;

    public VersatileLocalSearch<S> getOptHeuristic() {
        return mOptVLS;
    }

    public VersatileLocalSearch<S> getInitHeuristic() {
        return mInitVLS;
    }

    /**
     * @param componentManager
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public VRPSDSVScenarioOptimizer(ComponentManager<S, I> componentManager) {
        super(componentManager);

        mAcceptanceCriterion = new VRPSDAcceptanceCriterion(OptimizationSense.MINIMIZATION);
        mAcceptanceCriterion.setCostTolerance(0.10);
        mAcceptanceCriterion.setLoadThreshold(0.10);

        // Constraints
        this.mConstraintHandler = new ConstraintHandler<S>();
        this.mConstraintHandler.addConstraint(new CapacityConstraint<S>());
        this.mConstraintHandler.addConstraint(new MSAFixedNodeConstraint<S>());

        // -----------------------------------
        // OPTIMIZATION
        // -----------------------------------
        // Shake
        ArrayList<INeighborhood<S, ?>> shakeNeighborhoods = new ArrayList<INeighborhood<S, ?>>();
        // Local search
        ArrayList<INeighborhood<S, ?>> lsNeighborhoods = new ArrayList<INeighborhood<S, ?>>();

        // Swap
        lsNeighborhoods.add(new SwapNeighborhood<S>(this.mConstraintHandler));

        // Relocate
        RelocateNeighborhood<S> reloc = new RelocateNeighborhood<S>(mConstraintHandler, getSolutionFactory());
        reloc.setCardinality(1);
        // lsNeighborhoods.add(reloc);
        // shakeNeighborhoods.add(reloc);

        // 2-opt
        lsNeighborhoods.add(new TwoOptNeighborhood<S>(this.mConstraintHandler));

        // Or-opt
        OrOptNeighborhood<S> orOpt = new OrOptNeighborhood<S>(this.mConstraintHandler);
        orOpt.setMaxLength(3);
        lsNeighborhoods.add(orOpt);
        // reloc = new RelocateNeighborhood<S>(mConstraintHandler,
        // getSolutionFactory());
        // reloc.setCardinality(2);
        // shakeNeighborhoods.add(reloc);
        // reloc = new RelocateNeighborhood<S>(mConstraintHandler,
        // getSolutionFactory());
        // reloc.setCardinality(3);
        // shakeNeighborhoods.add(reloc);
        // reloc = new RelocateNeighborhood<S>(mConstraintHandler,
        // getSolutionFactory());
        // reloc.setCardinality(4);
        // shakeNeighborhoods.add(reloc);

        // String exchange
        StringExchangeNeighborhood<S> strExc = new StringExchangeNeighborhood<S>(this.mConstraintHandler);
        strExc.setMinLength(2);
        strExc.setMaxLength(3);
        shakeNeighborhoods.add(strExc);
        strExc = new StringExchangeNeighborhood<S>(this.mConstraintHandler);
        strExc.setMinLength(3);
        strExc.setMaxLength(4);
        shakeNeighborhoods.add(strExc);

        VariableNeighborhoodSearch<S> ls = VariableNeighborhoodSearch.newVNS(VNSVariant.VND,
                OptimizationSense.MINIMIZATION, null, getMSAProxy().getOptimizationRandomStream(), lsNeighborhoods);
        ((GenericNeighborhoodHandler) ls.getNeighHandler()).setStrategy(Strategy.EFFICIENCY_BASED);

        // Initialization
        // ---------------------------------
        this.mInitGlobParams = new VLSGlobalParameters();
        this.mInitVLSParams = new VLSParameters(this.mInitGlobParams, 1, 0, 0, 60000);
        this.mInitVLSParams.setStoppingCriterion(getComponentManager().getParentMSAProxy().newStoppingCriterion(
                mInitVLSParams.getStoppingCriterion(), true));
        this.mInitGlobParams.set(VLSGlobalParameters.ENABLE_CALLBACKS, false);

        // Initialization parameters
        this.mInitVNSParams = new VRPParameters(Integer.MAX_VALUE, Integer.MAX_VALUE, false, false, getMSAProxy()
                .getOptimizationRandomStream());
        // this.mInitVNSParams.setAcceptanceCriterion(mAcceptanceCriterion);
        this.mInitGlobParams.set(OPTIMIZATION_DIRECTION, -1);
        this.mInitGlobParams.set(PARAM_INIT, new SimpleParameters(LSStrategy.RND_FIRST_IMPROVEMENT, 10000,
                Integer.MAX_VALUE, getMSAProxy().getOptimizationRandomStream()));
        this.mInitGlobParams.set(PARAM_LOCALSEARCH, mInitVNSParams);
        this.mInitGlobParams.set(PARAM_PERTUBATION, new SimpleParameters(LSStrategy.RND_FIRST_IMPROVEMENT, 100, 6,
                getMSAProxy().getOptimizationRandomStream()));

        mInitVNS = VariableNeighborhoodSearch.newVNS(VariableNeighborhoodSearch.VNSVariant.GVNS,
                OptimizationSense.MINIMIZATION, ls, getMSAProxy().getOptimizationRandomStream(), shakeNeighborhoods);
        ((GenericNeighborhoodHandler) mInitVNS.getNeighHandler()).setStrategy(Strategy.SEQUENTIAL);
        this.mInitVNS.setAcceptanceCriterion(mAcceptanceCriterion);

        this.mInitVLS = new VersatileLocalSearch(this.mInitGlobParams, this.mInitVLSParams, VLSStateBase.class,
                new SimpleAcceptanceCriterion(this.mInitGlobParams), new MSACWInitialization(getMSAProxy()
                        .getOptimizationRandomStream()), mInitVNS, new Identity<VRPScenario>(), mConstraintHandler);
        this.mInitVLS.setAcceptanceCriterion(mAcceptanceCriterion);

        // Optimization
        // ---------------------------------
        this.mOptGlobParams = new VLSGlobalParameters();
        this.mOptVLSParams = new VLSParameters(mOptGlobParams, 3, 0, 0, 60000);
        this.mOptVLSParams.setStoppingCriterion(getComponentManager().getParentMSAProxy().newStoppingCriterion(
                mOptVLSParams.getStoppingCriterion(), true));
        this.mOptGlobParams.set(VLSGlobalParameters.ENABLE_CALLBACKS, false);

        mOptVNSParams = new VRPParameters(10000, 10000, false, false, getMSAProxy().getOptimizationRandomStream());
        // mOptVNSParams.setAcceptanceCriterion(mAcceptanceCriterion);
        this.mOptGlobParams.set(OPTIMIZATION_DIRECTION, -1);
        this.mOptGlobParams.set(PARAM_INIT, new SimpleParameters(LSStrategy.RND_FIRST_IMPROVEMENT, 10000,
                Integer.MAX_VALUE, getMSAProxy().getOptimizationRandomStream()));
        this.mOptGlobParams.set(PARAM_LOCALSEARCH, mOptVNSParams);
        this.mOptGlobParams.set(PARAM_PERTUBATION, new SimpleParameters(LSStrategy.RND_FIRST_IMPROVEMENT, 100, 6,
                getMSAProxy().getOptimizationRandomStream()));

        mOptVNS = VariableNeighborhoodSearch.newVNS(VariableNeighborhoodSearch.VNSVariant.GVNS,
                OptimizationSense.MINIMIZATION, ls, getMSAProxy().getOptimizationRandomStream(), shakeNeighborhoods);
        mOptVNS.setAcceptanceCriterion(mAcceptanceCriterion);

        ((GenericNeighborhoodHandler<S>) mOptVNS.getNeighHandler()).setStrategy(Strategy.SEQUENTIAL);
        ((GenericNeighborhoodHandler<S>) mOptVNS.getNeighHandler()).setResetStrategy(10000, 0.1);
        mOptVLS = new VersatileLocalSearch(mOptGlobParams, mOptVLSParams, VLSStateBase.class,
                new SimpleAcceptanceCriterion(this.mInitGlobParams), new VRPSmartInitialization(getMSAProxy()
                        .getOptimizationRandomStream()), mOptVNS, new Identity<S>(), mConstraintHandler);
        mOptVLS.setAcceptanceCriterion(mAcceptanceCriterion);
    }

    @Override
    public boolean initialize(S scenario, ScenarioOptimizerParam params) {
        scenario.acquireLock();

        boolean b = true;

        Stopwatch timer = new Stopwatch();
        timer.start();

        // Ignore empty scenarios
        if (scenario.getActualRequests().isEmpty() && scenario.getSampledRequests().isEmpty()) {
            VRPScenarioRoute route = new VRPScenarioRoute(scenario, scenario.getParentInstance().getFleet()
                    .getVehicle(0));
            route.appendNode(new VRPActualRequest(scenario.getParentInstance().getDepotsVisits().iterator().next()));
            route.appendNode(scenario.getParentInstance().getShrunkRequest(0));
            route.appendNode(new VRPActualRequest(scenario.getParentInstance().getDepotsVisits().iterator().next()));

            scenario.addRoute(route);
        } else {
            VersatileLocalSearch<S> solver = getInitHeuristic();
            solver.reset();
            // Update max time
            solver.getParameters().setMaxTime(params.getMaxTimePerScen());

            // Update the stopping criterion
            solver.getParameters().setStoppingCriterion(
                    getMSAProxy().newStoppingCriterion(solver.getParameters().getStoppingCriterion(),
                            params.isInterruptible()));

            for (VRPScenarioRoute r : scenario) {
                r.setAddAsObserver(false);
            }
            VRPScenarioInstanceSmartAdapter adapter = new VRPScenarioInstanceSmartAdapter(scenario);
            solver.setInstance(adapter);

            solver.run();

            VRPScenario bestSol = solver.getBestSolution();

            b = VRPSDScenarioUpdater.repairSolution(bestSol);

            if (b) {
                scenario.importScenario(bestSol);
                if (scenario.getRouteCount() == 0) {
                    MSALogging.getComponentsLogger().warn(
                            "VRPSDSVScenarioOptimizer.initialize: resulting scenario is empty: %s", scenario);
                    scenario.releaseLock();
                    return false;
                }
            } else {
                MSALogging.getComponentsLogger().warn(
                        "VRPSDSVScenarioOptimizer.initialize: resulting scenario is infeasible: %s", scenario);
                scenario.releaseLock();
                return false;
            }
        }

        // Check coherence
        String err = SolutionChecker.checkSolution(scenario, true, true, true);
        if (err != null) {
            MSALogging.getComponentsLogger().debug(
                    "VRPSDSVScenarioOptimizer.initialize: resulting scenario (%s) had inconsistencies: %s",
                    scenario.hashCode(), err);
        }

        // Check feasibility
        String infeas = this.mConstraintHandler.getInfeasibilityExplanation(scenario);
        if (infeas != null) {
            MSALogging.getComponentsLogger().debug(
                    "VRPSDSVScenarioOptimizer.initialize: resulting scenario (%s) is infeasible: %s",
                    scenario.hashCode(), infeas);
            b = false;
        }

        // Automatically monitor request updates
        if (b) {
            for (VRPScenarioRoute r : scenario) {
                r.setAddAsObserver(true);
            }
        }

        timer.stop();
        MSALogging.getComponentsLogger().lowDebug(
                "VRPSDSVScenarioOptimizer.initialize: New scenario initialized in %sms: %s", timer.readTimeMS(), scenario);

        scenario.releaseLock();

        return b;
    }

    @Override
    public boolean optimize(S scenario, ScenarioOptimizerParam params) {
        // Ignore empty scenarios
        if (scenario.getRouteCount() == 1 && scenario.getRoute(0).length() <= 4) {
            return true;
        }

        scenario.acquireLock();

        String err1 = SolutionChecker.checkSolution(scenario, true, true, true);
        if (err1 != null) {
            MSALogging.getComponentsLogger().debug(
                    "VRPSDSVScenarioOptimizer.optimize: (pre-opt) the scenario (%s) had inconsistencies: %s",
                    scenario.hashCode(), err1);
        }

        Stopwatch timer = new Stopwatch();
        timer.start();

        // mOptVNSParams.setMaxTime(params.getMaxTimePerScen() * 1000l);

        // VRPScenario bestSol = mOptVNS.perfomLocalSearch((MSAVRPInstance)
        // getComponentManager()
        // .getParentMSA().getInstance(), scenario, mOptVNSParams);

        VersatileLocalSearch<S> solver = getOptHeuristic();

        solver.reset();
        // Update max time
        solver.getParameters().setMaxTime(params.getMaxTimePerScen());

        // Update the stopping criterion
        solver.getParameters().setStoppingCriterion(
                getMSAProxy().newStoppingCriterion(solver.getParameters().getStoppingCriterion(),
                        params.isInterruptible()));

        VRPScenarioInstanceSmartAdapter adapter = new VRPScenarioInstanceSmartAdapter(scenario.clone());

        for (VRPScenarioRoute r : scenario) {
            r.setAddAsObserver(false);
        }
        solver.setInstance(adapter);

        solver.run();

        S bestSol = solver.getBestSolution();

        // Check coherence
        boolean feasible = true;
        // boolean improvement = bestSol.getCost() < scenario.getCost();
        boolean improvement = mAcceptanceCriterion.accept(scenario, bestSol);
        if (improvement) {
            feasible = VRPSDScenarioUpdater.repairSolution(bestSol);

            if (!feasible) {
                MSALogging.getComponentsLogger().warn(
                        "VRPSDSVScenarioOptimizer.optimize: resulting scenario is infeasible: %s", bestSol);
                bestSol.releaseLock();
                return VRPSDScenarioUpdater.repairSolution(scenario);
            }

            // Check coherence
            String err = SolutionChecker.checkSolution(bestSol, true, true, true);
            if (err != null) {
                MSALogging.getComponentsLogger().debug(
                        "VRPSDSVScenarioOptimizer.optimize: resulting scenario (%s) had inconsistencies: %s",
                        bestSol.hashCode(), err);
                if (SolutionChecker.checkUnservedCustomers(bestSol) != null) {
                    feasible = false;
                    return VRPSDScenarioUpdater.repairSolution(scenario);
                }
            }

            // Check feasibility
            String infeas = this.mConstraintHandler.getInfeasibilityExplanation(bestSol);
            if (infeas != null) {
                MSALogging.getComponentsLogger().debug(
                        "VRPSDSVScenarioOptimizer.optimize: resulting scenario (%s) is infeasible: %s (%s)",
                        bestSol.hashCode(), infeas, bestSol);
                feasible = false;
                return VRPSDScenarioUpdater.repairSolution(scenario);
            }

            // Automatically monitor request updates
            if (feasible && improvement) {
                scenario.importScenario(bestSol);

                scenario.resetNonImprovingCount();

                for (VRPScenarioRoute r : scenario) {
                    r.setAddAsObserver(true);
                }
            } else {
                scenario.incrementNonImprovingCount();
            }
        } else {
            scenario.incrementNonImprovingCount();
        }

        timer.stop();
        MSALogging.getComponentsLogger().lowDebug("VRPSDSVScenarioOptimizer.optimize: Scenario optimized in %sms %s",
                timer.readTimeMS(), scenario);

        scenario.releaseLock();

        return true;
    }

    @Override
    protected void finalize() throws Throwable {
        this.mInitVLS.destroy();
        super.finalize();
    }

    @Override
    public String toString() {
        return String.format("%s \nInit (%s):%s\nOpt (%s): %s", this.getClass().getSimpleName(), mInitVLSParams,
                mInitVLS, mOptVNSParams, mOptVNS);
    }

    protected MSAVRPSolutionFactory getSolutionFactory() {
        return ((VRPScenarioGeneratorBase<?>) getComponentManager().getScenarioGenerator()).getScenarioFactory();
    }

}// end VRPSDSVScenarioOptimizer