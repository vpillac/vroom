/**
 * 
 */
package vroom.common.heuristics.alns;

import vroom.common.utilities.IDistance;
import vroom.common.utilities.params.ClassParameterKey;
import vroom.common.utilities.params.GlobalParameters;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.params.RequiredParameter;

/**
 * The Class <code>ALNSGlobalParameters</code> defines global parameters used in the
 * {@link AdaptiveLargeNeighborhoodSearch}
 * <p>
 * Creation date: May 13, 2011 - 10:36:16 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ALNSGlobalParameters extends GlobalParameters {

    /**
     * The range for the value for the size of
     * {@link IDestroy#destroy(vroom.common.utilities.optimization.ISolution, vroom.common.utilities.optimization.IParameters, int)
     * destroy} (by convention in [0,1]).
     */
    @RequiredParameter
    public static final ParameterKey<double[]>                DESTROY_SIZE_RANGE = new ParameterKey<double[]>(
                                                                                         "DESTROY_SIZE_RANGE",
                                                                                         double[].class,
                                                                                         new double[] {
            0.1, 0.5                                                                    });

    /** The type of {@linkplain IPALNSSolutionPool solution pool} */
    @SuppressWarnings("rawtypes")
    public static final ClassParameterKey<IPALNSSolutionPool> PALNS_POOL         = new ClassParameterKey<IPALNSSolutionPool>(
                                                                                         "PALNS_POOL",
                                                                                         IPALNSSolutionPool.class,
                                                                                         SimpleSolutionPool.class);

    /** The number of iterations to be performed in parallel */
    public static final ParameterKey<Integer>                 PALNS_POOL_SIZE    = new ParameterKey<Integer>(
                                                                                         "PALNS_POOL_SIZE",
                                                                                         Integer.class,
                                                                                         Runtime.getRuntime()
                                                                                                 .availableProcessors());

    /** The number of Threads to use for parallelization */
    public static final ParameterKey<Integer>                 PALNS_THREAD_COUNT = new ParameterKey<Integer>(
                                                                                         "PALNS_THREAD_COUNT",
                                                                                         Integer.class,
                                                                                         Runtime.getRuntime()
                                                                                                 .availableProcessors());

    /** The number of iterations to be performed in parallel */
    public static final ParameterKey<Integer>                 PALNS_IT_P         = new ParameterKey<Integer>(
                                                                                         "PALNS_IT_P",
                                                                                         Integer.class,
                                                                                         100);

    @SuppressWarnings("rawtypes")
    public static final ClassParameterKey<IDistance>          DIVERSITY_METRIC   = new ClassParameterKey<IDistance>(
                                                                                         "DIVERSITY_METRIC",
                                                                                         IDistance.class);

}
