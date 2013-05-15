package vroom.trsp.bench;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import vroom.common.modeling.dataModel.attributes.ReleaseDate;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.common.utilities.FileBufferedWriter;
import vroom.common.utilities.IntegerSet;
import vroom.common.utilities.Utilities;
import vroom.common.utilities.logging.LoggerHelper;
import vroom.common.utilities.logging.Logging;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPRequest;
import vroom.trsp.optimization.alns.RepairRegret;
import vroom.trsp.optimization.alns.TRSPDestroyResult;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.sim.TRSPSimulator;
import vroom.trsp.util.TRSPGlobalParameters;

public class DTRSPBenchRegret {

    /**
     * @param args
     */
    public static void main(String[] args) {
        final int regretLevel = 3;

        LoggerHelper.setupRootLogger(LoggerHelper.LEVEL_FATAL, LoggerHelper.LEVEL_FATAL, false);
        TRSPGlobalParameters params = new TRSPGlobalParameters();
        params.set(TRSPGlobalParameters.ALNS_MAX_IT, 10);
        try {
            params.loadParameters(new File(args[0]));
        } catch (Exception e) {
            e.printStackTrace();
            Logging.awaitLogging(60000);
            System.exit(1);
        }

        TRSPBench.setup(params, true, "");
        TRSPBench.getInstance().createRuns();

        List<TRSPRunBase> runs = TRSPBench.getInstance().getRuns();

        FileBufferedWriter writer = null;
        try {
            writer = new FileBufferedWriter(String.format(
                    "results/dtrsp/trsp_bench_%s_dtrsp_regret-%s.csv",
                    Utilities.Time.getDateString(), regretLevel));
        } catch (IOException e) {
            e.printStackTrace();
            Logging.awaitLogging(60000);
            System.exit(1);
        }

        write(writer,
                "instance;comment;regret_cost;rejected_count;rejected;static_cost;vi_cost;static_accepted\n");
        Iterator<TRSPRunBase> it = runs.iterator();
        while (it.hasNext()) {
            TRSPRunBase run = it.next();
            try {
                run.initialize();
                // if (!run.getInstance().getName().startsWith("R"))
                // continue;
                write(writer, "%s;%s;", run.getInstance().getName(), run.getComment());

                DTRSPSolution sol = (DTRSPSolution) run.call(true);

                TRSPSimulator sim = run.getSimulator();
                TRSPInstance ins = run.getInstance();
                RepairRegret regret = new RepairRegret(params, run.getSolver().getTourCtrHandler(),
                        regretLevel, false);

                while (sim.hasUnreleasedRequests()) {
                    // Get the released request(s)
                    Collection<TRSPRequest> release = sim.nextRelease();
                    sim.updateState(sol);

                    IntegerSet rel = new IntegerSet(ins.getMaxId());
                    // Attempts to insert new requests
                    for (TRSPRequest r : release) {
                        rel.add(r.getID());
                        // Add new requests
                        sol.markAsUnserved(r.getID());
                    }
                    // Repair the solution (Best insertion)
                    regret.repair(sol, new TRSPDestroyResult(rel), null);
                    // Mark the unserved requests as rejected
                    for (Integer r : sol.getUnservedRequests())
                        sol.getInstance().getSimulator().markAsRejected(r);
                    // Clear the unserved request set
                    sol.markAllAsServed();
                }

                double rcost = sol.getObjectiveValue();

                run.setFinalSolution(sol);
                // Solve the instance without the rejected requests (if necessary)
                double scost = 0;
                // if (!rejected.isEmpty()) {
                for (TRSPRequest r : ins.getRequests())
                    r.setAttribute(RequestAttributeKey.RELEASE_DATE, new ReleaseDate(-1d));

                run.aPosterioriRun();
                scost = run.getStaticSolution().getObjectiveValue();
                // }

                write(writer, "%s;%s;%s;%s;%s;%s\n", rcost, sol.getInstance().getSimulator()
                        .getRejectedRequests().size(), sol.getInstance().getSimulator()
                        .getRejectedRequests(), scost, (rcost - scost) / scost, ins
                        .getReleasedRequests().size());
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    writer.newLine();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            run.dispose();
            it.remove();
            System.gc();

        }

        System.out.println("FINISHED");
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
