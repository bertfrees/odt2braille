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

import be.docarch.odt2braille.convert.ConversionException;
import be.docarch.odt2braille.convert.XSLTransformer;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ConfigurationEncoder;
import be.docarch.odt2braille.setup.ConfigurationDecoder;
import be.docarch.odt2braille.setup.ConfigurationFactory;
import be.docarch.odt2braille.utils.FileCreator;
import be.docarch.odt2braille.utils.OdtUtils;
import be.docarch.odt2braille.utils.XPathUtils;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.Map;
import java.util.Collection;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.apache.xpath.XPathAPI;

/**
 * This class enables you to transform a flat .odt file to a DAISY-like xml file that is suited to be processed by <code>liblouisxml</code>.
 * Note that, although it looks like a DAISY xml file, it is not, and would not validate.
 *
 * The actual transformation is done with XSLT.
 * In addition to XSLT, <code>ODT</code> also has a supporting {@link #preProcessing} method.
 * Thanks to this preprocessing the XSL transform is considerably simplified.
 * On the other hand, <code>preProcessing</code> is rather slow because it uses DOM (performance should be improved in the future).
 *
 * @see <a href="http://www.daisy.org/z3986/2005/Z3986-2005.html">DAISY xml specification</a>
 * @author Bert Frees
 */
public class ODT {

    private static final Logger logger = Constants.getLogger();
    
