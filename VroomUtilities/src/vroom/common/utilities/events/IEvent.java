/*
 * 
 */
package vroom.common.utilities.events;

import java.util.PriorityQueue;

/**
 * <code>IEvent</code> is the interface for all events that are to be managed in the jEvents framework
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 10-Feb-2010 06:54:34 p.m.
 * @param <S>
 *            the type of object that can generate instances of this event.
 */
public interface IEvent<S> extends Comparable<IEvent<?>> {
    /**
     * Access to the parent manager. It allows an event to send a new event to the same manager.
     * 
     * @return the event manager that managed this event
     */
    public EventQueue<? extends IEvent<S>> getParentManager();

    /**
     * Priority of this event, Please note that events will generally be ordered in their natural order, which means that a priority of 0 will be on
     * top of a {@link PriorityQueue}. Therefore, a lower value for the priority will cause the event to be handled first.
     * 
     * @return the priority of this event.
     */
    public int getPriority();

    /**
     * Access to the object that created this event.
     * 
     * @return the object that created this event
     */
    public S getSource();

    /**
     * Prehemptivity of the event
     * 
     * @return <code>true</code> is this event is preemptive and should be handled immediatly even if lower priority events are currently being
     *         handled
     */
    public boolean isPreemptive();

    /**
     * Setter for the parent manager. This method is invoked by an event manager when it receives an event.
     * 
     * @param manager
     *            the parent manager for this event
     */
    public void setManager(EventQueue<? extends IEvent<S>> manager);

    /**
     * Getter for the time stamp of this event
     * 
     * @return the time at which this event was created (in ms since 1970)
     */
    long getTimeStamp();

}