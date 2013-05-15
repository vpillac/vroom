package vroom.optimization.online.jmsa.events;

import vroom.optimization.online.jmsa.IActualRequest;

/**
 * <code>EndOfServiceEvent</code> is a preemptive event that is raised when a resource start its servicing, finish it,
 * is assigned to a request or finish the servicing of a given request.
 * 
 * @see EventTypes
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 3.0 #updated 16-Feb-2010 10:06:58 a.m.
 */
public class ResourceEvent extends MSAEvent {

    /**
     * Creation date: Apr 13, 2010 - 8:56:49 AM<br/>
     * <code>EventTypes</code> is an enumeration of the different types of events that can be associated with a resource
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    public static enum EventTypes {
        /** Event raised when a resource starts its servicing */
        START,
        /** Event raised when a resource stops its servicing */
        STOP,
        /** Event raised when a resource is assigned to a request */
        REQUEST_ASSIGNED,
        /** Event raised when a resource starts the servicing of a request */
        START_OF_SERVICE,
        /** Event raised when a resource finishes the servicing of a request */
        END_OF_SERVICE
    };

    private final EventTypes     mEventType;

    private final IActualRequest mRequest;
    private final int            mResourceId;

    /**
     * Creates a new <code>ResourceEvent</code>
     * 
     * @param simTime
     * @param type
     * @param source
     * @param resourceId
     * @param request
     * @param additionalInfo
     */
    private ResourceEvent(double simTime, EventTypes type, IMSAEventFactory source, int resourceId,
            IActualRequest request, Object additionalInfo) {
        super(IMSAEventFactory.PRIORITY_RESOURCE_EVENT, simTime, source, additionalInfo);
        mRequest = request;
        mResourceId = resourceId;
        mEventType = type;
    }

    /**
     * Factory method for the event representing the start of service of a resource
     * 
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     *            the {@link IMSAEventFactory} that generated the event
     * @param resourceId
     *            the id (index) of the corresponding resource
     * @param additionalInfo
     *            an optional object that can be used to carry
     * @return a new {@link ResourceEvent} representing the start of a service by the resource
     * @see EventTypes#START
     */
    public static ResourceEvent newStartServiceEvent(double simTime, IMSAEventFactory source, int resourceId,
            Object additionalInfo) {
        return new ResourceEvent(simTime, EventTypes.START, source, resourceId, null, additionalInfo);
    }

    /**
     * Factory method for the event representing the definitive end of service of a resource
     * 
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     *            the {@link IMSAEventFactory} that generated the event
     * @param resourceId
     *            the id (index) of the corresponding resource
     * @param additionalInfo
     *            an optional object that can be used to carry
     * @return a new {@link ResourceEvent} representing the end of a service by the resource
     * @see EventTypes#STOP
     */
    public static ResourceEvent newStopServiceEvent(double simTime, IMSAEventFactory source, int resourceId,
            Object additionalInfo) {
        return new ResourceEvent(simTime, EventTypes.STOP, source, resourceId, null, additionalInfo);
    }

    /**
     * Factory method for the event representing the start of the service of a request by a resource
     * 
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     *            the {@link IMSAEventFactory} that generated the event
     * @param resourceId
     *            the id (index) of the corresponding resource
     * @param servedRequest
     *            the request which servicing has started
     * @param additionalInfo
     *            an optional object that can be used to carry
     * @return a new {@link ResourceEvent} representing a start of service event for the given
     *         <code>servedRequest</code>
     * @see EventTypes#START_OF_SERVICE
     */
    public static ResourceEvent newStartOfServiceEvent(double simTime, IMSAEventFactory source, int resourceId,
            IActualRequest servedRequest, Object additionalInfo) {
        return new ResourceEvent(simTime, EventTypes.START_OF_SERVICE, source, resourceId, servedRequest,
                additionalInfo);
    }

    /**
     * Factory method for the event representing the end of the service of a request by a resource
     * 
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     *            the {@link IMSAEventFactory} that generated the event
     * @param resourceId
     *            the id (index) of the corresponding resource
     * @param servedRequest
     *            the request that has been served
     * @param additionalInfo
     *            an optional object that can be used to carry
     * @return a new {@link ResourceEvent} representing a end of service event for the given <code>servedRequest</code>
     * @see EventTypes#END_OF_SERVICE
     */
    public static ResourceEvent newEndOfServiceEvent(double simTime, IMSAEventFactory source, int resourceId,
            IActualRequest servedRequest, Object additionalInfo) {
        return new ResourceEvent(simTime, EventTypes.END_OF_SERVICE, source, resourceId, servedRequest, additionalInfo);
    }

    /**
     * Factory method for the event representing the assignment of a resource to a request
     * 
     * @param simTime
     *            the time at which this event occurred in a simulation setting (optional)
     * @param source
     *            the {@link IMSAEventFactory} that generated the event
     * @param resourceId
     *            the id (index) of the corresponding resource
     * @param assignedRequest
     *            the request to be assigned to the specified resource
     * @param additionalInfo
     *            an optional object that can be used to carry
     * @return a new {@link ResourceEvent} representing a request assignment for the given pair
     *         <code>assignedRequest</code> <code>resourceId</code>
     * @see EventTypes#REQUEST_ASSIGNED
     */
    public static ResourceEvent newRequestAssignedEvent(double simTime, IMSAEventFactory source, int resourceId,
            IActualRequest assignedRequest, Object additionalInfo) {
        return new ResourceEvent(simTime, EventTypes.REQUEST_ASSIGNED, source, resourceId, assignedRequest,
                additionalInfo);
    }

    @Override
    public boolean isPreemptive() {
        return true;
    }

    /**
     * @return the request which service has finished
     */
    public IActualRequest getRequest() {
        return mRequest;
    }

    /**
     * @return the id of the corresponding resource.
     */
    public int getResourceId() {
        return mResourceId;
    }

    /**
     * @return the type of this event
     */
    public EventTypes getType() {
        return mEventType;
    }

    @Override
    public String toString() {
        return String.format("%s (t:%s,res:%s,req:%s)", super.toString(), getType(), getResourceId(), getRequest());
    }

    @Override
    public String toShortString() {
        return getRequest() != null ? String.format("%s:%s %s", getType(), getResourceId(), getRequest().getID())
                : String.format("%s:%s", getType(), getResourceId());
    }
}