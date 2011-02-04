package be.docarch.odt2braille.checker;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;

import be.docarch.accessibility.URIs;
import be.docarch.accessibility.Check;
import be.docarch.accessibility.ExternalChecker;
import be.docarch.odt2braille.OdtTransformer;

import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;


/**
 *
 * @author Bert Frees
 */
public class BrailleChecker implements ExternalChecker {

    private File odtFile = null;
    private File earlReport = null;
    private Transformer earlXSL = null;

    public BrailleChecker() {

        try {
            TransformerFactoryImpl tFactory = new net.sf.saxon.TransformerFactoryImpl();
            earlXSL = tFactory.newTransformer(
                    new StreamSource(getClass().getResource("/be/docarch/odt2braille/xslt/earl.xsl").toString()));

            earlReport = File.createTempFile("odt2braille.", ".earl.rdf.xml");
            earlReport.deleteOnExit();
        } catch (IOException ex) {
        } catch (TransformerConfigurationException ex) {
        }
    }

    public void setOdtFile(File odtFile) {
        this.odtFile = odtFile;
    }

    public Set<Check> getChecks() {

        Set<Check> checks = new HashSet<Check>();
        for (BrailleCheck.ID id : BrailleCheck.ID.values()) {
            checks.add(new BrailleCheck(id));
        }
        return checks;
    }

    public Check getCheck(String identifier) {

        BrailleCheck.ID id = BrailleCheck.ID.valueOf(identifier);
        if (id == null) {
            return null;
        } else {
            return new BrailleCheck(id);
        }
    }

    public File getAccessibilityReport() {
        return earlReport;
    }

    public void check() {

        try {

            OdtTransformer odtTransformer = new OdtTransformer(odtFile, null, null);
            odtTransformer.makeControlFlow();
            earlXSL.transform(new StreamSource(odtTransformer.getControllerFile()), new StreamResult(earlReport));

        } catch (IOException ex) {
        } catch (TransformerException ex) {
        }
    }
}
