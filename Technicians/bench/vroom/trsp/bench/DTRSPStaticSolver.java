/**
 * 
 */
package vroom.trsp.bench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import vroom.common.utilities.FileBufferedWriter;
import vroom.common.utilities.Stopwatch;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * The class <code>DTRSPStaticSolver</code> is used to evaluate the aposteriori solution for already executed DTRSP
 * simulations. It reads a csv file and solves the static instances removing requests that were rejected.
 * <p>
 * Creation date: Feb 2, 2012 - 9:15:44 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class DTRSPStaticSolver {

    private final Map<String, int[]> mRejectedRequests;
    private final String             mCSVFile;

    public DTRSPStaticSolver(TRSPGlobalParameters params, String fileCom, String csvFile)
            throws IOException {
        mCSVFile = csvFile;
        TRSPBench.setup(params, false, fileCom);
        TRSPBench.getInstance().createRuns();
        mRejectedRequests = new HashMap<String, int[]>();
        readCSV(csvFile);
    }

    /**
     * Read the CSV file and populate the {@link #mRejectedRequests} map
     * 
     * @param csvFile
     * @throws IOException
     */
    void readCSV(String csvFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(csvFile));

        String line;
        while ((line = in.readLine()) != null && !line.startsWith("name;")) {
        }
        line = in.readLine();

        while (line != null) {
            String[] data = line.split(";");
            String key = getKey(data[0], data[5], data[4]);
            int[] rejected = Utilities.toIntArray(data[13]);

            int[] prev = mRejectedRequests.put(key, rejected);
            if (prev != null) {
                in.close();
                throw new IllegalStateException(
                        "A set of rejected requests was already defined for key " + key);
            }

            line = in.readLine();
        }
        in.close();
    }

    /**
     * Return the key associated with a given run (Name-Comment#Run)
     * 
     * @param name
     * @param comment
     * @param id
     * @return the key associated with a given run (Name-Comment#Run)
     */
    private String getKey(String name, String comment, String id) {
        return String.format("%s-%s#%s", name, comment, id);
    }

    public void run() {
        FileBufferedWriter writer = null;
        try {
            writer = new FileBufferedWriter(String.format(
                    "results/dtrsp/trsp_bench_%s_dtrsp_aposteriori-%s.csv",
                    Utilities.Time.getDateString(), TRSPBench.getInstance().getFileComment()));
        } catch (IOException e) {
            e.printStackTrace();
            Logging.awaitLogging(60000);
            System.exit(1);
        }
        try {
            writer.writeCommentLine("CSV File: %s", mCSVFile);
            writer.writeLine("name;run;comment;time;aposteriori;rejected;sol_rejected;sol_rejected_count");
            writer.flush();
        } catch (IOException e) {
            TRSPLogging.getRunLogger().exception("DTRSPStaticSolver.run", e);
        }

        TRSPLogging.getRunLogger().info("Added %s runs", TRSPBench.getInstance().getRuns().size());
        int i = 0;
        ListIterator<TRSPRunBase> it = TRSPBench.getInstance().getRuns().listIterator();
        while (it.hasNext()) {
            TRSPRunBase run = it.next();
            it.remove();
            i++;

            String key = getKey(run.getInstance().getName(), run.getComment(), "" + run.getRun());
            int[] rejected = mRejectedRequests.get(key);

            for (int reqId : rejected)
                run.getInstance().getSimulator().markAsRejected(reqId);

            TRSPLogging.getRunLogger().info("Run %s (%s) - Rejected: %s", i, key,
                    Utilities.toShortString(rejected));

            Stopwatch timer = new Stopwatch();

            timer.start();
            TRSPSolution sol = solveStaticRun(run);
            timer.stop();
            try {
                writer.writeLine("%s;%s;%s;%s;%s;%s;%s;%s", run.getInstance().getName(),
                        run.getRun(), run.getComment(), timer.readTimeMS(),
                        sol.getObjectiveValue(), Utilities.toShortString(rejected),
                        Utilities.toShortString(sol.getUnservedRequests()), sol.getUnservedCount());
                writer.flush();
            } catch (IOException e) {
                TRSPLogging.getRunLogger().exception("DTRSPStaticSolver.run", e);
            }

            run.dispose();
            System.gc();
        }
    }

    TRSPSolution solveStaticRun(TRSPRunBase run) {
        return run.aPosterioriRun();
    }

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {
        TRSPBench.setupLoggers(LoggerHelper.LEVEL_INFO, LoggerHelper.LEVEL_DEBUG, null);

        String baseFile = "results/dtrsp/trsp_bench_120128_15-45_DTRSP_pALNS_Regret_subset";

        if (args.length == 1)
            baseFile = args[0];

        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File(baseFile + ".cfg"));
            DTRSPStaticSolver solver = new DTRSPStaticSolver(params, "pALNS_Regret_subset",
                    baseFile + ".csv");

            solver.run();
        } catch (Exception e) {
            TRSPLogging.getRunLogger().exception("DTRSPStaticSolver.main", e);
        }

    }

}
