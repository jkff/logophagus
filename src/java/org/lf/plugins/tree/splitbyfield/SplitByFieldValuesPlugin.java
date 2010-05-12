package org.lf.plugins.tree.splitbyfield;


import com.sun.istack.internal.Nullable;
import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.logs.Log;
import org.lf.logs.Record;
import org.lf.plugins.Entity;
import org.lf.plugins.tree.TreePlugin;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.tree.TreeContext;
import org.lf.util.Filter;
import org.lf.util.HierarchicalAction;
import org.lf.util.Pair;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SplitByFieldValuesPlugin implements TreePlugin {

    @Nullable
    public Class getOutputType(Class[] inputTypes) {
        return null;
    }


    @Override
    public HierarchicalAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 1 ||
                !(context.selectedNodes[0].getNodeData().entity.data instanceof Log)) {
            return null;
        }

        Action splitByFieldAction = new AbstractAction(getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Pair<Format, Field> formatField = getSelectedFieldFor(context.selectedNodes[0].getNodeData().entity);
                if (formatField == null)
                    return;
                Thread splitterThread = new Thread() {
                    @Override
                    public void run() {

                    }
                };
            }
        };

    }


    @Override
    public String getName() {
        return "Split by field values";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "folder_files.gif");
    }

    @Nullable
    private Pair<Format, Field> getSelectedFieldFor(Entity parent) {
        Log log = (Log) parent.data;
        Format[] formats = log.getFormats();
        final Format selectedFormat = (Format) JOptionPane.showInputDialog(
                null,
                "Select format",
                "Setup",
                JOptionPane.PLAIN_MESSAGE,
                null,
                formats,
                null);

        if (selectedFormat == null)
            return null;
        final Field selectedField = (Field) JOptionPane.showInputDialog(
                null,
                "Select field",
                "Setup",
                JOptionPane.PLAIN_MESSAGE,
                null,
                selectedFormat.getFields(),
                null);
        if (selectedField == null) return null;
        return new Pair<Format, Field>(selectedFormat, selectedField);
    }

    Filter<Record> filter = new Filter<Record>() {
        public String toString() {
            return selectedField.toString();
        }

        public boolean accepts(Record r) {
            for (Object cell : r.getCellValues()) {
                if (cell != null && r.getFormat().equals(selectedFormat)) {
                    Format format = r.getFormat();
                    for (int i = 0; i < format.getFields().length; ++i) {
                        Field cur = format.getFields()[i];
                        if (cur.equals(selectedField) && r.getCellValues()[i].matches(substring)) return true;
                    }
                    return false;
                }

            }
            return false;
        }
    };


}
