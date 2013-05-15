package vroom.trsp.optimization.mTSPHeur;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import umontreal.iro.lecuyer.rng.RandomPermutation;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.IInitialization;
import vroom.common.heuristics.vls.IVLSState;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;

/**
 * The Class <code>MTSPRandomInitialization</code> contains a random initialization heuristic for the TSP subproblems
 * resulting from the decomposition of the TRSP
 * <p>
 * Creation date: Feb 17, 2011 - 4:11:11 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MTSPRandomInitialization implements IInitialization<TRSPTour> {

    /** the original instance **/
    private final TRSPInstance mOriginalInstance;

    /**
     * Getter for the original instance
     * 
     * @return the original instance
     */
    public TRSPInstance getOriginalInstance() {
        return this.mOriginalInstance;
    }

    /** the route cost delagate that will be used in all routes created by this class **/
    private final TRSPCostDelegate mCostDelegate;

    /**
     * Getter for the route cost delagate that will be used in all routes created by this class
     * 
     * @return the cost delagate
     */
    public TRSPCostDelegate getCostDelegate() {
        return this.mCostDelegate;
    }

    public MTSPRandomInitialization(TRSPInstance originalInstance, TRSPCostDelegate costDelegate) {
        super();
        mOriginalInstance = originalInstance;
        mCostDelegate = costDelegate;
    }

    /**
     * @param instance
     *            the instance for which a solution will be generated
     * @param stream
     *            a random stream
     * @return a new {@link TRSPTour}
     * @see #newSolution(IVLSState, IInstance, IParameters)
     */
    public TRSPTour newSolution(TRSPInstance instance, RandomStream stream) {
        // The technician
        if (instance.getFleet().size() != 1)
            throw new UnsupportedOperationException(
                    "MTSPRandomInitialization can only be used on subproblems with a single technician");
        Technician tech = instance.getFleet().getVehicle();

        TRSPTour route = new TRSPTour(instance, instance.getFleet().getVehicle(), getCostDelegate());

        // The route starts at home
        route.appendNode(tech.getHome().getID());
        // It visits the main depot
        route.appendNode(instance.getMainDepot().getID());

        List<TRSPRequest> requests = new ArrayList<TRSPRequest>(instance.getRequests());

        // Sort the requests depending on their due date
        Collections.sort(requests, new Comparator<TRSPRequest>() {

            @Override
            public int compare(TRSPRequest o1, TRSPRequest o2) {
                return (int) (o1.getTimeWindow().endAsDouble() - o2.getTimeWindow().endAsDouble());
            }
        });

        // Randomize the list
        RandomPermutation.shuffle(requests, (int) (requests.size() * 0.5), stream);

        // Append to route
        for (TRSPRequest req : requests) {
            route.appendNode(req.getID());
        }

        // The route finishes at home (implicit in route structure?)
        // route.appendNode(tech.getHome().getID());

        return route;
    }

    @Override
    public TRSPTour newSolution(IVLSState<TRSPTour> state, IInstance instance, IParameters params) {
        return newSolution((TRSPInstance) instance, params.getRandomStream());
    }
}
