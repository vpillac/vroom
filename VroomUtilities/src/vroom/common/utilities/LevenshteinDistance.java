package vroom.common.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vroom.common.utilities.Utilities.Math;

/**
 * The class <code>LevenshteinDistance</code> contains the lazy evaluated matrix used to calculate the Levenshtein
 * distance with a time/space complexity of {@code  O(n.d)} where {@code  d} is the distance between the two seqs, and
 * {@code  n} is the length of the longest seq.
 * <p>
 * Creation date: Nov 29, 2011 - 2:52:33 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class LevenshteinDistance<T> {
    private final Object[][]   mMatrix;
    private final ArrayList<T> mRef;
    private final ArrayList<T> mSeq;

    /**
     * Creates a new <code>LevenshteinDistance</code>
     * 
     * @param ref
     * @param seq
     */
    private LevenshteinDistance(List<T> ref, List<T> seq) {
        mRef = new ArrayList<T>(ref.size() + 1);
        mRef.add(null);
        for (T n : ref)
            mRef.add(n);
        mSeq = new ArrayList<T>(seq.size() + 1);
        mSeq.add(null);
        for (T n : seq)
            mSeq.add(n);
        mMatrix = new Object[mRef.size()][mSeq.size()];
    }

    /**
     * Returns the distance between {@code  ref} and {@code  seq}
     * 
     * @param ref
     * @param seq
     * @return the distance between {@code  ref} and {@code  seq}
     */
    public static <T> int getDistance(List<T> ref, List<T> seq) {
        LevenshteinDistance<T> dist = new LevenshteinDistance<T>(ref, seq);
        return dist.getDistance();
    }

    /**
     * Returns a minimum length edit sequence to transform {@code  ref} into {@code  seq}, using lazy evaluation.
     * <p>
     * Time complexity of {@code  O(n(1+d))} (where {@code d} is the distance) and space complexity of {@code  O(n)}
     * </p>
     * 
     * @param ref
     * @param seq
     * @return a minimum length edit sequence to transform {@code  ref} into {@code  seq}
     */
    public static <T> List<Edit<T>> getEditSequence(List<T> ref, List<T> seq) {
        LevenshteinDistance<T> dist = new LevenshteinDistance<T>(ref, seq);
        return dist.getEditSequence();
    }

    /**
     * Returns the <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein distance</a> between two
     * sequences of objects, using the classic version of the dynamic programming algorithm.
     * <p>
     * Time complexity of {@code  O(n²)} and space complexity of {@code  O(n)}
     * </p>
     * 
     * @param s
     *            the first sequence
     * @param t
     *            the second sequence
     * @param directed
     *            {@code true} if {@code  s} is the reference and {@code  t} the evaluated sequence, changes the outputs
     *            of the number of deletions, insertions and substitutions. Set to {@code false} if only the distance is
     *            required
     * @return an array containing: [distance, deletions, insertions, substitutions]
     */
    public static <T> int[] getDistanceClassic(List<T> s, List<T> t, boolean directed) {
        if (!directed && s.size() < t.size()) {
            List<T> o = t;
            t = s;
            s = o;
        }

        // int[][] d = new int[sSize + 1][tSize + 1];
        // distance, deletions, insertions, substitutions
        int[][][] d = new int[2][t.size() + 1][4];

        for (int i = 0; i < d.length; i++)
            d[i][0][0] = i; // Distance between any string and an empty string
        for (int j = 0; j < d[0].length; j++)
            d[0][j][0] = j; // Distance between an empty string and any string

        int iRow = 1, predIRow = 0;
        Iterator<T> sIt = s.iterator();
        int i = 1;
        while (sIt.hasNext()) {
            Object sNext = sIt.next();
            d[iRow][0][0] = i;
            int j = 1;
            Iterator<T> tIt = t.iterator();
            while (tIt.hasNext()) {
                Object tNext = tIt.next();

                if (Utilities.equal(tNext, sNext)) {
                    d[iRow][j][0] = d[predIRow][j - 1][0];
                    d[iRow][j][1] = d[predIRow][j - 1][1];
                    d[iRow][j][2] = d[predIRow][j - 1][2];
                    d[iRow][j][3] = d[predIRow][j - 1][3];
                } else {
                    int[][] pred = new int[][] { //
                    d[predIRow][j], // a deletion
                            d[iRow][j - 1], // an insertion
                            d[predIRow][j - 1] // a substitution
                    };
                    int[] argMin = Math.argMin(//
                            new int[] { pred[0][0], pred[1][0], pred[2][0] });

                    // Update the distance
                    d[iRow][j][0] = argMin[1] + 1;
                    // Update the count
                    // Copy count from selected pred
                    d[iRow][j][1] = pred[argMin[0]][1];
                    d[iRow][j][2] = pred[argMin[0]][2];
                    d[iRow][j][3] = pred[argMin[0]][3];
                    // Increment count for detected move
                    d[iRow][j][argMin[0] + 1]++;
                }
                j++;
            }
            predIRow = iRow;
            iRow = 1 - iRow;
            i++;
        }

        return d[predIRow][d[predIRow].length - 1];
    }

    /**
     * Returns the distance between the reference and the seq
     * 
     * @return the distance between the reference and the seq
     */
    private int getDistance() {
        return getSoutEastCell().value();
    }

    /**
     * Return a minimum length edit sequence to go from the the reference seq to the evaluated seq
     * 
     * @return a minimum length edit sequence to go from the the reference seq to the evaluated seq
     */
    private List<Edit<T>> getEditSequence() {
        ArrayList<Edit<T>> edits = new ArrayList<Edit<T>>(getDistance());
        Cell cell = getSoutEastCell();
        while (cell.getPred() != null) {
            if (cell.getEdit().getType() != LevenshteinDistance.EditType.NONE)
                edits.add(cell.getEdit());
            cell = cell.getPred();
        }
        return edits;
    }

    /**
     * Returns the cell at position {@code  [i,j]}
     * 
     * @param i
     * @param j
     * @return the cell at position {@code  [i,j]}
     */
    @SuppressWarnings("unchecked")
    private Cell getCell(int i, int j) {
        if (mMatrix[i][j] == null)
            mMatrix[i][j] = new Cell(i, j);
        return (Cell) mMatrix[i][j];
    }

    /**
     * Returns the south east cell
     * 
     * @return the south east cell
     */
    private Cell getSoutEastCell() {
        return getCell(mRef.size() - 1, mSeq.size() - 1);
    }

    /**
     * <code>Cell</code> represents a cell of the {@link LevenshteinDistance}, it contains a lazy evaluated value, and a
     * reference to its predecessor and the move to go from the predecessor to this cell
     */
    protected class Cell {
        final int                    i, j;
        int                          mEval;
        Cell                         mPred;
        LevenshteinDistance.EditType mType;
        LevenshteinDistance.Edit<T>  mEdit;

        /**
         * Creates a new <code>Cell</code>
         * 
         * @param i
         *            the index in the reference seq
         * @param j
         *            the index in the evaluated seq
         */
        public Cell(int i, int j) {
            this.i = i;
            this.j = j;
            mEval = -1;
            mPred = null;

            if (i == 0 && j == 0) {
                mEval = 0;
                mType = LevenshteinDistance.EditType.NONE;
            } else if (i == 0) {
                mEval = j;
                mPred = getCell(i, j - 1);
                mType = LevenshteinDistance.EditType.INS;
            } else if (j == 0) {
                mEval = i;
                mPred = getCell(i - 1, j);
                mType = LevenshteinDistance.EditType.DEL;
            }
        }

        /**
         * Returns this cell predecessor
         * 
         * @return this cell predecessor
         */
        private Cell getPred() {
            return mPred;
        }

        /**
         * Lazy evaluation of this cell
         * 
         * @return the value of this cell
         */
        private int value() {
            if (mEval == -1) {
                evaluate();
            }
            return mEval;
        }

        /**
         * Evaluate this cell
         */
        private void evaluate() {
            // NorthWest cell
            Cell nw = getCell(i - 1, j - 1);

            if (Utilities.equal(mRef.get(i), mSeq.get(j))) {
                mPred = nw;
                mType = LevenshteinDistance.EditType.NONE;
            } else {
                // West cell (insertion)
                Cell w = getCell(i, j - 1);
                if (w.value() < nw.value()) {
                    // Optimization: we now that w<=n in this case (see Lloyd page)
                    mPred = w;
                    mType = LevenshteinDistance.EditType.INS;
                } else {
                    // North cell (deletion)
                    Cell n = getCell(i - 1, j);
                    if (nw.value() <= n.value()) {
                        mPred = nw;
                        mType = LevenshteinDistance.EditType.SUB;
                    } else {
                        mPred = n;
                        mType = LevenshteinDistance.EditType.DEL;
                    }
                }
            }
            // Update the evaluation
            mEval = mPred.value() + mType.getCost();
        }

        /**
         * Return the edit required to move from this cell predecessor to this cell
         * 
         * @return the edit required to move from this cell predecessor to this cell
         */
        private LevenshteinDistance.Edit<T> getEdit() {
            if (mEdit == null) {
                if (mType == LevenshteinDistance.EditType.SUB
                        || mType == LevenshteinDistance.EditType.INS)
                    mEdit = new LevenshteinDistance.Edit<T>(mType, i, mRef.get(i), mSeq.get(j));
                else
                    mEdit = new LevenshteinDistance.Edit<T>(mType, i, mRef.get(i), mRef.get(i));
            }
            return mEdit;
        }
    }

    public static class Edit<T> {
        private final LevenshteinDistance.EditType mType;
        private final int                          mEditIndex;
        private final T                            mEditedElement;
        private final T                            mNewElement;

        /**
         * Getter for <code>type</code>
         * 
         * @return the type
         */
        public LevenshteinDistance.EditType getType() {
            return mType;
        }

        /**
         * Getter for the index at which the edit occurred in the reference sequence: the deleted element for
         * {@link EditType#DEL}, the element that was replaced for {@link EditType#SUB}, and the element after which an
         * element was inserted for {@link EditType#INS}.
         * 
         * @return the index at which the edit occurred in the reference sequence
         */
        public int getEditIndex() {
            return mEditIndex;
        }

        /**
         * Getter for the edited element in the reference sequence: the deleted element for {@link EditType#DEL}, the
         * element that was replaced for {@link EditType#SUB}, and the element after which an element was inserted for
         * {@link EditType#INS}.
         * 
         * @return the edited element in the reference sequence
         */
        public T getEditedElement() {
            return mEditedElement;
        }

        /**
         * Getter for the new element: the deleted element for {@link EditType#DEL}, the new element for
         * {@link EditType#SUB}, and the inserted element for {@link EditType#INS}.
         * 
         * @return the new element
         */
        public T getNewElement() {
            return mNewElement;
        }

        /**
         * Creates a new <code>Edit</code>
         * 
         * @param type
         *            the type of edit
         * @param editIndex
         *            the index at which the edit was made
         * @param editedElement
         *            the element that was edited
         * @param newElement
         *            the that was removed, inserted, or replaced a previous element
         */
        public Edit(LevenshteinDistance.EditType type, int editIndex, T editedElement, T newElement) {
            mType = type;
            mEditIndex = editIndex;
            mEditedElement = editedElement;
            mNewElement = newElement;
        }

        @Override
        public String toString() {
            if (mNewElement == null)
                return String.format("%s[%s@%s]", mType, mEditedElement, mEditIndex);
            else
                return String
                        .format("%s[%s@%s:%s]", mType, mEditedElement, mEditIndex, mNewElement);
        }
    }

    /**
     * The enum <code>EditType</code> represent the different edits measured by the Levenshtein distance.
     * <p>
     * Creation date: Nov 29, 2011 - 2:54:32 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static enum EditType {
        NONE(0), DEL(1), SUB(1), INS(1);
        final int mCost;

        /**
         * Returns the cost of this edit
         * 
         * @return the cost of this edit
         */
        public int getCost() {
            return mCost;
        }

        private EditType(int cost) {
            mCost = cost;
        }

    }
}