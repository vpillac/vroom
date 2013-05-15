/**
 *
 */
package vroom.common.utilities;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <code>BatchThreadPoolExecutorNew</code> is a specialization of {@link ThreadPoolExecutor} designed to maintain a pool
 * of {@link Thread} that will be periodically used to execute a batch of tasks. <br/>
 * It provides a convenience {@link #awaitBatchCompletion()} method that waits until the task queue is empty
 * <p>
 * Creation date: May 19, 2011 - 1:24:46 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class BatchThreadPoolExecutorNew {

    // FIXME this class should not directly inherit from ThreadPoolExecutor, instead it should use one and use a more
    // robust synchronization scheme based on CountDownLatch. The idea would be to have the capability of adding tasks
    // to a temporary queue, then push all the tasks to the executor and decrement the CountDownLatch each time a task
    // is over. This could be achieved with a specialized thread class

    private final ThreadPoolExecutor mExecutor;

    /** A flag that will activate a debug mode in which all tasks will be run sequentially in the parent thread */
    public static boolean            sDebugSequential = false;

    private final CountDownLatch     mCountDown;

    /**
     * Creates a new <code>BatchThreadPoolExecutorNew</code>
     * 
     * @param size
     *            the size of the thread pool
     * @param poolName
     *            a name for the threads of the pool
     */
    public BatchThreadPoolExecutorNew(int size, String poolName) {
        this(size, new NameThreadFactory(poolName));
    }

    /**
     * Creates a new <code>BatchThreadPoolExecutorNew</code>
     * 
     * @param size
     *            the size of the thread pool
     * @param factory
     *            the thread factory that will be used to create new threads
     */
    public BatchThreadPoolExecutorNew(int size, ThreadFactory factory) {
        mExecutor = new ThreadPoolExecutor(sDebugSequential ? 1 : size, sDebugSequential ? 1 : size, 1l,
                TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), factory);
        mCountDown = new CountDownLatch(0);
    }

    // /**
    // * Submit a batch of tasks and wait for their termination.
    // * <p>
    // * The {@link Future} results should then be checked for exception handling (see {@link Future#get()})
    // * </p>
    // *
    // * @param <C>
    // * The type of callable
    // * @param <V>
    // * The result type returned by the {@link Future}'s {@link Future#get() get} method
    // * @param batch
    // * the collection of tasks to be executed
    // * @param waitForCompletion
    // * <code>true</code> if the calling thread should be suspended until all tasks have been executed
    // * @return a mapping between tasks and their result
    // * @throws InterruptedException
    // * @see {@link #submit(Callable)}
    // */
    // public <C extends Callable<V>, V> Map<C, Future<V>> submitBatch(Collection<C> batch, boolean waitForCompletion)
    // throws InterruptedException {
    // Map<C, Future<V>> futures = new HashMap<C, Future<V>>();
    // if (sDebugSequential) {
    // for (C c : batch) {
    // DummyFuture<V> f = new DummyFuture<V>(c);
    // futures.put(c, f);
    // f.run();
    // }
    // } else {
    // for (C c : batch) {
    // futures.put(c, submit(c));
    // }
    // if (waitForCompletion) {
    // for (Future<V> f : futures.values()) {
    // try {
    // f.get();
    // } catch (ExecutionException e) {
    // // Do nothing
    // }
    // }
    // }
    // }
    // return futures;
    // }
    //
    // /**
    // * Submit a batch of tasks and wait for their termination.
    // * <p>
    // * The {@link Future} results should then be checked for exception handling (see {@link Future#get()})
    // * </p>
    // *
    // * @param batch
    // * the collection of tasks to be executed
    // * @param waitForCompletion
    // * <code>true</code> if the calling thread should be suspended until all tasks have been executed
    // * @return a mapping between tasks and their result
    // * @throws InterruptedException
    // * @see {@link #submit(Runnable)}
    // */
    // public <R extends Runnable> Map<R, Future<?>> executeBatch(Collection<R> batch, boolean waitForCompletion)
    // throws InterruptedException {
    // Map<R, Future<?>> futures = new HashMap<R, Future<?>>(batch.size() / 3 * 4);
    // if (sDebugSequential) {
    // for (R r : batch) {
    // DummyFuture<Object> f = new DummyFuture<Object>(r);
    // futures.put(r, f);
    // f.run();
    // }
    // } else {
    // for (R r : batch) {
    // futures.put(r, submit(r));
    // }
    //
    // if (waitForCompletion) {
    // for (Future<?> f : futures.values()) {
    // try {
    // f.get();
    // } catch (ExecutionException e) {
    // // Do nothing
    // }
    // }
    // }
    // }
    //
    // return futures;
    // }

    public void execute(Runnable command) {
        // mLock.lock();
        // mActiveCount.incrementAndGet();
        // mLock.unlock();
        // super.execute(command);
    }

    /**
     * Wait for the completion of the current batch of tasks. <br/>
     * This method assumes that all tasks were added sequentially by the same {@link Thread} that will call this method.
     * <p>
     * It is recommended to use {@link #executeBatch(Collection, boolean)} or {@link #submitBatch(Collection, boolean)}
     * instead
     * </p>
     * 
     * @throws InterruptedException
     */
    public void awaitBatchCompletion() throws InterruptedException {
        mCountDown.await();
    }

    @Override
    protected void finalize() {
        mExecutor.shutdown();
    }

    /**
     * The class <code>WrappedCallable</code> wraps an instance of {@link Callable} and a reference to a
     * {@link CountDownLatch} that will be updated upon task completion
     * <p>
     * Creation date: Apr 23, 2012 - 5:06:41 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     * @param <V>
     */
    protected static class WrappedCallable<V> implements Callable<V> {
        private final CountDownLatch mLatch;
        private final Callable<V>    mTask;

        /**
         * Creates a new <code>WrappedCallable</code>
         * 
         * @param latch
         * @param task
         */
        public WrappedCallable(CountDownLatch latch, Callable<V> task) {
            mLatch = latch;
            mTask = task;
        }

        @Override
        public V call() throws Exception {
            V r = mTask.call();
            mLatch.countDown();
            return r;
        }

    }

    /**
     * <code>NameThreadFactory</code> is a simple implementation of {@link ThreadFactory} with custom thread names
     * <p>
     * Creation date: May 19, 2011 - 2:07:26 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class NameThreadFactory implements ThreadFactory {
        static final AtomicInteger  sPoolNumber   = new AtomicInteger(1);
        private final ThreadGroup   mGroup;
        private final AtomicInteger mThreadNumber = new AtomicInteger(1);
        private final String        mNamePrefix;

        /**
         * Creates a new <code>NameThreadFactory</code>
         * 
         * @param name
         *            the name of the pool
         */
        public NameThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            mGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            mNamePrefix = name + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(getGroup(), r, getNamePrefix() + getThreadNumber().getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }

        /**
         * Getter for <code>group</code>
         * 
         * @return the group
         */
        public ThreadGroup getGroup() {
            return mGroup;
        }

        /**
         * Getter for <code>namePrefix</code>
         * 
         * @return the namePrefix
         */
        public String getNamePrefix() {
            return mNamePrefix;
        }

        /**
         * Getter for <code>threadNumber</code>
         * 
         * @return the threadNumber
         */
        public AtomicInteger getThreadNumber() {
            return mThreadNumber;
        }
    }

    /**
     * <code>DummyFuture</code> is an implementation of {@link Future} for tasks that are executed in a synchronous
     * manner.
     * <p>
     * Creation date: Jun 21, 2011 - 11:49:45 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     * @param <F>
     */
    protected static class DummyFuture<F> implements Future<F>, Runnable {

        private F                  mResult = null;
        private final Runnable     mRun;
        private final Callable<F>  mCall;
        private ExecutionException mException;

        protected DummyFuture(Runnable run) {
            mRun = run;
            mCall = null;
        }

        protected DummyFuture(Callable<F> call) {
            mRun = null;
            mCall = call;
        }

        @Override
        public void run() {
            try {
                if (mCall != null)
                    mResult = mCall.call();
                else
                    mRun.run();
            } catch (Exception e) {
                mException = new ExecutionException(e);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public F get() throws InterruptedException, ExecutionException {
            if (mException != null)
                throw mException;
            return mResult;
        }

        @Override
        public F get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (mException != null)
                throw mException;
            return mResult;
        }
    }
}
