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

package be.docarch.odt2braille.setup;

import java.io.Serializable;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeIterator;
import org.apache.xpath.XPathAPI;

import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.xml.transform.TransformerException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.NamespaceContext;
import be.docarch.odt2braille.OdtTransformer;
import be.docarch.odt2braille.XPathUtils;
import be.docarch.odt2braille.setup.style.ListStyle;
import be.docarch.odt2braille.setup.style.FootnoteStyle;
import be.docarch.odt2braille.setup.style.CharacterStyle;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.ParagraphStyle;
import be.docarch.odt2braille.setup.style.HeadingStyle;
import be.docarch.odt2braille.setup.style.TableStyle;
import be.docarch.odt2braille.setup.style.FrameStyle;
import be.docarch.odt2braille.setup.style.PictureStyle;

/**
 * Collection of all braille-related settings and properties of an OpenOffice.org document.
 *
 * @author Bert Frees
 */
public class Configuration implements Serializable {

    private static final long serialVersionUID = 1L;

    
    /********************/
    /*  FACTORY METHODS */
    /********************/

    private static OdtTransformer transformer = null;

    public static void setTransformer(OdtTransformer transformer) {
        Configuration.transformer = transformer;
    }

    public static Configuration newInstance() throws IOException,
                                                     SAXException,
                                                     Exception {
        if (transformer == null) {
            throw new Exception("Exception: transformer is not set!");
        }

        return new Configuration(transformer);
    }


    /********************/
    /* PUBLIC CONSTANTS */
    /********************/

    public final OdtTransformer odtTransformer;
    public final Locale mainLocale;
    public final String date;
    public final String continuedSuffix;
    public final String transcriptionInfo;


    /************/
    /* SETTINGS */
    /************/

    public final OptionSetting<MathCode> mathCode;
    public final DependentYesNoSetting printPageNumbers;
    public final Setting<Boolean> braillePageNumbers;
    public final Setting<Boolean> pageSeparator;
    public final DependentYesNoSetting pageSeparatorNumber;
    public final DependentYesNoSetting continuePages;
    public final Setting<Boolean> ignoreEmptyPages;
    public final Setting<Boolean> mergeUnnumberedPages;
    public final DependentYesNoSetting pageNumberLineAtTop;
    public final DependentYesNoSetting pageNumberLineAtBottom;
    public final DependentYesNoSetting printPageNumberRange;
    public final DependentOptionSetting<PageNumberPosition> printPageNumberPosition;
    public final DependentOptionSetting<PageNumberPosition> braillePageNumberPosition;
    public final DependentOptionSetting<PageNumberFormat> preliminaryPageNumberFormat;
  //public final DependentOptionSetting<PageNumberFormat> supplementaryPageNumberFormat;
    public final DependentNumberSetting beginningBraillePageNumber;
    public final Setting<Boolean> hardPageBreaks;
    public final Setting<String> creator;
    public final Setting<Boolean> hyphenate;
    public final Setting<Integer> minSyllableLength;
    public final Setting<String> transcribersNotesPageTitle;
    public final Setting<String> specialSymbolListTitle;
    public final DependentOptionSetting<VolumeManagementMode> bodyMatterMode;
    public final DependentOptionSetting<VolumeManagementMode> rearMatterMode;
    public final DependentOptionSetting<String> frontMatterSection;
    public final DependentOptionSetting<String> repeatFrontMatterSection;
    public final DependentOptionSetting<String> titlePageSection;
    public final DependentOptionSetting<String> rearMatterSection;
    public final Setting<Boolean> preliminaryVolumeEnabled;
    public final YesNoSetting volumeInfoEnabled;
    public final YesNoSetting transcriptionInfoEnabled;
    public final OptionSetting<ParagraphStyle> volumeInfoStyle;
    public final OptionSetting<ParagraphStyle> transcriptionInfoStyle;

    
    /* GETTERS */

    public MathCode             getMathCode()                      { return mathCode.get(); }
    public boolean              getPrintPageNumbers()              { return printPageNumbers.get(); }
    public boolean              getBraillePageNumbers()            { return braillePageNumbers.get(); }
    public boolean              getPageSeparator()                 { return pageSeparator.get(); }
    public boolean              getPageSeparatorNumber()           { return pageSeparatorNumber.get(); }
    public boolean              getContinuePages()                 { return continuePages.get(); }
    public boolean              getIgnoreEmptyPages()              { return ignoreEmptyPages.get(); }
    public boolean              getMergeUnnumberedPages()          { return mergeUnnumberedPages.get(); }
    public boolean              getPageNumberLineAtTop()           { return pageNumberLineAtTop.get(); }
    public boolean              getPageNumberLineAtBottom()        { return pageNumberLineAtBottom.get(); }
    public boolean              getPrintPageNumberRange()          { return printPageNumberRange.get(); }
    public PageNumberPosition   getPrintPageNumberPosition()       { return  printPageNumberPosition.get(); }
    public PageNumberPosition   getBraillePageNumberPosition()     { return braillePageNumberPosition.get(); }
    public PageNumberFormat     getPreliminaryPageNumberFormat()   { return preliminaryPageNumberFormat.get(); }
  //public PageNumberFormat     getSupplementaryPageNumberFormat() { return supplementaryPageNumberFormat.get(); }
    public int                  getBeginningBraillePageNumber()    { return beginningBraillePageNumber.get(); }
    public boolean              getHardPageBreaks()                { return hardPageBreaks.get(); }
    public String               getCreator()                       { return creator.get(); }
    public boolean              getHyphenate()                     { return hyphenate.get(); }
    public int                  getMinSyllableLength()             { return minSyllableLength.get(); }
    public String               getTranscribersNotesPageTitle()    { return transcribersNotesPageTitle.get(); }
    public String               getSpecialSymbolListTitle()        { return specialSymbolListTitle.get(); }
    public VolumeManagementMode getBodyMatterMode()                { return bodyMatterMode.get(); }
    public VolumeManagementMode getRearMatterMode()                { return rearMatterMode.get(); }
    public String               getFrontMatterSection()            { return frontMatterSection.get(); }
    public String               getRepeatFrontMatterSection()      { return repeatFrontMatterSection.get(); }
    public String               getTitlePageSection()              { return titlePageSection.get(); }
    public String               getRearMatterSection()             { return rearMatterSection.get(); }
    public boolean              getPreliminaryVolumeEnabled()      { return preliminaryVolumeEnabled.get(); }
    public boolean              getVolumeInfoEnabled()             { return volumeInfoEnabled.get(); }
    public boolean              getTranscriptionInfoEnabled()      { return transcriptionInfoEnabled.get(); }
    public ParagraphStyle       getVolumeInfoStyle()               { return volumeInfoStyle.get(); }
    public ParagraphStyle       getTranscriptionInfoStyle()        { return transcriptionInfoStyle.get(); }

    
    /* SETTERS */

