package vroom.optimization.online.jmsa.benchmarking;

import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import vroom.common.modeling.io.NovoaPersistenceHelper;

public class NovoaBenchmarkingTest {

    @Test
    public void testGetFirstSeed() {
        Set<Long> vals = new HashSet<Long>();

        for (int set = 1; set <= 2; set++) {
            for (int size : NovoaPersistenceHelper.SIZE_MAPPING) {
                for (int rep = 1; rep <= 5; rep++) {
                    for (int run = 0; run < 2000; run++) {
                        for (int cap = 0; cap <= 1; cap++) {
                            long seed = NovoaBenchmarking.getFirstSeed(run, size, rep, cap, set);
                            assertFalse("Seed " + seed + " is already present", vals.contains(seed));
                            // System.out.printf("%s,%s,%s,%s,%s=[%s", set,
                            // size, rep, run, cap, seed);
                            // vals.add(seed);

                            for (int i = 1; i < 6; i++) {
                                assertFalse("Seed " + seed + i + " is already present",
                                        vals.contains(seed + i));
                                vals.add(seed + i);
                                // System.out.printf(",%s", seed + i);
                            }

                            // System.out.println("]");

                        }
                    }
                }
            }
        }
    }
}
