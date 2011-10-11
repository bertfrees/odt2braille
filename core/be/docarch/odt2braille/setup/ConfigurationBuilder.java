package be.docarch.odt2braille.setup;

import org.xml.sax.SAXException;
import java.io.IOException;

import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.Constants;

public class ConfigurationBuilder {

    private static ODT odt = null;

    public static void setODT(ODT odt) {
        ConfigurationBuilder.odt = odt;
    }

    public static Configuration build() throws IOException,
                                               SAXException,
                                               Exception {
        if (odt == null) {
            throw new Exception("Exception: ODT is not set");
        }

        TranslationTable.setTablesFolder(Constants.getTablesDirectory());
        return new Configuration(odt);
    }
}
