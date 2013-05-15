package vroom.common.modeling.vrprep.translations;

import java.io.IOException;
import java.math.BigInteger;

import org.w3c.dom.Element;

import vroom.common.modeling.vrprep.Custom;
import vroom.common.modeling.vrprep.Demand;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
import vroom.common.modeling.vrprep.Instance.Network.Descriptor;
import vroom.common.modeling.vrprep.Instance.Network.Nodes;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Instance.Requests.Request;
import vroom.common.modeling.vrprep.Location;
import vroom.common.modeling.vrprep.Location.Euclidean;

/**
 * Taillard file format converter
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class Taillard extends Tools implements Translator {

	/* (non-Javadoc)
	 * @see vroom.common.modeling.vrprep.translations.Translator#translateFile(java.lang.String)
	 */
	@Override
	public void translateFile(String file) {		
		openFileReader(file);

		String fileName = file.substring(file.lastIndexOf("\\")+1, file.lastIndexOf("."));

		String l = "";
		String [] ls;

		// info
		info.setName(fileName);
		info.setProblem("CVRP");
		info.setReference("Rochat and Taillard (1995)");


		int custNumber = -2;
		String bestSol = "";
		double capacity = -2;

		try {	
			l = br.readLine().replaceAll("^\\s+", "");
			ls = l.split("\\s+");
			custNumber = Integer.valueOf(ls[0]);
			bestSol = ls[1];

			l = br.readLine().replaceAll("^\\s+", "");
			ls = l.split("\\s+");
			capacity = Double.valueOf(ls[0]);
			
			Nodes nodes = createInstanceNetworkNodes();
			l = br.readLine().replaceAll("^\\s+", "");
			ls = l.split("\\s+");
			nodes.getNode().add(createNode(ls, true));

			for(int i = 0; i < custNumber; i++){
				l = br.readLine().replaceAll("^\\s+", "");
				ls = l.split("\\s+");

				nodes.getNode().add(createNode(ls, false));
				requests.getRequest().add(createRequest(ls));
			}

			network.setNodes(nodes);
		} catch (IOException e) {
			e.printStackTrace();
		}


		//descriptor
		Descriptor d = createInstanceNetworkDescriptor();
		d.setIsComplete(true);
		d.setDistanceType("Euclidean 2D");
		// best know solution
		Custom c = createCustom();
		Element el = document.createElement("bestKnownSol");
		el.setTextContent(bestSol);
		c.getAny().add(el);
		d.setCustom(c);
		network.setDescriptor(d);	
		//vehicle
		Vehicle v = createInstanceFleetVehicle();
		v.getCapacity().add(capacity);
		fleet.getVehicle().add(v);
		
		marshalFile("./../InstancesMax/VRPRep/cvrp/Taillard/"+fileName+".xml");
	}
	
	
	/**
	 * Create VRPRep node with location 
	 * @param ls split values of buffered reader line
	 * @param isDepot true if is a depot
	 * @return new Node
	 */
	private Node createNode(String [] ls, boolean isDepot){
		Node n = createInstanceNetworkNodesNode();
		Euclidean e = createLocationEuclidean();
		e.setCx(Double.valueOf(ls[0]));
		e.setCy(Double.valueOf(ls[1]));
		Location loc = createLocation();
		loc.setEuclidean(e);
		n.setLocation(loc);
		n.setId(BigInteger.valueOf(0));
		
		if(isDepot)
			n.setType(BigInteger.valueOf(0));
		else
			n.setType(BigInteger.valueOf(1));
		
		return n;
	}
	
	/**
	 * Create VRPRep request
	 * @param ls split values of buffered reader line
	 * @return new Request
	 */
	private Request createRequest(String [] ls){
		Request r = createInstanceRequestsRequest();
		Demand d = createDemand();
		d.getContent().add(ls[3]);
		r.getDemand().add(d);
		r.setId(BigInteger.valueOf(Integer.valueOf(ls[0])));
		r.setNode(BigInteger.valueOf(Integer.valueOf(ls[0])));
		
		return r;
	}

}
