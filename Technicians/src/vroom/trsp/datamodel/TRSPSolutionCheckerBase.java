package vroom.trsp.datamodel;

import java.math.RoundingMode;

import vroom.common.modeling.dataModel.attributes.ILocation;
import vroom.common.utilities.IntegerSet;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.dataModel.ISolutionChecker;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;

/**
 * The Interface <code>TRSPSolutionCheckerBase</code> defines classes used to check the feasibility of a
 * {@link TRSPSolution} and {@link TRSPTour}
 * <p>
 * Creation date: Jun 15, 2011 - 9:28:35 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class TRSPSolutionCheckerBase implements ISolutionChecker<TRSPSolution> {

    private final boolean mCheckUnserved;

    /**
     * Creates a new <code>TRSPSolutionCheckerBase</code>
     */
    public TRSPSolutionCheckerBase() {
        this(false);
    }

    /**
     * Creates a new <code>TRSPSolutionCheckerBase</code>
     * 
     * @param checkUnserved
     *            <code>true</code> if unserved reauests should be checked, <code>false</code> otherwise
     */
    public TRSPSolutionCheckerBase(boolean checkUnserved) {
        super();
        mCheckUnserved = checkUnserved;
    }

    /**
     * Check the feasibility of a solution
     * 
     * @param solution
     *            the solution to be checked
     * @return a string describing the infeasibility of <code>solution</code>, or an empty string if the solution is
     *         feasible
     */
    @Override
    public String checkSolution(TRSPSolution solution) {
        if (solution == null)
            return "null solution";

        StringBuilder err = new StringBuilder();

        // ---------------------------------------------------------------
        // Check for unserved requests
        IntegerSet unservedRequests = new IntegerSet(solution.getInstance().getMaxId());
        for (int r : solution.getInstance().getReleasedRequests())
            if (solution.getInstance().getSimulator() == null
                    || !solution.getInstance().getSimulator().isRejected(r))
                unservedRequests.add(r);
        for (ITRSPTour tour : solution)
            for (int node : tour) {
                unservedRequests.remove(node);
            }

        if (mCheckUnserved && !unservedRequests.isEmpty()
                && !solution.getInstance().isUnservedReqAllowed())
            err.append(String.format("Unserved requests: %s",
                    Utilities.toShortString(unservedRequests)));
        if (!unservedRequests.equals(solution.getUnservedRequests()))
            append(err, "Wrong unserved requests (is:%s expected:%s)",
                    Utilities.toShortString(solution.getUnservedRequests()),
                    Utilities.toShortString(unservedRequests));
        // ---------------------------------------------------------------
        // ---------------------------------------------------------------
        // Check for doubly served requests
        IntegerSet servedRequests = new IntegerSet(solution.getInstance().getMaxId());
        for (ITRSPTour tour : solution)
            for (int node : tour) {
                if (solution.getInstance().isRequest(node) && !servedRequests.add(node)) {
                    err.append(String.format("Request %s is served twice (second tour: %s)", node,
                            tour.getTechnicianId()));
                }
            }
        // ---------------------------------------------------------------

        // Check each tour individually
        // ---------------------------------------------------------------
        for (TRSPTour tour : solution) {
            String e = checkTour(tour);
            if (!e.isEmpty()) {
                append(err, "[%s: %s]", tour.getTechnician().getID(), e);
            }
        }
        // ---------------------------------------------------------------

        // Check for the served nodes
        // ---------------------------------------------------------------
        if (solution.getInstance().isDynamic()) {
            for (TRSPTour tour : solution) {
                String e = checkServedAssignedRequests(tour);
                if (!e.isEmpty()) {
                    append(err, "[%s: %s]", tour.getTechnician().getID(), e);
                }
            }
        }
        // ---------------------------------------------------------------

        return err.toString();
    }

    /**
     * Check the feasibility of a tour
     * 
     * @param tour
     *            the tour to be checked
     * @return a string describing the infeasibility of <code>tour</code>, or an empty string if the tour is feasible
     */
    public abstract String checkTour(ITRSPTour tour);

    /**
     * Append a message to the string builder, inserting commas when required
     * 
     * @param err
     *            the error string builder
     * @param format
     *            the format string of the message
     * @param args
     *            the argument of the format string
     */
    protected static void append(StringBuilder err, String format, Object... args) {
        if (err.length() > 0)
            err.append(",");
        err.append(String.format(format, args));
    }

    /**
     * Returns the total distance traveled in the given {@code  solution}
     * 
     * @param solution
     *            the solution to be evaluated
     * @param precision
     *            the precision for possible rounding, a negative value means no rounding
     * @return the total distance traveled
     */
    public static double evaluateTotalEuclidianDistance(TRSPSolution solution, int precision) {
        double dist = 0;
        for (TRSPTour t : solution) {
            dist += evaluateTotalEuclidianDistance(t, precision);
        }
        return dist;
    }

    /**
     * Returns the total distance traveled in the given {@code  tour}
     * 
     * @param tour
     *            the tour to be evaluated
     * @param precision
     *            the precision for possible rounding, a negative value means no rounding
     * @return the total distance traveled
     */
    public static double evaluateTotalEuclidianDistance(TRSPTour tour, int precision) {
        if (tour.length() < 3)
            return 0;
        double totalDist = 0;
        TRSPTourIterator it = tour.iterator();

        ILocation pred = tour.getInstance().getNode(it.next()).getLocation();

        while (it.hasNext()) {
            ILocation node = tour.getInstance().getNode(it.next()).getLocation();
            double dist = Math.sqrt(Math.pow(pred.getX() - node.getX(), 2)
                    + Math.pow(pred.getY() - node.getY(), 2));
            if (precision >= 0)
                dist = Utilities.Math.round(dist, precision, RoundingMode.FLOOR);
            totalDist += dist;
            pred = node;
        }
        return totalDist;
    }

    /**
     * Returns the total duration of the given {@code  tour}
     * 
     * @param tour
     *            the tour to be evaluated
     * @param precision
     *            the precision for possible rounding, a negative value means no rounding
     * @return the total duration of {@code  tour}
     */
    public static double evaluateTotalDuration(TRSPTour tour, int precision) {
        if (tour.length() < 3)
            return 0;
        double time = 0;
        double waiting = 0;

        TRSPTourIterator it = tour.iterator();

        int pred = it.next();
        ILocation predNode = tour.getInstance().getNode(pred).getLocation();

        while (it.hasNext()) {
            int n = it.next();
            ILocation node = tour.getInstance().getNode(n).getLocation();

            // Travel time
            double travel = Math.sqrt(Math.pow(predNode.getX() - node.getX(), 2)
                    + Math.pow(predNode.getY() - node.getY(), 2))
                    / tour.getTechnician().getSpeed();
            if (precision >= 0)
                travel = Utilities.Math.round(travel, precision, RoundingMode.FLOOR);
            // --

            waiting += tour.getTimeWindow(pred).getWaiting(time);
            time = tour.getTimeWindow(pred).getEarliestStartOfService(time)
                    + tour.getServiceTime(pred) + travel;

            pred = n;
            predNode = node;
        }
        waiting += tour.getTimeWindow(pred).getWaiting(time);
        time = tour.getTimeWindow(pred).getEarliestStartOfService(time) + tour.getServiceTime(pred);

        double fwdSlack = evaluateFwdSlackTime(tour, tour.getFirstNode(), tour.getLastNode());

        return time - (tour.getEarliestStartTime() + Math.min(waiting, fwdSlack));
    }

    /**
     * Evaluates the forward slack time between node {@code  i} and {@code  j} in {@code  tour}
     * 
     * @param tour
     * @param i
     * @param j
     * @return the forward slack time between node {@code  i} and {@code  j} in {@code  tour}
     */
    public static double evaluateFwdSlackTime(TRSPTour tour, int i, int j) {
        if (j == i)
            return Double.POSITIVE_INFINITY;
        double Di = tour.getEarliestDepartureTime(i);
        TRSPTourIterator it = tour.iterator(tour.getSucc(i));
        return evaluateFwdSlackTime(tour, it, Di, i, j);
    }

    /**
     * Evaluates the forward slack time between node {@code  i} and {@code  j} in {@code  tour}
     * 
     * @param tour
     * @param earlyDepi
     *            the earliest departure time at node {@code  i}
     * @param i
     * @param j
     * @return the forward slack time between node {@code  i} and {@code  j} in {@code  tour}
     */
    public static double evaluateFwdSlackTime(ITRSPTour tour, double earlyDepi, int i, int j) {
        if (j == i)
            return Double.POSITIVE_INFINITY;
        ITourIterator it = tour.iterator();
        int k = it.next();
        while (k != i)
            k = it.next();
        return evaluateFwdSlackTime(tour, it, earlyDepi, i, j);
    }

    private static double evaluateFwdSlackTime(ITRSPTour tour, ITourIterator it, double earlyDep,
            int i, int j) {
        if (j == i)
            return Double.POSITIVE_INFINITY;
        double Fij = Double.POSITIVE_INFINITY;
        double sumT = 0, sumS = 0;
        double Di = earlyDep;
        int p = i;
        int q;
        do {
            q = it.next();

            sumT += tour
                    .getInstance()
                    .getCostDelegate()
                    .getTravelTime(p, q,
                            tour.getInstance().getFleet().getVehicle(tour.getTechnicianId()));
            double bq = tour.getInstance().getTimeWindow(q).endAsDouble();
            // Earliest arrival at q: departure from i + sum of travel times up to q + sum of service times up to p
            double eaq = Di + sumT + sumS;
            double Fiq = bq - eaq;
            if (Fiq < Fij)
                Fij = Fiq;
            // Sum of service times up to q
            sumS += tour.getInstance().getServiceTime(q);
            p = q;
        } while (q != j);
        return Fij;
    }

    /**
     * Check the feasibility of a tour regarding requests that have already been served or assigned
     * 
     * @param tour
     *            the tour to be checked
     * @return a string describing the infeasibility of <code>tour</code>, or an empty string if the tour is feasible
     */
    public static String checkServedAssignedRequests(ITRSPTour tour) {
        if (tour.getInstance().getSimulator().isStaticSetting())
            return "";

        StringBuilder sb = new StringBuilder();
        ITourIterator tIt = tour.iterator();
        TRSPTourIterator refIt = tour.getInstance().getSimulator().getCurrentSolution()
                .getTour(tour.getTechnicianId()).iterator();

        while (refIt.hasNext()) {
            int refn = refIt.next();
            if (!tIt.hasNext()) {
                append(sb, "%s:%s@%s", tour.getTechnicianId(), "null", refn);
                break;
            }
            int tn = tIt.next();
            if (tn != refn)
                append(sb, "%s:%s@%s", tour.getTechnicianId(), tn, refn);
        }

        return sb.toString();
    }

    /**
     * Check the feasibility of a solution regarding requests that have already been served or assigned
     * 
     * @param solution
     *            the solution to be checked
     * @return a string describing the infeasibility of <code>solution</code>, or an empty string if the solution is
     *         feasible
     */
    public static String checkServedAssignedRequests(TRSPSolution solution) {
        StringBuilder sb = new StringBuilder();
        for (TRSPTour t : solution) {
            String e = checkServedAssignedRequests(t);
            if (e.length() > 0)
                append(sb, e);
        }
        return sb.toString();
    }
}