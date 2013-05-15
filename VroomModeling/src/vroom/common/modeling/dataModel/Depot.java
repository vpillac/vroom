package vroom.common.modeling.dataModel;

import vroom.common.modeling.dataModel.attributes.ILocation;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.dataModel.attributes.IVehicleAttribute;
import vroom.common.modeling.dataModel.attributes.NodeAttributeKey;

/**
 * <code>Depot</code> is an extension of {@link Node} that represents a depot.
 * 
 * @author Victor Pillac <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public class Depot extends Node implements IVehicleAttribute {

    /**
     * Creates a new <code>Depot</code> with the given <code>id</code> and <code>location</code>.
     * 
     * @param id
     *            the id
     * @param location
     *            the location
     * @see Node#Node(int, ILocation)
     */
    public Depot(int id, ILocation location) {
        super(id, location);
    }

    /**
     * Creates a new <code>Depot</code> with the given <code>id</code>, <code>location</code> and <code>name</code>.
     * 
     * @param id
     *            the depot id
     * @param name
     *            the depot name
     * @param location
     *            the depot location
     * @see Node#Node(int, String, ILocation)
     */
    public Depot(int id, String name, ILocation location) {
        super(id, name, location);
    }

    /**
     * Gets the time window of this depot.
     * 
     * @return the time window
     */
    public ITimeWindow getTimeWindow() {
        return getAttribute(NodeAttributeKey.TIME_WINDOW);
    }

}// end Depot