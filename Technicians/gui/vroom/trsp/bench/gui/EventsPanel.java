/**
 * 
 */
package vroom.trsp.bench.gui;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import vroom.common.utilities.Utilities;
import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.common.utilities.gui.ColumnsAutoSizer;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.GenerateEvent;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.events.MSAEvent;
import vroom.optimization.online.jmsa.events.OptimizeEvent;
import vroom.trsp.bench.mpa.DTRSPRunMPA;

/**
 * <code>EventsPanel</code>
 * <p>
 * Creation date: Mar 19, 2012 - 1:54:51 PM
 * 
 * @author Victor Pillac, <a href="http://uniandes.edu.co">Universidad de Los Andes</a>-<a
 *         href="http://copa.uniandes.edu.co">Copa</a> <a href="http://www.emn.fr">Ecole des Mines de Nantes</a>-<a
 *         href="http://www.irccyn.ec-nantes.fr/irccyn/d/en/equipes/Slp">SLP</a>
 * @version 1.0
 */
public class EventsPanel extends JPanel implements ICallback<MSABase<?, ?>, EventTypes> {

    private static final long       serialVersionUID = 1L;

    private final JTable            mTable;

    private final DefaultTableModel mModel;

    public EventsPanel() {
        super();
        setBorder(BorderFactory.createTitledBorder("MPA event log"));

        mModel = new DefaultTableModel(new Object[0][3], new Object[] { "Time", "Type", "Details" });
        mTable = new JTable(mModel);
        mTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                if (Date.class.isInstance(value)) {
                    super.setValue(Utilities.Time.TIME_STAMP_FORMAT.format(value));
                } else {
                    super.setValue(value);
                }
            }
        });

        // ColumnsAutoSizer.addAutoSizer(mTable);
        mTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ColumnsAutoSizer.setWidthFromPrototype(mTable, new Object[] { "24h00m00s", "RessourceEvent", null }, true);

        setLayout(new GridLayout(1, 1));

        final JScrollPane scrollPane = new JScrollPane(mTable);
        mTable.setFillsViewportHeight(true);

        mTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.INSERT) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final Rectangle lastCell = mTable.getCellRect(mTable.getRowCount() - 1,
                                    mTable.getColumnCount() - 1, true);
                            scrollPane.getViewport().scrollRectToVisible(lastCell);
                        }
                    });
                }
            }
        });

        add(scrollPane);

    }

    public void initialize(DTRSPRunMPA run) {
        run.getMPA().registerCallback(EventTypes.MSA_NEW_EVENT, this);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int compareTo(ICallback<?, ?> o) {
        return o.getPriority() - getPriority();
    }

    @Override
    public void execute(ICallbackEvent<MSABase<?, ?>, EventTypes> event) {
        MSAEvent e = (MSAEvent) event.getParams()[0];

        if (OptimizeEvent.class.isAssignableFrom(e.getClass()) || GenerateEvent.class.isAssignableFrom(e.getClass()))
            return;

        mModel.addRow(new Object[] { Utilities.Time.secondsToString(e.getSimulationTimeStamp(), 3, false, false),
                e.getClass().getSimpleName(), e.toShortString() });
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean isExecutedSynchronously() {
        return false;
    }

}
