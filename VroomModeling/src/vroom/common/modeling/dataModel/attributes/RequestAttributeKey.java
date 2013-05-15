package vroom.common.modeling.dataModel.attributes;

import vroom.common.modeling.dataModel.Request;

/**
 * <code>RequestAttributeKey</code> is a class used to describe the attributes that a {@link Request} can have. It
 * includes definitions for the attributes already defined in the framework.
 * <p/>
 * When defining a new {@link IRequestAttribute}, one should create the associated {@link RequestAttributeKey}, taking
 * into account that the keys are compared based on their <b>name</b> as returned by {@link #getName()}.
 * <p/>
 * Note that the {@link #hashCode()} method returns the hash of the
 * 
 * @param <A>
 *            the type of {@link IRequestAttribute} associated with this key {@link RequestAttributeKey} name.
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 */
public class RequestAttributeKey<A extends IRequestAttribute> extends AttributeKey<A> {

    /**
     * A key for the deterministic demand(s) attribute of a request. @see IDemand
     */
    public static final RequestAttributeKey<IDemand>            DEMAND             = new RequestAttributeKey<IDemand>(
                                                                                           "dem", IDemand.class);
    // /**
    // * A key for the stochastic demand(s) attribute of a request
    // *
    // * @see IStochasticDemand
    // */
    // public static final RequestAttributeKey<IStochasticDemand>
    // STOCHASTIC_DEMAND = new
    // RequestAttributeKey<IStochasticDemand>(
    // "Stochastic Demand", IStochasticDemand.class);
    /**
     * A key for the request release date attribute, mainly used for the description of dynamic instances where requests
     * are revealed over time. @see IReleaseDate
     */
    public static final RequestAttributeKey<IReleaseDate>       RELEASE_DATE       = new RequestAttributeKey<IReleaseDate>(
                                                                                           "rel", IReleaseDate.class);

    /**
     * A key for the time window attribute of a request, used in problem with time windows. @see ITimeWindow
     */
    public static final RequestAttributeKey<ITimeWindow>        TIME_WINDOW        = new RequestAttributeKey<ITimeWindow>(
                                                                                           "tw", ITimeWindow.class);

    /** A key for the service time attribute of a request */
    public static final RequestAttributeKey<Duration>           SERVICE_TIME       = new RequestAttributeKey<Duration>(
                                                                                           "st", Duration.class);
    /** A key for the skills required to service a request */
    public static final RequestAttributeKey<AttributeWithIdSet> REQUIRED_SKILL_SET = new RequestAttributeKey<AttributeWithIdSet>(
                                                                                           "skill_set",
                                                                                           AttributeWithIdSet.class);

    /** A key for a single skill required to service a request */
    public static final RequestAttributeKey<Skill>              REQUIRED_SKILL     = new RequestAttributeKey<Skill>(
                                                                                           "skill", Skill.class);

    /**
     * Creates a new <code>RequestAttributeKey</code> with the given <code>name</code> that will be associated to the
     * given <code>attributeClass</code>.
     * 
     * @param name
     *            the name
     * @param attributeClass
     *            the attribute class
     */
    public RequestAttributeKey(String name, Class<A> attributeClass) {
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
        return obj instanceof RequestAttributeKey<?> && ((RequestAttributeKey<?>) obj).hashCode() == hashCode();
    }

}
