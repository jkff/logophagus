package org.lf.plugins.analysis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import org.lf.parser.FilteredLog;
import org.lf.parser.Log;
import org.lf.parser.Position;
import org.lf.parser.Record;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Entity;
import org.lf.util.FieldFilter;
import org.lf.util.RecordFilter;

public class SplitByFieldValuesPlugin implements AnalysisPlugin {

	public Object applyTo(Object[] args) {
		Log log = (Log) args[0];

		String index = JOptionPane.showInputDialog(null, "Enter field index", "Splitter setup", JOptionPane.QUESTION_MESSAGE );
		if (index == null)
			return null;

		try {
			Position pos = log.getStart();

			Set<String> fieldValues = new HashSet<String>();

			for (int i = 0; i < 100; ++i ) {
				Record rec = log.readRecord(pos);
				String curVal = rec.get(Integer.parseInt(index));
				int curSize = fieldValues.size();
				fieldValues.add(curVal);
				if (curSize != fieldValues.size()) {
					
				}
					
				//return new FilteredLog(log, new FieldFilter(Integer.parseInt(index), "5"));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return null;
	}


	@Override
	public Class[] getInputTypes() {
		return new Class[] {Log.class};
	}

	@Override
	public String getName() {
		return "SplitByFieldValues";  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Class getOutputType() {
		return Log.class;
	}

    public Entity applyTo(Entity[] args) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
