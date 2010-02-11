package org.lf.plugins.analysis;

import org.lf.parser.Log;
import org.lf.parser.Record;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.Highlighter;
import org.lf.services.RecordColorer;

import com.sun.istack.internal.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * User: jkff
 * Date: Jan 26, 2010
 * Time: 3:51:04 PM
 */
public class HighlightRegexPlugin implements AnalysisPlugin {
	
	@Nullable
	public Class getOutputType(Class[] inputTypes) {
		if( inputTypes.length == 1 && Log.class.isAssignableFrom(inputTypes[0])) 
			return Log.class;
		return null;
    }
    
	public Entity applyTo(Entity[] args) {
        Log log = (Log) args[0].data;

        final String regex = JOptionPane.showInputDialog(
                null, "Enter a regular expression to highlight", "Filter setup", JOptionPane.QUESTION_MESSAGE);
        if (regex == null)
        	return null;
        
        Attributes atr = args[0].attributes.createSuccessor();
        
        Highlighter highlighter = atr.getValue(Highlighter.class);
        if (highlighter == null) { 
        	highlighter = new Highlighter(null);
            atr.addAttribute(highlighter);        
        }
        
        highlighter.setRecordColorer(new RecordColorer() {			
			@Override
			public Color getColor(Record r) {
            	Pattern p = Pattern.compile(regex);
            	for (int i = 0; i < r.size(); ++i) {
            		if (p.matcher(r.get(i)).find()) return Color.RED; 
            	}
                return null;
			}
		});
        
        return new Entity(atr, log);
    }

    public String getName() {
        return "Highlight regex";
    }
}
