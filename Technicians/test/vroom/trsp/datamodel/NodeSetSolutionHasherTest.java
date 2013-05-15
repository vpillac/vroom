package vroom.trsp.datamodel;

import java.util.HashSet;
import java.util.Set;

import umontreal.iro.lecuyer.rng.RandomStream;

public class NodeSetSolutionHasherTest extends TRSPSolutionHasherTest {

    @Override
    protected ITRSPSolutionHasher getHasher(TRSPInstance instance, RandomStream stream) {
        return new NodeSetSolutionHasher(instance, stream);
    }

    @Override
    protected boolean equal(ITRSPTour a, ITRSPTour b) {
        if (a.length() != b.length())
            return false;

        Set<Integer> sa = new HashSet<Integer>();
        for (Integer i : a) {
            sa.add(i);
        }
        for (Integer i : b) {
            if (!sa.remove(i))
                return false;
        }
        return sa.isEmpty();
    }

}
