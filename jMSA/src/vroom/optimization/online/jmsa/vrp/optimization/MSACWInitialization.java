package vroom.optimization.online.jmsa.vrp.optimization;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.vls.IVLSState;
import vroom.common.heuristics.vls.vrp.CWInitialization;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.optimization.online.jmsa.vrp.MSAVRPSolutionFactory;
import vroom.optimization.online.jmsa.vrp.VRPScenario;

/**
 * Creation date: 1 mai 2010 - 18:14:27<br/>
 * <code>MSACWInitialization</code> is a specialization of
 * {@link CWInitialization} that use a CW heuristic to initialize a scenario.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MSACWInitialization extends CWInitialization<VRPScenario> {

	/**
	 * Creates a new <code>MSACWInitialization</code>
	 */
	public MSACWInitialization(RandomStream randomStream) {
		super(MSAVRPSolutionFactory.class, MSACWSavingsHeuristic.class);

		getCW().getConstraintHandler().addConstraint(
				new CapacityConstraint<VRPScenario>());

		getCW().setRandomStream(randomStream);

		getCW().setSolutionFactory(
				new MSAVRPSolutionFactory(ArrayListRoute.class));

	}

	@Override
	public VRPScenario newSolution(IVLSState<VRPScenario> state,
			IInstance instance, IParameters params) {

		getCW().initialize((IVRPInstance) instance,
				((VRPScenarioInstanceSmartAdapter) instance).getScenario());

		getCW().run();

		return getCW().getSolution();
	}
}
