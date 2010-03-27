package org.lf.ui.components.plugins.scrollablelogtable;

import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.lf.logs.Cell;
import org.lf.logs.Log;
import org.lf.logs.Record;

class LogTableModel extends AbstractTableModel implements Observer {
    private ScrollableLogViewModel underlyingModel;
    private String[] fields;
    public LogTableModel(ScrollableLogViewModel underlyingModel) {
        this.underlyingModel = underlyingModel;
        underlyingModel.addObserver(this);
    }

    @Override
    public int getColumnCount() {
        return underlyingModel.getRecordSize();
    }

    @Override
    public int getRowCount() {
        return underlyingModel.getRecordCount();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Cell[] cells = underlyingModel.getRecord(row).getCells();
        if (underlyingModel.getRecordCount() > row && cells.length > col){
            return cells[col].getValue();
        }
        return null;
    }

    @Override
    public void update(Observable obj, final Object message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if ("CHANGE_RECORD_SIZE".equals(message))
                    fireTableStructureChanged();
                else if ("ADD_END".equals(message)) {
                    fireTableRowsDeleted(0, 0);
                    fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
                } else if ("ADD_BEGIN".equals(message)) {
                    fireTableRowsInserted(0,0);
                    fireTableRowsDeleted(getRowCount()-1, getRowCount()-1);
                } else
                    fireTableDataChanged();
            }
        });
    }

    @Override
    public Class<?> getColumnClass(int arg0) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        if (fields == null) {
            Log log = underlyingModel.getLog();
            fields = log.getMetadata().getFieldNames();
        }
        return fields[column];
    }

}