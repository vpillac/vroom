/**
 * 
 */
package vroom.trsp.bench.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import vroom.common.utilities.Utilities;
import vroom.trsp.bench.mpa.DTRSPRunMPA;
import vroom.trsp.util.TRSPLogging;

/**
 * <code>TRSPSimFrame</code> is a simple GUI to visualize the DTRP simulation
 * <p>
 * Creation date: Mar 16, 2012 - 3:27:28 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TRSPSimFrame extends JFrame {

    private DTRSPRunMPA           mRun;

    private final TechnicianPanel mTechPanel;
    private final EventsPanel     mEventsPanel;
    private final StatePanel      mStatePanel;

    public DTRSPRunMPA getRun() {
        return mRun;
    }

    /**
     * JAVADOC
     */
    private static final long serialVersionUID = 1L;

    public TRSPSimFrame() throws HeadlessException {
        super("DTRSP Simulation - " + Utilities.Time.getVMStartDateString());

        JPanel panel = new JPanel();
        setContentPane(panel);
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.NONE;

        mStatePanel = new StatePanel();
        getContentPane().add(mStatePanel, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 0.5;

        c.gridx++;
        mTechPanel = new TechnicianPanel();
        getContentPane().add(mTechPanel, c);

        c.gridx++;
        mEventsPanel = new EventsPanel();
        getContentPane().add(mEventsPanel, c);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();
        mTechPanel.setMaximumSize(mTechPanel.getPreferredSize());
    }

    public void initialize(DTRSPRunMPA run) {
        mRun = run;
        mStatePanel.initialize(getRun());
        mEventsPanel.initialize(getRun());
        mTechPanel.initialize(getRun());
    }

    public void startUpdateDaemon() {
        SimFrameUpdateDaemon daemon = new SimFrameUpdateDaemon();
        daemon.start();
    }

    private class SimFrameUpdateDaemon extends Thread {

        private SimFrameUpdateDaemon() {
            super("frame-update-daemon");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!getRun().isTerminated() && getRun().getMPA().isRunning()) {
                mTechPanel.updateStats();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    TRSPLogging.getRunLogger().exception("SimFrameUpdateDaemon.run", e);
                }
            }
        }

    }

}
