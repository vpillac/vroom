/**
 * 
 */
package vroom.common.heuristics;

/**
 * <code>ProcedureStatus</code> is an enumeration of possible statuses of an procedure.
 * <p>
 * Creation date: Apr 12, 2011 - 1:47:24 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public enum ProcedureStatus {

    /** The procedure has just been instantiated */
    INSTANTIATED(false),

    /** Procedure is running. */
    RUNNING(true),

    /** Procedure is initializing */
    INITIALIZATION(true),

    /** Procedure initialized. */
    INITIALIZED(false),

    /** Procedure aborted. */
    ABORTED(false),

    /** Found a local optimum */
    LOCAL_OPTIMUM(false),

    /** Iteration limit reach */
    LIMIT_ITERATION(false),

    /** Time limit reached */
    LIMIT_TIME(false),

    /** Procedure met the convergence criterion. */
    LIMIT_CONVERGENCE(false),

    /** Infeasible instance. */
    INFEASIBLE(false),

    /** Found an infeasible solution. */
    INFEASIBLE_SOLUTION(false),

    /** Caught an exception. */
    EXCEPTION(false),

    /** Procedure terminated. */
    TERMINATED(false),

    /** The procedure is paused */
    PAUSED(true);

    private final boolean mRunning;

    /**
     * Creates a new <code>ProcedureStatus</code>
     * 
     * @param running
     */
    private ProcedureStatus(boolean running) {
        mRunning = running;
    }

    /**
     * Returns <code>true</code> if this state corresponds to a <em>running</em> state
     * 
     * @return <code>true</code> if this state corresponds to a <em>running</em> state
     */
    public boolean isRunning() {
        return mRunning;
    }
}
