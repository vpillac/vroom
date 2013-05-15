package vroom.trsp.datamodel;

import java.util.Iterator;

import umontreal.iro.lecuyer.rng.RandomStream;

public class GroerSolutionHasherTest extends TRSPSolutionHasherTest {

    @Override
    protected ITRSPSolutionHasher getHasher(TRSPInstance instance, RandomStream stream) {
        return new GroerSolutionHasher(instance, stream);
    }

    @Override
    protected boolean equal(ITRSPTour a, ITRSPTour b) {
        if (a.length() != b.length())
            return false;

        Iterator<Integer> ita = a.iterator();
        Iterator<Integer> itb = b.iterator();

        while (ita.hasNext()) {
            if (ita.next() != itb.next())
                return false;
        }
        return true;
    }

}
