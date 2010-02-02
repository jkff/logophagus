package org.lf.plugins.analysis;

import org.lf.parser.FilteredLog;
import org.lf.parser.Log;
import org.lf.parser.Record;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.util.RecordFilter;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

/**
 * User: jkff
 * Date: Jan 26, 2010
 * Time: 3:51:04 PM
 */
public class HighlightRegexPlugin implements AnalysisPlugin {
    public Class[] getInputTypes() {
        return new Class[] {Log.class};
    }

    public Class getOutputType() {
        return Log.class;
    }

    public Entity applyTo(Entity[] args) {
        Log log = (Log) args[0].data;

        final String regex = JOptionPane.showInputDialog(
                null, "Enter a regular expression to highlight", "Filter setup", JOptionPane.QUESTION_MESSAGE);
        if (regex == null)
        	return null;
        return new Entity(Attributes.with(args[0].attributes, Highlighter.class, new Highlighter() {
            public Color getHighlightColor(Record rec) {
            	Pattern p = Pattern.compile(regex);
            	for (int i = 0; i < rec.size(); ++i) {
            		if (p.matcher(rec.get(i)).find()) return Color.RED; 
            	}
                return null;
            }
        }, Highlighter.COMBINE_SEQUENTIALLY), log);
    }

    public String getName() {
        return "Highlight regex";
    }
}
