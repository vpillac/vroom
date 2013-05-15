/**
 * 
 */
package vroom.common.heuristics.vrp;

import vroom.common.modeling.dataModel.IVRPSolution;

/**
 * <code>TwoOptMove</code> is a class representing a 2-opt move.
 * <p>
 * Creation date: Jun 18, 2010 - 5:11:26 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TwoOptMove extends PairMove<IVRPSolution<?>> {

    /** <code>true</code> if the move is a special 2-opt* move */
    private boolean star;

    /**
     * Getter for <code>star</code>
     * 
     * @return the star
     */
    public boolean isStar() {
        return star;
    }

    /**
     * Setter for <code>star</code>
     * 
     * @param star
     *            the star to set
     */
    public void setStar(boolean star) {
        this.star = star;
    }

    /**
     * Creates a new <code>TwoOptMove</code>
     * 
     * @param improvement
     *            the change in the objective function
     * @param mSolution
     * @param routeI
     * @param routeJ
     * @param nodeI
     * @param nodeJ
     * @param star
     *            <code>true</code> if the move is a 2-opt* move
     */
    public TwoOptMove(double improvement, IVRPSolution<?> solution, int routeI, int routeJ, int i,
            int j, boolean star) {
        super(improvement, solution, routeI, routeJ, i, j);
        this.star = star;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.Move#getMoveName()
     */
    @Override
    public String getMoveName() {
        return String.format("2-opt%s", star ? "*" : " ");
    }
}