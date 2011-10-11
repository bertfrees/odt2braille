package be.docarch.odt2braille;

import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import static org.junit.Assert.assertTrue;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import org.w3c.dom.Node;

/**
 *
 * @author Bert Frees
 */
@Ignore
public abstract class Odt2BrailleTest {

    protected static String resources;

    static {

        XMLUnit.setIgnoreWhitespace(true);
        Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

        try {

            File logFile = File.createTempFile(Constants.TMP_PREFIX, ".log", Constants.getTmpDirectory());
          //logFile.deleteOnExit();
            Handler fh = new FileHandler(logFile.getAbsolutePath());
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.FINEST);

            resources = Odt2BrailleTest.class.getResource("/be/docarch/odt2braille/resources/").getFile();

            Constants.setLiblouisDirectory(new File("dist" + File.separator + "liblouis"));

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    protected static void comparePEFs(File correctPEF,
                                      File testPEF)
                               throws Exception{

        Diff myDiff = new Diff(new FileReader(correctPEF),
                               new FileReader(testPEF));

        myDiff.overrideDifferenceListener(new DifferenceListener() {
            public int differenceFound(Difference dfrnc) {
                if (dfrnc.getId() == DifferenceConstants.TEXT_VALUE_ID) {
                    if ("dc".equals(dfrnc.getControlNodeDetail().getNode().getParentNode().getPrefix())) {
                        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                    }
                }
                return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
            }
            public void skippedComparison(Node node, Node node1) {}
        });

        assertTrue("PEFs not equal\n" + myDiff, myDiff.identical());
    }

    protected static File simpleODT2PEF(File odtFile)
                                 throws Exception {

        ODT odt = new ODT(odtFile);
        ExportConfiguration exportSettings = new ExportConfiguration();

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);

        PEF pefBuilder = ODT2PEFConverter.convert(odt, exportSettings, null, null);

        return pefBuilder.getSinglePEF();
    }
}
