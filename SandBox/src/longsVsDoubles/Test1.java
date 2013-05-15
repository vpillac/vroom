/*
 * Log of changes: 2011.
 */
package longsVsDoubles;

import java.util.Random;

import dataGenerators.DoubleGenerator;
import dataGenerators.IntegerGenerator;

/**
 * @author Jorge E. Mendoza (jorge.mendoza@uco.f)
 * @since 2011.
 * @args[0] number of nodes in the instance
 * @args[1] number of operations to perform
 */
public class Test1 {

    public static void main(String[] args) {
        int size = 100;
        int bench = 10000000;
        if (args.length > 0) {
            size = Integer.valueOf(args[0]);
            bench = Integer.valueOf(args[1]);
        }

        Random rnd = new Random(1);
        System.out.printf("Initializing data with size=%s and bench=%s\n", size, bench);
        double[][] distanceMatrix = DoubleGenerator.getDoubleMatrix(size, size, rnd);
        int[][] pairs = IntegerGenerator.getIntMatrix(bench, 2, rnd, size);
        DoublesVsLongs test = new DoublesVsLongs(distanceMatrix);
        System.out.println("Starting test");
        test.testAddition(pairs);
        System.out.println("FINISHED");
    }
}