    private static final NamespaceContext NAMESPACE = new NamespaceContext();
    private static final DocumentBuilder docBuilder;
    
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
    }

    private Configuration configuration = null;
    private List<ODTListener> listeners = new ArrayList<ODTListener>();

    private ZipFile zip = null;
    private Document odtContentDocument = null;
    private File odtContentFile = null;
    private File odtStylesFile = null;
    private File odtMetaFile = null;
    private File odtSettingsFile = null;
    
    private File usedStylesFile = null;
    private Map<String,Style> usedParagraphStyles = null;
    private Map<String,Style> usedCharacterStyles = null;
    private Collection<Locale> usedLocales = null;
    
    private Locale odtLocale = null;
    private Integer pageCount = null;
    

    /**
     * Creates a new <code>ODT</code> instance.
     *
     * @param odtFile           The .odt file.
     * @param statusIndicator   The <code>StatusIndicator</code> that will be used.
     * @param oooLocale         The <code>Locale</code> for the user interface.
     */
    public ODT(File odtFile) throws Exception {
        open(odtFile);
    }

    public void addListener(ODTListener listener) {
        listeners.add(listener);
    }
    
    public void open(File odtFile) throws Exception {
        close();
        zip = new ZipFile(odtFile.getAbsolutePath());
        odtContentDocument = null;
        usedParagraphStyles = null;
        usedCharacterStyles = null;
        usedLocales = null;
        odtLocale = null;
        for (ODTListener listener : listeners) {
            listener.odtUpdated(this);
        }
    }

    public void close() {
        if (odtContentFile != null) {
            odtContentFile.delete();
            odtContentFile = null;
        }
        if (odtStylesFile != null) {
            odtStylesFile.delete();
            odtStylesFile = null;
        }
        if (odtMetaFile != null) {
            odtMetaFile.delete();
            odtMetaFile = null;
        }
        if (odtSettingsFile != null) {
            odtSettingsFile.delete();
            odtSettingsFile = null;
        }
        if (usedStylesFile != null) {
            usedStylesFile.delete();
            usedStylesFile = null;
        }
        if (zip != null) {
            try {
                zip.close();
            } catch (IOException e) {
            }
        }
    }
    
    
    /* TODO: verwijderen uit ODT.java ************************************************************/
    public Configuration getConfiguration() throws Exception {
        if (configuration == null) {
            ConfigurationFactory.setODT(this);
            configuration = ConfigurationFactory.getInstance();
        }
        return configuration;
    }

    public String saveConfiguration(String encoding) throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConfigurationEncoder.writeObject(getConfiguration(), output);
        return output.toString(encoding).replaceFirst("<\\?xml.*?\\?>", "");
    }

    public Configuration loadConfiguration(String xml,
                                           String encoding)
                                    throws Exception {

        ConfigurationFactory.setODT(this);
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes(encoding));
        Configuration newConfig = (Configuration)ConfigurationDecoder.readObject(input);
        if (newConfig != null) { configuration = newConfig; }
        return configuration;
    }
    /*********************************************************************************************/
    
    
    private Document getContentDocument() throws Exception {
        if (odtContentDocument == null) {
            odtContentDocument = docBuilder.parse(getContentFile().getAbsolutePath());
        }
        return odtContentDocument;
    }

    public Collection<Locale> getUsedLocales() {

        if (usedLocales == null) {
        
            usedLocales = new ArrayList<Locale>();
            
            try {
                
                File usedLanguagesFile = FileCreator.createTempFile(".languages.xml");
                
                logger.log(Level.INFO, "Determining used languages: {0}", usedLanguagesFile.getName());
                
                XSLTransformer transformer = new XSLTransformer("get-languages") {
                    @Override
                    public void convert(File input, File output) throws ConversionException {
                        try {
                            setParameter("styles-url", getStylesFile().toURI());
                            super.convert(input, output);
                        } catch (Exception e) {
                            throw new ConversionException(e);
                        }
                    }
                };
                        
                transformer.convert(getContentFile(), usedLanguagesFile);

                int count = XPathUtils.evaluateNumber(usedStylesFile.toURL().openStream(),
                        "count(/my:languages/my:language)", NAMESPACE).intValue();
                for (int i=0; i<count; i++) {
                    usedLocales.add(stringToLocale(XPathUtils.evaluateString(usedLanguagesFile.toURL().openStream(),
                                   "/my:languages/my:language[not(@class='main')][" + i + "]/@name", NAMESPACE)));
                }
                
                // Clean up
                usedLanguagesFile.delete();

            } catch (Exception e) {
            }
        }

        return new ArrayList<Locale>(usedLocales);

    }

    private void makeUsedStylesFile() throws Exception {
    
        if (usedStylesFile == null) {

            usedStylesFile = FileCreator.createTempFile(".styles.xml");
            
            logger.log(Level.INFO, "Determining used styles: {0}", usedStylesFile.getName());
            
            XSLTransformer transformer = new XSLTransformer("get-styles") {
                @Override
                public void convert(File input, File output) throws ConversionException {
                    try {
                        setParameter("styles-url", getStylesFile().toURI());
                        super.convert(input, output);
                    } catch (Exception e) {
                        throw new ConversionException(e);
                    }
                }
            };

            transformer.convert(getContentFile(), usedStylesFile);
        }
    }
    
    public Collection<Style> getUsedParagraphStyles() {

        if (usedParagraphStyles == null) {
        
            usedParagraphStyles = new TreeMap<String,Style>();
            
            try {
            
                makeUsedStylesFile();
                
                Map<String,String> parents = new TreeMap<String,String>();
            
                int count = XPathUtils.evaluateNumber(usedStylesFile.toURL().openStream(),
                        "count(/my:styles/my:style[@family='paragraph'])", NAMESPACE).intValue();
                for (int i=1; i<=count; i++) {
                    String styleName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                        "/my:styles/my:style[@family='paragraph'][" + i + "]/@name", NAMESPACE);
                    String parentStyleName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                        "/my:styles/my:style[@family='paragraph'][" + i + "]/@parent-style-name", NAMESPACE);
                    parents.put(styleName, (parentStyleName.length()>0) ? parentStyleName : null);
                }

                Collection<String> remove = new ArrayList<String>();
                boolean cont = true;
                while (cont) {
                    cont = false;
                    for (String styleName : parents.keySet()) {
                        String parentStyleName = parents.get(styleName);
                        if (parentStyleName == null || usedParagraphStyles.containsKey(parentStyleName)) {
                            Style parentStyle = (parentStyleName == null) ? null : usedParagraphStyles.get(parentStyleName);
                            String displayName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                                "/my:styles/my:style[@family='paragraph' and @name='" + styleName + "']/@display-name", NAMESPACE);
                            boolean automatic = XPathUtils.evaluateBoolean(usedStylesFile.toURL().openStream(),
                                "/my:styles/my:style[@family='paragraph' and @name='" + styleName + "']/@automatic", NAMESPACE);
                            if (styleName.equals("Standard")) {
                                displayName = "Default";
                            } else if (displayName.length() == 0) {
                                displayName = styleName;
                            }
                            usedParagraphStyles.put(styleName,
                                    new Style(styleName, Style.Family.PARAGRAPH, displayName, automatic, parentStyle));
                            remove.add(styleName);
                            cont = true;
                        }
                    }
                    for (String styleName : remove) { parents.remove(styleName); }
                    remove.clear();
                }

                if (usedParagraphStyles.isEmpty()) {
                    usedParagraphStyles.put("Standard", new Style("Standard", Style.Family.PARAGRAPH, "Default", false));
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        
        return usedParagraphStyles.values();
    }
    
    public Collection<Style> getUsedCharacterStyles() {

        if (usedCharacterStyles == null) {
        
            usedCharacterStyles = new TreeMap<String,Style>();
        
            try {
                
                makeUsedStylesFile();
                
                TreeMap<String,String> parents = new TreeMap<String,String>();

                usedCharacterStyles.put("Default", new Style("Default", Style.Family.TEXT, "Default", false));

                int count = XPathUtils.evaluateNumber(usedStylesFile.toURL().openStream(),
                        "count(/my:styles/my:style[@family='text'])",NAMESPACE).intValue();
                for (int i=1; i<=count; i++) {
                    String styleName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                        "/my:styles/my:style[@family='text'][" + i + "]/@name", NAMESPACE);
                    String parentStyleName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                        "/my:styles/my:style[@family='text'][" + i + "]/@parent-style-name", NAMESPACE);
                    parents.put(styleName, (parentStyleName.length()>0) ? parentStyleName : "Default");
                }

                Collection<String> remove = new ArrayList<String>();
                boolean cont = true;
                while (cont) {
                    cont = false;
                    for (String styleName : parents.keySet()) {
                        String parentStyleName = parents.get(styleName);
                        if (usedCharacterStyles.containsKey(parentStyleName)) {
                            Style parentStyle = usedCharacterStyles.get(parentStyleName);
                            String displayName = XPathUtils.evaluateString(usedStylesFile.toURL().openStream(),
                                "/my:styles/my:style[@family='text' and @name='" + styleName + "']/@display-name", NAMESPACE);
                            if (displayName.length() == 0) { displayName = styleName; }
                            usedCharacterStyles.put(styleName,
                                    new Style(styleName, Style.Family.TEXT, displayName, false, parentStyle));
                            remove.add(styleName);
                            cont = true;
                        }
                    }
                    for (String styleName : remove) { parents.remove(styleName); }
                    remove.clear();
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        
        return usedCharacterStyles.values();
    
    }

    public Element getSectionTree() throws Exception {

        Document doc = docBuilder.getDOMImplementation().createDocument(null, "section", null);
        Element rootSection = doc.getDocumentElement();

        try {
            Node bodyText = XPathAPI.selectSingleNode(getContentDocument().getDocumentElement(), "//body/text[1]");
            copySectionNodes(bodyText, rootSection, doc);
        } catch (TransformerException e) {
        }

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

 /* public Set<String> getTableNames() {} */
    
    public Set<String> getNoterefCharacters() throws Exception  {

        Set<String> characters = new HashSet<String>();
        NodeList nodes = XPathAPI.selectNodeList(getContentDocument().getDocumentElement(), "//body/text[1]//note-citation");
        for (int i=0; i<nodes.getLength(); i++) {
            characters.add(nodes.item(i).getNodeValue());
        }

        return characters;
    }
    
    public File getContentFile() throws IOException {
        if (odtContentFile == null) {
            odtContentFile = FileCreator.createTempFile(".odt.content.xml");
            OdtUtils.getFileFromZip(zip, "content.xml",  odtContentFile);
        }
        return odtContentFile;
    }

    public File getStylesFile() throws IOException {
        if (odtStylesFile == null) {
            odtStylesFile = FileCreator.createTempFile(".odt.styles.xml");
            OdtUtils.getFileFromZip(zip, "styles.xml",   odtStylesFile);
        }
        return odtStylesFile;
    }

    public File getMetaFile() throws IOException {
        if (odtMetaFile == null) {
            odtMetaFile = FileCreator.createTempFile(".odt.meta.xml");
            OdtUtils.getFileFromZip(zip, "meta.xml",     odtMetaFile);
        }
        return odtMetaFile;
    }

    public File getSettingsFile() throws IOException {
        if (odtSettingsFile == null) {
            odtSettingsFile = FileCreator.createTempFile(".odt.configuration.xml");
            OdtUtils.getFileFromZip(zip, "configuration.xml", odtSettingsFile);
        }
        return odtSettingsFile;
    }

    public Locale getLocale() throws Exception {
        if (odtLocale == null) {
            odtLocale = new Locale(XPathUtils.evaluateString(getStylesFile().toURL().openStream(),
                "//office:styles/style:default-style/style:text-properties/@fo:language",NAMESPACE).toLowerCase(),
                               XPathUtils.evaluateString(getStylesFile().toURL().openStream(),
                "//office:styles/style:default-style/style:text-properties/@fo:country", NAMESPACE).toUpperCase());
        }
        return odtLocale;
    }
    
    public int getPageCount() throws Exception {
        if (pageCount == null) {
            Element metaRoot = docBuilder.parse(getMetaFile().getAbsolutePath()).getDocumentElement();
            pageCount = Integer.parseInt(XPathAPI.eval(metaRoot, "//meta/document-statistic/@page-count").str());
        }
        return pageCount;
    }
    
    public boolean pageNumberInHeaderOrFooter() throws Exception {
        return XPathUtils.evaluateBoolean(getStylesFile().toURL().openStream(),
                        "//office:master-styles//style:header/text:p/text:page-number or " +
                        "//office:master-styles//style:footer/text:p/text:page-number", NAMESPACE);
    }
    
    public String getDate() throws Exception {
        
        if (XPathUtils.evaluateBoolean(getMetaFile().toURL().openStream(), "//office:meta/dc:date",NAMESPACE)) {
            return XPathUtils.evaluateString(getMetaFile().toURL().openStream(), "//office:meta/dc:date/text()",NAMESPACE).substring(0, 4);
        } else if (XPathUtils.evaluateBoolean(getMetaFile().toURL().openStream(), "//office:meta/meta:creation-date",NAMESPACE)) {
            return XPathUtils.evaluateString(getMetaFile().toURL().openStream(), "//office:meta/meta:creation-date/text()",NAMESPACE).substring(0, 4);
        } else {
            return (new SimpleDateFormat("yyyy")).format(new Date());
        }
    }

    public String getCreator() throws Exception {
        if (XPathUtils.evaluateBoolean(getMetaFile().toURL().openStream(), "//office:meta/dc:creator",NAMESPACE)) {
            return XPathUtils.evaluateString(getMetaFile().toURL().openStream(), "//office:meta/dc:creator/text()",NAMESPACE);
        } else if (XPathUtils.evaluateBoolean(getMetaFile().toURL().openStream(), "//office:meta/meta:initial-creator",NAMESPACE)) {
            return XPathUtils.evaluateString(getMetaFile().toURL().openStream(), "//office:meta/meta:initial-creator/text()",NAMESPACE);
        } else {
            return "";
        }
    }
    
    public ZipFile getZipFile() {
        return zip;
    }

    public static class Style {
    
        private static enum Family { PARAGRAPH, TEXT }
        
        private final String name;
        private final String displayName;
        private final Family family;
        private final boolean automatic;
        private final Style parentStyle;
        
        private Style(String name, Family family, String displayName, boolean automatic) {
            this(name, family, displayName, automatic, null);
        }
        
        private Style(String name, Family family, String displayName, boolean automatic, Style parentStyle) {
            this.name = name;
            this.family = family;
            this.displayName = displayName;
            this.automatic = automatic;
            this.parentStyle = parentStyle;
        }
        
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public boolean isAutomatic() { return automatic; }
        public Style getParentStyle() { return parentStyle; }
        
        public Style getNonAutomaticStyle() {
            if (automatic) {
                return parentStyle.getNonAutomaticStyle();
            } else {
                return this;
            }
        }
    }
    
    
    /********************/
    /* HELPER FUNCTIONS */
    /********************/
    
    private static Locale stringToLocale(String s) {

        if (!s.contains("-")) {
            return new Locale(s);
        } else {
            int i = s.indexOf("-");
            return new Locale(s.substring(0,i), s.substring(i+1));
        }
    }
}
