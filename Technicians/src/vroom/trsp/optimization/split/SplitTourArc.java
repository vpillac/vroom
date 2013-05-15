/**
 * 
 */
package vroom.trsp.optimization.split;

import java.util.Iterator;

import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.ITourIterator;
import vroom.trsp.datamodel.SimpleTourIterator;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPTourBase;

/**
 * The class <code>SplitTourArc</code> represent an arc in the Split auxiliary graph, i.e. a tour in the solution space.
 * For efficiency it is defined on top of an existing giant tour.
 * <p>
 * Creation date: Sep 26, 2011 - 4:53:41 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SplitTourArc extends TRSPTourBase {
    /** A constant used to specify that the main depot is not visited */
    public static final int DEPOT_NOT_VISITED = -1;

    /** the parent giant tour */
    private final ITRSPTour mGiantTour;
    /** the start index of this tour */
    private final int       mStart;
    /** the end index of this tour */
    private final int       mEnd;
    /** the index of the visit to the depot */
    private int             mDepotIndex       = DEPOT_NOT_VISITED;
    /** the id of the technician home */
    private final int       mHomeS;
    private final int       mHomeE;

    /** the length of this tour */
    private int             mLength;

    /** the cost of this tour **/
    private double          mTotalCost;

    /**
     * Getter for the cost of this tour
     * 
     * @return the value of name
     */
    @Override
    public double getTotalCost() {
        return this.mTotalCost;
    }

    /**
     * Setter for the cost of this tour
     * 
     * @param name
     *            the value to be set for the cost of this tour
     */
    @Override
    public void setTotalCost(double name) {
        this.mTotalCost = name;
    }

    /**
     * Returns the index of the first node of this arc in the parent giant tour
     * 
     * @return the index of the first node of this arc in the parent giant tour
     */
    public int getStart() {
        return mStart;
    }

    /**
     * Returns the index of the last node of this arc in the parent giant tour
     * 
     * @return the index of the last node of this arc in the parent giant tour
     */
    public int getEnd() {
        return mEnd;
    }

    /**
     * Creates a new <code>SplitTourArc</code>
     * 
     * @param giantTour
     *            the parent giant tour
     * @param start
     *            the start index of this tour
     * @param end
     *            the end index of this tour
     */
    public SplitTourArc(ITRSPTour giantTour, int start, int end) {
        super();
        mGiantTour = giantTour;
        mStart = start;
        mEnd = end;
        mTotalCost = Double.NaN;
        mHomeS = giantTour.getInstance().getTechnician(giantTour.getTechnicianId()).getHome()
                .getID();
        mHomeE = giantTour.getInstance().getHomeDuplicate(mHomeS);
        mLength = mEnd - mStart + 3;
    }

    @Override
    public TRSPInstance getInstance() {
        return mGiantTour.getInstance();
    }

    @Override
    public int getTechnicianId() {
        return mGiantTour.getTechnicianId();
    }

    @Override
    public int[] asArray() {
        int[] array = new int[length()];
        int idx = 0;
        for (int i : this)
            array[idx++] = i;
        return array;
    }

    /**
     * Set the index of the visit to the depot
     * 
     * @param index
     *            the index of the visit to the depot
     * @throws IllegalStateException
     *             if the depot visit was already set
     */
    public void setDepotVisitIndex(int index) {
        if (mDepotIndex != DEPOT_NOT_VISITED)
            throw new IllegalStateException("The depot visit was already set");
        if (index != DEPOT_NOT_VISITED) {
            mDepotIndex = index;
            mLength++;
        }
    }

    /**
     * Returns <code>true</code> if the main depot is visited, <code>false</code> otherwise
     * 
     * @return <code>true</code> if the main depot is visited, <code>false</code> otherwise
     */
    public boolean isMainDepotVisited() {
        return mDepotIndex != DEPOT_NOT_VISITED;
    }

    @Override
    public int getNodeAt(int index) {
        if (index == 0)
            return getFirstNode();
        else if (index == length() - 1)
            return getLastNode();
        else if (!isMainDepotVisited())
            return mGiantTour.getNodeAt(mStart + index - 1);
        else {
            if (index < mDepotIndex)
                return mGiantTour.getNodeAt(mStart + index - 1);
            else if (index == mDepotIndex)
                return getInstance().getMainDepot().getID();
            else
                return mGiantTour.getNodeAt(mStart + index - 2);
        }
    }

    @Override
    public int getFirstNode() {
        return mHomeS;
    }

    @Override
    public int getLastNode() {
        return mHomeE;
    }

    @Override
    public int length() {
        return mLength;
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
    public boolean isVisited(int node) {
        for (int i = 0; i < length(); i++) {
            if (getNodeAt(i) == node)
                return true;
        }
        return false;
    }

    @Override
    public ITourIterator iterator() {
        return new SimpleTourIterator(this);
    }

    @Override
    public ITRSPTour clone() {
        SplitTourArc clone = new SplitTourArc(mGiantTour, mStart, mEnd);
        if (isMainDepotVisited())
            clone.setDepotVisitIndex(mDepotIndex);
        return clone;
    }

    @Override
    public int hashSolution() {
        return hashCode();
    }
}
