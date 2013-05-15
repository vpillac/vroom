/**
 * 
 */
package vroom.common.heuristics.vrp;

import vroom.common.heuristics.Move;
import vroom.common.modeling.dataModel.IVRPSolution;

/**
 * <code>TwoOptMove</code> is a generic class for neighborhoods based on a customer pair
 * <p>
 * Creation date: Jun 18, 2010 - 5:11:26 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class PairMove<S extends IVRPSolution<?>> extends Move {

    public final S    mSolution;
    /** Index of the first route */
    private final int mRouteI;
    /** Index of the second route */
    private final int mRouteJ;
    /** Index of the first node */
    private final int mI;
    /** Index of the second node */
    private final int mJ;

    /**
     * Getter for <code>mSolution</code>
     * 
     * @return the mSolution
     */
    public S getSolution() {
        return mSolution;
    }

    /**
     * Getter for <code>mRouteI</code>
     * 
     * @return the mRouteI
     */
    public int getRouteI() {
        return mRouteI;
    }

    /**
     * Getter for <code>mRouteJ</code>
     * 
     * @return the mRouteJ
     */
    public int getRouteJ() {
        return mRouteJ;
    }

    /**
     * Getter the index of the first node
     * 
     * @return the index of the first node
     */
    public int getI() {
        return mI;
    }

    /**
     * Getter the index of the second node
     * 
     * @return the index of the second node
     */
    public int getJ() {
        return mJ;
    }

    /**
     * Creates a new <code>PairMove</code>
     * 
     * @param improvement
     *            the change in the objective function
     * @param mSolution
     * @param mRouteI
     * @param mRouteJ
     * @param mI
     * @param mJ
     */
    public PairMove(double improvement, S solution, int routeI, int routeJ, int i, int j) {
        super(improvement);
        this.mSolution = solution;
        this.mRouteI = routeI;
        this.mRouteJ = routeJ;
        this.mI = i;
        this.mJ = j;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s(%s:%s,%s:%s,%.3f)", getMoveName(), getRouteI(), getI(),
                getRouteJ(), getJ(), getImprovement());
    }

}