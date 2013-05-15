/**
 * 
 */
package vroom.optimization.online.jmsa.benchmarking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.probdist.UniformIntDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvar.UniformIntGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.modeling.dataModel.attributes.StochasticDemand;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPActualRequest;

/**
 * <code>NovoaSimulationDemands</code> is the class responsible for the generation of the demands in a {@link NovoaRun}
 * <p>
 * Creation date: Oct 12, 2010 - 10:06:54 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a href="http://copa.uniandes.edu.co">Copa</a> <a
 *         href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class NovoaSimulationDemands {

    public static final String DEMANDS_PROP_PATH = "./results/novoa_demands.dat";
    private final Properties   mDemandsProp;

    public NovoaSimulationDemands() {
        super();
        mDemandsProp = new Properties();
        try {
            File file = new File(DEMANDS_PROP_PATH);

            if (!file.exists()) {
                file.createNewFile();
            }

            mDemandsProp.load(new BufferedReader(new FileReader(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate the customer demands for the given instance and seeds
     * 
     * @param run
     * @param size
     * @param rep
     * @param cap
     * @param set
     * @param instance
     * @return a map between requests and their demands
     */
    public static Map<VRPActualRequest, Double> getDemands(int run, int size, int rep, int cap, int set,
            MSAVRPInstance instance) {
        long[] seeds = NovoaBenchmarking.getSeeds(run, size, rep, cap, set);

        return getDemands(seeds, instance);
    }

    /**
     * Generate the customer demands for the given instance and seeds
     * 
     * @param seeds
     * @param instance
     * @return a map between requests and their demands
     */
    public static Map<VRPActualRequest, Double> getDemands(long[] seeds, MSAVRPInstance instance) {
        MRG32k3a rndStream = new MRG32k3a("NovoaDemands");
        rndStream.setSeed(seeds);

        HashMap<VRPActualRequest, Double> demands = new HashMap<VRPActualRequest, Double>();

        List<VRPActualRequest> requests = instance.getPendingRequests();
        Collections.sort(requests, new ObjectWithIdComparator());
        for (VRPActualRequest r : requests) {

            Distribution dist = ((StochasticDemand) r.getParentRequest().getAttribute(
                RequestAttributeKey.DEMAND)).getDistribution(0);

            if (dist instanceof UniformIntDist) {
                UniformIntGen gen = new UniformIntGen(rndStream, (UniformIntDist) dist);
                demands.put(r, gen.nextDouble());
            } else if (dist instanceof NormalDist) {
                NormalGen gen = new NormalGen(rndStream, (NormalDist) dist);
                double d = gen.nextDouble();
                d = Math.round(d * 1000) / 1000d;
                d = d < 0 ? 0 : d > instance.getFleet().getVehicle().getCapacity() ? instance.getFleet()
                    .getVehicle()
                    .getCapacity() : d;
                demands.put(r, d);
            } else {
                throw new IllegalArgumentException("Distribution not supported: "
                        + dist.getClass().getSimpleName());
            }

        }

        return demands;
    }
}
