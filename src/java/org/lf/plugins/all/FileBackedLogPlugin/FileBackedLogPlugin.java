package org.lf.plugins.all.FileBackedLogPlugin;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.lf.parser.FileBackedLog;
import org.lf.parser.LineParser;
import org.lf.parser.Log;
import org.lf.plugins.interfaces.AnalysisPlugin;

public class FileBackedLogPlugin implements AnalysisPlugin {

	public Object applyTo(Object[] args) {
		JFileChooser fileOpen = new JFileChooser();
        fileOpen.showOpenDialog(null);
		File f = fileOpen.getSelectedFile();
		if (f != null){
			Log log;
			try {
				log = new FileBackedLog(f.getAbsolutePath(), new LineParser());
				return log;

			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}else 
			return null;
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
