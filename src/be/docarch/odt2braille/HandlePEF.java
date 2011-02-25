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

import java.util.logging.Logger;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.net.MalformedURLException;
import org.xml.sax.SAXException;
import org.daisy.util.xml.validation.ValidationException;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.print.PrintException;

import be.docarch.odt2braille.BrailleFileExporter.BrailleFileType;
import org_pef_text.pef2text.EmbosserFactory.EmbosserType;
import org_pef_text.pef2text.PEFHandler.AlignmentFallback;
import org_pef_text.pef2text.PrinterDevice;
import org_pef_text.pef2text.AbstractEmbosser;
import org_pef_text.pef2text.PEFHandler;
import org_pef_text.pef2text.PEFParser;
import org_pef_text.pef2text.EmbosserFactoryException;
import org_pef_text.pef2text.UnsupportedWidthException;

//import com_indexbraille.BlueBarEmbosser;
//import org.daisy.braille.embosser.EmbosserWriter;
//import org.daisy.braille.pef.PEFHandler.Alignment;
//import org_pef_text.pef2text.Paper.PaperSize;
//import org_pef_text.pef2text.Paper;


/**
 * This class handles the processing of a .pef file.
 * The .pef file can be converted to a generic braille file <code>convertToFile</code> or
 * an embosser-specific braille file <code>embossToFile</code>.
 * In addition, the .pef file can be printed on a braille embosser (<code>embossToDevice</code>).
 *
 * @author  Bert Frees
 */
public class HandlePEF {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();

    private Settings settings = null;
    private PEF pef = null;


    /**
     * Creates a new <code>HandlePEF</code> instance.
     *
     * @param  pefUrl      The URL of the .pef file.
     * @param  settings    The braille settings.
     */
    public HandlePEF(PEF pef,
                     Settings settings) {

        logger.entering("HandlePEF", "<init>");

        this.settings = settings;
        this.pef = pef;

    }

    /**
     * Convert to the Interpoint .x55 file format. Not implemented yet.
     *
     */
    public void convertToX55(File output) {}


    public File convertToSingleFile(BrailleFileType fileType)
                             throws ParserConfigurationException,
                                    IOException,
                                    SAXException,
                                    UnsupportedWidthException {

        logger.entering("HandlePEF", "convertToSingleFile");

        File output = File.createTempFile(TMP_NAME, "." + fileType.name().toLowerCase(), TMP_DIR);
        output.deleteOnExit();

        convertToFile(fileType, pef.getSinglePEF(), output);

        logger.exiting("HandlePEF", "convertToSingleFile");

        return output;

    }

    public File[] convertToFiles(BrailleFileType fileType)
                          throws ParserConfigurationException,
                                 IOException,
                                 UnsupportedWidthException,
                                 XMLStreamException,
                                 TransformerException,
                                 ValidationException,
                                 SAXException {

        logger.entering("HandlePEF", "convertToFiles");

        File[] outputFiles;
        File[] pefFiles;
            
        if (settings.getMultipleFiles()) {
            pefFiles = pef.getPEFs();
        } else {
            pefFiles = new File[] { pef.getSinglePEF() };
        }

        if (pefFiles == null) {
            return null;
        }

        outputFiles = new File[pefFiles.length];
        for (int i=0; i<outputFiles.length; i++) {

            outputFiles[i] = File.createTempFile(TMP_NAME, "." + fileType.name().toLowerCase(), TMP_DIR);
            outputFiles[i].deleteOnExit();
            convertToFile(fileType, pefFiles[i], outputFiles[i]);
            
        }

        logger.exiting("HandlePEF", "convertToFiles");

        return outputFiles;

    }

    /**
     * Convert to a generic braille file.
     *
     * @param   type    {@link BrailleFileType#BRF}, {@link BrailleFileType#BRF_INTERPOINT} or {@link BrailleFileType#BRL}.
     * @param   output  The location where the output file will be saved.
     */
    private boolean convertToFile (BrailleFileType fileType,
                                   File input,
                                   File output)
                            throws ParserConfigurationException,
                                   IOException,
                                   SAXException,
                                   UnsupportedWidthException {

        logger.entering("HandlePEF", "convertToFile");

        AbstractEmbosser brailleFileExporter = new BrailleFileExporter(new FileOutputStream(output),
                                                                       fileType,
                                                                       settings.getTable(),
                                                                       settings.getCellsPerLine(),
                                                                       settings.getLinesPerPage(),
                                                                       settings.getDuplex());
        PEFHandler.Builder builder = new PEFHandler.Builder(brailleFileExporter)
                  .alignmentFallback(AlignmentFallback.LEFT.name())
                  .mirrorAlignment(false)
                  .offset(0)
                  .topOffset(0);
        PEFParser.parse(input, builder.build());

        logger.exiting("HandlePEF", "convertToFile");

        return true;

    }

