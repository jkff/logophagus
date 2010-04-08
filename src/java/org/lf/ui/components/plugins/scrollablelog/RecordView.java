package org.lf.ui.components.plugins.scrollablelog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.lf.logs.Record;
import org.lf.plugins.analysis.highlight.Highlighter;
import static org.lf.util.CollectionFactory.newList;

public class RecordView extends JPanel implements ListCellRenderer {
    private final ScrollableLogModel model;
    private final Highlighter highlighter;
    private final List<JTextArea> jCells;
    private final JTextArea preferredSizeArea;

    public RecordView(ScrollableLogModel model, Highlighter highlighter) {
        this.model = model;
        this.highlighter = highlighter;
        this.jCells = newList();
        preferredSizeArea = new JTextArea();
        preferredSizeArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

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
        Record record = (Record)value;
        extendRecordViewIfSmaller(record);
        String[] cellValues = record.getCellValues();
        int maxHeight  = 0;
        for (int i = 0; i < cellValues.length; ++i) {
            jCells.get(i).setText(cellValues[i]);

            preferredSizeArea.setText(cellValues[i]);
            Dimension preferredSize = preferredSizeArea.getPreferredSize();
            jCells.get(i).setPreferredSize(preferredSize);

            if (preferredSize.height > maxHeight) maxHeight = preferredSize.height;
        }

        for (int i = 0; i < cellValues.length; ++i) {
            Dimension d = jCells.get(i).getPreferredSize();
            d.height = maxHeight;
            d.width += 10;
            jCells.get(i).setPreferredSize(d);
            jCells.get(i).setMinimumSize(d);
            jCells.get(i).setMaximumSize(d);
            jCells.get(i).setVisible(true);
        }

        for (int i = cellValues.length; i < jCells.size() ; i++) {
            jCells.get(i).setVisible(false);
        }

        if (isSelected) {
            this.setBackground(UIManager.getColor("List.selectionBackground"));
        } else {
            Color color;
            if (highlighter != null)   
                color = highlighter.getHighlightColor(record);
            else
                color = UIManager.getColor("List.background");
            this.setBackground(color);
        }
        this.revalidate();
        return this;
    }

    void extendRecordViewIfSmaller( Record record) {
        int recSize = record.getCellValues().length;
        int vRecSize = this.jCells.size();
        if ( recSize <= vRecSize) return;
        for (int i = 0; i < (recSize - vRecSize); ++i) {
            JTextArea newTextArea = new JTextArea();
            newTextArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            this.jCells.add(newTextArea);
            this.add(newTextArea);
        }    
    }
}
