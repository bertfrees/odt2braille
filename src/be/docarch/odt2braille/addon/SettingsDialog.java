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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Iterator;
import java.util.Arrays;

import com.sun.star.awt.XDialog;
import com.sun.star.uno.XComponentContext;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.lang.XComponent;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XRadioButton;
import com.sun.star.awt.XNumericField;
import com.sun.star.lang.EventObject;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XControl;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.awt.XItemEventBroadcaster;
import com.sun.star.awt.XTextListener;
import com.sun.star.awt.TextEvent;

import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.SpecialSymbol;
import be.docarch.odt2braille.SpecialSymbol.SpecialSymbolType;
import be.docarch.odt2braille.SpecialSymbol.SpecialSymbolMode;
import org_pef_text.pef2text.Paper;
import org_pef_text.pef2text.Paper.PaperSize;
import org_pef_text.pef2text.EmbosserFactory.EmbosserType;
import org_pef_text.TableFactory.TableType;
import be.docarch.odt2braille.Settings.BrailleFileType;
import be.docarch.odt2braille.Settings.MathType;
import be.docarch.odt2braille.Settings.BrailleRules;


/**
 * Show an OpenOffice.org dialog window for adjusting the braille settings.
 * The dialog has 10 tabs:
 * <ul>
 * <li>General Settings</li>
 * <li>Paragraph Settings</li>
 * <li>Heading Settings</li>
 * <li>List Settings</li>
 * <li>Table Settings</li>
 * <li>Pagenumber Settings</li>
 * <li>Language Settings (only enabled if the document contains multiple languages)</li>
 * <li>Table of Contents Settings</li>
 * <li>Mathematics Settings</li>
 * <li>Emboss/Export Settings</li>
 * </ul>
 *
 * @see         be.docarch.odt2braille.Settings
 * @author      Bert Frees
 */
