/**
 * 
 */
package vroom.common.heuristics.vrp;

import java.util.LinkedList;

import vroom.common.heuristics.Move;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.NodeInsertion;

/**
 * <code>RelocateMove</code>
 * <p>
 * Creation date: Sep 29, 2010 - 2:49:21 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RelocateMove extends Move {

    private final LinkedList<RelocateAtomicMove> mAtomicMoves;

    /** The parent solution for this move **/
    private final IVRPSolution<?>                mSolution;

    /**
     * Getter for The parent solution for this move
     * 
     * @return the value of solution
     */
    public IVRPSolution<?> getSolution() {
        return mSolution;
    }

    /**
     * List of atomic relocate moves
     * 
     * @return the list of relocate moves
     */
    public LinkedList<RelocateAtomicMove> getAtomicMoves() {
        return mAtomicMoves;
    }

    public RelocateMove(IVRPSolution<?> sol) {
        super(0);
        mSolution = sol;
        mAtomicMoves = new LinkedList<RelocateMove.RelocateAtomicMove>();
    }

    /**
     * Creates a new <code>RelocateAtomicMove</code>
     * 
     * @param node
     *            the node to be relocated
     * @param route
     *            its current route
     * @param nodeIndex
     *            its index within the route
     */
    public RelocateAtomicMove newAtomicMove(INodeVisit node, int route, int nodeIndex) {
        RelocateAtomicMove move = new RelocateAtomicMove(node, route, nodeIndex);
        return move;
    }

    /**
     * Add an atomic move to this move
     * 
     * @param reloc
     */
    public void addAtomicMove(RelocateAtomicMove reloc) {
        mAtomicMoves.add(reloc);
    }

    @Override
    public double getImprovement() {
        double cost = 0;
        for (RelocateAtomicMove r : mAtomicMoves) {
            cost += r.getInsertion() != null ? r.getInsertion().getCost() : 0;
        }
        setImprovement(-cost);
        return -cost;
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.Move#getMoveName()
     */
    @Override
    public String getMoveName() {
        return "reloc";
    }

    @Override
    public String toString() {
        return String.format("%s(%s,%.3f)", getMoveName(), getAtomicMoves(), getImprovement());
    }

    public class RelocateAtomicMove extends Move {
        private final INodeVisit mNode;
        private final int        mRoute;
        private NodeInsertion    mInsertion;

        /**
         * Creates a new <code>RelocateAtomicMove</code>
         * 
         * @param node
         *            the node to be relocated
         * @param route
         *            its current route
         * @param nodeIndex
         *            its index within the route
         */
        private RelocateAtomicMove(INodeVisit node, int route, int nodeIndex) {
            super(0);
            mNode = node;
            mRoute = route;
        }

        /**
         * Getter for <code>insertion</code>
         * 
         * @return the insertion
         */
        public NodeInsertion getInsertion() {
            return mInsertion;
        }

        /**
         * Setter for <code>insertion</code>
         * 
         * @param insertion
         *            the insertion to set
         */
        public void setInsertion(NodeInsertion insertion) {
            mInsertion = insertion;

            IRoute<?> rte = getSolution().getRoute(getRoute());
            int pos = rte.getNodePosition(getNode());
            double extractCost = 0;
            if (pos >= 0) {
                INodeVisit pred = pos > 0 ? rte.getNodeAt(pos - 1) : null;
                INodeVisit suc = pos < rte.length() - 1 ? rte.getNodeAt(pos + 1) : null;
                extractCost = -rte.getParentSolution()
                    .getParentInstance()
                    .getInsertionCost(getNode(), pred, suc, rte.getVehicle());
            }
            if (mInsertion != null) {
                setImprovement(-mInsertion.getCost() + extractCost);
            } else {
                setImprovement(0);
            }
        }

        /**
         * Getter for <code>node</code>
         * 
         * @return the node
         */
        public INodeVisit getNode() {
            return mNode;
        }

        /**
         * Getter for <code>route</code>
         * 
         * @return the route
         */
        public int getRoute() {
            return mRoute;
        }

        /**
         * The parent solution for this move
         * 
         * @return The parent solution for this move
         */
        public IVRPSolution<?> getSolution() {
            return RelocateMove.this.getSolution();
        }

        @Override
        public String getMoveName() {
            return "relocAt";
        }

        @Override
        public String toString() {
            return String.format("(%s[%s]->[%s])", getNode().getID(), getRoute(), getInsertion());
        }

    }

}
