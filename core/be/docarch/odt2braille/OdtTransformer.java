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

import java.io.File;
import java.io.ByteArrayInputStream;
import java.util.TreeMap;
import java.util.Map;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.ZipFile;
import java.util.regex.Pattern;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.OutputKeys;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.saxon.TransformerFactoryImpl;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.apache.xpath.XPathAPI;

import java.io.IOException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import be.docarch.odt2braille.setup.SpecialSymbol;
import be.docarch.odt2braille.setup.NoteReferenceFormat;
import be.docarch.odt2braille.setup.TranslationTable;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;
import be.docarch.odt2braille.setup.SettingMap;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.ParagraphStyle;
import be.docarch.odt2braille.setup.style.HeadingStyle;
import be.docarch.odt2braille.setup.style.TableStyle;
import be.docarch.odt2braille.setup.style.FootnoteStyle;
import be.docarch.odt2braille.setup.style.PictureStyle;
import be.docarch.odt2braille.setup.style.CharacterStyle;
import be.docarch.odt2braille.setup.style.Style.FollowPrint;

/**
 * This class enables you to transform a flat .odt file to a DAISY-like xml file that is suited to be processed by <code>liblouisxml</code>.
 * Note that, although it looks like a DAISY xml file, it is not, and would not validate.
 *
 * The actual transformation is done with XSLT.
 * In addition to XSLT, <code>OdtTransformer</code> also has a supporting {@link #preProcessing} method.
 * Thanks to this preprocessing the XSL transform is considerably simplified.
 * On the other hand, <code>preProcessing</code> is rather slow because it uses DOM (performance should be improved in the future).
 *
 * @see <a href="http://www.daisy.org/z3986/2005/Z3986-2005.html">DAISY xml specification</a>
 * @author Bert Frees
 */
public class OdtTransformer {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();
    private static final String XSLT = Constants.XSLT_PATH;
    private static final String L10N = Constants.L10N_PATH;

    private static NamespaceContext namespace = null;
    private TransformerFactoryImpl tFactory = null;

    private StatusIndicator statusIndicator = null;
    private Configuration settings = null;

    private ZipFile zip = null;
    private DocumentBuilder docBuilder = null;
    private Document contentDoc = null;
    private Document stylesDoc = null;
    private Document metaDoc = null;
    private File odtContentFile = null;
    private File odtStylesFile = null;
    private File odtMetaFile = null;
    private File odtSettingsFile = null;
    private File controllerFile = null;
    private File daisyFile = null;
    private File usedStylesFile = null;
    private File usedLanguagesFile = null;
    private Locale odtLocale = null;
    private Locale oooLocale = null;

    private boolean documentSaved = false;
    private boolean documentParsed = false;

    private boolean paginationDone = false;
    private boolean headingNumberingDone = false;
    private boolean listNumberingDone = false;
    private boolean correctionDone = false;
    private boolean metaDataReferencesDone = false;
    private boolean splitInAutomaticVolumesDone = false;
    private boolean transformDone = false;

    private static String L10N_in = null;
    private static String L10N_and = null;
    private static String L10N_volume = null;
    private static String L10N_volumes = null;
    private static String L10N_supplement = null;
    private static String L10N_supplements = null;
    private static String L10N_preliminary = null;
    private static String L10N_braillePages = null;
    private static String L10N_printPages = null;
    private static String L10N_transcriptionInfo = null;

    private ListStyleProperties outlineProperties = null;
    private ListNumber outlineNumber = null;
    private ListNumber outlineNumberFrame = null;
    private Map<String,ListStyleProperties> listProperties = new TreeMap<String,ListStyleProperties>();
    private Map<String,ListNumber> listNumber = new TreeMap<String,ListNumber>();
    private Map<String,ListNumber> listNumberFrame = new TreeMap<String,ListNumber>();
    private Map<String,String> linkedLists = new TreeMap<String,String>();
    private Map<String,String> listStyles = new TreeMap<String,String>();
    private ListNumber currentNumber = null;
    private Map<String,ParagraphStyle> automaticParagraphStyles = new TreeMap<String,ParagraphStyle>();

    public OdtTransformer(File odtFile)
                   throws IOException,
                          TransformerConfigurationException,
                          TransformerException,
                          ParserConfigurationException {
        
        this (odtFile, null);
    }

    /**
     * Creates a new <code>OdtTransformer</code> instance.
     *
     * @param odtFile           The .odt file.
     * @param statusIndicator   The <code>StatusIndicator</code> that will be used.
     * @param oooLocale         The <code>Locale</code> for the user interface.
     */
    public OdtTransformer(File odtFile,
                          StatusIndicator statusIndicator)
                   throws IOException,
                          TransformerConfigurationException,
                          TransformerException,
                          ParserConfigurationException {

        logger.entering("OdtTransformer","<init>");

        this.statusIndicator = statusIndicator;
        if (statusIndicator != null) {
            oooLocale = statusIndicator.getPreferredLocale();
        }

        tFactory = new net.sf.saxon.TransformerFactoryImpl();

        // Convert ODT to XML files

        odtContentFile = File.createTempFile(TMP_NAME, ".odt.content.xml", TMP_DIR);
        odtContentFile.deleteOnExit();
        odtStylesFile = File.createTempFile(TMP_NAME, ".odt.styles.xml", TMP_DIR);
        odtStylesFile.deleteOnExit();
        odtMetaFile = File.createTempFile(TMP_NAME, ".odt.meta.xml", TMP_DIR);
        odtMetaFile.deleteOnExit();
      //odtSettingsFile = File.createTempFile(TMP_PREFIX, ".odt.settings.xml", TMP_DIR);
      //odtSettingsFile.deleteOnExit();

        zip = new ZipFile(odtFile.getAbsolutePath());
        OdtUtils.getFileFromZip(zip, "content.xml",  odtContentFile);
        OdtUtils.getFileFromZip(zip, "styles.xml",   odtStylesFile);
        OdtUtils.getFileFromZip(zip, "meta.xml",     odtMetaFile);
      //OdtUtils.getFileFromZip(zip, "settings.xml", odtSettingsFile);

        // Syncing of content file and DOM-document
        documentSaved = true;
        documentParsed = false;

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

        // Locale

        namespace = new NamespaceContext();
        odtLocale = new Locale(XPathUtils.evaluateString(odtStylesFile.toURL().openStream(),
                "//office:styles/style:default-style/style:text-properties/@fo:language",namespace).toLowerCase(),
                               XPathUtils.evaluateString(odtStylesFile.toURL().openStream(),
                "//office:styles/style:default-style/style:text-properties/@fo:country", namespace).toUpperCase());

        // L10N

        ResourceBundle bundle = ResourceBundle.getBundle(L10N, odtLocale);

        L10N_in = bundle.getString("in");
        L10N_and = bundle.getString("and");
        L10N_volume = bundle.getString("volume");
        L10N_volumes = bundle.getString("volumes");
        L10N_supplement = bundle.getString("supplement");
        L10N_supplements = bundle.getString("supplements");
        L10N_preliminary = bundle.getString("preliminary");
        L10N_braillePages = bundle.getString("braillePages");
        L10N_printPages = bundle.getString("printPages");
        L10N_transcriptionInfo = bundle.getString("transcriptionInfo");

        logger.exiting("OdtTransformer","<init>");

    }

