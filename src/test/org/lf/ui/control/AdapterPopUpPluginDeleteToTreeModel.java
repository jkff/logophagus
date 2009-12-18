package org.lf.ui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class AdapterPopUpPluginDeleteToTreeModel implements ActionListener {
	private DefaultTreeModel treeModel;
	private JTree jTree;

	public AdapterPopUpPluginDeleteToTreeModel(DefaultTreeModel treeModel, JTree jTree) {
		this.treeModel = treeModel;
		this.jTree = jTree;
	}
	
	public void actionPerformed(ActionEvent e) {
		TreePath[] selPaths = jTree.getSelectionPaths();
		for (int i=0; i < selPaths.length; ++i){
				treeModel.removeNodeFromParent(((DefaultMutableTreeNode)selPaths[i].getLastPathComponent()));
			}
	}

}
