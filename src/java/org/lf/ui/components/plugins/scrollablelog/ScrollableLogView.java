package org.lf.ui.components.plugins.scrollablelog;


import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.Attributes;
import org.lf.plugins.tree.highlight.Highlighter;
import org.lf.plugins.tree.highlight.RecordColorer;
import org.lf.ui.components.plugins.scrollablelog.extension.SLKeyListener;
import org.lf.ui.components.plugins.scrollablelog.extension.SLPluginsRepository;
import org.lf.ui.components.plugins.scrollablelog.extension.SLToolbarExtension;
import org.lf.ui.util.GUIUtils;
import org.lf.util.Removable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

public class ScrollableLogView extends JPanel implements Observer {
    private final JToolBar toolbar;

    private final JList recordsList;
    private final JScrollPane scrollableRecords;
    private final JProgressBar progressBar;
    private final LogPopup popup;
    private final Attributes attributes;

    private final ScrollableLogModel logSegmentModel;
    private final AccumulativeColorer recordColorer;

    public Context context = new Context();

    public class Context {
        public ScrollableLogModel getModel() {
            return ScrollableLogView.this.logSegmentModel;
        }

        public int[] getSelectedIndexes() {
            return ScrollableLogView.this.recordsList.getSelectedIndices();
        }

        public Removable addRecordColorer(RecordColorer rc) {
            return ScrollableLogView.this.recordColorer.addFirst(rc);
        }


        public Attributes getAttributes() {
            return ScrollableLogView.this.attributes;
        }

        public void updateRecords() {
            recordsList.repaint();
        }
    }

    public ScrollableLogView(Log log, Attributes attributes) {
        this(log, attributes, null);
    }

    public ScrollableLogView(final Log log, Attributes attributes, Position pos) {
        this.attributes = attributes;
        this.logSegmentModel = new ScrollableLogModel(log, 50);
        this.logSegmentModel.start();
        // Create UI

        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setValue(0);
        this.progressBar.setStringPainted(true);

        this.toolbar = new JToolBar();
        this.toolbar.setFloatable(false);

        GUIUtils.fixMaxHeightSize(toolbar);

        RecordsListModel listModel = new RecordsListModel(logSegmentModel);

        final Highlighter customHighlighter = attributes.getValue(Highlighter.class);

        this.popup = new LogPopup(this.context);

        recordColorer = new AccumulativeColorer();
        if (customHighlighter != null)
            recordColorer.add(new RecordColorer() {
                @Override
                public Color getColor(Record r) {
                    return customHighlighter.getHighlightColor(r);
                }
            });

        RecordView cellRenderer = new RecordView(recordColorer);

        this.recordsList = new JList(listModel);
        this.recordsList.setCellRenderer(cellRenderer);
        this.recordsList.addKeyListener(new ListKeyListener());
        this.recordsList.setComponentPopupMenu(this.popup);
        this.recordsList.setVisible(true);

        scrollableRecords = new JScrollPane(this.recordsList);
        scrollableRecords.addMouseWheelListener(new ScrollBarMouseWheelListener());
        this.scrollableRecords.setVisible(true);

        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(layout);
        this.add(toolbar);
        this.add(Box.createVerticalStrut(5));
        this.add(scrollableRecords);
        this.add(Box.createVerticalStrut(5));
        this.add(this.progressBar);

        update(logSegmentModel, null);
        this.logSegmentModel.addObserver(this);
        installExtensions();
        if (toolbar.getComponentCount() == 0)
            toolbar.setVisible(false);
        this.setVisible(true);
    }

    @Override
    public void update(Observable o, Object message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateControls();
                updateProgress();
                recordsList.repaint();
            }
        });
    }

    private void installExtensions() {
        SLKeyListener[] keyListeners = SLPluginsRepository.getRegisteredKeyListeners();
        for (final SLKeyListener cur : keyListeners) {
            this.recordsList.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {
                    cur.keyTyped(e, context);
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    cur.keyTyped(e, context);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    cur.keyTyped(e, context);
                }
            });
        }

        SLToolbarExtension[] toolbarExtensions = SLPluginsRepository.getRegisteredToolbarExtensions();
        for (final SLToolbarExtension cur : toolbarExtensions) {
            this.toolbar.add(cur.getToolbarElement(context));
            this.toolbar.addSeparator();
        }

    }

    private void updateProgress() {
        progressBar.setValue((int) logSegmentModel.getProgress());
    }

    private void updateControls() {
        if (!logSegmentModel.isReadingDone() || logSegmentModel.getRecordCount() == 0) {
            toolbar.setEnabled(false);
        } else {
            toolbar.setEnabled(true);
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