public class SettingsDialog implements XItemListener,
                                       XActionListener,
                                       XTextListener {

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille.addon");

    private Settings settings = null;
    private XComponentContext xContext = null;

    public final static short SAVE_SETTINGS = 1;
    public final static short EXPORT = 2;
    public final static short EMBOSS = 3;

    private final static short GENERAL_PAGE = 1;
    private final static short LANGUAGES_PAGE = 2;
    private final static short PARAGRAPHS_PAGE = 3;
    private final static short HEADINGS_PAGE = 4;
    private final static short LISTS_PAGE = 5;
    private final static short TABLES_PAGE = 6;
    private final static short PAGENUMBERS_PAGE = 7;
    private final static short TOC_PAGE = 8;
    private final static short SPECIAL_SYMBOLS_PAGE = 9;
    private final static short MATH_PAGE = 10;
    private final static short EXPORT_EMBOSS_PAGE = 11;

    private final static short NUMBER_OF_PAGES = 11;

    private boolean[] pagesEnabled = {true,true,true,true,true,true,true,true,true,true,true};
    private boolean[] pagesVisited = {false,false,false,false,false,false,false,false,false,false,false};
    private int currentPage = 1;
    private short mode;

    private int currentHeadingLevel;
    private int currentListLevel;
    private int currentTableColumn;
    private int currentTableOfContentsLevel;
    private int selectedLanguagePos;
    private int selectedSpecialSymbolPos;
    
    private ArrayList<String> supportedTranslationTables = null;
    private ArrayList<String> languages = null;
    private ArrayList<SpecialSymbol> specialSymbols = null;

    private ArrayList<EmbosserType> embosserTypes = null;
    private ArrayList<TableType> tableTypes = null;
    private ArrayList<PaperSize> paperSizes = null;
    private ArrayList<BrailleFileType> genericBrailleTypes = null;
    private ArrayList<MathType> mathTypes = null;

    private static final int numberOfEmbosserTypes = EmbosserType.values().length;
    private static final int numberOfTableTypes = TableType.values().length;
    private static final int numberOfPaperSizes = PaperSize.values().length;
    private static final int numberOfGenericBrailleTypes = BrailleFileType.values().length;

    private XDialog dialog = null;
    private XControlContainer dialogControlContainer = null;
    private XComponent dialogComponent = null;

    // Main window

    private XButton okButton = null;
    private XButton cancelButton = null;
    private XButton backButton = null;
    private XButton nextButton = null;
    private XListBox brailleRulesListBox = null;
    private XItemEventBroadcaster roadMapBroadcaster = null;

    private XPropertySet windowProperties = null;
    private XPropertySet roadmapProperties = null;
    private XPropertySet okButtonProperties = null;
    private XPropertySet backButtonProperties = null;
    private XPropertySet nextButtonProperties = null;

    private static String _okButton = "CommandButton1";
    private static String _cancelButton = "CommandButton2";
    private static String _backButton = "CommandButton3";
    private static String _nextButton = "CommandButton4";
    private static String _brailleRulesListBox = "ListBox19";

    private static String L10N_windowTitle = null;
    private static String L10N_roadmapTitle = null;
    private static String[] L10N_roadmapLabels = new String[NUMBER_OF_PAGES];

    private String L10N_okButton1 = null;
    private String L10N_okButton2 = null;
    private String L10N_okButton3 = null;
    private String L10N_okButton4 = null;
    private String L10N_cancelButton = null;
    private String L10N_nextButton = null;
    private String L10N_backButton = null;

    private String L10N_left = null;
    private String L10N_center = null;

    // General Page

    private XTextComponent creatorField = null;
    private XTextComponent transcribersNotesPageField = null;
    private XListBox mainTranslationTableListBox = null;
    private XListBox mainGradeListBox = null;
    private XCheckBox transcribersNotesPageCheckBox = null;
    private XCheckBox transcriptionInfoCheckBox = null;
    private XCheckBox volumeInfoCheckBox = null;
    private XCheckBox preliminaryVolumeCheckBox = null;
    private XCheckBox hyphenateCheckBox = null;

    private XPropertySet transcribersNotesPageFieldProperties = null;    
    private XPropertySet transcriptionInfoCheckBoxProperties = null;
    private XPropertySet volumeInfoCheckBoxProperties = null;
    private XPropertySet transcribersNotesPageCheckBoxProperties = null;
    private XPropertySet creatorFieldProperties = null;
    private XPropertySet preliminaryVolumeCheckBoxProperties = null;

    private static String _mainTranslationTableListBox = "ListBox1";
    private static String _mainGradeListBox = "ListBox2";
    private static String _creatorField = "TextField1";
    private static String _transcribersNotesPageField = "TextField3";
    private static String _transcriptionInfoCheckBox = "CheckBox1";
    private static String _volumeInfoCheckBox = "CheckBox2";
    private static String _transcribersNotesPageCheckBox = "CheckBox4";
    private static String _preliminaryVolumeCheckBox = "CheckBox8";
    private static String _hyphenateCheckBox = "CheckBox20";

    private static String _mainTranslationTableLabel = "Label1";
    private static String _mainGradeLabel = "Label2";
    private static String _transcriptionInfoLabel = "Label3";
    private static String _creatorLabel = "Label4";
    private static String _volumeInfoLabel = "Label5";
    private static String _transcribersNotesPageLabel = "Label7";
    private static String _preliminaryVolumeLabel = "Label11";
    private static String _hyphenateLabel = "Label77";

    private String L10N_creatorLabel = null;
    private String L10N_mainTranslationTableLabel = null;
    private String L10N_mainGradeLabel = null;
    private String L10N_transcribersNotesPageLabel = null;
    private String L10N_transcriptionInfoLabel = null;
    private String L10N_volumeInfoLabel = null;
    private String L10N_preliminaryVolumeLabel = null;
    private String L10N_hyphenateLabel = null;

    // Paragraphs Page

    private XListBox paragraphAlignmentListBox = null;
    private XNumericField paragraphFirstLineField = null;
    private XNumericField paragraphRunoversField = null;
    private XNumericField paragraphLinesAboveField = null;
    private XNumericField paragraphLinesBelowField = null;

    private XPropertySet paragraphFirstLineFieldProperties = null;
    private XPropertySet paragraphRunoversFieldProperties = null;
    private XPropertySet paragraphAlignmentListBoxProperties = null;
    private XPropertySet paragraphLinesAboveProperties = null;
    private XPropertySet paragraphLinesBelowProperties = null;

    private static String _paragraphAlignmentListBox = "ListBox12";
    private static String _paragraphFirstLineField = "NumericField7";
    private static String _paragraphRunoversField = "NumericField8";
    private static String _paragraphLinesAboveField = "NumericField9";
    private static String _paragraphLinesBelowField = "NumericField10";

    private static String _paragraphAlignmentLabel = "Label37";
    private static String _paragraphFirstLineLabel = "Label31";
    private static String _paragraphRunoversLabel = "Label32";
    private static String _paragraphLinesAboveLabel = "Label33";
    private static String _paragraphLinesBelowLabel = "Label34";

    private String L10N_paragraphAlignmentLabel = null;
    private String L10N_paragraphFirstLineLabel = null;
    private String L10N_paragraphRunoversLabel = null;
    private String L10N_paragraphLinesAboveLabel = null;
    private String L10N_paragraphLinesBelowLabel = null;

    // Headings Page

    private XListBox headingLevelListBox = null;
    private XListBox headingAlignmentListBox = null;
    private XNumericField headingFirstLineField = null;
    private XNumericField headingRunoversField = null;
    private XNumericField headingLinesAboveField = null;
    private XNumericField headingLinesBelowField = null;

    private XPropertySet headingFirstLineFieldProperties = null;
    private XPropertySet headingRunoversFieldProperties = null;
    private XPropertySet headingAlignmentListBoxProperties = null;
    private XPropertySet headingLinesAboveProperties = null;
    private XPropertySet headingLinesBelowProperties = null;

    private static String _headingLevelListBox = "ListBox13";
    private static String _headingAlignmentListBox = "ListBox14";
    private static String _headingFirstLineField = "NumericField11";
    private static String _headingRunoversField = "NumericField12";
    private static String _headingLinesAboveField = "NumericField13";
    private static String _headingLinesBelowField = "NumericField14";

    private static String _headingLevelLabel = "Label42";
    private static String _headingAlignmentLabel = "Label43";
    private static String _headingFirstLineLabel = "Label38";
    private static String _headingRunoversLabel = "Label39";
    private static String _headingLinesAboveLabel = "Label40";
    private static String _headingLinesBelowLabel = "Label41";

    private String L10N_headingLevelLabel = null;
    private String L10N_headingAlignmentLabel = null;
    private String L10N_headingFirstLineLabel = null;
    private String L10N_headingRunoversLabel = null;
    private String L10N_headingLinesAboveLabel = null;
    private String L10N_headingLinesBelowLabel = null;

    // Lists Page

    private XNumericField listLinesAboveField = null;
    private XNumericField listLinesBelowField = null;
    private XNumericField listLinesBetweenField = null;
    private XListBox listLevelListBox = null;
    private XListBox listAlignmentListBox = null;
    private XNumericField listFirstLineField = null;
    private XNumericField listRunoversField = null;
    private XTextComponent listPrefixField = null;
    private XButton listPrefixButton = null;

    private XPropertySet listFirstLineFieldProperties = null;
    private XPropertySet listRunoversFieldProperties = null;
    private XPropertySet listAlignmentListBoxProperties = null;
    private XPropertySet listLinesAboveProperties = null;
    private XPropertySet listLinesBelowProperties = null;
    private XPropertySet listLinesBetweenProperties = null;

    private static String _listLinesAboveField = "NumericField17";
    private static String _listLinesBelowField = "NumericField18";
    private static String _listLinesBetweenField = "NumericField27";
    private static String _listLevelListBox = "ListBox11";
    private static String _listAlignmentListBox = "ListBox15";
    private static String _listFirstLineField = "NumericField15";
    private static String _listRunoversField = "NumericField16";
    private static String _listPrefixField = "TextField7";
    private static String _listPrefixButton = "CommandButton7";

    private static String _listLinesAboveLabel = "Label30";
    private static String _listLinesBelowLabel = "Label36";
    private static String _listLinesBetweenLabel = "Label60";
    private static String _listLevelLabel = "Label44";
    private static String _listAlignmentLabel = "Label45";
    private static String _listFirstLineLabel = "Label28";
    private static String _listRunoversLabel = "Label29";
    private static String _listPrefixLabel = "Label27";

    private String L10N_listLinesAboveLabel = null;
    private String L10N_listLinesBelowLabel = null;
    private String L10N_listLinesBetweenLabel = null;
    private String L10N_listLevelLabel = null;
    private String L10N_listAlignmentLabel = null;
    private String L10N_listFirstLineLabel = null;
    private String L10N_listRunoversLabel = null;
    private String L10N_listPrefixLabel = null;
    private String L10N_listPrefixButton = "...";

    // Tables Page

    private XRadioButton tableSimpleRadioButton = null;
    private XRadioButton tableStairstepRadioButton = null;
    private XNumericField tableLinesAboveField = null;
    private XNumericField tableLinesBelowField = null;
    private XNumericField tableLinesBetweenField = null;
    private XListBox tableColumnListBox = null;
    private XListBox tableAlignmentListBox = null;
    private XNumericField tableFirstLineField = null;
    private XNumericField tableRunoversField = null;
    private XTextComponent tableColumnDelimiterField = null;
    private XButton tableColumnDelimiterButton = null;

    private XPropertySet tableFirstLineFieldProperties = null;
    private XPropertySet tableRunoversFieldProperties = null;
    private XPropertySet tableColumnListBoxProperties = null;
    private XPropertySet tableColumnDelimiterFieldProperties = null;
    private XPropertySet tableColumnDelimiterButtonProperties = null;
    private XPropertySet tableSpacingGroupBoxProperties = null;
    private XPropertySet tablePositionGroupBoxProperties = null;
    private XPropertySet tableAlignmentListBoxProperties = null;
    private XPropertySet tableLinesAboveProperties = null;
    private XPropertySet tableLinesBelowProperties = null;
    private XPropertySet tableLinesBetweenProperties = null;
    private XPropertySet tableSimpleRadioButtonProperties = null;

    private static String _tableSimpleRadioButton = "OptionButton3";
    private static String _tableStairstepRadioButton = "OptionButton4";
    private static String _tableLinesAboveField = "NumericField21";
    private static String _tableLinesBelowField = "NumericField22";
    private static String _tableLinesBetweenField = "NumericField28";
    private static String _tableColumnListBox = "ListBox16";
    private static String _tableAlignmentListBox = "ListBox17";
    private static String _tableFirstLineField = "NumericField19";
    private static String _tableRunoversField = "NumericField20";
    private static String _tableColumnDelimiterField = "TextField5";
    private static String _tableColumnDelimiterButton = "CommandButton5";

    private static String _tableSimpleLabel = "Label23";
    private static String _tableStairstepLabel = "Label25";
    private static String _tableLinesAboveLabel = "Label48";
    private static String _tableLinesBelowLabel = "Label49";
    private static String _tableLinesBetweenLabel = "Label59";
    private static String _tableColumnLabel = "Label50";
    private static String _tableAlignmentLabel = "Label51";
    private static String _tableFirstLineLabel = "Label46";
    private static String _tableRunoversLabel = "Label47";
    private static String _tableColumnDelimiterLabel = "Label24";

    private String L10N_tableSimpleLabel = null;
    private String L10N_tableStairstepLabel = null;
    private String L10N_tableLinesAboveLabel = null;
    private String L10N_tableLinesBelowLabel = null;
    private String L10N_tableLinesBetweenLabel = null;
    private String L10N_tableColumnLabel = null;
    private String L10N_tableAlignmentLabel = null;
    private String L10N_tableFirstLineLabel = null;
    private String L10N_tableRunoversLabel = null;
    private String L10N_tableSpacingLabel = null;
    private String L10N_tablePositionLabel = null;
    private String L10N_tableColumnDelimiterLabel = null;
    private String L10N_tableColumnDelimiterButton = "...";

    // Pagenumbers Page

    private XCheckBox braillePageNumbersCheckBox = null;
    private XListBox braillePageNumberAtListBox = null;
    private XListBox preliminaryPageNumberFormatListBox = null;
    private XCheckBox printPageNumbersCheckBox = null;
    private XListBox printPageNumberAtListBox = null;
    private XCheckBox printPageNumberRangeCheckBox = null;
    private XCheckBox continuePagesCheckBox = null;
    private XCheckBox pageSeparatorCheckBox = null;
    private XCheckBox pageSeparatorNumberCheckBox = null;
    private XCheckBox ignoreEmptyPagesCheckBox = null;
    private XCheckBox mergeUnnumberedPagesCheckBox = null;
    private XCheckBox numbersAtTopOnSepLineCheckBox = null;
    private XCheckBox numbersAtBottomOnSepLineCheckBox = null;
    private XCheckBox hardPageBreaksCheckBox = null;
    
    private XPropertySet braillePageNumbersCheckBoxProperties = null;
    private XPropertySet braillePageNumberAtListBoxProperties = null;
    private XPropertySet preliminaryPageNumberFormatListBoxProperties = null;
    private XPropertySet printPageNumbersCheckBoxProperties = null;
    private XPropertySet printPageNumberAtListBoxProperties = null;
    private XPropertySet printPageNumberRangeCheckBoxProperties = null;
    private XPropertySet continuePagesCheckBoxProperties = null;
    private XPropertySet pageSeparatorCheckBoxProperties = null;
    private XPropertySet pageSeparatorNumberCheckBoxProperties = null;
    private XPropertySet ignoreEmptyPagesCheckBoxProperties = null;
    private XPropertySet mergeUnnumberedPagesCheckBoxProperties = null;
    private XPropertySet numbersAtTopOnSepLineCheckBoxProperties = null;
    private XPropertySet numbersAtBottomOnSepLineCheckBoxProperties = null;

    private static String _braillePageNumbersCheckBox = "CheckBox7";
    private static String _braillePageNumberAtListBox = "ListBox21";
    private static String _preliminaryPageNumberFormatListBox = "ListBox20";
    private static String _printPageNumbersCheckBox = "CheckBox6";
    private static String _printPageNumberAtListBox = "ListBox22";
    private static String _printPageNumberRangeCheckBox = "CheckBox11";
    private static String _continuePagesCheckBox = "CheckBox12";
    private static String _pageSeparatorCheckBox = "CheckBox13";
    private static String _pageSeparatorNumberCheckBox = "CheckBox14";
    private static String _ignoreEmptyPagesCheckBox = "CheckBox15";
    private static String _mergeUnnumberedPagesCheckBox = "CheckBox16";
    private static String _numbersAtTopOnSepLineCheckBox = "CheckBox17";
    private static String _numbersAtBottomOnSepLineCheckBox = "CheckBox18";
    private static String _hardPageBreaksCheckBox = "CheckBox19";

    private static String _braillePageNumbersLabel = "Label10";
    private static String _braillePageNumberAtLabel = "Label57";
    private static String _preliminaryPageNumberFormatLabel = "Label55";
    private static String _printPageNumbersLabel = "Label9";
    private static String _printPageNumberAtLabel = "Label62";
    private static String _printPageNumberRangeLabel = "Label61";
    private static String _continuePagesLabel = "Label63";
    private static String _pageSeparatorLabel = "Label64";
    private static String _pageSeparatorNumberLabel = "Label65";
    private static String _ignoreEmptyPagesLabel = "Label66";
    private static String _mergeUnnumberedPagesLabel = "Label67";
    private static String _numbersAtTopOnSepLineLabel = "Label68";
    private static String _numbersAtBottomOnSepLineLabel = "Label69";
    private static String _hardPageBreaksLabel = "Label72";

    private String L10N_braillePageNumbersLabel = null;
    private String L10N_braillePageNumberAtLabel = null;
    private String L10N_preliminaryPageNumberFormatLabel = null;
    private String L10N_printPageNumbersLabel = null;
    private String L10N_printPageNumberAtLabel = null;
    private String L10N_printPageNumberRangeLabel = null;
    private String L10N_continuePagesLabel = null;
    private String L10N_pageSeparatorLabel = null;
    private String L10N_pageSeparatorNumberLabel = null;
    private String L10N_ignoreEmptyPagesLabel = null;
    private String L10N_mergeUnnumberedPagesLabel = null;
    private String L10N_numbersAtTopOnSepLineLabel = null;
    private String L10N_numbersAtBottomOnSepLineLabel = null;
    private String L10N_hardPageBreaksLabel = null;
    private String L10N_top = null;
    private String L10N_bottom = null;

    // Languages Page

    private XListBox translationTableListBox = null;
    private XListBox gradeListBox = null;
    private XListBox languagesListBox = null;

    private static String _languagesListBox = "ListBox3";
    private static String _translationTableListBox = "ListBox4";
    private static String _gradeListBox = "ListBox5";

    private static String _translationTableLabel = "Label12";
    private static String _gradeLabel = "Label13";

    private String L10N_translationTableLabel = null;
    private String L10N_gradeLabel = null;

    private TreeMap<String,String> L10N_languages = new TreeMap();

    // Table of Contents Page

    private XCheckBox tableOfContentsCheckBox = null;
    private XTextComponent tableOfContentsTitleField = null;
    private XNumericField tableOfContentsLinesBetweenField = null;
    private XListBox tableOfContentsLevelListBox = null;
    private XNumericField tableOfContentsFirstLineField = null;
    private XNumericField tableOfContentsRunoversField = null;
    private XTextComponent tableOfContentsLineFillField = null;
    private XButton tableOfContentsLineFillButton = null;

    private XPropertySet tableOfContentsCheckBoxProperties = null;
    private XPropertySet tableOfContentsTitleFieldProperties = null;
    private XPropertySet tableOfContentsLinesBetweenFieldProperties = null;
    private XPropertySet tableOfContentsLevelListBoxProperties = null;
    private XPropertySet tableOfContentsFirstLineFieldProperties = null;
    private XPropertySet tableOfContentsRunoversFieldProperties = null;
    private XPropertySet tableOfContentsLineFillFieldProperties = null;
    private XPropertySet tableOfContentsLineFillButtonProperties = null;
    private XPropertySet tableOfContentsSpacingGroupBoxProperties = null;
    private XPropertySet tableOfContentsPositionGroupBoxProperties = null;

    private static String _tableOfContentsCheckBox = "CheckBox5";
    private static String _tableOfContentsTitleField = "TextField4";
    private static String _tableOfContentsLinesBetweenField = "NumericField25";
    private static String _tableOfContentsLevelListBox = "ListBox18";
    private static String _tableOfContentsFirstLineField = "NumericField23";
    private static String _tableOfContentsRunoversField = "NumericField24";
    private static String _tableOfContentsLineFillField = "TextField6";
    private static String _tableOfContentsLineFillButton = "CommandButton6";

    private static String _tableOfContentsLabel = "Label8";
    private static String _tableOfContentsTitleLabel = "Label58";
    private static String _tableOfContentsLinesBetweenLabel = "Label54";
    private static String _tableOfContentsLevelLabel = "Label56";
    private static String _tableOfContentsFirstLineLabel = "Label52";
    private static String _tableOfContentsRunoversLabel = "Label53";
    private static String _tableOfContentsLineFillLabel = "Label26";

    private String L10N_tableOfContentsLabel = null;
    private String L10N_tableOfContentsTitleLabel = null;
    private String L10N_tableOfContentsLinesBetweenLabel = null;
    private String L10N_tableOfContentsLevelLabel = null;
    private String L10N_tableOfContentsFirstLineLabel = null;
    private String L10N_tableOfContentsRunoversLabel = null;
    private String L10N_tableOfContentsSpacingLabel = null;
    private String L10N_tableOfContentsPositionLabel = null;
    private String L10N_tableOfContentsLineFillLabel = null;
    private String L10N_tableOfContentsLineFillButton = "...";

    // Special Symbols Page
    
    private XCheckBox specialSymbolsListCheckBox = null;
    private XTextComponent specialSymbolsListField = null;
    private XListBox specialSymbolsListBox = null;
    private XTextComponent specialSymbolsSymbolField = null;
    private XButton specialSymbolsSymbolButton = null;
    private XTextComponent specialSymbolsDescriptionField = null;
    private XRadioButton specialSymbolsMode0RadioButton = null;
    private XRadioButton specialSymbolsMode1RadioButton = null;
    private XRadioButton specialSymbolsMode2RadioButton = null;
    private XRadioButton specialSymbolsMode3RadioButton = null;
    private XButton specialSymbolsAddButton = null;
    private XButton specialSymbolsRemoveButton = null;
    private XButton specialSymbolsMoveUpButton = null;
    private XButton specialSymbolsMoveDownButton = null;

    private XPropertySet specialSymbolsListCheckBoxProperties = null;
    private XPropertySet specialSymbolsListFieldProperties = null;
    private XPropertySet specialSymbolsListBoxProperties = null;
    private XPropertySet specialSymbolsSymbolFieldProperties = null;
    private XPropertySet specialSymbolsSymbolButtonProperties = null;
    private XPropertySet specialSymbolsDescriptionFieldProperties = null;
    private XPropertySet specialSymbolsMode0RadioButtonProperties = null;
    private XPropertySet specialSymbolsMode1RadioButtonProperties = null;
    private XPropertySet specialSymbolsMode2RadioButtonProperties = null;
    private XPropertySet specialSymbolsMode3RadioButtonProperties = null;
    private XPropertySet specialSymbolsAddButtonProperties = null;
    private XPropertySet specialSymbolsRemoveButtonProperties = null;
    private XPropertySet specialSymbolsMoveUpButtonProperties = null;
    private XPropertySet specialSymbolsMoveDownButtonProperties = null;

    private static String _specialSymbolsListCheckBox = "CheckBox3";
    private static String _specialSymbolsListField = "TextField2";
    private static String _specialSymbolsListBox = "ListBox25";
    private static String _specialSymbolsSymbolField = "TextField8";
    private static String _specialSymbolsSymbolButton = "CommandButton8";
    private static String _specialSymbolsDescriptionField = "TextField9";
    private static String _specialSymbolsMode0RadioButton = "OptionButton8";
    private static String _specialSymbolsMode1RadioButton = "OptionButton7";
    private static String _specialSymbolsMode2RadioButton = "OptionButton6";
    private static String _specialSymbolsMode3RadioButton = "OptionButton5";
    private static String _specialSymbolsAddButton = "CommandButton11";
    private static String _specialSymbolsRemoveButton = "CommandButton9";
    private static String _specialSymbolsMoveUpButton = "CommandButton10";
    private static String _specialSymbolsMoveDownButton = "CommandButton12";
    
    private static String _specialSymbolsListLabel = "Label6";
    private static String _specialSymbolsListTitleLabel = "Label73";
    private static String _specialSymbolsLabel = "Label74";
    private static String _specialSymbolsSymbolLabel = "Label75";
    private static String _specialSymbolsDescriptionLabel = "Label76";
    private static String _specialSymbolsMode0Label = "Label81";
    private static String _specialSymbolsMode1Label = "Label80";
    private static String _specialSymbolsMode2Label = "Label79";
    private static String _specialSymbolsMode3Label = "Label78";
    
    private String L10N_specialSymbolsListLabel = null;
    private String L10N_specialSymbolsListTitleLabel = null;
    private String L10N_specialSymbolsLabel = null;
    private String L10N_specialSymbolsSymbolLabel = null;
    private String L10N_specialSymbolsDescriptionLabel = null;
    private String L10N_specialSymbolsMode0Label = null;
    private String L10N_specialSymbolsMode1Label = null;
    private String L10N_specialSymbolsMode2Label = null;
    private String L10N_specialSymbolsMode3Label = null;
    private String L10N_specialSymbolsSymbolButton = "...";

    // Mathematics Page

    private XListBox mathListBox = null;
    private static String _mathListBox = "ListBox10";
    private static String _mathLabel = "Label35";
    private String L10N_mathLabel = null;

    // Export & Emboss Page

    private XRadioButton genericRadioButton = null;
    private XRadioButton specificRadioButton = null;
    private XListBox embosserListBox = null;
    private XListBox genericBrailleListBox = null;
    private XListBox tableListBox = null;
    private XListBox paperSizeListBox = null;
    private XNumericField paperWidthField = null;
    private XNumericField paperHeightField = null;
    private XListBox paperWidthUnitListBox = null;
    private XListBox paperHeightUnitListBox = null;
    private XCheckBox duplexCheckBox = null;
    private XCheckBox mirrorAlignCheckBox = null;
    private XNumericField numberOfCellsPerLineField = null;
    private XNumericField numberOfLinesPerPageField = null;
    private XNumericField marginLeftField = null;
    private XNumericField marginRightField = null;
    private XNumericField marginTopField = null;
    private XNumericField marginBottomField = null;

    private XTextComponent paperWidthTextComponent = null;
    private XTextComponent paperHeightTextComponent = null;
    private XTextComponent numberOfCellsPerLineTextComponent = null;
    private XTextComponent numberOfLinesPerPageTextComponent = null;
    private XTextComponent marginLeftTextComponent = null;
    private XTextComponent marginRightTextComponent = null;
    private XTextComponent marginTopTextComponent = null;
    private XTextComponent marginBottomTextComponent = null;

    private XPropertySet embosserListBoxProperties = null;
    private XPropertySet genericBrailleListBoxProperties = null;
    private XPropertySet tableListBoxProperties = null;
    private XPropertySet paperSizeListBoxProperties = null;
    private XPropertySet paperWidthFieldProperties = null;
    private XPropertySet paperHeightFieldProperties = null;
    private XPropertySet paperWidthUnitListBoxProperties = null;
    private XPropertySet paperHeightUnitListBoxProperties = null;
    private XPropertySet duplexCheckBoxProperties = null;
    private XPropertySet mirrorAlignCheckBoxProperties = null;
    private XPropertySet marginLeftFieldProperties = null;
    private XPropertySet marginRightFieldProperties = null;
    private XPropertySet marginTopFieldProperties = null;
    private XPropertySet marginBottomFieldProperties = null;
    private XPropertySet genericRadioButtonProperties = null;

    private static String _genericRadioButton = "OptionButton1";
    private static String _specificRadioButton = "OptionButton2";
    private static String _genericBrailleListBox = "ListBox6";
    private static String _embosserListBox = "ListBox7";
    private static String _tableListBox = "ListBox8";
    private static String _paperSizeListBox = "ListBox9";
    private static String _paperWidthField = "NumericField26";
    private static String _paperHeightField = "NumericField29";
    private static String _paperWidthUnitListBox = "ListBox24";
    private static String _paperHeightUnitListBox = "ListBox23";
    private static String _duplexCheckBox = "CheckBox9";
    private static String _mirrorAlignCheckBox = "CheckBox10";
    private static String _numberOfCellsPerLineField = "NumericField1";
    private static String _numberOfLinesPerPageField = "NumericField2";
    private static String _marginLeftField = "NumericField3";
    private static String _marginRightField = "NumericField4";
    private static String _marginTopField = "NumericField5";
    private static String _marginBottomField = "NumericField6";

    private static String _genericLabel = "Label14";
    private static String _specificLabel = "Label15";
    private static String _tableLabel = "Label16";
    private static String _paperSizeLabel = "Label17";
    private static String _paperWidthLabel = "Label70";
    private static String _paperHeightLabel = "Label71";
    private static String _duplexLabel = "Label18";
    private static String _mirrorAlignLabel = "Label19";
    private static String _numberOfCellsPerLineLabel = "Label20";
    private static String _numberOfLinesPerPageLabel = "Label21";
    private static String _marginLabel = "Label22";

    private String L10N_genericLabel = null;
    private String L10N_specificLabel = null;
    private String L10N_tableLabel = null;
    private String L10N_paperSizeLabel = null;
    private String L10N_paperWidthLabel = null;
    private String L10N_paperHeightLabel = null;
    private String L10N_duplexLabel = null;
    private String L10N_mirrorAlignLabel = null;
    private String L10N_numberOfCellsPerLineLabel = null;
    private String L10N_numberOfLinesPerPageLabel = null;
    private String L10N_marginLabel = null;

    private TreeMap<Integer,String> L10N_grades = new TreeMap();
    private TreeMap<String,String> L10N_translationTables = new TreeMap();
    private TreeMap<String,String> L10N_embosser = new TreeMap();
    private TreeMap<String,String> L10N_table = new TreeMap();
    private TreeMap<String,String> L10N_paperSize = new TreeMap();
    private TreeMap<String,String> L10N_genericBraille = new TreeMap();
    private TreeMap<String,String> L10N_math = new TreeMap();


    /**
     * The dialog is created from an OpenOffice.org .xdl file.
     *
     * @param   xContext
     * @param   settings    The braille settings.
     * @param   mode        {@link #SAVE_SETTINGS}, {@link #EXPORT} or {@link #EMBOSS}, depending on how the dialog was called.
     *                      Which tab is showed first and the behaviour of the OK button will slightly vary with the mode.
     */
    public SettingsDialog(XComponentContext xContext,
                          Settings settings,
                          short mode)
                   throws com.sun.star.uno.Exception {

        logger.entering("SettingsDialog", "<init>");

        this.settings = settings;
        this.mode = mode;
        this.xContext = xContext;

        supportedTranslationTables = settings.getSupportedTranslationTables();
        languages = settings.getLanguages();
        mathTypes = new ArrayList(Arrays.asList(MathType.values()));
        pagesEnabled[LANGUAGES_PAGE-1] = (languages.size()>1);

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn-windows_x86")
                                                            + "/dialogs/SettingsDialog.xdl";

        // L10N

        Locale oooLocale;
        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }

        // Main Window

        L10N_windowTitle = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("settingsDialogTitle");
        L10N_roadmapTitle = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("settingsRoadmapTitle");
        L10N_roadmapLabels[GENERAL_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("generalSettingsPageTitle");
        L10N_roadmapLabels[PARAGRAPHS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paragraphSettingsPageTitle");
        L10N_roadmapLabels[HEADINGS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("headingSettingsPageTitle");
        L10N_roadmapLabels[LISTS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listSettingsPageTitle");
        L10N_roadmapLabels[TABLES_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableSettingsPageTitle");
        L10N_roadmapLabels[PAGENUMBERS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("pagenumberSettingsPageTitle");
        L10N_roadmapLabels[LANGUAGES_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("languageSettingsPageTitle");
        L10N_roadmapLabels[TOC_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsSettingsPageTitle");
        L10N_roadmapLabels[SPECIAL_SYMBOLS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsSettingsPageTitle");
        L10N_roadmapLabels[MATH_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("mathSettingsPageTitle");
        L10N_roadmapLabels[EXPORT_EMBOSS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("exportEmbossSettingsPageTitle");

        L10N_okButton1 = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("embossButton");
        L10N_okButton2 = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("printToFileButton");
        L10N_okButton3 = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("exportButton");
        L10N_okButton4 = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("saveSettingsButton");
        L10N_cancelButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("cancelButton");
        L10N_backButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("backButton");
        L10N_nextButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("nextButton");

        L10N_left = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("left");
        L10N_center = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("center");

        // General Page

        L10N_creatorLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("creatorLabel") + ":";
        L10N_mainTranslationTableLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("languageLabel") + ":";
        L10N_mainGradeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("gradeLabel") + ":";
        L10N_transcribersNotesPageLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("transcribersNotesPageLabel");
        L10N_transcriptionInfoLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("transcriptionInfoLabel");
        L10N_volumeInfoLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("volumeInfoLabel");
        L10N_preliminaryVolumeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("preliminaryVolumeLabel");
        L10N_hyphenateLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("hyphenateLabel");

        // Paragraphs Page

        L10N_paragraphAlignmentLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("alignmentLabel") + ":";
        L10N_paragraphFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_paragraphRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_paragraphLinesAboveLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesAboveLabel") + ":";
        L10N_paragraphLinesBelowLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesBelowLabel") + ":";

        // Headings Page

        L10N_headingLevelLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("levelLabel");
        L10N_headingAlignmentLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("alignmentLabel") + ":";
        L10N_headingFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_headingRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_headingLinesAboveLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesAboveLabel") + ":";
        L10N_headingLinesBelowLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesBelowLabel") + ":";

        // Lists Page

        L10N_listLinesAboveLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listLinesAboveLabel") + ":";
        L10N_listLinesBelowLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listLinesBelowLabel") + ":";
        L10N_listLinesBetweenLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listLinesBetweenLabel") + ":";
        L10N_listLevelLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("levelLabel");
        L10N_listAlignmentLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("alignmentLabel") + ":";
        L10N_listFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_listRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_listPrefixLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listPrefixLabel") + ":";

        // Tables Page

        L10N_tableSimpleLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("simpleTableLabel");
        L10N_tableStairstepLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("stairstepTableLabel");
        L10N_tableLinesAboveLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesAboveLabel") + ":";
        L10N_tableLinesBelowLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesBelowLabel") + ":";
        L10N_tableLinesBetweenLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableLinesBetweenLabel") + ":";
        L10N_tableColumnLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("columnLabel");
        L10N_tableAlignmentLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("alignmentLabel") + ":";
        L10N_tableFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_tableRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_tableColumnDelimiterLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("columnDelimiterLabel") + ":";
        L10N_tableSpacingLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("spacingLabel");
        L10N_tablePositionLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("positionLabel");

        // Pagenumbers Page

        L10N_braillePageNumbersLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("braillePageNumbersLabel");
        L10N_braillePageNumberAtLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("braillePageNumberAtLabel") + ":";
        L10N_preliminaryPageNumberFormatLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("preliminaryPageNumberFormatLabel") + ":";
        L10N_printPageNumbersLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("printPageNumbersLabel");
        L10N_printPageNumberAtLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("printPageNumberAtLabel") + ":";
        L10N_printPageNumberRangeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("printPageNumberRangeLabel");
        L10N_continuePagesLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("continuePagesLabel");
        L10N_pageSeparatorLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("pageSeparatorLabel");
        L10N_pageSeparatorNumberLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("pageSeparatorNumberLabel");
        L10N_ignoreEmptyPagesLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("ignoreEmptyPagesLabel");
        L10N_mergeUnnumberedPagesLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("mergeUnnumberedPagesLabel");
        L10N_numbersAtTopOnSepLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("numbersAtTopOnSepLineLabel");
        L10N_numbersAtBottomOnSepLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("numbersAtBottomOnSepLineLabel");
        L10N_hardPageBreaksLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("hardPageBreaksLabel");
        L10N_top = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("top");
        L10N_bottom = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("bottom");

        // Languages Page

        L10N_translationTableLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("languageLabel") + ":";
        L10N_gradeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("gradeLabel") + ":";

        // Table of Contents Page

        L10N_tableOfContentsLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsLabel");
        L10N_tableOfContentsTitleLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsTitleLabel") + ":";
        L10N_tableOfContentsLinesBetweenLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsLinesBetweenLabel") + ":";
        L10N_tableOfContentsLevelLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("levelLabel");
        L10N_tableOfContentsFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_tableOfContentsRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_tableOfContentsLineFillLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("lineFillSymbolLabel") + ":";
        L10N_tableOfContentsSpacingLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("spacingLabel");
        L10N_tableOfContentsPositionLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("positionLabel");

        // Special Symbols Page
        
        L10N_specialSymbolsListLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsListLabel");
        L10N_specialSymbolsListTitleLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsListTitleLabel") + ":";
        L10N_specialSymbolsLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsLabel");
        L10N_specialSymbolsSymbolLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsSymbolLabel") + ":";
        L10N_specialSymbolsDescriptionLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsDescriptionLabel") + ":";
        L10N_specialSymbolsMode0Label = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsMode0Label");
        L10N_specialSymbolsMode1Label = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsMode1Label");
        L10N_specialSymbolsMode2Label = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsMode2Label");
        L10N_specialSymbolsMode3Label = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsMode3Label");

        // Mathematics Page

        L10N_mathLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("formulasLabel") + ":";

        // Export & Emboss Page

        L10N_genericLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("genericLabel");
        L10N_specificLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specificLabel");
        L10N_tableLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableLabel") + ":";
        L10N_paperSizeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paperSizeLabel") + ":";
        L10N_paperWidthLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paperWidthLabel") + ":";
        L10N_paperHeightLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paperHeightLabel") + ":";
        L10N_duplexLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("duplexLabel");
        L10N_mirrorAlignLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("mirrorAlignLabel");
        L10N_numberOfCellsPerLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("numberOfCellsPerLineLabel") + ":";
        L10N_numberOfLinesPerPageLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("numberOfLinesPerPageLabel") + ":";
        L10N_marginLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("marginLabel") + ":";

        L10N_grades.put(0, "Grade 0 (computer Braille)");
        L10N_grades.put(1, "Grade 1 (uncontracted)");
        L10N_grades.put(2, "Grade 2 (contracted)");
        L10N_grades.put(3, "Grade 3");

        L10N_genericBraille.put("NONE",  "-");
        L10N_genericBraille.put("PEF",   "PEF (Portable Embosser Format)");
        L10N_genericBraille.put("BRF",   "BRF (Braille Formatted)");
        
        L10N_embosser.put("NONE",              "-");
        L10N_embosser.put("INDEX_BASIC",       "Index Braille - 3.30 Basic V2");
        L10N_embosser.put("INDEX_EVEREST",     "Index Braille - 9.20 Everest V2");
        L10N_embosser.put("INDEX_EVEREST_V3",  "Index Braille - Everest V3");
        L10N_embosser.put("INDEX_BASIC_D_V3",  "Index Braille - Basic-D V3");
        L10N_embosser.put("BRAILLO_200",       "Braillo 200 (firmware 000.17 or later)");
        L10N_embosser.put("BRAILLO_400_S",     "Braillo 400S (firmware 000.17 or later)");
        L10N_embosser.put("BRAILLO_400_SR",    "Braillo 400SR (firmware 000.17 or later)");
        L10N_embosser.put("INTERPOINT_55",     "Interpoint 55");

        L10N_table.put("EN_US",                "US Computer Braille");
        L10N_table.put("EN_GB",                "US Computer Braille (Lower Case)");
        L10N_table.put("BRAILLO_6DOT_001_00",  "Braillo USA 6 Dot 001.00");
        L10N_table.put("BRAILLO_6DOT_044_00",  "Braillo England 6 Dot 044.00");
        L10N_table.put("BRAILLO_6DOT_046_01",  "Braillo Sweden 6 Dot 046.01");
        L10N_table.put("BRAILLO_6DOT_047_01",  "Braillo Norway 6 Dot 047.01");
        L10N_table.put("UNICODE_BRAILLE",      "PEF (Unicode Braille)");
        L10N_table.put("BRF",                  "BRF (ASCII Braille)");
        L10N_table.put("BRL",                  "BRL (Non-ASCII Braille)");
        L10N_table.put("UNDEFINED",            "-");

        L10N_paperSize.put("A4",                "A4");
        L10N_paperSize.put("W210MM_X_H10INCH",  "210 mm x 10 inch");
        L10N_paperSize.put("W210MM_X_H11INCH",  "210 mm x 11 inch");
        L10N_paperSize.put("W210MM_X_H12INCH",  "210 mm x 12 inch");
        L10N_paperSize.put("FA44",              "FA44 Accurate");
        L10N_paperSize.put("FA44_LEGACY",       "FA44 Legacy");
        L10N_paperSize.put("UNDEFINED",         "-");
        L10N_paperSize.put("CUSTOM",            "Custom");

        L10N_math.put("NEMETH",    "Nemeth");
        L10N_math.put("UKMATHS",   "UK Maths");
        L10N_math.put("MARBURG",   "Marburg");
        L10N_math.put("WISKUNDE",  "Notaert");

        String key = null;
        String value = null;

        for (int i=0;i<languages.size();i++) {

            key = languages.get(i);
            value = (new Locale(key.substring(0,key.indexOf("-")),key.substring(key.indexOf("-")+1,key.length()))).getDisplayName(oooLocale);
            if (key.equals(settings.getMainLanguage())) { value += " - DEFAULT"; }
            L10N_languages.put(key, value);

        }

        for (int i=0;i<supportedTranslationTables.size();i++) {

            key = supportedTranslationTables.get(i);
            value = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("language_" + key);
            L10N_translationTables.put(key, value);
        }

        // Sort languages & supportedTranslationTables

        languages.clear();
        supportedTranslationTables.clear();

        TreeSet treeSet = new TreeSet(new Comparator() {
            public int compare(Object obj, Object obj1) { return ((Comparable) ((Map.Entry) obj).getValue()).compareTo(((Map.Entry) obj1).getValue()); }});

        treeSet.addAll(L10N_languages.entrySet());
        for (Iterator i = treeSet.iterator(); i.hasNext();) {
            languages.add(((Map.Entry<String,String>)i.next()).getKey());
        }
        treeSet.clear();
        treeSet.addAll(L10N_translationTables.entrySet());
        for (Iterator i = treeSet.iterator(); i.hasNext();) {
            supportedTranslationTables.add(((Map.Entry<String,String>)i.next()).getKey());
        }

        // Dialog creation

        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        XControl dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        XMultiServiceFactory xMSFDialog = (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class, dialogControl.getModel());
        XNameContainer dialogNameContainer = (XNameContainer)UnoRuntime.queryInterface(XNameContainer.class, dialogControl.getModel());
        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,dialogControl.getModel());

        // Roadmap

        int roadMapWidth = 85;
        int roadMapHeight = 215;

        Object roadmapModel = xMSFDialog.createInstance("com.sun.star.awt.UnoControlRoadmapModel");
        XMultiPropertySet roadMapMPSet = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, roadmapModel);
        String roadmapName = "SettingsRoadmap";
        roadMapMPSet.setPropertyValues( new String[] {"Complete",    "Height",      "Name",      "PositionX", "PositionY", "Text",            "Width" },
                                        new Object[] {Boolean.FALSE, roadMapHeight, roadmapName, 0,           0,           L10N_roadmapTitle, roadMapWidth});
        roadmapProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, roadmapModel);
        dialogNameContainer.insertByName(roadmapName, roadmapModel);
        XSingleServiceFactory xSSFRoadmap = (XSingleServiceFactory) UnoRuntime.queryInterface(XSingleServiceFactory.class, roadmapModel);
        XIndexContainer roadmapIndexContainer = (XIndexContainer) UnoRuntime.queryInterface(XIndexContainer.class, roadmapModel);
        XControl roadmapControl = dialogControlContainer.getControl(roadmapName);
        roadMapBroadcaster = (XItemEventBroadcaster) UnoRuntime.queryInterface(XItemEventBroadcaster.class, roadmapControl);

        Object roadmapItem = null;
        XPropertySet roadMapItemProperties = null;
        for (int i=0;i<NUMBER_OF_PAGES;i++) {

            roadmapItem = xSSFRoadmap.createInstance();
            roadMapItemProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, roadmapItem);
            roadMapItemProperties.setPropertyValue("Label", L10N_roadmapLabels[i]);
            roadMapItemProperties.setPropertyValue("Enabled", pagesEnabled[i]);
            roadMapItemProperties.setPropertyValue("ID", new Integer(i+1));
            roadmapIndexContainer.insertByIndex(i, roadmapItem);

        }

        // Dialog items

        // Main Window

        okButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_okButton));
        cancelButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_cancelButton));
        backButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_backButton));
        nextButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_nextButton));
        brailleRulesListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_brailleRulesListBox));

        // General Page

        mainTranslationTableListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_mainTranslationTableListBox));
        mainGradeListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_mainGradeListBox));
        creatorField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_creatorField));
        transcribersNotesPageField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_transcribersNotesPageField));
        transcribersNotesPageCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_transcribersNotesPageCheckBox));
        transcriptionInfoCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_transcriptionInfoCheckBox));
        volumeInfoCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_volumeInfoCheckBox));
        preliminaryVolumeCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_preliminaryVolumeCheckBox));
        hyphenateCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_hyphenateCheckBox));

        // Paragraphs Page

        paragraphAlignmentListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paragraphAlignmentListBox));
        paragraphFirstLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphFirstLineField));
        paragraphRunoversField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphRunoversField));
        paragraphLinesAboveField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphLinesAboveField));
        paragraphLinesBelowField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphLinesBelowField));

        // Headings page

        headingLevelListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_headingLevelListBox));
        headingAlignmentListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_headingAlignmentListBox));
        headingFirstLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingFirstLineField));
        headingRunoversField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingRunoversField));
        headingLinesAboveField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingLinesAboveField));
        headingLinesBelowField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingLinesBelowField));

        // Lists Page

        listLinesAboveField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_listLinesAboveField));
        listLinesBelowField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_listLinesBelowField));
        listLinesBetweenField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_listLinesBetweenField));
        listLevelListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_listLevelListBox));
        listAlignmentListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_listAlignmentListBox));
        listFirstLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_listFirstLineField));
        listRunoversField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_listRunoversField));
        listPrefixField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_listPrefixField));
        listPrefixButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_listPrefixButton));

        // Tables Page

        tableSimpleRadioButton = (XRadioButton) UnoRuntime.queryInterface(XRadioButton.class,
                dialogControlContainer.getControl(_tableSimpleRadioButton));
        tableStairstepRadioButton = (XRadioButton) UnoRuntime.queryInterface(XRadioButton.class,
                dialogControlContainer.getControl(_tableStairstepRadioButton));
        tableLinesAboveField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableLinesAboveField));
        tableLinesBelowField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableLinesBelowField));
        tableLinesBetweenField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableLinesBetweenField));
        tableColumnListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_tableColumnListBox));
        tableAlignmentListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_tableAlignmentListBox));
        tableFirstLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableFirstLineField));
        tableRunoversField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableRunoversField));
        tableColumnDelimiterField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_tableColumnDelimiterField));
        tableColumnDelimiterButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_tableColumnDelimiterButton));

        // Pagenumbers Page

        braillePageNumbersCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_braillePageNumbersCheckBox));
        braillePageNumberAtListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_braillePageNumberAtListBox));
        preliminaryPageNumberFormatListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_preliminaryPageNumberFormatListBox));
        printPageNumbersCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_printPageNumbersCheckBox));
        printPageNumberAtListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_printPageNumberAtListBox));
        printPageNumberRangeCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_printPageNumberRangeCheckBox));
        continuePagesCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_continuePagesCheckBox));
        pageSeparatorCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_pageSeparatorCheckBox));
        pageSeparatorNumberCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_pageSeparatorNumberCheckBox));
        ignoreEmptyPagesCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_ignoreEmptyPagesCheckBox));
        mergeUnnumberedPagesCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_mergeUnnumberedPagesCheckBox));
        numbersAtTopOnSepLineCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_numbersAtTopOnSepLineCheckBox));
        numbersAtBottomOnSepLineCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_numbersAtBottomOnSepLineCheckBox));
        hardPageBreaksCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_hardPageBreaksCheckBox));

        // Languages Page

        translationTableListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_translationTableListBox));
        gradeListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_gradeListBox));
        languagesListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_languagesListBox));

        // Table of Contents Page

        tableOfContentsCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_tableOfContentsCheckBox));
        tableOfContentsTitleField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_tableOfContentsTitleField));
        tableOfContentsLinesBetweenField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableOfContentsLinesBetweenField));
        tableOfContentsLevelListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_tableOfContentsLevelListBox));
        tableOfContentsFirstLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableOfContentsFirstLineField));
        tableOfContentsRunoversField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableOfContentsRunoversField));
        tableOfContentsLineFillField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_tableOfContentsLineFillField));
        tableOfContentsLineFillButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_tableOfContentsLineFillButton));

        // Special Symbols Page

        specialSymbolsListCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_specialSymbolsListCheckBox));
        specialSymbolsListField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_specialSymbolsListField));
        specialSymbolsListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_specialSymbolsListBox));
        specialSymbolsSymbolField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_specialSymbolsSymbolField));
        specialSymbolsSymbolButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_specialSymbolsSymbolButton));
        specialSymbolsDescriptionField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_specialSymbolsDescriptionField));
        specialSymbolsMode0RadioButton = (XRadioButton) UnoRuntime.queryInterface(XRadioButton.class,
                dialogControlContainer.getControl(_specialSymbolsMode0RadioButton));
        specialSymbolsMode1RadioButton = (XRadioButton) UnoRuntime.queryInterface(XRadioButton.class,
                dialogControlContainer.getControl(_specialSymbolsMode1RadioButton));
        specialSymbolsMode2RadioButton = (XRadioButton) UnoRuntime.queryInterface(XRadioButton.class,
                dialogControlContainer.getControl(_specialSymbolsMode2RadioButton));
        specialSymbolsMode3RadioButton = (XRadioButton) UnoRuntime.queryInterface(XRadioButton.class,
                dialogControlContainer.getControl(_specialSymbolsMode3RadioButton));
        specialSymbolsAddButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_specialSymbolsAddButton));
        specialSymbolsRemoveButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_specialSymbolsRemoveButton));
        specialSymbolsMoveUpButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_specialSymbolsMoveUpButton));
        specialSymbolsMoveDownButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_specialSymbolsMoveDownButton));

        // Mathematics Page

        mathListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_mathListBox));

        // Export & Emboss Page

        genericRadioButton = (XRadioButton) UnoRuntime.queryInterface(XRadioButton.class,
                dialogControlContainer.getControl(_genericRadioButton));
        specificRadioButton = (XRadioButton) UnoRuntime.queryInterface(XRadioButton.class,
                dialogControlContainer.getControl(_specificRadioButton));
        embosserListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_embosserListBox));
        genericBrailleListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_genericBrailleListBox));
        tableListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_tableListBox));
        paperSizeListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paperSizeListBox));
        paperWidthField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paperWidthField));
        paperHeightField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paperHeightField));
        paperWidthUnitListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paperWidthUnitListBox));
        paperHeightUnitListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paperHeightUnitListBox));
        duplexCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_duplexCheckBox));
        mirrorAlignCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_mirrorAlignCheckBox));
        numberOfCellsPerLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_numberOfCellsPerLineField));
        numberOfLinesPerPageField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_numberOfLinesPerPageField));
        marginLeftField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginLeftField));
        marginRightField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginRightField));
        marginTopField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginTopField));
        marginBottomField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginBottomField));
        paperWidthTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_paperWidthField));
        paperHeightTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_paperHeightField));
        numberOfCellsPerLineTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_numberOfCellsPerLineField));
        numberOfLinesPerPageTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_numberOfLinesPerPageField));
        marginLeftTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginLeftField));
        marginRightTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginRightField));
        marginTopTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginTopField));
        marginBottomTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginBottomField));

        // PROPERTIES

        // Main Window

        okButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, okButton)).getModel());
        backButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, backButton)).getModel());
        nextButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, nextButton)).getModel());

        // General Page

        creatorFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, creatorField)).getModel());
        transcribersNotesPageFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, transcribersNotesPageField)).getModel());
        transcribersNotesPageCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, transcribersNotesPageCheckBox)).getModel());
        volumeInfoCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, volumeInfoCheckBox)).getModel());
        transcriptionInfoCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, transcriptionInfoCheckBox)).getModel());
        preliminaryVolumeCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, preliminaryVolumeCheckBox)).getModel());

        // Paragraphs Page

        paragraphFirstLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphFirstLineField)).getModel());
        paragraphRunoversFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphRunoversField)).getModel());
        paragraphAlignmentListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphAlignmentListBox)).getModel());
        paragraphLinesAboveProperties =(XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphLinesAboveField)).getModel());
        paragraphLinesBelowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphLinesBelowField)).getModel());

        // Headings Page

        headingFirstLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingFirstLineField)).getModel());
        headingRunoversFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingRunoversField)).getModel());
        headingAlignmentListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingAlignmentListBox)).getModel());
        headingLinesAboveProperties =(XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingLinesAboveField)).getModel());
        headingLinesBelowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingLinesBelowField)).getModel());

        // Lists Page

        listFirstLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listFirstLineField)).getModel());
        listRunoversFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listRunoversField)).getModel());
        listAlignmentListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listAlignmentListBox)).getModel());
        listLinesAboveProperties =(XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listLinesAboveField)).getModel());
        listLinesBelowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listLinesBelowField)).getModel());
        listLinesBetweenProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listLinesBetweenField)).getModel());

        // Tables Page

        tableFirstLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableFirstLineField)).getModel());
        tableRunoversFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableRunoversField)).getModel());
        tableColumnListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableColumnListBox)).getModel());
        tableColumnDelimiterFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableColumnDelimiterField)).getModel());
        tableColumnDelimiterButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableColumnDelimiterButton)).getModel());
        tableAlignmentListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableAlignmentListBox)).getModel());
        tableLinesAboveProperties =(XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableLinesAboveField)).getModel());
        tableLinesBelowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableLinesBelowField)).getModel());
        tableLinesBetweenProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableLinesBetweenField)).getModel());
        tableSimpleRadioButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableSimpleRadioButton)).getModel());

        // Pagenumbers Page

        braillePageNumbersCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, braillePageNumbersCheckBox)).getModel());
        braillePageNumberAtListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, braillePageNumberAtListBox)).getModel());
        preliminaryPageNumberFormatListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, preliminaryPageNumberFormatListBox)).getModel());
        printPageNumbersCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, printPageNumbersCheckBox)).getModel());
        printPageNumberAtListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, printPageNumberAtListBox)).getModel());
        printPageNumberRangeCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, printPageNumberRangeCheckBox)).getModel());
        continuePagesCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, continuePagesCheckBox)).getModel());
        pageSeparatorCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, pageSeparatorCheckBox)).getModel());
        pageSeparatorNumberCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, pageSeparatorNumberCheckBox)).getModel());
        ignoreEmptyPagesCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, ignoreEmptyPagesCheckBox)).getModel());
        mergeUnnumberedPagesCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, mergeUnnumberedPagesCheckBox)).getModel());
        numbersAtTopOnSepLineCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, numbersAtTopOnSepLineCheckBox)).getModel());
        numbersAtBottomOnSepLineCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, numbersAtBottomOnSepLineCheckBox)).getModel());

        // Languages Page

        // Table of Contents Page

        tableOfContentsCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsCheckBox)).getModel());
        tableOfContentsTitleFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsTitleField)).getModel());
        tableOfContentsLinesBetweenFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsLinesBetweenField)).getModel());
        tableOfContentsLevelListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsLevelListBox)).getModel());
        tableOfContentsFirstLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsFirstLineField)).getModel());
        tableOfContentsRunoversFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsRunoversField)).getModel());
        tableOfContentsLineFillFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsLineFillField)).getModel());
        tableOfContentsLineFillButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsLineFillButton)).getModel());

        // Special Symbols Page
        
        specialSymbolsListCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsListCheckBox)).getModel());
        specialSymbolsListFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsListField)).getModel());
        specialSymbolsListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsListBox)).getModel());
        specialSymbolsSymbolFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsSymbolField)).getModel());
        specialSymbolsSymbolButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsSymbolButton)).getModel());
        specialSymbolsDescriptionFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsDescriptionField)).getModel());
        specialSymbolsMode0RadioButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsMode0RadioButton)).getModel());
        specialSymbolsMode1RadioButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsMode1RadioButton)).getModel());
        specialSymbolsMode2RadioButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsMode2RadioButton)).getModel());
        specialSymbolsMode3RadioButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsMode3RadioButton)).getModel());
        specialSymbolsAddButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsAddButton)).getModel());
        specialSymbolsRemoveButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsRemoveButton)).getModel());
        specialSymbolsMoveUpButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsMoveUpButton)).getModel());
        specialSymbolsMoveDownButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, specialSymbolsMoveDownButton)).getModel());
        
        // Mathematics Page

        // Export & Emboss Page

        genericRadioButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, genericRadioButton)).getModel());
        embosserListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, embosserListBox)).getModel());
        genericBrailleListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, genericBrailleListBox)).getModel());
        tableListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableListBox)).getModel());
        paperSizeListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperSizeListBox)).getModel());
        paperWidthFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperWidthField)).getModel());
        paperHeightFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperHeightField)).getModel());
        paperWidthUnitListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperWidthUnitListBox)).getModel());
        paperHeightUnitListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperHeightUnitListBox)).getModel());
        duplexCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, duplexCheckBox)).getModel());
        mirrorAlignCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, mirrorAlignCheckBox)).getModel());
        marginLeftFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginLeftField)).getModel());
        marginRightFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginRightField)).getModel());
        marginTopFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginTopField)).getModel());
        marginBottomFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginBottomField)).getModel());

        // Group Boxes

        String groupBoxProperties[] = new String[] {"Height", "Name", "PositionX", "PositionY", "Width", "Step"};

        String tableSpacingGroupBoxName = "tableSpacingGroupBox";
        String tablePositionGroupBoxName = "tablePositionGroupBox";
        String tableOfContentsSpacingGroupBoxName = "tableOfContentsSpacingGroupBox";
        String tableOfContentsPositionGroupBoxName = "tableOfContentsPositionGroupBox";

        Object tableSpacingGroupBoxModel = xMSFDialog.createInstance("com.sun.star.awt.UnoControlGroupBoxModel");
        Object tablePositionGroupBoxModel = xMSFDialog.createInstance("com.sun.star.awt.UnoControlGroupBoxModel");
        Object tableOfContentsSpacingGroupBoxModel = xMSFDialog.createInstance("com.sun.star.awt.UnoControlGroupBoxModel");
        Object tableOfContentsPositionGroupBoxModel = xMSFDialog.createInstance("com.sun.star.awt.UnoControlGroupBoxModel");

        XMultiPropertySet tableSpacingGroupBoxMPSet = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, tableSpacingGroupBoxModel);
        XMultiPropertySet tablePositionGroupBoxMPSet = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, tablePositionGroupBoxModel);
        XMultiPropertySet tableOfContentsSpacingGroupBoxMPSet = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, tableOfContentsSpacingGroupBoxModel);
        XMultiPropertySet tableOfContentsPositionGroupBoxMPSet = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, tableOfContentsPositionGroupBoxModel);

        tableSpacingGroupBoxMPSet.setPropertyValues(groupBoxProperties,            new Object[] { 1+roadMapHeight-57,  tableSpacingGroupBoxName,            roadMapWidth, 57,  200, TABLES_PAGE });
        tablePositionGroupBoxMPSet.setPropertyValues(groupBoxProperties,           new Object[] { 1+roadMapHeight-133, tablePositionGroupBoxName,           roadMapWidth, 133, 200, TABLES_PAGE });
        tableOfContentsSpacingGroupBoxMPSet.setPropertyValues(groupBoxProperties,  new Object[] { 1+roadMapHeight-75,  tableOfContentsSpacingGroupBoxName,  roadMapWidth, 75,  200, TOC_PAGE    });
        tableOfContentsPositionGroupBoxMPSet.setPropertyValues(groupBoxProperties, new Object[] { 1+roadMapHeight-108, tableOfContentsPositionGroupBoxName, roadMapWidth, 108, 200, TOC_PAGE    });

        dialogNameContainer.insertByName(tableSpacingGroupBoxName, tableSpacingGroupBoxModel);
        dialogNameContainer.insertByName(tablePositionGroupBoxName, tablePositionGroupBoxModel);
        dialogNameContainer.insertByName(tableOfContentsSpacingGroupBoxName, tableOfContentsSpacingGroupBoxModel);
        dialogNameContainer.insertByName(tableOfContentsPositionGroupBoxName, tableOfContentsPositionGroupBoxModel);

        tableSpacingGroupBoxProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, tableSpacingGroupBoxModel);
        tablePositionGroupBoxProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, tablePositionGroupBoxModel);
        tableOfContentsSpacingGroupBoxProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, tableOfContentsSpacingGroupBoxModel);
        tableOfContentsPositionGroupBoxProperties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, tableOfContentsPositionGroupBoxModel);

        tableSpacingGroupBoxProperties.setPropertyValue("Step", TABLES_PAGE);
        tablePositionGroupBoxProperties.setPropertyValue("Step", TABLES_PAGE);
        tableOfContentsSpacingGroupBoxProperties.setPropertyValue("Step", TOC_PAGE);
        tableOfContentsPositionGroupBoxProperties.setPropertyValue("Step", TOC_PAGE);

        logger.exiting("SettingsDialog", "<init>");

    }

    /**
     * An <code>XActionListener</code>, <code>XItemListener</code> or <code>XTextListener</code> (= always the dialog itself) is added to the dialog items.
     *
     */
    private void addListeners() {

        roadMapBroadcaster.addItemListener(this);
        brailleRulesListBox.addItemListener(this);
        backButton.addActionListener(this);
        nextButton.addActionListener(this);

        tableColumnDelimiterButton.addActionListener(this);
        tableOfContentsLineFillButton.addActionListener(this);
        listPrefixButton.addActionListener(this);
        specialSymbolsSymbolButton.addActionListener(this);
        specialSymbolsAddButton.addActionListener(this);
        specialSymbolsRemoveButton.addActionListener(this);
        specialSymbolsMoveUpButton.addActionListener(this);
        specialSymbolsMoveDownButton.addActionListener(this);

        transcriptionInfoCheckBox.addItemListener(this);
        volumeInfoCheckBox.addItemListener(this);
        transcribersNotesPageCheckBox.addItemListener(this);
        mainTranslationTableListBox.addItemListener(this);
        mainGradeListBox.addItemListener(this);
        paragraphAlignmentListBox.addItemListener(this);
        headingAlignmentListBox.addItemListener(this);
        headingLevelListBox.addItemListener(this);
        listAlignmentListBox.addItemListener(this);
        listLevelListBox.addItemListener(this);
        tableSimpleRadioButton.addItemListener(this);
        tableStairstepRadioButton.addItemListener(this);
        tableColumnListBox.addItemListener(this);
        tableAlignmentListBox.addItemListener(this);
        languagesListBox.addItemListener(this);
        translationTableListBox.addItemListener(this);
        gradeListBox.addItemListener(this);
        tableOfContentsCheckBox.addItemListener(this);
        tableOfContentsLevelListBox.addItemListener(this);
        specialSymbolsListCheckBox.addItemListener(this);
        specialSymbolsListBox.addItemListener(this);
        specialSymbolsMode0RadioButton.addItemListener(this);
        specialSymbolsMode1RadioButton.addItemListener(this);
        specialSymbolsMode2RadioButton.addItemListener(this);
        specialSymbolsMode3RadioButton.addItemListener(this);

        genericRadioButton.addItemListener(this);
        specificRadioButton.addItemListener(this);
        embosserListBox.addItemListener(this);
        genericBrailleListBox.addItemListener(this);
        paperSizeListBox.addItemListener(this);
        paperWidthUnitListBox.addItemListener(this);
        paperHeightUnitListBox.addItemListener(this);
        duplexCheckBox.addItemListener(this);

        braillePageNumbersCheckBox.addItemListener(this);
        braillePageNumberAtListBox.addItemListener(this);
        printPageNumbersCheckBox.addItemListener(this);
        printPageNumberAtListBox.addItemListener(this);
        printPageNumberRangeCheckBox.addItemListener(this);
        pageSeparatorCheckBox.addItemListener(this);

        paperWidthTextComponent.addTextListener(this);
        paperHeightTextComponent.addTextListener(this);
        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginLeftTextComponent.addTextListener(this);
        marginRightTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);

    }

    /**
     * The dialog is executed. It is disposed when the user presses OK or Cancel.
     *
     * @return          <code>true</code> if the user pressed OK;
     *                  <code>false</code> if the user pressed Cancel.
     */
    public boolean execute() throws com.sun.star.uno.Exception {

        logger.entering("SettingsDialog", "execute");
        
        setLabels();
        setDialogValues();
        addListeners();
        short ret = dialog.execute();
        getDialogValues();
        dialogComponent.dispose();

        logger.exiting("SettingsDialog", "execute");

        if (ret == ((short) PushButtonType.OK_value)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the dialog labels.
     *
     */
    private void setLabels() throws com.sun.star.uno.Exception {

        XFixedText xFixedText = null;

        windowProperties.setPropertyValue("Title", L10N_windowTitle);

        cancelButton.setLabel(L10N_cancelButton);
        backButton.setLabel(L10N_backButton);
        nextButton.setLabel(L10N_nextButton);

        tableColumnDelimiterButton.setLabel(L10N_tableColumnDelimiterButton);
        tableOfContentsLineFillButton.setLabel(L10N_tableOfContentsLineFillButton);
        listPrefixButton.setLabel(L10N_listPrefixButton);
        specialSymbolsSymbolButton.setLabel(L10N_specialSymbolsSymbolButton);

        tableSpacingGroupBoxProperties.setPropertyValue("Label", L10N_tableSpacingLabel);
        tablePositionGroupBoxProperties.setPropertyValue("Label", L10N_tablePositionLabel);
        tableOfContentsSpacingGroupBoxProperties.setPropertyValue("Label", L10N_tableOfContentsSpacingLabel);
        tableOfContentsPositionGroupBoxProperties.setPropertyValue("Label", L10N_tableOfContentsPositionLabel);

        // General Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mainTranslationTableLabel));
        xFixedText.setText(L10N_mainTranslationTableLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mainGradeLabel));
        xFixedText.setText(L10N_mainGradeLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_creatorLabel));
        xFixedText.setText(L10N_creatorLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_transcriptionInfoLabel));
        xFixedText.setText(L10N_transcriptionInfoLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_volumeInfoLabel));
        xFixedText.setText(L10N_volumeInfoLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_transcribersNotesPageLabel));
        xFixedText.setText(L10N_transcribersNotesPageLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_preliminaryVolumeLabel));
        xFixedText.setText(L10N_preliminaryVolumeLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_hyphenateLabel));
        xFixedText.setText(L10N_hyphenateLabel);

        // Paragraphs Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphAlignmentLabel));
        xFixedText.setText(L10N_paragraphAlignmentLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphFirstLineLabel));
        xFixedText.setText(L10N_paragraphFirstLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphRunoversLabel));
        xFixedText.setText(L10N_paragraphRunoversLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphLinesAboveLabel));
        xFixedText.setText(L10N_paragraphLinesAboveLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphLinesBelowLabel));
        xFixedText.setText(L10N_paragraphLinesBelowLabel);

        // Headings page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingLevelLabel));
        xFixedText.setText(L10N_headingLevelLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingAlignmentLabel));
        xFixedText.setText(L10N_headingAlignmentLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingFirstLineLabel));
        xFixedText.setText(L10N_headingFirstLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingRunoversLabel));
        xFixedText.setText(L10N_headingRunoversLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingLinesAboveLabel));
        xFixedText.setText(L10N_headingLinesAboveLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingLinesBelowLabel));
        xFixedText.setText(L10N_headingLinesBelowLabel);

        // Lists Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listLinesAboveLabel));
        xFixedText.setText(L10N_listLinesAboveLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listLinesBelowLabel));
        xFixedText.setText(L10N_listLinesBelowLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listLinesBetweenLabel));
        xFixedText.setText(L10N_listLinesBetweenLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listLevelLabel));
        xFixedText.setText(L10N_listLevelLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listAlignmentLabel));
        xFixedText.setText(L10N_listAlignmentLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listFirstLineLabel));
        xFixedText.setText(L10N_listFirstLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listRunoversLabel));
        xFixedText.setText(L10N_listRunoversLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listPrefixLabel));
        xFixedText.setText(L10N_listPrefixLabel);

        // Tables Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableSimpleLabel));
        xFixedText.setText(L10N_tableSimpleLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableStairstepLabel));
        xFixedText.setText(L10N_tableStairstepLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableLinesAboveLabel));
        xFixedText.setText(L10N_tableLinesAboveLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableLinesBelowLabel));
        xFixedText.setText(L10N_tableLinesBelowLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableLinesBetweenLabel));
        xFixedText.setText(L10N_tableLinesBetweenLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableColumnLabel));
        xFixedText.setText(L10N_tableColumnLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableAlignmentLabel));
        xFixedText.setText(L10N_tableAlignmentLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableFirstLineLabel));
        xFixedText.setText(L10N_tableFirstLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableRunoversLabel));
        xFixedText.setText(L10N_tableRunoversLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableColumnDelimiterLabel));
        xFixedText.setText(L10N_tableColumnDelimiterLabel);

        // Pagenumbers Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_braillePageNumbersLabel));
        xFixedText.setText(L10N_braillePageNumbersLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_braillePageNumberAtLabel));
        xFixedText.setText(L10N_braillePageNumberAtLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_preliminaryPageNumberFormatLabel));
        xFixedText.setText(L10N_preliminaryPageNumberFormatLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_printPageNumbersLabel));
        xFixedText.setText(L10N_printPageNumbersLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_printPageNumberAtLabel));
        xFixedText.setText(L10N_printPageNumberAtLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_printPageNumberRangeLabel));
        xFixedText.setText(L10N_printPageNumberRangeLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_continuePagesLabel));
        xFixedText.setText(L10N_continuePagesLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_pageSeparatorLabel));
        xFixedText.setText(L10N_pageSeparatorLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_pageSeparatorNumberLabel));
        xFixedText.setText(L10N_pageSeparatorNumberLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_ignoreEmptyPagesLabel));
        xFixedText.setText(L10N_ignoreEmptyPagesLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mergeUnnumberedPagesLabel));
        xFixedText.setText(L10N_mergeUnnumberedPagesLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numbersAtTopOnSepLineLabel));
        xFixedText.setText(L10N_numbersAtTopOnSepLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numbersAtBottomOnSepLineLabel));
        xFixedText.setText(L10N_numbersAtBottomOnSepLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_hardPageBreaksLabel));
        xFixedText.setText(L10N_hardPageBreaksLabel);

        // Languages Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_translationTableLabel));
        xFixedText.setText(L10N_translationTableLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_gradeLabel));
        xFixedText.setText(L10N_gradeLabel);

        // Table of Contents Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsLabel));
        xFixedText.setText(L10N_tableOfContentsLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsTitleLabel));
        xFixedText.setText(L10N_tableOfContentsTitleLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsLinesBetweenLabel));
        xFixedText.setText(L10N_tableOfContentsLinesBetweenLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsLevelLabel));
        xFixedText.setText(L10N_tableOfContentsLevelLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsFirstLineLabel));
        xFixedText.setText(L10N_tableOfContentsFirstLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsRunoversLabel));
        xFixedText.setText(L10N_tableOfContentsRunoversLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsLineFillLabel));
        xFixedText.setText(L10N_tableOfContentsLineFillLabel);

        // Special Symbols Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsListLabel));
        xFixedText.setText(L10N_specialSymbolsListLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsListTitleLabel));
        xFixedText.setText(L10N_specialSymbolsListTitleLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsLabel));
        xFixedText.setText(L10N_specialSymbolsLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsSymbolLabel));
        xFixedText.setText(L10N_specialSymbolsSymbolLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsDescriptionLabel));
        xFixedText.setText(L10N_specialSymbolsDescriptionLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsMode0Label));
        xFixedText.setText(L10N_specialSymbolsMode0Label);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsMode1Label));
        xFixedText.setText(L10N_specialSymbolsMode1Label);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsMode2Label));
        xFixedText.setText(L10N_specialSymbolsMode2Label);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsMode3Label));
        xFixedText.setText(L10N_specialSymbolsMode3Label);

        // Mathematics Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mathLabel));
        xFixedText.setText(L10N_mathLabel);

        // Export & Emboss Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_genericLabel));
        xFixedText.setText(L10N_genericLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specificLabel));
        xFixedText.setText(L10N_specificLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableLabel));
        xFixedText.setText(L10N_tableLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperSizeLabel));
        xFixedText.setText(L10N_paperSizeLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperWidthLabel));
        xFixedText.setText(L10N_paperWidthLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperHeightLabel));
        xFixedText.setText(L10N_paperHeightLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_duplexLabel));
        xFixedText.setText(L10N_duplexLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mirrorAlignLabel));
        xFixedText.setText(L10N_mirrorAlignLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numberOfCellsPerLineLabel));
        xFixedText.setText(L10N_numberOfCellsPerLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numberOfLinesPerPageLabel));
        xFixedText.setText(L10N_numberOfLinesPerPageLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginLabel));
        xFixedText.setText(L10N_marginLabel);

    }

    /**
     * Set the initial dialog values and field properties.
     *
     */
    private void setDialogValues() throws com.sun.star.uno.Exception {

        if (pagesEnabled[GENERAL_PAGE-1]) {

            transcribersNotesPageCheckBoxProperties.setPropertyValue("Enabled", settings.PRELIMINARY_PAGES_PRESENT);
            preliminaryVolumeCheckBoxProperties.setPropertyValue("Enabled", settings.PRELIMINARY_PAGES_PRESENT);
            transcriptionInfoCheckBoxProperties.setPropertyValue("Enabled", settings.TRANSCRIPTION_INFO_AVAILABLE);
            volumeInfoCheckBoxProperties.setPropertyValue("Enabled", settings.VOLUME_INFO_AVAILABLE);

            creatorField.setText(settings.getCreator());
            transcribersNotesPageField.setText(settings.transcribersNotesPageTitle);
            transcriptionInfoCheckBox.setState((short)(settings.transcriptionInfoEnabled?1:0));
            volumeInfoCheckBox.setState((short)(settings.volumeInfoEnabled?1:0));
            transcribersNotesPageCheckBox.setState((short)(settings.transcribersNotesPageEnabled?1:0));
            preliminaryVolumeCheckBox.setState((short)(settings.preliminaryVolumeEnabled?1:0));
            hyphenateCheckBox.setState((short)(settings.getHyphenate()?1:0));

            for (int i=0;i<supportedTranslationTables.size();i++) {
                mainTranslationTableListBox.addItem(L10N_translationTables.get(supportedTranslationTables.get(i)), (short)i);
            }

            updateMainTranslationTableListBox();
            updateMainGradeListBox();
            updateGeneralPageFieldProperties();

        }

        if (pagesEnabled[PARAGRAPHS_PAGE-1]) {

            paragraphAlignmentListBox.addItem(L10N_left, (short)0);
            paragraphAlignmentListBox.addItem(L10N_center, (short)1);

            paragraphFirstLineField.setDecimalDigits((short)0);
            paragraphFirstLineField.setMin((double)0);
            paragraphFirstLineField.setMax((double)Integer.MAX_VALUE);            

            paragraphRunoversField.setDecimalDigits((short)0);
            paragraphRunoversField.setMin((double)0);
            paragraphRunoversField.setMax((double)Integer.MAX_VALUE);

            paragraphLinesAboveField.setDecimalDigits((short)0);
            paragraphLinesAboveField.setMin((double)0);
            paragraphLinesAboveField.setMax((double)1);

            paragraphLinesBelowField.setDecimalDigits((short)0);
            paragraphLinesBelowField.setMin((double)0);
            paragraphLinesBelowField.setMax((double)1);

            updateParagraphsPageFieldValues();
            updateParagraphsPageFieldProperties();

        }

        if (pagesEnabled[HEADINGS_PAGE-1]) {

            currentHeadingLevel = 1;

            for (int i=0;i<4;i++) { headingLevelListBox.addItem((i<3)?String.valueOf(i+1):"4-10", (short)i); }
            headingLevelListBox.selectItemPos((short)(currentHeadingLevel-1), true);

            headingAlignmentListBox.addItem(L10N_left, (short)0);
            headingAlignmentListBox.addItem(L10N_center, (short)1);

            headingFirstLineField.setDecimalDigits((short)0);
            headingFirstLineField.setMin((double)0);
            headingFirstLineField.setMax((double)Integer.MAX_VALUE);

            headingRunoversField.setDecimalDigits((short)0);
            headingRunoversField.setMin((double)0);
            headingRunoversField.setMax((double)Integer.MAX_VALUE);

            headingLinesAboveField.setDecimalDigits((short)0);
            headingLinesAboveField.setMin((double)0);
            headingLinesAboveField.setMax((double)1);

            headingLinesBelowField.setDecimalDigits((short)0);
            headingLinesBelowField.setMin((double)0);
            headingLinesBelowField.setMax((double)1);
            
            updateHeadingsPageFieldValues();
            updateHeadingsPageFieldProperties();

        }

        if (pagesEnabled[LISTS_PAGE-1]) {

            currentListLevel = 1;

            listLinesAboveField.setDecimalDigits((short)0);
            listLinesAboveField.setMin((double)0);
            listLinesAboveField.setMax((double)1);
            
            listLinesBelowField.setDecimalDigits((short)0);
            listLinesBelowField.setMin((double)0);
            listLinesBelowField.setMax((double)1);

            listLinesBetweenField.setDecimalDigits((short)0);
            listLinesBetweenField.setMin((double)0);
            listLinesBetweenField.setMax((double)1);

            for (int i=0;i<10;i++) { listLevelListBox.addItem(String.valueOf(i+1), (short)i);}
            listLevelListBox.selectItemPos((short)(currentListLevel-1), true);
            listAlignmentListBox.addItem(L10N_left, (short)0);
            listAlignmentListBox.addItem(L10N_center, (short)1);

            listFirstLineField.setDecimalDigits((short)0);
            listFirstLineField.setMin((double)0);
            listFirstLineField.setMax((double)Integer.MAX_VALUE);

            listRunoversField.setDecimalDigits((short)0);
            listRunoversField.setMin((double)0);
            listRunoversField.setMax((double)Integer.MAX_VALUE);

            updateListsPageFieldValues();
            updateListsPageFieldProperties();

        }

        if (pagesEnabled[TABLES_PAGE-1]) {

            currentTableColumn = settings.stairstepTableIsEnabled()?1:0;

            tableSimpleRadioButton.setState(!settings.stairstepTableIsEnabled());
            tableStairstepRadioButton.setState(settings.stairstepTableIsEnabled());

            tableLinesAboveField.setDecimalDigits((short)0);
            tableLinesAboveField.setMin((double)0);
            tableLinesAboveField.setMax((double)1);
            tableLinesAboveField.setValue((double)settings.getLinesAbove("table"));

            tableLinesBelowField.setDecimalDigits((short)0);
            tableLinesBelowField.setMin((double)0);
            tableLinesBelowField.setMax((double)1);
            tableLinesBelowField.setValue((double)settings.getLinesBelow("table"));

            tableLinesBetweenField.setDecimalDigits((short)0);
            tableLinesBetweenField.setMin((double)0);
            tableLinesBetweenField.setMax((double)1);
            tableLinesBetweenField.setValue((double)settings.getLinesBetween("table"));

            for (int i=0;i<10;i++) { tableColumnListBox.addItem((i<9)?String.valueOf(i+1):"\u226510", (short)i); }
            tableColumnListBox.selectItemPos((short)(currentTableColumn-1), true);

            tableAlignmentListBox.addItem(L10N_left, (short)0);
            tableAlignmentListBox.addItem(L10N_center, (short)1);

            tableFirstLineField.setDecimalDigits((short)0);
            tableFirstLineField.setMin((double)0);
            tableFirstLineField.setMax((double)Integer.MAX_VALUE);

            tableRunoversField.setDecimalDigits((short)0);
            tableRunoversField.setMin((double)0);
            tableRunoversField.setMax((double)Integer.MAX_VALUE);

            tableColumnDelimiterField.setText(settings.getColumnDelimiter());

            updateTablesPageFieldValues();
            updateTablesPageFieldProperties();

        }

        if (pagesEnabled[PAGENUMBERS_PAGE-1]) {

            braillePageNumberAtListBox.addItem(L10N_top, (short)0);
            braillePageNumberAtListBox.addItem(L10N_bottom, (short)1);
            preliminaryPageNumberFormatListBox.addItem("p1,p2,p3,...", (short)0);
            preliminaryPageNumberFormatListBox.addItem("i,ii,iii,...", (short)1);
            printPageNumberAtListBox.addItem(L10N_top, (short)0);
            printPageNumberAtListBox.addItem(L10N_bottom, (short)1);

            updatePageNumbersPageFieldValues();
            updatePageNumbersPageFieldProperties();

        }

        if (pagesEnabled[LANGUAGES_PAGE-1]) {

            for (int i=0;i<languages.size();i++) {
                languagesListBox.addItem(L10N_languages.get(languages.get(i)), (short)i);
            }

            for (int i=0;i<supportedTranslationTables.size();i++) {
                translationTableListBox.addItem(L10N_translationTables.get(supportedTranslationTables.get(i)), (short)i);
            }

            languagesListBox.selectItemPos((short)languages.indexOf(settings.getMainLanguage()), true);
            selectedLanguagePos = (int)languagesListBox.getSelectedItemPos();
            updateTranslationTableListBox(settings.getMainLanguage());
            updateGradeListBox(settings.getMainLanguage());

        }

        if (pagesEnabled[TOC_PAGE-1]) {

            currentTableOfContentsLevel = 1;

            tableOfContentsCheckBox.setState((short)(settings.tableOfContentEnabled?1:0));
            tableOfContentsCheckBoxProperties.setPropertyValue("Enabled", settings.PRELIMINARY_PAGES_PRESENT);
            tableOfContentsTitleField.setText(settings.tableOfContentTitle);
            tableOfContentsLineFillField.setText(settings.getLineFillSymbol());
            
            tableOfContentsLinesBetweenField.setDecimalDigits((short)0);
            tableOfContentsLinesBetweenField.setMin((double)0);
            tableOfContentsLinesBetweenField.setMax((double)1);
            tableOfContentsLinesBetweenField.setValue((double)settings.getLinesBetween("toc"));

            for (int i=0;i<4;i++) { tableOfContentsLevelListBox.addItem((i<3)?String.valueOf(i+1):"4-10", (short)i); }
            tableOfContentsLevelListBox.selectItemPos((short)(currentTableOfContentsLevel-1), true);

            tableOfContentsFirstLineField.setDecimalDigits((short)0);
            tableOfContentsFirstLineField.setMin((double)0);
            tableOfContentsFirstLineField.setMax((double)Integer.MAX_VALUE);

            tableOfContentsRunoversField.setDecimalDigits((short)0);
            tableOfContentsRunoversField.setMin((double)0);
            tableOfContentsRunoversField.setMax((double)Integer.MAX_VALUE);

            updateTableOfContentsPageFieldValues();
            updateTableOfContentsPageFieldProperties();

        }
        
        if (pagesEnabled[SPECIAL_SYMBOLS_PAGE-1]) {

            selectedSpecialSymbolPos = 0;

            specialSymbolsListCheckBoxProperties.setPropertyValue("Enabled", settings.PRELIMINARY_PAGES_PRESENT);
            specialSymbolsListField.setText(settings.specialSymbolsListTitle);
            specialSymbolsListCheckBox.setState((short)(settings.specialSymbolsListEnabled?1:0));

            updateSpecialSymbolsListBox();
            updateSpecialSymbolsPageFieldValues();
            updateSpecialSymbolsPageFieldProperties();

        }

        if (pagesEnabled[MATH_PAGE-1]) {

            String key = null;

            for (int i=0;i<mathTypes.size();i++) {
                key = mathTypes.get(i).name();
                if (L10N_math.containsKey(key)) {
                    mathListBox.addItem(L10N_math.get(key), (short)i);
                } else {
                    mathListBox.addItem(key, (short)i);
                }
            }
            mathListBox.selectItemPos((short)mathTypes.indexOf(settings.getMath()), true);

        }

        if (pagesEnabled[EXPORT_EMBOSS_PAGE-1]) {

            if (mode==EMBOSS) {
                settings.setGenericOrSpecific(false);
                genericRadioButtonProperties.setPropertyValue("Enabled", false);
            }
            genericRadioButton.setState(settings.isGenericOrSpecific());
            specificRadioButton.setState(!settings.isGenericOrSpecific());
            mirrorAlignCheckBox.setState((short)(settings.isMirrorAlign()?1:0));
            duplexCheckBox.setState((short)(settings.isDuplex()?1:0));

            numberOfCellsPerLineField.setDecimalDigits((short)0);
            numberOfLinesPerPageField.setDecimalDigits((short)0);
            marginLeftField.setDecimalDigits((short)0);
            marginRightField.setDecimalDigits((short)0);
            marginTopField.setDecimalDigits((short)0);
            marginBottomField.setDecimalDigits((short)0);

            numberOfCellsPerLineField.setMin((double)settings.getMinWidth());
            numberOfLinesPerPageField.setMin((double)settings.getMinHeight());
            marginLeftField.setMin((double)0);
            marginRightField.setMin((double)0);
            marginTopField.setMin((double)0);
            marginBottomField.setMin((double)0);

            paperWidthUnitListBox.addItem("mm", (short)0);
            paperWidthUnitListBox.addItem("in", (short)1);
            paperWidthUnitListBox.selectItemPos((short)0, true);
            paperHeightUnitListBox.addItem("mm", (short)0);
            paperHeightUnitListBox.addItem("in", (short)1);
            paperHeightUnitListBox.selectItemPos((short)0, true);

            updateEmbosserListBox();
            updateGenericBrailleListBox();
            updateDuplexCheckBox();
            updateTableListBox();
            updatePaperSizeListBox();
            updatePaperDimensionFields();
            updateDimensionFields();
            updateMirrorAlignCheckBox();
            updateOKButton();

        }

        brailleRulesListBox.addItem("Custom", (short)0);
        brailleRulesListBox.addItem("BANA",   (short)1);
        brailleRulesListBox.selectItemPos((short)((settings.getBrailleRules()==BrailleRules.CUSTOM)?0:1), true);

        setPage((mode==SAVE_SETTINGS)?GENERAL_PAGE:EXPORT_EMBOSS_PAGE);
        windowProperties.setPropertyValue("Step", 0);
        windowProperties.setPropertyValue("Step", currentPage);
        roadmapProperties.setPropertyValue("Complete", true);
        roadmapProperties.setPropertyValue("CurrentItemID", (short)currentPage);
        updateOKButton();

    }

    /**
     * Read the dialog values set by the user (that have not been previously read).
     *
     */
    private void getDialogValues() {

        if (pagesVisited[GENERAL_PAGE-1]) {

            settings.setCreator(creatorField.getText());
            settings.transcribersNotesPageTitle = transcribersNotesPageField.getText();
            settings.transcriptionInfoEnabled = (transcriptionInfoCheckBox.getState() == (short) 1);
            settings.volumeInfoEnabled = (volumeInfoCheckBox.getState() == (short) 1);
            settings.transcribersNotesPageEnabled = (transcribersNotesPageCheckBox.getState() == (short) 1);            
            settings.preliminaryVolumeEnabled = (preliminaryVolumeCheckBox.getState() == (short) 1);
            settings.setHyphenate(hyphenateCheckBox.getState() == (short) 1);

        }

        if (pagesVisited[PARAGRAPHS_PAGE-1]) {

            settings.setFirstLineMargin("paragraph", (int)paragraphFirstLineField.getValue());
            settings.setRunoversMargin("paragraph", (int)paragraphRunoversField.getValue());
            settings.setLinesAbove("paragraph", (int)paragraphLinesAboveField.getValue());
            settings.setLinesBelow("paragraph", (int)paragraphLinesBelowField.getValue());

        }

        if (pagesVisited[HEADINGS_PAGE-1]) { saveHeadingsPageFieldValues(); }

        if (pagesVisited[LISTS_PAGE-1]) { saveListsPageFieldValues(); }

        if (pagesVisited[TABLES_PAGE-1]) {

            settings.setLinesAbove("table", (int)tableLinesAboveField.getValue());
            settings.setLinesBelow("table", (int)tableLinesBelowField.getValue());
            settings.setLinesBetween("table", (int)tableLinesBetweenField.getValue());
            saveTablesPageFieldValues();

        }

        if (pagesVisited[PAGENUMBERS_PAGE-1]) {

            settings.setPreliminaryPageFormat(((preliminaryPageNumberFormatListBox.getSelectedItemPos() == (short)0)?"p":"roman"));
            settings.setContinuePages(continuePagesCheckBox.getState() == (short) 1);
            settings.setPageSeparatorNumber(pageSeparatorNumberCheckBox.getState() == (short) 1);
            settings.setIgnoreEmptyPages(ignoreEmptyPagesCheckBox.getState() == (short) 1);
            settings.setMergeUnnumberedPages(mergeUnnumberedPagesCheckBox.getState() == (short) 1);
            settings.setPageNumberAtTopOnSeparateLine(numbersAtTopOnSepLineCheckBox.getState() == (short) 1);
            settings.setPageNumberAtBottomOnSeparateLine(numbersAtBottomOnSepLineCheckBox.getState() == (short) 1);
            settings.setHardPageBreaks(hardPageBreaksCheckBox.getState() == (short) 1);

        }

        if (pagesVisited[LANGUAGES_PAGE-1]) {}

        if (pagesVisited[TOC_PAGE-1]) {

            settings.tableOfContentTitle = tableOfContentsTitleField.getText();
            settings.setLinesBetween("toc", (int)tableOfContentsLinesBetweenField.getValue());
            saveTableOfContentsPageFieldValues();

        }

        if (pagesVisited[SPECIAL_SYMBOLS_PAGE-1]){

            settings.specialSymbolsListTitle = specialSymbolsListField.getText();
            settings.specialSymbolsListEnabled = (specialSymbolsListCheckBox.getState() == (short) 1);
            saveSpecialSymbolsPageFieldValues();

        }

        if (pagesVisited[MATH_PAGE-1]) {

            settings.setMath(mathTypes.get(mathListBox.getSelectedItemPos()));

        }

        if (pagesVisited[EXPORT_EMBOSS_PAGE-1]) {

            updateTable();
            updateMirrorAlign();

        }
    }

    /**
     * Update the label and the state of the OK button (enabled or disabled).
     * The behaviour of the OK button varies with the mode ({@link #SAVE_SETTINGS}, {@link #EXPORT} or {@link #EMBOSS}).
     *
     */
    private void updateOKButton() throws com.sun.star.uno.Exception {

        okButtonProperties.setPropertyValue("Enabled", ( settings.isGenericOrSpecific() && settings.getGenericBraille()!=BrailleFileType.NONE)
                                                    || (!settings.isGenericOrSpecific() && settings.getPaperSize()!=PaperSize.UNDEFINED)
                                                    || (mode==SAVE_SETTINGS));
        switch (mode) {
            case SAVE_SETTINGS:
                okButton.setLabel(L10N_okButton4);
                break;
            case EXPORT:
                okButton.setLabel(settings.isGenericOrSpecific()?L10N_okButton3:L10N_okButton2);
                break;
            case EMBOSS:
                okButton.setLabel(L10N_okButton1);
                break;
            default:

        }
    }

    /**
     * Update the states of the 'Back' and 'Next' buttons (enabled or disabled) when another tab is showed.
     *
     */
    private void updateBackNextButtons() throws com.sun.star.uno.Exception {

        boolean backButtonEnabled = false;
        boolean nextButtonEnabled = false;

        for (int i=currentPage-2;i>=0;i--) {
            if (pagesEnabled[i]) {
                backButtonEnabled = true;
                break;
            }
        }
        for (int i=currentPage;i<pagesEnabled.length;i++) {
            if (pagesEnabled[i]) {
                nextButtonEnabled = true;
                break;
            }
        }

        backButtonProperties.setPropertyValue("Enabled", backButtonEnabled);
        nextButtonProperties.setPropertyValue("Enabled", nextButtonEnabled);

    }

    /**
     * Update the states of the fields (enabled or disabled) on the 'General Settings' tab.
     *
     */
    private void updateGeneralPageFieldProperties() throws com.sun.star.uno.Exception {

        creatorFieldProperties.setPropertyValue("Enabled", settings.transcriptionInfoEnabled);
        transcribersNotesPageFieldProperties.setPropertyValue("Enabled", settings.transcribersNotesPageEnabled);

    }

    private void updateParagraphsPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean centered = settings.getCentered("paragraph");
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        paragraphFirstLineFieldProperties.setPropertyValue("Enabled", !centered && !bana);
        paragraphRunoversFieldProperties.setPropertyValue("Enabled", !centered && !bana);
        paragraphAlignmentListBoxProperties.setPropertyValue("Enabled", !bana);
        paragraphLinesAboveProperties.setPropertyValue("Enabled", !bana);
        paragraphLinesBelowProperties.setPropertyValue("Enabled", !bana);

    }

    private void updateHeadingsPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean centered = settings.getCentered("heading" + currentHeadingLevel);
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        headingFirstLineFieldProperties.setPropertyValue("Enabled", !centered && !bana);
        headingRunoversFieldProperties.setPropertyValue("Enabled", !centered && !bana);
        headingAlignmentListBoxProperties.setPropertyValue("Enabled", !bana);
        headingLinesAboveProperties.setPropertyValue("Enabled", !bana);
        headingLinesBelowProperties.setPropertyValue("Enabled", !bana);

    }

    private void updateListsPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean centered = settings.getCentered("list" + currentListLevel);
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        listFirstLineFieldProperties.setPropertyValue("Enabled", !centered && !bana);
        listRunoversFieldProperties.setPropertyValue("Enabled", !centered && !bana);
        listAlignmentListBoxProperties.setPropertyValue("Enabled", !bana);
        listLinesAboveProperties.setPropertyValue("Enabled", !bana);
        listLinesBelowProperties.setPropertyValue("Enabled", !bana);
        listLinesBetweenProperties.setPropertyValue("Enabled", !bana);


    }

    private void updateTablesPageFieldProperties() throws com.sun.star.uno.Exception {

        tableColumnListBoxProperties.setPropertyValue("Enabled", settings.stairstepTableIsEnabled());
        tableColumnDelimiterFieldProperties.setPropertyValue("Enabled", !settings.stairstepTableIsEnabled());
        tableColumnDelimiterButtonProperties.setPropertyValue("Enabled", !settings.stairstepTableIsEnabled());
        
        boolean centered = settings.getCentered("table" + ((currentTableColumn==0)?"":currentTableColumn));
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        tableFirstLineFieldProperties.setPropertyValue("Enabled", !centered && !bana);
        tableRunoversFieldProperties.setPropertyValue("Enabled", !centered &&!bana);
        tableAlignmentListBoxProperties.setPropertyValue("Enabled", !bana);
        tableLinesAboveProperties.setPropertyValue("Enabled", !bana);
        tableLinesBelowProperties.setPropertyValue("Enabled", !bana);
        tableLinesBetweenProperties.setPropertyValue("Enabled", !bana);
        tableSimpleRadioButtonProperties.setPropertyValue("Enabled", !bana);

    }

    private void updatePageNumbersPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        braillePageNumbersCheckBoxProperties.setPropertyValue("Enabled", !bana);
        braillePageNumberAtListBoxProperties.setPropertyValue("Enabled", !bana && settings.getBraillePageNumbers());
        preliminaryPageNumberFormatListBoxProperties.setPropertyValue("Enabled", !bana && settings.getBraillePageNumbers());
        printPageNumbersCheckBoxProperties.setPropertyValue("Enabled", !bana);
        printPageNumberAtListBoxProperties.setPropertyValue("Enabled", !bana && settings.getPrintPageNumbers());
        printPageNumberRangeCheckBoxProperties.setPropertyValue("Enabled", !bana && settings.getPrintPageNumbers());
        continuePagesCheckBoxProperties.setPropertyValue("Enabled", !bana && settings.getPrintPageNumbers());
        pageSeparatorCheckBoxProperties.setPropertyValue("Enabled", !bana);
        pageSeparatorNumberCheckBoxProperties.setPropertyValue("Enabled", !bana && settings.getPageSeparator());
        ignoreEmptyPagesCheckBoxProperties.setPropertyValue("Enabled", !bana);
        mergeUnnumberedPagesCheckBoxProperties.setPropertyValue("Enabled", !bana);
        numbersAtTopOnSepLineCheckBoxProperties.setPropertyValue("Enabled", !bana
                                                                            && ((settings.getBraillePageNumbers() && settings.getBraillePageNumberAt().equals("top"))
                                                                             || (settings.getPrintPageNumbers()   && settings.getPrintPageNumberAt().equals("top")))
                                                                            && !(settings.getPrintPageNumberAt().equals("top")
                                                                              && settings.getPrintPageNumbers()
                                                                              && settings.getPrintPageNumberRange()));
        numbersAtBottomOnSepLineCheckBoxProperties.setPropertyValue("Enabled", !bana
                                                                            && ((settings.getBraillePageNumbers() && settings.getBraillePageNumberAt().equals("bottom"))
                                                                             || (settings.getPrintPageNumbers()   && settings.getPrintPageNumberAt().equals("bottom"))));

    }

    private void updateTableOfContentsPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean enabled = settings.tableOfContentEnabled;
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        tableOfContentsTitleFieldProperties.setPropertyValue("Enabled", enabled);
        tableOfContentsLevelListBoxProperties.setPropertyValue("Enabled", enabled);
        tableOfContentsLineFillFieldProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsLineFillButtonProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsLinesBetweenFieldProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsFirstLineFieldProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsRunoversFieldProperties.setPropertyValue("Enabled", enabled && !bana);

    }

    private void updateSpecialSymbolsPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean enabled = settings.specialSymbolsListEnabled;

        specialSymbolsListFieldProperties.setPropertyValue("Enabled", enabled);
        specialSymbolsListBoxProperties.setPropertyValue("Enabled", enabled);
        specialSymbolsSymbolFieldProperties.setPropertyValue("Enabled", enabled
                                                             && specialSymbols.get(selectedSpecialSymbolPos).getType()==SpecialSymbolType.OTHER);
        specialSymbolsSymbolButtonProperties.setPropertyValue("Enabled", enabled
                                                             && specialSymbols.get(selectedSpecialSymbolPos).getType()==SpecialSymbolType.OTHER);
        specialSymbolsDescriptionFieldProperties.setPropertyValue("Enabled", enabled);
        specialSymbolsMode0RadioButtonProperties.setPropertyValue("Enabled", enabled);
        specialSymbolsMode1RadioButtonProperties.setPropertyValue("Enabled", enabled
                                                             && specialSymbols.get(selectedSpecialSymbolPos).getType()!=SpecialSymbolType.OTHER);
        specialSymbolsMode2RadioButtonProperties.setPropertyValue("Enabled", enabled);
        specialSymbolsMode3RadioButtonProperties.setPropertyValue("Enabled", enabled);
        specialSymbolsAddButtonProperties.setPropertyValue("Enabled", enabled);
        specialSymbolsRemoveButtonProperties.setPropertyValue("Enabled", enabled
                                                             && specialSymbols.get(selectedSpecialSymbolPos).getType()==SpecialSymbolType.OTHER);
        specialSymbolsMoveUpButtonProperties.setPropertyValue("Enabled", enabled
                                                             && selectedSpecialSymbolPos>0);
        specialSymbolsMoveDownButtonProperties.setPropertyValue("Enabled", enabled
                                                             && selectedSpecialSymbolPos<specialSymbols.size()-1);

    }

    private void updateParagraphsPageFieldValues() {

        paragraphAlignmentListBox.selectItemPos((short)(settings.getCentered("paragraph")?1:0), true);
        paragraphFirstLineField.setValue((double)settings.getFirstLineMargin("paragraph"));
        paragraphRunoversField.setValue((double)settings.getRunoversMargin("paragraph"));
        paragraphLinesAboveField.setValue((double)settings.getLinesAbove("paragraph"));
        paragraphLinesBelowField.setValue((double)settings.getLinesBelow("paragraph"));

    }

    private void updateHeadingsPageFieldValues() {

        headingAlignmentListBox.removeActionListener(this);
        headingAlignmentListBox.selectItemPos((short)(settings.getCentered("heading" + currentHeadingLevel)?1:0), true);
        headingAlignmentListBox.addActionListener(this);
        headingFirstLineField.setValue(settings.getFirstLineMargin("heading" + currentHeadingLevel));
        headingRunoversField.setValue(settings.getRunoversMargin("heading" + currentHeadingLevel));
        headingLinesAboveField.setValue(settings.getLinesAbove("heading" + currentHeadingLevel));
        headingLinesBelowField.setValue(settings.getLinesBelow("heading" + currentHeadingLevel));

    }

    private void saveHeadingsPageFieldValues() {

        settings.setFirstLineMargin("heading" + currentHeadingLevel, (int)headingFirstLineField.getValue());
        settings.setRunoversMargin("heading" + currentHeadingLevel, (int)headingRunoversField.getValue());
        settings.setLinesAbove("heading" + currentHeadingLevel, (int)headingLinesAboveField.getValue());
        settings.setLinesBelow("heading" + currentHeadingLevel, (int)headingLinesBelowField.getValue());

    }

    private void updateListsPageFieldValues() {

        listAlignmentListBox.removeActionListener(this);
        listAlignmentListBox.selectItemPos((short)(settings.getCentered("list" + currentListLevel)?1:0), true);
        listAlignmentListBox.addActionListener(this);
        listFirstLineField.setValue(settings.getFirstLineMargin("list" + currentListLevel));
        listRunoversField.setValue(settings.getRunoversMargin("list" + currentListLevel));
        listLinesAboveField.setValue((double)settings.getLinesAbove("list" + currentListLevel));
        listLinesBelowField.setValue((double)settings.getLinesBelow("list" + currentListLevel));
        listLinesBetweenField.setValue((double)settings.getLinesBetween("list" + currentListLevel));
        listPrefixField.setText(settings.getListPrefix(currentListLevel));

    }

    private void saveListsPageFieldValues() {

        settings.setFirstLineMargin("list" + currentListLevel, (int)listFirstLineField.getValue());
        settings.setRunoversMargin("list" + currentListLevel, (int)listRunoversField.getValue());
        settings.setLinesAbove("list" + currentListLevel, (int)listLinesAboveField.getValue());
        settings.setLinesBelow("list" + currentListLevel, (int)listLinesBelowField.getValue());
        settings.setLinesBetween("list" + currentListLevel, (int)listLinesBetweenField.getValue());

    }

    private void updateTablesPageFieldValues() {

        tableAlignmentListBox.removeActionListener(this);
        if (currentTableColumn>0) {
            tableAlignmentListBox.selectItemPos((short)(settings.getCentered("table" + currentTableColumn)?1:0), true);
            tableFirstLineField.setValue(settings.getFirstLineMargin("table" + currentTableColumn));
            tableRunoversField.setValue(settings.getRunoversMargin("table" + currentTableColumn));
            tableColumnListBox.selectItemPos((short)(currentTableColumn-1), true);
        } else {
            tableAlignmentListBox.selectItemPos((short)(settings.getCentered("table")?1:0), true);
            tableFirstLineField.setValue(settings.getFirstLineMargin("table"));
            tableRunoversField.setValue(settings.getRunoversMargin("table"));
        }
        tableAlignmentListBox.addActionListener(this);

    }

    private void saveTablesPageFieldValues() {

        if (currentTableColumn>0) {
            settings.setFirstLineMargin("table" + currentTableColumn, (int)tableFirstLineField.getValue());
            settings.setRunoversMargin("table" + currentTableColumn, (int)tableRunoversField.getValue());
        } else {
            settings.setFirstLineMargin("table", (int)tableFirstLineField.getValue());
            settings.setRunoversMargin("table", (int)tableRunoversField.getValue());
        }
    }

    private void updatePageNumbersPageFieldValues() {

        braillePageNumbersCheckBox.setState((short)(settings.getBraillePageNumbers()?1:0));
        braillePageNumberAtListBox.selectItemPos((short)((settings.getBraillePageNumberAt().equals("top"))?0:1), true);
        preliminaryPageNumberFormatListBox.selectItemPos((short)((settings.getPreliminaryPageFormat().equals("p"))?0:1), true);
        printPageNumbersCheckBox.setState((short)(settings.getPrintPageNumbers()?1:0));
        printPageNumberAtListBox .selectItemPos((short)((settings.getPrintPageNumberAt().equals("top"))?0:1), true);
        printPageNumberRangeCheckBox.setState((short)(settings.getPrintPageNumberRange()?1:0));
        continuePagesCheckBox.setState((short)(settings.getContinuePages()?1:0));
        pageSeparatorCheckBox.setState((short)(settings.getPageSeparator()?1:0));
        pageSeparatorNumberCheckBox.setState((short)(settings.getPageSeparatorNumber()?1:0));
        ignoreEmptyPagesCheckBox.setState((short)(settings.getIgnoreEmptyPages()?1:0));
        mergeUnnumberedPagesCheckBox.setState((short)(settings.getMergeUnnumberedPages()?1:0));
        numbersAtTopOnSepLineCheckBox.setState((short)(settings.getPageNumberAtTopOnSeparateLine()?1:0));
        numbersAtBottomOnSepLineCheckBox.setState((short)(settings.getPageNumberAtBottomOnSeparateLine()?1:0));
        hardPageBreaksCheckBox.setState((short)(settings.getHardPageBreaks()?1:0));

    }

    private void updateTableOfContentsPageFieldValues() {

        tableOfContentsFirstLineField.setValue(settings.getFirstLineMargin("toc" + currentTableOfContentsLevel));
        tableOfContentsRunoversField.setValue(settings.getRunoversMargin("toc" + currentTableOfContentsLevel));

    }

    private void saveTableOfContentsPageFieldValues() {

        settings.setFirstLineMargin("toc" + currentTableOfContentsLevel, (int)tableOfContentsFirstLineField.getValue());
        settings.setRunoversMargin("toc" + currentTableOfContentsLevel, (int)tableOfContentsRunoversField.getValue());

    }

    private void updateSpecialSymbolsListBox() {

        specialSymbolsListBox.removeItemListener(this);

        specialSymbols = settings.getSpecialSymbolsList();
        specialSymbolsListBox.removeItems((short)0, Short.MAX_VALUE);
        for (int i=0;i<specialSymbols.size();i++) {
            specialSymbolsListBox.addItem(specialSymbols.get(i).getSymbol(), (short)i);
        }
        specialSymbolsListBox.selectItemPos((short)(selectedSpecialSymbolPos), true);

        specialSymbolsListBox.addItemListener(this);

    }

    private void updateSpecialSymbolsPageFieldValues() {

        SpecialSymbol selectedSpecialSymbol = specialSymbols.get(selectedSpecialSymbolPos);

        specialSymbolsSymbolField.setText(selectedSpecialSymbol.getSymbol());
        specialSymbolsDescriptionField.setText(selectedSpecialSymbol.getDescription());

        specialSymbolsMode0RadioButton.removeItemListener(this);
        specialSymbolsMode1RadioButton.removeItemListener(this);
        specialSymbolsMode2RadioButton.removeItemListener(this);
        specialSymbolsMode3RadioButton.removeItemListener(this);

        specialSymbolsMode0RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.NEVER);
        specialSymbolsMode1RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.IF_PRESENT_IN_VOLUME);
        specialSymbolsMode2RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.FIRST_VOLUME);
        specialSymbolsMode3RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.ALWAYS);

        specialSymbolsMode0RadioButton.addItemListener(this);
        specialSymbolsMode1RadioButton.addItemListener(this);
        specialSymbolsMode2RadioButton.addItemListener(this);
        specialSymbolsMode3RadioButton.addItemListener(this);

    }

    private void saveSpecialSymbolsPageFieldValues() {

        specialSymbols.get(selectedSpecialSymbolPos).setDescription(specialSymbolsDescriptionField.getText());

    }

    /**
     * Select the correct item in the 'Main translation table' listbox on the 'General Settings' tab.
     *
     */
    private void updateMainTranslationTableListBox() {

        mainTranslationTableListBox.removeItemListener(this);
        mainTranslationTableListBox.selectItemPos((short)supportedTranslationTables.indexOf(settings.getTranslationTable(settings.getMainLanguage())),true);
        mainTranslationTableListBox.addItemListener(this);

    }

    /**
     * Update the list of available grades in the 'Main grade' listbox on the 'General Settings' tab and select the correct item.
     *
     */
    private void updateMainGradeListBox() {

        mainGradeListBox.removeItemListener(this);
        mainGradeListBox.removeItems((short)0, Short.MAX_VALUE);
        ArrayList<Integer> supportedGrades = settings.getSupportedGrades(settings.getMainLanguage());
        for (int i=0;i<supportedGrades.size();i++) {
            mainGradeListBox.addItem(L10N_grades.get(supportedGrades.get(i)), (short)i);
        }
        mainGradeListBox.selectItemPos((short)supportedGrades.indexOf(settings.getGrade(settings.getMainLanguage())), true);
        mainGradeListBox.addItemListener(this);

    }

    /**
     * Select the correct item in the 'Translation table' listbox on the 'Language Settings' tab.
     *
     */
    private void updateTranslationTableListBox(String language) {

        translationTableListBox.removeItemListener(this);
        translationTableListBox.selectItemPos((short)supportedTranslationTables.indexOf(settings.getTranslationTable(language)),true);
        translationTableListBox.addItemListener(this);

    }

    /**
     * Update the list of available grades in the 'Grade' listbox on the 'Language Settings' tab and select the correct item.
     *
     */
    private void updateGradeListBox(String language) {

        gradeListBox.removeItemListener(this);
        gradeListBox.removeItems((short)0, Short.MAX_VALUE);
        ArrayList<Integer> supportedGrades = settings.getSupportedGrades(language);
        for (int i=0;i<supportedGrades.size();i++) {
            gradeListBox.addItem(L10N_grades.get(supportedGrades.get(i)), (short)i);
        }
        gradeListBox.selectItemPos((short)supportedGrades.indexOf(settings.getGrade(language)), true);
        gradeListBox.addItemListener(this);

    }

    /**
     * Update the 'Generic' and 'Specific embosser' radioboxes on the 'Emboss/Export Settings' tab.
     *
     */
    private void updateGenericOrSpecific() {

        specificRadioButton.removeItemListener(this);
        genericRadioButton.removeItemListener(this);

        if (!settings.isGenericOrSpecific()) {
            specificRadioButton.setState(false);
            settings.setGenericOrSpecific(true);
        } else {
            genericRadioButton.setState(false);
            settings.setGenericOrSpecific(false);
        }

        specificRadioButton.addItemListener(this);
        genericRadioButton.addItemListener(this);
    }

    /**
     * Update the {@link be.docarch.odt2braille.Settings#genericOrSpecific} setting.
     *
     */
    private void updateGenericBraille() {
        settings.setGenericBraille(genericBrailleTypes.get(genericBrailleListBox.getSelectedItemPos()));
    }

    /**
     * Update the list of available generic braille files in the 'Generic braille' listbox
     * on the 'Emboss/Export Settings' tab and select the correct item.
     *
     */
    private void updateGenericBrailleListBox() throws com.sun.star.uno.Exception {

        String key = null;

        genericBrailleListBox.removeItemListener(this);

            genericBrailleListBox.removeItems((short)0, (short)numberOfGenericBrailleTypes);
            genericBrailleTypes = settings.getSupportedBrailleFileTypes();
            for (int i=0;i<genericBrailleTypes.size();i++) {
                key = genericBrailleTypes.get(i).name();
                if (L10N_genericBraille.containsKey(key)) {
                    genericBrailleListBox.addItem(L10N_genericBraille.get(key), (short)i);
                } else {
                    genericBrailleListBox.addItem(key, (short)i);
                }
            }
            if (!genericBrailleTypes.contains(settings.getGenericBraille())) { // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
                settings.setGenericBraille(genericBrailleTypes.get(0));
            }
            genericBrailleListBox.selectItemPos((short)genericBrailleTypes.indexOf(settings.getGenericBraille()), true);
            genericBrailleListBoxProperties.setPropertyValue("Enabled", settings.isGenericOrSpecific());

        genericBrailleListBox.addItemListener(this);

    }

    /**
     * Update the {@link be.docarch.odt2braille.Settings#embosser} setting.
     *
     */
    private void updateEmbosser() {
        settings.setEmbosser(embosserTypes.get(embosserListBox.getSelectedItemPos()));
    }

    /**
     * Update the list of available embosser in the 'Embosser' listbox on the 'Export/Emboss Settings' tab and select the correct item.
     *
     */
    private void updateEmbosserListBox() throws com.sun.star.uno.Exception {

        String key = null;

        embosserListBox.removeItemListener(this);

            embosserListBox.removeItems((short)0, (short)numberOfEmbosserTypes);
            embosserTypes = settings.getSupportedEmbosserTypes();
            for (int i=0;i<embosserTypes.size();i++) {
                key = embosserTypes.get(i).name();
                if (L10N_embosser.containsKey(key)) {
                    embosserListBox.addItem(L10N_embosser.get(key), (short)i);
                } else {
                    embosserListBox.addItem(key, (short)i);
                }
            }
            if (!embosserTypes.contains(settings.getEmbosser())) { // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
                settings.setEmbosser(embosserTypes.get(0));
            }
            embosserListBox.selectItemPos((short)embosserTypes.indexOf(settings.getEmbosser()), true);
            embosserListBoxProperties.setPropertyValue("Enabled", !settings.isGenericOrSpecific());

        embosserListBox.addItemListener(this);

    }

    /**
     * Update the {@link be.docarch.odt2braille.Settings#table} setting.
     *
     */
    private void updateTable() {
        settings.setTable(tableTypes.get(tableListBox.getSelectedItemPos()));
    }

    /**
     * Update the list of available character sets in the 'Character set' listbox on the 'Export/Emboss Settings' tab and select the correct item.
     *
     */
    private void updateTableListBox() throws com.sun.star.uno.Exception {

        String key;

        tableListBox.removeItemListener(this);

            tableListBox.removeItems((short)0, (short)numberOfTableTypes);
            tableTypes = settings.getSupportedTableTypes();
            for (int i=0;i<tableTypes.size();i++) {
                key = tableTypes.get(i).name();
                if (L10N_table.containsKey(key)) {
                    tableListBox.addItem(L10N_table.get(key), (short)i);
                } else {
                    tableListBox.addItem(key, (short)i);
                }
            }
            if (!tableTypes.contains(settings.getTable())) { // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
                settings.setTable(tableTypes.get(0));
            }

            tableListBox.selectItemPos((short)tableTypes.indexOf(settings.getTable()), true);
            tableListBoxProperties.setPropertyValue("Enabled", settings.getEmbosser()!=EmbosserType.NONE);

        tableListBox.addItemListener(this);

    }

    /**
     * Update the {@link be.docarch.odt2braille.Settings#paperSize} setting.
     *
     */
    private void updatePaperSize() {
        settings.setPaperSize(paperSizes.get(paperSizeListBox.getSelectedItemPos()));
    }

    /**
     * Update the list of available paper sizes in the 'Paper size' listbox on the 'Export/Emboss Settings' tab and select the correct item.
     *
     */
    private void updatePaperSizeListBox() throws com.sun.star.uno.Exception {

        String key;

        paperSizeListBox.removeItemListener(this);

            paperSizeListBox.removeItems((short)0, (short)numberOfPaperSizes);
            paperSizes = settings.getSupportedPaperSizes();
            for (int i=0;i<paperSizes.size();i++) {
                key = paperSizes.get(i).name();
                if (L10N_paperSize.containsKey(key)) {
                    paperSizeListBox.addItem(L10N_paperSize.get(key), (short)i);
                } else {
                    paperSizeListBox.addItem(key, (short)i);
                }
            }
            if (!paperSizes.contains(settings.getPaperSize())) { // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
                settings.setPaperSize(paperSizes.get(0));
            }
            paperSizeListBox.selectItemPos((short)paperSizes.indexOf(settings.getPaperSize()), true);
            paperSizeListBoxProperties.setPropertyValue("Enabled", settings.getEmbosser()!=EmbosserType.NONE);

        paperSizeListBox.addItemListener(this);

    }

    private void updatePaperDimensions() {

        settings.setCustomPaperSize(
                paperWidthField.getValue()  * ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d),
                paperHeightField.getValue() * ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));

    }
    
    private void updatePaperDimensionFields() throws com.sun.star.uno.Exception {

        paperWidthTextComponent.removeTextListener(this);
        paperHeightTextComponent.removeTextListener(this);

            paperWidthFieldProperties.setPropertyValue("Enabled", settings.getPaperSize() == PaperSize.CUSTOM);
            paperHeightFieldProperties.setPropertyValue("Enabled", settings.getPaperSize() == PaperSize.CUSTOM);
            paperWidthUnitListBoxProperties.setPropertyValue("Enabled", settings.getPaperSize() != PaperSize.UNDEFINED);
            paperHeightUnitListBoxProperties.setPropertyValue("Enabled", settings.getPaperSize() != PaperSize.UNDEFINED);

            if (settings.getPaperSize() == PaperSize.UNDEFINED) {
                
                paperWidthField.setDecimalDigits((short)0);
                paperHeightField.setDecimalDigits((short)0);

                paperWidthField.setMin(0d);
                paperHeightField.setMin(0d);
                paperWidthField.setMax(0d);
                paperHeightField.setMax(0d);
                paperWidthField.setValue(0d);
                paperHeightField.setValue(0d);
                
            } else {

                paperWidthField.setDecimalDigits((short) ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? 2 : 0));
                paperHeightField.setDecimalDigits((short)((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? 2 : 0));

                paperWidthField.setMin(settings.getMinPaperWidth()   / ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d));
                paperHeightField.setMin(settings.getMinPaperHeight() / ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));
                paperWidthField.setMax(settings.getMaxPaperWidth()   / ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d));
                paperHeightField.setMax(settings.getMaxPaperHeight() / ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));
                paperWidthField.setValue(settings.getPaperWidth()    / ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d));
                paperHeightField.setValue(settings.getPaperHeight()  / ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));

            }

        paperWidthTextComponent.addTextListener(this);
        paperHeightTextComponent.addTextListener(this);

    }

    /**
     * Update the {@link be.docarch.odt2braille.Settings#duplex} setting.
     *
     */
    private void updateDuplex() {
        settings.setDuplex((duplexCheckBox.getState()==(short)1));
    }

    /**
     * Update the 'Recto-verso' checkbox on the 'Export/Emboss Settings' tab.
     *
     */
    private void updateDuplexCheckBox() throws com.sun.star.uno.Exception {

        if (!settings.duplexIsSupported()) {
            settings.setDuplex(false); // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
            duplexCheckBox.setState((short)0);
            duplexCheckBoxProperties.setPropertyValue("Enabled", false);
        } else {
            duplexCheckBox.setState((short)(settings.isDuplex()?1:0));
            duplexCheckBoxProperties.setPropertyValue("Enabled", true);
        }      
    }

    /**
     * Update the {@link be.docarch.odt2braille.Settings#mirrorAlign} setting.
     *
     */
    private void updateMirrorAlign() {
        settings.setMirrorAlign((mirrorAlignCheckBox.getState() == (short)1));
    }

    /**
     * Update the 'Mirror margins' checkbox on the 'Export/Emboss Settings' tab.
     *
     */
    private void updateMirrorAlignCheckBox() throws com.sun.star.uno.Exception {

        if (!settings.mirrorAlignIsSupported()) {
            settings.setMirrorAlign(false); // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
            mirrorAlignCheckBox.setState((short)0);
            mirrorAlignCheckBoxProperties.setPropertyValue("Enabled", false);
        } else {
            mirrorAlignCheckBox.setState((short)(settings.isMirrorAlign()?1:0));
            mirrorAlignCheckBoxProperties.setPropertyValue("Enabled", true);
        }
    }

    /**
     * Update the 'Cells per line', 'Lines per page' and 'Margin' field values on the 'Export/Emboss' tab.
     * This method is called when the respective settings have possibly changed because the user changed one of these values.
     *
     */
    private void updateDimensionFieldValues() {

        numberOfCellsPerLineTextComponent.removeTextListener(this);
        numberOfLinesPerPageTextComponent.removeTextListener(this);
        marginLeftTextComponent.removeTextListener(this);
        marginRightTextComponent.removeTextListener(this);
        marginTopTextComponent.removeTextListener(this);
        marginBottomTextComponent.removeTextListener(this);

            numberOfCellsPerLineField.setValue((double)settings.getNumberOfCellsPerLine());
            numberOfLinesPerPageField.setValue((double)settings.getNumberOfLinesPerPage());
            marginLeftField.setValue((double)settings.getMarginLeft());
            marginRightField.setValue((double)settings.getMarginRight());
            marginTopField.setValue((double)settings.getMarginTop());
            marginBottomField.setValue((double)settings.getMarginBottom());

        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginLeftTextComponent.addTextListener(this);
        marginRightTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);

    }

    /**
     * Update the maximum, minimum and current values and the states (enabled or dissabled) of the
     * 'Cells per line', 'Lines per page' and 'Margin' field values on the 'Export/Emboss' tab.
     * This method is called when the respective braille settings have possibly changed because another paper size was selected.
     *
     */
    private void updateDimensionFields() throws com.sun.star.uno.Exception {

        numberOfCellsPerLineTextComponent.removeTextListener(this);
        numberOfLinesPerPageTextComponent.removeTextListener(this);
        marginLeftTextComponent.removeTextListener(this);
        marginRightTextComponent.removeTextListener(this);
        marginTopTextComponent.removeTextListener(this);
        marginBottomTextComponent.removeTextListener(this);

            marginLeftFieldProperties.setPropertyValue("Enabled", settings.alignmentIsSupported());
            marginRightFieldProperties.setPropertyValue("Enabled", settings.alignmentIsSupported());
            marginTopFieldProperties.setPropertyValue("Enabled", settings.alignmentIsSupported());
            marginBottomFieldProperties.setPropertyValue("Enabled", settings.alignmentIsSupported());

            if (settings.alignmentIsSupported()) {

                settings.setMarginLeft(settings.getMarginLeft()); // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
                settings.setMarginTop(settings.getMarginTop());

                marginLeftField.setMax((double)(settings.getMaxWidth()-settings.getMinWidth()));
                marginRightField.setMax((double)(settings.getMaxWidth()-settings.getMinWidth()));
                marginTopField.setMax((double)(settings.getMaxHeight()-settings.getMinHeight()));
                marginBottomField.setMax((double)(settings.getMaxHeight()-settings.getMinHeight()));

            } else {

                settings.setMarginLeft(0); // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
                settings.setMarginRight(0);
                settings.setMarginTop(0);
                settings.setMarginBottom(0);

                marginLeftField.setMax((double)0);
                marginRightField.setMax((double)0);
                marginTopField.setMax((double)0);
                marginBottomField.setMax((double)0);

            }

            settings.setCellsPerLine(settings.getNumberOfCellsPerLine()); // this has to be done in Settings instead !! -> Settings should be responsible for the correctness of its own members.
            settings.setLinesPerPage(settings.getNumberOfLinesPerPage());

            numberOfCellsPerLineField.setMax((double)settings.getMaxWidth());
            numberOfLinesPerPageField.setMax((double)settings.getMaxHeight());

            numberOfCellsPerLineField.setValue((double)settings.getNumberOfCellsPerLine());
            numberOfLinesPerPageField.setValue((double)settings.getNumberOfLinesPerPage());
            marginLeftField.setValue((double)settings.getMarginLeft());
            marginRightField.setValue((double)settings.getMarginRight());
            marginTopField.setValue((double)settings.getMarginTop());
            marginBottomField.setValue((double)settings.getMarginBottom());

        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginLeftTextComponent.addTextListener(this);
        marginRightTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);

    }

    private void setPage(int page)
                  throws com.sun.star.uno.Exception {
        
        currentPage = page;
        pagesVisited[currentPage-1] = true;
        updateBackNextButtons();
        
    }

    /**
     * Is called when a listbox, checkbox or radio button is changed or when the user selects a tab in the roadmap menu.
     * In the latter case, the correct dialog tab is activated.
     * All relevant braille settings, dialog fields and buttons are updated.
     *
     * @param itemEvent
     */
    public void itemStateChanged(ItemEvent itemEvent) {

        Object source = itemEvent.Source;

        try {

            if (source.equals(roadMapBroadcaster)) {

                int newPage = itemEvent.ItemId;
                if (newPage != currentPage){

                    if (newPage==GENERAL_PAGE) {
                        updateMainTranslationTableListBox();
                        updateMainGradeListBox();
                    } else if (newPage==LANGUAGES_PAGE) {
                        updateTranslationTableListBox(settings.getMainLanguage());
                        updateGradeListBox(settings.getMainLanguage());
                    }

                    windowProperties.setPropertyValue("Step", new Integer(newPage));
                    setPage(newPage);

                }

            } else if (source.equals(brailleRulesListBox)) {

                settings.setBrailleRules(BrailleRules.values()[brailleRulesListBox.getSelectedItemPos()]);

                if (settings.getBrailleRules()==BrailleRules.BANA) {

                    updateParagraphsPageFieldValues();
                    updateHeadingsPageFieldValues();
                    updateListsPageFieldValues();
                    if (currentTableColumn==0) {
                        settings.setFirstLineMargin("table", (int)tableFirstLineField.getValue());
                        settings.setRunoversMargin("table", (int)tableRunoversField.getValue());
                        currentTableColumn = 1;
                    }
                    tableSimpleRadioButton.setState(!settings.stairstepTableIsEnabled());
                    tableStairstepRadioButton.setState(settings.stairstepTableIsEnabled());
                    tableLinesAboveField.setValue((double)settings.getLinesAbove("table"));
                    tableLinesBelowField.setValue((double)settings.getLinesBelow("table"));
                    tableLinesBetweenField.setValue((double)settings.getLinesBetween("table"));
                    updateTablesPageFieldValues();
                    updatePageNumbersPageFieldValues();
                    tableOfContentsLineFillField.setText(settings.getLineFillSymbol());
                    tableOfContentsLinesBetweenField.setValue((double)settings.getLinesBetween("toc"));
                    updateTableOfContentsPageFieldValues();

                }

                updateParagraphsPageFieldProperties();
                updateHeadingsPageFieldProperties();
                updateListsPageFieldProperties();
                updateTablesPageFieldProperties();
                updatePageNumbersPageFieldProperties();
                updateTableOfContentsPageFieldProperties();

            } else {
                switch (currentPage) {

                    case GENERAL_PAGE:

                        if (source.equals(transcriptionInfoCheckBox)) {
                            settings.transcriptionInfoEnabled = (transcriptionInfoCheckBox.getState() == (short) 1);
                        } else if (source.equals(volumeInfoCheckBox)) {
                            settings.volumeInfoEnabled = (volumeInfoCheckBox.getState() == (short) 1);
                        } else if (source.equals(transcribersNotesPageCheckBox)) {
                            settings.transcribersNotesPageEnabled = (transcribersNotesPageCheckBox.getState() == (short) 1);
                        } else if (source.equals(mainTranslationTableListBox)) {
                            settings.setTranslationTable(supportedTranslationTables.get((int)mainTranslationTableListBox.getSelectedItemPos()),settings.getMainLanguage());
                            updateMainGradeListBox();
                        } else if (source.equals(mainGradeListBox)) {
                            settings.setGrade(settings.getSupportedGrades(settings.getMainLanguage()).get((int)mainGradeListBox.getSelectedItemPos()),settings.getMainLanguage());
                        }

                        updateGeneralPageFieldProperties();
                        break;

                    case PARAGRAPHS_PAGE:

                        if (source.equals(paragraphAlignmentListBox)) {
                            settings.setCentered("paragraph", (paragraphAlignmentListBox.getSelectedItemPos()==(short)1));
                            updateParagraphsPageFieldProperties();
                        }

                        break;
                        
                    case HEADINGS_PAGE: 
                        
                        if (source.equals(headingLevelListBox)) {
                            saveHeadingsPageFieldValues();
                            currentHeadingLevel = headingLevelListBox.getSelectedItemPos() + 1;
                            updateHeadingsPageFieldValues();
                            updateHeadingsPageFieldProperties();
                        } else if (source.equals(headingAlignmentListBox)) {
                            settings.setCentered("heading" + currentHeadingLevel, (headingAlignmentListBox.getSelectedItemPos()==(short)1));
                            updateHeadingsPageFieldProperties();
                        }

                        break;
                        
                    case LISTS_PAGE:

                        if (source.equals(listLevelListBox)) {
                            saveListsPageFieldValues();
                            currentListLevel = listLevelListBox.getSelectedItemPos() + 1;
                            updateListsPageFieldValues();
                            updateListsPageFieldProperties();
                        } else if (source.equals(listAlignmentListBox)) {
                            settings.setCentered("list" + currentListLevel, (listAlignmentListBox.getSelectedItemPos()==(short)1));
                            updateListsPageFieldProperties();
                        }

                        break;

                    case TABLES_PAGE:

                        if (source.equals(tableSimpleRadioButton) ||
                            source.equals(tableStairstepRadioButton)) {

                            if (!settings.stairstepTableIsEnabled()) {
                                tableSimpleRadioButton.setState(false);
                                settings.setStairstepTable(true);
                                saveTablesPageFieldValues();
                                currentTableColumn = 1;
                            } else {
                                tableStairstepRadioButton.setState(false);
                                settings.setStairstepTable(false);
                                saveTablesPageFieldValues();
                                currentTableColumn = 0;
                            }
                            updateTablesPageFieldValues();
                            updateTablesPageFieldProperties();
                        } else if (source.equals(tableColumnListBox)) {
                            saveTablesPageFieldValues();
                            currentTableColumn = tableColumnListBox.getSelectedItemPos() + 1;
                            updateTablesPageFieldValues();
                            updateTablesPageFieldProperties();
                        } else if (source.equals(tableAlignmentListBox)) {
                            settings.setCentered("table" + ((currentTableColumn==0)?"":currentTableColumn), (tableAlignmentListBox.getSelectedItemPos()==(short)1));
                            updateTablesPageFieldProperties();
                        }
                        
                        break;

                    case PAGENUMBERS_PAGE:

                        if (source.equals(braillePageNumbersCheckBox)) {
                            settings.setBraillePageNumbers(braillePageNumbersCheckBox.getState() == (short)1);
                        } else if (source.equals(braillePageNumberAtListBox)) {
                            settings.setBraillePageNumberAt(((braillePageNumberAtListBox.getSelectedItemPos() == (short)0)?"top":"bottom"));
                        } else if (source.equals(pageSeparatorCheckBox)) {
                            settings.setPageSeparator(pageSeparatorCheckBox.getState() == (short)1);
                        }

                        else if (source.equals(printPageNumbersCheckBox) ||
                                 source.equals(printPageNumberAtListBox) ||
                                 source.equals(printPageNumberRangeCheckBox)) {

                            if (source.equals(printPageNumbersCheckBox)) {
                                settings.setPrintPageNumbers(printPageNumbersCheckBox.getState() == (short)1);
                            } else if (source.equals(printPageNumberAtListBox)) {
                                settings.setPrintPageNumberAt(((printPageNumberAtListBox.getSelectedItemPos() == (short)0)?"top":"bottom"));
                            } else if (source.equals(printPageNumberRangeCheckBox)) {
                                settings.setPrintPageNumberRange(printPageNumberRangeCheckBox.getState() == (short) 1);
                            }

                            if (settings.getPrintPageNumberAt().equals("top") &&
                                settings.getPrintPageNumbers() &&
                                settings.getPrintPageNumberRange()) {
                                    settings.setPageNumberAtTopOnSeparateLine(true);
                                    numbersAtTopOnSepLineCheckBox.setState((short)(settings.getPageNumberAtTopOnSeparateLine()?1:0));
                            }
                        }

                        updatePageNumbersPageFieldProperties();
                        break;

                    case LANGUAGES_PAGE:

                        if (source.equals(translationTableListBox)) {
                            settings.setTranslationTable(supportedTranslationTables.get((int)translationTableListBox.getSelectedItemPos()),
                                                         languages.get(selectedLanguagePos));
                            updateGradeListBox(languages.get(selectedLanguagePos));
                        } else if (source.equals(gradeListBox)) {
                            settings.setGrade(Integer.parseInt(gradeListBox.getSelectedItem()), languages.get(selectedLanguagePos));
                        } else if (source.equals(languagesListBox)) {
                            selectedLanguagePos = (int)languagesListBox.getSelectedItemPos();
                            updateTranslationTableListBox(languages.get(selectedLanguagePos));
                            updateGradeListBox(languages.get(selectedLanguagePos));
                        }

                        break;

                    case TOC_PAGE:
                        
                        if (source.equals(tableOfContentsCheckBox)) {
                            settings.tableOfContentEnabled = (tableOfContentsCheckBox.getState()==(short)1);
                            updateTableOfContentsPageFieldProperties();
                        } else if (source.equals(tableOfContentsLevelListBox)) {
                            saveTableOfContentsPageFieldValues();
                            currentTableOfContentsLevel = (int)tableOfContentsLevelListBox.getSelectedItemPos() + 1;
                            updateTableOfContentsPageFieldValues();
                            updateTableOfContentsPageFieldProperties();
                        }
                        
                        break;

                    case SPECIAL_SYMBOLS_PAGE:
                        
                        if (source.equals(specialSymbolsListCheckBox)) {
                            settings.specialSymbolsListEnabled = (specialSymbolsListCheckBox.getState() == (short) 1);
                            updateSpecialSymbolsPageFieldProperties();
                        } else if (source.equals(specialSymbolsListBox)) {
                            saveSpecialSymbolsPageFieldValues();
                            selectedSpecialSymbolPos = (int)specialSymbolsListBox.getSelectedItemPos();
                            updateSpecialSymbolsPageFieldValues();
                            updateSpecialSymbolsPageFieldProperties();                            
                        } else if (source.equals(specialSymbolsMode0RadioButton) ||
                                   source.equals(specialSymbolsMode1RadioButton) ||
                                   source.equals(specialSymbolsMode2RadioButton) ||
                                   source.equals(specialSymbolsMode3RadioButton)) {

                            SpecialSymbol selectedSpecialSymbol = specialSymbols.get(selectedSpecialSymbolPos);

                            if (source.equals(specialSymbolsMode0RadioButton)) {
                                selectedSpecialSymbol.setMode(SpecialSymbolMode.NEVER);
                            } else if (source.equals(specialSymbolsMode1RadioButton)) {
                                selectedSpecialSymbol.setMode(SpecialSymbolMode.IF_PRESENT_IN_VOLUME);
                            } else if (source.equals(specialSymbolsMode2RadioButton)) {
                                selectedSpecialSymbol.setMode(SpecialSymbolMode.FIRST_VOLUME);
                            } else if (source.equals(specialSymbolsMode3RadioButton)) {
                                selectedSpecialSymbol.setMode(SpecialSymbolMode.ALWAYS);
                            }

                            specialSymbolsMode0RadioButton.removeItemListener(this);
                            specialSymbolsMode1RadioButton.removeItemListener(this);
                            specialSymbolsMode2RadioButton.removeItemListener(this);
                            specialSymbolsMode3RadioButton.removeItemListener(this);

                            specialSymbolsMode0RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.NEVER);
                            specialSymbolsMode1RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.IF_PRESENT_IN_VOLUME);
                            specialSymbolsMode2RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.FIRST_VOLUME);
                            specialSymbolsMode3RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.ALWAYS);

                            specialSymbolsMode0RadioButton.addItemListener(this);
                            specialSymbolsMode1RadioButton.addItemListener(this);
                            specialSymbolsMode2RadioButton.addItemListener(this);
                            specialSymbolsMode3RadioButton.addItemListener(this);

                        }

                        break;

                    case MATH_PAGE: break;

                    case EXPORT_EMBOSS_PAGE:

                        if (source.equals(genericRadioButton) ||
                            source.equals(specificRadioButton)) {

                            updateGenericOrSpecific();
                            updateGenericBrailleListBox();
                            updateEmbosserListBox();
                            updateDuplexCheckBox();
                            updateTableListBox();
                            updatePaperSizeListBox();
                            updateDimensionFields();
                            updateMirrorAlignCheckBox();
                            updateOKButton();

                        } else if (source.equals(embosserListBox)) {

                            updateEmbosser();
                            updateDuplexCheckBox();
                            updateTableListBox();
                            updatePaperSizeListBox();
                            updateDimensionFields();
                            updateMirrorAlignCheckBox();
                            updateOKButton();

                        } else if (source.equals(genericBrailleListBox)) {

                            updateGenericBraille();
                            updateDuplexCheckBox();
                            updateTableListBox();
                            updatePaperSizeListBox();
                            updateDimensionFields();
                            updateMirrorAlignCheckBox();
                            updateOKButton();

                        } else if (source.equals(paperSizeListBox)) {

                            updatePaperSize();
                            updatePaperDimensionFields();
                            updateDimensionFields();
                            updateMirrorAlignCheckBox();
                            updateOKButton();

                        } else if (source.equals(paperWidthUnitListBox) ||
                                   source.equals(paperHeightUnitListBox)) {

                            updatePaperDimensionFields();

                        } else if (source.equals(duplexCheckBox)) {

                            updateDuplex();
                            updateMirrorAlignCheckBox();

                        }

                        break;

                    default:
                }
            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Is called when a 'Back' or 'Next' button is pressed.
     * The correct dialog tab is activated and buttons are updated.
     *
     * @param actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent) {

        Object source = actionEvent.Source;

        try {

            if (source.equals(backButton) || source.equals(nextButton)) {

                int newPage = 0;

                if (source.equals(backButton)) {

                    for (int i=currentPage-2;i>=0;i--) {
                        if (pagesEnabled[i]) {
                            newPage = i+1;
                            break;
                        }
                    }

                } else if (source.equals(nextButton)) {

                    for (int i=currentPage;i<pagesEnabled.length;i++) {
                        if (pagesEnabled[i]) {
                            newPage = i+1;
                            break;
                        }
                    }
                }

                if (newPage > 0) {

                    if (newPage==1) {
                        updateMainTranslationTableListBox();
                        updateMainGradeListBox();
                    } else if (newPage==2) {
                        updateTranslationTableListBox(settings.getMainLanguage());
                        updateGradeListBox(settings.getMainLanguage());
                    }

                    windowProperties.setPropertyValue("Step", new Integer(newPage));
                    roadmapProperties.setPropertyValue("CurrentItemID", (short)newPage);
                    setPage(newPage);

                }

            } else {

                switch (currentPage) {

                    case GENERAL_PAGE:
                    case HEADINGS_PAGE: break;
                    case LISTS_PAGE:

                        if (source.equals(listPrefixButton)) {
                            InsertDialog insertBrailleDialog = new InsertDialog(xContext);
                            insertBrailleDialog.setBrailleCharacters(settings.getListPrefix(currentListLevel));
                            if (insertBrailleDialog.execute()) {
                                settings.setListPrefix(insertBrailleDialog.getBrailleCharacters(), currentListLevel);
                                listPrefixField.setText(settings.getListPrefix(currentListLevel));
                            }
                        }

                        break;

                    case TABLES_PAGE:

                        if (source.equals(tableColumnDelimiterButton)) {
                            InsertDialog insertBrailleDialog = new InsertDialog(xContext);
                            insertBrailleDialog.setBrailleCharacters(settings.getColumnDelimiter());
                            if (insertBrailleDialog.execute()) {
                                settings.setColumnDelimiter(insertBrailleDialog.getBrailleCharacters());
                                tableColumnDelimiterField.setText(settings.getColumnDelimiter());
                            }
                        }

                        break;

                    case PAGENUMBERS_PAGE:
                    case LANGUAGES_PAGE: break;
                    case TOC_PAGE:

                        if (source.equals(tableOfContentsLineFillButton)) {
                            InsertDialog insertBrailleDialog = new InsertDialog(xContext);
                            insertBrailleDialog.setBrailleCharacters(settings.getLineFillSymbol());
                            if (insertBrailleDialog.execute()) {
                                if (settings.setLineFillSymbol(insertBrailleDialog.getBrailleCharacters())) {
                                    tableOfContentsLineFillField.setText(settings.getLineFillSymbol());
                                }
                            }
                        }

                        break;

                    case SPECIAL_SYMBOLS_PAGE:

                        if (source.equals(specialSymbolsSymbolButton)) {
                            InsertDialog insertBrailleDialog = new InsertDialog(xContext);
                            SpecialSymbol selectedSpecialSymbol = specialSymbols.get(selectedSpecialSymbolPos);
                            insertBrailleDialog.setBrailleCharacters(selectedSpecialSymbol.getSymbol());
                            if (insertBrailleDialog.execute()) {
                                String symbol = insertBrailleDialog.getBrailleCharacters();
                                if (selectedSpecialSymbol.setSymbol(symbol)) {
                                    specialSymbolsSymbolField.setText(symbol);
                                    updateSpecialSymbolsListBox();
                                }
                            }
                        } else if (source.equals(specialSymbolsAddButton)) {
                            saveSpecialSymbolsPageFieldValues();
                            selectedSpecialSymbolPos = settings.addSpecialSymbol();
                            InsertDialog insertBrailleDialog = new InsertDialog(xContext);
                            SpecialSymbol selectedSpecialSymbol = specialSymbols.get(selectedSpecialSymbolPos);
                            insertBrailleDialog.setBrailleCharacters(selectedSpecialSymbol.getSymbol());
                            if (insertBrailleDialog.execute()) {
                                String symbol = insertBrailleDialog.getBrailleCharacters();
                                if (selectedSpecialSymbol.setSymbol(symbol)) {
                                    specialSymbolsSymbolField.setText(symbol);
                                }
                            }
                            updateSpecialSymbolsPageFieldValues();
                            updateSpecialSymbolsPageFieldProperties();
                            updateSpecialSymbolsListBox();
                        } else if (source.equals(specialSymbolsRemoveButton)) {
                            selectedSpecialSymbolPos = settings.deleteSpecialSymbol(selectedSpecialSymbolPos);
                            updateSpecialSymbolsPageFieldValues();
                            updateSpecialSymbolsPageFieldProperties();
                            updateSpecialSymbolsListBox();
                        } else if (source.equals(specialSymbolsMoveUpButton)) {
                            selectedSpecialSymbolPos = settings.moveSpecialSymbolUp(selectedSpecialSymbolPos);
                            updateSpecialSymbolsListBox();
                        } else if (source.equals(specialSymbolsMoveDownButton)) {
                            selectedSpecialSymbolPos = settings.moveSpecialSymbolDown(selectedSpecialSymbolPos);
                            updateSpecialSymbolsListBox();
                        }

                        break;

                    case MATH_PAGE:
                    case EXPORT_EMBOSS_PAGE: break;

                }
            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Is called when a 'Cells per line', 'Lines per page' or 'Margin' field value is changed by the user.
     * All relevant braille settings and dialog fields are updated.
     *
     * @param textEvent
     */
    public void textChanged(TextEvent textEvent) {

        Object source = textEvent.Source;

        try {

            if (source.equals(numberOfCellsPerLineTextComponent)) {
                settings.setCellsPerLine((int)numberOfCellsPerLineField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(numberOfLinesPerPageTextComponent)) {
                settings.setLinesPerPage((int)numberOfLinesPerPageField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(marginLeftTextComponent)) {
                settings.setMarginLeft((int)marginLeftField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(marginRightTextComponent)) {
                settings.setMarginRight((int)marginRightField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(marginTopTextComponent)) {
                settings.setMarginTop((int)marginTopField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(marginBottomTextComponent)) {
                settings.setMarginBottom((int)marginBottomField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(paperWidthTextComponent) ||
                       source.equals(paperHeightTextComponent)) {
                updatePaperDimensions();
                updateDimensionFields();
            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param event
     */
    public void disposing(EventObject event) {}

}
