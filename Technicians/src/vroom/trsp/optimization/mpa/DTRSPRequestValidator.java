/**
 * 
 */
package vroom.trsp.optimization.mpa;

import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.IParameters.LSStrategy;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.optimization.online.jmsa.components.ComponentManager;
import vroom.optimization.online.jmsa.components.RequestValidatorBase;
import vroom.trsp.MPASolver;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.alns.DestroyRelated;
import vroom.trsp.optimization.alns.DestroyStaticRelated;
import vroom.trsp.optimization.alns.RepairRegret;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>DTRSPRequestValidator</code>
 * <p>
 * Creation date: Feb 7, 2012 - 11:37:22 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPRequestValidator extends RequestValidatorBase {

    private final MPASolver      mSolver;

    private final DestroyRelated mDestroy;
    private final RepairRegret   mRepair;
    private final IParameters    mParams;

    public DTRSPRequestValidator(ComponentManager<?, ?> componentManager) {
        super(componentManager);
        mSolver = getMSAProxy().getParameters().get(MPASolver.TRSP_MPA_SOLVER);
        mDestroy = new DestroyStaticRelated(1, 1, 1, 1);
        mDestroy.initialize(mSolver.getInstance());
        mRepair = new RepairRegret(mSolver.getParams(), mSolver.getTourCtrHandler(), 3, false);
        mParams = new SimpleParameters(LSStrategy.DET_BEST_IMPROVEMENT, Long.MAX_VALUE,
                Integer.MAX_VALUE, mSolver.getParams().getRandomStream());
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.online.jmsa.components.RequestValidatorBase#isScenarioCompatible(vroom.optimization.online
     * .jmsa.IScenario, vroom.optimization.online.jmsa.IActualRequest)
     */
    @Override
    public boolean isScenarioCompatible(IScenario scenario, IActualRequest request) {
        DTRSPSolution scen = (DTRSPSolution) scenario;
        TRSPRequest req = (TRSPRequest) request;

        // FIXME [12/07/03] check if why this seems to reject too many requests

        // The scenario serves all its current request, try to insert the new request directly
        if (scen.getUnservedCount() == 0) {
            InsertionMove mve = InsertionMove.findInsertion(req.getID(), scen,
                    scen.getCostDelegate(), mSolver.getTourCtrHandler(), true, false);

            if (mve != null && mve.isFeasible()) {
                return true;
            }
        }

        // [12/06/14] Put more effort on checking whether the scenario is compatible or not
        // Idea: use related destroy to remove requests then regret repair
        DTRSPSolution clone = scen.clone();

        clone.markAsUnserved(request.getID());
        mRepair.repair(clone, mDestroy.destroy(clone, mParams, 0.2), mParams);

        return clone.getUnservedCount() == 0;

    }

    @Override
    public void requestAccepted(IActualRequest request) {
        super.requestAccepted(request);
        TRSPLogging.getSimulationLogger().info(
                "DTRSPRequestValidator.requestAccepted: accepted request %s", request);
        getMSAProxy().getInstance().requestReleased(request);
        // Nothing to do
    }

    @Override
    public void requestRejected(IActualRequest request) {
        super.requestRejected(request);
        TRSPLogging.getSimulationLogger().info(
                "DTRSPRequestValidator.requestRejected: rejected request %s", request);
        getMSAProxy().getInstance().requestReleased(request);
        ((TRSPInstance) getMSAProxy().getInstance()).getSimulator().markAsRejected(request.getID());
    }

}
