package vroom.optimization.online.jmsa.components;

import java.util.List;

import vroom.optimization.online.jmsa.ISampledRequest;

/**
 * <code>RequestSamplerBase</code> is the base type for all classes responsible for the generation of sampled requests
 * to be included in scenarios
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #updated 16-Feb-2010 10:06:52 a.m.
 */
public abstract class RequestSamplerBase extends MSAComponentBase {

    public RequestSamplerBase(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    /**
     * Generation of sampled requests
     * 
     * @return an array of length <code>numRequests</code> containing the generated sampled requests
     * @param params
     *            an optional parameter for the generation of sampled requests
     */
    public abstract List<ISampledRequest> generateSampledRequest(RequestSamplerParam params);

}