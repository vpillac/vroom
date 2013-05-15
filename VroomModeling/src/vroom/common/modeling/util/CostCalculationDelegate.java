package vroom.common.modeling.util;

import java.math.RoundingMode;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.utilities.Utilities.Math;

/**
 * <code>CostHelperBase</code> is the base type for classes responsible for the calculation of distances between two
 * {@link Node}.<br/>
 * Subclasses must implement {@link CostCalculationDelegate#getDistance(Node, Node)}
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:12 a.m.
 */
public abstract class CostCalculationDelegate {

    /**
     * The precision (number of digits) used in the distance calculations, {@code  double} precision is represented by
     * {@link Integer#MAX_VALUE}
     */
    private int mPrecision = Integer.MAX_VALUE;

    /**
     * Returns the precision (number of digits) used in the distance calculations, {@code  double} precision is
     * represented by {@link Integer#MAX_VALUE}
     * 
     * @return the precision used in the distance calculations
     */
    public int getPrecision() {
        return mPrecision;
    }

    /**
     * Set the precision (number of digits) used in the distance calculations, {@code  double} precision is represented
     * by {@link Integer#MAX_VALUE}
     * 
     * @param precision
     *            the precision used in the distance calculations
     * @throws IllegalArgumentException
     *             if the precision is increased (may be overriden by subclasses)
     */
    public void setPrecision(int precision, RoundingMode method) {
        if (precision > mPrecision)
            throw new IllegalArgumentException("Cannot increase the precision");
        this.mPrecision = precision;
        this.mRoundingMethod = method;
        precisionChanged();
    }

    /**
     * Method called when the precision is changed
     * 
     * @see #setPrecision(int)
     */
    protected abstract void precisionChanged();

    /** the rounding method used **/
    private RoundingMode mRoundingMethod = RoundingMode.UNNECESSARY;

    /**
     * Getter for the rounding method used
     * 
     * @return the rounding method used
     */
    public RoundingMode getRoundingMethod() {
        return this.mRoundingMethod;
    }

    /**
     * Calculation of a base cost - or distance.
     * 
     * @param origin
     *            the origin node
     * @param destination
     *            the destination node
     * @return the cost (distance) between the specified and
     */
    public final double getCost(Node origin, Node destination) {
        return getCost(origin, destination, 1);
    }

    /**
     * Calculation of a base cost - or distance.
     * 
     * @param origin
     *            the origin node
     * @param destination
     *            the destination node
     * @return the cost (distance) between the specified and
     */
    public double getCost(INodeVisit origin, INodeVisit destination) {
        return getCost(origin.getNode(), destination.getNode());
    }

    /**
     * Calculation of a travel cost.
     * 
     * @param origin
     *            the origin node (<code>s</code>)
     * @param destination
     *            the destination node (<code>t</code>)
     * @param variableCost
     *            a variable cost per distance unit (<code>cv</code>)
     * @return the cost for the origin-destination trip:
     */
    final double getCost(Node origin, Node destination, double variableCost) {
        return getDistance(origin, destination) * variableCost;
    }

    /**
     * Calculation of a vehicle-dependent cost.
     * 
     * @param origin
     *            the origin
     * @param destination
     *            the destination
     * @param vehicle
     *            the vehicle that will be used to calculate vehicle-dependant traveling costs between the
     *            <code>origin</code> and <code>destination</code>
     * @return the cost between the specified and
     */
    public final double getCost(Node origin, Node destination, Vehicle vehicle) {
        return getCost(origin, destination, vehicle.getVariableCost());
    }

    /**
     * Calculation of a vehicle-dependent cost.
     * 
     * @param origin
     *            the origin
     * @param destination
     *            the destination
     * @param vehicle
     *            the vehicle that will be used to calculate vehicle-dependant traveling costs between the
     *            <code>origin</code> and <code>destination</code>
     * @return the cost between the specified {@code  origin} and {@code  destination}
     */
    public double getCost(INodeVisit origin, INodeVisit destination, Vehicle vehicle) {
        return getCost(origin.getNode(), destination.getNode(), vehicle);
    }

    /**
     * Calculation of a vehicle-dependent traveling time.
     * 
     * @param origin
     *            the origin
     * @param destination
     *            the destination
     * @param vehicle
     *            the vehicle that will be used to calculate vehicle-dependant traveling time between the
     *            <code>origin</code> and <code>destination</code>
     * @return the traveling time between the specified {@code  origin} and {@code  destination}
     */
    public double getTravelTime(Node origin, Node destination, Vehicle vehicle) {
        return getDistance(origin, destination) / vehicle.getSpeed();
    }

    /**
     * Calculation of a vehicle-dependent traveling time.
     * 
     * @param origin
     *            the origin
     * @param destination
     *            the destination
     * @param vehicle
     *            the vehicle that will be used to calculate vehicle-dependant traveling time between the
     *            <code>origin</code> and <code>destination</code>
     * @return the traveling time between the specified {@code  origin} and {@code  destination}
     */
    public double getTravelTime(INodeVisit origin, INodeVisit destination, Vehicle vehicle) {
        return getDistance(origin, destination) / vehicle.getSpeed();
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
    public double getInsertionCost(Node node, Node pred, Node succ, Vehicle vehicle) {
        return getCost(pred, node, vehicle) + getCost(node, succ, vehicle)
                - getCost(pred, succ, vehicle);
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
    public double getInsertionCost(INodeVisit node, INodeVisit pred, INodeVisit succ,
            Vehicle vehicle) {
        return (pred != null ? getCost(pred, node, vehicle) : 0)
                + (succ != null ? getCost(node, succ, vehicle) : 0)
                - (pred != null && succ != null ? getCost(pred, succ, vehicle) : 0);
    }

    /**
     * Gets the distance.
     * 
     * @param origin
     *            the origin
     * @param destination
     *            the destination
     * @return the distance
     */
    public double getDistance(Node origin, Node destination) {
        return Math.round(getDistanceInternal(origin, destination), getPrecision(),
                getRoundingMethod());
    }

    /**
     * Gets the distance.
     * 
     * @param origin
     *            the origin
     * @param destination
     *            the destination
     * @return the distance
     */
    protected abstract double getDistanceInternal(Node origin, Node destination);

    /**
     * Gets the distance.
     * 
     * @param origin
     *            the origin
     * @param destination
     *            the destination
     * @return the distance
     */
    public double getDistance(INodeVisit origin, INodeVisit destination) {
        return getDistance(origin.getNode(), destination.getNode());
    }

    /**
     * Cost type as defined in the TSPLib
     * 
     * @return a string describing the cost calculation
     */
    public abstract String getDistanceType();

}