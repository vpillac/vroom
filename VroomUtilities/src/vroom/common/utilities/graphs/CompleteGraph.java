package vroom.common.utilities.graphs;

/**
 * The Class<code>Graph</code> encapsulate a index based representation of a graph
 * <p>
 * Creation date: 14 août 2010 - 18:47:46.
 * 
 * @author Victor Pillac <br/>
 *         <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <br/>
 *         <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class CompleteGraph {

    /** the symmetric flag **/
    private final boolean mSymmetric;

    /**
     * Getter for the symmetric flag
     * 
     * @return <code>true</code> if this graph is symmetric
     */
    public boolean isSymmetric() {
        return mSymmetric;
    }

    /** the cost matrix **/
    private final double[][] mCostMatrix;

    /**
     * Getter for the cost of an arc
     * 
     * @param i
     * @param j
     * @return the cost of arc (i,j)
     */
    public double getArcCost(int i, int j) {
        int[] a = getSymArc(i, j);
        return mCostMatrix[a[0]][a[1]];
    }

    /**
     * Setter for the cost of an arc
     * 
     * @param i
     * @param j
     * @param cost
     *            the cost of arc (i,j)
     */
    public void setArcCost(int i, int j, double cost) {
        int[] a = getSymArc(i, j);
        mCostMatrix[a[0]][a[1]] = cost;
    }

    /** the capacity matrix **/
    private final double[][] mCapacityMatrix;

    /**
     * Getter for the capacity of an arc
     * 
     * @param i
     * @param j
     * @return the capacity of arc (i,j)
     */
    public double getArcCapacity(int i, int j) {
        int[] a = getSymArc(i, j);
        return mCapacityMatrix[a[0]][a[1]];
    }

    /**
     * Setter for the capacity of an arc
     * 
     * @param i
     * @param j
     * @param capacity
     *            the capacity of arc (i,j)
     */
    public void setArcCapacity(int i, int j, double capacity) {
        int[] a = getSymArc(i, j);
        mCapacityMatrix[a[0]][a[1]] = capacity;
    }

    /**
     * Factory method for the (i,j) arc
     * 
     * @param i
     * @param j
     * @return the (i,j) arc
     */
    public Arc getArc(int i, int j) {
        int[] a = getSymArc(i, j);
        return new Arc(getArcID(i, j), a[0], a[1], getArcCost(i, j), getArcCapacity(i, j));
    }

    /**
     * Returns an unique id for the given arc
     * 
     * @param i
     * @param j
     * @return the unique id associated with arc (i,j)
     */
    public int getArcID(int i, int j) {
        int[] a = getSymArc(i, j);
        if (isSymmetric()) {
            return a[0] * (getNodeCount() - a[0]) + a[1];
        } else {
            return a[0] * (getNodeCount() - 1) + a[1];
        }
    }

    /**
     * Getter for the number of vertices of this graph
     * 
     * @return the node count
     */
    public int getNodeCount() {
        return mCostMatrix.length;
    }

    /**
     * Getter for the number of edges in this graph
     * 
     * @return the arc count
     */
    public int getArcCount() {
        return getNodeCount() * (getNodeCount() - 1);
    }

    public int[] getSymArc(int i, int j) {
        if (!isSymmetric() || i < j) {
            return new int[] { i, j };
        } else {
            return new int[] { j, i };
        }
    }

    /**
     * Creates a new <code>CompleteGraph</code>
     * 
     * @param symmetric
     *            <code>true</code> if the cost matrix is symmetric
     * @param costMatrix
     *            the cost matrix
     * @param capacityMatrix
     *            the capacity matrix
     */
    public CompleteGraph(boolean symmetric, double[][] costMatrix, double[][] capacityMatrix) {
        super();
        mSymmetric = symmetric;
        mCostMatrix = costMatrix;
        mCapacityMatrix = capacityMatrix;
    }

    /**
     * Creates a new <code>CompleteGraph</code>
     * 
     * @param symmetric
     *            <code>true</code> if the cost matrix is symmetric
     * @param size
     *            the size of the graph (node count)
     */
    public CompleteGraph(boolean symmetric, int size) {
        super();
        mSymmetric = symmetric;
        mCostMatrix = new double[size][size];
        mCapacityMatrix = new double[size][size];
    }

    @Override
    public String toString() {
        return String.format("size:%s sym:%s", getNodeCount(), isSymmetric());
    }
}
