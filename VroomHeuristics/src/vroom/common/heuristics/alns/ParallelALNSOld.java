/**
 * 
 */
package vroom.common.heuristics.alns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.alns.IDestroy.IDestroyResult;
import vroom.common.utilities.BatchThreadPoolExecutor;
import vroom.common.utilities.BatchThreadPoolExecutor.NameThreadFactory;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.optimization.IComponentHandler;
import vroom.common.utilities.optimization.IComponentHandler.Outcome;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.OptimizationSense;

/**
 * <code>ParallelALNSOld</code> is a deprecated implementation of {@link ParallelALNS}
 * <p>
 * In particular it contains a <em>pull</em> scheme that was abandoned as it introduces more waiting times between
 * threads
 * </p>
 * <p>
 * Creation date: Nov 17, 2011 - 3:37:35 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @deprecated Use {@link ParallelALNS} instead
 * @see ParallelALNS
 */
@Deprecated
public class ParallelALNSOld<S extends ISolution> extends AdaptiveLargeNeighborhoodSearch<S> {

    /**
     * {@code true} if the subprocesses push their results to a share queue (push scheme), {@code false} if the main
     * thread collects the results when all subprocesses have finished (pull scheme)
     * <p>
     * 21/11/2011 The push scheme was introduced to improve memory management and prevent the storage of a great number
     * of temporary solutions
     * </p>
     */
    protected static boolean             sPushScheme = true;

    /** The solution pool */
    private IPALNSSolutionPool<S>        mSolPool;

    /** The executor used to execute subprocesses */
    private BatchThreadPoolExecutor      mExecutor;

    /** The number of iterations to be executed in parallel (cached for performance */
    private int                          mItP;

    private BlockingQueue<PALNSItResult> mResultsQueue;

    /**
     * Sets the solution pool
     * 
     * @param solPool
     *            the solution pool to be set
     */
    public void setSolPool(IPALNSSolutionPool<S> solPool) {
        if (solPool == null)
            throw new NullPointerException();
        mSolPool = solPool;
    }

    /**
     * Returns the solution pool
     * 
     * @return the solution pool
     */
    public IPALNSSolutionPool<S> getSolPool() {
        return mSolPool;
    }

    /**
     * Creates a new <code>ParallelALNS</code>
     * 
     * @param optimizationSense
     * @param rndStream
     * @param params
     * @param destroyComponents
     * @param repairComponents
     */
    public ParallelALNSOld(OptimizationSense optimizationSense, RandomStream rndStream, ALNSGlobalParameters params,
            IComponentHandler<IDestroy<S>> destroyComponents, IComponentHandler<IRepair<S>> repairComponents) {
        super(optimizationSense, rndStream, params, destroyComponents, repairComponents);
        mSolPool = getGlobalParameters().newInstance(ALNSGlobalParameters.PALNS_POOL, getOptimizationSense(), params);
    }

    @Override
    void initialize(IInstance instance, S solution, IParameters params) {
        if (getCurrentInstance() != null && getCurrentInstance() != instance)
            mSolPool.clear();

        super.initialize(instance, solution, params);

        if (mExecutor != null)
            mExecutor.shutdownNow();
        mExecutor = new BatchThreadPoolExecutor(getGlobalParameters().get(ALNSGlobalParameters.PALNS_THREAD_COUNT),
                new PALNSThreadFactory("pALNS"));

        mItP = getGlobalParameters().get(ALNSGlobalParameters.PALNS_IT_P);
        // mResultsQueue = new SynchronousQueue<ParallelALNS<S>.PALNSItResult>();
        mResultsQueue = new ArrayBlockingQueue<ParallelALNSOld<S>.PALNSItResult>(getGlobalParameters().get(
                ALNSGlobalParameters.PALNS_THREAD_COUNT)
                * getGlobalParameters().get(ALNSGlobalParameters.PALNS_IT_P));
    };

