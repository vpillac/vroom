package vroom.optimization.online.jmsa.components;

import java.util.Collection;
import java.util.List;

import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.ISampledRequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.MSABase.MSAProxy;
import vroom.optimization.online.jmsa.MSAGlobalParameters;

/**
 * <code>MSAComponentManager</code> is the class responsible for the management of the components of an MSA procedure. Calls to the components methods
 * should be performed by using the delegate methods from this class, as it enables multithreading management.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:49 a.m.
 * @param <S>
 *            the implementation of {@link IScenario} used in the MSA procedure
 * @param <I>
 *            the implementation of {@link IInstance} used in the MSA procedure
 */
public abstract class ComponentManager<S extends IScenario, I extends IInstance> {

    /**
     * Factory method for the creation of a <code>ComponentManager</code> for a singled thread MSA procedure
     * 
     * @param <S>
     *            the type of scenario
     * @param <D>
     *            the type of mSolution
     * @param <I>
     *            the type of instance
     * @param msa
     *            the parent MSA procedure
     * @return a <code>ComponentManager</code> associated with the given <code>msa</code> that can be used in a single thread context
     */
    public static <S extends IScenario, I extends IInstance> ComponentManager<S, I> newSingleThreadComponentManager(
            MSABase<S, I> msa, MSAProxy<S, I> msaProxy) {
        return new DefaultComponentManager<S, I>(msa, msaProxy);
    }

    /**
     * The parent MSA instance
     */
    private final MSABase<S, I>              mMSA;

    /**
     * A proxy to access some of the MSA properties
     */
    protected final MSAProxy<S, I>           mMSAProxy;

    /** The <code>PoolCleanerBase</code> to be used **/
    protected final PoolCleanerBase          mPoolCleaner;

    /** The <code>RequestValidatorBase</code> to be used **/
    protected final RequestValidatorBase     mRequestValidator;

    /** The <code>ScenarioUpdaterBase</code> to be used **/
    protected final ScenarioUpdaterBase      mScenarioUpdater;

    /** The <code>RequestSamplerBase</code> to be used **/
    protected final RequestSamplerBase       mRequestSampler;

    /** The <code>ScenarioGeneratorBase</code> to be used **/
    protected final ScenarioGeneratorBase<S> mScenarioGenerator;

    /** The <code>ScenarioOptimizerBase</code> to be used **/
    protected final ScenarioOptimizerBase<S> mScenarioOptimizer;

    /** The <code>SolutionBuilderBase</code> to be used **/
    protected final SolutionBuilderBase      mSolutionBuilder;

    protected long                           mLastOptTime;

    /**
     * Creates a new <code>ComponentManager</code> associated with the given {@linkplain MSABase MSA procedure}
     * 
     * @param msa
     */
    public ComponentManager(MSABase<S, I> msa, MSAProxy<S, I> msaProxy) {
        this.mMSA = msa;
        this.mMSAProxy = msaProxy;

        this.mPoolCleaner = msa.getParameters().<PoolCleanerBase> newInstance(
            MSAGlobalParameters.POOL_CLEANER_CLASS, this);

        this.mRequestValidator = msa.getParameters().<RequestValidatorBase> newInstance(
            MSAGlobalParameters.REQUEST_VALIDATOR_CLASS, this);

        this.mScenarioUpdater = msa.getParameters().<ScenarioUpdaterBase> newInstance(
            MSAGlobalParameters.SCENARIO_UPDATER_CLASS, this);

        this.mRequestSampler = msa.getParameters().<RequestSamplerBase> newInstance(
            MSAGlobalParameters.REQUEST_SAMPLER_CLASS, this);

        this.mScenarioGenerator = msa.getParameters().<ScenarioGeneratorBase<S>> newInstance(
            MSAGlobalParameters.SCENARIO_GENERATOR_CLASS, this);

        this.mScenarioOptimizer = msa.getParameters().<ScenarioOptimizerBase<S>> newInstance(
            MSAGlobalParameters.SCENARIO_OPTIMIZER_CLASS, this);

        this.mSolutionBuilder = msa.getParameters().<SolutionBuilderBase> newInstance(
            MSAGlobalParameters.SOLUTION_BUILDER_CLASS, this);
    }

