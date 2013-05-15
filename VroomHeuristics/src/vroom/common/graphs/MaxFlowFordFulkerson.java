package vroom.common.graphs;

import java.util.Iterator;

import vroom.common.utilities.graphs.CompleteGraph;
import vroom.common.utilities.graphs.Cut;
import vroom.common.utilities.graphs.Path;

/**
 * The Class<code>MaxFlowFordFukerson</code> is an implementation of the Ford Fulkerson max flow / min cut algorithm
 * <p>
 * Creation date: 14 août 2010 - 20:19:18.
 * 
 * @author Victor Pillac <br/>
 *         <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <br/>
 *         <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class MaxFlowFordFulkerson {

    public static double        ZERO_TOLERANCE = 1e-10;

    /** The m flow. */
    private final double[][]    mFlow;

    /** The m residual. */
    private final double[][]    mResidual;

    /** The m graph. */
    private final CompleteGraph mGraph;

    /** The m current cut. */
    private Cut                 mCurrentCut;

    /** The m current max flow. */
    private double              mCurrentMaxFlow;

    /**
     * Creates a new <code>MaxFlowFordFulkerson</code>. algorithm
     * 
     * @param graph
     *            the graph on which calculation will be made
     */
    public MaxFlowFordFulkerson(CompleteGraph graph) {
        super();
        mGraph = graph;
        mFlow = new double[mGraph.getNodeCount()][mGraph.getNodeCount()];
        mResidual = new double[mGraph.getNodeCount()][mGraph.getNodeCount()];
        for (int i = 0; i < mGraph.getNodeCount(); i++) {
            for (int j = 0; j < mGraph.getNodeCount(); j++) {
                mResidual[i][j] = mGraph.getArcCapacity(i, j);
            }
        }
    }

    /**
     * Min cut algorithm.
     * 
     * @param s
     *            the source node
     * @param t
     *            the sink node
     * @return the min cut between <code>s</code> and <code>t</code>
     */
    public Cut minCut(int s, int t) {

        mCurrentMaxFlow = 0;
        mCurrentCut = new Cut(mGraph);

        Path improvingPath = improvingPath(s, t);

        while (improvingPath != null) {

            // System.out.println("Improving Path: "+improvingPath);

            mCurrentCut = new Cut(mGraph);
            // Update current max flow
            mCurrentMaxFlow += improvingPath.getFlow();

            Iterator<Integer> it = improvingPath.iterator();
            int prev = it.next();
            int next;
            while (it.hasNext()) {
                next = it.next();
                // Update current flow and residual
                mFlow[prev][next] += improvingPath.getFlow();
                mFlow[next][prev] -= improvingPath.getFlow();
                mResidual[prev][next] -= improvingPath.getFlow();
                mResidual[next][prev] += improvingPath.getFlow();
                prev = next;
            }

            improvingPath = improvingPath(s, t);
        }

        return mCurrentCut;
    }

    /**
     * Max flow algorithm
     * 
     * @param s
     *            the source node
     * @param t
     *            the sink node
     * @return the max flow between the <code>s</code> and <code>t</code>
     */
    public double maxFlow(int s, int t) {
        minCut(s, t);
        return mCurrentMaxFlow;
    }

    /**
     * Gets the current flow.
     * 
     * @param i
     * @param j
     * @return the current flow for arc (i,j)
     */
    public double getCurrentFlow(int i, int j) {
        return mFlow[i][j];
    }

    /**
     * Gets the max flow from the last call to {@link #maxFlow(int, int)} or {@link #minCut(int, int)}
     * 
     * @return the previously calculated max flow
     */
    public double getCurrentMaxFlow() {
        return mCurrentMaxFlow;
    }

    /**
     * Gets the min cut from the last call to {@link #maxFlow(int, int)} or {@link #minCut(int, int)}
     * 
     * @return the previously calculated min cut
     */
    public Cut getCurrentMinCut() {
        return mCurrentCut;
    }

    /**
     * Gets the current residual value.
     * 
     * @param i
     * @param j
     * @return the current residual for arc (i,j)
     */
    public double getCurrentResidual(int i, int j) {
        return mResidual[i][j];
    }

    /**
     * Improving path.
     * 
     * @param s
     *            the s
     * @param t
     *            the t
     * @return the path
     */
    public Path improvingPath(int s, int t) {
        mCurrentCut = new Cut(mGraph);
        Path path = new Path(mGraph);
        path.append(s);
        mCurrentCut.add(s);

        path.setFlow(Double.POSITIVE_INFINITY);
        double flow = improvingPathRec(path, t);

        if (flow > 0) {
            return path;
        } else {
            return null;
        }
    }

    /**
     * Improving path rec.
     * 
     * @param s
     *            the s
     * @param t
     *            the t
     * @param path
     *            the path
     * @return the double
     */
    protected double improvingPathRec(Path path, int t) {
        // Reached the sink node
        if (path.getLast() == t) {
            return path.getFlow();
        }

        // System.out.println("Cut  "+mCurrentCut);
        // System.out.println("Comp "+mCurrentCut.getComplement());
        for (int j : mCurrentCut.getComplement()) {
            // System.out.printf("(%s,%s)=%s\n",path.getLast(),j,mResidual[path.getLast()][j]);
            if (mResidual[path.getLast()][j] > ZERO_TOLERANCE) {
                mCurrentCut.add(j);
                double maxFlowBck = path.getFlow();

                // Append the node j and update the flow
                if (mResidual[path.getLast()][j] < path.getFlow()) {
                    path.setFlow(mResidual[path.getLast()][j]);
                }
                path.append(j);

                // Recursively find and improving path
                double maxFlow = improvingPathRec(path, t);
                if (maxFlow > 0) {
                    // Return the value of the improving path
                    return maxFlow;
                } else {
                    // Reset the path
                    path.pop();
                    path.setFlow(maxFlowBck);
                }
            }
        }

        return 0;
    }

    /**
     * Reset the algorithm data structures.
     */
    public void reset() {
        for (int i = 0; i < mFlow.length; i++) {
            for (int j = 0; j < mFlow.length; j++) {
                mFlow[i][j] = 0;
                mResidual[i][j] = mGraph.getArcCapacity(i, j);
                mCurrentCut = null;
                mCurrentMaxFlow = 0;
            }
        }
    }
}
