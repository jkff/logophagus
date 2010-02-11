package org.lf.ui.components.plugins.scrollableLogTable;


import org.lf.parser.*;
import org.lf.plugins.Attributes;
import org.lf.services.Bookmarks;
import org.lf.services.Highlighter;
import org.lf.util.Pair;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.beans.*;
import java.io.IOException;


public class ScrollableLogTable extends JPanel {

	private JButton addBookmark;
	private JComboBox bookmarksList; 

	private JButton startButton;
	private JButton endButton;
	private JButton prevButton;
	private JButton nextButton;

	private JTable table;
	private JProgressBar progressBar;

	private Log log;
	private Attributes attributes;

	private Position curPos;
	private LogTableModel tableModel;
	private LogTableCellRenderer cellRenderer;

	private ActionListener naviBtnListener = new NavigateButtonsActionListener(); 
	private PropertyChangeListener naviProgressListener = new NavigateTaskProgressListener(); 

	//this is for verification that there is only one NavigateTask that executes 
	private boolean navigateTaskDone = true;


	//Reads log records on its own thread
	class NavigateTask extends SwingWorker<Void, Void> {
		private String command;
		public NavigateTask(String command) {
			this.command = command;
			addPropertyChangeListener(naviProgressListener);
		}

		//Reading log records in background Worker thread.
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
			tableModel.clear();
			int progress = 0;
			setProgress(progress);
			//				int reflectionCount = 0;

