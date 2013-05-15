/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package vroom.optimization.online.jmsa.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import vroom.common.utilities.BatchThreadPoolExecutor;
import vroom.common.utilities.Stopwatch;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.ISampledRequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.MSAGlobalParameters;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * The Class <code>DefaultComponentManager</code> is an implementation of {@link ComponentManager} that will use a
 * {@link ThreadPoolExecutor} to execute the following tasks:
 * <ul>
 * <li>{@linkplain #optimizePool(ScenarioOptimizerParam) Scenario optimization}</li>
 * <li>{@linkplain #generateScenarios(ScenarioGeneratorParam) Scenario generation}</li>
 * <li>{@linkplain #enforceDecision(int, IActualRequest) Decision enforcement}</li>
 * </ul>
 * The number of {@link Thread Threads} used will depend on the {@link MSAGlobalParameters#MAX_THREADS} parameter.
 * <p>
 * Creation date: Nov 30, 2010 - 10:05:55 AM.
 * 
 * @param <S>
 *            the generic type
 * @param <I>
 *            the generic type
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DefaultComponentManager<S extends IScenario, I extends IInstance> extends ComponentManager<S, I> {

    private final BatchThreadPoolExecutor mGenerateExecutor;

    /**
     * Creates a new component manager that will use a {@link ThreadPoolExecutor} to execute the time consuming tasks.
     * 
     * @param msa
     *            the parent MSA
     * @param msaProxy
     *            the msa proxy
     */
    public DefaultComponentManager(MSABase<S, I> msa, MSAProxy<S, I> msaProxy) {
        super(msa, msaProxy);
        mOptimizerPool = new LinkedList<ScenarioOptimizerBase<S>>();
        mGenerateExecutor = new BatchThreadPoolExecutor(getParentMSAProxy().getParameters().get(
                MSAGlobalParameters.MAX_THREADS), "msa-gen");
        mOptimizeExecutor = new BatchThreadPoolExecutor(getParentMSAProxy().getParameters().get(
                MSAGlobalParameters.MAX_THREADS), "msa-opt");
    }

    private final ReentrantLock                        mOptimizerPoolLock = new ReentrantLock();

    /** A pool of {@linkplain ScenarioOptimizerBase scenario optimizers}. */
    private final LinkedList<ScenarioOptimizerBase<S>> mOptimizerPool;

    private final BatchThreadPoolExecutor              mOptimizeExecutor;

    /**
     * Take an optimizer from the pool.
     * 
     * @return an optimizer
     */
    protected ScenarioOptimizerBase<S> takeOptimizer() {
        if (mOptimizerPool.isEmpty()) {
            return getParentMSAProxy().getParameters().<ScenarioOptimizerBase<S>> newInstance(
                    MSAGlobalParameters.SCENARIO_OPTIMIZER_CLASS, this);
        } else {
            ScenarioOptimizerBase<S> opt = null;
            try {
                mOptimizerPoolLock.lockInterruptibly();
                opt = mOptimizerPool.pop();
                return opt;
            } catch (Exception e) {
                opt = getParentMSAProxy().getParameters().<ScenarioOptimizerBase<S>> newInstance(
                        MSAGlobalParameters.SCENARIO_OPTIMIZER_CLASS, this);
            } finally {
                if (mOptimizerPoolLock.isHeldByCurrentThread()) {
                    mOptimizerPoolLock.unlock();
                }
            }
            return opt;
        }
    }

    /**
     * Release an optimizer (return it to the pool).
     * 
     * @param optimizer
     *            the optimizer to be released
     */
    protected void releaseOptimizer(ScenarioOptimizerBase<S> optimizer) {
        try {
            mOptimizerPoolLock.lockInterruptibly();
            mOptimizerPool.push(optimizer);
            mOptimizerPoolLock.unlock();
        } catch (Exception e) {
            // Do nothing
        } finally {
            if (mOptimizerPoolLock.isHeldByCurrentThread()) {
                mOptimizerPoolLock.unlock();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager# buildDistinguishedPlan
     * (vroom.optimization.online.jmsa.components.ISolutionBuilderParam )
     */
    @Override
    public IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam params) {
        return getSolutionBuilder().buildDistinguishedPlan(params);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#canBeServiced
     * (vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public boolean canBeServiced(IActualRequest request) {
        return getRequestValidator().canBeServiced(request);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#enforceDecision (int,
     * vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public Collection<S> enforceDecision(int resourceId, IActualRequest committedRequest) {
        List<S> removedScenarios = new LinkedList<S>();

        Iterator<S> it = getParentMSAProxy().getScenarioPool().iterator();
        while (it.hasNext()) {
            S s = it.next();
            if (!getScenarioUpdater().enforceDecision(s, committedRequest, resourceId)) {
                removedScenarios.add(s);
                it.remove();
            }
        }

        if (removedScenarios.size() > 0) {
            MSALogging.getComponentsLogger().debug(
                    "DefaultComponentManager.enforceDecision: removing %s incompatible scenarios",
                    removedScenarios.size());
        }

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#cleanPool()
     */
    @Override
    public Collection<IScenario> cleanPool() {
        return getPoolCleaner().cleanPool();
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager# generateSampledRequest
     * (vroom.optimization.online.jmsa.components.RequestSamplerParam )
     */
    @Override
    public List<ISampledRequest> generateSampledRequest(RequestSamplerParam params) {
        return getRequestSampler().generateSampledRequest(params);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#generateScenarios
     * (vroom.optimization.online.jmsa.components.ScenarioGeneratorParam)
     */
    @Override
    public void generateScenarios(ScenarioGeneratorParam params) {
        if (mMSAProxy.getScenarioPool().isFull()) {
            MSALogging.getComponentsLogger().lowDebug("DefaultComponentManager.generateScenarios: Pool is full");
        } else {
            MSALogging.getComponentsLogger().debug(
                    "DefaultComponentManager.generateScenarios: Generating new scenarios: %s", params);

            int count = 0;
            Stopwatch timer = new Stopwatch(params.getMaxInitTime() * params.getMaxScen());
            timer.start();

            int targetCount = Math.min(params.getMaxScen(), getParentMSAProxy().getScenarioPool()
                    .getRemainingCapacity());
            ArrayList<ScenarioGeneratorTask> batch = new ArrayList<>();
            for (int i = 0; i < targetCount; i++)
                batch.add(new ScenarioGeneratorTask(params));

            // Launch all the tasks and wait for the executor termination
            // tasks are responsible for checking the arrival of a preemptive event
            Map<ScenarioGeneratorTask, Future<S>> results;
            try {
                results = mGenerateExecutor.submitBatch(batch, true);
                for (Future<S> f : results.values()) {
                    try {
                        S s = f.get();
                        if (s != null)
                            getParentMSAProxy().getScenarioPool().addScenario(s);
                    } catch (Exception e) {
                        MSALogging.getComponentsLogger().exception("DefaultComponentManager.generateScenarios", e);
                    }
                }
            } catch (InterruptedException e) {
                MSALogging.getComponentsLogger().exception("DefaultComponentManager.generateScenarios", e);
            }

            timer.stop();
            MSALogging.getComponentsLogger().lowDebug("DefaultComponentManager.generateScenarios: New pool: %s",
                    mMSAProxy.getScenarioPool());
        }
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#insertRequest
     * (vroom.optimization.online.jmsa.IMSARequest[])
     */
    @Override
    public Collection<S> insertRequest(IMSARequest... requests) {
        LinkedList<S> removedScenarios = new LinkedList<S>();

        for (S s : mMSAProxy.getScenarioPool()) {
            if (!getScenarioUpdater().insertRequests(s, requests)) {
                // The scenario cannot accommodate all requests
                removedScenarios.add(s);
            }
        }

        if (removedScenarios.size() > 0) {
            MSALogging.getComponentsLogger().debug(
                    "DefaultComponentManager.insertRequest: removing %s incompatible scenarios",
                    removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager# startOfServiceUpdate(int,
     * vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public Collection<S> startOfServiceUpdate(int resourceId, IActualRequest request) {
        LinkedList<S> removedScenarios = new LinkedList<S>();

        for (S s : mMSAProxy.getScenarioPool()) {
            if (!getScenarioUpdater().startOfServiceUpdate(s, resourceId, request)) {
                // The scenario is not coherent with the current state
                removedScenarios.add(s);
            }
        }

        if (removedScenarios.size() > 0) {
            MSALogging.getComponentsLogger().debug(
                    "DefaultComponentManager.startOfServiceUpdate: removing %s incompatible scenarios",
                    removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#endOfServiceUpdate (int,
     * vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public Collection<S> endOfServiceUpdate(int resourceId, IActualRequest servedRequest) {
        LinkedList<S> removedScenarios = new LinkedList<S>();

        for (S s : mMSAProxy.getScenarioPool()) {
            if (!getScenarioUpdater().endOfServiceUpdate(s, resourceId, servedRequest)) {
                // The scenario is not coherent with the current state
                removedScenarios.add(s);
            }
        }

        if (removedScenarios.size() > 0) {
            MSALogging.getComponentsLogger().debug(
                    "DefaultComponentManager.endOfServiceUpdate: removing %s incompatible scenarios",
                    removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager# startServicingUpdate(int)
     */
    @Override
    public Collection<S> startServicingUpdate(int resourceId) {
        LinkedList<S> removedScenarios = new LinkedList<S>();

        for (S s : mMSAProxy.getScenarioPool()) {
            if (!getScenarioUpdater().startServicingUpdate(s, resourceId)) {
                // The scenario is not coherent with the current state
                removedScenarios.add(s);
            }
        }

        if (removedScenarios.size() > 0) {
            MSALogging.getComponentsLogger().debug(
                    "DefaultComponentManager.startServicingUpdate: removing %s incompatible scenarios",
                    removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager# stopServicingUpdate(int)
     */
    @Override
    public Collection<S> stopServicingUpdate(int resourceId) {
        LinkedList<S> removedScenarios = new LinkedList<S>();

        for (S s : mMSAProxy.getScenarioPool()) {
            if (!getScenarioUpdater().stopServicingUpdate(s, resourceId)) {
                // The scenario is not coherent with the current state
                removedScenarios.add(s);
            }
        }

        if (removedScenarios.size() > 0) {
            MSALogging.getComponentsLogger().debug(
                    "DefaultComponentManager.stopServicingUpdate: removing %s incompatible scenarios",
                    removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#optimizePool
     * (vroom.optimization.online.jmsa.components.ScenarioOptimizerParam)
     */
    @Override
    public void optimizePool(ScenarioOptimizerParam params) {
        // Prevent log flooding
        if (System.currentTimeMillis() - mLastOptTime > 1000) {
            MSALogging.getComponentsLogger().debug("DefaultComponentManager.optimizePool: Optimizing the pool (%s)",
                    params);
        } else {
            MSALogging.getComponentsLogger().lowDebug("DefaultComponentManager.optimizePool: Optimizing the pool (%s)",
                    params);
        }

        LinkedList<S> scen = new LinkedList<S>(mMSAProxy.getScenarioPool().getScenarios());

        Collections.sort(scen, new Comparator<IScenario>() {
            @Override
            public int compare(IScenario o1, IScenario o2) {
                return o1.getNonImprovingCount() - o2.getNonImprovingCount();
            }
        });

        Iterator<S> scenarios = scen.iterator();

        Stopwatch timer = new Stopwatch(params.getMaxTime());
        timer.start();

        // while (!getParentMSA().hasPendingEvents() && scenarios.hasNext() &&
        // !timer.hasTimedOut()) {
        // optimize(scenarios.next(), params);
        // }
        while (!getParentMSAProxy().isNextEventPreemptive() && scenarios.hasNext() && !timer.hasTimedOut()) {
            mOptimizeExecutor.execute(new ScenarioOptimizerTask(scenarios.next(), params));
        }

        // Wait for the executor termination
        // tasks are responsible for checking the arrival of a preemptive event
        try {
            mOptimizeExecutor.awaitBatchCompletion();
        } catch (InterruptedException e) {
            MSALogging.getComponentsLogger().exception("DefaultComponentManager.generateScenarios", e);
        }

        timer.stop();
        MSALogging.getComponentsLogger().lowDebug("DefaultComponentManager.optimizePool: Pool optimized in %sms%s",
                timer.readTimeMS(), getParentMSAProxy().isNextEventPreemptive() ? " (interrupted)" : "");

        mLastOptTime = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#optimize(vroom
     * .optimization.online.jmsa.IScenario, vroom.optimization.online.jmsa.components.ScenarioOptimizerParam)
     */
    @Override
    public boolean optimize(S scenario, ScenarioOptimizerParam params) {
        return getScenarioOptimizer().optimize(scenario, params);
    }

    /**
     * The Class <code>ScenarioOptimizerTask</code> is responsible for the execution of the optimization procedure on a
     * given scenario.
     * <p>
     * Creation date: Nov 30, 2010 - 10:11:51 AM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    protected class ScenarioOptimizerTask implements Runnable {

        /** The Scenario. */
        private final S                      mScenario;

        /** The Params. */
        private final ScenarioOptimizerParam mParams;

        /**
         * Creates a new <code>ScenarioOptimizerTask</code>.
         * 
         * @param scenario
         *            the scenario
         * @param params
         *            the params
         */
        protected ScenarioOptimizerTask(S scenario, ScenarioOptimizerParam params) {
            mScenario = scenario;
            mParams = params;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            // Check if there is a preemptive event
            if (!getParentMSAProxy().isNextEventPreemptive()) {
                ScenarioOptimizerBase<S> opt = takeOptimizer();
                opt.optimize(mScenario, mParams);
                releaseOptimizer(opt);
            }
        }
    }

    /**
     * The Class <code>ScenarioGeneratorTask</code> is responsible for the generation of a single scenario.
     * <p>
     * Creation date: Dec 3, 2010 - 8:54:48 AM.
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    protected class ScenarioGeneratorTask implements Callable<S> {

        /** The Params. */
        private final ScenarioGeneratorParam mParams;

        /**
         * Creates a new <code>ScenarioGeneratorTask</code>.
         * 
         * @param params
         *            the params
         */
        public ScenarioGeneratorTask(ScenarioGeneratorParam params) {
            mParams = params;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public S call() {
            S scenario = null;
            // Check if there is a preemptive event
            if (!getParentMSAProxy().isNextEventPreemptive()) {
                Stopwatch t = new Stopwatch();
                t.start();
                // ScenarioGeneratorBase<S> gen =
                // getParentMSA().getParameters().<ScenarioGeneratorBase<S>>
                // newInstance(
                // MSAGlobalParameters.SCENARIO_GENERATOR_CLASS,
                // DefaultComponentManager.this);
                scenario = getScenarioGenerator().generateScenario(mParams);

                ScenarioOptimizerBase<S> opt = takeOptimizer();
                boolean feas = opt.initialize(scenario,
                        new ScenarioOptimizerParam(Integer.MAX_VALUE,
                                (int) (mParams.getMaxInitTime() - t.readTimeMS()), false));
                releaseOptimizer(opt);
                // The generated scenario is feasible
                if (feas) {
                    // Add it to the pool
                    // mMSAProxy.getScenarioPool().addScenario(scenario);

                    // Log a message
                    MSALogging.getComponentsLogger()
                            .lowDebug("DefaultComponentManager.generateScenarios: New scenario generated in %s (%s)",
                                    t, scenario);
                } else {
                    // Log a message
                    MSALogging.getComponentsLogger().warn(
                            "DefaultComponentManager.generateScenarios: Infeasible generated, will be discarded (%s)",
                            scenario);
                    scenario = null;
                }
            }
            return scenario;
        }

    }

}
