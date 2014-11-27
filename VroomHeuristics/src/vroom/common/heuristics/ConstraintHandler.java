/**
 * 
 */
package vroom.common.heuristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.optimization.IMove;

/**
 * <code>ConstraintHandler</code> is a utility class for the handling of a set of constraints.
 * <p>
 * It implements {@link IConstraint} in order to be transparently used as a simple constraint.
 * <p>
 * Creation date: Jun 22, 2010 - 9:08:40 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ConstraintHandler<S> implements IConstraint<S>, Iterable<IConstraint<S>> {

    private final List<IConstraint<S>> mConstraints;

    /**
     * Creates a new <code>ConstraintHandler</code> with an initial set of constraints
     * <p>
     * Please note that constraints will be checked in the same order they were added
     * </p>
     * 
     * @param constraints
     *            the constraints to be added to the new handler
     */
    public ConstraintHandler(IConstraint<S>... constraints) {
        mConstraints = new ArrayList<IConstraint<S>>(constraints.length);
        for (IConstraint<S> c : constraints)
            mConstraints.add(c);
    }

    /**
     * Creates a new <code>ConstraintHandler</code>
     */
    public ConstraintHandler() {
        mConstraints = new ArrayList<IConstraint<S>>();
    }

    /**
     * Returns the number of constraints registered in this handler
     * 
     * @return the number of constraints registered in this handler
     */
    public int size() {
        return mConstraints.size();
    }

    /**
     * Add a constraint to this handler.
     * <p>
     * Please note that constraints will be checked in the same order they were added
     * </p>
     * 
     * @param constraint
     *            the constraint to be added
     * @return <code>true</code> if the constraint was not already registered
     */
    public boolean addConstraint(IConstraint<S> constraint) {
        return mConstraints.add(constraint);
    }

    /**
     * Add a constraint to this handler.
     * <p>
     * Please note that constraints will be checked in the same order they were added
     * </p>
     * 
     * @param idx
     *            the index at which the constraint should be added
     * @param constraint
     *            the constraint to be added
     * @return <code>true</code> if the constraint was not already registered
     * @see List#add(int, Object)
     */
    public void addConstraint(int idx, IConstraint<S> constraint) {
        mConstraints.add(idx, constraint);
    }

    /**
     * Remove a constraint from this handler.
     * <p>
     * Please note that if a constraint was added more than once only the first occurrence will be removed
     * </p>
     * 
     * @param constraint
     *            the constraint to be removed
     * @return <code>true</code> if the constraint was registered
     */
    public boolean removeConstraint(IConstraint<S> constraint) {
        return mConstraints.remove(constraint);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.heuristics.IConstraint#checkIMove(vroom.common.heuristics
     * .ISolution, vroom.common.heuristics.IMove)
     */
    @Override
    public boolean isFeasible(S solution, IMove move) {
        for (IConstraint<S> c : this) {
            if (!c.isFeasible(solution, move)) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.heuristics.IConstraint#checkSolution(vroom.common.heuristics
     * .ISolution)
     */
    @Override
    public boolean isFeasible(S solution) {
        for (IConstraint<S> c : this) {
            if (!c.isFeasible(solution)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check the violated constraints
     * 
     * @param mSolution
     *            the mSolution to be checked
     * @return a collection containing the violated constraints
     */
    public Collection<IConstraint<S>> getViolatedConstraints(S solution) {
        LinkedList<IConstraint<S>> ctr = new LinkedList<IConstraint<S>>();
        for (IConstraint<S> c : mConstraints) {
            if (!c.isFeasible(solution)) {
                ctr.add(c);
            }
        }
        return ctr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.heuristics.IConstraint#getInfeasibilityExplanation(java.
     * lang.Object)
     */
    @Override
    public String getInfeasibilityExplanation(S solution) {
        StringBuilder sb = new StringBuilder();
        for (IConstraint<S> c : mConstraints) {
            String infeas = c.getInfeasibilityExplanation(solution);
            if (infeas != null) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append('[');
                sb.append(infeas);
                sb.append(']');
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * vroom.common.heuristics.IConstraint#getInfeasibilityExplanation(java.
     * lang.Object, vroom.common.heuristics.IMove)
     */
    @Override
    public String getInfeasibilityExplanation(S solution, IMove move) {
        StringBuilder sb = new StringBuilder();
        for (IConstraint<S> c : mConstraints) {
            String infeas = c.getInfeasibilityExplanation(solution, move);
            if (infeas != null) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append('[');
                sb.append(infeas);
                sb.append(']');
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    @Override
    public ListIterator<IConstraint<S>> iterator() {
        return mConstraints.listIterator();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        s.append("[");
        for (IConstraint<S> c : mConstraints) {
            if (s.length() > 1)
                s.append(",");
            s.append(c.getClass().getSimpleName());
        }
        s.append("]");

        return s.toString();
    }
}
