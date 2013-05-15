package vroom.trsp.optimization.constraints;

import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>HomeConstraint</code> is a constraint that ensure that a {@link TRSPTour} always start and or end where
 * expected, i.e. at the technician home.
 * <p>
 * Creation date: May 4, 2011 - 11:39:30 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class HomeConstraint extends TourConstraintBase {

    @Override
    public boolean isFeasible(ITRSPTour tour) {
        int first = tour.getFirstNode();
        int last = tour.getLastNode();
        int home = tour.getInstance().getFleet().getVehicle(tour.getTechnicianId()).getHome()
                .getID();
        int home2 = tour.getInstance().getHomeDuplicate(home);

        return (first == home || first == home2) && (last == home2 || last == home);
    }

    @Override
    protected FeasibilityState checkFeasibility(ITRSPTour tour) {
        int first = tour.getFirstNode();
        int last = tour.getLastNode();
        int home = tour.getInstance().getFleet().getVehicle(tour.getTechnicianId()).getHome()
                .getID();
        int home2 = tour.getInstance().getHomeDuplicate(home);

        String s = null;

        int node = ITRSPTour.UNDEFINED;

        if (first != home && first != home2) {
            s = "Tour does not start at technician home";
            node = first;
        }

        if (last == home2 && last == home) {
            if (s != null)
                s += " and ";
            else
                s = "";
            s += "Tour does not end at technician home";
            node = node == ITRSPTour.UNDEFINED ? last : node;
        }

        return new FeasibilityState(node, s);
    }

    @Override
    public int checkFeasibility(ITRSPTour tour, IMove move) {
        boolean isFeasible = isFeasible(tour, move);
        return isFeasible ? 3 : 2;
    }

    @Override
    protected boolean isTwoOptFeasible(ITRSPTour itour, TRSPTwoOptMove move) {
        if (!TRSPTour.class.isAssignableFrom(itour.getClass())) {
            TRSPLogging.getOptimizationLogger().warn(
                    String.format("HomeConstraint.isTwoOptFeasible: unsupported tour class (%s)",
                            itour.getClass()));
            return true;
        }
        TRSPTour tour = (TRSPTour) itour;
        return move.getFirst() != tour.getFirstNode()
                && tour.getSucc(move.getSecond()) != tour.getLastNode();
    }

    @Override
    protected boolean isShiftFeasible(ITRSPTour tour, TRSPShiftMove move) {
        return move.getNewSucc() != ITRSPTour.UNDEFINED && move.getNewSucc() != tour.getFirstNode();
    }

    @Override
    protected boolean isInsFeasible(ITRSPTour tour, InsertionMove move) {
        return move.getInsertionSucc() != ITRSPTour.UNDEFINED
                && move.getInsertionSucc() != tour.getFirstNode();
    }

}
