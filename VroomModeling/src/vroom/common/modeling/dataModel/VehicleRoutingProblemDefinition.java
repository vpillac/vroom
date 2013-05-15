package vroom.common.modeling.dataModel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;

/**
 * The Class <code>VehicleRoutingProblemDefinition</code> is used to define the characteristics of a vehicle routing
 * problem as it will be modeled in this framework.
 * <p>
 * The Class contains
 * </p>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:07:22 a.m.
 */
public class VehicleRoutingProblemDefinition {

    /** A definition for the Dynamic VRP with Stochastic Demands. */
    public static VehicleRoutingProblemDefinition       DynVRPSD = new VehicleRoutingProblemDefinition(
                                                                         "DynVRPSD",
                                                                         "Dynamic VRP with Stochastic Demands",
                                                                         true, null);

    /** A definition for the Dynamic Vehicle Routing Problem. */
    public static VehicleRoutingProblemDefinition       DynVRP   = new VehicleRoutingProblemDefinition(
                                                                         "DynVRP", "Dynamic VRP",
                                                                         true, DynVRPSD);

    /** A definition for the Pickup and Delivery Problem. */
    public static VehicleRoutingProblemDefinition       PDP      = new VehicleRoutingProblemDefinition(
                                                                         "PDP",
                                                                         "Pickup and Delivery Problem",
                                                                         false, null);

    /** A definition for the Vehicle Routing Problem. */
    public static VehicleRoutingProblemDefinition       VRP      = new VehicleRoutingProblemDefinition(
                                                                         "VRP", "VRP", false, null);

    /** A definition for the Vehicle Routing Problem. */
    public static VehicleRoutingProblemDefinition       CVRP     = new VehicleRoutingProblemDefinition(
                                                                         "CVRP", "CVRP", false, VRP);

    /** A definition for the Traveling Salesman Problem. */
    public static VehicleRoutingProblemDefinition       TSP      = new VehicleRoutingProblemDefinition(
                                                                         "TSP", "TSP", false, VRP);

    /** A definition for the Vehicle Routing Problem with Time Windows. */
    public static VehicleRoutingProblemDefinition       CVRPTW    = new VehicleRoutingProblemDefinition(
                                                                         "CVRPTW",
                                                                         "CVRP with Time Windows",
                                                                         false, CVRP);

    /** A definition for the VRP with Stochastic Demands. */
    public static VehicleRoutingProblemDefinition       VRPSD    = new VehicleRoutingProblemDefinition(
                                                                         "VRPSD",
                                                                         "VRP with Stochastic Demands",
                                                                         false, null);

    /** The routing problems that generalize this problem. */
    private final List<VehicleRoutingProblemDefinition> mChilds;

    /** A long description for this problem. */
    private final String                                mDescription;

    /**
     * <true> if the described problem is dynamic, <code>false</code> otherwise.
     */
    private final boolean                               mDynamic;

    /** The short name of this problem. */
    private final String                                mName;

    /** The parent problem for this problem. */
    private final VehicleRoutingProblemDefinition       mParent;
    /**
     * The @link{edu.uniandes.copa.routing.dataModel.RequestAttributes} associated with requests for this class of
     * problem
     */
    private final RequestAttributeKey<?>                mRequestAttributes[];

    /**
     * Instantiates a new vehicle routing problem definition.
     * 
     * @param name
     *            a short name for this problem (e.g.: VRPTW)
     * @param description
     *            a description of this problem (e.g.: VRP with Time Windows)
     * @param dynamic
     *            <code>true</code> if this problem is <em>dynamic</em>, <code>false</code> otherwise
     * @param parent
     *            the parent problem, i.e. a problem that is a special case of this problem
     * @param requestAttributes
     *            the request attributes
     */
    public VehicleRoutingProblemDefinition(String name, String description, boolean dynamic,
            VehicleRoutingProblemDefinition parent, RequestAttributeKey<?>... requestAttributes) {
        super();
        mName = name;
        mDescription = description;
        mDynamic = dynamic;

        mParent = parent;
        if (mParent != null) {
            mParent.addChild(this);
        }

        mRequestAttributes = requestAttributes;

        mChilds = new LinkedList<VehicleRoutingProblemDefinition>();
    }

    /**
     * Adds the child.
     * 
     * @param child
     *            the child
     */
    protected void addChild(VehicleRoutingProblemDefinition child) {
        mChilds.add(child);
    }

    /**
     * Gets the childs.
     * 
     * @return a list of the problems that extend this problem
     */
    public Iterator<VehicleRoutingProblemDefinition> getChilds() {
        return mChilds.iterator();
    }

    /**
     * Gets the description.
     * 
     * @return a description a description of this problem
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Gets the name.
     * 
     * @return the short name (acronym) for this problem
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets the parent.
     * 
     * @return the direct superproblem of this problem
     */
    public VehicleRoutingProblemDefinition getParent() {
        return mParent;
    }

    /**
     * Gets the request attributes.
     * 
     * @return an array containing the {@link RequestAttributeKey} that must have {@link Request}s in instances of this
     *         problem
     */
    public RequestAttributeKey<?>[] getRequestAttributes() {
        return Arrays.copyOf(mRequestAttributes, mRequestAttributes.length);
    }

    /**
     * Checks if is dynamic.
     * 
     * @return true, if is dynamic
     * @return otherwise
     */
    public boolean isDynamic() {
        return mDynamic;
    }
}// end VehicleRoutingProblemDefinition