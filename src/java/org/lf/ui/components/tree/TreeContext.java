package org.lf.ui.components.tree;


import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class TreeContext {
    private final JTree tree;
    public final TreePluginNode[] selectedNodes;

    public TreeContext(JTree tree) {
        this.tree = tree;

        TreePath[] tp = tree.getSelectionPaths();
        if(tp == null) {
            selectedNodes = new TreePluginNode[0];
        } else {
            selectedNodes = new TreePluginNode[tp.length];
            for (int i = 0; i < tp.length; ++i) {
                selectedNodes[i] = new TreePluginNode((DefaultMutableTreeNode) tp[i].getLastPathComponent());
            }
        }
    }

    public void addChildTo(final TreePluginNode parent, final NodeData child, final boolean shouldSelect) {
        addChildToTreeNode(parent.underlyingNode, child, shouldSelect);
    }

    public void addChildToRoot(NodeData child, boolean shouldSelect) {
        addChildToTreeNode((DefaultMutableTreeNode) tree.getModel().getRoot(), child, shouldSelect);
    }

    private void addChildToTreeNode(final DefaultMutableTreeNode parentNode, final NodeData child, final boolean shouldSelect) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

                parentNode.add(childNode);
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.nodesWereInserted(parentNode, new int[]{parentNode.getIndex(childNode)});
                if (shouldSelect) {
                    TreePath path = new TreePath(childNode.getPath());
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);
                }
            }
        });
    }
}
