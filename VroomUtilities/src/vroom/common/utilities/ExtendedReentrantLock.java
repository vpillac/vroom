/**
 * 
 */
package vroom.common.utilities;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import vroom.common.utilities.logging.LoggerHelper;

/**
 * <code>ExtendedReentrantLock</code> is an extension of {@link ReentrantLock} providing additional monitoring functions.
 * <p>
 * Creation date: Jun 30, 2010 - 3:53:14 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ExtendedReentrantLock extends ReentrantLock {

    private static final long                     serialVersionUID = 1L;

    /** set to <code>true</code> to enable detailed logging */
    public static boolean                         sLoggingEnabled  = false;

    /** set to <code>true</code> to enable extended debugging */
    public static boolean                         sRecordStack     = false;

    private long                                  mLastLockTime    = 0;
    private final LinkedList<StackTraceElement[]> mLocksStack;
    private StackTraceElement[]                   mLastStack;

    /**
     * Getter for <code>lastLockTime</code>
     * 
     * @return the lastLockTime
     */
    public long getLastLockTime() {
        return mLastLockTime;
    }

    public static final LoggerHelper LOGGER = LoggerHelper.getLogger(ReentrantLock.class.getSimpleName());

    /**
     * Getter for the owner thread name
     * 
     * @return
     */
    public String getOwnerName() {
        Thread owner = super.getOwner();
        return owner != null ? owner.getName() : "none";
    }

    /**
     * Creates a new <code>ExtendedReentrantLock</code>
     */
    public ExtendedReentrantLock() {
        mLocksStack = new LinkedList<StackTraceElement[]>();
    }

    /**
     * Creates a new <code>ExtendedReentrantLock</code>
     * 
     * @param fair
     */
    public ExtendedReentrantLock(boolean fair) {
        super(fair);
        mLocksStack = new LinkedList<StackTraceElement[]>();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        super.lockInterruptibly();
        if (sRecordStack) {
            mLastLockTime = System.currentTimeMillis();
            mLastStack = Thread.currentThread().getStackTrace();
            mLocksStack.add(mLastStack);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.locks.ReentrantLock#lock()
     */
    @Override
    public void lock() {
        super.lock();
        if (sRecordStack) {
            mLastLockTime = System.currentTimeMillis();
            mLastStack = Thread.currentThread().getStackTrace();
            mLocksStack.add(mLastStack);
        }
        if (sLoggingEnabled) {
            LOGGER.lowDebug("%s acquired lock %s", Thread.currentThread(), hashCode());
        }
    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.locks.ReentrantLock#tryLock(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        boolean b = true;
        b = super.tryLock(timeout, unit);
        if (b && sRecordStack) {
            mLastLockTime = System.currentTimeMillis();
            mLastStack = Thread.currentThread().getStackTrace();
            mLocksStack.add(mLastStack);
        }
        if (sLoggingEnabled) {
            if (b) {
                LOGGER.lowDebug("%s acquired lock %s", Thread.currentThread(), hashCode());
            } else {
                LOGGER.lowDebug("%s failed to acquired lock %s", Thread.currentThread(), hashCode());
            }
        }
        return b;

    }

    /*
     * (non-Javadoc)
     * @see java.util.concurrent.locks.ReentrantLock#unlock()
     */
    @Override
    public void unlock() {

        if (sRecordStack && mLastStack != null) {
            StackTraceElement[] currentStack = Thread.currentThread().getStackTrace();

            for (int i = 1; i < mLastStack.length; i++) {
                if (i > currentStack.length) {
                    throw new IllegalStateException("Lock and Unlock were not perfomed in the same method");
                }

                StackTraceElement l = mLastStack[i];

                if (!l.getMethodName().contains("lock") && !l.getMethodName().contains("Lock")) {

                    StackTraceElement u = currentStack[i];

                    if (!l.getClassName().equalsIgnoreCase(u.getClassName())
                            || !l.getMethodName().equalsIgnoreCase(u.getMethodName())) {
                        throw new IllegalStateException(
                            "Lock and Unlock were not perfomed in the same method");
                    }
                }
            }

            mLocksStack.pollLast();
            mLastStack = mLocksStack.isEmpty() ? null : mLocksStack.getLast();
        }

        super.unlock();
        if (sRecordStack && getHoldCount() == 0) {
            mLastLockTime = -1;
            mLocksStack.clear();
            mLastStack = null;
        }
        if (sLoggingEnabled) {
            LOGGER.lowDebug("%s released lock %s", Thread.currentThread(), hashCode());
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(mLocksStack.size() * 100);

        int i = 0;
        for (StackTraceElement[] stack : mLocksStack) {
            b.append("\nLock " + (i++) + " ");
            for (int j = 1; j < stack.length; j++) {
                if (j > 1) {
                    b.append("\n    ");
                }
                b.append(String.format("%s.%s (%s %s)", stack[j].getClassName(), stack[j].getMethodName(),
                    stack[j].getFileName(), stack[j].getLineNumber()));
            }
        }

        if (mLocksStack.isEmpty()) {
            b.append("NA");
        }

        return String.format("Lock %s owned by %s (%s holds) for %sms - Stacks: %s", hashCode(), getOwner(),
            getHoldCount(), getLastLockTime() >= 0 ? System.currentTimeMillis() - getLastLockTime() : "NA", b);
    }
}
