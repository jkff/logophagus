package org.lf.ui.components.plugins.fieldSplittedLog;

import org.lf.parser.FilteredLog;
import org.lf.parser.Log;
import org.lf.parser.Record;
import org.lf.plugins.Attributes;
import org.lf.plugins.analysis.splitbyfield.LogAndField;
import org.lf.ui.components.plugins.scrollableLogTable.ScrollableLogTable;
import org.lf.util.Filter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class FieldSplittedLog extends JPanel {
    private LogAndField logAndField;
    private Attributes attributes;

    private FieldValuesListModel listModel = new FieldValuesListModel();
    private JList fieldValues;
    private JSplitPane splitPane;
    private JPanel defaultPanel = new JPanel(new BorderLayout());

    public FieldSplittedLog(LogAndField logAndField, final Attributes attributes) {
        super(new BorderLayout());
        this.logAndField = logAndField;
        this.attributes = attributes;

        fieldValues = new JList(listModel);
        fieldValues.setCellRenderer(new FieldValuesCellRenderer());
        fieldValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fieldValues.addMouseListener(new ListMouseListener());
        fieldValues.addListSelectionListener(new FieldSelectionListener());

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.add(Box.createVerticalStrut(5));
        listPanel.add(new JLabel("Values of " + logAndField.field + " field:"));
        listPanel.add(Box.createVerticalStrut(5));
        listPanel.add(new JScrollPane(fieldValues));
        listPanel.add(Box.createVerticalStrut(5));
        defaultPanel.add(new JLabel("Select field value from right list to look at corresponding log"));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setRightComponent(listPanel);
        splitPane.setLeftComponent(defaultPanel);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        this.add(splitPane);
        listModel.fillModel(logAndField);
    }

    public void setViewPanel(JPanel panel) {
        splitPane.setLeftComponent(panel);
        splitPane.updateUI();
    }


    class FieldSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int selected = ((JList) e.getSource()).getSelectedIndex();
            JPanel panel = listModel.getView(selected);
            if (panel == null) {
                setViewPanel(defaultPanel);
            } else {
                setViewPanel(panel);
            }
        }
    }

    class ListMouseListener extends MouseAdapter {
        public void mousePressed(final MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1)
                return;
            int selectedIndex = fieldValues.getSelectedIndex();
            String fieldValue = (String) fieldValues.getSelectedValue();
            JPanel panel = listModel.getView(fieldValue);
            if (panel == null) {
                if (selectedIndex == listModel.getSize() - 1) {
                    panel = new ScrollableLogTable(FieldSplittedLog.this.logAndField.log, attributes);
                    try {
                        ((ScrollableLogTable) panel).scrollTo(listModel.getOtherPosition());
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } else {
                    final int fieldIndex = FieldSplittedLog.this.logAndField.field;
                    final String value1 = fieldValue;
                    Filter<Record> filter = new Filter<Record>() {
                        private String value = value1;

                        public String toString() {
                            return value;
                        }

                        public boolean accepts(Record t) {
                            return t.get(fieldIndex).equals(value);
                        }
                    };
                    Log log = new FilteredLog(FieldSplittedLog.this.logAndField.log, filter);
                    panel = new ScrollableLogTable(log, attributes);
                }
                listModel.setView(fieldValue, panel);
            }
            setViewPanel(panel);
        }
    }
}
