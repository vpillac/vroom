package vroom.optimization.online.jmsa;

import java.util.ConcurrentModificationException;
import java.util.concurrent.locks.Condition;

import org.apache.log4j.Level;

import umontreal.iro.lecuyer.rng.RandomStreamManager;
import vroom.common.utilities.ILockable;
import vroom.common.utilities.Timer.ReadOnlyTimer;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorParam;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerParam;
import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.events.MSAEvent;
import vroom.optimization.online.jmsa.events.MSAEventFactory;
import vroom.optimization.online.jmsa.events.MSAEventHandler;
import vroom.optimization.online.jmsa.utils.MSALogging;

public interface MultipleScenarioApproach<S extends IScenario, I extends IInstance> extends
        Runnable, ILockable {

    /**
     * A {@link Condition} on the {@linkplain #isRunning() running state} for
     * external monitoring of the MSA running state
     * 
     * @return the {@link Condition} associated with the running state of this
     *         procedure
     */
    public Condition getRunningCondition();

    /**
     * Getter for running : The running status of this MSA procedure:
     * <code>true</code> if started, <code>false</code> if not started yet or
     * stopped
     * 
     * @return the value of running
     */
    public boolean isRunning();

    /**
     * A {@link Condition} on the {@linkplain #isInitialized() initialized
     * state} for external monitoring of the MSA running state
     * 
     * @return the {@link Condition} associated with the initialized state of
     *         this procedure
     */
    public Condition getInitializedCondition();

    /**
     * Getter for initialized : <code>true</code> if the scenario pool has been
     * initialized
     * 
     * @return the value of initialized
     */
    public boolean isInitialized();

    /** The name of the distinguished mSolution property */
    public static final String PROP_DISTINGUISHED_SOL = "DistinguishedSolution";

    /**
     * Getter for the distinguished mSolution
     * 
     * @return The current distinguished mSolution
     */
    public IDistinguishedSolution getDistinguishedSolution();

    /**
     * Getter for the random stream manager
     * 
     * @return The random stream manager used by the whole MSA procedure
     */
    public RandomStreamManager getRandomStreamManager();

    /**
     * Getter for timer : A timer for this msa instance
     * 
     * @return the timer used in this instance
     */
    public ReadOnlyTimer getTimer();

    public <E extends MSAEvent> void setEventHanlder(Class<E> eventClass,
            MSAEventHandler<E, S, I> handler);

    /**
     * Starts the MSA procedure.<br/>
     * The MSA procedure can be paused by calling
     * {@link MultipleScenarioApproachMT#pause()} and unpaused with
     * {@link MultipleScenarioApproachMT#unpause()}. When the procedure is
     * paused, it will finish the handling of the current event (if any) and
     * then wait until unpaused to retreive and handle the next event.<br/>
     * On the other hand, the procedure can be terminated by calling calling
     * {@link MultipleScenarioApproachMT#stop()} in which case it will first
     * finish the handling of the current event (id any) and then terminate.
     * 
     * @throws IllegalStateException
     *             is the MSA procedure has already been started
     */
    public void start();

    /**
     * Permanently stop the MSA procedure (Cannot be resumed)<br/>
     * For thread safety reasons, the procedure will be aborted after the
     * handling of the current event if any.
     */
    public void stop();

    /**
     * Execute the callbacks associated with <code>event</code>
     * 
     * @param eventType
     *            the event type that has occurred and for which the associated
     *            callbacks will be run
     * @param params
     *            an optional parameter that will be transmitted to the callback
     */
    public void callbacks(MSACallbackEvent.EventTypes eventType, Object... params);

    /**
     * @return the eventFactory associated with this MSA procedure. All events
     *         should be created by this instance
     */
    public MSAEventFactory getEventFactory();

    /**
     * Getter for the msa parameters
     * 
     * @return the global parameters used by this instance of the MSA procedure
     */
    public MSAGlobalParameters getParameters();

    /**
     * Scenario optimization parameters
     * 
     * @return the parameters to be used for scenario generation
     */
    public ScenarioOptimizerParam getOptimizeParameters();

    /**
     * Scenario generation parameters
     * 
     * @return the parameters to be used for scenario generation
     */
    public ScenarioGeneratorParam getGenerateParameters();

    /**
     * Getter for the underlying instance
     * 
     * @return the instance on which this MSA procedure is based
     */
    public I getInstance();

    /**
     * Getter for the MSA proxy
     * 
     * @return a proxy for some properties of this procedure
     */
    public MSAProxy<S, I> getProxy();

    /**
     * Association of a callback to a specific event
     * 
     * @param eventType
     *            the event that will cause the execution of
     *            <code>callback</code>
     * @param callback
     *            the callback object that will be associated with
     *            <code>event</code>
     */
    public void registerCallback(MSACallbackEvent.EventTypes eventType, MSACallbackBase callback);

    /**
     * Removal of a callback for a specific event
     * 
     * @param eventType
     *            the considered event
     * @param callback
     *            the callback object that will no longer be associated with
     *            <code>event</code>
     */
    public void unregisterCallback(MSACallbackEvent.EventTypes eventType, MSACallbackBase callback);

    /**
     * Sets the level for the {@link org.apache.log4j.Logger} with the given
     * <code>loggerName</code>
     * 
     * @param loggerName
     *            the name of the logger for which the <code>level</code> will
     *            be set. <br/>
     *            Loggers names are defined in the {@link MSALogging} class.
     *            Note that all loggers loggers inherit by default from
     *            {@link MSALogging#BASE_LOGGER}
     * @param level
     *            the desired level of logging for logger
     *            <code>loggerName</code> <br/>
     *            Logging levels are defined in the {@link LoggerHelper} class.
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
    public void setVerbose(String loggerName, Level level);

    /**
     * Getter for the current mSolution.
     * <p/>
     * This method will return an object representing the current (or final is
     * the MSA procedure is terminated) request sequence that have been served
     * by each resource.
     * 
     * @return the current mSolution
     * @see IInstance#getCurrentSolution()
     */
    public Object getCurrentSolution();

    public void checkLock() throws ConcurrentModificationException;

    // ------------------------------------

    /**
     * @returns a String describing this instance components
     */
    public String getComponentsDescription();

}