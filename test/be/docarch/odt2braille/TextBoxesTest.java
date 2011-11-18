package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.style.FrameStyle;

public class TextBoxesTest extends Odt2BrailleTest {

    @Test
    public void textboxesTest() throws Exception {

        File correctPEF = new File(resources + "textboxes.pef");
        File testODT = new File(resources + "textboxes.odt");

        ODT odt = new ODT(testODT);
        ExportConfiguration exportSettings = new ExportConfiguration();

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        FrameStyle frameStyle = settings.getFrameStyle();
        frameStyle.setLinesAbove(0);
        frameStyle.setLinesBelow(0);
        frameStyle.setUpperBorderEnabled(true);
        frameStyle.setLowerBorderEnabled(true);
        frameStyle.setUpperBorderStyle('\u2836');
        frameStyle.setLowerBorderStyle('\u281b');
        frameStyle.setPaddingAbove(0);
        frameStyle.setPaddingBelow(0);

        PEF pefBuilder = ODT2PEFConverter.convert(odt, exportSettings, null);

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }
}
