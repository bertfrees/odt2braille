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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;

import org.xml.sax.SAXException;
import java.net.MalformedURLException;
import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import be.docarch.odt2braille.BrailleFileExporter.BrailleFileType;
import org_pef_text.pef2text.Paper;
import org_pef_text.pef2text.Paper.PaperSize;
import org_pef_text.pef2text.EmbosserFactory.EmbosserType;
import org_pef_text.TableFactory.TableType;

import org_pef_text.pef2text.UnsupportedPaperException;


/**
 * Collection of all braille-related settings and properties of an OpenOffice.org document.
 *
 * @author Bert Frees
 */
public class Settings {

    public static enum MathType { NEMETH, UKMATHS, MARBURG, WISKUNDE };
    public static enum BrailleRules { CUSTOM, BANA };
    public static enum PageNumberFormat { NORMAL, ROMAN, P, S, BLANK };
    public static enum PageNumberPosition { TOP_LEFT, TOP_RIGHT, TOP_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER };
    public static enum VolumeManagementMode { SINGLE, MANUAL, AUTOMATIC };

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static NamespaceContext namespace = new NamespaceContext();
    private final static String L10N = Constants.L10N_PATH;

    private OdtTransformer odtTransformer;

    private static String L10N_transcribersNotesPageTitle;
    private static String L10N_specialSymbolsListTitle;
    private static String L10N_tableOfContentTitle;
    private static String L10N_continuedSuffix;
    private static String L10N_in = null;
    private static String L10N_and = null;
    private static String L10N_volume = null;
    private static String L10N_volumes = null;
    private static String L10N_supplement = null;
    private static String L10N_supplements = null;
    private static String L10N_preliminary = null;
    private static String L10N_transcriptionInfo = null;

    private static final BrailleFileType DEFAULT_BRAILLE_FILE_TYPE = BrailleFileType.BRF;
    private static final TableType DEFAULT_TABLE = TableType.EN_US;
    private static final EmbosserType DEFAULT_EMBOSSER = EmbosserType.NONE;
    private static final PaperSize DEFAULT_PAPERSIZE = PaperSize.CUSTOM;
    private static final MathType DEFAULT_MATH = MathType.NEMETH;
    private static final String DEFAULT_TRANSLATION_TABLE = "en-US";
    private static final int DEFAULT_GRADE = 2;

    // Read only properties

    protected String DATE;
    protected int NUMBER_OF_VOLUMES;
    protected int NUMBER_OF_SUPPLEMENTS;
    protected boolean PARAGRAPHS_PRESENT = false;
    protected boolean HEADINGS_PRESENT = false;
    protected boolean LISTS_PRESENT = false;
    protected boolean TABLES_PRESENT = false;
    protected boolean PAGE_NUMBER_IN_HEADER_FOOTER = false;
    protected boolean MATH_PRESENT = false;

    // General Settings

    protected BrailleRules brailleRules;

    protected String mainLanguage;

    protected String creator;
    protected String transcribersNotesPageTitle;
    protected String specialSymbolsListTitle;
    protected String tableOfContentTitle;
    protected String continuedSuffix;

    protected boolean hyphenate = false;
    protected boolean hardPageBreaks = false;

    // Page Numbers Settings

    protected boolean printPageNumbers = false;
    protected boolean braillePageNumbers = false;
    protected boolean pageSeparator = false;
    protected boolean pageSeparatorNumber = false;
    protected boolean continuePages = false;
    protected boolean ignoreEmptyPages = false;
    protected boolean mergeUnnumberedPages = false;
    protected boolean pageNumberAtTopOnSeparateLine = false;
    protected boolean pageNumberAtBottomOnSeparateLine = false;
    protected boolean printPageNumberRange = false;
    protected PageNumberPosition printPageNumberAt;
    protected PageNumberPosition braillePageNumberAt;
    protected PageNumberFormat preliminaryPageNumberFormat;
    protected PageNumberFormat supplementaryPageNumberFormat;
    protected int beginningBraillePageNumber;
    
    // Language Settings

    protected Map<String,String> translationTableMap;
    protected Map<String,Integer> gradeMap;
    protected Map<String,Integer> dotsMap;

    // Table Of Contents Settings

    protected boolean tableOfContentEnabled = false;

    // List Of Special Symbols Settings

    private List<SpecialSymbol> specialSymbolsList;

    // Mathematics Settings

    protected MathType math;

    // Notes Settings

    protected Map<String,String> noterefNumberPrefixMap = null;
    protected Map<String,String> noterefCharactersMap = null;
//    protected boolean noterefSpaceBefore = false;
//    protected boolean noterefSpaceAfter = false;
    protected Style footnoteStyle = null;

    // Volume management

    protected VolumeManagementMode volumeManagementMode = null;
    protected Section rootSection = null;
    protected List<Section> mainSections = null;
    protected List<Section> allSections = null;
    protected Map<Section,Volume> volumeSectionsMap = null;
    protected List<Volume> automaticVolumes = null;
    protected Volume singleVolume = null;
    protected Volume preliminaryVolume = null;
    protected Section frontMatterSection = null;
    protected Section titlePageSection = null;
    protected Section extendedFrontMatterSection = null;
    protected boolean volumeInfoEnabled = false;
    protected boolean transcriptionInfoEnabled = false;
    protected String volumeInfo = null;
    protected String transcriptionInfo = null;

    protected boolean transcribersNotesPageEnabled = false;
    protected boolean specialSymbolsListEnabled = false;
    protected boolean preliminaryVolumeEnabled = false;

    // Emboss & Export Settings

    protected EmbosserType embosser;
    protected PaperSize paperSize;
    protected TableType table;
    protected BrailleFileType brailleFileType;

    protected boolean multipleFiles = false;
    protected boolean exportOrEmboss = false;

    protected double cellSpacing;
    protected double lineSpacing;
    protected double cellHeight;
    protected double cellWidth;
    protected double interpointShiftX;
    protected double interpointShiftY;

    protected double paperWidth;
    protected double paperHeight;
    protected double maxPaperWidth;
    protected double maxPaperHeight;
    protected double minPaperWidth;
    protected double minPaperHeight;

    protected double pageWidth;
    protected double pageHeight;
    protected double printablePageWidth;
    protected double printablePageHeight;
    protected double unprintableInner;
    protected double unprintableOuter;
    protected double unprintableTop;
    protected double unprintableBottom;

    protected int cellsInWidth;
    protected int linesInHeight;
    protected int cellsPerLine;
    protected int linesPerPage;
    protected int maxCellsPerLine;
    protected int maxLinesPerPage;
    protected int minCellsPerLine;
    protected int minLinesPerPage;

    protected int marginInner;
    protected int marginOuter;
    protected int marginTop;
    protected int marginBottom;
    protected int maxMarginInner;
    protected int maxMarginOuter;
    protected int maxMarginTop;
    protected int maxMarginBottom;
    protected int minMarginInner;
    protected int minMarginOuter;
    protected int minMarginTop;
    protected int minMarginBottom;

    protected boolean duplex = false;
    protected boolean eightDots = false;
    protected boolean zFolding = false;
    protected boolean saddleStitch = false;
    protected int sheetsPerQuire;

    // Various

    protected Map<String,ParagraphStyle> paragraphStylesMap;
    protected Map<String,CharacterStyle> characterStylesMap;
    protected List<HeadingStyle> headingStyles;
    protected List<ListStyle> listStyles;
    protected TableStyle tableStyle;
    protected FrameStyle frameStyle;
    protected TocStyle tocStyle;
    protected ParagraphStyle volumeInfoStyle;
    protected ParagraphStyle transcriptionInfoStyle;
    protected List<String> supportedTranslationTablesGrades;
    protected List<String> specialTranslationTables;


    /**
     * Creates a new <code>Settings</code> instance by copying other <code>Settings</code>.
     *
     * @param copySettigns    The <code>Settings</code> to be copied.
     */
    public Settings(Settings copySettings) {

        logger.entering("Settings", "<init>");

        Volume.init();

        this.odtTransformer = copySettings.odtTransformer;

        this.NUMBER_OF_VOLUMES = copySettings.NUMBER_OF_VOLUMES;
        this.NUMBER_OF_SUPPLEMENTS = copySettings.NUMBER_OF_SUPPLEMENTS;
        this.maxCellsPerLine = copySettings.maxCellsPerLine;
        this.minLinesPerPage = copySettings.minLinesPerPage;
        this.minCellsPerLine = copySettings.minCellsPerLine;
        this.maxLinesPerPage = copySettings.maxLinesPerPage;
        this.minMarginInner = copySettings.minMarginInner;
        this.minMarginOuter = copySettings.minMarginOuter;
        this.minMarginTop = copySettings.minMarginTop;
        this.minMarginBottom = copySettings.minMarginBottom;
        this.maxMarginInner = copySettings.maxMarginInner;
        this.maxMarginOuter = copySettings.maxMarginOuter;
        this.maxMarginTop = copySettings.maxMarginTop;
        this.maxMarginBottom = copySettings.maxMarginBottom;
        this.cellsInWidth = copySettings.cellsInWidth;
        this.linesInHeight = copySettings.linesInHeight;
        this.cellsPerLine = copySettings.cellsPerLine;
        this.linesPerPage = copySettings.linesPerPage;
        this.marginInner = copySettings.marginInner;
        this.marginOuter = copySettings.marginOuter;
        this.marginTop = copySettings.marginTop;
        this.marginBottom = copySettings.marginBottom;
        this.beginningBraillePageNumber = copySettings.beginningBraillePageNumber;
        this.sheetsPerQuire = copySettings.sheetsPerQuire;

        this.cellSpacing = copySettings.cellSpacing;
        this.lineSpacing = copySettings.lineSpacing;
        this.cellHeight = copySettings.cellHeight;
        this.cellWidth = copySettings.cellWidth;
        this.interpointShiftX = copySettings.interpointShiftX;
        this.interpointShiftY = copySettings.interpointShiftY;
        this.paperWidth = copySettings.paperWidth;
        this.paperHeight = copySettings.paperHeight;
        this.maxPaperWidth = copySettings.maxPaperWidth;
        this.maxPaperHeight = copySettings.maxPaperHeight;
        this.minPaperWidth = copySettings.minPaperWidth;
        this.minPaperHeight = copySettings.minPaperHeight;
        this.pageWidth = copySettings.pageWidth;
        this.pageHeight = copySettings.pageHeight;
        this.printablePageWidth = copySettings.printablePageWidth;
        this.printablePageHeight = copySettings.printablePageHeight;
        this.unprintableInner = copySettings.unprintableInner;
        this.unprintableOuter = copySettings.unprintableOuter;
        this.unprintableTop = copySettings.unprintableTop;
        this.unprintableBottom = copySettings.unprintableBottom;

        this.DATE = new String(copySettings.DATE);
        this.mainLanguage = new String(copySettings.mainLanguage);
        this.creator = new String(copySettings.creator);
        this.transcribersNotesPageTitle = new String(copySettings.transcribersNotesPageTitle);
        this.specialSymbolsListTitle = new String(copySettings.specialSymbolsListTitle);
        this.tableOfContentTitle = new String(copySettings.tableOfContentTitle);
        this.continuedSuffix = new String(copySettings.continuedSuffix);
        this.volumeInfo = new String(copySettings.volumeInfo);
        this.transcriptionInfo = new String(copySettings.transcriptionInfo);

        this.PARAGRAPHS_PRESENT = copySettings.PARAGRAPHS_PRESENT;
        this.HEADINGS_PRESENT = copySettings.HEADINGS_PRESENT;
        this.LISTS_PRESENT = copySettings.LISTS_PRESENT;
        this.TABLES_PRESENT = copySettings.TABLES_PRESENT;
        this.PAGE_NUMBER_IN_HEADER_FOOTER = copySettings.PAGE_NUMBER_IN_HEADER_FOOTER;
        this.MATH_PRESENT = copySettings.MATH_PRESENT;
        this.printPageNumbers = copySettings.printPageNumbers;
        this.braillePageNumbers = copySettings.braillePageNumbers;
        this.pageSeparator = copySettings.pageSeparator;
        this.pageSeparatorNumber = copySettings.pageSeparatorNumber;
        this.continuePages = copySettings.continuePages;
        this.ignoreEmptyPages = copySettings.ignoreEmptyPages;
        this.mergeUnnumberedPages = copySettings.mergeUnnumberedPages;
        this.pageNumberAtTopOnSeparateLine = copySettings.pageNumberAtTopOnSeparateLine;
        this.pageNumberAtBottomOnSeparateLine = copySettings.pageNumberAtBottomOnSeparateLine;
        this.printPageNumberRange = copySettings.printPageNumberRange;
        this.transcribersNotesPageEnabled = copySettings.transcribersNotesPageEnabled;
        this.specialSymbolsListEnabled = copySettings.specialSymbolsListEnabled;
        this.volumeInfoEnabled = copySettings.volumeInfoEnabled;
        this.transcriptionInfoEnabled = copySettings.transcriptionInfoEnabled;
        this.tableOfContentEnabled = copySettings.tableOfContentEnabled;
        this.preliminaryVolumeEnabled = copySettings.preliminaryVolumeEnabled;
        this.exportOrEmboss = copySettings.exportOrEmboss;
        this.multipleFiles = copySettings.multipleFiles;
        this.duplex = copySettings.duplex;
        this.eightDots = copySettings.eightDots;
        this.hardPageBreaks = copySettings.hardPageBreaks;
        this.hyphenate = copySettings.hyphenate;
        this.zFolding = copySettings.zFolding;
        this.saddleStitch = copySettings.saddleStitch;
//        this.noterefSpaceBefore = copySettings.noterefSpaceBefore;
//        this.noterefSpaceAfter = copySettings.noterefSpaceAfter;

        this.printPageNumberAt = copySettings.printPageNumberAt;
        this.braillePageNumberAt = copySettings.braillePageNumberAt;
        this.preliminaryPageNumberFormat = copySettings.preliminaryPageNumberFormat;
        this.supplementaryPageNumberFormat = copySettings.supplementaryPageNumberFormat;
        this.brailleRules = copySettings.brailleRules;
        this.embosser = copySettings.embosser;
        this.paperSize = copySettings.paperSize;
        this.table = copySettings.table;
        this.brailleFileType = copySettings.brailleFileType;
        this.math = copySettings.math;
        this.volumeManagementMode = copySettings.volumeManagementMode;

        this.translationTableMap = new TreeMap(copySettings.translationTableMap);
        this.noterefNumberPrefixMap = new TreeMap(copySettings.noterefNumberPrefixMap);
        this.noterefCharactersMap = new TreeMap(copySettings.noterefCharactersMap);

        this.gradeMap = new TreeMap(copySettings.gradeMap);
        this.dotsMap = new TreeMap(copySettings.dotsMap);

        this.paragraphStylesMap = new TreeMap<String,ParagraphStyle>();
        for (ParagraphStyle copyParagraphStyle: copySettings.paragraphStylesMap.values()) {
            this.paragraphStylesMap.put(copyParagraphStyle.getName(), new ParagraphStyle(copyParagraphStyle));
        }
        ParagraphStyle copyParagraphStyle = null;
        for (ParagraphStyle paragraphStyle: this.paragraphStylesMap.values()) {
            copyParagraphStyle = copySettings.paragraphStylesMap.get(paragraphStyle.getName());
            if (copyParagraphStyle != null) {
                if (copyParagraphStyle.getParentStyle() != null) {
                    paragraphStyle.setParentStyle(paragraphStylesMap.get(copyParagraphStyle.getParentStyle().getName()));
                }
            }
        }
        this.characterStylesMap = new TreeMap<String,CharacterStyle>();
        for (CharacterStyle copyCharacterStyle: copySettings.characterStylesMap.values()) {
            this.characterStylesMap.put(copyCharacterStyle.getName(), new CharacterStyle(copyCharacterStyle));
        }
        CharacterStyle copyCharacterStyle = null;
        for (CharacterStyle characterStyle: this.characterStylesMap.values()) {
            copyCharacterStyle = copySettings.characterStylesMap.get(characterStyle.getName());
            if (copyCharacterStyle != null) {
                if (copyCharacterStyle.getParentStyle() != null) {
                    characterStyle.setParentStyle(characterStylesMap.get(copyCharacterStyle.getParentStyle().getName()));
                }
            }
        }

        this.specialSymbolsList = new ArrayList(copySettings.specialSymbolsList.size());
        for (SpecialSymbol specialSymbol: copySettings.specialSymbolsList) {
            this.specialSymbolsList.add(new SpecialSymbol(specialSymbol));
        }
        this.headingStyles = new ArrayList(copySettings.headingStyles.size());
        for (HeadingStyle headingStyle: copySettings.headingStyles) {
            this.headingStyles.add(new HeadingStyle(headingStyle));
        }
        this.listStyles = new ArrayList(copySettings.listStyles.size());
        for (ListStyle listStyle: copySettings.listStyles) {
            this.listStyles.add(new ListStyle(listStyle));
        }
        ListStyle parentLevel = null;
        for (int i=0; i<this.listStyles.size(); i++) {
            if ((parentLevel = copySettings.listStyles.get(i).getParentLevel()) == null) {
                this.listStyles.get(i).setParentLevel(null);
            } else {
                this.listStyles.get(i).setParentLevel(this.listStyles.get(
                    copySettings.listStyles.indexOf(parentLevel)));
            }
        }

        this.tableStyle = new TableStyle(copySettings.tableStyle);
        this.frameStyle = new FrameStyle(copySettings.frameStyle);
        this.tocStyle = new TocStyle(copySettings.tocStyle);
        this.volumeInfoStyle = this.paragraphStylesMap.get(copySettings.volumeInfoStyle.getName());
        this.transcriptionInfoStyle = this.paragraphStylesMap.get(copySettings.transcriptionInfoStyle.getName());
        this.footnoteStyle = new Style(copySettings.footnoteStyle);

        this.supportedTranslationTablesGrades = new ArrayList<String>(copySettings.supportedTranslationTablesGrades);
        this.specialTranslationTables = new ArrayList<String>(copySettings.specialTranslationTables);

        this.rootSection = new Section(copySettings.rootSection);
        this.mainSections = this.rootSection.getChildren();
        this.allSections = this.rootSection.getDescendants();
        
        if (copySettings.frontMatterSection != null) {
            this.frontMatterSection = allSections.get(allSections.indexOf(copySettings.frontMatterSection));
        }
        if (copySettings.titlePageSection != null) {
            this.titlePageSection = allSections.get(allSections.indexOf(copySettings.titlePageSection));
        }
        if (copySettings.extendedFrontMatterSection != null) {
            this.extendedFrontMatterSection = allSections.get(allSections.indexOf(copySettings.extendedFrontMatterSection));
        }

        this.singleVolume = new Volume(copySettings.singleVolume);
        this.preliminaryVolume = new Volume(copySettings.preliminaryVolume);
        this.volumeSectionsMap = new HashMap<Section,Volume>();
        for (Section s : mainSections) {
            Volume v = copySettings.volumeSectionsMap.get(s);
            if (v!=null) {
                this.volumeSectionsMap.put(s, new Volume(v));
            }
        }

        logger.exiting("Settings","<init>");
    
    }

