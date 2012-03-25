/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.
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
package be.docarch.odt2braille.convert;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.PEFConfiguration;
import be.docarch.odt2braille.setup.TranslationTable;
import be.docarch.odt2braille.setup.style.CharacterStyle;
import be.docarch.odt2braille.setup.style.FrameStyle;
import be.docarch.odt2braille.setup.style.HeadingStyle;
import be.docarch.odt2braille.setup.style.ParagraphStyle;
import be.docarch.odt2braille.setup.style.PictureStyle;
import be.docarch.odt2braille.setup.style.TableStyle;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.Style.FollowPrint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Bert Frees
 */
public class ODT2PEFConverterParameters implements Iterable<Map.Entry<String,Object>> {
    
    private Map<String,Object> parameters = new TreeMap<String,Object>();
    
    public ODT2PEFConverterParameters(Configuration configuration,
                                      PEFConfiguration pefConfiguration) {

        // PEFCONFIGURATION
        
        parameters.put("columns", pefConfiguration.getColumns());
        parameters.put("rows", pefConfiguration.getRows());
        parameters.put("duplex", pefConfiguration.getDuplex());
        parameters.put("rowgap", pefConfiguration.getEightDots()?1:0);
        
        // CONFIGURATION

        // Languages

        Map<Locale,TranslationTable> translationTables = new HashMap<Locale,TranslationTable>();
        TranslationTable mainTranslationTable = configuration.getMainTranslationTable();
        for (Locale locale : configuration.getTranslationTables().keys()) {
            translationTables.put(locale, configuration.getTranslationTables().get(locale));
        }

        parameters.put("translationTables", translationTables);
        parameters.put("mainTranslationTable", mainTranslationTable);

        // Hyphenation

        parameters.put("hyphenationEnabled", configuration.getHyphenate());
        parameters.put("minSyllableLength", configuration.getMinSyllableLength());

        // Special typeface

        List<String> configuredCharacterStyles = new ArrayList<String>();
        List<String> keepBoldfaceStyles = new ArrayList<String>();
        List<String> keepItalicStyles = new ArrayList<String>();
        List<String> keepUnderlineStyles = new ArrayList<String>();
        List<String> keepCapsStyles = new ArrayList<String>();
        for (CharacterStyle style : configuration.getCharacterStyles().values()) {
            if (!style.getInherit()) {
                if (style.getParentStyle() != null) { configuredCharacterStyles.add(style.getID()); }
                if (style.getBoldface() == FollowPrint.FOLLOW_PRINT) { keepBoldfaceStyles.add(style.getID()); }
                if (style.getItalic() == FollowPrint.FOLLOW_PRINT) { keepItalicStyles.add(style.getID()); }
                if (style.getUnderline() == FollowPrint.FOLLOW_PRINT) { keepUnderlineStyles.add(style.getID()); }
                if (style.getCapitals() == FollowPrint.FOLLOW_PRINT) { keepCapsStyles.add(style.getID()); }
            }
        }

        parameters.put("configuredCharacterStyles", configuredCharacterStyles.toArray(new String[configuredCharacterStyles.size()]));
        parameters.put("keepBoldfaceStyles", keepBoldfaceStyles.toArray(new String[keepBoldfaceStyles.size()]));
        parameters.put("keepItalicStyles", keepItalicStyles.toArray(new String[keepItalicStyles.size()]));
        parameters.put("keepUnderlineStyles", keepUnderlineStyles.toArray(new String[keepUnderlineStyles.size()]));
        parameters.put("keepCapsStyles", keepCapsStyles.toArray(new String[keepCapsStyles.size()]));

        // Paragraphs

        List<String> configuredParagraphStyles = new ArrayList<String>();
        List<String> keepEmptyParagraphStyles = new ArrayList<String>();
        List<String> keepHardPageBreakStyles = new ArrayList<String>();
        for (ParagraphStyle style : configuration.getParagraphStyles().values()) {
            if (!style.getInherit()) {
                configuredParagraphStyles.add(style.getID());
                if (style.getEmptyParagraphs() == ParagraphStyle.FollowPrint.FOLLOW_PRINT) {
                    keepEmptyParagraphStyles.add(style.getID());
                }
                if (style.getHardPageBreaks() == ParagraphStyle.FollowPrint.FOLLOW_PRINT) {
                    keepHardPageBreakStyles.add(style.getID());
                }
            }
        }

        parameters.put("configuredParagraphStyles", configuredParagraphStyles.toArray(new String[configuredParagraphStyles.size()]));
        parameters.put("keepEmptyParagraphStyles", keepEmptyParagraphStyles.toArray(new String[keepEmptyParagraphStyles.size()]));
        parameters.put("keepHardPageBreakStyles", keepHardPageBreakStyles);
        parameters.put("paragraphStyles", configuration.getParagraphStyles().values());

        // Headings

        Boolean[] newBraillePages = new Boolean[10];
        Boolean[] headingUpperBorder = new Boolean[10];
        Boolean[] headingLowerBorder = new Boolean[10];
        for (int i=1; i<=10; i++) {
            HeadingStyle style = configuration.getHeadingStyles().get(i);
            headingUpperBorder[i-1] = style.getUpperBorderEnabled();
            headingLowerBorder[i-1] = style.getLowerBorderEnabled();
            newBraillePages[i-1] = style.getNewBraillePage();
        }

        parameters.put("headingStyles", configuration.getHeadingStyles().values());
        parameters.put("newBraillePages", newBraillePages);
        parameters.put("headingUpperBorder", headingUpperBorder);
        parameters.put("headingLowerBorder", headingLowerBorder);

        // Lists

        String[] listPrefixes = new String[10];
        for (int i=1; i<=10; i++) {
            listPrefixes[i-1] = configuration.getListStyles().get(i).getPrefix();
        }

        parameters.put("listStyles", configuration.getListStyles().values());
        parameters.put("listPrefixes", listPrefixes);

        // Tables

        TableStyle tableStyle = configuration.getTableStyles().get("Default");

        parameters.put("tableStyle", tableStyle);
        parameters.put("stairstepTableEnabled", tableStyle.getStairstepEnabled());
        parameters.put("tableUpperBorder", tableStyle.getUpperBorderEnabled());
        parameters.put("tableLowerBorder", tableStyle.getLowerBorderEnabled());
        parameters.put("columnDelimiter", tableStyle.getColumnDelimiter());
        parameters.put("columnHeadings", tableStyle.getColumnHeadings());
        parameters.put("rowHeadings", tableStyle.getRowHeadings());
        parameters.put("tableHeadingSuffix", tableStyle.getHeadingSuffix());
        parameters.put("repeatTableHeading", tableStyle.getRepeatHeading());
        parameters.put("mirrorTable", tableStyle.getMirrorTable());

        // Pictures

        PictureStyle pictureStyle = configuration.getPictureStyle();

        parameters.put("pictureStyle", pictureStyle);
        parameters.put("pictureDescriptionPrefix", pictureStyle.getDescriptionPrefix());
        parameters.put("pictureOpeningMarkPrefix", pictureStyle.getOpeningMark());
        parameters.put("pictureClosingMarkPrefix", pictureStyle.getClosingMark());

        // Frames

        FrameStyle frameStyle = configuration.getFrameStyle();

        parameters.put("frameStyle", frameStyle);
        parameters.put("frameUpperBorder", frameStyle.getUpperBorderEnabled());
        parameters.put("frameLowerBorder", frameStyle.getLowerBorderEnabled());

        // Notes

        Map<String,String> noterefNumberPrefixes = new HashMap<String,String>();
        for (String format : configuration.getNoteReferenceFormats().keys()) {
            noterefNumberPrefixes.put(format, configuration.getNoteReferenceFormats().get(format).getPrefix());
        }

        parameters.put("footnoteStyle", configuration.getFootnoteStyle());
        parameters.put("noterefSpaceBefore", configuration.getNoteReferenceFormats().get("1").getSpaceBefore());
        parameters.put("noterefSpaceAfter", configuration.getNoteReferenceFormats().get("1").getSpaceAfter());
        parameters.put("noterefNumberPrefixes", noterefNumberPrefixes);

        // Table of content

        TocStyle tocStyle = configuration.getTocStyle();

        parameters.put("tocStyle", tocStyle);
        parameters.put("tocHeadingStyle", configuration.getHeadingStyles().get(1));
        parameters.put("printPageNumbersInContents", tocStyle.getPrintPageNumbers());
        parameters.put("braillePageNumbersInContents", tocStyle.getBraillePageNumbers());
        parameters.put("lineFillSymbol", tocStyle.getLineFillSymbol());
        parameters.put("tocUptoLevel", tocStyle.getEvaluateUptoLevel());

        // Page numbering

        parameters.put("printPageNumbers", configuration.getPrintPageNumbers());
        parameters.put("braillePageNumbers", configuration.getBraillePageNumbers());
        parameters.put("pageSeparator", configuration.getPageSeparator());
        parameters.put("pageSeparatorNumber", configuration.getPageSeparatorNumber());
        parameters.put("ignoreEmptyPages", configuration.getIgnoreEmptyPages());
        parameters.put("continuePages", configuration.getContinuePages());
        parameters.put("mergeUnnumberedPages", configuration.getMergeUnnumberedPages());
        parameters.put("pageNumberLineAtTop", configuration.getPageNumberLineAtTop());
        parameters.put("pageNumberLineAtBottom", configuration.getPageNumberLineAtBottom());
        parameters.put("printPageNumberRange", configuration.getPrintPageNumberRange());
        parameters.put("printPageNumberPosition", configuration.getPrintPageNumberPosition());
        parameters.put("braillePageNumberPosition", configuration.getBraillePageNumberPosition());
        parameters.put("preliminaryPageNumberFormat", configuration.getPreliminaryPageNumberFormat());
        parameters.put("beginningBraillePageNumber", configuration.getBeginningBraillePageNumber());
        
        // Volume management

        String frontMatter = configuration.getFrontMatterSection();
        String repeatFrontMatter = configuration.getRepeatFrontMatterSection();
        String titlePage = configuration.getTitlePageSection();
        String rearMatter = configuration.getRearMatterSection();

        parameters.put("preliminaryVolumeEnabled", configuration.getPreliminaryVolumeEnabled());
        parameters.put("bodyMatterMode", configuration.getBodyMatterMode().name());
        parameters.put("rearMatterMode", configuration.getRearMatterMode().name());
        parameters.put("frontMatterSection", (frontMatter==null)?"":frontMatter);
        parameters.put("repeatFrontMatterSection", (repeatFrontMatter==null)?"":repeatFrontMatter);
        parameters.put("titlePageSection", (titlePage==null)?"":titlePage);
        parameters.put("rearMatterSection", (rearMatter==null)?"":rearMatter);
        parameters.put("preliminaryVolume", configuration.getPreliminaryVolume());
        parameters.put("bodyMatterVolume", configuration.getBodyMatterVolume());
        parameters.put("rearMatterVolume", configuration.getRearMatterVolume());
        parameters.put("manualVolumes", configuration.getSectionVolumeList().values());

        // Math

        parameters.put("mathCode", configuration.getMathCode().name().toLowerCase());

        // Misc

        parameters.put("transcriptionInfoStyle", configuration.getTranscriptionInfoStyle());
        parameters.put("volumeInfoStyle", configuration.getVolumeInfoStyle());
        parameters.put("creator", configuration.getCreator());
        parameters.put("noteSectionTitle", configuration.getEndNotesPageTitle());
        parameters.put("volumeInfoEnabled", configuration.getVolumeInfoEnabled());
        parameters.put("transcriptionInfoEnabled", configuration.getTranscriptionInfoEnabled());
        parameters.put("tableOfContentTitle", configuration.getTocStyle().getTitle());
        parameters.put("tnPageTitle", configuration.getTranscribersNotesPageTitle());
        parameters.put("specialSymbolListTitle", configuration.getSpecialSymbolListTitle());
        parameters.put("specialSymbolList", configuration.getSpecialSymbolList().values());
    
    }

    public Iterator<Map.Entry<String, Object>> iterator() {
        return parameters.entrySet().iterator();
    }
}
