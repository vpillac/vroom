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
 * <code>RndNearestFurthestIns</code> is an implementation of both random nearest and farthest insertion.
 * <p>
 * Creation date: Oct 4, 2011 - 1:44:48 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RndNearestFurthestIns extends TRSPRndConstructiveHeuristic {

    private final boolean             mRFI;

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
    public RndNearestFurthestIns(TRSPInstance instance, TRSPGlobalParameters parameters,
            TourConstraintHandler constraintHandler, TRSPCostDelegate costDelegate, int kMax, boolean rfi) {
        super(instance, parameters, constraintHandler, costDelegate, kMax);
        mNearestTourNode = new Neighbor[getInstance().getMaxId()];
        mUnserved = new DoublyLinkedIntSet(getInstance().getMaxId());
        mRFI = rfi;
        mComparator = mRFI ? new InvNeighborComparator() : new NeighborComparator();
        // mBestNeighbors = new LimitedSortedList<Neighbor>(getKmax(), mComparator);
        mBestNeighbors = new ArrayList<Neighbor>(mUnserved.size());
    }

    @Override
    protected Collection<ITRSPTour> generateGiantTour(Technician technician) {
        int tech = technician.getID();

        Arrays.fill(mNearestTourNode, null);
        mUnserved.clear();

        // Initialize the nearest tour node array
        for (ObservableNode node : getCompatibleRequests(tech)) {
            double dist = getInstance().getCostDelegate().getDistance(getHome(tech).getId(), node.getId());
            Neighbor neighbor = new Neighbor(node, getHome(tech), dist);

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
            List<Integer> tour = initTour(tech);

            // Until all compatible requests have been visited
            while (!mUnserved.isEmpty()) {
                // ObservableNode node = selectNextNode(tour, tech, isUnserved, unserved);
                ObservableNode neigh = selectNextNode(tour, tech);
                if (neigh == null)
                    break;
                insertNextNode(tour, neigh, tech);
                nodeInserted(neigh);
                abort = false;
            }
            tours.add(new TRSPSimpleTour(tech, getInstance(), tour));
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

    protected List<Integer> initTour(int tech) {
        DoublyLinkedIntList tour = new DoublyLinkedIntList(getInstance().getMaxId());
        // Add the technician home
        tour.add(getInstance().getTechnician(tech).getHome().getID());

        // TODO check if we really want to add a random node at the beginning
        // Add a random node
        // int rnd = getParameters().getRCHRndStream().nextInt(0, mBestNeighbors.size() - 1);
        // Neighbor seed = mBestNeighbors.remove(rnd);
        // tour.add(seed.getNode().getId());
        // nodeInserted(seed.getNode());
        // Return the list
        return tour;
    }

    protected ObservableNode selectNextNode(List<Integer> tour, int tech) {
        if (mBestNeighbors.isEmpty())
            return null;
        else
            return mBestNeighbors.remove(nextIdx(mBestNeighbors.size())).getNode();
    }

    protected void insertNextNode(List<Integer> ltour, ObservableNode node, int tech) {
        if (ltour.isEmpty())
            ltour.add(node.getId());
        double bestInsCost = Double.POSITIVE_INFINITY;
        Integer bestInsSucc = null;

        DoublyLinkedIntList tour = (DoublyLinkedIntList) ltour;

        Technician technician = getInstance().getTechnician(tech);

        // Check for all possible insertions
        ListIterator<Integer> it = tour.listIterator();
        Integer pred = it.next();
        while (it.hasNext()) {
            Integer succ = it.next();
            double insCost = evaluateInsertionCost(node.getId(), pred, succ, technician, true);
            if (insCost < bestInsCost) {
                bestInsCost = insCost;
                bestInsSucc = succ;
            }
            pred = succ;
        }
        // Insertion in the last position before the return to the depot
        Integer succ = tour.getFirst();
        double insCost = evaluateInsertionCost(node.getId(), pred, succ, technician, true);
        if (insCost < bestInsCost) {
            tour.add(node.getId());
        } else {
            tour.insert(node.getId(), bestInsSucc);
        }
    }

    /**
     * Update the array of nearest tour node after a new node is inserted in the current tour
     * 
     * @param node
     *            the inserted node
     */
    protected void nodeInserted(ObservableNode node) {
        mNearestTourNode[node.getId()] = null;
        mUnserved.remove(node.getId());
        node.markAsServed();

        boolean changed = false;
        for (Integer u : mUnserved) {
            double dist = getInstance().getCostDelegate().getDistance(node.getId(), u);
            if (mComparator.compare(dist, mNearestTourNode[u].getDistance()) < 0) {
                // mNearestTourNode[u] = new Neighbor(getObsNode(u), node, dist);
                mNearestTourNode[u].update(node, dist);
                // Maintain a list of the Kmax-best decisions
                // mBestNeighbors.add(mNearestTourNode[u]);
                changed = true;
            }
        }
        if (changed)
            Collections.sort(mBestNeighbors, mComparator);
    }

    @Override
    public String toString() {
        return mRFI ? "RFI" : "RNI";
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
