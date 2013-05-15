package vroom.common.modeling.vrprep.translations;

import java.io.IOException;
import java.math.BigInteger;

import org.w3c.dom.Element;

import vroom.common.modeling.vrprep.Custom;
import vroom.common.modeling.vrprep.Demand;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle.WorkloadProfile;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle.WorkloadProfile.MaxWorkTime;
import vroom.common.modeling.vrprep.Instance.Network.Descriptor;
import vroom.common.modeling.vrprep.Instance.Network.Nodes;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Instance.Requests.Request;
import vroom.common.modeling.vrprep.Location;
import vroom.common.modeling.vrprep.Location.Euclidean;
import vroom.common.modeling.vrprep.Time;
import vroom.common.modeling.vrprep.Tw;
import vroom.common.modeling.vrprep.Tw.End;
import vroom.common.modeling.vrprep.Tw.Start;

/**
 * Cordeau file format converter
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class Cordeau extends Tools implements Translator {

	private int nbDays;
	private int vehicleNumber;
	private int customerNumber;
	private String destination;

	public Cordeau(String destination){
		this.destination = destination;
	}

	/* (non-Javadoc)
	 * @see vroom.common.modeling.vrprep.translations.Translator#translateFile(java.lang.String)
	 */
	@Override
	public void translateFile(String file) {
		openFileReader(file);

		String fileName = file.substring(file.lastIndexOf("\\")+1, file.length());

		info.setName(fileName);
		info.setReference("Cordeau");

		String l = "";
		String [] ls;


		try {
			l = br.readLine().replaceAll("^\\s+", "");
			ls = l.split("\\s+");

			// info
			info.setProblem(getType(Integer.valueOf(ls[0])));
			vehicleNumber = Integer.valueOf(ls[1]);	
			customerNumber = Integer.valueOf(ls[2]);
			nbDays = Integer.valueOf(ls[3]);

			// vehicles
			boolean firstVehicle = true;
			for(int i = 0; i < nbDays; i++){
				l = br.readLine().replaceAll("^\\s+", "");
				ls = l.split("\\s+");
				if(info.getProblem().contains("SDVRP"))
					fleet.getVehicle().add(createVehicle(ls[0], Double.valueOf(ls[1]), true, i+1));
				else if(firstVehicle)
					fleet.getVehicle().add(createVehicle(ls[0], Double.valueOf(ls[1]), false, i+1));
				firstVehicle = false;
			}

			// nodes && requests
			Nodes n = createInstanceNetworkNodes();
			// depot non MDVRP
			if(!info.getProblem().contains("MDVRP")){
				ls = br.readLine().replaceAll("^\\s+", "").split("\\s+");
				n.getNode().add(createNode(ls, true));
			}
			// customers
			for(int i = 0; i < customerNumber;  i++){
				l = br.readLine().replaceAll("^\\s+", "");
				ls = l.split("\\s+");
				n.getNode().add(createNode(ls, false));
				requests.getRequest().add(createRequest(ls));		
			}		
			// depots MDVRP
			if(info.getProblem().contains("MDVRP")){
				while((l = br.readLine()) != null){
					l = l.replaceAll("^\\s+", "");
					ls = l.split("\\s+");
					n.getNode().add(createNode(ls, true));
				}
			}
			network.setNodes(n);

		} catch (IOException e) {
			e.printStackTrace();
		}

		//descriptor
		Descriptor d = createInstanceNetworkDescriptor();
		d.setIsComplete(true);
		network.setDescriptor(d);



		marshalFile(destination+fileName+".xml");

	}
	
	/**
	 * Create VRPRep Vehicle
	 * @param maxDuration max running duration
	 * @param maxLoad maximum load
	 * @param multipleTypes true if there are multiple vehicle types
	 * @param type integer value of type
	 * @return
	 */
	private Vehicle createVehicle(String maxDuration, double maxLoad, boolean multipleTypes, int type){
		Vehicle v = createInstanceFleetVehicle();
		if(multipleTypes)
			v.setType(BigInteger.valueOf(type));

		v.setNumber(BigInteger.valueOf(vehicleNumber));

		WorkloadProfile wlp = createInstanceFleetVehicleWorkloadProfile();
		MaxWorkTime maxWorkTime = createInstanceFleetVehicleWorkloadProfileMaxWorkTime();
		maxWorkTime.setContent(maxDuration);
		wlp.setMaxWorkTime(maxWorkTime);
		v.setWorkloadProfile(wlp);

		v.getCapacity().add(maxLoad);

		return v;

	}

	/**
	 * Create VRPRep node with location 
	 * @param v split values of buffered reader line
	 * @param isDepot true if is a depot
	 * @return new Node
	 */
	private Node createNode(String [] v, boolean isDepot){
		Node n = createInstanceNetworkNodesNode();
		if(isDepot)
			n.setType(BigInteger.valueOf(0));
		else
			n.setType(BigInteger.valueOf(1));

		n.setId(BigInteger.valueOf(Integer.valueOf(v[0])));

		Euclidean e = createLocationEuclidean();
		e.setCx(Double.valueOf(v[1]));
		e.setCy(Double.valueOf(v[2]));
		Location l = createLocation();
		l.setEuclidean(e);
		n.setLocation(l);

		return n;
	}


	/**
	 * Create VRPRep request
	 * @param v split values of buffered reader line
	 * @return new Request
	 */
	private Request createRequest(String [] v){
		Request r = createInstanceRequestsRequest();
		r.setNode(BigInteger.valueOf(Integer.valueOf(v[0])));
		r.setId(BigInteger.valueOf(Integer.valueOf(v[0])));

		Time t = createTime();
		t.getContent().add(v[3]);
		r.setServiceTime(t);

		Demand d = createDemand();
		d.getContent().add(v[4]);
		r.getDemand().add(d);

		if(v.length == 9+Integer.valueOf(v[6])){
			Tw tw = createTw();
			Start twS = createTwStart();
			End twE = createTwEnd();
			twS.setContent(v[v.length-2]);
			twE.setContent(v[v.length-1]);
			tw.setStart(twS);
			tw.setEnd(twE);

			r.getTw().add(tw);			
		}

		r.setCustom(createRequestCustom(v));

		return r;
	}

	/**
	 * Create VRPRep request custom elements
	 * @param v split values of buffered reader line
	 * @return new Request
	 */
	private Custom createRequestCustom(String [] v){
		Custom c = createCustom();	

		Element freqVisit = document.createElement("frequencyOfVisit");
		freqVisit.setTextContent(v[5]);
		c.getAny().add(freqVisit);


		Element visitCombos = document.createElement("visitingRule");

		Element numberDays = document.createElement("totalNbVisitingDays");
		numberDays.setTextContent(String.valueOf(nbDays));
		visitCombos.appendChild(numberDays);

		Element combos = document.createElement("combinations");		
		for(int i=7; i<7+Integer.valueOf(v[6]); i++){
			Element e = document.createElement("combination");
			e.setTextContent(convertIntToBytes(Integer.valueOf(v[i])));
			combos.appendChild(e);
		}
		visitCombos.appendChild(combos);

		c.getAny().add(visitCombos);

		return c;
	}

	/**
	 * Convert the integer VRP type to string
	 * @param t integer value of vrp type
	 * @return string value
	 */
	private String getType(int t){
		String type = "";
		switch(t){
		case 0:
			type = "VRP";
			break;
		case 1:
			type = "PVRP";
			break;
		case 2:
			type = "MDVRP";
			break;
		case 3:
			type = "SDVRP";
			break;
		case 4:
			type = "VRPTW";
			break;
		case 5:
			type = "PVRPTW";
			break;
		case 6:
			type = "MDVRPTW";
			break;
		case 7:
			type = "SDVRPTW";
			break;		
		}

		return type;		
	}


	/**
	 * Convert integer value into binary
	 * @param in integer value
	 * @return Strind version of binary value
	 */
	private String convertIntToBytes(int in){
		//integer to binary
		String bi = Integer.toBinaryString(in);

		int nbZerosMissing = nbDays - bi.length();
		String newBi = "";

		for(int i = 0; i < nbZerosMissing ; i++)
			newBi = newBi.concat("0");

		newBi = newBi.concat(bi);

		return newBi;
	}

}
