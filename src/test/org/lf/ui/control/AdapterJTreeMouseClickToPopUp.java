package org.lf.ui.control;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.lf.plugins.AnalysisPlugin;
import org.lf.services.AnalysisPluginRepository;
import org.lf.ui.NodeData;

public class AdapterJTreeMouseClickToPopUp extends MouseAdapter {

	private DefaultTreeModel treeModel;
	private JTree jTree;

	public AdapterJTreeMouseClickToPopUp(DefaultTreeModel myModel, JTree jTree) {
		this.treeModel = myModel;
		this.jTree = jTree;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3 ){
			JPopupMenu popMenu = new JPopupMenu();
			TreePath[] selPaths = jTree.getSelectionPaths();

			List<Object> analysisArgs = new LinkedList<Object>();
			for (int i=0; i < selPaths.length; ++i){
				DefaultMutableTreeNode cur = (DefaultMutableTreeNode)(selPaths[i].getLastPathComponent());
				NodeData data = (NodeData)(cur.getUserObject());
				analysisArgs.add(data.data);
			}

			List<AnalysisPlugin> availablePlugins = AnalysisPluginRepository.getApplicablePlugins(selPaths == null ? new Object[]{}:analysisArgs.toArray());

			for (int i=0; i < availablePlugins.size(); ++i){
				AnalysisPlugin plugin = availablePlugins.get(i);
				JMenuItem item = new JMenuItem((selPaths==null? "Add ":"Apply ") + plugin.getName());
				item.addActionListener( new AdapterPopUpPluginSelectToTreeModel(jTree,analysisArgs.toArray(), plugin, treeModel));
				popMenu.add(item);
			}

			if (selPaths != null) {
				popMenu.addSeparator();
				JMenuItem itemDelete = new JMenuItem("Delete");
				itemDelete.addActionListener(new AdapterPopUpPluginDeleteToTreeModel(treeModel, jTree));
				popMenu.add(itemDelete);
			}
			popMenu.show(jTree, e.getX(), e.getY());
		}

	}

}
