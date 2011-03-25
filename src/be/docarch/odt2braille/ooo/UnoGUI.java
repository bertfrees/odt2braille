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
import java.util.List;
import java.util.Locale;
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

import org.xml.sax.SAXException;
import org.daisy.util.xml.validation.ValidationException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import javax.print.PrintException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.LiblouisXML;
import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.OdtTransformer;
import be.docarch.odt2braille.HandlePEF;
import be.docarch.odt2braille.Volume;
import be.docarch.odt2braille.BrailleFileExporter.BrailleFileType;
import be.docarch.odt2braille.Checker;
import org_pef_text.pef2text.EmbosserFactory.EmbosserType;

import org_pef_text.pef2text.UnsupportedWidthException;
import org_pef_text.pef2text.EmbosserFactoryException;
import be.docarch.odt2braille.LiblouisXMLException;


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

    private static String L10N_Default_Export_Filename = null;
    private static String L10N_Warning_MessageBox_Title = null;
    private static String L10N_Exception_MessageBox_Title = null;
    private static String L10N_Unexpected_Exception_Message = null;

    private String exportFilename = null;

    private XMultiComponentFactory xMCF = null;
    private XComponentContext m_xContext = null;
    private XFrame m_xFrame = null;
    private XModel xDoc = null;
    private XWindow parentWindow = null;
    private XWindowPeer parentWindowPeer = null;
    private XComponent xDesktopComponent = null;

    private Handler fh = null;
    private File logFile = null;

    private OdtTransformer odtTransformer = null;

    private File liblouisLocation = null;

    private SettingsIO settingsIO = null;
    private ProgressBar progressBar = null;

    private Settings loadedSettings = null;
    private Settings changedSettings = null;

    private Locale odtLocale = null;
    private Locale oooLocale = null;


    /**
     * Configure logger and locale.
     *
     * @param   m_xContext
     * @param   m_xFrame
     */
    public UnoGUI(XComponentContext m_xContext,
                  XFrame m_xFrame) {

        logger.entering("UnoGUI", "<init>");

        try {

            this.m_xContext = m_xContext;
            this.m_xFrame = m_xFrame;
            this.xMCF = (XMultiComponentFactory)UnoRuntime.queryInterface(XMultiComponentFactory.class, m_xContext.getServiceManager());
            Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
            XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
            this.xDesktopComponent = (XComponent)xDesktop.getCurrentComponent();

            // Query Uno Object
            xDoc = (XModel) UnoRuntime.queryInterface(XModel.class, m_xFrame.getController().getModel());
            parentWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
            parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, parentWindow);

            // Create progress bar
            progressBar = new ProgressBar(m_xFrame);

            // Configuring logger
            logFile = File.createTempFile(TMP_NAME, ".log", TMP_DIR);
            logFile.deleteOnExit();
            fh = new FileHandler(logFile.getAbsolutePath());
            fh.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fh);
            Logger.getLogger("").setLevel(Level.FINEST);

            // L10N

            Locale.setDefault(Locale.ENGLISH);

            try {
                oooLocale = new Locale(UnoUtils.getUILocale(m_xContext));
            } catch (com.sun.star.uno.Exception ex) {
                logger.log(Level.SEVERE, null, ex);
                oooLocale = Locale.getDefault();
            }

            logger.exiting("UnoGUI", "<init>");

        } catch (IOException ex) {
            handleUnexpectedException(ex);
        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
        }

    }

    /**
     * The OpenOffice.org Writer document is exported as an .odt file and converted to a "flat XML" .odt file
     * (a single file that is the concatenation of all XML files in a normal .odt file).
     *
     */
    private void initialise () {

        logger.entering("UnoGUI", "initialise");

        try {

            L10N_Default_Export_Filename = ResourceBundle.getBundle(L10N, oooLocale).getString("defaultExportFilename");
            L10N_Warning_MessageBox_Title = ResourceBundle.getBundle(L10N, oooLocale).getString("warningMessageBoxTitle");
            L10N_Exception_MessageBox_Title = ResourceBundle.getBundle(L10N, oooLocale).getString("exceptionMessageBoxTitle");
            L10N_Unexpected_Exception_Message = ResourceBundle.getBundle(L10N, oooLocale).getString("unexpectedExceptionMessage");

            exportFilename = L10N_Default_Export_Filename;
            for (PropertyValue prop: xDoc.getArgs()) {
                if (prop.Name.equals("Title")) {
                    exportFilename = (String)AnyConverter.toString(prop.Value);
                    break;
                }
            }

            // Export in ODT Format
            File odtFile = File.createTempFile(TMP_NAME, ".odt", TMP_DIR);
            odtFile.deleteOnExit();
            String odtUnoUrl = UnoUtils.createUnoFileURL(odtFile.getAbsolutePath(), m_xContext);
            PropertyValue[] conversionProperties = new PropertyValue[1];
            conversionProperties[0] = new PropertyValue();
            conversionProperties[0].Name = "FilterName";

            conversionProperties[0].Value = FLAT_XML_FILTER_NAME;
            XStorable storable = (XStorable) UnoRuntime.queryInterface(
                    XStorable.class, m_xFrame.getController().getModel());
            storable.storeToURL(odtUnoUrl, conversionProperties);
            progressBar.increment();

            // Create odtTransformer
            odtTransformer = new OdtTransformer(odtFile, progressBar, oooLocale);
            progressBar.increment();

            // Locale
            odtLocale = odtTransformer.getOdtLocale();

            // Set liblouis directory
            String packageLocation = UnoUtils.UnoURLtoURL(PackageInformationProvider.get(m_xContext)
                                        .getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/", m_xContext);

            liblouisLocation = new File(packageLocation + File.separator + "liblouis");

            // Create new settingsIO
            settingsIO = new SettingsIO(m_xContext, xDesktopComponent);

            // Load settings
            loadedSettings = new Settings(odtTransformer, odtLocale);
            progressBar.increment();
            settingsIO.loadBrailleSettingsFromDocument(loadedSettings);

            logger.exiting("UnoGUI", "initialise");

        } catch (ParserConfigurationException ex) {
            handleUnexpectedException(ex);
        } catch (IOException ex) {
            handleUnexpectedException(ex);
        } catch (SAXException ex) {
            handleUnexpectedException(ex);
        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
        } catch (TransformerConfigurationException ex) {
            handleUnexpectedException(ex);
        } catch (TransformerException ex) {
            handleUnexpectedException(ex);
        }
    }

    /**
     * Overwrite the default settings with settings loaded from the OpenOffice.org Writer document,
     * execute the "Braille Settings" dialog that enables users to change these settings
     * and store the new settings if the user confirms.
     *
     * @return          <code>true</code> if the new settings were succesfully saved.
     */
    public boolean changeSettings() {

        logger.entering("UnoGUI", "changeSettings");

        SettingsDialog settingsDialog = null;

        try {

            // Create dialog
            settingsDialog = new SettingsDialog(m_xContext);

            // Start progressbar
            progressBar.start();
            progressBar.setSteps(4);
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            initialise();
            changedSettings = new Settings(loadedSettings);

            // Initialize dialog
            settingsDialog.initialise(changedSettings, progressBar);

            // Close progress bar
            progressBar.finish(true);
            progressBar.close();

            // Raise dialog
            if (!settingsDialog.execute()) {
                logger.log(Level.INFO, "User cancelled settings dialog");
                return false;
            }

            settingsIO.saveBrailleSettingsToDocument(changedSettings, loadedSettings);

            logger.exiting("UnoGUI", "changeSettings");

            return true;

        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
            return false;
        } finally {
            if (settingsDialog != null) {
                settingsDialog.dispose();
            }
        }
    }

    /**
     * Export the document as a generic or embosser-specific braille file.
     *
     * <ul>
     * <li>First, <code>changeSettings</code> is called.</li>
     * <li>A newly created {@link Checker} checks the settings and the flat .odt file, and
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
    public boolean exportBraille () {

        logger.entering("UnoGUI", "exportBraille");

        PEF pef = null;
        File[] brailleFiles = null;
        String warning = null;
        Checker checker = null;

        try {

            // Start progress bar
            progressBar.start();
            progressBar.setSteps(3);
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            initialise();

            // Load export settings
            settingsIO.loadExportSettingsFromDocument(loadedSettings);
            changedSettings = new Settings(loadedSettings);

            // Create export dialog
            ExportDialog exportDialog = new ExportDialog(m_xContext, changedSettings, progressBar);

            // Close progress bar
            progressBar.finish(true);
            progressBar.close();

            // Execute export dialog
            if (!exportDialog.execute()) {
                logger.log(Level.INFO, "User cancelled export dialog");
                return false;
            }

            // Save settings
            settingsIO.saveBrailleSettingsToDocument(changedSettings, loadedSettings);
            settingsIO.saveExportSettingsToDocument(changedSettings, loadedSettings);

            // Create LiblouisXML
            LiblouisXML liblouisXML = new LiblouisXML(changedSettings, liblouisLocation);

            // Checker
            checker = new Checker(oooLocale, changedSettings, odtTransformer);

            // Create PEF
            pef = new PEF(odtTransformer, liblouisXML, changedSettings, progressBar, checker, oooLocale);

            // Translate into braille
            if(!pef.makePEF()) {
                progressBar.finish(false);
                return false;
            }

            // Checker
            checker.checkPefFile(pef.getSinglePEF());

            // Show warning
            if (!(warning = checker.getWarning()).equals("")) {
                if (UnoAwtUtils.showYesNoWarningMessageBox(parentWindowPeer, L10N_Warning_MessageBox_Title, warning + "\n\n") == (short) 3) {
                    logger.log(Level.INFO, "User cancelled export on warning");
                    return false;
                }
            }

            // Convert to Braille file(s)
            if (changedSettings.getBrailleFileType() == BrailleFileType.PEF)  {

                if (changedSettings.getMultipleFilesEnabled()) {
                    brailleFiles = pef.getPEFs();
                } else {
                    brailleFiles = new File[] { pef.getSinglePEF() };
                }

            } else {

                // Create HandlePEF entity
                HandlePEF handlePef = new HandlePEF(pef, changedSettings);

                // Convert to Braille File
                if((brailleFiles = handlePef.convertToFiles(changedSettings.getBrailleFileType())) == null) {
                    return false;
                }
            }

            // Close progressbar
            progressBar.finish(true);
            progressBar.close();

            // Show post translation dialog
            PreviewDialog preview = new PreviewDialog(m_xContext, pef, changedSettings);
            PostTranslationDialog postTranslationDialog = new PostTranslationDialog(m_xContext, preview);

            if (!postTranslationDialog.execute()) {
                logger.log(Level.INFO, "User cancelled post translation dialog");
                return false;
            }

            // Show Save As... Dialog:

            String brailleExt = "." + changedSettings.getBrailleFileType().name().toLowerCase();
            String fileType;
            switch (changedSettings.getBrailleFileType()) {
                case PEF:
                    fileType = "Portable Embosser Format";
                    break;
                case BRF:
                    fileType = "Braille Formatted";
                    break;
                case BRL:
                    fileType = "MicroBraille File";
                    break;
                default:
                    fileType = "";
            }

            String exportUnoUrl = UnoAwtUtils.showSaveAsDialog(exportFilename, fileType, "*" + brailleExt, m_xContext);
            if (exportUnoUrl.length() < 1) {
                logger.log(Level.INFO, "User cancelled save as dialog");
                return false;
            }
            String exportUrl = UnoUtils.UnoURLtoURL(exportUnoUrl, m_xContext);

            // Rename Braille file(s)

            if (brailleFiles == null) {
                return false;
            }

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

                List<Volume> volumes = changedSettings.getVolumes();

                if (brailleFiles.length != volumes.size()) {
                    logger.log(Level.INFO, "The number of brailleFiles is not equals to the number of volumes");
                    return false;
                }

                for (int i=0; i<brailleFiles.length; i++) {

                    newFile = new File(newFolder.getAbsolutePath() + fileSeparator + fileName + "." + (i+1) + brailleExt);

                    if (newFile.exists()) { newFile.delete(); }
                    brailleFiles[i].renameTo(newFile);

                }

            } else {

                File newFile = new File(exportUrl);
                if (newFile.exists()) { newFile.delete(); }
                brailleFiles[0].renameTo(newFile);

            }

            logger.exiting("UnoGUI", "exportBraille");

            return true;

        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (MalformedURLException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (FileNotFoundException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (IOException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (ParserConfigurationException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (SAXException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (TransformerConfigurationException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (TransformerException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (InterruptedException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (UnsupportedWidthException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (LiblouisXMLException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (XMLStreamException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (ValidationException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
            return false;
        } finally {
            if (progressBar != null) {
                progressBar.close();
            }
        }
    }

    /**
     * Print the document on a braille embosser.
     *
     * <ul>
     * <li>First, <code>changeSettings</code> is called.</li>
     * <li>A newly created {@link Checker} checks the settings and the flat .odt file, and
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
    public boolean embossBraille () {

        logger.entering("UnoGUI", "embossBraille");

        PEF pef = null;
        HandlePEF handlePef = null;
        String deviceName = null;
        String warning = null;
        Checker checker = null;

        try {

            // Start progress bar
            progressBar.start();
            progressBar.setSteps(3);
            progressBar.setStatus("Analysing document, loading settings...");

            // Export document to flat XML file & load settings
            initialise();

            // Load emboss settings
            settingsIO.loadEmbossSettingsFromOpenOffice(loadedSettings);
            settingsIO.loadEmbossSettingsFromDocument(loadedSettings);
            changedSettings = new Settings(loadedSettings);

            // Create emboss dialog
            EmbossDialog embossDialog = new EmbossDialog(m_xContext, parentWindowPeer, changedSettings, progressBar);

            // Close progress bar
            progressBar.finish(true);
            progressBar.close();

            // Execute emboss dialog
            if (!embossDialog.execute()) {
                logger.log(Level.INFO, "User cancelled emboss dialog");
                return false;
            }

            // Save settings
            settingsIO.saveBrailleSettingsToDocument(changedSettings, loadedSettings);
            settingsIO.saveEmbossSettingsToDocument(changedSettings, loadedSettings);
            settingsIO.saveEmbossSettingsToOpenOffice(changedSettings, loadedSettings);

            // Create LiblouisXML
            LiblouisXML liblouisXML = new LiblouisXML(changedSettings, liblouisLocation);

            // Checker
            checker = new Checker(oooLocale, changedSettings, odtTransformer);

            // Create PEF
            pef = new PEF(odtTransformer, liblouisXML, changedSettings, progressBar, checker, oooLocale);

            // Translate into braille
            if(!pef.makePEF()) {
                progressBar.finish(false);
                return false;
            }

            // Checker
            checker.checkPefFile(pef.getSinglePEF());

            // Show warning
            if (!(warning = checker.getWarning()).equals("")) {
                if (UnoAwtUtils.showYesNoWarningMessageBox(parentWindowPeer, L10N_Warning_MessageBox_Title, warning + "\n\n") == (short) 3) {
                    logger.log(Level.INFO, "User cancelled export on warning");
                    return false;
                }
            }

            // Close progress bar
            progressBar.finish(true);
            progressBar.close();

            // Show post translation dialog
            PreviewDialog preview = new PreviewDialog(m_xContext, pef, changedSettings);
            PostTranslationDialog postTranslationDialog = new PostTranslationDialog(m_xContext, preview);

            if (!postTranslationDialog.execute()) {
                logger.log(Level.INFO, "User cancelled post translation dialog");
                return false;
            }

            // Create EmbossPEF entity
            handlePef = new HandlePEF(pef, changedSettings);

            // Emboss Dialog
            if (changedSettings.getEmbosser()==EmbosserType.INTERPOINT_55) {

                // Create temporary Braille file
                File brailleFile = File.createTempFile(TMP_NAME, ".brf", TMP_DIR);
                brailleFile.deleteOnExit();

                Interpoint55PrintDialog interpoint55PrintDialog = new Interpoint55PrintDialog(m_xContext, xDesktopComponent, changedSettings);

                if (!interpoint55PrintDialog.execute()) {
                    logger.log(Level.INFO, "User cancelled emboss dialog");
                    return false;
                }

                if((brailleFile = handlePef.convertToSingleFile(BrailleFileType.BRF_INTERPOINT)) == null) {
                    return false;
                }

                if (!interpoint55PrintDialog.getPrintToFile()) {

                    // Launch Wprint 55
                    interpoint55PrintDialog.runWPrint55(brailleFile);

                } else {

                    // Export brf file
                    String brailleExt = ".brf";
                    String fileType = "Interpoint 55 BRF";

                    // Show Save As... Dialog:
                    logger.entering("UnoAwtUtils", "showSaveAsDialog");
                    String exportUnoUrl = UnoAwtUtils.showSaveAsDialog(exportFilename, fileType, "*" + brailleExt, m_xContext);
                    if (exportUnoUrl.length() < 1) {
                        logger.log(Level.INFO, "User cancelled save as dialog");
                        return false;
                    }
                    String exportUrl = UnoUtils.UnoURLtoURL(exportUnoUrl, m_xContext);

                    // Rename Braille file
                    File newFile = new File(exportUrl);
                    if (newFile.exists()) { newFile.delete(); }
                    brailleFile.renameTo(newFile);

                }

            } else {

                PrintDialog printDialog = new PrintDialog(m_xContext);

                switch (changedSettings.getEmbosser()) {

                    case INDEX_BASIC_D_V3:
                    case INDEX_EVEREST_D_V3:
                    case INDEX_4X4_PRO_V3:
                    case INDEX_4WAVES_PRO_V3:
                        printDialog.setMaxNumberOfCopies(10000);
                        break;
                    case INDEX_BASIC_S_V2:
                    case INDEX_BASIC_D_V2:
                    case INDEX_EVEREST_D_V2:
                    case INDEX_4X4_PRO_V2:
                        printDialog.setMaxNumberOfCopies(999);
                        break;
                    case IMPACTO_600:
                    case IMPACTO_TEXTO:
                        printDialog.setMaxNumberOfCopies(32767);
                    default:

                }

                if ((deviceName=printDialog.execute()).equals("")) {
                    logger.log(Level.INFO, "User cancelled emboss dialog");
                    return false;
                }

                if (!printDialog.getPrintToFile()) {

                    // Print to device
                    if(!handlePef.embossToDevice(deviceName, printDialog.getNumberOfCopies())) {
                        return false;
                    }

                } else {

                    String brailleExt = ".prn";
                    String fileType = "Print File";

                    // Show Save As... Dialog:
                    logger.entering("UnoAwtUtils", "showSaveAsDialog");
                    String exportUnoUrl = UnoAwtUtils.showSaveAsDialog(exportFilename, fileType, "*" + brailleExt, m_xContext);
                    if (exportUnoUrl.length() < 1) {
                        logger.log(Level.INFO, "User cancelled save as dialog");
                        return false;
                    }
                    if (!exportUnoUrl.endsWith(brailleExt)) {
                        exportUnoUrl = exportUnoUrl.concat(brailleExt);
                    }
                    String exportUrl = UnoUtils.UnoURLtoURL(exportUnoUrl, m_xContext);

                    // Print to File
                    if(!handlePef.embossToFile(new File(exportUrl), printDialog.getNumberOfCopies())) {
                        logger.log(Level.INFO, "Emboss to file failed");
                        return false;
                    }
                }
            }

            return true;

        } catch (PrintException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (EmbosserFactoryException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (MalformedURLException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (IOException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (ParserConfigurationException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (SAXException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (TransformerConfigurationException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (TransformerException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (InterruptedException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (UnsupportedWidthException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (LiblouisXMLException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (ValidationException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
            return false;
        } finally {
            if (progressBar != null) {
                progressBar.close();
            }
        }
    }

    public boolean sixKeyEntryMode (){

        logger.entering("UnoGUI", "sixKeyEntryMode");

        try {

            XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDesktopComponent);
            XController xController = xModel.getCurrentController();
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                           XTextViewCursorSupplier.class, xController);
            XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();

            SixKeyEntryDialog dialog = new SixKeyEntryDialog(m_xContext, m_xFrame, xViewCursor);
            dialog.execute();

            logger.exiting("UnoGUI", "sixKeyEntryMode");

            return true;

        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
            return false;
        }
    }

    public boolean insertBraille () {

        logger.entering("UnoGUI", "insertBraille");

        try {

            XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDesktopComponent);
            XController xController = xModel.getCurrentController();
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                           XTextViewCursorSupplier.class, xController);
            XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();

            // Create dialog

            InsertDialog dialog = new InsertDialog(m_xContext, xViewCursor);
            dialog.execute();

            logger.exiting("UnoGUI", "insertBraille");

            return true;

        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
            return false;
        }
    }

    /**
     * Handling of an unexpected exception.
     * A message box is shown with a reference to the log file.
     *
     * @param   ex     The exception
     */
    private void handleUnexpectedException (Exception ex) {

        logger.log(Level.SEVERE, null, ex);
        UnoAwtUtils.showErrorMessageBox(parentWindowPeer, L10N_Exception_MessageBox_Title,
                L10N_Unexpected_Exception_Message + ": " + logFile.getAbsolutePath());

    }

    /**
     * Flush and close the logfile handler.
     */
    public void clean () {
        if (progressBar != null) {
            progressBar.close();
        }
        if (odtTransformer != null) {
            try {
                odtTransformer.close();
            } catch (IOException e) {
            }
        }
        if (fh != null) {
            fh.flush();
            fh.close();
        }
    }
}
