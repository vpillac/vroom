/**
 * 
 */
package vroom.optimization.online.jmsa.benchmarking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import vroom.common.utilities.callbacks.CallbackEventBase;
import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.utils.MSALogging;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;

public class ResourceCallback extends MSACallbackBase {

    private final BufferedWriter writer;

    public ResourceCallback(String file) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
    }

    @Override
    public void execute(MSACallbackEvent event) {

        if (event.getParams() == null || event.getParams().length < 2) {
            MSALogging.getBaseLogger().warn("ResourceCallback.execute: invalid argument - %s",
                    Arrays.asList(event.getParams()));
        } else if (event.getParams()[0] == null) {
            MSALogging.getBaseLogger().warn("ResourceCallback.execute: invalid argument - {%s,%s}",
                    event.getParams());
        } else {
            ResourceEvent ev = (ResourceEvent) event.getParams()[0];
            switch (ev.getType()) {
            case START:
                log(ev, "Started");
                break;
            case STOP:
                log(ev, "Stopped");
                break;
            case REQUEST_ASSIGNED:
                log(ev, "Assigned to request\t%s", ev.getRequest());
                break;
            case START_OF_SERVICE:
                if (event.getParams().length == 3) {
                    // Failure
                    log(ev, "Route failure at \t%s\t Actual demands:%s Exceeding capacity: %s",
                            ev.getRequest(),
                            Arrays.toString((double[]) ev.getAdditionalInformation()),
                            event.getParams()[2]);
                } else {
                    log(ev, "Start servicing of \t%s\t Actual demands:%s", ev.getRequest(),
                            Arrays.toString((double[]) ev.getAdditionalInformation()));
                }
                break;
            case END_OF_SERVICE:
                log(ev, "Served request     \t%s", ev.getRequest());
                break;
            default:
                log(ev, "Unknown resource event: %s", ev);
                break;
            }
            try {
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                MSALogging.getBaseLogger().warn(
                        "Exception caught in method RequestServedCallback.execute", e);
            }
        }
    }

    protected void log(ResourceEvent e, String format, Object... arguments) {
        int id = e.getResourceId();
        try {
            writer.append(String.format("[%s] V%s(%s/%s)\t %s",
                    CallbackEventBase.getTimeStampString(e.getTimeStamp()), id, ((MSAVRPInstance) e
                            .getSource().getParentMSA().getInstance()).getCurrentLoad(id, 0),
                    ((MSAVRPInstance) e.getSource().getParentMSA().getInstance()).getFleet()
                            .getVehicle(id).getCapacity(), String.format(format, arguments)));
        } catch (IOException e1) {
            MSALogging.getBaseLogger().warn(
                    "Exception caught in method RequestServedCallback.execute", e1);
        }
    }

    @Override
    public int getPriority() {
        return 10;
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