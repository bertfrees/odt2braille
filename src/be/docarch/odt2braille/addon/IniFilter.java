/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
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

package be.docarch.odt2braille.addon;

import java.io.FilenameFilter;
import java.io.File;

/**
 * Implementation of <code>FilenameFilter</code>.
 * Is used to filter an array of files. Only those files that end with '.ini' are accepted.
 *
 * @see         FilenameFilter
 * @author      Bert Frees
 */
public class IniFilter implements FilenameFilter {

    private static final String extension = "ini";

    /**
     * Creates a new <code>IniFilter</code> instance.
     *
     */
    public IniFilter() {}

    /**
     * @param   directory   Not used
     * @param   filename    The name of a file.
     * @return          <code>true</code> if the filename ends with '.ini'
     */
    public boolean accept(File directory,
                          String filename) {

        return filename.endsWith('.' + extension);

    }

}