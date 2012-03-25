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

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.setup.SettingMap;
import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;
import be.docarch.odt2braille.setup.Configuration.PageNumberPosition;
import be.docarch.odt2braille.setup.TranslationTable;
import be.docarch.odt2braille.setup.style.ListStyle;
import be.docarch.odt2braille.setup.style.TocStyle;
import be.docarch.odt2braille.setup.style.ParagraphStyle;
import be.docarch.odt2braille.setup.style.HeadingStyle;
import be.docarch.odt2braille.setup.style.TableStyle;
import be.docarch.odt2braille.setup.style.FrameStyle;
import be.docarch.odt2braille.setup.style.PictureStyle;
import be.docarch.odt2braille.setup.style.FootnoteStyle;
import be.docarch.odt2braille.setup.style.Style;
import be.docarch.odt2braille.setup.style.Style.Alignment;
import be.docarch.odt2braille.utils.FileCreator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.daisy.braille.table.BrailleConverter;

/**
 *
 * @author freesb
 */
public class LiblouisXML extends FileConverter implements Parameterized {

    private static final Logger logger = Constants.getLogger();

    private static final String FILE_SEPARATOR = File.separator;
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final boolean IS_MAC_OS = System.getProperty("os.name").toLowerCase().contains("mac os");
    private static final String LIBLOUISXML_EXEC_NAME = "xml2brl";
    private static final String LIBLOUISXML_VERSION_ATLEAST = "2.4.0";

    private static final String liblouisxmlExec;
    private static final String liblouisPath;
    
    private static final BrailleConverter liblouisTable = new LiblouisTable().newBrailleConverter();
    
    private static boolean createStylesFiles = true;

    private static final File stylesFile;
    private static final File paragraphsFile;
    private static final File bordersFile;
    
    private static final Set<String> styleParameters = new HashSet<String>();
    private static final Set<String> otherParameters = new HashSet<String>();
    
