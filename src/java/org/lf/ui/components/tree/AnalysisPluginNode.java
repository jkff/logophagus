package org.lf.ui.components.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public final class AnalysisPluginNode {
    final DefaultMutableTreeNode underlyingNode;

    public AnalysisPluginNode(DefaultMutableTreeNode node) {
        this.underlyingNode = node;
    }

    public NodeData getNodeData() {
        return (NodeData) underlyingNode.getUserObject();
    }
}
