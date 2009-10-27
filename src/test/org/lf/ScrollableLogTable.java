package test.org.lf;

import org.lf.parser.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import java.beans.*;
import java.io.IOException;
import java.util.ArrayList;


public class ScrollableLogTable extends JPanel implements ActionListener, PropertyChangeListener {
	private JButton startButton;
	private JButton endButton;
	private JButton prevButton;
	private JButton nextButton;
	private JProgressBar progressBar;
	private JTable table;
	
	private LogReader logReader;
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
	class LogReader extends SwingWorker<Void, Void> {
		private String command;
		public LogReader(String command) {
			this.command = command;
		}
		
//		 Reading log records in background Worker thread.
		@Override
		public Void doInBackground() {
			boolean directionForward = true;
			try {
				if ("next".equals(command)) {
					directionForward = true;
				} else if ("prev".equals(command)) {
					directionForward = false;
				} else if ("start".equals(command)) {
					curPos = log.getStart();
				} else if ("end".equals(command)) {
					curPos = log.getEnd();
				}

				result.clear();
				
				int progress = 0;
				setProgress(progress);
				
				Position tmp = curPos;
				for (int i = 0; i < 100; ++i) {
					if (!tmp.equals(directionForward ? log.next(tmp) : log
							.prev(tmp))) {
						tmp = (directionForward ? log.next(tmp) : log.prev(tmp));
						setProgress(++progress);
						if (directionForward) {
							result.add(result.size(),log.readRecord(tmp));
						} else {
							result.add(0,log.readRecord(tmp));
						}
					} else {
						directionForward = !directionForward;
						tmp = curPos;
						--i;
					}

				}
				curPos = tmp;
				
			} catch (IOException e) {
				System.out.print(e.getMessage());
			}
			return null;
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
		
		table= new JTable(new LogTableModel());
		
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
//		So we need to create new LogReader
		logReader = new LogReader(evt.getActionCommand());
		logReader.addPropertyChangeListener(this);
		logReader.execute();
	}

	
	
	// Invoked when LogReader's progress property changes
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("progress")) {
				table.updateUI();
				progressBar.setValue(logReader.getProgress());
		}
	}
}

