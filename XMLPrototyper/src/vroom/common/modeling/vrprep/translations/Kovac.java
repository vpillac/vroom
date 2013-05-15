package vroom.common.modeling.vrprep.translations;

import java.io.IOException;
import java.math.BigInteger;

import javax.xml.namespace.QName;

import vroom.common.modeling.vrprep.Demand;
import vroom.common.modeling.vrprep.Instance.Fleet.Vehicle;
import vroom.common.modeling.vrprep.Instance.Network.Descriptor;
import vroom.common.modeling.vrprep.Instance.Network.Nodes;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Instance.Requests.Request;
import vroom.common.modeling.vrprep.Location;
import vroom.common.modeling.vrprep.Location.Euclidean;
import vroom.common.modeling.vrprep.Skill;
import vroom.common.modeling.vrprep.Time;
import vroom.common.modeling.vrprep.Tw;
import vroom.common.modeling.vrprep.Tw.End;
import vroom.common.modeling.vrprep.Tw.Start;

/**
 * Kovac file format converter
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class Kovac extends Tools implements Translator {
	
	private int nbSkills;
	private int nbLevels;
	private boolean hasTeam;
	
	public Kovac (boolean hasTeam){
		this.hasTeam = hasTeam;
	}

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
		info.setProblem("STRSP");
		info.setReference("Kovac STRSP instances");
		
		// nb skills
		nbSkills = Integer.valueOf(fileName.substring(fileName.indexOf("_")+1, fileName.indexOf("x")));
		// nb levels
		nbLevels = Integer.valueOf(fileName.substring(fileName.indexOf("x")+1, fileName.lastIndexOf("_")));

		int custNumber = -2;
		double capacity = -2;

		try {	
			// capacity
			l = br.readLine().replaceAll("^\\s+", "");
			ls = l.split("\\s+");		
			capacity = Double.valueOf(ls[1]);
			// number of customers
			l = br.readLine().replaceAll("^\\s+", "");
			ls = l.split("\\s+");		
			custNumber = Integer.valueOf(ls[1]);
			// while not at first line of values
			while(!ls[0].equals("1")){
				l = br.readLine().replaceAll("^\\s+", "");
				ls = l.split("\\s+");
			}
			// depot
			Nodes nodes = createInstanceNetworkNodes();
			nodes.getNode().add(createNode(ls, true));
			requests.getRequest().add(createRequest(ls, true));

			// customers
			for(int i = 0; i < custNumber-1; i++){
				l = br.readLine().replaceAll("^\\s+", "");
				ls = l.split("\\s+");
				nodes.getNode().add(createNode(ls, false));
				requests.getRequest().add(createRequest(ls, false));
			}
			network.setNodes(nodes);

		} catch (IOException e) {
			e.printStackTrace();
		}


		//descriptor
		Descriptor d = createInstanceNetworkDescriptor();
		d.setIsComplete(true);
		d.setDistanceType("Euclidean 2D");
		network.setDescriptor(d);
		
		//vehicle
		Vehicle v = createInstanceFleetVehicle();
		v.getCapacity().add(capacity);
		fleet.getVehicle().add(v);

		if(hasTeam)
			marshalFile("./../InstancesMax/VRPRep/strsp/kovacs/tasks_Team/"+fileName+".xml");
		else
			marshalFile("./../InstancesMax/VRPRep/strsp/kovacs/tasks_noTeam/"+fileName+".xml");

	}

	/**
	 * Create VRPRep node with location 
	 * @param ls split values of buffered reader line
	 * @param isDepot true if is a depot
	 * @return new Node
	 */
	private Node createNode(String[] ls, boolean isDepot){
		Node n = createInstanceNetworkNodesNode();
		Location loc;
		Euclidean e;

		e = createLocationEuclidean();
		e.setCx(Double.valueOf(ls[1]));
		e.setCy(Double.valueOf(ls[2]));
		loc = createLocation();
		loc.setEuclidean(e);

		n.setLocation(loc);
		n.setId(BigInteger.valueOf(Integer.valueOf(ls[0])));

		if(isDepot)
			n.setType(BigInteger.valueOf(0));
		else
			n.setType(BigInteger.valueOf(1));

		return n;
	}


	/**
	 * Create VRPRep request
	 * @param v split values of buffered reader line
	 * @return new Request
	 */
	private Request createRequest(String[] ls, boolean isDepot){
		Request r = createInstanceRequestsRequest();
		// time window
		Tw tw = createTw();
		Start twS = createTwStart();
		End twE = createTwEnd();
		twS.setContent(ls[4]);
		twE.setContent(ls[5]);
		tw.setStart(twS);
		tw.setEnd(twE);
		r.getTw().add(tw);
		// meta deta
		r.setId(BigInteger.valueOf(Integer.valueOf(ls[0])));
		r.setNode(BigInteger.valueOf(Integer.valueOf(ls[0])));

		if(!isDepot){
			// demand
			Demand d = createDemand();
			d.getContent().add(ls[3]);
			r.getDemand().add(d);
			// service time
			Time t = createTime();
			t.getContent().add(ls[6]);
			r.setServiceTime(t);
			// cost
			r.setCost(Double.valueOf(ls[7]));
			
			// skills with levels
			int index = 8;
			for (int s = 1; s <= nbSkills; s++){
				for (int l = 1; l <= nbLevels; l++){
					if(Integer.valueOf(ls[index++]) > 0){
						Skill skill = createSkill();
						skill.setId(BigInteger.valueOf(s));
						skill.getOtherAttributes().put(QName.valueOf("level"), String.valueOf(l));	
						skill.setContent(ls[index-1]);
						r.getSkill().add(skill);
					}
				}
			}
		}
		

		return r;
	}

}
