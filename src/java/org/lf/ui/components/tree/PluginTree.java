package org.lf.ui.components.tree;

import org.lf.plugins.display.View;
import org.lf.services.TreePluginRepository;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PluginTree extends JTree {
    private final TreePopup popup;

    public PluginTree(TreePluginRepository tpr) {
        super(new DefaultTreeModel(new DefaultMutableTreeNode()));
        this.setRootVisible(false);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        this.addMouseListener(new TreeMouseListener());
        this.setCellRenderer(new PluginTreeCellRenderer());
        this.popup = new TreePopup(this, tpr);              
    }

    public void setRoot(Object root) {
        ((DefaultTreeModel)getModel()).setRoot((TreeNode) root);
        ((DefaultTreeModel)getModel()).reload();
        for(int i = 0; i < getRowCount(); ++i) {
            expandRow(i);
        }
    }

    public Object getRoot() {
        return getModel().getRoot();
    }

    /**
     * Find row for an entity whose data is 'data' and which is visualized
     * by a view of class 'viewClass', or return -1 
     */
    public TreePath findPathWithView(Object data, Class<? extends View> viewClass) {
        return findPathWithViewRec(new TreePath(getRoot()), data, viewClass);
    }

    private TreePath findPathWithViewRec(TreePath root, Object data, Class<? extends View> viewClass) {
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) root.getLastPathComponent());
        NodeData nd = (NodeData) node.getUserObject();
        if (nd != null && nd.entity.data.equals(data) && viewClass.isInstance(nd.view))
            return root;
        for (int i = 0; i < node.getChildCount(); ++i) {
            TreePath p = findPathWithViewRec(root.pathByAddingChild(node.getChildAt(i)), data, viewClass);
            if(p != null)
                return p;
        }
        return null;
    }

    private class TreeMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath selPath = PluginTree.this.getPathForLocation(e.getX(), e.getY());
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (e.isControlDown()) {
                    PluginTree.this.addSelectionPath(selPath);
                } else {
                    PluginTree.this.setSelectionPath(selPath);
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                boolean isAtSelection = false;
                if (PluginTree.this.getSelectionPaths() != null) {
                    for (TreePath cur : PluginTree.this.getSelectionPaths()) {
                        if (cur.equals(selPath)) {
                            isAtSelection = true;
                            break;
                        }
                    }
                }
                if (!isAtSelection) {
                    PluginTree.this.setSelectionPath(selPath);
                }
                popup.show(PluginTree.this, e.getX(), e.getY());
            }
        }
    }
}
