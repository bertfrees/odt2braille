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
import java.util.Collections;
import java.util.TreeMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;

import org.xml.sax.SAXException;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.daisy.braille.embosser.FileFormat;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserCatalog;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableCatalog;
import org.daisy.paper.Paper;
import org.daisy.paper.PageFormat;
import org.daisy.paper.Area;
import org.daisy.paper.PrintPage;
import org.daisy.paper.PaperCatalog;

/**
 * Collection of all braille-related settings and properties of an OpenOffice.org document.
 *
 * @author Bert Frees
 */
public class Settings {

    public static enum MathType { NEMETH, UKMATHS, MARBURG, WISKUNDE };
    public static enum BrailleRules { CUSTOM, BANA };
    public static enum PageNumberFormat { NORMAL, ROMAN, ROMANCAPS, P, S, BLANK };
    public static enum PageNumberPosition { TOP_LEFT, TOP_RIGHT, TOP_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER };
    public static enum VolumeManagementMode { SINGLE, MANUAL, AUTOMATIC };

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static NamespaceContext namespace = new NamespaceContext();
    private final static String L10N = Constants.L10N_PATH;
    private final static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private final static boolean IS_MAC_OS = System.getProperty("os.name").toLowerCase().contains("mac os");

    public final OdtTransformer odtTransformer;

    private boolean locked = false;

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

    public static final String PEF = "be.docarch.odt2braille.PEFFileFormat";
    public static final String BRF = "org_daisy.BrailleEditorsFileFormatProvider.FileType.BRF";
    public static final String BRL = "org_daisy.BrailleEditorsFileFormatProvider.FileType.BRL";
    public static final String BRA = "org_daisy.BrailleEditorsFileFormatProvider.FileType.BRA";

    public static final String INTERPOINT =              "be_interpoint";
    public static final String INDEX_BRAILLE =           "com_indexbraille";
    public static final String BRAILLO =                 "com_braillo";
    public static final String CIDAT =                   "es_once_cidat";
    public static final String ENABLING_TECHNOLOGIES =   "com_brailler";
    public static final String HARPO =                   "pl_com_harpo";
    public static final String VIEWPLUS =                "com_viewplus";

    public static final String GENERIC_EMBOSSER =        "org_daisy.GenericEmbosserProvider.EmbosserType.NONE";

    public static final String INTERPOINT_55 =           "be_interpoint.InterpointEmbosserProvider.EmbosserType.INTERPOINT_55";
    public static final String INDEX_BASIC_BLUE_BAR =    "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_BLUE_BAR";
    public static final String INDEX_EVEREST_S_V1 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_S_V1";
    public static final String INDEX_EVEREST_D_V1 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V1";
    public static final String INDEX_BASIC_S_V2 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_S_V2";
    public static final String INDEX_BASIC_D_V2 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_D_V2";
    public static final String INDEX_EVEREST_D_V2 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V2";
    public static final String INDEX_4X4_PRO_V2 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_4X4_PRO_V2";
    public static final String INDEX_BASIC_S_V3 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_S_V3";
    public static final String INDEX_BASIC_D_V3 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_D_V3";
    public static final String INDEX_EVEREST_D_V3 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V3";
    public static final String INDEX_4X4_PRO_V3 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_4X4_PRO_V3";
    public static final String INDEX_4WAVES_PRO_V3 =     "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_4WAVES_PRO_V3";
    public static final String INDEX_BASIC_D_V4 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_D_V4";
    public static final String INDEX_EVEREST_D_V4 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V4";
    public static final String INDEX_BRAILLE_BOX_V4 =    "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BRAILLE_BOX_V4";
    public static final String BRAILLO_200 =             "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_200";
    public static final String BRAILLO_270 =             "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_270";
    public static final String BRAILLO_400_S =           "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_400_S";
    public static final String BRAILLO_400_SR =          "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_400_SR";
    public static final String BRAILLO_440_SW_2P =       "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_440_SW_2P";
    public static final String BRAILLO_440_SW_4P =       "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_440_SW_4P";
    public static final String BRAILLO_440_SWSF =        "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_440_SWSF";
    public static final String IMPACTO_600 =             "es_once_cidat.CidatEmbosserProvider.EmbosserType.IMPACTO_600";
    public static final String IMPACTO_TEXTO =           "es_once_cidat.CidatEmbosserProvider.EmbosserType.IMPACTO_TEXTO";
    public static final String PORTATHIEL_BLUE =         "es_once_cidat.CidatEmbosserProvider.EmbosserType.PORTATHIEL_BLUE";
    public static final String ROMEO_ATTACHE =           "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_ATTACHE";
    public static final String ROMEO_ATTACHE_PRO =       "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_ATTACHE_PRO";
    public static final String ROMEO_25 =                "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.OMEO_25";
    public static final String ROMEO_PRO_50 =            "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_50";
    public static final String ROMEO_PRO_LE_NARROW =     "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_LE_NARROW";
    public static final String ROMEO_PRO_LE_WIDE =       "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_LE_WIDE";
    public static final String THOMAS =                  "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.THOMAS";
    public static final String THOMAS_PRO =              "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.THOMAS_PRO";
    public static final String MARATHON =                "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.MARATHON";
    public static final String ET =                      "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ET";
    public static final String JULIET_PRO =              "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_PRO";
    public static final String JULIET_PRO_60 =           "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_PRO_60";
    public static final String JULIET_CLASSIC =          "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_CLASSIC";
    public static final String BOOKMAKER =               "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BOOKMAKER";
    public static final String BRAILLE_EXPRESS_100 =     "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_EXPRESS_100";
    public static final String BRAILLE_EXPRESS_150 =     "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_EXPRESS_150";
    public static final String BRAILLE_PLACE =           "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_PLACE";
    public static final String MOUNTBATTEN_LS =          "pl_com_harpo.HarpoEmbosserProvider.EmbosserType.MOUNTBATTEN_LS";
    public static final String MOUNTBATTEN_PRO =         "pl_com_harpo.HarpoEmbosserProvider.EmbosserType.MOUNTBATTEN_PRO";
    public static final String MOUNTBATTEN_WRITER_PLUS = "pl_com_harpo.HarpoEmbosserProvider.EmbosserType.MOUNTBATTEN_WRITER_PLUS";
    public static final String PREMIER_80 =              "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.PREMIER_80";
    public static final String PREMIER_100 =             "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.PREMIER_100";
    public static final String ELITE_150 =               "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.ELITE_150";
    public static final String ELITE_200 =               "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.ELITE_200";
    public static final String PRO_GEN_II =              "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.PRO_GEN_II";
    public static final String CUB_JR =                  "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.CUB_JR";
    public static final String CUB =                     "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.CUB";
    public static final String MAX =                     "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.MAX";
    public static final String EMFUSE =                  "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.EMFUSE";
    public static final String EMPRINT_SPOTDOT =         "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.EMPRINT_SPOTDOT";

    public static final String CUSTOM_PAPER =            "be.docarch.odt2braille.CustomPaperProvider.PaperSize.CUSTOM";

    private static final String DEFAULT_BRAILLE_FILE_TYPE = BRF;
    private static final String DEFAULT_EMBOSSER = GENERIC_EMBOSSER;
    private static final String DEFAULT_PAPER = CUSTOM_PAPER;
    private static final String DEFAULT_TABLE = null;
    private static final String DEFAULT_TRANSLATION_TABLE = "en-US";
    private static final MathType DEFAULT_MATH = MathType.NEMETH;
    private static final int DEFAULT_GRADE = 2;

    // Read only properties

    private String DATE;
    private int NUMBER_OF_VOLUMES;
    private int NUMBER_OF_SUPPLEMENTS;
    private boolean PARAGRAPHS_PRESENT = false;
    private boolean HEADINGS_PRESENT = false;
    private boolean LISTS_PRESENT = false;
    private boolean TABLES_PRESENT = false;
    private boolean PAGE_NUMBER_IN_HEADER_FOOTER = false;
    private boolean MATH_PRESENT = false;

    // General Settings

    private BrailleRules brailleRules;

    private String mainLanguage;

    private String creator;
    private String transcribersNotesPageTitle;
    private String specialSymbolsListTitle;
    private String tableOfContentTitle;
    private String continuedSuffix;

    private boolean hyphenate = false;
    private boolean hardPageBreaks = false;

    private int minSyllableLength = 2;

    // Page Numbers Settings

