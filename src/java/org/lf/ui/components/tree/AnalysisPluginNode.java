package org.lf.ui.components.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class AnalysisPluginNode {
    private final DefaultMutableTreeNode underlyingNode;
    private AnalysisPluginNode parent;

    public AnalysisPluginNode(DefaultMutableTreeNode node) {
        underlyingNode = node;
    }

    public AnalysisPluginNode(NodeData content) {
        underlyingNode = new DefaultMutableTreeNode(content);
    }

    public void add(AnalysisPluginNode node) {
        add(node, true);
    }

    public void add(final AnalysisPluginNode node, final boolean shouldSelect) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlyingNode.add(node.underlyingNode);
                node.parent = AnalysisPluginNode.this;
                final JTree tree = getTree();
                if (tree != null) {
                    ((DefaultTreeModel) tree.getModel()).reload(underlyingNode);
                    if (shouldSelect) {
                        TreePath path = new TreePath(node.underlyingNode.getPath());
                        tree.setSelectionPath(path);
                        tree.scrollPathToVisible(path);
                    }
                }
            }
        });
    }


    public NodeData getNodeData() {
        return (NodeData) underlyingNode.getUserObject();
    }

    protected JTree getTree() {
        return parent == null ? null : parent.getTree();
    }

}
