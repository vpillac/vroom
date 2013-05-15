/*
 * 
 */
package vroom.common.utilities.events;

import java.util.Date;

/**
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 10-Feb-2010 06:54:18 p.m.
 * @param <S>
 *            the type of object that can generate instances of this event.
 */
public abstract class EventBase<S> implements IEvent<S> {

    private EventQueue<? extends IEvent<S>> mManager = null;

    /**
     * The priority of this event
     */
    private final int                       mPriority;

    /**
     * The object that created this event
     */
    private final S                         mSource;

    /**
     * The time at which this event was created (in ms since 1970)
     */
    private final long                      mTimeStamp;

    /**
     * @param priority
     *            the priority of this event
     */
    public EventBase(int priority, S source) {
        this.mPriority = priority;
        this.mSource = source;
        this.mTimeStamp = System.currentTimeMillis();
    }

    @Override
    public EventQueue<? extends IEvent<S>> getParentManager() {
        return this.mManager;
    }

    /**
     * Priority of this event
     * 
     * @return the priority associated with this event
     */
    @Override
    public int getPriority() {
        return this.mPriority;
    }

    @Override
    public S getSource() {
        return this.mSource;
    }

    @Override
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Preemptivity of the event
     * 
     * @return <code>true</code> if this event is preemptive, <code>false</code> otherwise
     */
    @Override
    public abstract boolean isPreemptive();

    /**
     * Setter for the manager field
     * 
     * @param manager
     *            the parent manager for this event
     * @throws IllegalStateException
     *             if this event was already associated with a manager
     */
    @Override
    public void setManager(EventQueue<? extends IEvent<S>> manager) {
        if (this.mManager != null && this.mManager != manager) {
            throw new IllegalStateException("This event is already associated to a different event manager: "
                    + this.mManager);
        } else {
            this.mManager = manager;
        }
    }

    @Override
    public int compareTo(IEvent<?> o) {
        return getPriority() - o.getPriority();
    }

    @Override
    public String toString() {
        return String.format("%1$s [p:%2$s,src:%3$s,t:%4$tHh%4$tMm%4$tSs]", this.getClass().getSimpleName(),
            this.getPriority(), this.getSource() != null ? this.getSource().getClass().getSimpleName()
                    : "null", new Date(this.getTimeStamp()));
    }

}// end SimpleEvent