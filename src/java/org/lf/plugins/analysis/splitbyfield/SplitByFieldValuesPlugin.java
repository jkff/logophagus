package org.lf.plugins.analysis.splitbyfield;


import com.sun.istack.internal.Nullable;
import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.logs.Log;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.plugins.analysis.TreeAction;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.tree.TreeContext;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SplitByFieldValuesPlugin implements AnalysisPlugin {

    @Nullable
    public Class getOutputType(Class[] inputTypes) {
        if (inputTypes.length == 1 && Log.class.isAssignableFrom(inputTypes[0]))
            return LogAndField.class;
        return null;
    }

    public Entity applyTo(Entity[] args) {
        Log log = (Log) args[0].data;
        Field[] commonFields = getCommonFields(log.getFormats());

        if (commonFields.length == 0) {
            JOptionPane.showMessageDialog(null, "Records in log must have some equal fields!");
            return null;
        }

        Field field = (Field) JOptionPane.showInputDialog(
                null,
                "Select format",
                "Filter setup",
                JOptionPane.PLAIN_MESSAGE,
                null,
                commonFields,
                null);

        if (field == null)
            return null;

        LogAndField result = new LogAndField(log, field);
        return new Entity(args[0].attributes.createSuccessor(log), result);
    }


    @Override
    public TreeAction getActionFor(TreeContext context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        return "Split by field values";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "folder_files.gif");
    }

    private Field[] getCommonFields(Format[] formats) {
        Set<Field> commonFields = new HashSet<Field>(Arrays.asList(formats[0].getFields()));
        for (Format format : formats) {
            commonFields.retainAll(Arrays.asList(format.getFields()));
        }
        return commonFields.toArray(new Field[0]);
    }
}
