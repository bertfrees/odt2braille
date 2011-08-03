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

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.Collection;
import java.util.EventObject;

import java.util.MissingResourceException;
import java.util.NoSuchElementException;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;
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
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XItemEventBroadcaster;
import com.sun.star.awt.FocusEvent;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.ooo.dialog.*;
import be.docarch.odt2braille.setup.NoteReferenceFormat;
import be.docarch.odt2braille.setup.SpecialSymbol;
import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.SettingMap;
import be.docarch.odt2braille.setup.OptionSetting;
import be.docarch.odt2braille.setup.TranslationTable;
import be.docarch.odt2braille.setup.FormattingRules;
import be.docarch.odt2braille.setup.BANAFormattingRules;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.Configuration.MathCode;
import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;
import be.docarch.odt2braille.setup.Configuration.PageNumberPosition;
import be.docarch.odt2braille.setup.Configuration.VolumeManagementMode;
import be.docarch.odt2braille.setup.Configuration.Volume;
import be.docarch.odt2braille.setup.Configuration.SectionVolume;
import be.docarch.odt2braille.setup.Configuration.SplittableVolume;
import be.docarch.odt2braille.setup.style.Style;
import be.docarch.odt2braille.setup.style.ParagraphStyle;
import be.docarch.odt2braille.setup.style.HeadingStyle;
import be.docarch.odt2braille.setup.style.ListStyle;
import be.docarch.odt2braille.setup.style.TableStyle;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.CharacterStyle;

public class SettingsDialog {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N_BUNDLE = Constants.OOO_L10N_PATH;

    private final static int roadMapWidth = 85;
    private final static int roadMapHeight = 273;

    private final static short GENERAL_PAGE = 1;
    private final static short LANGUAGES_PAGE = 2;
    private final static short TYPEFACE_PAGE = 3;
    private final static short PARAGRAPHS_PAGE = 4;
    private final static short HEADINGS_PAGE = 5;
    private final static short LISTS_PAGE = 6;
    private final static short TABLES_PAGE = 7;
    private final static short NOTES_PAGE = 8;
    private final static short PICTURES_PAGE = 9;
    private final static short PAGENUMBERS_PAGE = 10;
    private final static short DOCUMENT_LAYOUT_PAGE = 11;
    private final static short VOLUME_MANAGEMENT_PAGE = 12;
    private final static short TOC_PAGE = 13;
    private final static short SPECIAL_SYMBOLS_PAGE = 14;
    private final static short MATH_PAGE = 15;

    private final static short NUMBER_OF_PAGES = 15;
    private final boolean[] pagesEnabled = new boolean[NUMBER_OF_PAGES];

    private final Map<Style.Alignment,String> L10N_alignment = new HashMap<Style.Alignment,String>();
    private final Map<MathCode,String> L10N_math = new HashMap<MathCode,String>();
    private final Map<CharacterStyle.TypefaceOption,String> L10N_typeface = new HashMap<CharacterStyle.TypefaceOption,String>();
    private final Map<String,String> L10N_noterefFormats = new HashMap<String,String>();
    private final Map<PageNumberFormat,String> L10N_pageNumberFormats = new HashMap<PageNumberFormat,String>();
    private final Map<PageNumberPosition,String> L10N_pageNumberPositions = new HashMap<PageNumberPosition,String>();
    private final Map<String,String> L10N_translationTables = new HashMap<String,String>();
    private final Map<Locale,String> L10N_languages = new HashMap<Locale,String>();

    private final Configuration settings;
    private final XComponentContext context;
    private final XDialog dialog;
    private final XComponent component;
    private final XControl control;
    private final XPropertySet windowProperties;


    /**********/
    /* COMMON */
    /**********/

    private final Roadmap roadmap;
    private final Button okButton;
    private final Button cancelButton;
    private final NavigationButton backButton;
    private final NavigationButton nextButton;
    private final FormattingRulesButton formattingRulesListBox;

    /****************/
    /* GENERAL PAGE */
    /****************/

    /* CONTROLS */

    private final TextSettingControl creatorField;
    private final TextSettingControl transcribersNotesPageTitleField;
    private final ListBox<String> mainTranslationTableListBox;
    private final ListBox<Integer> mainGradeListBox;
    private final EightDotsCheckBox mainEightDotsCheckBox;
    private final CheckBox transcriptionInfoCheckBox;
    private final CheckBox volumeInfoCheckBox;
    private final CheckBox hyphenateCheckBox;
    private final CheckBox hardPageBreaksCheckBox;
    private final ListBox<ParagraphStyle> transcriptionInfoStyleListBox;
    private final ListBox<ParagraphStyle> volumeInfoStyleListBox;
    private final NumericSettingControl minSyllableLengthField;

    /* LABELS */

    private final Label creatorLabel;
    private final Label transcribersNotesPageTitleLabel;
    private final Label mainTranslationTableLabel;
    private final Label mainGradeLabel;
    private final Label mainEightDotsLabel;
    private final Label transcriptionInfoLabel;
    private final Label volumeInfoLabel;
    private final Label hyphenateLabel;
    private final Label hardPageBreaksLabel;
    private final Label transcriptionInfoStyleLabel;
    private final Label volumeInfoStyleLabel;
    private final Label minSyllableLengthLabel;

    /******************/
    /* LANGUAGES PAGE */
    /******************/

    /* CONTROLS */

    private final MapListBox<Locale,TranslationTable> languagesListBox;
    private final ListBox<String> translationTableListBox;
    private final ListBox<Integer> gradeListBox;
    private final EightDotsCheckBox eightDotsCheckBox;

    /* LABELS */
    
    private final Label translationTableLabel;
    private final Label gradeLabel;
    private final Label eightDotsLabel;
   
    /*****************/
    /* TYPEFACE PAGE */
    /*****************/
    
    /* CONTROLS */

    private final MapListBox<String,CharacterStyle> characterStyleListBox;
    private final CheckBox characterInheritCheckBox;
    private final TextField characterParentField;
    private final ListBox<CharacterStyle.TypefaceOption> characterBoldfaceListBox;
    private final ListBox<CharacterStyle.TypefaceOption> characterItalicListBox;
    private final ListBox<CharacterStyle.TypefaceOption> characterUnderlineListBox;
    private final ListBox<CharacterStyle.TypefaceOption> characterCapitalsListBox;

    /* LABELS */

    private final Label characterStyleLabel;
    private final Label characterInheritLabel;
    private final Label characterBoldfaceLabel;
    private final Label characterItalicLabel;
    private final Label characterUnderlineLabel;
    private final Label characterCapitalsLabel;

    /*******************/
    /* PARAGRAPHS PAGE */
    /*******************/

    /* CONTROLS */

    private final MapListBox<String,ParagraphStyle> paragraphStyleListBox;
    private final CheckBox paragraphInheritCheckBox;
    private final TextField paragraphParentField;
    private final NumericSettingControl paragraphLinesAboveField;
    private final NumericSettingControl paragraphLinesBelowField;
    private final ListBox<Style.Alignment> paragraphAlignmentListBox;
    private final NumericSettingControl paragraphFirstLineField;
    private final NumericSettingControl paragraphRunoversField;
    private final NumericSettingControl paragraphMarginLeftRightField;
    private final CheckBox paragraphKeepEmptyCheckBox;
    private final CheckBox paragraphKeepWithNextCheckBox;
    private final CheckBox paragraphDontSplitCheckBox;
    private final CheckBox paragraphWidowControlCheckBox;
    private final CheckBox paragraphOrphanControlCheckBox;
    private final NumericSettingControl paragraphWidowControlField;
    private final NumericSettingControl paragraphOrphanControlField;

    /* LABELS */

    private final Label paragraphStyleLabel;
    private final Label paragraphInheritLabel;
    private final Label paragraphAlignmentLabel;
    private final Label paragraphFirstLineLabel;
    private final Label paragraphRunoversLabel;
    private final Label paragraphMarginLeftRightLabel;
    private final Label paragraphLinesAboveLabel;
    private final Label paragraphLinesBelowLabel;
    private final Label paragraphKeepEmptyLabel;
    private final Label paragraphKeepWithNextLabel;
    private final Label paragraphDontSplitLabel;
    private final Label paragraphWidowControlLabel;
    private final Label paragraphOrphanControlLabel;

    /* LINES */

    private final FixedLine paragraphSpacingLine;
    private final FixedLine paragraphIndentsLine;
    private final FixedLine paragraphTextFlowLine;

    /*****************/
    /* HEADINGS PAGE */
    /*****************/

    /* CONTROLS */

    private final MapListBox<Integer,HeadingStyle> headingLevelListBox;
    private final ListBox<Style.Alignment> headingAlignmentListBox;
    private final NumericSettingControl headingFirstLineField;
    private final NumericSettingControl headingRunoversField;
    private final NumericSettingControl headingMarginLeftRightField;
    private final NumericSettingControl headingLinesAboveField;
    private final NumericSettingControl headingLinesBelowField;
    private final CheckBox headingNewBraillePageCheckBox;
    private final CheckBox headingKeepWithNextCheckBox;
    private final CheckBox headingDontSplitCheckBox;
    private final CheckBox headingUpperBorderCheckBox;
    private final CheckBox headingLowerBorderCheckBox;
    private final TextPropertyField<Character> headingUpperBorderField;
    private final TextPropertyField<Character> headingLowerBorderField;
    private final CharacterSettingButton headingUpperBorderButton;
    private final CharacterSettingButton headingLowerBorderButton;
    private final NumericSettingControl headingUpperBorderPaddingField;
    private final NumericSettingControl headingLowerBorderPaddingField;

    /* LABELS */

    private final Label headingLevelLabel;
    private final Label headingAlignmentLabel;
    private final Label headingFirstLineLabel;
    private final Label headingRunoversLabel;
    private final Label headingMarginLeftRightLabel;
    private final Label headingLinesAboveLabel;
    private final Label headingLinesBelowLabel;
    private final Label headingNewBraillePageLabel;
    private final Label headingKeepWithNextLabel;
    private final Label headingDontSplitLabel;
    private final Label headingUpperBorderLabel;
    private final Label headingUpperBorderPaddingLabel;
    private final Label headingLowerBorderLabel;
    private final Label headingLowerBorderPaddingLabel;

    /* LINES*/

    private final FixedLine headingSpacingLine;
    private final FixedLine headingIndentsLine;
    private final FixedLine headingTextFlowLine;
    private final FixedLine headingBordersLine;

    /**************/
    /* LISTS PAGE */
    /**************/

    /* CONTROLS */

    private final MapListBox<Integer,ListStyle> listLevelListBox;
    private final NumericSettingControl listLinesAboveField;
    private final NumericSettingControl listLinesBelowField;
    private final NumericSettingControl listLinesBetweenField;
    private final NumericSettingControl listFirstLineField;
    private final NumericSettingControl listRunoversField;
    private final TextPropertyField<String> listPrefixField;
    private final TextSettingButton listPrefixButton;
    private final CheckBox listDontSplitCheckBox;
    private final CheckBox listDontSplitItemsCheckBox;

    /* LABELS */

    private final Label listLinesAboveLabel;
    private final Label listLinesBelowLabel;
    private final Label listLinesBetweenLabel;
    private final Label listLevelLabel;
    private final Label listFirstLineLabel;
    private final Label listRunoversLabel;
    private final Label listPrefixLabel;
    private final Label listDontSplitLabel;
    private final Label listDontSplitItemsLabel;

    /***************/
    /* TABLES PAGE */
    /***************/

    /* CONTROLS */

    private final MapListBox<String,TableStyle> tableStyleListBox;
    private final RadioButton<Boolean> tableSimpleRadioButton;
    private final RadioButton<Boolean> tableStairstepRadioButton;
    private final TextPropertyField<String> tableColumnDelimiterField;
    private final TextSettingButton tableColumnDelimiterButton;
    private final NumericSettingControl tableIndentPerColumnField;
    private final NumericSettingControl tableLinesAboveBelowField;
    private final NumericSettingControl tableLinesBetweenField;
    private final NumericSettingControl tableFirstLineField;
    private final NumericSettingControl tableRunoversField;
    private final CheckBox tableMirrorCheckBox;
    private final CheckBox tableDontSplitRowsCheckBox;
    private final CheckBox tableColumnHeadingsCheckBox;
    private final CheckBox tableRowHeadingsCheckBox;
    private final CheckBox tableRepeatHeadingsCheckBox;
    private final TextPropertyField<String> tableHeadingSuffixField;
    private final TextSettingButton tableHeadingSuffixButton;

    /* LABELS */

    private final Label tableStyleLabel;
    private final Label tableSimpleLabel;
    private final Label tableStairstepLabel;
    private final Label tableColumnDelimiterLabel;
    private final Label tableIndentPerColumnLabel;
    private final Label tableLinesAboveBelowLabel;
    private final Label tableLinesBetweenLabel;
    private final Label tableFirstLineLabel;
    private final Label tableRunoversLabel;
    private final Label tableMirrorLabel;
    private final Label tableDontSplitRowsLabel;
    private final Label tableColumnHeadingsLabel;
    private final Label tableRowHeadingsLabel;
    private final Label tableRepeatHeadingsLabel;
    private final Label tableHeadingSuffixLabel;

    /* LINES */

    private final FixedLine tableSpacingLine;
    private final FixedLine tableTextFlowLine;

    /**************/
    /* NOTES PAGE */
    /**************/

    /* CONTROLS */

    private final MapListBox<String,NoteReferenceFormat> notesNoterefFormatListBox;
    private final TextPropertyField notesNoterefPrefixField;
    private final TextSettingButton notesNoterefPrefixButton;
    private final CheckBox notesNoterefSpaceBeforeCheckBox;
    private final CheckBox notesNoterefSpaceAfterCheckBox;
    private final NumericSettingControl notesFootnoteLinesAboveField;
    private final NumericSettingControl notesFootnoteLinesBelowField;
    private final ListBox<Style.Alignment> notesFootnoteAlignmentListBox;
    private final NumericSettingControl notesFootnoteFirstLineField;
    private final NumericSettingControl notesFootnoteRunoversField;
    private final NumericSettingControl notesFootnoteMarginLeftRightField;

    /* LABELS */

    private final Label notesNoterefFormatLabel;
    private final Label notesNoterefPrefixLabel;
    private final Label notesFootnoteLinesAboveLabel;
    private final Label notesFootnoteLinesBelowLabel;
    private final Label notesFootnoteAlignmentLabel;
    private final Label notesFootnoteFirstLineLabel;
    private final Label notesFootnoteRunoversLabel;
    private final Label notesFootnoteMarginLeftRightLabel;
    private final Label notesNoterefSpaceBeforeLabel;
    private final Label notesNoterefSpaceAfterLabel;

    /* LINES */

    private final FixedLine notesFootnoteLine;
    private final FixedLine notesNoterefLine;

