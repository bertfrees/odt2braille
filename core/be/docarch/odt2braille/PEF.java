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

import be.docarch.odt2braille.setup.SpecialSymbol;
import be.docarch.odt2braille.setup.PEFConfiguration;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.Configuration.VolumeManagementMode;
import be.docarch.odt2braille.setup.EmbossConfiguration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.checker.PostConversionBrailleChecker;

import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.pef.PEFValidator;
import org.daisy.braille.pef.PEFFileSplitter;
import org.daisy.validator.ValidatorFactory;
import org.daisy.validator.Validator;

/**
 * This class provides a way to convert a flat .odt file to a
 * <a href="http://www.daisy.org/projects/braille/braille_workarea/pef.html">.pef (portable embosser format)</a> file.
 * The conversion is done according to previously defined braille {@link Configuration}.
 * <code>liblouisxml</code> is used for the actual transcription to braille.
 * A {@link PostConversionBrailleChecker} checks the resulting braille document for possible accessibility issues.
 *
 * @see <a href="http://code.google.com/p/liblouisxml/"><code>liblouisxml</code></a>
 * @author Bert Frees
 */
public class PEF {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static NamespaceContext namespace = new NamespaceContext();

    private static final BrailleConverter liblouisTable = new LiblouisTable().newBrailleConverter();

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();
    private static final String L10N = Constants.L10N_PATH;
    private static final String pefNS = "http://www.daisy.org/ns/2008/pef";

    private final File pefFile;
    private final LiblouisXML liblouisXML;
    private final OdtTransformer odtTransformer;
    private final Configuration settings;
    private final PEFConfiguration pefSettings;
    private final StatusIndicator statusIndicator;
    private final PostConversionBrailleChecker checker;
    private final Validator validator;

    private final List<Volume> volumes;

    public PEF(Configuration settings,
               PEFConfiguration pefSettings,
               LiblouisXML liblouisXML)
        throws IOException,
               TransformerException {

        this(settings, pefSettings, liblouisXML, null, null);

    }

