package org.lf.ui.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.DisplayPlugin;
import org.lf.plugins.Entity;
import org.lf.services.AnalysisPluginRepository;
import org.lf.services.DisplayPluginRepository;

public class LogsHierarchy extends Observable{

	private DefaultTreeModel treeModel;
	private NodeData currentNode;

	public LogsHierarchy() {
		treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
	}

	public TreeModel getTreeModel() {
		return treeModel;
	}

	public void addChildToRoot(MutableTreeNode child) {
		addChildToNode(child, (MutableTreeNode) treeModel.getRoot());
	}

	public void addChildToNode(MutableTreeNode child, MutableTreeNode parent) {
		treeModel.insertNodeInto(child, parent, parent.getChildCount());
		setChanged();
		notifyObservers();
	}

	public void removeNode(MutableTreeNode node) {
		treeModel.removeNodeFromParent(node);		
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
//		System.out.println(availabaleDisplays);
		MutableTreeNode childNode = new DefaultMutableTreeNode(new NodeData(res, availabaleDisplays.get(0).createView(res))); 
		if (data.size() == 1) {
			addChildToNode(childNode, (MutableTreeNode)(selPaths[0].getLastPathComponent()));
		} else {
			addChildToRoot(childNode);
		}
	}

	public NodeData getCurrentNodeData() {
		return currentNode;
	}


	private List<Entity> getContentByPaths(TreePath[] selPaths) {
		List<Entity> nodeObjects = new LinkedList<Entity>();
		if (selPaths != null) {
			for (int i=0; i < selPaths.length; ++i){
				DefaultMutableTreeNode cur = (DefaultMutableTreeNode)(selPaths[i].getLastPathComponent());
				NodeData data = (NodeData)(cur.getUserObject());
				nodeObjects.add(data.entity);
			}
		}
		return nodeObjects;
	}

	private void setCurrentNodeData(NodeData nodeData) {
		this.currentNode = nodeData;
	}
	

}
