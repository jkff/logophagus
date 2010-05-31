package org.lf.plugins.tree.merge;

import org.lf.logs.Format;
import org.lf.logs.Log;
import org.lf.logs.TimeMergeLogs;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.tree.TreePlugin;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.ui.components.tree.TreePluginNode;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class MergeLogsPlugin implements TreePlugin, Plugin {
    @Override
    public void init(ProgramContext context) {
        context.getTreePluginRepository().register(this);
    }

    @Override
    public HierarchicalAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length < 2)
            return null;

        for (TreePluginNode curNode : context.selectedNodes) {
            if (!(curNode.getNodeData().entity.data instanceof Log))
                return null;

            boolean hasTimeField = false;

            for (Format curFormat : ((Log) curNode.getNodeData().entity.data).getFormats())
                if (curFormat.getTimeFieldIndex() != -1) {
                    hasTimeField = true;
                    break;
                }

            if (!hasTimeField) return null;
        }


        Action action = new AbstractAction(getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Entity> list = new LinkedList<Entity>();
                for (TreePluginNode cur : context.selectedNodes) {
                    list.add(cur.getNodeData().entity);
                }
                Entity entity = getEntity(list.toArray(new Entity[0]));
                if (entity == null)
                    return;
                NodeData nodeData = new NodeData(entity, getIconFilename());
                context.addChildToRoot(nodeData, true);
            }
        };


        return new HierarchicalAction(action);
    }


    @Override
    public String getName() {
        return "Merge logs";
    }

    @Override
    public String getIconFilename() {
        return "multi.gif";
    }

    private Entity getEntity(Entity[] args) {
        Log[] logs = new Log[args.length];
        for (int i = 0; i < args.length; ++i) {
            logs[i] = (Log) args[i].data;
        }

        Log mergedLog;
        try {
            mergedLog = new TimeMergeLogs(logs);

            Attributes[] childAttributes = new Attributes[args.length];
            for (int i = 0; i < args.length; ++i) {
                childAttributes[i] = args[i].attributes;
            }

            Attributes myAttributes = Attributes.join(childAttributes, mergedLog);
            return new Entity(myAttributes, mergedLog);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
