package org.lf.plugins.analysis.merge;

import java.io.IOException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.lf.logs.Log;
import org.lf.logs.MergeLogs;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.Bookmarks;
import org.lf.services.ProgramProperties;

import java.io.IOException;

public class MergeLogsPlugin implements AnalysisPlugin {

    @Override
    public Entity applyTo(Entity[] args) {
        Log[] logs = new Log[args.length];
        Integer[] fields = new Integer[args.length];

        for (int i = 0; i < args.length; ++i ) {
            final String index = JOptionPane.showInputDialog(null, "Enter field index of log (\"" + args[i].data + "\") for merging with other logs", "Merge setup", JOptionPane.QUESTION_MESSAGE );
            if (index == null)
                return null;

            logs[i] = (Log) args[i].data;
            fields[i] = Integer.parseInt(index);
        }

		Log mergedLog = null;
		try {
			mergedLog = new MergeLogs(logs, fields);

			Attributes[] childAttributes = new Attributes[args.length];
			for (int i = 0 ; i < args.length ; ++i) {
				childAttributes[i] = args[i].attributes;
			}
			
	        //TODO bookmarks use context approach -> so it is easy to overlap child positions by merged positions
	        // but it's better to do this in gui as it can take a lot of time because of position convertation
			Attributes myAttributes = Attributes.join(childAttributes, mergedLog);

			if (mergedLog != null)
				return new Entity(myAttributes, mergedLog);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
    }

    @Override
    public String getName() {
        return "Merge logs";
    }

    @Override
    public Class getOutputType(Class[] inputTypes) {
        if( inputTypes.length <= 1) return null;
        for (Class clazz : inputTypes) {
            if (!Log.class.isAssignableFrom(clazz)) return null;
        }
        return MergeLogs.class;
    }

    @Override
    public Icon getIcon() {
    	return new ImageIcon(ProgramProperties.iconsPath +"multi.gif");
    }

}
