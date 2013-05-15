package vroom.optimization.pl;

import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.utilities.lp.SolverStatus;

public interface IVRPSolver {

    /**
     * @return <code>true</code> if an instance has been properly loaded and the model initialized
     */
    public boolean isInitialized();

    /**
     * Getter for <code>instance</code>
     * 
     * @return the instance
     */
    public IVRPInstance getInstance();

    /**
     * Sets a time limit for the solve procedure.
     * 
     * @param sec
     *            the maximum time available to solve the model in seconds
     */
    public void setTimeLimit(int sec);

    /**
     * Reset this solver by restoring its initial state
     */
    public void reset();

    /**
     * Read an {@link IVRPInstance} and creates the associated model.
     * 
     * @param instance
     *            the instance to be read
     */
    public void readInstance(IVRPInstance instance) throws Exception;

    /**
     * @return a status code for the solver final state
     */
    public SolverStatus solve() throws Exception;

    /**
     * Retrieves the current solution
     * 
     * @return the current solution
     */
    public IVRPSolution<? extends IRoute<?>> getSolution();

    /**
     * Retrieves the current objective value
     * 
     * @return the objective value of the current solution
     */
    public double getObjectiveValue();

    /**
     * Prints the current solution
     */
    public void printSolution(boolean printVariables);

    /**
     * Solving time
     * 
     * @return the time spent on the solve procedure in milliseconds
     */
    public double getSolveTime();

    /**
     * Check whether or not the current solution is feasible
     * 
     * @return <code>true</code> if the current solution is feasible
     */
    public boolean isSolutionFeasible();

}