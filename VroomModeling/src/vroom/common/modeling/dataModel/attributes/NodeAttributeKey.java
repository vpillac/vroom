package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.Node;

/**
 * <code>NodeAttributeKey</code> is a class used to describe the attributes that a {@link Node} can have. It includes
 * definitions for the attributes already defined in the framework.
 * <p/>
 * When defining a new {@link INodeAttribute}, one should create the associated
 * 
 * @param <A>
 *            the time of {@link INodeAttribute} associated with this key {@link NodeAttributeKey}, taking into account
 *            that the keys are compared based on their <b>name</b> as returned by {@link #getName()}.
 *            <p/>
 *            Note that the {@link #hashCode()} method returns the hash of the {@link NodeAttributeKey} name.
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public class NodeAttributeKey<A extends INodeAttribute> extends AttributeKey<A> {

    /**
     * A key for the time window attribute node, used in problem with time windows where the time windows is associated
     * with the physical node and not the request. @see ITimeWindow @see RequestAttributeKey#TIME_WINDOW
     */
    public static final NodeAttributeKey<ITimeWindow>           TIME_WINDOW           = new NodeAttributeKey<ITimeWindow>(
                                                                                              "tw",
                                                                                              ITimeWindow.class);

    /** A key for the vehicle compatibility possibly associated with a. {@link Node} @see IVehicleCompatibility */
    public static final NodeAttributeKey<IVehicleCompatibility> VEHICLE_COMPATIBILITY = new NodeAttributeKey<IVehicleCompatibility>(
                                                                                              "vehComp",
                                                                                              IVehicleCompatibility.class);

    /** A key for node location */
    public static final NodeAttributeKey<ILocation>             LOCATION              = new NodeAttributeKey<ILocation>(
                                                                                              "loc",
                                                                                              ILocation.class);

    /**
     * Creates a new <code>NodeAttributeKey</code> with the given <code>name</code> that will be associated to the given
     * <code>attributeClass</code>.
     * 
     * @param name
     *            the name
     * @param attributeClass
     *            the attribute class
     */
    public NodeAttributeKey(String name, Class<A> attributeClass) {
        super(name, attributeClass);
    }

    /**
     * Hashcode of this key.
     * 
     * @return the hashcode of the string representing the name of this key, as returned by {@link #getName()}
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Comparison of objects.
     * 
     * @param obj
     *            the obj
     * @return if obj is an instance of {@link VehicleAttributeKey} with the same {@link #hashCode}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NodeAttributeKey<?>
                && ((NodeAttributeKey<?>) obj).hashCode() == hashCode();
    }

}
