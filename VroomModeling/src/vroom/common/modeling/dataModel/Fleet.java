package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import vroom.common.utilities.dataModel.ObjectWithIdComparator;

/**
 * The Class Fleet<V> contains the definition of the vehicle fleet of a particular instance. It basically contains a
 * list of vehicles and provides factory methods for the definition of a fleet.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public class Fleet<V extends Vehicle> implements Iterable<V> {

    /** <code>true</code> if the fleet is homogeneous **/
    private final boolean mHomogeneous;

    /**
     * Getter for the homogeneous flag
     * 
     * @return <code>true</code> if the fleet is homogeneous
     */
    public boolean isHomogeneous() {
        return mHomogeneous;
    }

    /** The fleet size for homogeneous fleets **/
    private final int mFleetSize;

    /**
     * Return <code>true</code> if the fleet is unlimited
     * 
     * @return <code>true</code> if the fleet is unlimited
     */
    public boolean isUnlimited() {
        return size() == Integer.MAX_VALUE;
    }

    /**
     * Factory method to create an unlimited fleet
     * 
     * @param baseVehicle
     *            the base vehicle for the fleet
     * @return a new {@link Fleet} with an unlimited number of vehicles similar to the given <code>baseVehicle</code>
     */
    public static <V extends Vehicle> Fleet<V> newUnlimitedFleet(V baseVehicle) {
        return new Fleet<V>(baseVehicle);
    }

    /**
     * Factory method to create homogeneous fleet.
     * 
     * @param size
     *            the size of the desired fleet
     * @param baseVehicle
     *            the base vehicle of the fleet
     * @return a new @link{Fleet} corresponding to an homogeneous fleet of size and with vehicles have the
     *         characteristic of
     */
    @SuppressWarnings("unchecked")
    public static <V extends Vehicle> Fleet<V> newHomogenousFleet(int size, V baseVehicle) {
        ArrayList<V> fleet = new ArrayList<V>(size);
        for (int i = 0; i < size; i++) {
            fleet.add((V) baseVehicle.clone(i));
        }
        return new Fleet<V>(fleet, true);
    }

    /**
     * Factory method to create an heterogeneous fleet
     * 
     * @param vehicles
     *            the list of vehicles
     * @return a new {@link Fleet} with the given vehicles
     */
    public static <V extends Vehicle> Fleet<V> newHeterogenousFleet(Collection<V> vehicles) {
        return new Fleet<V>(vehicles, false);
    }

    /** The m vehicles. */
    protected final List<V> mVehicles;

    /**
     * Creates a new <code>Fleet</code> based on the given collection of vehicles.
     * 
     * @param vehicles
     *            the vehicles of this fleet
     * @param homogeneous
     *            <code>true</code> if the fleet is homogeneous, <code>false</code> otherwise
     */
    protected Fleet(Collection<V> vehicles, boolean homogeneous) {
        mVehicles = new ArrayList<V>(vehicles.size());
        mVehicles.addAll(vehicles);
        Collections.sort(mVehicles, new ObjectWithIdComparator());
        mHomogeneous = homogeneous;
        mFleetSize = vehicles.size();
    }

    /**
     * Creates a new unlimited <code>Fleet</code>
     * 
     * @param vehicle
     *            the base vehicle
     */
    protected Fleet(V vehicle) {
        mVehicles = Collections.singletonList(vehicle);
        mFleetSize = Integer.MAX_VALUE;
        mHomogeneous = true;
    }

    /**
     * Gets the specified vehicle.
     * 
     * @param vehicleId
     *            the id of the desired vehicle
     * @return the vehicle with id
     */
    public V getVehicle(int vehicleId) {
        if (isUnlimited())
            return mVehicles.get(0);
        else
            return mVehicles.get(vehicleId);
    }

    /**
     * Gets the base vehicle for homogeneous fleets
     * 
     * @param vehicleId
     *            the id of the desired vehicle
     * @return the vehicle with id
     */
    public V getVehicle() {
        return mVehicles.get(0);
    }

    /**
     * Size.
     * 
     * @return the size of the fleet
     */
    public int size() {
        return isHomogeneous() ? mFleetSize : mVehicles.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<V> iterator() {
        return mVehicles.iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(isUnlimited() ? 15 : size() * 15);

        b.append("Fleet size:");
        b.append(isUnlimited() ? "UL" : size());

        if (isHomogeneous() || isUnlimited()) {
            b.append(" vehicle:");
            b.append(getVehicle().toString());
        } else {
            b.append(" vehicles:{");
            for (V v : this) {
                b.append('"');
                b.append(v.toString());
                b.append('"');
                b.append(',');
            }

            if (size() > 0) {
                b.setCharAt(b.length() - 1, '}');
            } else {
                b.append('}');
            }
        }

        return b.toString();
    }

}// end VRPFleet