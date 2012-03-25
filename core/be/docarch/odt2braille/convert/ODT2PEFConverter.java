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

package be.docarch.odt2braille.convert;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.NamespaceContext;
import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.ProgressMonitor;
import be.docarch.odt2braille.XML;
import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;
import be.docarch.odt2braille.utils.FileCreator;
import be.docarch.odt2braille.utils.OdtUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.IOUtils;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.pef.PEFValidator;
import org.daisy.braille.pef.PEFFileSplitter;
import org.daisy.validator.ValidatorFactory;
import org.daisy.validator.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ProcessingInstruction;

public class ODT2PEFConverter implements Converter<ODT,PEF>, Parameterized {

    private static final Logger logger = Constants.getLogger();
    private static final ProgressMonitor statusIndicator = Constants.getStatusIndicator();
    
    private static final String L10N = Constants.L10N_PATH;
    private static final String PEF_NS = "http://www.daisy.org/ns/2008/pef";
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    
    private static final BrailleConverter liblouisTable = new LiblouisTable().newBrailleConverter();
    private static final Validator pefValidator;
    
    private static final String L10N_preliminary_section = "Preliminary Section";
    private static final String L10N_main_section = "Main Section";
    
    private static final Set<String> supportedParameters = new HashSet<String>();
    
    static {
    
        // Validator
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PEF.class.getClassLoader()); {

            ValidatorFactory factory = ValidatorFactory.newInstance();
            pefValidator = factory.newValidator(PEFValidator.class.getCanonicalName());
            pefValidator.setFeature(PEFValidator.FEATURE_MODE, PEFValidator.Mode.LIGHT_MODE);

        } Thread.currentThread().setContextClassLoader(cl);
    
