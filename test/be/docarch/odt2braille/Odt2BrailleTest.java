package be.docarch.odt2braille;

import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Bert Frees
 */
@Ignore
public abstract class Odt2BrailleTest {

    protected static String resources;
    protected static File liblouis;

    static {

        XMLUnit.setIgnoreWhitespace(true);
        Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

        try {

            File logFile = File.createTempFile(Constants.TMP_PREFIX, ".log", Constants.getTmpDirectory());
            logFile.deleteOnExit();
            Handler fh = new FileHandler(logFile.getAbsolutePath());
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.FINEST);

            resources = Odt2BrailleTest.class.getResource("/be/docarch/odt2braille/resources/").getFile();
            liblouis = new File("dist" + File.separator + "liblouis");

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    protected static void comparePEFs(File correctPEF,
                                      File testPEF)
                               throws Exception{

        Diff myDiff = new Diff(new FileReader(correctPEF),
                               new FileReader(testPEF));

        assertTrue("PEFs not equal\n" + myDiff, myDiff.identical());
    }

    protected static File simpleODT2PEF(File odtFile)
                                 throws Exception {

        OdtTransformer tf = new OdtTransformer(odtFile);
        Settings settings = new Settings(tf);

        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);

        LiblouisXML liblouisXML = new LiblouisXML(settings, liblouis);
        PEF pefBuilder = new PEF(settings, liblouisXML);
        pefBuilder.makePEF();

        return pefBuilder.getSinglePEF();
    }
}
