/**
 *
 */
package vroom.trsp.optimization.alns;

import vroom.common.heuristics.alns.ALNSCallbackEvent;
import vroom.common.heuristics.alns.ALNSLogger;
import vroom.common.heuristics.alns.AdaptiveLargeNeighborhoodSearch;
import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.Utilities.Math.DeviationMeasure;
import vroom.common.utilities.optimization.IAcceptanceCriterion;
import vroom.common.utilities.optimization.SAAcceptanceCriterion;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPTourBalance;
import vroom.trsp.datamodel.costDelegates.TRSPWorkingTime;

/**
 * <code>TRSPBiObjALNSLogger</code> is a specialization of {@link ALNSLogger} for the TRSP.
 * <p>
 * It stores the total working time of the temporary and best solution
 * </p>
 * <p>
 * Creation date: Jul 19, 2011 - 10:21:17 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPBiObjALNSLogger extends ALNSLogger<TRSPSolution> {

    private final TRSPCostDelegate mFirstCostDelegate;
    private final TRSPCostDelegate mSecondCostDelegate;

    /**
     * Creates a new <code>TRSPBiObjALNSLogger</code> with working time and tour balance delegates
     * 
     * @param destDir
     *            the directory in which the stat file will be written
     * @param measure
     *            the deviation measure to be used to log costs
     */
    public TRSPBiObjALNSLogger(String destDir, DeviationMeasure measure) {
        super(destDir);
        mFirstCostDelegate = new TRSPWorkingTime();
        mSecondCostDelegate = new TRSPTourBalance(mFirstCostDelegate, measure);
    }

    /**
     * Creates a new <code>TRSPBiObjALNSLogger</code>
     * 
     * @param destDir
     * @param firstCostDelegate
     * @param secondCostDelegate
     */
    public TRSPBiObjALNSLogger(String destDir, TRSPCostDelegate firstCostDelegate, TRSPCostDelegate secondCostDelegate) {
        super(destDir);
        mFirstCostDelegate = firstCostDelegate;
        mSecondCostDelegate = secondCostDelegate;
    }

    @Override
    protected Object[] getAdditionalSolStats(ALNSCallbackEvent<TRSPSolution> e, TRSPSolution bestSol,
            TRSPSolution currentSol, TRSPSolution tmpSol) {
        boolean feas = tmpSol != null && tmpSol.getUnservedRequests().isEmpty();

        double bWT = bestSol != null ? mFirstCostDelegate.evaluateSolution(bestSol, true, false) : Double.NaN;
        double cWT = currentSol != null ? mFirstCostDelegate.evaluateSolution(currentSol, true, false) : Double.NaN;
        double tWT = tmpSol != null ? mFirstCostDelegate.evaluateSolution(tmpSol, true, false) : Double.NaN;

        double bBal = bestSol != null ? mSecondCostDelegate.evaluateSolution(bestSol, true, false) : Double.NaN;
        double cBal = currentSol != null ? mSecondCostDelegate.evaluateSolution(currentSol, true, false) : Double.NaN;
        double tBal = tmpSol != null ? mSecondCostDelegate.evaluateSolution(tmpSol, true, false) : Double.NaN;

        IAcceptanceCriterion crit = e.getSource().getAcceptanceCriterion();
        String accept = crit instanceof SAAcceptanceCriterion ? "" + ((SAAcceptanceCriterion) crit).getTemperature()
                : crit.toString();

        return new Object[] { //
        feas,//
                bWT, cWT, tWT,//
                bBal, cBal, tBal, //
                accept };
    }

    @Override
    protected Label<?>[] getAdditionalSolLabels(AdaptiveLargeNeighborhoodSearch<?> alns) {
        return new Label<?>[] { //
        new Label<Boolean>("tmp_feas", Boolean.class),//
                new Label<Double>("best_firstObj", Double.class),//
                new Label<Double>("cur_firstObj", Double.class),//
                new Label<Double>("tmp_firstObj", Double.class),//
                new Label<Double>("best_scdObj", Double.class),//
                new Label<Double>("cur_scdObj", Double.class),//
                new Label<Double>("tmp_scdObj", Double.class),//
                new Label<String>("accept_crit", String.class) };
    }
}
