package vroom.common.heuristics.vls;

import vroom.common.heuristics.IInitialization;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ILocalSearch;
import vroom.common.utilities.optimization.SimpleParameters;
import vroom.common.utilities.params.ClassParameterKey;
import vroom.common.utilities.params.GlobalParameters;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.params.RequiredParameter;

/**
 * <code>VLSGlobalParameters</code> is an specialization of {@link GlobalParameters} for the VLS procedure. It includes
 * definition of all the parameters required by the VLS procedure.
 * <p>
 * Creation date: Apr 26, 2010 - 10:31:05 AM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class VLSGlobalParameters extends GlobalParameters {

    /** The direction of optimization: -1 (default) for minimization, +1 for maximization */
    @RequiredParameter
    public static final ParameterKey<Integer>                      OPTIMIZATION_DIRECTION    = new ParameterKey<Integer>(
                                                                                                     "OPTIMIZATION_DIRECTION",
                                                                                                     Integer.class, -1);

    /** The random seed to be used in the procedure */
    @RequiredParameter
    public static final ParameterKey<Long>                         RANDOM_SEED               = new ParameterKey<Long>(
                                                                                                     "RANDOM_SEED",
                                                                                                     Long.class, 0l);

    /** The number of iteration of the GRASP loop */
    @RequiredParameter
    public static final ParameterKey<Integer>                      NS                        = new ParameterKey<Integer>(
                                                                                                     "NS",
                                                                                                     Integer.class, 0);

    /** The number of iteration of the ILS loop */
    @RequiredParameter
    public static final ParameterKey<Integer>                      NI                        = new ParameterKey<Integer>(
                                                                                                     "NI",
                                                                                                     Integer.class, 0);

    /** The number of iteration of the ELS loop */
    @RequiredParameter
    public static final ParameterKey<Integer>                      NC                        = new ParameterKey<Integer>(
                                                                                                     "NC",
                                                                                                     Integer.class, 0);

    /** The maximum overall time for the VLS procedure (in ms) */
    @RequiredParameter
    public static final ParameterKey<Long>                         VLS_MAX_TIME              = new ParameterKey<Long>(
                                                                                                     "VLS_MAX_TIME",
                                                                                                     Long.class,
                                                                                                     Long.MAX_VALUE);

    /** The vls state class */
    @RequiredParameter
    public static final ClassParameterKey<IVLSState>               STATE_CLASS               = new ClassParameterKey<IVLSState>(
                                                                                                     "STATE_CLASS",
                                                                                                     IVLSState.class,
                                                                                                     VLSStateBase.class);

    /** The acceptance criteria class */
    @RequiredParameter
    public static final ClassParameterKey<IVLSAcceptanceCriterion> ACCEPTANCE_CRITERIA_CLASS = new ClassParameterKey<IVLSAcceptanceCriterion>(
                                                                                                     "ACCEPTANCE_CRITERIA_CLASS",
                                                                                                     IVLSAcceptanceCriterion.class,
                                                                                                     SimpleAcceptanceCriterion.class);

    /** The initialization component class */
    public static final ClassParameterKey<IInitialization>         INITIALIZATION_CLASS      = new ClassParameterKey<IInitialization>(
                                                                                                     "INITIALIZATION_CLASS",
                                                                                                     IInitialization.class);

    /** The local search component class */
    @RequiredParameter
    public static final ClassParameterKey<ILocalSearch>            LOCAL_SEARCH_CLASS        = new ClassParameterKey<ILocalSearch>(
                                                                                                     "LOCAL_SEARCH_CLASS",
                                                                                                     ILocalSearch.class);

    /** The local search component class */
    @RequiredParameter
    public static final ClassParameterKey<IVLSPertubation>         PERTUBATION_CLASS         = new ClassParameterKey<IVLSPertubation>(
                                                                                                     "PERTUBATION_CLASS",
                                                                                                     IVLSPertubation.class);

    /** The parameters that will be passed to the initialization component */
    public static final ParameterKey<IParameters>                PARAM_INIT                = new ParameterKey<IParameters>(
                                                                                                     "PARAM_INIT",
                                                                                                     IParameters.class,
                                                                                                     SimpleParameters.BEST_IMPROVEMENT);

    /** The parameters that will be passed to the perturbation component */
    public static final ParameterKey<IParameters>                PARAM_PERTUBATION         = new ParameterKey<IParameters>(
                                                                                                     "PARAM_PERTUBATION",
                                                                                                     IParameters.class,
                                                                                                     SimpleParameters.PERTURBATION);

    /** The parameters that will be passed to the local search component */
    public static final ParameterKey<IParameters>                PARAM_LOCALSEARCH         = new ParameterKey<IParameters>(
                                                                                                     "PARAM_LOCALSEARCH",
                                                                                                     IParameters.class,
                                                                                                     SimpleParameters.BEST_IMPROVEMENT);

    /** Defines whether or not callbacks should be enabled (default is <code>false</code>) */
    public static final ParameterKey<Boolean>                      ENABLE_CALLBACKS          = new ParameterKey<Boolean>(
                                                                                                     "ENABLE_CALLBACKS",
                                                                                                     Boolean.class,
                                                                                                     Boolean.FALSE);

    static {
        addRequiredKeysByReflection(VLSGlobalParameters.class);
    }
}
