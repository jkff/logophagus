package org.lf.plugins.tree.highlight;

import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.tree.TreePlugin;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.util.HierarchicalAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;

public class HighlightRegexpPlugin implements TreePlugin, Plugin {

    @Override
    public void init(ProgramContext context) {
        context.getTreePluginRepository().register(this);
    }

    @Override
    public HierarchicalAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 1 ||
                !(context.selectedNodes[0].getNodeData().entity.data instanceof Log))
            return null;

        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = getEntity(context.selectedNodes[0].getNodeData().entity);
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIconFilename());
                context.addChildTo(context.selectedNodes[0], nodeData, true);
            }
        };

        action.putValue(Action.NAME, getName());
        return new HierarchicalAction(action);


    }

    public String getName() {
        return "Highlight records matching regexp";
    }

    @Override
    public String getIconFilename() {
        return "colorized.gif";
    }

    private Entity getEntity(Entity parent) {
        Log log = (Log) parent.data;

        final String regexp = JOptionPane.showInputDialog(
                null, "Enter a regular expression to highlight",
                "Highlight setup",
                JOptionPane.QUESTION_MESSAGE);
        if (regexp == null)
            return null;

        Attributes atr = parent.attributes.createSuccessor(log);

        Highlighter highlighter = atr.getValue(Highlighter.class);
        if (highlighter == null) {
            highlighter = new Highlighter(null);
            atr.addAttribute(highlighter);
        }

        highlighter.setRecordColorer(new RecordColorer() {
            // Compile the pattern just once to avoid
            // recompiling at each record
            private final Pattern p = Pattern.compile(regexp);

            @Override
            public Color getColor(Record r) {
                for (int i = 0; i < r.getCellCount(); ++i) {
                    CharSequence cell = r.getCell(i);
                    if (cell != null && p.matcher(cell).find())
                        return Color.RED;
                }
                return null;
            }
        });

        return new Entity(atr, log);
    }
}
