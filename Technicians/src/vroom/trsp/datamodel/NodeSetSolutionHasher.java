/**
 *
 */
package vroom.trsp.datamodel;

import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * <code>NodeSetSolutionHasher</code> is an implementation for {@link TRSPSolution} of the hashing procedure based on
 * the one proposed in:
 * <p>
 * Groër, C.; Golden, B. & Wasil, E. <br/>
 * A library of local search heuristics for the vehicle routing problem <br/>
 * Mathematical Programming Computation, Springer Berlin / Heidelberg, 2010, 2, 79-101
 * </p>
 * <p>
 * The main difference with {@link GroerSolutionHasher} is that the sequence of nodes is not considered when evaluating
 * a tour, but only the subset of requests that are visited, and the associated technician. Consequently, two tours
 * visiting the same set of requests in a different order will have the same hash. This is in particular useful when
 * maintaining a pool of tours for a set covering formulation.
 * </p>
 * <p>
 * Creation date: May 23, 2011 - 10:52:45 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NodeSetSolutionHasher extends GroerSolutionHasher {

    public NodeSetSolutionHasher(TRSPInstance instance, RandomStream rndStream) {
        super(instance, rndStream);
    }

    /**
     * Hashing of a tour from a start value
     * 
     * @param tour
     *            the tour to be hashed
     * @param hash
     *            the initial hash value
     * @return the hash of the tour depending on the initial hash value
     */
    @Override
    int hashTour(ITRSPTour tour, int hash) {
        for (int r : tour) {
            // if (tour.isVisited(r)) {
            hash ^= mRndInts[r % mRndInts.length];
            // }
        }

        return hash;
    }

}
