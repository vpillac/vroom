package vroom.optimization.online.jmsa.vrp;

import java.util.List;

import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.ISampledRequest;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.RequestSamplerBase;
import vroom.optimization.online.jmsa.components.RequestSamplerParam;

/**
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 18-Feb-2010 10:51:14 a.m.
 */
public class VRPRequestSamplerBase<I extends IInstance> extends
		RequestSamplerBase {

	/**
	 * Creates a new component
	 * 
	 * @param componentManager
	 *            the parent component manager
	 */
	public VRPRequestSamplerBase(ComponentManager<?, I> componentManager) {
		super(componentManager);
	}

	@Override
	public List<ISampledRequest> generateSampledRequest(
			RequestSamplerParam params) {
		// TODO implement generateSampledRequest

		throw new UnsupportedOperationException("Not implemented yet");

		// List<ISampledRequest> requests = new LinkedList<ISampledRequest>();
		//
		// for (int r = 0; r < params.getNumSampledRequests(); r++) {
		// }
		//
		// return requests;
	}

}// end VRPRequestSamplerBase