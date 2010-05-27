package org.lf.services;

import org.lf.util.IOUtils;

import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class ProgramProperties {
    private static final String propertiesFileName = "logophagus.txt";
    private static final String workingDir = "WORKING_DIR";
    private static final Properties mainProperties = new Properties();
    private static final String iconsPath;

    static {
        iconsPath = System.getProperty("user.dir") + "/src/java/org/lf/ui/icons/";
        String userHomePath = System.getProperty("user.home");
        File file = new File(userHomePath, propertiesFileName);
        InputStream stream;
        try {
            stream = new FileInputStream(file);
            mainProperties.loadFromXML(stream);
        } catch (FileNotFoundException e) {
        } catch (InvalidPropertiesFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mainProperties.getProperty(workingDir) == null) {
            mainProperties.setProperty(workingDir, System.getProperty("user.dir"));
        }
    }

    public static File getWorkingDir() {
        return new File(mainProperties.getProperty(workingDir));
    }

    public static void setWorkingDir(File f) {
        mainProperties.setProperty(workingDir, f.getAbsolutePath());
    }

    public static String getIconsPath() {
        return iconsPath;
    }

    public static void save() throws IOException {
        String userHomePath = System.getProperty("user.home");
        File file = new File(userHomePath, propertiesFileName);
        OutputStream stream = new FileOutputStream(file);
        mainProperties.storeToXML(stream, null);
    }

    public static void writeUIState(String state) throws IOException {
        Writer w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(
                    getUIStateFile()), "utf-8");
            w.write(state);
        } finally {
            if(w != null)
                w.close();
        }
    }

    private static File getUIStateFile() {
        return new File(System.getProperty("user.home"), "logophagus.state");
    }

    public static String readUIState() throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(new File(System.getProperty("user.home"), "logophagus.state"));
            return new String(IOUtils.readInputStream(is), "utf-8");
        } finally {
            if(is != null)
                is.close();
        }
    }
}
