/**
 * 
 */
package vroom.common.utilities.gurobi;

import gurobi.GRBVar;

/**
 * <code>GRBPartialSolution</code> is a class representing a partial solution.
 * <p>
 * It contains an array of variables and an array of double with the corresponding values
 * </p>
 * <p>
 * Creation date: 20/08/2010 - 13:21:27
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBPartialSolution {

    /** the array of variables **/
    private final GRBVar[] mVariables;

    /**
     * Getter for variables : the array of variables
     * 
     * @return the value of variables
     */
    public GRBVar[] getVariables() {
        return mVariables;
    }

    /** the array of values **/
    private final double[] mValues;

    /**
     * Getter for values : the array of values
     * 
     * @return the value of values
     */
    public double[] getValues() {
        return mValues;
    }

    /**
     * Creates a new <code>GRBPartialSolution</code> of the given size
     * 
     * @param size
     */
    public GRBPartialSolution(int size) {
        mVariables = new GRBVar[size];
        mValues = new double[size];
    }

    /**
     * Creates a new <code>GRBPartialSolution</code> based on the given variable and values array
     * 
     * @param variables
     * @param values
     */
    public GRBPartialSolution(GRBVar[] variables, double[] values) {
        super();
        mVariables = variables;
        mValues = values;
    }

}
