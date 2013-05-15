/*
 * jCW : a java library for the development of saving based heuristics
 */
package vroom.common.heuristics.cw.kernel;

import vroom.common.modeling.dataModel.IVRPSolution;

/**
 * <code>IJCWSavingsAlgorithm</code> is an interface for savings algorithms that
 * can be used inside a CW heuristic.
 * <p>
 * <b>Important: </b> for the proper functioning of the framework,
 * implementations must define a constructor taking exactly one argument of type
 * {@link ClarkeAndWrightHeuristic}
 * <p>
 * Creation date: Apr 16, 2010 - 11:30:05 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public interface ISavingsAlgorithm<S extends IVRPSolution<?>> extends Runnable {

	/**
	 * Parent Clark and Wright heuristic.
	 * <p>
	 * Note that an instance of {@link ISavingsAlgorithm} should be associated
	 * to a unique {@link ClarkeAndWrightHeuristic}
	 * 
	 * @return the instance of {@link ClarkeAndWrightHeuristic} to which this
	 *         instance is associated
	 */
	public ClarkeAndWrightHeuristic<S> getParentHeuristic();

	/**
	 * Getter for mSolution.
	 * 
	 * @return The mSolution manipulated by this instance
	 */
	public S getSolution();

	/**
	 * General a report for the state of the constraints.
	 * 
	 * @return a basic report describing the state of the problem-specific
	 *         constraints
	 */
	public String generateConstraintsReport();

	/**
	 * Generate a general report for the result of the algorithm.
	 * 
	 * @return a basic report including: -Total number of routes -Total
	 *         execution time -Total traveled distance of each route
	 *         (deterministic part of the objective function) -Total expected
	 *         recursion cost of each route (stochastic part of the objective
	 *         function) -Total traveled distance -Total expected recursion cost
	 *         -Total cost Format the mSolution using a data structure
	 *         compatible with RoutePlotter: -Column A: Route identifier -Column
	 *         B: Node identifier
	 */
	public String generateGeneralReport();

	/**
	 * Initialization of this heuristic with the given mSolution
	 * 
	 * @param mSolution
	 *            the mSolution that will be manipulated by this heuristic
	 */
	public abstract void initialize(S solution);

	/**
	 * Tries to remove routes in the case of a limited fleet
	 * 
	 * @return {@code true} if the solution was repaired, {@code false} if it
	 *         still contains too many routes
	 */
	public abstract boolean repairSolutionForLimitedFleet();
}
