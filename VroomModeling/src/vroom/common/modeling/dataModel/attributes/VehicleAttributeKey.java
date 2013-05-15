package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.Vehicle;

/**
 * <code>VehicleAttributeKey</code> is a class used to describe the attributes that a {@link Vehicle} can have. It
 * includes definitions for the attributes already defined in the framework.
 * <p/>
 * When defining a new {@link IVehicleAttribute}, one should create the associated {@link VehicleAttributeKey}, taking
 * into account that the keys are compared based on their <b>name</b> as returned by {@link #getName()}.
 * <p/>
 * Note that the {@link #hashCode()} method returns the hash of the
 * 
 * @param <A>
 *            the type of {@link IVehicleAttribute} associated with this key {@link VehicleAttributeKey} name.
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public class VehicleAttributeKey<A extends IVehicleAttribute> extends AttributeKey<A> {

    /**
     * A key for the possible trailer(s) associated with a vehicle. @see ITrailer
     */
    @SuppressWarnings("rawtypes")
    public static final VehicleAttributeKey<ITrailer>           TRAILER          = new VehicleAttributeKey<ITrailer>(
                                                                                         "trail",
                                                                                         ITrailer.class);

    /**
     * A key for a possible home depot of a vehicle
     */
    public static final VehicleAttributeKey<Depot>              DEPOT            = new VehicleAttributeKey<Depot>(
                                                                                         "home",
                                                                                         Depot.class);

    /** A key for the skills available to vehicle */
    public static final VehicleAttributeKey<AttributeWithIdSet> AVAILABLE_SKILLS = new VehicleAttributeKey<AttributeWithIdSet>(
                                                                                         "skills",
                                                                                         AttributeWithIdSet.class);

    /** A key for the availability range of a vehicle */
    public static final VehicleAttributeKey<Availability>       AVAILABILITY     = new VehicleAttributeKey<Availability>(
                                                                                         "avty",

                                                                                         Availability.class);

    /**
     * Creates a new <code>VehicleAttributeKey</code> with the given <code>name</code> that will be associated to the
     * given <code>attributeClass</code>.
     * 
     * @param name
     *            the name
     * @param attributeClass
     *            the attribute class
     */
    public VehicleAttributeKey(String name, Class<A> attributeClass) {
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
        return obj instanceof VehicleAttributeKey<?>
                && ((VehicleAttributeKey<?>) obj).hashCode() == hashCode();
    }

}
