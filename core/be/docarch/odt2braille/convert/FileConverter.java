package be.docarch.odt2braille.convert;

import be.docarch.odt2braille.Loggable;
import java.io.File;
import java.util.logging.Logger;

/**
 *
 * @author Bert Frees
 */
public abstract class FileConverter implements Converter<File,File>, Loggable {
    
    private Logger logger = null;
    
    @Override
    public File convert(File input) throws ConversionException {
        File output = new File(input.getAbsoluteFile() + ".temp");
        convert(input, output);
        input.delete();
        if (!output.renameTo(input)) {
            // ?
        }
        log("Output renamed back to input");
        return output;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    protected void log(String message) {
        if (logger != null) {
            logger.info(message);
        }
    }
    
    public abstract void convert(File input, File output) throws ConversionException;
    
}
