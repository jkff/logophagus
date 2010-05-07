package org.lf.plugins.analysis.filtersubstr;

import org.lf.logs.FilteredLog;
import org.lf.logs.Format;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.plugins.analysis.TreeAction;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.tree.AnalysisPluginNode;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.util.Filter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FilterByCriteriaPlugin implements AnalysisPlugin {
    @Override
    public TreeAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 1 ||
                !(context.selectedNodes[0].getNodeData().entity.data instanceof Log))
            return null;

        final NodeData parentNodeData = context.selectedNodes[0].getNodeData();


        Action filterBySubstringAction = new AbstractAction("Substring") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = getSubstringEntity(parentNodeData.entity);
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIcon());
                context.addChildTo(context.selectedNodes[0], nodeData, true);
            }
        };

        Action filterByFormatAction = new AbstractAction("Format") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = getFormatEntity(parentNodeData.entity);
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIcon());
                context.addChildTo(context.selectedNodes[0], nodeData, true);
            }
        };

        TreeAction root = new TreeAction("Filter by...");
        root.addChild(new TreeAction(filterBySubstringAction));
        root.addChild(new TreeAction(filterByFormatAction));
        return root;

    }

    public String getName() {
        return "Filter";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "filter.gif");
    }

    private Entity getSubstringEntity(Entity parent) {
        Log log = (Log) parent.data;

        String substring = JOptionPane.showInputDialog(null,
                "Enter substring for filter", "Filter setup", JOptionPane.QUESTION_MESSAGE);
        if (substring == null)
            return null;
        final String sub = substring;
        Filter<Record> filter = new Filter<Record>() {
            private String substring = sub;

            public String toString() {
                return substring;
            }

            public boolean accepts(Record r) {
                for (Object cell : r.getCellValues()) {
                    if (cell != null && ((String) cell).contains(substring))
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

        final Format selected = (Format) JOptionPane.showInputDialog(
                null,
                "Select field",
                "Setup",
                JOptionPane.PLAIN_MESSAGE,
                null,
                log.getFormats(),
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