        supportedParameters.add("columns");
        supportedParameters.add("rows");
        supportedParameters.add("duplex");
        supportedParameters.add("rowgap");
        supportedParameters.add("beginningBraillePageNumber");
        supportedParameters.add("preliminaryPageNumberFormat");
    }
    
    private final LiblouisXML liblouisXML = new LiblouisXML();
    private final ODT2XMLConverter odt2xmlConverter = new ODT2XMLConverter();
    
    private final Collection<File> tempFiles = new ArrayList<File>();
    private XML tempXML = null;
    
    private final Map<String,Object> parameters = new HashMap<String,Object>();
    
    public void setParameter(String key, Object value) {
        int set = 0;
        try {
            liblouisXML.setParameter(key, value);
            set++;
        } catch (Exception e) {}
        try {
            odt2xmlConverter.setParameter(key, value);
            set++;
        } catch (Exception e) {}
        if (supportedParameters.contains(key)) {
            parameters.put(key, value);
            set++;
        }
        if (set == 0) { throw new IllegalArgumentException("parameter '" + key + "' not supported"); }
    }
    
    private Object getParameter(String key) {
        if (!parameters.containsKey(key)) { throw new NullPointerException("parameter '" + key + "' not set"); }
        return parameters.get(key);
    }
    
    public ODT2PEFConverter() throws Exception {
        parameters.put("columns", 40);
        parameters.put("rows", 25);
        parameters.put("duplex", false);
        parameters.put("rowgap", 0);
        parameters.put("beginningBraillePageNumber", 1);
        parameters.put("preliminaryPageNumberFormat", PageNumberFormat.P);
    }
    
    public PEF convert(ODT odt) throws ConversionException {
        
        try {

            odt2xmlConverter.setParameter("mainLocale", odt.getLocale());
            
            tempXML = odt2xmlConverter.convert(odt);

            PEFImpl pef = new PEFImpl();

            List<XML.Volume> volumes = tempXML.getVolumes();

            int beginPage = (Integer)getParameter("beginningBraillePageNumber");

            statusIndicator.start();
            statusIndicator.setSteps(volumes.size());
            statusIndicator.setStatus(ResourceBundle.getBundle(L10N, statusIndicator.getPreferredLocale()).getString("statusIndicatorStep"));

            for (int volumeCount=0; volumeCount<volumes.size(); volumeCount++) {

                XML.Volume volume = volumes.get(volumeCount);
                PEFImpl.VolumeImpl pefVolume = pef.new VolumeImpl(volume.getTitle());
                pef.addVolume(pefVolume);

                // Body section

                logger.log(Level.INFO, "Processing volume {0} : {1}", new Object[]{volumeCount + 1, volume.getTitle()});
                
                try {
                    
                    liblouisXML.setParameter("preliminaryPageRangeMode", false);
                    liblouisXML.setParameter("beginPage", beginPage);
                    
                    File bodyFile = volume.getBodySection();
                    File brailleFile = new File(bodyFile.getAbsoluteFile() + ".brl");
                    tempFiles.add(bodyFile);
                    tempFiles.add(brailleFile);
                    
                    liblouisXML.convert(bodyFile, brailleFile);
                    PEFImpl.SectionImpl pefSection = pef.new SectionImpl(L10N_main_section, beginPage, PageNumberFormat.NORMAL);
                    pefVolume.addSection(pefSection);
                    
                    // Read pages
                    pefSection.addPages(brailleFile, -1);
                    
                    // Braille page range
                    volume.setBraillePagesStart(beginPage);
                    volume.setNumberOfBraillePages(pefSection.getNumberOfPages());
                    beginPage += pefSection.getNumberOfPages();
                    
                } catch (UnsupportedOperationException e) {
                }

                // Preliminary section

                try {
                    
                    liblouisXML.setParameter("preliminaryPageRangeMode", true);
                    liblouisXML.setParameter("beginPage", volume.getTableOfContentEnabled() ? volume.getFirstBraillePage():1);

                    File preliminaryFile = volume.getPreliminarySection();
                    File brailleFile = new File(preliminaryFile.getAbsoluteFile() + ".brl");
                    tempFiles.add(preliminaryFile);
                    tempFiles.add(brailleFile);
                    
                    liblouisXML.convert(preliminaryFile, brailleFile);

                    // Page range
                    int pageCount = countPages(brailleFile, volume);
                    volume.setNumberOfPreliminaryPages(pageCount);

                    // Translate again with updated volume info
                    preliminaryFile = volume.getPreliminarySection();
                    liblouisXML.setParameter("preliminaryPageRangeMode", false);
                    liblouisXML.convert(preliminaryFile, brailleFile);

                    // Read pages
                    PEFImpl.SectionImpl pefSection
                            = pef.new SectionImpl(L10N_preliminary_section, 1, 
                                (PageNumberFormat)getParameter("preliminaryPageNumberFormat"));
                    pefSection.addPages(brailleFile, pageCount);
                    pefVolume.addSectionBefore(pefSection);
                    
                } catch (UnsupportedOperationException e) {
                }

                statusIndicator.increment();
            }

            try {
                pef.save();
            } catch (ConversionException e) {
                Constants.getStatusIndicator().finish(false);
                throw e;
            }

            statusIndicator.finish(true);

            
            
            return pef;
            
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }
        
    public void cleanUp() {
        liblouisXML.cleanUp();
        odt2xmlConverter.cleanUp();
        if (tempXML != null) {
            tempXML.close();
        }
        for (File f : tempFiles) {
            f.delete();
        }
        tempFiles.clear();
    }
    
    private class PEFImpl implements PEF {

        private final File pefFile;
        private final Document document;
        private final Element bodyElement;
        private final List<VolumeImpl> volumes = new ArrayList<VolumeImpl>();
        private final int columns = (Integer)getParameter("columns");
        private final int rows = (Integer)getParameter("rows");
        private final int rowgap = (Integer)getParameter("rowgap");
        private final boolean duplex = (Boolean)getParameter("duplex");

        private PEFImpl() throws Exception {

            pefFile = FileCreator.createTempFile(".pef");

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setValidating(false);
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            DOMImplementation impl = docBuilder.getDOMImplementation();

            document = impl.createDocument(PEF_NS, "pef", null);
            Element root = document.getDocumentElement();
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", PEF_NS);
            root.setAttributeNS(null,"version","2008-1");

            Element headElement = document.createElementNS(PEF_NS,"head");
            Element metaElement = document.createElementNS(PEF_NS,"meta");
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
            bodyElement = document.createElementNS(PEF_NS, "body");
            root.appendChild(bodyElement);
            document.insertBefore((ProcessingInstruction)document.createProcessingInstruction(
                    "xml-stylesheet","type='text/css' href='pef.css'"), document.getFirstChild());
        }
        
        public void close() {}

        public List<PEF.Volume> getVolumes() {
            return new ArrayList<PEF.Volume>(volumes);
        }
        
        public File getSinglePEF() {
            return pefFile;
        }

        public File[] getPEFs() {
            File[] pefFiles = splitPEF();
            if (pefFiles != null) {
                return pefFiles;
            } else {
                return null;
            }
        }
        
        private void addVolume(VolumeImpl volume) {
            volumes.add(volume);
            bodyElement.appendChild(volume.volumeElement);
        }

        private void save() throws ConversionException, Exception {
            
            logger.log(Level.INFO, "Saving PEF to file: {0}", pefFile.getName());
            
            OdtUtils.saveDOM(document, pefFile);
            if (!validatePEF(pefFile)) {
                throw new ConversionException("PEF file invalid");
            }
        }

        /**
         * Split a single PEF file into several files, one file per volume.
         */
        private File[] splitPEF() {

            logger.info("Splitting PEF info multiple volume files");

            File input = pefFile;
            File output = new File(input.getAbsolutePath() + "-split");
            output.mkdir();

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); {

                PEFFileSplitter splitter = new PEFFileSplitter();
                splitter.split(input, output);

            } Thread.currentThread().setContextClassLoader(cl);

            File[] pefs = output.listFiles();
            Arrays.sort(pefs, new Comparator<File>() {
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
        
        private class VolumeImpl implements PEF.Volume {

            private final String name;
            private final Element volumeElement;
            private final List<SectionImpl> sections = new ArrayList<SectionImpl>();

            private VolumeImpl(String name) {
                this.name = name;
                volumeElement = document.createElementNS(PEF_NS, "volume");
                volumeElement.setAttributeNS(null, "cols", String.valueOf(columns));
                volumeElement.setAttributeNS(null, "rows", String.valueOf(rows + (int)Math.ceil(((rows-1)*rowgap)/4d)));
                volumeElement.setAttributeNS(null, "rowgap", String.valueOf(rowgap));
                volumeElement.setAttributeNS(null, "duplex", String.valueOf(duplex));
            }

            @Override
            public String getName() {
                return name;
            }
            
            @Override
            public List<PEF.Section> getSections() {
                return new ArrayList<PEF.Section>(sections);
            }

            private Element getDOMElement() {
                return volumeElement;
            }

            private void addSection(SectionImpl section) {
                volumeElement.appendChild(section.sectionElement);
                sections.add(section);
            }

            private void addSectionBefore(SectionImpl section) {
                volumeElement.insertBefore(section.sectionElement, volumeElement.getFirstChild());
                sections.add(section);
            }
        }

        private class SectionImpl implements PEF.Section {

            private final Element sectionElement;
            private final String name;
            private int numberOfPages = 0;
            private int firstPageNumber;
            private PageNumberFormat pageNumberFormat;

            private SectionImpl(String name,
                                int firstPageNumber,
                                PageNumberFormat pageNumberFormat) {
                this.name = name;
                this.firstPageNumber = firstPageNumber;
                this.pageNumberFormat = pageNumberFormat;
                sectionElement = document.createElementNS(PEF_NS, "section");
            }

            @Override
            public String getName() {
                return name;
            }
            
            public Element getDOMElement() {
                return sectionElement;
            }

            /**
             * maxPages: -1 = infinity
             */
            private int addPages(File brailleFile, int maxPages) throws Exception {

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
                        pageElement = document.createElementNS(PEF_NS, "page");
                        for (int i=0; i<rows; i++) {
                            line = bufferedReader.readLine();
                            if (line == null) { throw new Exception("number of rows < " + rows); }
                            line = line.replaceAll("\u2800","\u0020")
                                       .replaceAll("\u00A0","\u0020")
                                       .replaceAll("\uE00F","\u002D")
                                       .replaceAll("\uE000","\u0020");
                            if (line.length() > columns) { throw new Exception("line length > " + columns); }
                            rowElement = document.createElementNS(PEF_NS, "row");
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

                return numberOfPages += pageCount;
            }

            private int getNumberOfPages() {
                return numberOfPages;
            }

            public int getFirstPageNumber() {
                return firstPageNumber;
            }

            public PageNumberFormat getPageNumberFormat() {
                return pageNumberFormat;
            }
        }
    }
    
    private static int countPages(File brailleFile,
                                  XML.Volume volume)
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
                    if (volume.getTableOfContentEnabled()) { pageCount --; }
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
    
    private static boolean validatePEF(File pefFile) throws Exception {

        logger.info("Validating PEF");

        if (pefValidator.validate(pefFile.toURI().toURL())) {

            logger.info("PEF is valid");
            return true;

        } else {

            String message = "pef invalid!\nMessages returned by the validator:\n";
            InputStreamReader report = new InputStreamReader(pefValidator.getReportStream());
            int c;
            while ((c = report.read()) != -1) {
                message += (char)c;
            }
            logger.log(Level.SEVERE, message);

            return false;
        }
    }
}
