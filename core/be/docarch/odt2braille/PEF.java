/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ProcessingInstruction;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

import be.docarch.odt2braille.Settings.VolumeManagementMode;
import be.docarch.odt2braille.checker.PostConversionBrailleChecker;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.pef.PEFValidator;
import org.daisy.braille.pef.PEFFileSplitter;
import org.daisy.validator.ValidatorFactory;
import org.daisy.validator.Validator;

/**
 * This class provides a way to convert a flat .odt file to a
 * <a href="http://www.daisy.org/projects/braille/braille_workarea/pef.html">.pef (portable embosser format)</a> file.
 * The conversion is done according to previously defined braille {@link Settings}.
 * <code>liblouisxml</code> is used for the actual transcription to braille.
 * A {@link PostConversionBrailleChecker} checks the resulting braille document for possible accessibility issues.
 *
 * @see <a href="http://code.google.com/p/liblouisxml/"><code>liblouisxml</code></a>
 * @author Bert Frees
 */
public class PEF {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static NamespaceContext namespace = new NamespaceContext();

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();
    private static final String L10N = Constants.L10N_PATH;
    private static final String pefNS = "http://www.daisy.org/ns/2008/pef";

    public enum TranscribersNote { };
    enum State {HEADER, BODY, FOOTER};

    private File pefFile;
    private LiblouisXML liblouisXML = null;
    private OdtTransformer odtTransformer = null;
    private Settings settings = null;
    private StatusIndicator statusIndicator = null;
    private PostConversionBrailleChecker checker = null;
    private Validator validator = null;

    BrailleConverter liblouisTable = new LiblouisTable().newBrailleConverter();

    public PEF(Settings settings,
               LiblouisXML liblouisXML)
        throws IOException,
               TransformerException {

        this(settings, liblouisXML, null, null);

    }

    /**
     * Creates a new <code>PEF</code> instance.
     *
     * @param flatOdtFile       The "flat XML" .odt file.
     *                          This single file is the concatenation of all XML files in a normal .odt file.
     * @param liblouisDirUrl    The URL of the liblouis executable. liblouis is used for the actual transcription to braille.
     * @param statusIndicator   The <code>StatusIndicator</code> that will be used.
     * @param settings          The <code>Settings</code> that determine how the conversion is done.
     * @param checker           The <code>PostConversionBrailleChecker</code> that will check the braille document for possible accessibility issues.
     * @param oooLocale         The <code>Locale</code> for the user interface.
     */
    public PEF(Settings settings,
               LiblouisXML liblouisXML,
               StatusIndicator statusIndicator,
               PostConversionBrailleChecker checker)
        throws IOException,
               TransformerException {

        logger.entering("PEF", "<init>");

        this.settings = settings;
        this.liblouisXML = liblouisXML;
        this.statusIndicator = statusIndicator;
        this.checker = checker;

        settings.configureVolumes();
        odtTransformer = settings.odtTransformer;
        odtTransformer.configure(settings);

        pefFile = File.createTempFile(TMP_NAME, ".pef", TMP_DIR);
        pefFile.deleteOnExit();

        // odtTransformer preProcessing
        odtTransformer.ensureMetadataReferences();
        odtTransformer.makeControlFlow();

        // Initialize liblouisXML
        liblouisXML.createStylesFiles();

        // Validator
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); {

            ValidatorFactory factory = ValidatorFactory.newInstance();
            validator = factory.newValidator(PEFValidator.class.getCanonicalName());
            validator.setFeature(PEFValidator.FEATURE_MODE, PEFValidator.Mode.LIGHT_MODE);

        } Thread.currentThread().setContextClassLoader(cl);

