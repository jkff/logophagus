package org.lf.ui.components.tree;


import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class TreeContext {
    private final JTree tree;
    public final AnalysisPluginNode[] selectedNodes;

    public TreeContext(JTree tree) {
        this.tree = tree;

        TreePath[] tp = tree.getSelectionPaths();
        int length = tp == null ? 0 : tp.length;
        selectedNodes = new AnalysisPluginNode[length];
        for (int i = 0; i < length; ++i) {
            selectedNodes[i] = new AnalysisPluginNode((DefaultMutableTreeNode) tp[i].getLastPathComponent());
        }
    }

    public void addChildTo(final AnalysisPluginNode parent, final NodeData child, final boolean shouldSelect) {
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
                model.nodesWereInserted(parentNode, new int[] {parentNode.getIndex(childNode)});
                if (shouldSelect) {
                    TreePath path = new TreePath(childNode.getPath());
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);
                }
            }
        });
    }
}
