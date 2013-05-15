package vroom.trsp.optimization.mpa;

import java.util.Collections;
import java.util.List;

import vroom.optimization.online.jmsa.ISampledRequest;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.RequestSamplerBase;
import vroom.optimization.online.jmsa.components.RequestSamplerParam;

/**
 * <code>DTRSPRequestSampler</code>
 * <p>
 * Creation date: Feb 7, 2012 - 11:35:43 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPRequestSampler extends RequestSamplerBase {

    public DTRSPRequestSampler(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    @Override
    public List<ISampledRequest> generateSampledRequest(RequestSamplerParam params) {
        return Collections.emptyList();
    }

}
