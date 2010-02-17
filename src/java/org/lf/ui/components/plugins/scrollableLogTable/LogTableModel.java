package org.lf.ui.components.plugins.scrollableLogTable;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.lf.parser.Position;
import org.lf.parser.Record;

import com.sun.istack.internal.Nullable;

class LogTableModel extends AbstractTableModel {
	private ArrayList<Record> recData = new ArrayList<Record>();
	private ArrayList<Position> posData = new ArrayList<Position>();

	private int columnsNumber;


	@Nullable
	synchronized public Record getRecord(int index) {
		if (recData.size() <= index) return null; 
		return recData.get(index); 			
	}

	synchronized public Position getPosition(int index) {
		if (posData.size() <= index) return null; 
		return posData.get(index); 			
	}

	synchronized public void clear() {
		recData.clear();
		posData.clear();
		this.fireTableDataChanged();
	}

	synchronized public void add(int index, Record rec, Position pos) {
		recData.add(index, rec);
		posData.add(index, pos);
		if (columnsNumber < rec.size()) {
			columnsNumber = rec.size();
			this.fireTableStructureChanged();
			return;
		}
		this.fireTableRowsInserted(index, index);
	}

	public LogTableModel( int columnsNumber) {
		this.columnsNumber = columnsNumber;
	}

	public int getColumnCount() {
		return columnsNumber;
	}

	synchronized public int getRowCount() {
		return recData.size();
	}

	public String getColumnName(int col) {
		return "Field "+ col;
	}

	synchronized public Object getValueAt(int row, int col) {
		if (recData.size() > row && recData.get(row).size() > col){
			return recData.get(row).get(col);
		}
		return null;
	}
		
}