    /*****************/
    /* PICTURES PAGE */
    /*****************/

    /* CONTROLS */

    private final NumericSettingControl picturesFirstLineField;
    private final NumericSettingControl picturesRunoversField;
    private final NumericSettingControl picturesLinesAboveBelowField;
    private final TextPropertyField picturesOpeningMarkField;
    private final TextPropertyField picturesClosingMarkField;
    private final TextSettingControl picturesDescriptionPrefixField;
    private final TextSettingButton picturesOpeningMarkButton;
    private final TextSettingButton picturesClosingMarkButton;

    /* LABELS */

    private final Label picturesFirstLineLabel;
    private final Label picturesRunoversLabel;
    private final Label picturesLinesAboveBelowLabel;
    private final Label picturesOpeningMarkLabel;
    private final Label picturesClosingMarkLabel;
    private final Label picturesDescriptionPrefixLabel;

    /* LINES */

    private final FixedLine picturesSpacingLine;

    /********************/
    /* PAGENUMBERS PAGE */
    /********************/

    /* CONTROLS */

    private final CheckBox braillePageNumbersCheckBox;
    private final ListBox<PageNumberPosition> braillePageNumberAtListBox;
    private final ListBox<PageNumberFormat> preliminaryPageNumberFormatListBox;
    private final NumericSettingControl beginningBraillePageNumberField;
    private final CheckBox printPageNumbersCheckBox;
    private final ListBox<PageNumberPosition> printPageNumberAtListBox;
    private final CheckBox printPageNumberRangeCheckBox;
    private final CheckBox continuePagesCheckBox;
    private final CheckBox pageSeparatorCheckBox;
    private final CheckBox pageSeparatorNumberCheckBox;
    private final CheckBox ignoreEmptyPagesCheckBox;
    private final CheckBox mergeUnnumberedPagesCheckBox;
    private final CheckBox numbersAtTopOnSepLineCheckBox;
    private final CheckBox numbersAtBottomOnSepLineCheckBox;

    /* LABELS */

    private final Label braillePageNumbersLabel;
    private final Label preliminaryPageNumberFormatLabel;
    private final Label beginningBraillePageNumberLabel;
    private final Label printPageNumbersLabel;
    private final Label printPageNumberRangeLabel;
    private final Label continuePagesLabel;
    private final Label pageSeparatorLabel;
    private final Label pageSeparatorNumberLabel;
    private final Label ignoreEmptyPagesLabel;
    private final Label mergeUnnumberedPagesLabel;
    private final Label numbersAtTopOnSepLineLabel;
    private final Label numbersAtBottomOnSepLineLabel;

    /***************************/
    /* DOCUMENT STRUCTURE PAGE */
    /***************************/

    /* CONTROLS */

    private final SectionCheckBox frontmatterCheckBox;
    private final SectionListBox frontmatterListBox;
    private final SectionCheckBox titlePageCheckBox;
    private final SectionListBox titlePageListBox;
    private final SectionCheckBox repeatFrontmatterCheckBox;
    private final SectionListBox repeatFrontmatterListBox;
    private final RadioButton<VolumeManagementMode> singleVolumeRadioButton;
    private final RadioButton<VolumeManagementMode> manualVolumesRadioButton;
    private final RadioButton<VolumeManagementMode> automaticVolumesRadioButton;
    private final NumericSettingControl preferredVolumeSizeField;
    private final NumericSettingControl maxVolumeSizeField;
    private final NumericSettingControl minVolumeSizeField;
    private final NumericSettingControl minLastVolumeSizeField;
    private final SectionCheckBox rearmatterCheckBox;
    private final SectionListBox rearmatterListBox;
    private final RadioButton<VolumeManagementMode> singleRearVolumeRadioButton;
    private final RadioButton<VolumeManagementMode> manualRearVolumesRadioButton;

    /* LABELS */

    private final Label frontmatterLabel;
    private final Label titlePageLabel;
    private final Label repeatFrontmatterLabel;
    private final Label singleVolumeLabel;
    private final Label manualVolumesLabel;
    private final Label automaticVolumesLabel;
    private final Label preferredVolumeSizeLabel;
    private final Label maxVolumeSizeLabel;
    private final Label minVolumeSizeLabel;
    private final Label minLastVolumeSizeLabel;
    private final Label rearmatterLabel;
    private final Label singleRearVolumeLabel;
    private final Label manualRearVolumesLabel;

    /* LINES */

    private final FixedLine frontMatterLine;
    private final FixedLine bodyMatterLine;
    private final FixedLine rearMatterLine;

    /**************************/
    /* VOLUME MANAGEMENT PAGE */
    /**************************/

    /* CONTROLS */

    private final CheckBox preliminaryVolumeCheckBox;
    private final VolumeListBox volumesListBox;
    private final TextSettingControl volumeTitleField;
    private final ListBox<String> volumeSectionListBox;
    private final CheckBox volumeFrontmatterCheckBox;
    private final CheckBox volumeSpecialSymbolsListCheckBox;
    private final CheckBox volumeTranscribersNotePageCheckBox;
    private final CheckBox volumeTableOfContentsCheckBox;

    /* LABELS */

    private final Label preliminaryVolumeLabel;
    private final Label volumeTitleLabel;
    private final Label volumeSectionLabel;
    private final Label volumeFrontmatterLabel;
    private final Label volumeListOfSpecialSymbolsLabel;
    private final Label volumeTranscribersNotePageLabel;
    private final Label volumeTableOfContentsLabel;

    /* LINES */

    private final FixedLine volumesLine;

    /*************************/
    /* TABLE OF CONTENT PAGE */
    /*************************/

    /* CONTROLS */

    private final TextSettingControl tableOfContentsTitleField;
    private final MapListBox<Integer,TocStyle.TocLevelStyle> tableOfContentsLevelListBox;
    private final NumericSettingControl tableOfContentsFirstLineField;
    private final NumericSettingControl tableOfContentsRunoversField;
    private final TextPropertyField<Character> tableOfContentsLineFillField;
    private final CharacterSettingButton tableOfContentsLineFillButton;
    private final CheckBox tableOfContentsPrintPageNumbersCheckBox;
    private final CheckBox tableOfContentsBraillePageNumbersCheckBox;
    private final NumericSettingControl tableOfContentsUptoLevelField;

    /* LABELS */

    private final Label tableOfContentsTitleLabel;
    private final Label tableOfContentsLevelLabel;
    private final Label tableOfContentsFirstLineLabel;
    private final Label tableOfContentsRunoversLabel;
    private final Label tableOfContentsLineFillLabel;
    private final Label tableOfContentsPrintPageNumbersLabel;
    private final Label tableOfContentsBraillePageNumbersLabel;
    private final Label tableOfContentsUptoLevelLabel;

    /* LINES */

    private final FixedLine tableOfContentsIndentsLine;

    /************************/
    /* SPECIAL SYMBOLS PAGE */
    /************************/

    /* CONTROLS */

    private final TextSettingControl specialSymbolsListTitleField;
    private final SpecialSymbolListBox specialSymbolsListBox;
    private final TextPropertyField specialSymbolsSymbolField;
    private final TextSettingButton specialSymbolsSymbolButton;
    private final TextSettingControl specialSymbolsDescriptionField;
    private final RadioButton<SpecialSymbol.Mode> specialSymbolsMode0RadioButton;
    private final RadioButton<SpecialSymbol.Mode> specialSymbolsMode1RadioButton;
    private final RadioButton<SpecialSymbol.Mode> specialSymbolsMode2RadioButton;
    private final RadioButton<SpecialSymbol.Mode> specialSymbolsMode3RadioButton;

    /* LABELS */

    private final Label specialSymbolsListTitleLabel;
    private final Label specialSymbolsSymbolLabel;
    private final Label specialSymbolsDescriptionLabel;
    private final Label specialSymbolsMode0Label;
    private final Label specialSymbolsMode1Label;
    private final Label specialSymbolsMode2Label;
    private final Label specialSymbolsMode3Label;

    /* LINES */

    private final FixedLine specialSymbolsLine;

    /********************/
    /* MATHEMATICS PAGE */
    /********************/

    /* CONTROLS */

    private final ListBox<MathCode> mathListBox;

    /* LABELS */

    private final Label mathLabel;


    /**
     * The dialog is created from an OpenOffice.org .xdl file.
     *
     * @param   xContext
     */
    public SettingsDialog(XComponentContext ctxt,
                          Configuration cfg,
                          ProgressBar pb)
                   throws com.sun.star.uno.Exception {

        logger.entering("SettingsDialog", "<init>");

        pb.increment();

        this.context = ctxt;
        this.settings = cfg;

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(context);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/SettingsDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(context);
        dialog = xDialogProvider.createDialog(dialogUrl);
        XControlContainer container = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        component = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        control = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());

        Locale oooLocale;
        try { oooLocale = UnoUtils.getUILocale(context); } catch (Exception e) {
              oooLocale = Locale.ENGLISH; }

