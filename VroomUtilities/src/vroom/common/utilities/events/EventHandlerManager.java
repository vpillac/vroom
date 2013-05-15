/*
 * 
 */
package vroom.common.utilities.events;

import java.util.HashMap;
import java.util.Map;

/**
 * <code>EventHandlerManager</code> is an utility class that allows the association of event <b>types</b> (
 * <code>Class<</>? extends IEvent<</>?>></code>) to event handlers (IEventHandler<</>? extends IEvent<</>?>>).
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @see IEvent
 * @see IEventHandler
 */
public class EventHandlerManager {

    /**
     * This class is used to associate event types to event handlers. It is also responsible of the forwarding of an
     * event to the associated handler.
     */
    private final Map<Class<? extends IEvent<?>>, IEventHandler<? extends IEvent<?>>> mEventsHandlers;

    /**
     * Creates a new <code>EventHandlerManager</code> based on the mapping of event types to handlers
     */
    public EventHandlerManager() {
        mEventsHandlers = new HashMap<Class<? extends IEvent<?>>, IEventHandler<? extends IEvent<?>>>();
    }

    /**
     * @param event
     *            the event for which an handler is needed
     * @return the event handler corresponding to <code>event</code>
     */
    @SuppressWarnings("unchecked")
    public <E extends IEvent<?>> IEventHandler<E> getEventHandler(E event) {
        if (event == null) {
            throw new IllegalArgumentException("Argument event cannot be null");
        }

        return (IEventHandler<E>) mEventsHandlers.get(event.getClass());
    }

    /**
     * @param eventClass
     *            the event class that needs to be associated with the <code>handler</code>
     * @param handler
     *            the event handler that will be associated with events of class <code>eventClass</code>
     * @return the {@link IEventHandler} that was previously associated with type of event <code>eventClass</code> (
     *         <code>null</code> if none)
     * @throws IllegalArgumentException
     *             if one of the arguments is <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public <E extends IEvent<?>> IEventHandler<E> setEventHandler(Class<E> eventClass,
            IEventHandler<E> handler) throws IllegalArgumentException {
        if (eventClass == null) {
            throw new IllegalArgumentException("Argument eventClass cannot be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("Argument handler cannot be null");
        }

        return (IEventHandler<E>) mEventsHandlers.put(eventClass, handler);
    }

}