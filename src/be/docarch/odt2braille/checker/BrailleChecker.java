package be.docarch.odt2braille.checker;

import java.io.File;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;

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

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille");

    private File odtFile = null;
    private File earlReport = null;
    private Transformer earlXSL = null;
    private Date lastChecked = null;
    private SimpleDateFormat dateFormat = null;

    public BrailleChecker() {

        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        try {
            TransformerFactoryImpl tFactory = new net.sf.saxon.TransformerFactoryImpl();
            earlXSL = tFactory.newTransformer(
                    new StreamSource(getClass().getResource("/be/docarch/odt2braille/xslt/earl.xsl").toString()));

            earlXSL.setParameter("paramNoPreliminarySection", "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_NoPreliminarySection.name());
            earlXSL.setParameter("paramNoTitlePage",          "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_NoTitlePage.name());
            earlXSL.setParameter("paramNoBrailleToc",         "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_NoBrailleToc.name());
            earlXSL.setParameter("paramNotInBrailleVolume",   "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_NotInBrailleVolume.name());
            earlXSL.setParameter("paramOmittedInBraille",     "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_OmittedInBraille.name());
            earlXSL.setParameter("paramTransposedInBraille",  "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_TransposedInBraille.name());
            earlXSL.setParameter("paramUnnaturalVolumeBreak", "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_UnnaturalVolumeBreak.name());

            earlReport = File.createTempFile("odt2braille.", ".earl.rdf.xml");
            earlReport.deleteOnExit();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
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

        lastChecked = new Date();

        try {

            OdtTransformer odtTransformer = new OdtTransformer(odtFile, null, null);
            odtTransformer.makeControlFlow();
            earlXSL.setParameter("paramTimestamp", dateFormat.format(lastChecked));
            earlXSL.setParameter("content-url", odtTransformer.getOdtContentFile().toURI());
            earlXSL.setParameter("meta-url", odtTransformer.getOdtMetaFile().toURI());
            earlXSL.transform(new StreamSource(odtTransformer.getControllerFile()), new StreamResult(earlReport));

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public String getIdentifier() {
        return "be.docarch.odt2braille.checker.BrailleChecker";
    }

    public Date getLastChecked() {

        try {
            if (lastChecked != null) {
                return dateFormat.parse(dateFormat.format(lastChecked));
            } else {
                return null;
            }
        } catch (java.text.ParseException ex) {
            return null;
        }
    }
}
