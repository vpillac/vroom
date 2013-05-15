/**
 * 
 */
package vroom.trsp.optimization.mpa;

import java.util.Collection;

import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.PoolCleanerBase;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * <code>DTRSPPoolCleaner</code>
 * <p>
 * Creation date: Feb 7, 2012 - 11:29:11 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPPoolCleaner extends PoolCleanerBase {

    public DTRSPPoolCleaner(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.PoolCleanerBase#cleanPool()
     */
    @Override
    public Collection<IScenario> cleanPool() {
        // TODO Auto-generated method stub
        MSALogging.getComponentsLogger().info("Cleaning the pool");
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.components.PoolCleanerBase#isScenarioCompatible(vroom.optimization.online.jmsa
     * .IInstance, vroom.optimization.online.jmsa.IScenario)
     */
    @Override
    public boolean isScenarioCompatible(IInstance instance, IScenario scenario) {
        // TODO Auto-generated method stub
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.components.PoolCleanerBase#isScenarioCompatible(vroom.optimization.online.jmsa
     * .IMSARequest, vroom.optimization.online.jmsa.IScenario)
     */
    @Override
    public boolean isScenarioCompatible(IMSARequest request, IScenario scenario) {
        // TODO Auto-generated method stub
        return true;
    }

}
