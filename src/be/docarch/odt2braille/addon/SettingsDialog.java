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

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.container.XNameContainer;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XRadioButton;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XItemEventBroadcaster;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XToolkit;

import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.Settings.MathType;
import be.docarch.odt2braille.Settings.BrailleRules;
import be.docarch.odt2braille.Settings.PageNumberFormat;
import be.docarch.odt2braille.Settings.PageNumberPosition;
import be.docarch.odt2braille.SpecialSymbol;
import be.docarch.odt2braille.SpecialSymbol.SpecialSymbolType;
import be.docarch.odt2braille.SpecialSymbol.SpecialSymbolMode;
import be.docarch.odt2braille.Style;
import be.docarch.odt2braille.Style.Alignment;
import be.docarch.odt2braille.ParagraphStyle;
import be.docarch.odt2braille.HeadingStyle;
import be.docarch.odt2braille.CharacterStyle;
import be.docarch.odt2braille.CharacterStyle.TypefaceOption;


/**
 * Show an OpenOffice.org dialog window for adjusting the braille settings.
 * The dialog has 10 tabs:
 * <ul>
 * <li>General Settings</li>
 * <li>Language Settings</li>
 * <li>Typeface Settings</li>
 * <li>Paragraph Settings</li>
 * <li>Heading Settings</li>
 * <li>List Settings</li>
 * <li>Table Settings</li>
 * <li>Pagenumber Settings</li>
 * <li>Table of Contents Settings</li>
 * <li>Special Symbols Settings</li>
 * <li>Mathematics Settings</li>
 * </ul>
 *
 * @see         be.docarch.odt2braille.Settings
 * @author      Bert Frees
 */
