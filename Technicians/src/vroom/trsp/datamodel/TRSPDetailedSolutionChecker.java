package vroom.trsp.datamodel;

import java.util.Arrays;

import vroom.common.utilities.Constants;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;

/**
 * The Class <code>TRSPDetailedSolutionChecker</code> is a utility class that checks the coherence of a
 * {@link TRSPSolution} and {@link TRSPTour}, including the stored information
 * <p>
 * Creation date: Mar 16, 2011 - 4:49:29 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPDetailedSolutionChecker extends TRSPSolutionCheckerBase {

    public static final TRSPDetailedSolutionChecker INSTANCE = new TRSPDetailedSolutionChecker();

    /**
     * Creates a new <code>TRSPDetailedSolutionChecker</code>
     */
    public TRSPDetailedSolutionChecker() {
        super(true);
    }

    /**
     * Creates a new <code>TRSPDetailedSolutionChecker</code>
     * 
     * @param checkUnserved
     *            <code>true</code> if unserved reauests should be checked, <code>false</code> otherwise
     */
    public TRSPDetailedSolutionChecker(boolean checkUnserved) {
        super(checkUnserved);
    }

    /**
     * Check the coherence of a tour
     * 
     * @param mytour
     *            the tour to be checked
     * @param startEndDepot
     *            <code>true</code> if all tours must start and end at a depot
     * @return a string describing the <code>tour</code> incoherences or <code>null</code> if none was found
     */
    @Override
    public String checkTour(ITRSPTour tour) {
        if (!TRSPTour.class.isAssignableFrom(tour.getClass())) {
            return TRSPSolutionChecker.INSTANCE.checkTour(tour);
        }

        TRSPTour mytour = (TRSPTour) tour;

        StringBuilder err = new StringBuilder();

        TRSPInstance instance = mytour.getInstance();
        Technician tech = instance.getFleet().getVehicle(mytour.getTechnicianId());

        // ---------------------------------------------------------------
        // Check if the tour starts and end at the technician home
        int home = tech.getHome().getID();
        int homeEnd = instance.getHomeDuplicate(home);
        if (mytour.getFirstNode() != home || mytour.getLastNode() != homeEnd)
            err.append("Tour does not start/end at the technician home");
        // ---------------------------------------------------------------

        int length = 0;
        boolean[] tools = mytour.getTechnicianToolSet();
        int[] spare = mytour.getTechnicianSpareParts();
        double earliest = Long.MIN_VALUE;
        double[][] wait = new double[mytour.getInstance().getMaxId()][mytour.getInstance()
                .getMaxId()];

        double val = mytour.getCostDelegate().evaluateTour(mytour, false);
        if (Math.abs(val - mytour.getTotalCost()) > 1e-3) {
            err.append(String.format("Bad total cost (expected %.3f is %.3f)", val,
                    mytour.getTotalCost()));
        }

        TRSPTourIterator fwdit = mytour.iterator();

        boolean checkSpare = true, checkTools = true, checkTime = true, checkWait = true, checkLateness = true, checkLatestFeas = true, checkSpareReq = true;

        int pred = -1;
        while (fwdit.hasNext()) {
            int node = fwdit.next();

            if (pred == -1)
                earliest = 0;
            else {
                earliest = mytour.getTimeWindow(pred).getEarliestStartOfService(earliest);
                earliest += mytour.getServiceTime(pred);
                earliest += mytour.getTravelTime(pred, node);
            }

            if (checkTime && mytour.getEarliestArrivalTime(node) != earliest) {
                err.append(String.format("Bad earliest time at node %s (expected %.3f is %.3f) ",
                        node, earliest, mytour.getEarliestArrivalTime(node)));
                checkTime = false;
            }

            if (checkTime && !mytour.getTimeWindow(node).isFeasible(earliest)) {
                err.append(String.format("Time window violated at node %s (@.3f@%s) ", node,
                        earliest, mytour.getTimeWindow(node)));
                checkTime = false;
            }

            wait[node][node] = Math.max(0, mytour.getTimeWindow(node).startAsDouble() - earliest);
            if (checkWait && mytour.getWaitingTime(node) != wait[node][node]) {
                err.append(String.format("Bad waiting time at node %s (expected %.3f is %.3f) ",
                        node, wait[node][node], mytour.getWaitingTime(node)));
                checkTime = false;
            }

            // double lateness = mytour.getTimeWindow(node).getViolation(earliest);
            // if (checkLateness && mytour.getLateness(node) != lateness) {
            // err.append(String.format("Bad lateness at node %s (expected %.3f is %.3f) ", node, lateness,
            // mytour.getLateness(node)));
            // checkLateness = false;
            // }

            length++;
            if (instance.isMainDepot(node)) {
                Arrays.fill(tools, true);
                Arrays.fill(spare, Integer.MAX_VALUE);
            } else if (!instance.isDepot(node)) {
                for (int i = 0; i < spare.length; i++) {
                    if (spare[i] != Integer.MAX_VALUE)
                        spare[i] -= instance.getRequest(node).getSparePartRequirement(i);
                }
            }

            for (int i = 0; i < spare.length && checkSpare; i++) {
                if (spare[i] != mytour.getAvailableSpareParts(node, i)) {
                    err.append(String.format(
                            "Bad number of spare parts of type %s at node %s (expected %s is %s) ",
                            i, node, spare[i], mytour.getAvailableSpareParts(node, i)));
                    checkSpare = false;
                }
            }

            for (int i = 0; i < tools.length && checkTools; i++) {
                if (tools[i] != mytour.isToolAvailable(node, i)) {
                    err.append(String.format(
                            "Bad availability for tool %s at node %s (expected %s is %s) ", i,
                            node, tools[i], mytour.isToolAvailable(node, i)));
                    checkTools = false;
                }
            }

            pred = node;
            // System.out.printf("%3s %6.1f@%s\t%s\n",node,earliest,mytour.getTimeWindow(node),spare[0]);
        }

        fwdit = mytour.iterator();
        boolean checkFwdSlack = true;
        while (fwdit.hasNext()) {
            int node = fwdit.next();
            TRSPTourIterator sucIt = mytour.iterator(node);
            double cumWait = 0;

            if (!sucIt.hasNext())
                continue;

            sucIt.next();
            while (sucIt.hasNext()) {
                int succ = sucIt.next();
                wait[node][succ] = cumWait;
                cumWait += wait[succ][succ];

                if (!instance.isCVRPTW() && checkWait
                        && !Constants.equals(mytour.getWaitingTime(node, succ), wait[node][succ])) {
                    err.append(String
                            .format("Bad cumulated slack time between node %s and %s (expected %.3f is %.3f) ",
                                    node, succ, wait[node][succ], mytour.getWaitingTime(node, succ)));
                    checkTime = false;
                }

                double slack = evaluateFwdSlackTime(mytour, node, succ);
                if (!instance.isCVRPTW() && checkFwdSlack
                        && !Constants.equals(mytour.getFwdSlackTime(node, succ), slack)) {
                    err.append(String
                            .format("Bad forward slack time between node %s and %s (expected %.3f is %.3f) ",
                                    node, succ, slack, mytour.getFwdSlackTime(node, succ)));
                    checkFwdSlack = false;
                }
            }
        }

        TRSPTourIterator bkwdit = mytour.iterator(mytour.getLastNode());
        double lft = Double.POSITIVE_INFINITY;
        int[] spareReq = new int[instance.getSpareCount()];
        int succ = ITRSPTour.UNDEFINED;
        while (bkwdit.hasPrevious() && (checkLatestFeas || checkSpareReq)) {
            int node = bkwdit.previous();
            if (succ == ITRSPTour.UNDEFINED) {
                lft = mytour.getTimeWindow(node).endAsDouble();
                if (instance.isRequest(node))
                    spareReq = instance.getRequest(node).getSparePartRequirements();
            } else {
                lft = Math.min(mytour.getTimeWindow(node).endAsDouble(),
                        lft - mytour.getServiceTime(node) - mytour.getTravelTime(node, succ));
                if (instance.isRequest(node))
                    for (int i = 0; i < spareReq.length; i++) {
                        spareReq[i] += instance.getRequest(node).getSparePartRequirement(i);
                    }
                // (Do not) Reset the requirement (see TRSPTour#getRequiredSpareParts(int,int)
                // else if (instance.isMainDepot(node))
                // Arrays.fill(spareReq, 0);
            }
            succ = node;

            // Check latest feasible arrival time
            if (lft != mytour.getLatestFeasibleArrivalTime(node)) {
                checkLatestFeas = false;
                err.append(String.format("Bad latest arrival time at node %s (expected %s is %s)",
                        node, lft, mytour.getLatestFeasibleArrivalTime(node)));
            }
            // FIXME Check required spare parts
            for (int i = 0; i < spareReq.length; i++) {
                if (spareReq[i] != mytour.getRequiredSpareParts(node, i)) {
                    err.append(String.format(
                            "Bad spare part requirement of type %s at node %s (expected %s is %s)",
                            i, node, spareReq[i], mytour.getRequiredSpareParts(node, i)));
                    checkSpareReq = false;
                }
            }
        }

        if (tour.getInstance().getSimulator() != null) {
            append(err, checkServedAssignedRequests(tour));
        }

        return err.toString();
    }

}
