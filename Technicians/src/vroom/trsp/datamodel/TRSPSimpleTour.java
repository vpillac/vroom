package vroom.trsp.datamodel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import vroom.common.utilities.Utilities;

/**
 * <code>TRSPSimpleTour</code> is a minimal representation of a tour that should be used to reduce memory footprint.
 * <p>
 * Creation date: Aug 16, 2011 - 2:55:53 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPSimpleTour extends TRSPTourBase implements Serializable {

    private static final long  serialVersionUID = 1L;

    private final int          mTechnicianId;

    private final int          mHash;

    private final int[]        mNodes;

    private final double       mCost;

    private final TRSPInstance mInstance;

    /**
     * Creates a new <code>TRSPSimpleTour</code> with a specific hash
     * 
     * @param tour
     *            the tour to be duplicated
     * @param hash
     *            the value that {@link #hashCode()} will return
     */
    public TRSPSimpleTour(ITRSPTour tour, int hash) {
        mNodes = new int[tour.length()];
        int i = 0;
        for (int node : tour) {
            mNodes[i++] = node;
        }
        mTechnicianId = tour.getTechnicianId();
        mCost = tour.getTotalCost();
        mHash = tour.hashCode();
        mInstance = tour.getInstance();
    }

    /**
     * Creates a new <code>TRSPSimpleTour</code>
     * 
     * @param tour
     *            the tour to be duplicated
     */
    public TRSPSimpleTour(ITRSPTour tour) {
        this(tour, tour.hashCode());
    }

    /**
     * Creates a new empty <code>TRSPSimpleTour</code>
     * 
     * @param techId
     *            the technician id
     * @param instance
     *            the parent instance
     */
    public TRSPSimpleTour(int techId, TRSPInstance instance) {
        mNodes = new int[0];
        mTechnicianId = techId;
        mCost = 0;
        mHash = techId;
        mInstance = instance;
    }

    /**
     * Creates a new <code>TRSPSimpleTour</code>
     * 
     * @param tech
     *            the technician id
     * @param instance
     *            the parent instance
     * @param tour
     *            the sequence of visited nodes
     * @param cost
     *            the cost of this tour
     * @param hash
     *            the hash code of this tour
     */
    public TRSPSimpleTour(int tech, TRSPInstance instance, List<Integer> tour, double cost, int hash) {
        mTechnicianId = tech;
        mInstance = instance;
        mCost = cost;
        mHash = hash;
        mNodes = Utilities.toIntArray(tour);
    }

    /**
     * Creates a new <code>TRSPSimpleTour</code>
     * 
     * @param tech
     *            the technician id
     * @param instance
     *            the parent instance
     * @param tour
     *            the sequence of visited nodes
     */
    public TRSPSimpleTour(int tech, TRSPInstance instance, List<Integer> tour) {
        this(tech, instance, tour, Double.NaN, tour.hashCode());
    }

    @Override
    public int getTechnicianId() {
        return mTechnicianId;
    }

    @Override
    public TRSPInstance getInstance() {
        return mInstance;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TRSPSimpleTour && ((TRSPSimpleTour) obj).mHash == this.mHash;
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public int hashSolution() {
        return mHash;
    }

    @Override
    public ITourIterator iterator() {
        return new SimpleTourIterator(this);
    }

    @Override
    public double getTotalCost() {
        return mCost;
    }

    @Override
    public int length() {
        return mNodes.length;
    }

    @Override
    public int[] asArray() {
        return Arrays.copyOf(mNodes, mNodes.length);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(length() * 3);

        sb.append(String.format("t:%s c:%.2f l:%s <", getTechnicianId(), getTotalCost(), length()));

        Iterator<Integer> it = iterator();
        while (it.hasNext()) {
            int n = it.next();
            sb.append(n);
            if (it.hasNext())
                sb.append(",");
        }
        sb.append(">");

        return sb.toString();
    }

    @Override
    public String getNodeSeqString() {
        StringBuilder sb = new StringBuilder(length() * 3);

        sb.append("<");

        Iterator<Integer> it = iterator();
        while (it.hasNext()) {
            int n = it.next();
            sb.append(n);
            if (it.hasNext())
                sb.append(",");
        }
        sb.append(">");

        return sb.toString();
    }

    @Override
    public int getFirstNode() {
        if (length() == 0)
            return ITRSPTour.UNDEFINED;
        return mNodes[0];
    }

    @Override
    public int getLastNode() {
        if (length() == 0)
            return ITRSPTour.UNDEFINED;
        return mNodes[mNodes.length - 1];
    }

    @Override
    public int getNodeAt(int index) {
        return mNodes[index];
    }

    @Override
    public boolean isVisited(int node) {
        for (int i = 0; i < mNodes.length; i++) {
            if (mNodes[i] == node)
                return true;
        }
        return false;
    }

    @Override
    public TRSPSimpleTour clone() {
        return new TRSPSimpleTour(this);
    }

}
