package org.lf.ui.components.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public final class TreePluginNode {
    final DefaultMutableTreeNode underlyingNode;

    public TreePluginNode(DefaultMutableTreeNode node) {
        this.underlyingNode = node;
    }

    public NodeData getNodeData() {
        return (NodeData) underlyingNode.getUserObject();
    }
}
