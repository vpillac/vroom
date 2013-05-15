package vroom.common.modeling.dataModel.attributes;

import java.util.Collection;

import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.Vehicle;

/**
 * <code>IVehicleCompatibility</code> is an interface to attributes that represent a compatibility relation between
 * nodes and vehicles.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 24-Feb-2010 01:48:53 p.m.
 */
public interface IVehicleCompatibility extends INodeAttribute {

    /**
     * Test whether a vehicle is compatible with the associated node.
     * 
     * @param vehicle
     *            the vehicle which compatibility will be tested
     * @return can visit the associated node, otherwise
     */
    public boolean isVehicleCompatible(Vehicle vehicle);

    /**
     * Identification of compatible vehicles.
     * 
     * @param fleet
     *            the fleet of vehicles from which compatible vehicles should be identified
     * @return a collection of vehicles from the given that are compatible with the associated node
     */
    public Collection<Vehicle> getCompatibleVehicles(Fleet<?> fleet);

}