        ResourceBundle bundle = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale);

        windowProperties.setPropertyValue("Title", bundle.getString("settingsDialogTitle"));

        for (int i=0; i<pagesEnabled.length; i++) { pagesEnabled[i] = true; }

        pagesEnabled[LANGUAGES_PAGE-1] = (settings.getTranslationTables().keys().size() > 1);

        SettingMap<Locale,TranslationTable> translationTables = settings.getTranslationTables();
        TranslationTable mainTranslationTable = translationTables.get(settings.mainLocale);

        L10N_alignment.put(Style.Alignment.LEFT,     bundle.getString("left"));
        L10N_alignment.put(Style.Alignment.CENTERED, bundle.getString("center"));
        L10N_alignment.put(Style.Alignment.RIGHT,    bundle.getString("right"));

        L10N_math.put(MathCode.NEMETH,    "Nemeth");
        L10N_math.put(MathCode.UKMATHS,   "UK Maths");
        L10N_math.put(MathCode.MARBURG,   "Marburg");
        L10N_math.put(MathCode.WISKUNDE,  "Woluwe");

        L10N_typeface.put(CharacterStyle.TypefaceOption.FOLLOW_PRINT, bundle.getString("followPrint"));
        L10N_typeface.put(CharacterStyle.TypefaceOption.YES,          bundle.getString("yes"));
        L10N_typeface.put(CharacterStyle.TypefaceOption.NO,           bundle.getString("no"));

        L10N_noterefFormats.put("1", "1, 2, 3,\u2026");
        L10N_noterefFormats.put("A", "A, B, C,\u2026");
        L10N_noterefFormats.put("a", "a, b, c,\u2026");
        L10N_noterefFormats.put("i", "i, ii, iii,\u2026");
        L10N_noterefFormats.put("I", "I, II, III,\u2026");

        L10N_pageNumberFormats.put(PageNumberFormat.BLANK,      "");
        L10N_pageNumberFormats.put(PageNumberFormat.NORMAL,     "1,2,3,\u2026");
        L10N_pageNumberFormats.put(PageNumberFormat.P,          "p1,p2,p3,\u2026");
        L10N_pageNumberFormats.put(PageNumberFormat.S,          "s1,s2,s3,\u2026");
        L10N_pageNumberFormats.put(PageNumberFormat.ROMAN,      "i,ii,iii,\u2026");
        L10N_pageNumberFormats.put(PageNumberFormat.ROMANCAPS,  "I,II,III,\u2026");

        L10N_pageNumberPositions.put(PageNumberPosition.TOP_RIGHT, bundle.getString("top"));
        L10N_pageNumberPositions.put(PageNumberPosition.BOTTOM_RIGHT, bundle.getString("bottom"));

        for (String locale : settings.getTranslationTables().get(settings.mainLocale).locale.options()) {
            try {
                L10N_translationTables.put(locale, bundle.getString("language_" + locale));
            } catch (MissingResourceException e) {
                L10N_translationTables.put(locale, locale);
            }
        }

        for (Locale locale : settings.getTranslationTables().keys()) {
            L10N_languages.put(locale, locale.getDisplayName(oooLocale));
        }

        pb.increment();

        /**********/
        /* COMMON */
        /**********/

        /* ROADMAP */

        String[] roadmapLabels = new String[NUMBER_OF_PAGES];

        roadmapLabels[GENERAL_PAGE-1] = bundle.getString("generalSettingsPageTitle");
        roadmapLabels[LANGUAGES_PAGE-1] = bundle.getString("languageSettingsPageTitle");
        roadmapLabels[TYPEFACE_PAGE-1] = bundle.getString("typefaceSettingsPageTitle");
        roadmapLabels[PARAGRAPHS_PAGE-1] = bundle.getString("paragraphSettingsPageTitle");
        roadmapLabels[HEADINGS_PAGE-1] = bundle.getString("headingSettingsPageTitle");
        roadmapLabels[LISTS_PAGE-1] = bundle.getString("listSettingsPageTitle");
        roadmapLabels[TABLES_PAGE-1] = bundle.getString("tableSettingsPageTitle");
        roadmapLabels[NOTES_PAGE-1] = "Notes";
        roadmapLabels[PICTURES_PAGE-1] = "Pictures";
        roadmapLabels[PAGENUMBERS_PAGE-1] = bundle.getString("pagenumberSettingsPageTitle");
        roadmapLabels[DOCUMENT_LAYOUT_PAGE-1] = "Document Layout";
        roadmapLabels[VOLUME_MANAGEMENT_PAGE-1] = "Volume Management";
        roadmapLabels[TOC_PAGE-1] = bundle.getString("tableOfContentsSettingsPageTitle");
        roadmapLabels[SPECIAL_SYMBOLS_PAGE-1] = bundle.getString("specialSymbolsSettingsPageTitle");
        roadmapLabels[MATH_PAGE-1] = bundle.getString("mathSettingsPageTitle");

        roadmap = new Roadmap(bundle.getString("settingsRoadmapTitle"), roadmapLabels, pagesEnabled);        

        /* BUTTONS */

        okButton = new Button(container.getControl("CommandButton1"),
                              bundle.getString("saveButton")) {
            public void actionPerformed(ActionEvent event) {}
        };

        cancelButton = new Button(container.getControl("CommandButton2"),
                                  bundle.getString("cancelButton")) {
            public void actionPerformed(ActionEvent event) {}
        };

        backButton = new NavigationButton(container.getControl("CommandButton3"),
                                          bundle.getString("backButton")) {
            @Override
            public void updateProperties() {
                boolean enabled = false;
                for (int i=roadmap.getPage()-2; i>=0; i--) {
                    if (pagesEnabled[i]) { enabled = true; break; }
                }
                try {
                    propertySet.setPropertyValue("Enabled", enabled);
                } catch (UnknownPropertyException e) {
                } catch (PropertyVetoException e) {
                } catch (IllegalArgumentException e) {
                } catch (WrappedTargetException e) {
                }
            }

            @Override
            public void actionPerformed(ActionEvent event) {
                if (event.Source.equals(button)) {
                    for (int i=roadmap.getPage()-2; i>=0; i--) {
                        if (pagesEnabled[i]) { roadmap.setPage(i+1); break; }
                    }
                }
            }
        };

        nextButton = new NavigationButton(container.getControl("CommandButton4"),
                                          bundle.getString("nextButton")) {
            @Override
            public void updateProperties() {
                boolean enabled = false;
                for (int i=roadmap.getPage(); i<pagesEnabled.length; i++) {
                    if (pagesEnabled[i]) { enabled = true; break; }
                }
                try {
                    propertySet.setPropertyValue("Enabled", enabled);
                } catch (UnknownPropertyException e) {
                } catch (PropertyVetoException e) {
                } catch (IllegalArgumentException e) {
                } catch (WrappedTargetException e) {
                }
            }

            @Override
            public void actionPerformed(ActionEvent event) {
                if (event.Source.equals(button)) {
                    for (int i=roadmap.getPage(); i<pagesEnabled.length; i++) {
                        if (pagesEnabled[i]) { roadmap.setPage(i+1); break; }
                    }
                }
            }
        };

        /* CONTROLS */

        formattingRulesListBox = new FormattingRulesButton(container.getControl("CommandButton21"),
                                                           container.getControl("ListBox19"));

        /* INITIALIZATION */

        backButton.updateProperties();
        nextButton.updateProperties();

        roadmap.addListener(backButton);
        roadmap.addListener(nextButton);

        pb.increment();

        /****************/
        /* GENERAL PAGE */
        /****************/

        /* CONTROLS */

        creatorField = new TextSettingControl(container.getControl("TextField1"));
        transcribersNotesPageTitleField = new TextSettingControl(container.getControl("TextField3"));

        mainTranslationTableListBox = new ListBox<String>(container.getControl("ListBox1")) {
            @Override
            public String getDisplayValue(String value) {
                return L10N_translationTables.get(value);
            }
            @Override
            public void update() {
                listbox.selectItem(getDisplayValue(property.get()), true);
            }
        };

        mainGradeListBox = new ListBox<Integer>(container.getControl("ListBox2")) {
            @Override
            public String getDisplayValue(Integer value) {
                return "Grade " + value;
            }
        };

        mainEightDotsCheckBox = new EightDotsCheckBox(container.getControl("CheckBox21"));        
        transcriptionInfoCheckBox = new CheckBox(container.getControl("CheckBox1"));        
        volumeInfoCheckBox = new CheckBox(container.getControl("CheckBox2"));        
        hyphenateCheckBox = new CheckBox(container.getControl("CheckBox20"));
        hardPageBreaksCheckBox = new CheckBox(container.getControl("CheckBox19"));

        transcriptionInfoStyleListBox = new ListBox<ParagraphStyle>(container.getControl("ListBox6")) {
            @Override
            public String getDisplayValue(ParagraphStyle value) {
                return value.getDisplayName();
            }
        };

        volumeInfoStyleListBox = new ListBox<ParagraphStyle>(container.getControl("ListBox7")) {
            @Override
            public String getDisplayValue(ParagraphStyle value) {
                return value.getDisplayName();
            }
        };

        minSyllableLengthField = new NumericSettingControl(container.getControl("NumericField40"));
        
        /* LABELS */
        
        creatorLabel = new Label(container.getControl("Label4"),
                                 bundle.getString("creatorLabel") + ":");

        transcribersNotesPageTitleLabel = new Label(container.getControl("Label7"),
                                                    "Transcriber's notes page title:");

        mainTranslationTableLabel = new Label(container.getControl("Label1"),
                                              bundle.getString("languageLabel") + ":");

        mainGradeLabel = new Label(container.getControl("Label2"),
                                   bundle.getString("gradeLabel") + ":");

        mainEightDotsLabel = new Label(container.getControl("Label74"),
                                       bundle.getString("useEightDotsLabel"));

        transcriptionInfoLabel = new Label(container.getControl("Label3"),
                                           bundle.getString("transcriptionInfoLabel"));

        volumeInfoLabel = new Label(container.getControl("Label5"),
                                    bundle.getString("volumeInfoLabel"));

        hyphenateLabel = new Label(container.getControl("Label77"),
                                   bundle.getString("hyphenateLabel"));

        hardPageBreaksLabel = new Label(container.getControl("Label72"),
                                        bundle.getString("hardPageBreaksLabel"));

        transcriptionInfoStyleLabel = new Label(container.getControl("Label98"),
                                                bundle.getString("paragraphStyleLabel") + ":");

        volumeInfoStyleLabel = new Label(container.getControl("Label99"),
                                         bundle.getString("paragraphStyleLabel") + ":");

        minSyllableLengthLabel = new Label(container.getControl("Label117"),
                                           "Don't break words into parts smaller than:");

        /* INITIALIZATION */

        creatorField.link(settings.creator);
        transcribersNotesPageTitleField.link(settings.transcribersNotesPageTitle);
        mainTranslationTableListBox.link(mainTranslationTable.locale);
        mainGradeListBox.link(mainTranslationTable.grade);
        mainEightDotsCheckBox.link(mainTranslationTable.dots);
        transcriptionInfoCheckBox.link(settings.transcriptionInfoEnabled);
        volumeInfoCheckBox.link(settings.volumeInfoEnabled);
        hyphenateCheckBox.link(settings.hyphenate);
        hardPageBreaksCheckBox.link(settings.hardPageBreaks);
        transcriptionInfoStyleListBox.link(settings.transcriptionInfoStyle);
        volumeInfoStyleListBox.link(settings.volumeInfoStyle);
        minSyllableLengthField.link(settings.minSyllableLength);

        pb.increment();

        /******************/
        /* LANGUAGES PAGE */
        /******************/

        /* CONTROLS */

        languagesListBox = new MapListBox<Locale,TranslationTable>(container.getControl("ListBox3"),
                                                                   settings.getTranslationTables()) {
            @Override
            public String getDisplayValue(Locale value) {
                return L10N_languages.get(value);
            }
        };

        translationTableListBox = new ListBox<String>(container.getControl("ListBox4")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.Source.equals(languagesListBox)) {
                    link(languagesListBox.getSelectedItem().locale);
                } else { super.itemStateChanged(event); }
            }
            @Override
            public String getDisplayValue(String value) {
                return L10N_translationTables.get(value);
            }
        };
        
        gradeListBox = new ListBox<Integer>(container.getControl("ListBox5")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.Source.equals(languagesListBox)) {
                    link(languagesListBox.getSelectedItem().grade);
                } else { super.itemStateChanged(event); }
            }
            @Override
            public String getDisplayValue(Integer value) {
                return "Grade " + value;
            }
        };

        eightDotsCheckBox = new EightDotsCheckBox(container.getControl("CheckBox22")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.Source.equals(languagesListBox)) {
                    link(languagesListBox.getSelectedItem().dots);
                } else { super.itemStateChanged(event); }
            }
        };

        /* LABELS */

        translationTableLabel = new Label(container.getControl("Label12"),
                                          bundle.getString("languageLabel") + ":");

        gradeLabel = new Label(container.getControl("Label13"),
                               bundle.getString("gradeLabel") + ":");

        eightDotsLabel = new Label(container.getControl("Label82"),
                                   bundle.getString("useEightDotsLabel"));

        /* INITIALIZATION */

        translationTableListBox.link(languagesListBox.getSelectedItem().locale);
        gradeListBox.link(languagesListBox.getSelectedItem().grade);
        eightDotsCheckBox.link(languagesListBox.getSelectedItem().dots);

        languagesListBox.addListener(translationTableListBox);
        languagesListBox.addListener(gradeListBox);
        languagesListBox.addListener(eightDotsCheckBox);

        pb.increment();

        /*****************/
        /* TYPEFACE PAGE */
        /*****************/

        /* CONTROLS */

        characterStyleListBox = new MapListBox<String,CharacterStyle>(container.getControl("ListBox28"),
                                                                      settings.getCharacterStyles()) {
            @Override
            public int compare(String style1, String style2) {
                if (style1.equals("Default")) { return 1; }
                return super.compare(style1, style2);
            }
            @Override
            public String getDisplayValue(String key) {
                return map.get(key).getDisplayName();
            }
        };

        characterInheritCheckBox = new CheckBox(container.getControl("CheckBox24")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == characterStyleListBox) {
                    link(characterStyleListBox.getSelectedItem().inherit);
                }
            }
        };
        
        characterParentField = new TextField(container.getControl("TextField11")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == characterStyleListBox) {
                    CharacterStyle style = characterStyleListBox.getSelectedItem().getParentStyle();
                    setText(style != null ? style.getDisplayName() : null);
                }
            }
        };

        characterBoldfaceListBox = new ListBox<CharacterStyle.TypefaceOption>(container.getControl("ListBox29")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == characterStyleListBox) {
                    link(characterStyleListBox.getSelectedItem().boldface);
                }
            }
            @Override
            public String getDisplayValue(CharacterStyle.TypefaceOption value) {
                return L10N_typeface.get(value);
            }
        };

        characterItalicListBox = new ListBox<CharacterStyle.TypefaceOption>(container.getControl("ListBox27")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == characterStyleListBox) {
                    link(characterStyleListBox.getSelectedItem().italic);
                }
            }
            @Override
            public String getDisplayValue(CharacterStyle.TypefaceOption value) {
                return L10N_typeface.get(value);
            }
        };

        characterUnderlineListBox = new ListBox<CharacterStyle.TypefaceOption>(container.getControl("ListBox31")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == characterStyleListBox) {
                    link(characterStyleListBox.getSelectedItem().underline);
                }
            }
            @Override
            public String getDisplayValue(CharacterStyle.TypefaceOption value) {
                return L10N_typeface.get(value);
            }
        };

        characterCapitalsListBox = new ListBox<CharacterStyle.TypefaceOption>(container.getControl("ListBox30")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == characterStyleListBox) {
                    link(characterStyleListBox.getSelectedItem().capitals);
                }
            }
            @Override
            public String getDisplayValue(CharacterStyle.TypefaceOption value) {
                return L10N_typeface.get(value);
            }
        };

        /* LABELS */

        characterStyleLabel = new Label(container.getControl("Label90"),
                                        bundle.getString("styleLabel") + ":");

        characterInheritLabel = new Label(container.getControl("Label91"),
                                          bundle.getString("inheritLabel") + ":");

        characterBoldfaceLabel = new Label(container.getControl("Label85"),
                                           bundle.getString("characterBoldfaceLabel"));

        characterItalicLabel = new Label(container.getControl("Label89"),
                                         bundle.getString("characterItalicLabel"));

        characterUnderlineLabel = new Label(container.getControl("Label87"),
                                            bundle.getString("characterUnderlineLabel"));

        characterCapitalsLabel = new Label(container.getControl("Label86"),
                                           bundle.getString("characterCapitalsLabel"));

        /* INITIALIZATION */

        characterInheritCheckBox.link(characterStyleListBox.getSelectedItem().inherit);
        CharacterStyle charStyle = characterStyleListBox.getSelectedItem().getParentStyle();
        characterParentField.setText(charStyle != null ? charStyle.getDisplayName() : null);
        characterBoldfaceListBox.link(characterStyleListBox.getSelectedItem().boldface);
        characterItalicListBox.link(characterStyleListBox.getSelectedItem().italic);
        characterUnderlineListBox.link(characterStyleListBox.getSelectedItem().underline);
        characterCapitalsListBox.link(characterStyleListBox.getSelectedItem().capitals);

        characterStyleListBox.addListener(characterInheritCheckBox);
        characterStyleListBox.addListener(characterParentField);
        characterStyleListBox.addListener(characterBoldfaceListBox);
        characterStyleListBox.addListener(characterItalicListBox);
        characterStyleListBox.addListener(characterUnderlineListBox);
        characterStyleListBox.addListener(characterCapitalsListBox);

        pb.increment();

        /*******************/
        /* PARAGRAPHS PAGE */
        /*******************/

        /* CONTROLS */

        paragraphStyleListBox = new MapListBox<String,ParagraphStyle>(container.getControl("ListBox26"),
                                                                      settings.getParagraphStyles()) {
            @Override
            public int compare(String style1, String style2) {
                if (style1.equals("Standard")) { return 1; }
                return super.compare(style1, style2);
            }
            @Override
            public String getDisplayValue(String key) {
                return map.get(key).getDisplayName();
            }
        };

        paragraphInheritCheckBox = new CheckBox(container.getControl("CheckBox23")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().inherit);
                }
            }
        };

        paragraphParentField = new TextField(container.getControl("TextField10")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    ParagraphStyle style = paragraphStyleListBox.getSelectedItem().getParentStyle();
                    setText(style != null ? style.getDisplayName() : null);
                }
            }
        };

        paragraphAlignmentListBox = new AlignmentListBox(container.getControl("ListBox12")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().alignment);
                }
            }
        };

        paragraphFirstLineField = new NumericSettingControl(container.getControl("NumericField7")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().firstLine);
                }
            }
        };

        paragraphRunoversField = new NumericSettingControl(container.getControl("NumericField8")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().runovers);
                }
            }
        };

        paragraphMarginLeftRightField = new NumericSettingControl(container.getControl("NumericField2")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().marginLeftRight);
                }
            }
        };

        paragraphLinesAboveField = new NumericSettingControl(container.getControl("NumericField9")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().linesAbove);
                }
            }
        };

        paragraphLinesBelowField = new NumericSettingControl(container.getControl("NumericField10")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().linesBelow);
                }
            }
        };

        paragraphKeepEmptyCheckBox = new CheckBox(container.getControl("CheckBox9")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().keepEmptyParagraphs);
                }
            }
        };

        paragraphKeepWithNextCheckBox = new CheckBox(container.getControl("CheckBox27")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().keepWithNext);
                }
            }
        };

        paragraphDontSplitCheckBox = new CheckBox(container.getControl("CheckBox28")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().dontSplit);
                }
            }
        };

        paragraphWidowControlCheckBox = new CheckBox(container.getControl("CheckBox32")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().widowControlEnabled);
                }
            }
        };

        paragraphOrphanControlCheckBox = new CheckBox(container.getControl("CheckBox31")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().orphanControlEnabled);
                }
            }
        };

        paragraphWidowControlField = new NumericSettingControl(container.getControl("NumericField26")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().widowControl);
                }
            }
        };

        paragraphOrphanControlField = new NumericSettingControl(container.getControl("NumericField16")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == paragraphStyleListBox) {
                    link(paragraphStyleListBox.getSelectedItem().orphanControl);
                }
            }
        };

        /* LABELS */

        paragraphStyleLabel = new Label(container.getControl("Label83"),
                                        bundle.getString("styleLabel") + ":");

        paragraphInheritLabel = new Label(container.getControl("Label84"),
                                          bundle.getString("inheritLabel") + ":");

        paragraphAlignmentLabel = new Label(container.getControl("Label37"),
                                            bundle.getString("alignmentLabel") + ":");

        paragraphFirstLineLabel = new Label(container.getControl("Label31"),
                                            bundle.getString("firstLineLabel") + ":");

        paragraphRunoversLabel = new Label(container.getControl("Label32"),
                                           bundle.getString("runoversLabel") + ":");

        paragraphMarginLeftRightLabel = new Label(container.getControl("Label19"),
                                                  bundle.getString("centeredMarginLabel") + ":");

        paragraphLinesAboveLabel = new Label(container.getControl("Label33"),
                                             bundle.getString("linesAboveLabel") + ":");

        paragraphLinesBelowLabel = new Label(container.getControl("Label34"),
                                             bundle.getString("linesBelowLabel") + ":");

        paragraphKeepEmptyLabel = new Label(container.getControl("Label14"),
                                            bundle.getString("paragraphKeepEmptyLabel"));

        paragraphKeepWithNextLabel = new Label(container.getControl("Label70"),
                                               bundle.getString("keepWithNextLabel"));

        paragraphDontSplitLabel = new Label(container.getControl("Label71"),
                                            bundle.getString("dontSplitLabel"));

        paragraphWidowControlLabel = new Label(container.getControl("Label92"),
                                               bundle.getString("widowControlLabel") + ":");

        paragraphOrphanControlLabel = new Label(container.getControl("Label88"),
                                                bundle.getString("orphanControlLabel") + ":");

        /* LINES */

        paragraphSpacingLine = new FixedLine(container.getControl("FixedLine8"),
                                             bundle.getString("spacingLabel"));

        paragraphIndentsLine = new FixedLine(container.getControl("FixedLine11"),
                                             "Alignment & Indents");

        paragraphTextFlowLine = new FixedLine(container.getControl("FixedLine12"),
                                              "Text Flow");

        /* INITIALIZATION */

        paragraphInheritCheckBox.link(paragraphStyleListBox.getSelectedItem().inherit);
        ParagraphStyle paraStyle = paragraphStyleListBox.getSelectedItem().getParentStyle();
        paragraphParentField.setText(paraStyle != null ? paraStyle.getDisplayName() : null);
        paragraphAlignmentListBox.link(paragraphStyleListBox.getSelectedItem().alignment);
        paragraphFirstLineField.link(paragraphStyleListBox.getSelectedItem().firstLine);
        paragraphRunoversField.link(paragraphStyleListBox.getSelectedItem().runovers);
        paragraphMarginLeftRightField.link(paragraphStyleListBox.getSelectedItem().marginLeftRight);
        paragraphLinesAboveField.link(paragraphStyleListBox.getSelectedItem().linesAbove);
        paragraphLinesBelowField.link(paragraphStyleListBox.getSelectedItem().linesBelow);
        paragraphKeepEmptyCheckBox.link(paragraphStyleListBox.getSelectedItem().keepEmptyParagraphs);
        paragraphKeepWithNextCheckBox.link(paragraphStyleListBox.getSelectedItem().keepWithNext);
        paragraphDontSplitCheckBox.link(paragraphStyleListBox.getSelectedItem().dontSplit);
        paragraphWidowControlCheckBox.link(paragraphStyleListBox.getSelectedItem().widowControlEnabled);
        paragraphOrphanControlCheckBox.link(paragraphStyleListBox.getSelectedItem().orphanControlEnabled);
        paragraphWidowControlField.link(paragraphStyleListBox.getSelectedItem().widowControl);
        paragraphOrphanControlField.link(paragraphStyleListBox.getSelectedItem().orphanControl);

        paragraphStyleListBox.addListener(paragraphInheritCheckBox);
        paragraphStyleListBox.addListener(paragraphParentField);
        paragraphStyleListBox.addListener(paragraphAlignmentListBox);
        paragraphStyleListBox.addListener(paragraphFirstLineField);
        paragraphStyleListBox.addListener(paragraphRunoversField);
        paragraphStyleListBox.addListener(paragraphMarginLeftRightField);
        paragraphStyleListBox.addListener(paragraphLinesAboveField);
        paragraphStyleListBox.addListener(paragraphLinesBelowField);
        paragraphStyleListBox.addListener(paragraphKeepEmptyCheckBox);
        paragraphStyleListBox.addListener(paragraphKeepWithNextCheckBox);
        paragraphStyleListBox.addListener(paragraphDontSplitCheckBox);
        paragraphStyleListBox.addListener(paragraphWidowControlCheckBox);
        paragraphStyleListBox.addListener(paragraphOrphanControlCheckBox);
        paragraphStyleListBox.addListener(paragraphWidowControlField);
        paragraphStyleListBox.addListener(paragraphOrphanControlField);

        pb.increment();

        /*****************/
        /* HEADINGS PAGE */
        /*****************/

        /* CONTROLS */

        headingLevelListBox = new MapListBox<Integer,HeadingStyle>(container.getControl("ListBox13"),
                                                                   settings.getHeadingStyles()) {
            @Override
            public int compare(Integer level1, Integer level2) {
                return level1.compareTo(level2);
            }
        };

        headingAlignmentListBox = new AlignmentListBox(container.getControl("ListBox14")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().alignment);
                }
            }
        };

        headingFirstLineField = new NumericSettingControl(container.getControl("NumericField11")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().firstLine);
                }
            }
        };

        headingRunoversField = new NumericSettingControl(container.getControl("NumericField12")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().runovers);
                }
            }
        };

        headingMarginLeftRightField = new NumericSettingControl(container.getControl("NumericField3")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().marginLeftRight);
                }
            }
        };

        headingLinesAboveField = new NumericSettingControl(container.getControl("NumericField13")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().linesAbove);
                }
            }
        };

        headingLinesBelowField = new NumericSettingControl(container.getControl("NumericField14")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().linesBelow);
                }
            }
        };

        headingNewBraillePageCheckBox = new CheckBox(container.getControl("CheckBox10")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().newBraillePage);
                }
            }
        };

        headingKeepWithNextCheckBox = new CheckBox(container.getControl("CheckBox29")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().keepWithNext);
                }
            }
        };

        headingDontSplitCheckBox = new CheckBox(container.getControl("CheckBox30")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().dontSplit);
                }
            }
        };

        headingUpperBorderCheckBox = new CheckBox(container.getControl("CheckBox36")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().upperBorderEnabled);
                }
            }
        };

        headingLowerBorderCheckBox = new CheckBox(container.getControl("CheckBox37")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().lowerBorderEnabled);
                }
            }
        };

        headingUpperBorderField = new TextPropertyField(container.getControl("TextField12")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().upperBorderStyle);
                }
            }
        };

        headingLowerBorderField = new TextPropertyField(container.getControl("TextField13")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().lowerBorderStyle);
                }
            }
        };

        headingUpperBorderPaddingField = new NumericSettingControl(container.getControl("NumericField25")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().paddingAbove);
                }
            }
        };

        headingLowerBorderPaddingField = new NumericSettingControl(container.getControl("NumericField29")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().paddingBelow);
                }
            }
        };

        headingUpperBorderButton = new CharacterSettingButton(container.getControl("CommandButton13"), "...") {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().upperBorderStyle);
                }
            }
        };

        headingLowerBorderButton = new CharacterSettingButton(container.getControl("CommandButton14"), "...") {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == headingLevelListBox) {
                    link(headingLevelListBox.getSelectedItem().lowerBorderStyle);
                }
            }
        };

        /* LABELS */

        headingLevelLabel = new Label(container.getControl("Label42"),
                                      bundle.getString("levelLabel") + ":");

        headingAlignmentLabel = new Label(container.getControl("Label43"),
                                          bundle.getString("alignmentLabel") + ":");

        headingFirstLineLabel = new Label(container.getControl("Label38"),
                                          bundle.getString("firstLineLabel") + ":");

        headingRunoversLabel = new Label(container.getControl("Label39"),
                                         bundle.getString("runoversLabel") + ":");

        headingMarginLeftRightLabel = new Label(container.getControl("Label20"),
                                                bundle.getString("centeredMarginLabel") + ":");

        headingLinesAboveLabel = new Label(container.getControl("Label40"),
                                           bundle.getString("linesAboveLabel") + ":");

        headingLinesBelowLabel = new Label(container.getControl("Label41"),
                                           bundle.getString("linesBelowLabel") + ":");

        headingNewBraillePageLabel = new Label(container.getControl("Label15"),
                                               bundle.getString("headingNewBraillePageLabel"));

        headingKeepWithNextLabel = new Label(container.getControl("Label93"),
                                             bundle.getString("keepWithNextLabel"));

        headingDontSplitLabel = new Label(container.getControl("Label94"),
                                          bundle.getString("dontSplitLabel"));

        headingUpperBorderLabel = new Label(container.getControl("Label54"),
                                            "Top:");

        headingUpperBorderPaddingLabel = new Label(container.getControl("Label62"),
                                                   "Padding:");

        headingLowerBorderLabel = new Label(container.getControl("Label57"),
                                            "Bottom:");

        headingLowerBorderPaddingLabel = new Label(container.getControl("Label100"),
                                                   "Padding:");

        /* LINES */

        headingSpacingLine = new FixedLine(container.getControl("FixedLine13"),
                                           bundle.getString("spacingLabel"));

        headingIndentsLine = new FixedLine(container.getControl("FixedLine14"),
                                           "Alignment & Indents");

        headingTextFlowLine = new FixedLine(container.getControl("FixedLine15"),
                                            "Text Flow");

        headingBordersLine = new FixedLine(container.getControl("FixedLine16"),
                                           "Borders");

        /* INITIALIZATION */

        headingAlignmentListBox.link(headingLevelListBox.getSelectedItem().alignment);
        headingFirstLineField.link(headingLevelListBox.getSelectedItem().firstLine);
        headingRunoversField.link(headingLevelListBox.getSelectedItem().runovers);
        headingMarginLeftRightField.link(headingLevelListBox.getSelectedItem().marginLeftRight);
        headingLinesAboveField.link(headingLevelListBox.getSelectedItem().linesAbove);
        headingLinesBelowField.link(headingLevelListBox.getSelectedItem().linesBelow);
        headingNewBraillePageCheckBox.link(headingLevelListBox.getSelectedItem().newBraillePage);
        headingKeepWithNextCheckBox.link(headingLevelListBox.getSelectedItem().keepWithNext);
        headingDontSplitCheckBox.link(headingLevelListBox.getSelectedItem().dontSplit);
        headingUpperBorderCheckBox.link(headingLevelListBox.getSelectedItem().upperBorderEnabled);
        headingLowerBorderCheckBox.link(headingLevelListBox.getSelectedItem().lowerBorderEnabled);
        headingUpperBorderField.link(headingLevelListBox.getSelectedItem().upperBorderStyle);
        headingLowerBorderField.link(headingLevelListBox.getSelectedItem().lowerBorderStyle);
        headingUpperBorderPaddingField.link(headingLevelListBox.getSelectedItem().paddingAbove);
        headingLowerBorderPaddingField.link(headingLevelListBox.getSelectedItem().paddingBelow);
        headingUpperBorderButton.link(headingLevelListBox.getSelectedItem().upperBorderStyle);
        headingLowerBorderButton.link(headingLevelListBox.getSelectedItem().lowerBorderStyle);

        headingLevelListBox.addListener(headingAlignmentListBox);
        headingLevelListBox.addListener(headingFirstLineField);
        headingLevelListBox.addListener(headingRunoversField);
        headingLevelListBox.addListener(headingMarginLeftRightField);
        headingLevelListBox.addListener(headingLinesAboveField);
        headingLevelListBox.addListener(headingLinesBelowField);
        headingLevelListBox.addListener(headingNewBraillePageCheckBox);
        headingLevelListBox.addListener(headingKeepWithNextCheckBox);
        headingLevelListBox.addListener(headingDontSplitCheckBox);
        headingLevelListBox.addListener(headingUpperBorderCheckBox);
        headingLevelListBox.addListener(headingUpperBorderField);
        headingLevelListBox.addListener(headingUpperBorderPaddingField);
        headingLevelListBox.addListener(headingLowerBorderCheckBox);
        headingLevelListBox.addListener(headingLowerBorderField);
        headingLevelListBox.addListener(headingLowerBorderPaddingField);
        headingLevelListBox.addListener(headingUpperBorderButton);
        headingLevelListBox.addListener(headingLowerBorderButton);

        pb.increment();

        /**************/
        /* LISTS PAGE */
        /**************/

        /* CONTROLS */

        listLevelListBox = new MapListBox<Integer,ListStyle>(container.getControl("ListBox11"),
                                                             settings.getListStyles()) {
            @Override
            public int compare(Integer level1, Integer level2) {
                return level1.compareTo(level2);
            }
        };

        listLinesAboveField = new NumericSettingControl(container.getControl("NumericField17")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().linesAbove);
                }
            }
        };

        listLinesBelowField = new NumericSettingControl(container.getControl("NumericField18")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().linesBelow);
                }
            }
        };

        listLinesBetweenField = new NumericSettingControl(container.getControl("NumericField27")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().linesBetween);
                }
            }
        };

        listFirstLineField = new NumericSettingControl(container.getControl("NumericField15")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().firstLine);
                }
            }
        };

        listRunoversField = new NumericSettingControl(container.getControl("NumericField4")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().runovers);
                }
            }
        };

        listPrefixField = new TextPropertyField<String>(container.getControl("TextField7")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().prefix);
                }
            }
        };

        listPrefixButton = new TextSettingButton(container.getControl("CommandButton7"), "...") {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().prefix);
                }
            }
        };

        listDontSplitCheckBox = new CheckBox(container.getControl("CheckBox34")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().dontSplit);
                }
            }
        };

        listDontSplitItemsCheckBox = new CheckBox(container.getControl("CheckBox35")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == listLevelListBox) {
                    link(listLevelListBox.getSelectedItem().dontSplitItems);
                }
            }
        };


        /* LABELS */

        listLevelLabel = new Label(container.getControl("Label44"),
                                   bundle.getString("levelLabel") + ":");

        listLinesAboveLabel = new Label(container.getControl("Label30"),
                                        bundle.getString("listLinesAboveLabel") + ":");

        listLinesBelowLabel = new Label(container.getControl("Label36"),
                                        bundle.getString("listLinesBelowLabel") + ":");

        listLinesBetweenLabel = new Label(container.getControl("Label60"),
                                          bundle.getString("listLinesBetweenLabel") + ":");

        listFirstLineLabel = new Label(container.getControl("Label28"),
                                       bundle.getString("firstLineLabel") + ":");

        listRunoversLabel = new Label(container.getControl("Label21"),
                                      bundle.getString("runoversLabel") + ":");

        listPrefixLabel = new Label(container.getControl("Label27"),
                                    bundle.getString("listPrefixLabel") + ":");

        listDontSplitLabel = new Label(container.getControl("Label96"),
                                       bundle.getString("listDontSplitLabel"));

        listDontSplitItemsLabel = new Label(container.getControl("Label97"),
                                            bundle.getString("listDontSplitItemsLabel"));

        /* INITIALIZATION */

        listLinesAboveField.link(listLevelListBox.getSelectedItem().linesAbove);
        listLinesBelowField.link(listLevelListBox.getSelectedItem().linesBelow);
        listLinesBetweenField.link(listLevelListBox.getSelectedItem().linesBetween);
        listFirstLineField.link(listLevelListBox.getSelectedItem().firstLine);
        listRunoversField.link(listLevelListBox.getSelectedItem().runovers);
        listPrefixField.link(listLevelListBox.getSelectedItem().prefix);
        listPrefixButton.link(listLevelListBox.getSelectedItem().prefix);
        listDontSplitCheckBox.link(listLevelListBox.getSelectedItem().dontSplit);
        listDontSplitItemsCheckBox.link(listLevelListBox.getSelectedItem().dontSplitItems);
        
        listLevelListBox.addListener(listLinesAboveField);
        listLevelListBox.addListener(listLinesBelowField);
        listLevelListBox.addListener(listLinesBetweenField);
        listLevelListBox.addListener(listFirstLineField);
        listLevelListBox.addListener(listRunoversField);
        listLevelListBox.addListener(listPrefixField);
        listLevelListBox.addListener(listPrefixButton);
        listLevelListBox.addListener(listDontSplitCheckBox);
        listLevelListBox.addListener(listDontSplitItemsCheckBox);

        pb.increment();

        /***************/
        /* TABLES PAGE */
        /***************/

        /* CONTROLS */

        tableStyleListBox = new MapListBox<String,TableStyle>(container.getControl("ListBox15"),
                                                              settings.getTableStyles());

        tableSimpleRadioButton = new RadioButton<Boolean>(container.getControl("OptionButton3")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().stairstepEnabled);
                }
            }
        };

        tableStairstepRadioButton = new RadioButton<Boolean>(container.getControl("OptionButton4")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().stairstepEnabled);
                }
            }
        };

        tableColumnDelimiterField = new TextPropertyField<String>(container.getControl("TextField5")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().columnDelimiter);
                }
            }
        };

        tableIndentPerColumnField = new NumericSettingControl(container.getControl("NumericField5")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().indentPerColumn);
                }
            }
        };

        tableLinesAboveBelowField = new NumericSettingControl(container.getControl("NumericField21")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().linesAbove);
                }
            }
        };

        tableLinesBetweenField = new NumericSettingControl(container.getControl("NumericField28")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().linesBetween);
                }
            }
        };

        tableFirstLineField = new NumericSettingControl(container.getControl("NumericField19")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().firstLine);
                }
            }
        };

        tableRunoversField = new NumericSettingControl(container.getControl("NumericField20")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().runovers);
                }
            }
        };

        tableMirrorCheckBox = new CheckBox(container.getControl("CheckBox42")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().mirrorTable);
                }
            }
        };
        
        tableDontSplitRowsCheckBox = new CheckBox(container.getControl("CheckBox40")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().dontSplitRows);
                }
            }
        };

        tableColumnHeadingsCheckBox = new CheckBox(container.getControl("CheckBox41")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().columnHeadings);
                }
            }
        };

        tableRowHeadingsCheckBox = new CheckBox(container.getControl("CheckBox43")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().rowHeadings);
                }
            }
        };

        tableRepeatHeadingsCheckBox = new CheckBox(container.getControl("CheckBox33")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().repeatHeading);
                }
            }
        };

        tableHeadingSuffixField = new TextPropertyField<String>(container.getControl("TextField15")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().headingSuffix);
                }
            }
        };

        tableColumnDelimiterButton = new TextSettingButton(container.getControl("CommandButton5"), "...") {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().columnDelimiter);
                }
            }
        };

        tableHeadingSuffixButton = new TextSettingButton(container.getControl("CommandButton16"), "...") {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableStyleListBox) {
                    link(tableStyleListBox.getSelectedItem().headingSuffix);
                }
            }
        };

        /* LABELS */

        tableStyleLabel = new Label(container.getControl("Label29"),
                                    "Table style:");

        tableSimpleLabel = new Label(container.getControl("Label23"),
                                     bundle.getString("simpleTableLabel"));

        tableStairstepLabel = new Label(container.getControl("Label25"),
                                        bundle.getString("stairstepTableLabel"));

        tableColumnDelimiterLabel = new Label(container.getControl("Label24"),
                                              bundle.getString("columnDelimiterLabel") + ":");

        tableIndentPerColumnLabel = new Label(container.getControl("Label22"),
                                              "Indent per column:");

        tableLinesAboveBelowLabel = new Label(container.getControl("Label48"),
                                              bundle.getString("linesAboveLabel") + "/" + bundle.getString("linesBelowLabel") + ":");

        tableLinesBetweenLabel = new Label(container.getControl("Label59"),
                                           bundle.getString("tableLinesBetweenLabel") + ":");

        tableFirstLineLabel = new Label(container.getControl("Label46"),
                                        bundle.getString("firstLineLabel") + ":");

        tableRunoversLabel = new Label(container.getControl("Label47"),
                                       bundle.getString("runoversLabel") + ":");

        tableMirrorLabel = new Label(container.getControl("Label121"),
                                     "Invert reading order");

        tableDontSplitRowsLabel = new Label(container.getControl("Label45"),
                                            bundle.getString("tableDontSplitRowsLabel"));

        tableColumnHeadingsLabel = new Label(container.getControl("Label120"),
                                             "Column headings in first row");

        tableRowHeadingsLabel = new Label(container.getControl("Label50"),
                                          "Row headings in first column");

        tableRepeatHeadingsLabel = new Label(container.getControl("Label95"),
                                             "Repeat headings");

        tableHeadingSuffixLabel = new Label(container.getControl("Label49"),
                                            "Heading suffix:");

        /* LINES */

        tableSpacingLine = new FixedLine(container.getControl("FixedLine4"),
                                         "Spacing & Indents");
        tableTextFlowLine = new FixedLine(container.getControl("FixedLine22"),
                                          "Text Flow");

        /* INITIALIZATION */

        tableSimpleRadioButton.setCondition(false);
        tableStairstepRadioButton.setCondition(true);

        tableSimpleRadioButton.link(tableStyleListBox.getSelectedItem().stairstepEnabled);
        tableStairstepRadioButton.link(tableStyleListBox.getSelectedItem().stairstepEnabled);
        tableColumnDelimiterField.link(tableStyleListBox.getSelectedItem().columnDelimiter);
        tableIndentPerColumnField.link(tableStyleListBox.getSelectedItem().indentPerColumn);
        tableLinesAboveBelowField.link(tableStyleListBox.getSelectedItem().linesAbove);
        tableLinesBetweenField.link(tableStyleListBox.getSelectedItem().linesBetween);
        tableFirstLineField.link(tableStyleListBox.getSelectedItem().firstLine);
        tableRunoversField.link(tableStyleListBox.getSelectedItem().runovers);
        tableMirrorCheckBox.link(tableStyleListBox.getSelectedItem().mirrorTable);
        tableDontSplitRowsCheckBox.link(tableStyleListBox.getSelectedItem().dontSplit);
        tableColumnHeadingsCheckBox.link(tableStyleListBox.getSelectedItem().columnHeadings);
        tableRowHeadingsCheckBox.link(tableStyleListBox.getSelectedItem().rowHeadings);
        tableRepeatHeadingsCheckBox.link(tableStyleListBox.getSelectedItem().repeatHeading);
        tableHeadingSuffixField.link(tableStyleListBox.getSelectedItem().headingSuffix);
        tableColumnDelimiterButton.link(tableStyleListBox.getSelectedItem().columnDelimiter);
        tableHeadingSuffixButton.link(tableStyleListBox.getSelectedItem().headingSuffix);
        
        tableStyleListBox.addListener(tableSimpleRadioButton);
        tableStyleListBox.addListener(tableStairstepRadioButton);
        tableStyleListBox.addListener(tableColumnDelimiterField);
        tableStyleListBox.addListener(tableIndentPerColumnField);
        tableStyleListBox.addListener(tableLinesAboveBelowField);
        tableStyleListBox.addListener(tableLinesBetweenField);
        tableStyleListBox.addListener(tableFirstLineField);
        tableStyleListBox.addListener(tableRunoversField);
        tableStyleListBox.addListener(tableMirrorCheckBox);
        tableStyleListBox.addListener(tableDontSplitRowsCheckBox);
        tableStyleListBox.addListener(tableColumnHeadingsCheckBox);
        tableStyleListBox.addListener(tableRowHeadingsCheckBox);
        tableStyleListBox.addListener(tableRepeatHeadingsCheckBox);
        tableStyleListBox.addListener(tableHeadingSuffixField);
        tableStyleListBox.addListener(tableColumnDelimiterButton);
        tableStyleListBox.addListener(tableHeadingSuffixButton);

        pb.increment();

        /**************/
        /* NOTES PAGE */
        /**************/

        /* CONTROLS */

        notesNoterefFormatListBox = new MapListBox<String,NoteReferenceFormat>(container.getControl("ListBox8"),
                                                                               settings.getNoteReferenceFormats()) {
            @Override
            public String getDisplayValue(String key) {
                return L10N_noterefFormats.get(key);
            }
        };

        notesNoterefPrefixField = new TextPropertyField(container.getControl("TextField14")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == notesNoterefFormatListBox) {
                    link(notesNoterefFormatListBox.getSelectedItem().prefix);
                }
            }
        };

        notesNoterefPrefixButton = new TextSettingButton(container.getControl("CommandButton15"), "...") {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == notesNoterefFormatListBox) {
                    link(notesNoterefFormatListBox.getSelectedItem().prefix);
                }
            }
        };

        notesNoterefSpaceBeforeCheckBox = new CheckBox(container.getControl("CheckBox38")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == notesNoterefFormatListBox) {
                    link(notesNoterefFormatListBox.getSelectedItem().spaceBefore);
                }
            }
        };

        notesNoterefSpaceAfterCheckBox = new CheckBox(container.getControl("CheckBox39")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == notesNoterefFormatListBox) {
                    link(notesNoterefFormatListBox.getSelectedItem().spaceAfter);
                }
            }
        };

        notesFootnoteLinesAboveField = new NumericSettingControl(container.getControl("NumericField31"));
        notesFootnoteLinesBelowField = new NumericSettingControl(container.getControl("NumericField32"));
        notesFootnoteAlignmentListBox = new AlignmentListBox(container.getControl("ListBox9"));
        notesFootnoteFirstLineField = new NumericSettingControl(container.getControl("NumericField33"));
        notesFootnoteRunoversField = new NumericSettingControl(container.getControl("NumericField34"));
        notesFootnoteMarginLeftRightField = new NumericSettingControl(container.getControl("NumericField35"));

        /* LABELS */

        notesNoterefFormatLabel = new Label(container.getControl("Label102"),
                                            "Format");

        notesNoterefPrefixLabel = new Label(container.getControl("Label103"),
                                            "Prefix:");
        
        notesNoterefSpaceBeforeLabel = new Label(container.getControl("Label118"),
                                                 "Space before");
        
        notesNoterefSpaceAfterLabel = new Label(container.getControl("Label119"),
                                                "Space after");

        notesFootnoteLinesAboveLabel = new Label(container.getControl("Label105"),
                                                 bundle.getString("linesAboveLabel") + ":");

        notesFootnoteLinesBelowLabel = new Label(container.getControl("Label107"),
                                                 bundle.getString("linesBelowLabel") + ":");

        notesFootnoteAlignmentLabel = new Label(container.getControl("Label106"),
                                                bundle.getString("alignmentLabel") + ":");

        notesFootnoteFirstLineLabel = new Label(container.getControl("Label104"),
                                                bundle.getString("firstLineLabel") + ":");

        notesFootnoteRunoversLabel = new Label(container.getControl("Label108"),
                                               bundle.getString("runoversLabel") + ":");

        notesFootnoteMarginLeftRightLabel = new Label(container.getControl("Label109"),
                                                      bundle.getString("centeredMarginLabel") + ":");

        /* LINES */

        notesFootnoteLine = new FixedLine(container.getControl("FixedLine18"), "Footnotes");
        notesNoterefLine = new FixedLine(container.getControl("FixedLine17"), "Note references");

        /* INITIALIZATION */

        notesNoterefPrefixField.link(notesNoterefFormatListBox.getSelectedItem().prefix);
        notesNoterefPrefixButton.link(notesNoterefFormatListBox.getSelectedItem().prefix);
        notesNoterefSpaceBeforeCheckBox.link(notesNoterefFormatListBox.getSelectedItem().spaceBefore);
        notesNoterefSpaceAfterCheckBox.link(notesNoterefFormatListBox.getSelectedItem().spaceAfter);
        notesFootnoteLinesAboveField.link(settings.getFootnoteStyle().linesAbove);
        notesFootnoteLinesBelowField.link(settings.getFootnoteStyle().linesBelow);
        notesFootnoteAlignmentListBox.link(settings.getFootnoteStyle().alignment);
        notesFootnoteFirstLineField.link(settings.getFootnoteStyle().firstLine);
        notesFootnoteRunoversField.link(settings.getFootnoteStyle().runovers);
        notesFootnoteMarginLeftRightField.link(settings.getFootnoteStyle().marginLeftRight);

        notesNoterefFormatListBox.addListener(notesNoterefPrefixField);
        notesNoterefFormatListBox.addListener(notesNoterefPrefixButton);
        notesNoterefFormatListBox.addListener(notesNoterefSpaceBeforeCheckBox);
        notesNoterefFormatListBox.addListener(notesNoterefSpaceAfterCheckBox);

        pb.increment();

        /*****************/
        /* PICTURES PAGE */
        /*****************/

        /* CONTROLS */

        picturesFirstLineField = new NumericSettingControl(container.getControl("NumericField22"));
        picturesRunoversField = new NumericSettingControl(container.getControl("NumericField41"));
        picturesLinesAboveBelowField = new NumericSettingControl(container.getControl("NumericField6"));
        picturesOpeningMarkField = new TextPropertyField(container.getControl("TextField17"));
        picturesClosingMarkField = new TextPropertyField(container.getControl("TextField16"));
        picturesDescriptionPrefixField = new TextSettingControl(container.getControl("TextField18"));
        picturesOpeningMarkButton = new TextSettingButton(container.getControl("CommandButton18"), "...");
        picturesClosingMarkButton = new TextSettingButton(container.getControl("CommandButton17"), "...");

        /* LABELS */

        picturesFirstLineLabel = new Label(container.getControl("Label125"),
                                           bundle.getString("firstLineLabel") + ":");

        picturesRunoversLabel = new Label(container.getControl("Label51"),
                                          bundle.getString("runoversLabel") + ":");

        picturesLinesAboveBelowLabel = new Label(container.getControl("Label124"),
                                                 bundle.getString("linesAboveLabel") + "/" + bundle.getString("linesBelowLabel") + ":");

        picturesOpeningMarkLabel = new Label(container.getControl("Label127"),
                                             "Opening mark:");

        picturesClosingMarkLabel = new Label(container.getControl("Label126"),
                                             "Closing mark:");

        picturesDescriptionPrefixLabel = new Label(container.getControl("Label122"),
                                                   "Description prefix:");


        /* LINES */

        picturesSpacingLine = new FixedLine(container.getControl("FixedLine5"), "Spacing & indents");

        /* INITIALIZATION */

        picturesFirstLineField.link(settings.getPictureStyle().firstLine);
        picturesRunoversField.link(settings.getPictureStyle().runovers);
        picturesLinesAboveBelowField.link(settings.getPictureStyle().linesAbove);
        picturesOpeningMarkField.link(settings.getPictureStyle().openingMark);
        picturesClosingMarkField.link(settings.getPictureStyle().closingMark);
        picturesDescriptionPrefixField.link(settings.getPictureStyle().descriptionPrefix);
        picturesOpeningMarkButton.link(settings.getPictureStyle().openingMark);
        picturesClosingMarkButton.link(settings.getPictureStyle().closingMark);

        pb.increment();

        /********************/
        /* PAGENUMBERS PAGE */
        /********************/

        /* CONTROLS */

        braillePageNumbersCheckBox = new CheckBox(container.getControl("CheckBox7"));

        braillePageNumberAtListBox = new ListBox<PageNumberPosition>(container.getControl("ListBox21")) {
            @Override
            public String getDisplayValue(PageNumberPosition pos) {
                return L10N_pageNumberPositions.get(pos);
            }
        };

        preliminaryPageNumberFormatListBox = new ListBox<PageNumberFormat>(container.getControl("ListBox20")) {
            @Override
            public String getDisplayValue(PageNumberFormat format) {
                return L10N_pageNumberFormats.get(format);
            }
        };

        beginningBraillePageNumberField = new NumericSettingControl(container.getControl("NumericField1"));
        printPageNumbersCheckBox = new CheckBox(container.getControl("CheckBox6"));

        printPageNumberAtListBox = new ListBox<PageNumberPosition>(container.getControl("ListBox22")) {
            @Override
            public String getDisplayValue(PageNumberPosition pos) {
                return L10N_pageNumberPositions.get(pos);
            }
        };

        printPageNumberRangeCheckBox = new CheckBox(container.getControl("CheckBox11"));
        continuePagesCheckBox = new CheckBox(container.getControl("CheckBox12"));
        pageSeparatorCheckBox = new CheckBox(container.getControl("CheckBox13"));
        pageSeparatorNumberCheckBox = new CheckBox(container.getControl("CheckBox14"));
        ignoreEmptyPagesCheckBox = new CheckBox(container.getControl("CheckBox15"));
        mergeUnnumberedPagesCheckBox = new CheckBox(container.getControl("CheckBox16"));
        numbersAtTopOnSepLineCheckBox = new CheckBox(container.getControl("CheckBox17"));
        numbersAtBottomOnSepLineCheckBox = new CheckBox(container.getControl("CheckBox18"));

        /* LABELS */

        braillePageNumbersLabel = new Label(container.getControl("Label10"),
                                            bundle.getString("braillePageNumbersLabel"));

        preliminaryPageNumberFormatLabel = new Label(container.getControl("Label55"),
                                                     bundle.getString("preliminaryPageNumberFormatLabel") + ":");

        beginningBraillePageNumberLabel = new Label(container.getControl("Label16"),
                                                    bundle.getString("beginningBraillePageNumberLabel") + ":");

        printPageNumbersLabel = new Label(container.getControl("Label9"),
                                          bundle.getString("printPageNumbersLabel"));

        printPageNumberRangeLabel = new Label(container.getControl("Label61"),
                                              bundle.getString("printPageNumberRangeLabel"));

        continuePagesLabel = new Label(container.getControl("Label63"),
                                       bundle.getString("continuePagesLabel"));

        pageSeparatorLabel = new Label(container.getControl("Label64"),
                                       bundle.getString("pageSeparatorLabel"));

        pageSeparatorNumberLabel = new Label(container.getControl("Label65"),
                                             bundle.getString("pageSeparatorNumberLabel"));

        ignoreEmptyPagesLabel = new Label(container.getControl("Label66"),
                                          bundle.getString("ignoreEmptyPagesLabel"));

        mergeUnnumberedPagesLabel = new Label(container.getControl("Label67"),
                                              bundle.getString("mergeUnnumberedPagesLabel"));

        numbersAtTopOnSepLineLabel = new Label(container.getControl("Label68"),
                                               bundle.getString("numbersAtTopOnSepLineLabel"));

        numbersAtBottomOnSepLineLabel = new Label(container.getControl("Label69"),
                                                  bundle.getString("numbersAtBottomOnSepLineLabel"));

        /* INITIALIZATION */

        braillePageNumbersCheckBox.link(settings.braillePageNumbers);
        braillePageNumberAtListBox.link(settings.braillePageNumberPosition);
        preliminaryPageNumberFormatListBox.link(settings.preliminaryPageNumberFormat);
        beginningBraillePageNumberField.link(settings.beginningBraillePageNumber);
        printPageNumbersCheckBox.link(settings.printPageNumbers);
        printPageNumberAtListBox.link(settings.printPageNumberPosition);
        printPageNumberRangeCheckBox.link(settings.printPageNumberRange);
        continuePagesCheckBox.link(settings.continuePages);
        pageSeparatorCheckBox.link(settings.pageSeparator);
        pageSeparatorNumberCheckBox.link(settings.pageSeparatorNumber);
        ignoreEmptyPagesCheckBox.link(settings.ignoreEmptyPages);
        mergeUnnumberedPagesCheckBox.link(settings.mergeUnnumberedPages);
        numbersAtTopOnSepLineCheckBox.link(settings.pageNumberLineAtTop);
        numbersAtBottomOnSepLineCheckBox.link(settings.pageNumberLineAtBottom);

        pb.increment();

        /***************************/
        /* DOCUMENT STRUCTURE PAGE */
        /***************************/

        /* CONTROLS */

        frontmatterCheckBox = new SectionCheckBox(container.getControl("CheckBox44"),
                                                  settings.frontMatterSection) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        frontmatterListBox = new SectionListBox(container.getControl("ListBox16"),
                                                settings.frontMatterSection) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        titlePageCheckBox = new SectionCheckBox(container.getControl("CheckBox47"),
                                                settings.titlePageSection);

        titlePageListBox = new SectionListBox(container.getControl("ListBox17"),
                                              settings.titlePageSection);

        repeatFrontmatterCheckBox = new SectionCheckBox(container.getControl("CheckBox46"),
                                                        settings.repeatFrontMatterSection);

        repeatFrontmatterListBox = new SectionListBox(container.getControl("ListBox23"),
                                                      settings.repeatFrontMatterSection);

        singleVolumeRadioButton = new RadioButton<VolumeManagementMode>(container.getControl("OptionButton1")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        manualVolumesRadioButton = new RadioButton<VolumeManagementMode>(container.getControl("OptionButton2")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        automaticVolumesRadioButton = new RadioButton<VolumeManagementMode>(container.getControl("OptionButton9")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        preferredVolumeSizeField = new NumericSettingControl(container.getControl("NumericField36"));
        maxVolumeSizeField = new NumericSettingControl(container.getControl("NumericField37"));
        minVolumeSizeField = new NumericSettingControl(container.getControl("NumericField38"));
        minLastVolumeSizeField = new NumericSettingControl(container.getControl("NumericField39"));

        rearmatterCheckBox = new SectionCheckBox(container.getControl("CheckBox45"),
                                                 settings.rearMatterSection) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        rearmatterListBox = new SectionListBox(container.getControl("ListBox24"),
                                               settings.rearMatterSection) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        singleRearVolumeRadioButton = new RadioButton<VolumeManagementMode>(container.getControl("OptionButton10")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        manualRearVolumesRadioButton = new RadioButton<VolumeManagementMode>(container.getControl("OptionButton11")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };

        /* LABELS */

        frontmatterLabel = new Label(container.getControl("Label129"),
                                     "Frontmatter section:");

        titlePageLabel = new Label(container.getControl("Label130"),
                                   "Title page:");

        repeatFrontmatterLabel = new Label(container.getControl("Label131"),
                                           "Repeat frontmatter:");

        singleVolumeLabel = new Label(container.getControl("Label110"),
                                      "Single volume");

        manualVolumesLabel = new Label(container.getControl("Label111"),
                                       "Manual volumes");

        automaticVolumesLabel = new Label(container.getControl("Label112"),
                                          "Automatic volumes");

        preferredVolumeSizeLabel = new Label(container.getControl("Label114"),
                                             "Preferred volume size");

        maxVolumeSizeLabel = new Label(container.getControl("Label113"),
                                       "Maximum volume size");

        minVolumeSizeLabel = new Label(container.getControl("Label115"),
                                       "Minimum volume size");

        minLastVolumeSizeLabel = new Label(container.getControl("Label116"),
                                           "Minimum size of last volume");

        rearmatterLabel = new Label(container.getControl("Label132"),
                                    "Rearmatter section:");

        singleRearVolumeLabel = new Label(container.getControl("Label123"),
                                          "Single volume");

        manualRearVolumesLabel = new Label(container.getControl("Label128"),
                                           "Manual volumes");

        /* LINES */

        frontMatterLine = new FixedLine(container.getControl("FixedLine26"),
                                        "Frontmatter");

        bodyMatterLine = new FixedLine(container.getControl("FixedLine28"),
                                       "Bodymatter");

        rearMatterLine = new FixedLine(container.getControl("FixedLine27"),
                                       "Rearmatter");

        /* INITIALIZATION */

        singleVolumeRadioButton.setCondition(VolumeManagementMode.SINGLE);
        manualVolumesRadioButton.setCondition(VolumeManagementMode.MANUAL);
        automaticVolumesRadioButton.setCondition(VolumeManagementMode.AUTOMATIC);
        singleRearVolumeRadioButton.setCondition(VolumeManagementMode.SINGLE);
        manualRearVolumesRadioButton.setCondition(VolumeManagementMode.MANUAL);

        singleVolumeRadioButton.link(settings.bodyMatterMode);
        manualVolumesRadioButton.link(settings.bodyMatterMode);
        automaticVolumesRadioButton.link(settings.bodyMatterMode);
        preferredVolumeSizeField.link(settings.getBodyMatterVolume().preferredVolumeSize);
        maxVolumeSizeField.link(settings.getBodyMatterVolume().maxVolumeSize);
        minVolumeSizeField.link(settings.getBodyMatterVolume().minVolumeSize);
        minLastVolumeSizeField.link(settings.getBodyMatterVolume().minLastVolumeSize);
        singleRearVolumeRadioButton.link(settings.rearMatterMode);
        manualRearVolumesRadioButton.link(settings.rearMatterMode);

        pb.increment();

        /**************************/
        /* VOLUME MANAGEMENT PAGE */
        /**************************/

        /* CONTROLS */

        preliminaryVolumeCheckBox = new CheckBox(container.getControl("CheckBox8")) {
            @Override
            public void itemStateChanged(ItemEvent event) {
                super.itemStateChanged(event);
                volumesListBox.update();
                volumesListBox.updateProperties();
            }
        };
        
        volumesListBox = new VolumeListBox(container.getControl("ListBox32"),
                                           container.getControl("CommandButton20"),
                                           container.getControl("CommandButton19"));

        volumeTitleField = new TextSettingControl(container.getControl("TextField19")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == volumesListBox) {
                    Volume v = volumesListBox.getSelectedItem();
                    link(v != null ? v.title : null);
                }
            }
            @Override
            public void focusLost(FocusEvent event) {
                if (event.Source.equals(window)) {
                    save();
                    volumesListBox.update();
                    volumesListBox.updateProperties();
                }
            }
        };

        volumeSectionListBox = new ListBox<String>(container.getControl("ListBox33")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == volumesListBox) {
                    Volume v = volumesListBox.getSelectedItem();
                    link(v != null && v instanceof SectionVolume ? ((SectionVolume)v).section : null);
                }
            }
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.Source.equals(listbox)) {
                    save();
                    volumesListBox.update();
                    volumesListBox.updateProperties();
                }
            }
        };

        volumeFrontmatterCheckBox = new CheckBox(container.getControl("CheckBox48")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == volumesListBox) {
                    Volume v = volumesListBox.getSelectedItem();
                    link(v != null ? v.frontMatter : null);
                }
            }
        };

        volumeSpecialSymbolsListCheckBox = new CheckBox(container.getControl("CheckBox49")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == volumesListBox) {
                    Volume v = volumesListBox.getSelectedItem();
                    link(v != null ? v.specialSymbolList : null);
                }
            }
        };

        volumeTranscribersNotePageCheckBox = new CheckBox(container.getControl("CheckBox50")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == volumesListBox) {
                    Volume v = volumesListBox.getSelectedItem();
                    link(v != null ? v.transcribersNotesPage : null);
                }
            }
        };

        volumeTableOfContentsCheckBox = new CheckBox(container.getControl("CheckBox51")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == volumesListBox) {
                    Volume v = volumesListBox.getSelectedItem();
                    link(v != null ? v.tableOfContent : null);
                }
            }
        };

        /* LABELS */

        preliminaryVolumeLabel = new Label(container.getControl("Label11"),
                                           bundle.getString("preliminaryVolumeLabel"));

        volumeTitleLabel = new Label(container.getControl("Label135"),
                                     "Title:");

        volumeSectionLabel = new Label(container.getControl("Label134"),
                                       "Section:");

        volumeFrontmatterLabel = new Label(container.getControl("Label136"),
                                           "Include frontmatter");

        volumeListOfSpecialSymbolsLabel = new Label(container.getControl("Label137"),
                                                    "Include list of special symbols");

        volumeTranscribersNotePageLabel = new Label(container.getControl("Label138"),
                                                    "Include transcriber's notes page");

        volumeTableOfContentsLabel = new Label(container.getControl("Label139"),
                                               "Include table of contents");

        /* LINES */

        volumesLine = new FixedLine(container.getControl("FixedLine29"),
                                    "Volumes");

        /* INITIALIZATION */

        preliminaryVolumeCheckBox.link(settings.preliminaryVolumeEnabled);
        Volume v = volumesListBox.getSelectedItem();
        volumeTitleField.link(v != null ? v.title : null);
        volumeSectionListBox.link(v != null && v instanceof SectionVolume ? ((SectionVolume)v).section : null);
        volumeFrontmatterCheckBox.link(v != null ? v.frontMatter : null);
        volumeSpecialSymbolsListCheckBox.link(v != null ? v.specialSymbolList : null);
        volumeTranscribersNotePageCheckBox.link(v != null ? v.transcribersNotesPage : null);
        volumeTableOfContentsCheckBox.link(v != null ? v.tableOfContent : null);

        volumesListBox.addListener(volumeTitleField);
        volumesListBox.addListener(volumeSectionListBox);
        volumesListBox.addListener(volumeFrontmatterCheckBox);
        volumesListBox.addListener(volumeSpecialSymbolsListCheckBox);
        volumesListBox.addListener(volumeTranscribersNotePageCheckBox);
        volumesListBox.addListener(volumeTableOfContentsCheckBox);

        pb.increment();

        /*************************/
        /* TABLE OF CONTENT PAGE */
        /*************************/

        /* CONTROLS */

        tableOfContentsTitleField = new TextSettingControl(container.getControl("TextField4"));

        tableOfContentsLineFillField = new TextPropertyField<Character>(container.getControl("TextField6"));

        tableOfContentsLineFillButton = new CharacterSettingButton(container.getControl("CommandButton6"), "...");

        tableOfContentsPrintPageNumbersCheckBox = new CheckBox(container.getControl("CheckBox25"));

        tableOfContentsBraillePageNumbersCheckBox = new CheckBox(container.getControl("CheckBox26"));

        tableOfContentsUptoLevelField = new NumericSettingControl(container.getControl("NumericField30"));

        tableOfContentsLevelListBox = new MapListBox<Integer,TocStyle.TocLevelStyle>(container.getControl("ListBox18"),
                                                                                     settings.getTocStyle().getLevels()) {
            @Override
            public int compare(Integer level1, Integer level2) {
                return level1.compareTo(level2);
            }
        };

        tableOfContentsFirstLineField = new NumericSettingControl(container.getControl("NumericField23")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableOfContentsLevelListBox) {
                    link(tableOfContentsLevelListBox.getSelectedItem().firstLine);
                }
            }
        };

        tableOfContentsRunoversField = new NumericSettingControl(container.getControl("NumericField24")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == tableOfContentsLevelListBox) {
                    link(tableOfContentsLevelListBox.getSelectedItem().runovers);
                }
            }
        };

        /* LABELS */

        tableOfContentsTitleLabel = new Label(container.getControl("Label58"),
                                              bundle.getString("tableOfContentsTitleLabel") + ":");

        tableOfContentsLineFillLabel = new Label(container.getControl("Label26"),
                                                 bundle.getString("lineFillSymbolLabel") + ":");

        tableOfContentsPrintPageNumbersLabel = new Label(container.getControl("Label17"),
                                                         bundle.getString("tableOfContentsPrintPageNumbersLabel"));

        tableOfContentsBraillePageNumbersLabel = new Label(container.getControl("Label18"),
                                                           bundle.getString("tableOfContentsBraillePageNumbersLabel"));

        tableOfContentsUptoLevelLabel = new Label(container.getControl("Label101"),
                                                  "Evaluate up to level:");

        tableOfContentsLevelLabel = new Label(container.getControl("Label56"),
                                              bundle.getString("levelLabel") + ":");

        tableOfContentsFirstLineLabel = new Label(container.getControl("Label52"),
                                                  bundle.getString("firstLineLabel") + ":");

        tableOfContentsRunoversLabel = new Label(container.getControl("Label53"),
                                                 bundle.getString("runoversLabel") + ":");

        /* LINES */

        tableOfContentsIndentsLine = new FixedLine(container.getControl("FixedLine6"),
                                                   "Indents");

        /* INITIALIZATION */

        tableOfContentsTitleField.link(settings.getTocStyle().title);
        tableOfContentsLineFillField.link(settings.getTocStyle().lineFillSymbol);
        tableOfContentsLineFillButton.link(settings.getTocStyle().lineFillSymbol);
        tableOfContentsPrintPageNumbersCheckBox.link(settings.getTocStyle().printPageNumbers);
        tableOfContentsBraillePageNumbersCheckBox.link(settings.getTocStyle().braillePageNumbers);
        tableOfContentsUptoLevelField.link(settings.getTocStyle().evaluateUptoLevel);
        tableOfContentsFirstLineField.link(tableOfContentsLevelListBox.getSelectedItem().firstLine);
        tableOfContentsRunoversField.link(tableOfContentsLevelListBox.getSelectedItem().runovers);

        tableOfContentsLevelListBox.addListener(tableOfContentsFirstLineField);
        tableOfContentsLevelListBox.addListener(tableOfContentsRunoversField);

        pb.increment();

        /************************/
        /* SPECIAL SYMBOLS PAGE */
        /************************/

        /* CONTROLS */

        specialSymbolsListTitleField = new TextSettingControl(container.getControl("TextField2"));
        
        specialSymbolsListBox = new SpecialSymbolListBox(container.getControl("ListBox25"),
                                                         container.getControl("CommandButton11"),
                                                         container.getControl("CommandButton9"),
                                                         container.getControl("CommandButton10"),
                                                         container.getControl("CommandButton12"));
        
        specialSymbolsSymbolField = new TextPropertyField(container.getControl("TextField8")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == specialSymbolsListBox) {
                    SpecialSymbol s = specialSymbolsListBox.getSelectedItem();
                    link(s != null ? s.symbol : null);
                }
            }
        };

        specialSymbolsSymbolButton = new TextSettingButton(container.getControl("CommandButton8"), "...") {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == specialSymbolsListBox) {
                    SpecialSymbol s = specialSymbolsListBox.getSelectedItem();
                    link(s != null ? s.symbol : null);
                }
            }
            @Override
            public void actionPerformed(ActionEvent event) {
                super.actionPerformed(event);
                specialSymbolsListBox.update();
            }
        };

        specialSymbolsDescriptionField = new TextSettingControl(container.getControl("TextField9")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == specialSymbolsListBox) {
                    SpecialSymbol s = specialSymbolsListBox.getSelectedItem();
                    link(s != null ? s.description : null);
                }
            }
        };

        specialSymbolsMode0RadioButton = new RadioButton<SpecialSymbol.Mode>(container.getControl("OptionButton8")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == specialSymbolsListBox) {
                    SpecialSymbol s = specialSymbolsListBox.getSelectedItem();
                    link(s != null ? s.mode : null);
                }
            }
        };

        specialSymbolsMode1RadioButton = new RadioButton<SpecialSymbol.Mode>(container.getControl("OptionButton7")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == specialSymbolsListBox) {
                    SpecialSymbol s = specialSymbolsListBox.getSelectedItem();
                    link(s != null ? s.mode : null);
                }
            }
        };

        specialSymbolsMode2RadioButton = new RadioButton<SpecialSymbol.Mode>(container.getControl("OptionButton6")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == specialSymbolsListBox) {
                    SpecialSymbol s = specialSymbolsListBox.getSelectedItem();
                    link(s != null ? s.mode : null);
                }
            }
        };

        specialSymbolsMode3RadioButton = new RadioButton<SpecialSymbol.Mode>(container.getControl("OptionButton5")) {
            @Override
            public void dialogElementUpdated(EventObject event) {
                if (event.getSource() == specialSymbolsListBox) {
                    SpecialSymbol s = specialSymbolsListBox.getSelectedItem();
                    link(s != null ? s.mode : null);
                }
            }
        };

        /* LABELS */

        specialSymbolsListTitleLabel = new Label(container.getControl("Label73"),
                                                 bundle.getString("specialSymbolsListTitleLabel") + ":");
        
        specialSymbolsSymbolLabel = new Label(container.getControl("Label75"),
                                              bundle.getString("specialSymbolsSymbolLabel") + ":");
        
        specialSymbolsDescriptionLabel = new Label(container.getControl("Label76"),
                                                   bundle.getString("specialSymbolsDescriptionLabel") + ":");
        
        specialSymbolsMode0Label = new Label(container.getControl("Label81"),
                                             bundle.getString("specialSymbolsMode0Label"));
        
        specialSymbolsMode1Label = new Label(container.getControl("Label80"),
                                             bundle.getString("specialSymbolsMode1Label"));
        
        specialSymbolsMode2Label = new Label(container.getControl("Label79"),
                                             bundle.getString("specialSymbolsMode2Label"));
        
        specialSymbolsMode3Label = new Label(container.getControl("Label78"),
                                             bundle.getString("specialSymbolsMode3Label"));

        /* LINES */

        specialSymbolsLine = new FixedLine(container.getControl("FixedLine7"),
                                           bundle.getString("specialSymbolsLabel"));

        /* INITIALIZATION */

        specialSymbolsMode0RadioButton.setCondition(SpecialSymbol.Mode.NEVER);
        specialSymbolsMode1RadioButton.setCondition(SpecialSymbol.Mode.IF_PRESENT_IN_VOLUME);
        specialSymbolsMode2RadioButton.setCondition(SpecialSymbol.Mode.FIRST_VOLUME);
        specialSymbolsMode3RadioButton.setCondition(SpecialSymbol.Mode.ALWAYS);

        specialSymbolsListTitleField.link(settings.specialSymbolListTitle);
        SpecialSymbol s = specialSymbolsListBox.getSelectedItem();
        specialSymbolsSymbolField.link(s != null ? s.symbol : null);
        specialSymbolsSymbolButton.link(s != null ? s.symbol : null);
        specialSymbolsDescriptionField.link(s != null ? s.description : null);
        specialSymbolsMode0RadioButton.link(s != null ? s.mode : null);
        specialSymbolsMode1RadioButton.link(s != null ? s.mode : null);
        specialSymbolsMode2RadioButton.link(s != null ? s.mode : null);
        specialSymbolsMode3RadioButton.link(s != null ? s.mode : null);

        specialSymbolsListBox.addListener(specialSymbolsSymbolField);
        specialSymbolsListBox.addListener(specialSymbolsSymbolButton);
        specialSymbolsListBox.addListener(specialSymbolsDescriptionField);
        specialSymbolsListBox.addListener(specialSymbolsMode0RadioButton);
        specialSymbolsListBox.addListener(specialSymbolsMode1RadioButton);
        specialSymbolsListBox.addListener(specialSymbolsMode2RadioButton);
        specialSymbolsListBox.addListener(specialSymbolsMode3RadioButton);

        pb.increment();

        /********************/
        /* MATHEMATICS PAGE */
        /********************/

        /* CONTROLS */

        mathListBox = new ListBox<MathCode>(container.getControl("ListBox10")) {
            @Override
            public String getDisplayValue(MathCode value) {
                return L10N_math.get(value);
            }
        };

        /* LABELS */

        mathLabel = new Label(container.getControl("Label35"),
                              bundle.getString("formulasLabel") + ":");

        /* INITIALIZATION */
        
        mathListBox.link(settings.mathCode);

        pb.increment();

        logger.exiting("SettingsDialog", "<init>");
    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    private class Roadmap implements DialogElement,
                                     XItemListener {

        private final XItemEventBroadcaster broadcaster;
        private final XPropertySet propertySet;
        private int page = 1;

        public Roadmap(String title,
                       String[] labels,
                       boolean[] enabled)
                throws com.sun.star.uno.Exception {

            String name = "SettingsRoadmap";
            XMultiServiceFactory xMSF = (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class, control.getModel());
            XNameContainer nameContainer = (XNameContainer)UnoRuntime.queryInterface(XNameContainer.class, control.getModel());
            Object model = xMSF.createInstance("com.sun.star.awt.UnoControlRoadmapModel");
            XMultiPropertySet roadMapMPSet = (XMultiPropertySet) UnoRuntime.queryInterface(XMultiPropertySet.class, model);
            roadMapMPSet.setPropertyValues(new String[]{"Complete",
                                                        "Height",
                                                        "Name",
                                                        "PositionX",
                                                        "PositionY",
                                                        "TabIndex",
                                                        "Text",
                                                        "Width"},
                                           new Object[]{Boolean.FALSE,
                                                        roadMapHeight,
                                                        name,
                                                        0,
                                                        0,
                                                        (short)0,
                                                        title,
                                                        roadMapWidth});
            propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, model);
            nameContainer.insertByName(name, model);
            XSingleServiceFactory xSSFRoadmap = (XSingleServiceFactory)UnoRuntime.queryInterface(XSingleServiceFactory.class, model);
            XIndexContainer roadmapIndexContainer = (XIndexContainer)UnoRuntime.queryInterface(XIndexContainer.class, model);
            XControlContainer container = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
            XControl roadmapControl = container.getControl(name);
            broadcaster = (XItemEventBroadcaster)UnoRuntime.queryInterface(XItemEventBroadcaster.class, roadmapControl);

            Object roadmapItem = null;
            XPropertySet roadMapItemProperties = null;
            for (int i=0; i<labels.length; i++) {
                roadmapItem = xSSFRoadmap.createInstance();
                roadMapItemProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, roadmapItem);
                roadMapItemProperties.setPropertyValue("Label", labels[i]);
                roadMapItemProperties.setPropertyValue("Enabled", enabled[i]);
                roadMapItemProperties.setPropertyValue("ID", new Integer(i+1));
                roadmapIndexContainer.insertByIndex(i, roadmapItem);
            }

            propertySet.setPropertyValue("Complete", true);
            windowProperties.setPropertyValue("Step", 0);
            setPage(GENERAL_PAGE);
            windowProperties.setPropertyValue("Step", getPage());
            broadcaster.addItemListener(this);

        }
        public void itemStateChanged(ItemEvent event) {
            if (event.Source.equals(broadcaster)) {
                int newPage = event.ItemId;
                if (newPage != page) {
                    try {
                        windowProperties.setPropertyValue("Step", new Integer(newPage));
                        page = newPage;
                    } catch (UnknownPropertyException e) {
                    } catch (PropertyVetoException e) {
                    } catch (IllegalArgumentException e) {
                    } catch (WrappedTargetException e) {
                    }
                }
            }
        }
        public void setPage(int value) {
            if (value > 0) {
                try {
                    windowProperties.setPropertyValue("Step", new Integer(value));
                    page = value;
                    propertySet.setPropertyValue("CurrentItemID", (short)value);
                } catch (UnknownPropertyException e) {
                } catch (PropertyVetoException e) {
                } catch (IllegalArgumentException e) {
                } catch (WrappedTargetException e) {
                }
            }
        }
        public int getPage() { return page; }
        public void addListener(XItemListener listener) {
            broadcaster.addItemListener(listener);
        }
        public void disposing(com.sun.star.lang.EventObject object) {}
    }

    private class NavigationButton extends Button
                                implements XItemListener {

        public NavigationButton(XControl control, String label) {
            super(control, label);
        }
        public void itemStateChanged(ItemEvent event) {
            updateProperties();
        }
        public void actionPerformed(ActionEvent event) {}
    }

    private class CharacterSettingButton extends SettingButton<Setting<Character>> {

        public CharacterSettingButton(XControl control, String label) {
            super(control, label);
        }

        public void actionPerformed(ActionEvent event) {
            if (property == null) { return; }
            if (event.Source.equals(button)) {
                try {
                    InsertDialog insertBrailleDialog = new InsertDialog(context);
                    insertBrailleDialog.setBrailleCharacters(String.valueOf(property.get()));
                    if (insertBrailleDialog.execute()) {
                        String s = insertBrailleDialog.getBrailleCharacters();
                        if (s.length()==1) { property.set(s.charAt(0)); }
                    }
                } catch (com.sun.star.uno.Exception e) {
                }
            }
        }
    };

    private class TextSettingButton extends SettingButton<Setting<String>> {

        public TextSettingButton(XControl control, String label) {
            super(control, label);
        }

        public void actionPerformed(ActionEvent event) {
            if (property == null) { return; }
            if (event.Source.equals(button)) {
                try {
                    InsertDialog insertBrailleDialog = new InsertDialog(context);
                    insertBrailleDialog.setBrailleCharacters(property.get());
                    if (insertBrailleDialog.execute()) {
                        property.set(insertBrailleDialog.getBrailleCharacters());
                    }
                } catch (com.sun.star.uno.Exception e) {
                }
            }
        }
    };

    private class FormattingRulesButton implements DialogElement,
                                                   XActionListener {
        private final XListBox listbox;
        private final XButton button;
        private final List<FormattingRules> rulesList = new ArrayList<FormattingRules>();

        public FormattingRulesButton(XControl buttonControl,
                                     XControl listboxControl) {
            listbox = (XListBox)UnoRuntime.queryInterface(XListBox.class, listboxControl);
            button = (XButton)UnoRuntime.queryInterface(XButton.class, buttonControl);
            rulesList.add(new BANAFormattingRules());
            listbox.addItem("BANA", (short)1);
            listbox.selectItemPos((short)0, true);
            button.setLabel("Apply...");
            button.addActionListener(this);
        }
        public void actionPerformed(ActionEvent event) {
            if (event.Source == button) {
                rulesList.get((int)listbox.getSelectedItemPos()).applyTo(settings);
            }
        }
        public void disposing(com.sun.star.lang.EventObject object) {}
    }

    private class EightDotsCheckBox extends SettingControl<OptionSetting<TranslationTable.Dots>>
                                 implements XItemListener {

        private final XCheckBox checkbox;

        public EightDotsCheckBox(XControl control) {
            super(control);
            checkbox = (XCheckBox)UnoRuntime.queryInterface(XCheckBox.class, control);
        }
        public void save() {
            property.set((checkbox.getState()==(short)1) ? TranslationTable.Dots.EIGHTDOTS :
                                                           TranslationTable.Dots.SIXDOTS);
        }
        public void update() {
            checkbox.setState((short)((property.get()==TranslationTable.Dots.EIGHTDOTS) ? 1 : 0));
        }
        @Override
        public void updateProperties() {
            try {
                propertySet.setPropertyValue("Enabled", property!=null ? property.options().size() > 1 : false);
            } catch (UnknownPropertyException e) {
            } catch (PropertyVetoException e) {
            } catch (IllegalArgumentException e) {
            } catch (WrappedTargetException e) {
            }
        }
        public void listenControl(boolean onOff) {
            if (onOff) {
                checkbox.addItemListener(this);
            } else {
                checkbox.removeItemListener(this);
            }
        }
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.Source.equals(checkbox)) { save(); }
        }
    }

    private class AlignmentListBox extends ListBox<Style.Alignment> {
        public AlignmentListBox(XControl control) {
            super(control);
        }
        @Override
        public String getDisplayValue(Style.Alignment value) {
            return L10N_alignment.get(value);
        }
    }

    private class SectionCheckBox extends SettingControl<OptionSetting<String>>
                               implements XItemListener {

        private final XCheckBox checkbox;

        public SectionCheckBox(XControl control, OptionSetting<String> sectionSetting) {
            super(control);
            checkbox = (XCheckBox)UnoRuntime.queryInterface(XCheckBox.class, control);
            link(sectionSetting);
        }

        public void update() {
            checkbox.setState((short)(property.get() == null ? 0 : 1));
        }

        public void save() {
            if (checkbox.getState() == (short)1) {
                try {
                    property.set(property.options().iterator().next());
                } catch (NoSuchElementException e) {
                    update();
                }
            } else {
                property.set(null);
            }
        }

        public void listenControl(boolean onOff) {
            if (onOff) {
                checkbox.addItemListener(this);
            } else {
                checkbox.removeItemListener(this);
            }
        }

        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.Source.equals(checkbox)) { save(); }
        }
    }

    private class SectionListBox extends ListBox<String> {
    
        public SectionListBox(XControl control, OptionSetting<String> sectionSetting) {
            super(control);
            link(sectionSetting);
        }

        @Override
        public int compare(String section1, String section2) { return 0; }

        @Override
        public void update() {
            updateItems();
            super.update();
            updateProperties();
        }
        
        @Override
        public void updateItems() {
            if (property.get() == null) {
                listbox.removeItems((short)0, Short.MAX_VALUE);
            } else {
                super.updateItems();
            }
        }

        @Override
        public void updateProperties() {
            if (property.get() == null) {
                try {
                    propertySet.setPropertyValue("Enabled", false);
                } catch (UnknownPropertyException e) {
                } catch (PropertyVetoException e) {
                } catch (IllegalArgumentException e) {
                } catch (WrappedTargetException e) {
                }
            } else {
                super.updateProperties();
            }
        }
    }

    private class VolumeListBox implements DialogElement,
                                           XItemListener,
                                           XActionListener {

        private final XListBox listbox;
        private final XButton addButton;
        private final XButton removeButton;
        private final XPropertySet addPropertySet;
        private final XPropertySet removePropertySet;

        private final List<Volume> list = new ArrayList<Volume>();
        private Volume selectedVolume;
        private Collection<DialogElementListener> listeners = new ArrayList<DialogElementListener>();

        public VolumeListBox(XControl listboxControl,
                             XControl addButtonControl,
                             XControl removeButtonControl) {

            listbox = (XListBox)UnoRuntime.queryInterface(XListBox.class, listboxControl);
            addButton = (XButton)UnoRuntime.queryInterface(XButton.class, addButtonControl);
            removeButton = (XButton)UnoRuntime.queryInterface(XButton.class, removeButtonControl);
            addPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, addButtonControl.getModel());
            removePropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, removeButtonControl.getModel());
            update();
            updateProperties();
            addButton.addActionListener(this);
            removeButton.addActionListener(this);
        }

        public Volume getSelectedItem() {
            return selectedVolume;
        }

        public void update() {
            list.clear();
            if (settings.getPreliminaryVolumeEnabled()) {
                list.add(settings.getPreliminaryVolume());
            }
            if (settings.getBodyMatterMode() != VolumeManagementMode.MANUAL) {
                list.add(settings.getBodyMatterVolume());
            }
            list.addAll(settings.getSectionVolumeList().values());
            if (settings.getRearMatterSection() != null &&
                settings.getRearMatterMode() != VolumeManagementMode.MANUAL) {
                list.add(settings.getRearMatterVolume());
            }
            if (!list.contains(selectedVolume)) {
                selectedVolume = list.get(0);
            }
            listenControl(false);
            listbox.removeItems((short)0, Short.MAX_VALUE);
            short i = 0;
            for (Volume v : list) {
                listbox.addItem(getDisplayValue(v), i);
                i++;
            }
            listenControl(true);
            listbox.selectItem(getDisplayValue(selectedVolume), true);
        }

        public void updateProperties() {
            try {
                addPropertySet.setPropertyValue("Enabled", settings.getSectionVolumeList().canAdd());
                removePropertySet.setPropertyValue("Enabled", selectedVolume != null ? selectedVolume instanceof SectionVolume : false);
            } catch (UnknownPropertyException e) {
            } catch (PropertyVetoException e) {
            } catch (IllegalArgumentException e) {
            } catch (WrappedTargetException e) {
            }
        }

        protected String getDisplayValue(Volume volume) {
            if (volume instanceof SplittableVolume) {
                if (settings.getBodyMatterMode() == VolumeManagementMode.AUTOMATIC) {
                    return "[AUTO] " + volume.getTitle();
                }
            }
            return volume.getTitle();
        }

        public void actionPerformed(ActionEvent event) {
            if (event.Source.equals(addButton)) {
                SectionVolume volume = settings.getSectionVolumeList().add();
                if (volume != null) {
                    selectedVolume = volume;
                    update();
                    updateProperties();
                }
            } else if (event.Source.equals(removeButton)) {
                int i = list.indexOf(selectedVolume);
                if (i >= 0) {
                    settings.getSectionVolumeList().remove(i);
                    update();
                    updateProperties();
                }
            }
        }

        public void itemStateChanged(ItemEvent event) {
            if (event.Source.equals(listbox)) {
                selectedVolume = list.get(listbox.getSelectedItemPos());
                updateProperties();
                EventObject eo = new EventObject(this);
                for (DialogElementListener listener : listeners) {
                    listener.dialogElementUpdated(eo);
                }
            }
        }

        public void addListener(DialogElementListener listener) {
            listeners.add(listener);
        }

        public void listenControl(boolean onOff) {
            if (onOff) {
                listbox.addItemListener(this);
            } else {
                listbox.removeItemListener(this);
            }
        }

        public void disposing(com.sun.star.lang.EventObject object) {}
    }

    private class SpecialSymbolListBox implements DialogElement,
                                                  XItemListener,
                                                  XActionListener {        
        private final XListBox listbox;
        private final XButton addButton;
        private final XButton removeButton;
        private final XButton moveUpButton;
        private final XButton moveDownButton;
        
        private final XPropertySet addPropertySet;
        private final XPropertySet removePropertySet;
        private final XPropertySet moveUpPropertySet;
        private final XPropertySet moveDownPropertySet;

        private final List<SpecialSymbol> list;
        private SpecialSymbol selectedSymbol;
        private Collection<DialogElementListener> listeners = new ArrayList<DialogElementListener>();

        public SpecialSymbolListBox(XControl listboxControl,
                                    XControl addButtonControl,
                                    XControl removeButtonControl,
                                    XControl moveUpButtonControl,
                                    XControl moveDownButtonControl) {

            listbox = (XListBox)UnoRuntime.queryInterface(XListBox.class, listboxControl);
            addButton = (XButton)UnoRuntime.queryInterface(XButton.class, addButtonControl);
            removeButton = (XButton)UnoRuntime.queryInterface(XButton.class, removeButtonControl);
            moveUpButton = (XButton)UnoRuntime.queryInterface(XButton.class, moveUpButtonControl);
            moveDownButton = (XButton)UnoRuntime.queryInterface(XButton.class, moveDownButtonControl);
            
            addPropertySet = ((XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, addButtonControl.getModel()));
            removePropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, removeButtonControl.getModel());
            moveUpPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, moveUpButtonControl.getModel());
            moveDownPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, moveDownButtonControl.getModel());
            
            list = new ArrayList<SpecialSymbol>();
            update();
            updateProperties();
            addButton.addActionListener(this);
            removeButton.addActionListener(this);
            moveUpButton.addActionListener(this);
            moveDownButton.addActionListener(this);
        }

        public SpecialSymbol getSelectedItem() {
            return selectedSymbol;
        }

        public void update() {
            list.clear();
            list.addAll(settings.getSpecialSymbolList().values());
            if (!list.contains(selectedSymbol)) {
                selectedSymbol = list.get(0);
            }
            listenControl(false);
            listbox.removeItems((short)0, Short.MAX_VALUE);
            short i = 0;
            for (SpecialSymbol s : list) {
                listbox.addItem(getDisplayValue(s), i);
                i++;
            }
            listenControl(true);
            listbox.selectItem(getDisplayValue(selectedSymbol), true);
        }

        public void updateProperties() {
            try {
                addPropertySet.setPropertyValue("Enabled", settings.getSpecialSymbolList().canAdd());
                removePropertySet.setPropertyValue("Enabled", selectedSymbol != null);
                moveUpPropertySet.setPropertyValue("Enabled", selectedSymbol != null ? list.indexOf(selectedSymbol) > 0 : false);
                moveDownPropertySet.setPropertyValue("Enabled", selectedSymbol != null ? list.indexOf(selectedSymbol) < list.size() - 1 : false);
            } catch (UnknownPropertyException e) {
            } catch (PropertyVetoException e) {
            } catch (IllegalArgumentException e) {
            } catch (WrappedTargetException e) {
            }
        }

        protected String getDisplayValue(SpecialSymbol symbol) {
            return symbol.getSymbol();
        }

        public void actionPerformed(ActionEvent event) {
            if (event.Source.equals(addButton)) {
                SpecialSymbol symbol = settings.getSpecialSymbolList().add();
                if (symbol != null) {
                    selectedSymbol = symbol;
                    update();
                    updateProperties();
                }
            } else if (event.Source.equals(removeButton)) {
                int i = list.indexOf(selectedSymbol);
                if (i >= 0) {
                    settings.getSpecialSymbolList().remove(i);
                    update();
                    updateProperties();
                }
            } else if (event.Source.equals(moveUpButton)) {
                int i = list.indexOf(selectedSymbol);
                if (i >= 0) {
                    settings.getSpecialSymbolList().moveUp(i);
                    update();
                    updateProperties();
                }
            } else if (event.Source.equals(moveDownButton)) {
                int i = list.indexOf(selectedSymbol);
                if (i >= 0) {
                    settings.getSpecialSymbolList().moveDown(i);
                    update();
                    updateProperties();
                }
            }
        }

        public void itemStateChanged(ItemEvent event) {
            if (event.Source.equals(listbox)) {
                selectedSymbol = list.get(listbox.getSelectedItemPos());
                updateProperties();
                EventObject eo = new EventObject(this);
                for (DialogElementListener listener : listeners) {
                    listener.dialogElementUpdated(eo);
                }
            }
        }

        public void addListener(DialogElementListener listener) {
            listeners.add(listener);
        }

        public void listenControl(boolean onOff) {
            if (onOff) {
                listbox.addItemListener(this);
            } else {
                listbox.removeItemListener(this);
            }
        }

        public void disposing(com.sun.star.lang.EventObject object) {}
    }

    /******************/
    /* EXECUTE DIALOG */
    /******************/

    public boolean execute() throws com.sun.star.uno.Exception {

        logger.entering("SettingsDialog", "execute");

        short ret = dialog.execute();

        logger.exiting("SettingsDialog", "execute");

        if (ret == ((short) PushButtonType.OK_value)) {
            return true;
        } else {
            return false;
        }
    }

    public void dispose() {

        if (component != null) {
            component.dispose();
        }
    }

    /********************/
    /* STATIC FUNCTIONS */
    /********************/

    public static int getSteps() {
        return 18;
    }
}
