package vroom.common.heuristics.vls;

import static vroom.common.heuristics.vls.VLSGlobalParameters.ACCEPTANCE_CRITERIA_CLASS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.INITIALIZATION_CLASS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.LOCAL_SEARCH_CLASS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.PERTUBATION_CLASS;
import static vroom.common.heuristics.vls.VLSGlobalParameters.STATE_CLASS;
import static vroom.common.heuristics.vls.VLSPhase.ELS;
import static vroom.common.heuristics.vls.VLSPhase.GRASP;
import static vroom.common.heuristics.vls.VLSPhase.ILS;

import java.util.Collection;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.IInitialization;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.callbacks.CallbackEventBase;
import vroom.common.utilities.callbacks.CallbackManagerDelegate;
import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.ssj.RandomSourceBase;

/**
 * The Class <code>VersatileLocalSearch</code> is a generic implementation of a GRASPx[ILS,ELS] procedure.
 * <p>
 * Creation date: Apr 26, 2010 - 10:11:57 a.m.
 * 
 * @param <S>
 *            the generic type
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VersatileLocalSearch<S extends ISolution> extends RandomSourceBase implements
        Runnable, IInitialization<S> {

    /** The current state. */
    private final IVLSState<S>      mState;

    /** The acceptance criteria. */
    private IVLSAcceptanceCriterion mAcceptanceCriterion;

    /**
     * Getter for <code>AcceptanceCriterion</code>.
     * 
     * @return the AcceptanceCriterion
     */
    public IVLSAcceptanceCriterion getAcceptanceCriterion() {
        return mAcceptanceCriterion;
    }

    /**
     * Setter for <code>AcceptanceCriterion</code>.
     * 
     * @param acceptanceCriterion
     *            the new acceptance criterion
     */
    public void setAcceptanceCriterion(IVLSAcceptanceCriterion acceptanceCriterion) {
        mAcceptanceCriterion = acceptanceCriterion;
    }

    /** The initialization. */
    private final IInitialization<S>   mInitialization;

    /** The local search. */
    private final ILocalSearch<S>      mLocalSearch;

    /** The perturbation. */
    private final IVLSPertubation<S>   mPerturbation;

    /** The constraint handler used in this VLS procedure *. */
    private final ConstraintHandler<S> mConstraintHandler;

    /**
     * Getter for the constraint handler.
     * 
     * @return The constraint handler used in this VLS procedure
     */
    public ConstraintHandler<S> getConstraintHandler() {
        return this.mConstraintHandler;
    }

    /** The parameters for this VLS procedure. */
    private final VLSGlobalParameters mGlobalParameters;

    /**
     * Getter for the global parameters used in this procedure.
     * 
     * @return the global parameters used in this procedure
     */
    public final VLSGlobalParameters getGlobalParameters() {
        return mGlobalParameters;
    }

    /** The parameters used in the VLS *. */
    private final VLSParameters mParameters;

    /**
     * Getter for the VLS parameters : The parameters used in the VLS.
     * 
     * @return the value of the VLS parameters
     */
    public VLSParameters getParameters() {
        return this.mParameters;
    }

    /** A flag to determine the running state of the procedure. */
    private boolean mRunning;

    /**
     * Getter for <code>running</code> flag.
     * 
     * @return <code>true</code> if the heuristic is currently running
     */
    public boolean isRunning() {
        return mRunning;
    }

    /** The instance on which the heuristic will be run. */
    private IInstance                                                                 mInstance;

    /** A callback manager. */
    private final CallbackManagerDelegate<VersatileLocalSearch<S>, VLSCallbackEvents> mCallbackDelegate;

    /** The Timer. */
    private final Stopwatch                                                           mTimer;

    /**
     * Instantiates a new versatile local search.
     * 
     * @param parameters
     *            the global parameters from which this instance will be initialized
     */
    public VersatileLocalSearch(VLSGlobalParameters parameters) {
        mGlobalParameters = parameters;

        mConstraintHandler = new ConstraintHandler<S>();

        Collection<ParameterKey<?>> missingParams = mGlobalParameters.checkRequiredParameters();
        if (!missingParams.isEmpty()) {
            throw new IllegalStateException("Some required parameters are missing: "
                    + missingParams.toString());
        }

        mState = getGlobalParameters().newInstance(STATE_CLASS, this);
        mState.reset();

        setAcceptanceCriterion((IVLSAcceptanceCriterion) getGlobalParameters().newInstance(
                ACCEPTANCE_CRITERIA_CLASS, getGlobalParameters()));
        mInitialization = getGlobalParameters().newInstance(INITIALIZATION_CLASS,
                getGlobalParameters());
        mLocalSearch = getGlobalParameters().newInstance(LOCAL_SEARCH_CLASS, getGlobalParameters());
        mPerturbation = getGlobalParameters().newInstance(PERTUBATION_CLASS, getGlobalParameters());

        if (parameters.get(VLSGlobalParameters.ENABLE_CALLBACKS)) {
            mCallbackDelegate = new CallbackManagerDelegate<VersatileLocalSearch<S>, VLSCallbackEvents>(
                    VLSCallbackEvents.class, "vls");
        } else {
            mCallbackDelegate = null;
        }

        mParameters = new VLSParameters(parameters);

        mTimer = new Stopwatch();
    }

    /**
     * Creates a new <code>VersatileLocalSearch</code> with the given components.
     * 
     * @param parameters
     *            the global parameters for this instance
     * @param params
     *            the params
     * @param stateClass
     *            the class used to describe the state of the procedure
     * @param AcceptanceCriterion
     *            the acceptance criteria that will be used in this procedure
     * @param initialization
     *            the instance of {@link IInitialization} responsible for the initialization of new solutions
     * @param localSearch
     *            the instance of {@link ILocalSearch} that will perform local searches on solutions
     * @param perturbation
     *            the instance of {@link IVLSPertubation} that will be used to introduce variability in solutions
     * @param ctrHandler
     *            the {@link ConstraintHandler} to be used in this procedure
     */
    @SuppressWarnings("unchecked")
    public VersatileLocalSearch(VLSGlobalParameters parameters, VLSParameters params,
            @SuppressWarnings("rawtypes") Class<? extends IVLSState> stateClass,
            IVLSAcceptanceCriterion AcceptanceCriterion, IInitialization<S> initialization,
            ILocalSearch<S> localSearch, IVLSPertubation<S> perturbation,
            ConstraintHandler<S> ctrHandler) {
        mGlobalParameters = parameters;

        if (ctrHandler == null) {
            ctrHandler = new ConstraintHandler<S>();
        }
        mConstraintHandler = ctrHandler;

        mParameters = params;
        mGlobalParameters.set(VLSGlobalParameters.NS, params.getNS());
        mGlobalParameters.set(VLSGlobalParameters.NC, params.getNC());
        mGlobalParameters.set(VLSGlobalParameters.NI, params.getNI());
        mGlobalParameters.set(VLSGlobalParameters.VLS_MAX_TIME, params.getMaxTime());

        mState = Utilities.newInstance(stateClass, this);
        mState.reset();

        setAcceptanceCriterion(AcceptanceCriterion);
        mInitialization = initialization;
        mLocalSearch = localSearch;
        mPerturbation = perturbation;

        if (parameters.get(VLSGlobalParameters.ENABLE_CALLBACKS)) {
            mCallbackDelegate = new CallbackManagerDelegate<VersatileLocalSearch<S>, VLSCallbackEvents>(
                    VLSCallbackEvents.class, "vls");
        } else {
            mCallbackDelegate = null;
        }

        mTimer = new Stopwatch();
    }

    /**
     * Sets the instance on which the heuristic will be run.
     * 
     * @param instance
     *            the new instance
     */
    public void setInstance(IInstance instance) {
        if (isRunning()) {
            throw new IllegalStateException(
                    "Cannot set the instance while the heuristic is running");
        }
        mInstance = instance;
        mState.reset();
    }

    /**
     * Getter for the <code>instance</code>.
     * 
     * @return the instance on which the heuristic will be run
     */
    public IInstance getInstance() {
        return mInstance;
    }

    /**
     * Gets the best mSolution.
     * 
     * @return the best mSolution
     */
    public S getBestSolution() {
        return getState().getOverallBestSolution();
    }

    /**
     * Gets the current state.
     * 
     * @return the current state
     */
    public IVLSState<S> getState() {
        return mState;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        if (getInstance() == null) {
            throw new IllegalStateException("The working instance has not been set");
        }

        mRunning = true;

        newSolution(null, getInstance(), getParameters());

        VLSLogging.getProcedureLogger().info(
                "VLS procedure terminated after %sms, best mSolution: %s", mTimer.readTimeMS(),
                getBestSolution());

        stop();
    }

    /**
     * Performs an iteration of the grasp loop.
     * 
     * @param graspCount
     *            this iteration of the grasp loop
     */
    protected void graspIteration(int graspCount) {
        VLSLogging.getProcedureLogger().debug("GRASP loop iteration #%s", graspCount);

        getState().setCurrentPhase(GRASP);

        Stopwatch detail = new Stopwatch();
        Stopwatch timeGRASP = new Stopwatch();
        timeGRASP.start();
        detail.start();
        // TODO add timer & callback
        S sol = mInitialization.newSolution(getState(), getInstance(), getParameters()
                .getInitParams());

        VLSLogging.getProcedureLogger().debug("GRASP #%s> initialization (%sms)\t - %s",
                graspCount, detail.readTimeMS(), sol);

        detail.restart();
        // TODO add timer & callback
        // TODO add pre-LS action? (eg Split)
        // TODO add timer & callback
        sol = mLocalSearch.localSearch(getInstance(), sol, getParameters().getLSParameters());
        VLSLogging.getProcedureLogger().debug("GRASP #%s> local search   (%sms)\t - %s",
                graspCount, detail.readTimeMS(), sol);
        // TODO add timer & callback
        // TODO add post LS action? (eg Concat)

        // First iteration, initializing the best GRASP mSolution
        if (graspCount == 0) {
            getState().solutionAccepted(sol, GRASP);
        }
        // ILS LOOP
        int ilsCount = 0;
        // Initializing the best ILS mSolution
        getState().solutionAccepted(sol, ILS);
        while (ilsCount < getParameters().getNI()) {
            if (!isRunning()) {
                break;
            }
            ilsIteration(graspCount, ilsCount, getState().getBestSolution(ILS));
            ilsCount++;
        }

        getState().setCurrentPhase(GRASP);
        if (getAcceptanceCriterion().acceptSolution(getState(), getInstance(),
                getState().getBestSolution(ILS))) {
            // ILS Solution accepted
            getState().solutionAccepted(getState().getBestSolution(ILS), GRASP);
            VLSLogging.getProcedureLogger().info("GRAPS #%s> New best mSolution: %s", graspCount,
                    getState().getBestSolution(ILS));
            callbacks(VLSCallbackEvents.SOLUTION_ACCEPTED, getState().getBestSolution(ILS), GRASP,
                    getState());
        } else {
            // ILS Solution rejected
            getState().solutionRejected(getState().getBestSolution(ILS), GRASP);
            VLSLogging.getProcedureLogger().debug("GRAPS #%s> Solution rejected %s", graspCount,
                    getState().getBestSolution(ILS));
            callbacks(VLSCallbackEvents.SOLUTION_REJECTED, getState().getBestSolution(ILS), GRASP,
                    getState());
        }
        // Reset the ILS best mSolution for next iteration
        getState().resetBestSolution(ILS);

        timeGRASP.stop();
        VLSLogging.getProcedureLogger().debug("GRAPS loop iteration #%s finished after %sms",
                graspCount, timeGRASP.readTimeMS());
    }

    /**
     * Perform an iteration of the ils loop.
     * 
     * @param graspCount
     *            the current grasp iteration
     * @param ilsCount
     *            this ils iteration
     * @param sol
     *            the initial mSolution of the grasp
     */
    protected void ilsIteration(int graspCount, int ilsCount, S sol) {
        VLSLogging.getProcedureLogger().debug("> ILS loop iteration #%s-%s", graspCount, ilsCount);

        mState.setCurrentPhase(ILS);
        Stopwatch timILS = new Stopwatch();
        timILS.start();

        sol.acquireLock();

        // ELS LOOP
        int elsCount = 0;
        while (elsCount < getParameters().getNC()) {
            if (!isRunning()) {
                break;
            }
            elsIteration(graspCount, ilsCount, elsCount, sol);
            elsCount++;
        }

        getState().setCurrentPhase(ILS);
        if (getAcceptanceCriterion().acceptSolution(getState(), getInstance(),
                getState().getBestSolution(ELS))) {
            // ELS mSolution accepted
            getState().solutionAccepted(getState().getBestSolution(ELS), ILS);
            VLSLogging.getProcedureLogger().debug("> ILS #%s-%s> Solution accepted %s", graspCount,
                    ilsCount, getState().getBestSolution(ELS));
            callbacks(VLSCallbackEvents.SOLUTION_ACCEPTED, getState().getBestSolution(ELS), ILS,
                    getState());
        } else {
            // ELS mSolution rejected
            getState().solutionRejected(getState().getBestSolution(ELS), ILS);
            VLSLogging.getProcedureLogger().lowDebug("> ILS #%s-%s> Solution rejected %s",
                    graspCount, ilsCount, getState().getBestSolution(ELS));
            callbacks(VLSCallbackEvents.SOLUTION_REJECTED, getState().getBestSolution(ELS), ILS,
                    getState());
        }
        // Reset the ELS best mSolution for next iteration
        getState().resetBestSolution(ELS);

        sol.releaseLock();

        timILS.stop();
        VLSLogging.getProcedureLogger().debug("> ILS loop iteration #%s-%s finished after %sms",
                graspCount, ilsCount, timILS.readTimeMS());
    }

    /**
     * Performs an iteration of els loop.
     * 
     * @param graspCount
     *            the current grasp iteration
     * @param ilsCount
     *            the current ils iteration
     * @param elsCount
     *            this els iteration
     * @param sol
     *            the initial mSolution generated by the grasp initialization
     */
    @SuppressWarnings("unchecked")
    protected void elsIteration(int graspCount, int ilsCount, int elsCount, S sol) {
        VLSLogging.getProcedureLogger().debug(">> ELS loop iteration #%s-%s-%s", graspCount,
                ilsCount, elsCount);
        getState().setCurrentPhase(ELS);

        Stopwatch timELS = new Stopwatch();
        timELS.start();

        Stopwatch detail = new Stopwatch();
        detail.start();

        sol.acquireLock();

        // Perturbation (mutation)
        // TODO add timer & callback
        S solTmp = (S) sol.clone();
        solTmp.acquireLock();
        mPerturbation.pertub(getState(), getInstance(), solTmp, getParameters()
                .getPertubParameters());
        VLSLogging.getProcedureLogger().lowDebug(">> ELS #%s-%s-%s> Perturbation (%sms)\t - %s",
                graspCount, ilsCount, elsCount, detail.readTimeMS(), solTmp);

        // TODO add timer & callback
        // Local search
        // TODO add timer & callback
        detail.restart();
        solTmp = mLocalSearch.localSearch(getInstance(), solTmp, getParameters().getLSParameters());

        VLSLogging.getProcedureLogger().lowDebug(">> ELS #%s-%s-%s> Local search (%sms)\t - %s",
                graspCount, ilsCount, elsCount, detail.readTimeMS(), solTmp);
        // TODO add timer & callback

        if (getAcceptanceCriterion().acceptSolution(getState(), getInstance(), solTmp)) {
            // Solution accepted
            getState().solutionAccepted(solTmp, ELS);
            VLSLogging.getProcedureLogger().debug(">> ELS #%s-%s-%s> Solution accepted %s",
                    graspCount, ilsCount, elsCount, solTmp);
            callbacks(VLSCallbackEvents.SOLUTION_ACCEPTED, solTmp, ELS, getState());
        } else {
            // Solution rejected
            getState().solutionRejected(solTmp, ELS);
            VLSLogging.getProcedureLogger().lowDebug(">> ELS #%s-%s-%s> Solution rejected %s",
                    graspCount, ilsCount, elsCount, solTmp);
            callbacks(VLSCallbackEvents.SOLUTION_REJECTED, solTmp, ELS, getState());
        }
        solTmp.releaseLock();
        sol.releaseLock();

        timELS.stop();
        VLSLogging.getProcedureLogger().debug(
                ">> ELS loop iteration #%s-%s-%s finished after %sms", graspCount, ilsCount,
                elsCount, timELS.readTimeMS());
    }

    /**
     * Reset the heuristic to prevent collisions between runs.
     */
    public void reset() {
        stop();
        getParameters().getStoppingCriterion().reset();
        mState.reset();
        mTimer.reset();
        setInstance(null);
    }

    /**
     * Causes the heuristic to stop at the end of the current loop iteration.
     */
    public void stop() {
        mRunning = false;
        if (mTimer.isStarted() && !mTimer.isStopped())
            mTimer.stop();
        getParameters().getStoppingCriterion().reset();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    /**
     * Association of a callback to a specific event.
     * 
     * @param eventType
     *            the event that will cause the execution of <code>callback</code>
     * @param callback
     *            the callback object that will be associated with <code>event</code>
     */
    public void registerCallback(VLSCallbackEvents eventType,
            ICallback<VersatileLocalSearch<S>, VLSCallbackEvents> callback) {
        if (mCallbackDelegate != null) {
            this.mCallbackDelegate.registerCallback(callback, eventType);
        } else {
            throw new IllegalStateException(
                    "Callbacks are disabled, set ENABLE_CALLBACKS to true in VLSGlobalParameters");
        }
    }

    /**
     * Execute the callbacks associated with <code>event</code>.
     * 
     * @param eventType
     *            the event type that has occurred and for which the associated callbacks will be run
     * @param params
     *            an optional parameter that will be transmitted to the callback
     */
    protected void callbacks(VLSCallbackEvents eventType, Object... params) {
        if (mCallbackDelegate != null) {
            this.mCallbackDelegate
                    .callbacks(new CallbackEventBase<VersatileLocalSearch<S>, VLSCallbackEvents>(
                            eventType, this, params));
        }
    }

    /**
     * Will stop all child threads.
     */
    public void destroy() {
        if (mCallbackDelegate != null) {
            mCallbackDelegate.stop();
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s (%s)\n Init:%s\n Pertub:%s\n LS:%s", this.getClass()
                .getSimpleName(), mParameters, mInitialization, mPerturbation, mLocalSearch);
    }

    @Override
    public S newSolution(IVLSState<S> state, IInstance instance, IParameters params) {
        mTimer.setTimout(getParameters().getMaxTime());

        getParameters().getStoppingCriterion().reset();
        VLSLogging.getProcedureLogger().info("VLS procedure started (%s)", getParameters());

        getParameters().getStoppingCriterion().init();
        // GRASP LOOP
        int graspCount = 0;
        mTimer.start();
        while (graspCount < getParameters().getNS() && !mTimer.hasTimedOut()) {
            if (!isRunning()) {
                break;
            }
            graspIteration(graspCount);
            graspCount++;

            getParameters().getStoppingCriterion().update(getState());
            if (getParameters().getStoppingCriterion().isStopCriterionMet()) {
                VLSLogging.getProcedureLogger().info("Stopping conditions are met: %s",
                        getParameters().getStoppingCriterion());
                stop();
            }
        }
        return getBestSolution();
    }

}// end VersatilLocalSearch