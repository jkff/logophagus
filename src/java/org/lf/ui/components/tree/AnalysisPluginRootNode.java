package org.lf.ui.components.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class AnalysisPluginRootNode extends AnalysisPluginNode {
    private final JTree tree;

    public AnalysisPluginRootNode(JTree tree) {
        super((DefaultMutableTreeNode) tree.getModel().getRoot());
        this.tree = tree;
    }

    @Override
    protected JTree getTree() {
        return tree;
    }
}
