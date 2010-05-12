package org.lf.ui.components.plugins.scrollablelog;

import org.lf.plugins.tree.Bookmarks;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AddBookmarkListener implements ActionListener {
    private final ScrollableLogView scrollableLogView;

    public AddBookmarkListener(ScrollableLogView scrollableLogView) {
        this.scrollableLogView = scrollableLogView;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        while (true) {
            String name = JOptionPane.showInputDialog(
                    scrollableLogView,
                    "Enter bookmark name", "Add bookmark", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.equals("")) return;

            try {
                if (scrollableLogView.context.getAttributes().getValue(Bookmarks.class).getValue(name) != null) {
                    JOptionPane.showMessageDialog(
                            scrollableLogView,
                            "Bookmark with such name already exists. Please input a different name.");
                } else {
                    int indexes[] = scrollableLogView.context.getSelectedIndexes();
                    int row = indexes.length == 0 ? 0 : indexes[0];
                    scrollableLogView.context.getAttributes().getValue(Bookmarks.class).addBookmark(name, scrollableLogView.context.getModel().getPosition(row));
                    return;
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }
}