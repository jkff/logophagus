package org.lf.ui.components.plugins.scrollablelog.extension.search;

import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.tree.highlight.RecordColorer;
import org.lf.ui.components.dialog.LongTaskDialog;
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
import java.util.regex.Pattern;

public class SearchExtension implements SLInitExtension {
    @Override
    public void init(final ScrollableLogPanel.Context context) {
        final Action searchAction = getActionForContext(context);
        JComponent component = new JButton(searchAction);
        context.addToolbarElement(component);
        context.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
                    searchAction.actionPerformed(null);
                }
            }
        });
    }

    private class SearchContext {
        public final SearchDialog.SearchContext dialogContext;
        public final Removable highlighter;

        SearchContext(SearchDialog.SearchContext dialogContext, Removable highlighter) {
            this.dialogContext = dialogContext;
            this.highlighter = highlighter;
        }
    }

    private Action getActionForContext(final ScrollableLogPanel.Context context) {
        return new AbstractAction("Find") {
            private SearchContext lastContext;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (context.getModel().getShownRecordCount() == 0)
                    return;
                SearchDialog dialog = new SearchDialog(Frame.getFrames()[0], Dialog.ModalityType.APPLICATION_MODAL);
                final SearchDialog.SearchContext dialogSearchContext = lastContext != null ?
                        dialog.showSetupDialog(lastContext.dialogContext) :
                        dialog.showSetupDialog();
                if(dialogSearchContext == null)
                    return;
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
                searchStateDialog.setSize(300, 100);
                searchStateDialog.setResizable(true);

                final AtomicBoolean found = new AtomicBoolean(false);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Position cur = pos[0];
                            Position border = dialogSearchContext.forwardNotBackward ? log.last() : log.first();
                            int numScanned = 0;
                            while (cur != null && !cur.equals(border) &&
                                    !found.get() &&
                                    !searchStateDialog.isCanceled())
                            {
                                cur = dialogSearchContext.forwardNotBackward ? log.next(cur) : log.prev(cur);
                                Record r = log.readRecord(cur);
                                if (filter.accepts(r)) {
                                    found.set(true);
                                    break;
                                }
                                if(numScanned++ % 10000 == 0) {
                                    int timeFieldIndex = r.getFormat().getTimeFieldIndex();
                                    if(timeFieldIndex != -1) {
                                        searchStateDialog.setProgressText("Now at " + r.getCell(timeFieldIndex));
                                    } else {
                                        CharSequence s = r.getRawString();
                                        searchStateDialog.setProgressText("Now at " + s.subSequence(0, Math.min(50,s.length())) + "...");
                                    }
                                }
                            }
                            pos[0] = cur;
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
                    context.getModel().shiftTo(pos[0], new Runnable() {
                        @Override
                        public void run() {
                            context.selectPosition(pos[0]);
                        }
                    });
                else if (!searchStateDialog.isCanceled())
                    JOptionPane.showMessageDialog(null, "Nothing found");
                searchStateDialog.dispose();
                context.updateRecords();
            }
        };
    }

    private Filter<Record> getFilterFromSearchContext(final SearchDialog.SearchContext searchContext) {
        final String userInput = searchContext.caseSensitive ? searchContext.text : searchContext.text.toLowerCase();
        final Pattern p = Pattern.compile(userInput,
                (searchContext.substringNotRegexp ? Pattern.LITERAL : 0) |
                (searchContext.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE));
        return new Filter<Record>() {
            @Override
            public boolean accepts(Record r) {
                for (int i = 0; i < r.getCellCount(); ++i) {
                    CharSequence cell = r.getCell(i);
                    if(p.matcher(cell).find())
                        return true;
                }
                return false;
            }
        };
    }
}
