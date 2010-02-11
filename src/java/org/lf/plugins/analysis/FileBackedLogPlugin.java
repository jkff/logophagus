package org.lf.plugins.analysis;

import org.lf.parser.FileBackedLog;
import org.lf.parser.Log;
import org.lf.parser.csv.CSVParser;
import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.services.Bookmarks;

import com.sun.istack.internal.Nullable;

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
			Bookmarks bmParent = new Bookmarks(null);
			bmParent.addBookmark("test-1", log.getStart());
			bmParent.addBookmark("test0", log.getStart());

			Bookmarks bm = new Bookmarks(bmParent);
			bm.addBookmark("test1", log.getStart());
			bm.addBookmark("test2", log.getEnd());

			Attributes atr = new Attributes();
			atr.addAttribute(bm);
			return new Entity(atr, log);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getName() {
		return "FileBackedLog";
	}

	public Class getOutputType(Class[] inputTypes) {
		if (inputTypes.length == 0) 
			return Log.class;
		return null;
	}

}
