package org.lf.ui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class DeleteSelectedNodesFromTree implements ActionListener {
	private DefaultTreeModel treeModel;
	private JTree jTree;

    // TODO Pass only selection paths

	public DeleteSelectedNodesFromTree(DefaultTreeModel treeModel, JTree jTree) {
		this.treeModel = treeModel;
		this.jTree = jTree;
	}

    public void actionPerformed(ActionEvent e) {
		TreePath[] selPaths = jTree.getSelectionPaths();
        for (TreePath selPath : selPaths) {
            treeModel.removeNodeFromParent(((DefaultMutableTreeNode) selPath.getLastPathComponent()));
        }
	}
}
