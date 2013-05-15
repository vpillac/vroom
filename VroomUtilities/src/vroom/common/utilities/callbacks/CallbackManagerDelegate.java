/*
 * 
 */
package vroom.common.utilities.callbacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>CallBackManagerDelegate</code> is a delegate class that is used to register and notify callbacks in a procedure<br/>
 * Beware that by default the callbacks will be associated with the <b>instance</b> of the event object
 * {@link #registerCallback(ICallback, ICallbackEventTypes)}. It is therefore recommended to use enumerations as event
 * types.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:40 a.m.
 * @param <S>
 *            the type of source that will generate events
 * @param <T>
 *            the type of {@link ICallbackEventTypes} that will be used to associate callbacks to events
 */
public class CallbackManagerDelegate<S, T extends ICallbackEventTypes> {

    private Thread                        mCallbackThread;
    private final String                  mThreadLabel;
    private final CallbackStack           mCallbackStack;

    private final CallbackComparator      mComparator = new CallbackComparator();

    /**
     * A map containing the callbacks associated with events in priority order (callbacks with lower values for
     * {@link ICallback#getPriority()} are first)
     */
    private Map<T, List<ICallback<S, T>>> mCallbackMapping;

    /**
     * Creates a new generic <code>CallbackManagerDelegate</code> able to manage any type of event
     */
    public CallbackManagerDelegate(String label) {
        this(new HashMap<T, List<ICallback<S, T>>>(), label);
    }

    /**
     * Creates a new <code>CallbackManagerDelegate</code> optimized for the events of the given enumeration
     * 
     * @param eventsEnum
     *            an enumeration of the events that will be managed by this instance
     * @param label
     *            a label for the callback thread
     */
    @SuppressWarnings("unchecked")
    public <K extends Enum<K>> CallbackManagerDelegate(Class<K> eventsEnum, String label) {
        if (!ICallbackEventTypes.class.isAssignableFrom(eventsEnum)) {
            throw new IllegalArgumentException(
                    "Argument eventsEnum must implement ICallbackEvent<S,T>");
        } else {
            this.mCallbackMapping = (Map<T, List<ICallback<S, T>>>) new EnumMap<K, List<ICallback<S, T>>>(
                    eventsEnum);
        }

        this.mCallbackStack = new CallbackStack();
        mThreadLabel = label;
    }

    /**
     * Creates a new <code>CallbackManagerDelegate</code> based on the given <code>map</code>
     * 
     * @param map
     *            the map that will be used to associate callbacks to events
     */
    public CallbackManagerDelegate(Map<T, List<ICallback<S, T>>> map, String label) {
        this.mCallbackMapping = map;

        this.mCallbackStack = new CallbackStack();
        mThreadLabel = label;
    }

    private void setupAndStartThread() {
        this.mCallbackThread = new Thread(this.mCallbackStack, mThreadLabel + "-cb");
        this.mCallbackThread.setDaemon(true);
        this.mCallbackThread.start();
    }

    /**
     * Call @link{ICallback<S,T>#execute(ICallbackEvent<S,T>,Object)} on the callbacks associated with the given
     * <code>event</code>, passing the optional <code>parameter</code>
     * 
     * @param event
     *            the event that has occurred and for which the associated callbacks will be executed
     */
    public void callbacks(ICallbackEvent<S, T> event) {
        if (this.mCallbackMapping.containsKey(event.getType())) {
            List<ICallback<S, T>> callbacks = this.mCallbackMapping.get(event.getType());

            if (callbacks != null) {
                for (ICallback<S, T> cb : callbacks) {
                    if (cb.isExecutedSynchronously()) {
                        cb.execute(event);
                    } else {
                        this.mCallbackStack.put(cb, event);
                    }
                }
            }
        }
    }

    /**
     * Register the given <code>callback</code> to the <code>eventType</code><br/>
     * 
     * @param callback
     *            the callback that will be associated with <code>event</code>
     * @param eventType
     *            the event to which the given <code>callback</code> will be associated
     */
    public void registerCallback(ICallback<S, T> callback, T eventType) {
        List<ICallback<S, T>> callbacks;

        // Lazy start of the callback thread
        if (mCallbackThread == null && !callback.isExecutedSynchronously())
            setupAndStartThread();

        if (this.mCallbackMapping.containsKey(eventType)) {
            callbacks = this.mCallbackMapping.get(eventType);
        } else {
            // create and add the callback list
            callbacks = new ArrayList<ICallback<S, T>>();
            this.mCallbackMapping.put(eventType, callbacks);
        }

        // add the callback to the list
        callbacks.add(callback);
        // Sort the callback list
        Collections.sort(callbacks, mComparator);
    }

    /**
     * Unregister the given <code>callback</code> from the <code>eventType</code> <br/>
     * If <code>callback</code> was registered more than once then only the first reference will be removed
     * 
     * @param callback
     *            the callback that will no longer be associated with <code>event</code>
     * @param eventType
     *            the considered event
     */
    public void unregisterCallback(ICallback<S, T> callback, T eventType) {
        List<ICallback<S, T>> callbacks;

        if (this.mCallbackMapping.containsKey(eventType)) {
            callbacks = this.mCallbackMapping.get(eventType);
            callbacks.remove(callback);
        }
    }

    /**
     * A {@link Comparator} for {@link ICallback} instances used to order the list of callbacks associated with an event
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
     *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href= "http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     */
    private class CallbackComparator implements Comparator<ICallback<S, T>> {

        @Override
        public int compare(ICallback<S, T> c1, ICallback<S, T> c2) {
            if (c1 == null) {
                return 1;
            }
            if (c2 == null) {
                return -1;
            }
            return c2.getPriority() - c1.getPriority();
        }

    }

    /**
     * Will cause the suspension of the callback thread
     */
    public void stop() {
        this.mCallbackStack.stop();
    }

    @Override
    public void finalize() throws Throwable {
        stop();
        super.finalize();
    }

}// end CallBackManagerDelegate