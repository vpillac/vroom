/*
 * 
 */
package vroom.common.utilities.events;

/**
 * An interface for all event handlers.<br/>
 * An event handler is responsible for performing the corresponding actions when given a particular event
 * 
 * @param E
 *            the type of event handled by instances of this class
 */
public interface IEventHandler<E extends IEvent<?>> {
    /**
     * @return <code>true</code> if the given <code>event</code> can be handled, <code>false</code> otherwise
     * @param event
     *            the event which handling has to be tested
     */
    boolean canHandleEvent(E event);

    /**
     * Procedure responsible for the handling of a particular event
     * 
     * @return <code>true</code> if the given event has been successfully handled, <code>false</code> otherwise
     * @param event
     *            the event to be handled
     * @exception EventHandlingException
     */
    public boolean handleEvent(E event) throws EventHandlingException;

}