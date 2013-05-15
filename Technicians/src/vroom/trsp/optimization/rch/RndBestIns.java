/**
 * 
 */
package vroom.trsp.optimization.rch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.DoublyLinkedIntList;
import vroom.common.utilities.DoublyLinkedIntSet;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSimpleTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>RndNearestFurthestIns</code> is an implementation of the random best insertion.
 * <p>
 * Creation date: Oct 4, 2011 - 1:44:48 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RndBestIns extends TRSPRndConstructiveHeuristic {

    private final NeighborComparator  mComparator;

    private final Neighbor[]          mNearestTourNode;

    private final DoublyLinkedIntSet  mUnserved;

    // private final LimitedSortedList<Neighbor> mBestNeighbors;
    private final ArrayList<Neighbor> mBestNeighbors;

    /**
     * Creates a new <code>TRSPRndNearestFarthestInsOld</code>
     * 
     * @param instance
     * @param parameters
     * @param constraintHandler
     * @param costDelegate
     * @param kMax
     *            the K<sub>max</sub> value
     * @param rfi
     *            <code>true</code> if the farthest insertion should be selected (RFI), <code>false</code> if the
     *            nearest insertion should be selected (RNI)
     */
    public RndBestIns(TRSPInstance instance, TRSPGlobalParameters parameters, TourConstraintHandler constraintHandler,
            TRSPCostDelegate costDelegate, int kMax) {
        super(instance, parameters, constraintHandler, costDelegate, kMax);
        mNearestTourNode = new Neighbor[getInstance().getMaxId()];
        mUnserved = new DoublyLinkedIntSet(getInstance().getMaxId());
        mComparator = new NeighborComparator();
        // mBestNeighbors = new LimitedSortedList<Neighbor>(getKmax(), mComparator);
        mBestNeighbors = new ArrayList<Neighbor>(mUnserved.size());
    }

    @Override
    protected Collection<ITRSPTour> generateGiantTour(Technician technician) {
        Arrays.fill(mNearestTourNode, null);
        mUnserved.clear();

        // Initialize the nearest tour node array
        for (ObservableNode node : getCompatibleRequests(technician.getID())) {
            double dist = getInstance().getCostDelegate()
                    .getDistance(getHome(technician.getID()).getId(), node.getId());
            Neighbor neighbor = new Neighbor(node, getHome(technician.getID()), 2 * dist);

            mNearestTourNode[node.getId()] = neighbor;
            mBestNeighbors.add(neighbor);
            mUnserved.add(node.getId());
        }
        Collections.sort(mBestNeighbors, mComparator);

        Collection<ITRSPTour> tours = new LinkedList<ITRSPTour>();
        boolean abort = false;
        while (!mUnserved.isEmpty() && !abort) {
            abort = true;
            // Initialize the tour
            DoublyLinkedIntList tour = initTour(technician);

            // Until all compatible requests have been visited
            while (!mUnserved.isEmpty()) {
                // ObservableNode node = selectNextNode(tour, tech, isUnserved, unserved);
                Neighbor neigh = selectNextNode(tour, technician);
                if (neigh == null)
                    break;
                insertNextNode(tour, neigh, technician);
                nodeInserted(tour, neigh, technician);
                // System.out.println("ins: " + neigh + " tour: " + tour + " bi:" + mBestNeighbors);
                abort = false;
            }
            tours.add(new TRSPSimpleTour(technician.getID(), getInstance(), tour));
        }
        if (!mUnserved.isEmpty())
            TRSPLogging.getOptimizationLogger().info(
                    "RndNearestFurthestIns.generateGiantTour: %s request(s) left unserved: %s", mUnserved.size(),
                    mUnserved);

        return tours;
    }

    @Override
    protected ITRSPTour generateFeasibleTour(Technician tech) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected DoublyLinkedIntList initTour(Technician tech) {
        DoublyLinkedIntList tour = new DoublyLinkedIntList(getInstance().getMaxId());
        // Add the technician home
        tour.add(tech.getHome().getID());

        // Add a random node
        int rnd = getParameters().getRCHRndStream().nextInt(0, mBestNeighbors.size() - 1);
        Neighbor seed = mBestNeighbors.remove(rnd);
        tour.add(seed.getNode().getId());
        nodeInserted(tour, seed, tech);
        // Return the list
        return tour;
    }

    protected Neighbor selectNextNode(List<Integer> tour, Technician tech) {
        if (mBestNeighbors.isEmpty())
            return null;
        else
            return mBestNeighbors.remove(nextIdx(mBestNeighbors.size()));
    }

    protected void insertNextNode(DoublyLinkedIntList tour, Neighbor neigh, Technician tech) {
        if (tour.isEmpty())
            tour.add(neigh.getNode().getId());

        if (tour.getLast() == neigh.getNeighbor().getId())
            tour.add(neigh.getNode().getId());
        else
            tour.insert(neigh.getNode().getId(), tour.getSucc(neigh.getNeighbor().getId()));
    }

    /**
     * Update the array of nearest tour node after a new node is inserted in the current tour
     * 
     * @param neigh
     *            the neighbor representing the node insertion
     */
    protected void nodeInserted(DoublyLinkedIntList tour, Neighbor neigh, Technician technician) {
        mNearestTourNode[neigh.getNode().getId()] = null;
        mUnserved.remove(neigh.getNode().getId());
        neigh.getNode().markAsServed();

        boolean changed = false;
        for (Integer u : mUnserved) {
            if (mNearestTourNode[u].getNeighbor() == neigh.getNeighbor()) {
                // The best insertion predecessor of u is the predecessor of the inserted node
                // Reevaluate the best insertion of node u over the whole tour
                Neighbor n = bestInsertion(tour, u, technician);
                mNearestTourNode[u].update(n.getNeighbor(), n.getDistance());
            } else {
                // Evaluate the new insertion positions

                // Insert between the predecessor and the inserted node
                double distPred = getInstance().getCostDelegate().getInsertionDetour(u, neigh.getNeighbor().getId(),
                        neigh.getNode().getId());

                // Insert after the inserted node
                Integer succ = tour.getSucc(neigh.getNode().getId());
                if (succ == DoublyLinkedIntList.UNDEFINED)
                    succ = tour.getFirst();
                double distNode = getInstance().getCostDelegate().getInsertionDetour(u, neigh.getNode().getId(), succ);

                ObservableNode best = null;
                double dist;
                if (mComparator.compare(distPred, distNode) < 0) {
                    best = neigh.getNeighbor();
                    dist = distPred;
                } else {
                    best = neigh.getNode();
                    dist = distNode;
                }

                // getInstance().getCostDelegate().getDistance(neigh.getNode().getId(), u);
                if (mComparator.compare(dist, mNearestTourNode[u].getDistance()) < 0) {
                    mNearestTourNode[u].update(best, dist);
                    changed = true;
                }
            }
        }
        if (changed)
            Collections.sort(mBestNeighbors, mComparator);
    }

    protected Neighbor bestInsertion(DoublyLinkedIntList tour, Integer node, Technician technician) {
        double bestInsCost = Double.POSITIVE_INFINITY;
        Integer bestInsPred = null;

        // Check for all possible insertions
        ListIterator<Integer> it = tour.listIterator();
        Integer pred = it.next();
        while (it.hasNext()) {
            Integer succ = it.next();
            double insCost = evaluateInsertionCost(node, pred, succ, technician, true);
            if (insCost < bestInsCost) {
                bestInsCost = insCost;
                bestInsPred = pred;
            }
            pred = succ;
        }
        // Insertion in the last position before the return to the depot
        double insCost = evaluateInsertionCost(node, tour.getLast(), tour.getFirst(), technician, true);
        if (insCost < bestInsCost) {
            bestInsPred = tour.getLast();
            bestInsCost = insCost;
        }

        return new Neighbor(getObsNode(node), getObsNode(bestInsPred), bestInsCost);
    }

    @Override
    public String toString() {
        return "RBI";
    }

    /**
     * <code>InvNeighborComparator</code> is used to sort neighbors in the RNI variant
     */
    protected class NeighborComparator implements Comparator<Neighbor> {
        @Override
        public int compare(Neighbor o1, Neighbor o2) {
            return compare(o1.getDistance(), o2.getDistance());
        }

        public int compare(double d1, double d2) {
            return Double.compare(d1, d2);
        }
    }

    /**
     * <code>InvNeighborComparator</code> is used to sort neighbors in the RFI variant
     */
    protected class InvNeighborComparator extends NeighborComparator {
        @Override
        public int compare(double d1, double d2) {
            return -Double.compare(d1, d2);
        }
    }
}
