package vroom.optimization.online.jmsa.vrp.vrpsd;

import vroom.common.heuristics.vls.IVLSAcceptanceCriterion;
import vroom.common.heuristics.vls.IVLSState;
import vroom.common.heuristics.vls.VLSGlobalParameters;
import vroom.common.heuristics.vls.VersatileLocalSearch;
import vroom.common.utilities.optimization.IAcceptanceCriterion;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IMove;
import vroom.common.utilities.optimization.INeighborhood;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.optimization.online.jmsa.vrp.VRPScenario;
import vroom.optimization.online.jmsa.vrp.VRPScenarioRoute;

/**
 * The Class <code>VRPSDAcceptanceCriterion</code> is an implementation of {@link IAcceptanceCriterion} and
 * {@link IVLSAcceptanceCriterion} that accepts certain deterioration in the objective function if the new solution has
 * a better distribution of load among routes
 * <p>
 * Creation date: Dec 2, 2010 - 3:17:13 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPSDAcceptanceCriterion implements IAcceptanceCriterion, IVLSAcceptanceCriterion {

    /** optimization sense **/
    private final OptimizationSense mOptimizationSense;

    /**
     * Getter for optimization sense
     * 
     * @return the optimization sense
     */
    public OptimizationSense getOptimizationSense() {
        return mOptimizationSense;
    }

    /** the cost deterioration tolerance **/
    private double mCostTolerance = 0.05;

    /**
     * Getter for the cost deterioration tolerance
     * 
     * @return the value of the cost tolerance
     */
    public double getCostTolerance() {
        return this.mCostTolerance;
    }

    /**
     * Setter for the cost deterioration tolerance
     * 
     * @param costTolerance
     *            the value to be set for the cost tolerance
     */
    public void setCostTolerance(double costTolerance) {
        this.mCostTolerance = costTolerance;
    }

    /**
     * the load increase threshold for the first route above which a deteriorating solution will be accepted (between 0
     * an 1)
     **/
    private double mLoadThreshold = 0.1;

    /**
     * Getter for the load increase threshold for the first route above which a deteriorating solution will be accepted
     * (between 0 an 1)
     * 
     * @return the value of name
     */
    public double getLoadThreshold() {
        return this.mLoadThreshold;
    }

    /**
     * Setter for the load increase threshold for the first route above which a deteriorating solution will be accepted
     * (between 0 an 1)
     * 
     * @param name
     *            the value to be set for the load increase threshold for the first route above which a deteriorating
     *            solution will be accepted (between 0 an 1)
     */
    public void setLoadThreshold(double name) {
        this.mLoadThreshold = name;
    }

    /**
     * Creates a new <code>VRPSDAcceptanceCriterion</code> for a {@link VersatileLocalSearch} procedure
     * 
     * @param params
     */
    public VRPSDAcceptanceCriterion(VLSGlobalParameters params) {
        mOptimizationSense = params.get(VLSGlobalParameters.OPTIMIZATION_DIRECTION) < 0 ? OptimizationSense.MINIMIZATION
                : OptimizationSense.MAXIMIZATION;
    }

    /**
     * Creates a new <code>VRPSDAcceptanceCriterion</code>
     * 
     * @param optimizationSense
     */
    public VRPSDAcceptanceCriterion(OptimizationSense optimizationSense) {
        mOptimizationSense = optimizationSense;
    }

    @Override
    public boolean acceptSolution(IVLSState<?> state, IInstance instance, ISolution solution) {
        return accept(state.getBestSolution(state.getCurrentPhase()), solution);
    }

    @Override
    public void initialize() {
        // Do nothing
    }

    @Override
    public void reset() {
        // Do nothing
    }

    @Override
    public boolean accept(ISolution oldSolution, ISolution newSolution) {
        VRPScenario original = (VRPScenario) oldSolution;
        VRPScenario candidate = (VRPScenario) newSolution;

        // Cost comparison
        if (candidate.getCost() < original.getCost()) {
            return true;
        }
        // Deteriorating solution
        else if (candidate.getCost() < (1 + getCostTolerance()) * original.getCost() && candidate.getRouteCount() > 0
                && original.getRouteCount() > 0) {
            // First routes
            VRPScenarioRoute or = null;
            VRPScenarioRoute cand = null;

            for (VRPScenarioRoute r : original) {
                if (r.containsShrunkNode()) {
                    or = r;
                    break;
                }
            }

            for (VRPScenarioRoute r : candidate) {
                if (r.containsShrunkNode()) {
                    cand = r;
                    break;
                }
            }
            // The candidate has a higher load on first route, which is
            // desirable
            if (or != null && cand != null
                    && (cand.getLoad() - or.getLoad()) > cand.getVehicle().getCapacity() * getLoadThreshold()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean accept(ISolution solution, IMove move) {
        return accept(solution, null, move);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean accept(ISolution solution, INeighborhood<?, ?> neighborhood, IMove move) {
        // FIXME find a more efficient implementation depending on the move
        ISolution clone = solution.clone();
        ((INeighborhood<ISolution, IMove>) neighborhood).executeMove(clone, move);
        return accept(solution, clone);
    }

    @Override
    public double getImprovement(ISolution oldSolution, ISolution newSolution) {
        return mOptimizationSense.getImprovement(oldSolution.getObjectiveValue(), newSolution.getObjectiveValue());
    }

    @Override
    public VRPSDAcceptanceCriterion clone() {
        VRPSDAcceptanceCriterion clone = new VRPSDAcceptanceCriterion(mOptimizationSense);
        clone.mCostTolerance = mCostTolerance;
        clone.mLoadThreshold = mLoadThreshold;
        return clone;
    }

}
