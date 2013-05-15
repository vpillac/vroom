/**
 * 
 */
package vroom.common.utilities.optimization;

import java.util.HashSet;
import java.util.Set;

import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * <code>SAAcceptanceCriterion</code> is an implementation of {@link IAcceptanceCriterion} based on a simulated
 * annealing acceptance criterion.
 * <p>
 * Creation date: May 18, 2011 - 4:40:02 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SAAcceptanceCriterion implements IAcceptanceCriterion {

    /** the accept already visited solution flag **/
    private final boolean mAcceptAlreadyVisited;

    /**
     * Getter for the accept already visited solution flag
     * 
     * @return <code>true</code> if the criterion should accept solutions that were already visited
     */
    public boolean isAcceptAlreadyVisited() {
        return this.mAcceptAlreadyVisited;
    }

    /** optimization sense **/
    private final OptimizationSense mOptimizationSense;

    /**
     * Getter for optimization sense
     * 
     * @return the optimization sense
     */
    public OptimizationSense getOptimizationSense() {
        return mOptimizationSense;
    }

    /** the random stream */
    private final RandomStream mRandomStream;

    /** The cooling rate */
    private final double       mCoolingRate;

    /** The current temperature */
    private double             mTemp;

    /** The initial temperature */
    private final double       mInitialTemp;

    /** The set of already visited solutions */
    private final Set<Integer> mVisitedSolutions;

    /**
     * Creates a new <code>SAAcceptanceCriterion</code>
     * 
     * @param optimizationSense
     *            the optimization sense
     * @param rndStream
     *            the random stream
     * @param coolingRate
     *            the SA cooling rate
     * @param initTemp
     *            the initial temperature
     * @param acceptRepeated
     *            <code>true</code> if the criterion should accept solutions that were already visited
     */
    public SAAcceptanceCriterion(OptimizationSense optimizationSense, RandomStream rndStream, double coolingRate,
            double initTemp, boolean acceptRepeated) {
        mOptimizationSense = optimizationSense;
        mRandomStream = rndStream;
        mCoolingRate = coolingRate;
        mTemp = initTemp;
        mInitialTemp = initTemp;
        mAcceptAlreadyVisited = acceptRepeated;
        mVisitedSolutions = mAcceptAlreadyVisited ? null : new HashSet<Integer>();
    }

    /**
     * Creates a new parametric <code>SAAcceptanceCriterion</code>.
     * <p>
     * The initial temperature <code>T<sub>0</sub></code> is chosen such as a solution with value <code>z*w</code> is
     * accepted with probability <code>p</code> <br/>
     * The cooling rate <code>c</code> is defined such as the temperature after <code>n</code> iterations is equal to
     * <code>alpha*T<sub>0</sub></code>
     * </p>
     * <p>
     * More precisely: <code>T<sub>0</sub> = -w*z/log(p)</code> and <code>c=alpha<sup>1/n</sup></code>
     * </p>
     * 
     * @param optimizationSense
     *            the optimization sense
     * @param rndStream
     *            the random stream
     * @param z
     *            the objective value of the initial solution
     * @param w
     *            the reference degradation of the objective function (between 0 and 1, e.g., 0.05 for 5%)
     * @param p
     *            the probability of accepting a solution with the reference degradation (between 0 and 1, e.g., 0.5 for
     *            50%)
     * @param n
     *            the expected number of iterations
     * @param alpha
     *            the desired proportion of initial temperature at the last iteration (between 0 and 1, e.g. 0.002 for
     *            0.2%)
     * @param acceptRepeated
     *            <code>true</code> if the criterion should accept solutions that were already visited
     */
    public SAAcceptanceCriterion(OptimizationSense optimizationSense, RandomStream rndStream, double z, double w,
            double p, int n, double alpha, boolean acceptRepeated) {
        this(optimizationSense, rndStream, Math.pow(alpha, 1d / n), -w * z / Math.log(p), acceptRepeated);
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.utilities.optimization.IAcceptanceCriterion#initialize()
     */
    @Override
    public void initialize() {
        mTemp = mInitialTemp;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.utilities.optimization.IAcceptanceCriterion#reset()
     */
    @Override
    public void reset() {
        mTemp = mInitialTemp;
    }

    /**
     * Check the improvement against the current temperature and updates the temperature
     * 
     * @param improvement
     *            the value of the improvement
     * @return <code>true</code> if a solution with the given <code>improvement</code> should be accepted
     */
    public boolean accept(double improvement) {
        boolean accept = mRandomStream.nextDouble() < Math.exp(improvement / mTemp);
        // Update temperature
        mTemp *= mCoolingRate;
        return accept;
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.common.utilities.optimization.IAcceptanceCriterion#accept(vroom.common.utilities.optimization.ISolution,
     * vroom.common.utilities.optimization.ISolution)
     */
    @Override
    public boolean accept(ISolution oldSolution, ISolution newSolution) {
        boolean accept = (isAcceptAlreadyVisited() || !mVisitedSolutions.contains(newSolution.hashCode()))
                && accept(mOptimizationSense.getImprovement(oldSolution.getObjectiveValue(),
                        newSolution.getObjectiveValue()));

        // We assume that the solution WILL be accepted
        if (!isAcceptAlreadyVisited() && accept)
            mVisitedSolutions.add(newSolution.hashCode());

        return accept;
    }

    @Override
    public double getImprovement(ISolution oldSolution, ISolution newSolution) {
        return mOptimizationSense.getImprovement(oldSolution.getObjectiveValue(), newSolution.getObjectiveValue());
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.common.utilities.optimization.IAcceptanceCriterion#accept(vroom.common.utilities.optimization.ISolution,
     * vroom.common.utilities.optimization.IMove)
     */
    @Override
    public boolean accept(ISolution solution, IMove move) {
        return accept(move.getImprovement());
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.common.utilities.optimization.IAcceptanceCriterion#accept(vroom.common.utilities.optimization.ISolution,
     * vroom.common.utilities.optimization.INeighborhood, vroom.common.utilities.optimization.IMove)
     */
    @Override
    public boolean accept(ISolution solution, INeighborhood<?, ?> neighborhood, IMove move) {
        return accept(move.getImprovement());
    }

    /**
     * Returns the current temperature
     * 
     * @return the current temperature
     */
    public double getTemperature() {
        return mTemp;
    }

    /**
     * Getter for <code>coolingRate</code>
     * 
     * @return the coolingRate
     */
    public double getCoolingRate() {
        return mCoolingRate;
    }

    /**
     * Getter for <code>initialTemp</code>
     * 
     * @return the initialTemp
     */
    public double getInitialTemp() {
        return mInitialTemp;
    }

    @Override
    public String toString() {
        return String.format("SA(t:%s t0:%s c:%s)", getTemperature(), getInitialTemp(), getCoolingRate());
    }

    @Override
    public SAAcceptanceCriterion clone() {
        SAAcceptanceCriterion clone = new SAAcceptanceCriterion(mOptimizationSense, mRandomStream, mCoolingRate,
                mInitialTemp, mAcceptAlreadyVisited);
        clone.mTemp = mTemp;
        if (mVisitedSolutions != null)
            clone.mVisitedSolutions.addAll(mVisitedSolutions);
        return clone;
    }
}
