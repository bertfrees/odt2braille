package be.docarch.odt2braille.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bert Frees
 */
public class FileCreator {
    
    private static final Logger logger = Logger.getLogger("be.docarch.odt2braille");
    private static final String TMP_PREFIX = "odt2braille.";
    private static File tempDirectory;

    public static boolean setTempDirectory(File dir) {
        tempDirectory = dir;
        if (!tempDirectory.isDirectory()) { tempDirectory.mkdir(); }
        logger.log(Level.INFO, "Temp directory set to {0}", tempDirectory.getAbsolutePath());
        return true;
    }
    
    public static File getTempDirectory() {
        if (tempDirectory == null) {
            setTempDirectory(new File(System.getProperty("java.io.tmpdir") + File.separator + "odt2braille"));
        }
        return tempDirectory;
    }
    
    public static File createTempFile(String suffix) throws IOException {
        File file = File.createTempFile(TMP_PREFIX, suffix, getTempDirectory());
        logger.log(Level.INFO, "Temp file {0} created", file.getName());
        return file;
    }
}