        logger.exiting("PEF", "<init>");
    }

    /**
     * Converts the flat .odt filt to a .pef file according to the braille settings.
     *
     * This function
     * <ul>
     * <li>uses {@link OdtTransformer} to convert the .odt file to multiple DAISY-like xml files,</li>
     * <li>uses {@link LiblouisXML} to translate these files into braille, and</li>
     * <li>recombines these braille files into one single .pef file.</li>
     * </ul>
     *
     * First, the document <i>body</i> is processed and split in volumes, then the <i>page ranges</i> are calculated
     * and finally the <i>preliminary pages</i> of each volume are processed and inserted at the right places.
     * The checker checks the DAISY-like files and the volume lengths.
     *
     */

    public boolean makePEF() throws IOException,
                                    ParserConfigurationException,
                                    TransformerException,
                                    InterruptedException,
                                    SAXException,
                                    LiblouisXMLException {

        logger.entering("PEF", "makePEF");

        DocumentBuilderFactory docFactory;
        DocumentBuilder docBuilder;
        Document document;
        Element headElement;
        Element metaElement;
        Element dcElement;
        Element bodyElement;
        Element[] volumeElements;
        Element sectionElement;
        Element pageElement;
        Element rowElement;
        Node node;
        BufferedReader bufferedReader = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        String brfInput = null;
        String line = null;
        int lineCount;
        int pageCount;
        int beginPage;
        int volumeCount;
        boolean cont;
        char ch;
        Matcher matcher;
        Volume volume;
        File bodyFile;
        File preliminaryFile;
        List<SpecialSymbol> specialSymbolsList = settings.getSpecialSymbolsList();

        List<Volume>volumes = settings.getVolumes();
        volumeElements = new Element[volumes.size()];

        File brailleFile = File.createTempFile(TMP_NAME, ".txt", TMP_DIR);
        brailleFile.deleteOnExit();

        try {

            docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setValidating(false);
            docFactory.setNamespaceAware(true);
            docBuilder = docFactory.newDocumentBuilder();
            DOMImplementation impl = docBuilder.getDOMImplementation();

            document = impl.createDocument(pefNS, "pef", null);
            Element root = document.getDocumentElement();
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", pefNS);
            root.setAttributeNS(null,"version","2008-1");

            headElement = document.createElementNS(pefNS,"head");
            metaElement = document.createElementNS(pefNS,"meta");
            metaElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:dc", "http://purl.org/dc/elements/1.1/");
            dcElement = document.createElementNS("http://purl.org/dc/elements/1.1/","dc:identifier");
            node = document.createTextNode("00001");
            dcElement.appendChild(node);
            metaElement.appendChild(dcElement);
            dcElement = document.createElementNS("http://purl.org/dc/elements/1.1/","dc:format");
            node = document.createTextNode("application/x-pef+xml");
            dcElement.appendChild(node);
            metaElement.appendChild(dcElement);
            headElement.appendChild(metaElement);

            root.appendChild(headElement);

            // Split body into volumes and extract page ranges

            beginPage = settings.getBeginningBraillePageNumber();

            bodyFile = File.createTempFile(TMP_NAME, ".daisy.body.xml", TMP_DIR);
            bodyFile.deleteOnExit();

            odtTransformer.getBodyMatter(bodyFile);
            if (checker != null) {
                checker.checkDaisyFile(bodyFile);
            }
            liblouisXML.configure(bodyFile, brailleFile, false, beginPage);
            liblouisXML.run();

            fileInputStream = new FileInputStream(brailleFile);
            inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            int steps = 1;
            for (Volume v : volumes) {
                if (v.getFrontMatter() ||
                    v.getToc() ||
                    v.getTranscribersNotesPage() ||
                    v.getSpecialSymbolsList()) {
                    steps++;
                }
            }

            if (statusIndicator != null) {
                statusIndicator.start();
                statusIndicator.setSteps(steps);
                statusIndicator.setStatus(ResourceBundle.getBundle(L10N, statusIndicator.getPreferredLocale()).getString("statusIndicatorStep"));
            }

            for (volumeCount=0; volumeCount<volumes.size(); volumeCount++) {

                volume = volumes.get(volumeCount);

                logger.info("Processing body of volume " + (volumeCount + 1) + " : " + volume.getType().name());

                volumeElements[volumeCount] = document.createElementNS(pefNS, "volume");
                volumeElements[volumeCount].setAttributeNS(null, "cols", String.valueOf(settings.getCellsPerLine()));
                volumeElements[volumeCount].setAttributeNS(null, "rows", String.valueOf(settings.getLinesPerPage()));
                volumeElements[volumeCount].setAttributeNS(null, "rowgap", settings.getEightDots()?"1":"0");
                volumeElements[volumeCount].setAttributeNS(null, "duplex", settings.getDuplex()?"true":"false");

                if (!(volume instanceof PreliminaryVolume)) {

                    sectionElement = document.createElementNS(pefNS, "section");

                    cont = true;
                    volume.setBraillePagesStart(beginPage);
                    pageCount = 0;
                    while (cont) {

                        pageElement = document.createElementNS(pefNS,"page");

                        pageCount++;
                        lineCount = 1;
                        while (lineCount <= settings.getLinesPerPage()) {

                            line = bufferedReader.readLine();
                            line = line.replaceAll("\u2800","\u0020")
                                       .replaceAll("\u00A0","\u0020")
                                       .replaceAll("\uE00F","\u002D");
                            if (IS_WINDOWS) { bufferedReader.readLine(); }
                            if (line.contains("\uE000")) {
                                line = line.replaceAll("\uE000","\u0020");
                                cont = false;
                                volume.setNumberOfBraillePages(pageCount);
                            }
                            rowElement = document.createElementNS(pefNS,"row");
                            node = document.createTextNode(liblouisTable.toBraille(line));
                            rowElement.appendChild(node);
                            pageElement.appendChild(rowElement);

                            lineCount++;
                        }

                        sectionElement.appendChild(pageElement);
                        bufferedReader.skip(1);
                    }

                    beginPage += pageCount;

                    volumeElements[volumeCount].appendChild(sectionElement);
                }
            }

            if (statusIndicator != null) {
                statusIndicator.increment();
            }

            if (bufferedReader != null) {
                bufferedReader.close();
                inputStreamReader.close();
                fileInputStream.close();
            }

            // Insert preliminary sections before body of volumes 1,2,3,...

            for (volumeCount=0; volumeCount<volumes.size(); volumeCount++) {

                volume = volumes.get(volumeCount);

                if (volume.getFrontMatter() ||
                    volume.getToc() ||
                    volume.getTranscribersNotesPage() ||
                    volume.getSpecialSymbolsList()) {

                    logger.log(Level.INFO, "Processing preliminary pages of volume " + (volumeCount + 1) + " : " + volume.getType().name());

                    // Print page range

                    if (settings.getVolumeInfoEnabled() &&
                        volume.getFrontMatter() &&
                        !(volume instanceof PreliminaryVolume)) {

                        String volumeNode = "dtb:volume";
                        if (volume instanceof SectionVolume) {
                            volumeNode += "[@id='" + ((SectionVolume)volume).getSectionName()  + "']";
                        } else if (volume instanceof AutomaticVolume) {
                            volumeNode += "[@id='" + ((AutomaticVolume)volume).getIdentifier() + "']";
                        }

                        String s;
                        if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                            "//" + volumeNode +
                                            "/*[not(self::dtb:pagebreak or ancestor::dtb:div[@class='not-in-volume'])][1][self::dtb:pagenum]", namespace)) {
                            s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                            "//" + volumeNode +
                                            "/*[not(self::dtb:pagebreak or ancestor::dtb:div[@class='not-in-volume'])][1][self::dtb:pagenum]", namespace);
                        } else {
                            s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                            "//" + volumeNode +
                                            "/*[not(ancestor::dtb:div[@class='not-in-volume'])][1]/preceding::dtb:pagenum[1]", namespace);
                        }
                        if (s.equals("")){
                            if (settings.getMergeUnnumberedPages()) {
                                s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                            "//" + volumeNode +
                                            "/*[not(self::dtb:div[@class='not-in-volume'])][1]/preceding::dtb:pagenum[text()][1]", namespace);
                            } else {
                                s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                            "//" + volumeNode +
                                            "//dtb:pagenum[text() and not(ancestor::dtb:div[@class='not-in-volume'])][1]", namespace);
                            }
                        }
                        if (!s.equals("")){
                            volume.setFirstPrintPage(s);
                            s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                           "//" + volumeNode + "//dtb:pagenum[" +
                                           "text() and not(ancestor::dtb:div[@class='not-in-volume']) and not(following::dtb:pagenum[ancestor::" +
                                           volumeNode + " and text() and not(ancestor::dtb:div[@class='not-in-volume'])])]", namespace);
                            if (!(s.equals("") || s.equals(volume.getFirstPrintPage()))) {
                                volume.setLastPrintPage(s);
                            }
                        }
                    }

                    // Determine which symbols to display in list of special symbols

                    if (volume.getSpecialSymbolsList()) {

                        ArrayList<Boolean> specialSymbolsPresent = new ArrayList();
                        boolean specialSymbolPresent;

                        String volumeNode = "dtb:volume";
                        if (volume instanceof SectionVolume) {
                            volumeNode += "[@id='" + ((SectionVolume)volume).getSectionName()  + "']";
                        } else if (volume instanceof AutomaticVolume) {
                            volumeNode += "[@id='" + ((AutomaticVolume)volume).getIdentifier() + "']";
                        }

                        for (int i=0; i<specialSymbolsList.size(); i++) {

                            specialSymbolPresent = false;

                            switch (specialSymbolsList.get(i).getMode()) {
                                case NEVER:
                                    break;
                                case ALWAYS:
                                    specialSymbolPresent = true;
                                    break;
                                case FIRST_VOLUME:
                                    if (volumeCount == 0) { specialSymbolPresent = true; }
                                    break;
                                case IF_PRESENT_IN_VOLUME:
                                    if (!(volume instanceof PreliminaryVolume)) {
                                        switch (specialSymbolsList.get(i).getType()) {
                                            case NOTE_REFERENCE_INDICATOR:
                                                specialSymbolPresent = XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:note[@class='footnote' or @class='endnote']",namespace);
                                                break;
                                            case TRANSCRIBERS_NOTE_INDICATOR:
                                                specialSymbolPresent = XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:div[@class='tn']/dtb:note",namespace);
                                                break;
                                            case ITALIC_INDICATOR:
                                                specialSymbolPresent = XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:em[not(@class='reset')]",namespace);
                                                break;
                                            case BOLDFACE_INDICATOR:
                                                specialSymbolPresent = XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:strong[not(@class='reset')]",namespace);
                                                break;
                                            case ELLIPSIS:
                                                specialSymbolPresent = XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:flag[@class='ellipsis']",namespace);
                                                break;
                                            case DOUBLE_DASH:
                                                specialSymbolPresent = XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                   "//" +  volumeNode + "//dtb:flag[@class='double-dash']",namespace);
                                                break;
                                            default:
                                        }
                                    }
                                    break;
                            }

                            specialSymbolsPresent.add(specialSymbolPresent);
                        }

                        volume.setSpecialSymbolsPresent(specialSymbolsPresent);
                    }

                    // Determine which notes to display on transcriber's note page

                    if (volume.getTranscribersNotesPage()) {

                        TranscribersNote[] transcribersNoteValues = TranscribersNote.values();
                        ArrayList<Boolean> transcribersNotesEnabled = new ArrayList();
                        boolean transcribersNoteEnabled;

                        for (int i=0;i <transcribersNoteValues.length; i++) {

                            transcribersNoteEnabled = false;

                            switch (transcribersNoteValues[i]) {
                                default:
                                    transcribersNoteEnabled = false;
                            }

                            transcribersNotesEnabled.add(transcribersNoteEnabled);
                        }

                        volume.setTranscribersNotesEnabled(transcribersNotesEnabled);
                    }

                    // Extract preliminary page range

                    preliminaryFile = File.createTempFile(TMP_NAME, ".daisy." + (volumeCount + 1) + ".xml", TMP_DIR);
                    preliminaryFile.deleteOnExit();

                    sectionElement = document.createElementNS(pefNS,"section");

                    odtTransformer.getFrontMatter(preliminaryFile, volume);
                    liblouisXML.configure(preliminaryFile, brailleFile, true, volume.getToc()?volume.getFirstBraillePage():1);
                    liblouisXML.run();

                    fileInputStream = new FileInputStream(brailleFile);
                    inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
                    brfInput = IOUtils.toString(inputStreamReader);

                    matcher = Pattern.compile("(\f|\uE000)").matcher(brfInput);
                    pageCount = 1;

                    while (matcher.find()) {
                        ch = brfInput.charAt(matcher.start());
                        if (ch=='\f') {
                            pageCount ++;
                        } else {
                            if (volume.getToc()) {
                                pageCount --;
                            }
                            break;
                        }
                    }

                    volume.setNumberOfPreliminaryPages(pageCount);

                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                        fileInputStream.close();
                    }

                    // Get preliminary pages

                    odtTransformer.getFrontMatter(preliminaryFile, volume);
                    if (checker != null) {
                        checker.checkDaisyFile(preliminaryFile);
                    }
                    liblouisXML.configure(preliminaryFile, brailleFile, false, volume.getToc()?volume.getFirstBraillePage():1);
                    liblouisXML.run();

                    fileInputStream = new FileInputStream(brailleFile);
                    inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
                    bufferedReader = new BufferedReader(inputStreamReader);

                    pageCount = 1;

                    while (pageCount <= volume.getNumberOfPreliminaryPages()) {

                        pageElement = document.createElementNS(pefNS,"page");
                        lineCount = 1;

                        while (lineCount <= settings.getLinesPerPage()) {

                            line = bufferedReader.readLine();
                            line = line.replaceAll("\u2800","\u0020")
                                       .replaceAll("\u00A0","\u0020")
                                       .replaceAll("\uE00F","\u002D");
                            if (IS_WINDOWS) { bufferedReader.readLine(); }
                            rowElement = document.createElementNS(pefNS,"row");
                            node = document.createTextNode(liblouisTable.toBraille(line));
                            rowElement.appendChild(node);
                            pageElement.appendChild(rowElement);

                            lineCount++;
                        }

                        sectionElement.appendChild(pageElement);
                        bufferedReader.skip(1);
                        pageCount ++;
                    }

                    volumeElements[volumeCount].insertBefore(sectionElement, volumeElements[volumeCount].getFirstChild());

                    if (statusIndicator != null) {
                        statusIndicator.increment();
                    }

                    if (bufferedReader != null) {
                        bufferedReader.close();
                        inputStreamReader.close();
                        fileInputStream.close();
                    }
                }
            }

            if (checker != null) {
                checker.checkVolumes(volumes);
            }

            bodyElement = document.createElementNS(pefNS,"body");

            for (volumeCount=0; volumeCount<volumes.size(); volumeCount++) {
                bodyElement.appendChild(volumeElements[volumeCount]);
            }

            root.appendChild(bodyElement);

            document.insertBefore((ProcessingInstruction)document.createProcessingInstruction(
                    "xml-stylesheet","type='text/css' href='pef.css'"), document.getFirstChild());

            OdtUtils.saveDOM(document, pefFile);

            logger.exiting("PEF","makePEF");

            if (!validatePEF(pefFile)) {
                return false;
            }

            return true;

        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
                inputStreamReader.close();
                fileInputStream.close();
            }
        }
    }

    private boolean validatePEF(File pefFile)
                         throws IOException,
                                MalformedURLException {

        logger.entering("PEF", "validatePEF");

        if (validator.validate(pefFile.toURI().toURL())) {

            logger.info("pef valid");
            return true;

        } else {

            String message = "pef invalid!\nMessages returned by the validator:\n";
            InputStreamReader report = new InputStreamReader(validator.getReportStream());
            int c;
            while ((c = report.read()) != -1) {
                message += (char)c;
            }
            logger.log(Level.SEVERE, message);

            return false;
        }
    }

    public File getSinglePEF() {

        logger.entering("PEF", "getSinglePEF");

        return pefFile;
    }

    public File[] getPEFs() {

        logger.entering("PEF", "getPEFs");

        if (settings.getVolumeManagementMode() == VolumeManagementMode.SINGLE) {
            return new File[] { pefFile };
        } else {
            File[] pefFiles = splitPEF();
            if (pefFiles != null) {
                return pefFiles;
            } else {
                return null;
            }
        }
    }

    /*
     * Split a single PEF file into several files, one file per volume.
     */
    private File[] splitPEF() {

        logger.entering("PEF", "splitPEF");

        File input = pefFile;
        File output = new File(input.getAbsolutePath() + "-split");
        output.mkdir();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); {

            PEFFileSplitter splitter = new PEFFileSplitter();
            splitter.split(input, output);

        } Thread.currentThread().setContextClassLoader(cl);

        return output.listFiles();
    }
}
