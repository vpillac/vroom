package vroom.optimization.online.jmsa;

import static vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes.MSA_EVENT_HANDLING_END;
import static vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes.MSA_EVENT_HANDLING_START;
import static vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes.MSA_NEW_EVENT;

import java.util.Arrays;

import vroom.common.heuristics.ProcedureStatus;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.events.EventHandlingException;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.components.RequestSamplerParam;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorParam;
import vroom.optimization.online.jmsa.events.GenerateEvent;
import vroom.optimization.online.jmsa.events.MSAEvent;
import vroom.optimization.online.jmsa.events.OptimizeEvent;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>MSASequential</code> is the class responsible for running a multiple scenario approach procedure.
 * 
 * @param <S>
 *            the type of scenario that will be handled in by the MSA algorithm
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:45 a.m.
 */
public class MSASequential<S extends IScenario, I extends IInstance> extends MSABase<S, I> {

    /**
     * Constructor for a new MSA procedure
     * 
     * @param parameters
     *            the global parameters that will be used for this MSA procedure
     * @param instance
     *            the instance on which this msa procedure will be based
     * @see MSAGlobalParameters
     */
    public MSASequential(I instance, MSAGlobalParameters parameters) {
        super(instance, parameters);
    }

    /**
     * Utility method used to log a message if the procedure has to be aborted
     * 
     * @return <code>true</code> if the procedure is to be continued, <code>false</code> if it has to be aborted
     */
    private boolean checkRunningState() {
        // Abort if the the procedure is no longer running
        if (!isRunning()) {
            MSALogging.getProcedureLogger().info(
                    "MSA procedure is not running - aborting running:%s paused:%s", isRunning(),
                    isPaused());
            return false;
        } else {
            return true;
        }
    }

