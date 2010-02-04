package org.lf.plugins.analysis;

import org.jetbrains.annotations.Nullable;
import org.lf.parser.FileBackedLog;
import org.lf.parser.Log;
import org.lf.parser.csv.CSVParser;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.Bookmarks;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class FileBackedLogPlugin implements AnalysisPlugin {

    @Nullable
	public Entity applyTo(Entity[] args) {
		JFileChooser fileOpen = new JFileChooser();
        fileOpen.showOpenDialog(null);
		File f = fileOpen.getSelectedFile();
		if (f == null)
			return null;

		try {
			Log log = new FileBackedLog(f.getAbsolutePath(), new CSVParser());
			Bookmarks empty = new Bookmarks(null);
			return new Entity(Attributes.with(Attributes.NONE, Bookmarks.class, empty, Bookmarks.COMBINE_BOOKMARK), log);
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
