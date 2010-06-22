/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
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
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XTextDocument;

import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.Settings.BrailleFileType;
import be.docarch.odt2braille.Settings.MathType;
import be.docarch.odt2braille.Settings.BrailleRules;
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
    private static String printPageNumbersEnabledProperty =      "[BRL]PrintPageNumbers";
    private static String braillePageNumbersEnabledProperty =    "[BRL]BraillePageNumbers";
    private static String preliminaryVolumeEnabledProperty =     "[BRL]PreliminaryVolume";
    private static String genericOrSpecificProperty =            "[BRL]ExportAsGeneric";
    private static String genericBrailleProperty =               "[BRL]GenericFileType";
    private static String embosserProperty =                     "[BRL]Embosser";
    private static String tableProperty =                        "[BRL]CharacterSet";
    private static String paperSizeProperty =                    "[BRL]PaperSize";
    private static String duplexProperty =                       "[BRL]RectoVerso";
    private static String mirrorAlignProperty =                  "[BRL]MirrorAlign";
    private static String numberOfCellsPerLineProperty =         "[BRL]CellsPerLine";
    private static String numberOfLinesPerPageProperty =         "[BRL]LinesPerPage";
    private static String marginLeftProperty =                   "[BRL]MarginLeft";
    private static String marginTopProperty =                    "[BRL]MarginTop";
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

    /**
     * Creates a new <code>SettingsIO</code> instance.
     *
     * @param   xContext
     */
    public SettingsIO(XComponentContext xContext) 
               throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "<init>");

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        packageLocation = xPkgInfo.getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn");
        this.xContext = xContext;

        XMultiComponentFactory xMCF =(XMultiComponentFactory) UnoRuntime.queryInterface(XMultiComponentFactory.class,xContext.getServiceManager());
        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
        XComponent xDesktopComponent = (XComponent) xDesktop.getCurrentComponent();
        XTextDocument xTextDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class,xDesktopComponent);
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
     * @param   defaultSettings     The default settings.
     * @return                      The settings loaded from the document.
     *                              If a setting cannot be loaded, the default setting is chosen.
     */
    public Settings loadSettingsFromDocument (Settings defaultSettings)
                                       throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "loadSettingsFromDocument");

        Settings loadedSettings = new Settings(defaultSettings);

        String s;
        Double d;
        Boolean b;

        ArrayList<String> languages = loadedSettings.getLanguages();
        for (int i=0;i<languages.size();i++) {
            if ((s = getStringProperty(languageProperty + "_" + languages.get(i))) != null) {
                loadedSettings.setTranslationTable(s, languages.get(i));
            }
            if (!(d = getDoubleProperty(gradeProperty + "_" + languages.get(i))).isNaN()) {
                loadedSettings.setGrade(d.intValue(), languages.get(i));
            }
        }

        String elements[] = {"Paragraph",
                             "Heading1", "Heading2", "Heading3", "Heading4",
                             "Toc", "Toc1", "Toc2", "Toc3", "Toc4",
                             "List", "List1", "List2", "List3", "List4", "List5", "List6", "List7", "List8", "List9", "List10",
                             "Table", "Table1", "Table2", "Table3", "Table4", "Table5", "Table6", "Table7", "Table8", "Table9", "Table10" };
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

        if ((b = getBooleanProperty(genericOrSpecificProperty)) != null) {
            loadedSettings.setGenericOrSpecific(b);
        }

        if ((s = getStringProperty(genericBrailleProperty)) != null) {
            try {
                loadedSettings.setGenericBraille(BrailleFileType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid generic braille type");
            }
        }

        if ((s = getStringProperty(embosserProperty)) != null) {
            try {
                loadedSettings.setEmbosser(EmbosserType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid embossertype");
            }
        }

        if ((s = getStringProperty(tableProperty)) != null) {
            try {
                loadedSettings.setTable(TableType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid tabletype");
            }
        }

        if ((s = getStringProperty(paperSizeProperty)) != null) {
            try {
                loadedSettings.setPaperSize(PaperSize.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid papersize");
            }
        }

        if ((s = getStringProperty(mathProperty)) != null) {
            try {
                loadedSettings.setMath(MathType.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid math type");
            }
        }

        if ((b = getBooleanProperty(duplexProperty)) != null) {
            loadedSettings.setDuplex(b);
        }

        if ((b = getBooleanProperty(mirrorAlignProperty)) != null) {
            loadedSettings.setMirrorAlign(b);
        }

        if ((s = getStringProperty(creatorProperty)) != null) {
            loadedSettings.creator = s;
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

        if (!(d = getDoubleProperty(numberOfCellsPerLineProperty)).isNaN()) {
            loadedSettings.setCellsPerLine(d.intValue());
        }

        if (!(d = getDoubleProperty(numberOfLinesPerPageProperty)).isNaN()) {
            loadedSettings.setLinesPerPage(d.intValue());
        }

        if (!(d = getDoubleProperty(marginLeftProperty)).isNaN()) {
            loadedSettings.setMarginLeft(d.intValue());
        }

        if (!(d = getDoubleProperty(marginTopProperty)).isNaN()) {
            loadedSettings.setMarginTop(d.intValue());
        }

        if ((b = getBooleanProperty(printPageNumbersEnabledProperty)) != null) {
            loadedSettings.printPageNumbersEnabled = b;
        }

        if ((b = getBooleanProperty(braillePageNumbersEnabledProperty)) != null) {
            loadedSettings.braillePageNumbersEnabled = b;
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

        logger.exiting("SettingsIO", "loadSettingsFromDocument");

        return loadedSettings;

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
    public void saveSettingsToDocument (Settings settingsAfterChange,
                                        Settings settingsBeforeChange)
                                 throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "saveSettingsToDocument");

        odtModified = false;

        ArrayList<String> languages = settingsAfterChange.getLanguages();
        for (int i=0;i<languages.size();i++) {
            setProperty(languageProperty + "_" + languages.get(i),
                        settingsAfterChange.getTranslationTable(languages.get(i)),
                        settingsBeforeChange.getTranslationTable(languages.get(i)));
            setProperty(gradeProperty + "_" + languages.get(i),
                        settingsAfterChange.getGrade(languages.get(i)),
                        settingsBeforeChange.getGrade(languages.get(i)));
        }

        String elements[] = {"Paragraph",
                             "Heading1", "Heading2", "Heading3", "Heading4",
                             "Toc", "Toc1", "Toc2", "Toc3", "Toc4",
                             "List", "List1", "List2", "List3", "List4", "List5", "List6", "List7", "List8", "List9", "List10",
                             "Table", "Table1", "Table2", "Table3", "Table4", "Table5", "Table6", "Table7", "Table8", "Table9", "Table10" };
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
        
        setProperty(creatorProperty,
                    settingsAfterChange.creator,
                    settingsBeforeChange.creator);
        setProperty(transcribersNotesPageTitleProperty,
                    settingsAfterChange.transcribersNotesPageTitle,
                    settingsBeforeChange.transcribersNotesPageTitle);
        setProperty(specialSymbolsListTitleProperty,
                    settingsAfterChange.specialSymbolsListTitle,
                    settingsBeforeChange.specialSymbolsListTitle);
        setProperty(tableOfContentTitleProperty,
                    settingsAfterChange.tableOfContentTitle,
                    settingsBeforeChange.tableOfContentTitle);
        setProperty(printPageNumbersEnabledProperty,
                    settingsAfterChange.printPageNumbersEnabled,
                    settingsBeforeChange.printPageNumbersEnabled);
        setProperty(braillePageNumbersEnabledProperty,
                    settingsAfterChange.braillePageNumbersEnabled,
                    settingsBeforeChange.braillePageNumbersEnabled);
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
        setProperty(genericOrSpecificProperty,
                    settingsAfterChange.isGenericOrSpecific(),
                    settingsBeforeChange.isGenericOrSpecific());
        setProperty(embosserProperty,
                    settingsAfterChange.getEmbosser().name(),
                    settingsBeforeChange.getEmbosser().name());
        setProperty(genericBrailleProperty,
                    settingsAfterChange.getGenericBraille().name(),
                    settingsBeforeChange.getGenericBraille().name());
        setProperty(tableProperty,
                    settingsAfterChange.getTable().name(),
                    settingsBeforeChange.getTable().name());
        setProperty(paperSizeProperty,
                    settingsAfterChange.getPaperSize().name(),
                    settingsBeforeChange.getPaperSize().name());
        setProperty(duplexProperty,
                    settingsAfterChange.isDuplex(),
                    settingsBeforeChange.isDuplex());
        setProperty(mirrorAlignProperty,
                    settingsAfterChange.isMirrorAlign(),
                    settingsBeforeChange.isMirrorAlign());
        setProperty(numberOfCellsPerLineProperty,
                    settingsAfterChange.getNumberOfCellsPerLine(),
                    settingsBeforeChange.getNumberOfCellsPerLine());
        setProperty(numberOfLinesPerPageProperty,
                    settingsAfterChange.getNumberOfLinesPerPage(),
                    settingsBeforeChange.getNumberOfLinesPerPage());
        setProperty(marginLeftProperty,
                    settingsAfterChange.getMarginLeft(),
                    settingsBeforeChange.getMarginLeft());
        setProperty(marginTopProperty,
                    settingsAfterChange.getMarginTop(),
                    settingsBeforeChange.getMarginTop());
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
        setProperty(brailleRulesProperty,
                    settingsAfterChange.getBrailleRules().name(),
                    settingsBeforeChange.getBrailleRules().name());

        if (odtModified) {
            xModifiable.setModified(true);
        }

        logger.exiting("SettingsIO", "saveSettingsToDocument");

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
