package org.lf.ui.components.plugins.scrollablelogtable;


import org.lf.logs.Log;
import org.lf.parser.*;
import org.lf.plugins.Attributes;
import org.lf.plugins.analysis.Bookmarks;
import org.lf.plugins.analysis.highlight.Highlighter;
import org.lf.ui.util.GUIUtils;

import java.awt.HeadlessException;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import java.util.Observable;
import java.util.Observer;

public class ScrollableLogView extends JPanel implements Observer {
    private final JButton addBookmark;
    private final JComboBox bookmarksList;

    private final JButton startButton;
    private final JButton endButton;

    private final JTable table;
    private final JScrollPane scrollTable;
    private final JProgressBar progressBar;

    private final Attributes attributes;
    private final ListSelectionModel tableSelectionModel = new TableSelectionModel();
    private final ScrollableLogViewModel logSegmentModel;

    public ScrollableLogView(Log log, Attributes attributes) {
        this(log, attributes, null);
    }

    public ScrollableLogView(Log log, Attributes attributes, Position pos) {
        this.attributes = attributes;
        this.logSegmentModel = new ScrollableLogViewModel(log, 100);
        this.logSegmentModel.start();

        LogTableModel tableModel = new LogTableModel(logSegmentModel);
        LogTableCellRenderer cellRenderer = new LogTableCellRenderer(
                attributes.getValue(Highlighter.class), logSegmentModel);
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

        GUIUtils.createRecommendedButtonMargin(new JButton[]{startButton, endButton, addBookmark});
        GUIUtils.makePreferredSize(startButton);
        GUIUtils.makePreferredSize(endButton);
        GUIUtils.makeSameWidth(new JComponent[]{startButton, endButton});

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
        this.table.setDefaultRenderer(String.class, cellRenderer);
        this.table.setSelectionModel(tableSelectionModel);
        this.table.addKeyListener(new TableKeyListener());
        tableSelectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                table.repaint();
            }
        });

        scrollTable = new JScrollPane(this.table);
        scrollTable.addMouseWheelListener( new ScrollBarMouseWheelListener());
        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(layout);
        this.add(controlPanel);
        this.add(Box.createVerticalStrut(12));
        this.add(scrollTable);
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateControls();
                updateProgress();
            }
        });
    }

    private void updateProgress() {
        if (logSegmentModel.isReadingDone()) {
            progressBar.setValue(100);
            return;
        }
        int cur = logSegmentModel.getRecordCount();
        int max = logSegmentModel.getRegionSize();
        int progress = cur * 100 / max;
        progressBar.setValue(progress);
    }

    private void updateControls() {
        if (logSegmentModel.isReadingDone()) {
            enableControls();
            if (logSegmentModel.isAtBegin())
                startButton.setEnabled(false);
            if (logSegmentModel.isAtEnd())
                endButton.setEnabled(false);
        } else
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
            while (true) {
                String name = JOptionPane.showInputDialog(
                        ScrollableLogView.this,
                        "Enter bookmark name", "Add bookmark", JOptionPane.QUESTION_MESSAGE);
                if (name == null) return;

                try {
					if (attributes.getValue(Bookmarks.class).getValue(name) != null) {
					    JOptionPane.showMessageDialog(
					            ScrollableLogView.this,
					            "Bookmark with such name already exists. Please input a different name.");
					} else {
					    int row = table.getSelectedRow();
					    if (row == -1) row = 0;
					    attributes.getValue(Bookmarks.class).addBookmark(name, logSegmentModel.getPosition(row));
					    return;
					}
				} catch (HeadlessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }

    }

    class ComboBoxActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!logSegmentModel.isReadingDone()) return;
            JComboBox cb = (JComboBox) e.getSource();
            String selectedBookmark = (String) cb.getSelectedItem();
            if (selectedBookmark == null) return;
            Position pos;
			try {
				pos = attributes.getValue(Bookmarks.class).getValue(selectedBookmark);
	            logSegmentModel.shiftTo(pos);

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            //TODO think about better solution
            //int row = getCorrespondingVisualRow(pos);
            //table.setRowSelectionInterval(row, row);
            //scrollTable.repaint();
        }
    }

    class BookmarkFocusListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            ((BookmarksComboBoxModel) bookmarksList.getModel()).update();
        }
    }

    class TableKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int curIndex = tableSelectionModel.getMaxSelectionIndex();
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (curIndex == 0 && !logSegmentModel.isAtBegin())
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
                if (curIndex == (table.getRowCount() - 1) && !logSegmentModel.isAtEnd()) {
                    tableSelectionModel.setSelectionInterval(table.getRowCount()-1, table.getRowCount()-1);
                    logSegmentModel.next();
                }
                break;
            case KeyEvent.VK_HOME:
                tableSelectionModel.setSelectionInterval(0, 0);
                if (!logSegmentModel.isAtBegin())
                    logSegmentModel.start();
                break;
            case KeyEvent.VK_END:
                tableSelectionModel.setSelectionInterval(table.getRowCount()-1, table.getRowCount()-1);
                if (!logSegmentModel.isAtEnd())
                    logSegmentModel.end();
                break;
            }
        }
    }

    class ScrollBarMouseWheelListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent event) {
            int maxValue = scrollTable.getVerticalScrollBar().getModel().getMaximum();
            int curValue = scrollTable.getVerticalScrollBar().getModel().getValue();
            int extValue = scrollTable.getVerticalScrollBar().getModel().getExtent();
            if (event.getWheelRotation() < 0) {
                if (curValue == 0 && !logSegmentModel.isAtBegin())
                    logSegmentModel.shiftUp();
            } else {
                if (curValue + extValue == maxValue && !logSegmentModel.isAtEnd())
                    logSegmentModel.shiftDown();
            }
        }
    }
}