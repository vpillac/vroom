/**
 * 
 */
package vroom.common.utilities.algorithms;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <code>HungarianAlgorithm</code> is a simple implementation of the <a
 * href="http://en.wikipedia.org/wiki/Hungarian_algorithm">Hungarian algorithm</a>
 * <p>
 * This class is largely based on the implementation by <a
 * href="http://sites.google.com/site/garybaker/hungarian-algorithm/assignment">Gary Baker</a> and distributed under the
 * GPL 3 license.
 * </p>
 * Creation date: Mar 23, 2011 - 3:44:19 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class HungarianAlgorithm {

    /**
     * Solve the assignment problem defined by the penalty matrix.
     * 
     * @param penalties
     *            a squared matrix containing the assignment penalties.
     * @return and array containing the assignments. If <code>penalties</code> is indexed by <code>[worker,task]</code>
     *         then the result is given in the form <code>array[job]=task</code>
     */
    public static int[] solveAssignment(double[][] penalties) {
        // double[][] clone = new double[penalties.length][];
        // for (int i = 0; i < clone.length; i++) {
        // clone[i] = Arrays.copyOf(penalties[i], penalties[i].length);
        // }
        // penalties = clone;

        // subtract minimum value from rows and columns to create lots of zeroes
        reduceMatrix(penalties);

        // non negative values are the index of the starred or primed zero in the row or column
        int[] starsByRow = new int[penalties.length];
        Arrays.fill(starsByRow, -1);
        int[] starsByCol = new int[penalties[0].length];
        Arrays.fill(starsByCol, -1);
        int[] primesByRow = new int[penalties.length];
        Arrays.fill(primesByRow, -1);

        // 1s mean covered, 0s mean not covered
        boolean[] coveredRows = new boolean[penalties.length];
        boolean[] coveredCols = new boolean[penalties[0].length];

        // star any zero that has no other starred zero in the same row or column
        initStars(penalties, starsByRow, starsByCol);
        coverColumnsOfStarredZeroes(starsByCol, coveredCols);

        boolean abort = false;

        while (!allAreCovered(coveredCols) && !abort) {

            int[] primedZero = primeSomeUncoveredZero(penalties, primesByRow, coveredRows, coveredCols);

            while (primedZero == null && !abort) {
                // keep making more zeroes until we find something that we can prime (i.e. a zero that is uncovered)
                abort = !makeMoreZeroes(penalties, coveredRows, coveredCols);
                if (abort)
                    break;
                primedZero = primeSomeUncoveredZero(penalties, primesByRow, coveredRows, coveredCols);
            }

            if (abort)
                break;
            // check if there is a starred zero in the primed zero's row
            int columnIndex = starsByRow[primedZero[0]];
            if (-1 == columnIndex) {

                // if not, then we need to increment the zeroes and start over
                incrementSetOfStarredZeroes(primedZero, starsByRow, starsByCol, primesByRow);
                Arrays.fill(primesByRow, -1);
                Arrays.fill(coveredRows, false);
                Arrays.fill(coveredCols, false);
                coverColumnsOfStarredZeroes(starsByCol, coveredCols);
            } else {
                // cover the row of the primed zero and uncover the column of the starred zero in the same row
                coveredRows[primedZero[0]] = true;
                coveredCols[columnIndex] = false;
            }
        }

        // ok now we should have assigned everything
        // take the starred zeroes in each column as the correct assignments

        int[] retval = new int[penalties.length];
        for (int i = 0; i < starsByCol.length; i++) {
            retval[starsByCol[i]] = i;
        }
        return retval;

    }

    private static boolean allAreCovered(boolean[] coveredCols) {
        for (boolean covered : coveredCols) {
            if (!covered)
                return false;
        }
        return true;
    }

    /**
     * the first step of the hungarian algorithm is to find the smallest element in each row and subtract it's values
     * from all elements in that row
     * 
     * @return the next step to perform
     */
    private static void reduceMatrix(double[][] matrix) {

        for (int i = 0; i < matrix.length; i++) {
            // find the min value in the row
            double minValInRow = Double.POSITIVE_INFINITY;
            for (int j = 0; j < matrix[i].length; j++) {
                if (minValInRow > matrix[i][j]) {
                    minValInRow = matrix[i][j];
                }
            }

            // subtract it from all values in the row
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] -= minValInRow;
            }
        }

        for (int i = 0; i < matrix[0].length; i++) {
            // Find the min in all cols
            double minValInCol = Double.POSITIVE_INFINITY;
            for (int j = 0; j < matrix.length; j++) {
                if (minValInCol > matrix[j][i]) {
                    minValInCol = matrix[j][i];
                }
            }
            // Substract it to all values in col
            for (int j = 0; j < matrix.length; j++) {
                matrix[j][i] -= minValInCol;
            }

        }

    }

    /**
     * init starred zeroes for each column find the first zero if there is no other starred zero in that row then star
     * the zero, cover the column and row and go onto the next column
     * 
     * @param costMatrix
     * @param starredZeroes
     * @param coveredRows
     * @param coveredCols
     * @return the next step to perform
     */
    private static void initStars(double costMatrix[][], int[] starsByRow, int[] starsByCol) {

        boolean[] rowHasStarredZero = new boolean[costMatrix.length];
        boolean[] colHasStarredZero = new boolean[costMatrix[0].length];

        for (int i = 0; i < costMatrix.length; i++) {
            for (int j = 0; j < costMatrix[i].length; j++) {
                if (0 == costMatrix[i][j] && !rowHasStarredZero[i] && !colHasStarredZero[j]) {
                    starsByRow[i] = j;
                    starsByCol[j] = i;
                    rowHasStarredZero[i] = true;
                    colHasStarredZero[j] = true;
                    break; // move onto the next row
                }
            }
        }
    }

    /**
     * just mark the columns covered for any column containing a starred zero
     * 
     * @param starsByCol
     * @param coveredCols
     */
    private static void coverColumnsOfStarredZeroes(int[] starsByCol, boolean[] coveredCols) {
        for (int i = 0; i < starsByCol.length; i++) {
            coveredCols[i] = -1 == starsByCol[i] ? false : true;
        }
    }

    /**
     * finds some uncovered zero and primes it
     * 
     * @param matrix
     * @param primesByRow
     * @param coveredRows
     * @param coveredCols
     * @return
     */
    private static int[] primeSomeUncoveredZero(double matrix[][], int[] primesByRow, boolean[] coveredRows,
            boolean[] coveredCols) {

        // find an uncovered zero and prime it
        for (int i = 0; i < matrix.length; i++) {
            if (coveredRows[i])
                continue;
            for (int j = 0; j < matrix[i].length; j++) {
                // if it's a zero and the column is not covered
                if (0 == matrix[i][j] && !coveredCols[j]) {

                    // ok this is an unstarred zero
                    // prime it
                    primesByRow[i] = j;
                    return new int[] { i, j };
                }
            }
        }
        return null;

    }

    /**
     * @param unpairedZeroPrime
     * @param starsByRow
     * @param starsByCol
     * @param primesByRow
     */
    private static void incrementSetOfStarredZeroes(int[] unpairedZeroPrime, int[] starsByRow, int[] starsByCol,
            int[] primesByRow) {

        // build the alternating zero sequence (prime, star, prime, star, etc)
        int i, j = unpairedZeroPrime[1];

        Set<int[]> zeroSequence = new LinkedHashSet<int[]>();
        zeroSequence.add(unpairedZeroPrime);
        boolean paired = false;
        do {
            i = starsByCol[j];
            paired = -1 != i && zeroSequence.add(new int[] { i, j });
            if (!paired)
                break;

            j = primesByRow[i];
            paired = -1 != j && zeroSequence.add(new int[] { i, j });

        } while (paired);

        // unstar each starred zero of the sequence
        // and star each primed zero of the sequence
        for (int[] zero : zeroSequence) {
            if (starsByCol[zero[1]] == zero[0]) {
                starsByCol[zero[1]] = -1;
                starsByRow[zero[0]] = -1;
            }
            if (primesByRow[zero[0]] == zero[1]) {
                starsByRow[zero[0]] = zero[1];
                starsByCol[zero[1]] = zero[0];
            }
        }

    }

    /**
     * @param matrix
     * @param coveredRows
     * @param coveredCols
     * @return <code>true</code> if at least one additional zero appeared, <code>false</code> otherwise
     */
    private static boolean makeMoreZeroes(double[][] matrix, boolean[] coveredRows, boolean[] coveredCols) {

        // find the minimum uncovered value
        double minUncoveredValue = Double.POSITIVE_INFINITY;
        for (int i = 0; i < matrix.length; i++) {
            if (!coveredRows[i]) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (!coveredCols[j] && matrix[i][j] < minUncoveredValue) {
                        minUncoveredValue = matrix[i][j];
                    }
                }
            }
        }

        if (minUncoveredValue == Double.POSITIVE_INFINITY)
            return false;

        // add the min value to all covered rows
        for (int i = 0; i < coveredRows.length; i++) {
            if (coveredRows[i]) {
                for (int j = 0; j < matrix[i].length; j++) {
                    matrix[i][j] += minUncoveredValue;
                }
            }
        }

        // subtract the min value from all uncovered columns
        for (int i = 0; i < coveredCols.length; i++) {
            if (!coveredCols[i]) {
                for (int j = 0; j < matrix.length; j++) {
                    matrix[j][i] -= minUncoveredValue;
                }
            }
        }

        return true;
    }

}
