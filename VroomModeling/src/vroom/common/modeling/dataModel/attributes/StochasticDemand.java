package vroom.common.modeling.dataModel.attributes;

import java.util.Arrays;

import umontreal.iro.lecuyer.probdist.Distribution;
import vroom.common.utilities.ssj.SSJUtilities;

/**
 * The Class StochasticDemand.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public class StochasticDemand implements IStochasticDemand {

    /** The m demands. */
    private Distribution[] mDemands;

    /**
     * Instantiates a new stochastic demand.
     * 
     * @param demands
     *            the demands
     */
    public StochasticDemand(Distribution... demands) {
        mDemands = Arrays.copyOf(demands, demands.length);
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.IStochasticDemand#getDistribution(int)
     */
    @Override
    public Distribution getDistribution(int productId) {
        return mDemands[productId];
    }

    /* (non-Javadoc)
     * @see vroom.common.modeling.dataModel.attributes.IStochasticDemand#getDemand(int)
     */
    @Override
    public double getDemand(int productId) {
        return mDemands[productId].getMean();
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.utils.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return "Stochastic Demand";
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');

        for (Distribution d : mDemands) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append(SSJUtilities.toShortString(d));
        }

        sb.append(']');
        return sb.toString();
    }

    @Override
    public int getProductCount() {
        return mDemands.length;
    }

    @Override
    public double[] asArray() {
        double[] dem = new double[getProductCount()];
        for (int i = 0; i < dem.length; i++) {
            dem[i] = getDemand(i);
        }
        return dem;
    }

}// end StochasticDemand