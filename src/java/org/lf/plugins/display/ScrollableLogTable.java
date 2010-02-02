package org.lf.plugins.display;


import org.lf.parser.*;
import org.lf.plugins.analysis.Highlighter;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import java.beans.*;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


public class ScrollableLogTable extends JPanel implements ActionListener,  PropertyChangeListener {

        private JButton startButton;
		private JButton endButton;
		private JButton prevButton;
		private JButton nextButton;
		private JProgressBar progressBar;
		private JTable table;

    public void setHighlighter(Highlighter value) {
        this.highlighter = value;
    }

    //this is for verification that there is only one NavigateTask that executed
		private enum NavigateTaskState {
            BUSY, FREE
		}
		private NavigateTaskState taskState = NavigateTaskState.FREE;
		
		private Log log;
		private List<Record> result;
		private Position curPos;

        private Highlighter highlighter;

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
				if (highlighter == null) 
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					
				Record rec = result.get(row);
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Color bg = highlighter.getHighlightColor(rec);
                cell.setBackground(bg == null ? Color.WHITE : bg);					
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
				addPropertyChangeListener(ScrollableLogTable.this);
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
				taskState = NavigateTaskState.FREE;
			}
		}

		public ScrollableLogTable(Log log) {
			this.log = log;
			try {
				curPos = log.getStart();
			} catch(IOException e){
				e.printStackTrace();
			}
			result = new ArrayList<Record>();
			
			// Create UI
			
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
			for(int i =0; i < table.getColumnCount(); ++i){
				table.getColumnModel().getColumn(i).setCellRenderer(renderer);
			}
			
			JScrollPane sTable = new JScrollPane(table);
			table.addKeyListener(new KeyAdapter() {
	            @Override
	            public void keyPressed(KeyEvent e) {
	            	if (taskState == NavigateTaskState.FREE){
	            		switch(e.getKeyCode()) {
	            		case KeyEvent.VK_PAGE_DOWN:
	            			taskState = NavigateTaskState.BUSY;
	            			new NavigateTask("next").execute();
	            			break;
	            		case KeyEvent.VK_PAGE_UP:
	            			taskState = NavigateTaskState.BUSY;
	            			new NavigateTask("prev").execute();
	            			break;
	            		case KeyEvent.VK_HOME:
	            			taskState = NavigateTaskState.BUSY;
	            			new NavigateTask("start").execute();
	            			break;
	            		case KeyEvent.VK_END:
	            			taskState = NavigateTaskState.BUSY;
	            			new NavigateTask("end").execute();
	            			break;
	            		}
	            	}
	            }
	        });
	        
	        SpringLayout layout = new SpringLayout();
	        layout.putConstraint(SpringLayout.NORTH, naviButtons, 5, SpringLayout.NORTH, this);
	        
	        layout.putConstraint(SpringLayout.NORTH, sTable, 5, SpringLayout.SOUTH, naviButtons);
	        layout.putConstraint(SpringLayout.EAST, sTable, 0, SpringLayout.EAST, this);
	        layout.putConstraint(SpringLayout.WEST, sTable, 5, SpringLayout.WEST, this);
	        layout.putConstraint(SpringLayout.SOUTH, sTable, -5, SpringLayout.NORTH, progressBar);
	        
	        layout.putConstraint(SpringLayout.SOUTH, progressBar, -5, SpringLayout.SOUTH, this);
	        layout.putConstraint(SpringLayout.WEST, progressBar, 5, SpringLayout.WEST, this);
	        layout.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, this);
	        
	        this.setLayout(layout);
	        this.add(naviButtons);
			this.add(sTable);
			this.add(progressBar);
			
			taskState = NavigateTaskState.BUSY;
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
				taskState = NavigateTaskState.BUSY;
				new NavigateTask(evt.getActionCommand()).execute();
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