    private boolean printPageNumbers = false;
    private boolean braillePageNumbers = false;
    private boolean pageSeparator = false;
    private boolean pageSeparatorNumber = false;
    private boolean continuePages = false;
    private boolean ignoreEmptyPages = false;
    private boolean mergeUnnumberedPages = false;
    private boolean pageNumberAtTopOnSeparateLine = false;
    private boolean pageNumberAtBottomOnSeparateLine = false;
    private boolean printPageNumberRange = false;
    private PageNumberPosition printPageNumberAt;
    private PageNumberPosition braillePageNumberAt;
    private PageNumberFormat preliminaryPageNumberFormat;
    private PageNumberFormat supplementaryPageNumberFormat;
    private int beginningBraillePageNumber;
    
    // Language Settings

    private Map<String,String> translationTableMap;
    private Map<String,Integer> gradeMap;
    private Map<String,Integer> dotsMap;

    // Table Of Contents Settings

    private boolean tableOfContentEnabled = false;

    // List Of Special Symbols Settings

    private List<SpecialSymbol> specialSymbolsList;

    // Mathematics Settings

    private MathType math;

    // Notes Settings

    private Map<String,String> noterefNumberPrefixMap = null;
    private Map<String,String> noterefCharactersMap = null;
    private boolean noterefSpaceBefore = false;
    private boolean noterefSpaceAfter = false;
    private Style footnoteStyle = null;

    // Volume management

    private VolumeManagementMode volumeManagementMode = null;
    private Section rootSection = null;
    private List<Section> mainSections = null;
    private List<Section> allSections = null;
    private Map<Section,SectionVolume> volumeSectionsMap = null;
    private List<AutomaticVolume> automaticVolumes = null;
    private SingleVolume singleVolume = null;
    private PreliminaryVolume preliminaryVolume = null;
    private Section frontMatterSection = null;
    private Section titlePageSection = null;
    private Section extendedFrontMatterSection = null;
    private boolean volumeInfoEnabled = false;
    private boolean transcriptionInfoEnabled = false;
    protected String volumeInfo = null;
    protected String transcriptionInfo = null;

    private boolean transcribersNotesPageEnabled = false;
    private boolean specialSymbolsListEnabled = false;
    private boolean preliminaryVolumeEnabled = false;

    private int minVolumeSize;
    private int maxVolumeSize;
    private int minLastVolumeSize;
    private int preferredVolumeSize;

    // Emboss & Export Settings

    private FileFormatCatalog fileFormatCatalog = null;
    private EmbosserCatalog embosserCatalog = null;
    private TableCatalog tableCatalog = null;
    private PaperCatalog paperCatalog = null;

    private Collection<String> supportedFormats = null;
    private Collection<String> supportedEmbossers = null;

    private FileFormat format = null;
    private Embosser embosser = null;
    private Paper paper = null;
    private Table table = null;

    private boolean multipleFiles = false;
    private boolean exportOrEmboss = false;

//    private double cellSpacing;
//    private double lineSpacing;
//    private double cellHeight;
//    private double cellWidth;
 
    private double unprintableInner;
    private double unprintableOuter;
    private double unprintableTop;
    private double unprintableBottom;

    private int cellsInWidth;
    private int linesInHeight;
    private int cellsPerLine;
    private int linesPerPage;
    private int maxCellsPerLine;
    private int maxLinesPerPage;
    private int minCellsPerLine;
    private int minLinesPerPage;

    private int marginInner;
    private int marginOuter;
    private int marginTop;
    private int marginBottom;
    private int maxMarginInner;
    private int maxMarginOuter;
    private int maxMarginTop;
    private int maxMarginBottom;
    private int minMarginInner;
    private int minMarginOuter;
    private int minMarginTop;
    private int minMarginBottom;

    private boolean duplex = false;
    private boolean eightDots = false;
    private boolean zFolding = false;
    private boolean saddleStitch = false;
    private int sheetsPerQuire;

    // Various

    private Map<String,ParagraphStyle> paragraphStyles;
    private Map<String,CharacterStyle> characterStyles;
  //private Map<String,TableStyle> tableStyles;           // default + self defined table styles (because there is no such thing as table styles in OOo)
  //private Map<String,TableStyle> tableStylesMap;        // map each table name <-> table style
    private List<HeadingStyle> headingStyles;
    private List<ListStyle> listStyles;
    private TableStyle tableStyle;
    private FrameStyle frameStyle;
    private TocStyle tocStyle;
    private ParagraphStyle volumeInfoStyle;
    private ParagraphStyle transcriptionInfoStyle;
    private Collection<String> supportedTranslationTablesGrades;
    private Collection<String> specialTranslationTables;
    

