package org.lf.ui.components.plugins.scrollablelog;


import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.Attributes;
import org.lf.plugins.tree.highlight.Highlighter;
import org.lf.plugins.tree.highlight.RecordColorer;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;
import org.lf.ui.util.GUIUtils;
import org.lf.util.HierarchicalAction;
import org.lf.util.Removable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static org.lf.util.CollectionFactory.newList;

public class ScrollableLogPanel extends JPanel implements Observer {
    private final JToolBar toolbar;
    private final List<PopupElementProvider> popupProviders = newList();
    private final JList recordsList;
    private final JScrollPane recordsScrollPane;
    private final JProgressBar progressBar;
    private final LogPopup popup;
    private final Attributes attributes;

    private final ScrollableLogModel logSegmentModel;
    private final AccumulativeColorer recordColorer;

    public final Context context;

    public class Context {
        public ScrollableLogModel getModel() {
            return ScrollableLogPanel.this.logSegmentModel;
        }
        public int[] getSelectedIndexes() {
            return ScrollableLogPanel.this.recordsList.getSelectedIndices();
        }

        public Removable addRecordColorer(RecordColorer rc) {
            return ScrollableLogPanel.this.recordColorer.addFirst(rc);
        }

        public Removable addPopupElementProvider(final PopupElementProvider pep) {
            popupProviders.add(pep);
            return new Removable() {
                @Override
                public void remove() {
                    popupProviders.remove(pep);
                }
            };
        }

        public Attributes getAttributes() {
            return ScrollableLogPanel.this.attributes;
        }

        public Removable addKeyListener(final KeyListener kl) {
            recordsList.addKeyListener(kl);
            return new Removable() {
                @Override
                public void remove() {
                    recordsList.removeKeyListener(kl);
                }
            };
        }

        public Removable addToolbarElement(final JComponent c) {
            toolbar.add(c);
            toolbar.addSeparator();
            return new Removable() {
                @Override
                public void remove() {
                    toolbar.remove(c);
                }
            };
        }

        public void updateRecords() {
            recordsList.repaint();
        }
    }

    public ScrollableLogPanel(final Log log, Attributes attributes) {
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

        this.popup = new LogPopup();

        recordColorer = new AccumulativeColorer();
        if (customHighlighter != null)
            recordColorer.add(new RecordColorer() {
                @Override
                public Color getColor(Record r) {
                    return customHighlighter.getHighlightColor(r);
                }
            });

        RecordRenderer cellRenderer = new RecordRenderer(recordColorer);

        this.recordsList = new JList(listModel);
        this.recordsList.setCellRenderer(cellRenderer);
        this.recordsList.addKeyListener(new ListKeyListener());
        this.recordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    recordsList.setSelectedIndex(recordsList.locationToIndex(e.getPoint()));
                    popup.show(recordsList, e.getX(), e.getY());
                }
            }
        });

        this.recordsList.setVisible(true);

        recordsScrollPane = new JScrollPane(this.recordsList);
        recordsScrollPane.addMouseWheelListener(new ScrollBarMouseWheelListener());
        this.recordsScrollPane.setVisible(true);

        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(layout);
        this.add(toolbar);
        this.add(Box.createVerticalStrut(5));
        this.add(recordsScrollPane);
        this.add(Box.createVerticalStrut(5));
        this.add(this.progressBar);

        update(logSegmentModel, null);
        this.logSegmentModel.addObserver(this);
        context = new Context();
        installExtensions();
        if (toolbar.getComponentCount() == 0)
            toolbar.setVisible(false);
        this.setVisible(true);
    }

    public ScrollableLogState getState() {
        return new ScrollableLogState(logSegmentModel.getPosition(0));
    }

    public void restoreState(ScrollableLogState state) {
        logSegmentModel.shiftTo(state.firstPosition);
    }

    private void installExtensions() {
        List<SLInitExtension> extensions = ScrollableLogPlugin.getInitExtensions();
        for (SLInitExtension cur : extensions)
            cur.init(context);
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

            Point viewPos = recordsScrollPane.getViewport().getViewPosition();
            int maxScrollValue = recordsScrollPane.getHorizontalScrollBar().getModel().getMaximum();
            int curScrollValue = recordsScrollPane.getHorizontalScrollBar().getModel().getValue();
            int extScrollValue = recordsScrollPane.getHorizontalScrollBar().getModel().getExtent();

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
                    recordsScrollPane.getViewport().setViewPosition(viewPos);
                    break;
                case KeyEvent.VK_RIGHT:
                    if (curScrollValue + extScrollValue + 5 < maxScrollValue)
                        viewPos.x += 5;
                    else if (curScrollValue + extScrollValue < maxScrollValue)
                        viewPos.x = maxScrollValue;
                    recordsScrollPane.getViewport().setViewPosition(viewPos);
                    break;
            }
        }
    }

    class ScrollBarMouseWheelListener implements MouseWheelListener {
        @Override
        public void mouseWheelMoved(MouseWheelEvent event) {
            int maxValue = recordsScrollPane.getVerticalScrollBar().getModel().getMaximum();
            int curValue = recordsScrollPane.getVerticalScrollBar().getModel().getValue();
            int extValue = recordsScrollPane.getVerticalScrollBar().getModel().getExtent();
            if (event.getWheelRotation() < 0) {
                if (curValue == 0 && !logSegmentModel.isAtBegin())
                    logSegmentModel.shiftUp();
            } else {
                if (curValue + extValue == maxValue && !logSegmentModel.isAtEnd())
                    logSegmentModel.shiftDown();
            }
        }
    }


    private class LogPopup extends JPopupMenu {

        @Override
        public void show(Component invoker, int x, int y) {
            update();
            if (this.getComponentCount() != 0)
                super.show(invoker, x, y);
        }

        private void update() {
            this.removeAll();


            for (PopupElementProvider cur : popupProviders) {
                HierarchicalAction treeAction = cur.getHierarchicalAction();
                if (treeAction == null) continue;
                JMenuItem itemPlugin;
                if (treeAction.getAction() != null)
                    itemPlugin = new JMenuItem(treeAction.getAction());
                else {
                    itemPlugin = new JMenu(treeAction.getName());
                    fillByChildren(itemPlugin, treeAction);
                }
                add(itemPlugin);
            }

            this.revalidate();
        }

        private void fillByChildren(JMenuItem item, HierarchicalAction itemAction) {
            HierarchicalAction[] subActions = itemAction.getChildren();
            for (HierarchicalAction cur : subActions) {
                if (cur.getAction() != null)
                    item.add(new JMenuItem(cur.getAction()));
                else {
                    JMenuItem child = new JMenu(cur.getName());
                    fillByChildren(child, cur);
                    item.add(child);
                }
            }
        }

    }
}