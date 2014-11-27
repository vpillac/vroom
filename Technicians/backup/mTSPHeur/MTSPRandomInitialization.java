package vroom..optimization.mTSPHeur;

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
import vroom..datamodel.Instance;
import vroom..datamodel.Request;
import vroom..datamodel.Tour;
import vroom..datamodel.Technician;
import vroom..datamodel.costDelegates.CostDelegate;

/**
 * The Class <code>MTSPRandomInitialization</code> contains a random initialization heuristic for the TSP subproblems
 * resulting from the decomposition of the 
 * <p>
 * Creation date: Feb 17, 2011 - 4:11:11 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MTSPRandomInitialization implements IInitialization<Tour> {

    /** the original instance **/
    private final Instance mOriginalInstance;

    /**
     * Getter for the original instance
     * 
     * @return the original instance
     */
    public Instance getOriginalInstance() {
        return this.mOriginalInstance;
    }

    /** the route cost delagate that will be used in all routes created by this class **/
    private final CostDelegate mCostDelegate;

    /**
     * Getter for the route cost delagate that will be used in all routes created by this class
     * 
     * @return the cost delagate
     */
    public CostDelegate getCostDelegate() {
        return this.mCostDelegate;
    }

    public MTSPRandomInitialization(Instance originalInstance, CostDelegate costDelegate) {
        super();
        mOriginalInstance = originalInstance;
        mCostDelegate = costDelegate;
    }

    /**
     * @param instance
     *            the instance for which a solution will be generated
     * @param stream
     *            a random stream
     * @return a new {@link Tour}
     * @see #newSolution(IVLSState, IInstance, IParameters)
     */
    public Tour newSolution(Instance instance, RandomStream stream) {
        // The technician
        if (instance.getFleet().size() != 1)
            throw new UnsupportedOperationException(
                    "MTSPRandomInitialization can only be used on subproblems with a single technician");
        Technician tech = instance.getFleet().getVehicle();

        Tour route = new Tour(instance, instance.getFleet().getVehicle(), getCostDelegate());

        // The route starts at home
        route.appendNode(tech.getHome().getID());
        // It visits the main depot
        route.appendNode(instance.getMainDepot().getID());

        List<Request> requests = new ArrayList<Request>(instance.getRequests());

        // Sort the requests depending on their due date
        Collections.sort(requests, new Comparator<Request>() {

            @Override
            public int compare(Request o1, Request o2) {
                return (int) (o1.getTimeWindow().endAsDouble() - o2.getTimeWindow().endAsDouble());
            }
        });

        // Randomize the list
        RandomPermutation.shuffle(requests, (int) (requests.size() * 0.5), stream);

        // Append to route
        for (Request req : requests) {
            route.appendNode(req.getID());
        }

        // The route finishes at home (implicit in route structure?)
        // route.appendNode(tech.getHome().getID());

        return route;
    }

    @Override
    public Tour newSolution(IVLSState<Tour> state, IInstance instance, IParameters params) {
        return newSolution((Instance) instance, params.getRandomStream());
    }
}
