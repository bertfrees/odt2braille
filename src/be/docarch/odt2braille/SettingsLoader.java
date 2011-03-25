package be.docarch.odt2braille;


/**
 *
 * @author Bert Frees
 */
public class SettingsLoader {

    // Braille settings

    protected static String brailleRulesProperty =                 "[BRL]BrailleRules";
    protected static String languageProperty =                     "[BRL]Language";
    protected static String gradeProperty =                        "[BRL]Grade";
    protected static String dotsProperty =                         "[BRL]Dots";
    protected static String transcriptionInfoEnabledProperty =     "[BRL]TranscriptionInfo";
    protected static String creatorProperty =                      "[BRL]Creator";
    protected static String volumeInfoEnabledProperty =            "[BRL]VolumeInfo";
    protected static String transcriptionInfoStyleProperty =       "[BRL]TranscriptionInfoStyle";
    protected static String volumeInfoStyleProperty =              "[BRL]VolumeInfoStyle";
    protected static String specialSymbolsListEnabledProperty =    "[BRL]ListOfSpecialSymbols";
    protected static String specialSymbolsListTitleProperty =      "[BRL]ListOfSpecialSymbolsTitle";
    protected static String transcribersNotesPageEnabledProperty = "[BRL]TNPage";
    protected static String transcribersNotesPageTitleProperty =   "[BRL]TNPageTitle";
    protected static String tableOfContentEnabledProperty =        "[BRL]TableOfContent";
    protected static String tableOfContentTitleProperty =          "[BRL]TabFleOfContentTitle";
    protected static String tableOfContentLevelProperty =          "[BRL]TableOfContentLevel";
    protected static String preliminaryVolumeEnabledProperty =     "[BRL]PreliminaryVolume";
    protected static String stairstepTableProperty =               "[BRL]StairstepTable";
    protected static String columnDelimiterProperty =              "[BRL]ColumnDelimiter";
    protected static String lineFillSymbolProperty =               "[BRL]LineFillSymbol";
    protected static String mathProperty =                         "[BRL]Math";
    protected static String inheritProperty =                      "[BRL]Inherit";
    protected static String alignmentProperty =                    "[BRL]Alignment";
    protected static String firstLineProperty =                    "[BRL]FirstLineIndent";
    protected static String runoversProperty =                     "[BRL]Runovers";
    protected static String marginLeftRightProperty =              "[BRL]MarginLeftRight";
    protected static String linesAboveProperty =                   "[BRL]LinesAbove";
    protected static String linesBelowProperty =                   "[BRL]LinesBelow";
    protected static String linesBetweenProperty =                 "[BRL]LinesBetween";
    protected static String listPrefixProperty =                   "[BRL]ListPrefix";
    protected static String keepEmptyParagraphsProperty =          "[BRL]KeepEmptyParagraphs";
    protected static String newBraillePage =                       "[BRL]NewBraillePage";
    protected static String dontSplitProperty =                    "[BRL]DontSplit";
    protected static String dontSplitTableRowsProperty =           "[BRL]DontSplitTableRows";
    protected static String dontSplitItemsProperty =               "[BRL]DontSplitItems";
    protected static String keepWithNextProperty =                 "[BRL]KeepWithNext";
    protected static String orphanControlEnabledProperty =         "[BRL]OrphanControlEnabled";
    protected static String widowControlEnabledProperty =          "[BRL]WidowControlEnabled";
    protected static String orphanControlProperty =                "[BRL]OrphanControl";
    protected static String widowControlProperty =                 "[BRL]WidowControl";
    protected static String boldProperty =                         "[BRL]Boldface";
    protected static String italicProperty =                       "[BRL]Italic";
    protected static String underlineProperty =                    "[BRL]Underline";
    protected static String capsProperty =                         "[BRL]Capitals";
    protected static String printPageNumbersProperty =             "[BRL]PrintPageNumbers";
    protected static String braillePageNumbersProperty =           "[BRL]BraillePageNumbers";
    protected static String pageSeparatorProperty =                "[BRL]PageSeparator";
    protected static String pageSeparatorNumberProperty =          "[BRL]PageSeparatorNumber";
    protected static String continuePagesProperty =                "[BRL]ContinuePages";
    protected static String ignoreEmptyPagesProperty =             "[BRL]IgnoreEmptyPages";
    protected static String mergeUnnumberedPagesProperty =         "[BRL]MergeUnnumberedPages";
    protected static String pageNumberAtTopOnSepLineProperty =     "[BRL]PageNumberAtTopOnSepLine";
    protected static String pageNumberAtBottomOnSepLineProperty =  "[BRL]PageNumberAtBottomOnSepLine";
    protected static String printPageNumberRangeProperty =         "[BRL]PrintPageNumberRange";
    protected static String printPageNumberAtProperty =            "[BRL]PrintPageNumberAt";
    protected static String braillePageNumberAtProperty =          "[BRL]BraillePageNumberAt";
    protected static String preliminaryPageNumberFormatProperty =  "[BRL]PreliminaryPageNumberFormat";
    protected static String beginningBraillePageNumberProperty =   "[BRL]BeginningBraillePageNumber";
    protected static String printPageNumbersInTocProperty =        "[BRL]PrintpageNumbersInToc";
    protected static String braillePageNumbersInTocProperty =      "[BRL]BrintpageNumbersInToc";
    protected static String hardPageBreaksProperty =               "[BRL]HardPageBreaks";
    protected static String hyphenateProperty =                    "[BRL]Hyphenation";
    protected static String specialSymbolProperty =                "[BRL]SpecialSymbol";
    protected static String specialSymbolsCountProperty =          "[BRL]SpecialSymbolsCount";
    protected static String noterefPrefixProperty =                "[BRL]NoterefPrefix";
    protected static String upperBorderStyleProperty =             "[BRL]UpperBorderEnabled";
    protected static String lowerBorderStyleProperty =             "[BRL]LowerBorderEnabled";
    protected static String upperBorderProperty =                  "[BRL]UpperBorder";
    protected static String lowerBorderProperty =                  "[BRL]LowerBorder";
    protected static String paddingAboveProperty =                 "[BRL]PaddingAbove";
    protected static String paddingBelowProperty =                 "[BRL]PaddingBelow";

