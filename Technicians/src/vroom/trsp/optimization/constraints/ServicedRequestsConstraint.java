/**
 * 
 */
package vroom.trsp.optimization.constraints;

import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;
import vroom.trsp.sim.TRSPSimulator;

/**
 * <code>ServicedRequestsConstraint</code> is used to model the fact that already serviced requests cannot be moved
 * within a solution.
 * <p>
 * Creation date: Nov 10, 2011 - 9:32:18 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ServicedRequestsConstraint extends TourConstraintBase {

    /**
     * Creates a new <code>ServicedRequestsConstraint</code>
     * 
     * @param simulator
     */
    public ServicedRequestsConstraint() {
    }

    @Override
    public int firstInfeasibleNode(ITRSPTour tour) {
        return checkFeasibility(tour).getInfeasibleNode();
    }

    @Override
    protected FeasibilityState checkFeasibility(ITRSPTour tour) {
        TRSPSimulator sim = tour.getInstance().getSimulator();
        if (sim != null) {
            TRSPTour refTour = sim.getCurrentSolution().getTour(tour.getTechnicianId());
            if (tour.length() < refTour.length())
                return new FeasibilityState(tour.getLastNode(),
                        "Tour is shorter than reference tour (tour:%s ref:%s)", tour, refTour);

            ITourIterator it = tour.iterator();
            TRSPTourIterator refIt = refTour.iterator();
            while (refIt.hasNext()) {
                int ref = refIt.next();
                int n = it.next();
                if (ref != n) {
                    return new FeasibilityState(n, "Found %s when expecting %s (tour:%s ref:%s)",
                            n, ref, tour, refTour);
                }
            }

        }
        return new FeasibilityState();
    }

    @Override
    protected boolean isTwoOptFeasible(ITRSPTour tour, TRSPTwoOptMove move) {
        TRSPSimulator sim = tour.getSolution().getInstance().getSimulator();
        return sim == null || !sim.isServedOrAssigned(move.getFirst())
                && !sim.isServedOrAssigned(move.getSecond());
    }

    @Override
    protected boolean isShiftFeasible(ITRSPTour tour, TRSPShiftMove move) {
        TRSPSimulator sim = tour.getSolution().getInstance().getSimulator();
        return sim == null || !sim.isServedOrAssigned(move.getNode())
                && !sim.isServedOrAssigned(move.getNewSucc());
    }

    @Override
    protected boolean isInsFeasible(ITRSPTour tour, InsertionMove move) {
        TRSPSimulator sim = tour.getSolution().getInstance().getSimulator();
        return sim == null
                || sim.isStaticSetting()
                || (!sim.isServedOrAssigned(move.getInsertionSucc())
                        && !sim.isServedOrAssigned(move.getNodeId()) && (!move.isDepotTrip() || !sim
                        .isServedOrAssigned(move.getDepotSucc())));
    }

    @Override
    public int checkFeasibility(ITRSPTour tour, IMove move) {
        return isFeasible(tour, move) ? 3 : 2;
    }

}
