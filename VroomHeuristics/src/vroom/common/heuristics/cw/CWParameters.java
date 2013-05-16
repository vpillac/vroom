/*
 * jCW : a java library for the development of saving based heuristics
 */
package vroom.common.heuristics.cw;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.cw.kernel.ISavingsAlgorithm;
import vroom.common.modeling.util.DefaultSolutionFactory;
import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.optimization.IConstraint;
import vroom.common.utilities.params.ClassParameterKey;
import vroom.common.utilities.params.GlobalParameters;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.params.ParameterKey.DoubleParameterKey;
import vroom.common.utilities.params.ParametersFilePersistenceDelegate;
import vroom.common.utilities.params.RequiredParameter;

/**
 * <code>CWGlobalParameters</code> is the class responsible for the handling of the various parameters used in the
 * {@link ClarkeAndWrightHeuristic} procedure
 * <p>
 * Creation date: Apr 15, 2010 - 3:48:24 PM.
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class CWParameters extends GlobalParameters {

    // TODO check the deprecated parameters

    /** The Constant CONSTRAINT_PREFIX. */
    public static final String                               CONSTRAINT_PREFIX      = "CONSTRAINT_";

    /** The implementation of {@link ISolutionFactory} that will be used to create new solutions and routes */
    @RequiredParameter
    public static final ClassParameterKey<ISolutionFactory>  SOLUTION_FACTORY_CLASS = new ClassParameterKey<ISolutionFactory>(
                                                                                            "SOLUTION_FACTORY_CLASS",
                                                                                            ISolutionFactory.class,
                                                                                            DefaultSolutionFactory.class);

    /** The implementation of {@link ISavingsAlgorithm} used in this execution. */
    @RequiredParameter
    public static final ClassParameterKey<ISavingsAlgorithm> ALGORITHM_CLASS        = new ClassParameterKey<ISavingsAlgorithm>(
                                                                                            "SAVINGS_HEURISTIC",
                                                                                            ISavingsAlgorithm.class);

    /** The implementation of {@link ConstraintHandler} used in this execution. */
    public static final ClassParameterKey<ConstraintHandler> CTR_HANDLER_CLASS      = new ClassParameterKey<ConstraintHandler>(
                                                                                            "CTR_HANDLER",
                                                                                            ConstraintHandler.class,
                                                                                            ConstraintHandler.class);

    /** The file containing the problem data. */
    public static final ParameterKey<File>                   PROBLEM_DATA_FILE      = new ParameterKey<File>(
                                                                                            "PROBLEM_DATA_FILE",
                                                                                            File.class);

    /** The number of constraints for this problem. */
    @RequiredParameter
    public static final ParameterKey<Integer>                NUMBER_OF_CONSTRAINTS  = new ParameterKey<Integer>(
                                                                                            "NUMBER_OF_CONSTRAINTS",
                                                                                            Integer.class);

    /**
     * The random seed.
     * <p>
     * If {@link #RANDOM_SEEDS} is defined it will be used instead
     * </p>
     * 
     * @see #RANDOM_SEEDS
     */
    @RequiredParameter
    public static final ParameterKey<Long>                   RANDOM_SEED            = new ParameterKey<Long>(
                                                                                            "RANDOM_SEED",
                                                                                            Long.class);

    /** The minimum fraction of the savings list to shuffle */
    public static final DoubleParameterKey                   RND_MIN_FRACTION       = new DoubleParameterKey(
                                                                                            "RND_MIN_FRACTION",
                                                                                            0.2d);
    /** The maximum fraction of the savings list to shuffle */
    public static final DoubleParameterKey                   RND_MAX_FRACTION       = new DoubleParameterKey(
                                                                                            "RND_MAX_FRACTION",
                                                                                            0.5d);

    static {
        setPersistenceDelegate(new CWPersistenceDelegate());

        addRequiredKeysByReflection(CWParameters.class);
    }

    /** The trace level. */
    public static final ParameterKey<Integer>                TRACE_LEVEL            = new ParameterKey<Integer>(
                                                                                            "TRACELEVEL",
                                                                                            Integer.class,
                                                                                            0);

    /* (non-Javadoc)
     * @see vroom.common.utilities.params.GlobalParameters#setParameterNoCheck(vroom.common.utilities.params.ParameterKey, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object setNoCheck(ParameterKey<?> key, Object value) {
        if (key instanceof ClassParameterKey<?> && key.getName().startsWith(CONSTRAINT_PREFIX)) {
            mConstraintKeys.add((ClassParameterKey<IConstraint>) key);
        }

        return super.setNoCheck(key, value);
    }

    /** A set of constraint parameters keys. */
    private final Set<ClassParameterKey<IConstraint>> mConstraintKeys;

    /**
     * Set of constraint parameters keys.
     * 
     * @return a list containing the constraint parameters keys
     */
    public Set<ClassParameterKey<IConstraint>> getConstraintKeys() {
        return new HashSet<ClassParameterKey<IConstraint>>(mConstraintKeys);
    }

    /**
     * Instantiates a new cW global parameters.
     */
    public CWParameters() {
        super();

        mConstraintKeys = new HashSet<ClassParameterKey<IConstraint>>();

        registerKey(TRACE_LEVEL);
        registerKey(PROBLEM_DATA_FILE);
    }

    /* (non-Javadoc)
     * @see vroom.common.utilities.params.GlobalParameters#clear()
     */
    @Override
    public void clear() {
        super.clear();
        mConstraintKeys.clear();
    }

    /**
     * The Class CWPersistenceDelegate.
     */
    private static class CWPersistenceDelegate extends ParametersFilePersistenceDelegate {

        /* (non-Javadoc)
         * @see vroom.common.utilities.params.ParametersFilePersistenceDelegate#setUnregisteredParameter(vroom.common.utilities.params.GlobalParameters, java.lang.String, java.lang.String)
         */
        @Override
        protected void setUnregisteredParameter(GlobalParameters params, String key, String value)
                throws Exception {
            if (key.startsWith(CONSTRAINT_PREFIX)) {
                ClassParameterKey<IConstraint> pKey = new ClassParameterKey<IConstraint>(key,
                        IConstraint.class);
                ((CWParameters) params).mConstraintKeys.add(pKey);
                super.setRegisteredParameter(params, pKey, value);
            } else {
                super.setUnregisteredParameter(params, key, value);
            }
        }

    }
}
