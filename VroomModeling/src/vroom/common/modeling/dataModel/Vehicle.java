package vroom.common.modeling.dataModel;

import java.util.Arrays;

import vroom.common.modeling.dataModel.attributes.IVehicleAttribute;
import vroom.common.modeling.dataModel.attributes.VehicleAttributeKey;
import vroom.common.utilities.dataModel.IObjectWithID;
import vroom.common.utilities.dataModel.IObjectWithName;

/**
 * The Class <code>Vehicle</code> is used to represent a generic vehicle, with multiple compartments and associated
 * fixed and variable costs.
 * <p>
 * It extends {@link ObjectWithAttributes} and care therefore have additional attributes associated to it.
 * </p>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:25 a.m.
 */
public class Vehicle extends ObjectWithAttributes<IVehicleAttribute, VehicleAttributeKey<?>>
        implements IObjectWithName, IObjectWithID {

    /** An array containing the capacities of the different compartments. */
    private final double[] mCapacities;

    /** The fixed cost associated with this vehicle. */
    private final double   mFixedCost;
    /**
     * The variable cost associated with this vehicle.
     */
    private final double   mVariableCost;

    /**
     * The average speed of the vehicle
     */
    private final double   mSpeed;

    /**
     * Creates a new vehicle with the given characteristics, a fixed cost of 0 and a variable cost of 1.
     * 
     * @param id
     *            the id
     * @param description
     *            a description for this vehicle
     * @param capacities
     *            an array containing the capacities of the compartments of this vehicle
     * @see Vehicle#Vehicle(int, String, double, double, double...)
     * @see Vehicle#getFixedCost()
     * @see Vehicle#getVariableCost()
     */
    public Vehicle(int id, String description, double... capacities) {
        this(id, description, 0, 1, capacities);
    }

    /**
     * Instantiates a new vehicle.
     * 
     * @param id
     *            the id
     * @param description
     *            a description for this vehicle
     * @param fixedCost
     *            a fixed cost incurred when using this vehicle
     * @param variableCost
     *            a variable cost per distance unit when using this vehicle
     * @param capacities
     *            an array containing the capacities of the compartments of this vehicle
     */
    public Vehicle(int id, String description, double fixedCost, double variableCost,
            double... capacities) {
        this(id, description, fixedCost, variableCost, 1, capacities);
    }

    /**
     * Instantiates a new vehicle.
     * 
     * @param id
     *            the id
     * @param description
     *            a description for this vehicle
     * @param fixedCost
     *            a fixed cost incurred when using this vehicle
     * @param variableCost
     *            a variable cost per distance unit when using this vehicle
     * @param speed
     *            the average speed of this vehicle
     * @param capacities
     *            an array containing the capacities of the compartments of this vehicle
     */
    public Vehicle(int id, String description, double fixedCost, double variableCost, double speed,
            double... capacities) {
        setName(description);
        mID = id;

        if (capacities == null)
            capacities = new double[0];

        mCapacities = Arrays.copyOf(capacities, capacities.length);
        mFixedCost = fixedCost;
        mVariableCost = variableCost;
        mSpeed = speed;
    }

    /**
     * Clone.
     * 
     * @param cloneId
     *            the id for the cloned {@link Vehicle}
     * @return a clone of this instance with the given as id
     */
    public Vehicle clone(int cloneId) {
        return new Vehicle(cloneId, getName(), mFixedCost, mVariableCost, mSpeed, mCapacities);
    }

    /**
     * Getter for the vehicle capacity in the multiple compartment case.
     * 
     * @param product
     *            the id of the considered product
     * @return the capacity of the compartment of this vehicle for the given
     */
    public double getCapacity(int product) {
        return mCapacities[product];
    }

    /**
     * Gets the compartment count.
     * 
     * @return the number of compartments in this vehicle
     */
    public int getCompartmentCount() {
        return mCapacities.length;
    }

    /**
     * Gets the fixed cost.
     * 
     * @return the fixed cost associated with this vehicle. The default value is 0.
     */
    public double getFixedCost() {
        return mFixedCost;
    }

    /**
     * Gets the variable cost.
     * 
     * @return the variable cost associated with this vehicle per distance unit. The default value is 1
     */
    public double getVariableCost() {
        return mVariableCost;
    }

    /**
     * Gets the vehicle's average speed
     * 
     * @return the vehicle's average speed (default value is 1)
     */
    public double getSpeed() {
        return mSpeed;
    }

    /**
     * Getter for the vehicle capacity in the single compartment case.
     * 
     * @return the capacity of this vehicle
     */
    public double getCapacity() {
        return mCapacities[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("[%s %s cap:%s cf:%s cv:%s s:%s %s]", getID(), getName(),
                Arrays.toString(mCapacities), getFixedCost(), getVariableCost(), getSpeed(),
                getAttributesAsString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return mID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Vehicle other = (Vehicle) obj;
        if (!Arrays.equals(mCapacities, other.mCapacities)) {
            return false;
        }
        if (Double.doubleToLongBits(mFixedCost) != Double.doubleToLongBits(other.mFixedCost)) {
            return false;
        }
        if (mID != other.mID) {
            return false;
        }
        if (mName == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!mName.equals(other.mName)) {
            return false;
        }
        if (Double.doubleToLongBits(mVariableCost) != Double.doubleToLongBits(other.mVariableCost)) {
            return false;
        }
        return true;
    }

    /* IObjectWithName interface implementation */
    /** The name of this object. */
    private String mName;

    /*
     * (non-Javadoc)
     * 
     * @see edu.uniandes.copa.utils.IObjectWithName#getName()
     */
    @Override
    public String getName() {
        return mName;
    }

    /**
     * Setter for this object name.
     * 
     * @param name
     *            the name to be set
     */
    public void setName(String name) {
        mName = name;
    }

    /* */

    /* IObjectWithId interface implementation */
    /** The m id. */
    private final int mID;

    /*
     * (non-Javadoc)
     * 
     * @see edu.uniandes.copa.utils.IObjectWithID#getID()
     */
    @Override
    public int getID() {
        return mID;
    }
    /* */

}// end VRPVehicle