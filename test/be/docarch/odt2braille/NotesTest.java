package be.docarch.odt2braille;

import java.io.File;
import org.junit.Test;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.NoteReferenceFormat;
import be.docarch.odt2braille.setup.SettingMap;
import be.docarch.odt2braille.setup.style.FootnoteStyle;

/**
 *
 * @author Bert Frees
 */
public class NotesTest extends Odt2BrailleTest {

    @Test
    public void footnotesTest() throws Exception {

        File correctPEF = new File(resources + "footnotes.pef");
        File testODT = new File(resources + "footnotes.odt");

        ODT odt = new ODT(testODT);
        ExportConfiguration exportSettings = new ExportConfiguration();

        Configuration settings = odt.getConfiguration();
        settings.setBraillePageNumbers(false);
        settings.setPageSeparator(false);
        FootnoteStyle style = settings.getFootnoteStyle();
        style.setFirstLine(6);
        style.setRunovers(4);        
        SettingMap<String,NoteReferenceFormat> formats = settings.getNoteReferenceFormats();
        formats.get("1").setPrefix("\u2814\u2814");
        formats.get("a").setPrefix("\u2814\u2814\u2830");
        formats.get("i").setPrefix("\u2814\u2814\u2830");
        formats.get("1").setSpaceAfter(true);
        formats.get("a").setSpaceAfter(true);
        formats.get("i").setSpaceAfter(true);

        PEF pefBuilder = ODT2PEFConverter.convert(odt, exportSettings, null);

        File testPEF = pefBuilder.getSinglePEF();

        comparePEFs(correctPEF, testPEF);
    }

    @Test
    public void endnotesTest() throws Exception {

    }
}
