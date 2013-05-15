/**
 * 
 */
package vroom.trsp.util;

import vroom.common.utilities.IDistance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.TRSPTour.TRSPTourIterator;

/**
 * <code>BrokenPairsDistance</code> is an implementation of {@link IDistance} that measure the number of common arcs
 * between two solutions.
 * <p>
 * Note that this implementation should not be used concurrently as it stores previous information for performance
 * </p>
 * <p>
 * Prins, C. 2009. Two memetic algorithms for heterogeneous fleet vehicle routing problems.
 * <em>Engineering Applications of
 * Artificial Intelligence</em> 22(6) 916–928.
 * </p>
 * <p>
 * Creation date: Feb 29, 2012 - 4:48:58 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class BrokenPairsDistance implements IDistance<TRSPSolution> {

    private boolean[][]  mPairs;
    private int          mPairCount;
    private TRSPSolution mSol;

    public void reset() {
        mPairs = null;
        mSol = null;
        mPairCount = 0;
    }

    @Override
    public double evaluateDistance(TRSPSolution sol1, TRSPSolution sol2) {
        if (sol2 == mSol) {
            sol2 = sol1;
            sol1 = mSol;
        } else if (sol1 != mSol) {
            // Reevaluate the pair matrix
            mSol = sol1;
            mPairs = new boolean[mSol.getInstance().getMaxId()][mSol.getInstance().getMaxId()];
            mPairCount = 0;
            for (TRSPTour t : mSol) {
                if (t.length() <= 1)
                    continue;
                TRSPTourIterator it = t.iterator();
                int pred = it.next();
                while (it.hasNext()) {
                    int node = it.next();
                    mPairs[pred][node] = true;
                    mPairCount++;
                    pred = node;
                }
            }
        }
        // We have mSol and mPairs updated, sol1=mSol
        int inter = 0;
        int pairCount2 = 0;
        for (TRSPTour t : sol2) {
            if (t.length() <= 1)
                continue;
            TRSPTourIterator it = t.iterator();
            int pred = it.next();
            while (it.hasNext()) {
                int node = it.next();
                pairCount2++;
                if (mPairs[pred][node])
                    inter++;
                pred = node;
            }
        }

        return mPairCount + pairCount2 - 2 * inter;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }

}
