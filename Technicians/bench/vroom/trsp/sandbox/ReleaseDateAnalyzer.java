/**
 * 
 */
package vroom.trsp.sandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.math.SimpleStats;
import vroom.trsp.bench.TRSPBench;
import vroom.trsp.bench.TRSPRunBase;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.util.TRSPGlobalParameters;

/**
 * <code>ReleaseDateAnalyzer</code>
 * <p>
 * Creation date: Jun 20, 2012 - 10:23:30 AM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class ReleaseDateAnalyzer {

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_WARN, LoggerHelper.LEVEL_LOW_DEBUG, true);
        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File("./config/bench/bench_dtrsp_mpa_25crew.cfg"));
            // params.set(TRSPGlobalParameters.RUN_INSTANCE_FOLDER, "../Instances/trsp/pillac/crew25");
            // params.set(TRSPGlobalParameters.RUN_REL_DATE_FOLDER,
            // "../Instances/trsp/pillac/crew25/dyn");
            // params.set(TRSPGlobalParameters.RUN_DODS, new int[] { 10, 30, 50, 70, 90 });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        TRSPBench.setup(params, true, "");
        TRSPBench.getInstance().createRuns();

        HashMap<String, SimpleStats> avgLatest = new HashMap<>();
        HashMap<String, SimpleStats> rdGroupStats = new HashMap<>();
        SimpleStats rdStats = new SimpleStats();

        System.out.println("name;group;dod;mean_rd;min_rd;max_rd;90p;95p");
        for (TRSPRunBase run : TRSPBench.getInstance().getRuns()) {

            TRSPInstance i = run.getInstance();
            if (!avgLatest.containsKey(i.getGroup())) {
                avgLatest.put(i.getGroup(), new SimpleStats());
                rdGroupStats.put(i.getGroup(), new SimpleStats());
            }
            double dod = i.getDod();
            SimpleStats stats = new SimpleStats();
            for (TRSPRequest r : i.getRequests()) {
                double rd = r.getReleaseDate();
                if (rd > 0) {
                    stats.addValue(rd);
                    rdStats.addValue(rd / i.getMainDepot().getTimeWindow().endAsDouble());
                    rdGroupStats.get(i.getGroup()).addValue(
                            rd / i.getMainDepot().getTimeWindow().endAsDouble());
                }
            }
            avgLatest.get(i.getGroup()).addValue(stats.max());

            System.out.printf("%s;%s;%.0f;%.1f;%.1f;%.1f;%.4f;%.4f \n", i.getName(), i.getGroup(),
                    dod * i.getRequestCount(), stats.mean(), stats.min(), stats.max(),
                    stats.percentile(90) / i.getMainDepot().getTimeWindow().endAsDouble(),
                    stats.percentile(95) / i.getMainDepot().getTimeWindow().endAsDouble());
        }

        System.out.println("================================");
        System.out.println("Group   Median   Latest    90-p");
        ArrayList<String> keys = new ArrayList<>(avgLatest.keySet());
        Collections.sort(keys);
        for (String g : keys) {
            System.out.printf(" %3s : %7.1f %7.1f %7.1f \n", g, avgLatest.get(g).mean(), avgLatest
                    .get(g).median(), rdGroupStats.get(g).percentile(90) * 100);
        }
        System.out.println("================================");
        System.out.printf("mean:%7.1f median:%7.1f  90-percentile:%7.1f stdDev:%7.3f",
                rdStats.mean() * 100, rdStats.median() * 100, rdStats.percentile(90) * 100,
                rdStats.stdDev());
    }
}
