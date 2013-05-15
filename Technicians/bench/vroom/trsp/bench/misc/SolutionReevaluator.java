package vroom.trsp.bench.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import vroom.common.utilities.StatCollector;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.trsp.bench.CVRPUtilities;
import vroom.trsp.bench.TRSPRunBase;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.costDelegates.TRSPCostDelegate;
import vroom.trsp.datamodel.costDelegates.TRSPDistance;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.util.TRSPGlobalParameters;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>SolutionReevaluator</code>
 * <p>
 * Creation date: Aug 14, 2012 - 8:56:06 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class SolutionReevaluator {

    public static String sSolHeader      = "final_sol";
    public static String sInstanceHeader = "name";

    public static void reevaluateSolutionCost(String csvFile, boolean dynamic,
            TRSPCostDelegate costDelegate, String instanceFolder, boolean overwrite,
            String costHeader) throws IOException {
        String cfgFile = csvFile.replace(".csv", ".cfg");
        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File(cfgFile));
        } catch (Exception e1) {
            TRSPLogging.getBaseLogger().exception("SolutionReevaluator.reevaluateSolutionCost", e1);
            return;
        }

        BufferedReader in = new BufferedReader(new FileReader(csvFile));

        File tmp = new File(csvFile + ".tmp");
        BufferedWriter out = new BufferedWriter(new FileWriter(tmp));

        String line = in.readLine();
        out.append(line);
        while (line != null && !line.contains(sInstanceHeader)) {
            out.append('\n');
            line = in.readLine();
            out.append(line);
        }

        if (line == null) {
            System.out.println("Header not found: " + sSolHeader);
            return;
        }

        out.append(StatCollector.sCVSSeparator);
        out.append("new_" + costHeader + "\n");

        String[] labels = line.split(StatCollector.sCVSSeparator);
        int nameCol = 0;
        while (!labels[nameCol].equals(sInstanceHeader))
            nameCol++;
        int costCol = 0;
        while (!labels[costCol].equals(costHeader))
            costCol++;
        int solCol = 0;
        while (!labels[solCol].equals(sSolHeader))
            solCol++;

        ITRSPPersistenceHelper reader = TRSPUtilities.getPersistenceHelper(instanceFolder);

        out.flush();
        line = in.readLine();
        while (line != null) {
            out.append(line);
            out.append(StatCollector.sCVSSeparator);

            String[] data = line.split(StatCollector.sCVSSeparator);
            String name = data[nameCol];
            String solution = data[solCol];

            if (!solution.startsWith("<")) {
                // The solution doesnt seem to be valid, ignore
                out.append('\n');
                line = in.readLine();
                System.out.printf("%s\t %s SKIPPED\n", name, data[costCol]);

                continue;
            }

            File instanceFile = new File(String.format("%s/%s.txt", instanceFolder, name));
            TRSPInstance instance;
            try {
                instance = reader.readInstance(instanceFile);
            } catch (Exception e) {
                TRSPLogging.getBaseLogger().exception("SolutionReevaluator.reevaluateSolutionCost",
                        e);
                continue;
            }
            TRSPSolution sol = CVRPUtilities.castSolution(instance, solution, costDelegate);
            // System.out.println(sol);
            if (dynamic) {
                DTRSPSolution dsol = new DTRSPSolution(sol);
                instance.setupSimulator(costDelegate, params);
                TRSPRunBase.fixFinalEarliestDeparture(dsol);
                sol = dsol;
            }

            double cost = costDelegate.evaluateSolution(sol, true, false);

            System.out.printf("%s\t %s %15.5f\n", name, data[costCol], cost);
            out.append(String.format("%.5f", cost));
            out.append('\n');
            line = in.readLine();

            out.flush();
        }

        out.flush();
        out.close();

        in.close();

        if (overwrite)
            tmp.renameTo(new File(csvFile));

        System.out.println("TERMINATED");
    }

    public static void main(String[] args) {
        TRSPLogging.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_WARN, false);

        // String csvFile =
        // "/home/vpillac/Documents/Dropbox/Doctorat/ResultsBench/results-portauto41/trsp_bench_120810_17-06_DTRSP_pALNS.csv";
        String csvFile = "/home/vpillac/Documents/Dropbox/Doctorat/ResultsBench/" +
        // "results-portauto41/trsp_bench_120805_15-32_DTRSP_Regret.csv";
        // "results/trsp_bench_120805_11-39_DTRSP_MPA.csv";
                "results/trsp_bench_120811_00-36_DTRSP_MPA_dist.csv";
        String instanceFolder = "../Instances/trsp/pillac/crew25";
        // TRSPCostDelegate cd = new TRSPWorkingTime();
        TRSPCostDelegate cd = new TRSPDistance();
        try {
            reevaluateSolutionCost(csvFile, true, cd, instanceFolder, false, "final_dis");
        } catch (IOException e) {
            TRSPLogging.getBaseLogger().exception("SolutionReevaluator.main", e);
        }
    }

}
