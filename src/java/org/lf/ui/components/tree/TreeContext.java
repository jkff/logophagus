package org.lf.ui.components.tree;


import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class TreeContext {
    public final AnalysisPluginNode[] selectedNodes;
    public final AnalysisPluginNode root;

    public TreeContext(JTree tree) {

        TreePath[] tp = tree.getSelectionPaths();
        int length = tp == null ? 0 : tp.length;
        selectedNodes = new AnalysisPluginNode[length];
        for (int i = 0; i < length; ++i) {
            selectedNodes[i] = new AnalysisPluginNode((DefaultMutableTreeNode) tp[i].getLastPathComponent());
        }
        root = new AnalysisPluginRootNode(tree);
    }
}
