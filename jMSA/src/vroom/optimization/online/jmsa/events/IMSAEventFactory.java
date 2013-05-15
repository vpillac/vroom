package vroom.optimization.online.jmsa.events;

import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.ResourceEvent.EventTypes;

public interface IMSAEventFactory {

    public static final int PRIORITY_POOL_UPDATE_PRE = 1;
    public static final int PRIORITY_DECISION        = 6;
    // -- Events with the same priority are handled in arrival time
    public static final int PRIORITY_RESOURCE_EVENT  = 5;
    public static final int PRIORITY_NEW_REQUEST     = 5;
    // -- Low priority events raised by the MSA itself
    public static final int PRIORITY_POOL_UPDATE     = 7;
    public static final int PRIORITY_GENERATE        = 8;
    public static final int PRIORITY_OPTIMIZE        = 9;

    /**
     * Getter for the parent MSA.
     * 
     * @return the parent MSA instance for this event factory
     */
    public MSABase<?, ?> getParentMSA();

    /**
     * Creates a new {@link DecisionEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    public boolean raiseDecisionEvent();

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
    public boolean raiseRequestAssignedEvent(int resourceId, IActualRequest assignedRequest);

    /**
     * Creates a new {@link NewRequestEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @param request
     *            the new request
     */
    public boolean raiseNewRequestEvent(IActualRequest request);

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
     * @see ResourceEvent#newStartOfServiceEvent(double, MSAEventFactoryST, int, IActualRequest, Object)
     */
    public boolean raiseStartOfServiceEvent(int resourceId, IActualRequest servedRequest,
            Object additionalInfo);

    /**
     * Creates a new {@link ResourceEvent} representing an end of service and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @param servedRequest
     *            the request that has been served
     * @param resourceId
     *            the id (index) of the corresponding resource
     * @see ResourceEvent#newEndOfServiceEvent(double, MSAEventFactoryST, int, IActualRequest, Object)
     */
    public boolean raiseEndOfServiceEvent(int resourceId, IActualRequest servedRequest);

    /**
     * Creates a new {@link ResourceEvent} representing a start of service and pushes it to the event manager.
     * 
     * @param resourceId
     *            the id (index) of the resource that starts servicing requests
     * @param param
     *            an optional parameter to be added in the event
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @see ResourceEvent#newStartServiceEvent(double, MSAEventFactoryST, int, Object)
     */
    public boolean raiseResourceStart(int resourceId, Object param);

    /**
     * Creates a new {@link ResourceEvent} representing a definitive end of service and pushes it to the event manager.
     * 
     * @param resourceId
     *            the id (index) of the resource that is no longer available
     * @param param
     *            an optional parameter to be passed in the event
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     * @see ResourceEvent#newStopServiceEvent(double, MSAEventFactoryST, int, Object)
     */
    public boolean raiseResourceStop(int resourceId, Object param);

    /**
     * Creates a new {@link PoolUpdateEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    public boolean raisePoolUpdateEvent();

    /**
     * Creates a new {@link PoolUpdateEvent} and pushes it to the event manager.
     * 
     * @param preemptive
     *            the prehemptivity of the raised event. If <code>true</code> the event will be handled before any other
     *            event.
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    public boolean raisePoolUpdateEvent(boolean preemptive);

    /**
     * Creates a new {@link GenerateEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    public boolean raiseGenerateScenarioEvent();

    /**
     * Creates a new {@link OptimizeEvent} and pushes it to the event manager.
     * 
     * @return <code>true</code> if the event was successfully raised, <code>false</code> otherwise
     */
    public boolean raiseOptimizeEvent();

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
    public boolean raiseEvent(MSAEvent event);

}