package vroom.common.heuristics.cw.algorithms;

import java.util.List;
import java.util.Set;

import umontreal.iro.lecuyer.rng.RandomPermutation;
import vroom.common.heuristics.cw.CWLogging;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.IJCWArc;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.modeling.dataModel.IArc;
import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.utilities.Utilities;

/**
 * <code>RandomizedBasicSavingsHeuristic</code> is a specialization of the {@link BasicSavingsHeuristic} where the list
 * of savings is being shuffled before processing.
 * <p>
 * TODO another way of randomizing the heuristic would be to assign probabilities to arcs depending on their value.
 * 
 * @author Jorge E. Mendoza <br/>
 *         <a href="http://www.uco.fr">Universite Catholique de l'Ouest</a>
 * @author Victor Pillac <br/>
 *         <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <br/>
 *         <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 2.0
 */
public class RandomizedSavingsHeuristic<S extends IVRPSolution<?>> extends BasicSavingsHeuristic<S> {

    public RandomizedSavingsHeuristic(ClarkeAndWrightHeuristic<S> parentHeuristic) {
        super(parentHeuristic);
    }

    @Override
    public List<IJCWArc> calculateSavings(Set<? extends IArc> arcs) {

        // Use an ArrayList for performance (set methods)
        List<IJCWArc> mergings = super.calculateSavings(arcs);

        randomizeSavings(mergings);

        return mergings;
    }

    /**
     * This method randomizes the savings list as proposed in: J.E. Mendoza, A.L. Medaglia and N. Velasco An
     * evolutionary-based decision support system for vehicle routing: the case of a public utility. Decision Support
     * Systems, 46(3):730-742,2009.
     * 
     * @param savingsList
     *            the savings list
     */
    private void randomizeSavings(List<IJCWArc> savingsList) {
        // Calculate the proportion of savings that should be moved to a new
        // position

        int shuffledArcsCount = getParentHeuristic().getRandomStream().nextInt(
                (int) ((savingsList.size() - 1) * getParentHeuristic().getParameters().get(
                        CWParameters.RND_MIN_FRACTION)), //
                (int) ((savingsList.size() - 1) * getParentHeuristic().getParameters().get(
                        CWParameters.RND_MAX_FRACTION)));

        // IJCWArc temporarySaving = null;
        // int temporarySavingIndex = 0;
        // boolean exchangeWithNext = false;

        // Shuffled arcs
        IJCWArc[] shuffledArcs = new IJCWArc[shuffledArcsCount];
        int[] shuffledArcsIndex = Utilities.Random.randomIndexes(savingsList.size(),
                shuffledArcsCount, getParentHeuristic().getRandomStream());

        int i = 0;
        for (int idx : shuffledArcsIndex) {
            shuffledArcs[i++] = savingsList.get(idx);
        }

        RandomPermutation.shuffle(shuffledArcs, getParentHeuristic().getRandomStream());
        i = 0;
        for (int idx : shuffledArcsIndex) {
            savingsList.set(idx, shuffledArcs[i++]);
        }

        // // We start from the top of the list cause exchanging arcs at the end of
        // // the list is not likely to impact the result of the C&W heuristic
        // for (int s = 0; s < savingsList.size(); s++) {
        // double randomizeProb = getParentHeuristic().getRandomStream().nextDouble();
        // if (randomizeProb < randomizedSavingsProp) {
        // if (exchangeWithNext == true) {
        // savingsList.set(temporarySavingIndex, savingsList.get(s));
        // savingsList.set(s, temporarySaving);
        // exchangeWithNext = false;
        // exchangedArcs = exchangedArcs + 2;
        // } else {
        // temporarySaving = savingsList.get(s);
        // temporarySavingIndex = s;
        // exchangeWithNext = true;
        // }
        // }
        // }
        CWLogging.getAlgoLogger().info(
                "RandomizedSavingsHeuristic.randomizeSavings: Shuffled saving arcs: %s (%.1f%%) ",
                shuffledArcsCount, (100d * shuffledArcsCount) / savingsList.size());
        CWLogging.getAlgoLogger().debug(
                "RandomizedSavingsHeuristic.randomizeSavings: Savings list size: %s",
                savingsList.size());
        CWLogging.getAlgoLogger().lowDebug(
                "RandomizedSavingsHeuristic.randomizeSavings: Savings list : %s", savingsList);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", super.toString(), getParentHeuristic().getRandomStream());
    }
}
