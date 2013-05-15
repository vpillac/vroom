package vroom.trsp.sandbox;

import java.math.RoundingMode;

import vroom.trsp.ALNSSCSolver;
import vroom.trsp.bench.TRSPRunBase;
import vroom.trsp.datamodel.TRSPDetailedSolutionChecker;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPSolutionChecker;
import vroom.trsp.datamodel.TRSPSolutionCheckerBase;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.util.TRSPGlobalParameters;

public class SandBox {

    public static void main(String[] args) {
        // System.out.println(Integer.MAX_VALUE);
        // testDoublePrecision();
        // testFwdSlackTime();
        // String c = "C101.100_25-5-5-5_rd_10.txt";
        // int idx = c.indexOf("rd_");
        // String dp = c.substring(idx + 3, idx + 5);
        // System.out.println(dp);
        testFwdSlackTime();
    }

    static void testDoublePrecision() {
        double n = 100;
        double ratio = 1 / 3;
        // double ratio = 2;
        double result = 1 / Math.pow(3, n);
        double result2 = 1;
        for (int i = 0; i < n; i++)
            result2 = result2 * ratio;

        System.out.println(result);
        System.out.println(Double.doubleToLongBits(result));
        System.out.println(result2);
        System.out.println(Double.doubleToLongBits(result2));

    }

    static void testFwdSlackTime() {
        TRSPRunBase run = TRSPRunBase.newTRSPRunTest("C101.100_25-5-5-5.txt",
                "./config/bench/bench_trsp_palnssc_25crew.cfg", 10);
        run.getInstance().getCostDelegate().setPrecision(0, RoundingMode.FLOOR);
        run.getParameters().set(TRSPGlobalParameters.THREAD_COUNT, 1);
        ALNSSCSolver solver = (ALNSSCSolver) run.getSolver();

        solver.initialization();
        TRSPSolution sol = solver.getInitSol();
        // Choose the longest tour
        TRSPTour tour = sol.getTour(0);
        for (int i = 1; i < sol.getTourCount(); i++) {
            if (sol.getTour(i).length() > tour.length())
                tour = sol.getTour(i);
            printTourFwdSlack(sol.getTour(i));
            tour.propagateUpdate(tour.getFirstNode(), tour.getLastNode());
        }
    }

    static void printTourFwdSlack(TRSPTour tour) {
        if (tour.length() <= 2)
            return;
        System.out.println(tour);

        double delayS = Math.min(tour.getFwdSlackTime(tour.getFirstNode()),
                tour.getWaitingTime(tour.getFirstNode(), tour.getLastNode()));
        double delayF0n = Math.min(
                TRSPSolutionCheckerBase.evaluateFwdSlackTime(tour, tour.getFirstNode(),
                        tour.getLastNode()),
                tour.getWaitingTime(tour.getFirstNode(), tour.getLastNode()));

        double time = 0;
        double timeS = delayS;
        double timeF0n = delayF0n;
        int pred = tour.getFirstNode();
        for (int i : tour) {
            // System.out.printf(
            // "%3.0f - %3.0f\n", //
            // tour.getTimeWindow(pred).getEarliestStartOfService(time)
            // + tour.getServiceTime(pred), //
            // tour.getEarliestDepartureTime(pred));

            time = tour.getTimeWindow(pred).getEarliestStartOfService(time)
                    + tour.getServiceTime(pred) + tour.getTravelTime(pred, i);
            timeS = tour.getTimeWindow(pred).getEarliestStartOfService(timeS)
                    + tour.getServiceTime(pred) + tour.getTravelTime(pred, i);
            timeF0n = tour.getTimeWindow(pred).getEarliestStartOfService(timeF0n)
                    + tour.getServiceTime(pred) + tour.getTravelTime(pred, i);

            System.out
                    .printf("%-5s %3.0f@[%3.0f;%3.0f]->%3.0f s=%3.0f Fi=%3.0f\t t=%3.0f  ts=%3.0f  tF0n=%3.0f   W0i=%3.0f  %s %s %s sc=%s\n",
                            i, //
                            tour.getEarliestArrivalTime(i), //
                            tour.getTimeWindow(i).startAsDouble(), //
                            tour.getTimeWindow(i).endAsDouble(), //
                            tour.getEarliestDepartureTime(i), //
                            tour.getFwdSlackTime(i), //
                            TRSPSolutionChecker.evaluateFwdSlackTime(tour, i, tour.getLastNode()),//
                            time,
                            timeS,
                            timeF0n,//
                            tour.getWaitingTime(tour.getFirstNode(), i),//
                            tour.getTimeWindow(i).isFeasible(timeS) ? "..." : "/!\\",//
                            tour.getTimeWindow(i).isFeasible(timeF0n) ? "..." : "/!\\",//
                            timeS == timeF0n ? "==" : "!=",
                            Math.abs(tour.getFwdSlackTime(i)
                                    - TRSPDetailedSolutionChecker.evaluateFwdSlackTime(tour, i,
                                            tour.getLastNode())) > 1e-3 ? "!=" : "==");

            pred = i;
        }

        TRSPWorkingTime wt = new TRSPWorkingTime();

        System.out.printf("T=%3.0f Ts=%3.0f TF0n=%3.0f Cost=%3.0f Cost2=%3.0f\n", time, timeS
                - delayS, timeF0n - delayF0n, tour.getTotalCost(), wt.evaluateGenericTour(tour));

    }

}
