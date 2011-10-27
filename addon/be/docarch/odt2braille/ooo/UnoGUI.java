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

import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.AnyConverter;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.frame.XStorable;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XFrame;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.XWindow;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.util.XModifiable;
import com.sun.star.container.XEnumeration;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.Statement;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URIs;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.Literal;
import com.sun.star.rdf.XLiteral;
import com.sun.star.rdf.XDocumentMetadataAccess;
import com.sun.star.rdf.XRepository;
import com.sun.star.rdf.XNamedGraph;

import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.PEFFileFormat;
import be.docarch.odt2braille.ODT2PEFConverter;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.EmbossConfiguration;
import be.docarch.odt2braille.setup.ConfigurationDecoder;
import be.docarch.odt2braille.setup.ConfigurationEncoder;
import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.PefHandler;
import be.docarch.odt2braille.Volume;
import be.docarch.odt2braille.LiblouisXMLException;
import be.docarch.odt2braille.checker.PostConversionBrailleChecker;
import be.docarch.odt2braille.ooo.checker.BrailleCheckerDialog;
import be.docarch.odt2braille.ooo.checker.ReportWriter;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserFeatures;
import be_interpoint.Interpoint55Embosser;

/**
 * The <code>changeSettings</code>, <code>exportBraille</code> and <code>embossBraille</code> methods
 * roughly correspond with the three main tasks that be called from the 'Braille' menu.
 * The main control flow of these tasks is found here.
 *
 * @author  Bert Frees
 */
public class UnoGUI {

    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();
    private static final String FLAT_XML_FILTER_NAME = "writer8";
    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static final String L10N = Constants.OOO_L10N_PATH;
    private static final String META_FILE = "meta/odt2braille/configuration.rdf";
    private static final String RDF_ENCODING = "UTF-8";

    private XURI CONFIGURATION_GENERAL;
    private XURI CONFIGURATION_EXPORT;

    private String L10N_Exception_MessageBox_Title;
    private String L10N_Unexpected_Exception_Message;

    private String exportFilename;
    private String packageLocation;

    private XMultiComponentFactory xMCF;
    private XComponentContext xContext;
    private XFrame xFrame;
    private XWindowPeer parentWindowPeer;
    private XComponent xDesktopComponent;
    private XModifiable xModifiable;
    private XDocumentMetadataAccess xDMA;
    private XRepository xRepository;
    private XNamedGraph metadataGraph;

    private Handler fh;
    private File logFile;
    private Locale oooLocale;

    public UnoGUI(XComponentContext m_xContext,
                  XFrame m_xFrame) {

        logger.entering("UnoGUI", "<init>");

        try {

            xContext = m_xContext;
            xFrame = m_xFrame;
            xMCF = (XMultiComponentFactory)UnoRuntime.queryInterface(XMultiComponentFactory.class, m_xContext.getServiceManager());
            Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
            XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
            xDesktopComponent = (XComponent)xDesktop.getCurrentComponent();
            xModifiable = (XModifiable)UnoRuntime.queryInterface(XModifiable.class, xDesktop.getCurrentComponent());

            XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xFrame.getController().getModel());
            XWindow parentWindow = xModel.getCurrentController().getFrame().getContainerWindow();
            parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, parentWindow);
            xDMA = (XDocumentMetadataAccess)UnoRuntime.queryInterface(XDocumentMetadataAccess.class, xModel);
            xRepository = xDMA.getRDFRepository();

            XURI CONFIGURATION = URI.create(xContext, "http://www.docarch.be/odt2braille#Configuration");
            CONFIGURATION_GENERAL = URI.create(xContext, "http://www.docarch.be/odt2braille/general-configuration");
            CONFIGURATION_EXPORT = URI.create(xContext, "http://www.docarch.be/odt2braille/export-configuration");

            try {
                metadataGraph = xRepository.getGraph(xDMA.addMetadataFile(META_FILE, new XURI[]{CONFIGURATION}));
            } catch (ElementExistException e) {
                metadataGraph = xRepository.getGraph(URI.create(xContext, xDMA.getNamespace() + META_FILE));
            }

