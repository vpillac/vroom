package vroom.trsp.io;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBException;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.vrprep.Demand;
import vroom.common.modeling.vrprep.Instance;
import vroom.common.modeling.vrprep.Instance.Fleet;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
import vroom.common.modeling.vrprep.Instance.Info;
import vroom.common.modeling.vrprep.Instance.Network;
import vroom.common.modeling.vrprep.Instance.Network.Descriptor;
import vroom.common.modeling.vrprep.Instance.Network.Links;
import vroom.common.modeling.vrprep.Instance.Network.Links.Link;
import vroom.common.modeling.vrprep.Instance.Network.Nodes;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Instance.Requests;
import vroom.common.modeling.vrprep.Instance.Requests.Request;
import vroom.common.modeling.vrprep.Skill;
import vroom.common.modeling.vrprep.Time;
import vroom.common.modeling.vrprep.Tool;
import vroom.common.modeling.vrprep.VRPRepFactory;
import vroom.common.modeling.vrprep.VRPRepJAXBUtilities;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.trsp.datamodel.TRSPDistTimeMatrix;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.util.TRSPLogging;

public class TRSPVRPRepPersistenceHelper implements ITRSPPersistenceHelper {

    /** the compression flag **/
    private boolean mCompress;

    /**
     * Getter for the compression flag
     * 
     * @return {@code true} if output file should be compressed in a zip
     */
    public boolean isCompress() {
        return this.mCompress;
    }

    /**
     * Setter for the compression flag
     * 
     * @param compressed
     *            {@code true} if output file should be compressed in a zip
     */
    public void setCompress(boolean compressed) {
        this.mCompress = compressed;
    }

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

    public TRSPVRPRepPersistenceHelper() {
        mFactory = new VRPRepFactory();
        mDefaultInfo = mFactory.createInstanceInfo("Victor Pillac", "vpillac@mines-nantes.fr", "",
                "TRSP", "");
        mCompress = true;
    }

    @Override
    public TRSPInstance readInstance(File file, Object... params) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean writeInstance(TRSPInstance instance, File file) throws JAXBException,
            IOException {
        Instance ins = convertInstance(instance);

        VRPRepJAXBUtilities.writeInstance(ins, file, isCompress());

        // Check the XML
        // Schema schema;
        // try {
        // schema = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema").newSchema(
        // new File("../VroomModeling/vrprep/VRPRep.xsd"));
        // Validator validator = schema.newValidator();
        // validator.validate(new SAXSource(new InputSource(new FileReader(file))));
        // } catch (SAXException | IOException e) {
        // TRSPLogging.getBaseLogger().exception("VRPRepPersistenceHelper.writeInstance", e);
        // }

        return true;
    }

    /**
     * Converts a {@link TRSPInstance} to an {@link Instance} to be stored in XML
     * 
     * @param trspInstance
     * @return the converted instance
     */
    public Instance convertInstance(TRSPInstance trspInstance) {
        Instance instance = mFactory.createInstance();

        // Info
        mDefaultInfo.setName(trspInstance.getName());
        instance.setInfo(mDefaultInfo);

        // Network
        // -----------------------------------------------------
        Network net = mFactory.createInstanceNetwork();
        instance.setNetwork(net);
        Nodes nodes = mFactory.createInstanceNetworkNodes();
        net.setNodes(nodes);
        // -Depots
        for (Depot d : trspInstance.getDepots()) {
            Node n = mFactory.convertNode(d);
            n.setType(BigInteger.valueOf(trspInstance.isMainDepot(d.getID()) ? 0 : 1));
            nodes.getNode().add(n);
        }
        // -Request nodes
        for (TRSPRequest r : trspInstance.getRequests()) {
            Node n = mFactory.convertNode(r.getNode());
            n.setType(BigInteger.valueOf(2));
            nodes.getNode().add(n);
        }
        // -Links
        Descriptor desc = mFactory.createInstanceNetworkDescriptor();
        net.setDescriptor(desc);
        if (TRSPDistTimeMatrix.class.isAssignableFrom(trspInstance.getCostDelegate().getClass())) {
            TRSPDistTimeMatrix cd = (TRSPDistTimeMatrix) trspInstance.getCostDelegate();
            // Explicit edge definition
            desc.setDistanceType("EXPLICIT");
            desc.setIsComplete(true);
            desc.setRoundingRule("none");

            Links links = mFactory.createInstanceNetworkLinks();
            net.setLinks(links);
            for (int i = 0; i < nodes.getNode().size(); i++) {
                for (int j = i + 1; j < nodes.getNode().size(); j++) {
                    Link edge = mFactory.createInstanceNetworkLinksLink();
                    links.getLink().add(edge);

                    Node tail = nodes.getNode().get(i);
                    Node head = nodes.getNode().get(j);

                    edge.setTail(tail.getId());
                    edge.setHead(head.getId());
                    edge.setDirected(false);
                    edge.setLength(cd.getDistance(tail.getId().intValue(), head.getId().intValue()));
                    Time tt = mFactory.createTime();
                    tt.getContent().add(Utilities.format(cd.getTravelTime(i, j, null)));
                    edge.setTime(tt);
                }
            }
        } else {
            // Euclidean distance
            desc.setDistanceType("EUC_2D");
            desc.setIsComplete(true);
            desc.setRoundingRule("none");
        }
        // -----------------------------------------------------

        // Fleet
        // -----------------------------------------------------
        Fleet fleet = mFactory.createInstanceFleet();
        instance.setFleet(fleet);
        for (Technician t : trspInstance.getFleet()) {
            fleet.getVehicle().add(convertTechnician(t));
        }
        // -----------------------------------------------------

        // Requests
        // -----------------------------------------------------
        Requests reqs = mFactory.createInstanceRequests();
        instance.setRequests(reqs);
        for (TRSPRequest r : trspInstance.getRequests()) {
            reqs.getRequest().add(convertRequest(r, trspInstance));
        }
        // -----------------------------------------------------

        return instance;
    }

