/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
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
    private final ODT odt;
    private final PEFConfiguration pefSettings;
    private final StatusIndicator statusIndicator;
    private final PostConversionBrailleChecker checker;
    private final Validator validator;

    private final VolumeManager manager;

    public PEF(ODT odt,
               PEFConfiguration pefSettings,
               LiblouisXML liblouisXML)
        throws IOException,
               TransformerException,
               SAXException,
               ConversionException,
               Exception {

        this(odt, pefSettings, liblouisXML, null, null);

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
    public PEF(ODT odt,
               PEFConfiguration pefSettings,
               LiblouisXML liblouisXML,
               StatusIndicator statusIndicator,
               PostConversionBrailleChecker checker)
        throws IOException,
               TransformerException,
               SAXException,
               ConversionException,
               Exception {

        logger.entering("PEF", "<init>");

        this.odt = odt;
        this.pefSettings = pefSettings;
        this.liblouisXML = liblouisXML;
        this.statusIndicator = statusIndicator;
        this.checker = checker;

        pefFile = File.createTempFile(TMP_NAME, ".pef", TMP_DIR);
        pefFile.deleteOnExit();

        manager = new VolumeManager(odt);

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
        return manager.getVolumes();
    }


    /**
     * Converts the flat .odt filt to a .pef file according to the braille settings.
     *
     * This function
     * <ul>
     * <li>uses {@link ODT} to convert the .odt file to multiple DAISY-like xml files,</li>
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
                                    ConversionException,
                                    LiblouisXMLException,
                                    Exception {

        logger.entering("PEF", "makePEF");

        Configuration settings = odt.getConfiguration();

        Element[] volumeElements;
        Element sectionElement;
        File bodyFile = null;
        File brailleFile = null;
        File preliminaryFile = null;

        List<Volume> volumes = manager.getVolumes();

        String volumeInfo = capitalizeFirstLetter(
                ResourceBundle.getBundle(L10N, settings.mainLocale).getString("in")) + " " + volumes.size() + " " +
                ResourceBundle.getBundle(L10N, settings.mainLocale).getString((volumes.size()>1) ? "volumes" : "volume") + "\n@title\n@pages";

        volumeElements = new Element[volumes.size()];

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        DOMImplementation impl = docBuilder.getDOMImplementation();

        Document document = impl.createDocument(pefNS, "pef", null);
        Element root = document.getDocumentElement();
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", pefNS);
        root.setAttributeNS(null,"version","2008-1");

        Element headElement = document.createElementNS(pefNS,"head");
        Element metaElement = document.createElementNS(pefNS,"meta");
        metaElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:dc", "http://purl.org/dc/elements/1.1/");
        Element dcElement = document.createElementNS("http://purl.org/dc/elements/1.1/","dc:identifier");
        dcElement.appendChild(document.createTextNode(Integer.toHexString((int)(Math.random()*1000000)) + " "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((new Date()))));
        metaElement.appendChild(dcElement);
        dcElement = document.createElementNS("http://purl.org/dc/elements/1.1/","dc:format");
        dcElement.appendChild(document.createTextNode("application/x-pef+xml"));
        metaElement.appendChild(dcElement);
        headElement.appendChild(metaElement);

        root.appendChild(headElement);

        int columns = pefSettings.getColumns();
        int rows = pefSettings.getRows();
        boolean duplex = pefSettings.getDuplex();
        int rowgap = pefSettings.getEightDots()?1:0;
        int beginPage = settings.getBeginningBraillePageNumber();

        if (statusIndicator != null) {
            statusIndicator.start();
            statusIndicator.setSteps(volumes.size());
            statusIndicator.setStatus(ResourceBundle.getBundle(L10N, statusIndicator.getPreferredLocale()).getString("statusIndicatorStep"));
        }

        for (int volumeCount=0; volumeCount<volumes.size(); volumeCount++) {

            volumeElements[volumeCount] = document.createElementNS(pefNS, "volume");
            volumeElements[volumeCount].setAttributeNS(null, "cols", String.valueOf(columns));
            volumeElements[volumeCount].setAttributeNS(null, "rows", String.valueOf(rows + (int)Math.ceil(((rows-1)*rowgap)/4d)));
            volumeElements[volumeCount].setAttributeNS(null, "rowgap", String.valueOf(rowgap));
            volumeElements[volumeCount].setAttributeNS(null, "duplex", duplex?"true":"false");

            Volume volume = volumes.get(volumeCount);

            // Body section

            logger.info("Processing volume " + (volumeCount + 1) + " : " + volume.getTitle());

            if (!(volume instanceof PreliminaryVolume)) {

                bodyFile = File.createTempFile(TMP_NAME, ".daisy.body." + (volumeCount + 1) + ".xml", TMP_DIR);
                bodyFile.deleteOnExit();
                brailleFile = File.createTempFile(TMP_NAME, ".txt", TMP_DIR);
                brailleFile.deleteOnExit();

                odt.getBodyMatter(bodyFile, volume);
                liblouisXML.configure(bodyFile, brailleFile, false, beginPage);
                liblouisXML.run();

                // Read pages
                sectionElement = document.createElementNS(pefNS, "section");
                int pageCount = addPagesToSection(document, sectionElement, brailleFile, rows, columns, -1);
                volumeElements[volumeCount].appendChild(sectionElement);

                // Checker
                if (checker != null) { checker.checkDaisyFile(bodyFile); }

                // Braille page range
                volume.setBraillePagesStart(beginPage);
                volume.setNumberOfBraillePages(pageCount);
                beginPage += pageCount;

                // Print page range
                if (volume.getFrontMatter() && settings.getVolumeInfoEnabled()) {
                    extractPrintPageRange(bodyFile, volume, settings);
                }
            }

            // Special symbols list
            if (volume.getSpecialSymbolListEnabled()) {
                extractSpecialSymbols(bodyFile, volume, volumeCount, settings);
            }

            // Preliminary section

            if (volume.getFrontMatter() ||
                volume.getTableOfContent() ||
                volume.getTranscribersNotesPageEnabled() ||
                volume.getSpecialSymbolListEnabled()) {

                preliminaryFile = File.createTempFile(TMP_NAME, ".daisy.front." + (volumeCount + 1) + ".xml", TMP_DIR);
                preliminaryFile.deleteOnExit();
                brailleFile = File.createTempFile(TMP_NAME, ".txt", TMP_DIR);
                brailleFile.deleteOnExit();

                odt.getFrontMatter(preliminaryFile, volume, volumeInfo);
                liblouisXML.configure(preliminaryFile, brailleFile, true, volume.getTableOfContent()?volume.getFirstBraillePage():1);
                liblouisXML.run();

                // Page range
                int pageCount = countPages(brailleFile, volume);
                volume.setNumberOfPreliminaryPages(pageCount);

                // Translate again with updated volume info and without volume separator marks
                brailleFile = File.createTempFile(TMP_NAME, ".txt", TMP_DIR);
                brailleFile.deleteOnExit();
                odt.getFrontMatter(preliminaryFile, volume, volumeInfo);
                liblouisXML.configure(preliminaryFile, brailleFile, false, volume.getTableOfContent()?volume.getFirstBraillePage():1);
                liblouisXML.run();

                // Read pages
                sectionElement = document.createElementNS(pefNS, "section");
                addPagesToSection(document, sectionElement, brailleFile, rows, columns, pageCount);
                volumeElements[volumeCount].insertBefore(sectionElement, volumeElements[volumeCount].getFirstChild());

                // Checker
                if (checker != null) { checker.checkDaisyFile(preliminaryFile); }
            }

            if (statusIndicator != null) {
                statusIndicator.increment();
            }
        }

        if (checker != null) { checker.checkVolumes(volumes); }

        Element bodyElement = document.createElementNS(pefNS, "body");

        for (int volumeCount=0; volumeCount<volumes.size(); volumeCount++) {
            bodyElement.appendChild(volumeElements[volumeCount]);
        }

        root.appendChild(bodyElement);

        document.insertBefore((ProcessingInstruction)document.createProcessingInstruction(
                "xml-stylesheet","type='text/css' href='pef.css'"), document.getFirstChild());

        OdtUtils.saveDOM(document, pefFile);

        logger.exiting("PEF", "makePEF");

        if (!validatePEF(pefFile)) {
            return false;
        }

        return true;
    }

    /**
     * maxPages: -1 = infinity
     */
    private int addPagesToSection(Document document,
                                  Element sectionElement,
                                  File brailleFile,
                                  int maxRows,
                                  int maxCols,
                                  int maxPages)
                           throws IOException,
                                  Exception {

        int pageCount = 0;

        FileInputStream fileInputStream = new FileInputStream(brailleFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        Element pageElement;
        Element rowElement;
        Node textNode;
        String line;

        boolean nextPage = bufferedReader.ready() && (maxPages > pageCount || maxPages == -1);

        try {
            while (nextPage) {
                pageElement = document.createElementNS(pefNS, "page");
                for (int i=0; i<maxRows; i++) {
                    line = bufferedReader.readLine();
                    if (line == null) { throw new Exception("number of rows < " + maxRows); }
                    line = line.replaceAll("\u2800","\u0020")
                               .replaceAll("\u00A0","\u0020")
                               .replaceAll("\uE00F","\u002D")
                               .replaceAll("\uE000","\u0020");
                    if (line.length() > maxCols) { throw new Exception("line length > " + maxCols); }
                    rowElement = document.createElementNS(pefNS, "row");
                    textNode = document.createTextNode(liblouisTable.toBraille(line));
                    rowElement.appendChild(textNode);
                    pageElement.appendChild(rowElement);
                    if (IS_WINDOWS) { bufferedReader.readLine(); }
                }

                sectionElement.appendChild(pageElement);
                pageCount++;
                if (bufferedReader.read() != '\f') { throw new Exception("unexpected character, should be form feed"); }
                nextPage = nextPage = bufferedReader.ready() && (maxPages > pageCount || maxPages == -1);
            }

        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
                inputStreamReader.close();
                fileInputStream.close();
            }
        }

        return pageCount;
    }

    private int countPages(File brailleFile,
                           Volume volume)
                    throws IOException {

        int pageCount = 0;

        FileInputStream fileInputStream = new FileInputStream(brailleFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
        String brfInput = IOUtils.toString(inputStreamReader);

        try {

            Matcher matcher = Pattern.compile("(\f|\uE000)").matcher(brfInput);
            pageCount = 1;

            while (matcher.find()) {
                char ch = brfInput.charAt(matcher.start());
                if (ch=='\f') {
                    pageCount ++;
                } else {
                    if (volume.getTableOfContent()) { pageCount --; }
                    break;
                }
            }

        } finally {
            if (inputStreamReader != null) {
                inputStreamReader.close();
                fileInputStream.close();
            }
        }

        return pageCount;
    }

    private void extractPrintPageRange(File bodyFile,
                                       Volume volume,
                                       Configuration settings)
                                throws IOException {

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

    /**
     * Determine which symbols to display in list of special symbols
     */
    private void extractSpecialSymbols(File bodyFile,
                                       Volume volume,
                                       int volumeCount,
                                       Configuration settings)
                                throws IOException {

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

    /**
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

        File[] pefs = output.listFiles();
        Arrays.sort(pefs, new Comparator<File>(){
            public int compare(File f1, File f2) {
                String n1 = f1.getName();
                String n2 = f2.getName();
                Integer i1 = Integer.parseInt(n1.substring(n1.lastIndexOf('-')+1, n1.length()-4));
                Integer i2 = Integer.parseInt(n2.substring(n2.lastIndexOf('-')+1, n2.length()-4));
                return i1.compareTo(i2);
            }
        });
        return pefs;
    }

    private String capitalizeFirstLetter(String in) {
        return in.substring(0,1).toUpperCase() + in.substring(1);
    }
    
}