			for (int i = 0; i < 100; ++i) {
				if (!fromWhere.equals(directionForward ? log.next(fromWhere) : log.prev(fromWhere))) {
					setProgress(++progress);
					if (directionForward) {
						tableModel.add(tableModel.getRowCount(), log.readRecord(fromWhere));
						fromWhere = log.next(fromWhere);
					} else {
						//we always read forward , thats why go back firstly
						fromWhere = log.prev(fromWhere);
						tableModel.add(0, log.readRecord(fromWhere));
					}						
				} else {
					//						++reflectionCount;
					//						System.out.print(reflectionCount);
					//						if (reflectionCount == 2) break;
					directionForward = !directionForward;
					fromWhere = curPos;
					--i;
				}
				//					System.out.println(">> " + i);
			}
			curPos = fromWhere;
			setProgress(100);
		}

	}

	public ScrollableLogTable(Log log, Attributes attributes) {
		this.log = log;
		this.attributes = attributes;
		try {
			curPos = log.getStart();
			tableModel = new LogTableModel(log.readRecord(curPos).size());
		} catch(IOException e){
			e.printStackTrace();
		}


		cellRenderer = new LogTableCellRenderer(attributes.getValue(Highlighter.class));
		// Create UI

		bookmarksList = new JComboBox(new BookmarksComboBoxModel(attributes.getValue(Bookmarks.class)));
		bookmarksList.addActionListener(new ComboBoxActionListener());
		//update bookmarks before any actions 
		bookmarksList.addFocusListener(new BookmarkFocusListener());

		addBookmark = new JButton("Add bookmark");
		addBookmark.addActionListener(new AddBookmarkActionListener());
		
		JPanel bookmarkPanel = new JPanel();
		bookmarkPanel.add(addBookmark);
		bookmarkPanel.add(bookmarksList);

		startButton = new JButton("Start");
		startButton.setActionCommand("start");
		startButton.addActionListener(naviBtnListener);

		endButton = new JButton("End");
		endButton.setActionCommand("end");
		endButton.addActionListener(naviBtnListener);

		prevButton = new JButton("Prev");
		prevButton.setActionCommand("prev");
		prevButton.addActionListener(naviBtnListener);

		nextButton = new JButton("Next");
		nextButton.setActionCommand("next");
		nextButton.addActionListener(naviBtnListener);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		JPanel naviButtons = new JPanel();
		naviButtons.setLayout(new GridLayout(1, 4, 5, 0));
		naviButtons.add(startButton);
		naviButtons.add(prevButton);
		naviButtons.add(nextButton);
		naviButtons.add(endButton);

		table = new JTable(tableModel);

		for(int i =0; i < table.getColumnCount(); ++i){
			table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
		}

		JScrollPane scrollTable = new JScrollPane(table);
		table.addKeyListener(new TableKeyListener());

		SpringLayout layout = new SpringLayout();

		layout.putConstraint(SpringLayout.NORTH, bookmarkPanel, 5, SpringLayout.NORTH, this);
		layout.putConstraint(SpringLayout.WEST, bookmarkPanel, 5, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, naviButtons, 5, SpringLayout.SOUTH, bookmarkPanel);
		layout.putConstraint(SpringLayout.WEST, naviButtons, 5, SpringLayout.WEST, this);

		layout.putConstraint(SpringLayout.NORTH, scrollTable, 5, SpringLayout.SOUTH, naviButtons);
		layout.putConstraint(SpringLayout.EAST, scrollTable, 0, SpringLayout.EAST, this);
		layout.putConstraint(SpringLayout.WEST, scrollTable, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.SOUTH, scrollTable, -5, SpringLayout.NORTH, progressBar);

		layout.putConstraint(SpringLayout.SOUTH, progressBar, -5, SpringLayout.SOUTH, this);
		layout.putConstraint(SpringLayout.WEST, progressBar, 5, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.EAST, progressBar, 0, SpringLayout.EAST, this);

		this.setLayout(layout);

		this.add(bookmarkPanel);
		this.add(naviButtons);
		this.add(scrollTable);
		this.add(progressBar);

		navigateTaskDone = false;
		new NavigateTask("start").execute();

	}

	
	
	//controls
	
	class NavigateTaskProgressListener implements PropertyChangeListener { 
		// Invoked when NavigateTask's progress property changes
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("progress")) {
				table.updateUI();
				progressBar.setValue((Integer)evt.getNewValue());
			} else if (evt.getPropertyName().equals("state") 
					&& 
					((NavigateTask)evt.getSource()).getState().equals(SwingWorker.StateValue.DONE)) 
			{
				startButton.setEnabled(true);
				endButton.setEnabled(true);
				prevButton.setEnabled(true);
				nextButton.setEnabled(true);
				navigateTaskDone = true;
			}
		}
	}

	class AddBookmarkActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			do {
				String name = JOptionPane.showInputDialog(null, "Enter bookmark name", "Add bookmark", JOptionPane.QUESTION_MESSAGE );
				if (name == null) return;

				if (attributes.getValue(Bookmarks.class).getValue(name) != null) {
					JOptionPane.showMessageDialog(ScrollableLogTable.this, "Bookmark with such name already exist. Please enter other name.");
				} else {
					bookmarksList.addItem(new Pair<String, Position>(name , curPos));
					return;
				}
			} while (true);
		}

	}

	class ComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox)e.getSource();
			String selectedBookmark = (String)cb.getSelectedItem();
			curPos = attributes.getValue(Bookmarks.class).getValue(selectedBookmark);
			navigateTaskDone = false;
			new NavigateTask("prev").execute();
		}

	}

	class NavigateButtonsActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (!evt.getActionCommand().equals("set")){
				nextButton.setEnabled(false);
				prevButton.setEnabled(false);
				startButton.setEnabled(false);
				endButton.setEnabled(false);
				//			SwingWorker is only designed to be executed once. 
				//			Executing a SwingWorker more than once will not result in invoking the doInBackground method twice.
				//			So we need to create new NavigateTask
				navigateTaskDone = false;
				new NavigateTask(evt.getActionCommand()).execute();
			}
		}

	}

	class BookmarkFocusListener extends FocusAdapter {
		@Override
		public void focusGained(FocusEvent arg0) {
			((BookmarksComboBoxModel)bookmarksList.getModel()).update();
		}
	}

	class TableKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			if (navigateTaskDone){
				switch(e.getKeyCode()) {
				case KeyEvent.VK_PAGE_DOWN:
					navigateTaskDone = false;
					new NavigateTask("next").execute();
					break;
				case KeyEvent.VK_PAGE_UP:
					navigateTaskDone = false;
					new NavigateTask("prev").execute();
					break;
				case KeyEvent.VK_HOME:
					navigateTaskDone = false;
					new NavigateTask("start").execute();
					break;
				case KeyEvent.VK_END:
					navigateTaskDone = false;
					new NavigateTask("end").execute();
					break;
				}
			}
		}
	}
}
