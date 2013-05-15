package vroom.optimization.online.jmsa.utils;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.ILockable;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.MSAEvent;

/**
 * The class <code>MSASimulator</code> is used to simulate a dynamic optimization problem. It uses an event queue and
 * sends scheduled events to a MSA procedure, and can be used to store the current state of all resources.
 * <p>
 * Creation date: Feb 9, 2012 - 5:07:39 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MSASimulator implements Runnable, ILockable {

    /** The wall time tolerance ({@value} s) */
    public static final double WALL_TIME_TOLERANCE = 1d;

    /**
     * The enumeration <code>ResourceState</code> describes the different states in which a resource can be.
     * <p>
     * Creation date: Mar 16, 2012 - 1:06:09 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static enum ResourceStates {
        /** State of a resource that has not started yet */
        NOT_STARTED,
        /** State of a resource that has been stopped */
        STOPPED,
        /** State of a resource that is waiting before starting servicing its next assigned request */
        WAITING,
        /** State of a resource that has currently no request assigned */
        IDLE,
        /** State of a resource that is currently servicing a request */
        SERVICING,
        /** State of a resource that is currently busy */
        BUSY
    };

    private final MSABase<?, ?>                         mMSA;

    private final PriorityBlockingQueue<ScheduledEvent> mFutureEvents;
    private final LinkedList<ScheduledEvent>            mExecutedEvents;

    private final Stopwatch                             mWallTimer;
    private final double                                mSpeed;

    private boolean                                     mRunning;
    private boolean                                     mPaused;

    private final Map<Integer, ResourceState>           mResourceStates;

    /**
     * Getter for <code>MSA</code> to which this simulator is attached
     * 
     * @return the MSA
     */
    public MSABase<?, ?> getMSA() {
        return mMSA;
    }

    /**
     * Returns {@code true} if this simulator is currently running, {@code false} otherwise
     * 
     * @return {@code true} if this simulator is currently running, {@code false} otherwise
     */
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Creates a new <code>MSASimulator</code>
     * 
     * @param msa
     *            the msa procedure used in this simulation
     * @param expectedEventCount
     *            the expected number of events
     * @param speed
     *            the speed of the simulation ({@code  simTime=clockTime*speed} )
     */
    public MSASimulator(MSABase<?, ?> msa, int expectedEventCount, double speed) {
        mMSA = msa;

        mSpeed = speed;
        mWallTimer = new Stopwatch();
        mRunning = false;

        mFutureEvents = new PriorityBlockingQueue<ScheduledEvent>(expectedEventCount);
        mExecutedEvents = new LinkedList<MSASimulator.ScheduledEvent>();

        mResourceStates = new HashMap<>();

        mLock = new ExtendedReentrantLock();
    }

    /**
     * Returns the last event that was pushed to the MSA
     * 
     * @return the last event that was pushed to the MSA
     */
    public ScheduledEvent getLastEvent() {
        return mExecutedEvents.isEmpty() ? null : mExecutedEvents.getLast();
    }

    /**
     * Returns the last but one (penultimate) event that was pushed to the MSA
     * 
     * @return the last but one event that was pushed to the MSA
     */
    public ScheduledEvent getPenultimateEvent() {
        Iterator<ScheduledEvent> it = mExecutedEvents.iterator();
        if (!it.hasNext())
            return null;
        it.next();
        return it.hasNext() ? it.next() : null;
    }

    @Override
    public void run() {
        start();
        while (mRunning) {
            ScheduledEvent e = null;
            try {
                e = pollNextEvent();
                acquireLock();
                if (e != null) {
                    pause();
                    // We check that the event is AFTER the last event
                    // We ensure that the simulation time is correct
                    adjustSimulationTime(e.time());
                    MSALogging.getSimulationLogger().info(
                            "MSASimulator.run: Current time:%.1f - Raising event %s",
                            simulationTime(), e);
                    pushEventToMSA(e);
                    mExecutedEvents.add(e);
                    releaseLock();
                }
            } catch (Exception e1) {
                MSALogging.getSimulationLogger().exception("MSASimulator.run", e1);
                getMSA().stop();
                stop();
            }
        }
        stop();
    }

    /**
     * Run this simulator in a new thread
     */
    public void runInNewThread() {
        Thread t = new Thread(this, "msa-sim");
        t.start();
    }

    private void start() {
        if (isRunning())
            throw new IllegalStateException("Already started");
        mWallTimer.start();
        mWallTimer.pause();
        mRunning = true;
        mPaused = true;
        MSALogging.getSimulationLogger().info(
                "MSASimulator.run: Current time:%.1f - Simulator started (paused)",
                simulationTime());
    }

    /**
     * Stop the simulation
     */
    public void stop() {
        if (isRunning()) {
            mRunning = false;
            mWallTimer.stop();
            MSALogging.getSimulationLogger().info(
                    "MSASimulator.run: Current time:%.1f - Simulator stopped", simulationTime());
        }
    }

    /**
     * Pause the simulation
     */
    public void pause() {
        checkLock();
        if (isRunning() && !mPaused) {
            mWallTimer.pause();
            mPaused = true;
            MSALogging.getSimulationLogger().info(
                    "MSASimulator.run: Current time:%.1f - Simulator paused", simulationTime());
        }
    }

    /**
     * Resume the simulation
     */
    public void resume() {
        checkLock();
        if (isRunning() && mPaused) {
            mWallTimer.resume();
            mPaused = false;
            MSALogging.getSimulationLogger().info(
                    "MSASimulator.run: Current time:%.1f - Simulator resumed", simulationTime());
        }
    }

    /**
     * Sets the wall timer to adjust it to a given simulation time
     * 
     * @param time
     *            the simulation time to be set
     */
    public void adjustSimulationTime(double time) {
        checkLock();
        ScheduledEvent le = getPenultimateEvent();
        if (le != null && time < le.time() - simTimeTolerance()) {
            throw new IllegalStateException(
                    String.format(
                            "MSASimulator.adjustSimulationTime: the given time is before the last event (time:%.3f last:%.3f) - Stopping the MSA",
                            time, getLastEvent()));
        } else {
            mWallTimer.setAccumulatedTime((long) (time / getSpeed() * Stopwatch.NS_IN_S));
        }
    }

    /**
     * Schedule an event in the simulation
     * 
     * @param e
     *            the event to be schedule
     * @param time
     *            the time at which the event will be scheduled
     * @throws IllegalStateException
     *             if {@code  time} is in the past
     */
    public synchronized void scheduleEvent(MSAEvent e) {
        if (e.getSimulationTimeStamp() < simulationTime())
            throw new IllegalStateException(String.format(
                    "The specified time (%.1f) in in the past (current is:%.1f)",
                    e.getSimulationTimeStamp(), simulationTime()));

        mFutureEvents.offer(new ScheduledEvent(e));
        MSALogging.getSimulationLogger().info(
                "MSASimulator.schedule event: scheduled %s at %.2f [%s] (%s)",
                e.getClass().getSimpleName(),
                e.getSimulationTimeStamp(),
                Utilities.Time.millisecondsToString(
                        ((long) (e.getSimulationTimeStamp() * 1000 / getSpeed())), 4, true, false),
                e);
    }

    /**
     * Remove some events from the queue
     * 
     * @param cleaner
     *            the {@link IEventQueueCleaner} that will be used to determine which events should be removed
     */
    public synchronized void cleanQueue(IEventQueueCleaner cleaner) {
        if (!mPaused)
            throw new IllegalStateException("The simulator should be paused");
        Iterator<ScheduledEvent> it = mFutureEvents.iterator();
        while (it.hasNext()) {
            ScheduledEvent e = it.next();
            if (cleaner.remove(e)) {
                it.remove();
            }
        }
    }

    /**
     * Wait until the simulation time has reached the next event
     * 
     * @return the next event
     * @throws InterruptedException
     */
    private ScheduledEvent pollNextEvent() throws InterruptedException {
        while (mRunning
                && (mFutureEvents.isEmpty() || mFutureEvents.peek().time() > simulationTime())) {
            Thread.sleep(50);
        }

        if (!mRunning)
            return null;
        else
            return mFutureEvents.poll();

    }

    /**
     * Push an event to the MSA
     * 
     * @param e
     *            the event to be pushed
     */
    private void pushEventToMSA(ScheduledEvent e) {
        mMSA.getEventFactory().raiseEvent(e.getMSAEvent());
    }

    /**
     * Returns the speed of the simulation
     * <p>
     * Simulation time is defined as follows: {@code  simTime=wallTimeSeconds * speed}
     * 
     * @return the speed of the simulation
     */
    public double getSpeed() {
        return mSpeed;
    }

    /**
     * Converts a time (in seconds) from wall time to simulation time
     * 
     * @param time
     *            the wall time
     * @return the corresponding simulation time
     */
    public double wallToSimTime(double time) {
        return time * mSpeed;
    }

    /**
     * Converts a time (in seconds) from simulation time to wall time
     * 
     * @param time
     *            the simulation time
     * @return the corresponding wall time
     */
    public double simToWallTime(double time) {
        return time / mSpeed;
    }

    /**
     * Returns the tolerance in simulation time: two events scheduled with a difference of times lower than this value
     * will be considered as simultaneous.
     * 
     * @return the tolerance in simulation time
     * @see #WALL_TIME_TOLERANCE
     */
    public double simTimeTolerance() {
        return wallToSimTime(WALL_TIME_TOLERANCE);
    }

    /**
     * Returns the current time in the simulation
     * 
     * @return the current time in the simulation
     */
    public double simulationTime() {
        return wallToSimTime(wallTime());
    }

    /**
     * Returns the current time on the wall clock (in seconds)
     * 
     * @return the current time on the wall clock
     */
    public double wallTime() {
        return mWallTimer.readTimeS();
    }

    /**
     * Get the current state of the resource with id {@code  resourceId}
     * 
     * @param resourceId
     *            the id of the considered resource
     * @return the state associated with the specified resource
     */
    public ResourceState getState(int resourceId) {
        ResourceState state = mResourceStates.get(resourceId);
        if (state == null) {
            state = new ResourceState(resourceId);
            mResourceStates.put(resourceId, state);
        }
        return state;
    }

    @Override
    public String toString() {
        return String.format("Wall time: %.3f, Sim time: %.3f, %s Pending events, Resources: %s",
                wallTime(), simulationTime(), mFutureEvents.size(), mResourceStates.toString());
    }

    /**
     * The class <code>ScheduledEvent</code> contains a {@link MSAEvent} and a time at which the event should be sent to
     * the MSA procedure
     * <p>
     * Creation date: Feb 9, 2012 - 5:03:32 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public class ScheduledEvent implements Comparable<ScheduledEvent> {
        private final MSAEvent mMSAEvent;

        /**
         * Return the time at which this event is scheduled
         * 
         * @return the time at which this event is scheduled
         * @see MSAEvent#getSimulationTimeStamp()
         */
        public double time() {
            return mMSAEvent.getSimulationTimeStamp();
        }

        public MSAEvent getMSAEvent() {
            return mMSAEvent;
        }

        private ScheduledEvent(MSAEvent event) {
            mMSAEvent = event;
        }

        @Override
        public int compareTo(ScheduledEvent o) {
            return Double.compare(time(), o.time());
        }

        @Override
        public String toString() {
            return String.format("%.3f:%s", time(), getMSAEvent());
        }
    }

    /**
     * The class <code>ResourceState</code> describe the current state of a resource
     * <p>
     * Creation date: Mar 16, 2012 - 1:16:18 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public class ResourceState extends Observable {
        private final int                        mResourceId;

        private ResourceStates                   mState;

        private final LinkedList<IActualRequest> mServedRequests;
        private final List<IActualRequest>       mServedRequestsView;
        private final LinkedList<IActualRequest> mAssignedRequests;
        private final List<IActualRequest>       mAssignedRequestsView;

        /**
         * Creates a new <code>ResourceState</code>
         * 
         * @param resourceId
         *            the id of the resource that will be associated to this state
         */
        public ResourceState(int resourceId) {
            mResourceId = resourceId;
            mState = ResourceStates.NOT_STARTED;
            mServedRequests = new LinkedList<>();
            mServedRequestsView = Collections.unmodifiableList(mServedRequests);
            mAssignedRequests = new LinkedList<>();
            mAssignedRequestsView = Collections.unmodifiableList(mAssignedRequests);
        }

        /**
         * Returns the id of the resource which state is describe in this instance
         * 
         * @return the id of the resource
         */
        public int getResourceId() {
            return mResourceId;
        }

        /**
         * Returns the current state of the associated resource
         * 
         * @return the current state of the associated resource
         */
        public ResourceStates getState() {
            return mState;
        }

        /**
         * Sets the current state of the associated resource
         * 
         * @param state
         *            the current state of the associated resource
         */
        public void setState(ResourceStates state) {
            mState = state;
            setChanged();
            notifyObservers(state);
        }

        /**
         * Returns a view of the list of requests served by the associated resource
         * 
         * @return a view of the list of requests served by the associated resource
         */
        public List<IActualRequest> getServedRequests() {
            return mServedRequestsView;
        }

        /**
         * Add a request to the list of requests served by this resource
         * 
         * @param r
         *            the served request
         */
        public void addServedRequest(IActualRequest r) {
            mServedRequests.add(r);
            setChanged();
            notifyObservers(r);
        }

        /**
         * Returns a view of the list of requests assigned to associated resource
         * 
         * @return a view of the list of requests assigned to associated resource
         */
        public List<IActualRequest> getAssignedRequests() {
            return mAssignedRequestsView;
        }

        /**
         * Add a request to the list of requests assigned to this resource
         * 
         * @param req
         *            the request
         */
        public void addAssignedRequest(IActualRequest req) {
            mAssignedRequests.add(req);
            setChanged();
            notifyObservers(req);
        }

        /**
         * Removes a request from the list of requests assigned to this resource
         * 
         * @param req
         *            the request
         * @return {@code true} if the list contained {@code  req}
         */
        public boolean removeAssignedRequest(IActualRequest req) {
            boolean b = mAssignedRequests.remove(req);
            setChanged();
            notifyObservers(req);
            return b;
        }

        @Override
        public String toString() {
            return String.format("%s:%s(served:%s assigned:%s)", getResourceId(), getState(),
                    Utilities.toShortString(getServedRequests()),
                    Utilities.toShortString(getAssignedRequests()));
        }
    }

    /**
     * The interface <code>IEventQueueCleaner</code> defines the behavior of classes that are used to remove some events
     * from the event queue.
     * <p>
     * Creation date: Apr 25, 2012 - 5:17:56 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static interface IEventQueueCleaner {
        /**
         * Check if an event should be removed
         * 
         * @param event
         *            the event to be checked
         * @return {@code true} if {@code  event} should be removed from the queue, {@code false} otherwise
         */
        public boolean remove(ScheduledEvent event);
    }

    // ------------------------------------
    // ILockable interface implementation
    // ------------------------------------
    /** A lock to be used by this instance */
    private final ExtendedReentrantLock mLock;
    private boolean                     mSelfLock = false;

    @Override
    public boolean tryLock(long timeout) {
        try {
            return getLockInstance().tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void acquireLock() {
        try {
            if (!getLockInstance().tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
                throw new IllegalStateException(
                        String.format(
                                "Unable to acquire lock on this instance of %s (%s) after %s %s, owner: %s",
                                this.getClass().getSimpleName(), hashCode(), TRY_LOCK_TIMOUT,
                                TRY_LOCK_TIMOUT_UNIT, getLockInstance().getOwnerName()));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format(
                    "Unable to acquire lock on this instance of %s (%s)", this.getClass()
                            .getSimpleName(), hashCode()), e);
        }
        ;
    }

    @Override
    public void releaseLock() {
        if (mLock.isLocked()) {
            this.mLock.unlock();
        }
    }

    public void internalReleaseLock() {
        if (mSelfLock) {
            releaseLock();
            mSelfLock = false;
        }
    }

    @Override
    public boolean isLockOwnedByCurrentThread() {
        return this.mLock.isHeldByCurrentThread();
    }

    @Override
    public ExtendedReentrantLock getLockInstance() {
        return this.mLock;
    }

    /**
     * Check the lock state of this object
     * 
     * @return <code>true</code> if there was no previous lock and lock was acquired, <code>false</code> if the lock was
     *         already owned by the current thread
     * @throws ConcurrentModificationException
     */
    public boolean checkLock() throws ConcurrentModificationException {
        if (!isLockOwnedByCurrentThread() && mLock.isLocked()) {
            throw new ConcurrentModificationException(
                    String.format(
                            "The current thread (%s) does not have the lock on this instance of %s, owner: %s",
                            Thread.currentThread(), this.getClass().getSimpleName(),
                            getLockInstance().getOwnerName()));
        } else if (!mLock.isLocked()) {
            acquireLock();
            mSelfLock = true;
            return true;
        } else {
            mSelfLock = false;
            return false;
        }
    }

    // ------------------------------------

    public static class DuplicateEventCleaner<T extends MSAEvent> implements IEventQueueCleaner {

        private final Class<T> mClass;

        private final double   mTolerance;

        private ScheduledEvent mLastEvent;

        /**
         * Creates a new <code>DuplicateEventCleaner</code>
         * 
         * @param clazz
         * @param tolerance
         *            a tolerance in simulation time units under which two events are considered to happen at the same
         *            time
         */
        public DuplicateEventCleaner(Class<T> clazz, double tolerance) {
            super();
            this.mClass = clazz;
            this.mTolerance = tolerance;
        }

        @Override
        public boolean remove(ScheduledEvent event) {
            boolean remove = false;
            if (event.getMSAEvent() != null
                    && mClass.isAssignableFrom(event.getMSAEvent().getClass())) {
                if (mLastEvent != null) {
                    if (Math.abs(mLastEvent.time() - event.time()) < mTolerance
                            && isDuplicate(mLastEvent, event)) {
                        remove = true;
                    }
                }
                mLastEvent = event;
            }
            return remove;
        }

        /**
         * Return {@code true} if {@code  event1} and {@code  event2} can be considered identical
         * 
         * @param event1
         * @param event2
         * @return {@code true} if {@code  event1} and {@code  event2} can be considered identical
         */
        protected boolean isDuplicate(ScheduledEvent event1, ScheduledEvent event2) {
            return true;
        }

    }
}