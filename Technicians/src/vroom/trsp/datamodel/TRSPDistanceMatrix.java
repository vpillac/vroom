/**
 * 
 */
package vroom.trsp.datamodel;

import vroom.common.modeling.dataModel.DistanceMatrix;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.attributes.ILocation;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.utilities.GeoTools;

/**
 * <code>TRSPDistanceMatrix</code> is an extension of {@link DistanceMatrix} for the special case of the TRSP
 * <p>
 * Creation date: Feb 23, 2011 - 12:09:42 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPDistanceMatrix extends CostCalculationDelegate {

    /** The distance matrix between all nodes of the graph. */
    private final double       mDistances[][];

    private final TRSPInstance mInstance;

    public TRSPDistanceMatrix(TRSPInstance instance) {
        mInstance = instance;

        mDistances = new double[mInstance.getMaxId()][mInstance.getMaxId()];
        for (int i = 0; i < mDistances.length; i++) {
            for (int j = 0; j < mDistances.length; j++) {
                if (i != j)
                    mDistances[i][j] = evaluateDistance(mInstance.getNode(i).getLocation(),
                            mInstance.getNode(j).getLocation());
            }
        }
    }

    /**
     * Creates a new <code>TRSPDistanceMatrix</code> with the specified distance matrix
     * 
     * @param instance
     * @param ds
     */
    protected TRSPDistanceMatrix(TRSPInstance instance, double[][] ds) {
        mInstance = instance;
        mDistances = new double[mInstance.getMaxId()][mInstance.getMaxId()];
        for (int i = 0; i < mDistances.length; i++) {
            for (int j = 0; j < mDistances.length; j++) {
                if (i != j)
                    mDistances[i][j] = ds[i][j];
            }
        }
    }

    @Override
    protected void precisionChanged() {
        for (int i = 0; i < mDistances.length; i++) {
            for (int j = 0; j < mDistances[i].length; j++) {
                mDistances[i][j] = vroom.common.utilities.Utilities.Math.round(mDistances[i][j],
                        getPrecision(), getRoundingMethod());
            }
        }
    }

    /**
     * @param a
     * @param b
     * @return
     */
    private double evaluateDistance(ILocation a, ILocation b) {
        if (a.getCoordinateSystem() != b.getCoordinateSystem())
            throw new IllegalArgumentException(String.format(
                    "Both locations must be in the same coordinate system (%s/%s)",
                    a.getCoordinateSystem(), b.getCoordinateSystem()));
        switch (a.getCoordinateSystem()) {
        case CARTESIAN:
            return GeoTools.distEuclidean(a.getX(), a.getY(), b.getX(), b.getY());
        case LAT_LON_DEC_DEG:
            return GeoTools.distVincenty(a.getX(), a.getY(), b.getX(), b.getY()) / 1000;
        default:
            throw new UnsupportedOperationException("Unsupported coordinate system: "
                    + a.getCoordinateSystem());
        }
    }

    @Override
    public double getDistance(Node origin, Node destination) {
        return getDistanceInternal(origin, destination);
    }

    @Override
    protected double getDistanceInternal(Node origin, Node destination) {
        return getDistance(origin.getID(), destination.getID());
    }

    /**
     * Calculate the distance between to nodes.
     * 
     * @param pred
     *            the first node id
     * @param succ
     *            the second node id
     * @return the distance separating <code>pred</code> and <code>succ</code>: <code>|(pred,succ)|</code>
     */
    public double getDistance(int pred, int succ) {
        return mDistances[pred][succ];
    }

    /**
     * Calculate the cost of traveling an arc with a given technician.
     * <p>
     * Depending on the definition of the technician {@linkplain Technician#getVariableCost() variable cost} this can be
     * a measure of time, monetary value or other.
     * </p>
     * 
     * @param o
     *            the first node id
     * @param d
     *            the second node id
     * @param technician
     *            the considered technician
     * @return the cost of traveling from o to d, defined by <code>|(o,d)|*technician.getVariableCost()</code>
     * @see Technician#getVariableCost()
     */
    public double getCost(int o, int d, Technician technician) {
        return getDistance(o, d) * technician.getVariableCost();
    }

    /**
     * Calculation of the insertion detour.
     * 
     * @param node
     *            the node to be inserted
     * @param pred
     *            the candidate predecessor of <code>node</code>
     * @param succ
     *            the candidate successor of <code>node</code>
     * @return the cost of inserting as given by
     */
    public double getInsertionDetour(int node, int pred, int succ) {
        return getDistance(pred, node) + getDistance(node, succ) - getDistance(pred, succ);
    }

    /**
     * Calculation of the insertion cost.
     * 
     * @param node
     *            the node to be inserted
     * @param pred
     *            the candidate predecessor of <code>node</code>
     * @param succ
     *            the candidate successor of <code>node</code>
     * @param vehicle
     *            the considered vehicle
     * @return the cost of inserting as given by
     */
    public double getInsertionCost(int node, int pred, int succ, Technician technician) {
        return getCost(pred, node, technician) + getCost(node, succ, technician)
                - getCost(pred, succ, technician);
    }

    /**
     * Calculate the traveling time over an arc with a given technician, depending on its traveling speed
     * {@linkplain Technician#getSpeed() speed}.
     * 
     * @param o
     *            the first node id
     * @param d
     *            the second node id
     * @param technician
     *            the considered technician
     * @return the traveling time from o to d, defined by <code>|(o,d)|*technician.getSpeed()</code>
     * @see Technician#getSpeed()
     */
    public double getTravelTime(int o, int d, Technician technician) {
        return getDistance(o, d) / technician.getSpeed();
    }

    /**
     * Returns the maximum distance between two nodes in this matrix
     * 
     * @return the maximum distance between two nodes in this matrix
     */
    public double getMaxDistance() {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < mDistances.length; i++)
            for (int j = 0; j < mDistances[i].length; j++)
                if (mDistances[i][j] > max)
                    max = mDistances[i][j];
        return max;
    }

    @Override
    public String getDistanceType() {
        return "EUCLIDIAN_2D";
    }

}
