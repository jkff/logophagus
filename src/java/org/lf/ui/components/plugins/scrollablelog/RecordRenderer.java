package org.lf.ui.components.plugins.scrollablelog;

import org.lf.logs.Format;
import org.lf.logs.Record;
import org.lf.plugins.tree.highlight.RecordColorer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class RecordRenderer extends JPanel implements ListCellRenderer {
    private RecordColorer colorer;
    private final List<JLabel> cells;
    private final Font defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private final Font timeFont = new Font(Font.MONOSPACED, Font.BOLD, 12);


    public RecordRenderer(RecordColorer colorer) {
        this.colorer = colorer;
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
            boolean cellHasFocus)
    {
        if (value == null || value.getClass().isAssignableFrom(Record.class)) return null;
        Record record = (Record) value;
        extendRecordViewIfSmaller(record);
        String[] cellValues = record.getCellValues();

        for (int i = 0; i < cellValues.length; ++i) cells.get(i).setText(" " + cellValues[i] + " ");
        for (int i = 0; i < cells.size(); ++i) cells.get(i).setVisible(i < cellValues.length);
        for (int i = 0; i < cells.size(); ++i) cells.get(i).setFont(defaultFont);
        if (record.getFormat().getTimeFieldIndex() != -1)
            cells.get(record.getFormat().getTimeFieldIndex()).setFont(timeFont);

        Color background, foreground;
        if (isSelected) {
            background = UIManager.getColor("List.selectionBackground");
            foreground = UIManager.getColor("List.selectionForeground");
        } else {

            Color c = (colorer == null) ? null : colorer.getColor(record);
            if (c == null && record.getFormat().equals(Format.UNKNOWN_FORMAT))
                c = Color.PINK;

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
            label.setFont(defaultFont);
            this.cells.add(label);
            this.add(label);
        }
    }

}