    @Override
    public S localSearch(IInstance instance, S solution, IParameters params) {
        if (params.getAcceptanceCriterion() != null)
            setAcceptanceCriterion(params.getAcceptanceCriterion());

        initialize(instance, solution, params);

        mSolPool.add(solution, true);

        getProgress().start();
        while (!getStoppingCriterion().isStopCriterionMet()) {
            // Select a subset of solutions
            Collection<S> solSubset = mSolPool.subset(mExecutor.getMaximumPoolSize(), getRandomStream());
            // Collection<S> solSubset = mSolPool.subset(Math.max(1, mExecutor.getMaximumPoolSize() - 1),
            // getRandomStream());
            getLogger().debug("ALNS %s: New iteration batch, stopping criterion: %s, solution pool size:%s",
                    getProgress(), getStoppingCriterion(), mSolPool.size());

            // Parallel forall
            // - Batch of subprocesses
            ArrayList<PALNSSubprocess> batch = new ArrayList<PALNSSubprocess>(solSubset.size());

            // Add the current best solution
            // if (mExecutor.getMaximumPoolSize() > 1)
            // batch.add(new PALNSSubprocess(mSolPool.getBest(), params));
            while (batch.size() < mExecutor.getMaximumPoolSize())
                for (S sol : solSubset) {
                    batch.add(new PALNSSubprocess(sol, params));
                    if (batch.size() == mExecutor.getMaximumPoolSize())
                        break;
                }

            // - Execute the subprocesses
            Map<PALNSSubprocess, Future<List<PALNSItResult>>> results = null;
            try {
                results = mExecutor.submitBatch(batch, false);
            } catch (InterruptedException e) {
                getLogger().fatalException("ParallelALNS.perfomLocalSearch", e);
                Logging.awaitLogging(5000);
                System.exit(1);
            }
            // - Synchronize with the results
            if (sPushScheme)
                synchronizePush();
            else
                synchronizePull(results);

            for (Entry<PALNSSubprocess, Future<List<PALNSItResult>>> r : results.entrySet()) {
                try {
                    r.getValue().get();
                } catch (Exception e) {
                    throw new IllegalStateException("pALNS subprocess terminated abnormally", e);
                }
                getSolPool().add(r.getKey().mSolution, true);
            }

            // Update the number of iterations
            getProgress().iterationsFinished(mItP * solSubset.size());
            // Update the global stopping criterion
            getStoppingCriterion().update(mItP * solSubset.size(), new Object[0]);

        }
        getProgress().stop();

        setStopped();
        getCallbacks().callbacks(
                new ALNSCallbackEvent<S>(ALNSEventType.FINISHED, this, getTimer().readTimeMS(), getProgress()
                        .getIteration(), instance, mSolPool.getBest()));

        mExecutor.shutdownNow();

        return mSolPool.getBest();
    }

    /**
     * Synchronization between {@link PALNSSubprocess subprocesses} using the push scheme
     * 
     * @see #sPushScheme
     */
    protected void synchronizePush() {
        while (!mExecutor.isBatchComplete() || !mResultsQueue.isEmpty()) {
            try {
                PALNSItResult result = mResultsQueue.poll(10, TimeUnit.MILLISECONDS);
                if (result != null)
                    synchronize(result);
            } catch (InterruptedException e) {
                getLogger().exception("ParallelALNS.synchronize", e);
            }
        }
    }

    /**
     * This method collect the results of various {@link PALNSSubprocess subprocesses} and updates all shared data with
     * the pull scheme
     * 
     * @param results
     *            the results of the subprocesses
     * @see #sPushScheme
     */
    protected void synchronizePull(Map<PALNSSubprocess, Future<List<PALNSItResult>>> results) {
        ArrayList<Future<List<PALNSItResult>>> futures = new ArrayList<Future<List<PALNSItResult>>>(results.values());
        boolean completed = false;
        while (!completed) {
            completed = true;

            for (int i = 0; i < futures.size(); i++) {
                Future<List<PALNSItResult>> future = futures.get(i);
                if (future == null)
                    continue;
                else if (!future.isDone()) {
                    completed = false;
                    continue;
                }

                futures.set(i, null);

                List<PALNSItResult> itResultList = null;
                try {
                    itResultList = future.get();
                } catch (InterruptedException e) {
                    getLogger().exception("ParallelALNS.synchronize", e);
                    continue;
                } catch (ExecutionException e) {
                    getLogger().exception("ParallelALNS.synchronize", e);
                    continue;
                }
                for (PALNSItResult result : itResultList) {
                    synchronize(result);
                }
            }
        }
    }