    public void setMathCode                      (MathCode value)             { mathCode.set(value); }
    public void setPrintPageNumbers              (boolean value)              { printPageNumbers.set(value); }
    public void setBraillePageNumbers            (boolean value)              { braillePageNumbers.set(value); }
    public void setPageSeparator                 (boolean value)              { pageSeparator.set(value); }
    public void setPageSeparatorNumber           (boolean value)              { pageSeparatorNumber.set(value); }
    public void setContinuePages                 (boolean value)              { continuePages.set(value); }
    public void setIgnoreEmptyPages              (boolean value)              { ignoreEmptyPages.set(value); }
    public void setMergeUnnumberedPages          (boolean value)              { mergeUnnumberedPages.set(value); }
    public void setPageNumberLineAtTop           (boolean value)              { pageNumberLineAtTop.set(value); }
    public void setPageNumberLineAtBottom        (boolean value)              { pageNumberLineAtBottom.set(value); }
    public void setPrintPageNumberRange          (boolean value)              { printPageNumberRange.set(value); }
    public void setPrintPageNumberPosition       (PageNumberPosition value)   { printPageNumberPosition.set(value); }
    public void setBraillePageNumberPosition     (PageNumberPosition value)   { braillePageNumberPosition.set(value); }
    public void setPreliminaryPageNumberFormat   (PageNumberFormat value)     { preliminaryPageNumberFormat.set(value); }
  //public void setSupplementaryPageNumberFormat (PageNumberFormat value)     { supplementaryPageNumberFormat.set(value); }
    public void setBeginningBraillePageNumber    (int value)                  { beginningBraillePageNumber.set(value); }
    public void setHardPageBreaks                (boolean value)              { hardPageBreaks.set(value); }
    public void setCreator                       (String value)               { creator.set(value); }
    public void setHyphenate                     (boolean value)              { hyphenate.set(value); }
    public void setMinSyllableLength             (int value)                  { minSyllableLength.set(value); }
    public void setTranscribersNotesPageTitle    (String value)               { transcribersNotesPageTitle.set(value); }
    public void setSpecialSymbolListTitle        (String value)               { specialSymbolListTitle.set(value); }
    public void setBodyMatterMode                (VolumeManagementMode value) { bodyMatterMode.set(value); }
    public void setRearMatterMode                (VolumeManagementMode value) { rearMatterMode.set(value); }
    public void setFrontMatterSection            (String value)               { frontMatterSection.set(value); }
    public void setRepeatFrontMatterSection      (String value)               { repeatFrontMatterSection.set(value); }
    public void setTitlePageSection              (String value)               { titlePageSection.set(value); }
    public void setRearMatterSection             (String value)               { rearMatterSection.set(value); }
    public void setPreliminaryVolumeEnabled      (boolean value)              { preliminaryVolumeEnabled.set(value); }
    public void setVolumeInfoEnabled             (boolean value)              { volumeInfoEnabled.set(value); }
    public void setTranscriptionInfoEnabled      (boolean value)              { transcriptionInfoEnabled.set(value); }
    public void setVolumeInfoStyle               (ParagraphStyle value)       { volumeInfoStyle.set(value); }
    public void setTranscriptionInfoStyle        (ParagraphStyle value)       { transcriptionInfoStyle.set(value); }

    
    /***************************/
    /* SUBLEVEL CONFIGURATIONS */
    /***************************/

    private final SettingMap<Locale,TranslationTable> translationTables;
    private final SettingMap<String,NoteReferenceFormat> noteReferenceFormats;
    private final SettingMap<String,ParagraphStyle> paragraphStyles;
    private final SettingMap<String,CharacterStyle> characterStyles;
    private final SettingMap<Integer,HeadingStyle> headingStyles;
    private final SettingMap<String,TableStyle> tableStyles;
    private final SettingMap<Integer,ListStyle> listStyles;
    private final TocStyle tocStyle;
    private final FrameStyle frameStyle;
    private final FootnoteStyle footnoteStyle;
    private final PictureStyle pictureStyle;
    private final SplittableVolume bodyMatterVolume;
    private final Volume rearMatterVolume;
    private final Volume preliminaryVolume;
    private final SectionVolumeList sectionVolumeList;
    private final SpecialSymbolList specialSymbolList;

    
    /* GETTERS */
    
    public SettingMap<Locale,TranslationTable>    getTranslationTables()    { return translationTables; }
    public SettingMap<String,NoteReferenceFormat> getNoteReferenceFormats() { return noteReferenceFormats; }
    public SettingMap<String,ParagraphStyle>      getParagraphStyles()      { return paragraphStyles; }
    public SettingMap<String,CharacterStyle>      getCharacterStyles()      { return characterStyles; }
    public SettingMap<Integer,HeadingStyle>       getHeadingStyles()        { return headingStyles; }
    public SettingMap<String,TableStyle>          getTableStyles()          { return tableStyles; }
    public SettingMap<Integer,ListStyle>          getListStyles()           { return listStyles; }
    public TocStyle                               getTocStyle()             { return tocStyle; }
    public FrameStyle                             getFrameStyle()           { return frameStyle; }
    public FootnoteStyle                          getFootnoteStyle()        { return footnoteStyle; }
    public PictureStyle                           getPictureStyle()         { return pictureStyle; }
    public SplittableVolume                       getBodyMatterVolume()     { return bodyMatterVolume; }
    public Volume                                 getRearMatterVolume()     { return rearMatterVolume; }
    public Volume                                 getPreliminaryVolume()    { return preliminaryVolume; }
    public SpecialSymbolList                      getSpecialSymbolList()    { return specialSymbolList; }
    public SettingList<SectionVolume>             getSectionVolumeList()    { return sectionVolumeList; }
   
    /***************************/
    /* PUBLIC STATIC CONSTANTS */
    /***************************/

    public static enum MathCode { NEMETH, UKMATHS, MARBURG, WISKUNDE };
    public static enum PageNumberFormat { NORMAL, ROMAN, ROMANCAPS, P, S, BLANK };
    public static enum PageNumberPosition { TOP_LEFT, TOP_RIGHT, TOP_CENTER, BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_CENTER };
    public static enum VolumeManagementMode { SINGLE, MANUAL, AUTOMATIC };

    
    /****************************/
    /* PRIVATE STATIC CONSTANTS */
    /****************************/

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static final NamespaceContext namespace = new NamespaceContext();
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final boolean IS_MAC_OS = System.getProperty("os.name").toLowerCase().contains("mac os");


    /*********************/
    /* PRIVATE CONSTANTS */
    /*********************/

    private final String L10N_transcribersNotesPageTitle;
    private final String L10N_specialSymbolListTitle;
    private final String L10N_tableOfContentTitle;
    private final String L10N_continuedSuffix;
    private final String L10N_supplement;
    private final String L10N_preliminary;
    private final String L10N_transcriptionInfo;

    private final boolean PAGE_NUMBER_IN_HEADER_FOOTER;


    /***********/
    /* PRIVATE */
    /***********/

    private Element rootSection;
    private List<String> allSections;
    private Map<String,SectionVolume> volumeSectionsMap;


    /***********************/
    /* PRIVATE CONSTRUCTOR */
    /***********************/
    
