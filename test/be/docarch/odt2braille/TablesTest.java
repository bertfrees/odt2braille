package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

/**
 *
 * @author Bert Frees
 */
public class TablesTest extends Odt2BrailleTest {

    @Test
    public void stairstepFormatTest() throws Exception {

        File correctPEF = new File(resources + "tables_stairstep.pef");
        File testODT = new File(resources + "tables.odt");

        OdtTransformer tf = new OdtTransformer(testODT);
        Settings settings = new Settings(tf);

        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        settings.getTableStyle().setStairstepTable(true);

        LiblouisXML liblouisXML = new LiblouisXML(settings, liblouis);
        PEF pefBuilder = new PEF(settings, liblouisXML);
        pefBuilder.makePEF();

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }

    @Test
    public void linearFormatTest() throws Exception {

        File correctPEF = new File(resources + "tables_linear.pef");
        File testODT = new File(resources + "tables.odt");

        OdtTransformer tf = new OdtTransformer(testODT);
        Settings settings = new Settings(tf);

        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        settings.getTableStyle().setStairstepTable(false);

        LiblouisXML liblouisXML = new LiblouisXML(settings, liblouis);
        PEF pefBuilder = new PEF(settings, liblouisXML);
        pefBuilder.makePEF();

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }
}