    /**
     * Creates a new <code>Settings</code> instance.
     * The {@link Locale} for the document is set to the default value.
     *
     * @param flatOdtFile       The "flat XML" .odt file.
     *                          This single file is the concatenation of all XML files in a normal .odt file.
     */
    public Settings(OdtTransformer odtTransformer)
             throws MalformedURLException,
                    IOException,
                    SAXException,
                    TransformerConfigurationException,
                    TransformerException {

        this(odtTransformer, Locale.getDefault());
        
    }

    /**
     * Creates a new <code>Settings</code> instance.
     *
     * @param   flatOdtFile     The "flat XML" .odt file.
     *                          This single file is the concatenation of all XML files in a normal .odt file.
     * @param   odtLocale       The {@link Locale} for the document.
     */
    public Settings(OdtTransformer odtTransformer,
                    Locale odtLocale)
             throws MalformedURLException,
                    IOException,
                    SAXException,
                    TransformerConfigurationException,
                    TransformerException {

        logger.entering("Settings","<init>");

        this.odtTransformer = odtTransformer;

        File odtContentFile = odtTransformer.getOdtContentFile();
        File odtStylesFile = odtTransformer.getOdtStylesFile();
        File odtMetaFile = odtTransformer.getOdtMetaFile();

        volumeManagementMode = VolumeManagementMode.SINGLE;

        rootSection = odtTransformer.extractSectionTree();
        mainSections = rootSection.getChildren();
        allSections = rootSection.getDescendants();
        volumeSectionsMap = new HashMap<Section,Volume>();

        // L10N

        L10N_in = ResourceBundle.getBundle(L10N, odtLocale).getString("in");
        L10N_and = ResourceBundle.getBundle(L10N, odtLocale).getString("and");
        L10N_volume = ResourceBundle.getBundle(L10N, odtLocale).getString("volume");
        L10N_volumes = ResourceBundle.getBundle(L10N, odtLocale).getString("volumes");
        L10N_supplement = ResourceBundle.getBundle(L10N, odtLocale).getString("supplement");
        L10N_supplements = ResourceBundle.getBundle(L10N, odtLocale).getString("supplements");
        L10N_preliminary = ResourceBundle.getBundle(L10N, odtLocale).getString("preliminary");
        L10N_transcriptionInfo = ResourceBundle.getBundle(L10N, odtLocale).getString("transcriptionInfo");
        L10N_transcribersNotesPageTitle = ResourceBundle.getBundle(L10N, odtLocale).getString("transcribersNotesPageTitle");
        L10N_specialSymbolsListTitle = ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolsListTitle");
        L10N_tableOfContentTitle = ResourceBundle.getBundle(L10N, odtLocale).getString("tableOfContentTitle");
        L10N_continuedSuffix = ResourceBundle.getBundle(L10N, odtLocale).getString("continuedSuffix");

        // Readonly properties

        PARAGRAPHS_PRESENT = XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                    "//office:body/office:text//text:p",namespace);
        HEADINGS_PRESENT = XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                    "//office:body/office:text//text:h",namespace);
        LISTS_PRESENT = XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                    "//office:body/office:text//text:list",namespace);
        TABLES_PRESENT = XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                    "//office:body/office:text//table:table",namespace);
        PAGE_NUMBER_IN_HEADER_FOOTER = XPathUtils.evaluateBoolean(odtStylesFile.toURL().openStream(),
                    "//office:master-styles//style:header/text:p/text:page-number or " +
                    "//office:master-styles//style:footer/text:p/text:page-number", namespace);
        MATH_PRESENT = XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                    "//office:body/office:text//draw:object",namespace);

        if (XPathUtils.evaluateBoolean(odtMetaFile.toURL().openStream(), "//office:meta/dc:date",namespace)) {
            DATE = XPathUtils.evaluateString(odtMetaFile.toURL().openStream(), "//office:meta/dc:date/text()",namespace).substring(0, 4);
        } else if (XPathUtils.evaluateBoolean(odtMetaFile.toURL().openStream(), "//office:meta/meta:creation-date",namespace)) {
            DATE = XPathUtils.evaluateString(odtMetaFile.toURL().openStream(), "//office:meta/meta:creation-date/text()",namespace).substring(0, 4);
        } else {
            DATE = (new SimpleDateFormat("yyyy")).format(new Date());
        }

        String[] supportedTranslationTablesGradesArray = {    /* "ar-g0",     */  "ar-g1",
                                                                 "as-g0",
                                                                 "awa-g0",
                                                                 "bg-g0",
                                                                 "bh-g0",
                                                                 "bn-g0",
                                                                 "bo-g0",
                                                                 "bra-g0",
                                                                                  "ca-g1",
                                                              /* "cs-g0",     */  "cs-g1",
                                                              /* "cy-g0",     */  "cy-g1",      "cy-g2",
                                                              /* "da-g0-8d",  */  "da-g1-8d",   "da-g2-8d",
                                                                 "de-DE-g0",      "de-DE-g1",   "de-DE-g2",
                                                                 "de-CH-g0",      "de-CH-g1",   "de-CH-g2",
                                                                 "dra-g0",
                                                              /* "el-GR-g0",  */  "el-GR-g1",
                                                                 "el-LLX-g0",
                                                              /* "en-US-g0",  */  "en-US-g1",   "en-US-g2",
                                                              /* "en-GB-g0",  */  "en-GB-g1",   "en-GB-g2",
                                                                 "en-CA-g0",
                                                              /* "en-UEB-g0", */  "en-UEB-g1",  "en-UEB-g2",
                                                                 "eo-g0",
                                                                                  "es-g1",
                                                                 "et-g0",
                                                                 "fi-g0-8d",
                                                                                  "fr-BFU-g1",  "fr-BFU-g2",
                                                                                  "fr-BFU-g1-8d",
                                                              /* "fr-FR-g0",      "fr-FR-g1",   "fr-FR-g2", */
                                                              /* "fr-CA-g0",      "fr-CA-g1",   "fr-CA-g2", */
                                                                 "ga-g0",
                                                                 "gd-g0",
                                                                 "gon-g0",
                                                                 "gu-g0",
                                                                 "he-g0",
                                                              /* "hi-g0",     */  "hi-g1",
                                                                 "hr-g0",
                                                                 "hu-g0-8d",
                                                                 "hy-g0",
                                                              /* "is-g0",     */  "is-g1",
                                                              /* "it-g0",     */  "it-g1",
                                                                 "kha-g0",
                                                                 "kn-g0",
                                                                 "kok-g0",
                                                                 "kru-g0",
                                                                 "lt-g0",
                                                              /* "lv-g0",     */  "lv-g1",
                                                                 "ml-g0",
                                                                 "mni-g0",
                                                                 "mr-g0",
                                                                 "mt-g0",                                                                 
                                                                 "mun-g0",
                                                                 "mwr-g0",
                                                                 "ne-g0",
                                                                 "new-g0",
                                                              /* "nl-NL-g0",  */  "nl-NL-g1",
                                                              /* "nl-BE-g0",  */  "nl-BE-g1",
                                                                 "no-g0",         "no-g1",      "no-g2",       "no-g3",
                                                                 "or-g0",
                                                                 "pa-g0",
                                                                 "pi-g0",
                                                              /* "pl-g0",     */  "pl-g1",
                                                              /* "pt-g0",     */  "pt-g1",      "pt-g2",
                                                                 "ro-g0",
                                                              /* "ru-g0",     */  "ru-g1",
                                                                 "sa-g0",
                                                                 "sat-g0",
                                                                 "sd-g0",
                                                              /* "sk-g0",     */  "sk-g1",
                                                              /* "sl-g0",     */  "sl-g1",
                                                              /* "sv-g0",     */  "sv-g1",      "sv-g2",
                                                                 "ta-g0",
                                                                 "te-g0",
                                                                 "tr-g0",
                                                                 "vi-g0",
                                                                 "zh-HK-g0",
                                                                 "zh-TW-g0"   };

        supportedTranslationTablesGrades = new ArrayList<String>();
        for (int i=0;i<supportedTranslationTablesGradesArray.length;i++) {
            supportedTranslationTablesGrades.add(supportedTranslationTablesGradesArray[i]);
        }

        String[] specialTranslationTablesArray = {  /* "el-es", */
                                                    /* "el-en"  */  };

        specialTranslationTables = new ArrayList();
        for (int i=0;i<specialTranslationTablesArray.length;i++) {
            specialTranslationTables.add(specialTranslationTablesArray[i]);
        }

        // Styles

        paragraphStylesMap = new TreeMap<String,ParagraphStyle>(odtTransformer.extractParagraphStyles());
        characterStylesMap = new TreeMap<String,CharacterStyle>(odtTransformer.extractCharacterStyles());
        headingStyles = new ArrayList<HeadingStyle>();
        for (int i=1; i<=10; i++) {
            headingStyles.add(new HeadingStyle(i));
        }
        listStyles = new ArrayList<ListStyle>();
        ListStyle listStyle = null;
        ListStyle parentLevel = null;
        for (int i=1; i<=10; i++) {
            listStyle = new ListStyle(i);
            if (parentLevel != null) {
                listStyle.setParentLevel(parentLevel);
            }
            listStyles.add(listStyle);
            parentLevel = listStyle;
        }
        tableStyle = new TableStyle();
        frameStyle = new FrameStyle();
        tocStyle = new TocStyle();
        setVolumeInfoStyle("Standard");
        setTranscriptionInfoStyle("Standard");
        footnoteStyle = new Style();

        // Settings

        continuedSuffix = L10N_continuedSuffix;
       
        setTranscribersNotesPageTitle(L10N_transcribersNotesPageTitle.toUpperCase());
        setSpecialSymbolsListTitle(L10N_specialSymbolsListTitle.toUpperCase());
        setTableOfContentTitle(L10N_tableOfContentTitle.toUpperCase());

        if (XPathUtils.evaluateBoolean(odtMetaFile.toURL().openStream(), "//office:meta/dc:creator",namespace)) {
            creator = XPathUtils.evaluateString(odtMetaFile.toURL().openStream(), "//office:meta/dc:creator/text()",namespace);
        } else if (XPathUtils.evaluateBoolean(odtMetaFile.toURL().openStream(), "//office:meta/meta:initial-creator",namespace)) {
            creator = XPathUtils.evaluateString(odtMetaFile.toURL().openStream(), "//office:meta/meta:initial-creator/text()",namespace);
        } else {
            creator = "";
        }

        volumeInfoEnabled = false;
        transcriptionInfoEnabled = false;

        setHyphenate(false);
        setVolumeInfoStyle(paragraphStylesMap.get("Standard"));
        setTranscriptionInfoStyle(paragraphStylesMap.get("Standard"));

        singleVolume = new Volume(Volume.Type.SINGLE, null);
        singleVolume.setTitle("(Single volume)");
        preliminaryVolume = new Volume(Volume.Type.PRELIMINARY, null);
        preliminaryVolume.setTitle(capitalizeFirstLetter(L10N_preliminary));

        boolean PRELIMINARY_PAGES_PRESENT = XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                    "//office:body/office:text/text:section[@text:name='PreliminaryPages']",namespace);
        if (PRELIMINARY_PAGES_PRESENT) {
            for (Section s : getAvailableFrontMatterSections()) {
                if (s.getName().equals("PreliminaryPages")) {
                    setFrontMatterSection(s);
                    break;
                }
            }
            for (Section s : getAvailableTitlePageSections()) {
                if (s.getName().equals("TitlePage")) {
                    Section frontmatter = getFrontMatterSection();
                    setFrontMatterSection(s);
                    setTitlePageSection(s);
                    setExtendedFrontMatterSection(frontmatter);
                    break;
                }
            }
        }

        NUMBER_OF_VOLUMES = 0;
        NUMBER_OF_SUPPLEMENTS = 0;
        if ((PRELIMINARY_PAGES_PRESENT
                && XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                        "//office:body/office:text/text:section[@text:name='PreliminaryPages']" +
                        "/following-sibling::text:section[@text:name='Volume1']",namespace))
           || (!PRELIMINARY_PAGES_PRESENT
                && XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                        "//office:body/office:text/text:sequence-decls[1]" +
                        "/following-sibling::text:section[@text:name='Volume1']",namespace))) {
            setVolumeManagementMode(VolumeManagementMode.MANUAL);
            NUMBER_OF_VOLUMES = 1;
            while(XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                    "//office:body/office:text/text:section[@text:name='Volume" + (NUMBER_OF_VOLUMES) + "']" +
                    "/following-sibling::text:section[@text:name='Volume" + (NUMBER_OF_VOLUMES + 1) + "']",namespace)) {
                NUMBER_OF_VOLUMES ++;
            }
            for (Section s : getAvailableVolumeSections()) {
                if (s.getName().startsWith("Volume")) {
                    try {
                        int nr = Integer.parseInt(s.getName().substring(6));
                        if (nr>=1 && nr<=NUMBER_OF_VOLUMES) {
                            setVolumeSection(s, true, false);
                            Volume v = getVolume(s);
                            v.setTitle(capitalizeFirstLetter(L10N_volume) + " " + nr);

                        }
                    } catch (Exception e) {}
                }
            }
            if ((XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                            "//office:body/office:text/text:section[@text:name='Volume" + NUMBER_OF_VOLUMES + "']" +
                            "/following-sibling::text:section[@text:name='Supplement1']",namespace))) {
                NUMBER_OF_SUPPLEMENTS = 1;
                while(XPathUtils.evaluateBoolean(odtContentFile.toURL().openStream(),
                        "//office:body/office:text/text:section[@text:name='Supplement" + (NUMBER_OF_SUPPLEMENTS) + "']" +
                        "/following-sibling::text:section[@text:name='Supplement" + (NUMBER_OF_SUPPLEMENTS + 1) + "']",namespace)) {
                    NUMBER_OF_SUPPLEMENTS ++;
                }
                for (Section s : getAvailableVolumeSections()) {
                    if (s.getName().startsWith("Supplement")) {
                        try {
                            int nr = Integer.parseInt(s.getName().substring(10));
                            if (nr>=1 && nr<=NUMBER_OF_SUPPLEMENTS) {
                                setVolumeSection(s, true, true);
                                Volume v = getVolume(s);
                                v.setTitle(capitalizeFirstLetter(L10N_supplement) + " " + nr);
                            }
                        } catch (Exception e) {}
                    }
                }
            }
        }

