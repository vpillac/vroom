package vroom.trsp.bench.gui;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import vroom.common.utilities.callbacks.ICallback;
import vroom.common.utilities.callbacks.ICallbackEvent;
import vroom.optimization.online.jmsa.IDistinguishedSolution;
import vroom.optimization.online.jmsa.MSABase;
import vroom.optimization.online.jmsa.events.MSACallbackEvent.EventTypes;
import vroom.optimization.online.jmsa.utils.MSASimulator;
import vroom.optimization.online.jmsa.utils.MSASimulator.ResourceState;
import vroom.optimization.online.jmsa.utils.MSASimulator.ResourceStates;
import vroom.trsp.datamodel.ITRSPNode;
import vroom.trsp.datamodel.TRSPInstance;
import vroom.trsp.datamodel.TRSPSolution;
import vroom.trsp.datamodel.TRSPTour;
import vroom.trsp.datamodel.Technician;

public class TechnicianTableModel extends AbstractTableModel implements ICallback<MSABase<?, ?>, EventTypes>, Observer {

    private final static int   DIST_SOL_COL     = 5;

    private static final long  serialVersionUID = 1L;

    // Columns:
    // Name - State - Current - Served - Assigned - Distinguished solution

    private final TRSPInstance mInstance;
    private final MSASimulator mSimulator;

    public TechnicianTableModel(TRSPInstance instance, MSASimulator simulator) {
        super();
        mInstance = instance;
        mSimulator = simulator;

        mSimulator.getMSA().registerCallback(EventTypes.MSA_NEW_DISTINGUISHED_SOLUTION, this);
        for (Technician t : instance.getFleet())
            simulator.getState(t.getID()).addObserver(this);
    }

    @Override
    public int getRowCount() {
        return mInstance.getFleet().size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int techId, int columnIndex) {
        switch (columnIndex) {
        case 0:// Name
            return mInstance.getFleet().getVehicle(techId).getName();
        case 1:// State
            return mSimulator.getState(techId).getState();
        case 2:// Current
            return mInstance.getSimulator().getCurrentNode(techId);
        case 3:// Assigned
            return mInstance.getSimulator().getAssignedNode(techId);
        case 4:// Served
            return mSimulator.getState(techId).getServedRequests();
        case DIST_SOL_COL:// Distinguished solution
            IDistinguishedSolution sol = mSimulator.getMSA().getDistinguishedSolution();
            return sol != null ? ((TRSPSolution) sol).getTour(techId) : null;
        default:
            return "NA";
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:// Name
            return "Name";
        case 1:// State
            return "State";
        case 2:// Current
            return "Current";
        case 3:// Assigned
            return "Assigned";
        case 4:// Served
            return "Served";
        case DIST_SOL_COL:// Distinguished solution
            return "Dist. Sol.";
        default:
            return "NA";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case 0:// Name
            return String.class;
        case 1:// State
            return ResourceStates.class;
        case 2:// Current
            return ITRSPNode.class;
        case 3:// Assigned
            return ITRSPNode.class;
        case 4:// Served
            return List.class;
        case DIST_SOL_COL:// Distinguished solution
            return TRSPTour.class;
        default:
            return Object.class;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        ResourceState state = (ResourceState) o;
        fireTableRowsUpdated(state.getResourceId(), state.getResourceId());
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
        for (int row = 0; row < getRowCount(); row++)
            fireTableCellUpdated(row, DIST_SOL_COL);
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
