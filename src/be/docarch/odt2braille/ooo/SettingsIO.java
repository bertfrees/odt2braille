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

package be.docarch.odt2braille.ooo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.Properties;

import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertyContainer;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.beans.XPropertySet;
import com.sun.star.util.XModifiable;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.document.XDocumentProperties;
import com.sun.star.document.XDocumentPropertiesSupplier;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextDocument;

import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.BrailleFileExporter.BrailleFileType;
import be.docarch.odt2braille.Settings.MathType;
import be.docarch.odt2braille.Settings.BrailleRules;
import be.docarch.odt2braille.Settings.PageNumberFormat;
import be.docarch.odt2braille.Settings.PageNumberPosition;
import be.docarch.odt2braille.SpecialSymbol;
import be.docarch.odt2braille.SpecialSymbol.SpecialSymbolMode;
import be.docarch.odt2braille.SpecialSymbol.SpecialSymbolType;
import be.docarch.odt2braille.Style;
import be.docarch.odt2braille.Style.Alignment;
import be.docarch.odt2braille.ParagraphStyle;
import be.docarch.odt2braille.HeadingStyle;
import be.docarch.odt2braille.ListStyle;
import be.docarch.odt2braille.TableStyle;
import be.docarch.odt2braille.TocStyle;
import be.docarch.odt2braille.CharacterStyle;
import be.docarch.odt2braille.CharacterStyle.TypefaceOption;
import org_pef_text.pef2text.Paper.PaperSize;
import org_pef_text.pef2text.EmbosserFactory.EmbosserType;
import org_pef_text.TableFactory.TableType;

import java.io.IOException;


/**
 * This class has methods to save/load settings to/from
 * <ul>
 * <li>OpenOffice.org, or</li>
 * <li>the OpenOffice.org Writer document.</li>
 * </ul>
 *
 * @see         be.docarch.odt2braille.Settings
 * @author      Bert Frees
 */
public class SettingsIO {

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille");

    private XComponentContext xContext = null;
    private String packageLocation = null;

    private boolean odtModified = false;

    private XPropertyContainer xPropCont = null;
    private XPropertySet xPropSet = null;
    private XPropertySetInfo xPropSetInfo = null;
    private XModifiable xModifiable = null;

    private final static short OPTIONAL = (short) 256;

    // Braille settings

    private static String brailleRulesProperty =                 "[BRL]BrailleRules";
    private static String languageProperty =                     "[BRL]Language";
    private static String gradeProperty =                        "[BRL]Grade";
    private static String dotsProperty =                         "[BRL]Dots";
    private static String transcriptionInfoEnabledProperty =     "[BRL]TranscriptionInfo";
    private static String creatorProperty =                      "[BRL]Creator";
    private static String volumeInfoEnabledProperty =            "[BRL]VolumeInfo";
    private static String transcriptionInfoStyleProperty =       "[BRL]TranscriptionInfoStyle";
    private static String volumeInfoStyleProperty =              "[BRL]VolumeInfoStyle";
    private static String specialSymbolsListEnabledProperty =    "[BRL]ListOfSpecialSymbols";
    private static String specialSymbolsListTitleProperty =      "[BRL]ListOfSpecialSymbolsTitle";
    private static String transcribersNotesPageEnabledProperty = "[BRL]TNPage";
    private static String transcribersNotesPageTitleProperty =   "[BRL]TNPageTitle";
    private static String tableOfContentEnabledProperty =        "[BRL]TableOfContent";
    private static String tableOfContentTitleProperty =          "[BRL]TableOfContentTitle";
    private static String preliminaryVolumeEnabledProperty =     "[BRL]PreliminaryVolume";
    private static String stairstepTableProperty =               "[BRL]StairstepTable";
    private static String columnDelimiterProperty =              "[BRL]ColumnDelimiter";
    private static String lineFillSymbolProperty =               "[BRL]LineFillSymbol";
    private static String mathProperty =                         "[BRL]Math";
    private static String inheritProperty =                      "[BRL]Inherit";
    private static String alignmentProperty =                    "[BRL]Alignment";
    private static String firstLineProperty =                    "[BRL]FirstLineIndent";
    private static String runoversProperty =                     "[BRL]Runovers";
    private static String marginLeftRightProperty =              "[BRL]MarginLeftRight";
    private static String linesAboveProperty =                   "[BRL]LinesAbove";
    private static String linesBelowProperty =                   "[BRL]LinesBelow";
    private static String linesBetweenProperty =                 "[BRL]LinesBetween";
    private static String prefixProperty =                       "[BRL]ListPrefix";
    private static String keepEmptyParagraphsProperty =          "[BRL]KeepEmptyParagraphs";
    private static String newBraillePage =                       "[BRL]NewBraillePage";
    private static String dontSplitProperty =                    "[BRL]DontSplit";
    private static String dontSplitTableRowsProperty =           "[BRL]DontSplitTableRows";
    private static String dontSplitItemsProperty =               "[BRL]DontSplitItems";
    private static String keepWithNextProperty =                 "[BRL]KeepWithNext";
    private static String orphanControlEnabledProperty =         "[BRL]OrphanControlEnabled";
    private static String widowControlEnabledProperty =          "[BRL]WidowControlEnabled";
    private static String orphanControlProperty =                "[BRL]OrphanControl";
    private static String widowControlProperty =                 "[BRL]WidowControl";
    private static String boldProperty =                         "[BRL]Boldface";
    private static String italicProperty =                       "[BRL]Italic";
    private static String underlineProperty =                    "[BRL]Underline";
    private static String capsProperty =                         "[BRL]Capitals";
    private static String printPageNumbersProperty =             "[BRL]PrintPageNumbers";
    private static String braillePageNumbersProperty =           "[BRL]BraillePageNumbers";
    private static String pageSeparatorProperty =                "[BRL]PageSeparator";
    private static String pageSeparatorNumberProperty =          "[BRL]PageSeparatorNumber";
    private static String continuePagesProperty =                "[BRL]ContinuePages";
    private static String ignoreEmptyPagesProperty =             "[BRL]IgnoreEmptyPages";
    private static String mergeUnnumberedPagesProperty =         "[BRL]MergeUnnumberedPages";
    private static String pageNumberAtTopOnSepLineProperty =     "[BRL]PageNumberAtTopOnSepLine";
    private static String pageNumberAtBottomOnSepLineProperty =  "[BRL]PageNumberAtBottomOnSepLine";
    private static String printPageNumberRangeProperty =         "[BRL]PrintPageNumberRange";
    private static String printPageNumberAtProperty =            "[BRL]PrintPageNumberAt";
    private static String braillePageNumberAtProperty =          "[BRL]BraillePageNumberAt";
    private static String preliminaryPageNumberFormatProperty =  "[BRL]PreliminaryPageNumberFormat";
    private static String beginningBraillePageNumberProperty =   "[BRL]BeginningBraillePageNumber";
    private static String printPageNumbersInTocProperty =        "[BRL]PrintpageNumbersInToc";
    private static String braillePageNumbersInTocProperty =      "[BRL]BrintpageNumbersInToc";
    private static String hardPageBreaksProperty =               "[BRL]HardPageBreaks";
    private static String hyphenateProperty =                    "[BRL]Hyphenation";
    private static String specialSymbolProperty =                "[BRL]SpecialSymbol";
    private static String specialSymbolsCountProperty =          "[BRL]SpecialSymbolsCount";

    // Export settings

    private static String exportFileProperty =                   "[BRL]ExportFileType";
    private static String exportTableProperty =                  "[BRL]ExportCharacterSet";
    private static String exportNumberOfCellsPerLineProperty =   "[BRL]ExportCellsPerLine";
    private static String exportNumberOfLinesPerPageProperty =   "[BRL]ExportLinesPerPage";
    private static String exportDuplexProperty =                 "[BRL]ExportRectoVerso";
    private static String exportEightDotsProperty =              "[BRL]ExportEightDots";
    private static String exportMultipleFilesProperty =          "[BRL]ExportMultipleFiles";

    // Emboss settings

