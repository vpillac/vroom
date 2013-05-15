package vroom.common.heuristics.vrp;

import java.util.ArrayList;
import java.util.LinkedList;

import umontreal.iro.lecuyer.rng.RandomPermutation;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.GenericNeighborhood;
import vroom.common.heuristics.vrp.RelocateMove.RelocateAtomicMove;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.NodeInsertion;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;

public class RelocateNeighborhood<S extends ISolution> extends GenericNeighborhood<S, RelocateMove> {

    private final ISolutionFactory mSolutionFactory;

    /** Number of nodes to relocate **/
    private int                    mCardinality;

    /**
     * Getter for Number of nodes to relocate
     * 
     * @return the value of cardinality
     */
    public int getCardinality() {
        return mCardinality;
    }

    /**
     * Setter for Number of nodes to relocate
     * 
     * @param cardinality
     *            the value to be set for Number of nodes to relocate
     */
    public void setCardinality(int cardinality) {
        mCardinality = cardinality;
    }

    public RelocateNeighborhood(ConstraintHandler<S> constraintHandler, ISolutionFactory solFactory) {
        super(constraintHandler);
        setCardinality(1);
        mSolutionFactory = solFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean executeMoveImplem(S solution, IMove move) {
        RelocateMove mve = (RelocateMove) move;
        IVRPSolution<?> sol = (IVRPSolution<?>) solution;

        boolean b = true;
        for (RelocateAtomicMove reloc : mve.getAtomicMoves()) {
            sol.getRoute(reloc.getRoute()).remove(reloc.getNode());
            // b &= ((IVRPSolution<IRoute<INodeVisit>>)
            // solution).getRoute(reloc.getRoute())
            // .insertNode(reloc.getInsertion(), reloc.getNode());
        }
        for (RelocateAtomicMove reloc : mve.getAtomicMoves()) {
            evaluateCandidateMove(sol, reloc);

            if (reloc.getInsertion() == null) {
                // Add a new route to the solution
                IRoute<INodeVisit> newRte = (IRoute<INodeVisit>) mSolutionFactory.newRoute(sol, sol
                        .getParentInstance().getFleet().getVehicle());

                newRte.appendNode(sol.getParentInstance().getDepotsVisits().iterator().next());
                newRte.appendNode(reloc.getNode());
                newRte.appendNode(sol.getParentInstance().getDepotsVisits().iterator().next());
                evaluateCandidateMove(sol, reloc);
            } else {
                b &= ((IRoute<INodeVisit>) reloc.getInsertion().getRoute()).insertNode(
                        reloc.getInsertion(), reloc.getNode());
            }
        }
        return b;
    }

    @Override
    protected LinkedList<RelocateMove> generateCandidateList(S solution, IParameters params) {
        if (getCardinality() != 1) {
            throw new UnsupportedOperationException(
                    "generateCandidateList not implemented yet for cardinality other than 1");
        }
        LinkedList<RelocateMove> cand = new LinkedList<RelocateMove>();

        IVRPSolution<?> sol = (IVRPSolution<?>) solution;

        int ir = 0;
        for (IRoute<?> r : sol) {
            int in = 0;
            for (INodeVisit n : r) {
                if (!n.isFixed()) {
                    RelocateMove m = new RelocateMove(sol);
                    m.addAtomicMove(m.newAtomicMove(n, ir, in));
                    cand.add(m);
                }
                in++;
            }
            ir++;
        }

        return cand;
    }

    @Override
    protected double evaluateCandidateMove(RelocateMove cand) {
        IVRPSolution<?> sol = cand.getSolution();
        for (RelocateAtomicMove reloc : cand.getAtomicMoves()) {
            evaluateCandidateMove(sol, reloc);
        }

        return cand.getImprovement();
    }

    protected void evaluateCandidateMove(IVRPSolution<?> sol, RelocateAtomicMove reloc) {
        NodeInsertion bestIns = null;
        for (int rte = 0; rte < sol.getRouteCount(); rte++) {
            IRoute<?> route = sol.getRoute(rte);
            if (rte != reloc.getRoute()
                    && route.canAccommodateRequest(reloc.getNode().getParentRequest())) {
                NodeInsertion ins = route.getBestNodeInsertion(reloc.getNode());
                if (bestIns == null || ins.getCost() < bestIns.getCost()) {
                    bestIns = ins;
                }
            }
        }
        reloc.setInsertion(bestIns);
    }

    @Override
    public RelocateMove randomNonImproving(S solution, IParameters params) {
        RelocateMove move = new RelocateMove((IVRPSolution<?>) solution);
        int count = 0;
        RandomStream rnd = params.getRandomStream();
        IVRPSolution<?> sol = (IVRPSolution<?>) solution;

        if (sol.getRouteCount() >= 2) {

            ArrayList<Integer> candRoutes = new ArrayList<Integer>(sol.getRouteCount());
            LinkedList<?>[] candidates = new LinkedList<?>[sol.getRouteCount()];
            for (int r = 0; r < candidates.length; r++) {
                candRoutes.add(r);
                LinkedList<Integer> nodes = new LinkedList<Integer>();
                for (int i = 0; i < sol.getRoute(r).length(); i++) {
                    nodes.add(i);
                }
                RandomPermutation.shuffle(candidates, params.getRandomStream());
                candidates[r] = nodes;
            }

            while (count < getCardinality() && !candRoutes.isEmpty()) {
                int r = rnd.nextInt(0, candRoutes.size() - 1);
                int rndRouteIdx = candRoutes.get(r);
                IRoute<?> rndroute = sol.getRoute(rndRouteIdx);
                if (rndroute.length() > 2 && !candidates[rndRouteIdx].isEmpty()) {
                    int n = (Integer) candidates[rndRouteIdx].pop();
                    if (!rndroute.getNodeAt(n).isDepot()) {
                        move.addAtomicMove(move.newAtomicMove(rndroute.getNodeAt(n), rndRouteIdx, n));
                        // evaluateCandidateMove(sol, reloc);
                        // if (reloc.getInsertion() != null
                        // && getConstraintHandler().checkMove(solution, reloc))
                        // {
                        // move.addAtomicMove(reloc);
                        // count++;
                        // }
                    }
                } else {
                    candRoutes.remove(r);
                }
            }
        }

        return move;
    };

    @Override
    public String toString() {
        return String.format("%s(%s)", super.toString(), getCardinality());
    }

    @Override
    public String getShortName() {
        return "reloc";
    }

}
