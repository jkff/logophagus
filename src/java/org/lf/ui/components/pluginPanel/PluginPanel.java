package org.lf.ui.components.pluginPanel;


import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.lf.ui.model.NodeData;


public class PluginPanel extends JPanel implements TreeSelectionListener {

	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		this.removeAll();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)arg0.getPath().getLastPathComponent();
		if (node != null) {
			NodeData nodeData = (NodeData)node.getUserObject();
            this.add(nodeData.jComponent);
        }
		this.updateUI();

	}

}