    public boolean configure(Configuration settings) {
        
        if (this.settings == null) {
            this.settings = settings;
            settings.lock();
            return true;
        }
        return false;
    }

    private void parseDocument() throws IOException,
                                        SAXException {
        if (!documentParsed) {

            contentDoc = docBuilder.parse(odtContentFile.getAbsolutePath());
            stylesDoc = docBuilder.parse(odtStylesFile.getAbsolutePath());
            metaDoc = docBuilder.parse(odtMetaFile.getAbsolutePath());

            documentParsed = true;
            documentSaved = true;
        }
    }

    private void saveDocument() {

        if (!documentSaved) {

            if (contentDoc != null) {
                OdtUtils.saveDOM(contentDoc, odtContentFile);
            }
            documentParsed = true;
            documentSaved = true;
        }
    }

    public void ensureMetadataReferences() throws TransformerConfigurationException,
                                                  TransformerException {

        if (!metaDataReferencesDone) {

            logger.entering("OdtTransformer","ensureMetadataReferences");

            Transformer ensureReferencesXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(
                        XSLT + "ensure-references.xsl").toString()));

            ensureReferencesXSL.setParameter("styles-url", odtStylesFile.toURI());

            ensureReferencesXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            ensureReferencesXSL.setOutputProperty(OutputKeys.METHOD, "xml");
            ensureReferencesXSL.setOutputProperty(OutputKeys.INDENT, "no");

            File tempFile = new File(odtContentFile.getAbsoluteFile() + ".temp");

            saveDocument();
            ensureReferencesXSL.transform(new StreamSource(odtContentFile), new StreamResult(tempFile));
            documentParsed = false;

            odtContentFile.delete();
            tempFile.renameTo(odtContentFile);

            metaDataReferencesDone = true;