    public Configuration(OdtTransformer transformer)
                  throws IOException,
                         SAXException {

        logger.entering("Configuration","<init>");

        odtTransformer = transformer;

      //File odtContentFile = odtTransformer.getOdtContentFile();
        File odtStylesFile = transformer.getOdtStylesFile();
        File odtMetaFile = transformer.getOdtMetaFile();
        
        PAGE_NUMBER_IN_HEADER_FOOTER = XPathUtils.evaluateBoolean(odtStylesFile.toURL().openStream(),
                    "//office:master-styles//style:header/text:p/text:page-number or " +
                    "//office:master-styles//style:footer/text:p/text:page-number", namespace);

        String DATE;
        if (XPathUtils.evaluateBoolean(odtMetaFile.toURL().openStream(), "//office:meta/dc:date",namespace)) {
            DATE = XPathUtils.evaluateString(odtMetaFile.toURL().openStream(), "//office:meta/dc:date/text()",namespace).substring(0, 4);
        } else if (XPathUtils.evaluateBoolean(odtMetaFile.toURL().openStream(), "//office:meta/meta:creation-date",namespace)) {
            DATE = XPathUtils.evaluateString(odtMetaFile.toURL().openStream(), "//office:meta/meta:creation-date/text()",namespace).substring(0, 4);
        } else {
            DATE = (new SimpleDateFormat("yyyy")).format(new Date());
        }

        String CREATOR = "";
        if (XPathUtils.evaluateBoolean(odtMetaFile.toURL().openStream(), "//office:meta/dc:creator",namespace)) {
            CREATOR = XPathUtils.evaluateString(odtMetaFile.toURL().openStream(), "//office:meta/dc:creator/text()",namespace);
        } else if (XPathUtils.evaluateBoolean(odtMetaFile.toURL().openStream(), "//office:meta/meta:initial-creator",namespace)) {
            CREATOR = XPathUtils.evaluateString(odtMetaFile.toURL().openStream(), "//office:meta/meta:initial-creator/text()",namespace);
        }

        rootSection = transformer.extractSectionTree();
        allSections = new ArrayList<String>();

        try {
            NodeIterator sections = XPathAPI.selectNodeIterator(rootSection, "descendant::section");
            for (Node section = sections.nextNode(); section != null; section = sections.nextNode()) {
                String name = section.getAttributes().getNamedItem("name").getNodeValue();
                allSections.add(name);
            }
        } catch (TransformerException e) {
        }

        volumeSectionsMap = new HashMap<String,SectionVolume>();

        String[] languages = transformer.extractLanguages();

        ResourceBundle bundle = ResourceBundle.getBundle(Constants.L10N_PATH, stringToLocale(languages[0]));

        L10N_supplement = bundle.getString("supplement");
        L10N_preliminary = bundle.getString("preliminary");
        L10N_transcribersNotesPageTitle = bundle.getString("transcribersNotesPageTitle");
        L10N_specialSymbolListTitle = bundle.getString("specialSymbolsListTitle");
        L10N_tableOfContentTitle = bundle.getString("tableOfContentTitle");
        L10N_continuedSuffix = bundle.getString("continuedSuffix");
        L10N_transcriptionInfo = bundle.getString("transcriptionInfo");
        

        /********************
           PUBLIC CONSTANTS
         ********************/

        mainLocale = stringToLocale(languages[0]);
        date = DATE;
        continuedSuffix = L10N_continuedSuffix;
        transcriptionInfo = L10N_transcriptionInfo;


        /***************************
           PROPERTIES and SETTINGS
         ***************************/

        /* DECLARATION */

        translationTables = new TranslationTableMap();
        noteReferenceFormats = new NoteReferenceFormatsMap();

        mathCode = new MathCodeSetting();
        
        printPageNumbers = new PrintPageNumbersSetting();
        braillePageNumbers = new YesNoSetting();
        pageSeparator = new YesNoSetting();
        pageSeparatorNumber = new PageSeparatorNumberSetting();
        continuePages = new ContinuePagesSetting();
        ignoreEmptyPages = new YesNoSetting();
        mergeUnnumberedPages = new YesNoSetting();
        pageNumberLineAtTop = new PageNumberLineAtTopSetting();
        pageNumberLineAtBottom = new PageNumberLineAtBottomSetting();
        printPageNumberRange = new PrintPageNumberRangeSetting();
        printPageNumberPosition = new PrintPageNumberPositionSetting();
        braillePageNumberPosition = new BraillePageNumberPositionSetting();
        preliminaryPageNumberFormat = new PreliminaryPageNumberFormatSetting();
      //supplementaryPageNumberFormat = new SupplementaryPageNumberFormatSetting();
        beginningBraillePageNumber = new BeginningBraillePageNumberSetting();

        paragraphStyles = new ParagraphStyleMap(transformer.extractParagraphStyles());
        characterStyles = new CharacterStyleMap(transformer.extractCharacterStyles());
        headingStyles = new HeadingStyleMap();
        tableStyles = new TableStyleMap();
        listStyles = new ListStyleMap();
        tocStyle = new TocStyle(this);
        frameStyle = new FrameStyle();
        footnoteStyle = new FootnoteStyle();
        pictureStyle = new PictureStyle();

        volumeInfoStyle = new ParagraphStyleSetting();
        transcriptionInfoStyle = new ParagraphStyleSetting();

        hardPageBreaks = new YesNoSetting();
        creator = new TextSetting();
        hyphenate = new YesNoSetting();
        minSyllableLength = new MinSyllableLengthSetting();
        transcribersNotesPageTitle = new TextSetting();
        specialSymbolListTitle = new TextSetting();
        volumeInfoEnabled = new YesNoSetting();
        transcriptionInfoEnabled = new YesNoSetting();

        bodyMatterMode = new BodyMatterModeSetting();
        rearMatterMode = new RearMatterModeSetting();
        frontMatterSection = new FrontMatterSectionSetting();
        repeatFrontMatterSection = new RepeatFrontMatterSectionSetting();
        titlePageSection = new TitlePageSectionSetting();
        rearMatterSection = new RearMatterSectionSetting();
        preliminaryVolumeEnabled = new YesNoSetting();

        bodyMatterVolume = new SplittableVolume();
        rearMatterVolume = new Volume();
        preliminaryVolume = new Volume();
        sectionVolumeList = new SectionVolumeList();

        specialSymbolList = new SpecialSymbolList();

        
        /* INITIALIZATION */

        printPageNumbers.set(true);
        braillePageNumbers.set(true);
        continuePages.set(true);
        printPageNumberRange.set(false);
        pageSeparator.set(true);
        pageSeparatorNumber.set(true);
        ignoreEmptyPages.set(true);
        mergeUnnumberedPages.set(false);
        pageNumberLineAtTop.set(false);
        pageNumberLineAtBottom.set(false);
        beginningBraillePageNumber.set(1);

        tocStyle.setTitle(L10N_tableOfContentTitle.toUpperCase());

        hardPageBreaks.set(false);
        creator.set(CREATOR);
        hyphenate.set(false);
        volumeInfoEnabled.set(false);
        transcriptionInfoEnabled.set(false);
        minSyllableLength.set(2);
        transcribersNotesPageTitle.set(L10N_transcribersNotesPageTitle.toUpperCase());
        specialSymbolListTitle.set(L10N_specialSymbolListTitle.toUpperCase());

        frontMatterSection.set("PreliminaryPages");
        if (titlePageSection.options().contains("TitlePage")) {
            repeatFrontMatterSection.set("TitlePage");
            titlePageSection.set("TitlePage");
        }

        bodyMatterVolume.setTitle("Volume @i");
        rearMatterVolume.setTitle(capitalizeFirstLetter(L10N_supplement));
        preliminaryVolume.setTitle(capitalizeFirstLetter(L10N_preliminary));
        
        bodyMatterVolume.setFrontMatter(true);
        rearMatterVolume.setFrontMatter(true);
        preliminaryVolume.setFrontMatter(true);

        rearMatterMode.refresh();
        bodyMatterMode.set(VolumeManagementMode.MANUAL);
        sectionVolumeList.refresh();

        SectionVolume v;
        while((v = sectionVolumeList.add()) != null) {
            v.setTitle(v.getSection());
            v.setFrontMatter(true);
        }

        bodyMatterMode.set(VolumeManagementMode.AUTOMATIC);
        sectionVolumeList.refresh();

        String mainTableLocale = TranslationTable.computeLocale(mainLocale);
        translationTables.get(mainLocale).locale.set(mainTableLocale);
        for (String language : languages) {
            TranslationTable t = translationTables.get(stringToLocale(language));
            t.locale.set(mainTableLocale);
            t.grade.set(0);
        }


        /* LINKING */

        printPageNumbers.addListener(printPageNumberPosition);
        printPageNumbers.addListener(printPageNumberRange);
        printPageNumbers.addListener(continuePages);
        printPageNumbers.addListener(pageNumberLineAtTop);
        printPageNumbers.addListener(pageSeparatorNumber);
        printPageNumbers.addListener(pageNumberLineAtBottom);
        braillePageNumbers.addListener(braillePageNumberPosition);
        braillePageNumbers.addListener(preliminaryPageNumberFormat);
        braillePageNumbers.addListener(beginningBraillePageNumber);
        braillePageNumbers.addListener(pageNumberLineAtTop);
        braillePageNumbers.addListener(pageNumberLineAtBottom);
        pageSeparator.addListener(pageSeparatorNumber);
        printPageNumberRange.addListener(pageNumberLineAtTop);
        printPageNumberPosition.addListener(pageNumberLineAtTop);
        printPageNumberPosition.addListener(pageNumberLineAtBottom);
        braillePageNumberPosition.addListener(pageNumberLineAtTop);
        braillePageNumberPosition.addListener(pageNumberLineAtBottom);

        frontMatterSection.addListener(repeatFrontMatterSection);
        frontMatterSection.addListener(titlePageSection);
        frontMatterSection.addListener(rearMatterSection);
        frontMatterSection.addListener(bodyMatterMode);
        rearMatterSection.addListener(frontMatterSection);
        rearMatterSection.addListener(bodyMatterMode);
        rearMatterSection.addListener(rearMatterMode);

        frontMatterSection.addListener(sectionVolumeList);
        bodyMatterMode.addListener(sectionVolumeList);
        rearMatterSection.addListener(sectionVolumeList);
        rearMatterMode.addListener(sectionVolumeList);

        logger.exiting("Configuration","<init>");

    }


