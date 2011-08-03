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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Properties;
import java.io.File;
import java.io.FilenameFilter;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ui.dialogs.XFolderPicker;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;

import java.io.IOException;

import be.docarch.odt2braille.Constants;
import be_interpoint.Interpoint55Embosser;

/**
 * Show an OpenOffice.org dialog window for embossing with the Interpoint55 printer. With this dialog, a user can choose
 * <ul>
 * <li>the location for saving the .brf file,</li>
 * <li>the location of the wprint55 program folder,</li>
 * <li>a wprint55 configuration file (.ini), and</li>
 * <li>whether or not this configuration file should be overwritten by the new settings.</li>
 * </ul>
 * The user can only press OK if a save location is set, wprint55 is found and a configuration file is selected.
 * Initial settings are loaded from the OpenOffice.org extension file (.oxt) and saved afterwards.
 * The .ini file is overwritten if this option is checked.
 * This class also has public methods for getting the .brf file location and executing the wprint55 program.
 *
 * @author      Bert Frees
 */
public class Interpoint55PrintDialog implements XActionListener,
                                                XItemListener {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N = Constants.OOO_L10N_PATH;

    private Interpoint55Embosser embosser = null;
  //private SettingsIO settingsIO = null;

    private XComponentContext xContext = null;

    private XDialog xDialog = null;
    private XControlContainer xControlContainer = null;
    private XComponent xComponent = null;

    private XFolderPicker xFolderPicker = null;

    private XTextComponent wp55Field = null;
    private XListBox iniListBox = null;
    private XCheckBox overWriteCheckBox = null;
    private XCheckBox printToFileCheckBox = null;
    private XButton okButton = null;
    private XButton cancelButton = null;
    private XButton searchWp55Button = null;

    private XPropertySet windowProperties = null;
    private XPropertySet okButtonProperties = null;
    private XPropertySet iniListBoxProperties = null;
    private XPropertySet wp55FieldProperties = null;
    private XPropertySet searchWp55ButtonProperties = null;
    private XPropertySet overWriteCheckBoxProperties = null;

    private static String _wp55Field = "TextField2";
    private static String _iniListBox = "ListBox1";
    private static String _overWriteCheckBox = "CheckBox1";
    private static String _printToFileCheckBox = "CheckBox2";
    private static String _okButton = "CommandButton1";
    private static String _cancelButton = "CommandButton2";
    private static String _searchWp55Button = "CommandButton4";

    private static String _wp55Label = "Label2";
    private static String _iniLabel = "Label3";
    private static String _overWriteLabel = "Label4";
    private static String _printToFileLabel = "Label5";

    private String L10N_wp55Label = null;
    private String L10N_iniLabel = null;
    private String L10N_overWriteLabel = null;
    private String L10N_printToFileLabel = null;
    private String L10N_okButton = null;
    private String L10N_cancelButton = null;
    private String L10N_searchWp55Button = null;
    private String L10N_windowTitle = null;

    private String wp55FolderUrl = "C:\\Program Files\\Interpoint\\wprint55";
    private File wp55File = new File(wp55FolderUrl + System.getProperty("file.separator") + "WP55.exe");
    private File iniFile = null;
    private String iniFileName = null;
    private boolean overWriteIniFile = false;
    private boolean printToFile = false;
    

    /**
     * The dialog is created from an OpenOffice.org .xdl file.
     *
     * @param settings  The braille settings.
     * @param xContext
     */
    public Interpoint55PrintDialog(XComponentContext xContext,
                                   XComponent xDesktopComponent,
                                   Interpoint55Embosser embosser)
                            throws com.sun.star.uno.Exception {

        logger.entering("Interpoint55PrintDialog", "<init>");

        this.embosser = embosser;
        this.xContext = xContext;

      //settingsIO = new SettingsIO(xContext, xDesktopComponent);

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/Interpoint55PrintDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        XMultiComponentFactory xMCF = xContext.getServiceManager();

        Locale oooLocale;
        try { oooLocale = UnoUtils.getUILocale(xContext); } catch (Exception e) {
              oooLocale = Locale.ENGLISH; }

        ResourceBundle bundle = ResourceBundle.getBundle(L10N, oooLocale);

        L10N_wp55Label = bundle.getString("wp55Label") + ":";
        L10N_iniLabel = bundle.getString("iniLabel") + ":";
        L10N_overWriteLabel = bundle.getString("overWriteLabel");
        L10N_printToFileLabel = bundle.getString("printToFileLabel");
        L10N_searchWp55Button = "...";
        L10N_okButton = bundle.getString("embossButton");
        L10N_cancelButton = bundle.getString("cancelButton");
        L10N_windowTitle = bundle.getString("interpoint55EmbossDialogTitle");

        xFolderPicker = (XFolderPicker) UnoRuntime.queryInterface(XFolderPicker.class,
                xMCF.createInstanceWithContext("com.sun.star.ui.dialogs.FolderPicker", xContext));
        XPropertySet xPropertySet = (XPropertySet) com.sun.star.uno.UnoRuntime.queryInterface(XPropertySet.class,
                xMCF.createInstanceWithContext("com.sun.star.util.PathSettings",xContext));
        String sTemplateUrl = (String) xPropertySet.getPropertyValue("Work_writable");
        xFolderPicker.setDisplayDirectory(sTemplateUrl);

        xDialog = xDialogProvider.createDialog(dialogUrl);
        xControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, xDialog);
        xComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, xDialog);

        wp55Field = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                            xControlContainer.getControl(_wp55Field));
        iniListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                            xControlContainer.getControl(_iniListBox));
        overWriteCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                            xControlContainer.getControl(_overWriteCheckBox));
        printToFileCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                            xControlContainer.getControl(_printToFileCheckBox));
        okButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                            xControlContainer.getControl(_okButton));
        cancelButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                            xControlContainer.getControl(_cancelButton));
        searchWp55Button = (XButton) UnoRuntime.queryInterface(XButton.class,
                            xControlContainer.getControl(_searchWp55Button));

        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, xDialog)).getModel());
        okButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, okButton)).getModel());
        iniListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, iniListBox)).getModel());
        wp55FieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, wp55Field)).getModel());
        searchWp55ButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, searchWp55Button)).getModel());
        overWriteCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, overWriteCheckBox)).getModel());

    }

    /**
     * An <code>XActionListener</code> (= the dialog itself) is added to the dialog items.
     *
     */
    private void addActionListeners() {

        searchWp55Button.addActionListener(this);
        iniListBox.addActionListener(this);
        printToFileCheckBox.addItemListener(this);

    }

    /**
     * Initial settings are loaded and the dialog is executed. It is disposed when the user presses OK or Cancel.
     * If the user pressed OK, the settings are saved and the .ini file is overwritten if this option was checked.
     *
     * @return          <code>true</code> if the user pressed OK;
     *                  <code>false</code> if the user pressed Cancel.
     */
    public boolean execute() throws com.sun.star.uno.Exception,
                                    IOException {

        logger.entering("Interpoint55PrintDialog", "execute");

        setLabels();
        loadEmbossSettings();
        setDialogValues();
        addActionListeners();
        short ret = xDialog.execute();
        getDialogValues();
        xComponent.dispose();
        if (ret == ((short) PushButtonType.OK_value)) {
            saveEmbossSettings();
            if (overWriteIniFile && !printToFile) {
                overWriteIniFile();
            }
            logger.exiting("Interpoint55PrintDialog", "execute");
            return true;
        } else {
            logger.exiting("Interpoint55PrintDialog", "execute");
            return false;
        }

    }

    public boolean getPrintToFile() {
        return printToFile;
    }

    /**
     * Run wprint55.
     *
     * @return          <code>true</code>
     */
    public boolean runWPrint55(File brfFile)
                        throws IOException {

        logger.entering("Interpoint55PrintDialog", "runWPrint55");

        if (checkWp55FolderUrl() && brfFile.exists()) {

            Runtime runtime = Runtime.getRuntime();
            String exec_cmd[] = {"\"" + wp55File.getPath() + "\"", "\"" + brfFile.getPath() + "\"", "/config:" + iniFileName};

            String message = "wp55:";
            for (int i=0;i<exec_cmd.length;i++) {
                message += "\n          " + exec_cmd[i];
            }

            runtime.exec(exec_cmd);

            logger.log(Level.INFO, message);
            
        } else {
            logger.log(Level.INFO, wp55File.getPath() + " is not executable");
        }

        logger.exiting("Interpoint55PrintDialog", "runWPrint55");

        return true;

    }

    /**
     * Set the dialog labels.
     *
     */
    private void setLabels() throws com.sun.star.uno.Exception {

        windowProperties.setPropertyValue("Title", L10N_windowTitle);

        XFixedText xFixedText = null;

        okButton.setLabel(L10N_okButton);
        cancelButton.setLabel(L10N_cancelButton);
        searchWp55Button.setLabel(L10N_searchWp55Button);

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                        xControlContainer.getControl(_printToFileLabel));
        xFixedText.setText(L10N_printToFileLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                xControlContainer.getControl(_wp55Label));
        xFixedText.setText(L10N_wp55Label);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                xControlContainer.getControl(_iniLabel));
        xFixedText.setText(L10N_iniLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                xControlContainer.getControl(_overWriteLabel));
        xFixedText.setText(L10N_overWriteLabel);

    }

    /**
     * Set the initial dialog values and field properties.
     *
     */
    private void setDialogValues() throws com.sun.star.uno.Exception {

        okButtonProperties.setPropertyValue("Enabled", false);
        wp55Field.setEditable(false);
        overWriteCheckBox.setState((short)(overWriteIniFile?1:0));

        if (System.getProperty("os.name").toLowerCase().contains("mac os")) {
            printToFile = true;
            XPropertySet printToFileCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, printToFileCheckBox)).getModel());
            printToFileCheckBoxProperties.setPropertyValue("Enabled", false);
        }

        printToFileCheckBox.setState((short)(printToFile?1:0));
        iniListBoxProperties.setPropertyValue("Enabled", !printToFile);
        wp55FieldProperties.setPropertyValue("Enabled", !printToFile);
        searchWp55ButtonProperties.setPropertyValue("Enabled", !printToFile);
        overWriteCheckBoxProperties.setPropertyValue("Enabled", !printToFile);
        updateWp55Field();
        updateIniListBox();
        updateOKButton();

    }

    /**
     * Read the dialog values set by the user (that have not been previously read).
     *
     */
    private void getDialogValues() {
        overWriteIniFile = (overWriteCheckBox.getState()==(short)1);
    }

    /**
     * Load the initial settings from the OpenOffice.org extension file (.oxt).
     *
     */
    private void loadEmbossSettings() throws IOException {

        
      //Properties interpoint55Settings = settingsIO.loadSettingsFromOpenOffice("interpoint55");
        Properties interpoint55Settings = new Properties();

        String s = null;

        if ((s = interpoint55Settings.getProperty("overWriteIni")) != null) {
            overWriteIniFile = s.equals("1");
        }
        if ((s = interpoint55Settings.getProperty("iniFile")) != null) {
            iniFileName = s;
        }
        if ((s = interpoint55Settings.getProperty("wp55Folder")) != null) {
            wp55FolderUrl = s;
            wp55File = new File(wp55FolderUrl + System.getProperty("file.separator") + "WP55.exe");
        }
    }

    /**
     * Save settings to the OpenOffice.org extension file (.oxt).
     *
     */
    private void saveEmbossSettings() throws IOException {

        if (!printToFile) {

            Properties interpoint55Settings = new Properties();

            interpoint55Settings.setProperty("wp55Folder",   wp55FolderUrl);
            interpoint55Settings.setProperty("iniFile",      iniFileName);
            interpoint55Settings.setProperty("overWriteIni", overWriteIniFile?"1":"0");

          //settingsIO.saveSettingsToOpenOffice("interpoint55", interpoint55Settings);

        }
    }

    /**
     *
     * @return          <code>true</code> if the specified wprint55 program is executable.
     */
    private boolean checkWp55FolderUrl() {
        return wp55File.exists();
      //return wp55File.canExecute();
    }

    /**
     * Update the wprint55 field.
     *
     */
    private void updateWp55Field() {
        wp55Field.setText(wp55FolderUrl);
    }

    /**
     * Update the list of available .ini files. These are the files in the 'wprint55/Config' folder with extension '.ini'.
     *
     */
    private void updateIniListBox() {

        iniListBox.removeItems((short)0, Short.MAX_VALUE);
        iniFile = null;
        short selectPos = (short)0;

        if (checkWp55FolderUrl()) {

            iniListBox.removeActionListener(this);

            File iniFolder = new File(wp55FolderUrl + System.getProperty("file.separator") + "Config");
            File[] iniFiles = null;

            if (iniFolder.isDirectory()) {
                iniFiles = iniFolder.listFiles(new IniFilter());
            }
            for (int i=0;i<iniFiles.length;i++) {
                iniListBox.addItem(iniFiles[i].getName(),(short) i);
                if (iniFiles[i].getName().equals(iniFileName)) {
                    selectPos = (short)i;
                }
            }
            if (iniFiles.length > 0) {
                iniListBox.selectItemPos(selectPos, true);
                iniFileName = iniListBox.getSelectedItem();
                iniFile = new File(wp55FolderUrl + System.getProperty("file.separator") + "Config" +
                                                   System.getProperty("file.separator") + iniFileName);
            }

            iniListBox.addActionListener(this);
        }
    }

    /**
     * Update the state of the OK button (enabled or disabled).
     * The user can only press OK if a save location is set, wprint55 is found and a configuration file is selected.
     *
     */
    private void updateOKButton() throws com.sun.star.uno.Exception {

        okButtonProperties.setPropertyValue("Enabled", printToFile || (checkWp55FolderUrl() && iniFile != null));

    }

    /**
     * Overwrite some settings of the the selected wprint55 configuration file
     * with the corresponding odt2braille settings.
     *
     * @return          <code>true</code>
     */
    private boolean overWriteIniFile() throws IOException {

        logger.entering("Interpoint55PrintDialog", "overWriteIniFile");

        embosser.saveConfigurationFile(iniFile);

        logger.exiting("Interpoint55PrintDialog", "overWriteIniFile");

        return true;
        
    }

    /**
     * Is called when a listbox is changed by the user or a button is pressed.
     * When the <code>searchBrfButton</code> or <code>searchWp55Button</code> button is pressed,
     * the appropriate "Save-as-dialog" or "File-picker" is executed.
     * Afterwards, variables and dialog fields are updated.
     *
     * @param actionEvent
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        try {

            Object source = actionEvent.Source;

            if (source.equals(searchWp55Button)) {

                XComponent xFolderPickerComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xFolderPicker);
                XExecutableDialog xExecutable = (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, xFolderPicker);
                short nResult = xExecutable.execute();
                if (nResult == com.sun.star.ui.dialogs.ExecutableDialogResults.OK){
                    wp55File = new File(UnoUtils.UnoURLtoURL(xFolderPicker.getDirectory(), xContext)
                            + System.getProperty("file.separator") + "WP55.exe");
                    wp55FolderUrl = wp55File.getParent();

                    updateWp55Field();
                    updateIniListBox();
                    updateOKButton();

                }

                if (xFolderPickerComponent != null){
                    xFolderPickerComponent.dispose();
                }

            } else if (source.equals(iniListBox)) {

                iniFileName = iniListBox.getSelectedItem();
                iniFile = new File(wp55FolderUrl + System.getProperty("file.separator") + "Config" +
                                                   System.getProperty("file.separator") + iniFileName);
                updateOKButton();

            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {

        Object source = itemEvent.Source;

        try {

            if (source.equals(printToFileCheckBox)) {

                printToFile = (printToFileCheckBox.getState() == (short) 1);

                iniListBoxProperties.setPropertyValue("Enabled", !printToFile);
                wp55FieldProperties.setPropertyValue("Enabled", !printToFile);
                searchWp55ButtonProperties.setPropertyValue("Enabled", !printToFile);
                overWriteCheckBoxProperties.setPropertyValue("Enabled", !printToFile);

                updateOKButton();

            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param event
     */
    @Override
    public void disposing(EventObject event) {}

    /**
     * Implementation of <code>FilenameFilter</code>.
     * Is used to filter an array of files. Only those files that end with '.ini' are accepted.
     *
     * @see         FilenameFilter
     * @author      Bert Frees
     */
    private static class IniFilter implements FilenameFilter {

        private static final String extension = "ini";

        /**
         * Creates a new <code>IniFilter</code> instance.
         *
         */
        public IniFilter() {}

        /**
         * @param   directory   Not used
         * @param   filename    The name of a file.
         * @return          <code>true</code> if the filename ends with '.ini'
         */
        public boolean accept(File directory,
                              String filename) {

            return filename.endsWith('.' + extension);
        }
    }
}