//        int volumeCount = 0;
//        for(Section section : mainSections) {
//            volumeCount ++;
//            setVolumeSection(section, true);
//            getVolume(section).setTitle(capitalizeFirstLetter(L10N_volume) + " " + volumeCount);
//        }

        transcriptionInfo = L10N_transcriptionInfo;
        volumeInfo = "@title\n@pages";
        
        beginningBraillePageNumber = 1;        
        math = DEFAULT_MATH;
        hardPageBreaks = false;
//        noterefSpaceBefore = true;
//        noterefSpaceAfter = true;
        noterefCharactersMap = new TreeMap();
        noterefNumberPrefixMap = new TreeMap();
        noterefNumberPrefixMap.put("1", "");
        noterefNumberPrefixMap.put("a", "");
        noterefNumberPrefixMap.put("A", "");
        noterefNumberPrefixMap.put("i", "");
        noterefNumberPrefixMap.put("I", "");

        setBraillePageNumberAt(PageNumberPosition.BOTTOM_RIGHT);
        setPreliminaryPageFormat(PageNumberFormat.P);
        setPrintPageNumberAt(PageNumberPosition.TOP_RIGHT);

        setBraillePageNumbers(true);
        setPrintPageNumbers(true);
        setContinuePages(true);
        setPrintPageNumberRange(false);
        setPageSeparator(true);
        setPageSeparatorNumber(true);
        setIgnoreEmptyPages(true);
        setMergeUnnumberedPages(false);
        setPageNumberAtTopOnSeparateLine(false);
        setPageNumberAtBottomOnSeparateLine(false);
        setPrintPageNumbersInToc(true);
        setBraillePageNumbersInToc(true);

        // Languages

        translationTableMap = new TreeMap();
        gradeMap = new TreeMap();
        dotsMap = new TreeMap();

        String[] languages = odtTransformer.extractLanguages();
        mainLanguage = languages[0];
        setTranslationTable(mainLanguage, mainLanguage);

        for (int i=1; i<languages.length; i++) {
            if (!translationTableMap.containsKey(languages[i])) {
                setTranslationTable(mainLanguage, languages[i]);
                setGrade(0, languages[i]);
            }
        }

        // Special Symbols
        
        specialSymbolsList = new ArrayList();
        addSpecialSymbol(new SpecialSymbol(getDefaultSpecialSymbol(SpecialSymbol.Type.ELLIPSIS, mainLanguage),
                                           ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolEllipsisDescription"),
                                           SpecialSymbol.Type.ELLIPSIS,
                                           SpecialSymbol.Mode.IF_PRESENT_IN_VOLUME));
        addSpecialSymbol(new SpecialSymbol(getDefaultSpecialSymbol(SpecialSymbol.Type.DOUBLE_DASH, mainLanguage),
                                           ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolDoubleDashDescription"),
                                           SpecialSymbol.Type.DOUBLE_DASH,
                                           SpecialSymbol.Mode.IF_PRESENT_IN_VOLUME));
        addSpecialSymbol(new SpecialSymbol(getDefaultSpecialSymbol(SpecialSymbol.Type.LETTER_INDICATOR, mainLanguage),
                                           ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolLetterIndicatorDescription"),
                                           SpecialSymbol.Type.LETTER_INDICATOR,
                                           SpecialSymbol.Mode.NEVER));
        addSpecialSymbol(new SpecialSymbol(getDefaultSpecialSymbol(SpecialSymbol.Type.NUMBER_INDICATOR, mainLanguage),
                                           ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolNumberIndicatorDescription"),
                                           SpecialSymbol.Type.NUMBER_INDICATOR,
                                           SpecialSymbol.Mode.NEVER));
        addSpecialSymbol(new SpecialSymbol(getDefaultSpecialSymbol(SpecialSymbol.Type.TRANSCRIBERS_NOTE_INDICATOR, mainLanguage),
                                           ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolTNIndicatorDescription"),
                                           SpecialSymbol.Type.TRANSCRIBERS_NOTE_INDICATOR,
                                           SpecialSymbol.Mode.IF_PRESENT_IN_VOLUME));
        addSpecialSymbol(new SpecialSymbol(getDefaultSpecialSymbol(SpecialSymbol.Type.NOTE_REFERENCE_INDICATOR, mainLanguage),
                                           ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolNoterefIndicatorDescription"),
                                           SpecialSymbol.Type.NOTE_REFERENCE_INDICATOR,
                                           SpecialSymbol.Mode.IF_PRESENT_IN_VOLUME));
        addSpecialSymbol(new SpecialSymbol(getDefaultSpecialSymbol(SpecialSymbol.Type.ITALIC_INDICATOR, mainLanguage),
                                           ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolItalicIndicatorDescription"),
                                           SpecialSymbol.Type.ITALIC_INDICATOR,
                                           SpecialSymbol.Mode.NEVER));
        addSpecialSymbol(new SpecialSymbol(getDefaultSpecialSymbol(SpecialSymbol.Type.BOLDFACE_INDICATOR, mainLanguage),
                                           ResourceBundle.getBundle(L10N, odtLocale).getString("specialSymbolBoldIndicatorDescription"),
                                           SpecialSymbol.Type.BOLDFACE_INDICATOR,
                                           SpecialSymbol.Mode.NEVER));

        // Emboss & export options

        beginningBraillePageNumber = 1;
        maxPaperWidth = Double.MAX_VALUE;
        maxPaperHeight = Double.MAX_VALUE;
        minPaperWidth = 0;
        minPaperHeight = 0;
        paperWidth = 0;
        paperHeight = 0;
        pageWidth = 0;
        pageHeight = 0;
        printablePageWidth = 0;
        printablePageHeight = 0;
        unprintableInner = 0;
        unprintableOuter = 0;
        unprintableTop = 0;
        unprintableBottom = 0;
        cellSpacing = 0;
        lineSpacing = 0;
        cellHeight = 0;
        cellWidth = 0;
        interpointShiftX = 0;
        interpointShiftY = 0;
        cellsInWidth = 0;
        linesInHeight = 0;
        cellsPerLine = 40;
        linesPerPage = 25;
        maxCellsPerLine = Integer.MAX_VALUE;
        maxLinesPerPage = Integer.MAX_VALUE;
        minCellsPerLine = 1;
        minLinesPerPage = 1;
        minMarginInner = 0;
        minMarginOuter = 0;
        minMarginTop = 0;
        minMarginBottom = 0;
        maxMarginInner = 0;
        maxMarginOuter = 0;
        maxMarginTop = 0;
        maxMarginBottom = 0;
        marginInner = 0;
        marginOuter = 0;
        marginTop = 0;
        marginBottom = 0;
        sheetsPerQuire = 1;
        saddleStitch = false;
        zFolding = false;
        eightDots = false;
        duplex = true;

        brailleFileType = DEFAULT_BRAILLE_FILE_TYPE;
        embosser = DEFAULT_EMBOSSER;
        paperSize = DEFAULT_PAPERSIZE;
        table = DEFAULT_TABLE;
        math = DEFAULT_MATH;

        setExportOrEmboss(true);
        setMultipleFilesEnabled(false);
        setBrailleRules(BrailleRules.BANA);
        setBrailleRules(BrailleRules.CUSTOM);

        logger.exiting("Settings","<init>");

    }

    /**
     * Compute the most appropriate translation table for a certain language.
     *
     * First, the list of supported translation tables is scanned for the exact language code.
     * If no match is found, only the first part of the codes (the part before the first "-", or the whole code if it doesn't contain a "-") are considered.
     * The first supported translation table in the list of which the first part matches the first part of the language code is selected.
     *
     * @param   language    The language code.
     * @return              The code of the most appropriate translation table if one is found, <code>null</code> otherwise.
     */
    private String computeTranslationTable(String language) {

        List<String> supportedTranslationTables = getSupportedTranslationTables();
        String ret = null;

        if (specialTranslationTables.contains(language)) {
            ret = language;
        } else if (supportedTranslationTables.contains(language)) {
            ret = language;
        } else {
            int pos = language.indexOf("-");
            if (pos > 0) {
                language = language.substring(0,pos);
            }

            String temp;
            for (int i=0;i<supportedTranslationTables.size();i++) {
                temp = supportedTranslationTables.get(i);
                if ((temp + "-").indexOf(language + "-") == 0) {
                    ret = temp;
                    break;
                }
            }
        }

        return ret;
    
    }

    /**
     * Compute the grade for a certain language.
     *
     * First, the list of supported grades for this language is scanned for the desired grade.
     * If no match is found, the list is scanned for ever decreasing grades.
     * If still no match is found, the first supported grade of the list is selected.
     *
     * @param   grade       The desired grade.
     * @param   language    The language code.
     * @return              The computed grade.
     */
    private int computeGrade(int grade, String language) {

        List<Integer> supportedGrades = getSupportedGrades(language);
        
        if (supportedGrades.isEmpty())       { return -1;    }
        if (supportedGrades.contains(grade)) { return grade; }

        for (int i=grade-1;i>=0;i--) {
            if (supportedGrades.contains(i)) {
                return i;
            }
        }

        return supportedGrades.get(0);
    
    }

    private int computeDots(int dots, String language) {

        List<Integer> supportedDots = getSupportedDots(language);

        if (supportedDots.isEmpty())      { return -1;   }
        if (supportedDots.contains(dots)) { return dots; }
        return supportedDots.get(0);

    }

    /**
     * Get the list of all supported translation tables.
     *
     * (Each supported translation has a code of the form "[a-z]+(-[A-Z]+)*" and
     * for each supported translation table there is at least one entry in {@link #supportedTranslationTablesGrades}
     * of the form <i>translation table code</i> + "-g[0-9]".)
     *
     * @return  The supported translation table codes.
     */
    public ArrayList<String> getSupportedTranslationTables() {
    
        ArrayList<String> supportedTranslationTables = new ArrayList();
        String translationTable = null;
        String translationTableGrade = null;

        for (int i=0;i<supportedTranslationTablesGrades.size();i++) {

            translationTableGrade = supportedTranslationTablesGrades.get(i);

            if (translationTableGrade.matches("[a-z]+(-[A-Z]+)?-g[0-9](-8d)?")) {

                translationTable = translationTableGrade.substring(0,translationTableGrade.lastIndexOf("-g"));

                if (!supportedTranslationTables.contains(translationTable)) {
                    supportedTranslationTables.add(translationTable);
                }
            }
        }

        return supportedTranslationTables;
    
    }
    
    public ArrayList<String> getSpecialTranslationTables() {
        return new ArrayList(specialTranslationTables);
    }

    /**
     * Get the list of all supported grades for a certain language.
     *
     * (For each supported grade there is an entry in {@link #supportedTranslationTablesGrades} that is equal to
     * <i>translation table code for the specified language</i> + "-g" + <i>grade</i>.)
     *
     * @param   language   The language code
     * @return             The supported grades.
     */
    public ArrayList<Integer> getSupportedGrades(String language) {

        ArrayList<Integer> supportedGrades = new ArrayList();
        String translationTableGrade = null;
        String translationTable = getTranslationTable(language);
        int grade;

        if (!specialTranslationTables.contains(translationTable)) {
            for (int i=0;i<supportedTranslationTablesGrades.size();i++) {

                translationTableGrade = supportedTranslationTablesGrades.get(i);

                if (translationTableGrade.matches(translationTable + "-g[0-9](-8d)?")) {
                    int start = translationTableGrade.lastIndexOf("-g") + 2;
                    grade = Integer.parseInt(translationTableGrade.substring(start, start + 1));
                    if (!supportedGrades.contains(grade)) {
                        supportedGrades.add(grade);
                    }
                }
            }
        }

        return supportedGrades;

    }

    public ArrayList<Integer> getSupportedDots(String language) {

        ArrayList<Integer> supportedDots = new ArrayList();
        String translationTableGrade = null;
        String translationTable = getTranslationTable(language);
        int grade = getGrade(language);

        if (!specialTranslationTables.contains(translationTable)) {
            for (int i=0;i<supportedTranslationTablesGrades.size();i++) {

                translationTableGrade = supportedTranslationTablesGrades.get(i);

                if (translationTableGrade.matches(translationTable + "-g" + grade + "(-8d)?")) {
                    if (translationTableGrade.equals(translationTable + "-g" + grade + "-8d")) {
                        supportedDots.add(8);
                    } else {
                        supportedDots.add(6);
                    }
                }
            }
        }

        return supportedDots;

    }

    /**
     * Link a translation table to a language if the desired translation table is supported. The grade is set to a default value.
     * If the desired translation table is not supported and the language is not linked to another translation table yet,
     * the most appropriate supported translation table is selected for this language.
     *
     * @param   translationTable    The desired translation table code.
     * @param   language            The language code.
     */
    public void setTranslationTable(String translationTable, String language) {
    
        String trantab = computeTranslationTable(translationTable);
        
        if (trantab!=null) {
            translationTableMap.put(language, trantab);
            setGrade(DEFAULT_GRADE,language);
        } else if (!translationTableMap.containsKey(language)) {
            translationTableMap.put(language, DEFAULT_TRANSLATION_TABLE);
            setGrade(0,language);
        }  
    }

    /**
     * Change the grade for a certain language.
     * If the language doesn't support the desired grade, the current grade is not changed.
     *
     * @param   grade     The desired grade.
     * @param   language  The language code.
     */
    public void setGrade(int grade, String language) {

        if (translationTableMap.containsKey(language)) {
            gradeMap.put(language, computeGrade(grade, language));
            setDots(6, language);
        }
    }

    public void setDots(int dots, String language) {

        if (translationTableMap.containsKey(language)) {
            dotsMap.put(language, computeDots(dots, language));
        }
    }

    /**
     * Get the translation table code for a certain language.
     * If the language is not linked to a translation table yet, the most appropriate supported translation table is selected.
     *
     * @param    language   The language code.
     * @return              The tranlation table code.
     */
    public String getTranslationTable(String language) {

        if (!translationTableMap.containsKey(language)) {
            setTranslationTable(language, language);
        }

        return translationTableMap.get(language);

    }

    /**
     * Get the grade for a certain language.
     * If the language is not linked to a translation table yet, it is linked to the most appropriate supported translation
     * and the grade is set to a default value.
     *
     * @param   language    The language code.
     * @return              The grade.
     */
    public int getGrade(String language) {

        if (!translationTableMap.containsKey(language)) {
            setTranslationTable(language, language);
        }

        return gradeMap.get(language);

    }

    public int getDots(String language) {

        if (!translationTableMap.containsKey(language)) {
            setTranslationTable(language, language);
        }

        return dotsMap.get(language);

    }

    /**
     * Get a list of all languages that are linked to a translation table
     *
     * @return  The language codes.
     */
    public ArrayList<String> getLanguages() {
        return new ArrayList(translationTableMap.keySet());
    }

    /**
     * @return  The main language code of the document.
     */
    public String getMainLanguage() {
        return mainLanguage;
    }

    public void setMultipleFilesEnabled(boolean multipleFiles) {
        this.multipleFiles = multipleFiles && exportOrEmboss;
    }
    
    public boolean getMultipleFilesEnabled() {
        return multipleFiles;
    }

    /**
     * Update the <code>exportOrEmboss</code> setting.
     *
     * @param   exportOrEmboss.
     */
    public void setExportOrEmboss(boolean exportOrEmboss) {

        this.exportOrEmboss = exportOrEmboss;

        refreshBrailleFileType();
        refreshEmbosser();
        refreshZFolding();
        refreshSaddleStitch();
        refreshDuplex();
        refreshEightDots();
        refreshTable();
        refreshPaperSize();
        try {
            refreshDimensions();
        } catch (UnsupportedPaperException ex) {}

    }

    /**
     * @param   embosser    An embosser type.
     * @return              <code>true</code> if the embosser type is supported, given the current settings.
     */
    private boolean embosserIsSupported(EmbosserType embosser) {

        if (embosser == null) { return false; }

        if (!exportOrEmboss) {
            switch (embosser) {
                case NONE:
                case INDEX_BASIC_BLUE_BAR:
                case INDEX_BASIC_S_V2:
                case INDEX_BASIC_D_V2:
                case INDEX_EVEREST_D_V2:
                case INDEX_4X4_PRO_V2:
                case INDEX_EVEREST_D_V3:
                case INDEX_BASIC_D_V3:
                case INDEX_4X4_PRO_V3:
                case INDEX_4WAVES_PRO_V3:
                case INTERPOINT_55:
                case BRAILLO_200:
                case BRAILLO_400_S:
                case BRAILLO_400_SR:
                case IMPACTO_TEXTO:
                case IMPACTO_600:
                case PORTATHIEL_BLUE:
                    return true;
                case INTERPOINT_ELEKUL_03:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @return   A list of all supported embosser types, given the current settings.
     */
    public ArrayList<EmbosserType> getSupportedEmbossers() {

        EmbosserType[] allEmbosserTypes = EmbosserType.values();
        ArrayList<EmbosserType> supportedEmbosserTypes = new ArrayList();

        for (int i=0;i<allEmbosserTypes.length;i++) {
            if (embosserIsSupported(allEmbosserTypes[i])) {
                supportedEmbosserTypes.add(allEmbosserTypes[i]);
            }
        }
        return supportedEmbosserTypes;
    }

    private void changeEmbosser(EmbosserType embosser) {

        this.embosser = embosser;

    }

    private void refreshEmbosser() {

        if (!embosserIsSupported(this.embosser)) {
            if (getSupportedEmbossers().size() > 0) {
                changeEmbosser(getSupportedEmbossers().get(0));
            } else {
                changeEmbosser(null);
            }
        }
    }

    /**
     * Update the embosser type if the desired type is supported.
     *
     * @param   embosser   The desired embosser type.
     */
    public void setEmbosser(EmbosserType embosser) throws UnsupportedPaperException {

        if (embosserIsSupported(embosser)) {

            changeEmbosser(embosser);
            refreshZFolding();
            refreshSaddleStitch();
            refreshDuplex();
            refreshEightDots();
            refreshTable();
            refreshPaperSize();
            refreshDimensions();
        }
    }

    /**
     * @return  The current embosser type.
     */
    public EmbosserType getEmbosser() {
        return embosser;
    }

    /**
     * @param   fileType        A generic braille file type.
     * @return                  <code>true</code> if the file type is supported, given the current settings.
     */
    private boolean brailleFileTypeIsSupported(BrailleFileType fileType) {

        if (fileType == null) { return false; }

        if (exportOrEmboss) {
            switch(fileType) {
                case PEF:
                case BRF:
                case BRA:
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @return   A list of all supported generic braille file types, given the current settings.
     */
    public ArrayList<BrailleFileType> getSupportedBrailleFileTypes() {

        BrailleFileType[] allBrailleFileTypes = BrailleFileType.values();
        ArrayList<BrailleFileType> supportedBrailleFileTypes = new ArrayList();

        for (int i=0;i<allBrailleFileTypes.length;i++) {
            if (brailleFileTypeIsSupported(allBrailleFileTypes[i])) {
                supportedBrailleFileTypes.add(allBrailleFileTypes[i]);
            }
        }
        return supportedBrailleFileTypes;
    }

    private void changeBrailleFileType(BrailleFileType brailleFileType) {
        this.brailleFileType = brailleFileType;
    }

    private void refreshBrailleFileType() {

        if (!brailleFileTypeIsSupported(this.brailleFileType)) {
            if (getSupportedBrailleFileTypes().size() > 0) {
                changeBrailleFileType(getSupportedBrailleFileTypes().get(0));
            } else {
                changeBrailleFileType(null);
            }
        }
    }

    /**
     * Update the braille file type if the desired type is supported.
     *
     * @param   brailleFileType  The desired braille file type.
     */
    public void setBrailleFileType(BrailleFileType brailleFileType) {

        if (brailleFileTypeIsSupported(brailleFileType)) {

            changeBrailleFileType(brailleFileType);
            refreshZFolding();
            refreshSaddleStitch();
            refreshDuplex();
            refreshEightDots();
            refreshTable();
            try { 
                refreshDimensions();
            } catch (UnsupportedPaperException ex) {}
        }
    }

    /**
     * @return  The currently selected generic braille file type.
     */
    public BrailleFileType getBrailleFileType() {
        return brailleFileType;
    }

    /**
     * @param   table   A character set.
     * @return          <code>true</code> if the character set is supported, given the current settings.
     */
    private boolean tableIsSupported (TableType table) {

        if (table == null) { return false; }

        if (exportOrEmboss) {
            switch (brailleFileType) {

                case PEF:
                    return (table==TableType.UNICODE_BRAILLE);
                case BRF:
                    return (table==TableType.EN_US ||
                            table==TableType.EN_GB ||
                            table==TableType.NL ||
                            table==TableType.EN_GB ||
                            table==TableType.DA_DK ||
                            table==TableType.DE_DE ||
                            table==TableType.IT_IT_FIRENZE ||
                            table==TableType.SV_SE_CX ||
                            table==TableType.SV_SE_MIXED ||
                            table==TableType.ES_OLD ||
                            table==TableType.ES_NEW);
                case BRA:
                    return (table==TableType.ES_OLD ||
                            table==TableType.ES_NEW);
                case BRL:
                    return (table==TableType.BRL);
                default:
                    return false;
            }

        } else {
            switch (embosser) {

                case NONE:
                    return (table==TableType.EN_US ||
                            table==TableType.EN_GB ||
                            table==TableType.NL ||
                            table==TableType.EN_GB ||
                            table==TableType.DA_DK ||
                            table==TableType.DE_DE ||
                            table==TableType.IT_IT_FIRENZE ||
                            table==TableType.SV_SE_CX ||
                            table==TableType.SV_SE_MIXED ||
                            table==TableType.ES_OLD ||
                            table==TableType.ES_NEW);
                case INTERPOINT_55:
                    return (table==TableType.EN_US);
                case INDEX_BASIC_BLUE_BAR:
                case INDEX_BASIC_S_V2:
                case INDEX_BASIC_D_V2:
                case INDEX_EVEREST_D_V2:
                case INDEX_4X4_PRO_V2:
                    if (eightDots) {
                        return (table==TableType.INDEX_TRANSPARENT_256);
                    } else {
                        return (table==TableType.INDEX_TRANSPARENT);
                    }
                case INDEX_EVEREST_D_V3:
                case INDEX_BASIC_D_V3:
                case INDEX_4X4_PRO_V3:
                case INDEX_4WAVES_PRO_V3:
                    if (eightDots) {
                        return (table==TableType.INDEX_TRANSPARENT_256);
                    } else {
                        return (table==TableType.EN_US);
                    }
                case BRAILLO_200:
                case BRAILLO_400_S:
                case BRAILLO_400_SR:
                    switch (table) {
                        case BRAILLO_6DOT_001_00:
                        case BRAILLO_6DOT_044_00:
                        case BRAILLO_6DOT_046_01:
                        case BRAILLO_6DOT_047_01:
                            return true;
                        default:
                            return false;
                    }
                case IMPACTO_600:
                case IMPACTO_TEXTO:
                case PORTATHIEL_BLUE:
                        return (table==TableType.PORTATHIEL);
                default:
                    return false;

            }
        }
    }

    /**
     * @return   A list of all supported character sets, given the current settings.
     */
    public ArrayList<TableType> getSupportedTableTypes() {

        TableType[] allTableTypes = TableType.values();
        ArrayList<TableType> supportedTableTypes = new ArrayList();

        for (int i=0;i<allTableTypes.length;i++) {
            if (tableIsSupported(allTableTypes[i])) {
                supportedTableTypes.add(allTableTypes[i]);
            }
        }
        return supportedTableTypes;
    }

    private void changeTable(TableType table) {
        this.table = table;
    }

    private void refreshTable() {

        if (!tableIsSupported(this.table)) {
            if (getSupportedTableTypes().size() > 0) {
                changeTable(getSupportedTableTypes().get(0));
            } else {
                changeTable(null);
            }
        }
    }

    /**
     * Update the character set if the desired set is supported.
     *
     * @param   table   The desired character set.
     */
    public void setTable(TableType table) {

        if (tableIsSupported(table)) {
            changeTable(table);
        }
    }

    /**
     * @return  The current character set.
     */
    public TableType getTable() {
        return table;
    }

    private boolean paperSizeIsSupported (double paperWidth,
                                          double paperHeight) {

        return (paperWidth  <= maxPaperWidth)  &&
               (paperWidth  >= minPaperWidth)  &&
               (paperHeight <= maxPaperHeight) &&
               (paperHeight >= minPaperHeight);
    }

    /**
     * @param   papersize   A paper size.
     * @return              <code>true</code> if the paper size is supported, given the current settings.
     */
    private boolean paperSizeIsSupported(PaperSize papersize) {

        if (papersize == null) { return false; }
        if (papersize == PaperSize.UNDEFINED) { return false; }

        if (!exportOrEmboss) {
            switch (embosser) {
                case NONE:
                    return true;
                case INDEX_BASIC_BLUE_BAR:
                case INDEX_BASIC_S_V2:
                case INDEX_BASIC_D_V2:
                case INDEX_BASIC_D_V3:
                    switch (papersize) {
                        case CUSTOM:
                        case W210MM_X_H10INCH:
                        case W210MM_X_H11INCH:
                        case W210MM_X_H12INCH:
                        case W240MM_X_H12INCH:
                        case W280MM_X_H12INCH:
                            break;
                        default:
                            return false;
                    }                  
            }
            if (papersize == PaperSize.CUSTOM) {
                return true;
            }
            Paper paper = Paper.newPaper(papersize);
            return paperSizeIsSupported(paper.getWidth(), paper.getHeight());
        }

        return false;
    }

    /**
     * @return   A list of all supported paper sizes, given the current settings.
     */
    public ArrayList<PaperSize> getSupportedPaperSizes() {

        PaperSize[] allPaperSizes = PaperSize.values();
        ArrayList<PaperSize> supportedPaperSizes = new ArrayList();

        for (int i=0;i<allPaperSizes.length;i++) {
            if (paperSizeIsSupported(allPaperSizes[i])) {
                supportedPaperSizes.add(allPaperSizes[i]);
            }
        }
        return supportedPaperSizes;
    }

    private void changePaperSize(double paperWidth,
                                 double paperHeight) {

        if (embosser == null) { return; }

        this.paperWidth = paperWidth;
        this.paperHeight = paperHeight;

        switch (embosser) {
            case INDEX_4X4_PRO_V2:
            case INDEX_4X4_PRO_V3:
                pageWidth = saddleStitch?paperHeight*0.5
                                        :paperHeight;
                pageHeight = paperWidth;
                break;
            case INTERPOINT_55:
                pageWidth = saddleStitch?paperWidth*0.5
                                        :paperWidth;
                pageHeight = paperHeight;
                break;
            default:
                pageWidth = paperWidth;
                pageHeight = paperHeight;
        }
        
        printablePageWidth = pageWidth;
        printablePageHeight = pageHeight;
        unprintableInner = 0;
        unprintableOuter = 0;
        unprintableTop = 0;
        unprintableBottom = 0;

        switch (embosser) {
            case INDEX_4X4_PRO_V2:
                printablePageHeight = Math.min(paperWidth, 248.5);
                break;
            case INDEX_EVEREST_D_V2:
            case INDEX_EVEREST_D_V3:
            case INDEX_BASIC_S_V2:
            case INDEX_BASIC_D_V2:
            case INDEX_BASIC_D_V3:
            case INDEX_4WAVES_PRO_V3:
                printablePageWidth = Math.min(paperWidth, 248.5);
                break;
        }

        switch (embosser) {
            case INDEX_BASIC_D_V3:
                unprintableInner = Math.max(0, paperWidth - 276.4);
                break;
            case INDEX_EVEREST_D_V3:
                unprintableInner = Math.max(0, paperWidth - 272.75);
                break;
        }

        unprintableOuter = pageWidth - printablePageWidth - unprintableInner;
        unprintableBottom = pageHeight - printablePageHeight - unprintableTop;

    }

    private void changePaperSize(PaperSize papersize) {

        this.paperSize = papersize;

        if (papersize != null) {
            switch (papersize) {
                case CUSTOM:
                    changePaperSize(
                            Math.min(Math.max(getPaperWidth(),  getMinPaperWidth()),  getMaxPaperWidth()),
                            Math.min(Math.max(getPaperHeight(), getMinPaperHeight()), getMaxPaperHeight()));
                    break;
                default:
                    Paper paper = Paper.newPaper(papersize);
                    changePaperSize(paper.getWidth(), paper.getHeight());
                    break;
            }
        }
    }

    private void refreshPaperSize() {

        if (!paperSizeIsSupported(this.paperSize)) {
            if (getSupportedPaperSizes().size() > 0) {
                changePaperSize(getSupportedPaperSizes().get(0));
            } else {
                changePaperSize(null);
            }
        } else {
            changePaperSize(this.paperSize);
        }
    }

    /**
     * Update the paper size if the desired paper size is supported.
     *
     * @param   paperSize   The desired paper size.
     */
    public void setPaperSize(PaperSize papersize) throws UnsupportedPaperException {

        if (paperSizeIsSupported(papersize)) {

            changePaperSize(papersize);
            refreshDimensions();
        }
    }

    public void setPaperSize(double paperWidth,
                             double paperHeight)
                      throws UnsupportedPaperException {

        if ((this.paperSize == PaperSize.CUSTOM) && paperSizeIsSupported(paperWidth, paperHeight)) {

            changePaperSize(paperWidth, paperHeight);
            refreshDimensions();
        }
    }

    /**
     * @return  The current paper size.
     */
    public PaperSize getPaperSize() {
        return paperSize;
    }

    public double getPaperWidth() {
        return paperWidth;
    }

    public double getPaperHeight() {
        return paperHeight;
    }

    public double getMaxPaperWidth() {        
        return maxPaperWidth;
    }

    public double getMaxPaperHeight() {
        return maxPaperHeight;
    }

    public double getMinPaperWidth() {
        return minPaperWidth;
    }

    public double getMinPaperHeight() {
        return minPaperHeight;
    }

    public double getPageWidth() {
        return pageWidth;
    }

    public double getPageHeight() {
        return pageHeight;
    }

    /**
     * @return   <code>true</code> if recto-verso is supported, given the current settings.
     */
    public boolean duplexIsSupported(boolean duplex) {

        if (exportOrEmboss) {
            if (brailleFileType == null) { return false; }
            switch (brailleFileType) {
                case PEF:
                    return true;
                default:
                    return !duplex;
            }
        } else {
            if (embosser == null) { return false; }
            if (getSaddleStitch()) {
                return duplex;
            } else {
                switch (embosser) {
                    case NONE:
                        return true;
                    case INDEX_BASIC_D_V2:
                        return !getZFolding() || duplex;
                    case INDEX_EVEREST_D_V2:
                    case INDEX_4X4_PRO_V2:
                    case INDEX_EVEREST_D_V3:
                    case INDEX_BASIC_D_V3:
                    case INDEX_4X4_PRO_V3:
                    case INDEX_4WAVES_PRO_V3:
                    case BRAILLO_200:
                    case BRAILLO_400_S:
                    case BRAILLO_400_SR:
                    case INTERPOINT_55:
                    case IMPACTO_TEXTO:
                    case IMPACTO_600:
                        return true;
                    case INDEX_BASIC_BLUE_BAR:
                    case INDEX_BASIC_S_V2:
                    case INDEX_EVEREST_S_V1:
                    case PORTATHIEL_BLUE:
                    default:
                        return !duplex;

                }
            }
        }
    }

    private void changeDuplex(boolean duplex) {
        this.duplex = duplex;
    }

    private void refreshDuplex() {

        if (!duplexIsSupported(duplex) &&
             duplexIsSupported(!duplex)) {
            changeDuplex(!duplex);
        }
    }

    public void setDuplex(boolean duplex)
                   throws UnsupportedPaperException {

        if (duplexIsSupported(duplex)) {
            changeDuplex(duplex);
            refreshDimensions();
        }
    }

    /**
     * @return  <code>true</code> if recto-verso is enabled.
     */
    public boolean getDuplex() {
        return duplex;
    }

    public boolean saddleStitchIsSupported() {

        if (exportOrEmboss) {
            return false;
        } else {
            if (embosser==null) { return false; }
            switch (embosser) {
                case INTERPOINT_55:
                case INDEX_4X4_PRO_V2: 
                case INDEX_4X4_PRO_V3:
                    return true;
                default:
                    return false;
            }
        }
    }
    
    private void changeSaddleStitch(boolean saddleStitch) {

        this.saddleStitch = saddleStitch;

        maxPaperWidth = Double.MAX_VALUE;
        maxPaperHeight = Double.MAX_VALUE;
        minPaperWidth = 50d;
        minPaperHeight = 50d;

        if (embosser!=null) {
            switch (embosser) {
                case INDEX_BASIC_BLUE_BAR:
                    //minPaperWidth = ?
                    //minPaperHeight = ?
                    maxPaperWidth = 280d;
                    maxPaperHeight = 12*Paper.INCH_IN_MM;
                    break;
                case INDEX_BASIC_S_V2:
                case INDEX_BASIC_D_V2:
                    minPaperWidth = 138d; // = 23*6
                    minPaperHeight = 1*Paper.INCH_IN_MM;
                    //maxPaperWidth = ?
                    maxPaperHeight = (20+2/3)*Paper.INCH_IN_MM;
                    break;
                case INDEX_EVEREST_D_V2:
                    minPaperWidth = 138d; // = 23*6
                    minPaperHeight = 100d;
                    //maxPaperWidth = ?
                    maxPaperHeight = 350d;
                    break;
                case INDEX_4X4_PRO_V2:
                    minPaperWidth = 100d;
                    minPaperHeight = Math.max(110d, saddleStitch?276d:138d); // = 23*6(*2)
                    maxPaperWidth = 297d;
                    maxPaperHeight = 500d;
                    break;
                case INDEX_EVEREST_D_V3:
                case INDEX_4X4_PRO_V3:
                    minPaperWidth = 130d;
                    minPaperHeight = 120d;
                    maxPaperWidth = 297d;
                    maxPaperHeight = 585d;
                    break;
                case INDEX_BASIC_D_V3:
                    minPaperWidth = 90d;
                    minPaperHeight = 1*Paper.INCH_IN_MM;
                    maxPaperWidth = 295d;
                    maxPaperHeight = 17*Paper.INCH_IN_MM;
                    break;
                case INDEX_4WAVES_PRO_V3:
                    minPaperWidth = 90d;
                    minPaperHeight = 11*Paper.INCH_IN_MM;
                    maxPaperWidth = 295d;
                    maxPaperHeight = 12*Paper.INCH_IN_MM;
                    break;
                case BRAILLO_200:
                case BRAILLO_400_S:
                case BRAILLO_400_SR:
                    maxPaperWidth = 43*(6d);
                    maxPaperHeight = 14*Paper.INCH_IN_MM;
                    minPaperWidth = 9*(10d);
                    minPaperHeight = 3.5*Paper.INCH_IN_MM;
                    break;
                case INTERPOINT_55:
                    maxPaperHeight = 340d;
                    //minPaperHeight = ?
                    //maxPaperWidth = ?
                    //maxPaperHeight = ?
                    break;
                case IMPACTO_TEXTO:
                case IMPACTO_600:
                    maxPaperWidth = 42*(0.25*Paper.INCH_IN_MM);
                    maxPaperHeight = 13*Paper.INCH_IN_MM;
                    minPaperWidth = 12*(0.25*Paper.INCH_IN_MM);
                    minPaperHeight = 6*Paper.INCH_IN_MM;
                    break;
                case PORTATHIEL_BLUE:
                    maxPaperWidth = 42*(0.25*Paper.INCH_IN_MM);
                    maxPaperHeight = 13*Paper.INCH_IN_MM;
                    minPaperWidth = 10*(0.25*Paper.INCH_IN_MM);
                    minPaperHeight = 8*Paper.INCH_IN_MM;
                    break;
                case NONE:
                default:
            }
        }
    }
    
    private void refreshSaddleStitch() {

        changeSaddleStitch(saddleStitch && saddleStitchIsSupported());
    }

    public void setSaddleStitch(boolean saddleStitch)
                         throws UnsupportedPaperException {

        if (saddleStitchIsSupported()) {
            changeSaddleStitch(saddleStitch);
            refreshDuplex();
            refreshPaperSize();
            refreshDimensions();
        }
    }

    public boolean getSaddleStitch() {
        return saddleStitch;
    }

    public boolean sheetsPerQuireIsSupported() {
    
        return (!exportOrEmboss && 
                embosser == EmbosserType.INTERPOINT_55);
    }

    public void setSheetsPerQuire(int sheetsPerQuire) {

        if (zFoldingIsSupported() && sheetsPerQuire > 0) {
            this.sheetsPerQuire = sheetsPerQuire;
        }
    }

    public int getSheetsPerQuire() {
        return sheetsPerQuire;
    }

    public boolean zFoldingIsSupported() {

        if (exportOrEmboss) {
            return false;
        } else {
            if (embosser == null) { return false; }
            switch (embosser) {
                case INDEX_BASIC_D_V2:
                case INDEX_BASIC_D_V3:
                case INDEX_4WAVES_PRO_V3:
                    return true;
                default:
                    return false;
            }
        }
    }

    private void changeZFolding(boolean zFolding) {
        this.zFolding = zFolding;
    }

    private void refreshZFolding() {
        changeZFolding(zFolding && zFoldingIsSupported());
    }

    public void setZFolding(boolean zFolding)
                     throws UnsupportedPaperException {

        if (zFoldingIsSupported()) {
            changeZFolding(zFolding);
            refreshDuplex();
            refreshDimensions();
        }
    }

    public boolean getZFolding() {
        return zFolding;
    }

    public boolean eightDotsIsSupported() {

        if (exportOrEmboss) {
            if (brailleFileType==null) { return false; }
            switch (brailleFileType) {
                case PEF:
                    return true;
                default:
                    return false;

            }
        } else {
            if (embosser==null) { return false; }
            switch (embosser) {
                case IMPACTO_TEXTO:
                case IMPACTO_600:
                case PORTATHIEL_BLUE:
                    return false;
                    //return true;
                case INDEX_BASIC_S_V2:
                case INDEX_BASIC_D_V2:
                case INDEX_EVEREST_D_V2:
                case INDEX_4X4_PRO_V2:
                case INDEX_EVEREST_D_V3:
                case INDEX_BASIC_D_V3:
                case INDEX_4X4_PRO_V3:
                case INDEX_4WAVES_PRO_V3:
                case INTERPOINT_55:
                case INDEX_BASIC_BLUE_BAR:
                case BRAILLO_200:
                case BRAILLO_400_S:
                case BRAILLO_400_SR:                
                default:
                    return false;
            }
        }
    }

    private void changeEightDots(boolean eightDots) {

        this.eightDots = eightDots;

        if (embosser==null) { return; }

        cellWidth = cellSpacing = 6d;
        cellHeight = lineSpacing = 10d;
        interpointShiftX = 0;
        interpointShiftY = 0;

        switch (embosser) {
            case INDEX_BASIC_BLUE_BAR:
                cellWidth = cellSpacing = 6.1d;
                break;
            case INDEX_4X4_PRO_V3:
                interpointShiftX = (2.5d)/2;
                interpointShiftY = (2.5d)*3/2;
            case INDEX_BASIC_S_V2:
            case INDEX_BASIC_D_V2:
            case INDEX_EVEREST_D_V2:
            case INDEX_4X4_PRO_V2:
            case INDEX_BASIC_D_V3:
            case INDEX_EVEREST_D_V3:
            case INDEX_4WAVES_PRO_V3:
                cellSpacing = 6d;
                lineSpacing = ((eightDots?3:2)+2)*(2.5d);
                cellWidth = 2.5d;
                cellHeight = (eightDots?3:2)*(2.5d);
                break;
            case IMPACTO_600:
            case IMPACTO_TEXTO:
            case PORTATHIEL_BLUE:
                cellWidth = cellSpacing = 0.25*Paper.INCH_IN_MM;
                cellHeight = lineSpacing = ((eightDots?3:2)+2)*(0.1*Paper.INCH_IN_MM);
                break;
            case BRAILLO_200:
            case BRAILLO_400_S:
            case BRAILLO_400_SR:
            case INTERPOINT_55:
            default:
                cellSpacing = 6d;
                lineSpacing = 10d;
                break;
        }
    }

    private void refreshEightDots() {
        changeEightDots(eightDots && eightDotsIsSupported());
    }

    public void setEightDots(boolean eightDots)
                      throws UnsupportedPaperException {

        if (eightDotsIsSupported()) {

            changeEightDots(eightDots);
            refreshTable();
            refreshDimensions();
        }
    }

    public boolean getEightDots() {
        return eightDots;
    }

    private void refreshDimensions() throws UnsupportedPaperException {

        if (paperSize == null) {

            maxCellsPerLine = Integer.MAX_VALUE;
            maxLinesPerPage = Integer.MAX_VALUE;
            maxMarginInner = Integer.MAX_VALUE;
            maxMarginTop = Integer.MAX_VALUE;
            maxMarginOuter = Integer.MAX_VALUE;
            maxMarginBottom = Integer.MAX_VALUE;
            minCellsPerLine = 1;
            minLinesPerPage = 1;
            minMarginInner = 0;
            minMarginTop = 0;
            minMarginOuter = 0;
            minMarginBottom = 0;

        } else {

            if (embosser == null) { return; }

            int maxCellsInWidth = Integer.MAX_VALUE;
            int maxLinesInHeight = Integer.MAX_VALUE;
            int minCellsInWidth = 0;
            int minLinesInHeight = 0;

            switch (embosser) {
                case INDEX_BASIC_S_V2:
                case INDEX_BASIC_D_V2:
                case INDEX_EVEREST_D_V2:
                case INDEX_4X4_PRO_V2:
                    minCellsInWidth = 23;
                case INDEX_4X4_PRO_V3:
                case INDEX_BASIC_D_V3:
                case INDEX_EVEREST_D_V3:
                case INDEX_4WAVES_PRO_V3:
                    maxCellsInWidth = 42;
                    break;
            }

            printablePageWidth  = Math.min(printablePageWidth,  maxCellsInWidth  * cellSpacing);
            printablePageHeight = Math.min(printablePageHeight, maxLinesInHeight * lineSpacing);
            unprintableOuter = pageWidth - printablePageWidth - unprintableInner;
            unprintableBottom = pageHeight - printablePageHeight - unprintableTop;
            
            cellsInWidth =  (int)Math.floor(printablePageWidth / cellSpacing);
            linesInHeight = (int)Math.floor(printablePageHeight / lineSpacing);

            if (cellsInWidth  < minCellsInWidth  ||
                linesInHeight < minLinesInHeight ||
                cellsInWidth  > maxCellsInWidth  ||
                linesInHeight > maxLinesInHeight) {

                throw new UnsupportedPaperException();
            }

            maxCellsPerLine = cellsInWidth;
            maxLinesPerPage = linesInHeight;
            maxMarginInner = cellsInWidth;
            maxMarginOuter = cellsInWidth;
            maxMarginTop = linesInHeight;
            maxMarginBottom = linesInHeight;
            minCellsPerLine = 1;
            minLinesPerPage = 1;
            minMarginInner = 0;
            minMarginOuter = 0;
            minMarginTop = 0;
            minMarginBottom = 0;

            switch (embosser) {
                case INDEX_4X4_PRO_V3:
                    if (unprintableInner == 0) {
                        minMarginInner = 1;
                    }
                    if (unprintableTop == 0 && duplex) {
                        minMarginTop = 1;
                    }
                case INDEX_BASIC_D_V3:
                case INDEX_EVEREST_D_V3:
                case INDEX_4WAVES_PRO_V3:
                    maxMarginInner = 10;
                    maxMarginOuter = 10;
                    maxMarginTop = 10;
                    break;
            }

            int tempMaxCellsPerLine = maxCellsPerLine;
            int tempMaxLinesPerPage = maxLinesPerPage;
            int tempMaxMarginInner = maxMarginInner;
            int tempMaxMarginOuter = maxMarginOuter;
            int tempMaxMarginTop = maxMarginTop;
            int tempMaxMarginBottom = maxMarginBottom;
            int tempMinCellsPerLine = minCellsPerLine;
            int tempMinLinesPerPage = minLinesPerPage;
            int tempMinMarginInner = minMarginInner;
            int tempMinMarginOuter = minMarginOuter;
            int tempMinMarginTop = minMarginTop;
            int tempMinMarginBottom = minMarginBottom;

            maxCellsPerLine = (int)Math.min(tempMaxCellsPerLine, cellsInWidth  - tempMinMarginInner  - tempMinMarginOuter);
            maxMarginInner =  (int)Math.min(tempMaxMarginInner,  cellsInWidth  - tempMinCellsPerLine - tempMinMarginOuter);
            maxMarginOuter =  (int)Math.min(tempMaxMarginOuter,  cellsInWidth  - tempMinCellsPerLine - tempMinMarginInner);
            minCellsPerLine = (int)Math.max(tempMinCellsPerLine, cellsInWidth  - tempMaxMarginInner  - tempMaxMarginOuter);
            minMarginInner =  (int)Math.max(tempMinMarginInner,  cellsInWidth  - tempMaxCellsPerLine - tempMaxMarginOuter);
            minMarginOuter =  (int)Math.max(tempMinMarginOuter,  cellsInWidth  - tempMaxCellsPerLine - tempMinMarginInner);
            maxLinesPerPage = (int)Math.min(tempMaxLinesPerPage, linesInHeight - tempMinMarginTop    - tempMinMarginBottom);
            maxMarginTop =    (int)Math.min(tempMaxMarginTop,    linesInHeight - tempMinLinesPerPage - tempMinMarginBottom);
            maxMarginBottom = (int)Math.min(tempMaxMarginBottom, linesInHeight - tempMinLinesPerPage - tempMinMarginTop);
            minLinesPerPage = (int)Math.max(tempMinLinesPerPage, linesInHeight - tempMaxMarginTop    - tempMaxMarginBottom);
            minMarginTop =    (int)Math.max(tempMinMarginTop,    linesInHeight - tempMaxLinesPerPage - tempMaxMarginBottom);
            minMarginBottom = (int)Math.max(tempMinMarginBottom, linesInHeight - tempMaxLinesPerPage - tempMaxMarginTop);

            if (minCellsPerLine > maxCellsPerLine ||
                minLinesPerPage > maxLinesPerPage ||
                minMarginInner  > maxMarginInner  ||
                minMarginOuter  > maxMarginOuter  ||
                minMarginTop    > maxMarginTop    ||
                minMarginBottom > maxMarginBottom) {

                throw new UnsupportedPaperException();
            }
        }

        setCellsPerLine(cellsPerLine);
        setLinesPerPage(linesPerPage);
    }

    /**
     * Set the number of cells per line to the desired value. If this is not possible, it will be set to the most nearby value.
     * The left and right margins are updated accordingly.
     *
     * @param   cells  The desired value.
     */
    public void setCellsPerLine(int cells) {

        cellsPerLine = Math.min(maxCellsPerLine, Math.max(minCellsPerLine, cells));

        if (marginsSupported()) {
            marginOuter = Math.min(maxMarginOuter,  Math.max(minMarginOuter, cellsInWidth - cellsPerLine - marginInner));
            marginInner = Math.min(maxMarginInner,  Math.max(minMarginInner, cellsInWidth - cellsPerLine - marginOuter));
            marginOuter = cellsInWidth - cellsPerLine - marginInner;
        } else {
            marginInner = 0;
            marginOuter = 0;
        }
    }

    /**
     * Set the number of lines per page to the desired value. If this is not possible, it will be set to the most nearby value.
     * The top and bottom margins are updated accordingly.
     *
     * @param   lines  The desired value.
     */
    public void setLinesPerPage(int lines) {

        linesPerPage = Math.min(maxLinesPerPage, Math.max(minLinesPerPage, lines));

        if (marginsSupported()) {
            marginBottom = Math.min(maxMarginBottom, Math.max(minMarginBottom, linesInHeight - linesPerPage - marginTop));
            marginTop =    Math.min(maxMarginTop,    Math.max(minMarginTop,    linesInHeight - linesPerPage - marginBottom));
            marginBottom = linesInHeight - linesPerPage - marginTop;
        } else {
            marginTop = 0;
            marginBottom = 0;
        }
    }

    public int getCellsInWidth() {
        return cellsInWidth;
    }
    
    public int getLinesInHeight() {
        return linesInHeight;
    }

    /**
     * @return  The current number of cells per line.
     */
    public int getCellsPerLine() {
        return cellsPerLine;
    }

    /**
     * @return  The current number of lines per page.
     */
    public int getLinesPerPage() {
        return linesPerPage;
    }

    /**
     * @return  The maximum number of lines that can be fit on a page, given the current paper size.
     */
    public int getMaxCellsPerLine() {
        return maxCellsPerLine;
    }

    /**
     * @return  The maximum number of cells that can be fit on a line, given the current paper size.
     */
    public int getMaxLinesPerPage() {
        return maxLinesPerPage;
    }

    /**
     * @return  The minimum number of cells per line.
     */
    public int getMinCellsPerLine() {
        return minCellsPerLine;
    }

    /**
     * @return  The minimum number of lines per page.
     */
    public int getMinLinesPerPage() {
        return minLinesPerPage;
    }

    /**
     * @return   <code>true</code> if margins are supported, given the current settings.
     */
    public boolean marginsSupported() {
        return (paperSize!=null);
    }

    /**
     * Set the left margin to the desired value. If this is not possible, it will be set to the most nearby value.
     * The right margin and number of cells per line are updated accordingly.
     *
     * @param   margin  The desired value.
     */
    public void setMarginInner(int margin) {

        if (marginsSupported()) {
            marginInner =  Math.min(maxMarginInner,  Math.max(minMarginInner,  margin));
            marginOuter =  Math.min(maxMarginOuter,  Math.max(minMarginOuter,  cellsInWidth - marginInner - cellsPerLine));
            cellsPerLine = Math.max(minCellsPerLine, Math.min(maxCellsPerLine, cellsInWidth - marginInner - marginOuter));
            marginOuter =  cellsInWidth - cellsPerLine - marginInner;
        } else {
            marginInner = 0;
        }
    }

    /**
     * Set the right margin to the desired value. If this is not possible, it will be set to the most nearby value.
     * The left margin and number of cells per line are updated accordingly.
     *
     * @param   margin  The desired value.
     */
    public void setMarginOuter(int margin) {

        if (marginsSupported()) {
            marginOuter =  Math.min(maxMarginOuter,  Math.max(minMarginOuter,  margin));
            marginInner =  Math.min(maxMarginInner,  Math.max(minMarginInner,  cellsInWidth - marginOuter - cellsPerLine));
            cellsPerLine = Math.max(minCellsPerLine, Math.min(maxCellsPerLine, cellsInWidth - marginOuter - marginInner));
            marginInner =  cellsInWidth - cellsPerLine - marginOuter;
        } else {
            marginOuter = 0;
        }
    }

    /**
     * Set the top margin to the desired value. If this is not possible, it will be set to the most nearby value.
     * The bottom margin and number of lines per page are updated accordingly.
     *
     * @param   margin  The desired value.
     */
    public void setMarginTop(int margin) {

        if (marginsSupported()) {
            marginTop =    Math.min(maxMarginTop,    Math.max(minMarginTop,    margin));
            marginBottom = Math.min(maxMarginBottom, Math.max(minMarginBottom, linesInHeight - marginTop - linesPerPage));
            linesPerPage = Math.max(minLinesPerPage, Math.min(maxLinesPerPage, linesInHeight - marginTop - marginBottom));
            marginBottom = linesInHeight - linesPerPage - marginTop;
        } else {
            marginTop = 0;
        }
    }

    /**
     * Set the bottom margin to the desired value. If this is not possible, it will be set to the most nearby value.
     * The top margin and number of lines per page are updated accordingly.
     *
     * @param   margin  The desired value.
     */
    public void setMarginBottom(int margin) {

        if (marginsSupported()) {
            marginBottom = Math.min(maxMarginBottom, Math.max(minMarginBottom, margin));
            marginTop =    Math.min(maxMarginTop,    Math.max(minMarginTop,    linesInHeight - marginBottom - linesPerPage));
            linesPerPage = Math.max(minLinesPerPage, Math.min(maxLinesPerPage, linesInHeight - marginBottom - marginTop));
            marginTop =    linesInHeight - linesPerPage - marginBottom;
        } else {
            marginBottom = 0;
        }
    }

    public int getMarginInner() {
        return marginInner;
    }

    public int getMarginOuter() {
        return marginOuter;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public int getMarginInnerOffset() {
        return (int)Math.floor(unprintableInner / cellSpacing);
    }

    public int getMarginOuterOffset() {
        return (int)Math.floor(unprintableOuter / cellSpacing);
    }

    public int getMarginTopOffset() {
        return (int)Math.floor(unprintableTop / lineSpacing);
    }

    public int getMarginBottomOffset() {
        return (int)Math.floor(unprintableBottom / lineSpacing);
    }

    /**
     * @return  The minimum left margin.
     */
    public int getMinMarginInner() {
        return minMarginInner;
    }

    /**
     * @return  The minimum right margin.
     */
    public int getMinMarginOuter() {
        return minMarginOuter;
    }

    /**
     * @return  The minimum top margin.
     */
    public int getMinMarginTop() {
        return minMarginTop;
    }

    /**
     * @return  The minimum bottom margin.
     */
    public int getMinMarginBottom() {
        return minMarginBottom;
    }

    /**
     * @return  The maximum left margin.
     */
    public int getMaxMarginInner() {
        return maxMarginInner;
    }

    /**
     * @return  The maximum right margin.
     */
    public int getMaxMarginOuter() {
        return maxMarginOuter;
    }

    /**
     * @return  The maximum top margin.
     */
    public int getMaxMarginTop() {
        return maxMarginTop;
    }

    /**
     * @return  The maximum bottom margin.
     */
    public int getMaxMarginBottom() {
        return maxMarginBottom;
    }

    /**
     * @return  The spacing between successive braille cells, given the current embosser type.
     */
    public double getCellSpacing() {
        return cellSpacing;
    }

    /**
     * @return  The spacing between successive braille lines, given the current embosser type.
     */
    public double getLineSpacing() {
        return lineSpacing;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    public double getCellHeight() {
        return cellHeight;
    }

    /**
     * @param stairstep
     */
    public void setStairstepTable(boolean stairstep) {
        tableStyle.setStairstepTable(stairstep);
    }

    /**
     * @return
     */
    public boolean stairstepTableIsEnabled() {
        return tableStyle.getStairstepTable();
    }

    /**
     * @param delimiter
     */
    public void setColumnDelimiter(String delimiter) {
        tableStyle.setColumnDelimiter(delimiter);
    }

    /**
     * @return
     */
    public String getColumnDelimiter() {
        return tableStyle.getColumnDelimiter();
    }

    /**
     * @param   symbol
     * @return  <code>true</code> if lineFillSymbol is in [U+2801,U+283F].
     */
    public boolean setLineFillSymbol(char symbol) {
        return tocStyle.setLineFillSymbol(symbol);
    }

    public char getLineFillSymbol() {
        return tocStyle.getLineFillSymbol();
    }

    public void setMath(MathType math) {
        this.math = math;
    }

    public MathType getMath() {
        return math;
    }

    public void setPrintPageNumbers(boolean b) {

        this.printPageNumbers = b && PAGE_NUMBER_IN_HEADER_FOOTER;
        setPrintPageNumberRange(printPageNumberRange);
        setContinuePages(continuePages);
        setPageNumberAtTopOnSeparateLine(pageNumberAtTopOnSeparateLine);
        setPageSeparatorNumber(pageSeparatorNumber);
        setPrintPageNumbersInToc(tocStyle.getPrintPageNumbers());

    }
    
    public void setBraillePageNumbers(boolean b) {

        this.braillePageNumbers = b;
        setBraillePageNumbersInToc(tocStyle.getBraillePageNumbers());

    }
    
    public void setPageSeparator(boolean b) {

        this.pageSeparator = b;
        setPageSeparatorNumber(pageSeparatorNumber);

    }
    
    public void setPageSeparatorNumber(boolean b) {

        this.pageSeparatorNumber = b && printPageNumbers && pageSeparator;

    }
    
    public void setContinuePages(boolean b) {
        this.continuePages = b && printPageNumbers;
    }
    
    public void setIgnoreEmptyPages(boolean b) {
        this.ignoreEmptyPages = b;
    }
    
    public void setMergeUnnumberedPages(boolean b) {
        this.mergeUnnumberedPages = b;
    }
    
    public void setPageNumberAtTopOnSeparateLine(boolean b) {

        this.pageNumberAtTopOnSeparateLine = b
                || (printPageNumbers && (printPageNumberAt == PageNumberPosition.TOP_RIGHT
                                      || printPageNumberAt == PageNumberPosition.TOP_LEFT) && printPageNumberRange);

    }
    
    public void setPageNumberAtBottomOnSeparateLine(boolean b) {
        this.pageNumberAtBottomOnSeparateLine = b;
    }
    
    public void setPrintPageNumberRange(boolean b) {

        this.printPageNumberRange = b && this.printPageNumbers;
        setPageNumberAtTopOnSeparateLine(pageNumberAtTopOnSeparateLine);

    }
    
    public void setPrintPageNumberAt(PageNumberPosition at) {
        
        if (at == PageNumberPosition.TOP_RIGHT || at == PageNumberPosition.BOTTOM_RIGHT) {
            this.printPageNumberAt = at;
            setPageNumberAtTopOnSeparateLine(pageNumberAtTopOnSeparateLine);
        }
    }
    
    public void setBraillePageNumberAt(PageNumberPosition at) {
        
        if (at == PageNumberPosition.TOP_RIGHT || at == PageNumberPosition.BOTTOM_RIGHT) {
            this.braillePageNumberAt = at;
        }
    }

    public void setPreliminaryPageFormat(PageNumberFormat format) {

        if (format == PageNumberFormat.P || format == PageNumberFormat.ROMAN ) {
            this.preliminaryPageNumberFormat = format;
        }
    }

    public void setBeginningBraillePageNumber(int number) {

        if (number > 0) {
            this.beginningBraillePageNumber = number;
        }
    }

    public void setPrintPageNumbersInToc(boolean b) {
        tocStyle.setPrintPageNumbers(b && this.printPageNumbers);
    }

    public void setBraillePageNumbersInToc(boolean b) {
        tocStyle.setBraillePageNumbers(b && this.braillePageNumbers);
    }

    public boolean getPrintPageNumbers() {
        return this.printPageNumbers;
    }

    public boolean getBraillePageNumbers() {
        return this.braillePageNumbers;
    }

    public boolean getPageSeparator() {
        return this.pageSeparator;
    }

    public boolean getPageSeparatorNumber() {
        return this.pageSeparatorNumber;
    }

    public boolean getContinuePages() {
        return this.continuePages;
    }

    public boolean getIgnoreEmptyPages() {
        return this.ignoreEmptyPages;
    }

    public boolean getMergeUnnumberedPages() {
        return this.mergeUnnumberedPages;
    }

    public boolean getPageNumberAtTopOnSeparateLine() {
        return this.pageNumberAtTopOnSeparateLine;
    }

    public boolean getPageNumberAtBottomOnSeparateLine() {
        return this.pageNumberAtBottomOnSeparateLine;
    }

    public boolean getPrintPageNumberRange() {
        return this.printPageNumberRange;
    }

    public PageNumberPosition getPrintPageNumberAt() {
        return this.printPageNumberAt;
    }

    public PageNumberPosition getBraillePageNumberAt() {
        return this.braillePageNumberAt;
    }

    public PageNumberFormat getPreliminaryPageFormat() {
        return this.preliminaryPageNumberFormat;
    }

    public int getBeginningBraillePageNumber() {
        return this.beginningBraillePageNumber;
    }

    public PageNumberFormat getSupplementaryPageFormat() {
        return this.supplementaryPageNumberFormat;
    }

    public boolean getPrintPageNumbersInToc() {
        return tocStyle.getPrintPageNumbers();
    }

    public boolean getBraillePageNumbersInToc() {
        return tocStyle.getBraillePageNumbers();
    }

    public void setHardPageBreaks(boolean b) {
        this.hardPageBreaks = b;
    }

    public boolean getHardPageBreaks() {
        return this.hardPageBreaks;
    }

    public boolean setCreator(String creator) {
        if (creator.length() > 0) {
            this.creator = creator;
            return true;
        }
        return false;
    }

    public String getCreator() {
        return this.creator;
    }

    public void setHyphenate(boolean hyphenate) {
        this.hyphenate = hyphenate;
    }

    public boolean getHyphenate() {
        return this.hyphenate;
    }

    public List<SpecialSymbol> getSpecialSymbolsList() {
        return specialSymbolsList;
    }

    public SpecialSymbol getSpecialSymbol(int index) {
        return specialSymbolsList.get(index);
    }

    public void setSpecialSymbol(SpecialSymbol specialSymbol, int index) {
        specialSymbolsList.set(index, specialSymbol);
    }

    private void addSpecialSymbol(SpecialSymbol specialSymbol) {
        specialSymbolsList.add(specialSymbol);
    }

    public int addSpecialSymbol() {
        specialSymbolsList.add(new SpecialSymbol());
        return specialSymbolsList.size()-1;
    }

    public int deleteSpecialSymbol(int index) {
        specialSymbolsList.remove(index);
        return Math.min(index, specialSymbolsList.size()-1);
    }

    public int moveSpecialSymbolUp(int index) {

        try {
            Collections.swap(specialSymbolsList, index, index-1);
            return index-1;
        } catch (IndexOutOfBoundsException e) {
            return index;
        }
    }

    public int moveSpecialSymbolDown(int index) {
        try {
            Collections.swap(specialSymbolsList, index, index+1);
            return index+1;
        } catch (IndexOutOfBoundsException e) {
            return index;
        }
    }
    
    public boolean setVolumeManagementMode(VolumeManagementMode mode) {

        if (mode == VolumeManagementMode.MANUAL && mainSections.size() < 1) {
            return false;
        }
        volumeManagementMode = mode;
        return true;
    }

    public VolumeManagementMode getVolumeManagementMode() {
        return volumeManagementMode;
    }

    public List<Volume> getAutomaticVolumes() {

        if (automaticVolumes==null) {

            try {

                int[] allPages = odtTransformer.extractDocumentOutline(this);
                int[] optimalVolumes = computeOptimalVolumeSizes(allPages);



            } catch (Exception e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        return automaticVolumes;
    }

    public Volume getPreliminaryVolume() {
        return preliminaryVolume;
    }

    public Volume getVolume(Section section) {
        return volumeSectionsMap.get(section);
    }

    public List<Volume> getVolumes() {

        Volume volume = null;
        List<Volume> volumes = new ArrayList<Volume>();
        if (getPreliminaryVolumeEnabled()) {
            volumes.add(preliminaryVolume);
        }

        switch (volumeManagementMode) {
            case MANUAL:
                for (Section section : getAvailableVolumeSections()) {
                    volume = volumeSectionsMap.get(section);
                    if (volume != null) {
                        volumes.add(volume);
                    }
                }
                break;
            case SINGLE:
                volumes.add(singleVolume);
                break;
            case AUTOMATIC:
                volumes.addAll(getAutomaticVolumes());
                break;
        }
        return volumes;
    }

    public void setVolumeSection(Section section,
                                 boolean enabled,
                                 boolean supplement) {
        if (!enabled) {
            volumeSectionsMap.remove(section);
        } else {
            if (!volumeSectionsMap.containsKey(section)) {
                volumeSectionsMap.put(section, new Volume(supplement?Volume.Type.SUPPLEMENTARY:Volume.Type.NORMAL, section.getName()));
            }
        }
    }

    public Set<Section> getVolumeSections() {
        return volumeSectionsMap.keySet();
    }

    public List<Section> getAvailableVolumeSections() {
        return mainSections;
    }
    
    public List<Section> getAvailableFrontMatterSections() {
        return allSections;
    }

    public List<Section> getAvailableTitlePageSections() {

        List<Section> titlePageSections = new ArrayList<Section>();
        if (frontMatterSection != null) {
            titlePageSections.add(frontMatterSection);
            titlePageSections.addAll(frontMatterSection.getDescendants());
        }
        return titlePageSections;
    }

    public List<Section> getAvailableExtendedFrontMatterSections() {

        if (frontMatterSection != null) {
            return frontMatterSection.getAncestors();
        }
        return new ArrayList<Section>();
    }

    public Section getFrontMatterSection() {
        return frontMatterSection;
    }

    public Section getTitlePageSection() {
        return titlePageSection;
    }

    public Section getExtendedFrontMatterSection() {
        return extendedFrontMatterSection;
    }

    public void setTitlePageSection(Section section) {

        if (getAvailableTitlePageSections().contains(section)) {
            titlePageSection = section;
        } else if (titlePageSection != null &&
                   !getAvailableTitlePageSections().contains(titlePageSection)) {
            titlePageSection = null;
        }
        setVolumeInfoEnabled(volumeInfoEnabled);
        setTranscriptionInfoEnabled(transcriptionInfoEnabled);
    }

    public void setFrontMatterSection(Section section) {

        if (getAvailableFrontMatterSections().contains(section)) {
            frontMatterSection = section;
        } else if (frontMatterSection != null &&
                   !getAvailableFrontMatterSections().contains(frontMatterSection)) {
            frontMatterSection = null;
        }
        setTitlePageSection(titlePageSection);
        setExtendedFrontMatterSection(extendedFrontMatterSection);
        Volume.setFrontMatterAvailable(frontMatterSection != null);
    }

    public void setExtendedFrontMatterSection(Section section) {

        List<Section> availableSections = getAvailableExtendedFrontMatterSections();
        if (availableSections.contains(section)) {
            extendedFrontMatterSection = section;
        } else if (extendedFrontMatterSection != null &&
                   !availableSections.contains(extendedFrontMatterSection)) {
            extendedFrontMatterSection = null;
        }        
        Volume.setExtFrontMatterAvailable(extendedFrontMatterSection != null);
    }

    public void setTranscribersNotesPageEnabled(boolean b) {
        transcribersNotesPageEnabled = b && getPreliminaryPagesPresent();
    }

    public boolean getTranscribersNotesPageEnabled() {
        return transcribersNotesPageEnabled;
    }

    public void setSpecialSymbolsListEnabled(boolean b) {
        specialSymbolsListEnabled = b && getPreliminaryPagesPresent();
    }

    public boolean getSpecialSymbolsListEnabled() {
        return specialSymbolsListEnabled;
    }

    public void setTableOfContentEnabled(boolean b) {
        tableOfContentEnabled = b && getPreliminaryPagesPresent();
    }

    public boolean getTableOfContentEnabled() {
        return tableOfContentEnabled;
    }

    public void setVolumeInfoEnabled(boolean b) {
        volumeInfoEnabled = b && getVolumeInfoAvailable();
    }

    public boolean getVolumeInfoEnabled() {
        return volumeInfoEnabled;
    }

    public void setTranscriptionInfoEnabled(boolean b) {
        transcriptionInfoEnabled = b && getTranscriptionInfoAvailable();
    }

    public boolean getTranscriptionInfoEnabled() {
        return transcriptionInfoEnabled;
    }

    public void setPreliminaryVolumeEnabled(boolean b) {
        preliminaryVolumeEnabled = b && getPreliminaryPagesPresent();
    }

    public boolean getPreliminaryVolumeEnabled() {
        return preliminaryVolumeEnabled;
    }

    public void setTranscribersNotesPageTitle(String s) {
        transcribersNotesPageTitle = s;
    }

    public String getTranscribersNotesPageTitle() {
        return transcribersNotesPageTitle;
    }

    public void setSpecialSymbolsListTitle(String s) {
        specialSymbolsListTitle = s;
    }

    public String getSpecialSymbolsListTitle() {
        return specialSymbolsListTitle;
    }

    public void setTableOfContentTitle(String s) {
        tableOfContentTitle = s;
    }

    public String getTableOfContentTitle() {
        return tableOfContentTitle;
    }

    public String getDate() {
        return DATE;
    }

    public int getNumberOfVolumes() {
        return NUMBER_OF_VOLUMES;
    }

    public int getNumberOfSupplements() {
        return NUMBER_OF_SUPPLEMENTS;
    }

    public boolean getPreliminaryPagesPresent() {
        return getFrontMatterSection() != null;
    }

    public boolean getVolumeInfoAvailable() {
        return getTitlePageSection() != null;
    }

    public boolean getTranscriptionInfoAvailable() {
        return getTitlePageSection() != null;
    }

    public boolean getParagraphsPresent() {
        return PARAGRAPHS_PRESENT;
    }

    public boolean getHeadingsPresent() {
        return HEADINGS_PRESENT;
    }

    public boolean getListsPresent() {
        return LISTS_PRESENT;
    }

    public boolean getTablesPresent() {
        return TABLES_PRESENT;
    }

    public boolean getPageNumbersPresent() {
        return PAGE_NUMBER_IN_HEADER_FOOTER;
    }

    public boolean getMathPresent() {
        return MATH_PRESENT;
    }

    public String getDefaultSpecialSymbol(SpecialSymbol.Type type, String language) {

        String translationTable = getTranslationTable(language);
        int grade = getGrade(language);
        String specialSymbol = "";

        switch (type) {
            case ELLIPSIS:
                specialSymbol = "\u2810\u2810\u2810";
                break;
            case DOUBLE_DASH:
                specialSymbol = "\u2824\u2824\u2824\u2824";
                break;
            case TRANSCRIBERS_NOTE_INDICATOR:
                specialSymbol = "\u2820\u2804";
                break;
            case NOTE_REFERENCE_INDICATOR:
                specialSymbol = "\u2814\u2814";
                break;
            case ITALIC_INDICATOR:
                if (translationTable.equals("en-US")) {
                    specialSymbol = "\u2828";
                    break;
                }
            case BOLDFACE_INDICATOR:
                if (translationTable.equals("en-US")) {
                    specialSymbol = "\u2838";
                    break;
                }
            case LETTER_INDICATOR:
                if (translationTable.equals("en-US")) {
                    specialSymbol = "\u2830";
                    break;
                }
            case NUMBER_INDICATOR:
                if (translationTable.equals("en-US")) {
                    specialSymbol = "\u283C";
                    break;
                }
            default:
        }

        return specialSymbol;
    }

    public ArrayList<ParagraphStyle> getParagraphStyles() {    
        return new ArrayList(paragraphStylesMap.values());
    }

    public ArrayList<CharacterStyle> getCharacterStyles() {
        return new ArrayList(characterStylesMap.values());
    }

    public ArrayList<HeadingStyle> getHeadingStyles() {
        return new ArrayList(headingStyles);
    }

    public ArrayList<ListStyle> getListStyles() {
        return new ArrayList(listStyles);
    }

    public TableStyle getTableStyle() {
        return tableStyle;
    }

    public FrameStyle getFrameStyle() {
        return frameStyle;
    }
    
    public Style getFootnoteStyle() {
        return footnoteStyle;
    }

    public TocStyle getTocStyle() {
        return tocStyle;
    }

    public ParagraphStyle getVolumeInfoStyle() {
        return volumeInfoStyle;
    }

    public ParagraphStyle getTranscriptionInfoStyle() {
        return transcriptionInfoStyle;
    }

    public boolean setVolumeInfoStyle(ParagraphStyle volumeInfoStyle) {

        if (volumeInfoStyle != null) {
            if (paragraphStylesMap.containsValue(volumeInfoStyle)) {
                this.volumeInfoStyle = volumeInfoStyle;
                return true;
            }
        }
        return false;
    }

    public boolean setTranscriptionInfoStyle(ParagraphStyle transcriptionInfoStyle) {

        if (transcriptionInfoStyle != null) {
            if (paragraphStylesMap.containsValue(transcriptionInfoStyle)) {
                this.transcriptionInfoStyle = transcriptionInfoStyle;
                return true;
            }
        }
        return false;
    }
    
    public boolean setVolumeInfoStyle(String volumeInfoStyle) {
        
        if (paragraphStylesMap.containsKey(volumeInfoStyle)) {
            this.volumeInfoStyle = paragraphStylesMap.get(volumeInfoStyle);
            return true;
        }    
        return false;
    }

    public boolean setTranscriptionInfoStyle(String transcriptionInfoStyle) {

        if (paragraphStylesMap.containsKey(transcriptionInfoStyle)) {
            this.transcriptionInfoStyle = paragraphStylesMap.get(transcriptionInfoStyle);
            return true;
        }
        return false;
    }

    public boolean setNoterefNumberPrefix(String numFormat, String prefix) {

        if (noterefNumberPrefixMap.containsKey(numFormat) &&
            (prefix.length() == 0 ||
             prefix.matches("[\\p{InBraille_Patterns}]*"))) {
            noterefNumberPrefixMap.put(numFormat, prefix);
            return true;
        } else {
            return false;
        }
    }
    
    public String getNoterefNumberPrefix(String numFormat) {
        return noterefNumberPrefixMap.get(numFormat);
    }

    public List<String> getNoterefNumberFormats() {

        List<String> formats = new ArrayList<String>();
        for (String s : noterefNumberPrefixMap.keySet()) {
            formats.add(s);
        }
        return formats;
    }

    private String capitalizeFirstLetter(String in) {
        return in.substring(0,1).toUpperCase() + in.substring(1);
    }

    public void setBrailleRules(BrailleRules rules) {

        this.brailleRules = rules;

        if (this.brailleRules==BrailleRules.BANA) {

            setPrintPageNumbers(true);
            setBraillePageNumbers(true);
            setPageSeparator(true);
            setPageSeparatorNumber(true);
            setContinuePages(true);
            setIgnoreEmptyPages(false);
            setMergeUnnumberedPages(false);
            setPageNumberAtTopOnSeparateLine(false);
            setPageNumberAtBottomOnSeparateLine(false);
            setPrintPageNumberRange(false);
            setPrintPageNumberAt(PageNumberPosition.TOP_RIGHT);
            setBraillePageNumberAt(PageNumberPosition.BOTTOM_RIGHT);
            setPreliminaryPageFormat(PageNumberFormat.P);
            setLineFillSymbol('\u2804');
            setNoterefNumberPrefix("1", "\u2814\u2814");
            setNoterefNumberPrefix("a", "\u2814\u2814\u2830");
            setNoterefNumberPrefix("A", "\u2814\u2814");
            setNoterefNumberPrefix("i", "\u2814\u2814\u2830");
            setNoterefNumberPrefix("I", "\u2814\u2814");

            for (ParagraphStyle paraStyle : getParagraphStyles()) {
                paraStyle.setInherit(true);
                if (paraStyle.getName().equals("Standard")) {
                    paraStyle.setAlignment(Style.Alignment.LEFT);
                    paraStyle.setFirstLine(2);
                    paraStyle.setRunovers(0);
                    paraStyle.setLinesAbove(0);
                    paraStyle.setLinesBelow(0);
                }
            }

            for (CharacterStyle charStyle : getCharacterStyles()) {
                charStyle.setInherit(true);
                if (charStyle.getName().equals("Default")) {
                    charStyle.setItalic(CharacterStyle.TypefaceOption.FOLLOW_PRINT);
                    charStyle.setBoldface(CharacterStyle.TypefaceOption.FOLLOW_PRINT);
                    charStyle.setUnderline(CharacterStyle.TypefaceOption.NO);
                    charStyle.setCapitals(CharacterStyle.TypefaceOption.FOLLOW_PRINT);
                }
            }
            
            for (HeadingStyle headStyle : getHeadingStyles()) {
                switch (headStyle.getLevel()) {
                    case 1:
                        headStyle.setAlignment(Style.Alignment.CENTERED);
                        headStyle.setLinesAbove(1);
                        headStyle.setLinesBelow(1);
                        break;
                    case 2:
                        headStyle.setAlignment(Style.Alignment.CENTERED);
                        headStyle.setLinesAbove(1);
                        headStyle.setLinesBelow(0);
                        break;
                    case 3:
                        headStyle.setAlignment(Style.Alignment.LEFT);
                        headStyle.setFirstLine(4);
                        headStyle.setRunovers(4);
                        headStyle.setLinesAbove(1);
                        headStyle.setLinesBelow(0);
                        break;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                        headStyle.setAlignment(Style.Alignment.LEFT);
                        headStyle.setFirstLine(0);
                        headStyle.setRunovers(0);
                        headStyle.setLinesAbove(1);
                        headStyle.setLinesBelow(0);
                        break;
                    default:
                        break;
                }
                headStyle.setUpperBorder(false);
                headStyle.setLowerBorder(false);
            }

            for (ListStyle listStyle : getListStyles()) {
                int level = listStyle.getLevel();
                if (level == 1) {
                    listStyle.setLinesAbove(1);
                    listStyle.setLinesBelow(1);
                } else {
                    listStyle.setLinesAbove(0);
                    listStyle.setLinesBelow(0);
                }
                listStyle.setAlignment(Style.Alignment.LEFT);
                listStyle.setFirstLine(2*level-2);
                listStyle.setRunovers(2*level+2);
                listStyle.setLinesBetween(0);
                listStyle.setPrefix("");
            }

            Style style = null;
            for (int i=1;i<=10;i++) {
                style = tableStyle.getColumn(i);
                if (style != null) {
                    style.setAlignment(Style.Alignment.LEFT);
                    style.setFirstLine(2*i-2);
                    style.setRunovers(2*i-2);
                }
            }

            tableStyle.setLinesAbove(0);
            tableStyle.setLinesBelow(0);
            tableStyle.setUpperBorder(true);
            tableStyle.setLowerBorder(true);
            tableStyle.setUpperBorderStyle('\u2836');
            tableStyle.setLowerBorderStyle('\u281b');
            tableStyle.setPaddingAbove(0);
            tableStyle.setPaddingBelow(0);
            tableStyle.setLinesBetween(0);
            tableStyle.setStairstepTable(true);

            frameStyle.setLinesAbove(0);
            frameStyle.setLinesBelow(0);
            frameStyle.setUpperBorder(true);
            frameStyle.setLowerBorder(true);
            frameStyle.setUpperBorderStyle('\u2836');
            frameStyle.setLowerBorderStyle('\u281b');
            frameStyle.setPaddingAbove(0);
            frameStyle.setPaddingBelow(0);

            for (int i=1;i<=10;i++) {
                style = tocStyle.getLevel(i);
                if (style != null) {
                    style.setFirstLine(2*i-2);
                    style.setRunovers(2*i+2);
                }
            }

            tocStyle.setLinesBetween(0);
            setPrintPageNumbersInToc(true);
            setBraillePageNumbersInToc(true);

            footnoteStyle.setAlignment(Style.Alignment.LEFT);
            footnoteStyle.setLinesAbove(0);
            footnoteStyle.setLinesBelow(0);
            footnoteStyle.setFirstLine(6);
            footnoteStyle.setRunovers(4);
        }
    }

    public BrailleRules getBrailleRules() {
        return brailleRules;
    }

    public void configureVolumes() {




        //setVolumeManagementMode(VolumeManagementMode.AUTOMATIC);

        


        volumeInfo = capitalizeFirstLetter(L10N_in) + " ";
        if (getPreliminaryVolumeEnabled()) {
           volumeInfo += "1 " + L10N_preliminary + " " + L10N_and + " ";
        }

        int volumeCount = 0;
        int supplementCount = 0;

        switch (volumeManagementMode) {
            case MANUAL:
                volumeCount = getNumberOfVolumes();
                supplementCount = getNumberOfSupplements();
                break;
            case SINGLE:
                volumeCount = 1;
                break;
            case AUTOMATIC:
                volumeCount = getAutomaticVolumes().size();
                break;
        }

        volumeInfo += volumeCount + ((volumeCount>1)?L10N_volumes:L10N_volume) + ((supplementCount==0)?"":" " + L10N_and + " " +
                      supplementCount + ((supplementCount>1)?L10N_supplements:L10N_supplement));
        volumeInfo += "\n@title\n@pages";

        volumeCount = 0;
        for (Volume v : getVolumes()) {
            volumeCount++;
            v.setToc(getTableOfContentEnabled());
            v.setFrontMatter(getPreliminaryPagesPresent());
            v.setTranscribersNotesPage(getTranscribersNotesPageEnabled());
            v.setSpecialSymbolsList(getSpecialSymbolsListEnabled());
            if (volumeCount==1) {
                v.setExtToc(true);
                v.setExtFrontMatter(true);
            } else {
                v.setExtToc(false);
                v.setExtFrontMatter(false);
            }
        }
    }

    private int[] computeOptimalVolumeSizes(int[] pages) {

        Combination optimalCombination = null;

        int[] weigths = new int[] { 512, 0, 1, 2, 4, 8, 16, 32, 64, 128, 256 };
        int total = pages.length;
        int preferred = 55;
        
        int min = 40;
        int max = 60;
        int minLast = 20;

        Combination.setBoundaries(total, min, max, minLast);

        Combination zeroCombination = new Combination();

        if (zeroCombination.dividable()) {

            Combinations allCombinations = null;
            Collection<Combination> possibleCombinations = null;
            Collection<Combination> selectedCombinations = new ArrayList<Combination>();
            double minError;
            double error;

            List<Collection<Integer>> h = new ArrayList<Collection<Integer>>();
            for (int i=0; i<=10; i++) {
                h.add(new ArrayList<Integer>());
            }
            for (int i=0; i<pages.length; i++) {
                h.get(pages[i]).add(i);
            }
            
            int level = 1;
            allCombinations = new Combinations(h.get(level));
            possibleCombinations = allCombinations.getOK();

            if (zeroCombination.ok()) {
                possibleCombinations.add(zeroCombination);
            } else {
                while (possibleCombinations.size()==0 && level<10) {
                    level++;
                    allCombinations = new Combinations(allCombinations, new Combinations(h.get(level)));
                    possibleCombinations = allCombinations.getOK();
                }
                if (possibleCombinations.size()==0) {
                    allCombinations = new Combinations(allCombinations, new Combinations(h.get(0)));
                    possibleCombinations = allCombinations.getOK();
                }
            }
            
            minError = (double)Integer.MAX_VALUE;
            for (Combination c : possibleCombinations) {
                error = 0;
                if (c.size()>0) {
                    for (int i : c) {
                        error += weigths[pages[i-1]];
                    }
                    error /= c.size();
                }
                if (error <= minError) {
                    if (error < minError) {
                        selectedCombinations.clear();
                    }
                    selectedCombinations.add(c);
                }
            }

            minError = (double)Integer.MAX_VALUE;
            for (Combination c : selectedCombinations) {
                error = 0;
                int start = 0;
                for (int end : c) {
                    error += Math.pow(end-start-preferred,2);
                    start = end;
                }
                error += Math.pow(total-start-preferred,2);
                error /= (c.size()+1);
                if (error < minError) {
                    optimalCombination = c;
                }
            }
        }
        
        if (optimalCombination != null) {
            int[] r = new int[optimalCombination.size()];
            int i = 0;
            for (int b : optimalCombination) {
                r[i] = b;
                i++;
            }
            return r;
        } else {
            return new int[0];
        }
    }
}