/**
 * 
 */
package vroom.common.utilities.gurobi;

import gurobi.GRB.IntAttr;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBModel;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

import vroom.common.utilities.logging.LoggerHelper;

/**
 * <code>GRBConstraintManager</code> is a utility class to manage a set of constraints.
 * <p>
 * In particular it can be used in cut generation to manage a set of cut and automatically remove cuts that are no
 * longer active
 * </p>
 * <p>
 * Creation date: 19/08/2010 - 10:10:43
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBConstraintManager {

    public static final LoggerHelper LOGGER = LoggerHelper.getLogger(GRBConstraintManager.class
                                                    .getSimpleName());

    public static enum OrderingCriterion {
        AGE, ACTIVE_COUNT, INACTIVE_COUNT
    }

    private final GRBModel           mModel;

    private final Set<GRBConstraint> mConstraints;

    /**
     * Returns the number of constraints in this manager
     * 
     * @return the number of constraints in this manager
     */
    public int getConstraintCount() {
        return mConstraints.size();
    }

    /** The ordering criterion for this constraint manager **/
    private OrderingCriterion mOrderingCriterion;

    /**
     * Getter for the ordering criterion for this constraint manager
     * 
     * @return the ordering criterion
     */
    public OrderingCriterion getOrderingCriterion() {
        return mOrderingCriterion;
    }

    /**
     * Setter for the ordering criterion for this constraint manager
     * 
     * @param criterion
     *            the value to be set for name
     */
    public void setOrderingCriterion(OrderingCriterion criterion) {
        mOrderingCriterion = criterion;
    }

    /** A threshold determining which constraints should be trashed **/
    private int mTrashThreshold;

    /**
     * Getter for the threshold determining which constraints should be trashed
     * 
     * @return the value of the trash threshold
     */
    public int getTrashThreshold() {
        return mTrashThreshold;
    }

    /**
     * Setter for the threshold determining which constraints should be trashed
     * 
     * @param threshold
     *            the value to be set for the trash threshold
     */
    public void setTrashThreshold(int threshold) {
        mTrashThreshold = threshold;
    }

    /** the maximum number of constraints to be kept **/
    private int mMaxConstraints;

    /**
     * Getter for maxConstraints : the maximum number of constraints to be kept
     * 
     * @return the value of maxConstraints
     */
    public int getMaxConstraints() {
        return mMaxConstraints;
    }

    /**
     * Setter for maxConstraints : the maximum number of constraints to be kept
     * 
     * @param maxConstraints
     *            the value to be set for maxConstraints
     */
    public void setMaxConstraints(int maxConstraints) {
        mMaxConstraints = maxConstraints;
    }

    /**
     * Creates a new <code>GRBConstraintManager</code>
     * 
     * @param model
     * @param orderingCriterion
     * @param trashThreshold
     */
    public GRBConstraintManager(GRBModel model, OrderingCriterion orderingCriterion,
            int trashThreshold) {
        mModel = model;
        mOrderingCriterion = orderingCriterion;
        mTrashThreshold = trashThreshold;
        mConstraints = new HashSet<GRBConstraint>();
        mMaxConstraints = Integer.MAX_VALUE;
    }

    /**
     * Add a constraint to this manager and to the underlying model
     * 
     * @param constraint
     * @throws GRBException
     * @throws {@link IllegalStateException} if the constraint was already added to another model
     */
    public void addConstraint(GRBConstraint constraint) throws GRBException {
        if (constraint.getConstraint() == null) {
            constraint.addToModel(mModel);
        }
        registerConstraint(constraint);
    }

    /**
     * Add a constraint to this manager, checking that it is already in the underlying model
     * 
     * @param constraint
     * @throws {@link IllegalStateException} if the constraint was already added to another model
     */
    public void registerConstraint(GRBConstraint constraint) {
        if (constraint.getModel() != mModel) {
            throw new IllegalStateException("The constraint was already added to another model");
        }
        mConstraints.add(constraint);
    }

    /**
     * Update the constraint statistics
     */
    public void updateStats() {
        GRBConstr[] constrs = new GRBConstr[mConstraints.size()];
        GRBConstraint[] constraints = new GRBConstraint[mConstraints.size()];
        int i = 0;
        for (GRBConstraint c : mConstraints) {
            constraints[i] = c;
            constrs[i++] = c.getConstraint();
        }

        int[] states = null;
        try {
            GRBModel model = mModel;
            if (mModel.get(IntAttr.IsMIP) == 1) {
                model = mModel.fixedModel();
                model.optimize();
            }
            // Get the constraint basic state (0 in basis, -1 out)
            states = model.get(IntAttr.CBasis, constrs);
            LOGGER.lowDebug("updateStats: Constraint stats updated");

        } catch (GRBException e) {
            LOGGER.exception("GRBConstraintManager.updateStats", e);
        } finally {
            if (states == null) {
                return;
            }
        }

        for (int j = 0; j < states.length; j++) {
            constraints[j].updateAge(states[j] == 0);
        }
    }

    /**
     * Trash constraints that do not satisfy the {@linkplain #getTrashThreshold() threshold} defined for the given
     * {@linkplain #getOrderingCriterion() ordering criterion}.
     * 
     * @param autoRemove
     *            <code>true</code> if the trashed constraints should be automatically removed from the model
     */
    public void trashConstraints(boolean autoRemove) {
        PriorityQueue<GRBConstraint> trashQueue = null;
        if (getConstraintCount() > getMaxConstraints()) {
            trashQueue = new PriorityQueue<GRBConstraint>(getConstraintCount(),
                    new ConstraintComparator());
        }

        LinkedList<GRBConstraint> trashConstraints = new LinkedList<GRBConstraint>();

        for (GRBConstraint c : mConstraints) {
            // Check threshold
            if (!checkThreshold(c)) {
                trashConstraints.add(c);
                // Add to trash queue if too many constraints
            } else if (trashQueue != null) {
                trashQueue.add(c);
            }
        }

        // Trash constraints
        // Exceeding constraints
        int exc = trashQueue == null ? 0 : getMaxConstraints() - trashQueue.size();
        // Too many constraints
        while (exc > 0) {
            GRBConstraint c = trashQueue.poll();
            trashConstraints.add(c);
            exc--;
        }

        // Remove the trashed constraints from the model
        if (autoRemove) {
            for (GRBConstraint c : trashConstraints) {
                try {
                    mModel.remove(c.getConstraint());
                    LOGGER.debug("updateStats: Constraint removed - %s", c);
                } catch (GRBException e) {
                    LOGGER.exception("GRBConstraintManager.trashConstraints (constraint %s)", e, c);
                }
            }
            try {
                mModel.update();
            } catch (GRBException e) {
                LOGGER.exception("GRBConstraintManager.trashConstraints - model update", e);
            }
        }
        mConstraints.removeAll(trashConstraints);

    }

    /**
     * Check whether or not a constraint satisfies the trash threshold
     * 
     * @param grbConstraint
     * @return <code>true</code> if the constraint is to be kept, <code>false</code> if it should be trashed
     */
    private boolean checkThreshold(GRBConstraint grbConstraint) {
        switch (getOrderingCriterion()) {
        case AGE:
            return grbConstraint.getAge() < getTrashThreshold();
        case ACTIVE_COUNT:
            return grbConstraint.getActiveCount() > getTrashThreshold();
        case INACTIVE_COUNT:
            return grbConstraint.getInactiveCount() < getTrashThreshold();
        default:
            return true;
        }
    }

    private class ConstraintComparator implements Comparator<GRBConstraint> {

        @Override
        public int compare(GRBConstraint o1, GRBConstraint o2) {
            switch (getOrderingCriterion()) {
            case AGE:
                return o2.getAge() - o1.getAge();
            case ACTIVE_COUNT:
                return o1.getActiveCount() - o2.getActiveCount();
            case INACTIVE_COUNT:
                return o2.getInactiveCount() - o1.getInactiveCount();
            default:
                return 0;
            }
        }

    }

}