    /**
     * Synchronize shared data for a single result
     * 
     * @param result
     */
    protected void synchronize(PALNSItResult result) {
        Outcome state = result.mAccepted ? Outcome.ACCEPTED : Outcome.REJECTED;

        // Add the solution to the pool
        boolean added = mSolPool.add(result.mTempSol, false);
        if (added)
            state = Outcome.NEW_BEST;

        // Update destroy stats
        getDestroyComponents().updateStats(result.mDestroy,
                getAcceptanceCriterion().getImprovement(result.mCurrentSol, result.mTempSol), result.mTime,
                result.mIteration, state);

        // Update repair stats
        getRepairComponents().updateStats(result.mRepair,
                getAcceptanceCriterion().getImprovement(result.mCurrentSol, result.mTempSol), result.mTime,
                result.mIteration, state);

        // Execute callbacks
        getCallbacks().callbacks(
                new ALNSCallbackEvent<S>(ALNSEventType.REPAIRED, this, getTimer().readTimeMS(), getProgress()
                        .getIteration(), mSolPool.getBest(), result.mCurrentSol, result.mTempSol, result.mRepair,
                        result.mRepaired));
        S newCurrent = result.mCurrentSol;
        switch (state) {
        case NEW_BEST:
            getCallbacks().callbacks(
                    new ALNSCallbackEvent<S>(ALNSEventType.SOL_NEW_BEST, this, getTimer().readTimeMS(), getProgress()
                            .getIteration(), result.mTempSol));
            newCurrent = result.mTempSol;
            break;
        case ACCEPTED:
            getCallbacks().callbacks(
                    new ALNSCallbackEvent<S>(ALNSEventType.SOL_NEW_CURRENT, this, getTimer().readTimeMS(),
                            getProgress().getIteration(), mSolPool.getBest(), result.mTempSol));
            newCurrent = result.mTempSol;
            break;
        case REJECTED:
            getCallbacks().callbacks(
                    new ALNSCallbackEvent<S>(ALNSEventType.SOL_REJECTED, this, getTimer().readTimeMS(), getProgress()
                            .getIteration(), result.mTempSol));
            break;

        default:
            break;
        }

        getCallbacks().callbacks(
                new ALNSCallbackEvent<S>(ALNSEventType.IT_FINISHED, this, getTimer().readTimeMS(), getProgress()
                        .getIteration(), mSolPool.getBest(), newCurrent, result.mTempSol, getMainTimer()
                        .getReadOnlyStopwatch()));
    }

    /**
     * <code>PALNSSubprocess</code> is the class responsible to execute a subprocess of the parallel ALNS.
     * <p>
     * Creation date: Nov 17, 2011 - 5:16:43 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected class PALNSSubprocess implements Callable<List<PALNSItResult>> {

        /** The current solution */
        private S                 mSolution;
        /** Parameters */
        private final IParameters mParams;

        /**
         * The local copy of the acceptance criterion
         */
        // private final IAcceptanceCriterion mAccept;

        /**
         * Creates a new <code>PALNSSubprocess</code>
         * 
         * @param solution
         *            the starting solution
         * @param params
         *            parameters for the ALNS
         */
        public PALNSSubprocess(S solution, IParameters params) {
            mSolution = solution;
            mParams = params;
        }

