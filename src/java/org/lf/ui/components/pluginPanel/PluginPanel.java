package org.lf.ui.components.pluginPanel;



import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.lf.ui.model.NodeData;


public class PluginPanel extends JPanel implements TreeSelectionListener {
	
	public PluginPanel() {
		super(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(12,4,12,12));
	}

	@Override
	public void valueChanged(TreeSelectionEvent event) {
		this.removeAll();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
		if (node != null) {
			NodeData nodeData = (NodeData)node.getUserObject();
			if (nodeData != null)
				this.add(nodeData.component);
        }
		this.repaint();
		this.updateUI();
	}

}

