package org.lf.ui.components.plugins.scrollablelog.extension.builtin;

import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.tree.highlight.RecordColorer;
import org.lf.ui.components.dialog.LongTaskDialog;
import org.lf.ui.components.dialog.SearchSetupDialog;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPanel;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;
import org.lf.util.Filter;
import org.lf.util.Removable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchExtension implements SLInitExtension {
    @Override
    public void init(final ScrollableLogPanel.Context context) {
        final Action searchAction = getActionForContext(context);
        JComponent component = new JButton(searchAction);
        context.addToolbarElement(component);
        context.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == (KeyEvent.CTRL_MASK | KeyEvent.VK_F)) {
                    searchAction.actionPerformed(null);
                }
            }
        });
    }

    private class SearchContext {
        public final SearchSetupDialog.SearchContext dialogContext;
        public final Removable highlighter;

        SearchContext(SearchSetupDialog.SearchContext dialogContext, Removable highlighter) {
            this.dialogContext = dialogContext;
            this.highlighter = highlighter;
        }
    }

    private Action getActionForContext(final ScrollableLogPanel.Context context) {
        return new AbstractAction("Find") {
            private SearchContext lastContext;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!context.getModel().isReadingDone() || context.getModel().getRecordCount() == 0)
                    return;
                SearchSetupDialog dialog = new SearchSetupDialog(Frame.getFrames()[0], Dialog.ModalityType.APPLICATION_MODAL);
                final SearchSetupDialog.SearchContext dialogSearchContext = lastContext != null ?
                        dialog.showSetupDialog(lastContext.dialogContext) :
                        dialog.showSetupDialog();
                if (lastContext != null)
                    lastContext.highlighter.remove();

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

                lastContext = new SearchContext(dialogSearchContext, removable);

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
//                        long startTime = new DateTime().getMillis();
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
//                            System.out.println(new DateTime().getMillis()-startTime);
                            while (!searchStateDialog.isVisible()) {
                                //TODO make right                                
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

    private Filter<Record> getFilterFromSearchContext(final SearchSetupDialog.SearchContext searchContext) {
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
