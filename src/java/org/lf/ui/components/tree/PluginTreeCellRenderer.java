package org.lf.ui.components.tree;


import org.lf.services.ProgramProperties;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

class PluginTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus)
    {
        Object nodeValue = ((DefaultMutableTreeNode) value).getUserObject();
        JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (nodeValue != null) {
            if (nodeValue.getClass() != NodeData.class)
                return label;
            label.setIcon(new ImageIcon(ProgramProperties.getIconsPath()+((NodeData) nodeValue).iconFilename));
        }
        return label;
    }

}
