/**
 *
 */
package vroom.trsp.optimization.matheuristic;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBColumn;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.gurobi.GRBEnvProvider;
import vroom.common.utilities.gurobi.GRBUtilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.lp.SolverStatus;
import vroom.trsp.datamodel.ITRSPSolutionHasher;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.NodeSetSolutionHasher;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSimpleTour;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>SCGurobiSolver</code> contains the logic to create a set covering model for the TRSP based on a collection of
 * routes.
 * <p>
 * Creation date: Aug 11, 2011 - 3:35:09 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SCGurobiSolver extends SCSolverBase {

    private final boolean  mCVRPSolver;

    private int            mColumnCount;

    /** The Model. */
    private final GRBModel mModel;

    /**
     * Gets the model.
     * 
     * @return the model
     */
    public GRBModel getModel() {
        return mModel;
    }

    /**
     * Returns the number of columns (tours) in this model
     * 
     * @return the number of columns (tours) in this model
     */
    public int getColumnCount() {
        return mColumnCount;
    }

    /** An array containing the covering constraints associated with each request. */
    private GRBConstr[]               mCoverCtrs;

    /** A constraint limiting the number of selected columns (fleet size) */
    private GRBConstr                 mFleetSizeCtr;

    /** An array containing the constraints associated with each technician. */
    private GRBConstr[]               mTechCtrs;

    /** An array containing all the columns. */
    private TourColumn[]              mColumns;

    /** An array containing the technician dummy tour columns */
    private TourColumn[]              mDummyCols;

    /** An array containing all the variables. */
    private GRBVar[]                  mVariables;

    /** An array containing the slack variables for the two-phase approach */
    private GRBVar[]                  mSlackVars;

    /** <code>true</code> if the model should be solved in two phases */
    private final boolean             mTwoPhases;

    /** The hasher that was used to hash tours */
    private final ITRSPSolutionHasher mHasher;

    /** flags defining whether or not cover constraints are used */
    private final boolean[]           mCoverUseds;

    /** flags defining whether or not technicians are used */
    private final boolean[]           mTechUseds;

    /**
     * Instantiates a new tRSP set covering gurobi solver.
     * 
     * @param instance
     *            the instance
     * @param twoPhases
     *            <code>true</code> if the model should be solved in two phases: i) maximize number of served requests,
     *            ii) minimize cost
     * @param hasher
     *            the hasher that will be used to hash tours
     */
    public SCGurobiSolver(TRSPInstance instance, TRSPGlobalParameters parameters,
            ITRSPSolutionHasher hasher, boolean twoPhases) {
        super(instance, parameters);
        mColumnCount = 0;
        mCVRPSolver = getParameters().isCVRPTW();
        mTwoPhases = twoPhases;

        GRBModel m = null;
        try {
            m = new GRBModel(GRBEnvProvider.getEnvironment());
            m.set(StringAttr.ModelName, "GRBModel_" + instance.getName());
        } catch (GRBException e) {
            TRSPLogging.getOptimizationLogger().exception(
                    "SCGurobiSolver.TRSPSetCoveringGurobiSolver", e);
        }
        mModel = m;

        mHasher = hasher;

        mCoverCtrs = new GRBConstr[getInstance().getMaxId()];
        mCoverUseds = new boolean[mCoverCtrs.length];

        mTechCtrs = mCVRPSolver ? null : new GRBConstr[getInstance().getFleet().size()];
        mTechUseds = mCVRPSolver ? null : new boolean[mTechCtrs.length];

        mColumns = new TourColumn[0];
        mDummyCols = new TourColumn[0];
        mVariables = new GRBVar[0];

        // addCoveringConstraints(getInstance().getRequests(), false);
        addCoveringConstraints(getInstance().getRequests(),
                parameters.get(TRSPGlobalParameters.SC_FORCE_EQUAL));
        addFleetSizeConstraint();
        if (!mCVRPSolver)
            addTechConstraints();
        try {
            getModel().update();
        } catch (GRBException e) {
            TRSPLogging.getOptimizationLogger().exception(
                    "SCGurobiSolver.TRSPSetCoveringGurobiSolver", e);
        }
    }

    @Override
    public boolean addCoveringConstraints(List<TRSPRequest> requests, boolean forceEqual) {
        int count = requests.size();

        mSlackVars = null;
        if (mTwoPhases) {
            // Add the required slack variables

            forceEqual = true;

            double[] lb = new double[count];
            double[] ub = new double[count];
            double[] obj = new double[count];
            char[] type = new char[count];
            String[] names = new String[count];
            int i = 0;
            for (TRSPRequest r : requests) {
                lb[i] = 0;
                ub[i] = 1;
                obj[i] = 1;
                type[i] = GRB.CONTINUOUS;
                names[i] = "slack-" + r.getID();
                i++;
            }

            try {
                mSlackVars = getModel().addVars(lb, ub, obj, type, names);
                getModel().update();
            } catch (GRBException e) {
                TRSPLogging.getBaseLogger().exception("SCGurobiSolver.addCoveringConstraints", e);
            }
        }

        char c = forceEqual ? GRB.EQUAL : GRB.GREATER_EQUAL;
        GRBLinExpr[] lhsExprs = new GRBLinExpr[count];
        char[] senses = new char[count];
        double[] rhsVals = new double[count];
        String[] name = new String[count];

        int i = 0;
        for (TRSPRequest r : requests) {
            lhsExprs[i] = new GRBLinExpr();
            if (mTwoPhases) {
                lhsExprs[i].addTerm(-1, mSlackVars[i]);
            }
            // Add coefficient if existing columns cover this request
            senses[i] = c;
            rhsVals[i] = mTwoPhases ? 0 : 1;
            name[i] = "cover-" + r.getID();
            i++;
        }

        try {
            GRBConstr[] cons = getModel().addConstrs(lhsExprs, senses, rhsVals, name);
            i = 0;
            // Copy the constraints to the internal array
            for (TRSPRequest r : requests) {
                saveCoverConstraint(cons[i++], r.getID());
            }
        } catch (GRBException e) {
            TRSPLogging.getOptimizationLogger().exception("SCGurobiSolver.addCoveringConstraints",
                    e);
            return false;
        }
        return true;
    }

    private void addFleetSizeConstraint() {
        mFleetSizeCtr = null;
        if (getInstance().getFleet().isUnlimited() )
            return;

        GRBLinExpr exp = new GRBLinExpr();
        try {
            mFleetSizeCtr = getModel().addConstr(exp, GRB.LESS_EQUAL,//
                    getInstance().getFleet().size(), //
                    "fleetSize");
        } catch (GRBException e1) {
            TRSPLogging.getBaseLogger().exception("SCGurobiSolver.addFleetSizeConstraint", e1);
        }

    }

    /**
     * Add constraints ensuring that each technician does at most one tour
     */
    private void addTechConstraints() {
        // Add one constraint per technician
        GRBLinExpr[] lhsExprs = new GRBLinExpr[getInstance().getFleet().size()];
        char[] senses = new char[getInstance().getFleet().size()];
        double[] rhsVals = new double[getInstance().getFleet().size()];
        String[] name = new String[getInstance().getFleet().size()];
        // Add one empty tour per technician to ensure feasibility
        ArrayList<ITRSPTour> dummyTours = new ArrayList<ITRSPTour>(getInstance().getFleet().size());
        mDummyCols = new TourColumn[getInstance().getFleet().size()];
        int offset = mColumns.length;

        int i = 0;
        for (Technician tec : getInstance().getFleet()) {
            lhsExprs[i] = new GRBLinExpr();
            senses[i] = GRB.EQUAL;
            rhsVals[i] = 1;
            name[i] = "tech-" + tec.getID();
            i++;
            dummyTours.add(new TRSPSimpleTour(tec.getID(), getInstance()));
        }

        try {
            GRBConstr[] cons = getModel().addConstrs(lhsExprs, senses, rhsVals, name);
            i = 0;
            // Copy the constraints to the internal array
            for (Technician tec : getInstance().getFleet()) {
                // Safety check, increase array size if needed
                if (mTechCtrs.length <= tec.getID())
                    mTechCtrs = Arrays.copyOf(mTechCtrs, tec.getID() + 1);
                mTechCtrs[tec.getID()] = cons[i++];
            }

            getModel().update();
            // Add columns for dummy tours
            addColumns(dummyTours);
            for (int col = 0; col < mDummyCols.length; col++) {
                TourColumn dummyCol = mColumns[col + offset];
                mDummyCols[dummyCol.getTour().getTechnicianId()] = dummyCol;
            }
        } catch (GRBException e) {
            TRSPLogging.getOptimizationLogger().exception("SCGurobiSolver.addTechConstraints", e);
        }

    }

    @Override
    public boolean addColumns(Collection<ITRSPTour> tours) {
        double[] lb = new double[tours.size()];
        double[] ub = new double[tours.size()];
        double[] obj = new double[tours.size()];
        char[] type = new char[tours.size()];
        String[] names = new String[tours.size()];
        GRBColumn[] col = new GRBColumn[tours.size()];

        int i = 0;
        for (ITRSPTour tour : tours) {
            lb[i] = 0;
            ub[i] = 1;
            obj[i] = mTwoPhases ? 0 : tour.getTotalCost();
            type[i] = GRB.BINARY;
            names[i] = String.format("tour-%s-%s", mColumns.length + i, tour.getTechnicianId());
            col[i] = new GRBColumn();

            // Add a coefficient of 1 in each constraint corresponding to a visited request
            for (int reqId : tour) {
                if (mCoverCtrs[reqId] != null) {
                    col[i].addTerm(1, mCoverCtrs[reqId]);
                    mCoverUseds[reqId] = true;
                }
            }
            // Add a coefficient of 1 in each technician constraint
            if (!mCVRPSolver) {
                col[i].addTerm(1, mTechCtrs[tour.getTechnicianId()]);
                mTechUseds[tour.getTechnicianId()] = true;
            }

            // Add a coefficient of 1 in the fleet size constraint
            if (mFleetSizeCtr != null) {
                col[i].addTerm(1, mFleetSizeCtr);
            }

            i++;
        }

        try {
            GRBVar[] vars = getModel().addVars(lb, ub, obj, type, names, col);
            // Store all new columns in the internal array
            int offset = mColumns.length;
            ensureColumnArrayCapacity(tours.size());
            i = 0;
            for (ITRSPTour tour : tours) {
                int id = offset + i;
                mColumns[id] = new TourColumn(vars[i], id, tour);
                mVariables[id] = vars[i];
                i++;
            }
            getModel().update();

            mColumnCount += tours.size();
        } catch (GRBException e) {
            TRSPLogging.getOptimizationLogger().exception("SCGurobiSolver.addColumns", e);
            return false;
        }
        return true;
    }

    /**
     * Check if all requests are covered by at least one tour and that all technicians are used, and log warning
     * messages if any incoherence is found
     * 
     * @return <code>true</code> if all requests are covered by at least one tour and that all technicians are used
     */
    public boolean checkModel() {
        boolean ok = true;
        for (TRSPRequest r : getInstance().getRequests()) {
            if (!mCoverUseds[r.getID()]) {
                TRSPLogging
                        .getOptimizationLogger()
                        .warn("SCGurobiSolver.addColumns: no column covering request %s, removing the corresponding constraint",
                                r);
                try {
                    mModel.remove(mCoverCtrs[r.getID()]);
                } catch (GRBException e) {
                    TRSPLogging
                            .getOptimizationLogger()
                            .exception(
                                    "SCGurobiSolver.checkModel exception caught while removing covering constraint %s",
                                    e, r.getID());
                }
                ok = false;
            }
        }

        if (mCVRPSolver)
            return ok;

        for (int t = 0; t < mTechUseds.length; t++) {
            if (!mTechUseds[t]) {
                TRSPLogging.getOptimizationLogger().warn(
                        "SCGurobiSolver.addColumns: no column using technician %s", t);
                ok = false;
            }
        }
        return ok;
    }

    @Override
    public boolean setIncumbent(TRSPSolution incumbent) {
        // Add all tours to the model
        Collection<ITRSPTour> tours = new ArrayList<ITRSPTour>(incumbent.getTourCount());
        for (TRSPTour tour : incumbent) {
            tours.add(tour);
        }
        addColumns(tours);

        double unset = incumbent.getUnservedRequests().isEmpty() ? 0 : GRB.UNDEFINED;
        // double unset = GRB.UNDEFINED;
        Stopwatch timer = new Stopwatch();

        timer.start();
        Set<Integer> sol = new HashSet<Integer>((int) (getInstance().getFleet().size() / 0.75) + 1,
                0.75f);
        for (TRSPTour t : incumbent) {
            if (t.length() > 2 || !t.getInstance().isDepot(t.getFirstNode())
                    || !t.getInstance().isDepot(t.getLastNode()))

                sol.add(mHasher.hash(t));
        }

        Set<Integer> unusedTech = new HashSet<Integer>();
        for (Technician tech : getInstance().getFleet()) {
            unusedTech.add(tech.getID());
        }
        int count = 0;
        double[] start = new double[mColumns.length];
        LinkedList<Integer> cols = new LinkedList<Integer>();
        for (int i = 0; i < mColumns.length && !sol.isEmpty(); i++) {
            if (sol.contains(mColumns[i].getHash())) {
                // This column is present in the solution
                start[i] = 1;
                sol.remove(mColumns[i].getHash());
                count++;
                cols.add(mColumns[i].getHash());
                unusedTech.remove(mColumns[i].getTour().getTechnicianId());
            } else {
                start[i] = unset;
            }
        }

        // Select dummy tours for unused technicians
        if (!mCVRPSolver) {
            for (int tech : unusedTech) {
                start[mDummyCols[tech].getIndex()] = 1;
            }
        }

        timer.stop();

        TRSPLogging.getOptimizationLogger().info(
                "SCGurobiSolver.setIncumbent: selected %s columns for %s technicians (%sms - %s)",
                count, getInstance().getFleet().size(), timer.readTimeMS(), cols);

        if (!sol.isEmpty()) {
            TRSPLogging
                    .getOptimizationLogger()
                    .warn("SCGurobiSolver.setIncumbent: %s tours from the incumbent were not present in the pool (%s)",
                            sol.size(), Utilities.toShortString(sol));
            // Unset the values that were previously set to 0
            if (unset != GRB.UNDEFINED) {
                for (int i = 0; i < start.length; i++) {
                    if (start[i] != 1)
                        start[i] = GRB.UNDEFINED;
                }
            }
        }

        try {
            getModel().set(DoubleAttr.Start, mVariables, start);
            getModel().update();
        } catch (GRBException e) {
            TRSPLogging.getOptimizationLogger().exception("SCGurobiSolver.setIncumbent", e);
            return false;
        }
        return true;
    }

    public void logRemovedRows() {
        if (TRSPLogging.getOptimizationLogger().isEnabledFor(LoggerHelper.LEVEL_DEBUG)) {
            try {
                GRBModel presolved = getModel().presolve();

                Set<String> remRows = GRBUtilities.getRemovedRows(getModel(), presolved);
                for (String row : remRows) {
                    TRSPLogging.getOptimizationLogger().debug(
                            "SCGurobiSolver.logRemovedRows: presolve removed row %s", row);

                }

            } catch (GRBException e) {
                TRSPLogging.getOptimizationLogger().exception("SCGurobiSolver.logRemovedRows", e);
            }
        }
    }

    /**
     * Solve the current set covering model and return the corresponding solution
     * 
     * @return the solution found by the solver, or null if no solution was found
     */
    @Override
    public SolverStatus solve() {
        setSolution(null);

        TRSPLogging.getOptimizationLogger().info(
                "SCGurobiSolver.solve: solving a sub-problem with %s columns", mColumns.length);

        try {
            // getModel().set(DoubleAttr.ObjBound, upperBound);
            if (mTwoPhases) {
                TRSPLogging
                        .getOptimizationLogger()
                        .info("SCGurobiSolver.solve: first phase - minimizing number of unserved requests");
                getModel().set(IntAttr.ModelSense, -1);
            }

            mTimer.reset();
            mTimer.start();

            getModel().optimize();

            mStatus = GRBUtilities.convertStatus(getModel().get(IntAttr.Status));
            int solCount = getModel().get(IntAttr.SolCount);

            if (mTwoPhases && solCount > 0) {
                // Switch to second phase
                double obj = getModel().get(DoubleAttr.ObjVal);
                TRSPLogging.getOptimizationLogger().info(
                        "SCGurobiSolver.solve: first phase over - %.0f/%s served requests", obj,
                        getInstance().getRequestCount());

                // Adding a constraint on the number of unserved customers
                GRBLinExpr exp = new GRBLinExpr();
                double[] coef = new double[mSlackVars.length];
                for (int i = 0; i < coef.length; i++) {
                    coef[i] = 1;
                }
                exp.addTerms(coef, mSlackVars);
                getModel().addConstr(exp, GRB.EQUAL, obj, "unserved");

                // Setting the cost of slack variables to 0
                double[] slackCost = new double[mSlackVars.length];
                getModel().set(DoubleAttr.Obj, mSlackVars, slackCost);

                // Setting the cost of tour variables to the corresponding tour cost
                double[] cost = new double[mColumns.length];
                for (int i = 0; i < cost.length; i++) {
                    cost[i] = mColumns[i].mTour.getTotalCost();
                }
                getModel().set(DoubleAttr.Obj, mVariables, cost);
                getModel().set(IntAttr.ModelSense, 1);

                // Setting the initial value of variables
                GRBVar[] vars = getModel().getVars();
                double[] start = getModel().get(DoubleAttr.X, vars);
                getModel().set(DoubleAttr.Start, vars, start);

                getModel().update();

                // Solving the second phase
                TRSPLogging.getOptimizationLogger().info(
                        "SCGurobiSolver.solve: second phase - minimizing the total cost");
                getModel().optimize();

                mStatus = GRBUtilities.convertStatus(getModel().get(IntAttr.Status));
                solCount = getModel().get(IntAttr.SolCount);
            }

            if (solCount > 0) {
                buildSolution();
            }

        } catch (GRBException e) {
            TRSPLogging.getOptimizationLogger().exception("SCGurobiSolver.solve", e);
        } finally {
            mTimer.stop();
        }

        return mStatus;
    }

    /**
     * Build a solution to the original problem from the solution of the SC model
     * 
     * @throws GRBException
     */
    protected void buildSolution() throws GRBException {
        setSolution(new TRSPSolution(getInstance(), getParameters().newSCCostDelegate()));
        double[] values = getModel().get(DoubleAttr.X, mVariables);
        int k = 0;
        for (int colIdx = 0; colIdx < values.length; colIdx++) {
            if (Math.abs(1 - values[colIdx]) < ZERO_TOLERANCE) {
                // The corresponding tour is selected
                ITRSPTour itour = mColumns[colIdx].getTour();

                int techId = mCVRPSolver ? k : itour.getTechnicianId();

                TRSPTour tour = getSolution().getTour(techId);
                if (tour.length() > 0) {
                    TRSPLogging
                            .getOptimizationLogger()
                            .warn("SCGurobiSolver.buildSolution: a tour was already present in the solution (%s)",
                                    tour);
                }
                // Append all nodes from the tour
                ITourIterator it = itour.iterator();
                int node;
                while (it.hasNext()) {
                    node = it.next();

                    if (mCVRPSolver) {
                        // Fix possible incoherences
                        if (getInstance().isMainDepot(node))
                            node = tour.getMainDepotId();
                        else if (getInstance().isDepot(node)) {
                            if (node < getInstance().getDepotCount())
                                node = tour.getTechnician().getHome().getID();
                            else
                                node = getInstance().getHomeDuplicate(
                                        tour.getTechnician().getHome().getID());
                        }
                    }

                    TRSPTour otherTour = getSolution().getVisitingTour(node);
                    if (otherTour == null || otherTour == tour) {
                        tour.appendNode(node);
                    } else {
                        // The node is already visited, find in which tour its visit is less expensive
                        int otherPred = otherTour.getPred(node);
                        int otherSucc = otherTour.getSucc(node);
                        double otherDetour = otherTour.getCostDelegate().evaluateDetour(otherTour,
                                otherPred, node, otherSucc, true);

                        int nodeRealId = it.previous();
                        int pred = it.previous();
                        it.next();
                        it.next();
                        int succ = it.next();
                        it.previous(); // Reset the iterator to the good position
                        double detour = tour.getCostDelegate().evaluateDetour(itour, pred,
                                nodeRealId, succ, true);

                        if (otherDetour > detour) {
                            otherTour.removeNode(node);
                            tour.appendNode(node);
                        }// else: skip this node
                    }
                }

                for (int req : itour) {
                    getSolution().markAsServed(req);
                }
                k++;
                if (mCVRPSolver && k > getInstance().getFleet().size()) {
                    TRSPLogging
                            .getOptimizationLogger()
                            .warn("SCGurobiSolver.buildSolution: the solution is using too many vehicles");
                    k = 0;
                }
            }
        }
    }

    /**
     * Increase the size of the columns and variables array by <code>cap</code>
     * 
     * @param cap
     *            the required capacity increase
     */
    private void ensureColumnArrayCapacity(int cap) {
        mColumns = Arrays.copyOf(mColumns, mColumns.length + cap);
        mVariables = Arrays.copyOf(mVariables, mVariables.length + cap);
    }

    /**
     * Save a covering constraint in the internal data structure.
     * 
     * @param ctr
     *            the ctr
     * @param reqId
     *            the req id
     */
    private void saveCoverConstraint(GRBConstr ctr, int reqId) {
        mCoverCtrs[reqId] = ctr;
        // For dynamic mode: check indices and expand array if needed
    }

    /**
     * <code>TourColumn</code> is a container class that represents a column in the {@link SCGurobiSolver} model. It
     * contains the actual {@link GRBVar}, the column index, and a reference to the corresponding {@link TRSPTour}.
     * <p>
     * Creation date: Aug 11, 2011 - 4:28:52 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected class TourColumn {

        /** The corresponding gurobi variable. */
        private final GRBVar    mVar;

        /** The column index. */
        private final int       mIndex;

        /** The tour represented by this column. */
        private final ITRSPTour mTour;

        /** The hash of the tour as calculated by {@link NodeSetSolutionHasher} */
        private final int       mHash;

        /**
         * Gets the corresponding gurobi variable.
         * 
         * @return the corresponding gurobi variable
         */
        protected GRBVar getVar() {
            return mVar;
        }

        /**
         * Gets the column index.
         * 
         * @return the column index
         */
        protected int getIndex() {
            return mIndex;
        }

        /**
         * Gets the tour represented by this column.
         * 
         * @return the tour represented by this column
         */
        protected ITRSPTour getTour() {
            return mTour;
        }

        /**
         * Gets the hash of the tour represented by this column.
         * 
         * @return the hash of the tour represented by this column
         */
        protected int getHash() {
            return mHash;
        }

        /**
         * Instantiates a new tour column.
         * 
         * @param var
         *            the corresponding gurobi variable
         * @param index
         *            the column index
         * @param tour
         *            the tour represented by this column
         */
        protected TourColumn(GRBVar var, int index, ITRSPTour tour) {
            super();
            mVar = var;
            mIndex = index;
            mTour = tour;
            mHash = mHasher.hash(tour);
        }

    }

    @Override
    protected void finalize() throws Throwable {
        mModel.dispose();
        super.finalize();
    }

    /**
     * Dispose this object to help the garbage collector freeing up memory
     */
    @Override
    public void dispose() {
        mModel.dispose();
        mTechCtrs = null;
        mColumns = null;
        mVariables = null;
        mCoverCtrs = null;
    }

}
