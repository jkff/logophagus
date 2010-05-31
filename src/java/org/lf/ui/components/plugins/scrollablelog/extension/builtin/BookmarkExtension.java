package org.lf.ui.components.plugins.scrollablelog.extension.builtin;

import org.lf.parser.Position;
import org.lf.plugins.tree.Bookmarks;
import org.lf.ui.components.plugins.scrollablelog.PopupElementProvider;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPanel;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.TimerTask;

public class BookmarkExtension implements SLInitExtension {
    private static java.util.Timer toolbarUpdateTimer =
            new java.util.Timer("Bookmarks toolbar item updater", true); 

    @Override
    public void init(final ScrollableLogPanel.Context context) {
        context.addToolbarElement(createToolbarElement(context));
        context.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == (KeyEvent.CTRL_MASK | KeyEvent.VK_B)) {
                    getHierarchicalActionFor(context).getAction().actionPerformed(null);
                }
            }
        });

        context.addPopupElementProvider(new PopupElementProvider() {
            @Override
            public HierarchicalAction getHierarchicalAction() {
                return getHierarchicalActionFor(context);
            }
        });
    }

    private JComponent createToolbarElement(ScrollableLogPanel.Context context) {
        JLabel label = new JLabel("Bookmarks");

        final Bookmarks bookmarks = context.getAttributes().getValue(Bookmarks.class);
        final BookmarksComboBoxModel model = new BookmarksComboBoxModel(bookmarks);

        final JComboBox bookmarksComboBox = new JComboBox(model);
        bookmarksComboBox.setPrototypeDisplayValue("0123456789");

        toolbarUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                model.update();
                if (model.getSize() != 0)
                    bookmarksComboBox.setEnabled(true);
                else
                    bookmarksComboBox.setEnabled(false);
            }
        }, 0, 100);

        bookmarksComboBox.addActionListener(new ComboBoxActionListener(context));
        Box box = Box.createHorizontalBox();
        box.add(label);
        box.add(Box.createHorizontalStrut(3));
        box.add(bookmarksComboBox);
        return box;
    }

    private HierarchicalAction getHierarchicalActionFor(final ScrollableLogPanel.Context context) {
        Action action = new AbstractAction("Add to bookmarks") {
            @Override
            public void actionPerformed(ActionEvent evt) {
                while (true) {
                    String name = JOptionPane.showInputDialog(
                            null,
                            "Enter bookmark name",
                            "Add bookmark",
                            JOptionPane.QUESTION_MESSAGE);
                    if (name == null) return;

                    try {
                        Bookmarks bookmarks = context.getAttributes().getValue(Bookmarks.class);
                        if (bookmarks == null)
                            return;
                        if (bookmarks.getValue(name) != null) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Bookmark with such name already exists. Please input a different name.");
                        } else {
                            int row = context.getSelectedIndexes()[0];
                            bookmarks.addBookmark(
                                    name, context.getModel().getPosition(row));
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        return new HierarchicalAction(action);
    }

    class ComboBoxActionListener implements ActionListener {
        private final ScrollableLogPanel.Context context;

        ComboBoxActionListener(ScrollableLogPanel.Context context) {
            this.context = context;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox) e.getSource();
            String selectedBookmark = (String) cb.getSelectedItem();
            if (selectedBookmark == null) return;
            final Position pos;
            try {
                Bookmarks bookmarks = context.getAttributes().getValue(Bookmarks.class);
                if(bookmarks == null)
                    return;
                pos = bookmarks.getValue(selectedBookmark);
                context.getModel().shiftTo(pos, new Runnable() {
                    @Override
                    public void run() {
                        context.selectPosition(pos);
                    }
                });
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            //TODO think about auto selection
        }
    }

}
