package org.lf.plugins.display;


import org.lf.parser.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.beans.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HighlightedScrollableLogTable extends JPanel implements ActionListener,  PropertyChangeListener {
		private JTextField patternField;
		private JButton setPatternButton;
	
		private JButton startButton;
		private JButton endButton;
		private JButton prevButton;
		private JButton nextButton;
		private JProgressBar progressBar;
		private JTable table;
		
		//this is for verification that there is only one NavigateTask that executed
		private enum NavigateTaskState {
			Busy, Free;
		}
		private NavigateTaskState taskState = NavigateTaskState.Free;
		
		private Log log;
		private List<Record> result;
		private Position curPos;
		private Pattern pattern; 
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				if (pattern != null) {
					Matcher m = pattern.matcher((String)value);
					if (m.matches()){
						cell.setBackground(Color.RED);
					}
				}
				return cell;
			}
		};
		
		private class LogTableModel extends AbstractTableModel {
			private int columnsNumber;
			
			public LogTableModel() {
				try {
					columnsNumber = log.readRecord(log.getStart()).size(); 
				} catch (IOException e) {
					columnsNumber = 0;
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			public int getColumnCount() {
				return columnsNumber;
			}

			public int getRowCount() {
				synchronized (result) {
					return result.size();
				}
			}

			public String getColumnName(int col) {
				return "Field "+ col;
			}

			public Object getValueAt(int row, int col) {
				synchronized (result) {
					if (result.size() > row && result.get(row).size() > col){
						table.getColumnModel().getColumn(col).setCellRenderer(renderer);
						return result.get(row).get(col);
					} else {
						return null;
					}

				} 
			}
		}
		
	//Reads log records on its own thread
		class NavigateTask extends SwingWorker<Void, Void> {
			private String command;
			public NavigateTask(String command) {
				this.command = command;
				addPropertyChangeListener(HighlightedScrollableLogTable.this);
			}

			//		 Reading log records in background Worker thread.
			@Override
			public Void doInBackground() {
				try {
					if ("next".equals(command)) {
						navigate(true, curPos);
					} else if ("prev".equals(command)) {
						navigate(false, curPos);
					} else if ("start".equals(command)) {
						navigate(true, log.getStart());
					} else if ("end".equals(command)) {
						navigate(false, log.getEnd());
					}
				} catch (IOException e) {
					System.out.print(e.getMessage());
				}
				return null;
			}

			public void navigate(boolean directionForward, Position fromWhere) throws IOException {
				synchronized (result) {
					result.clear();				
				}
				int progress = 0;
				setProgress(progress);
				
				for (int i = 0; i < 100; ++i) {
					if (!fromWhere.equals(directionForward ? log.next(fromWhere) : log.prev(fromWhere))) {
						setProgress(++progress);
						synchronized (result) {
							if (directionForward) {
								result.add(result.size(),log.readRecord(fromWhere));
								fromWhere = log.next(fromWhere);
							} else {
								//we always read forward , thats why go back firstly
								fromWhere = log.prev(fromWhere);
								result.add(0, log.readRecord(fromWhere));
							}						
						}
					} else {
						directionForward = !directionForward;
						fromWhere = curPos;
						--i;
					}

				}
				curPos = fromWhere;
			}

			//		  Executed in event dispatching thread when doInBackground() ends
			@Override
			public void done() {
				startButton.setEnabled(true);
				endButton.setEnabled(true);
				prevButton.setEnabled(true);
				nextButton.setEnabled(true);
				taskState = NavigateTaskState.Free;
			}
		}

		public HighlightedScrollableLogTable(Log log) {
			super(new BorderLayout());
			this.log = log;
			try {
				curPos = log.getStart();
			} catch(IOException e){
				e.printStackTrace();
			}
			result = new ArrayList<Record>();
			
			// Create UI
			patternField = new JTextField(20);

			setPatternButton = new JButton("Set Pattern");
			setPatternButton.setActionCommand("set");
			setPatternButton.addActionListener(this);

			JPanel patternPanel = new JPanel();
			patternPanel.add(patternField);
			patternPanel.add(setPatternButton);
			
			startButton = new JButton("Start");
			startButton.setActionCommand("start");
			startButton.addActionListener(this);

			endButton = new JButton("End");
			endButton.setActionCommand("end");
			endButton.addActionListener(this);

			prevButton = new JButton("Prev");
			prevButton.setActionCommand("prev");
			prevButton.addActionListener(this);

			nextButton = new JButton("Next");
			nextButton.setActionCommand("next");
			nextButton.addActionListener(this);
			
			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			
			JPanel naviButtons = new JPanel();
			
			naviButtons.add(startButton);
			naviButtons.add(prevButton);
			naviButtons.add(nextButton);
			naviButtons.add(endButton);
			
			table = new JTable(new LogTableModel());
			JScrollPane sTable = new JScrollPane(table);
			table.addKeyListener(new KeyAdapter() {
	            @Override
	            public void keyPressed(KeyEvent e) {
	            	if (taskState == NavigateTaskState.Free){
	            		switch(e.getKeyCode()) {
	            		case KeyEvent.VK_PAGE_DOWN:
	            			taskState = NavigateTaskState.Busy;
	            			new NavigateTask("next").execute();
	            			break;
	            		case KeyEvent.VK_PAGE_UP:
	            			taskState = NavigateTaskState.Busy;
	            			new NavigateTask("prev").execute();
	            			break;
	            		case KeyEvent.VK_HOME:
	            			taskState = NavigateTaskState.Busy;
	            			new NavigateTask("start").execute();
	            			break;
	            		case KeyEvent.VK_END:
	            			taskState = NavigateTaskState.Busy;
	            			new NavigateTask("end").execute();
	            			break;
	            		}
	            	}
	            }
	        });
	        
	        SpringLayout layout = new SpringLayout();
	        layout.putConstraint(SpringLayout.NORTH, patternPanel, 5, SpringLayout.NORTH, this);
	        layout.putConstraint(SpringLayout.NORTH, naviButtons, 5, SpringLayout.SOUTH, patternPanel);
	        layout.putConstraint(SpringLayout.NORTH, sTable, 5, SpringLayout.SOUTH, naviButtons);
	        layout.putConstraint(SpringLayout.NORTH, progressBar, 5, SpringLayout.SOUTH, sTable);
	        
	        this.setLayout(layout);
	        this.add(patternPanel);
	        this.add(naviButtons);
			this.add(sTable);
			this.add(progressBar);
			
			taskState = NavigateTaskState.Busy;
			new NavigateTask("start").execute();
		}

		/**
		 * Invoked when the user presses button.
		 */
		public void actionPerformed(ActionEvent evt) {
			if (!evt.getActionCommand().equals("set")){
				nextButton.setEnabled(false);
				prevButton.setEnabled(false);
				startButton.setEnabled(false);
				endButton.setEnabled(false);
				//			SwingWorker is only designed to be executed once. 
				//			Executing a SwingWorker more than once will not result in invoking the doInBackground method twice.
				//			So we need to create new NavigateTask
				taskState = NavigateTaskState.Busy;
				new NavigateTask(evt.getActionCommand()).execute();
			} else {
				System.out.println(patternField.getText());
				pattern = Pattern.compile(patternField.getText());
				table.updateUI();
			}
		}

		// Invoked when NavigateTask's progress property changes
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("progress")) {
					table.updateUI();
					progressBar.setValue((Integer)evt.getNewValue());
			}
		}




}
