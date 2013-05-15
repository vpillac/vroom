package vroom.optimization.online.jmsa.vrp;

import vroom.common.modeling.dataModel.NodeVisit;
import vroom.optimization.online.jmsa.ISampledRequest;

/**
 * Creation date: Apr 29, 2010 - 10:32:00 AM<br/>
 * <code>VRPSampledRequest</code>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSampledRequest extends VRPRequest implements ISampledRequest {

	public VRPSampledRequest(NodeVisit nodeVisit) {
		super(nodeVisit);
	}

	@Override
	public NodeVisit clone() {
		throw new UnsupportedOperationException();
	}

}