    /*****************/
    /* INNER CLASSES */
    /*****************/

    public class TranslationTableMap extends SettingMap<Locale,TranslationTable> {

        private final Map<Locale,TranslationTable> map = new HashMap<Locale,TranslationTable>();

        public TranslationTable get(Locale key) {
            add(key);
            return map.get(key);
        }

        public Collection<TranslationTable> values() { return map.values(); }
        public Collection<Locale> keys() { return map.keySet(); }

        protected void add(Locale key) {
            if (!map.containsKey(key)) {
                map.put(key, new TranslationTable(key));
            }
        }
    }

    public class NoteReferenceFormatsMap extends SettingMap<String,NoteReferenceFormat> {

        private final Map<String,NoteReferenceFormat> map;

        public NoteReferenceFormatsMap() {
            map = new HashMap<String,NoteReferenceFormat>();
            map.put("1", new NoteReferenceFormat());
            map.put("a", new NoteReferenceFormat());
            map.put("A", new NoteReferenceFormat());
            map.put("i", new NoteReferenceFormat());
            map.put("I", new NoteReferenceFormat());
        }

        public NoteReferenceFormat get(String key) {
            add(key);
            return map.get(key);
        }

        public Collection<NoteReferenceFormat> values() { return map.values(); }
        public Collection<String> keys() { return map.keySet(); }

        protected void add(String key) {
            if (!map.containsKey(key)) {
                map.put(key, new NoteReferenceFormat());
            }
        }
    }

    private class MathCodeSetting extends EnumSetting<MathCode> {
        public MathCodeSetting() {
            super(MathCode.class);
            update(MathCode.NEMETH);
        }
    }

    private class PrintPageNumbersSetting extends DependentYesNoSetting {
        public boolean accept(Boolean value) {
            return !value || PAGE_NUMBER_IN_HEADER_FOOTER;
        }
    }

    private class PageSeparatorNumberSetting extends DependentYesNoSetting {
        public boolean accept(Boolean value) {
            return !value || (printPageNumbers.get() && pageSeparator.get());
        }
    }

    private class ContinuePagesSetting extends DependentYesNoSetting {
        public boolean accept(Boolean value) {
            return !value || printPageNumbers.get();
        }
    }

    private class PageNumberLineAtTopSetting extends DependentYesNoSetting {
        public boolean accept(Boolean value) {
            if (printPageNumberRange.get() &&
                printPageNumberPosition.get() == PageNumberPosition.TOP_RIGHT) { return value; }
            return !value ||
                    (printPageNumbers.get() && printPageNumberPosition.get() == PageNumberPosition.TOP_RIGHT) ||
                    (braillePageNumbers.get() && braillePageNumberPosition.get() == PageNumberPosition.TOP_RIGHT);
        }
    }

    private class PageNumberLineAtBottomSetting extends DependentYesNoSetting {
        public boolean accept(Boolean value) {
            return !value  ||
                    (printPageNumbers.get() && printPageNumberPosition.get() == PageNumberPosition.BOTTOM_RIGHT) ||
                    (braillePageNumbers.get() && braillePageNumberPosition.get() == PageNumberPosition.BOTTOM_RIGHT);
        }
    }

    private class PrintPageNumberRangeSetting extends DependentYesNoSetting {
        public boolean accept(Boolean value) {
            return !value || printPageNumbers.get();
        }
    }

    private class PrintPageNumberPositionSetting extends DependentOptionSetting<PageNumberPosition> {

        private PageNumberPosition position = PageNumberPosition.TOP_RIGHT;

        public Collection<PageNumberPosition> options() {
            return Arrays.asList(new PageNumberPosition[] {PageNumberPosition.TOP_RIGHT, PageNumberPosition.BOTTOM_RIGHT});
        }

