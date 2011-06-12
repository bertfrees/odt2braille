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
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.SettingsLoader;
import be.docarch.odt2braille.Settings.MathType;
import be.docarch.odt2braille.Settings.BrailleRules;
import be.docarch.odt2braille.Settings.PageNumberFormat;
import be.docarch.odt2braille.Settings.PageNumberPosition;
import be.docarch.odt2braille.Settings.VolumeManagementMode;
import be.docarch.odt2braille.SpecialSymbol;
import be.docarch.odt2braille.Style;
import be.docarch.odt2braille.Style.Alignment;
import be.docarch.odt2braille.ParagraphStyle;
import be.docarch.odt2braille.HeadingStyle;
import be.docarch.odt2braille.ListStyle;
import be.docarch.odt2braille.TableStyle;
import be.docarch.odt2braille.TocStyle;
import be.docarch.odt2braille.CharacterStyle;
import be.docarch.odt2braille.CharacterStyle.TypefaceOption;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.table.Table;
import org.daisy.paper.Paper;

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
public class SettingsIO extends SettingsLoader {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private XComponentContext xContext = null;
    private String packageLocation = null;

    private boolean odtModified = false;

    private XPropertyContainer xPropCont = null;
    private XPropertySet xPropSet = null;
    private XPropertySetInfo xPropSetInfo = null;
    private XModifiable xModifiable = null;

    private final static short OPTIONAL = (short) 256;


