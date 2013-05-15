/**
 *
 */
package vroom.common.heuristics.alns;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.LocalSearchBase;
import vroom.common.heuristics.alns.IDestroy.IDestroyResult;
import vroom.common.heuristics.utils.HeuristicsLogging;
import vroom.common.utilities.ProgressMonitor;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.callbacks.CallbackManagerDelegate;
import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.IComponentHandler;
import vroom.common.utilities.optimization.IComponentHandler.Outcome;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.OptimizationSense;

/**
 * <code>AdaptiveLargeNeighborhoodSearch</code> is a generic implementation of the ALNS algorithm as presented in
 * <p>
 * Ropke, S. & Pisinger, D.<br/>
 * An adaptive large neighborhood search heuristic for the pickup and delivery problem with time windows<br/>
 * Transportation Science, 2006, 40, 455-472
 * </p>
 * <p>
 * Creation date: May 12, 2011 - 1:15:15 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class AdaptiveLargeNeighborhoodSearch<S extends ISolution> extends LocalSearchBase<S> {

    /** The Constant LOGGER. */
    private final static LoggerHelper sLogger = HeuristicsLogging.getLogger("ALNS");

    /**
     * Getter for the logger used in this ALNS
     * 
     * @return the logger used in this ALNS
     */
    public static LoggerHelper getLogger() {
        return sLogger;
    }

    /** A callback handler for this procedure */
    private final CallbackManagerDelegate<AdaptiveLargeNeighborhoodSearch<S>, ALNSEventType> mCallbacks;

    CallbackManagerDelegate<AdaptiveLargeNeighborhoodSearch<S>, ALNSEventType> getCallbacks() {
        return mCallbacks;
    }

    /**
     * Register the given <code>callback</code> to the event <code>type</code><br/>
     * 
     * @param callback
     *            the callback that will be associated with <code>event</code>
     * @param type
     *            the event to which the given <code>callback</code> will be associated
     */
    public void registerCallback(ICallback<AdaptiveLargeNeighborhoodSearch<S>, ALNSEventType> callback,
            ALNSEventType type) {
        getCallbacks().registerCallback(callback, type);
    }

    /** the destroy components **/
    private final IComponentHandler<IDestroy<S>> mDestroyComponents;

    /**
     * Getter for the destroy components
     * 
     * @return the destroy components
     */
    public IComponentHandler<IDestroy<S>> getDestroyComponents() {
        return this.mDestroyComponents;
    }

    /** the repair components **/
    private final IComponentHandler<IRepair<S>> mRepairComponents;

    /**
     * Getter for the repair components
     * 
     * @return the repair components
     */
    public IComponentHandler<IRepair<S>> getRepairComponents() {
        return this.mRepairComponents;
    }

    /** the global parameters for this ALNS **/
    private final ALNSGlobalParameters mGlobalParameters;

    /**
     * Getter for the global parameters for this ALNS
     * 
     * @return the value of global parameters
     */
    public ALNSGlobalParameters getGlobalParameters() {
        return this.mGlobalParameters;
    }

    /** A progress monitor for the procedure */
    private ProgressMonitor mProgress;

    /**
     * Returns the progress monitor for this procedure
     * 
     * @return the progress monitor for this procedure
     */
    public ProgressMonitor getProgress() {
        return mProgress;
    }

    private IInstance mCurrentInstance;

    /**
     * Returns the instance currently solved
     * 
     * @return the instance currently solved
     */
    protected IInstance getCurrentInstance() {
        return mCurrentInstance;
    }

    /**
     * Creates a new <code>AdaptiveLargeNeighborhoodSearch</code>
     * 
     * @param optimizationSense
     *            the {@link OptimizationSense}
     * @param rndStream
     *            the random stream used in this ALNS
     * @param params
     *            global parameters for this ALNS
     * @param destroyComponents
     *            the {@link IDestroy destroy components} used in this ALNS
     * @param repairComponents
     *            the {@link IDestroy repair components} used in this ALNS
     */
    public AdaptiveLargeNeighborhoodSearch(OptimizationSense optimizationSense, RandomStream rndStream,
            ALNSGlobalParameters params, IComponentHandler<IDestroy<S>> destroyComponents,
            IComponentHandler<IRepair<S>> repairComponents) {
        super(optimizationSense, rndStream);

        mCallbacks = new CallbackManagerDelegate<AdaptiveLargeNeighborhoodSearch<S>, ALNSEventType>(
                ALNSEventType.class, "alns");

        mGlobalParameters = params;
        mDestroyComponents = destroyComponents;
        mRepairComponents = repairComponents;

        mCurrentInstance = null;
    }

    void initialize(IInstance instance, S solution, IParameters params) {
        setRunning();

        if (instance != mCurrentInstance) {
            for (IALNSComponent<?> comp : mDestroyComponents.getComponents())
                comp.initialize(instance);
            for (IALNSComponent<?> comp : mRepairComponents.getComponents())
                comp.initialize(instance);

            mDestroyComponents.initialize(instance);
            mRepairComponents.initialize(instance);
        }

        mCurrentInstance = instance;

        mProgress = new ProgressMonitor(params.getMaxIterations(), false);

        if (params.getAcceptanceCriterion() != null)
            setAcceptanceCriterion(params.getAcceptanceCriterion().clone());
        setStoppingCriterion(params.getStoppingCriterion().clone());
        // getAcceptanceCriterion().reset();
        // getAcceptanceCriterion().initialize();
        getStoppingCriterion().reset();
        getStoppingCriterion().init();

        getLogger().info("ALNS started with solution %s \t(params: %s)", solution, params);
        getLogger().info("Destroy: %s", mDestroyComponents);
        getLogger().info("Repair : %s", mRepairComponents);
        getLogger().debug("Stopping criterion: %s", getStoppingCriterion());

        getCallbacks().callbacks(
                new ALNSCallbackEvent<S>(ALNSEventType.STARTED, this, getTimer().readTimeMS(), getProgress()
                        .getIteration(), instance, solution));
    }

    @Override
    public S localSearch(IInstance instance, S solution, IParameters params) {
        if (params.getAcceptanceCriterion() != null)
            setAcceptanceCriterion(params.getAcceptanceCriterion());

        initialize(instance, solution, params);

        S best = solution;
        S current = solution;

        getProgress().start();
        Stopwatch itTimer = new Stopwatch();
        while (!getStoppingCriterion().isStopCriterionMet()) {
            itTimer.reset();
            itTimer.start();
            getLogger().lowDebug("ALNS %s: New iteration, stopping criterion: %s", getProgress(),
                    getStoppingCriterion());
            getCallbacks().callbacks(
                    new ALNSCallbackEvent<S>(ALNSEventType.IT_STARTED, this, getTimer().readTimeMS(), getProgress()
                            .getIteration(), current));

            @SuppressWarnings("unchecked")
            S tmp = (S) current.clone();

            // Select destroy operator
            IDestroy<S> destroy = mDestroyComponents.nextComponent();
            double sizeMin = getGlobalParameters().get(ALNSGlobalParameters.DESTROY_SIZE_RANGE)[0];
            double sizeMax = getGlobalParameters().get(ALNSGlobalParameters.DESTROY_SIZE_RANGE)[1];
            double size = sizeMin + params.getRandomStream().nextDouble() * (sizeMax - sizeMin);
            getLogger().lowDebug("ALNS %s: Selecting destroy %s (size=%.2f)", getProgress(), destroy, size);
            // Destroy solution
            IDestroyResult<S> result = destroy.destroy(tmp, params, size);
            getLogger().lowDebug("ALNS %s: Destroy result: %s ", getProgress(), result);
            getCallbacks().callbacks(
                    new ALNSCallbackEvent<S>(ALNSEventType.DESTROYED, this, getTimer().readTimeMS(), getProgress()
                            .getIteration(), best, current, tmp, destroy, result));

            // Select repair operator
            IRepair<S> repair = mRepairComponents.nextComponent();
            getLogger().lowDebug("ALNS %s: Selecting repair  %s", getProgress(), repair);
            // Repair solution
            boolean repaired = repair.repair(tmp, result, params);
            getCallbacks().callbacks(
                    new ALNSCallbackEvent<S>(ALNSEventType.REPAIRED, this, getTimer().readTimeMS(), getProgress()
                            .getIteration(), best, current, tmp, repair, repaired));
            if (isCheckSolutionAfterMove()) {
                String err = checkSolution(tmp);
                if (!err.isEmpty()) {
                    getLogger().warn("ALNS %s: Infeasible temporary solution: %s", getProgress(), err);
                }
            }
            itTimer.stop();

            // FIXME Apply an optional post-repair local search

            double improvement = getOptimizationSense().getImprovement(current.getObjectiveValue(),
                    tmp.getObjectiveValue());

            if (!repaired) {
                getLogger().lowDebug("ALNS %s: Unable to fully repair solution, iteration (sol:%s d:%s r:%s)",
                        getProgress(), tmp, destroy, repair);
            }

            // Test the solution and update the acceptance criterion
            boolean accept = getAcceptanceCriterion().accept(current, tmp);
            boolean compUpdated = false;
            if (getOptimizationSense().isBetter(best.getObjectiveValue(), tmp.getObjectiveValue(), false) && accept) {
                // A new best solution was found
                best = tmp;
                current = tmp;

                // Update the component evaluation
                compUpdated |= mDestroyComponents.updateStats(destroy, improvement, itTimer.readTimeMS(), getProgress()
                        .getIteration(), Outcome.NEW_BEST);
                compUpdated |= mRepairComponents.updateStats(repair, improvement, itTimer.readTimeMS(), getProgress()
                        .getIteration(), Outcome.NEW_BEST);

                getLogger().debug("ALNS %s: New best    [%.2f] (d:%s,r:%s)", getProgress(),
                        current.getObjectiveValue(), destroy, repair);
                getCallbacks().callbacks(
                        new ALNSCallbackEvent<S>(ALNSEventType.SOL_NEW_BEST, this, getTimer().readTimeMS(),
                                getProgress().getIteration(), best));
            } else if (accept) {
                // The new solution is accepted as current solution
                current = tmp;

                // Update the component evaluation
                compUpdated |= mDestroyComponents.updateStats(destroy, improvement, itTimer.readTimeMS(), getProgress()
                        .getIteration(), Outcome.ACCEPTED);
                compUpdated |= mRepairComponents.updateStats(repair, improvement, itTimer.readTimeMS(), getProgress()
                        .getIteration(), Outcome.ACCEPTED);

                getLogger().debug("ALNS %s: New current [%.2f] (d:%s,r:%s)", getProgress(),
                        current.getObjectiveValue(), destroy, repair);
                getCallbacks().callbacks(
                        new ALNSCallbackEvent<S>(ALNSEventType.SOL_NEW_CURRENT, this, getTimer().readTimeMS(),
                                getProgress().getIteration(), best, current));
            } else {
                getLogger().lowDebug("ALNS %s: Solution rejected    (d:%s,r:%s) %s", getProgress(), destroy, repair,
                        tmp);

                // Update the component evaluation
                compUpdated |= mDestroyComponents.updateStats(destroy, improvement, itTimer.readTimeMS(), getProgress()
                        .getIteration(), Outcome.REJECTED);
                compUpdated |= mRepairComponents.updateStats(repair, improvement, itTimer.readTimeMS(), getProgress()
                        .getIteration(), Outcome.REJECTED);
                getCallbacks().callbacks(
                        new ALNSCallbackEvent<S>(ALNSEventType.SOL_REJECTED, this, getTimer().readTimeMS(),
                                getProgress().getIteration(), best, current, tmp));
            }

            if (compUpdated)
                getCallbacks().callbacks(
                        new ALNSCallbackEvent<S>(ALNSEventType.COMP_UPDATED, this, getTimer().readTimeMS(),
                                getProgress().getIteration(), best, current, tmp, itTimer.getReadOnlyStopwatch()));

            // Update stopping criterion
            getStoppingCriterion().update(current);
            getCallbacks().callbacks(
                    new ALNSCallbackEvent<S>(ALNSEventType.IT_FINISHED, this, getTimer().readTimeMS(), getProgress()
                            .getIteration(), best, current, tmp, itTimer.getReadOnlyStopwatch()));

            getProgress().iterationFinished();
        }
        getProgress().stop();
        //

        setStopped();
        getCallbacks().callbacks(
                new ALNSCallbackEvent<S>(ALNSEventType.FINISHED, this, (long) getTimer().readTimeMS(), getProgress()
                        .getIteration(), instance, best));

        return best;
    }

    /**
     * This methods should be called when the ALNS will no longer be used. It stops the callback threads.
     */
    public void stop() {
        getCallbacks().stop();
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    };

    @Override
    public String toString() {
        return String.format("D: %s\nR: %s\nParams: %s\nStop: %s\nAccept:%s", getDestroyComponents(),
                getRepairComponents(), getGlobalParameters(), getStoppingCriterion(), getAcceptanceCriterion());
    }

    /**
     * Dispose this object to help the garbage collector freeing up memory
     */
    @Override
    public void dispose() {
        mDestroyComponents.dispose();
        mRepairComponents.dispose();
    }
}
