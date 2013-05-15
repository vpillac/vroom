package vroom.trsp.datamodel.costDelegates;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.optimization.InsertionMove;

/**
 * The class <code>NoisyCostDelegate</code> is a wrapper around a {@link TRSPCostDelegate} that adds noise to insertion
 * costs
 * <p>
 * Creation date: Feb 28, 2012 - 4:44:06 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NoisyCostDelegate extends TRSPCostDelegate {
    private final TRSPCostDelegate mDelegate;
    private final RandomStream     mRnd;
    private final double           mMaxNoise;

    /**
     * Creates a new <code>NoisyCostDelegate</code>
     * 
     * @param delegate
     *            the wrapped cost delegate
     * @param rnd
     *            the random stream to use
     * @param maxNoise
     *            the maximum amount of noise to be added/subtracted to the cost
     */
    public NoisyCostDelegate(TRSPCostDelegate delegate, RandomStream rnd, double maxNoise) {
        super();
        mDelegate = delegate;
        mRnd = rnd;
        mMaxNoise = 2 * maxNoise; // We directly store twice the value for performance
    }

    @Override
    protected double evaluateGenericTour(ITRSPTour tour) {
        throw new UnsupportedOperationException(
                "NoisyCostDelegate should only be used to evaluate insertions and detours");
    }

    @Override
    protected double evaluateTRSPTour(TRSPTour tour, int node, boolean updateTour) {
        throw new UnsupportedOperationException(
                "NoisyCostDelegate should only be used to evaluate insertions and detours");
    }

    @Override
    public double evaluateDetour(ITRSPTour tour, int i, int n, int j, boolean isRemoval) {
        return addNoise(mDelegate.evaluateDetour(tour, i, n, j, isRemoval));
    }

    @Override
    protected double evaluateInsMove(InsertionMove move) {
        return addNoise(mDelegate.evaluateInsMove(move));
    }

    private double addNoise(double value) {
        // Make sure the returned value is of the same sign
        return value > 0 ? Math.max(0, value + (mRnd.nextDouble() - 0.5) * mMaxNoise) : Math.min(0,
                value + (mRnd.nextDouble() - 0.5) * mMaxNoise);
    }

    @Override
    public boolean isInsertionSeqDependent() {
        return mDelegate.isInsertionSeqDependent();
    }

}