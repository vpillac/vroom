package vroom.optimization.online.jmsa.events;

import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.MSAParallel;
import vroom.optimization.online.jmsa.events.ResourceEvent.EventTypes;

/**
 * <code>IMSAEventFactory</code> is the class responsible for the creation and raising of events in the MSA procedure
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:02 a.m.
 * @deprecated
 */
@Deprecated
public class MSAEventFactoryMT implements IMSAEventFactory {

    public static final int         PRIORITY_POOL_UPDATE_PRE = 1;

    public static final int         PRIORITY_DECISION        = 6;

    // -- Events with the same priority are handled in arrival time
    public static final int         PRIORITY_RESOURCE_EVENT  = 5;
    public static final int         PRIORITY_NEW_REQUEST     = 5;

    // -- Low priority events raised by the MSA itself
    public static final int         PRIORITY_POOL_UPDATE     = 7;
    public static final int         PRIORITY_GENERATE        = 8;
    public static final int         PRIORITY_OPTIMIZE        = 9;

    // private final MSAEventQueue mEventQueue;
    private final MSAEventExecutor  mEventExecutor;

    private final MSAParallel<?, ?> mParentMSA;

    /**
     * Getter for the parent MSA.
     * 
     * @return the parent MSA instance for this event factory
     */
    @Override
    public MSAParallel<?, ?> getParentMSA() {
        return mParentMSA;
    }

    /**
     * @param eventQueue
     */
    public MSAEventFactoryMT(MSAParallel<?, ?> parentMSA, MSAEventExecutor eventExecutor) {
        super();
        mEventExecutor = eventExecutor;
        mParentMSA = parentMSA;
    }

    /**
     * Creates a new {@link DecisionEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    @Override
    public boolean raiseDecisionEvent() {
        return raiseEvent(new DecisionEvent(0, this));
    }

    /**
     * Creates a new {@link ResourceEvent} of type {@link EventTypes#REQUEST_ASSIGNED} and pushes it to the event
     * manager.
     * 
     * @param resourceId
     *            the id of the resource that will be committed to <code>assignedRequest</code>
     * @param assignedRequest
     *            the request that will be served next by the specified resource
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    @Override
    public boolean raiseRequestAssignedEvent(int resourceId, IActualRequest assignedRequest) {
        return raiseEvent(ResourceEvent.newRequestAssignedEvent(0, this, resourceId, assignedRequest, null));
    }

    /**
     * Creates a new {@link NewRequestEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @param request
     *            the new request
     */
    @Override
    public boolean raiseNewRequestEvent(IActualRequest request) {
        return raiseEvent(new NewRequestEvent(0, this, request));
    }

    /**
     * Creates a new {@link ResourceEvent} representing an start of service and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @param servedRequest
     *            the request which servicing has been started
     * @param resourceId
     *            the id (index) of the corresponding resource *
     * @param additionalInfo
     *            an optional object that can be used to carry
     * @see ResourceEvent#newStartOfServiceEvent(double, IMSAEventFactory, int, IActualRequest, Object)
     */
    @Override
    public boolean raiseStartOfServiceEvent(int resourceId, IActualRequest servedRequest, Object additionalInfo) {
        return servedRequest != null
                && raiseEvent(ResourceEvent.newStartOfServiceEvent(0, this, resourceId, servedRequest, additionalInfo));
    }

    /**
     * Creates a new {@link ResourceEvent} representing an end of service and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @param servedRequest
     *            the request that has been served
     * @param resourceId
     *            the id (index) of the corresponding resource
     * @see ResourceEvent#newEndOfServiceEvent(double, IMSAEventFactory, int, IActualRequest, Object)
     */
    @Override
    public boolean raiseEndOfServiceEvent(int resourceId, IActualRequest servedRequest) {
        return servedRequest != null
                && raiseEvent(ResourceEvent.newEndOfServiceEvent(0, this, resourceId, servedRequest, null));
    }

    /**
     * Creates a new {@link ResourceEvent} representing a start of service and pushes it to the event manager.
     * 
     * @param resourceId
     *            the id (index) of the resource that starts servicing requests
     * @param param
     *            an optional parameter to be added in the event
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @see ResourceEvent#newStartServiceEvent(double, IMSAEventFactory, int, Object)
     */
    @Override
    public boolean raiseResourceStart(int resourceId, Object param) {
        return raiseEvent(ResourceEvent.newStartServiceEvent(0, this, resourceId, param));
    }

    /**
     * Creates a new {@link ResourceEvent} representing a definitive end of service and pushes it to the event manager.
     * 
     * @param resourceId
     *            the id (index) of the resource that is no longer available
     * @param param
     *            an optional parameter to be passed in the event
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @see ResourceEvent#newStopServiceEvent(double, IMSAEventFactory, int, Object)
     */
    @Override
    public boolean raiseResourceStop(int resourceId, Object param) {
        return raiseEvent(ResourceEvent.newStopServiceEvent(0, this, resourceId, param));

    }

    /**
     * Creates a new {@link PoolUpdateEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    @Override
    public boolean raisePoolUpdateEvent() {
        return raisePoolUpdateEvent(false);
    }

    /**
     * Creates a new {@link PoolUpdateEvent} and pushes it to the event manager.
     * 
     * @param preemptive
     *            the prehemptivity of the raised event. If <code>true</code> the event will be handled before any other
     *            event.
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    @Override
    public boolean raisePoolUpdateEvent(boolean preemptive) {
        return raiseEvent(new PoolUpdateEvent(this, preemptive));
    }

    /**
     * Creates a new {@link GenerateEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    @Override
    public boolean raiseGenerateScenarioEvent() {
        return raiseEvent(new GenerateEvent(this));
    }

    /**
     * Creates a new {@link OptimizeEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    @Override
    public boolean raiseOptimizeEvent() {
        return raiseEvent(new OptimizeEvent(this));
    }

    /**
     * Raise the given <code>event</code>
     * <p>
     * This method should be exclusively used for user specialization of {@link MSAEvent}. Build-in events should be
     * raised using the dedicated methods provided by this class
     * </p>
     * 
     * @param event
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    @Override
    public boolean raiseEvent(MSAEvent event) {
        return mEventExecutor.pushEvent(event);
    }

}// end IMSAEventFactory