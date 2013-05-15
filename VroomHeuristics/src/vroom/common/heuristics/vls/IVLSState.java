package vroom.common.heuristics.vls;

import vroom.common.utilities.optimization.ISolution;

/**
 * <code>IVLSState</code> is an interface for classes describing the current state of the VLS.
 * <p/>
 * Implementations should declare a constructor taking and instance of {@link VersatileLocalSearch} as unique argument
 * <p>
 * Creation date: Apr 26, 2010 - 2:14:37 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface IVLSState<S extends ISolution> {

    /**
     * Method called when a mSolution has been accepted in the specified phase.
     * 
     * @param mSolution
     *            the accepted mSolution
     * @param phase
     *            the phase in which the mSolution was accepted
     */
    public void solutionAccepted(S solution, VLSPhase phase);

    /**
     * Method called when a mSolution has been rejected in the specified phase.
     * 
     * @param mSolution
     *            the rejected mSolution
     * @param phase
     *            the phase in which the mSolution was accepted
     */
    public void solutionRejected(S solution, VLSPhase phase);

    // /**
    // * Setter for the best mSolution found so far in the given phase
    // *
    // * @param phase the phase for which the best mSolution will be set
    // * @param mSolution the best mSolution to set
    // * @return the previous best mSolution
    // */
    // public S setBestSolution(VLSPhase phase, S mSolution);

    /**
     * Getter for the best mSolution found so far in the given phase
     * 
     * @param phase
     *            the phase for which the best mSolution is desired
     * @return the best mSolution found for the given phase
     */
    public S getBestSolution(VLSPhase phase);

    /**
     * Getter for the current best mSolution found in the main procedure.
     * 
     * @return the best mSolution found so far in the main loop
     */
    public S getOverallBestSolution();

    /**
     * Set the current phase of the VLS procedure.
     * 
     * @param phase
     *            the new current phase
     */
    public void setCurrentPhase(VLSPhase phase);

    /**
     * Getter for the current phase of the VLS procedure.
     * 
     * @return the phase in which the VLS procedure is
     */
    public VLSPhase getCurrentPhase();

    /**
     * Number of non improving iterations
     * 
     * @param phase
     *            the considered phase
     * @return the number of iteration without improvement in the given phase
     */
    public int getNonImprovingIterationCount(VLSPhase phase);

    /**
     * Reset the best mSolution for the given phase
     * 
     * @param phase
     *            the considered phase
     */
    public void resetBestSolution(VLSPhase phase);

    /**
     * Optional stopping condition
     * 
     * @return <code>true</code> if a stopping condition has been reached and the VLS procedure should be stopped.
     */
    public boolean stopConditionReached();

    /**
     * Reset this state to an initial setting
     */
    public void reset();

    /**
     * Getter for parent VLS procedure
     * 
     * @return the parentVLS
     */
    public VersatileLocalSearch<S> getParentVLS();
}