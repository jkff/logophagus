package org.lf.ui.model;

import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.services.AnalysisPluginRepository;
import org.lf.services.DisplayPluginRepository;

import javax.swing.tree.*;

import java.util.List;
import java.util.Observable;

import static org.lf.util.CollectionFactory.newLinkedList;

public class LogsHierarchy extends Observable{
    private final DefaultTreeModel treeModel;
    private final TreePath rootPath;
    private TreePath lastNewPath;

    public LogsHierarchy() {
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        rootPath = new TreePath(treeModel.getRoot());
        lastNewPath = rootPath;
    }

    public TreeModel getTreeModel() {
        return treeModel;
    }

    public void addChildToRoot(MutableTreeNode child) {
        addChildToNode(child, (MutableTreeNode) treeModel.getRoot());
    }

    public void addChildToNode(MutableTreeNode child, MutableTreeNode parent) {
        treeModel.insertNodeInto(child, parent, parent.getChildCount());
        lastNewPath = new TreePath(((DefaultMutableTreeNode)child).getPath());
        setChanged();
        notifyObservers();
    }

    public void removeNode(MutableTreeNode node) {
        treeModel.removeNodeFromParent(node);
        lastNewPath = rootPath;
        setChanged();
        notifyObservers();
    }

    public void removeNodesByPath(TreePath[] paths) {
        for (TreePath path : paths){
            removeNode(((DefaultMutableTreeNode)path.getLastPathComponent()));
        }
    }

    public List<AnalysisPlugin> getApplicablePlugins(TreePath[] selPaths) {
        List<Entity> pluginArgs = getContentByPaths(selPaths);
        Entity[] argsArray  = (selPaths == null ? new Entity[0] : pluginArgs.toArray(new Entity[0]));
        return AnalysisPluginRepository.getApplicablePlugins(argsArray);
    }

    public void applyPluginForPath(AnalysisPlugin plugin, TreePath[] selPaths) {
        List<Entity> data = getContentByPaths(selPaths);
        Entity res = plugin.applyTo(data.toArray(new Entity[0]));
        if (res == null) return;

        List<DisplayPlugin> availabaleDisplays = DisplayPluginRepository.getApplicablePlugins(res.data);
        MutableTreeNode childNode = new DefaultMutableTreeNode(
                new NodeData(res, availabaleDisplays.get(0).createView(res), plugin.getIcon())); 
        if (data.size() == 1) {
            addChildToNode(childNode, (MutableTreeNode)(selPaths[0].getLastPathComponent()));
        } else {
            addChildToRoot(childNode);
        }
    }

    public TreePath getLastNewPath() {
        return lastNewPath;
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
