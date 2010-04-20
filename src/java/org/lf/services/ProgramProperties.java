package org.lf.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class ProgramProperties {
    public static final String propertiesFileName = "logophagus.txt";
    public static final String workingDir = "WORKING_DIR";
    public static final Properties mainProperties = new Properties();
    public static final String iconsPath;
   
    static {
        //System.setProperty("jna.library.path", System.getProperty("user.dir")+ "/lib");
        iconsPath = System.getProperty("user.dir") +"/src/java/org/lf/ui/icons/";
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

    public static void save() throws IOException {
        String userHomePath = System.getProperty("user.home");
        File file = new File(userHomePath,propertiesFileName);
        OutputStream stream = new FileOutputStream(file);
        mainProperties.storeToXML(stream, null);
    }
}
