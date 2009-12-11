package org.lf.plugins.analysis;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.lf.parser.CSVParser;
import org.lf.parser.FileBackedLog;
import org.lf.parser.Log;
import org.lf.plugins.AnalysisPlugin;

public class FileBackedLogPlugin implements AnalysisPlugin {

	public Object applyTo(Object[] args) {
		JFileChooser fileOpen = new JFileChooser();
        fileOpen.showOpenDialog(null);
		File f = fileOpen.getSelectedFile();
		if (f == null)
			return null;

		try {
			Log log = new FileBackedLog(f.getAbsolutePath(), new CSVParser('\n',',','"' , '\\' ));
			return log;
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
