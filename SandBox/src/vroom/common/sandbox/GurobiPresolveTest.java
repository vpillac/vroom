package vroom.common.sandbox;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntAttr;
import gurobi.GRB.IntParam;
import gurobi.GRB.StringAttr;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GurobiPresolveTest {

    private final static GRBEnv sEnv;
    static {
        GRBEnv env;
        try {
            env = new GRBEnv("gurobi.log");
            env.set(DoubleParam.TimeLimit, 300);
        } catch (GRBException e) {
            e.printStackTrace();
            System.exit(1);
            env = null;
        }
        sEnv = env;
    }

    public static void main(String[] args) {
        testRealTRSP(0);
        testRealTRSP(1);
        testRealTRSP(2);
    }

    private static void testPresolve(boolean setStart) throws GRBException {
        if (setStart)
            System.out.println("TEST WITH START VALUES");
        else
            System.out.println("TEST WITHOUT START VALUES");

        GRBModel model = new GRBModel(sEnv);

        GRBVar x = model.addVar(0, 1, 1, GRB.BINARY, "x");
        GRBVar y = model.addVar(0, 1, 1, GRB.BINARY, "y");
        GRBVar z = model.addVar(0, 1, 1.5, GRB.BINARY, "z");

        model.update();

        GRBLinExpr cov1 = new GRBLinExpr();
        cov1.addTerm(1, x);
        cov1.addTerm(1, z);
        model.addConstr(cov1, GRB.GREATER_EQUAL, 1, "cov1");

        GRBLinExpr cov2 = new GRBLinExpr();
        cov2.addTerm(1, y);
        cov2.addTerm(1, z);
        model.addConstr(cov2, GRB.GREATER_EQUAL, 1, "cov2");

        GRBLinExpr res = new GRBLinExpr();
        res.addTerm(1, y);
        res.addTerm(1, z);
        model.addConstr(res, GRB.LESS_EQUAL, 1, "res");

        model.update();

        if (setStart) {
            model.set(DoubleAttr.Start, new GRBVar[] { x, y }, new double[] { 1, 1 });
        }

        model.write("gurobi/test_set_covering.lp");
        model.presolve().write(
                "gurobi/test_set_covering_presolve-" + (setStart ? "start" : "") + ".lp");

        model.optimize();
    }

    private static void testRealTRSP(int presolve) {
        System.out.println("TEST WITH PRESOLVE = " + presolve);
        System.out.println("==================================================");

        try {
            sEnv.set(IntParam.Presolve, presolve);

            GRBModel model = new GRBModel(sEnv, "gurobi/trsp_model.lp");
            model.set(StringAttr.ModelName, "test_pres" + presolve);

            GRBVar[] vars = model.getVars();

            model.read("gurobi/trsp_model_start.mst");
            checkStartSolution(model, 100);

            model.optimize();

            // Print first solution
            int solCount = model.get(IntAttr.SolCount);
            if (solCount > 0) {
                sEnv.set(IntParam.SolutionNumber, solCount - 1);
                double[] vals = model.get(DoubleAttr.Xn, vars);
                String[] names = model.get(StringAttr.VarName, vars);
                for (int i = 0; i < vars.length; i++) {
                    if (vals[i] > 1e-3)
                        System.out.printf("%s %s\n", names[i], vals[i]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("==================================================");
    }

    public static boolean checkStartSolution(GRBModel model, int nodes) throws GRBException {
        boolean[] visited = new boolean[nodes];

        GRBConstr[] ctr = model.getConstrs();
        GRBVar[] vars = model.getVars();
        double[] starts = model.get(DoubleAttr.Start, vars);

        for (int i = 0; i < starts.length; i++) {
            if (starts[i] == 1) {
                for (int n = 0; n < visited.length; n++) {
                    if (model.getCoeff(ctr[n], vars[i]) == 1)
                        visited[n] = true;
                }
            }
        }

        boolean ok = true;
        for (int n = 0; n < visited.length; n++) {
            if (!visited[n]) {
                ok = false;
                System.out.printf("Unvisited node: %s (%s)\n", (n + 26),
                        ctr[n].get(StringAttr.ConstrName));
            }
        }

        if (ok)
            System.out.println("Initial solution is feasible");
        else
            System.out.println("Initial solution is not feasible");

        return ok;
    }

}
