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

package be.docarch.odt2braille.ooo;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.PEFFileFormat;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.setup.EmbossConfiguration;
import be.docarch.odt2braille.setup.ConfigurationDecoder;
import be.docarch.odt2braille.setup.ConfigurationEncoder;
import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.PEFHandler;
import be.docarch.odt2braille.convert.LiblouisXMLException;
import be.docarch.odt2braille.convert.ODT2PEFConverter;
import be.docarch.odt2braille.convert.ODT2PEFConverterParameters;
import be.docarch.odt2braille.utils.FileCreator;
import be.docarch.odt2braille.check.PEFChecker;
import be.docarch.odt2braille.ooo.check.ReportWriter;
import be.docarch.odt2braille.ooo.dialog.PEFCheckerDialog;
import be.docarch.odt2braille.ooo.dialog.SettingsDialog;
import be.docarch.odt2braille.ooo.dialog.EmbossDialog;
import be.docarch.odt2braille.ooo.dialog.ExportDialog;
import be.docarch.odt2braille.ooo.dialog.InsertDialog;
import be.docarch.odt2braille.ooo.dialog.Interpoint55PrintDialog;
import be.docarch.odt2braille.ooo.dialog.PostConversionDialog;
import be.docarch.odt2braille.ooo.dialog.PreviewDialog;
import be.docarch.odt2braille.ooo.dialog.PrintDialog;
import be.docarch.odt2braille.ooo.dialog.SixKeyEntryDialog;

import be_interpoint.Interpoint55Embosser;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.xml.sax.SAXException;

import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.XWindow;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.AnyConverter;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.frame.XStorable;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XFrame;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.util.XModifiable;
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
import com.sun.star.rdf.RepositoryException;

/**
 * The <code>changeSettings</code>, <code>exportBraille</code> and <code>embossBraille</code> methods
 * roughly correspond with the three main tasks that be called from the 'Braille' menu.
 * The main control flow of these tasks is found here.
 *
 * @author  Bert Frees
 */
public class UnoGUI {

    private static final Logger logger = Constants.getLogger();
    
    private static final boolean DEBUG = false;
    
    private static final String FLAT_XML_FILTER_NAME = "writer8";
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

    private Locale oooLocale;

