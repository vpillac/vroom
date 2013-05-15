package vroom.optimization.online.jmsa.events;

import java.util.Arrays;

import vroom.common.utilities.events.EventExecutor;
import vroom.common.utilities.events.EventHandlerManager;
import vroom.common.utilities.events.IEvent;
import vroom.common.utilities.events.IEventHandler;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>MSAEventExecutor</code> is a specialization of the generic {@link EventExecutor} with specific handling of the
 * {@link MSAEvent}
 * <p>
 * Creation date: 31/08/2010 - 13:33:52
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MSAEventExecutor extends EventExecutor {

    private final MSABase<?, ?> mMSA;

    /**
     * Creates a new <code>MSAEventExecutor</code>
     * 
     * @param minThreads
     *            the minimum number of threads to be used to handle events
     * @param maxThreads
     *            the maximum number of threads
     * @param manager
     *            an {@link EventHandlerManager} used to fetch the {@link IEventHandler} associated with an event
     */
    public MSAEventExecutor(int minThreads, int maxThreads, EventHandlerManager manager,
            MSABase<?, ?> msa) {
        super(minThreads, maxThreads, manager);
        mMSA = msa;
    }

    /**
     * Add an event to the current pending queue, and ensures that there is no duplicate events in the queue.
     * 
     * @return <code>true</code> if <code>event</code> was successfully added to the queue, <code>false</code> otherwise
     * @param event
     *            the event to be added to the queue
     * @throws InterruptedException
     */
    public synchronized boolean pushEvent(MSAEvent event) {
        boolean b;
        // Ensure that there is no duplicate event in the queue
        if (event.getClass() == ResourceEvent.class || event.getClass() == NewRequestEvent.class
                || !contains(event.getClass())) {

            b = super.pushEvent(event);

            if (b) {
                MSALogging.getEventsLogger().debug(
                        "pushEvent: Event added to the queue (event:%s queue:%s)", event,
                        Arrays.toString(getPendingEvents()));
            } else {
                MSALogging.getEventsLogger().warn(
                        "pushEvent: Error while adding event %s to the queue", event);
            }
        } else {
            b = false;
            MSALogging.getEventsLogger().debug(
                    "pushEvent: Duplicated event ignored (event:%s queue:%s)", event,
                    Arrays.toString(getPendingEvents()));
        }
        return b;
    }

    /**
     * Schedule the handling of an event
     * 
     * @param <E>
     *            the type of event
     * @param event
     *            the event to be handled
     * @param handler
     *            the associated handler
     */
    @Override
    public synchronized <E extends IEvent<?>> void execute(E event, IEventHandler<E> handler) {
        MSAEventHandlerWorker<E> worker = new MSAEventHandlerWorker<E>(event, handler, mMSA);
        super.execute(worker);
    }

    /**
     * Checks if an event of the specified class is present in the queue
     * 
     * @param eventClass
     *            the class of event to be searched
     * @return <code>true</code> if the current queue contains at list one event of class <code>eventClass</code>,
     *         <code>false</code> otherwise
     */
    public synchronized boolean contains(Class<? extends IEvent<?>> eventClass) {
        if (getPendingEventsCount() == 0) {
            return false;
        }
        for (IEvent<?> e : getPendingEvents()) {
            if (e != null && e.getClass() == eventClass) {
                return true;
            }
        }
        return false;
    }

}// end MSAEventQueue