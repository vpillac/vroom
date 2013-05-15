package vroom.trsp.bench;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import vroom.common.modeling.dataModel.attributes.ReleaseDate;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.utilities.FileBufferedWriter;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.io.ITRSPPersistenceHelper;
import vroom.trsp.optimization.TRSPUtilities;
import vroom.trsp.util.TRSPGlobalParameters;

public class OfflineSolver {

    public static final String INSTANCE_FOLDER = "../Instances/trsp/pillac/crew25";

    public static String       sStatFile       = "./results/trsp_bench_111112_19-13_DTRSP_all.csv";

    /**
     * JAVADOC
     * 
     * @param args
     */
    public static void main(String[] args) {

        ITRSPPersistenceHelper instanceReader = TRSPUtilities.getPersistenceHelper(INSTANCE_FOLDER);
        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_FATAL, LoggerHelper.LEVEL_FATAL, false);
        TRSPGlobalParameters params = new TRSPGlobalParameters();
        try {
            params.loadParameters(new File("./config/bench/bench_dtrsp_alns_25crew.cfg"));
        } catch (Exception e) {
            e.printStackTrace();
            Logging.awaitLogging(60000);
            System.exit(1);
        }
        BufferedReader reader = null;
        try {
            FileBufferedWriter writer = new FileBufferedWriter("offline.csv");
            write(writer, "name;comment;rejected_count;rejected;offline_obj;offline_size\n");

            reader = new BufferedReader(new FileReader(new File(sStatFile)));

            String line = reader.readLine();
            while (line != null && !line.startsWith("name;group")) {
                line = reader.readLine();
            }
            line = reader.readLine();

            while (line != null) {
                String[] stats = line.split(";");
                int rejectedCount = Integer.valueOf(stats[9]);
                TRSPInstance instance = instanceReader.readInstance(new File(INSTANCE_FOLDER + "/"
                        + stats[0] + ".txt"), false);
                instance.setDod(0);
                for (TRSPRequest r : instance.getRequests())
                    r.setAttribute(RequestAttributeKey.RELEASE_DATE, new ReleaseDate(-1));
                int[] rejected = new int[0];
                if (rejectedCount > 0) {
                    rejected = Utilities.toIntArray(stats[10]);
                    for (int r : rejected) {
                        instance.getRequest(r).setAttribute(RequestAttributeKey.RELEASE_DATE,
                                new ReleaseDate(Double.POSITIVE_INFINITY));
                    }
                }
                write(writer, "%s;%s;%s;%s;", stats[0], stats[5], rejectedCount,
                        Utilities.toShortString(rejected));
                TRSPRunBase run = new TRSPRunBase(null, instance, params, null, 0);
                TRSPSolution sol = run.call(true);

                write(writer, "%s;%s\n", sol.getObjectiveValue(), instance.getReleasedRequests()
                        .size());
                line = reader.readLine();
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write(FileBufferedWriter w, String format, Object... args) {
        System.out.printf(format, args);
        try {
            w.write(format, args);
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
