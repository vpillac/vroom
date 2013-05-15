/*
 * Log of changes: 2011.
 */
package longsVsDoubles;

/**
 * Tests the performance of doubles vs longs in basic operations used by VRP algorithms.
 * 
 * @author Jorge E. Mendoza (jorge.mendoza@uco.f)
 * @since 2011.12.12
 */
public class DoublesVsLongs {

    /**
     * Holds a distance matrix. The distances are represented by double types
     */
    private final double[][] doubleDistances;
    /**
     * Holds a distance matrix. The distances are represented by long types
     */
    private final long[][]   longDistances;

    /**
     * Builds a new DoublesVsLongs test
     * 
     * @param matrix
     *            a matrix of double numbers
     */
    public DoublesVsLongs(double[][] matrix) {
        this.doubleDistances = matrix.clone();
        this.longDistances = new long[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix[0].length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                this.longDistances[i][j] = (long) (this.doubleDistances[i][j] * 10000000);
            }
        }
    }

    /**
     * Tests the performance of doubles vs longs in addition operations.
     * 
     * @param couples
     *            the access points to the distance matrix
     */
    public void testAddition(int[][] couples) {

        // Floats
        long start, end;
        double sum = 0;
        start = System.currentTimeMillis();
        for (int i = 0; i < couples[0].length; i++) {
            sum = sum + this.doubleDistances[couples[i][0]][couples[i][1]];
        }
        end = System.currentTimeMillis();
        System.out.println("Adding doubles: " + (end - start));

        long sum2 = 0;
        start = System.currentTimeMillis();
        for (int i = 0; i < couples[0].length; i++) {
            sum2 = sum2 + this.longDistances[couples[i][0]][couples[i][1]];
        }
        end = System.currentTimeMillis();
        System.out.println("Adding longs: " + (end - start));

    }

    /**
     * Tests the performance of doubles vs longs in addition operations.
     * 
     * @param couples
     *            the access points to the distance matrix
     */
    public void testComparison(int[][] couples) {

        // Floats
        long start, end;
        start = System.currentTimeMillis();
        for (int i = 1; i < couples[0].length; i++) {
            if (this.doubleDistances[couples[i - 1][0]][couples[i - 1][1]] <= this.doubleDistances[couples[i][0]][couples[i][1]]) {
                // Do something (e.g., update best solution found)
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Comparing doubles: " + (end - start));

        start = System.currentTimeMillis();
        for (int i = 1; i < couples[0].length; i++) {
            if (this.longDistances[couples[i - 1][0]][couples[i - 1][1]] <= this.longDistances[couples[i][0]][couples[i][1]]) {
                // Do something (e.g., update best solution found)
            }
        }
        end = System.currentTimeMillis();
        System.out.println("Comparing longs: " + (end - start));
    }

}
