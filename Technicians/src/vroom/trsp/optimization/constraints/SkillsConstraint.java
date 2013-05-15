/**
 * 
 */
package vroom.trsp.optimization.constraints;

import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;

/**
 * <code>SkillsConstraint</code> is an implementation of {@link IConstraint} that checks the feasibility of a move
 * regarding skill constraints
 * <p>
 * Creation date: Mar 28, 2011 - 10:27:55 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SkillsConstraint extends TourConstraintBase {

    @Override
    public int checkFeasibility(ITRSPTour tour, IMove move) {
        // We assume that the tour data is updated and coherent
        if (move instanceof InsertionMove) {
            boolean feasible = isFeasible(tour, move);
            return feasible ? 3 : 0;
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported move: %s", move));
        }
    }

    @Override
    protected FeasibilityState checkFeasibility(ITRSPTour tour) {
        for (int node : tour) {
            if (tour.getInstance().isRequest(node)
                    && !tour.getInstance().hasRequiredSkills(tour.getTechnicianId(), node))
                return new FeasibilityState(node,
                        "Missing skills to serve request %s (req:%s has:%s)", node, tour.getInstance().getRequest(node).getSkillSet(), tour
                                .getInstance().getFleet().getVehicle(tour.getTechnicianId())
                                .getSkillSet());
        }

        return new FeasibilityState();
    }

    @Override
    protected boolean isTwoOptFeasible(ITRSPTour tour, TRSPTwoOptMove move) {
        // Intra-tour 2-opt: feasibility is maintained
        return true;
    }

    @Override
    protected boolean isShiftFeasible(ITRSPTour tour, TRSPShiftMove move) {
        // The shifted request is already in the tour and the technician therefore has the required skills
        return true;
    }

    @Override
    protected boolean isInsFeasible(ITRSPTour tour, InsertionMove move) {
        return tour.getInstance().hasRequiredSkills(tour.getTechnicianId(), move.getNodeId());
    }
}
