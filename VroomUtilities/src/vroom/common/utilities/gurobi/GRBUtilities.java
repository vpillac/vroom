/**
 *
 */
package vroom.common.utilities.gurobi;

import gurobi.GRB;
import gurobi.GRB.CharAttr;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import vroom.common.utilities.lp.SolverStatus;

/**
 * <code>GRBUtilities</code> is a class providing convenience methods when working with gurobi models.
 * <p>
 * Creation date: Jul 6, 2010 - 4:58:26 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBUtilities {

    /**
     * Returns a string describing the given variable
     * 
     * @param var
     * @return a string describing the given variable
     */
    public static String varToString(GRBVar var) {
        try {
            return String.format("%s [%s,%s] c=%.3f: %s", var.get(StringAttr.VarName), var.get(DoubleAttr.LB),
                    var.get(DoubleAttr.UB), var.get(DoubleAttr.Obj), var.get(CharAttr.VType));
        } catch (GRBException e) {
            return String.format("%s:%s", var, e.getMessage());
        }
    }

    /**
     * Returns a string describing the given constraint
     * 
     * @param ctr
     * @param model
     * @return a string describing the given constraint
     */
    public static String constrToString(GRBConstr ctr, GRBModel model) {
        StringBuilder row = new StringBuilder();
        for (GRBVar v : model.getVars()) {
            try {
                double coef = model.getCoeff(ctr, v);

                if (coef != 0) {
                    row.append(String.format("%s*%s ", coefToString(coef), v.get(StringAttr.VarName)));
                }

            } catch (GRBException e) {
                row.append(e.getMessage());
            }
        }

        try {
            return String.format("%s\t: %s %s %s", ctr.get(StringAttr.ConstrName), row, ctr.get(CharAttr.Sense),
                    ctr.get(DoubleAttr.RHS));
        } catch (GRBException e) {
            return String.format("%s:%s", ctr, e.getMessage());
        }
    }

    /**
     * Returns a string describing the given coefficient
     * 
     * @param coef
     * @return a string describing the given coefficient
     */
    public static String coefToString(double coef) {
        String sign = "";
        if (coef > 0) {
            sign = "+";
        }
        return String.format("%s%s", sign, coef);
    }

    /**
     * <ul>
     * <li>LOADED Model is loaded, but no solution information is available.</li>
     * <li>OPTIMAL Model was solved to optimality (subject to tolerances), and an optimal solution is available.</li>
     * <li>INFEASIBLE Model was proven to be infeasible.</li>
     * <li>INF_OR_UNBD Model was proven to be either infeasible or unbounded.</li>
     * <li>UNBOUNDED Model was proven to be unbounded.</li>
     * <li>CUTOFF Optimal objective for model was proven to be worse than the value specified in the Cutoff parameter.
     * No solution information is available.</li>
     * <li>ITERATION_LIMIT Optimization terminated because the total number of simplex iterations performed exceeded the
     * value specified in the IterationLimit parameter.</li>
     * <li>NODE_LIMIT Optimization terminated because the total number of branch-and-cut nodes explored exceeded the
     * value specified in the NodeLimit parameter.</li>
     * <li>TIME_LIMIT Optimization terminated because the time expended exceeded the value specified in the TimeLimit
     * parameter.</li>
     * <li>SOLUTION_LIMIT Optimization terminated because the number of solutions found reached the value specified in
     * the SolutionLimit parameter.</li>
     * <li>INTERRUPTED Optimization was terminated by the user.</li>
     * <li>NUMERIC Optimization was terminated due to unrecoverable numerical difficulties.</li>
     * <ul/>
     * 
     * @param status
     * @return a string describing the given status code
     */
    public static String statusToString(int status) {
        switch (status) {
        case GRB.LOADED:
            return "LOADED";
        case GRB.OPTIMAL:
            return "OPTIMAL";
        case GRB.INFEASIBLE:
            return "INFEASIBLE";
        case GRB.INF_OR_UNBD:
            return "INF_OR_UNBD";
        case GRB.UNBOUNDED:
            return "UNBOUNDED";
        case GRB.CUTOFF:
            return "CUTOFF";
        case GRB.ITERATION_LIMIT:
            return "ITERATION_LIMIT";
        case GRB.NODE_LIMIT:
            return "NODE_LIMIT";
        case GRB.TIME_LIMIT:
            return "TIME_LIMIT";
        case GRB.SOLUTION_LIMIT:
            return "SOLUTION_LIMIT";
        case GRB.INTERRUPTED:
            return "INTERRUPTED";
        case GRB.NUMERIC:
            return "NUMERIC";
        default:
            return "UNKNOWN_STATUS";
        }
    }

    /**
     * <ul>
     * <li>LOADED Model is loaded, but no solution information is available.</li>
     * <li>OPTIMAL Model was solved to optimality (subject to tolerances), and an optimal solution is available.</li>
     * <li>INFEASIBLE Model was proven to be infeasible.</li>
     * <li>INF_OR_UNBD Model was proven to be either infeasible or unbounded.</li>
     * <li>UNBOUNDED Model was proven to be unbounded.</li>
     * <li>CUTOFF Optimal objective for model was proven to be worse than the value specified in the Cutoff parameter.
     * No solution information is available.</li>
     * <li>ITERATION_LIMIT Optimization terminated because the total number of simplex iterations performed exceeded the
     * value specified in the IterationLimit parameter.</li>
     * <li>NODE_LIMIT Optimization terminated because the total number of branch-and-cut nodes explored exceeded the
     * value specified in the NodeLimit parameter.</li>
     * <li>TIME_LIMIT Optimization terminated because the time expended exceeded the value specified in the TimeLimit
     * parameter.</li>
     * <li>SOLUTION_LIMIT Optimization terminated because the number of solutions found reached the value specified in
     * the SolutionLimit parameter.</li>
     * <li>INTERRUPTED Optimization was terminated by the user.</li>
     * <li>NUMERIC Optimization was terminated due to unrecoverable numerical difficulties.</li>
     * <ul/>
     * 
     * @param status
     * @return a string describing the given status code
     */
    public static SolverStatus convertStatus(int status) {
        switch (status) {
        case GRB.LOADED:
            return SolverStatus.LOADED;
        case GRB.OPTIMAL:
            return SolverStatus.OPTIMAL;
        case GRB.INFEASIBLE:
            return SolverStatus.INFEASIBLE;
        case GRB.INF_OR_UNBD:
            return SolverStatus.INF_OR_UNBD;
        case GRB.UNBOUNDED:
            return SolverStatus.UNBOUNDED;
        case GRB.CUTOFF:
            return SolverStatus.CUTOFF;
        case GRB.ITERATION_LIMIT:
            return SolverStatus.ITERATION_LIMIT;
        case GRB.NODE_LIMIT:
            return SolverStatus.NODE_LIMIT;
        case GRB.TIME_LIMIT:
            return SolverStatus.TIME_LIMIT;
        case GRB.SOLUTION_LIMIT:
            return SolverStatus.SOLUTION_LIMIT;
        case GRB.INTERRUPTED:
            return SolverStatus.INTERRUPTED;
        case GRB.NUMERIC:
            return SolverStatus.NUMERIC;
        default:
            return SolverStatus.UNKNOWN_STATUS;
        }
    }

    /**
     * Returns a string describing the current solver status
     * 
     * @param model
     * @return a string describing the current solver status
     */
    public static String solverStatusString(GRBModel model) {
        int status = -1;
        int solCount = -1;
        double obj = Double.NaN;

        try {
            status = model.get(IntAttr.Status);
        } catch (GRBException e) {
        }

        try {
            solCount = model.get(IntAttr.SolCount);
        } catch (GRBException e) {
        }

        try {
            obj = model.get(DoubleAttr.ObjVal);
        } catch (GRBException e) {
        }

        return String.format("Status:%s SolCount:%s Objective:%s", statusToString(status), solCount, obj);
    }

    /**
     * Human friendly description of the where value in a callback
     * 
     * @param where
     *            the value of the <code>where</code> field in the callback
     * @return a description of the where value
     */
    public static String callbackWhereDescription(int where) {
        switch (where) {
        case GRB.Callback.POLLING:
            return "Periodic polling callback";
        case GRB.Callback.PRESOLVE:
            return "Currently performing presolve";
        case GRB.Callback.SIMPLEX:
            return "Currently in simplex";
        case GRB.Callback.MIP:
            return "Currently in MIP";
        case GRB.Callback.MIPSOL:
            return "Found a new MIP incumbent";
        case GRB.Callback.MIPNODE:
            return "Currently exploring a MIP node";
        case GRB.Callback.BARRIER:
            return "Currently in barrier";
        case GRB.Callback.MESSAGE:
            return "Printing a log message";
        default:
            return "Not a valid value";
        }
    }

    /**
     * Name of the where value in a callback
     * 
     * @param where
     *            the value of the <code>where</code> field in the callback
     * @return the name of the GRB.Callback constant corresponding to the where value
     */
    public static String callbackWhereToString(int where) {
        switch (where) {
        case GRB.Callback.POLLING:
            return "POLLING";
        case GRB.Callback.PRESOLVE:
            return "PRESOLVE";
        case GRB.Callback.SIMPLEX:
            return "SIMPLEX";
        case GRB.Callback.MIP:
            return "MIP";
        case GRB.Callback.MIPSOL:
            return "MIPSOL";
        case GRB.Callback.MIPNODE:
            return "MIPNODE";
        case GRB.Callback.BARRIER:
            return "BARRIER";
        case GRB.Callback.MESSAGE:
            return "MESSAGE";
        default:
            return "UNKNOWN";
        }
    }

    /**
     * Returns a set containing the name of the columns removed by the presolve
     * 
     * @param model
     *            the original model
     * @param presolved
     *            the presolved model
     * @return a set containing the name of the columns removed by the presolve
     */
    public static Set<String> getRemovedColumns(GRBModel model, GRBModel presolved) {
        String[] colNames;
        GRBVar[] preCols;
        try {
            colNames = model.get(StringAttr.VarName, model.getVars());

            preCols = presolved.getVars();
        } catch (GRBException e) {
            return Collections.singleton(e.getMessage());
        }

        Set<String> remCols = new HashSet<String>(colNames.length);
        for (int r = 0; r < colNames.length; r++) {
            try {
                preCols[r].get(StringAttr.VarName);
            } catch (GRBException e) {
                remCols.add(colNames[r]);
            }
        }

        return remCols;
    }

    /**
     * Returns a set containing the name of the rows removed by the presolve
     * 
     * @param model
     *            the original model
     * @param presolved
     *            the presolved model
     * @return a set containing the name of the rows removed by the presolve
     */
    public static Set<String> getRemovedRows(GRBModel model, GRBModel presolved) {
        String[] rowNames;
        GRBConstr[] preRows;
        try {
            rowNames = model.get(StringAttr.ConstrName, model.getConstrs());

            preRows = presolved.getConstrs();
        } catch (GRBException e) {
            return Collections.singleton(e.getMessage());
        }

        Set<String> remRows = new HashSet<String>(rowNames.length);
        for (int r = 0; r < rowNames.length; r++) {
            try {
                preRows[r].get(StringAttr.ConstrName);
            } catch (GRBException e) {
                remRows.add(rowNames[r]);
            }
        }

        return remRows;
    }
}
