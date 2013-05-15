package vroom.common.modeling.dataModel;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import vroom.common.utilities.ExtendedReentrantLock;

/**
 * <code>VRPSolution</code> is the base class used to store a solution to a VRP problem <br>
 * In particular, it contains a set of {@link RouteBase} of type <code>R</code>, that can be iterated over by the
 * {@link Iterable} interface.
 * 
 * @param <R>
 *            the type of {@link RouteBase} used to represent the routes
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a> - <a
 *         href="http://copa.uniandes.edu.co">Copa</a>, <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0 #created 15-Feb-2010 11:29:51 a.m.
 */
public class Solution<R extends IRoute<?>> implements IVRPSolution<R> {

    /** The parent instance for this solution *. */
    private final IVRPInstance mParentInstance;

    /** The m routes. */
    private List<R>            mRoutes;

    /**
     * Creates a new <code>VRPSolution</code> associated with the given.
     * 
     * @param parentInstance
     *            the parent instance {@link InstanceBase}
     */
    public Solution(IVRPInstance parentInstance) {
        this.mParentInstance = parentInstance;

        this.mLock = new ExtendedReentrantLock();

        if (this.mParentInstance.getFleet().isUnlimited()) {
            this.mRoutes = new ArrayList<R>();
        } else {
            this.mRoutes = new ArrayList<R>(this.mParentInstance.getFleet().size());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#addRoute(R)
     */
    @Override
    public void addRoute(R route) {
        this.mRoutes.add(route);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#addRoute(int,R)
     */
    @Override
    public void addRoute(R route, int index) {
        this.mRoutes.add(index, route);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.modeling.dataModel.ISolution#removeRoute(vroom.modelling.
     * VroomModelling.dataModel.IRoute)
     */
    @Override
    public void removeRoute(IRoute<?> route) {
        this.mRoutes.remove(route);
    };

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#getCost()
     */
    @Override
    public double getCost() {
        double cost = 0;

        for (R route : this.mRoutes) {
            cost += route.getCost();
        }

        return cost;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.utilities.optimization.ISolution#getObjectiveValue()
     */
    @Override
    public double getObjectiveValue() {
        return getCost();
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#getParentInstance()
     */
    @Override
    public IVRPInstance getParentInstance() {
        return this.mParentInstance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#getRoute(int)
     */
    @Override
    public R getRoute(int index) {
        return this.mRoutes.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#getRouteCount()
     */
    @Override
    public int getRouteCount() {
        return this.mRoutes.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#toString()
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(mRoutes.size() * 50);

        b.append(String.format("Cost:%.2f, %s Routes: {", getCost(), this.getRouteCount()));
        for (IRoute<?> r : this) {
            b.append(r.toString());
            b.append(',');
        }

        if (mRoutes.size() > 0) {
            b.setCharAt(b.length() - 1, '}');
        } else {
            b.append('}');
        }

        return b.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#iterator()
     */
    @Override
    public ListIterator<R> iterator() {
        return mRoutes.listIterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Solution<R> clone() {
        Solution<R> clone = new Solution<R>(getParentInstance());

        for (R route : this) {
            clone.addRoute((R) route.clone());
        }

        return clone;
    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.modeling.dataModel.ISolution#clear()
     */
    @Override
    public void clear() {
        this.mRoutes.clear();

    }

    /*
     * (non-Javadoc)
     * 
     * @see vroom.common.utilities.optimization.ISolution#getFitnessValue()
     */
    @Override
    public Comparable<?> getObjective() {
        return getCost();
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
    public int hashSolution() {
        return hashCode();
    }
}// end VRPSolution