    /**
     * Creates a new <code>Settings</code> instance by copying other <code>Settings</code>.
     *
     * @param copySettigns    The <code>Settings</code> to be copied.
     */
    public Settings(Settings copySettings) {

        logger.entering("Settings", "<init>");

        Volume.init();

        this.locked = copySettings.locked;
        copySettings.lock();

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
        this.minVolumeSize = copySettings.minVolumeSize;
        this.maxVolumeSize = copySettings.maxVolumeSize;
        this.minLastVolumeSize = copySettings.minLastVolumeSize;
        this.preferredVolumeSize = copySettings.preferredVolumeSize;
        this.minSyllableLength = copySettings.minSyllableLength;

//        this.cellSpacing = copySettings.cellSpacing;
//        this.lineSpacing = copySettings.lineSpacing;
//        this.cellHeight = copySettings.cellHeight;
//        this.cellWidth = copySettings.cellWidth;
        
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
        this.noterefSpaceBefore = copySettings.noterefSpaceBefore;
        this.noterefSpaceAfter = copySettings.noterefSpaceAfter;

        this.printPageNumberAt = copySettings.printPageNumberAt;
        this.braillePageNumberAt = copySettings.braillePageNumberAt;
        this.preliminaryPageNumberFormat = copySettings.preliminaryPageNumberFormat;
        this.supplementaryPageNumberFormat = copySettings.supplementaryPageNumberFormat;
        this.brailleRules = copySettings.brailleRules;
        this.math = copySettings.math;
        this.volumeManagementMode = copySettings.volumeManagementMode;

        this.translationTableMap = new TreeMap(copySettings.translationTableMap);
        this.noterefNumberPrefixMap = new TreeMap(copySettings.noterefNumberPrefixMap);
        this.noterefCharactersMap = new TreeMap(copySettings.noterefCharactersMap);

        this.gradeMap = new TreeMap(copySettings.gradeMap);
        this.dotsMap = new TreeMap(copySettings.dotsMap);

        this.paragraphStyles = new TreeMap<String,ParagraphStyle>();
        for (ParagraphStyle copyParagraphStyle: copySettings.paragraphStyles.values()) {
            this.paragraphStyles.put(copyParagraphStyle.getName(), new ParagraphStyle(copyParagraphStyle));
        }
        ParagraphStyle copyParagraphStyle = null;
        for (ParagraphStyle paragraphStyle: this.paragraphStyles.values()) {
            copyParagraphStyle = copySettings.paragraphStyles.get(paragraphStyle.getName());
            if (copyParagraphStyle != null) {
                if (copyParagraphStyle.getParentStyle() != null) {
                    paragraphStyle.setParentStyle(paragraphStyles.get(copyParagraphStyle.getParentStyle().getName()));
                }
            }
        }
        this.characterStyles = new TreeMap<String,CharacterStyle>();
        for (CharacterStyle copyCharacterStyle: copySettings.characterStyles.values()) {
            this.characterStyles.put(copyCharacterStyle.getName(), new CharacterStyle(copyCharacterStyle));
        }
        CharacterStyle copyCharacterStyle = null;
        for (CharacterStyle characterStyle: this.characterStyles.values()) {
            copyCharacterStyle = copySettings.characterStyles.get(characterStyle.getName());
            if (copyCharacterStyle != null) {
                if (copyCharacterStyle.getParentStyle() != null) {
                    characterStyle.setParentStyle(characterStyles.get(copyCharacterStyle.getParentStyle().getName()));
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
        this.volumeInfoStyle = this.paragraphStyles.get(copySettings.volumeInfoStyle.getName());
        this.transcriptionInfoStyle = this.paragraphStyles.get(copySettings.transcriptionInfoStyle.getName());
        this.footnoteStyle = new Style(copySettings.footnoteStyle);

        this.supportedTranslationTablesGrades = new ArrayList<String>(copySettings.supportedTranslationTablesGrades);
        this.specialTranslationTables = new ArrayList<String>(copySettings.specialTranslationTables);

        this.embosser = copySettings.embosser;
        this.paper = copySettings.paper;
        this.table = copySettings.table;
        this.format = copySettings.format;

        this.fileFormatCatalog = copySettings.fileFormatCatalog;
        this.embosserCatalog = copySettings.embosserCatalog;
        this.tableCatalog = copySettings.tableCatalog;
        this.paperCatalog = copySettings.paperCatalog;
        this.supportedFormats = copySettings.supportedFormats;
        this.supportedEmbossers = copySettings.supportedEmbossers;

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

        this.singleVolume = new SingleVolume(copySettings.singleVolume);
        this.preliminaryVolume = new PreliminaryVolume(copySettings.preliminaryVolume);
        this.volumeSectionsMap = new HashMap<Section,SectionVolume>();
        for (Section s : mainSections) {
            SectionVolume v = copySettings.volumeSectionsMap.get(s);
            if (v!=null) {
                this.volumeSectionsMap.put(s, new SectionVolume(v));
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

        this(odtTransformer, Locale.ENGLISH);
        
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

        locked = false;

        File odtContentFile = odtTransformer.getOdtContentFile();
        File odtStylesFile = odtTransformer.getOdtStylesFile();
        File odtMetaFile = odtTransformer.getOdtMetaFile();

        volumeManagementMode = VolumeManagementMode.SINGLE;

        rootSection = odtTransformer.extractSectionTree();
        mainSections = rootSection.getChildren();
        allSections = rootSection.getDescendants();
        volumeSectionsMap = new HashMap<Section,SectionVolume>();

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
                                                                                  "ckb-g1",
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
                                                                                  "gez-g1",
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
                                                                                  "sr-g1",
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

        paragraphStyles = new TreeMap<String,ParagraphStyle>();
        for (ParagraphStyle style : odtTransformer.extractParagraphStyles()) {
            paragraphStyles.put(style.getName(), style);
        }
        characterStyles = new TreeMap<String,CharacterStyle>();
        for (CharacterStyle style : odtTransformer.extractCharacterStyles()) {
            characterStyles.put(style.getName(), style);
        }
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
        setVolumeInfoStyle(paragraphStyles.get("Standard"));
        setTranscriptionInfoStyle(paragraphStyles.get("Standard"));

        singleVolume = new SingleVolume();
        singleVolume.setTitle("(Single volume)");
        preliminaryVolume = new PreliminaryVolume();
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

        minVolumeSize = 30;
        maxVolumeSize = 40;
        minLastVolumeSize = 20;
        preferredVolumeSize = 35;

        transcriptionInfo = L10N_transcriptionInfo;
        volumeInfo = "@title\n@pages";
        
        beginningBraillePageNumber = 1;        
        math = DEFAULT_MATH;
        hardPageBreaks = false;
        noterefSpaceBefore = false;
        noterefSpaceAfter = false;
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
        unprintableInner = 0;
        unprintableOuter = 0;
        unprintableTop = 0;
        unprintableBottom = 0;
//        cellSpacing = 0;
//        lineSpacing = 0;
//        cellHeight = 0;
//        cellWidth = 0;
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

        // Catalogs use the context class loader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); {

            fileFormatCatalog = new FileFormatCatalog();
            tableCatalog = TableCatalog.newInstance();
            embosserCatalog = EmbosserCatalog.newInstance();
            paperCatalog = PaperCatalog.newInstance();

        } Thread.currentThread().setContextClassLoader(cl);

        supportedEmbossers = new ArrayList<String>();
        supportedFormats = new ArrayList<String>();

        for (Embosser e : embosserCatalog.list()) {
            supportedEmbossers.add(e.getIdentifier());
        }
        for (FileFormat f : fileFormatCatalog.list()) {
            supportedFormats.add(f.getIdentifier());
        }

        format = fileFormatCatalog.get(DEFAULT_BRAILLE_FILE_TYPE);
        embosser = embosserCatalog.get(DEFAULT_EMBOSSER);
        paper = paperCatalog.get(DEFAULT_PAPER);
        table = tableCatalog.get(DEFAULT_TABLE);

        math = DEFAULT_MATH;

        setExportOrEmboss(true);
        setMultipleFilesEnabled(false);
        setBrailleRules(BrailleRules.BANA);
        setBrailleRules(BrailleRules.CUSTOM);

        logger.exiting("Settings","<init>");

    }

    public void lock() {
        locked = true;
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

        Collection<String> supportedTranslationTables = getSupportedTranslationTables();
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
            for (String s : supportedTranslationTables) {
                if ((s + "-").indexOf(language + "-") == 0) {
                    ret = s;
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

        Collection<Integer> supportedGrades = getSupportedGrades(language);
        
        if (supportedGrades.isEmpty())       { return -1;    }
        if (supportedGrades.contains(grade)) { return grade; }

        for (int i=grade-1;i>=0;i--) {
            if (supportedGrades.contains(i)) {
                return i;
            }
        }

        return supportedGrades.iterator().next();
    
    }

    private int computeDots(int dots, String language) {

        Collection<Integer> supportedDots = getSupportedDots(language);

        if (supportedDots.isEmpty())      { return -1;   }
        if (supportedDots.contains(dots)) { return dots; }
        return supportedDots.iterator().next();

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
    public Collection<String> getSupportedTranslationTables() {
    
        List<String> supportedTranslationTables = new ArrayList();

        for (String translationTableGrade : supportedTranslationTablesGrades) {
            if (translationTableGrade.matches("[a-z]+(-[A-Z]+)?-g[0-9](-8d)?")) {
                String translationTable = translationTableGrade.substring(0,translationTableGrade.lastIndexOf("-g"));
                if (!supportedTranslationTables.contains(translationTable)) {
                    supportedTranslationTables.add(translationTable);
                }
            }
        }

        return supportedTranslationTables;
    
    }
    
    public Collection<String> getSpecialTranslationTables() {
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
    public Collection<Integer> getSupportedGrades(String language) {

        Collection<Integer> supportedGrades = new ArrayList();
        String translationTable = getTranslationTable(language);

        if (!specialTranslationTables.contains(translationTable)) {
            for (String translationTableGrade : supportedTranslationTablesGrades) {
                if (translationTableGrade.matches(translationTable + "-g[0-9](-8d)?")) {
                    int start = translationTableGrade.lastIndexOf("-g") + 2;
                    int grade = Integer.parseInt(translationTableGrade.substring(start, start + 1));
                    if (!supportedGrades.contains(grade)) {
                        supportedGrades.add(grade);
                    }
                }
            }
        }

        return supportedGrades;
        
    }

    public Collection<Integer> getSupportedDots(String language) {

        List<Integer> supportedDots = new ArrayList();
        String translationTable = getTranslationTable(language);
        int grade = getGrade(language);

        if (!specialTranslationTables.contains(translationTable)) {
            for (String translationTableGrade : supportedTranslationTablesGrades) {
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
    public boolean setTranslationTable(String translationTable, String language) {
    
        if (locked) { return false; }

        String trantab = computeTranslationTable(translationTable);
        
        if (trantab!=null) {
            translationTableMap.put(language, trantab);
            setGrade(DEFAULT_GRADE,language);
        } else if (!translationTableMap.containsKey(language)) {
            translationTableMap.put(language, DEFAULT_TRANSLATION_TABLE);
            setGrade(0,language);
        }

        return true;
    }

    /**
     * Change the grade for a certain language.
     * If the language doesn't support the desired grade, the current grade is not changed.
     *
     * @param   grade     The desired grade.
     * @param   language  The language code.
     */
    public boolean setGrade(int grade, String language) {

        if (locked) { return false; }

        if (translationTableMap.containsKey(language)) {
            gradeMap.put(language, computeGrade(grade, language));
            setDots(6, language);
        }

        return true;
    }

    public boolean setDots(int dots, String language) {

        if (locked) { return false; }

        if (translationTableMap.containsKey(language)) {
            dotsMap.put(language, computeDots(dots, language));
        }

        return true;
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
    public List<String> getLanguages() {
        return new ArrayList(translationTableMap.keySet());
    }

    /**
     * @return  The main language code of the document.
     */
    public String getMainLanguage() {
        return mainLanguage;
    }

    public boolean setMultipleFilesEnabled(boolean multipleFiles) {
        
        if (locked) { return false; }
        
        this.multipleFiles = multipleFiles && exportOrEmboss;

        return true;
    }
    
    public boolean getMultipleFilesEnabled() {
        return multipleFiles;
    }

    /**
     * Update the <code>exportOrEmboss</code> setting.
     *
     * @param   exportOrEmboss.
     */
    public boolean setExportOrEmboss(boolean exportOrEmboss) {

        if (locked) { return false; }

        this.exportOrEmboss = exportOrEmboss;

        refreshBrailleFileType();
        refreshEmbosser();
        refreshZFolding();
        refreshSaddleStitch();
        refreshDuplex();
        refreshEightDots();
        refreshTable();
        refreshPaper();
        refreshDimensions();
        return true;
    }

    public boolean getExportOrEmboss() {
        return exportOrEmboss;
    }

    /**
     * @param   embosser    An embosser type.
     * @return              <code>true</code> if the embosser type is supported, given the current settings.
     */
    private boolean embosserIsSupported(Embosser embosser) {

        if (embosser == null) {
            return false;
        }
        return supportedEmbossers.contains(embosser.getIdentifier());
    }

    /**
     * @return   A list of all supported embosser types, given the current settings.
     */
    public Collection<Embosser> getSupportedEmbossers() {
        return embosserCatalog.list();
    }

    private void changeEmbosser(Embosser embosser) {
        this.embosser = embosser;
    }

    private void refreshEmbosser() {

        if (!embosserIsSupported(this.embosser)) {
            try {
                changeEmbosser(getSupportedEmbossers().iterator().next());
            } catch (NoSuchElementException e) {
                changeEmbosser(null);
            }
        }
    }

    /**
     * Update the embosser type if the desired type is supported.
     *
     * @param   embosser   The desired embosser type.
     */
    public boolean setEmbosser(Embosser embosser) {

        if (locked) { return false; }

        if (embosserIsSupported(embosser)) {
            changeEmbosser(embosser);
            refreshZFolding();
            refreshSaddleStitch();
            refreshDuplex();
            refreshEightDots();
            refreshTable();
            refreshPaper();
            refreshDimensions();
            return true;
        } else {
            return false;
        }
    }

    public boolean setEmbosser(String identifier) {

        Embosser e = embosserCatalog.get(identifier);

        if (e != null) {
            return setEmbosser(e);
        }

        return false;
    }

    /**
     * @return  The current embosser
     */
    public Embosser getEmbosser() {
        return embosser;
    }

    /**
     * @param   fileType  A generic braille file type.
     * @return            <code>true</code> if the file type is supported
     */
    private boolean brailleFileTypeIsSupported(FileFormat format) {

        if (format == null) {
            return false;
        }
        return supportedFormats.contains(format.getIdentifier());
    }

    /**
     * @return   A list of all supported generic braille file types
     */
    public Collection<FileFormat> getSupportedBrailleFileTypes() {
        return fileFormatCatalog.list();
    }

    private void changeBrailleFileType(FileFormat format) {
        this.format = format;
    }

    private void refreshBrailleFileType() {

        if (!brailleFileTypeIsSupported(this.format)) {
            try {
                changeBrailleFileType(getSupportedBrailleFileTypes().iterator().next());
            } catch (NoSuchElementException e) {
                changeBrailleFileType(null);
            }
        }
    }

    /**
     * Update the braille file type if the desired type is supported.
     *
     * @param   brailleFileType  The desired braille file type.
     */
    public boolean setBrailleFileType(FileFormat format) {

        if (locked) { return false; }

        if (brailleFileTypeIsSupported(format)) {
            changeBrailleFileType(format);
            refreshDuplex();
            refreshEightDots();
            refreshTable();
            refreshDimensions();            
            return true;
        }

        return false;
    }

    public boolean setBrailleFileType(String identifier) {

        FileFormat f = fileFormatCatalog.get(identifier);

        if (f != null) {
            return setBrailleFileType(f);
        }

        return false;
    }

    /**
     * @return  The currently selected generic braille file type.
     */
    public FileFormat getBrailleFileType() {
        return format;
    }

    /**
     * @param   table   A character set.
     * @return          <code>true</code> if the character set is supported, given the current settings.
     */
    private boolean tableIsSupported (Table table) {

        if (table == null) {
            return false;
        }

        if (eightDots ^ table.newBrailleConverter().supportsEightDot()) {
            return false;
        }

        if (exportOrEmboss) {
            return format.supportsTable(table);
        } else {
            return embosser.supportsTable(table);            
        }
    }

    /**
     * @return   A list of all supported character sets, given the current settings.
     */
    public Collection<Table> getSupportedTables() {

        Collection<Table> supportedTableTypes = new ArrayList();
        for (Table tab : tableCatalog.list()) {
            if (tableIsSupported(tab)) {
                supportedTableTypes.add(tab);
            }
        }
        return supportedTableTypes;
    }

    private void changeTable(Table table) {

        this.table = table;
        try {
            if (exportOrEmboss) {
                format.setFeature(EmbosserFeatures.TABLE, table);
            } else {
                embosser.setFeature(EmbosserFeatures.TABLE, table);
            }
        } catch (IllegalArgumentException e) {
        }
    }

    private void refreshTable() {

        if (!tableIsSupported(table)) {
            try {
                changeTable(getSupportedTables().iterator().next());
            } catch (NoSuchElementException e) {
                changeTable(null);
            }
        }
    }

    /**
     * Update the character set if the desired set is supported.
     *
     * @param   table   The desired character set.
     */
    public boolean setTable(Table table) {

        if (locked) { return false; }

        if (tableIsSupported(table)) {
            changeTable(table);
            return true;
        }

        return false;
    }

    public boolean setTable(String identifier) {

        Table t = tableCatalog.get(identifier);

        if (t != null) {
            return setTable(t);
        }

        return false;
    }

    /**
     * @return  The current character set.
     */
    public Table getTable() {
        return table;
    }

    /**
     * @param   paper   A paper size.
     * @return          <code>true</code> if the paper size is supported, given the current settings.
     */
    private boolean paperIsSupported(Paper paper) {

        if (paper == null) {
            return false;
        }
        String id = paper.getIdentifier();

        if (!exportOrEmboss && embosser != null) {
            String emb = embosser.getIdentifier();
            if (!(emb.equals(INDEX_BASIC_BLUE_BAR) ||
                  emb.equals(INDEX_BASIC_S_V2) ||
                  emb.equals(INDEX_BASIC_D_V2) ||
                  emb.equals(INDEX_BASIC_S_V3) ||
                  emb.equals(INDEX_BASIC_D_V3))
                && id.equals(CUSTOM_PAPER)) {
                return true;
            }
            return embosser.supportsDimensions(paper);
        }

        return false;
    }

    /**
     * @return   A list of all supported paper sizes, given the current settings.
     */
    public Collection<Paper> getSupportedPapers() {

        Collection<Paper> supportedPapers = new ArrayList();

        for (Paper p : paperCatalog.list()) {
            if (paperIsSupported(p)) {
                supportedPapers.add(p);
            }
        }
        return supportedPapers;
    }

    private void changePaper(Paper paper) {

        if (paper == null || embosser == null) {
            this.paper = null;
            return;
        }

        if (paper instanceof CustomPaper && !embosser.supportsDimensions(paper)) {
            CustomPaper cp = (CustomPaper)paper;
            for (Paper p : getSupportedPapers()) {
                if (embosser.supportsDimensions(p)) {
                    cp.setWidth(p.getWidth());
                    cp.setHeight(p.getHeight());
                    return;
                }
            }
            this.paper = null;
        } else {
            this.paper = paper;
        }
    }

    private void refreshPaper() {

        if (embosser == null) { return; }

        if (!paperIsSupported(paper)) {
            for (Paper p : getSupportedPapers()) {
                if (embosser.supportsDimensions(p)) {
                    changePaper(p);
                    return;
                }
            }
            changePaper(null);
        } else {
            changePaper(paper);
        }
    }

    /**
     * Update the paper size if the desired paper size is supported.
     *
     * @param   paper   The desired paper size.
     */
    public boolean setPaper(Paper paper) {

        if (locked) { return false; }

        if (paperIsSupported(paper)) {
            changePaper(paper);
            refreshDimensions();
            return true;
        }

        return false;
    }

    public boolean setPaper(String identifier) {

        Paper p = paperCatalog.get(identifier);

        if (p != null) {
            return setPaper(p);
        }

        return false;
    }

    public boolean setCustomPaper(double paperWidth,
                                  double paperHeight) {

        if (locked ||
            exportOrEmboss ||
            embosser == null ||
            paper == null) {
            return false;
        }

        if (paper instanceof CustomPaper) {
            if (embosser.supportsDimensions(new Dimensions(paperWidth, paperHeight))) {
                CustomPaper p = (CustomPaper)paper;
                p.setWidth(paperWidth);
                p.setHeight(paperHeight);
                changePaper(this.paper);
                refreshDimensions();
                return true;
            }
        }

        return false;
    }

    /**
     * @return  The current paper size.
     */
    public Paper getPaper() {
        return paper;
    }

    public double getPaperWidth() {

        if (paper==null) {
            return 0;
        } else {
            return paper.getWidth();
        }
    }

    public double getPaperHeight() {

        if (paper==null) {
            return 0;
        } else {
            return paper.getHeight();
        }
    }

    /**
     * @return   <code>true</code> if recto-verso is supported, given the current settings.
     */
    public boolean duplexIsSupported() {

        if (exportOrEmboss) {
            return format.supportsDuplex();
        } else {
            return embosser.supportsDuplex();
        }
    }

    private void changeDuplex(boolean duplex) {

        this.duplex = duplex;
        if (!exportOrEmboss) {
            try {
                embosser.setFeature(EmbosserFeatures.DUPLEX, duplex);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void refreshDuplex() {

        if (duplex && !duplexIsSupported()) {
            changeDuplex(false);
        }
    }

    public boolean setDuplex(boolean duplex) {

        if (locked) { return false; }

        if (!duplex || duplexIsSupported()) {
            changeDuplex(duplex);
            return true;
        }

        return false;
    }

    /**
     * @return  <code>true</code> if recto-verso is enabled.
     */
    public boolean getDuplex() {
        return duplex;
    }

    public boolean saddleStitchIsSupported() {

        if (!exportOrEmboss && embosser.getFeature(EmbosserFeatures.SADDLE_STITCH) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    private void changeSaddleStitch(boolean saddleStitch) {

        this.saddleStitch = saddleStitch;
        if (!exportOrEmboss) {
            try {
                embosser.setFeature(EmbosserFeatures.SADDLE_STITCH, saddleStitch);
            } catch (IllegalArgumentException e) {
            }
        }
    }
    
    private void refreshSaddleStitch() {
        changeSaddleStitch(saddleStitch && saddleStitchIsSupported());
    }

    public boolean setSaddleStitch(boolean saddleStitch) {

        if (locked) { return false; }

        if (saddleStitchIsSupported()) {
            changeSaddleStitch(saddleStitch);
            refreshDuplex();
            refreshPaper();
            refreshDimensions();
            return true;
        }

        return false;
    }

    public boolean getSaddleStitch() {
        return saddleStitch;
    }

    public boolean sheetsPerQuireIsSupported() {

        if (!exportOrEmboss && embosser.getFeature(EmbosserFeatures.PAGES_IN_QUIRE) != null) {
            return true;
        } else {
            return false;
        }
    }

    private void changeSheetsPerQuire(int sheetsPerQuire) {

        this.sheetsPerQuire = sheetsPerQuire;
        if (!exportOrEmboss) {
            try {
                embosser.setFeature(EmbosserFeatures.PAGES_IN_QUIRE, sheetsPerQuire);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    public boolean setSheetsPerQuire(int sheetsPerQuire) {

        if (locked) { return false; }

        if (zFoldingIsSupported() && sheetsPerQuire > 0) {
            changeSheetsPerQuire(sheetsPerQuire);
            return true;
        }

        return false;
    }

    public int getSheetsPerQuire() {
        return sheetsPerQuire;
    }

    public boolean zFoldingIsSupported() {

        if (!exportOrEmboss && embosser.getFeature(EmbosserFeatures.Z_FOLDING) != null) {
            return true;
        } else {
            return false;
        }
    }

    private void changeZFolding(boolean zFolding) {

        this.zFolding = zFolding;
        if (!exportOrEmboss) {
            try {
                embosser.setFeature(EmbosserFeatures.Z_FOLDING, zFolding);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void refreshZFolding() {
        changeZFolding(zFolding && zFoldingIsSupported());
    }

    public boolean setZFolding(boolean zFolding) {

        if (locked) { return false; }

        if (zFoldingIsSupported()) {
            changeZFolding(zFolding);
            refreshDuplex();
            return true;
        }

        return false;
    }

    public boolean getZFolding() {
        return zFolding;
    }

    public boolean eightDotsIsSupported() {

        if (exportOrEmboss) {
            return format.supports8dot();            
        } else {
            return embosser.supports8dot();
        }
    }

    private void changeEightDots(boolean eightDots) {
        this.eightDots = eightDots;
    }

    private void refreshEightDots() {
        changeEightDots(eightDots && eightDotsIsSupported());
    }

    public boolean setEightDots(boolean eightDots) {

        if (locked) { return false; }

        if (eightDotsIsSupported()) {
            changeEightDots(eightDots);
            refreshTable();
            refreshDimensions();
            return true;
        }

        return false;
    }

    public boolean getEightDots() {
        return eightDots;
    }

    private void refreshDimensions() {

        minMarginInner = 0;
        minMarginTop = 0;
        minMarginOuter = 0;
        minMarginBottom = 0;
        maxMarginInner = 0;
        maxMarginTop = 0;
        maxMarginOuter = 0;
        maxMarginBottom = 0;

        minCellsPerLine = 1;
        minLinesPerPage = 1;
        maxCellsPerLine = Integer.MAX_VALUE;
        maxLinesPerPage = Integer.MAX_VALUE;

        unprintableInner = 0;
        unprintableOuter = 0;
        unprintableTop = 0;
        unprintableBottom = 0;
        cellsInWidth = 0;
        linesInHeight = 0;

        if (paper != null && embosser != null) {

            PageFormat inputPage = new PageFormat(paper);
            PrintPage printPage = embosser.getPrintPage(inputPage);
            Area printableArea = embosser.getPrintableArea(inputPage);

            unprintableInner = printableArea.getOffsetX();
            unprintableOuter = printPage.getWidth() - printableArea.getWidth() - unprintableInner;
            unprintableTop = printableArea.getOffsetY();
            unprintableBottom = printPage.getHeight() - printableArea.getHeight() - unprintableTop;

            cellsInWidth = embosser.getMaxWidth(inputPage);
            linesInHeight = embosser.getMaxHeight(inputPage);

            maxCellsPerLine = cellsInWidth;
            maxLinesPerPage = linesInHeight;

            if (embosser.supportsAligning()) {

                maxMarginInner = cellsInWidth;
                maxMarginOuter = cellsInWidth;
                maxMarginTop = linesInHeight;
                maxMarginBottom = linesInHeight;
            }

            int tempMaxMarginInner = maxMarginInner;
            int tempMaxMarginOuter = maxMarginOuter;
            int tempMaxMarginTop = maxMarginTop;
            int tempMaxMarginBottom = maxMarginBottom;
            int tempMinCellsPerLine = minCellsPerLine;
            int tempMinLinesPerPage = minLinesPerPage;

            maxMarginInner =  (int)Math.min(tempMaxMarginInner,  cellsInWidth  - tempMinCellsPerLine);
            maxMarginOuter =  (int)Math.min(tempMaxMarginOuter,  cellsInWidth  - tempMinCellsPerLine);
            minCellsPerLine = (int)Math.max(tempMinCellsPerLine, cellsInWidth  - tempMaxMarginInner  - tempMaxMarginOuter);
            maxMarginTop =    (int)Math.min(tempMaxMarginTop,    linesInHeight - tempMinLinesPerPage);
            maxMarginBottom = (int)Math.min(tempMaxMarginBottom, linesInHeight - tempMinLinesPerPage);
            minLinesPerPage = (int)Math.max(tempMinLinesPerPage, linesInHeight - tempMaxMarginTop    - tempMaxMarginBottom);
        }

        setMarginInner(marginInner);
        setMarginTop(marginTop);
    }

    /**
     * Set the number of cells per line to the desired value.
     *
     * @param   cells  The desired value.
     */
    public boolean setCellsPerLine(int cells) {

        if (locked) { return false; }

        cellsPerLine = Math.min(maxCellsPerLine, Math.max(minCellsPerLine, cells));

        if (marginsSupported()) {
            marginOuter = Math.min(maxMarginOuter,  Math.max(minMarginOuter, cellsInWidth - cellsPerLine - marginInner));
            marginInner = Math.min(maxMarginInner,  Math.max(minMarginInner, cellsInWidth - cellsPerLine - marginOuter));
            marginOuter = cellsInWidth - cellsPerLine - marginInner;
        } else {
            marginInner = 0;
            marginOuter = 0;
        }

        return true;
    }

    /**
     * Set the number of lines per page to the desired value.
     *
     * @param   lines  The desired value.
     */
    public boolean setLinesPerPage(int lines) {

        if (locked) { return false; }

        linesPerPage = Math.min(maxLinesPerPage, Math.max(minLinesPerPage, lines));

        if (marginsSupported()) {
            marginBottom = Math.min(maxMarginBottom, Math.max(minMarginBottom, linesInHeight - linesPerPage - marginTop));
            marginTop =    Math.min(maxMarginTop,    Math.max(minMarginTop,    linesInHeight - linesPerPage - marginBottom));
            marginBottom = linesInHeight - linesPerPage - marginTop;
        } else {
            marginTop = 0;
            marginBottom = 0;
        }

        return true;
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
     * @return   <code>true</code> if margins are supported, given the current settings.
     */
    public boolean marginsSupported() {
        return (paper != null && embosser != null && embosser.supportsAligning());
    }

    /**
     * Set the left margin to the desired value. If this is not possible, it will be set to the most nearby value.
     * The right margin and number of cells per line are updated accordingly.
     *
     * @param   margin  The desired value.
     */
    public boolean setMarginInner(int margin) {

        if (locked) { return false; }

        if (paper != null && embosser != null) {
            marginInner =  Math.min(maxMarginInner,  Math.max(minMarginInner,  margin));
            cellsPerLine = Math.max(minCellsPerLine, Math.min(maxCellsPerLine, cellsInWidth - marginInner - marginOuter));
            marginOuter =  cellsInWidth - cellsPerLine - marginInner;
        } else {
            marginInner = 0;
            marginOuter = 0;
        }

        return true;
    }

    /**
     * Set the right margin to the desired value. If this is not possible, it will be set to the most nearby value.
     * The left margin and number of cells per line are updated accordingly.
     *
     * @param   margin  The desired value.
     */
    public boolean setMarginOuter(int margin) {

        if (locked) { return false; }

        if (paper != null && embosser != null) {
            marginOuter =  Math.min(maxMarginOuter,  Math.max(minMarginOuter,  margin));
            cellsPerLine = Math.max(minCellsPerLine, Math.min(maxCellsPerLine, cellsInWidth - marginOuter - marginInner));
            marginInner =  cellsInWidth - cellsPerLine - marginOuter;
        } else {
            marginInner = 0;
            marginOuter = 0;
        }

        return true;
    }

    /**
     * Set the top margin to the desired value. If this is not possible, it will be set to the most nearby value.
     * The bottom margin and number of lines per page are updated accordingly.
     *
     * @param   margin  The desired value.
     */
    public boolean setMarginTop(int margin) {

        if (locked) { return false; }

        if (paper != null && embosser != null) {
            marginTop =    Math.min(maxMarginTop,    Math.max(minMarginTop,    margin));
            linesPerPage = Math.max(minLinesPerPage, Math.min(maxLinesPerPage, linesInHeight - marginTop - marginBottom));
            marginBottom = linesInHeight - linesPerPage - marginTop;
        } else {
            marginTop = 0;
            marginBottom = 0;
        }

        return true;
    }

    /**
     * Set the bottom margin to the desired value. If this is not possible, it will be set to the most nearby value.
     * The top margin and number of lines per page are updated accordingly.
     *
     * @param   margin  The desired value.
     */
    public boolean setMarginBottom(int margin) {

        if (locked) { return false; }

        if (paper != null && embosser != null) {
            marginBottom = Math.min(maxMarginBottom, Math.max(minMarginBottom, margin));
            linesPerPage = Math.max(minLinesPerPage, Math.min(maxLinesPerPage, linesInHeight - marginBottom - marginTop));
            marginTop =    linesInHeight - linesPerPage - marginBottom;
        } else {
            marginTop = 0;
            marginBottom = 0;
        }

        return true;
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
        return (int)Math.floor(unprintableInner / 6d);
    }

    public int getMarginOuterOffset() {
        return (int)Math.floor(unprintableOuter / 6d);
    }

    public int getMarginTopOffset() {
        return (int)Math.floor(unprintableTop / 10d);
    }

    public int getMarginBottomOffset() {
        return (int)Math.floor(unprintableBottom / 10d);
    }

    /**
     * @param stairstep
     */
    public boolean setStairstepTable(boolean stairstep) {

        if (locked) { return false; }
        tableStyle.setStairstepTable(stairstep);
        return true;
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
    public boolean setColumnDelimiter(String delimiter) {
        
        if (locked) { return false; }        
        tableStyle.setColumnDelimiter(delimiter);
        return true;
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

        if (locked) { return false; }
        return tocStyle.setLineFillSymbol(symbol);
    }

    public char getLineFillSymbol() {
        return tocStyle.getLineFillSymbol();
    }

    public boolean setMath(MathType math) {

        if (locked) { return false; }
        this.math = math;
        return true;
    }

    public MathType getMath() {
        return math;
    }

    public boolean setPrintPageNumbers(boolean b) {

        if (locked) { return false; }
        this.printPageNumbers = b && PAGE_NUMBER_IN_HEADER_FOOTER;
        setPrintPageNumberRange(printPageNumberRange);
        setContinuePages(continuePages);
        setPageNumberAtTopOnSeparateLine(pageNumberAtTopOnSeparateLine);
        setPageSeparatorNumber(pageSeparatorNumber);
        setPrintPageNumbersInToc(tocStyle.getPrintPageNumbers());
        return true;
    }
    
    public boolean setBraillePageNumbers(boolean b) {

        if (locked) { return false; }
        this.braillePageNumbers = b;
        setBraillePageNumbersInToc(tocStyle.getBraillePageNumbers());
        return true;
    }
    
    public boolean setPageSeparator(boolean b) {

        if (locked) { return false; }
        this.pageSeparator = b;
        setPageSeparatorNumber(pageSeparatorNumber);
        return true;
    }
    
    public boolean setPageSeparatorNumber(boolean b) {

        if (locked) { return false; }
        this.pageSeparatorNumber = b && printPageNumbers && pageSeparator;
        return true;
    }
    
    public boolean setContinuePages(boolean b) {

        if (locked) { return false; }
        this.continuePages = b && printPageNumbers;
        return true;
    }
    
    public boolean setIgnoreEmptyPages(boolean b) {

        if (locked) { return false; }
        this.ignoreEmptyPages = b;
        return true;
    }
    
    public boolean setMergeUnnumberedPages(boolean b) {
        
        if (locked) { return false; }
        this.mergeUnnumberedPages = b;
        return true;
    }
    
    public boolean setPageNumberAtTopOnSeparateLine(boolean b) {

        if (locked) { return false; }
        this.pageNumberAtTopOnSeparateLine = b
                || (printPageNumbers && (printPageNumberAt == PageNumberPosition.TOP_RIGHT
                                      || printPageNumberAt == PageNumberPosition.TOP_LEFT) && printPageNumberRange);
        return true;
    }
    
    public boolean setPageNumberAtBottomOnSeparateLine(boolean b) {

        if (locked) { return false; }
        this.pageNumberAtBottomOnSeparateLine = b;
        return true;
    }
    
    public boolean setPrintPageNumberRange(boolean b) {

        if (locked) { return false; }
        this.printPageNumberRange = b && this.printPageNumbers;
        setPageNumberAtTopOnSeparateLine(pageNumberAtTopOnSeparateLine);
        return true;
    }
    
    public boolean setPrintPageNumberAt(PageNumberPosition at) {
        
        if (locked) { return false; }
        if (at == PageNumberPosition.TOP_RIGHT || at == PageNumberPosition.BOTTOM_RIGHT) {
            this.printPageNumberAt = at;
            setPageNumberAtTopOnSeparateLine(pageNumberAtTopOnSeparateLine);
        }
        return true;
    }
    
    public boolean setBraillePageNumberAt(PageNumberPosition at) {
        
        if (locked) { return false; }
        if (at == PageNumberPosition.TOP_RIGHT || at == PageNumberPosition.BOTTOM_RIGHT) {
            this.braillePageNumberAt = at;
        }
        return true;
    }

    public boolean preliminaryPageFormatIsSupported(PageNumberFormat format) {

        return (format == PageNumberFormat.P ||
                format == PageNumberFormat.ROMAN ||
               (format == PageNumberFormat.ROMANCAPS && IS_WINDOWS));
    }

    public boolean setPreliminaryPageFormat(PageNumberFormat format) {

        if (locked) { return false; }
        if (preliminaryPageFormatIsSupported(format)) {
            this.preliminaryPageNumberFormat = format;
        }
        return true;
    }

    public boolean setBeginningBraillePageNumber(int number) {

        if (locked) { return false; }
        if (number > 0) {
            this.beginningBraillePageNumber = number;
        }
        return true;
    }

    public boolean setPrintPageNumbersInToc(boolean b) {

        if (locked) { return false; }
        tocStyle.setPrintPageNumbers(b && this.printPageNumbers);
        return true;
    }

    public boolean setBraillePageNumbersInToc(boolean b) {

        if (locked) { return false; }
        tocStyle.setBraillePageNumbers(b && this.braillePageNumbers);
        return true;
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

    public boolean setHardPageBreaks(boolean b) {

        if (locked) { return false; }
        this.hardPageBreaks = b;
        return true;
    }

    public boolean getHardPageBreaks() {
        return this.hardPageBreaks;
    }

    public boolean setCreator(String creator) {

        if (locked) { return false; }
        if (creator.length() > 0) {
            this.creator = creator;
            return true;
        }
        return false;
    }

    public String getCreator() {
        return this.creator;
    }

    public boolean setHyphenate(boolean hyphenate) {

        if (locked) { return false; }
        this.hyphenate = hyphenate;
        return true;
    }

    public boolean getHyphenate() {
        return this.hyphenate;
    }

    public boolean setMinSyllableLength(int length) {

        if (locked) { return false; }
        if (IS_WINDOWS || IS_MAC_OS) {
            minSyllableLength = Math.max(1, length);
            return true;
        }
        return false;
    }

    public int getMinSyllableLength() {
        return minSyllableLength;
    }

    public List<SpecialSymbol> getSpecialSymbolsList() {
        return specialSymbolsList;
    }

    public SpecialSymbol getSpecialSymbol(int index) {
        return specialSymbolsList.get(index);
    }

    public boolean setSpecialSymbol(SpecialSymbol specialSymbol,
                                    int index) {

        if (locked) { return false; }
        specialSymbolsList.set(index, specialSymbol);
        return true;
    }

    private boolean addSpecialSymbol(SpecialSymbol specialSymbol) {
        
        if (locked) { return false; }
        specialSymbolsList.add(specialSymbol);
        return true;
    }

    public int addSpecialSymbol() {

        if (locked) { return -1; }
        specialSymbolsList.add(new SpecialSymbol());
        return specialSymbolsList.size()-1;
    }

    public int deleteSpecialSymbol(int index) {

        if (locked) { return -1; }
        specialSymbolsList.remove(index);
        return Math.min(index, specialSymbolsList.size()-1);
    }

    public int moveSpecialSymbolUp(int index) {

        if (locked) { return -1; }
        try {
            Collections.swap(specialSymbolsList, index, index-1);
            return index-1;
        } catch (IndexOutOfBoundsException e) {
            return index;
        }
    }

    public int moveSpecialSymbolDown(int index) {

        if (locked) { return -1; }
        try {
            Collections.swap(specialSymbolsList, index, index+1);
            return index+1;
        } catch (IndexOutOfBoundsException e) {
            return index;
        }
    }
    
    public boolean setVolumeManagementMode(VolumeManagementMode mode) {

        if (locked) { return false; }
        if (mode == VolumeManagementMode.MANUAL && mainSections.size() < 1) {
            return false;
        }
        volumeManagementMode = mode;
        return true;
    }

    public VolumeManagementMode getVolumeManagementMode() {
        return volumeManagementMode;
    }

    public boolean setMinVolumeSize(int size) {

        if (locked || size < 1) { return false; }

        minVolumeSize =       size;
        maxVolumeSize =       Math.max(minVolumeSize, maxVolumeSize);
        preferredVolumeSize = Math.max(minVolumeSize, preferredVolumeSize);
        minLastVolumeSize =   Math.min(minVolumeSize, minLastVolumeSize);
        return true;
    }

    public boolean setMaxVolumeSize(int size) {

        if (locked || size < 1) { return false; }

        maxVolumeSize =       size;
        minVolumeSize =       Math.min(maxVolumeSize, minVolumeSize);
        preferredVolumeSize = Math.min(maxVolumeSize, preferredVolumeSize);
        minLastVolumeSize =   Math.min(minVolumeSize, minLastVolumeSize);
        return true;
    }

    public boolean setMinLastVolumeSize(int size) {

        if (locked || size < 1 || size > minVolumeSize) { return false; }

        minLastVolumeSize = size;
        return true;
    }

    public boolean setPreferredVolumeSize(int size) {

        if (locked || size < 1) { return false; }

        preferredVolumeSize = size;
        maxVolumeSize =       Math.max(preferredVolumeSize, maxVolumeSize);
        minVolumeSize =       Math.min(preferredVolumeSize, minVolumeSize);
        minLastVolumeSize =   Math.min(minVolumeSize, minLastVolumeSize);
        return true;
    }

    public int getMinVolumeSize() {
        return minVolumeSize;
    }

    public int getMaxVolumeSize() {
        return maxVolumeSize;
    }

    public int getMinLastVolumeSize() {
        return minLastVolumeSize;
    }

    public int getPreferredVolumeSize() {
        return preferredVolumeSize;
    }

    public List<AutomaticVolume> getAutomaticVolumes() {

        if (automaticVolumes==null) {
            try {

                odtTransformer.configure(this);

                int[] allPages = odtTransformer.extractDocumentOutline();
                int[] optimalVolumes = computeOptimalVolumes(allPages, minVolumeSize, maxVolumeSize, preferredVolumeSize, minLastVolumeSize);
                automaticVolumes = new ArrayList<AutomaticVolume>();
                AutomaticVolume v;

                for (int i=0; i<optimalVolumes.length; i++) {
                    v = new AutomaticVolume(i+1, optimalVolumes[i]+1);
                    v.setTitle(capitalizeFirstLetter(L10N_volume) + " " + (i+1));
                    automaticVolumes.add(v);
                }
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        return automaticVolumes;
    }

    private Volume getPreliminaryVolume() {
        return preliminaryVolume;
    }

    public Volume getVolume(Section section) {
        return volumeSectionsMap.get(section);
    }

    public List<Volume> getVolumes() {

        Volume volume = null;
        List<Volume> volumes = new ArrayList<Volume>();
        if (getPreliminaryVolumeEnabled()) {
            volumes.add(getPreliminaryVolume());
        }

        switch (volumeManagementMode) {
            case MANUAL:
                for (Section section : getAvailableVolumeSections()) {
                    volume = getVolume(section);
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

    public boolean setVolumeSection(Section section,
                                 boolean enabled,
                                 boolean supplement) {

        if (locked) { return false; }

        if (!enabled) {
            volumeSectionsMap.remove(section);
        } else {
            if (!volumeSectionsMap.containsKey(section)) {
                volumeSectionsMap.put(section,
                        new SectionVolume(supplement?Volume.Type.SUPPLEMENTARY:Volume.Type.NORMAL, section.getName()));
            }
        }

        return true;
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

    public Section getRearMatterSection() { // TODO
        return null;
    }

    public Section getTitlePageSection() {
        return titlePageSection;
    }

    public Section getExtendedFrontMatterSection() {
        return extendedFrontMatterSection;
    }

    public boolean setTitlePageSection(Section section) {

        if (locked) { return false; }

        if (getAvailableTitlePageSections().contains(section)) {
            titlePageSection = section;
        } else if (titlePageSection != null &&
                   !getAvailableTitlePageSections().contains(titlePageSection)) {
            titlePageSection = null;
        }
        setVolumeInfoEnabled(volumeInfoEnabled);
        setTranscriptionInfoEnabled(transcriptionInfoEnabled);

        return true;
    }

    public boolean setFrontMatterSection(Section section) {

        if (locked) { return false; }

        if (getAvailableFrontMatterSections().contains(section)) {
            frontMatterSection = section;
        } else if (frontMatterSection != null &&
                   !getAvailableFrontMatterSections().contains(frontMatterSection)) {
            frontMatterSection = null;
        }
        setTitlePageSection(titlePageSection);
        setExtendedFrontMatterSection(extendedFrontMatterSection);
        Volume.setFrontMatterAvailable(frontMatterSection != null);

        return true;
    }

    public boolean setExtendedFrontMatterSection(Section section) {

        if (locked) { return false; }

        List<Section> availableSections = getAvailableExtendedFrontMatterSections();
        if (availableSections.contains(section)) {
            extendedFrontMatterSection = section;
        } else if (extendedFrontMatterSection != null &&
                   !availableSections.contains(extendedFrontMatterSection)) {
            extendedFrontMatterSection = null;
        }        
        Volume.setExtFrontMatterAvailable(extendedFrontMatterSection != null);

        return true;
    }

    public boolean setRearMatterSection(Section section) { // TODO
        return false;
    }

    public boolean setTranscribersNotesPageEnabled(boolean b) {

        if (locked) { return false; }
        transcribersNotesPageEnabled = b && getFrontMatterPresent(); // TODO: remove "&& getFrontMatterPresent()"
        return true;
    }

    public boolean getTranscribersNotesPageEnabled() {
        return transcribersNotesPageEnabled;
    }

    public boolean setSpecialSymbolsListEnabled(boolean b) {

        if (locked) { return false; }
        specialSymbolsListEnabled = b && getFrontMatterPresent(); // TODO: remove "&& getFrontMatterPresent()"
        return true;
    }

    public boolean getSpecialSymbolsListEnabled() {
        return specialSymbolsListEnabled;
    }

    public boolean setTableOfContentEnabled(boolean b) {

        if (locked) { return false; }
        tableOfContentEnabled = b && getFrontMatterPresent(); // TODO: remove "&& getFrontMatterPresent()"
        return true;
    }

    public boolean getTableOfContentEnabled() {
        return tableOfContentEnabled;
    }

    public boolean setVolumeInfoEnabled(boolean b) {

        if (locked) { return false; }
        volumeInfoEnabled = b && getVolumeInfoAvailable();
        return true;
    }

    public boolean getVolumeInfoEnabled() {
        return volumeInfoEnabled;
    }

    public boolean setTranscriptionInfoEnabled(boolean b) {

        if (locked) { return false; }
        transcriptionInfoEnabled = b && getTranscriptionInfoAvailable();
        return true;
    }

    public boolean getTranscriptionInfoEnabled() {
        return transcriptionInfoEnabled;
    }

    public boolean setPreliminaryVolumeEnabled(boolean b) {

        if (locked) { return false; }
        preliminaryVolumeEnabled = b && getFrontMatterPresent(); // TODO: remove "&& getFrontMatterPresent()"
        return true;
    }

    public boolean getPreliminaryVolumeEnabled() {
        return preliminaryVolumeEnabled;
    }

    public boolean setTranscribersNotesPageTitle(String s) {

        if (locked) { return false; }
        transcribersNotesPageTitle = s;
        return true;
    }

    public String getTranscribersNotesPageTitle() {
        return transcribersNotesPageTitle;
    }

    public boolean setSpecialSymbolsListTitle(String s) {

        if (locked) { return false; }
        specialSymbolsListTitle = s;
        return true;
    }

    public String getSpecialSymbolsListTitle() {
        return specialSymbolsListTitle;
    }

    public boolean setTableOfContentTitle(String s) {

        if (locked) { return false; }
        tableOfContentTitle = s;
        return true;
    }

    public String getTableOfContentTitle() {
        return tableOfContentTitle;
    }

    public String getContinuedSuffix() {
        return continuedSuffix;
    }

    public String getDate() {
        return DATE;
    }

    public boolean getFrontMatterPresent() {
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

    public List<ParagraphStyle> getParagraphStyles() {    
        return new ArrayList(paragraphStyles.values());
    }

    public List<CharacterStyle> getCharacterStyles() {
        return new ArrayList(characterStyles.values());
    }

    public List<HeadingStyle> getHeadingStyles() {
        return new ArrayList(headingStyles);
    }

    public List<ListStyle> getListStyles() {
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
            if (paragraphStyles.containsValue(volumeInfoStyle)) {
                this.volumeInfoStyle = volumeInfoStyle;
                return true;
            }
        }
        return false;
    }

    public boolean setTranscriptionInfoStyle(ParagraphStyle transcriptionInfoStyle) {

        if (transcriptionInfoStyle != null) {
            if (paragraphStyles.containsValue(transcriptionInfoStyle)) {
                this.transcriptionInfoStyle = transcriptionInfoStyle;
                return true;
            }
        }
        return false;
    }
    
    public boolean setVolumeInfoStyle(String volumeInfoStyle) {
        
        if (paragraphStyles.containsKey(volumeInfoStyle)) {
            this.volumeInfoStyle = paragraphStyles.get(volumeInfoStyle);
            return true;
        }    
        return false;
    }

    public boolean setTranscriptionInfoStyle(String transcriptionInfoStyle) {

        if (paragraphStyles.containsKey(transcriptionInfoStyle)) {
            this.transcriptionInfoStyle = paragraphStyles.get(transcriptionInfoStyle);
            return true;
        }
        return false;
    }

    public boolean setNoterefSpaceBefore(boolean b) {

        if (locked) { return false; }
        noterefSpaceBefore = b;
        return true;
    }

    public boolean getNoterefSpaceBefore() {
        return noterefSpaceBefore;
    }

    public boolean setNoterefSpaceAfter(boolean b) {

        if (locked) { return false; }
        noterefSpaceAfter = b;
        return true;
    }

    public boolean getNoterefSpaceAfter() {
        return noterefSpaceAfter;
    }

    public boolean setNoterefNumberPrefix(String numFormat,
                                          String prefix) {
        if (locked) { return false; }

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

    public boolean setBrailleRules(BrailleRules rules) {

        if (locked) { return false; }

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
            setNoterefSpaceBefore(true);
            setNoterefSpaceAfter(true);

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

        return true;
    }

    public BrailleRules getBrailleRules() {
        return brailleRules;
    }

    public boolean configureVolumes() {

        if (locked) { return false; }

        volumeInfo = capitalizeFirstLetter(L10N_in) + " ";
        if (getPreliminaryVolumeEnabled()) {
           volumeInfo += "1 " + L10N_preliminary + " " + L10N_and + " ";
        }

        int volumeCount = 0;
        int supplementCount = 0;

        switch (volumeManagementMode) {
            case MANUAL:
                volumeCount = NUMBER_OF_VOLUMES;
                supplementCount = NUMBER_OF_SUPPLEMENTS;
                break;
            case SINGLE:
                volumeCount = 1;
                break;
            case AUTOMATIC:
                volumeCount = getAutomaticVolumes().size();
                break;
        }

        volumeInfo += volumeCount + " " + ((volumeCount>1)?L10N_volumes:L10N_volume) + ((supplementCount==0)?"":" " + L10N_and + " " +
                      supplementCount + ((supplementCount>1)?L10N_supplements:L10N_supplement));
        volumeInfo += "\n@title\n@pages";

        volumeCount = 0;
        for (Volume v : getVolumes()) {
            volumeCount++;
            v.setToc(getTableOfContentEnabled());
            v.setFrontMatter(getFrontMatterPresent());
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

        return true;
    }

    private int[] computeOptimalVolumes(int[] pages,
                                        int min,
                                        int max,
                                        int preferred,
                                        int minLast) {

        int total = Math.max(1,pages.length);
        int[] weigths = new int[] { 512, 0, 1, 2, 4, 8, 16, 32, 64, 128, 256 };
        TreeMap<Integer, ArrayList<Integer>> optimalpartitions = new TreeMap<Integer, ArrayList<Integer>>();        
        int[] minerror1 = new int[total+1];
        int[] minerror2 = new int[total+1];
        boolean[] ok = new boolean[total+1];
        int previouspage;
        int currentpage;
        ArrayList<Integer> previouspartition;
        ArrayList<Integer> currentpartition;

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
            ArrayList<Integer> optimalpartition = optimalpartitions.get(total);
            int[] r = new int[optimalpartition.size()];
            int j = 0;
            for (int i : optimalpartition) {
                r[j] = i;
                j++;
            }
            return r;
        } else {
            return new int[]{0};
        }
    }
}