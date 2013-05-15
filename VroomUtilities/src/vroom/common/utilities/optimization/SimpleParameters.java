package vroom.common.utilities.optimization;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.ssj.RandomSourceBase;

/**
 * <code>SimpleParameters</code> is an implementation of {@link IParameters} with containing a maximum time and number
 * of iterations, as well as a flag defining whether the exploration should be complete or accept the first improvement.
 * <p>
 * Creation date: Apr 27, 2010 - 11:10:55 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SimpleParameters extends RandomSourceBase implements IParameters {

    /** A default value for first improvement with unlimited time and iterations */
    public static final SimpleParameters FIRST_IMPROVEMENT = new SimpleParameters(
                                                                   LSStrategy.DET_FIRST_IMPROVEMENT,
                                                                   Long.MAX_VALUE,
                                                                   Integer.MAX_VALUE, 0);
    /** A default value for best improvement with unlimited time and iterations */
    public static final SimpleParameters BEST_IMPROVEMENT  = new SimpleParameters(
                                                                   LSStrategy.DET_BEST_IMPROVEMENT,
                                                                   Long.MAX_VALUE,
                                                                   Integer.MAX_VALUE, 0);

    /** A default value for random perturbation with unlimited time and iterations */
    public static final SimpleParameters PERTURBATION      = new SimpleParameters(
                                                                   LSStrategy.RND_NON_IMPROVING,
                                                                   Long.MAX_VALUE,
                                                                   Integer.MAX_VALUE, 0);

    /**
     * The strategy to be used
     */
    private final LSStrategy             mStrategy;

    @Override
    public LSStrategy getStrategy() {
        return mStrategy;
    }

    /** The stopping criterion associated with this parameter set */
    private IStoppingCriterion mStoppingCriterion;

    /**
     * Returns the stopping criterion associated with this parameter set
     * 
     * @return the stopping criterion associated with this parameter set
     */
    @Override
    public IStoppingCriterion getStoppingCriterion() {
        return mStoppingCriterion;
    }

    /**
     * Setter the stopping criterion associated with this parameter set
     * 
     * @param stoppingCriterion
     *            the stopping criterion to set
     * @throws IllegalArgumentException
     *             if {@code  stoppingCriterion} is <code>null</code>
     */
    public void setStoppingCriterion(IStoppingCriterion stoppingCriterion) {
        if (stoppingCriterion == null)
            throw new IllegalArgumentException("The stopping criterion cannot be null");
        mStoppingCriterion = stoppingCriterion;
    }

    @Override
    public long getMaxTime() {
        return mStoppingCriterion.getMaxTime();
    }

    @Override
    public int getMaxIterations() {
        return mStoppingCriterion.getMaxIterations();
    }

    @Override
    public boolean acceptFirstImprovement() {
        return getStrategy().acceptFirstImprovement();
    }

    /**
     * Getter for A flag stating whether the search should accept non-improving moves
     * 
     * @return the value of acceptNonImproving
     */
    @Override
    public boolean acceptNonImproving() {
        return getStrategy().acceptNonImproving();
    }

    @Override
    public boolean randomize() {
        return getStrategy().randomized();
    }

    /** the acceptance criterion to be used, default is {@link ImprovingAcceptanceCriterion} **/
    private IAcceptanceCriterion mAcceptanceCriterion;

    /**
     * Getter for the acceptance criterion to be used, default is {@link ImprovingAcceptanceCriterion}
     * 
     * @return the value of acceptanceCriterion
     */
    @Override
    public IAcceptanceCriterion getAcceptanceCriterion() {
        return this.mAcceptanceCriterion;
    }

    /**
     * Setter for the acceptance criterion to be used, default is {@link ImprovingAcceptanceCriterion}
     * 
     * @param acceptanceCriterion
     *            the value to be set for the acceptance criterion to be used, default is
     *            {@link ImprovingAcceptanceCriterion}
     */
    public void setAcceptanceCriterion(IAcceptanceCriterion acceptanceCriterion) {
        this.mAcceptanceCriterion = acceptanceCriterion;
    }

    /**
     * Creates a new <code>SimpleParameters</code>
     * 
     * @param maxTime
     *            the maximum time (in ms)
     * @param maxIt
     *            the maximum number of iterations
     * @param acceptFirstImprovement
     *            a flag stating whether the research should be thorough ( <code>false</code>) or accept the first
     *            improving move ( <code>true</code>)
     * @param randonized
     *            the randomized flag, <code>true</code> if the procedure should be randomized
     * @param acceptNonImproving
     *            <code>true</code> if non improving moves should be accepted
     * @param rndStream
     *            the random stream to use (can be <code>null</code>)
     */
    public SimpleParameters(long maxTime, int maxIt, boolean acceptFirstImprovement,
            boolean randonized, boolean acceptNonImproving, RandomStream rndStream) {
        this(LSStrategy.getStrategy(randonized, acceptFirstImprovement, acceptNonImproving),
                maxTime, maxIt, rndStream);
    }

    /**
     * Creates a new <code>SimpleParameters</code>
     * 
     * @param strategy
     *            the local search strategy
     * @param maxTime
     *            the maximum time (in ms)
     * @param maxIt
     *            the maximum number of iterations
     */
    public SimpleParameters(LSStrategy strategy, long maxTime, int maxIt) {
        this(strategy, maxTime, maxIt, null);
    }

    /**
     * Creates a new <code>SimpleParameters</code>
     * 
     * @param strategy
     *            the local search strategy
     * @param maxTime
     *            the maximum time (in ms)
     * @param maxIt
     *            the maximum number of iterations
     * @param seed
     *            a seed for the random stream
     */
    public SimpleParameters(LSStrategy strategy, long maxTime, int maxIt, long seed) {
        this(strategy, maxTime, maxIt, null);
        setMRG32k3aRndStream(seed, "simpleParams");
    }

    /**
     * Creates a new <code>SimpleParameters</code>
     * 
     * @param strategy
     *            the local search strategy
     * @param maxTime
     *            the maximum time (in ms)
     * @param maxIt
     *            the maximum number of iterations
     * @param rndStream
     *            the random stream to use (can be <code>null</code>)
     */
    public SimpleParameters(LSStrategy strategy, long maxTime, int maxIt, RandomStream randomStream) {
        super(randomStream);

        mStrategy = strategy;
        mStoppingCriterion = new SimpleStoppingCriterion(maxTime, maxIt);
        mAcceptanceCriterion = new ImprovingAcceptanceCriterion(null);
    }

    /**
     * @return <code>new SimpleParameters(Long.MAX_VALUE, Integer.MAX_VALUE, false, false)</code>
     */
    public static IParameters newUnlimitedThoroughDetParameters() {
        return BEST_IMPROVEMENT;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("maxT:%s,maxIt:%s,%s,%s", getMaxTime() < Long.MAX_VALUE ? getMaxTime()
                : "none", getMaxIterations() < Integer.MAX_VALUE ? getMaxIterations() : "none",
                acceptNonImproving() ? "NI" : acceptFirstImprovement() ? "FI" : "BI",
                randomize() ? "rnd" : "det");
    }
}
