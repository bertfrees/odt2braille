package be.docarch.odt2braille.setup;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.beans.XMLDecoder;

public class ConfigurationDecoder {

    /*
     * @return An instance of Configuration, EmbossConfiguration or ExportConfiguration
     */
    public static Object readObject(InputStream input) {

        Object object;
        BufferedInputStream bis = new BufferedInputStream(input);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Configuration.class.getClassLoader()); {

            XMLDecoder xmlDecoder = new XMLDecoder(bis);
            object = xmlDecoder.readObject();
            
        } Thread.currentThread().setContextClassLoader(cl);

        return object;
    }
}
