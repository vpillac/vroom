/**
 * 
 */
package vroom.trsp.optimization.mpa;

import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorBase;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorParam;
import vroom.trsp.MPASolver;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;

/**
 * <code>DTRSPScenarioGenerator</code> is a scenario generator for the D-TRSP
 * <p>
 * Creation date: Feb 7, 2012 - 10:59:57 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPScenarioGenerator extends ScenarioGeneratorBase<DTRSPSolution> {

    private final TRSPCostDelegate mInitCostDelegate;

    /**
     * Returns the instance being optimized
     * 
     * @return the instance being optimized
     */
    private TRSPInstance getInstance() {
        return (TRSPInstance) getMSAProxy().getInstance();
    }

    public DTRSPScenarioGenerator(ComponentManager<DTRSPSolution, ?> componentManager) {
        super(componentManager);
        mInitCostDelegate = getMSAProxy().getParameters().get(MPASolver.TRSP_MPA_SOLVER).getParams()
                .newInitCostDelegate();
    }

    @Override
    public DTRSPSolution generateScenario(ScenarioGeneratorParam params) {
        DTRSPSolution scenario = new DTRSPSolution(getInstance(), mInitCostDelegate);
        return scenario;
    }

}
