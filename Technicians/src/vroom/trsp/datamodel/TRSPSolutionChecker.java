/**
 *
 */
package vroom.trsp.datamodel;

import java.util.Iterator;

import vroom.common.modeling.dataModel.Depot;

/**
 * <code>TRSPSolutionChecker</code> is an implementation of {@link TRSPSolutionCheckerBase} that checks the feasibility
 * of a solution regarding the base definition of the TRSP.
 * <p>
 * Creation date: Jun 15, 2011 - 10:22:15 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPSolutionChecker extends TRSPSolutionCheckerBase {

    public static final TRSPSolutionChecker INSTANCE = new TRSPSolutionChecker();

    /**
     * Creates a new <code>TRSPSolutionChecker</code>
     */
    public TRSPSolutionChecker() {
        super();
    }

    /**
     * Creates a new <code>TRSPSolutionChecker</code>
     * 
     * @param checkUnserved
     */
    public TRSPSolutionChecker(boolean checkUnserved) {
        super(checkUnserved);
    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.datamodel.TRSPSolutionCheckerBase#checkTour(vroom.trsp.datamodel.TRSPTour)
     */
    @Override
    public String checkTour(ITRSPTour tour) {
        if (tour.length() == 0)
            return "";

        TRSPInstance instance = tour.getInstance();
        Technician tech = instance.getFleet().getVehicle(tour.getTechnicianId());

        StringBuilder err = new StringBuilder();
        // ---------------------------------------------------------------
        // Check if the tour starts and end at the technician home
        Depot home = tech.getHome();
        if (!instance.isDynamic()
                && (tour.getFirstNode() != home.getID() || tour.getLastNode() != instance
                        .getHomeDuplicate(home.getID())))
            err.append("Tour does not start/end at the technician home");
        // ---------------------------------------------------------------

        if (tour.length() < 2)
            return err.toString();

        int[] spare = tech.getSpareParts();

        Iterator<Integer> it = tour.iterator();
        // Iterate over the tour
        double time = tech.getHome().getTimeWindow().startAsDouble();
        int pred = it.next();
        boolean checkSPT = true;
        while (it.hasNext()) {
            int node = it.next();

            // Update current time
            time = instance.getTimeWindow(pred).getEarliestStartOfService(time);
            time += instance.getServiceTime(pred);
            time += instance.getCostDelegate().getTravelTime(pred, node, tech);

            // Time windows
            if (!instance.getTimeWindow(node).isFeasible(time)) {
                append(err, "TW violated at node %s (%.4f@%s)", node, time,
                        instance.getTimeWindow(node));
            }

            // Visit to main depot
            if (instance.isMainDepot(node)) {
                if (!instance.isMainDepotTripAllowed())
                    append(err, "Trips to main depot are not allowed");
                else
                    checkSPT = false; // We assume that SP and T constraints will be satisfied
            }

            // The node is a request, check skills, tools, and spare parts
            if (instance.isRequest(node)) {
                final TRSPRequest req = instance.getRequest(node);

                // Skills
                for (int s : req.getSkillSet()) {
                    if (!tech.getSkillSet().hasAttribute(s))
                        append(err, "SkillCtr violated at node %s (skill:%s)", node, s);
                }

                if (checkSPT) {
                    // Tools
                    for (int t : req.getToolSet()) {
                        if (!tech.getToolSet().hasAttribute(t))
                            append(err, "ToolCtr violated at node %s (tool:%s)", node, t);
                    }

                    // Spare parts
                    for (int s = 0; s < spare.length; s++) {
                        spare[s] -= req.getSparePartRequirement(s);
                        if (spare[s] < 0)
                            append(err, "SpareCtr violated at node %s (part:%s rem:%s)", node, s,
                                    spare[s]);
                    }
                }
            }

            pred = node;
        }

        return err.toString();
    }

}
