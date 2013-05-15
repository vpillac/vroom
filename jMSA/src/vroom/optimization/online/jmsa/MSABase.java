package vroom.optimization.online.jmsa;

import static vroom.common.utilities.ILockable.TRY_LOCK_TIMOUT;
import static vroom.common.utilities.ILockable.TRY_LOCK_TIMOUT_UNIT;
import static vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes.MSA_END;
import static vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes.MSA_NEW_DISTINGUISHED_SOLUTION;
import static vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes.MSA_START;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Observable;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Level;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.ProcedureStatus;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Stopwatch.ReadOnlyStopwatch;
import vroom.common.utilities.callbacks.CallbackManagerDelegate;
import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.events.EventHandlerManager;
import vroom.common.utilities.events.IEvent;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.IStoppingCriterion;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.ssj.IRandomSource;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.RequestSamplerParam;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorParam;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerParam;
import vroom.optimization.online.jmsa.events.DecisionEvent;
import vroom.optimization.online.jmsa.events.DecisionHandler;
import vroom.optimization.online.jmsa.events.GenerateEvent;
import vroom.optimization.online.jmsa.events.GenerateHandler;
import vroom.optimization.online.jmsa.events.IMSAEventFactory;
import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.events.MSAEvent;
import vroom.optimization.online.jmsa.events.MSAEventHandler;
import vroom.optimization.online.jmsa.events.MSAEventQueue;
import vroom.optimization.online.jmsa.events.NewRequestEvent;
import vroom.optimization.online.jmsa.events.NewRequestHandler;
import vroom.optimization.online.jmsa.events.OptimizeEvent;
import vroom.optimization.online.jmsa.events.OptimizeHandler;
import vroom.optimization.online.jmsa.events.PoolUpdateEvent;
import vroom.optimization.online.jmsa.events.PoolUpdateHandler;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.events.ResourceHandler;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * The Class <code>MSABase</code> is a base implementation of the MSA procedure
 * <p>
 * Creation date: Sep 21, 2010 - 3:21:21 PM.
 * 
 * @param <S>
 *            the generic type
 * @param <I>
 *            the generic type
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class MSABase<S extends IScenario, I extends IInstance> extends Observable
        implements Runnable, IRandomSource {

    public static final long MAX_FIRST_SEEDS = 4294967087l;

    public static final long MAX_LAST_SEEDS  = 4294944443l;

    /**
     * <code>MSAProxy</code> is a proxy class to restrict access to MSA fields to the authorized classes.
     * 
     * @param <SS>
     *            the generic type
     * @param <II>
     *            the generic type
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
     *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de
     *         Nanntes</a>-<a href= "http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     */
    public static class MSAProxy<SS extends IScenario, II extends IInstance> {

        /** The parent. */
        private final MSABase<SS, II> parent;

        /**
         * Instantiates a new mSA proxy.
         * 
         * @param msaBase
         *            the msa base
         */
        MSAProxy(MSABase<SS, II> msaBase) {
            this.parent = msaBase;

        }

        /**
         * Execute the callbacks associated with <code>event</code>.
         * 
         * @param eventType
         *            the event type that has occurred and for which the associated callbacks will be run
         * @param params
         *            an optional parameter that will be transmitted to the callback
         */
        public void callbacks(MSACallbackEvent.EventTypes eventType, Object... params) {
            this.parent.callbacks(eventType, params);
        }

        /**
         * Getter for the component manager.
         * 
         * @return the component manager of the associated MSA instance
         */
        public ComponentManager<SS, II> getComponentManager() {
            return this.parent.mComponentManager;
        }

        /**
         * Getter proy for the distinguished solution.
         * 
         * @return The current distinguished solution
         * @see MSASequential#getDistinguishedSolution()
         */
        public IDistinguishedSolution getDistinguishedSolution() {
            return this.parent.getDistinguishedSolution();
        }

        /**
         * Getter for the instance.
         * 
         * @return the instance associated with this msa procedure
         */
        public II getInstance() {
            return this.parent.mInstance;
        }

        /**
         * Getter for the global parameters.
         * 
         * @return the global parameters associated with this msa procedure
         */
        public MSAGlobalParameters getParameters() {
            return this.parent.getParameters();
        }

        /**
         * Getter for the scenario pool.
         * 
         * @return the scenario pool of this msa procedure
         */
        public ScenarioPool<SS> getScenarioPool() {
            return this.parent.mPool;
        }

        /**
         * Setter for the distinguished solution, should only be called by instances of {@link DecisionHandler}.
         * 
         * @param distinguishedSolution
         *            The new distinguished solution
         */
        public void setDistinguishedSolution(IDistinguishedSolution distinguishedSolution) {
            IDistinguishedSolution oldValue = this.parent.mDistinguishedSolution;
            this.parent.mDistinguishedSolution = distinguishedSolution;
            this.parent.callbacks(MSA_NEW_DISTINGUISHED_SOLUTION, oldValue,
                    this.parent.mDistinguishedSolution);
        }

        /**
         * Getter for the main {@link RandomStream}
         * 
         * @return the main {@link RandomStream} used in this msa instance
         */
        public RandomStream getRandomStream() {
            return this.parent.getRandomStream();
        }

        /**
         * Getter for the {@link RandomStream} used in scenario optimization
         * 
         * @return the {@link RandomStream} used to optimize scenarios
         */
        public RandomStream getOptimizationRandomStream() {
            return this.parent.getOptimizationRandomStream();
        }

        /**
         * Getter for the {@link RandomStream} used in decisions
         * 
         * @return the {@link RandomStream} used to take decisions
         */
        public RandomStream getDecisionRandomStream() {
            return this.parent.getDecisionRandomStream();
        }

        /**
         * Getter for the {@link RandomStream} used in scenario generation
         * 
         * @return the {@link RandomStream} used to generate new scenarios
         */
        public RandomStream getGenerationRandomStream() {
            return this.parent.getGenerationRandomStream();
        }

        /**
         * Checks if a preemptive event was received
         * 
         * @return <code>true</code> if the next event in the queue is preemptive
         */
        public boolean isNextEventPreemptive() {
            return !this.parent.isRunning() || this.parent.mEventQueue.isNextEventPreemptive();
        }

        /**
         * Factory method for a {@link MSABaseStoppingCriterion}
         * 
         * @param stoppingCriterion
         *            the base stopping criterion
         * @param interruptible
         *            <code>true</code> if the process is interruptible
         * @return an instance of {@link MSABaseStoppingCriterion} wrapping the provided <code>stoppingCriterion</code>
         */
        @SuppressWarnings("unchecked")
        public IStoppingCriterion newStoppingCriterion(IStoppingCriterion stoppingCriterion,
                boolean interruptible) {
            if (stoppingCriterion instanceof MSABase.MSAProxy.MSABaseStoppingCriterion) {
                return new MSABaseStoppingCriterion(
                        ((MSABaseStoppingCriterion) stoppingCriterion).getParentCriterion(),
                        interruptible);
            } else {
                return new MSABaseStoppingCriterion(stoppingCriterion, interruptible);
            }
        }

        /**
         * The Class <code>MSABaseStoppingCriterion</code> is an implementation of {@link IStoppingCriterion} that wraps
         * an instance of {@link IStoppingCriterion} and additionally checks if there are
         * {@linkplain IEvent#isPreemptive() preemptive} events in the event queue.
         * <p>
         * {@link #isStopCriterionMet()} will return <code>true</code> if this criterion is interruptible and a
         * preemptive event is present. This is useful to terminate subroutines.
         * </p>
         * <p>
         * Creation date: Nov 30, 2010 - 4:12:44 PM.
         * </p>
         * 
         * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
         *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de
         *         Nantes</a>-<a href ="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp"> SLP</a>
         * @version 1.0
         */
        public class MSABaseStoppingCriterion implements IStoppingCriterion {
            private final IStoppingCriterion mCriterion;
            private final boolean            mInterruptible;

            /**
             * Creates a new <code>MSABaseStoppingCriterion</code>
             * 
             * @param criterion
             */
            private MSABaseStoppingCriterion(IStoppingCriterion criterion, boolean interruptible) {
                mCriterion = criterion;
                mInterruptible = interruptible;
            }

            @Override
            public boolean isStopCriterionMet() {
                return mCriterion.isStopCriterionMet()
                        || (mInterruptible && isNextEventPreemptive());
            }

            @Override
            public void update(int iterations, Object... args) {
                mCriterion.update(iterations, args);
            }

            @Override
            public void update(Object... args) {
                mCriterion.update(args);
            }

            @Override
            public void reset() {
                mCriterion.reset();
            }

            @Override
            public void init() {
                mCriterion.init();
            }

            /**
             * Returns the wrapped criterion
             * 
             * @return the wrapped criterion
             */
            public IStoppingCriterion getParentCriterion() {
                return mCriterion;
            }

            /**
             * Returns <code>true</code> if the criterion is used for an interruptible process
             * 
             * @return<code>true</code> if the criterion is used for an interruptible process
             */
            public boolean isInterruptible() {
                return mInterruptible;
            }

            @Override
            public int getIterationCount() {
                return mCriterion.getIterationCount();
            }

            @Override
            public int getMaxIterations() {
                return mCriterion.getMaxIterations();
            }

            @Override
            public double getCurrentTime() {
                return mCriterion.getCurrentTime();
            }

            @Override
            public long getMaxTime() {
                return mCriterion.getMaxTime();
            }

            @Override
            public MSABaseStoppingCriterion clone() {
                return new MSABaseStoppingCriterion(mCriterion.clone(), mInterruptible);
            }
        }

    }

    private ProcedureStatus mState;

    /**
     * Returns the current state of the procedure
     * <p>
     * Possible values are:
     * <ul>
     * <li>{@link ProcedureStatus#INSTANTIATED}</li>
     * <li>{@link ProcedureStatus#INITIALIZATION}</li>
     * <li>{@link ProcedureStatus#INITIALIZED}</li>
     * <li>{@link ProcedureStatus#RUNNING}</li>
     * <li>{@link ProcedureStatus#PAUSED}</li>
     * <li>{@link ProcedureStatus#TERMINATED}</li>
     * <li>{@link ProcedureStatus#EXCEPTION}</li>
     * </ul>
     * </p>
     * 
     * @return the current state of the procedure
     */
    public ProcedureStatus getStatus() {
        return mState;
    }

    /**
     * Set the current state of the procedure
     * <p>
     * Possible values are:
     * <ul>
     * <li>{@link ProcedureStatus#INSTANTIATED}</li>
     * <li>{@link ProcedureStatus#INITIALIZATION}</li>
     * <li>{@link ProcedureStatus#INITIALIZED}</li>
     * <li>{@link ProcedureStatus#RUNNING}</li>
     * <li>{@link ProcedureStatus#PAUSED}</li>
     * <li>{@link ProcedureStatus#TERMINATED}</li>
     * <li>{@link ProcedureStatus#EXCEPTION}</li>
     * </ul>
     * </p>
     * 
     * @param state
     */
    protected void setStatus(ProcedureStatus state) {
        if (mState != ProcedureStatus.EXCEPTION) {
            acquireLock();
            mState = state;
            getStatusCond().signalAll();
            releaseLock();
        }
    }

    /**
     * Returns a condition on the current state of the procedure
     * 
     * @return a condition on the current state of the procedure
     */
    public Condition getStatusCond() {
        return mStatusCond;
    }

    /**
     * Pause the main MSA procedure</br> This will cause the main procedure to be suspended before the retrieval of the
     * next event or before the handling of the current event.
     */
    public void pause() {
        setPaused(true);
        setStatus(ProcedureStatus.PAUSED);
    }

    /**
     * Msa procedure.
     */
    protected abstract void msaProcedure();

    /** The name of the distinguished solution property. */
    public static final String                                         PROP_DISTINGUISHED_SOL = "DistinguishedSolution";

    /** The name of the paused property. */
    private static final String                                        PROP_PAUSED            = "Paused";

    /** The number of initialized MSA procedures. */
    protected static int                                               sMSACount              = 0;

    /**
     * The callback manager delegate used to register, unregister and call callback procedures.
     */
    protected final CallbackManagerDelegate<MSABase<?, ?>, EventTypes> mCallbackManagerDelegate;

    /** The object responsible for the management of MSA components. */
    protected final ComponentManager<S, I>                             mComponentManager;

    /** The current distinguished solution. */
    private IDistinguishedSolution                                     mDistinguishedSolution;
    /**
     * The event factory associated with this MSA procedure. All events should be created by this instance
     */
    protected final IMSAEventFactory                                   mEventFactory;
    /**
     * The event handler manager for this MSA procedure. It is responsible of the association of handlers to events for
     * their handling
     */
    protected final EventHandlerManager                                mEventHandlerManager;
    /**
     * The event manager for this MSA procedure. It is responsible of the reception and handling of incoming events.
     */
    protected final MSAEventQueue                                      mEventQueue;

    /**
     * The event being currently handled
     */
    private MSAEvent                                                   mCurrentEvent;

    /**
     * Returns the event being currently handled
     * 
     * @return the event being currently handled
     */
    public MSAEvent getCurrentEvent() {
        return mCurrentEvent;
    }

    /**
     * Setter for the event being currently handled
     * 
     * @param currentEvent
     *            the currentEvent to set
     */
    void setCurrentEvent(MSAEvent currentEvent) {
        mCurrentEvent = currentEvent;
    }

    /** <code>true</code> if the scenario pool has been initialized *. */
    private boolean                       mInitialized;
    /** The instance on which thiw MSA is based. */
    protected final I                     mInstance;

    private final ExtendedReentrantLock   mPublicLock                = new ExtendedReentrantLock(
                                                                             true);
    /** A private lock for this object. */
    protected final ExtendedReentrantLock mLock                      = new ExtendedReentrantLock(
                                                                             true);

    /** A condition on the initialized state. */
    private final Condition               mInitializedCond           = this.mPublicLock
                                                                             .newCondition();

    /** The Paused condition. */
    protected final Condition             mPausedCondition           = this.mLock.newCondition();

    /**
     * The running status of this MSA procedure: <code>true</code> if started, <code>false</code> if not started yet or
     * stopped.
     */
    private boolean                       mRunning                   = false;

    /** A condition on the running state. */
    private final Condition               mRunningCond               = this.mPublicLock
                                                                             .newCondition();

    /** A condition on the current state of the procedure. */
    private final Condition               mStatusCond                = this.mPublicLock
                                                                             .newCondition();

    /** An id for this procedure. */
    protected final int                   mMSAId;

    /** The global parameters used by this instance of the MSA procedure. */
    protected final MSAGlobalParameters   mParameters;

    /**
     * The paused status of this MSA procedure: <code>true</code> if it is currently paused, <code>false</code> if not.
     */
    private boolean                       mPaused                    = true;

    /** The pool of scenario used by this procedure. */
    protected final ScenarioPool<S>       mPool;

    /** A proxy to restrict access to instance properties. */
    protected final MSAProxy<S, I>        mProxy;

    /** A public lock for observable properties. */

    /** A timer for this msa instance *. */
    protected final Stopwatch             mTimer;

    /** The key to which the main random stream is associated */
    public static final String            MAIN_RANDOM_STREAM         = "MSARndStream";

    /** The key to which the optimization random stream is associated */
    public static final String            OPTIMIZATION_RANDOM_STREAM = "MSAOptRndStream";

    /** The key to which the decision random stream is associated */
    public static final String            DECISION_RANDOM_STREAM     = "MSADecRndStream";

    /** The key to which the generation random stream is associated */
    public static final String            GENERATION_RANDOM_STREAM   = "MSAGenRndStream";

    private RandomStream                  mMainRndStream;
    private RandomStream                  mOptRndStream;
    private RandomStream                  mDecRndStream;
    private RandomStream                  mGenRndStream;

    /**
     * Instantiates a new mSA base.
     * 
     * @param instance
     *            the instance
     * @param parameters
     *            the parameters
     */
    public MSABase(I instance, MSAGlobalParameters parameters) {
        super();

        // Sets the id of this MSA instance
        this.mMSAId = sMSACount;
        sMSACount++;

        this.mProxy = new MSAProxy<S, I>(this);

        Collection<ParameterKey<?>> missingParams = parameters.checkRequiredParameters();
        if (!missingParams.isEmpty()) {
            MSALogging.getSetupLogger().warn(
                    "The following required global parameters have not been defined: "
                            + missingParams.toString());
        }

        this.mInstance = instance;
        this.mParameters = parameters;

        long[] seeds = this.mParameters.get(MSAGlobalParameters.RANDOM_SEEDS);

        Long paramSeeds = this.mParameters.get(MSAGlobalParameters.RANDOM_SEED);
        Long seed = seeds != null ? seeds[0] : paramSeeds != null ? paramSeeds : System
                .currentTimeMillis();
        long[] seedsMain = new long[6];
        if (seed != null) {
            Random r = new Random(seed);
            for (int i = 0; i < seedsMain.length; i++) {
                seedsMain[i] = seeds != null && i < seeds.length ? seeds[i] : (long) Math.floor(r
                        .nextDouble() * 4294944443l);
            }
        }
        MRG32k3a rnd = new MRG32k3a(MAIN_RANDOM_STREAM);
        rnd.setSeed(seedsMain);
        setRandomStream(rnd);

        this.mPool = new ScenarioPool<S>(getParameter(MSAGlobalParameters.POOL_SIZE));

        this.mCallbackManagerDelegate = new CallbackManagerDelegate<MSABase<?, ?>, EventTypes>(
                "msa");

        this.mEventQueue = new MSAEventQueue();

        this.mComponentManager = getParameters().newInstance(
                MSAGlobalParameters.COMPONENT_MANAGER_CLASS, this, this.mProxy);

        this.mEventFactory = getParameters().<IMSAEventFactory> newInstance(
                MSAGlobalParameters.EVENT_FACTORY_CLASS, this, this.mEventQueue);

        this.mEventHandlerManager = new EventHandlerManager();

        setDefaultEventHandlers();

        this.mTimer = new Stopwatch();
        mState = ProcedureStatus.INSTANTIATED;
    }

    /**
     * Acquire lock.
     */
    public void acquireLock() {
        try {
            if (!mPublicLock.tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
                throw new IllegalStateException(
                        String.format(
                                "Unable to acquire lock on this instance of %s (%s) after %s %s, owner: %s",
                                this.getClass().getSimpleName(), hashCode(), TRY_LOCK_TIMOUT,
                                TRY_LOCK_TIMOUT_UNIT, mPublicLock.getOwnerName()));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format(
                    "Unable to acquire lock on this instance of %s (%s)", this.getClass()
                            .getSimpleName(), hashCode()), e);
        }
        ;
    }

    /**
     * Execute the callbacks associated with <code>event</code>.
     * 
     * @param eventType
     *            the event type that has occurred and for which the associated callbacks will be run
     * @param params
     *            an optional parameter that will be transmitted to the callback
     */

    public void callbacks(MSACallbackEvent.EventTypes eventType, Object... params) {
        this.mCallbackManagerDelegate.callbacks(new MSACallbackEvent(eventType, this, params));
    }

    /**
     * Check lock.
     * 
     * @throws ConcurrentModificationException
     *             the concurrent modification exception
     */
    public void checkLock() throws ConcurrentModificationException {
        if (!isLockOwnedByCurrentThread()) {
            throw new ConcurrentModificationException(String.format(
                    "The current thread (%s) does not have the lock on this object",
                    Thread.currentThread()));
        }
    }

    // ------------------------------------

    /*
     * (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        stopChildThreads();
        super.finalize();
    }

    /**
     * Stop child threads.
     */
    protected void stopChildThreads() {
        this.mCallbackManagerDelegate.stop();
    }

    /**
     * Gets the components description.
     * 
     * @return the components description
     */
    public String getComponentsDescription() {
        return mComponentManager.toString();
    }

    /**
     * Getter for the current solution.
     * <p/>
     * This method will return an object representing the current (or final is the MSA procedure is terminated) request
     * sequence that have been served by each resource.
     * 
     * @return the current solution
     * @see IInstance#getCurrentSolution()
     */

    public Object getCurrentSolution() {
        return getInstance().getCurrentSolution();
    }

    /**
     * Getter for the distinguished solution.
     * 
     * @return The current distinguished solution
     */

    public IDistinguishedSolution getDistinguishedSolution() {
        return this.mDistinguishedSolution;
    }

    /**
     * Gets the event factory.
     * 
     * @return the eventFactory associated with this MSA procedure. All events should be created by this instance
     */

    public IMSAEventFactory getEventFactory() {
        return this.mEventFactory;
    }

    /**
     * Scenario generation parameters.
     * 
     * @return the parameters to be used for scenario generation
     */

    public ScenarioGeneratorParam getGenerateParameters() {
        // TODO find a way to dynamically define these parameters
        return new ScenarioGeneratorParam(
                // getParameter(MSAGlobalParameters.POOL_SIZE),
                (int) (getParameter(MSAGlobalParameters.POOL_SIZE) * 0.2),
                getParameter(MSAGlobalParameters.GEN_MAX_SCEN_OPT_TIME), new RequestSamplerParam(
                        getParameter(MSAGlobalParameters.SAMPLED_REQUEST_COUNT)));
    }

    /**
     * A {@link Condition} on the {@linkplain #isInitialized() initialized state} for external monitoring of the MSA
     * running state.
     * 
     * @return the {@link Condition} associated with the initialized state of this procedure
     */

    public Condition getInitializedCondition() {
        return this.mInitializedCond;
    }

    /**
     * Getter for the underlying instance.
     * 
     * @return the instance on which this MSA procedure is based
     */

    public I getInstance() {
        return this.mInstance;
    }

    /**
     * Gets the lock instance.
     * 
     * @return the lock instance
     */
    public ReentrantLock getLockInstance() {
        return this.mPublicLock;
    }

    /**
     * Scenario optimization parameters.
     * 
     * @return the parameters to be used for scenario generation
     */

    public ScenarioOptimizerParam getOptimizeParameters(MSAEvent event) {
        return new ScenarioOptimizerParam(
                (int) (getParameter(MSAGlobalParameters.OPT_MAX_SCEN_OPT_TIME) * mPool.size() * 0.5),
                getParameter(MSAGlobalParameters.OPT_MAX_SCEN_OPT_TIME), !event.isPreemptive());
    }

    /**
     * Utility class to read parameters.
     * 
     * @param <T>
     *            the generic type
     * @param key
     *            the key for the desired parameter
     * @return the value associated with the given <code>key</code>, <code>null</code> if there is no value associated
     *         with <code>key</code>
     * @throws IllegalArgumentException
     *             if no value is associated with <code>key</code>
     */
    protected <T> T getParameter(ParameterKey<T> key) throws IllegalArgumentException {
        return getParameters().get(key);
    }

    /**
     * Getter for the msa parameters.
     * 
     * @return the global parameters used by this instance of the MSA procedure
     */

    public final MSAGlobalParameters getParameters() {
        return this.mParameters;
    }

    /**
     * Getter for the MSA proxy.
     * 
     * @return a proxy for some properties of this procedure
     */

    public MSAProxy<S, I> getProxy() {
        return this.mProxy;
    }

    @Override
    public RandomStream getRandomStream() {
        return mMainRndStream;
    }

    @Override
    public void setRandomStream(RandomStream stream) {
        mMainRndStream = stream;

        // Generate the other random streams
        long[] seedsOpt = new long[6];
        long[] seedsGen = new long[6];
        long[] seedsDec = new long[6];
        for (int i = 0; i < seedsDec.length; i++) {
            long max = i < 3 ? MAX_FIRST_SEEDS : MAX_LAST_SEEDS;
            seedsOpt[i] = (long) Math.floor(stream.nextDouble() * max);
            seedsGen[i] = (long) Math.floor(stream.nextDouble() * max);
            seedsDec[i] = (long) Math.floor(stream.nextDouble() * max);
        }
        mGenRndStream = new MRG32k3a(GENERATION_RANDOM_STREAM);
        ((MRG32k3a) mGenRndStream).setSeed(seedsGen);
        mDecRndStream = new MRG32k3a(DECISION_RANDOM_STREAM);
        ((MRG32k3a) mDecRndStream).setSeed(seedsDec);
        mOptRndStream = new MRG32k3a(OPTIMIZATION_RANDOM_STREAM);
        ((MRG32k3a) mOptRndStream).setSeed(seedsOpt);
    }

    /**
     * Getter for the {@link RandomStream} used in scenario optimization
     * 
     * @return the {@link RandomStream} used to optimize scenarios
     */
    public RandomStream getOptimizationRandomStream() {
        return mOptRndStream;
    }

    /**
     * Getter for the {@link RandomStream} used in decisions
     * 
     * @return the {@link RandomStream} used to take decisions
     */
    public RandomStream getDecisionRandomStream() {
        return mDecRndStream;
    }

    /**
     * Getter for the {@link RandomStream} used in scenario generation
     * 
     * @return the {@link RandomStream} used to generate new scenarios
     */
    public RandomStream getGenerationRandomStream() {
        return mGenRndStream;
    }

    /**
     * Getter for timer : A timer for this msa instance.
     * 
     * @return the timer used in this instance
     */

    public ReadOnlyStopwatch getTimer() {
        return this.mTimer.getReadOnlyStopwatch();
    }

    /**
     * Getter for initialized : <code>true</code> if the scenario pool has been initialized.
     * 
     * @return the value of initialized
     */

    public boolean isInitialized() {
        return this.mInitialized;
    }

    /**
     * Checks if is lock owned by current thread.
     * 
     * @return true, if is lock owned by current thread
     */
    public boolean isLockOwnedByCurrentThread() {
        return this.mPublicLock.isHeldByCurrentThread();
    }

    /**
     * Getter for paused : The paused status of this MSA procedure: <code>true</code> if it is currently paused,
     * <code>false</code> if not.
     * 
     * @return the value of paused
     */
    public boolean isPaused() {
        return this.mPaused;
    }

    /**
     * Getter for running : The running status of this MSA procedure: <code>true</code> if started, <code>false</code>
     * if not started yet or stopped. Note that the actual status of the procedure should be queried with
     * {@link #getStatus()}
     * 
     * @return the value of running
     */

    public boolean isRunning() {
        return this.mRunning;
    }

    /**
     * A {@link Condition} on the {@linkplain #isRunning() running state} for external monitoring of the MSA running
     * state.
     * 
     * @return the {@link Condition} associated with the running state of this procedure
     */

    public Condition getRunningCondition() {
        return this.mRunningCond;
    }

    /**
     * Check if there are pending events.
     * 
     * @return <code>true</code> if there are pending events waiting to ve handled
     */
    public boolean hasPendingEvents() {
        return !mEventQueue.isEmpty();
    }

    /**
     * Association of a callback to a specific event.
     * 
     * @param eventType
     *            the event that will cause the execution of <code>callback</code>
     * @param callback
     *            the callback object that will be associated with <code>event</code>
     */

    public void registerCallback(MSACallbackEvent.EventTypes eventType,
            ICallback<MSABase<?, ?>, EventTypes> callback) {
        this.mCallbackManagerDelegate.registerCallback(callback, eventType);
    }

    /**
     * Release lock.
     */
    public void releaseLock() {
        this.mPublicLock.unlock();
    }

    /**
     * Calling this method will start the MSA procedure.
     * 
     * @see MSASequential#start()
     */

    @Override
    public void run() {
        try {
            start();
        } catch (Exception e) {
            stop();
            setStatus(ProcedureStatus.EXCEPTION);
            throw new IllegalStateException("Exception thrown in the MSA procedure", e);
        }
    }

    /**
     * This method is called during initialization to set the default values for each event handled in the MSA
     * procedure.<br/>
     * It can be used to restore default values during the MSA execution
     */
    protected final void setDefaultEventHandlers() {
        setEventHanlder(DecisionEvent.class, new DecisionHandler<S, I>(this.mProxy));
        setEventHanlder(ResourceEvent.class, new ResourceHandler<S, I>(this.mProxy));
        setEventHanlder(GenerateEvent.class, new GenerateHandler<S, I>(this.mProxy));
        setEventHanlder(NewRequestEvent.class, new NewRequestHandler<S, I>(this.mProxy));
        setEventHanlder(OptimizeEvent.class, new OptimizeHandler<S, I>(this.mProxy));
        setEventHanlder(PoolUpdateEvent.class, new PoolUpdateHandler<S, I>(this.mProxy));
    }

    /**
     * Sets the event hanlder.
     * 
     * @param <E>
     *            the element type
     * @param eventClass
     *            the event class
     * @param handler
     *            the handler
     */
    public <E extends MSAEvent> void setEventHanlder(Class<E> eventClass,
            MSAEventHandler<E, S, I> handler) {
        this.mEventHandlerManager.setEventHandler(eventClass, handler);
    }

    /**
     * Setter for initialized : <code>true</code> if the scenario pool has been initialized.
     * 
     * @param initialized
     *            the value to be set for initialized
     */
    protected void setInitialized(boolean initialized) {
        this.mInitialized = initialized;
        acquireLock();
        getInitializedCondition().signalAll();
        releaseLock();
    }

    /**
     * Sets the paused status.
     * 
     * @param paused
     *            the new paused
     */
    protected void setPaused(boolean paused) {
        boolean old = isPaused();
        this.mPaused = paused;
        if (old != isPaused()) {
            notifyObservers(PROP_PAUSED);
            try {
                this.mLock.lockInterruptibly();
                this.mPausedCondition.signalAll();
                this.mLock.unlock();
            } catch (InterruptedException e) {
                MSALogging.getProcedureLogger().warn(
                        "Exception catched while setting the paused flag", e);
            }
        }
    }

    /**
     * Sets the running state.
     * 
     * @param running
     *            the new running
     */
    private void setRunning(boolean running) {
        this.mRunning = running;
        acquireLock();
        getRunningCondition().signalAll();
        releaseLock();
    }

    /**
     * Sets the level for the {@link org.apache.log4j.Logger} with the given <code>loggerName</code>
     * 
     * @param loggerName
     *            the name of the logger for which the <code>level</code> will be set. <br/>
     *            Loggers names are defined in the {@link MSALogging} class. Note that all loggers loggers inherit by
     *            default from
     * @param level
     *            the desired level of logging for logger <code>loggerName</code> <br/>
     *            Logging levels are defined in the {@link LoggerHelper} class. {@link MSALogging#BASE_LOGGER}
     * @see MSALogging#BASE_LOGGER
     * @see MSALogging#MSA_PROCEDURE_LOGGER
     * @see MSALogging#MSA_SETUP_LOGGER
     * @see MSALogging#MSA_COMPONENTS_LOGGER
     * @see MSALogging#MSA_EVENTS_LOGGER
     * @see LoggerHelper#LEVEL_LOW_DEBUG
     * @see LoggerHelper#LEVEL_DEBUG
     * @see LoggerHelper#LEVEL_INFO
     * @see LoggerHelper#LEVEL_WARN
     * @see LoggerHelper#LEVEL_ERROR
     * @see LoggerHelper#LEVEL_FATAL
     */

    public void setVerbose(String loggerName, Level level) {
        Logging.setLoggerLevel(loggerName, level);
    }

    /**
     * Starts the MSA procedure.<br/>
     * The MSA procedure can be paused by calling {@link MSASequential#pause()} and unpaused with
     * {@link MSASequential#resume()}. When the procedure is paused, it will finish the handling of the current event
     * (if any) and then wait until unpaused to retreive and handle the next event.<br/>
     * On the other hand, the procedure can be terminated by calling calling {@link MSASequential#stop()} in which case
     * it will first finish the handling of the current event (id any) and then terminate.
     */

    public void start() {
        if (isRunning()) {
            throw new IllegalStateException("This MSA procedure has already been started");
        }

        if (!checkState()) {
            MSALogging.getProcedureLogger().warn("Aborting the MSA procedure, current state: %s",
                    this);
            return;
        }

        setRunning(true);
        resume();

        mTimer.start();
        MSALogging.getProcedureLogger().info("MSA procedure #%s started in thread %s [%s] ",
                this.mMSAId, Thread.currentThread().getName(), Thread.currentThread().getId());
        callbacks(MSA_START);

        msaProcedure();

        setStatus(ProcedureStatus.TERMINATED);

        mTimer.stop();
        MSALogging.getProcedureLogger().info("MSA procedure #%s finished in thread %s [%s] ",
                this.mMSAId, Thread.currentThread().getName(), Thread.currentThread().getId());
        callbacks(MSA_END);

        stopChildThreads();
    }

    /**
     * @return true if all components of the MSA are correctly defined
     */
    protected boolean checkState() {
        Collection<ParameterKey<?>> missingParams = getParameters().checkRequiredParameters();
        if (!missingParams.isEmpty()) {
            MSALogging.getProcedureLogger().warn(
                    "The following required global parameters have not been defined: "
                            + missingParams.toString());
            return false;
        }

        return true;
    }

    /**
     * Permanently stop the MSA procedure (Cannot be resumed)<br/>
     * For thread safety reasons, the procedure will be aborted after the handling of the current event if any.
     */

    public void stop() {
        if (!isRunning())
            MSALogging.getProcedureLogger().info(
                    "MSABase.stop: The MSA procedure is already stopped, ignoring");
        else {

            MSALogging
                    .getProcedureLogger()
                    .info("MSABase.stop: Stopping the MSA procedure at the end of the current iteration if any");
            resume();
            setRunning(false);
            this.mEventQueue.clear();
        }
    }

    /**
     * Permanently stop the MSA procedure (Cannot be resumed) after an exception<br/>
     * For thread safety reasons, the procedure will be aborted after the handling of the current event if any.
     */
    public void exception() {
        stop();
        setStatus(ProcedureStatus.EXCEPTION);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String state = "";
        if (isInitialized()) {
            state += "Initialized";
        }
        if (isRunning()) {
            state += ",Running";
        } else {
            state += ",Stopped";
        }
        if (isPaused()) {
            state += ",Paused";
        }

        return String.format("State: %s\n" + "Running Time: %s\n" + "Pool:%s\n"
                + "Current Solution:\n%s", state, this.mTimer, this.mPool.toString(),
                getCurrentSolution());
    }

    /**
     * Try lock.
     * 
     * @param timeout
     *            the timeout
     * @return true, if successful
     */
    public boolean tryLock(long timeout) {
        try {
            return getLockInstance().tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Unpause the main MSA procedure.
     */
    public void resume() {
        setPaused(false);
    }

    /**
     * Removal of a callback for a specific event.
     * 
     * @param eventType
     *            the considered event
     * @param callback
     *            the callback object that will no longer be associated with <code>event</code>
     */

    public void unregisterCallback(MSACallbackEvent.EventTypes eventType, MSACallbackBase callback) {
        this.mCallbackManagerDelegate.unregisterCallback(callback, eventType);
    }

    /**
     * This method will wait until the paused flag is set to <code>false</code>.
     * 
     * @throws InterruptedException
     *             the interrupted exception
     */
    protected synchronized void waitUntilUnpaused() throws InterruptedException {
        while (isPaused()) {
            // Logging.getProcedureLogger().lowDebug(
            // "MSA Procedure paused, waiting untill it is unpaused");
            acquireLock();
            try {
                try {
                    this.mPausedCondition.await();
                } catch (InterruptedException ie) {
                    throw ie;
                }
            } finally {
                releaseLock();
            }
        }
    }

    /**
     * Getter for the component manager
     * 
     * @return the component manager used in this instance
     */
    public ComponentManager<S, I> getComponentManager() {
        return mComponentManager;
    }

}