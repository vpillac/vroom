/*
 * 
 */
package vroom.common.utilities.lp;

/**
 * <code>SolverStatus</code>
 * <p>
 * Creation date, Sep 15, 2010 - 10,35,29 AM
 * 
 * @author Victor Pillac, <a href="http,//uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http,//copa.uniandes.edu.co">Copa</a> <a href="http,//www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http,//www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public enum SolverStatus {
    LOADED, //
    OPTIMAL, //
    INFEASIBLE, //
    INF_OR_UNBD, //
    UNBOUNDED, //
    CUTOFF, //
    ITERATION_LIMIT, //
    NODE_LIMIT, //
    TIME_LIMIT, //
    SOLUTION_LIMIT, //
    INTERRUPTED, //
    NUMERIC, //
    UNKNOWN_STATUS, //
    HEURISTIC
}
