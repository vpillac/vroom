package vroom.common.modeling.dataModel.attributes;

import java.util.Arrays;

/**
 * The Class DeterministicDemand is an implementation of {@link IDemand} that defines demands for each product as a
 * <code>double</code>.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:49 a.m.
 */
public class DeterministicDemand implements IDemand {

    /** An array containing the demand for each product *. */
    private final double[] mDemands;

    /**
     * Creates a new demand by copying the given array.
     * 
     * @param demands
     *            an array containing the demands for each product
     */
    public DeterministicDemand(double... demands) {
        mDemands = Arrays.copyOf(demands, demands.length);
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.IDemand#getDemand(int)
     */
    @Override
    public double getDemand(int productId) {
        return mDemands[productId];
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Deterministic Demand";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Arrays.toString(mDemands);
    }

    @Override
    public int getProductCount() {
        return mDemands.length;
    }

    @Override
    public double[] asArray() {
        return Arrays.copyOf(mDemands, getProductCount());
    }
}// end DeterministicDemand