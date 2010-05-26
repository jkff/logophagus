package org.lf.ui.components.plugins.scrollablelog;

import org.lf.plugins.tree.Bookmarks;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class AddBookmarkListener implements ActionListener {
    private final ScrollableLogPanel scrollableLogPanel;

    public AddBookmarkListener(ScrollableLogPanel scrollableLogPanel) {
        this.scrollableLogPanel = scrollableLogPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        while (true) {
            String name = JOptionPane.showInputDialog(
                    scrollableLogPanel,
                    "Enter bookmark name", "Add bookmark", JOptionPane.QUESTION_MESSAGE);
            if (name == null || name.equals("")) return;

            try {
                if (scrollableLogPanel.context.getAttributes().getValue(Bookmarks.class).getValue(name) != null) {
                    JOptionPane.showMessageDialog(
                            scrollableLogPanel,
                            "Bookmark with such name already exists. Please input a different name.");
                } else {
                    int indexes[] = scrollableLogPanel.context.getSelectedIndexes();
                    int row = indexes.length == 0 ? 0 : indexes[0];
                    scrollableLogPanel.context.getAttributes().getValue(Bookmarks.class).addBookmark(name, scrollableLogPanel.context.getModel().getPosition(row));
                    return;
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }
}