    /**
     * Creates a new <code>SettingsLoader</code> instance.
     *
     * @param   xContext
     */
    public SettingsIO(XComponentContext xContext,
                      XComponent xDesktopComponent)
               throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "<init>");

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        packageLocation = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME);
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

        List<String> languages = loadedSettings.getLanguages();
        List<ParagraphStyle> paragraphStyles = loadedSettings.getParagraphStyles();
        List<CharacterStyle> characterStyles = loadedSettings.getCharacterStyles();
        List<HeadingStyle> headingStyles = loadedSettings.getHeadingStyles();
        List<ListStyle> listStyles = loadedSettings.getListStyles();
        TableStyle tableStyle = loadedSettings.getTableStyle();
        TocStyle tocStyle = loadedSettings.getTocStyle();
        Style footnoteStyle = loadedSettings.getFootnoteStyle();
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
            if ((b = getBooleanProperty(upperBorderProperty + "_heading_" + level)) != null) {
                headStyle.setUpperBorder(b);
            }
            if ((b = getBooleanProperty(lowerBorderProperty + "_heading_" + level)) != null) {
                headStyle.setLowerBorder(b);
            }
            if ((s = getStringProperty(upperBorderStyleProperty + "_heading_" + level)) != null) {
                if (s.length()==1) {
                    headStyle.setUpperBorderStyle(s.charAt(0));
                }
            }
            if ((s = getStringProperty(lowerBorderStyleProperty + "_heading_" + level)) != null) {
                if (s.length()==1) {
                    headStyle.setLowerBorderStyle(s.charAt(0));
                }
            }
            if (!((d = getDoubleProperty(paddingAboveProperty + "_heading_" + level)).isNaN())) {
                headStyle.setPaddingAbove(d.intValue());
            }
            if (!((d = getDoubleProperty(paddingBelowProperty + "_heading_" + level)).isNaN())) {
                headStyle.setPaddingBelow(d.intValue());
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
            if ((s = getStringProperty(listPrefixProperty + "_list_" + level)) != null) {
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
        if (!(d = getDoubleProperty(tableOfContentLevelProperty)).isNaN()) {
            tocStyle.setUptoLevel(d.intValue());
        }

        for (level=1;level<=10;level++) {

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

        if (!(d = getDoubleProperty(firstLineProperty + "_footnote")).isNaN()) {
            footnoteStyle.setFirstLine(d.intValue());
        }
        if (!((d = getDoubleProperty(runoversProperty + "_footnote")).isNaN())) {
            footnoteStyle.setRunovers(d.intValue());
        }
        if (!((d = getDoubleProperty(marginLeftRightProperty + "_footnote")).isNaN())) {
            footnoteStyle.setMarginLeftRight(d.intValue());
        }
        if (!((d = getDoubleProperty(linesAboveProperty + "_footnote")).isNaN())) {
            footnoteStyle.setLinesAbove(d.intValue());
        }
        if (!((d = getDoubleProperty(linesBelowProperty + "_footnote")).isNaN())) {
            footnoteStyle.setLinesBelow(d.intValue());
        }
        if ((s = getStringProperty(alignmentProperty + "_footnote")) != null) {
            try {
                footnoteStyle.setAlignment(Alignment.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid alignment option");
            }
        }

        for (String format : loadedSettings.getNoterefNumberFormats()) {
            if ((s = getStringProperty(noterefPrefixProperty + "_" + format)) != null) {
                loadedSettings.setNoterefNumberPrefix(format, s);
            }
        }
        if ((b = getBooleanProperty(noterefSpaceBeforeProperty)) != null) {
            loadedSettings.setNoterefSpaceBefore(b);
        }
        if ((b = getBooleanProperty(noterefSpaceAfterProperty)) != null) {
            loadedSettings.setNoterefSpaceAfter(b);
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
                    loadedSettings.getSpecialSymbol(i).setType(SpecialSymbol.Type.valueOf(s));
                } catch (IllegalArgumentException ex) {
                    logger.log(Level.SEVERE, null, s + " is no valid special symbol type");
                }
            }
            if ((s = getStringProperty(specialSymbolProperty + "_" + (i+1) + "_Mode")) != null) {
                try {
                    loadedSettings.getSpecialSymbol(i).setMode(SpecialSymbol.Mode.valueOf(s));
                } catch (IllegalArgumentException ex) {
                    logger.log(Level.SEVERE, null, s + " is no valid special symbol mode");
                }
            }
        }

        if ((b = getBooleanProperty(hyphenateProperty)) != null) {
            loadedSettings.setHyphenate(b);
        }

        if (!(d = getDoubleProperty(minSyllableLengthProperty)).isNaN()) {
            loadedSettings.setMinSyllableLength(d.intValue());
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

        if ((s = getStringProperty(volumeManagementModeProperty)) != null) {
            try {
                loadedSettings.setVolumeManagementMode(VolumeManagementMode.valueOf(s));
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, s + " is no valid volume management mode");
            }
        }

        if (!(d = getDoubleProperty(preferredVolumeSizeProperty)).isNaN()) {
            loadedSettings.setPreferredVolumeSize(d.intValue());
        }
        if (!(d = getDoubleProperty(minVolumeSizeProperty)).isNaN()) {
            loadedSettings.setMinVolumeSize(d.intValue());
        }
        if (!(d = getDoubleProperty(maxVolumeSizeProperty)).isNaN()) {
            loadedSettings.setMaxVolumeSize(d.intValue());
        }
        if (!(d = getDoubleProperty(minLastVolumeSizeProperty)).isNaN()) {
            loadedSettings.setMinLastVolumeSize(d.intValue());
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
            loadedSettings.setBrailleFileType(s);
        }

        if ((s = getStringProperty(exportTableProperty)) != null) {
            loadedSettings.setTable(s);
        }

        if ((b = getBooleanProperty(exportDuplexProperty)) != null) {
            loadedSettings.setDuplex(b);
        }

        if ((b = getBooleanProperty(exportEightDotsProperty)) != null) {
            loadedSettings.setEightDots(b);
        }

        if (!(d = getDoubleProperty(exportNumberOfCellsPerLineProperty)).isNaN()) {
            loadedSettings.setCellsPerLine(d.intValue());
        }

        if (!(d = getDoubleProperty(exportNumberOfLinesPerPageProperty)).isNaN()) {
            loadedSettings.setLinesPerPage(d.intValue());
        }

        if ((b = getBooleanProperty(exportMultipleFilesProperty)) != null) {
            loadedSettings.setMultipleFilesEnabled(b);
        }

        logger.exiting("SettingsIO", "loadExportSettingsFromDocument");

    }

    public void loadEmbossSettingsFromDocument (Settings loadedSettings)
                                         throws com.sun.star.uno.Exception {

        logger.entering("SettingsIO", "loadEmbossSettingsFromDocument");

        Double d;

        if (!(d = getDoubleProperty(marginRightProperty)).isNaN()) {
            loadedSettings.setMarginOuter(d.intValue());
        }

        if (!(d = getDoubleProperty(marginLeftProperty)).isNaN()) {
            loadedSettings.setMarginInner(d.intValue());
        }

        if (!(d = getDoubleProperty(marginBottomProperty)).isNaN()) {
            loadedSettings.setMarginBottom(d.intValue());
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
            loadedSettings.setEmbosser(s);
        }

        if ((s = embosserSettings.getProperty(saddleStitchProperty)) != null) {
            loadedSettings.setSaddleStitch(s.equals("true"));
        }

        if ((s = embosserSettings.getProperty(sheetsPerQuireProperty)) != null) {
            try {
                loadedSettings.setSheetsPerQuire(Integer.parseInt(s));
            } catch (NumberFormatException ex) {
                logger.log(Level.SEVERE, null, s + " is not an integer");
            }
        }

        if ((s = embosserSettings.getProperty(zFoldingProperty)) != null) {
            loadedSettings.setZFolding(s.equals("true"));
        }

        if ((s = embosserSettings.getProperty(embossDuplexProperty)) != null) {
            loadedSettings.setDuplex(s.equals("true"));
        }

        if ((s = embosserSettings.getProperty(embossEightDotsProperty)) != null) {
            loadedSettings.setEightDots(s.equals("true"));
        }

        if ((s = embosserSettings.getProperty(paperSizeProperty)) != null) {
            loadedSettings.setPaper(s);
            if (s.equals(Settings.CUSTOM_PAPER)) {
                String s1;
                String s2;
                if ((s1 = embosserSettings.getProperty(customPaperWidthProperty)) != null &&
                    (s2 = embosserSettings.getProperty(customPaperHeightProperty)) != null) {
                    try {
                        loadedSettings.setCustomPaper(Integer.parseInt(s1), Integer.parseInt(s2));
                    } catch (NumberFormatException ex) {
                        logger.log(Level.SEVERE, null, s1 + " or " + s2 + " is not an integer");
                    }
                }
            }
        }

        if ((s = embosserSettings.getProperty(embossTableProperty)) != null) {
            try {
                loadedSettings.setTable(s);
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
                             String key,
                             Object after,
                             Object before) {

        if (after!=null) {
            if (!after.equals(before)) {
                String value;
                if (after instanceof Embosser) {
                    value = ((Embosser)after).getIdentifier();
                } else if (after instanceof Paper) {
                    value = ((Paper)after).getIdentifier();
                } else if (after instanceof Table) {
                    value = ((Table)after).getIdentifier();
                } else {
                    value = String.valueOf(after);
                }
                properties.setProperty(key, value);
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

        List<String> languages = settingsAfterChange.getLanguages();
        List<SpecialSymbol> specialSymbolsAfterChange = settingsAfterChange.getSpecialSymbolsList();
        List<SpecialSymbol> specialSymbolsBeforeChange = settingsBeforeChange.getSpecialSymbolsList();
        List<ParagraphStyle> paragraphStylesAfterChange = settingsAfterChange.getParagraphStyles();
        List<ParagraphStyle> paragraphStylesBeforeChange = settingsBeforeChange.getParagraphStyles();
        List<CharacterStyle> characterStylesAfterChange = settingsAfterChange.getCharacterStyles();
        List<CharacterStyle> characterStylesBeforeChange = settingsBeforeChange.getCharacterStyles();
        List<HeadingStyle> headingStylesAfterChange = settingsAfterChange.getHeadingStyles();
        List<HeadingStyle> headingStylesBeforeChange = settingsBeforeChange.getHeadingStyles();
        List<ListStyle> listStylesBeforeChange = settingsBeforeChange.getListStyles();
        List<ListStyle> listStylesAfterChange = settingsAfterChange.getListStyles();
        TableStyle tableStyleBeforeChange = settingsBeforeChange.getTableStyle();
        TableStyle tableStyleAfterChange = settingsAfterChange.getTableStyle();
        TocStyle tocStyleBeforeChange = settingsBeforeChange.getTocStyle();
        TocStyle tocStyleAfterChange = settingsAfterChange.getTocStyle();
        Style footnoteStyleBeforeChange = settingsBeforeChange.getFootnoteStyle();
        Style footnoteStyleAfterChange = settingsAfterChange.getFootnoteStyle();
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
            setProperty(upperBorderProperty + "_heading_" + level,
                        headStyleAfterChange.getUpperBorder(),
                        headStyleBeforeChange.getUpperBorder());
            setProperty(lowerBorderProperty + "_heading_" + level,
                        headStyleAfterChange.getLowerBorder(),
                        headStyleBeforeChange.getLowerBorder());
            setProperty(upperBorderStyleProperty + "_heading_" + level,
                        String.valueOf(headStyleAfterChange.getUpperBorderStyle()),
                        String.valueOf(headStyleBeforeChange.getUpperBorderStyle()));
            setProperty(lowerBorderStyleProperty + "_heading_" + level,
                        String.valueOf(headStyleAfterChange.getLowerBorderStyle()),
                        String.valueOf(headStyleBeforeChange.getLowerBorderStyle()));
            setProperty(paddingAboveProperty + "_heading_" + level,
                        headStyleAfterChange.getPaddingAbove(),
                        headStyleBeforeChange.getPaddingBelow());
            setProperty(paddingBelowProperty + "_heading_" + level,
                        headStyleAfterChange.getPaddingBelow(),
                        headStyleBeforeChange.getPaddingBelow());
            
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
            setProperty(listPrefixProperty + "_list_" + level,
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

        setProperty(alignmentProperty + "_footnote",
                    footnoteStyleAfterChange.getAlignment().name(),
                    footnoteStyleBeforeChange.getAlignment().name());
        setProperty(firstLineProperty + "_footnote",
                    footnoteStyleAfterChange.getFirstLine(),
                    footnoteStyleBeforeChange.getFirstLine());
        setProperty(runoversProperty + "_footnote",
                    footnoteStyleAfterChange.getRunovers(),
                    footnoteStyleBeforeChange.getRunovers());
        setProperty(marginLeftRightProperty + "_footnote",
                    footnoteStyleAfterChange.getMarginLeftRight(),
                    footnoteStyleBeforeChange.getMarginLeftRight());
        setProperty(linesAboveProperty + "_footnote",
                    footnoteStyleAfterChange.getLinesAbove(),
                    footnoteStyleBeforeChange.getLinesAbove());
        setProperty(linesBelowProperty + "_footnote",
                    footnoteStyleAfterChange.getLinesBelow(),
                    footnoteStyleBeforeChange.getLinesBelow());

        for (String format : settingsBeforeChange.getNoterefNumberFormats()) {
            setProperty(noterefPrefixProperty + "_" + format,
                        settingsAfterChange.getNoterefNumberPrefix(format),
                        settingsBeforeChange.getNoterefNumberPrefix(format));
        }

        setProperty(noterefSpaceBeforeProperty,
                    settingsAfterChange.getNoterefSpaceBefore(),
                    settingsBeforeChange.getNoterefSpaceBefore());
        setProperty(noterefSpaceAfterProperty,
                    settingsAfterChange.getNoterefSpaceAfter(),
                    settingsBeforeChange.getNoterefSpaceAfter());

        setProperty(tableOfContentTitleProperty,
                    settingsAfterChange.getTableOfContentTitle(),
                    settingsBeforeChange.getTableOfContentTitle());
        setProperty(lineFillSymbolProperty,
                    String.valueOf(settingsAfterChange.getLineFillSymbol()),
                    String.valueOf(settingsBeforeChange.getLineFillSymbol()));
        setProperty(tableOfContentLevelProperty,
                    tocStyleAfterChange.getUptoLevel(),
                    tocStyleBeforeChange.getUptoLevel());

        for (level=1;level<=10;level++) {

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
        setProperty(minSyllableLengthProperty,
                    settingsAfterChange.getMinSyllableLength(),
                    settingsBeforeChange.getMinSyllableLength());
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
        setProperty(volumeManagementModeProperty,
                    settingsAfterChange.getVolumeManagementMode().name(),
                    settingsBeforeChange.getVolumeManagementMode().name());
        setProperty(preferredVolumeSizeProperty,
                    settingsAfterChange.getPreferredVolumeSize(),
                    settingsBeforeChange.getPreferredVolumeSize());
        setProperty(minVolumeSizeProperty,
                    settingsAfterChange.getMinVolumeSize(),
                    settingsBeforeChange.getMinVolumeSize());
        setProperty(maxVolumeSizeProperty,
                    settingsAfterChange.getMaxVolumeSize(),
                    settingsBeforeChange.getMaxVolumeSize());
        setProperty(minLastVolumeSizeProperty,
                    settingsAfterChange.getMinLastVolumeSize(),
                    settingsBeforeChange.getMinLastVolumeSize());
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
                    settingsAfterChange.getBrailleFileType().getIdentifier(),
                    settingsBeforeChange.getBrailleFileType().getIdentifier());
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
                    settingsAfterChange.getMultipleFilesEnabled(),
                    settingsBeforeChange.getMultipleFilesEnabled());

        if (settingsAfterChange.getTable() != null) {
            setProperty(exportTableProperty,
                        settingsAfterChange.getTable().getIdentifier(),
                        settingsBeforeChange.getTable().getIdentifier());
        }

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

        setProperty(marginLeftProperty,
                    settingsAfterChange.getMarginInner(),
                    settingsBeforeChange.getMarginInner());
        setProperty(marginRightProperty,
                    settingsAfterChange.getMarginOuter(),
                    settingsBeforeChange.getMarginOuter());
        setProperty(marginTopProperty,
                    settingsAfterChange.getMarginTop(),
                    settingsBeforeChange.getMarginTop());
        setProperty(marginBottomProperty,
                    settingsAfterChange.getMarginBottom(),
                    settingsBeforeChange.getMarginBottom());

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

        Properties embosserSettings = loadSettingsFromOpenOffice("embosser");

        odtModified = false;

        setProperty(embosserSettings,
                    embosserProperty,
                    settingsAfterChange.getEmbosser(),
                    settingsBeforeChange.getEmbosser());
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
                    paperSizeProperty,
                    settingsAfterChange.getPaper(),
                    settingsBeforeChange.getPaper());
        setProperty(embosserSettings,
                    embossDuplexProperty,
                    settingsAfterChange.getDuplex(),
                    settingsBeforeChange.getDuplex());
        setProperty(embosserSettings,
                    embossEightDotsProperty,
                    settingsAfterChange.getEightDots(),
                    settingsBeforeChange.getEightDots());
        setProperty(embosserSettings,
                    embossTableProperty,
                    settingsAfterChange.getTable(),
                    settingsBeforeChange.getTable());

        if (settingsAfterChange.getPaper().getIdentifier().equals(Settings.CUSTOM_PAPER)) {
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