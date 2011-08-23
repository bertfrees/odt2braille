package be.docarch.odt2braille.ooo;

import be.docarch.odt2braille.setup.PropertyEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XSpinField;
import com.sun.star.awt.XSpinListener;
import com.sun.star.awt.SpinEvent;
import com.sun.star.awt.XTextListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XFocusListener;
import com.sun.star.awt.FocusEvent;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.PushButtonType;
import com.sun.star.lang.XComponent;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.EmbossConfiguration;
import be.docarch.odt2braille.setup.EmbossConfiguration.PageFormatProperty;
import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.Property;
import be.docarch.odt2braille.setup.PropertyListener;
import be.docarch.odt2braille.ooo.dialog.*;
import org.daisy.paper.Paper;
import org.daisy.braille.table.Table;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.tools.Length;

/**
 *
 * @author   Bert Frees
 */
public class EmbossDialog {

    private enum EmbosserStatus { ALPHA,
                                  BETA,
                                  STABLE };

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N_BUNDLE = Constants.OOO_L10N_PATH;
    
    private final Configuration settings;
    private final XComponentContext context;
    private final ProgressBar progressbar;
    private final XDialog dialog;
    private final XComponent component;
    private final Map<String,EmbosserStatus> embosserStatus;
    private final String warningImageUrl;

    private SettingsDialog settingsDialog;

    /* BUTTONS */

    private final OKButton okButton;
    private final Button cancelButton;
    private final Button settingsButton;

    /* IMAGES */

    private final EmbosserWarningImage warningImage;

    /* FIELDS & CONTROLS */

    private final ListBox<Embosser> embosserListBox;
    private final ListBox<Table> charSetListBox;
    private final ListBox<Paper> paperListBox;
    
    private final CheckBox duplexCheckBox;
    private final CheckBox eightDotsCheckBox;
    private final CheckBox zFoldingCheckBox;
    private final CheckBox magazineModeCheckBox;

    private final CheckBox pageOrientationCheckBox;
    private final PaperDimensionControl pageWidthField;
    private final PaperDimensionControl pageHeightField;
    private final NumericPropertyField columnsField;
    private final NumericPropertyField rowsField;
    private final NumericSettingControl marginInnerField;
    private final NumericSettingControl marginOuterField;
    private final NumericSettingControl marginTopField;
    private final NumericSettingControl marginBottomField;
    private final NumericSettingControl sheetsPerQuireField;

    /* LABELS */

    private final Label embosserLabel;
    private final Label charSetLabel;
    private final Label paperLabel;
    private final Label duplexLabel;
    private final Label eightDotsLabel;
    private final Label zFoldingLabel;
    private final Label magazineModeLabel;
    private final Label pageOrientationLabel;
    private final Label pageWidthLabel;
    private final Label pageHeightLabel;
    private final Label columnsLabel;
    private final Label rowsLabel;
    private final Label marginLabel;
    private final Label marginInnerLabel;
    private final Label marginOuterLabel;
    private final Label marginTopLabel;
    private final Label marginBottomLabel;
    private final Label sheetsPerQuireLabel;


    public EmbossDialog(XComponentContext ctxt,
                        EmbossConfiguration embossSettings,
                        Configuration cfg,
                        ProgressBar pb)
                 throws com.sun.star.uno.Exception {

        logger.entering("EmbossDialog", "<init>");

        this.settings = cfg;
        this.context = ctxt;
        this.progressbar = pb;
        
        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(context);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/EmbossDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(context);
        dialog = xDialogProvider.createDialog(dialogUrl);
        XControlContainer container = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        component = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        XControl control = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        XPropertySet windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());
        warningImageUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/images/warning_20x20.png";
        
        Locale oooLocale;
        try { oooLocale = UnoUtils.getUILocale(context); } catch (Exception e) {
              oooLocale = Locale.ENGLISH; }

