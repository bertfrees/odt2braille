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


/**
 *
 * @author Bert Frees
 */
public class Constants {

    public static final String LOGGER_NAME = "be.docarch.odt2braille";
    public static final String OOO_PACKAGE_NAME = "be.docarch.odt2braille.ooo.odt2brailleaddon";

    public static final String XSLT_PATH = "/be/docarch/odt2braille/xslt/";
    public static final String L10N_PATH = "be/docarch/odt2braille/l10n/Bundle";
    public static final String OOO_L10N_PATH = "be/docarch/odt2braille/ooo/l10n/Bundle";

    public static final String TMP_PREFIX = "odt2braille.";
    private static final File TMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir") + File.separator + "odt2braille");

    private static File liblouisDir;
    private static File tablesDir;

    public static File getTmpDirectory() {
        if (!TMP_DIRECTORY.isDirectory()) { TMP_DIRECTORY.mkdir(); }
        return TMP_DIRECTORY;
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
}
