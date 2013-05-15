/**
 * 
 */
package vroom.common.heuristics.vns;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.optimization.IHeuristicParameters;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.IStoppingCriterion;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.common.utilities.optimization.SimpleStoppingCriterion;

/**
 * <code>VNSParameters</code> is an extension of {@link SimpleParameters} for
 * the {@link VariableNeighborhoodSearch}
 * <p>
 * Creation date: 11 juil. 2010 - 21:06:06
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VNSParameters extends SimpleParameters implements
		IHeuristicParameters {

	private final IStoppingCriterion mStoppingCriterion;

	private final IParameters mLSParameters;
	private final IParameters mShakeParameters;

	/**
	 * Define new <code>VNSParameters</code>
	 * 
	 * @param vnsMaxTime
	 *            maximum time (in ms) for the vns
	 * @param vnsMaxIt
	 *            maximum number of iterations for the vns
	 * @param rndShake
	 *            <code>true</code> if the shake should be randomized
	 * @param shakeMaxTime
	 *            maximum time (in ms) for the shake
	 * @param shakeSampleSize
	 *            size of the sample when the shake is randomized
	 * @param lsParameters
	 *            parameters for the local search
	 * @param rndStream
	 *            the random stream that will be used in the VNS
	 */
	public VNSParameters(long vnsMaxTime, int vnsMaxIt, boolean rndShake,
			long shakeMaxTime, int shakeSampleSize, IParameters lsParameters,
			RandomStream rndStream) {
		super(vnsMaxTime, vnsMaxIt, true, rndShake, false, rndStream);
		mStoppingCriterion = new SimpleStoppingCriterion(vnsMaxTime, vnsMaxIt);
		mLSParameters = lsParameters;
		mShakeParameters = new SimpleParameters(shakeMaxTime, shakeSampleSize,
				rndShake, rndShake, false, rndStream);
	}

	@Override
	public IStoppingCriterion getStoppingCriterion() {
		return mStoppingCriterion;
	}

	/**
	 * @return <code>true</code> if the shake should be randomized
	 */
	@Override
	public boolean randomize() {
		return super.randomize();
	}

	/**
	 * Getter for the local search parameters
	 * 
	 * @return the parameters to be used by the local search
	 */
	public IParameters getLSParameters() {
		return mLSParameters;
	}

	/**
	 * Getter for the shake parameters
	 * 
	 * @return the parameters to be used by the shake
	 */
	public IParameters getShakeParameters() {
		return mShakeParameters;
	}
}
