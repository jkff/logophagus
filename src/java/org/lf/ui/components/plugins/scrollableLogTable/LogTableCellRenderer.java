package org.lf.ui.components.plugins.scrollableLogTable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.lf.parser.Record;
import org.lf.plugins.analysis.highlight.Highlighter;

class LogTableCellRenderer extends DefaultTableCellRenderer {
	private Highlighter highlighter;
	
	
	public LogTableCellRenderer(Highlighter highlighter) {
		this.highlighter = highlighter;
	}


	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (highlighter == null) 
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		Record rec = ((LogTableModel)table.getModel()).getRecord(row);
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		Color bg = highlighter.getHighlightColor(rec);
		cell.setBackground(bg == null ? Color.WHITE : bg);		
		return cell;
	}
}
