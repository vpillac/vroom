/**
 * 
 */
package vroom.trsp.optimization.mTSPHeur;

import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.common.utilities.ssj.RandomSourceBase;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTourChecker;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTimeDelegate;
import vroom.trsp.optimization.localSearch.TRSPShift;

/**
 * <code>MTSPSubproblemSolver</code> is the class responsible for the optimization of the TSP subproblems resulting from
 * the decomposition of the initial TRSP.
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
    private final TRSPInstance mOriginalInstance;

    /**
     * Getter the original (complete) instance
     * 
     * @return the original instance
     */
    public TRSPInstance getOriginalInstance() {
        return this.mOriginalInstance;
    }

    /** the considered subproblem **/
    private TRSPInstance mSubproblem;

    /**
     * Sets the subproblem that will be solved by this solver
     * 
     * @param subproblem
     *            the subproblem to be solved
     */
    public void setSubproblem(TRSPInstance subproblem) {
        if (isRunning())
            throw new IllegalStateException("Cannot set the solver while solver is running");

        mSubproblem = subproblem;
    }

    /**
     * Getter for the considered subproblem
     * 
     * @return the subproblem
     */
    public TRSPInstance getSubproblem() {
        return this.mSubproblem;
    }

    private final MTSPRandomInitialization mInitialization;

    public MTSPSubproblemSolver(TRSPInstance originalInstance) {
        super();

        mOriginalInstance = originalInstance;

        mInitialization = new MTSPRandomInitialization(originalInstance, new TRSPWorkingTimeDelegate());
    }

    /** the solution found by this solver **/
    private TRSPTour mSolution;

    /**
     * Getter for the solution found by this solver
     * 
     * @return the current solution
     */
    public TRSPTour getSolution() {
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
        String check = TRSPTourChecker.checkTour(mSolution, true);
        System.out.printf(" > Init sol      (maxLateness: %s)\n", mSolution.getMaxLateness());
        System.out.printf(" > Incoherencies: %s\n", check);
        System.out.println(mSolution);
        // TRSPShift.CHECK_SOLUTIONS_AFTER_MOVE = true;
        TRSPShift shift = new TRSPShift();
        shift.localSearch(mSolution, new SimpleParameters(LSStrategy.DET_FIRST_IMPROVEMENT, Integer.MAX_VALUE,
                Integer.MAX_VALUE));
        System.out.printf(" > 1-shift sol   (maxLateness: %s)\n", mSolution.getMaxLateness());
        System.out.println(mSolution);

        mRunning = false;
    }
}
