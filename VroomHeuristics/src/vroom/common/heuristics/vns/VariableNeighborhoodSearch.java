package vroom.common.heuristics.vns;

import java.util.Arrays;
import java.util.List;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.GenericNeighborhoodHandler;
import vroom.common.heuristics.Identity;
import vroom.common.heuristics.LocalSearchBase;
import vroom.common.heuristics.utils.HeuristicsLogging;
import vroom.common.heuristics.vrp.IVRPSolutionNeighborhood;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.optimization.IComponentHandler;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.IStoppingCriterion;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.common.utilities.optimization.SimpleStoppingCriterion;

/**
 * <code>VariableNeighborhoodSearch</code> is an implementation of.
 * 
 * @param <S>
 *            the generic type {@link ILocalSearch} that uses different {@linkplain IVRPSolutionNeighborhood
 *            neighborhoods} to explore the vicinity of a mSolution.
 *            <p>
 *            <b>Pseudo-code:</b>
 *            </p>
 *            <p>
 *            <code>
 * <b>input :</b> S  - a solution <br/>
 * <b>output:</b> S* - the best solution found
 *            </p>
 *            <p>
 *            S* <- S<br/>
 *            k <- 1 // Select first neighborhood<br/>
 *            while !stopCriterion() <br/>
 *            &nbsp;S’ <- shake(S,Nk) // Generate neighbor <br/>
 *            &nbsp;S’ <- ls(S’,N,k) // Improve neighbor <br/>
 *            &nbsp;if f(S’) < f(S) // Improvement found <br/>
 *            &nbsp;&nbsp;S <- S’ k <- 1 // Select first neighborhood <br/>
 *            &nbsp;else <br/>
 *            &nbsp;&nbsp;k <- k+1 // Select next neighborhood <br/>
 *            &nbsp;end-if<br/>
 *            end-while<br/>
 *            return S<br/>
 *            </code>
 *            </p>
 *            <p>
 *            Creation date: Apr 26, 2010 - 4:38:07 PM
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VariableNeighborhoodSearch<S extends ISolution> extends LocalSearchBase<S> {

    /**
     * <code>VNSVariant</code> is an enumeration of some generic VNS variants that can be used to instantiate a
     * {@link VariableNeighborhoodSearch} with default components.
     * <p>
     * Creation date: 11 juil. 2010 - 20:57:55
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp" >SLP</a>
     * @version 1.0
     */
    public static enum VNSVariant {

        /** Descendant Variable Neighborhood Search with best improvement. */
        VND(true, true, false),

        /** Reduced Variable Neighborhood Search. */
        RVNS(true, true, true),

        /** Basic Variable Neighborhood Search. */
        BVNS(false, false, false),

        /** General Variable Neighborhood Search. */
        GVNS(false, false, false);

        /** <code>true</code> if no shake should be applied. */
        protected final boolean identityShake;

        /**
         * <code>true</code> if the local search should explore the current neighborhood.
         */
        protected final boolean neighLS;

        /** <code>true</code> if the local search should be randomized. */
        protected final boolean rndLS;

        private VNSVariant(boolean identityShake, boolean neighLS, boolean rndLS) {
            this.identityShake = identityShake;
            this.neighLS = neighLS;
            this.rndLS = rndLS;
        }
    }

    /** The Constant LOGGER. */
    public final static LoggerHelper                     LOGGER = HeuristicsLogging
                                                                        .getLogger("VNS");

    /** the neighborhood handler associated with this VNS *. */
    private final IComponentHandler<INeighborhood<S, ?>> mNeighHandler;

    /**
     * Getter for the neighborhood handler associated with this VNS.
     * 
     * @return the value of neighHandler
     */
    public IComponentHandler<INeighborhood<S, ?>> getNeighHandler() {
        return this.mNeighHandler;
    }

    /** <code>true</code> if the shake operator should be applied */
    private final boolean   mShake;

    /** the local search operator *. */
    private ILocalSearch<S> mLocalSearch;

    /**
     * Getter for the local search operator.
     * 
     * @return the value of localSearch
     */
    public ILocalSearch<S> getLocalSearch() {
        return this.mLocalSearch;
    }

    /**
     * Setter for the local search operator.
     * 
     * @param localSearch
     *            the value to be set for localSearch
     */
    public void setLocalSearch(ILocalSearch<S> localSearch) {
        this.mLocalSearch = localSearch;
    }

    /** The Shake parameters. */
    private IParameters mLSParameters, mShakeParameters;

    /**
     * Creates a new <code>VariableNeighborhoodSearch</code>.
     * 
     * @param sense
     *            the optimization sense
     * @param localSearch
     *            the local search component, if <code>null</code>, then the current neighborhood will be used as local
     *            search.
     * @param neighborhoods
     *            a list of neighborhoods to be used in this vns
     * @param shake
     *            <code>true</code> if the shake operator should be applied with the current neighborhood
     * @param rndStream
     *            the {@link RandomStream} to be used in this instance
     */
    public VariableNeighborhoodSearch(OptimizationSense sense, ILocalSearch<S> localSearch,
            List<INeighborhood<S, ?>> neighborhoods, boolean shake, RandomStream rndStream) {
        super(sense);
        mLocalSearch = localSearch;
        mNeighHandler = new GenericNeighborhoodHandler<S>(neighborhoods, rndStream);
        setDefaultParameters(VNSVariant.VND);
        mShake = shake;
        setRandomStream(rndStream);
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.ILocalSearch#perfomLocalSearch(vroom.common.utilities .optimization.IInstance,
     * vroom.common.utilities.optimization.ISolution, vroom.common.utilities.optimization.IParameters)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public synchronized S localSearch(IInstance instance, S solution, IParameters params) {
        if (solution == null) {
            throw new IllegalArgumentException("Argument mSolution cannot be null");
        }
        setRunning();

        getNeighHandler().initialize(null);

        if (params == null) {
            params = SimpleParameters.newUnlimitedThoroughDetParameters();
        }

        // Clone the mSolution
        S sol = (S) solution.clone();

        IParameters oldLSParams = mLSParameters;
        IParameters oldShakeParams = mShakeParameters;
        IStoppingCriterion stop = null;
        if (params instanceof VNSParameters) {
            VNSParameters vnsParams = (VNSParameters) params;
            stop = vnsParams.getStoppingCriterion();
            mLSParameters = vnsParams.getLSParameters();
            mShakeParameters = vnsParams.getShakeParameters();
        } else {
            stop = new SimpleStoppingCriterion(params.getMaxTime(), params.getMaxIterations());
        }

        stop.init();

        LOGGER.info("VNS started with solution %s \t(params: %s)", solution, params);
        LOGGER.info("Neighborhoods: %s", getNeighHandler());
        LOGGER.debug("Stopping criterion: %s", stop);

        int iteration = 0;
        while (!stop.isStopCriterionMet() && !getNeighHandler().isCompletelyExplored()) {
            getMainTimer().reset();
            INeighborhood<S, ?> neigh = getNeighHandler().nextComponent();
            if (neigh == null) {
                break;
            }

            S temp = (S) sol.clone();
            LOGGER.debug("Current solution: %s", temp);

            LOGGER.debug("Selecting neighborhood %s", neigh);

            getMainTimer().start();

            // Shake
            if (mShake) {
                neigh.pertub(instance, temp, getShakeParameters(neigh, params));
                LOGGER.debug("Shake: \t(neigh:%s, time:%s, sol:%s)", neigh, getMainTimer()
                        .readTimeMS(), temp);
            }

            // Local search
            ILocalSearch<S> ls = getLocalSearch() == null ? neigh : getLocalSearch();
            Stopwatch lsTimer = new Stopwatch();
            lsTimer.start();
            temp = ls.localSearch(instance, temp, params);
            lsTimer.stop();

            if (!(ls instanceof Identity)) {
                LOGGER.debug("Local search finished \t(new sol:%s, time:%s", temp,
                        lsTimer.readTimeMS());
            }
            getMainTimer().stop();

            double improvement = getOptimizationSense().getImprovement(sol.getObjectiveValue(),
                    temp.getObjectiveValue());

            if (getAcceptanceCriterion().accept(sol, temp)) {
                LOGGER.debug(
                        "New solution accepted \t(neigh:%s, improv:%s, new sol:%s, acceptanceCriterion:%s)",
                        neigh, improvement, temp, getAcceptanceCriterion());
                sol = temp;

                getNeighHandler().updateStats(neigh, improvement, getMainTimer().readTimeMS(),
                        iteration, IComponentHandler.Outcome.ACCEPTED);
            } else {
                LOGGER.debug(
                        "New solution rejected \t(neigh:%s, improv:%s, new sol:%s, acceptanceCriterion:%s)",
                        neigh, improvement, temp, getAcceptanceCriterion());
                getNeighHandler().updateStats(neigh, improvement, getMainTimer().readTimeMS(),
                        iteration, IComponentHandler.Outcome.REJECTED);
            }

            stop.update(improvement);
            iteration++;
        }

        setStopped();

        LOGGER.info("VNS finished, final solution \t(obj change:%s) %s",
                ((Comparable) sol.getObjective()).compareTo(solution.getObjective()), sol);

        mLSParameters = oldLSParams;
        mShakeParameters = oldShakeParams;

        return sol;
    }

    /**
     * Reset this variable neighborhood search.
     */
    public void reset() {
        getNeighHandler().reset();
    }

    /**
     * Return the parameters to be used for the given neighborhood.
     * 
     * @param neigh
     *            the current neighborhood
     * @param params
     *            the vns parameters
     * @return the parameters to be used for the given neighborhood
     */
    protected IParameters getShakeParameters(INeighborhood<S, ?> neigh, IParameters params) {
        return mShakeParameters;
    }

    /**
     * Return the parameters to be used for the local search procedure.
     * 
     * @param neigh
     *            the current neighborhood
     * @param params
     *            the vns parameters
     * @return the parameters to be used for the local search procedure
     */
    protected IParameters getLSParameters(INeighborhood<S, ?> neigh, IParameters params) {
        return mLSParameters;
    }

    // ---------------------------------------
    // Factory methods
    // ---------------------------------------

    /**
     * Factory method to create a new VNS.
     * 
     * @param <S>
     *            the generic type
     * @param variant
     *            the vns variant to be instantiated
     * @param sense
     *            the optimization sense
     * @param ls
     *            the local search component (can be <code>null</code>)
     * @param rndStream
     *            the random stream that will be used in the created vns
     * @param neighborhoods
     *            the list of neighborhoods to be used
     * @return a new variable neighborhood search
     */
    public static <S extends ISolution> VariableNeighborhoodSearch<S> newVNS(VNSVariant variant,
            OptimizationSense sense, ILocalSearch<S> ls, RandomStream rndStream,
            List<INeighborhood<S, ?>> neighborhoods) {
        if (variant.neighLS && ls != null) {
            throw new IllegalArgumentException(String.format(
                    "Local search operator should be null (vns variant:%s ls:%s)", variant, ls));
        }

        VariableNeighborhoodSearch<S> vns = new VariableNeighborhoodSearch<S>(sense, ls,
                neighborhoods, !variant.identityShake, rndStream);

        vns.setDefaultParameters(variant);

        return vns;
    }

    /**
     * Factory method to create a new VNS.
     * 
     * @param <S>
     *            the generic type
     * @param variant
     *            the vns variant to be instantiated
     * @param sense
     *            the optimization sense
     * @param ls
     *            the local search component (can be <code>null</code>)
     * @param rndStream
     *            the random stream that will be used in the created vns
     * @param neighborhoods
     *            the list of neighborhoods to be used
     * @return a new variable neighborhood search
     */
    public static <S extends ISolution> VariableNeighborhoodSearch<S> newVNS(VNSVariant variant,
            OptimizationSense sense, ILocalSearch<S> ls, RandomStream rndStream,
            INeighborhood<S, ?>... neighborhoods) {
        return newVNS(variant, sense, ls, rndStream, Arrays.asList(neighborhoods));
    }

    /**
     * Sets the default shake and ls parameters for the given vns variant.
     * <p>
     * Note that these parameters can be overridden when calling
     * 
     * @param variant
     *            the new default parameters {@link #localSearch(IInstance, ISolution, IParameters)}
     */
    private synchronized void setDefaultParameters(VNSVariant variant) {
        mLSParameters = SimpleParameters.newUnlimitedThoroughDetParameters();
        mShakeParameters = SimpleParameters.PERTURBATION;
        ((SimpleParameters) mShakeParameters).setRandomStream(getRandomStream());
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        if (this.getLocalSearch() != null) {
            return String.format("%s \n Shake:%s (%s)\n LS:%s (%s)", this.getClass()
                    .getSimpleName(), getNeighHandler().getComponents(), mShakeParameters,
                    getLocalSearch(), mLSParameters);
        } else {
            return String.format("%s \n Shake: Identity \n LS:%s (%s)", this.getClass()
                    .getSimpleName(), getNeighHandler().getComponents(), mLSParameters);
        }
    }

    @Override
    public void dispose() {
        mNeighHandler.dispose();
        if (mLocalSearch != null) {
            mLocalSearch.dispose();
            mLocalSearch = null;
        }
    }
}
