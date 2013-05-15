/*
 * National ICT Australia - http://www.nicta.com.au - All Rights Reserved
 */
package vroom.common.modeling.dataModel;

import java.util.ListIterator;

/**
 * The class <code>NodeSetSolutionHasher</code> is an implementationof the hashing procedure based on the one proposed
 * in:
 * <p>
 * Groer, C.; Golden, B. & Wasil, E. <br/>
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
 * Creation date: May 4, 2013 - 2:27:04 PM
 * 
 * @author vpillac, <a href="http://www.nicta.com.au">National ICT Australia</a>
 * @version 1.0
 */
public class NodeSetSolutionHasher extends GroerSolutionHasher {

    public NodeSetSolutionHasher(IVRPInstance instance) {
        super(instance);
    }

    @Override
    protected int hash(IRoute<?> route, int hash) {
        ListIterator<?> it = route.iterator();
        boolean first = true;
        while (it.hasNext()) {
            INodeVisit r = (INodeVisit) it.next();
            if (!r.isDepot() || (!first && it.hasNext())) // Skip the first/last depot
                hash ^= mRndInts[r.getID() % mRndInts.length];
        }
        return hash;
    }
}
