package org.lf.services;

import java.io.File;

public class ProgramProperties {
	public static File workingDir = new File ("."); 
	
	public static File getWorkingDir() {
		return new File(workingDir, "");
	}
	
	public static File setWorkingDir(File f) {
		return workingDir = f;
	}
}
