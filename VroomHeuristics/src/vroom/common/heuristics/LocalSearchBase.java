/**
 *
 */
package vroom.common.heuristics;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.utils.HeuristicsLogging;
import vroom.common.utilities.IDisposable;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Stopwatch.ReadOnlyStopwatch;
import vroom.common.utilities.dataModel.ISolutionChecker;
import vroom.common.utilities.optimization.IAcceptanceCriterion;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.IStoppingCriterion;
import vroom.common.utilities.optimization.ImprovingAcceptanceCriterion;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.common.utilities.ssj.RandomSourceBase;

/**
 * <code>LocalSearchBase</code> is a base class for common local search based procedures.
 * <p>
 * Creation date: 11 juil. 2010 - 19:48:35
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class LocalSearchBase<S extends ISolution> extends RandomSourceBase implements
        ILocalSearch<S>, IDisposable {

    public static final String   MAIN_RANDOM_STREAM = "LSRndStream";

    /** an acceptance criterion **/
    private IAcceptanceCriterion mAcceptanceCriterion;

    /**
     * Getter for an acceptance criterion
     * 
     * @return the acceptance criterion used in this local search
     */
    public IAcceptanceCriterion getAcceptanceCriterion() {
        return this.mAcceptanceCriterion;
    }

    /**
     * Setter for an acceptance criterion
     * 
     * @param acceptanceCriterion
     *            the value to be set for acceptanceCriterion
     */
    public void setAcceptanceCriterion(IAcceptanceCriterion acceptanceCriterion) {
        this.mAcceptanceCriterion = acceptanceCriterion;
    }

    /** a stopping criterion **/
    private IStoppingCriterion mStoppingCriterion;

    /**
     * Getter for a stopping criterion
     * 
     * @return the value of stoppingCriterion
     */
    public IStoppingCriterion getStoppingCriterion() {
        return this.mStoppingCriterion;
    }

    /**
     * Setter for a stopping criterion
     * 
     * @param stoppingCriterion
     *            the value to be set for a stopping criterion
     */
    protected void setStoppingCriterion(IStoppingCriterion stoppingCriterion) {
        this.mStoppingCriterion = stoppingCriterion;
    }

    /** the optimization sense for this local search **/
    private final OptimizationSense mOptimizationSense;

    /**
     * Getter for the optimization sense for this local search
     * 
     * @return the value of the optimization sense
     */
    public OptimizationSense getOptimizationSense() {
        return this.mOptimizationSense;
    }

    /** <code>true</code> if the local search algorithm is currently running, <code>false</code> otherwise */
    private boolean mRunning;

    /**
     * Sets the running flag to <code>true</code>
     * 
     * @throws IllegalStateException
     *             if the local search is already running
     */
    protected void setRunning() {
        if (mRunning) {
            throw new IllegalStateException("The local search is already running");
        }
        mRunning = true;
        getMainTimer().reset();
        getMainTimer().start();
    }

    /**
     * Sets the running flag to <code>false</code>
     */
    protected void setStopped() {
        mRunning = false;
        if (getMainTimer().isStarted() && !getMainTimer().isStopped())
            getMainTimer().stop();
    }

    /**
     * Returns <code>true</code> if the local search algorithm is currently running, <code>false</code> otherwise
     * 
     * @return <code>true</code> if the local search algorithm is currently running, <code>false</code> otherwise
     */
    protected boolean isRunning() {
        return mRunning;
    }

    /** the main timer **/
    private final Stopwatch mTimer;

    /**
     * Getter for the main timer
     * 
     * @return the main timer
     */
    public ReadOnlyStopwatch getTimer() {
        return this.mTimer.getReadOnlyStopwatch();
    }

    /**
     * Getter for the main timer, for subclass use only
     * 
     * @return the main timer
     */
    protected Stopwatch getMainTimer() {
        return this.mTimer;
    }

    private static boolean sCheckSolutionAfterMove = false;

    /**
     * Setter for <code>checkSolutionAfterMove</code>
     * 
     * @param checkSolutionAfterMove
     *            the checkSolutionAfterMove to set
     */
    public static void setCheckSolutionAfterMove(boolean checkSolutionAfterMove) {
        sCheckSolutionAfterMove = checkSolutionAfterMove;
        if (checkSolutionAfterMove) {
            HeuristicsLogging
                    .getProcedureLogger()
                    .warn("LocalSearchBase.CheckSolutionAfterMove is set to true, set to false to increase performance (set in %s)",
                            Thread.currentThread().getStackTrace()[2]);
        }
    }

    /**
     * Getter for <code>checkSolutionAfterMove</code>
     * 
     * @return the checkSolutionAfterMove
     */
    public static boolean isCheckSolutionAfterMove() {
        return sCheckSolutionAfterMove;
    }

    private ISolutionChecker<S> mSolutionChecker = null;

    /**
     * Getter for the solution checker used in this local search
     * 
     * @return the solution checker used in this local search
     */
    public ISolutionChecker<S> getSolutionChecker() {
        return mSolutionChecker;
    }

    /**
     * Setter for the solution checker used in this local search
     * 
     * @param solutionChecker
     *            the solution checker used in this local search
     */
    public void setSolutionChecker(ISolutionChecker<S> solutionChecker) {
        mSolutionChecker = solutionChecker;
    }

    /**
     * Check a solution against the defined solution checker if any, and only if the {@link #isCheckSolutionAfterMove()
     * checkSolutionAfterMove} flag is set to <code>true</code>
     * 
     * @param solution
     *            the solution to be checked
     * @return a string describing the infeasibility of <code>solution</code>, or an empty string if the solution is
     *         feasible
     * @see ISolutionChecker#checkSolution(ISolution)
     */
    protected String checkSolution(S solution) {
        if (isCheckSolutionAfterMove() && getSolutionChecker() != null) {
            return getSolutionChecker().checkSolution(solution);
        } else {
            return "";
        }
    }

    /**
     * Creates a new <code>LocalSearchBase</code>
     * 
     * @param optimizationSense
     *            the sense of optimization
     * @see RandomSourceBase#RandomSourceBase()
     */
    protected LocalSearchBase(OptimizationSense optimizationSense) {
        super();

        mTimer = new Stopwatch();
        mOptimizationSense = optimizationSense;
        setAcceptanceCriterion(new ImprovingAcceptanceCriterion(optimizationSense));
    }

    /**
     * Creates a new <code>LocalSearchBase</code>
     * 
     * @param optimizationSense
     *            the sense of optimization
     * @param rndStream
     *            the random stream that will be used in this local search
     * @see RandomSourceBase#RandomSourceBase(RandomStream)
     */
    protected LocalSearchBase(OptimizationSense optimizationSense, RandomStream rndStream) {
        super(rndStream);

        mTimer = new Stopwatch();
        mOptimizationSense = optimizationSense;
        setAcceptanceCriterion(new ImprovingAcceptanceCriterion(optimizationSense));
    }

}
