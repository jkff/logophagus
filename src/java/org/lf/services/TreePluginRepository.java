package org.lf.services;

import org.lf.plugins.extension.ExtensionPoint;
import org.lf.plugins.extension.ExtensionPointID;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.plugins.tree.TreePlugin;
import org.lf.ui.components.tree.TreeContext;
import org.lf.util.Removable;

import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class TreePluginRepository {
    public final static ExtensionPointID<TreePlugin> EXTENSION_POINT_ID = ExtensionPointID.create();

    private final static ExtensionPoint<TreePlugin> EXTENSION_POINT = new ExtensionPoint<TreePlugin>() {
        @Override
        public Removable addExtension(final TreePlugin extension) {
            TreePluginRepository.register(extension);
            return new Removable() {
                @Override
                public void remove() {
                    treePlugins.remove(extension);
                }
            };
        }
    };

    static {
        ExtensionPointsManager.registerExtensionPoint(EXTENSION_POINT_ID, EXTENSION_POINT);
    }

    private static List<TreePlugin> treePlugins = newList();

    public static void register(TreePlugin plugin) {
        treePlugins.add(plugin);
    }

    public static List<TreePlugin> getApplicablePlugins(TreeContext context) {
        List<TreePlugin> res = newList();
        for (TreePlugin plugin : treePlugins) {
            if (plugin.getActionFor(context) != null)
                res.add(plugin);
        }
        return res;
    }
}
