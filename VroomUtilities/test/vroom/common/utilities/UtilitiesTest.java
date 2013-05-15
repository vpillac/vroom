package vroom.common.utilities;

import org.junit.Before;
import org.junit.Test;

public class UtilitiesTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testLevenshteinDistance() {
        // Looks like this test procedure is not able to generate valid test cases
        // Assume the function works ok

        // int testSize = 100;
        // int rep = 10;
        //
        // for (int j = 0; j < rep; j++) {
        // Random r = new Random(j);
        //
        // // Initialize
        // ArrayList<Integer> ref = new ArrayList<Integer>(testSize);
        // ArrayList<Integer> test = new ArrayList<Integer>(testSize);
        // boolean[] removed = new boolean[testSize];
        // for (int i = 0; i < testSize; i++) {
        // ref.add(i);
        // test.add(i);
        // }
        // System.out.println("ref : " + ref);
        //
        // int delCount = r.nextInt(testSize / 10 + 1);
        // int subCount = r.nextInt(testSize / 10 + 1);
        // int insCount = r.nextInt(testSize / 10 + 1);
        //
        // int correction = 0;
        //
        // System.out.print("    rem: ");
        // // Removals
        // for (int i = 0; i < delCount; i++) {
        // int rem = test.remove(r.nextInt(test.size()));
        // removed[rem] = true;
        // System.out.print(rem + " ");
        // }
        // // Substitutions
        // System.out.print("sub: ");
        // for (int i = 0; i < subCount; i++) {
        // int prev = test.set(r.nextInt(test.size()), testSize + i);
        // System.out.printf("(%s,%s) ", prev, testSize + i);
        // } // Insertions
        // System.out.print("ins: ");
        // for (int i = 0; i < insCount; i++) {
        // int idx = r.nextInt(test.size());
        // if (test.get(idx) > 0 && test.get(idx) < testSize && removed[test.get(idx) - 1])
        // correction++;
        // test.add(idx, 2 * testSize + i);
        // System.out.printf("(%s,%s) ", test.get(idx + 1), 2 * testSize + i);
        // }
        // System.out.println();
        //
        // int[] result = Math.levenshteinDistance(ref, ref.size(), test, test.size(), true);
        // int expectedDist = delCount + subCount + insCount - correction;
        // System.out.println("test: " + test);
        // System.out.printf("dist: %s (expected %s - del:%s/%s, ins:%s/%s, sub:%s/%s)\n", result[0], expectedDist,
        // result[1], delCount,//
        // result[2], insCount,//
        // result[3], subCount);
        // assertEquals("Wrong distance", expectedDist, result[0]);
        // System.out.println();
        // }
    }

    public static void main(String[] args) {
        UtilitiesTest test = new UtilitiesTest();

        try {
            test.setUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        test.testLevenshteinDistance();
    }

}
