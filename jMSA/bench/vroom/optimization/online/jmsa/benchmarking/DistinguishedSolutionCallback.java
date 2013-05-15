/**
 * 
 */
package vroom.optimization.online.jmsa.benchmarking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.utils.MSALogging;

public class DistinguishedSolutionCallback extends MSACallbackBase {

    private final BufferedWriter writer;

    public DistinguishedSolutionCallback(String file) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
    }

    @Override
    public void execute(MSACallbackEvent event) {
        String m = "";

        if (event.getParams() != null && event.getParams()[1] != null) {
            m = String.format("[%s] New Distinguished Solution : %s", event.getTimeStampString(),
                    event.getParams()[1]);
        } else {
            m = String.format("[%s] New Distinguished Solution Event - Params: %s",
                    event.getTimeStampString(), Arrays.toString(event.getParams()));
        }

        try {
            writer.append(m);
            writer.append('\n');
            writer.flush();
        } catch (IOException e) {
            MSALogging.getBaseLogger().warn(
                    "Execption caught in method DistinguishedSolutionCallback.execute", e);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isExecutedSynchronously() {
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        writer.flush();
        writer.close();
        super.finalize();
    }

}