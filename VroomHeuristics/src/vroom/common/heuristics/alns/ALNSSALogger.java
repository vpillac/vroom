/**
 * 
 */
package vroom.common.heuristics.alns;

import vroom.common.utilities.StatCollector.Label;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.optimization.SAAcceptanceCriterion;

/**
 * <code>ALNSSALogger</code> is a specialization of {@link ALNSLogger} that additionally logs the state of the
 * acceptance criterion, assumed to be a {@link SAAcceptanceCriterion}
 * <p>
 * Creation date: May 30, 2011 - 8:44:13 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ALNSSALogger<S extends ISolution> extends ALNSLogger<S> {

    /**
     * Creates a new <code>ALNSSALogger</code>
     * 
     * @param destDir
     *            the destination dir for log files
     */
    public ALNSSALogger(String destDir) {
        super(destDir);
    }

    @Override
    protected Object[] getAdditionalSolStats(ALNSCallbackEvent<S> e, S bestSol, S currentSol, S tmpSol) {
        return new Object[] { ((SAAcceptanceCriterion) e.getSource().getAcceptanceCriterion()).getTemperature() };
    }

    @Override
    protected Label<?>[] getAdditionalSolLabels(AdaptiveLargeNeighborhoodSearch<?> alns) {
        return new Label<?>[] { new Label<Double>("sa_T", Double.class) };
    }
}
