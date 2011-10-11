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
