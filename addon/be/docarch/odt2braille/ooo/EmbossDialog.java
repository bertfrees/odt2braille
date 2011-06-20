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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XSpinField;
import com.sun.star.awt.XSpinListener;
import com.sun.star.awt.SpinEvent;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XTextListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XFocusListener;
import com.sun.star.awt.FocusEvent;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.PushButtonType;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.EventObject;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.Settings;
import be.docarch.odt2braille.CustomPaper;
import org.daisy.paper.Paper;
import org.daisy.braille.table.Table;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserTools;

/**
 *
 * @author   Bert Frees
 */
public class EmbossDialog implements XItemListener,
                                     XActionListener,
                                     XTextListener,
                                     XFocusListener,
                                     XSpinListener {

    private enum EmbosserStatus {  ALPHA,
                                   BETA,
                                   STABLE  };

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N_BUNDLE = Constants.OOO_L10N_PATH;

    private Settings settings = null;
    private XComponentContext xContext = null;
    private ProgressBar progressbar = null;
    private SettingsDialog settingsDialog = null;

    private List<Embosser> embosserList = null;
    private List<Table> tableList = null;
    private List<Paper> paperList = null;

    private Comparator<Embosser> embosserComparator = null;
    private Comparator<Table> tableComparator = null;
    private Comparator<Paper> paperComparator = null;

    private Map<String,EmbosserStatus> embosserStatus = null;

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

    private String warningImageUrl = null;

    private XListBox embosserListBox = null;
    private XListBox tableListBox = null;
    private XListBox paperListBox = null;
    private XNumericField paperWidthField = null;
    private XNumericField paperHeightField = null;
    private XListBox paperWidthUnitListBox = null;
    private XListBox paperHeightUnitListBox = null;
    private XCheckBox duplexCheckBox = null;
    private XCheckBox zFoldingCheckBox = null;
    private XCheckBox saddleStitchCheckBox = null;
    private XCheckBox eightDotsCheckBox = null;
    private XNumericField numberOfCellsPerLineField = null;
    private XNumericField numberOfLinesPerPageField = null;
    private XNumericField marginInnerField = null;
    private XNumericField marginOuterField = null;
    private XNumericField marginTopField = null;
    private XNumericField marginBottomField = null;
    private XNumericField sheetsPerQuireField = null;

    private XWindow paperWidthWindow = null;
    private XWindow paperHeightWindow = null;

    private XSpinField paperWidthSpinButton = null;
    private XSpinField paperHeightSpinButton = null;

    private XTextComponent paperWidthTextComponent = null;
    private XTextComponent paperHeightTextComponent = null;
    private XTextComponent marginInnerTextComponent = null;
    private XTextComponent marginOuterTextComponent = null;
    private XTextComponent marginTopTextComponent = null;
    private XTextComponent marginBottomTextComponent = null;

    private XPropertySet tableListBoxProperties = null;
    private XPropertySet paperListBoxProperties = null;
    private XPropertySet paperWidthFieldProperties = null;
    private XPropertySet paperHeightFieldProperties = null;
    private XPropertySet paperWidthUnitListBoxProperties = null;
    private XPropertySet paperHeightUnitListBoxProperties = null;
    private XPropertySet duplexCheckBoxProperties = null;
    private XPropertySet eightDotsCheckBoxProperties = null;
    private XPropertySet cellsPerLineFieldProperties = null;
    private XPropertySet linesPerPageFieldProperties = null;
    private XPropertySet marginInnerFieldProperties = null;
    private XPropertySet marginOuterFieldProperties = null;
    private XPropertySet marginTopFieldProperties = null;
    private XPropertySet marginBottomFieldProperties = null;
    private XPropertySet zFoldingCheckBoxProperties = null;
    private XPropertySet saddleStitchCheckBoxProperties = null;
    private XPropertySet sheetsPerQuireFieldProperties = null;
    private XPropertySet warningImageControlProperties = null;

    private static String _embosserListBox = "ListBox2";
    private static String _tableListBox = "ListBox3";
    private static String _paperListBox = "ListBox4";
    private static String _paperWidthField = "NumericField1";
    private static String _paperHeightField = "NumericField2";
    private static String _paperWidthUnitListBox = "ListBox5";
    private static String _paperHeightUnitListBox = "ListBox6";
    private static String _duplexCheckBox = "CheckBox1";
    private static String _eightDotsCheckBox = "CheckBox3";
    private static String _numberOfCellsPerLineField = "NumericField3";
    private static String _numberOfLinesPerPageField = "NumericField4";
    private static String _marginInnerField = "NumericField5";
    private static String _marginOuterField = "NumericField6";
    private static String _marginTopField = "NumericField7";
    private static String _marginBottomField = "NumericField8";
    private static String _zFoldingCheckBox = "CheckBox2";
    private static String _saddleStitchCheckBox = "CheckBox4";
    private static String _sheetsPerQuireField = "NumericField9";
    private static String _warningImageControl = "ImageControl1";

    private static String _embosserLabel = "Label2";
    private static String _tableLabel = "Label3";
    private static String _paperLabel = "Label4";
    private static String _paperWidthLabel = "Label10";
    private static String _paperHeightLabel = "Label11";
    private static String _duplexLabel = "Label8";
    private static String _eightDotsLabel = "Label15";
    private static String _numberOfCellsPerLineLabel = "Label6";
    private static String _numberOfLinesPerPageLabel = "Label7";
    private static String _marginLabel = "Label9";
    private static String _marginInnerLabel = "Label1";
    private static String _marginOuterLabel = "Label13";
    private static String _marginTopLabel = "Label12";
    private static String _marginBottomLabel = "Label14";
    private static String _zFoldingLabel = "Label5";
    private static String _saddleStitchLabel = "Label16";
    private static String _sheetsPerQuireLabel = "Label17";

    private String L10N_embosserLabel = null;
    private String L10N_tableLabel = null;
    private String L10N_paperLabel = null;
    private String L10N_paperWidthLabel = null;
    private String L10N_paperHeightLabel = null;
    private String L10N_duplexLabel = null;
    private String L10N_eightDotsLabel = null;
    private String L10N_numberOfCellsPerLineLabel = null;
    private String L10N_numberOfLinesPerPageLabel = null;
    private String L10N_marginLabel = null;
    private String L10N_marginInnerLabel = null;
    private String L10N_marginOuterLabel = null;
    private String L10N_marginTopLabel = null;
    private String L10N_marginBottomLabel = null;
    private String L10N_zFoldingLabel = null;
    private String L10N_saddleStitchLabel = null;
    private String L10N_sheetsPerQuireLabel = null;

    public EmbossDialog(XComponentContext xContext,
                        Settings settings,
                        ProgressBar progressbar)
                 throws com.sun.star.uno.Exception {

        logger.entering("EmbossDialog", "<init>");

        this.settings = settings;
        this.xContext = xContext;
        this.progressbar = progressbar;

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/EmbossDialog.xdl";
        warningImageUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/images/warning_20x20.png";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, dialogControl.getModel());

        embosserList = new ArrayList<Embosser>();
        tableList = new ArrayList<Table>();
        paperList = new ArrayList<Paper>();
        
        embosserComparator = new Comparator<Embosser>() {
          //@Override
            public int compare(Embosser e1, Embosser e2) {
                String id1 = e1.getIdentifier();
                String id2 = e2.getIdentifier();
                if (id1.equals(id2)) {
                    return 0;
                } else if (id1.equals(Settings.GENERIC_EMBOSSER)) {
                    return -1;
                } else if (id2.equals(Settings.GENERIC_EMBOSSER)) {
                    return 1;
                } else {
                    return ((Comparable)e1.getDisplayName()).compareTo(e2.getDisplayName());
                }
            }
        };
        tableComparator = new Comparator<Table>() {
          //@Override
            public int compare(Table t1, Table t2) {
                return ((Comparable)t1.getDisplayName()).compareTo(t2.getDisplayName());
            }
        };
        paperComparator = new Comparator<Paper>() {
          //@Override
            public int compare(Paper p1, Paper p2) {
                String id1 = p1.getIdentifier();
                String id2 = p2.getIdentifier();
                if (id1.equals(id2)) {
                    return 0;
                } else if (id1.equals(Settings.CUSTOM_PAPER)) {
                    return 1;
                } else if (id2.equals(Settings.CUSTOM_PAPER)) {
                    return -1;
                } else {
                    return ((Comparable)p1.getDisplayName()).compareTo(p2.getDisplayName());
                }
            }
        };

        embosserStatus = new HashMap<String,EmbosserStatus>();
        for (Embosser e : settings.getSupportedEmbossers()) {
            String id = e.getIdentifier();
            if (id.startsWith(Settings.INTERPOINT) ||
                id.startsWith(Settings.BRAILLO)||
                id.equals(Settings.GENERIC_EMBOSSER)) {
                embosserStatus.put(id, EmbosserStatus.STABLE);
            } else if (id.startsWith(Settings.INDEX_BRAILLE)) {
                embosserStatus.put(id, EmbosserStatus.BETA);
            } else {
                embosserStatus.put(id, EmbosserStatus.ALPHA);
            }
        }

        Locale oooLocale;
        try { oooLocale = UnoUtils.getUILocale(xContext); } catch (Exception e) {
              oooLocale = Locale.ENGLISH; }

        ResourceBundle bundle = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale);

        L10N_windowTitle = bundle.getString("embossDialogTitle");
        L10N_okButton = bundle.getString("embossButton");
        L10N_cancelButton = bundle.getString("cancelButton");
        L10N_settingsButton = bundle.getString("settingsDialogTitle")+ "\u2026";

        L10N_embosserLabel = bundle.getString("embosserLabel") + ":";
        L10N_tableLabel = bundle.getString("tableLabel") + ":";
        L10N_paperLabel = bundle.getString("paperSizeLabel") + ":";
        L10N_paperWidthLabel = bundle.getString("paperWidthLabel") + ":";
        L10N_paperHeightLabel = bundle.getString("paperHeightLabel") + ":";
        L10N_duplexLabel = bundle.getString("duplexLabel");
        L10N_eightDotsLabel = bundle.getString("eightDotsLabel");
        L10N_numberOfCellsPerLineLabel = bundle.getString("numberOfCellsPerLineLabel") + ":";
        L10N_numberOfLinesPerPageLabel = bundle.getString("numberOfLinesPerPageLabel") + ":";
        L10N_marginLabel = bundle.getString("marginLabel") + ":";
        L10N_marginInnerLabel = bundle.getString("inner") + ":";
        L10N_marginOuterLabel = bundle.getString("outer") + ":";
        L10N_marginTopLabel = bundle.getString("top") + ":";
        L10N_marginBottomLabel = bundle.getString("bottom") + ":";
        L10N_zFoldingLabel = bundle.getString("zFoldingLabel");
        L10N_saddleStitchLabel = bundle.getString("saddleStitchLabel");
        L10N_sheetsPerQuireLabel = bundle.getString("sheetsPerQuireLabel") + ":";

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
        paperListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                dialogControlContainer.getControl(_paperListBox));
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
        zFoldingCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_zFoldingCheckBox));
        saddleStitchCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_saddleStitchCheckBox));
        sheetsPerQuireField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_sheetsPerQuireField));
        eightDotsCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                dialogControlContainer.getControl(_eightDotsCheckBox));
        numberOfCellsPerLineField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_numberOfCellsPerLineField));
        numberOfLinesPerPageField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_numberOfLinesPerPageField));
        marginInnerField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginInnerField));
        marginOuterField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginOuterField));
        marginTopField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginTopField));
        marginBottomField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                dialogControlContainer.getControl(_marginBottomField));
        paperWidthTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_paperWidthField));
        paperHeightTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_paperHeightField));
        marginInnerTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginInnerField));
        marginOuterTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginOuterField));
        marginTopTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginTopField));
        marginBottomTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginBottomField));
        paperWidthWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class,
                dialogControlContainer.getControl(_paperWidthField));
        paperHeightWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class,
                dialogControlContainer.getControl(_paperHeightField));
        paperWidthSpinButton = (XSpinField) UnoRuntime.queryInterface(XSpinField.class,
                dialogControlContainer.getControl(_paperWidthField));
        paperHeightSpinButton = (XSpinField) UnoRuntime.queryInterface(XSpinField.class,
                dialogControlContainer.getControl(_paperHeightField));

        okButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, okButton)).getModel());
        tableListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, tableListBox)).getModel());
        paperListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, paperListBox)).getModel());
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
        cellsPerLineFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, numberOfCellsPerLineField)).getModel());
        linesPerPageFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, numberOfLinesPerPageField)).getModel());
        marginInnerFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginInnerField)).getModel());
        marginOuterFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginOuterField)).getModel());
        marginTopFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginTopField)).getModel());
        marginBottomFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, marginBottomField)).getModel());
        zFoldingCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, zFoldingCheckBox)).getModel());
        saddleStitchCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, saddleStitchCheckBox)).getModel());
        sheetsPerQuireFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, sheetsPerQuireField)).getModel());

        XControl warningImageControl = (XControl) UnoRuntime.queryInterface(XControl.class,
                dialogControlContainer.getControl(_warningImageControl));
        warningImageControlProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, warningImageControl)).getModel());

        setDialogValues();
        addListeners();
        setLabels();

        logger.exiting("EmbossDialog", "<init>");

    }

    private void addListeners() {

        settingsButton.addActionListener(this);
        embosserListBox.addItemListener(this);
        paperListBox.addItemListener(this);
        paperWidthUnitListBox.addItemListener(this);
        paperHeightUnitListBox.addItemListener(this);
        duplexCheckBox.addItemListener(this);
        saddleStitchCheckBox.addItemListener(this);
        zFoldingCheckBox.addItemListener(this);
        eightDotsCheckBox.addItemListener(this);
        paperWidthTextComponent.addTextListener(this);
        paperHeightTextComponent.addTextListener(this);
        marginInnerTextComponent.addTextListener(this);
        marginOuterTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);
        paperWidthSpinButton.addSpinListener(this);
        paperHeightSpinButton.addSpinListener(this);
        paperWidthWindow.addFocusListener(this);
        paperHeightWindow.addFocusListener(this);

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
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperLabel));
        xFixedText.setText(L10N_paperLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperWidthLabel));
        xFixedText.setText(L10N_paperWidthLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_paperHeightLabel));
        xFixedText.setText(L10N_paperHeightLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_duplexLabel));
        xFixedText.setText(L10N_duplexLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_eightDotsLabel));
        xFixedText.setText(L10N_eightDotsLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numberOfCellsPerLineLabel));
        xFixedText.setText(L10N_numberOfCellsPerLineLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_numberOfLinesPerPageLabel));
        xFixedText.setText(L10N_numberOfLinesPerPageLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginLabel));
        xFixedText.setText(L10N_marginLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginInnerLabel));
        xFixedText.setText(L10N_marginInnerLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginOuterLabel));
        xFixedText.setText(L10N_marginOuterLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginTopLabel));
        xFixedText.setText(L10N_marginTopLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_marginBottomLabel));
        xFixedText.setText(L10N_marginBottomLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_zFoldingLabel));
        xFixedText.setText(L10N_zFoldingLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_saddleStitchLabel));
        xFixedText.setText(L10N_saddleStitchLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_sheetsPerQuireLabel));
        xFixedText.setText(L10N_sheetsPerQuireLabel);

    }

    private void setDialogValues() throws com.sun.star.uno.Exception {

        numberOfCellsPerLineField.setDecimalDigits((short)0);
        numberOfLinesPerPageField.setDecimalDigits((short)0);
        marginInnerField.setDecimalDigits((short)0);
        marginOuterField.setDecimalDigits((short)0);
        marginTopField.setDecimalDigits((short)0);
        marginBottomField.setDecimalDigits((short)0);
        sheetsPerQuireField.setDecimalDigits((short)0);
        sheetsPerQuireField.setMin((double)1);

        paperWidthField.setMin((double)0);
        paperHeightField.setMin((double)0);
        paperWidthField.setMax((double)Integer.MAX_VALUE);
        paperHeightField.setMax((double)Integer.MAX_VALUE);

        marginInnerField.setMin((double)0);
        marginOuterField.setMin((double)0);
        marginTopField.setMin((double)0);
        marginBottomField.setMin((double)0);

        marginInnerField.setMax((double)Integer.MAX_VALUE);
        marginOuterField.setMax((double)Integer.MAX_VALUE);
        marginTopField.setMax((double)Integer.MAX_VALUE);
        marginBottomField.setMax((double)Integer.MAX_VALUE);

        numberOfCellsPerLineField.setMin((double)0);
        numberOfLinesPerPageField.setMin((double)0);
        numberOfCellsPerLineField.setMax((double)Integer.MAX_VALUE);
        numberOfLinesPerPageField.setMax((double)Integer.MAX_VALUE);

        cellsPerLineFieldProperties.setPropertyValue("Enabled", false);
        linesPerPageFieldProperties.setPropertyValue("Enabled", false);

        paperWidthUnitListBox.addItem("mm", (short)0);
        paperWidthUnitListBox.addItem("in", (short)1);
        paperWidthUnitListBox.selectItemPos((short)0, true);
        paperHeightUnitListBox.addItem("mm", (short)0);
        paperHeightUnitListBox.addItem("in", (short)1);
        paperHeightUnitListBox.selectItemPos((short)0, true);

        sheetsPerQuireField.setValue((double)settings.getSheetsPerQuire());

        updateEmbosserListBox();
        updateWarningImage();
        updateSaddleStitchCheckBox();
        updateZFoldingCheckBox();
        updateDuplexCheckBox();
        updateEightDotsCheckBox();
        updatePaperListBox();
        updatePaperDimensionFields();
        updateDimensionFields();
        updateTableListBox();
        updateOKButton();

    }

    private void getDialogValues() {

        if (tableList.size() > 1) {
            settings.setTable(tableList.get(tableListBox.getSelectedItemPos()));
        }
        settings.setSheetsPerQuire((int)sheetsPerQuireField.getValue());

    }

    /**
     * Update the state of the OK button (enabled or disabled).
     *
     */
    private void updateOKButton() throws com.sun.star.uno.Exception {
        okButtonProperties.setPropertyValue("Enabled", settings.getPaper()!=null &&
                                                       settings.getEmbosser()!=null);
    }

    /**
     * Update the list of available embosser in the 'Embosser' listbox and select the correct item.
     *
     */
    private void updateEmbosserListBox() throws com.sun.star.uno.Exception {

        short i = 0;
        short select = -1;
        String selectedId = settings.getEmbosser().getIdentifier();

        embosserList.clear();
        embosserList.addAll(settings.getSupportedEmbossers());
        Collections.sort(embosserList, embosserComparator);

        embosserListBox.removeItemListener(this);

            embosserListBox.removeItems((short)0, Short.MAX_VALUE);
            for (Embosser e : embosserList) {
                if (e.getIdentifier().equals(selectedId)) {
                    select = i;
                }
                switch (embosserStatus.get(e.getIdentifier())) {
                    case ALPHA:
                        embosserListBox.addItem(e.getDisplayName() + " (alpha)", i);
                        break;
                    case BETA:
                        embosserListBox.addItem(e.getDisplayName() + " (beta)", i);
                        break;
                    case STABLE:
                    default:
                        embosserListBox.addItem(e.getDisplayName(), i);
                }
                i++;
            }
            if (select>=0) {
                embosserListBox.selectItemPos(select, true);
            }

        embosserListBox.addItemListener(this);
    }

    private void updateWarningImage() throws com.sun.star.uno.Exception {

        switch (embosserStatus.get(settings.getEmbosser().getIdentifier())) {
            case ALPHA:
            case BETA:
                warningImageControlProperties.setPropertyValue("ImageURL", warningImageUrl);
                break;
            case STABLE:
            default:
                warningImageControlProperties.setPropertyValue("ImageURL", "");
        }
    }

    /**
     * Update the list of available character sets in the 'Character set' listbox and select the correct item.
     *
     */
    private void updateTableListBox() throws com.sun.star.uno.Exception {

        short i = 0;
        short select = -1;
        String selectedId = "";
        if (settings.getTable()!=null) { selectedId = settings.getTable().getIdentifier(); }

        tableList.clear();
        tableList.addAll(settings.getSupportedTables());
        Collections.sort(tableList, tableComparator);

        tableListBox.removeItems((short)0, Short.MAX_VALUE);

        if (tableList.size()<2) {
            tableListBoxProperties.setPropertyValue("Enabled", false);
            return;
        } else {
            tableListBoxProperties.setPropertyValue("Enabled", true);
        }

        for (Table t : tableList) {
            if (t.getIdentifier().equals(selectedId)) {
                select = i;
            }
            tableListBox.addItem(t.getDisplayName(), i);
            i++;
        }
        if (select>=0) {
            tableListBox.selectItemPos(select, true);
        }
    }

    /**
     * Update the list of available paper sizes in the 'Paper size' listbox and select the correct item.
     *
     */
    private void updatePaperListBox() throws com.sun.star.uno.Exception {

        short i = 0;
        short select = -1;
        String selectedId = settings.getPaper().getIdentifier();

        paperList.clear();
        paperList.addAll(settings.getSupportedPapers());
        Collections.sort(paperList, paperComparator);

        paperListBox.removeItemListener(this);

            paperListBox.removeItems((short)0, Short.MAX_VALUE);
            for (Paper p : paperList) {
                if (p.getIdentifier().equals(selectedId)) {
                    select = i;
                }
                paperListBox.addItem(p.getDisplayName(), (short)i);
                i++;
            }
            if (select>=0) {
                paperListBox.selectItemPos(select, true);
            }
            paperListBoxProperties.setPropertyValue("Enabled", settings.getEmbosser()!=null);

        paperListBox.addItemListener(this);

    }

    private void updatePaperDimensionFields() throws com.sun.star.uno.Exception {

        paperWidthTextComponent.removeTextListener(this);
        paperHeightTextComponent.removeTextListener(this);
            
            boolean enabled = false;            
            Paper paper = settings.getPaper();            
            if (paper != null) {
                enabled = (paper instanceof CustomPaper);
            }

            paperWidthFieldProperties.setPropertyValue("Enabled", enabled);
            paperHeightFieldProperties.setPropertyValue("Enabled", enabled);

            paperWidthField.setDecimalDigits((short) ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? 2 : 0));
            paperHeightField.setDecimalDigits((short)((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? 2 : 0));

            paperWidthField.setValue(settings.getPaperWidth()    / ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? EmbosserTools.INCH_IN_MM : 1d));
            paperHeightField.setValue(settings.getPaperHeight()  / ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? EmbosserTools.INCH_IN_MM : 1d));

            paperWidthUnitListBoxProperties.setPropertyValue("Enabled", paper != null);
            paperHeightUnitListBoxProperties.setPropertyValue("Enabled", paper != null);

        paperWidthTextComponent.addTextListener(this);
        paperHeightTextComponent.addTextListener(this);

    }
    
    private void updateSaddleStitchCheckBox() throws com.sun.star.uno.Exception {

        saddleStitchCheckBox.removeItemListener(this);
            saddleStitchCheckBox.setState((short)(settings.getSaddleStitch()?1:0));
            saddleStitchCheckBoxProperties.setPropertyValue("Enabled", settings.saddleStitchIsSupported());
            sheetsPerQuireFieldProperties.setPropertyValue("Enabled", settings.getSaddleStitch() &&
                                                                      settings.sheetsPerQuireIsSupported());
        saddleStitchCheckBox.addItemListener(this);

    }

    private void updateZFoldingCheckBox() throws com.sun.star.uno.Exception {

        zFoldingCheckBox.removeItemListener(this);
                zFoldingCheckBox.setState((short)(settings.getZFolding()?1:0));
                zFoldingCheckBoxProperties.setPropertyValue("Enabled", settings.zFoldingIsSupported());
        zFoldingCheckBox.addItemListener(this);
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
     * Update the maximum, minimum and current values and the states (enabled or dissabled) of the
     * 'Cells per line', 'Lines per page' and 'Margin' field values on the 'Export/Emboss' tab.
     * This method is called when the respective braille settings have possibly changed because another paper size was selected.
     *
     */
    private void updateDimensionFields() throws com.sun.star.uno.Exception {

        marginInnerTextComponent.removeTextListener(this);
        marginOuterTextComponent.removeTextListener(this);
        marginTopTextComponent.removeTextListener(this);
        marginBottomTextComponent.removeTextListener(this);

            marginInnerFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginOuterFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginTopFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginBottomFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());

            numberOfCellsPerLineField.setValue((double)settings.getCellsPerLine());
            numberOfLinesPerPageField.setValue((double)settings.getLinesPerPage());
            marginInnerField.setValue((double)(settings.getMarginInner() + settings.getMarginInnerOffset()));
            marginOuterField.setValue((double)(settings.getMarginOuter() + settings.getMarginOuterOffset()));
            marginTopField.setValue((double)(settings.getMarginTop() + settings.getMarginTopOffset()));
            marginBottomField.setValue((double)(settings.getMarginBottom() + settings.getMarginBottomOffset()));

        marginInnerTextComponent.addTextListener(this);
        marginOuterTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);

    }

  //@Override
    public void itemStateChanged(ItemEvent itemEvent) {

        logger.entering("EmbossDialog", "itemStateChanged");

        Object source = itemEvent.Source;

        try {

             if (source.equals(embosserListBox)) {

                settings.setEmbosser(embosserList.get(embosserListBox.getSelectedItemPos()));

                updateWarningImage();
                updateSaddleStitchCheckBox();
                updateZFoldingCheckBox();
                updateDuplexCheckBox();
                updateEightDotsCheckBox();
                updatePaperListBox();
                updatePaperDimensionFields();
                updateDimensionFields();
                updateTableListBox();
                updateOKButton();

            } else if (source.equals(paperListBox)) {

                settings.setPaper(paperList.get(paperListBox.getSelectedItemPos()));
                updatePaperDimensionFields();
                updateDimensionFields();
                updateOKButton();

            } else if (source.equals(paperWidthUnitListBox) ||
                       source.equals(paperHeightUnitListBox)) {

                updatePaperDimensionFields();
                updateOKButton();

            } else if (source.equals(duplexCheckBox)) {

                settings.setDuplex((duplexCheckBox.getState()==(short)1));
                updateDimensionFields();

            } else if (source.equals(eightDotsCheckBox)) {

                settings.setEightDots((eightDotsCheckBox.getState()==(short)1));
                updateDimensionFields();
                updateTableListBox();

            } else if (source.equals(saddleStitchCheckBox)) {

                settings.setSaddleStitch((saddleStitchCheckBox.getState()==(short)1));
                updateSaddleStitchCheckBox();
                updateDuplexCheckBox();
                updatePaperListBox();
                updatePaperDimensionFields();
                updateDimensionFields();
                updateOKButton();

            } else if (source.equals(zFoldingCheckBox)) {

                settings.setZFolding(zFoldingCheckBox.getState()==(short)1);
                updateDuplexCheckBox();
                updateDimensionFields();

            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

  //@Override
    public void actionPerformed(ActionEvent actionEvent) {

        logger.entering("EmbossDialog", "actionPerformed");

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
  //@Override
    public void textChanged(TextEvent textEvent) {

        logger.entering("EmbossDialog", "textChanged");

        Object source = textEvent.Source;

        try {

            if (source.equals(marginInnerTextComponent)) {
                settings.setMarginInner((int)marginInnerField.getValue() - settings.getMarginInnerOffset());
                updateDimensionFields();
            } else if (source.equals(marginOuterTextComponent)) {
                settings.setMarginOuter((int)marginOuterField.getValue() - settings.getMarginOuterOffset());
                updateDimensionFields();
            } else if (source.equals(marginTopTextComponent)) {
                settings.setMarginTop((int)marginTopField.getValue() - settings.getMarginTopOffset());
                updateDimensionFields();
            } else if (source.equals(marginBottomTextComponent)) {
                settings.setMarginBottom((int)marginBottomField.getValue() - settings.getMarginBottomOffset());
                updateDimensionFields();
            } else if (source.equals(paperWidthTextComponent) ||
                       source.equals(paperHeightTextComponent)) {
                if (settings.setCustomPaper(
                        paperWidthField.getValue()  * ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? EmbosserTools.INCH_IN_MM : 1d),
                        paperHeightField.getValue() * ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? EmbosserTools.INCH_IN_MM : 1d))) {
                    updatePaperDimensionFields();
                    updateDimensionFields();
                    updateOKButton();
                } else {
                    okButtonProperties.setPropertyValue("Enabled", false);
                }
            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

  //@Override
    public void focusLost(FocusEvent focusEvent) {

        logger.entering("EmbossDialog", "focusLost");

        Object source = focusEvent.Source;

        try {

            if (source.equals(paperWidthField) ||
                source.equals(paperHeightField)) {
                updatePaperDimensionFields();
                updateDimensionFields();
                updateOKButton();
            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

  //@Override
    public void focusGained(FocusEvent focusEvent) {}

    private void spin(SpinEvent spinEvent) {

        logger.entering("EmbossDialog", "spin");

        Object source = spinEvent.Source;

        try {

            if (source.equals(paperWidthWindow) ||
                source.equals(paperHeightWindow)) {
                updatePaperDimensionFields();
                updateDimensionFields();
                updateOKButton();
            }
            
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

  //@Override
    public void up(SpinEvent spinEvent) {
        spin(spinEvent);
    }

  //@Override
    public void down(SpinEvent spinEvent) {
        spin(spinEvent);
    }

  //@Override
    public void first(SpinEvent spinEvent) {}

  //@Override
    public void last(SpinEvent spinEvent) {}

    /**
     * @param event
     */
  //@Override
    public void disposing(EventObject event) {}
}