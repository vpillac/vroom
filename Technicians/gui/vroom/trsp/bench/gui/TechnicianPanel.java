/**
 * 
 */
package vroom.trsp.bench.gui;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.gui.ColumnsAutoSizer;
import vroom.trsp.bench.mpa.DTRSPRunMPA;
import vroom.trsp.datamodel.ITRSPNode;
import vroom.trsp.datamodel.TRSPTour;

/**
 * <code>TechnicianPanel</code>
 * <p>
 * Creation date: Mar 16, 2012 - 3:28:22 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class TechnicianPanel extends JPanel {

    private final JTable      mTable;

    private static final long serialVersionUID = 1L;

    public TechnicianPanel() {
        super();
        setBorder(BorderFactory.createTitledBorder("Technician state"));

        mTable = new JTable();
        mTable.setDefaultRenderer(List.class, new ListRenderer());
        mTable.setDefaultRenderer(TRSPTour.class, new TourRenderer());
        mTable.setDefaultRenderer(ITRSPNode.class, new NodeRenderer());
        setLayout(new GridLayout(1, 1));

        JScrollPane scrollPane = new JScrollPane(mTable);
        mTable.setFillsViewportHeight(true);

        add(scrollPane);

    }

    public void initialize(DTRSPRunMPA run) {
        mTable.setModel(new TechnicianTableModel(run.getInstance(), run.getMSASimulator()));

        mTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ColumnsAutoSizer.setWidthFromPrototype(mTable, new Object[] { "tech-99", "STOPPED", 999,
                999, null, null }, true);
    }

    public void updateStats() {
    }

    public static class ListRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        protected void setValue(Object value) {
            super.setText(value == null ? "null" : Utilities.toShortString((Iterable<?>) value));
        }
    }

    public static class TourRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        protected void setValue(Object value) {
            super.setText(value == null ? "null" : ((TRSPTour) value).toShortString());
        }
    }

    public static class NodeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        protected void setValue(Object value) {
            super.setText(value == null ? "null" : ((ITRSPNode) value).toShortString());
        }
    }
}
