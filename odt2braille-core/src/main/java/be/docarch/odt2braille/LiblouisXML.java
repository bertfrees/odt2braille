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

package be.docarch.odt2braille;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import be.docarch.odt2braille.setup.SettingMap;
import be.docarch.odt2braille.setup.PEFConfiguration;
import be.docarch.odt2braille.setup.Configuration;
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
import org.daisy.braille.table.BrailleConverter;

/**
 *
 * @author freesb
 */
public class LiblouisXML {

    private final static Logger logger = Constants.getLogger();

    private final static String FILE_SEPARATOR = File.separator;
    private final static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTempDirectory();

    private String liblouisxmlExec = null;
    private String liblouisPath = null;

    private File stylesFile = null;
    private File paragraphsFile = null;
    private File bordersFile = null;

    private Configuration settings = null;
    private PEFConfiguration pefSettings = null;

    private List<String> configurationList = new ArrayList<String>();

    BrailleConverter liblouisTable = new LiblouisTable().newBrailleConverter();

    public LiblouisXML(Configuration settings,
                       PEFConfiguration pefSettings,
                       File liblouisLocation)
                throws IOException,
                       InterruptedException,
                       LiblouisXMLInstallationException {

        logger.entering("LiblouisXML", "<init>");

        this.settings = settings;
        this.pefSettings = pefSettings;
        try {
            this.liblouisPath = liblouisLocation.getAbsolutePath();
        } catch (NullPointerException e) {
            throw new LiblouisXMLInstallationException("Could not find liblouis directory");
        }

        stylesFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_cfg_styles.cfg");
        paragraphsFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_sem_paragraphs.sem");
        bordersFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_sem_borders.sem");

        if (IS_WINDOWS) {

            liblouisxmlExec = new File(liblouisPath + FILE_SEPARATOR + "bin" +
                                                      FILE_SEPARATOR + "file2brl.exe").getAbsolutePath();
        } else {

            liblouisxmlExec = new File(liblouisPath + FILE_SEPARATOR + "bin" +
                                                      FILE_SEPARATOR + "file2brl").getAbsolutePath();
        }

        logger.exiting("LiblouisXML", "<init>");

    }

