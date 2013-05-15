/**
 *
 */
package vroom.common.heuristics.alns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.utilities.RouletteWheel;
import vroom.common.utilities.optimization.IComponentHandler;
import vroom.common.utilities.optimization.IInstance;

/**
 * <code>ALNSComponentHandler</code> is a generic implementation of the ALNS destroy/repair selection process presented
 * in
 * <p>
 * Ropke, S. & Pisinger, D.<br/>
 * An adaptive large neighborhood search heuristic for the pickup and delivery problem with time windows<br/>
 * Transportation Science, 2006, 40, 455-472
 * </p>
 * <p>
 * Creation date: May 12, 2011 - 1:49:12 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ALNSComponentHandler<M extends IALNSComponent<?>> implements IComponentHandler<M> {

    public static boolean sNewWeightUpdate = true;

    /** the increment applied for components that lead to a best solution **/
    private final double  mSigmaOne;

    /**
     * Getter for the increment applied for components that lead to a best solution
     * 
     * @return the value of sigmaOne
     */
    public double getSigmaOne() {
        return this.mSigmaOne;
    }

    /** the increment applied for components that lead to a different and better current solution **/
    private final double mSigmaTwo;

    /**
     * Getter for the increment applied for components that lead to a different and better current solution
     * 
     * @return the value of sigmaTwo
     */
    public double getSigmaTwo() {
        return this.mSigmaTwo;
    }

    /** the increment applied for components that lead to a different non-improving current solution **/
    private final double mSigmaThree;

    /**
     * Getter for the increment applied for components that lead to a different non-improving current solution
     * 
     * @return the value of sigmaThree
     */
    public double getSigmaThree() {
        return this.mSigmaThree;
    }

    /** the reaction factor **/
    private final double mReactionFactor;

    /**
     * Getter for the reaction factor
     * 
     * @return the value of reactionFactor
     */
    public double getReactionFactor() {
        return this.mReactionFactor;
    }

    /** the length of the time segments in number of iterations **/
    private final int mTimeSegmentLength;

    /**
     * Getter for the length of the time segments in number of iterations
     * 
     * @return the value of timeSegmentLength
     */
    public int getTimeSegmentLength() {
        return this.mTimeSegmentLength;
    }

    /** the score of each component */
    private final Map<M, Evaluation> mEvaluations;

    /** the current selection wheel, replaced at the beginning of each segment (to prevent unecessary synchronization) */
    private RouletteWheel<M>         mWheel;

    /** a random source */
    private final RandomStream       mRndStream;

    /**
     * Creates a new <code>ALNSComponentHandler</code>.
     * 
     * @param rndStream
     *            a random stream
     * @param sigma1
     *            the sigma1
     * @param sigma2
     *            the sigma2
     * @param sigma3
     *            the sigma3
     * @param reactionFactor
     *            the reaction factor
     * @param timeSegLength
     *            the length of each time segment
     */
    public ALNSComponentHandler(RandomStream rndStream, Collection<M> components, double sigma1, double sigma2,
            double sigma3, double reactionFactor, int timeSegLength) {
        mRndStream = rndStream;
        mEvaluations = new HashMap<M, Evaluation>();
        mWheel = new RouletteWheel<M>();

        mSigmaOne = sigma1;
        mSigmaTwo = sigma2;
        mSigmaThree = sigma3;
        mReactionFactor = reactionFactor;
        mTimeSegmentLength = timeSegLength;

        for (M c : components) {
            mEvaluations.put(c, new Evaluation(0, 0, 1d / components.size()));
        }
        reset();
    }

    @Override
    public List<M> getComponents() {
        return new ArrayList<M>(mEvaluations.keySet());
    }

    /**
     * Returns a copy of the evaluation of a component
     * 
     * @return a copy of the evaluation of a component
     */
    public Evaluation getEvaluation(M component) {
        Evaluation e = mEvaluations.get(component);
        return e != null ? e.clone() : null;
    }

    @Override
    public double getWeight(M component) {
        return getEvaluation(component).getWeight();
    };

    @Override
    public M nextComponent() {
        return mWheel.drawObject(mRndStream, false);
    }

    @Override
    public boolean isCompletelyExplored() {
        return false;
    }

    @Override
    public boolean updateStats(M currentComponent, double improvement, double time, int iteration, Outcome state) {
        boolean changed = false;
        if (iteration % mTimeSegmentLength == 0) {
            // Update weights and reset scores and counts
            updateWeights();
            for (Entry<M, Evaluation> eval : mEvaluations.entrySet()) {
                eval.getValue().reset();
            }
            changed = true;
        }

        // Update score
        switch (state) {
        case NEW_BEST:
            mEvaluations.get(currentComponent).updateScore(getSigmaOne());
            break;
        case ACCEPTED:
            if (improvement > 0)
                mEvaluations.get(currentComponent).updateScore(getSigmaTwo());
            else
                mEvaluations.get(currentComponent).updateScore(getSigmaThree());
            break;
        case REJECTED:
            // Do nothing
            break;
        default:
            throw new IllegalArgumentException("Unsupported state: " + state);
        }

        // Update count
        mEvaluations.get(currentComponent).increaseCount();

        return changed;
    }

    /**
     * Update the weights and the selection wheel
     */
    void updateWeights() {
        RouletteWheel<M> wheel = new RouletteWheel<>();
        if (sNewWeightUpdate) {
            double overallScore = 0;
            for (Entry<M, Evaluation> eval : mEvaluations.entrySet()) {
                overallScore += eval.getValue().getScore();
            }
            if (overallScore == 0)
                overallScore = 1;
            for (Entry<M, Evaluation> eval : mEvaluations.entrySet()) {
                eval.getValue().updateWeight(getReactionFactor(), eval.getValue().getScore() / overallScore);
                wheel.add(eval.getKey(), eval.getValue().getWeight());
            }
        } else {
            for (Entry<M, Evaluation> eval : mEvaluations.entrySet()) {
                Evaluation e = eval.getValue();
                if (e.getCount() != 0)
                    eval.getValue().updateWeight(getReactionFactor());
                wheel.add(eval.getKey(), eval.getValue().getWeight());
            }
        }
        // Replace previous wheel
        mWheel = wheel;
    }

    @Override
    public void initialize(IInstance instance) {
        reset();
    }

    @Override
    public void reset() {
        RouletteWheel<M> wheel = new RouletteWheel<>();
        for (M comp : mEvaluations.keySet()) {
            Evaluation e = new Evaluation(0, 0, 1d / mEvaluations.size());
            mEvaluations.put(comp, e);
            wheel.add(comp, e.getWeight());
        }
        // Replace previous wheel
        mWheel = wheel;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("s1=%s,s2=%s,s3=%s,r=%s,T=%s", getSigmaOne(), getSigmaTwo(), getSigmaThree(),
                getReactionFactor(), getTimeSegmentLength()));
        sb.append("[");
        Iterator<Entry<M, Evaluation>> it = mEvaluations.entrySet().iterator();
        while (it.hasNext()) {
            Entry<M, Evaluation> e = it.next();
            sb.append(String.format("%s (%s)", e.getKey(), e.getValue()));
            if (it.hasNext())
                sb.append(",");
        }
        sb.append("]");

        return sb.toString();
    }

    /**
     * <code>Evaluation</code> is a simple wrapper for the different aspect of a component evaluation
     * <p>
     * Creation date: May 12, 2011 - 3:35:58 PM
     * 
     * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
     *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
     *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
     * @version 1.0
     */
    protected static class Evaluation implements Cloneable {
        private int    mCount;
        private double mScore;
        private double mWeight;

        /**
         * Reset the score and count of this evaluation
         */
        private void reset() {
            this.mScore = 0;
            this.mCount = 0;
        }

        /**
         * Update the score by adding the given <code>delta</code> value
         * 
         * @param delta
         */
        private void updateScore(double delta) {
            this.mScore += delta;
        }

        /**
         * Getter for <code>score</code>
         * 
         * @return the score
         */
        protected double getScore() {
            return mScore;
        }

        /**
         * Increase the counter
         */
        private void increaseCount() {
            this.mCount++;
        }

        /**
         * Getter for <code>count</code>
         * 
         * @return the count
         */
        protected int getCount() {
            return mCount;
        }

        /**
         * Getter for <code>weight</code>
         * 
         * @return the weight
         */
        protected double getWeight() {
            return mWeight;
        }

        /**
         * Update this evaluation weight
         * 
         * @param reactionFactor
         */
        private void updateWeight(double reactionFactor) {
            if (getCount() > 0)
                this.mWeight = getWeight() * (1 - reactionFactor) + reactionFactor * getScore() / getCount();
        }

        /**
         * Update this evaluation weight
         * 
         * @param reactionFactor
         *            the reaction factor to be used
         * @param newWeight
         *            the weight that will be assigned if <code>reactionFactor=1</code>
         */
        public void updateWeight(double reactionFactor, double newWeight) {
            this.mWeight = getWeight() * (1 - reactionFactor) + reactionFactor * newWeight;
        }

        /**
         * Creates a new <code>Evaluation</code>
         * 
         * @param count
         * @param score
         * @param weight
         */
        private Evaluation(int count, double score, double weight) {
            this.mCount = count;
            this.mScore = score;
            this.mWeight = weight;
        }

        @Override
        public String toString() {
            return String.format("c:%s,s:%.2f,w:%.2f", getCount(), getScore(), getWeight());
        }

        @Override
        protected Evaluation clone() {
            return new Evaluation(getCount(), getScore(), getWeight());
        }

    }

    @Override
    public void dispose() {
        for (M comp : mEvaluations.keySet()) {
            comp.dispose();
        }
        mEvaluations.clear();
        mWheel.clear();
    }

}
