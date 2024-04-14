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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.print.PrintException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import be.docarch.odt2braille.setup.EmbossConfiguration;

import org.daisy.braille.utils.pef.PrinterDevice;
import org.daisy.braille.utils.pef.UnsupportedWidthException;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.embosser.Embosser;
import org.daisy.dotify.api.embosser.EmbosserWriter;

import org.xml.sax.SAXException;

/**
 * This class handles the processing of a .pef file.
 * The .pef file can be converted to a generic braille file <code>convertToFile</code> or
 * an embosser-specific braille file <code>embossToFile</code>.
 * In addition, the .pef file can be printed on a braille embosser (<code>embossToDevice</code>).
 *
 * @author  Bert Frees
 */
public class PEFHandler {

    private final static Logger logger = Constants.getLogger();
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTempDirectory();

    private PEF pef = null;

    /**
     * Creates a new <code>PEFHandler</code> instance.
     */
    public PEFHandler(PEF pef) {

        logger.entering("PEFHandler", "<init>");

        this.pef = pef;
    }

    public File convertToSingleFile(FileFormat format)
                             throws ParserConfigurationException,
                                    IOException,
                                    SAXException,
                                    UnsupportedWidthException {

        logger.entering("PEFHandler", "convertToSingleFile");

        File output = File.createTempFile(TMP_NAME, format.getFileExtension(), TMP_DIR);
        output.deleteOnExit();

        convertToFile(format, pef.getSinglePEF(), output);

        logger.exiting("PEFHandler", "convertToSingleFile");

        return output;
    }

    public File[] convertToFiles(FileFormat format)
                          throws ParserConfigurationException,
                                 IOException,
                                 UnsupportedWidthException,
                                 SAXException,
                                 ConversionException {

        logger.entering("PEFHandler", "convertToFiles");

        File[] outputFiles;
        File[] pefFiles;
            
        pefFiles = pef.getPEFs();

        if (pefFiles == null) { throw new ConversionException(); }

        outputFiles = new File[pefFiles.length];
        for (int i=0; i<outputFiles.length; i++) {

            outputFiles[i] = File.createTempFile(TMP_NAME, format.getFileExtension(), TMP_DIR);
            outputFiles[i].deleteOnExit();
            convertToFile(format, pefFiles[i], outputFiles[i]);
            
        }

        logger.exiting("PEFHandler", "convertToFiles");

        return outputFiles;
    }

    /**
     * Convert to a generic braille file.
     *
     * @param   output  The location where the output file will be saved.
     */
    private boolean convertToFile(FileFormat format,
                                  File input,
                                  File output)
                           throws ParserConfigurationException,
                                  IOException,
                                  SAXException,
                                  UnsupportedWidthException {

        logger.entering("PEFHandler", "convertToFile");

        // TableCatalog uses the context class loader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); {

            EmbosserWriter writer = format.newEmbosserWriter(new FileOutputStream(output));
            org.daisy.braille.utils.pef.PEFHandler handler = new org.daisy.braille.utils.pef.PEFHandler.Builder(writer)
                              .range(null)
                              .align(org.daisy.braille.utils.pef.PEFHandler.Alignment.INNER)
                              .offset(0)
                              .topOffset(0)
                              .build();
            parsePefFile(input, handler);

        } Thread.currentThread().setContextClassLoader(cl);

        logger.exiting("PEFHandler", "convertToFile");

        return true;
    }

    /**
     * Convert to an embosser-specific braille file. The embosser type is specified in embossSettings.
     *
     * @param   output  The location where the output file will be saved.
     */
    public boolean embossToFile(File output,
                                EmbossConfiguration embossSettings)
                         throws ParserConfigurationException,
                                IOException,
                                SAXException,
                                UnsupportedWidthException {

        logger.entering("PEFHandler", "embossToFile");

        int offset = 0;
        int topOffset = 0;

        Embosser embosser = embossSettings.getEmbosser();

        if (embosser.supportsAligning()) {
            offset = embossSettings.getMargins().getInner();
            topOffset = embossSettings.getMargins().getTop();
        }

        // TableCatalog uses the context class loader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); {

            EmbosserWriter writer = embosser.newEmbosserWriter(new FileOutputStream(output));
            org.daisy.braille.utils.pef.PEFHandler handler = new org.daisy.braille.utils.pef.PEFHandler.Builder(writer)
                              .range(null)
                              .align(org.daisy.braille.utils.pef.PEFHandler.Alignment.INNER)
                              .offset(offset)
                              .topOffset(topOffset)
                              .build();
            parsePefFile(pef.getSinglePEF(), handler);

        } Thread.currentThread().setContextClassLoader(cl);

        logger.exiting("PEFHandler", "embossToFile");

        return true;
    }

    /**
     * Print the PEF file on a braille embosser. The embosser type is specified in embossSettings.
     *
     * @param   deviceName    The name of the printer device.
     */
    public boolean embossToDevice(String deviceName,
                                  EmbossConfiguration embossSettings)
                           throws IOException,
                                  PrintException,
                                  ParserConfigurationException,
                                  SAXException,
                                  UnsupportedWidthException {

        logger.entering("PEFHandler", "embossToDevice");

        File prnFile = File.createTempFile(TMP_NAME, ".prn", TMP_DIR);
        prnFile.deleteOnExit();
        
        if (!embossToFile(prnFile, embossSettings)) {
            return false;
        }
        PrinterDevice bd = new PrinterDevice(deviceName, true);
        bd.transmit(prnFile);

        logger.exiting("PEFHandler", "embossToDevice");

        return true;
    }

//    private int getPageCount() throws MalformedURLException,
//                                      IOException{
//
//        NamespaceContext namespace = new NamespaceContext();
//        File pefFile = pef.getSinglePEF();
//
//        if (settings.getDuplex()) {
//            return Integer.parseInt(XPathUtils.evaluateString(pefFile.toURL().openStream(),
//                    "count(//pef:page) + count(//pef:section[following::pef:section and count(pef:page) mod 2 = 1])", namespace));
//        } else {
//            return Integer.parseInt(XPathUtils.evaluateString(pefFile.toURL().openStream(),
//                    "count(//pef:page)", namespace));
//        }
//    }

    private static void parsePefFile(File file, org.daisy.braille.utils.pef.PEFHandler ph)
                              throws ParserConfigurationException,
                                     SAXException,
                                     IOException,
                                     UnsupportedWidthException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser sp = spf.newSAXParser();
        try (InputStream is = new FileInputStream(file)) {
            sp.parse(is, ph);
        } catch (SAXException e) {
            if (ph.hasWidthError())
                throw new UnsupportedWidthException(e);
            else
                throw e;
        }
    }
}
