package org.lf.plugins;

import com.thoughtworks.xstream.XStream;
import org.lf.plugins.extension.ExtensionPointsManager;
import org.lf.services.DisplayPluginRepository;
import org.lf.services.TreePluginRepository;
import org.lf.ui.components.tree.PluginTree;

/**
 * Created on: 26.05.2010 15:44:10
 */
public class ProgramContext {
    private TreePluginRepository treePluginRepository = new TreePluginRepository();
    private DisplayPluginRepository displayPluginRepository = new DisplayPluginRepository();
    private ExtensionPointsManager extensionPointsManager = new ExtensionPointsManager();
    private XStream xstream = new XStream();
    private PluginTree pluginTree = new PluginTree(treePluginRepository);

    public ProgramContext() {
    }

    public ExtensionPointsManager getExtensionPointsManager() {
        return extensionPointsManager;
    }

    public PluginTree getPluginTree() {
        return pluginTree;
    }

    public XStream getXstream() {
        return xstream;
    }

    public TreePluginRepository getTreePluginRepository() {
        return treePluginRepository;
    }

    public DisplayPluginRepository getDisplayPluginRepository() {
        return displayPluginRepository;
    }
}
