package vroom.common.modeling.vrprep.translations;

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
 * Homberger file format converter
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class Homberger extends Tools implements Translator {

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
		info.setProblem("CVRPTW");
		info.setReference("Homberger");



		try {
			while((l = br.readLine()) != null){
				l = l.replaceAll("^\\s+", "");
				ls = l.split("\\s+");
				
				// vehicle
				if(ls[0].equals("NUMBER") && ls[1].equals("CAPACITY")){
					l = br.readLine().replaceAll("^\\s+", "");
					ls = l.split("\\s+");	
					
					Vehicle v = createInstanceFleetVehicle();		
					v.getCapacity().add(Double.valueOf(ls[1]));	
					v.setNumber(BigInteger.valueOf(Integer.valueOf(ls[0])));
					fleet.getVehicle().add(v);				
				}
				
				// nodes and requests
				if(ls[0].equals("CUST")){
					boolean firstLine = true;
					Nodes n = createInstanceNetworkNodes();
					while((l = br.readLine()) != null){
						l = l.replaceAll("^\\s+", "");				
						
						if(!l.equals("")){
							ls = l.split("\\s+");
							
							n.getNode().add(createNode(ls, firstLine));	
							
							requests.getRequest().add(createRequest(ls));		
							
							if(firstLine)
								firstLine = false;
						}					
					}
					network.setNodes(n);
				}								
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		

		
		//descriptor
		Descriptor d = createInstanceNetworkDescriptor();
		d.setIsComplete(true);

		network.setDescriptor(d);

		
		marshalFile("./../InstancesMax/VRPRep/cvrptw/homberger/"+fileName+".xml");


	}
	
	/**
	 * Create VRPRep node with location 
	 * @param ls split values of buffered reader line
	 * @param isDepot true if is a depot
	 * @return new Node
	 */
	private Node createNode(String[]v, boolean isDepot){
		Node n = createInstanceNetworkNodesNode();
		n.setId(BigInteger.valueOf(Integer.valueOf(v[0])));
		
		if(isDepot)
			n.setType(BigInteger.valueOf(0));
		else
			n.setType(BigInteger.valueOf(1));
		
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
	 * @param ls split values of buffered reader line
	 * @return new Request
	 */
	private Request createRequest(String [] v){
		Request r = createInstanceRequestsRequest();		
		r.setId(BigInteger.valueOf(Integer.valueOf(v[0])));		
		r.setNode(BigInteger.valueOf(Integer.valueOf(v[0])));
		
		Demand d = createDemand();
		d.getContent().add(v[3]);
		r.getDemand().add(d);
		
		Tw tw = createTw();
		Start twS = createTwStart();
		End twE = createTwEnd();
		twS.setContent(v[4]);
		twE.setContent(v[5]);
		tw.setStart(twS);
		tw.setEnd(twE);
		r.getTw().add(tw);
		
		Time t = createTime();
		t.getContent().add(v[6]);
		r.setServiceTime(t);
		
		
		return r;
	}

}