    // Export settings

    protected static String exportFileProperty =                   "[BRL]ExportFileType";
    protected static String exportTableProperty =                  "[BRL]ExportCharacterSet";
    protected static String exportNumberOfCellsPerLineProperty =   "[BRL]ExportCellsPerLine";
    protected static String exportNumberOfLinesPerPageProperty =   "[BRL]ExportLinesPerPage";
    protected static String exportDuplexProperty =                 "[BRL]ExportRectoVerso";
    protected static String exportEightDotsProperty =              "[BRL]ExportEightDots";
    protected static String exportMultipleFilesProperty =          "[BRL]ExportMultipleFiles";

    // Emboss settings

    protected static String embosserProperty =                     "[BRL]Embosser";
    protected static String saddleStitchProperty =                 "[BRL]SaddleStitch";
    protected static String sheetsPerQuireProperty =               "[BRL]SheetsPerQuire";
    protected static String zFoldingProperty =                     "[BRL]ZFolding";
    protected static String paperSizeProperty =                    "[BRL]PaperSize";
    protected static String customPaperWidthProperty =             "[BRL]CustomPaperWidth";
    protected static String customPaperHeightProperty =            "[BRL]CustomPaperHeight";
    protected static String marginLeftProperty =                   "[BRL]MarginLeft";
    protected static String marginTopProperty =                    "[BRL]MarginTop";
    protected static String embossTableProperty =                  "[BRL]EmbossCharacterSet";
    protected static String embossNumberOfCellsPerLineProperty =   "[BRL]EmbossCellsPerLine";
    protected static String embossNumberOfLinesPerPageProperty =   "[BRL]EmbossLinesPerPage";
    protected static String embossDuplexProperty =                 "[BRL]EmbossRectoVerso";
    protected static String embossEightDotsProperty =              "[BRL]EmbossEightDots";

    public void loadVolumeSettings(Settings loadedSettings) {



    }
}
