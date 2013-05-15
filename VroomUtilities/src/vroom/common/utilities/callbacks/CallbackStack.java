package vroom.common.utilities.callbacks;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import vroom.common.utilities.logging.Logging;

/**
 * Creation date: Mar 9, 2010 - 11:43:48 AM<br/>
 * <code>CallbackStack</code> provides a stack to store callbacks calls that will be executed asynchronously.
 * <p>
 * Callbacks are executed in the order in which they appear in the stack
 * </p>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CallbackStack implements Runnable {

    private static boolean sGlobalRunning = true;

    /**
     * This method will cause all instances of {@link CallbackStack} to finish their execution.
     */
    public static void stopAllThreads() {
        sGlobalRunning = false;
    }

    private final PriorityBlockingQueue<CallbackCall<?, ?>> mPendingCallbacks;

    private boolean                                         mRunning;

    /**
     * Creates a new <code>CallbackExecuter</code>
     */
    public CallbackStack() {
        super();

        mRunning = true;
        mPendingCallbacks = new PriorityBlockingQueue<CallbackCall<?, ?>>(100);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void run() {
        CallbackCall c;
        boolean invariant = mRunning && sGlobalRunning;
        while (invariant) {
            try {
                c = mPendingCallbacks.poll(500, TimeUnit.MILLISECONDS);
                if (c != null) {
                    c.callback.execute(c.event);
                }
            } catch (InterruptedException e) {
                Logging.getBaseLogger().warn("Execption caught in method CallbackStack.run", e);
            }

            c = null;

            invariant = mRunning && sGlobalRunning || !mPendingCallbacks.isEmpty();
        }
    }

    /**
     * Put the call to <code>callback</code> with the given <code>event</code> and <code>params</code> in the stack.
     * 
     * @param <S>
     * @param <T>
     * @param callback
     *            the callback to be added to the stack
     * @param event
     *            the event that triggered the callback
     * @param params
     *            optional parameters
     */
    public synchronized <S, T extends ICallbackEventTypes> void put(ICallback<S, T> callback,
            ICallbackEvent<S, T> event) {
        if (!mRunning || !sGlobalRunning) {
            Logging.getBaseLogger()
                    .warn("This callback stack has been stopped, ignoring callback %s [event %s](running=%s, globalRunning=%s)",
                            callback, event, mRunning, sGlobalRunning);
            throw new IllegalStateException(
                    "Cannot add new callbacks if this stack has been stopped");
        } else {
            CallbackCall<S, T> call = new CallbackCall<S, T>(callback, event);
            if (!mPendingCallbacks.offer(call)) {
                Logging.getBaseLogger().warn("Callback %s could not be added to the stack", call);
            }
        }
    }

    /**
     * Stops the parent thread by causing the termination of the {@link #run()} method.
     */
    public void stop() {
        mRunning = false;
    }

    /**
     * <code>CallbackCall</code> is a container class used to store information to asynchronous executing of a callback.
     */
    private static class CallbackCall<S, T extends ICallbackEventTypes> implements
            Comparable<CallbackCall<S, T>> {
        private final ICallback<S, T>      callback;
        private final ICallbackEvent<S, T> event;

        /**
         * Creates a new <code>CallbackCall</code>
         * 
         * @param params
         * @param callback
         * @param event
         */
        public CallbackCall(ICallback<S, T> callback, ICallbackEvent<S, T> event) {
            super();
            this.callback = callback;
            this.event = event;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("event:%s, callback:%s", event, callback);
        }

        @Override
        public int compareTo(CallbackCall<S, T> o) {
            if (o.callback.getPriority() != this.callback.getPriority()) {
                return (o.callback.getPriority() - this.callback.getPriority()) * 60000;
            } else {
                return (int) (this.event.getTimeStamp() - o.event.getTimeStamp());
            }
        }
    }

}