    public UnoGUI(XComponentContext m_xContext,
                  XFrame m_xFrame) {

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

        } catch (Exception e) {
            handleUnexpectedException(e);
        }
    }

    public void changeSettings() {

        SettingsDialog dialog = null;
        ProgressBar progressBar = null;

        try {

            // Start progress bar
            progressBar = new ProgressBar(xFrame, oooLocale);
            Constants.setStatusIndicator(progressBar);
            progressBar.start();
            progressBar.setSteps(3 + SettingsDialog.getSteps());
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            ODT odt = openODT();
            loadConfiguration(odt);
            progressBar.increment();

            // Create dialog
            dialog = new SettingsDialog(xContext, odt.getConfiguration());

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

            // Clean up
            if (!DEBUG) {
                odt.close();
            }
            
        } catch (Exception e) {
            handleUnexpectedException(e);
        } finally {
            if (dialog != null) {
                dialog.dispose();
            }
            if (progressBar != null) {
                progressBar.close();
            }
        }
    }

    public void exportBraille () {

        File[] brailleFiles = null;
        ProgressBar progressBar = null;

        try {

            // Start progress bar
            progressBar = new ProgressBar(xFrame, oooLocale);
            Constants.setStatusIndicator(progressBar);
            progressBar.start();
            progressBar.setSteps(3);
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            ODT odt = openODT();
            loadConfiguration(odt);
            progressBar.increment();

            // Load export settings
            ExportConfiguration exportSettings = loadExportConfiguration();

            // Create export dialog
            ExportDialog dialog = new ExportDialog(xContext, exportSettings, odt.getConfiguration());

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

            // Convert to PEF
            ODT2PEFConverter converter = new ODT2PEFConverter();
            ODT2PEFConverterParameters parameters 
                    = new ODT2PEFConverterParameters(odt.getConfiguration(), exportSettings);
            for (Map.Entry<String,Object> parameter : parameters) {
                converter.setParameter(parameter.getKey(), parameter.getValue());
            }
            PEF pef = converter.convert(odt);
            
            // Check PEF
            PEFChecker checker = new PEFChecker();
            checker.check(pef, exportSettings);

            // Show warning
            PEFCheckerDialog checkerDialog = new PEFCheckerDialog(checker, xContext, parentWindowPeer);
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
                PEFHandler handlePef = new PEFHandler(pef);

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

                List<PEF.Volume> volumes = pef.getVolumes();

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
            
            // Clean up
            if (!DEBUG) {
                odt.close();
                converter.cleanUp();
                pef.close();
            }
            
        } catch (Exception ex) {
            handleUnexpectedException(ex);
        } finally {
            if (progressBar != null) {
                progressBar.close();
            }
        }
    }

    public void embossBraille () {

        ProgressBar progressBar = null;
        

        try {

            // Start progress bar
            progressBar = new ProgressBar(xFrame, oooLocale);
            Constants.setStatusIndicator(progressBar);
            progressBar.start();
            progressBar.setSteps(3);
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            ODT odt = openODT();
            loadConfiguration(odt);
            progressBar.increment();

            // Load emboss settings
            EmbossConfiguration embossSettings = loadEmbossConfiguration();

            // Create emboss dialog
            EmbossDialog embossDialog = new EmbossDialog(xContext, embossSettings, odt.getConfiguration());

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

            // Convert to PEF
            ODT2PEFConverter converter = new ODT2PEFConverter();
            ODT2PEFConverterParameters parameters 
                    = new ODT2PEFConverterParameters(odt.getConfiguration(), embossSettings);
            for (Map.Entry<String,Object> parameter : parameters) {
                converter.setParameter(parameter.getKey(), parameter.getValue());
            }
            PEF pef = converter.convert(odt);

            // Check PEF
            PEFChecker checker = new PEFChecker();
            checker.check(pef, embossSettings);
            
            // Show warning            
            PEFCheckerDialog checkerDialog = new PEFCheckerDialog(checker, xContext, parentWindowPeer);
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

            // Create PEFHandler entity
            PEFHandler handlePef = new PEFHandler(pef);

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

                File prnFile = FileCreator.createTempFile(".prn");

                handlePef.embossToFile(prnFile, embossSettings);

                if (!printDialog.getPrintToFile()) {

                    // Launch Wprint 55
                    printDialog.runWPrint55(prnFile);

                } else {

                    // Export brf file
                    String brailleExt = ".brf";
                    String fileType = "Interpoint 55 BRF";

                    // Show Save As... Dialog:
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
                    handlePef.embossToDevice(driver, embossSettings);

                } else {

                    String brailleExt = ".prn";
                    String fileType = "Print File";

                    // Show Save As... Dialog:
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
                    handlePef.embossToFile(new File(exportUrl), embossSettings);
                }
            }
            
            // Clean up
            if (!DEBUG) {
                odt.close();
                converter.cleanUp();
                pef.close(); 
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
            
        }
    }

    public void embossBrailleAlternative() {

        ProgressBar progressBar = null;

        try {

           //Start progress bar
            progressBar = new ProgressBar(xFrame, oooLocale);
            Constants.setStatusIndicator(progressBar);
            progressBar.start();
            progressBar.setSteps(2);
            progressBar.setStatus("Analysing document, loading settings...");

            ODT odt = openODT();
            loadConfiguration(odt);
            ExportConfiguration exportSettings = loadExportConfiguration();

            progressBar.finish(true);
            progressBar.close();

            ODT2PEFConverter converter = new ODT2PEFConverter();
            ODT2PEFConverterParameters parameters 
                    = new ODT2PEFConverterParameters(odt.getConfiguration(), exportSettings);
            for (Map.Entry<String,Object> parameter : parameters) {
                converter.setParameter(parameter.getKey(), parameter.getValue());
            }
            PEF pef = converter.convert(odt);
            
            // Check PEF
            PEFChecker checker = new PEFChecker();
            checker.check(pef, exportSettings);

            PEFCheckerDialog checkerDialog = new PEFCheckerDialog(checker, xContext, parentWindowPeer);
            if (!checkerDialog.execute()) {
                logger.log(Level.INFO, "User cancelled export on warning");
                return;
            }



            // TODO: Launch e2u.jar with PEF file as input

			
            
            // Clean up
            if (!DEBUG) {
                odt.close();
                converter.cleanUp();
                pef.close(); 
            }

        } catch (Exception ex) {
            handleUnexpectedException(ex);
        } finally {
            if (progressBar != null) {
                progressBar.close();
            }
        }
    }

    public void sixKeyEntryMode (){

        try {

            XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDesktopComponent);
            XController xController = xModel.getCurrentController();
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                           XTextViewCursorSupplier.class, xController);
            XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();

            SixKeyEntryDialog dialog = new SixKeyEntryDialog(xContext, xFrame, xViewCursor);
            dialog.execute();

        } catch (Exception ex) {
            handleUnexpectedException(ex);
        }
    }

    public void insertBraille () {

        try {

            XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDesktopComponent);
            XController xController = xModel.getCurrentController();
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                           XTextViewCursorSupplier.class, xController);
            XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();

            // Create dialog
            InsertDialog dialog = new InsertDialog(xContext, xViewCursor);
            dialog.execute();

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
            logger.log(Level.SEVERE, null, e);
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
    
    private ExportConfiguration loadExportConfiguration() {

        try {

            InputStream input = new FileInputStream(
                    new File(packageLocation + File.separator + "settings" + File.separator + "export.xml"));
            return (ExportConfiguration)ConfigurationDecoder.readObject(input);

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
            return new ExportConfiguration();
        }
    }

    private void saveExportConfiguration(ExportConfiguration config) {

        try {

            OutputStream output = new FileOutputStream(
                    new File(packageLocation + File.separator + "settings" + File.separator + "export.xml"));
            ConfigurationEncoder.writeObject(config, output);

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    private ODT openODT() throws Exception {

        // Export in ODT Format
        File odtFile = FileCreator.createTempFile(".odt");
        //odtFile.deleteOnExit();
        String odtUnoUrl = UnoUtils.createUnoFileURL(odtFile.getAbsolutePath(), xContext);
        PropertyValue[] conversionProperties = new PropertyValue[1];
        conversionProperties[0] = new PropertyValue();
        conversionProperties[0].Name = "FilterName";

        conversionProperties[0].Value = FLAT_XML_FILTER_NAME;
        XStorable storable = (XStorable) UnoRuntime.queryInterface(
                XStorable.class, xFrame.getController().getModel());
        storable.storeToURL(odtUnoUrl, conversionProperties);
        
        logger.log(Level.INFO, "ODT document written to file: {0}", odtFile.getName()); 

        Constants.getStatusIndicator().increment();
        
        ODT odt = new ODT(odtFile);

        Constants.getStatusIndicator().increment();
        
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

        try {
            
            logger.info("Loading configuration from RDF");
            
            XResource document = getDocumentNode();
            XEnumeration statements = metadataGraph.getStatements(document, CONFIGURATION_GENERAL, null);
            if (statements.hasMoreElements()) {
                 String xml = ((Statement)statements.nextElement()).Object.getStringValue();
                 odt.loadConfiguration(xml, RDF_ENCODING);
            }
            
            logger.info("Configuration loaded");

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    private void saveConfiguration(ODT odt) {

        try {
            
            logger.info("Saving configuration to RDF");

            String xml = odt.saveConfiguration(RDF_ENCODING);

            XURI RDF_XMLLITERAL = URI.createKnown(xContext, URIs.RDF_XMLLITERAL);
            XLiteral configuration = Literal.createWithType(xContext, xml, RDF_XMLLITERAL);
            XResource document = getDocumentNode();

            metadataGraph.removeStatements(document, CONFIGURATION_GENERAL, null);
            metadataGraph.addStatement(document, CONFIGURATION_GENERAL, configuration);
            
            logger.info("Configuration saved");

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
     * @param   e     The exception
     */
    private void handleUnexpectedException(Exception e) {

        logger.log(Level.SEVERE, null, e);

        String message = L10N_Unexpected_Exception_Message + ": " + Constants.getLogFile().getAbsolutePath();
        if (e instanceof LiblouisXMLException ||
            e instanceof IllegalArgumentException) { message = e.getMessage(); }
        UnoAwtUtils.showErrorMessageBox(parentWindowPeer, L10N_Exception_MessageBox_Title, message);

    }
}