    /**
     * Creates a new <code>PEF</code> instance.
     *
     * @param flatOdtFile       The "flat XML" .odt file.
     *                          This single file is the concatenation of all XML files in a normal .odt file.
     * @param liblouisDirUrl    The URL of the liblouis executable. liblouis is used for the actual transcription to braille.
     * @param statusIndicator   The <code>StatusIndicator</code> that will be used.
     * @param settings          The <code>Configuration</code> that determine how the conversion is done.
     * @param checker           The <code>PostConversionBrailleChecker</code> that will check the braille document for possible accessibility issues.
     * @param oooLocale         The <code>Locale</code> for the user interface.
     */
    public PEF(Configuration settings,
               PEFConfiguration pefSettings,
               LiblouisXML liblouisXML,
               StatusIndicator statusIndicator,
               PostConversionBrailleChecker checker)
        throws IOException,
               TransformerException {

        logger.entering("PEF", "<init>");

        this.settings = settings;
        this.pefSettings = pefSettings;
        this.liblouisXML = liblouisXML;
        this.statusIndicator = statusIndicator;
        this.checker = checker;

        pefFile = File.createTempFile(TMP_NAME, ".pef", TMP_DIR);
        pefFile.deleteOnExit();

        odtTransformer = settings.odtTransformer;
        odtTransformer.configure(settings);
        odtTransformer.ensureMetadataReferences();
        odtTransformer.makeControlFlow();

        volumes = new ArrayList<Volume>();

        if (settings.getPreliminaryVolumeEnabled()) {
            volumes.add(new PreliminaryVolume(settings.getPreliminaryVolume()));
        }
        switch (settings.getBodyMatterMode()) {
            case SINGLE:
                volumes.add(new Volume(settings.getBodyMatterVolume()));
                break;
            case AUTOMATIC:
                volumes.addAll(VolumeSplitter.splitBodyMatterVolume(settings));
                break;
        }
        for (Configuration.SectionVolume volume : settings.getSectionVolumeList().values()) {
            volumes.add(new Volume(volume, volume.getSection()));
        }
        if (settings.getRearMatterSection() != null &&
            settings.getRearMatterMode() == VolumeManagementMode.SINGLE) {
            volumes.add(new Volume(settings.getRearMatterVolume()));
        }

        int i = 1;
        for (Volume v : volumes) {
            String title = v.getTitle();
            if (title.contains("@i")) {
                v.setTitle(title.replaceFirst("@i", String.valueOf(i)));
                i++;
            }
        }
        for (Volume v : volumes) {
            if (v.getFrontMatter()) {
                v.setExtendedFrontMatter(true);
                break;
            }
        }
        for (Volume v : volumes) {
            if (v.getTableOfContent()) {
                v.setExtendedTableOfContent(true);
                break;
            }
        }

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


    public List<Volume> getVolumes() {
        return volumes;
    }


    /**
     * Converts the flat .odt filt to a .pef file according to the braille settings.
     *
     * This function
     * <ul>
     * <li>uses {@link ODTTransformer} to convert the .odt file to multiple DAISY-like xml files,</li>
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

        String volumeInfo = capitalizeFirstLetter(
                ResourceBundle.getBundle(L10N, settings.mainLocale).getString("in")) + " " + volumes.size() + " " +
                ResourceBundle.getBundle(L10N, settings.mainLocale).getString((volumes.size()>1) ? "volumes" : "volume") + "\n@title\n@pages";

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
                    v.getTableOfContent() ||
                    v.getTranscribersNotesPageEnabled() ||
                    v.getSpecialSymbolListEnabled()) {
                    steps++;
                }
            }

            if (statusIndicator != null) {
                statusIndicator.start();
                statusIndicator.setSteps(steps);
                statusIndicator.setStatus(ResourceBundle.getBundle(L10N, statusIndicator.getPreferredLocale()).getString("statusIndicatorStep"));
            }

            int columns = pefSettings.getColumns();
            int rows = pefSettings.getRows();
            boolean eightDots = pefSettings.getEightDots();
            boolean duplex = pefSettings.getDuplex();

            for (volumeCount=0; volumeCount<volumes.size(); volumeCount++) {

                volume = volumes.get(volumeCount);

                logger.info("Processing body of volume " + (volumeCount + 1) + " : " + volume.getTitle());

                volumeElements[volumeCount] = document.createElementNS(pefNS, "volume");
                volumeElements[volumeCount].setAttributeNS(null, "cols", String.valueOf(columns));
                volumeElements[volumeCount].setAttributeNS(null, "rows", String.valueOf(rows));
                volumeElements[volumeCount].setAttributeNS(null, "rowgap", eightDots?"1":"0");
                volumeElements[volumeCount].setAttributeNS(null, "duplex", duplex?"true":"false");

                if (!(volume instanceof PreliminaryVolume)) {

                    sectionElement = document.createElementNS(pefNS, "section");

                    cont = true;
                    volume.setBraillePagesStart(beginPage);
                    pageCount = 0;
                    while (cont) {

                        pageElement = document.createElementNS(pefNS,"page");

                        pageCount++;
                        lineCount = 1;
                        while (lineCount <= rows) {

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
                    volume.getTableOfContent() ||
                    volume.getTranscribersNotesPageEnabled() ||
                    volume.getSpecialSymbolListEnabled()) {

                    logger.log(Level.INFO, "Processing preliminary pages of volume " + (volumeCount + 1) + " : " + volume.getTitle());

                    // Print page range

                    if (settings.getVolumeInfoEnabled() &&
                        volume.getFrontMatter() &&
                        !(volume instanceof PreliminaryVolume)) {

                        String volumeNode = "dtb:volume";
                        String id = volume.getIdentifier();
                        if (id != null) { volumeNode += "[@id='" + id  + "']"; }

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

                    if (volume.getSpecialSymbolListEnabled()) {

                        List<SpecialSymbol> specialSymbols = new ArrayList();

                        String volumeNode = "dtb:volume";
                        String id = volume.getIdentifier();
                        if (id != null) { volumeNode += "[@id='" + id  + "']"; }

                        for (SpecialSymbol symbol : settings.getSpecialSymbolList().values()) {

                            switch (symbol.getMode()) {
                                case NEVER:
                                    break;
                                case ALWAYS:
                                    specialSymbols.add(symbol);
                                    break;
                                case FIRST_VOLUME:
                                    if (volumeCount == 0) { specialSymbols.add(symbol); }
                                    break;
                                case IF_PRESENT_IN_VOLUME:
                                    if (!(volume instanceof PreliminaryVolume)) {
                                        switch (symbol.getType()) {
                                            case NOTE_REFERENCE_INDICATOR:
                                                if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                        "//" + volumeNode + "//dtb:note[@class='footnote' or @class='endnote']",namespace)) {
                                                    specialSymbols.add(symbol);
                                                }
                                                break;
                                            case TRANSCRIBERS_NOTE_INDICATOR:
                                                if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                        "//" + volumeNode + "//dtb:div[@class='tn']/dtb:note",namespace)) {
                                                    specialSymbols.add(symbol);
                                                }
                                                break;
                                            case ITALIC_INDICATOR:
                                                if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                        "//" + volumeNode + "//dtb:em[not(@class='reset')]",namespace)) {
                                                    specialSymbols.add(symbol);
                                                }
                                                break;
                                            case BOLDFACE_INDICATOR:
                                                if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                        "//" + volumeNode + "//dtb:strong[not(@class='reset')]",namespace)) {
                                                    specialSymbols.add(symbol);
                                                }
                                                break;
                                            case ELLIPSIS:
                                                if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                        "//" + volumeNode + "//dtb:flag[@class='ellipsis']",namespace)) {
                                                    specialSymbols.add(symbol);
                                                }
                                                break;
                                            case DOUBLE_DASH:
                                                if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                        "//" +  volumeNode + "//dtb:flag[@class='double-dash']",namespace)) {
                                                    specialSymbols.add(symbol);
                                                }
                                                break;
                                            default:
                                        }
                                    }
                                    break;
                            }
                        }

                        volume.setSpecialSymbols(specialSymbols);
                    }

                    // Extract preliminary page range

                    preliminaryFile = File.createTempFile(TMP_NAME, ".daisy." + (volumeCount + 1) + ".xml", TMP_DIR);
                    preliminaryFile.deleteOnExit();

                    sectionElement = document.createElementNS(pefNS,"section");

                    odtTransformer.getFrontMatter(preliminaryFile, volume, volumeInfo);
                    liblouisXML.configure(preliminaryFile, brailleFile, true, volume.getTableOfContent()?volume.getFirstBraillePage():1);
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
                            if (volume.getTableOfContent()) {
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

                    odtTransformer.getFrontMatter(preliminaryFile, volume, volumeInfo);
                    if (checker != null) {
                        checker.checkDaisyFile(preliminaryFile);
                    }
                    liblouisXML.configure(preliminaryFile, brailleFile, false, volume.getTableOfContent()?volume.getFirstBraillePage():1);
                    liblouisXML.run();

                    fileInputStream = new FileInputStream(brailleFile);
                    inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
                    bufferedReader = new BufferedReader(inputStreamReader);

                    pageCount = 1;

                    while (pageCount <= volume.getNumberOfPreliminaryPages()) {

                        pageElement = document.createElementNS(pefNS,"page");
                        lineCount = 1;

                        while (lineCount <= rows) {

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

        File[] pefFiles = splitPEF();
        if (pefFiles != null) {
            return pefFiles;
        } else {
            return null;
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

    private String capitalizeFirstLetter(String in) {
        return in.substring(0,1).toUpperCase() + in.substring(1);
    }
    
}
