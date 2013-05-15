/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.optimization;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.vls.IVLSState;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.optimization.online.jmsa.vrp.VRPScenario;

/**
 * <code>VRPSmartInitialization</code>
 * <p>
 * Creation date: Sep 28, 2010 - 3:12:25 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSmartInitialization extends MSACWInitialization {

	/** A threshold for the number of non-improving optimizations **/
	private int mNonImprThreshold;
	private VRPScenario scen;

	/**
	 * Getter for A threshold for the number of non-improving optimizations
	 * 
	 * @return the value of nonImprThreshold
	 */
	public int getNonImprThreshold() {
		return mNonImprThreshold;
	}

	/**
	 * Setter for A threshold for the number of non-improving optimizations
	 * 
	 * @param nonImprThreshold
	 *            the value to be set for A threshold for the number of
	 *            non-improving optimizations
	 */
	public void setNonImprThreshold(int nonImprThreshold) {
		mNonImprThreshold = nonImprThreshold;
	}

	public VRPSmartInitialization(RandomStream randomStream) {
		super(randomStream);
		setNonImprThreshold(2);
	}

	@Override
	public VRPScenario newSolution(IVLSState<VRPScenario> state,
			IInstance instance, IParameters params) {
		scen = ((VRPScenarioInstanceSmartAdapter) instance).getScenario();
		if (scen.getNonImprovingCount() >= getNonImprThreshold()) {
			return super.newSolution(state, instance, params);
		} else {
			return scen.clone();
		}
	}

}
