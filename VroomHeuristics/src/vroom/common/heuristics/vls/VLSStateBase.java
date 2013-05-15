package vroom.common.heuristics.vls;

import vroom.common.utilities.optimization.ISolution;

/**
 * The Class VLSStateBase.
 * 
 * @author Victor Pillac
 * @version 1.0
 * @created 26-Abr-2010 10:11:56 a.m.
 */
public class VLSStateBase<S extends ISolution> implements IVLSState<S> {

    /** The best mSolution for the ELS loop */
    private S                             mELSBestSolution;

    /** The best mSolution for the ILS loop */
    private S                             mILSBestSolution;

    /** The overall best mSolution. */
    private S                             mOverallBestSolution;

    /** The current phase. */
    private VLSPhase                      mCurrentPhase;

    /** The parent versatile local search */
    private final VersatileLocalSearch<S> mParentVLS;

    /** The number of GRASP iterations without improvement */
    private int                           mNonImprovingGRASP;

    /** The number of ILS iterations without improvement */
    private int                           mNonImprovingILS;

    /** The number of ELS iterations without improvement */
    private int                           mNonImprovingELS;

    /**
     * Instantiates a new vLS state base.
     */
    public VLSStateBase(VersatileLocalSearch<S> parentVLS) {
        mParentVLS = parentVLS;

        mNonImprovingELS = 0;
        mNonImprovingGRASP = 0;
        mNonImprovingILS = 0;
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.vls.IVLSState#getCurrentOverallBestSolution()
     */
    @Override
    public S getOverallBestSolution() {
        return mOverallBestSolution;
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.vls.IVLSState#solutionAccepted(edu.uniandes.copa.vls.S, edu.uniandes.copa.vls.VLSPhase)
     */
    @Override
    public void solutionAccepted(S solution, VLSPhase phase) {
        switch (phase) {
        case GRASP:
            mOverallBestSolution = solution;
            mNonImprovingGRASP = 0;
            break;
        case ILS:
            mILSBestSolution = solution;
            mNonImprovingILS = 0;
            break;
        case ELS:
            mELSBestSolution = solution;
            mNonImprovingELS = 0;
            break;
        default:
            throw new IllegalArgumentException("Unsupported phase: " + phase);
        }
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.vls.IVLSState#solutionRejected(edu.uniandes.copa.vls.S, edu.uniandes.copa.vls.VLSPhase)
     */
    @Override
    public void solutionRejected(S solution, VLSPhase phase) {
        switch (phase) {
        case GRASP:
            mNonImprovingGRASP++;
            break;
        case ILS:
            mNonImprovingILS++;
            break;
        case ELS:
            mNonImprovingELS++;
            break;
        default:
            throw new IllegalArgumentException("Unsupported phase: " + phase);
        }
    }

    /*
     * (non-Javadoc)
     * @see vroom.common.heuristics.vls.IVLSState#getParentVLS()
     */
    @Override
    public VersatileLocalSearch<S> getParentVLS() {
        return mParentVLS;
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.vls.IVLSState#getCurrentPhase()
     */
    @Override
    public VLSPhase getCurrentPhase() {
        return mCurrentPhase;
    }

    /* (non-Javadoc)
     * @see edu.uniandes.copa.vls.IVLSState#setCurrentPhase(edu.uniandes.copa.vls.VLSPhase)
     */
    @Override
    public void setCurrentPhase(VLSPhase phase) {
        mCurrentPhase = phase;
    }

    @Override
    public S getBestSolution(VLSPhase phase) {
        switch (phase) {
        case ILS:
            return mILSBestSolution;
        case ELS:
            return mELSBestSolution;
        case GRASP:
        case TERMINATED:
            return mOverallBestSolution;
        default:
            throw new IllegalArgumentException("Unsupported phase: " + phase);
        }
    }

    // @Override
    // public S setBestSolution(VLSPhase phase, S mSolution) {
    // S prev;
    //
    // switch (phase) {
    // case ILS:
    // prev= mILSBestSolution;
    // mILSBestSolution = mSolution;
    // break;
    // case ELS:
    // prev= mELSBestSolution;
    // mELSBestSolution = mSolution;
    // break;
    // case GRASP:
    // case TERMINATED:
    // prev= mOverallBestSolution;
    // mOverallBestSolution = mSolution;
    // break;
    // default:
    // throw new IllegalArgumentException("Unsupported phase: "+phase);
    // }
    //
    // return prev;
    // }

    @Override
    public String toString() {
        return String.format("sols:[GRASP:%s, ILS:%s, ELS:%s]", getBestSolution(VLSPhase.GRASP),
                getBestSolution(VLSPhase.ILS), getBestSolution(VLSPhase.ELS));
    }

    @Override
    public int getNonImprovingIterationCount(VLSPhase phase) {
        switch (phase) {
        case ILS:
            return mNonImprovingILS;
        case ELS:
            return mNonImprovingELS;
        case GRASP:
            return mNonImprovingGRASP;
        default:
            throw new IllegalArgumentException("Unsupported phase: " + phase);
        }
    }

    @Override
    public void resetBestSolution(VLSPhase phase) {
        switch (phase) {
        case ILS:
            mILSBestSolution = null;
            mNonImprovingILS = 0;
            break;
        case ELS:
            mELSBestSolution = null;
            mNonImprovingELS = 0;
            break;
        default:
            throw new IllegalArgumentException("Unsupported phase: " + phase);
        }
    }

    @Override
    public boolean stopConditionReached() {
        return false;
    }

    /* (non-Javadoc)
     * @see vroom.common.heuristics.vls.IVLSState#reset()
     */
    @Override
    public void reset() {
        mCurrentPhase = VLSPhase.INIT;
        mELSBestSolution = null;
        mILSBestSolution = null;
        mNonImprovingELS = 0;
        mNonImprovingGRASP = 0;
        mNonImprovingILS = 0;
        mOverallBestSolution = null;
    }

}// end VLSStateBase