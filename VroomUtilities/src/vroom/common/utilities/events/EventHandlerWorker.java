/**
 * 
 */
package vroom.common.utilities.events;

import java.util.concurrent.Callable;

import vroom.common.utilities.logging.LoggerHelper;

/**
 * <code>CallableEvent</code> is an implementation of {@link Callable} that encapsulate an event and an handler.
 * <p>
 * It implements {@link Comparable} using an event based comparison as defined in
 * {@link EventComparator#compareEvents(IEvent, IEvent)}
 * </p>
 * <p>
 * Creation date: 31/08/2010 - 11:10:28
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class EventHandlerWorker<E extends IEvent<?>> implements Callable<Boolean>, Runnable,
        Comparable<EventHandlerWorker<?>> {

    /** the event to be handled **/
    private final E mEvent;

    /**
     * Getter for event : the event to be handled
     * 
     * @return the value of event
     */
    public E getEvent() {
        return this.mEvent;
    }

    /** the event handler **/
    private final IEventHandler<E> mHandler;

    /**
     * Getter for handler : the event handler
     * 
     * @return the value of handler
     */
    public IEventHandler<E> getHandler() {
        return this.mHandler;
    }

    /**
     * Creates a new <code>CallableEvent</code>
     * 
     * @param event
     *            the event to be handled
     * @param handler
     *            the handler to be executed
     */
    protected EventHandlerWorker(E event, IEventHandler<E> handler) {
        super();
        mEvent = event;
        mHandler = handler;
    }

    @Override
    public Boolean call() throws Exception {
        return mHandler.handleEvent(mEvent);
    }

    @Override
    public int compareTo(EventHandlerWorker<?> o) {
        return EventComparator.compareEvents(getEvent(), o.getEvent());
    }

    @Override
    public void run() {
        try {
            call();
        } catch (Exception e) {
            getLogger().exception("EventHandlerWorker.run - executing %s", e, this);
        }
    }

    @Override
    public String toString() {
        return String.format("ev:%s handler:%s", getEvent(), getHandler());
    }

    protected LoggerHelper getLogger() {
        return LoggerHelper.getLogger(getClass());
    }
}
