package org.lf.plugins.analysis.merge;

import org.lf.logs.Log;
import org.lf.logs.TimeMergeLogs;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.plugins.analysis.TreeAction;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.tree.AnalysisPluginNode;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


public class MergeLogsPlugin implements AnalysisPlugin {
    @Override
    public TreeAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length < 2)
            return null;
        for (AnalysisPluginNode cur : context.selectedNodes) {
            if (!(cur.getNodeData().entity.data instanceof Log))
                return null;
        }

        Action action = new AbstractAction(getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Entity> list = new LinkedList<Entity>();
                for (AnalysisPluginNode cur : context.selectedNodes) {
                    list.add(cur.getNodeData().entity);
                }
                Entity entity = getEntity(list.toArray(new Entity[0]));
                if (entity == null)
                    return;
                NodeData nodeData = new NodeData(entity, getIcon());
                context.addChildToRoot(nodeData, true);
            }
        };

       
        return new TreeAction(action);
    }

    @Override
    public String getName() {
        return "Merge logs";
    }


    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "multi.gif");
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
