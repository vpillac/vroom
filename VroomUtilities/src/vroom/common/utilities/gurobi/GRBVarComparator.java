/**
 * 
 */
package vroom.common.utilities.gurobi;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBVar;

import java.util.Comparator;

/**
 * <code>GRBVarComparator</code> is an implementation of {@link Comparator} for {@link GRBVar}
 * <p>
 * Creation date: Jul 6, 2010 - 4:27:49 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBVarComparator implements Comparator<GRBVar> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(GRBVar o1, GRBVar o2) {
        try {
            return o1.get(GRB.StringAttr.VarName).compareTo(o2.get(GRB.StringAttr.VarName));
        } catch (GRBException e) {
            return 0;
        }
    }

}
