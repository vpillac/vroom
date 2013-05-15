/**
 * 
 */
package vroom.optimization.pl.gurobi;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntAttr;
import gurobi.GRB.IntParam;
import gurobi.GRB.StringAttr;
import gurobi.GRB.StringParam;
import gurobi.GRBCallback;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.Vehicle;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.gurobi.GRBPartialSolution;
import vroom.common.utilities.gurobi.GRBUtilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.lp.SolverStatus;
import vroom.optimization.pl.IVRPSolver;

/**
 * The Class <code>CVRPSolverBase</code> is a base type for implementations of CVRP solvers using arc based formulations
 * with gurobi.
 * <p>
 * Creation date: Aug 18, 2010 - 11:19:53 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class CVRPSolverBase implements IVRPSolver {

    public static final String       OUTPUT_FILE_FORMAT_STRING = "%1$s/%2$ty%2$tm%2$td_%2$tk-%2$tM_stats.csv";

    public final static LoggerHelper LOGGER                    = LoggerHelper
                                                                       .getLogger(CVRPSolverBase.class
                                                                               .getSimpleName());

    public static final double       ZERO_TOLERANCE            = 1e-10;

    public static String             sWorkingFolder            = "./gurobi";

    private final boolean            mSymmetric;

    private boolean                  mInitialized;

    /*
     * (non-Javadoc)
     * @see vroom.optimization.pl.gurobi.VRPSolver#isInitialized()
     */
    @Override
    public boolean isInitialized() {
        return mInitialized;
    }

    private IVRPInstance mInstance;

    /*
     * (non-Javadoc)
     * @see vroom.optimization.pl.gurobi.VRPSolver#getInstance()
     */
    @Override
    public IVRPInstance getInstance() {
        return mInstance;
    }

    private int mSize;

    /**
     * Number of nodes (including the depot)
     * 
     * @return the number of nodes
     */
    public int getSize() {
        return mSize;
    }

    private GRBVar[] mArcVars;

    public GRBVar[] getArcVars() {
        return mArcVars;
    }

    /** A mapping idx->(i,j) */
    private int[][]      mArcsMap;
    /** A mapping (i,j)->idx */
    private int[][]      mArcsIdxMap;

    private double[]     mCosts;

    private INodeVisit[] mNodeMapping;

    private double[]     mDemands;

    public double[] getDemands() {
        return mDemands;
    }

    private double mTotalDemand;

    private double mCapacity;

    public double getCapacity() {
        return mCapacity;
    }

    private final GRBEnv mEnvironment;

    /**
     * Getter for the {@link GRBEnv}
     * 
     * @return the environment of this solver
     */
    public GRBEnv getEnvironment() {
        return mEnvironment;
    }

    private GRBModel mModel;

    /**
     * Getter for the {@link GRBModel}
     * 
     * @return the model
     */
    public GRBModel getModel() {
        return mModel;
    }

    private final GRBCallback mCallback;

    protected GRBCallback getCallback() {
        return mCallback;
    }

    private Vehicle     mVehicle;

    private final Stopwatch mTimer;

    protected Stopwatch getTimer() {
        return mTimer;
    }

    @Override
    public double getSolveTime() {
        return getTimer().readTimeMS();
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.pl.gurobi.VRPSolver#setTimeLimit(int)
     */
    @Override
    public void setTimeLimit(int sec) {
        mTimer.setTimout(sec * 1000);
    }

    /**
     * Creates a new Branch and Cut solver for the CVRP
     * 
     * @param output
     *            <code>true</code> if solver output should be displayed
     * @throws GRBException
     */
    public CVRPSolverBase(boolean symmetric, boolean output) throws GRBException {
        String log = String.format("%s/%s.log", sWorkingFolder, "default");
        mEnvironment = new GRBEnv(log);
        LOGGER.debug("New Gurobi environment created");
        LOGGER.info("Log file: %s", log);

        mEnvironment.set(IntParam.OutputFlag, output ? 1 : 0);
        mSymmetric = symmetric;
        mTimer = new Stopwatch();

        mCallback = newCallback();
    }

    @Override
    public void reset() {
        mArcsIdxMap = null;
        mArcsMap = null;
        mArcVars = null;
        mCapacity = 0;
        mCosts = null;
        mDemands = null;
        mInitialized = false;
        mInstance = null;
        mModel = null;
        mNodeMapping = null;
        mSize = 0;
        mTotalDemand = 0;
        mVehicle = null;
        mTimer.reset();
    }

    /*
     * (non-Javadoc)
     * @see
     * vroom.optimization.pl.gurobi.VRPSolver#readInstance(vroom.common.modeling
     * .dataModel.IVRPInstance)
     */
    @Override
    public void readInstance(IVRPInstance instance) throws GRBException {
        if (instance == getInstance()) {
            LOGGER.warn("readInstance: Instance is the same: %s", getInstance());
            return;
        }
        reset();

        try {
            if (mEnvironment.get(IntParam.OutputFlag) == 1) {
                String log = String.format("%s/%s.log", sWorkingFolder, instance.getName());
                mEnvironment.set(StringParam.LogFile, log);
                LOGGER.info("Log file changed to %s", log);
            }
        } catch (GRBException e) {
        }

        setModel(new GRBModel(mEnvironment));

        setInstance(instance);

        int nClients = getInstance().getRequestCount();
        mSize = nClients + 1;
        mVehicle = instance.getFleet().getVehicle(0);
        mCapacity = mVehicle.getCapacity();

        LOGGER.info("Reading the instance: %s clients, vehicle capacity: %s", nClients, mCapacity);

        Set<INodeVisit> requests = instance.getNodeVisits();
        INodeVisit depot = instance.getDepotsVisits().iterator().next();

        mDemands = new double[mSize];
        mNodeMapping = new INodeVisit[mSize];

        mDemands[0] = 0;
        mNodeMapping[0] = depot;

        int idx = 1;
        for (INodeVisit n : requests) {
            mDemands[idx] = n.getDemand();
            mTotalDemand += mDemands[idx];
            mNodeMapping[idx] = n;
            idx++;
        }

        addVariables();

        getModel().update();
        LOGGER.info("Model updated");

        addConstraints();

        getModel().update();
        LOGGER.info("Model updated");

        getModel().setCallback(mCallback);

        mInitialized = true;
        LOGGER.info("Solver initialized");
    }

    /**
     * Add variables to the model.
     * <p>
     * This base implementation add arc variables and initialize the cost matrix.
     * 
     * @throws GRBException
     */
    protected void addVariables() throws GRBException {
        int numArcs = mSymmetric ? mSize * (mSize - 1) / 2 : mSize * (mSize - 1);

        setArcVars(new GRBVar[numArcs]);
        mArcsMap = new int[getArcVars().length][2];
        mArcsIdxMap = new int[mSize][mSize];

        mCosts = new double[getArcVars().length];

        // Define all arc variables at once
        LOGGER.info("Adding arc variables");
        double[] lb = new double[getArcVars().length];
        double[] ub = new double[getArcVars().length];
        char[] type = new char[getArcVars().length];
        String[] names = new String[getArcVars().length];
        int idx = 0;
        for (int i = 0; i < mSize; i++) {
            int ubt = i == 0 ? 2 : 1;
            char typet = i == 0 ? GRB.INTEGER : GRB.BINARY;
            mArcsIdxMap[i][i] = -1;

            int minJ = mSymmetric ? i + 1 : 0;
            for (int j = minJ; j < mSize; j++) {
                if (i != j) {
                    mArcsMap[idx] = new int[] { i, j };
                    mArcsIdxMap[i][j] = idx;
                    if (mSymmetric) {
                        mArcsIdxMap[j][i] = idx;
                    }

                    mCosts[idx] = getInstance().getCost(mNodeMapping[i], mNodeMapping[j], mVehicle);
                    lb[idx] = 0;
                    ub[idx] = ubt;
                    type[idx] = typet;
                    names[idx] = String.format("x(%s,%s)", i, j);
                    idx++;
                }
            }
        }
        setArcVars(getModel().addVars(lb, ub, mCosts, type, names));
        LOGGER.info("%s variables added to the model", mCosts.length);

    }

    /**
     * Add constraints to the model
     * <p>
     * This base implementation adds flow constraints for each node
     * 
     * @throws GRBException
     */
    protected void addConstraints() throws GRBException {
        int ctr = 0;
        if (mSymmetric) {
            // Enter or leave every node exactly twice (except the depot)
            LOGGER.debug("Adding flow constraints");
            for (int i = 1; i < mSize; i++) {
                // An array of 1 for the sum of edges
                double[] ones = new double[getArcVars().length];

                for (int j = 0; j < mSize; j++) {
                    if (i != j) {
                        ones[getArcIdx(i, j)] = 1;
                    }
                }

                GRBLinExpr degree = new GRBLinExpr();
                degree.addTerms(ones, getArcVars());

                getModel().addConstr(degree, GRB.EQUAL, 2, String.format("degree(%s)", i));
                ctr++;
            }
            double[] ones = new double[getArcVars().length];

            for (int j = 1; j < mSize; j++) {
                ones[getArcIdx(0, j)] = 1;
            }

            GRBLinExpr degree = new GRBLinExpr();
            degree.addTerms(ones, getArcVars());

            double rhs = 2 * Math.ceil(mTotalDemand / mCapacity);
            getModel().addConstr(degree, GRB.LESS_EQUAL, 2 * mSize - 2, "degreeLB(0)");
            getModel().addConstr(degree, GRB.GREATER_EQUAL, rhs, "degreeUB(0)");
            ctr += 2;
        } else {
            // Enter and exit every node exactly one (except the depot)
            LOGGER.debug("Adding flow constraints");
            double[] onesIn;
            double[] onesOut;
            for (int i = 1; i < mSize; i++) {
                // An array of 1 for the sum of edges
                onesIn = new double[getArcVars().length];
                onesOut = new double[getArcVars().length];

                for (int j = 0; j < mSize; j++) {
                    if (i != j) {
                        onesIn[getArcIdx(i, j)] = 1;
                        onesOut[getArcIdx(j, i)] = 1;
                    }
                }

                GRBLinExpr degreeIn = new GRBLinExpr();
                degreeIn.addTerms(onesIn, getArcVars());
                getModel().addConstr(degreeIn, GRB.EQUAL, 1, String.format("degreeIn(%s)", i));
                ctr++;

                GRBLinExpr degreeOut = new GRBLinExpr();
                degreeOut.addTerms(onesOut, getArcVars());
                getModel().addConstr(degreeOut, GRB.EQUAL, 1, String.format("degreeOut(%s)", i));
                ctr++;
            }
            onesIn = new double[getArcVars().length];
            onesOut = new double[getArcVars().length];

            for (int j = 1; j < mSize; j++) {
                onesIn[getArcIdx(0, j)] = 1;
                onesOut[getArcIdx(j, 0)] = 1;
            }

            double rhs = Math.ceil(mTotalDemand / mCapacity);

            GRBLinExpr degreeIn = new GRBLinExpr();
            degreeIn.addTerms(onesIn, getArcVars());
            getModel().addConstr(degreeIn, GRB.LESS_EQUAL, mSize - 1, "degreeInUB(0)");
            getModel().addConstr(degreeIn, GRB.GREATER_EQUAL, rhs, "degreeInLB(0)");
            ctr++;

            GRBLinExpr degreeOut = new GRBLinExpr();
            degreeOut.addTerms(onesOut, getArcVars());
            getModel().addConstr(degreeOut, GRB.LESS_EQUAL, mSize - 1, "degreeOutUB(0)");
            getModel().addConstr(degreeOut, GRB.GREATER_EQUAL, rhs, "degreeOutLB(0)");
            ctr++;
        }
        LOGGER.info("%s constraints added to the model", ctr);
    }

    /**
     * Create a callback to be added to the model.
     * 
     * @return the {@link GRBCallback} to be added to the model.
     */
    protected GRBCallback newCallback() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.pl.gurobi.VRPSolver#solve()
     */
    @Override
    public SolverStatus solve() throws GRBException {
        if (!mInitialized) {
            throw new IllegalStateException("Solver is not initialized");
        }

        getTimer().start();
        // Minimize
        getModel().set(IntAttr.ModelSense, +1);
        double timeout = getTimer().getTimeout();
        getModel().getEnv().set(DoubleParam.TimeLimit, timeout);
        LOGGER.info("Starting the optimizer");
        getModel().optimize();
        getTimer().stop();
        LOGGER.info("Optimization finished in %ss: %s", getTimer().readTimeS(),
                GRBUtilities.solverStatusString(getModel()));
        return GRBUtilities.convertStatus(getModel().get(GRB.IntAttr.Status));
    }

    @Override
    public double getObjectiveValue() {
        try {
            return getModel().get(DoubleAttr.ObjVal);
        } catch (GRBException e) {
            return Double.NaN;
        }
    }

    /**
     * Prints the CVRP MIP model
     */
    public void printModel() {
        // System.out.println(this.toString());
    }

    /**
     * Translates an array of arc variables into a {@link IVRPSolution}
     * 
     * @param vars
     * @return the solution corresponing to the given variables
     */
    public IVRPSolution<? extends IRoute<?>> getSolution(double[] vars) {
        Solution<ArrayListRoute> sol = new Solution<ArrayListRoute>(getInstance());

        boolean[] marked = new boolean[mSize];
        int numMarked = 0;

        ArrayListRoute route = null;
        int next = 0;
        while (next >= 0 && numMarked < getSize() - 1) {
            // Depot: create a new route
            if (next == 0) {
                // Close current route
                if (route != null) {
                    route.appendNode(mNodeMapping[next]);
                }
                // Add a new route
                route = new ArrayListRoute(sol, mVehicle);
                sol.addRoute(route);
            }
            route.appendNode(mNodeMapping[next]);
            marked[next] = true;
            if (next != 0) {
                numMarked++;
            }
            boolean found = false;
            for (int j = 1; j < mSize; j++) {
                if (!marked[j] && vars[getArcIdx(next, j)] >= 1 - ZERO_TOLERANCE) {
                    next = j;
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (next != 0 && vars[getArcIdx(next, 0)] > 1e-10) {
                    next = 0;
                } else {
                    next = -1;
                }
            }
        }
        // Close last route
        if (route != null) {
            route.appendNode(mNodeMapping[0]);
        }

        return sol;
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.pl.gurobi.VRPSolver#getSolution()
     */
    @Override
    public IVRPSolution<? extends IRoute<?>> getSolution() {
        double[] vars = null;
        try {
            vars = getModel().get(DoubleAttr.X, getArcVars());
        } catch (GRBException e) {
            e.printStackTrace();
        } finally {
            if (vars == null) {
                LOGGER.error("Solution is not available");
                return null;
            }
        }

        return getSolution(vars);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.pl.gurobi.VRPSolver#printSolution(boolean)
     */
    @Override
    public void printSolution(boolean printVariables) {
        System.out.println("Solution");
        System.out.println(getSolution());

        if (printVariables) {
            System.out.println("Variables");
            for (int i = 0; i < getSize(); i++) {
                for (int j = mSymmetric ? i + 1 : 0; j < getSize(); j++) {
                    if (i != j) {
                        double val;
                        try {
                            val = getArcVars()[getArcIdx(i, j)].get(DoubleAttr.X);
                        } catch (GRBException e1) {
                            val = Double.NaN;
                        }
                        if (val != 0) {
                            System.out.printf("x(%s,%s) = %s\n", i, j, val);
                        }
                    }
                }
            }
        }

        if (!isSolutionFeasible()) {
            System.err.println("Infeasible solution with subtours");
        }

    }

    /**
     * Build a partial MIP solution from an infeasible integer solution.
     * <p>
     * The result can the be given to the solver as partial solution.
     * 
     * @param vars
     *            the arc vars of the solution to be repaired
     * @return a copy of the <code>vars</code> containing the value of the variables in the partial solution, or
     *         <code>-1</code> if the variable should be left unset. The last element containts the number of set
     *         variables.
     */
    public GRBPartialSolution buildPartialMIPSolution(double[] vars) {
        Set<Set<Integer>> comps = findConnectedComponents(vars);

        int varCount = vars.length;

        for (Set<Integer> comp : comps) {
            double load = 0;
            for (int node : comp) {
                load += getDemands()[node];
            }
            int rhs = (int) (2 * Math.ceil(load / getCapacity()));

            boolean connected = comp.contains(0);

            // Disconnected subtour that satisfies capacity constraint
            // Repair it by connecting it to the depot
            if (!connected && rhs == 2) {
                PriorityQueue<DepotInsertion> insertions = new PriorityQueue<DepotInsertion>();

                // Build the subtour sequence
                ArrayList<Integer> seq = new ArrayList<Integer>(comp.size() + 1);
                int last = connected ? 0 : comp.iterator().next();

                comp = new HashSet<Integer>(comp);
                seq.add(last);
                comp.remove(last);
                while (!comp.isEmpty()) {
                    int seqL = seq.size() - 1;
                    last = seq.get(seqL);
                    for (int j : comp) {
                        if (vars[getArcIdx(last, j)] >= 1 - ZERO_TOLERANCE) {
                            seq.add(j);
                            comp.remove(j);
                            if (last != 0 && j != 0) {
                                double cost = mCosts[getArcIdx(last, 0)] + mCosts[getArcIdx(0, j)]
                                        - mCosts[getArcIdx(last, j)];
                                insertions.add(new DepotInsertion(last, j, seqL, seqL + 1, cost));
                            }
                            break;
                        }
                    }
                }

                if (seq.get(seq.size() - 1) != 0 && seq.get(0) != 0) {
                    double cost = mCosts[getArcIdx(seq.get(seq.size() - 1), 0)]
                            + mCosts[getArcIdx(seq.get(0), 0)]
                            - mCosts[getArcIdx(seq.get(seq.size() - 1), seq.get(0))];
                    insertions.add(new DepotInsertion(seq.get(seq.size() - 1), seq.get(0), seq
                            .size() - 1, 0, cost));
                }

                // Connect the subtour with least cost insertion
                DepotInsertion ins = insertions.poll();
                vars[getArcIdx(ins.i, ins.j)] = 0;
                vars[getArcIdx(ins.i, 0)] = 1;
                vars[getArcIdx(0, ins.j)] = 1;
            }
            // Disconnected tour or route exceeding capacity
            // Unset the variables
            else if (rhs > 2 || !connected) {
                for (int i : comp) {
                    if (i != 0) {
                        for (int j = 0; j < getSize(); j++) {
                            if (i != j && vars[getArcIdx(i, j)] >= -ZERO_TOLERANCE) {
                                vars[getArcIdx(i, j)] = -1;
                                varCount--;

                                if (!mSymmetric && vars[getArcIdx(j, i)] >= -ZERO_TOLERANCE) {
                                    vars[getArcIdx(j, i)] = -1;
                                    varCount--;
                                }
                            }
                        }
                    }
                }
            }
        }
        GRBPartialSolution sol = new GRBPartialSolution(varCount);
        int k = 0;
        // StringBuffer s = new StringBuffer(varCount*7);
        for (int i = 0; i < vars.length; i++) {
            if (vars[i] >= -ZERO_TOLERANCE) {
                // s.append(String.format(" (%s,%s)=%s",
                // getArc(i)[0],
                // getArc(i)[1],
                // vars[i]));
                sol.getValues()[k] = Math.round(vars[i]);
                sol.getVariables()[k] = getArcVars()[i];
                k++;
            }
        }
        // System.out.println("Solution: "+s.toString());

        return sol;
    }

    /**
     * Build a partial MIP solution from an infeasible integer solution.
     * <p>
     * The result can the be given to the solver as partial solution.
     * 
     * @param vars
     *            the arc vars of the solution to be repaired
     * @return a copy of the <code>vars</code> containing the value of the variables in the partial solution, or
     *         <code>-1</code> if the variable should be left unset. The last element containts the number of set
     *         variables.
     */
    public GRBPartialSolution buildPartialMIPSolution(int num) throws GRBException {
        double[] vars = null;

        if (num > getModel().get(IntAttr.SolCount)) {
            return null;
        }

        getModel().getEnv().set(IntParam.SolutionNumber, num);
        vars = getModel().get(DoubleAttr.Xn, getArcVars());

        return buildPartialMIPSolution(vars);
    }

    /**
     * Repair a MIP solution
     * <p>
     * <b>THIS IMPLEMENTATION IS NOT EFFICIENT</b>
     * </p>
     * 
     * @param vars
     *            the arc vars of the solution to be repaired
     * @return a copy of the <code>vars</code> array containing the repaired var values and the objective function as an
     *         additional element.
     */
    public double[] repairMIPSolution(double[] vars) {
        Set<Set<Integer>> comps = findConnectedComponents(vars);

        vars = Arrays.copyOf(vars, vars.length + 1);

        boolean feasible = true;
        Set<Integer> disconnectedNodes = new HashSet<Integer>();
        for (int i = 1; i < getSize(); i++) {
            disconnectedNodes.add(i);
        }
        while (!comps.isEmpty()) {
            // for(Set<Integer> comp : comps){
            Set<Integer> comp = comps.iterator().next();
            Set<Integer> compTmp = new HashSet<Integer>(comp);
            comps.remove(comp);

            double load = 0;
            for (int node : compTmp) {
                load += getDemands()[node];
                disconnectedNodes.remove(node);
            }
            int rhs = (int) (2 * Math.ceil(load / getCapacity()));

            boolean connected = compTmp.contains(0);
            if (rhs > 2 || !connected) {
                feasible = false;

                PriorityQueue<DepotInsertion> insertions = new PriorityQueue<DepotInsertion>();

                // Build the subtour sequence
                ArrayList<Integer> seq = new ArrayList<Integer>(compTmp.size() + 1);
                ArrayList<Double> loads = new ArrayList<Double>(compTmp.size() + 1);
                int last = connected ? 0 : compTmp.iterator().next();

                seq.add(last);
                double accLoad = getDemands()[last];
                loads.add(accLoad);
                compTmp.remove(last);
                while (!compTmp.isEmpty()) {
                    int seqL = seq.size() - 1;
                    last = seq.get(seqL);
                    accLoad = loads.get(seqL);
                    for (int j : compTmp) {
                        if (vars[getArcIdx(last, j)] >= 1 - ZERO_TOLERANCE) {
                            seq.add(j);
                            accLoad += getDemands()[j];
                            loads.add(accLoad);
                            compTmp.remove(j);
                            if (last != 0 && j != 0) {
                                double cost = mCosts[getArcIdx(last, 0)] + mCosts[getArcIdx(0, j)]
                                        - mCosts[getArcIdx(last, j)];
                                insertions.add(new DepotInsertion(last, j, seqL, seqL + 1, cost));
                            }
                            break;
                        }
                    }
                }

                if (connected) {
                    seq.add(0);
                }

                if (seq.get(seq.size() - 1) != 0 && seq.get(0) != 0) {
                    double cost = mCosts[getArcIdx(seq.get(seq.size() - 1), 0)]
                            + mCosts[getArcIdx(seq.get(0), 0)]
                            - mCosts[getArcIdx(seq.get(seq.size() - 1), seq.get(0))];
                    insertions.add(new DepotInsertion(seq.get(seq.size() - 1), seq.get(0), seq
                            .size() - 1, 0, cost));
                }

                if (!connected) {
                    // Connect the subtour with least cost insertion
                    DepotInsertion ins = insertions.poll();
                    vars[getArcIdx(ins.i, ins.j)] = 0;
                    vars[getArcIdx(ins.i, 0)] = 1;
                    vars[getArcIdx(0, ins.j)] = 1;
                    // Repair the route
                    comp.add(0);
                    comps.add(comp);
                } else {
                    int r = 1;
                    for (int i = 1; i < loads.size(); i++) {
                        if (loads.get(i) > r * getCapacity()) {
                            int pred = seq.get(i - 1);
                            int succ = seq.get(i);
                            vars[getArcIdx(pred, succ)] = 0;
                            vars[getArcIdx(pred, 0)] = 1;
                            vars[getArcIdx(0, succ)] = 1;
                            r++;
                        }
                    }
                }

            }
        }

        // Reconnect disconnected edges
        // (shouldn't happen)
        if (!disconnectedNodes.isEmpty()) {
            feasible = false;
            for (int node : disconnectedNodes) {
                if (mSymmetric) {
                    vars[getArcIdx(node, 0)] = 2;
                } else {
                    vars[getArcIdx(node, 0)] = 1;
                    vars[getArcIdx(0, node)] = 1;
                }
            }
        }

        // Calculate the solution cost
        vars[vars.length - 1] = 0;
        for (int e = 0; e < vars.length - 1; e++) {
            vars[e] = Math.round(vars[e]);
            if (vars[e] >= 1) {
                vars[vars.length - 1] += mCosts[e] * vars[e];
            }
        }

        return feasible ? null : vars;
    }

    /**
     * Repair the num-th MIP solution
     * 
     * @param num
     *            the MIP solution number
     * @return an array containing the arc vars of the repaired solution
     * @throws GRBException
     * @see {@link IntParam#SolutionNumber}
     * @see {@link DoubleAttr#Xn}
     */
    public double[] repairMIPSolution(int num) throws GRBException {
        double[] vars = null;

        if (num >= getModel().get(IntAttr.SolCount)) {
            return null;
        }

        getModel().getEnv().set(IntParam.SolutionNumber, num);
        vars = getModel().get(DoubleAttr.Xn, getArcVars());

        return repairMIPSolution(vars);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.pl.gurobi.VRPSolver#isSolutionFeasible()
     */
    @Override
    public boolean isSolutionFeasible() {
        double[] vars = null;
        try {
            vars = getModel().get(DoubleAttr.X, getArcVars());
        } catch (GRBException e) {
            e.printStackTrace();
        } finally {
            if (vars == null) {
                LOGGER.error("isSolutionFeasible: Solution is not available");
                return false;
            }
        }

        return isSolutionFeasible(vars);
    }

    /**
     * Check whether or not the given solution is feasible
     * 
     * @return <code>true</code> if the current solution is feasible
     */
    public boolean isSolutionFeasible(double[] vars) {
        Set<Set<Integer>> comps = findConnectedComponents(vars);

        boolean feasible = true;
        Set<Integer> disconnectedNodes = new HashSet<Integer>();
        for (int i = 0; i < getSize(); i++) {
            disconnectedNodes.add(i);
        }
        for (Set<Integer> comp : comps) {
            double load = 0;
            for (int node : comp) {
                load += getDemands()[node];
                disconnectedNodes.remove(node);
            }

            if (!comp.contains(0)) {
                LOGGER.debug(
                        "isSolutionFeasible: Solution contains a disconected subtour (%s/%s): %s",
                        load, getCapacity(), comp);
                feasible = false;
            } else if (load > getCapacity()) {
                LOGGER.debug(
                        "isSolutionFeasible: Solution contains a route that violates the capacity (%s/%s): %s",
                        load, getCapacity(), comp);
                feasible = false;
            }
        }

        if (!disconnectedNodes.isEmpty()) {
            LOGGER.debug("isSolutionFeasible: Solution contains unconnected nodes :%s",
                    disconnectedNodes);
            feasible = false;
        }

        return feasible;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder variables = new StringBuilder();
        StringBuilder obj = new StringBuilder();
        StringBuilder constraints = new StringBuilder();

        try {
            if (getModel().get(IntAttr.ModelSense) < 0) {
                obj.append("Max ");
            } else {
                obj.append("Min ");
            }
        } catch (GRBException e1) {
        }

        GRBVar[] vars = getModel().getVars();
        GRBConstr[] ctr = getModel().getConstrs();

        // Arrays.sort(vars, new GRBVarComparator());
        // Arrays.sort(ctr, new GRBConstrComparator());

        for (GRBVar v : vars) {
            variables.append(GRBUtilities.varToString(v));
            variables.append("\n");
            try {
                obj.append(String.format("%s*%s ", v.get(DoubleAttr.Obj), v.get(StringAttr.VarName)));
            } catch (GRBException e) {
                obj.append(String.format("%s ", e.getMessage()));
            }

        }

        // variables.append("Constraints:\n");
        for (GRBConstr c : ctr) {
            constraints.append(GRBUtilities.constrToString(c, getModel()));
            constraints.append("\n");
        }

        return String.format("Variables:\n%s\n\nObj:%s\ns.t.:\n%s", variables, obj, constraints);
    }

    /**
     * Returns the id of arc (i,j)
     * 
     * @param i
     * @param j
     * @return the id of arc (i,j)
     */
    protected int getArcIdx(int i, int j) {
        return mArcsIdxMap[i][j];
    }

    /**
     * Returns the arc (i,j)
     * 
     * @param idx
     *            the index of the required arc
     * @return an array <code>[tail,head]</code> for the specified arc
     */
    protected int[] getArc(int idx) {
        return mArcsMap[idx];
    }

    /**
     * Find connected components in the given solution.
     * <p>
     * This method uses an O(n<sup>2</sup>) algorithm to fin connected components in the sub-graph containing only arcs
     * with an associated variable greater or equal to 1.
     * </p>
     * 
     * @param vars
     *            the arc variable values
     * @return a set of connected components (subtours/routes)
     */
    protected Set<Set<Integer>> findConnectedComponents(double[] vars) {
        // A list of subtours
        @SuppressWarnings("unchecked")
        Set<Integer>[] subtours = new Set[getSize()];
        // A mapping between subtours idx and actual subtours
        int[] subtourMapping = new int[getSize()];
        // The last subtour index
        int lastSubtour = -1;
        // Maps each node to a subtour
        // invariant: depot (0) is never mapped to a subtour
        int[] nodeMapping = new int[getSize()];
        for (int i = 0; i < nodeMapping.length; i++) {
            nodeMapping[i] = -1;
            subtourMapping[i] = Integer.MIN_VALUE;
        }

        // Iterate over all edges
        for (int e = 0; e < getArcVars().length; e++) {
            if (vars[e] >= 1 - ZERO_TOLERANCE) {
                int[] arc = getArc(e);
                int i = arc[0], j = arc[1];
                int ti = nodeMapping[i];
                int tj = nodeMapping[j];
                int si = getMappedSubtour(ti, subtourMapping);
                int sj = getMappedSubtour(tj, subtourMapping);

                if (si != -1 && si == sj) {
                    // System.out.printf("%s and %s already in the same subtour\n",i,j);
                } else {
                    if (ti != -1) {
                        // i is already in a subtour
                        if (tj == -1) {
                            // j was not in a subtour
                            // Add j to the subtour of i
                            if (j != 0) {
                                nodeMapping[j] = ti;
                            }
                            // System.out.printf("Add %s to subtour of %s %s\n",j,i,subtours[si]);
                            subtours[si].add(j);
                        } else {
                            // j was in a subtour
                            // Merge the two subtours
                            // System.out.printf("Merge subtour of %s and %s %s %s\n",j,i,subtours[si],subtours[sj]);
                            subtours[si].addAll(subtours[sj]);
                            subtours[sj] = null;
                            // Replace the subtour of j by the merged one
                            int refti = subtourMapping[ti] >= 0 ? -(ti + 1) : subtourMapping[ti];
                            int fwdtj = tj;
                            while (subtourMapping[fwdtj] < 0) {
                                int fwd = subtourMapping[fwdtj];
                                subtourMapping[fwdtj] = refti;
                                fwdtj = -fwd - 1;
                            }
                            subtourMapping[fwdtj] = refti;
                        }
                    } else {
                        // i was not in a subtour
                        if (tj == -1) {
                            // j was not in a subtour
                            // Add a new subtour
                            lastSubtour++;
                            if (i != 0) {
                                nodeMapping[i] = lastSubtour;
                            }
                            if (j != 0) {
                                nodeMapping[j] = lastSubtour;
                            }
                            // System.out.printf("Create new subtour with %s and %s \n",i,j);
                            Set<Integer> subtour = new HashSet<Integer>();
                            subtour.add(i);
                            subtour.add(j);
                            subtourMapping[lastSubtour] = lastSubtour;
                            subtours[lastSubtour] = subtour;
                        } else {
                            // j was in a subtour
                            // Add i to the subtour of j
                            // System.out.printf("Add %s to subtour of %s %s\n",i,j,subtours[sj]);
                            if (i != 0) {
                                nodeMapping[i] = tj;
                            }
                            subtours[sj].add(i);
                        }
                    }

                    ti = nodeMapping[i];
                    tj = nodeMapping[j];
                    si = getMappedSubtour(ti, subtourMapping);
                    sj = getMappedSubtour(tj, subtourMapping);
                }
            }
        }
        Set<Set<Integer>> comps = new HashSet<Set<Integer>>();

        for (Set<Integer> subtour : subtours) {
            if (subtour != null) {
                comps.add(subtour);
            }
        }

        return comps;
    }

    private int getMappedSubtour(int t, int[] subtourMapping) {
        if (t < 0) {
            return -1;
        } else {
            while (subtourMapping[t] < 0) {
                t = -subtourMapping[t] - 1;
            }
            return subtourMapping[t];
        }
    }

    private static class DepotInsertion implements Comparable<DepotInsertion> {
        private final int    i, j;
        @SuppressWarnings("unused")
        private final int    seqI, seqJ;
        private final double cost;

        public DepotInsertion(int i, int j, int seqI, int seqJ, double cost) {
            super();
            this.i = i;
            this.j = j;
            this.seqI = seqI;
            this.seqJ = seqJ;
            this.cost = cost;
        }

        @Override
        public int compareTo(DepotInsertion ins) {
            return Double.compare(cost , ins.cost);
        }

        @Override
        public String toString() {
            return String.format("(%s,%s)-%s", i, j, cost);
        }
    }

    public abstract int getIterations();

    private void setModel(GRBModel mModel) {
        this.mModel = mModel;
    }

    protected void setArcVars(GRBVar[] arcVars) {
        mArcVars = arcVars;
    }

    private void setInstance(IVRPInstance instance) {
        mInstance = instance;
    }
}