            // Configuring logger
            logFile = File.createTempFile(TMP_NAME, ".log", TMP_DIR);
            logFile.deleteOnExit();
            fh = new FileHandler(logFile.getAbsolutePath());
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.FINEST);

            // Locale
            try { oooLocale = UnoUtils.getUILocale(m_xContext); } catch (Exception e) {
                  oooLocale = Locale.ENGLISH; }
            logger.info("Locale: " + oooLocale.toString());

            ResourceBundle bundle = ResourceBundle.getBundle(L10N, oooLocale);

            L10N_Exception_MessageBox_Title = bundle.getString("exceptionMessageBoxTitle");
            L10N_Unexpected_Exception_Message = bundle.getString("unexpectedExceptionMessage");

            // Export file name
            for (PropertyValue prop: xModel.getArgs()) {
                if (prop.Name.equals("Title")) {
                    exportFilename = (String)AnyConverter.toString(prop.Value);
                    break;
                }
            }

            // Set liblouis directory
            packageLocation = UnoUtils.UnoURLtoURL(PackageInformationProvider.get(m_xContext)
                                        .getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/", m_xContext);

            Constants.setLiblouisDirectory(new File(packageLocation + File.separator + "liblouis"));

            logger.exiting("UnoGUI", "<init>");

        } catch (Exception e) {
            handleUnexpectedException(e);
        }
    }

    /**
     * Overwrite the default settings with settings loaded from the OpenOffice.org Writer document,
     * execute the "Braille Configuration" dialog that enables users to change these settings
     * and store the new settings if the user confirms.
     *
     * @return          <code>true</code> if the new settings were succesfully saved.
     */
    public void changeSettings() {

        logger.entering("UnoGUI", "changeSettings");

        SettingsDialog dialog = null;
        ProgressBar progressBar = null;
        ODT odt = null;

        try {

            // Start progress bar
            progressBar = new ProgressBar(xFrame, oooLocale);
            progressBar.start();
            progressBar.setSteps(3 + SettingsDialog.getSteps());
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            odt = openODT(progressBar);
            loadConfiguration(odt);
            progressBar.increment();

            // Create dialog
            dialog = new SettingsDialog(xContext, odt.getConfiguration(), progressBar);

            // Close progress bar
            progressBar.finish(true);
            progressBar.close();

            // Raise dialog
            if (!dialog.execute()) {
                logger.log(Level.INFO, "User cancelled settings dialog");
                return;
            }

            // Save settings
            saveConfiguration(odt);

            logger.exiting("UnoGUI", "changeSettings");

        } catch (Exception e) {
            handleUnexpectedException(e);
        } finally {
            if (dialog != null) {
                dialog.dispose();
            }
            if (progressBar != null) {
                progressBar.close();
            }
            if (odt != null) {
                odt.close();
            }
        }
    }

    /**
     * Export the document as a generic or embosser-specific braille file.
     *
     * <ul>
     * <li>First, <code>changeSettings</code> is called.</li>
     * <li>A newly created {@link PostConversionBrailleChecker} checks the settings and the flat .odt file, and
     *     if necessary, a first warning box with possible accessibility issues is shown.</li>
     * <li>The user selects the location of the output file in a "Save as" dialog.</li>
     * <li>The document is converted to a temporary .pef file.</li>
     * <li>If necessary, a second warning box with possible accessibility issues is shown.</li>
     * <li>The .pef file is converted to the specified output format and stored at its final location.</li>
     * </ul>
     *
     * @return          <code>true</code> if the document is succesfully exported to a braille file,
     *                  <code>false</code> if the user aborts the process at any time.
     */
    public void exportBraille () {

        logger.entering("UnoGUI", "exportBraille");

        File[] brailleFiles = null;
        ProgressBar progressBar = null;
        ODT odt = null;

        try {

            // Start progress bar
            progressBar = new ProgressBar(xFrame, oooLocale);
            progressBar.start();
            progressBar.setSteps(3);
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            odt = openODT(progressBar);
            loadConfiguration(odt);
            progressBar.increment();

            // Load export settings
            ExportConfiguration exportSettings = loadExportConfiguration();

            // Create export dialog
            ExportDialog dialog = new ExportDialog(xContext, exportSettings, odt.getConfiguration(), progressBar);

            // Close progress bar
            progressBar.finish(true);
            progressBar.close();

            // Execute export dialog
            if (!dialog.execute()) {
                logger.log(Level.INFO, "User cancelled export dialog");
                return;
            }

            // Save settings
            saveConfiguration(odt);
            saveExportConfiguration(exportSettings);
            
            // Checker
            PostConversionBrailleChecker checker = new PostConversionBrailleChecker();

            // Convert to PEF
            PEF pef = ODT2PEFConverter.convert(odt, exportSettings, checker, progressBar);

            // Show warning
            BrailleCheckerDialog checkerDialog = new BrailleCheckerDialog(checker, xContext, parentWindowPeer);
            boolean cancel = !checkerDialog.execute();

            // Store checker report
            ReportWriter reportWriter = new ReportWriter(checker, xContext, xDesktopComponent);
            if (reportWriter.write()) {
                try {
                    xModifiable.setModified(true);
                } catch (PropertyVetoException e) { // read-only
                }
            }

            if (cancel) {
                logger.log(Level.INFO, "User cancelled export on warning");
                return;
            }

            // Convert to Braille file(s)
            if (exportSettings.getFileFormat() instanceof PEFFileFormat)  {

                if (exportSettings.getMultipleFiles()) {
                    brailleFiles = pef.getPEFs();
                } else {
                    brailleFiles = new File[] { pef.getSinglePEF() };
                }

            } else {

                // Create PEFHandler entity
                PefHandler handlePef = new PefHandler(pef);

                // Convert to Braille File
                if (exportSettings.getMultipleFiles()) {
                    brailleFiles = handlePef.convertToFiles(exportSettings.getFileFormat());
                } else {
                    brailleFiles = new File[] { handlePef.convertToSingleFile(exportSettings.getFileFormat()) };
                }
            }

            // Close progressbar
            progressBar.finish(true);
            progressBar.close();

            // Show post translation dialog
            PreviewDialog preview = new PreviewDialog(xContext, pef, odt.getConfiguration(), exportSettings);
            PostConversionDialog postConversionDialog = new PostConversionDialog(xContext, preview);

            if (!postConversionDialog.execute()) {
                logger.log(Level.INFO, "User cancelled post translation dialog");
                return;
            }

            // Show Save As... Dialog:
            String brailleExt = exportSettings.getFileFormat().getFileExtension();
            String fileType;            
            String id = exportSettings.getFileFormat().getIdentifier();
            if (id.equals(ExportConfiguration.PEF)) {
                fileType = "Portable Embosser Format";
            } else if (id.equals(ExportConfiguration.BRF)) {
                fileType = "Braille Formatted";
            } else if (id.equals(ExportConfiguration.BRA)) {
                fileType = "BRA File";
            } else if (id.equals(ExportConfiguration.BRL)) {
                fileType = "MicroBraille File";
            } else {
                fileType = "";
            }

            String exportUnoUrl = UnoAwtUtils.showSaveAsDialog(exportFilename, fileType, "*" + brailleExt, xContext);
            if (exportUnoUrl.length() < 1) {
                logger.log(Level.INFO, "User cancelled save as dialog");
                return;
            }
            String exportUrl = UnoUtils.UnoURLtoURL(exportUnoUrl, xContext);

            // Rename Braille file(s)
            if (brailleFiles.length > 1) {

                File newFile;
                File newFolder = new File(exportUrl);
                newFolder.mkdir();

                String fileSeparator = System.getProperty("file.separator");
                String folderName = newFolder.getName();
                String fileName = folderName;

                if (folderName.lastIndexOf(brailleExt) > -1) {
                    fileName = folderName.substring(0, folderName.lastIndexOf(brailleExt));
                }

                List<Volume> volumes = pef.getVolumes();

                if (brailleFiles.length != volumes.size()) {
                    logger.log(Level.INFO, "The number of brailleFiles is not equals to the number of volumes");
                }

                DecimalFormat format = new DecimalFormat();
                format.setMaximumFractionDigits(0);
                format.setMinimumIntegerDigits(1+(int)Math.floor(Math.log10(volumes.size())));
                format.setGroupingUsed(false);

                for (int i=0; i<brailleFiles.length; i++) {
                    newFile = new File(newFolder.getAbsolutePath() + fileSeparator + fileName + "." + format.format(i+1) + brailleExt);
                    if (newFile.exists()) { newFile.delete(); }
                    brailleFiles[i].renameTo(newFile);
                }

            } else {

                File newFile = new File(exportUrl);
                if (newFile.exists()) { newFile.delete(); }
                brailleFiles[0].renameTo(newFile);
            }

            logger.exiting("UnoGUI", "exportBraille");

        } catch (Exception ex) {
            handleUnexpectedException(ex);
        } finally {
            if (progressBar != null) {
                progressBar.close();
            }
            if (odt != null) {
                odt.close();
            }
        }
    }

    /**
     * Print the document on a braille embosser.
     *
     * <ul>
     * <li>First, <code>changeSettings</code> is called.</li>
     * <li>A newly created {@link PostConversionBrailleChecker} checks the settings and the flat .odt file, and
     *     if necessary, a first warning box with possible accessibility issues is shown.</li>
     * <li>Either the "Emboss" dialog or the "Emboss on Interpoint 55" is shown, depending on the selected embosser type.</li>
     * <li>The document is converted to a temporary .pef file.</li>
     * <li>If necessary, a second warning box with possible accessibility issues is shown.</li>
     * <li>If the "Interpoint 55" was selected, the .pef file is converted to a .brf file
     *     that can be interpreted by the wprint55 program, and wprint55 is executed. Otherwise,
     *     the .pef file is converted to the appropriate embosser-specific braille format and send to the specified printer device.</li>
     * </ul>
     *
     * @return          <code>true</code> if the document is succesfully printed on a braille embosser,
     *                  <code>false</code> if the user aborts the process at any time.
     */
    public void embossBraille () {

        logger.entering("UnoGUI", "embossBraille");

        ProgressBar progressBar = null;
        ODT odt = null;

        try {

            // Start progress bar
            progressBar = new ProgressBar(xFrame, oooLocale);
            progressBar.start();
            progressBar.setSteps(3);
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            odt = openODT(progressBar);
            loadConfiguration(odt);
            progressBar.increment();

            // Load emboss settings
            EmbossConfiguration embossSettings = loadEmbossConfiguration();

            // Create emboss dialog
            EmbossDialog embossDialog = new EmbossDialog(xContext, embossSettings, odt.getConfiguration(), progressBar);

            // Close progress bar
            progressBar.finish(true);
            progressBar.close();

            // Execute emboss dialog
            if (!embossDialog.execute()) {
                logger.log(Level.INFO, "User cancelled emboss dialog");
                return;
            }

            // Save settings
            saveConfiguration(odt);
            saveEmbossConfiguration(embossSettings);

            // Checker
            PostConversionBrailleChecker checker = new PostConversionBrailleChecker();

            // Convert to PEF
            PEF pef = ODT2PEFConverter.convert(odt, embossSettings, checker, progressBar);

            // Show warning            
            BrailleCheckerDialog checkerDialog = new BrailleCheckerDialog(checker, xContext, parentWindowPeer);
            if (!checkerDialog.execute()) {
                logger.log(Level.INFO, "User cancelled export on warning");
                return;
            }

            // Close progress bar
            progressBar.finish(true);
            progressBar.close();

            // Show post translation dialog
            PreviewDialog preview = new PreviewDialog(xContext, pef, odt.getConfiguration(), embossSettings);
            PostConversionDialog postConversionDialog = new PostConversionDialog(xContext, preview);

            if (!postConversionDialog.execute()) {
                logger.log(Level.INFO, "User cancelled post translation dialog");
                return;
            }

            // Create EmbossPEF entity
            PefHandler handlePef = new PefHandler(pef);

            // Load embosser with paper
            Embosser embosser = embossSettings.getEmbosser();
            embosser.setFeature(EmbosserFeatures.PAGE_FORMAT, embossSettings.getPageFormat());

            // Emboss Dialog
            if (embosser instanceof Interpoint55Embosser) {

                Interpoint55PrintDialog printDialog = new Interpoint55PrintDialog(xContext, xDesktopComponent, (Interpoint55Embosser)embosser);

                if (!printDialog.execute()) {
                    logger.log(Level.INFO, "User cancelled emboss dialog");
                    return;
                }

                File prnFile = File.createTempFile(TMP_NAME, ".prn", TMP_DIR);
                prnFile.deleteOnExit();
                if (!handlePef.embossToFile(prnFile, embossSettings)) {
                    return;
                }

                if (!printDialog.getPrintToFile()) {

                    // Launch Wprint 55
                    printDialog.runWPrint55(prnFile);

                } else {

                    // Export brf file
                    String brailleExt = ".brf";
                    String fileType = "Interpoint 55 BRF";

                    // Show Save As... Dialog:
                    logger.entering("UnoAwtUtils", "showSaveAsDialog");
                    String exportUnoUrl = UnoAwtUtils.showSaveAsDialog(exportFilename, fileType, "*" + brailleExt, xContext);
                    if (exportUnoUrl.length() < 1) {
                        logger.log(Level.INFO, "User cancelled save as dialog");
                        return;
                    }
                    String exportUrl = UnoUtils.UnoURLtoURL(exportUnoUrl, xContext);

                    // Rename Braille file
                    File newFile = new File(exportUrl);
                    if (newFile.exists()) { newFile.delete(); }
                    prnFile.renameTo(newFile);

                }

            } else {

                // Set default driver
                File driverSettings = new File(packageLocation + File.separator + "settings" + File.separator + "driver.properties");
                Properties prop = new Properties();
                if (driverSettings.exists()) {
                    FileInputStream inputStream = new FileInputStream(driverSettings);
                    if (inputStream != null) {
                        prop.load(inputStream);
                        inputStream.close();
                    }
                }

                PrintDialog printDialog = new PrintDialog(xContext, embosser, prop.getProperty("default"));
                String driver = printDialog.execute();

                if (driver.equals("")) {
                    logger.log(Level.INFO, "User cancelled emboss dialog");
                    return;
                }

                // Remember driver
                if (!driverSettings.exists()) { driverSettings.createNewFile(); }
                FileOutputStream outputStream = new FileOutputStream(driverSettings);
                if (outputStream != null) {
                    prop.setProperty("default", driver);
                    prop.store(outputStream, null);
                    outputStream.close();
                }

                if (!printDialog.getPrintToFile()) {

                    // Print to device
                    if(!handlePef.embossToDevice(driver, embossSettings)) {
                        return;
                    }

                } else {

                    String brailleExt = ".prn";
                    String fileType = "Print File";

                    // Show Save As... Dialog:
                    logger.entering("UnoAwtUtils", "showSaveAsDialog");
                    String exportUnoUrl = UnoAwtUtils.showSaveAsDialog(exportFilename, fileType, "*" + brailleExt, xContext);
                    if (exportUnoUrl.length() < 1) {
                        logger.log(Level.INFO, "User cancelled save as dialog");
                        return;
                    }
                    if (!exportUnoUrl.endsWith(brailleExt)) {
                        exportUnoUrl = exportUnoUrl.concat(brailleExt);
                    }
                    String exportUrl = UnoUtils.UnoURLtoURL(exportUnoUrl, xContext);

                    // Print to File
                    if(!handlePef.embossToFile(new File(exportUrl), embossSettings)) {
                        logger.log(Level.INFO, "Emboss to file failed");
                        return;
                    }
                }
            }

            // TODO: change PrintDate & PrintedBy information in DocumentProperties
            // http://api.openoffice.org/docs/common/ref/com/sun/star/document/XDocumentProperties.html#PrintedBy
            // http://api.openoffice.org/docs/common/ref/com/sun/star/document/XDocumentProperties.html#PrintDate

        } catch (Exception ex) {
            handleUnexpectedException(ex);
        } finally {
            if (progressBar != null) {
                progressBar.close();
            }
            if (odt != null) {
                odt.close();
            }
        }
    }

    public void sixKeyEntryMode (){

        logger.entering("UnoGUI", "sixKeyEntryMode");

        try {

            XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDesktopComponent);
            XController xController = xModel.getCurrentController();
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                           XTextViewCursorSupplier.class, xController);
            XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();

            SixKeyEntryDialog dialog = new SixKeyEntryDialog(xContext, xFrame, xViewCursor);
            dialog.execute();

            logger.exiting("UnoGUI", "sixKeyEntryMode");

        } catch (Exception ex) {
            handleUnexpectedException(ex);
        }
    }

    public void insertBraille () {

        logger.entering("UnoGUI", "insertBraille");

        try {

            XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDesktopComponent);
            XController xController = xModel.getCurrentController();
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                           XTextViewCursorSupplier.class, xController);
            XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();

            // Create dialog
            InsertDialog dialog = new InsertDialog(xContext, xViewCursor);
            dialog.execute();

            logger.exiting("UnoGUI", "insertBraille");

        } catch (Exception ex) {
            handleUnexpectedException(ex);
        }
    }

    private EmbossConfiguration loadEmbossConfiguration() {

        try {

            InputStream input = new FileInputStream(
                    new File(packageLocation + File.separator + "settings" + File.separator + "embosser.xml"));
            return (EmbossConfiguration)ConfigurationDecoder.readObject(input);

        } catch (Exception e) {
            return new EmbossConfiguration();
        }
    }

    private void saveEmbossConfiguration(EmbossConfiguration config) {

        try {

            OutputStream output = new FileOutputStream(
                    new File(packageLocation + File.separator + "settings" + File.separator + "embosser.xml"));
            ConfigurationEncoder.writeObject(config, output);

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    private ODT openODT(ProgressBar progressBar)
                 throws IOException,
                        TransformerException,
                        TransformerConfigurationException,
                        ParserConfigurationException,
                        com.sun.star.uno.Exception {
    
        logger.entering("UnoGUI", "openODT");

        // Export in ODT Format
        File odtFile = File.createTempFile(TMP_NAME, ".odt", TMP_DIR);
        odtFile.deleteOnExit();
        String odtUnoUrl = UnoUtils.createUnoFileURL(odtFile.getAbsolutePath(), xContext);
        PropertyValue[] conversionProperties = new PropertyValue[1];
        conversionProperties[0] = new PropertyValue();
        conversionProperties[0].Name = "FilterName";

        conversionProperties[0].Value = FLAT_XML_FILTER_NAME;
        XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, xFrame.getController().getModel());
        storable.storeToURL(odtUnoUrl, conversionProperties);
        
        progressBar.increment();
        
        ODT odt = new ODT(odtFile, progressBar);

        progressBar.increment();
        
        logger.exiting("UnoGUI", "openODT");
        
        return odt;        
    }

    private void loadConfiguration(ODT odt)
                            throws ParserConfigurationException,
                                   IOException,
                                   SAXException,
                                   TransformerConfigurationException,
                                   TransformerException,
                                   com.sun.star.uno.Exception,
                                   Exception {

        logger.entering("UnoGUI", "loadConfiguration");

        try {
            
            XResource document = getDocumentNode();
            XEnumeration statements = metadataGraph.getStatements(document, CONFIGURATION_GENERAL, null);
            if (statements.hasMoreElements()) {
                 String xml = ((Statement)statements.nextElement()).Object.getStringValue();
                 odt.loadConfiguration(xml, RDF_ENCODING);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }

        logger.exiting("UnoGUI", "loadConfiguration");
    }

    private void saveConfiguration(ODT odt) {

        logger.entering("UnoGUI", "saveConfiguration");

        try {

            String xml = odt.saveConfiguration(RDF_ENCODING);

            XURI RDF_XMLLITERAL = URI.createKnown(xContext, URIs.RDF_XMLLITERAL);
            XLiteral configuration = Literal.createWithType(xContext, xml, RDF_XMLLITERAL);
            XResource document = getDocumentNode();

            metadataGraph.removeStatements(document, CONFIGURATION_GENERAL, null);
            metadataGraph.addStatement(document, CONFIGURATION_GENERAL, configuration);

            xModifiable.setModified(true);

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }

        logger.exiting("UnoGUI", "saveConfiguration");
    }

    private ExportConfiguration loadExportConfiguration() {

        try {

            XResource document = getDocumentNode();
            XEnumeration statements = metadataGraph.getStatements(document, CONFIGURATION_EXPORT, null);
            if (statements.hasMoreElements()) {
                 String xml = ((Statement)statements.nextElement()).Object.getStringValue();
                 InputStream input = new ByteArrayInputStream(xml.getBytes(RDF_ENCODING));
                 return (ExportConfiguration)ConfigurationDecoder.readObject(input);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }

        return new ExportConfiguration();
    }

    private void saveExportConfiguration(ExportConfiguration config) {

        try {

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ConfigurationEncoder.writeObject(config, output);
            String xml = output.toString(RDF_ENCODING).replaceFirst("<\\?xml.*?\\?>", "");

            XURI RDF_XMLLITERAL = URI.createKnown(xContext, URIs.RDF_XMLLITERAL);
            XLiteral configuration = Literal.createWithType(xContext, xml, RDF_XMLLITERAL);
            XResource document = getDocumentNode();

            metadataGraph.removeStatements(document, CONFIGURATION_EXPORT, null);
            metadataGraph.addStatement(document, CONFIGURATION_EXPORT, configuration);

            xModifiable.setModified(true);

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    private XResource getDocumentNode() throws IllegalArgumentException,
                                               NoSuchElementException,
                                               RepositoryException,
                                               WrappedTargetException {

        XURI RDF_TYPE = URI.createKnown(xContext, URIs.RDF_TYPE);
        XURI PKG_DOCUMENT = URI.createKnown(xContext, URIs.PKG_DOCUMENT);

        XResource document;
        XEnumeration statements = metadataGraph.getStatements(null, RDF_TYPE, PKG_DOCUMENT);
        if (statements.hasMoreElements()) {
            document = ((Statement)statements.nextElement()).Subject;
        } else {
            document = xRepository.createBlankNode();
            metadataGraph.addStatement(document, RDF_TYPE, PKG_DOCUMENT);
        }

        return document;
    }

    /**
     * Handling of an unexpected exception.
     * A message box is shown with a reference to the log file.
     *
     * @param   ex     The exception
     */
    private void handleUnexpectedException(Exception e) {

        logger.log(Level.SEVERE, null, e);

        String message = L10N_Unexpected_Exception_Message + ": " + logFile.getAbsolutePath();
        if (e instanceof LiblouisXMLException ||
            e instanceof IllegalArgumentException) { message = e.getMessage(); }
        UnoAwtUtils.showErrorMessageBox(parentWindowPeer, L10N_Exception_MessageBox_Title, message);

    }

    /**
     * Flush and close the logfile handler.
     */
    public void cleanLogger () {
        if (fh != null) {
            fh.flush();
            fh.close();
        }
    }
}