        ResourceBundle bundle = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale);

        windowProperties.setPropertyValue("Title", bundle.getString("embossDialogTitle"));

        embosserStatus = new HashMap<String,EmbosserStatus>();
        for (Embosser e : embossSettings.embosser.options()) {
            String id = e.getIdentifier();
            if (id.startsWith(EmbossConfiguration.INTERPOINT) ||
                id.startsWith(EmbossConfiguration.BRAILLO)||
                id.equals(EmbossConfiguration.GENERIC_EMBOSSER)) {
                embosserStatus.put(id, EmbosserStatus.STABLE);
            } else if (id.startsWith(EmbossConfiguration.INDEX_BRAILLE)) {
                embosserStatus.put(id, EmbosserStatus.BETA);
            } else {
                embosserStatus.put(id, EmbosserStatus.ALPHA);
            }
        }
        
        /* DIALOG ELEMENTS */
        
        okButton = new OKButton(container.getControl("CommandButton2"),
                                bundle.getString("embossButton"),
                                embossSettings.pageFormat);
        
        cancelButton = new Button(container.getControl("CommandButton1"),
                                  bundle.getString("cancelButton")) {
            public void actionPerformed(ActionEvent event) {}
        };
        
        settingsButton = new Button(container.getControl("CommandButton3"),
                                    bundle.getString("settingsDialogTitle") + "\u2026") {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (event.Source.equals(button)) {
                    try {
                        if (settingsDialog == null) {
                            progressbar.start();
                            progressbar.setSteps(SettingsDialog.getSteps());
                            progressbar.setStatus("Loading settings...");
                            settingsDialog = new SettingsDialog(context, settings, progressbar);
                            progressbar.finish(true);
                            progressbar.close();
                        }
                        settingsDialog.execute();
                    } catch (com.sun.star.uno.Exception e) {
                    }
                }
            }
        };

        embosserListBox = new ListBox<Embosser>(container.getControl("ListBox2")) {
            @Override
            protected String getDisplayValue(Embosser e) {
                switch (embosserStatus.get(e.getIdentifier())) {
                    case ALPHA:
                        return e.getDisplayName() + " (alpha)";
                    case BETA:
                        return e.getDisplayName() + " (beta)";
                    case STABLE:
                    default:
                        return e.getDisplayName();
                }
            }
            @Override
            public int compare(Embosser e1, Embosser e2) {
                String id1 = e1.getIdentifier();
                String id2 = e2.getIdentifier();
                if (id1.equals(id2)) {
                    return 0;
                } else if (id1.equals(EmbossConfiguration.GENERIC_EMBOSSER)) {
                    return -1;
                } else if (id2.equals(EmbossConfiguration.GENERIC_EMBOSSER)) {
                    return 1;
                } else {
                    return ((Comparable)e1.getDisplayName()).compareTo(e2.getDisplayName());
                }
            }
        };
        
        charSetListBox = new ListBox<Table>(container.getControl("ListBox3")) {
            @Override
            protected String getDisplayValue(Table t) {
                return t.getDisplayName();
            }
            @Override
            public void update() {
                if (property.options().size() > 1) {
                    super.update();
                } else {
                    options.clear();
                    listbox.removeItems((short)0, Short.MAX_VALUE);
                }
            }
            @Override
            public void updateProperties() {
                if (property.options().size() > 1) {
                    super.updateProperties();
                } else {
                    try {
                        propertySet.setPropertyValue("Enabled", false);
                    } catch (UnknownPropertyException e) {
                    } catch (PropertyVetoException e) {
                    } catch (IllegalArgumentException e) {
                    } catch (WrappedTargetException e) {
                    }
                }
            }
        };
        
        paperListBox = new ListBox<Paper>(container.getControl("ListBox4")) {
            @Override
            protected String getDisplayValue(Paper p) {
                return p.getDisplayName();
            }
            @Override
            public int compare(Paper p1, Paper p2) {
                String id1 = p1.getIdentifier();
                String id2 = p2.getIdentifier();
                if (id1.equals(id2)) {
                    return 0;
                } else if (id1.startsWith("be.docarch.odt2braille.CustomPaperProvider") &&
                          !id2.startsWith("be.docarch.odt2braille.CustomPaperProvider")) {
                    return 1;
                } else if (id2.startsWith("be.docarch.odt2braille.CustomPaperProvider") &&
                          !id1.startsWith("be.docarch.odt2braille.CustomPaperProvider")) {
                    return -1;
                } else {
                    return p1.getDisplayName().compareTo(p2.getDisplayName());
                }
            }
        };
        
        duplexCheckBox = new CheckBox(container.getControl("CheckBox1"));
        
        eightDotsCheckBox = new CheckBox(container.getControl("CheckBox3"));

        zFoldingCheckBox = new CheckBox(container.getControl("CheckBox2"));
        
        magazineModeCheckBox = new CheckBox(container.getControl("CheckBox4"));

        pageOrientationCheckBox = new CheckBox(container.getControl("CheckBox5"));

        pageWidthField = new PaperDimensionControl(container.getControl("NumericField1"),
                                                   container.getControl("ListBox5"),
                                                   embossSettings.pageWidth);

        pageHeightField = new PaperDimensionControl(container.getControl("NumericField2"),
                                                    container.getControl("ListBox6"),
                                                    embossSettings.pageHeight);
        
        columnsField = new NumericPropertyField(container.getControl("NumericField3"));
        
        rowsField = new NumericPropertyField(container.getControl("NumericField4"));
        
        marginInnerField = new MarginControl(container.getControl("NumericField5"),
                                             embossSettings.getMargins().inner);
        
        marginOuterField = new MarginControl(container.getControl("NumericField6"),
                                              embossSettings.getMargins().outer);
        
        marginTopField = new MarginControl(container.getControl("NumericField7"),
                                            embossSettings.getMargins().top);
        
        marginBottomField = new MarginControl(container.getControl("NumericField8"),
                                               embossSettings.getMargins().bottom);

        sheetsPerQuireField = new NumericSettingControl(container.getControl("NumericField9"));

        warningImage = new EmbosserWarningImage(container.getControl("ImageControl1"),
                                                embossSettings.embosser);
        
        /* LABELS */
        
        embosserLabel = new Label(container.getControl("Label2"),
                                  bundle.getString("embosserLabel") + ":");
        
        charSetLabel = new Label(container.getControl("Label3"),
                                 bundle.getString("tableLabel") + ":");
        
        paperLabel = new Label(container.getControl("Label4"),
                               bundle.getString("paperSizeLabel") + ":");
        
        duplexLabel = new Label(container.getControl("Label8"),
                                bundle.getString("duplexLabel"));
        
        eightDotsLabel = new Label(container.getControl("Label15"),
                                   bundle.getString("eightDotsLabel"));
        
        zFoldingLabel = new Label(container.getControl("Label5"),
                                  bundle.getString("zFoldingLabel"));
        
        magazineModeLabel = new Label(container.getControl("Label16"),
                                      bundle.getString("saddleStitchLabel"));

        pageOrientationLabel = new Label(container.getControl("Label18"),
                                         "Reverse orientation");

        pageWidthLabel = new Label(container.getControl("Label10"),
                                   bundle.getString("paperWidthLabel") + ":");
       
        pageHeightLabel = new Label(container.getControl("Label11"),
                                    bundle.getString("paperHeightLabel") + ":");
        
        columnsLabel = new Label(container.getControl("Label6"),
                                 bundle.getString("numberOfCellsPerLineLabel") + ":");
        
        rowsLabel = new Label(container.getControl("Label7"),
                              bundle.getString("numberOfLinesPerPageLabel") + ":");
        
        marginLabel = new Label(container.getControl("Label9"),
                                bundle.getString("marginLabel") + ":");
        
        marginInnerLabel = new Label(container.getControl("Label1"),
                                     bundle.getString("inner") + ":");
        
        marginOuterLabel = new Label(container.getControl("Label13"),
                                     bundle.getString("outer") + ":");
        
        marginTopLabel = new Label(container.getControl("Label12"),
                                   bundle.getString("top") + ":");
        
        marginBottomLabel = new Label(container.getControl("Label14"),
                                      bundle.getString("bottom") + ":");
        
        sheetsPerQuireLabel = new Label(container.getControl("Label17"),
                                        bundle.getString("sheetsPerQuireLabel") + ":");

        /* INITIALIZE */

        embosserListBox.link(embossSettings.embosser);
        charSetListBox.link(embossSettings.charSet);
        paperListBox.link(embossSettings.paper);
        duplexCheckBox.link(embossSettings.duplex);
        eightDotsCheckBox.link(embossSettings.eightDots);
        zFoldingCheckBox.link(embossSettings.zFolding);
        magazineModeCheckBox.link(embossSettings.magazineMode);
        sheetsPerQuireField.link(embossSettings.sheetsPerQuire);
        pageOrientationCheckBox.link(embossSettings.pageOrientation);
        columnsField.link(embossSettings.columns);
        rowsField.link(embossSettings.rows);

        okButton.updateProperties();
        cancelButton.updateProperties();
        settingsButton.updateProperties();

        logger.exiting("EmbossDialog", "<init>");

    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    private class PaperDimensionControl extends SettingControl<Setting<Length>>
                                     implements XTextListener,
                                                XSpinListener,
                                                XFocusListener,
                                                XItemListener {
        private final XWindow window;
        private final XSpinField spinButton;
        private final XNumericField numericField;
        private final XTextComponent textComponent;
        private final XListBox unitListbox;
        private boolean inches = false;
        
        public PaperDimensionControl(XControl valueControl,
                                     XControl unitControl,
                                     Setting<Length> dimension) {
            super(valueControl);
            numericField = (XNumericField)UnoRuntime.queryInterface(XNumericField.class, valueControl);
            textComponent = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, valueControl);
            numericField.setDecimalDigits((short)0);
            numericField.setMin((double)0);
            numericField.setMax((double)Integer.MAX_VALUE);
            window = (XWindow)UnoRuntime.queryInterface(XWindow.class, valueControl);
            spinButton = (XSpinField)UnoRuntime.queryInterface(XSpinField.class, valueControl);
            unitListbox = (XListBox)UnoRuntime.queryInterface(XListBox.class, unitControl);
            unitListbox.addItem("mm", (short)0);
            unitListbox.addItem("in", (short)1);
            unitListbox.selectItemPos((short)0, true);
            link(dimension);
            unitListbox.addItemListener(this);
        }

        public void update() {
            updateUnit();
            updateValue();
        }

        private void updateUnit() {
            inches = property.get().getUnitsOfLength() == Length.UnitsOfLength.INCH;
            unitListbox.selectItemPos((short)(inches?1:0), true);
            numericField.setDecimalDigits((short)(inches ? 2 : 0));
        }

        private void updateValue() {
            Length l = property.get();
            numericField.setValue(inches ? l.asInches() : l.asMillimeter());
        }

        public void save() {
            Length newValue = inches ? Length.newInchValue(numericField.getValue()) :
                                       Length.newMillimeterValue(numericField.getValue());
            okButton.updateProperties();
            if (property.accept(newValue)) {
                property.set(newValue);
            } else {
                okButton.disable();
            }
        }

        public void textChanged(TextEvent event) {
            if (event.Source.equals(numericField)) { save(); }
        }
        public void itemStateChanged(ItemEvent event) {
            if (event.Source.equals(unitListbox)) {
                inches = (unitListbox.getSelectedItemPos()==(short)1);
                numericField.setDecimalDigits((short)(inches?2:0));
                updateValue();
            }
        }
        private void spin(SpinEvent event) {
            if (event.Source.equals(spinButton)) {
                updateValue();
                okButton.updateProperties();
            }
        }
        public void up(SpinEvent event) { spin(event); }
        public void down(SpinEvent event) { spin(event); }
        public void first(SpinEvent event) {}
        public void last(SpinEvent event) {}
        public void focusGained(FocusEvent event) {}

        public void focusLost(FocusEvent event) {
            if (event.Source.equals(window)) {
                updateValue();
                okButton.updateProperties();
            }
        }

        public void listenControl(boolean onOff) {
            if (onOff) {
                textComponent.addTextListener(this);
                window.addFocusListener(this);
                spinButton.addSpinListener(this);
            } else {
                textComponent.removeTextListener(this);
                window.removeFocusListener(this);
                spinButton.removeSpinListener(this);
            }
        }
    }

    private class EmbosserWarningImage extends PropertyField<Property<Embosser>> {

        public EmbosserWarningImage(XControl control,
                                    Property<Embosser> embosserProperty) {
            super(control);
            link(embosserProperty);
        }

        public void update() {
            try {
                switch (embosserStatus.get(property.get().getIdentifier())) {
                    case ALPHA:
                    case BETA:
                        propertySet.setPropertyValue("ImageURL", warningImageUrl);
                        break;
                    case STABLE:
                    default:
                        propertySet.setPropertyValue("ImageURL", "");
                }
            } catch (UnknownPropertyException e) {
            } catch (PropertyVetoException e) {
            } catch (IllegalArgumentException e) {
            } catch (WrappedTargetException e) {
            }
        }
    }

    private class MarginControl extends NumericSettingControl {

        private EmbossConfiguration.MarginSetting marginSetting;

        public MarginControl(XControl control,
                             EmbossConfiguration.MarginSetting marginSetting) {
            super(control);
            this.marginSetting = marginSetting;
            link(marginSetting);
        }
        @Override
        public void update() {
            numericField.setValue((double)(marginSetting.get() + marginSetting.getOffset()));
        }
        @Override
        public void save() {
            marginSetting.set((int)(numericField.getValue()) - marginSetting.getOffset());
        }
    }

    private class OKButton extends Button implements PropertyListener {

        private PageFormatProperty pageFormat;
        private boolean enabled = false;

        public OKButton(XControl control, String label, PageFormatProperty pageFormat) {
            super(control, label);
            this.pageFormat = pageFormat;
            enabled = pageFormat.isValid();
            pageFormat.addListener(this);
        }

        public void disable() {
            try {
                propertySet.setPropertyValue("Enabled", false);
            } catch (UnknownPropertyException e) {
            } catch (PropertyVetoException e) {
            } catch (IllegalArgumentException e) {
            } catch (WrappedTargetException e) {
            }
        }

        @Override
        public void updateProperties() {
            try {
                propertySet.setPropertyValue("Enabled", enabled);
            } catch (UnknownPropertyException e) {
            } catch (PropertyVetoException e) {
            } catch (IllegalArgumentException e) {
            } catch (WrappedTargetException e) {
            }
        }

        public void propertyUpdated(PropertyEvent event) {
            if (event.getSource() == pageFormat) {
                enabled = pageFormat.isValid();
                updateProperties();
            }
        }

        public void actionPerformed(ActionEvent event) {}
    }

    /******************/
    /* EXECUTE DIALOG */
    /******************/

    public boolean execute() throws com.sun.star.uno.Exception {

        logger.entering("EmbossDialog", "execute");

        short ret = dialog.execute();

        component.dispose();

        if (settingsDialog != null) {
            settingsDialog.dispose();
        }

        logger.exiting("EmbossDialog", "execute");

        if (ret == ((short)PushButtonType.OK_value)) {
            return true;
        } else {
            return false;
        }
    }
}