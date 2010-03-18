package org.lf.ui.components.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import org.lf.plugins.AnalysisPlugin;
import org.lf.ui.model.LogsHierarchy;


public class TreeRightClickPopup extends JPopupMenu {

    public TreeRightClickPopup(final LogsHierarchy logsHierarchy, final TreePath[] selPaths) {
        List<AnalysisPlugin> plugins = logsHierarchy.getApplicablePlugins(selPaths);
        for (final AnalysisPlugin plugin : plugins) {
            JMenuItem itemPlugin = new JMenuItem(plugin.getName());
            itemPlugin.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    logsHierarchy.applyPluginForPath(plugin, selPaths);
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
                    logsHierarchy.removeNodesByPath(selPaths);
                }
            });
            add(itemDelete);

        };


    }
}
