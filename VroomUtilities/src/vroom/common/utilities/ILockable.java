package vroom.common.utilities;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Creation date: Apr 6, 2010 - 5:34:06 PM<br/>
 * <code>ILockable</code> is an interface for objects that are susceptible to be accessed by various threads
 * concurrently and that require advanced locked features
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @see Lock
 */
public interface ILockable {

    /**
     * A global timeout for locking
     * 
     * @see #TRY_LOCK_TIMOUT_UNIT
     * @see Lock#tryLock(long, TimeUnit)
     */
    public static int      TRY_LOCK_TIMOUT      = 60;

    /**
     * The unit used in the global timeout for locking
     * 
     * @see #TRY_LOCK_TIMOUT_UNIT
     * @see Lock#tryLock(long, TimeUnit)
     */
    public static TimeUnit TRY_LOCK_TIMOUT_UNIT = TimeUnit.SECONDS;

    /**
     * @return <code>true</code> if the lock on this object is owned by the current thread
     */
    public boolean isLockOwnedByCurrentThread();

    /**
     * Release the lock on this object
     * <p/>
     * Implementations should ensure that this method behaves as the {@link java.util.concurrent.locks.Lock#unlock()}
     * method.
     * 
     * @see Lock#unlock()
     */
    public void releaseLock();

    /**
     * Acquire the lock on this object.
     * <p/>
     * Implementations should ensure that this method behaves as the {@link java.util.concurrent.locks.Lock#lock()}
     * method.
     * 
     * @see Lock#lock()
     */
    public void acquireLock();

    /**
     * Try to acquire the lock on this object
     * 
     * @param timeout
     *            the maximum time (in ms) that the current thread is willing to wait for the lock
     * @return <code>true</code> if the lock was acquired
     */
    public boolean tryLock(long timeout);

    /**
     * Getter for the underlying {@link Lock} object
     * 
     * @return the instance of {@link Lock} used by this instance to manage locks
     * @see ILockable#acquireLock()
     * @see ILockable#releaseLock()
     * @see ILockable#isLockOwnedByCurrentThread()
     */
    public Lock getLockInstance();

}
