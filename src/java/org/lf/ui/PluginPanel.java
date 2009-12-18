package org.lf.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class PluginPanel extends JPanel implements TreeSelectionListener {
	public PluginPanel(){
		super(new BorderLayout());
	}
	
	public void valueChanged(TreeSelectionEvent arg0) {
		this.removeAll();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)arg0.getNewLeadSelectionPath().getLastPathComponent();
		if (node != null) {
			NodeData nodeData = (NodeData)node.getUserObject(); 
			if (node != null){
				this.add(nodeData.jComponent);
			}
		}
		this.updateUI();

	}

}
