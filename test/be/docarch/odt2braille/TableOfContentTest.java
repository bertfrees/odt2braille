package be.docarch.odt2braille;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.TocStyle.TocLevelStyle;

import java.io.File;
import org.junit.Test;

//@org.junit.Ignore
public class TableOfContentTest extends Odt2BrailleTest {

    @Test
    public void tableOfContentTest() throws Exception {

        File testODT = new File(resources + "toc.odt");

        // Configuration 1
        File correctPEF = new File(resources + "toc_config1.pef");
        ODT odt = new ODT(testODT);

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(true);
        settings.setPrintPageNumbers(false);
        settings.setPageSeparator(false);
        settings.getBodyMatterVolume().setTableOfContent(true);
        settings.getHeadingStyles().get(1).setNewBraillePage(true);
        TocStyle style = settings.getTocStyle();
        style.setBraillePageNumbers(true);
        style.setEvaluateUptoLevel(2);
        style.setTitle("TABLE OF CONTENTS");
        TocLevelStyle level;
        level = style.getLevels().get(1);
        level.setFirstLine(0);
        level.setRunovers(6);
        level = style.getLevels().get(2);
        level.setFirstLine(3);
        level.setRunovers(9);

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }

        // Configuration 2
        correctPEF = new File(resources + "toc_config2.pef");        
        odt = new ODT(testODT);
        
        settings = odt.getConfiguration();
        settings.setBraillePageNumbers(true);
        settings.setPrintPageNumbers(true);
        settings.setPageSeparator(false);
        settings.getBodyMatterVolume().setTableOfContent(true);
        settings.getHeadingStyles().get(1).setNewBraillePage(true);
        style = settings.getTocStyle();
        style.setBraillePageNumbers(true);
        style.setPrintPageNumbers(true);
        style.setEvaluateUptoLevel(3);
        style.setTitle("CONTENTS");
        level = style.getLevels().get(1);
        level.setFirstLine(0);
        level.setRunovers(4);
        level = style.getLevels().get(2);
        level.setFirstLine(2);
        level.setRunovers(6);
        level = style.getLevels().get(3);
        level.setFirstLine(4);
        level.setRunovers(8);

        result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }
}
