/**
 * 
 */
package vroom.trsp.optimization.alns;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import umontreal.iro.lecuyer.rng.RandomPermutation;
import vroom.common.heuristics.alns.IDestroy;
import vroom.common.utilities.optimization.IInstance;
import vroom.common.utilities.optimization.IParameters;
import vroom.trsp.datamodel.TRSPSolution;

/**
 * <code>DestroyRandom</code> is an implementation of {@link IDestroy} that randomly removes a given number of requests.
 * <p>
 * Creation date: May 13, 2011 - 1:35:09 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DestroyRandom extends DestroyTRSP {

    public DestroyRandom() {
        super();
    }

    @Override
    public void initialize(IInstance instance) {
        // Do nothing
    }

    @Override
    public Set<Integer> doDestroy(TRSPSolution solution, IParameters parameters, List<Integer> removableReq, int numReq) {
        RandomPermutation.shuffle(removableReq, parameters.getRandomStream());

        HashSet<Integer> remRequests = new HashSet<Integer>(numReq);

        int remReq = 0;
        int i = 0;
        while (remReq < numReq && i < removableReq.size()) {
            // Select a request to remove
            int req = removableReq.get(i++);

            // Remove the request from its tour
            if (removeRequest(solution, req) != null) {
                remReq++;
                remRequests.add(req);
            }
        }

        return remRequests;
    }

    @Override
    public DestroyRandom clone() {
        return new DestroyRandom();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        return "rnd";
    };
}
