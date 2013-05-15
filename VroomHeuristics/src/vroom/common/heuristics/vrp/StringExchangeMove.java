/**
 * 
 */
package vroom.common.heuristics.vrp;

import vroom.common.heuristics.Move;
import vroom.common.modeling.dataModel.IVRPSolution;

/**
 * <code>StringExchangeMove</code> is a generic representation of a string-exchange move
 * <p>
 * Creation date: Jul 8, 2010 - 3:31:40 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class StringExchangeMove<S extends IVRPSolution<?>> extends Move {

    public final S    mSolution;
    /** Index of the first route */
    private final int mFirstRoute;
    /** Index of the second route */
    private final int mSecondRoute;
    /** Index of the first node */
    private final int mNodeI;
    /** Index of the second node */
    private final int mNodeJ;
    /** Index of the third node */
    private final int mNodeK;
    /** Index of the fourth node */
    private final int mNodeL;

    /* (non-Javadoc)
     * @see vroom.common.heuristics.Move#setImprovement(double)
     */
    @Override
    public void setImprovement(double improvement) {
        super.setImprovement(improvement);
    }

    /**
     * Getter for <code>solution</code>
     * 
     * @return the solution
     */
    public S getSolution() {
        return mSolution;
    }

    /**
     * Getter for <code>firstRoute</code>
     * 
     * @return the firstRoute
     */
    public int getFirstRoute() {
        return mFirstRoute;
    }

    /**
     * Getter for <code>secondRoute</code>
     * 
     * @return the secondRoute
     */
    public int getSecondRoute() {
        return mSecondRoute;
    }

    /**
     * Getter for <code>nodeI</code>
     * 
     * @return the nodeI
     */
    public int getNodeI() {
        return mNodeI;
    }

    /**
     * Getter for <code>nodeJ</code>
     * 
     * @return the nodeJ
     */
    public int getNodeJ() {
        return mNodeJ;
    }

    /**
     * Getter for <code>nodeK</code>
     * 
     * @return the nodeK
     */
    public int getNodeK() {
        return mNodeK;
    }

    /**
     * Getter for <code>nodeL</code>
     * 
     * @return the nodeL
     */
    public int getNodeL() {
        return mNodeL;
    }

    /** <code>true</code> if the first string should be reversed */
    private boolean reverseFirst  = false;
    /** <code>true</code> if the second string should be reversed */
    private boolean reverseSecond = false;

    /**
     * Getter for <code>reverseFirst</code>
     * 
     * @return the reverseFirst
     */
    public boolean isReverseFirst() {
        return reverseFirst;
    }

    /**
     * Setter for <code>reverseFirst</code>
     * 
     * @param reverseFirst
     *            the reverseFirst to set
     */
    public void setReverseFirst(boolean reverseFirst) {
        this.reverseFirst = reverseFirst;
    }

    /**
     * Getter for <code>reverseSecond</code>
     * 
     * @return the reverseSecond
     */
    public boolean isReverseSecond() {
        return reverseSecond;
    }

    /**
     * Setter for <code>reverseSecond</code>
     * 
     * @param reverseSecond
     *            the reverseSecond to set
     */
    public void setReverseSecond(boolean reverseSecond) {
        this.reverseSecond = reverseSecond;
    }

    /**
     * Creates a new <code>StringExchangeMove</code>
     * 
     * @param solution
     * @param firstRoute
     * @param secondRoute
     * @param nodeI
     * @param nodeJ
     * @param nodeK
     * @param nodeL
     */
    public StringExchangeMove(S solution, int firstRoute, int secondRoute, int nodeI, int nodeJ, int nodeK, int nodeL) {
        super(Double.NEGATIVE_INFINITY);
        mSolution = solution;
        mFirstRoute = firstRoute;
        mSecondRoute = secondRoute;
        mNodeI = nodeI;
        mNodeJ = nodeJ;
        mNodeK = nodeK;
        mNodeL = nodeL;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.Move#getMoveName()
     */
    @Override
    public String getMoveName() {
        return "string-exchange";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s(%s:[%s,%s],%s:[%s,%s],%.3f)", getMoveName(), getFirstRoute(), getNodeI(), getNodeJ(),
                getSecondRoute(), getNodeK(), getNodeL(), getImprovement());
    }

}
