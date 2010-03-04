package org.lf.ui.components.plugins.scrollablelogtable;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.lf.parser.Record;
import org.lf.plugins.analysis.highlight.Highlighter;

class OldLogTableCellRenderer extends DefaultTableCellRenderer {
	private Highlighter highlighter;
	
	public OldLogTableCellRenderer(Highlighter highlighter) {
		this.highlighter = highlighter;
	}

	
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		Record rec = ((OldLogTableModel)table.getModel()).getRecord(row);
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if (highlighter == null || rec == null) 
			return cell;
		cell.setBackground(highlighter.getHighlightColor(rec));		
		return cell;
	}
}
