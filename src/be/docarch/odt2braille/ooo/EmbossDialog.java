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
import java.util.TreeMap;
import java.util.List;
import java.util.Map;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XTextListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XItemListener;
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
import org_pef_text.pef2text.Paper;
import org_pef_text.pef2text.Paper.PaperSize;
import org_pef_text.pef2text.EmbosserFactory.EmbosserType;
import org_pef_text.TableFactory.TableType;

import org_pef_text.pef2text.UnsupportedPaperException;


/**
 *
 * @author   Bert Frees
 */
public class EmbossDialog implements XItemListener,
                                     XActionListener,
                                     XTextListener {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N_BUNDLE = Constants.OOO_L10N_PATH;

    private Settings settings = null;
    private XComponentContext xContext = null;
    private XWindowPeer parentWindowPeer = null;
    private Locale oooLocale = null;
    private ProgressBar progressbar = null;
    private SettingsDialog settingsDialog = null;

    private List<EmbosserType> embosserTypes = null;
    private List<TableType> tableTypes = null;
    private List<PaperSize> paperSizes = null;

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

    private XTextComponent paperWidthTextComponent = null;
    private XTextComponent paperHeightTextComponent = null;
    private XTextComponent numberOfCellsPerLineTextComponent = null;
    private XTextComponent numberOfLinesPerPageTextComponent = null;
    private XTextComponent marginInnerTextComponent = null;
    private XTextComponent marginOuterTextComponent = null;
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
    private XPropertySet cellsPerLineFieldProperties = null;
    private XPropertySet linesPerPageFieldProperties = null;
    private XPropertySet marginInnerFieldProperties = null;
    private XPropertySet marginOuterFieldProperties = null;
    private XPropertySet marginTopFieldProperties = null;
    private XPropertySet marginBottomFieldProperties = null;
    private XPropertySet zFoldingCheckBoxProperties = null;
    private XPropertySet saddleStitchCheckBoxProperties = null;
    private XPropertySet sheetsPerQuireFieldProperties = null;

    private static String _embosserListBox = "ListBox2";
    private static String _tableListBox = "ListBox3";
    private static String _paperSizeListBox = "ListBox4";
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

    private static String _embosserLabel = "Label2";
    private static String _tableLabel = "Label3";
    private static String _paperSizeLabel = "Label4";
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
    private String L10N_paperSizeLabel = null;
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

    private Map<EmbosserType,String> L10N_embosser = new TreeMap<EmbosserType,String>();
    private Map<TableType,String> L10N_table = new TreeMap<TableType,String>();
    private Map<PaperSize,String> L10N_paperSize = new TreeMap<PaperSize,String>();


    public EmbossDialog(XComponentContext xContext,
                        XWindowPeer parentWindowPeer,
                        Settings settings,
                        ProgressBar progressbar)
                 throws com.sun.star.uno.Exception {

        logger.entering("EmbossDialog", "<init>");

        this.settings = settings;
        this.xContext = xContext;
        this.parentWindowPeer = parentWindowPeer;
        this.progressbar = progressbar;

        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/EmbossDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, dialogControl.getModel());

        L10N_windowTitle = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("embossDialogTitle");
        L10N_okButton = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("embossButton");
        L10N_cancelButton = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("cancelButton");
        L10N_settingsButton = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("settingsDialogTitle")+ "\u2026";

        L10N_embosserLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("embosserLabel") + ":";
        L10N_tableLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("tableLabel") + ":";
        L10N_paperSizeLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("paperSizeLabel") + ":";
        L10N_paperWidthLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("paperWidthLabel") + ":";
        L10N_paperHeightLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("paperHeightLabel") + ":";
        L10N_duplexLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("duplexLabel");
        L10N_eightDotsLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("eightDotsLabel");
        L10N_numberOfCellsPerLineLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("numberOfCellsPerLineLabel") + ":";
        L10N_numberOfLinesPerPageLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("numberOfLinesPerPageLabel") + ":";
        L10N_marginLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("marginLabel") + ":";
        L10N_marginInnerLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("inner") + ":";
        L10N_marginOuterLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("outer") + ":";
        L10N_marginTopLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("top") + ":";
        L10N_marginBottomLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("bottom") + ":";
        L10N_zFoldingLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("zFoldingLabel");
        L10N_saddleStitchLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("saddleStitchLabel");
        L10N_sheetsPerQuireLabel = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale).getString("sheetsPerQuireLabel") + ":";

        L10N_embosser.put(EmbosserType.NONE,                    "Generic");
        L10N_embosser.put(EmbosserType.INDEX_3_7,               "Index 3.7");
        L10N_embosser.put(EmbosserType.INDEX_ADVANCED,          "Index Advanced");
        L10N_embosser.put(EmbosserType.INDEX_BASIC_BLUE_BAR,    "Index Basic Blue-Bar");
        L10N_embosser.put(EmbosserType.INDEX_CLASSIC,           "Index Classic");
        L10N_embosser.put(EmbosserType.INDEX_DOMINO,            "Index Domino");
        L10N_embosser.put(EmbosserType.INDEX_EVEREST_S_V1,      "Index Everest-S V1");
        L10N_embosser.put(EmbosserType.INDEX_EVEREST_D_V1,      "Index Everest-D V1");
        L10N_embosser.put(EmbosserType.INDEX_BASIC_S_V2,        "Index Basic-S V2");
        L10N_embosser.put(EmbosserType.INDEX_BASIC_D_V2,        "Index Basic-D V2");
        L10N_embosser.put(EmbosserType.INDEX_EVEREST_D_V2,      "Index Everest-D V2");
        L10N_embosser.put(EmbosserType.INDEX_4X4_PRO_V2,        "Index 4X4 Pro V2");
        L10N_embosser.put(EmbosserType.INDEX_BASIC_D_V3,        "Index Basic-D V3");
        L10N_embosser.put(EmbosserType.INDEX_EVEREST_D_V3,      "Index Everest-D V3");
        L10N_embosser.put(EmbosserType.INDEX_4X4_PRO_V3,        "Index 4X4 Pro V3");
        L10N_embosser.put(EmbosserType.INDEX_4WAVES_PRO_V3,     "Index 4Waves Pro");
        L10N_embosser.put(EmbosserType.INDEX_BASIC_D_V4,        "Index Basic-D V4");
        L10N_embosser.put(EmbosserType.INDEX_EVEREST_D_V4,      "Index Everest-D V4");
        L10N_embosser.put(EmbosserType.INDEX_4X4_PRO_V4,        "Index 4X4 Pro V4");
        L10N_embosser.put(EmbosserType.INDEX_BRAILLE_BOX_V4,    "Index Braille Box V4");
        L10N_embosser.put(EmbosserType.BRAILLO_200,             "Braillo 200");
        L10N_embosser.put(EmbosserType.BRAILLO_400_S,           "Braillo 400S");
        L10N_embosser.put(EmbosserType.BRAILLO_400_SR,          "Braillo 400SR");
        L10N_embosser.put(EmbosserType.INTERPOINT_55,           "Interpoint 55");
        L10N_embosser.put(EmbosserType.IMPACTO_600,             "Impacto 600");
        L10N_embosser.put(EmbosserType.IMPACTO_TEXTO,           "Impacto Texto");
        L10N_embosser.put(EmbosserType.PORTATHIEL_BLUE,         "Portathiel Blue");

        L10N_table.put(TableType.UNDEFINED,             "-");
        L10N_table.put(TableType.BRAILLO_6DOT_001_00,   "Braillo USA 6 Dot 001.00");
        L10N_table.put(TableType.BRAILLO_6DOT_044_00,   "Braillo England 6 Dot 044.00");
        L10N_table.put(TableType.BRAILLO_6DOT_046_01,   "Braillo Sweden 6 Dot 046.01");
        L10N_table.put(TableType.BRAILLO_6DOT_047_01,   "Braillo Norway 6 Dot 047.01");
        L10N_table.put(TableType.EN_US,                 "US English (Uppercase)");
        L10N_table.put(TableType.EN_GB,                 "UK English (Lowercase)");
        L10N_table.put(TableType.NL,                    "Dutch");
        L10N_table.put(TableType.DA_DK,                 "Danish");
        L10N_table.put(TableType.DE_DE,                 "German");
        L10N_table.put(TableType.IT_IT_FIRENZE,         "Italian");
        L10N_table.put(TableType.SV_SE_CX,              "Swedish");
        L10N_table.put(TableType.SV_SE_MIXED,           "Swedish (2)");
        L10N_table.put(TableType.ES_OLD,                "Spanish (Old)");
        L10N_table.put(TableType.ES_NEW,                "Spanish (New)");

        L10N_paperSize.put(PaperSize.UNDEFINED,         "-");
        L10N_paperSize.put(PaperSize.A4_LANDSCAPE,      "A4 (Landscape)");
        L10N_paperSize.put(PaperSize.A3_LANDSCAPE,      "A3 (Landscape)");
        L10N_paperSize.put(PaperSize.W210MM_X_H10INCH,  "210 mm x 10 inch");
        L10N_paperSize.put(PaperSize.W210MM_X_H11INCH,  "210 mm x 11 inch");
        L10N_paperSize.put(PaperSize.W210MM_X_H12INCH,  "210 mm x 12 inch");
        L10N_paperSize.put(PaperSize.W240MM_X_H12INCH,  "240 mm x 12 inch");
        L10N_paperSize.put(PaperSize.W280MM_X_H12INCH,  "280 mm x 12 inch");
        L10N_paperSize.put(PaperSize.FA44,              "FA44 Accurate");
        L10N_paperSize.put(PaperSize.FA44_LEGACY,       "FA44 Legacy");
        L10N_paperSize.put(PaperSize.CUSTOM,            "Custom...");

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
        numberOfCellsPerLineTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_numberOfCellsPerLineField));
        numberOfLinesPerPageTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_numberOfLinesPerPageField));
        marginInnerTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginInnerField));
        marginOuterTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_marginOuterField));
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
        saddleStitchCheckBox.addItemListener(this);
        zFoldingCheckBox.addItemListener(this);
        eightDotsCheckBox.addItemListener(this);
        paperWidthTextComponent.addTextListener(this);
        paperHeightTextComponent.addTextListener(this);
        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginInnerTextComponent.addTextListener(this);
        marginOuterTextComponent.addTextListener(this);
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
        sheetsPerQuireField.setMin((short)1);

        paperWidthUnitListBox.addItem("mm", (short)0);
        paperWidthUnitListBox.addItem("in", (short)1);
        paperWidthUnitListBox.selectItemPos((short)0, true);
        paperHeightUnitListBox.addItem("mm", (short)0);
        paperHeightUnitListBox.addItem("in", (short)1);
        paperHeightUnitListBox.selectItemPos((short)0, true);

        sheetsPerQuireField.setValue((double)settings.getSheetsPerQuire());

        updateEmbosserListBox();
        updateSaddleStitchCheckBox();
        updateZFoldingCheckBox();
        updateDuplexCheckBox();
        updateEightDotsCheckBox();
        updatePaperSizeListBox();
        updatePaperDimensionFields();
        updateDimensionFields();
        updateTableListBox();
        updateOKButton();

    }

    private void getDialogValues() {

        if (tableTypes.size() > 1) {
            settings.setTable(tableTypes.get(tableListBox.getSelectedItemPos()));
        }
        settings.setSheetsPerQuire((int)sheetsPerQuireField.getValue());

    }

    /**
     * Update the state of the OK button (enabled or disabled).
     *
     */
    private void updateOKButton() throws com.sun.star.uno.Exception {
        okButtonProperties.setPropertyValue("Enabled", settings.getPaperSize()!=null &&
                                                       settings.getEmbosser()!=null ||
                                                       settings.getPaperSize()!=null);
    }

    /**
     * Update the list of available embosser in the 'Embosser' listbox and select the correct item.
     *
     */
    private void updateEmbosserListBox() throws com.sun.star.uno.Exception {

        EmbosserType key = null;

        embosserListBox.removeItemListener(this);

            embosserListBox.removeItems((short)0, Short.MAX_VALUE);
            embosserTypes = settings.getSupportedEmbossers();
            for (int i=0;i<embosserTypes.size();i++) {
                key = embosserTypes.get(i);
                if (L10N_embosser.containsKey(key)) {
                    embosserListBox.addItem(L10N_embosser.get(key), (short)i);
                } else {
                    embosserListBox.addItem(key.name(), (short)i);
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

        TableType key;

        tableListBox.removeItems((short)0, Short.MAX_VALUE);
        tableTypes = settings.getSupportedTableTypes();

        if (tableTypes.size() > 1) {
            tableListBoxProperties.setPropertyValue("Enabled", true);
            for (int i=0;i<tableTypes.size();i++) {
                key = tableTypes.get(i);
                if (L10N_table.containsKey(key)) {
                    tableListBox.addItem(L10N_table.get(key), (short)i);
                } else {
                    tableListBox.addItem(key.name(), (short)i);
                }
            }
            tableListBox.selectItemPos((short)tableTypes.indexOf(settings.getTable()), true);
        } else {
            tableListBoxProperties.setPropertyValue("Enabled", false);
        }
    }

    /**
     * Update the list of available paper sizes in the 'Paper size' listbox and select the correct item.
     *
     */
    private void updatePaperSizeListBox() throws com.sun.star.uno.Exception {

        PaperSize key;

        paperSizeListBox.removeItemListener(this);

            paperSizeListBox.removeItems((short)0, Short.MAX_VALUE);
            paperSizes = settings.getSupportedPaperSizes();
            for (int i=0;i<paperSizes.size();i++) {
                key = paperSizes.get(i);
                if (L10N_paperSize.containsKey(key)) {
                    paperSizeListBox.addItem(L10N_paperSize.get(key), (short)i);
                } else {
                    paperSizeListBox.addItem(key.name(), (short)i);
                }
            }
            if (paperSizes.size() > 0) {
                paperSizeListBox.selectItemPos((short)paperSizes.indexOf(settings.getPaperSize()), true);
                paperSizeListBoxProperties.setPropertyValue("Enabled", settings.getEmbosser()!=null);
            } else {
                paperSizeListBoxProperties.setPropertyValue("Enabled", false);
            }

        paperSizeListBox.addItemListener(this);

    }

    private void updatePaperDimensionFields() throws com.sun.star.uno.Exception {

        paperWidthTextComponent.removeTextListener(this);
        paperHeightTextComponent.removeTextListener(this);

            paperWidthFieldProperties.setPropertyValue("Enabled", settings.getPaperSize() == PaperSize.CUSTOM);
            paperHeightFieldProperties.setPropertyValue("Enabled", settings.getPaperSize() == PaperSize.CUSTOM);
            paperWidthUnitListBoxProperties.setPropertyValue("Enabled", settings.getPaperSize() != null);
            paperHeightUnitListBoxProperties.setPropertyValue("Enabled", settings.getPaperSize() != null);

            if (settings.getPaperSize() == null) {

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
            duplexCheckBoxProperties.setPropertyValue("Enabled", settings.duplexIsSupported(true) &&
                                                                 settings.duplexIsSupported(false));
        duplexCheckBox.addItemListener(this);

    }

    private void updateEightDotsCheckBox() throws com.sun.star.uno.Exception {

        eightDotsCheckBox.removeItemListener(this);
            eightDotsCheckBox.setState((short)(settings.getEightDots()?1:0));
            eightDotsCheckBoxProperties.setPropertyValue("Enabled", settings.eightDotsIsSupported());
        eightDotsCheckBox.addItemListener(this);

    }

    /**
     * Update the 'Cells per line', 'Lines per page' and 'Margin' field values.
     * This method is called when the respective settings have possibly changed because the user changed one of these values.
     *
     */
    private void updateDimensionFieldValues() {

        numberOfCellsPerLineTextComponent.removeTextListener(this);
        numberOfLinesPerPageTextComponent.removeTextListener(this);
        marginInnerTextComponent.removeTextListener(this);
        marginOuterTextComponent.removeTextListener(this);
        marginTopTextComponent.removeTextListener(this);
        marginBottomTextComponent.removeTextListener(this);

            numberOfCellsPerLineField.setValue((double)settings.getCellsPerLine());
            numberOfLinesPerPageField.setValue((double)settings.getLinesPerPage());
            marginInnerField.setValue((double)(settings.getMarginInner() + settings.getMarginInnerOffset()));
            marginOuterField.setValue((double)(settings.getMarginOuter() + settings.getMarginOuterOffset()));
            marginTopField.setValue((double)(settings.getMarginTop() + settings.getMarginTopOffset()));
            marginBottomField.setValue((double)(settings.getMarginBottom() + settings.getMarginBottomOffset()));

        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginInnerTextComponent.addTextListener(this);
        marginOuterTextComponent.addTextListener(this);
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
        marginInnerTextComponent.removeTextListener(this);
        marginOuterTextComponent.removeTextListener(this);
        marginTopTextComponent.removeTextListener(this);
        marginBottomTextComponent.removeTextListener(this);

            cellsPerLineFieldProperties.setPropertyValue("Enabled", true);
            linesPerPageFieldProperties.setPropertyValue("Enabled", true);
            marginInnerFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginOuterFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginTopFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());
            marginBottomFieldProperties.setPropertyValue("Enabled", settings.marginsSupported());

            int marginInnerOffset = settings.getMarginInnerOffset();
            int marginOuterOffset = settings.getMarginOuterOffset();
            int marginTopOffset = settings.getMarginTopOffset();
            int marginBottomOffset = settings.getMarginBottomOffset();

            if (settings.marginsSupported()) {

                marginInnerField.setMin((double)(settings.getMinMarginInner() + marginInnerOffset));
                marginOuterField.setMin((double)(settings.getMinMarginOuter() + marginOuterOffset));
                marginTopField.setMin((double)(settings.getMinMarginTop() + marginTopOffset));
                marginBottomField.setMin((double)(settings.getMinMarginBottom() + marginBottomOffset));

                marginInnerField.setMax((double)(settings.getMaxMarginInner() + marginInnerOffset));
                marginOuterField.setMax((double)(settings.getMaxMarginOuter() + marginOuterOffset));
                marginTopField.setMax((double)(settings.getMaxMarginTop() + marginTopOffset));
                marginBottomField.setMax((double)(settings.getMaxMarginBottom() + marginBottomOffset));

            } else {

                marginInnerField.setMin((double)0);
                marginOuterField.setMin((double)0);
                marginTopField.setMin((double)0);
                marginBottomField.setMin((double)0);

                marginInnerField.setMax((double)0);
                marginOuterField.setMax((double)0);
                marginTopField.setMax((double)0);
                marginBottomField.setMax((double)0);

            }

            numberOfCellsPerLineField.setMin((double)settings.getMinCellsPerLine());
            numberOfLinesPerPageField.setMin((double)settings.getMinLinesPerPage());
            numberOfCellsPerLineField.setMax((double)settings.getMaxCellsPerLine());
            numberOfLinesPerPageField.setMax((double)settings.getMaxLinesPerPage());

            numberOfCellsPerLineField.setValue((double)settings.getCellsPerLine());
            numberOfLinesPerPageField.setValue((double)settings.getLinesPerPage());
            marginInnerField.setValue((double)(settings.getMarginInner() + marginInnerOffset));
            marginOuterField.setValue((double)(settings.getMarginOuter() + marginOuterOffset));
            marginTopField.setValue((double)(settings.getMarginTop() + marginTopOffset));
            marginBottomField.setValue((double)(settings.getMarginBottom() + marginBottomOffset));

        numberOfCellsPerLineTextComponent.addTextListener(this);
        numberOfLinesPerPageTextComponent.addTextListener(this);
        marginInnerTextComponent.addTextListener(this);
        marginOuterTextComponent.addTextListener(this);
        marginTopTextComponent.addTextListener(this);
        marginBottomTextComponent.addTextListener(this);

    }

    public void handleUnsupportedPaperException(UnsupportedPaperException ex) {

        logger.log(Level.SEVERE, null, ex);

        UnoAwtUtils.showErrorMessageBox(parentWindowPeer, "Exception", "Unsupported paper. Please make another choice.");

        try {
            cellsPerLineFieldProperties.setPropertyValue("Enabled", false);
            linesPerPageFieldProperties.setPropertyValue("Enabled", false);
            marginInnerFieldProperties.setPropertyValue("Enabled", false);
            marginOuterFieldProperties.setPropertyValue("Enabled", false);
            marginTopFieldProperties.setPropertyValue("Enabled", false);
            marginBottomFieldProperties.setPropertyValue("Enabled", false);
            okButtonProperties.setPropertyValue("Enabled", false);
        } catch (com.sun.star.uno.Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public void itemStateChanged(ItemEvent itemEvent) {

        Object source = itemEvent.Source;

        try {

             if (source.equals(embosserListBox)) {

                settings.setEmbosser(embosserTypes.get(embosserListBox.getSelectedItemPos()));

                updateSaddleStitchCheckBox();
                updateZFoldingCheckBox();
                updateDuplexCheckBox();
                updateEightDotsCheckBox();                
                updatePaperSizeListBox();
                updatePaperDimensionFields();
                updateDimensionFields();
                updateTableListBox();
                updateOKButton();

            } else if (source.equals(paperSizeListBox)) {

                settings.setPaperSize(paperSizes.get(paperSizeListBox.getSelectedItemPos()));
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
                updatePaperSizeListBox();
                updatePaperDimensionFields();
                updateDimensionFields();
                updateOKButton();

            } else if (source.equals(zFoldingCheckBox)) {

                settings.setZFolding(zFoldingCheckBox.getState()==(short)1);
                updateDuplexCheckBox();
                updateDimensionFields();

            }

        } catch (UnsupportedPaperException ex) {
            handleUnsupportedPaperException(ex);
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
            } else if (source.equals(marginInnerTextComponent)) {
                settings.setMarginInner((int)marginInnerField.getValue() - settings.getMarginInnerOffset());
                updateDimensionFieldValues();
            } else if (source.equals(marginOuterTextComponent)) {
                settings.setMarginOuter((int)marginOuterField.getValue() - settings.getMarginOuterOffset());
                updateDimensionFieldValues();
            } else if (source.equals(marginTopTextComponent)) {
                settings.setMarginTop((int)marginTopField.getValue() - settings.getMarginTopOffset());
                updateDimensionFieldValues();
            } else if (source.equals(marginBottomTextComponent)) {
                settings.setMarginBottom((int)marginBottomField.getValue() - settings.getMarginBottomOffset());
                updateDimensionFieldValues();
            } else if (source.equals(paperWidthTextComponent) ||
                       source.equals(paperHeightTextComponent)) {
                settings.setPaperSize(
                    paperWidthField.getValue()  * ((paperWidthUnitListBox.getSelectedItemPos()==(short)1)  ? Paper.INCH_IN_MM : 1d),
                    paperHeightField.getValue() * ((paperHeightUnitListBox.getSelectedItemPos()==(short)1) ? Paper.INCH_IN_MM : 1d));
                updateDimensionFields();
            }

        } catch (UnsupportedPaperException ex) {
            handleUnsupportedPaperException(ex);
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param event
     */
    public void disposing(EventObject event) {}
}