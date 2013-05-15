package vroom.optimization.online.jmsa.vrp.visu;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class MSAVisualizationPanel extends JPanel implements ComponentListener {

    private static final long           serialVersionUID = 1L;

    private final DynamicInstanceViewer mViewer;

    public MSAVisualizationPanel(DynamicInstanceViewer viewer) {
        super();
        mViewer = viewer;

        setBorder(BorderFactory.createTitledBorder("Real time routing"));
        setLayout(new GridLayout(1, 1));
        add(mViewer);
        mViewer.setBackground(Color.white);

        addComponentListener(this);
    }

    @Override
    public void componentResized(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    public void detach() {
        mViewer.detach();
    }

}
