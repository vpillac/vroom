/* 
 * This file is part of the VRP 2013 Computational Aspects of Vehicle Routing course (http://www.ima.uco.fr/vrp2013/).
 * 
 * You can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or any later version.
 * 
 * Author: Victor Pillac - http://www.victorpillac.com
 */
package vrp2013.datamodel;

import vroom.common.modeling.vrprep.Instance;
import vroom.common.modeling.vrprep.Instance.Network.Nodes.Node;
import vroom.common.modeling.vrprep.Instance.Requests.Request;

/**
 * The class <code>CVRPInstance</code> contains the definition of a Capacitated Vehicle Routing Problem
 * <p>
 * Creation date: 08/04/2013 - 10:13:51 PM.
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public class CVRPInstance {

    /** The Demand. */
    private final double[]   mDemand;

    /** The Distance. */
    private final double[][] mDistance;

    /** The Vehicle capacity. */
    private final double     mVehicleCapacity;

    /** The Fleet size. */
    private final int        mFleetSize;

    /**
     * Gets the vehicle capacity.
     * 
     * @return the vehicle capacity
     */
    public double getVehicleCapacity() {
        return mVehicleCapacity;
    }

    /**
     * Gets the fleet size, or {@code  -1} if the fleet is unlimited.
     * 
     * @return the fleet size
     */
    public int getFleetSize() {
        return mFleetSize;
    }

    /**
     * Gets the demand of node {@code  nodeId} .
     * 
     * @param nodeId
     *            the node id
     * @return the demand of node {@code  nodeId}
     */
    public double getDemand(int nodeId) {
        return mDemand[nodeId];
    }

    /**
     * Gets the distance between nodes {@code  i} and {@code  j} .
     * 
     * @param i
     *            first node
     * @param j
     *            second node
     * @return the distance between nodes {@code  i} and {@code  j} .
     */
    public double getDistance(int i, int j) {
        return mDistance[i][j];
    }

    /**
     * Returns the number of nodes in this instance
     * 
     * @return the the number of nodes in this instance
     */
    public int size() {
        return mDemand.length;
    }

    /**
     * Creates a new <code>CVRPInstance</code> from a {@linkplain Instance VRPRep instance}.
     * 
     * @param vrprepInstance
     *            the vrprep instance
     */
    public CVRPInstance(Instance vrprepInstance) {
        // if (!"CVRP".equalsIgnoreCase(vrprepInstance.getInfo().getProblem()))
        // throw new UnsupportedOperationException("Unsupported VRP variant: "
        // + vrprepInstance.getInfo().getProblem());

        mFleetSize = vrprepInstance.getFleet().getVehicle().get(0).getNumber().intValue();
        mVehicleCapacity = vrprepInstance.getFleet().getVehicle().get(0).getCapacity().get(0);

        mDemand = new double[vrprepInstance.getNetwork().getNodes().getNode().size()];
        for (Request r : vrprepInstance.getRequests().getRequest()) {
            // Store the demand for request r
            mDemand[r.getNode().intValue()] = Double.valueOf((String) r.getDemand().get(0)
                    .getContent().get(0));
        }

        mDistance = new double[mDemand.length][mDemand.length];
        for (Node i : vrprepInstance.getNetwork().getNodes().getNode()) {
            for (Node j : vrprepInstance.getNetwork().getNodes().getNode()) {
                // Use the euclidean distance
                mDistance[i.getId().intValue()][j.getId().intValue()] = Math.hypot(i.getLocation()
                        .getEuclidean().getCx()
                        - j.getLocation().getEuclidean().getCx(), i.getLocation().getEuclidean()
                        .getCy()
                        - j.getLocation().getEuclidean().getCy());
            }

        }
    }

    /**
     * Print this instance in the standard output
     */
    public void print() {
        System.out.printf("Size : %s\n", size());
        System.out.printf("Fleet: Q=%.1f k=%s\n", getVehicleCapacity(), getFleetSize());
        System.out.println("Nodes:");
        System.out.printf("%4s: %7s\n", "ID", "Demand");
        for (int j = 0; j < size(); j++) {
            System.out.printf("%4s: %7.1f\n", j, getDemand(j));
        }
    }
}
