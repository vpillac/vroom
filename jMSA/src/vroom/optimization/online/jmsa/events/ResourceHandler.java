package vroom.optimization.online.jmsa.events;

import vroom.common.utilities.events.EventHandlingException;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>EndOfServiceHandler<code> is an implementation of  {@link
 * IEventHandler}responsible of the handling of {@link ResourceEvent} events.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:59 a.m.
 */
public class ResourceHandler<S extends IScenario, I extends IInstance> extends
        MSAEventHandler<ResourceEvent, S, I> {

    public ResourceHandler(MSAProxy<S, I> parentMSA) {
        super(parentMSA);
    }

    @Override
    public boolean canHandleEvent(ResourceEvent event) {
        return true;
    }

    @Override
    public boolean handleEvent(ResourceEvent event) throws EventHandlingException {

        boolean result = false;

        switch (event.getType()) {
        case START:
            MSALogging.getEventsLogger().info(
                    "ResourceHandler.handleEvent: Start servicing handler (%s)", event);

            getParentMSAProxy().getInstance().acquireLock();
            getParentMSAProxy().getInstance().setResourceStarted(event.getResourceId(),
                    event.getAdditionalInformation());
            getParentMSAProxy().getInstance().releaseLock();

            getParentMSAProxy().getComponentManager().startServicingUpdate(event.getResourceId());

            result = true;

            break;
        case STOP:
            MSALogging.getEventsLogger().info(
                    "ResourceHandler.handleEvent: Stop servicing handler (%s)", event);

            getParentMSAProxy().getInstance().acquireLock();
            getParentMSAProxy().getInstance().setResourceStopped(event.getResourceId(),
                    event.getAdditionalInformation());
            getParentMSAProxy().getInstance().releaseLock();

            getParentMSAProxy().getComponentManager().stopServicingUpdate(event.getResourceId());

            result = true;

            break;
        case REQUEST_ASSIGNED:
            MSALogging.getEventsLogger().info(
                    "ResourceHandler.handleEvent: Enforcing decision (req:%s res:%s)",
                    event.getRequest(), event.getResourceId());

            getParentMSAProxy().getInstance().acquireLock();
            result = getParentMSAProxy().getInstance().assignRequestToResource(event.getRequest(),
                    event.getResourceId());
            getParentMSAProxy().getInstance().releaseLock();
            if (!result) {
                MSALogging.getEventsLogger().warn(
                        "ResourceHandler.handleEvent: Error when calling IInstance.commitResourceToRequest, "
                                + "ignoring the event (method returned false) (req:%s res:%s)",
                        event.getRequest(), event.getResourceId());
            } else {
                getParentMSAProxy().getComponentManager().enforceDecision(event.getResourceId(),
                        event.getRequest());
            }
            break;
        case START_OF_SERVICE:
            MSALogging.getEventsLogger().info(
                    "ResourceHandler.handleEvent: Start of service handler (%s)", event);

            getParentMSAProxy().getComponentManager().startOfServiceUpdate(event.getResourceId(),
                    event.getRequest());

            result = true;

            break;
        case END_OF_SERVICE:
            MSALogging.getEventsLogger().info(
                    "ResourceHandler.handleEvent: End of service handler (%s)", event);

            getParentMSAProxy().getComponentManager().endOfServiceUpdate(event.getResourceId(),
                    event.getRequest());

            getParentMSAProxy().getInstance().acquireLock();
            result = getParentMSAProxy().getInstance().markRequestAsServed(event.getRequest(),
                    event.getResourceId());
            getParentMSAProxy().getInstance().releaseLock();

            if (!result) {
                MSALogging
                        .getEventsLogger()
                        .error("ResourceHandler.handleEvent: Unable to mark the request as served (req:%s)",
                                event.getRequest());
            }

            // event.getSource().raisePoolUpdateEvent(true);
            event.getSource().raiseDecisionEvent();
            break;
        default:
            MSALogging.getEventsLogger().info(
                    "ResourceHandler.handleEvent: Nothing to do for event %s", event);
            result = true;
            break;
        }

        getParentMSAProxy().callbacks(EventTypes.EVENTS_RESOURCE, new Object[] { event, result });

        return result;
    }
}