/**
 * 
 */
package vroom.trsp.datamodel.costDelegates;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.Utilities.Math.DeviationMeasure;
import vroom.common.utilities.optimization.IMove;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.TRSPMove;

/**
 * <code>TRSPTourBalance</code> is a specialization of {@link TRSPCostDelegate} that measures the balancing of tours
 * depending on a given metric
 * <p>
 * Creation date: May 18, 2011 - 2:22:00 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPTourBalance extends TRSPCostDelegate {

    public static boolean sPenaliseBalInInsertion = false;

    private final double  mPenaltyWeight;

    /**
     * Returns the weight of the disbalance penalty that will be added to the initial objective function.
     * <p>
     * A value of {@link Double#NaN} indicates that the final cost is equal to the disblance value
     * </p>
     * 
     * @return the weight of the disbalance penalty that will be added to the initial objective function.
     */
    public double getPenaltyWeight() {
        return mPenaltyWeight;
    }

    private final DeviationMeasure mMeasure;

    private final TRSPCostDelegate mTourCostDelegate;

    /**
     * Returns the {@link TRSPCostDelegate cost delegate} used to evaluate each tour
     * 
     * @return the {@link TRSPCostDelegate cost delegate} used to evaluate each tour
     */
    public TRSPCostDelegate getTourCostDelegate() {
        return mTourCostDelegate;
    }

    /**
     * Creates a new <code>TRSPTourBalance</code> based on {@link TRSPWorkingTime}
     */
    public TRSPTourBalance() {
        this(new TRSPWorkingTime(), DeviationMeasure.MaxAbsDev);
    }

    /**
     * Creates a new <code>TRSPTourBalance</code>
     * 
     * @param baseCostDelegate
     *            the cost delegate that will be used to measure the cost of each tour
     */
    public TRSPTourBalance(TRSPCostDelegate baseCostDelegate, DeviationMeasure measure) {
        this(baseCostDelegate, measure, Double.NaN);
    }

    /**
     * Creates a new <code>TRSPTourBalance</code>
     * 
     * @param baseCostDelegate
     *            the cost delegate that will be used to measure the cost of each tour
     * @param measure
     *            the DeviationMeasure to be used to measure the disbalance between tours
     * @param penaltyWeight
     *            the weight given to the disbalance measure in the final cost, {@link Double#NaN} if the original
     *            objective function is to be ignored and replaced by the disbalance measure
     */
    public TRSPTourBalance(TRSPCostDelegate baseCostDelegate, DeviationMeasure measure,
            double penaltyWeight) {
        mTourCostDelegate = baseCostDelegate;
        mMeasure = measure;
        mPenaltyWeight = penaltyWeight;
    }

    @Override
    protected double evaluateTRSPTour(TRSPTour tour, int node, boolean updateTour) {
        return mTourCostDelegate.evaluateTRSPTour(tour, node, updateTour);
    }

    @Override
    protected double evaluateGenericTour(ITRSPTour tour) {
        return mTourCostDelegate.evaluateGenericTour(tour);
    }

    /**
     * Returns an array containing the costs of all tours in {@code  solution}
     * 
     * @param solution
     *            the studied solution
     * @param evaluateTours
     *            {@code true} if tours are to be reevaluated, {@code false} to used the stored value
     * @param updateTours
     *            {@code true} if the tours are to be updated when reevaluated
     * @return an array containing the costs of all tours in {@code  solution}
     * @see #evaluateTour(ITRSPTour, boolean)
     */
    private double[] evaluateTours(TRSPSolution solution, boolean evaluateTours, boolean updateTours) {
        double[] costs = new double[solution.getTourCount()];
        for (int t = 0; t < costs.length; t++)
            costs[t] = evaluateTours ? evaluateTour(solution.getTour(t), updateTours) : solution
                    .getTour(t).getTotalCost();
        return costs;
    }

    /**
     * Returns the main objective value for the given {@code  costs}, or {@code  0} if this component is ignored
     * 
     * @param costs
     * @return the main objective value for the given {@code  costs}
     */
    private double evaluateFinalCost(double[] costs) {
        double mainObj = 0;
        double disbalance = Utilities.Math.deviation(mMeasure, costs);
        if (!Double.isNaN(getPenaltyWeight())) {
            disbalance *= getPenaltyWeight();
            for (double c : costs)
                mainObj += c;
        }
        return mainObj + disbalance;
    }

    @Override
    public double evaluateSolution(TRSPSolution solution, boolean evaluateTours, boolean updateTours) {
        double[] costs = evaluateTours(solution, evaluateTours, updateTours);
        return evaluateFinalCost(costs) + evaluatePenalty(solution);
    }

    @Override
    public double evaluateDetour(ITRSPTour tour, int i, int n, int j, boolean isRemoval) {
        double cost = mTourCostDelegate.evaluateDetour(tour, i, n, j, isRemoval);

        if (!sPenaliseBalInInsertion)
            return cost;

        double[] costs = evaluateTours(tour.getSolution(), false, false);

        costs[tour.getTechnicianId()] += cost;

        return evaluateFinalCost(costs);
    }

    @Override
    public double evaluateMove(IMove move) throws UnsupportedOperationException {
        if (move instanceof TRSPMove) {
            double cost = mTourCostDelegate.evaluateMove(move);
            if (!InsertionMove.class.isAssignableFrom(move.getClass()) || sPenaliseBalInInsertion) {
                double[] costs = evaluateTours(((TRSPMove) move).getTour().getSolution(), false,
                        false);

                double prev = evaluateFinalCost(costs);

                costs[((TRSPMove) move).getTour().getTechnicianId()] += cost;

                double imp = prev - evaluateFinalCost(costs);
                move.setImprovement(imp);
                return imp;
            } else {
                return cost;
            }
        } else {
            throw new IllegalArgumentException("Unsupported move: " + move);
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s %s)", super.toString(), mMeasure, mTourCostDelegate.getClass()
                .getSimpleName());
    }

    @Override
    public boolean isInsertionSeqDependent() {
        return mTourCostDelegate.isInsertionSeqDependent();
    }

}
