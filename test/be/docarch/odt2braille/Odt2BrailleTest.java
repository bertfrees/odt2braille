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

    protected static String pefs;
    protected static String odts;
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

            pefs = Odt2BrailleTest.class.getResource("/be/docarch/odt2braille/resources/pef/").getFile();
            odts = Odt2BrailleTest.class.getResource("/be/docarch/odt2braille/resources/odt/").getFile();
            liblouis = new File(new File(Odt2BrailleTest.class.getResource("/be").getFile())
                                        .getParentFile().getParentFile().getParent() + File.separator + "liblouis");

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    static void compare(File correctPEF,
                        File producedPEF)
                 throws Exception{

        Diff myDiff = new Diff(new FileReader(correctPEF),
                               new FileReader(producedPEF));

        assertTrue("PEFs not equal\n" + myDiff, myDiff.identical());
    }

    static void simpleTest(String fileName)
                    throws Exception {

        File odt = new File(odts + fileName + ".odt");
        File correctPEF = new File(pefs + fileName + ".pef");
        OdtTransformer tf = new OdtTransformer(odt);
        Settings settings = new Settings(tf);

        settings.setBraillePageNumbers(false);

        LiblouisXML liblouisXML = new LiblouisXML(settings, liblouis);
        PEF pefBuilder = new PEF(settings, liblouisXML);
        pefBuilder.makePEF();
        File producedPEF = pefBuilder.getSinglePEF();

        compare(correctPEF, producedPEF);
    }
}
