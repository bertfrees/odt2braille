package be.docarch.odt2braille.convert;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.NamespaceContext;
import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.ProgressMonitor;
import be.docarch.odt2braille.XML;
import be.docarch.odt2braille.setup.SpecialSymbol;
import be.docarch.odt2braille.setup.TranslationTable;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;
import be.docarch.odt2braille.utils.FileCreator;
import be.docarch.odt2braille.utils.NumberFormatter;
import be.docarch.odt2braille.utils.OdtUtils;
import be.docarch.odt2braille.utils.XPathUtils;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Map;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.apache.xpath.XPathAPI;

/**
 *
 * @author Bert Frees
 */
public class ODT2XMLConverter implements Converter<ODT,XML>, Parameterized {
    
    private static final Logger logger = Constants.getLogger();
    private static final ProgressMonitor statusIndicator = Constants.getStatusIndicator();
    
    private static final String L10N = Constants.L10N_PATH;
    private static final NamespaceContext NAMESPACE = new NamespaceContext();
    
    private static final DocumentBuilder docBuilder;
    
    private static final Set<String> publicParameters = new HashSet<String>();
    
    static {
        
        try {
            DocumentBuilderFactory docFactory;
            docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setValidating(false);
            docBuilder = docFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        publicParameters.add("mainLocale");
        publicParameters.add("braillePageNumbers");
        publicParameters.add("printPageNumbers");
        publicParameters.add("preliminaryPageNumberFormat");
        publicParameters.add("creator");
        publicParameters.add("noteSectionTitle");
        publicParameters.add("volumeInfoEnabled");
        publicParameters.add("transcriptionInfoEnabled");
        publicParameters.add("tableOfContentTitle");
        publicParameters.add("tnPageTitle");
        publicParameters.add("specialSymbolListTitle");
        publicParameters.add("specialSymbolList");
        publicParameters.add("mergeUnnumberedPages");
        publicParameters.add("preliminaryVolumeEnabled");
        publicParameters.add("bodyMatterMode");
        publicParameters.add("rearMatterMode");
        publicParameters.add("preliminaryVolume");
        publicParameters.add("bodyMatterVolume");
        publicParameters.add("rearMatterVolume");
        publicParameters.add("manualVolumes");
        publicParameters.add("rearMatterSection");
        
    }
    
    private Element stylesRoot;
    private ZipFile zip;
    private Map<String,ODT.Style> paragraphStyles;
    private URI controllerURL;
    private int pageCount;
    
    private final Collection<File> tempFiles = new ArrayList<File>();
    
    private final PaginationProcessor paginationProcessor = new PaginationProcessor();
    private final Converter<Document,Document> headingNumberingProcessor = new HeadingNumberingProcessor();
    private final ListNumberingProcessor listNumberingProcessor = new ListNumberingProcessor();
    private final Converter<Document,Document> correctionProcessor = new CorrectionProcessor();
    private final XSLTransformer ensureMetadataReferences;
    private final XSLTransformer makeControlFlow;
    private final MainTransformer mainTransformer = new MainTransformer();
    private final LanguagesAndTypefaceTransformer languagesAndTypefaceTransformer = new LanguagesAndTypefaceTransformer();
    
    private final Locale oooLocale = statusIndicator.getPreferredLocale();
    
    private final Map<String,Object> parameters = new HashMap<String,Object>();
    
    public void setParameter(String key, Object value) {
        if ("newBraillePages".equals(key) ||
            "keepHardPageBreakStyles".equals(key)) {
            paginationProcessor.setParameter(key, value);
            return;
        }
        if ("listPrefixes".equals(key)) {
            listNumberingProcessor.setParameter(key, value);
            return;
        }
        if (mainTransformer.publicParameters.contains(key)) {
            mainTransformer.setParameter(key, value);
            return;
        }
        if (languagesAndTypefaceTransformer.publicParameters.contains(key)) {
            languagesAndTypefaceTransformer.setParameter(key, value);
            return;
        }
        if ("manualVolumeSections".equals(key) ||
            "styles-url".equals(key)) {
            throw new IllegalArgumentException("parameter '" + key + "' not supported");
        }
        int set = 0;
        try {
            makeControlFlow.setParameter(key, value);
            set++;
        } catch (Exception e) {
        }
        if (publicParameters.contains(key)) {
            parameters.put(key, value);
            set++;
        }
        if (set == 0) { throw new IllegalArgumentException("parameter '" + key + "' not supported"); }
    }
    
    private Object getParameter(String key) {
        if (!parameters.containsKey(key)) { throw new NullPointerException("parameter '" + key + "' not set"); }
        return parameters.get(key);
    }
    
    public ODT2XMLConverter() throws Exception {
        
        ensureMetadataReferences = new EnsureMetadataReferences();
        makeControlFlow = new ControlFlowMaker();
        
        ensureMetadataReferences.setLogger(logger);
        makeControlFlow.setLogger(logger);
        mainTransformer.setLogger(logger);
        languagesAndTypefaceTransformer.setLogger(logger);
    }

    public XML convert(ODT odt) throws ConversionException {
        
        try {

            stylesRoot = docBuilder.parse(odt.getStylesFile().getAbsolutePath()).getDocumentElement();
            zip = odt.getZipFile();
            URI stylesURL = odt.getStylesFile().toURI();
            pageCount = odt.getPageCount();
            paragraphStyles = new HashMap<String,ODT.Style>();
            for (ODT.Style style : odt.getUsedParagraphStyles()) {
                paragraphStyles.put(style.getName(), style);
            }

            XMLImpl xml = new XMLImpl();
            
            List<XMLImpl.VolumeImpl> volumes = new ArrayList<XMLImpl.VolumeImpl>();

            XMLImpl.SplittableVolumeImpl singleBodyVolume = null;
            XMLImpl.VolumeImpl singleRearVolume = null;
            Map<String,XMLImpl.VolumeImpl> sectionToVolume = new HashMap<String,XMLImpl.VolumeImpl>();

            if ((Boolean)getParameter("preliminaryVolumeEnabled")) {
                volumes.add(xml.new PreliminaryVolumeImpl((Configuration.Volume)getParameter("preliminaryVolume")));
            }
            String bodyMatterMode = (String)getParameter("bodyMatterMode");
            if ("SINGLE".equals(bodyMatterMode) || "AUTOMATIC".equals(bodyMatterMode)) {
                singleBodyVolume = xml.new SplittableVolumeImpl((Configuration.SplittableVolume)getParameter("bodyMatterVolume"));
                volumes.add(singleBodyVolume);
            }
            for (Configuration.SectionVolume v : (List<Configuration.SectionVolume>)getParameter("manualVolumes")) {
                XMLImpl.VolumeImpl volume = xml.new VolumeImpl(v);
                volumes.add(volume);
                sectionToVolume.put(v.getSection(), volume);
            }
            if (!"".equals(getParameter("rearMatterSection")) && "SINGLE".equals(getParameter("rearMatterMode"))) {
                singleRearVolume = xml.new VolumeImpl((Configuration.Volume)getParameter("rearMatterVolume"));
                volumes.add(singleRearVolume);
            }
            
            List<String> manualVolumeSections = new ArrayList<String>();
            List<String> manualVolumeIDs = new ArrayList<String>();
            for (String section : sectionToVolume.keySet()) {
                manualVolumeSections.add(section);
                manualVolumeIDs.add(sectionToVolume.get(section).identifier);
            }
            
            ensureMetadataReferences.setParameter("styles-url",  stylesURL);
            makeControlFlow.setParameter("styles-url",           stylesURL);
            mainTransformer.setParameter("styles-url",           stylesURL);
            mainTransformer.setParameter("singleBodyVolumeId",   singleBodyVolume==null?"":singleBodyVolume.identifier);
            mainTransformer.setParameter("singleRearVolumeId",   singleRearVolume==null?"":singleRearVolume.identifier);
            mainTransformer.setParameter("manualVolumeIds",      manualVolumeIDs.toArray(new String[manualVolumeIDs.size()]));
            mainTransformer.setParameter("manualVolumeSections", manualVolumeSections.toArray(new String[manualVolumeSections.size()]));
            makeControlFlow.setParameter("manualVolumeSections", manualVolumeSections.toArray(new String[manualVolumeSections.size()]));
            
            Document enhancedOdtContentDocument = docBuilder.parse(odt.getContentFile().getAbsolutePath());
            paginationProcessor.convert(enhancedOdtContentDocument);
            headingNumberingProcessor.convert(enhancedOdtContentDocument);
            listNumberingProcessor.convert(enhancedOdtContentDocument);
            correctionProcessor.convert(enhancedOdtContentDocument);
            
            File enhancedOdtContentFile = FileCreator.createTempFile(".odt.content.enhanced.xml");
            File controllerFile = FileCreator.createTempFile(".controller.rdf.xml");
            File tempFile = FileCreator.createTempFile(".daisy.xml.temp");
            File xmlFile = FileCreator.createTempFile(".daisy.xml");
            
            tempFiles.add(enhancedOdtContentFile);
            tempFiles.add(controllerFile);
            tempFiles.add(tempFile);
            
            OdtUtils.saveDOM(enhancedOdtContentDocument, enhancedOdtContentFile);
            ensureMetadataReferences.convert(enhancedOdtContentFile);
            makeControlFlow.convert(enhancedOdtContentFile, controllerFile);
            controllerURL = controllerFile.toURI();
            mainTransformer.convert(enhancedOdtContentFile, tempFile);
            languagesAndTypefaceTransformer.convert(tempFile, xmlFile);
            
            xml.xmlFile = xmlFile;

            for (XML.Volume v : volumes) { xml.addVolume(v); }

            if("AUTOMATIC".equals((String)getParameter("bodyMatterMode"))) {
                singleBodyVolume.split();
            }

            int i = 1;
            for (XML.Volume v : volumes) {
                if (v.getTitle().contains("@i")) {
                    XMLImpl.VolumeImpl vImpl = (XMLImpl.VolumeImpl)v;
                    vImpl.title = vImpl.title.replaceFirst("@i", String.valueOf(i)); // Wat gaat er hier mis?
                    i++;
                }
            }
            for (XML.Volume v : volumes) {
                XMLImpl.VolumeImpl vImpl = (XMLImpl.VolumeImpl)v;
                if (vImpl.frontMatterMode == FrontMatterMode.BASIC) {
                    vImpl.frontMatterMode = FrontMatterMode.EXTENDED;
                    break;
                }
            }
            for (XML.Volume v : volumes) {
                XMLImpl.VolumeImpl vImpl = (XMLImpl.VolumeImpl)v;
                if (vImpl.tableOfContentMode == TableOfContentMode.BASIC) {
                    vImpl.tableOfContentMode = TableOfContentMode.EXTENDED;
                    break;
                }
            }

            return xml;
            
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }
    
    public void cleanUp() {
        paginationProcessor.cleanUp();
        headingNumberingProcessor.cleanUp();
        listNumberingProcessor.cleanUp();
        correctionProcessor.cleanUp();
        ensureMetadataReferences.cleanUp();
        makeControlFlow.cleanUp();
        mainTransformer.cleanUp();
        languagesAndTypefaceTransformer.cleanUp();
        for (File f : tempFiles) {
            f.delete();
        }
        tempFiles.clear();
    }
    
    private class PaginationProcessor implements Converter<Document,Document>, Parameterized {
        
        private Element contentRoot;
        private Boolean[] newBraillePages;
        private Collection<String> keepHardPageBreakStyles;
        
        public void setParameter(String key, Object value) {
            if ("newBraillePages".equals(key)) {
                newBraillePages = (Boolean[])value;
            } else if ("keepHardPageBreakStyles".equals(key)) {
                keepHardPageBreakStyles = (Collection<String>)value;
            } else {
                throw new IllegalArgumentException("parameter '" + key + "' not supported");
            }
        }
        
        public Document convert(Document input) throws ConversionException {
            
            logger.info("PaginationProcessor starting");

            try {
            
                contentRoot = input.getDocumentElement();
                Node firstNode = XPathAPI.selectSingleNode(contentRoot, "//body/text/sequence-decls/following-sibling::*[1]");
                if (firstNode != null) {
                    statusIndicator.start();
                    try {
                        statusIndicator.setSteps(pageCount);
                    } catch (NumberFormatException e) {
                        statusIndicator.setSteps(0);
                    }
                    statusIndicator.setStatus(ResourceBundle.getBundle(L10N, oooLocale).getString("statusIndicatorStep1"));
                    insertPagination(firstNode, 0, "Standard", true);
                    statusIndicator.finish(true);
                    statusIndicator.close();
                }

                return input;
                
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
        
        /**
         * Insert a special <code>pagenum</code> tag at the beginning of each page.
         * This method is called recursively so that each document node is eventually processed once.
         *
         * @param   root             The root element of the .odt document.
         * @param   node             The next node to be processed. On the first call, this parameter should be set to the first element of the document body.
         *                           This first node plus all following siblings and their descendants will then be processed eventually.
         * @param   pagenum          The number of the current page (should be set to zero on the first call).
         * @param   masterPageName   The master-page name of the current page (should be set to "Standard" on the first call).
         * @param   isFirst          Should be set to <code>true</code> on the first call.
         *
         * @return  The updated [pagenum, masterPageName].
         *
         * Most of the code was copied from <code>com.versusoft.packages.jodl.OdtUtils</code> (Vincent Spiewak),
         * but several improvements were made.
         *
         */
        private Object[] insertPagination(Node node,
                                          int pagenum,
                                          String masterPageName,
                                          boolean isFirst)
                                   throws TransformerException {

            boolean inclPageNum = false;
            int offset = 0;
            String enumType = "";
            String styleName = null;
            String nodeName = null;
            String xpath = null;
            String temp = null;
            String value = null;
            Object[] ret = null;
            int softPageBreaksBefore = 0;
            int softPageBreaksAfter = 0;
            int hardPageBreaksBefore = 0;
            int hardPageBreaksAfter = 0;
            boolean newBraillePage = false;
            boolean followPrint = false;
            boolean thisIsFirst = isFirst;

            nodeName = node.getNodeName();

            // Decide whether or not to append pagenumber

            if (nodeName.equals("text:soft-page-break")) {

                softPageBreaksBefore = 1;

            } else if (nodeName.equals("table:table-row")) {

                NodeList softPageBreakDescendants = ((Element)node).getElementsByTagName("text:soft-page-break");
                if (softPageBreakDescendants.getLength() > 0) {
                    softPageBreaksAfter = 1;
                }

            } else if (nodeName.equals("text:p") ){

                styleName = node.getAttributes().getNamedItem("text:style-name").getNodeValue();
                ODT.Style style = paragraphStyles.get(styleName);
                if (style != null) {
                    followPrint = keepHardPageBreakStyles.contains(style.getNonAutomaticStyle().getName());
                }

            } else if (nodeName.equals("text:h")) {

                styleName = node.getAttributes().getNamedItem("text:style-name").getNodeValue();
                int level = Integer.parseInt(node.getAttributes().getNamedItem("text:outline-level").getNodeValue());
                newBraillePage = newBraillePages[level-1];
                NodeList softPageBreakDescendants = ((Element)node).getElementsByTagName("text:soft-page-break");
                if (softPageBreakDescendants.getLength() > 0) {
                    softPageBreaksBefore = 1;
                }

            } else if (nodeName.equals("text:list")) {

                if (node.getAttributes().getNamedItem("text:style-name") != null) {

                    xpath = "./*//@style-name";
                    if (XPathAPI.eval(node, xpath).bool()) {
                        styleName = XPathAPI.eval(node, xpath).str();
                    }
                }
            } else if (nodeName.equals("table:table")) {

                styleName = node.getAttributes().getNamedItem("table:style-name").getNodeValue();

            } else if (nodeName.equals("text:table-of-content")   ||
                       nodeName.equals("text:alphabetical-index") ||
                       nodeName.equals("text:illustration-index") ||
                       nodeName.equals("text:table-index")        ||
                       nodeName.equals("text:user-index")         ||
                       nodeName.equals("text:object-index")       ||
                       nodeName.equals("text:bibliography")       ){

                Node indexBodyNode = ((Element)node).getElementsByTagName("text:index-body").item(0);
                NodeList indexTitleNodes = ((Element)indexBodyNode).getElementsByTagName("text:index-title");
                if (indexTitleNodes.getLength() > 0) {
                    styleName = ((Element) indexTitleNodes.item(0)).getElementsByTagName("text:p").item(0).getAttributes().getNamedItem("text:style-name").getNodeValue();
                } else {
                    styleName = ((Element) indexBodyNode).getElementsByTagName("text:p").item(0).getAttributes().getNamedItem("text:style-name").getNodeValue();
                }

                NodeList softPageBreakDescendants = ((Element)node).getElementsByTagName("text:soft-page-break");
                if (softPageBreakDescendants.getLength() > 0) {
                    softPageBreaksAfter = softPageBreakDescendants.getLength();
                }
            }

            if (styleName != null) {

                xpath = "//automatic-styles/style[@name='" + styleName + "']/paragraph-properties"; // TODO: hoeft niet in automatic-styles te zitten !

                if (XPathAPI.eval(contentRoot, xpath + "[@page-number='auto']").bool()) {
                    if (XPathAPI.eval(contentRoot, "//automatic-styles/style[@name='" + styleName + "']/@master-page-name").str().length() > 0) {
                        hardPageBreaksBefore ++;
                    }
                } else if (XPathAPI.eval(contentRoot, xpath + "[@page-number>0]").bool()) {
                    hardPageBreaksBefore ++;
                    pagenum = Integer.parseInt(XPathAPI.eval(contentRoot, xpath + "/@page-number").str()) - 1;
                } else if (XPathAPI.eval(contentRoot, xpath + "[@break-before='page']").bool()) {
                    hardPageBreaksBefore ++;
                } else if (XPathAPI.eval(contentRoot, xpath + "[@break-after='page']").bool()) {
                    hardPageBreaksAfter ++;
                }
            }

            if (isFirst && !nodeName.equals("text:section")) {
                if (hardPageBreaksBefore > 0) { hardPageBreaksBefore --; }
                if (softPageBreaksBefore + hardPageBreaksBefore == 0) { softPageBreaksBefore = 1; }
            }

            Node insertAfterNode = node;

            while (newBraillePage ||
                   softPageBreaksBefore + hardPageBreaksBefore +
                   softPageBreaksAfter +  hardPageBreaksAfter > 0) {

                Element pageNode = contentRoot.getOwnerDocument().createElement("pagebreak");

                if (softPageBreaksBefore + hardPageBreaksBefore +
                    softPageBreaksAfter +  hardPageBreaksAfter > 0) {

                    // Update masterPageName

                    if (!thisIsFirst) {
                        temp = XPathAPI.eval(stylesRoot, "//master-styles/master-page[@name='" + masterPageName + "']/@next-style-name").str();
                        if (!temp.equals("")) {
                            masterPageName = temp;
                        }
                    }
                    if (softPageBreaksBefore + hardPageBreaksBefore > 0 && styleName != null) {
                        temp = XPathAPI.eval(contentRoot, "//automatic-styles/style[@name='" + styleName + "']/@master-page-name").str();
                        if (!temp.equals("")) {
                            masterPageName = temp;
                        }
                    }

                    // Update inclPageNum, enumType and offset

                    xpath = "(count(//master-styles/master-page[@name='" + masterPageName + "']/header/p/page-number)" +
                            "+count(//master-styles/master-page[@name='" + masterPageName + "']/footer/p/page-number))>0";
                    inclPageNum = XPathAPI.eval(stylesRoot, xpath).bool();

                    xpath = "//master-styles/master-page[@name='" + masterPageName + "']/@page-layout-name";
                    String pageLayoutName = XPathAPI.eval(stylesRoot, xpath).str();

                    xpath = "//automatic-styles/page-layout[@name='" + pageLayoutName + "']/page-layout-properties/@num-format";
                    enumType = XPathAPI.eval(stylesRoot, xpath).str();

                    if (inclPageNum) {

                        xpath = "//master-styles/master-page[@name='" + masterPageName + "']//page-number[1]/@num-format";
                        temp = XPathAPI.eval(stylesRoot, xpath).str();
                        if(temp.length()>0){
                            enumType = temp;
                        }

                        xpath = "//master-styles/master-page[@name='" + masterPageName + "']//page-number[1]/@page-adjust";
                        temp = XPathAPI.eval(stylesRoot, xpath).str();
                        if (!temp.equals("")) {
                            offset = Integer.parseInt(temp);
                        }
                    }

                    // Append pagenumber

                    pagenum ++;
                    value = NumberFormatter.format(pagenum + offset, enumType);

                    if (inclPageNum) {
                        pageNode.setAttribute("pagenum", value);
                    }
                }

                if (softPageBreaksBefore > 0) {
                    pageNode.setAttribute("type", newBraillePage?"both":"print");
                    node.getParentNode().insertBefore(pageNode, node);
                    softPageBreaksBefore--;
                } else if (hardPageBreaksBefore > 0) {
                    pageNode.setAttribute("type", (newBraillePage || followPrint)?"both":"print");
                    node.getParentNode().insertBefore(pageNode, node);
                    hardPageBreaksBefore--;
                } else if (newBraillePage) {
                    pageNode.setAttribute("type", "braille");
                    node.getParentNode().insertBefore(pageNode, node);
                } else if (softPageBreaksAfter > 0) {
                    pageNode.setAttribute("type", "print");
                    if (node.getNextSibling() != null) {
                        node.getParentNode().insertBefore(pageNode, insertAfterNode.getNextSibling());
                    } else {
                        node.getParentNode().appendChild(pageNode);
                    }
                    softPageBreaksAfter--;
                } else if (hardPageBreaksAfter > 0) {
                    pageNode.setAttribute("type",followPrint?"both":"print");
                    if (node.getNextSibling() != null) {
                        node.getParentNode().insertBefore(pageNode, insertAfterNode.getNextSibling());
                    } else {
                        node.getParentNode().appendChild(pageNode);
                    }
                    hardPageBreaksAfter--;
                }

                newBraillePage = false;
                insertAfterNode = pageNode;
                thisIsFirst = false;

                statusIndicator.increment();
            }

            // Process all children

            if (nodeName.equals("text:section")            ||
                nodeName.equals("text:list")               ||
                nodeName.equals("text:list-item")          ||
                nodeName.equals("table:table")             ||
                nodeName.equals("table:table-header-rows") ){

                NodeList children = node.getChildNodes();

                Node child = children.item(0);

                if (nodeName.equals("text:section") && isFirst) {

                    while (child != null) {
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            ret = insertPagination(child, pagenum, masterPageName, true);
                            pagenum = (Integer)ret[0];
                            masterPageName = (String)ret[1];
                            break;
                        } else {
                            child = child.getNextSibling();
                        }
                    }

                } else {

                    while(child != null) {
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            ret = insertPagination(child, pagenum, masterPageName, false);
                            pagenum = (Integer)ret[0];
                            masterPageName = (String)ret[1];
                        }
                        child = child.getNextSibling();

                    }
                }
            }
            else if (nodeName.equals("text:p") ){

                NodeList softPageBreakDescendants = ((Element) node).getElementsByTagName("text:soft-page-break");

                for (int i = 0; i < softPageBreakDescendants.getLength();i++) {
                    ret = insertPagination(softPageBreakDescendants.item(i), pagenum, masterPageName, false);
                    pagenum = (Integer)ret[0];
                    masterPageName = (String)ret[1];
                }
            }

            // Process all siblings

            if (isFirst) {

                Node next = node.getNextSibling();
                while(next != null) {
                    if (next.getNodeType() == Node.ELEMENT_NODE) {
                        ret = insertPagination(next, pagenum, masterPageName, false);
                        pagenum = (Integer)ret[0];
                        masterPageName = (String)ret[1];
                    }
                    next = next.getNextSibling();
                }

            }

            // Return statement

            return new Object[]{pagenum,masterPageName};

        }

        public void cleanUp() {}
    }
    
    private class HeadingNumberingProcessor implements Converter<Document,Document> {

        private Element contentRoot;
        private ListStyleProperties outlineProperties = null;
        private ListNumber outlineNumber = null;
        private ListNumber outlineNumberFrame = null;
        
        @Override
        public Document convert(Document input) throws ConversionException {
            
            logger.info("HeadingNumberingProcessor starting");
            
            try {
            
                contentRoot = input.getDocumentElement();
                outlineProperties = null;
                outlineNumber = null;
                outlineNumberFrame = null;

                Node firstNode = XPathAPI.selectSingleNode(contentRoot, "//body/text/sequence-decls/following::h[1]");
                if (firstNode != null) {
                    statusIndicator.start();
                    statusIndicator.setSteps(Integer.parseInt(XPathAPI.eval(contentRoot, "count(//body/text//h)").str()));
                    statusIndicator.setStatus(ResourceBundle.getBundle(L10N, oooLocale).getString("statusIndicatorStep2"));
                    insertHeadingNumbering(firstNode, true);
                    statusIndicator.finish(true);
                    statusIndicator.close();
                }

                return input;
                
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
        
        /**
         * Insert a special <code>num</code> tag before each numbered or bulleted heading.
         * This method is called recursively so that each heading element is eventually processed once.
         *
         * @param  root     The root element of the .odt document.
         * @param  node     The next node to be processed. On the first call, this parameter is irrelevant.
         * @param  init     Should be set to <code>true</code> on the first call.
         */
        private void insertHeadingNumbering(Node node,
                                            boolean init)
                                     throws TransformerException {

            String xpath = null;
            String display = null;
            String numFormat_i = null;
            int displayLevels_i;
            int num_i;
            int newLevel;
            int prevLevel;
            Node next = null;
            ListNumber number = null;
            NodeList nodes = null;
            Node headingText = null;

            NamedNodeMap attr = node.getAttributes();
            boolean isListHeader = false;
            String nodeName = node.getNodeName();

            String[] numFormat = null;
            String[] prefix = null;
            String[] suffix = null;
            int[] displayLevels = null;
            int[] startValue = null;

            if (init) {

                // Process all headings outside frames

                nodes = XPathAPI.selectNodeList(contentRoot, "//body/text/sequence-decls/following::h[not(ancestor::frame)]");
                for (int i=0;i<nodes.getLength();i++) {
                    next = nodes.item(i);
                    insertHeadingNumbering(next, false);
                }

                // Process all frames of depth = 1

                nodes = XPathAPI.selectNodeList(contentRoot, "//body/text/sequence-decls/following::frame[not(ancestor::frame)]");
                for (int i=0;i<nodes.getLength();i++) {
                    next = nodes.item(i);
                    insertHeadingNumbering(next, false);
                }

            } else {

                if (nodeName.equals("text:h")) {

                    if (attr.getNamedItem("text:style-name") != null) {

                        boolean outlineNumbering = true;
                        String styleName = attr.getNamedItem("text:style-name").getNodeValue();
                        String parentStyleName = "";
                        while (styleName.length() > 0) {
                            if (XPathAPI.eval(contentRoot, "//style[@name='" + styleName + "']/@list-style-name").bool() ||
                                XPathAPI.eval(stylesRoot,  "//style[@name='" + styleName + "']/@list-style-name").bool()) {
                                outlineNumbering = false;
                                break;
                            }
                            parentStyleName = XPathAPI.eval(contentRoot, "//style[@name='" + styleName + "']/@parent-style-name").str();
                            if (parentStyleName.length() == 0) {
                                parentStyleName = XPathAPI.eval(stylesRoot, "//style[@name='" + styleName + "']/@parent-style-name").str();
                            }
                            styleName = parentStyleName;
                        }

                        if (outlineNumbering) {

                            // Get Properties

                            if (outlineProperties!=null) {

                                numFormat = outlineProperties.getNumFormat();
                                prefix = outlineProperties.getPrefix();
                                suffix = outlineProperties.getSuffix();
                                displayLevels = outlineProperties.getDisplayLevels();

                            } else {

                                numFormat = new String[10];
                                prefix = new String[10];
                                suffix = new String[10];
                                displayLevels = new int[10];
                                startValue = new int[10];

                                for (int i=0;i<10;i++) {

                                    xpath = "//styles/outline-style[@name='Outline']/*[@level='" + (i+1) + "'][1]/@bullet-char";
                                    if (XPathAPI.eval(stylesRoot, xpath).bool()) {
                                        numFormat[i] = "bullet";
                                    } else {
                                        xpath = "//styles/outline-style[@name='Outline']/*[@level='" + (i+1) + "'][1]/@num-format";
                                        numFormat[i] = XPathAPI.eval(stylesRoot, xpath).str();
                                    }
                                    xpath = "//styles/outline-style[@name='Outline']/*[@level='" + (i+1) + "']/@start-value";
                                    if (XPathAPI.eval(stylesRoot, xpath).bool()) {
                                        startValue[i] = Integer.parseInt(XPathAPI.eval(stylesRoot, xpath).str());
                                    } else {
                                        startValue[i] = 1;
                                    }
                                    xpath = "//styles/outline-style[@name='Outline']/*[@level='" + (i+1) + "']/@display-levels";
                                    if (XPathAPI.eval(stylesRoot, xpath).bool()) {
                                        displayLevels[i] = Integer.parseInt(XPathAPI.eval(stylesRoot, xpath).str());
                                    } else {
                                        displayLevels[i] = 1;
                                    }
                                    xpath = "//styles/outline-style[@name='Outline']/*[@level='" + (i+1) + "']/@num-prefix";
                                    if (XPathAPI.eval(stylesRoot, xpath).bool()) {
                                        prefix[i] = XPathAPI.eval(stylesRoot, xpath).str();
                                    } else {
                                        prefix[i] = "";
                                    }
                                    xpath = "//styles/outline-style[@name='Outline']/*[@level='" + (i+1) + "']/@num-suffix";
                                    if (XPathAPI.eval(stylesRoot, xpath).bool()) {
                                        suffix[i] = XPathAPI.eval(stylesRoot, xpath).str();
                                    } else {
                                        suffix[i] = "";
                                    }
                                }

                                outlineProperties = new ListStyleProperties("Outline", numFormat, prefix, suffix, displayLevels, startValue);

                            }

                            if (!XPathAPI.eval(node, "./ancestor::frame").bool()) {
                                if (outlineNumber==null) {
                                    outlineNumber = new ListNumber(outlineProperties);
                                }
                                number = outlineNumber;
                            } else {
                                if (outlineNumberFrame==null) {
                                    outlineNumberFrame = new ListNumber(outlineProperties);
                                }
                                number = outlineNumberFrame;
                            }

                            // Update outline number and display

                            prevLevel = number.getLevel();
                            newLevel = Math.min(10,Integer.parseInt(attr.getNamedItem("text:outline-level").getNodeValue()));

                            if ( attr.getNamedItem("text:is-list-header") != null
                              && attr.getNamedItem("text:is-list-header").getNodeValue().equals("true") ) {
                                    isListHeader = true;
                            }

                            if (isListHeader) {

                                newLevel = Math.min(Math.min(prevLevel,newLevel + 1),10);
                                number.reset(newLevel);

                            } else if (numFormat[newLevel-1].equals("")) {

                                display = prefix[newLevel-1] + suffix[newLevel-1];
                                if (!display.equals("")) {
                                    Element numNode = contentRoot.getOwnerDocument().createElement("num");
                                    numNode.setAttribute("value", display + " ");
                                    node.insertBefore(numNode, node.getFirstChild());
                                }

                                newLevel = Math.min(Math.min(prevLevel,newLevel),10);
                                number.reset(newLevel);

                            } else if (numFormat[newLevel-1].equals("bullet")) {

                                newLevel = Math.min(Math.min(prevLevel,newLevel),10);
                                number.reset(newLevel);

                            } else {

                                if (attr.getNamedItem("text:restart-numbering") != null ) {
                                    if (attr.getNamedItem("text:restart-numbering").getNodeValue().equals("true") ) {
                                        number.restart(newLevel,Integer.parseInt(attr.getNamedItem("text:start-value").getNodeValue()));
                                    }
                                }

                                number.update(newLevel);

                                // Display

                                displayLevels_i = displayLevels[newLevel-1];
                                num_i = number.getNumber(newLevel-displayLevels_i+1);
                                numFormat_i = numFormat[newLevel-displayLevels_i];
                                display = "";

                                if (numFormat_i.equals("")) {
                                } else if (numFormat_i.equals("bullet")) {
                                } else if (num_i == 0) {
                                    display += "0";
                                } else {
                                    display += NumberFormatter.format(num_i, numFormat_i);
                                }

                                for (int i = 1;i < displayLevels_i;i++) {

                                    num_i = number.getNumber(newLevel-displayLevels_i+i+1);
                                    numFormat_i = numFormat[newLevel-displayLevels_i+i];

                                    if (numFormat_i.equals("")) {
                                    } else {
                                        if (display.length() > 0) {
                                            display += ".";
                                        }
                                        if (numFormat_i.equals("bullet")) {
                                        } else if (num_i == 0) {
                                            display += "0";
                                        } else {
                                            display += NumberFormatter.format(num_i, numFormat_i);
                                        }
                                    }
                                }

                                display = prefix[newLevel-1] + display + suffix[newLevel-1];

                                Element numNode = contentRoot.getOwnerDocument().createElement("num");
                                numNode.setAttribute("value", display + " ");
                                headingText = node.getFirstChild();
                                while(isWhiteSpaceOnlyTextNode(headingText) ||
                                      headingText.getNodeName().equals("draw:frame") ||
                                      headingText.getNodeName().equals("text:soft-page-break") ||
                                      headingText.getNodeName().equals("pagenum")) {
                                        headingText = headingText.getNextSibling();
                                }
                                if (headingText != null) {
                                    node.insertBefore(numNode, headingText);
                                } else {
                                    node.appendChild(numNode);
                                }

                                // logger.info("<num> " + display + " added to heading");
                            }

                            statusIndicator.increment();
                        }
                    }

                } else if (nodeName.equals("draw:frame")) {

                    int depth = Integer.parseInt(XPathAPI.eval(node, "count(./ancestor-or-self::frame)").str());

                    // Process all heading descendants

                    nodes = XPathAPI.selectNodeList(node, "./descendant::h[count(ancestor::frame)=" + depth + "]");
                    for (int i=0;i<nodes.getLength();i++) {
                        next = nodes.item(i);
                        insertHeadingNumbering(next, false);
                    }

                    // Process all frame descendants

                    nodes = XPathAPI.selectNodeList(node, "./descendant::frame[count(ancestor::frame)=" + depth + "]");
                    for (int i=0;i<nodes.getLength();i++) {
                        next = nodes.item(i);
                        insertHeadingNumbering(next, false);
                    }
                }
            }
        }

        public void cleanUp() {}
    }
    
    private class ListNumberingProcessor implements Converter<Document,Document>, Parameterized {

        private Element contentRoot;
        private String[] listPrefixes;
        private Map<String,String> linkedLists = new TreeMap<String,String>();
        private Map<String,String> listStyles = new TreeMap<String,String>();
        private Map<String,ListStyleProperties> listProperties = new TreeMap<String,ListStyleProperties>();
        private Map<String,ListNumber> listNumber = new TreeMap<String,ListNumber>();
        private Map<String,ListNumber> listNumberFrame = new TreeMap<String,ListNumber>();
        private ListNumber currentNumber = null;
        
        public void setParameter(String key, Object value) {
            if ("listPrefixes".equals(key)) {
                listPrefixes = (String[])value;
            } else {
                throw new IllegalArgumentException("parameter '" + key + "' not supported");
            }
        }
        
        public Document convert(Document input) throws ConversionException {
            
            logger.info("ListNumberingProcessor starting");

            try {
            
                contentRoot = input.getDocumentElement();
                linkedLists.clear();
                listStyles.clear();
                listProperties.clear();
                listNumber.clear();
                listNumberFrame.clear();
                currentNumber = null;

                Node firstNode = XPathAPI.selectSingleNode(contentRoot, "//body/text/sequence-decls/following::list[@id][1]");
                if (firstNode != null) {
                    statusIndicator.start();
                    statusIndicator.setSteps(Integer.parseInt(XPathAPI.eval(contentRoot, "count(//body/text//list[@id])").str()));
                    statusIndicator.setStatus(ResourceBundle.getBundle(L10N, oooLocale).getString("statusIndicatorStep3"));
                    insertListNumbering(firstNode, 0, true);
                    statusIndicator.finish(true);
                    statusIndicator.close();
                }

                return input;
                
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
        
        /**
         * Insert a special <code>num</code> tag before each numbered or bulleted listitem.
         * This method is called recursively so that each list is eventually processed once.
         *
         * @param  root     The root element of the .odt document.
         * @param  node     The next node to be processed. On the first call, this parameter is irrelevant.
         * @param  level    The level of the next node, being the number of ancestors of type list. On the first call, this parameter is irrelevant.
         * @param  init     Should be set to <code>true</code> on the first call.
         */
        private void insertListNumbering(Node node,
                                         int level,
                                         boolean init)
                                  throws TransformerException {

            String listStyleName = null;
            String xpathBase = null;
            String xpath = null;
            String id = null;
            String prevId = null;
            String linkId = "";
            String tmp = null;
            String display = null;
            String numFormat_i = null;
            int displayLevels_i;
            int num_i;

            int newLevel = 0;
            int prevLevel = 0;
            Element numNode = null;
            Node listText = null;
            Node next = null;
            Node child = null;
            NodeList nodes = null;
            ListStyleProperties properties = null;

            String nodeName = node.getNodeName();
            NamedNodeMap attr = null;
            boolean autolist = false;
            boolean isListHeader = false;

            String[] numFormat = null;
            String[] prefix = null;
            String[] suffix = null;
            int[] displayLevels = null;
            int[] startValue = null;

            if (init) {

                // Initialization

                nodes = XPathAPI.selectNodeList(contentRoot, "//body/text/sequence-decls/following::list[@id]");

                for (int i=0;i<nodes.getLength();i++) {

                    next = nodes.item(i);
                    attr = next.getAttributes();
                    id = XPathAPI.eval(next, "./@id").str();

                    if (attr.getNamedItem("text:style-name") != null) {

                        listStyleName = attr.getNamedItem("text:style-name").getNodeValue();
                        tmp = id;

                        if (XPathAPI.eval(next, "./@continue-list").bool()) {
                            tmp = XPathAPI.eval(next, "./@continue-list").str();
                        } else if (XPathAPI.eval(next, "./@continue-numbering").bool()) {
                            if (XPathAPI.eval(next, "./@continue-numbering").str().equals("true")) {
                                if (prevId!=null) {
                                    tmp = prevId;
                                }
                            }
                        }
                        if (!linkedLists.containsKey(tmp)) {
                            tmp = id;
                        }
                        if (tmp.equals(id)) {
                            listStyles.put(id, listStyleName);
                        }
                        while (!linkId.equals(tmp)) {
                            linkId = tmp;
                            if (linkedLists.containsKey(linkId)) {
                                tmp = linkedLists.get(linkId);
                            }
                        }

                        linkedLists.put(id, linkId);
                    }

                    prevId = id;
                }

                // Process all lists[@id] outside frames

                nodes = XPathAPI.selectNodeList(contentRoot, "//body/text/sequence-decls/following::list[@id][not(ancestor::frame)]");
                for (int i=0;i<nodes.getLength();i++) {
                    next = nodes.item(i);
                    insertListNumbering(next, 0, false);
                }

                // Process all frames of depth = 1

                nodes = XPathAPI.selectNodeList(contentRoot, "//body/text/sequence-decls/following::frame[not(ancestor::frame)]");
                for (int i=0;i<nodes.getLength();i++) {
                    next = nodes.item(i);
                    insertListNumbering(next, 0, false);
                }

            } else {

                if (nodeName.equals("text:list")) {

                    if (XPathAPI.eval(node, "@id").bool()) {

                        // Main list

                        id = XPathAPI.eval(node, "@id").str();

                        level = 1;

                        statusIndicator.increment();

                        if (linkedLists.containsKey(id)) {

                            id = linkedLists.get(id);
                            listStyleName = listStyles.get(id);

                            if (Pattern.matches("L[1-9][0-9]*", listStyleName)) {
                                autolist = true;
                            }

                            // Set Properties

                            if (!listProperties.containsKey(listStyleName)) {

                                numFormat = new String[10];
                                prefix = new String[10];
                                suffix = new String[10];
                                displayLevels = new int[10];
                                startValue = new int[10];

                                xpathBase = "//" + (autolist?"automatic-":"") + "styles/list-style[@name='" + listStyleName + "']/";
                                Node root = autolist?contentRoot:stylesRoot;

                                for (int i=0;i<10;i++) {

                                    xpath = xpathBase + "*[@level='" + (i+1) + "'][1]/@bullet-char";
                                    if (XPathAPI.eval(root, xpath).bool()) {
                                        numFormat[i] = "bullet";
                                    } else {
                                        xpath = xpathBase + "*[@level='" + (i+1) + "'][1]/@num-format";
                                        numFormat[i] = XPathAPI.eval(root, xpath).str();
                                    }
                                    xpath = xpathBase + "*[@level='" + (i+1) + "']/@start-value";
                                    if (XPathAPI.eval(root, xpath).bool()) {
                                        startValue[i] = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                                    } else {
                                        startValue[i] = 1;
                                    }
                                    xpath = xpathBase + "*[@level='" + (i+1) + "']/@display-levels";
                                    if (XPathAPI.eval(root, xpath).bool()) {
                                        displayLevels[i] = Integer.parseInt(XPathAPI.eval(root, xpath).str());
                                    } else {
                                        displayLevels[i] = 1;
                                    }
                                    xpath = xpathBase + "*[@level='" + (i+1) + "']/@num-prefix";
                                    if (XPathAPI.eval(root, xpath).bool()) {
                                        prefix[i] = XPathAPI.eval(root, xpath).str();
                                    } else {
                                        prefix[i] = "";
                                    }
                                    xpath = xpathBase + "*[@level='" + (i+1) + "']/@num-suffix";
                                    if (XPathAPI.eval(root, xpath).bool()) {
                                        suffix[i] = XPathAPI.eval(root, xpath).str();
                                    } else {
                                        suffix[i] = "";
                                    }
                                }

                                properties = new ListStyleProperties(listStyleName, numFormat, prefix, suffix, displayLevels, startValue);
                                listProperties.put(listStyleName,properties);
                            }

                            // Update currentnumber

                            if (!XPathAPI.eval(node, "./ancestor::frame").bool()) {
                                if (!listNumber.containsKey(id)) {
                                    listNumber.put(id, new ListNumber(listProperties.get(listStyleName)));
                                }
                                currentNumber = listNumber.get(id);
                            } else {
                                if (!listNumberFrame.containsKey(id)) {
                                    listNumberFrame.put(id, new ListNumber(listProperties.get(listStyleName)));
                                }
                                currentNumber = listNumberFrame.get(id);
                            }

                        } else {
                            logger.log(Level.SEVERE, null, "Exception: id cannot be found");
                        }
                    }

                    // Process all children of type 'list-item' or 'list-header'

                    nodes = XPathAPI.selectNodeList(node, "list-item | list-header");
                    for (int i=0;i<nodes.getLength();i++) {
                        child = nodes.item(i);
                        insertListNumbering(child, level, false);
                    }

                } else if (nodeName.equals("text:list-item") || nodeName.equals("text:list-header")) {

                    if (nodeName.equals("text:list-header")) {
                        isListHeader = true;
                    }

                    nodes = XPathAPI.selectNodeList(node, "p | h | list");

                    for (int j=0;j<nodes.getLength();j++) {

                        child = nodes.item(j);
                        numNode = null;

                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            if (child.getNodeName().equals("text:p")
                             || child.getNodeName().equals("text:h")) {

                                // Get properties

                                properties = currentNumber.getProperties();

                                numFormat = properties.getNumFormat();
                                prefix = properties.getPrefix();
                                suffix = properties.getSuffix();
                                displayLevels = properties.getDisplayLevels();

                                prevLevel = currentNumber.getLevel();
                                newLevel = Math.min(level,10);

                                if (isListHeader) {

                                    newLevel = Math.min(Math.min(prevLevel,newLevel + 1),10);
                                    currentNumber.reset(newLevel);

                                } else if (numFormat[newLevel-1].equals("")) {

                                    display = prefix[newLevel-1] + suffix[newLevel-1];
                                    if (!display.equals("")) {
                                        numNode = contentRoot.getOwnerDocument().createElement("num");
                                        numNode.setAttribute("value", display + " ");
                                    }

                                    newLevel = Math.min(Math.min(prevLevel,newLevel),10);
                                    currentNumber.reset(newLevel);

                                } else if (numFormat[newLevel-1].equals("bullet")) {

                                    display = listPrefixes[newLevel-1];
                                    if (!display.equals("")) {
                                        numNode = contentRoot.getOwnerDocument().createElement("num");
                                        numNode.setAttribute("value", display + " ");
                                    }

                                    newLevel = Math.min(Math.min(prevLevel,newLevel),10);
                                    currentNumber.reset(newLevel);

                                } else {

                                    attr = node.getAttributes();

                                    if (attr.getNamedItem("text:start-value") != null ) {
                                        currentNumber.restart(newLevel,Integer.parseInt(attr.getNamedItem("text:start-value").getNodeValue()));
                                    }

                                    currentNumber.update(newLevel);

                                    // Display

                                    displayLevels_i = displayLevels[newLevel-1];
                                    num_i = currentNumber.getNumber(newLevel-displayLevels_i+1);
                                    numFormat_i = numFormat[newLevel-displayLevels_i];
                                    display = "";

                                    if (numFormat_i.equals("")) {
                                    } else if (numFormat_i.equals("bullet")) {
                                    } else if (num_i == 0) {
                                        display += "0";
                                    } else {
                                        display += NumberFormatter.format(num_i, numFormat_i);
                                    }

                                    for (int i = 1;i < displayLevels_i;i++) {

                                        num_i = currentNumber.getNumber(newLevel-displayLevels_i+i+1);
                                        numFormat_i = numFormat[newLevel-displayLevels_i+i];

                                        if (numFormat_i.equals("")) {
                                        } else {
                                            if (display.length() > 0) {
                                                display += ".";
                                            }
                                            if (numFormat_i.equals("bullet")) {
                                            } else if (num_i == 0) {
                                                display += "0";
                                            } else {
                                                display += NumberFormatter.format(num_i, numFormat_i);
                                            }
                                        }
                                    }

                                    display = prefix[newLevel-1] + display + suffix[newLevel-1];

                                    numNode = contentRoot.getOwnerDocument().createElement("num");
                                    numNode.setAttribute("value", display + " ");

                                }

                                if (numNode != null) {

                                    listText = child.getFirstChild();                                
                                    while(listText != null &&
                                           (isWhiteSpaceOnlyTextNode(listText) ||
                                            listText.getNodeName().equals("text:soft-page-break") ||
                                            listText.getNodeName().equals("pagenum"))) {
                                        listText = listText.getNextSibling();
                                    }
                                    if (listText != null) {
                                        child.insertBefore(numNode, listText);
                                    } else {
                                        child.appendChild(numNode);
                                    }
                                }

                                isListHeader = true;

                            } else if (child.getNodeName().equals("text:list")) {
                                newLevel = level + 1;
                                insertListNumbering(child, newLevel, false);
                            }
                        }
                    }

                } else if (nodeName.equals("draw:frame")) {

                    int depth = Integer.parseInt(XPathAPI.eval(node, "count(./ancestor-or-self::frame)").str());

                    // Process all descendants of type list[@id]

                    nodes = XPathAPI.selectNodeList(node, "./descendant::list[@id][count(ancestor::frame)=" + depth + "]");
                    for (int i=0;i<nodes.getLength();i++) {
                        next = nodes.item(i);
                        insertListNumbering(next, level, false);
                    }

                    // Process all frame descendants

                    nodes = XPathAPI.selectNodeList(node, "./descendant::frame[count(ancestor::frame)=" + depth + "]");
                    for (int i=0;i<nodes.getLength();i++) {
                        next = nodes.item(i);
                        insertListNumbering(next, 0, false);
                    }
                }
            }
        }

        public void cleanUp() {}
    }
    
    private class CorrectionProcessor implements Converter<Document,Document> {

        public Document convert(Document input) throws ConversionException {
            
            logger.info("CorrectionProcessor starting");

            try {
                
                Element contentRoot = input.getDocumentElement();

                OdtUtils.replaceObjectContent(docBuilder, input, zip);
                OdtUtils.removeEmptyHeadings(contentRoot);
              //OdtUtils.normalizeTextS(contentDoc);
              //OdtUtils.removeEmptyParagraphs(contentRoot);
              //OdtUtils.insertEmptyParaForHeadings(contentDoc);

                return input;
                
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }

        public void cleanUp() {}
    }
    
    private class EnsureMetadataReferences extends XSLTransformer {
    
        private EnsureMetadataReferences() throws Exception {
            super("ensure-references", false);
        }
        
        @Override
        public void setParameter(String key, Object value) {
            if ("styles-url".equals(key)) {
                super.setParameter("styles-url", (URI)value);
            }
        }
    }
    
    private class MainTransformer extends XSLTransformer {
        
        private final Set<String> publicParameters = new HashSet<String>();
        private final Set<String> privateParameters = new HashSet<String>();
        
        @Override
        public void setParameter(String key, Object value) {
            if ("styles-url".equals(key)) {
                super.setParameter("styles-url", (URI)value);
                return;
            }
            if (privateParameters.contains(key)) {
                super.setParameter("param" + capitalizeFirstLetter(key), value);
                return;
            }
            if (!publicParameters.contains(key)) {
                throw new IllegalArgumentException("parameter '" + key + "' not supported");
            }
            if ("noterefNumberPrefixes".equals(key)) {
                Map<String,String> noterefNumberPrefixesMap = (Map<String,String>)value;
                String[] noterefNumberFormats = new String[noterefNumberPrefixesMap.size()];
                String[] noterefNumberPrefixes = new String[noterefNumberPrefixesMap.size()];
                int i = 0;
                for (String format : noterefNumberPrefixesMap.keySet()) {
                    noterefNumberFormats[i] = format;
                    noterefNumberPrefixes[i] = noterefNumberPrefixesMap.get(format);
                    i++;
                }
                super.setParameter("paramNoterefNumberFormats", noterefNumberFormats);
                super.setParameter("paramNoterefNumberPrefixes", noterefNumberPrefixes);
                return;
            }
            super.setParameter("param" + capitalizeFirstLetter(key), value);
        }
        
        private MainTransformer() throws Exception {
            super("main");
            publicParameters.add("hyphenationEnabled");
            publicParameters.add("noterefSpaceBefore");
            publicParameters.add("noterefSpaceAfter");
            publicParameters.add("noterefNumberPrefixes");
            publicParameters.add("configuredCharacterStyles");
            publicParameters.add("configuredParagraphStyles");
            publicParameters.add("keepEmptyParagraphStyles");
            publicParameters.add("tocUptoLevel");
            publicParameters.add("tableUpperBorder");
            publicParameters.add("tableLowerBorder");
            publicParameters.add("stairstepTableEnabled");
            publicParameters.add("columnDelimiter");
            publicParameters.add("columnHeadings");
            publicParameters.add("rowHeadings");
            publicParameters.add("tableHeadingSuffix");
            publicParameters.add("repeatTableHeading");
            publicParameters.add("mirrorTable");
            publicParameters.add("pictureDescriptionPrefix");
            publicParameters.add("pictureOpeningMarkPrefix");
            publicParameters.add("pictureClosingMarkPrefix");
            publicParameters.add("frameUpperBorder");
            publicParameters.add("frameLowerBorder");
            publicParameters.add("headingUpperBorder");
            publicParameters.add("headingLowerBorder");
            
            privateParameters.add("singleBodyVolumeId");
            privateParameters.add("singleRearVolumeId");
            privateParameters.add("manualVolumeSections");
            privateParameters.add("manualVolumeIds");
        }
        
        @Override
        public void convert(File input, File output) throws ConversionException {
            try {
                super.setParameter("controller-url", controllerURL);
                super.setParameter("paramBodyMatterMode", ODT2XMLConverter.this.getParameter("bodyMatterMode"));
                super.setParameter("paramRearMatterMode", ODT2XMLConverter.this.getParameter("rearMatterMode"));
                super.convert(input, output);
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
    }
    
    private class LanguagesAndTypefaceTransformer extends XSLTransformer {
        
        private final Set<String> publicParameters = new HashSet<String>();
        
        @Override
        public void setParameter(String key, Object value) {
            if (!publicParameters.contains(key)) { throw new IllegalArgumentException("parameter '" + key + "' not supported"); }
            if ("mainTranslationTable".equals(key)) {
                super.setParameter("paramMainTranslationTable", ((TranslationTable)value).getID());
                return;
            }
            if ("translationTables".equals(key)) {
                Map<Locale,TranslationTable> translationTablesMap = (Map<Locale,TranslationTable>)value;
                String[] locales = new String[translationTablesMap.size()];
                String[] translationTables = new String[translationTablesMap.size()];
                int i = 0;
                for (Locale locale : translationTablesMap.keySet()) {
                    locales[i] = locale.toString().replaceAll("_", "-");
                    translationTables[i] = translationTablesMap.get(locale).getID();
                    i++;
                }
                super.setParameter("paramLocales", locales);
                super.setParameter("paramTranslationTables", translationTables);
                return;
            }
            super.setParameter("param" + capitalizeFirstLetter(key), value);
        }
        
        private LanguagesAndTypefaceTransformer() throws Exception {
            super("languages-and-typeface");
            publicParameters.add("translationTables");
            publicParameters.add("mainTranslationTable");
            publicParameters.add("mathCode");
            publicParameters.add("keepBoldfaceStyles");
            publicParameters.add("keepItalicStyles");
            publicParameters.add("keepUnderlineStyles");
            publicParameters.add("keepCapsStyles");
        }
    }
    
    /**
     * A <code>ListNumber</code> instance keeps track of the numbering of an OpenOffice.org list (or the sequence of OpenOffice.org headings).
     *
     * The current level as well as the current number in each level is kept and
     * updated whenever <code>reset</code>, <code>restart</code> or <code>update</code> is called.
     *
     * @author freesb
     */
    private class ListNumber {

        private int level;
        private int[] num;
        private int[] startValue;
        private ListStyleProperties properties;

        /**
         * Creates and initializes a new <code>ListNumber</code> instance.
         * The level is set to zero and the number in each level is set to the corresponding start value minus 1.
         *
         * @param properties   The properties of the list.
         */
        private ListNumber(ListStyleProperties properties) {

            level = 0;
            num = new int[10];
            this.properties = properties;
            startValue = properties.getStartValue().clone();

            for (int i=1;i<=10;i++) {
                num[i-1] = startValue[i-1] - 1;
            }
        }

        /**
         * The current level is set to <code>newLevel</code> and
         * the numbers of all higher levels are set to the corresponding start value minus 1.
         *
         * @param newLevel   The new level.
         */
        public void reset(int newLevel) {

            level = newLevel;
            for (int i=level+1;i<=10;i++) {
                num[i-1] = startValue[i-1] - 1;
            }
        }

        /**
         * The numbering of a certain level is restarted.
         *
         * @param   level
         * @param   num     If <code>num</code> is equal to <code>-1</code>, the number of the specified level is set to the corresponding start value minus 1.
         *                  Else, the number is set to <code>num - 1</code>.
         */
        public void restart(int level,
                            int num) {

            if (num == -1) {
                this.num[level-1] = startValue[level-1] - 1;
            } else {
                this.num[level-1] = num - 1;
            }
        }

        /**
         * The current level is set to <code>newLevel</code> and the numbering is updated accordingly.
         *
         * @param newLevel   The new level.
         */
        public void update(int newLevel) {

            for (int i=level+1;i<newLevel;i++) {
                num[i-1]++;
            }
            num[newLevel-1]++;
            reset(newLevel);

        }

        /**
         * @return   The current level.
         */
        public int getLevel() {
            return level;
        }

        /**
         * @param     level
         * @return    The current numbers in the specified level.
         */
        public int getNumber(int level) {
            return num[level-1];
        }

        /**
         * @return   The properties of the list.
         */
        public ListStyleProperties getProperties() {
            return properties;
        }
    }
    
    /**
     * The numbering properties of either an OpenOffice.org list or the sequence of OpenOffice.org headings.
     * These properties are
     * <ul>
     * <li>{@link #styleName}: the name of the numbering style.</li>
     * <li>{@link #numFormat}: the format of the number for various levels (level 1 - level 10).
     *     Possible values are: "<i>bullet</i>", "" (empty string), "<i>i</i>", "<i>I</i>", "<i>a</i>", "<i>A</i>", etc.
     * <li>{@link #prefix}: the prefix of the number for various levels.</li>
     * <li>{@link #suffix}: the suffix of the number for various levels.</li>
     * <li>{@link #displayLevels}: the number of sublevels shown for various levels.</li>
     * <li>{@link #startValue}: the start value if the number for various levels.</li>
     * </ul>
     *
     * @author freesb
     */
    private class ListStyleProperties {

        public String styleName = null;

        private String[] numFormat = null;
        private String[] prefix = null;
        private String[] suffix = null;
        private int[] displayLevels = null;
        private int[] startValue = null;

        private ListStyleProperties(String styleName,
                                    String[] numFormat,
                                    String[] prefix,
                                    String[] suffix,
                                    int[] displayLevels,
                                    int[] startValue) {

            this.styleName = styleName;

            this.numFormat = numFormat.clone();
            this.prefix = prefix.clone();
            this.suffix = suffix.clone();
            this.displayLevels = displayLevels.clone();
            this.startValue = startValue.clone();

        }

        public String getStyleName() {
            return styleName;
        }

        public String[] getNumFormat() {
            return numFormat;
        }

        public String[] getPrefix() {
            return prefix;
        }

        public String[] getSuffix() {
            return suffix;
        }

        public int[] getDisplayLevels() {
            return displayLevels;
        }

        public int[] getStartValue() {
            return startValue;
        }
    }

    private enum FrontMatterMode { EXTENDED, BASIC, NONE }
    private enum TableOfContentMode { EXTENDED, BASIC, NONE }
    
    private class XMLImpl implements XML {
        
        private File xmlFile;
        
        private final List<Volume> volumes = new ArrayList<Volume>();
        
        private final PreliminarySectionTransformer preliminarySectionTransformer;
        private final BodySectionTransformer bodySectionTransformer;
        
        private final boolean braillePageNumbers = (Boolean)getParameter("braillePageNumbers");
        private final boolean printPageNumbers = (Boolean)getParameter("printPageNumbers");
        private final PageNumberFormat preliminaryPageNumberFormat = (PageNumberFormat)getParameter("preliminaryPageNumberFormat");
        private final String creator = (String)getParameter("creator");
        private final String date = (new SimpleDateFormat("yyyy")).format(new Date());
        private final String noteSectionTitle = (String)getParameter("noteSectionTitle");
        private final boolean volumeInfoEnabled = (Boolean)getParameter("volumeInfoEnabled");
        private final boolean transcriptionInfoEnabled = (Boolean)getParameter("transcriptionInfoEnabled");
        private final String tableOfContentTitle = (String)getParameter("tableOfContentTitle");
        private final String tnPageTitle = (String)getParameter("tnPageTitle");
        private final String specialSymbolListTitle = (String)getParameter("specialSymbolListTitle");
        private final List<SpecialSymbol> specialSymbolList = (List<SpecialSymbol>)getParameter("specialSymbolList");
        private final boolean mergeUnnumberedPages = (Boolean)getParameter("mergeUnnumberedPages"); 
        
        private final String L10N_in;
        private final String L10N_and;
        private final String L10N_volume;
        private final String L10N_volumes;
        private final String L10N_braillePages;
        private final String L10N_printPages;
        private final String L10N_transcriptionInfo;
        private final String L10N_continuedSuffix;
        
        private XMLImpl() throws Exception {
            
            preliminarySectionTransformer = new PreliminarySectionTransformer();
            bodySectionTransformer = new BodySectionTransformer();
            
            ResourceBundle bundle = ResourceBundle.getBundle(L10N, (Locale)getParameter("mainLocale"));
            L10N_in = bundle.getString("in");
            L10N_and = bundle.getString("and");
            L10N_volume = bundle.getString("volume");
            L10N_volumes = bundle.getString("volumes");
            L10N_braillePages = bundle.getString("braillePages");
            L10N_printPages = bundle.getString("printPages");
            L10N_transcriptionInfo = bundle.getString("transcriptionInfo");
            L10N_continuedSuffix = bundle.getString("continuedSuffix");
        }
        
        @Override
        public List<Volume> getVolumes() {
            return new ArrayList<Volume>(volumes);
        }
        
        private void addVolume(Volume volume) {
            volumes.add(volume);
        }
        
        public void close() {
            xmlFile.delete();
        }
        
        private class PreliminarySectionTransformer extends XSLTransformer {

            private final Set<String> privateParameters = new HashSet<String>();
            
            @Override
            public void setParameter(String key, Object value) {
                if (privateParameters.contains(key)) {
                    super.setParameter("param" + capitalizeFirstLetter(key), value);
                }
            }
            
            private PreliminarySectionTransformer() throws Exception {
                super("split-volumes");
                super.setParameter("paramBodyMatterEnabled", false);
                super.setParameter("paramRearMatterEnabled", false);
                super.setParameter("paramVolumeInfoEnabled", volumeInfoEnabled);
                super.setParameter("paramTranscriptionInfoEnabled", transcriptionInfoEnabled);
                super.setParameter("paramTableOfContentTitle", tableOfContentTitle);
                super.setParameter("paramTnPageTitle", tnPageTitle);
                super.setParameter("paramTranscribersNotes", new String[0]);
                super.setParameter("paramSpecialSymbolsListTitle", specialSymbolListTitle);
                super.setParameter("paramNoteSectionTitle", noteSectionTitle);
                super.setParameter("paramContinuedHeadingSuffix", L10N_continuedSuffix);
                privateParameters.add("volumeId");
                privateParameters.add("frontMatterEnabled");
                privateParameters.add("tableOfContentEnabled");
                privateParameters.add("specialSymbolsListEnabled");
                privateParameters.add("tnPageEnabled");
                privateParameters.add("extendedFront");
                privateParameters.add("extendedToc");
                privateParameters.add("transcriptionInfoLine");
                privateParameters.add("volumeInfoLines");
                privateParameters.add("specialSymbols");
                privateParameters.add("specialSymbolsDots");
                privateParameters.add("specialSymbolsDescription");
            }
        }
        
        private class BodySectionTransformer extends XSLTransformer {
            
            @Override
            public void setParameter(String key, Object value) {
                if ("volumeId".equals(key)) {
                    super.setParameter("paramVolumeId", value);
                }
            }
            
            private BodySectionTransformer() throws Exception {
                super("split-volumes");
                super.setParameter("paramFrontMatterEnabled", false);
                super.setParameter("paramBodyMatterEnabled", true);
                super.setParameter("paramRearMatterEnabled", true);
                super.setParameter("paramContinuedHeadingSuffix", L10N_continuedSuffix);
                super.setParameter("paramNoteSectionTitle", noteSectionTitle);
            }
        }
    
        private class VolumeImpl implements XML.Volume {
            
            private String title;
            protected final String identifier;
            
            private File bodyFile = null;
            private File preliminaryFile = null;
                    
            private final boolean transcribersNotesPageEnabled;
            private final boolean specialSymbolListEnabled;
            
            private FrontMatterMode frontMatterMode;
            private TableOfContentMode tableOfContentMode;

            private int braillePagesStart = 1;
            private int numberOfBraillePages = 0;
            private int numberOfPreliminaryPages = 0;
            
            private String firstPrintPage = null;
            private String lastPrintPage = null;

            private List<SpecialSymbol> specialSymbols = null;
            private List<String> transcribersNotes = null;

            private VolumeImpl(Configuration.Volume settings) {

                title = settings.getTitle();
                identifier = createUniqueIdentifier();

                frontMatterMode = settings.getFrontMatter() ? FrontMatterMode.BASIC : FrontMatterMode.NONE;
                tableOfContentMode = settings.getTableOfContent() ? TableOfContentMode.BASIC : TableOfContentMode.NONE;
                transcribersNotesPageEnabled = settings.getTranscribersNotesPage();
                specialSymbolListEnabled = settings.getSpecialSymbolList();

            }

            @Override
            public void setBraillePagesStart(int value) {
                braillePagesStart = value;
                if (getFrontMatterEnabled() &&
                    volumeInfoEnabled &&
                    braillePageNumbers) {
                    if (preliminaryFile != null) {
                        preliminaryFile.delete();
                        preliminaryFile = null;
                    } 
                }
            }
            
            @Override
            public void setNumberOfBraillePages(int value) {
                numberOfBraillePages = value;
                if (getFrontMatterEnabled() &&
                    volumeInfoEnabled &&
                    braillePageNumbers) {
                    if (preliminaryFile != null) {
                        preliminaryFile.delete();
                        preliminaryFile = null;
                    }
                }
            }
            
            @Override
            public void setNumberOfPreliminaryPages(int value) {
                numberOfPreliminaryPages = value;
                if (getFrontMatterEnabled() &&
                    volumeInfoEnabled &&
                    braillePageNumbers) {
                    if (preliminaryFile != null) {
                        preliminaryFile.delete();
                        preliminaryFile = null;
                    }
                }
            }

            @Override 
            public String getTitle() { return title; }
            @Override
            public boolean getFrontMatterEnabled() { return frontMatterMode != FrontMatterMode.NONE; }
            @Override
            public boolean getTableOfContentEnabled() { return tableOfContentMode != TableOfContentMode.NONE; }
            @Override
            public boolean getTranscribersNotesPageEnabled() { return transcribersNotesPageEnabled; }
            @Override
            public boolean getSpecialSymbolListEnabled() { return specialSymbolListEnabled; }
            @Override
            public int getFirstBraillePage() { return braillePagesStart; }
            @Override
            public int getLastBraillePage() { return braillePagesStart + numberOfBraillePages - 1; }

            private String getVolumeInfo() throws Exception {
                
                if (!(getFrontMatterEnabled() && volumeInfoEnabled)) { return ""; }
                
                String volumeInfo = capitalizeFirstLetter(L10N_in) + " " + volumes.size() + " " +
                        (volumes.size()>1 ? L10N_volumes : L10N_volume) + "\n@title\n@pages";

                int firstBraillePage = getFirstBraillePage();
                int lastBraillePage = getLastBraillePage();
                String braillePages = "";
                String printPages = "";
                extractPrintPageRange();

                if (braillePageNumbers) {
                                                                     braillePages  = L10N_braillePages + " ";
                    if (numberOfPreliminaryPages > 0) {
                        switch (preliminaryPageNumberFormat) {
                            case P:
                                if (numberOfPreliminaryPages > 1) {  braillePages += "p1-"; }
                                                                     braillePages += "p" + numberOfPreliminaryPages ;
                                break;
                            case ROMAN:
                                if (numberOfPreliminaryPages > 1) {  braillePages += "i-"; }
                                                                     braillePages += NumberFormatter.format(numberOfPreliminaryPages, "i");
                                break;
                            case ROMANCAPS:
                                if (numberOfPreliminaryPages > 1) {  braillePages += "I-"; }
                                                                     braillePages += NumberFormatter.format(numberOfPreliminaryPages, "I");
                                break;
                        }
                    }         
                    if (numberOfPreliminaryPages > 0 && lastBraillePage >= firstBraillePage) {
                                                                     braillePages += " " + L10N_and.toLowerCase() + " "; }
                    if (lastBraillePage >= firstBraillePage) {
                        if (lastBraillePage > firstBraillePage) {    braillePages += firstBraillePage + "-"; }
                                                                     braillePages += lastBraillePage;
                    }
                }
                if (printPageNumbers) {
                    if (firstPrintPage != null) {                    printPages  = L10N_printPages + " " + firstPrintPage;
                        if (lastPrintPage != null) {                 printPages += "-" + lastPrintPage; }
                    }
                }

                volumeInfo = volumeInfo.replaceFirst("@title", getTitle())
                                       .replaceFirst("@pages", braillePages + (printPages.length()>0?"\n":"") + printPages);
                
                return volumeInfo;
            }
            
            @Override
            public File getBodySection() throws Exception {
                if (bodyFile == null || !bodyFile.exists()) {
                    
                    bodyFile = FileCreator.createTempFile(".daisy.body." + (volumes.indexOf(this) + 1) + ".xml");
                    
                    bodySectionTransformer.setParameter("volumeId", identifier);
                    bodySectionTransformer.convert(xmlFile, bodyFile);
                }
                return bodyFile;
            }

            @Override
            public File getPreliminarySection() throws Exception {
                
                if (!getFrontMatterEnabled() &&
                    !getTableOfContentEnabled() &&
                    !getTranscribersNotesPageEnabled() &&
                    !getSpecialSymbolListEnabled()) {
                    throw new UnsupportedOperationException("Volume doesn't have a preliminary section");
                }
                
                if (preliminaryFile == null || !preliminaryFile.exists()) {
                    
                    preliminaryFile = FileCreator.createTempFile(".daisy.front." + (volumes.indexOf(this) + 1) + ".xml");
                    
                    String volumeInfo = getVolumeInfo();
                    String transcriptionInfo = L10N_transcriptionInfo.replaceFirst("@creator", creator)
                                                                     .replaceFirst("@date", date);
                    
                    List<String> symbols = new ArrayList<String>();
                    List<String> symbolsDots = new ArrayList<String>();
                    List<String> symbolsDescription = new ArrayList<String>();
                    
                    if (getSpecialSymbolListEnabled()) {
                        for (SpecialSymbol symbol : getSpecialSymbols()) {
                            symbols.add(symbol.getSymbol());
                            symbolsDots.add("(" + symbol.getDotPattern() + ")");
                            symbolsDescription.add(symbol.getDescription());
                        }
                    }
                    
                    preliminarySectionTransformer.setParameter("volumeId", identifier);
                    preliminarySectionTransformer.setParameter("frontMatterEnabled", getFrontMatterEnabled());
                    preliminarySectionTransformer.setParameter("tableOfContentEnabled", getTableOfContentEnabled());
                    preliminarySectionTransformer.setParameter("specialSymbolsListEnabled", getSpecialSymbolListEnabled());
                    preliminarySectionTransformer.setParameter("tnPageEnabled", getTranscribersNotesPageEnabled());
                    preliminarySectionTransformer.setParameter("extendedFront", frontMatterMode == FrontMatterMode.EXTENDED);
                    preliminarySectionTransformer.setParameter("extendedToc", tableOfContentMode == TableOfContentMode.EXTENDED);
                    preliminarySectionTransformer.setParameter("transcriptionInfoLine", transcriptionInfo);
                    preliminarySectionTransformer.setParameter("volumeInfoLines", volumeInfo.split("\n"));
                    preliminarySectionTransformer.setParameter("specialSymbols", symbols.toArray(new String[symbols.size()]));
                    preliminarySectionTransformer.setParameter("specialSymbolsDots", symbolsDots.toArray(new String[symbolsDots.size()]));
                    preliminarySectionTransformer.setParameter("specialSymbolsDescription", symbolsDescription.toArray(new String[symbolsDescription.size()]));

                    preliminarySectionTransformer.convert(xmlFile, preliminaryFile);
                    
                }
                
                return preliminaryFile;
            }
            
            private void extractPrintPageRange() throws Exception {
                
                getBodySection();
                
                String volumeNode = "dtb:volume[@id='" + identifier  + "']";

                String s;
                if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                    "//" + volumeNode +
                                    "/*[not(self::dtb:pagebreak or ancestor::dtb:div[@class='not-in-volume'])][1][self::dtb:pagenum]", NAMESPACE)) {
                    s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                    "//" + volumeNode +
                                    "/*[not(self::dtb:pagebreak or ancestor::dtb:div[@class='not-in-volume'])][1][self::dtb:pagenum]", NAMESPACE);
                } else {
                    s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                    "//" + volumeNode +
                                    "/*[not(ancestor::dtb:div[@class='not-in-volume'])][1]/preceding::dtb:pagenum[1]", NAMESPACE);
                }
                if (s.equals("")){
                    if (mergeUnnumberedPages) {
                        s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                    "//" + volumeNode +
                                    "/*[not(self::dtb:div[@class='not-in-volume'])][1]/preceding::dtb:pagenum[text()][1]", NAMESPACE);
                    } else {
                        s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                    "//" + volumeNode +
                                    "//dtb:pagenum[text() and not(ancestor::dtb:div[@class='not-in-volume'])][1]", NAMESPACE);
                    }
                }
                if (!s.equals("")){
                    firstPrintPage = s;
                    s = XPathUtils.evaluateString(bodyFile.toURL().openStream(),
                                   "//" + volumeNode + "//dtb:pagenum[" +
                                   "text() and not(ancestor::dtb:div[@class='not-in-volume']) and not(following::dtb:pagenum[ancestor::" +
                                   volumeNode + " and text() and not(ancestor::dtb:div[@class='not-in-volume'])])]", NAMESPACE);
                    if (!(s.equals("") || s.equals(firstPrintPage))) {
                        lastPrintPage = s;
                    }
                }
            }

            /**
             * Determine which symbols to display in list of special symbols
             */
            private List<SpecialSymbol> getSpecialSymbols() throws Exception {

                if (specialSymbols == null) {
                    
                    specialSymbols = new ArrayList<SpecialSymbol>();
                    
                    if (!getSpecialSymbolListEnabled()) { return specialSymbols; }
                    if (!(this instanceof PreliminaryVolumeImpl)) { getBodySection(); }
                    
                    boolean firstVolume = false; // TODO

                    String volumeNode = "dtb:volume[@id='" + identifier  + "']";

                    for (SpecialSymbol symbol : specialSymbolList) {

                        switch (symbol.getMode()) {
                            case NEVER:
                                break;
                            case ALWAYS:
                                specialSymbols.add(symbol);
                                break;
                            case FIRST_VOLUME:
                                if (firstVolume) { specialSymbols.add(symbol); }
                                break;
                            case IF_PRESENT_IN_VOLUME:
                                if (!(this instanceof PreliminaryVolumeImpl)) {
                                    switch (symbol.getType()) {
                                        case NOTE_REFERENCE_INDICATOR:
                                            if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:note[@class='footnote' or @class='endnote']", NAMESPACE)) {
                                                specialSymbols.add(symbol);
                                            }
                                            break;
                                        case TRANSCRIBERS_NOTE_INDICATOR:
                                            if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:div[@class='tn']/dtb:note", NAMESPACE)) {
                                                specialSymbols.add(symbol);
                                            }
                                            break;
                                        case ITALIC_INDICATOR:
                                            if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:em[not(@class='reset')]", NAMESPACE)) {
                                                specialSymbols.add(symbol);
                                            }
                                            break;
                                        case BOLDFACE_INDICATOR:
                                            if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:strong[not(@class='reset')]", NAMESPACE)) {
                                                specialSymbols.add(symbol);
                                            }
                                            break;
                                        case ELLIPSIS:
                                            if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" + volumeNode + "//dtb:flag[@class='ellipsis']", NAMESPACE)) {
                                                specialSymbols.add(symbol);
                                            }
                                            break;
                                        case DOUBLE_DASH:
                                            if (XPathUtils.evaluateBoolean(bodyFile.toURL().openStream(),
                                                    "//" +  volumeNode + "//dtb:flag[@class='double-dash']", NAMESPACE)) {
                                                specialSymbols.add(symbol);
                                            }
                                            break;
                                        default:
                                    }
                                }
                                break;
                        }
                    }
                }
                return specialSymbols;
            }
        
            @Override
            public boolean equals(Object object) {
                if (this == object) { return true; }
                if (!(object instanceof XMLImpl)) { return false; }
                try {
                    VolumeImpl that = (VolumeImpl)object;
                    return this.identifier.equals(that.identifier);
                } catch (ClassCastException e) {
                    return false;
                }
            }

            @Override
            public int hashCode() {
                return identifier.hashCode();
            }
        }

        private class PreliminaryVolumeImpl extends VolumeImpl {
            private PreliminaryVolumeImpl(Configuration.Volume settings) {
                super(settings);
            }

            @Override
            public File getBodySection() {
                throw new UnsupportedOperationException("Volume doesn't have a body section");
            }
            
            @Override
            public File getPreliminarySection() throws Exception {
                try {
                    return super.getPreliminarySection();
                } catch (UnsupportedOperationException e) {
                    throw new ConversionException("Preliminary volume must have at least a frontmatter, a table of "
                                                + "contents, a transcriber's notes page, or a list of special symbols");
                }
            }
        }

        private class SplittableVolumeImpl extends VolumeImpl {
            
            private final Configuration.SplittableVolume settings;
            
            private final int min;
            private final int max;
            private final int preferred;
            private final int minLast;
            
            private SplittableVolumeImpl(Configuration.SplittableVolume settings) {
                super(settings);
                this.settings = settings;
                min = settings.getMinVolumeSize();
                max = settings.getMaxVolumeSize();
                preferred = settings.getPreferredVolumeSize();
                minLast = settings.getMinLastVolumeSize();
            }
            
            private int[] extractDocumentOutline() throws Exception {

                logger.info("Extracting document outline");

                String volumeNode = "dtb:volume[@id='" + identifier  + "']";
                
                Document xmlDocument = docBuilder.parse(xmlFile);
                Node root = xmlDocument.getDocumentElement();

                int pageCount =  Integer.parseInt(XPathAPI.eval(root, "count(//" + volumeNode + "//pagenum)").str());
                int[] outline = new int[pageCount];
                NodeIterator iterator;
                int lvl;
                for (int i=0; i<pageCount; i++) {
                    outline[i] = 0;
                    iterator = XPathAPI.selectNodeIterator(root, "//" + volumeNode + "/heading" +
                                        "//*[(self::h1 or self::h2 or self::h3 or " +
                                             "self::h4 or self::h5 or self::h6 or " +
                                             "self::h7 or self::h8 or self::h9 or self::h10) " +
                                         "and not(@dummy) " +
                                         "and count(preceding::pagenum[ancestor::bodymatter])=" + (i+1) +"]");

                    for (Node node = iterator.nextNode(); node != null; node = iterator.nextNode()) {
                        try {
                            lvl = Integer.parseInt(node.getNodeName().substring(5));
                            if (outline[i]==0 && lvl>0) {
                                outline[i] = lvl;
                            } else if (lvl<outline[i]) {
                                outline[i] = lvl;
                            }
                        } catch (Exception e) {}
                    }
                }

                return outline;
            }

            private void split() throws Exception {

                logger.info("Splitting volume in automatic volumes");
                
                final List<Integer> volumeBoundaries = new ArrayList<Integer>();
                final List<String> volumeIds = new ArrayList<String>();

                statusIndicator.start();
                statusIndicator.setStatus("Computing optimal volume sizes...");

                int[] optimalVolumes = computeOptimalVolumes(extractDocumentOutline(), min, max, preferred, minLast);

                statusIndicator.finish(true);
                
                int idx = volumes.indexOf(this);
                volumes.remove(idx);
                for (int i=0; i<optimalVolumes.length; i++) {
                    XMLImpl.VolumeImpl volume = new VolumeImpl(settings);
                    volumes.add(idx+i, volume);
                    volumeBoundaries.add(Math.max(1,optimalVolumes[i]+1));
                    volumeIds.add(volume.identifier);
                }

                XSLTransformer autoVolumesXSL = new XSLTransformer("auto-volumes") {
                    @Override
                    public void convert(File input, File output) throws ConversionException {
                        super.setParameter("paramAutoVolumeBoundaries", volumeBoundaries.toArray(new Integer[volumeBoundaries.size()]));
                        super.setParameter("paramAutoVolumeIds",        volumeIds.toArray(new String[volumeIds.size()]));
                        super.convert(input, output);
                    }
                };

                autoVolumesXSL.convert(xmlFile);
                
            }
        }
    }

    private static int[] computeOptimalVolumes(int[] pages,
                                               int min,
                                               int max,
                                               int preferred,
                                               int minLast) {

        int total = Math.max(1,pages.length);
        int[] weigths = new int[] { 512, 0, 1, 2, 4, 8, 16, 32, 64, 128, 256 };
        Map<Integer, List<Integer>> optimalpartitions = new TreeMap<Integer, List<Integer>>();
        int[] minerror1 = new int[total+1];
        int[] minerror2 = new int[total+1];
        boolean[] ok = new boolean[total+1];
        int previouspage;
        int currentpage;
        List<Integer> previouspartition;
        List<Integer> currentpartition;

        max =   Math.max(1,max);
        min =   Math.max(1,Math.min(min,max));
        preferred = Math.max(min, Math.min(max, preferred));
        int maxMinLast = Math.min(min, total);
        int lower = max;
        int upper = 2*min;
        while (true) {
            if (total <= lower) {
                break;
            } else if (total < upper) {
                maxMinLast = total - lower;
                break;
            }
            lower += max;
            upper += min;
        }
        minLast = Math.max(1,Math.min(minLast,maxMinLast));

        for (int i=0; i<=total; i++) {
            ok[i] = false;
            minerror1[i] = Integer.MAX_VALUE;
            minerror2[i] = Integer.MAX_VALUE;
        }
        for (int i=total-minLast; i>=total-max && i>=0; i--) {
            ok[i] = true;
        }
        minerror1[0] = 0;
        minerror2[0] = 0;

        currentpartition = new ArrayList<Integer>();
        currentpartition.add(0);
        optimalpartitions.put(0, currentpartition);

        for (int j=0; j<total; j++) {
            if (optimalpartitions.containsKey(j)) {
                previouspartition = optimalpartitions.get(j);
                previouspage = previouspartition.get(previouspartition.size()-1);
                for (int i=min; i<max; i++) {
                    currentpage = previouspage + i;
                    if (currentpage >= total) { break; }
                    currentpartition = new ArrayList<Integer>(previouspartition);
                    currentpartition.add(currentpage);
                    int e1 = minerror1[previouspage] + weigths[pages[currentpage]];
                    int e2 = minerror2[previouspage] + Math.abs(i-preferred);
                    if (e1<minerror1[currentpage]) {
                        minerror1[currentpage] = e1;
                        minerror2[currentpage] = Integer.MAX_VALUE;
                        optimalpartitions.put(currentpage, currentpartition);
                    } else if (e1==minerror1[currentpage]) {
                        if (e2<minerror2[currentpage]) {
                            minerror2[currentpage] = e2;
                            optimalpartitions.put(currentpage, currentpartition);
                        }
                    }
                    if (ok[currentpage]) {
                        if (e1<minerror1[total-1]) {
                            minerror1[total-1] = e1;
                            minerror2[total-1] = Integer.MAX_VALUE;
                            optimalpartitions.put(total, currentpartition);
                        } else if (e1==minerror1[total-1]) {
                            e2 += Math.abs(total-currentpage-preferred);
                            if (e2<minerror2[total-1]) {
                                minerror2[total-1] = e2;
                                optimalpartitions.put(total, currentpartition);
                            }
                        }
                    }
                }
            }
        }

        if (optimalpartitions.containsKey(total)) {
            List<Integer> optimalpartition = optimalpartitions.get(total);
            int[] r = new int[optimalpartition.size()];
            int j = 0;
            for (int i : optimalpartition) {
                r[j] = i;
                j++;
            }
            return r;
        } else {
            return new int[]{0}; // throw Exception?
        }
    }
    
    private static boolean isWhiteSpaceOnlyTextNode(Node node) {

        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getNodeValue();
            if (text.startsWith(" ") || text.startsWith("\n ")) {
                return true;
            }
        }

        return false;
    }
    
    private static String capitalizeFirstLetter(String in) {
        return in.substring(0,1).toUpperCase() + in.substring(1);
    }
    
    private final static Set<String> uniqueIDs = new HashSet<String>();

    private static String createUniqueIdentifier() {

        Random random = new Random();
        String s = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<4; i++) {
            int index = random.nextInt(36);
            builder.append(s.charAt(index));
        }
        String id = builder.toString();
        if (uniqueIDs.contains(id)) {
            return createUniqueIdentifier();
        } else {
            uniqueIDs.add(id);
            return id;
        }
    }
}
