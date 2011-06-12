package be.docarch.odt2braille;

import java.io.File;


/**
 *
 * @author Bert Frees
 */
public class Constants {

    public static final String LOGGER_NAME = "be.docarch.odt2braille";
    public static final String OOO_PACKAGE_NAME = "be.docarch.odt2braille.ooo.odt2brailleaddon";

    public static final String XSLT_PATH = "/be/docarch/odt2braille/xslt/";
    public static final String L10N_PATH = "be/docarch/odt2braille/l10n/Bundle";
    public static final String OOO_L10N_PATH = "be/docarch/odt2braille/ooo/l10n/Bundle";

    public static final String TMP_PREFIX = "odt2braille.";
    private static final File TMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir") + File.separator + "odt2braille");

    public static File getTmpDirectory() {
        if (!TMP_DIRECTORY.isDirectory()) {
            TMP_DIRECTORY.mkdir();
        }
        return TMP_DIRECTORY;
    }
}
