package vroom.common.modeling.vrprep.translations;

import java.io.IOException;


/**
 *Fischetti, Toth and Vigo file format converter
 * @author Maxim Hoskins <a href="https://plus.google.com/115909706630698463631/about">Profil Google+</a>
 *
 */
public class FischettiTothAndVigo extends VRPLibFormat implements Translator {


	/* (non-Javadoc)
	 * @see vroom.common.modeling.vrprep.translations.Translator#translateFile(java.lang.String)
	 */
	@Override
	public void translateFile(String file) {
		openFileReader(file);

		String fileName = file.substring(file.lastIndexOf("\\")+1, file.lastIndexOf("."));


		String l = "";
		String [] ls;
		
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
					info.setReference(l.substring(l.indexOf(":"), l.length()));
				}		
				// vehicle capacity
				if(ls[0].equals("CAPACITY")){
					vehicleCap = Double.valueOf(ls[2]);
				}
				// nb nodes
				if(ls[0].equals("DIMENSION")){
					nbNodes = Integer.valueOf(ls[2]);					
				}
				// nb vehicles
				if(ls[0].equals("VEHICLES")){
					vehicleNumber = Integer.valueOf(ls[2]);					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		super.translateData(true, l, true);
		
		marshalFile("./../InstancesMax/VRPRep/acvrp/FischettiTothAndVigo/"+fileName+".xml");
	}
}
