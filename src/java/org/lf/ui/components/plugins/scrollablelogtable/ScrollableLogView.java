package org.lf.ui.components.plugins.scrollablelogtable;


import org.lf.parser.*;
import org.lf.plugins.Attributes;
import org.lf.plugins.analysis.Bookmarks;
import org.lf.plugins.analysis.highlight.Highlighter;
import org.lf.ui.util.GUIUtils;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.TableModel;

import java.util.Observable;
import java.util.Observer;

import static org.lf.util.CollectionFactory.pair;


public class ScrollableLogView extends JPanel implements Observer {
	private JButton addBookmark;
	private JComboBox bookmarksList; 

	private JScrollPane scrollTable;
	private JButton startButton;
	private JButton endButton;

	private JTable table;
	private JProgressBar progressBar;

	private Attributes attributes;

	private LogTableModel tableModel;
	private final ScrollableLogViewModel logSegmentModel;
	private LogTableCellRenderer cellRenderer;

	public ScrollableLogView(Log log, Attributes attributes) {
		this(log, attributes, null);
	}

	public ScrollableLogView(Log log, Attributes attributes, Position pos) {
		this.attributes = attributes;
		this.logSegmentModel = new ScrollableLogViewModel(log, 100);
		this.tableModel = new LogTableModel(logSegmentModel);
		this.cellRenderer = new LogTableCellRenderer(attributes.getValue(Highlighter.class), logSegmentModel);
		// Create UI

		this.bookmarksList = new JComboBox(new BookmarksComboBoxModel(attributes.getValue(Bookmarks.class)));
		this.bookmarksList.setPrototypeDisplayValue("0123456789");
		this.bookmarksList.addActionListener(new ComboBoxActionListener());
		//update bookmarks before any actions 
		this.bookmarksList.addFocusListener(new BookmarkFocusListener());
		GUIUtils.makePreferredSize(bookmarksList);

		this.addBookmark = new JButton("Add bookmark");
		this.addBookmark.addActionListener(new AddBookmarkActionListener());


		this.startButton = new JButton("Start");
		this.startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!logSegmentModel.isReadingDone() || logSegmentModel.isAtBegin()) return;
				logSegmentModel.start();
			}
		});

		this.endButton = new JButton("End");
		this.endButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!logSegmentModel.isReadingDone() || logSegmentModel.isAtEnd()) return;
				logSegmentModel.end();
			}
		});

		GUIUtils.createRecommendedButtonMargin(new JButton[]{startButton, endButton, addBookmark} );
		GUIUtils.makePreferredSize(startButton);
		GUIUtils.makePreferredSize(endButton);
		GUIUtils.makeSameWidth(new JComponent[] { startButton, endButton });

		this.progressBar = new JProgressBar(0, 100);
		this.progressBar.setValue(0);
		this.progressBar.setStringPainted(true);

		Box controlPanel = Box.createHorizontalBox();
		controlPanel.add(startButton);
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(endButton);
		controlPanel.add(Box.createHorizontalStrut(12));
		controlPanel.add(addBookmark);
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(bookmarksList);
		controlPanel.add(Box.createHorizontalGlue());
		GUIUtils.fixMaxHeightSize(controlPanel);

		this.table = new JTable(tableModel);
		this.table.setDefaultRenderer(String.class, this.cellRenderer);

		this.table.addKeyListener(new TableKeyListener());
		this.scrollTable = new JScrollPane(this.table);

		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(layout);
		this.add(controlPanel);
		this.add(Box.createVerticalStrut(12));
		this.add(this.scrollTable);
		this.add(Box.createVerticalStrut(12));
		this.add(this.progressBar);
		this.setVisible(true);
		this.logSegmentModel.addObserver(this);
		update(logSegmentModel, null);
	}

	public Position getSelectedRecord() {
		if (table.getSelectedRow() == -1) return logSegmentModel.getPosition(0);
		return logSegmentModel.getPosition(table.getSelectedRow());
	}

	public TableModel getTableModel() {
		return this.table.getModel();
	}
	
	@Override
	public void update(Observable o, Object message) {
		if (SwingUtilities.isEventDispatchThread()) {
			updateControls();
			updateProgress();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateControls();
					updateProgress();
				}
			});
		}
	}

	private void updateProgress() {
		if (logSegmentModel.isReadingDone()) {
			progressBar.setValue(100);
			return;
		}
		int cur = logSegmentModel.getRecordCount();
		int max = logSegmentModel.getRegionSize();
		int progress = cur*100/max;
		progressBar.setValue(progress);
	}

	private void updateControls() {
		if (logSegmentModel.isReadingDone()) {
			enableControls();
			if (logSegmentModel.isAtBegin()) 
				startButton.setEnabled(false);
			if (logSegmentModel.isAtEnd()) 
				endButton.setEnabled(false);				
		}
		else 
			disableControls();
	}

	private void enableControls() {
		startButton.setEnabled(true);
		endButton.setEnabled(true);
		addBookmark.setEnabled(true);
		bookmarksList.setEnabled(true);
	}

	private void disableControls() {
		startButton.setEnabled(false);
		endButton.setEnabled(false);
		addBookmark.setEnabled(false);
		bookmarksList.setEnabled(false);
	}

	//controllers
	class AddBookmarkActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			while(true) {
				String name = JOptionPane.showInputDialog(
						ScrollableLogView.this,
						"Enter bookmark name", "Add bookmark", JOptionPane.QUESTION_MESSAGE );
				if (name == null) return;

				if (attributes.getValue(Bookmarks.class).getValue(name) != null) {
					JOptionPane.showMessageDialog(
							ScrollableLogView.this,
					"Bookmark with such name already exists. Please input a different name.");
				} else {
					bookmarksList.addItem(pair(name , getSelectedRecord()));
					return;
				}
			}
		}

	}

	class ComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!logSegmentModel.isReadingDone()) return;
			JComboBox cb = (JComboBox)e.getSource();
			String selectedBookmark = (String)cb.getSelectedItem();
			if (selectedBookmark == null ) return;
			Position pos = attributes.getValue(Bookmarks.class).getValue(selectedBookmark);
			cellRenderer.setBookmarkPosition(pos);
			logSegmentModel.shiftTo(pos);
		}


	}

	class BookmarkFocusListener extends FocusAdapter {
		@Override
		public void focusGained(FocusEvent e) {
			((BookmarksComboBoxModel)bookmarksList.getModel()).update();
		}
	}

	class TableKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			int curIndex = table.getSelectionModel().getMaxSelectionIndex();
			switch(e.getKeyCode()) {
			case KeyEvent.VK_UP:
				if ( curIndex == 0 && !logSegmentModel.isAtBegin())
					logSegmentModel.shiftUp();
				break;
			case KeyEvent.VK_PAGE_UP:
				if (curIndex == 0 && !logSegmentModel.isAtBegin())
					logSegmentModel.prev();
				break;
			case KeyEvent.VK_DOWN:
				if (curIndex == (table.getRowCount() - 1) && !logSegmentModel.isAtEnd()) 
					logSegmentModel.shiftDown();
				break;
			case KeyEvent.VK_PAGE_DOWN:
				if (curIndex == (table.getRowCount() - 1) && !logSegmentModel.isAtEnd()) 
					logSegmentModel.next();
				break;
			case KeyEvent.VK_HOME:
				if (!logSegmentModel.isAtBegin())
					logSegmentModel.start();
				break;
			case KeyEvent.VK_END:
				if (!logSegmentModel.isAtEnd())
					logSegmentModel.end();
				break;
			}
		}
	}
}