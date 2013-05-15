/**
 * 
 */
package vroom.trsp.optimization.alns;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.RouletteWheel;
import vroom.common.utilities.Utilities;
import vroom.trsp.optimization.InsertionMove;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;

/**
 * <code>RepairRndRegret</code> is a randomized implementation of {@link RepairRegret}
 * <p>
 * Creation date: Feb 7, 2012 - 3:42:55 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class RepairRndRegret extends RepairRegret {

    private final RandomStream mRndStream;

    /**
     * Creates a new <code>RepairRndRegret</code>
     * 
     * @param params
     * @param constraintHandler
     * @param level
     */
    public RepairRndRegret(TRSPGlobalParameters params, TourConstraintHandler constraintHandler, int level,
            RandomStream stream) {
        super(params, constraintHandler, level, false);
        mRndStream = stream;
    }

    @Override
    protected InsertionMove selectBestInsertion() {
        RouletteWheel<InsertionMove> wheel = new RouletteWheel<InsertionMove>();

        for (int r : mSolution.getUnservedRequests()) {
            if (mRegretValues[r] == null) {
                // Get the q best insertions
                InsertionMove[] bests = Utilities.Math.max(mInsMatrix[r], getLevel());

                if (bests.length == 0) {
                    // No feasible insertion was found
                    mBestIns[r] = null;
                    mRegretValues[r] = null;
                    continue;
                }

                // Store the best insertion
                mBestIns[r] = bests[bests.length - 1];

                // Evaluate the regret
                if (getLevel() == 1)
                    mRegretValues[r] = -mBestIns[r].getCost();
                else {
                    mRegretValues[r] = 0d;
                    for (int i = 0; i < bests.length - 1; i++) {
                        mRegretValues[r] += bests[i].getCost() - mBestIns[r].getCost();
                    }
                }
            }

            wheel.add(mBestIns[r], mRegretValues[r]);
        }

        return wheel.drawObject(mRndStream, false);
    }

}
