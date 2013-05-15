package vroom.optimization.online.jmsa.events;

import java.util.Arrays;

import vroom.common.utilities.events.EventComparator;
import vroom.common.utilities.events.EventQueue;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>MSAEventQueue</code> is a specialization of the generic {@link EventQueue} with specific handling of the <code>MSAEvent</code>
 * 
 * @see vroom.common.utilities.events.EventQueue
 * @see MSAEvent
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:04 a.m.
 */
public class MSAEventQueue extends EventQueue<MSAEvent> {

    public MSAEventQueue() {
        super(new EventComparator());
    }

    /**
     * Add an event to the current pending queue, and ensures that there is no duplicate events in the queue.
     * 
     * @return <code>true</code> if <code>event</code> was successfully added to the queue, <code>false</code> otherwise
     * @param event
     *            the event to be added to the queue
     * @throws InterruptedException
     */
    @Override
    public boolean pushEvent(MSAEvent event) throws InterruptedException {
        boolean b;
        // Ensure that there is no duplicate event in the queue
        if (event.getClass() == ResourceEvent.class || event.getClass() == NewRequestEvent.class
                || !super.contains(event.getClass())) {

            b = super.pushEvent(event);

            if (b) {
                if (!(event instanceof GenerateEvent) && !(event instanceof OptimizeEvent)) {
                    MSALogging.getEventsLogger().debug(
                        "MSAEventQueue.pushEvent: Event added to the queue (event:%s queue:%s)", event,
                        Arrays.toString(getPendingEvents()));
                }
            } else {
                MSALogging.getEventsLogger().warn(
                    "MSAEventQueue.pushEvent: Error while adding event %s to the queue", event);
            }
        } else {
            b = false;
            MSALogging.getEventsLogger().debug(
                "MSAEventQueue.pushEvent: Duplicated event ignored (event:%s queue:%s)", event,
                Arrays.toString(getPendingEvents()));
        }
        return b;
    }

}// end MSAEventQueue