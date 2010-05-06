package org.lf.ui.components.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AnalysisPluginTree extends JTree {
    private final TreePopup popup;

    public AnalysisPluginTree() {
        super(new DefaultTreeModel(new DefaultMutableTreeNode()));
        this.setRootVisible(false);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.addMouseListener(new TreeMouseListener());
        this.setCellRenderer(new AnalysisPluginTreeCellRenderer());
        this.popup = new TreePopup(this);
    }

    private class TreeMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath selPath = AnalysisPluginTree.this.getPathForLocation(e.getX(), e.getY());
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (e.isControlDown()) {
                    AnalysisPluginTree.this.addSelectionPath(selPath);
                } else {
                    AnalysisPluginTree.this.setSelectionPath(selPath);
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                boolean isAtSelection = false;
                if (AnalysisPluginTree.this.getSelectionPaths() != null)
                    for (TreePath cur : AnalysisPluginTree.this.getSelectionPaths())
                        if (cur.equals(selPath)) {
                            isAtSelection = true;
                            break;
                        }
                if (!isAtSelection)
                    AnalysisPluginTree.this.setSelectionPath(selPath);
                popup.show(AnalysisPluginTree.this, e.getX(), e.getY());
            }
        }
    }

}
