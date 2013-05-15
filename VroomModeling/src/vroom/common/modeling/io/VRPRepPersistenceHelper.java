/**
 * 
 */
package vroom.common.modeling.io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.DistanceMatrix;
import vroom.common.modeling.dataModel.Fleet;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.StaticInstance;
import vroom.common.modeling.dataModel.VehicleRoutingProblemDefinition;
import vroom.common.modeling.dataModel.attributes.ILocation;
import vroom.common.modeling.dataModel.attributes.PointLocation;
import vroom.common.modeling.util.CostCalculationDelegate;
import vroom.common.modeling.util.EuclidianDistance;
import vroom.common.modeling.vrprep.Demand;
import vroom.common.modeling.vrprep.Instance;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
import vroom.common.modeling.vrprep.Instance.Info;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Instance.Requests.Request;
import vroom.common.modeling.vrprep.Location;
import vroom.common.modeling.vrprep.VRPRepFactory;
import vroom.common.modeling.vrprep.VRPRepJAXBUtilities;
import vroom.common.utilities.GeoTools.CoordinateSytem;
import vroom.common.utilities.logging.Logging;

/**
 * The class <code>VRPRepPersistenceHelper</code> is an implementation of {@link IPersistenceHelper} for the VRPRep xml
 * format
 * <p>
 * Creation date: Jun 22, 2012 - 4:57:28 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPRepPersistenceHelper implements IPersistenceHelper<File> {

    public Info mDefaultInfo;

    /**
     * Returns the default information associated with an instance
     * 
     * @return the default information associated with an instance
     */
    public Info getDefaultInfo() {
        return mDefaultInfo;
    }

    private final VRPRepFactory mFactory;

    /**
     * Creates a new <code>VRPRepPersistenceHelper</code>
     */
    public VRPRepPersistenceHelper() {
        mFactory = new VRPRepFactory();
        mDefaultInfo = mFactory.createInstanceInfo("Unknown", "na", "", "na", "");
    }

    @Override
    public IVRPInstance readInstance(File input, Object... params) throws Exception {
        Instance vrpIns = VRPRepJAXBUtilities.readInstance(input);

        VehicleRoutingProblemDefinition routingProblem = convertProbDef(vrpIns);
        List<Depot> depots = convertDepotList(vrpIns);
        Fleet<?> fleet = convertFleet(vrpIns);
        CostCalculationDelegate costHelper = convertCostDel(vrpIns);
        IVRPInstance instance = new StaticInstance(vrpIns.getInfo().getName(), 0, fleet, depots,
                routingProblem, costHelper);

        instance.addRequests(convertRequests(vrpIns));

        instance.setCostHelper(new DistanceMatrix(instance));

        return instance;
    }

    private Collection<IVRPRequest> convertRequests(Instance vrpIns) {
        List<IVRPRequest> requests = new LinkedList<>();
        for (Request r : vrpIns.getRequests().getRequest()) {
            IVRPRequest req = new vroom.common.modeling.dataModel.Request(r.getId().intValue(),
                    convertNode(findNode(vrpIns, r.getNode().intValue())));

            double demands[] = new double[r.getDemand().size()];
            int i = 0;
            for (Demand d : r.getDemand()) {
                demands[i++] = Double.valueOf((String) d.getContent().get(0));
            }
            req.setDemands(demands);

            requests.add(req);
        }
        return requests;
    }

    private Node findNode(Instance vrpIns, int id) {
        for (Node n : vrpIns.getNetwork().getNodes().getNode()) {
            if (n.getId().intValue() == id)
                return n;
        }
        throw new IllegalStateException();
    }

    private vroom.common.modeling.dataModel.Node convertNode(Node n) {
        return new vroom.common.modeling.dataModel.Node(n.getId().intValue(),
                convertLocation(n.getLocation()));
    }

    private CostCalculationDelegate convertCostDel(Instance vrpIns) {
        return new EuclidianDistance();
    }

    private Fleet<?> convertFleet(Instance vrpIns) {
        // Fleet<vroom.common.modeling.dataModel.Vehicle> fleet = Fleet.
        List<vroom.common.modeling.dataModel.Vehicle> vehicles = new LinkedList<>();
        int id = 0;
        for (Vehicle v : vrpIns.getFleet().getVehicle()) {
            int n = v.getNumber().intValue();
            double[] capacities = new double[v.getCapacity().size()];
            for (int i = 0; i < capacities.length; i++) {
                capacities[i] = v.getCapacity().get(i);
            }

            double fixedCost = v.getFixedCost().isEmpty() ? 0 : v.getFixedCost().get(0)
                    .doubleValue();
            double varCost = v.getVariableCost().isEmpty() ? 1 : v.getVariableCost().get(0)
                    .doubleValue();

            vroom.common.modeling.dataModel.Vehicle veh = new vroom.common.modeling.dataModel.Vehicle(
                    id, "veh" + id, fixedCost, varCost, 1, capacities);
            if (n < 0)
                return Fleet.newUnlimitedFleet(veh);
            else if (n > 1)
                return Fleet.newHomogenousFleet(n, veh);

            vehicles.add(veh);

            id++;
        }
        return Fleet.newHeterogenousFleet(vehicles);
    }

    private List<Depot> convertDepotList(Instance vrpIns) {
        List<Depot> depots = new LinkedList<>();
        for (Node n : vrpIns.getNetwork().getNodes().getNode()) {
            if (n.getType().intValue() == 0) {
                depots.add(new Depot(n.getId().intValue(), convertLocation(n.getLocation())));
            }
        }
        return depots;
    }

    private ILocation convertLocation(Location location) {
        if (location.getEuclidean() != null) {
            double x = location.getEuclidean().getCx();
            double y = location.getEuclidean().getCy();
            // Double z = location.getEuclidean().getCz();

            return new PointLocation(x, y);
        } else if (location.getGPSCoordinates() != null) {
            double x = location.getGPSCoordinates().getLat();
            double y = location.getGPSCoordinates().getLon();
            // Double z = location.getEuclidean().getCz();

            return new PointLocation(CoordinateSytem.LAT_LON_DEC_DEG, x, y);
        }
        return null;
    }

    private VehicleRoutingProblemDefinition convertProbDef(Instance vrpIns) {
        VehicleRoutingProblemDefinition def = VehicleRoutingProblemDefinition.VRP;
        try {
            def = (VehicleRoutingProblemDefinition) VehicleRoutingProblemDefinition.class.getField(
                    vrpIns.getInfo().getProblem()).get(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    @Override
    public boolean writeInstance(IVRPInstance instance, File output, Object params)
            throws IOException {

        Boolean compress = true;
        if (params != null && params instanceof Boolean)
            compress = (Boolean) params;

        Instance ins = mFactory.convertInstance(instance);

        ins.setInfo(getDefaultInfo());

        try {
            VRPRepJAXBUtilities.writeInstance(ins, output, compress);
        } catch (JAXBException e) {
            Logging.getBaseLogger().exception("VRPRepPersistenceHelper.writeInstance", e);
            return false;
        }

        return true;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

}
