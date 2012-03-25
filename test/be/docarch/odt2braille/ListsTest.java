package be.docarch.odt2braille;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.style.ListStyle;

import java.io.File;
import org.junit.Test;

//@org.junit.Ignore
public class ListsTest extends Odt2BrailleTest {

    @Test
    public void listsTest() throws Exception {

        File correctPEF = new File(resources + "lists.pef");
        File testODT = new File(resources + "lists.odt");

        ODT odt = new ODT(testODT);

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

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }
}
