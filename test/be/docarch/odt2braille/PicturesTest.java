package be.docarch.odt2braille;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.style.PictureStyle;

import java.io.File;
import org.junit.Test;

//@org.junit.Ignore
public class PicturesTest extends Odt2BrailleTest {

    @Test
    public void picturesTest() throws Exception {

        File correctPEF = new File(resources + "pictures.pef");
        File testODT = new File(resources + "pictures.odt");

        ODT odt = new ODT(testODT);

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        PictureStyle style = settings.getPictureStyle();
        style.setLinesAbove(1);
        style.setLinesBelow(1);
        style.setFirstLine(6);
        style.setRunovers(4);
        style.setOpeningMark("\u2820\u2804");
        style.setClosingMark("\u2820\u2804");
        style.setDescriptionPrefix("Picture description:");

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }

    @Test
    public void pictureCaptionsTest() throws Exception {

        File correctPEF = new File(resources + "picture_captions.pef");
        File testODT = new File(resources + "picture_captions.odt");

        ODT odt = new ODT(testODT);

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        PictureStyle style = settings.getPictureStyle();
        style.setLinesAbove(1);
        style.setLinesBelow(1);
        style.setFirstLine(6);
        style.setRunovers(4);
        style.setOpeningMark("\u2820\u2804");
        style.setClosingMark("\u2820\u2804");
        style.setDescriptionPrefix("Picture description:");

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }
}
