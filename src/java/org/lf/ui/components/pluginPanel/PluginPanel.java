package org.lf.ui.components.pluginPanel;


import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.lf.ui.model.NodeData;


public class PluginPanel extends JPanel implements TreeSelectionListener {

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		this.removeAll();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
		if (node != null) {
			NodeData nodeData = (NodeData)node.getUserObject();
            this.add(nodeData.component);
        }
		this.updateUI();
	}

}

