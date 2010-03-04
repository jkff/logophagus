package org.lf.ui.components.plugins.scrollablelogtable;

import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

class LogTableModel extends AbstractTableModel implements Observer {
	private ScrollableLogViewModel underlyingModel;
	public LogTableModel(ScrollableLogViewModel underlyingModel) {
		this.underlyingModel = underlyingModel;
		underlyingModel.addObserver(this);
	}

	@Override
	public int getColumnCount() {
		return underlyingModel.getMaxRecordSize();
	}

	@Override
	public int getRowCount() {
		return underlyingModel.getRecordCount();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (underlyingModel.getRecordCount() > row && underlyingModel.getRecord(row).size() > col){
			return underlyingModel.getRecord(row).get(col);
		}
		return null;
	}

	@Override
	public void update(Observable obj, final Object message) {
		if (SwingUtilities.isEventDispatchThread()) {
			if ("maxRecordSize".equals(message))
				fireTableStructureChanged();
			else
				fireTableDataChanged();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if ("maxRecordSize".equals(message))
						fireTableStructureChanged();
					else
						fireTableDataChanged();
				}
			});
		}
	}

	@Override
	public Class<?> getColumnClass(int arg0) {
		return String.class;
	}
	
}