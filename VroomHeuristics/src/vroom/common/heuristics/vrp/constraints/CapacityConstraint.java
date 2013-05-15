/**
 * 
 */
package vroom.common.heuristics.vrp.constraints;

import java.util.Iterator;
import java.util.List;

import vroom.common.heuristics.cw.kernel.RouteMergingMove;
import vroom.common.heuristics.vrp.OrOptMove;
import vroom.common.heuristics.vrp.RelocateMove;
import vroom.common.heuristics.vrp.RelocateMove.RelocateAtomicMove;
import vroom.common.heuristics.vrp.StringExchangeMove;
import vroom.common.heuristics.vrp.SwapMove;
import vroom.common.heuristics.vrp.TwoOptMove;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;

/**
 * <code>CapacityConstraint</code> is a constraint that ensures that each routes of a mSolution satisfy the capacity of its vehicle and that a given
 * move will not violate capacities.
 * <p>
 * Supported moves:
 * <ul>
 * <li>{@link TwoOptMove}</li>
 * <li>{@link SwapMove}</li>
 * <li>{@link OrOptMove}</li>
 * <li>{@link RouteMergingMove}</li>
 * </ul>
 * <p>
 * Creation date: Jun 22, 2010 - 9:56:51 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CapacityConstraint<S extends IVRPSolution<?>> implements IConstraint<S> {

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.IConstraint#checkMove(java.lang.Object, vroom.common.heuristics.Move)
     */
    @Override
    public boolean isFeasible(S solution, IMove move) {
        if (move instanceof TwoOptMove) {
            return check2Opt(solution, (TwoOptMove) move);
        } else if (move instanceof RouteMergingMove) {
            return checkRouteMerge(solution, (RouteMergingMove) move);
        } else if (move instanceof SwapMove) {
            return checkSwap(solution, (SwapMove) move);
        } else if (move instanceof OrOptMove<?>) {
            return checkOrOpt(solution, (OrOptMove<?>) move);
        } else if (move instanceof StringExchangeMove<?>) {
            return checkStringExchange(solution, (StringExchangeMove<?>) move);
        } else if (move instanceof RelocateMove) {
            for (RelocateAtomicMove reloc : ((RelocateMove) move).getAtomicMoves()) {
                if (!checkRelocAtomicMove(reloc)) {
                    return false;
                }
            }
            return true;
        } else if (move instanceof RelocateAtomicMove) {
            return checkRelocAtomicMove((RelocateAtomicMove) move);
        }
        return true;
    }

    public boolean checkRelocAtomicMove(RelocateAtomicMove move) {
        IRoute<?> rte = move.getInsertion().getRoute();

        double[] loads = new double[rte.getVehicle().getCompartmentCount()];
        for (int p = 0; p < loads.length; p++) {
            loads[p] += move.getNode().getDemand(p);
            if (loads[p] > rte.getVehicle().getCapacity(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Capacity check for a Or-opt move
     * 
     * @param mSolution
     * @param move
     * @return
     */
    public boolean checkOrOpt(S solution, OrOptMove<?> move) {
        if (move.getRouteI() == move.getInsertionRoute()) {
            // Assumes that the route is already feasible
            return true;
        }

        List<?> segment = solution.getRoute(move.getRouteI()).subroute(move.getI(), move.getJ());

        if (move.getInsertionRoute() < 0) {
            return false;
        }
        IRoute<?> dest = solution.getRoute(move.getInsertionRoute());

        double[] load = new double[dest.getVehicle().getCompartmentCount()];

        for (int p = 0; p < load.length; p++) {
            load[p] = dest.getLoad(p);
        }

        Iterator<INodeVisit> it = Utilities.castIterator(segment.iterator());
        while (it.hasNext()) {
            INodeVisit node = it.next();
            for (int p = 0; p < load.length; p++) {
                load[p] += node.getDemand(p);

                if (load[p] > dest.getVehicle().getCapacity(p)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean checkStringExchange(S solution, StringExchangeMove<?> move) {
        if (move.getFirstRoute() == move.getSecondRoute()) {
            // Assumes that the route is already feasible
            return true;
        }

        IRoute<?> r1 = solution.getRoute(move.getFirstRoute());
        IRoute<?> r2 = solution.getRoute(move.getSecondRoute());

        double[] s1Load = new double[r1.getVehicle().getCompartmentCount()];
        double[] s2Load = new double[r2.getVehicle().getCompartmentCount()];

        for (int i = move.getNodeI(); i <= move.getNodeJ(); i++) {
            for (int k = 0; k < s1Load.length; k++) {
                s1Load[k] += r1.getNodeAt(i).getDemand(k);
            }
        }

        for (int i = move.getNodeK(); i <= move.getNodeL(); i++) {
            for (int k = 0; k < s2Load.length; k++) {
                s2Load[k] += r2.getNodeAt(i).getDemand(k);
            }
        }

        for (int k = 0; k < s1Load.length; k++) {
            if (r1.getLoad(k) - s1Load[k] + s2Load[k] > r1.getVehicle().getCapacity(k)) {
                return false;
            }
            if (r2.getLoad(k) - s2Load[k] + s1Load[k] > r2.getVehicle().getCapacity(k)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Capacity check for a node swap
     * 
     * @param mSolution
     * @param move
     * @return <code>true</code> if the move is feasible
     */
    public boolean checkSwap(IVRPSolution<?> solution, SwapMove move) {
        if (move.getRouteI() == move.getRouteJ()) {
            // Assumes that the route is already feasible
            return true;
        }

        IRoute<?> rI = solution.getRoute(move.getRouteI());
        IRoute<?> rJ = solution.getRoute(move.getRouteJ());
        INodeVisit i = rI.getNodeAt(move.getI());
        INodeVisit j = rJ.getNodeAt(move.getJ());

        for (int p = 0; p < rI.getVehicle().getCompartmentCount(); p++) {
            if (rI.getLoad(p) - i.getDemand(p) + j.getDemand(p) > rI.getVehicle().getCapacity(p)) {
                return false;
            }
            if (rJ.getLoad(p) - j.getDemand(p) + i.getDemand(p) > rJ.getVehicle().getCapacity(p)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Capacity check for route merge move
     * 
     * @param mSolution
     * @param move
     * @return <code>true</code> if the move is feasible
     */
    public boolean checkRouteMerge(IVRPSolution<?> solution, RouteMergingMove move) {

        for (int p = 0; p < move.tailRoute.getVehicle().getCompartmentCount(); p++) {
            if (move.tailRoute.getLoad() + move.headRoute.getLoad() > move.tailRoute.getVehicle()
                .getCapacity()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Capacity check for 2-opt move
     * 
     * @param mSolution
     * @param move
     * @return <code>true</code> if the move is feasible
     */
    public boolean check2Opt(IVRPSolution<?> solution, TwoOptMove move) {
        if (move.getRouteI() == move.getRouteJ()) {
            // Assumes that the route is already feasible
            return true;
        }

        IRoute<?> rI = solution.getRoute((move).getRouteI());
        IRoute<?> rJ = solution.getRoute((move).getRouteJ());
        int i = (move).getI();
        int j = (move).getJ();

        double[] startI = new double[rI.getVehicle().getCompartmentCount()];
        double[] endI = new double[startI.length];
        double[] startJ = new double[startI.length];
        double[] endJ = new double[startI.length];

        for (int p = 0; p < startI.length; p++) {
            int node = 0;
            Iterator<? extends INodeVisit> it = rI.iterator();
            // Accumulated load of the first route up to nodeI
            while (node <= i && it.hasNext()) {
                startI[p] += it.next().getDemand(p);
                node++;
            }
            // Accumulated load of the first route from nodeI+1 to depot
            while (it.hasNext()) {
                endI[p] += it.next().getDemand(p);
                node++;
            }

            node = 0;
            it = rJ.iterator();
            // Accumulated load of the second route up to nodeJ
            while (node <= j && it.hasNext()) {
                startJ[p] += it.next().getDemand(p);
                node++;
            }
            // Accumulated load of the second route from to nodeJ+1 to depot
            while (it.hasNext()) {
                endJ[p] += it.next().getDemand(p);
                node++;
            }
        }

        // Capacity check
        // standard 2-opt
        // nodeI linked with nodeJ
        // nodeI+1 linked with nodeJ+1
        if (!(move).isStar()) {
            // Route I : startI + startJ
            // Route J : endI + endJ
            for (int p = 0; p < startI.length; p++) {
                if (startI[p] + startJ[p] > rI.getVehicle().getCapacity(p)
                        || endI[p] + endJ[p] > rJ.getVehicle().getCapacity(p)) {
                    return false;
                }
            }
        }
        // special 2-opt*
        // nodeI linked with nodeJ+1
        // nodeI+1 linked with nodeJ
        else {
            // Route I : startI + endJ
            // Route J : startJ + endI
            for (int p = 0; p < startI.length; p++) {
                if (startI[p] + endJ[p] > rI.getVehicle().getCapacity(p)
                        || startJ[p] + endI[p] > rJ.getVehicle().getCapacity(p)) {
                    return false;
                }
            }

        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.IConstraint#checkSolution(java.lang.Object)
     */
    @Override
    public boolean isFeasible(S solution) {
        double[] load;

        for (IRoute<?> route : solution) {
            load = new double[route.getVehicle().getCompartmentCount()];

            for (int p = 0; p < load.length; p++) {
                for (INodeVisit n : route) {
                    load[p] += n.getDemand(p);

                    if (load[p] > route.getVehicle().getCapacity(p)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.IConstraint#getInfeasibilityExplanation(java. lang.Object)
     */
    @Override
    public String getInfeasibilityExplanation(S solution) {

        double[] load;
        StringBuilder sb = new StringBuilder();

        for (IRoute<?> route : solution) {
            load = new double[route.getVehicle().getCompartmentCount()];

            boolean stop = false;
            for (INodeVisit n : route) {
                for (int p = 0; p < load.length; p++) {
                    load[p] += n.getDemand(p);

                    if (load[p] > route.getVehicle().getCapacity(p)) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(String.format(
                            "Capacity Ctr: route:%s product:%s load:%s (cap:%s, node:%s)", route.hashCode(),
                            p, load[p], route.getVehicle().getCapacity(p), n.getID()));
                        break;
                    }
                }
                if (stop) {
                    break;
                }

            }
        }

        return sb.length() > 0 ? sb.toString() : null;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.IConstraint#getInfeasibilityExplanation(java. lang.Object, vroom.common.heuristics.Move)
     */
    @Override
    public String getInfeasibilityExplanation(S solution, IMove move) {
        // StringBuilder sb = new StringBuilder();
        //
        // return sb.length()>0?sb.toString():null;

        if (!isFeasible(solution, move)) {
            return String.format("move:%s violates capacity constraint", move);
        } else {
            return null;
        }
    }

}
