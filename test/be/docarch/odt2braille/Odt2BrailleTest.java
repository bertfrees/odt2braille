package be.docarch.odt2braille;

import be.docarch.odt2braille.convert.Converter;
import be.docarch.odt2braille.convert.ODT2PEFConverter;
import be.docarch.odt2braille.convert.ODT2PEFConverterParameters;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.PEFConfiguration;

import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import static org.junit.Assert.assertTrue;
import org.w3c.dom.Node;

/**
 *
 * @author Bert Frees
 */
@Ignore
public abstract class Odt2BrailleTest {

    protected static String resources;

    static {

        XMLUnit.setIgnoreWhitespace(true);
        Logger logger = Logger.getLogger("be.docarch.odt2braille");

        try {

            logger.setLevel(Level.FINEST);

            resources = Odt2BrailleTest.class.getResource("/be/docarch/odt2braille/resources/").getFile();

            Constants.setLiblouisDirectory(new File("dist" + File.separator + "liblouis"));

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    protected static boolean comparePEFs(File correctPEF,
                                         File testPEF)
                                  throws Exception{

        Diff myDiff = new Diff(new FileReader(correctPEF),
                               new FileReader(testPEF));

        myDiff.overrideDifferenceListener(new DifferenceListener() {
            public int differenceFound(Difference dfrnc) {
                if (dfrnc.getId() == DifferenceConstants.TEXT_VALUE_ID) {
                    if ("dc".equals(dfrnc.getControlNodeDetail().getNode().getParentNode().getPrefix())) {
                        return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                    }
                }
                return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
            }
            public void skippedComparison(Node node, Node node1) {}
        });

        boolean identical = myDiff.identical();
        
        assertTrue("PEFs not equal\n" + myDiff, identical);
        
        return identical;
    }
    
    protected static Configuration getBasicConfiguration(ODT odt) throws Exception {
        
        Configuration configuration = odt.getConfiguration();
        configuration.setBraillePageNumbers(false);
        configuration.setPageSeparator(false);
        return configuration;
    }

    protected static ConversionResult convertODT2PEF(ODT odt,
                                         Configuration configuration,
                                         PEFConfiguration pefConfiguration) 
                                  throws Exception {
        
        ODT2PEFConverter pefConverter = new ODT2PEFConverter();
        ODT2PEFConverterParameters parameters = new ODT2PEFConverterParameters(configuration, pefConfiguration);
        for (Map.Entry<String,Object> parameter : parameters) {
            pefConverter.setParameter(parameter.getKey(), parameter.getValue());
        }
        return new ConversionResult(odt, pefConverter, pefConverter.convert(odt));
    }
    
    protected static ConversionResult convertODT2PEF(ODT odt, Configuration configuration) throws Exception {
        return convertODT2PEF(odt, configuration, new ExportConfiguration());
    }
    
    protected static ConversionResult convertODT2PEF(ODT odt) throws Exception {
        return convertODT2PEF(odt, getBasicConfiguration(odt));
    }
    
    protected static ConversionResult convertODT2PEF(File odtFile) throws Exception {
        return convertODT2PEF(new ODT(odtFile));
    }
    
    public static class ConversionResult {
        
        private final ODT odt;
        private final Converter converter;
        private final PEF pef;
        
        private ConversionResult(ODT odt, Converter converter, PEF pef) {
            this.odt = odt;
            this.converter = converter;
            this.pef = pef;
        }
        
        public File getPEFFile() {
            return pef.getSinglePEF();
        }
        
        public void cleanUp() {
            odt.close();
            converter.cleanUp();
            pef.close();
            getPEFFile().delete();
        }
    }
}
