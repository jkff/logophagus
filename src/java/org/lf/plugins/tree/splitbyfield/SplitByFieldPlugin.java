package org.lf.plugins.tree.splitbyfield;


import org.lf.logs.*;
import org.lf.parser.Position;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.tree.TreePlugin;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.ui.components.tree.TreePluginNode;
import org.lf.util.Filter;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SplitByFieldPlugin implements TreePlugin {

    @Override
    public HierarchicalAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 1 ||
                !(context.selectedNodes[0].getNodeData().entity.data instanceof Log)) {
            return null;
        }

        final Entity parentEntity = context.selectedNodes[0].getNodeData().entity;
        final Log parentLog = (Log) context.selectedNodes[0].getNodeData().entity.data;

        final Set<Field> uniqueFields = new HashSet<Field>();
        for (Format format : parentLog.getFormats())
            for (Field field : format.getFields())
                uniqueFields.add(field);

        final TreePluginNode parentNode = context.selectedNodes[0];

        HierarchicalAction rootAction = new HierarchicalAction("Split by ...");

        for (final Field curField : uniqueFields) {
            Action fieldSplitAction = new AbstractAction(curField.name) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread() {
                        Set<String> uniqueValues = new HashSet<String>();

                        @Override
                        public void run() {
                            try {
                                Position cur = parentLog.first();
                                do {
                                    cur = parentLog.next(cur);
                                    Record rec = parentLog.readRecord(cur);
                                    for (int i = 0; i < rec.getFormat().getFields().length; ++i) {
                                        if (rec.getFormat().getFields()[i].equals(curField) &&
                                                !uniqueValues.contains(rec.getCellValues()[i])) {
                                            uniqueValues.add(rec.getCellValues()[i]);
                                            Log log = new FilteredLog(parentLog, getFilter(curField, rec.getCellValues()[i]));
                                            Attributes attr = parentEntity.attributes.createSuccessor(log);
                                            Entity entity = new Entity(attr, log);
                                            context.addChildTo(parentNode, new NodeData(entity, getIcon()), false);
                                        }
                                    }
                                    if (cur.equals(parentLog.last())) break;
                                } while (true);
                            } catch (IOException e1) {
                                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }
                    }.start();
                }
            };

            rootAction.addChild(new HierarchicalAction(fieldSplitAction));
        }
        return rootAction;
    }


    @Override
    public String getName() {
        return "Split by field values";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "folder_files.gif");
    }


    private Filter<Record> getFilter(final Field field, final String value) {
        return new Filter<Record>() {
            @Override
            public boolean accepts(Record r) {
                for (int i = 0; i < r.getFormat().getFields().length; ++i)
                    if (r.getFormat().getFields()[i].equals(field) &&
                            r.getCellValues()[i].equals(value))
                        return true;
                return false;

            }
        };
    }


}