            logger.entering("OdtTransformer","ensureMetadataReferences");
        }
    }
  
    public boolean makeControlFlow() throws IOException,
                                            TransformerConfigurationException,
                                            TransformerException,
                                            ConversionException {

        if (settings == null) { throw new ConversionException(); }

        if (controllerFile == null) {

            logger.entering("OdtTransformer","makeControlFlow");

            controllerFile = File.createTempFile(TMP_NAME, ".controller.rdf.xml", TMP_DIR);
            controllerFile.deleteOnExit();

            // Dependencies
            // ensureMetadataReferences();

            Transformer controllerXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(
                        XSLT + "controller.xsl").toString()));

            String frontMatter = settings.getFrontMatterSection();
            String repeatFrontMatter = settings.getRepeatFrontMatterSection();
            String titlePage = settings.getTitlePageSection();
            String rearMatter = settings.getRearMatterSection();
            List<String> volumeSections = new ArrayList<String>();
            for (Configuration.SectionVolume volume : settings.getSectionVolumeList().values()) {
                volumeSections.add(volume.getSection());
            }

            controllerXSL.setParameter("styles-url",                    odtStylesFile.toURI());
            controllerXSL.setParameter("paramFrontMatterSection",       (frontMatter==null)?"":frontMatter);
            controllerXSL.setParameter("paramRepeatFrontMatterSection", (repeatFrontMatter==null)?"":repeatFrontMatter);
            controllerXSL.setParameter("paramTitlePageSection",         (titlePage==null)?"":titlePage);
            controllerXSL.setParameter("paramRearMatterSection",        (rearMatter==null)?"":rearMatter);
            controllerXSL.setParameter("paramVolumeSections",           volumeSections.toArray(new String[volumeSections.size()]));

            controllerXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            controllerXSL.setOutputProperty(OutputKeys.METHOD, "xml");
            controllerXSL.setOutputProperty(OutputKeys.INDENT, "yes");
            controllerXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

            saveDocument();
            controllerXSL.transform(new StreamSource(odtContentFile), new StreamResult(controllerFile));

            logger.exiting("OdtTransformer","makeControlFlow");
        }

        return true;
    }

    private void correctionProcessing() throws SAXException,
                                               IOException,
                                               TransformerConfigurationException,
                                               TransformerException {
        if (!correctionDone) {

            logger.entering("OdtTransformer","correctionProcessing");

            parseDocument();

            Element contentRoot = contentDoc.getDocumentElement();

            OdtUtils.replaceObjectContent(docBuilder, contentDoc, zip);
            OdtUtils.removeEmptyHeadings(contentRoot);
          //OdtUtils.normalizeTextS(contentDoc);
          //OdtUtils.removeEmptyParagraphs(contentRoot);
          //OdtUtils.insertEmptyParaForHeadings(contentDoc);

            documentSaved = false;
            correctionDone = true;

            logger.exiting("OdtTransformer","correctionProcessing");
        }
    }


    private void paginationProcessing() throws IOException,
                                               SAXException,
                                               TransformerConfigurationException,
                                               TransformerException,
                                               ConversionException {

        // TODO: speed up

        if (settings == null) { throw new ConversionException(); }

        if (!paginationDone) {

            logger.entering("OdtTransformer","paginationProcessing");

            parseDocument();

            Element contentRoot = contentDoc.getDocumentElement();
            Element stylesRoot = stylesDoc.getDocumentElement();
            Element metaRoot = metaDoc.getDocumentElement();

            Node firstNode = XPathAPI.selectSingleNode(contentRoot, "//body/text/sequence-decls/following-sibling::*[1]");
            if (firstNode != null) {
                if (statusIndicator != null) {
                    statusIndicator.start();
                    statusIndicator.setSteps(Integer.parseInt(XPathAPI.eval(metaRoot, "//meta/document-statistic/@page-count").str()));
                    statusIndicator.setStatus(ResourceBundle.getBundle(L10N, oooLocale).getString("statusIndicatorStep1"));
                }
                insertPagination(contentRoot, stylesRoot, firstNode, 0, "Standard", true);
                if (statusIndicator != null) {
                    statusIndicator.finish(true);
                    statusIndicator.close();
                }
            }

            documentSaved = false;
            paginationDone = true;

            logger.exiting("OdtTransformer","paginationProcessing");
        }
    }

    private void headingNumberingProcessing() throws IOException,
                                                     SAXException,
                                                     TransformerConfigurationException,
                                                     TransformerException,
                                                     ConversionException {

        if (settings == null) { throw new ConversionException(); }

        if (!headingNumberingDone) {

            logger.entering("OdtTransformer","headingNumberingProcessing");

            parseDocument();

            Element contentRoot = contentDoc.getDocumentElement();
            Element stylesRoot = stylesDoc.getDocumentElement();

            Node firstNode = XPathAPI.selectSingleNode(contentRoot, "//body/text/sequence-decls/following::h[1]");
            if (firstNode != null) {
                if (statusIndicator != null) {
                    statusIndicator.start();
                    statusIndicator.setSteps(Integer.parseInt(XPathAPI.eval(contentRoot, "count(//body/text//h)").str()));
                    statusIndicator.setStatus(ResourceBundle.getBundle(L10N, oooLocale).getString("statusIndicatorStep2"));
                }
                insertHeadingNumbering(contentRoot, stylesRoot, firstNode, true);
                if (statusIndicator != null) {
                    statusIndicator.finish(true);
                    statusIndicator.close();
                }
            }

            documentSaved = false;
            headingNumberingDone = true;

            logger.exiting("OdtTransformer","headingNumberingProcessing");
        }
    }

    private void listNumberingProcessing() throws IOException,
                                                  SAXException,
                                                  TransformerConfigurationException,
                                                  TransformerException,
                                                  ConversionException {

        if (settings == null) { throw new ConversionException(); }

        if (!listNumberingDone) {

            logger.entering("OdtTransformer","listNumberingProcessing");

            parseDocument();

            Element contentRoot = contentDoc.getDocumentElement();
            Element stylesRoot = stylesDoc.getDocumentElement();

            Node firstNode = XPathAPI.selectSingleNode(contentRoot, "//body/text/sequence-decls/following::list[@id][1]");
            if (firstNode != null) {
                if (statusIndicator != null) {
                    statusIndicator.start();
                    statusIndicator.setSteps(Integer.parseInt(XPathAPI.eval(contentRoot, "count(//body/text//list[@id])").str()));
                    statusIndicator.setStatus(ResourceBundle.getBundle(L10N, oooLocale).getString("statusIndicatorStep3"));
                    }
                insertListNumbering(contentRoot, stylesRoot, firstNode, 0, true);
                if (statusIndicator != null) {
                    statusIndicator.finish(true);
                    statusIndicator.close();
                }
            }

            documentSaved = false;
            listNumberingDone = true;

            logger.exiting("OdtTransformer","listNumberingProcessing");
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
    private Object[] insertPagination(Node contentRoot,
                                      Node stylesRoot,
                                      Node node,
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
            ParagraphStyle style = settings.getParagraphStyles().get(styleName);
            if (style == null) { style = automaticParagraphStyles.get(styleName); }
            if (style != null) { style = style.getNonAutomaticStyle(); }
            if (style != null) { followPrint = (style.getHardPageBreaks() == ParagraphStyle.FollowPrint.FOLLOW_PRINT); }

        } else if (nodeName.equals("text:h")) {

            styleName = node.getAttributes().getNamedItem("text:style-name").getNodeValue();
            int level = Integer.parseInt(node.getAttributes().getNamedItem("text:outline-level").getNodeValue());
            HeadingStyle style = settings.getHeadingStyles().get(level);
            if (style != null) { newBraillePage = style.getNewBraillePage(); }
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

                if (enumType.equals("i")) {
                    value = RomanNumbering.toRoman(pagenum + offset);
                } else if (enumType.equals("I")) {
                    value = RomanNumbering.toRoman(pagenum + offset).toUpperCase();
                } else if (enumType.equals("a")) {
                    value = LetterNumbering.toLetter(pagenum + offset);
                } else if (enumType.equals("A")) {
                    value = LetterNumbering.toLetter(pagenum + offset).toUpperCase();
                } else {
                    value = String.valueOf(pagenum + offset);
                }

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

            if (statusIndicator != null) {
                statusIndicator.increment();
            }
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
                        ret = insertPagination(contentRoot, stylesRoot, child, pagenum, masterPageName, true);
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
                        ret = insertPagination(contentRoot, stylesRoot, child, pagenum, masterPageName, false);
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
                ret = insertPagination(contentRoot, stylesRoot, softPageBreakDescendants.item(i), pagenum, masterPageName, false);
                pagenum = (Integer)ret[0];
                masterPageName = (String)ret[1];
            }
        }

        // Process all siblings

        if (isFirst) {

            Node next = node.getNextSibling();
            while(next != null) {
                if (next.getNodeType() == Node.ELEMENT_NODE) {
                    ret = insertPagination(contentRoot, stylesRoot, next, pagenum, masterPageName, false);
                    pagenum = (Integer)ret[0];
                    masterPageName = (String)ret[1];
                }
                next = next.getNextSibling();
            }

        }
            
        // Return statement

        return new Object[]{pagenum,masterPageName};

    }

    /**
     * Insert a special <code>num</code> tag before each numbered or bulleted heading.
     * This method is called recursively so that each heading element is eventually processed once.
     *
     * @param  root     The root element of the .odt document.
     * @param  node     The next node to be processed. On the first call, this parameter is irrelevant.
     * @param  init     Should be set to <code>true</code> on the first call.
     */
    private void insertHeadingNumbering(Node contentRoot,
                                        Node stylesRoot,
                                        Node node,
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
                insertHeadingNumbering(contentRoot, stylesRoot, next, false);
            }

            // Process all frames of depth = 1

            nodes = XPathAPI.selectNodeList(contentRoot, "//body/text/sequence-decls/following::frame[not(ancestor::frame)]");
            for (int i=0;i<nodes.getLength();i++) {
                next = nodes.item(i);
                insertHeadingNumbering(contentRoot, stylesRoot, next, false);
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
                            } else if (numFormat_i.equals("i")) {
                                display += RomanNumbering.toRoman(num_i);
                            } else if (numFormat_i.equals("I")) {
                                display += RomanNumbering.toRoman(num_i).toUpperCase();
                            } else if (numFormat_i.equals("a")) {
                                display += LetterNumbering.toLetter(num_i);
                            } else if (numFormat_i.equals("A")) {
                                display += LetterNumbering.toLetter(num_i).toUpperCase();
                            } else {
                                display += String.valueOf(num_i);
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
                                    } else if (numFormat_i.equals("i")) {
                                        display += RomanNumbering.toRoman(num_i);
                                    } else if (numFormat_i.equals("I")) {
                                        display += RomanNumbering.toRoman(num_i).toUpperCase();
                                    } else if (numFormat_i.equals("a")) {
                                        display += LetterNumbering.toLetter(num_i);
                                    } else if (numFormat_i.equals("A")) {
                                        display += LetterNumbering.toLetter(num_i).toUpperCase();
                                    } else {
                                        display += String.valueOf(num_i);
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

                        if (statusIndicator != null) {
                            statusIndicator.increment();
                        }
                    }
                }

            } else if (nodeName.equals("draw:frame")) {

                int depth = Integer.parseInt(XPathAPI.eval(node, "count(./ancestor-or-self::frame)").str());

                // Process all heading descendants

                nodes = XPathAPI.selectNodeList(node, "./descendant::h[count(ancestor::frame)=" + depth + "]");
                for (int i=0;i<nodes.getLength();i++) {
                    next = nodes.item(i);
                    insertHeadingNumbering(contentRoot, stylesRoot, next, false);
                }

                // Process all frame descendants

                nodes = XPathAPI.selectNodeList(node, "./descendant::frame[count(ancestor::frame)=" + depth + "]");
                for (int i=0;i<nodes.getLength();i++) {
                    next = nodes.item(i);
                    insertHeadingNumbering(contentRoot, stylesRoot, next, false);
                }
            }
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
    private void insertListNumbering(Node contentRoot,
                                     Node stylesRoot,
                                     Node node,
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
                insertListNumbering(contentRoot, stylesRoot, next, 0, false);
            }

            // Process all frames of depth = 1

            nodes = XPathAPI.selectNodeList(contentRoot, "//body/text/sequence-decls/following::frame[not(ancestor::frame)]");
            for (int i=0;i<nodes.getLength();i++) {
                next = nodes.item(i);
                insertListNumbering(contentRoot, stylesRoot, next, 0, false);
            }

        } else {

            if (nodeName.equals("text:list")) {

                if (XPathAPI.eval(node, "@id").bool()) {

                    // Main list

                    id = XPathAPI.eval(node, "@id").str();

                    level = 1;

                    if (statusIndicator != null) {
                        statusIndicator.increment();
                    }

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
                    insertListNumbering(contentRoot, stylesRoot, child, level, false);
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

                                display = settings.getListStyles().get(newLevel).getPrefix();
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
                                } else if (numFormat_i.equals("i")) {
                                    display += RomanNumbering.toRoman(num_i);
                                } else if (numFormat_i.equals("I")) {
                                    display += RomanNumbering.toRoman(num_i).toUpperCase();
                                } else if (numFormat_i.equals("a")) {
                                    display += LetterNumbering.toLetter(num_i);
                                } else if (numFormat_i.equals("A")) {
                                    display += LetterNumbering.toLetter(num_i).toUpperCase();
                                } else {
                                    display += String.valueOf(num_i);
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
                                        } else if (numFormat_i.equals("i")) {
                                            display += RomanNumbering.toRoman(num_i);
                                        } else if (numFormat_i.equals("I")) {
                                            display += RomanNumbering.toRoman(num_i).toUpperCase();
                                        } else if (numFormat_i.equals("a")) {
                                            display += LetterNumbering.toLetter(num_i);
                                        } else if (numFormat_i.equals("A")) {
                                            display += LetterNumbering.toLetter(num_i).toUpperCase();
                                        } else {
                                            display += String.valueOf(num_i);
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
                            insertListNumbering(contentRoot, stylesRoot, child, newLevel, false);
                        }
                    }
                }

            } else if (nodeName.equals("draw:frame")) {

                int depth = Integer.parseInt(XPathAPI.eval(node, "count(./ancestor-or-self::frame)").str());

                // Process all descendants of type list[@id]

                nodes = XPathAPI.selectNodeList(node, "./descendant::list[@id][count(ancestor::frame)=" + depth + "]");
                for (int i=0;i<nodes.getLength();i++) {
                    next = nodes.item(i);
                    insertListNumbering(contentRoot, stylesRoot, next, level, false);
                }

                // Process all frame descendants

                nodes = XPathAPI.selectNodeList(node, "./descendant::frame[count(ancestor::frame)=" + depth + "]");
                for (int i=0;i<nodes.getLength();i++) {
                    next = nodes.item(i);
                    insertListNumbering(contentRoot, stylesRoot, next, 0, false);
                }
            }
        }
    }

    public boolean getBodyMatter(File saveToFile)
                          throws IOException,
                                 SAXException,
                                 TransformerConfigurationException,
                                 TransformerException,
                                 ConversionException {

        // Dependencies
        if (settings == null || !transformDone) { throw new ConversionException(); }

        logger.entering("OdtTransformer","getBodyMatter");

        // Create transformer

        Transformer splitVolumesXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(XSLT + "split-volumes.xsl").toString()));

        // Set parameters

        splitVolumesXSL.setParameter("paramFrontMatterEnabled", false);
        splitVolumesXSL.setParameter("paramBodyMatterEnabled", true);
        splitVolumesXSL.setParameter("paramRearMatterEnabled", true);
        splitVolumesXSL.setParameter("paramAllVolumes", true);
        splitVolumesXSL.setParameter("paramNoteSectionTitle", "NOTES");
        splitVolumesXSL.setParameter("paramContinuedHeadingSuffix", settings.continuedSuffix);
        
        // Set output options

        splitVolumesXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        splitVolumesXSL.setOutputProperty(OutputKeys.METHOD, "xml");
        splitVolumesXSL.setOutputProperty(OutputKeys.INDENT, "yes");
        splitVolumesXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

        // Transform

        splitVolumesXSL.transform(new StreamSource(daisyFile), new StreamResult(saveToFile));

        logger.exiting("OdtTransformer","getBodyMatter");

        return true;
    }

    public boolean getFrontMatter(File saveToFile,
                                  Volume volume,
                                  String volumeInfo)
                           throws IOException,
                                  SAXException,
                                  TransformerConfigurationException,
                                  TransformerException,
                                  ConversionException {

        // Dependencies
        if (settings == null || !transformDone) { throw new ConversionException(); }

        logger.entering("OdtTransformer","getFrontMatter");

        List<String> specialSymbols = new ArrayList<String>();
        List<String> specialSymbolsDots = new ArrayList<String>();
        List<String> specialSymbolsDescription = new ArrayList<String>();
      //List<String> transcribersNotes = volume.getTranscribersNotes();

        int firstBraillePage = volume.getFirstBraillePage();
        int lastBraillePage = volume.getLastBraillePage();
        int numberOfPreliminaryPages = volume.getNumberOfPreliminaryPages();
        String firstPrintPage = volume.getFirstPrintPage();
        String lastPrintPage = volume.getLastPrintPage();
        String braillePages = "";
        String printPages = "";

        if (settings.getBraillePageNumbers())         {  braillePages  = L10N_braillePages + " ";
        if (numberOfPreliminaryPages > 0)             {
        if (settings.getPreliminaryPageNumberFormat()
                == PageNumberFormat.P)                {
        if (numberOfPreliminaryPages > 1)             {  braillePages += "p1-";                                            }
                                                         braillePages += "p" + numberOfPreliminaryPages ;                  }
        if (settings.getPreliminaryPageNumberFormat()
                == PageNumberFormat.ROMAN)            {
        if (numberOfPreliminaryPages > 1)             {  braillePages += "i-";                                             }
                                                         braillePages += RomanNumbering.toRoman(numberOfPreliminaryPages); }
        if (settings.getPreliminaryPageNumberFormat()
                == PageNumberFormat.ROMANCAPS)        {
        if (numberOfPreliminaryPages > 1)             {  braillePages += "I-";                                             }
                                                         braillePages += RomanNumbering.toRoman(numberOfPreliminaryPages)
                                                                                       .toUpperCase();                     }}
        if (numberOfPreliminaryPages > 0 &&
                lastBraillePage >= firstBraillePage)  {  braillePages += " " + L10N_and.toLowerCase() + " ";               }
        if (lastBraillePage >= firstBraillePage)      {
        if (lastBraillePage > firstBraillePage)       {  braillePages += firstBraillePage + "-";                           }
                                                         braillePages += lastBraillePage;                                  }}        
        if (settings.getPrintPageNumbers())           {
        if (firstPrintPage != null)                   {  printPages  = L10N_printPages + " " + firstPrintPage;
        if (lastPrintPage != null)                    {  printPages += "-" + lastPrintPage;                                }}}


        volumeInfo = volumeInfo.replaceFirst("@title", volume.getTitle())
                               .replaceFirst("@pages", braillePages + (printPages.length()>0?"\n":"") + printPages);
        String transcriptionInfo = L10N_transcriptionInfo.replaceFirst("@creator", settings.getCreator())
                                                         .replaceFirst("@date",    settings.date);

        if (volume.getSpecialSymbolListEnabled()) {

            for (SpecialSymbol symbol : volume.getSpecialSymbols()) {
                specialSymbols.add(symbol.getSymbol());
                specialSymbolsDots.add("(" + symbol.getDotPattern() + ")");
                specialSymbolsDescription.add(symbol.getDescription());
            }
        }

        // Create transformer

        Transformer splitVolumesXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(XSLT + "split-volumes.xsl").toString()));

        // Set parameters

        splitVolumesXSL.setParameter("paramBodyMatterEnabled", false);
        splitVolumesXSL.setParameter("paramRearMatterEnabled", false);
        splitVolumesXSL.setParameter("paramAllVolumes", false);
        splitVolumesXSL.setParameter("paramVolumeId", (volume instanceof PreliminaryVolume) ? "" : volume.getIdentifier());
        splitVolumesXSL.setParameter("paramFrontMatterEnabled", volume.getFrontMatter());
        splitVolumesXSL.setParameter("paramTableOfContentEnabled", volume.getTableOfContent());
        splitVolumesXSL.setParameter("paramSpecialSymbolsListEnabled", volume.getSpecialSymbolListEnabled());
        splitVolumesXSL.setParameter("paramTNPageEnabled", volume.getTranscribersNotesPageEnabled());
        splitVolumesXSL.setParameter("paramExtendedFront", volume.getExtendedFrontMatter());
        splitVolumesXSL.setParameter("paramExtendedToc", volume.getExtendedTableOfContent());
        splitVolumesXSL.setParameter("paramVolumeInfoEnabled", settings.getVolumeInfoEnabled());
        splitVolumesXSL.setParameter("paramTranscriptionInfoEnabled", settings.getTranscriptionInfoEnabled());
        splitVolumesXSL.setParameter("paramTranscriptionInfoLine", transcriptionInfo);
        splitVolumesXSL.setParameter("paramVolumeInfoLines", volumeInfo.split("\n"));
        splitVolumesXSL.setParameter("paramTableOfContentTitle", settings.getTocStyle().getTitle());
        splitVolumesXSL.setParameter("paramTNPageTitle",settings.getTranscribersNotesPageTitle());
        splitVolumesXSL.setParameter("paramTranscribersNotes", new String[0]);
        splitVolumesXSL.setParameter("paramSpecialSymbolsListTitle", settings.getSpecialSymbolListTitle());
        splitVolumesXSL.setParameter("paramSpecialSymbols", specialSymbols.toArray(new String[specialSymbols.size()]));
        splitVolumesXSL.setParameter("paramSpecialSymbolsDots", specialSymbolsDots.toArray(new String[specialSymbolsDots.size()]));
        splitVolumesXSL.setParameter("paramSpecialSymbolsDescription", specialSymbolsDescription.toArray(new String[specialSymbolsDescription.size()]));
        splitVolumesXSL.setParameter("paramNoteSectionTitle", settings.getEndNotesPageTitle());
        splitVolumesXSL.setParameter("paramContinuedHeadingSuffix", settings.continuedSuffix);

        // Set output options

        splitVolumesXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        splitVolumesXSL.setOutputProperty(OutputKeys.METHOD, "xml");
        splitVolumesXSL.setOutputProperty(OutputKeys.INDENT, "yes");
        splitVolumesXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

        // Transform

        splitVolumesXSL.transform(new StreamSource(daisyFile), new StreamResult(saveToFile));

        logger.exiting("OdtTransformer","getFrontMatter");

        return true;
    }

    public boolean splitInAutomaticVolumes(List<AutoGeneratedVolume> autoGeneratedVolumes)
                                   throws IOException,
                                          SAXException,
                                          TransformerException,
                                          TransformerConfigurationException,
                                          ConversionException {
        // Dependencies
        if (!transformDone) { throw new ConversionException(); }

        if (!splitInAutomaticVolumesDone) {

            logger.entering("OdtTransformer","splitInAutomaticVolumes");

            List<Integer> volumeBoundaries = new ArrayList<Integer>();
            List<String> volumeIds = new ArrayList<String>();
            for (AutoGeneratedVolume volume : autoGeneratedVolumes) {
                volumeBoundaries.add(volume.getStartPage());
                volumeIds.add(volume.getIdentifier());
            }

            Transformer autoVolumesXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(XSLT + "auto-volumes.xsl").toString()));

            autoVolumesXSL.setParameter("paramAutoVolumeBoundaries", volumeBoundaries.toArray(new Integer[volumeBoundaries.size()]));
            autoVolumesXSL.setParameter("paramAutoVolumeIds",        volumeIds.toArray(new String[volumeIds.size()]));

            autoVolumesXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            autoVolumesXSL.setOutputProperty(OutputKeys.METHOD, "xml");
            autoVolumesXSL.setOutputProperty(OutputKeys.INDENT, "yes");
            autoVolumesXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

            File tempFile = new File(daisyFile.getAbsoluteFile() + ".temp");
            autoVolumesXSL.transform(new StreamSource(daisyFile), new StreamResult(tempFile));
            daisyFile.delete();
            tempFile.renameTo(daisyFile);

            logger.exiting("OdtTransformer","splitInAutomaticVolumes");

            splitInAutomaticVolumesDone = true;
            return true;
        }

        return false;
    }

    public boolean transform(Volume singleBodyVolume,
                             Volume singleRearVolume,
                             Map<String,Volume> manualVolumes)
                      throws IOException,
                             SAXException,
                             TransformerConfigurationException,
                             TransformerException,
                             ConversionException {

        if (settings == null) { throw new ConversionException(); }

        if (!transformDone || daisyFile == null) {

            logger.entering("OdtTransformer","transform");

            daisyFile = File.createTempFile(TMP_NAME, ".daisy.xml", TMP_DIR);
            daisyFile.deleteOnExit();

            // Dependencies
            paginationProcessing();
            headingNumberingProcessing();
            listNumberingProcessing();
            correctionProcessing();
            ensureMetadataReferences();
            makeControlFlow();

            List<String> manualVolumeSections = new ArrayList<String>();
            List<String> manualVolumeIDs = new ArrayList<String>();
            List<String> languages = new ArrayList<String>();
            List<String> translationTables = new ArrayList<String>();
            List<Integer> grades = new ArrayList<Integer>();
            List<Boolean> eightDots = new ArrayList<Boolean>();
            List<String> configuredParagraphStyles = new ArrayList<String>();
            List<String> keepEmptyParagraphStyles = new ArrayList<String>();
            List<String> characterStyles = new ArrayList<String>();
            List<Boolean> boldfaceFollowPrint = new ArrayList<Boolean>();
            List<Boolean> italicFollowPrint = new ArrayList<Boolean>();
            List<Boolean> underlineFollowPrint = new ArrayList<Boolean>();
            List<Boolean> capsFollowPrint = new ArrayList<Boolean>();
            List<Boolean> headingUpperBorder = new ArrayList<Boolean>();
            List<Boolean> headingLowerBorder = new ArrayList<Boolean>();
            List<String> noterefNumberPrefixes = new ArrayList<String>();
            List<String> noterefNumberFormats = new ArrayList<String>();

            FootnoteStyle footnoteStyle = settings.getFootnoteStyle();
            TableStyle tableStyle = settings.getTableStyles().get("Default");
            PictureStyle pictureStyle = settings.getPictureStyle();
            TocStyle tocStyle = settings.getTocStyle();

            for (String section : manualVolumes.keySet()) {
                manualVolumeSections.add(section);
                manualVolumeIDs.add(manualVolumes.get(section).getIdentifier());
            }

            for (ParagraphStyle style : settings.getParagraphStyles().values()) {
                if (!style.getInherit()) {
                    configuredParagraphStyles.add(style.getID());
                    if (style.getEmptyParagraphs() == ParagraphStyle.FollowPrint.FOLLOW_PRINT) {
                        keepEmptyParagraphStyles.add(style.getID());
                    }
                }
            }

            for (Locale locale : settings.getTranslationTables().keys()) {
            
                TranslationTable t = settings.getTranslationTables().get(locale);
            
                languages.add(locale.toString().replaceAll("_", "-"));
                translationTables.add(t.getLocale());
                grades.add(t.getGrade());
                eightDots.add(t.getDots() == TranslationTable.Dots.EIGHTDOTS);
            
            }

            for (CharacterStyle style : settings.getCharacterStyles().values()) {

                characterStyles.add(style.getID());
                boldfaceFollowPrint.add(style.getBoldface() == FollowPrint.FOLLOW_PRINT);
                italicFollowPrint.add(style.getItalic() == FollowPrint.FOLLOW_PRINT);
                underlineFollowPrint.add(style.getUnderline() == FollowPrint.FOLLOW_PRINT);
                capsFollowPrint.add(style.getCapitals() == FollowPrint.FOLLOW_PRINT);

            }

            for (HeadingStyle headStyle : settings.getHeadingStyles().values()) {
                headingUpperBorder.add(headStyle.getUpperBorderEnabled());
                headingLowerBorder.add(headStyle.getLowerBorderEnabled());
            }

            SettingMap<String,NoteReferenceFormat> noterefFormats = settings.getNoteReferenceFormats();
            for (String key : noterefFormats.keys()) {
                noterefNumberFormats.add(key);
                noterefNumberPrefixes.add(noterefFormats.get(key).getPrefix());
            }

            // Create transformers

            Transformer mainXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(XSLT + "main.xsl").toString()));
            Transformer languagesAndTypefaceXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(XSLT + "languages-and-typeface.xsl").toString()));

            // Set parameters
            
            mainXSL.setParameter("styles-url",                     odtStylesFile.toURI());
            mainXSL.setParameter("controller-url",                 controllerFile.toURI());

            mainXSL.setParameter("paramBodyMatterMode",            settings.getBodyMatterMode().name());
            mainXSL.setParameter("paramRearMatterMode",            settings.getRearMatterMode().name());
            mainXSL.setParameter("paramSingleBodyVolumeId",        singleBodyVolume==null?"":singleBodyVolume.getIdentifier());
            mainXSL.setParameter("paramSingleRearVolumeId",        singleRearVolume==null?"":singleRearVolume.getIdentifier());
            mainXSL.setParameter("paramManualVolumeSections",      manualVolumeSections.toArray(new String[manualVolumeSections.size()]));
            mainXSL.setParameter("paramManualVolumeIds",           manualVolumeIDs.toArray(new String[manualVolumeIDs.size()]));
            mainXSL.setParameter("paramHyphenationEnabled",        settings.getHyphenate());
            mainXSL.setParameter("paramNoterefSpaceBefore",        noterefFormats.get("1").getSpaceBefore());
            mainXSL.setParameter("paramNoterefSpaceAfter",         noterefFormats.get("1").getSpaceAfter());
            mainXSL.setParameter("paramNoterefNumberFormats",      noterefNumberFormats.toArray(new String[noterefNumberFormats.size()]));
            mainXSL.setParameter("paramNoterefNumberPrefixes",     noterefNumberPrefixes.toArray(new String[noterefNumberPrefixes.size()]));
            mainXSL.setParameter("paramConfiguredParagraphStyles", configuredParagraphStyles.toArray(new String[configuredParagraphStyles.size()]));
            mainXSL.setParameter("paramKeepEmptyParagraphStyles",  keepEmptyParagraphStyles.toArray(new String[keepEmptyParagraphStyles.size()]));
            mainXSL.setParameter("paramTocUptoLevel",              tocStyle.getEvaluateUptoLevel());
            mainXSL.setParameter("paramTableUpperBorder",          tableStyle.getUpperBorderEnabled());
            mainXSL.setParameter("paramTableLowerBorder",          tableStyle.getLowerBorderEnabled());
            mainXSL.setParameter("paramStairstepTableEnabled",     tableStyle.getStairstepEnabled());
            mainXSL.setParameter("paramColumnDelimiter",           tableStyle.getColumnDelimiter());
            mainXSL.setParameter("paramColumnHeadings",            tableStyle.getColumnHeadings());
            mainXSL.setParameter("paramRowHeadings",               tableStyle.getRowHeadings());
            mainXSL.setParameter("paramTableHeadingSuffix",        tableStyle.getHeadingSuffix());
            mainXSL.setParameter("paramRepeatTableHeading",        tableStyle.getRepeatHeading());
            mainXSL.setParameter("paramMirrorTable",               tableStyle.getMirrorTable());
            mainXSL.setParameter("paramPictureDescriptionPrefix",  pictureStyle.getDescriptionPrefix());
            mainXSL.setParameter("paramPictureOpeningMarkPrefix",  pictureStyle.getOpeningMark());
            mainXSL.setParameter("paramPictureClosingMarkPrefix",  pictureStyle.getClosingMark());

            mainXSL.setParameter("paramFrameUpperBorder",          settings.getFrameStyle().getUpperBorderEnabled());
            mainXSL.setParameter("paramFrameLowerBorder",          settings.getFrameStyle().getLowerBorderEnabled());
            mainXSL.setParameter("paramHeadingUpperBorder",        headingUpperBorder.toArray(new Boolean[headingUpperBorder.size()]));
            mainXSL.setParameter("paramHeadingLowerBorder",        headingLowerBorder.toArray(new Boolean[headingLowerBorder.size()]));

            languagesAndTypefaceXSL.setParameter("paramLanguages",            languages.toArray(new String[languages.size()]));
            languagesAndTypefaceXSL.setParameter("paramTranslationTables",    translationTables.toArray(new String[translationTables.size()]));
            languagesAndTypefaceXSL.setParameter("paramGrades",               grades.toArray(new Integer[grades.size()]));
            languagesAndTypefaceXSL.setParameter("paramEightDots",            eightDots.toArray(new Boolean[eightDots.size()]));
            languagesAndTypefaceXSL.setParameter("paramMathCode",             settings.getMathCode().name().toLowerCase());

            languagesAndTypefaceXSL.setParameter("paramCharacterStyles",      characterStyles.toArray(new String[characterStyles.size()]));
            languagesAndTypefaceXSL.setParameter("paramBoldfaceFollowPrint",  boldfaceFollowPrint.toArray(new Boolean[boldfaceFollowPrint.size()]));
            languagesAndTypefaceXSL.setParameter("paramItalicFollowPrint",    italicFollowPrint.toArray(new Boolean[italicFollowPrint.size()]));
            languagesAndTypefaceXSL.setParameter("paramUnderlineFollowPrint", underlineFollowPrint.toArray(new Boolean[underlineFollowPrint.size()]));
            languagesAndTypefaceXSL.setParameter("paramCapsFollowPrint",      capsFollowPrint.toArray(new Boolean[capsFollowPrint.size()]));

            // Set output properties

            mainXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            mainXSL.setOutputProperty(OutputKeys.METHOD, "xml");
            mainXSL.setOutputProperty(OutputKeys.INDENT, "yes");
            mainXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

            languagesAndTypefaceXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            languagesAndTypefaceXSL.setOutputProperty(OutputKeys.METHOD, "xml");
            languagesAndTypefaceXSL.setOutputProperty(OutputKeys.INDENT, "yes");
            languagesAndTypefaceXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

            // Transform

            File tempFile = new File(daisyFile.getAbsoluteFile() + ".temp");

            saveDocument();
            mainXSL.transform(new StreamSource(odtContentFile), new StreamResult(tempFile));

            languagesAndTypefaceXSL.transform(new StreamSource(tempFile), new StreamResult(daisyFile));
            tempFile.delete();

            logger.exiting("OdtTransformer","transform");

            transformDone = true;
            return true;
        }

        return false;
    }

    public String[] extractLanguages() {

        logger.entering("OdtTransformer","extractLanguages");

        String[] languages = new String[0];

        try {

            if (usedLanguagesFile == null) {

                usedLanguagesFile = File.createTempFile(TMP_NAME, ".languages.xml", TMP_DIR);
                usedLanguagesFile.deleteOnExit();

                Transformer languagesXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(XSLT + "get-languages.xsl").toString()));

                languagesXSL.setParameter("styles-url", odtStylesFile.toURI());

                languagesXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                languagesXSL.setOutputProperty(OutputKeys.METHOD, "xml");
                languagesXSL.setOutputProperty(OutputKeys.INDENT, "yes");
                languagesXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

                saveDocument();
                languagesXSL.transform(new StreamSource(odtContentFile), new StreamResult(usedLanguagesFile));

            }

            languages = new String[XPathUtils.evaluateNumber(usedLanguagesFile.toURL().openStream(), "count(/o2b:languages/o2b:language)", namespace).intValue()];
            languages[0] = XPathUtils.evaluateString(usedLanguagesFile.toURL().openStream(),
                           "/o2b:languages/o2b:language[@class='main'][1]/@name", namespace);
            for (int i=1; i<languages.length; i++) {
                languages[i] = XPathUtils.evaluateString(usedLanguagesFile.toURL().openStream(),
                               "/o2b:languages/o2b:language[not(@class='main')][" + i + "]/@name", namespace);
            }

        } catch (TransformerException e) {
        } catch (IOException e) {
        }

        logger.exiting("OdtTransformer","extractLanguages");

        return languages;

    }

    public Collection<ParagraphStyle> extractParagraphStyles() {

        logger.entering("OdtTransformer","extractParagraphStyles");

        Map<String,ParagraphStyle> styles = new TreeMap<String,ParagraphStyle>();
        Map<String,String> parents = new TreeMap<String,String>();

        try {

            if (usedStylesFile == null) {

                usedStylesFile = File.createTempFile(TMP_NAME, ".styles.xml", TMP_DIR);
                usedStylesFile.deleteOnExit();

                Transformer stylesXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(XSLT + "get-styles.xsl").toString()));

                stylesXSL.setParameter("styles-url", odtStylesFile.toURI());

                stylesXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                stylesXSL.setOutputProperty(OutputKeys.METHOD, "xml");
                stylesXSL.setOutputProperty(OutputKeys.INDENT, "yes");
                stylesXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

                saveDocument();
                stylesXSL.transform(new StreamSource(odtContentFile), new StreamResult(usedStylesFile));

            }

            int count = XPathUtils.evaluateNumber(usedStylesFile.toURL().openStream(), "count(/o2b:styles/o2b:style[@family='paragraph'])", namespace).intValue();
            for (int i=1; i<=count; i++) {
                String styleName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                    "/o2b:styles/o2b:style[@family='paragraph'][" + i + "]/@name", namespace);
                String parentStyleName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                    "/o2b:styles/o2b:style[@family='paragraph'][" + i + "]/@parent-style-name", namespace);
                parents.put(styleName, (parentStyleName.length()>0) ? parentStyleName : null);
            }

            Collection<String> remove = new ArrayList<String>();
            boolean cont = true;
            while (cont) {
                cont = false;
                for (String styleName : parents.keySet()) {
                    String parentStyleName = parents.get(styleName);
                    if (parentStyleName == null || styles.containsKey(parentStyleName)) {
                        ParagraphStyle parentStyle = (parentStyleName == null) ? null : styles.get(parentStyleName);
                        String displayName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                            "/o2b:styles/o2b:style[@family='paragraph' and @name='" + styleName + "']/@display-name", namespace);
                        boolean auto = XPathUtils.evaluateBoolean(usedStylesFile.toURL().openStream(),
                            "/o2b:styles/o2b:style[@family='paragraph' and @name='" + styleName + "']/@automatic", namespace);
                        if (styleName.equals("Standard")) {
                            displayName = "Default";
                        } else if (displayName.length() == 0) {
                            displayName = styleName;
                        }
                        ParagraphStyle style = new ParagraphStyle(styleName, auto, displayName, parentStyle);
                        if (auto) { automaticParagraphStyles.put(styleName, style); }
                        styles.put(styleName, style);
                        remove.add(styleName);
                        cont = true;
                    }
                }
                for (String styleName : remove) { parents.remove(styleName); }
                remove.clear();
            }

        } catch (IOException e) {
        } catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
        }

        logger.exiting("OdtTransformer","extractParagraphStyles");

        return styles.values();

    }

    public Collection<CharacterStyle> extractCharacterStyles() {
        
        logger.entering("OdtTransformer","extractCharacterStyles");

        TreeMap<String,CharacterStyle> styles = new TreeMap<String,CharacterStyle>();
        TreeMap<String,String> parents = new TreeMap<String,String>();

        try {

            if (usedStylesFile == null) {

                usedStylesFile = File.createTempFile(TMP_NAME, ".styles.xml", TMP_DIR);
                usedStylesFile.deleteOnExit();

                Transformer stylesXSL = tFactory.newTransformer(new StreamSource(getClass().getResource(XSLT + "get-styles.xsl").toString()));

                stylesXSL.setParameter("styles-url", odtStylesFile.toURI());

                stylesXSL.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                stylesXSL.setOutputProperty(OutputKeys.METHOD, "xml");
                stylesXSL.setOutputProperty(OutputKeys.INDENT, "yes");
                stylesXSL.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");

                saveDocument();
                stylesXSL.transform(new StreamSource(odtContentFile), new StreamResult(usedStylesFile));

            }

            styles.put("Default", new CharacterStyle("Default", "Default", null));

            int count = XPathUtils.evaluateNumber(usedStylesFile.toURL().openStream(), "count(/o2b:styles/o2b:style[@family='text'])", namespace).intValue();
            for (int i=1; i<=count; i++) {
                String styleName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                    "/o2b:styles/o2b:style[@family='text'][" + i + "]/@name", namespace);
                String parentStyleName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                    "/o2b:styles/o2b:style[@family='text'][" + i + "]/@parent-style-name", namespace);
                parents.put(styleName, (parentStyleName.length()>0) ? parentStyleName : "Default");
            }

            Collection<String> remove = new ArrayList<String>();
            boolean cont = true;
            while (cont) {
                cont = false;
                for (String styleName : parents.keySet()) {
                    String parentStyleName = parents.get(styleName);
                    if (styles.containsKey(parentStyleName)) {
                        CharacterStyle parentStyle = styles.get(parentStyleName);
                        String displayName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                            "/o2b:styles/o2b:style[@family='text' and @name='" + styleName + "']/@display-name", namespace);
                        if (displayName.length() == 0) { displayName = styleName; }
                        CharacterStyle style = new CharacterStyle(styleName, displayName, parentStyle);
                        styles.put(styleName, style);
                        remove.add(styleName);
                        cont = true;
                    }
                }
                for (String styleName : remove) { parents.remove(styleName); }
                remove.clear();
            }

        } catch (IOException e) {
        } catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
        }

        logger.exiting("OdtTransformer","extractCharacterStyles");
        
        return styles.values();
    
    }

    public Element extractSectionTree() throws IOException,
                                               SAXException {

        logger.entering("OdtTransformer","extractSectionTree");

        parseDocument();

        Document doc = docBuilder.getDOMImplementation().createDocument(null, "section", null);
        Element rootSection = doc.getDocumentElement();

        try {
            Node bodyText = XPathAPI.selectSingleNode(contentDoc.getDocumentElement(), "//body/text[1]");
            copySectionNodes(bodyText, rootSection, doc);
        } catch (TransformerException e) {
        }

        logger.exiting("OdtTransformer","extractSectionTree");

        return rootSection;
    }

    private void copySectionNodes(Node from,
                                  Element to,
                                  Document doc)
                           throws TransformerException {
        Element e;
        String name;
        NodeIterator iterator = XPathAPI.selectNodeIterator(from, "section");
        for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
            name = n.getAttributes().getNamedItem("text:name").getNodeValue();
            e = doc.createElement("section");
            e.setAttribute("name", name);
            to.appendChild(e);
            copySectionNodes(n, e, doc);
        }
    }

    public int[] extractDocumentOutline() throws IOException,
                                                 SAXException,
                                                 TransformerConfigurationException,
                                                 TransformerException,
                                                 ConversionException {
        // Dependencies
        if (!transformDone) { throw new ConversionException(); }

        logger.entering("OdtTransformer","extractDocumentOutline");

        Document daisy = docBuilder.parse(daisyFile);
        Node root = daisy.getDocumentElement();

        int pageCount =  Integer.parseInt(XPathAPI.eval(root, "count(//bodymatter/volume[1]//pagenum)").str());
        int[] outline = new int[pageCount];
        NodeIterator iterator;
        int lvl;
        for (int i=0; i<pageCount; i++) {
            outline[i] = 0;
            iterator = XPathAPI.selectNodeIterator(root, "//bodymatter/volume[1]/heading" +
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

        logger.exiting("OdtTransformer","extractDocumentOutline");

        return outline;
    }

 /* public Set<String> extractTableNames() {



    } */
    
    public Set<String> extractNoterefCharacters() throws IOException,
                                                         TransformerException,
                                                         SAXException  {

        logger.entering("OdtTransformer","extractNoterefCharacters");

        parseDocument();

        Set<String> characters = new HashSet<String>();
        NodeList nodes = XPathAPI.selectNodeList(contentDoc.getDocumentElement(), "//body/text[1]//note-citation");
        for (int i=0; i<nodes.getLength(); i++) {
            characters.add(nodes.item(i).getNodeValue());
        }

        logger.exiting("OdtTransformer","extractNoterefCharacters");

        return characters;
    }

    public File getOdtContentFile() {
        return odtContentFile;
    }

    public File getOdtStylesFile() {
        return odtStylesFile;
    }

    public File getOdtMetaFile() {
        return odtMetaFile;
    }

    public File getOdtSettingsFile() {
        return odtSettingsFile;
    }

    public File getControllerFile() {
        return controllerFile;
    }

    public Locale getOdtLocale() {
        return odtLocale;
    }

    private boolean isWhiteSpaceOnlyTextNode(Node node) {

        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getNodeValue();
            if (text.startsWith(" ") || text.startsWith("\n ")) {
                return true;
            }
        }

        return false;
    }

    public void close() throws IOException {
        zip.close();
    }
}
