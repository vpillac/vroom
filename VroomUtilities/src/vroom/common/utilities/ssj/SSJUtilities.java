/**
 * 
 */
package vroom.common.utilities.ssj;

import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.probdist.UniformDist;
import umontreal.iro.lecuyer.probdist.UniformIntDist;

/**
 * <code>SSJUtilities</code>
 * <p>
 * Creation date: Jul 1, 2010 - 4:04:29 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SSJUtilities {

    /**
     * @param dist
     * @return
     */
    public static String toShortString(Distribution dist) {
        if (dist instanceof UniformDist) {
            UniformDist d = (UniformDist) dist;
            return String.format("U(%s,%s)", d.getA(), d.getB());
        } else if (dist instanceof UniformIntDist) {
            UniformIntDist d = (UniformIntDist) dist;
            return String.format("Uint(%s,%s)", d.getI(), d.getJ());
        } else if (dist instanceof NormalDist) {
            NormalDist d = (NormalDist) dist;
            return String.format("N(%s,%s)", d.getMu(), d.getSigma());
        } else {
            return dist.toString();
        }
    }

}
