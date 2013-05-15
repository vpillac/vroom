package vroom.optimization.online.jmsa.vrp;

import java.util.LinkedList;
import java.util.List;

import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.utilities.Utilities;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.ISampledRequest;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorBase;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorParam;
import vroom.optimization.online.jmsa.utils.MSALogging;

/**
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 18-Feb-2010 10:51:12 a.m.
 */
public class VRPScenarioGeneratorBase<S extends VRPScenario> extends ScenarioGeneratorBase<S> {

    private final MSAVRPSolutionFactory mScenarioFactory;

    /**
     * Getter for the scenario factory used to instantiate scenarios
     * 
     * @return the {@link MSAVRPSolutionFactory} associated with this instance.
     */
    public MSAVRPSolutionFactory getScenarioFactory() {
        return mScenarioFactory;
    }

    /**
     * @param componentManager
     */
    public VRPScenarioGeneratorBase(ComponentManager<S, ?> componentManager) {
        super(componentManager);

        mScenarioFactory = getComponentManager().getParentMSAProxy().getParameters()
                .newInstance(VRPParameterKeys.SCENARIO_FACTORY_CLASS);
    }

    @Override
    public S generateScenario(ScenarioGeneratorParam params) {
        MSALogging.getComponentsLogger().debug("Generating a new scenario (params:%s)", params);

        // Generate the sampled requests
        List<VRPSampledRequest> sampledRequests = new LinkedList<VRPSampledRequest>();
        // Add only the requests of the good type
        for (ISampledRequest r : getComponentManager().generateSampledRequest(
                params.getSamplerParams())) {
            if (r instanceof VRPSampledRequest) {
                sampledRequests.add((VRPSampledRequest) r);
            }
        }

        @SuppressWarnings("unchecked")
        S scenario = (S) getScenarioFactory().newSolution(
                (IVRPInstance) getComponentManager().getParentMSA().getInstance(),
                Utilities.convertToList(((IInstance) getComponentManager().getParentMSA()
                        .getInstance()).getPendingRequests()), sampledRequests);

        return scenario;
    }

}// end VRPScenarioGeneratorBase