package vroom.common.modeling.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.DiscreteTimeStamp;
import vroom.common.modeling.dataModel.DynamicInstance;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.Node;
import vroom.common.modeling.dataModel.Request;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.dataModel.attributes.DefaultReleaseDate;
import vroom.common.modeling.dataModel.attributes.DeterministicDemand;
import vroom.common.modeling.dataModel.attributes.ILocation;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;

/**
 * The Class VRPInstanceBuilder is a utility class to build random VRP instances
 */
public class VRPInstanceBuilder {

    /** The rnd. */
    private static Random      rnd           = new Random();

    /** The Constant CENTRAL_DEPOT. */
    private static final Depot CENTRAL_DEPOT = new Depot(0, "CentralDepot", new PointLocation(0, 0));

    private static final Depot CIRCLE_DEPOT  = new Depot(0, "CircleDepot", new PointLocation(1, 0));

    /**
     * New simple dynamic instance.
     * 
     * @param nRequests
     *            the n requests
     * @param vehicleCap
     *            the vehicle cap
     * @param maxDem
     *            the max dem
     * @param horizon
     *            the horizon
     * @param seed
     *            the seed
     * @param randomPoints
     *            <code>true</code> if the nodes are to be randomly distributed
     * @return a instance with a fleet composed of a single vehicle of capacity , with requests associated with
     *         different nodes, each with a single random demand between 1 and . The instance has a reference to a
     *         {@link EuclidianDistance} for the calulation of costs
     */
    public static DynamicInstance newSimpleDynamicInstance(int nRequests, int vehicleCap, int maxDem, int horizon,
            long seed, boolean randomPoints) {
        Fleet<?> fleet = Fleet.newHomogenousFleet(1, new Vehicle(0, "DefaultVehicle", vehicleCap));

        List<Depot> depots = new ArrayList<Depot>();
        if (randomPoints) {
            depots.add(CENTRAL_DEPOT);
        } else {
            depots.add(CIRCLE_DEPOT);
        }

        DynamicInstance instance = new DynamicInstance("SimpleInstance_" + nRequests, 0, fleet, depots,
                VehicleRoutingProblemDefinition.DynVRP, new EuclidianDistance());

        for (int n = 1; n <= nRequests; n++) {
            instance.addRequest(generateRequest(maxDem, horizon, true, n, nRequests));
        }

        return instance;
    }

    /**
     * Rnd location.
     * 
     * @param rnd
     *            the rnd
     * @return a random location with coordinates between -100 and 100
     */
    public static ILocation rndLocation(Random rnd) {
        return new PointLocation((0.5 - rnd.nextDouble()) * 200, (0.5 - rnd.nextDouble()) * 200);
    }

    /**
     * Generate request.
     * 
     * @param maxDem
     *            the max dem
     * @param horizon
     *            the horizon
     * @param rndPoint
     * @param id
     * @param nRequests
     * @return the request
     */
    public static Request generateRequest(int maxDem, int horizon, boolean rndPoint, int id, int nRequests) {
        ILocation loc;

        if (rndPoint) {
            loc = rndLocation(rnd);
        } else {
            loc = new PointLocation(Math.cos(2 * Math.PI * id / nRequests), Math.sin(2 * Math.PI * id / nRequests));
        }

        Request r = new Request(id, new Node(id, "Node" + (id), loc));

        r.setAttribute(RequestAttributeKey.DEMAND, new DeterministicDemand(1 + rnd.nextInt(maxDem)));

        r.setAttribute(RequestAttributeKey.RELEASE_DATE,
                new DefaultReleaseDate(new DiscreteTimeStamp(rnd.nextInt(horizon))));
        return r;
    }
}
