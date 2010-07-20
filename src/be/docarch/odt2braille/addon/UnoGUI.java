/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille.addon;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XStorable;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.frame.XFrame;
import com.sun.star.uno.XComponentContext;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.frame.XModel;
import com.sun.star.awt.XWindow;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XController;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.print.PrintException;

import be.docarch.odt2braille.Odt2Braille;
import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.HandlePEF;
import be.docarch.odt2braille.Settings.BrailleFileType;
import be.docarch.odt2braille.XPathUtils;
import be.docarch.odt2braille.Checker;
import be.docarch.odt2braille.Odt2BrailleNamespaceContext;
import com.versusoft.packages.jodl.OdtUtils;
import org_pef_text.pef2text.EmbosserFactory.EmbosserType;

import org_pef_text.pef2text.UnsupportedWidthException;
import org_pef_text.pef2text.EmbosserFactoryException;
import be.docarch.odt2braille.LiblouisException;


/**
 * The <code>changeSettings</code>, <code>exportBraille</code> and <code>embossBraille</code> methods
 * roughly correspond with the three main tasks that be called from the 'Braille' menu.
 * The main control flow of these tasks is found here.
 *
 * @author  Bert Frees
 */
public class UnoGUI {

    private static final String LOG_FILE = "odt2braille.log";
    private static final String TMP_NAME = "odt2braille";
    private static final String ODT_EXT = ".odt";
    private static final String XML_EXT = ".xml";
    private static final String PEF_EXT = ".pef";
    private static final String FLAT_XML_FILTER_NAME = "writer8";
    private static final Logger logger = Logger.getLogger("odt2braille");

    private static String L10N_Default_Export_Filename = null;
    private static String L10N_Warning_MessageBox_Title = null;
    private static String L10N_Exception_MessageBox_Title = null;
    private static String L10N_Unexpected_Exception_Message = null;

    private XComponentContext m_xContext = null;
    private XFrame m_xFrame = null;
    private XModel xDoc = null;
    private XWindow parentWindow = null;
    private XWindowPeer parentWindowPeer = null;

    private Handler fh = null;
    private File logFile = null;

    private File odtFile = null;
    private File flatOdtFile = null;

    private String brailleExt = null;
    private String odtUrl = null;
    private String odtUnoUrl = null;
    private String flatOdtUrl = null;
    private String liblouisDirUrl = null;

    private SettingsIO settingsIO = null;

    private Settings defaultSettings = null;
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

        this.m_xContext = m_xContext;
        this.m_xFrame = m_xFrame;