    /**
     * Building of a distinguished mSolution from the given <code>pool</code> and based on the given <code>instance</code>
     * 
     * @param params
     *            an optional parameter for the building of the distinguished plan
     * @see SolutionBuilderBase#buildDistinguishedPlan(ISolutionBuilderParam)
     */
    public abstract IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam params);

    /**
     * Validation of a request
     * 
     * @param request
     *            the request that has to be validated
     * @return <code>true</code> if the request can be serviced, <code>false</code> otherwise
     * @see RequestValidatorBase#canBeServiced(IActualRequest)
     */
    public abstract boolean canBeServiced(IActualRequest request);

    /**
     * Remove the scenarios from the pool that are incompatible with the given <code>instance</code>
     * 
     * @return a collection containing the scenarios that were removed
     * @see PoolCleanerBase#cleanPool()
     */
    public abstract Collection<IScenario> cleanPool();

    /**
     * Update the scenario pool after the servicing of a request by a resource and remove scenarios that are incoherent with the current state.
     * 
     * @param resourceId
     *            the id (index) of the considered resource
     * @param servedRequest
     *            the request that has been served by the specified resource
     * @return a collection containing the removed scenarios
     * @see ScenarioUpdaterBase#endOfServiceUpdate(IScenario, int, IActualRequest)
     */
    public abstract Collection<S> endOfServiceUpdate(int resourceId, IActualRequest servedRequest);

    /**
     * Remove the scenarios from the pool that are incompatible with the current distinguished mSolution
     * 
     * @param committedRequest
     *            the request that will be served next by the specified resource
     * @param resourceId
     *            the id of the resource that will be committed to <code>committedRequest</code>
     * @return a collection containing the scenarios that were removed
     * @see ScenarioUpdaterBase#enforceDecision(IScenario, IActualRequest, int)
     * @see MSABase#getDistinguishedSolution()
     */
    public abstract Collection<S> enforceDecision(int resourceId, IActualRequest committedRequest);

    /**
     * Generation of sampled requests
     * 
     * @return an array of length <code>numRequests</code> containing the generated sampled requests
     * @param params
     *            an optional parameter for the generation of sampled requests
     * @see RequestSamplerBase#generateSampledRequest(RequestSamplerParam)
     */
    public abstract List<ISampledRequest> generateSampledRequest(RequestSamplerParam params);

    /**
     * Generation of a new scenario. <br/>
     * The implementation of this method should consider the possible parallelization of scenario generation
     * 
     * @param params
     *            optional parameters for the scenario generation s * @see ScenarioGeneratorBase#generateScenario(ScenarioGeneratorParam)
     */
    public abstract void generateScenarios(ScenarioGeneratorParam params);

    /**
     * Getter for the parent MSA
     * 
     * @return the parent MSA instance
     */
    public MSABase<S, I> getParentMSA() {
        return this.mMSA;
    }

    /**
     * Getter for the proxy to the parent MSA
     * 
     * @return a proxy for the parent MSA
     */
    public MSAProxy<S, I> getParentMSAProxy() {
        return this.mMSAProxy;
    }

    /**
     * @return a instance of {@link PoolCleanerBase} to be used in the MSA procedure
     */
    public PoolCleanerBase getPoolCleaner() {
        return this.mPoolCleaner;
    }

    /**
     * @return an instance of {@link RequestSamplerBase} to be used in the MSA procedure
     */
    public RequestSamplerBase getRequestSampler() {
        return this.mRequestSampler;
    }

    /**
     * @return an instance of {@link RequestValidatorBase} to be used in the MSA procedure
     */
    public RequestValidatorBase getRequestValidator() {
        return this.mRequestValidator;
    }

    /**
     * @return an instance of {@link ScenarioGeneratorBase} to be used in the MSA procedure
     */
    public ScenarioGeneratorBase<S> getScenarioGenerator() {
        return this.mScenarioGenerator;
    }

    /**
     * @return an instance of {@link ScenarioOptimizerBase} to be used in the MSA procedure
     */
    public ScenarioOptimizerBase<S> getScenarioOptimizer() {
        return this.mScenarioOptimizer;
    }

    /**
     * @return an instance of {@link ScenarioUpdaterBase} to be used in the MSA procedure
     */
    public ScenarioUpdaterBase getScenarioUpdater() {
        return this.mScenarioUpdater;
    }

    /**
     * @return an instance of {@link SolutionBuilderBase} to be used in the MSA procedure
     */
    public SolutionBuilderBase getSolutionBuilder() {
        return this.mSolutionBuilder;
    }

    /**
     * Perform the update of the <code>pool</code> by inserting all the requests from <code>requests</code> and removing incompatible scenarios
     * 
     * @param requests
     *            the requests that will be inserted in the scenarios from the <code>pool</code>
     * @see ScenarioUpdaterBase#insertRequests(IScenario, IMSARequest...)
     * @return a collection containing the removed scenarios
     */
    public abstract Collection<S> insertRequest(IMSARequest... requests);

    /**
     * Optimize a single scenario.
     * 
     * @param params
     *            an optional parameter for the optimization of scenarios
     * @param scenario
     *            the scenario to be optimized
     * @return <code>true</code> if the optimization procedure finished correctly, <code>false</code> otherwise
     * @see ScenarioOptimizerBase#optimize(IScenario, ScenarioOptimizerParam)
     */
    public abstract boolean optimize(S scenario, ScenarioOptimizerParam params);

    /**
     * Optimize the whole pool of scenarios of the associated MSA procedure
     * 
     * @param params
     *            an optional parameter for the optimization of scenarios
     * @see ScenarioOptimizerBase#optimize(IScenario, ScenarioOptimizerParam)
     */
    public abstract void optimizePool(ScenarioOptimizerParam params);

    /**
     * Update the scenario pool when a resource starts the service of a request.
     * 
     * @param resourceId
     *            the id (index) of the considered resource
     * @param request
     *            the request which is beeing served by the specified resource
     * @return a collection containing the removed scenarios
     */
    public abstract Collection<S> startOfServiceUpdate(int resourceId, IActualRequest request);

    /**
     * Update the scenario pool the start of servicing by a resource and remove scenarios that are incoherent with the current state
     * 
     * @param resourceId
     *            the id (index) of the considered resource
     * @return a collection containing the removed scenarios
     * @see ScenarioUpdaterBase#startServicingUpdate(IScenario, int)
     */
    public abstract Collection<S> startServicingUpdate(int resourceId);

    /**
     * Update the scenario pool after the end of servicing by a resource
     * 
     * @param resourceId
     *            the id (index) of the considered resource and remove scenarios that are incoherent with the current state
     * @return a collection containing the removed scenarios
     * @see ScenarioUpdaterBase#stopServicingUpdate(IScenario, int)
     */
    public abstract Collection<S> stopServicingUpdate(int resourceId);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.getClass().getSimpleName());
        sb.append("\n");

        sb.append("Scenario Generator:\n ");
        sb.append(getScenarioGenerator());
        sb.append("\n");

        sb.append("Scenario Optimizer:\n ");
        sb.append(getScenarioOptimizer());
        sb.append("\n");

        sb.append("Scenario Updater:\n ");
        sb.append(getScenarioUpdater());
        sb.append("\n");

        sb.append("Solution Builder:\n ");
        sb.append(getSolutionBuilder());
        sb.append("\n");

        sb.append("Pool Cleaner:\n ");
        sb.append(getPoolCleaner());
        sb.append("\n");

        sb.append("Request Sampler:\n ");
        sb.append(getRequestSampler());
        sb.append("\n");

        sb.append("Request Validator:\n ");
        sb.append(getRequestValidator());

        return sb.toString();
    }
}// end ComponentManager