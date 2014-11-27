package vroom.trsp.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.vrprep.model.instance.Instance;
import org.vrprep.model.instance.Instance.Drivers;
import org.vrprep.model.instance.Instance.Drivers.DriverProfile;
import org.vrprep.model.instance.Instance.Drivers.DriverProfile.Skill;
import org.vrprep.model.instance.Instance.Fleet;
import org.vrprep.model.instance.Instance.Fleet.VehicleProfile;
import org.vrprep.model.instance.Instance.Info;
import org.vrprep.model.instance.Instance.Network;
import org.vrprep.model.instance.Instance.Network.Nodes.Node;
import org.vrprep.model.instance.Instance.Requests;
import org.vrprep.model.instance.Instance.Requests.Request;
import org.vrprep.model.instance.Instance.Resources;
import org.vrprep.model.instance.Instance.Resources.Resource;
import org.vrprep.model.instance.ObjectFactory;
import org.xml.sax.SAXException;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.trsp.datamodel.ITRSPNode;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.util.TRSPLogging;

public class TRSPVRPRepConverterV030 {

    public static Instance convertInstance(TRSPInstance instance, String dataset) {
        ObjectFactory f = new ObjectFactory();
        Instance ins = f.createInstance();

        // INSTANCE INFO
        // ================================================
        Info info = f.createInstanceInfo();
        ins.setInfo(info);
        info.setName(instance.getName());
        info.setDataset(dataset);
        // ================================================

        // RESOURCES
        // ================================================
        Resources res = f.createInstanceResources();
        Resource[] toolMap = new Resource[instance.getToolCount()];
        Resource[] spareMap = new Resource[instance.getSpareCount()];
        ins.setResources(res);
        int id = 0;
        for (int k = 0; k < instance.getToolCount(); k++) {
            Resource r = f.createInstanceResourcesResource();
            toolMap[k] = r;
            res.getResource().add(r);
            r.setId(BigInteger.valueOf(id++));
            r.setName("tool-" + k);
            r.setRenewable(true);
            r.setValue(Double.POSITIVE_INFINITY);
        }
        for (int k = 0; k < instance.getSpareCount(); k++) {
            Resource r = f.createInstanceResourcesResource();
            spareMap[k] = r;
            res.getResource().add(r);
            r.setId(BigInteger.valueOf(id++));
            r.setName("spare-part-" + k);
            r.setRenewable(false);
            r.setValue(Double.POSITIVE_INFINITY);
        }
        // ================================================

        // FLEET
        // ================================================
        Fleet fleet = f.createInstanceFleet();
        ins.setFleet(fleet);
        Drivers drivers = f.createInstanceDrivers();
        ins.setDrivers(drivers);
        for (int k = 0; k < instance.getFleet().size(); k++) {
            Technician tech = instance.getTechnician(k);
            VehicleProfile p = f.createInstanceFleetVehicleProfile();
            fleet.getVehicleProfile().add(p);
            DriverProfile d = f.createInstanceDriversDriverProfile();
            drivers.getDriverProfile().add(d);

            p.setNumber(1);
            p.setFixCost(tech.getFixedCost());
            p.setCostXDistance(tech.getVariableCost());
            p.setType(BigInteger.valueOf(k));
            // FIXME set dep/arrival node
            p.getDepartureNode().add(BigInteger.valueOf(tech.getHome().getID()));
            p.getArrivalNode().add(BigInteger.valueOf(tech.getHome().getID()));
            for (Integer tool : tech.getToolSet()) {
                org.vrprep.model.instance.Instance.Fleet.VehicleProfile.Resource toolRes = f
                        .createInstanceFleetVehicleProfileResource();
                toolRes.setId(toolMap[tool].getId().intValue());
                toolRes.setMax(1);
                p.getResource().add(toolRes);
            }
            for (int s = 0; s < instance.getSpareCount(); s++) {
                int qty = tech.getAvailableSpareParts(s);
                if (qty > 0) {
                    org.vrprep.model.instance.Instance.Fleet.VehicleProfile.Resource spareRes = f
                            .createInstanceFleetVehicleProfileResource();
                    spareRes.setId(spareMap[s].getId().intValue());
                    spareRes.setStart(Double.valueOf(qty));
                    spareRes.setMax(Double.POSITIVE_INFINITY);
                    p.getResource().add(spareRes);
                }
            }

            d.setType(k);
            d.getCompatibleVehicleType().add(k);
            // if (k == 0)
            for (int s = 0; s < instance.getSkillCount(); s++) {
                if (tech.getSkillSet().hasAttribute(s)) {
                    Skill skill = f.createInstanceDriversDriverProfileSkill();
                    skill.setId(BigInteger.valueOf(s));
                    d.getSkill().add(skill);
                }
            }
        }

        // ================================================

        // NETWORK
        // ================================================
        Network net = f.createInstanceNetwork();
        Map<Integer, Node> nodeMap = new HashMap<>();

        ins.setNetwork(net);
        net.setNodes(f.createInstanceNetworkNodes());
        // net.setLinks(f.createInstanceNetworkLinks());
        // Convert nodes
        for (int i = 0; i < instance.size(); i++) {
            ITRSPNode o = instance.getTRSPNode(i);
            Node n = f.createInstanceNetworkNodesNode();
            Node nn = nodeMap.put(i, n);
            if (nn != null) {
                throw new UnsupportedOperationException("Node with same id already created " + nn);
            }
            n.setId(BigInteger.valueOf(o.getID()));
            n.setCx(o.getNode().getLocation().getX());
            n.setCy(o.getNode().getLocation().getY());
            n.setType(BigInteger.valueOf(o.getType().toInt()));
            net.getNodes().getNode().add(n);
        }

        // Distances
        net.setEuclidean(f.createInstanceNetworkEuclidean());
        net.setDecimals(14);
        // ================================================

        // REQUESTS
        // ================================================
        Requests requests = f.createInstanceRequests();
        ins.setRequests(requests);
        id = 0;
        for (TRSPRequest r : instance.getRequests()) {
            Request rr = f.createInstanceRequestsRequest();
            requests.getRequest().add(rr);
            rr.setId(BigInteger.valueOf(id++));
            rr.setNode(BigInteger.valueOf(r.getNode().getID()));
            for (Integer tool : r.getToolSet()) {
                org.vrprep.model.instance.Instance.Requests.Request.Resource toolRes = f
                        .createInstanceRequestsRequestResource();
                toolRes.setId(toolMap[tool].getId().intValue());
                toolRes.setValue(1);
                rr.getResource().add(toolRes);
            }
            for (int s = 0; s < instance.getSpareCount(); s++) {
                int qty = r.getSparePartRequirement(s);
                if (qty > 0) {
                    org.vrprep.model.instance.Instance.Requests.Request.Resource spareRes = f
                            .createInstanceRequestsRequestResource();
                    spareRes.setId(spareMap[s].getId().intValue());
                    spareRes.setValue(Double.valueOf(qty));
                    rr.getResource().add(spareRes);
                }
            }
            for (int s : r.getSkillSet()) {
                // skill = f.createinstancere
                rr.getSkill().add(s);
            }
        }
        // ================================================

        return ins;
    }

