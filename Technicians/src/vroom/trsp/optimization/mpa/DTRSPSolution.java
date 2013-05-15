/**
 * 
 */
package vroom.trsp.optimization.mpa;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import vroom.common.utilities.Utilities;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.ISampledRequest;
import vroom.optimization.online.jmsa.IScenario;
import vroom.trsp.datamodel.ITRSPNode;
import vroom.trsp.datamodel.ITRSPTour;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.Technician;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;

/**
 * <code>DTRSPSolution</code> is an implementation of {@link IScenario} wrapping a {@link TRSPSolution}
 * <p>
 * Creation date: Feb 7, 2012 - 10:39:21 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPSolution extends TRSPSolution implements IScenario, IDistinguishedSolution {

    private final long mTimeStamp;

    /**
     * Returns the time at which this scenario was created
     * 
     * @return the time at which this scenario was created
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Returns the time at which this scenario was created as a string
     * 
     * @return the time at which this scenario was created as a string
     */
    public String getTimeStampString() {
        return Utilities.Time.TIME_STAMP_FORMAT.format(new Date(mTimeStamp));
    }

    /**
     * Creates a new <code>DTRSPSolution</code>
     * 
     * @param parent
     */
    public DTRSPSolution(TRSPSolution parent) {
        super(parent);
        mTimeStamp = System.currentTimeMillis();
    }

    @Override
    protected GiantPermutation clonePermutation(GiantPermutation perm) {
        DynGiantPermutation clone = new DynGiantPermutation(this);
        clone.importPermutation(perm);
        return clone;
    }

    @Override
    protected TRSPTour cloneTour(TRSPTour tour) {
        return new DTRSPTour(this, tour);
    }

    /**
     * Creates a new <code>DTRSPSolution</code>
     * 
     * @param instance
     *            the parent instance
     * @param costDelegate
     *            the cost delegate used to evaluate tours
     */
    public DTRSPSolution(TRSPInstance instance, TRSPCostDelegate costDelegate) {
        super(instance, costDelegate);
        mTimeStamp = System.currentTimeMillis();
    }

    @Override
    protected GiantPermutation newGiantPermutation() {
        return new DynGiantPermutation(this);
    }

    @Override
    protected TRSPTour newTour(Technician t) {
        return new DTRSPTour(this, t);
    }

    @Override
    public DTRSPSolution clone() {
        return new DTRSPSolution(this);
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.IScenario#getActualRequests()
     */
    @Override
    public List<? extends IActualRequest> getActualRequests() {
        // FIXME Implement getActualRequests
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends ISampledRequest> getSampledRequests() {
        return Collections.emptyList();
    }

    @Override
    public IActualRequest getFirstActualRequest(int resource) {
        // return getInstance().getTRSPNode(getTour(resource).getFirstNode());
        TRSPTour t = getTour(resource);
        ITRSPNode current = getInstance().getSimulator().getCurrentNode(resource);
        int succ = t.getSucc(current.getID());

        return succ != ITRSPTour.UNDEFINED ? getInstance().getTRSPNode(succ) : null;
    }

    @Override
    public IActualRequest fixFirstActualRequest(int resource) {
        // This is implemented at the instance.simulator/constraint handler level
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.IScenario#markLastVisitAsServed(int)
     */
    @Override
    public boolean markLastVisitAsServed(int resource) {
        // This is implemented at the instance.simulator/constraint handler level
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * @see vroom.optimization.online.jmsa.IScenario#getOrderedActualRequests(int)
     */
    @Override
    public List<? extends IActualRequest> getOrderedActualRequests(int resource) {
        // This is implemented at the instance.simulator/constraint handler level
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<? extends ISampledRequest> getOrderedSampledRequests(int resource) {
        return Collections.emptyList();
    }

    @Override
    public int getResourceCount() {
        return getTourCount();
    }

    private int mNonImproving = 0;

    @Override
    public void incrementNonImprovingCount() {
        mNonImproving++;
    }

    @Override
    public void resetNonImprovingCount() {
        mNonImproving = 0;
    }

    @Override
    public int getNonImprovingCount() {
        return mNonImproving;
    }

    @Override
    public void dereference() {
    }

    @Override
    public IActualRequest getNextRequest() {
        throw new UnsupportedOperationException("Must specify a resource");
    }

    @Override
    public IActualRequest getNextRequest(int resource) {
        ITRSPNode current = getInstance().getSimulator().getCurrentNode(resource);
        if (current != null) {
            int next = getTour(resource).getSucc(current.getID());
            return next != ITRSPTour.UNDEFINED ? getInstance().getTRSPNode(next) : null;
        } else {
            return getInstance().getTRSPNode(getTour(resource).getFirstNode());
        }
    }

    @Override
    protected DTRSPSolution.DynGiantPermutation getGiantPermutation() {
        return (DynGiantPermutation) super.getGiantPermutation();
    }

    @Override
    public int hashCode() {
        return super.defaultHashCode();
    }

    @Override
    public DTRSPTour getTour(int techId) {
        return (DTRSPTour) super.getTour(techId);
    }

    /**
     * Freeze this solution to prevent any changes
     */
    public void freeze() {
        getGiantPermutation().freeze();
    }

    /**
     * Freeze a specific node to prevent any changes
     * 
     * @param node
     *            the node to be frozen
     * @param arrivalTime
     *            the arrival time at the frozen node
     * @param departureTime
     *            the departure time at the frozen node
     */
    public void freeze(int node, double arrivalTime, double departureTime) {
        getGiantPermutation().freeze(node, arrivalTime, departureTime);
        getVisitingTour(node).propagateTime(node); // FIXME this does not propagate slack time properly (25/06/14:
                                                   // confirmed)
        getCostDelegate().nodeFrozen(getVisitingTour(node), node);
    }

    /**
     * Unfreeze this solution to allow changes
     */
    public void unfreeze() {
        getGiantPermutation().unfreeze();
    }

    /**
     * Return {@code true} if {@code  node} is frozen
     * 
     * @param node
     * @return {@code true} if {@code  node} is frozen
     */
    public boolean isFrozen(int node) {
        return getGiantPermutation().isFrozen(node);
    }

    /**
     * <code>TRSPDynTour</code> is an extension of {@link TRSPTour} with adjustments for dynamic contexts
     * <p>
     * Creation date: Apr 30, 2012 - 10:37:11 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class DTRSPTour extends TRSPTour {

        public DTRSPTour(DTRSPSolution solution, Technician technician) {
            super(solution, technician);
        }

        public DTRSPTour(DTRSPSolution solution, TRSPTour tour) {
            super(solution, tour.getTechnician());
            setAutoUpdated(tour.isAutoUpdated());
            setFirst(tour.getFirstNode());
            setLast(tour.getLastNode());
            setLength(tour.length());
            setTotalCost(this.getTotalCost());
        }

        @Override
        protected DTRSPTour cloneInternal(TRSPSolution solution) {
            return new DTRSPTour((DTRSPSolution) solution, getTechnician());
        }

        @Override
        public DTRSPSolution getSolution() {
            return (DTRSPSolution) super.getSolution();
        }

        @Override
        protected DTRSPSolution.DynGiantPermutation getPermutation() {
            return (DTRSPSolution.DynGiantPermutation) super.getPermutation();
        }

        /**
         * Returns the latest time at which the tour can be started so that it ends at the earliest date possible and no
         * time window is violated
         * 
         * @return the latest time at which the tour can be started
         */
        @Override
        public double getLatestStartTime() {
            int secondNode = getSucc(getFirstNode());
            if (length() < 2)
                return getEarliestStartTime();
            if (getSolution().isFrozen(secondNode))
                return getEarliestArrivalTime(secondNode)
                        - getTravelTime(getFirstNode(), secondNode);
            else if (getPermutation().isFwdSlackTimeDefined())
                return getEarliestStartTime()
                        + Math.min(getFwdSlackTime(getFirstNode()),
                                getWaitingTime(getFirstNode(), getLastNode()));
            else
                return getEarliestStartTime();
        }

        @Override
        public double getEarliestDepartureTime(int node) {
            if (getPermutation().isFrozen(node))
                return getPermutation().getEarliestDeparture(node);
            else
                return super.getEarliestDepartureTime(node);
        }

        /**
         * Sets the earliest departure time for a node
         * 
         * @param node
         * @param time
         */
        public void setEarliestDepartureTime(int node, double time) {
            if (getPermutation().isFrozen(node)) {
                getPermutation().setEarliestDeparture(node, time);
                propagateTime(node);
            } else
                throw new IllegalStateException(
                        "Cannot set the earliest departure time of an unfrozen node");
        }

        @Override
        protected boolean isUpdateAllowed(int node) {
            return !getSolution().isFrozen(node);
        }
    }

    /**
     * <code>DynGiantPermutation</code> is an extension of {@link GiantPermutation} that is used in dynamic context
     * <p>
     * Creation date: Apr 26, 2012 - 4:56:25 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class DynGiantPermutation extends GiantPermutation {

        private final boolean[] mFrozenNodes;

        /** the frozen flag, if {@code true} no changes will be permitted **/
        private boolean         mFrozen;

        /** the earliest departure time for frozen nodes */
        private final double[]  mDeparture;

        /**
         * Returns {@code true} if this permutation is frozen, {@code false} otherwise
         * 
         * @return {@code true} if this permutation is frozen, {@code false} otherwise
         */
        public boolean isFrozen() {
            return this.mFrozen;
        }

        /**
         * Returns {@code true} if the specified {@code  node} is frozen, {@code false} otherwise
         * 
         * @param node
         *            the node to be checked
         * @return {@code true} if the specified {@code  node} is frozen, {@code false} otherwise
         */
        @Override
        public boolean isFrozen(int node) {
            return this.mFrozenNodes[node];
        }

        /**
         * Freeze this permutation to prevent any changes
         */
        public void freeze() {
            this.mFrozen = true;
        }

        /**
         * Freeze a node to prevent further changes
         * 
         * @param node
         *            the node to freeze
         * @param arrivalTime
         *            the arrival time at {@code  node}
         * @param departureTime
         *            the departure time at {@code  node}
         */
        public void freeze(int node, double arrivalTime, double departureTime) {
            super.setEarliestArrivalTime(node, arrivalTime);
            this.mDeparture[node] = departureTime;
            this.mFrozenNodes[node] = true;
        }

        /**
         * Unfreeze this permutation to allow changes
         */
        public void unfreeze() {
            this.mFrozen = false;
        }

        /**
         * Creates a new <code>FreezableGiantPermutation</code>
         * 
         * @param solution
         */
        public DynGiantPermutation(TRSPSolution solution) {
            super(solution);
            mFrozenNodes = new boolean[solution.getInstance().getMaxId()];
            mDeparture = new double[solution.getInstance().getMaxId()];
        }

        private void checkState() {
            if (isFrozen())
                throw new IllegalStateException("Attempting to modify a frozen solution");
        }

        @Override
        public void setAvailableSpareParts(int node, int type, int num) {
            checkState();
            super.setAvailableSpareParts(node, type, num);
        }

        @Override
        public void setCumulativeCost(int node, double value) {
            checkState();
            super.setCumulativeCost(node, value);
        }

        @Override
        public void setEarliestArrivalTime(int node, double time) {
            checkState();
            if (isFrozen(node))
                throw new IllegalStateException("Attempting to set a value for a frozen node: "
                        + node);

            super.setEarliestArrivalTime(node, time);
        }

        @Override
        public void setFwdSlackTime(int i, int j, double slack) {
            checkState();
            super.setFwdSlackTime(i, j, slack);
        }

        @Override
        public void setLatestFeasibleTime(int node, double time) {
            checkState();
            super.setLatestFeasibleTime(node, time);
        }

        @Override
        public void setMainDepotVisited(int node, boolean visited) {
            checkState();
            super.setMainDepotVisited(node, visited);
        }

        @Override
        public void setPred(int node, int pred) {
            checkState();
            super.setPred(node, pred);
        }

        @Override
        public void setRequiredSpareParts(int node, int type, int num) {
            checkState();
            super.setRequiredSpareParts(node, type, num);
        }

        @Override
        public void setSucc(int node, int succ) {
            checkState();
            super.setSucc(node, succ);
        }

        @Override
        public void setToolAvailability(int node, int tool, boolean available) {
            checkState();
            super.setToolAvailability(node, tool, available);
        }

        @Override
        public void setVisitingTechnician(int node, int techId) {
            checkState();
            super.setVisitingTechnician(node, techId);
        }

        @Override
        public void setWaitingTime(int i, int j, double time) {
            checkState();
            super.setWaitingTime(i, j, time);
        }

        @Override
        public void setWaitingTime(int node, double time) {
            checkState();
            super.setWaitingTime(node, time);
        }

        @Override
        protected GiantPermutation cloneInternal(TRSPSolution solution) {
            DynGiantPermutation clone = new DynGiantPermutation(solution);
            for (int i = 0; i < mFrozenNodes.length; i++) {
                clone.mFrozenNodes[i] = this.mFrozenNodes[i];
                clone.mDeparture[i] = this.mDeparture[i];
            }
            clone.mFrozen = this.mFrozen;
            return clone;
        }

        @Override
        protected void importPermutationInternal(GiantPermutation giantPermutation) {
            DynGiantPermutation perm = (DynGiantPermutation) giantPermutation;
            for (int i = 0; i < mFrozenNodes.length; i++) {
                this.mFrozenNodes[i] = perm.mFrozenNodes[i];
                this.mDeparture[i] = perm.mDeparture[i];
            }
            this.mFrozen = perm.mFrozen;
        }

        @Override
        protected void resetNodeData(int node) {
            this.mFrozenNodes[node] = false;
            this.mDeparture[node] = NA;
            super.resetNodeData(node);
        }

        /**
         * Sets the earliest departure for a given node
         * 
         * @param node
         * @param time
         */
        public void setEarliestDeparture(int node, double time) {
            if (isFrozen(node))
                throw new IllegalStateException("Attempting to set a value for a frozen node "
                        + node);
            mDeparture[node] = time;
        }

        /**
         * Returns the stored earliest departure for the given node
         * 
         * @param node
         * @return the earliest departure time from node
         */
        public double getEarliestDeparture(int node) {
            return mDeparture[node];
        }
    }
}
