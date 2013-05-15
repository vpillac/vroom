package vroom.optimization.online.jmsa;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import vroom.common.utilities.params.ClassParameterKey;
import vroom.common.utilities.params.GlobalParameters;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.params.RequiredParameter;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.DefaultComponentManager;
import vroom.optimization.online.jmsa.components.PoolCleanerBase;
import vroom.optimization.online.jmsa.components.RequestSamplerBase;
import vroom.optimization.online.jmsa.components.RequestValidatorBase;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorBase;
import vroom.optimization.online.jmsa.components.ScenarioOptimizerBase;
import vroom.optimization.online.jmsa.components.ScenarioUpdaterBase;
import vroom.optimization.online.jmsa.components.SolutionBuilderBase;
import vroom.optimization.online.jmsa.events.IMSAEventFactory;
import vroom.optimization.online.jmsa.events.MSAEventFactoryST;

/**
 * <code>MSAGlobalParameters</code> contains the parameters for the MSA procedure.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:49 a.m.
 */
public class MSAGlobalParameters extends GlobalParameters {

    /** The key for the event factory class parameter (default value provided) */
    @RequiredParameter
    public static final ClassParameterKey<IMSAEventFactory>      EVENT_FACTORY_CLASS      = new ClassParameterKey<IMSAEventFactory>(
                                                                                                  "EVENT_FACTORY_CLASS",
                                                                                                  IMSAEventFactory.class,
                                                                                                  MSAEventFactoryST.class);

    /** The key for the {@link ComponentManager} implementation (<b>Mandatory</b>) */
    @SuppressWarnings("rawtypes")
    @RequiredParameter
    public static final ClassParameterKey<ComponentManager>      COMPONENT_MANAGER_CLASS  = new ClassParameterKey<ComponentManager>(
                                                                                                  "COMPONENT_MANAGER_CLASS",
                                                                                                  ComponentManager.class,
                                                                                                  DefaultComponentManager.class);

    /** The key for the pool cleaner class parameter (<b>Mandatory</b>) */
    @RequiredParameter
    public static final ClassParameterKey<PoolCleanerBase>       POOL_CLEANER_CLASS       = new ClassParameterKey<PoolCleanerBase>(
                                                                                                  "POOL_CLEANER_CLASS",
                                                                                                  PoolCleanerBase.class);

    /** The key for the pool updater class parameter (<b>Mandatory</b>) */
    @RequiredParameter
    public static final ClassParameterKey<RequestValidatorBase>  REQUEST_VALIDATOR_CLASS  = new ClassParameterKey<RequestValidatorBase>(
                                                                                                  "REQUEST_VALIDATOR_CLASS",
                                                                                                  RequestValidatorBase.class);

    /** The key for the pool updater class parameter (<b>Mandatory</b>) */
    @RequiredParameter
    public static final ClassParameterKey<ScenarioUpdaterBase>   SCENARIO_UPDATER_CLASS   = new ClassParameterKey<ScenarioUpdaterBase>(
                                                                                                  "SCENARIO_UPDATER_CLASS",
                                                                                                  ScenarioUpdaterBase.class);

    /** The key for the request sampler class parameter (<b>Mandatory</b>) */
    @RequiredParameter
    public static final ClassParameterKey<RequestSamplerBase>    REQUEST_SAMPLER_CLASS    = new ClassParameterKey<RequestSamplerBase>(
                                                                                                  "REQUEST_SAMPLER_CLASS",
                                                                                                  RequestSamplerBase.class);

    /** The key for the scenario generator class parameter (<b>Mandatory</b>) */
    @SuppressWarnings("rawtypes")
    @RequiredParameter
    public static final ClassParameterKey<ScenarioGeneratorBase> SCENARIO_GENERATOR_CLASS = new ClassParameterKey<ScenarioGeneratorBase>(
                                                                                                  "SCENARIO_GENERATOR_CLASS",
                                                                                                  ScenarioGeneratorBase.class);

    /** The key for the scenario optimizer class parameter (<b>Mandatory</b>) */
    @SuppressWarnings("rawtypes")
    @RequiredParameter
    public static final ClassParameterKey<ScenarioOptimizerBase> SCENARIO_OPTIMIZER_CLASS = new ClassParameterKey<ScenarioOptimizerBase>(
                                                                                                  "SCENARIO_OPTIMIZER_CLASS",
                                                                                                  ScenarioOptimizerBase.class);

