package org.lf.ui.components.plugins.scrollablelogtable;

import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.lf.formatterlog.FormattedLog;
import org.lf.logs.Field;

class LogTableModel extends AbstractTableModel implements Observer {
    private ScrollableLogViewModel underlyingModel;
    private Field[] fields;
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
        if (row < underlyingModel.getRecordCount()) {
        	Object[] cells = underlyingModel.getRecord(row).getCellValues();
        	if (col < cells.length) {
        		return cells[col];
        	}
        }
        return null;
    }

    @Override
    public void update(Observable obj, final Object message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if ("ADD_TO_END".equals(message)) {
                    fireTableRowsDeleted(0, 0);
                    fireTableRowsInserted(getRowCount()-1, getRowCount()-1);
                } else if ("ADD_TO_BEGIN".equals(message)) {
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
            FormattedLog log = underlyingModel.getLog();
            fields = log.getFields();
        }
        return fields[column].name;
    }

}