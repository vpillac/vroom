package vroom.common.modeling.visualization;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * <code>VisualizationFrame</code> is an extension of {@link JFrame} containing a {@link DefaultInstanceViewer}.
 * <p>
 * Creation date: Mar 24, 2011 - 1:26:47 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class VisualizationFrame extends JFrame {

    private static final long     serialVersionUID = 1L;

    /** the {@link DefaultInstanceViewer} contained in this frame **/
    private DefaultInstanceViewer mInstanceViewer;

    /**
     * Getter for the {@link DefaultInstanceViewer} contained in this frame
     * 
     * @return the value of instanceViewer
     */
    public DefaultInstanceViewer getInstanceViewer() {
        return this.mInstanceViewer;
    }

    /**
     * Setter for the {@link DefaultInstanceViewer} contained in this frame
     * 
     * @param instanceViewer
     *            the value to be set for the {@link DefaultInstanceViewer} contained in this frame
     */
    public void setInstanceViewer(DefaultInstanceViewer instanceViewer) {
        this.mInstanceViewer = instanceViewer;
        getContentPane().add(instanceViewer, BorderLayout.CENTER);
    }

    @Override
    public JPanel getContentPane() {
        return (JPanel) super.getContentPane();
    }

    public VisualizationFrame(String title, DefaultInstanceViewer viewer) {
        super(title);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        setContentPane(panel);
        setInstanceViewer(viewer);
    }

}
