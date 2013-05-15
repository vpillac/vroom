/**
 * 
 */
package vroom.trsp.optimization.rch;

import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.DoublyLinkedIntList;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.constraints.TRSPConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;

/**
 * <code>TRSPRndNearestFarthestInsOld</code> is an implementation of both random nearest and farthest insertion.
 * <p>
 * It extends {@link TRSPRndNearestNeighbor} as it also uses a sorted list of neighbors
 * </p>
 * <p>
 * Creation date: Oct 4, 2011 - 1:44:48 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 * @deprecated use {@link TRSPRndNearestFarthestIns} instead
 */
public class TRSPRndNearestFarthestInsOld extends TRSPRndNearestNeighbor {

    /**
     * Creates a new <code>TRSPRndNearestFarthestInsOld</code>
     * 
     * @param instance
     * @param parameters
     * @param constraintHandler
     * @param costDelegate
     * @param farthest
     *            <code>true</code> if the farthest insertion should be selected, <code>false</code> if the nearest
     *            insertion should be selected
     */
    public TRSPRndNearestFarthestInsOld(TRSPInstance instance, TRSPGlobalParameters parameters,
            TRSPConstraintHandler constraintHandler, TRSPCostDelegate costDelegate, boolean farthest) {
        super(instance, parameters, constraintHandler, costDelegate, farthest);
    }

    @Override
    protected List<Integer> initTour(int tech) {
        DoublyLinkedIntList list = new DoublyLinkedIntList(getInstance().getMaxId());
        // Add the technician home
        list.add(getInstance().getTechnician(tech).getHome().getID());
        // Add a random node
        ObservableNode seed = getCompatibleRequests(tech)[getParameters().getRCHRndStream().nextInt(0,
                getCompatibleRequests(tech).length - 1)];
        list.add(seed.getId());
        seed.markAsServed();
        // Return the list
        return list;
    }

    @Override
    protected ObservableNode selectNextNode(List<Integer> tour, int tech, int unserved) {
        // FIXME we must return a compatible node
        // Draw a random index k
        int targetK = nextIdx(unserved);

        // int bestNodeSeedIdx = -1;
        // int k = -1;
        // int[] currentNeighIndex = new int[tour.size()];
        // if (mFarthest) {
        // ListIterator<Integer> it = tour.listIterator();
        // while (it.hasNext()) {
        // int nodeIdx = it.nextIndex();
        // int node = it.next();
        // currentNeighIndex[nodeIdx] = getNeighbors(node).length - 1;
        // }
        // }
        //
        // while (k < targetK) {
        // ListIterator<Integer> it = tour.listIterator();
        // double bestDist = mFarthest ? 0 : Double.POSITIVE_INFINITY;
        // while (it.hasNext()) {
        // int seedIdx = it.nextIndex();
        // int seed = it.next();
        // // Candidate neighbor
        // // Ignore already served nodes
        // if (mFarthest) {
        // while (currentNeighIndex[seedIdx] >= 0
        // && !isUnserved[getNeighbors(seed)[currentNeighIndex[seedIdx]].getNode()
        // .getId()])
        // currentNeighIndex[seedIdx]--;
        // } else {
        // while (currentNeighIndex[seedIdx] < getNeighbors(seed).length
        // && !isUnserved[getNeighbors(seed)[currentNeighIndex[seedIdx]].getNode()
        // .getId()])
        // currentNeighIndex[seedIdx]++;
        //
        // }
        // if (currentNeighIndex[seedIdx] < 0
        // || currentNeighIndex[seedIdx] >= getNeighbors(seed).length)
        // // This seed has no unserved neighbors
        // continue;
        //
        // Neighbor cand = getNeighbors(seed)[currentNeighIndex[seedIdx]];
        // if (mFarthest ? cand.getDistance() > bestDist : cand.getDistance() < bestDist) {
        // // Find a new best candidate
        // bestNodeSeedIdx = seedIdx;
        // bestNode = getNeighbors(seed)[currentNeighIndex[seedIdx]];
        // bestDist = bestNode.getDistance();
        // }
        // }
        //
        // // We did not find any candidate node
        // if (bestNodeSeedIdx < 0)
        // return null;
        //
        // k++;
        // // We found the k-th farthest/closest node
        // // Update the offset of the corresponding seed
        // if (mFarthest)
        // currentNeighIndex[bestNodeSeedIdx]--;
        // else
        // currentNeighIndex[bestNodeSeedIdx]++;
        // }
        //
        // return bestNode != null ? bestNode.getNode() : null;

        NeighborListIterator[] neighbors = new NeighborListIterator[tour.size()];
        int i = 0;
        for (Integer n : tour) {
            neighbors[i] = getNeighbors(n).iterator();
            neighbors[i].next();
            i++;
        }

        int k = -1;

        Neighbor kthBestNeigh = null;
        Neighbor selectedNeigh = null;
        while (k < targetK) {
            i = 0;
            NeighborListIterator bestIt = null;
            for (Integer n : tour) {
                Neighbor neigh = neighbors[i].peek();
                if (neigh != null && (kthBestNeigh == null || kthBestNeigh.compareTo(neigh) < 0)) {
                    kthBestNeigh = neigh;
                    bestIt = neighbors[i];
                }
                i++;
            }

            if (bestIt != null) {
                selectedNeigh = kthBestNeigh;
                if (bestIt.hasNext()) // Shift the iterator
                    bestIt.next();
            }
            k++;
        }

        if (selectedNeigh != null)
            return selectedNeigh.getNode();
        else
            return null;
    }

    @Override
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
            double insCost = getInstance().getCostDelegate().getInsertionCost(node.getId(), pred, succ, technician);
            if (insCost < bestInsCost) {
                bestInsCost = insCost;
                bestInsSucc = succ;
            }
        }
        // Insertion in the last position before the return to the depot
        Integer succ = tour.getFirst();
        double insCost = getInstance().getCostDelegate().getInsertionCost(node.getId(), pred, succ, technician);
        if (insCost < bestInsCost) {
            tour.add(node.getId());
        } else {
            tour.insert(node.getId(), bestInsSucc);
        }
    }

    @Override
    public String toString() {
        return isInvertedSortOrder() ? "RFI" : "RNI";
    }
}
