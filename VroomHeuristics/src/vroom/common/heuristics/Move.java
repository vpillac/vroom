/**
 * 
 */
package vroom.common.heuristics;

import vroom.common.utilities.Constants;
import vroom.common.utilities.optimization.IMove;

/**
 * <code>Move</code>
 * <p>
 * Creation date: Jun 18, 2010 - 1:28:36 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class Move implements IMove {

    /** the variation in the objective function resulting from this move **/
    private double mImprovement;

    /* (non-Javadoc)
     * @see vroom.common.heuristics.IMove#getImprovement()
     */
    @Override
    public double getImprovement() {
        return mImprovement;
    }

    /**
     * Setter for <code>improvement</code>
     * 
     * @param improvement
     *            the improvement to set
     */
    @Override
    public void setImprovement(double improvement) {
        mImprovement = improvement;
    }

    /**
     * Creates a new <code>Move</code> with an undefined improvement
     */
    public Move() {
        this(Double.NaN);
    }

    /**
     * Creates a new <code>Move</code>
     * 
     * @param improvement
     *            the improvement in the objective function resulting from this move
     * @see #getImprovement()
     */
    public Move(double improvement) {
        mImprovement = improvement;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.IMove#isImproving()
     */
    @Override
    public boolean isImproving() {
        return Constants.isStrictlyPositive(getImprovement());
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.IMove#getMoveName()
     */
    @Override
    public abstract String getMoveName();

    @Override
    public int compareTo(IMove o) {
        if (o == null)
            throw new NullPointerException();

        return Double.compare(getImprovement(), o.getImprovement());
    }
}