    public Vehicle convertTechnician(Technician t) {
        Vehicle v = mFactory.convertSingleVehicle(t);

        // Home
        v.setDepartureNode(BigInteger.valueOf(t.getHome().getID()));
        v.setArrivalNode(BigInteger.valueOf(t.getHome().getID()));

        // Skills
        for (int i : t.getSkillSet()) {
            Skill s = mFactory.createSkill();
            s.setId(BigInteger.valueOf(i));
            v.getSkill().add(s);
        }

        // Tools
        for (int i : t.getToolSet()) {
            Tool tool = mFactory.createTool();
            tool.setId(BigInteger.valueOf(i));
            v.getTool().add(tool);
        }

        // Spare parts

        return v;
    }

    /**
     * Convert a simple request (supported: single node, multiple deterministic demands, tw)
     * 
     * @param request
     * @return the converted request
     */
    public Request convertRequest(TRSPRequest request, TRSPInstance instance) {
        Request r = mFactory.createInstanceRequestsRequest();

        r.setId(BigInteger.valueOf(request.getID() - instance.getDepotCount()));

        // Node
        r.setNode(BigInteger.valueOf(request.getNode().getID()));

        // Skills
        for (int i : request.getSkillSet()) {
            Skill s = mFactory.createSkill();
            s.setId(BigInteger.valueOf(i));
            r.getSkill().add(s);
        }

        // Tools
        for (int i : request.getToolSet()) {
            Tool t = mFactory.createTool();
            t.setId(BigInteger.valueOf(i));
            r.getTool().add(t);
        }

        // Spare parts
        for (int p = 0; p < request.getSparePartRequirements().length; p++) {
            Demand d = mFactory.createDemand();
            r.getDemand().add(d);

            d.getContent().add("" + request.getSparePartRequirement(p));
            d.setType(BigInteger.valueOf(p));
        }

        // Time window
        r.getTw().add(mFactory.convertTimeWindow(request.getTimeWindow()));

        // Service time
        Time st = mFactory.createTime();
        st.getContent().add(vroom.common.utilities.Utilities.format(request.getServiceTime()));
        r.setServiceTime(st);

        return r;
    }

    public static void main(String[] args) {
        TRSPLogging.setupRootLogger(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, false);

         String src = "../Instances/trsp/pillac/crew25";
         String pattern = ".+100_25.+txt";
         String dest = "../Instances/vrprep/trsp/pillac/%s.xml";
         String ref =
         "Pillac, V.; Gueret, C. & Medaglia, A. L. A parallel matheuristic for the Technician Routing and Scheduling Problem Optimization Letters, 2012, in press, doi:10.1007/s11590-012-0567-4";

        // String src = "../Instances/trsp/pillac2/";
        // String pattern = ".+txt";
        // String dest = "../Instances/vrprep/trsp/pillac2/%s.xml";
        // String ref =
        // "Pillac, V., Object oriented modules for dynamic vehicle routing, Ph.D. thesis, Ecole des Mines de Nantes, Universidad de Los Andes, 2012";

//        String src = "../Instances/vrprep/trsp/pillac2/";
//        String pattern = ".+zip";

        PillacSimplePersistenceHelper reader = new PillacSimplePersistenceHelper();
         TRSPVRPRepPersistenceHelper writer = new TRSPVRPRepPersistenceHelper();
         writer.getDefaultInfo().setReference(ref);
         writer.setCompress(false);

        try {
            List<File> files = Utilities.listFiles(src, pattern);

            Stopwatch timer = new Stopwatch();
            timer.start();
            for (File f : files) {
                Stopwatch t = new Stopwatch();
                 System.out.println("Converting " + f.getName());
                 TRSPInstance trspInstance = reader.readInstance(f);

                 writer.writeInstance(trspInstance,
                 new File(String.format(dest, trspInstance.getName())));
//                t.reset();
//                t.start();
//                Instance i = VRPRepJAXBUtilities.readInstance(f);
//                t.stop();
//                System.out.println(t.readTimeMS());
            }
            timer.stop();

            System.out.println("Total time: " + timer.readTimeMS());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("TERMINATED");
    }
}
