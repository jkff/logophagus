package org.lf.plugins.analysis.splitbyfield;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.logs.Log;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.splitbyfield.LogAndField;
import org.lf.services.ProgramProperties;

import com.sun.istack.internal.Nullable;
import static org.lf.util.CollectionFactory.newHashSet;

public class SplitByFieldValuesPlugin implements AnalysisPlugin {

    @Nullable
    public Class getOutputType(Class[] inputTypes) {
        if( inputTypes.length == 1 && Log.class.isAssignableFrom(inputTypes[0]))
            return LogAndField.class;
        return null;
    }

    @Override
    public Entity applyTo(Entity[] args) {
        Log log = (Log) args[0].data;
        Field[] commonFields = getCommonFields(log.getFormats());
        
        if (commonFields.length == 0) {
        	JOptionPane.showMessageDialog(null, "Records in log must have some equal fields!");
        	return null;
        }
        	
        Field field = (Field)JOptionPane.showInputDialog(
                null,
                "Select field", 
                "Setup", 
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
    public String getName() {
        return "Split by field values";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath +"folder_files.gif");
    }

    private Field[] getCommonFields(Format[] formats) {
        Set<Field> commonFields = new HashSet<Field>(Arrays.asList(formats[0].getFields()));
        for (Format format : formats) {
            commonFields.retainAll(Arrays.asList(format.getFields()));
        }
        return commonFields.toArray(new Field[0]);
    }
}
