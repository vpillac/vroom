/**
 *
 */
package vroom.common.modeling.dataModel;

import java.util.ListIterator;
import java.util.Random;

/**
 * The class <code>GroerSolutionHasher</code> is an implementation of {@link VRPSolutionHasher} using the hashing
 * procedure presented in:
 * <p>
 * Groer, C.; Golden, B. & Wasil, E. <br/>
 * A library of local search heuristics for the vehicle routing problem <br/>
 * Mathematical Programming Computation, Springer Berlin / Heidelberg, 2010, 2, 79-101
 * </p>
 * <p>
 * <p>
 * Creation date: May 4, 2013 - 10:04:51 AM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class GroerSolutionHasher extends VRPSolutionHasher {

    /** The parent instance of solutions that will be hashed */
    private final IVRPInstance mInstance;

    /** The random 32bits ints used for hashing (<code>Y<sub>i</sub></code>) */
    final int[]                mRndInts;

    /**
     * Creates a new <code>GroerSolutionHasher</code>
     * 
     * @param instance
     */
    public GroerSolutionHasher(IVRPInstance instance) {
        mInstance = instance;

        // Generate the random ints
        int size = mInstance.getDepotCount() + mInstance.getRequestCount();
        mRndInts = new int[size];
        Random rnd = new Random(0);
        for (int i = 0; i < mRndInts.length; i++) {
            mRndInts[i] = rnd.nextInt();
        }
    }

    @Override
    public int hash(IRoute<?> route) {
        int hash = mRndInts[route.getVehicle().getID() % mRndInts.length];
        return hash(route, hash);
    }

    @Override
    protected int hash(IRoute<?> route, int hash) {
        if (route.length() == 0)
            return hash;

        ListIterator<? extends INodeVisit> it = route.iterator();

        INodeVisit prev = it.next();
        while (it.hasNext()) {
            INodeVisit r = it.next();
            hash ^= mRndInts[(r.getID() + prev.getID()) % mRndInts.length];
            prev = r;
        }

        return hash;
    }

}
