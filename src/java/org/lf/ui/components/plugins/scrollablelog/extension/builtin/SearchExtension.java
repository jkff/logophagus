package org.lf.ui.components.plugins.scrollablelog.extension.builtin;

import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.tree.highlight.RecordColorer;
import org.lf.ui.components.dialog.LongTaskDialog;
import org.lf.ui.components.dialog.SearchSetupDialog;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;
import org.lf.ui.components.plugins.scrollablelog.extension.SLKeyListener;
import org.lf.ui.components.plugins.scrollablelog.extension.SLToolbarExtension;
import org.lf.util.Filter;
import org.lf.util.Removable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchExtension implements SLKeyListener, SLToolbarExtension {
    private Map<ScrollableLogView.Context, SearchContext> viewToSearchContext =
            new WeakHashMap<ScrollableLogView.Context, SearchContext>();

    private class SearchContext {
        public final SearchSetupDialog.SearchContext dialogContext;
        public final Removable highlighter;

        SearchContext(SearchSetupDialog.SearchContext dialogContext, Removable highlighter) {
            this.dialogContext = dialogContext;
            this.highlighter = highlighter;
        }
    }

    @Override
    public JComponent getToolbarElement(final ScrollableLogView.Context context) {
        Action searchAction = getActionForContext(context);
        return new JButton(searchAction);
    }

    @Override
    public void keyTyped(KeyEvent e, ScrollableLogView.Context context) {
        if (e.getKeyCode() == (KeyEvent.CTRL_MASK | KeyEvent.VK_F)) {
            getActionForContext(context).actionPerformed(null);
        }
    }

    @Override
    public void keyPressed(KeyEvent e, ScrollableLogView.Context context) {
    }

    @Override
    public void keyReleased(KeyEvent e, ScrollableLogView.Context context) {
    }


    private Action getActionForContext(final ScrollableLogView.Context context) {
        return new AbstractAction("Find") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!context.getModel().isReadingDone() || context.getModel().getRecordCount() == 0)
                    return;
                SearchSetupDialog dialog = new SearchSetupDialog(Frame.getFrames()[0], Dialog.ModalityType.APPLICATION_MODAL);
                final SearchSetupDialog.SearchContext dialogSearchContext = viewToSearchContext.containsKey(context) ?
                        dialog.showSetupDialog(viewToSearchContext.get(context).dialogContext) :
                        dialog.showSetupDialog();
                if (viewToSearchContext.containsKey(context))
                    viewToSearchContext.remove(context).highlighter.remove();

                if (!dialog.isOkPressed()) {
                    context.updateRecords();
                    return;
                }

                final Filter<Record> filter = getFilterFromSearchContext(dialogSearchContext);
                Removable removable = context.addRecordColorer(new RecordColorer() {
                    @Override
                    public Color getColor(Record r) {
                        if (filter.accepts(r))
                            return Color.YELLOW;
                        return null;
                    }
                });

                viewToSearchContext.put(context, new SearchContext(dialogSearchContext, removable));

                int indexes[] = context.getSelectedIndexes();
                int fromIndex = indexes.length != 0 ? indexes[0] : 0;
                final Position[] pos = {context.getModel().getPosition(fromIndex)};
                final Log log = pos[0].getCorrespondingLog();
                final LongTaskDialog searchStateDialog = new LongTaskDialog(
                        null,
                        "Searching",
                        "Please wait",
                        Dialog.ModalityType.APPLICATION_MODAL);
                searchStateDialog.setSize(200, 100);
                searchStateDialog.setResizable(false);
                final AtomicBoolean found = new AtomicBoolean(false);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Position cur = pos[0];
                            Position border = dialogSearchContext.forwardNotBackward ? log.last() : log.first();
                            while (!cur.equals(border) &&
                                    !found.get() &&
                                    !searchStateDialog.isCanceled()
                                    ) {
                                cur = dialogSearchContext.forwardNotBackward ? log.next(cur) : log.prev(cur);
                                Record r = log.readRecord(cur);
                                if (filter.accepts(r)) {
                                    found.set(true);
                                    break;
                                }
                            }
                            pos[0] = cur;
                            while (!searchStateDialog.isVisible()) {
                                //wait for dialog become visible
                            }
                            searchStateDialog.setVisible(false);
                        } catch (IOException e1) {
                            //Ignore
                            e1.printStackTrace();
                        }

                    }
                }.start();
                searchStateDialog.setVisible(true);

                if (found.get())
                    context.getModel().shiftTo(pos[0]);
                else if (!searchStateDialog.isCanceled())
                    JOptionPane.showMessageDialog(null, "Nothing found");
                searchStateDialog.dispose();
                context.updateRecords();
            }
        };
    }

    Filter<Record> getFilterFromSearchContext(final SearchSetupDialog.SearchContext searchContext) {
        final String userInput = searchContext.caseSensitive ? searchContext.text : searchContext.text.toLowerCase();
        return new Filter<Record>() {
            @Override
            public boolean accepts(Record r) {
                for (String field : r.getCellValues()) {
                    field = searchContext.caseSensitive ? field : field.toLowerCase();
                    if (searchContext.substringNotRegexp) {
                        if (field.contains(userInput))
                            return true;
                    } else {
                        if (field.matches(userInput))
                            return true;
                    }
                }
                return false;
            }
        };
    }
}
