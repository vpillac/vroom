package vroom.common.utilities.optimization;

/**
 * <code>OptimizationSense</code> defines the optimization sense
 * <p>
 * Creation date: 11 juil. 2010 - 19:55:20
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public enum OptimizationSense {

    MAXIMIZATION(1), MINIMIZATION(-1);

    private final byte sense;

    private OptimizationSense(int sense) {
        this.sense = (byte) sense;
    }

    /**
     * Return a byte representing this optimization sense: -1 for minimization, or 1 for maximization
     * 
     * @return -1 for minimization, or 1 for maximization
     */
    public byte toByte() {
        return sense;
    }

    /**
     * Compare two objective function relative to this optimization sense.
     * <p>
     * Return <code>true</code> if the second objective value is better than the first
     * </p>
     * 
     * @param obj1
     *            the first objective value
     * @param obj2
     *            the second objective value
     * @param acceptTie
     *            <code>true</code> if the method should return <code>true</code> if <code>obj1==obj2</code>
     * @return <code>true</code> if <code>obj2</code> is better than <code>obj1</code>
     */
    public boolean isBetter(double obj1, double obj2, boolean acceptTie) {
        return isImproving(this, obj1, obj2, acceptTie);
    }

    /**
     * Compare two objective function values given an optimization sense.
     * <p>
     * Return <code>true</code> if the {@code  obj2} is better than {@code  obj1}
     * </p>
     * 
     * @param sense
     *            the optimization sense
     * @param obj1
     *            the first objective value
     * @param obj2
     *            the second objective value
     * @param acceptTie
     *            <code>true</code> if the method should return <code>true</code> if <code>obj1==obj2</code>
     * @return <code>true</code> if <code>obj2</code> is better than <code>obj1</code>
     */
    public static boolean isImproving(OptimizationSense sense, double obj1, double obj2,
            boolean acceptTie) {
        int compare = Double.compare(obj1, obj2);
        if (compare == 0)
            return acceptTie;
        else
            return compare * sense.toByte() < 0;
    }

    /**
     * Compare two objective function values relative to this optimization sense.
     * <p>
     * Return the improvement {@code  (obj2-obj1)*sense} of the second objective value relative to the first
     * </p>
     * 
     * @param obj1
     *            the first objective value
     * @param obj2
     *            the second objective value
     * @return the improvement between <code>obj2</code> and <code>obj1</code>
     */
    public double getImprovement(double obj1, double obj2) {
        return getImprovement(this, obj1, obj2);
    }

    /**
     * Compare two objective function values given an optimization sense.
     * <p>
     * Return the improvement {@code  (obj2-obj1)*sense} of the second objective value relative to the first
     * </p>
     * 
     * @param sense
     *            the optimization sense
     * @param obj1
     *            the first objective value
     * @param obj2
     *            the second objective value
     * @return the improvement between <code>obj2</code> and <code>obj1</code>
     */
    public static double getImprovement(OptimizationSense sense, double obj1, double obj2) {
        if (Double.compare(obj1, obj2) == 0)
            return 0;
        else
            return (obj2 - obj1) * sense.toByte();
    }

    /**
     * Compare two objective values depending on this optimization sense
     * 
     * @param obj1
     * @param obj2
     * @return the value {@code 0} if {@code obj1} is numerically equal to {@code obj2}; a value less than {@code 0} if
     *         {@code obj1} is worst than {@code obj2}; and a value greater than {@code 0} if {@code obj1} is better
     *         than {@code obj2}.
     */
    public int compare(double obj1, double obj2) {
        return sense * Double.compare(obj1, obj2);
    }

    /**
     * Compare two solutions depending on this optimization sense
     * 
     * @param sol1
     * @param sol2
     * @return the value {@code 0} if {@code sol1} is numerically equal to {@code sol2}; a value less than {@code 0} if
     *         {@code sol1} is worst than {@code sol2}; and a value greater than {@code 0} if {@code sol1} is better
     *         than {@code sol2}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compare(ISolution sol1, ISolution sol2) {
        return sense * ((Comparable) sol1.getObjective()).compareTo(sol2.getObjective());
    }

    /**
     * Returns {@code true} iif {@code  sol2} is strictly better than {@code  sol1}
     * 
     * @param sol1
     * @param sol2
     * @return {@code true} iif {@code  sol2} is strictly better than {@code  sol1}
     */
    public boolean isBetter(ISolution sol1, ISolution sol2) {
        return compare(sol1, sol2) < 0;
    }
}
