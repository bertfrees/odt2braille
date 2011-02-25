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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.TreeMap;
import java.util.ArrayList;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.EventObject;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.BrailleFileExporter.BrailleFileType;
import org_pef_text.TableFactory.TableType;


/**
 *
 * @author   Bert Frees
 */
public class ExportDialog implements XItemListener,
                                     XActionListener {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N_BUNDLE = Constants.OOO_L10N_PATH;

    private Settings settings = null;
    private XComponentContext xContext = null;
    private Locale oooLocale = null;
    private ProgressBar progressbar = null;
    private SettingsDialog settingsDialog = null;

    private ArrayList<BrailleFileType> brailleFileTypes = null;
    private ArrayList<TableType> tableTypes = null;

    private XDialog dialog = null;
    private XControlContainer dialogControlContainer = null;
    private XComponent dialogComponent = null;
    private XControl dialogControl = null;

    private XButton okButton = null;
    private XButton cancelButton = null;
    private XButton settingsButton = null;

    private static String _okButton = "CommandButton2";
    private static String _cancelButton = "CommandButton1";
    private static String _settingsButton = "CommandButton3";

    private String L10N_okButton = null;
    private String L10N_cancelButton = null;
    private String L10N_settingsButton = null;

    private XPropertySet okButtonProperties = null;
    private XPropertySet windowProperties = null;
    private String L10N_windowTitle = null;
    
    private XListBox brailleFileListBox = null;
    private XListBox tableListBox = null;
    private XCheckBox duplexCheckBox = null;
    private XCheckBox eightDotsCheckBox = null;
    private XNumericField numberOfCellsPerLineField = null;
    private XNumericField numberOfLinesPerPageField = null;
    private XCheckBox multipleFilesCheckBox = null;

    private XPropertySet tableListBoxProperties = null;
    private XPropertySet duplexCheckBoxProperties = null;
    private XPropertySet eightDotsCheckBoxProperties = null;
    private XPropertySet multipleFilesCheckBoxProperties = null;

    private static String _brailleFileListBox = "ListBox2";
    private static String _tableListBox = "ListBox3";
    private static String _duplexCheckBox = "CheckBox1";
    private static String _eightDotsCheckBox = "CheckBox2";
    private static String _numberOfCellsPerLineField = "NumericField3";
    private static String _numberOfLinesPerPageField = "NumericField4";
    private static String _multipleFilesCheckBox = "CheckBox3";

    private static String _brailleFileLabel = "Label2";
    private static String _tableLabel = "Label3";
    private static String _duplexLabel = "Label8";
    private static String _eightDotsLabel = "Label1";
    private static String _numberOfCellsPerLineLabel = "Label6";
    private static String _numberOfLinesPerPageLabel = "Label7";
    private static String _multipleFilesLabel = "Label4";

    private String L10N_brailleFileLabel = null;
    private String L10N_tableLabel = null;
    private String L10N_duplexLabel = null;
    private String L10N_eightDotsLabel = null;
    private String L10N_numberOfCellsPerLineLabel = null;
    private String L10N_numberOfLinesPerPageLabel = null;
    private String L10N_multipleFilesLabel = null;

    private TreeMap<BrailleFileType,String> L10N_brailleFile = new TreeMap();
    private TreeMap<TableType,String> L10N_table = new TreeMap();


    public ExportDialog(XComponentContext xContext,
                        Settings settings,
                        ProgressBar progressbar)
                 throws com.sun.star.uno.Exception {

        logger.entering("ExportDialog", "<init>");

        this.settings = settings;
        this.xContext = xContext;
        this.progressbar = progressbar;

        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/ExportDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, dialogControl.getModel());

        L10N_windowTitle = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("exportDialogTitle");
        L10N_okButton = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("exportButton");
        L10N_cancelButton = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("cancelButton");
        L10N_settingsButton = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("settingsDialogTitle")+ "\u2026";

        L10N_brailleFileLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("brailleFileLabel") + ":";
        L10N_tableLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("tableLabel") + ":";
        L10N_duplexLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("duplexLabel");
        L10N_eightDotsLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("eightDotsLabel");
        L10N_numberOfCellsPerLineLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("numberOfCellsPerLineLabel") + ":";
        L10N_numberOfLinesPerPageLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("numberOfLinesPerPageLabel") + ":";
        L10N_multipleFilesLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("multipleFilesLabel");

        L10N_table.put(TableType.UNDEFINED,         "-");
        L10N_table.put(TableType.UNICODE_BRAILLE,   "PEF (Unicode Braille)");
        L10N_table.put(TableType.EN_US,             "US English (Uppercase)");
        L10N_table.put(TableType.EN_GB,             "UK English (Lowercase)");
        L10N_table.put(TableType.NL,                "Dutch");
        L10N_table.put(TableType.DA_DK,             "Danish");
        L10N_table.put(TableType.DE_DE,             "German");
        L10N_table.put(TableType.IT_IT_FIRENZE,     "Italian");
        L10N_table.put(TableType.SV_SE_CX,          "Swedish");
        L10N_table.put(TableType.SV_SE_MIXED,       "Swedish (2)");

        L10N_table.put(TableType.BRL,               "BRL (Non-ASCII Braille)");
        L10N_table.put(TableType.ES_OLD,            "Spanish (Old)");
        L10N_table.put(TableType.ES_NEW,            "Spanish (New)");

        L10N_brailleFile.put(BrailleFileType.NONE,  "-");
        L10N_brailleFile.put(BrailleFileType.PEF,   "PEF (Portable Embosser Format)");
        L10N_brailleFile.put(BrailleFileType.BRF,   "BRF (Braille Formatted)");
        L10N_brailleFile.put(BrailleFileType.BRA,   "BRA");

        okButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_okButton));
        cancelButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_cancelButton));
        settingsButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_settingsButton));

        brailleFileListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_brailleFileListBox));
        tableListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_tableListBox));
        duplexCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_duplexCheckBox));
        eightDotsCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_eightDotsCheckBox));
        numberOfCellsPerLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_numberOfCellsPerLineField));
        numberOfLinesPerPageField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_numberOfLinesPerPageField));
        multipleFilesCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_multipleFilesCheckBox));

        okButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, okButton)).getModel());
        tableListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableListBox)).getModel());
        duplexCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, duplexCheckBox)).getModel());
        eightDotsCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, eightDotsCheckBox)).getModel());
        multipleFilesCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, multipleFilesCheckBox)).getModel());

        setDialogValues();
        addListeners();
        setLabels();

        logger.exiting("ExportDialog", "<init>");

    }

    private void addListeners() {
        
        brailleFileListBox.addItemListener(this);
        eightDotsCheckBox.addItemListener(this);
        settingsButton.addActionListener(this);

    }

    public boolean execute() throws com.sun.star.uno.Exception {

        logger.entering("ExportDialog", "execute");

        short ret = dialog.execute();

        getDialogValues();
        dialogComponent.dispose();

        if (settingsDialog != null) {
            settingsDialog.dispose();
        }

        logger.exiting("ExportDialog", "execute");

        if (ret == ((short) PushButtonType.OK_value)) {
            return true;
        } else {
            return false;
        }
    }

    private void setLabels() throws com.sun.star.uno.Exception {

        XFixedText xFixedText = null;

        windowProperties.setPropertyValue("Title", L10N_windowTitle);
        okButton.setLabel(L10N_okButton);
        cancelButton.setLabel(L10N_cancelButton);
        settingsButton.setLabel(L10N_settingsButton);

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_brailleFileLabel));
        xFixedText.setText(L10N_brailleFileLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableLabel));
        xFixedText.setText(L10N_tableLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_duplexLabel));
        xFixedText.setText(L10N_duplexLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_eightDotsLabel));
        xFixedText.setText(L10N_eightDotsLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numberOfCellsPerLineLabel));
        xFixedText.setText(L10N_numberOfCellsPerLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numberOfLinesPerPageLabel));
        xFixedText.setText(L10N_numberOfLinesPerPageLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_multipleFilesLabel));
        xFixedText.setText(L10N_multipleFilesLabel);

    }

    private void setDialogValues() throws com.sun.star.uno.Exception {

        numberOfCellsPerLineField.setDecimalDigits((short)0);
        numberOfLinesPerPageField.setDecimalDigits((short)0);
        numberOfCellsPerLineField.setMin((double)settings.getMinCellsPerLine());
        numberOfLinesPerPageField.setMin((double)settings.getMinLinesPerPage());
        numberOfCellsPerLineField.setMax((double)settings.getMaxCellsPerLine());
        numberOfLinesPerPageField.setMax((double)settings.getMaxLinesPerPage());
        numberOfCellsPerLineField.setValue((double)settings.getCellsPerLine());
        numberOfLinesPerPageField.setValue((double)settings.getLinesPerPage());

        if (System.getProperty("os.name").toLowerCase().contains("mac os") ||
                (Math.min(1, settings.getNumberOfVolumes()) +
                 settings.getNumberOfSupplements() +
                (settings.getPreliminaryVolumeEnabled()?1:0)) < 2) {
            settings.setMultipleFiles(false);
            multipleFilesCheckBoxProperties.setPropertyValue("Enabled", false);
        }

        multipleFilesCheckBox.setState((short)(settings.getMultipleFiles()?1:0));

        updateBrailleFileListBox();
        updateDuplexCheckBox();
        updateEightDotsCheckBox();
        updateTableListBox();
        updateOKButton();

    }

    private void getDialogValues() {

        settings.setCellsPerLine((int)numberOfCellsPerLineField.getValue());
        settings.setLinesPerPage((int)numberOfLinesPerPageField.getValue());
        settings.setTable(tableTypes.get(tableListBox.getSelectedItemPos()));
        settings.setMultipleFiles(multipleFilesCheckBox.getState()==(short)1);
        try {
            settings.setDuplex((duplexCheckBox.getState()==(short)1));
        } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Update the state of the OK button (enabled or disabled).
     *
     */
    private void updateOKButton() throws com.sun.star.uno.Exception {
        okButtonProperties.setPropertyValue("Enabled", settings.getBrailleFileType()!=BrailleFileType.NONE);
    }

    /**
     * Update the list of available generic braille files in the 'Braille file' listbox and select the correct item.
     *
     */
    private void updateBrailleFileListBox() throws com.sun.star.uno.Exception {

        BrailleFileType key = null;

        brailleFileListBox.removeItemListener(this);

            brailleFileListBox.removeItems((short)0, Short.MAX_VALUE);
            brailleFileTypes = settings.getSupportedBrailleFileTypes();
            for (int i=0;i<brailleFileTypes.size();i++) {
                key = brailleFileTypes.get(i);
                if (L10N_brailleFile.containsKey(key)) {
                    brailleFileListBox.addItem(L10N_brailleFile.get(key), (short)i);
                } else {
                    brailleFileListBox.addItem(key.name(), (short)i);
                }
            }
            brailleFileListBox.selectItemPos((short)brailleFileTypes.indexOf(settings.getBrailleFileType()), true);

        brailleFileListBox.addItemListener(this);

    }

    /**
     * Update the list of available character sets in the 'Character set' listbox and select the correct item.
     *
     */
    private void updateTableListBox() throws com.sun.star.uno.Exception {

        TableType key;

        tableListBox.removeItems((short)0, Short.MAX_VALUE);
        tableTypes = settings.getSupportedTableTypes();
        for (int i=0;i<tableTypes.size();i++) {
            key = tableTypes.get(i);
            if (L10N_table.containsKey(key)) {
                tableListBox.addItem(L10N_table.get(key), (short)i);
            } else {
                tableListBox.addItem(key.name(), (short)i);
            }
        }
        tableListBox.selectItemPos((short)tableTypes.indexOf(settings.getTable()), true);
        tableListBoxProperties.setPropertyValue("Enabled", settings.getBrailleFileType()!=BrailleFileType.NONE);

    }

    /**
     * Update the 'Recto-verso' checkbox.
     *
     */
    private void updateDuplexCheckBox() throws com.sun.star.uno.Exception {

        duplexCheckBox.setState((short)(settings.getDuplex()?1:0));
        duplexCheckBoxProperties.setPropertyValue("Enabled", settings.duplexIsSupported(true) &&
                                                             settings.duplexIsSupported(false));

    }

    private void updateEightDotsCheckBox() throws com.sun.star.uno.Exception {

        boolean eightDotsSupported = settings.eightDotsIsSupported();
        if (eightDotsSupported) {
            try {
                settings.setEightDots(true);
            } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
                logger.log(Level.SEVERE, null, ex);
            } 
        }

        eightDotsCheckBox.removeItemListener(this);
            eightDotsCheckBox.setState((short)(settings.getEightDots()?1:0));
            eightDotsCheckBoxProperties.setPropertyValue("Enabled", settings.eightDotsIsSupported() && !eightDotsSupported);
        eightDotsCheckBox.addItemListener(this);

    }

    public void itemStateChanged(ItemEvent itemEvent) {

        Object source = itemEvent.Source;

        try {

             if (source.equals(brailleFileListBox)) {

                settings.setBrailleFileType(brailleFileTypes.get(brailleFileListBox.getSelectedItemPos()));

                updateTableListBox();
                updateDuplexCheckBox();
                updateEightDotsCheckBox();
                updateOKButton();

            } else if (source.equals(eightDotsCheckBox)) {

                settings.setEightDots((eightDotsCheckBox.getState()==(short)1));

                updateTableListBox();

            }

        } catch (org_pef_text.pef2text.UnsupportedPaperException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void actionPerformed(ActionEvent actionEvent) {

        Object source = actionEvent.Source;

        try {

            if (source.equals(settingsButton)) {

                if (settingsDialog == null) {
                    settingsDialog = new SettingsDialog(xContext);
                    progressbar.start();
                    progressbar.setSteps(1);
                    progressbar.setStatus("Loading settings...");
                    settingsDialog.initialise(settings, progressbar);
                    progressbar.finish(true);
                    progressbar.close();
                }

                settingsDialog.execute();

            }
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param event
     */
    public void disposing(EventObject event) {}

}