        protected boolean update(PageNumberPosition value) {
            if (position == value) { return false;}
            position = value;
            return true;
        }

        public PageNumberPosition get() { return position; }
        public boolean refresh() { return false; }
        @Override
        public boolean enabled() { return printPageNumbers.get(); }
    }

    private class BraillePageNumberPositionSetting extends DependentOptionSetting<PageNumberPosition> {

        private PageNumberPosition position = PageNumberPosition.BOTTOM_RIGHT;

        public Collection<PageNumberPosition> options() {
            return Arrays.asList(new PageNumberPosition[] {PageNumberPosition.TOP_RIGHT, PageNumberPosition.BOTTOM_RIGHT});
        }

        protected boolean update(PageNumberPosition value) {
            if (position == value) { return false;}
            position = value;
            return true;
        }

        public PageNumberPosition get() { return position; }
        public boolean refresh() { return false; }
        @Override
        public boolean enabled() { return braillePageNumbers.get(); }
    }

    private class PreliminaryPageNumberFormatSetting extends DependentOptionSetting<PageNumberFormat> {

        private PageNumberFormat format = PageNumberFormat.P;
        
        public Collection<PageNumberFormat> options() {
            Collection<PageNumberFormat> options = new ArrayList<PageNumberFormat>();
            options.add(PageNumberFormat.P);
            options.add(PageNumberFormat.ROMAN);
            if (IS_WINDOWS) { options.add(PageNumberFormat.ROMANCAPS); }
            return options;
        }

        protected boolean update(PageNumberFormat value) {
            if (format == value) { return false;}
            format = value;
            return true;
        }

        public PageNumberFormat get() { return format; }
        public boolean refresh() { return false; }
        @Override
        public boolean enabled() { return braillePageNumbers.get(); }
    }

    private class BeginningBraillePageNumberSetting extends DependentNumberSetting {
        public boolean accept(Integer value) { return true; }
        @Override
        public boolean enabled() { return braillePageNumbers.get(); }
    }

    private class MinSyllableLengthSetting extends Setting<Integer> {

        private boolean enabled = IS_WINDOWS || IS_MAC_OS;
        private int number = 2;

        public boolean accept(Integer value) {
            return enabled ? (value >= 1) : (value == 2);
        }

        protected boolean update(Integer value) {
            if (number == value) { return false; }
            number = value;
            return true;
        }

        public Integer get() { return number; }
        @Override
        public boolean enabled() { return enabled; }
    }

    private class BodyMatterModeSetting extends DependentOptionSetting<VolumeManagementMode> {

        private VolumeManagementMode mode = VolumeManagementMode.SINGLE;

        public Collection<VolumeManagementMode> options() {
            Collection<VolumeManagementMode> options = new ArrayList<VolumeManagementMode>();
            options.add(VolumeManagementMode.SINGLE);
            options.add(VolumeManagementMode.AUTOMATIC);
            if (getBodyMatterVolumeSections().size() +
                getAvailableBodyMatterVolumeSections(null).size() > 0) {
                options.add(VolumeManagementMode.MANUAL);
            }
            return options;
        }

        protected boolean update(VolumeManagementMode value) {
            if (mode == value) { return false; }
            mode = value;
            return true;
        }

        public VolumeManagementMode get() { return mode; }

        public boolean refresh() {
            if (accept(get())) { return false; }
            update(VolumeManagementMode.SINGLE);
            return true;
        }

        @Override
        public boolean enabled() { return true; }
    }

    private class RearMatterModeSetting extends DependentOptionSetting<VolumeManagementMode> {

        private VolumeManagementMode mode = VolumeManagementMode.SINGLE;

        public Collection<VolumeManagementMode> options() {
            Collection<VolumeManagementMode> options = new ArrayList<VolumeManagementMode>();
            options.add(VolumeManagementMode.SINGLE);
            if (getRearMatterVolumeSections().size() +
                getAvailableRearMatterVolumeSections(null).size() > 0) {
                options.add(VolumeManagementMode.MANUAL);
            }
            return options;
        }

        protected boolean update(VolumeManagementMode value) {
            if (mode == value) { return false; }
            mode = value;
            return true;
        }

        public VolumeManagementMode get() { return mode; }

        public boolean refresh() {
            if (accept(get())) { return false; }
            update(VolumeManagementMode.SINGLE);
            return true;
        }

        @Override
        public boolean enabled() { return getRearMatterSection() != null; }
    }

    private class FrontMatterSectionSetting extends DependentOptionSetting<String> {

        private String name = null;

        public Collection<String> options() {
            Collection<String> options = new ArrayList<String>();
            String xpath = "./descendant::section[@name";
            if (getRearMatterSection() != null) { xpath += " and following::section[@name='" + getRearMatterSection() + "']"; }
            xpath += "]";
            try {
                NodeIterator sections = XPathAPI.selectNodeIterator(rootSection, xpath);
                for (Node section = sections.nextNode(); section != null; section = sections.nextNode()) {
                    options.add(section.getAttributes().getNamedItem("name").getNodeValue());
                }
            } catch (TransformerException e) {}
            return options;
        }

        @Override
        public boolean accept(String value) {
            if (value == null) { return true; }
            return super.accept(value);
        }

        protected boolean update(String value) {
            if ((name == null) ? (value == null) : name.equals(value)) { return false; }
            name = value;
            return true;
        }

        public String get() { return name; }

        public boolean refresh() {
            if (accept(get())) { return false; }
            try {
                return update(options().iterator().next());
            } catch (NoSuchElementException e) {
                return update(null);
            }
        }
    }

    private class RepeatFrontMatterSectionSetting extends DependentOptionSetting<String> {

        private String name = null;

        public Collection<String> options() {
            Collection<String> options = new ArrayList<String>();
            if (getFrontMatterSection() != null) {
                try {
                    options.add(getFrontMatterSection());
                    NodeIterator sections = XPathAPI.selectNodeIterator(rootSection, "//section[@name='" + getFrontMatterSection() + "']/descendant::section");
                    for (Node section = sections.nextNode(); section != null; section = sections.nextNode()) {
                        options.add(section.getAttributes().getNamedItem("name").getNodeValue());
                    }
                } catch (TransformerException e) {}
            }
            return options;
        }

        @Override
        public boolean accept(String value) {
            if (value == null) { return true; }
            return super.accept(value);
        }

        protected boolean update(String value) {
            if ((name == null) ? (value == null) : name.equals(value)) { return false; }
            name = value;
            return true;
        }

        public String get() { return name; }

        public boolean refresh() {
            if (accept(get())) { return false; }
            try {
                return update(options().iterator().next());
            } catch (NoSuchElementException e) {
                return update(null);
            }
        }
    }

    private class TitlePageSectionSetting extends DependentOptionSetting<String> {

        private String name = null;

        public Collection<String> options() {
            Collection<String> options = new ArrayList<String>();
            if (getFrontMatterSection() != null) {
                try {
                    options.add(getFrontMatterSection());
                    NodeIterator sections = XPathAPI.selectNodeIterator(rootSection, "//section[@name='" + getFrontMatterSection() + "']/descendant::section");
                    for (Node section = sections.nextNode(); section != null; section = sections.nextNode()) {
                        options.add(section.getAttributes().getNamedItem("name").getNodeValue());
                    }
                } catch (TransformerException e) {}
            }
            return options;
        }

