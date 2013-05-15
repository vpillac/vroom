package vroom.common.modeling.dataModel;

import vroom.common.modeling.util.CostCalculationDelegate;

/**
 * <code>DistanceMatrix</code> is a {@link CostCalculationDelegate} that is based on an double matrix representing the
 * distances between two vertexes.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:50 a.m.
 */
public class DistanceMatrix extends CostCalculationDelegate {

    /** The distance matrix between all nodes of the graph. */
    private final double mDistances[][];

    private String       mType;

    /**
     * Creates a new empty <code>DistanceMatrix</code> based on the given matrix.
     * 
     * @param distances
     *            the matrix of distances that will be used by this cost helper
     */
    public DistanceMatrix(double[][] distances) {
        mDistances = distances;
        mType = "EXPLICIT";
    }

    /**
     * Creates a new <code>DistanceMatrix</code> based on the given instance.
     */
    public DistanceMatrix(IVRPInstance instance) {
        int size = 0;
        for (Depot d : instance.getDepots())
            if (d.getID() > size)
                size = d.getID();
        for (IVRPRequest r : instance.getRequests())
            if (r.getNode().getID() > size)
                size = r.getNode().getID();
        size++;
        mDistances = new double[size][size];
        mType = "EXPLICIT";

        double[] x = new double[mDistances.length];
        double[] y = new double[mDistances.length];

        for (int d = 0; d < instance.getDepotCount(); d++) {
            Depot depot = instance.getDepot(d);
            x[depot.getID()] = depot.getLocation().getX();
            y[depot.getID()] = depot.getLocation().getY();
        }

        for (IVRPRequest r : instance.getRequests()) {
            x[r.getNode().getID()] = r.getNode().getLocation().getX();
            y[r.getNode().getID()] = r.getNode().getLocation().getY();
        }

        for (int i = 0; i < mDistances.length; i++) {
            for (int j = 0; j < mDistances.length; j++) {
                mDistances[i][j] = Math.sqrt(Math.pow(x[i] - x[j], 2) + Math.pow(y[i] - y[j], 2));
            }
        }

        mType = "EUC_2D";
    }

    /**
     * Creates a new empty <code>DistanceMatrix</code> based on a matrix of dimension <code>size x size</code> .
     * 
     * @param size
     *            the number of vertexes in the graph
     */
    public DistanceMatrix(int size) {
        this(new double[size][size]);
    }

    /**
     * Gets the distance between two nodes.
     * 
     * @param origin
     *            the index of the origin node
     * @param destination
     *            the index of the destination node
     * @return the distance between the specified nodes
     */
    public double getDistance(int origin, int destination) {
        return mDistances[origin][destination];
    }

    @Override
    public double getDistance(Node origin, Node destination) {
        return getDistance(origin.getID(), destination.getID());
    }

    @Override
    protected double getDistanceInternal(Node origin, Node destination) {
        return getDistance(origin.getID(), destination.getID());
    }

    /**
     * Gets the cost of traveling between two nodes with a given vehicle.
     * 
     * @param origin
     *            the index of the origin node
     * @param destination
     *            the index of the destination node
     * @param vehicle
     *            the considered vehicle
     * @return the cost of arc <code>(origin,destination)</code> for <code>vehicle</code>
     */
    public double getCost(int origin, int destination, Vehicle vehicle) {
        return getDistance(origin, destination) * vehicle.getVariableCost();
    }

    @Override
    public String getDistanceType() {
        return mType;
    }

    @Override
    protected void precisionChanged() {
        mType = String.format("%s[%s]", mType, getPrecision());
        for (int i = 0; i < mDistances.length; i++) {
            for (int j = 0; j < mDistances[i].length; j++) {
                mDistances[i][j] = vroom.common.utilities.Utilities.Math.round(mDistances[i][j],
                        getPrecision(), getRoundingMethod());
            }
        }
    }

    /**
     * Sets the distance between two nodes
     * 
     * @param i
     * @param j
     * @param distance
     * @author vpillac
     */
    public void setDistance(int i, int j, double distance) {
        mDistances[i][j] = distance;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getDistanceType());
    }

}// end DistanceMatrix