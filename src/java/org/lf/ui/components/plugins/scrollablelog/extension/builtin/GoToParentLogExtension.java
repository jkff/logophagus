package org.lf.ui.components.plugins.scrollablelog.extension.builtin;

import org.lf.logs.Log;
import org.lf.parser.Position;
import org.lf.ui.components.plugins.scrollablelog.PopupElementProvider;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPanel;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogView;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.PluginTree;
import org.lf.util.HierarchicalAction;
import org.lf.util.Pair;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Created on: 31.05.2010 21:32:21
 */
public class GoToParentLogExtension implements SLInitExtension {
    @Override
    public void init(final ScrollableLogPanel.Context context) {
        context.addPopupElementProvider(new PopupElementProvider() {
            @Override
            public HierarchicalAction getHierarchicalAction() {
                int[] sinds = context.getSelectedIndexes();
                if(sinds.length != 1)
                    return null;
                final int sind = sinds[0];

                return new HierarchicalAction(new AbstractAction("Go to parent log") {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        Position pos = context.getModel().getPosition(sind);
                        if(pos == null)
                            return;
                        try {
                            Pair<Log,Position> parent = pos.getCorrespondingLog().convertToParent(pos);
                            if(parent == null)
                                return;
                            PluginTree pluginTree = context.getPluginTree();
                            navigateTo(pluginTree, parent.first, parent.second);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private static void navigateTo(PluginTree pluginTree, Log log, final Position pos) {
        TreePath path = pluginTree.findPathWithView(log, ScrollableLogView.class);
        if(path == null)
            return;
        pluginTree.setSelectionPath(path);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        NodeData data = (NodeData) node.getUserObject();
        ScrollableLogView v = (ScrollableLogView) data.view;
        final ScrollableLogPanel panel = (ScrollableLogPanel) v.getComponent();
        panel.getLogSegmentModel().shiftTo(pos, new Runnable() {
            @Override
            public void run() {
                panel.selectPosition(pos);
            }
        });
    }
}
