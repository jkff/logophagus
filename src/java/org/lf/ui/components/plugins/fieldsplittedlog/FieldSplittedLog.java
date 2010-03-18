package org.lf.ui.components.plugins.fieldsplittedlog;

import org.lf.logs.Field;
import org.lf.logs.FilteredLog;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.Attributes;
import org.lf.plugins.analysis.splitbyfield.LogAndField;
import org.lf.ui.components.plugins.scrollablelogtable.ScrollableLogView;
import org.lf.ui.util.GUIUtils;
import org.lf.util.Filter;

import java.io.IOException;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FieldSplittedLog extends JPanel {
	private LogAndField logAndField;
	private Attributes attributes;
	private ProgressMonitor progressMonitor;
	private FieldValuesListModel listModel = new FieldValuesListModel();
	private MyList fieldValues;
	private JSplitPane splitPane;
	private JPanel defaultPanel = new JPanel(new BorderLayout());
	private int fieldIndex = 0;
	
	class ValuesSearchTask extends SwingWorker<Void, Void> {
        private static final int DEFAULT_RECORDS_TO_LOAD = 5000;

        @Override
		protected Void doInBackground() {
			try {
				Position cur = logAndField.log.first();
				Position end = logAndField.log.last();
				for (int i = 0; i < DEFAULT_RECORDS_TO_LOAD; ++i) {
					if (progressMonitor.isCanceled())
						break;
					Record rec = logAndField.log.readRecord(cur);
					listModel.addFieldValue(rec.get(fieldIndex));
					if (cur.equals(end)) break;
					cur = logAndField.log.next(cur);
					progressMonitor.setProgress(i);
				}
				listModel.setMaxReadedPosition(cur);
				listModel.addFieldValue("Other");
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
	}

	public FieldSplittedLog(LogAndField logAndField, final Attributes attributes) {
		super(new BorderLayout());
		this.logAndField = logAndField;
		this.attributes = attributes;
		Field[] fields = logAndField.log.getFields();		
		for (Field field : fields) {
			if (field.equals(logAndField.field)) break;
			++fieldIndex;
		};

		fieldValues = new MyList(listModel);
		fieldValues.setCellRenderer(new FieldValuesCellRenderer());
		fieldValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.add(Box.createVerticalStrut(5));
		listPanel.add(new JLabel("Values of " + logAndField.field + " field:"));
		listPanel.add(Box.createVerticalStrut(5));
		listPanel.add(new JScrollPane(fieldValues));
		defaultPanel.add(new JLabel("Select field value from right list to look at corresponding log"));
		GUIUtils.makePreferredSize(listPanel);
		GUIUtils.makePreferredSize(defaultPanel);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerSize(5);
		splitPane.setContinuousLayout(true);
		splitPane.setRightComponent(listPanel);
		splitPane.setLeftComponent(defaultPanel);
		splitPane.setResizeWeight(0.7);
		GUIUtils.makePreferredSize(splitPane);
		this.add(splitPane);
		progressMonitor = new ProgressMonitor(this, "Reading", "Please wait", 0, ValuesSearchTask.DEFAULT_RECORDS_TO_LOAD);
		new ValuesSearchTask().execute();
		this.setVisible(true);
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
			final String fieldValue = (String) fieldValues.getSelectedValue();
			JPanel panel = listModel.getView(fieldValue);
			if (panel == null) {
				//this section handles last list element
				if (selectedIndex == listModel.getSize() - 1) {
					if (listModel.getOtherPosition() == null) {
						panel = new JPanel();
						panel.add(new JLabel("There is no other field values"));
					} else {
						Filter<Record> filter = new Filter<Record>() {
							private List<String> exceptedValues = listModel.getValues();
							public String toString() {
								return "Except " + exceptedValues;
							}

							public boolean accepts(Record t) {
								for (String cur : exceptedValues) {
									if (t.get(fieldIndex).equals(cur)) return false;
								}
								return true;
							}
						};
						Log log = new FilteredLog(FieldSplittedLog.this.logAndField.log, filter);
						panel = new ScrollableLogView(log, attributes, listModel.getOtherPosition());
					}
				} else {
					Filter<Record> filter = new Filter<Record>() {
						public String toString() {
							return fieldValue;
						}

						public boolean accepts(Record t) {
							return t.get(fieldIndex).equals(fieldValue);
						}
					};
					Log log = new FilteredLog(FieldSplittedLog.this.logAndField.log, filter);
					panel = new ScrollableLogView(log, attributes);
				}
				listModel.setView(fieldValue, panel);
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
			this.addListSelectionListener(new FieldSelectionListener());
		}
	}
}
