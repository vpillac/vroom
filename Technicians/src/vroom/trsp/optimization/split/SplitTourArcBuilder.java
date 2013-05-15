/**
 * 
 */
package vroom.trsp.optimization.split;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.utilities.optimization.IConstraint;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSimpleTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;
import vroom.trsp.optimization.constraints.SparePartsConstraint;
import vroom.trsp.optimization.constraints.TWConstraint;
import vroom.trsp.optimization.constraints.ToolsConstraint;
import vroom.trsp.util.TRSPGlobalParameters;

/**
 * The class <code>SplitTourArcBuilder</code> is responsible for the creation of the arcs of the Split auxiliary graph.
 * In particular it allows incremental construction and evaluation of arcs.
 * <p>
 * Creation date: Sep 27, 2011 - 4:16:36 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SplitTourArcBuilder {

    private final TRSPGlobalParameters         mParameters;
    private final boolean                      mCVRPTW;

    private final ConstraintHandler<ITRSPTour> mConstraints;

    private final TRSPCostDelegate             mCostDelegate;

    private SplitTourArc                       mArc;

    private boolean                            mTWChecked;

    /**
     * Returns the arc that were generated during the last call to {@link #buildArc(TRSPSimpleTour, int, int)}
     * 
     * @return the arc that were generated during the last call to {@link #buildArc(TRSPSimpleTour, int, int)}
     */
    public SplitTourArc getArc() {
        return mArc;
    }

    /**
     * Sets the arc
     * 
     * @param arc
     */
    void setArc(SplitTourArc arc) {
        mArc = arc;
    }

    /**
     * Returns the constraint handler defined in this instance
     * 
     * @return the constraint handler defined in this instance
     */
    protected ConstraintHandler<ITRSPTour> getConstraints() {
        return mConstraints;
    }

    /**
     * Returns the cost delegate used in this instance
     * 
     * @return the cost delegate used in this instance
     */
    protected TRSPCostDelegate getCostDelegate() {
        return mCostDelegate;
    }

    /**
     * Creates a new <code>SplitTourArcBuilder</code>
     * 
     * @param constraints
     * @param costDelegate
     * @param parameters
     */
    public SplitTourArcBuilder(ConstraintHandler<ITRSPTour> constraints, TRSPCostDelegate costDelegate,
            TRSPGlobalParameters parameters) {
        super();
        mConstraints = constraints;
        mCostDelegate = costDelegate;
        mParameters = parameters;
        mCVRPTW = mParameters.isCVRPTW();
    }

    /**
     * Build a arc of the split procedure auxiliary graph
     * 
     * @param giantTour
     *            the giant tour being split
     * @param start
     *            the index of the arc tail in the giant tour
     * @param end
     *            the index of the arc head in the giant tour
     * @return an arc corresponding to the section of the <code>giantTour</code> delimited by <code>start</code> and
     *         <code>end</code>, or <code>null</code> if such arc does not exist
     */
    public SplitTourArc buildArc(ITRSPTour giantTour, int start, int end) {
        mTWChecked = false;
        setArc(new SplitTourArc(giantTour, start, end));

        // Add a visit to the main depot if required
        boolean feasible = fixArc();

        if (!feasible)
            setArc(null);

        return getArc();
    }

    /**
     * Check that the arc represent a tour that is feasible with regard to the tools and spare part constraints, and
     * insert a visit to the main depot if needed</p>
     * <p>
     * This method is called by {@link #buildArc(TRSPSimpleTour, int, int)} to add a visit to the main depot when
     * required
     * </p>
     * <p>
     * Implementations should ensure that the constraints are only enforced if they are present in the
     * {@linkplain #getConstraints() constraint delegate}
     * </p>
     * 
     * @return <code>true</code> if the resulting arc is feasible, <code>false</code> otherwise
     */
    protected boolean fixArc() {
        boolean checkTools = false;
        boolean checkParts = false;

        int failureIdx = getArc().length();
        int toolsFailure = failureIdx, partsFailure = failureIdx;
        for (IConstraint<?> ctr : getConstraints())
            if (ToolsConstraint.class.isAssignableFrom(ctr.getClass()))
                checkTools = true;
            else if (SparePartsConstraint.class.isAssignableFrom(ctr.getClass()))
                checkParts = true;
        TRSPInstance instance = getArc().getInstance();
        Technician tech = instance.getTechnician(getArc().getTechnicianId());
        if (checkTools) {
            for (int i = 0; i < getArc().length(); i++) {
                int node = getArc().getNodeAt(i);
                if (!instance.hasRequiredTools(getArc().getTechnicianId(), node)) {
                    // Found a request that cannot be serviced
                    toolsFailure = i;
                    break;
                }
            }
        }

        if (checkParts) {
            int[] parts = tech.getSpareParts();
            for (int i = 0; i < toolsFailure && i < partsFailure; i++) {
                for (int p = 0; p < parts.length; p++) {
                    parts[p] -= instance.getSparePartReq(getArc().getNodeAt(i), p);
                    if (parts[p] < 0) {
                        // Found a request that cannot be serviced
                        partsFailure = i;
                        if (mCVRPTW)
                            return false;
                        break;
                    }
                }

            }
        }

        failureIdx = Math.min(partsFailure, toolsFailure);

        if (failureIdx < getArc().length()) {
            // A visit to the depot must be inserted before failureIdx
            TRSPInstance ins = getArc().getInstance();
            int mainDepot = ins.getMainDepot().getID();

            // Initialize the data structures for TW checking for main depot insertion
            // We will also check the tour TW, set the flag to true to prevent second checking
            mTWChecked = true;
            // Array of latest feasible arrival times
            double[] latest = new double[getArc().length()];
            // Array of arrival times
            double[] arrival = new double[getArc().length()];
            // Array of slack times
            double[] waiting = new double[getArc().length()];

            // ---------------------------------------------
            // Evaluate arrival times
            arrival[0] = getArc().getInstance().getTimeWindow(getArc().getFirstNode()).startAsDouble();
            for (int i = 1; i < getArc().length(); i++) {
                int pred = getArc().getNodeAt(i - 1);
                int node = getArc().getNodeAt(i);
                arrival[i] = ins.calculateArrivalTime(node, pred, arrival[i - 1], getArc().getTechnicianId());

                if (!ins.getTimeWindow(node).isFeasible(arrival[i]))
                    // A TW is violated
                    // The tour is not feasible
                    return false;
            }

            // ---------------------------------------------
            // Evaluate latest feasible arrival times and cumulated waiting
            latest[getArc().length() - 1] = ins.getTimeWindow(getArc().getLastNode()).endAsDouble();
            waiting[getArc().length() - 1] = 0;
            for (int i = latest.length - 2; i > 0; i--) {
                int succ = getArc().getNodeAt(i + 1);
                int node = getArc().getNodeAt(i);
                latest[i] = Math.min( //
                        // TW End at i
                        ins.getTimeWindow(node).endAsDouble(),
                        // Latest at i+1 - (service + travel time)
                        latest[i + 1] - ins.getServiceTime(node)
                                - ins.getCostDelegate().getTravelTime(node, succ, tech));
                waiting[i] = waiting[i + 1] + ins.getTimeWindow(succ).getWaiting(arrival[i + 1]);
                if (latest[i] < ins.getTimeWindow(node).startAsDouble())
                    // The latest feasible arrival time is before the start of the node TW
                    // The tour is infeasible
                    return false;
            }

            // Find the cheapest feasible insertion point
            double min = Double.POSITIVE_INFINITY;
            int insIdx = SplitTourArc.DEPOT_NOT_VISITED;
            for (int i = 1; i <= failureIdx; i++) {
                int pred = getArc().getNodeAt(i - 1);
                int succ = getArc().getNodeAt(i);
                // Insertion cost
                double cost = evaluateInsertionCost(mainDepot, i, arrival, waiting);
                if (cost < min) {
                    // Cheapest insertion
                    // Check feasibility
                    // Arrival time at the main depot
                    double newArrival = ins.calculateArrivalTime(mainDepot, pred, arrival[i - 1], getArc()
                            .getTechnicianId());
                    // Check depot TW
                    if (!ins.getTimeWindow(mainDepot).isFeasible(newArrival))
                        continue;
                    // Arrival time at the depot successor
                    newArrival = ins.calculateArrivalTime(succ, mainDepot, newArrival, getArc().getTechnicianId());
                    // Check arrival time
                    if (newArrival < latest[i]) {
                        // The insertion is feasible
                        min = cost;
                        insIdx = i;
                    }
                }
            }

            if (insIdx != SplitTourArc.DEPOT_NOT_VISITED) {
                // A feasible insertion has been found
                getArc().setDepotVisitIndex(insIdx);
            } else {
                // No feasible insertion, arc is infeasible
                return false;
            }
        }

        // if (!getConstraints().isFeasible(getArc())) {
        // TRSPLogging
        // .getOptimizationLogger()
        // .warn("SplitTourArcBuilder.fixArc: arc is infeasible (toolFailure:%s partFailure:%s failure:%s): %s (%s)",
        // toolsFailure, partsFailure, failureIdx,
        // getConstraints().getInfeasibilityExplanation(getArc()), getArc());
        // }

        return true;
    }

    /**
     * Evaluate the cost of inserting <code>node</code> at the index <code>idx</code> of the current arc.
     * 
     * @param node
     * @param idx
     * @param arrival
     * @param waiting
     * @return the insertion cost
     */
    protected double evaluateInsertionCost(int node, int idx, double[] arrival, double[] waiting) {
        final int pred = getArc().getNodeAt(idx - 1);
        final int succ = getArc().getNodeAt(idx);
        TRSPInstance instance = getArc().getInstance();
        if (getCostDelegate() instanceof TRSPWorkingTime) {
            // Faster implementation for the WT
            // Arrival time at the main depot
            double newArrival = instance.calculateArrivalTime(node, pred, arrival[idx - 1], getArc().getTechnicianId());
            // Arrival time at the successor
            newArrival = instance.calculateArrivalTime(succ, node, newArrival, getArc().getTechnicianId());

            // New arrival time at the last node
            return Math.max(0, newArrival - arrival[idx] - waiting[idx - 1]);
        } else {
            return getCostDelegate().evaluateDetour(getArc(), pred, node, succ, false);
        }
    }

    /**
     * Evaluate the cost of an arc
     * 
     * @return the cost of the arc
     */
    public double evaluateArc() {
        return getCostDelegate().evaluateTour(getArc(), true);
    }

    /**
     * Check the feasibility of an arc
     * 
     * @return <code>true</code> if the arc is feasible, <code>false</code> otherwise
     */
    public boolean isFeasible() {
        for (IConstraint<ITRSPTour> ctr : getConstraints()) {
            if ((mTWChecked && ctr instanceof TWConstraint) || ctr instanceof ToolsConstraint
                    || ctr instanceof SparePartsConstraint)
                // Ignore constraints that were already checked in the construction
                continue;
            else if (!ctr.isFeasible(getArc()))
                return false;

        }
        return true;
    }
}
