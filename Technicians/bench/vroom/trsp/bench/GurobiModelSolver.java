/**
 *
 */
package vroom.trsp.bench;

import gurobi.GRB;
import gurobi.GRB.CharAttr;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBColumn;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import vroom.common.utilities.gurobi.GRBEnvProvider;
import vroom.common.utilities.gurobi.GRBStatCollector;
import vroom.common.utilities.gurobi.GRBUtilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>GurobiModelSolver</code> is a class that will load a previously saved model and attempt to solve if
 * <p>
 * Creation date: Aug 23, 2011 - 3:07:18 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GurobiModelSolver {

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, false);

        try {
            GRBEnvProvider.getEnvironment().readParams("config/gurobi/grb_bench.env");

            GRBModel model;

            System.out.println("== SET COVERING ===========================");
            System.out.println("---- Without All Tech ------------------------");
            model = readModel("tmp/TRSPTestBase.lp", "tmp/TRSPTestBase.mst", false);
            // model = readModel("tmp/TRSPTestBase.lp", null, false);
            model.setCallback(new GRBStatCollector("gurobi/grb_stats_relaxed.csv",
                    "GurobiModelSolver"));
            solveRelaxedModel(model);
            printSolution(model);
            // System.out.println("");
            // System.out.println("---- With All Tech ---------------------------");
            // solveSetCoveringModel(model);
            // addEmptyTours(model);
            // System.out.println("");
            // System.out.println("---- Without UB ---------------------------");
            // model = readModel("tmp/trsp_model.lp", "tmp/trsp_model_start.mst", false);
            // solveSetCoveringModel(model);
            // System.out.println("");
            // System.out.println("---- With UB ------------------------------");
            // model = readModel("tmp/trsp_model.lp", "tmp/trsp_model_start.mst", true);
            // solveSetCoveringModel(model);
            // System.out.println("");
            //
            // System.out.println("== SET PARTITIONING =======================");
            // System.out.println("---- Without UB ---------------------------");
            // model = readModel("tmp/trsp_model.lp", "tmp/trsp_model_start.mst", false);
            // solveSetPartitioningModel(model);
            // System.out.println("---- With UB ------------------------------");
            // model = readModel("tmp/trsp_model.lp", "tmp/trsp_model_start.mst", true);
            // System.out.println("");
            // solveSetPartitioningModel(model);

            // System.out.println("== PRESOLVE PARTITIONING + COVERING =======");
            // System.out.println("---- Without UB ---------------------------");
            // model = readModel("tmp/trsp_model.lp", "tmp/trsp_model_start.mst", false);
            // solveSetMixedModel(model);
            // System.out.println("---- With UB ------------------------------");
            // model = readModel("tmp/trsp_model.lp", "tmp/trsp_model_start.mst", true);
            // solveSetMixedModel(model);

        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    public static void printSolution(GRBModel model) {
        try {
            Map<Integer, Integer> techUtil = new HashMap<Integer, Integer>();

            GRBVar[] vars = model.getVars();
            double[] x = model.get(DoubleAttr.X, vars);
            String[] names = model.get(StringAttr.VarName, vars);

            for (int i = 0; i < x.length; i++) {
                if (x[i] > 0) {
                    System.out.println(GRBUtilities.varToString(vars[i]));
                    int tech = Integer.valueOf(names[i].split("-")[2]);
                    Integer util = techUtil.get(tech);
                    if (util == null)
                        util = 1;
                    else
                        util++;
                    techUtil.put(tech, util);
                }
            }

            ArrayList<Integer> tech = new ArrayList<Integer>(techUtil.keySet());
            Collections.sort(tech);
            for (Integer t : tech) {
                System.out.printf("Technician %s: %s\n", t, techUtil.get(t));
            }
        } catch (GRBException e) {
            TRSPLogging.getBaseLogger().exception("GurobiModelSolver.printSolution", e);
        }

    }

    public static GRBModel solveModel(String model, String start) throws GRBException {
        GRBModel mdl = readModel(model, start, true);

        mdl.optimize();

        return mdl;
    }

    public static GRBModel solveRelaxedModel(GRBModel model) throws GRBException {
        GRBConstr[] ctr = model.getConstrs();

        for (GRBConstr c : ctr) {
            String name = c.get(StringAttr.ConstrName);
            if (name.startsWith("tech")) {
                model.remove(c);
                System.out.println("Removed constraint " + name);
            }
        }

        model.update();
        model.optimize();

        return model;
    }

    public static GRBModel solveSetPartitioningModel(GRBModel model) throws GRBException {
        setCoveringConstraintsSense(model, GRB.EQUAL);

        model.optimize();

        return model;
    }

    public static GRBModel solveSetCoveringModel(GRBModel model) throws GRBException {
        setCoveringConstraintsSense(model, GRB.GREATER_EQUAL);

        model.optimize();

        return model;
    }

    public static GRBModel solveSetMixedModel(GRBModel model) throws GRBException {
        setCoveringConstraintsSense(model, GRB.EQUAL);

        GRBModel presolved = model.presolve();

        setCoveringConstraintsSense(presolved, GRB.GREATER_EQUAL);

        presolved.optimize();

        return presolved;
    }

    /**
     * Set the sense of the set covering constraints
     * 
     * @param model
     *            the model to be modified
     * @param sense
     *            the sense of constraints (GRB.EQUAL or GRB.GREATER_EQUAL)
     * @throws GRBException
     */
    public static void setCoveringConstraintsSense(GRBModel model, char sense) throws GRBException {
        GRBConstr[] ctr = model.getConstrs();
        String[] names = model.get(StringAttr.ConstrName, ctr);

        // Change all constraints to sense
        for (int i = 0; i < ctr.length; i++) {
            if (names[i].startsWith("cover"))
                ctr[i].set(CharAttr.Sense, sense);
        }

        model.update();
    }

    /**
     * Add empty tours to the model and change the technician constraint sense to GRB.EQUAL
     * 
     * @param model
     *            the model to be edited
     * @throws GRBException
     */
    public static void addEmptyTours(GRBModel model) throws GRBException {
        GRBConstr[] ctr = model.getConstrs();
        String[] names = model.get(StringAttr.ConstrName, ctr);

        // Change all constraints to sense
        ArrayList<GRBConstr> techConstr = new ArrayList<GRBConstr>(25);
        for (int i = 0; i < ctr.length; i++) {
            if (names[i].startsWith("tech"))
                techConstr.add(ctr[i]);
        }

        int techCount = techConstr.size();
        double lb[] = new double[techCount];
        double ub[] = new double[techCount];
        char type[] = new char[techCount];
        double c[] = new double[techCount];
        String n[] = new String[techCount];
        GRBColumn col[] = new GRBColumn[techCount];

        for (int i = 0; i < techCount; i++) {
            lb[i] = 0;
            ub[i] = 1;
            type[i] = GRB.BINARY;
            c[i] = 0;
            n[i] = techConstr.get(i).get(StringAttr.ConstrName).replace("tech", "dummy");
            col[i] = new GRBColumn();
            col[i].addTerm(1, techConstr.get(i));

            techConstr.get(i).set(CharAttr.Sense, GRB.EQUAL);
        }

        model.addVars(lb, ub, c, type, n, col);

        model.update();
    }

    /**
     * Read a model from a file
     * 
     * @param modelFile
     *            the file containing the model
     * @param startFile
     *            the file containing start values for the solver (optional)
     * @param addUB
     *            <code>true</code> if the number of technicians should be limited to the one used in the initial
     *            solution
     * @return the model
     * @throws GRBException
     */
    public static GRBModel readModel(String modelFile, String startFile, boolean addUB)
            throws GRBException {
        GRBModel mdl = new GRBModel(GRBEnvProvider.getEnvironment(), modelFile);

        if (startFile != null)
            mdl.read(startFile);

        if (addUB && startFile != null) {
            // Add an upper bound on the number of technicians used
            GRBVar vars[] = mdl.getVars();
            double init[] = mdl.get(DoubleAttr.Start, vars);
            double coef[] = new double[init.length];

            double count = 0;

            for (int i = 0; i < init.length; i++) {
                if (init[i] == 1)
                    count++;
                coef[i] = 1;
            }

            GRBLinExpr ub = new GRBLinExpr();
            ub.addTerms(coef, vars);
            mdl.addConstr(ub, GRB.LESS_EQUAL, count, "ub-tech");
        }

        mdl.update();

        return mdl;
    }

}
