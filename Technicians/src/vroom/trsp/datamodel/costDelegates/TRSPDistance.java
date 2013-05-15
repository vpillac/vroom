package vroom.trsp.datamodel.costDelegates;

import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.TRSPDistanceMatrix;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.RemoveMove;
import vroom.trsp.optimization.localSearch.TRSPShift.TRSPShiftMove;
import vroom.trsp.optimization.localSearch.TRSPTwoOpt.TRSPTwoOptMove;

/**
 * <code>TRSPDistance</code> is an implementation of {@link TRSPCostDelegate} based on the traveled distance. It ignores
 * service times.
 * <p>
 * Creation date: Jun 6, 2011 - 4:46:03 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPDistance extends TRSPCostDelegate {

    // FIXME implement specific update methods to improve performance

    @Override
    protected double evaluateTRSPTour(TRSPTour tour, int node, boolean updateTour) {
        if (tour.length() < 2) {
            if (updateTour) {
                for (int i : tour) {
                    tour.setCumulativeCost(i, 0);
                }
                tour.setTotalCost(0);
            }
            return 0;
        }

        // Cumulated distance
        double d = node != ITRSPTour.UNDEFINED ? tour.getCumulativeCost(node) : 0;
        // Iterator over the tour
        TRSPTourIterator it = node != ITRSPTour.UNDEFINED ? tour.iterator(node) : tour.iterator();

        if (!it.hasNext()) {
            if (updateTour)
                tour.setTotalCost(d);
            return d;
        }

        int pred = it.next();
        while (it.hasNext()) {
            if (updateTour)
                tour.setCumulativeCost(pred, d);
            int succ = it.next();
            d += tour.getInstance().getCostDelegate().getDistance(pred, succ);
            pred = succ;
        }
        if (updateTour)
            tour.setCumulativeCost(pred, d);

        if (updateTour)
            tour.setTotalCost(d);

        return d;
    }

    @Override
    protected double evaluateGenericTour(ITRSPTour tour) {
        if (tour.length() == 0)
            return 0;

        // Cumulated distance
        double dist = 0;
        // Iterator over the tour
        ITourIterator it = tour.iterator();

        int pred = it.next();
        while (it.hasNext()) {
            int succ = it.next();
            dist += tour.getInstance().getCostDelegate().getDistance(pred, succ);
            pred = succ;
        }
        return dist;
    }

    @Override
    public double evaluateDetour(ITRSPTour tour, int i, int n, int j,
            boolean isRemoval) {
        TRSPDistanceMatrix c = tour.getInstance().getCostDelegate();
        return c.getDistance(i, n) + c.getDistance(n, j)
                - c.getDistance(i, j);
    }

    @Override
    protected double evaluateRemMove(RemoveMove move) {
        TRSPTour tour = (TRSPTour) move.getTour();
        int node = move.getNodeId();
        int pred = tour.getPred(node);
        int succ = tour.getSucc(node);

        TRSPDistanceMatrix c = tour.getInstance().getCostDelegate();
        int imp = 0;
        // Node removal
        if (pred != ITRSPTour.UNDEFINED)
            imp += c.getDistance(pred, node);
        if (succ != ITRSPTour.UNDEFINED)
            imp += c.getDistance(node, succ);
        if (pred != ITRSPTour.UNDEFINED && succ != ITRSPTour.UNDEFINED)
            imp -= c.getDistance(pred, succ);

        return imp;
    }

    @Override
    protected double evaluateTwoOptMove(TRSPTwoOptMove move) {
        TRSPTour tour = (TRSPTour) move.getTour();
        TRSPDistanceMatrix c = tour.getInstance().getCostDelegate();

        int i = move.getFirst();
        int j = tour.getSucc(i);
        int m = move.getSecond();
        int n = tour.getSucc(m);

        // We assume that the instance is symmetric
        return c.getDistance(i, j) + c.getDistance(m, n) - c.getDistance(i, m)
                - c.getDistance(j, n);
    }

    @Override
    protected double evaluateShiftMove(TRSPShiftMove move) {
        TRSPTour tour = (TRSPTour) move.getTour();
        if (move.getNewSucc() == tour.getSucc(move.getNode()))
            return 0;

        TRSPDistanceMatrix c = tour.getInstance().getCostDelegate();

        int node = move.getNode();

        int insSucc = move.getNewSucc();
        int insPred = insSucc != ITRSPTour.UNDEFINED ? tour.getPred(insSucc) : move.getTour()
                .getLastNode();

        int nodePred = tour.getPred(node);
        int nodeSucc = tour.getSucc(node);

        double imp = 0;

        // Node removal
        if (nodePred != ITRSPTour.UNDEFINED)
            imp += c.getDistance(nodePred, node);
        if (nodeSucc != ITRSPTour.UNDEFINED)
            imp += c.getDistance(node, nodeSucc);
        if (nodePred != ITRSPTour.UNDEFINED && nodeSucc != ITRSPTour.UNDEFINED)
            imp -= c.getDistance(nodePred, nodeSucc);

        // Node insertion
        if (insSucc != ITRSPTour.UNDEFINED && insPred != ITRSPTour.UNDEFINED)
            imp += c.getDistance(insPred, insSucc);
        if (insPred != ITRSPTour.UNDEFINED)
            imp -= c.getDistance(insPred, node);
        if (insSucc != ITRSPTour.UNDEFINED)
            imp -= c.getDistance(node, insSucc);

        return imp;
    }

    @Override
    protected double evaluateInsMove(InsertionMove move) {
        TRSPTour tour = (TRSPTour) move.getTour();

        if (move.getTour().length() == 0)
            return 0;

        int succ = move.getInsertionSucc();
        int node = move.getNodeId();
        int pred;
        TRSPDistanceMatrix c = move.getTour().getInstance().getCostDelegate();

        double imp = 0;
        if (succ == ITRSPTour.UNDEFINED)
            pred = move.getTour().getLastNode();
        else
            pred = tour.getPred(succ);

        if (pred != ITRSPTour.UNDEFINED)
            imp -= c.getDistance(pred, node);
        if (succ != ITRSPTour.UNDEFINED)
            imp -= c.getDistance(node, succ);
        if (pred != ITRSPTour.UNDEFINED && succ != ITRSPTour.UNDEFINED)
            imp += c.getDistance(pred, succ);

        if (move.isDepotTrip()) {
            int depSucc = move.getDepotSucc();
            int depPred = depSucc != node ? tour.getPred(depSucc) : pred;

            if (depPred != ITRSPTour.UNDEFINED)
                imp -= c.getDistance(depPred, tour.getMainDepotId());
            imp -= c.getDistance(tour.getMainDepotId(), depSucc);
            imp += c.getDistance(depPred, depSucc);
        }

        return imp;
    }

    @Override
    public boolean isInsertionSeqDependent() {
        return false;
    }

}
