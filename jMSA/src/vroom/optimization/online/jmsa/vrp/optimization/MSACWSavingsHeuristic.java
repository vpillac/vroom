/**
 * 
 */
package vroom.optimization.online.jmsa.vrp.optimization;

import vroom.common.heuristics.cw.IJCWArc;
import vroom.common.heuristics.cw.algorithms.BasicSavingsHeuristic;
import vroom.common.heuristics.cw.algorithms.RandomizedSavingsHeuristic;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPShrunkRequest;

/**
 * <code>MSACWRndAlgorithm</code> is a specialization of {@link RandomizedSavingsHeuristic} that forces any shrunk
 * node to be directly before or after the depot.
 * <p>
 * This behavior is achieved by prohibiting merging arcs that contain the shrunk node, except if it is in a singleton
 * route
 * </p>
 * <p>
 * Creation date: May 4, 2010 - 5:57:31 PM
 * </p>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MSACWSavingsHeuristic extends BasicSavingsHeuristic<VRPScenario> {

    /**
     * <code>true</code> if the shrunk node is in a singleton route, which can therefore be merge with an arc containing
     * the shrunk node.
     */
    private boolean mShrunkNodeSingletonRoute = true;

    /**
     * Creates a new <code>MSACWRndAlgorithm</code>
     * 
     * @param parentHeuristic
     */
    public MSACWSavingsHeuristic(ClarkeAndWrightHeuristic<VRPScenario> parentHeuristic) {
        super(parentHeuristic);
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.common.heuristics.jcw.algorithms.SavingsAlgorithmBase#initialize
     * (vroom.common.modeling.dataModel.ISolution)
     */
    @Override
    public void initialize(VRPScenario solution) {
        mShrunkNodeSingletonRoute = true;
        super.initialize(solution);
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.common.heuristics.jcw.algorithms.BasicSavingsHeuristic#runHeuristic
     * ()
     */
    @Override
    public void runHeuristic() {
        super.runHeuristic();
    }

    /*
     * (non-Javadoc)
     * @seeedu.uniandes.copa.heuristics.jcw.algorithms.SavingsAlgorithmBase#
     * checkFeasibility(vroom.common.heuristics.jcw.IJCWArc,
     * vroom.common.modeling.dataModel.IRoute,
     * vroom.common.modeling.dataModel.IRoute)
     */
    @Override
    protected boolean checkFeasibility(IJCWArc currentArc, IRoute<INodeVisit> tailRoute,
            IRoute<INodeVisit> headRoute) {
        return (mShrunkNodeSingletonRoute || !(currentArc.getTailNode() instanceof VRPShrunkRequest)
                && !(currentArc.getHeadNode() instanceof VRPShrunkRequest))
                && super.checkFeasibility(currentArc, tailRoute, headRoute);
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.common.heuristics.jcw.algorithms.SavingsAlgorithmBase#mergeRoutes
     * (vroom.common.heuristics.jcw.IJCWArc,
     * vroom.common.modeling.dataModel.IRoute,
     * vroom.common.modeling.dataModel.IRoute)
     */
    @Override
    protected void mergeRoutes(IJCWArc linkingArc, IRoute<INodeVisit> tailRoute,
            IRoute<INodeVisit> headRoute) {
        if (linkingArc.getTailNode() instanceof VRPShrunkRequest
                || linkingArc.getHeadNode() instanceof VRPShrunkRequest) {
            if (!mShrunkNodeSingletonRoute) {
                throw new IllegalStateException(String.format(
                        "Attempting to merge the route containing the shrunk node : %s -%s- %s",
                        tailRoute, linkingArc, headRoute));
            }

            mShrunkNodeSingletonRoute = false;
        }

        super.mergeRoutes(linkingArc, tailRoute, headRoute);
    }
}
