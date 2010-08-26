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
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille.addon;

import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.FileWriter;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.logging.Level;

import com.sun.star.uno.XComponentContext;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertyContainer;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.beans.XPropertySet;
import com.sun.star.util.XModifiable;
import com.sun.star.uno.AnyConverter;
import com.sun.star.document.XDocumentProperties;
import com.sun.star.document.XDocumentPropertiesSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextDocument;

import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.BrailleFileExporter.BrailleFileType;
import be.docarch.odt2braille.Settings.MathType;
import be.docarch.odt2braille.Settings.BrailleRules;
import be.docarch.odt2braille.SpecialSymbol;
import be.docarch.odt2braille.SpecialSymbol.SpecialSymbolMode;
import be.docarch.odt2braille.SpecialSymbol.SpecialSymbolType;
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

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille.addon");

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
    private static String transcriptionInfoEnabledProperty =     "[BRL]TranscriptionInfo";
    private static String creatorProperty =                      "[BRL]Creator";
    private static String volumeInfoEnabledProperty =            "[BRL]VolumeInfo";
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
    private static String centeredProperty =                     "[BRL]Centered";
    private static String firstLineProperty =                    "[BRL]FirstLineIndent";
    private static String runoversProperty =                     "[BRL]Runovers";
    private static String linesAboveProperty =                   "[BRL]LinesAbove";
    private static String linesBelowProperty =                   "[BRL]LinesBelow";
    private static String linesBetweenProperty =                 "[BRL]LinesBetween";
    private static String listPrefixProperty =                   "[BRL]ListPrefix";
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

    // Emboss settings

    private static String embosserProperty =                     "[BRL]Embosser";
    private static String paperSizeProperty =                    "[BRL]PaperSize";
    private static String customPaperWidthProperty =             "[BRL]CustomPaperWidth";
    private static String customPaperHeightProperty =            "[BRL]CustomPaperHeight";
    private static String mirrorAlignProperty =                  "[BRL]MirrorAlign";
    private static String marginLeftProperty =                   "[BRL]MarginLeft";
    private static String marginTopProperty =                    "[BRL]MarginTop";
    private static String embossTableProperty =                  "[BRL]EmbossCharacterSet";
    private static String embossNumberOfCellsPerLineProperty =   "[BRL]EmbossCellsPerLine";
    private static String embossNumberOfLinesPerPageProperty =   "[BRL]EmbossLinesPerPage";
    private static String embossDuplexProperty =                 "[BRL]EmbossRectoVerso";


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
        packageLocation = xPkgInfo.getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn");
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
                               throws com.sun.star.uno.Exception {

        String s;

        if (xPropSetInfo.hasPropertyByName(property)) {
            s = AnyConverter.toString(xPropSet.getPropertyValue(property));
            if (!s.isEmpty()) {
                return s;
            } else {
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
                              throws com.sun.star.uno.Exception {

        if (xPropSetInfo.hasPropertyByName(property)) {
            return AnyConverter.toDouble(xPropSet.getPropertyValue(property));
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
                                throws com.sun.star.uno.Exception {

        if (xPropSetInfo.hasPropertyByName(property)) {
            return AnyConverter.toBoolean(xPropSet.getPropertyValue(property));
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
        String elements[] = {"Paragraph",
                             "Heading1", "Heading2", "Heading3", "Heading4",
                             "Toc", "Toc1", "Toc2", "Toc3", "Toc4",
                             "List", "List1", "List2", "List3", "List4", "List5", "List6", "List7", "List8", "List9", "List10",
                             "Table", "Table1", "Table2", "Table3", "Table4", "Table5", "Table6", "Table7", "Table8", "Table9", "Table10" };

        for (int i=0;i<languages.size();i++) {
            if ((s = getStringProperty(languageProperty + "_" + languages.get(i))) != null) {
                loadedSettings.setTranslationTable(s, languages.get(i));
            }
            if (!(d = getDoubleProperty(gradeProperty + "_" + languages.get(i))).isNaN()) {
                loadedSettings.setGrade(d.intValue(), languages.get(i));
            }
        }
        
        for (int i=0;i<elements.length;i++) {
            if ((b = getBooleanProperty(centeredProperty + elements[i])) != null) {
                loadedSettings.setCentered(elements[i].toLowerCase(), b);
            }
            if (!(d = getDoubleProperty(firstLineProperty + elements[i])).isNaN()) {
                loadedSettings.setFirstLineMargin(elements[i].toLowerCase(), d.intValue());
            }
            if (!(d = getDoubleProperty(runoversProperty + elements[i])).isNaN()) {
                loadedSettings.setRunoversMargin(elements[i].toLowerCase(), d.intValue());
            }
            if (!(d = getDoubleProperty(linesAboveProperty + elements[i])).isNaN()) {
                loadedSettings.setLinesAbove(elements[i].toLowerCase(), d.intValue());
            }
            if (!(d = getDoubleProperty(linesBelowProperty + elements[i])).isNaN()) {
                loadedSettings.setLinesBelow(elements[i].toLowerCase(), d.intValue());
            }
            if (!(d = getDoubleProperty(linesBetweenProperty + elements[i])).isNaN()) {
                loadedSettings.setLinesBetween(elements[i].toLowerCase(), d.intValue());
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
            loadedSettings.transcribersNotesPageTitle = s;
        }

        if ((s = getStringProperty(specialSymbolsListTitleProperty)) != null) {
            loadedSettings.specialSymbolsListTitle = s;
        }

        if ((s = getStringProperty(tableOfContentTitleProperty)) != null) {
            loadedSettings.tableOfContentTitle = s;
        }
        
        if ((s = getStringProperty(columnDelimiterProperty)) != null) {
            loadedSettings.setColumnDelimiter(s);
        }

        if ((s = getStringProperty(lineFillSymbolProperty)) != null) {
            loadedSettings.setLineFillSymbol(s);
        }

        for (int i=0;i<10;i++) {
            if ((s = getStringProperty(listPrefixProperty + "_" + Integer.toString(i+1))) != null) {
                loadedSettings.setListPrefix(s,i+1);
            }
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

        if ((b = getBooleanProperty(pageNumberAtTopOnSepLineProperty)) != null) {
            loadedSettings.setPageNumberAtTopOnSeparateLine(b);
        }

        if ((b = getBooleanProperty(pageNumberAtBottomOnSepLineProperty)) != null) {
            loadedSettings.setPageNumberAtBottomOnSeparateLine(b);
        }

        if ((b = getBooleanProperty(printPageNumberRangeProperty)) != null) {
            loadedSettings.setPrintPageNumberRange(b);
        }

        if ((s = getStringProperty(printPageNumberAtProperty)) != null) {
            loadedSettings.setPrintPageNumberAt(s);
        }

        if ((s = getStringProperty(braillePageNumberAtProperty)) != null) {
            loadedSettings.setBraillePageNumberAt(s);
        }

        if ((s = getStringProperty(preliminaryPageNumberFormatProperty)) != null) {
            loadedSettings.setPreliminaryPageFormat(s);
        }

        if ((b = getBooleanProperty(transcribersNotesPageEnabledProperty)) != null) {
            loadedSettings.transcribersNotesPageEnabled = loadedSettings.PRELIMINARY_PAGES_PRESENT && b;
        }

        if ((b = getBooleanProperty(specialSymbolsListEnabledProperty)) != null) {
            loadedSettings.specialSymbolsListEnabled = loadedSettings.PRELIMINARY_PAGES_PRESENT && b;
        }

        if ((b = getBooleanProperty(tableOfContentEnabledProperty)) != null) {
            loadedSettings.tableOfContentEnabled = loadedSettings.PRELIMINARY_PAGES_PRESENT && b;
        }

        if ((b = getBooleanProperty(transcriptionInfoEnabledProperty)) != null) {
            loadedSettings.transcriptionInfoEnabled = loadedSettings.TRANSCRIPTION_INFO_AVAILABLE && b;
        }

        if ((b = getBooleanProperty(volumeInfoEnabledProperty)) != null) {
            loadedSettings.volumeInfoEnabled = loadedSettings.VOLUME_INFO_AVAILABLE && b;
        }

        if ((b = getBooleanProperty(preliminaryVolumeEnabledProperty)) != null) {
            loadedSettings.preliminaryVolumeEnabled = loadedSettings.PRELIMINARY_PAGES_PRESENT && b;
        }

        if ((b = getBooleanProperty(stairstepTableProperty)) != null) {
            loadedSettings.setStairstepTable(b);
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
            loadedSettings.setDuplex(b);
        }

        if (!(d = getDoubleProperty(exportNumberOfCellsPerLineProperty)).isNaN()) {
            loadedSettings.setCellsPerLine(d.intValue());
        }

        if (!(d = getDoubleProperty(exportNumberOfLinesPerPageProperty)).isNaN()) {
            loadedSettings.setLinesPerPage(d.intValue());
        }

        logger.exiting("SettingsIO", "loadExportSettingsFromDocument");

    }

    public void loadEmbossSettingsFromDocument (Settings loadedSettings)
                                         throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "loadEmbossSettingsFromDocument");

        String s;
        Boolean b;
        Double d;
        Double d2;

        if ((s = getStringProperty(embosserProperty)) != null) {
            try {
                loadedSettings.setEmbosser(EmbosserType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid embossertype");
            }
        }

        if ((s = getStringProperty(embossTableProperty)) != null) {
            try {
                loadedSettings.setTable(TableType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid tabletype");
            }
        }

        if ((s = getStringProperty(paperSizeProperty)) != null) {
            try {
                loadedSettings.setPaperSize(PaperSize.valueOf(s));
                if (s.equals("CUSTOM")) {
                    if (!(d =  getDoubleProperty(customPaperWidthProperty)).isNaN() &&
                        !(d2 = getDoubleProperty(customPaperHeightProperty)).isNaN()) {
                        loadedSettings.setPaperSize(d, d2);
                    }
                }
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid papersize");
            }
        }

        if ((b = getBooleanProperty(embossDuplexProperty)) != null) {
            loadedSettings.setDuplex(b);
        }

        if ((b = getBooleanProperty(mirrorAlignProperty)) != null) {
            loadedSettings.setMirrorAlign(b);
        }

        if (!(d = getDoubleProperty(embossNumberOfCellsPerLineProperty)).isNaN()) {
            loadedSettings.setCellsPerLine(d.intValue());
        }

        if (!(d = getDoubleProperty(embossNumberOfLinesPerPageProperty)).isNaN()) {
            loadedSettings.setLinesPerPage(d.intValue());
        }

        if (!(d = getDoubleProperty(marginLeftProperty)).isNaN()) {
            loadedSettings.setMarginLeft(d.intValue());
        }

        if (!(d = getDoubleProperty(marginTopProperty)).isNaN()) {
            loadedSettings.setMarginTop(d.intValue());
        }

        logger.exiting("SettingsIO", "loadEmbossSettingsFromDocument");

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

        logger.entering("SettingsIO", "saveSettingsToDocument");

        odtModified = false;

        ArrayList<String> languages = settingsAfterChange.getLanguages();
        ArrayList<SpecialSymbol> specialSymbolsAfterChange = settingsAfterChange.getSpecialSymbolsList();
        ArrayList<SpecialSymbol> specialSymbolsBeforeChange = settingsBeforeChange.getSpecialSymbolsList();
        String elements[] = {"Paragraph",
                             "Heading1", "Heading2", "Heading3", "Heading4",
                             "Toc", "Toc1", "Toc2", "Toc3", "Toc4",
                             "List", "List1", "List2", "List3", "List4", "List5", "List6", "List7", "List8", "List9", "List10",
                             "Table", "Table1", "Table2", "Table3", "Table4", "Table5", "Table6", "Table7", "Table8", "Table9", "Table10" };

        for (int i=0;i<languages.size();i++) {
            setProperty(languageProperty + "_" + languages.get(i),
                        settingsAfterChange.getTranslationTable(languages.get(i)),
                        settingsBeforeChange.getTranslationTable(languages.get(i)));
            setProperty(gradeProperty + "_" + languages.get(i),
                        settingsAfterChange.getGrade(languages.get(i)),
                        settingsBeforeChange.getGrade(languages.get(i)));
        }
        
        for (int i=0;i<elements.length;i++) {
            setProperty(centeredProperty + elements[i],
                        settingsAfterChange.getCentered(elements[i].toLowerCase()),
                        settingsBeforeChange.getCentered(elements[i].toLowerCase()));
            setProperty(firstLineProperty + elements[i],
                        settingsAfterChange.getFirstLineMargin(elements[i].toLowerCase()),
                        settingsBeforeChange.getFirstLineMargin(elements[i].toLowerCase()));
            setProperty(runoversProperty + elements[i],
                        settingsAfterChange.getRunoversMargin(elements[i].toLowerCase()),
                        settingsBeforeChange.getRunoversMargin(elements[i].toLowerCase()));
            setProperty(linesAboveProperty + elements[i],
                        settingsAfterChange.getLinesAbove(elements[i].toLowerCase()),
                        settingsBeforeChange.getLinesAbove(elements[i].toLowerCase()));
            setProperty(linesBelowProperty + elements[i],
                        settingsAfterChange.getLinesBelow(elements[i].toLowerCase()),
                        settingsBeforeChange.getLinesBelow(elements[i].toLowerCase()));
            setProperty(linesBetweenProperty + elements[i],
                        settingsAfterChange.getLinesBetween(elements[i].toLowerCase()),
                        settingsBeforeChange.getLinesBetween(elements[i].toLowerCase()));
        }
        
        for (int i=0;i<10;i++) {
            setProperty(listPrefixProperty + "_" + Integer.toString(i+1),
                        settingsAfterChange.getListPrefix(i+1),
                        settingsBeforeChange.getListPrefix(i+1));
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
                    settingsAfterChange.transcribersNotesPageTitle,
                    settingsBeforeChange.transcribersNotesPageTitle);
        setProperty(specialSymbolsListTitleProperty,
                    settingsAfterChange.specialSymbolsListTitle,
                    settingsBeforeChange.specialSymbolsListTitle);
        setProperty(tableOfContentTitleProperty,
                    settingsAfterChange.tableOfContentTitle,
                    settingsBeforeChange.tableOfContentTitle);
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
                    settingsAfterChange.getPrintPageNumberAt(),
                    settingsBeforeChange.getPrintPageNumberAt());
        setProperty(braillePageNumberAtProperty,
                    settingsAfterChange.getBraillePageNumberAt(),
                    settingsBeforeChange.getBraillePageNumberAt());
        setProperty(preliminaryPageNumberFormatProperty,
                    settingsAfterChange.getPreliminaryPageFormat(),
                    settingsBeforeChange.getPreliminaryPageFormat());
        setProperty(transcribersNotesPageEnabledProperty,
                    settingsAfterChange.transcribersNotesPageEnabled,
                    settingsBeforeChange.transcribersNotesPageEnabled);
        setProperty(specialSymbolsListEnabledProperty,
                    settingsAfterChange.specialSymbolsListEnabled,
                    settingsBeforeChange.specialSymbolsListEnabled);
        setProperty(tableOfContentEnabledProperty,
                    settingsAfterChange.tableOfContentEnabled,
                    settingsBeforeChange.tableOfContentEnabled);
        setProperty(transcriptionInfoEnabledProperty,
                    settingsAfterChange.transcriptionInfoEnabled,
                    settingsBeforeChange.transcriptionInfoEnabled);
        setProperty(volumeInfoEnabledProperty,
                    settingsAfterChange.volumeInfoEnabled,
                    settingsBeforeChange.volumeInfoEnabled);
        setProperty(preliminaryVolumeEnabledProperty,
                    settingsAfterChange.preliminaryVolumeEnabled,
                    settingsBeforeChange.preliminaryVolumeEnabled);
        setProperty(stairstepTableProperty,
                    settingsAfterChange.stairstepTableIsEnabled(),
                    settingsBeforeChange.stairstepTableIsEnabled());
        setProperty(columnDelimiterProperty,
                    settingsAfterChange.getColumnDelimiter(),
                    settingsBeforeChange.getColumnDelimiter());
        setProperty(lineFillSymbolProperty,
                    settingsAfterChange.getLineFillSymbol(),
                    settingsBeforeChange.getLineFillSymbol());
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
        setProperty(exportNumberOfCellsPerLineProperty,
                    settingsAfterChange.getCellsPerLine(),
                    settingsBeforeChange.getCellsPerLine());
        setProperty(exportNumberOfLinesPerPageProperty,
                    settingsAfterChange.getLinesPerPage(),
                    settingsBeforeChange.getLinesPerPage());

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

        setProperty(embosserProperty,
                    settingsAfterChange.getEmbosser().name(),
                    settingsBeforeChange.getEmbosser().name());
        setProperty(embossTableProperty,
                    settingsAfterChange.getTable().name(),
                    settingsBeforeChange.getTable().name());
        setProperty(paperSizeProperty,
                    settingsAfterChange.getPaperSize().name(),
                    settingsBeforeChange.getPaperSize().name());
        setProperty(customPaperWidthProperty,
                    settingsAfterChange.getPaperWidth(),
                    settingsBeforeChange.getPaperWidth());
        setProperty(customPaperHeightProperty,
                    settingsAfterChange.getPaperHeight(),
                    settingsBeforeChange.getPaperHeight());
        setProperty(embossDuplexProperty,
                    settingsAfterChange.getDuplex(),
                    settingsBeforeChange.getDuplex());
        setProperty(mirrorAlignProperty,
                    settingsAfterChange.getMirrorAlign(),
                    settingsBeforeChange.getMirrorAlign());
        setProperty(embossNumberOfCellsPerLineProperty,
                    settingsAfterChange.getCellsPerLine(),
                    settingsBeforeChange.getCellsPerLine());
        setProperty(embossNumberOfLinesPerPageProperty,
                    settingsAfterChange.getLinesPerPage(),
                    settingsBeforeChange.getLinesPerPage());
        setProperty(marginLeftProperty,
                    settingsAfterChange.getMarginLeft(),
                    settingsBeforeChange.getMarginLeft());
        setProperty(marginTopProperty,
                    settingsAfterChange.getMarginTop(),
                    settingsBeforeChange.getMarginTop());

        if (odtModified) {
            xModifiable.setModified(true);
        }

        logger.exiting("SettingsIO", "saveEmbossSettingsToDocument");

    }


    /**
     * Load settings from OpenOffice.org.
     * The settings are stored in a .settings file in the OpenOffice.org extension (.oxt).
     *
     * @param   fileName        The name of the .settings file
     * @param   settingNames    An array of setting names.
     * @return                  An array of the respective setting values or <code>null</code> if the setting is not found.
     *                          This array has the same lenght as <code>settingNames</code>
     */
    public String[] loadSettingsFromOpenOffice(String fileName,
                                               String[] settingNames)
                                        throws IOException{

        logger.entering("SettingsIO", "loadSettingsFromOpenOffice");

        String[] settingValues = new String[settingNames.length];
        String line = null;
        File settingsFile = new File(UnoUtils.UnoURLtoURL(packageLocation+ "/settings/" + fileName, xContext));

        FileInputStream fileInputStream = new FileInputStream(settingsFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        while((line=bufferedReader.readLine())!=null) {

            for (int i=0;i<settingNames.length;i++) {

                if (line.startsWith(settingNames[i]+"=")) {

                    settingValues[i] = line.substring(line.indexOf("=")+1);
                    break;

                }
            }
        }

        if (bufferedReader != null) {
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        }

        logger.exiting("SettingsIO", "loadSettingsFromOpenOffice");

        return settingValues;

    }

    /**
     * Save settings to OpenOffice.org.
     * The settings are stored in a .settings file in the OpenOffice.org extension (.oxt).
     *
     * @param   fileName        The name of the .settings file
     * @param   settingNames    An array of setting names.
     * @param   settingValues   An array of the respective setting values.
     *                          This array must have the same lenght as <code>settingNames</code>
     */
    public void saveSettingsToOpenOffice(String fileName,
                                         String[] settingNames,
                                         String[] settingValues)
                                  throws IOException {

        logger.entering("SettingsIO", "saveSettingsToOpenOffice");

        String line = null;
        String content = "";
        boolean concat = true;

        File settingsFile = new File(UnoUtils.UnoURLtoURL(packageLocation+ "/settings/" + fileName, xContext));
        FileInputStream fileInputStream = new FileInputStream(settingsFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        while((line=bufferedReader.readLine())!=null) {

            concat = true;

            for (int i=0;i<settingNames.length;i++) {
                if (line.startsWith(settingNames[i]+"=")) {
                    concat = false;
                    break;
                }
            }

            if (concat) {
                content += line + System.getProperty("line.separator");
            }
        }

        for (int i=0;i<settingNames.length;i++) {

            line = settingNames[i] + "=" + settingValues[i];
            content += line + System.getProperty("line.separator");

        }

        if (bufferedReader != null) {
            bufferedReader.close();
            inputStreamReader.close();
            fileInputStream.close();
        }

        Writer bufferedWriter = new BufferedWriter(new FileWriter(settingsFile));
        bufferedWriter.write(content);

        if (bufferedWriter != null) {
            bufferedWriter.close();
        }

        logger.exiting("SettingsIO", "saveSettingsToOpenOffice");

    }
}