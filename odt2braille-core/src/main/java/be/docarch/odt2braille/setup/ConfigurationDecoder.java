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

package be.docarch.odt2braille.setup;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.beans.XMLDecoder;

public class ConfigurationDecoder {

    /*
     * @return An instance of Configuration, EmbossConfiguration or ExportConfiguration
     */
    public static Object readObject(InputStream input) {

        Object object;
        BufferedInputStream bis = new BufferedInputStream(input);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Configuration.class.getClassLoader()); {

            XMLDecoder xmlDecoder = new XMLDecoder(bis);
            object = xmlDecoder.readObject();
            
        } Thread.currentThread().setContextClassLoader(cl);

        return object;
    }
}
