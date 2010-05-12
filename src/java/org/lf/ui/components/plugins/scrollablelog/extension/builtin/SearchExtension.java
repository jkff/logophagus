package org.lf.ui.components.plugins.scrollablelog.extension.builtin;

import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.parser.Position;
import org.lf.plugins.tree.highlight.RecordColorer;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;
import org.lf.ui.components.plugins.scrollablelog.extension.SLKeyListener;
import org.lf.ui.components.plugins.scrollablelog.extension.SLToolbarExtension;
import org.lf.ui.util.LongTaskDialog;
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
    private Map<ScrollableLogView.Context, Removable> viewToColorer = new WeakHashMap<ScrollableLogView.Context, Removable>();

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
        Action searchAction = new AbstractAction("Search") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (viewToColorer.containsKey(context)) {
                    viewToColorer.get(context).remove();
                    viewToColorer.remove(context);
                }

                final String input = JOptionPane.showInputDialog(
                        null,
                        "Enter search text",
                        "Search",
                        JOptionPane.QUESTION_MESSAGE);
                if (input == null) {
                    context.updateRecords();
                    return;
                }
                int indexes[] = context.getSelectedIndexes();
                int fromIndex = indexes.length != 0 ? indexes[0] : 0;
                final Position[] pos = {context.getModel().getPosition(fromIndex)};
                final Log log = pos[0].getCorrespondingLog();
                final AtomicBoolean found = new AtomicBoolean(false);

                final LongTaskDialog searchStateDialog = new LongTaskDialog(
                        (Frame) null,
                        "Search",
                        "Please wait",
                        Dialog.ModalityType.APPLICATION_MODAL);
                searchStateDialog.setSize(200, 100);
                searchStateDialog.setResizable(false);
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Position cur = pos[0];
                            while (!cur.equals(log.last()) && !found.get() && !searchStateDialog.isCanceled()) {
                                cur = log.next(cur);
                                Record r = log.readRecord(cur);
                                for (String field : r.getCellValues()) {
                                    if (field.contains(input)) {
                                        found.set(true);
                                        break;
                                    }
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

                if (found.get()) {
                    Removable removable = context.addRecordColorer(new RecordColorer() {
                        @Override
                        public Color getColor(Record r) {
                            for (String field : r.getCellValues()) {
                                if (field.contains(input))
                                    return Color.YELLOW;

                            }
                            return null;
                        }
                    });
                    viewToColorer.put(context, removable);
                    context.getModel().shiftTo(pos[0]);

                } else if (!searchStateDialog.isCanceled())
                    JOptionPane.showMessageDialog(null, "Nothing found");
                searchStateDialog.dispose();
                context.updateRecords();
            }
        };

        return searchAction;
    }
}
