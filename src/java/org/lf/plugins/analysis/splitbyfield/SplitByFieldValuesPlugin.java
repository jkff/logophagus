package org.lf.plugins.analysis.splitbyfield;


import javax.swing.JOptionPane;
import javax.swing.JPanel;


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
		final String index = JOptionPane.showInputDialog(
                null, "Enter field index", "Splitter setup", JOptionPane.QUESTION_MESSAGE);
		if (index == null)
			return null;
		Log log = (Log) args[0].data;
		LogAndField result = new LogAndField(log, Integer.parseInt(index));
		return new Entity(args[0].attributes.createSuccessor(), result);
	}


	@Override
	public String getName() {
		return "Split by field values";  
	}


}
