package org.lf.plugins.tree.splitbyfield;


import org.lf.logs.*;
import org.lf.parser.Position;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.tree.TreePlugin;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.ui.components.tree.TreePluginNode;
import org.lf.util.Filter;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SplitByFieldPlugin implements TreePlugin, Plugin {
    @Override
    public void init(ProgramContext context) {
        context.getTreePluginRepository().register(this);
    }

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
            uniqueFields.addAll(Arrays.asList(format.getFields()));

        final TreePluginNode parentNode = context.selectedNodes[0];

        HierarchicalAction rootAction = new HierarchicalAction("Split by ...");

        for (final Field curField : uniqueFields) {
            rootAction.addChild(new HierarchicalAction(new AbstractAction(curField.name) {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    new Thread() {
                        Set<String> uniqueValues = new HashSet<String>();

                        @Override
                        public void run() {
                            try {
                                Position cur = parentLog.first();
                                while (true) {
                                    cur = parentLog.next(cur);
                                    Record rec = parentLog.readRecord(cur);
                                    Field[] fields = rec.getFormat().getFields();
                                    String[] cells = rec.getCellValues();
                                    for (int i = 0; i < fields.length; ++i) {
                                        if (fields[i].equals(curField) && !uniqueValues.contains(cells[i])) {
                                            uniqueValues.add(cells[i]);
                                            Log log = new FilteredLog(parentLog, getFilter(curField, cells[i]));
                                            Attributes attr = parentEntity.attributes.createSuccessor(log);
                                            Entity entity = new Entity(attr, log);
                                            context.addChildTo(parentNode, new NodeData(entity, getIconFilename()), false);
                                        }
                                    }
                                    if (cur.equals(parentLog.last())) break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }));
        }
        return rootAction;
    }

    @Override
    public String getName() {
        return "Split by field values";
    }


    @Override
    public String getIconFilename() {
        return "folder_files.gif";
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

            public String toString() {
                return "split:" + value;
            };
        };
    }
}
