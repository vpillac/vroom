/**
 * 
 */
package vroom.common.utilities.events;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;

/**
 * <code>EventExecutor</code> is a specialization of {@link ThreadPoolExecutor} for the handling of {@link IEvent}.
 * <p>
 * It is aimed to replace {@link EventQueue} in a multithreading context, as it allows various events to be handled in
 * parallel by using multiple threads.
 * </p>
 * <p>
 * Creation date: 31/08/2010 - 10:57:33
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class EventExecutor {

    private final ThreadPoolExecutor  mExecutor;

    private final EventHandlerManager mHandlerManager;

    /**
     * Creates a new <code>EventExecutor</code>
     * 
     * @param minThreads
     *            the minimum number of threads to be kept in the pool
     * @param maxThreads
     *            the maximum number of threads
     * @param manager
     *            an {@link EventHandlerManager}
     */
    public EventExecutor(int minThreads, int maxThreads, EventHandlerManager manager) {
        PriorityBlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<Runnable>(20, null);
        ThreadFactory factory = new EventHandlingThreadFactory();
        RejectedExecutionHandler handler = new EventRejectedExecutionHandler();

        mHandlerManager = manager;

        mExecutor = new ThreadPoolExecutor(minThreads, maxThreads, 60, TimeUnit.SECONDS, workQueue,
                factory, handler);
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
    public synchronized <E extends IEvent<?>> void execute(E event, IEventHandler<E> handler) {
        execute(new EventHandlerWorker<E>(event, handler));
    }

    /**
     * Schedule the handling of an event
     * 
     * @param call
     *            the {@link EventHandlerWorker} to be scheduled
     */
    protected synchronized void execute(EventHandlerWorker<?> call) {
        mExecutor.execute(call);
    }

    /**
     * Schedule the handling of an event
     * 
     * @param <E>
     *            the type of event
     * @param event
     *            the event to be scheduled
     * @return <code>true</code> if the associated handler was successfuly retreived and
     *         {@linkplain IEventHandler#canHandleEvent(IEvent) can handle} the given event.
     */
    public synchronized <E extends IEvent<?>> boolean pushEvent(E event) {
        IEventHandler<E> handler = mHandlerManager.getEventHandler(event);

        if (handler == null) {
            return false;
        }

        boolean b = handler.canHandleEvent(event);

        if (b) {
            execute(event, handler);
        }

        return b;
    }

    /**
     * Returns <code>true</code> if there is no pending events
     * 
     * @return <code>true</code> if there is no pending events
     */
    public synchronized boolean isEmpty() {
        return mExecutor.getQueue().isEmpty();
    }

    /**
     * @return the number of pending events
     */
    public synchronized int getPendingEventsCount() {
        return mExecutor.getQueue().size();
    }

    /**
     * @return the pending events
     */
    public synchronized IEvent<?>[] getPendingEvents() {
        BlockingQueue<Runnable> queue = mExecutor.getQueue();
        IEvent<?>[] events = new IEvent<?>[queue.size()];
        int idx = 0;
        for (Runnable r : queue) {
            events[idx++] = ((EventHandlerWorker<?>) r).getEvent();
        }
        Arrays.sort(events, new EventComparator());

        return events;
    }

    /**
     * Initiates an orderly shutdown in which previously submitted events are handled, but no new event will be
     * accepted. Invocation has no additional effect if already shut down.
     * 
     * @see ExecutorService#shutdown()
     */
    public synchronized void shutdown() {
        mExecutor.shutdown();
    }

    /**
     * Attempts to stop all actively executing event handling, halts the processing of waiting events, and returns a
     * list of the event handling that were awaiting execution.
     * 
     * @see ExecutorService#shutdownNow()
     */
    public synchronized List<EventHandlerWorker<?>> shutdownNow() {
        return Utilities.convertToList(mExecutor.shutdownNow());
    }

    /**
     * <code>EventRejectedExecutionHandler</code> is an implementation of {@link RejectedExecutionHandler} that logs an
     * error message.
     * <p>
     * Creation date: 31/08/2010 - 11:05:40
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class EventRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LoggerHelper
                    .getLogger(getClass())
                    .error("EventRejectedExecutionHandler.rejectedExecution: Unable to execute %s in executor %s",
                            r, executor);
        }
    }

    /**
     * <code>EventHandlingThreadFactory</code> is an implementation of {@link ThreadFactory} that creates
     * {@linkplain Thread threads} to be handle events
     * <p>
     * Creation date: 31/08/2010 - 11:48:00
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class EventHandlingThreadFactory implements ThreadFactory {
        private int               mThreadCount = 1;

        /** a thread group for this thread factory **/
        private final ThreadGroup mThreadGroup;

        /**
         * Getter for threadGroup : a thread group for this thread factory
         * 
         * @return the value of threadGroup
         */
        public ThreadGroup getThreadGroup() {
            return mThreadGroup;
        }

        public EventHandlingThreadFactory() {
            mThreadCount = 1;
            mThreadGroup = new ThreadGroup("EventThreads");
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(getThreadGroup(), r, "Event-" + mThreadCount++);
        }
    }
}
