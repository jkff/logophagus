package org.lf.ui.components.plugins.scrollablelog;


import org.lf.logs.Format;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.*;
import org.lf.plugins.Attributes;
import org.lf.plugins.analysis.Bookmarks;
import org.lf.plugins.analysis.highlight.Highlighter;
import org.lf.plugins.analysis.highlight.RecordColorer;
import org.lf.ui.util.GUIUtils;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class ScrollableLogView extends JPanel implements Observer {
    private final JButton addBookmark;
    private final JComboBox bookmarksList;

    private final JButton startButton;
    private final JButton endButton;

    private final JList recordsList;
    private final JScrollPane scrollableRecords;
    private final JProgressBar progressBar;

    private final Attributes attributes;

    private ScrollableLogModel logSegmentModel;

    public ScrollableLogView(Log log, Attributes attributes) {
        this(log, attributes, null);
    }

    public ScrollableLogView(final Log log, Attributes attributes, Position pos) {
        this.attributes = attributes;
        this.logSegmentModel = null;
        this.logSegmentModel = new ScrollableLogModel(log, 50);
        this.logSegmentModel.start();
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

        RecordsListModel listModel = new RecordsListModel(logSegmentModel);

        Highlighter customHighlighter = attributes.getValue(Highlighter.class);
        Highlighter highlighter = new Highlighter(
                customHighlighter==null ? new ArrayList<Highlighter>() : Arrays.asList(customHighlighter));
        highlighter.setRecordColorer(new RecordColorer() {
            public Color getColor(Record r) {
                return r.getFormat() == Format.UNKNOWN_FORMAT ? Color.PINK : null;
            }
        });
        RecordView cellRenderer = new RecordView(highlighter);
        this.recordsList = new JList(listModel);
        this.recordsList.setCellRenderer(cellRenderer);
        this.recordsList.addKeyListener(new ListKeyListener());
        this.recordsList.setVisible(true);
        
        scrollableRecords = new JScrollPane(this.recordsList);
        scrollableRecords.addMouseWheelListener( new ScrollBarMouseWheelListener());
        this.scrollableRecords.setVisible(true);
        
//        RepaintManager.currentManager(recordsList).setDoubleBufferingEnabled(false);
//        scrollableRecords.setDebugGraphicsOptions(DebugGraphics.FLASH_OPTION);

        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(layout);
        this.add(controlPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(scrollableRecords);
        this.add(Box.createVerticalStrut(5));
        this.add(this.progressBar);
        this.setVisible(true);
        update(logSegmentModel, null);
        this.logSegmentModel.addObserver(this);

    }

    @Override
    public void update(Observable o, Object message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateControls();
                updateProgress();
                recordsList.revalidate();
                recordsList.repaint();
            }
        });
    }

    private void updateProgress() {
        progressBar.setValue(logSegmentModel.getProgress());
    }

    private void updateControls() {
        if (!logSegmentModel.isReadingDone() || logSegmentModel.getRecordCount() == 0) {
            disableControls();
        } else {
            enableControls();
            if (logSegmentModel.isAtBegin())
                startButton.setEnabled(false);
            if (logSegmentModel.isAtEnd())
                endButton.setEnabled(false);
        }
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
                if (name == null || name.equals("")) return;

                    try {
                        if (attributes.getValue(Bookmarks.class).getValue(name) != null) {
                            JOptionPane.showMessageDialog(
                                    ScrollableLogView.this,
                                    "Bookmark with such name already exists. Please input a different name.");
                        } else {
                            int row = recordsList.getSelectedIndex();
                            if (row == -1) row = 0;
                            attributes.getValue(Bookmarks.class).addBookmark(name, logSegmentModel.getPosition(row));
                            return;
                        }
                    } catch (IOException e1) {
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
                e1.printStackTrace();
            }
            //TODO think about better solution
            recordsList.setSelectedIndex(0);
            scrollableRecords.scrollRectToVisible(new Rectangle(0, 0));
        }
    }

    class BookmarkFocusListener extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            ((BookmarksComboBoxModel) bookmarksList.getModel()).update();
        }
    }

    class ListKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int curIndex = recordsList.getSelectedIndex();

            Point viewPos = scrollableRecords.getViewport().getViewPosition();
            int maxScrollValue = scrollableRecords.getHorizontalScrollBar().getModel().getMaximum();
            int curScrollValue = scrollableRecords.getHorizontalScrollBar().getModel().getValue();
            int extScrollValue = scrollableRecords.getHorizontalScrollBar().getModel().getExtent();

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
                if (curIndex == (recordsList.getModel().getSize() - 1) && !logSegmentModel.isAtEnd())
                    logSegmentModel.shiftDown();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                if (curIndex == (recordsList.getModel().getSize() - 1) && !logSegmentModel.isAtEnd()) {
                    recordsList.setSelectedIndex(recordsList.getModel().getSize() - 1);
                    logSegmentModel.next();
                }
                break;
            case KeyEvent.VK_HOME:
                recordsList.setSelectedIndex(0);
                if (!logSegmentModel.isAtBegin())
                    logSegmentModel.start();
                break;
            case KeyEvent.VK_END:
                recordsList.setSelectedIndex(recordsList.getModel().getSize() - 1);
                if (!logSegmentModel.isAtEnd())
                    logSegmentModel.end();
                break;
            case KeyEvent.VK_LEFT:
                if (curScrollValue > 5) 
                    viewPos.x -= 5;
                else if (curScrollValue > 0) 
                    viewPos.x = 0;
                scrollableRecords.getViewport().setViewPosition(viewPos);
                break;
            case KeyEvent.VK_RIGHT:
                if (curScrollValue + extScrollValue + 5 < maxScrollValue) 
                    viewPos.x += 5;
                else if (curScrollValue + extScrollValue < maxScrollValue) 
                    viewPos.x = maxScrollValue;
                scrollableRecords.getViewport().setViewPosition(viewPos);     
                break;
            }
        }
    }

    class ScrollBarMouseWheelListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent event) {
            int maxValue = scrollableRecords.getVerticalScrollBar().getModel().getMaximum();
            int curValue = scrollableRecords.getVerticalScrollBar().getModel().getValue();
            int extValue = scrollableRecords.getVerticalScrollBar().getModel().getExtent();
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