/**
 * 
 */
package vroom.trsp.datamodel.costDelegates;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import vroom.common.utilities.Constants;
import vroom.trsp.bench.TRSPRunBase;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionCheckerBase;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.constraints.HomeConstraint;
import vroom.trsp.optimization.constraints.SparePartsConstraint;
import vroom.trsp.optimization.constraints.TWConstraint;
import vroom.trsp.optimization.constraints.ToolsConstraint;
import vroom.trsp.optimization.constraints.TourConstraintHandler;

/**
 * <code>TRSPWorkingTimeTest</code> is a test case for {@link TRSPWorkingTime}
 * <p>
 * Creation date: Mar 9, 2012 - 12:58:37 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPWorkingTimeTest {

    private static TRSPRunBase           sRun;
    private static TRSPInstance          sInstance;
    private static TRSPWorkingTime       sWT;

    private static TourConstraintHandler sCtr;

    private TRSPSolution                 mSolution;
    private TRSPTour                     mTour;
    private final int                    sRep  = 500;
    private final int                    sRep2 = 5;

    private Random                       mRnd;

    @BeforeClass
    public static void setUpBeforeClass() {
        sRun = TRSPRunBase.newTRSPRunTest("RC201.100_25-5-5-5.txt", TRSPRunBase.CFG_TRSP, 10);
        sInstance = sRun.getInstance();
        sCtr = new TourConstraintHandler(new HomeConstraint(), new TWConstraint(),
                new ToolsConstraint(), new SparePartsConstraint());
        sWT = new TRSPWorkingTime();
    }

    @Before
    public void setUp() {
        mSolution = new TRSPSolution(sInstance, sWT);
        mTour = mSolution.getTour(0);
        mTour.initialize();
    }

    InsertionMove insertRequest(boolean execute) {
        InsertionMove mve = null;
        ArrayList<Integer> unserved = new ArrayList<>(mSolution.getUnservedRequests());
        Collections.shuffle(unserved, mRnd);
        ListIterator<Integer> it = unserved.listIterator(unserved.size() - 1);
        while (mve == null && it.hasPrevious()) {
            // Pick a request
            int r = it.previous();
            it.remove();
            mSolution.markAsServed(r);
            // Insert request
            mve = InsertionMove.findInsertion(r, mTour, mTour.getCostDelegate(), sCtr, false, true);
            if (mve != null && (execute && !InsertionMove.executeMove(mve))) {
                mve = null;
            }
        }
        return mve;
    }

    /**
     * Test method for
     * {@link vroom.trsp.datamodel.costDelegates.TRSPWorkingTime#evaluateGenericTour(vroom.trsp.datamodel.ITRSPTour)}.
     */
    @Test
    public void testEvaluateGenericTour() {
        mRnd = new Random(0);
        System.out.println("testEvaluateGenericTour");
        for (int i = 0; i < sRep; i++) {
            InsertionMove mve = insertRequest(true);
            while (mve != null) {
                double checkWT = TRSPSolutionCheckerBase.evaluateTotalDuration(mTour, -1);
                double cdWT = mTour.getCostDelegate().evaluateTour(mTour, true);
                // System.out.printf("check:%5.2f cd:%5.2f tour:%5.2f  - %s\n", checkWT, cdWT, tourWT,
                // mTour);
                assertEquals(checkWT, cdWT, 1E-6);
                mve = insertRequest(true);
            }
        }
    }

    /**
     * Test method for
     * {@link vroom.trsp.datamodel.costDelegates.TRSPWorkingTime#evaluateInsMove(vroom.trsp.optimization.InsertionMove)}
     * .
     */
    @Test
    public void testEvaluateInsMove() {
        mRnd = new Random(1);
        System.out.println("testEvaluateInsMove");

        for (int k = 0; k < sRep; k++) {
            mTour.clear();
            mTour.initialize();
            for (TRSPRequest r : sInstance.getRequests())
                mSolution.markAsUnserved(r.getID());

            double costBefore = TRSPSolutionCheckerBase.evaluateTotalDuration(mTour, -1);
            InsertionMove mve = insertRequest(true);
            double costAfter = TRSPSolutionCheckerBase.evaluateTotalDuration(mTour, -1);
            while (mve != null) {
                assertEquals(mve.toString(), costAfter - costBefore, mve.getCost(),
                        Constants.getZeroTolerance());

                costBefore = TRSPSolutionCheckerBase.evaluateTotalDuration(mTour, -1);
                mve = insertRequest(true);
                costAfter = TRSPSolutionCheckerBase.evaluateTotalDuration(mTour, -1);
            }

            if (mTour.length() <= 5)
                continue;
            // At this stage we have a complete tour
            for (int kk = 0; kk < sRep2; kk++) {
                System.out.println();
                // Remove two requests randomly and reinsert them at the same position
                int[] idx = new int[] { mRnd.nextInt(mTour.length() - 2),
                        mRnd.nextInt(mTour.length() - 2) };
                while (idx[0] == idx[1])
                    idx = new int[] { mRnd.nextInt(mTour.length() - 2),
                            mRnd.nextInt(mTour.length() - 2) };
                Arrays.sort(idx);

                double old = mTour.getMinimalDuration();

                int r = mTour.getNodeAt(idx[0] + 1);
                int m = mTour.getPred(r), n = mTour.getSucc(r);

                int q = mTour.getNodeAt(idx[1] + 1);
                int i = mTour.getPred(q), j = mTour.getSucc(q);

                // System.out.printf("(%s,%s,%s,...,%s,%s,%s) %s\n", m, r, n, i, q, j, mTour.getNodeSeqString());

                mTour.removeNode(r);
                mTour.removeNode(q);
                double tmp = mTour.getMinimalDuration();

                double eval = sWT.evaluateDetour(mTour, m, r, n, i, q, j, false);
                mTour.insertAfter(m, r);
                mTour.insertAfter(i, q);
                // System.out.printf("old=%3.0f tmp=%3.0f new=%3.0f eval=%3.0f real=%3.0f\n", old,
                // tmp, mTour.getMinimalDuration(), eval, old - tmp);

                // System.out
                // .printf("F0f'=%3.2f Di'=%3.2f Ar=%3.2f Aq=%3.2f An=%3.2f Wr=%3.2f W0q=%3.2f  W0j'=%3.2f W0f'=%3.2f Aj'=%3.2f\n",
                // mTour.getFwdSlackTime(mTour.getFirstNode()),
                // mTour.getEarliestDepartureTime(i), mTour.getEarliestArrivalTime(r),
                // mTour.getEarliestArrivalTime(q), mTour.getEarliestArrivalTime(n),
                // mTour.getWaitingTime(r),
                // mTour.getWaitingTime(mTour.getFirstNode(), q),
                // mTour.getWaitingTime(mTour.getFirstNode(), j),
                // mTour.getWaitingTime(mTour.getFirstNode(), mTour.getLastNode()),
                // mTour.getEarliestArrivalTime(j));
                // System.out.println(mTour.toDetailedString());
                assertEquals(old - tmp, eval, Constants.getZeroTolerance());
            }
        }
    }

    void buildKnownTour(String tour) {
        String[] nodes = tour.split(",");
        for (String n : nodes) {
            mTour.insertBefore(mTour.getLastNode(), Integer.valueOf(n));
        }
    }

    public static void main(String[] args) {
        TRSPWorkingTimeTest test = new TRSPWorkingTimeTest();
        TRSPWorkingTimeTest.setUpBeforeClass();
        test.setUp();
        final TRSPTour t = test.mTour;
        // test.testEvaluateGenericTour();

        // test.setUp();
        // test.testEvaluateInsMove();

        test.buildKnownTour("70,30,152,87,107,94,27,63,96,78,104,33,28,81,75,57,79,60,85,102,95");
        double old = t.getMinimalDuration();
        System.out.println(t.getFwdSlackTime(t.getFirstNode()));

        int m = 87, r = 107, n = 94, i = 94, q = 27, j = 63;

        System.out.printf("(%s,%s,%s,...,%s,%s,%s) %s\n", m, r, n, i, q, j, t.getNodeSeqString());

        t.removeNode(r);
        t.removeNode(q);
        double tmp = t.getMinimalDuration();

        double eval = sWT.evaluateDetour(t, m, r, n, i, q, j, false);
        t.insertAfter(m, r);
        t.insertAfter(i, q);
        System.out.printf("old=%3.2f tmp=%3.2f new=%3.2f eval=%3.2f real=%3.2f\n", old, tmp,
                t.getMinimalDuration(), eval, old - tmp);
        System.out.printf("Ar=%3.2f Aq=%3.2f An=%3.2f Wr=%3.2f W0q=%3.2f ",
                t.getEarliestArrivalTime(r), t.getEarliestArrivalTime(q),
                t.getEarliestArrivalTime(n), t.getWaitingTime(r),
                t.getWaitingTime(t.getFirstNode(), q));
    }
}
