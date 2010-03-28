package org.lf.plugins.analysis.splitbyfield;


import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


import org.lf.logs.Log;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.splitbyfield.LogAndField;
import org.lf.services.ProgramProperties;

import com.sun.istack.internal.Nullable;


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
        Object field = JOptionPane.showInputDialog(
                null,
                "Select field", 
                "Setup", 
                JOptionPane.PLAIN_MESSAGE, 
                null,
                log.getMetadata().getFieldNames(),
                null);
        if (field == null)
            return null;
        
        int index;
        
        for(index = 0; index < log.getMetadata().getFieldCount() ; ++index) {
			if (log.getMetadata().getFieldName(index).equals(field)) break;
		}
        
        LogAndField result = new LogAndField(log, index);
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


}
