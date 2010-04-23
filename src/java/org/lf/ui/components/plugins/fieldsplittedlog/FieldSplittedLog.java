package org.lf.ui.components.plugins.fieldsplittedlog;

import org.lf.logs.FilteredLog;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.Attributes;
import org.lf.plugins.analysis.splitbyfield.LogAndField;
import org.lf.ui.util.GUIUtils;
import org.lf.util.Filter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FieldSplittedLog extends JPanel {
    private LogAndField logAndField;
    private Attributes attributes;
    private ProgressMonitor progressMonitor;
    private FieldValuesListModel listModel = new FieldValuesListModel();
    private MyList fieldValues;
    private JSplitPane splitPane;
    private JPanel defaultPanel = new JPanel(new BorderLayout());
    private int fieldIndex = 0;

    private class ValuesSearchTask extends SwingWorker<Void, Void> {
        private static final int DEFAULT_RECORDS_TO_LOAD = 5000;

        @Override
        protected Void doInBackground() {
            try {
                Position cur = logAndField.log.first();
                Position end = logAndField.log.last();
                for (int i = 0; i < DEFAULT_RECORDS_TO_LOAD; ++i) {
                    if (progressMonitor.isCanceled())
                        break;
                    try {
                        Thread.currentThread().sleep(1);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Record rec = logAndField.log.readRecord(cur);
                    listModel.addFieldValue(rec.getCellValues()[fieldIndex]);
                    if (cur.equals(end)) break;
                    cur = logAndField.log.next(cur);
                    progressMonitor.setProgress(i);
                }
                listModel.setMaxReadedPosition(cur);
                listModel.addFieldValue(new Cell() {
                    @Override
                    public int getIndexInRecord() {
                        return fieldIndex;
                    }

                    @Override
                    public String getName() {
                        return "Other...";
                    }

                    @Override
                    public Type getType() {
                        return Type.TEXT;
                    }

                    @Override
                    public Object getValue() {
                        return "Other...";
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            progressMonitor.setProgress(DEFAULT_RECORDS_TO_LOAD);
            fieldValues.enableControls();

        }

        @Override
        protected void process(List<Void> arg0) {
            FieldSplittedLog.this.splitPane.getRightComponent().validate();
        }
    }

    public FieldSplittedLog(LogAndField logAndField, final Attributes attributes) {
        super(new BorderLayout());
        this.logAndField = logAndField;
        this.attributes = attributes;
        this.fieldIndex = logAndField.fieldIndex;

        fieldValues = new MyList(listModel);
        fieldValues.setCellRenderer(new FieldValuesCellRenderer());
        fieldValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.add(Box.createVerticalStrut(5));
        listPanel.add(new JLabel("Values of " + logAndField.fieldIndex + " field:"));
        listPanel.add(Box.createVerticalStrut(5));
        listPanel.add(new JScrollPane(fieldValues));
        defaultPanel.add(new JLabel("Select field value from right list to look at corresponding log"));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setRightComponent(listPanel);
        splitPane.setLeftComponent(defaultPanel);
        splitPane.setResizeWeight(0.7);

        GUIUtils.makePreferredSize(listPanel);
        GUIUtils.makePreferredSize(defaultPanel);
        GUIUtils.makePreferredSize(splitPane);

        this.add(splitPane);
        progressMonitor = new ProgressMonitor(this, "Reading", "Please wait", 0, ValuesSearchTask.DEFAULT_RECORDS_TO_LOAD);
        new ValuesSearchTask().execute();

    }

    void setViewPanel(JPanel panel) {
        splitPane.setLeftComponent(panel);
        panel.setVisible(true);
        splitPane.validate();
    }


    private class MyListSelectionListener implements ListSelectionListener {
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

    private class ListMouseListener extends MouseAdapter {
        public void mousePressed(final MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1)
                return;
            int selectedIndex = fieldValues.getSelectedIndex();
            final Cell cell = (Cell) fieldValues.getSelectedValue();
            JPanel panel = listModel.getView(cell);
            if (panel == null) {
                //this section handles last list element
                if (selectedIndex == listModel.getSize() - 1) {
                    if (listModel.getEndScanPosition() == null) {
                        panel = new JPanel();
                        panel.add(new JLabel("There is no other field values"));
                    } else {
                        Filter<Record> filter = new Filter<Record>() {
                            private Set<Cell> exceptedValues = new HashSet<Cell>(listModel.getValues());

                            public String toString() {
                                return "Except " + exceptedValues;
                            }

                            public boolean accepts(Record r) {
                                return !exceptedValues.contains(r.getCellValues()[fieldIndex]);
                            }
                        };
                        Log log = new FilteredLog(FieldSplittedLog.this.logAndField.log, filter);
                        panel = new ScrollableLogView(log, attributes, listModel.getEndScanPosition());
                    }
                } else {
                    Filter<Record> filter = new Filter<Record>() {
                        public String toString() {
                            return (String) cell.getValue();
                        }

                        public boolean accepts(Record r) {
                            return r.getCellValues()[fieldIndex].equals(cell);
                        }
                    };
                    Log log = new FilteredLog(FieldSplittedLog.this.logAndField.log, filter);
                    panel = new ScrollableLogView(log, attributes);
                }
                listModel.setView(cell, panel);
            }
            setViewPanel(panel);
        }
    }

    class MyList extends JList {
        public MyList(ListModel model) {
            super(model);
        }

        void enableControls() {
            this.addMouseListener(new ListMouseListener());
            this.addListSelectionListener(new MyListSelectionListener());
        }
    }
}
