package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.style.ListStyle;

/**
 *
 * @author Bert Frees
 */
public class ListsTest extends Odt2BrailleTest {

    @Test
    public void listsTest() throws Exception {

        File correctPEF = new File(resources + "lists.pef");
        File testODT = new File(resources + "lists.odt");

        ODT odt = new ODT(testODT);
        ExportConfiguration exportSettings = new ExportConfiguration();

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        ListStyle style1 = settings.getListStyles().get(1);
        style1.setLinesAbove(1);
        style1.setLinesBelow(1);
        style1.setFirstLine(0);
        style1.setRunovers(4);
        style1.setPrefix("\u2812");
        ListStyle style2 = settings.getListStyles().get(2);
        style2.setFirstLine(2);
        style2.setRunovers(6);
        style2.setPrefix("\u2836");

        PEF pefBuilder = ODT2PEFConverter.convert(odt, exportSettings, null);

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }
}
