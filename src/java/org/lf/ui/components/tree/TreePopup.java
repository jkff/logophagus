package org.lf.ui.components.tree;

import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.plugins.analysis.TreeAction;
import org.lf.services.AnalysisPluginRepository;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class TreePopup extends JPopupMenu {
    private final JTree tree;

    public TreePopup(JTree pluginsTree) {
        this.tree = pluginsTree;
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    }

    @Override
    public void show(Component invoker, int x, int y) {
        update();
        super.show(invoker, x, y);
    }

    private void update() {
        this.removeAll();
        final TreeContext context = new TreeContext(tree);
        List<AnalysisPlugin> plugins = AnalysisPluginRepository.getApplicablePlugins(context);
        for (final AnalysisPlugin plugin : plugins) {
            TreeAction treeAction = plugin.getActionFor(context);
            JMenuItem itemPlugin;
            if (treeAction.getAction() != null)
                itemPlugin = new JMenuItem(treeAction.getAction());
            else {
                itemPlugin = new JMenu(treeAction.getName());
                fillByChildren(itemPlugin, treeAction);
            }
            add(itemPlugin);
        }

        final TreePath[] tp = tree.getSelectionPaths();
        if (tp != null) {
            if (this.getComponentCount() != 0)
                addSeparator();
            add(new JMenuItem(new AbstractAction("Delete") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (TreePath cur : tp) {
                        ((DefaultTreeModel) tree.getModel()).removeNodeFromParent((MutableTreeNode) cur.getLastPathComponent());
                    }
                }
            }));
        }
        this.revalidate();
    }


    private void fillByChildren(JMenuItem item, TreeAction itemAction) {
        TreeAction[] subActions = itemAction.getChildren();
        for (TreeAction cur : subActions) {
            if (cur.getAction() != null)
                item.add(new JMenuItem(cur.getAction()));
            else {
                JMenuItem child = new JMenu(cur.getName());
                fillByChildren(child, cur);
                item.add(child);
            }
        }
    }
}
