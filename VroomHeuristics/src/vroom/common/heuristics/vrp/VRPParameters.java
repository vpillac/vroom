/**
 * 
 */
package vroom.common.heuristics.vrp;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.optimization.SimpleParameters;

/**
 * <code>VRPParameters</code>
 * <p>
 * Creation date: Apr 27, 2010 - 11:49:04 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPParameters extends SimpleParameters {

    /**
     * Creates a new <code>VRPParameters</code>
     * 
     * @param maxTime
     *            the maximum time (in ms)
     * @param maxIt
     *            the maximum number of iterations
     * @param acceptFirstImprovement
     *            a flag stating whether the research should be thorough (<code>false</code>) or accept the first
     *            improving move (<code>true</code>)
     * @param randonized
     *            the randomized flag, <code>true</code> if the procedure should be randomized
     * @param rndStream
     *            a random stream
     * @see SimpleParameters#SimpleParameters(long, int, boolean, boolean, boolean, RandomStream)
     */
    public VRPParameters(long maxTime, int maxIt, boolean acceptFirstImprovement,
            boolean randonized, RandomStream rndStream) {
        super(maxTime, maxIt, acceptFirstImprovement, randonized, false, rndStream);
    }

}
