/**
 *
 */
package vroom.trsp.datamodel;

import java.util.Iterator;

/**
 * <code>CVRPTWSolutionChecker</code> is a solution checker for {@link TRSPSolution solutions} of a {@link TRSPInstance}
 * representing a sCVRPTW instance.
 * <p>
 * Creation date: Jun 15, 2011 - 9:05:03 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CVRPTWSolutionChecker extends TRSPSolutionCheckerBase {

    public static final CVRPTWSolutionChecker INSTANCE = new CVRPTWSolutionChecker();

    @Override
    public String checkTour(ITRSPTour tour) {
        if (tour.length() == 0)
            return "";

        StringBuilder err = new StringBuilder();

        TRSPInstance instance = tour.getInstance();
        Technician tech = instance.getFleet().getVehicle(tour.getTechnicianId());

        int cap = tech.getAvailableSpareParts(0);
        boolean checkCap = true;

        // Tour must start and end at the depot
        if (!tour.getInstance().isDepot(tour.getFirstNode())
                || !tour.getInstance().isDepot(tour.getLastNode()))
            append(err, "Tour must start and end at the depot");

        if (tour.length() < 2)
            return err.toString();

        double time = tech.getHome().getTimeWindow().startAsDouble();
        Iterator<Integer> it = tour.iterator();
        // Iterate over the tour
        int pred = it.next();
        while (it.hasNext()) {
            int node = it.next();

            time = instance.getTimeWindow(pred).getEarliestStartOfService(time);
            time += instance.getServiceTime(pred);
            time += instance.getCostDelegate().getTravelTime(pred, node, tech);

            // Time window
            if (!instance.getTimeWindow(node).isFeasible(time)) {
                append(err, "TW violated at node %s (%.1f-%s)", node, time,
                        instance.getTimeWindow(node));
            }

            // Load
            cap -= tour.getInstance().getSparePartReq(node, 0);
            if (checkCap && cap < 0) {
                append(err, "Cap violated at node %s (rem: %s)", node, cap);
                checkCap = false;
            }

            pred = node;
        }

        return err.toString();
    }

}
