package be.docarch.odt2braille;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.style.TableStyle;

import java.io.File;
import org.junit.Test;

//@org.junit.Ignore
public class TablesTest extends Odt2BrailleTest {

    // TODO: op elke tabel in table.odt een andere tabelstijl toepassen (column heading en/of row heading)

    @Test
    public void linearFormatTest() throws Exception {

        File correctPEF = new File(resources + "tables_linear.pef");
        File testODT = new File(resources + "tables.odt");

        ODT odt = new ODT(testODT);

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        TableStyle style = settings.getTableStyles().get("Default");
        style.setStairstepEnabled(false);
        style.setColumnDelimiter("\u2830");
        style.setFirstLine(0);
        style.setRunovers(3);
        
        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }

    @Test
    public void stairstepFormatTest() throws Exception {

        File correctPEF = new File(resources + "tables_stairstep.pef");
        File testODT = new File(resources + "tables.odt");

        ODT odt = new ODT(testODT);

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        TableStyle style = settings.getTableStyles().get("Default");
        style.setStairstepEnabled(true);
        style.setIndentPerColumn(3);
        style.setFirstLine(0);
        style.setRunovers(0);

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }

    @Test
    public void repeatHeadingsTest() throws Exception {

        File correctPEF = new File(resources + "tables_repeat_heading.pef");
        File testODT = new File(resources + "tables.odt");

        ODT odt = new ODT(testODT);

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        TableStyle style = settings.getTableStyles().get("Default");
        style.setStairstepEnabled(false);
        style.setColumnDelimiter("\u2830");
        style.setFirstLine(0);
        style.setRunovers(3);
        style.setColumnHeadings(true);
        style.setRepeatHeading(true);
        style.setHeadingSuffix("\u2812");

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }

    @Test
    public void invertReadingOrderTest() throws Exception {

        File correctPEF = new File(resources + "tables_invert.pef");
        File testODT = new File(resources + "tables.odt");

        ODT odt = new ODT(testODT);

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        TableStyle style = settings.getTableStyles().get("Default");
        style.setStairstepEnabled(false);
        style.setColumnDelimiter("\u2830");
        style.setFirstLine(0);
        style.setRunovers(3);
        style.setMirrorTable(true);

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }

    @Test
    public void tableCaptionsTest() throws Exception {

        // TODO: caption boven tabel moet werken

        File correctPEF = new File(resources + "table_captions.pef");
        File testODT = new File(resources + "table_captions.odt");

        ODT odt = new ODT(testODT);

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        TableStyle style = settings.getTableStyles().get("Default");
        style.setStairstepEnabled(false);
        style.setColumnDelimiter("\u2830");
        style.setFirstLine(0);
        style.setRunovers(3);
        style.setColumnHeadings(true);

        ConversionResult result = convertODT2PEF(odt, settings);

        if (comparePEFs(correctPEF, result.getPEFFile())) {
            result.cleanUp();
        }
    }
}
