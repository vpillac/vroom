/**
 *
 */
package vroom.trsp.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import vroom.common.utilities.IntegerSet;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>HashTourPool</code> is an implementation of {@link ITRSPTourPool} based on a {@link HashSet}
 * <p>
 * Creation date: Aug 16, 2011 - 3:27:31 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class HashTourPool implements ITRSPTourPool {

    /** A flag that will enable the count of the number of hash collisions, default is {@code false} for performance */
    public static boolean     sCountCollisions = false;

    /** The minimum size for tours, default is 3 (<code>&lt;depot,node,depot&gt;</code>), shorter tours will be ignored */
    public static int         sMinTourSize     = 3;

    private final Map<?, ?>[] mTourPool;

    private int               mSize;

    private int               mCollisionsCount = 0;

    /**
     * Returns the number of hash collisions detected The result is only valid is {@link #sCountCollisions} is set to
     * {@code true}
     * 
     * @return the number of hash collisions detected
     * @throws IllegalStateException
     *             if HashTourPool.sCountCollisions is set to false
     */
    public int getCollisionsCount() {
        if (!sCountCollisions)
            throw new IllegalStateException("HashTourPool.sCountCollisions is set to false");
        return mCollisionsCount;
    }

    /** The hasher that will be used to calculate the hash of tours */
    private final ITRSPSolutionHasher mHasher;

    /**
     * Returns the hasher that will be used to calculate the hash of tours
     * 
     * @return the hasher that will be used to calculate the hash of tours
     */
    public ITRSPSolutionHasher getHasher() {
        return mHasher;
    }

    /**
     * Creates a new <code>HashTourPool</code>
     * 
     * @param techCount
     *            the number of available technicians
     * @param expectedToursPerTech
     *            the expected number of tours per technician
     * @param hasher
     */
    public HashTourPool(int techCount, int expectedToursPerTech, ITRSPSolutionHasher hasher) {
        mTourPool = new Map<?, ?>[techCount];
        for (int i = 0; i < mTourPool.length; i++) {
            mTourPool[i] = new HashMap<Integer, TRSPSimpleTour>((int) (expectedToursPerTech / 0.75) + 1, 0.75f);
        }
        mSize = 0;
        mHasher = hasher;
    }

    @SuppressWarnings("unchecked")
    private synchronized Map<Integer, TRSPSimpleTour> getPool(int technician) {
        return (Map<Integer, TRSPSimpleTour>) mTourPool[technician];
    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.datamodel.ITRSPTourPool#add(java.util.Collection)
     */
    @Override
    public synchronized int add(Iterable<? extends ITRSPTour> tours) {
        int count = 0;
        for (ITRSPTour tour : tours) {
            // Check the tour feasibility
            String err = TRSPSolutionChecker.INSTANCE.checkTour(tour);

            if (err.isEmpty() && tour.length() >= sMinTourSize) {
                // Check for the presence of a tour with the same hash
                Map<Integer, TRSPSimpleTour> pool = getPool(tour.getTechnicianId());
                Integer hash = mHasher.hash(tour);
                TRSPSimpleTour prevTour = pool.get(hash);

                if (prevTour != null) {
                    // A tour with the same hash already exists, compare objective values
                    if (prevTour.getTotalCost() > tour.getTotalCost()) {
                        // Previous tour has a higher cost, replace it
                        pool.put(hash, new TRSPSimpleTour(tour, hash));
                    } else {
                        // Previous tour has a lower cost, keep it
                    }
                    if (sCountCollisions) {
                        checkCollision(prevTour, tour);
                    }
                } else {
                    pool.put(hash, new TRSPSimpleTour(tour, hash));
                    count++;
                    mSize++;
                }

            } else if (!err.isEmpty()) {
                TRSPLogging.getOptimizationLogger().warn("HashTourPool.add: ignoring infeasible tour %s (%s)", tour,
                        err);

            }

        }

        return count;
    }

    /**
     * Check if {@code  prevTour} visits the same requests as {@code  tour}, if not increment the collision count
     * 
     * @param prevTour
     * @param tour
     */
    private void checkCollision(TRSPSimpleTour prevTour, ITRSPTour tour) {
        IntegerSet prevSet = new IntegerSet(tour.getSolution().getInstance().getMaxId());
        for (int n : prevTour)
            prevSet.add(n);
        for (int n : tour)
            if (!prevSet.remove(n)) {
                mCollisionsCount++;
                break;
            }
        if (!prevSet.isEmpty())
            mCollisionsCount++;
    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.datamodel.ITRSPTourPool#getAllTours()
     */
    @Override
    public synchronized Collection<ITRSPTour> getAllTours() {
        ArrayList<ITRSPTour> tours = new ArrayList<ITRSPTour>(size());

        for (int i = 0; i < mTourPool.length; i++) {
            tours.addAll(getPool(i).values());
        }

        return tours;
    }

    /*
     * (non-Javadoc)
     * @see vroom.trsp.datamodel.ITRSPTourPool#size()
     */
    @Override
    public int size() {
        return mSize;
    }

    @Override
    public synchronized void clear() {
        for (int i = 0; i < mTourPool.length; i++) {
            getPool(i).clear();
        }
        mSize = 0;
    }

    @Override
    public synchronized void dispose() {
        clear();
    }

    @Override
    public Iterator<ITRSPTour> iterator() {
        return getAllTours().iterator();
    }
}
