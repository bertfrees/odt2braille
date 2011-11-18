package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.TocStyle.TocLevelStyle;

public class TableOfContentTest extends Odt2BrailleTest {

    @Test
    public void tableOfContentTest() throws Exception {

        File testODT = new File(resources + "toc.odt");

        // Configuration 1
        File correctPEF = new File(resources + "toc_config1.pef");
        ODT odt = new ODT(testODT);
        ExportConfiguration exportSettings = new ExportConfiguration();

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

        PEF pefBuilder = ODT2PEFConverter.convert(odt, exportSettings, null);

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);

        // Configuration 2
        correctPEF = new File(resources + "toc_config2.pef");        
        odt = new ODT(testODT);
        exportSettings = new ExportConfiguration();
        
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

        pefBuilder = ODT2PEFConverter.convert(odt, exportSettings, null);

        testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }
}
