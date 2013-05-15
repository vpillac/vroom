/**
 *
 */
package vroom.common.heuristics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.Constants;
import vroom.common.utilities.RouletteWheel;
import vroom.common.utilities.optimization.IComponentHandler;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.ISolution;

/**
 * <code>GenericComponentHandler</code> is a general purpose implementation of {@link INeighborhoodHandler}
 * <p>
 * Creation date: 2 juil. 2010 - 20:17:03
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GenericNeighborhoodHandler<S extends ISolution> implements
        IComponentHandler<INeighborhood<S, ?>> {

    public static enum Strategy {
        /** Sequential component exploration (default) */
        SEQUENTIAL,
        /** Random wheel selection based on improvement frequency over all iterations */
        FREQUENCY_BASED,
        /** Random wheel selection based on improvement frequency for a given component */
        SELF_FREQUENCY_BASED,
        /** Random wheel selection based on average efficiency (ratio improvement/time) */
        EFFICIENCY_BASED,
        /** Purely random selection */
        RANDOM
    };

    /** The selection strategy used by this handler **/
    private Strategy mStrategy;

    /**
     * Getter for the selection strategy used by this handler
     * 
     * @return the value of strategy
     */
    public Strategy getStrategy() {
        return this.mStrategy;
    }

    /**
     * Setter for the selection strategy used by this handler
     * 
     * @param strategy
     *            the value to be set for strategy
     */
    public void setStrategy(Strategy strategy) {
        this.mStrategy = strategy;
    }

    /** The list of components */
    private final List<INeighborhood<S, ?>> mComponents;

    @Override
    public List<INeighborhood<S, ?>> getComponents() {
        return mComponents;
    }

    protected int                            mCurrentNeighIndex;
    protected INeighborhood<S, ?>            mCurrentComponent;
    protected final Set<INeighborhood<S, ?>> mExploredComponents;
    protected final Set<INeighborhood<S, ?>> mUnexploredComponents;

    /**
     * Return the set of unexplored components
     * 
     * @return a set containing the unexplored components
     * @see #getExploredComponents()
     */
    public Set<INeighborhood<S, ?>> getUnexploredComponents() {
        return new HashSet<INeighborhood<S, ?>>(mUnexploredComponents);
    }

    /**
     * Return the set of explored components
     * 
     * @return a set containing the explored components
     * @see #getUnexploredComponents()
     */
    public Set<INeighborhood<S, ?>> getExploredComponents() {
        return new HashSet<INeighborhood<S, ?>>(mExploredComponents);
    }

    /** Number of times an improving solution was found in the component */
    protected final Map<INeighborhood<S, ?>, Integer> mSuccessCount;
    /** Number of times the component was explored */
    protected final Map<INeighborhood<S, ?>, Integer> mExplorationCount;
    /** Average efficiency metric when exploring the component */
    protected final Map<INeighborhood<S, ?>, Double>  mEfficiencyMetric;
    protected int                                     mIterationCount;

    protected int                                     mInitialFrequency        = 1;
    protected double                                  mInitialPerformance      = 1;

    private final RandomStream                        mRandom;

    // Reset strategy
    protected int                                     mResetIterationThreshold = Integer.MAX_VALUE;
    protected double                                  mResetProbaThreshold     = 0;

    /**
     * Set a reset strategy.
     * <p>
     * The component memory will be {@linkplain #reset() reset} whenever one of the two following criterion is met:
     * <ul>
     * <li>The number of iterations exceeds <code>iterationThreshold</code></li>
     * <li>The evaluation of selecting a given components is less than <code>probaThreshold*equiProba</code> where
     * <code>equiProba=1/componentCount</code> is the evaluation of selecting any component when no history is available
     * </li>
     * </ul>
     * </p>
     * 
     * @param iterationThreshold
     *            a reset threshold on the number of iterations
     * @param probaThreshold
     *            a reset threshold based on the evaluation of selecting any component (between 0 and 1)
     */
    public void setResetStrategy(int iterationThreshold, double probaThreshold) {
        if (probaThreshold < 0 || probaThreshold > 1) {
            throw new IllegalArgumentException(
                    "probaThreshold has to be between 0 and 1 (inclusive) : " + probaThreshold);
        }
        if (iterationThreshold < 0) {
            throw new IllegalArgumentException("iterationThreshold has to be positive : "
                    + iterationThreshold);
        }

        mResetIterationThreshold = iterationThreshold;
        mResetProbaThreshold = probaThreshold;
    }

    /**
     * Creates a default <code>GenericComponentHandler</code> with a {@linkplain Strategy#SEQUENTIAL sequential}
     * strategy
     * 
     * @param components
     *            a list of components
     * @param rndStream
     *            the random stream to be used in this component selection
     */
    public GenericNeighborhoodHandler(List<INeighborhood<S, ?>> components, RandomStream rndStream) {
        this(Strategy.SEQUENTIAL, components, rndStream);
    }

    /**
     * Creates a new <code>GenericComponentHandler</code>
     * 
     * @param strategy
     *            an initial component selection strategy
     * @param components
     *            a list of components
     * @param rndStream
     *            the random stream to be used in this component selection
     * @param parentVNS
     *            the parent VNS
     */
    public GenericNeighborhoodHandler(Strategy strategy, List<INeighborhood<S, ?>> components,
            RandomStream rndStream) {
        super();
        this.mStrategy = strategy;
        this.mComponents = new LinkedList<INeighborhood<S, ?>>(components);
        this.mUnexploredComponents = new HashSet<INeighborhood<S, ?>>();
        this.mExploredComponents = new HashSet<INeighborhood<S, ?>>();
        this.mSuccessCount = new HashMap<INeighborhood<S, ?>, Integer>();
        this.mEfficiencyMetric = new HashMap<INeighborhood<S, ?>, Double>();
        this.mExplorationCount = new HashMap<INeighborhood<S, ?>, Integer>();
        this.mRandom = rndStream;
    }

    @Override
    public void initialize(IInstance instance) {
        mIterationCount = 1;

        for (INeighborhood<S, ?> n : getComponents()) {
            if (!mSuccessCount.containsKey(n)) {
                mSuccessCount.put(n, mInitialFrequency * mIterationCount);
            }
            if (!mEfficiencyMetric.containsKey(n)) {
                mEfficiencyMetric.put(n, mInitialPerformance);
            }
            if (!mExplorationCount.containsKey(n)) {
                mExplorationCount.put(n, 1);
            }
        }

        mUnexploredComponents.addAll(getComponents());
        mExploredComponents.clear();
        mCurrentComponent = null;
        mCurrentNeighIndex = 0;
    }

    @Override
    public boolean updateStats(INeighborhood<S, ?> currentComponent, double improvement, double time,
            int iteration, Outcome state) {

        // Prevent zero division
        time = Math.max(time, 1);
        // Setup the initial performance metric
        if (mIterationCount == 1 || mInitialPerformance == 0) {
            mInitialPerformance = Math.max(improvement / time, 0);
        }

        // Initialize success count
        if (!mSuccessCount.containsKey(currentComponent)) {
            mSuccessCount.put(currentComponent, mInitialFrequency * mIterationCount);
        }
        // Initialize the self frequency
        if (!mExplorationCount.containsKey(currentComponent)) {
            mExplorationCount.put(currentComponent, 1);
        }
        // Initialize efficiency metric
        if (!mEfficiencyMetric.containsKey(currentComponent) || mIterationCount == 0) {
            mEfficiencyMetric.put(currentComponent, mInitialPerformance);
        }

        // Update number of exploration
        int numEx = mExplorationCount.get(currentComponent) + 1;
        mExplorationCount.put(currentComponent, numEx);

        // Update efficiency
        double current = mEfficiencyMetric.get(currentComponent);
        mEfficiencyMetric.put(currentComponent, (current * (numEx - 1) + improvement / time)
                / numEx);

        if (Constants.isStrictlyPositive(improvement)) {
            // Update success count
            mSuccessCount.put(currentComponent, mSuccessCount.get(currentComponent) + 1);

            // Reset current component to the first (used for SEQUENTIAL strategy)
            mCurrentNeighIndex = 0;

            // Reset the list of unexplored component
            mUnexploredComponents.addAll(getComponents());
            mExploredComponents.clear();
        } else {
            // Mark the component as explored
            mUnexploredComponents.remove(currentComponent);
            mExploredComponents.add(currentComponent);

            // Move to the next component (used for SEQUENTIAL strategy)
            mCurrentNeighIndex++;
        }

        mIterationCount++;

        return true;
    }

    @Override
    public void reset() {
        mSuccessCount.clear();
        mExplorationCount.clear();
        mEfficiencyMetric.clear();
        mUnexploredComponents.addAll(getComponents());
        mExploredComponents.clear();
        mIterationCount = 1;
        mCurrentNeighIndex = 0;
        mCurrentComponent = null;
    }

    /**
     * Reset the component memory
     */
    protected void resetMemory() {
        for (INeighborhood<S, ?> n : getComponents()) {
            mSuccessCount.put(n, 1);
            mExplorationCount.put(n, 1);
            mEfficiencyMetric.put(n, mInitialPerformance);
        }
    }

    @Override
    public boolean isCompletelyExplored() {
        switch (getStrategy()) {
        case SEQUENTIAL:
            return mCurrentNeighIndex >= getComponents().size();
        case EFFICIENCY_BASED:
        case SELF_FREQUENCY_BASED:
        case FREQUENCY_BASED:
        case RANDOM:
            return mUnexploredComponents.isEmpty();

        default:
            throw new IllegalStateException("Unsupported strategy :" + getStrategy());
        }
    }

    @Override
    public INeighborhood<S, ?> nextComponent() {
        INeighborhood<S, ?> next = null;

        if (mIterationCount % mResetIterationThreshold > mResetIterationThreshold) {
            resetMemory();
        }

        if (getStrategy() == Strategy.SEQUENTIAL) {
            if (mCurrentNeighIndex < getComponents().size()) {
                next = getComponents().get(mCurrentNeighIndex);
            }
        } else {
            // Generate the evaluation wheel
            RouletteWheel<INeighborhood<S, ?>> wheel = new RouletteWheel<INeighborhood<S, ?>>();
            double acc = 0;
            for (INeighborhood<S, ?> n : mUnexploredComponents) {
                double eval = evalComponent(n);
                if (eval < mResetProbaThreshold / getComponents().size()) {
                    resetMemory();
                    eval = 1d / getComponents().size();
                }
                wheel.add(n, eval);
                acc += eval;
            }

            next = wheel.drawObject(mRandom, false);
        }

        mCurrentComponent = next;
        return mCurrentComponent;
    }

    /**
     * Evaluation of a component for the selection wheel
     * 
     * @param component
     * @return a value representing the performance of the component
     */
    protected double evalComponent(INeighborhood<S, ?> component) {
        switch (getStrategy()) {
        case EFFICIENCY_BASED:
            return mEfficiencyMetric.get(component);
        case FREQUENCY_BASED:
            return mSuccessCount.get(component).doubleValue() / mIterationCount;
        case SELF_FREQUENCY_BASED:
            return mSuccessCount.get(component).doubleValue() / mExplorationCount.get(component);
        case RANDOM:
            return 1;
        default:
            throw new IllegalStateException("Unsupported strategy: " + getStrategy());
        }
    }

    @Override
    public String toString() {

        RouletteWheel<INeighborhood<S, ?>> freq = new RouletteWheel<INeighborhood<S, ?>>();
        RouletteWheel<INeighborhood<S, ?>> selfFreq = new RouletteWheel<INeighborhood<S, ?>>();
        RouletteWheel<INeighborhood<S, ?>> eff = new RouletteWheel<INeighborhood<S, ?>>();
        for (INeighborhood<S, ?> n : getComponents()) {
            double suc = mSuccessCount.get(n);
            freq.add(n, suc / mIterationCount);
            selfFreq.add(n, suc / mExplorationCount.get(n));
            eff.add(n, mEfficiencyMetric.get(n));
        }

        return String.format("Unex. Neigh:%s Freq:%s SelfFreq:%s Eff:%s", mUnexploredComponents,
                freq, selfFreq, eff);
    }

    @Override
    public void dispose() {
        for (INeighborhood<S, ?> comp : mComponents) {
            comp.dispose();
        }
        mExploredComponents.clear();
        mUnexploredComponents.clear();
        mSuccessCount.clear();
        mExplorationCount.clear();
        mEfficiencyMetric.clear();
        mCurrentComponent = null;
        mComponents.clear();
    }

    @Override
    public double getWeight(INeighborhood<S, ?> component) {
        switch (mStrategy) {
        case SEQUENTIAL:
            return getComponents().get(mCurrentNeighIndex) == component ? 1 : 0;
        case FREQUENCY_BASED:
        case SELF_FREQUENCY_BASED:
        case EFFICIENCY_BASED:
            return evalComponent(component);
        case RANDOM:
            return mUnexploredComponents.contains(component) ? 1d / mUnexploredComponents.size()
                    : 0;
        default:
            return 0;
        }
    }
}
