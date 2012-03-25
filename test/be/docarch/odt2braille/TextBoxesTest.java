package be.docarch.odt2braille;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.style.FrameStyle;

import java.io.File;
import org.junit.Test;

//@org.junit.Ignore
public class TextBoxesTest extends Odt2BrailleTest {

    @Test
    public void textboxesTest() throws Exception {

        File correctPEF = new File(resources + "textboxes.pef");
        File testODT = new File(resources + "textboxes.odt");

        ODT odt = new ODT(testODT);

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

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }
}
