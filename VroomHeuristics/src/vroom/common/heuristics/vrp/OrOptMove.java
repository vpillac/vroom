/**
 * 
 */
package vroom.common.heuristics.vrp;

import vroom.common.modeling.dataModel.IVRPSolution;

/**
 * <code>OrOptMove</code> is a representation of a Or-opt move.
 * <p>
 * Creation date: Jul 2, 2010 - 2:00:44 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class OrOptMove<S extends IVRPSolution<?>> extends PairMove<S> {

    /** The insertion point **/
    private int mInsertionIndex;

    /**
     * Getter for the insertion index
     * 
     * @return The insertion index
     */
    public int getInsertionIndex() {
        return this.mInsertionIndex;
    }

    /** the insertion route **/
    private int mInsertionRoute;

    /**
     * Getter for the insertion route
     * 
     * @return the insertion route
     */
    public int getInsertionRoute() {
        return this.mInsertionRoute;
    }

    /** <code>true</code> if the substring has to be reversed */
    private boolean mReverseString = false;

    /**
     * Getter for <code>reverseString</code>
     * 
     * @return <code>true</code> if the substring has to be reversed
     */
    public boolean isStringReversed() {
        return mReverseString;
    }

    /**
     * Set the insertion point
     * 
     * @param route
     *            the route in which the substring will be inserted
     * @param index
     *            the insertion index
     * @param reverse
     *            <code>true</code> if the substring has to be reversed
     */
    public void setInsertion(int route, int index, boolean reverse) {
        this.mInsertionIndex = index;
        this.mInsertionRoute = route;
        this.mReverseString = reverse;
    }

    /**
     * Creates a new <code>OrOptMove</code>
     * 
     * @param mSolution
     * @param route
     * @param nodeI
     * @param nodeJ
     */
    public OrOptMove(S solution, int route, int i, int j) {
        super(Double.NEGATIVE_INFINITY, solution, route, route, i, j);
        setInsertion(-1, -1, false);
    }

    /**
     * Creates a new <code>OrOptMove</code>
     * 
     * @param improvement
     * @param mSolution
     * @param routeI
     * @param routeJ
     * @param i
     * @param j
     * @param insertionIndex
     * @param insertionRoute
     */
    public OrOptMove(double improvement, S solution, int routeI, int i, int j, int insertionIndex, int insertionRoute) {
        super(improvement, solution, routeI, routeI, i, j);
        mInsertionIndex = insertionIndex;
        mInsertionRoute = insertionRoute;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.Move#setImprovement(double)
     */
    @Override
    public void setImprovement(double improvement) {
        super.setImprovement(improvement);
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.Move#getMoveName()
     */
    @Override
    public String getMoveName() {
        return "Or-opt";
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.vrp.PairMove#toString()
     */
    @Override
    public String toString() {
        return String.format("%s%s(%s:[%s-%s],%s:%s,%.3f)", this.getMoveName(), isStringReversed() ? "R" : "",
                getRouteI(), getI(), getJ(), getInsertionRoute(), getInsertionIndex(), getImprovement());
    }
}
