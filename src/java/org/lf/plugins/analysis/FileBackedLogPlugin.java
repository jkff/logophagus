package org.lf.plugins.analysis;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.lf.parser.FileBackedLog;
import org.lf.parser.Log;
import org.lf.parser.csv.CSVParser;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;

public class FileBackedLogPlugin implements AnalysisPlugin {

	public Entity applyTo(Entity[] args) {
		JFileChooser fileOpen = new JFileChooser();
        fileOpen.showOpenDialog(null);
		File f = fileOpen.getSelectedFile();
		if (f == null)
			return null;

		try {
			Log log = new FileBackedLog(f.getAbsolutePath(), new CSVParser());
			return new Entity(Attributes.NONE, log);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Class[] getInputTypes() {
		return new Class[]{};
	}

	public String getName() {
		return "FileBackedLog";
	}

	public Class getOutputType() {
		return Log.class;
	}

}
