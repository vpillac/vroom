/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vroom.optimization.online.jmsa.components;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import vroom.common.utilities.Timer;
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

// TODO: Auto-generated Javadoc
/**
 * The Class <code>DefaultComponentManager</code> is an implementation of {@link ComponentManager} that will use a {@link ThreadPoolExecutor} to
 * execute the following tasks:
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
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DefaultComponentManager<S extends IScenario, I extends IInstance> extends ComponentManager<S, I> {

    /**
     * Creates a new component manager that will use a {@link ThreadPoolExecutor} to execute the time consuming tasks.
     * 
     * @param msa
     *            the parent MSA
     * @param msaProxy
     *            the msa proxy
     */
    protected DefaultComponentManager(MSABase<S, I> msa, MSAProxy<S, I> msaProxy) {
        super(msa, msaProxy);
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.components.ComponentManager#buildDistinguishedPlan(vroom.optimization.online.jmsa.components.ISolutionBuilderParam
     * )
     */
    @Override
    public IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam params) {
        return getSolutionBuilder().buildDistinguishedPlan(params);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#canBeServiced(vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public boolean canBeServiced(IActualRequest request) {
        return getRequestValidator().canBeServiced(request);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#enforceDecision(int, vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public Collection<S> enforceDecision(int resourceId, IActualRequest committedRequest) {
        List<S> removedScenarios = new LinkedList<S>();

        for (S s : getParentMSAProxy().getScenarioPool()) {
            if (!getScenarioUpdater().enforceDecision(s, committedRequest, resourceId)) {
                removedScenarios.add(s);
            }
        }

        if (removedScenarios.size() > 0) {
            MSALogging.getComponentsLogger().debug(
                "SingleThreadComponentManager.enforceDecision: removing %s incompatible scenarios",
                removedScenarios.size());
        }

        getParentMSAProxy().getScenarioPool().removeScenarios(removedScenarios);

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
     * @see
     * vroom.optimization.online.jmsa.components.ComponentManager#generateSampledRequest(vroom.optimization.online.jmsa.components.RequestSamplerParam
     * )
     */
    @Override
    public List<ISampledRequest> generateSampledRequest(RequestSamplerParam params) {
        return getRequestSampler().generateSampledRequest(params);
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.components.ComponentManager#generateScenarios(vroom.optimization.online.jmsa.components.ScenarioGeneratorParam)
     */
    @Override
    public void generateScenarios(ScenarioGeneratorParam params) {
        // TODO monitor the MSA state to suspend when an event occurs

        if (mMSAProxy.getScenarioPool().isFull()) {
            MSALogging.getComponentsLogger().lowDebug(
                "SingleThreadComponentManager.generateScenarios: Pool is full");
        } else {
            MSALogging.getComponentsLogger().debug(
                "SingleThreadComponentManager.generateScenarios: Generating new scenarios: %s", params);

            int count = 0;
            Timer timer = new Timer(params.getMaxInitTime() * 1000l * params.getMaxScen());
            timer.start();

            while (!getParentMSA().hasPendingEvents() && count < params.getMaxScen()
                    && !mMSAProxy.getScenarioPool().isFull() && !timer.hasTimedOut()) {

                Timer t = new Timer();
                t.start();
                S scenario = getScenarioGenerator().generateScenario(params);
                boolean feas = getScenarioOptimizer().initialize(scenario, params.getMaxInitTime());
                if (feas) {
                    mMSAProxy.getScenarioPool().addScenario(scenario);
                    MSALogging.getComponentsLogger().lowDebug(
                        "SingleThreadComponentManager.generateScenarios: New scenario generated in %s (%s)",
                        t, scenario);
                    count++;
                } else {
                    MSALogging.getComponentsLogger()
                        .debug(
                            "SingleThreadComponentManager.generateScenarios: Infeasible generated, will be discarded (%s)",
                            scenario);
                }
            }

            MSALogging.getComponentsLogger().lowDebug(
                "SingleThreadComponentManager.generateScenarios: New pool: %s", mMSAProxy.getScenarioPool());
        }
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#insertRequest(vroom.optimization.online.jmsa.IMSARequest[])
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
                "SingleThreadComponentManager.insertRequest: removing %s incompatible scenarios",
                removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#startOfServiceUpdate(int, vroom.optimization.online.jmsa.IActualRequest)
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
                "SingleThreadComponentManager.startOfServiceUpdate: removing %s incompatible scenarios",
                removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#endOfServiceUpdate(int, vroom.optimization.online.jmsa.IActualRequest)
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
                "SingleThreadComponentManager.endOfServiceUpdate: removing %s incompatible scenarios",
                removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#startServicingUpdate(int)
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
                "SingleThreadComponentManager.startServicingUpdate: removing %s incompatible scenarios",
                removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#stopServicingUpdate(int)
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
                "SingleThreadComponentManager.stopServicingUpdate: removing %s incompatible scenarios",
                removedScenarios.size());
        }

        mMSAProxy.getScenarioPool().removeScenarios(removedScenarios);

        return removedScenarios;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#optimizePool(vroom.optimization.online.jmsa.components.ScenarioOptimizerParam)
     */
    @Override
    public void optimizePool(ScenarioOptimizerParam params) {
        // Prevent log flooding
        if (System.currentTimeMillis() - mLastOptTime > 1000) {
            MSALogging.getComponentsLogger().debug(
                "SingleThreadComponentManager.optimizePool: Optimizing the pool", params);
        } else {
            MSALogging.getComponentsLogger().lowDebug(
                "SingleThreadComponentManager.optimizePool: Optimizing the pool", params);
        }
        mLastOptTime = System.currentTimeMillis();

        LinkedList<S> scen = new LinkedList<S>(mMSAProxy.getScenarioPool().getScenarios());

        Collections.sort(scen, new Comparator<IScenario>() {
            @Override
            public int compare(IScenario o1, IScenario o2) {
                return o1.getNonImprovingCount() - o2.getNonImprovingCount();
            }
        });

        Iterator<S> scenarios = scen.iterator();

        // TODO monitor the MSA state to suspend when an event occurs
        Timer timer = new Timer(params.getMaxTime() * 1000l);
        timer.start();
        while (!getParentMSA().hasPendingEvents() && scenarios.hasNext() && !timer.hasTimedOut()) {
            optimize(scenarios.next(), params);
        }
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.ComponentManager#optimize(vroom.optimization.online.jmsa.IScenario,
     * vroom.optimization.online.jmsa.components.ScenarioOptimizerParam)
     */
    @Override
    public boolean optimize(S scenario, ScenarioOptimizerParam params) {
        return getScenarioOptimizer().optimize(scenario, params);
    }

    /**
     * The Class <code>ScenarioOptimizerTask</code> is responsible for the execution of the optimization procedure on a given scenario.
     * <p>
     * Creation date: Nov 30, 2010 - 10:11:51 AM.
     * 
     * @param <SS>
     *            the scenario class type
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
     *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected class ScenarioOptimizerTask<SS extends S> implements Runnable {

        /** The Scenario. */
        private final SS                     mScenario;

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
        protected ScenarioOptimizerTask(SS scenario, ScenarioOptimizerParam params) {
            mScenario = scenario;
            mParams = params;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            getScenarioOptimizer().optimize(mScenario, mParams);
        }
    }

}