    static {
        try {
            liblouisPath = Constants.getLiblouisDirectory().getAbsolutePath();
            stylesFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_cfg_styles.cfg");
            paragraphsFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_sem_paragraphs.sem");
            bordersFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_sem_borders.sem");
            if (IS_WINDOWS) {
                liblouisxmlExec = new File(liblouisPath + FILE_SEPARATOR + "bin" +
                                                          FILE_SEPARATOR + LIBLOUISXML_EXEC_NAME + ".exe").getAbsolutePath();
            } else if (IS_MAC_OS) {
                liblouisxmlExec = new File(liblouisPath + FILE_SEPARATOR + "bin" +
                                                          FILE_SEPARATOR + LIBLOUISXML_EXEC_NAME).getAbsolutePath();
             /* Runtime.getRuntime().exec(new String[] { "chmod",
                                                         "775",
                                                         liblouisxmlExec }); */
            } else {
                liblouisxmlExec = "xml2brl";
                InputStream stdout = null;
                InputStreamReader isr = null;
                Process process = Runtime.getRuntime().exec(new String[]{"xml2brl", "--version"});
                try {
                    if (process.waitFor() == 0) {
                        stdout = process.getInputStream();
                        isr = new InputStreamReader(stdout);
                        BufferedReader br = new BufferedReader(isr);
                        String r;
                        if ((r = br.readLine()) != null) {
                            String version = r.substring(r.lastIndexOf(' ') + 1);
                            if (compareVersions(version, LIBLOUISXML_VERSION_ATLEAST) < 0) {
                                throw new LiblouisXMLInstallationException("liblouisxml " + LIBLOUISXML_VERSION_ATLEAST + " not installed");
                            }
                        }
                    }
                } finally {
                    if (isr != null) {
                        isr.close();
                        stdout.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        styleParameters.add("paragraphStyles");
        styleParameters.add("headingStyles");
        styleParameters.add("listStyles");
        styleParameters.add("frameStyle");
        styleParameters.add("tableStyle");
        styleParameters.add("braillePageNumbers");
        styleParameters.add("preliminaryPageNumberFormat");
        styleParameters.add("contentsHeaderStyle");
        styleParameters.add("tocStyle");
        styleParameters.add("tocHeadingStyle");
        styleParameters.add("footnoteStyle");
        styleParameters.add("pictureStyle");
        styleParameters.add("transcriptionInfoStyle");
        styleParameters.add("volumeInfoStyle");
        
        otherParameters.add("columns");
        otherParameters.add("rows");
        otherParameters.add("preliminaryPageRangeMode");
        otherParameters.add("beginPage");
        otherParameters.add("mathCode");
        otherParameters.add("mainTranslationTable");
        otherParameters.add("stairstepTableEnabled");
        otherParameters.add("hyphenationEnabled");
        otherParameters.add("minSyllableLength");
        otherParameters.add("printPageNumbers");
        otherParameters.add("pageSeparator");
        otherParameters.add("pageSeparatorNumber");
        otherParameters.add("ignoreEmptyPages");
        otherParameters.add("continuePages");
        otherParameters.add("mergeUnnumberedPages");
        otherParameters.add("pageNumberLineAtTop");
        otherParameters.add("pageNumberLineAtBottom");
        otherParameters.add("printPageNumberRange");
        otherParameters.add("printPageNumberPosition");
        otherParameters.add("braillePageNumberPosition");
        otherParameters.add("printPageNumbersInContents");
        otherParameters.add("braillePageNumbersInContents");
        otherParameters.add("lineFillSymbol");
        
    }
    
    private final Map<String,Object> parameters = new HashMap<String,Object>();
    
    public void setParameter(String key, Object value) {
        if (styleParameters.contains(key)) {
            createStylesFiles = true;
            parameters.put(key, value);
        } else if (otherParameters.contains(key)) {
            parameters.put(key, value);
        } else {
            throw new IllegalArgumentException("parameter '" + key + "' not supported");
        }
    }
    
    private Object getParameter(String key) {
        if (!parameters.containsKey(key)) { throw new NullPointerException("parameter '" + key + "' not set"); }
        return parameters.get(key);
    }
    
    public LiblouisXML() {}

    public void cleanUp() {}
    
    private void createStylesFiles() throws IOException {
        
        if (!createStylesFiles) { return; }

        File newStylesFile = FileCreator.createTempFile(".styles.cfg");
        File newParagraphsFile = FileCreator.createTempFile(".paragraphs.sem");
        File newBordersFile = FileCreator.createTempFile(".borders.sem");
        String sep = System.getProperty("line.separator");
        String s = null;
        Writer bufferedWriter = null;
        Map<Alignment,String> alignmentMap = new TreeMap();

        bufferedWriter = new BufferedWriter(new FileWriter(newStylesFile));

        alignmentMap.put(Alignment.LEFT,     "leftJustified");
        alignmentMap.put(Alignment.CENTERED, "centered");
        alignmentMap.put(Alignment.RIGHT,    "rightJustified");

        // Create _cfg_styles.cfg file
        
        logger.log(Level.INFO, "Creating styles file: {0}", stylesFile.getAbsolutePath());

        bufferedWriter.write("# CUSTOM STYLES FILE" + sep +
                             "# generated by be.docarch.odt2braille.LiblouisXML#createStylesFiles()" + sep + sep);

        // Paragraphs

        for (ParagraphStyle paraStyle : (Collection<ParagraphStyle>)getParameter("paragraphStyles")) {
            if (!paraStyle.getInherit()) {
                s = "firstLineIndent " + (paraStyle.getFirstLine() - paraStyle.getRunovers()) + sep
                  + "leftMargin "      + paraStyle.getRunovers() + sep
                  + "centeredMargin "  + paraStyle.getMarginLeftRight() + sep
                  + "linesBefore "     + 0 + sep
                  + "linesAfter "      + 0 + sep
                  + "format "          + alignmentMap.get(paraStyle.getAlignment()) + sep;
                bufferedWriter.write("style paragraph_" + paraStyle.getID() + sep + s);
                s = "linesBefore "     + paraStyle.getLinesAbove() + sep
                  + "linesAfter "      + paraStyle.getLinesBelow() + sep
                  + "keepWithNext "    + (paraStyle.getKeepWithNext()?"yes":"no") + sep
                  + "dontSplit "       + (paraStyle.getDontSplit()?"yes":"no") + sep
               // + "widowControl "    + (paraStyle.getWidowControlEnabled()?paraStyle.getWidowControl():0) + sep
                  + "orphanControl "   + (paraStyle.getOrphanControlEnabled()?paraStyle.getOrphanControl():0) + sep;
                bufferedWriter.write("style wrap-paragraph_" + paraStyle.getID() + sep + s);
            }
        }

        // Headings

        for (HeadingStyle headStyle : (Collection<HeadingStyle>)getParameter("headingStyles")) {
            int level = headStyle.getLevel();
            s = "firstLineIndent " + (headStyle.getFirstLine() - headStyle.getRunovers()) + sep
              + "leftMargin "      + headStyle.getRunovers() + sep
              + "centeredMargin "  + headStyle.getMarginLeftRight() + sep
              + "linesBefore "     + 0 + sep
              + "linesAfter "      + 0 + sep
              + "format "          + alignmentMap.get(headStyle.getAlignment()) + sep;
            bufferedWriter.write("style heading" + level + sep + s);
            bufferedWriter.write("style dummy-heading" + level + sep + s);
            s = "linesBefore "     + headStyle.getLinesAbove() + sep
              + "linesAfter "      + headStyle.getLinesBelow() + sep
              + "keepWithNext "    + (headStyle.getKeepWithNext()?"yes":"no") + sep
              + "dontSplit "       + (headStyle.getDontSplit()?"yes":"no") + sep;
            bufferedWriter.write("style wrap-heading" + level + sep + s);
            s = "linesBefore "     + (headStyle.getUpperBorderEnabled()?headStyle.getPaddingAbove():0) + sep
              + "linesAfter "      + (headStyle.getLowerBorderEnabled()?headStyle.getPaddingBelow():0) + sep;
            bufferedWriter.write("style pad-heading" + level + sep + s);
        }

        // Lists

        for (ListStyle listStyle : (Collection<ListStyle>)getParameter("listStyles")) {
            s = "linesBefore "     + listStyle.getLinesAbove() + sep
              + "linesAfter "      + listStyle.getLinesBelow() + sep
              + "dontSplit "       + (listStyle.getDontSplit()?"yes":"no") + sep;
            bufferedWriter.write("style list" + listStyle.level + sep + s);
            s = "dontSplit "       + (listStyle.getDontSplitItems()?"yes":"no") + sep;
            bufferedWriter.write("style lastli" + listStyle.level + sep + s);
            s += "linesAfter "     + listStyle.getLinesBetween() + sep;
            bufferedWriter.write("style li" + listStyle.level + sep + s);
            s = "leftMargin "      + listStyle.getRunovers() + sep
              + "format "          + alignmentMap.get(Style.Alignment.LEFT) + sep;
            bufferedWriter.write("style listpara" + listStyle.level + sep + s);
            s += "firstLineIndent " + (listStyle.getFirstLine() - listStyle.getRunovers()) + sep;
            bufferedWriter.write("style firstlistpara" + listStyle.level + sep + s);
        }

        // Frames

        FrameStyle frameStyle = (FrameStyle)getParameter("frameStyle");
        s = "linesBefore " + frameStyle.getLinesAbove() + sep
          + "linesAfter "  + frameStyle.getLinesBelow() + sep;
        bufferedWriter.write("style frame" + sep + s);
        s = "linesBefore " + (frameStyle.getUpperBorderEnabled()?frameStyle.getPaddingAbove():0) + sep
          + "linesAfter "  + (frameStyle.getLowerBorderEnabled()?frameStyle.getPaddingBelow():0) + sep;
        bufferedWriter.write("style pad-frame" + sep + s);

        // Tables

        TableStyle tableStyle = (TableStyle)getParameter("tableStyle");
        s = "linesBefore " + tableStyle.getLinesAbove() + sep
          + "linesAfter "  + tableStyle.getLinesAbove() + sep;
        bufferedWriter.write("style table" + sep + s);
        s = "linesBefore " + (tableStyle.getUpperBorderEnabled()?tableStyle.getPaddingAbove():0) + sep
          + "linesAfter "  + (tableStyle.getLowerBorderEnabled()?tableStyle.getPaddingBelow():0) + sep;
        bufferedWriter.write("style pad-table" + sep + s);
        if (tableStyle.getStairstepEnabled()) {
            int firstLine = tableStyle.getFirstLine();
            int runovers = tableStyle.getRunovers();
            int indentPerColumn = tableStyle.getIndentPerColumn();
            for (int i=1;i<=10;i++) {
                s = "firstLineIndent " + (firstLine - runovers) + sep
                  + "leftMargin "      + (runovers + (i-1)*indentPerColumn) + sep
                  + "format "          + alignmentMap.get(Style.Alignment.LEFT) + sep;
                bufferedWriter.write("style tablecolumn" + i + sep + s);
            }
            s = "";
        } else {
            s = "firstLineIndent " + (tableStyle.getFirstLine() - tableStyle.getRunovers()) + sep
              + "leftMargin "      + tableStyle.getRunovers() + sep
              + "format "          + alignmentMap.get(Style.Alignment.LEFT) + sep;
        }
        s += "dontSplit "  + (tableStyle.getDontSplitRows()?"yes":"no") + sep;
        bufferedWriter.write("style lasttablerow" + sep + s);
        s += "linesAfter " + tableStyle.getLinesBetween() + sep;
        bufferedWriter.write("style tablerow" + sep + s);

        // Pagenumbers

        boolean braillePageNumbers = (Boolean)getParameter("braillePageNumbers");
        PageNumberFormat preliminaryPageNumberFormat = (PageNumberFormat)getParameter("preliminaryPageNumberFormat");
        
        bufferedWriter.write("style document"    + sep + "braillePageNumberFormat " +
                (braillePageNumbers?"normal":"blank")  + sep);
        bufferedWriter.write("style preliminary" + sep + "braillePageNumberFormat " +
                (braillePageNumbers?preliminaryPageNumberFormat.name().toLowerCase():"blank")  + sep);

        // Table of contents

        TocStyle tocStyle = (TocStyle)getParameter("tocStyle");
        HeadingStyle contentsHeader = (HeadingStyle)getParameter("tocHeadingStyle");

        s = "firstLineIndent " + (contentsHeader.getFirstLine() - contentsHeader.getRunovers()) + sep
          + "leftMargin "      + contentsHeader.getRunovers() + sep
          + "centeredMargin "  + contentsHeader.getMarginLeftRight() + sep
          + "linesBefore "     + contentsHeader.getLinesAbove() + sep
          + "linesAfter "      + contentsHeader.getLinesBelow() + sep
          + "format "          + alignmentMap.get(contentsHeader.getAlignment()) + sep;
        bufferedWriter.write("style contentsheader" + sep + s);

        SettingMap<Integer,TocStyle.TocLevelStyle> levels = tocStyle.getLevels();
        for (int i=1;i<=10;i++) {
            TocStyle.TocLevelStyle style = levels.get(i);
            if (style!=null) {
                s = "firstLineIndent " + (style.getFirstLine() - style.getRunovers()) + sep
                  + "leftMargin "      + style.getRunovers() + sep
                  + "format "          + "contents" + sep;
                bufferedWriter.write("style contents" + i + sep + s);
            }
        }

        // Footnotes

        FootnoteStyle footnoteStyle = (FootnoteStyle)getParameter("footnoteStyle");

        s = "linesBefore "     + footnoteStyle.getLinesAbove() + sep
          + "linesAfter "      + footnoteStyle.getLinesBelow() + sep
          + "firstLineIndent " + (footnoteStyle.getFirstLine() - footnoteStyle.getRunovers()) + sep
          + "leftMargin "      + footnoteStyle.getRunovers() + sep
          + "centeredMargin "  + footnoteStyle.getMarginLeftRight() + sep
          + "format "          + alignmentMap.get(footnoteStyle.getAlignment()) + sep;
        bufferedWriter.write("style footnote" + sep + s);

        // Pictures

        PictureStyle pictureStyle = (PictureStyle)getParameter("pictureStyle");

        s = "linesBefore "     + pictureStyle.getLinesAbove() + sep
          + "linesAfter "      + pictureStyle.getLinesBelow() + sep;
        bufferedWriter.write("style picture" + sep + s);
        s = "firstLineIndent " + (pictureStyle.getFirstLine() - pictureStyle.getRunovers()) + sep
          + "leftMargin "      + pictureStyle.getRunovers() + sep
          + "format "          + alignmentMap.get(Style.Alignment.LEFT) + sep;
        bufferedWriter.write("style picturenote" + sep + s);
        
        bufferedWriter.close();
        if (stylesFile.exists()) { stylesFile.delete(); }
        newStylesFile.renameTo(stylesFile);

        bufferedWriter = new BufferedWriter(new FileWriter(newParagraphsFile));

        // Create _sem_paragraphs.sem file
        
        logger.log(Level.INFO, "Creating paragraph styles file: {0}", paragraphsFile.getAbsolutePath());

        bufferedWriter.write("# PARAGRAPH STYLES FILE" + sep +
                             "# generated by be.docarch.odt2braille.LiblouisXML#createStylesFiles()" + sep + sep);

        String styleName = null;
        String xpath = null;
        for (ParagraphStyle paraStyle : (Collection<ParagraphStyle>)getParameter("paragraphStyles")) {
            if (!paraStyle.getInherit()) {
                styleName = paraStyle.getID();
                xpath = "//dtb:paragraph[@style='" + styleName + "' and " +
                        "not(ancestor::dtb:th or ancestor::dtb:td or ancestor::dtb:note or ancestor::dtb:li)]";
                bufferedWriter.write("paragraph_" + styleName + " &xpath(" + xpath + "/dtb:p)" + sep);
                bufferedWriter.write("wrap-paragraph_" + styleName + " &xpath(" + xpath + ")" + sep);
            }
        }

        ParagraphStyle paraStyle = (ParagraphStyle)getParameter("transcriptionInfoStyle");
        while(paraStyle.getInherit()) { paraStyle = paraStyle.getParentStyle(); }
        xpath = "//dtb:div[@class='transcription-info']";
        bufferedWriter.write("paragraph_" + paraStyle.getID() + " &xpath(" + xpath + "/dtb:p)" + sep);
        bufferedWriter.write("wrap-paragraph_" + paraStyle.getID() + " &xpath(" + xpath + ")" + sep);
        paraStyle = (ParagraphStyle)getParameter("volumeInfoStyle");
        while(paraStyle.getInherit()) { paraStyle = paraStyle.getParentStyle(); }
        xpath = "//dtb:div[@class='volume-info']";
        bufferedWriter.write("paragraph_" + paraStyle.getID() + " &xpath(" + xpath + "/dtb:p)" + sep);
        bufferedWriter.write("wrap-paragraph_" + paraStyle.getID() + " &xpath(" + xpath + ")" + sep);

        bufferedWriter.close();
        if (paragraphsFile.exists()) { paragraphsFile.delete(); }
        newParagraphsFile.renameTo(paragraphsFile);

        bufferedWriter = new BufferedWriter(new FileWriter(newBordersFile));

        // Create _sem_borders.sem file

        logger.log(Level.INFO, "Creating border styles file: {0}", bordersFile.getAbsolutePath());
        

        bufferedWriter.write("# BORDERS STYLES FILE" + sep +
                             "# generated by be.docarch.odt2braille.LiblouisXML#createStylesFiles()" + sep + sep);

        bufferedWriter.write("boxline &xpath(//dtb:table/dtb:div[@class='border' and @at='top']/dtb:hr) "
                + liblouisTable.toText(String.valueOf(tableStyle.getUpperBorderStyle())) + sep);
        bufferedWriter.write("boxline &xpath(//dtb:table/dtb:div[@class='border' and @at='bottom']/dtb:hr) "
                + liblouisTable.toText(String.valueOf(tableStyle.getLowerBorderStyle())) + sep);
        bufferedWriter.write("boxline &xpath(//dtb:div[@class='frame']/dtb:div[@class='border' and @at='top']/dtb:hr) "
                + liblouisTable.toText(String.valueOf(frameStyle.getUpperBorderStyle())) + sep);
        bufferedWriter.write("boxline &xpath(//dtb:div[@class='frame']/dtb:div[@class='border' and @at='bottom']/dtb:hr) "
                + liblouisTable.toText(String.valueOf(frameStyle.getLowerBorderStyle())) + sep);

        for (HeadingStyle headStyle : (Collection<HeadingStyle>)getParameter("headingStyles")) {
            bufferedWriter.write("boxline &xpath(//dtb:heading[descendant::dtb:h" + headStyle.getLevel()
                    + "]/dtb:div[@class='border' and @at='top']/dtb:hr) "
                    + liblouisTable.toText(String.valueOf(headStyle.getUpperBorderStyle())) + sep);
            bufferedWriter.write("boxline &xpath(//dtb:heading[descendant::dtb:h" + headStyle.getLevel()
                    + "]/dtb:div[@class='border' and @at='bottom']/dtb:hr) "
                    + liblouisTable.toText(String.valueOf(headStyle.getLowerBorderStyle())) + sep);
        }

        bufferedWriter.close();
        if (bordersFile.exists()) { bordersFile.delete(); }
        newBordersFile.renameTo(bordersFile);
        
        createStylesFiles = false;

    }

    /**
     * Execute the <code>xml2brl</code> program.
     * An xml-file is translated to a braille file.
     *
     * @see <a href="http://code.google.com/p/liblouisxml/"><code>liblouisxml</code></a>
     */
    @Override
    public synchronized void convert(File input, File output) throws ConversionException {
        
        try {
            
            createStylesFiles();
            
            logger.info("LiblouisXML starting");
            
            String math = (String)getParameter("mathCode");
            TranslationTable t = (TranslationTable)getParameter("mainTranslationTable");
            String translationTable = "__" + t.getID() + ".ctb";
            String backTranslationTable = "__" + t.getID() + (t.getLocale().equals("nl-BE")?"-back":"") + ".ctb";
            String configFiles = liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_cfg_main.cfg," + "_cfg_styles.cfg";
            String semanticFiles = "_sem_main.sem," +
                                   "_sem_paragraphs.sem," +
                                   "_sem_borders.sem," +
                                  ((Boolean)getParameter("preliminaryPageRangeMode") ? "_sem_extractpprangemode.sem," : "") +
                                  ((Boolean)getParameter("stairstepTableEnabled") ? "_sem_stairsteptable.sem," : "") +
                                  "_sem_" + math + ".sem";
            String mathTable = "__" + math + ".ctb";
            String editTables = "_edit_" + math + ".ctb";

            List<String> configurationList = new ArrayList<String>();
            
            configurationList.add(liblouisxmlExec);
            configurationList.add("-f");
            configurationList.add(configFiles);

            configurationList.add("-C" + "literaryTextTable="            + translationTable);
            configurationList.add("-C" + "interlineBackTable="           + backTranslationTable);
            configurationList.add("-C" + "semanticFiles="                + semanticFiles);
            configurationList.add("-C" + "mathtextTable="                + translationTable);
            configurationList.add("-C" + "mathexprTable="                + mathTable);
            configurationList.add("-C" + "editTable="                    + editTables);
            configurationList.add("-C" + "beginningPageNumber="          + Integer.toString((Integer)getParameter("beginPage")));
            configurationList.add("-C" + "cellsPerLine="                 + Integer.toString((Integer)getParameter("columns")));
            configurationList.add("-C" + "linesPerPage="                 + Integer.toString((Integer)getParameter("rows")));
            if (IS_WINDOWS || IS_MAC_OS) {
                configurationList.add("-C" + "minSyllableLength="        + Integer.toString((Integer)getParameter("minSyllableLength"))); }
            configurationList.add("-C" + "hyphenate="                    + ((Boolean)getParameter("hyphenationEnabled")?"yes":"no"));
            configurationList.add("-C" + "printPages="                   + ((Boolean)getParameter("printPageNumbers")?"yes":"no"));
            configurationList.add("-C" + "pageSeparator="                + ((Boolean)getParameter("pageSeparator")?"yes":"no"));
            configurationList.add("-C" + "pageSeparatorNumber="          + ((Boolean)getParameter("pageSeparatorNumber")?"yes":"no"));
            configurationList.add("-C" + "ignoreEmptyPages="             + ((Boolean)getParameter("ignoreEmptyPages")?"yes":"no"));
            configurationList.add("-C" + "continuePages="                + ((Boolean)getParameter("continuePages")?"yes":"no"));
            configurationList.add("-C" + "mergeUnnumberedPages="         + ((Boolean)getParameter("mergeUnnumberedPages")?"yes":"no"));
            configurationList.add("-C" + "pageNumberTopSeparateLine="    + ((Boolean)getParameter("pageNumberLineAtTop")?"yes":"no"));
            configurationList.add("-C" + "pageNumberBottomSeparateLine=" + ((Boolean)getParameter("pageNumberLineAtBottom")?"yes":"no"));
            configurationList.add("-C" + "printPageNumberRange="         + ((Boolean)getParameter("printPageNumberRange")?"yes":"no"));
            configurationList.add("-C" + "printPageNumberAt="            + (((PageNumberPosition)getParameter("printPageNumberPosition") == PageNumberPosition.TOP_RIGHT)?"top":"bottom"));
            configurationList.add("-C" + "braillePageNumberAt="          + (((PageNumberPosition)getParameter("braillePageNumberPosition") == PageNumberPosition.TOP_RIGHT)?"top":"bottom"));
            configurationList.add("-C" + "printPageNumbersInContents="   + ((Boolean)getParameter("printPageNumbersInContents")?"yes":"no"));
            configurationList.add("-C" + "braillePageNumbersInContents=" + ((Boolean)getParameter("braillePageNumbersInContents")?"yes":"no"));
            configurationList.add("-C" + "lineFill="                     + liblouisTable.toText(String.valueOf((Character)getParameter("lineFillSymbol"))));

            configurationList.add(input.getAbsolutePath());
            configurationList.add(output.getAbsolutePath());
        
            Process process;
            Runtime runtime = Runtime.getRuntime();
            String exec_cmd[] = new String[configurationList.size()];

            configurationList.toArray(exec_cmd);

            int i;
            String message = null;
            String line = null;
            String errors = "";

            message = "liblouisxml:  ";
            for (i=0;i<exec_cmd.length;i++) {
                message += "\n" + exec_cmd[i] + " \\";
            }

            logger.log(Level.INFO,message);

            try {
                process = runtime.exec(exec_cmd);
            } catch (IOException e) {
                if (IS_MAC_OS) {
                    runtime.exec(new String[] { "chmod", "775", liblouisxmlExec }).waitFor();
                    process = runtime.exec(exec_cmd);
                } else {
                    throw e;
                }
            }

            if (process.waitFor() != 0) {
                throw new LiblouisXMLException("liblouisxml did not terminate correctly");
            }
            InputStream stderr = process.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);

            while ((line = br.readLine()) != null) {
                errors += line + "\n";
            }

            if (isr != null) {
                isr.close();
                stderr.close();
            }

            if (!errors.equals("")) {
                throw new LiblouisXMLException("liblouisxml error:  " + "\n" + errors);
            }
            
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    private static String unicodeCodePointNotation(String s) {

        StringBuffer sb = new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            sb.append("\\x" + Integer.toHexString((Character.codePointAt(s, i) & 0xFFFF) | 0x10000).substring(1));
        }
        return sb.toString();
    }

    private static int compareVersions(String v1, String v2) {

        int i1 = v1.indexOf('.');
        int i2 = v2.indexOf('.');

        if (i1<0) { return (i2<0) ? 0 : -1; }
        if (i2<0) { return (i1<0) ? 0 : 1; }

        int p1 = Integer.parseInt(v1.substring(0, i1));
        int p2 = Integer.parseInt(v2.substring(0, i2));

        if (p1 > p2) { return 1; }
        if (p1 < p2) { return -1; }

        return compareVersions(v1.substring(i1 + 1), v2 = v2.substring(i2 + 1));
    }
}