    /**
     * The main method for the MSA procedure
     */
    @Override
    protected void msaProcedure() {
        Stopwatch procTimer = new Stopwatch();

        setStatus(ProcedureStatus.INITIALIZATION);

        setInitialized(false);

        int poolSize = getParameter(MSAGlobalParameters.POOL_SIZE);
        double initialProp = getParameter(MSAGlobalParameters.POOL_INITIAL_PROPORTION);

        int sampledReqCount = getParameter(MSAGlobalParameters.SAMPLED_REQUEST_COUNT);

        ScenarioGeneratorParam params = new ScenarioGeneratorParam((int) (poolSize * initialProp),
                getParameter(MSAGlobalParameters.GEN_MAX_SCEN_OPT_TIME), new RequestSamplerParam(
                        sampledReqCount));

        procTimer.start();
        MSALogging.getProcedureLogger().info("Scenario pool initialization started: %s", params);
        mComponentManager.generateScenarios(params);
        setStatus(ProcedureStatus.INITIALIZED);
        setInitialized(true);
        procTimer.stop();
        MSALogging.getProcedureLogger().info(
                "Scenario pool initialization terminated in %ss (%s scenarios in the pool)",
                procTimer.readTimeS(), mPool.size());
        MSALogging.getProcedureLogger().debug("Current state: %ss", this);

        mEventFactory.raiseOptimizeEvent();

        setStatus(ProcedureStatus.RUNNING);
        while (isRunning()) {
            // TODO add periodic cleaning of the pool
            MSALogging.getProcedureLogger().lowDebug("Processing of the next event");
            try {
                waitUntilUnpaused();
            } catch (InterruptedException e) {
                // The waiting has been interrupted
                // - Log the error
                MSALogging
                        .getProcedureLogger()
                        .exception(
                                "MSASequential.msaProcedure while waiting for the next event, aborting the MSA procedure",
                                e);
                // - Terminate the MSA procedure
                setStatus(ProcedureStatus.EXCEPTION);
                stop();
            }

            // Get the next event
            setCurrentEvent(null);

            try {
                setCurrentEvent(this.takeNextEvent());
            } catch (InterruptedException e) {
                MSALogging
                        .getProcedureLogger()
                        .exception(
                                "MSASequential.msaProcedure while retrieving the next event, aborting the MSA procedure",
                                e);
                // - Terminate the MSA procedure
                setStatus(ProcedureStatus.EXCEPTION);
                stop();
            }

            // Abort if the the procedure is no longer running
            if (!checkRunningState()) {
                break;
            }

            // If the event is null, then this iteration should be
            // aborted
            if (getCurrentEvent() == null) {
                break;
            }

            MSALogging.getProcedureLogger().lowDebug("Next event successfully retreived: %s",
                    getCurrentEvent());
            MSALogging.getProcedureLogger().lowDebug("Event queue: %s)",
                    Arrays.toString(mEventQueue.getPendingEvents()));

            // Get the associated event handler
            IEventHandler<MSAEvent> handler = mEventHandlerManager
                    .getEventHandler(getCurrentEvent());

            // Check if the handler exists
            if (handler == null) {
                // Log the error
                MSALogging
                        .getProcedureLogger()
                        .error("MSA procedure was not able to handle the event : there is no hanlder associated with this event (%s)",
                                getCurrentEvent());
            } else {
                MSALogging.getProcedureLogger().lowDebug(
                        "Event handler successfully retreived (%s)", handler);

                // Abort if the the procedure is no longer running
                if (!checkRunningState()) {
                    break;
                }

                // Execute callbacks
                callbacks(MSA_NEW_EVENT, getCurrentEvent(), handler);

                // Abort if the the procedure is no longer running
                if (!checkRunningState()) {
                    break;
                }

                // Check if the handler can handle the event
                if (handler.canHandleEvent(getCurrentEvent())) {

                    try {
                        waitUntilUnpaused();
                    } catch (InterruptedException e) {
                        // The waiting has been interrupted
                        // - Log the error
                        MSALogging
                                .getProcedureLogger()
                                .exception(
                                        "MSASequential.msaProcedure while waiting for the next event, aborting the MSA procedure",
                                        e);
                        // - Terminate the MSA procedure
                        stop();
                    }

                    // Handle the event
                    // Add parameters when relevant
                    if (getCurrentEvent() instanceof GenerateEvent) {
                        ((GenerateEvent) getCurrentEvent()).setParameters(getGenerateParameters());
                    } else if (getCurrentEvent() instanceof OptimizeEvent) {
                        ((OptimizeEvent) getCurrentEvent())
                                .setParameters(getOptimizeParameters(getCurrentEvent()));
                    }

                    MSALogging.getProcedureLogger().lowDebug(
                            "Handling of the event %s by handler %s", getCurrentEvent(), handler);

                    // Execute callbacks
                    callbacks(MSA_EVENT_HANDLING_START, getCurrentEvent(), handler);

                    // Abort if the the procedure is no longer running
                    if (!checkRunningState()) {
                        break;
                    }
                    // Handle the event
                    try {
                        handler.handleEvent(getCurrentEvent());
                    } catch (EventHandlingException e) {
                        // Log the exception
                        MSALogging.getProcedureLogger().error(
                                "MSA procedure was not able to handle the event %s",
                                getCurrentEvent(), e);
                    }

                } else {
                    // The handler cannot handle the event: log the error
                    MSALogging
                            .getProcedureLogger()
                            .warn("MSA procedure was not able to handle the next event: the associated handler %s cannot handle the event %s, current state: %s",
                                    handler, getCurrentEvent(), this);
                }
            }

            MSALogging.getProcedureLogger().lowDebug("Event handling finished");

            // Abort if the the procedure is no longer running
            if (!checkRunningState()) {
                break;
            }

            // Execute the callbacks
            callbacks(MSA_EVENT_HANDLING_END, getCurrentEvent(), handler);

        }
        setStatus(ProcedureStatus.TERMINATED);
    }

    /**
     * Waits for an event to become available
     * 
     * @return the next event from the event manager
     * @throws InterruptedException
     */
    synchronized MSAEvent takeNextEvent() throws InterruptedException {

        // if (this.mEventQueue.isEmpty())
        // Logging
        // .getProcedureLogger()
        // .debug(
        // "No event is currently available, will wait untill a new event becomes available");

        final ExtendedReentrantLock lock = mLock;
        lock.lockInterruptibly();
        try {
            while (mEventQueue.isEmpty() && isRunning()) {
                // Checks the paused state
                while (isPaused() && isRunning()) {
                    // Wait for the procedure to be unpaused
                    mPausedCondition.await();
                }

                if (isRunning() && mEventQueue.isEmpty()) {
                    // Waits 100ms if no event is available
                    // We wait only 100ms in case the procedure is paused in
                    // the meantime
                    // Logging.getProcedureLogger().lowDebug(
                    // "No event available, waiting 100ms");
                    mEventQueue.awaitForNewEvent(100);
                }
            }

            if (!isRunning()) {
                return null;
            } else {
                // Logging.getProcedureLogger().lowDebug(
                // "A event is available, returning it");
                return mEventQueue.pollNextEvent();
            }
        } catch (InterruptedException ie) {
            mPausedCondition.signal(); // propagate to non-interrupted
            // thread
            throw ie;
        } finally {
            lock.unlock();
        }
    }
}