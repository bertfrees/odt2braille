/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import java.io.IOException;

/**
 *
 * @author Bert Frees
 */
public class Constants {

    public static final String OOO_PACKAGE_NAME = "be.docarch.odt2braille.ooo.odt2brailleaddon";
    public static final String XSLT_PATH = "/be/docarch/odt2braille/xslt/";
    public static final String L10N_PATH = "be/docarch/odt2braille/l10n/core";
    public static final String OOO_L10N_PATH = "be/docarch/odt2braille/ooo/l10n/dialogs";
    public static final String TMP_PREFIX = "odt2braille.";
    
    private static File tempDirectory;
    private static File liblouisDir;
    private static File tablesDir;
    private static File logFile;
    private static Logger customLogger;
    private static Logger defaultLogger;
    private static Handler logFileHandler;
    private static StatusIndicator statusIndicator;

    public static boolean setTempDirectory(File dir) {
        if (tempDirectory != null) { return false; }
        tempDirectory = dir;
        return true;
    }
    
    public static File getTempDirectory() {
        if (tempDirectory == null) { tempDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + "odt2braille"); }
        if (!tempDirectory.isDirectory()) { tempDirectory.mkdir(); }
        return tempDirectory;
    }

    public static void setLogger(Logger logger) {
        Constants.customLogger = logger;
    }

    public static Logger getLogger() {
        if (customLogger != null) {
            return customLogger;
        } else {
            return getDefaultLogger();
        }
    }
    
    private static Logger getDefaultLogger() {
        if (defaultLogger == null) {
            defaultLogger = Logger.getLogger("be.docarch.odt2braille");
            defaultLogger.setLevel(Level.FINEST);
            try {
                logFile = File.createTempFile(TMP_PREFIX, ".log", getTempDirectory());
                logFileHandler = new FileHandler(logFile.getAbsolutePath());
                logFileHandler.setFormatter(new SimpleFormatter());
                logFile.deleteOnExit();
                defaultLogger.addHandler(logFileHandler);
                defaultLogger.log(Level.INFO, "log file: {0}", logFile.getAbsolutePath());
            } catch (IOException e) {
                defaultLogger.log(Level.SEVERE, null, e);
            }
        }
        return defaultLogger;
    }

    public static File getLogFile() {
        return logFile;
    }
    
    public static void flushLogger() {
        if (logFileHandler != null && defaultLogger != null) {
            logFileHandler.flush();
        }
    }
    
    public static void closeLogger () {
        if (logFileHandler != null && defaultLogger != null) {
            defaultLogger.entering("Constants", "cleanLogger");
            logFileHandler.close();
            defaultLogger.removeHandler(logFileHandler);
            defaultLogger = null;
            logFileHandler = null;
        }
    }

    public static boolean setLiblouisDirectory(File dir) throws Exception {
        if (liblouisDir != null) { return false; }
        if (!dir.exists() || !dir.isDirectory()) { throw new Exception(dir + " is no directory"); }
        File tables = new File(dir.getAbsolutePath() + File.separator + "files");
        if (!tables.exists() || !tables.isDirectory()) { throw new Exception("Directory has no subdirectory 'files'"); }
        liblouisDir = dir;
        tablesDir = tables;
        return true;
    }

    public static File getLiblouisDirectory() throws Exception {
        if (liblouisDir == null) { throw new Exception("Liblouis directory not set"); }
        return liblouisDir;
    }

    public static File getTablesDirectory() throws Exception {
        if (liblouisDir == null) { throw new Exception("Liblouis directory not set"); }
        return tablesDir;
    }

    public static boolean setStatusIndicator(StatusIndicator indicator) {
        if (statusIndicator != null) { return false; }
        statusIndicator = indicator;
        return true;
    }

    public static StatusIndicator getStatusIndicator() {
        if (statusIndicator == null) {
            statusIndicator = new StatusIndicator();
        }
        return statusIndicator;
    }
}
