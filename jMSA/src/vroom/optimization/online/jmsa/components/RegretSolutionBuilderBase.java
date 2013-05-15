/*
 * 
 */
package vroom.optimization.online.jmsa.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import vroom.optimization.online.jmsa.DistinguishedSolutionBase;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * The Class <code>RegretSolutionBuilderBase</code> is a method pattern for
 * problem specific implementations of the regret algorithm.
 * <p>
 * Creation date: Aug 24, 2010 - 11:28:46 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los
 *         Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class RegretSolutionBuilderBase<S extends IScenario> extends
		SolutionBuilderBase {

	@SuppressWarnings("unchecked")
	@Override
	protected ComponentManager<S, ?> getComponentManager() {
		return (ComponentManager<S, ?>) super.getComponentManager();
	}

	/**
	 * Creates a new <code>RegretSolutionBuilder</code> associated with the
	 * given component manager.
	 * 
	 * @param componentManager
	 */
	public RegretSolutionBuilderBase(ComponentManager<S, ?> componentManager) {
		super(componentManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see vroom.optimization.online.jmsa.components.SolutionBuilderBase#
	 * buildDistinguishedPlan(vroom.optimization.online.jmsa.components.
	 * ISolutionBuilderParam)
	 */
	@Override
	public IDistinguishedSolution buildDistinguishedPlan(
			ISolutionBuilderParam param) {

		int nextRequestId = -1;
		double bestEval = 0;
		Map<Integer, Double> evaluations = new HashMap<Integer, Double>();

		Collection<? extends IActualRequest> candidates = selectCandidateRequests();

		IActualRequest nextRequest;
		// Failsafe: ignore when no candidate
		if (candidates.isEmpty()) {
			return defaultDecision(param);
		}
		// Ignore when only one candidate
		else if (candidates.size() == 1) {
			nextRequest = candidates.iterator().next();
		} else {
			bestEval = Double.POSITIVE_INFINITY;

			for (IActualRequest r : candidates) {
				int rID = r.getID();
				double eval = 0;
				for (S s : getComponentManager().getParentMSAProxy()
						.getScenarioPool()) {
					s.acquireLock();
					eval = evaluateRegret(r, s, eval);
					s.releaseLock();
				}
				evaluations.put(rID, eval);
				if (eval < bestEval) {
					bestEval = eval;
					nextRequestId = rID;
				}

			}
			nextRequest = getComponentManager().getParentMSAProxy()
					.getInstance().getNodeVisit(nextRequestId);

		}

		MSALogging
				.getComponentsLogger()
				.info("%s.buildDistinguishedPlan: best request found : %s - score:%s",
						this.getClass().getSimpleName(), nextRequest, bestEval);

		return new DistinguishedSolutionBase(nextRequest);
	}

	/**
	 * Default request when no suitable candidate was found
	 * 
	 * @param param
	 * @return a default request
	 */
	protected abstract IDistinguishedSolution defaultDecision(
			ISolutionBuilderParam param);

	/**
	 * Approximation of the regret value of forcing the request <code>r</code>
	 * to be visited first in scenario <code>s</code>.
	 * <p>
	 * The request with the <b>lowest</b> aggregated regret value will be
	 * selected.
	 * 
	 * @param r
	 *            the request being evaluated
	 * @param s
	 *            the scenario being considered
	 * @param currentValue
	 *            the current aggregated regret value for request <code>r</code>
	 * @return the new aggregated regret value for request <code>r</code>
	 */
	protected abstract double evaluateRegret(IActualRequest r, S s,
			double currentValue);

	/**
	 * Candidate requests selection
	 * 
	 * @return an iterable object containing the selected candidate requests
	 */
	protected Collection<? extends IActualRequest> selectCandidateRequests() {
		return getComponentManager().getParentMSAProxy().getInstance()
				.getPendingRequests();
	}
}
