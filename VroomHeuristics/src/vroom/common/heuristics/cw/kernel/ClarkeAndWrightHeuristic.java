/*
 * jCW : a java library for the development of saving based heuristics
 */
package vroom.common.heuristics.cw.kernel;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.IInitialization;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.vls.IVLSState;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.params.ClassParameterKey;
import vroom.common.utilities.ssj.RandomSourceBase;

/**
 * <code>ClarkeAndWrightHeuristic</code> is the entry point to the jCW framework.
 * <p>
 * It contains {@link CWParameters} definition, as well as a {@link ConstraintHandler} and a reference to the
 * {@link ISavingsAlgorithm} used.
 * <p>
 * It implements {@link Runnable} so that a CW heuristic can directly be run in a {@linkplain Thread thread}
 * <p>
 * Creation date: Apr 16, 2010 - 11:11:48 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ClarkeAndWrightHeuristic<S extends IVRPSolution<?>> extends RandomSourceBase implements
        Runnable, IInitialization<S> {

    /** The Constant VERSION_TAG. */
    public static final String         VERSION_TAG        = "v2 (2010-04-17)";

    public static final String         MAIN_RANDOM_STREAM = "CWRndStream";

    /**
     * The {@linkplain ISavingsAlgorithm saving algorithm} used in this instance.
     */
    private final ISavingsAlgorithm<S> mSavingsAlgo;

    /**
     * Getter for the savings algorithm.
     * 
     * @return The {@linkplain ISavingsAlgorithm saving algorithm} used in this instance
     */
    public ISavingsAlgorithm<S> getSavingsAlgo() {
        return mSavingsAlgo;
    }

    /** The {@linkplain CWParameters global parameters} used in this instance. */
    private final CWParameters mParameters;

    /**
     * Getter for the global parameters.
     * 
     * @return The {@linkplain CWParameters global parameters} used in this instance
     */
    public CWParameters getParameters() {
        return mParameters;
    }

    /**
     * The {@linkplain ConstraintHandler constraint handler} used in this instance.
     */

    private final ConstraintHandler<S> mConstraintHandler;

    /**
     * Getter for the constraint handler.
     * 
     * @return The {@linkplain ConstraintHandler constraint handler} used in this instance
     */
    public ConstraintHandler<S> getConstraintHandler() {
        return mConstraintHandler;
    }

    /** the solution factory used in this instance **/
    private ISolutionFactory mSolutionFactory;

    /**
     * Getter for the solution factory used in this instance
     * 
     * @return the value of solutionFactory
     */
    public ISolutionFactory getSolutionFactory() {
        return this.mSolutionFactory;
    }

    /**
     * Setter for the solution factory used in this instance
     * 
     * @param solutionFactory
     *            the solution factory to be used
     */
    public void setSolutionFactory(ISolutionFactory solutionFactory) {
        mSolutionFactory = solutionFactory;
    }

    /**
     * The {@linkplain IVRPInstance problem instance} that will be used in to run the heuristic.
     */
    private IVRPInstance mInstance;

    /** The mSolution that will be modified by the CW heuristic */
    private S            mSolution;

    /**
     * Getter for problem instance.
     * 
     * @param <I>
     *            the implementation of {@link IVRPInstance} to which the problem instance should be casted
     * @return The {@linkplain IVRPInstance problem instance} that will be used in to run the heuristic
     */
    public IVRPInstance getInstance() {
        return mInstance;
    }

    /**
     * Sets the problem instance : The {@linkplain IVRPInstance problem instance} that will be used in to run the
     * heuristic.
     * 
     * @param instance
     *            the value to be set for problem instance {@linkplain #isRunning() running flag})
     */
    public void initialize(IVRPInstance instance) {
        if (isRunning()) {
            throw new IllegalStateException("Heuristic is running");
        }
        mInstance = instance;
    }

    /**
     * Sets the problem instance : The {@linkplain IVRPInstance problem instance} that will be used in to run the
     * heuristic.
     * 
     * @param instance
     *            the value to be set for problem instance {@linkplain #isRunning() running flag})
     * @param mSolution
     *            the mSolution that will be modified by the cw heuristic
     */
    public void initialize(IVRPInstance instance, S solution) {
        if (isRunning()) {
            throw new IllegalStateException("Heuristic is running");
        }
        mInstance = instance;
        mSolution = solution;
    }

    /**
     * A flag for the state of the procedure: <code>true</code> if the heuristic is currently running.
     */
    private boolean mRunning;

    /**
     * Flag for the state of the procedure.
     * 
     * @return if the heuristic is currently running
     */
    public boolean isRunning() {
        return mRunning;
    }

    /**
     * Creates a new <code>ClarkeAndWrightHeuristic</code>.
     * <p>
     * The {@linkplain #getSavingsAlgo() savings algorithm} and the global parameters for this heuristic
     * {@linkplain #getConstraintHandler() constraint handler} will be instantiated using the given
     * <code>parameters</code>.
     * 
     * @param parameters
     */

    @SuppressWarnings("rawtypes")
    public ClarkeAndWrightHeuristic(CWParameters parameters) {
        mParameters = parameters;

        // Setup random source
        updateSeed(this.mParameters.get(CWParameters.RANDOM_SEED));

        mSavingsAlgo = getParameters().newInstance(CWParameters.ALGORITHM_CLASS, this);
        mConstraintHandler = getParameters().newInstance(CWParameters.CTR_HANDLER_CLASS);

        // Instantiate the solution factory
        mSolutionFactory = getParameters().newInstance(CWParameters.SOLUTION_FACTORY_CLASS);

        // Load the constraints defined as parameters
        for (ClassParameterKey<IConstraint> key : getParameters().getConstraintKeys()) {
            mConstraintHandler.addConstraint(getParameters()
                    .<IConstraint<S>> newInstance(key, this));
        }
    }

    /**
     * Creates a new <code>ClarkeAndWrightHeuristic</code> with the given saving algorithm and constraint handler
     * 
     * @param parameters
     *            the global parameters for this instance
     * @param savingsAlgo
     *            the saving algorithm to be used
     * @param constraintHandler
     *            a constraint handler containing all the considered constraints
     */

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ClarkeAndWrightHeuristic(CWParameters parameters,
            Class<? extends ISavingsAlgorithm> savingsAlgo, ConstraintHandler<S> constraintHandler) {
        mParameters = parameters;
        mConstraintHandler = constraintHandler;

        // Setup random source
        updateSeed(this.mParameters.get(CWParameters.RANDOM_SEED));

        // Instantiate the solution factory
        mSolutionFactory = getParameters().newInstance(CWParameters.SOLUTION_FACTORY_CLASS);

        mSavingsAlgo = Utilities.newInstance(savingsAlgo, this);
    }

    /**
     * Update the random stream seed (and set {@link CWParameters#RANDOM_SEED}
     * 
     * @author vpillac
     */
    public void updateSeed(long seed) {
        getParameters().set(CWParameters.RANDOM_SEED, seed);
        setMRG32k3aRndStream(seed, MAIN_RANDOM_STREAM);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        if (isRunning()) {
            throw new IllegalStateException("The heuristic is already running");
        }
        if (getInstance() == null) {
            throw new IllegalStateException("No instance has been defined");
        }

        mRunning = true;
        newSolution(null, getInstance(), null);
        mRunning = false;
    }

    /**
     * Getter for the current mSolution
     * 
     * @return the current mSolution found by the saving algorithm
     */
    public S getSolution() {
        return getSavingsAlgo().getSolution();
    }

    @Override
    public String toString() {
        return String.format("%s Algo:%s", this.getClass().getSimpleName(), mSavingsAlgo);
    }

    @Override
    public S newSolution(IVLSState<S> state, IInstance instance, IParameters params) {
        mInstance = (IVRPInstance) instance;
        getSavingsAlgo().initialize(mSolution);
        getSavingsAlgo().run();
        return getSolution();
    }
}
