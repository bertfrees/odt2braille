package be.docarch.odt2braille.setup;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.beans.XMLDecoder;

public class ConfigurationDecoder {

    /*
     * @return An instance of Configuration, EmbossConfiguration or ExportConfiguration
     */
    public static Object readObject(InputStream input) {

        BufferedInputStream bis = new BufferedInputStream(input);
        XMLDecoder xmlDecoder = new XMLDecoder(bis);

        return xmlDecoder.readObject();        
    }
}