    public void createStylesFiles() throws IOException {

        logger.entering("LiblouisXML", "createStylesFiles");

        File newStylesFile = File.createTempFile(TMP_NAME, ".styles.cfg", TMP_DIR);
        File newParagraphsFile = File.createTempFile(TMP_NAME, ".paragraphs.sem", TMP_DIR);
        File newBordersFile = File.createTempFile(TMP_NAME, ".borders.sem", TMP_DIR);
        String sep = System.getProperty("line.separator");
        String s = null;
        Writer bufferedWriter = null;
        Map<Alignment,String> alignmentMap = new TreeMap();

        bufferedWriter = new BufferedWriter(new FileWriter(newStylesFile));

        alignmentMap.put(Alignment.LEFT,     "leftJustified");
        alignmentMap.put(Alignment.CENTERED, "centered");
        alignmentMap.put(Alignment.RIGHT,    "rightJustified");

        // Create _cfg_styles.cfg file

        bufferedWriter.write("# CUSTOM STYLES FILE" + sep +
                             "# generated by be.docarch.odt2braille.LiblouisXML#createStylesFiles()" + sep + sep);

        // Paragraphs

        for (ParagraphStyle paraStyle : settings.getParagraphStyles().values()) {

            if (!paraStyle.getInherit()) {

                s = "firstLineIndent " + (paraStyle.getFirstLine() - paraStyle.getRunovers()) + sep
                  + "leftMargin "      + (paraStyle.getAlignment() == Alignment.CENTERED
                                             ? paraStyle.getMarginLeftRight()
                                             : paraStyle.getRunovers()) + sep
                  + "rightMargin "     + (paraStyle.getAlignment() == Alignment.CENTERED
                                             ? paraStyle.getMarginLeftRight()
                                             : 0) + sep
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

        for (HeadingStyle headStyle : settings.getHeadingStyles().values()) {

            int level = headStyle.getLevel();

            s = "firstLineIndent " + (headStyle.getFirstLine() - headStyle.getRunovers()) + sep
              + "leftMargin "      + (headStyle.getAlignment() == Alignment.CENTERED
                                         ? headStyle.getMarginLeftRight()
                                         : headStyle.getRunovers()) + sep
              + "rightMargin "     + (headStyle.getAlignment() == Alignment.CENTERED
                                         ? headStyle.getMarginLeftRight()
                                         : 0) + sep
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

        for (ListStyle listStyle : settings.getListStyles().values()) {

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

        FrameStyle frameStyle = settings.getFrameStyle();

        s = "linesBefore " + frameStyle.getLinesAbove() + sep
          + "linesAfter "  + frameStyle.getLinesBelow() + sep;
        bufferedWriter.write("style frame" + sep + s);
        s = "linesBefore " + (frameStyle.getUpperBorderEnabled()?frameStyle.getPaddingAbove():0) + sep
          + "linesAfter "  + (frameStyle.getLowerBorderEnabled()?frameStyle.getPaddingBelow():0) + sep;
        bufferedWriter.write("style pad-frame" + sep + s);

        // Tables

        TableStyle tableStyle = settings.getTableStyles().get("Default");

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

        bufferedWriter.write("style document"    + sep + "braillePageNumberFormat " +
                (settings.getBraillePageNumbers()?"normal":"blank")  + sep);
        bufferedWriter.write("style preliminary" + sep + "braillePageNumberFormat " +
                (settings.getBraillePageNumbers()?settings.getPreliminaryPageNumberFormat().name().toLowerCase():"blank")  + sep);

        // Table of contents

        HeadingStyle contentsHeader = settings.getHeadingStyles().get(1);

        s = "firstLineIndent " + (contentsHeader.getFirstLine() - contentsHeader.getRunovers()) + sep
          + "leftMargin "      + (contentsHeader.getAlignment() == Alignment.CENTERED
                                     ? contentsHeader.getMarginLeftRight()
                                     : contentsHeader.getRunovers()) + sep
          + "rightMargin "     + (contentsHeader.getAlignment() == Alignment.CENTERED
                                     ? contentsHeader.getMarginLeftRight()
                                     : 0) + sep
          + "linesBefore "     + contentsHeader.getLinesAbove() + sep
          + "linesAfter "      + contentsHeader.getLinesBelow() + sep
          + "format "          + alignmentMap.get(contentsHeader.getAlignment()) + sep;
        bufferedWriter.write("style contentsheader" + sep + s);

        TocStyle tocStyle = settings.getTocStyle();
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

        FootnoteStyle footnoteStyle = settings.getFootnoteStyle();

        s = "linesBefore "     + footnoteStyle.getLinesAbove() + sep
          + "linesAfter "      + footnoteStyle.getLinesBelow() + sep
          + "firstLineIndent " + (footnoteStyle.getFirstLine() - footnoteStyle.getRunovers()) + sep
          + "leftMargin "      + (footnoteStyle.getAlignment() == Alignment.CENTERED
                                     ? footnoteStyle.getMarginLeftRight()
                                     : footnoteStyle.getRunovers()) + sep
          + "rightMargin "     + (footnoteStyle.getAlignment() == Alignment.CENTERED
                                     ? footnoteStyle.getMarginLeftRight()
                                     : 0) + sep
          + "format "          + alignmentMap.get(footnoteStyle.getAlignment()) + sep;
        bufferedWriter.write("style footnote" + sep + s);

        // Pictures

        PictureStyle pictureStyle = settings.getPictureStyle();

        s = "linesBefore "     + pictureStyle.getLinesAbove() + sep
          + "linesAfter "      + pictureStyle.getLinesBelow() + sep;
        bufferedWriter.write("style picture" + sep + s);

        s = "firstLineIndent " + (pictureStyle.getFirstLine() - pictureStyle.getRunovers()) + sep
          + "leftMargin "      + pictureStyle.getRunovers() + sep
          + "format "          + alignmentMap.get(Style.Alignment.LEFT) + sep;
        bufferedWriter.write("style picturenote" + sep + s);

        // Borders

        Set<String> borderChars = new HashSet<String>();
        borderChars.add(liblouisTable.toText(String.valueOf(tableStyle.getUpperBorderStyle())));
        borderChars.add(liblouisTable.toText(String.valueOf(tableStyle.getLowerBorderStyle())));
        borderChars.add(liblouisTable.toText(String.valueOf(frameStyle.getUpperBorderStyle())));
        borderChars.add(liblouisTable.toText(String.valueOf(frameStyle.getLowerBorderStyle())));
        for (HeadingStyle headStyle : settings.getHeadingStyles().values()) {
          borderChars.add(liblouisTable.toText(String.valueOf(headStyle.getUpperBorderStyle())));
          borderChars.add(liblouisTable.toText(String.valueOf(headStyle.getLowerBorderStyle())));
        }

        for (String c : borderChars)
          bufferedWriter.write("style boxline" + c + sep + "topBoxline " + c + sep);

        bufferedWriter.close();
        if (stylesFile.exists()) { stylesFile.delete(); }
        newStylesFile.renameTo(stylesFile);

        bufferedWriter = new BufferedWriter(new FileWriter(newParagraphsFile));

        // Create _sem_paragraphs.sem file

        bufferedWriter.write("# PARAGRAPH STYLES FILE" + sep +
                             "# generated by be.docarch.odt2braille.LiblouisXML#createStylesFiles()" + sep + sep);

        String styleName = null;
        String xpath = null;
        for (ParagraphStyle paraStyle : settings.getParagraphStyles().values()) {
            if (!paraStyle.getInherit()) {
                styleName = paraStyle.getID();
                xpath = "//dtb:paragraph[@style='" + styleName + "' and " +
                        "not(ancestor::dtb:th or ancestor::dtb:td or ancestor::dtb:note or ancestor::dtb:li)]";
                bufferedWriter.write("paragraph_" + styleName + " &xpath(" + xpath + "/dtb:p)" + sep);
                bufferedWriter.write("wrap-paragraph_" + styleName + " &xpath(" + xpath + ")" + sep);
            }
        }

        ParagraphStyle paraStyle = settings.getTranscriptionInfoStyle();
        while(paraStyle.getInherit()) { paraStyle = paraStyle.getParentStyle(); }
        xpath = "//dtb:div[@class='transcription-info']";
        bufferedWriter.write("paragraph_" + paraStyle.getID() + " &xpath(" + xpath + "/dtb:p)" + sep);
        bufferedWriter.write("wrap-paragraph_" + paraStyle.getID() + " &xpath(" + xpath + ")" + sep);
        paraStyle = settings.getVolumeInfoStyle();
        while(paraStyle.getInherit()) { paraStyle = paraStyle.getParentStyle(); }
        xpath = "//dtb:div[@class='volume-info']";
        bufferedWriter.write("paragraph_" + paraStyle.getID() + " &xpath(" + xpath + "/dtb:p)" + sep);
        bufferedWriter.write("wrap-paragraph_" + paraStyle.getID() + " &xpath(" + xpath + ")" + sep);

        bufferedWriter.close();
        if (paragraphsFile.exists()) { paragraphsFile.delete(); }
        newParagraphsFile.renameTo(paragraphsFile);

        bufferedWriter = new BufferedWriter(new FileWriter(newBordersFile));

        // Create _sem_borders.sem file

        bufferedWriter.write("# BORDERS STYLES FILE" + sep +
                             "# generated by be.docarch.odt2braille.LiblouisXML#createStylesFiles()" + sep + sep);

        bufferedWriter.write("boxline"
                + liblouisTable.toText(String.valueOf(tableStyle.getUpperBorderStyle()))
                + " &xpath(//dtb:table/dtb:div[@class='border' and @at='top']/dtb:hr) "
                + sep);
        bufferedWriter.write("boxline"
                + liblouisTable.toText(String.valueOf(tableStyle.getLowerBorderStyle()))
                + " &xpath(//dtb:table/dtb:div[@class='border' and @at='bottom']/dtb:hr) "
                + sep);
        bufferedWriter.write("boxline"
                + liblouisTable.toText(String.valueOf(frameStyle.getUpperBorderStyle()))
                + " &xpath(//dtb:div[@class='frame']/dtb:div[@class='border' and @at='top']/dtb:hr) "
                + sep);
        bufferedWriter.write("boxline"
                + liblouisTable.toText(String.valueOf(frameStyle.getLowerBorderStyle()))
                + " &xpath(//dtb:div[@class='frame']/dtb:div[@class='border' and @at='bottom']/dtb:hr) "
                + sep);

        for (HeadingStyle headStyle : settings.getHeadingStyles().values()) {
            bufferedWriter.write("boxline"
                    + liblouisTable.toText(String.valueOf(headStyle.getUpperBorderStyle()))
                    + " &xpath(//dtb:heading[descendant::dtb:h" + headStyle.getLevel() + "]/dtb:div[@class='border' and @at='top']/dtb:hr)" + sep);
            bufferedWriter.write("boxline"
                    + liblouisTable.toText(String.valueOf(headStyle.getLowerBorderStyle()))
                    + " &xpath(//dtb:heading[descendant::dtb:h" + headStyle.getLevel() + "]/dtb:div[@class='border' and @at='bottom']/dtb:hr)" + sep);
        }

        bufferedWriter.close();
        if (bordersFile.exists()) { bordersFile.delete(); }
        newBordersFile.renameTo(bordersFile);

        logger.exiting("LiblouisXML", "createStylesFiles");

    }

    /**
     * Execute the <code>file2brl</code> program.
     * An xml-file is translated to a braille file.
     * Before executing, <code>liblouisxml</code> has to be configured.
     *
     * @see <a href="http://code.google.com/p/liblouisxml/"><code>liblouisxml</code></a>
     */
    public void run() throws IOException,
                             InterruptedException,
                             LiblouisXMLException {

        logger.entering("LiblouisXML","run");

        Process process;

        int i;
        String message = null;
        String line = null;
        String errors = "";
        String tablePath = new File(liblouisPath + FILE_SEPARATOR + "files").getAbsolutePath();

        message = "liblouisxml:  ";
        message += "\nLOUIS_TABLEPATH=" + tablePath + " \\";
        for (String s : configurationList) {
            message += "\n" + s + " \\";
        }

        logger.log(Level.INFO,message);

        ProcessBuilder builder = new ProcessBuilder(configurationList);
        builder.directory(Constants.getTempDirectory());
        builder.environment().put("LOUIS_TABLEPATH", tablePath);
        try {
            process = builder.start();
        } catch (IOException e) {
            if (!IS_WINDOWS) {
                Runtime.getRuntime().exec(new String[] { "chmod", "775", liblouisxmlExec }).waitFor();
                process = builder.start();
            } else {
                throw e;
            }
        }

        boolean success = process.waitFor() == 0;
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

        if (!errors.equals(""))
          logger.log(Level.INFO, "stderr:  " + "\n" + errors);
        
        if (!success)
            throw new LiblouisXMLException("liblouisxml did not terminate correctly");
        
        logger.exiting("LiblouisXML","run");

    }

    /**
     * Configure the <code>file2brl</code> program.
     * @see <a href="http://code.google.com/p/liblouisxml/"><code>liblouisxml</code></a>
     * <ul>
     * <li><code>literaryTextTable</code>, <code>printPages</code>, <code>cellsPerLine</code> and <code>linesPerPage</code>
     * are set according to the braille settings.</li>
     * <li>Depending on <code>extractpprangemode</code>, an additional semantic-action file is added to <code>semanticFiles</code>.</li>
     * <li><code>beginningPageNumber</code> is set to <code>beginPage</code>.</li>
     * </ul>
     *
     * @param   extractpprangemode  <code>true</code> if the braille output will be used to extract the page range of the preliminary section.
     * @param   beginPage           The first braille page number after this table of contents.
     *                              If no table of contents is rendered, <code>beginPage</code> is just the first braille page number.
     */
    public void configure(File inputFile,
                          File outputFile,
                          boolean extractpprangemode,
                          int beginPage)
                   throws TransformerConfigurationException,
                          TransformerException,
                          IOException {

        logger.entering("LiblouisXML","configure");

        configurationList.clear();

        String math = settings.getMathCode().name().toLowerCase();
        TranslationTable t = settings.getTranslationTables().get(settings.mainLocale);
        String translationTable = "_display.dis," + t.getFileName() + ",_misc";
        String configFiles = liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_cfg_main.cfg," + "_cfg_styles.cfg";
        String semanticFiles = "_sem_main.sem," +
                               "_sem_paragraphs.sem," +
                               "_sem_borders.sem," +
                              (extractpprangemode?"_sem_extractpprangemode.sem,":"") +
                              (settings.getTableStyles().get("Default").getStairstepEnabled()?"_sem_stairsteptable.sem,":"") +
                              math + ".sem";
        String mathTable = "_display.dis," + math + ".ctb,_misc";
        String editTables = "_edit_" + math;
        String lineEnd = IS_WINDOWS ? "\\r\\n" : "\\n";

        configurationList.add(liblouisxmlExec);
        configurationList.add("-f");
        configurationList.add(configFiles);

        configurationList.add("-C" + "literaryTextTable="            + translationTable);
        configurationList.add("-C" + "semanticFiles="                + semanticFiles);
        configurationList.add("-C" + "mathtextTable="                + translationTable);
        configurationList.add("-C" + "mathexprTable="                + mathTable);
        configurationList.add("-C" + "editTable="                    + editTables);
        configurationList.add("-C" + "beginningPageNumber="          + beginPage);
        configurationList.add("-C" + "cellsPerLine="                 + Integer.toString(pefSettings.getColumns()));
        configurationList.add("-C" + "linesPerPage="                 + Integer.toString(pefSettings.getDoubleLineSpacing()
                                                                                        ? ((1 + pefSettings.getRows()) / 2)
                                                                                        : pefSettings.getRows()));
        configurationList.add("-C" + "lineEnd="                      + (pefSettings.getDoubleLineSpacing()?(lineEnd+lineEnd):lineEnd));
        configurationList.add("-C" + "minSyllableLength="            + (settings.minSyllableLength.enabled()?Integer.toString(settings.getMinSyllableLength()):0));
        configurationList.add("-C" + "hyphenate="                    + (settings.getHyphenate()?"yes":"no"));
        configurationList.add("-C" + "printPages="                   + (settings.getPrintPageNumbers()?"yes":"no"));
        configurationList.add("-C" + "pageSeparator="                + (settings.getPageSeparator()?"yes":"no"));
        configurationList.add("-C" + "pageSeparatorNumber="          + (settings.getPageSeparatorNumber()?"yes":"no"));
        configurationList.add("-C" + "ignoreEmptyPages="             + (settings.getIgnoreEmptyPages()?"yes":"no"));
        configurationList.add("-C" + "continuePages="                + (settings.getContinuePages()?"yes":"no"));
        configurationList.add("-C" + "mergeUnnumberedPages="         + (settings.getMergeUnnumberedPages()?"yes":"no"));
        configurationList.add("-C" + "pageNumberTopSeparateLine="    + (settings.getPageNumberLineAtTop()?"yes":"no"));
        configurationList.add("-C" + "pageNumberBottomSeparateLine=" + (settings.getPageNumberLineAtBottom()?"yes":"no"));
        configurationList.add("-C" + "printPageNumberRange="         + (settings.getPrintPageNumberRange()?"yes":"no"));
        configurationList.add("-C" + "printPageNumberAt="            + ((settings.getPrintPageNumberPosition() == PageNumberPosition.TOP_RIGHT)?"top":"bottom"));
        configurationList.add("-C" + "braillePageNumberAt="          + ((settings.getBraillePageNumberPosition() == PageNumberPosition.TOP_RIGHT)?"top":"bottom"));
        configurationList.add("-C" + "printPageNumbersInContents="   + (settings.getTocStyle().getPrintPageNumbers()?"yes":"no"));
        configurationList.add("-C" + "braillePageNumbersInContents=" + (settings.getTocStyle().getBraillePageNumbers()?"yes":"no"));
        configurationList.add("-C" + "lineFill="                     + liblouisTable.toText(String.valueOf(settings.getTocStyle().getLineFillSymbol())));

        configurationList.add(inputFile.getAbsolutePath());
        configurationList.add(outputFile.getAbsolutePath());

        logger.exiting("LiblouisXML","configure");

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
