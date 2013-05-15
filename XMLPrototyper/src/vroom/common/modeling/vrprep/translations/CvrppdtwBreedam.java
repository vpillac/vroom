package vroom.common.modeling.vrprep.translations;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;


import vroom.common.modeling.vrprep.Demand;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
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
 * Breedam file format converter
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class CvrppdtwBreedam extends Tools implements Translator {


	public CvrppdtwBreedam(){
		super();
	}

	@Override
	public void translateFile(String file) {
		openFileReader(file);

		String fileName = file.substring(file.lastIndexOf("\\")+1, file.lastIndexOf("."));

		String l = "";
		String [] ls;
		
		// info
		info.setName(fileName);
		info.setProblem("CVRPPDTW");
		info.setReference("Breedam");
		
		
		// find vehicle capacity
		double capacity = -2;
		
		FileReader fwTemp;
		BufferedReader brTemp;
		try {
			fwTemp = new FileReader("./../InstancesMax/CVRPPDTW/Breedam/README.15");
			brTemp = new BufferedReader(fwTemp);
			
			String line;
			String [] lineSplit;
			
			while((line = brTemp.readLine()) != null){
				line = line.replaceAll("^\\s+", "");
				lineSplit = line.split("\\s+");
				
				if(lineSplit[0].equals("Problems")){
					while (!lineSplit[0].equals(fileName.toLowerCase()+".dat:")){
						line = brTemp.readLine().replaceAll("^\\s+", "");
						lineSplit = line.split("\\s+");			
					}
					
					capacity = Double.valueOf(lineSplit[3].substring(lineSplit[3].lastIndexOf("=")+1, lineSplit[3].length()));					
				}							
			}
			fwTemp.close();
			brTemp.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// read file
		try {
			boolean firstLine = true;
			Nodes n = createInstanceNetworkNodes();
			int i = 0;
			while((l = br.readLine()) != null){
				l = l.replaceAll("^\\s+", "");
				ls = l.split("\\s+");
				
				n.getNode().add(createNode(ls, firstLine));
				
				i++;
				if(!firstLine)
					requests.getRequest().add(createRequest(ls, i));
				
				if(firstLine)			
					firstLine = false;
			}
			
			network.setNodes(n);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// vehicle		
		if(capacity == -2)
			System.out.println("Capacity was not found");
		
		Vehicle v = createInstanceFleetVehicle();		
		v.getCapacity().add(capacity);	
		fleet.getVehicle().add(v);
		

		//descriptor
		Descriptor d = createInstanceNetworkDescriptor();
		d.setIsComplete(true);
		
		network.setDescriptor(d);
		
		marshalFile("./../InstancesMax/VRPRep/cvrppdtw/breedam/"+fileName+".xml");

	}
	
	
	/**
	 * Create VRPRep node with location 
	 * @param v split values of buffered reader line
	 * @param isDepot true if is a depot
	 * @return new Node
	 */
	private Node createNode(String[]v, boolean isDepot){
		Node n = createInstanceNetworkNodesNode();
		
		if(isDepot)
			n.setType(BigInteger.valueOf(0));
		else
			n.setType(BigInteger.valueOf(1));
		
		n.setId(BigInteger.valueOf(Integer.valueOf(v[0])));
		
		Euclidean e = createLocationEuclidean();
		e.setCx(Double.valueOf(v[1]));
		e.setCx(Double.valueOf(v[2]));
		
		Location l = createLocation();
		l.setEuclidean(e);
		n.setLocation(l);
		
		return n;
	}
	
	/**
	 * Create VRPRep request
	 * @param v split values of buffered reader line
	 * @param id id of request
	 * @return new Request
	 */
	private Request createRequest(String[]v, int id){
		Request r = createInstanceRequestsRequest();
		r.setId(BigInteger.valueOf(id));
		r.setNode(BigInteger.valueOf(Integer.valueOf(v[0])));
		
		Tw tw = createTw();
		Tw tw2 = createTw();		
		Start start = createTwStart();
		End end = createTwEnd();
		Start start1 = createTwStart();
		End end1 = createTwEnd();
		
		// tw1
		start.setContent(v[3]);
		tw.setStart(start);	
		end.setContent(v[4]);
		tw.setEnd(end);		
		r.getTw().add(tw);
		// tw2
		start1.setContent(v[5]);
		tw2.setStart(start1);		
		end1.setContent(v[6]);
		tw2.setEnd(end1);
		r.getTw().add(tw2);
		// service time
		Time t = createTime();
		t.getContent().add(v[8]);
		r.setServiceTime(t);
		// demand
		Demand d = createDemand();
		d.getContent().add(v[7]);
		r.getDemand().add(d);
		
		// type (0=delivery, 1=pickup)
		r.setType(BigInteger.valueOf(Integer.valueOf(v[9])));
		
		return r;
	}



}
