/**
 * 
 */
package vroom.common.instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import umontreal.iro.lecuyer.rng.MRG32k3a;
import umontreal.iro.lecuyer.rng.RandomStream;
import vroom.common.heuristics.ConstraintHandler;
import vroom.common.heuristics.cw.CWParameters;
import vroom.common.heuristics.cw.algorithms.RandomizedSavingsHeuristic;
import vroom.common.heuristics.cw.kernel.ClarkeAndWrightHeuristic;
import vroom.common.heuristics.vrp.constraints.CapacityConstraint;
import vroom.common.modeling.dataModel.ListRoute.ArrayListRoute;
import vroom.common.modeling.dataModel.INodeVisit;
import vroom.common.modeling.dataModel.IVRPInstance;
import vroom.common.modeling.dataModel.IVRPRequest;
import vroom.common.modeling.dataModel.Solution;
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.modeling.io.ChristofidesPersistenceHelper;
import vroom.common.modeling.io.FlatFilePersistenceHelper;
import vroom.common.utilities.FileBufferedWriter;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.Utilities.Random;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.ssj.RandomSourceBase;

/**
 * The class <code>DVRPReleaseDateGenerator</code> is used to generate a release date file for a given instance of the
 * VRP
 * <p>
 * Creation date: Nov 8, 2011 - 10:30:45 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DVRPReleaseDateGenerator extends RandomSourceBase {

    /**
     * The format string for the release date file, the first argument is the path to the folder, the second the
     * instance name, and the third the proportion of dynamic requests
     */
    public static String sNameFormat = "%1$s/%2$s_rd_%3$s.txt";

    /**
     * Creates a new <code>DVRPReleaseDateGenerator</code>
     * 
     * @param stream
     *            the random stream that will be used to generate random numbers
     */
    public DVRPReleaseDateGenerator(RandomStream stream) {
        super(stream);
    }

    /**
     * Generate a release date file and save it in the specified {@code path} using the format defined in
     * {@link #sNameFormat}
     * 
     * @param instance
     *            the static instance
     * @param dynProp
     *            the proportion of dynamic requests (between 0 an 1)
     * @param destDir
     *            the destination folder
     * @throws IOException
     */
    public void generateRDFile(IVRPInstance instance, double dynProp, String destDir) throws IOException {
        // Get the planning horizon
        int hStart = 0;
        int hEnd = 0;
        ITimeWindow tw = instance.getDepot(0).getTimeWindow();
        if (tw != null) {
            hStart = (int) tw.startAsDouble();
            hEnd = (int) tw.endAsDouble();
        } else {
            hStart = 0;
            hEnd = getHorizonBound(instance);
        }

        List<IVRPRequest> requests = instance.getRequests();
        Collections.sort(requests, ObjectWithIdComparator.INSTANCE);
        int numDynReq = (int) (requests.size() * dynProp);

        // Draw the dynamic requests
        int[] dynReqIdx = Random.randomIndexes(requests.size(), numDynReq, getRandomStream());
        Arrays.sort(dynReqIdx);

        // Split the request list in two lists
        ArrayList<IVRPRequest> staticReq = new ArrayList<IVRPRequest>(requests.size() - numDynReq);
        ArrayList<IVRPRequest> dynReq = new ArrayList<IVRPRequest>(numDynReq);
        ListIterator<IVRPRequest> it = requests.listIterator();
        int dynIdx = 0;
        while (it.hasNext()) {
            if (dynIdx < dynReqIdx.length && it.nextIndex() == dynReqIdx[dynIdx]) {
                dynReq.add(it.next());
                dynIdx++;
            } else {
                staticReq.add(it.next());
            }
        }

        generateRDFile(instance.getName(), staticReq, dynReq, hStart, hEnd, destDir, "" + ((int) (100 * dynProp)));

    }

    /**
     * Generate a release date file and save it in the specified {@code path} using the format defined in
     * {@link #sNameFormat} with the {@code  comment} as third argument
     * 
     * @param instanceName
     *            the name of the instance
     * @param staticReq
     *            a list of static requests
     * @param dynReq
     *            a list of dynamic requests
     * @param minReleaseDate
     *            the minimum value for the release date
     * @param maxReleaseDate
     *            the maximum value for the release date
     * @param destDir
     *            the destination folder
     * @param comment
     *            a comment for the file name
     * @throws IOException
     */
    public void generateRDFile(String instanceName, List<IVRPRequest> staticReq, List<IVRPRequest> dynReq,
            int minReleaseDate, int maxReleaseDate, String destDir, String comment) throws IOException {
        FileBufferedWriter out = new FileBufferedWriter(String.format(sNameFormat, destDir, instanceName, comment));

        // Write a comment line
        out.writeLine("# Instance %s - %s dynamic requests - %s static requests", instanceName, dynReq.size(),
                staticReq.size());

        // #Dyn/Static Propotions
        out.writeLine("%-5s %s", "Dyn", "Stat");
        out.writeLine("%-5s %s", dynReq.size(), staticReq.size());

        // Headers
        out.writeLine("%-5s %s", "ID", "RD");

        // Write static requests
        for (IVRPRequest r : staticReq) {
            out.writeLine("%-5s %s", r.getID(), -1);
        }

        // Write dynamic requests
        for (IVRPRequest r : dynReq) {
            out.writeLine("%-5s %s", r.getID(), getRandomStream().nextInt(minReleaseDate, maxReleaseDate));
        }

        // Flush and close
        out.flush();
        out.close();
    }

    /**
     * Estimate a bound on the planning horizon length by constructing a solution and JAVADOC
     * 
     * @param instance
     * @return
     */
    protected int getHorizonBound(IVRPInstance instance) {
        CWParameters params = new CWParameters();
        params.set(CWParameters.ALGORITHM_CLASS, RandomizedSavingsHeuristic.class);
        params.set(CWParameters.CTR_HANDLER_CLASS, ConstraintHandler.class);

        ClarkeAndWrightHeuristic<Solution<ArrayListRoute>> cw = new ClarkeAndWrightHeuristic<Solution<ArrayListRoute>>(
                params);

        cw.getConstraintHandler().addConstraint(new CapacityConstraint<Solution<ArrayListRoute>>());

        cw.initialize(instance);
        cw.run();

        // A correction factor in the case of limited fleet (removed for now)
        // -> More routes => underestimated bound => easier problem as more
        // requests will be disclosed early
        double correction = 1;
        if (!instance.getFleet().isUnlimited()) {
            boolean feas = cw.getSavingsAlgo().repairSolutionForLimitedFleet();
            // correction = cw.getSolution().getRouteCount()
            // / instance.getFleet().size();
        }

        Solution<ArrayListRoute> sol = cw.getSolution();
        int maxDur = 0;
        for (ArrayListRoute r : sol) {
            if (r.length() < 2)
                continue;
            double dur = 0;
            ListIterator<INodeVisit> it = r.iterator();
            INodeVisit pred = it.next();
            dur += pred.getServiceTime();
            while (it.hasNext()) {
                INodeVisit node = it.next();
                dur += node.getServiceTime();
                dur += instance.getCostDelegate().getTravelTime(pred, node, r.getVehicle());
                pred = node;
            }
            if (dur > maxDur)
                maxDur = (int) dur;
        }
        return (int) (maxDur * correction);
    }

    /**
     * Generate the release date files for a collection of instances
     * 
     * @param instances
     * @param dynProp
     * @param destDir
     * @param stream
     */
    public static void generateRDFFiles(Collection<IVRPInstance> instances, double dynProp, String destDir,
            RandomStream stream) {
        DVRPReleaseDateGenerator gen = new DVRPReleaseDateGenerator(stream);
        for (IVRPInstance i : instances) {
            try {
                gen.generateRDFile(i, dynProp, destDir);
                Logging.getBaseLogger().info(
                        "DVRPReleaseDateGenerator.generateRDFFiles: write rd file for instance %s (dynProp: %s)",
                        i.getName(), dynProp);
            } catch (IOException e) {
                Logging.getBaseLogger().exception("DVRPReleaseDateGenerator.generateRDFFiles", e);
            }
        }
    }

    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_WARN, false);
        String sourceDir = "../Instances/cvrp/christofides-mingozzi-toth";
        String filePattern = "vrpnc.+txt";
        FlatFilePersistenceHelper reader = new ChristofidesPersistenceHelper();

        double dynProp = 0.1;
        String destDir = "../Instances/dvrp/pillac";
        MRG32k3a stream = new MRG32k3a();
        int firstSeed = sourceDir.hashCode();
        if (firstSeed == 0)
            firstSeed = 1;
        stream.setSeed(new long[] { firstSeed, 2, 3, 4, 5, 6 });

        try {
            List<File> files = Utilities.listFiles(sourceDir, filePattern);

            List<IVRPInstance> instances = new ArrayList<IVRPInstance>(files.size());
            for (File f : files) {
                try {
                    instances.add(reader.readInstance(f));
                    System.out.println("Added instance " + f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            generateRDFFiles(instances, dynProp, destDir, stream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("FINISHED");
    }
}
