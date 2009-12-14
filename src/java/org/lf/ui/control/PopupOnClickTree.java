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

/**
 * Creates a pop-up when the tree is clicked
 */
public class PopupOnClickTree extends MouseAdapter {

	private DefaultTreeModel treeModel;
	private JTree jTree;

	public PopupOnClickTree(DefaultTreeModel myModel, JTree jTree) {
		this.treeModel = myModel;
		this.jTree = jTree;
	}

    @Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3 ){
            TreePath[] selPaths = jTree.getSelectionPaths();

			JPopupMenu popMenu = new JPopupMenu();

			List<Object> analysisArgs = new LinkedList<Object>();
            for (TreePath selPath : selPaths) {
                DefaultMutableTreeNode cur = (DefaultMutableTreeNode) (selPath.getLastPathComponent());
                NodeData data = (NodeData) (cur.getUserObject());
                analysisArgs.add(data.data);
            }

			List<AnalysisPlugin> availablePlugins = AnalysisPluginRepository.getApplicablePlugins(
                    analysisArgs.toArray());

            for (AnalysisPlugin plugin : availablePlugins) {
                JMenuItem item = new JMenuItem("Apply " + plugin.getName());
                item.addActionListener(new AttachPluginNodeToTree(jTree, analysisArgs.toArray(), plugin, treeModel));
                popMenu.add(item);
            }

            popMenu.addSeparator();
            JMenuItem itemDelete = new JMenuItem("Delete");
            itemDelete.addActionListener(new DeleteSelectedNodesFromTree(treeModel, jTree));
            popMenu.add(itemDelete);
            popMenu.show(jTree, e.getX(), e.getY());
		}

	}

}
