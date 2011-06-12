package be.docarch.odt2braille.checker;

import java.io.File;
import java.util.Date;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.TransformerFactoryImpl;

import be.docarch.accessibility.Check;
import be.docarch.accessibility.RemoteRunnableChecker;
import be.docarch.accessibility.Report;
import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.OdtTransformer;
import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.Volume;

import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * @author Bert Frees
 */
public class BrailleChecker implements RemoteRunnableChecker {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private File odtFile = null;
    private File earlReport = null;
    private String reportName = null;
    private Transformer earlXSL = null;
    private Date lastChecked = null;
    private SimpleDateFormat dateFormat = null;
    private Map<String,Check> checks = null;

    public BrailleChecker() {

        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        try {
            
            TransformerFactoryImpl tFactory = new net.sf.saxon.TransformerFactoryImpl();
            earlXSL = tFactory.newTransformer(
                    new StreamSource(getClass().getResource(Constants.XSLT_PATH + "earl.xsl").toString()));

            earlXSL.setParameter("paramNoBrailleToc",         "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_NoBrailleToc.name());
            earlXSL.setParameter("paramNotInBrailleVolume",   "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_NotInBrailleVolume.name());
            earlXSL.setParameter("paramOmittedInBraille",     "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_OmittedInBraille.name());
            earlXSL.setParameter("paramTransposedInBraille",  "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_TransposedInBraille.name());
            earlXSL.setParameter("paramUnnaturalVolumeBreak", "http://www.docarch.be/accessibility/checks#" + BrailleCheck.ID.A_UnnaturalVolumeBreak.name());

            earlReport = File.createTempFile(Constants.TMP_PREFIX, ".earl.rdf.xml", Constants.getTmpDirectory());
            earlReport.deleteOnExit();

            checks = new HashMap<String,Check>();
            checks.put(BrailleCheck.ID.A_NoBrailleToc.name(),         new BrailleCheck(BrailleCheck.ID.A_NoBrailleToc));
            checks.put(BrailleCheck.ID.A_NotInBrailleVolume.name(),   new BrailleCheck(BrailleCheck.ID.A_NotInBrailleVolume));
            checks.put(BrailleCheck.ID.A_OmittedInBraille.name(),     new BrailleCheck(BrailleCheck.ID.A_OmittedInBraille));
            checks.put(BrailleCheck.ID.A_TransposedInBraille.name(),  new BrailleCheck(BrailleCheck.ID.A_TransposedInBraille));
            checks.put(BrailleCheck.ID.A_UnnaturalVolumeBreak.name(), new BrailleCheck(BrailleCheck.ID.A_UnnaturalVolumeBreak));

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setOdtFile(File odtFile) {
        this.odtFile = odtFile;
    }

    @Override
    public Collection<Check> getChecks() {
        return checks.values();
    }

    @Override
    public Report getAccessibilityReport() {
        return new Report(earlReport, reportName);
    }

    @Override
    public boolean run() {

        if (odtFile == null) { return false; }

        lastChecked = new Date();
        reportName = getIdentifier() + "/" + new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(lastChecked) + ".rdf";

        try {

            OdtTransformer odtTransformer = new OdtTransformer(odtFile);
            Settings settings = new Settings(odtTransformer);

              // TODO: load settings from odt-file

            odtTransformer.configure(settings);
            odtTransformer.makeControlFlow();

            settings.configureVolumes();
            boolean tocEnabled = false;
            for (Volume volume : settings.getVolumes()) {
                if (volume.getToc()) {
                    tocEnabled = true;
                    break;
                }
            }

            earlXSL.setParameter("paramTocEnabled", tocEnabled);
            earlXSL.setParameter("paramTimestamp", dateFormat.format(lastChecked));
            earlXSL.setParameter("content-url", odtTransformer.getOdtContentFile().toURI());
            //earlXSL.setParameter("meta-url", odtTransformer.getOdtMetaFile().toURI());
            earlXSL.transform(new StreamSource(odtTransformer.getControllerFile()), new StreamResult(earlReport));
            odtTransformer.close();
            
            return true;

        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
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
