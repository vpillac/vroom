/**
 * 
 */
package vroom.trsp.optimization.rch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import umontreal.iro.lecuyer.rng.RandomPermutation;
import vroom.common.modeling.dataModel.Depot;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSimpleTour;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>RndNearestNeighbor</code> is an implementation of the random nearest neighbor heuristic for the TRSP.
 * <p>
 * Complexity of the {@link #generateGiantTours()} is in O(Kn²), where K is the number of technicians and n the number
 * of requests.
 * </p>
 * <p>
 * Creation date: Sep 22, 2011 - 1:45:11 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RndNearestNeighbor extends TRSPRndConstructiveHeuristic {

    /** A matrix containing the neighbors of a node sorted in increasing distance order */
    private NeighborList[] mNeighbors;

    /**
     * Creates a new <code>RndNearestNeighbor</code>
     * 
     * @param instance
     * @param parameters
     * @param constraintHandler
     * @param costDelegate
     * @param kMax
     *            the K<sub>max</sub> value
     * @param giantTours
     *            {@code true} if the heuristic will only be used to generate giant tours, {@code false} for feasible
     *            tours only
     */
    public RndNearestNeighbor(TRSPInstance instance, TRSPGlobalParameters parameters,
            TourConstraintHandler constraintHandler, TRSPCostDelegate costDelegate, int kMax) {
        super(instance, parameters, constraintHandler, costDelegate, kMax);
    }

    @Override
    protected void initialize(List<ObservableNode> requestsId) {
        super.initialize(requestsId);

        // Add the main depot
        if (!isGiantTours() && !getParameters().isCVRPTW())
            requestsId.add(getObsNode(getInstance().getMainDepot().getID()));

        mNeighbors = new NeighborList[getInstance().getMaxId()];
        initNeighborMatrix(requestsId);
    }

    /**
     * Initialize the matrix containing the nearest neighbors of all nodes
     * 
     * @param requestsId
     */
    private void initNeighborMatrix(List<ObservableNode> requestsId) {
        for (Depot dep : getInstance().getDepots()) {
            mNeighbors[dep.getID()] = initNeighborsList(dep.getID(), requestsId);
        }
        for (ObservableNode req : requestsId) {
            mNeighbors[req.getId()] = initNeighborsList(req.getId(), requestsId);
        }
    }

    /**
     * Returns the sorted list of neighbors of a node
     * 
     * @param node
     * @param requestsId
     * @return the sorted list of neighbors of a node
     */
    private NeighborList initNeighborsList(Integer node, List<ObservableNode> requestsId) {
        // Initialize the list of possible successors
        ArrayList<Neighbor> neigh = new ArrayList<Neighbor>(getInstance().getRequestCount());
        for (ObservableNode succ : requestsId) {
            neigh.add(new Neighbor(getObsNode(node), succ, getInstance().getCostDelegate()
                    .getDistance(node, succ.getId())));
        }

        // Sort the list of successors
        Collections.sort(neigh);

        // Store the list of successors
        return new NeighborList(getInstance(), neigh, false);
    }

    /**
     * Returns an array containing the neighbors, sorted according to their distance to the specified node
     * 
     * @param node
     * @return an array containing the neighbors, sorted according to their distance to the specified node
     */
    protected NeighborList getNeighbors(int node) {
        return mNeighbors[node];
    }

    @Override
    protected Collection<ITRSPTour> generateGiantTour(Technician technician) {
        int tech = technician.getID();
        // Initialize the set of unserved requests
        // boolean[] isUnserved = new boolean[getInstance().getMaxId()];

        int unserved = getCompatibleRequests(tech).length;
        resetNodes(tech);

        Collection<ITRSPTour> tours = new LinkedList<ITRSPTour>();
        boolean abort = false;
        while (unserved > 0 && !abort) {
            abort = true;
            // Initialize the tour
            List<Integer> tour = initTour(tech);
            // Until all compatible requests have been visited
            while (unserved > 0) {
                // ObservableNode node = selectNextNode(tour, tech, isUnserved, unserved);
                ObservableNode node = selectNextNode(tour, tech, unserved);
                if (node == null)
                    break;
                // if (node.isServed())
                // throw new IllegalStateException("Attempting to add a node that is already present");
                insertNextNode(tour, node, tech);
                node.markAsServed();
                // uns.remove(node.getId());
                unserved--;
                abort = false;
            }
            tours.add(new TRSPSimpleTour(tech, getInstance(), tour));
        }
        if (unserved > 0)
            TRSPLogging.getOptimizationLogger().warn(
                    "RndNearestNeighbor.generateGiantTour: %s request(s) left unserved", unserved);

        return tours;
    }

    @Override
    protected Collection<ITRSPTour> generateFeasibleTours() {
        // Reset all nodes
        resetNodes();

        int unserved = getInstance().getRequestCount();

        // Initialization of the solution
        // ----------------
        TRSPSolution solution = new TRSPSolution(getInstance(), getCostDelegate());

        TRSPTour[] clone = new TRSPTour[getInstance().getFleet().size()];
        boolean[] mainDepotVisited = new boolean[getInstance().getFleet().size()];
        Integer[] last = new Integer[getInstance().getFleet().size()];
        boolean[] finished = new boolean[getInstance().getFleet().size()];

        for (Technician technician : getInstance().getFleet()) {
            int tech = technician.getID();
            TRSPTour tour = solution.getTour(tech);

            tour.appendNode(technician.getHome().getID());
            tour.appendNode(getInstance().getHomeDuplicate(technician.getHome().getID()));
            last[tech] = technician.getHome().getID();

            tour.setAutoUpdated(true);
        }
        // ----------------

        // Shuffle the list of technicians
        Technician[] fleet = new Technician[getInstance().getFleet().size()];
        int v = 0;
        for (Technician technician : getInstance().getFleet()) {
            fleet[v++] = technician;
        }
        RandomPermutation.shuffle(fleet, getParameters().getRCHRndStream());

        // We abort the heuristic if all tours are "finished"
        boolean abort = false;
        // Until all compatible requests have been visited
        while (unserved > 0 && !abort) {
            abort = true;
            // Insert a request into each of the tours
            for (Technician technician : fleet) {
                int tech = technician.getID();

                if (finished[tech])
                    continue; // The tour cannot be build any further

                abort = false;
                // ObservableNode node = selectNextNode(tour, tech, isUnserved, unserved);
                ObservableNode node = selectNextFeasibleNode(solution.getTour(tech), last[tech],
                        tech, unserved);
                if (node == null) {
                    if (mainDepotVisited[tech])
                        finished[tech] = true; // Nothing else to do
                    else {
                        // Try to insert a visit to the main depot
                        clone[tech] = solution.getTour(tech).clone();
                        InsertionMove ins = InsertionMove.findInsertion(solution.getTour(tech)
                                .getMainDepotId(), solution.getTour(tech), getCostDelegate(),
                                getTourConstraintHandler(), true, true);
                        if (ins.isFeasible()) {
                            InsertionMove.executeMove(ins);
                            mainDepotVisited[tech] = true;
                        } else {
                            finished[tech] = true; // Nothing else to do
                        }
                    }
                } else {
                    // if (node.isServed())
                    // throw new IllegalStateException("Attempting to add a node that is already present");
                    // insertNextNode(tour, node, tech);
                    solution.getTour(tech).insertBefore(solution.getTour(tech).getLastNode(),
                            node.getId());
                    last[tech] = node.getId();
                    node.markAsServed();
                    // uns.remove(node.getId());
                    if (getInstance().isRequest(node.getId()))
                        unserved--;
                    else if (node.getId() == solution.getTour(tech).getMainDepotId())
                        mainDepotVisited[tech] = true;
                }
            }
        }

        for (Technician technician : getInstance().getFleet()) {
            int tech = technician.getID();
            if (clone[tech] != null && solution.getTour(tech).length() == clone[tech].length() + 1) {
                solution.importTour(clone[tech]);
            }
        }

        if (unserved > 0)
            TRSPLogging.getOptimizationLogger().lowDebug(
                    "RndNearestNeighbor.generateFeasibleTour: %s request(s) left unserved",
                    unserved);

        ArrayList<ITRSPTour> tours = new ArrayList<ITRSPTour>(solution.getTourCount());
        for (TRSPTour trspTour : solution) {
            tours.add(trspTour);
        }

        return tours;
    }

    @Override
    protected ITRSPTour generateFeasibleTour(Technician technician) {
        int tech = technician.getID();
        int unserved = getCompatibleRequests(tech).length;
        resetNodes(tech);

        TRSPSolution solution = new TRSPSolution(getInstance(), getCostDelegate());
        TRSPTour tour = new TRSPTour(solution, technician);

        Integer last = technician.getHome().getID();
        tour.appendNode(last);
        tour.appendNode(getInstance().getHomeDuplicate(last));

        tour.setAutoUpdated(true);

        boolean mainDepotVisited = false;

        TRSPTour clone = null;

        // Until all compatible requests have been visited
        while (unserved > 0) {
            // ObservableNode node = selectNextNode(tour, tech, isUnserved, unserved);
            ObservableNode node = selectNextFeasibleNode(tour, last, tech, unserved);
            if (node == null)
                if (mainDepotVisited)
                    break; // Nothing else to do
                else {
                    // Try to insert a visit to the main depot
                    clone = tour.clone();
                    InsertionMove ins = InsertionMove.findInsertion(tour.getMainDepotId(), tour,
                            getCostDelegate(), getTourConstraintHandler(), true, true);
                    if (ins.isFeasible()) {
                        InsertionMove.executeMove(ins);
                        mainDepotVisited = true;
                        continue;
                    } else {
                        break;
                    }
                }
            // if (node.isServed())
            // throw new IllegalStateException("Attempting to add a node that is already present");
            // insertNextNode(tour, node, tech);
            tour.insertBefore(tour.getLastNode(), node.getId());
            last = node.getId();
            node.markAsServed();
            // uns.remove(node.getId());
            if (getInstance().isRequest(node.getId()))
                unserved--;
            else if (node.getId() == tour.getMainDepotId())
                mainDepotVisited = true;
        }

        if (clone != null && tour.length() == clone.length() + 1) {
            tour = clone;
            TRSPLogging
                    .getOptimizationLogger()
                    .lowDebug(
                            "RndNearestNeighbor.generateFeasibleTour: the visit to the main depot did not allowed further improvements");
        }

        if (unserved > 0)
            TRSPLogging.getOptimizationLogger().lowDebug(
                    "RndNearestNeighbor.generateFeasibleTour: %s request(s) left unserved",
                    unserved);
        return tour;
    }

    /**
     * Creates a new empty tour using the most efficient data structure
     * 
     * @param tech
     *            the technician id
     * @return a new empty tour using the most efficient data structure
     */
    protected List<Integer> initTour(int tech) {
        List<Integer> tour = new ArrayList<Integer>(getCompatibleRequests(tech).length);
        tour.add(getInstance().getTechnician(tech).getHome().getID());
        return tour;
    }

    /**
     * Select the next node to be inserted
     * 
     * @param tour
     *            the current tour
     * @param tech
     *            the technician id
     * @param unserved
     *            the number of unserved requests
     * @param seed
     *            the previously inserted node
     * @return the next node to be inserted, or <code>null</code> if no suitable node was found
     */
    protected ObservableNode selectNextNode(List<Integer> tour, int tech, int unserved) {
        int k = nextIdx(unserved);
        int itwfeas = -1;
        int itwinfeas = -1;
        Integer last = tour.get(tour.size() - 1);
        NeighborListIterator it = getNeighbors(last).iterator();
        Neighbor n = null;
        Neighbor kUnsTWFeas = null;
        Neighbor kUnsTWInf = null;
        while (it.hasNext() && itwfeas < k) {
            n = it.next();
            Integer cand = n.getNeighbor().getId();
            if (!n.getNeighbor().isServed() // Unserved node
                    && getInstance().isCompatible(tech, cand)) {
                if (!isCheckTWFeas() || getInstance().isArcTWFeasible(last, cand)) {
                    itwfeas++;
                    kUnsTWFeas = n;
                } else if (itwinfeas < k) {
                    itwinfeas++;
                    kUnsTWInf = n;
                }
            }
        }

        if (kUnsTWFeas != null)
            // We found an unserved request that is TW feasible
            return kUnsTWFeas.getNeighbor();
        else
            // We did not find a TW feasible unserved request, return the k-th TW infeasible unserved request
            return kUnsTWInf != null ? kUnsTWInf.getNeighbor() : null;
    }

    /**
     * Select the next node to be inserted
     * 
     * @param tour
     *            the current tour
     * @param tech
     *            the technician id
     * @param unserved
     *            the number of unserved requests
     * @param seed
     *            the previously inserted node
     * @return the next node to be inserted, or <code>null</code> if no suitable node was found
     */
    protected ObservableNode selectNextFeasibleNode(TRSPTour tour, Integer last, int tech,
            int unserved) {
        int k = nextIdx(unserved);
        int i = -1;
        NeighborListIterator it = getNeighbors(last).iterator();
        Neighbor n = null;
        Neighbor kuns = null;
        while (it.hasNext() && i < k) {
            n = it.next();
            if (!n.getNeighbor().isServed() // Unserved node
                    && getTourConstraintHandler().isFeasible(
                            tour,
                            new InsertionMove(n.getNeighbor().getId(), tour, 0, last, tour
                                    .getLastNode()))) {
                i++;
                kuns = n;
            }
        }
        return kuns != null ? kuns.getNeighbor() : null;
    }

    /**
     * Insert the <code>node</code> in the given <code>tour</code>
     * 
     * @param tour
     *            the tour in which <code>node</code> will be inserted
     * @param node
     *            the node to be inserted
     * @param tech
     *            TODO
     */
    protected void insertNextNode(List<Integer> tour, ObservableNode node, int tech) {
        tour.add(node.getId());
    }

    @Override
    public String toString() {
        return "RNN";
    }

}
