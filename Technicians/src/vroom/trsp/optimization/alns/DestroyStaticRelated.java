/**
 *
 */
package vroom.trsp.optimization.alns;

import java.util.Arrays;
import java.util.Set;

import vroom.common.heuristics.alns.IDestroy;
import vroom.common.utilities.optimization.IInstance;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;

/**
 * <code>DestroyStaticRelated</code> is an implementation of {@link IDestroy} based on a static relatedness metric.
 * <p>
 * Given two requests <code>i</code> and <code>j</code> we define the static relatedness
 * <code>r<sub>ij</sub><sup>s</sup></code>
 * </p>
 * <p>
 * Creation date: May 26, 2011 - 11:23:41 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DestroyStaticRelated extends DestroyRelated {

    private Relatedness[][] mRelatednessMatrix;

    private final double    mGammaD;
    private final double    mGammaT;
    private final double    mGammaS;

    /**
     * Creates a new <code>DestroyStaticRelated</code>
     * 
     * @param randomization
     *            the parameter <em>p</em> that control the level of randomization
     * @param gammaD
     *            the weight of the distance component
     * @param gammaT
     *            the weight of the time windows end component
     * @param gammaS
     *            the weight of the skill compatibility component
     */
    public DestroyStaticRelated(double randomization, double gammaD, double gammaT, double gammaS) {
        super(randomization);
        mGammaD = gammaD;
        mGammaT = gammaT;
        mGammaS = gammaS;
    }

    @Override
    public void initialize(IInstance instance) {
        TRSPInstance ins = (TRSPInstance) instance;
        mRelatednessMatrix = new Relatedness[ins.getMaxId()][];

        // Scaling constants
        double Mc = Double.NEGATIVE_INFINITY, Mt = Double.NEGATIVE_INFINITY;
        double[][] twd = new double[ins.getMaxId()][ins.getMaxId()];
        for (TRSPRequest i : ins.getRequests()) {
            mRelatednessMatrix[i.getID()] = new Relatedness[ins.getMaxId()];
            for (TRSPRequest j : ins.getRequests()) {
                double dij = ins.getCostDelegate().getDistance(i.getID(), j.getID());
                if (dij > Mc)
                    Mc = dij;
                twd[i.getID()][j.getID()] = Math.abs(ins.getTimeWindow(i.getID()).endAsDouble()
                        - ins.getTimeWindow(j.getID()).endAsDouble());
                if (twd[i.getID()][j.getID()] > Mt)
                    Mt = twd[i.getID()][j.getID()];
            }
        }
        if (Mc == 0)
            Mc = 1;
        if (Mt == 0)
            Mt = 1;

        // Evaluate the relatedness matrix
        for (TRSPRequest i : ins.getRequests()) {
            for (TRSPRequest j : ins.getRequests()) {
                if (i == j)
                    continue;

                // Distance component
                double dij = ins.getCostDelegate().getDistance(i.getID(), j.getID()) / Mc;
                // Time component
                double tij = twd[i.getID()][j.getID()] / Mt;
                // Compatible technician component
                Set<Integer> Ki = ins.getCompatibleTechnicians(i.getID());
                Set<Integer> Kj = ins.getCompatibleTechnicians(j.getID());
                double Ms = Math.min(Ki.size(), Kj.size());
                int inter = 0;
                for (int t : Ki)
                    if (Kj.contains(t))
                        inter++;
                double sij = (1 - inter / Ms);

                // Relatedness
                double rij = Math.pow(1 + dij, mGammaD) * Math.pow(1 + tij, mGammaT) * Math.pow(1 + sij, mGammaS);

                // Store this value
                mRelatednessMatrix[i.getID()][j.getID()] = new Relatedness(i.getID(), j.getID(), rij);
            }
        }
    }

    @Override
    protected Relatedness[] evaluateRelatedRequests(int seed, TRSPSolution solution, Set<Integer> candidates) {
        return mRelatednessMatrix[seed];
    }

    @Override
    public String getName() {
        return String.format("rel-stat-(%s,%s,%s)", mGammaD, mGammaT, mGammaS);
    };

    @Override
    public DestroyStaticRelated clone() {
        DestroyStaticRelated clone = new DestroyStaticRelated(getRandomization(), mGammaD, mGammaT, mGammaS);
        clone.mRelatednessMatrix = new Relatedness[mRelatednessMatrix.length][];
        for (int i = 0; i < clone.mRelatednessMatrix.length; i++) {
            if (mRelatednessMatrix[i] != null)
                clone.mRelatednessMatrix[i] = Arrays.copyOf(mRelatednessMatrix[i], mRelatednessMatrix[i].length);
        }
        return clone;
    }

    @Override
    public void dispose() {
        mRelatednessMatrix = null;
    }
}
