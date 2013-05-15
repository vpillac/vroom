package vroom.optimization.online.jmsa.vrp.vrpsd;

import java.util.LinkedList;
import java.util.List;

import umontreal.iro.lecuyer.randvar.RandomVariateGen;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.NodeVisit;
import vroom.common.modeling.dataModel.attributes.IDemand;
import vroom.common.modeling.dataModel.attributes.IStochasticDemand;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.ssj.RandomGeneratorManager;
import vroom.optimization.online.jmsa.IInstance;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.ScenarioGeneratorParam;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;
import vroom.optimization.online.jmsa.vrp.VRPSampledRequest;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioGeneratorBase;

/**
 * Creation date: Apr 19, 2010 - 11:46:58 AM<br/>
 * <code>VRPSDScenarioGenerator</code> is a custom scenario generator for the VRPSD.
 * <p>
 * It does not add sampled requests but instead sample the clients demand distributions to create a scenario.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @param <S>
 */
public class VRPSDScenarioGenerator<S extends VRPScenario> extends VRPScenarioGeneratorBase<S> {

    private final RandomGeneratorManager sRndGenManager;

    public VRPSDScenarioGenerator(ComponentManager<S, ?> componentManager) {
        super(componentManager);

        this.sRndGenManager = new RandomGeneratorManager();
        this.sRndGenManager.setRandomStream(componentManager.getParentMSAProxy().getGenerationRandomStream());
    }

    @Override
    public S generateScenario(ScenarioGeneratorParam params) {
        MSALogging.getComponentsLogger().lowDebug(
            "VRPSDScenarioGenerator.generateScenario: Generating a new scenario (params:%s)", params);

        // No sampled requests are used in this case
        List<VRPSampledRequest> sampledRequests = new LinkedList<VRPSampledRequest>();

        List<VRPActualRequest> actualRequests = Utilities.convertToList(((IInstance) getComponentManager().getParentMSA()
            .getInstance()).getPendingRequests());

        List<VRPActualRequest> sampledDemandsRequests = new LinkedList<VRPActualRequest>();

        for (VRPActualRequest req : actualRequests) {
            if (req.getWrappedObject().getAttribute(RequestAttributeKey.DEMAND) instanceof IStochasticDemand) {
                SampledRequest sampledReq = new SampledRequest(req.getWrappedObject(),
                    sampleDemands(req.getWrappedObject()));
                sampledDemandsRequests.add(new VRPSDActualRequest(NodeVisit.createNodeVisits(sampledReq)[0]));
            } else {
                sampledDemandsRequests.add(req);
            }
        }

        @SuppressWarnings("unchecked")
        S scenario = (S) getScenarioFactory().newSolution(
            (IVRPInstance) getComponentManager().getParentMSA().getInstance(), sampledDemandsRequests,
            sampledRequests);

        MSALogging.getComponentsLogger().lowDebug(
            "VRPSDScenarioGenerator.generateScenario: Scenario generated, actual requests: %s",
            sampledDemandsRequests);

        return scenario;
    }

    private double[] sampleDemands(IVRPRequest req) {
        IDemand demand = req.getAttribute(RequestAttributeKey.DEMAND);
        double[] samples = new double[demand.getProductCount()];

        for (int p = 0; p < samples.length; p++) {
            if (demand instanceof IStochasticDemand) {
                double d = 0;
                // Synchronize with the random generator instance to prevent concurrent accesses
                // synchronized (sRndGenManager) {
                // d = this.sRndGenManager.nextDouble(((IStochasticDemand) demand).getDistribution(p));
                RandomVariateGen gen = new RandomVariateGen(sRndGenManager.getRandomStream(),
                    ((IStochasticDemand) demand).getDistribution(p));
                d = gen.nextDouble();
                // }
                d = Math.round(d * 1000) / 1000d;
                d = d < 0 ? 0
                        : d > getInstance().getFleet().getVehicle().getCapacity() ? getInstance().getFleet()
                            .getVehicle()
                            .getCapacity() : d;

                samples[p] = d;
            } else {
                samples[p] = demand.getDemand(p);
            }
        }

        return samples;
    }

    protected MSAVRPInstance getInstance() {
        return (MSAVRPInstance) getMSAProxy().getInstance();
    }
}
