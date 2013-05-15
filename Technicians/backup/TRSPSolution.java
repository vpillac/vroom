/**
 *
 */
package vroom.trsp.datamodel;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import vroom.common.utilities.ArrayIterator;
import vroom.common.utilities.ExtendedReentrantLock;
import vroom.common.utilities.IntegerSet;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.optimization.ISolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;

/**
 * <code>TRSPSolution</code> is a class representing a solution for the TRSP. It contains a set of {@link TRSPTour},
 * each associated with a {@link Technician}
 * <p>
 * Creation date: Mar 24, 2011 - 2:14:41 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPSolution implements ISolution, Iterable<TRSPTour> {

    /** the set of unserved requests **/
    private final Set<Integer> mUnservedRequests;

    /**
     * Returns the set of unserved requests <br/>
     * This set is initialized with all requests but should be maintained externally
     * 
     * @return the set of unserved requests
     */
    public Set<Integer> getUnservedRequests() {
        return this.mUnservedRequests;
    }

    /** The tours contained in this solution */
    private final TRSPTour[] mTours;

    /**
     * Returns the tour associated with a given technician
     * 
     * @param techId
     *            the id of the technician
     * @return the tour associated with the technician with id <code>techID</code>
     */
    public TRSPTour getTour(int techId) {
        return mTours[techId];
    }

    /**
     * Returns the tour that visits a given request
     * 
     * @param reqId
     *            the considered request
     * @return the tour that visits the request with id <code>reqId</code>
     */
    public TRSPTour getVisitingTour(int reqId) {
        for (TRSPTour t : this)
            if (t.isVisited(reqId))
                return t;

        return null;
    }

    /**
     * Add the tour to this solution, replacing possible previous values
     * 
     * @param tour
     *            the tour to be added
     * @return the tour previously associated with the same technician
     */
    public TRSPTour setTour(TRSPTour tour) {
        TRSPTour prev = getTour(tour.getTechnician().getID());
        mTours[tour.getTechnician().getID()] = tour;
        return prev;
    }

    /** the parent instance of this solution **/
    private final TRSPInstance mInstance;

    /**
     * Getter for the parent instance of this solution
     * 
     * @return the value of the instance
     */
    public TRSPInstance getInstance() {
        return this.mInstance;
    }

    /** the cost delegate used in this solution tours **/
    private TRSPCostDelegate mCostDelegate;

    /**
     * Getter for the cost delegate used in this solution tours
     * 
     * @return the value of costDelegate
     */
    public TRSPCostDelegate getCostDelegate() {
        return this.mCostDelegate;
    }

    /**
     * Setter for the cost delegate used in this solution tours
     * 
     * @param costDelegate
     *            the value to be set for the cost delegate used in this solution tours
     */
    public void setCostDelegate(TRSPCostDelegate costDelegate) {
        this.mCostDelegate = costDelegate;
        getCostDelegate().evaluateSolution(this, true, true);
    }

    /**
     * Creates a new <code>TRSPSolution</code>
     * 
     * @param instance
     */
    public TRSPSolution(TRSPInstance instance, TRSPCostDelegate costDelegate) {
        mLock = new ExtendedReentrantLock();
        mInstance = instance;
        mCostDelegate = costDelegate;
        mTours = new TRSPTour[instance.getFleet().size()];

        mUnservedRequests = new IntegerSet(instance.getMaxId());

        for (TRSPRequest r : instance.getReleasedRequests())
            mUnservedRequests.add(r.getID());

        for (int i = 0; i < mTours.length; i++) {
            mTours[i] = new TRSPTour(this, getInstance().getFleet().getVehicle(i));
        }
    }

    /**
     * Returns the number of tours in this solution
     * 
     * @return the number of tours in this solution
     */
    public int getTourCount() {
        return mTours.length;
    }

    /**
     * Returns the number of non-empty tours in this solution
     * 
     * @return the number of non-empty tours in this solution
     */
    public int getActualTourCount() {
        int count = 0;

        for (TRSPTour t : this) {
            for (int n : t) {
                if (getInstance().isRequest(n)) {
                    // This tour visits a request, it is not empty
                    count++;
                    break;
                }
            }
        }

        return count;
    }

    @Override
    public Comparable<?> getObjective() {
        return getObjectiveValue();
    }

    @Override
    public double getObjectiveValue() {
        return getCostDelegate().evaluateSolution(this, false, false);
    }

    /**
     * Returns the number of requests that are not served by this solution
     * 
     * @return the number of requests that are not served by this solution
     */
    public int getUnservedCount() {
        return getUnservedCount();
    }

    /**
     * Returns the number of request that are present in this solution
     * <p>
     * This method will iterate over all tours
     * </p>
     * 
     * @return the number of request that are present in this solution
     */
    public int getServedCount() {
        int count = 0;

        for (TRSPTour t : this)
            for (Integer n : t)
                if (getInstance().isRequest(n))
                    count++;

        return count;
    }

    @Override
    public TRSPSolution clone() {
        // TODO TRSPTour redesign: optimize the cloning
        TRSPSolution clone = new TRSPSolution(getInstance(), getCostDelegate());
        for (int t = 0; t < this.mTours.length; t++) {
            clone.mTours[t] = this.mTours[t].clone();
        }
        clone.mUnservedRequests.clear();
        clone.mUnservedRequests.addAll(mUnservedRequests);
        return clone;
    }

    @Override
    public Iterator<TRSPTour> iterator() {
        return new ArrayIterator<TRSPTour>(mTours);
    }

    // ------------------------------------
    // ILockable interface implementation
    // ------------------------------------
    /** A lock to be used by this instance */
    private final ExtendedReentrantLock mLock;
    private boolean                     mSelfLock = false;

    @Override
    public boolean tryLock(long timeout) {
        try {
            return getLockInstance().tryLock(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    @Override
    public void acquireLock() {
        try {
            if (!getLockInstance().tryLock(TRY_LOCK_TIMOUT, TRY_LOCK_TIMOUT_UNIT)) {
                throw new IllegalStateException(
                        String.format(
                                "Unable to acquire lock on this instance of %s (%s) after %s %s, owner: %s",
                                this.getClass().getSimpleName(), hashCode(), TRY_LOCK_TIMOUT,
                                TRY_LOCK_TIMOUT_UNIT, getLockInstance().getOwnerName()));
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(String.format(
                    "Unable to acquire lock on this instance of %s (%s)", this.getClass()
                            .getSimpleName(), hashCode()), e);
        }
        ;
    }

    @Override
    public void releaseLock() {
        if (mLock.isLocked()) {
            this.mLock.unlock();
        }
    }

    public void internalReleaseLock() {
        if (mSelfLock) {
            releaseLock();
            mSelfLock = false;
        }
    }

    @Override
    public boolean isLockOwnedByCurrentThread() {
        return this.mLock.isHeldByCurrentThread();
    }

    @Override
    public ExtendedReentrantLock getLockInstance() {
        return this.mLock;
    }

    /**
     * Check the lock state of this object
     * 
     * @return <code>true</code> if there was no previous lock and lock was acquired, <code>false</code> if the lock was
     *         already owned by the current thread
     * @throws ConcurrentModificationException
     */
    public boolean checkLock() throws ConcurrentModificationException {
        if (!isLockOwnedByCurrentThread() && mLock.isLocked()) {
            throw new ConcurrentModificationException(
                    String.format(
                            "The current thread (%s) does not have the lock on this instance of %s, owner: %s",
                            Thread.currentThread(), this.getClass().getSimpleName(),
                            getLockInstance().getOwnerName()));
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
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append(String.format("Cost:%.2f, Unserved:%s Tours [", getObjectiveValue(),
                Utilities.toShortString(getUnservedRequests())));
        Iterator<TRSPTour> it = iterator();
        while (it.hasNext()) {
            b.append(it.next().toString());
            if (it.hasNext())
                b.append(',');
        }

        b.append(']');

        return b.toString();
    }

    @Override
    public int hashCode() {
        return getInstance().getSolutionHasher().hash(this);
    }

    /**
     * Convert a solution to a giant tour
     * 
     * @return a list containing all the tours concatenated into a giant tour
     */
    public List<Integer> toGiantTour() {
        ArrayList<Integer> tour = new ArrayList<Integer>(getInstance().getMaxId() + 1);
        for (ITRSPTour t : this) {
            for (Integer node : t) {
                tour.add(node);
            }
        }
        return tour;
    }

    /**
     * Convert a solution to a short string of the form: <br/>
     * <code>"<1,11,12,0,13,21|2,14,15,16,22>"</code>
     * 
     * @return a short string describing this solution
     */
    public String toShortString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        for (ITRSPTour tour : this) {
            if (sb.length() > 1)
                sb.append("|");
            int idx = 0;
            for (int node : tour) {
                if (idx > 0)
                    sb.append(",");
                sb.append(node);
                idx++;
            }
        }
        sb.append(">");

        return sb.toString();
    }

}
