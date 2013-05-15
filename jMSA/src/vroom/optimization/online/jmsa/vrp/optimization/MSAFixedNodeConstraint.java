package vroom.optimization.online.jmsa.vrp.optimization;

import vroom.common.heuristics.vrp.OrOptMove;
import vroom.common.heuristics.vrp.RelocateMove.RelocateAtomicMove;
import vroom.common.heuristics.vrp.StringExchangeMove;
import vroom.common.heuristics.vrp.SwapMove;
import vroom.common.heuristics.vrp.TwoOptMove;
import vroom.common.heuristics.vrp.constraints.FixedNodesConstraint;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;
import vroom.optimization.online.jmsa.vrp.VRPShrunkRequest;

public class MSAFixedNodeConstraint<S extends VRPScenario> extends FixedNodesConstraint<S> {

    @Override
    public boolean checkSwap(S solution, SwapMove m) {
        VRPScenarioRoute rI = solution.getRoute(m.getRouteI());
        VRPScenarioRoute rJ = solution.getRoute(m.getRouteJ());
        int i = m.getI();
        int j = m.getJ();

        // Swap a shrunk node only to the same position (first or last)
        if (rI.getNodeAt(i) instanceof VRPShrunkRequest
                || rJ.getNodeAt(j) instanceof VRPShrunkRequest) {
            return rI.length() > 2 && rJ.length() > 2
                    && ((i == 1 && j == 1) || (i == rI.length() - 2 && j == rJ.length() - 2));
        } else {
            return super.checkSwap(solution, m);
        }
    }

    @Override
    public boolean checkTwoOpt(S solution, TwoOptMove move) {
        IRoute<?> rI = solution.getRoute(move.getRouteI());
        IRoute<?> rJ = solution.getRoute(move.getRouteJ());
        int i = move.getI();
        int j = move.getJ();

        if (i >= rI.length() - 1 || j >= rJ.length() - 1) {
            return false;
        }
        // Allow insertion after a shrunk request even when the next node is a
        // depot when route is of length 3 (<o,[shrunk],0>)
        else if (rI.getNodeAt(i) instanceof VRPShrunkRequest
                || rI.getNodeAt(i + 1) instanceof VRPShrunkRequest) {
            return rI.length() == 3;
        } else if (rJ.getNodeAt(j) instanceof VRPShrunkRequest
                || rJ.getNodeAt(j + 1) instanceof VRPShrunkRequest) {
            return rJ.length() == 3;
        } else {
            return !(rI.getNodeAt(i).isFixed() && rI.getNodeAt(i + 1).isFixed())
                    && !(rJ.getNodeAt(j).isFixed() && rJ.getNodeAt(j + 1).isFixed());
        }
    }

    @Override
    public boolean checkOrOpt(S solution, OrOptMove<?> move) {
        IRoute<?> rI = solution.getRoute(move.getRouteI());
        IRoute<?> insRoute = solution.getRoute(move.getInsertionRoute());

        int i = move.getI();
        int j = move.getJ();
        int ins = move.getInsertionIndex();

        INodeVisit nI = rI.getNodeAt(i);
        INodeVisit nJ = rI.getNodeAt(j);

        if (ins < 0 || ins > insRoute.length() // out of bounds
                // append but last node is fixed
                || (ins == insRoute.length() && insRoute.getLastNode().isFixed())
                // wrong node order
                || j < i || (insRoute == rI && i <= ins && ins <= j)) {
            return false;
        }
        // Accept insertion at a shrunk node only if the route is of length 3
        else if (insRoute.getNodeAt(ins) instanceof VRPShrunkRequest) {
            return insRoute.length() == 3;
        }
        // A shrunk node can be reinserted only on first position or on last
        // position
        else if (nI instanceof VRPShrunkRequest) {
            return (ins == 1 || (move.isStringReversed() && ins == insRoute.length() - 2))
                    && !nJ.isFixed();
        } else if (nJ instanceof VRPShrunkRequest) {
            return (ins == insRoute.length() - 2 || (move.isStringReversed() && ins == 1))
                    && !nI.isFixed();
        } else {
            return super.checkOrOpt(solution, move);
        }
    }

    @Override
    public boolean checkStrExchange(S solution, StringExchangeMove<?> mve) {
        return super.checkStrExchange(solution, mve);
    }

    @Override
    public boolean checkRelocAtomicMove(RelocateAtomicMove reloc) {
        return super.checkRelocAtomicMove(reloc);
    }

    @Override
    public boolean isFeasible(S solution) {
        for (VRPScenarioRoute route : solution) {
            if (route.containsShrunkNode()
                    && !(route.getNodeAt(1) instanceof VRPShrunkRequest || route.getNodeAt(route
                            .length() - 2) instanceof VRPShrunkRequest)) {
                return false;
            }
        }
        return super.isFeasible(solution);
    };

    @Override
    public String getInfeasibilityExplanation(S solution) {
        String infeas = super.getInfeasibilityExplanation(solution);

        String shrunk = null;
        for (VRPScenarioRoute route : solution) {
            if (route.containsShrunkNode()
                    && !(route.getNodeAt(1) instanceof VRPShrunkRequest || route.getNodeAt(route
                            .length() - 2) instanceof VRPShrunkRequest)) {
                shrunk = String.format("route %s does not start or end with shrunk node (%s)",
                        route.getID(), route);
                break;
            }
        }

        return infeas != null ? shrunk != null ? String.format("%s, %s", infeas, shrunk) : infeas
                : shrunk;
    };

}
