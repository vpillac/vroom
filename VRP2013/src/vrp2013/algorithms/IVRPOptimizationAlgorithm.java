package vrp2013.algorithms;

import java.util.concurrent.Callable;

import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.util.IRoutePool;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.IDisposable;
import vrp2013.util.VRPSolution;

/**
 * The interface <code>VRPOptimizationAlgorithm</code> defines a generic interface for the optimization algorithms used
 * in the VRP 2013 examples
 * <p>
 * Creation date: 06/05/2013 - 2:59:58 PM
 * 
 * @author Victor Pillac, <a href="http://www.nicta.com.au">National ICT Australia</a>, <a
 *         href="http://www.victorpillac.com">www.victorpillac.com</a>
 * @version 1.0
 */
public interface IVRPOptimizationAlgorithm extends Callable<VRPSolution>, IDisposable {

    /**
     * Returns the pool of routes generated during the iterations of the GRASP
     * 
     * @return the pool containing the generated routes
     */
    public abstract IRoutePool<INodeVisit> getRoutePool();

    /**
     * Returns the best solution found by this GRASP instance on the last run
     * 
     * @return the best solution found by this GRASP instance on the last run
     */
    public abstract VRPSolution getBestSolution();

    /**
     * Returns the instance that this algorithm is solving
     * 
     * @return the instance that this algorithm is solving
     */
    public IVRPInstance getInstance();

    /**
     * Returns the number of iterations allowed in this algorithm
     * 
     * @return the number of iterations allowed in this algorithm
     */
    public abstract int getIterations();

    /**
     * Returns the solution factory used to instantiate new solutions and routes
     * 
     * @return the solution factory used to instantiate new solutions and routes
     */
    public ISolutionFactory getSolutionFactory();
}