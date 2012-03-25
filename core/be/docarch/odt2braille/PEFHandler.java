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

import be.docarch.odt2braille.convert.ConversionException;
import be.docarch.odt2braille.setup.EmbossConfiguration;
import be.docarch.odt2braille.utils.FileCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.daisy.braille.embosser.FileFormat;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.UnsupportedWidthException;
import org.daisy.braille.facade.PEFConverterFacade;
import org.daisy.printing.PrinterDevice;

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

    private PEF pef = null;

    /**
     * Creates a new <code>PEFHandler</code> instance.
     */
    public PEFHandler(PEF pef) {
        this.pef = pef;
    }

    public File convertToSingleFile(FileFormat format)
                             throws ParserConfigurationException,
                                    IOException,
                                    SAXException,
                                    UnsupportedWidthException {

        File output = FileCreator.createTempFile(format.getFileExtension());

        convertToFile(format, pef.getSinglePEF(), output);

        return output;
    }

    public File[] convertToFiles(FileFormat format)
                          throws ParserConfigurationException,
                                 IOException,
                                 UnsupportedWidthException,
                                 SAXException,
                                 ConversionException {

        File[] outputFiles;
        File[] pefFiles;
            
        pefFiles = pef.getPEFs();

        if (pefFiles == null) { throw new ConversionException(); }

        outputFiles = new File[pefFiles.length];
        for (int i=0; i<outputFiles.length; i++) {

            outputFiles[i] = FileCreator.createTempFile(format.getFileExtension());
            convertToFile(format, pefFiles[i], outputFiles[i]);
            
        }

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

        logger.log(Level.INFO,"Converting PEF to {0} file", format.getFileExtension());

        // TableCatalog uses the context class loader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); {

            EmbosserWriter writer = format.newEmbosserWriter(new FileOutputStream(output));
            org.daisy.braille.pef.PEFHandler handler = new org.daisy.braille.pef.PEFHandler.Builder(writer)
                              .range(null)
                              .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                              .offset(0)
                              .topOffset(0)
                              .build();
            PEFConverterFacade.parsePefFile(input, handler);

        } Thread.currentThread().setContextClassLoader(cl);
        
        logger.log(Level.INFO, "Output written to {0}", output.getName());

        return true;
    }

    /**
     * Convert to an embosser-specific braille file. The embosser type is specified in embossSettings.
     *
     * @param   output  The location where the output file will be saved.
     */
    public void embossToFile(File output,
                             EmbossConfiguration embossSettings)
                      throws Exception {
        
        logger.info("Converting PEF to embosser file");
        
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
            org.daisy.braille.pef.PEFHandler handler = new org.daisy.braille.pef.PEFHandler.Builder(writer)
                              .range(null)
                              .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                              .offset(offset)
                              .topOffset(topOffset)
                              .build();
            PEFConverterFacade.parsePefFile(pef.getSinglePEF(), handler);

        } Thread.currentThread().setContextClassLoader(cl);
        
        logger.log(Level.INFO, "Output written to {0}", output.getName());
    }

    /**
     * Print the PEF file on a braille embosser. The embosser type is specified in embossSettings.
     *
     * @param   deviceName    The name of the printer device.
     */
    public void embossToDevice(String deviceName,
                               EmbossConfiguration embossSettings)
                        throws Exception {

        File prnFile = FileCreator.createTempFile(".prn");
        
        embossToFile(prnFile, embossSettings);
                
        PrinterDevice bd = new PrinterDevice(deviceName, true);
        bd.transmit(prnFile);
        
        // prnFile.delete();
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
}
