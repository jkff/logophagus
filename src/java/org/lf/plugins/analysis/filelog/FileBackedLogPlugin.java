package org.lf.plugins.analysis.filelog;

import org.lf.parser.FileBackedLog;
import org.lf.parser.Log;
import org.lf.parser.csv.CSVParser;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.Bookmarks;
import org.lf.services.ProgramProperties;

import com.sun.istack.internal.Nullable;

import javax.swing.*;

import java.io.File;
import java.io.IOException;

public class FileBackedLogPlugin implements AnalysisPlugin {

    @Nullable
	public Entity applyTo(Entity[] args) {
		JFileChooser fileOpen = new JFileChooser(ProgramProperties.getWorkingDir());
        fileOpen.showOpenDialog(null);
		File f = fileOpen.getSelectedFile();
		if (f == null || !f.isFile())
			return null;
		ProgramProperties.setWorkingDir(f.getParentFile());
		
		try {
			ProgramProperties.save();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Log log = new FileBackedLog(f.getAbsolutePath(), new CSVParser());
			Attributes atr = new Attributes();
			atr.addAttribute(new Bookmarks(null));
			return new Entity(atr, log);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getName() {
		return "Open log from file";
	}

	public Class getOutputType(Class[] inputTypes) {
		if (inputTypes.length == 0) 
			return Log.class;
		return null;
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

}
