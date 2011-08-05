package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.style.PictureStyle;

public class PicturesTest extends Odt2BrailleTest {

    @Test
    public void picturesTest() throws Exception {

        File correctPEF = new File(resources + "pictures.pef");
        File testODT = new File(resources + "pictures.odt");

        OdtTransformer tf = new OdtTransformer(testODT);
        Configuration.setTransformer(tf);
        Configuration settings = Configuration.newInstance();
        ExportConfiguration exportSettings = new ExportConfiguration();

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

        PEF pefBuilder = ODT2PEFConverter.convert(settings, exportSettings, null, null);

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }

    @Test
    public void pictureCaptionsTest() throws Exception {

        File correctPEF = new File(resources + "picture_captions.pef");
        File testODT = new File(resources + "picture_captions.odt");

        OdtTransformer tf = new OdtTransformer(testODT);
        Configuration.setTransformer(tf);
        Configuration settings = Configuration.newInstance();
        ExportConfiguration exportSettings = new ExportConfiguration();

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

        PEF pefBuilder = ODT2PEFConverter.convert(settings, exportSettings, null, null);

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }
}