        @Override
        public List<PALNSItResult> call() {
            @SuppressWarnings("unchecked")
            PALNSThread thread = (PALNSThread) Thread.currentThread();

            Stopwatch itTimer = new Stopwatch();

            ArrayList<PALNSItResult> subProcessResults = sPushScheme ? null : new ArrayList<PALNSItResult>(mItP);

            for (int it = 1; it <= mItP; it++) {
                itTimer.reset();
                itTimer.start();

                @SuppressWarnings("unchecked")
                S tmp = (S) mSolution.clone();

                // Select destroy operator
                IDestroy<S> destroy = getDestroyComponents().nextComponent();
                IDestroy<S> destroyClone = thread.getClone(destroy);
                double sizeMin = getGlobalParameters().get(ALNSGlobalParameters.DESTROY_SIZE_RANGE)[0];
                double sizeMax = getGlobalParameters().get(ALNSGlobalParameters.DESTROY_SIZE_RANGE)[1];
                double size = sizeMin + mParams.getRandomStream().nextDouble() * (sizeMax - sizeMin);
                // getLogger().lowDebug("ALNS %s: Selecting destroy %s (size=%.2f)", getProgress(), destroy, size);
                // Destroy solution
                IDestroyResult<S> result = destroyClone.destroy(tmp, mParams, size);
                getLogger().lowDebug("ALNS %s: Destroy result: %s ", getProgress(), result);

                // Select repair operator
                IRepair<S> repair = getRepairComponents().nextComponent();
                IRepair<S> repairClone = thread.getClone(repair);
                // getLogger().lowDebug("ALNS %s: Selecting repair  %s", getProgress(), repair);
                // Repair solution
                boolean repaired = repairClone.repair(tmp, result, mParams);
                if (isCheckSolutionAfterMove()) {
                    String err = checkSolution(tmp);
                    if (!err.isEmpty()) {
                        getLogger().warn("ALNS %s: Infeasible temporary solution: %s", getProgress(), err);
                    }
                }
                itTimer.stop();

                // FIXME Apply an optional post-repair local search

                // Test the solution and update the acceptance criterion
                // boolean accepted = mAccept.accept(mSolution, tmp);
                boolean accepted = ParallelALNSOld.this.getAcceptanceCriterion().accept(mSolution, tmp);
                // Add the results of this iteration
                PALNSItResult alnsResult = new PALNSItResult(mSolution, tmp, destroy, repair, repaired, accepted,
                        itTimer.readTimeMS(), getProgress().getIteration() + it);
                if (sPushScheme)
                    try {
                        boolean pushed = ParallelALNSOld.this.mResultsQueue.offer(alnsResult, 60, TimeUnit.SECONDS);
                        if (!pushed)
                            getLogger().error("PALNSSubprocess.call: unable to push result %s", alnsResult);
                    } catch (InterruptedException e) {
                        getLogger().exception("PALNSSubprocess.call", e);
                    }
                else
                    subProcessResults.add(alnsResult);

                if (accepted) {
                    // The new solution is accepted as current solution
                    mSolution = tmp;

                    getLogger().debug("ALNS %s: New current [%.2f] (d:%s,r:%s)", getProgress(),
                            mSolution.getObjectiveValue(), destroy, repair);
                }
            }
            return subProcessResults;
        }
    }

    /**
     * <code>PALNSItResult</code> contains the information required to update the operators scores when subprocesses
     * finish
     * <p>
     * Creation date: Nov 17, 2011 - 5:08:25 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected class PALNSItResult {

        protected final S           mCurrentSol;
        protected final S           mTempSol;
        protected final IDestroy<S> mDestroy;
        protected final IRepair<S>  mRepair;
        protected final boolean     mRepaired;
        protected final boolean     mAccepted;
        protected final double      mTime;
        protected final int         mIteration;

        /**
         * Creates a new <code>PALNSItResult</code>
         * 
         * @param currentSol
         *            the current solution
         * @param tempSol
         *            the temporary solution (at this iteration)
         * @param destroy
         *            the destroy operator used
         * @param repair
         *            the repair operator used
         * @param repaired
         *            {@code true} if the solution was successfully repaired
         * @param accepted
         *            {@code true} if the solution was accepted as current solution
         * @param time
         *            the current time
         * @param iteration
         *            the current iteration
         */
        public PALNSItResult(S currentSol, S tempSol, IDestroy<S> destroy, IRepair<S> repair, boolean repaired,
                boolean accepted, double time, int iteration) {
            mCurrentSol = currentSol;
            mTempSol = tempSol;
            mDestroy = destroy;
            mRepair = repair;
            mRepaired = repaired;
            mAccepted = accepted;
            mTime = time;
            mIteration = iteration;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("d:%s,r:%s,a:%s,t:%s,it:%s]", mDestroy, mRepair, mAccepted, mTime, mIteration);
        }
    }

    /**
     * <code>PALNSThread</code> is a specialization of {@link Thread} that contains a clone of all the ALNS components
     * <p>
     * Creation date: Nov 18, 2011 - 1:47:27 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected class PALNSThread extends Thread {

        private final Map<IDestroy<S>, IDestroy<S>> mDestroyClones;
        private final Map<IRepair<S>, IRepair<S>>   mRepairCopies;

        public PALNSThread(ThreadGroup group, Runnable runnable, String name, int stackSize) {
            super(group, runnable, name, stackSize);
            mDestroyClones = new HashMap<IDestroy<S>, IDestroy<S>>();
            mRepairCopies = new HashMap<IRepair<S>, IRepair<S>>();

            for (IDestroy<S> d : getDestroyComponents().getComponents())
                mDestroyClones.put(d, d.clone());
            for (IRepair<S> r : getRepairComponents().getComponents())
                mRepairCopies.put(r, r.clone());
        }

        /**
         * Returns this thread's clone of the given component
         * 
         * @param original
         * @return this thread's clone of the given component
         */
        public IDestroy<S> getClone(IDestroy<S> original) {
            return mDestroyClones.get(original);
        }

        /**
         * Returns this thread's clone of the given component
         * 
         * @param original
         * @return this thread's clone of the given component
         */
        public IRepair<S> getClone(IRepair<S> original) {
            return mRepairCopies.get(original);
        }

    }

    /**
     * <code>PALNSThreadFactory</code> is a specialization of {@link NameThreadFactory} that returns instances of
     * {@link PALNSThread}
     * <p>
     * Creation date: Nov 18, 2011 - 1:47:53 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected class PALNSThreadFactory extends NameThreadFactory {

        public PALNSThreadFactory(String name) {
            super(name);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new PALNSThread(getGroup(), r, getNamePrefix() + getThreadNumber().getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }

    }

    @Override
    public void dispose() {
        super.dispose();
        mSolPool.clear();
        mExecutor.shutdown();
    }
}