    /**
     * Convert to an embosser-specific braille file. The embosser type is specified in the braille settings.
     *
     * @param   output  The location where the output file will be saved.
     */
    public boolean embossToFile(File output,
                                int numberOfCopies)
                         throws ParserConfigurationException,
                                EmbosserFactoryException,
                                IOException,
                                SAXException,
                                UnsupportedWidthException {

        logger.entering("HandlePEF", "embossToFile");

//        if (settings.getEmbosser() == EmbosserType.INDEX_BASIC_BLUE_BAR) {
//
//            BlueBarEmbosser embosser = new BlueBarEmbosser("", "");
//            EmbosserWriter writer = embosser.newEmbosserWriter(new FileOutputStream(output));
//            org.daisy.braille.pef.PEFHandler.Builder b = new org.daisy.braille.pef.PEFHandler.Builder(writer)
//                      .range(null)
//                      .align(Alignment.INNER)
//                      .offset(offset)
//                      .topOffset(topOffset);
//
//            org.daisy.braille.ui.PEFParser.parse(pef.getSinglePEF(), b.build());
//
//        }

        EmbosserFactory ef = new EmbosserFactory(settings);
        ef.setNumberOfCopies(numberOfCopies);
        ef.setPageCount(getPageCount());

        int offset;
        int topOffset;

        switch (settings.getEmbosser()) {

            /* Margins not in header (or configuration file) */
            case INDEX_BASIC_BLUE_BAR:
            case INDEX_BASIC_S_V2:
            case INDEX_BASIC_D_V2:
            case INDEX_EVEREST_D_V2:
            case INDEX_4X4_PRO_V2:
            case BRAILLO_200:
            case BRAILLO_400_S:
            case BRAILLO_400_SR:
                offset = settings.getMarginInner();
                topOffset = settings.getMarginTop();
                break;

            /* Margins in header (or configuration file) */
            case INDEX_BASIC_D_V3:
            case INDEX_EVEREST_D_V3:
            case INDEX_4X4_PRO_V3:
            case INDEX_4WAVES_PRO_V3:
            case IMPACTO_600:
            case IMPACTO_TEXTO:
            case PORTATHIEL_BLUE:
            case INTERPOINT_55:
            default:
                offset = 0;
                topOffset = 0;
                break;

        }

        AbstractEmbosser embosserObj = ef.newEmbosser(new FileOutputStream(output));
        PEFHandler.Builder builder = new PEFHandler.Builder(embosserObj)
                  .range(null)
                  .alignmentFallback(AlignmentFallback.LEFT.name())
                  .mirrorAlignment(true)
                  .offset(offset)
                  .topOffset(topOffset);
        PEFParser.parse(pef.getSinglePEF(), builder.build());

        logger.exiting("HandlePEF", "embossToFile");

        return true;

    }

    /**
     * Print the .pef file on a braille embosser. The embosser type is specified in the braille settings.
     * If the "Interpoint 55" was selected, the .pef file is converted to a .brf file
     * that can be interpreted by the wprint55 program, and wprint55 is executed. Otherwise,
     * the .pef file is converted to the appropriate embosser-specific braille format with <code>embossToFile</code>
     * and send to the specified printer device.
     *
     * @param   deviceName    The name of the printer device.
     */
    public boolean embossToDevice(String deviceName,
                                  int numberOfCopies)
                           throws IOException,
                                  PrintException,
                                  ParserConfigurationException,
                                  SAXException,
                                  UnsupportedWidthException,
                                  EmbosserFactoryException {

        logger.entering("HandlePEF", "embossToDevice");

        File prnFile = File.createTempFile(TMP_NAME, ".prn", TMP_DIR);
        prnFile.deleteOnExit();

        if (settings.getEmbosser()==EmbosserType.INTERPOINT_55) {
            if (!convertToFile(BrailleFileType.BRF_INTERPOINT, pef.getSinglePEF(), prnFile)) {
                return false;
            }
        } else {
            if (!embossToFile(prnFile, numberOfCopies)) {
                return false;
            }
            PrinterDevice bd = new PrinterDevice(deviceName, true);
            bd.transmit(prnFile);
        }

        logger.exiting("HandlePEF", "embossToDevice");

        return true;

    }

    private int getPageCount() throws MalformedURLException,
                                      IOException{

        NamespaceContext namespace = new NamespaceContext();
        File pefFile = pef.getSinglePEF();

        if (settings.getDuplex()) {
            return Integer.parseInt(XPathUtils.evaluateString(pefFile.toURL().openStream(),
                    "count(//pef:page) + count(//pef:section[following::pef:section and count(pef:page) mod 2 = 1])", namespace));
        } else {
            return Integer.parseInt(XPathUtils.evaluateString(pefFile.toURL().openStream(),
                    "count(//pef:page)", namespace));
        }
    }
}
