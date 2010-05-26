package org.lf.ui.components.plugins.scrollablelog.extension.builtin;

import org.lf.parser.Position;
import org.lf.plugins.tree.BookmarkListener;
import org.lf.plugins.tree.Bookmarks;
import org.lf.ui.components.plugins.scrollablelog.PopupElementProvider;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPanel;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class BookmarkExtension implements SLInitExtension {

    @Override
    public void init(final ScrollableLogPanel.Context context) {

        context.addToolbarElement(getToolbarElement(context));
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

    private JComponent getToolbarElement(ScrollableLogPanel.Context context) {
        JLabel label = new JLabel("Bookmarks");
        final Bookmarks bookmarks = context.getAttributes().getValue(Bookmarks.class);
        BookmarksComboBoxModel model = new BookmarksComboBoxModel(bookmarks);
        final JComboBox bookmarksList = new JComboBox(model);
        bookmarksList.setPrototypeDisplayValue("0123456789");
        bookmarks.addListener(new BookmarkListener() {
            @Override
            public void bookmarkAdd(String name) {
                bookmarksList.setEnabled(true);
                bookmarks.removeListener(this);
            }
        });
        if (bookmarks.getSize() == 0) bookmarksList.setEnabled(false);
        bookmarksList.addActionListener(new ComboBoxActionListener(context));
        Box box = Box.createHorizontalBox();
        box.add(label);
        box.add(Box.createHorizontalStrut(3));
        box.add(bookmarksList);
        return box;
    }

    private HierarchicalAction getHierarchicalActionFor(final ScrollableLogPanel.Context context) {
        if (!context.getModel().isReadingDone()) return null;
        Action action = new AbstractAction("Add to bookmarks") {
            @Override
            public void actionPerformed(ActionEvent e) {
                while (true) {
                    String name = JOptionPane.showInputDialog(
                            null,
                            "Enter bookmark name",
                            "Add bookmark",
                            JOptionPane.QUESTION_MESSAGE);
                    if (name == null) return;

                    try {
                        if (context.getAttributes().getValue(Bookmarks.class).getValue(name) != null) {
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Bookmark with such name already exists. Please input a different name.");
                        } else {
                            int row = context.getSelectedIndexes()[0];
                            context.getAttributes().getValue(Bookmarks.class).addBookmark(name, context.getModel().getPosition(row));
                            return;
                        }
                    } catch (HeadlessException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
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
            if (!context.getModel().isReadingDone()) return;
            JComboBox cb = (JComboBox) e.getSource();
            String selectedBookmark = (String) cb.getSelectedItem();
            if (selectedBookmark == null) return;
            Position pos;
            try {
                pos = context.getAttributes().getValue(Bookmarks.class).getValue(selectedBookmark);
                context.getModel().shiftTo(pos);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
            //TODO think about auto selection
        }
    }

}
