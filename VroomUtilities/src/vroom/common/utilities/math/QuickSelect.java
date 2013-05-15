package vroom.common.utilities.math;

import java.util.Arrays;
import java.util.Random;

import vroom.common.utilities.Utilities;

/**
 * The class <code>QuickSelect</code> contains an implementation of <em>quicksort</em> that can be used to find the k
 * lowest or greatest elements of an array.
 * <p>
 * Freely inspired from <a href=
 * "http://www.java-tips.org/java-se-tips/java.lang/quickselect-implementation-with-median-of-three-partitioning-and-cutoff.html"
 * >http://www.java-tips.org/java-se-tips/java.lang/quickselect-implementation-with-median-of-three-partitioning-and-
 * cutoff.html</a>
 * <p>
 * <cite>Quicksort with median-of-three partitioning functions nearly the same as normal quicksort with the only
 * difference being how the pivot item is selected. In normal quicksort the first element is automatically the pivot
 * item. This causes normal quicksort to function very inefficiently when presented with an already sorted list. The
 * divison will always end up producing one sub-array with no elements and one with all the elements (minus of course
 * the pivot item). In quicksort with median-of-three partitioning the pivot item is selected as the median between the
 * first element, the last element, and the middle element (decided using integer division of n/2). In the cases of
 * already sorted lists this should take the middle element as the pivot thereby reducing the inefficency found in
 * normal quicksort.</cite>
 * </p>
 * <p>
 * Creation date: Feb 29, 2012 - 2:54:21 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class QuickSelect {

    /**
     * Returns the <code>k</code> lowest values of an array
     * <p>
     * Complexity is in <code>O(array.length)</code>, this implementation will accept {@code  null} values if there is at
     * least {@code  k} non {@code  null} values
     * </p>
     * 
     * @param values
     *            an array of value
     * @param k
     *            the number of elements to extract
     * @param preserveArray
     *            {@code true} if the original array should not be modified, {@code false} if its elements can be
     *            reordered
     * @param sort
     *            {@code true} if the result should be sorted (according to their natural order)
     * @return the he <code>k</code> lowest values of <code>values</code>
     */
    public static <T extends Comparable<? super T>> T[] min(T[] values, int k, boolean preserveArray, boolean sort) {
        return quickSelect(values, k, preserveArray, sort, true);
    }

    /**
     * Returns the <code>k</code> greatest values of an array
     * <p>
     * Complexity is in <code>O(array.length)</code>, this implementation will accept {@code  null} values if there is at
     * least {@code  k} non {@code  null} values
     * </p>
     * 
     * @param values
     *            an array of value
     * @param k
     *            the number of elements to extract
     * @param preserveArray
     *            {@code true} if the original array should not be modified, {@code false} if its elements can be
     *            reordered
     * @param sort
     *            {@code true} if the result should be sorted (according to their natural order)
     * @return the he <code>k</code> greatest values of <code>values</code>
     */
    public static <T extends Comparable<? super T>> T[] max(T[] values, int k, boolean preserveArray, boolean sort) {
        return quickSelect(values, k, preserveArray, sort, false);
    }

    /**
     * Returns the <code>k</code> lowest or greatest values of an array
     * <p>
     * Complexity is in <code>O(array.length)</code>, this implementation will accept {@code  null} values if there is at
     * least {@code  k} non {@code  null} values. In the contrary, the returned array will only contain non {@code  null}
     * values and will therefore be of smaller length.
     * </p>
     * 
     * @param values
     *            an array of value
     * @param k
     *            the number of elements to extract
     * @param preserveArray
     *            {@code true} if the original array should not be modified, {@code false} if its elements can be
     *            reordered
     * @param sort
     *            {@code true} if the result should be sorted (according to their natural order)
     * @param min
     *            {@code true} for the <code>k</code> lowest values, {@code false} for the <code>k</code> greatest
     *            values
     * @return the he <code>k</code> lowest or greatest values of <code>values</code>
     */
    public static <T extends Comparable<? super T>> T[] quickSelect(T[] values, int k, boolean preserveArray,
            boolean sort, boolean min) {
        T[] clone = preserveArray ? Arrays.copyOf(values, values.length) : values;
        quickSelect(clone, k, min);

        // The number of non-null elements
        int nonNullLength = Math.min(k, clone.length);
        // Reduce the length of the result array so that it does not contains any null values
        while (clone[nonNullLength] == null && nonNullLength > 0)
            nonNullLength--;

        T[] sel = Arrays.copyOf(clone, nonNullLength);
        if (sort)
            Arrays.sort(sel);
        return sel;
    }

    /**
     * Quick selection algorithm. Places the kth lowest/greatest item in a[k-1].
     * <p>
     * This implementation will accept {@code  null} values if there is at least {@code  k} non {@code  null} values
     * </p>
     * 
     * @param a
     *            an array of Comparable items.
     * @param k
     *            the desired rank (1 is minimum) in the entire array
     * @param min
     *            {@code true} for the <code>k</code> lowest values, {@code false} for the <code>k</code> greatest
     *            values
     * @return the kth lowest/greatest item
     */
    public static <T extends Comparable<? super T>> T quickSelect(T[] a, int k, boolean min) {
        return quickSelect(a, 0, a.length - 1, k, min);
    }

    /**
     * Internal selection method that makes recursive calls. Uses median-of-three partitioning and a cutoff of 10.
     * Places the kth smallest item in a[k-1].
     * 
     * @param a
     *            an array of Comparable items.
     * @param low
     *            the left-most index of the subarray.
     * @param high
     *            the right-most index of the subarray.
     * @param k
     *            the desired rank (1 is minimum) in the entire array.
     * @param min
     *            {@code true} for the <code>k</code> lowest values, {@code false} for the <code>k</code> greatest
     *            values
     * @return the kth lowest/greatest item
     */
    private static <T extends Comparable<? super T>> T quickSelect(T[] a, int low, int high, int k, boolean min) {
        if (k > a.length)
            k = a.length;
        if (low + CUTOFF > high)
            insertionSort(a, low, high, min);
        else {
            // Sort low, middle, high
            int middle = (low + high) / 2;
            if (compare(a[middle], a[low], min) < 0)
                swapReferences(a, low, middle);
            if (compare(a[high], a[low], min) < 0)
                swapReferences(a, low, high);
            if (compare(a[high], a[middle], min) < 0)
                swapReferences(a, middle, high);

            // Place pivot at position high - 1
            swapReferences(a, middle, high - 1);
            T pivot = a[high - 1];

            // Begin partitioning
            int i, j;
            for (i = low, j = high - 1;;) {
                while (compare(a[++i], pivot, min) < 0)
                    ;
                while (compare(pivot, a[--j], min) < 0)
                    ;
                if (i >= j)
                    break;
                swapReferences(a, i, j);
            }

            // Restore pivot
            swapReferences(a, i, high - 1);

            // Recurse; only this part changes
            if (k <= i)
                quickSelect(a, low, i - 1, k, min);
            else if (k > i + 1)
                quickSelect(a, i + 1, high, k, min);
        }

        return a[k - 1];
    }

    /**
     * Internal insertion sort routine for subarrays that is used by quicksort.
     * 
     * @param a
     *            an array of Comparable items.
     * @param low
     *            the left-most index of the subarray.
     * @param n
     *            the number of items to sort.
     */
    private static <T extends Comparable<? super T>> void insertionSort(T[] a, int low, int high, boolean min) {
        for (int p = low + 1; p <= high; p++) {
            T tmp = a[p];
            int j;

            for (j = p; j > low && compare(tmp, a[j - 1], min) < 0; j--)
                a[j] = a[j - 1];
            a[j] = tmp;
        }
    }

    /**
     * Adjusted comparison for the max version
     * 
     * @param o1
     * @param o2
     * @param min
     *            {@code true} for min version, {@code false} for max version
     * @return {@code  +/- o1.compareTo(o2)}
     */
    private static <T extends Comparable<? super T>> int compare(T o1, T o2, boolean min) {
        if (o1 == null)
            return 1;
        else if (o2 == null)
            return -1;
        else
            return min ? o1.compareTo(o2) : -o1.compareTo(o2);
    }

    private static final int CUTOFF = 10;

    /**
     * Method to swap to elements in an array.
     * 
     * @param a
     *            an array of objects.
     * @param index1
     *            the index of the first object.
     * @param index2
     *            the index of the second object.
     */
    public static final void swapReferences(Object[] a, int index1, int index2) {
        Object tmp = a[index1];
        a[index1] = a[index2];
        a[index2] = tmp;
    }

    public static void main(String[] args) {
        Random r = new Random();
        Integer[] array = new Integer[500];
        for (int i = 0; i < array.length; i++)
            array[i] = r.nextInt(1000);

        array[0] = null;
        int k = 10;

        // ----------------------
        quickSelect(array, 10, true);
        for (int i = 0; i < k; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
        System.out.println(Utilities.toShortString(quickSelect(array, 10, true, true, true)));
        System.out.println("Incoherencies:");
        for (int i = 0; i < k; i++) {
            for (int j = k; j < array.length; j++)
                if (array[i] != null && array[j] != null && array[j] < array[i])
                    System.out.println(array[j]);
        }
        // ----------------------
        System.out.println();

        quickSelect(array, 10, false);
        for (int i = 0; i < k; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
        System.out.println(Utilities.toShortString(quickSelect(array, 10, true, true, false)));

        System.out.println("Incoherencies:");
        for (int i = 0; i < k; i++) {
            for (int j = k; j < array.length; j++)
                if (array[i] != null && array[j] != null && array[j] > array[i])
                    System.out.println(array[j]);
        }
    }

}