/**
 * 
 */
package vroom..optimization.mTSPHeur;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import vroom.common.heuristics.HeuristicStatus;
import vroom.common.modelling.dataModel.Depot;
import vroom.common.modelling.dataModel.IVRPRequest;
import vroom..datamodel.Instance;
import vroom..datamodel.Request;
import vroom..datamodel.Technician;
import vroom..optimization.ConstructiveHeuristic;
import vroom..visualization.Visualization;

/**
 * <code>mTSPHeuristic</code> is the root class for a multiple TSP based constructive heuristic for the .
 * <p>
 * The original paper is first decomposed into a set of TSP (one per technician) by relaxing tooling constraints, doing
 * a Lagrangian relaxation of TW constraints. Each problem is solved trying to minimize the total time
 * (travel+service+penalizations). Afterwards individual solutions are merged to obtain a solution to the 
 * </p>
 * <p>
 * Creation date: Feb 17, 2011 - 10:33:47 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class mTSPHeuristic extends ConstructiveHeuristic implements Runnable {

    /** A list of the TSP subproblems */
    private List<Instance> mSubproblems;

    public mTSPHeuristic() {
        super();
    }

    /**
     * Decompose the original instance into a set of subproblems
     */
    protected void decompose() {
        int numTech = getInstance().getFleet().size();
        mSubproblems = new ArrayList<Instance>(numTech);

        // Create the subproblems
        for (int t = 0; t < numTech; t++) {
            // The considered technician
            Technician tech = getInstance().getFleet().getVehicle(t);

            // List of compatible requests
            LinkedList<Request> compatibleRequests = new LinkedList<Request>();
            for (IVRPRequest r : getInstance().getRequests()) {
                Request req = (Request) r;
                if (tech.getSkillSet().isCompatibleWith(req.getSkillSet()))
                    compatibleRequests.add(req);
            }

            // Create the subproblem instance
            String name = String.format("%s_mTSP%s", getInstance().getName(), t);
            ArrayList<Depot> depots = new ArrayList<Depot>(2);
            depots.add(getInstance().getDepot(0));
            depots.add(tech.getHome());
            Instance subproblem = new Instance(name, Collections.singleton(tech),
                    getInstance().getSkillCount(), getInstance().getToolCount(), getInstance().getSpareCount(), depots,
                    compatibleRequests);

            mSubproblems.add(subproblem);
        }
    }

    /**
     * Solve independently the subproblems
     */
    protected void solveSubproblems() {
        MTSPSubproblemSolver solver = new MTSPSubproblemSolver(getInstance());
        solver.setRandomStream(getRandomStream());

        for (Instance sub : mSubproblems) {
            System.out.println("-------------------------------------");
            System.out.println("Solving subproblem " + sub);

            solver.setSubproblem(sub);

            solver.run();
            System.out.println(" > Final Solution");
            System.out.println(solver.getSolution());
            Visualization.showVisualizationFrame(solver.getSolution());
        }
    }

    /**
     * Recombine the subproblems in a solution of the original 
     */
    protected void recombine() {

    }

    @Override
    public HeuristicStatus call() {
        checkState();

        mRunnning = true;

        decompose();
        solveSubproblems();
        recombine();
        mRunnning = false;

        return HeuristicStatus.LOCAL_OPTIMA;
    }

}
