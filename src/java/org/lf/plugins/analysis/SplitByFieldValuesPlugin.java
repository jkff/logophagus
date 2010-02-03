package org.lf.plugins.analysis;

import java.io.IOException;

import javax.swing.JOptionPane;


import org.lf.parser.Log;
import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.LogAndField;


public class SplitByFieldValuesPlugin implements AnalysisPlugin {

	@Override
	public Entity applyTo(Entity[] args) {
		final String index = JOptionPane.showInputDialog(null, "Enter field index", "Splitter setup", JOptionPane.QUESTION_MESSAGE );
		if (index == null)
			return null;
		 Log log = (Log) args[0].data;
		 LogAndField result = new LogAndField(log, Integer.parseInt(index));
		return new Entity(Attributes.NONE, result);
	}


	@Override
	public Class[] getInputTypes() {
		return new Class[] {Log.class};
	}

	@Override
	public String getName() {
		return "Split by field values";  
	}

	@Override
	public Class getOutputType() {
		return LogAndField.class;
	}

}
