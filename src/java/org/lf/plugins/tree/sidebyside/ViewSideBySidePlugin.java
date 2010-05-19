package org.lf.plugins.tree.sidebyside;

import org.lf.logs.Log;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.tree.TreePlugin;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class ViewSideBySidePlugin implements TreePlugin {

    @Override
    public HierarchicalAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 2 ||
                !(context.selectedNodes[0].getNodeData().entity.data instanceof Log) ||
                !(context.selectedNodes[1].getNodeData().entity.data instanceof Log)) {
            return null;
        }

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity first = context.selectedNodes[0].getNodeData().entity;
                Entity second = context.selectedNodes[1].getNodeData().entity;
                Entity entity = new Entity(new Attributes(), new LogsPair(first, second));
                NodeData nodeData = new NodeData(entity, getIcon());
                context.addChildToRoot(nodeData, true);
            }
        };

        action.putValue(Action.NAME, getName());
        return new HierarchicalAction(action);
    }

    public String getName() {
        return "Show logs side by side";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "folder_files.gif");
    }
}
