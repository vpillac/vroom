/**
 * 
 */
package vroom.common.utilities.optimization;

import java.util.concurrent.atomic.AtomicInteger;

import vroom.common.utilities.Stopwatch;

/**
 * <code>SimpleStoppingCriterion</code> is an implementation of {@link IStoppingCriterion} based on a maximum number of
 * iterations and maximum running time.
 * <p/>
 * Method {@link #reset()} should be called first to activate the timer.
 * <p>
 * Creation date: 1 mai 2010 - 16:27:17
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SimpleStoppingCriterion implements IStoppingCriterion {

    /**
     * Getter for maxTime : The maximum execution time
     * 
     * @return the value of maxTime
     */
    @Override
    public long getMaxTime() {
        return mTimer.getTimeout();
    }

    /**
     * Setter for maxTime : The maximum execution time
     * 
     * @param maxTime
     *            the value to be set for maxTime
     */
    public void setMaxTime(long maxTime) {
        mTimer.setTimout(maxTime);
    }

    /** The maximum number of iteration **/
    private int mMaxIterations;

    /**
     * Getter for maxIterations : The maximum number of iteration
     * 
     * @return the value of maxIterations
     */
    @Override
    public int getMaxIterations() {
        return mMaxIterations;
    }

    /**
     * Setter for maxIterations : The maximum number of iteration
     * 
     * @param maxIterations
     *            the value to be set for maxIterations
     */
    public void setMaxIterations(int maxIterations) {
        mMaxIterations = maxIterations;
    }

    private final Stopwatch     mTimer;
    private final AtomicInteger mItCount;

    /**
     * Creates a new <code>SimpleStoppingCriterion</code>
     * 
     * @param maxTime
     * @param maxIt
     */
    public SimpleStoppingCriterion(long maxTime, int maxIt) {
        mTimer = new Stopwatch(maxTime);
        mItCount = new AtomicInteger();

        setMaxTime(maxTime);
        setMaxIterations(maxIt);
    }

    /**
     * Creates a clone of {@code  master}
     * 
     * @param master
     */
    private SimpleStoppingCriterion(SimpleStoppingCriterion master) {
        mTimer = master.mTimer.clone();
        mItCount = master.mItCount;
        mMaxIterations = master.mMaxIterations;
    }

    /*
     * (non-Javadoc)
     * @see edu.uniandes.copa.heuristics.IStoppingCondition#isStopConditionMet()
     */
    @Override
    public boolean isStopCriterionMet() {
        return mTimer.hasTimedOut() || mItCount.get() >= getMaxIterations() || Thread.interrupted();
    }

    @Override
    public void update(int iterations, Object... args) {
        if (!mTimer.isStarted())
            throw new IllegalStateException("Stopping criterion is not initialized");

        mItCount.addAndGet(iterations);
    }

    /*
     * (non-Javadoc)
     * @see edu.uniandes.copa.heuristics.IStoppingCondition#update(java.lang.Object)
     */
    @Override
    public void update(Object... args) {
        this.update(1, args);
    }

    /*
     * (non-Javadoc)
     * @see edu.uniandes.copa.heuristics.IStoppingCondition#reset()
     */
    @Override
    public void reset() {
        mTimer.reset();
        mItCount.set(0);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Timer:%s Iterations:%s/%s", mTimer, mItCount,
                getMaxIterations() != Integer.MAX_VALUE ? getMaxIterations() : "-");
    }

    /*
     * (non-Javadoc)
     * @see edu.uniandes.copa.heuristics.IStoppingCondition#init()
     */
    @Override
    public void init() {
        mTimer.start();
    }

    @Override
    public int getIterationCount() {
        return mItCount.get();
    }

    @Override
    public double getCurrentTime() {
        return mTimer.readTimeMS();
    }

    @Override
    public IStoppingCriterion clone() {
        SimpleStoppingCriterion clone = new SimpleStoppingCriterion(this);
        return clone;
    }

}
