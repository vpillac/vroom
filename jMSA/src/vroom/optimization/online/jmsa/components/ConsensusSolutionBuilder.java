/**
 * 
 */
package vroom.optimization.online.jmsa.components;

import java.util.HashMap;
import java.util.Map;

import vroom.optimization.online.jmsa.DistinguishedSolutionBase;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * Creation date: Mar 8, 2010 - 4:26:46 PM<br/>
 * <code>ConsensusSolutionBuilder</code>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 2.0
 */
public class ConsensusSolutionBuilder extends SolutionBuilderBase {

    public ConsensusSolutionBuilder(ComponentManager<?, ?> componentManager) {
        super(componentManager);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.components.SolutionBuilderBase#
     * buildDistinguishedPlan
     * (vroom.optimization.online.jmsa.components.ISolutionBuilderParam)
     */
    @Override
    public IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam param) {

        int nextRequestId = -1;
        double eval = 0, bestEval = -1;
        Map<Integer, Double> evaluations = new HashMap<Integer, Double>();
        for (IScenario s : getComponentManager().getParentMSAProxy().getScenarioPool()) {
            s.acquireLock();

            IActualRequest req = s.getFirstActualRequest(0);
            if (req != null && isRequestFeasible(req)) {
                int reqId = req.getID();
                eval = updateEvaluation(evaluations, reqId);
                if (eval >= bestEval) {
                    bestEval = eval;
                    nextRequestId = reqId;
                }
            }
            s.releaseLock();
        }

        IActualRequest nextRequest = getComponentManager().getParentMSAProxy().getInstance()
                .getNodeVisit(nextRequestId);

        MSALogging
                .getComponentsLogger()
                .info("ConsensusSolutionBuilder.buildDistinguishedPlan: best request found : %s - score:%s",
                        nextRequest, bestEval);

        return new DistinguishedSolutionBase(nextRequest);
    }

    /**
     * Updates the evaluation of the given request
     * 
     * @param evaluations
     * @param reqId
     * @return the new evaluation
     */
    protected double updateEvaluation(Map<Integer, Double> evaluations, int reqId) {
        double eval;
        if (!evaluations.containsKey(reqId)) {
            eval = 1;
        } else {
            eval = evaluations.get(reqId) + 1;
        }

        evaluations.put(reqId, eval);

        return eval;
    }

    /**
     * Checks whether a request can be serviced next.
     * 
     * @param req
     *            the request to be tested
     * @returns <code>true</code> if <code>req</code> can be serviced next.
     */
    protected boolean isRequestFeasible(IActualRequest req) {
        return true;
    }

}
