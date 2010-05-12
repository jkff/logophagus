package org.lf.ui.components.plugins.scrollablelog.extension.builtin;

import org.lf.parser.Position;
import org.lf.plugins.tree.Bookmarks;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;
import org.lf.ui.components.plugins.scrollablelog.extension.SLKeyListener;
import org.lf.ui.components.plugins.scrollablelog.extension.SLPopupExtension;
import org.lf.ui.components.plugins.scrollablelog.extension.SLToolbarExtension;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class BookmarkExtension implements SLToolbarExtension, SLPopupExtension, SLKeyListener {

    @Override
    public JComponent getToolbarElement(ScrollableLogView.Context context) {
        JLabel label = new JLabel("Bookmarks");
        BookmarksComboBoxModel model = new BookmarksComboBoxModel(context.getAttributes().getValue(Bookmarks.class));
        final JComboBox bookmarksList = new JComboBox(model);
        bookmarksList.setPrototypeDisplayValue("0123456789");
        bookmarksList.addActionListener(new ComboBoxActionListener(context));
        //update bookmarks before any actions
        bookmarksList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                ((BookmarksComboBoxModel) bookmarksList.getModel()).update();
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(label);
        box.add(Box.createHorizontalStrut(3));
        box.add(bookmarksList);
        return box;
    }

    @Override
    public HierarchicalAction getHierarchicalActionFor(final ScrollableLogView.Context context) {
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

    @Override
    public void keyTyped(KeyEvent e, ScrollableLogView.Context context) {
        if (e.getKeyCode() == (KeyEvent.CTRL_MASK | KeyEvent.VK_B)) {
            getHierarchicalActionFor(context).getAction().actionPerformed(null);
        }
    }

    @Override
    public void keyPressed(KeyEvent e, ScrollableLogView.Context context) {
    }

    @Override
    public void keyReleased(KeyEvent e, ScrollableLogView.Context context) {
    }


    class ComboBoxActionListener implements ActionListener {
        private final ScrollableLogView.Context context;

        ComboBoxActionListener(ScrollableLogView.Context context) {
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
