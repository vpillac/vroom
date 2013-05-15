package vroom.common.modeling.vrprep.translations;

import java.io.IOException;
import java.math.BigInteger;

import org.w3c.dom.Element;

import vroom.common.modeling.vrprep.Custom;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Location;
import vroom.common.modeling.vrprep.Location.Euclidean;

/**
 * Golden file format converter
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class Golden extends VRPLibFormat implements Translator {

	
	
	/* (non-Javadoc)
	 * @see vroom.common.modeling.vrprep.translations.Translator#translateFile(java.lang.String)
	 */
	@Override
	public void translateFile(String file) {
		openFileReader(file);

		String fileName = file.substring(file.lastIndexOf("\\")+1, file.lastIndexOf("."));

		info.setReference("Golden Instances from RHSmith website");

		String l = "";
		String [] ls = null;
		
		String optimalValue = "";
		
		try {
			while(!(l = br.readLine()).contains("NODE_COORD_SECTION") && !l.contains("EDGE_WEIGHT_SECTION") && !l.contains("DEMAND_SECTION")){
				l = l.replaceAll("^\\s+", "");
				ls = l.split("\\s+");
				
				ls[0] = ls[0].replaceAll(":", "");
				
				// instance name
				if(ls[0].equals("NAME")){
					info.setName(ls[1]);
				}
				// problem name
				if(ls[0].equals("TYPE")){
					info.setProblem(ls[1]);
				}	
				// comments
				if(ls[0].equals("COMMENT")){
					// reference
					optimalValue = ls[1];
				}		
				// vehicle capacity
				if(ls[0].equals("CAPACITY")){
					vehicleCap = Double.valueOf(ls[1]);
				}
				// distance
				if(ls[0].equals("DISTANCE")){
					vehicleDist = Double.valueOf(ls[1]);
				}
				// nb nodes
				if(ls[0].equals("DIMENSION")){
					nbNodes = Integer.valueOf(ls[1]);					
				}
				// nb vehicles
				if(ls[0].equals("VEHICLES")){
					vehicleNumber = Integer.valueOf(ls[1]);					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		super.translateData(true, l, false);
		
		network.getDescriptor().setDistanceType("Euclidean 2D");
		
		// optimal value
		Custom c = createCustom();
		Element el = document.createElement("bestKnownSolution");
		el.setTextContent(optimalValue);
		network.getDescriptor().setCustom(c);
		
		// add depot
		while(!ls[0].equals("DEPOT_SECTION")){
			l = l.replaceAll("^\\s+", "");
			ls = l.split("\\s+");
		}
		Node n = createInstanceNetworkNodesNode();
		n.setId(BigInteger.valueOf(0));
		n.setType(BigInteger.valueOf(0));
		Euclidean e = createLocationEuclidean();
		e.setCx(Double.valueOf(ls[0]));
		e.setCx(Double.valueOf(ls[1]));
		Location loc = createLocation();
		loc.setEuclidean(e);
		n.setLocation(loc);
		network.getNodes().getNode().add(n);
		
		marshalFile("./../InstancesMax/VRPRep/cvrp/golden_benchmarks/"+fileName+".xml");
	}

}
