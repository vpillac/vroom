package vroom.optimization.online.jmsa.utils;

import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.events.MSAEvent;
import vroom.optimization.online.jmsa.events.NewRequestEvent;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.utils.MSASimulator.ResourceState;
import vroom.optimization.online.jmsa.utils.MSASimulator.ResourceStates;

/**
 * The class <code>MSASimulationCallback</code> is designed to simplify the design of a simulation scheme around MSA. It
 * contains default cases that are useful when doing a simulation.
 * <p>
 * Creation date: Mar 15, 2012 - 5:30:05 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
/**
 * <p>
 * Creation date: Mar 15, 2012 - 5:36:20 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class MSASimulationCallback extends MSACallbackBase {

    protected final MSASimulator mSimulator;

    /**
     * Creates a new <code>MSASimulationCallback</code>
     * 
     * @param simulator
     *            the {@link MSASimulator} to which this simulation callback is linked
     */
    public MSASimulationCallback(MSASimulator simulator) {
        super();
        mSimulator = simulator;
    }

    /**
     * Returns the {@link MSASimulator} to which this simulation callback is linked
     * 
     * @return the {@link MSASimulator} to which this simulation callback is linked
     */
    public MSASimulator getSimulator() {
        return mSimulator;
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isExecutedSynchronously() {
        return true;
    }

    /**
     * Register this instance as a callback on all {@linkplain EventTypes event types}
     * 
     * @param msa
     *            the procedure in which this instance will be registered
     */
    public void registerAsCallback(MSABase<?, ?> msa) {
        for (EventTypes t : EventTypes.values())
            msa.registerCallback(t, this);
    }

    @Override
    public void execute(MSACallbackEvent event) {
        MSALogging.getBaseLogger()
                .debug("TRSPSimulationCallback.execute: handling event %s", event);
        beforeExecute(event);
        switch (event.getType()) {
        case EVENTS_RESOURCE:
            ResourceEvent e = (ResourceEvent) event.getParams()[0];
            switch (e.getType()) {
            case START:
                resourceStarted(event, e);
                break;
            case STOP:
                resourceStopped(event, e);
                break;
            case REQUEST_ASSIGNED:
                requestAssigned(event, e);
                break;
            case START_OF_SERVICE:
                startOfService(event, e);
                break;
            case END_OF_SERVICE:
                endOfService(event, e);
                break;
            }
            break;
        case MSA_NEW_DISTINGUISHED_SOLUTION:
            IDistinguishedSolution oldS = event.getParams()[0] != null ? (IDistinguishedSolution) event
                    .getParams()[0] : null;
            IDistinguishedSolution newS = event.getParams()[1] != null ? (IDistinguishedSolution) event
                    .getParams()[1] : null;
            newDistinguishedSolution(event, oldS, newS);
            break;
        case MSA_START:
            msaStarted(event);
            break;
        case MSA_END:
            msaEnded(event);
            break;
        case MSA_NEW_EVENT:
            MSAEvent ev = (MSAEvent) event.getParams()[0];
            if (NewRequestEvent.class.isInstance(ev))
                newRequest(event, (NewRequestEvent) ev);
            break;
        default: // Do nothing
            break;
        }
        afterExecture(event);
    }

    /**
     * This method will be called at the beginning of {@link #execute(vroom.common.utilities.callbacks.ICallbackEvent)}
     * <p>
     * The default behavior of this method is to acquire the lock on the attached {@link #getSimulator() simulator}
     * </p>
     * 
     * @param event
     *            the event that triggered the callback
     */
    protected void beforeExecute(MSACallbackEvent event) {
        getSimulator().acquireLock();
    }

    /**
     * This method will be called at the end of {@link #execute(vroom.common.utilities.callbacks.ICallbackEvent)}, even
     * if an exception was thrown
     * <p>
     * The default behavior of this method is to release the lock on the attached {@link #getSimulator() simulator}
     * </p>
     * 
     * @param event
     *            the event that triggered the callback
     */
    protected void afterExecture(MSACallbackEvent event) {
        getSimulator().releaseLock();
    }

    /**
     * This method will be called when the MSA procedure has been started
     * 
     * @param cbEvent
     *            the original callback event
     */
    protected void msaStarted(MSACallbackEvent event) {
    }

    /**
     * This method will be called when the MSA procedure finished
     * 
     * @param cbEvent
     *            the original callback event
     */
    protected void msaEnded(MSACallbackEvent event) {
    }

    /**
     * This method will be called when a resource started its service
     * <p>
     * By default it will set the {@link MSASimulator#getState(int) state} of the corresponding resource to
     * {@link ResourceStates#SERVICING}
     * </p>
     * 
     * @param cbEvent
     *            the original callback event
     * @param e
     *            the original resource event
     */
    protected void startOfService(MSACallbackEvent cbEvent, ResourceEvent e) {
        getSimulator().getState(e.getResourceId()).setState(ResourceStates.SERVICING);
    }

    /**
     * This method will be called when a resource finished its service
     * <p>
     * By default it will set the {@link MSASimulator#getState(int) state} of the corresponding resource to
     * {@link ResourceStates#IDLE}, remove the request from the resource's
     * {@linkplain ResourceState#getAssignedRequests() list of assigned requests}, and add the request to the resource's
     * {@linkplain ResourceState#getServedRequests() list of served requests}
     * </p>
     * 
     * @param cbEvent
     *            the original callback event
     * @param e
     *            the original resource event
     */
    protected void endOfService(MSACallbackEvent cbEvent, ResourceEvent e) {
        ResourceState state = getSimulator().getState(e.getResourceId());
        state.setState(ResourceStates.IDLE);
        state.addServedRequest(e.getRequest());
        state.removeAssignedRequest(e.getRequest());
    }

    /**
     * This method will be called when a resource is assigned a request
     * <p>
     * By default it will set the {@link MSASimulator#getState(int) state} of the corresponding resource to
     * {@link ResourceStates#BUSY} and add the request to the resource's
     * {@linkplain ResourceState#getAssignedRequests() list of assigned requests}
     * </p>
     * 
     * @param cbEvent
     *            the original callback event
     * @param e
     *            the original resource event
     */
    protected void requestAssigned(MSACallbackEvent cbEvent, ResourceEvent e) {
        ResourceState state = getSimulator().getState(e.getResourceId());
        state.setState(ResourceStates.BUSY);
        state.addAssignedRequest(e.getRequest());
    }

    /**
     * This method will be called when a resource has started
     * <p>
     * By default it will set the {@link MSASimulator#getState(int) state} of the corresponding resource to
     * {@link ResourceStates#IDLE}
     * </p>
     * 
     * @param cbEvent
     *            the original callback event
     * @param e
     *            the original resource event
     */
    protected void resourceStarted(MSACallbackEvent cbEvent, ResourceEvent e) {
        getSimulator().getState(e.getResourceId()).setState(ResourceStates.IDLE);
    }

    /**
     * This method will be called when a resource has stopped
     * <p>
     * By default it will set the {@link MSASimulator#getState(int) state} of the corresponding resource to
     * {@link ResourceStates#STOPPED}
     * </p>
     * 
     * @param cbEvent
     *            the original callback event
     * @param e
     *            the original resource event
     */
    protected void resourceStopped(MSACallbackEvent cbEvent, ResourceEvent e) {
        getSimulator().getState(e.getResourceId()).setState(ResourceStates.STOPPED);
    }

    /**
     * This method will be called when a new distinguished solution has been produced
     * 
     * @param cbEvent
     *            the original callback event
     */
    protected void newDistinguishedSolution(MSACallbackEvent event, IDistinguishedSolution oldSol,
            IDistinguishedSolution newSol) {
    }

    /**
     * This method will be called when a new request event is retrieved by the jMSA procedure
     * 
     * @param event
     *            the original callback event
     * @param ev
     *            the new request event
     */
    protected void newRequest(MSACallbackEvent event, NewRequestEvent e) {

    }

}