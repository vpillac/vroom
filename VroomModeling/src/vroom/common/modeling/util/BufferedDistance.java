/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package vroom.common.modeling.util;

import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.ILocation;
import vroom.common.utilities.BiHashMap;

/**
 * <code>BufferedDistance</code> is an extension of {@link CostCalculationDelegate} that adds a buffer to another
 * instance of {@link CostCalculationDelegate}
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 4-Aug-2010 16:33:50
 */
public class BufferedDistance extends CostCalculationDelegate {

    private final CostCalculationDelegate                 mCostDelegate;

    private final BiHashMap<ILocation, ILocation, Double> mBuffer;

    public BufferedDistance(CostCalculationDelegate costDelegate) {
        mCostDelegate = costDelegate;
        mBuffer = new BiHashMap<ILocation, ILocation, Double>();
        setPrecision(costDelegate.getPrecision(), costDelegate.getRoundingMethod());
    }

    @Override
    public double getDistance(Node origin, Node destination) {
        return getDistanceInternal(origin, destination);
    }

    @Override
    protected double getDistanceInternal(Node origin, Node destination) {
        if (origin == destination) {
            return 0;
        }

        Double dist = mBuffer.get(origin.getLocation(), destination.getLocation());
        if (dist == null) {
            dist = mCostDelegate.getDistance(origin, destination);
            synchronized (mBuffer) {
                mBuffer.put(origin.getLocation(), destination.getLocation(), dist);
            }
        }
        return dist;
    }

    @Override
    protected void precisionChanged() {
        clear();
        mCostDelegate.setPrecision(getPrecision(), getRoundingMethod());
    }

    /**
     * Clear all the buffered values
     */
    public void clear() {
        synchronized (mBuffer) {
            mBuffer.clear();
        }
    }

    @Override
    public String getDistanceType() {
        return mCostDelegate.getDistanceType();
    }
}
