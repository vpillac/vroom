package vroom.common.modeling.vrprep.xmlPrototyper;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.swing.DefaultListModel;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import vroom.common.modeling.vrprep.Compartment;
import vroom.common.modeling.vrprep.Custom;
import vroom.common.modeling.vrprep.Demand;
import vroom.common.modeling.vrprep.Instance;
import vroom.common.modeling.vrprep.Instance.Fleet;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle.WorkloadProfile;
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
import vroom.common.modeling.vrprep.Location;
import vroom.common.modeling.vrprep.Location.Euclidean;
import vroom.common.modeling.vrprep.Location.GPSCoordinates;
import vroom.common.modeling.vrprep.NormalVariable;
import vroom.common.modeling.vrprep.ObjectFactory;
import vroom.common.modeling.vrprep.PoissonVariable;
import vroom.common.modeling.vrprep.SpeedProfile;
import vroom.common.modeling.vrprep.SpeedProfile.SpeedInterval;
import vroom.common.modeling.vrprep.Time;
import vroom.common.modeling.vrprep.Tw;
import vroom.common.modeling.vrprep.VRPRepJAXBUtilities;

/**
 * Class used to generate JAXB objects based on the Prototyper's GUI values
 * @author Maxim Hoskins s<a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public final class XMLBinder extends ObjectFactory{

	/**
	 * A {@link Document} object that will be used to create {@link Element elements}
	 */
	private final Document            document;

	/**
	 * object factory instance element
	 * 
	 * @see vroom.common.modeling.vrprep.Instance
	 */
	private Instance                  instance;
	/**
	 * object factory instance.info element
	 * 
	 * @see vroom.common.modeling.vrprep.Instance.Info
	 */
	private Info                      info;
	/**
	 * object factory instance.requests element
	 * 
	 * @see vroom.common.modeling.vrprep.Instance.Requests
	 */
	private Requests                  requests;
	/**
	 * object factory instance.network element
	 * 
	 * @see vroom.common.modeling.vrprep.Instance.Network
	 */
	private Network                   network;
	/**
	 * object factory instance.fleet element
	 * 
	 * @see vroom.common.modeling.vrprep.Instance.Fleet
	 */
	private Fleet                     fleet;

	/** * Link to info questions panel */
	private InfoQuestions             infoQuestions;
	/** * Link to request questions panel */
	private RequestQuestions          requestQuestions;
	/** * Link to network questions panel */
	private NetworkQuestions          networkQuestions;
	/** * Link to fleet questions panel */
	private FleetQuestions            fleetQuestions;

	/** * Node ID increment to avoid duplicate IDs */
	private long                      nodeId;
	/** * Request ID increment to avoid duplicate IDs */
	private long                      requestId;
	/** * Link ID increment to avoid duplicate IDs */
	private long                      linkId;
	/** * Capacity type increment to avoid duplicate types */
	private long                      compartmentType;
	/** * Demand type increment to avoid duplicate types */
	private long                      demandType;
	/** * Vehicle type increment to avaoid duplicate types */
	private long 					  vehicleType;

	/** * Singleton instance of XMLBinder */
	// volatile avoids the case with JAVA 5 and above where XMLBinder.XMLBinderInstance is not null, but not yet properly
	// instantiated
	private static volatile XMLBinder XMLBinderInstance;

	/**
	 * XMLBinder instance getter
	 * 
	 * @return Singleton instance of XMLBinder
	 */
	public final static XMLBinder getInstance() {
		// The "Double-Checked XMLBinder" avoids a costly call to synchronized once the instantiation is complete
		if (XMLBinder.XMLBinderInstance == null) {
			// synchronized avoids multiple XMLbinder instantiations on different threads
			synchronized (XMLBinder.class) {
				if (XMLBinder.XMLBinderInstance == null) {
					XMLBinder.XMLBinderInstance = new XMLBinder();
				}
			}
		}
		return XMLBinder.XMLBinderInstance;
	}

	/**
	 * Constructor for the class XMLBinder.java Private constructor to avoid object instantiation from an exterior class
	 */
	private XMLBinder() {
		DocumentBuilder builder = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// No exception should be thrown, just in case:
			throw new IllegalStateException("Could not create a DocumentBuilder", e);
		}
		document = builder.newDocument();
	}

	/**
	 * Generate the JAXB XML objects and save to xml file
	 * 
	 * @param infoQuestions
	 *            info questions panel
	 * @param requestQuestions
	 *            request questions panel
	 * @param networkQuestions
	 *            network questions panel
	 * @param fleetQuestions
	 *            fleet questions panel
	 * @param compressFile
	 *            boolean value whether file should be compressed or not
	 * @param destinationFile
	 *            new file name and location to be saved
	 */
	public void generateXMLPrototyper(InfoQuestions infoQuestions,
			RequestQuestions requestQuestions, NetworkQuestions networkQuestions,
			FleetQuestions fleetQuestions, boolean compressFile, File destinationFile) {

		// instantiate question panel variables
		this.infoQuestions = infoQuestions;
		this.networkQuestions = networkQuestions;
		this.fleetQuestions = fleetQuestions;
		this.requestQuestions = requestQuestions;

		// instantiate new xml prototype to avoid errors if user generates prototype twice
		instance = createInstance();
		info = createInstanceInfo();
		requests = createInstanceRequests();
		network = createInstanceNetwork();
		fleet = createInstanceFleet();

		// id & type values
		nodeId = 1;
		requestId = 1;
		linkId = 1;
		compartmentType = 1;
		demandType = 1;
		vehicleType = 1;

		// build xml prototype
		buildInfo();
		buildNetwork();
		buildFleet();
		buildRequests();

		// write xml prototype to file
		try {
			VRPRepJAXBUtilities.writeInstance(instance, destinationFile, compressFile);
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Build the objects relevant to the problem description
	 * 
	 * @see vroom.common.modeling.vrprep.Instance.Info
	 */
	private void buildInfo() {
		info.setName(infoQuestions.getTextField_InstanceName());
		info.setProblem(infoQuestions.getTextField_ProblemName());
		info.setReference(infoQuestions.getTextField_BiblioRef());

		Contributor contributor = createInstanceInfoContributor();
		contributor.setName(infoQuestions.getTextField_ContriName());
		contributor.setEmail(infoQuestions.getTextField_ContiEMail());

		info.setContributor(contributor);

		instance.setInfo(info);
	}

	/**
	 * Build the objects relevant to the network
	 * 
	 * @see vroom.common.modeling.vrprep.Instance.Network
	 * @see vroom.common.modeling.vrprep.Instance.Network.Nodes
	 * @see Links
	 */
	private void buildNetwork() {
		Nodes nodes = createInstanceNetworkNodes();

		Links links = null;

		// NODES
		if (networkQuestions.getQ2_nodeL().isSelected()
				|| networkQuestions.getQ2_both().isSelected()) {
			// euclidean location
			if (networkQuestions.getQ4_euclid().isSelected()) {
				nodes.getNode().add(createNode("euclidean", null));
			}
			// gps location
			if (networkQuestions.getQ4_gps().isSelected()) {
				nodes.getNode().add(createNode("gps", null));
			}
			// other location
			if (networkQuestions.getQ4_other().isSelected()) {
				for (int i = 0; i < networkQuestions.getLocationAny().getListModel().size(); i++) {
					nodes.getNode().add(
							createNode("other", networkQuestions.getLocationAny().getListModel()
									.get(i).toString()));
				}
			}
		}

		// If no node, then add empty to respect schema
		if (nodes.getNode().isEmpty()) {
			nodes.getNode().add(createNode("", null));
		}

		// add second node if only one
		if (this.nodeId <= 2) {
			if (networkQuestions.getQ2_nodeL().isSelected()
					|| networkQuestions.getQ2_both().isSelected()) {
				// euclidean location
				if (networkQuestions.getQ4_euclid().isSelected()) {
					nodes.getNode().add(createNode("euclidean", null));
				}
				// gps location
				else if (networkQuestions.getQ4_gps().isSelected()) {
					nodes.getNode().add(createNode("gps", null));
				}
				// other location
				else if (networkQuestions.getQ4_other().isSelected()) {
					for (int i = 0; i < networkQuestions.getLocationAny().getListModel().size(); i++) {
						nodes.getNode().add(
								createNode("other", networkQuestions.getLocationAny().getListModel()
										.get(i).toString()));
					}
				} 
			}else {
				nodes.getNode().add(createNode("", null));
			}
		}

		// LINKS
		if (networkQuestions.getQ2_linkL().isSelected()
				|| networkQuestions.getQ2_both().isSelected()) {
			links = createInstanceNetworkLinks();

			// TIME
			if (networkQuestions.getQ100_time().isSelected()) {
				// standard time
				if (networkQuestions.getQ12_value().isSelected()) {
					links.getLink().add(createLink("value", null));
				}

				// poisson time
				if (networkQuestions.getQ12_poisson().isSelected()) {
					links.getLink().add(createLink("poisson", null));
				}

				// normal time
				if (networkQuestions.getQ12_normal().isSelected()) {
					links.getLink().add(createLink("normal", null));
				}

				// other time
				if (networkQuestions.getQ12_other().isSelected()) {
					for (int i = 0; i < networkQuestions.getTimeAny().getListModel().size(); i++) {
						links.getLink().add(
								createLink("other", networkQuestions.getTimeAny().getListModel()
										.get(i).toString()));
					}
				}
			}

			// If no link, then add empty to respect schema
			if (links.getLink().isEmpty()) {
				links.getLink().add(createLink("", null));
			}
		}

		network.setNodes(nodes);

		if (links != null) {
			network.setLinks(links);
		}

		network.setDescriptor(createDescriptor());

		instance.setNetwork(network);
	}

	/**
	 * Build the objects relevant to the fleet
	 * 
	 * @see vroom.common.modeling.vrprep.Instance.Fleet
	 */
	private void buildFleet() {
		// speed profiles
		if (fleetQuestions.getQ7_yes().isSelected()) {
			// average speed
			if (fleetQuestions.getQ8_average().isSelected()) {
				fleet.getVehicle().add(createVehicle("avg"));
			}

			// speed intervals
			if (fleetQuestions.getQ8_intervals().isSelected()) {
				fleet.getVehicle().add(createVehicle("int"));
			}
		} else {
			fleet.getVehicle().add(createVehicle(""));
		}

		int sumTrues = 0;
		if (fleetQuestions.getQ8_average().isSelected())
			sumTrues++;
		if (fleetQuestions.getQ8_intervals().isSelected())
			sumTrues++;

		if(fleetQuestions.getQ1_multiple().isSelected() && sumTrues <= 1){
			if (fleetQuestions.getQ7_yes().isSelected()) {
				// average speed
				if (fleetQuestions.getQ8_average().isSelected()) {
					fleet.getVehicle().add(createVehicle("avg"));
				}

				// speed intervals
				if (fleetQuestions.getQ8_intervals().isSelected()) {
					fleet.getVehicle().add(createVehicle("int"));
				}
			} else {
				fleet.getVehicle().add(createVehicle(""));
			}
		}

		instance.setFleet(fleet);
	}

	/**
	 * Build the objects relevant to the requests
	 * 
	 * @see vroom.common.modeling.vrprep.Instance.Requests
	 */
	private void buildRequests() {
		// has service time
		if (requestQuestions.getQ15_yes().isSelected()) {
			// value
			if (requestQuestions.getQ16_value().isSelected()) {
				requests.getRequest().add(createRequest("value", null));
			}
			// poisson variable
			if (requestQuestions.getQ16_poisson().isSelected()) {
				requests.getRequest().add(createRequest("poisson", null));
			}
			// normal variable
			if (requestQuestions.getQ16_normal().isSelected()) {
				requests.getRequest().add(createRequest("normal", null));
			}
			// any
			if (requestQuestions.getQ16_other().isSelected()) {
				for (int i = 0; i < requestQuestions.getTimeAny().getListModel().size(); i++) {
					requests.getRequest().add(
							createRequest("other", requestQuestions.getTimeAny().getListModel()
									.get(i).toString()));
				}
			}
		}

		// If no request, then add empty to respect schema
		if (requests.getRequest().isEmpty()) {
			requests.getRequest().add(createRequest("", null));
		}

		instance.setRequests(requests);

	}

	/**
	 * Create a Node object
	 * 
	 * @param locationType
	 *            type of location the node has if any
	 * @param anyName
	 *            name of new location element if any
	 * @return new Node
	 * @see vroom.common.modeling.vrprep.Instance.Network.Nodes.Node
	 */
	private Node createNode(String locationType, String anyName) {
		Node n = createInstanceNetworkNodesNode();
		Location l = createLocation();
		// location
		if (locationType.equals("euclidean")) {
			Euclidean euclidean = createLocationEuclidean();
			euclidean.setCx(1.0);
			euclidean.setCy(2.0);
			euclidean.setCz(3.0);

			l.setEuclidean(euclidean);
			n.setLocation(l);
		} else if (locationType.equals("gps")) {
			GPSCoordinates gps = createLocationGPSCoordinates();
			gps.setLat(1.0);
			gps.setLon(2.0);
			l.setGPSCoordinates(gps);
			n.setLocation(l);

		} else if (locationType.equals("other")) {
			l.setCustom(createCustom(networkQuestions.getLocationAny().getListModel()));
			n.setLocation(l);
		}
		// other info
		if((networkQuestions.getQ2_nodeL().isSelected() || networkQuestions.getQ2_both().isSelected()) &&
				networkQuestions.getQ105_yes().isSelected()){			
			n.setCustom(createCustom(networkQuestions.getAnyMoreAnyN().getListModel()));
		}
		// node type
		if(networkQuestions.getQ101_yes().isSelected()){
			n.setType(BigInteger.valueOf(nodeId));
		}
		// node id
		n.setId(BigInteger.valueOf(nodeId));
		nodeId++;

		return n;
	}

	/**
	 * Create a Link object
	 * 
	 * @param timeType
	 *            type of time the link has if any
	 * @param anyName
	 *            name of new time element if any
	 * @return new Link
	 * @see vroom.common.modeling.vrprep.Instance.Network.Links.Link
	 */
	private Link createLink(String timeType, String anyName) {
		Link l = createInstanceNetworkLinksLink();
		Time t = createTime();
		// time
		if (timeType.equals("value")) {
			t.getContent().add("ANY VALUE");
			l.setTime(t);
		} else if (timeType.equals("normal")) {
			NormalVariable normal = createNormalVariable();
			normal.setMean(1.0);
			normal.setVariance(0.5);
			t.getContent().add(normal);
			l.setTime(t);
		} else if (timeType.equals("poisson")) {
			PoissonVariable poisson = createPoissonVariable();
			poisson.setLambda(0.5);
			t.getContent().add(poisson);
			l.setTime(t);
		} else if (timeType.equals("other")) {
			t.getContent().add(createCustom(networkQuestions.getTimeAny().getListModel()));	
			l.setTime(t);
		}
		// link type
		if (networkQuestions.getQ7_yes().isSelected()) {
			l.setType(BigInteger.ONE);
		}
		// link directed
		if (networkQuestions.getQ8_yes().isSelected()) {
			l.setDirected(true);
		}
		// cost
		if (networkQuestions.getQ100_cost().isSelected()) {
			l.setCost(10.0);
		}
		// length
		if (networkQuestions.getQ100_length().isSelected()) {
			l.setLength(5.0);
		}
		// other info
		if((networkQuestions.getQ2_linkL().isSelected() || networkQuestions.getQ2_both().isSelected()) &&
				networkQuestions.getQ106_yes().isSelected()){
			l.setCustom(createCustom(networkQuestions.getAnyMoreAnyL().getListModel()));
		}
		// link head and tail node attachment
		l.setHead(BigInteger.valueOf(1));
		l.setTail(BigInteger.valueOf(2));
		// link id
		if (requestQuestions.getQ1_link().isSelected()
				|| requestQuestions.getQ1_both().isSelected()) {
			l.setId(BigInteger.valueOf(linkId));
			linkId++;
		}

		return l;
	}

	/**
	 * Create a Descriptor object
	 * 
	 * @return new Descriptor
	 * @see vroom.common.modeling.vrprep.Instance.Network.Descriptor
	 */
	private Descriptor createDescriptor() {
		Descriptor d = createInstanceNetworkDescriptor();
		// network complete
		if (networkQuestions.getQ13_yes().isSelected()) {
			d.setIsComplete(true);
		}
		// distance type
		if (networkQuestions.getQ14_yes().isSelected()) {
			d.setDistanceType(networkQuestions.getTextFieldDistMesure().getText());
		}
		// rounding rule
		if (networkQuestions.getQ15_yes().isSelected()) {
			d.setRoundingRule(networkQuestions.getTextFieldRoundingRule().getText());
		}
		// other info
		if (networkQuestions.getQ16_yes().isSelected()) {
			d.setCustom(createCustom(networkQuestions.getDescriptorAny().getListModel()));
		}

		return d;
	}

	/**
	 * Create Vehicle object
	 * 
	 * @param spType
	 *            type of speed the vehicle has if any
	 * @return new Vehicle
	 * @see vroom.common.modeling.vrprep.Instance.Fleet.Vehicle
	 */
	private Vehicle createVehicle(String spType) {
		Vehicle v = createInstanceFleetVehicle();

		// speed profiles
		if (fleetQuestions.getQ7_yes().isSelected()) {
			v.setSpeedProfile(createSpeedProfile(spType));
		}

		// vehicle has type
		if (fleetQuestions.getQ1_multiple().isSelected()) {
			v.setType(BigInteger.valueOf(vehicleType));
			vehicleType++;
		}
		// fixed cost
		if (fleetQuestions.getQ3_yes().isSelected()) {
			v.getFixedCost().add(10.0);
		}
		// variable cost
		if (fleetQuestions.getQ4_yes().isSelected()) {
			v.getVariableCost().add(10.0);
		}
		// number
		if (fleetQuestions.getQ5_yes().isSelected()) {
			v.setNumber(BigInteger.ONE);
		}
		// node types compatible
		if (fleetQuestions.getQ6_yes().isSelected()) {
			v.getNodeTypesCompatible().add(BigInteger.ONE);
		}
		// max travel distance
		if (fleetQuestions.getQ14_yes().isSelected()) {
			v.setMaxTravelDistance(createInstanceFleetVehicleMaxTravelDistance());
			v.getMaxTravelDistance().setContent("100");
			if (!fleetQuestions.getQ15_yes().isSelected()) {
				v.getMaxTravelDistance().setIsFlexible(true);
			}
		}
		// max requests
		if (fleetQuestions.getQ16_yes().isSelected()) {
			v.setMaxRequests(createInstanceFleetVehicleMaxRequests());
			v.getMaxRequests().setContent("3");
			if (!fleetQuestions.getQ17_yes().isSelected()) {
				v.getMaxRequests().setIsFlexible(true);
			}
		}
		// departure node
		if (fleetQuestions.getQ30_yes().isSelected()) {
			v.setDepartureNode(BigInteger.ONE);
		}
		// arrival node
		if (fleetQuestions.getQ31_yes().isSelected()) {
			v.setArrivalNode(BigInteger.ONE);
		}
		// skill
		if (fleetQuestions.getQ32_yes().isSelected()) {
			v.getSkill().add(createSkill());
			v.getSkill().get(0).setId(BigInteger.ONE);
		}
		// tool
		if (fleetQuestions.getQ33_yes().isSelected()) {
			v.getTool().add(createTool());
			v.getTool().get(0).setId(BigInteger.ONE);
		}
		// workload profile
		if (fleetQuestions.getQ18_yes().isSelected()) {
			v.setWorkloadProfile(createWorkloadProfile());
		}

		// capacities and compartments
		if (fleetQuestions.getQ26_yes().isSelected()) {
			// multiple compartments
			if (fleetQuestions.getQ28_yes().isSelected()) {
				// min and max values
				if (fleetQuestions.getQ29_minMax().isSelected()) {
					v.getCompartment().add(createCompartment("minMax"));
					if (!fleetQuestions.getQ29_fixed().isSelected()) {
						v.getCompartment().add(createCompartment("minMax"));
					}
					// add total vehicle capacity
					v.getCapacity().add(20.0);
				}
				// fixed value
				if (fleetQuestions.getQ29_fixed().isSelected()) {
					v.getCompartment().add(createCompartment("fixed"));
					if (!fleetQuestions.getQ29_minMax().isSelected()) {
						v.getCompartment().add(createCompartment("fixed"));
					}
				}
			}else{
				// add total vehicle capacity
				v.getCapacity().add(20.0);
			}
		}

		// extra info
		if(fleetQuestions.getQ105_yes().isSelected()){
			v.setCustom(createCustom(fleetQuestions.getAnyMoreAny().getListModel()));
		}

		return v;
	}

	/**
	 * Create SpeedProfile object
	 * 
	 * @param spType
	 *            type of speed to be instantiated
	 * @return new SpeedProfile
	 * @see vroom.common.modeling.vrprep.SpeedProfile
	 */
	private SpeedProfile createSpeedProfile(String spType) {
		SpeedProfile sp = createSpeedProfile();
		if (spType.equals("avg")) {
			sp.setAvg(15.0);
		} else if (spType.equals("int")) {

			sp.getSpeedInterval().add(createSpeedInterval());

			// multiple speed intervals
			if (fleetQuestions.getQ9_yes().isSelected()) {
				sp.getSpeedInterval().add(createSpeedInterval());
			}
		}

		return sp;
	}

	/**
	 * Create SpeedInterval object
	 * 
	 * @return new SpeedInterval
	 * @see vroom.common.modeling.vrprep.SpeedProfile.SpeedInterval
	 */
	private SpeedInterval createSpeedInterval() {
		SpeedInterval spsi = createSpeedProfileSpeedInterval();
		Tw tw = createTw();

		// has start
		if (fleetQuestions.getQ11_start().isSelected()) {
			tw.setStart(createTwStart());
			if (!fleetQuestions.getQ12_yes().isSelected()) {
				tw.getStart().setIsHard(false);
			}
			tw.getStart().setContent("start time here");
		}

		// has end
		if (fleetQuestions.getQ11_end().isSelected()) {
			tw.setEnd(createTwEnd());
			if (!fleetQuestions.getQ12_yes().isSelected()) {
				tw.getEnd().setIsHard(false);
			}
			tw.getEnd().setContent("end time here");
		}

		spsi.getTw().add(tw);

		// has multiple time windows
		if (fleetQuestions.getQ10_yes().isSelected()) {
			spsi.getTw().add(tw);
		}

		return spsi;
	}

	/**
	 * Create WorkloadProfile object
	 * 
	 * @return new WorkloadProfile
	 * @see vroom.common.modeling.vrprep.Instance.Fleet.Vehicle.WorkloadProfile
	 */
	private WorkloadProfile createWorkloadProfile() {
		WorkloadProfile wlp = createInstanceFleetVehicleWorkloadProfile();

		// max work time
		if (fleetQuestions.getQ19_yes().isSelected()) {
			wlp.setMaxWorkTime(createInstanceFleetVehicleWorkloadProfileMaxWorkTime());
			wlp.getMaxWorkTime().setContent("12");
			if (!fleetQuestions.getQ20_yes().isSelected()) {
				wlp.getMaxWorkTime().setIsFlexible(true);
			}
		}

		// time windows
		if (fleetQuestions.getQ21_yes().isSelected()) {
			Tw tw = createTw();

			// has start
			if (fleetQuestions.getQ23_start().isSelected()) {
				tw.setStart(createTwStart());
				if (!fleetQuestions.getQ24_yes().isSelected()) {
					tw.getStart().setIsHard(false);
				}
				tw.getStart().setContent("start time here");
			}
			// has end
			if (fleetQuestions.getQ23_end().isSelected()) {
				tw.setEnd(createTwEnd());
				if (!fleetQuestions.getQ25_yes().isSelected()) {
					tw.getEnd().setIsHard(false);
				}
				tw.getEnd().setContent("end time here");
			}

			wlp.getTw().add(tw);

			// multiple time windows
			if (fleetQuestions.getQ22_yes().isSelected()) {
				wlp.getTw().add(tw);
			}
		}

		// other wlp elements
		if (fleetQuestions.getQ101_yes().isSelected()) {
			wlp.setCustom(createCustom(fleetQuestions.getwLPAny().getListModel()));
		}

		return wlp;
	}

	//	/**
	//	 * Create Capacity object
	//	 * 
	//	 * @param capacityType
	//	 *            type of capacity to be instantiated
	//	 * @return new Capacity
	//	 * @see vroom.common.modeling.vrprep.Capacity
	//	 */
	//	private Capacity createCapacity(String capacityType) {
	//		Capacity c = createCapacity();
	//
	//		if (capacityType.equals("minMax")) {
	//			c.getContent().add(createCapacityMin(0.0));
	//			c.getContent().add(createCapacityMax(10.0));
	//		} else if (capacityType.equals("fixed")) {
	//			c.getContent().add(createCapacityFixed(10.0));
	//		} else if (capacityType.equals("total")) {
	//			c.getContent().add(createCapacityTotal(10.0));
	//		}
	//
	//		// capacity has types
	//		if (fleetQuestions.getQ27_yes().isSelected()) {
	//			c.setType(BigInteger.valueOf(this.compartmentType));
	//			this.compartmentType++;
	//		}
	//
	//		return c;
	//	}

	/**
	 * Create Compartment object
	 * 
	 * @param compartmentType
	 * 				type of compartment to be instantiated
	 * @return new Compartment Object
	 * @see vroom.common.modeling.vrprep.Compartment
	 */
	private Compartment createCompartment(String compartmentType){
		Compartment c = createCompartment();

		if(compartmentType.equals("minMax")){
			c.setMinCapacity(0.0);
			c.setMaxCapacity(10.0);
		}else if (compartmentType.equals("fixed")){
			c.setFixed(10.0);
		}

		//		// compartment has types
		//		if (fleetQuestions.getQ27_yes().isSelected()) {
		c.setType(BigInteger.valueOf(this.compartmentType));
		this.compartmentType++;
		//		}

		return c;
	}

	/**
	 * Create Request object
	 * 
	 * @param serviceTimeType
	 *            type of service time to be instantiated
	 * @param anyValue
	 *            name of new time element if any
	 * @return new Request
	 * @see vroom.common.modeling.vrprep.Instance.Requests
	 */
	private Request createRequest(String serviceTimeType, String anyValue) {
		Request r = createInstanceRequestsRequest();

		r.setId(BigInteger.valueOf(requestId));
		requestId++;

		// request attached to node
		if (requestQuestions.getQ1_node().isSelected()
				|| requestQuestions.getQ1_both().isSelected()) {
			r.setNode(BigInteger.ONE);
		}
		// request attached to link
		if (requestQuestions.getQ1_link().isSelected()
				|| requestQuestions.getQ1_both().isSelected()) {
			r.setLink(BigInteger.ONE);
		}
		// requests have types
		if (requestQuestions.getQ3_yes().isSelected()) {
			r.setType(BigInteger.ONE);
		}
		// prize
		if (requestQuestions.getQ12_yes().isSelected()) {
			r.setPrize(BigInteger.ONE);
		}
		// cost
		if (requestQuestions.getQ13_yes().isSelected()) {
			r.setCost(10.0);
		}
		// skills
		if (requestQuestions.getQ19_yes().isSelected()) {
			r.getSkill().add(createSkill());
			r.getSkill().get(0).setId(BigInteger.ONE);
		}
		// tools
		if (requestQuestions.getQ20_yes().isSelected()) {
			r.getTool().add(createTool());
			r.getTool().get(0).setId(BigInteger.ONE);
		}
		// time windows
		if (requestQuestions.getQ4_yes().isSelected()) {
			Tw tw1 = createTw();
			// has start
			if (requestQuestions.getQ6_start().isSelected()) {
				tw1.setStart(createTwStart());
				if (!requestQuestions.getQ7_yes().isSelected()) {
					tw1.getStart().setIsHard(false);
				}
				tw1.getStart().setContent("start time here");
			}
			// has end
			if (requestQuestions.getQ6_end().isSelected()) {
				tw1.setEnd(createTwEnd());
				if (!requestQuestions.getQ8_yes().isSelected()) {
					tw1.getEnd().setIsHard(false);
				}
				tw1.getEnd().setContent("end time here");
			}

			r.getTw().add(tw1);

			// multiple time windows
			if (requestQuestions.getQ5_yes().isSelected()) {
				r.getTw().add(tw1);
			}
		}

		// release date
		if (requestQuestions.getQ14_yes().isSelected()) {
			r.setReleaseDate(12.0);
		}

		if (requestQuestions.getQ15_yes().isSelected()) {
			r.setServiceTime(createServiceTime(serviceTimeType, anyValue));
		}

		// demand
		if (requestQuestions.getQ9_yes().isSelected()) {
			// value
			if (requestQuestions.getQ11_value().isSelected()) {
				r.getDemand().add(createDemand("value", null));
			}
			// poisson variable
			if (requestQuestions.getQ11_poisson().isSelected()) {
				r.getDemand().add(createDemand("poisson", null));
			}
			// normal variable
			if (requestQuestions.getQ11_normal().isSelected()) {
				r.getDemand().add(createDemand("normal", null));
			}
			// any
			if (requestQuestions.getQ11_other().isSelected()) {
				for (int i = 0; i < requestQuestions.getDemandAny().getListModel().size(); i++) {
					r.getDemand().add(
							createDemand("other", requestQuestions.getDemandAny().getListModel()
									.get(i).toString()));
				}
			}
			// multiple demands
			if (requestQuestions.getQ10_yes().isSelected()) {
				if (this.demandType <= 2) {
					// value
					if (requestQuestions.getQ11_value().isSelected()) {
						r.getDemand().add(createDemand("value", null));
					}
					// poisson variable
					if (requestQuestions.getQ11_poisson().isSelected()) {
						r.getDemand().add(createDemand("poisson", null));
					}
					// normal variable
					if (requestQuestions.getQ11_normal().isSelected()) {
						r.getDemand().add(createDemand("normal", null));
					}
					// any
					if (requestQuestions.getQ11_other().isSelected()) {
						for (int i = 0; i < requestQuestions.getDemandAny().getListModel().size(); i++) {
							r.getDemand().add(
									createDemand("other", requestQuestions.getDemandAny()
											.getListModel().get(i).toString()));
						}
					}
				}
			}
		}

		// predecessors
		if (requestQuestions.getQ18_prec().isSelected()) {
			r.setPredecessors(createInstanceRequestsRequestPredecessors());
			r.getPredecessors().getRequest().add(BigInteger.ONE);
		}
		// successors
		if (requestQuestions.getQ18_succ().isSelected()) {
			r.setSuccessors(createInstanceRequestsRequestSuccessors());
			r.getSuccessors().getRequest().add(BigInteger.ONE);
		}
		// extra info
		if(requestQuestions.getQ105_yes().isSelected()){
			r.setCustom(createCustom(requestQuestions.getAnyMoreAny().getListModel()));
		}

		return r;
	}

	/**
	 * Create new Demand object
	 * 
	 * @param demandType
	 *            type of demand to be instantiated
	 * @param anyValue
	 *            name of new demand element if any
	 * @return new Demand
	 * @see vroom.common.modeling.vrprep.Demand
	 */
	private Demand createDemand(String demandType, String anyValue) {
		Demand d = createDemand();

		if (demandType.equals("value")) {
			d.getContent().add("value here");
		} else if (demandType.equals("normal")) {
			NormalVariable n = createNormalVariable();
			n.setMean(1.0);
			n.setVariance(0.5);
			d.getContent().add(n);
		} else if (demandType.equals("poisson")) {
			PoissonVariable p = createPoissonVariable();
			p.setLambda(0.5);
			d.getContent().add(p);
		} else if (demandType.equals("other")) {
			d.getContent().add(createCustom(requestQuestions.getDemandAny().getListModel()));
		}

		d.setType(BigInteger.valueOf(this.demandType));
		this.demandType++;

		// damand is splitable
		if (requestQuestions.getQ100_yes().isSelected()) {
			d.setIsSplitable(true);
		}

		return d;
	}

	/**
	 * Create ServiceTime object
	 * 
	 * @param serviceTimeType
	 *            type of time to be instantiated
	 * @param anyName
	 *            name of new time element if any
	 * @return new ServiceTime
	 * @see vroom/common/modeling/vrprep/Time
	 */
	private Time createServiceTime(String serviceTimeType, String anyName) {
		Time t = createTime();

		if (serviceTimeType.equals("value")) {
			t.getContent().add("value here");
		} else if (serviceTimeType.equals("normal")) {
			NormalVariable n = createNormalVariable();
			n.setMean(1.0);
			n.setVariance(0.5);
			t.getContent().add(n);
		} else if (serviceTimeType.equals("poisson")) {
			PoissonVariable p = createPoissonVariable();
			p.setLambda(0.5);
			t.getContent().add(p);
		} else if (serviceTimeType.equals("other")) {
			t.getContent().add(createCustom(requestQuestions.getTimeAny().getListModel()));
		}

		return t;

	}
	
	/**
	 * Create JAXB custom element bases of elements in a list
	 * @param dlm default list model
	 * @return the new custom element
	 */
	private Custom createCustom(DefaultListModel dlm){
		Custom c = createCustom();

		for(int i = 0; i < dlm.size(); i++) {
			Element el = document.createElement(dlm.get(i).toString());
			el.setTextContent("text here");
			c.getAny().add(el);
		}

		return c;
	}

	/**
	 * Getter for the variable info
	 * 
	 * @return the info
	 */
	public Info getInfo() {
		return info;
	}

}
