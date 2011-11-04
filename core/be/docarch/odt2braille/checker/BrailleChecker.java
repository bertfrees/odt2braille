package be.docarch.odt2braille.checker;

import java.io.File;
import java.net.URI;
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

import be.docarch.accessodf.Check;
import be.docarch.accessodf.RemoteRunnableChecker;
import be.docarch.accessodf.Report;
import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.ODT;

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

    static {
        try {
            String uri = BrailleChecker.class.getResource(".").toURI().toString();
            uri = uri.substring(4, uri.lastIndexOf("lib/odt2braille.jar!")) + "liblouis";
            Constants.setLiblouisDirectory(new File(new URI(uri)));
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public BrailleChecker() {

        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        try {
            
            TransformerFactoryImpl tFactory = new net.sf.saxon.TransformerFactoryImpl();
            earlXSL = tFactory.newTransformer(
                    new StreamSource(getClass().getResource(Constants.XSLT_PATH + "earl.xsl").toString()));

            earlXSL.setParameter("checkerID",                 getIdentifier());
            earlXSL.setParameter("paramNoBrailleToc",         be.docarch.accessodf.Constants.A11Y_CHECKS + BrailleCheck.ID.A_NoBrailleToc.name());
            earlXSL.setParameter("paramOmittedCaption",       be.docarch.accessodf.Constants.A11Y_CHECKS + BrailleCheck.ID.A_OmittedCaption.name());

            earlReport = File.createTempFile(Constants.TMP_PREFIX, ".earl.rdf.xml", Constants.getTmpDirectory());
            earlReport.deleteOnExit();

            checks = new HashMap<String,Check>();
            checks.put(BrailleCheck.ID.A_NoBrailleToc.name(),    new BrailleCheck(BrailleCheck.ID.A_NoBrailleToc));
            checks.put(BrailleCheck.ID.A_OmittedCaption.name(),  new BrailleCheck(BrailleCheck.ID.A_OmittedCaption));

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public Collection<Check> list() {
        return checks.values();
    }

    public Check get(String identifier) {
        return checks.get(identifier);
    }

    public void setOdtFile(File odtFile) {
        this.odtFile = odtFile;
    }

    public Report getAccessibilityReport() {
        return new Report(earlReport, reportName);
    }

    public boolean run() {

        if (odtFile == null) { return false; }

        lastChecked = new Date();
        reportName = "be.docarch.odt2braille.checker.BrailleChecker/"
                        + new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(lastChecked) + ".rdf";

        try {

            ODT odt = new ODT(odtFile);
            
            // TODO: load (certain) settings from odt-file

            odt.makeControlFlow();

            earlXSL.setParameter("paramTocEnabled", true);
            earlXSL.setParameter("paramTimestamp", dateFormat.format(lastChecked));
            earlXSL.setParameter("content-url", odt.getOdtContentFile().toURI());
            earlXSL.transform(new StreamSource(odt.getControllerFile()), new StreamResult(earlReport));
            odt.close();

            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }

        return false;
    }

    public String getIdentifier() {
        return "http://docarch.be/odt2braille/checker/BrailleChecker";
    }
}
