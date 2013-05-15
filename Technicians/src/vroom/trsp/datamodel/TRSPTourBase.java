/**
 * 
 */
package vroom.trsp.datamodel;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import vroom.common.modeling.dataModel.Depot;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.IToShortString;
import vroom.trsp.datamodel.TRSPSolution.GiantPermutation;

/**
 * <code>TRSPTourBase</code> is a common class for all implementations of {@link ITRSPTour}
 * <p>
 * Creation date: Sep 27, 2011 - 2:48:10 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public abstract class TRSPTourBase implements ITRSPTour, IToShortString {

    /** The parent solution */
    private final TRSPSolution mSolution;

    /**
     * Gets the parent solution.
     * 
     * @return the parent solution
     */
    @Override
    public TRSPSolution getSolution() {
        return mSolution;
    }

    /**
     * Returns the giant permutation defined in the associated solution
     * 
     * @return the giant permutation defined in the associated solution
     */
    protected GiantPermutation getPermutation() {
        return mSolution.getGiantPermutation();
    }

    /** The total cost. */
    private double mTotalCost;

    @Override
    public double getTotalCost() {
        return mTotalCost;
    }

    /**
     * Sets the total cost.
     * 
     * @param totalCost
     *            the new total cost
     */
    @Override
    public void setTotalCost(double totalCost) {
        mTotalCost = totalCost;
    }

    /**
     * Creates a new <code>TRSPTourBase</code>
     * 
     * @param solution
     */
    public TRSPTourBase(TRSPSolution solution) {
        super();
        this.mSolution = solution;
        this.mLock = new ExtendedReentrantLock();
    }

    /**
     * Creates a new <code>TRSPTourBase</code>
     */
    public TRSPTourBase() {
        this(null);
    }

    @Override
    public Comparable<?> getObjective() {
        return getTotalCost();
    }

    @Override
    public double getObjectiveValue() {
        return getTotalCost();
    }

    /**
     * Gets the node sequence.
     * 
     * @return the node sequence
     */
    @Override
    public List<Integer> asList() {
        List<Integer> seq = new ArrayList<Integer>(length());

        for (Integer i : this) {
            seq.add(i);
        }

        return seq;
    }

    @Override
    public int[] asArray() {
        int[] seq = new int[length()];
        int idx = 0;
        for (Integer i : this) {
            seq[idx++] = i;
        }
        return seq;
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
    public String toString() {
        return String.format("t:%s c:%.2f l:%s %s", getTechnicianId(), getTotalCost(), length(), getNodeSeqString());
    }

    @Override
    public String toShortString() {
        return getNodeSeqString();
    }

    @Override
    public String toDetailedString() {
        return toString();
    }

    // ------------------------------------
    // Utility methods
    // ------------------------------------
    /**
     * Getter for the technician which is executing this tour
     * 
     * @return the Technician associated with this tour
     */
    public Technician getTechnician() {
        return getInstance().getTechnician(getTechnicianId());
    }

    /**
     * Gets the earliest start time of this tour, as defined by the technician home time window start
     * 
     * @return the earliest start time of this tour, as defined by the technician home time window start
     * @see Depot#getTimeWindow()
     * @see Technician#getHome()
     */
    public double getEarliestStartTime() {
        return getTechnician().getHome().getTimeWindow().startAsDouble();
    }

    /**
     * Utility method to get the time window associated with a node
     * 
     * @param node
     *            the considered node
     * @return the time window of <code>node</code>
     * @see TRSPInstance#getTimeWindow(int)
     */
    public ITimeWindow getTimeWindow(int node) {
        return getInstance().getTimeWindow(node);
    }

    /**
     * Utility method to get the service time associated with a node.
     * <p>
     * In this implementation depots are assumed to have a service time of <code>0</code>.
     * </p>
     * 
     * @param node
     *            the considered node
     * @return the service time of <code>node</code>
     * @see TRSPInstance#getServiceTime(int)
     */
    public double getServiceTime(int node) {
        return getInstance().getServiceTime(node);
    }

    /**
     * Utility method to calculate the travel time between two nodes
     * 
     * @param pred
     * @param succ
     * @return the travel time between <code>pred</code> and <code>succ</code>.
     */
    public double getTravelTime(int pred, int succ) {
        if (pred == ITRSPTour.UNDEFINED || succ == ITRSPTour.UNDEFINED)
            return 0;
        return getInstance().getCostDelegate().getTravelTime(pred, succ, getTechnician());
    }

    /**
     * Creates a boolean array representing the tools initially available to this tour's technician
     * 
     * @return a boolean array representing the tools initially available to this tour's technician
     */
    public boolean[] getTechnicianToolSet() {
        boolean[] tools = new boolean[getInstance().getToolCount()];

        for (int t : getTechnician().getToolSet())
            tools[t] = true;

        return tools;
    }

    /**
     * Creates an integer array containing the spare parts initially available to this tour's technician
     * 
     * @return an integer array containing the spare parts initially available to this tour's technician
     */
    public int[] getTechnicianSpareParts() {
        int[] spare = new int[getInstance().getSpareCount()];

        for (int t = 0; t < spare.length; t++)
            spare[t] = getTechnician().getAvailableSpareParts(t);

        return spare;
    }

    /**
     * Returns <code>true</code> if <code>node</code> is a main depot
     * 
     * @param node
     *            , the node id, can be {@link ITRSPTour#UNDEFINED}
     * @return <code>true</code> if <code>node</code> is a main depot
     */
    protected boolean isMainDepot(int node) {
        return node != ITRSPTour.UNDEFINED && getInstance().isMainDepot(node);
    }

    // ------------------------------------

    // ------------------------------------
    // ILockable interface implementation
    // ------------------------------------
    /** A lock to be used by this instance */
    private final ExtendedReentrantLock mLock;
    private boolean                     mSelfLock = false;

    @Override
    public final boolean tryLock(long timeout) {
        try {
            return getLockInstance().tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public final void acquireLock() {
        try {
            if (!getLockInstance().tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
                throw new IllegalStateException(String.format(
                        "Unable to acquire lock on this instance of %s (%s) after %s %s, owner: %s", this.getClass()
                                .getSimpleName(), hashCode(), TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT, getLockInstance()
                                .getOwnerName()));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format("Unable to acquire lock on this instance of %s (%s)", this
                    .getClass().getSimpleName(), hashCode()), e);
        }
        ;
    }

    @Override
    public final void releaseLock() {
        if (mLock.isLocked()) {
            this.mLock.unlock();
        }
    }

    public final void internalReleaseLock() {
        if (mSelfLock) {
            releaseLock();
            mSelfLock = false;
        }
    }

    @Override
    public final boolean isLockOwnedByCurrentThread() {
        return this.mLock.isHeldByCurrentThread();
    }

    @Override
    public final ExtendedReentrantLock getLockInstance() {
        return this.mLock;
    }

    /**
     * Check the lock state of this object
     * 
     * @return <code>true</code> if there was no previous lock and lock was acquired, <code>false</code> if the lock was
     *         already owned by the current thread
     * @throws ConcurrentModificationException
     */
    public final boolean checkLock() throws ConcurrentModificationException {
        if (!isLockOwnedByCurrentThread() && mLock.isLocked()) {
            throw new ConcurrentModificationException(String.format(
                    "The current thread (%s) does not have the lock on this instance of %s, owner: %s",
                    Thread.currentThread(), this.getClass().getSimpleName(), getLockInstance().getOwnerName()));
        } else if (!mLock.isLocked()) {
            acquireLock();
            mSelfLock = true;
            return true;
        } else {
            mSelfLock = false;
            return false;
        }
    }

    // ------------------------------------

    @Override
    public abstract ITRSPTour clone();
}
