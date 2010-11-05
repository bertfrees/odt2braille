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

package be.docarch.odt2braille.addon;

import java.util.logging.Logger;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.TreeMap;
import java.util.ArrayList;

import com.sun.star.uno.XComponentContext;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.PushButtonType;
import com.sun.star.lang.XComponent;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XCheckBox;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.beans.XPropertySet;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XTextListener;
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.lang.EventObject;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;

import be.docarch.odt2braille.Settings;
import org_pef_text.pef2text.Paper;
import org_pef_text.pef2text.Paper.PaperSize;
import org_pef_text.pef2text.EmbosserFactory.EmbosserType;
import org_pef_text.TableFactory.TableType;


/**
 *
 * @author   Bert Frees
 */
public class EmbossDialog implements XItemListener,
                                     XActionListener,
                                     XTextListener {

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille.addon");

    private Settings settings = null;
    private XComponentContext xContext = null;
    private Locale oooLocale = null;
    private ProgressBar progressbar = null;
    private SettingsDialog settingsDialog = null;

    private ArrayList<EmbosserType> embosserTypes = null;
    private ArrayList<TableType> tableTypes = null;
    private ArrayList<PaperSize> paperSizes = null;

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

    private XListBox embosserListBox = null;
    private XListBox tableListBox = null;
    private XListBox paperSizeListBox = null;
    private XNumericField paperWidthField = null;
    private XNumericField paperHeightField = null;
    private XListBox paperWidthUnitListBox = null;
    private XListBox paperHeightUnitListBox = null;
    private XCheckBox duplexCheckBox = null;
    private XCheckBox eightDotsCheckBox = null;
    private XCheckBox mirrorAlignCheckBox = null;
    private XNumericField numberOfCellsPerLineField = null;
    private XNumericField numberOfLinesPerPageField = null;
    private XNumericField marginLeftField = null;
    private XNumericField marginRightField = null;
    private XNumericField marginTopField = null;
    private XNumericField marginBottomField = null;

    private XTextComponent paperWidthTextComponent = null;
    private XTextComponent paperHeightTextComponent = null;
    private XTextComponent numberOfCellsPerLineTextComponent = null;
    private XTextComponent numberOfLinesPerPageTextComponent = null;
    private XTextComponent marginLeftTextComponent = null;
    private XTextComponent marginRightTextComponent = null;
    private XTextComponent marginTopTextComponent = null;
    private XTextComponent marginBottomTextComponent = null;

    private XPropertySet tableListBoxProperties = null;
    private XPropertySet paperSizeListBoxProperties = null;
    private XPropertySet paperWidthFieldProperties = null;
    private XPropertySet paperHeightFieldProperties = null;
    private XPropertySet paperWidthUnitListBoxProperties = null;
    private XPropertySet paperHeightUnitListBoxProperties = null;
    private XPropertySet duplexCheckBoxProperties = null;
    private XPropertySet eightDotsCheckBoxProperties = null;
    private XPropertySet mirrorAlignCheckBoxProperties = null;
    private XPropertySet marginLeftFieldProperties = null;
    private XPropertySet marginRightFieldProperties = null;
    private XPropertySet marginTopFieldProperties = null;
    private XPropertySet marginBottomFieldProperties = null;

    private static String _embosserListBox = "ListBox2";
    private static String _tableListBox = "ListBox3";
    private static String _paperSizeListBox = "ListBox4";
    private static String _paperWidthField = "NumericField1";
    private static String _paperHeightField = "NumericField2";
    private static String _paperWidthUnitListBox = "ListBox5";
    private static String _paperHeightUnitListBox = "ListBox6";
    private static String _duplexCheckBox = "CheckBox1";
    private static String _eightDotsCheckBox = "CheckBox3";
    private static String _mirrorAlignCheckBox = "CheckBox2";
    private static String _numberOfCellsPerLineField = "NumericField3";
    private static String _numberOfLinesPerPageField = "NumericField4";
    private static String _marginLeftField = "NumericField5";
    private static String _marginRightField = "NumericField6";
    private static String _marginTopField = "NumericField7";
    private static String _marginBottomField = "NumericField8";

    private static String _embosserLabel = "Label2";
    private static String _tableLabel = "Label3";
    private static String _paperSizeLabel = "Label4";
    private static String _paperWidthLabel = "Label10";
    private static String _paperHeightLabel = "Label11";
    private static String _duplexLabel = "Label8";
    private static String _eightDotsLabel = "Label15";
    private static String _mirrorAlignLabel = "Label5";
    private static String _numberOfCellsPerLineLabel = "Label6";
    private static String _numberOfLinesPerPageLabel = "Label7";
    private static String _marginLabel = "Label9";
    private static String _marginLeftLabel = "Label1";
    private static String _marginRightLabel = "Label13";
    private static String _marginTopLabel = "Label12";
    private static String _marginBottomLabel = "Label14";

    private String L10N_embosserLabel = null;
    private String L10N_tableLabel = null;
    private String L10N_paperSizeLabel = null;
    private String L10N_paperWidthLabel = null;
    private String L10N_paperHeightLabel = null;
    private String L10N_duplexLabel = null;
    private String L10N_eightDotsLabel = null;
    private String L10N_mirrorAlignLabel = null;
    private String L10N_numberOfCellsPerLineLabel = null;
    private String L10N_numberOfLinesPerPageLabel = null;
    private String L10N_marginLabel = null;
    private String L10N_marginLeftLabel = null;
    private String L10N_marginRightLabel = null;
    private String L10N_marginTopLabel = null;
    private String L10N_marginBottomLabel = null;

    private TreeMap<String,String> L10N_embosser = new TreeMap();
    private TreeMap<String,String> L10N_table = new TreeMap();
    private TreeMap<String,String> L10N_paperSize = new TreeMap();


    public EmbossDialog(XComponentContext xContext,
                        Settings settings,
                        ProgressBar progressbar)
                 throws com.sun.star.uno.Exception {

        logger.entering("EmbossDialog", "<init>");

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
        String dialogUrl = xPkgInfo.getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn") + "/dialogs/EmbossDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, dialogControl.getModel());

        L10N_windowTitle = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("embossDialogTitle");
        L10N_okButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("embossButton");
        L10N_cancelButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("cancelButton");
        L10N_settingsButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("settingsDialogTitle")+ "\u2026";

        L10N_embosserLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("embosserLabel") + ":";
        L10N_tableLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("tableLabel") + ":";
        L10N_paperSizeLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paperSizeLabel") + ":";
        L10N_paperWidthLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paperWidthLabel") + ":";
        L10N_paperHeightLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("paperHeightLabel") + ":";
        L10N_duplexLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("duplexLabel");
        L10N_eightDotsLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("eightDotsLabel");
        L10N_mirrorAlignLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("mirrorAlignLabel");
        L10N_numberOfCellsPerLineLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("numberOfCellsPerLineLabel") + ":";
        L10N_numberOfLinesPerPageLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("numberOfLinesPerPageLabel") + ":";
        L10N_marginLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("marginLabel") + ":";
        L10N_marginLeftLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("left");
        L10N_marginRightLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("right");
        L10N_marginTopLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("top");
        L10N_marginBottomLabel = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("bottom");

        L10N_embosser.put("NONE",                  "-");
        L10N_embosser.put("INDEX_BASIC",           "Index Braille - 3.30 Basic V2");
        L10N_embosser.put("INDEX_EVEREST",         "Index Braille - 9.20 Everest V2");
        L10N_embosser.put("INDEX_EVEREST_V3",      "Index Braille - Everest V3");
        L10N_embosser.put("INDEX_BASIC_D_V3",      "Index Braille - Basic-D V3");
        L10N_embosser.put("INDEX_BASIC_D",         "Index Braille - Basic-D");
        L10N_embosser.put("INDEX_BASIC_BLUE_BAR",  "Index Braille - Basic Blue-Bar");
        L10N_embosser.put("BRAILLO_200",           "Braillo 200 (firmware 000.17 or later)");
        L10N_embosser.put("BRAILLO_400_S",         "Braillo 400S (firmware 000.17 or later)");
        L10N_embosser.put("BRAILLO_400_SR",        "Braillo 400SR (firmware 000.17 or later)");
        L10N_embosser.put("INTERPOINT_55",         "Interpoint 55");
        L10N_embosser.put("IMPACTO_600",           "Impacto 600");
        L10N_embosser.put("IMPACTO_TEXTO",         "Impacto Texto");
        L10N_embosser.put("PORTATHIEL_BLUE",       "Portathiel Blue");

        L10N_table.put("UNDEFINED",             "-");
        L10N_table.put("EN_US",                 "US Computer Braille");
        L10N_table.put("EN_GB",                 "US Computer Braille (Lower Case)");
        L10N_table.put("BRAILLO_6DOT_001_00",   "Braillo USA 6 Dot 001.00");
        L10N_table.put("BRAILLO_6DOT_044_00",   "Braillo England 6 Dot 044.00");
        L10N_table.put("BRAILLO_6DOT_046_01",   "Braillo Sweden 6 Dot 046.01");
        L10N_table.put("BRAILLO_6DOT_047_01",   "Braillo Norway 6 Dot 047.01");
        L10N_table.put("IMPACTO",               "Impacto");
        L10N_table.put("IMPACTO256",            "Impacto");

        L10N_paperSize.put("UNDEFINED",         "-");
        L10N_paperSize.put("A4",                "A4");
        L10N_paperSize.put("W210MM_X_H10INCH",  "210 mm x 10 inch");
        L10N_paperSize.put("W210MM_X_H11INCH",  "210 mm x 11 inch");
        L10N_paperSize.put("W210MM_X_H12INCH",  "210 mm x 12 inch");
        L10N_paperSize.put("FA44",              "FA44 Accurate");
        L10N_paperSize.put("FA44_LEGACY",       "FA44 Legacy");
        L10N_paperSize.put("CUSTOM",            "Custom...");

        okButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_okButton));
        cancelButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_cancelButton));
        settingsButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_settingsButton));

        embosserListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_embosserListBox));
        tableListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_tableListBox));
        paperSizeListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paperSizeListBox));
        paperWidthField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paperWidthField));
        paperHeightField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_paperHeightField));
        paperWidthUnitListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paperWidthUnitListBox));
        paperHeightUnitListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paperHeightUnitListBox));
        duplexCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_duplexCheckBox));
        eightDotsCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_eightDotsCheckBox));
        mirrorAlignCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_mirrorAlignCheckBox));
        numberOfCellsPerLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_numberOfCellsPerLineField));
        numberOfLinesPerPageField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_numberOfLinesPerPageField));
        marginLeftField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginLeftField));
        marginRightField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginRightField));
        marginTopField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginTopField));
        marginBottomField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginBottomField));
        paperWidthTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_paperWidthField));
        paperHeightTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_paperHeightField));
        numberOfCellsPerLineTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_numberOfCellsPerLineField));
        numberOfLinesPerPageTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_numberOfLinesPerPageField));
        marginLeftTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginLeftField));
        marginRightTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginRightField));
        marginTopTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginTopField));
        marginBottomTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginBottomField));

        okButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, okButton)).getModel());
        tableListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableListBox)).getModel());
        paperSizeListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperSizeListBox)).getModel());
        paperWidthFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperWidthField)).getModel());
        paperHeightFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperHeightField)).getModel());
        paperWidthUnitListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperWidthUnitListBox)).getModel());
        paperHeightUnitListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperHeightUnitListBox)).getModel());
        duplexCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, duplexCheckBox)).getModel());
        eightDotsCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, eightDotsCheckBox)).getModel());
        mirrorAlignCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, mirrorAlignCheckBox)).getModel());
        marginLeftFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginLeftField)).getModel());
        marginRightFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginRightField)).getModel());
        marginTopFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginTopField)).getModel());
        marginBottomFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginBottomField)).getModel());

        setDialogValues();
        addListeners();
        setLabels();

        logger.exiting("EmbossDialog", "<init>");

    }

    private void addListeners() {

        settingsButton.addActionListener(this);
        embosserListBox.addItemListener(this);
        paperSizeListBox.addItemListener(this);
        paperWidthUnitListBox.addItemListener(this);
        paperHeightUnitListBox.addItemListener(this);
        duplexCheckBox.addItemListener(this);
        eightDotsCheckBox.addItemListener(this);
        paperWidthTextComponent.addTextListener(this);
        paperHeightTextComponent.addTextListener(this);
        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginLeftTextComponent.addTextListener(this);
        marginRightTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);

    }

    public boolean execute() throws com.sun.star.uno.Exception {

        logger.entering("EmbossDialog", "execute");

        short ret = dialog.execute();

        getDialogValues();
        dialogComponent.dispose();
        
        if (settingsDialog != null) {
            settingsDialog.dispose();
        }

        logger.exiting("EmbossDialog", "execute");

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

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_embosserLabel));
        xFixedText.setText(L10N_embosserLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_tableLabel));
        xFixedText.setText(L10N_tableLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperSizeLabel));
        xFixedText.setText(L10N_paperSizeLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperWidthLabel));
        xFixedText.setText(L10N_paperWidthLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperHeightLabel));
        xFixedText.setText(L10N_paperHeightLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_duplexLabel));
        xFixedText.setText(L10N_duplexLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_eightDotsLabel));
        xFixedText.setText(L10N_eightDotsLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_mirrorAlignLabel));
        xFixedText.setText(L10N_mirrorAlignLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numberOfCellsPerLineLabel));
        xFixedText.setText(L10N_numberOfCellsPerLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numberOfLinesPerPageLabel));
        xFixedText.setText(L10N_numberOfLinesPerPageLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginLabel));
        xFixedText.setText(L10N_marginLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginLeftLabel));
        xFixedText.setText(L10N_marginLeftLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginRightLabel));
        xFixedText.setText(L10N_marginRightLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginTopLabel));
        xFixedText.setText(L10N_marginTopLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginBottomLabel));
        xFixedText.setText(L10N_marginBottomLabel);

    }

    private void setDialogValues() throws com.sun.star.uno.Exception {

        numberOfCellsPerLineField.setDecimalDigits((short)0);
        numberOfLinesPerPageField.setDecimalDigits((short)0);
        marginLeftField.setDecimalDigits((short)0);
        marginRightField.setDecimalDigits((short)0);
        marginTopField.setDecimalDigits((short)0);
        marginBottomField.setDecimalDigits((short)0);

        paperWidthUnitListBox.addItem("mm", (short)0);
        paperWidthUnitListBox.addItem("in", (short)1);
        paperWidthUnitListBox.selectItemPos((short)0, true);
        paperHeightUnitListBox.addItem("mm", (short)0);
        paperHeightUnitListBox.addItem("in", (short)1);
        paperHeightUnitListBox.selectItemPos((short)0, true);

        updateEmbosserListBox();        
        updateDuplexCheckBox();
        updateEightDotsCheckBox();
        updatePaperSizeListBox();
        updatePaperDimensionFields();
        updateDimensionFields();
        updateMirrorAlignCheckBox();
        updateTableListBox();
        updateOKButton();

    }

    private void getDialogValues() {

        settings.setMirrorAlign((mirrorAlignCheckBox.getState() == (short)1));
        settings.setTable(tableTypes.get(tableListBox.getSelectedItemPos()));

    }

    /**
     * Update the state of the OK button (enabled or disabled).
     *
     */
    private void updateOKButton() throws com.sun.star.uno.Exception {
        okButtonProperties.setPropertyValue("Enabled", settings.getPaperSize()!=PaperSize.UNDEFINED);
    }

    /**
     * Update the list of available embosser in the 'Embosser' listbox and select the correct item.
     *
     */
    private void updateEmbosserListBox() throws com.sun.star.uno.Exception {

        String key = null;

        embosserListBox.removeItemListener(this);

            embosserListBox.removeItems((short)0, Short.MAX_VALUE);
            embosserTypes = settings.getSupportedEmbossers();
            for (int i=0;i<embosserTypes.size();i++) {
                key = embosserTypes.get(i).name();
                if (L10N_embosser.containsKey(key)) {
                    embosserListBox.addItem(L10N_embosser.get(key), (short)i);
                } else {
                    embosserListBox.addItem(key, (short)i);
                }
            }
            embosserListBox.selectItemPos((short)embosserTypes.indexOf(settings.getEmbosser()), true);

        embosserListBox.addItemListener(this);

    }

    /**
     * Update the list of available character sets in the 'Character set' listbox and select the correct item.
     *
     */
    private void updateTableListBox() throws com.sun.star.uno.Exception {

        String key;

        tableListBox.removeItems((short)0, Short.MAX_VALUE);
        tableTypes = settings.getSupportedTableTypes();
        for (int i=0;i<tableTypes.size();i++) {
            key = tableTypes.get(i).name();
            if (L10N_table.containsKey(key)) {
                tableListBox.addItem(L10N_table.get(key), (short)i);
            } else {
                tableListBox.addItem(key, (short)i);
            }
        }
        tableListBox.selectItemPos((short)tableTypes.indexOf(settings.getTable()), true);
        tableListBoxProperties.setPropertyValue("Enabled", settings.getEmbosser()!=EmbosserType.NONE);

    }

    /**
     * Update the list of available paper sizes in the 'Paper size' listbox and select the correct item.
     *
     */
    private void updatePaperSizeListBox() throws com.sun.star.uno.Exception {

        String key;

        paperSizeListBox.removeItemListener(this);

            paperSizeListBox.removeItems((short)0, Short.MAX_VALUE);
            paperSizes = settings.getSupportedPaperSizes();
            for (int i=0;i<paperSizes.size();i++) {
                key = paperSizes.get(i).name();
                if (L10N_paperSize.containsKey(key)) {
                    paperSizeListBox.addItem(L10N_paperSize.get(key), (short)i);
                } else {
                    paperSizeListBox.addItem(key, (short)i);
                }
            }
            paperSizeListBox.selectItemPos((short)paperSizes.indexOf(settings.getPaperSize()), true);
            paperSizeListBoxProperties.setPropertyValue("Enabled", settings.getEmbosser()!=EmbosserType.NONE);

        paperSizeListBox.addItemListener(this);

    }

    private void updatePaperDimensionFields() throws com.sun.star.uno.Exception {

        paperWidthTextComponent.removeTextListener(this);
        paperHeightTextComponent.removeTextListener(this);

            paperWidthFieldProperties.setPropertyValue("Enabled", settings.getPaperSize() == PaperSize.CUSTOM);
            paperHeightFieldProperties.setPropertyValue("Enabled", settings.getPaperSize() == PaperSize.CUSTOM);
            paperWidthUnitListBoxProperties.setPropertyValue("Enabled", settings.getPaperSize() != PaperSize.UNDEFINED);
            paperHeightUnitListBoxProperties.setPropertyValue("Enabled", settings.getPaperSize() != PaperSize.UNDEFINED);

            if (settings.getPaperSize() == PaperSize.UNDEFINED) {

                paperWidthField.setDecimalDigits((short)0);
                paperHeightField.setDecimalDigits((short)0);

                paperWidthField.setMin(0d);
                paperHeightField.setMin(0d);
                paperWidthField.setMax(0d);
                paperHeightField.setMax(0d);
                paperWidthField.setValue(0d);
                paperHeightField.setValue(0d);

            } else {

                paperWidthField.setDecimalDigits((short) ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? 2 : 0));
                paperHeightField.setDecimalDigits((short)((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? 2 : 0));

                paperWidthField.setMin(settings.getMinPaperWidth()   / ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d));
                paperHeightField.setMin(settings.getMinPaperHeight() / ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));
                paperWidthField.setMax(settings.getMaxPaperWidth()   / ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d));
                paperHeightField.setMax(settings.getMaxPaperHeight() / ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));
                paperWidthField.setValue(settings.getPaperWidth()    / ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d));
                paperHeightField.setValue(settings.getPaperHeight()  / ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));

            }

        paperWidthTextComponent.addTextListener(this);
        paperHeightTextComponent.addTextListener(this);

    }

    /**
     * Update the 'Recto-verso' checkbox.
     *
     */
    private void updateDuplexCheckBox() throws com.sun.star.uno.Exception {

        duplexCheckBox.removeItemListener(this);
            duplexCheckBox.setState((short)(settings.getDuplex()?1:0));
            duplexCheckBoxProperties.setPropertyValue("Enabled", settings.duplexIsSupported());
        duplexCheckBox.addItemListener(this);

    }

    private void updateEightDotsCheckBox() throws com.sun.star.uno.Exception {

        eightDotsCheckBox.removeItemListener(this);
            eightDotsCheckBox.setState((short)(settings.getEightDots()?1:0));
            eightDotsCheckBoxProperties.setPropertyValue("Enabled", settings.eightDotsIsSupported());
        eightDotsCheckBox.addItemListener(this);

    }

    /**
     * Update the 'Mirror margins' checkbox.
     *
     */
    private void updateMirrorAlignCheckBox() throws com.sun.star.uno.Exception {

        mirrorAlignCheckBox.setState((short)(settings.getMirrorAlign()?1:0));
        mirrorAlignCheckBoxProperties.setPropertyValue("Enabled", settings.mirrorAlignIsSupported());

    }

    /**
     * Update the 'Cells per line', 'Lines per page' and 'Margin' field values.
     * This method is called when the respective settings have possibly changed because the user changed one of these values.
     *
     */
    private void updateDimensionFieldValues() {

        numberOfCellsPerLineTextComponent.removeTextListener(this);
        numberOfLinesPerPageTextComponent.removeTextListener(this);
        marginLeftTextComponent.removeTextListener(this);
        marginRightTextComponent.removeTextListener(this);
        marginTopTextComponent.removeTextListener(this);
        marginBottomTextComponent.removeTextListener(this);

            numberOfCellsPerLineField.setValue((double)settings.getCellsPerLine());
            numberOfLinesPerPageField.setValue((double)settings.getLinesPerPage());
            marginLeftField.setValue((double)settings.getMarginLeft());
            marginRightField.setValue((double)settings.getMarginRight());
            marginTopField.setValue((double)settings.getMarginTop());
            marginBottomField.setValue((double)settings.getMarginBottom());

        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginLeftTextComponent.addTextListener(this);
        marginRightTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);

    }

    /**
     * Update the maximum, minimum and current values and the states (enabled or dissabled) of the
     * 'Cells per line', 'Lines per page' and 'Margin' field values on the 'Export/Emboss' tab.
     * This method is called when the respective braille settings have possibly changed because another paper size was selected.
     *
     */
    private void updateDimensionFields() throws com.sun.star.uno.Exception {

        numberOfCellsPerLineTextComponent.removeTextListener(this);
        numberOfLinesPerPageTextComponent.removeTextListener(this);
        marginLeftTextComponent.removeTextListener(this);
        marginRightTextComponent.removeTextListener(this);
        marginTopTextComponent.removeTextListener(this);
        marginBottomTextComponent.removeTextListener(this);

            marginLeftFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginRightFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginTopFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginBottomFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());

            if (settings.marginsSupported()) {

                marginLeftField.setMin((double)settings.getMinMarginLeft());
                marginRightField.setMin((double)settings.getMinMarginRight());
                marginTopField.setMin((double)settings.getMinMarginTop());
                marginBottomField.setMin((double)settings.getMinMarginBottom());

                marginLeftField.setMax((double)settings.getMaxMarginLeft());
                marginRightField.setMax((double)settings.getMaxMarginRight());
                marginTopField.setMax((double)settings.getMaxMarginTop());
                marginBottomField.setMax((double)settings.getMaxMarginBottom());

            } else {

                marginLeftField.setMin((double)0);
                marginRightField.setMin((double)0);
                marginTopField.setMin((double)0);
                marginBottomField.setMin((double)0);

                marginLeftField.setMax((double)0);
                marginRightField.setMax((double)0);
                marginTopField.setMax((double)0);
                marginBottomField.setMax((double)0);

            }

            numberOfCellsPerLineField.setMin((double)settings.getMinCellsPerLine());
            numberOfLinesPerPageField.setMin((double)settings.getMinLinesPerPage());
            numberOfCellsPerLineField.setMax((double)settings.getMaxCellsPerLine());
            numberOfLinesPerPageField.setMax((double)settings.getMaxLinesPerPage());

            numberOfCellsPerLineField.setValue((double)settings.getCellsPerLine());
            numberOfLinesPerPageField.setValue((double)settings.getLinesPerPage());
            marginLeftField.setValue((double)settings.getMarginLeft());
            marginRightField.setValue((double)settings.getMarginRight());
            marginTopField.setValue((double)settings.getMarginTop());
            marginBottomField.setValue((double)settings.getMarginBottom());

        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginLeftTextComponent.addTextListener(this);
        marginRightTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);

    }

    public void itemStateChanged(ItemEvent itemEvent) {

        Object source = itemEvent.Source;

        try {

             if (source.equals(embosserListBox)) {

                settings.setEmbosser(embosserTypes.get(embosserListBox.getSelectedItemPos()));

                updateDuplexCheckBox();
                updateEightDotsCheckBox();                
                updatePaperSizeListBox();
                updatePaperDimensionFields();
                updateDimensionFields();
                updateMirrorAlignCheckBox();
                updateTableListBox();
                updateOKButton();

            } else if (source.equals(paperSizeListBox)) {

                settings.setPaperSize(paperSizes.get(paperSizeListBox.getSelectedItemPos()));

                updatePaperDimensionFields();
                updateDimensionFields();
                updateMirrorAlignCheckBox();
                updateOKButton();

            } else if (source.equals(paperWidthUnitListBox) ||
                       source.equals(paperHeightUnitListBox)) {

                updatePaperDimensionFields();

            } else if (source.equals(duplexCheckBox)) {

                settings.setDuplex((duplexCheckBox.getState()==(short)1));

                updateMirrorAlignCheckBox();

            } else if (source.equals(eightDotsCheckBox)) {

                settings.setEightDots((eightDotsCheckBox.getState()==(short)1));

                updateDimensionFields();
                updateTableListBox();

            }

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
     * Is called when a 'Cells per line', 'Lines per page' or 'Margin' field value is changed by the user.
     * All relevant braille settings and dialog fields are updated.
     *
     * @param textEvent
     */
    public void textChanged(TextEvent textEvent) {

        Object source = textEvent.Source;

        try {

            if (source.equals(numberOfCellsPerLineTextComponent)) {
                settings.setCellsPerLine((int)numberOfCellsPerLineField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(numberOfLinesPerPageTextComponent)) {
                settings.setLinesPerPage((int)numberOfLinesPerPageField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(marginLeftTextComponent)) {
                settings.setMarginLeft((int)marginLeftField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(marginRightTextComponent)) {
                settings.setMarginRight((int)marginRightField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(marginTopTextComponent)) {
                settings.setMarginTop((int)marginTopField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(marginBottomTextComponent)) {
                settings.setMarginBottom((int)marginBottomField.getValue());
                updateDimensionFieldValues();
            } else if (source.equals(paperWidthTextComponent) ||
                       source.equals(paperHeightTextComponent)) {
                settings.setPaperSize(
                    paperWidthField.getValue()  * ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d),
                    paperHeightField.getValue() * ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));
                updateDimensionFields();
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