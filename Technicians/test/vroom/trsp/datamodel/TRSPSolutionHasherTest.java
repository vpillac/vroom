package vroom.trsp.datamodel;

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomPermutation;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.modeling.dataModel.Depot;

public abstract class TRSPSolutionHasherTest {

    private final static int    EXP_REP        = 100000;
    private final static int    REQ_COUNT      = 1000;
    private static final int    TEC_COUNT      = 50;
    private final static double MAX_COLLISIONS = 0.0001;
    private ITRSPSolutionHasher mHasher;

    private TRSPInstance        mInstance;
    private Integer[]           mNodes;

    protected abstract ITRSPSolutionHasher getHasher(TRSPInstance instance, RandomStream stream);

    protected final RandomStream getHasherStream() {
        MRG32k3a stream = new MRG32k3a("test-hash");
        stream.setSeed(new long[] { 1, 2, 3, 4, 5, 6 });
        return stream;
    }

    protected final RandomStream getTestStream() {
        MRG32k3a stream = new MRG32k3a("test-hash");
        stream.setSeed(new long[] { 10, 20, 30, 40, 50, 60 });
        return stream;
    }

    @Before
    public void setup() {
        mInstance = new TRSPInstance("test", new ArrayList<Technician>(), 0, 0, 0,
                new ArrayList<Depot>(), new ArrayList<TRSPRequest>(), false) {
            @Override
            public int getDepotCount() {
                return 0;
            }

            @Override
            public int getRequestCount() {
                return REQ_COUNT;
            }
        };
        mHasher = getHasher(mInstance, getHasherStream());
        mNodes = new Integer[REQ_COUNT];
        for (int i = 0; i < mNodes.length; i++) {
            mNodes[i] = i;
        }
    }

    @Test
    public void hashTourTest() {
        RandomStream rnd = getTestStream();
        HashMap<Integer, ITRSPTour> pool = new HashMap<Integer, ITRSPTour>(EXP_REP);
        int collisions = 0;
        for (int i = 0; i < EXP_REP; i++) {
            // Generate a tour
            // Length
            int l = rnd.nextInt((int) (0.8 * REQ_COUNT), REQ_COUNT);
            // Tour
            ITRSPTour tour = generateTour(l, rnd);

            // Check if tour was already present
            ITRSPTour prev = pool.put(tour.hashCode(), tour);
            if (prev != null && !equal(prev, tour)) {
                collisions++;
                System.out.printf("hashToutTest: Collision: hash=%s prev=%s new=%s\n",
                        tour.hashCode(), prev.getNodeSeqString(), tour.getNodeSeqString());
            }
        }

        int maxCol = (int) (MAX_COLLISIONS * EXP_REP);
        System.out.printf(
                "hashToutTest: %s collisions out of %s different tours (max: %s - it:%s)\n",
                collisions, pool.size(), maxCol, EXP_REP);
        if (collisions > maxCol) {
            fail(String.format("Number of collisions (%s) exceeds tolerance (%s)", collisions,
                    maxCol));
        }
    }

    protected abstract boolean equal(ITRSPTour a, ITRSPTour b);

    private final ITRSPTour generateTour(int length, RandomStream rnd) {
        RandomPermutation.shuffle(mNodes, rnd);
        Integer[] tour = Arrays.copyOf(mNodes, length);
        return new HashTestTour(tour, rnd.nextInt(0, TEC_COUNT));
    }

    protected class HashTestTour extends TRSPTourBase implements Serializable {

        private static final long serialVersionUID = 1L;

        private final int         mTechnicianId;

        private final int         mHash;

        private final int[]       mNodes;

        public HashTestTour(Integer[] tour, int tech) {
            mNodes = new int[tour.length];
            for (int i = 0; i < tour.length; i++) {
                mNodes[i] = tour[i];
            }
            mTechnicianId = tech;
            mHash = mHasher.hash(this);
        }

        @Override
        public int getTechnicianId() {
            return mTechnicianId;
        }

        @Override
        public TRSPInstance getInstance() {
            return mInstance;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof HashTestTour && ((HashTestTour) obj).mHash == this.mHash;
        }

        @Override
        public int hashCode() {
            return mHash;
        }

        @Override
        public int hashSolution() {
            return mHash;
        }

        @Override
        public ITourIterator iterator() {
            return new SimpleTourIterator(this);
        }

        @Override
        public double getTotalCost() {
            return 0;
        }

        @Override
        public int length() {
            return mNodes.length;
        }

        @Override
        public int[] asArray() {
            return new int[0];
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(length() * 3);

            sb.append(String.format("t:%s c:%.2f l:%s <", getTechnicianId(), getTotalCost(),
                    length()));

            Iterator<Integer> it = iterator();
            while (it.hasNext()) {
                int n = it.next();
                sb.append(n);
                if (it.hasNext())
                    sb.append(",");
            }
            sb.append(">");

            return sb.toString();
        }

        @Override
        public String getNodeSeqString() {
            StringBuilder sb = new StringBuilder(length() * 3);

            sb.append("<");

            Iterator<Integer> it = iterator();
            while (it.hasNext()) {
                int n = it.next();
                sb.append(n);
                if (it.hasNext())
                    sb.append(",");
            }
            sb.append(">");

            return sb.toString();
        }

        @Override
        public int getFirstNode() {
            if (length() == 0)
                return ITRSPTour.UNDEFINED;
            return mNodes[0];
        }

        @Override
        public int getLastNode() {
            if (length() == 0)
                return ITRSPTour.UNDEFINED;
            return mNodes[mNodes.length];
        }

        @Override
        public boolean isVisited(int node) {
            for (int i = 0; i < mNodes.length; i++) {
                if (mNodes[i] == node)
                    return true;
            }
            return false;
        }

        @Override
        public int getNodeAt(int index) {
            return mNodes[index];
        }

        @Override
        public ITRSPTour clone() {
            return null;
        }

    }
}
