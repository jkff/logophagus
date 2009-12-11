package org.lf.ui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.DisplayPlugin;
import org.lf.services.DisplayPluginRepository;
import org.lf.ui.NodeData;

public class AdapterPopUpPluginSelectToTreeModel implements ActionListener {

	private DefaultTreeModel treeModel;
	private AnalysisPlugin plugin;
	private Object[] pluginArgs;
	private JTree jtree;
	
	public AdapterPopUpPluginSelectToTreeModel(JTree jtree, Object[] pluginArgs, AnalysisPlugin plugin, DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
		this.plugin = plugin;
		this.pluginArgs = pluginArgs;
		this.jtree = jtree;
		
		System.out.println("Adaptee");
	}

	public void actionPerformed(ActionEvent arg0) {
		Object res = plugin.applyTo(pluginArgs);
		if (res == null) 
			return;
		DisplayPlugin availableDisplays = DisplayPluginRepository.getApplicablePlugins(res).get(0);
		TreePath[] selPaths = jtree.getSelectionPaths();
		addNode((DefaultMutableTreeNode)(pluginArgs.length == 1 ? selPaths[0].getLastPathComponent() : treeModel.getRoot()) , new DefaultMutableTreeNode(new NodeData(res, availableDisplays.createView(res))));
	}

    private void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
        if (parent == null) 
            parent = (DefaultMutableTreeNode) treeModel.getRoot();
        treeModel.insertNodeInto(child, parent, parent.getChildCount());
    }

}
