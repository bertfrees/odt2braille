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

import be.docarch.odt2braille.utils.FileCreator;
import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Bert Frees
 */
public class Constants {

    public static final String OOO_PACKAGE_NAME = "be.docarch.odt2braille.ooo.odt2brailleaddon";
    public static final String XSLT_PATH = "/be/docarch/odt2braille/xslt/";
    public static final String L10N_PATH = "be/docarch/odt2braille/l10n/Bundle";
    public static final String OOO_L10N_PATH = "be/docarch/odt2braille/ooo/l10n/Bundle";
    
    private static File liblouisDir;
    private static File tablesDir;
    private static File logFile;
    private static Logger customLogger;
    private static Logger defaultLogger;
    private static Handler logFileHandler;
    private static ProgressMonitor statusIndicator;

    public static void setLogger(Logger logger) {
        Constants.customLogger = logger;
    }

    public static Logger getLogger() {
        return (customLogger != null) ? customLogger : getDefaultLogger();
    }
    
    private static Logger getDefaultLogger() {
        if (defaultLogger == null) {
            defaultLogger = Logger.getLogger("be.docarch.odt2braille");
            defaultLogger.setLevel(Level.FINEST);
            try {
                logFile = FileCreator.createTempFile(".log");
                logFileHandler = new FileHandler(logFile.getAbsolutePath());
                logFileHandler.setFormatter(new SimpleFormatter());
                defaultLogger.addHandler(logFileHandler);
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

    public static boolean setStatusIndicator(ProgressMonitor indicator) {
        if (statusIndicator != null) { return false; }
        statusIndicator = indicator;
        return true;
    }

    public static ProgressMonitor getStatusIndicator() {
        if (statusIndicator == null) {
            statusIndicator = new ProgressMonitor();
        }
        return statusIndicator;
    }
}
