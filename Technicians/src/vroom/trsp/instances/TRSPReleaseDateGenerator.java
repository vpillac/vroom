/**
 * 
 */
package vroom.trsp.instances;

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
import vroom.common.modeling.dataModel.attributes.ITimeWindow;
import vroom.common.utilities.FileBufferedWriter;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.Utilities.Random;
import vroom.common.utilities.dataModel.ObjectWithIdComparator;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.common.utilities.ssj.RandomSourceBase;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.io.PillacSimplePersistenceHelper;

/**
 * The class <code>TRSPReleaseDateGenerator</code> is used to generate a release date file for a given instance of the
 * VRP
 * <p>
 * Creation date: Nov 8, 2011 - 10:30:45 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPReleaseDateGenerator extends RandomSourceBase {

    /**
     * The format string for the release date file, the first argument is the path to the folder, the second the
     * instance name, and the third the proportion of dynamic requests
     */
    public static String sNameFormat = "%1$s/%2$s_rd_%3$s.txt";

    /**
     * Creates a new <code>TRSPReleaseDateGenerator</code>
     * 
     * @param stream
     *            the random stream that will be used to generate random numbers
     */
    public TRSPReleaseDateGenerator(RandomStream stream) {
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
    public void generateRDFile(TRSPInstance instance, double dynProp, String destDir)
            throws IOException {
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

        List<TRSPRequest> requests = new ArrayList<TRSPRequest>(instance.getRequests());
        Collections.sort(requests, ObjectWithIdComparator.INSTANCE);
        int numDynReq = (int) (requests.size() * dynProp);

        // Draw the dynamic requests
        int[] dynReqIdx = Random.randomIndexes(requests.size(), numDynReq, getRandomStream());
        Arrays.sort(dynReqIdx);

        // Split the request list in two lists
        ArrayList<TRSPRequest> staticReq = new ArrayList<TRSPRequest>(requests.size() - numDynReq);
        ArrayList<TRSPRequest> dynReq = new ArrayList<TRSPRequest>(numDynReq);
        ListIterator<TRSPRequest> it = requests.listIterator();
        int dynIdx = 0;
        while (it.hasNext()) {
            if (dynIdx < dynReqIdx.length && it.nextIndex() == dynReqIdx[dynIdx]) {
                dynReq.add(it.next());
                dynIdx++;
            } else {
                staticReq.add(it.next());
            }
        }

        generateRDFile(instance, staticReq, dynReq, hStart, hEnd, destDir, ""
                + ((int) (100 * dynProp)));

    }

    /**
     * Generate a release date file and save it in the specified {@code path} using the format defined in
     * {@link #sNameFormat} with the {@code  comment} as third argument
     * 
     * @param instance
     *            the instance
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
    public void generateRDFile(TRSPInstance instance, List<TRSPRequest> staticReq,
            List<TRSPRequest> dynReq, int minReleaseDate, int maxReleaseDate, String destDir,
            String comment) throws IOException {
        FileBufferedWriter out = new FileBufferedWriter(String.format(sNameFormat, destDir,
                instance.getName(), comment));

        // Write a comment line
        out.writeLine("# Instance %s - %s dynamic requests - %s static requests",
                instance.getName(), dynReq.size(), staticReq.size());

        // #Dyn/Static Propotions
        out.writeLine("%-5s %s", "Dyn", "Stat");
        out.writeLine("%-5s %s", dynReq.size(), staticReq.size());

        // Headers
        out.writeLine("%-5s %s", "ID", "RD");

        // Write static requests
        for (TRSPRequest r : staticReq) {
            out.writeLine("%-5s %s", r.getID(), -1);
        }

        // Write dynamic requests
        for (TRSPRequest r : dynReq) {
            // Estimate the latest feasible feasible release date
            // as the difference between the time window end and 1.5 the time required to travel from the main depot to
            // the request
            double rdBound = r.getTimeWindow().endAsDouble()
                    - 1.5
                    * instance.getCostDelegate().getTravelTime(instance.getMainDepot().getID(),
                            r.getID(), instance.getFleet().getVehicle());
            int maxRd = Math.min(maxReleaseDate, (int) rdBound);
            maxRd = Math.min((int) r.getTimeWindow().startAsDouble(), maxRd);
            int rd = maxRd >= minReleaseDate ? getRandomStream().nextInt(minReleaseDate, maxRd)
                    : minReleaseDate;
            out.writeLine("%-5s %s", r.getID(), rd);
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
    protected int getHorizonBound(TRSPInstance instance) {
        throw new UnsupportedOperationException();
        // CWParameters params = new CWParameters();
        // params.set(CWParameters.ALGORITHM_CLASS, RandomizedSavingsHeuristic.class);
        // params.set(CWParameters.CTR_HANDLER_CLASS, ConstraintHandler.class);
        //
        // ClarkeAndWrightHeuristic<Solution<ArrayListRoute>> cw = new
        // ClarkeAndWrightHeuristic<Solution<ArrayListRoute>>(
        // params);
        //
        // cw.getConstraintHandler().addConstraint(new CapacityConstraint<Solution<ArrayListRoute>>());
        //
        // cw.initialize(instance);
        // cw.run();
        //
        // // A correction factor in the case of limited fleet (removed for now)
        // // -> More routes => underestimated bound => easier problem as more
        // // requests will be disclosed early
        // double correction = 1;
        // if (!instance.getFleet().isUnlimited()) {
        // boolean feas = cw.getSavingsAlgo().repairSolutionForLimitedFleet();
        // // correction = cw.getSolution().getRouteCount()
        // // / instance.getFleet().size();
        // }
        //
        // Solution<ArrayListRoute> sol = cw.getSolution();
        // int maxDur = 0;
        // for (ArrayListRoute r : sol) {
        // if (r.length() < 2)
        // continue;
        // double dur = 0;
        // ListIterator<INodeVisit> it = r.iterator();
        // INodeVisit pred = it.next();
        // dur += pred.getServiceTime();
        // while (it.hasNext()) {
        // INodeVisit node = it.next();
        // dur += node.getServiceTime();
        // dur += instance.getCostDelegate().getTravelTime(pred, node, r.getVehicle());
        // pred = node;
        // }
        // if (dur > maxDur)
        // maxDur = (int) dur;
        // }
        // return (int) (maxDur * correction);
    }

    /**
     * Generate the release date files for a collection of instances
     * 
     * @param instances
     * @param dynProp
     * @param destDir
     * @param stream
     */
    public static void generateRDFFiles(Collection<TRSPInstance> instances, double[] dynProp,
            String destDir, RandomStream stream) {
        TRSPReleaseDateGenerator gen = new TRSPReleaseDateGenerator(stream);
        for (TRSPInstance i : instances) {
            for (double d : dynProp) {

                try {
                    gen.generateRDFile(i, d, destDir);
                    System.out
                            .printf("TRSPReleaseDateGenerator.generateRDFFiles: write rd file for instance %s (dynProp: %s)\n",
                                    i.getName(), d);
                } catch (IOException e) {
                    Logging.getBaseLogger().exception("TRSPReleaseDateGenerator.generateRDFFiles",
                            e);
                }
            }
        }
    }

    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_WARN, false);
        String sourceDir = "../Instances/trsp/pillac/crew25";
        String filePattern = ".+txt";
        ITRSPPersistenceHelper reader = new PillacSimplePersistenceHelper();

        // double[] dynProp = new double[] { 0.1, 0.5, 0.9 };
        double[] dynProp = new double[] { 0.3, 0.7 };
        String destDir = "../Instances/trsp/pillac/crew25/dyn";
        MRG32k3a stream = new MRG32k3a();
        int firstSeed = sourceDir.hashCode();
        if (firstSeed == 0)
            firstSeed = 1;
        stream.setSeed(new long[] { firstSeed, 2, 3, 4, 5, 6 });

        try {
            List<File> files = Utilities.listFiles(sourceDir, filePattern);

            List<TRSPInstance> instances = new ArrayList<TRSPInstance>(files.size());
            for (File f : files) {
                try {
                    instances.add(reader.readInstance(f, false));
                    // System.out.println("Added instance " + f);
                } catch (Exception e) {
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
