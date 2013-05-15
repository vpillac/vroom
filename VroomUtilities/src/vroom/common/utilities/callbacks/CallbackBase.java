/*
 * 
 */
package vroom.common.utilities.callbacks;

/**
 * <code>CallbackBase<code> is a simple implementation of the @link{ICallback}
 * interface, providing the priority and synchronousity properties
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:41 a.m.
 */
public abstract class CallbackBase<S, T extends ICallbackEventTypes> implements ICallback<S, T> {

    /**
     * The priority of this callback (default value is 0)
     */
    private final int     mPriority;
    /**
     * Synchronous/Asynchronous execution of this callback: <code>true</code> if this callback has to be executed
     * synchronously (in the same thread that called its execution), <code>false</code> if it can be executed
     * asynchronously (in a thread different from the caller) (default is <code>false</code>)
     */
    private final boolean mSynchronous;

    /**
     * Creates a new asynchronous <code>CallbackBase</code> with priority 0.
     */
    public CallbackBase() {
        this(0, false);
    }

    /**
     * Creates a new <code>CallbackBase</code>
     * 
     * @param priority
     *            the priority of this event that will be used to sort callbacks if various of them are associated with
     *            a same event
     * @param synchornous
     *            <code>true</code> if this callback has to be executed in a synchronous way (in the calling thread),
     *            <code>false</code> if it can be executed asynchronously (in a different thread)
     */
    public CallbackBase(int priority, boolean synchornous) {
        this.mSynchronous = synchornous;
        this.mPriority = priority;
    }

    @Override
    public abstract void execute(ICallbackEvent<S, T> event);

    /**
     * @return the priority of this callback
     */
    @Override
    public final int getPriority() {
        return this.mPriority;
    }

    @Override
    public final boolean isExecutedSynchronously() {
        return this.mSynchronous;
    }

    @Override
    public final int compareTo(ICallback<?, ?> o) {
        return o.getPriority() - getPriority();
    }
}// end CallbackBase