    /**
     * Write an instance in a file
     * 
     * @param instance
     *            the instance to be written
     * @param destFile
     *            the destination file
     * @param compress
     *            {@code true} if the instance should be compressed, in which case the suffix {@code  .zip} will be added
     *            to the file name
     * @throws JAXBException
     * @throws IOException
     * @throws SAXException
     */
    public static void writeInstance(Instance instance, File destFile, boolean compress)
            throws JAXBException, IOException, SAXException {
        JAXBContext context = JAXBContext.newInstance(Instance.class.getPackage().getName());

        Marshaller marshaller = context.createMarshaller();
        // Nicelly format the output XML
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));

        // Schema schema = sf.newSchema(new URL(
        // "http://www.beta-version.vrp-rep.org/schemas/download/vrp-rep-instance.xsd"));
        //
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(new File("lib/instance.xsd").getAbsoluteFile());
        // marshaller.setSchema(schema);

        // Write the instance
        destFile.getParentFile().mkdirs();
        OutputStream os;
        if (compress) {
            String zipFile = destFile.getAbsolutePath().endsWith(".zip") ? destFile
                    .getAbsolutePath() : destFile.getAbsolutePath() + ".zip";
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry ze = new ZipEntry(destFile.getName());
            zos.putNextEntry(ze);

            os = zos;
        } else {
            os = new FileOutputStream(destFile);
        }

        marshaller.marshal(instance, os);

        os.flush();
        os.close();
    }

    public static void main(String[] args) throws IOException, JAXBException, SAXException {
        TRSPLogging.setupRootLogger(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, false);

        String src = "../Instances/trsp/pillac/crew25";
        String pattern = ".+100_25.+txt";
        // File destDir = new File("../Instances/vrprep_0.3.0/trsp/pillac");
        File destDir = new File("./vrprep");
        destDir.mkdirs();
        String dest = destDir.getAbsolutePath() + "/%s.xml";

        String ref = "Pillac, V.; Gueret, C. & Medaglia, A. L. A parallel matheuristic for the Technician Routing and Scheduling Problem Optimization Letters, 2012, in press, doi:10.1007/s11590-012-0567-4";
        String dataset = "Pillac et al. 2012";
        // String src = "../Instances/trsp/pillac2/";
        // String pattern = ".+txt";
        // String dest = "../Instances/vrprep/trsp/pillac2/%s.xml";
        // String ref =
        // "Pillac, V., Object oriented modules for dynamic vehicle routing, Ph.D. thesis, Ecole des Mines de Nantes, Universidad de Los Andes, 2012";

        // String src = "../Instances/vrprep/trsp/pillac2/";
        // String pattern = ".+zip";

        PillacSimplePersistenceHelper reader = new PillacSimplePersistenceHelper();

        List<File> files = Utilities.listFiles(src, pattern);

        for (File f : files) {
            TRSPInstance trspInstance = reader.readInstance(f);
            File destFile = new File(String.format(dest, trspInstance.getName()));
            System.out.println("Converting " + f.getName() + " to " + destFile.getPath());

            Instance instance = convertInstance(trspInstance, dataset);

            writeInstance(instance, destFile, false);
            // t.reset();
            // t.start();
            // Instance i = VRPRepJAXBUtilities.readInstance(f);
            // t.stop();
            // System.out.println(t.readTimeMS());

            // break;
        }

        System.out.println("TERMINATED");
    }
}
