/**
 * 
 */
package vroom.trsp.optimization.split;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPSimpleTour;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPSplit</code> is an implementation of the split procedure that optimally splits a giant tour into a set of
 * feasible tours.
 * <p>
 * Creation date: Sep 26, 2011 - 4:09:20 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPSplit {

    private final SplitTourArcBuilder mArcBuilder;

    /**
     * Creates a new <code>TRSPSplit</code>
     * 
     * @param constraints
     * @param costDelegate
     */
    public TRSPSplit(SplitTourArcBuilder arcBuilder) {
        super();
        mArcBuilder = arcBuilder;
    }

    /**
     * Optimally split a giant tour in a set of feasible tours
     * 
     * @param giantTour
     *            the giant tour to be split, should start at the technician home and be open-ended
     * @return a collection of the tours resulting from the split of <code>giantTour</code>
     */
    public Collection<ITRSPTour> splitTour(ITRSPTour giantTour) {
        if (giantTour.length() == 0)
            return Collections.<ITRSPTour> emptySet();

        // Array of labels (costs)
        double[] labels = new double[giantTour.length()];
        Arrays.fill(labels, Double.POSITIVE_INFINITY);

        // Array of predecessors
        SplitTourArc[] arcs = new SplitTourArc[giantTour.length()];

        // Main loop iterator
        int i = 0;

        labels[i] = 0;

        // --------------------------------------
        // Shortest path
        // --------------------------------------
        // Loop for the arc tail
        // TRSPLogging.getOptimizationLogger().lowDebug(
        // "TRSPSplit.splitTour: Splitting giant tour %s", giantTour);

        while (i < giantTour.length()) {
            if (labels[i] == Double.POSITIVE_INFINITY) {
                TRSPLogging
                        .getOptimizationLogger()
                        .warn("TRSPSplit.splitTour: request %s seems to be incompatibel with technician %s, check preprocessing (aborting)",
                                giantTour.getNodeAt(i), giantTour.getTechnicianId());
                return Collections.<ITRSPTour> emptySet();
            }

            boolean feasible = true;
            int j = i + 1;
            SplitTourArc arc = null;
            // Loop for the arc head
            // Abort when an arc is infeasible, as longer arcs will also be infeasible
            while (j < giantTour.length() && feasible) {
                // Generate the arc
                arc = mArcBuilder.buildArc(giantTour, i + 1, j);

                if (arc == null) {
                    // TRSPLogging.getOptimizationLogger().lowDebug(
                    // "TRSPSplit.splitTour: ([%s]%s,[%s]%s) is infeasible", i,
                    // giantTour.getNodeAt(i), j, giantTour.getNodeAt(j));
                    feasible = false;
                    break;
                }

                // Evaluate cost
                double arcCost = mArcBuilder.evaluateArc();

                double candLabel = labels[i] + arcCost;
                double oldLabel = labels[j];

                if (candLabel < oldLabel) {
                    // Promising new path
                    // Check feasibility
                    feasible = mArcBuilder.isFeasible();

                    if (feasible) {
                        // The new path is feasible and improves the label
                        labels[j] = candLabel;
                        arcs[j] = arc;
                    }
                }
                // TRSPLogging
                // .getOptimizationLogger()
                // .lowDebug(
                // "TRSPSplit.splitTour: ([%s]%s,[%s]%s) cost:%.3f feas:%s imp:%s oldLabel:%.3f candLabel:%.3f",
                // i, giantTour.getNodeAt(i), j, giantTour.getNodeAt(j),
                // arc.getTotalCost(), feasible, candLabel < oldLabel, oldLabel,
                // candLabel);
                j++;
            }
            i++;
        }

        // --------------------------------------
        // Extract the arcs from the shortest path
        // --------------------------------------
        LinkedList<ITRSPTour> tours = new LinkedList<ITRSPTour>();

        int idx = giantTour.length() - 1;
        while (idx != 0) {
            if (arcs[idx] != null) {
                // Add the current arc
                tours.add(arcs[idx]);
                // Walk back to the tail
                idx = arcs[idx].getStart() - 1;
            } else {
                idx--;
            }
        }

        // boolean check = checkSplit(giantTour, tours);

        TRSPLogging.getOptimizationLogger().lowDebug("TRSPSplit.splitTour: Giant tour split in %s tours", tours.size());
        return tours;
    }

    public static boolean checkSplit(TRSPSimpleTour giantTour, List<ITRSPTour> splittedTours) {
        int idx = 1;
        // Start from the end: assumes that the first tour was added last
        ListIterator<ITRSPTour> it = splittedTours.listIterator(splittedTours.size());
        while (idx < giantTour.length()) {
            ITRSPTour splitTour = it.previous();
            for (int node : splitTour) {
                if (giantTour.getInstance().isRequest(node)) {
                    if (node != giantTour.getNodeAt(idx))
                        return false;
                    idx++;
                }
            }
        }

        return true;
    }
}
