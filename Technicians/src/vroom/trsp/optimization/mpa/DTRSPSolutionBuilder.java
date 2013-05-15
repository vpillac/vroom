/**
 * 
 */
package vroom.trsp.optimization.mpa;

import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.ScenarioPool;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ISolutionBuilderParam;
import vroom.optimization.online.jmsa.components.SolutionBuilderBase;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.Technician;

/**
 * <code>DTRSPSolutionBuilder</code>
 * <p>
 * Creation date: Feb 9, 2012 - 3:24:28 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPSolutionBuilder extends SolutionBuilderBase {

    private final TRSPInstance mInstance;

    /**
     * Creates a new <code>DTRSPSolutionBuilder</code>
     * 
     * @param componentManager
     */
    public DTRSPSolutionBuilder(ComponentManager<?, ?> componentManager) {
        super(componentManager);
        mInstance = (TRSPInstance) getMSAProxy().getInstance();
    }

    @Override
    public IDistinguishedSolution buildDistinguishedPlan(ISolutionBuilderParam param) {
        @SuppressWarnings("unchecked")
        final ScenarioPool<DTRSPSolution> pool = (ScenarioPool<DTRSPSolution>) getComponentManager()
                .getParentMSAProxy().getScenarioPool();
        double[] nodeEval = new double[mInstance.getMaxId()];

        double bestScen = Double.POSITIVE_INFINITY;
        for (DTRSPSolution scen : pool) {
            if (bestScen > scen.getObjectiveValue())
                bestScen = scen.getObjectiveValue();
        }

        // Evaluate all requests in all scenarios
        for (DTRSPSolution scen : pool) {
            if (scen.getUnservedCount() > 0)
                continue;
            for (Technician t : mInstance.getFleet()) {
                IActualRequest first = scen.getFirstActualRequest(t.getID());
                if (first != null)
                    // nodeEval[first.getID()] += 1; // A request receive a score of 1 each time it appears in first
                    // position
                    nodeEval[first.getID()] += bestScen / scen.getObjectiveValue(); // A request receive a score of
                                                                                    // proportional to the scenario
                                                                                    // relative cost each time it
                                                                                    // appears in first
                // position
            }
        }

        // Select the best scenario depending on the evaluation of its requests
        DTRSPSolution best = null;
        double bestScenEval = Double.NEGATIVE_INFINITY;
        for (DTRSPSolution scen : pool) {
            // if (scen.getUnservedCount() > 0)
            // continue;
            double scenEval = 0;
            for (Technician t : mInstance.getFleet()) {
                IActualRequest first = scen.getFirstActualRequest(t.getID());
                if (first != null)
                    scenEval += nodeEval[first.getID()];
            }
            if (scenEval > bestScenEval) {
                bestScenEval = scenEval;
                best = scen;
            }
        }

        return best;
    }
}
