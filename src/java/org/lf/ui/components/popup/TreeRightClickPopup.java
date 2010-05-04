package org.lf.ui.components.popup;

import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.ui.model.AnalysisPluginsTreeModel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class TreeRightClickPopup extends JPopupMenu {

    public TreeRightClickPopup(final AnalysisPluginsTreeModel pluginsTreeModel, final TreePath[] selPaths) {
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        List<AnalysisPlugin> plugins = pluginsTreeModel.getApplicablePlugins(selPaths);
        for (final AnalysisPlugin plugin : plugins) {
            JMenuItem itemPlugin = new JMenuItem(plugin.getName());
            itemPlugin.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    pluginsTreeModel.applyPluginForPath(plugin, selPaths);
                }
            });
            add(itemPlugin);
        }

        if (selPaths != null) {
            if (this.getComponentCount() != 0)
                addSeparator();
            JMenuItem itemDelete = new JMenuItem("Delete");
            itemDelete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    pluginsTreeModel.removeNodesByPath(selPaths);
                }
            });
            add(itemDelete);

        }
        ;


    }
}
