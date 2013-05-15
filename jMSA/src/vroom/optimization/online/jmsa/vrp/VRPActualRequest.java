package vroom.optimization.online.jmsa.vrp;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.optimization.online.jmsa.IActualRequest;

/**
 * Creation date: Apr 29, 2010 - 10:25:12 AM<br/>
 * <code>VRPActualRequest</code>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPActualRequest extends VRPRequest implements IActualRequest {

	public VRPActualRequest(INodeVisit nodeVisit) {
		super(nodeVisit);
	}

	@Override
	public VRPActualRequest clone() {
		return new VRPActualRequest(getNodeVisit());
	}

}
