package be.docarch.odt2braille;

import java.io.File;
import be.docarch.odt2braille.checker.PostConversionBrailleChecker;
import be.docarch.odt2braille.setup.PEFConfiguration;
import be.docarch.odt2braille.setup.Configuration;

import org.xml.sax.SAXException;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;


public class ODT2PEFConverter {

    private static File liblouisLocation;

    public static void setLiblouisLocation(File folder) {
        liblouisLocation = folder;
    }

    public static PEF convert(Configuration configuration,
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
                              ConversionException {


        // Create LiblouisXML
        if (liblouisLocation == null) {
            throw new ConversionException("LiblouisXML location not set");
        }
        
        LiblouisXML liblouisXML = new LiblouisXML(configuration, pefConfiguration, liblouisLocation);

        // Create PEF
        PEF pef = new PEF(configuration, pefConfiguration, liblouisXML, indicator, checker);

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
