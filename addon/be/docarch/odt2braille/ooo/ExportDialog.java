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

import java.util.logging.Logger;
import java.util.Locale;
import java.util.ResourceBundle;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XComponent;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XControl;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.ooo.dialog.*;
import org.daisy.braille.table.Table;
import org.daisy.braille.embosser.FileFormat;

/**
 *
 * @author   Bert Frees
 */
public class ExportDialog {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N_BUNDLE = Constants.OOO_L10N_PATH;

    private final Configuration settings;
    private final XComponentContext context;
    private final ProgressBar progressbar;
    private final XDialog dialog;
    private final XComponent component;
    private final XControl control;
    private SettingsDialog settingsDialog;


    /* BUTTONS */

    private final Button okButton;
    private final Button cancelButton;
    private final Button settingsButton;

    /* FIELDS & CONTROLS */

    private final ListBox<FileFormat> fileFormatListBox;
    private final ListBox<Table> charSetListBox;
    private final CheckBox duplexCheckBox;
    private final CheckBox eightDotsCheckBox;
    private final CheckBox multipleFilesCheckBox;
    private final NumericSettingControl columnsField;
    private final NumericSettingControl rowsField;

    /* LABELS */

    private final Label fileFormatLabel;
    private final Label charSetLabel;
    private final Label duplexLabel;
    private final Label eightDotsLabel;
    private final Label multipleFilesLabel;
    private final Label columnsLabel;
    private final Label rowsLabel;


    public ExportDialog(XComponentContext ctxt,
                        ExportConfiguration exportSettings,
                        Configuration cfg,
                        ProgressBar pb)
                 throws com.sun.star.uno.Exception {

        logger.entering("ExportDialog", "<init>");

        this.settings = cfg;
        this.context = ctxt;
        this.progressbar = pb;

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(context);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/ExportDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(context);
        dialog = xDialogProvider.createDialog(dialogUrl);
        XControlContainer container = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        component = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        control = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);
        XPropertySet windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());

        Locale oooLocale;
        try { oooLocale = UnoUtils.getUILocale(context); } catch (Exception e) {
              oooLocale = Locale.ENGLISH; }

        ResourceBundle bundle = ResourceBundle.getBundle(L10N_BUNDLE, oooLocale);

        windowProperties.setPropertyValue("Title", bundle.getString("exportDialogTitle"));

        /* DIALOG ELEMENTS */

        okButton = new Button(container.getControl("CommandButton2"),
                              bundle.getString("exportButton")) {
            public void actionPerformed(ActionEvent event) {}
        };

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

        fileFormatListBox = new ListBox<FileFormat>(container.getControl("ListBox2")) {
            @Override
            protected String getDisplayValue(FileFormat f) {
                return f.getDisplayName();
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

        duplexCheckBox = new CheckBox(container.getControl("CheckBox1"));

        eightDotsCheckBox = new CheckBox(container.getControl("CheckBox2"));

        multipleFilesCheckBox = new CheckBox(container.getControl("CheckBox3"));

        columnsField = new NumericSettingControl(container.getControl("NumericField3"));

        rowsField = new NumericSettingControl(container.getControl("NumericField4"));


        /* LABELS */

        fileFormatLabel = new Label(container.getControl("Label2"),
                                    bundle.getString("brailleFileLabel") + ":");

        charSetLabel = new Label(container.getControl("Label3"),
                                 bundle.getString("tableLabel") + ":");

        duplexLabel = new Label(container.getControl("Label8"),
                                bundle.getString("duplexLabel"));

        eightDotsLabel = new Label(container.getControl("Label1"),
                                   bundle.getString("eightDotsLabel"));

        multipleFilesLabel = new Label(container.getControl("Label4"),
                                       bundle.getString("multipleFilesLabel"));

        columnsLabel = new Label(container.getControl("Label6"),
                                 bundle.getString("numberOfCellsPerLineLabel") + ":");

        rowsLabel = new Label(container.getControl("Label7"),
                              bundle.getString("numberOfLinesPerPageLabel") + ":");

        /* INITIALIZE */

        fileFormatListBox.link(exportSettings.fileFormat);
        charSetListBox.link(exportSettings.charSet);
        duplexCheckBox.link(exportSettings.duplex);
        eightDotsCheckBox.link(exportSettings.eightDots);
        multipleFilesCheckBox.link(exportSettings.multipleFiles);
        columnsField.link(exportSettings.columns);
        rowsField.link(exportSettings.rows);

        okButton.updateProperties();
        cancelButton.updateProperties();
        settingsButton.updateProperties();

        logger.exiting("ExportDialog", "<init>");

    }

    /******************/
    /* EXECUTE DIALOG */
    /******************/

    public boolean execute() throws com.sun.star.uno.Exception {

        logger.entering("ExportDialog", "execute");

        short ret = dialog.execute();

        component.dispose();

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
}