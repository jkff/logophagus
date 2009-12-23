package org.lf.ui.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.DisplayPlugin;
import org.lf.services.DisplayPluginRepository;
import org.lf.ui.NodeData;

/**
 * Apply plugin to arguments and attach a node to tree.
 */
public class AttachPluginNodeToTree implements ActionListener {

	private DefaultTreeModel treeModel;
	private AnalysisPlugin plugin;
	private Object[] pluginArgs;
	private JTree jtree;

    // TODO Don't pass the tree, pass only the particular node to which
    // the plugin results must be attached. Compute the node externally.

    // TODO split into 2 classes: one is a generic "add node to tree" class
    // dealing only with Swing, the other computes the node that should be added
    // and almost doesn't depend on Swing at all (this class will apply the plugin
    // and invoke DisplayPluginRepository)
    // In the same fashion as PopupOnClickTree.

	public AttachPluginNodeToTree(
            DefaultMutableTreeNode parent, Object[] pluginArgs, AnalysisPlugin plugin, DefaultTreeModel treeModel)
    {
		this.treeModel = treeModel;
		this.plugin = plugin;
		this.pluginArgs = pluginArgs;
		this.jtree = jtree;
		
		System.out.println("Adaptee");
	}

    public void actionPerformed(ActionEvent unused) {
		Object res = plugin.applyTo(pluginArgs);
		if (res == null) 
			return;
		DisplayPlugin availableDisplays = DisplayPluginRepository.getApplicablePlugins(res).get(0);
		TreePath[] selPaths = jtree.getSelectionPaths();
		addNode((DefaultMutableTreeNode)(
                pluginArgs.length == 1
                ? selPaths[0].getLastPathComponent()
                : treeModel.getRoot()) ,
                new DefaultMutableTreeNode(new NodeData(res, availableDisplays.createView(res))));
	}

    private void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
        if (parent == null) 
            parent = (DefaultMutableTreeNode) treeModel.getRoot();
        treeModel.insertNodeInto(child, parent, parent.getChildCount());
    }

}
