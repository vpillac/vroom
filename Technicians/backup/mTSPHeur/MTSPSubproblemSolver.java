/**
 * 
 */
package vroom..optimization.mTSPHeur;

import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.common.utilities.ssj.RandomSourceBase;
import vroom..datamodel.Instance;
import vroom..datamodel.Tour;
import vroom..datamodel.TourChecker;
import vroom..datamodel.costDelegates.WorkingTimeDelegate;
import vroom..optimization.localSearch.Shift;

/**
 * <code>MTSPSubproblemSolver</code> is the class responsible for the optimization of the TSP subproblems resulting from
 * the decomposition of the initial .
 * <p>
 * Creation date: Feb 17, 2011 - 11:02:43 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MTSPSubproblemSolver extends RandomSourceBase implements Runnable {

    /** A flag set to <code>true</code> if the solver is running */
    private boolean mRunning;

    /**
     * A flag set to <code>true</code> if the solver is running
     * 
     * @return <code>true</code> if the solver is running
     */
    public boolean isRunning() {
        return mRunning;
    }

    /** the original (complete) instance **/
    private final Instance mOriginalInstance;

    /**
     * Getter the original (complete) instance
     * 
     * @return the original instance
     */
    public Instance getOriginalInstance() {
        return this.mOriginalInstance;
    }

    /** the considered subproblem **/
    private Instance mSubproblem;

    /**
     * Sets the subproblem that will be solved by this solver
     * 
     * @param subproblem
     *            the subproblem to be solved
     */
    public void setSubproblem(Instance subproblem) {
        if (isRunning())
            throw new IllegalStateException("Cannot set the solver while solver is running");

        mSubproblem = subproblem;
    }

    /**
     * Getter for the considered subproblem
     * 
     * @return the subproblem
     */
    public Instance getSubproblem() {
        return this.mSubproblem;
    }

    private final MTSPRandomInitialization mInitialization;

    public MTSPSubproblemSolver(Instance originalInstance) {
        super();

        mOriginalInstance = originalInstance;

        mInitialization = new MTSPRandomInitialization(originalInstance, new WorkingTimeDelegate());
    }

    /** the solution found by this solver **/
    private Tour mSolution;

    /**
     * Getter for the solution found by this solver
     * 
     * @return the current solution
     */
    public Tour getSolution() {
        return this.mSolution;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        if (isRunning())
            throw new IllegalStateException("The solver is already running");
        mRunning = true;

        mSolution = mInitialization.newSolution(mSolution, getSubproblem(), getRandomStream());
        mSolution.setAutoUpdated(true);
        String check = TourChecker.checkTour(mSolution, true);
        System.out.printf(" > Init sol      (maxLateness: %s)\n", mSolution.getMaxLateness());
        System.out.printf(" > Incoherencies: %s\n", check);
        System.out.println(mSolution);
        // Shift.CHECK_SOLUTIONS_AFTER_MOVE = true;
        Shift shift = new Shift();
        shift.localSearch(mSolution, new SimpleParameters(LSStrategy.DET_FIRST_IMPROVEMENT, Integer.MAX_VALUE,
                Integer.MAX_VALUE));
        System.out.printf(" > 1-shift sol   (maxLateness: %s)\n", mSolution.getMaxLateness());
        System.out.println(mSolution);

        mRunning = false;
    }
}
