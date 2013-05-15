package vroom.trsp.bench.gui;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.trsp.bench.mpa.DTRSPRunMPA;
import vroom.trsp.optimization.mpa.DTRSPSolution;
import vroom.trsp.sim.TRSPSimulator;
import vroom.trsp.sim.TRSPSimulator.UpdateNotification;
import vroom.trsp.util.TRSPLogging;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class StatePanel extends JPanel implements Observer, ICallback<MSABase<?, ?>, EventTypes> {
    private final JTextField mCurrentStateField;
    private final JTextField mReleasedField;
    private final JTextField mRejectedField;
    private final JTextField mUnreleasedField;
    private final JTextField mServedField;
    private final JTextField mAssignedField;
    private final JTextField mUnservedField;
    private final JTextField mCurTimeField;
    private final JTextField mSimTimeField;
    private final JTextField mScenPoolSize;
    private final JTextField mCurrentEventField;
    private final JTextField mAvgCost;
    private final JTextField mCurrentSolCost;
    private final JTextField mDistinguishedSolCost;

    private DTRSPRunMPA      mRun;

    private TimeDameon       mDaemon;

    /**
     * Create the panel.
     */
    public StatePanel() {
        setBorder(BorderFactory.createTitledBorder("Current state"));

        int rowCount = 16;
        RowSpec[] specs = new RowSpec[rowCount * 2];
        for (int i = 0; i < rowCount; i++) {
            specs[2 * i] = FormFactory.RELATED_GAP_ROWSPEC;
            specs[2 * i + 1] = FormFactory.DEFAULT_ROWSPEC;
        }
        setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"), }, specs));

        int row = 2;
        JLabel lblCurrentState = new JLabel("Current State:");
        add(lblCurrentState, "2, " + row + ", right, default");

        mCurrentStateField = new JTextField();
        mCurrentStateField.setText("UNKNOWN");
        mCurrentStateField.setEditable(false);
        add(mCurrentStateField, "4, " + row + ", fill, default");
        mCurrentStateField.setColumns(10);
        row += 2;

        JLabel lblCurrentEvent = new JLabel("Current Event:");
        add(lblCurrentEvent, "2, " + row + ", right, default");

        mCurrentEventField = new JTextField();
        mCurrentEventField.setText("UNKNOWN");
        mCurrentEventField.setEditable(false);
        add(mCurrentEventField, "4, " + row + ", fill, default");
        mCurrentEventField.setColumns(10);
        row += 2;

        JLabel lblReleasedRequests = new JLabel("Released Requests:");
        add(lblReleasedRequests, "2, " + row + ", right, default");

        mReleasedField = new JTextField();
        mReleasedField.setText("[]");
        mReleasedField.setEditable(false);
        add(mReleasedField, "4, " + row + ", fill, default");
        mReleasedField.setColumns(10);
        row += 2;

        JLabel lblUnreleasedRequests = new JLabel("Unreleased Requests:");
        add(lblUnreleasedRequests, "2, " + row + ", right, default");

        mUnreleasedField = new JTextField();
        mUnreleasedField.setEditable(false);
        mUnreleasedField.setText("[]");
        add(mUnreleasedField, "4, " + row + ", fill, default");
        mUnreleasedField.setColumns(10);
        row += 2;

        JLabel lblServedRequests = new JLabel("Served Requests");
        add(lblServedRequests, "2, " + row + ", right, default");

        mServedField = new JTextField();
        mServedField.setEditable(false);
        mServedField.setText("[]");
        add(mServedField, "4, " + row + ", fill, default");
        mServedField.setColumns(10);
        row += 2;

        JLabel lblAssignedRequests = new JLabel("Assigned Requests:");
        add(lblAssignedRequests, "2, " + row + ", right, default");

        mAssignedField = new JTextField();
        mAssignedField.setEditable(false);
        mAssignedField.setText("[]");
        add(mAssignedField, "4, " + row + ", fill, default");
        mAssignedField.setColumns(10);
        row += 2;

        JLabel lblUnservedRequests = new JLabel("Unserved Requests:");
        add(lblUnservedRequests, "2, " + row + ", right, default");

        mUnservedField = new JTextField();
        mUnservedField.setEditable(false);
        mUnservedField.setText("[]");
        add(mUnservedField, "4, " + row + ", fill, default");
        mUnservedField.setColumns(10);
        row += 2;

        JLabel lblRejectedRequests = new JLabel("Rejected Requests:");
        add(lblRejectedRequests, "2, " + row + ", right, default");

        mRejectedField = new JTextField();
        mRejectedField.setEditable(false);
        mRejectedField.setText("[]");
        add(mRejectedField, "4, " + row + ", fill, default");
        mRejectedField.setColumns(10);
        row += 2;

        // ----------------------------------------------
        JSeparator separator = new JSeparator();
        add(separator, "2, " + row + ", 3, 1");
        row += 2;
        // ----------------------------------------------

        JLabel lblCurrentTime = new JLabel("Wall Time:");
        add(lblCurrentTime, "2, " + row + ", right, default");

        mCurTimeField = new JTextField();
        mCurTimeField.setEditable(false);
        add(mCurTimeField, "4, " + row + ", fill, default");
        mCurTimeField.setColumns(10);
        row += 2;

        JLabel lblSimulationTime = new JLabel("Simulation Time:");
        add(lblSimulationTime, "2, " + row + ", right, default");

        mSimTimeField = new JTextField();
        mSimTimeField.setEditable(false);
        add(mSimTimeField, "4, " + row + ", fill, default");
        mSimTimeField.setColumns(10);
        row += 2;

        JLabel lblTRSPSimulationTime = new JLabel("Num scenarios:");
        add(lblTRSPSimulationTime, "2, " + row + ", right, default");

        mScenPoolSize = new JTextField();
        mScenPoolSize.setEditable(false);
        add(mScenPoolSize, "4, " + row + ", fill, default");
        mScenPoolSize.setColumns(10);
        row += 2;

        // ----------------------------------------------
        // separator = new JSeparator();
        add(separator, "2, " + row + ", 3, 1");
        row += 2;
        // ----------------------------------------------

        JLabel lblCurCost = new JLabel("Current cost:");
        add(lblCurCost, "2, " + row + ", right, default");

        mCurrentSolCost = new JTextField();
        mCurrentSolCost.setEditable(false);
        add(mCurrentSolCost, "4, " + row + ", fill, default");
        mCurrentSolCost.setColumns(10);
        row += 2;

        JLabel label = new JLabel("Distinguished cost:");
        add(label, "2, " + row + ", right, default");

        mDistinguishedSolCost = new JTextField();
        mDistinguishedSolCost.setEditable(false);
        add(mDistinguishedSolCost, "4, " + row + ", fill, default");
        mDistinguishedSolCost.setColumns(10);
        row += 2;

        JLabel lblAvgCost = new JLabel("Average cost:");
        add(lblAvgCost, "2, " + row + ", right, default");

        mAvgCost = new JTextField();
        mAvgCost.setEditable(false);
        add(mAvgCost, "4, " + row + ", fill, default");
        mAvgCost.setColumns(10);
        row += 2;

    }

    public void initialize(DTRSPRunMPA run) {
        mRun = run;
        run.getSimulator().addObserver(this);
        updateAssigned();
        updateRejected();
        updateReleased();
        updateServed();
        mDaemon = new TimeDameon();
        mDaemon.start();
        mRun.getMPA().registerCallback(EventTypes.MSA_NEW_EVENT, this);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mDaemon.stop = true;
    }

    @Override
    public void update(Observable o, Object arg) {
        UpdateNotification u = (TRSPSimulator.UpdateNotification) arg;

        switch (u.type) {
        case TIME_CHANGED:
            updateReleased();
            break;
        case REQ_REJECTED:
            updateRejected();
            break;
        case REQ_RELEASED:
            updateReleased();
            break;
        case NODE_ASSIGNED:
            updateAssigned();
            break;
        case NODE_SERVED:
            updateServed();
            break;
        case NODE_SET_AS_CURRENT:
            break;
        default:
            TRSPLogging.getSimulationLogger().warn("StatePanel.update: unknown update type:%s",
                    u.type);
            break;
        }
    }

    private String formatList(Collection<?> list) {
        return String.format("%-3s %s", list.size(), Utilities.toShortString(list));
    }

    private void updateServed() {
        mServedField.setText(formatList(mRun.getInstance().getServedRequests()));
        mUnservedField.setText(formatList(mRun.getInstance().getUnservedReleasedRequests()));
    }

    private void updateAssigned() {
        mAssignedField.setText(formatList(mRun.getInstance().getSimulator().getAssignedNodes()));
    }

    private void updateReleased() {
        // mReleasedField.setText(Utilities.toShortString(mRun.getInstance().getReleasedRequests()));
        mReleasedField.setText(formatList(mRun.getInstance().getReleasedRequests()));
        mUnreleasedField.setText(formatList(mRun.getInstance().getSimulator()
                .getUnreleasedRequests()));
    }

    private void updateRejected() {
        mRejectedField.setText(formatList(mRun.getSimulator().getRejectedRequests()));
    }

    private class TimeDameon extends Thread {
        private boolean stop = false;

        public TimeDameon() {
            super("state-panel-daemon");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!stop && !mRun.isTerminated()) {
                try {
                    mCurTimeField.setText(Utilities.Time.millisecondsToString((long) (mRun
                            .getMSASimulator().wallTime() * 1000), 3, true, false));
                    mSimTimeField.setText(String.format("%-7.2f", mRun.getMSASimulator()
                            .simulationTime()));
                    mScenPoolSize.setText(String.format("%s", mRun.getSolver().getMPA().getProxy()
                            .getScenarioPool().size()));
                    mCurrentStateField.setText(mRun.getSolver().getMPA().getStatus().toString());

                    if (mRun.getSimulator().getCurrentSolution() != null)
                        mCurrentSolCost.setText(String.format("%-7.2f", mRun.getSimulator()
                                .getCurrentSolution().getObjectiveValue()));
                    if (mRun.getMPA().getDistinguishedSolution() != null)
                        mDistinguishedSolCost.setText(String.format("%-7.2f", ((DTRSPSolution) mRun
                                .getMPA().getDistinguishedSolution()).getObjectiveValue()));

                    double avg = 0;
                    int count = 0;
                    for (DTRSPSolution s : mRun.getMPA().getProxy().getScenarioPool()) {
                        avg += s.getObjectiveValue();
                        count++;
                    }
                    avg /= count;
                    mAvgCost.setText(String.format("%-7.2f", avg));

                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    TRSPLogging.getSimulationLogger().exception("TimeDameon.run", e);
                    stop = true;
                } catch (Exception e) {
                    TRSPLogging.getSimulationLogger().exception("TimeDameon.run", e);
                }
            }
        }
    }

    @Override
    public int compareTo(ICallback<?, ?> o) {
        return Integer.compare(this.getPriority(), o.getPriority());
    }

    @Override
    public void execute(ICallbackEvent<MSABase<?, ?>, EventTypes> event) {
        mCurrentEventField.setText(event.getParams()[0] != null ? event.getParams()[0].toString()
                : "[none]");
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean isExecutedSynchronously() {
        return false;
    }
}
