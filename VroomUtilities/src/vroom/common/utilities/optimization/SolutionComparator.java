/**
 * 
 */
package vroom.common.utilities.optimization;

import java.util.Comparator;

/**
 * <code>SolutionComparator</code> is a comparator for {@link ISolution solutions}.
 * <p>
 * Creation date: Nov 18, 2011 - 10:58:30 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SolutionComparator<S extends ISolution> implements Comparator<S> {

    private final OptimizationSense mSense;

    /**
     * Creates a new <code>SolutionComparator</code>
     * 
     * @param optimizationSense
     */
    public SolutionComparator(OptimizationSense optimizationSense) {
        mSense = optimizationSense;
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(S o1, S o2) {
        if (o1 == o2)
            return 0;
        int comp = mSense.compare(o1, o2);
        return comp == 0 ? -1 : comp;
    }

}
