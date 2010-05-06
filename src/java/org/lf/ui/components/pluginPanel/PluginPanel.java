package org.lf.ui.components.pluginPanel;


import org.lf.plugins.display.DisplayPlugin;
import org.lf.services.DisplayPluginRepository;
import org.lf.ui.components.tree.NodeData;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.List;


public class PluginPanel extends JPanel implements TreeSelectionListener {

    public PluginPanel() {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(12, 4, 12, 12));
    }

    @Override
    public void valueChanged(TreeSelectionEvent event) {
        this.removeAll();
        JTree tree = (JTree) event.getSource();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
            NodeData nodeData = (NodeData) node.getUserObject();
            if (nodeData != null) {
                if (nodeData.component == null) {
                    List<DisplayPlugin> dPlugins = DisplayPluginRepository.getApplicablePlugins(nodeData.entity.data);
                    nodeData.component = dPlugins.get(0).createView(nodeData.entity);
                }
                this.add(nodeData.component);
            }
        }
        this.revalidate();
        this.repaint();
    }

}

