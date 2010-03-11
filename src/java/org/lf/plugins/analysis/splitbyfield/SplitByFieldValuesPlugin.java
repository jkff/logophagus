package org.lf.plugins.analysis.splitbyfield;


import javax.swing.Icon;
import javax.swing.JOptionPane;


import org.lf.parser.Field;
import org.lf.parser.Log;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.splitbyfield.LogAndField;

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
                null, "Select field", "Setup", JOptionPane.PLAIN_MESSAGE, null,
                log.getFields(), log.getFields()[0]);
		if (field == null)
			return null;
		LogAndField result = new LogAndField(log, (Field)field);
		return new Entity(args[0].attributes.createSuccessor(), result);
	}


	@Override
	public String getName() {
		return "Split by field values";  
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}


}