    /** The key for the mSolution builder class parameter (<b>Mandatory</b>) */
    @RequiredParameter
    public static final ClassParameterKey<SolutionBuilderBase>   SOLUTION_BUILDER_CLASS   = new ClassParameterKey<SolutionBuilderBase>(
                                                                                                  "SOLUTION_BUILDER_CLASS",
                                                                                                  SolutionBuilderBase.class);

    /**
     * The minimum proportion of compatible scenarios for a request to be accepted (relative to the current pool size,
     * default is 0.5)
     */
    @RequiredParameter
    public static final ParameterKey<Double>                     MIN_COMPATIBLE_SCEN_PROP = new ParameterKey<Double>(
                                                                                                  "MIN_COMPATIBLE_SCEN_PROP",
                                                                                                  Double.class, 0.50);

    /** The maximum size of the scenario pool (default 200) */
    @RequiredParameter
    public static final ParameterKey<Integer>                    POOL_SIZE                = new ParameterKey<Integer>(
                                                                                                  "POOL_SIZE",
                                                                                                  Integer.class, 200);

    /**
     * The initial proportion of the scenario pool to be generated (default 80%)
     */
    public static final ParameterKey<Double>                     POOL_INITIAL_PROPORTION  = new ParameterKey<Double>(
                                                                                                  "POOL_INITIAL_PROPORTION",
                                                                                                  Double.class, 0.8);

    /**
     * The maximum time to be spent on initial optimization of a scenario (in milliseconds) (default 5000)
     */
    @RequiredParameter
    public static final ParameterKey<Integer>                    GEN_MAX_SCEN_OPT_TIME    = new ParameterKey<Integer>(
                                                                                                  "GEN_MAX_SCEN_OPT_TIME",
                                                                                                  Integer.class, 5000);

    /**
     * The maximum time to be spent on subsequent optimization of a scenario (in milliseconds) (default 60000)
     */
    @RequiredParameter
    public static final ParameterKey<Integer>                    OPT_MAX_SCEN_OPT_TIME    = new ParameterKey<Integer>(
                                                                                                  "OPT_MAX_SCEN_OPT_TIME",
                                                                                                  Integer.class, 60000);

    /** The number of sampled requests to be generated (default 10) */
    @RequiredParameter
    public static final ParameterKey<Integer>                    SAMPLED_REQUEST_COUNT    = new ParameterKey<Integer>(
                                                                                                  "SAMPLED_REQUEST_COUNT",
                                                                                                  Integer.class, 10);

    /**
     * The random seed used by the random number generator.
     * <p>
     * If {@link #RANDOM_SEEDS} is defined, it will used instead
     * 
     * @see #RANDOM_SEEDS
     * @see MRG32k3a#setSeed(long[])
     */
    @RequiredParameter
    public static final ParameterKey<Long>                       RANDOM_SEED              = new ParameterKey<Long>(
                                                                                                  "RANDOM_SEED",
                                                                                                  Long.class);
    /**
     * An array containing the random seeds used by the random number generator
     * 
     * @see MRG32k3a#setSeed(long[])
     */
    @RequiredParameter
    public static final ParameterKey<long[]>                     RANDOM_SEEDS             = new ParameterKey<long[]>(
                                                                                                  "RANDOM_SEEDS",
                                                                                                  long[].class);

    /** The minimum number of threads to be used to handle events (default is 1) */
    @RequiredParameter
    public static final ParameterKey<Integer>                    MIN_THREADS              = new ParameterKey<Integer>(
                                                                                                  "MIN_THREADS",
                                                                                                  Integer.class, 1);

    /**
     * The maximum number of threads to be used to handle events (default is {@link Runtime#availableProcessors()})
     */
    @RequiredParameter
    public static final ParameterKey<Integer>                    MAX_THREADS              = new ParameterKey<Integer>(
                                                                                                  "MAX_THREADS",
                                                                                                  Integer.class,
                                                                                                  Runtime.getRuntime()
                                                                                                          .availableProcessors());

    static {
        addRequiredKeysByReflection(MSAGlobalParameters.class);
    }

    /**
     * Creates a new <code>MSAGlobalParameters</code> instance
     */
    public MSAGlobalParameters() {
        super();
    }

}// end GlobalParameters