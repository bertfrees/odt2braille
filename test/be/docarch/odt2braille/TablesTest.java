package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;

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
        Configuration.setTransformer(tf);
        Configuration settings = Configuration.newInstance();
        ExportConfiguration exportSettings = new ExportConfiguration();

        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        settings.getTableStyles().get("Default").setStairstepEnabled(true);

        PEF pefBuilder = ODT2PEFConverter.convert(settings, exportSettings, null, null);

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }

    @Test
    public void linearFormatTest() throws Exception {

        File correctPEF = new File(resources + "tables_linear.pef");
        File testODT = new File(resources + "tables.odt");

        OdtTransformer tf = new OdtTransformer(testODT);
        Configuration.setTransformer(tf);
        Configuration settings = Configuration.newInstance();
        ExportConfiguration exportSettings = new ExportConfiguration();

        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        settings.getTableStyles().get("Default").setStairstepEnabled(false);

        PEF pefBuilder = ODT2PEFConverter.convert(settings, exportSettings, null, null);

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }
}
