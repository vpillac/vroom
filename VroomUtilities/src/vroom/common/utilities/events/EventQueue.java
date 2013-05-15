/*
 * 
 */
package vroom.common.utilities.events;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Observable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import vroom.common.utilities.ExtendedReentrantLock;

/**
 * <code>EventQueue</code> is a generic utility class for the management of a
 * list of events, it uses a priority queue to store the pending events.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @see PriorityBlockingQueue
 * @param <E>
 *            the type of events managed by this instance
 */
public class EventQueue<E extends IEvent<?>> extends Observable {

	private final ExtendedReentrantLock mLock = new ExtendedReentrantLock(true);
	private final Condition mEmptyCondition = mLock.newCondition();

	private final Comparator<IEvent<?>> mComparator;

	/** The name of the <code>nextEventPreemptive</code> property */
	public static final String NEXT_EVENT_Preemptive_PROP = "NextEventPreemptive";

	/**
	 * The list of events
	 */
	private final PriorityBlockingQueue<E> mEventsQueue;

	/**
	 * Creates a new event manager that will order events according to the
	 * {@linkplain Comparable natural ordering}
	 * 
	 * @see #EventQueue(Comparator)
	 */
	public EventQueue() {
		this(null);
	}

	/**
	 * Creates a new <code>EventQueue</code> that will order events according to
	 * the specified comparator
	 * 
	 * @param eventComparator
	 *            the comparator that will be used to order this priority queue.
	 *            If null, the natural ordering of the elements will be used.
	 * @see PriorityBlockingQueue#PriorityBlockingQueue(int, Comparator)
	 */
	public EventQueue(Comparator<IEvent<?>> eventComparator) {
		mComparator = eventComparator;
		mEventsQueue = new PriorityBlockingQueue<E>(20, mComparator);
	}

	/**
	 * Check if the next event of the queue is preemptive
	 * 
	 * @return <code>true</code> if the next event is preemptive
	 */
	public synchronized boolean isNextEventPreemptive() {
		return mEventsQueue != null && mEventsQueue.peek() != null
				&& mEventsQueue.peek().isPreemptive();
	}

	/**
	 * Causes the calling thread to wait until a new event becomes available or
	 * the timeout expires (does nothing if there is an available event)
	 * 
	 * @param timeout
	 *            the time in milliseconds to wait
	 * @return {@code false} if the waiting time detectably elapsed before
	 *         return from the method, else {@code true}
	 * @throws InterruptedException
	 * @see Condition#await(long, TimeUnit)
	 */
	public boolean awaitForNewEvent(long timeout) throws InterruptedException {
		boolean r = true;
		if (isEmpty()) {
			this.mLock.lockInterruptibly();
			r = mEmptyCondition.await(timeout, TimeUnit.MILLISECONDS);
			this.mLock.unlock();
		}

		return r;
	}

	/**
	 * Retrieves and removes the next event, waiting if necessary until an event
	 * becomes available.
	 * 
	 * @return the next event
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 * @see PriorityBlockingQueue#take()
	 * @see EventQueue#pollNextEvent()
	 */
	public synchronized E takeNextEvent() throws InterruptedException {
		return this.mEventsQueue.take();
	}

	/**
	 * Retrieves and removes the next event
	 * 
	 * @return the next event
	 * @see PriorityBlockingQueue#poll()
	 * @see EventQueue#takeNextEvent()
	 */
	public synchronized E pollNextEvent() throws InterruptedException {
		return this.mEventsQueue.poll();
	}

	/**
	 * Remove all the pending events
	 */
	public synchronized void clear() {
		mEventsQueue.clear();
	}

	/**
	 * @return the number of pending events
	 */
	public synchronized int getPendingEventsCount() {
		return this.mEventsQueue.size();
	}

	/**
	 * @return the pending events
	 */
	public synchronized IEvent<?>[] getPendingEvents() {
		IEvent<?>[] e = this.mEventsQueue
				.toArray(new IEvent<?>[getPendingEventsCount()]);
		Arrays.sort(e, mComparator);
		return e;
	}

	/**
	 * Add an event to the current pending queue
	 * 
	 * @param event
	 *            the event to be added to the queue
	 * @return <code>true</code> if <code>event</code> was successfully added to
	 *         the queue, <code>false</code> otherwise
	 * @throws InterruptedException
	 * @see PriorityBlockingQueue#offer(Object)
	 */
	public synchronized boolean pushEvent(E event) throws InterruptedException {
		boolean p = isNextEventPreemptive();
		boolean b = this.mEventsQueue.offer(event);
		if (b) {
			this.mLock.lockInterruptibly();

			// The preemptive property has changed
			if (p != isNextEventPreemptive()) {
				// Notify observers
				notifyObservers(NEXT_EVENT_Preemptive_PROP);
			}

			mEmptyCondition.signalAll();
			this.mLock.unlock();
		}
		return b;
	}

	/**
	 * State of the event queue
	 * 
	 * @return <code>true</code> if there is no pending events,
	 *         <code>false</code> otherwise
	 */
	public synchronized boolean isEmpty() {
		return this.mEventsQueue.isEmpty();
	}

	/**
	 * Checks if an event of the specified class is present in the queue
	 * 
	 * @param eventClass
	 *            the class of event to be searched
	 * @return <code>true</code> if the current queue contains at list one event
	 *         of class <code>eventClass</code>, <code>false</code> otherwise
	 */
	public synchronized boolean contains(Class<? extends E> eventClass) {
		if (isEmpty()) {
			return false;
		}
		for (IEvent<?> e : getPendingEvents()) {
			if (e != null && e.getClass() == eventClass) {
				return true;
			}
		}
		return false;
	}
}