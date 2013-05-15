package vroom.common.utilities.gurobi;

import gurobi.GRB;
import gurobi.GRB.StringAttr;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;

/**
 * The Class <code>GRBConstraint</code> is a simple wrapper to store the information needed to describe a
 * {@link GRBConstr} in a {@link GRBModel}
 * <p>
 * Creation date: Aug 18, 2010 - 9:55:07 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class GRBConstraint {

    private int mAge;

    /**
     * Getter for the age of this constraint, i.e. the number of times {@link #updateAge(boolean)} has bee called
     * 
     * @return the age of this constraint
     */
    public int getAge() {
        return mAge;
    }

    private int mActiveCount;

    /**
     * Getter for the number of times this constraint was active, i.e. the number of calls to
     * {@link #updateAge(boolean)} with argument <code>true</code>
     * 
     * @return the number of times this constraint was active
     */
    public int getActiveCount() {
        return mActiveCount;
    }

    private int mInactiveCount;

    /**
     * Getter for the number of iteration since the last activation of this constraint, i.e. the number of calls to
     * {@link #updateAge(boolean)} with argument <code>false</code>
     * 
     * @return the lastActive
     */
    public int getInactiveCount() {
        return mInactiveCount;
    }

    private final GRBLinExpr mLinExpr;

    /**
     * Getter for <code>linExpr</code>
     * 
     * @return the linExpr
     */
    public GRBLinExpr getLinExpr() {
        return mLinExpr;
    }

    private char mSense;

    /**
     * Getter for <code>sense</code>
     * 
     * @return the sense
     */
    public char getSense() {
        return mSense;
    }

    /**
     * Setter for <code>sense</code>
     * 
     * @param sense
     *            the sense to set
     * @throws IllegalStateException
     *             if this constraint was already added to a model
     */
    public void setSense(char sense) {
        if (mConstraint != null) {
            throw new IllegalStateException("This constraint was already added to a model");
        }
        mSense = sense;
    }

    private double mRHS;

    /**
     * Getter for <code>rHS</code>
     * 
     * @return the rHS
     * @throws IllegalStateException
     *             if this constraint was already added to a model
     */
    public double getRHS() {
        return mRHS;
    }

    /**
     * Setter for <code>rHS</code>
     * 
     * @param rHS
     *            the rHS to set
     * @throws IllegalStateException
     *             if this constraint was already added to a model
     */
    public void setRHS(double rHS) {
        if (mConstraint != null) {
            throw new IllegalStateException("This constraint was already added to a model");
        }
        mRHS = rHS;
    }

    private String mName;

    /**
     * Getter for <code>name</code>
     * 
     * @return the name
     */
    public String getName() {
        return mName;
    }

    /**
     * Setter for <code>name</code>
     * 
     * @param name
     *            the name to set
     * @throws IllegalStateException
     *             if this constraint was already added to a model
     */
    public void setName(String name) {
        if (mConstraint != null) {
            throw new IllegalStateException("This constraint was already added to a model");
        }
        mName = name;
    }

    private GRBConstr mConstraint;

    /**
     * Getter for <code>constraint</code>
     * 
     * @return the constraint
     */
    public GRBConstr getConstraint() {
        return mConstraint;
    }

    private GRBModel mModel;

    /**
     * Getter for the model to which this constraint was added
     * 
     * @return the model to which this constraint was added
     */
    public GRBModel getModel() {
        return mModel;
    }

    /**
     * Creates a new <code>GRBConstraint</code>
     * 
     * @param linExpr
     * @param sense
     * @param rhs
     */
    public GRBConstraint(GRBLinExpr linExpr, char sense, double rhs, String name) {
        mLinExpr = linExpr;
        mSense = sense;
        mRHS = rhs;
        mName = name;
        mActiveCount = 0;
        mAge = 0;
    }

    /**
     * Creates a new empty <code>GRBConstraint</code>
     */
    public GRBConstraint() {
        mLinExpr = new GRBLinExpr();
        mSense = GRB.LESS_EQUAL;
        mRHS = 0;
    }

    /**
     * Add this constraint the given model
     * 
     * @param model
     * @return the corresponding {@link GRBConstr} object
     * @throws GRBException
     * @see {@link GRBModel#addConstr(GRBLinExpr, char, double, String)}
     */
    public GRBConstr addToModel(GRBModel model) throws GRBException {
        if (mConstraint != null) {
            throw new IllegalStateException("This constraint was already added to a model");
        }
        mConstraint = model.addConstr(mLinExpr, mSense, mRHS, mName);
        mModel = model;
        return mConstraint;
    }

    /**
     * Update this constraint {@linkplain #getAge() age}, {@linkplain #getActiveCount() activation count} and
     * {@linkplain #getInactiveCount() number of inactive iterations}
     * 
     * @param isActive
     *            <code>true</code> if the constraint is active is this iteration
     */
    public void updateAge(boolean isActive) {
        mAge++;
        if (isActive) {
            mActiveCount++;
            mInactiveCount = 0;
        } else {
            mInactiveCount++;
        }
    }

    @Override
    public String toString() {
        StringBuilder lhs = new StringBuilder();

        for (int e = 0; e < mLinExpr.size(); e++) {
            try {
                double coef = mLinExpr.getCoeff(e);
                if (coef > 0) {
                    if (e > 0) {
                        lhs.append(" ");
                    }
                    if (coef != 1) {
                        lhs.append(String.format("%s*%s", GRBUtilities.coefToString(coef), mLinExpr
                                .getVar(e).get(StringAttr.VarName)));
                    } else {
                        lhs.append(String.format("%s", mLinExpr.getVar(e).get(StringAttr.VarName)));
                    }
                }
            } catch (GRBException e1) {
                lhs.append("NA");
            }
        }

        return String.format("%s: %s %s %s", getName(), lhs, getSense(), getRHS());
    }

}
