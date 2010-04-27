package org.lf.ui.components.tree;


import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;


public class TreeSelectionController implements TreeModelListener {
    private final JTree tree;

    public TreeSelectionController(JTree tree) {
        this.tree = tree;
        this.tree.setScrollsOnExpand(true);
        this.tree.getModel().addTreeModelListener(this);
    }

    @Override
    public void treeNodesChanged(TreeModelEvent treeModelEvent) {
    }

    @Override
    public void treeNodesInserted(TreeModelEvent treeModelEvent) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) (treeModelEvent.getTreePath().getLastPathComponent());
        int index = treeModelEvent.getChildIndices()[0];
        node = (DefaultMutableTreeNode) (node.getChildAt(index));
        TreePath tp = new TreePath(node.getPath());
        this.tree.setSelectionPath(tp);
        this.tree.scrollPathToVisible(tp);
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent treeModelEvent) {
        TreePath tp = treeModelEvent.getTreePath();
        this.tree.setSelectionPath(tp);
        this.tree.scrollPathToVisible(tp);
    }

    @Override
    public void treeStructureChanged(TreeModelEvent treeModelEvent) {
    }


}
