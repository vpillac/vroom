/**
 * 
 */
package vroom.trsp.optimization.biobj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import vroom.common.heuristics.Move;
import vroom.common.utilities.LevenshteinDistance;
import vroom.common.utilities.LevenshteinDistance.Edit;
import vroom.common.utilities.LevenshteinDistance.EditType;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.IPathRelinking;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * The class <code>LevenshteinPR</code> contains an implementation of Path relinking that used the sequence of edits
 * found while evaluating the Levenshtein distance.
 * <p>
 * Creation date: Nov 29, 2011 - 4:45:55 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class LevenshteinPR implements IPathRelinking<TRSPSolution> {

    private final TRSPCostDelegate mCostDelegate;

    public LevenshteinPR(TRSPGlobalParameters params) {
        mCostDelegate = params.newALNSCostDelegate(null);
    }

    @Override
    public List<TRSPSolution> pathRelinking(TRSPSolution start, TRSPSolution target,
            IParameters params) {
        List<PRMove> moveList = decomposeInMoves(start, target);

        // Evaluate moves
        for (PRMove prMove : moveList) {
            mCostDelegate.evaluateMove(prMove);
        }
        // Sort moves
        // Collections.sort(moveList, Collections.reverseOrder());

        // Execute moves
        ArrayList<TRSPSolution> path = new ArrayList<TRSPSolution>(moveList.size() + 1);
        path.add(start);

        if (moveList.size() == 1) {
            path.add(target);
            return path;
        }

        for (PRMove prMove : moveList) {
            TRSPLogging.getOptimizationLogger().debug("LevenshteinPR.pathRelinking: PR Move %s",
                    prMove);
            // Clone the last solution in the path
            TRSPSolution sol = path.get(path.size() - 1).clone();
            path.add(sol);

            for (TRSPTour tour : sol)
                tour.setAutoUpdated(false);

            // Execute the move on the solution
            for (AtomicPRMove mve : prMove.getMoves())
                executeMove(sol, mve);

            for (TRSPTour tour : sol)
                tour.setAutoUpdated(true);
            String err = TRSPSolutionChecker.INSTANCE.checkSolution(sol);
            if (err.contains("Unserved requests)") || err.contains("served twice"))
                TRSPLogging.getOptimizationLogger().info(
                        "LevenshteinPR.pathRelinking: generated an invalid solution: %s (%s)",
                        prMove, err);

        }

        return path;
    }

    /**
     * Execute an {@linkplain AtomicPRMove atomic move}
     * 
     * @param solution
     *            the solution to be modified
     * @param move
     *            the move to execute
     */
    protected void executeMove(TRSPSolution solution, AtomicPRMove move) {
        if (move.isExecuted())
            return;

        TRSPTour tour = solution.getTour(move.getTour().getTechnicianId());

        checkTour(tour);
        switch (move.getType()) {
        case INS:
            for (Integer node : move.getInsertedNodes())
                tour.insertAfter(move.getEditedReq(), node);
            break;
        case DEL:
            tour.removeNode(move.getEditedReq());
            break;
        case SUB:
            // FIXME Add a special case when both nodes are on the same tour
            tour.setNode(move.getEditedReq(), move.getNewReq());
            break;
        default:
            throw new UnsupportedOperationException("Unsupported edit type :" + move.getType());
        }

        checkTour(tour);

        move.setExecuted();
    }

    private void checkTour(TRSPTour tour) {
        int last = 0;
        for (int n : tour)
            last = n;

        if (!tour.getSolution().getInstance().isDepot(last) || last != tour.getLastNode())
            throw new IllegalStateException();
    }

    /**
     * Decompose the differences between two solutions (in the sense of the Levenshtein distance) into a sequence of
     * independent {@link PRMove}
     * 
     * @param start
     *            the starting point
     * @param target
     *            the target solution
     * @return a sequence of independent {@link PRMove}
     */
    @SuppressWarnings("unchecked")
    public List<PRMove> decomposeInMoves(TRSPSolution start, TRSPSolution target) {
        List<?>[] edits = new List<?>[start.getInstance().getMaxId()];
        int mveCount = 0;
        for (int techId = 0; techId < start.getTourCount(); techId++) {
            TRSPTour s = start.getTour(techId);
            TRSPTour t = target.getTour(techId);
            List<Edit<Integer>> editSequence = LevenshteinDistance.getEditSequence(s.asList(),
                    t.asList());

            ListIterator<Edit<Integer>> it = editSequence.listIterator();
            while (it.hasNext()) {
                Edit<Integer> e = it.next();

                // Special case for insert: check for sequential insertions
                List<Integer> newNodes = null;
                if (e.getType() == EditType.INS) {
                    newNodes = new ArrayList<Integer>(3);
                    int idx = e.getEditIndex();
                    newNodes.add(e.getNewElement());
                    Edit<Integer> next = null;
                    // Step forward while we are on an sequence insertion
                    boolean insSeq = true;
                    while (it.hasNext() && insSeq) {
                        next = it.next();
                        insSeq = next.getType() == EditType.INS && next.getEditIndex() == idx;
                        if (insSeq)
                            newNodes.add(next.getNewElement());
                    }
                    // Step back if insertion sequence finished
                    if (!insSeq)
                        it.previous();
                    // Reverse the insertion order
                    // Collections.reverse(newNodes);
                } else {
                    newNodes = Collections.singletonList(e.getNewElement());
                }

                AtomicPRMove mve = new AtomicPRMove(e.getType(), s, e.getEditedElement(), newNodes);
                for (Integer n : newNodes)
                    getEditList(edits, n).add(mve);
                if (mve.getType() == EditType.SUB)
                    getEditList(edits, e.getEditedElement()).add(mve);
                mveCount++;
            }
        }

        List<PRMove> moveList = new ArrayList<PRMove>(mveCount / 2);
        int lastIdx = -1;
        while (lastIdx < edits.length) {
            lastIdx++;
            LinkedList<Integer> relatedIds = new LinkedList<Integer>();
            // Find the next atomic move to process
            for (int i = lastIdx; i < edits.length; i++) {
                lastIdx = i;
                if (edits[i] != null && !edits[i].isEmpty()) {
                    relatedIds.add(i);
                    break;
                }
            }

            // Build the complete move
            PRMove mve = new PRMove();
            while (!relatedIds.isEmpty()) {
                int i = relatedIds.pop();

                if (edits[i] != null && !edits[i].isEmpty()) {
                    for (AtomicPRMove atMve : (Iterable<AtomicPRMove>) edits[i]) {
                        relatedIds.addAll(affectedIds(atMve));
                        mve.getMoves().add(atMve);
                    }
                    edits[i] = null;
                }
            }
            if (!mve.getMoves().isEmpty()) {
                mve.sort();
                moveList.add(mve);
            }
        }
        return moveList;
    }

    /**
     * Get the id of the requests affected by a move
     * 
     * @param move
     * @return
     */
    private Collection<Integer> affectedIds(AtomicPRMove move) {
        switch (move.getType()) {
        case INS:
            return move.getInsertedNodes();
        case SUB:
            return Arrays.asList(move.getEditedReq(), move.getNewReq());
        case DEL:
            return Collections.singleton(move.getEditedReq());
        default:
            throw new UnsupportedOperationException("Unsupported edit type: " + move.getType());
        }
    }

    @SuppressWarnings("unchecked")
    private List<AtomicPRMove> getEditList(List<?>[] edits, int id) {
        if (edits[id] == null)
            edits[id] = new ArrayList<AtomicPRMove>(2);
        return (List<AtomicPRMove>) edits[id];
    }

    /**
     * The class <code>PRMove</code> contains a list of {@link AtomicPRMove}, it represent a sequence of edits that will
     * lead to a valid (but not necessarily feasible) solution.
     * <p>
     * In other words it describes a neighbor of the start solution
     * </p>
     * <p>
     * Creation date: Nov 30, 2011 - 10:46:35 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class PRMove extends Move {
        private final LinkedList<AtomicPRMove> mMoves;

        protected PRMove() {
            mMoves = new LinkedList<AtomicPRMove>();
        }

        /**
         * Returns the actual list of atomic moves
         * 
         * @return the actual list of atomic moves
         */
        private LinkedList<AtomicPRMove> getMoves() {
            return mMoves;
        }

        /**
         * Returns a view of the atomic moves composing this PR move
         * 
         * @return a view of the atomic moves composing this PR move
         */
        public List<AtomicPRMove> getAtomicMoves() {
            return Collections.unmodifiableList(mMoves);
        }

        /**
         * Sort the atomic moves composing this PR move
         */
        public void sort() {
            Collections.sort(mMoves, new Comparator<AtomicPRMove>() {
                @Override
                public int compare(AtomicPRMove o1, AtomicPRMove o2) {
                    if (o1.getTour().getTechnicianId() != o2.getTour().getTechnicianId()) {
                        return 10000 * Integer.compare(o1.getTour().getTechnicianId(), o2.getTour()
                                .getTechnicianId());
                    } else if (o1.getType() != o2.getType()) {
                        return 100 * o1.getType().compareTo(o2.getType());
                    } else if (o1.getType() == EditType.SUB) {
                        if (o1.getTour().isVisited(o1.getNewReq()))
                            return 10;
                        else
                            return -10;
                    } else {
                        return Integer.compare(o1.getEditedReq(), o2.getEditedReq());
                    }
                };
            });
        }

        @Override
        public String toString() {
            return String.format("PR(imp:%.3f a:%s)", getImprovement(),
                    Utilities.toShortString(mMoves));
        }

        @Override
        public String getMoveName() {
            return "PR";
        }

    }

    /**
     * The class <code>AtomicPRMove</code> represent an atomic PR move, or in other words an edit in the Levenshtein
     * distance
     * <p>
     * Creation date: Nov 30, 2011 - 10:44:07 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class AtomicPRMove {
        private final EditType      mType;
        private final TRSPTour      mTour;
        private final Integer       mEditedReq;
        private final List<Integer> mNewReqs;

        private boolean             mExecuted = false;

        /**
         * Creates a new <code>AtomicPRMove</code>
         * 
         * @param type
         *            the type of move
         * @param tour
         *            the affected tour
         * @param editedReq
         *            the affected request
         * @param newReqs
         *            the new requests
         */
        protected AtomicPRMove(EditType type, TRSPTour tour, Integer editedReq, Integer newReq) {
            this(type, tour, editedReq, Collections.singletonList(newReq));
        }

        /**
         * Creates a new <code>AtomicPRMove</code>
         * 
         * @param type
         *            the type of move
         * @param tour
         *            the affected tour
         * @param editedReq
         *            the affected request
         * @param newReqs
         *            the inserted requests
         */
        protected AtomicPRMove(EditType type, TRSPTour tour, Integer editedReq, List<Integer> insReq) {
            mType = type;
            mEditedReq = editedReq;
            mNewReqs = insReq;
            mTour = tour;
        }

        /**
         * Getter for <code>type</code>
         * 
         * @return the type
         */
        public EditType getType() {
            return mType;
        }

        /**
         * Getter for the tour to which this move applies
         * 
         * @return the tour
         */
        public TRSPTour getTour() {
            return mTour;
        }

        /**
         * Getter for the edited request in the reference sequence: the deleted request for {@link EditType#DEL}, the
         * request that was replaced for {@link EditType#SUB}, and the request after which an element was inserted for
         * {@link EditType#INS}.
         * 
         * @return the edited request in the reference sequence
         */
        public Integer getEditedReq() {
            return mEditedReq;
        }

        /**
         * Getter for the substituted request for {@link EditType#SUB}
         * 
         * @return the substituted request
         */
        public Integer getNewReq() {
            return mNewReqs.get(0);
        }

        /**
         * Getter for the inserted requests for {@link EditType#INS}.
         * 
         * @return the inserted request
         */
        public List<Integer> getInsertedNodes() {
            return mNewReqs;
        }

        /**
         * Getter for <code>executed</code>
         * 
         * @return the executed
         */
        public boolean isExecuted() {
            return mExecuted;
        }

        /**
         * Setter for <code>executed</code>
         * 
         * @param executed
         *            the executed to set
         */
        public void setExecuted() {
            mExecuted = true;
        }

        @Override
        public String toString() {
            return String.format("%s[%s:%s/%s]", getType(), getTour().getTechnicianId(),
                    getEditedReq(), getInsertedNodes().size() > 1 ? getInsertedNodes()
                            : getNewReq());
        }

    }

}
