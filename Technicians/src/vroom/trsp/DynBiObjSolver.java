/**
 * 
 */
package vroom.trsp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import vroom.common.heuristics.alns.ParallelALNS;
import vroom.common.heuristics.alns.SimpleSolutionPool;
import vroom.common.utilities.BatchThreadPoolExecutor;
import vroom.common.utilities.optimization.IPathRelinking;
import vroom.common.utilities.optimization.OptimizationSense;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPLevenshteinDistance;
import vroom.trsp.optimization.biobj.ALNSParetoFront;
import vroom.trsp.optimization.biobj.HierarchicalParetoSelector;
import vroom.trsp.optimization.biobj.IParetoSelector;
import vroom.trsp.optimization.biobj.LevenshteinPR;
import vroom.trsp.optimization.biobj.ParetoFront;
import vroom.trsp.optimization.biobj.ParetoFront.ParetoSolution;
import vroom.trsp.optimization.constraints.TourConstraintHandler;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * The class <code>DynBiObjSolver</code> is an extension of {@link ALNSSCSolver} specialized for dynamic and
 * bi-objective problems
 * <p>
 * Creation date: Dec 13, 2011 - 1:35:04 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DynBiObjSolver extends ALNSSCSolver {
    // FIXME Try to use the initial solution as reference solution instead of the solution from previous decision

    /** the pareto selector that will select a unique solution from the pareto front */
    private final IParetoSelector              mSelector;

    /** the path relinking component */
    private final IPathRelinking<TRSPSolution> mPathRelinking;

    /** the path relinking pareto front */
    private ParetoFront                        mPRPareto;

    /** the reference solution for the biobjective approach */
    private TRSPSolution                       mReferenceSol;

    /** enable or disable the bi-objective behavior, independently of the global parameter **/
    private boolean                            mBiObjective;

    /**
     * Getter for enable or disable the bi-objective behavior, independently of the global parameter
     * 
     * @return {@code true} if the next call to {@link #call()} will be to solve a bi-objective problem, {@code false}
     *         otherwise
     */
    public boolean isBiObjective() {
        return this.mBiObjective;
    }

    /**
     * Setter for enable or disable the bi-objective behavior, independently of the global parameter
     * 
     * @param biobj
     *            {@code true} if the next call to {@link #call()} will be to solve a bi-objective problem,
     *            {@code false} otherwise
     */
    public void setBiObjective(boolean biobj) {
        this.mBiObjective = biobj;
    }

    /**
     * Returns the solution used as reference to evaluate the route consistency
     * 
     * @return the solution used as reference to evaluate the route consistency
     */
    public TRSPSolution getReferenceSol() {
        return mReferenceSol;
    }

    /**
     * Sets the solution used as reference to evaluate the route consistency
     * 
     * @param referenceSol
     *            the solution used as reference to evaluate the route consistency
     */
    public void setReferenceSol(TRSPSolution referenceSol) {
        mReferenceSol = referenceSol;
    }

    @Override
    public ParallelALNS<TRSPSolution> getALNS() {
        return (ParallelALNS<TRSPSolution>) super.getALNS();
    }

    /**
     * Returns the path relinking component
     * 
     * @return the path relinking component
     */
    public IPathRelinking<TRSPSolution> getPathRelinking() {
        return mPathRelinking;
    }

    /**
     * Returns the Pareto front resulting from the last optimization
     * 
     * @return the Pareto front resulting from the last optimization
     */
    public ParetoFront getALNSPareto() {
        return (ParetoFront) getALNS().getSolPool();
    }

    /**
     * Returns the Pareto front found by path relinking
     * 
     * @return the Pareto front found by path relinking
     */
    public ParetoFront getPRPareto() {
        return mPRPareto;
    }

    /**
     * Creates a new <code>DynBiObjSolver</code>
     * 
     * @param instance
     * @param params
     */
    public DynBiObjSolver(TRSPInstance instance, TRSPGlobalParameters params) {
        super(instance, params);
        if (params.get(TRSPGlobalParameters.SC_ENABLED))
            throw new IllegalArgumentException("SC_ENABLED set to true");
        mSelector = new HierarchicalParetoSelector(
                params.get(TRSPGlobalParameters.BIOBJ_ALLOWED_DEG));
        mPathRelinking = new LevenshteinPR(params);
        // if (!ParallelALNS.class.isAssignableFrom(params.get(TRSPGlobalParameters.ALNS_VARIANT)))
        // throw new IllegalArgumentException("Unsupported value for ALNS_VARIANT");
    }

    @Override
    public TRSPSolution call() {
        super.call();
        if (getParams().get(TRSPGlobalParameters.BIOBJ_ENABLE_PR)) {
            pr();
            setFinalSolution(mSelector.selectSolution(getPRPareto()));
        }

        return getFinalSolution();
    }

    @Override
    public void setupALNS() {
        super.setupALNS();
        // Set the solution pool
        if (isBiObjective())
            getALNS().setSolPool(
                    new ALNSParetoFront(getALNSCostDelegate(getReferenceSol()), getALNS()
                            .getOptimizationSense(),
                            new TRSPLevenshteinDistance(getReferenceSol()),
                            OptimizationSense.MINIMIZATION));
        else
            getALNS().setSolPool(
                    new SimpleSolutionPool<TRSPSolution>(OptimizationSense.MINIMIZATION,
                            getALNSGlobalParams()));
    }

    @Override
    public void alns() {
        super.alns();
        if (isBiObjective()) {
            ParetoSolution sol = mSelector.selectParetoSolution(getALNSPareto());
            TRSPLogging.getRunLogger().debug(
                    "DynBiObjSolver[alns  ]: Selected solution %s from Pareto %s", sol,
                    getALNSPareto());
            setALNSSol(sol != null ? sol.getSolution() : null);
            setFinalSolution(getALNSSol());
        }
    }

    /**
     * Performs a path relinking between all the non-dominated solutions found by the ALNS
     */
    public void pr() {
        mPRPareto = getALNSPareto().clone();
        int numThreads = getParams().getThreadCount();
        BatchThreadPoolExecutor executor = new BatchThreadPoolExecutor(numThreads, "pr");
        // Do a PR between all Pareto solutions
        List<TRSPSolution> solutions = getALNSPareto().getSolutions();
        int solPerThread = solutions.size() / numThreads;
        ListIterator<TRSPSolution> it = solutions.listIterator();
        for (int t = 0; t < numThreads; t++) {
            ArrayList<TRSPSolution> sourceSol = new ArrayList<TRSPSolution>(solPerThread);
            while (it.hasNext() && sourceSol.size() < solPerThread)
                sourceSol.add(it.next());
            executor.execute(new PRSubprocess(getPRPareto(), sourceSol, solutions,
                    getPathRelinking(), getTourCtrHandler()));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            TRSPLogging.getBaseLogger().exception("DynBiObjSolver.pr", e);
        }
    }

    /**
     * The class <code>PRSubprocess</code> is an implementation of {@link Runnable} that performs a path relinking
     * between two sets of collections
     * <p>
     * Creation date: Dec 14, 2011 - 11:38:09 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class PRSubprocess implements Runnable {

        private final ParetoFront                  mPareto;

        private final List<TRSPSolution>           mSourceSols;
        private final List<TRSPSolution>           mTargetSols;

        private final IPathRelinking<TRSPSolution> mPR;

        private final TourConstraintHandler        mCtr;

        /**
         * Creates a new <code>PRSubprocess</code>
         * 
         * @param pareto
         *            the {@link ParetoFront} in which feasible non-dominated solutions will be stored
         * @param sourceSols
         *            the source solutions
         * @param targetSols
         *            the target solutions
         * @param pR
         *            the path relinking component
         * @param ctr
         *            the constraint handler
         */
        public PRSubprocess(ParetoFront pareto, List<TRSPSolution> sourceSols,
                List<TRSPSolution> targetSols, IPathRelinking<TRSPSolution> pR,
                TourConstraintHandler ctr) {
            mPareto = pareto;
            mSourceSols = sourceSols;
            mTargetSols = targetSols;
            mPR = pR;
            mCtr = ctr;
        }

        @Override
        public void run() {
            for (TRSPSolution source : mSourceSols) {
                for (TRSPSolution target : mTargetSols) {
                    LinkedList<TRSPSolution> feasSol = new LinkedList<TRSPSolution>();
                    if (source != target) {
                        List<TRSPSolution> sols = mPR.pathRelinking(source, target, null);
                        for (TRSPSolution s : sols)
                            if (mCtr.isFeasible(s, false))
                                feasSol.add(s);
                        mPareto.acquireLock();
                        for (TRSPSolution s : feasSol)
                            mPareto.add(s, false);
                        mPareto.releaseLock();
                    }
                }
            }

        }

    }

    /**
     * The class <code>PRJob</code> is an implementation of {@link Callable} that performs a path relinking between two
     * solutions and return the feasible solutions found.
     * <p>
     * Creation date: Dec 14, 2011 - 10:42:36 AM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    public static class PRJob implements Callable<Collection<TRSPSolution>> {

        private final TRSPSolution                 mRef;
        private final TRSPSolution                 mTarget;

        private final IPathRelinking<TRSPSolution> mPR;
        private final TourConstraintHandler        mCtr;

        /**
         * Creates a new <code>PRJob</code>
         * 
         * @param ref
         * @param target
         * @param pR
         * @param ctr
         */
        public PRJob(TRSPSolution ref, TRSPSolution target, IPathRelinking<TRSPSolution> pR,
                TourConstraintHandler ctr) {
            mRef = ref;
            mTarget = target;
            mPR = pR;
            mCtr = ctr;
        }

        @Override
        public Collection<TRSPSolution> call() throws Exception {
            List<TRSPSolution> sols = this.mPR.pathRelinking(mRef, mTarget, null);
            ArrayList<TRSPSolution> feasSol = new ArrayList<TRSPSolution>(sols.size());
            for (TRSPSolution s : sols) {
                if (mCtr.isFeasible(s, false))
                    feasSol.add(s);
            }
            return feasSol;
        }

    }
}
