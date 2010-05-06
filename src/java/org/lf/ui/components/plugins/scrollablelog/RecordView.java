package org.lf.ui.components.plugins.scrollablelog;

import org.lf.logs.Record;
import org.lf.plugins.analysis.highlight.Highlighter;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class RecordView extends JPanel implements ListCellRenderer {
    private final Highlighter highlighter;
    private final List<JLabel> cells;

    public RecordView(Highlighter highlighter) {
        this.highlighter = highlighter;
        this.cells = newList();

        this.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));
        this.setVisible(true);
    }

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        if (value == null || value.getClass().isAssignableFrom(Record.class)) return null;
        Record record = (Record) value;
        extendRecordViewIfSmaller(record);
        String[] cellValues = record.getCellValues();

        for (int i = 0; i < cellValues.length; ++i) cells.get(i).setText(" " + cellValues[i] + " ");
        for (int i = 0; i < cells.size(); ++i) cells.get(i).setVisible(i < cellValues.length);

        Color background, foreground;
        if (isSelected) {
            background = UIManager.getColor("List.selectionBackground");
            foreground = UIManager.getColor("List.selectionForeground");
        } else {
            Color c = (highlighter == null) ? null : highlighter.getHighlightColor(record);
            background = (c == null) ? ((index % 2 == 0) ? new Color(244, 244, 244) : Color.WHITE) : c;
            foreground = UIManager.getColor("List.foreground");
        }

        for (JLabel cell : cells) {
            if (!cell.getBackground().equals(background))
                cell.setBackground(background);
            if (!cell.getForeground().equals(foreground))
                cell.setForeground(foreground);
        }

        if (!this.getBackground().equals(background))
            this.setBackground(background);
        if (!this.getForeground().equals(foreground))
            this.setForeground(foreground);

        this.revalidate();
        return this;
    }

    void extendRecordViewIfSmaller(Record record) {
        int recSize = record.getCellValues().length;
        int vRecSize = this.cells.size();
        if (recSize <= vRecSize) return;
        for (int i = 0; i < (recSize - vRecSize); ++i) {
            JLabel label = new JLabel();
            label.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            this.cells.add(label);
            this.add(label);
        }
    }
}
