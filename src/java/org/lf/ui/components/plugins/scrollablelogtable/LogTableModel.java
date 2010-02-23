package org.lf.ui.components.plugins.scrollablelogtable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.lf.parser.Position;
import org.lf.parser.Record;

import com.sun.istack.internal.Nullable;

import static org.lf.util.CollectionFactory.newList;

class LogTableModel extends AbstractTableModel {
	private List<Record> records = newList();
	private List<Position> positions = newList();

	private int columnsNumber;


	@Nullable
	synchronized public Record getRecord(int index) {
		if (records.size() <= index) return null;
		return records.get(index);
	}

	synchronized public Position getPosition(int index) {
		if (positions.size() <= index) return null;
		return positions.get(index);
	}

	synchronized public void clear() {
		records.clear();
		positions.clear();
		this.fireTableDataChanged();
	}

	synchronized public void add(int index, Record rec, Position pos) {
		records.add(index, rec);
		positions.add(index, pos);
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
		return records.size();
	}

	public String getColumnName(int col) {
		return "Field "+ col;
	}

	synchronized public Object getValueAt(int row, int col) {
		if (records.size() > row && records.get(row).size() > col){
			return records.get(row).get(col);
		}
		return null;
	}
		
}

