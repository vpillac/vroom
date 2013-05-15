package vroom.common.utilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StopwatchGUI extends JFrame implements ActionListener {

    private final Stopwatch mStopwatch;
    private final JLabel    mTime;

    public StopwatchGUI() {
        super();
        mStopwatch = new Stopwatch();

        getContentPane().setLayout(new BorderLayout());

        mTime = new JLabel();
        updateTimer();
        mTime.setFont(new Font("arial", Font.PLAIN, 18));
        mTime.setAlignmentY(Component.CENTER_ALIGNMENT);
        mTime.setAlignmentX(Component.CENTER_ALIGNMENT);
        getContentPane().add(mTime, BorderLayout.CENTER);

        JPanel controls = new JPanel();
        controls.setLayout(new FlowLayout());
        getContentPane().add(controls, BorderLayout.SOUTH);

        JButton button = new JButton("Start");
        button.setActionCommand("start");
        button.addActionListener(this);
        controls.add(button);

        button = new JButton("Pause");
        button.setActionCommand("pause");
        button.addActionListener(this);
        controls.add(button);

        button = new JButton("Resume");
        button.setActionCommand("resume");
        button.addActionListener(this);
        controls.add(button);

        button = new JButton("Stop");
        button.setActionCommand("stop");
        button.addActionListener(this);
        controls.add(button);

        button = new JButton("Reset");
        button.setActionCommand("reset");
        button.addActionListener(this);
        controls.add(button);

        button = new JButton("Read");
        button.setActionCommand("read");
        button.addActionListener(this);
        controls.add(button);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TimerDaemon daemon = new TimerDaemon();
        daemon.start();
    }

    public void updateTimer() {
        mTime.setText(mStopwatch.readTimeString(4, true, true));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // System.out.printf("%s - %s\n", mStopwatch.readTimeS(), e.getActionCommand());

        try {
            switch (e.getActionCommand()) {
            case "start":
                mStopwatch.start();
                break;
            case "pause":
                mStopwatch.pause();
                break;
            case "resume":
                mStopwatch.resume();
                break;
            case "stop":
                mStopwatch.stop();
                break;
            case "reset":
                mStopwatch.reset();
                break;
            case "read":
                System.out.println(mStopwatch.readTimeString(4, true, true));
                break;
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    public class TimerDaemon extends Thread {

        public TimerDaemon() {
            super("timer");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                updateTimer();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        };
    }

    public static void main(String[] args) {
        StopwatchGUI gui = new StopwatchGUI();
        gui.pack();
        gui.setVisible(true);
    }
}
