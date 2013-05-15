package vroom.optimization.online.jmsa.vrp;

import java.util.Collection;

import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.IMSARequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.PoolCleanerBase;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a> - <a href="http://copa.uniandes.edu.co">Copa</a>, <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 18-Feb-2010 10:51:12 a.m.
 */
public class VRPPoolCleanerBase extends PoolCleanerBase {

	public VRPPoolCleanerBase(ComponentManager<VRPScenario, ?> componentManager) {
		super(componentManager);
	}

	@Override
	public Collection<IScenario> cleanPool() {
		// TODO Implement cleanPool
		MSALogging.getComponentsLogger().info("Cleaning the pool");

		return null;
	}

	@Override
	public boolean isScenarioCompatible(IInstance instance, IScenario scenario) {
		// TODO Implement isScenarioCompatible
		return true;
	}

	@Override
	public boolean isScenarioCompatible(IMSARequest request, IScenario scenario) {
		// TODO Implement isScenarioCompatible
		return true;
	}

}// end VRPPoolCleanerBase