        try {

            // Configuring logger
            logFile = File.createTempFile(LOG_FILE, null);
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
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
        }

    }

    /**
     * The OpenOffice.org Writer document is exported as an .odt file and converted to a "flat XML" .odt file
     * (a single file that is the concatenation of all XML files in a normal .odt file).
     * 
     */
    private void initialize() {

        logger.entering("UnoGUI", "initialize");

        try {

            L10N_Default_Export_Filename = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("defaultExportFilename");
            L10N_Warning_MessageBox_Title = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("warningMessageBoxTitle");
            L10N_Exception_MessageBox_Title = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("exceptionMessageBoxTitle");
            L10N_Unexpected_Exception_Message = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("unexpectedExceptionMessage");

            // Query Uno Object
            xDoc = (XModel) UnoRuntime.queryInterface(XModel.class, m_xFrame.getController().getModel());
            parentWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
            parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, parentWindow);

            // Export in ODT Format
            odtFile = File.createTempFile(TMP_NAME,ODT_EXT);
            odtFile.deleteOnExit();
            odtUrl = odtFile.getAbsolutePath();
            odtUnoUrl = UnoUtils.createUnoFileURL(odtUrl, m_xContext);
            PropertyValue[] conversionProperties = new PropertyValue[1];
            conversionProperties[0] = new PropertyValue();
            conversionProperties[0].Name = "FilterName";
            conversionProperties[0].Value = FLAT_XML_FILTER_NAME; //Daisy DTBook OpenDocument XML

            XStorable storable = (XStorable) UnoRuntime.queryInterface(
                    XStorable.class, m_xFrame.getController().getModel());

            storable.storeToURL(odtUnoUrl, conversionProperties);

            // Convert ODT to flat XML file
            flatOdtFile = File.createTempFile(TMP_NAME,XML_EXT);
            flatOdtFile.deleteOnExit();
            flatOdtUrl = flatOdtFile.getAbsolutePath();
            OdtUtils odtutil = new OdtUtils();
            odtutil.open(odtUrl);
            odtutil.saveXML(flatOdtUrl);

            // Locale
            odtLocale = new Locale(XPathUtils.evaluateString(flatOdtFile.toURL().openStream(),
                    "/office:document/office:styles/style:default-style/style:text-properties/@fo:language",
                    new Odt2BrailleNamespaceContext()).toLowerCase());

            // Create new settingsIO
            settingsIO = new SettingsIO(m_xContext);

            // Create default settings
            defaultSettings = new Settings(flatOdtFile, odtLocale);

            // Set liblouis directory
            liblouisDirUrl = new File(UnoUtils.UnoURLtoURL(PackageInformationProvider.get(m_xContext)
                                .getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn-windows_x86")
                                + "/liblouis/", m_xContext)).getAbsolutePath()
                                + System.getProperty("file.separator");

            logger.exiting("UnoGUI", "initialize");

        } catch (IOException ex) {
            handleUnexpectedException(ex);
        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
        }

    }

    /**
     * Overwrite the default settings with settings loaded from the OpenOffice.org Writer document,
     * execute the "Braille Settings" dialog that enables users to change these settings
     * and store the new settings if the user confirms.
     *
     * @param   mode    {@link SettingsDialog#SAVE_SETTINGS}, {@link SettingsDialog#EXPORT} or {@link SettingsDialog#EMBOSS},
     *                  depending on how the dialog was called (through which menu item).
     * @return          <code>true</code> if the new settings were succesfully saved.
     */
    public boolean changeSettings(short mode) {

        logger.entering("UnoGUI", "changeSettings");

        try {

            initialize();

            SettingsDialog dialog;

            loadedSettings = settingsIO.loadSettingsFromDocument(defaultSettings);
            changedSettings = new Settings(loadedSettings);

            // Create Configuration Dialog
            dialog = new SettingsDialog(m_xContext, changedSettings, mode);

            // Raise dialog
            if (!dialog.execute()) {
                logger.log(Level.INFO, "User cancelled settings dialog");
                return false;
            }

            settingsIO.saveSettingsToDocument(changedSettings, loadedSettings);

            logger.exiting("UnoGUI", "changeSettings");

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
    public boolean exportBraille() {

        logger.entering("UnoGUI", "exportBraille");

        Odt2Braille odt2braille = null;
        HandlePEF handlePef = null;
        String pefUrl = null;
        String brailleUrl = null;
        String brailleUnoUrl = null;
        String fileType = null;
        String warning = null;
        Checker checker = null;
        ProgressBar progressBar = new ProgressBar(m_xFrame);

        try {

            // Change Settings
            if(!changeSettings(SettingsDialog.EXPORT)) {
                return false;
            }

            // Checker
            checker = new Checker(oooLocale, changedSettings);
            checker.checkSettings();

            // Show first warning
            checker.checkFlatOdtFile(flatOdtFile);
            if (!(warning = checker.getFirstWarning()).equals("")) {
                if (UnoAwtUtils.showYesNoWarningMessageBox(parentWindowPeer, L10N_Warning_MessageBox_Title, warning + "\n\n") == (short) 3) {
                    logger.log(Level.INFO, "User cancelled export on first warning");
                    return false;
                }
            }

            if (changedSettings.isGenericOrSpecific()) {
                brailleExt = "." + changedSettings.getGenericBraille().name().toLowerCase();
                switch (changedSettings.getGenericBraille()) {
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

            } else {
                switch (changedSettings.getEmbosser()) {
                    case INTERPOINT_55:
                        brailleExt = ".brf";
                        fileType = "Interpoint 55 Braille Formatted";
                        break;
                    default:
                        brailleExt = ".txt";
                        fileType = "Specific Embosser File";

                }
            }
                
            // Raise Save As... Dialog:
            logger.entering("UnoAwtUtils", "showSaveAsDialog");

            brailleUnoUrl = UnoAwtUtils.showSaveAsDialog(L10N_Default_Export_Filename, fileType, "*" + brailleExt, m_xContext);
            if (brailleUnoUrl.length() < 1) {
                return false;
            }
            if (!brailleUnoUrl.endsWith(brailleExt)) {
                brailleUnoUrl = brailleUnoUrl.concat(brailleExt);
            }
            brailleUrl = UnoUtils.UnoURLtoURL(brailleUnoUrl, m_xContext);
                
            // Create temporary PEF
            File pefFile = File.createTempFile(TMP_NAME,PEF_EXT);
            pefFile.deleteOnExit();
            pefUrl = pefFile.getAbsolutePath();

            // Initialize progress bar
            progressBar.init();

            // Create odt2braille with settings from export dialog
            odt2braille = new Odt2Braille(flatOdtFile, liblouisDirUrl, changedSettings, progressBar, checker, odtLocale, oooLocale);

            // Translate into braille
            if(!odt2braille.makePEF(pefUrl)) {
                progressBar.ready();
                return false;
            }

            // Show second warning
            if (!(warning = checker.getSecondWarning()).equals("")) {
                if (UnoAwtUtils.showYesNoWarningMessageBox(parentWindowPeer, L10N_Warning_MessageBox_Title, warning + "\n\n") == (short) 3) {
                    logger.log(Level.INFO, "User cancelled export on second warning");
                    return false;
                }
            }

            if (changedSettings.getGenericBraille() == BrailleFileType.PEF)  {

                // Rename PEF file
                File newFile = new File(brailleUrl);
                if (newFile.exists()) { newFile.delete(); }
                pefFile.renameTo(newFile);

            } else {

                // Create HandlePEF entity
                handlePef = new HandlePEF(pefUrl, changedSettings);
                
                if (changedSettings.isGenericOrSpecific()) {

                    // Convert to Braille File
                    if(!handlePef.convertToFile(changedSettings.getGenericBraille(), new File(brailleUrl))) {
                        return false;
                    }

                } else {

                    if (changedSettings.getEmbosser()==EmbosserType.INTERPOINT_55) {
                        if(!handlePef.convertToFile(BrailleFileType.BRF_INTERPOINT, new File(brailleUrl))) {
                            return false;
                        }
                    } else {
                        // Emboss to File
                        if(!handlePef.embossToFile(new File(brailleUrl))) {
                            return false;
                        }
                    }
                }
            }

            logger.exiting("UnoGUI", "exportBraille");

            return true;

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
        } catch (EmbosserFactoryException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (UnsupportedWidthException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (LiblouisException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
            return false;
        } finally {
            progressBar.ready();
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
    public boolean embossBraille(){

        logger.entering("UnoGUI", "embossBraille");

        HandlePEF handlePef = null;
        Odt2Braille odt2braille = null;
        File pefFile = null;
        String pefUrl = null;
        String deviceName = null;
        String warning = null;
        Checker checker = null;
        ProgressBar progressBar = new ProgressBar(m_xFrame);
        
        try {
            
            // Change Settings
            if(!changeSettings(SettingsDialog.EMBOSS)) {
                return false;
            }

            // Checker
            checker = new Checker(oooLocale, changedSettings);
            checker.checkSettings();

            // Show first checker warning
            checker.checkFlatOdtFile(flatOdtFile);
            if (!(warning = checker.getFirstWarning()).equals("")) {
                if (UnoAwtUtils.showYesNoWarningMessageBox(parentWindowPeer, L10N_Warning_MessageBox_Title, warning + "\n\n") == (short) 3) {
                    logger.log(Level.INFO, "User cancelled export on first warning");
                    return false;
                }
            }

            // Create temporary PEF
            pefFile = File.createTempFile(TMP_NAME,PEF_EXT);
            pefFile.deleteOnExit();
            pefUrl = pefFile.getAbsolutePath();

            // Emboss Dialog
            if (changedSettings.getEmbosser()==EmbosserType.INTERPOINT_55) {

                Interpoint55EmbossDialog dialog = new Interpoint55EmbossDialog(m_xContext, changedSettings);

                if (!dialog.execute()) {
                    logger.log(Level.INFO, "User cancelled emboss dialog");
                    return false;
                }

                // Initialize progress bar
                progressBar.init();

                // Create odt2braille with settings from export dialog
                odt2braille = new Odt2Braille(flatOdtFile, liblouisDirUrl, changedSettings, progressBar, checker, odtLocale, oooLocale);

                // Translate into braille
                if(!odt2braille.makePEF(pefUrl)) {
                    return false;
                }

                // Show second checker warning
                if (!(warning = checker.getSecondWarning()).equals("")) {
                    if (UnoAwtUtils.showYesNoWarningMessageBox(parentWindowPeer, L10N_Warning_MessageBox_Title, warning + "\n\n") == (short) 3) {
                        logger.log(Level.INFO, "User cancelled export on second warning");
                        return false;
                    }
                }

                // Create EmbossPEF entity
                handlePef = new HandlePEF(pefUrl, changedSettings);

                if(!handlePef.convertToFile(BrailleFileType.BRF_INTERPOINT, dialog.getBrfFile())) {
                    return false;
                }

                dialog.runWPrint55();

            } else {

                EmbossDialog dialog = new EmbossDialog(m_xContext);

                if ((deviceName=dialog.execute()).equals("")) {
                    logger.log(Level.INFO, "User cancelled emboss dialog");
                    return false;
                }

                // Initialize progress bar
                progressBar.init();

                // Create odt2braille with settings from export dialog
                odt2braille = new Odt2Braille(flatOdtFile, liblouisDirUrl, changedSettings, progressBar, checker, odtLocale, oooLocale);

                // Translate into braille
                if(!odt2braille.makePEF(pefUrl)) {
                    return false;
                }

                // Show second checker warning
                if (!(warning = checker.getSecondWarning()).equals("")) {
                    if (UnoAwtUtils.showYesNoWarningMessageBox(parentWindowPeer, L10N_Warning_MessageBox_Title, warning + "\n\n") == (short) 3) {
                        logger.log(Level.INFO, "User cancelled export on second warning");
                        return false;
                    }
                }

                // Create EmbossPEF entity
                handlePef = new HandlePEF(pefUrl, changedSettings);

                if(!handlePef.embossToDevice(deviceName)) {
                    return false;
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
        } catch (LiblouisException ex) {
            handleUnexpectedException(ex);
            return false;
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
            return false;
        } finally {
            progressBar.ready();
        }

    }

    public boolean sixKeyEntryMode(){
    
        logger.entering("UnoGUI", "sixKeyEntryMode");

        try {

            XMultiComponentFactory xMCF =(XMultiComponentFactory) UnoRuntime.queryInterface(
                                          XMultiComponentFactory.class, m_xContext.getServiceManager());
            Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
            XComponent xDesktopComponent = (XComponent) xDesktop.getCurrentComponent();
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

    public boolean insertBraille() {
        
        logger.entering("UnoGUI", "insertBraille");

        try {

            XMultiComponentFactory xMCF =(XMultiComponentFactory) UnoRuntime.queryInterface(
                                          XMultiComponentFactory.class,m_xContext.getServiceManager());
            Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
            XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
            XComponent xDesktopComponent = (XComponent) xDesktop.getCurrentComponent();
            XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDesktopComponent);
            XController xController = xModel.getCurrentController();
            XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                           XTextViewCursorSupplier.class, xController);
            XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
            XText xText = xViewCursor.getText();
            XTextCursor xTextCursor = xText.createTextCursorByRange(xViewCursor.getStart());

            // Create dialog

            InsertDialog dialog = new InsertDialog(m_xContext);

            if (!dialog.execute()) {
                logger.log(Level.INFO, "User cancelled insert braille dialog");
                return false;
            }

            // Insert Braille in document

            xText.insertString(xTextCursor.getEnd(), dialog.getBrailleCharacters(), false);

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
    private void handleUnexpectedException(Exception ex) {

        logger.log(Level.SEVERE, null, ex);
        UnoAwtUtils.showErrorMessageBox(parentWindowPeer, L10N_Exception_MessageBox_Title,
                L10N_Unexpected_Exception_Message + ": " + logFile.getAbsolutePath());

    }

    /**
     * Flush and close the logfile handler.
     */
    public void flushLogger() {
        if (fh != null) {
            fh.flush();
            fh.close();
        }
    }
}
