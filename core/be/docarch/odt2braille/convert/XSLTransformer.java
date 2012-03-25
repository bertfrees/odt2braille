package be.docarch.odt2braille.convert;

import be.docarch.odt2braille.Constants;

import java.io.File;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;

/**
 *
 * @author Bert Frees
 */
public class XSLTransformer extends FileConverter implements Parameterized {

    private static final TransformerFactoryImpl transformerFactory = new TransformerFactoryImpl();
    private static final String XSLT_PATH = Constants.XSLT_PATH;
    
    private final Transformer transformer;
    private final String name;
    
    public XSLTransformer(String xslFile) throws Exception {
        this(xslFile, true);
    }
    
    public XSLTransformer(String xslFile, boolean indent) throws Exception {
        name = xslFile;
        transformer = transformerFactory.newTransformer(
                new StreamSource(getClass().getResource(XSLT_PATH + name + ".xsl").toString()));
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, indent?"yes":"no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
    }
    
    @Override
    public void convert(File input, File output) throws ConversionException {
        log("Transforming file " + input.getName() + " with " + name + " XSLT");
        if (input.equals(output)) { throw new ConversionException("input File equals output File"); }
        try {
            transformer.transform(new StreamSource(input), new StreamResult(output));
            log("Output written to file: " + output.getName());
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    public void setParameter(String key, Object value) {
        transformer.setParameter(key, value);
    }

    public void cleanUp() {}
}
