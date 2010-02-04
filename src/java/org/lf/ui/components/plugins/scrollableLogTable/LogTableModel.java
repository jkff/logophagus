package org.lf.ui.components.plugins.scrollableLogTable;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.lf.parser.Record;

import com.sun.istack.internal.Nullable;

class LogTableModel extends AbstractTableModel {
	private ArrayList<Record> data = new ArrayList<Record>();
	private int columnsNumber;
	
	
	@Nullable
	public Record getRecord(int index) {
		synchronized (data) {
			if (data.size() <= index) return null; 
			return data.get(index); 			
		}
	}

	public void clear() {
		synchronized (data) {
			data.clear();
		}
		this.fireTableDataChanged();
	}
	
	public void add(int index, Record rec) {
		synchronized (data) {
			data.add(index, rec);
		}
		this.fireTableRowsInserted(index, index);
	}

	public LogTableModel( int columnsNumber) {
			this.columnsNumber = columnsNumber;
	}
	
	public int getColumnCount() {
		return columnsNumber;
	}

	public int getRowCount() {
		synchronized (data) {
			return data.size();
		}
	}

	public String getColumnName(int col) {
		return "Field "+ col;
	}
	
	public Object getValueAt(int row, int col) {
		synchronized (data) {
			if (data.size() > row && data.get(row).size() > col){
				return data.get(row).get(col);
			}
		} 
		return null;
	}
}

