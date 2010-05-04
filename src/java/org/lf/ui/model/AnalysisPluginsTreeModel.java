package org.lf.ui.model;

import org.lf.plugins.Entity;
import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.plugins.display.DisplayPlugin;
import org.lf.services.AnalysisPluginRepository;
import org.lf.services.DisplayPluginRepository;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.List;

import static org.lf.util.CollectionFactory.newLinkedList;

public class AnalysisPluginsTreeModel extends DefaultTreeModel {

    public AnalysisPluginsTreeModel() {
        super(new DefaultMutableTreeNode());
    }

    public void addChildToRoot(MutableTreeNode child) {
        addChildToNode(child, (MutableTreeNode) this.getRoot());
    }

    public void addChildToNode(MutableTreeNode child, MutableTreeNode parent) {
        this.insertNodeInto(child, parent, parent.getChildCount());
    }

    public void removeNode(DefaultMutableTreeNode node) {
        this.removeNodeFromParent(node);
    }

    public void removeNodesByPath(TreePath[] paths) {
        for (TreePath path : paths) {
            removeNode(((DefaultMutableTreeNode) path.getLastPathComponent()));
        }
    }

    public List<AnalysisPlugin> getApplicablePlugins(TreePath[] selPaths) {
        List<Entity> pluginArgs = getContentByPaths(selPaths);
        Entity[] argsArray = selPaths == null ? new Entity[0] : pluginArgs.toArray(new Entity[0]);
        return AnalysisPluginRepository.getApplicablePlugins(argsArray);
    }

    public void applyPluginForPath(AnalysisPlugin plugin, TreePath[] selPaths) {
        List<Entity> data = getContentByPaths(selPaths);
        Entity res = plugin.applyTo(data.toArray(new Entity[0]));
        if (res == null) return;

        List<DisplayPlugin> availableDisplays = DisplayPluginRepository.getApplicablePlugins(res.data);
        MutableTreeNode childNode = new DefaultMutableTreeNode(
                new NodeData(res, availableDisplays.get(0).createView(res), plugin.getIcon()));
        if (data.size() == 1) {
            addChildToNode(childNode, (MutableTreeNode) (selPaths[0].getLastPathComponent()));
        } else {
            addChildToRoot(childNode);
        }
    }

    private List<Entity> getContentByPaths(TreePath[] selPaths) {
        List<Entity> nodeObjects = newLinkedList();
        if (selPaths != null) {
            for (TreePath selPath : selPaths) {
                DefaultMutableTreeNode cur = (DefaultMutableTreeNode) (selPath.getLastPathComponent());
                NodeData data = (NodeData) (cur.getUserObject());
                nodeObjects.add(data.entity);
            }
        }
        return nodeObjects;
    }
}
