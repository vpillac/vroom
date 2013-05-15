/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.vrpsd;

import vroom.optimization.online.jmsa.components.ComponentManager;

/**
 * <code>VRPSDSmartConsensus</code> is an improvement of {@link VRPSDConsensus} to limit premature replenishment trips.
 * <p>
 * Creation date: Oct 4, 2010 - 5:48:08 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDSmartConsensusDepot extends VRPSDSmartConsensus {

    public VRPSDSmartConsensusDepot(ComponentManager<?, ?> componentManager) {
        super(componentManager);
        sDepotOnly = true;
    }

}
