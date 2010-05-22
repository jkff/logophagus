package org.lf.ui.components.plugins.scrollablelog;


import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.Attributes;
import org.lf.plugins.extension.ExtensionPointID;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.plugins.extension.ListExtensionPoint;
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

public class ScrollableLogView extends JPanel implements Observer {
    public static final ExtensionPointID<SLInitExtension> SL_INIT_EXTENSION_POINT_ID = ExtensionPointID.create();
    private static final ListExtensionPoint<SLInitExtension> SL_INIT_EXTENSION_POINT = new ListExtensionPoint<SLInitExtension>();

    static {
        ExtensionPointsManager.registerExtensionPoint(SL_INIT_EXTENSION_POINT_ID, SL_INIT_EXTENSION_POINT);
    }

    private final JToolBar toolbar;
    private final List<PopupElementProvider> pepList = newList();
    private final JList recordsList;
    private final JScrollPane scrollableRecords;
    private final JProgressBar progressBar;
    private final LogPopup popup;
    private final Attributes attributes;

    private final ScrollableLogModel logSegmentModel;
    private final AccumulativeColorer recordColorer;

    public final Context context;

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

        public Removable addPopupElementProvider(final PopupElementProvider pep) {
            pepList.add(pep);
            return new Removable() {
                @Override
                public void remove() {
                    pepList.remove(pep);
                }
            };
        }

        public Attributes getAttributes() {
            return ScrollableLogView.this.attributes;
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

        public Removable addToolbarElement(final JComponent c, int index) {
            toolbar.add(c, index);
            return new Removable() {
                @Override
                public void remove() {
                    toolbar.remove(c);
                }
            };

        }

        public int getToolbarElementIndex(JComponent c) {
            return toolbar.getComponentIndex(c);
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

        this.popup = new LogPopup();

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
        context = new Context();
        installExtensions();
        if (toolbar.getComponentCount() == 0)
            toolbar.setVisible(false);
        this.setVisible(true);
    }

    private void installExtensions() {
        List<SLInitExtension> extensions = SL_INIT_EXTENSION_POINT.getItems();
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


    private class LogPopup extends JPopupMenu {

        @Override
        public void show(Component invoker, int x, int y) {
            update();
            if (this.getComponentCount() != 0)
                super.show(invoker, x, y);
        }

        private void update() {
            this.removeAll();


            for (PopupElementProvider cur : pepList) {
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