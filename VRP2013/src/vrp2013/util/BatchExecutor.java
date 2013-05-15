/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */
/**
 * 
 */
package vrp2013.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import vroom.common.utilities.BatchThreadPoolExecutor;

/**
 * The class <code>BatchThreadPoolExecutor</code> is a wrapper around a {@link BatchThreadPoolExecutor} used to simplify
 * the VRP 2013 examples code. In particular, it handles the logging of exceptions.
 * <p>
 * Creation date: 13/05/2013 - 2:43:01 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class BatchExecutor {

    private final BatchThreadPoolExecutor mExecutor;

    /**
     * Creates a new <code>BatchExecutor</code>
     * 
     * @param size
     *            the number of threads to use
     * @param poolName
     *            a name for the thread pool
     * @author vpillac
     */
    public BatchExecutor(int size, String poolName) {
        mExecutor = new BatchThreadPoolExecutor(size, poolName);
    }

    /**
     * Submit a batch of tasks and wait for completion
     * 
     * @param batch
     * @return a mapping between tasks and results
     * @author vpillac
     */
    public <C extends Callable<V>, V> Map<C, Result<V>> submitBatchAndWait(Collection<C> batch) {
        Map<C, Future<V>> results = null;
        try {
            results = mExecutor.submitBatch(batch, true);
        } catch (InterruptedException e) {
            VRPLogging.getOptLogger().exception("BatchExecutor.submitBatchAndWait", e);
        }
        HashMap<C, Result<V>> safeResults = new HashMap<>();

        if (results != null)
            for (Entry<C, Future<V>> r : results.entrySet()) {
                safeResults.put(r.getKey(), new Result<>(r.getValue()));
            }

        return safeResults;
    }

    /**
     * The class <code>Result</code> is a wrapper around a result that handles the logging of exceptions to simplify
     * code
     * <p>
     * Creation date: 13/05/2013 - 2:46:09 PM
     * 
     * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
     * @version 1.0
     * @param <V>
     */
    public static class Result<V> {
        private final Future<V> mFuture;

        public Result(Future<V> future) {
            super();
            mFuture = future;
        }

        /**
         * Returns the value of this result
         * 
         * @return the value of this result
         * @author vpillac
         */
        public V get() {
            try {
                return mFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                VRPLogging.getOptLogger().exception("Result.get", e);
                return null;
            }
        }
    }

    /**
     * Shutdown this executor
     * 
     * @author vpillac
     */
    public void shutdown() {
        mExecutor.shutdown();
    }
}
