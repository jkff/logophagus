package org.lf.ui.components.plugins.scrollablelogtable;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.analysis.highlight.Highlighter;

class LogTableCellRenderer extends DefaultTableCellRenderer {
    private Highlighter highlighter;
    private ScrollableLogViewModel model;

    public LogTableCellRenderer(Highlighter highlighter, ScrollableLogViewModel model) {
        this.highlighter = highlighter;
        this.model = model;
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Record rec = model.getRecord(row);
        Position pos = model.getPosition(row);
        JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) return cell;
        cell.setBackground(null);

        if (highlighter != null && rec != null) {
            Color col = highlighter.getHighlightColor(rec);
            if (col != null)
                cell.setBackground(col);
        }

        return cell;
    }
}
