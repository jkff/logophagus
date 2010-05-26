package org.lf.services;

import org.lf.plugins.tree.TreePlugin;
import org.lf.ui.components.tree.TreeContext;

import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class TreePluginRepository {
    private static List<TreePlugin> treePlugins = newList();

    public void register(TreePlugin plugin) {
        treePlugins.add(plugin);
    }

    public List<TreePlugin> getApplicablePlugins(TreeContext context) {
        List<TreePlugin> res = newList();
        for (TreePlugin plugin : treePlugins) {
            if (plugin.getActionFor(context) != null)
                res.add(plugin);
        }
        return res;
    }
}
