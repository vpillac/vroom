package vroom.optimization.online.jmsa.vrp.visu;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import vroom.common.modeling.dataModel.IVRPSolution;
import vroom.common.modeling.dataModel.attributes.RequestAttributeKey;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.MSACallbackBase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.events.ResourceEvent;
import vroom.optimization.online.jmsa.vrp.MSAVRPInstance;
import vroom.optimization.online.jmsa.vrp.VRPRequest;

public class MSAInfoPanel extends JPanel implements ComponentListener {

    private static final long serialVersionUID = 1L;

    private MSABase<?, ?>     mMSA;

    private JTextArea         mLogArea;

    /** javadoc **/
    private final double      mPI;

    private JSplitPane        mSplit;

    private JButton           mPause;

    private JButton           mResume;

    /**
     * Getter for javadoc
     * 
     * @return the value of perfect information
     */
    public double getPI() {
        return mPI;
    }

    /**
     * Create the panel.
     * 
     * @param msa
     */
    public MSAInfoPanel(MSABase<?, ?> msa, double pi) {
        mMSA = msa;
        mPI = pi;

        mMSA.registerCallback(EventTypes.EVENTS_RESOURCE, new UpdateCallback());

        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());

        // Log area
        mLogArea = new JTextArea();
        mLogArea.setEditable(false);
        // Scroll
        JScrollPane scrollLog = new JScrollPane(mLogArea);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log"));
        scrollLog.setAutoscrolls(true);

        // Info
        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setText(mMSA.getComponentsDescription());
        JScrollPane scrollInfo = new JScrollPane(info);
        scrollInfo.setBorder(BorderFactory.createTitledBorder("Info"));
        scrollInfo.setAutoscrolls(true);

        // Split Pane
        mSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, scrollLog, scrollInfo);
        mSplit.validate();
        mSplit.setDividerLocation(0.8);
        mSplit.setDividerSize(4);
        mSplit.addComponentListener(this);

        add(mSplit, BorderLayout.CENTER);

        // Pause / Resume
        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(1, 2));
        mPause = new JButton("Pause");
        mResume = new JButton("Resume");
        controls.add(mPause);
        controls.add(mResume);

        mPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MSAInfoPanel.this.pause();
            }
        });

        mResume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MSAInfoPanel.this.resume();
            }
        });

        mResume.setEnabled(false);

        // add(controls, BorderLayout.SOUTH);
    }

    protected void pause() {
        mMSA.pause();
        mResume.setEnabled(true);
        mPause.setEnabled(false);
    }

    protected void resume() {
        mMSA.resume();
        mResume.setEnabled(false);
        mPause.setEnabled(true);
    }

    public class UpdateCallback extends MSACallbackBase {

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public boolean isExecutedSynchronously() {
            return false;
        }

        @Override
        public void execute(MSACallbackEvent event) {
            if (event.getType() == EventTypes.EVENTS_RESOURCE) {
                ResourceEvent ev = (ResourceEvent) event.getParams()[0];
                switch (ev.getType()) {
                case START:
                    log(ev, "Started");
                    break;
                case STOP:
                    double cost = ((IVRPSolution<?>) mMSA.getCurrentSolution()).getCost();
                    log(ev, "Stopped");
                    log(ev, "Cost: %.3f", cost);
                    log(ev, "PI  : %.3f", getPI());
                    log(ev, "Gap : %.1f%s", 100 * (cost - mPI) / mPI, "%");
                    break;
                case REQUEST_ASSIGNED:
                    if (((VRPRequest) ev.getRequest()).isDepot()) {
                        log(ev, "A %s", ev.getRequest().getID());
                    } else {
                        log(ev,
                                "A  %s d:%s",
                                ev.getRequest().getID(),
                                ((VRPRequest) ev.getRequest()).getParentRequest().getAttribute(
                                        RequestAttributeKey.DEMAND));
                    }
                    break;
                case START_OF_SERVICE:
                    if (event.getParams().length == 3) {
                        // Failure
                        log(ev, "FAIL %s d:%s ex:%s", ev.getRequest().getID(),
                                Arrays.toString((double[]) ev.getAdditionalInformation()),
                                event.getParams()[2]);
                    } else {
                        log(ev, "SS %s d:%s", ev.getRequest().getID(),
                                Arrays.toString((double[]) ev.getAdditionalInformation()));
                    }
                    break;
                case END_OF_SERVICE:
                    log(ev, "ES %s", ev.getRequest().getID());
                    break;
                default:
                    log(ev, "Unknown event: %s", ev);
                    break;
                }
            }
        }

        private void log(ResourceEvent ev, String string, Object... params) {
            MSAVRPInstance instance = (MSAVRPInstance) mMSA.getInstance();

            double load = instance.getCurrentLoad(ev.getResourceId(), 0);
            double cap = instance.getFleet().getVehicle(ev.getResourceId()).getCapacity();
            Date now = new Date(System.currentTimeMillis());
            mLogArea.append(String.format("\n%1$tH:%1$tM (%2$s/%3$s): %4$s", now, load, cap,
                    String.format(string, params)));
            mLogArea.setCaretPosition(mLogArea.getDocument().getLength());
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        mSplit.setDividerLocation(0.8);
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        mSplit.setDividerLocation(0.8);
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    public void detach() {
        mMSA = null;
    }
}
