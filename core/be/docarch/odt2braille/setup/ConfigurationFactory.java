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

import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.Constants;

import java.io.IOException;
import org.xml.sax.SAXException;

public class ConfigurationFactory {

    public static enum Mode { CURRENT, RESET, NEW }
    
    private static ODT odt = null;
    private static Mode mode = Mode.NEW;

    public static void setODT(ODT odt) {
        ConfigurationFactory.odt = odt;
    }
    
    public static void setMode(Mode mode) {
        ConfigurationFactory.mode = mode;
    }

    public static Configuration getInstance() throws IOException,
                                               SAXException,
                                               Exception {
        if (odt == null) {
            throw new Exception("Exception: ODT is not set");
        }

        TranslationTable.setTablesFolder(Constants.getTablesDirectory());
        
        switch (mode) {
            case CURRENT:
            case RESET:
                return odt.getConfiguration();
            case NEW:
            default:
                return new Configuration(odt);
        }
    }
}
