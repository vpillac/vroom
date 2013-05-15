/**
 * 
 */
package vroom.common.modeling.vrprep;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashSet;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.attributes.DeterministicDemand;
import vroom.common.modeling.dataModel.attributes.Duration;
import vroom.common.modeling.dataModel.attributes.IDemand;
import vroom.common.modeling.dataModel.attributes.ILocation;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.VehicleAttributeKey;
import vroom.common.modeling.vrprep.Instance.Fleet;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
import vroom.common.modeling.vrprep.Instance.Info;
import vroom.common.modeling.vrprep.Instance.Info.Contributor;
import vroom.common.modeling.vrprep.Instance.Network;
import vroom.common.modeling.vrprep.Instance.Network.Descriptor;
import vroom.common.modeling.vrprep.Instance.Network.Links;
import vroom.common.modeling.vrprep.Instance.Network.Links.Link;
import vroom.common.modeling.vrprep.Instance.Network.Nodes;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Instance.Requests;
import vroom.common.modeling.vrprep.Instance.Requests.Request;
import vroom.common.modeling.vrprep.Location.Euclidean;
import vroom.common.modeling.vrprep.Tw.End;
import vroom.common.modeling.vrprep.Tw.Start;
import vroom.common.utilities.Utilities;

/**
 * The class <code>VRPRepFactory</code> provides factory methods to convert objects between the VroomModeling data model
 * and the VRPRep JAXB data model
 * <p>
 * Creation date: Jun 22, 2012 - 11:35:46 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPRepFactory extends ObjectFactory {

    public Vehicle convertSingleVehicle(vroom.common.modeling.dataModel.Vehicle vehicle) {
        Vehicle v = createInstanceFleetVehicle();

        v.setNumber(BigInteger.valueOf(1));
        v.setType(BigInteger.valueOf(vehicle.getID()));

        Depot d = vehicle.getAttribute(VehicleAttributeKey.DEPOT);
        if (d != null) {
            v.setDepartureNode(BigInteger.valueOf(d.getID()));
            v.setArrivalNode(BigInteger.valueOf(d.getID()));
        }

        for (int i = 0; i < vehicle.getCompartmentCount(); i++) {
            v.getCapacity().add(vehicle.getCapacity(i));
        }

        return v;
    }

    public Fleet convertFleet(vroom.common.modeling.dataModel.Fleet<?> fleet) {
        Fleet f = createInstanceFleet();

        if (fleet.isHomogeneous()) {
            Vehicle v = convertSingleVehicle(fleet.getVehicle());
            if (fleet.isUnlimited())
                v.setNumber(BigInteger.valueOf(-1));
            else
                v.setNumber(BigInteger.valueOf(fleet.size()));
            f.getVehicle().add(v);
        } else {
            for (vroom.common.modeling.dataModel.Vehicle v : fleet) {
                f.getVehicle().add(convertSingleVehicle(v));
            }
        }

        return f;
    }

    public Location convertLocation(ILocation location) {
        if (Double.isNaN(location.getX()))
            return null;
        // FIXME check coordinate type
        Location loc = createLocation();
        Euclidean coords = createLocationEuclidean();
        loc.setEuclidean(coords);
        coords.setCx(location.getX());
        coords.setCy(location.getY());
        return loc;
    }

    public Node convertNode(vroom.common.modeling.dataModel.Node node) {
        Node n = createInstanceNetworkNodesNode();
        n.setId(BigInteger.valueOf(node.getID()));
        Location loc = convertLocation(node.getLocation());
        if (loc != null)
            n.setLocation(loc);

        if (Depot.class.isAssignableFrom(node.getClass()))
            n.setType(BigInteger.valueOf(0));
        else
            n.setType(BigInteger.valueOf(1));

        return n;
    }

    /**
     * Convert a simple request (supported: single node, multiple deterministic demands, tw)
     * 
     * @param request
     * @return the converted request
     */
    public Request convertRequest(IVRPRequest request) {
        Request r = createInstanceRequestsRequest();

        r.setId(BigInteger.valueOf(request.getID()));

        // Node
        r.setNode(BigInteger.valueOf(request.getNode().getID()));

        // Demands
        IDemand dem = request.getDemandAttribute();
        if (dem != null) {
            for (int p = 0; p < dem.getProductCount(); p++) {
                Demand d = createDemand();
                r.getDemand().add(d);

                if (DeterministicDemand.class.isAssignableFrom(dem.getClass()))
                    d.getContent().add(Utilities.format(dem.getDemand(p)));
                else
                    throw new UnsupportedOperationException("Unsupported demand " + dem);
                d.setType(BigInteger.valueOf(p));
            }
        }

        // Time window
        ITimeWindow rtw = request.getAttribute(RequestAttributeKey.TIME_WINDOW);
        if (rtw != null)
            r.getTw().add(convertTimeWindow(rtw));

        // Service time
        Duration rst = request.getAttribute(RequestAttributeKey.SERVICE_TIME);
        if (rst != null && rst.getDuration() > 0) {
            Time st = createTime();
            st.getContent().add(rst.toString());
            r.setServiceTime(st);
        }

        return r;
    }

    public Tw convertTimeWindow(ITimeWindow rtw) {
        Tw tw = createTw();
        Start twstart = createTwStart();
        twstart.setIsHard(!rtw.isSoftStart());
        twstart.setContent(Utilities.format(rtw.startAsDouble()));
        tw.setStart(twstart);

        End twend = createTwEnd();
        twend.setIsHard(!rtw.isSoftEnd());
        twend.setContent(Utilities.format(rtw.endAsDouble()));
        tw.setEnd(twend);

        return tw;
    }

    /**
     * Factory method to create the information related to an instance
     * 
     * @param contribName
     * @param contribEmail
     * @param instanceName
     * @param problemName
     * @param reference
     * @return
     */
    public Info createInstanceInfo(String contribName, String contribEmail, String instanceName,
            String problemName, String reference) {
        Info info = createInstanceInfo();
        Contributor contrib = createInstanceInfoContributor();
        contrib.setName(contribName);
        contrib.setEmail(contribEmail);
        info.setContributor(contrib);
        info.setName(instanceName);
        info.setProblem(problemName);
        info.setReference(reference);
        return info;
    }

    /**
     * Convert a {@link IVRPInstance} into an {@link Instance}
     * 
     * @param instance
     * @return the converted instance
     */
    public Instance convertInstance(IVRPInstance instance) {
        Instance i = createInstance();

        // Network
        // ------------------------
        Network net = createInstanceNetwork();
        i.setNetwork(net);
        Nodes nodes = createInstanceNetworkNodes();
        net.setNodes(nodes);
        // - Nodes
        HashSet<INodeVisit> addedNodes = new HashSet<>();
        for (Depot d : instance.getDepots()) {
            nodes.getNode().add(convertNode(d));
        }
        for (INodeVisit n : instance.getNodeVisits()) {
            if (!addedNodes.contains(n.getNode())) {
                Node node = convertNode(n.getNode());
                nodes.getNode().add(node);
                addedNodes.add(n);
            }
        }
        // - Descriptor
        Descriptor desc = createInstanceNetworkDescriptor();
        net.setDescriptor(desc);
        // Euclidean distance
        desc.setDistanceType(instance.getCostDelegate().getDistanceType());
        desc.setIsComplete(true);
        if (instance.getCostDelegate().getPrecision() != Double.MAX_VALUE
                && instance.getCostDelegate().getRoundingMethod() != RoundingMode.UNNECESSARY)
            desc.setRoundingRule(String.format("%s[%s]", instance.getCostDelegate()
                    .getRoundingMethod().toString(), instance.getCostDelegate().getPrecision()));
        // -----------------------------------------------------

        // Fleet
        // -----------------------------------------------------
        Fleet fleet = convertFleet(instance.getFleet());
        i.setFleet(fleet);
        // -----------------------------------------------------

        // Requests
        // -----------------------------------------------------
        Requests reqs = createInstanceRequests();
        i.setRequests(reqs);
        for (IVRPRequest r : instance.getRequests()) {
            reqs.getRequest().add(convertRequest(r));
        }
        // -----------------------------------------------------

        if (instance.getCostDelegate().getDistanceType().contains("EXPLICIT")) {
            Links links = createInstanceNetworkLinks();
            i.getNetwork().setLinks(links);
            for (INodeVisit m : instance.getNodeVisits()) {
                for (INodeVisit n : instance.getNodeVisits()) {
                    if (m == n)
                        continue;
                    double distance = instance.getCostDelegate().getDistance(m, n);
                    if (!Double.isInfinite(distance) && !Double.isNaN(distance)) {
                        Link edge = createInstanceNetworkLinksLink();
                        edge.setTail(BigInteger.valueOf(m.getID()));
                        edge.setHead(BigInteger.valueOf(n.getID()));
                        edge.setDirected(false);
                        edge.setCost(distance);
                        links.getLink().add(edge);
                    }
                }
            }
        }

        return i;
    }
}
