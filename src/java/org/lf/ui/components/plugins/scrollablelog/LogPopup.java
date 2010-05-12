package org.lf.ui.components.plugins.scrollablelog;

import org.lf.ui.components.plugins.scrollablelog.extension.SLPluginsRepository;
import org.lf.ui.components.plugins.scrollablelog.extension.SLPopupExtension;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.*;

class LogPopup extends JPopupMenu {
    private final ScrollableLogView.Context context;

    LogPopup(ScrollableLogView.Context context) {
        this.context = context;
    }

    @Override
    public void show(Component invoker, int x, int y) {
        update();
        if (this.getComponentCount() != 0)
            super.show(invoker, x, y);
    }

    private void update() {
        this.removeAll();
        SLPopupExtension[] extensions = SLPluginsRepository.getRegisteredPopupExtensions();

        for (final SLPopupExtension cur : extensions) {
            HierarchicalAction treeAction = cur.getHierarchicalActionFor(context);
            if (treeAction == null) continue;
            JMenuItem itemPlugin;
            if (treeAction.getAction() != null)
                itemPlugin = new JMenuItem(treeAction.getAction());
            else {
                itemPlugin = new JMenu(treeAction.getName());
                fillByChildren(itemPlugin, treeAction);
            }
            add(itemPlugin);
        }

        this.revalidate();
    }

    private void fillByChildren(JMenuItem item, HierarchicalAction itemAction) {
        HierarchicalAction[] subActions = itemAction.getChildren();
        for (HierarchicalAction cur : subActions) {
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
