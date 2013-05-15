/**
 * 
 */
package vroom.common.heuristics.vls.vrp;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.IInitialization;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.cw.kernel.ISavingsAlgorithm;
import vroom.common.heuristics.vls.IVLSState;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;

/**
 * <code>CWInitialization</code> is an implementation of {@link IInitialization}
 * relying on a {@link ClarkeAndWrightHeuristic} to build a routing plan.
 * <p>
 * Creation date: Apr 30, 2010 - 11:29:11 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CWInitialization<S extends IVRPSolution<?>> implements
		IInitialization<S> {

	/** The CW heuristic used in this instance */
	private final ClarkeAndWrightHeuristic<S> mCW;

	/**
	 * Getter for the CW heuristic
	 * 
	 * @return the Clark and Wright heuristic used in this instance
	 */
	public ClarkeAndWrightHeuristic<S> getCW() {
		return mCW;
	}

	/** The parameters for the CW heuristic */
	private final CWParameters mParameters;

	/**
	 * Creates a new <code>CWInitialization</code> from an existing CW heuristic
	 * 
	 * @param cW
	 * @param parameters
	 */
	public CWInitialization(ClarkeAndWrightHeuristic<S> cW,
			CWParameters parameters) {
		mCW = cW;
		mParameters = parameters;
	}

	/**
	 * Creates a new <code>CWInitialization</code> with a default configuration.
	 * 
	 * @param routeClazz
	 *            the implementation of {@link IRoute} to be used
	 * @param savingsAlgoClass
	 *            the algorithm to be used in the CW procedure
	 */
	public CWInitialization(Class<? extends ISolutionFactory> solutionFactory,
			Class<? extends ISavingsAlgorithm<S>> savingsAlgoClass) {
		mParameters = new CWParameters();

		mParameters.set(CWParameters.SOLUTION_FACTORY_CLASS, solutionFactory);

		mCW = new ClarkeAndWrightHeuristic<S>(mParameters, savingsAlgoClass,
				new ConstraintHandler<S>());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * vroom.common.heuristics.vls.IInitialization#newSolution(vroom.common.
	 * heuristics.vls.IVLSState, vroom.common.heuristics.IInstance)
	 */
	@Override
	public S newSolution(IVLSState<S> state, IInstance instance,
			IParameters params) {
		getCW().initialize((IVRPInstance) instance);

		getCW().run();

		return getCW().getSolution();
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", this.getClass().getSimpleName(),
				getCW());
	}
}
