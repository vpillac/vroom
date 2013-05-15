package vroom.common.modeling.vrprep.translations;

import java.io.IOException;

import org.w3c.dom.Element;

import vroom.common.modeling.vrprep.Custom;

/**
 * Augerat file format converter
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class Augerat extends VRPLibFormat implements Translator {


	/* (non-Javadoc)
	 * @see vroom.common.modeling.vrprep.translations.Translator#translateFile(java.lang.String)
	 */
	@Override
	public void translateFile(String file) {
		openFileReader(file);

		String fileName = file.substring(file.lastIndexOf("\\")+1, file.lastIndexOf("."));

		String optimalValue ="";
		String minNumVehicles = "";

		String l = "";
		String [] ls;
		
		String edgeType = "";

		try {
			while(!(l = br.readLine()).contains("NODE_COORD_SECTION") && !l.contains("EDGE_WEIGHT_SECTION") && !l.contains("DEMAND_SECTION")){
				l = l.replaceAll("^\\s+", "");
				ls = l.split("\\s+");
				
				// instance name
				if(ls[0].equals("NAME")){
					info.setName(ls[2]);
				}
				// problem name
				if(ls[0].equals("TYPE")){
					info.setProblem(ls[2]);
				}	
				// comments
				if(ls[0].equals("COMMENT")){
					// reference
					info.setReference(l.substring(l.indexOf("(")+1, l.indexOf(",")));
					
					//min number of vehicles
					minNumVehicles = (l.split(":")[2]).substring(0, l.split(":")[2].indexOf(",")).replaceAll("^\\s+", "");
					
					// optimal value
					optimalValue = l.substring(l.lastIndexOf(":")+1, l.lastIndexOf("")-1).replaceAll("^\\s+", "");
				}		
				// link distance type
				if(ls[0].equals("EDGE_WEIGHT_TYPE")){
					edgeType = ls[2];
				}
				// vehicle capacity
				if(ls[0].equals("CAPACITY")){
					vehicleCap = Double.valueOf(ls[2]);
				}
				//nb nodes
				if(ls[0].equals("DIMENSION")){
					nbNodes = Integer.valueOf(ls[2]);					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		super.translateData(false, l, true);

		// distance type
		network.getDescriptor().setDistanceType(edgeType);
		
		
		// optimal solution value
		if(!optimalValue.equals("")){
			Custom custom = createCustom();
			Element e = document.createElement("optimalValue");
			e.setTextContent(optimalValue);			
			custom.getAny().add(e);
			network.getDescriptor().setCustom(custom);
		}
		
		// minimum number of vehicles
		if(!minNumVehicles.equals("")){
			Custom custom1 = createCustom();
			Element e = document.createElement("mininumNumber");
			e.setTextContent(minNumVehicles);			
			custom1.getAny().add(e);
			fleet.getVehicle().get(0).setCustom(custom1);
		}
		
		marshalFile("./../InstancesMax/VRPRep/cvrp/augerat/"+fileName+".xml");
	}

}
