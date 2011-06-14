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

package be.docarch.odt2braille;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import be.docarch.odt2braille.Settings.PageNumberPosition;
import be.docarch.odt2braille.Style.Alignment;
import org.daisy.braille.table.BrailleConverter;

/**
 *
 * @author freesb
 */
public class LiblouisXML {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private final static String FILE_SEPARATOR = System.getProperty("file.separator");
    private final static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private final static boolean IS_MAC_OS = System.getProperty("os.name").toLowerCase().contains("mac os");
    private static final String LIBLOUISXML_EXEC_NAME = "xml2brl";
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();
    private static final String LIBLOUISXML_VERSION_ATLEAST = "2.4.0";

    private String liblouisxmlExec = null;
    private String liblouisPath = null;

    private File stylesFile = null;
    private File paragraphsFile = null;
    private File bordersFile = null;

    private Settings settings = null;

    private List configurationList = new ArrayList();

    BrailleConverter liblouisTable = new LiblouisTable().newBrailleConverter();

    public LiblouisXML (Settings settings,
                        File liblouisLocation)
                 throws IOException,
                        InterruptedException,
                        LiblouisXMLException {

        logger.entering("LiblouisXML", "<init>");

        this.settings = settings;
        try {
            this.liblouisPath = liblouisLocation.getAbsolutePath();
        } catch (NullPointerException e) {
            throw new LiblouisXMLException("Could not find liblouis directory");
        }

        stylesFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_cfg_styles.cfg");
        paragraphsFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_sem_paragraphs.sem");
        bordersFile = new File(liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_sem_borders.sem");

        if (IS_WINDOWS) {

            liblouisxmlExec = new File(liblouisPath + FILE_SEPARATOR + "bin" +
                                                      FILE_SEPARATOR + "win" +
                                                      FILE_SEPARATOR + LIBLOUISXML_EXEC_NAME + ".exe").getAbsolutePath();
        } else if (IS_MAC_OS) {

            liblouisxmlExec = new File(liblouisPath + FILE_SEPARATOR + "bin" +
                                                      FILE_SEPARATOR + "mac" +
                                                      FILE_SEPARATOR + LIBLOUISXML_EXEC_NAME).getAbsolutePath();

         /* Runtime.getRuntime().exec(new String[] { "chmod",
                                                     "775",
                                                     liblouisxmlExec }); */

        } else {

            Runtime runtime = Runtime.getRuntime();
            Process process = null;
            InputStream stdout = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            process = runtime.exec(new String[]{"xml2brl", "--version"});
            if (process.waitFor() == 0) {
                stdout = process.getInputStream();
                isr = new InputStreamReader(stdout);
                br = new BufferedReader(isr);
                String r;
                if ((r = br.readLine()) != null) {
                    try {
                        int i,j,k,l;
                        String version = r.substring(r.lastIndexOf(' ') + 1);
                        String versionAtLeast = LIBLOUISXML_VERSION_ATLEAST;
                        while (true) {
                            i = version.indexOf('.');
                            j = versionAtLeast.indexOf('.');
                            if (i<0 || j<0) {
                                break;
                            } else {
                                k = Integer.parseInt(version.substring(0, i));
                                l = Integer.parseInt(versionAtLeast.substring(0, j));
                                if (k < l) {
                                    break;
                                } else if (k > l) {
                                    liblouisxmlExec = "xml2brl";
                                    break;
                                }
                            }
                            version = version.substring(i + 1);
                            versionAtLeast = versionAtLeast.substring(j + 1);
                        }
                    } catch (Exception e) {}
                }
            }
            if (isr != null) {
                isr.close();
                stdout.close();
            }
            if (liblouisxmlExec == null) {
                throw new LiblouisXMLException("liblouisxml " + LIBLOUISXML_VERSION_ATLEAST + " not installed");
            }
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
        Style style = null;
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

        for (ParagraphStyle paraStyle : settings.getParagraphStyles()) {

            s = "firstLineIndent " + (paraStyle.getFirstLine() - paraStyle.getRunovers()) + sep
              + "leftMargin "      + paraStyle.getRunovers() + sep
              + "centeredMargin "  + paraStyle.getMarginLeftRight() + sep
              + "linesBefore "     + 0 + sep
              + "linesAfter "      + 0 + sep
              + "format "          + alignmentMap.get(paraStyle.getAlignment()) + sep;
            bufferedWriter.write("style paragraph_" + paraStyle.getName() + sep + s);
            s = "linesBefore "     + paraStyle.getLinesAbove() + sep
              + "linesAfter "      + paraStyle.getLinesBelow() + sep
              + "keepWithNext "    + (paraStyle.getKeepWithNext()?"yes":"no") + sep
              + "dontSplit "       + (paraStyle.getDontSplit()?"yes":"no") + sep
              // + "widowControl "    + (paraStyle.widowControlEnabled?paraStyle.getWidowControl():0) + sep
              + "orphanControl "   + (paraStyle.orphanControlEnabled?paraStyle.getOrphanControl():0) + sep;
            bufferedWriter.write("style wrap-paragraph_" + paraStyle.getName() + sep + s);

        }

        // Headings

        for (HeadingStyle headStyle : settings.getHeadingStyles()) {

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
            s = "linesBefore "     + (headStyle.getUpperBorder()?headStyle.getPaddingAbove():0) + sep
              + "linesAfter "      + (headStyle.getLowerBorder()?headStyle.getPaddingBelow():0) + sep;
            bufferedWriter.write("style pad-heading" + level + sep + s);

        }

        // Lists

        for (ListStyle listStyle : settings.getListStyles()) {

            int level = listStyle.getLevel();

            s = "linesBefore "     + listStyle.getLinesAbove() + sep
              + "linesAfter "      + listStyle.getLinesBelow() + sep
              + "dontSplit "       + (listStyle.getDontSplit()?"yes":"no") + sep;
            bufferedWriter.write("style list" + level + sep + s);
            s = "dontSplit "       + (listStyle.getDontSplitItems()?"yes":"no") + sep;
            bufferedWriter.write("style lastli" + level + sep + s);
            s += "linesAfter "     + listStyle.getLinesBetween() + sep;
            bufferedWriter.write("style li" + level + sep + s);
            s = "leftMargin "      + listStyle.getRunovers() + sep
              + "centeredMargin "  + listStyle.getMarginLeftRight() + sep
              + "format "          + alignmentMap.get(listStyle.getAlignment()) + sep;
            bufferedWriter.write("style listpara" + level + sep + s);
            s += "firstLineIndent " + (listStyle.getFirstLine() - listStyle.getRunovers()) + sep;
            bufferedWriter.write("style firstlistpara" + level + sep + s);

        }

        // Frames

        FrameStyle frameStyle = settings.getFrameStyle();

        s = "linesBefore " + frameStyle.getLinesAbove() + sep
          + "linesAfter "  + frameStyle.getLinesBelow() + sep;
        bufferedWriter.write("style frame" + sep + s);
        s = "linesBefore " + (frameStyle.getUpperBorder()?frameStyle.getPaddingAbove():0) + sep
          + "linesAfter "  + (frameStyle.getLowerBorder()?frameStyle.getPaddingBelow():0) + sep;
        bufferedWriter.write("style pad-frame" + sep + s);

        // Tables

        TableStyle tableStyle = settings.getTableStyle();

        s = "linesBefore " + tableStyle.getLinesAbove() + sep
          + "linesAfter "  + tableStyle.getLinesBelow() + sep;
        bufferedWriter.write("style table" + sep + s);
        s = "linesBefore " + (tableStyle.getUpperBorder()?tableStyle.getPaddingAbove():0) + sep
          + "linesAfter "  + (tableStyle.getLowerBorder()?tableStyle.getPaddingBelow():0) + sep;
        bufferedWriter.write("style pad-table" + sep + s);

        if (settings.stairstepTableIsEnabled()) {

            for (int i=1;i<=10;i++) {

                style = tableStyle.getColumn(i);
                if (style!= null) {

                    s = "firstLineIndent " + (style.getFirstLine() - style.getRunovers()) + sep
                      + "leftMargin "      + style.getRunovers() + sep
                      + "centeredMargin "  + style.getMarginLeftRight() + sep
                      + "format "          + alignmentMap.get(style.getAlignment()) + sep;
                    bufferedWriter.write("style tablecolumn" + i + sep + s);
                }
            }

            s = "";

        } else {

            s = "firstLineIndent " + (tableStyle.getFirstLine() - tableStyle.getRunovers()) + sep
              + "leftMargin "      + tableStyle.getRunovers() + sep
              + "centeredMargin "  + tableStyle.getMarginLeftRight() + sep
              + "format "          + alignmentMap.get(tableStyle.getAlignment()) + sep;
        }

        s += "dontSplit "  + (tableStyle.getDontSplitRows()?"yes":"no") + sep;
        bufferedWriter.write("style lasttablerow" + sep + s);
        s += "linesAfter " + tableStyle.getLinesBetween() + sep;
        bufferedWriter.write("style tablerow" + sep + s);

        // Pagenumbers

        bufferedWriter.write("style document"    + sep + "braillePageNumberFormat " +
                (settings.getBraillePageNumbers()?"normal":"blank")  + sep);
        bufferedWriter.write("style preliminary" + sep + "braillePageNumberFormat " +
                (settings.getBraillePageNumbers()?settings.getPreliminaryPageFormat().name().toLowerCase():"blank")  + sep);

        // Table of contents

        HeadingStyle contentsHeader = settings.getHeadingStyles().get(0);

        s = "firstLineIndent " + (contentsHeader.getFirstLine() - contentsHeader.getRunovers()) + sep
          + "leftMargin "      + contentsHeader.getRunovers() + sep
          + "centeredMargin "  + contentsHeader.getMarginLeftRight() + sep
          + "linesBefore "     + contentsHeader.getLinesAbove() + sep
          + "linesAfter "      + contentsHeader.getLinesBelow() + sep
          + "format "          + alignmentMap.get(contentsHeader.getAlignment()) + sep;
        bufferedWriter.write("style contentsheader" + sep + s);

        TocStyle tocStyle = settings.getTocStyle();

        for (int i=1;i<=10;i++) {

            style = tocStyle.getLevel(i);

            s = "firstLineIndent " + (style.getFirstLine() - style.getRunovers()) + sep
              + "leftMargin "      + style.getRunovers() + sep
              + "format "          + "contents" + sep;
            bufferedWriter.write("style contents" + i + sep + s);
        }

        // Footnotes

        Style footnoteStyle = settings.getFootnoteStyle();

        s = "linesBefore "     + footnoteStyle.getLinesAbove() + sep
          + "linesAfter "      + footnoteStyle.getLinesBelow() + sep
          + "firstLineIndent " + (footnoteStyle.getFirstLine() - footnoteStyle.getRunovers()) + sep
          + "leftMargin "      + footnoteStyle.getRunovers() + sep
          + "centeredMargin "  + footnoteStyle.getMarginLeftRight() + sep
          + "format "          + alignmentMap.get(footnoteStyle.getAlignment()) + sep;
        bufferedWriter.write("style footnote" + sep + s);

        bufferedWriter.close();
        if (stylesFile.exists()) { stylesFile.delete(); }
        newStylesFile.renameTo(stylesFile);

        bufferedWriter = new BufferedWriter(new FileWriter(newParagraphsFile));

        // Create _sem_paragraphs.cfg file

        bufferedWriter.write("# PARAGRAPH STYLES FILE" + sep +
                             "# generated by be.docarch.odt2braille.LiblouisXML#createStylesFiles()" + sep + sep);

        String styleName = null;
        String xpath = null;
        for (ParagraphStyle paraStyle : settings.getParagraphStyles()) {
            styleName = paraStyle.getName();
            xpath = "//dtb:paragraph[@style='" + styleName + "' and " +
                    "not(ancestor::dtb:th or ancestor::dtb:td or ancestor::dtb:note or ancestor::dtb:li)]";
            bufferedWriter.write("paragraph_" + styleName + " &xpath(" + xpath + "/dtb:p)" + sep);
            bufferedWriter.write("wrap-paragraph_" + styleName + " &xpath(" + xpath + ")" + sep);
        }

        styleName = settings.getTranscriptionInfoStyle().getName();
        xpath = "//dtb:div[@class='transcription-info']";
        bufferedWriter.write("paragraph_" + styleName + " &xpath(" + xpath + "/dtb:p)" + sep);
        bufferedWriter.write("wrap-paragraph_" + styleName + " &xpath(" + xpath + ")" + sep);
        styleName = settings.getVolumeInfoStyle().getName();
        xpath = "//dtb:div[@class='volume-info']";
        bufferedWriter.write("paragraph_" + styleName + " &xpath(" + xpath + "/dtb:p)" + sep);
        bufferedWriter.write("wrap-paragraph_" + styleName + " &xpath(" + xpath + ")" + sep);

        bufferedWriter.close();
        if (paragraphsFile.exists()) { paragraphsFile.delete(); }
        newParagraphsFile.renameTo(paragraphsFile);

        bufferedWriter = new BufferedWriter(new FileWriter(newBordersFile));

        // Create _sem_borders.sem file

        bufferedWriter.write("# BORDERS STYLES FILE" + sep +
                             "# generated by be.docarch.odt2braille.LiblouisXML#createStylesFiles()" + sep + sep);

        bufferedWriter.write("boxline &xpath(//dtb:table/dtb:hr[following-sibling::*[1][self::dtb:div[@class='border']]]) "
                + liblouisTable.toText(String.valueOf(tableStyle.getUpperBorderStyle())) + sep);
        bufferedWriter.write("boxline &xpath(//dtb:table/dtb:hr[preceding-sibling::*[1][self::dtb:div[@class='border']]]) "
                + liblouisTable.toText(String.valueOf(tableStyle.getLowerBorderStyle())) + sep);
        bufferedWriter.write("boxline &xpath(//dtb:div[@class='frame']/dtb:hr[following-sibling::*[1][self::dtb:div[@class='border']]]) "
                + liblouisTable.toText(String.valueOf(frameStyle.getUpperBorderStyle())) + sep);
        bufferedWriter.write("boxline &xpath(//dtb:div[@class='frame']/dtb:hr[preceding-sibling::*[1][self::dtb:div[@class='border']]]) "
                + liblouisTable.toText(String.valueOf(frameStyle.getLowerBorderStyle())) + sep);

        for (HeadingStyle headStyle : settings.getHeadingStyles()) {
            bufferedWriter.write("boxline &xpath(//dtb:heading/dtb:hr[following-sibling::*[1][self::dtb:div[@class='border'][child::dtb:h"
                    + headStyle.getLevel() + "]]]) " + liblouisTable.toText(String.valueOf(headStyle.getUpperBorderStyle())) + sep);
            bufferedWriter.write("boxline &xpath(//dtb:heading/dtb:hr[preceding-sibling::*[1][self::dtb:div[@class='border'][child::dtb:h"
                    + headStyle.getLevel() + "]]]) " + liblouisTable.toText(String.valueOf(headStyle.getLowerBorderStyle())) + sep);
        }

        bufferedWriter.close();
        if (bordersFile.exists()) { bordersFile.delete(); }
        newBordersFile.renameTo(bordersFile);

        logger.exiting("LiblouisXML", "createStylesFiles");

    }

    /**
     * Execute the <code>xml2brl</code> program.
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
        Runtime runtime = Runtime.getRuntime();
        String exec_cmd[] = new String[configurationList.size()];

        configurationList.toArray(exec_cmd);

        int i;
        String message = null;
        String line = null;
        String errors = "";

        message = "liblouisxml:  ";
        for (i=0;i<exec_cmd.length;i++) {
            message += "\n               " + exec_cmd[i];
        }

        logger.log(Level.INFO,message);

        process = runtime.exec(exec_cmd);

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

        logger.exiting("LiblouisXML","run");

        if (!errors.equals("")) {
            throw new LiblouisXMLException("liblouisxml error:  " + "\n" + errors);
        }
        
    }

    /**
     * Configure the <code>xml2brl</code> program.
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

        String math = settings.getMath().name().toLowerCase();
        String table = settings.getTranslationTable(settings.getMainLanguage());
        int grade = settings.getGrade(settings.getMainLanguage());
        boolean eightDots = (settings.getDots(settings.getMainLanguage())==8);

        String translationTable = "__" + table + "-g" + grade + (eightDots?"-8d":"") + ".ctb";
        String backTranslationTable = "__" + table + "-g" + grade + (eightDots?"-8d":"") + (table.equals("nl-BE")?"-back":"") + ".ctb";
        String configFiles = liblouisPath + FILE_SEPARATOR + "files" + FILE_SEPARATOR + "_cfg_main.cfg," + "_cfg_styles.cfg";
        String semanticFiles = "_sem_main.sem," +
                               "_sem_paragraphs.sem," +
                               "_sem_borders.sem," +
                              (extractpprangemode?"_sem_extractpprangemode.sem,":"") +
                              (settings.stairstepTableIsEnabled()?"_sem_stairsteptable.sem,":"") +
                              "_sem_" + math + ".sem";
        String mathTable = "__" + math + ".ctb";
        String editTables = "_edit_" + math + ".ctb";

        configurationList.add(liblouisxmlExec);
        configurationList.add("-f");
        configurationList.add(configFiles);

        configurationList.add("-C" + "literaryTextTable="            + translationTable);
        configurationList.add("-C" + "interlineBackTable="           + backTranslationTable);
        configurationList.add("-C" + "semanticFiles="                + semanticFiles);
        configurationList.add("-C" + "mathtextTable="                + translationTable);
        configurationList.add("-C" + "mathexprTable="                + mathTable);
        configurationList.add("-C" + "editTable="                    + editTables);
        configurationList.add("-C" + "beginningPageNumber="          + beginPage);
        configurationList.add("-C" + "cellsPerLine="                 + Integer.toString(settings.getCellsPerLine()));
        configurationList.add("-C" + "linesPerPage="                 + Integer.toString(settings.getLinesPerPage()));
        if (IS_WINDOWS)
            configurationList.add("-C" + "minSyllableLength="        + Integer.toString(settings.getMinSyllableLength()));
        configurationList.add("-C" + "hyphenate="                    + (settings.getHyphenate()?"yes":"no"));
        configurationList.add("-C" + "printPages="                   + (settings.getPrintPageNumbers()?"yes":"no"));
        configurationList.add("-C" + "pageSeparator="                + (settings.getPageSeparator()?"yes":"no"));
        configurationList.add("-C" + "pageSeparatorNumber="          + (settings.getPageSeparatorNumber()?"yes":"no"));
        configurationList.add("-C" + "ignoreEmptyPages="             + (settings.getIgnoreEmptyPages()?"yes":"no"));
        configurationList.add("-C" + "continuePages="                + (settings.getContinuePages()?"yes":"no"));
        configurationList.add("-C" + "mergeUnnumberedPages="         + (settings.getMergeUnnumberedPages()?"yes":"no"));
        configurationList.add("-C" + "pageNumberTopSeparateLine="    + (settings.getPageNumberAtTopOnSeparateLine()?"yes":"no"));
        configurationList.add("-C" + "pageNumberBottomSeparateLine=" + (settings.getPageNumberAtBottomOnSeparateLine()?"yes":"no"));
        configurationList.add("-C" + "printPageNumberRange="         + (settings.getPrintPageNumberRange()?"yes":"no"));
        configurationList.add("-C" + "printPageNumberAt="            + ((settings.getPrintPageNumberAt() == PageNumberPosition.TOP_RIGHT)?"top":"bottom"));
        configurationList.add("-C" + "braillePageNumberAt="          + ((settings.getBraillePageNumberAt() == PageNumberPosition.TOP_RIGHT)?"top":"bottom"));
        configurationList.add("-C" + "printPageNumbersInContents="   + (settings.getPrintPageNumbersInToc()?"yes":"no"));
        configurationList.add("-C" + "braillePageNumbersInContents=" + (settings.getBraillePageNumbersInToc()?"yes":"no"));
        configurationList.add("-C" + "lineFill="                     + liblouisTable.toText(String.valueOf(settings.getLineFillSymbol())));

        configurationList.add(inputFile.getAbsolutePath());
        configurationList.add(outputFile.getAbsolutePath());

        logger.exiting("LiblouisXML","configure");

    }
}
