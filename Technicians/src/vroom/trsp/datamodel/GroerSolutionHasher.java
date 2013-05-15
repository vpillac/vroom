/**
 *
 */
package vroom.trsp.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import umontreal.iro.lecuyer.rng.RandomStream;

/**
 * <code>GroerSolutionHasher</code> is an implementation for {@link TRSPSolution} of the hashing procedure presented in:
 * <p>
 * Groer, C.; Golden, B. & Wasil, E. <br/>
 * A library of local search heuristics for the vehicle routing problem <br/>
 * Mathematical Programming Computation, Springer Berlin / Heidelberg, 2010, 2, 79-101
 * </p>
 * <p>
 * Creation date: May 23, 2011 - 10:52:45 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GroerSolutionHasher implements ITRSPSolutionHasher {

    private final static Comparator<TRSPTour> TOUR_COMPARATOR = new Comparator<TRSPTour>() {

                                                                  @Override
                                                                  public int compare(TRSPTour o1,
                                                                          TRSPTour o2) {
                                                                      int n1 = o1.length() > 1 ? o1
                                                                              .getNodeAt(1) : o1
                                                                              .getTechnicianId();
                                                                      int n2 = o2.length() > 1 ? o2
                                                                              .getNodeAt(1) : o2
                                                                              .getTechnicianId();

                                                                      return n1 - n2;
                                                                  }
                                                              };

    /** The parent instance of solutions that will be hashed */
    private final TRSPInstance                mInstance;

    /** The random 32bits ints used for hashing (<code>Y<sub>i</sub></code>) */
    final int[]                               mRndInts;

    /**
     * Creates a new <code>GroerSolutionHasher</code>
     * 
     * @param instance
     */
    public GroerSolutionHasher(TRSPInstance instance, RandomStream rndStream) {
        mInstance = instance;

        // Generate the random ints
        int size = mInstance.getDepotCount() + mInstance.getRequestCount();
        mRndInts = new int[size];
        rndStream.nextArrayOfInt(0, Integer.MAX_VALUE, mRndInts, 0, size);

    }

    /* (non-Javadoc)
     * @see vroom.trsp.datamodel.ISolutionHasher#hash(vroom.trsp.datamodel.TRSPSolution)
     */
    @Override
    public int hash(TRSPSolution solution) {
        if (solution.getInstance() != mInstance)
            throw new IllegalArgumentException(
                    "Solution parent instance is different from the reference instance");
        int hash = 0;

        if (solution.getInstance().isCVRPTW()) {
            // Sort the tours to break symmetries
            ArrayList<TRSPTour> tours = new ArrayList<>();
            for (int t = 0; t < solution.getTourCount(); t++) {
                tours.add(solution.getTour(t));
            }
            Collections.sort(tours, TOUR_COMPARATOR);

            for (TRSPTour t : tours) {
                hash = hashTour(t, hash);
            }
        } else {
            for (int t = 0; t < solution.getTourCount(); t++) {
                hash = hashTour(solution.getTour(t), hash);
            }
        }

        return hash;
    }

    /**
     * Hashing of a tour
     * 
     * @param tour
     *            the tour to be hashed
     * @return a hash for the given tour
     */
    @Override
    public int hash(ITRSPTour tour) {
        int hash = mRndInts[tour.getTechnicianId() % mRndInts.length];
        return hashTour(tour, hash);
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
    int hashTour(ITRSPTour tour, int hash) {
        boolean skipEnds = tour.getSolution() != null
                && tour.getSolution().getInstance().isCVRPTW();
        if (tour.length() == 0 || (skipEnds && tour.length() < 3))
            return hash;

        ITourIterator it = tour.iterator();
        if (skipEnds)
            it.next();

        int prev = it.next();
        while (it.hasNext() && (!skipEnds || prev != tour.getLastNode())) {
            int r = it.next();
            hash ^= mRndInts[(r + prev) % mRndInts.length];
            prev = r;
        }

        return hash;
    }

}
