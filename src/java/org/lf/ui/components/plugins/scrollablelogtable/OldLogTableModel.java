package org.lf.ui.components.plugins.scrollablelogtable;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.util.Pair;

import com.sun.istack.internal.Nullable;

import static org.lf.util.CollectionFactory.newList;
import static org.lf.util.CollectionFactory.pair;;

class OldLogTableModel extends AbstractTableModel {
	private List<Pair<Record,Position>> recAndPos = newList();
	private int columnsNumber = 0;

	@Nullable
	synchronized public Record getRecord(int index) {
		if (index >= recAndPos.size()) return null;
		return recAndPos.get(index).first;
	}

	synchronized public Position getPosition(int index) {
		if (recAndPos.size() <= index) return null;
		return recAndPos.get(index).second;
	}

	synchronized public void clear() {
		recAndPos.clear();
		recAndPos.clear();
		this.fireTableDataChanged();
	}

	synchronized public void add(int index, Record rec, Position pos) {
		recAndPos.add(index, pair(rec, pos));
		if (columnsNumber < rec.size()) {
			columnsNumber = rec.size();
			this.fireTableStructureChanged();
			return;
		}
		this.fireTableRowsInserted(index, index);
	}

	@Override
	public int getColumnCount() {
		return columnsNumber;
	}
	@Override
	synchronized public int getRowCount() {
		return recAndPos.size();
	}

	public String getColumnName(int col) {
		return "Field "+ col;
	}
	@Override
	synchronized public Object getValueAt(int row, int col) {
		if (recAndPos.size() > row && recAndPos.get(row).first.size() > col){
			return recAndPos.get(row).first.get(col);
		}
		return null;
	}
		
}

