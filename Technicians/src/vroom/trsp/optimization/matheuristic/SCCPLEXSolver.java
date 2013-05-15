/**
 *
 */
package vroom.trsp.optimization.matheuristic;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.Collection;
import java.util.List;

import vroom.common.utilities.lp.SolverStatus;
import vroom.trsp.datamodel.ITRSPSolutionHasher;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>SCCPLEXSolver</code> ontains the logic to create a set covering model for the TRSP based on a
 * collection of routes.
 * <p>
 * Creation date: Aug 23, 2011 - 6:18:29 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SCCPLEXSolver extends SCSolverBase {

    private final IloCplex mModel;

    public SCCPLEXSolver(TRSPInstance instance, TRSPGlobalParameters parameters,
            ITRSPSolutionHasher hasher, boolean twoPhases) {
        super(instance, null);

        IloCplex model = null;
        try {
            model = new IloCplex();
            model.setName("CPLEXModel_" + instance.getName());
        } catch (IloException e) {
            TRSPLogging.getBaseLogger().exception(
                    "SCCPLEXSolver.TRSPSetCoveringCPLEXSolver", e);
        }
        mModel = model;
    }

    /* (non-Javadoc)
     * @see vroom.trsp.optimization.matheuristic.SCSolverBase#solve()
     */
    @Override
    public SolverStatus solve() {
        return null;
    }

    /* (non-Javadoc)
     * @see vroom.trsp.optimization.matheuristic.SCSolverBase#setIncumbent(vroom.trsp.datamodel.TRSPSolution)
     */
    @Override
    public boolean setIncumbent(TRSPSolution incumbent) {
        return false;
    }

    /* (non-Javadoc)
     * @see vroom.trsp.optimization.matheuristic.SCSolverBase#addColumns(java.util.Collection)
     */
    @Override
    public boolean addColumns(Collection<ITRSPTour> tours) {
        return false;

    }

    /* (non-Javadoc)
     * @see vroom.trsp.optimization.matheuristic.SCSolverBase#addCoveringConstraints(java.util.List, boolean)
     */
    @Override
    public boolean addCoveringConstraints(List<TRSPRequest> requests, boolean equal) {
        return false;

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}
