package org.lf.plugins.all.ViewSideBySidePlugin;

import org.lf.parser.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import java.beans.*;
import java.io.IOException;
import java.util.ArrayList;

class ScrollableLogTable extends JPanel implements ActionListener,  PropertyChangeListener {
	private JButton startButton;
	private JButton endButton;
	private JButton prevButton;
	private JButton nextButton;
	private JProgressBar progressBar;
	private JTable table;
	
	private Log log;
	private ArrayList<Record> result;
	private Position curPos;
	
	private class LogTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return result.size();
		}

		public String getColumnName(int col) {
			return "Record";
		}

		public Object getValueAt(int row, int col) {
			return result.get(row);
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
			result.clear();
			int progress = 0;
			setProgress(progress);

			for (int i = 0; i < 100; ++i) {
				if (!fromWhere.equals(directionForward ? log.next(fromWhere) : log.prev(fromWhere))) {
					fromWhere = (directionForward ? log.next(fromWhere) : log.prev(fromWhere));
					setProgress(++progress);
					if (directionForward) {
						result.add(result.size(),log.readRecord(fromWhere));
					} else {
						result.add(0,log.readRecord(fromWhere));
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
		}
	}

	public ScrollableLogTable(Log log) {
		super(new BorderLayout());
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
		
		JPanel allButtons = new JPanel();
		
		allButtons.add(startButton);
		allButtons.add(prevButton);
		allButtons.add(nextButton);
		allButtons.add(endButton);
		
		table = new JTable(new LogTableModel());
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
            	switch(e.getKeyCode()) {
                case KeyEvent.VK_PAGE_DOWN:
                    new NavigateTask("next").execute();
                    break;
                case KeyEvent.VK_PAGE_UP:
                    new NavigateTask("prev").execute();
                    break;
                case KeyEvent.VK_HOME:
                    new NavigateTask("start").execute();
                    break;
                case KeyEvent.VK_END:
                    new NavigateTask("end").execute();
                    break;
                }
            }
        });

		this.add(allButtons,BorderLayout.PAGE_START);
		this.add(progressBar,BorderLayout.PAGE_END);
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
		
	}

	/**
	 * Invoked when the user presses button.
	 */
	public void actionPerformed(ActionEvent evt) {
		nextButton.setEnabled(false);
		prevButton.setEnabled(false);
		startButton.setEnabled(false);
		endButton.setEnabled(false);
//		SwingWorker is only designed to be executed once. 
//		Executing a SwingWorker more than once will not result in invoking the doInBackground method twice.
//		So we need to create new NavigateTask
		new NavigateTask(evt.getActionCommand()).execute();
	}

	
	
	// Invoked when NavigateTask's progress property changes
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("progress")) {
				table.updateUI();
				progressBar.setValue((Integer)evt.getNewValue());
		}
	}


}

