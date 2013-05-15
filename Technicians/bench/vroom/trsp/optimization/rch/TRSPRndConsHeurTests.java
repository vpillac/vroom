/**
 * 
 */
package vroom.trsp.optimization.rch;

import java.util.Collection;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.Technician;

/**
 * <code>TRSPRndConsHeurTests</code> contains test routines to check the behavior of
 * {@link TRSPRndConstructiveHeuristic}
 * <p>
 * Creation date: Oct 4, 2011 - 11:46:16 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPRndConsHeurTests {

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_DEBUG, LoggerHelper.LEVEL_DEBUG, false);
        // TRSPTestRCHSC.setupGlobalParameters();

        double[] pValues = new double[] { 1.5, 2, 3, 5, 7.5, 10, 20 };

        int size = 50;
        int rep = 1000000;

        System.out.print("p");
        for (int i = 0; i < size; i++)
            System.out.print("," + i);
        System.out.println("");
        for (double p : pValues) {
            // TRSPTestRCHSC.DEFAULT_PARAMS.set(TRSPGlobalParameters.RCH_RND_FACTOR, p);
            TRSPRndConstructiveHeuristic heur = new TRSPRndConstructiveHeuristic(null, null, null,
                    null, 3) {
                @Override
                protected Collection<ITRSPTour> generateGiantTour(Technician tech) {
                    return null;
                }

                @Override
                protected ITRSPTour generateFeasibleTour(Technician tech) {
                    return null;
                }

                @Override
                protected Collection<ITRSPTour> generateFeasibleTours() {
                    return null;
                }
            };
            double[] freq = testNextIds(heur, size, rep);
            double cumFreq = 0;
            System.out.print(p);
            for (int i = 0; i < size; i++) {
                cumFreq += freq[i];
                System.out.print("," + cumFreq);
            }
            System.out.println();
        }

    }

    public static double[] testNextIds(TRSPRndConstructiveHeuristic heur, int size, int rep) {
        double[] freq = new double[size];
        for (int i = 0; i < rep; i++) {
            freq[heur.nextIdx(freq.length)]++;
        }
        for (int i = 0; i < freq.length; i++) {
            freq[i] /= rep;
        }
        return freq;
    }
}