public class SettingsDialog implements XItemListener,
                                       XActionListener {

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille.addon");

    private Settings settings = null;
    private XComponentContext xContext = null;
    private Locale oooLocale = null;

    private final static short GENERAL_PAGE = 1;
    private final static short LANGUAGES_PAGE = 2;
    private final static short TYPEFACE_PAGE = 3;
    private final static short PARAGRAPHS_PAGE = 4;
    private final static short HEADINGS_PAGE = 5;
    private final static short LISTS_PAGE = 6;
    private final static short TABLES_PAGE = 7;
    private final static short PAGENUMBERS_PAGE = 8;
    private final static short TOC_PAGE = 9;
    private final static short SPECIAL_SYMBOLS_PAGE = 10;
    private final static short MATH_PAGE = 11;

    private final static short NUMBER_OF_PAGES = 11;

    private boolean[] pagesEnabled = {true, true, true, true, true, true, true, true, true, true, true};
    private boolean[] pagesVisited = {false,false,false,false,false,false,false,false,false,false,false};
    private int currentPage = 1;

    private int currentListLevel;
    private int currentTableColumn;
    private int currentTableOfContentsLevel;
    private int selectedLanguagePos;
    private int selectedSpecialSymbolPos;
    private int selectedParagraphStylePos;
    private int selectedCharacterStylePos;
    private int selectedHeadingStylePos;
    
    private ArrayList<String> allTranslationTables = null;
    private ArrayList<String> mainTranslationTables = null;
    private ArrayList<String> specialTranslationTables = null;
    private ArrayList<String> languages = null;
    private ArrayList<SpecialSymbol> specialSymbols = null;
    private ArrayList<ParagraphStyle> paragraphStyles = null;
    private ArrayList<CharacterStyle> characterStyles = null;
    private ArrayList<HeadingStyle> headingStyles = null;
    private ArrayList<MathType> mathTypes = null;
    private ArrayList<Alignment>alignmentOptions = null;
    private ArrayList<TypefaceOption>typefaceOptions = null;

    private XDialog dialog = null;
    private XControlContainer dialogControlContainer = null;
    private XComponent dialogComponent = null;
    private XControl dialogControl = null;

    // Main window

    private XButton okButton = null;
    private XButton cancelButton = null;
    private XButton backButton = null;
    private XButton nextButton = null;
    private XListBox brailleRulesListBox = null;
    private XItemEventBroadcaster roadMapBroadcaster = null;

    private XPropertySet windowProperties = null;
    private XPropertySet roadmapProperties = null;
    private XPropertySet backButtonProperties = null;
    private XPropertySet nextButtonProperties = null;

    private static String _okButton = "CommandButton1";
    private static String _cancelButton = "CommandButton2";
    private static String _backButton = "CommandButton3";
    private static String _nextButton = "CommandButton4";
    private static String _brailleRulesListBox = "ListBox19";

    private String L10N_windowTitle = null;
    private String L10N_roadmapTitle = null;
    private String[] L10N_roadmapLabels = new String[NUMBER_OF_PAGES];

    private String L10N_okButton = null;
    private String L10N_cancelButton = null;
    private String L10N_nextButton = null;
    private String L10N_backButton = null;

    private TreeMap<Alignment,String> L10N_alignment = new TreeMap();
    private TreeMap<TypefaceOption,String> L10N_typeface = new TreeMap();

    // General Page

    private XTextComponent creatorField = null;
    private XTextComponent transcribersNotesPageField = null;
    private XListBox mainTranslationTableListBox = null;
    private XListBox mainGradeListBox = null;
    private XCheckBox mainEightDotsCheckBox = null;
    private XCheckBox transcribersNotesPageCheckBox = null;
    private XCheckBox transcriptionInfoCheckBox = null;
    private XCheckBox volumeInfoCheckBox = null;
    private XCheckBox preliminaryVolumeCheckBox = null;
    private XCheckBox hyphenateCheckBox = null;
    private XCheckBox hardPageBreaksCheckBox = null;

    private XPropertySet mainEightDotsCheckBoxProperties = null;
    private XPropertySet transcribersNotesPageFieldProperties = null;    
    private XPropertySet transcriptionInfoCheckBoxProperties = null;
    private XPropertySet volumeInfoCheckBoxProperties = null;
    private XPropertySet transcribersNotesPageCheckBoxProperties = null;
    private XPropertySet creatorFieldProperties = null;
    private XPropertySet preliminaryVolumeCheckBoxProperties = null;

    private static String _mainTranslationTableListBox = "ListBox1";
    private static String _mainGradeListBox = "ListBox2";
    private static String _mainEightDotsCheckBox = "CheckBox21";
    private static String _creatorField = "TextField1";
    private static String _transcribersNotesPageField = "TextField3";
    private static String _transcriptionInfoCheckBox = "CheckBox1";
    private static String _volumeInfoCheckBox = "CheckBox2";
    private static String _transcribersNotesPageCheckBox = "CheckBox4";
    private static String _preliminaryVolumeCheckBox = "CheckBox8";
    private static String _hyphenateCheckBox = "CheckBox20";
    private static String _hardPageBreaksCheckBox = "CheckBox19";

    private static String _mainTranslationTableLabel = "Label1";
    private static String _mainGradeLabel = "Label2";
    private static String _mainEightDotsLabel = "Label74";
    private static String _transcriptionInfoLabel = "Label3";
    private static String _creatorLabel = "Label4";
    private static String _volumeInfoLabel = "Label5";
    private static String _transcribersNotesPageLabel = "Label7";
    private static String _preliminaryVolumeLabel = "Label11";
    private static String _hyphenateLabel = "Label77";
    private static String _hardPageBreaksLabel = "Label72";

    private String L10N_creatorLabel = null;
    private String L10N_mainTranslationTableLabel = null;
    private String L10N_mainGradeLabel = null;
    private String L10N_mainEightDotsLabel = null;
    private String L10N_transcribersNotesPageLabel = null;
    private String L10N_transcriptionInfoLabel = null;
    private String L10N_volumeInfoLabel = null;
    private String L10N_preliminaryVolumeLabel = null;
    private String L10N_hyphenateLabel = null;
    private String L10N_hardPageBreaksLabel = null;

    private TreeMap<Integer,String> L10N_grades = new TreeMap();
    private TreeMap<String,String> L10N_translationTables = new TreeMap();

    // Languages Page

    private XListBox translationTableListBox = null;
    private XListBox gradeListBox = null;
    private XListBox languagesListBox = null;
    private XCheckBox eightDotsCheckBox = null;

    private XPropertySet gradeListBoxProperties = null;
    private XPropertySet eightDotsCheckBoxProperties = null;

    private static String _languagesListBox = "ListBox3";
    private static String _translationTableListBox = "ListBox4";
    private static String _gradeListBox = "ListBox5";
    private static String _eightDotsCheckBox = "CheckBox22";

    private static String _translationTableLabel = "Label12";
    private static String _gradeLabel = "Label13";
    private static String _eightDotsLabel = "Label82";

    private String L10N_translationTableLabel = null;
    private String L10N_gradeLabel = null;
    private String L10N_eightDotsLabel = null;

    private TreeMap<String,String> L10N_languages = new TreeMap();

    // Typeface Page

    private XListBox characterStyleListBox = null;
    private XCheckBox characterInheritCheckBox = null;
    private XTextComponent characterParentField = null;
    private XListBox characterBoldfaceListBox = null;
    private XListBox characterItalicListBox = null;
    private XListBox characterUnderlineListBox = null;
    private XListBox characterCapitalsListBox = null;

    private XPropertySet characterInheritCheckBoxProperties = null;
    private XPropertySet characterParentFieldProperties = null;
    private XPropertySet characterBoldfaceListBoxProperties = null;
    private XPropertySet characterItalicListBoxProperties = null;
    private XPropertySet characterUnderlineListBoxProperties = null;
    private XPropertySet characterCapitalsListBoxProperties = null;

    private static String _characterStyleListBox = "ListBox28";
    private static String _characterInheritCheckBox = "CheckBox24";
    private static String _characterParentField = "TextField11";
    private static String _characterBoldfaceListBox = "ListBox29";
    private static String _characterItalicListBox = "ListBox27";
    private static String _characterUnderlineListBox = "ListBox31";
    private static String _characterCapitalsListBox = "ListBox30";

    private static String _characterStyleLabel = "Label90";
    private static String _characterInheritLabel = "Label91";
    private static String _characterBoldfaceLabel = "Label85";
    private static String _characterItalicLabel = "Label89";
    private static String _characterUnderlineLabel = "Label87";
    private static String _characterCapitalsLabel = "Label86";

    private String L10N_characterStyleLabel = null;
    private String L10N_characterInheritLabel = null;
    private String L10N_characterBoldfaceLabel = null;
    private String L10N_characterItalicLabel = null;
    private String L10N_characterUnderlineLabel = null;
    private String L10N_characterCapitalsLabel = null;

    // Paragraphs Page

    private XListBox paragraphStyleListBox = null;
    private XCheckBox paragraphInheritCheckBox = null;
    private XTextComponent paragraphParentField = null;
    private XNumericField paragraphLinesAboveField = null;
    private XNumericField paragraphLinesBelowField = null;
    private XListBox paragraphAlignmentListBox = null;
    private XNumericField paragraphFirstLineField = null;
    private XNumericField paragraphRunoversField = null;
    private XNumericField paragraphMarginLeftRightField = null;
    private XCheckBox paragraphKeepEmptyCheckBox = null;
    private XCheckBox paragraphKeepWithNextCheckBox = null;
    private XCheckBox paragraphDontSplitCheckBox = null;
    private XCheckBox paragraphWidowControlCheckBox = null;
    private XCheckBox paragraphOrphanControlCheckBox = null;
    private XNumericField paragraphWidowControlField = null;
    private XNumericField paragraphOrphanControlField = null;

    private XPropertySet paragraphInheritCheckBoxProperties = null;
    private XPropertySet paragraphParentFieldProperties = null;
    private XPropertySet paragraphFirstLineFieldProperties = null;
    private XPropertySet paragraphRunoversFieldProperties = null;
    private XPropertySet paragraphMarginLeftRightFieldProperties = null;
    private XPropertySet paragraphAlignmentListBoxProperties = null;
    private XPropertySet paragraphLinesAboveProperties = null;
    private XPropertySet paragraphLinesBelowProperties = null;
    private XPropertySet paragraphKeepEmptyCheckBoxProperties = null;
    private XPropertySet paragraphKeepWithNextCheckBoxProperties = null;
    private XPropertySet paragraphDontSplitCheckBoxProperties = null;
    private XPropertySet paragraphWidowControlCheckBoxProperties = null;
    private XPropertySet paragraphOrphanControlCheckBoxProperties = null;
    private XPropertySet paragraphWidowControlFieldProperties = null;
    private XPropertySet paragraphOrphanControlFieldProperties  = null;

    private static String _paragraphStyleListBox = "ListBox26";
    private static String _paragraphInheritCheckBox = "CheckBox23";
    private static String _paragraphParentField = "TextField10";
    private static String _paragraphAlignmentListBox = "ListBox12";
    private static String _paragraphFirstLineField = "NumericField7";
    private static String _paragraphRunoversField = "NumericField8";
    private static String _paragraphMarginLeftRightField = "NumericField2";
    private static String _paragraphLinesAboveField = "NumericField9";
    private static String _paragraphLinesBelowField = "NumericField10";
    private static String _paragraphKeepEmptyCheckBox = "CheckBox9";
    private static String _paragraphKeepWithNextCheckBox = "CheckBox27";
    private static String _paragraphDontSplitCheckBox = "CheckBox28";
    private static String _paragraphWidowControlCheckBox = "CheckBox31";
    private static String _paragraphOrphanControlCheckBox = "CheckBox32";
    private static String _paragraphWidowControlField = "NumericField16";
    private static String _paragraphOrphanControlField  = "NumericField26";

    private static String _paragraphStyleLabel = "Label83";
    private static String _paragraphInheritLabel = "Label84";
    private static String _paragraphAlignmentLabel = "Label37";
    private static String _paragraphFirstLineLabel = "Label31";
    private static String _paragraphRunoversLabel = "Label32";
    private static String _paragraphMarginLeftRightLabel = "Label19";
    private static String _paragraphLinesAboveLabel = "Label33";
    private static String _paragraphLinesBelowLabel = "Label34";
    private static String _paragraphKeepEmptyLabel = "Label14";
    private static String _paragraphKeepWithNextLabel = "Label70";
    private static String _paragraphDontSplitLabel = "Label71";
    private static String _paragraphWidowControlLabel = "Label88";
    private static String _paragraphOrphanControlLabel = "Label92";

    private String L10N_paragraphStyleLabel = null;
    private String L10N_paragraphInheritLabel = null;
    private String L10N_paragraphAlignmentLabel = null;
    private String L10N_paragraphFirstLineLabel = null;
    private String L10N_paragraphRunoversLabel = null;
    private String L10N_paragraphMarginLeftRightLabel = null;
    private String L10N_paragraphLinesAboveLabel = null;
    private String L10N_paragraphLinesBelowLabel = null;
    private String L10N_paragraphKeepEmptyLabel = null;
    private String L10N_paragraphKeepWithNextLabel = null;
    private String L10N_paragraphDontSplitLabel = null;
    private String L10N_paragraphWidowControlLabel = null;
    private String L10N_paragraphOrphanControlLabel = null;

    // Headings Page

    private XListBox headingLevelListBox = null;
    private XListBox headingAlignmentListBox = null;
    private XNumericField headingFirstLineField = null;
    private XNumericField headingRunoversField = null;
    private XNumericField headingMarginLeftRightField = null;
    private XNumericField headingLinesAboveField = null;
    private XNumericField headingLinesBelowField = null;
    private XCheckBox headingNewBraillePageCheckBox = null;
    private XCheckBox headingKeepWithNextCheckBox = null;
    private XCheckBox headingDontSplitCheckBox = null;

    private XPropertySet headingFirstLineFieldProperties = null;
    private XPropertySet headingRunoversFieldProperties = null;
    private XPropertySet headingMarginLeftRightFieldProperties = null;
    private XPropertySet headingAlignmentListBoxProperties = null;
    private XPropertySet headingLinesAboveProperties = null;
    private XPropertySet headingLinesBelowProperties = null;
    private XPropertySet headingDontSplitCheckBoxProperties = null;

    private static String _headingLevelListBox = "ListBox13";
    private static String _headingAlignmentListBox = "ListBox14";
    private static String _headingFirstLineField = "NumericField11";
    private static String _headingRunoversField = "NumericField12";
    private static String _headingMarginLeftRightField = "NumericField3";
    private static String _headingLinesAboveField = "NumericField13";
    private static String _headingLinesBelowField = "NumericField14";
    private static String _headingNewBraillePageCheckBox = "CheckBox10";
    private static String _headingKeepWithNextCheckBox = "CheckBox29";
    private static String _headingDontSplitCheckBox = "CheckBox30";

    private static String _headingLevelLabel = "Label42";
    private static String _headingAlignmentLabel = "Label43";
    private static String _headingFirstLineLabel = "Label38";
    private static String _headingRunoversLabel = "Label39";
    private static String _headingMarginLeftRightLabel = "Label20";
    private static String _headingLinesAboveLabel = "Label40";
    private static String _headingLinesBelowLabel = "Label41";
    private static String _headingNewBraillePageLabel = "Label15";
    private static String _headingKeepWithNextLabel = "Label93";
    private static String _headingDontSplitLabel = "Label94";

    private String L10N_headingLevelLabel = null;
    private String L10N_headingAlignmentLabel = null;
    private String L10N_headingFirstLineLabel = null;
    private String L10N_headingRunoversLabel = null;
    private String L10N_headingMarginLeftRightLabel = null;
    private String L10N_headingLinesAboveLabel = null;
    private String L10N_headingLinesBelowLabel = null;
    private String L10N_headingNewBraillePageLabel = null;
    private String L10N_headingKeepWithNextLabel = null;
    private String L10N_headingDontSplitLabel = null;

    // Lists Page

    private XNumericField listLinesAboveField = null;
    private XNumericField listLinesBelowField = null;
    private XNumericField listLinesBetweenField = null;
    private XListBox listLevelListBox = null;
    private XListBox listAlignmentListBox = null;
    private XNumericField listFirstLineField = null;
    private XNumericField listRunoversField = null;
    private XNumericField listMarginLeftRightField = null;
    private XTextComponent listPrefixField = null;
    private XButton listPrefixButton = null;

    private XPropertySet listFirstLineFieldProperties = null;
    private XPropertySet listRunoversFieldProperties = null;
    private XPropertySet listMarginLeftRightFieldProperties = null;
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
    private static String _listRunoversField = "NumericField4";
    private static String _listMarginLeftRightField = "NumericField5";
    private static String _listPrefixField = "TextField7";
    private static String _listPrefixButton = "CommandButton7";

    private static String _listLinesAboveLabel = "Label30";
    private static String _listLinesBelowLabel = "Label36";
    private static String _listLinesBetweenLabel = "Label60";
    private static String _listLevelLabel = "Label44";
    private static String _listAlignmentLabel = "Label45";
    private static String _listFirstLineLabel = "Label28";
    private static String _listRunoversLabel = "Label21";
    private static String _listMarginLeftRightLabel = "Label22";
    private static String _listPrefixLabel = "Label27";

    private String L10N_listLinesAboveLabel = null;
    private String L10N_listLinesBelowLabel = null;
    private String L10N_listLinesBetweenLabel = null;
    private String L10N_listLevelLabel = null;
    private String L10N_listAlignmentLabel = null;
    private String L10N_listFirstLineLabel = null;
    private String L10N_listRunoversLabel = null;
    private String L10N_listMarginLeftRightLabel = null;
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
    private XNumericField tableMarginLeftRightField = null;
    private XTextComponent tableColumnDelimiterField = null;
    private XButton tableColumnDelimiterButton = null;

    private XPropertySet tableFirstLineFieldProperties = null;
    private XPropertySet tableRunoversFieldProperties = null;
    private XPropertySet tableMarginLeftRightFieldProperties = null;
    private XPropertySet tableColumnListBoxProperties = null;
    private XPropertySet tableColumnDelimiterFieldProperties = null;
    private XPropertySet tableColumnDelimiterButtonProperties = null;
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
    private static String _tableMarginLeftRightField = "NumericField6";
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
    private static String _tableMarginLeftRightLabel = "Label29";
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
    private String L10N_tableMarginLeftRightLabel = null;
    private String L10N_tableColumnDelimiterLabel = null;
    private String L10N_tableColumnDelimiterButton = "...";

    // Pagenumbers Page

    private XCheckBox braillePageNumbersCheckBox = null;
    private XListBox braillePageNumberAtListBox = null;
    private XListBox preliminaryPageNumberFormatListBox = null;
    private XNumericField beginningBraillePageNumberField = null;
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

    private XPropertySet braillePageNumbersCheckBoxProperties = null;
    private XPropertySet braillePageNumberAtListBoxProperties = null;
    private XPropertySet preliminaryPageNumberFormatListBoxProperties = null;
    private XPropertySet beginningBraillePageNumberFieldProperties = null;
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
    private static String _beginningBraillePageNumberField = "NumericField1";
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

    private static String _braillePageNumbersLabel = "Label10";
    private static String _braillePageNumberAtLabel = "Label57";
    private static String _preliminaryPageNumberFormatLabel = "Label55";
    private static String _beginningBraillePageNumberLabel = "Label16";
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

    private String L10N_braillePageNumbersLabel = null;
    private String L10N_braillePageNumberAtLabel = null;
    private String L10N_preliminaryPageNumberFormatLabel = null;
    private String L10N_beginningBraillePageNumberLabel = null;
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
    private String L10N_top = null;
    private String L10N_bottom = null;

    // Table of Contents Page

    private XCheckBox tableOfContentsCheckBox = null;
    private XTextComponent tableOfContentsTitleField = null;
    private XNumericField tableOfContentsLinesBetweenField = null;
    private XListBox tableOfContentsLevelListBox = null;
    private XNumericField tableOfContentsFirstLineField = null;
    private XNumericField tableOfContentsRunoversField = null;
    private XTextComponent tableOfContentsLineFillField = null;
    private XButton tableOfContentsLineFillButton = null;
    private XCheckBox tableOfContentsPrintPageNumbersCheckBox = null;
    private XCheckBox tableOfContentsBraillePageNumbersCheckBox = null;

    private XPropertySet tableOfContentsCheckBoxProperties = null;
    private XPropertySet tableOfContentsTitleFieldProperties = null;
    private XPropertySet tableOfContentsLinesBetweenFieldProperties = null;
    private XPropertySet tableOfContentsLevelListBoxProperties = null;
    private XPropertySet tableOfContentsFirstLineFieldProperties = null;
    private XPropertySet tableOfContentsRunoversFieldProperties = null;
    private XPropertySet tableOfContentsLineFillFieldProperties = null;
    private XPropertySet tableOfContentsLineFillButtonProperties = null;
    private XPropertySet tableOfContentsPrintPageNumbersCheckBoxProperties = null;
    private XPropertySet tableOfContentsBraillePageNumbersCheckBoxProperties = null;

    private static String _tableOfContentsCheckBox = "CheckBox5";
    private static String _tableOfContentsTitleField = "TextField4";
    private static String _tableOfContentsLinesBetweenField = "NumericField25";
    private static String _tableOfContentsLevelListBox = "ListBox18";
    private static String _tableOfContentsFirstLineField = "NumericField23";
    private static String _tableOfContentsRunoversField = "NumericField24";
    private static String _tableOfContentsLineFillField = "TextField6";
    private static String _tableOfContentsLineFillButton = "CommandButton6";
    private static String _tableOfContentsPrintPageNumbersCheckBox = "CheckBox25";
    private static String _tableOfContentsBraillePageNumbersCheckBox = "CheckBox26";

    private static String _tableOfContentsLabel = "Label8";
    private static String _tableOfContentsTitleLabel = "Label58";
    private static String _tableOfContentsLinesBetweenLabel = "Label54";
    private static String _tableOfContentsLevelLabel = "Label56";
    private static String _tableOfContentsFirstLineLabel = "Label52";
    private static String _tableOfContentsRunoversLabel = "Label53";
    private static String _tableOfContentsLineFillLabel = "Label26";
    private static String _tableOfContentsPrintPageNumbersLabel = "Label17";
    private static String _tableOfContentsBraillePageNumbersLabel = "Label18";

    private String L10N_tableOfContentsLabel = null;
    private String L10N_tableOfContentsTitleLabel = null;
    private String L10N_tableOfContentsLinesBetweenLabel = null;
    private String L10N_tableOfContentsLevelLabel = null;
    private String L10N_tableOfContentsFirstLineLabel = null;
    private String L10N_tableOfContentsRunoversLabel = null;
    private String L10N_tableOfContentsLineFillLabel = null;
    private String L10N_tableOfContentsLineFillButton = "...";
    private String L10N_tableOfContentsPrintPageNumbersLabel = null;
    private String L10N_tableOfContentsBraillePageNumbersLabel = null;

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
    private static String _specialSymbolsSymbolLabel = "Label75";
    private static String _specialSymbolsDescriptionLabel = "Label76";
    private static String _specialSymbolsMode0Label = "Label81";
    private static String _specialSymbolsMode1Label = "Label80";
    private static String _specialSymbolsMode2Label = "Label79";
    private static String _specialSymbolsMode3Label = "Label78";
    
    private String L10N_specialSymbolsListLabel = null;
    private String L10N_specialSymbolsListTitleLabel = null;
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
    private TreeMap<String,String> L10N_math = new TreeMap();


    /**
     * The dialog is created from an OpenOffice.org .xdl file.
     *
     * @param   xContext
     */
    public SettingsDialog(XComponentContext xContext)
                   throws com.sun.star.uno.Exception {

        logger.entering("SettingsDialog", "<init>");

        this.xContext = xContext;

        try {
            this.oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            this.oooLocale = Locale.getDefault();
        }

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn") + "/dialogs/SettingsDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, dialogControl.getModel());

        logger.exiting("SettingsDialog", "<init>");

    }

    /**
     * Not used
     *
     * @param xMCF
     * @throws com.sun.star.uno.Exception
     */
    public void startLoading(XMultiComponentFactory xMCF)
                      throws com.sun.star.uno.Exception {
    
        logger.entering("SettingsDialog", "startLoading");

        XToolkit xToolkit = (XToolkit) UnoRuntime.queryInterface(XToolkit.class, xMCF.createInstanceWithContext("com.sun.star.awt.Toolkit", xContext));
        XWindow xWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class, dialogControl);
        dialogControl.createPeer(xToolkit, null);
        windowProperties.setPropertyValue("Title",
                ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("settingsDialogTitle")
                + "  -  Loading... please wait");
        windowProperties.setPropertyValue("Step", 100);
        xWindow.setVisible(true);

        logger.exiting("SettingsDialog", "startLoading");
    
    }

    /**
     * @param   settings       The braille settings.
     * @param   progressbar
     */
    public void initialise(Settings settings,
                           ProgressBar progressbar)
                    throws com.sun.star.uno.Exception {

        logger.entering("SettingsDialog", "initialise");

        this.settings = settings;

        specialTranslationTables = settings.getSpecialTranslationTables();
        mainTranslationTables = settings.getSupportedTranslationTables();
        languages = settings.getLanguages();
        mathTypes = new ArrayList(Arrays.asList(MathType.values()));
        alignmentOptions = new ArrayList(Arrays.asList(Alignment.values()));
        typefaceOptions = new ArrayList(Arrays.asList(TypefaceOption.values()));

        pagesEnabled[LANGUAGES_PAGE-1] = (languages.size() > 1);
        // pagesEnabled[PARAGRAPHS_PAGE-1] = settings.getParagraphsPresent();
        // pagesEnabled[HEADINGS_PAGE-1] = settings.getHeadingsPresent();
        // pagesEnabled[LISTS_PAGE-1] = settings.getListsPresent();
        // pagesEnabled[TABLES_PAGE-1] = settings.getTablesPresent();
        // pagesEnabled[MATH_PAGE-1] = settings.getMathPresent();
        // pagesEnabled[TOC_PAGE-1] = settings.getPreliminaryPagesPresent();
        // pagesEnabled[SPECIAL_SYMBOLS_PAGE-1] = settings.getPreliminaryPagesPresent();

        // Main Window

        L10N_windowTitle = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("settingsDialogTitle");
        L10N_roadmapTitle = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("settingsRoadmapTitle");
        L10N_roadmapLabels[GENERAL_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("generalSettingsPageTitle");
        L10N_roadmapLabels[LANGUAGES_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("languageSettingsPageTitle");
        L10N_roadmapLabels[TYPEFACE_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("typefaceSettingsPageTitle");
        L10N_roadmapLabels[PARAGRAPHS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paragraphSettingsPageTitle");
        L10N_roadmapLabels[HEADINGS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("headingSettingsPageTitle");
        L10N_roadmapLabels[LISTS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listSettingsPageTitle");
        L10N_roadmapLabels[TABLES_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableSettingsPageTitle");
        L10N_roadmapLabels[PAGENUMBERS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("pagenumberSettingsPageTitle");
        L10N_roadmapLabels[TOC_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsSettingsPageTitle");
        L10N_roadmapLabels[SPECIAL_SYMBOLS_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsSettingsPageTitle");
        L10N_roadmapLabels[MATH_PAGE-1] = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("mathSettingsPageTitle");

        L10N_okButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("saveButton");
        L10N_cancelButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("cancelButton");
        L10N_backButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("backButton");
        L10N_nextButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("nextButton");

        // General Page

        L10N_creatorLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("creatorLabel") + ":";
        L10N_mainTranslationTableLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("languageLabel") + ":";
        L10N_mainGradeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("gradeLabel") + ":";
        L10N_mainEightDotsLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("useEightDotsLabel");
        L10N_transcribersNotesPageLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("transcribersNotesPageLabel");
        L10N_transcriptionInfoLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("transcriptionInfoLabel");
        L10N_volumeInfoLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("volumeInfoLabel");
        L10N_preliminaryVolumeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("preliminaryVolumeLabel");
        L10N_hyphenateLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("hyphenateLabel");
        L10N_hardPageBreaksLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("hardPageBreaksLabel");

        // Languages Page

        L10N_translationTableLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("languageLabel") + ":";
        L10N_gradeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("gradeLabel") + ":";
        L10N_eightDotsLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("useEightDotsLabel");

        // Typeface Page

        L10N_characterStyleLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("styleLabel");
        L10N_characterInheritLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("inheritLabel") + ":";
        L10N_characterBoldfaceLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("characterBoldfaceLabel");
        L10N_characterItalicLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("characterItalicLabel");
        L10N_characterUnderlineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("characterUnderlineLabel");
        L10N_characterCapitalsLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("characterCapitalsLabel");

        // Paragraphs Page

        L10N_paragraphStyleLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("styleLabel");
        L10N_paragraphInheritLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("inheritLabel") + ":";
        L10N_paragraphAlignmentLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("alignmentLabel") + ":";
        L10N_paragraphFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_paragraphRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_paragraphMarginLeftRightLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("centeredMarginLabel") + ":";
        L10N_paragraphLinesAboveLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesAboveLabel") + ":";
        L10N_paragraphLinesBelowLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesBelowLabel") + ":";
        L10N_paragraphKeepEmptyLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paragraphKeepEmptyLabel");
        L10N_paragraphKeepWithNextLabel = "Keep with next";
        L10N_paragraphDontSplitLabel = "Don't split";
        L10N_paragraphWidowControlLabel = "Widow control:";
        L10N_paragraphOrphanControlLabel = "Orphan control:";

        // Headings Page

        L10N_headingLevelLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("levelLabel");
        L10N_headingAlignmentLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("alignmentLabel") + ":";
        L10N_headingFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_headingRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_headingMarginLeftRightLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("centeredMarginLabel") + ":";
        L10N_headingLinesAboveLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesAboveLabel") + ":";
        L10N_headingLinesBelowLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("linesBelowLabel") + ":";
        L10N_headingNewBraillePageLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("headingNewBraillePageLabel");
        L10N_headingKeepWithNextLabel = "Keep with next";
        L10N_headingDontSplitLabel = "Don't split";

        // Lists Page

        L10N_listLinesAboveLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listLinesAboveLabel") + ":";
        L10N_listLinesBelowLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listLinesBelowLabel") + ":";
        L10N_listLinesBetweenLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("listLinesBetweenLabel") + ":";
        L10N_listLevelLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("levelLabel");
        L10N_listAlignmentLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("alignmentLabel") + ":";
        L10N_listFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_listRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_listMarginLeftRightLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("centeredMarginLabel") + ":";
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
        L10N_tableMarginLeftRightLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("centeredMarginLabel") + ":";
        L10N_tableColumnDelimiterLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("columnDelimiterLabel") + ":";

        // Pagenumbers Page

        L10N_braillePageNumbersLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("braillePageNumbersLabel");
        L10N_braillePageNumberAtLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("braillePageNumberAtLabel") + ":";
        L10N_preliminaryPageNumberFormatLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("preliminaryPageNumberFormatLabel") + ":";
        L10N_beginningBraillePageNumberLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("beginningBraillePageNumberLabel") + ":";
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
        L10N_top = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("top");
        L10N_bottom = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("bottom");

        // Table of Contents Page

        L10N_tableOfContentsLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsLabel");
        L10N_tableOfContentsTitleLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsTitleLabel") + ":";
        L10N_tableOfContentsLinesBetweenLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsLinesBetweenLabel") + ":";
        L10N_tableOfContentsLevelLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("levelLabel");
        L10N_tableOfContentsFirstLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("firstLineLabel") + ":";
        L10N_tableOfContentsRunoversLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("runoversLabel") + ":";
        L10N_tableOfContentsLineFillLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("lineFillSymbolLabel") + ":";
        L10N_tableOfContentsPrintPageNumbersLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsPrintPageNumbersLabel");
        L10N_tableOfContentsBraillePageNumbersLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableOfContentsBraillePageNumbersLabel");

        // Special Symbols Page
        
        L10N_specialSymbolsListLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsListLabel");
        L10N_specialSymbolsListTitleLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsListTitleLabel") + ":";
        L10N_specialSymbolsSymbolLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsSymbolLabel") + ":";
        L10N_specialSymbolsDescriptionLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsDescriptionLabel") + ":";
        L10N_specialSymbolsMode0Label = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsMode0Label");
        L10N_specialSymbolsMode1Label = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsMode1Label");
        L10N_specialSymbolsMode2Label = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsMode2Label");
        L10N_specialSymbolsMode3Label = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("specialSymbolsMode3Label");

        // Mathematics Page

        L10N_mathLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("formulasLabel") + ":";

        L10N_math.put("NEMETH",    "Nemeth");
        L10N_math.put("UKMATHS",   "UK Maths");
        L10N_math.put("MARBURG",   "Marburg");
        L10N_math.put("WISKUNDE",  "Woluwe");

        // Languages, translation tables, grades, alignment, paragraph styles

        L10N_grades.put(0, "Grade 0");  /* (computer Braille) */
        L10N_grades.put(1, "Grade 1");  /* (uncontracted)     */
        L10N_grades.put(2, "Grade 2");  /* (contracted)       */
        L10N_grades.put(3, "Grade 3");
        L10N_grades.put(4, "Grade 4");

        L10N_alignment.put(Alignment.LEFT,     ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("left"));
        L10N_alignment.put(Alignment.CENTERED, ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("center"));
        L10N_alignment.put(Alignment.RIGHT,    ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("right"));

        L10N_typeface.put(TypefaceOption.FOLLOW_PRINT, ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("followPrint"));
        L10N_typeface.put(TypefaceOption.YES,          ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("yes"));
        L10N_typeface.put(TypefaceOption.NO,           ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("no"));

        String key = null;
        String value = null;
        TreeSet treeSet = null;

        languages.remove(settings.getMainLanguage());
        for (int i=0;i<languages.size();i++) {
            key = languages.get(i);
            value = (new Locale(key.substring(0,key.indexOf("-")),key.substring(key.indexOf("-")+1,key.length()))).getDisplayName(oooLocale);
            L10N_languages.put(key, value);
        }

        for (int i=0;i<mainTranslationTables.size();i++) {
            key = mainTranslationTables.get(i);
            value = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("language_" + key);
            L10N_translationTables.put(key, value);
        }

        languages.clear();
        mainTranslationTables.clear();

        treeSet = new TreeSet(new Comparator() {
            public int compare(Object entry1, Object entry2) {
                return ((Comparable) ((Map.Entry) entry1).getValue()).compareTo(((Map.Entry) entry2).getValue());
            }
        });

        treeSet.addAll(L10N_languages.entrySet());
        for (Iterator i = treeSet.iterator(); i.hasNext();) {
            languages.add(((Map.Entry<String,String>)i.next()).getKey());
        }
        treeSet.clear();
        treeSet.addAll(L10N_translationTables.entrySet());
        for (Iterator i = treeSet.iterator(); i.hasNext();) {
            mainTranslationTables.add(((Map.Entry<String,String>)i.next()).getKey());
        }

        for (int i=0;i<specialTranslationTables.size();i++) {
            key = specialTranslationTables.get(i);
            value = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("language_" + key);
            L10N_translationTables.put(key, value);
        }

        allTranslationTables = new ArrayList();
        treeSet.clear();
        treeSet.addAll(L10N_translationTables.entrySet());
        for (Iterator i = treeSet.iterator(); i.hasNext();) {
            allTranslationTables.add(((Map.Entry<String,String>)i.next()).getKey());
        }

        treeSet = new TreeSet(new Comparator() {
            public int compare(Object style1, Object style2) {
                return ((Style) style1).compareTo(style2);
            }
        });

        paragraphStyles = new ArrayList();
        treeSet.addAll(settings.getParagraphStyles());
        ParagraphStyle style = null;
        for (Iterator i = treeSet.iterator(); i.hasNext();) {
            style = (ParagraphStyle)i.next();
            if (style.getName().equals("Standard")) {
                paragraphStyles.add(0, style);
            } else if (!style.getAutomatic()) {
                paragraphStyles.add(style);
            }
        }

        characterStyles = new ArrayList();
        treeSet.clear();
        treeSet.addAll(settings.getCharacterStyles());
        CharacterStyle style2 = null;
        for (Iterator i = treeSet.iterator(); i.hasNext();) {
            style2 = (CharacterStyle)i.next();
            if (style2.getName().equals("Default")) {
                characterStyles.add(0, style2);
            } else {
                characterStyles.add(style2);
            }
        }

        headingStyles = settings.getHeadingStyles();

        // Roadmap

        int roadMapWidth = 85;
        int roadMapHeight = 227;

        XMultiServiceFactory xMSFDialog = (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class, dialogControl.getModel());
        XNameContainer dialogNameContainer = (XNameContainer)UnoRuntime.queryInterface(XNameContainer.class, dialogControl.getModel());

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

        progressbar.increment();

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
        mainEightDotsCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_mainEightDotsCheckBox));
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
        hardPageBreaksCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_hardPageBreaksCheckBox));

        // Languages Page

        translationTableListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_translationTableListBox));
        gradeListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_gradeListBox));
        eightDotsCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_eightDotsCheckBox));
        languagesListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_languagesListBox));

        // Typeface Page

        characterStyleListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_characterStyleListBox));
        characterInheritCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_characterInheritCheckBox));
        characterParentField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_characterParentField));
        characterBoldfaceListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_characterBoldfaceListBox));
        characterItalicListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_characterItalicListBox));
        characterUnderlineListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_characterUnderlineListBox));
        characterCapitalsListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_characterCapitalsListBox));

        // Paragraphs Page

        paragraphStyleListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paragraphStyleListBox));
        paragraphInheritCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_paragraphInheritCheckBox));
        paragraphParentField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_paragraphParentField));
        paragraphAlignmentListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paragraphAlignmentListBox));
        paragraphFirstLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphFirstLineField));
        paragraphRunoversField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphRunoversField));
        paragraphMarginLeftRightField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphMarginLeftRightField));
        paragraphLinesAboveField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphLinesAboveField));
        paragraphLinesBelowField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphLinesBelowField));
        paragraphKeepEmptyCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_paragraphKeepEmptyCheckBox));
        paragraphKeepWithNextCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_paragraphKeepWithNextCheckBox));
        paragraphDontSplitCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_paragraphDontSplitCheckBox));
        paragraphWidowControlCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_paragraphWidowControlCheckBox));
        paragraphOrphanControlCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_paragraphOrphanControlCheckBox));
        paragraphWidowControlField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphWidowControlField));
        paragraphOrphanControlField  = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paragraphOrphanControlField));

        // Headings page

        headingLevelListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_headingLevelListBox));
        headingAlignmentListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_headingAlignmentListBox));
        headingFirstLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingFirstLineField));
        headingRunoversField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingRunoversField));
        headingMarginLeftRightField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingMarginLeftRightField));
        headingLinesAboveField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingLinesAboveField));
        headingLinesBelowField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_headingLinesBelowField));
        headingNewBraillePageCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_headingNewBraillePageCheckBox));
        headingKeepWithNextCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_headingKeepWithNextCheckBox));
        headingDontSplitCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_headingDontSplitCheckBox));

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
        listMarginLeftRightField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_listMarginLeftRightField));
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
        tableMarginLeftRightField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_tableMarginLeftRightField));
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
        beginningBraillePageNumberField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_beginningBraillePageNumberField));
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
        tableOfContentsPrintPageNumbersCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_tableOfContentsPrintPageNumbersCheckBox));
        tableOfContentsBraillePageNumbersCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_tableOfContentsBraillePageNumbersCheckBox));

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

        // PROPERTIES

        // Main Window

        backButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, backButton)).getModel());
        nextButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, nextButton)).getModel());

        // General Page

        mainEightDotsCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, mainEightDotsCheckBox)).getModel());
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

        // Languages Page

        gradeListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, gradeListBox)).getModel());
        eightDotsCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, eightDotsCheckBox)).getModel());

        // Typeface Page

        characterInheritCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, characterInheritCheckBox)).getModel());
        characterParentFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, characterParentField)).getModel());
        characterBoldfaceListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, characterBoldfaceListBox)).getModel());
        characterItalicListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, characterItalicListBox)).getModel());
        characterUnderlineListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, characterUnderlineListBox)).getModel());
        characterCapitalsListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, characterCapitalsListBox)).getModel());

        // Paragraphs Page

        paragraphInheritCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphInheritCheckBox)).getModel());
        paragraphParentFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphParentField)).getModel());
        paragraphFirstLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphFirstLineField)).getModel());
        paragraphRunoversFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphRunoversField)).getModel());
        paragraphMarginLeftRightFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphMarginLeftRightField)).getModel());
        paragraphAlignmentListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphAlignmentListBox)).getModel());
        paragraphLinesAboveProperties =(XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphLinesAboveField)).getModel());
        paragraphLinesBelowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphLinesBelowField)).getModel());
        paragraphKeepEmptyCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphKeepEmptyCheckBox)).getModel());
        paragraphKeepWithNextCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphKeepWithNextCheckBox)).getModel());
        paragraphDontSplitCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphDontSplitCheckBox)).getModel());
        paragraphWidowControlCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphWidowControlCheckBox)).getModel());
        paragraphOrphanControlCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphOrphanControlCheckBox)).getModel());
        paragraphWidowControlFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphWidowControlField)).getModel());
        paragraphOrphanControlFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paragraphOrphanControlField)).getModel());

        // Headings Page

        headingFirstLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingFirstLineField)).getModel());
        headingRunoversFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingRunoversField)).getModel());
        headingMarginLeftRightFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingMarginLeftRightField)).getModel());
        headingAlignmentListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingAlignmentListBox)).getModel());
        headingLinesAboveProperties =(XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingLinesAboveField)).getModel());
        headingLinesBelowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingLinesBelowField)).getModel());
        headingDontSplitCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, headingDontSplitCheckBox)).getModel());

        // Lists Page

        listFirstLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listFirstLineField)).getModel());
        listRunoversFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listRunoversField)).getModel());
        listMarginLeftRightFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, listMarginLeftRightField)).getModel());
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
        tableMarginLeftRightFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableMarginLeftRightField)).getModel());
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
        beginningBraillePageNumberFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, beginningBraillePageNumberField)).getModel());
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
        tableOfContentsPrintPageNumbersCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsPrintPageNumbersCheckBox)).getModel());
        tableOfContentsBraillePageNumbersCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableOfContentsBraillePageNumbersCheckBox)).getModel());

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

        setDialogValues();
        addListeners();
        setLabels();

        logger.exiting("SettingsDialog", "initialise");

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

        addGeneralPageListeners();
        addLanguagesPageListeners();
        addTypefacePageListeners();
        addParagraphsPageListeners();
        addHeadingsPageListeners();
        addListsPageListeners();
        addTablesPageListeners();
        addPageNumbersPageListeners();
        addTableOfContentsPageListeners();
        addSpecialSymbolsPageListeners();

    }

    private void addGeneralPageListeners() {

        transcriptionInfoCheckBox.addItemListener(this);
        volumeInfoCheckBox.addItemListener(this);
        transcribersNotesPageCheckBox.addItemListener(this);
        mainTranslationTableListBox.addItemListener(this);
        mainGradeListBox.addItemListener(this);

    }

    private void addLanguagesPageListeners() {

        languagesListBox.addItemListener(this);
        translationTableListBox.addItemListener(this);
        gradeListBox.addItemListener(this);
        eightDotsCheckBox.addItemListener(this);

    }

    private void addTypefacePageListeners() {

        characterStyleListBox.addItemListener(this);
        characterInheritCheckBox.addItemListener(this);

    }

    private void addParagraphsPageListeners() {

        paragraphAlignmentListBox.addItemListener(this);
        paragraphStyleListBox.addItemListener(this);
        paragraphInheritCheckBox.addItemListener(this);
        paragraphKeepWithNextCheckBox.addItemListener(this);
        paragraphDontSplitCheckBox.addItemListener(this);

    }

    private void addHeadingsPageListeners() {

        headingAlignmentListBox.addItemListener(this);
        headingLevelListBox.addItemListener(this);
        headingKeepWithNextCheckBox.addItemListener(this);

    }

    private void addListsPageListeners() {

        listAlignmentListBox.addItemListener(this);
        listLevelListBox.addItemListener(this);

    }

    private void addTablesPageListeners() {

        tableSimpleRadioButton.addItemListener(this);
        tableStairstepRadioButton.addItemListener(this);
        tableColumnListBox.addItemListener(this);
        tableAlignmentListBox.addItemListener(this);

    }

    private void addPageNumbersPageListeners() {

        braillePageNumbersCheckBox.addItemListener(this);
        braillePageNumberAtListBox.addItemListener(this);
        printPageNumbersCheckBox.addItemListener(this);
        printPageNumberAtListBox.addItemListener(this);
        printPageNumberRangeCheckBox.addItemListener(this);
        pageSeparatorCheckBox.addItemListener(this);

    }

    private void addTableOfContentsPageListeners() {

        tableOfContentsCheckBox.addItemListener(this);
        tableOfContentsLevelListBox.addItemListener(this);

    }

    private void addSpecialSymbolsPageListeners() {

        specialSymbolsListCheckBox.addItemListener(this);
        specialSymbolsListBox.addItemListener(this);
        specialSymbolsMode0RadioButton.addItemListener(this);
        specialSymbolsMode1RadioButton.addItemListener(this);
        specialSymbolsMode2RadioButton.addItemListener(this);
        specialSymbolsMode3RadioButton.addItemListener(this);

    }

    private void removeGeneralPageListeners() {

        transcriptionInfoCheckBox.removeItemListener(this);
        volumeInfoCheckBox.removeItemListener(this);
        transcribersNotesPageCheckBox.removeItemListener(this);
        mainTranslationTableListBox.removeItemListener(this);
        mainGradeListBox.removeItemListener(this);

    }

    private void removeLanguagesPageListeners() {

        languagesListBox.removeItemListener(this);
        translationTableListBox.removeItemListener(this);
        gradeListBox.removeItemListener(this);
        eightDotsCheckBox.removeItemListener(this);

    }

    private void removeTypefacePageListeners() {

        characterStyleListBox.removeItemListener(this);
        characterInheritCheckBox.removeItemListener(this);

    }

    private void removeParagraphsPageListeners() {

        paragraphAlignmentListBox.removeItemListener(this);
        paragraphStyleListBox.removeItemListener(this);
        paragraphInheritCheckBox.removeItemListener(this);
        paragraphKeepWithNextCheckBox.removeItemListener(this);
        paragraphDontSplitCheckBox.removeItemListener(this);

    }

    private void removeHeadingsPageListeners() {

        headingAlignmentListBox.removeItemListener(this);
        headingLevelListBox.removeItemListener(this);
        headingKeepWithNextCheckBox.removeItemListener(this);

    }

    private void removeListsPageListeners() {

        listAlignmentListBox.removeItemListener(this);
        listLevelListBox.removeItemListener(this);

    }

    private void removeTablesPageListeners() {

        tableSimpleRadioButton.removeItemListener(this);
        tableStairstepRadioButton.removeItemListener(this);
        tableColumnListBox.removeItemListener(this);
        tableAlignmentListBox.removeItemListener(this);

    }

    private void removePageNumbersPageListeners() {

        braillePageNumbersCheckBox.removeItemListener(this);
        braillePageNumberAtListBox.removeItemListener(this);
        printPageNumbersCheckBox.removeItemListener(this);
        printPageNumberAtListBox.removeItemListener(this);
        printPageNumberRangeCheckBox.removeItemListener(this);
        pageSeparatorCheckBox.removeItemListener(this);

    }

    private void removeTableOfContentsPageListeners() {

        tableOfContentsCheckBox.removeItemListener(this);
        tableOfContentsLevelListBox.removeItemListener(this);

    }

    private void removeSpecialSymbolsPageListeners() {

        specialSymbolsListCheckBox.removeItemListener(this);
        specialSymbolsListBox.removeItemListener(this);
        specialSymbolsMode0RadioButton.removeItemListener(this);
        specialSymbolsMode1RadioButton.removeItemListener(this);
        specialSymbolsMode2RadioButton.removeItemListener(this);
        specialSymbolsMode3RadioButton.removeItemListener(this);

    }

    /**
     * The dialog is executed. It is disposed when the user presses OK or Cancel.
     *
     * @return          <code>true</code> if the user pressed OK;
     *                  <code>false</code> if the user pressed Cancel.
     */
    public boolean execute() throws com.sun.star.uno.Exception {

        logger.entering("SettingsDialog", "execute");

        short ret = dialog.execute();

        getDialogValues();
        
        logger.exiting("SettingsDialog", "execute");

        if (ret == ((short) PushButtonType.OK_value)) {
            return true;
        } else {
            return false;
        }
    }

    public void dispose() {

        if (dialogComponent != null) {
            dialogComponent.dispose();
        }
    }

    /**
     * Set the dialog labels.
     *
     * @throws com.sun.star.uno.Exception
     */
    private void setLabels() throws com.sun.star.uno.Exception {

        XFixedText xFixedText = null;

        windowProperties.setPropertyValue("Title", L10N_windowTitle);

        cancelButton.setLabel(L10N_cancelButton);
        backButton.setLabel(L10N_backButton);
        nextButton.setLabel(L10N_nextButton);
        okButton.setLabel(L10N_okButton);

        tableColumnDelimiterButton.setLabel(L10N_tableColumnDelimiterButton);
        tableOfContentsLineFillButton.setLabel(L10N_tableOfContentsLineFillButton);
        listPrefixButton.setLabel(L10N_listPrefixButton);
        specialSymbolsSymbolButton.setLabel(L10N_specialSymbolsSymbolButton);

        // General Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mainTranslationTableLabel));
        xFixedText.setText(L10N_mainTranslationTableLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mainGradeLabel));
        xFixedText.setText(L10N_mainGradeLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mainEightDotsLabel));
        xFixedText.setText(L10N_mainEightDotsLabel);
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
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_hardPageBreaksLabel));
        xFixedText.setText(L10N_hardPageBreaksLabel);

        // Languages Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_translationTableLabel));
        xFixedText.setText(L10N_translationTableLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_gradeLabel));
        xFixedText.setText(L10N_gradeLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_eightDotsLabel));
        xFixedText.setText(L10N_eightDotsLabel);

        // Typeface Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_characterStyleLabel));
        xFixedText.setText(L10N_characterStyleLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_characterInheritLabel));
        xFixedText.setText(L10N_characterInheritLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_characterBoldfaceLabel));
        xFixedText.setText(L10N_characterBoldfaceLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_characterItalicLabel));
        xFixedText.setText(L10N_characterItalicLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_characterUnderlineLabel));
        xFixedText.setText(L10N_characterUnderlineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_characterCapitalsLabel));
        xFixedText.setText(L10N_characterCapitalsLabel);

        // Paragraphs Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphStyleLabel));
        xFixedText.setText(L10N_paragraphStyleLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphInheritLabel));
        xFixedText.setText(L10N_paragraphInheritLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphAlignmentLabel));
        xFixedText.setText(L10N_paragraphAlignmentLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphFirstLineLabel));
        xFixedText.setText(L10N_paragraphFirstLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphRunoversLabel));
        xFixedText.setText(L10N_paragraphRunoversLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphMarginLeftRightLabel));
        xFixedText.setText(L10N_paragraphMarginLeftRightLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphLinesAboveLabel));
        xFixedText.setText(L10N_paragraphLinesAboveLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphLinesBelowLabel));
        xFixedText.setText(L10N_paragraphLinesBelowLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphKeepEmptyLabel));
        xFixedText.setText(L10N_paragraphKeepEmptyLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphKeepWithNextLabel));
        xFixedText.setText(L10N_paragraphKeepWithNextLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphDontSplitLabel));
        xFixedText.setText(L10N_paragraphDontSplitLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphWidowControlLabel));
        xFixedText.setText(L10N_paragraphWidowControlLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paragraphOrphanControlLabel));
        xFixedText.setText(L10N_paragraphOrphanControlLabel);

        // Headings page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingLevelLabel));
        xFixedText.setText(L10N_headingLevelLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingAlignmentLabel));
        xFixedText.setText(L10N_headingAlignmentLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingFirstLineLabel));
        xFixedText.setText(L10N_headingFirstLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingRunoversLabel));
        xFixedText.setText(L10N_headingRunoversLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingMarginLeftRightLabel));
        xFixedText.setText(L10N_headingMarginLeftRightLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingLinesAboveLabel));
        xFixedText.setText(L10N_headingLinesAboveLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingLinesBelowLabel));
        xFixedText.setText(L10N_headingLinesBelowLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingNewBraillePageLabel));
        xFixedText.setText(L10N_headingNewBraillePageLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingKeepWithNextLabel));
        xFixedText.setText(L10N_headingKeepWithNextLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_headingDontSplitLabel));
        xFixedText.setText(L10N_headingDontSplitLabel);

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
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_listMarginLeftRightLabel));
        xFixedText.setText(L10N_listMarginLeftRightLabel);
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
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableMarginLeftRightLabel));
        xFixedText.setText(L10N_tableMarginLeftRightLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableColumnDelimiterLabel));
        xFixedText.setText(L10N_tableColumnDelimiterLabel);

        // Pagenumbers Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_braillePageNumbersLabel));
        xFixedText.setText(L10N_braillePageNumbersLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_braillePageNumberAtLabel));
        xFixedText.setText(L10N_braillePageNumberAtLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_preliminaryPageNumberFormatLabel));
        xFixedText.setText(L10N_preliminaryPageNumberFormatLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_beginningBraillePageNumberLabel));
        xFixedText.setText(L10N_beginningBraillePageNumberLabel);
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
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsPrintPageNumbersLabel));
        xFixedText.setText(L10N_tableOfContentsPrintPageNumbersLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableOfContentsBraillePageNumbersLabel));
        xFixedText.setText(L10N_tableOfContentsBraillePageNumbersLabel);

        // Special Symbols Page

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsListLabel));
        xFixedText.setText(L10N_specialSymbolsListLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_specialSymbolsListTitleLabel));
        xFixedText.setText(L10N_specialSymbolsListTitleLabel);
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

    }

    /**
     * Set the initial dialog values and field properties.
     *
     */
    private void setDialogValues() throws com.sun.star.uno.Exception {

        if (pagesEnabled[GENERAL_PAGE-1]) {

            transcribersNotesPageCheckBoxProperties.setPropertyValue("Enabled", settings.getPreliminaryPagesPresent());
            preliminaryVolumeCheckBoxProperties.setPropertyValue("Enabled", settings.getPreliminaryPagesPresent());
            transcriptionInfoCheckBoxProperties.setPropertyValue("Enabled", settings.getTranscriptionInfoAvailable());
            volumeInfoCheckBoxProperties.setPropertyValue("Enabled", settings.getVolumeInfoAvailable());

            creatorField.setText(settings.getCreator());
            transcribersNotesPageField.setText(settings.getTranscribersNotesPageTitle());
            transcriptionInfoCheckBox.setState((short)(settings.getTranscriptionInfoEnabled()?1:0));
            volumeInfoCheckBox.setState((short)(settings.getVolumeInfoEnabled()?1:0));
            transcribersNotesPageCheckBox.setState((short)(settings.getTranscribersNotesPageEnabled()?1:0));
            preliminaryVolumeCheckBox.setState((short)(settings.getPreliminaryVolumeEnabled()?1:0));
            hyphenateCheckBox.setState((short)(settings.getHyphenate()?1:0));
            hardPageBreaksCheckBox.setState((short)(settings.getHardPageBreaks()?1:0));

            for (int i=0;i<mainTranslationTables.size();i++) {
                mainTranslationTableListBox.addItem(L10N_translationTables.get(mainTranslationTables.get(i)), (short)i);
            }

            updateMainTranslationTableListBox();
            updateMainGradeListBox();
            updateMainEightDotsCheckBox();
            updateGeneralPageFieldProperties();

        }

        if (pagesEnabled[LANGUAGES_PAGE-1]) {

            for (int i=0;i<languages.size();i++) {
                languagesListBox.addItem(L10N_languages.get(languages.get(i)), (short)i);
            }

            for (int i=0;i<allTranslationTables.size();i++) {
                translationTableListBox.addItem(L10N_translationTables.get(allTranslationTables.get(i)), (short)i);
            }

            selectedLanguagePos = 0;
            languagesListBox.selectItemPos((short) selectedLanguagePos, true);
            updateTranslationTableListBox();
            updateGradeListBox();
            updateEightDotsCheckBox();

            updateLanguagesPageFieldProperties();

        }

        if (pagesEnabled[TYPEFACE_PAGE-1]) {

            for (int i=0; i<characterStyles.size(); i++) {
                characterStyleListBox.addItem(characterStyles.get(i).getDisplayName(), (short)i);
            }
            selectedCharacterStylePos = 0;
            characterStyleListBox.selectItemPos((short) selectedCharacterStylePos, true);

            for (int i=0; i<typefaceOptions.size(); i++) {
                characterBoldfaceListBox.addItem(L10N_typeface.get(typefaceOptions.get(i)), (short)i);
                characterItalicListBox.addItem(L10N_typeface.get(typefaceOptions.get(i)), (short)i);
                characterUnderlineListBox.addItem(L10N_typeface.get(typefaceOptions.get(i)), (short)i);
                characterCapitalsListBox.addItem(L10N_typeface.get(typefaceOptions.get(i)), (short)i);
            }

            characterParentFieldProperties.setPropertyValue("Enabled", false);

            updateTypefacePageFieldValues();
            updateTypefacePageFieldProperties();

        }

        if (pagesEnabled[PARAGRAPHS_PAGE-1]) {

            for (int i=0; i<paragraphStyles.size(); i++) {
                paragraphStyleListBox.addItem(paragraphStyles.get(i).getDisplayName(), (short)i);
            }

            selectedParagraphStylePos = 0;
            paragraphStyleListBox.selectItemPos((short) selectedParagraphStylePos, true);

            for (int i=0; i<alignmentOptions.size(); i++) {
                paragraphAlignmentListBox.addItem(L10N_alignment.get(alignmentOptions.get(i)), (short)i);
            }
            
            paragraphFirstLineField.setDecimalDigits((short)0);
            paragraphFirstLineField.setMin((double)0);
            paragraphFirstLineField.setMax((double)Integer.MAX_VALUE);            

            paragraphRunoversField.setDecimalDigits((short)0);
            paragraphRunoversField.setMin((double)0);
            paragraphRunoversField.setMax((double)Integer.MAX_VALUE);

            paragraphMarginLeftRightField.setDecimalDigits((short)0);
            paragraphMarginLeftRightField.setMin((double)0);
            paragraphMarginLeftRightField.setMax((double)Integer.MAX_VALUE);

            paragraphLinesAboveField.setDecimalDigits((short)0);
            paragraphLinesAboveField.setMin((double)0);
            paragraphLinesAboveField.setMax((double)Integer.MAX_VALUE);

            paragraphLinesBelowField.setDecimalDigits((short)0);
            paragraphLinesBelowField.setMin((double)0);
            paragraphLinesBelowField.setMax((double)Integer.MAX_VALUE);

            paragraphWidowControlField.setDecimalDigits((short)0);
            paragraphWidowControlField.setMin((double)2);
            paragraphWidowControlField.setMax((double)Integer.MAX_VALUE);

            paragraphOrphanControlField.setDecimalDigits((short)0);
            paragraphOrphanControlField.setMin((double)2);
            paragraphOrphanControlField.setMax((double)Integer.MAX_VALUE);

            paragraphParentFieldProperties.setPropertyValue("Enabled", false);

            updateParagraphsPageFieldValues();
            updateParagraphsPageFieldProperties();

        }

        if (pagesEnabled[HEADINGS_PAGE-1]) {

            selectedHeadingStylePos = 0;

            for (int i=0;i<4;i++) { headingLevelListBox.addItem((i<3)?String.valueOf(i+1):"4-10", (short)i); }
            headingLevelListBox.selectItemPos((short)selectedHeadingStylePos, true);

            for (int i=0; i<alignmentOptions.size(); i++) {
                headingAlignmentListBox.addItem(L10N_alignment.get(alignmentOptions.get(i)), (short)i);
            }

            headingFirstLineField.setDecimalDigits((short)0);
            headingFirstLineField.setMin((double)0);
            headingFirstLineField.setMax((double)Integer.MAX_VALUE);

            headingRunoversField.setDecimalDigits((short)0);
            headingRunoversField.setMin((double)0);
            headingRunoversField.setMax((double)Integer.MAX_VALUE);

            headingMarginLeftRightField.setDecimalDigits((short)0);
            headingMarginLeftRightField.setMin((double)0);
            headingMarginLeftRightField.setMax((double)Integer.MAX_VALUE);

            headingLinesAboveField.setDecimalDigits((short)0);
            headingLinesAboveField.setMin((double)0);
            headingLinesAboveField.setMax((double)Integer.MAX_VALUE);

            headingLinesBelowField.setDecimalDigits((short)0);
            headingLinesBelowField.setMin((double)0);
            headingLinesBelowField.setMax((double)Integer.MAX_VALUE);
            
            updateHeadingsPageFieldValues();
            updateHeadingsPageFieldProperties();

        }

        if (pagesEnabled[LISTS_PAGE-1]) {

            currentListLevel = 1;

            listLinesAboveField.setDecimalDigits((short)0);
            listLinesAboveField.setMin((double)0);
            listLinesAboveField.setMax((double)Integer.MAX_VALUE);
            
            listLinesBelowField.setDecimalDigits((short)0);
            listLinesBelowField.setMin((double)0);
            listLinesBelowField.setMax((double)Integer.MAX_VALUE);

            listLinesBetweenField.setDecimalDigits((short)0);
            listLinesBetweenField.setMin((double)0);
            listLinesBetweenField.setMax((double)Integer.MAX_VALUE);

            for (int i=0;i<10;i++) { listLevelListBox.addItem(String.valueOf(i+1), (short)i);}
            listLevelListBox.selectItemPos((short)(currentListLevel-1), true);

            for (int i=0; i<alignmentOptions.size(); i++) {
                listAlignmentListBox.addItem(L10N_alignment.get(alignmentOptions.get(i)), (short)i);
            }

            listFirstLineField.setDecimalDigits((short)0);
            listFirstLineField.setMin((double)0);
            listFirstLineField.setMax((double)Integer.MAX_VALUE);

            listRunoversField.setDecimalDigits((short)0);
            listRunoversField.setMin((double)0);
            listRunoversField.setMax((double)Integer.MAX_VALUE);

            listMarginLeftRightField.setDecimalDigits((short)0);
            listMarginLeftRightField.setMin((double)0);
            listMarginLeftRightField.setMax((double)Integer.MAX_VALUE);

            updateListsPageFieldValues();
            updateListsPageFieldProperties();

        }

        if (pagesEnabled[TABLES_PAGE-1]) {

            currentTableColumn = settings.stairstepTableIsEnabled()?1:0;

            tableSimpleRadioButton.setState(!settings.stairstepTableIsEnabled());
            tableStairstepRadioButton.setState(settings.stairstepTableIsEnabled());

            tableLinesAboveField.setDecimalDigits((short)0);
            tableLinesAboveField.setMin((double)0);
            tableLinesAboveField.setMax((double)Integer.MAX_VALUE);
            tableLinesAboveField.setValue((double)settings.getStyle("table").getLinesAbove());

            tableLinesBelowField.setDecimalDigits((short)0);
            tableLinesBelowField.setMin((double)0);
            tableLinesBelowField.setMax((double)Integer.MAX_VALUE);
            tableLinesBelowField.setValue((double)settings.getStyle("table").getLinesBelow());

            tableLinesBetweenField.setDecimalDigits((short)0);
            tableLinesBetweenField.setMin((double)0);
            tableLinesBetweenField.setMax((double)Integer.MAX_VALUE);
            tableLinesBetweenField.setValue((double)settings.getStyle("table").getLinesBetween());

            for (int i=0;i<9;i++) { tableColumnListBox.addItem(String.valueOf(i+1), (short)i); }
            tableColumnListBox.addItem("\u226510", (short)9);
            tableColumnListBox.selectItemPos((short)(currentTableColumn-1), true);

            for (int i=0; i<alignmentOptions.size(); i++) {
                tableAlignmentListBox.addItem(L10N_alignment.get(alignmentOptions.get(i)), (short)i);
            }

            tableFirstLineField.setDecimalDigits((short)0);
            tableFirstLineField.setMin((double)0);
            tableFirstLineField.setMax((double)Integer.MAX_VALUE);

            tableRunoversField.setDecimalDigits((short)0);
            tableRunoversField.setMin((double)0);
            tableRunoversField.setMax((double)Integer.MAX_VALUE);

            tableMarginLeftRightField.setDecimalDigits((short)0);
            tableMarginLeftRightField.setMin((double)0);
            tableMarginLeftRightField.setMax((double)Integer.MAX_VALUE);

            tableColumnDelimiterField.setText(settings.getColumnDelimiter());

            updateTablesPageFieldValues();
            updateTablesPageFieldProperties();

        }

        if (pagesEnabled[PAGENUMBERS_PAGE-1]) {

            braillePageNumberAtListBox.addItem(L10N_top, (short)0);
            braillePageNumberAtListBox.addItem(L10N_bottom, (short)1);
            preliminaryPageNumberFormatListBox.addItem("p1,p2,p3,...", (short)0);
            preliminaryPageNumberFormatListBox.addItem("i,ii,iii,...", (short)1);
            beginningBraillePageNumberField.setDecimalDigits((short)0);
            beginningBraillePageNumberField.setMin((double)1);
            beginningBraillePageNumberField.setMax((double)Integer.MAX_VALUE);
            beginningBraillePageNumberField.setValue((double)settings.getBeginningBraillePageNumber());
            printPageNumberAtListBox.addItem(L10N_top, (short)0);
            printPageNumberAtListBox.addItem(L10N_bottom, (short)1);

            updatePageNumbersPageFieldValues();
            updatePageNumbersPageFieldProperties();

        }

        if (pagesEnabled[TOC_PAGE-1]) {

            currentTableOfContentsLevel = 1;

            tableOfContentsCheckBox.setState((short)(settings.getTableOfContentEnabled()?1:0));
            tableOfContentsCheckBoxProperties.setPropertyValue("Enabled", settings.getPreliminaryPagesPresent());
            tableOfContentsTitleField.setText(settings.getTableOfContentTitle());
            tableOfContentsLineFillField.setText(String.valueOf(settings.getLineFillSymbol()));
            tableOfContentsPrintPageNumbersCheckBox.setState((short)(settings.getPrintPageNumbersInToc()?1:0));

            tableOfContentsLinesBetweenField.setDecimalDigits((short)0);
            tableOfContentsLinesBetweenField.setMin((double)0);
            tableOfContentsLinesBetweenField.setMax((double)Integer.MAX_VALUE);
            tableOfContentsLinesBetweenField.setValue((double)settings.getStyle("toc").getLinesBetween());

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

            specialSymbolsListCheckBoxProperties.setPropertyValue("Enabled", settings.getPreliminaryPagesPresent());
            specialSymbolsListField.setText(settings.getSpecialSymbolsListTitle());
            specialSymbolsListCheckBox.setState((short)(settings.getSpecialSymbolsListEnabled()?1:0));

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

        brailleRulesListBox.addItem("Custom", (short)0);
        brailleRulesListBox.addItem("BANA",   (short)1);
        brailleRulesListBox.selectItemPos((short)((settings.getBrailleRules()==BrailleRules.CUSTOM)?0:1), true);

        setPage(GENERAL_PAGE);
        windowProperties.setPropertyValue("Step", 0);
        windowProperties.setPropertyValue("Step", currentPage);
        roadmapProperties.setPropertyValue("Complete", true);
        roadmapProperties.setPropertyValue("CurrentItemID", (short)currentPage);

    }

    /**
     * Read the dialog values set by the user (that have not been previously read).
     *
     */
    private void getDialogValues() {

        if (pagesVisited[GENERAL_PAGE-1]) {

            settings.setDots((mainEightDotsCheckBox.getState()==(short)1)?8:6, settings.getMainLanguage());
            settings.setCreator(creatorField.getText());
            settings.setTranscribersNotesPageTitle(transcribersNotesPageField.getText());
            settings.setTranscriptionInfoEnabled(transcriptionInfoCheckBox.getState() == (short) 1);
            settings.setVolumeInfoEnabled(volumeInfoCheckBox.getState() == (short) 1);
            settings.setTranscribersNotesPageEnabled(transcribersNotesPageCheckBox.getState() == (short) 1);
            settings.setPreliminaryVolumeEnabled(preliminaryVolumeCheckBox.getState() == (short) 1);
            settings.setHyphenate(hyphenateCheckBox.getState() == (short) 1);
            settings.setHardPageBreaks(hardPageBreaksCheckBox.getState() == (short) 1);

        }

        if (pagesVisited[LANGUAGES_PAGE-1]) {}

        if (pagesVisited[TYPEFACE_PAGE-1]) { saveTypefacePageFieldValues(); }

        if (pagesVisited[PARAGRAPHS_PAGE-1]) { saveParagraphsPageFieldValues(); }

        if (pagesVisited[HEADINGS_PAGE-1]) { saveHeadingsPageFieldValues(); }

        if (pagesVisited[LISTS_PAGE-1]) { saveListsPageFieldValues(); }

        if (pagesVisited[TABLES_PAGE-1]) {

            Style style = settings.getStyle("table");

            style.setLinesAbove((int)tableLinesAboveField.getValue());
            style.setLinesBelow((int)tableLinesBelowField.getValue());
            style.setLinesBetween((int)tableLinesBetweenField.getValue());
            saveTablesPageFieldValues();

        }

        if (pagesVisited[PAGENUMBERS_PAGE-1]) {

            settings.setPreliminaryPageFormat(((preliminaryPageNumberFormatListBox.getSelectedItemPos() == (short)0)?
                PageNumberFormat.P:PageNumberFormat.ROMAN));
            settings.setBeginningBraillePageNumber((int)beginningBraillePageNumberField.getValue());
            settings.setContinuePages(continuePagesCheckBox.getState() == (short) 1);
            settings.setPageSeparatorNumber(pageSeparatorNumberCheckBox.getState() == (short) 1);
            settings.setIgnoreEmptyPages(ignoreEmptyPagesCheckBox.getState() == (short) 1);
            settings.setMergeUnnumberedPages(mergeUnnumberedPagesCheckBox.getState() == (short) 1);
            settings.setPageNumberAtTopOnSeparateLine(numbersAtTopOnSepLineCheckBox.getState() == (short) 1);
            settings.setPageNumberAtBottomOnSeparateLine(numbersAtBottomOnSepLineCheckBox.getState() == (short) 1);

        }

        if (pagesVisited[TOC_PAGE-1]) {

            settings.setTableOfContentTitle(tableOfContentsTitleField.getText());
            settings.setPrintPageNumbersInToc(tableOfContentsPrintPageNumbersCheckBox.getState()==(short)1);
            settings.setBraillePageNumbersInToc(tableOfContentsBraillePageNumbersCheckBox.getState()==(short)1);
            settings.getStyle("toc").setLinesBetween((int)tableOfContentsLinesBetweenField.getValue());            
            saveTableOfContentsPageFieldValues();

        }

        if (pagesVisited[SPECIAL_SYMBOLS_PAGE-1]){

            settings.setSpecialSymbolsListTitle(specialSymbolsListField.getText());
            settings.setSpecialSymbolsListEnabled(specialSymbolsListCheckBox.getState() == (short) 1);
            saveSpecialSymbolsPageFieldValues();

        }

        if (pagesVisited[MATH_PAGE-1]) {

            settings.setMath(mathTypes.get(mathListBox.getSelectedItemPos()));

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

        mainEightDotsCheckBoxProperties.setPropertyValue("Enabled", settings.getSupportedDots(settings.getMainLanguage()).size()>1);
        creatorFieldProperties.setPropertyValue("Enabled", settings.getTranscriptionInfoEnabled());
        transcribersNotesPageFieldProperties.setPropertyValue("Enabled", settings.getTranscribersNotesPageEnabled());

    }

    private void updateLanguagesPageFieldProperties() throws com.sun.star.uno.Exception {

        gradeListBoxProperties.setPropertyValue("Enabled", settings.getGrade(languages.get(selectedLanguagePos)) > -1);
        eightDotsCheckBoxProperties.setPropertyValue("Enabled", settings.getSupportedDots(languages.get(selectedLanguagePos)).size() > 1);

    }

    private void updateTypefacePageFieldProperties() throws com.sun.star.uno.Exception {

        CharacterStyle style = characterStyles.get(selectedCharacterStylePos);

        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);
        boolean inherit = style.getInherit();

        characterInheritCheckBoxProperties.setPropertyValue("Enabled", style.getParentStyle() != null && !bana);
        characterBoldfaceListBoxProperties.setPropertyValue("Enabled", !bana && !inherit);
        characterItalicListBoxProperties.setPropertyValue("Enabled", !bana && !inherit);
        characterUnderlineListBoxProperties.setPropertyValue("Enabled", !bana && !inherit);
        characterCapitalsListBoxProperties.setPropertyValue("Enabled", !bana && !inherit);

    }

    private void updateParagraphsPageFieldProperties() throws com.sun.star.uno.Exception {

        ParagraphStyle style = paragraphStyles.get(selectedParagraphStylePos);
        
        boolean left = (style.getAlignment() == Alignment.LEFT);
        boolean centered = (style.getAlignment() == Alignment.CENTERED);
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);
        boolean inherit = style.getInherit();

        paragraphInheritCheckBoxProperties.setPropertyValue("Enabled", style.getParentStyle() != null && !bana);
        paragraphFirstLineFieldProperties.setPropertyValue("Enabled", left && !bana && !inherit);
        paragraphRunoversFieldProperties.setPropertyValue("Enabled", left && !bana && !inherit);
        paragraphMarginLeftRightFieldProperties.setPropertyValue("Enabled", centered && !bana && !inherit);
        paragraphAlignmentListBoxProperties.setPropertyValue("Enabled", !bana && !inherit);
        paragraphLinesAboveProperties.setPropertyValue("Enabled", !bana && !inherit);
        paragraphLinesBelowProperties.setPropertyValue("Enabled", !bana && !inherit);
        paragraphKeepEmptyCheckBoxProperties.setPropertyValue("Enabled", !inherit);
        paragraphKeepWithNextCheckBoxProperties.setPropertyValue("Enabled", !inherit);
        paragraphDontSplitCheckBoxProperties.setPropertyValue("Enabled", !inherit && !style.getKeepWithNext());
        paragraphWidowControlCheckBoxProperties.setPropertyValue("Enabled", !inherit && !style.getDontSplit());
        paragraphOrphanControlCheckBoxProperties.setPropertyValue("Enabled", !inherit && !style.getDontSplit());
        paragraphWidowControlFieldProperties.setPropertyValue("Enabled", !inherit && !style.getDontSplit() && style.getWidowControl() > 1);
        paragraphOrphanControlFieldProperties.setPropertyValue("Enabled", !inherit && !style.getDontSplit()&& style.getOrphanControl() > 1);

    }

    private void updateHeadingsPageFieldProperties() throws com.sun.star.uno.Exception {

        HeadingStyle style = headingStyles.get(selectedHeadingStylePos);

        boolean left = (style.getAlignment() == Alignment.LEFT);
        boolean centered = (style.getAlignment() == Alignment.CENTERED);
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        headingFirstLineFieldProperties.setPropertyValue("Enabled", left && !bana);
        headingRunoversFieldProperties.setPropertyValue("Enabled", left && !bana);
        headingMarginLeftRightFieldProperties.setPropertyValue("Enabled", centered && !bana);
        headingAlignmentListBoxProperties.setPropertyValue("Enabled", !bana);
        headingLinesAboveProperties.setPropertyValue("Enabled", !bana);
        headingLinesBelowProperties.setPropertyValue("Enabled", !bana);
        headingDontSplitCheckBoxProperties.setPropertyValue("Enabled", !style.getKeepWithNext());

    }

    private void updateListsPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean left = (settings.getStyle("list_" + currentListLevel).getAlignment() == Alignment.LEFT);
        boolean centered = (settings.getStyle("list_" + currentListLevel).getAlignment() == Alignment.CENTERED);
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        listFirstLineFieldProperties.setPropertyValue("Enabled", left && !bana);
        listRunoversFieldProperties.setPropertyValue("Enabled", left && !bana);
        listMarginLeftRightFieldProperties.setPropertyValue("Enabled", centered && !bana);
        listAlignmentListBoxProperties.setPropertyValue("Enabled", !bana);
        listLinesAboveProperties.setPropertyValue("Enabled", !bana);
        listLinesBelowProperties.setPropertyValue("Enabled", !bana);
        listLinesBetweenProperties.setPropertyValue("Enabled", !bana);

    }

    private void updateTablesPageFieldProperties() throws com.sun.star.uno.Exception {

        tableColumnListBoxProperties.setPropertyValue("Enabled", settings.stairstepTableIsEnabled());
        tableColumnDelimiterFieldProperties.setPropertyValue("Enabled", !settings.stairstepTableIsEnabled());
        tableColumnDelimiterButtonProperties.setPropertyValue("Enabled", !settings.stairstepTableIsEnabled());
        
        boolean left = (settings.getStyle("table" + ((currentTableColumn==0)?"":"_" + currentTableColumn))
                                .getAlignment() == Alignment.LEFT);
        boolean centered = (settings.getStyle("table" + ((currentTableColumn==0)?"":"_" + currentTableColumn))
                                .getAlignment() == Alignment.CENTERED);
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        tableFirstLineFieldProperties.setPropertyValue("Enabled", left && !bana);
        tableRunoversFieldProperties.setPropertyValue("Enabled", left &&!bana);
        tableMarginLeftRightFieldProperties.setPropertyValue("Enabled", centered && !bana);
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
        preliminaryPageNumberFormatListBoxProperties.setPropertyValue("Enabled", !bana && settings.getBraillePageNumbers()
                                                                                       && settings.getPreliminaryPagesPresent());
        beginningBraillePageNumberFieldProperties.setPropertyValue("Enabled", settings.getBraillePageNumbers());
        printPageNumbersCheckBoxProperties.setPropertyValue("Enabled", !bana && settings.getPageNumbersPresent());
        printPageNumberAtListBoxProperties.setPropertyValue("Enabled", !bana && settings.getPageNumbersPresent()
                                                                             && settings.getPrintPageNumbers());
        printPageNumberRangeCheckBoxProperties.setPropertyValue("Enabled", !bana && settings.getPrintPageNumbers());
        continuePagesCheckBoxProperties.setPropertyValue("Enabled", !bana && settings.getPrintPageNumbers());
        pageSeparatorCheckBoxProperties.setPropertyValue("Enabled", !bana);
        pageSeparatorNumberCheckBoxProperties.setPropertyValue("Enabled", !bana && settings.getPageSeparator()
                                                                                && settings.getPrintPageNumbers());
        ignoreEmptyPagesCheckBoxProperties.setPropertyValue("Enabled", !bana);
        mergeUnnumberedPagesCheckBoxProperties.setPropertyValue("Enabled", !bana);
        numbersAtTopOnSepLineCheckBoxProperties.setPropertyValue("Enabled", !bana
                                                                            && ((settings.getBraillePageNumbers()
                                                                              && settings.getBraillePageNumberAt() == PageNumberPosition.TOP_RIGHT)
                                                                             || (settings.getPrintPageNumbers()
                                                                              && settings.getPrintPageNumberAt() == PageNumberPosition.TOP_RIGHT))
                                                                            && !(settings.getPrintPageNumberAt() == PageNumberPosition.TOP_RIGHT
                                                                              && settings.getPrintPageNumbers()
                                                                              && settings.getPrintPageNumberRange()));
        numbersAtBottomOnSepLineCheckBoxProperties.setPropertyValue("Enabled", !bana
                                                                            && ((settings.getBraillePageNumbers()
                                                                              && settings.getBraillePageNumberAt() == PageNumberPosition.BOTTOM_RIGHT)
                                                                             || (settings.getPrintPageNumbers()
                                                                              && settings.getPrintPageNumberAt() == PageNumberPosition.BOTTOM_RIGHT)));

    }

    private void updateTableOfContentsPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean enabled = settings.getTableOfContentEnabled();
        boolean bana = (settings.getBrailleRules()==BrailleRules.BANA);

        tableOfContentsTitleFieldProperties.setPropertyValue("Enabled", enabled);
        tableOfContentsLevelListBoxProperties.setPropertyValue("Enabled", enabled);
        tableOfContentsLineFillFieldProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsLineFillButtonProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsLinesBetweenFieldProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsFirstLineFieldProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsRunoversFieldProperties.setPropertyValue("Enabled", enabled && !bana);
        tableOfContentsBraillePageNumbersCheckBoxProperties.setPropertyValue("Enabled", enabled && settings.getBraillePageNumbers());
        tableOfContentsPrintPageNumbersCheckBoxProperties.setPropertyValue("Enabled", enabled && settings.getPrintPageNumbers());

    }

    private void updateSpecialSymbolsPageFieldProperties() throws com.sun.star.uno.Exception {

        boolean enabled = settings.getSpecialSymbolsListEnabled();

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

    private void updateTypefacePageFieldValues() {

        CharacterStyle style = characterStyles.get(selectedCharacterStylePos);

        characterInheritCheckBox.setState((short)(style.getInherit()?1:0));
        characterParentField.setText((style.getParentStyle() != null)?style.getParentStyle().getDisplayName():"");
        characterBoldfaceListBox.selectItemPos((short)(typefaceOptions.indexOf(style.getBoldface())), true);
        characterItalicListBox.selectItemPos((short)(typefaceOptions.indexOf(style.getItalic())), true);
        characterUnderlineListBox.selectItemPos((short)(typefaceOptions.indexOf(style.getUnderline())), true);
        characterCapitalsListBox.selectItemPos((short)(typefaceOptions.indexOf(style.getCapitals())), true);

    }
    
    private void saveTypefacePageFieldValues() {

        CharacterStyle style = characterStyles.get(selectedCharacterStylePos);

        if (!style.getInherit()) {
            style.setBoldface(typefaceOptions.get((int)characterBoldfaceListBox.getSelectedItemPos()));
            style.setItalic(typefaceOptions.get((int)characterItalicListBox.getSelectedItemPos()));
            style.setUnderline(typefaceOptions.get((int)characterUnderlineListBox.getSelectedItemPos()));
            style.setCapitals(typefaceOptions.get((int)characterCapitalsListBox.getSelectedItemPos()));
        }
    }

    private void updateParagraphsPageFieldValues() {

        ParagraphStyle style = paragraphStyles.get(selectedParagraphStylePos);
        boolean left = (style.getAlignment() == Alignment.LEFT);
        boolean centered = (style.getAlignment() == Alignment.CENTERED);

        paragraphInheritCheckBox.setState((short)(style.getInherit()?1:0));
        paragraphParentField.setText((style.getParentStyle() != null)?style.getParentStyle().getDisplayName():"");
        paragraphLinesAboveField.setValue((double)style.getLinesAbove());
        paragraphLinesBelowField.setValue((double)style.getLinesBelow());
        paragraphAlignmentListBox.selectItemPos((short)(alignmentOptions.indexOf(style.getAlignment())), true);
        paragraphFirstLineField.setValue((double)(left?style.getFirstLine():0));
        paragraphRunoversField.setValue((double)(left?style.getRunovers():0));
        paragraphMarginLeftRightField.setValue((double)(centered?style.getMarginLeftRight():0));
        paragraphKeepEmptyCheckBox.setState((short)(style.getKeepEmptyParagraphs()?1:0));
        paragraphKeepWithNextCheckBox.setState((short)(style.getKeepWithNext()?1:0));
        paragraphDontSplitCheckBox.setState((short)(style.getDontSplit()?1:0));
        paragraphWidowControlCheckBox.setState((short)(style.getWidowControl()>1?1:0));
        paragraphOrphanControlCheckBox.setState((short)(style.getOrphanControl()>1?1:0));
        paragraphWidowControlField.setValue((double)Math.max(2,style.getWidowControl()));
        paragraphOrphanControlField.setValue((double)Math.max(2,style.getOrphanControl()));

    }

    private void saveParagraphsPageFieldValues() {

        ParagraphStyle style = paragraphStyles.get(selectedParagraphStylePos);

        if (!style.getInherit()) {
            style.setLinesAbove((int)paragraphLinesAboveField.getValue());
            style.setLinesBelow((int)paragraphLinesBelowField.getValue());
            style.setKeepEmptyParagraphs(paragraphKeepEmptyCheckBox.getState()==(short)1);
            if (style.getAlignment() == Alignment.LEFT) {
                style.setFirstLine((int)paragraphFirstLineField.getValue());
                style.setRunovers((int)paragraphRunoversField.getValue());
            } else if (style.getAlignment() == Alignment.CENTERED) {
                style.setMarginLeftRight((int)paragraphMarginLeftRightField.getValue());
            }
        }
    }

    private void updateHeadingsPageFieldValues() {

        HeadingStyle style = headingStyles.get(selectedHeadingStylePos);
        boolean left = (style.getAlignment() == Alignment.LEFT);
        boolean centered = (style.getAlignment() == Alignment.CENTERED);

        headingLinesAboveField.setValue((double)style.getLinesAbove());
        headingLinesBelowField.setValue((double)style.getLinesBelow());
        headingAlignmentListBox.selectItemPos((short)(alignmentOptions.indexOf(style.getAlignment())), true);
        headingFirstLineField.setValue((double)(left?style.getFirstLine():0));
        headingRunoversField.setValue((double)(left?style.getRunovers():0));
        headingMarginLeftRightField.setValue((double)(centered?style.getMarginLeftRight():0));
        headingNewBraillePageCheckBox.setState((short)(style.getNewBraillePage()?1:0));
        headingKeepWithNextCheckBox.setState((short)(style.getKeepWithNext()?1:0));
        headingDontSplitCheckBox.setState((short)(style.getDontSplit()?1:0));

    }

    private void saveHeadingsPageFieldValues() {

        HeadingStyle style = headingStyles.get(selectedHeadingStylePos);
        
        style.setLinesAbove((int)headingLinesAboveField.getValue());
        style.setLinesBelow((int)headingLinesBelowField.getValue());
        style.setNewBraillePage(headingNewBraillePageCheckBox.getState()==(short)1);
        style.setDontSplit(headingDontSplitCheckBox.getState()==(short)1);
        if (style.getAlignment() == Alignment.LEFT) {
            style.setFirstLine((int)headingFirstLineField.getValue());
            style.setRunovers((int)headingRunoversField.getValue());
        } else if (style.getAlignment() == Alignment.CENTERED) {
            style.setMarginLeftRight((int)headingMarginLeftRightField.getValue());
        }
    }

    private void updateListsPageFieldValues() {

        Style style = settings.getStyle("list_" + currentListLevel);
        boolean left = (style.getAlignment() == Alignment.LEFT);
        boolean centered = (style.getAlignment() == Alignment.CENTERED);

        listAlignmentListBox.selectItemPos((short)(alignmentOptions.indexOf(style.getAlignment())), true);
        listFirstLineField.setValue((double)(left?style.getFirstLine():0));
        listRunoversField.setValue((double)(left?style.getRunovers():0));
        listMarginLeftRightField.setValue((double)(centered?style.getMarginLeftRight():0));
        listLinesAboveField.setValue((double)style.getLinesAbove());
        listLinesBelowField.setValue((double)style.getLinesBelow());
        listLinesBetweenField.setValue((double)style.getLinesBetween());
        listPrefixField.setText(style.getPrefix());

    }

    private void saveListsPageFieldValues() {

        Style style = settings.getStyle("list_" + currentListLevel);
        
        style.setLinesAbove((int)listLinesAboveField.getValue());
        style.setLinesBelow((int)listLinesBelowField.getValue());
        style.setLinesBetween((int)listLinesBetweenField.getValue());
        if (style.getAlignment() == Alignment.LEFT) {
            style.setFirstLine((int)listFirstLineField.getValue());
            style.setRunovers((int)listRunoversField.getValue());
        } else if (style.getAlignment() == Alignment.CENTERED) {
            style.setMarginLeftRight((int)listMarginLeftRightField.getValue());
        }

    }

    private void updateTablesPageFieldValues() {

        Style style = settings.getStyle("table" + ((currentTableColumn>0)?"_" + currentTableColumn:""));
        boolean left = (style.getAlignment() == Alignment.LEFT);
        boolean centered = (style.getAlignment() == Alignment.CENTERED);

        tableAlignmentListBox.selectItemPos((short)(alignmentOptions.indexOf(style.getAlignment())), true);
        tableFirstLineField.setValue((double)(left?style.getFirstLine():0));
        tableRunoversField.setValue((double)(left?style.getRunovers():0));
        tableMarginLeftRightField.setValue((double)(centered?style.getMarginLeftRight():0));
        tableColumnListBox.selectItemPos((short)(Math.max(0,currentTableColumn-1)), true);

    }

    private void saveTablesPageFieldValues() {

        Style style = settings.getStyle("table" + ((currentTableColumn>0)?"_" + currentTableColumn:""));

        if (style.getAlignment() == Alignment.LEFT) {
            style.setFirstLine((int)tableFirstLineField.getValue());
            style.setRunovers((int)tableRunoversField.getValue());
        } else if (style.getAlignment() == Alignment.CENTERED) {
            style.setMarginLeftRight((int)tableMarginLeftRightField.getValue());
        }
    }

    private void updatePageNumbersPageFieldValues() {

        braillePageNumbersCheckBox.setState((short)(settings.getBraillePageNumbers()?1:0));
        braillePageNumberAtListBox.selectItemPos((short)((settings.getBraillePageNumberAt() == PageNumberPosition.TOP_RIGHT)?0:1), true);
        preliminaryPageNumberFormatListBox.selectItemPos((short)((settings.getPreliminaryPageFormat() == PageNumberFormat.P)?0:1), true);
        printPageNumbersCheckBox.setState((short)(settings.getPrintPageNumbers()?1:0));
        printPageNumberAtListBox .selectItemPos((short)((settings.getPrintPageNumberAt() == PageNumberPosition.TOP_RIGHT)?0:1), true);
        printPageNumberRangeCheckBox.setState((short)(settings.getPrintPageNumberRange()?1:0));
        continuePagesCheckBox.setState((short)(settings.getContinuePages()?1:0));
        pageSeparatorCheckBox.setState((short)(settings.getPageSeparator()?1:0));
        pageSeparatorNumberCheckBox.setState((short)(settings.getPageSeparatorNumber()?1:0));
        ignoreEmptyPagesCheckBox.setState((short)(settings.getIgnoreEmptyPages()?1:0));
        mergeUnnumberedPagesCheckBox.setState((short)(settings.getMergeUnnumberedPages()?1:0));
        numbersAtTopOnSepLineCheckBox.setState((short)(settings.getPageNumberAtTopOnSeparateLine()?1:0));
        numbersAtBottomOnSepLineCheckBox.setState((short)(settings.getPageNumberAtBottomOnSeparateLine()?1:0));

    }

    private void updateTableOfContentsPageFieldValues() {

        tableOfContentsBraillePageNumbersCheckBox.setState((short)(settings.getBraillePageNumbersInToc()?1:0));
        Style style = settings.getStyle("toc_" + currentTableOfContentsLevel);
        tableOfContentsFirstLineField.setValue(style.getFirstLine());
        tableOfContentsRunoversField.setValue(style.getRunovers());

    }

    private void saveTableOfContentsPageFieldValues() {

        Style style = settings.getStyle("toc_" + currentTableOfContentsLevel);
        style.setFirstLine((int)tableOfContentsFirstLineField.getValue());
        style.setRunovers((int)tableOfContentsRunoversField.getValue());

    }

    private void updateSpecialSymbolsListBox() {

        specialSymbols = settings.getSpecialSymbolsList();
        specialSymbolsListBox.removeItems((short)0, Short.MAX_VALUE);
        for (int i=0;i<specialSymbols.size();i++) {
            specialSymbolsListBox.addItem(specialSymbols.get(i).getSymbol(), (short)i);
        }
        specialSymbolsListBox.selectItemPos((short)(selectedSpecialSymbolPos), true);

    }

    private void updateSpecialSymbolsPageFieldValues() {

        SpecialSymbol selectedSpecialSymbol = specialSymbols.get(selectedSpecialSymbolPos);

        specialSymbolsSymbolField.setText(selectedSpecialSymbol.getSymbol());
        specialSymbolsDescriptionField.setText(selectedSpecialSymbol.getDescription());

        specialSymbolsMode0RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.NEVER);
        specialSymbolsMode1RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.IF_PRESENT_IN_VOLUME);
        specialSymbolsMode2RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.FIRST_VOLUME);
        specialSymbolsMode3RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.ALWAYS);

    }

    private void saveSpecialSymbolsPageFieldValues() {

        specialSymbols.get(selectedSpecialSymbolPos).setDescription(specialSymbolsDescriptionField.getText());

    }

    /**
     * Select the correct item in the 'Main translation table' listbox on the 'General Settings' tab.
     *
     */
    private void updateMainTranslationTableListBox() {

        mainTranslationTableListBox.selectItemPos((short)mainTranslationTables.indexOf(
                settings.getTranslationTable(settings.getMainLanguage())),true);

    }

    /**
     * Update the list of available grades in the 'Main grade' listbox on the 'General Settings' tab and select the correct item.
     *
     */
    private void updateMainGradeListBox() {

        mainGradeListBox.removeItems((short)0, Short.MAX_VALUE);
        ArrayList<Integer> supportedGrades = settings.getSupportedGrades(settings.getMainLanguage());
        for (int i=0;i<supportedGrades.size();i++) {
            mainGradeListBox.addItem(L10N_grades.get(supportedGrades.get(i)), (short)i);
        }
        mainGradeListBox.selectItemPos((short)supportedGrades.indexOf(settings.getGrade(settings.getMainLanguage())), true);

    }

    private void updateMainEightDotsCheckBox() {

        mainEightDotsCheckBox.setState((short)((settings.getDots(settings.getMainLanguage())==8)?1:0));

    }

    /**
     * Select the correct item in the 'Translation table' listbox on the 'Language Settings' tab.
     *
     */
    private void updateTranslationTableListBox() {

        translationTableListBox.selectItemPos((short)allTranslationTables.indexOf(
                settings.getTranslationTable(languages.get(selectedLanguagePos))),true);

    }

    /**
     * Update the list of available grades in the 'Grade' listbox on the 'Language Settings' tab and select the correct item.
     *
     */
    private void updateGradeListBox() {

        gradeListBox.removeItems((short)0, Short.MAX_VALUE);
        ArrayList<Integer> supportedGrades = settings.getSupportedGrades(languages.get(selectedLanguagePos));
        for (int i=0;i<supportedGrades.size();i++) {
            gradeListBox.addItem(L10N_grades.get(supportedGrades.get(i)), (short)i);
        }
        gradeListBox.selectItemPos((short)supportedGrades.indexOf(settings.getGrade(languages.get(selectedLanguagePos))), true);

    }

    private void updateEightDotsCheckBox() {

        mainEightDotsCheckBox.setState((short)((settings.getDots(languages.get(selectedLanguagePos))==8)?1:0));

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
                if (newPage != currentPage) {
                    
                    windowProperties.setPropertyValue("Step", new Integer(newPage));
                    setPage(newPage);

                }

            } else if (source.equals(brailleRulesListBox)) {

                settings.setBrailleRules(BrailleRules.values()[brailleRulesListBox.getSelectedItemPos()]);

                removeGeneralPageListeners();
                removeLanguagesPageListeners();
                removeTypefacePageListeners();
                removeParagraphsPageListeners();
                removeHeadingsPageListeners();
                removeListsPageListeners();
                removeTablesPageListeners();
                removePageNumbersPageListeners();
                removeTableOfContentsPageListeners();
                removeSpecialSymbolsPageListeners();

                if (settings.getBrailleRules()==BrailleRules.BANA) {

                    if (pagesEnabled[TYPEFACE_PAGE-1])   { updateTypefacePageFieldValues();   }
                    if (pagesEnabled[PARAGRAPHS_PAGE-1]) { updateParagraphsPageFieldValues(); }
                    if (pagesEnabled[HEADINGS_PAGE-1])   { updateHeadingsPageFieldValues();   }
                    if (pagesEnabled[LISTS_PAGE-1])      { updateListsPageFieldValues();      }
                    if (pagesEnabled[TABLES_PAGE-1]) {
                        if (currentTableColumn==0) {
                            saveTablesPageFieldValues();
                            currentTableColumn = 1;
                        }
                        Style style = settings.getStyle("table");
                        tableLinesAboveField.setValue((double)style.getLinesAbove());
                        tableLinesBelowField.setValue((double)style.getLinesBelow());
                        tableLinesBetweenField.setValue((double)style.getLinesBetween());
                        tableSimpleRadioButton.setState(!settings.stairstepTableIsEnabled());
                        tableStairstepRadioButton.setState(settings.stairstepTableIsEnabled());
                        updateTablesPageFieldValues();
                    }
                    if (pagesEnabled[PAGENUMBERS_PAGE-1]) { updatePageNumbersPageFieldValues(); }
                    if (pagesEnabled[TOC_PAGE-1])         {
                        tableOfContentsLineFillField.setText(String.valueOf(settings.getLineFillSymbol()));
                        tableOfContentsLinesBetweenField.setValue((double)settings.getStyle("toc").getLinesBetween());
                        updateTableOfContentsPageFieldValues();
                    }
                }

                if (pagesEnabled[TYPEFACE_PAGE-1])    { updateTypefacePageFieldProperties();        }
                if (pagesEnabled[PARAGRAPHS_PAGE-1])  { updateParagraphsPageFieldProperties();      }
                if (pagesEnabled[HEADINGS_PAGE-1])    { updateHeadingsPageFieldProperties();        }
                if (pagesEnabled[LISTS_PAGE-1])       { updateListsPageFieldProperties();           }
                if (pagesEnabled[TABLES_PAGE-1])      { updateTablesPageFieldProperties();          }
                if (pagesEnabled[PAGENUMBERS_PAGE-1]) { updatePageNumbersPageFieldProperties();     }
                if (pagesEnabled[TOC_PAGE-1])         { updateTableOfContentsPageFieldProperties(); }

                addGeneralPageListeners();
                addLanguagesPageListeners();
                addTypefacePageListeners();
                addParagraphsPageListeners();
                addHeadingsPageListeners();
                addListsPageListeners();
                addTablesPageListeners();
                addPageNumbersPageListeners();
                addTableOfContentsPageListeners();
                addSpecialSymbolsPageListeners();

            } else {
                switch (currentPage) {

                    case GENERAL_PAGE:

                        removeGeneralPageListeners();

                        if (source.equals(transcriptionInfoCheckBox)) {
                            settings.setTranscriptionInfoEnabled(transcriptionInfoCheckBox.getState() == (short) 1);
                        } else if (source.equals(volumeInfoCheckBox)) {
                            settings.setVolumeInfoEnabled(volumeInfoCheckBox.getState() == (short) 1);
                        } else if (source.equals(transcribersNotesPageCheckBox)) {
                            settings.setTranscribersNotesPageEnabled(transcribersNotesPageCheckBox.getState() == (short) 1);
                        } else if (source.equals(mainTranslationTableListBox)) {
                            settings.setTranslationTable(
                                    mainTranslationTables.get((int)mainTranslationTableListBox.getSelectedItemPos()),settings.getMainLanguage());
                            updateMainGradeListBox();
                            updateMainEightDotsCheckBox();
                        } else if (source.equals(mainGradeListBox)) {
                            settings.setGrade(settings.getSupportedGrades(
                                    settings.getMainLanguage()).get((int)mainGradeListBox.getSelectedItemPos()),settings.getMainLanguage());
                            updateMainEightDotsCheckBox();
                        }

                        updateGeneralPageFieldProperties();
                        addGeneralPageListeners();
                        break;

                    case LANGUAGES_PAGE:

                        removeLanguagesPageListeners();

                        if (source.equals(translationTableListBox)) {
                            settings.setTranslationTable(allTranslationTables.get((int)translationTableListBox.getSelectedItemPos()),
                                                         languages.get(selectedLanguagePos));
                            updateGradeListBox();
                            updateEightDotsCheckBox();
                        } else if (source.equals(gradeListBox)) {
                            settings.setGrade(settings.getSupportedGrades(
                                    languages.get(selectedLanguagePos)).get((int)gradeListBox.getSelectedItemPos()),languages.get(selectedLanguagePos));
                            updateEightDotsCheckBox();
                        } else if (source.equals(eightDotsCheckBox)) {
                            settings.setDots((eightDotsCheckBox.getState()==(short)1)?8:6, languages.get(selectedLanguagePos));
                        } else if (source.equals(languagesListBox)) {
                            selectedLanguagePos = (int)languagesListBox.getSelectedItemPos();
                            updateTranslationTableListBox();
                            updateGradeListBox();
                            updateEightDotsCheckBox();
                        }

                        updateLanguagesPageFieldProperties();
                        addLanguagesPageListeners();
                        break;

                    case TYPEFACE_PAGE:

                        removeTypefacePageListeners();
                        saveTypefacePageFieldValues();

                        if (source.equals(characterStyleListBox)) {
                            selectedCharacterStylePos = (int)characterStyleListBox.getSelectedItemPos();
                        } else if (source.equals(characterInheritCheckBox)) {
                            characterStyles.get(selectedCharacterStylePos).setInherit(characterInheritCheckBox.getState()==(short)1);
                        }

                        updateTypefacePageFieldValues();
                        updateTypefacePageFieldProperties();
                        addTypefacePageListeners();
                        break;

                    case PARAGRAPHS_PAGE:

                        removeParagraphsPageListeners();
                        saveParagraphsPageFieldValues();

                        if (source.equals(paragraphStyleListBox)) {
                            selectedParagraphStylePos = (int)paragraphStyleListBox.getSelectedItemPos();
                        } else if (source.equals(paragraphAlignmentListBox)) {
                            if (!paragraphStyles.get(selectedParagraphStylePos).getInherit()) {
                                paragraphStyles.get(selectedParagraphStylePos).setAlignment(
                                        alignmentOptions.get(paragraphAlignmentListBox.getSelectedItemPos()));
                            }
                        } else if (source.equals(paragraphInheritCheckBox)) {
                            paragraphStyles.get(selectedParagraphStylePos).setInherit(paragraphInheritCheckBox.getState()==(short)1);
                        } else if (source.equals(paragraphDontSplitCheckBox)) {
                            paragraphStyles.get(selectedParagraphStylePos).setDontSplit(paragraphDontSplitCheckBox.getState()==(short)1);
                        } else if (source.equals(paragraphKeepWithNextCheckBox)) {
                            paragraphStyles.get(selectedParagraphStylePos).setKeepWithNext(paragraphKeepWithNextCheckBox.getState()==(short)1);
                        }

                        updateParagraphsPageFieldValues();
                        updateParagraphsPageFieldProperties();
                        addParagraphsPageListeners();
                        break;
                        
                    case HEADINGS_PAGE: 

                        removeHeadingsPageListeners();
                        saveHeadingsPageFieldValues();

                        if (source.equals(headingLevelListBox)) {                            
                            selectedHeadingStylePos = (int)headingLevelListBox.getSelectedItemPos();
                        } else if (source.equals(headingAlignmentListBox)) {
                            headingStyles.get(selectedHeadingStylePos).setAlignment(
                                    alignmentOptions.get(headingAlignmentListBox.getSelectedItemPos()));
                        } else if (source.equals(headingKeepWithNextCheckBox)) {
                            headingStyles.get(selectedHeadingStylePos).setKeepWithNext(headingKeepWithNextCheckBox.getState()==(short)1);
                        }

                        updateHeadingsPageFieldValues();
                        updateHeadingsPageFieldProperties();
                        addHeadingsPageListeners();
                        break;
                        
                    case LISTS_PAGE:

                        removeListsPageListeners();
                        saveListsPageFieldValues();

                        if (source.equals(listLevelListBox)) {
                            currentListLevel = listLevelListBox.getSelectedItemPos() + 1;                            
                        } else if (source.equals(listAlignmentListBox)) {
                            settings.getStyle("list_" + currentListLevel).setAlignment(
                                    alignmentOptions.get(listAlignmentListBox.getSelectedItemPos()));
                        }

                        updateListsPageFieldValues();
                        updateListsPageFieldProperties();
                        addListsPageListeners();
                        break;

                    case TABLES_PAGE:

                        removeTablesPageListeners();
                        saveTablesPageFieldValues();
                        
                        if (source.equals(tableSimpleRadioButton) ||
                            source.equals(tableStairstepRadioButton)) {

                            if (!settings.stairstepTableIsEnabled()) {
                                tableSimpleRadioButton.setState(false);
                                settings.setStairstepTable(true);
                                currentTableColumn = 1;
                            } else {
                                tableStairstepRadioButton.setState(false);
                                settings.setStairstepTable(false);
                                currentTableColumn = 0;
                            }
                        } else if (source.equals(tableColumnListBox)) {
                            currentTableColumn = tableColumnListBox.getSelectedItemPos() + 1;
                        } else if (source.equals(tableAlignmentListBox)) {
                            settings.getStyle("table" + ((currentTableColumn==0)?"":"_" + currentTableColumn)).setAlignment(
                                    alignmentOptions.get(tableAlignmentListBox.getSelectedItemPos()));
                        }

                        updateTablesPageFieldValues();
                        updateTablesPageFieldProperties();
                        addTablesPageListeners();
                        break;

                    case PAGENUMBERS_PAGE:

                        removePageNumbersPageListeners();
                        removeTableOfContentsPageListeners();

                        if (source.equals(braillePageNumbersCheckBox)) {
                            settings.setBraillePageNumbers(braillePageNumbersCheckBox.getState() == (short)1);
                            if (pagesEnabled[TOC_PAGE-1]) {
                                updateTableOfContentsPageFieldValues();
                                updateTableOfContentsPageFieldProperties();
                            }
                        } else if (source.equals(braillePageNumberAtListBox)) {
                            settings.setBraillePageNumberAt(((braillePageNumberAtListBox.getSelectedItemPos() == (short)0)?
                                PageNumberPosition.TOP_RIGHT:PageNumberPosition.BOTTOM_RIGHT));
                        } else if (source.equals(pageSeparatorCheckBox)) {
                            settings.setPageSeparator(pageSeparatorCheckBox.getState() == (short)1);
                            pageSeparatorNumberCheckBox.setState((short)(settings.getPageSeparatorNumber()?1:0));
                        } else if (source.equals(printPageNumbersCheckBox)) {
                            settings.setPrintPageNumbers(printPageNumbersCheckBox.getState() == (short)1);
                            continuePagesCheckBox.setState((short)(settings.getContinuePages()?1:0));
                            printPageNumberRangeCheckBox.setState((short)(settings.getPrintPageNumberRange()?1:0));
                            numbersAtTopOnSepLineCheckBox.setState((short)(settings.getPageNumberAtTopOnSeparateLine()?1:0));
                            pageSeparatorNumberCheckBox.setState((short)(settings.getPageSeparatorNumber()?1:0));
                            tableOfContentsPrintPageNumbersCheckBox.setState((short)(settings.getPrintPageNumbersInToc()?1:0));
                            tableOfContentsPrintPageNumbersCheckBoxProperties.setPropertyValue("Enabled",
                                    settings.getTableOfContentEnabled() && settings.getPrintPageNumbers());
                        } else if (source.equals(printPageNumberAtListBox)) {
                            settings.setPrintPageNumberAt(((printPageNumberAtListBox.getSelectedItemPos() == (short)0)?
                                PageNumberPosition.TOP_RIGHT:PageNumberPosition.BOTTOM_RIGHT));
                            numbersAtTopOnSepLineCheckBox.setState((short)(settings.getPageNumberAtTopOnSeparateLine()?1:0));
                        } else if (source.equals(printPageNumberRangeCheckBox)) {
                            settings.setPrintPageNumberRange(printPageNumberRangeCheckBox.getState() == (short) 1);
                            numbersAtTopOnSepLineCheckBox.setState((short)(settings.getPageNumberAtTopOnSeparateLine()?1:0));
                        }
                        
                        updatePageNumbersPageFieldProperties();
                        addPageNumbersPageListeners();
                        addTableOfContentsPageListeners();
                        break;

                    case TOC_PAGE:

                        removeTableOfContentsPageListeners();

                        if (source.equals(tableOfContentsCheckBox)) {
                            settings.setTableOfContentEnabled(tableOfContentsCheckBox.getState()==(short)1);
                        } else if (source.equals(tableOfContentsLevelListBox)) {
                            saveTableOfContentsPageFieldValues();
                            currentTableOfContentsLevel = (int)tableOfContentsLevelListBox.getSelectedItemPos() + 1;
                            updateTableOfContentsPageFieldValues();                            
                        }

                        updateTableOfContentsPageFieldProperties();
                        addTableOfContentsPageListeners();
                        break;

                    case SPECIAL_SYMBOLS_PAGE:

                        removeSpecialSymbolsPageListeners();

                        if (source.equals(specialSymbolsListCheckBox)) {
                            settings.setSpecialSymbolsListEnabled(specialSymbolsListCheckBox.getState() == (short) 1);
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

                            specialSymbolsMode0RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.NEVER);
                            specialSymbolsMode1RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.IF_PRESENT_IN_VOLUME);
                            specialSymbolsMode2RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.FIRST_VOLUME);
                            specialSymbolsMode3RadioButton.setState(selectedSpecialSymbol.getMode()==SpecialSymbolMode.ALWAYS);

                        }

                        addSpecialSymbolsPageListeners();
                        break;

                    case MATH_PAGE: break;
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

                    windowProperties.setPropertyValue("Step", new Integer(newPage));
                    roadmapProperties.setPropertyValue("CurrentItemID", (short)newPage);
                    setPage(newPage);

                }

            } else {

                switch (currentPage) {

                    case GENERAL_PAGE:
                    case LANGUAGES_PAGE:
                    case TYPEFACE_PAGE:
                    case PARAGRAPHS_PAGE:
                    case HEADINGS_PAGE: break;
                    case LISTS_PAGE:

                        if (source.equals(listPrefixButton)) {
                            Style style = settings.getStyle("list_" + currentListLevel);
                            InsertDialog insertBrailleDialog = new InsertDialog(xContext);
                            insertBrailleDialog.setBrailleCharacters(style.getPrefix());
                            if (insertBrailleDialog.execute()) {
                                style.setPrefix(insertBrailleDialog.getBrailleCharacters());
                                listPrefixField.setText(style.getPrefix());
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

                    case PAGENUMBERS_PAGE: break;
                    case TOC_PAGE:

                        if (source.equals(tableOfContentsLineFillButton)) {
                            InsertDialog insertBrailleDialog = new InsertDialog(xContext);
                            insertBrailleDialog.setBrailleCharacters(String.valueOf(settings.getLineFillSymbol()));
                            if (insertBrailleDialog.execute()) {
                                String s = insertBrailleDialog.getBrailleCharacters();
                                if (s.length()==1) {
                                    if (settings.setLineFillSymbol(s.charAt(0))) {
                                        tableOfContentsLineFillField.setText(String.valueOf(settings.getLineFillSymbol()));
                                    }
                                }
                            }
                        }

                        break;

                    case SPECIAL_SYMBOLS_PAGE:

                        removeSpecialSymbolsPageListeners();

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

                        addSpecialSymbolsPageListeners();
                        break;

                    case MATH_PAGE: break;

                }
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