    private static String embosserProperty =                     "[BRL]Embosser";
    private static String saddleStitchProperty =                 "[BRL]SaddleStitch";
    private static String sheetsPerQuireProperty =               "[BRL]SheetsPerQuire";
    private static String zFoldingProperty =                     "[BRL]ZFolding";
    private static String paperSizeProperty =                    "[BRL]PaperSize";
    private static String customPaperWidthProperty =             "[BRL]CustomPaperWidth";
    private static String customPaperHeightProperty =            "[BRL]CustomPaperHeight";
    private static String marginLeftProperty =                   "[BRL]MarginLeft";
    private static String marginTopProperty =                    "[BRL]MarginTop";
    private static String embossTableProperty =                  "[BRL]EmbossCharacterSet";
    private static String embossNumberOfCellsPerLineProperty =   "[BRL]EmbossCellsPerLine";
    private static String embossNumberOfLinesPerPageProperty =   "[BRL]EmbossLinesPerPage";
    private static String embossDuplexProperty =                 "[BRL]EmbossRectoVerso";
    private static String embossEightDotsProperty =              "[BRL]EmbossEightDots";


    /**
     * Creates a new <code>SettingsIO</code> instance.
     *
     * @param   xContext
     */
    public SettingsIO(XComponentContext xContext,
                      XComponent xDesktopComponent)
               throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "<init>");

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        packageLocation = xPkgInfo.getPackageLocation("be.docarch.odt2braille.ooo.odt2brailleaddon");
        this.xContext = xContext;

        XTextDocument xTextDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xDesktopComponent);
        XDocumentPropertiesSupplier xDocInfoSuppl = (XDocumentPropertiesSupplier) UnoRuntime.queryInterface(XDocumentPropertiesSupplier.class, xTextDoc);
        XDocumentProperties xDocProps = xDocInfoSuppl.getDocumentProperties();

        xModifiable = (XModifiable)UnoRuntime.queryInterface(XModifiable.class, xDesktopComponent);
        xPropCont = xDocProps.getUserDefinedProperties();
        xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xPropCont);
        xPropSetInfo = xPropSet.getPropertySetInfo();

    }

    /**
     * Read a <code>String</code> property from the OpenOffice document
     *
     * @param   property    The property name
     * @return              The property value, if this property is set.
     *                      <code>null</code> otherwise.
     */
    private String getStringProperty (String property)
                               throws com.sun.star.beans.UnknownPropertyException,
                                      com.sun.star.lang.WrappedTargetException {

        String s;

        if (xPropSetInfo.hasPropertyByName(property)) {
            try {
                s = AnyConverter.toString(xPropSet.getPropertyValue(property));
                if (s.length() > 0) {
                    return s;
                } else {
                    return null;
                }
            } catch(com.sun.star.lang.IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, "The property " + property + "was not a string");
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Read a <code>Double</code> property from the OpenOffice document
     *
     * @param   property    The property name
     * @return              The property value, if this property is set.
     *                      <code>NaN</code> otherwise.
     */
    private Double getDoubleProperty(String property)
                              throws com.sun.star.beans.UnknownPropertyException,
                                     com.sun.star.lang.WrappedTargetException {

        if (xPropSetInfo.hasPropertyByName(property)) {
            try {
                return AnyConverter.toDouble(xPropSet.getPropertyValue(property));
            } catch(com.sun.star.lang.IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, "The property " + property + "was not a double");
                return null;
            }
        } else {
            return Double.NaN;
        }
    }

    /**
     * Read a <code>Boolean</code> property from the OpenOffice document
     *
     * @param   property    The property name
     * @return              The property value, if this property is set.
     *                      <code>null</code> otherwise.
     */
    private Boolean getBooleanProperty(String property)
                                throws com.sun.star.beans.UnknownPropertyException,
                                       com.sun.star.lang.WrappedTargetException {

        if (xPropSetInfo.hasPropertyByName(property)) {
            try {
                return AnyConverter.toBoolean(xPropSet.getPropertyValue(property));
            } catch(com.sun.star.lang.IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, "The property " + property + "was not a boolean");
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Load settings from the OpenOffice.org Writer document.
     * The settings are stored in the document as user-defined meta-data.
     *
     * @param   loadedSettings
     */
    public void loadBrailleSettingsFromDocument (Settings loadedSettings)
                                          throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "loadBrailleSettingsFromDocument");

        String s;
        Double d;
        Boolean b;

        ArrayList<String> languages = loadedSettings.getLanguages();
        ArrayList<ParagraphStyle> paragraphStyles = loadedSettings.getParagraphStyles();
        ArrayList<CharacterStyle> characterStyles = loadedSettings.getCharacterStyles();
        ArrayList<HeadingStyle> headingStyles = loadedSettings.getHeadingStyles();
        ArrayList<ListStyle> listStyles = loadedSettings.getListStyles();
        TableStyle tableStyle = loadedSettings.getTableStyle();
        TocStyle tocStyle = loadedSettings.getTocStyle();
        ParagraphStyle paraStyle = null;
        CharacterStyle charStyle = null;
        HeadingStyle headStyle = null;
        ListStyle listStyle = null;
        Style style = null;
        String styleName = null;
        int level = 0;

        for (int i=0;i<languages.size();i++) {
            if ((s = getStringProperty(languageProperty + "_" + languages.get(i))) != null) {
                loadedSettings.setTranslationTable(s, languages.get(i));
            }
            if (!(d = getDoubleProperty(gradeProperty + "_" + languages.get(i))).isNaN()) {
                loadedSettings.setGrade(d.intValue(), languages.get(i));
            }
            if (!(d = getDoubleProperty(dotsProperty + "_" + languages.get(i))).isNaN()) {
                loadedSettings.setDots(d.intValue(), languages.get(i));
            }
        }

        for (int i=0;i<paragraphStyles.size();i++) {

            paraStyle = paragraphStyles.get(i);
            styleName = paraStyle.getName();

            if (!paraStyle.getAutomatic()) {

                if ((b = getBooleanProperty(inheritProperty + "_paragraph_" + styleName)) != null) {
                    paraStyle.setInherit(b);
                }

                if (!paraStyle.getInherit()) {

                    if (!(d = getDoubleProperty(firstLineProperty + "_paragraph_" + styleName)).isNaN()) {
                        paraStyle.setFirstLine(d.intValue());
                    }
                    if (!((d = getDoubleProperty(runoversProperty + "_paragraph_" + styleName)).isNaN())) {
                        paraStyle.setRunovers(d.intValue());
                    }
                    if (!((d = getDoubleProperty(marginLeftRightProperty + "_paragraph_" + styleName)).isNaN())) {
                        paraStyle.setMarginLeftRight(d.intValue());
                    }
                    if (!((d = getDoubleProperty(linesAboveProperty + "_paragraph_" + styleName)).isNaN())) {
                        paraStyle.setLinesAbove(d.intValue());
                    }
                    if (!((d = getDoubleProperty(linesBelowProperty + "_paragraph_" + styleName)).isNaN())) {
                        paraStyle.setLinesBelow(d.intValue());
                    }
                    if ((s = getStringProperty(alignmentProperty + "_paragraph_" + styleName)) != null) {
                        try {
                            paraStyle.setAlignment(Alignment.valueOf(s));
                        } catch (IllegalArgumentException ex) {
                            logger.log(Level.SEVERE, null, s + " is no valid alignment option");
                        }
                    }
                    if ((b = getBooleanProperty(keepEmptyParagraphsProperty + "_paragraph_" + styleName)) != null) {
                        paraStyle.setKeepEmptyParagraphs(b);
                    }
                    if ((b = getBooleanProperty(widowControlEnabledProperty + "_paragraph_" + styleName)) != null) {
                        paraStyle.setWidowControlEnabled(b);
                    }
                    if ((b = getBooleanProperty(orphanControlEnabledProperty + "_paragraph_" + styleName)) != null) {
                        paraStyle.setOrphanControlEnabled(b);
                    }
                    if (!((d = getDoubleProperty(widowControlProperty + "_paragraph_" + styleName)).isNaN())) {
                        paraStyle.setWidowControl(d.intValue());
                    }
                    if (!((d = getDoubleProperty(orphanControlProperty + "_paragraph_" + styleName)).isNaN())) {
                        paraStyle.setOrphanControl(d.intValue());
                    }
                    if ((b = getBooleanProperty(dontSplitProperty + "_paragraph_" + styleName)) != null) {
                        paraStyle.setDontSplit(b);
                    }
                    if ((b = getBooleanProperty(keepWithNextProperty + "_paragraph_" + styleName)) != null) {
                        paraStyle.setKeepWithNext(b);
                    }
                }
            }
        }

        for (int i=0;i<characterStyles.size();i++) {

            charStyle = characterStyles.get(i);
            styleName = charStyle.getName();

            if ((b = getBooleanProperty(inheritProperty + "_text_" + styleName)) != null) {
                charStyle.setInherit(b);
            }

            if (!charStyle.getInherit()) {

                if ((s = getStringProperty(boldProperty + "_text_" + styleName)) != null) {
                    try {
                        charStyle.setBoldface(TypefaceOption.valueOf(s));
                    } catch (IllegalArgumentException ex) {
                        logger.log(Level.SEVERE, null, s + " is no valid special typeface option");
                    }
                }
                if ((s = getStringProperty(italicProperty + "_text_" + styleName)) != null) {
                    try {
                        charStyle.setItalic(TypefaceOption.valueOf(s));
                    } catch (IllegalArgumentException ex) {
                        logger.log(Level.SEVERE, null, s + " is no valid special typeface option");
                    }
                }
                if ((s = getStringProperty(underlineProperty + "_text_" + styleName)) != null) {
                    try {
                        charStyle.setUnderline(TypefaceOption.valueOf(s));
                    } catch (IllegalArgumentException ex) {
                        logger.log(Level.SEVERE, null, s + " is no valid special typeface option");
                    }
                }
                if ((s = getStringProperty(capsProperty + "_text_" + styleName)) != null) {
                    try {
                        charStyle.setCapitals(TypefaceOption.valueOf(s));
                    } catch (IllegalArgumentException ex) {
                        logger.log(Level.SEVERE, null, s + " is no valid special typeface option");
                    }
                }
            }
        }

        for (int i=0;i<headingStyles.size();i++) {

            headStyle = headingStyles.get(i);
            level = headStyle.getLevel();

            if (!(d = getDoubleProperty(firstLineProperty + "_heading_" + level)).isNaN()) {
                headStyle.setFirstLine(d.intValue());
            }
            if (!((d = getDoubleProperty(runoversProperty + "_heading_" + level)).isNaN())) {
                headStyle.setRunovers(d.intValue());
            }
            if (!((d = getDoubleProperty(marginLeftRightProperty + "_heading_" + level)).isNaN())) {
                headStyle.setMarginLeftRight(d.intValue());
            }
            if (!((d = getDoubleProperty(linesAboveProperty + "_heading_" + level)).isNaN())) {
                headStyle.setLinesAbove(d.intValue());
            }
            if (!((d = getDoubleProperty(linesBelowProperty + "_heading_" + level)).isNaN())) {
                headStyle.setLinesBelow(d.intValue());
            }
            if ((s = getStringProperty(alignmentProperty + "_heading_" + level)) != null) {
                try {
                    headStyle.setAlignment(Alignment.valueOf(s));
                } catch (IllegalArgumentException ex) {
                    logger.log(Level.SEVERE, null, s + " is no valid alignment option");
                }
            }
            if ((b = getBooleanProperty(dontSplitProperty + "_heading_" + level)) != null) {
                headStyle.setDontSplit(b);
            }
            if ((b = getBooleanProperty(keepWithNextProperty + "_heading_" + level)) != null) {
                headStyle.setKeepWithNext(b);
            }
            if ((b = getBooleanProperty(newBraillePage + "_heading_" + level)) != null) {
                headStyle.setNewBraillePage(b);
            }
        }

        for (int i=0;i<listStyles.size();i++) {

            listStyle = listStyles.get(i);
            level = listStyle.getLevel();

            if (!(d = getDoubleProperty(firstLineProperty + "_list_" + level)).isNaN()) {
                listStyle.setFirstLine(d.intValue());
            }
            if (!((d = getDoubleProperty(runoversProperty + "_list_" + level)).isNaN())) {
                listStyle.setRunovers(d.intValue());
            }
            if (!((d = getDoubleProperty(marginLeftRightProperty + "_list_" + level)).isNaN())) {
                listStyle.setMarginLeftRight(d.intValue());
            }
            if (!((d = getDoubleProperty(linesAboveProperty + "_list_" + level)).isNaN())) {
                listStyle.setLinesAbove(d.intValue());
            }
            if (!((d = getDoubleProperty(linesBelowProperty + "_list_" + level)).isNaN())) {
                listStyle.setLinesBelow(d.intValue());
            }
            if (!((d = getDoubleProperty(linesBetweenProperty + "_list_" + level)).isNaN())) {
                listStyle.setLinesBetween(d.intValue());
            }
            if ((s = getStringProperty(alignmentProperty + "_list_" + level)) != null) {
                try {
                    listStyle.setAlignment(Alignment.valueOf(s));
                } catch (IllegalArgumentException ex) {
                    logger.log(Level.SEVERE, null, s + " is no valid alignment option");
                }
            }
            if ((s = getStringProperty(prefixProperty + "_list_" + level)) != null) {
                listStyle.setPrefix(s);
            }
            if ((b = getBooleanProperty(dontSplitItemsProperty + "_list_" + level)) != null) {
                listStyle.setDontSplitItems(b);
            }
            if ((b = getBooleanProperty(dontSplitProperty + "_list_" + level)) != null) {
                listStyle.setDontSplit(b);
            }
        }

        if ((b = getBooleanProperty(stairstepTableProperty)) != null) {
            loadedSettings.setStairstepTable(b);
        }
        if ((s = getStringProperty(columnDelimiterProperty)) != null) {
            loadedSettings.setColumnDelimiter(s);
        }
        if ((b = getBooleanProperty(dontSplitTableRowsProperty)) != null) {
            tableStyle.setDontSplitRows(b);
        }
        if (!((d = getDoubleProperty(linesBetweenProperty + "_table")).isNaN())) {
            tableStyle.setLinesBetween(d.intValue());
        }
        if (!(d = getDoubleProperty(firstLineProperty + "_table")).isNaN()) {
            listStyle.setFirstLine(d.intValue());
        }
        if (!((d = getDoubleProperty(runoversProperty + "_table")).isNaN())) {
            listStyle.setRunovers(d.intValue());
        }
        if (!((d = getDoubleProperty(marginLeftRightProperty + "_table")).isNaN())) {
            listStyle.setMarginLeftRight(d.intValue());
        }
        if (!((d = getDoubleProperty(linesAboveProperty + "_table")).isNaN())) {
            listStyle.setLinesAbove(d.intValue());
        }
        if (!((d = getDoubleProperty(linesBelowProperty + "_table")).isNaN())) {
            listStyle.setLinesBelow(d.intValue());
        }
        if ((s = getStringProperty(alignmentProperty + "_table")) != null) {
            try {
                listStyle.setAlignment(Alignment.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid alignment option");
            }
        }

        for (int column=1;column<=10;column++) {

            style = tableStyle.getColumn(column);

            if (style!= null) {

                if (!(d = getDoubleProperty(firstLineProperty + "_table_" + column)).isNaN()) {
                    style.setFirstLine(d.intValue());
                }
                if (!((d = getDoubleProperty(runoversProperty + "_table_" + column)).isNaN())) {
                    style.setRunovers(d.intValue());
                }
                if (!((d = getDoubleProperty(marginLeftRightProperty + "_table_" + column)).isNaN())) {
                    style.setMarginLeftRight(d.intValue());
                }
                if (!((d = getDoubleProperty(linesAboveProperty + "_table_" + column)).isNaN())) {
                    style.setLinesAbove(d.intValue());
                }
                if (!((d = getDoubleProperty(linesBelowProperty + "_table_" + column)).isNaN())) {
                    style.setLinesBelow(d.intValue());
                }
                if ((s = getStringProperty(alignmentProperty + "_table_" + column)) != null) {
                    try {
                        style.setAlignment(Alignment.valueOf(s));
                    } catch (IllegalArgumentException ex) {
                        logger.log(Level.SEVERE, null, s + " is no valid alignment option");
                    }
                }
            }
        }

        if ((s = getStringProperty(tableOfContentTitleProperty)) != null) {
            loadedSettings.setTableOfContentTitle(s);
        }
        if ((s = getStringProperty(lineFillSymbolProperty)) != null) {
            if (s.length()==1) {
                loadedSettings.setLineFillSymbol(s.charAt(0));
            }
        }

        for (level=1;level<=4;level++) {

            style = tocStyle.getLevel(level);

            if (style!=null) {
                if (!(d = getDoubleProperty(firstLineProperty + "_toc_" + level)).isNaN()) {
                    style.setFirstLine(d.intValue());
                }
                if (!((d = getDoubleProperty(runoversProperty + "_toc_" + level)).isNaN())) {
                    style.setRunovers(d.intValue());
                }
            }
        }

        int loadedSpecialSymbolsCount = loadedSettings.getSpecialSymbolsList().size();
        int defaultSpecialSymbolsCount = loadedSpecialSymbolsCount;

        if (!(d = getDoubleProperty(specialSymbolsCountProperty)).isNaN()) {
            loadedSpecialSymbolsCount = d.intValue();
        }

        for (int i=0; i<loadedSpecialSymbolsCount; i++) {
            if (i>=defaultSpecialSymbolsCount) {
                int j = loadedSettings.addSpecialSymbol();
                assert(i == j);
            }
            if ((s = getStringProperty(specialSymbolProperty + "_" + (i+1))) != null) {
                loadedSettings.getSpecialSymbol(i).setSymbol(s);
            }
            if ((s = getStringProperty(specialSymbolProperty + "_" + (i+1) + "_Description")) != null) {
                loadedSettings.getSpecialSymbol(i).setDescription(s);
            }
            if ((s = getStringProperty(specialSymbolProperty + "_" + (i+1) + "_Type")) != null) {
                try {
                    loadedSettings.getSpecialSymbol(i).setType(SpecialSymbolType.valueOf(s));
                } catch (IllegalArgumentException ex) {
                    logger.log(Level.SEVERE, null, s + " is no valid special symbol type");
                }
            }
            if ((s = getStringProperty(specialSymbolProperty + "_" + (i+1) + "_Mode")) != null) {
                try {
                    loadedSettings.getSpecialSymbol(i).setMode(SpecialSymbolMode.valueOf(s));
                } catch (IllegalArgumentException ex) {
                    logger.log(Level.SEVERE, null, s + " is no valid special symbol mode");
                }
            }
        }

        if ((b = getBooleanProperty(hyphenateProperty)) != null) {
            loadedSettings.setHyphenate(b);
        }

        if ((s = getStringProperty(mathProperty)) != null) {
            try {
                loadedSettings.setMath(MathType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid math type");
            }
        }

        if ((b = getBooleanProperty(hardPageBreaksProperty)) != null) {
            loadedSettings.setHardPageBreaks(b);
        }

        if ((s = getStringProperty(creatorProperty)) != null) {
            loadedSettings.setCreator(s);
        }

        if ((s = getStringProperty(transcribersNotesPageTitleProperty)) != null) {
            loadedSettings.setTranscribersNotesPageTitle(s);
        }

        if ((s = getStringProperty(specialSymbolsListTitleProperty)) != null) {
            loadedSettings.setSpecialSymbolsListTitle(s);
        }

        if ((b = getBooleanProperty(printPageNumbersProperty)) != null) {
            loadedSettings.setPrintPageNumbers(b);
        }

        if ((b = getBooleanProperty(braillePageNumbersProperty)) != null) {
            loadedSettings.setBraillePageNumbers(b);
        }

        if ((b = getBooleanProperty(pageSeparatorProperty)) != null) {
            loadedSettings.setPageSeparator(b);
        }

        if ((b = getBooleanProperty(pageSeparatorNumberProperty)) != null) {
            loadedSettings.setPageSeparatorNumber(b);
        }

        if ((b = getBooleanProperty(continuePagesProperty)) != null) {
            loadedSettings.setContinuePages(b);
        }

        if ((b = getBooleanProperty(ignoreEmptyPagesProperty)) != null) {
            loadedSettings.setIgnoreEmptyPages(b);
        }

        if ((b = getBooleanProperty(mergeUnnumberedPagesProperty)) != null) {
            loadedSettings.setMergeUnnumberedPages(b);
        }

        if ((b = getBooleanProperty(printPageNumberRangeProperty)) != null) {
            loadedSettings.setPrintPageNumberRange(b);
        }

        if ((s = getStringProperty(printPageNumberAtProperty)) != null) {
            try {
                loadedSettings.setPrintPageNumberAt(PageNumberPosition.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid page number position");
            }
        }

        if ((s = getStringProperty(braillePageNumberAtProperty)) != null) {
            try {
                loadedSettings.setBraillePageNumberAt(PageNumberPosition.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid page number position");
            }
        }

        if ((s = getStringProperty(preliminaryPageNumberFormatProperty)) != null) {
            try {
                loadedSettings.setPreliminaryPageFormat(PageNumberFormat.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid page number format");
            }
        }

        if (!(d = getDoubleProperty(beginningBraillePageNumberProperty)).isNaN()) {
            loadedSettings.setBeginningBraillePageNumber(d.intValue());
        }

        if ((b = getBooleanProperty(printPageNumbersInTocProperty)) != null) {
            loadedSettings.setPrintPageNumbersInToc(b);
        }

        if ((b = getBooleanProperty(braillePageNumbersInTocProperty)) != null) {
            loadedSettings.setBraillePageNumbersInToc(b);
        }

        if ((b = getBooleanProperty(pageNumberAtTopOnSepLineProperty)) != null) {
            loadedSettings.setPageNumberAtTopOnSeparateLine(b);
        }

        if ((b = getBooleanProperty(pageNumberAtBottomOnSepLineProperty)) != null) {
            loadedSettings.setPageNumberAtBottomOnSeparateLine(b);
        }

        if ((b = getBooleanProperty(transcribersNotesPageEnabledProperty)) != null) {
            loadedSettings.setTranscribersNotesPageEnabled(b);
        }

        if ((b = getBooleanProperty(specialSymbolsListEnabledProperty)) != null) {
            loadedSettings.setSpecialSymbolsListEnabled(b);
        }

        if ((b = getBooleanProperty(tableOfContentEnabledProperty)) != null) {
            loadedSettings.setTableOfContentEnabled(b);
        }

        if ((b = getBooleanProperty(transcriptionInfoEnabledProperty)) != null) {
            loadedSettings.setTranscriptionInfoEnabled(b);
        }

        if ((b = getBooleanProperty(volumeInfoEnabledProperty)) != null) {
            loadedSettings.setVolumeInfoEnabled(b);
        }

        if ((s = getStringProperty(transcriptionInfoStyleProperty)) != null) {
            if (!loadedSettings.setTranscriptionInfoStyle(s)) {
                logger.log(Level.SEVERE, null, s + " is no valid paragraph style");
            }
        }

        if ((s = getStringProperty(volumeInfoStyleProperty)) != null) {
            if (!loadedSettings.setVolumeInfoStyle(s)) {
                logger.log(Level.SEVERE, null, s + " is no valid paragraph style");
            }
        }

        if ((b = getBooleanProperty(preliminaryVolumeEnabledProperty)) != null) {
            loadedSettings.setPreliminaryVolumeEnabled(b);
        }

        if ((s = getStringProperty(brailleRulesProperty)) != null) {
            try {
                loadedSettings.setBrailleRules(BrailleRules.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid braille rules type");
            }
        }

        logger.exiting("SettingsIO", "loadBrailleSettingsFromDocument");

    }

    public void loadExportSettingsFromDocument (Settings loadedSettings)
                                         throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "loadExportSettingsFromDocument");

        String s;
        Boolean b;
        Double d;

        loadedSettings.setExportOrEmboss(true);

        if ((s = getStringProperty(exportFileProperty)) != null) {
            try {
                loadedSettings.setBrailleFileType(BrailleFileType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid braille file type");
            }
        }

        if ((s = getStringProperty(exportTableProperty)) != null) {
            try {
                loadedSettings.setTable(TableType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid tabletype");
            }
        }

        if ((b = getBooleanProperty(exportDuplexProperty)) != null) {
            try {
                loadedSettings.setDuplex(b);
            } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        if ((b = getBooleanProperty(exportEightDotsProperty)) != null) {
            try {
                loadedSettings.setEightDots(b);
            } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        if (!(d = getDoubleProperty(exportNumberOfCellsPerLineProperty)).isNaN()) {
            loadedSettings.setCellsPerLine(d.intValue());
        }

        if (!(d = getDoubleProperty(exportNumberOfLinesPerPageProperty)).isNaN()) {
            loadedSettings.setLinesPerPage(d.intValue());
        }

        if ((b = getBooleanProperty(exportMultipleFilesProperty)) != null) {
            loadedSettings.setMultipleFiles(b);
        }

        logger.exiting("SettingsIO", "loadExportSettingsFromDocument");

    }

    public void loadEmbossSettingsFromDocument (Settings loadedSettings)
                                         throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "loadEmbossSettingsFromDocument");

        Double d;

        if (!(d = getDoubleProperty(embossNumberOfCellsPerLineProperty)).isNaN()) {
            loadedSettings.setCellsPerLine(d.intValue());
        }

        if (!(d = getDoubleProperty(embossNumberOfLinesPerPageProperty)).isNaN()) {
            loadedSettings.setLinesPerPage(d.intValue());
        }

        if (!(d = getDoubleProperty(marginLeftProperty)).isNaN()) {
            loadedSettings.setMarginInner(d.intValue());
        }

        if (!(d = getDoubleProperty(marginTopProperty)).isNaN()) {
            loadedSettings.setMarginTop(d.intValue());
        }

        logger.exiting("SettingsIO", "loadEmbossSettingsFromDocument");

    }

    public void loadEmbossSettingsFromOpenOffice(Settings loadedSettings)
                                          throws IOException {

        logger.entering("SettingsIO", "loadEmbossSettingsFromOpenOffice");

        Properties embosserSettings = loadSettingsFromOpenOffice("embosser");

        loadedSettings.setExportOrEmboss(false);

        String s;

        if ((s = embosserSettings.getProperty(embosserProperty)) != null) {
            try {
                try {
                    loadedSettings.setEmbosser(EmbosserType.valueOf(s));
                } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid embossertype");
            }
        }

        if ((s = embosserSettings.getProperty(saddleStitchProperty)) != null) {
            try {
                loadedSettings.setSaddleStitch(s.equals("true"));
            } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        if ((s = embosserSettings.getProperty(sheetsPerQuireProperty)) != null) {
            try {
                loadedSettings.setSheetsPerQuire(Integer.parseInt(s));
            } catch (NumberFormatException ex) {
                logger.log(Level.SEVERE, null, s + " is not an integer");
            }
        }
            
        if ((s = embosserSettings.getProperty(zFoldingProperty)) != null) {
            try {
                loadedSettings.setZFolding(s.equals("true"));
            } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        if ((s = embosserSettings.getProperty(embossDuplexProperty)) != null) {
            try {
                loadedSettings.setDuplex(s.equals("true"));
            } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        if ((s = embosserSettings.getProperty(embossEightDotsProperty)) != null) {
            try {
                loadedSettings.setEightDots(s.equals("true"));
            } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        if ((s = embosserSettings.getProperty(paperSizeProperty)) != null) {
            try {
                loadedSettings.setPaperSize(PaperSize.valueOf(s));
                if (s.equals("CUSTOM")) {
                    String s1;
                    String s2;
                    if ((s1 = embosserSettings.getProperty(customPaperWidthProperty)) != null &&
                        (s2 = embosserSettings.getProperty(customPaperHeightProperty)) != null) {
                        try {
                            loadedSettings.setPaperSize(Integer.parseInt(s1), Integer.parseInt(s2));
                        } catch (NumberFormatException ex) {
                            logger.log(Level.SEVERE, null, s1 + " or " + s2 + " is not an integer");
                        }
                    }
                }
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid papersize");
            } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        if ((s = embosserSettings.getProperty(embossTableProperty)) != null) {
            try {
                loadedSettings.setTable(TableType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid tabletype");
            }
        }

        logger.exiting("SettingsIO", "loadEmbossSettingsFromOpenOffice");
    }

    /**
     * Write a <code>String</code>, <code>Double</code> or <code>Boolean</code> property to the OpenOffice document,
     * but only if the new property value differs from the old value.
     *
     * @param   property            The property name.
     * @param   valueAfterChange    The new property value (after the settings are changed).
     * @param   valueBeforeChange   The old property value (before the settings are changed).
     */
    private void setProperty (String property,
                              Object valueAfterChange,
                              Object valueBeforeChange)
                       throws com.sun.star.uno.Exception {

        if (valueAfterChange!=null) {
            if (!valueAfterChange.equals(valueBeforeChange)) {
                if (!(xPropSetInfo.hasPropertyByName(property))) {
                    xPropCont.addProperty(property,OPTIONAL,valueAfterChange);
                } else {
                    xPropSet.setPropertyValue(property,valueAfterChange);
                }
                odtModified = true;
            }
        }
    }

    private void setProperty(Properties properties,
                             String property,
                             Object valueAfterChange,
                             Object valueBeforeChange) {

        if (valueAfterChange!=null) {
            if (!valueAfterChange.equals(valueBeforeChange)) {
                properties.setProperty(property, String.valueOf(valueAfterChange));
                odtModified = true;
            }
        }
    }

    /**
     * Save settings to the OpenOffice.org Writer document.
     * The settings are stored in the document as user-defined meta-data.
     *
     * @param   settingsAfterChange     The new settings.
     * @param   settingsBeforeChange    The old settings.
     */
    public void saveBrailleSettingsToDocument (Settings settingsAfterChange,
                                               Settings settingsBeforeChange)
                                        throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "saveBrailleSettingsToDocument");

        odtModified = false;

        ArrayList<String> languages = settingsAfterChange.getLanguages();
        ArrayList<SpecialSymbol> specialSymbolsAfterChange = settingsAfterChange.getSpecialSymbolsList();
        ArrayList<SpecialSymbol> specialSymbolsBeforeChange = settingsBeforeChange.getSpecialSymbolsList();
        ArrayList<ParagraphStyle> paragraphStylesAfterChange = settingsAfterChange.getParagraphStyles();
        ArrayList<ParagraphStyle> paragraphStylesBeforeChange = settingsBeforeChange.getParagraphStyles();
        ArrayList<CharacterStyle> characterStylesAfterChange = settingsAfterChange.getCharacterStyles();
        ArrayList<CharacterStyle> characterStylesBeforeChange = settingsBeforeChange.getCharacterStyles();
        ArrayList<HeadingStyle> headingStylesAfterChange = settingsAfterChange.getHeadingStyles();
        ArrayList<HeadingStyle> headingStylesBeforeChange = settingsBeforeChange.getHeadingStyles();
        ArrayList<ListStyle> listStylesBeforeChange = settingsBeforeChange.getListStyles();
        ArrayList<ListStyle> listStylesAfterChange = settingsAfterChange.getListStyles();
        TableStyle tableStyleBeforeChange = settingsBeforeChange.getTableStyle();
        TableStyle tableStyleAfterChange = settingsAfterChange.getTableStyle();
        TocStyle tocStyleBeforeChange = settingsBeforeChange.getTocStyle();
        TocStyle tocStyleAfterChange = settingsAfterChange.getTocStyle();
        ParagraphStyle paraStyleAfterChange = null;
        ParagraphStyle paraStyleBeforeChange = null;
        CharacterStyle charStyleAfterChange = null;
        CharacterStyle charStyleBeforeChange = null;
        HeadingStyle headStyleAfterChange = null;
        HeadingStyle headStyleBeforeChange = null;
        ListStyle listStyleAfterChange = null;
        ListStyle listStyleBeforeChange = null;
        Style styleAfterChange = null;
        Style styleBeforeChange = null;
        String styleName = null;
        int level = 0;

        for (int i=0;i<languages.size();i++) {
            setProperty(languageProperty + "_" + languages.get(i),
                        settingsAfterChange.getTranslationTable(languages.get(i)),
                        settingsBeforeChange.getTranslationTable(languages.get(i)));
            setProperty(gradeProperty + "_" + languages.get(i),
                        settingsAfterChange.getGrade(languages.get(i)),
                        settingsBeforeChange.getGrade(languages.get(i)));
            setProperty(dotsProperty + "_" + languages.get(i),
                        settingsAfterChange.getDots(languages.get(i)),
                        settingsBeforeChange.getDots(languages.get(i)));
        }

        for (int i=0;i<paragraphStylesBeforeChange.size();i++) {

            paraStyleAfterChange = paragraphStylesAfterChange.get(i);
            paraStyleBeforeChange = paragraphStylesBeforeChange.get(i);
            styleName = paraStyleBeforeChange.getName();

            if (!paraStyleBeforeChange.getAutomatic()) {

                setProperty(inheritProperty + "_paragraph_" + styleName,
                            paraStyleAfterChange.getInherit(),
                            paraStyleBeforeChange.getInherit());

                if (!paraStyleAfterChange.getInherit()) {

                    setProperty(alignmentProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getAlignment().name(),
                                paraStyleBeforeChange.getAlignment().name());
                    setProperty(firstLineProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getFirstLine(),
                                paraStyleBeforeChange.getFirstLine());
                    setProperty(runoversProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getRunovers(),
                                paraStyleBeforeChange.getRunovers());
                    setProperty(marginLeftRightProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getMarginLeftRight(),
                                paraStyleBeforeChange.getMarginLeftRight());
                    setProperty(linesAboveProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getLinesAbove(),
                                paraStyleBeforeChange.getLinesAbove());
                    setProperty(linesBelowProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getLinesBelow(),
                                paraStyleBeforeChange.getLinesBelow());
                    setProperty(keepEmptyParagraphsProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getKeepEmptyParagraphs(),
                                paraStyleBeforeChange.getKeepEmptyParagraphs());
                    setProperty(dontSplitProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getDontSplit(),
                                paraStyleBeforeChange.getDontSplit());
                    setProperty(keepWithNextProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getKeepWithNext(),
                                paraStyleBeforeChange.getKeepWithNext());
                    setProperty(widowControlEnabledProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getWidowControlEnabled(),
                                paraStyleBeforeChange.getWidowControlEnabled());
                    setProperty(orphanControlEnabledProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getOrphanControlEnabled(),
                                paraStyleBeforeChange.getOrphanControlEnabled());
                    setProperty(widowControlProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getWidowControl(),
                                paraStyleBeforeChange.getWidowControl());
                    setProperty(orphanControlProperty + "_paragraph_" + styleName,
                                paraStyleAfterChange.getOrphanControl(),
                                paraStyleBeforeChange.getOrphanControl());
                }
            }
        }

        for (int i=0;i<characterStylesBeforeChange.size();i++) {

            charStyleAfterChange = characterStylesAfterChange.get(i);
            charStyleBeforeChange = characterStylesBeforeChange.get(i);
            styleName = charStyleBeforeChange.getName();

            setProperty(inheritProperty + "_text_" + styleName,
                        (charStyleAfterChange.getInherit()),
                        (charStyleBeforeChange.getInherit()));

            if (!charStyleAfterChange.getInherit()) {

                setProperty(boldProperty + "_text_" + styleName,
                            (charStyleAfterChange.getBoldface().name()),
                            (charStyleBeforeChange.getBoldface().name()));
                setProperty(italicProperty + "_text_" + styleName,
                            (charStyleAfterChange.getItalic().name()),
                            (charStyleBeforeChange.getItalic().name()));
                setProperty(underlineProperty + "_text_" + styleName,
                            (charStyleAfterChange.getUnderline().name()),
                            (charStyleBeforeChange.getUnderline().name()));
                setProperty(capsProperty + "_text_" + styleName,
                            (charStyleAfterChange.getCapitals().name()),
                            (charStyleBeforeChange.getCapitals().name()));

            }
        }

        for (int i=0;i<headingStylesBeforeChange.size();i++) {

            headStyleAfterChange = headingStylesAfterChange.get(i);
            headStyleBeforeChange = headingStylesBeforeChange.get(i);
            level = headStyleBeforeChange.getLevel();

            setProperty(alignmentProperty + "_heading_" + level,
                        headStyleAfterChange.getAlignment().name(),
                        headStyleBeforeChange.getAlignment().name());
            setProperty(firstLineProperty + "_heading_" + level,
                        headStyleAfterChange.getFirstLine(),
                        headStyleBeforeChange.getFirstLine());
            setProperty(runoversProperty + "_heading_" + level,
                        headStyleAfterChange.getRunovers(),
                        headStyleBeforeChange.getRunovers());
            setProperty(marginLeftRightProperty + "_heading_" + level,
                        headStyleAfterChange.getMarginLeftRight(),
                        headStyleBeforeChange.getMarginLeftRight());
            setProperty(linesAboveProperty + "_heading_" + level,
                        headStyleAfterChange.getLinesAbove(),
                        headStyleBeforeChange.getLinesAbove());
            setProperty(linesBelowProperty + "_heading_" + level,
                        headStyleAfterChange.getLinesBelow(),
                        headStyleBeforeChange.getLinesBelow());
            setProperty(newBraillePage + "_heading_" + level,
                        headStyleAfterChange.getNewBraillePage(),
                        headStyleBeforeChange.getNewBraillePage());
            setProperty(dontSplitProperty + "_heading_" + level,
                        headStyleAfterChange.getDontSplit(),
                        headStyleBeforeChange.getDontSplit());
            setProperty(keepWithNextProperty + "_heading_" + level,
                        headStyleAfterChange.getKeepWithNext(),
                        headStyleBeforeChange.getKeepWithNext());

        }

        for (int i=0;i<listStylesBeforeChange.size();i++) {

            listStyleAfterChange = listStylesAfterChange.get(i);
            listStyleBeforeChange = listStylesBeforeChange.get(i);
            level = listStyleBeforeChange.getLevel();

            setProperty(alignmentProperty + "_list_" + level,
                        listStyleAfterChange.getAlignment().name(),
                        listStyleBeforeChange.getAlignment().name());
            setProperty(firstLineProperty + "_list_" + level,
                        listStyleAfterChange.getFirstLine(),
                        listStyleBeforeChange.getFirstLine());
            setProperty(runoversProperty + "_list_" + level,
                        listStyleAfterChange.getRunovers(),
                        listStyleBeforeChange.getRunovers());
            setProperty(marginLeftRightProperty + "_list_" + level,
                        listStyleAfterChange.getMarginLeftRight(),
                        listStyleBeforeChange.getMarginLeftRight());
            setProperty(linesAboveProperty + "_list_" + level,
                        listStyleAfterChange.getLinesAbove(),
                        listStyleBeforeChange.getLinesAbove());
            setProperty(linesBelowProperty + "_list_" + level,
                        listStyleAfterChange.getLinesBelow(),
                        listStyleBeforeChange.getLinesBelow());
            setProperty(linesBetweenProperty + "_list_" + level,
                        listStyleAfterChange.getLinesBetween(),
                        listStyleBeforeChange.getLinesBetween());
            setProperty(prefixProperty + "_list_" + level,
                        listStyleAfterChange.getPrefix(),
                        listStyleBeforeChange.getPrefix());
            setProperty(dontSplitItemsProperty + "_list_" + level,
                        listStyleAfterChange.getDontSplitItems(),
                        listStyleBeforeChange.getDontSplitItems());
            setProperty(dontSplitProperty + "_list_" + level,
                        listStyleAfterChange.getDontSplit(),
                        listStyleBeforeChange.getDontSplit());
        }

        setProperty(stairstepTableProperty,
                    settingsAfterChange.stairstepTableIsEnabled(),
                    settingsBeforeChange.stairstepTableIsEnabled());
        setProperty(columnDelimiterProperty,
                    settingsAfterChange.getColumnDelimiter(),
                    settingsBeforeChange.getColumnDelimiter());
        setProperty(dontSplitTableRowsProperty,
                    tableStyleAfterChange.getDontSplitRows(),
                    tableStyleBeforeChange.getDontSplitRows());
        setProperty(alignmentProperty + "_table",
                    tableStyleAfterChange.getAlignment().name(),
                    tableStyleBeforeChange.getAlignment().name());
        setProperty(firstLineProperty + "_table",
                    tableStyleAfterChange.getFirstLine(),
                    tableStyleBeforeChange.getFirstLine());
        setProperty(runoversProperty + "_table",
                    tableStyleAfterChange.getRunovers(),
                    tableStyleBeforeChange.getRunovers());
        setProperty(marginLeftRightProperty + "_table",
                    tableStyleAfterChange.getMarginLeftRight(),
                    tableStyleBeforeChange.getMarginLeftRight());
        setProperty(linesAboveProperty + "_table",
                    tableStyleAfterChange.getLinesAbove(),
                    tableStyleBeforeChange.getLinesAbove());
        setProperty(linesBelowProperty + "_table",
                    tableStyleAfterChange.getLinesBelow(),
                    tableStyleBeforeChange.getLinesBelow());
        setProperty(linesBetweenProperty + "_table",
                    tableStyleAfterChange.getLinesBetween(),
                    tableStyleBeforeChange.getLinesBetween());

        for (int column=1;column<=10;column++) {

            styleAfterChange = tableStyleAfterChange.getColumn(column);
            styleBeforeChange = tableStyleBeforeChange.getColumn(column);

            if (styleAfterChange!= null && styleBeforeChange!=null) {

                setProperty(alignmentProperty + "_table_" + column,
                            styleAfterChange.getAlignment().name(),
                            styleBeforeChange.getAlignment().name());
                setProperty(firstLineProperty + "_table_" + column,
                            styleAfterChange.getFirstLine(),
                            styleBeforeChange.getFirstLine());
                setProperty(runoversProperty + "_table_" + column,
                            styleAfterChange.getRunovers(),
                            styleBeforeChange.getRunovers());
                setProperty(marginLeftRightProperty + "_table_" + column,
                            styleAfterChange.getMarginLeftRight(),
                            styleBeforeChange.getMarginLeftRight());
                setProperty(linesAboveProperty + "_table_" + column,
                            styleAfterChange.getLinesAbove(),
                            styleBeforeChange.getLinesAbove());
                setProperty(linesBelowProperty + "_table_" + column,
                            styleAfterChange.getLinesBelow(),
                            styleBeforeChange.getLinesBelow());
            }
        }

        setProperty(tableOfContentTitleProperty,
                    settingsAfterChange.getTableOfContentTitle(),
                    settingsBeforeChange.getTableOfContentTitle());
        setProperty(lineFillSymbolProperty,
                    String.valueOf(settingsAfterChange.getLineFillSymbol()),
                    String.valueOf(settingsBeforeChange.getLineFillSymbol()));

        for (level=1;level<=4;level++) {

            styleAfterChange = tocStyleAfterChange.getLevel(level);
            styleBeforeChange = tocStyleBeforeChange.getLevel(level);

            if (styleAfterChange!= null && styleBeforeChange!=null) {

                setProperty(firstLineProperty + "_toc_" + level,
                            styleAfterChange.getFirstLine(),
                            styleBeforeChange.getFirstLine());
                setProperty(runoversProperty + "_toc_" + level,
                            styleAfterChange.getRunovers(),
                            styleBeforeChange.getRunovers());
            }
        }

        setProperty(specialSymbolsCountProperty,
                    specialSymbolsAfterChange.size(),
                    specialSymbolsBeforeChange.size());

        for (int i=0;i<Math.min(specialSymbolsAfterChange.size(),
                                specialSymbolsBeforeChange.size());i++) {
            setProperty(specialSymbolProperty + "_" + (i+1),
                        specialSymbolsAfterChange.get(i).getSymbol(),
                        specialSymbolsBeforeChange.get(i).getSymbol());
            setProperty(specialSymbolProperty + "_" + (i+1) + "_Description",
                        specialSymbolsAfterChange.get(i).getDescription(),
                        specialSymbolsBeforeChange.get(i).getDescription());
            setProperty(specialSymbolProperty + "_" + (i+1) + "_Type",
                        specialSymbolsAfterChange.get(i).getType().name(),
                        specialSymbolsBeforeChange.get(i).getType().name());
            setProperty(specialSymbolProperty + "_" + (i+1) + "_Mode",
                        specialSymbolsAfterChange.get(i).getMode().name(),
                        specialSymbolsBeforeChange.get(i).getMode().name());
        }

        for (int i=specialSymbolsBeforeChange.size();i<specialSymbolsAfterChange.size();i++) {
            setProperty(specialSymbolProperty + "_" + (i+1),
                        specialSymbolsAfterChange.get(i).getSymbol(), null);
            setProperty(specialSymbolProperty + "_" + (i+1) + "_Description",
                        specialSymbolsAfterChange.get(i).getDescription(), null);
            setProperty(specialSymbolProperty + "_" + (i+1) + "_Type",
                        specialSymbolsAfterChange.get(i).getType().name(), null);
            setProperty(specialSymbolProperty + "_" + (i+1) + "_Mode",
                        specialSymbolsAfterChange.get(i).getMode().name(), null);
        }
        
        setProperty(hyphenateProperty,
                    settingsAfterChange.getHyphenate(),
                    settingsBeforeChange.getHyphenate());
        setProperty(creatorProperty,
                    settingsAfterChange.getCreator(),
                    settingsBeforeChange.getCreator());
        setProperty(transcribersNotesPageTitleProperty,
                    settingsAfterChange.getTranscribersNotesPageTitle(),
                    settingsBeforeChange.getTranscribersNotesPageTitle());
        setProperty(specialSymbolsListTitleProperty,
                    settingsAfterChange.getSpecialSymbolsListTitle(),
                    settingsBeforeChange.getSpecialSymbolsListTitle());
        setProperty(printPageNumbersProperty,
                    settingsAfterChange.getPrintPageNumbers(),
                    settingsBeforeChange.getPrintPageNumbers());
        setProperty(braillePageNumbersProperty,
                    settingsAfterChange.getBraillePageNumbers(),
                    settingsBeforeChange.getBraillePageNumbers());
        setProperty(pageSeparatorProperty,
                    settingsAfterChange.getPageSeparator(),
                    settingsBeforeChange.getPageSeparator());
        setProperty(pageSeparatorNumberProperty,
                    settingsAfterChange.getPageSeparatorNumber(),
                    settingsBeforeChange.getPageSeparatorNumber());
        setProperty(continuePagesProperty,
                    settingsAfterChange.getContinuePages(),
                    settingsBeforeChange.getContinuePages());
        setProperty(ignoreEmptyPagesProperty,
                    settingsAfterChange.getIgnoreEmptyPages(),
                    settingsBeforeChange.getIgnoreEmptyPages());
        setProperty(mergeUnnumberedPagesProperty,
                    settingsAfterChange.getMergeUnnumberedPages(),
                    settingsBeforeChange.getMergeUnnumberedPages());
        setProperty(pageNumberAtTopOnSepLineProperty,
                    settingsAfterChange.getPageNumberAtTopOnSeparateLine(),
                    settingsBeforeChange.getPageNumberAtTopOnSeparateLine());
        setProperty(pageNumberAtBottomOnSepLineProperty,
                    settingsAfterChange.getPageNumberAtBottomOnSeparateLine(),
                    settingsBeforeChange.getPageNumberAtBottomOnSeparateLine());
        setProperty(printPageNumberRangeProperty,
                    settingsAfterChange.getPrintPageNumberRange(),
                    settingsBeforeChange.getPrintPageNumberRange());
        setProperty(printPageNumberAtProperty,
                    settingsAfterChange.getPrintPageNumberAt().name(),
                    settingsBeforeChange.getPrintPageNumberAt().name());
        setProperty(braillePageNumberAtProperty,
                    settingsAfterChange.getBraillePageNumberAt().name(),
                    settingsBeforeChange.getBraillePageNumberAt().name());
        setProperty(preliminaryPageNumberFormatProperty,
                    settingsAfterChange.getPreliminaryPageFormat().name(),
                    settingsBeforeChange.getPreliminaryPageFormat().name());
        setProperty(beginningBraillePageNumberProperty,
                    settingsAfterChange.getBeginningBraillePageNumber(),
                    settingsBeforeChange.getBeginningBraillePageNumber());
        setProperty(printPageNumbersInTocProperty,
                    settingsAfterChange.getPrintPageNumbersInToc(),
                    settingsBeforeChange.getPrintPageNumbersInToc());
        setProperty(braillePageNumbersInTocProperty,
                    settingsAfterChange.getBraillePageNumbersInToc(),
                    settingsBeforeChange.getBraillePageNumbersInToc());
        setProperty(transcribersNotesPageEnabledProperty,
                    settingsAfterChange.getTranscribersNotesPageEnabled(),
                    settingsBeforeChange.getTranscribersNotesPageEnabled());
        setProperty(specialSymbolsListEnabledProperty,
                    settingsAfterChange.getSpecialSymbolsListEnabled(),
                    settingsBeforeChange.getSpecialSymbolsListEnabled());
        setProperty(tableOfContentEnabledProperty,
                    settingsAfterChange.getTableOfContentEnabled(),
                    settingsBeforeChange.getTableOfContentEnabled());
        setProperty(transcriptionInfoEnabledProperty,
                    settingsAfterChange.getTranscriptionInfoEnabled(),
                    settingsBeforeChange.getTranscriptionInfoEnabled());
        setProperty(volumeInfoEnabledProperty,
                    settingsAfterChange.getVolumeInfoEnabled(),
                    settingsBeforeChange.getVolumeInfoEnabled());
        setProperty(transcriptionInfoStyleProperty,
                    settingsAfterChange.getTranscriptionInfoStyle().getName(),
                    settingsBeforeChange.getTranscriptionInfoStyle().getName());
        setProperty(volumeInfoStyleProperty,
                    settingsAfterChange.getVolumeInfoStyle().getName(),
                    settingsBeforeChange.getVolumeInfoStyle().getName());
        setProperty(preliminaryVolumeEnabledProperty,
                    settingsAfterChange.getPreliminaryVolumeEnabled(),
                    settingsBeforeChange.getPreliminaryVolumeEnabled());
        setProperty(mathProperty,
                    settingsAfterChange.getMath().name(),
                    settingsBeforeChange.getMath().name());
        setProperty(hardPageBreaksProperty,
                    settingsAfterChange.getHardPageBreaks(),
                    settingsBeforeChange.getHardPageBreaks());


        setProperty(brailleRulesProperty,
                    settingsAfterChange.getBrailleRules().name(),
                    settingsBeforeChange.getBrailleRules().name());

        if (odtModified) {
            xModifiable.setModified(true);
        }

        logger.exiting("SettingsIO", "saveBrailleSettingsToDocument");

    }

    public void saveExportSettingsToDocument (Settings settingsAfterChange,
                                              Settings settingsBeforeChange)
                                       throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "saveExportToDocument");

        odtModified = false;

        setProperty(exportFileProperty,
                    settingsAfterChange.getBrailleFileType().name(),
                    settingsBeforeChange.getBrailleFileType().name());
        setProperty(exportTableProperty,
                    settingsAfterChange.getTable().name(),
                    settingsBeforeChange.getTable().name());
        setProperty(exportDuplexProperty,
                    settingsAfterChange.getDuplex(),
                    settingsBeforeChange.getDuplex());
        setProperty(exportEightDotsProperty,
                    settingsAfterChange.getEightDots(),
                    settingsBeforeChange.getEightDots());
        setProperty(exportNumberOfCellsPerLineProperty,
                    settingsAfterChange.getCellsPerLine(),
                    settingsBeforeChange.getCellsPerLine());
        setProperty(exportNumberOfLinesPerPageProperty,
                    settingsAfterChange.getLinesPerPage(),
                    settingsBeforeChange.getLinesPerPage());
        setProperty(exportMultipleFilesProperty,
                    settingsAfterChange.getMultipleFiles(),
                    settingsBeforeChange.getMultipleFiles());

        if (odtModified) {
            xModifiable.setModified(true);
        }

        logger.exiting("SettingsIO", "saveExportToDocument");

    }

    public void saveEmbossSettingsToDocument (Settings settingsAfterChange,
                                              Settings settingsBeforeChange)
                                       throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "saveEmbossSettingsToDocument");

        odtModified = false;

        setProperty(embossNumberOfCellsPerLineProperty,
                    settingsAfterChange.getCellsPerLine(),
                    settingsBeforeChange.getCellsPerLine());
        setProperty(embossNumberOfLinesPerPageProperty,
                    settingsAfterChange.getLinesPerPage(),
                    settingsBeforeChange.getLinesPerPage());
        setProperty(marginLeftProperty,
                    settingsAfterChange.getMarginInner(),
                    settingsBeforeChange.getMarginInner());
        setProperty(marginTopProperty,
                    settingsAfterChange.getMarginTop(),
                    settingsBeforeChange.getMarginTop());

        if (odtModified) {
            xModifiable.setModified(true);
        }

        logger.exiting("SettingsIO", "saveEmbossSettingsToDocument");

    }

    public void saveEmbossSettingsToOpenOffice (Settings settingsAfterChange,
                                                Settings settingsBeforeChange)
                                         throws IOException,
                                                com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "saveEmbossSettingsToOpenOffice");

        Properties embosserSettings = new Properties();

        odtModified = false;

        setProperty(embosserSettings,
                    embosserProperty,
                    settingsAfterChange.getEmbosser().name(),
                    settingsBeforeChange.getEmbosser().name());
        setProperty(embosserSettings,
                    saddleStitchProperty,
                    settingsAfterChange.getSaddleStitch(),
                    settingsBeforeChange.getSaddleStitch());
        setProperty(embosserSettings,
                    sheetsPerQuireProperty,
                    settingsAfterChange.getSheetsPerQuire(),
                    settingsBeforeChange.getSheetsPerQuire());
        setProperty(embosserSettings,
                    zFoldingProperty,
                    settingsAfterChange.getZFolding(),
                    settingsBeforeChange.getZFolding());
        setProperty(embosserSettings,
                    embossTableProperty,
                    settingsAfterChange.getTable().name(),
                    settingsBeforeChange.getTable().name());
        setProperty(embosserSettings,
                    paperSizeProperty,
                    settingsAfterChange.getPaperSize().name(),
                    settingsBeforeChange.getPaperSize().name());
        setProperty(embosserSettings,
                    embossDuplexProperty,
                    settingsAfterChange.getDuplex(),
                    settingsBeforeChange.getDuplex());
        setProperty(embosserSettings,
                    embossEightDotsProperty,
                    settingsAfterChange.getEightDots(),
                    settingsBeforeChange.getEightDots());
        
        if (settingsAfterChange.getPaperSize()==PaperSize.CUSTOM) {
            setProperty(embosserSettings,
                        customPaperWidthProperty,
                        settingsAfterChange.getPaperWidth(),
                        settingsBeforeChange.getPaperWidth());
            setProperty(embosserSettings,
                        customPaperHeightProperty,
                        settingsAfterChange.getPaperHeight(),
                        settingsBeforeChange.getPaperHeight());
        }

        if (odtModified) {
            saveSettingsToOpenOffice("embosser", embosserSettings);
            xModifiable.setModified(true);
        }

        logger.exiting("SettingsIO", "saveEmbossSettingsToOpenOffice");
    }

    /**
     * Load settings from OpenOffice.org.
     * The settings are stored in a .properties file in the OpenOffice.org extension (.oxt).
     *
     * @param   name            The name of the .properties file
     * @return                  The loaded settings
     */
    public Properties loadSettingsFromOpenOffice(String name)
                                          throws IOException{

        logger.entering("SettingsIO", "loadSettingsFromOpenOffice");

        Properties settings = new Properties();

        FileInputStream inputStream = new FileInputStream(
                new File(UnoUtils.UnoURLtoURL(packageLocation + "/settings/" + name + ".properties", xContext)));
        settings.load(inputStream);

        if (inputStream != null) {
            inputStream.close();
        }

        logger.exiting("SettingsIO", "loadSettingsFromOpenOffice");

        return settings;

    }

    /**
     * Save settings to OpenOffice.org.
     * The settings are stored in a .properties file in the OpenOffice.org extension (.oxt).
     *
     * @param   name             The name of the .properties file
     * @param   settings         The settings
     */
    public void saveSettingsToOpenOffice(String name,
                                         Properties settings)
                                  throws IOException {

        logger.entering("SettingsIO", "saveSettingsToOpenOffice");

        FileOutputStream outputStream = new FileOutputStream(
                new File(UnoUtils.UnoURLtoURL(packageLocation + "/settings/" + name + ".properties", xContext)));

        if (settings != null) {
            settings.store(outputStream, null);
        }

        if (outputStream != null) {
            outputStream.close();
        }

        logger.exiting("SettingsIO", "saveSettingsToOpenOffice");

    }
}