        @Override
        public boolean accept(String value) {
            if (value == null) { return true; }
            return super.accept(value);
        }

        protected boolean update(String value) {
            if ((name == null) ? (value == null) : name.equals(value)) { return false; }
            name = value;
            return true;
        }

        public String get() { return name; }

        public boolean refresh() {
            if (accept(get())) { return false; }
            try {
                return update(options().iterator().next());
            } catch (NoSuchElementException e) {
                return update(null);
            }
        }
    }

    private class RearMatterSectionSetting extends DependentOptionSetting<String> {

        private String name = null;

        public Collection<String> options() {
            Collection<String> options = new ArrayList<String>();
            String xpath = "//section[@name";
            if (getFrontMatterSection() != null) {
                xpath += " and preceding::section[@name='" + getFrontMatterSection() + "']";
            }
            xpath += "]";
            try {
                NodeIterator sections = XPathAPI.selectNodeIterator(rootSection, xpath);
                for (Node section = sections.nextNode(); section != null; section = sections.nextNode()) {
                    options.add(section.getAttributes().getNamedItem("name").getNodeValue());
                }
            } catch (TransformerException e) {
            }
            return options;
        }

        @Override
        public boolean accept(String value) {
            if (value == null) { return true; }
            return super.accept(value);
        }

        protected boolean update(String value) {
            if ((name == null) ? (value == null) : name.equals(value)) { return false; }
            name = value;
            return true;
        }

        public String get() { return name; }

        public boolean refresh() {
            if (accept(get())) { return false; }
            try {
                return update(options().iterator().next());
            } catch (NoSuchElementException e) {
                return update(null);
            }
        }
    }

    public class ParagraphStyleMap extends SettingMap<String,ParagraphStyle> {

        private final Map<String,ParagraphStyle> map = new HashMap<String,ParagraphStyle>();

        public ParagraphStyleMap(Collection<ParagraphStyle> styles) {
            for (ParagraphStyle style : styles) {
                map.put(style.getID(), style);
            }
        }

        public ParagraphStyle get(String key) { return map.get(key); }
        public Collection<ParagraphStyle> values() { return map.values(); }
        public Collection<String> keys() { return map.keySet(); }
        protected void add(String key) {}
    }

    public class CharacterStyleMap extends SettingMap<String,CharacterStyle> {

        private final Map<String,CharacterStyle> map = new HashMap<String,CharacterStyle>();

        public CharacterStyleMap(Collection<CharacterStyle> styles) {
            for (CharacterStyle style : styles) {
                map.put(style.getID(), style);
            }
        }

        public CharacterStyle get(String key) { return map.get(key); }
        public Collection<CharacterStyle> values() { return map.values(); }
        public Collection<String> keys() { return map.keySet(); }
        protected void add(String key) {}
    }

    public class HeadingStyleMap extends SettingMap<Integer,HeadingStyle> {

        private final Map<Integer,HeadingStyle> map = new HashMap<Integer,HeadingStyle>();

        public HeadingStyleMap() {
            for (int i=1; i<=10; i++) { map.put(i, new HeadingStyle(i)); }
        }

        public HeadingStyle get(Integer key) { return map.get(key); }
        public Collection<HeadingStyle> values() { return map.values(); }
        public Collection<Integer> keys() { return map.keySet(); }
        protected void add(Integer key) {}
    }

    public class TableStyleMap extends SettingMap<String,TableStyle> {

        private final Map<String,TableStyle> map = new HashMap<String,TableStyle>();

        public TableStyleMap() {
            map.put("Default", new TableStyle("Default"));
        }

        public TableStyle get(String key) { return map.get(key); }
        public Collection<TableStyle> values() { return map.values(); }
        public Collection<String> keys() { return map.keySet(); }
        protected void add(String key) {
            if (!map.containsKey(key)) {
                map.put(key, new TableStyle(key));
            }
        }
    }

    public class ListStyleMap extends SettingMap<Integer,ListStyle> {

        private final Map<Integer,ListStyle> map = new HashMap<Integer,ListStyle>();

        public ListStyleMap() {
            ListStyle list = null;
            ListStyle parent = null;
            for (int i=1; i<=10; i++) {
                list = new ListStyle(i, parent);
                map.put(i, list);
                parent = list;
            }
        }

        public ListStyle get(Integer key) { return map.get(key); }
        public Collection<ListStyle> values() { return map.values(); }
        public Collection<Integer> keys() { return map.keySet(); }
        protected void add(Integer key) {}
    }

    private class ParagraphStyleSetting extends OptionSetting<ParagraphStyle> {

        private ParagraphStyle style = paragraphStyles.get("Standard");

        public Collection<ParagraphStyle> options() { return paragraphStyles.values(); }

        protected boolean update(ParagraphStyle value) {
            if (value == style) { return false; }
            style = value;
            return true;
        }

        public ParagraphStyle get() { return style; }
    }

    public class SpecialSymbolList extends SettingList<SpecialSymbol> {
    
        private List<SpecialSymbol> list = new ArrayList<SpecialSymbol>();

        public SpecialSymbolList() {

            String translationTable = getTranslationTables().get(mainLocale).getLocale();

            list.add(new SpecialSymbol(SpecialSymbol.Type.ELLIPSIS,                    mainLocale, translationTable));
            list.add(new SpecialSymbol(SpecialSymbol.Type.DOUBLE_DASH,                 mainLocale, translationTable));
            list.add(new SpecialSymbol(SpecialSymbol.Type.LETTER_INDICATOR,            mainLocale, translationTable));
            list.add(new SpecialSymbol(SpecialSymbol.Type.NUMBER_INDICATOR,            mainLocale, translationTable));
            list.add(new SpecialSymbol(SpecialSymbol.Type.TRANSCRIBERS_NOTE_INDICATOR, mainLocale, translationTable));
            list.add(new SpecialSymbol(SpecialSymbol.Type.NOTE_REFERENCE_INDICATOR,    mainLocale, translationTable));
            list.add(new SpecialSymbol(SpecialSymbol.Type.ITALIC_INDICATOR,            mainLocale, translationTable));
            list.add(new SpecialSymbol(SpecialSymbol.Type.BOLDFACE_INDICATOR,          mainLocale, translationTable));
        }

