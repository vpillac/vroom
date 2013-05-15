package vroom.common.heuristics.vls.vrp;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.vls.IVLSPertubation;
import vroom.common.heuristics.vls.IVLSState;
import vroom.common.heuristics.vls.VLSGlobalParameters;
import vroom.common.heuristics.vls.VLSPhase;
import vroom.common.heuristics.vrp.SwapMove;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IRoute;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.common.utilities.optimization.ISolution;
import vroom.common.utilities.params.GlobalParameters;
import vroom.common.utilities.params.ParameterKey;

/**
 * <code>PSwapPerturbation</code> is an implementation of {@link IVLSPertubation} that performs p swaps in a given tsp tour.
 * <p/>
 * The mSolution passe to {@link #pertub(IVLSState, IInstance, ISolution, IParameters)} should be a subtype of {@link VRPSolutionWrapper}
 * <p>
 * Creation date: Apr 26, 2010 - 3:57:36 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class PSwapPerturbation<S extends IVRPSolution<? extends IRoute<?>>> implements IVLSPertubation<S> {

    private final int                         mPMax;
    private static int                        sLoopBound = 100;

    /** The maximum number of swaps to be performed */
    public static final ParameterKey<Integer> PSWAP_PMAX = new ParameterKey<Integer>("PSWAP_PMAX",
                                                             Integer.class, 3);

    static {
        GlobalParameters.addRequiredParameter(VLSGlobalParameters.class, PSWAP_PMAX);
    }

    /**
     * Creates a new <code>PSwapPerturbation</code>
     * 
     * @param params
     *            the global parameters used
     */
    public PSwapPerturbation(VLSGlobalParameters params) {
        this(params, params.get(PSWAP_PMAX));
    }

    /**
     * Creates a new <code>PSwapPerturbation</code> with the given maximum number of swaps
     * 
     * @param params
     *            the global parameters used
     * @param pMax
     *            the maximum number of swaps to be perfomed
     */
    public PSwapPerturbation(VLSGlobalParameters params, int pMax) {
        mPMax = pMax;
    }

    @Override
    public void pertub(IVLSState<S> state, IInstance instance, S solution, IParameters params) {
        // Number of swaps

        RandomStream r = state.getParentVLS().getRandomStream();

        int p = Math.max(state.getNonImprovingIterationCount(VLSPhase.ELS), mPMax);

        int k = 0, l = 0;
        while (k < p && l < sLoopBound) {
            int r1 = r.nextInt(0, solution.getRouteCount() - 1);
            int r2 = r.nextInt(0, solution.getRouteCount() - 1);

            if (solution.getRoute(r1).length() > 1 && solution.getRoute(r2).length() > 1) {
                int i = r.nextInt(0, solution.getRoute(r1).length() - 1);
                int j = r.nextInt(0, solution.getRoute(r2).length() - 1);

                SwapMove move = new SwapMove(0, solution, r1, r2, i, j);

                if (state.getParentVLS().getConstraintHandler().isFeasible(solution, move)
                        && swap(solution.getRoute(r1), solution.getRoute(r2), i, j)) {
                    k++;
                }

            }
            l++;
        }

    }

    @SuppressWarnings("unchecked")
    protected <V extends INodeVisit, W extends INodeVisit> boolean swap(IRoute<V> r1, IRoute<W> r2, int i,
            int j) {

        if (i != j && !r1.getNodeAt(i).isFixed() && !r2.getNodeAt(j).isFixed()) {
            r2.setNodeAt(j, (W) r1.setNodeAt(i, (V) r2.getNodeAt(j)));
            return true;
        } else {
            return false;
        }
    }

}
