package org.lf.plugins.tree.filtersubstr;

import org.lf.logs.FilteredLog;
import org.lf.logs.Format;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.Entity;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.tree.TreePlugin;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.util.Filter;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FilterByCriteriaPlugin implements TreePlugin, Plugin {
    @Override
    public void init(ProgramContext context) {
        context.getTreePluginRepository().register(this);
    }

    @Override
    public HierarchicalAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 1 ||
                !(context.selectedNodes[0].getNodeData().entity.data instanceof Log))
            return null;

        final NodeData parentNodeData = context.selectedNodes[0].getNodeData();

        HierarchicalAction root = new HierarchicalAction("Filter by...");

        Action filterBySubstringAction = new AbstractAction("Substring") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = getEntity(parentNodeData.entity, true);
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIconFilename());
                context.addChildTo(context.selectedNodes[0], nodeData, true);
            }
        };
        root.addChild(new HierarchicalAction(filterBySubstringAction));


        Action filterByFormatAction = new AbstractAction("Format") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = getFormatEntity(parentNodeData.entity);
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIconFilename());
                context.addChildTo(context.selectedNodes[0], nodeData, true);
            }
        };
        root.addChild(new HierarchicalAction(filterByFormatAction));

        Action filterByRegexpAction = new AbstractAction("Regexp") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = getEntity(parentNodeData.entity, false);
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIconFilename());
                context.addChildTo(context.selectedNodes[0], nodeData, true);
            }
        };
        root.addChild(new HierarchicalAction(filterByRegexpAction));

        return root;

    }

    public String getName() {
        return "Filter";
    }

    @Override
    public String getIconFilename() {
        return "filter.gif";
    }

    private Entity getEntity(Entity parent, final boolean substringNotRegexp) {
        Log log = (Log) parent.data;

        String message = substringNotRegexp ? "Enter substring for filter" : "Enter regexp for filter";
        final String input = JOptionPane.showInputDialog(
                null,
                message,
                "Filter setup",
                JOptionPane.QUESTION_MESSAGE);
        if (input == null)
            return null;
        Filter<Record> filter = new Filter<Record>() {
            public String toString() {
                return input;
            }

            public boolean accepts(Record r) {
                for (String cell : r.getCellValues()) {
                    if (cell != null &&
                            substringNotRegexp ? cell.contains(input) : cell.matches(input))
                        return true;
                }
                return false;
            }
        };

        Log fLog = new FilteredLog(log, filter);
        return new Entity(parent.attributes.createSuccessor(fLog), fLog);
    }

    private Entity getFormatEntity(Entity parent) {
        final Log log = (Log) parent.data;

        Format[] formats = log.getFormats();
        boolean hasUnknown = false;
        for (Format format : formats) {
            if (format.equals(Format.UNKNOWN_FORMAT)) {
                hasUnknown = true;
                break;
            }
        }

        Format[] formatsWithUnknown = new Format[hasUnknown ? formats.length : formats.length + 1];
        if (hasUnknown) {
            formatsWithUnknown = formats;
        } else {
            formatsWithUnknown[0] = Format.UNKNOWN_FORMAT;
            System.arraycopy(formats, 0, formatsWithUnknown, 1, formats.length);
        }

        if (formatsWithUnknown.length == 1) return null;
        final Format selected = (Format) JOptionPane.showInputDialog(
                null,
                "Select field",
                "Setup",
                JOptionPane.PLAIN_MESSAGE,
                null,
                formatsWithUnknown,
                null);

        if (selected == null)
            return null;
        Filter<Record> filter = new Filter<Record>() {
            public String toString() {
                return "Format:" + selected.toString();
            }

            public boolean accepts(Record r) {
                return r.getFormat().equals(selected);
            }
        };

        Log fLog = new FilteredLog(log, filter);
        return new Entity(parent.attributes.createSuccessor(fLog), fLog);
    }
}