        public SpecialSymbol get(int index) {
            try {
                return list.get(index);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

        public List<SpecialSymbol> values() { return new ArrayList(list); }
        public void clear() { list.clear(); }
        public boolean canAdd() { return true; }

        public SpecialSymbol add() {
            SpecialSymbol symbol = new SpecialSymbol();
            list.add(symbol);
            return symbol;
        }

        public SpecialSymbol remove(int index) {
            try {
                return list.remove(index);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

        public void moveUp(int index) {
            try {
                Collections.swap(list, index, index-1);
            } catch (IndexOutOfBoundsException e) {}
        }

        public void moveDown(int index) {
            try {
                Collections.swap(list, index, index+1);
            } catch (IndexOutOfBoundsException e) {}
        }

    }

    public class Volume implements Serializable {

        public TextSetting           title;
        public DependentYesNoSetting frontMatter;
        public Setting<Boolean>      tableOfContent;
        public Setting<Boolean>      transcribersNotesPage;
        public Setting<Boolean>      specialSymbolList;

        public String  getTitle()                 { return title.get(); }
        public boolean getFrontMatter()           { return frontMatter.get(); }
        public boolean getTableOfContent()        { return tableOfContent.get(); }
        public boolean getTranscribersNotesPage() { return transcribersNotesPage.get(); }
        public boolean getSpecialSymbolList()     { return specialSymbolList.get(); }
        
        public void setTitle                 (String value)  { title.set(value); }
        public void setFrontMatter           (boolean value) { frontMatter.set(value); }
        public void setTableOfContent        (boolean value) { tableOfContent.set(value); }
        public void setTranscribersNotesPage (boolean value) { transcribersNotesPage.set(value); }
        public void setSpecialSymbolList     (boolean value) { specialSymbolList.set(value); }

        public Volume() {
            title = new TextSetting();
            frontMatter = new FrontMatterSetting();
            tableOfContent = new YesNoSetting();
            transcribersNotesPage = new YesNoSetting();
            specialSymbolList = new YesNoSetting();
            frontMatterSection.addListener(frontMatter);
        }

        private class FrontMatterSetting extends DependentYesNoSetting {
            public boolean accept(Boolean value) {
                return !value || (getFrontMatterSection() != null);
            }
        };

        @Override
        public boolean equals(Object object) {
            if (this == object) { return true; }
            if (!(object instanceof Volume)) { return false; }
            Volume that = (Volume)object;
            return this.title.equals(that.title) &&
                   this.frontMatter.equals(that.frontMatter) &&
                   this.tableOfContent.equals(that.tableOfContent) &&
                   this.transcribersNotesPage.equals(that.transcribersNotesPage) &&
                   this.specialSymbolList.equals(that.specialSymbolList);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 11 * hash + title.hashCode();
            hash = 11 * hash + frontMatter.hashCode();
            hash = 11 * hash + tableOfContent.hashCode();
            hash = 11 * hash + transcribersNotesPage.hashCode();
            hash = 11 * hash + specialSymbolList.hashCode();
            return hash;
        }
    }

    public class SectionVolume extends Volume {

        public OptionSetting<String> section;
        public String getSection() { return section.get(); }
        public void setSection(String value) { section.set(value); }

        private SectionVolume(String name) {
            section = new SectionSetting(name);
            section.addListener(sectionVolumeList);
        }

        private class SectionSetting extends OptionSetting<String> {

            private String name = null;

            private SectionSetting(String name) { this.name = name; }

            public Collection<String> options() {
                Collection<String> options = new ArrayList<String>();
                options.addAll(getAvailableBodyMatterVolumeSections(name));
                options.addAll(getAvailableRearMatterVolumeSections(name));
                return options;
            }

            protected boolean update(String value) {
                if (value.equals(name)) { return false; }
                volumeSectionsMap.put(value, volumeSectionsMap.remove(name));
                name = value;
                return true;
            }

            public String get() { return name; }
            
            @Override
            public boolean enabled() {
                if (options().size() < 2) { return false; }
                return super.enabled();
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) { return true; }
            if (!super.equals(object)) { return false; }
            if (!(object instanceof SectionVolume)) { return false; }
            SectionVolume that = (SectionVolume)object;
            return this.section.equals(that.section);
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = 11 * hash + section.hashCode();
            return hash;
        }
    }

    public class SplittableVolume extends Volume {
    
        public final DependentNumberSetting minVolumeSize;
        public final DependentNumberSetting maxVolumeSize;
        public final DependentNumberSetting minLastVolumeSize;
        public final DependentNumberSetting preferredVolumeSize;

        public int  getMinVolumeSize()        { return minVolumeSize.get(); }
        public int  getMaxVolumeSize()        { return maxVolumeSize.get(); }
        public int  getMinLastVolumeSize()    { return minLastVolumeSize.get(); }
        public int  getPreferredVolumeSize()  { return preferredVolumeSize.get(); }

        public void setMinVolumeSize          (int value)   { minVolumeSize.set(value); }
        public void setMaxVolumeSize          (int value)   { maxVolumeSize.set(value); }
        public void setMinLastVolumeSize      (int value)   { minLastVolumeSize.set(value); }
        public void setPreferredVolumeSize    (int value)   { preferredVolumeSize.set(value); }
        
        public SplittableVolume() {

            /* DECLARATION */

            minVolumeSize = new MinVolumeSizeSetting();
            maxVolumeSize = new MaxVolumeSizeSetting();
            minLastVolumeSize = new MinLastVolumeSizeSetting();
            preferredVolumeSize = new PreferredVolumeSizeSetting();

            /* INITIALIZATION */

            minVolumeSize.set(30);
            maxVolumeSize.set(40);
            minLastVolumeSize.set(20);
            preferredVolumeSize.set(35);

            /* LINKING */

            minVolumeSize.addListener(maxVolumeSize);
            minVolumeSize.addListener(preferredVolumeSize);
            minVolumeSize.addListener(minLastVolumeSize);
            maxVolumeSize.addListener(minVolumeSize);
            maxVolumeSize.addListener(preferredVolumeSize);
            preferredVolumeSize.addListener(minVolumeSize);
            preferredVolumeSize.addListener(maxVolumeSize);

            bodyMatterMode.addListener(minVolumeSize);
            bodyMatterMode.addListener(maxVolumeSize);
            bodyMatterMode.addListener(preferredVolumeSize);
            bodyMatterMode.addListener(minLastVolumeSize);

        }
        
        private class MinVolumeSizeSetting extends DependentNumberSetting {
            public boolean accept(Integer value) { return value > 0; }
            @Override
            public boolean enabled() { return getBodyMatterMode() == VolumeManagementMode.AUTOMATIC; }
            @Override
            public boolean refresh() { return update(Math.min(maxVolumeSize.get(), get())); }
        }

        private class MaxVolumeSizeSetting extends DependentNumberSetting {
            public boolean accept(Integer value) { return value > 0; }
            @Override
            public boolean enabled() { return getBodyMatterMode() == VolumeManagementMode.AUTOMATIC; }
            @Override
            public boolean refresh() { return update(Math.max(minVolumeSize.get(), get())); }
        }

        private class MinLastVolumeSizeSetting extends DependentNumberSetting {
            public boolean accept(Integer value) { return value > 0 && value <= minVolumeSize.get(); }
            @Override
            public boolean enabled() { return getBodyMatterMode() == VolumeManagementMode.AUTOMATIC; }
            @Override
            public boolean refresh() { return update(Math.min(minVolumeSize.get(), get())); }
        }

        private class PreferredVolumeSizeSetting extends DependentNumberSetting {
            public boolean accept(Integer value) { return value > 0; }
            @Override
            public boolean enabled() { return getBodyMatterMode() == VolumeManagementMode.AUTOMATIC; }
            @Override
            public boolean refresh() {
                return update(Math.min(maxVolumeSize.get(),
                              Math.max(minVolumeSize.get(), get())));
            }
        }
    }

    private class SectionVolumeList extends SettingList<SectionVolume>
                                 implements Dependent {

        private List<SectionVolume> list = new ArrayList<SectionVolume>();
        private Collection<String> available = new ArrayList<String>();

        public SectionVolume get(int index) {
            try {
                return list.get(index);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

        public List<SectionVolume> values() { return new ArrayList<SectionVolume>(list); }

        public SectionVolume add() {
            if (available.size() > 0) {
                SectionVolume volume = new SectionVolume(available.iterator().next());
                if (!volumeSectionsMap.containsKey(volume.getSection())) {
                    volumeSectionsMap.put(volume.getSection(), volume);
                    volume.setTitle(volume.getSection());
                    refresh();
                    return volume;
                }
            }
            return null;
        }

        public SectionVolume remove(int index) {
            try {
                SectionVolume volume = volumeSectionsMap.remove(get(index).getSection());
                refresh();
                return volume;
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }

        public boolean canAdd() { return available.size() > 0; }
        public void clear() {
            volumeSectionsMap.clear();
            refresh();
        }
        
        public boolean refresh() {
            list.clear();
            if (getBodyMatterMode() == VolumeManagementMode.MANUAL) {
                for (String section : getBodyMatterVolumeSections()) {
                    list.add(volumeSectionsMap.get(section));
                }
            }
            if (getRearMatterMode() == VolumeManagementMode.MANUAL) {
                for (String section : getRearMatterVolumeSections()) {
                    list.add(volumeSectionsMap.get(section));
                }
            }
            available.clear();
            if (getBodyMatterMode() == VolumeManagementMode.MANUAL) {
                available.addAll(getAvailableBodyMatterVolumeSections(null));
            }
            if (getRearMatterMode() == VolumeManagementMode.MANUAL) {
                available.addAll(getAvailableRearMatterVolumeSections(null));
            }
            return false;
        }

        public void propertyUpdated(PropertyEvent event) { refresh(); }

    }

    /********************/


    public void lock() { }


    /********************/
    /* HELPER FUNCTIONS */
    /********************/

    private String capitalizeFirstLetter(String in) {
        return in.substring(0,1).toUpperCase() + in.substring(1);
    }

    private static Locale stringToLocale(String s) {

        if (!s.contains("-")) {
            return new Locale(s);
        } else {
            int i = s.indexOf("-");
            return new Locale(s.substring(0,i), s.substring(i+1));
        }
    }

    private boolean sectionInBodyMatter(String section) {

        String xpath = "//section[@name='" + section + "'";
        if (getFrontMatterSection() != null) {
            xpath += " and preceding::section[@name='" + getFrontMatterSection() + "']";
        }
        if (getRearMatterSection() != null) {
            xpath += " and following::section[@name='" + getRearMatterSection() + "']";
        }
        xpath += "]";

        try {
            return (XPathAPI.selectSingleNode(rootSection, xpath) != null);
        } catch (TransformerException e) {
            return false;
        }
    }

    private boolean sectionInRearMatter(String section) {

        if (getRearMatterSection() == null) { return false; }
        try {
            return (XPathAPI.selectSingleNode(rootSection,
                        "//section[@name='" + getRearMatterSection() + "']/descendant::section[@name='" + section + "']") != null);
        } catch (TransformerException e) {
            return false;
        }
    }

    private List<String> getBodyMatterVolumeSections() {

        List<String> sections = new ArrayList<String>();
        String lastSection = null;

        for (String section : allSections) {
            if (volumeSectionsMap.containsKey(section)) {
                if (sectionInBodyMatter(section)) {
                    if (lastSection != null) {
                        try {
                            if (XPathAPI.selectSingleNode(rootSection, "//section[@name='" + lastSection + "']" +
                                    "/descendant::section[@name='" + section + "']") != null) {
                                continue;
                            }
                        } catch (TransformerException e) {
                        }
                    }
                    sections.add(section);
                    lastSection = section;
                }
            }
        }

        return sections;
    }

    private List<String> getRearMatterVolumeSections() {

        List<String> sections = new ArrayList<String>();
        String lastSection = null;

        if (getRearMatterSection() != null) {
            for (String section : allSections) {
                if (volumeSectionsMap.containsKey(section)) {
                    if (sectionInRearMatter(section)) {
                        if (lastSection != null) {
                            try {
                                if (XPathAPI.selectSingleNode(rootSection, "//section[@name='" + lastSection + "']" +
                                        "/descendant::section[@name='" + section + "']") != null) {
                                    continue;
                                }
                            } catch (TransformerException e) {
                            }
                        }
                        sections.add(section);
                        lastSection = section;
                    }
                }
            }
        }

        return sections;
    }

    private Collection<String> getAvailableBodyMatterVolumeSections(String currentSection) {

        Collection<String> availableSections = new ArrayList<String>();

        String xpath = "//section[@name";
        if (getFrontMatterSection() != null) {
            xpath += " and preceding::section[@name='" + getFrontMatterSection() + "']";
        }
        if (getRearMatterSection() != null) {
            xpath += " and following::section[@name='" + getRearMatterSection() + "']";
        }
        Collection<String> takenSections = getBodyMatterVolumeSections();
        if (currentSection != null) {
            takenSections.remove(currentSection);
        }
        for (String section : takenSections) {
            xpath += " and not(@name='" + section + "')";
            xpath += " and not(descendant::section[@name='" + section + "'])";
            xpath += " and not(ancestor::section[@name='" + section + "'])";
        }
        xpath += "]";
        try {
            NodeIterator sections = XPathAPI.selectNodeIterator(rootSection, xpath);
            for (Node section = sections.nextNode(); section != null; section = sections.nextNode()) {
                String name = section.getAttributes().getNamedItem("name").getNodeValue();
                availableSections.add(name);
            }
        } catch (TransformerException e) {
        }

        return availableSections;
    }

    private Collection<String> getAvailableRearMatterVolumeSections(String currentSection) {

        Collection<String> availableSections = new ArrayList<String>();

        if (getRearMatterSection() != null) {
            String xpath = "//section[@name='" + getRearMatterSection() + "']/descendant::section[@name";
            Collection<String> takenSections = getRearMatterVolumeSections();
            if (currentSection != null) { takenSections.remove(currentSection); }
            for (String section : takenSections) {
                xpath += " and not(@name='" + section + "')";
                xpath += " and not(descendant::section[@name='" + section + "'])";
                xpath += " and not(ancestor::section[@name='" + section + "'])";
            }
            xpath += "]";
            try {
                NodeIterator sections = XPathAPI.selectNodeIterator(rootSection, xpath);
                for (Node section = sections.nextNode(); section != null; section = sections.nextNode()) {
                    String name = section.getAttributes().getNamedItem("name").getNodeValue();
                    availableSections.add(name);
                }
            } catch (TransformerException e) {
            }
        }

        return availableSections;
    }

}