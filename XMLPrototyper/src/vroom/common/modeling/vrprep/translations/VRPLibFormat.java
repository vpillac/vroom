package vroom.common.modeling.vrprep.translations;

import java.io.IOException;
import java.math.BigInteger;

import vroom.common.modeling.vrprep.Demand;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle.MaxTravelDistance;
import vroom.common.modeling.vrprep.Location;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
import vroom.common.modeling.vrprep.Instance.Network.Descriptor;
import vroom.common.modeling.vrprep.Instance.Network.Links;
import vroom.common.modeling.vrprep.Instance.Network.Nodes;
import vroom.common.modeling.vrprep.Instance.Network.Links.Link;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Instance.Requests.Request;
import vroom.common.modeling.vrprep.Location.Euclidean;

/**
 * Used to convert desfault values of the VRPLiv fileformat. Class is extended by children of the VRPLib fprmat
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class VRPLibFormat extends Tools {
	
	/**
	 * Number of nodes
	 */
	protected int nbNodes = -2;
	/**
	 * Vehicle capacity
	 */
	protected double vehicleCap = -2;
	/**
	 * Number of vehicles
	 */
	protected int vehicleNumber = -2;
	/**
	 * Vehicle max travel distance
	 */
	protected double vehicleDist = -2;


	/**
	 * Constructor for the class VRPLibFormat.java
	 */
	public VRPLibFormat(){
		super();
	}


	/**
	 * Convert the default data into VRPRep format
	 * @param hasLinks equals true if the problem has links
	 * @param brReadLineValue line value, buffered reader was at when this method was called
	 * @param hasDepotIndex equals true if the depots indices are specifies
	 */
	public void translateData(boolean hasLinks, String brReadLineValue, boolean hasDepotIndex) {

		boolean first = true;

		String l = "";
		String [] ls;

		try {
			while(l != null){
				if(first){
					l = brReadLineValue;
					first = false;
				}else{
					l = br.readLine();
				}

				if(l != null){
					l = l.replaceAll("^\\s+", "");
					ls = l.split("\\s+");

					// nodes
					if(hasLinks){
						Nodes n = createInstanceNetworkNodes();
						for(int i = 1; i <= nbNodes; i++)
							n.getNode().add(createDefaultNode(i));
						network.setNodes(n);
					}
					if(ls[0].equals("NODE_COORD_SECTION")){
						String nl;
						Nodes nodes = createInstanceNetworkNodes();

						for(int i = 0; i<nbNodes; i++){
							nl = br.readLine().replaceAll("^\\s+", "");
							if(!nl.equals(""))
								nodes.getNode().add(createNode(nl.split("\\s+"), false));
						}
						network.setNodes(nodes);
					}				
					// links
					if(ls[0].equals("EDGE_WEIGHT_SECTION")){

						Links links = createInstanceNetworkLinks();
						for(int i = 1; i <= nbNodes; i++){
							l = br.readLine().replaceAll("^\\s+", "");
							ls = l.split("\\s+");

							for(int j = 1; j <= ls.length; j++){
								if(i != j){
									links.getLink().add(createLink(i, j, Double.valueOf(ls[j-1])));
								}
							}
						}				
						network.setLinks(links);
					}
					// requests
					if(ls[0].equals("DEMAND_SECTION")){
						String rl;

						for(int i = 0; i<nbNodes; i++){
							rl = br.readLine().replaceAll("^\\s+", "");
							if(!rl.equals(""))
								requests.getRequest().add(createRequest(rl.split("\\s+")));
						}
					}
					// depots
					if(ls[0].equals("DEPOT_SECTION") && hasDepotIndex){
						String dl;
						String [] dls;
						while(!(dl = br.readLine()).contains("-1") && !dl.contains("EOF")){
							dl = dl.replaceAll("^\\s+", "");
							if(!dl.equals("") && !dl.contains("EOF")){
								dls = dl.split("\\s+");
								network.getNodes().getNode().get(Integer.valueOf(dls[0])-1).setType(BigInteger.valueOf(0));
							}
						}
					}	
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		// vehicle	
		fleet.getVehicle().add(createVehicle());



		//descriptor
		Descriptor d = createInstanceNetworkDescriptor();
		if(!hasLinks)
			d.setIsComplete(true);
		network.setDescriptor(d);
	}

	/**
	 * Create VRPRep node with no location
	 * @param id id of node
	 * @return new Node
	 */
	private Node createDefaultNode(int id){
		Node n = createInstanceNetworkNodesNode();

		n.setType(BigInteger.valueOf(1));	
		n.setId(BigInteger.valueOf(id));

		return n;
	}

	/**
	 * Create VRPRep node with location 
	 * @param v split values of buffered reader line
	 * @param isDepot true if is a depot
	 * @return new Node
	 */
	private Node createNode(String[] v, boolean isDepot){
		Node n = createInstanceNetworkNodesNode();
		n.setId(BigInteger.valueOf(Integer.valueOf(v[0])));

		Euclidean e = createLocationEuclidean();
		e.setCx(Double.valueOf(v[1]));
		e.setCy(Double.valueOf(v[2]));

		Location l = createLocation();
		l.setEuclidean(e);

		if(isDepot)
			n.setType(BigInteger.valueOf(0));
		else
			n.setType(BigInteger.valueOf(1));

		n.setLocation(l);

		return n;	
	}

	/**
	 * Create VRPRep link
	 * @param head id value of head node
	 * @param tail id value of tail node
	 * @param length length of link
	 * @return new Link
	 */
	private Link createLink(int head, int tail, double length){
		Link l = createInstanceNetworkLinksLink();

		l.setHead(BigInteger.valueOf(head));
		l.setTail(BigInteger.valueOf(tail));

		l.setLength(length);

		return l;
	}

	/**
	 * Create VRPRep request
	 * @param v split values of buffered reader line
	 * @return new Request
	 */
	private Request createRequest(String[] v){
		Request r = createInstanceRequestsRequest();
		r.setId(BigInteger.valueOf(Integer.valueOf(v[0])));
		r.setNode(BigInteger.valueOf(Integer.valueOf(v[0])));

		Demand d = createDemand();
		d.getContent().add(v[1]);

		r.getDemand().add(d);

		return r;
	}

	/**
	 * Create VRPRep vehicle based on local variable values
	 * @return new Vehicle
	 */
	private Vehicle createVehicle(){
		Vehicle v = createInstanceFleetVehicle();

		if(vehicleCap == -2)
			System.out.println("Vehicle capacity was not found");
		else
			v.getCapacity().add(vehicleCap);

		if(vehicleNumber == -2)
			System.out.println("Vehicle number was not found");
		else
			v.setNumber(BigInteger.valueOf(vehicleNumber));
		
		if(vehicleDist != -2){
			MaxTravelDistance mtd = createInstanceFleetVehicleMaxTravelDistance();
			mtd.setContent(String.valueOf(vehicleDist));
			v.setMaxTravelDistance(mtd);
		}

		return v;
	}




}
