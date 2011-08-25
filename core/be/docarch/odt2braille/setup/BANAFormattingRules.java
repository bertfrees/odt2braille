package be.docarch.odt2braille.setup;

import be.docarch.odt2braille.setup.Configuration.PageNumberPosition;
import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;
import be.docarch.odt2braille.setup.style.ParagraphStyle;
import be.docarch.odt2braille.setup.style.CharacterStyle;
import be.docarch.odt2braille.setup.style.HeadingStyle;
import be.docarch.odt2braille.setup.style.TableStyle;
import be.docarch.odt2braille.setup.style.ListStyle;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.FrameStyle;
import be.docarch.odt2braille.setup.style.FootnoteStyle;
import be.docarch.odt2braille.setup.style.PictureStyle;
import be.docarch.odt2braille.setup.style.Style.Alignment;

public class BANAFormattingRules implements FormattingRules {

    public String getName() {
        return "EBAE (BANA)";
    }

    public String getDescription() {
        return "English Braille American Edition (Braille Authority of North America)";
    };

    public void applyTo(Configuration configuration) {

        configuration.setPrintPageNumbers(true);
        configuration.setBraillePageNumbers(true);
        configuration.setPageSeparator(true);
        configuration.setPageSeparatorNumber(true);
        configuration.setContinuePages(true);
        configuration.setIgnoreEmptyPages(false);
        configuration.setMergeUnnumberedPages(false);
        configuration.setPageNumberLineAtTop(false);
        configuration.setPageNumberLineAtBottom(false);
        configuration.setPrintPageNumberRange(false);
        configuration.setPrintPageNumberPosition(PageNumberPosition.TOP_RIGHT);
        configuration.setBraillePageNumberPosition(PageNumberPosition.BOTTOM_RIGHT);
        configuration.setPreliminaryPageNumberFormat(PageNumberFormat.P);

        for (ParagraphStyle paraStyle : configuration.getParagraphStyles().values()) {
            paraStyle.setInherit(true);
            if (paraStyle.getID().equals("Standard")) {
                paraStyle.setAlignment(Alignment.LEFT);
                paraStyle.setFirstLine(2);
                paraStyle.setRunovers(0);
                paraStyle.setLinesAbove(0);
                paraStyle.setLinesBelow(0);
            }
        }

        for (CharacterStyle charStyle : configuration.getCharacterStyles().values()) {
            charStyle.setInherit(true);
            if (charStyle.getID().equals("Default")) {
                charStyle.setItalic(CharacterStyle.FollowPrint.FOLLOW_PRINT);
                charStyle.setBoldface(CharacterStyle.FollowPrint.FOLLOW_PRINT);
                charStyle.setUnderline(CharacterStyle.FollowPrint.IGNORE);
                charStyle.setCapitals(CharacterStyle.FollowPrint.FOLLOW_PRINT);
            }
        }

        for (HeadingStyle headStyle : configuration.getHeadingStyles().values()) {
            switch (headStyle.getLevel()) {
                case 1:
                    headStyle.setAlignment(Alignment.CENTERED);
                    headStyle.setLinesAbove(1);
                    headStyle.setLinesBelow(1);
                    break;
                case 2:
                    headStyle.setAlignment(Alignment.CENTERED);
                    headStyle.setLinesAbove(1);
                    headStyle.setLinesBelow(0);
                    break;
                case 3:
                    headStyle.setAlignment(Alignment.LEFT);
                    headStyle.setFirstLine(4);
                    headStyle.setRunovers(4);
                    headStyle.setLinesAbove(1);
                    headStyle.setLinesBelow(0);
                    break;
                case 4: case 5: case 6:
                case 7: case 8: case 9:
                case 10:
                    headStyle.setAlignment(Alignment.LEFT);
                    headStyle.setFirstLine(0);
                    headStyle.setRunovers(0);
                    headStyle.setLinesAbove(1);
                    headStyle.setLinesBelow(0);
                    break;
                default:
                    break;
            }
            headStyle.setUpperBorderEnabled(false);
            headStyle.setLowerBorderEnabled(false);
        }

        for (ListStyle listStyle : configuration.getListStyles().values()) {
            if (listStyle.level == 1) {
                listStyle.setLinesAbove(1);
                listStyle.setLinesBelow(1);
            } else {
                listStyle.setLinesAbove(0);
                listStyle.setLinesBelow(0);
            }
            listStyle.setFirstLine(2*listStyle.level-2);
            listStyle.setRunovers(2*listStyle.level+2);
            listStyle.setLinesBetween(0);
            listStyle.setPrefix("");
        }

        TableStyle tableStyle = configuration.getTableStyles().get("Default");

        tableStyle.setLinesAbove(0);
        tableStyle.setLinesBelow(0);
        tableStyle.setUpperBorderEnabled(true);
        tableStyle.setLowerBorderEnabled(true);
        tableStyle.setUpperBorderStyle('\u2836');
        tableStyle.setLowerBorderStyle('\u281b');
        tableStyle.setPaddingAbove(0);
        tableStyle.setPaddingBelow(0);
        tableStyle.setLinesBetween(0);
        tableStyle.setStairstepEnabled(true);
        tableStyle.setFirstLine(0);
        tableStyle.setRunovers(0);
        tableStyle.setIndentPerColumn(2);

        FrameStyle frameStyle = configuration.getFrameStyle();

        frameStyle.setLinesAbove(0);
        frameStyle.setLinesBelow(0);
        frameStyle.setUpperBorderEnabled(true);
        frameStyle.setLowerBorderEnabled(true);
        frameStyle.setUpperBorderStyle('\u2836');
        frameStyle.setLowerBorderStyle('\u281b');
        frameStyle.setPaddingAbove(0);
        frameStyle.setPaddingBelow(0);

        TocStyle tocStyle = configuration.getTocStyle();
        SettingMap<Integer,TocStyle.TocLevelStyle> levels = tocStyle.getLevels();

        for (int i=1;i<=10;i++) {
            TocStyle.TocLevelStyle tocLevelStyle = levels.get(i);
            if (tocLevelStyle != null) {
                tocLevelStyle.setFirstLine(2*i-2);
                tocLevelStyle.setRunovers(2*i+2);
            }
        }

        tocStyle.setLinesBetween(0);
        tocStyle.setPrintPageNumbers(true);
        tocStyle.setBraillePageNumbers(true);
        tocStyle.setLineFillSymbol('\u2804');

        FootnoteStyle footnoteStyle = configuration.getFootnoteStyle();

        footnoteStyle.setAlignment(Alignment.LEFT);
        footnoteStyle.setLinesAbove(0);
        footnoteStyle.setLinesBelow(0);
        footnoteStyle.setFirstLine(6);
        footnoteStyle.setRunovers(4);

        SettingMap<String,NoteReferenceFormat> noterefFormats = configuration.getNoteReferenceFormats();

        noterefFormats.get("1").setPrefix("\u2814\u2814");
        noterefFormats.get("a").setPrefix("\u2814\u2814\u2830");
        noterefFormats.get("A").setPrefix("\u2814\u2814");
        noterefFormats.get("i").setPrefix("\u2814\u2814\u2830");
        noterefFormats.get("I").setPrefix("\u2814\u2814");

        for (NoteReferenceFormat format : noterefFormats.values()) {
            format.setSpaceAfter(true);
            format.setSpaceAfter(true);
        }

        PictureStyle pictureStyle = configuration.getPictureStyle();
        pictureStyle.setFirstLine(6);
        pictureStyle.setRunovers(4);
        pictureStyle.setLinesAbove(1);
        pictureStyle.setLinesBelow(1);
        pictureStyle.setOpeningMark("\u2820\u2804");
        pictureStyle.setClosingMark("\u2820\u2804");
    }
}
