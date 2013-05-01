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

import be.docarch.odt2braille.checker.PostConversionBrailleChecker;
import be.docarch.odt2braille.setup.PEFConfiguration;

import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;


public class ODT2PEFConverter {

    public static PEF convert(ODT odt,
                              PEFConfiguration pefConfiguration,
                              PostConversionBrailleChecker checker,
                              StatusIndicator indicator)
                       throws TransformerConfigurationException,
                              TransformerException,
                              InterruptedException,
                              LiblouisXMLException,
                              MalformedURLException,
                              IOException,
                              ParserConfigurationException,
                              SAXException,
                              ConversionException,
                              Exception {

        // Create LiblouisXML
        LiblouisXML liblouisXML = new LiblouisXML(odt.getConfiguration(), pefConfiguration, Constants.getLiblouisDirectory());

        // Create PEF
        PEF pef = new PEF(odt, pefConfiguration, liblouisXML, indicator, checker);

        // Convert
        if(!pef.makePEF()) {
            if (indicator != null) { indicator.finish(false); }
            throw new ConversionException("Conversion exception");
        }

        // Check PEF file
        if (checker != null) {
            checker.checkPefFile(pef.getSinglePEF(), pefConfiguration);
        }

        return pef;
    }
}
