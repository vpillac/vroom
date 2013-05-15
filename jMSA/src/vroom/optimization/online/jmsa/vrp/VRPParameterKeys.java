package vroom.optimization.online.jmsa.vrp;

import vroom.common.modeling.util.ISolutionFactory;
import vroom.common.utilities.params.ClassParameterKey;
import vroom.common.utilities.params.GlobalParameters;
import vroom.common.utilities.params.ParameterKey;
import vroom.common.utilities.params.RequiredParameter;
import vroom.optimization.online.jmsa.IActualRequest;
import vroom.optimization.online.jmsa.MSAGlobalParameters;

/**
 * Creation date: Mar 4, 2010 - 4:27:28 PM<br/>
 * <code>VRPParameterKeys</code>
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VRPParameterKeys {

    /** The class used to represent an actual request */
    public static final ClassParameterKey<IActualRequest>        ACTUAL_REQUEST_CLASS   = new ClassParameterKey<IActualRequest>(
                                                                                                "ACTUAL_REQUEST_CLASS",
                                                                                                IActualRequest.class,
                                                                                                VRPActualRequest.class);

    /** The implementation of {@link ISolutionFactory} that will be used to create new scenarios and routes */
    @RequiredParameter
    public static final ClassParameterKey<MSAVRPSolutionFactory> SCENARIO_FACTORY_CLASS = new ClassParameterKey<MSAVRPSolutionFactory>(
                                                                                                "SOLUTION_FACTORY_CLASS",
                                                                                                MSAVRPSolutionFactory.class,
                                                                                                MSAVRPSolutionFactory.class);

    private static final ClassParameterKey<?>[]                  REQUIRED_PARAMETERS    = new ClassParameterKey<?>[] {
            ACTUAL_REQUEST_CLASS, SCENARIO_FACTORY_CLASS                               };

    /**
     * Add the required parameters for VRP applications to the global MSA parameters
     */
    public static void registerRequiredParameters() {
        for (ParameterKey<?> key : REQUIRED_PARAMETERS) {
            GlobalParameters.addRequiredParameter(MSAGlobalParameters.class, key);
        }
    }

}
