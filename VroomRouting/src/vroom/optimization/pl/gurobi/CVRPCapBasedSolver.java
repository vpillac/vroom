/*
 * 
 */
package vroom.optimization.pl.gurobi;

import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;

/**
 * <code>CVRPSolvergetCapacity()</code> is a solver for the CVRP problem based on a formulation that keeps track of the
 * load at each node.
 * <p>
 * Creation date: Aug 18, 2010 - 11:25:30 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CVRPCapBasedSolver extends CVRPSolverBase {

    protected GRBVar[] mLoadVars;

    /**
     * Creates a new <code>CVRPSolvergetCapacity()</code>
     * 
     * @param output
     * @throws GRBException
     */
    public CVRPCapBasedSolver(boolean output) throws GRBException {
        super(false, output);
    }

    @Override
    protected void addVariables() throws GRBException {
        super.addVariables();

        // Vehicle load : load[i] stores the load up to client i
        mLoadVars = new GRBVar[getSize() - 1];
        for (int i = 1; i < getSize(); i++) {
            mLoadVars[i - 1] = getModel().addVar(getDemands()[i], getCapacity(), 0, GRB.CONTINUOUS,
                    String.format("load(%s)", i));
        }
    }

    @Override
    protected void addConstraints() throws GRBException {
        super.addConstraints();

        // If i is the first client of a tour, then load(i)=dem(i)
        // load(i) <= getCapacity() + (dem(i)-getCapacity())*prec(1,i)
        // load(i) - (dem(i)-getCapacity())*prec(1,i) <= getCapacity()
        for (int i = 1; i < getSize(); i++) {
            GRBLinExpr l = new GRBLinExpr();

            l.addTerm(1, mLoadVars[i - 1]);
            l.addTerm(getCapacity() - getDemands()[i], getArcVars()[getArcIdx(0, i)]);

            getModel().addConstr(l, GRB.LESS_EQUAL, getCapacity(),
                    String.format("resetLoad(%s)", i));
        }

        // If j comes just after i in a tour, then load(j) is greater than the
        // quantity delivered during the tour up to i plus the quantity to be
        // delivered at j (to avoid loops and keep getCapacity()acity limit of the tanker)
        // load(j) >= load(i) + dem(j) - getCapacity() + getCapacity()*prec(i,j) +
        // (getCapacity()-dem(j)-dem(i))*prec(j,i)
        // - load(j) + load(i) + getCapacity()*prec(i,j) + (getCapacity()-dem(j)-dem(i))*prec(j,i) <= getCapacity() -dem
        // (j)
        for (int i = 1; i < getSize(); i++) {
            for (int j = 1; j < getSize(); j++) {
                if (i != j) {
                    GRBLinExpr e = new GRBLinExpr();

                    e.addTerm(-1, mLoadVars[j - 1]);
                    e.addTerm(1, mLoadVars[i - 1]);
                    e.addTerm(getCapacity(), getArcVars()[getArcIdx(i, j)]);
                    e.addTerm(getCapacity() - getDemands()[i] - getDemands()[j],
                            getArcVars()[getArcIdx(j, i)]);

                    getModel().addConstr(e, GRB.LESS_EQUAL, getCapacity() - getDemands()[j],
                            String.format("load(%s,%s)", i, j));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see vroom.optimization.pl.gurobi.CVRPSolverBase#isSolutionFeasible()
     */
    @Override
    public boolean isSolutionFeasible() {
        return true;
    }

    @Override
    public void printSolution(boolean printVariables) {
        if (printVariables) {
            double[] vars = null;
            try {
                vars = getModel().get(DoubleAttr.X, mLoadVars);
            } catch (GRBException e) {
                e.printStackTrace();
            } finally {
                if (vars == null) {
                    System.err.println("Solution is not available");
                    super.printSolution(printVariables);
                    return;
                }
            }

            for (int i = 0; i < vars.length; i++) {
                if (vars[i] > 0) {
                    System.out.printf("load(%s)=%.3f\n", i + 1, vars[i]);
                }
            }
        }

        super.printSolution(printVariables);
    }

    @Override
    public int getIterations() {
        return -1;
    }

}
